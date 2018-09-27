package com.ca.apm.systemtest.sizingguidetest.role;

import java.util.Arrays;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class PrepareConfigimportRole extends AbstractRole {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareConfigimportRole.class);

    private String scriptFile;

    protected PrepareConfigimportRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.scriptFile = builder.scriptFile;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        RunCommandFlowContext mvRunCommandFlowContext =
            (new RunCommandFlowContext.Builder("mv"))
                .args(
                    Arrays.asList(new String[] {
                            "/opt/automation/deployed/em/install/database-scripts/",
                            "/opt/automation/deployed/em/install/database-scripts_9.7/"}))
                .doNotPrependWorkingDirectory().build();
        runCommandFlow(aaClient, mvRunCommandFlowContext);

        RunCommandFlowContext mkdirRunCommandFlowContext =
            (new RunCommandFlowContext.Builder("mkdir"))
                .args(
                    Arrays
                        .asList(new String[] {"-p", "/opt/automation/tmp/database-scripts_10.5/"}))
                .doNotPrependWorkingDirectory().build();
        runCommandFlow(aaClient, mkdirRunCommandFlowContext);

        DeployFileFlowContext deployFileFlowContext1 =
            (new DeployFileFlowContext.Builder())
                .srcFile("/database-scripts/database-scripts_10.5/database-scripts-1.zip")
                .dstFilePath(
                    new String[] {"/opt/automation/tmp/database-scripts_10.5/",
                            "database-scripts-1.zip"}).build();
        runFlow(aaClient, DeployFileFlow.class, deployFileFlowContext1);

        DeployFileFlowContext deployFileFlowContext2 =
            (new DeployFileFlowContext.Builder())
                .srcFile("/database-scripts/database-scripts_10.5/database-scripts-2.zip")
                .dstFilePath(
                    new String[] {"/opt/automation/tmp/database-scripts_10.5/",
                            "database-scripts-2.zip"}).build();
        runFlow(aaClient, DeployFileFlow.class, deployFileFlowContext2);

        RunCommandFlowContext unzipRunCommandFlowContext1 =
            new RunCommandFlowContext.Builder("unzip")
                .args(
                    Arrays.asList("database-scripts-1.zip", "-d",
                        "/opt/automation/deployed/em/install/")).doNotPrependWorkingDirectory()
                .workDir("/opt/automation/tmp/database-scripts_10.5/").build();
        runCommandFlow(aaClient, unzipRunCommandFlowContext1);

        RunCommandFlowContext unzipRunCommandFlowContext2 =
            new RunCommandFlowContext.Builder("unzip")
                .args(
                    Arrays.asList("database-scripts-2.zip", "-d",
                        "/opt/automation/deployed/em/install/")).doNotPrependWorkingDirectory()
                .workDir("/opt/automation/tmp/database-scripts_10.5/").build();
        runCommandFlow(aaClient, unzipRunCommandFlowContext2);

        RunCommandFlowContext chmodRunCommandFlowContext =
            new RunCommandFlowContext.Builder("chmod")
                .args(
                    Arrays.asList("-R", "775",
                        "/opt/automation/deployed/em/install/database-scripts/"))
                .doNotPrependWorkingDirectory()
                // .workDir("/opt/automation/deployed/em/install/database-scripts/unix")
                .build();
        runCommandFlow(aaClient, chmodRunCommandFlowContext);



        // RunCommandFlowContext runCommandFlowContext =
        // (new RunCommandFlowContext.Builder("mv"))
        // .args(Arrays.asList(new String[] {scriptFile, scriptFile + ".orig"}))
        // .doNotPrependWorkingDirectory().build();
        // runCommandFlow(aaClient, runCommandFlowContext);
        //
        // DeployFileFlowContext deployFileFlowContext =
        // (new DeployFileFlowContext.Builder()).srcFile("/configimport/configimport.sh")
        // .dstFilePath(new String[] {scriptFile}).build();
        // runFlow(aaClient, DeployFileFlow.class, deployFileFlowContext);
        //
        // RunCommandFlowContext chmodRunCommandFlowContext =
        // new RunCommandFlowContext.Builder("chmod").args(Arrays.asList("u+x", scriptFile))
        // .doNotPrependWorkingDirectory().build();
        // runCommandFlow(aaClient, chmodRunCommandFlowContext);

        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/createtables-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/createtables-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/defaults-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/defaults-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/addindexes-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/addindexes-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/create-apm-tables-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/create-apm-tables-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/create-apm-sequences-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/create-apm-sequences-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/add-apm-indexes-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/add-apm-indexes-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/apm-procedures-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/apm-procedures-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/apm-defaults-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/apm-defaults-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/addconstraints-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/addconstraints-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/add-apm-constraints-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/add-apm-constraints-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/addviews-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/addviews-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/procedures-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/procedures-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/dbupdate-sequences-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/dbupdate-sequences-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/createsequences-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/createsequences-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/dbupdate-apm-sequences-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/dbupdate-apm-sequences-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/drop-apm-tables-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/drop-apm-tables-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/dropconstraints-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/dropconstraints-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/dropindexes-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/dropindexes-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/dropviews-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/dropviews-postgres-10.3.0.0.sql");
        // createLink(aaClient,
        // "/opt/automation/deployed/em/install/database-scripts/initdb-postgres-9.7.0.0.sql",
        // "/opt/automation/deployed/em/install/database-scripts/initdb-postgres-10.3.0.0.sql");
    }

    // private void createLink(IAutomationAgentClient aaClient, String fileName, String linkName) {
    // RunCommandFlowContext chmodRunCommandFlowContext =
    // new RunCommandFlowContext.Builder("ln").args(Arrays.asList("-s", fileName, linkName))
    // .doNotPrependWorkingDirectory().build();
    // runCommandFlow(aaClient, chmodRunCommandFlowContext);
    // LOGGER.info("PrepareConfigimportRole.createLink():: created link {} ==> {}", linkName,
    // fileName);
    // }

    public String getScriptFile() {
        return scriptFile;
    }

    public static class Builder extends BuilderBase<Builder, PrepareConfigimportRole> {
        public static final String DEFAULT_SCRIPT_FILE =
            "/opt/automation/deployed/em/install/database-scripts/unix/configimport.sh";

        private String roleId;
        private String scriptFile = DEFAULT_SCRIPT_FILE;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        public PrepareConfigimportRole build() {
            Args.notNull(scriptFile, "scriptFile");
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected PrepareConfigimportRole getInstance() {
            PrepareConfigimportRole role = new PrepareConfigimportRole(this);
            return role;
        }

        public Builder scriptFile(String scriptFile) {
            Args.notNull(scriptFile, "scriptFile");
            this.scriptFile = scriptFile;
            return builder();
        }
    }

}
