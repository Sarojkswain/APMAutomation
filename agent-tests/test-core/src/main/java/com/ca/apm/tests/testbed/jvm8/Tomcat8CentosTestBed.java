package com.ca.apm.tests.testbed.jvm8;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.artifact.TestAppArtifact;
import com.ca.apm.tests.role.ClientDeployLinuxRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Devesh Bajpai (bajde02), Abhishek Sinha (sinab10), Liddy Hsieh (hsiwa01)
 * TAS testbed for Tomcat 8 on Centos
 * 
 */
@TestBedDefinition
public class Tomcat8CentosTestBed extends AgentRegressionBaseTestBed {

    public static final String TOMCAT_ROLE_ID = "tomcat8";    
    private static final String TOMCAT_HOME   = DEPLOY_LINUX_BASE + "apache-tomcat-8.0.17";
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
        
        String home = codifyPath(TOMCAT_HOME);
        String host = tasResolver.getHostnameById(TOMCAT_ROLE_ID);
        
        //set a flag for linux properties and targets
        isLinuxTestbed = true;
        
        initGenericSystemProperties(tasResolver, testBed, props);
        initTomcatSystemProperties(host, home, "8", props, javaRole.getInstallDir());
        setTestngCustomJvmArgs(props, testBed); 
    }
    
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.LinuxBuilder(MACHINE_1).templateId(TEMPLATE_CO66).build();
        javaRole = new CustomJavaRole.LinuxBuilder("java8Role", tasResolver).version(CustomJavaBinary.LINUX_64BIT_JDK_18_0_112).build();

        // install EM
        addEmLinuxRole(tasResolver, machine);
         
        //Tomcat
        String QATESTAPP_JVM8_CONTEXT = "qatestapp-jvm8";
        TestAppArtifact testappQATestAppJVM8 = new TestAppArtifact(tasResolver);
        testappQATestAppJVM8.setArtifactID(QATESTAPP_JVM8_CONTEXT);
        testappQATestAppJVM8.setClassifier("dist");
        
        TestAppArtifact testappJava7NewApp = new TestAppArtifact(tasResolver);
        testappJava7NewApp.setArtifactID(JAVA7NEWAPP_ARTIFACTID);
        testappJava7NewApp.setClassifier("dist-tomcat");

        TestAppArtifact testappAxis2TestApp = new TestAppArtifact(tasResolver);
        testappAxis2TestApp.setGroupID("com.ca.apm.binaries.testapps");
        testappAxis2TestApp.setArtifactID(AXIS2TESTAPP);      
        
        TestAppArtifact testappStruts2TestApp = new TestAppArtifact(tasResolver);
        testappStruts2TestApp.setGroupID("com.ca.apm.binaries.testapps");
        testappStruts2TestApp.setArtifactID(STRUTS2TESTAPP);
        
        TestAppArtifact testappQATestAppNoDB = new TestAppArtifact(tasResolver);
        testappQATestAppNoDB.setClassifier("jvm8-tomcatnodb");
        TomcatRole tomcat8 = new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
            .jdkHomeDir(javaRole.getInstallDir())
            .tomcatVersion(TomcatVersion.v80).tomcatCatalinaPort(9091)
            .webApplication(testappQATestAppNoDB.createArtifact(), QATESTAPP_CONTEXT)
            .webApplication(testappQATestAppJVM8.createArtifact(), QATESTAPP_JVM8_CONTEXT)
            .webApplication(testappJava7NewApp.createArtifact(), JAVA7NEWAPP_CONTEXT)
            .webApplication(testappAxis2TestApp.createArtifact("1.0"), AXIS2TESTAPP)
            .webApplication(testappStruts2TestApp.createArtifact("1.0"), STRUTS2TESTAPP)
            .build();
         
        UniversalRole installAgent = tomcatUnixAgentRole(tasResolver, machine, tomcat8, javaRole.getInstallDir(), "8.0");
        
        machine.addRole(new ClientDeployLinuxRole.Builder("tomcat_client01", tasResolver)
            .jvmVersion("8")
            .shouldDeployConsoleApps(false)
            .build()); 
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + 
            "_" + "fetchAgentLogs", TasBuilder.LINUX_SOFTWARE_LOC + RESULTS_DIR);

        javaRole.before(tomcat8);
        tomcat8.before(installAgent);
        machine.addRole(javaRole, tomcat8, installAgent, fetchAgentLogs);
        
        return machine;
    }
}
