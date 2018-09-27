package com.ca.apm.tests.testbed.devel;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.tests.artifact.JmeterScriptsVersion;
import com.ca.apm.tests.role.CautlRole;
import com.ca.apm.tests.role.FakeWorkstationRole;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.apm.tests.role.WebViewLoadRole;
import com.ca.apm.tests.role.WurlitzerRole;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author jirji01
 */
@TestBedDefinition
public class StopLoadTestbed implements ITestbedFactory {

    private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m",
        "-Xmx2048m", "-Dappmap.user=admin", "-Dappmap.token=f47ac10b-58cc-4372-a567-0e02b2c3d479");

    private static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms256m", "-Xmx256m");

    public static final String EM_ROLE_ID = "emRoleId";
    public static final String EM_TEST_MACHINE_ID = "emTestMachineId";
    public static final String LOAD_TEST_MACHINE_ID = "loadTestMachineId";
    public static final String FAKE_WORKSTATION_ROLE_ID = "fwRoleId";
    public static final String FAKE_WORKSTATION_CAUTL_ROLE_ID = "fwCautlRoleId";
    public static final String JMETER_ROLE_ID = "jmeterRoleId";
    public static final String JMETER_SCRIPT_ROLE_ID = "jmeterScriptRoleId";
    public static final String WEB_VIEW_LOAD_ROLE = "webViewRoleId";
    public static final String WURLITZER_ROLE_ID = "wurlitzerRoleId";

    private static final String EM_VERSION = "10.2.0-SNAPSHOT";
    private static final int RUN_DURATION_SECONDS = 300;

    @Override
    public ITestbed create(ITasResolver resolver) {

        Testbed testbed = new Testbed(getClass().getSimpleName());

        // collector machine
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, resolver).version(EM_VERSION)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).noTimeout()
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION).nostartWV()
                .emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION).build();
        ITestbedMachine emWindowsMachine =
            TestBedUtils.createWindowsMachine(EM_TEST_MACHINE_ID, TEMPLATE_W64, emRole);
        testbed.addMachine(emWindowsMachine);

        // setup webview agent on WV
        Map<String, String> propsMap = new HashMap<String, String>();
        String emHost = resolver.getHostnameById(EM_ROLE_ID);
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", emHost);
        propsMap.put("agentManager.url.1", emHost + ":" + emRole.getEmPort());

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(
                emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR)
                    + "/product/webview/agent/wily/core/config/IntroscopeAgent.profile", propsMap)
                .build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(EM_ROLE_ID + "_setupWVAgentProfile", resolver).runFlow(
                ConfigureFlow.class, ctx).build();
        setWVAgent.after(emRole);
        emWindowsMachine.addRole(setWVAgent);

        ExecutionRole startRole =
            new ExecutionRole.Builder(EM_ROLE_ID + "_startWebView").asyncCommand(
                emRole.getWvRunCommandFlowContext()).build();
        startRole.after(setWVAgent);
        emWindowsMachine.addRole(startRole);

        // load machine
        FakeWorkstationRole fwRole =
            new FakeWorkstationRole.Builder(FAKE_WORKSTATION_ROLE_ID, resolver).user("Admin")
                .host(emHost).port(5001).historicalQuery().liveQuery().resolution(15)
                .agent(".*DatabaseAgent_%2").metric("EJB\\|.*:Average Response Time \\(ms\\)")
                .runDuration(RUN_DURATION_SECONDS).build();
        CautlRole fwCautlRole =
            new CautlRole.Builder(FAKE_WORKSTATION_CAUTL_ROLE_ID, resolver).executedRole(fwRole)
                .build();

        Map<String, String> params = new HashMap<>();
        params.put("testDurationInSeconds", Integer.toString(RUN_DURATION_SECONDS));
        params.put("appServerHost", emHost);
        params.put("csvFolder", "\"Generated Files\"");

        JMeterRole jmeterRole =
            new JMeterRole.Builder(JMETER_ROLE_ID, resolver)
                .deploySourcesLocation("c:\\sw\\jmeter")
                .scriptFilePath("c:\\sw\\jmeter\\fld-jmeter-scripts\\TeamCenterWithCode.jmx")
                .outputJtlFile("c:\\sw\\jmeter\\some.jtl")
                .outputLogFile("c:\\sw\\jmeter\\some.log").params(params).build();
        UniversalRole jMeterScriptRole =
            new UniversalRole.Builder(JMETER_SCRIPT_ROLE_ID, resolver).unpack(
                JmeterScriptsVersion.v10_3.getArtifact(), "c:\\sw\\jmeter").build();

        WurlitzerRole wurlitzerRole =
            new WurlitzerRole.Builder(WURLITZER_ROLE_ID, resolver)
                .wurlitzerMachine(resolver.getHostnameById(WURLITZER_ROLE_ID))
                .targetMachine(emHost).runDuration(RUN_DURATION_SECONDS * 60)
                .version(Version.SNAPSHOT_DEV_99_99)
                .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                .antScriptArgs("20-agents-150-apps-15-backends-1-frontends")
                .terminateOnMatch("Connected to COLLECTOR").build();

        WebViewLoadRole webViewLoadRole =
            (new WebViewLoadRole.Builder(WEB_VIEW_LOAD_ROLE, resolver))
                .webViewServerHost(emHost)
                .webViewServerPort(8082)
                .webViewCredentials("admin", "")
                .workDir(TasBuilder.WIN_SOFTWARE_LOC + "webview-load")
                .openWebViewUrl(
                    "http://" + resolver.getHostnameById(EM_ROLE_ID)
                        + ":8082/#console;db=EM+Capacity;dn=SuperDomain;mm=Supportability;tr=0")
                .build();

        ITestbedMachine loadWindowsMachine =
            TestBedUtils.createWindowsMachine(LOAD_TEST_MACHINE_ID, TEMPLATE_W64, fwRole,
                fwCautlRole, jmeterRole, jMeterScriptRole, wurlitzerRole, webViewLoadRole);
        testbed.addMachine(loadWindowsMachine);

        return testbed;
    }
}
