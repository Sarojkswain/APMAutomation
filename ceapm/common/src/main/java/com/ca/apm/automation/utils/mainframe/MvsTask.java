/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.utils.mainframe;

import com.ca.apm.automation.utils.mainframe.ControlMvsTaskContext.Builder;

import org.apache.commons.io.IOUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

/**
 * Utility class that provides ability to control MVS started tasks on z/OS.
 */
public class MvsTask {

    public enum Command {
        START, STOP, MODIFY;

        public String getCommand() {
            return toString();
        }
    }

    public enum State {
        STOPPED(Command.STOP), RUNNING(Command.START);

        private final Command defaultCommand;

        State(Command defaultCommand) {
            this.defaultCommand = defaultCommand;
        }

        public Command getDefaultCommand() {
            return defaultCommand;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MvsTask.class);

    /** Default timeout [seconds] to wait for task state change. */
    public static final int DEFAULT_TIMEOUT = 60;

    /**
     * Execute operations on task as specified in the context.
     *
     * @param ctx Operation context.
     * @throws IOException on error during execution of command.
     * @throws TimeoutException when timed out waiting for an operation to finish.
     * @see ControlMvsTaskContext
     */
    public static void execute(ControlMvsTaskContext ctx) throws IOException, TimeoutException {
        final String taskName = ctx.getTaskName();
        final String taskLpar = ctx.getTaskLpar();
        final State desiredState = ctx.getDesiredState();
        final String resultMessage =
            desiredState.toString().toLowerCase() + (taskLpar == null ? "" : " on " + taskLpar);
        final Command command = ctx.getCommand();
        final int timeout = ctx.getTimeout();

        State currentState = getTaskState(taskName, taskLpar);
        if (ctx.isCheckOnly()) {
            if (currentState != desiredState) {
                throw new IllegalStateException("Task '" + taskName + "' is not " + resultMessage);
            }
            return;
        }
        assert !(ctx.getIgnoreCurrentState() && ctx.getRestartIfRunning()); // mutually exclusive
        if (!ctx.getIgnoreCurrentState() && currentState == desiredState) {
            logger.debug("Task '{}' is already {}", taskName, resultMessage);
            return;
        }
        if (ctx.getRestartIfRunning() && desiredState == State.RUNNING
            && currentState == State.RUNNING) {
            logger.debug("Stopping task '{}' before restarting", taskName);
            // In this case we use a minimum value for the timeout period
            // because the subsequent start completely depends on the
            // stop operation finishing first.
            stop(taskName, taskLpar, Math.max(timeout, 60));
        }

        switch (command) {
            case STOP:
                logger.debug("Issuing {} for the '{}' task", command, taskName);
                executeCommand(new CommandBuilder(command).add(taskName).build());
                break;
            case START:
                executeCommand(new CommandBuilder(command).add(ctx.getJclMember())
                    .add("JOBNAME", taskName).add(ctx.getTaskParms()).build());
                break;
            default:
                throw new IllegalStateException("Unknown action value: " + command);
        }

        if (timeout > 0) {
            waitForTask(taskName, taskLpar, timeout, desiredState);
        }
        logger.info("Task '{}' is {}", taskName, resultMessage);
    }

    /**
     * Stops a running task.
     *
     * @param taskName Name of the task to stop.
     * @param taskLpar Expected LPAR for the task. If {@code null} or empty any LPAR will match.
     * @param timeout Number of seconds to wait for the task to stop.
     * @throws IOException When querying of the task status fails unexpectedly.
     * @throws TimeoutException If the process of re(starting) the task times out.
     */
    public static void stop(String taskName, @Nullable String taskLpar, int timeout)
        throws IOException, TimeoutException {
        Builder context = new ControlMvsTaskContext.Builder(taskName, State.STOPPED)
        .timeout(timeout);
        if (taskLpar != null) {
            context.taskLpar(taskLpar);
        }
        execute(context.build());
    }

    /**
     * Starts a task if not already running.
     *
     * @see #start(String, String, Map, boolean, boolean, int)
     */
    public static void start(String jclMember, @Nullable String taskLpar) throws IOException,
        TimeoutException {
        start(jclMember, taskLpar, null, false, false, DEFAULT_TIMEOUT);
    }

    /**
     * Starts a task if not already running, using provided timeout.
     *
     * @see #start(String, String, Map, boolean, boolean, int)
     */
    public static void start(String jclMember, @Nullable String taskLpar, int timeout)
        throws IOException, TimeoutException {
        start(jclMember, taskLpar, null, false, false, timeout);
    }

