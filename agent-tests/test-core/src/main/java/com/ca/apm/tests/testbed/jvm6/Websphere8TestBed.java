package com.ca.apm.tests.testbed.jvm6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.WASAgentDeployRole;
import com.ca.apm.tests.role.WASWebappDeployRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Websphere8TestBed extends AgentRegressionBaseTestBed {

    public static final String WAS_85_ROLE_ID  = "was85";
    public static final String WAS_85_ROLE2_ID = "was85_role2";
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
        
        String host = tasResolver.getHostnameById(WAS_85_ROLE_ID);
        String xjvmhost = tasResolver.getHostnameById(WAS_85_ROLE2_ID);
        
        initGenericSystemProperties(tasResolver, testBed, props);
        initWasSystemProperties(host, xjvmhost, props, javaRole.getInstallDir(), "java");        
        setTestngCustomJvmArgs(props, testBed);    
    }
   
    @NotNull
    protected ITestbedMachine initMachine1(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new CustomJavaRole.Builder("java6Role", tasResolver)
            .version(CustomJavaBinary.WINDOWS_64BIT_JDK_16_45)
            .build();
        
        //EM & misc roles
        addQCUploadRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
         
        //testng resources
        machine.addRole(new ClientDeployRole.Builder("was_client01", tasResolver)
            .jvmVersion("6")
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
    
    @NotNull
    private GenericRole getWASJdk6Role(ITasResolver tasResolver, String roleId, String wasInstallDir) {

        String dir = DEPLOY_BASE + "installers/was_jdk6";
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("install");
        args.add("8.5.0.0-WS-WASJavaSDK-WinX64-IFPI76779");
        args.add("-repositories");
        args.add(dir);
        args.add("-installationDirectory");
        args.add(wasInstallDir);
        args.add("-acceptLicense");
        args.add("-log");
        args.add("was_java6_install.log");
       
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("imcl")
            .workDir("C:\\Program Files (x86)\\IBM\\Installation Manager\\eclipse\\tools")
            .args(args)
            .build();        
        GenericRole role = new GenericRole.Builder(roleId + "_wasJdk6Role", tasResolver)
            .unpack(new DefaultArtifact("com.ca.apm.binaries", "was-java", "8.5.0.0-winx64", "zip", "1.6.0_SR8_FP41"), dir)
            .runCommand(command)
            .build();  
        
        return role;
    }
    
    private void addWebsphereRoles(String roleId, TestbedMachine machine, ITasResolver tasResolver) {
        
        //install was    
        WebSphere8Role was85Role = new WebSphere8Role.Builder(roleId, tasResolver)
            .wasVersion(WebSphere8Version.v85base)
            .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
            .build();
    
        //install jdk6 update
        GenericRole wasJdk6Role = getWASJdk6Role(tasResolver, machine.getMachineId(), was85Role.getInstallDir());
           
        //switch WAS to use jdk6  
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("managesdk")
            .workDir(was85Role.getInstallDir() + "/bin")
            .args(Arrays.asList("-enableProfileAll", "-sdkname", "1.6_64", "-enableServers"))
            .build();        
        ExecutionRole switchToJdk6Role = new ExecutionRole.Builder(machine.getMachineId() + "_switchToJdk6Role")
            .flow(RunCommandFlow.class, command)
            .build();
        
        //install was webapps
        WASWebappDeployRole wasWebappDeployAppRole =
            new WASWebappDeployRole.Builder(machine.getMachineId() + "_wasWebappDeployAppRole", tasResolver)
                .appserverDir(codifyPath(was85Role.getInstallDir()))
                .jvmVersion("6")
                .profileName("AppSrv01")
                .serverName("server1")
                .nodeName(tasResolver.getHostnameById(roleId) + "Node01")
                .minHeapSize("512")
                .maxHeapSize("1024")
                .permSpaceSize("256")
                .maxPermSpaceSize("1024")
                .shouldDeployJassApps(isJassEnabled)
                .build();
        
        //install was agent
        WASAgentDeployRole.Builder wasRoleBuilder = 
        new WASAgentDeployRole.Builder(machine.getMachineId() + "_" + "wasAgentDeployAppRole", tasResolver)
            .appserverDir(codifyPath(was85Role.getInstallDir()))
            .isNoRedefEnabled(isNoRedefEnabled)
            .isLegacyMode(isLegacyMode)
            .serverName("server1")
            .installScriptPath(codifyPath(was85Role.getInstallDir() + "/TestApps/ws_pipeorgan3"));
        
		if (isAccAgentBundle) {
			wasRoleBuilder = wasRoleBuilder.accPackageName("WebSphere - Spring").accPackageOsName(
			        "windows");
		}
		WASAgentDeployRole wasAgentDeployAppRole = wasRoleBuilder.build();
		
		// set version
		if (!isAccAgentBundle) {
			String artifact = "agent-noinstaller-websphere-windows";
			if (isLegacyMode) {
				artifact = "agent-legacy-noinstaller-websphere-windows";
			}
			DefaultArtifact agentArtifact = new DefaultArtifact("com.ca.apm.delivery", artifact,
			        "", "zip", getAgentArtifactVersion(tasResolver));
			setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
		}
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        
        //add roles
        was85Role.before(wasJdk6Role, wasWebappDeployAppRole);
        switchToJdk6Role.after(wasJdk6Role);
        wasAgentDeployAppRole.after(wasWebappDeployAppRole);         
        machine.addRole(was85Role, wasJdk6Role, switchToJdk6Role, wasWebappDeployAppRole, wasAgentDeployAppRole, fetchAgentLogs);
    }
}