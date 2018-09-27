package com.ca.apm.tests.testbed.jvm7;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.WASAgentDeployRole;
import com.ca.apm.tests.role.WASWebappDeployRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebSphere8Role;
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
public class Websphere8TestBed extends AgentRegressionBaseTestBed {

    public static final String WAS_85_ROLE_ID  = "was85";
    public static final String WAS_85_ROLE2_ID = "was85_role2";
    private JavaRole javaRole;
    
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
        
        String host = tasResolver.getHostnameById(WAS_85_ROLE_ID);
        String xjvmhost = tasResolver.getHostnameById(WAS_85_ROLE2_ID);
        
        initGenericSystemProperties(tasResolver, testBed, props);
        initWasSystemProperties(host, xjvmhost, props, javaRole.getInstallDir(), "java_1.7.1_64");        
        setTestngCustomJvmArgs(props, testBed);    
    }
   
    @NotNull
    protected ITestbedMachine initMachine1(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new JavaRole.Builder("java7Role", tasResolver).version(JavaBinary.WINDOWS_64BIT_JDK_17).build();
    
        //EM & misc roles
        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
         
        //testng resources
        machine.addRole(new ClientDeployRole.Builder("was_client01", tasResolver)
            .jvmVersion("7")
            .shouldDeployConsoleApps(true)
            .shouldDeployJassApps(isJassEnabled)
            .build());  
        
        addWebsphereRoles(WAS_85_ROLE_ID, machine, tasResolver);   
        return machine.addRole(javaRole);
    }      
    
    @NotNull
    protected ITestbedMachine initMachine2(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_2).templateId(defaultAgentTemplateId).build();
        
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);  
        addWebsphereRoles(WAS_85_ROLE2_ID, machine, tasResolver);  
        
        return machine;
    }
    
    private void addWebsphereRoles(String role, TestbedMachine machine, ITasResolver tasResolver) {
        
        //install was    
        WebSphere8Role was85Role = new WebSphere8Role.Builder(role, tasResolver)
            .wasVersion(WebSphere8Version.v85base)
            .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
            .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64)
            .build();
        
        //install was webapps
        WASWebappDeployRole wasWebappDeployAppRole =
            new WASWebappDeployRole.Builder(machine.getMachineId() + "_" + "wasWebappDeployAppRole", tasResolver)
                .appserverDir(codifyPath(was85Role.getInstallDir()))
                .jvmVersion("7")
                .profileName("AppSrv01")
                .serverName("server1")
                .nodeName(tasResolver.getHostnameById(role) + "Node01")
                .minHeapSize("512")
                .maxHeapSize("1024")
                .permSpaceSize("256")
                .maxPermSpaceSize("1024")
                .shouldDeployJassApps(isJassEnabled)
                .build();
        
        // install was agent
        WASAgentDeployRole wasAgentDeployAppRole = getWASAgentDeployRole(tasResolver, was85Role, machine);
        
        //set version    
        if(!isAccAgentBundle) {
            String artifact = "agent-noinstaller-websphere-windows";
            if(isLegacyMode) {
                artifact = "agent-legacy-noinstaller-websphere-windows";
            }     
            DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", 
                artifact, "", "zip", getAgentArtifactVersion(tasResolver));
            setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        }
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        //add roles
        wasWebappDeployAppRole.after(was85Role);
        wasAgentDeployAppRole.after(wasWebappDeployAppRole);         
        machine.addRole(was85Role, wasWebappDeployAppRole, wasAgentDeployAppRole, fetchAgentLogs);
    }
	
	protected WASAgentDeployRole getWASAgentDeployRole(ITasResolver tasResolver,
	        WebSphere8Role was85Role, TestbedMachine machine) {
	    
		WASAgentDeployRole.Builder builder = new WASAgentDeployRole.Builder(machine.getMachineId()
		        + "_" + "wasAgentDeployAppRole", tasResolver)
		        .appserverDir(codifyPath(was85Role.getInstallDir()))
		        .isNoRedefEnabled(isNoRedefEnabled)
		        .isLegacyMode(isLegacyMode)
		        .serverName("server1")
		        .installScriptPath(
		                codifyPath(was85Role.getInstallDir() + "/TestApps/ws_pipeorgan3"));

		if (isAccAgentBundle) {
			builder = builder.accPackageName("WebSphere - Spring").accPackageOsName("windows");
		}
		return builder.build();
	}
}