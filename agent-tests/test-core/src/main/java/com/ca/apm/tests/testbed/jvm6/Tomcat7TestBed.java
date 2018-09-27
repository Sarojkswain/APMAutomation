package com.ca.apm.tests.testbed.jvm6;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Tomcat7TestBed extends AgentRegressionBaseTestBed {

    public static final String TOMCAT_ROLE_ID = "tomcat7";    
    private static final String TOMCAT_HOME   = DEPLOY_BASE + "apache-tomcat-7.0.57";
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
        javaRole = new CustomJavaRole.Builder("java6Role", tasResolver).version(CustomJavaBinary.WINDOWS_64BIT_JDK_16_45).build();

        //EM & misc roles
        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
         
        //Tomcat
        TomcatRole tomcat7 = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
            .jdkHomeDir(javaRole.getInstallDir())
            .tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091)
            .build();
        
        //get QATestApp
        DefaultArtifact qaTestAppArtifact = 
            new DefaultArtifact("com.ca.apm.coda-projects.test-tools", 
                "qatestapp", "jvm6-tomcatnodb", "war", tasResolver.getDefaultVersion());        
        GenericRole qaTestAppRole = new GenericRole.Builder("qaTestAppRole", tasResolver)
            .download(qaTestAppArtifact, tomcat7.getInstallDir() + "/webapps/QATestApp.war")
            .build();  
        
        // get Axis2TestApp
        DefaultArtifact axisAppArtifact = 
            new DefaultArtifact("com.ca.apm.binaries.testapps", "Axis2TestApp", "war", "1.0");        
        GenericRole axisAppRole = new GenericRole.Builder("axisAppRole", tasResolver)
            .download(axisAppArtifact, tomcat7.getInstallDir() + "/webapps/Axis2TestApp.war")
            .build();   
    
        // get Struts app
        DefaultArtifact strutsAppArtifact = 
            new DefaultArtifact("com.ca.apm.binaries.testapps", STRUTS2TESTAPP, "war", "1.0");        
        GenericRole strutsAppRole = new GenericRole.Builder("strutsAppRole", tasResolver)
            .download(strutsAppArtifact, tomcat7.getInstallDir() + "/webapps/" + STRUTS2TESTAPP + ".war")
            .build();   
        
        //misc
        tomcatWinAgentRole(tasResolver, machine, tomcat7, javaRole.getInstallDir(), "7.0");        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        machine.addRole(new ClientDeployRole.Builder("tomcat_client01", tasResolver)
            .jvmVersion("6")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build()); 
        
        javaRole.before(tomcat7);
        tomcat7.before(qaTestAppRole, axisAppRole, strutsAppRole);
        machine.addRole(javaRole, tomcat7, qaTestAppRole, axisAppRole, strutsAppRole, fetchAgentLogs);
        
        return machine;
    }      
}