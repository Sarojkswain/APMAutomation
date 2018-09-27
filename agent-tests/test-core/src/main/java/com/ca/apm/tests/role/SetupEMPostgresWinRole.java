package com.ca.apm.tests.role;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 */
public class SetupEMPostgresWinRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupEMPostgresWinRole.class);
    private ITasResolver tasResolver;
    private String emInstallDir;
    private String emInstallVersion;
    
    protected SetupEMPostgresWinRole(Builder builder) {

        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.emInstallDir = builder.emInstallDir;
        this.emInstallVersion = builder.emInstallVersion;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        installPostgres(aaClient);
        createDb(aaClient);
        createDbSchema(aaClient);
        updateDBconfig(aaClient);
        restartDB(aaClient);
    }

    private void installPostgres(IAutomationAgentClient aaClient) {

        //install postgres via *.exe installer  
        
        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("postgresql-9.6.2-3-windows-x64.exe")
                .workDir(emInstallDir + "/install/database-install/windows")
                .args(
                    Arrays.asList( "--mode","unattended","--unattendedmodeui","none","--servicename","pgsql-9.6",
                        "--serviceaccount","postgres","--servicepassword","Lister@123","--superaccount","postgres",
                        "--superpassword","Lister@123","--serverport","5432","--prefix",
                        TasBuilder.WIN_SOFTWARE_LOC + "database","--datadir",TasBuilder.WIN_SOFTWARE_LOC + "database\\data")).build();
        
        //flow delay is 2 min (increase if it's not enough)
        runCommandFlowAsync(aaClient, command, 120);
    }
    
    private void createDb(IAutomationAgentClient aaClient) {

        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("createdb-postgres.bat")
                .workDir(emInstallDir + "/install/database-scripts/windows")
                .args(
                    Arrays.asList("localhost", TasBuilder.WIN_SOFTWARE_LOC + "database",
                        "postgres", "Lister@123", "cemdb", "postgres", "Lister@123")).build();
        
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 360000) {    
            try {
                runFlow(aaClient, RunCommandFlow.class, command);
                return;
            }
            catch (Exception e) {
                LOGGER.error("Error occured trying to create db, will try again...max 6 min");
                e.printStackTrace();
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }
    
    private void createDbSchema(IAutomationAgentClient aaClient) {

        //update release version if needed
        //"10.5.2.0"        
        String version = "99.99.0.0";
        
        if(!emInstallVersion.contains("99.99")) {
            version = emInstallVersion.replace("-SNAPSHOT", ".0");
        }
        
        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("createschema.bat")
                .workDir(emInstallDir + "/install/database-scripts/windows")
                .args(
                    Arrays.asList("-databaseName", "cemdb", "-databaseType", "postgres", "-host",
                        "localhost", "-password", "Lister@123", "-port", "5432", "-releaseVersion",
                        version, "-scriptsDir", emInstallDir + "/install/database-scripts",
                        "-user", "postgres")).build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    private void updateDBconfig(IAutomationAgentClient aaClient) {
        
        String file = TasBuilder.WIN_SOFTWARE_LOC + "database\\data\\pg_hba.conf";
        
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .append(file, 
                Arrays.asList("host all all 0.0.0.0/0 password ", "host all all ::/0 password"))
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }
    
    private void restartDB(IAutomationAgentClient aaClient) {
       
        RunCommandFlowContext context =
            new RunCommandFlowContext.Builder("net")
                .args(Arrays.asList("stop", "pgsql-9.6")).build();
        runFlow(aaClient, RunCommandFlow.class, context);
        
        context =
            new RunCommandFlowContext.Builder("net")
                .args(Arrays.asList("start", "pgsql-9.6")).build();
        runFlow(aaClient, RunCommandFlow.class, context);
    }
    
    public static class Builder extends BuilderBase<Builder, SetupEMPostgresWinRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String emInstallDir;
        protected String emInstallVersion;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public SetupEMPostgresWinRole build() {
            return getInstance();
        }

        @Override
        protected SetupEMPostgresWinRole getInstance() {
            return new SetupEMPostgresWinRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder emInstallDir(String emInstallDir) {
            this.emInstallDir = emInstallDir;
            return builder();
        }
        
        public Builder emInstallVersion(String emInstallVersion) {
            this.emInstallVersion = emInstallVersion;
            return builder();
        }
    }
}
