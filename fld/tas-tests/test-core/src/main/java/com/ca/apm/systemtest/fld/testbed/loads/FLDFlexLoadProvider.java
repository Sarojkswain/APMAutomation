package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ca.apm.systemtest.fld.artifact.FLDWorkflowsArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FlexEchoWebappArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.GroovyAllArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JmeterAmfJmeterExtArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.LoremIpsumArtifact;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.PortForwardingRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Created by haiva01 on 17.2.2016.
 */
public class FLDFlexLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

    public static final String TOMCAT_USERS =
        "<?xml version='1.0' encoding='utf-8'?>\n"
            + "<tomcat-users>\n"
            + "<role rolename=\"tomcat\" />\n"
            + "<role rolename=\"manager-gui\" />\n"
            + "<role rolename=\"manager-script\" />\n"
            + "<role rolename=\"admin-gui\" />\n"
            + "<role rolename=\"admin\" />\n"
            + "<user username=\"tomcat\" password=\"tomcat\" "
            + "roles=\"tomcat,manager-gui,admin-gui,manager-script,admin\" />\n"
            + "</tomcat-users>\n";
    
    public static final String FLEX_ECHO_APP_CONTEXT = "flex-echo-app";
    public static final String TOMCAT_INSTALL_DIR = "C:\\sw\\tomcat";
    public static final String FLEX_AMF_LOAD_JMX = "FLEX AMF load.jmx";
    public static final String FLEX_AMFX_LOAD_JMX = "FLEX AMFX load.jmx";
    
    private static final String FLEX_AMF_LOAD_FRW_PORT = "8008";
    private static final String FLEX_AMFX_LOAD_FRW_PORT = "8009";
    
    private ITestbedMachine jmeterMachine;
    private ITestbedMachine flexWebappMachine;

    private FLDWorkflowsArtifact fldWorkflows;
    
    private ITestbedMachine timMachine = null;
    private String timHostname = null;

    public FLDFlexLoadProvider(ITestbedMachine timMachineId,
        String timHostname) {
        this.timMachine = timMachineId;
        this.timHostname = timHostname;
    }

    private void prepareFlexWebappMachine(ITasResolver tasResolver) {

        WebAppRole<TomcatRole> flexEchoWebapp
            = new WebAppRole.Builder<TomcatRole>(FLEX_ECHO_WEBAPP_ROLE_ID)
            .artifact(new FlexEchoWebappArtifact(tasResolver).createArtifact())
            .cargoDeploy()
            .contextName(FLEX_ECHO_APP_CONTEXT)
            .build();

        TomcatRole tomcatRole =
            new TomcatRole.Builder(TC_ROLE_ID, tasResolver)
                .autoStart()
                .tomcatVersion(TomcatVersion.v80)
                //.installDir(TOMCAT_INSTALL_DIR)
                .addConfigFile("tomcat-users.xml",
                    Arrays.asList(StringUtils.split(TOMCAT_USERS, '\n')))
                .webApp(flexEchoWebapp)
                .customJava(
                    new JavaRole.Builder(JAVA_18_FLEX_MACHINE_ROLE, tasResolver)
                    .version(JavaBinary.WINDOWS_64BIT_JDK_18)
                    .build())
                .build();

        flexWebappMachine.addRole(tomcatRole);
    }

    private JMeterRole buildJmeterRole(ITasResolver tasResolver, String roleId, String jmxFile,
        JavaRole javaRole, String port) {
        
        String targetHost = tasResolver.getHostnameById(TC_ROLE_ID);
        //forward over TIM 5
        String name = "portforward-" + port + "-" + roleId;
        PortForwardingRole pfRole = new PortForwardingRole.Builder(name)
            .listenPort(Integer.valueOf(port)).targetIpAddress(targetHost).targetPort(8080)
            .workDir(name).build();
        timMachine.addRole(pfRole);
        
        Map<String, String> jmeterProperties = new LinkedHashMap<>(4);
        jmeterProperties.put("targetHost", timHostname==null?"localhost":timHostname);// HACK - hostnames may not be properly worked out during the init phase
        jmeterProperties.put("targetPort", port);//8080
        jmeterProperties.put("hostHeader", targetHost);
        jmeterProperties.put("loops", "255000");

        
        return new JMeterRole.Builder(roleId, tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .addJMeterExtension(
                new JmeterAmfJmeterExtArtifact(tasResolver).createArtifact())
            .addJMeterExtension(new GroovyAllArtifact().indy(true).createArtifact())
            .addJMeterExtension(
                new LoremIpsumArtifact(tasResolver).createArtifact())
            .installDir(roleId)
            .logFile("log.txt")
            .jmeterLogFile("jmeter-log.txt")
            .outputFile("output-log.txt")
            .customJava(javaRole)
            //.autoStart()
            .testPlanArchive(fldWorkflows.createArtifact())
            .testPlan("diagrams/loads/" + jmxFile)
            .jmeterProperties(jmeterProperties)
            .build();
    }

    private void prepareJmeterMachine(ITasResolver tasResolver) {

        
        JavaRole javaRole = new JavaRole.Builder(JAVA_18_JMETER_MACHNE_ROLE, tasResolver)
            .version(JavaBinary.WINDOWS_64BIT_JDK_18)
            .build();
        jmeterMachine.addRole(javaRole);

        JMeterRole amfRole = buildJmeterRole(tasResolver, JMETER_ROLE_AMF_ID, FLEX_AMF_LOAD_JMX,
            javaRole, FLEX_AMF_LOAD_FRW_PORT);
        amfRole.after(javaRole);
        jmeterMachine.addRole(amfRole);

        JMeterRole amfxRole = buildJmeterRole(tasResolver, JMETER_ROLE_AMFX_ID, FLEX_AMFX_LOAD_JMX,
            javaRole, FLEX_AMFX_LOAD_FRW_PORT);
        amfxRole.after(javaRole);
        jmeterMachine.addRole(amfxRole);
    }
    
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        jmeterMachine = new TestbedMachine.Builder(JMETER_MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();

        flexWebappMachine =
            new TestbedMachine.Builder(FLEX_ECHO_WEBAPP_MACHINE_ID)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .build();
        
        return Arrays.asList(jmeterMachine, flexWebappMachine);
    }
    

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        fldWorkflows = new FLDWorkflowsArtifact(tasResolver);
        prepareFlexWebappMachine(tasResolver);
        prepareJmeterMachine(tasResolver);
    }
}
