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

package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.artifact.thirdparty.MQExplorerVersion;
import com.ca.apm.systemtest.fld.flow.MQExplorerFlow;
import com.ca.apm.systemtest.fld.flow.MQExplorerFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author rsssa02
 */
public class IBMMQRole extends AbstractRole {

    private String installPath;
    private MQExplorerFlowContext flowContext;
    private ITasResolver tasResolver;
    private String installerFileName;
    public ArrayList<String> queueManagers;
    public ArrayList<String> queueNames;
    public boolean createQueue;
    private final boolean predeployed;
    public HashMap<String, Integer> portMap;
    private String responseFileName;

    public IBMMQRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.installPath = builder.installPath;
        this.tasResolver = builder.tasResolver;
        this.flowContext = builder.flowContext;
        this.portMap = builder.portMap;
        this.responseFileName = builder.responseFileName;
        predeployed = builder.predeployed;
    }

    public String getResponseFileName() {
        return this.responseFileName;
    }

    public int getMqPort(String key) {
        return this.portMap.get(key);
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            this.runFlow(aaClient, MQExplorerFlow.class, this.flowContext);
        }

    }

    public static class Builder extends BuilderBase<Builder, IBMMQRole> {
        protected static final MQExplorerVersion DEFAULT_MQ_EXPLORER_VERSION;
        protected final String roleId;
        protected MQExplorerFlowContext flowContext;
        protected MQExplorerFlowContext.Builder flowContextBuilder;
        public HashMap<String, String> queueMap;
        public HashMap<String, Integer> portMap;
        public boolean createQueue;
        public String installPath;
        public String responseFileName;
        protected boolean predeployed;
        public final ITasResolver tasResolver;
        protected Artifact mqArtifact;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.flowContextBuilder = new MQExplorerFlowContext.Builder();
            this.version(DEFAULT_MQ_EXPLORER_VERSION);
        }

        static {
            DEFAULT_MQ_EXPLORER_VERSION = MQExplorerVersion.VER_75;
        }


        private void initFlowContext() {
            URL artifactUrl = this.tasResolver.getArtifactUrl(this.mqArtifact);
            this.flowContext = this.flowContextBuilder.installPackageUrl(artifactUrl)
                    .build();
        }

        protected IBMMQRole getInstance() {
            return new IBMMQRole(this);
        }

        protected Builder builder() {
            return this;
        }

        public IBMMQRole build() {
            initFlowContext();
            IBMMQRole ibmmqRole = this.getInstance();
            Args.notNull(ibmmqRole.flowContext, "Deploy flow context cannot be null.");
            //this.mqPort = flowContext.getMqPort();
            return ibmmqRole;
        }

        public Builder version(@NotNull MQExplorerVersion mqVersion) {
            this.mqArtifact = mqVersion.getArtifact();
            this.flowContextBuilder.installerFileName(mqVersion.getInstallerFileName());
            return this.builder();
        }

        public Builder installPath(String installPath) {
            this.installPath = installPath;
            this.flowContextBuilder.installPath(this.installPath);
            return builder();
        }

        public Builder queueMap(HashMap<String, String> queueMap) {
            this.queueMap = queueMap;
            this.flowContextBuilder.queueMap(queueMap);
            return this.builder();
        }

        public Builder portMap(HashMap<String, Integer> portMap) {
            this.portMap = portMap;
            this.flowContextBuilder.portMap(portMap);
            return this.builder();
        }

        public Builder createQueue(boolean createQueue) {
            this.createQueue = createQueue;
            this.flowContextBuilder.createQueue(createQueue);
            return this.builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return this.builder();
        }
    }
}
