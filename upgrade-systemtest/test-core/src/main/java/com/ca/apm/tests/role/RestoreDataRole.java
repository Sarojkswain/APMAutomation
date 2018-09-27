package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.tests.flow.RestoreDataFlow;
import com.ca.apm.tests.flow.RestoreDataFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.URL;
import java.nio.file.Path;

/**
 * Created by jirji01 on 6/27/2017.
 */
public class RestoreDataRole extends AbstractRole {
    private final RestoreDataFlowContext flowContext;
    private final String deployBase;
    private URL databaseUtilityUrl;

    public RestoreDataRole(Builder builder) {
        super(builder.roleId);

        flowContext = builder.flowContext;
        databaseUtilityUrl = builder.databaseUtilityUrl;

        deployBase = flowContext.data.sourceData.substring(0, flowContext.data.sourceData.lastIndexOf('/'));
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        UniversalFlowContext context = new UniversalFlowContext.Builder()
                .artifact(databaseUtilityUrl, deployBase, "data-pump.jar")
                .build();
        runFlow(aaClient, context);

        runFlow(aaClient, RestoreDataFlow.class, this.flowContext);
    }

    public static class Builder extends BuilderBase<Builder, RestoreDataRole> {
        private final String roleId;
        private final URL databaseUtilityUrl;

        private RestoreDataFlowContext flowContext;
        private RestoreDataFlowContext.Builder flowContextBuilder;

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            flowContextBuilder = new RestoreDataFlowContext.Builder();

            databaseUtilityUrl = resolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.tools", "data-pump", "jar", "0.1"));
        }

        @Override
        protected RestoreDataRole getInstance() {
            return new RestoreDataRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public RestoreDataRole build() {
            flowContext = flowContextBuilder.build();
            return new RestoreDataRole(this);
        }

        public Builder em(String dir) {
            flowContextBuilder.em(dir);
            return builder();
        }

        public Builder smartstor(String dir) {
            flowContextBuilder.smartstor(dir);
            return builder();
        }

        public Builder smartstorArchive(String dir) {
            flowContextBuilder.smartstorArchive(dir);
            return builder();
        }

        public Builder smartstorMeta(String dir) {
            flowContextBuilder.smartstorMeta(dir);
            return builder();
        }

        public Builder traces(String dir) {
            flowContextBuilder.traces(dir);
            return builder();
        }

        public Builder baseLine(String path) {
            flowContextBuilder.baseLine(path);
            return builder();
        }

        public Builder hammondData(String dir) {
            flowContextBuilder.hammondData(dir);
            return builder();
        }

        public Builder dbHost(String dbHost) {
            flowContextBuilder.dbHost(dbHost);
            return builder();
        }

        public Builder dbVersion(String version) {
            flowContextBuilder.dbVersion(version);
            return builder();
        }

        public Builder sourceData(String path) {
            flowContextBuilder.sourceData(path);
            return builder();
        }
    }
}
