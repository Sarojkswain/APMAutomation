package com.ca.apm.tests.testbed.jvm7;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.artifact.TestAppArtifact;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.TomcatRole;
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
public class Tomcat7TestBed extends AgentRegressionBaseTestBed {

    public static final String TOMCAT_ROLE_ID = "tomcat8";    
    protected static final String TOMCAT_HOME   = DEPLOY_BASE + "apache-tomcat-7.0.57";
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
        
        initGenericSystemProperties(tasResolver, testBed, props);
        initTomcatSystemProperties(host, home, "7", props, javaRole.getInstallDir());
        setTestngCustomJvmArgs(props, testBed); 
    }
   
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new CustomJavaRole.Builder("java7Role", tasResolver).version(CustomJavaBinary.WINDOWS_64BIT_JDK_17_0_80).build();

        //EM & misc roles
        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
         
        //Tomcat
        TestAppArtifact testappQATestAppNoDB = new TestAppArtifact(tasResolver);
        testappQATestAppNoDB.setClassifier("jvm7-tomcatnodb");
        
        TestAppArtifact testappJava7NewApp = new TestAppArtifact(tasResolver);
        testappJava7NewApp.setArtifactID(JAVA7NEWAPP_ARTIFACTID);
        testappJava7NewApp.setClassifier("dist-tomcat");

        TestAppArtifact testappAxis2TestApp = new TestAppArtifact(tasResolver);
        testappAxis2TestApp.setGroupID("com.ca.apm.binaries.testapps");
        testappAxis2TestApp.setArtifactID(AXIS2TESTAPP);
        
        TestAppArtifact testappStruts2TestApp = new TestAppArtifact(tasResolver);
        testappStruts2TestApp.setGroupID("com.ca.apm.binaries.testapps");
        testappStruts2TestApp.setArtifactID(STRUTS2TESTAPP);
    
        TomcatRole.Builder tomcat7Builder = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
            .jdkHomeDir(javaRole.getInstallDir())
            .tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091)
            .webApplication(testappQATestAppNoDB.createArtifact(), QATESTAPP_CONTEXT)
            .webApplication(testappJava7NewApp.createArtifact(), JAVA7NEWAPP_CONTEXT)
            .webApplication(testappAxis2TestApp.createArtifact("1.0"), AXIS2TESTAPP)
            .webApplication(testappStruts2TestApp.createArtifact("1.0"), STRUTS2TESTAPP);
        
        if(isJassEnabled) {
            TestAppArtifact springTestApp = new TestAppArtifact(tasResolver);
            springTestApp.setGroupID("com.ca.apm.binaries.testapps");
            springTestApp.setArtifactID(SPRINGTESTAPP);
            tomcat7Builder.webApplication(springTestApp.createArtifact("1.0"), SPRINGTESTAPP);
        }
        
        TomcatRole tomcat7 = tomcat7Builder.build();
        
        if(isJassEnabled) {
            addKonakartRoles(machine, tomcat7, tasResolver);
        }
        
        tomcatWinAgentRole(tasResolver, machine, tomcat7, javaRole.getInstallDir(), "7.0");
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        machine.addRole(new ClientDeployRole.Builder("tomcat_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build()); 
        
        javaRole.before(tomcat7);
        machine.addRole(javaRole, tomcat7, fetchAgentLogs);
        
        return machine;
    }    
}