package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.utils.BuilderFactories;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;


@TestBedDefinition
public class DifferentialAnalysisTestbed implements ITestbedFactory {

    public static final String MOM_MACHINE = "momEm";
    public static final String MOM_ROLE = "momRole";

    public static final String COLL_ONE_MACHINE = "collOneEm";
    public static final String COLL_ONEROLE = "collOneRole";

    public static final String COLL_TWO_MACHINE = "collTwoEm";
    public static final String COLL_TWOROLE = "collTwoRole";
    public static final String TOMCAT_LOCATION = "/root/Tomcat";

    @Override
    public ITestbed create(ITasResolver arg0) {

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());

        // Collector Roles
        EmRole collOneRole =
            new EmRole.LinuxBuilder(COLL_ONEROLE, arg0).emClusterRole(EmRoleEnum.COLLECTOR).build();
        ITestbedMachine collOneMachine =
            TestBedUtils.createLinuxMachine(COLL_ONE_MACHINE, ITestbedMachine.TEMPLATE_CO66,
                collOneRole);
        testbed.addMachine(collOneMachine);

        EmRole collTwoRole =
            new EmRole.LinuxBuilder(COLL_TWOROLE, arg0).emClusterRole(EmRoleEnum.COLLECTOR).build();
        ITestbedMachine collTwoMachine =
            TestBedUtils.createLinuxMachine(COLL_TWO_MACHINE, ITestbedMachine.TEMPLATE_CO66,
                collTwoRole);
        testbed.addMachine(collTwoMachine);

        // Mom Role
        EmRole momEMRole =
            new EmRole.LinuxBuilder(MOM_ROLE, arg0).emCollector(collOneRole)
                .emCollector(collTwoRole).emClusterRole(EmRoleEnum.MANAGER).build();

        ITestbedMachine momMachine =
            TestBedUtils.createLinuxMachine(MOM_MACHINE, ITestbedMachine.TEMPLATE_CO66, momEMRole);
        testbed.addMachine(momMachine);

        createLoadScript(momMachine);

        // Two Tomcat Roles one on each Collector
        TomcatRole tomcatCollOneRole = createTomcatRole("collOneTomcatRole", arg0, TOMCAT_LOCATION);
        collOneMachine.addRole(tomcatCollOneRole);

        TomcatRole tomcatCollTwoRole = createTomcatRole("collTwoTomcatRole", arg0, TOMCAT_LOCATION);
        collTwoMachine.addRole(tomcatCollTwoRole);

        // Tomcat Properties Edit Roles
        ExecutionRole propertiesEditRoleTomcatOne =
            tomcatPropertiesEditRole("Tomcat1", TOMCAT_LOCATION);
        propertiesEditRoleTomcatOne.after(tomcatCollOneRole);
        collOneMachine.addRole(propertiesEditRoleTomcatOne);
        ExecutionRole propertiesEditRoleTomcatTwo =
            tomcatPropertiesEditRole("Tomcat2", TOMCAT_LOCATION);
        propertiesEditRoleTomcatTwo.after(tomcatCollTwoRole);
        collTwoMachine.addRole(propertiesEditRoleTomcatTwo);

        // WebApp Roles
        ExecutionRole webAppOneRole = webAppRole("One");
        ExecutionRole webAppTwoRole = webAppRole("Two");

        collOneMachine.addRole(webAppOneRole);
        collTwoMachine.addRole(webAppTwoRole);

        // Two Agent Roles one on each Collector
        @SuppressWarnings("deprecation")
        IRole collOneAgentRole =
            BuilderFactories.getAgentBuilder(Platform.LINUX, "collOneAgentRole", arg0)
                .webAppRole(tomcatCollOneRole).emRole(collOneRole).build();
        collOneAgentRole.after(tomcatCollOneRole);
        collOneMachine.addRole(collOneAgentRole);