    /**
     * Starts or restarts a task. Provides ability to specify task parameters.
     *
     * @param jclMember Name of the JCL member to start the task with.
     * @param taskLpar Expected LPAR for the task. If {@code null} or empty any LPAR will match.
     * @param taskParms Custom JCL parameters stored as map where key is the parameter name and
     *        value is the parameter value.
     * @param restartIfRunning Whether the task should be restarted if already running.
     *        Must be {@code false} if {@code force} is {@code true}.
     * @param force Force start regardless of current task state.
     * @param timeout How long to wait for the task to start if timeout is positive. Also how long
     *        to wait for the task to stop if being restarted and the value is above 60.
     * @throws IOException When querying of the task status fails unexpectedly.
     * @throws TimeoutException If the process of re(starting) the task times out.
     */
    public static void start(String jclMember, @Nullable String taskLpar,
        @Nullable Map<String, String> taskParms, boolean restartIfRunning, boolean force,
        int timeout) throws IOException, TimeoutException {
        ControlMvsTaskContext.Builder context =
            new ControlMvsTaskContext.Builder(jclMember, State.RUNNING).timeout(timeout);
        if (taskLpar != null) {
            context.taskLpar(taskLpar);
        }
        if (taskParms != null && !taskParms.isEmpty()) {
            context.taskParms(taskParms);
        }
        if (restartIfRunning) {
            context.restartIfRunning();
        }
        if (force) {
            context.ignoreCurrentState();
        }
        execute(context.build());
    }

    /**
     * Execute MVS command and expect RC=0.
     *
     * @param command MVS command to execute
     * @throws IOException when execution fails or ends with nonzero RC.
     */
    private static void executeCommand(String command) throws IOException {
        try (Mvs mvs = new Mvs()) {
            int rc = mvs.execute(command);

            if (rc != 0) { // Dump the whole output of the command to the log.
                try (BufferedReader output =
                    new BufferedReader(new InputStreamReader(mvs.getStdoutStream()))) {
                    String line;
                    while ((line = output.readLine()) != null) {
                        logger.error(line);
                    }
                }
                throw new IllegalStateException("Failed to execute '" + command + "', rc="
                    + String.valueOf(rc));
            }
        }
    }

    /**
     * Waits until a task enters a specific state or a timeout expires.
     *
     * @param taskName Name of the task to wait on.
     * @param taskLpar Expected LPAR for the task. If {@code null} or empty any LPAR will match.
     * @param timeout How long to wait for the task to change state [seconds].
     * @param state Expected task state.
     * @throws IOException When querying of the task status fails unexpectedly.
     * @throws TimeoutException If a timeout occurs while waiting for the task.
     */
    public static void waitForTask(String taskName, @Nullable String taskLpar, int timeout,
        State state) throws IOException, TimeoutException {
        Args.notBlank(taskName, "taskName");
        Args.positive(timeout, "timeout");

        String action = state.getDefaultCommand().toString().toLowerCase();

        logger.debug("Waiting for the '{}' task to {}", taskName, action);
        long end = System.currentTimeMillis() + timeout * 1000;
        while (getTaskState(taskName, taskLpar) != state && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for task '{}' to {}", taskName, action);
                break; // Stop waiting if interrupted
            }
        }
        if (getTaskState(taskName, taskLpar) != state) {
            throw new TimeoutException("Timed out while waiting for task '" + taskName + "' to "
                + action);
        }
    }

    /**
     * Checks whether a task is in a specific state.
     *
     * @param taskName Name of the task to check.
     * @param taskLpar Expected LPAR for the task. If {@code null} or empty any LPAR will match.
     * @param state Expected task state.
     * @return {@code true} if the task is in the expected state, {@code false} otherwise.
     * @throws IOException When querying of the task status fails unexpectedly.
     */
    public static boolean isTaskInState(String taskName, @Nullable String taskLpar, State state)
        throws IOException {
        Args.notBlank(taskName, "taskName");

        return getTaskState(taskName, taskLpar) == state;
    }

