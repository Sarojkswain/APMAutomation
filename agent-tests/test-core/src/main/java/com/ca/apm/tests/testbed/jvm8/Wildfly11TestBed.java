package com.ca.apm.tests.testbed.jvm8;

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
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 * TAS testbed
 */
@TestBedDefinition
public class Wildfly11TestBed extends AgentRegressionBaseTestBed {

    private static final String WILDFLY_INSTALL_HOME = DEPLOY_BASE + "wildfly-11.0.0.Final/";
    public static final String WILDFLY_ROLE_ID       = "wildfly11";
    private CustomJavaRole javaRole;

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
     
        String host = tasResolver.getHostnameById(WILDFLY_ROLE_ID);
        String home = codifyPath(WILDFLY_INSTALL_HOME);

        initGenericSystemProperties(tasResolver, testBed, props);
        initJbossSystemProperties(host, home, "11", props, javaRole.getInstallDir());     
        setTestngCustomJvmArgs(props, testBed);   
    }
    
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new CustomJavaRole.Builder("java8Role", tasResolver)
            .version(CustomJavaBinary.WINDOWS_64BIT_JDK_18_0_131)
            .shouldUpdateJavaSecurity(true)
            .build(); 

        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
        
        machine.addRole(new ClientDeployRole.Builder("wildfly_client01", tasResolver)
            .jvmVersion("8")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build());
    
        //install wildfly
        GenericRole wildflyRole = new GenericRole.Builder(WILDFLY_ROLE_ID, tasResolver)
            .unpack(new DefaultArtifact("com.ca.apm.binaries", "wildfly", "zip", "11.0.0.Final"),  
                codifyPath(WILDFLY_INSTALL_HOME))
            .build();  
        
        //install agent
        String jvmArgs = "-Djava.util.logging.manager=org.jboss.logmanager.LogManager -Djboss.modules.system.pkgs=org.jboss.logmanager," +     
            "org.jboss.byteman,com.wily.util,com.wily.util.*,com.wily.introscope,com.wily.agent,com.sun.naming.internal " +
            "-Xbootclasspath/p:" + codifyPath(WILDFLY_INSTALL_HOME) + "/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-2.0.7.Final.jar;" +
            codifyPath(WILDFLY_INSTALL_HOME) + "/modules/system/layers/base/org/jboss/log4j/logmanager/main/log4j-jboss-logmanager-1.1.4.Final.jar";
        
        JBOSSAgentDeployRole wildflyAgentDeployAppRole = 
            new JBOSSAgentDeployRole.Builder("wildfly_agent01", tasResolver)
                .appserverDir(codifyPath(WILDFLY_INSTALL_HOME))
                .javaInstallDir(codifyPath(javaRole.getInstallDir()))
                .isAccAgentBundle(isAccAgentBundle)
                .isLegacyMode(isLegacyMode)
                .serverName(WILDFLY_ROLE_ID)
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
        JBOSSWebappDeployRole wildflyWebappDeployAppRole =
                new JBOSSWebappDeployRole.Builder("wildfly_webapp01", tasResolver)
                    .appserverDir(codifyPath(WILDFLY_INSTALL_HOME))
                    .serverName(WILDFLY_ROLE_ID)
                    .jvmVersionQATestapp("jvm8-jbossnodb")
                    .serverPort("8585")
                    .build();        
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        javaRole.before(wildflyRole);
        wildflyRole.before(wildflyAgentDeployAppRole);
        wildflyAgentDeployAppRole.before(wildflyWebappDeployAppRole);
        
        machine.addRole(javaRole, wildflyRole, wildflyAgentDeployAppRole, wildflyWebappDeployAppRole, fetchAgentLogs);
        return machine;
    }   
}