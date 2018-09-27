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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlow;
import com.ca.apm.automation.action.flow.mainframe.ControlMvsTaskFlowContext;
import com.ca.apm.automation.action.flow.mainframe.sysview.SysviewPortlistFlow;
import com.ca.apm.automation.action.flow.mainframe.sysview.SysviewPortlistFlow.Operation;
import com.ca.apm.automation.action.flow.mainframe.sysview.SysviewPortlistFlowContext;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * This role starts Sysview instance and configures it for CEAPM connection.
 */
public class SysviewRole extends AbstractRole {
    /**
     * List of contexts for MvsTask flows that are to be executed.
     */
    private List<ControlMvsTaskFlowContext> taskContexts = new ArrayList<>();
    private SysviewPortlistFlowContext smfPortsContext;

    /**
     * Constructor.
     *
     * @param builder Builder instance containing all the necessary data.
     */
    protected SysviewRole(Builder builder) {
        super(builder.roleId);

        assert builder.tasks != null && !builder.tasks.isEmpty();
        assert builder.loadlib != null && !builder.loadlib.isEmpty();

        for (Entry<String, String> task : builder.tasks.entrySet()) {
            ControlMvsTaskFlowContext.Builder contextBuilder =
                new ControlMvsTaskFlowContext.Builder(task.getKey(), MvsTask.State.RUNNING)
                    .taskName(task.getValue()).taskLpar(builder.lpar);
            if (builder.onlyVerify) {
                contextBuilder.checkOnly();
            }
            taskContexts.add(contextBuilder.build());
        }

        if (!builder.smfPorts.isEmpty()) {
            smfPortsContext = new SysviewPortlistFlowContext(builder.loadlib,
                builder.onlyVerify ? Operation.TEST : Operation.ADD, builder.smfPorts);
        }
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        for (ControlMvsTaskFlowContext context : taskContexts) {
            runFlow(aaClient, ControlMvsTaskFlow.class, context);
        }

        if (smfPortsContext != null) {
            runFlow(aaClient, SysviewPortlistFlow.class, smfPortsContext);
        }
    }

    /**
     * Builder for the {@link SysviewRole} role.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends BuilderBase<Builder, SysviewRole> {
        private final String roleId;
        private Map<String, String> tasks;
        private String lpar;
        private final List<Integer> smfPorts = new ArrayList<>();
        private final String loadlib;
        private boolean onlyVerify = true;

        /**
         * Constructor using known Sysview configuration.
         *
         * @param config Known Sysview configuration.
         */
        public Builder(SysviewConfig config) {
            Args.notNull(config, "config");

            roleId = config.getRole();
            loadlib = config.getLoadlib();
            assert loadlib != null;
            Map<String, String> taskMap = new HashMap<>();
            for (String task : new String[] {config.getMainTask(), config.getUserTask()}) {
                if (task != null) {
                    taskMap.put(task, task);
                }
            }
            tasks = Collections.unmodifiableMap(taskMap);
            assert !tasks.isEmpty();
            lpar = config.getLpar();
            onlyVerify = !config.isAutoStartable();
        }

        /**
         * Constructor using tasks with implicit task names.
         *
         * @param roleId Id of the role.
         * @param jcls Set of JCL members to be used by the role.
         */
        public Builder(String roleId, String loadlib, String... jcls) {
            Args.notBlank(loadlib, "loadlib");
            Args.check(jcls.length > 0, "jcls parameter can not be empty");

            this.roleId = roleId;
            this.loadlib = loadlib;
            tasks = new HashMap<>();
            for (String jcl : jcls) {
                tasks.put(jcl, jcl);
            }
        }

        /**
         * Adds an additional SMF port to be configured.
         *
         * @param smfPort Port number to use for delivery of CICS and IMS SMF records.
         * @return Builder instance the method was called on.
         */
        public Builder addSmfPort(int smfPort) {
            Args.check(smfPort >= 1 && smfPort <= 65535,
                "smfPort parameter must be a valid port number");
            smfPorts.add(smfPort);
            return builder();
        }

        /**
         * Do not just verify the role, deploy it.
         *
         * @return Builder instance the method was called on.
         */
        public Builder deployRole() {
            onlyVerify = false;
            return builder();
        }

        /**
         * Builds an instance of the role based on the provided parameters.
         *
         * @return Role instance.
         */
        @Override
        public synchronized SysviewRole build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected SysviewRole getInstance() {
            return new SysviewRole(this);
        }
    }

    /**
     * This class holds default Sysview configurations.
     */
    public enum SysviewConfig {
        WILY_14_0("SYSV14WL", "SYSV14WU", "CA31", "WILY.SYSV.CNM4BLOD", "GSVW"),
        WILY_14_1("WILS141L", "WILS141U", "CA31", "WILY.SYSV.SYSV141.CNM4BLOD.CA31", "GSVW"),
        WILY_14_1_11("WILS141L", "WILS141U", "CA11", "WILY.SYSV.SYSV141.CNM4BLOD.CA11", "GSVW"),
        // NOTE: Do not use instances below unless you receive explicit approval from the owner
        SYSVIEW_QA("SYSVQAA", "CA31", "QASYSM.SYSVIEW.BASE.CNM4BLOD.CA31", "GSVQ"),
        SYSVIEW_DEVELOPMENT("SYSVDEVU", "CA31", "SYSVIEW.DEV.BASE.LOADLIB", "SYSV"),
        ;

        /** Sysview load library */
        private final String loadlib;
        /** Main service address space */
        private final String mainTask;
        /** User interface address space */
        private final String userTask;
        /** LPAR the instance runs on */
        private final String lpar;
        /** Subsystem Id of the instance */
        private final String subsystemId;
        /** Can we start this instance from automation if it's not running? */
        private final boolean autoStartable;

        /**
         * Configuration for Sysview started by starting tasks separately.
         *
         * @param mainTask Main service address space task.
         * @param userTask User interface address space task.
         * @param lpar LPAR where the instance tasks run.
         * @param loadlib Load library.
         * @param subsystemId Subsystem ID of the Sysview instance.
         */
        SysviewConfig(String mainTask, String userTask, String lpar, String loadlib,
                      String subsystemId) {
            this.mainTask = mainTask;
            this.userTask = userTask;
            this.lpar = lpar;
            this.loadlib = loadlib;
            this.subsystemId = subsystemId;
            autoStartable = true;
        }

        /**
         * Configuration for Sysview started externally. Doesn't start the Sysview instance if not
         * already running.
         *
         * @param userTask Task to be verified that it's running.
         * @param lpar LPAR where the instance tasks run.
         * @param loadlib Load library.
         * @param subsystemId Subsystem ID of the Sysview instance.
         */
        SysviewConfig(String userTask, String lpar, String loadlib, String subsystemId) {
            mainTask = null;
            this.userTask = userTask;
            this.lpar = lpar;
            this.loadlib = loadlib;
            this.subsystemId = subsystemId;
            autoStartable = false;
        }

        public String getLoadlib() {
            return loadlib;
        }

        public String getRole() {
            return "sysview" + CommonUtils.constantToCamelCase(name()) + "Role";
        }

        public String getMainTask() {
            return mainTask;
        }

        public String getUserTask() {
            return userTask;
        }

        public String getLpar() {
            return lpar;
        }

        public String getSubsystemId() {
            return subsystemId;
        }

        public boolean isAutoStartable() {
            return autoStartable;
        }

        @Override
        public String toString() {
            return "Sysview " + CommonUtils.constantToCamelCase(name());
        }
    }
}
