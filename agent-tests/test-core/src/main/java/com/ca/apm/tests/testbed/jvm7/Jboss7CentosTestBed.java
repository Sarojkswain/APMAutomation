package com.ca.apm.tests.testbed.jvm7;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployLinuxRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.JBOSSAgentDeployRole;
import com.ca.apm.tests.role.JBOSSWebappDeployRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 * Jboss CentOS Testbed
 */
@TestBedDefinition
public class Jboss7CentosTestBed extends AgentRegressionBaseTestBed {

    public static final String JBOSS_HOME = TasBuilder.LINUX_SOFTWARE_LOC + "jboss/";
    public static final String JBOSS_ROLE_ID = "jboss7";
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
     
        String host = tasResolver.getHostnameById(JBOSS_ROLE_ID);
        
        isLinuxTestbed = true;
        initGenericSystemProperties(tasResolver, testBed, props);
        initJbossSystemProperties(host, JBOSS_HOME, "7", props, javaRole.getInstallDir());     
        setTestngCustomJvmArgs(props, testBed);   
    }
    
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.LinuxBuilder(MACHINE_1).templateId(TEMPLATE_CO66).build();
        javaRole = new CustomJavaRole.LinuxBuilder("java7Role", tasResolver).version(CustomJavaBinary.LINUX_64BIT_JDK_17_0_80).build();

        //install em
        addEmLinuxRole(tasResolver, machine);
        
        //install testng/jmeter
        machine.addRole(new ClientDeployLinuxRole.Builder("jboss_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(false)
            .build());
        
        //install jboss
        JbossRole jbossRole = new JbossRole.LinuxBuilder(JBOSS_ROLE_ID , tasResolver)
            .jbossInstallDirectory(JBOSS_HOME)
            .version(JBossVersion.JBOSS711_CODA)
            .build();
        
        //install agent        
        String jvmArgs = "-Djava.util.logging.manager=org.jboss.logmanager.LogManager -Djboss.modules.system.pkgs=org.jboss.logmanager," + 
                         "com.wily.util,com.wily.util.*,com.wily.introscope,com.wily.agent,com.sun.naming.internal -Doracle.jdbc.timezoneAsRegion=false " + 
                         "-Xbootclasspath/p:" + jbossRole.getInstallDir() + "/modules/org/jboss/logmanager/main/jboss-logmanager-1.2.2.GA.jar:" + 
                         jbossRole.getInstallDir() + "/modules/org/jboss/logmanager/log4j/main/jboss-logmanager-log4j-1.0.0.GA.jar:" + 
                         jbossRole.getInstallDir() + "/modules/org/apache/log4j/main/log4j-1.2.16.jar";
        
        JBOSSAgentDeployRole jbossAgentDeployAppRole = 
            new JBOSSAgentDeployRole.Builder("jboss_agent01", tasResolver)
            .appserverDir(jbossRole.getInstallDir())
            .javaInstallDir(javaRole.getInstallDir())
            .isAccAgentBundle(isAccAgentBundle)
            .isLegacyMode(isLegacyMode)
            .serverName(JBOSS_ROLE_ID)
            .platform("unix")
            .additionalJavaOptions(jvmArgs)
            .build();
        
        //set version        
        String artifact = "agent-noinstaller-jboss-unix";
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-jboss-unix";
        }
        DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", 
            artifact, "", "tar", tasResolver.getDefaultVersion());
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        
        //install test apps
        JBOSSWebappDeployRole jbossWebappDeployAppRole =
                new JBOSSWebappDeployRole.Builder("jboss_webapp01", tasResolver)
                    .appserverDir(jbossRole.getInstallDir())
                    .serverName(JBOSS_ROLE_ID)
                    .jvmVersionQATestapp("jvm7-jbossnodb")
                    .serverPort("8585")
                    .build();        
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", 
            TasBuilder.LINUX_SOFTWARE_LOC + "/" + RESULTS_DIR);
                
        javaRole.before(jbossRole);
        jbossRole.before(jbossAgentDeployAppRole);
        jbossAgentDeployAppRole.before(jbossWebappDeployAppRole);
        machine.addRole(javaRole, jbossRole, jbossAgentDeployAppRole, jbossWebappDeployAppRole, fetchAgentLogs);

        return machine;
    }   
}