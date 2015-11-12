/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.arquillian.container.domain.managed;

import static org.wildfly.core.launcher.ProcessHelper.addShutdownHook;
import static org.wildfly.core.launcher.ProcessHelper.destroyProcess;
import static org.wildfly.core.launcher.ProcessHelper.processHasDied;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.arquillian.container.domain.CommonDomainDeployableContainer;
import org.jboss.as.arquillian.container.domain.Domain;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.core.launcher.DomainCommandBuilder;
import org.wildfly.core.launcher.Launcher;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagedDomainDeployableContainer extends CommonDomainDeployableContainer<ManagedDomainContainerConfiguration> {

    static final String TEMP_CONTAINER_DIRECTORY = "arquillian-temp-container";

    static final String CONFIG_DIR = "configuration";
    static final String DATA_DIR = "data";
    static final String SERVERS_DIR = "servers";

    private final Logger log = Logger.getLogger(ManagedDomainDeployableContainer.class.getName());

    private Thread shutdownThread;
    private Process process;

    @Override
    public Class<ManagedDomainContainerConfiguration> getConfigurationClass() {
        return ManagedDomainContainerConfiguration.class;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        ManagedDomainContainerConfiguration config = getContainerConfiguration();

        if (isServerRunning()) {
            if (config.isAllowConnectingToRunningServer()) {
                return;
            } else {
                failDueToRunning();
            }
        }

        try {
            final DomainCommandBuilder commandBuilder = DomainCommandBuilder.of(config.getJbossHome(), config.getJavaHome());
            if (config.getJavaVmArguments() != null) {
                final String[] javaOpts = config.getJavaVmArguments().split("\\s+");
                commandBuilder.setProcessControllerJavaOptions(javaOpts)
                        .setHostControllerJavaOptions(javaOpts);
            }

            if (config.isSetupCleanServerBaseDir() || config.getCleanServerBaseDir() != null) {
                setupCleanServerDirectories(commandBuilder, config.getCleanServerBaseDir());
            }

            final String modulesPath = config.getModulePath();
            if (modulesPath != null && !modulesPath.isEmpty()) {
                commandBuilder.addModuleDirs(modulesPath.split(Pattern.quote(File.pathSeparator)));
            }

            if (config.isEnableAssertions()) {
                commandBuilder.addHostControllerJavaOption("-ea")
                        .addProcessControllerJavaOption("-ea");
            }
            if (config.getDomainConfig() != null) {
                commandBuilder.setDomainConfiguration(config.getDomainConfig());
            }
            if (config.getHostConfig() != null) {
                commandBuilder.setHostConfiguration(config.getHostConfig());
            }

            // Set server arguments if not null
            final String serverArgs = config.getJbossArguments();
            if (serverArgs != null) {
                commandBuilder.addServerArguments(serverArgs.split("\\s+"));
            }

            // Previous versions of arquillian set the jboss.home.dir property in the JVM properties.
            // Some tests may rely on this behavior, but could be considered to be removed as all the scripts add this
            // property after the modules path (-mp) has been defined. The command builder will set the property after
            // the module path has been defined as well.
            commandBuilder.addProcessControllerJavaOption("-Djboss.home.dir=" + commandBuilder.getWildFlyHome());

            log.info("Starting container with: " + commandBuilder.build());
            process = Launcher.of(commandBuilder).setRedirectErrorStream(true).launch();
            new Thread(new ConsoleConsumer()).start();
            shutdownThread = addShutdownHook(process);

            long startupTimeout = getContainerConfiguration().getStartupTimeoutInSeconds();
            long timeout = startupTimeout * 1000;
            boolean serverAvailable = false;
            long sleep = 1000;
            while (timeout > 0 && serverAvailable == false) {
                long before = System.currentTimeMillis();
                serverAvailable = getManagementClient().isDomainInRunningState();
                timeout -= (System.currentTimeMillis() - before);
                if (!serverAvailable) {
                    if (processHasDied(process))
                        break;
                    Thread.sleep(sleep);
                    timeout -= sleep;
                    sleep = Math.max(sleep / 2, 100);
                }
            }
            if (!serverAvailable) {
                destroyProcess(process);
                throw new TimeoutException(String.format("Managed Domain server was not started within [%d] s",
                        config.getStartupTimeoutInSeconds()));
            }
        } catch (Exception e) {
            throw new LifecycleException("Could not start container", e);
        }
    }

    @Override
    protected void waitForStart(Domain domain, ManagementClient client) throws LifecycleException {
        waitForAutoStartServersToStart(domain, client);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        if (shutdownThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            shutdownThread = null;
        }
        try {
            if (process != null) {
                Thread shutdown = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(getContainerConfiguration().getStopTimeoutInSeconds() * 1000);
                        } catch (InterruptedException e) {
                            return;
                        }

                        // The process hasn't shutdown within 60 seconds. Terminate forcibly.
                        if (process != null) {
                            process.destroy();
                        }
                    }
                });
                shutdown.start();

                // Fetch the local-host-name attribute (e.g. "master")
                ModelNode op = new ModelNode();
                op.get("operation").set("read-attribute");
                op.get("name").set("local-host-name");
                ModelNode result = getManagementClient().getControllerClient().execute(op, null);
                String hostName = result.get("result").asString();

                // AS7-6620: Create the shutdown operation and run it asynchronously and wait for process to terminate
                op = new ModelNode();
                op.get("operation").set("shutdown");
                ModelNode address = op.get("address");
                address.add("host", hostName);
                getManagementClient().getControllerClient().executeAsync(op, null);

                process.waitFor();
                process = null;

                shutdown.interrupt();
            }
        } catch (Exception e) {
            throw new LifecycleException("Could not stop container", e);
        }
    }

    private boolean isServerRunning() {
        Socket socket = null;
        try {
            socket = new Socket(getContainerConfiguration().getManagementAddress(), getContainerConfiguration()
                    .getManagementPort());
        } catch (Exception ignored) { // nothing is running on defined ports
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException("Could not close isServerStarted socket", e);
                }
            }
        }
        return true;
    }

    private void failDueToRunning() throws LifecycleException {
        throw new LifecycleException("The server is already running! "
                + "Managed containers do not support connecting to running server instances due to the "
                + "possible harmful effect of connecting to the wrong server. Please stop server before running or "
                + "change to another type of container.\n"
                + "To disable this check and allow Arquillian to connect to a running server, "
                + "set allowConnectingToRunningServer to true in the container configuration");
    }

    private void waitForAutoStartServersToStart(Domain domain, ManagementClient client) {
        Set<Server> servers = domain.getAutoStartServers();

        long startupTimeout = getContainerConfiguration().getAutoServerStartupTimeoutInSeconds();
        long timeout = startupTimeout * 1000;
        long sleep = 100;

        while (timeout > 0 && servers.size() > 0) {
            Iterator<Server> serverIterator = servers.iterator();
            while (serverIterator.hasNext()) {
                Server server = serverIterator.next();
                if (client.isServerStarted(server)) {
                    serverIterator.remove();
                }
            }
            try {
                Thread.sleep(sleep);
                timeout -= sleep;
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed waiting for servers to start", e);
            }
        }
        if (timeout <= 0) {
            throw new RuntimeException(
                    "Auto started servers did not start within set timeout [autoServerStartupTimeoutInSeconds=" + startupTimeout + "]. " + servers);
        }
    }

    /**
     * Runnable that consumes the output of the process. If nothing consumes the output the AS will hang on some platforms
     * @author Stuart Douglas
     */
    private class ConsoleConsumer implements Runnable {

        @Override
        public void run() {
            final InputStream stream = process.getInputStream();
            final boolean writeOutput = getContainerConfiguration().isOutputToConsole();

            try {
                byte[] buf = new byte[32];
                int num;
                // Do not try reading a line cos it considers '\r' end of line
                while ((num = stream.read(buf)) != -1) {
                    if (writeOutput)
                        System.out.write(buf, 0, num);
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Setup clean directories to run the container.
     * @param cleanServerBaseDirPath the clean server base directory
     */
    private static void setupCleanServerDirectories(final DomainCommandBuilder commandBuilder, final String cleanServerBaseDirPath) throws IOException {
        final Path cleanBase;
        if (cleanServerBaseDirPath != null) {
            cleanBase = Paths.get(cleanServerBaseDirPath);
        } else {
            cleanBase = Files.createTempDirectory(TEMP_CONTAINER_DIRECTORY);
        }

        if (Files.notExists(cleanBase)) {
            throw serverBaseDirectoryDoesNotExist(cleanBase.toFile());
        }
        if (!Files.isDirectory(cleanBase)) {
            throw serverBaseDirectoryIsNotADirectory(cleanBase.toFile());
        }

        final Path currentConfigDir = commandBuilder.getConfigurationDirectory();
        final Path configDir = cleanBase.resolve(CONFIG_DIR);
        copyDir(currentConfigDir, configDir);

        final Path currentDataDir = commandBuilder.getBaseDirectory().resolve(DATA_DIR);
        if (Files.exists(currentDataDir)) {
            copyDir(currentDataDir, cleanBase.resolve(DATA_DIR));
        }
        commandBuilder.setBaseDirectory(cleanBase);
        commandBuilder.setConfigurationDirectory(configDir);
    }

    static IllegalStateException serverBaseDirectoryDoesNotExist(File f) {
        return new IllegalStateException(String.format("Server base directory does not exist: %s", f));
    }

    static IllegalStateException serverBaseDirectoryIsNotADirectory(File file) {
        return new IllegalStateException(String.format("Server base directory is not a directory: %s", file));
    }

    private static void copyDir(final Path from, final Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Files.copy(dir, to.resolve(from.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
