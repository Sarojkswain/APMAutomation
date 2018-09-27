package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Arrays;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Install MySql on Windows & populate db via provided sql script
 * 
 * @author kurma05
 */
public class MySqlWinRole extends AbstractRole {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlWinRole.class);
    private static final String ARTIFACT_NAME = "mysql-5.7.20-winx64"; 
    private ITasResolver tasResolver;
    private String installDir;
    private String userName;
    private String userPassword;
    private String dbName;
    private String sqlScript;
    private String binDir;
 
    protected MySqlWinRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.dbName = builder.dbName;
        this.userName = builder.userName;
        this.userPassword = builder.userPassword;
        this.sqlScript = builder.sqlScript;
        this.binDir = installDir + ARTIFACT_NAME + "\\bin";        
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        getArtifact(aaClient);
        startMySql(aaClient);
        createDatabase(aaClient);
        runSqlScript(aaClient);
    }
    
    private void getArtifact(IAutomationAgentClient aaClient) {
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", 
            "mysql", "winx64", "zip", "5.7.20"));            
        LOGGER.info("Downloading artifact " + url.toString());
      
        GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(installDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, getAgentContext);  
    }  
    
    private void startMySql(IAutomationAgentClient aaClient) {
        
        //create data dir
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("mysqld")
            .workDir(binDir)
            .args(Arrays.asList("--initialize-insecure"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install windows service
        command = new RunCommandFlowContext.Builder("mysqld")
            .workDir(binDir)
            .args(Arrays.asList("--install"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    
        //start windows service
        command = new RunCommandFlowContext.Builder("net")
            .args(Arrays.asList("start", "mysql"))  
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    private void createDatabase(IAutomationAgentClient aaClient) {
        
        //create db
        RunCommandFlowContext command = new RunCommandFlowContext.Builder(binDir + "\\mysql")
            .args(Arrays.asList("-u", "root", "-e", "CREATE DATABASE " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create user
        command = new RunCommandFlowContext.Builder(binDir + "\\mysql")
            .args(Arrays.asList("-u", "root", "-e", "CREATE USER " + userName + "@'%' IDENTIFIED BY '" + userPassword + "'"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //set user permissions
        command = new RunCommandFlowContext.Builder(binDir + "\\mysql")
            .args(Arrays.asList("-u", "root", "-e", "GRANT ALL PRIVILEGES ON *.* TO " + userName + "@'%';FLUSH PRIVILEGES"))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);        
    }    
    
    private void runSqlScript(IAutomationAgentClient aaClient) {
        
        RunCommandFlowContext command = new RunCommandFlowContext.Builder(binDir + "\\mysql")
            .args(Arrays.asList("-u", "root", dbName, "<", sqlScript))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
 
    public static class Builder extends BuilderBase<Builder, MySqlWinRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String installDir;     
        protected String userName;
        protected String dbName;
        protected String sqlScript;
        protected String userPassword;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public MySqlWinRole build() {
            return getInstance();
        }

        @Override
        protected MySqlWinRole getInstance() {
            return new MySqlWinRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }   
        
        public Builder userName(String userName) {
            this.userName = userName;
            return builder();
        }
        
        public Builder userPassword(String userPassword) {
            this.userPassword = userPassword;
            return builder();
        }
        
        public Builder dbName(String dbName) {
            this.dbName = dbName;
            return builder();
        }
        
        public Builder sqlScript(String sqlScript) {
            this.sqlScript = sqlScript;
            return builder();
        }
    }
}