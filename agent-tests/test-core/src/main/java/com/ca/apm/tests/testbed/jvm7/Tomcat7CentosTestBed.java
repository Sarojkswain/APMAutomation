/*
* Copyright (c) 2016 CA. All rights reserved.
*
* This software and all information contained therein is confidential and proprietary and
* shall not be duplicated, used, disclosed or disseminated in any way except as authorized
* by the applicable license agreement, without the express written permission of CA. All
* authorized reproductions must be marked with this language.
*
* EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
* PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY
* OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
* MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
* LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
* INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
* PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
* EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
*/
package com.ca.apm.tests.testbed.jvm7;

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
 * TAS testbed for Tomcat 7 on Linux without CODA bridge
 * 
 * This testbed uses real EM. 
 */
@TestBedDefinition
public class Tomcat7CentosTestBed extends AgentRegressionBaseTestBed {

    public static final String TOMCAT_ROLE_ID = "tomcat7";    
    private static final String TOMCAT_HOME   = DEPLOY_LINUX_BASE + "apache-tomcat-7.0.57";
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
        initTomcatSystemProperties(host, home, "7", props, javaRole.getInstallDir());
        setTestngCustomJvmArgs(props, testBed); 
    }
    
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.LinuxBuilder(MACHINE_1).templateId(TEMPLATE_CO66).build();
        javaRole = new CustomJavaRole.LinuxBuilder("java7Role", tasResolver).version(CustomJavaBinary.LINUX_64BIT_JDK_17_0_80).build();

        // Add emRole
        addEmLinuxRole(tasResolver, machine);
        
        //Tomcat
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
        testappQATestAppNoDB.setClassifier("jvm7-tomcatnodb");
        TomcatRole tomcat7 = new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
            .jdkHomeDir(javaRole.getInstallDir())
            .tomcatVersion(TomcatVersion.v70).tomcatCatalinaPort(9091)
            .webApplication(testappQATestAppNoDB.createArtifact(), QATESTAPP_CONTEXT)
            .webApplication(testappJava7NewApp.createArtifact(), JAVA7NEWAPP_CONTEXT)
            .webApplication(testappAxis2TestApp.createArtifact("1.0"), AXIS2TESTAPP)
            .webApplication(testappStruts2TestApp.createArtifact("1.0"), STRUTS2TESTAPP)
            .build();
         
        UniversalRole installAgent = tomcatUnixAgentRole(tasResolver, machine, tomcat7, javaRole.getInstallDir(), "7.0");
        
        machine.addRole(new ClientDeployLinuxRole.Builder("tomcat_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(false)
            .build()); 
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + 
              "_" + "fetchAgentLogs", TasBuilder.LINUX_SOFTWARE_LOC + RESULTS_DIR);

        javaRole.before(tomcat7);
        tomcat7.before(installAgent);
        machine.addRole(javaRole, tomcat7, installAgent, fetchAgentLogs);
        
        return machine;
    }      
}
