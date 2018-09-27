package com.ca.apm.tests.testbed;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.WLSAgentAppDeployRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.web.QaAppTomcatRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author banra06
 * 
 */
@TestBedDefinition

public class Weblogic12TestBed  implements ITestbedFactory
 {

public static final String WLS_ROLE_ID = "wls12c";  
public static final String AGENT_MACHINE_ID = "agentMachine";
private static final String AGENT_MACHINE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;
public static final String TOMCAT_ROLE_ID = "tomcatRole";
public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
protected static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole"; 
public static final String WLS_ROLE2_ID = "wls12c_role2";
private JavaRole javaRole;
public static final String WLSMACHINE_1 = "wlsmachine1";
public static final String WLSMACHINE_2 = "wlsmachine2";
protected boolean isJassEnabled = false;
protected boolean isLegacyMode = false;
public String defaultAgentTemplateId = TEMPLATE_W64;
protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
protected static final String WLS12C_INSTALL_HOME = DEPLOY_BASE
    + "Oracle/Middleware12.1.3";
public EmRole emRole=null;
public static final String EM_MACHINE_ID = "emMachine";
public static final String EM_ROLE_ID = "emRole";
private static final String EM_MACHINE_TEMPLATE_ID =TEMPLATE_W64;



            @Override
            public ITestbed create(ITasResolver tasResolver) {
                emRole = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .nostartEM().nostartWV()
                .build();
                ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
                emMachine.addRole(emRole);
                //create QAApp role for Tomcat   
                WebAppRole<TomcatRole> qaAppTomcatRole = new QaAppTomcatRole.Builder(QA_APP_TOMCAT_ROLE_ID, tasResolver)
                .cargoDeploy()
                .contextName("qa-app")
                .build();
               
                 //create Tomcat role
                TomcatRole tomcatRole = new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(9091)
                //.webApp(qaAppTomcatRole)
                .build();
                //create Tomcat Agent role
                IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_AGENT_ROLE_ID, tasResolver)
                .webAppRole(tomcatRole)
                .intrumentationLevel(AgentInstrumentationLevel.FULL)
                .emRole(emRole)
                .build(); 
                ITestbedMachine agentMachine = TestBedUtils.createWindowsMachine(AGENT_MACHINE_ID, AGENT_MACHINE_TEMPLATE_ID);
                
                agentMachine.addRole(tomcatRole, qaAppTomcatRole,tomcatAgentRole);
                ITestbedMachine machine1 = initMachine1(tasResolver);
                ITestbedMachine machine2 = initMachine2(tasResolver);
                return new Testbed(getClass().getSimpleName()).addMachine(emMachine,agentMachine,machine1,machine2);
}
/*@Override
public void initTestbed(Testbed testbed, ITasResolver tasResolver) {

ITestbedMachine machine1 = initMachine1(tasResolver);
ITestbedMachine machine2 = initMachine2(tasResolver);
testbed.addMachine(machine1, machine2);

}*/

@NotNull
protected ITestbedMachine initMachine1(ITasResolver tasResolver) {

TestbedMachine machine = new TestbedMachine.Builder(WLSMACHINE_1)
        .templateId(defaultAgentTemplateId).build();

// Client resources
machine.addRole(new ClientDeployRole.Builder("wls_client01",
        tasResolver).jvmVersion("8").shouldDeployConsoleApps(true)
        .shouldDeployJassApps(isJassEnabled)
        .fldXjvmhost(tasResolver.getHostnameById(WLS_ROLE2_ID)).build());

// String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
// Uncomment above line and replace fldmom01 with emHost

addWeblogicRoles(WLS_ROLE_ID, machine, tasResolver, "tas-itc-n24");
return machine;
}

@NotNull
protected ITestbedMachine initMachine2(ITasResolver tasResolver) {

TestbedMachine machine = new TestbedMachine.Builder(WLSMACHINE_2)
        .templateId(defaultAgentTemplateId).build();

// String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);
// Uncomment above line and replace fldmom01 with emHost
addWeblogicRoles(WLS_ROLE2_ID, machine, tasResolver, "tas-itc-n24");

return machine;
}

protected void addWeblogicRoles(String wlsRoleId, TestbedMachine machine,
    ITasResolver tasResolver, String emHost) {

addWeblogicRoles(wlsRoleId, machine, tasResolver,
        tasResolver.getDefaultVersion(), emHost);
}

protected void addWeblogicRoles(String wlsRoleId, TestbedMachine machine,
    ITasResolver tasResolver, String agentVersion, String emHost) {

// install wls
javaRole = new JavaRole.Builder(machine.getMachineId() + "_"
        + "java8Role", tasResolver).version(
        JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();

GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);
//emHost =emRole;
 emHost = tasResolver.getHostnameById(EM_ROLE_ID);
WLSAgentAppDeployRole wlsAgentPORole = new WLSAgentAppDeployRole.Builder(
        machine.getMachineId() + "_" + wlsRoleId, tasResolver)
        .agentVersion(agentVersion).classifier("jvm7-genericnodb")
        .isLegacyMode(isLegacyMode).isJassEnabled(isJassEnabled)
        .javaRole(javaRole).serverPort("7001").wlsRole(wlsRoleId)
        .emHost(emHost).build();

javaRole.before(wlsRole);
wlsRole.before(wlsAgentPORole);
machine.addRole(javaRole, wlsRole, wlsAgentPORole);
}

@NotNull
protected GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {

ArrayList<String> args = new ArrayList<String>();
args.add("-silent");

RunCommandFlowContext installWlc12cCommand = new RunCommandFlowContext.Builder(
        "configure.cmd").workDir(WLS12C_INSTALL_HOME).args(args)
        .build();

GenericRole wls12cInstallRole = new GenericRole.Builder(wlsRole,
        tasResolver)
        .unpack(new DefaultArtifact("com.ca.apm.binaries", "weblogic",
                "dev", "zip", "12.1.3"),
                codifyPath(WLS12C_INSTALL_HOME))
        .runCommand(installWlc12cCommand).build();

return wls12cInstallRole;
}

@NotNull
protected String codifyPath(String path) {
return FilenameUtils.separatorsToUnix(path);
}
}
