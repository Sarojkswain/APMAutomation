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

import com.ca.apm.automation.utils.mainframe.MvsTask.Command;
import com.ca.apm.automation.utils.mainframe.MvsTask.State;

import org.apache.commons.lang.Validate;
import org.apache.http.util.Args;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Context for the {@link MvsTask} operations.
 */
public class ControlMvsTaskContext {
    private final String jclMember;
    private final String taskName;
    private final Map<String, String> taskParms;
    private final String taskLpar;
    private final MvsTask.State desiredState;
    private final boolean restartIfRunning;
    private final boolean ignoreCurrentState;
    private final int timeout;
    private final boolean checkOnly;
    private final MvsTask.Command action;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    protected ControlMvsTaskContext(BuilderBase<?, ?> builder) {
        assert !builder.jclMember.trim().isEmpty();
        assert !builder.taskName.trim().isEmpty();
        assert builder.timeout >= 0;

        jclMember = builder.jclMember;
        taskName = builder.taskName;
        taskParms = builder.taskParms;
        taskLpar = builder.taskLpar;
        desiredState = builder.desiredState;
        restartIfRunning = builder.restartIfRunning;
        ignoreCurrentState = builder.ignoreCurrentState;
        timeout = builder.timeout;
        checkOnly = builder.checkOnly;
        action = builder.action;
    }

    public String getJclMember() {
        return jclMember;
    }

    public String getTaskName() {
        return taskName;
    }

    public Map<String, String> getTaskParms() {
        return taskParms;
    }

    public String getTaskLpar() {
        return taskLpar;
    }

    public MvsTask.State getDesiredState() {
        return desiredState;
    }

    public MvsTask.Command getCommand() {
        return action;
    }

    public boolean getRestartIfRunning() {
        return restartIfRunning;
    }

    public boolean getIgnoreCurrentState() {
        return ignoreCurrentState;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isCheckOnly() {
        return checkOnly;
    }

    /**
     * Builder for the {@link ControlMvsTaskContext} flow context.
     *
     * @see BuilderBase
     */
    public static class Builder extends BuilderBase<Builder, ControlMvsTaskContext> {

        public Builder(String jclMember, State desiredState) {
            super(jclMember, desiredState);
        }

        @Override
        protected ControlMvsTaskContext getInstance() {
            return new ControlMvsTaskContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    /**
     * Builder base for {@link ControlMvsTaskContext} that allows builder inheritance.
     *
     * This avoids dependency on TAS in this project and allows override of
     * {@link ControlMvsTaskContext} that implements IFlowContext.
     */
    public abstract static class BuilderBase<B extends BuilderBase<B, C>,
                                             C extends ControlMvsTaskContext> {
        private String jclMember;
        private String taskName;
        private Map<String, String> taskParms = new LinkedHashMap<>();
        private String taskLpar;
        private MvsTask.State desiredState;
        private MvsTask.Command action;
        private boolean restartIfRunning = false;
        private boolean ignoreCurrentState = false;
        private int timeout = 60;
        private boolean checkOnly = false;

        /**
         * Constructor.
         *
         * @param jclMember Name of the JCL member.
         * @param desiredState Desired state of the task.
         */
        public BuilderBase(String jclMember, MvsTask.State desiredState) {
            Args.notBlank(jclMember, "jclMember");

            this.jclMember = jclMember;
            this.desiredState = desiredState;
        }

        /**
         * Explicitly specify which {@link Command} will be used to get to desired state.
         *
         * @param action Action to be used.
         * @return referenced instance.
         */
        public B action(Command action) {
            this.action = action;
            return builder();
        }

        /**
         * Indicates that, if already running, a task should be restarted.
         * Only applies if {@link #desiredState} is set to {@link MvsTask.State#RUNNING}.
         *
         * @return Referenced instance.
         */
        public B restartIfRunning() {
            Validate.isTrue(!ignoreCurrentState,
                "Restart if running is mutually exclusive with Ignore current state.");
            restartIfRunning = true;
            return builder();
        }

        /**
         * Indicates that the task state change action should be performed even if task is already
         * in desired state.
         *
         * @return Referenced instance.
         */
        public B ignoreCurrentState() {
            Validate.isTrue(!restartIfRunning,
                "Ignore current state is mutually exclusive with Restart if running.");
            ignoreCurrentState = true;
            return builder();
        }


        /**
         * Indicates that the task should be only checked for desired state, and fail if it isn't.
         *
         * @return Referenced instance.
         */
        public B checkOnly() {
            checkOnly = true;
            return builder();
        }

        /**
         * Sets the approximate timeout value.
         *
         * @param timeout Timeout value in seconds to wait for change of task state.
         * @return Referenced instance.
         */
        public B timeout(int timeout) {
            Args.positive(timeout, "timeout");

            this.timeout = timeout;
            return builder();
        }

        /**
         * Sets up the map of JCL task parameters. Adds/overwrites existing parameters if called
         * repeatedly.
         *
         * @param taskParms Custom JCL parameters stored as map where key is the parameter name and
         *        value is the parameter value.
         * @return Referenced instance.
         */
        public B taskParms(Map<String, String> taskParms) {
            Validate.notEmpty(taskParms, "JCL parameters map is empty.");
            this.taskParms.putAll(taskParms);
            return builder();
        }

        /**
         * Sets an explicit task name separate from the JCL name.
         * By default this is the value of {@link #jclMember}.
         *
         * @param taskName Name of the task to control.
         * @return Referenced instance.
         */
        public B taskName(String taskName) {
            Args.notBlank(taskName, "taskName");

            this.taskName = taskName;
            return builder();
        }

        /**
         * Sets an explicit task LPAR.
         * By default this is empty and any available LPAR will be used when checking task state.
         *
         * @param taskLpar Task LPAR.
         * @return Referenced instance.
         */
        public B taskLpar(String taskLpar) {
            Args.notBlank(taskLpar, "taskLpar");

            this.taskLpar = taskLpar;
            return builder();
        }

        public C build() {
            if (taskName == null) {
                taskName = jclMember;
            }
            if (action == null) {
                action = desiredState.getDefaultCommand();
            }
            return getInstance();
        }

        protected abstract C getInstance();

        protected abstract B builder();
    }
}
