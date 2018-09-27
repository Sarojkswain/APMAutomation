/**
 * 
 */
package com.ca.apm.saas.standalone;

import static com.ca.apm.saas.standalone.FLDStandAloneTestbed.EM_PORT;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.systemtest.fld.artifact.QATestAppArtifact;
import com.ca.apm.systemtest.fld.artifact.TessTestArtifact;
import com.ca.apm.systemtest.fld.artifact.WurlitzerWebAppArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.Axis2WebappVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.HelloWorld10KVersion;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * @author keyja01
 *
 */
public class FldLoadTomcatProvider implements FldTestbedProvider, FLDStandAloneConstants {
    private ITestbedMachine[] tomcats;
    private int nextRoleId = 0;
    private static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        
        tomcats = new TestbedMachine[TOMCAT_MACHINE_IDS.length]; 
        
        // create our four testbeds
        for (int i = 0; i < TOMCAT_MACHINE_IDS.length; i++) {
            String machineId = TOMCAT_MACHINE_IDS[i];
            tomcats[i] = new TestbedMachine.Builder(machineId)
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .build();
        }
        
        return Arrays.asList(tomcats);
    }
    

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        initTomcats(testbed, tasResolver);
    }

    private void initTomcats(ITestbed testbed, ITasResolver tasResolver) {
        
        TomcatRole tomcat6Role = configureTomcat6(tasResolver, tomcats[0]);
        TomcatRole tomcat7Role = configureTomcat7(tasResolver, tomcats[0]);
        TomcatRole tomcat9080Role = configureTomcat9080(tasResolver, tomcats[1]);
        TomcatRole tomcat9081Role = configureTomcat9081(tasResolver, tomcats[1]);
        
//        javaRole1.before(tomcat6Role, tomcat7Role);
//        javaRole2.before(tomcat9080Role, tomcat9081Role);
//        
        tomcat7Role.before(tomcat6Role);
        tomcat9081Role.before(tomcat9080Role);
//        tomcat9080Role.before(tomcat9081Role);

        testbed.addMachines(Arrays.asList(tomcats));
    }

    
    private WebAppRole<TomcatRole> axis2Role(String roleId) {

        return new WebAppRole.Builder<TomcatRole>(roleId)
            .artifact(Axis2WebappVersion.v154.getArtifact())
            .cargoDeploy()
            .contextName("axis2")
            .build();
    }
    
    
    private WebAppRole<TomcatRole> wurlitzerRole(ITasResolver resolver, String roleId) {
        WurlitzerWebAppArtifact wurlizterWeb = new WurlitzerWebAppArtifact(resolver);

        return new WebAppRole.Builder<TomcatRole>(roleId)
            .artifact(wurlizterWeb.createArtifact())
            .cargoDeploy()
            .contextName("wurlitzer")
            .build();
    }
    
    
    private WebAppRole<TomcatRole> qaTestAppRole(ITasResolver resolver, String roleId) {
        return new WebAppRole.Builder<TomcatRole>(roleId)
            .artifact(new QATestAppArtifact("jvm7-tomcat", resolver).createArtifact())
            .cargoDeploy()
            .contextName("QATestApp")
            .build();
    }
    
    private TomcatRole configureTomcat9081(ITasResolver tasResolver, ITestbedMachine machine) {
        WebAppRole<TomcatRole> axis2Role = axis2Role(TOMCAT_9081_AXIS2_ROLE_ID);
        WebAppRole<TomcatRole> wurlitzerRole = wurlitzerRole(tasResolver, TOMCAT_9081_WURLITZER_ROLE_ID);
        
        String javaDir = getJavaDir(JAVA_VERSION) + "-1";
        JavaRole javaRole = javaRole(javaDir, JAVA_VERSION, tasResolver);
        
        TomcatRole tomcat9081Role = new TomcatRole.Builder(TOMCAT_9081_ROLE_ID, tasResolver)
            .additionalVMOptions(Arrays.asList("-Xmx1000m", "-XX:MaxPermSize=256m",
                "-Dopenejb.validation.output.level=VERBOSE", "-server"))
            .tomcatArtifact(new DefaultArtifact("com.ca.apm.binaries", "tomcat", "zip", "6.0.45"), TomcatVersion.v60)
            .webApp(axis2Role)
            .webApp(wurlitzerRole)
            .customJava(javaRole)
            .jdkHomeDir(javaDir)
            .autoStart()
            .installDir("c:\\tomcat9081")
            .tomcatAjpPort(9009)
            .tomcatSslPort(9443)
            .tomcatCatalinaPort(9080)
            .tomcatServerPort(9005)
            .build();

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.remoteagentdynamicinstrumentation.enabled", "true");
        additionalProperties.put("introscope.agent.hostName", TOMCAT_HOST_NAME);
        
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_9081_AGENT_ROLE_ID, tasResolver)
            .webAppServer(tomcat9081Role)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .overrideEM(tasResolver.getHostnameById(EM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .customName(TOMCAT_AGENT_9081)
            .additionalProperties(additionalProperties)
            .build();
        
        JMeterRole jmeter = new JMeterRole.Builder(JMETER_LOAD4, tasResolver).jmxFile("Tomcat_fld_Tomcat_9080_Axis2.jmx").build();
        machine.addRole(javaRole, axis2Role, wurlitzerRole, tomcat9081Role, tomcatAgentRole, jmeter);
        
        return tomcat9081Role;
    }


    private TomcatRole configureTomcat9080(ITasResolver tasResolver, ITestbedMachine machine) {
        WebAppRole<TomcatRole> axis2Role = axis2Role(TOMCAT_9080_AXIS2_ROLE_ID);
        WebAppRole<TomcatRole> wurlitzerRole = wurlitzerRole(tasResolver, TOMCAT_9080_WURLITZER_ROLE_ID);
        
        String javaDir = getJavaDir(JAVA_VERSION) + "-2";
        JavaRole javaRole = javaRole(javaDir, JAVA_VERSION, tasResolver);
        
        TomcatRole tomcat9080Role = new TomcatRole.Builder(TOMCAT_9080_ROLE_ID, tasResolver)
            .additionalVMOptions(Arrays.asList("-Xmx1000m", "-XX:MaxPermSize=256m",
                "-Dopenejb.validation.output.level=VERBOSE", "-server"))
            .tomcatArtifact(new DefaultArtifact("com.ca.apm.binaries", "tomcat", "zip", "6.0.45"), TomcatVersion.v60)
            .webApp(axis2Role)
            .webApp(wurlitzerRole)
            .customJava(javaRole)
            .jdkHomeDir(javaDir)
            .installDir("c:\\tomcat9080")
            .autoStart()
            .tomcatAjpPort(8009)
            .tomcatSslPort(8443)
            .tomcatCatalinaPort(8080)
            .tomcatServerPort(8005)
            .build();

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.remoteagentdynamicinstrumentation.enabled", "true");
        additionalProperties.put("introscope.agent.hostName", TOMCAT_HOST_NAME);
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_9080_AGENT_ROLE_ID, tasResolver)
            .webAppServer(tomcat9080Role)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .overrideEM(tasResolver.getHostnameById(EM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .customName(TOMCAT_AGENT_9080)
            .additionalProperties(additionalProperties)
            .build();
        
        JMeterRole jmeter = new JMeterRole.Builder(JMETER_LOAD3, tasResolver).installJmeter(true).jmxFile("Tomcat_fld_Tomcat_8080_Axis2.jmx").build();
        machine.addRole(javaRole, axis2Role, wurlitzerRole, tomcat9080Role, tomcatAgentRole, jmeter);
        
        return tomcat9080Role;
    }
    
    
    private TomcatRole configureTomcat7(ITasResolver tasResolver, ITestbedMachine machine) {
        WebAppRole<TomcatRole> axis2Role = axis2Role(TOMCAT_7_AXIS2_ROLE_ID);
        WebAppRole<TomcatRole> qaTestAppRole = qaTestAppRole(tasResolver, TOMCAT_7_QATESTAPP_ROLE_ID);
        
        // install the HelloWorld10k test app for dynamic instrumentation
        WebAppRole<TomcatRole> helloWorldRole = new WebAppRole.Builder<TomcatRole>(TOMCAT_7_ROLE_ID + "_HelloWorld10K")
            .artifact(HelloWorld10KVersion.v1_0)
            .contextName("HelloWorld10k")
            .cargoDeploy()
            .build();

        
        // tomcat7
        String javaDir = getJavaDir(JAVA_VERSION) + "-3";
        JavaRole javaRole = javaRole(javaDir, JAVA_VERSION, tasResolver);
        
        TomcatRole tomcat7Role = new TomcatRole.Builder(TOMCAT_7_ROLE_ID, tasResolver)
            .additionalVMOptions(Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server"))
            .tomcatVersion(TomcatVersion.v70)
            .webApp(axis2Role)
            .webApp(qaTestAppRole)
            .webApp(helloWorldRole)
            .customJava(javaRole)
            .jdkHomeDir(javaDir)
            .autoStart()
            .installDir("c:\\tomcat7")
            .tomcatAjpPort(9009)
            .tomcatSslPort(9443)
            .tomcatCatalinaPort(9080)
            .tomcatServerPort(9005)
            .build();
//        tomcat7Role.after(tomcat6Role);

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.remoteagentdynamicinstrumentation.enabled", "true");
        additionalProperties.put("introscope.agent.hostName", TOMCAT_HOST_NAME);
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_7_AGENT_ROLE_ID, tasResolver)
            .webAppServer(tomcat7Role)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .overrideEM(tasResolver.getHostnameById(EM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .customName(TOMCAT7_AGENT)
            .additionalProperties(additionalProperties)
            .build();
        JMeterRole jmeter = new JMeterRole.Builder(JMETER_LOAD2, tasResolver).jmxFile("Tomcat_fld_Tomcat7_9080_Axis2.jmx").build();
        
        machine.addRole(javaRole, tomcat7Role, axis2Role, qaTestAppRole, tomcatAgentRole, helloWorldRole, jmeter);
        
        return tomcat7Role;
    }


    private TomcatRole configureTomcat6(ITasResolver tasResolver, ITestbedMachine machine) {
        WebAppRole<TomcatRole> axis2Role = axis2Role(TOMCAT_6_AXIS2_ROLE_ID);
        
        WebAppRole<TomcatRole> tessTestRole = new WebAppRole.Builder<TomcatRole>(TOMCAT_6_TESTAPP_ROLE_ID)
                .artifact(new TessTestArtifact(tasResolver).createArtifact())
                .cargoDeploy()
                .contextName("tesstest")
                .build();
        
        WebAppRole<TomcatRole> qaTestAppRole = qaTestAppRole(tasResolver, TOMCAT_6_QATESTAPP_ROLE_ID);
        
        String javaDir = getJavaDir(JAVA_VERSION) + "-4";
        JavaRole javaRole = javaRole(javaDir, JAVA_VERSION, tasResolver);
        
        TomcatRole tomcat6Role = new TomcatRole.Builder(TOMCAT_6_ROLE_ID, tasResolver)
            .additionalVMOptions(Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server",
                        "-Dcom.wily.introscope.agent.agentName=Tomcat6"))
            .tomcatArtifact(new DefaultArtifact("com.ca.apm.binaries", "tomcat", "zip", "6.0.45"), TomcatVersion.v60)
            .webApp(axis2Role)
            .webApp(qaTestAppRole)
            .webApp(tessTestRole)
            .customJava(javaRole)
            .jdkHomeDir(javaDir)
            .installDir("c:\\tomcat6")
            .autoStart()
            .tomcatAjpPort(8009)
            .tomcatSslPort(8443)
            .tomcatCatalinaPort(8080)
            .tomcatServerPort(8005)
            .build();

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.hostName", TOMCAT_HOST_NAME);
        IRole tomcatAgentRole = new AgentRole.Builder(TOMCAT_6_AGENT_ROLE_ID, tasResolver)
            .webAppServer(tomcat6Role)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .overrideEM(tasResolver.getHostnameById(EM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .customName(TOMCAT6_AGENT)
            .additionalProperties(additionalProperties)
            .build();
        JMeterRole jmeter = new JMeterRole.Builder(JMETER_LOAD1, tasResolver).installJmeter(true).jmxFile("Tomcat_fld_Tomcat6_8080_Axis2.jmx").build();

        machine.addRole(javaRole, axis2Role, qaTestAppRole, tomcat6Role, tomcatAgentRole, tessTestRole, jmeter);
        
        return tomcat6Role;
    }
    

    private JavaRole javaRole(String javaDir, JavaBinary version, ITasResolver tasResolver) {

        return new JavaRole.Builder("javaRole" + nextRoleId++, tasResolver)
            .dir(javaDir)
            .version(version)
            .build();
    }

}
