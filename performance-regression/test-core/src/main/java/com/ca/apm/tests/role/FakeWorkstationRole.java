/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.FakeWorkstationDistribution;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class FakeWorkstationRole extends AbstractRole implements ExecCmdRole {
    public static final String ENV_FAKEWORKSTATION_HOME = "home";

    private final GenericFlowContext fakeWorkstationContext;
    private int runDuration;
    private String workDirectory;
    private List<ExecContext> execContext;

    protected FakeWorkstationRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        fakeWorkstationContext = builder.fakeWorkstationContext;
        runDuration = builder.runDuration;
        execContext = builder.execContext;
        workDirectory = builder.installPath;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {

        runFlow(client, GenericFlow.class, fakeWorkstationContext);
    }

    @Override
    public List<ExecContext> getExecContext() {

        return execContext;
    }

    @Override
    public int getRunDuration() {
        return runDuration;
    }

    @Override
    public String getOkStatus() {
        return "Connected...";
    }

    @Override
    public String getWorkDirecotry() {
        return workDirectory;
    }

    public static class Builder extends BuilderBase<Builder, FakeWorkstationRole> {
        protected String roleId;
        @Nullable
        protected GenericFlowContext fakeWorkstationContext;
        protected ITasResolver tasResolver;
        protected List<ExecContext> execContext;
        protected int runDuration;
        protected String installPath;
        protected int instances = 5;
        protected int resolution = 15;
        protected int interval = 5000;
        protected String agentPattern;
        protected String metricPattern;
        protected String hostMachine;
        protected int hostPort = 5001;
        protected String user = "Admin";
        protected boolean historicalQuery = false;
        protected boolean liveQuery = false;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public FakeWorkstationRole build() {
            if (installPath == null) {
                installPath = concatPaths(getWinDeployBase(), "fakeworkstation");
            }
            getEnvProperties().add(ENV_FAKEWORKSTATION_HOME, installPath.toString());

            FakeWorkstationDistribution artifact = new FakeWorkstationDistribution(tasResolver);
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());

            fakeWorkstationContext =
                new GenericFlowContext.Builder().artifactUrl(artifactUrl).destination(installPath)
                    .build();

            execContext = new ArrayList<>();
            for (int i = 1; i <= instances; i++) {
                if (historicalQuery) {
                    List<String> params =
                        Arrays.asList(getJavaCmd(), "-Xmx512m", "-XX:+HeapDumpOnOutOfMemoryError",
                            "-jar", "fakeworkstation.jar", "-host", hostMachine, "-port",
                            Integer.toString(hostPort), "-user", user, "-historical",
                            "-resolution", Integer.toString(resolution), "-sleepBetween",
                            Integer.toString(interval), "-agent", agentPattern + i, "-metric",
                            metricPattern, "-threads", "1");
                    execContext
                        .add(new ExecContext(params, Collections.<String, String>emptyMap()));
                }
                if (liveQuery) {
                    List<String> params =
                        Arrays.asList(getJavaCmd(), "-Xmx512m", "-XX:+HeapDumpOnOutOfMemoryError",
                            "-jar", "fakeworkstation.jar", "-host", hostMachine, "-port",
                            Integer.toString(hostPort), "-user", user, "-resolution",
                            Integer.toString(resolution), "-sleepBetween",
                            Integer.toString(interval), "-agent", agentPattern + i, "-metric",
                            metricPattern, "-threads", "1");

                    execContext
                        .add(new ExecContext(params, Collections.<String, String>emptyMap()));
                }
            }

            return getInstance();
        }

        protected String getJavaCmd() {
            return concatPaths(getWinJavaBase(), "jre7", "bin", "java.exe");
        }

        @Override
        protected FakeWorkstationRole getInstance() {
            return new FakeWorkstationRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installPath(@NotNull String path) {
            this.installPath = path;
            return this;
        }

        public Builder runDuration(int seconds) {
            runDuration = seconds;

            return builder();
        }

        public Builder instances(int instances) {
            this.instances = instances;
            return this;
        }

        public Builder resolution(int resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder interval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder agent(String agentPattern) {
            this.agentPattern = agentPattern;
            return this;
        }

        public Builder metric(String metricPattern) {
            this.metricPattern = metricPattern;
            return this;
        }

        public Builder host(String machine) {
            this.hostMachine = machine;
            return this;
        }

        public Builder port(int port) {
            this.hostPort = port;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder historicalQuery() {
            historicalQuery = true;
            return this;
        }

        public Builder liveQuery() {
            liveQuery = true;
            return this;
        }
    }
}
