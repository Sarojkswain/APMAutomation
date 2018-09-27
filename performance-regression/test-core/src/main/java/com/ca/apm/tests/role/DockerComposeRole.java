package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow2;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.util.*;

/**
 * Created by jirji01 on 11/24/2016.
 */
public class DockerComposeRole extends AbstractRole {

    public enum Template {
        mom, collector, db, wv;

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
    private final FileCreatorFlowContext laxConfigFileFlowContext;
    private final FileCreatorFlowContext propConfigFileFlowContext;

    private final RunCommandFlowContext composeUpCmdFlowContext;
    private final RunCommandFlowContext composePullCmdFlowContext;
    private final RunCommandFlowContext composeRmCmdFlowContext;
    private final RunCommandFlowContext composeStopCmdFlowContext;

    public DockerComposeRole(AbstractBuilder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.installDir = builder.installDir;

        ymlFileCreatorFlowContext = builder.ymlFileCreatorFlowContext;
        laxConfigFileFlowContext = builder.laxConfigFileFlowContext;
        propConfigFileFlowContext = builder.propConfigFileFlowContext;
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
        runFlow(aaClient, FileCreatorFlow2.class, ymlFileCreatorFlowContext);

        if (laxConfigFileFlowContext != null)
            runFlow(aaClient, FileCreatorFlow2.class, laxConfigFileFlowContext);
        if (propConfigFileFlowContext != null)
            runFlow(aaClient, FileCreatorFlow2.class, propConfigFileFlowContext);
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

    public static class DbBuilder extends AbstractBuilder<DbBuilder> {

        public DbBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            ymlTemplate = Template.db;
            textToMatch = "Executing APM DB entrypoint script";
        }

        @Override
        public DbBuilder builder() {
            return this;
        }
    }

    public static class WVBuilder extends AbstractBuilder<WVBuilder> {

        public WVBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            ymlTemplate = Template.wv;
            textToMatch = "Web Application Server started";
        }

        @Override
        public DockerComposeRole build() {

            if (laxNlJavaOptionAdditional != null) {
                laxConfigFileFlowContext = new FileCreatorFlowContext.Builder()
                        .destinationPath(installDir + "/config/Introscope_WebView.lax")
                        .fromData(Collections.singleton(laxNlJavaOptionAdditional))
                        .build();
            } else if (javaHeapSize != null) {
                laxNlJavaOptionAdditional =
                        "lax.nl.java.option.additional=-Xms" + javaHeapSize +"m -Xmx" + javaHeapSize +"m -Djava.awt.headless=true " +
                                "-Dorg.owasp.esapi.resources=./config/esapi -Dsun.java2d.noddraw=true " +
                                "-Dorg.osgi.framework.bootdelegation=org.apache.xpath";
            }

            return super.build();
        }

        @Override
        public WVBuilder builder() {
            return this;
        }
    }

    public static class CollectorBuilder extends AbstractBuilder<CollectorBuilder> {

        public CollectorBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            ymlTemplate = Template.collector;
            textToMatch = "Introscope Enterprise Manager started.";
        }

        @Override
        public DockerComposeRole build() {
            Args.notNull(substitution.containsKey("env.mom.db"), "DB server");

            if (laxNlJavaOptionAdditional != null) {
                laxConfigFileFlowContext = new FileCreatorFlowContext.Builder()
                        .destinationPath(installDir + "/config/Introscope_Enterprise_Manager.lax")
                        .fromData(Collections.singleton(laxNlJavaOptionAdditional))
                        .build();
            } else if (javaHeapSize != null) {
                laxNlJavaOptionAdditional =
                        "lax.nl.java.option.additional=-Xms%s" + javaHeapSize +"m -Xmx" + javaHeapSize +"m -Djava.awt.headless=true " +
                                "-Dmail.mime.charset=UTF-8 -Dorg.owasp.esapi.resources=./config/esapi " +
                                "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -Xss512k";
            }

            return super.build();
        }

