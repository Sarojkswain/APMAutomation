package com.ca.apm.systemtest.fld.role;


import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * the goal is to conveniently keep the misc scripts in resources folder under test-core project
 * this class is to deploy the resource folder within test-core folder which can have excel template, jmeter scipts
 * and other misc needed files.
 * @Author rsssa02
 */
public class ClientDeployRole extends AbstractRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDeployRole.class);
    private ITasResolver tasResolver;

    public String getClientHome() {
        return clientHome;
    }

    public void setClientHome(String clientHome) {
        this.clientHome = clientHome;
    }

    public String clientHome;

    public ClientDeployRole(Builder builder) {
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.clientHome = builder.clientHome;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployResourcesDist(aaClient, "com.ca.apm.tests", "systemtest-powerpacks-core", "dist");
        deployJarResource(aaClient, "com.ca.apm.em", "com.wily.introscope.clw.feature", "CLWorkstation.jar");
    }

    private void deployJarResource(IAutomationAgentClient aaClient, String groupId, String artifactId, String dest) {
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId,
                "", "jar", tasResolver.getDefaultVersion()));
        LOGGER.info("Downloading jar artifact " + url.toString());

        GenericFlowContext context = new GenericFlowContext.Builder()
                .notArchive()
                .artifactUrl(url)
                .destination(this.clientHome + "/lib/em/" + dest)
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void deployResourcesDist(IAutomationAgentClient aaClient, String groupId, String artifactId, String classifier) {

        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId,
                classifier, "zip", tasResolver.getDefaultVersion()));

        GenericFlowContext context = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(this.clientHome)
                .build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    public static class Builder extends BuilderBase<Builder, ClientDeployRole>{
        private final ITasResolver tasResolver;
        private final String roleId;
        public String clientHome;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected ClientDeployRole getInstance() {
            return new ClientDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ClientDeployRole build() {
            initClientLoc();
            return getInstance();
        }

        private void initClientLoc() {
            this.clientHome(TasBuilder.WIN_SOFTWARE_LOC + "/client/");
        }

        public ClientDeployRole.Builder clientHome(String dir){
            this.clientHome = dir;
            return builder();
        }
    }
}
