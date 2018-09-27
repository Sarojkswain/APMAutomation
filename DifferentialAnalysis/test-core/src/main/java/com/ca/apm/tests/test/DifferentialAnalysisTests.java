package com.ca.apm.tests.test;

import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.DifferentialAnalysisTestbed;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class DifferentialAnalysisTests extends TasTestNgTest {

    private String momHost = envProperties
        .getMachineHostnameByRoleId(DifferentialAnalysisTestbed.MOM_ROLE);
    private String collectorOneHost = envProperties
        .getMachineHostnameByRoleId(DifferentialAnalysisTestbed.COLL_ONEROLE);
    private String collectorTwoHost = envProperties
        .getMachineHostnameByRoleId(DifferentialAnalysisTestbed.COLL_TWOROLE);
    private static String LOAD_FILE_LOCATION = "/opt/load/";
    private static HashMap<String, Integer> metricsValueMap = new HashMap<>();

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = DifferentialAnalysisTestbed.class, executeOn = DifferentialAnalysisTestbed.MOM_MACHINE), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"differential_analysis_tests"})
    private void Differential_Analysis_TestCase() throws InterruptedException,
        ClassNotFoundException, SQLException, JSchException, IOException {

        ArrayList<String> collectors =
            new ArrayList<>(Arrays.asList(collectorOneHost, collectorTwoHost));
        ExecutorService es = Executors.newCachedThreadPool();
        for (final String collector : collectors) {
            es.execute(new Runnable() {
                public void run() {
                    try {
                        startTest(collector);
                    } catch (IOException | InterruptedException | JSchException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        Thread.sleep(1200_000);
        es.shutdown();

        for (String collector : collectors)
            executeCLW(collector);
    }

    private void startTest(String collector) throws InterruptedException, JSchException,
        IOException {
        changeServerName(collector);
        executeCommand(momHost, "root", "Lister@123", "sh " + LOAD_FILE_LOCATION + collector
            + "_Load.sh");
    }

    private void executeCommand(String machine, String user, String password, String command)
        throws JSchException, IOException {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, machine, 22);
        session.setPassword(password);
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String s;
        int i = 0;
        while ((s = br.readLine()) != null) {

            if (command.contains("CLWorkstation.jar") && i > 1) {
                List<String> metricValue = Arrays.asList(s.split("\\s*,\\s*"));
                String key =
                    metricValue.get(3) + "|" + metricValue.get(4) + "|" + metricValue.get(5);

                if ((!metricsValueMap.keySet().contains(key))
                    || (metricsValueMap.keySet().contains(key) && Integer.valueOf(metricValue.get(
                        13).trim()) > metricsValueMap.get(key)))
                    metricsValueMap.put(key, Integer.valueOf(metricValue.get(13)));
            }

            else
                System.out.println(s);
            i++;
        }

    }

    private void executeCLW(String collectorHost) throws JSchException, IOException {
        String command =
            "java -Xmx128M -Dhost="
                + collectorHost
                + ".ca.com -jar /opt/automation/deployed/em/lib/CLWorkstation.jar get historical data from agents matching "
                + "\".*\" and metrics matching \".*Frontends\\|Apps\\|.*\\|URLs\\|Default:Average Response Time \\(ms\\) Variance Intensity\" for past 20 minute with frequency of 15 seconds";

        executeCommand(collectorHost + ".ca.com", "root", "Lister@123", command);

        if (metricsValueMap.keySet().size() > 3) {
            Iterator<String> it = metricsValueMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                System.out.println("Metrics : " + key + "Max Value: " + metricsValueMap.get(key));
                if (metricsValueMap.get(key) < 16)
                    fail(key + "  metric value should be greater than 15");
            }
        }
    }

    private void changeServerName(String host) {
        String loadFile;
        try {
            loadFile = FileUtils.readFileToString(new File(LOAD_FILE_LOCATION + "Load.sh"));
            String newLoadFile = loadFile.replaceAll("Host", host);
            FileUtils.writeStringToFile(new File(LOAD_FILE_LOCATION + host + "_Load.sh"),
                newLoadFile);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to change Web Server name in load file : " + LOAD_FILE_LOCATION
                + "Load.sh");
        }
    }
}
