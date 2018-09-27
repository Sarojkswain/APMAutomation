package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.tests.role.FileUpdateRole;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Base Windows Testbed for Java & IA
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IAWinBaseTestbed extends BaseTestbed {

    public static final String DEPLOY_BASE                  = TasBuilder.WIN_SOFTWARE_LOC;
    public static final String WEBAPP_ROLE_ID               = "webAppRole";
    public static final String TOMCAT_ROLE_ID               = "tomcatRole";
    public static final String TOMCAT_SCRIPT_UPDATE_ROLE_ID = "tomcatStartScript";
    public static final String TOMCAT_8_HOME                = DEPLOY_BASE + "tomcat" + TomcatVersion.v80;
    public static final String TOMCAT_THIEVES               = TOMCAT_8_HOME + "/webapps/thieves.war";
    public static final int    TOMCAT_PORT                  = 9091;
    public static final String JMETER_PARENT_HOME           = DEPLOY_BASE + "/jmeter";
    public static final String JMETER_SCRIPTS_ROLE_ID       = "jmeterScriptsRole";
    public static final String JMETER_HOME                  = JMETER_PARENT_HOME + "/apache-jmeter-3.1";
    public static final String JMETER_SCRIPTS_HOME          = JMETER_PARENT_HOME + "/scripts";

    @Override
    protected void initMachines(ITasResolver tasResolver) {

        TestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        shouldDeployJmeter = true;
        setupJarvis = true;

        //deploy client & em
        addClientRoles(tasResolver, machine1);
        addEMWinRole(tasResolver, machine1);

        //deploy tomcat/apps/agents
        addTomcatAgentRole(tasResolver, machine1);
        addIARole(tasResolver, machine1);

        //deploy jmeter scripts
        testbed.addProperty("jmeter.install.dir", JMETER_HOME);
        testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_HOME);
        GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver);
        
        machine1.addRole(jmeterScriptsRole);        
        testbed.addMachine(machine1);
    }
    
    protected void initMachines(ITasResolver tasResolver, String agentVmTemplate) {

        TestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        TestbedMachine machine2 =
            new TestbedMachine.Builder(MACHINE2_ID).templateId(agentVmTemplate).build();
        shouldDeployJmeter = true;
        setupJarvis = true;

        //deploy client & jmeter script on machine1
        addClientRoles(tasResolver, machine1);
        testbed.addProperty("jmeter.install.dir", JMETER_HOME);
        testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_HOME);
        GenericRole jmeterScriptsRole = createJmeterScriptsRole(tasResolver);        
        machine1.addRole(jmeterScriptsRole); 
        
        //deploy em/tomcat/apps/agents on machine2
        addEMWinRole(tasResolver, machine2);
        addTomcatAgentRole(tasResolver, machine2);
        addIARole(tasResolver, machine2);
               
        testbed.addMachine(machine1, machine2);
    }
   
    protected void addTomcatAgentRole(ITasResolver tasResolver, TestbedMachine machine) {

        GenericRole thievesAppRole =
            new GenericRole.Builder(WEBAPP_ROLE_ID, tasResolver).download(
                new DefaultArtifact("com.ca.apm", "thieves", "war", "0.0.1-SNAPSHOT"),
                TOMCAT_THIEVES).build();

        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
                .installDir(TOMCAT_8_HOME)
                .tomcatVersion(TomcatVersion.v80)
                .tomcatCatalinaPort(TOMCAT_PORT)
                .build();

        FileUpdateRole tomcatFileUpdateRole = updateTomcatStartScript(tasResolver, TOMCAT_8_HOME);
        
        //deploy agent
        DefaultArtifact agentArtifact = new DefaultArtifact(
                "com.ca.apm.delivery", "agent-noinstaller-tomcat-windows", "zip",
                tasResolver.getDefaultVersion());        
        UniversalRole tomcatAgentRole = new UniversalRole.Builder(
                "tomcatAgentRole", tasResolver)
                .unpack(agentArtifact, TOMCAT_8_HOME)
                .build();
        
        tomcatRole.before(thievesAppRole, tomcatFileUpdateRole, tomcatAgentRole);
        machine.addRole(tomcatRole, thievesAppRole, tomcatFileUpdateRole, tomcatAgentRole);
    }

    protected void addIARole(ITasResolver tasResolver, TestbedMachine machine) {
        
        DefaultArtifact iaArtifact = new DefaultArtifact(
            "com.ca.apm.delivery", "APM-Infrastructure-Agent", "windows", 
            "zip", tasResolver.getDefaultVersion());    
        UniversalRole iaRole = new UniversalRole.Builder(
            "iaRole", tasResolver)
            .unpack(iaArtifact, WIN_DEPLOY_BASE + "\\apmia")
            .build();
        
        machine.addRole(iaRole);   
    }
 
    private FileUpdateRole updateTomcatStartScript(ITasResolver tasResolver, String tomcatDeployPath) {

        String fileName = tomcatDeployPath + "/bin/catalina.bat";
        HashMap<String, String> addJavaOpts = new HashMap<String, String>();

        String originalString = "set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER%\"";
        String updatedString =
                "set \"JAVA_OPTS=%JAVA_OPTS% %LOGGING_MANAGER% -javaagent:%CATALINA_HOME%/wily/Agent.jar"
                + " -Dcom.wily.introscope.agentProfile=%CATALINA_HOME%/wily/core/config/IntroscopeAgent.profile"
                + " -Dcom.wily.introscope.agent.agentName=Tomcat_Thieves\"";

        String fileFinishPrompt_orig = ":end";
        String fileFinishPrompt_updated = ":end\n" + "echo Tomcat started";

        addJavaOpts.put(originalString, updatedString);
        addJavaOpts.put(fileFinishPrompt_orig, fileFinishPrompt_updated);

        FileUpdateRole fileUpdateRole =
            new FileUpdateRole.Builder(TOMCAT_SCRIPT_UPDATE_ROLE_ID, tasResolver)
                .filePath(fileName)
                .replacePairs(addJavaOpts)
                .build();
        return fileUpdateRole;
    }

    protected static GenericRole createJmeterScriptsRole(ITasResolver tasResolver) {

        DefaultArtifact loadJmx =
            new DefaultArtifact("com.ca.apm.saas", "saas-tests-core", "jmeterscripts", "zip",
                tasResolver.getDefaultVersion());

        GenericRole jmeterScriptsRole =
            new GenericRole.Builder(JMETER_SCRIPTS_ROLE_ID, tasResolver)
                .unpack(loadJmx, JMETER_SCRIPTS_HOME)
                .build();

        return jmeterScriptsRole;
    }
}