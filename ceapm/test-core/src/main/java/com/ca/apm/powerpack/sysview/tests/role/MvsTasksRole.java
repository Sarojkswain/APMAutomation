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

package com.ca.apm.powerpack.sysview.tests.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlow;
import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlowContext;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * This role brings up a set of MVS tasks in order one after the other.
 * If a task is already running it is kept untouched.
 */
public class MvsTasksRole extends AbstractRole {
    /**
     * List of contexts for MvsTask flows that are to be executed.
     */
    private List<ControlMvsTaskFlowContext> taskContexts = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param builder Builder instance containing all the necessary data.
     */
    MvsTasksRole(Builder builder) {
        super(builder.roleId);

        assert builder.tasks != null && !builder.tasks.isEmpty();

        for (Entry<String, String> task : builder.tasks.entrySet()) {
            ControlMvsTaskFlowContext.Builder contextBuilder =
                new ControlMvsTaskFlowContext.Builder(task.getKey(), MvsTask.State.RUNNING)
                    .taskName(task.getValue());
            if (builder.onlyVerify) {
                contextBuilder.checkOnly();
            }

            taskContexts.add(contextBuilder.build());
        }
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        for (ControlMvsTaskFlowContext context : taskContexts) {
            runFlow(aaClient, ControlMvsTaskFlow.class, context);
        }
    }

    /**
     * Builder for the {@link MvsTasksRole} role.
     */
    public static class Builder extends BuilderBase<Builder, MvsTasksRole> {
        private final String roleId;
        private Map<String, String> tasks;
        protected boolean onlyVerify = false;

        /**
         * Constructor using tasks with explicit task names.
         *
         * @param roleId Id of the role.
         * @param tasks Set of JCL member and task name pairs used by the role. The keys are the JCL
         *        member, the values are task names.
         */
        public Builder(String roleId, Map<String, String> tasks) {
            Args.check(!tasks.isEmpty(), "tasks parameter can not be empty");

            this.roleId = roleId;
            this.tasks = tasks;
        }

        /**
         * Constructor using tasks with implicit task names.
         *
         * @param roleId Id of the role.
         * @param jcls Set of JCL members to be used by the role.
         */
        public Builder(String roleId, String... jcls) {
            Args.check(jcls.length > 0, "jcls parameter can not be empty");

            this.roleId = roleId;

            tasks = new HashMap<>();
            for (String jcl : jcls) {
                tasks.put(jcl, jcl);
            }
        }

        /**
         * Only verify whether the role is correctly deployed.
         *
         * @return Builder instance the method was called on.
         */
        public Builder onlyVerify() {
            onlyVerify = true;
            return builder();
        }

        /**
         * Builds an instance of the role based on the provided parameters.
         *
         * @return Role instance.
         */
        @Override
        public synchronized MvsTasksRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MvsTasksRole getInstance() {
            return new MvsTasksRole(this);
        }
    }
}