        @Override
        protected CollectorBuilder builder() {
            return this;
        }

    }

    public static class MomBuilder extends AbstractBuilder<MomBuilder> {

        public MomBuilder(String roleId, ITasResolver resolver) {
            super(roleId, resolver);
            ymlTemplate = Template.mom;
            textToMatch = "Introscope Enterprise Manager started.";
        }

        @Override
        public DockerComposeRole build() {
            Args.notEmpty(collectors, "collectors");
            Args.check(collectors.size() < 10, "maximum 10 collectors can be connected to MOM");
            Args.notNull(substitution.containsKey("env.mom.db"), "DB server");

            if (laxNlJavaOptionAdditional != null) {
                laxConfigFileFlowContext = new FileCreatorFlowContext.Builder()
                        .destinationPath(installDir + "/config/Introscope_Enterprise_Manager.lax")
                        .fromData(Collections.singleton(laxNlJavaOptionAdditional))
                        .build();
            } else if (javaHeapSize != null) {
                laxNlJavaOptionAdditional =
                        "lax.nl.java.option.additional=-Xms%s" + javaHeapSize +"m -Xmx" + javaHeapSize +"m -Djava.awt.headless=true " +
                                "-Dmail.mime.charset=UTF-8 -Dorg.owasp.esapi.resources=./config/esapi " +
                                "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -Xss512k";
            }

            StringBuilder sb = new StringBuilder();
            int idx = 1;
            for (String collector : collectors) {
                if (idx > 1) {
                    sb.append('\n');
                }
                sb.append("      - EM_COLLECTOR_HOST_");
                sb.append(idx);
                sb.append('=');
                sb.append(collector);
                idx++;
            }
            substitution.put("env.col.hosts", sb.toString());

            return super.build();
        }

        @Override
        public MomBuilder builder() {
            return this;
        }
    }

    public static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends BuilderBase<T, DockerComposeRole> {
        private String roleId;
        private ITasResolver resolver;

        String installDir = TasBuilder.LINUX_SOFTWARE_LOC + "docker-compose";
        Template ymlTemplate;
        String textToMatch = "";

        HashMap<String, String> substitution = new HashMap<>();
        ArrayList<String> collectors = new ArrayList<>();
        String laxNlJavaOptionAdditional = null;
        Integer javaHeapSize = null;

        FileCreatorFlowContext ymlFileCreatorFlowContext;
        FileCreatorFlowContext laxConfigFileFlowContext;
        FileCreatorFlowContext propConfigFileFlowContext;
        RunCommandFlowContext composeUpCmdFlowContext;
        RunCommandFlowContext composePullCmdFlowContext;
        RunCommandFlowContext composeRmCmdFlowContext;
        RunCommandFlowContext composeStopCmdFlowContext;

        private String projectVersion = "99.99.docker-SNAPSHOT";
        private String imageRegistry = "artifactory-emea-cz.ca.com:4443";

        AbstractBuilder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }

        public T javaHeapSize(int mem) {
            javaHeapSize = mem;
            return builder();
        }

        public T javaOptions(Collection<String> options) {
            StringBuilder sb = new StringBuilder();
            sb.append("lax.nl.java.option.additional=");
            for (String option : options) {
                sb.append(option);
                sb.append(' ');
            }
            laxNlJavaOptionAdditional = sb.toString();
            return builder();
        }

        public T addCollector(String host) {
            collectors.add(host);
            return builder();
        }

        public T databaseHost(String hostname) {
            substitution.put("env.mom.db", hostname);
            return builder();
        }

        public T installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public T version(String projectVersion) {
            this.projectVersion = projectVersion;
            return builder();
        }

        public T imageRegistry(String imageRegistry) {
            this.imageRegistry = imageRegistry;
            return builder();
        }

        @Override
        public DockerComposeRole build() {

            FileCreatorFlowContext.Builder ymlBuilder = new FileCreatorFlowContext.Builder();

            ymlBuilder.destinationPath(installDir + "/docker-compose.yml")
                    .fromResource(ymlTemplate.toString());

            for (Map.Entry<String, String> entry : substitution.entrySet()) {
                ymlBuilder.substitution(entry.getKey(), entry.getValue());
            }

            ymlBuilder.substitution("project.version", projectVersion);
            ymlBuilder.substitution("image.registry", imageRegistry);

            ymlFileCreatorFlowContext = ymlBuilder.build();



            composeUpCmdFlowContext = new RunCommandFlowContext.Builder("/usr/local/bin/docker-compose")
                    .args(Collections.singletonList("up"))
                    .workDir(installDir)
                    .dontUseWindowsShell()
                    .doNotPrependWorkingDirectory()
                    .terminateOnMatch(textToMatch)
                    .build();
            getEnvProperties().add(UP, composeUpCmdFlowContext);

            composePullCmdFlowContext = new RunCommandFlowContext.Builder("/usr/local/bin/docker-compose")
                    .args(Collections.singletonList("pull"))
                    .workDir(installDir)
                    .dontUseWindowsShell()
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(PULL, composePullCmdFlowContext);

            composeRmCmdFlowContext = new RunCommandFlowContext.Builder("/usr/local/bin/docker-compose")
                    .args(Arrays.asList("rm", "-f"))
                    .workDir(installDir)
                    .dontUseWindowsShell()
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(RM, composeRmCmdFlowContext);

            composeStopCmdFlowContext = new RunCommandFlowContext.Builder("/usr/local/bin/docker-compose")
                    .args(Collections.singletonList("stop"))
                    .workDir(installDir)
                    .dontUseWindowsShell()
                    .doNotPrependWorkingDirectory()
                    .build();
            getEnvProperties().add(STOP, composeStopCmdFlowContext);

            return getInstance();
        }

        @Override
        protected DockerComposeRole getInstance() {
            return new DockerComposeRole(this);
        }

        abstract protected T builder();
    }

    public String getInstallDir() {
        return installDir;
    }
}
