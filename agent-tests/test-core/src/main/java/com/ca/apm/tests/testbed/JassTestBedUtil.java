/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.tests.testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.tests.role.CollectorMMDeployRole;
import com.ca.apm.tests.role.EnableCemApiRole;
import com.ca.apm.tests.role.FetchEMDataRole;
import com.ca.apm.tests.role.FileUpdateRole;
import com.ca.apm.tests.role.SetupEMPostgresWinRole;
import com.ca.apm.tests.role.StartEMRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation Util class
 *
 * @author kurma05
 */
@TestBedDefinition
public class JassTestBedUtil {
    
    public static final String MOM_ROLE_ID             = "momRole";
    public static final String LAX_ROLE_ID             = "laxRole";
    public static final String START_MOM_ROLE_ID       = "startMomRole";
    public static final String START_EM_ROLE_ID        = "startEmRole";
    public static final String MOM_MMJAR_ROLE_ID       = "momMMJarRole";
    public static final String COLLECTOR_MMJAR_ROLE_ID = "collectorMMJarRole";
    public static final String ENABLE_CEM_API_ROLE_ID  = "enableCemApiRole";
    private static EmRole collectorRole                = null;
    //flag to install postgres via *.bat file (on Windows)
    //due to: DE134399: TAS - EM Installer sometimes doesn't install all jars
    public static boolean shouldInstallDbWithEM        = false;
   
    public static void addCollectorRole(ITasResolver tasResolver, 
                                        ITestbedMachine machine,
                                        boolean isCluster) {
        
        addCollectorRole(tasResolver, machine, isCluster, 
            AgentRegressionBaseTestBed.getEMArtifactVersion(tasResolver)); 
    }
    