    /**
     * Returns the current state of the task.
     *
     * @param taskName Name of the task to query.
     * @param taskLpar LPAR to check for. If {@code null} or empty a random LPAR will be checked.
     * @return Task state.
     * @throws IOException If unable to query the task state.
     */
    public static State getTaskState(String taskName, @Nullable String taskLpar) throws IOException {
        boolean isRunning = false;

        try (Mvs mvs = new Mvs()) {
            String command = "$DS(" + taskName + ")";

            int rc = mvs.execute(command);
            if (rc != 0) {
                logger.debug(IOUtils.toString(mvs.getStdoutStream()));
                logger.error("Completed with rc={}", rc);
                throw new IllegalStateException("Unable to obtain task status for " + taskName);
            }

            try (BufferedReader output =
                new BufferedReader(new InputStreamReader(mvs.getStdoutStream()))) {

                String executionState = "EXECUTING/";
                if (taskLpar != null && !taskLpar.isEmpty()) {
                    executionState += taskLpar;
                }

                for (String line = output.readLine(); !isRunning && line != null; line =
                    output.readLine()) {
                    isRunning = line.contains(executionState);
                }
            }
        }

        return isRunning ? State.RUNNING : State.STOPPED;
    }

    /**
     * Builder for MVS commands with parameters.
     *
     * @see <a href=https://www.ibm.com/support/knowledgecenter/SSLTBW_2.1.0/com.ibm.zos.v2r1.ieag100/skp1.htm>IBM doc</a>
     */
    public static class CommandBuilder {
        private static final int MAX_VALUE_LENGTH = 44;
        private static final int MAX_KEY_VALUE_LENGTH = 66;

        private final Command command;
        private final ArrayList<String> positional;
        private final LinkedHashMap<String, String> keyword;
        private String fullCommand;

        /**
         * Command builder for specified command.
         *
         * @param command MVS command.
         */
        public CommandBuilder(Command command) {
            Args.notNull(command, "command");
            this.command = command;
            positional = new ArrayList<>();
            keyword = new LinkedHashMap<>();
        }

        /**
         * Add positional parameter.
         *
         * @param parameter Parameter value, can be blank.
         * @return Builder instance the method was called on.
         */
        public CommandBuilder add(String parameter) {
            Args.notNull(parameter, "parameter");
            positional.add(parameter);
            return builder();
        }

        /**
         * Add keyword parameter. Value of keyword parameter that is specified multiple times is
         * overridden. Order of keyword parameters is order of last addition.
         *
         * @param parameter Parameter name.
         * @param value Parameter value, can be blank.
         * @return Builder instance the method was called on.
         */
        public CommandBuilder add(String parameter, String value) {
            Args.notBlank(parameter, "parameter");
            Args.notNull(value, "value " + parameter);
            value = escapeValue(value);
            Args.check(value.length() <= MAX_VALUE_LENGTH, "value too long: " + value);
            Args.check(parameter.length() + value.length() <= MAX_KEY_VALUE_LENGTH - 1,
                "key=value pair too long");
            keyword.remove(parameter); // order according to last addition
            keyword.put(parameter, value);
            return builder();
        }

        /**
         * Set multiple keyword parameters. See {@link #add(String, String)}.
         *
         * @param parameters Map of parameter name to value pairs.
         * @return Builder instance the method was called on.
         */
        public CommandBuilder add(Map<String, String> parameters) {
            Args.notNull(parameters, "parameters");
            for (Map.Entry<String, String> parmPair : parameters.entrySet()) {
                add(parmPair.getKey(), parmPair.getValue());
            }
            return builder();
        }

        /**
         * Get resulting command string.
         *
         * @return command string.
         */
        public String build() {
            StringBuilder builder = new StringBuilder(command.getCommand());
            boolean first = true;

            for (String parameter : positional) {
                if (first) {
                    builder.append(" ");
                    first = false;
                } else {
                    builder.append(",");
                }
                builder.append(parameter);
            }

            for (Map.Entry<String, String> parameterPair : keyword.entrySet()) {
                if (first) {
                    builder.append(" ");
                    first = false;
                } else {
                    builder.append(",");
                }
                builder.append(parameterPair.getKey());
                builder.append("=");
                builder.append(parameterPair.getValue());
            }

            fullCommand = builder.toString();
            return fullCommand;
        }

        @Override
        public String toString() {
            return fullCommand;
        }

        protected CommandBuilder builder() {
            return this;
        }

        /** Quote parameter value where needed */
        private String escapeValue(String value) {
            assert !value.contains("'");
            if (value.contains(",") || value.contains(" ") || !value.toUpperCase().equals(value)) {
                return "'" + value + "'";
            }
            return value;
        }
    }
}
