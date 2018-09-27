package com.ca.apm.tests.role;

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
import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.URL;
import java.util.Arrays;

/**
 * @author kurma05
 */
public class DotNetAppsDeployRole extends AbstractRole {
    
    private ITasResolver tasResolver;
    private String installDir;
    private String dotNetTestAppDir;
    private String cMSGalleryDir;
    private String galleryProDir;
    private String myCommerceBooksDir;
    private String mSPetShop4WebappDir;
    private String mSPetShop4DbDir;
    private boolean shouldDisableHttpLogging;
 
    protected DotNetAppsDeployRole(Builder builder) {
        
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.shouldDisableHttpLogging = builder.shouldDisableHttpLogging;
        dotNetTestAppDir = installDir + "\\DotNetTestApp";
        cMSGalleryDir = installDir + "\\CMSGallery";
        galleryProDir = installDir + "\\GalleryPro";
        myCommerceBooksDir = installDir + "\\MyCommerceBooks";
        mSPetShop4WebappDir = installDir + "\\MSPetShop4";
        mSPetShop4DbDir = installDir + "\\MSPetShop4_DB_Setup";
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        
        installSqlServer(aaClient);
        deployTestArtifacts(aaClient);
        installDotNetTestApp(aaClient);
        createIISPool(aaClient);
        createIISSites(aaClient);
        createIISApps(aaClient);
        startIISSites(aaClient);
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

    private void createIISSites(IAutomationAgentClient aaClient) {
        
        //create QASite
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"QASite\"", 
                "/bindings:\"http/*:8082:\"", "/physicalPath:\"" + dotNetTestAppDir + "\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create CMSGallery site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"CMSGallery\"", 
                "/bindings:\"http/*:8086:\"", "/physicalPath:\"" + cMSGalleryDir + "\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create GalleryPro site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"GalleryPro\"", 
                "/bindings:\"http/*:8081:\"", "/physicalPath:\"" + galleryProDir + "\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create MyCommerceBooks site
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"MyCommerceBooks\"", 
                "/bindings:\"http/*:8084:\"", "/physicalPath:\"" + myCommerceBooksDir + "\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create MSPetShop4
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("add", "site", "/name:\"MSPetShop4\"", 
                "/bindings:\"http/*:8085:\"", "/physicalPath:\"" + mSPetShop4WebappDir + "\\Web\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    
        //disable IIS logging to avoid disk space issue for long-duration tests
        if(shouldDisableHttpLogging) {
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
    }
    
    private void startIISSites(IAutomationAgentClient aaClient) {

        //start QASite
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"QASite\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //start CMSGallery
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"CMSGallery\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //start GalleryPro
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"GalleryPro\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //start MyCommerceBooks
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"MyCommerceBooks\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //start MSPetShop4
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("start", "site", "/site.name:\"MSPetShop4\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //create petshop app db schema
        importPetshopDBData("MSPetShop4", aaClient);
        importPetshopDBData("MSPetShop4Orders", aaClient);
        importPetshopDBData("MSPetShop4Profile", aaClient);
        importPetshopDBData("MSPetShop4Services", aaClient);        
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
        
        //assign CMSGallery to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"CMSGallery/\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign GalleryPro to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"GalleryPro/\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign MyCommerceBooks to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"MyCommerceBooks/\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
        
        //assign MSPetShop4 to an application pool
        command = new RunCommandFlowContext.Builder("appcmd")
            .workDir("C:\\Windows\\system32\\inetsrv")
            .args(Arrays.asList("set", "app", "\"MSPetShop4/\"", 
                "/applicationPool:\"CustomAppPool4.0\""))
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
    
    private void deployTestArtifacts(IAutomationAgentClient aaClient) {
        
        //get dotnet-testtools-files
        URL url = tasResolver.getArtifactUrl(new DefaultArtifact("dotnet",
            "dotnet-agent", "dotnet-testtools-files", "zip", tasResolver.getDefaultVersion()));
        GenericFlowContext context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(installDir + "/libtemp")
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get dotnettestapp
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "DotNetTestApp", "", "zip", "2.1"));            
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
        
        //get CMSGallery
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "CMSGallery", "", "zip", "1.0"));            
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(cMSGalleryDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
       
        //get GalleryPro
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "GalleryPro", "", "zip", "1.0"));            
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(galleryProDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get ShoppingCartNet
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "MyCommerceBooks", "", "zip", "1.0"));            
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(myCommerceBooksDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get MSPetShop4 webapp
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "MSPetShop4", "webapp", "zip", "4.1"));            
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(mSPetShop4WebappDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //get MSPetShop4 db scripts
        url = tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries.testapps",
            "MSPetShop4", "dbdata", "zip", "4.0"));            
        context = new GenericFlowContext.Builder()
           .artifactUrl(url)
           .destination(mSPetShop4DbDir)
           .build();  
        runFlow(aaClient, GenericFlow.class, context);
        
        //update file permissions
        command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe")
            .args(Arrays.asList(installDir, "/grant", "Everyone:(OI)(CI)M"))
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
       
        //install 
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("SETUP.EXE")
            .workDir(sqlserverInstallDir)
            .args(Arrays.asList("/q", "/ACTION=Install", "/FEATURES=SQL", "/INSTANCENAME=SQLExpress",
                "/SQLSVCACCOUNT=\".\\Administrator\"", "/SQLSVCPASSWORD=\"Lister@123\"",
                "/SQLSYSADMINACCOUNTS=\"Builtin\\Administrators\"", "/AGTSVCACCOUNT=\"NT AUTHORITY\\Network Service\"",
                "/IACCEPTSQLSERVERLICENSETERMS", "/SECURITYMODE=SQL", "/SAPWD=\"Lister@123\""))
            .terminateOnMatch("You should restart your computer to complete this process")
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    private void importPetshopDBData(String dbName, IAutomationAgentClient aaClient) {
        
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("SchemaZen.exe")
            .workDir(mSPetShop4DbDir)
            .args(Arrays.asList("create", "--server", ".\\sqlexpress", "--database", dbName, "--scriptDir", mSPetShop4DbDir + "\\" + dbName))
            .build();        
        runFlow(aaClient, RunCommandFlow.class, command);
    }
    
    public static class Builder extends BuilderBase<Builder, DotNetAppsDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String installDir;     
        protected boolean shouldDisableHttpLogging;
        
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
      
        public Builder shouldDisableHttpLogging(boolean shouldDisableHttpLogging) {
            this.shouldDisableHttpLogging = shouldDisableHttpLogging;
            return builder();
        }
    }
}