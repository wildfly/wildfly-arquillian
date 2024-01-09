/*
 * Copyright 2023 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.as.arquillian.container.ParameterUtils;
import org.jboss.logging.Logger;
import org.wildfly.plugin.tools.ServerHelper;

/**
 * A wrapper for an application client process. Allows interacting with the application client process.
 *
 * @author Dominik Pospisil <dpospisi@redhat.com>
 * @author Stuart Douglas
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class AppClientWrapper implements AutoCloseable {
    private final BlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();
    private final ManagedContainerConfiguration config;
    private final Logger log;
    private final Lock lock;
    private Process process;
    private ExecutorService executorService;

    private Future<?> stdoutConsumer;
    private Future<?> stderrConsumer;

    /**
     * Creates a new application client wrapper.
     *
     * @param config the configuration for the container
     * @param log    the logger to use
     */
    protected AppClientWrapper(final ManagedContainerConfiguration config, final Logger log) {
        this.config = config;
        this.log = log;
        lock = new ReentrantLock();
    }

    /**
     * If the application client has started, causes the current thread to wait, if necessary, until the application
     * client terminates or the specified wait has been reached.
     *
     * <p>
     * If the application client has not started a value of {@code false} is returned and an error message logged
     * indicating the application client has not started.
     * </p>
     *
     * <p>
     * If the application client has started and the process has terminated, this method returns {@code true}
     * immediately.
     * </p>
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the {@code timeout} argument
     *
     * @return {@code true} if the application client process has exited and {@code false} if the waiting time elapsed
     *             before the process has exited
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws NullPointerException if unit is null
     * @see Process#waitFor(long, TimeUnit)
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean waitForExit(final long timeout, final TimeUnit unit) throws InterruptedException {
        try {
            lock.lock();
            if (process != null) {
                try {
                    final boolean b = process.waitFor(timeout, unit);
                    process = null;
                    return b;
                } finally {
                    close();
                }
            }
        } finally {
            lock.unlock();
        }
        log.warn("waitForExit was invoked before the process was started.");
        return false;
    }

    /**
     * Consumes all available output from application client using the output queue filled by the process
     * standard out reader thread.
     *
     * @param timeout number of milliseconds to wait for each subsequent line
     *
     * @return list of application client output lines
     */
    public List<String> readAll(final long timeout) {
        final List<String> lines = new ArrayList<>();
        String line = null;
        do {
            try {
                line = outputQueue.poll(timeout, TimeUnit.MILLISECONDS);
                if (line != null)
                    lines.add(line);
            } catch (InterruptedException ignore) {
            }

        } while (line != null);
        return List.copyOf(lines);
    }

    /**
     * Starts the application client in a new process and creates two thread to read the process output ({@code stdout})
     * and error streams ({@code stderr}).
     *
     * @throws IOException if there is a failure to start the application client process
     */
    public void run() throws IOException {
        try {
            lock.lock();
            if (process == null) {
                process = new ProcessBuilder(getAppClientCommand())
                        .start();
                executorService = Executors.newFixedThreadPool(2);
                stdoutConsumer = executorService
                        .submit(new LogConsumer(outputQueue, process.getInputStream(), Logger.Level.INFO, process.pid()));
                stderrConsumer = executorService
                        .submit(new LogConsumer(null, process.getErrorStream(), Logger.Level.ERROR, process.pid()));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Kills the application client.
     */
    @Override
    public void close() {
        try {
            lock.lock();
            if (process != null) {
                process.destroy();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            stdoutConsumer.cancel(true);
            stderrConsumer.cancel(true);
            executorService.shutdownNow();
        } finally {
            lock.unlock();
        }
    }

    private List<String> getAppClientCommand() {
        final List<String> cmd = new ArrayList<>();

        final String archivePath = config.getClientAppEar();
        final String clientArchiveName = config.getClientArchiveName();

        final String jbossHome = config.getJbossHome();
        if (jbossHome == null)
            throw new IllegalArgumentException("jbossHome config property is not set.");
        if (!ServerHelper.isValidHomeDirectory(jbossHome))
            throw new IllegalArgumentException("Server directory from config jbossHome doesn't exist: " + jbossHome);

        final String archiveArg = String.format("%s#%s", archivePath, clientArchiveName);

        final String client = config.resolveAppClientCommand();
        final Path clientExe = Path.of(jbossHome, "bin", client);
        if (Files.notExists(clientExe)) {
            throw new IllegalArgumentException("Could not find appclient executable " + clientExe);
        }
        cmd.add(clientExe.toString());
        cmd.add(archiveArg);
        if (config.getClientArguments() != null) {
            cmd.addAll(ParameterUtils.splitParams(config.getClientArguments()));
        }

        log.info("AppClient cmd: " + cmd);
        return cmd;
    }

    private class LogConsumer implements Runnable {
        private final BlockingQueue<String> queue;
        private final InputStreamReader reader;
        private final Logger.Level level;
        private final long pid;

        private LogConsumer(final BlockingQueue<String> queue, final InputStream in, final Logger.Level level, final long pid) {
            this.queue = queue;
            this.reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            this.level = level;
            this.pid = pid;
        }

        @Override
        public void run() {
            final StringBuilder buffer = new StringBuilder();
            final char[] inBuffer = new char[256];
            int len;
            try {
                while ((len = reader.read(inBuffer)) != -1) {
                    int mark = 0;
                    int i;
                    for (i = 0; i < len; i++) {
                        final char c = inBuffer[i];
                        if (c == '\n') {
                            buffer.append(inBuffer, mark, i - mark);
                            log.log(level, buffer.toString());
                            if (queue != null) {
                                queue.add(buffer.toString());
                            }
                            buffer.setLength(0);
                            mark = i + 1;
                        }
                    }
                    buffer.append(inBuffer, mark, i - mark);
                }
                // If we're here, we should log the buffer if it's not empty
                if (buffer.length() > 0) {
                    log.log(level, buffer.toString());
                    if (queue != null) {
                        queue.add(buffer.toString());
                    }
                }
            } catch (IOException e) {
                if (buffer.length() > 0) {
                    log.errorf(e, "Failed to consume output from %s: %s", pid, buffer.toString());
                    buffer.setLength(0);
                } else {
                    log.errorf(e, "Failed to consume output from %s", pid);
                }
            }
        }
    }

}
