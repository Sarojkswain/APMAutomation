package com.ca.apm.tests.testbed.jvm7;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.JBOSSAgentDeployRole;
import com.ca.apm.tests.role.JBOSSWebappDeployRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 * TAS testbed without CODA bridge
 */
@TestBedDefinition
public class Jboss7TestBed extends AgentRegressionBaseTestBed {

    private CustomJavaRole javaRole;
    public static final String JBOSS_HOME = DEPLOY_BASE + "jboss/";
    public static final String JBOSS_ROLE_ID = "jboss7";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {         
        
        ITestbed testBed = new Testbed(getTestBedName());
        testBed.addMachine(initMachine(tasResolver));        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());

        return testBed;     
    }
   
    protected void initSystemProperties(ITasResolver tasResolver, 
                                        ITestbed testBed, 
                                        HashMap<String,String> props) {
     
        String host = tasResolver.getHostnameById(JBOSS_ROLE_ID);
        String home = codifyPath(JBOSS_HOME);

        initGenericSystemProperties(tasResolver, testBed, props);
        initJbossSystemProperties(host, home, "7", props, javaRole.getInstallDir());     
        setTestngCustomJvmArgs(props, testBed);   
    }
    
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new CustomJavaRole.Builder("java7Role", tasResolver).version(CustomJavaBinary.WINDOWS_64BIT_JDK_17_0_80).build();

        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
        
        machine.addRole(new ClientDeployRole.Builder("jboss_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build());
        
        //install jboss
        JbossRole jbossRole = new JbossRole.Builder(JBOSS_ROLE_ID , tasResolver)
            .jbossInstallDirectory(JBOSS_HOME)
            .version(JBossVersion.JBOSS711_CODA)
            .build();
        
        //install agent        
        String jvmArgs = "-Djava.util.logging.manager=org.jboss.logmanager.LogManager -Djboss.modules.system.pkgs=org.jboss.logmanager," + 
                         "com.wily.util,com.wily.util.*,com.wily.introscope,com.wily.agent,com.sun.naming.internal -Doracle.jdbc.timezoneAsRegion=false " + 
                         "-Xbootclasspath/p:" + codifyPath(jbossRole.getInstallDir()) + "/modules/org/jboss/logmanager/main/jboss-logmanager-1.2.2.GA.jar;" + 
                         codifyPath(jbossRole.getInstallDir()) + "/modules/org/jboss/logmanager/log4j/main/jboss-logmanager-log4j-1.0.0.GA.jar;" + 
                         codifyPath(jbossRole.getInstallDir()) + "/modules/org/apache/log4j/main/log4j-1.2.16.jar";
        
        JBOSSAgentDeployRole jbossAgentDeployAppRole = 
            new JBOSSAgentDeployRole.Builder("jboss_agent01", tasResolver)
            .appserverDir(codifyPath(jbossRole.getInstallDir()))
            .javaInstallDir(codifyPath(javaRole.getInstallDir()))
            .isAccAgentBundle(isAccAgentBundle)
            .isLegacyMode(isLegacyMode)
            .serverName(JBOSS_ROLE_ID)
            .platform("windows")
            .additionalJavaOptions(jvmArgs)
            .build();
        
        //set version        
        String artifact = "agent-noinstaller-jboss-windows";
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-jboss-windows";
        }
        DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "", "zip", getAgentArtifactVersion(tasResolver));
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        
        //install test apps
        JBOSSWebappDeployRole jbossWebappDeployAppRole =
                new JBOSSWebappDeployRole.Builder("jboss_webapp01", tasResolver)
                    .appserverDir(codifyPath(jbossRole.getInstallDir()))
                    .serverName(JBOSS_ROLE_ID)
                    .jvmVersionQATestapp("jvm7-jbossnodb")
                    .serverPort("8585")
                    .build();        
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
                
        javaRole.before(jbossRole);
        jbossRole.before(jbossAgentDeployAppRole);
        jbossAgentDeployAppRole.before(jbossWebappDeployAppRole);
        machine.addRole(javaRole, jbossRole, jbossAgentDeployAppRole, jbossWebappDeployAppRole, fetchAgentLogs);

        return machine;
    }   
}