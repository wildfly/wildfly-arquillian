/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.arquillian.container;

import static org.wildfly.core.launcher.ProcessHelper.addShutdownHook;
import static org.wildfly.core.launcher.ProcessHelper.destroyProcess;
import static org.wildfly.core.launcher.ProcessHelper.processHasDied;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.wildfly.core.launcher.CommandBuilder;
import org.wildfly.core.launcher.Launcher;

/**
 * A deployable container that manages a {@linkplain Process process}.
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 3.0.0
 */
@SuppressWarnings("MagicNumber")
public abstract class CommonManagedDeployableContainer<T extends CommonManagedContainerConfiguration>
        extends CommonDeployableContainer<T> {

    private static final int PORT_RANGE_MIN = 1;
    private static final int PORT_RANGE_MAX = 65535;
    private Thread shutdownThread = null;
    private Process process = null;
    private boolean timeoutSupported = false;

    @Override
    @SuppressWarnings("FeatureEnvy")
    protected void startInternal() throws LifecycleException {
        final T config = getContainerConfiguration();
        if (isServerRunning(config)) {
            if (config.isAllowConnectingToRunningServer()) {
                return;
            } else {
                failDueToRunning(config);
            }
        }

        try {
            final CommandBuilder commandBuilder = createCommandBuilder(config);

            // Wait on ports before launching; AS7-4070
            this.waitOnPorts(config);

            getLogger().info("Starting container with: " + commandBuilder.build());
            final Process process = Launcher.of(commandBuilder).setRedirectErrorStream(true).launch();
            new Thread(new ConsoleConsumer(process, config.isOutputToConsole())).start();
            shutdownThread = addShutdownHook(process);

            long startupTimeout = config.getStartupTimeoutInSeconds();
            long timeout = startupTimeout * 1000;
            boolean serverAvailable = false;
            long sleep = 1000;
            while (timeout > 0 && !serverAvailable) {
                long before = System.currentTimeMillis();
                serverAvailable = getManagementClient().isServerInRunningState();
                timeout -= (System.currentTimeMillis() - before);
                if (!serverAvailable) {
                    if (processHasDied(process)) {
                        final String msg = String.format(
                                "The java process starting the managed server exited unexpectedly with code [%d]",
                                process.exitValue());
                        throw new LifecycleException(msg);
                    }
                    Thread.sleep(sleep);
                    timeout -= sleep;
                    sleep = Math.max(sleep / 2, 100);
                }
            }
            if (!serverAvailable) {
                destroyProcess(process);
                throw new TimeoutException(String.format("Managed server was not started within [%d] s", startupTimeout));
            }
            timeoutSupported = isOperationAttributeSupported("shutdown", "timeout");
            this.process = process;

        } catch (LifecycleException e) {
            throw e;
        } catch (Exception e) {
            throw new LifecycleException("Could not start container", e);
        }
    }

    /**
     * Returns the command builder used to launch the server.
     *
     * @return the command builder
     */
    protected abstract CommandBuilder createCommandBuilder(T config);

    /**
     * The logger used for messages.
     *
     * @return the logger
     */
    protected abstract Logger getLogger();

    /**
     * If specified in the configuration, waits on the specified ports to become
     * available for the specified time, else throws a {@link PortAcquisitionTimeoutException}
     *
     * @throws PortAcquisitionTimeoutException if a timeout occurs
     */
    private void waitOnPorts(final T config) throws PortAcquisitionTimeoutException {
        // Get the config
        final Integer[] ports = config.getWaitForPorts();
        final int timeoutInSeconds = config.getWaitForPortsTimeoutInSeconds();

        // For all ports we'll wait on
        if (ports != null && ports.length > 0) {
            for (final int port : ports) {
                final long start = System.currentTimeMillis();
                // If not available
                while (!this.isPortAvailable(port)) {

                    // Get time elapsed
                    final int elapsedSeconds = (int) ((System.currentTimeMillis() - start) / 1000);

                    // See that we haven't timed out
                    if (elapsedSeconds > timeoutInSeconds) {
                        throw new PortAcquisitionTimeoutException(port, timeoutInSeconds);
                    }
                    try {
                        // Wait a bit, then try again.
                        TimeUnit.MILLISECONDS.sleep(500L);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Log that we're waiting
                    getLogger().warnf("Waiting on port %d to become available for %ds", port,
                            (timeoutInSeconds - elapsedSeconds));
                }
            }
        }
    }

    private boolean isPortAvailable(final int port) {
        // Precondition checks
        if (port < PORT_RANGE_MIN || port > PORT_RANGE_MAX) {
            throw new IllegalArgumentException("Port specified is out of range: " + port);
        }

        try (ServerSocket ss = new ServerSocket(port); DatagramSocket ds = new DatagramSocket(port)) {
            // Attempt both TCP and UDP
            // So we don't block from using this port while it's in a TIMEOUT state after we release it
            ss.setReuseAddress(true);
            ds.setReuseAddress(true);
            // Could be acquired
            return true;
        } catch (final IOException ignore) {
            // Swallow
        }

        // Couldn't be acquired
        return false;
    }

    @Override
    protected void stopInternal(final Integer timeout) throws LifecycleException {
        if (shutdownThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            shutdownThread = null;
        }
        final Process process = this.process;
        this.process = null;
        try {
            if (process != null) {
                final Logger logger = getLogger();

                // AS7-6620: Create the shutdown operation and run it asynchronously and wait for process to terminate
                final ModelNode op = Operations.createOperation("shutdown");
                if (timeoutSupported) {
                    if (timeout != null) {
                        op.get("timeout").set(timeout);
                    }
                } else {
                    getLogger().error(String.format("Timeout is not supported for %s on the shutdown operation.",
                            getContainerDescription()));
                }

                // If the process is not alive there is no sense it invoking a shutdown operation.
                if (process.isAlive()) {
                    final ManagementClient client = getManagementClient();
                    if (client == null) {
                        logger.error("The management client does not seem to be active. Forcibly destroying the process.");
                        process.destroyForcibly();
                    } else {
                        final ModelNode result = client.getControllerClient().execute(op);
                        if (!Operations.isSuccessfulOutcome(result)) {
                            // Don't fail stopping, but we should log an error
                            logger.errorf("Failed to shutdown the server: %s",
                                    Operations.getFailureDescription(result).asString());
                            process.destroyForcibly();
                        }
                    }
                }

                final int timeoutSeconds = getContainerConfiguration().getStartupTimeoutInSeconds();
                if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                    // Log a warning indicating the timeout happened
                    logger.warnf("The container process did not exit within %d seconds. Forcibly destroying the process.",
                            timeoutSeconds);
                    process.destroyForcibly();
                }
            }
        } catch (Exception e) {
            try {
                destroyProcess(process);
            } catch (Exception ignore) {
            }
            throw new LifecycleException("Could not stop container", e);
        }
    }

    private boolean isServerRunning(final T config) {
        Socket socket = null;
        try {
            socket = new Socket(
                    config.getManagementAddress(),
                    config.getManagementPort());
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

    private void failDueToRunning(final T config) throws LifecycleException {
        final int managementPort = config.getManagementPort();
        throw new LifecycleException(
                String.format("The port %1$d is already in use. It means that either the server might be already running " +
                        "or there is another process using port %1$d.%n" +
                        "Managed containers do not support connecting to running server instances due to the " +
                        "possible harmful effect of connecting to the wrong server.%n" +
                        "Please stop server (or another process) before running, " +
                        "change to another type of container (e.g. remote) or use jboss.socket.binding.port-offset variable " +
                        "to change the default port.%n" +
                        "To disable this check and allow Arquillian to connect to a running server, " +
                        "set allowConnectingToRunningServer to true in the container configuration",
                        managementPort));
    }

    /**
     * Runnable that consumes the output of the process. If nothing consumes the output the AS will hang on some platforms
     *
     * @author Stuart Douglas
     */
    private static class ConsoleConsumer implements Runnable {
        private final Process process;
        private final boolean writeOutput;

        private ConsoleConsumer(final Process process, final boolean writeOutput) {
            this.process = process;
            this.writeOutput = writeOutput;
        }

        @Override
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        public void run() {
            final InputStream stream = process.getInputStream();

            try {
                byte[] buf = new byte[32];
                int num;
                // Do not try reading a line cos it considers '\r' end of line
                while ((num = stream.read(buf)) != -1) {
                    if (writeOutput)
                        System.out.write(buf, 0, num);
                }
            } catch (IOException ignore) {
            }
        }
    }
}
