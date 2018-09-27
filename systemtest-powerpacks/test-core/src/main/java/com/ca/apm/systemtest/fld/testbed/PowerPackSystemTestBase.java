package com.ca.apm.systemtest.fld.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.testng.RunTestNgFlowContext;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;

/**
 * @Author rsssa02
 */
public class PowerPackSystemTestBase implements ITestbedFactory {

    public static final String TESTBED_NAME_PROP = "testBedName";

    public static final String appServerMachine = "STMachine";
    public static final String loadMachine = "STMachine2";
    public static final String dbMachine = "STMachine3";
    public static final String mqServerMachine = "STMachine4";

    public static final String RESULTS_LOC = "c:\\sw\\results";
    public static final String AGENT_LOGS = RESULTS_LOC + "\\agentlogs";
    public static final String INSTALL_DIR = "C:\\sw\\tibco";
    public static final String INSTALL_BASE = TasBuilder.WIN_SOFTWARE_LOC + "/";
    public static final String emailSender = "bocto01@ca.com";
    public static final String emailRecepients = "bocto01@ca.com";
    public static final String smtpServer = "mail.ca.com";
    public static final String MACHINE_TEMPLATE_ID = TEMPLATE_W64;

    public static final String TYPE_PERF_ROLE_ID = "typeperfRole";
    public static final String JMETER_ROLE_ID = "jmeter_role";
    public static final String CLIENT_ID_APPSERVER = "agentclientRole";
    public static final String CLIENT_ID_LOAD = "loadclientRole";
    public static final String EM_ROLE_ID = "emrole";
    public static final String JAVA7_ROLE_ID = "java7";

    protected HashMap<String, String> props = new HashMap<>();

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testBed = new Testbed(getTestBedName());
        return testBed;
    }

    protected void initGenericSystemProperties(ITasResolver tasResolver, ITestbed testBed,
        HashMap<String, String> props, String containerName) {
        // props.put("role_client.test.priority", "1"); //enabling here will overwrite suite level
        props.put("client.dir", INSTALL_BASE + "/client/");
        props.put("results.dir", RESULTS_LOC + "/");
        props.put("role_webapp.container.type", containerName);
        // em & agent
        props.put("legacy.param", "");
        props.put("testbed_em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        props.put("role_em.port", "5001");
        props.put("mockem.port", "5002");
        props
            .put("common.scp.user", testBed.getMachineById(appServerMachine).getLocalSSHUserName());
        props.put("common.scp.password", testBed.getMachineById(appServerMachine)
            .getLocalSSHPassword());
        props.put("browseragent.enabled", "true");
        // misc
        props.put("role_client.jmeter.install.dir", INSTALL_BASE + "jmeter/apache-jmeter-2.11");
        props.put("java_v2.email.feature.enabled", "true");
        props.put("java_v2.email.recipients", emailRecepients.replace(" ", ","));
        props.put("java_v2.email.smtp.server", smtpServer);
        props.put("java_v2.email.sender", emailSender);
        testBed.addProperty(TESTBED_NAME_PROP, getClass().getSimpleName());

    }

    protected void updateTestBedProps(HashMap<String, String> map, ITestbed testBed) {

        HashSet<String> props = new HashSet<String>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            props.add("-D" + entry.getKey() + "=" + entry.getValue());
        }
        testBed.addProperty(RunTestNgFlowContext.CUSTOM_JAVA_ARGS, props);
    }

    @NotNull
    protected String getTestBedName() {
        return PowerPackSystemTestBase.class.getSimpleName();
    }

}