        @SuppressWarnings("deprecation")
        IRole collTwoAgentRole =
            BuilderFactories.getAgentBuilder(Platform.LINUX, "collTwoAgentRole", arg0)
                .webAppRole(tomcatCollTwoRole).emRole(collTwoRole).build();
        collTwoAgentRole.after(tomcatCollTwoRole);
        collTwoMachine.addRole(collTwoAgentRole);
        return testbed;
    }

    protected TomcatRole createTomcatRole(String roleString, ITasResolver tasResolver,
        String location) {

        return new TomcatRole.LinuxBuilder(roleString + "_Role", tasResolver).installDir(location)
            .tomcatVersion(TomcatVersion.v70).build();
    }

    // Add Java Agent to Catalina file
    protected ExecutionRole tomcatPropertiesEditRole(String roleString, String location) {
        Map<String, String> configData = new HashMap<String, String>();
        String replacedString =
            "# ----- Execute The Requested Command -----------------------------------------";
        String replacingString =
            "# ----------------------------- Wily Changes ---------------------------"
                + "\n"
                + "export AGENT_HOME="
                + Matcher.quoteReplacement("$")
                + "CATALINA_HOME/wily/Agent.jar"
                + "\n"
                + "export AGENT_PROFILE="
                + Matcher.quoteReplacement("$")
                + "CATALINA_HOME/wily/core/config/IntroscopeAgent.profile"
                + "\n"
                + "# ----------------------------- Wily Changes ---------------------------"
                + "\n"
                + "export JAVA_OPTS=\""
                + Matcher.quoteReplacement("$")
                + "JAVA_OPTS -Xmx1000m -XX:MaxPermSize=256m -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="
                + Matcher.quoteReplacement("$")
                + "CATALINA_BASE/conf/logging.properties -javaagent:"
                + Matcher.quoteReplacement("$") + "AGENT_HOME -Dcom.wily.introscope.agentProfile="
                + Matcher.quoteReplacement("$")
                + "AGENT_PROFILE -Dcom.wily.introscope.agent.agentName=" + roleString + "\"";
        String configFile =
            location + BuilderBase.LINUX_SEPARATOR + "bin" + BuilderBase.LINUX_SEPARATOR
                + "catalina.sh";
        configData.put(replacedString, replacingString);
        return new ExecutionRole.Builder(roleString + "_CatalinaChangeRole").flow(
            FileModifierFlow.class,
            new FileModifierFlowContext.Builder().replace(configFile, configData).build()).build();
    }

    protected ExecutionRole webAppRole(String role) {
        Collection<String> data =
            Arrays
                .asList("mkdir /root/JarExtract;cd /root/JarExtract;x=$(find /tmp/ -name \\DifferentialAnalysis*.jar);/opt/jdk1.7/bin/jar xf $x && \n"
                    + "cp /root/JarExtract/webapps/* /root/Tomcat/webapps/ \n"
                    + "sh "
                    + TOMCAT_LOCATION
                    + "/bin/startup.sh && \n"
                    + "sleep 30; echo Tomcat Server started ....");

        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create("/opt/temp/" + "temp.sh", data).build();
        ExecutionRole execRole =
            new ExecutionRole.Builder("tempExtract" + role + "_Role")
                .flow(FileModifierFlow.class, createFileFlow)
                .command(new RunCommandFlowContext.Builder("/opt/temp/" + "temp.sh").build())
                .build();
        return execRole;
    }

    protected void createLoadScript(ITestbedMachine machine) {
        Collection<String> data =
            Arrays.asList("#!/bin/bash \n" + "function_curl() \n" + "{ \n"
                + "for i in {1..65..1} \n" + "do \n" + "if [ \"$i\" -lt 54 -a \"$i\" -gt 48 ] \n"
                + "then  \n" + "curl -O http://Host.ca.com:8080/testapp1/frontend?sleep=1000 \n"
                + "curl -O http://Host.ca.com:8080/testapp2/frontend?sleep=1000 \n"
                + "curl -O http://Host.ca.com:8080/testapp3/frontend?sleep=1000 \n" + "else \n"
                + "curl -O http://Host.ca.com:8080/testapp1/frontend?sleep=10 \n"
                + "curl -O http://Host.ca.com:8080/testapp2/frontend?sleep=10 \n"
                + "curl -O http://Host.ca.com:8080/testapp3/frontend?sleep=10 \n" + "fi \n"
                + "sleep 15 \n" + "done \n" + "} \n" + "function_curl $i & \n" + "pid=$! \n"
                + "echo \"Started Load Process with $i (pid=$pid)\" \n" + "pids[$pid]=$i \n");

        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(
                "/opt/load" + BuilderBase.LINUX_SEPARATOR + "Load.sh", data).build();
        ExecutionRole execRole =
            new ExecutionRole.Builder("Load_" + "Role")
                .flow(FileModifierFlow.class, createFileFlow).build();
        machine.addRole(execRole);
    }

}
