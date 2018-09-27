package com.ca.apm.tests.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.testbed.ATPerformanceTestbed;
import com.ca.apm.tests.utils.FetchPerformamanceMetrics;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class PerformanceTest extends TasTestNgTest {


    private final Logger log = LoggerFactory.getLogger(getClass());


    private String agcMachine = envProperties
        .getMachineHostnameByRoleId(ATPerformanceTestbed.SA_MASTER_ROLE);
    public String standaloneProvider = envProperties
        .getMachineHostnameByRoleId(ATPerformanceTestbed.SA_PROVIDER_ROLE);
    private String momProviderMachine = envProperties
        .getMachineHostnameByRoleId(ATPerformanceTestbed.MOM_PROVIDER_ROLE);
    private String collectorMachine = envProperties
        .getMachineHostnameByRoleId(ATPerformanceTestbed.COLL_ROLE);
    private final String machine = "tas-scx-n320";
    private final String user = "root";
    private final String password = "Lister@123";
    private final String NUMBER_OF_AGENTS = "1.0"; // number of agents configured in
                                                   // Hammond
    public final int TEST_DURATION = 3600; // in seconds, Jmeter and Hammond run for this
                                           // duration
    private final String RESULTS_LOCATION = "/opt/Results/";
    private final String MAIL_RECEPIENTS = "patpr15@ca.com";

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = ATPerformanceTestbed.class, executeOn = "tas-scx-n320.ca.com"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"performance_test"})
    private void PerformanceMetricsTest_ATEnabled() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        long end;
        startHammondandJmeterLoad();
        startPerfTest();
        // Thread.sleep(TEST_DURATION * 1000 + 60000);
        readResultFilesandMail(" Enabled ");
        end = System.currentTimeMillis();
        System.out.println(" Total Time Taken : " + (end - start) / 1000 + " seconds");

    }

    @Tas(testBeds = @TestBed(name = ATPerformanceTestbed.class, executeOn = "tas-scx-n320.ca.com"), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"performance_test"})
    private void PerformanceMetricsTest_ATDisabled() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        long end;
        disableAT(ATPerformanceTestbed.SA_MASTER_ROLE);
        restartEMandWV(ATPerformanceTestbed.SA_MASTER_ROLE);
        disableAT(ATPerformanceTestbed.COLL_ROLE);
        restartEMandWV(ATPerformanceTestbed.COLL_ROLE);
        disableAT(ATPerformanceTestbed.SA_PROVIDER_ROLE);
        restartEMandWV(ATPerformanceTestbed.SA_PROVIDER_ROLE);
        disableAT(ATPerformanceTestbed.MOM_PROVIDER_ROLE);
        restartEMandWV(ATPerformanceTestbed.MOM_PROVIDER_ROLE);
        Thread.sleep(60000);
        startHammondandJmeterLoad();
        startPerfTest();
        // Thread.sleep(TEST_DURATION * 1000 + 60000);
        readResultFilesandMail(" Disabled ");
        end = System.currentTimeMillis();
        System.out.println(" Total Time Taken : " + (end - start) / 1000 + " seconds");



    }

    private void disableAT(String roleId) {
        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.triage.enabled", "false");
        Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

    }

    private void restartEMandWV(String roleId) {
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner = utilities.createClwUtils(roleId).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, roleId);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }
        startEmAndWebview(roleId);
        log.info(roleId + " restarted.");
    }

    private void startEmAndWebview(String roleId) {
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        checkWebview(roleId);
    }

    public void killWebview(String roleId) {
        try {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("taskkill").args(
                    Arrays.asList("/F", "/T", "/IM", EmRole.Builder.WEBVIEW_EXECUTABLE)).build();
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } catch (Exception e) {
            // swallow all
        }
    }

    private void checkWebview(String roleId) {
        String wvHost = envProperties.getMachineHostnameByRoleId(roleId);
        String wvPort = envProperties.getRolePropertyById(roleId, "wvPort");
        while (!loadPage("http://" + wvHost + ":" + wvPort)) {
            killWebview(roleId);
            runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        }
    }

    private boolean loadPage(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode = con.getResponseCode();
            con.disconnect();
            log.info("Response code from Webview: " + Integer.toString(responseCode));
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                return true;
            }
        } catch (Exception e) {
            // swallow all
        }
        return false;
    }


    private void startPerfTest() throws IOException, InterruptedException {


        HashMap<String, String> map = new HashMap<String, String>() {
            {
                put("AGC", agcMachine);
                put("MOM Provider", momProviderMachine);
                put("MOM Collector", collectorMachine);
                put("Standalone Provider", standaloneProvider);
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        for (final Map.Entry<String, String> entry : map.entrySet()) {

            es.execute(new Runnable() {
                public void run() {
                    try {
                        new FetchPerformamanceMetrics().performanceMetricsCollection(
                            entry.getValue(), entry.getKey(), TEST_DURATION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        es.shutdown();
        while (!es.awaitTermination(120, TimeUnit.MINUTES));



    }

    private void startHammondandJmeterLoad() {

        String standaloneHammondCommand =
            "rm -rf "
                + new String(RESULTS_LOCATION.replace("lts/", "lts"))
                + "; mkdir "
                + RESULTS_LOCATION
                + "; cd /root/Hammond; echo StandaloneHammond; nohup java -Xmx1G -cp hammond.jar com.ca.apm.systemtest.fld.hammond.SmartstorPlayer -i 8DBTC_output_servlet_1/ -c"
                + standaloneProvider + " --scale=" + NUMBER_OF_AGENTS + " --duration="
                + TEST_DURATION + " --prefix=" + standaloneProvider + "-Agent >"
                + standaloneProvider + ".out &";

        String collectorHammondCommand =
            "cd /root/Hammond; echo CollectorHammond; nohup java -Xmx1G -cp hammond.jar com.ca.apm.systemtest.fld.hammond.SmartstorPlayer -i 8DBTC_output_servlet_2/ -c"
                + collectorMachine
                + " --scale="
                + NUMBER_OF_AGENTS
                + " --duration="
                + TEST_DURATION
                + " --prefix="
                + collectorMachine
                + "-Agent >"
                + collectorMachine
                + ".out &";

        new FetchPerformamanceMetrics().jschMetricsConnection(machine, user, password,
            standaloneHammondCommand, "StandaloneHammondLoad");
        new FetchPerformamanceMetrics().jschMetricsConnection(machine, user, password,
            collectorHammondCommand, "CollectorHammondLoad");


        changeServerName();
        String JmeterCommand =
            "cd /root/Hammond/; echo JmeterLoad; nohup timeout " + TEST_DURATION
                + "s sh apache-jmeter-3.0/bin/jmeter.sh -n -t apache-jmeter-3.0/AT_Load.JMX -l "
                + RESULTS_LOCATION + "Jmeter.jtl > jmeter.out &";
        new FetchPerformamanceMetrics().jschMetricsConnection(machine, user, password,
            JmeterCommand, "JmeterLoad");

    }

    // changes Jmeter destination server name in load jmx file
    private void changeServerName()

    {
        String jmxLoadFile;
        try {
            jmxLoadFile =
                FileUtils.readFileToString(new File("/root/Hammond/apache-jmeter-3.0/AT_Load.JMX"));
            String newjmxLoadFile = jmxLoadFile.replaceAll("HostName", agcMachine + ".ca.com");
            FileUtils.writeStringToFile(new File("/root/Hammond/apache-jmeter-3.0/AT_Load.JMX"),
                newjmxLoadFile);
        } catch (IOException e) {
            e.printStackTrace();
            //
        }
    }

    public void readResultFilesandMail(String atStatus) throws IOException {

        ArrayList<String> fileList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        File dir = new File(RESULTS_LOCATION);
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".log"))) {
                fileList.add(file.getName());
            }
        }

        for (String fileName : fileList) {
            String line;
            sb.append("\n");
            sb.append("---------->>>> " + fileName.replace(".log", "") + " <<<<----------");
            try {
                String logFile = RESULTS_LOCATION + fileName;
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                while ((line = br.readLine()) != null) {
                    if (line.contains("buffer memory")) break;
                }
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                //
            }

        }
        File file = new File(RESULTS_LOCATION + "mailFile.log");
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(sb.toString());
        bw.close();

        String mailCommand =
            "mailx -s \"AT Performance Test Results (AT - " + atStatus + ") "
                + (TEST_DURATION / 60) + " Minutes Test\"" + " -a " + RESULTS_LOCATION
                + "StandaloneProvider_PerformanceTestResults*.log" + " -a " + RESULTS_LOCATION
                + "MOMProvider_PerformanceTestResults*.log" + " -a " + RESULTS_LOCATION
                + "MOMCollector_PerformanceTestResults*.log" + " -a " + RESULTS_LOCATION
                + "AGC_PerformanceTestResults*.log " + MAIL_RECEPIENTS
                + " < /opt/Results/mailFile.log";


        new FetchPerformamanceMetrics().jschMetricsConnection(machine, user, password, mailCommand,
            "MailFile");
    }



}
