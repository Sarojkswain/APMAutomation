package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.flow.FileCreatorFlow;
import com.ca.apm.tests.flow.FileCreatorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.util.Arrays;

/**
 * Created by jirji01 on 11/24/2016.
 */
public class DockerComposeRole extends AbstractRole {

    public enum Template {
        collector, db, mom, wv, cluster;

        public String toString() {
            return "/docker-compose/" + this.name() + ".yml";
        }
    }

    public static final String UP = "docker-compose_up";
    public static final String PULL = "docker-compose_pull";
    public static final String RM = "docker-compose_rm";
    public static final String STOP = "docker-compose_stop";

    private final String installDir;

    private final FileCreatorFlowContext ymlFileCreatorFlowContext;

    private final RunCommandFlowContext composeUpCmdFlowContext;
    private final RunCommandFlowContext composePullCmdFlowContext;
    private final RunCommandFlowContext composeRmCmdFlowContext;
    private final RunCommandFlowContext composeStopCmdFlowContext;

    /**
     * @param builder
     */
    public DockerComposeRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.installDir = builder.installDir;

        ymlFileCreatorFlowContext = builder.ymlFileCreatorFlowContext;
        composeUpCmdFlowContext = builder.composeUpCmdFlowContext;
        composePullCmdFlowContext = builder.composePullCmdFlowContext;
        composeRmCmdFlowContext = builder.composeRmCmdFlowContext;
        composeStopCmdFlowContext = builder.composeStopCmdFlowContext;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, FileCreatorFlow.class, ymlFileCreatorFlowContext);
    }

    public RunCommandFlowContext getComposeUpCmdFlowContext() {
        return composeUpCmdFlowContext;
    }

    public RunCommandFlowContext getComposePullCmdFlowContext() {
        return composePullCmdFlowContext;
    }

    public RunCommandFlowContext getComposeRmCmdFlowContext() {
        return composeRmCmdFlowContext;
    }

    public RunCommandFlowContext getComposeStopCmdFlowContext() {
        return composeStopCmdFlowContext;
    }

    public static class Builder extends BuilderBase<Builder, DockerComposeRole> {
        private String roleId;
        private ITasResolver resolver;

        private String installDir = TasBuilder.LINUX_SOFTWARE_LOC + "docker-compose";
        private Template ymlTemplate;
        private String heapMemorySize = "2048m";
        private String projectVersion = "99.99.docker-SNAPSHOT";
        private String imageRegistry = "artifactory-emea-cz.ca.com:4443";

        protected FileCreatorFlowContext ymlFileCreatorFlowContext;
        protected RunCommandFlowContext composeUpCmdFlowContext;
        protected RunCommandFlowContext composePullCmdFlowContext;
        protected RunCommandFlowContext composeRmCmdFlowContext;
        protected RunCommandFlowContext composeStopCmdFlowContext;

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public Builder composeYmlTemplateName(Template template) {
            this.ymlTemplate = template;
            return builder();
        }

        public Builder heapMemorySize(String heapMemorySize) {
            this.heapMemorySize = heapMemorySize;
            return builder();
        }

        public Builder version(String projectVersion) {
            this.projectVersion = projectVersion;
            return builder();
        }

        public void setInstallDir(String installDir) {
            this.installDir = installDir;
        }

        @Override
        public DockerComposeRole build() {
            Args.notNull(ymlTemplate, "YML template name");
            Args.notNull(installDir, "Install dir");
            Args.notBlank(heapMemorySize, "Heap memory size");

            ymlFileCreatorFlowContext = new FileCreatorFlowContext.Builder()
                    .destinationPath(installDir + "/docker-compose.yml")
                    .fromResource(ymlTemplate.toString())
                    .substitution("ENV_MEMORY", heapMemorySize)
                    .substitution("PROJECT_VERSION", projectVersion)
                    .substitution("IMAGE_REGISTRY", imageRegistry)
                    .build();

            composeUpCmdFlowContext = new RunCommandFlowContext.Builder("docker-compose")
                    .args(Arrays.asList("up"))
                    .workDir(installDir)
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(UP, composeUpCmdFlowContext);

            composePullCmdFlowContext = new RunCommandFlowContext.Builder("docker-compose")
                    .args(Arrays.asList("pull"))
                    .workDir(installDir)
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(PULL, composePullCmdFlowContext);

            composeRmCmdFlowContext = new RunCommandFlowContext.Builder("docker-compose")
                    .args(Arrays.asList("rm"))
                    .workDir(installDir)
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(RM, composeRmCmdFlowContext);

            composeStopCmdFlowContext = new RunCommandFlowContext.Builder("docker-compose")
                    .args(Arrays.asList("stop"))
                    .workDir(installDir)
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(STOP, composeStopCmdFlowContext);

            return getInstance();
        }

        @Override
        protected DockerComposeRole getInstance() {
            return new DockerComposeRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public String getInstallDir() {
        return installDir;
    }
}
