package com.ca.apm.systemtest.fld.plugin.weblogic;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.ca.apm.systemtest.fld.plugin.run.RunPlugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestWeblogicPlugin {
    Logger log = LoggerFactory.getLogger(TestWeblogicPlugin.class);

    WeblogicPlugin weblogicPlugin;
    RunPlugin runPlugin;
    AbstractApplicationContext ctx;

    @BeforeTest
    public void setup() {
        cleanup();
        ctx = new ClassPathXmlApplicationContext("weblogic-plugin-test-context.xml");
        weblogicPlugin = ctx.getBean(WeblogicPlugin.class);
        runPlugin = ctx.getBean(RunPlugin.class);
    }

    @AfterTest
    private void cleanup() {
        File logDir = new File("run-logs");
        if (logDir.exists()) {
            FileUtils.deleteQuietly(logDir);
        }

        File logFile = new File("logfiles.txt");
        FileUtils.deleteQuietly(logFile);

        if (ctx != null) {
            ctx.close();
        }
    }

    // @Test
    public void testInstall() throws InterruptedException {
        String startInstl1 = weblogicPlugin.startInstallation("inst1");
        String startInstl2 = weblogicPlugin.startInstallation("inst2");
        waitFor(startInstl1, startInstl2);
        long exStartInstl1 = runPlugin.exitValue(startInstl1);
        long exStartInstl2 = runPlugin.exitValue(startInstl2);

        assertEquals(exStartInstl1, 0);
        assertEquals(exStartInstl2, 0);
    }

    // @Test(dependsOnMethods="testInstall", alwaysRun=true)
    public void testCreateInstance() throws InterruptedException {
        String createInst1 = weblogicPlugin.createInstance("inst1", "domA", 7001);
        String createInst2 = weblogicPlugin.createInstance("inst1", "domB", 7011);
        waitFor(createInst1, createInst2);
        long exCreateInst1 = runPlugin.exitValue(createInst1);
        long exCreateInst2 = runPlugin.exitValue(createInst2);

        assertEquals(exCreateInst1, 0);
        assertEquals(exCreateInst2, 0);
    }

    String startInst1;
    String startInst2;

    // @Test(dependsOnMethods="testCreateInstance", alwaysRun=true)
    public void testStartInstance() throws InterruptedException {
        startInst1 = weblogicPlugin.startInstance("inst1", "domA");
        startInst2 = weblogicPlugin.startInstance("inst1", "domB");
        Thread.sleep(15000);
        long exStartInst1 = runPlugin.exitValue(startInst1);
        long exStartInst2 = runPlugin.exitValue(startInst2);
        assertEquals(exStartInst1, RunPlugin.STILL_RUNNING);
        assertEquals(exStartInst2, RunPlugin.STILL_RUNNING);
    }

    // @Test(dependsOnMethods="testStartInstance", alwaysRun=true)
    public void testCreateDs() throws InterruptedException {
        String createDs = weblogicPlugin.createDatasource("inst1", "domA", 7001, "pgresTest1",
            "jdbc:postgresql://localhost:5432/test1", "org.postgresql.Driver", "user1", "pass1",
            "SELECT 1");
        waitFor(createDs);
    }

    // @Test(dependsOnMethods="testStartInstance", alwaysRun=true)
    public void testCreateJms() throws InterruptedException {
        String createJms = weblogicPlugin
            .createJms("inst1", "domA", 7001, "connFactory1", "queueA,queueB", "topicA,topicB");
        waitFor(createJms);
    }

    // @Test(dependsOnMethods="testStartInstance", alwaysRun=true)
    public void testDeployApp() throws InterruptedException {
        String createJms = weblogicPlugin.deployApp("inst1", "domA", 7001, "servletTest",
            "http://isl-dsdc.ca.com/artifactory/apm-third-party-local/com/ca/apm/coda/qatestapp"
                + "/dist/external/ServletsTest.war");
        waitFor(createJms);
    }

    // @Test(dependsOnMethods="testDeployApp", alwaysRun=true)
    public void testStopInstance() throws InterruptedException {
        String stopInst1 = weblogicPlugin.stopInstance("inst1", "domA");
        Thread.sleep(2000); // Wait 2 second
        String stopInst2 = weblogicPlugin.stopInstance("inst1", "domB");
        waitFor(stopInst1, stopInst2);
        waitFor(startInst1, startInst2);
        long exStartInst1 = runPlugin.exitValue(startInst1);
        long exStartInst2 = runPlugin.exitValue(startInst2);
        long exStopInst1 = runPlugin.exitValue(stopInst1);
        long exStopInst2 = runPlugin.exitValue(stopInst2);

        assertEquals(exStartInst1, 0);
        assertEquals(exStartInst2, 0);
        assertEquals(exStopInst1, 0);
        assertEquals(exStopInst2, 0);
    }

    String[] instList = {"inst2", "inst1,domB,7011", "inst1,domA,7001"};

    // @Test(dependsOnMethods="testStopInstance", alwaysRun=true)
    public void testListInstances() throws InterruptedException {
        List<String> listInstances = weblogicPlugin.listInstances();
        assertEquals(listInstances.size(), 3, "Number of instances");
        for (String str : listInstances) {
            boolean match = false;
            for (String i : instList) {
                if (str.equals(i)) {
                    match = true;
                    break;
                }
            }
            assertTrue(match, "Wrong instance " + str);
        }
    }

    // @Test(dependsOnMethods="testListInstances")
    public void testUninstall() throws InterruptedException {
        String uninstall1 = weblogicPlugin.startUninstallation("inst1");
        String uninstall2 = weblogicPlugin.startUninstallation("inst2");
        waitFor(uninstall1, uninstall2);
        String tmpDir = System.getProperty("java.io.tmpdir");
        File f1 = new File(tmpDir + "bea/webl10-3_inst1");
        File f2 = new File(tmpDir + "bea/webl10-3_inst2");
        assertFalse(f1.exists(), "Installation 'inst1' not removed");
        assertFalse(f2.exists(), "Installation 'inst2' not removed");
    }

    private void waitFor(String... procIds) throws InterruptedException {
        for (String id : procIds) {
            if (id.startsWith("ERROR:")) {
                log.error("Process error '{}'", id);
            }
        }

        boolean atLeastOne = true;
        while (atLeastOne) {
            Thread.sleep(5000);
            log.info("Date {}", new Date());
            atLeastOne = false;
            for (String id : procIds) {
                if (!id.startsWith("ERROR:")) {
                    long idResult = runPlugin.exitValue(id);
                    log.info("\tProcess {} -> {}", id,
                        idResult == RunPlugin.STILL_RUNNING ? "STILL_RUNNING" : idResult);
                    if (idResult == RunPlugin.STILL_RUNNING) {
                        atLeastOne = true;
                    }
                }
            }
        }
    }
}
