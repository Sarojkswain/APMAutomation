package com.ca.apm.tests.testbed.jvm6;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.WLSAgentAppDeployRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic103TestBed extends AgentRegressionBaseTestBed {

    public static final String WLS_ROLE_ID  = "wls103";
    public static final String WLS_ROLE2_ID = "wls103_role2";
    private CustomJavaRole javaRole;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {         
        
        ITestbed testBed = new Testbed(getTestBedName());
        ITestbedMachine machine1 = initMachine1(tasResolver);
        ITestbedMachine machine2 = initMachine2(tasResolver);
        testBed.addMachine(machine1, machine2);        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());

        return testBed;     
    }
    
    protected void initSystemProperties(ITasResolver tasResolver, 
                                        ITestbed testBed, 
                                        HashMap<String,String> props) {
        
        String host = tasResolver.getHostnameById(WLS_ROLE_ID);
        String xjvmhost = tasResolver.getHostnameById(WLS_ROLE2_ID);
        
        initGenericSystemProperties(tasResolver, testBed, props);
        initWlsSystemProperties(host, xjvmhost, props, javaRole.getInstallDir());        
        setTestngCustomJvmArgs(props, testBed); 
    }
   
    @NotNull
    protected ITestbedMachine initMachine1(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
    
        //EM & misc roles
        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
         
        //testng resources
        machine.addRole(new ClientDeployRole.Builder("wls_client01", tasResolver)
            .jvmVersion("6")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build());  
        
        addWeblogicRoles(WLS_ROLE_ID, machine, tasResolver);   
        return machine;
    }      
    
    @NotNull
    protected ITestbedMachine initMachine2(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_2).templateId(defaultAgentTemplateId).build();

        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);  
        addWeblogicRoles(WLS_ROLE2_ID, machine, tasResolver);  
        
        return machine;
    }
    
    protected void addWeblogicRoles(String wlsRoleId, 
                                    TestbedMachine machine, 
                                    ITasResolver tasResolver) {
        
        //install wls
        javaRole = new CustomJavaRole.Builder(machine.getMachineId() + "_" + "java6Role", tasResolver)
            .version(CustomJavaBinary.WINDOWS_64BIT_JDK_16_45)
            .build();
        WebLogicRole wlsRole = getwls103Role(tasResolver, wlsRoleId);        
        
        //install test apps & agent
        WLSAgentAppDeployRole.Builder builder = new WLSAgentAppDeployRole.Builder(machine.getMachineId() + "_" + wlsRoleId, tasResolver)
            .agentVersion(getAgentArtifactVersion(tasResolver))
            .classifier("jvm6-genericnodb")
            .isLegacyMode(isLegacyMode)
            .isNoRedefEnabled(isNoRedefEnabled)
            .isJassEnabled(isJassEnabled)
            .javaHome(javaRole.getInstallDir())
            .installHome(codifyPath(DEPLOY_BASE) + "Oracle/Middleware10.3")
            .serverPort("7001")
            .wlsRole(wlsRoleId);
     
        if (isAccAgentBundle) {
            builder = builder.accPackageName("WebLogic - Spring").accPackageOsName("windows");
        }
        
        WLSAgentAppDeployRole wlsAgentPORole = builder.build();
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        //set version  
        if(!isAccAgentBundle) {                  
            String artifact = "agent-noinstaller-weblogic-windows";
            if(isLegacyMode) {
                artifact = "agent-legacy-noinstaller-weblogic-windows";
            }    
            DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", artifact, "zip", getAgentArtifactVersion(tasResolver));
            setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        }
        
        javaRole.before(wlsRole);
        wlsRole.before(wlsAgentPORole);         
        machine.addRole(javaRole, wlsRole, wlsAgentPORole, fetchAgentLogs);
    }
}