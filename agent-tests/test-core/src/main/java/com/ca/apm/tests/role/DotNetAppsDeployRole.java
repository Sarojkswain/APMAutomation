/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
 
package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author kurma05
 * Nerd Dinner additions by warma11
 */
public class DotNetAppsDeployRole extends AbstractRole {
    
    private ITasResolver tasResolver;
    private String installDir;
    private String dotNetTestAppDir;
    private String calcWCFappDir;
    private String stocktraderDir;
    private String stocktraderDBDataDir;
    private String nerdDinnerMVC4DataDir;
    private String nerdDinnerMVC5DataDir;
    private boolean shouldDeploySystemApps;
    private boolean shouldDisableHttpLogging;
    private boolean shouldInstallSQLServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetAppsDeployRole.class);

    protected DotNetAppsDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.shouldDeploySystemApps = builder.shouldDeploySystemApps;
        this.shouldDisableHttpLogging = builder.shouldDisableHttpLogging;
        this.shouldInstallSQLServer = builder.shouldInstallSQLServer;
        dotNetTestAppDir = installDir + "\\DotNetTestApp";
        calcWCFappDir = installDir + "\\CalcWCFapp";
        stocktraderDir = installDir + "\\netstocktrader55";
        stocktraderDBDataDir = installDir + "\\netstocktrader55_dbdata";
        nerdDinnerMVC4DataDir = installDir + "\\NerdDinnerMVC4";
        nerdDinnerMVC5DataDir = installDir + "\\NerdDinnerMVC5";
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        deployArtifacts(aaClient);
        installDotNetTestApp(aaClient);
        createIISPool(aaClient);
        createIISSite(aaClient);
        createIISApps(aaClient);
        createNerdDinnerApps(aaClient, "NerdDinnerMVC4", 9090);
        createNerdDinnerApps(aaClient, "NerdDinnerMVC5", 9091);
        if (shouldDeploySystemApps) {
            deployStockTrader(aaClient);
        }
        if (shouldInstallSQLServer) {
            installSqlServer(aaClient);
        }
    }

    private void deployStockTrader(IAutomationAgentClient aaClient) {
        
        //get stocktrader
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps", 
            "netstocktrader", "", "zip", "5.5"));
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(installDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //configure db connection   
        Map<String,String> replacePairs = new HashMap<String,String>();       
        replacePairs.put("\\[\\[SQL_DB_HOST\\]\\]", ".\\\\sqlexpress");
      
        FileModifierFlowContext configUpdate = new FileModifierFlowContext.Builder()
             .replace(stocktraderDir + "/StockTrader/StockTraderBusinessService/BusinessServiceConsole/App.config", replacePairs)
             .replace(stocktraderDir + "/StockTrader/StockTraderBusinessService/BusinessServiceHost/App.config", replacePairs)
             .replace(stocktraderDir + "/StockTrader/StockTraderBusinessService/BusinessServiceNTServiceHost/app.config", replacePairs)             
             .replace(stocktraderDir + "/StockTrader/StockTraderBusinessService/TradeWebBSL/web.config", replacePairs)             
             .replace(stocktraderDir + "/StockTrader/StockTraderOrderProcessorService/OrderProcessorConsoleServiceHost/App.config", replacePairs)             
             .replace(stocktraderDir + "/StockTrader/StockTraderOrderProcessorService/OrderProcessorNTServiceHost/app.config", replacePairs)             
             .replace(stocktraderDir + "/StockTrader/StockTraderOrderProcessorService/OrderProcessorServiceHost/App.config", replacePairs)             
             .replace(stocktraderDir + "/StockTrader/StockTraderWebApplication/Trade/Web.config", replacePairs)
             .build();
        runFlow(aaClient, FileModifierFlow.class, configUpdate);
         
        //create TradeWebBSL app
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
             .workDir("C:\\Windows\\system32\\inetsrv")
             .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                 "/path:/TradeWebBSL", "/physicalPath:\"" + stocktraderDir + "\\StockTrader\\StockTraderBusinessService\\TradeWebBSL\""))
             .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
         
        //assign TradeWebBSL to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
             .workDir("C:\\Windows\\system32\\inetsrv")
             .args(Arrays.asList("set", "app", "\"QASite/TradeWebBSL\"", 
                 "/applicationPool:\"CustomAppPool4.0\""))
             .build();        
        runFlow(aaClient, RunCommandFlow.class, command);

        //create Trade app
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                 "/path:/Trade", "/physicalPath:\"" + stocktraderDir + "\\StockTrader\\StockTraderWebApplication\\Trade\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
         
        //assign Trade to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"QASite/Trade\"", 
                 "/applicationPool:\"CustomAppPool4.0\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    private void installSqlServer(IAutomationAgentClient aaClient) {
        
        //get artifact
        String sqlserverInstallDir = installDir + "/sqlserver_install";
        
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.microsoft", 
            "sqlserver2014express", "", "zip", "12.0.2000.8")); 
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(sqlserverInstallDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //update registry to avoid reboot machine prompt (known issue with sql server installer)
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("reg")
            .args(Arrays.asList("delete", "\"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\"",
                                "/v", "PendingFileRenameOperations", "/f"))
            .build();
        try{
            runFlow(aaClient, RunCommandFlow.class, command);
            LOGGER.info("No error deleting registry keys");
        }
        catch(Exception ex){
            LOGGER.info("Swallowed error deleting registry keys");
        }
        
        //install 
        command = new RunCommandFlowContext.Builder("SETUP.EXE")
            .workDir(sqlserverInstallDir)
            .args(Arrays.asList("/q", "/ACTION=Install", "/FEATURES=SQL", "/INSTANCENAME=SQLExpress",
                "/SQLSVCACCOUNT=\".\\Administrator\"", "/SQLSVCPASSWORD=\"Lister@123\"",
                "/SQLSYSADMINACCOUNTS=\"Builtin\\Administrators\"", "/AGTSVCACCOUNT=\"NT AUTHORITY\\Network Service\"",
                "/IACCEPTSQLSERVERLICENSETERMS", "/SECURITYMODE=SQL", "/SAPWD=\"Lister@123\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        if (shouldDeploySystemApps) {
            //get stocktrader db data/scripts
            url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps", 
                "netstocktrader", "dbdata1.0", "zip", "5.5"));
            context = new GenericFlowContext.Builder()
               .artifactUrl(url)
               .destination(stocktraderDBDataDir)
               .build();
            runFlow(aaClient, GenericFlow.class, context);
            
            //create stocktrader app db schema
            importDBData("accountdb", aaClient);
            importDBData("businessserviceIISRepository", aaClient);
            importDBData("businessserviceRepository", aaClient);
            importDBData("capacityplanner", aaClient);
            importDBData("logging", aaClient);
            importDBData("orderprocessorrepository", aaClient);
            importDBData("quotedb", aaClient);
            importDBData("reportservertempdb", aaClient);
            importDBData("reportserver", aaClient);
            importDBData("stocktraderwebapprepository", aaClient);
        }
    }
    
    private void importDBData(String dbName, IAutomationAgentClient aaClient) {
        
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("SchemaZen.exe")
            .workDir(stocktraderDBDataDir)
            .args(Arrays.asList("create", "--server", ".\\sqlexpress", 
                "--database", dbName, "--scriptDir", stocktraderDBDataDir + "\\" + dbName))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command); 
    }

    private void createIISPool(IAutomationAgentClient aaClient) {
        
        //add iis pool
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "apppool", "/name:\"CustomAppPool4.0\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
   
        //set framework & pipeline mode
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "apppool", "\"CustomAppPool4.0\"", 
                "/managedRuntimeVersion:v4.0", "/recycling.periodicRestart.time:00:00:00", "/enable32BitAppOnWin64:false", "/managedPipelineMode:Integrated"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //set isapi restrictions
        setIsapiRestrictions(aaClient, "Framework\\v4.0.30319");
        setIsapiRestrictions(aaClient, "Framework64\\v4.0.30319");
    }
    
    private void setIsapiRestrictions(IAutomationAgentClient aaClient,
                                      String framework) {
    
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "config", "/section:isapiCgiRestriction",
                "/[path='C:\\Windows\\Microsoft.NET\\" + framework + "\\aspnet_isapi.dll'].allowed:True",
                "/commit:apphost"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    private void createIISSite(IAutomationAgentClient aaClient) {
        
        //create site
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"QASite\"", 
                "/bindings:\"http/*:80:\"", "/physicalPath:\"" + dotNetTestAppDir + "\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //disable IIS logging
        if (shouldDisableHttpLogging) {
            //change log level to server
            command = new RunCommandFlowContext.Builder("appcmd")
                .workDir("C:\\Windows\\system32\\inetsrv")
                .args(Arrays.asList("set", "config", "-section:system.applicationHost/log", "/centralLogFileMode:\"CentralW3C\""))
                .build();
            runFlow(aaClient, RunCommandFlow.class, command);
            
            //disable logging
            command = new RunCommandFlowContext.Builder("appcmd")
                .workDir("C:\\Windows\\system32\\inetsrv")
                .args(Arrays.asList("set", "config", "/section:httpLogging", "/dontLog:True"))
                .build();
            runFlow(aaClient, RunCommandFlow.class, command);
        }
        
        //start site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"QASite\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    private void createIISApps(IAutomationAgentClient aaClient) {
        
        //create DotNetTestApp
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                "/path:/DotNetTestApp", "/physicalPath:\"" + dotNetTestAppDir + "\\web\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign DotNetTestApp to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"QASite/DotNetTestApp\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create wcf app - CalcClient
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                "/path:/CalcClient", "/physicalPath:\"" + calcWCFappDir + "\\CalcClient\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign CalcClient to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"QASite/CalcClient\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create wcf app - CalcDependService
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                "/path:/CalcDependService", "/physicalPath:\"" + calcWCFappDir + "\\CalcDependService\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign CalcDependService to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"QASite/CalcDependService\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create wcf app - CalculatorService
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "app", "/site.name:\"QASite\"", 
                "/path:/CalculatorService", "/physicalPath:\"" + calcWCFappDir + "\\CalculatorService\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign CalculatorService to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"QASite/CalculatorService\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    private void createNerdDinnerApps(IAutomationAgentClient aaClient, String name, int port) {

        // add iis app pool, set framework & pipeline mode
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "apppool", "/name:\"" + name + "\"",
                    "/managedRuntimeVersion:v4.0", "/managedPipelineMode:Integrated", "/recycling.periodicRestart.time:00:00:00"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // create site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"" + name + "\"", 
                "/id:" + port, "/bindings:http/*:" + port + ":", "/physicalPath:\"" + installDir + "\\" + name + "\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // associate site with app pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"" + name + "/\"", "/applicationPool:\"" + name + "\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // setup permissions
        command = new RunCommandFlowContext.Builder("icacls")
            //.workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("\"" + installDir + "\\" + name + "\"", "/T", "/grant", "Users:(R,RX,RD)"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // Modify the web.config file
        Map<String,String> replacePairs = new HashMap<String,String>();
        //replacePairs.put("Server=REPLACE-SERVER-NAME", "Server=" + sqlServerMachineName);
        replacePairs.put("Server=REPLACE-SERVER-NAME", "Server=.\\\\sqlexpress");
        replacePairs.put("User Id=AUTOMATION", "User Id=sa");
        replacePairs.put("Password=AUTOMATION", "Password=Lister@123");
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(installDir + "\\" + name + "\\Web.config", replacePairs)
            .build();
        runFlow(aaClient, FileModifierFlow.class, context);

        // Finally, start the site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"" + name + "\""))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    private void installDotNetTestApp(IAutomationAgentClient aaClient) {

        //build
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("build.bat")
            .workDir(dotNetTestAppDir)
            .args(Arrays.asList("4.0", "64"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //install
        command = new RunCommandFlowContext.Builder("install.bat")
            .workDir(dotNetTestAppDir)
            .args(Arrays.asList("4.0"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //register enterprise services
        command = new RunCommandFlowContext.Builder("registerService.bat")
            .workDir(dotNetTestAppDir + "\\web\\register")
            .args(Arrays.asList("4.0", "64", "i", "DotNetTest.Service.dll"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    private void deployArtifacts(IAutomationAgentClient aaClient) {
        
        //get dotnet-testtools-files
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("dotnet", 
            "dotnet-agent", "dotnet-testtools-files", "zip", tasResolver.getDefaultVersion()));
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(installDir + "/libtemp")
           .build();
        runFlow(aaClient, GenericFlow.class, context);
        
        //get dotnettestapp
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
            "dotnettestapp", "dist", "zip", tasResolver.getDefaultVersion()));
             
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(dotNetTestAppDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);  
        
        //update permissions
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\ATTRIB")
            .args(Arrays.asList("-r", "-s", dotNetTestAppDir + "\\src\\webservices\\*", "/S", "/D"))
            .build();
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //copy test dlls
        FileModifierFlowContext copyFiles = new FileModifierFlowContext.Builder()
            .copy(installDir + "/libtemp/x64/NameResolverSleep.dll", dotNetTestAppDir + "/lib/NameResolverSleep.x64.dll")
            .build();
        runFlow(aaClient, FileModifierFlow.class, copyFiles);
        
        copyFiles = new FileModifierFlowContext.Builder()
            .copy(installDir + "/libtemp/x86/NameResolverSleep.dll", dotNetTestAppDir + "/lib/NameResolverSleep.x86.dll")
            .build();
        runFlow(aaClient, FileModifierFlow.class, copyFiles);
        
        //get wcfapp
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps", 
            "CalcWCFapp", "", "zip", "1.0"));
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(calcWCFappDir)
           .build();
        runFlow(aaClient, GenericFlow.class, context); 

        //get Nerd Dinner MVC 4 Version
        DefaultArtifact artifact = new DefaultArtifact(
            "dotnet",                          // groupId
            "dotnet-agent",                    // artifactId
            "dotnet-nerd-dinner-mvc4-files",   // classifier
            "zip",                             // extension
            tasResolver.getDefaultVersion());
        url = tasResolver.getArtifactUrl(artifact);
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(nerdDinnerMVC4DataDir)
           .build();
        runFlow(aaClient, GenericFlow.class, context);

        //get Nerd Dinner MVC 5 Version
        artifact = new DefaultArtifact(
            "dotnet",                          // groupId
            "dotnet-agent",                    // artifactId
            "dotnet-nerd-dinner-mvc5-files",   // classifier
            "zip",                             // extension
            tasResolver.getDefaultVersion());
        url = tasResolver.getArtifactUrl(artifact);
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(nerdDinnerMVC5DataDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
    }

    public static class Builder extends BuilderBase<Builder, DotNetAppsDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;  
        protected String installDir;
        protected boolean shouldDeploySystemApps;
        protected boolean shouldDisableHttpLogging;
        protected boolean shouldInstallSQLServer;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public DotNetAppsDeployRole build() {
            return getInstance();
        }

        @Override
        protected DotNetAppsDeployRole getInstance() {
            return new DotNetAppsDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }  
        
        public Builder shouldDeploySystemApps(boolean shouldDeploySystemApps) {
            this.shouldDeploySystemApps = shouldDeploySystemApps;
            return builder();
        }
        
        public Builder shouldDisableHttpLogging(boolean shouldDisableHttpLogging) {
            this.shouldDisableHttpLogging = shouldDisableHttpLogging;
            return builder();
        }

        public Builder shouldInstallSQLServer(boolean shouldInstallSQLServer) {
            this.shouldInstallSQLServer = shouldInstallSQLServer;
            return builder();
        }
    }
}