package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH7;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.role.FileUpdateRole;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Base testbed for Infrastructure agent (IA)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IALinuxBaseTestbed extends BaseTestbed {
  
    protected String linuxTemplate                   = TEMPLATE_RH7; 
    public static final String TOMCAT_ROLE_ID        = "tomcatRole";
    public static final String TOMCAT_SCRIPT_ROLE_ID = "tomcatStartScript";
    public static final int    TOMCAT_PORT        = 9091;
    public static final String TOMCAT_LINUX_HOME  = 
        TasBuilder.LINUX_SOFTWARE_LOC + "tomcat" + TomcatVersion.v80;
    public static final String DOCKERFILE_HOME  = "/opt/tomcat";
    protected boolean shouldInstallDockerCompose = false;
  
    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        //add machines
        TestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        TestbedMachine machine2 =
            new TestbedMachine.Builder(MACHINE2_ID).templateId(linuxTemplate).build();
        
        //add client, em & agents
        addClientRoles(tasResolver, machine1);
        addEMLinuxRole(tasResolver, machine2);        
        IRole tomcatAgentRole = getTomcatAgentsRole(tasResolver, DOCKERFILE_HOME);
        addIARole(tasResolver, machine2);
       
        //required for docker monitoring
        IRole createRepoRole = createCityFanRepo(tasResolver);
        IRole installCurlRole = upgradeCurl(tasResolver);
        installCurlRole.after(createRepoRole);
        
        if(shouldInstallDockerCompose) {
            IRole dockerRole = createDockerComposeRole(tasResolver);
            dockerRole.after(installCurlRole);
            machine2.addRole(dockerRole);            
        }
        
        machine2.addRole(createRepoRole, installCurlRole, tomcatAgentRole);  
        testbed.addMachine(machine1, machine2);
    }
    
    protected void addIARole(ITasResolver tasResolver, TestbedMachine machine) {
        
        DefaultArtifact iaArtifact = new DefaultArtifact(
            "com.ca.apm.delivery", "APM-Infrastructure-Agent", "unix", 
            "tar.gz", tasResolver.getDefaultVersion());    
        UniversalRole iaRole = new UniversalRole.Builder(
            "iaRole", tasResolver)
            .unpack(iaArtifact, "/opt/apmia")
            .build();
        
        machine.addRole(iaRole);        
    }
    
    protected void addTomcatAgentRole(ITasResolver tasResolver, TestbedMachine machine1) {

        //deploy tomcat
        TomcatRole tomcatRole =
            new TomcatRole.LinuxBuilder(TOMCAT_ROLE_ID, tasResolver)
                .installDir(TOMCAT_LINUX_HOME)
                .tomcatVersion(TomcatVersion.v80)
                .tomcatCatalinaPort(TOMCAT_PORT)
                .build();

        //update startup script
        FileUpdateRole tomcatFileUpdateRole = updateTomcatStartScript(tasResolver);
        
        //deploy agent
        IRole tomcatAgentRole = getTomcatAgentsRole(tasResolver, TOMCAT_LINUX_HOME);
        
        tomcatRole.before(tomcatFileUpdateRole, tomcatAgentRole);
        machine1.addRole(tomcatRole, tomcatFileUpdateRole, tomcatAgentRole);
    }
    
    protected FileUpdateRole updateTomcatStartScript(ITasResolver tasResolver){
        
        HashMap<String,String> javaOpts = new HashMap<String,String>();
        
        String originalString = "JAVA_HOME=\"/opt/jdk1.8\";export JAVA_HOME";
        String updatedString = "export JAVA_HOME=\"/opt/jdk1.8\"\n" + 
                    "export JAVA_OPTS=\"\\${JAVA_OPTS} " + 
                    "-javaagent:" + TOMCAT_LINUX_HOME + "/wily/Agent.jar " +
                    "-Dcom.wily.introscope.agentProfile=" + TOMCAT_LINUX_HOME + "/wily/core/config/IntroscopeAgent.profile\"";
        javaOpts.put(originalString,updatedString);
        
        FileUpdateRole fileUpdateRole =
            new FileUpdateRole.Builder(TOMCAT_SCRIPT_ROLE_ID, tasResolver)
                .filePath(TOMCAT_LINUX_HOME + "/bin/setenv.sh")
                .replacePairs(javaOpts)
                .build();
        return fileUpdateRole;
    }
    
    private IRole getTomcatAgentsRole(ITasResolver tasResolver, String unpackDir) {
        
        DefaultArtifact agentArtifact = new DefaultArtifact(
                "com.ca.apm.delivery", "agent-noinstaller-tomcat-unix", "tar",
                tasResolver.getDefaultVersion());        
        UniversalRole tomcatAgentRole = new UniversalRole.Builder(
                "tomcatAgentRole", tasResolver)
                .unpack(agentArtifact, unpackDir)
                .build();
        
        return tomcatAgentRole;        
    }
    
    private IRole createDockerComposeRole(ITasResolver tasResolver) {
            
        RunCommandFlowContext command = new RunCommandFlowContext.Builder("curl")
            .args(Arrays.asList("-L", "https://github.com/docker/compose/releases/download/1.10.0/docker-compose-Linux-x86_64","-o","/usr/local/bin/docker-compose"))     
            .build();     
        ExecutionRole role =
            new ExecutionRole.Builder("upgradeDockerComposeRole")
            .flow(RunCommandFlow.class, command)
            .build();
        
        return role;
    }
    
    private IRole upgradeCurl(ITasResolver tasResolver) {

        RunCommandFlowContext installCurlContext = new RunCommandFlowContext.Builder("yum")
            .args(Arrays.asList("-y", "install", "curl"))     
            .build();     
        ExecutionRole installCurlRole =
            new ExecutionRole.Builder("installCurlRole")
            .flow(RunCommandFlow.class, installCurlContext)
            .build();
        
        return installCurlRole;
    }
        
    private IRole createCityFanRepo(ITasResolver tasResolver) {
        
        ArrayList<String> lines = new ArrayList<String>(); 
        lines.add("[CityFan]");
        lines.add("name=City Fan Repo");
        lines.add("baseurl=http://www.city-fan.org/ftp/contrib/yum-repo/rhel$releasever/$basearch/");
        lines.add("enabled=1");
        lines.add("gpgcheck=0");
       
        FileModifierFlowContext createRepoContext = new FileModifierFlowContext.Builder()
            .create("/etc/yum.repos.d/city-fan.repo", lines)
            .build();        
        UniversalRole createRepoRole = new UniversalRole.Builder(
            "createRepoRole", tasResolver)
            .runFlow(FileModifierFlow.class, createRepoContext)
            .build();
        
        return createRepoRole;
    }
}