    public static void addCollectorRole(ITasResolver tasResolver, 
                                        ITestbedMachine machine,
                                        boolean isCluster,
                                        String emVersion) {
        
        Collection<String> collectorFeatures = new ArrayList<String>();
        collectorFeatures.add("Enterprise Manager");
        collectorFeatures.add("ProbeBuilder");
     
        EmRole.Builder builder = new EmRole.Builder(AgentRegressionBaseTestBed.EM_ROLE_ID, tasResolver)
               .version(emVersion)
               .configProperty("introscope.changeDetector.disable", "false")
               .configProperty("introscope.enterprisemanager.performance.compressed", "false")
               .configProperty("log4j.logger.Manager", "INFO, console,logfile")
               .configProperty("log4j.logger.Manager.Performance", "INFO, performance, logfile")
               .configProperty("transport.buffer.input.maxNum", "1500")
               .configProperty("transport.buffer.input.maxNumNio", "6000")
               .configProperty("introscope.enterprisemanager.threaddump.storage.max.disk.usage", "50")
               .configProperty("enable.default.BusinessTransaction", "false")
               .nostartEM()
               .nostartWV();
        
        if(isCluster) {
            builder.emClusterRole(EmRoleEnum.COLLECTOR)
                   .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID));
        }   
        else {
            collectorFeatures.add("WebView"); 
            if(shouldInstallDbWithEM) {
                collectorFeatures.add("Database"); 
            }
        }
        
        builder
            .silentInstallChosenFeatures(collectorFeatures)
            .dbuser("postgres")
            .dbpassword("Lister@123");    
        collectorRole = builder.build();
        
        collectorRole.addProperty("min.heap.mb", "1024");
        collectorRole.addProperty("max.heap.mb", "3072");
        collectorRole.addProperty("max.permsize.mb", "512");
        collectorRole.addProperty("em.port", collectorRole.getEnvPropertyById("emPort"));
        collectorRole.addProperty("em.loc", codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()));
        collectorRole.addProperty("em.java.exe.path", codifyPath(TasBuilder.WIN_JDK_1_7_51 + "/bin/java.exe"));
        collectorRole.addProperty("em.host.name", tasResolver.getHostnameById(AgentRegressionBaseTestBed.EM_ROLE_ID));
  
        //update lax props
        Map<String,String> replacePairs = new HashMap<String,String>();
        replacePairs.put("-Xmx1024m","-Xmx3072m -Djava.awt.headless=false");
        replacePairs.put("-XX:MaxPermSize=256m", "-XX:MaxPermSize=512m -Xloggc:" 
               + codifyPath(AgentRegressionBaseTestBed.DEPLOY_BASE) + "/CollectorGC.log");        
        FileUpdateRole laxRole = new FileUpdateRole.Builder(LAX_ROLE_ID, tasResolver)
            .filePath(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()) + "/Introscope_Enterprise_Manager.lax")
            .replacePairs(replacePairs)
            .build();
     
        //deploy mm.jar to collector
        CollectorMMDeployRole mmJar = new CollectorMMDeployRole.Builder(COLLECTOR_MMJAR_ROLE_ID, tasResolver)
          .emHomeDir(collectorRole.getDeployEmFlowContext().getInstallDir())
          .build();
      
        //update threshold default values
        //TODO maybe create xml parser role instead of using file updater
        replacePairs = new HashMap<String,String>();
        //introscope.enterprisemanager.metrics.historical.limit
        replacePairs.put("<threshold value=\"1200000\"/>","<threshold value=\"2200000\"/>");
        //introscope.enterprisemanager.agent.metrics.limit
        replacePairs.put("<threshold value=\"50000\"/>","<threshold value=\"1200000\"/>");
      
        FileUpdateRole thresholdConfigRole = new FileUpdateRole.Builder("thresholdConfigRole", tasResolver)
            .filePath(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()) + "/config/apm-events-thresholds-config.xml")
            .replacePairs(replacePairs)
            .build();
      
        //start em
        StartEMRole.Builder startEMRoleBuilder = new StartEMRole.Builder(START_EM_ROLE_ID, tasResolver)
            .emHomeDir(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()));
        
        if(isCluster) {
            startEMRoleBuilder.shouldStartView(false);
        }        
        StartEMRole startEMRole = startEMRoleBuilder.build();
        
        if(!isCluster) {
            //add cem role 
            EnableCemApiRole enableCemApiRole = new EnableCemApiRole.Builder(ENABLE_CEM_API_ROLE_ID, tasResolver)
                .emHomeDir(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()))
                .unpackDir(TasBuilder.WIN_SOFTWARE_LOC + "/cemtemp")
                .emInstallVersion(emVersion) 
                .build();
            
             if(!shouldInstallDbWithEM) {
                SetupEMPostgresWinRole postgresRole = new SetupEMPostgresWinRole.Builder("SetupEMPostgresWinRole", tasResolver)
                    .emInstallDir(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()))
                    .emInstallVersion(emVersion)
                    .build();
                collectorRole.before(postgresRole);
                startEMRole.after(postgresRole);
                machine.addRole(postgresRole);
            }
            
            collectorRole.before(enableCemApiRole);
            startEMRole.after(enableCemApiRole);
            machine.addRole(enableCemApiRole);
            machine.addRole(JassTestBedUtil.getUpdatePermissionRole(collectorRole, machine.getMachineId()));
        }
        
        startEMRole.after(laxRole, mmJar, thresholdConfigRole);
        collectorRole.before(laxRole, mmJar, thresholdConfigRole, startEMRole);
        machine.addRole(collectorRole, laxRole, mmJar, thresholdConfigRole, startEMRole);
        
        //backup em data
        FetchEMDataRole fetchEMDataRole = new FetchEMDataRole("fetchEMData", codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()));
        machine.addRole(fetchEMDataRole);
    }
    
    public static void addMomRole(ITasResolver tasResolver, 
                                  ITestbedMachine momMachine) {
        
        addMomRole(tasResolver, momMachine, 
            AgentRegressionBaseTestBed.getEMArtifactVersion(tasResolver));
    }
    
    public static void addMomRole(ITasResolver tasResolver, 
                                  ITestbedMachine momMachine,
                                  String emVersion) {
        
        Collection<String> momFeatures = new ArrayList<String>();
        momFeatures.add("Enterprise Manager");
        momFeatures.add("ProbeBuilder");        
        momFeatures.add("Webview");
        if(shouldInstallDbWithEM) {
            momFeatures.add("Database"); 
        }
        
        EmRole.Builder builder = new EmRole.Builder(MOM_ROLE_ID, tasResolver)
               .version(emVersion)
               .emClusterRole(EmRoleEnum.MANAGER)
               .silentInstallChosenFeatures(momFeatures)
               .dbuser("postgres")
               .dbpassword("Lister@123") 
               .emCollector(collectorRole)
               .nostartEM()
               .nostartWV();        
        //add mom properties
        for (Map.Entry<String, String> entry : getMomProperties().entrySet()) {
            builder.configProperty(entry.getKey(), entry.getValue());
        }
        
        EmRole momRole = builder.build();
        
        //deploy mm.jar to mom
        GenericRole mmJar = new GenericRole.Builder(MOM_MMJAR_ROLE_ID, tasResolver)
         .download(new DefaultArtifact("com.ca.apm.coda.em.jass", "mom", "JASSManagementModule", "jar", "tas"), 
             codifyPath(momRole.getDeployEmFlowContext().getInstallDir()) + "/config/modules/mom-tas-JASSManagementModule.jar")
         .build();
      
        //enable cem api & start em/webview
        EnableCemApiRole enableCemApiRole = new EnableCemApiRole.Builder(ENABLE_CEM_API_ROLE_ID, tasResolver)
            .emHomeDir(codifyPath(momRole.getDeployEmFlowContext().getInstallDir()))
            .unpackDir(TasBuilder.WIN_SOFTWARE_LOC + "/cemtemp")
            .emInstallVersion(emVersion) 
            .build();
       
        StartEMRole startEMRole = new StartEMRole.Builder(START_MOM_ROLE_ID, tasResolver)
            .emHomeDir(codifyPath(momRole.getDeployEmFlowContext().getInstallDir()))
            .shouldStartView(false)
            .build();
        
        if(!shouldInstallDbWithEM) {
            SetupEMPostgresWinRole postgresRole = new SetupEMPostgresWinRole.Builder("SetupEMPostgresWinRole", tasResolver)
                .emInstallDir(codifyPath(collectorRole.getDeployEmFlowContext().getInstallDir()))
                .emInstallVersion(emVersion) 
                .build();
            momRole.before(postgresRole);
            startEMRole.after(postgresRole);
            momMachine.addRole(postgresRole);
        }
        
        //add roles
        momRole.before(mmJar, enableCemApiRole, startEMRole);
        startEMRole.after(enableCemApiRole);
        momMachine.addRole(getUpdatePermissionRole(momRole, momMachine.getMachineId()));
        momMachine.addRole(momRole, mmJar, enableCemApiRole, startEMRole);
    }
    
    public static ExecutionRole getUpdatePermissionRole(EmRole emRole,
                                                        String machineId) {
        
        //update tas base dir to be able to start em/postgres as any user
        //more info in DE136282: EM deployment fails sometimes when running multiple test suites in parallel
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("C:\\Windows\\System32\\icacls.exe")
            .args(Arrays.asList("C:\\automation", "/grant", "Everyone:(OI)(CI)M"))
            .build();   
        ExecutionRole updatePermissionRole =
            new ExecutionRole.Builder(machineId + "_" + "updatePermissionRole")
            .flow(RunCommandFlow.class, command)
            .build();
        
        updatePermissionRole.before(emRole);
        return updatePermissionRole;
    }
    
    private static Map<String,String> getMomProperties() {
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("introscope.changeDetector.disable", "false");              
        props.put("introscope.enterprisemanager.db.driver", "oracle.jdbc.driver.OracleDriver"); 
        props.put("introscope.enterprisemanager.db.url", "jdbc:oracle:thin:@jass6:1521:AUTO");
        props.put("introscope.enterprisemanager.db.username", "JASSEMDATA");
        props.put("introscope.enterprisemanager.db.password.plaintextpassword", "true");
        props.put("introscope.enterprisemanager.db.password", "AUTOMATION");
        props.put("introscope.enterprisemanager.database.collection1.agentExpression", "(.*)");
        props.put("introscope.enterprisemanager.database.collection1.metricExpression", "GC Heap:(.*)");
        props.put("introscope.enterprisemanager.database.collection1.frequencyinseconds", "300");
        props.put("introscope.enterprisemanager.database.collection2.agentExpression", "(.*)");
        props.put("introscope.enterprisemanager.database.collection2.metricExpression", "CPU:Utilization(.*)process(.*)");
        props.put("introscope.enterprisemanager.database.collection2.frequencyinseconds", "300");
        props.put("introscope.enterprisemanager.database.collection3.agentExpression", "(.*)");
        props.put("introscope.enterprisemanager.database.collection3.metricExpression", "GC Monitor(.*)");
        props.put("introscope.enterprisemanager.database.collection3.frequencyinseconds", "300");
        props.put("enable.default.BusinessTransaction", "false");
        
        return props;
    }
    
    @NotNull
    private static String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
}
