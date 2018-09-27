package com.ca.apm.systemtest.fld.plugin.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/plugin-api-test-context.xml"})
public class TestRunPlugin {
    Logger log = LoggerFactory.getLogger(TestRunPlugin.class);

    String scriptName = "runtst.sh";
    String realScriptName = "runimport.sh";

    @Autowired
    RunPlugin runPlugin;

    @Before
    public void setup() {
        cleanup();
    }

    @After
    public void cleanup() {
        File logDir = new File("run-logs");
        if (!FileUtils.deleteQuietly(logDir)) {
            log.warn("Failed to delete {}", logDir.getAbsolutePath());
        }

        File logFile = new File("logfiles.txt");
        if (!FileUtils.deleteQuietly(logFile)) {
            log.warn("Failed to delete {}", logFile.getAbsolutePath());
        }

        if (StringUtils.isNotEmpty(scriptName)) {
            File extract1 = new File(
                System.getProperty("java.io.tmpdir") + File.separatorChar + scriptName);
            if (!FileUtils.deleteQuietly(extract1)) {
                log.warn("Failed to delete {}", extract1.getAbsolutePath());
            }
        }
    }

    @Test
    public void dirTest() {
        String handle = runPlugin.runProcess("dirTest", new HashMap<String, Object>(10));
        long exitValue;
        for (int i = 0; i != 20; ++i) {
            exitValue = runPlugin.exitValue(handle);
            assertNotEquals(exitValue, RunPlugin.NO_SUCH_PROCESS);
            if (exitValue != RunPlugin.STILL_RUNNING) {
                log.info("Process exited with exit value {}", exitValue);
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("Interrupted!", e);
            }
        }
        log.info("dir test output:\n{}", runPlugin.getLog(handle, 4));
    }

    //	@Test
    public void testRunPlugin() throws InterruptedException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("produceLines", 5);
        String procId = runPlugin.runProcess("simpleTest", params);
        Thread.sleep(100);
        final String out1 = runPlugin.getLog(procId, 1);
        Thread.sleep(2000);
        runPlugin.stopProcess(procId);
        Thread.sleep(6000); // retain time
        final String out2 = runPlugin.getLog(procId, 1); // this should clean up all resources

        String expectStart = "Lines: 500 Sleep: 5";
        assertEquals(out1.substring(0, expectStart.length()), expectStart);
        boolean isMsg = false;
        for (String s : ProduceLog.LOG_LEVELS) {
            if (out2.startsWith(s)) {
                isMsg = true;
                break;
            }
        }
        assertTrue("Wrong line start alignment in getLog()", isMsg);

        // File logFile = new File("logfiles.txt");
        // assertEquals(logFile.length(), 0); // Crashing on windows
        File logDir = new File("run-logs");
        assertEquals(logDir.listFiles().length, 0);
    }

    //	@Test
    public void testLogDel() throws InterruptedException, IOException {
        String procId1 = runPlugin.runProcess("produceLog", null);
        String procId2 = runPlugin.runProcess("produceLog", null);
        String procId3 = runPlugin.runProcess("produceLog", null);
        Thread.sleep(12000); // 5000 produce, 5000 retain time, than try to start new
        String procId4 = runPlugin.runProcess("produceLog", null);
        Thread.sleep(4000);

        String proc1Log = runPlugin.getLog(procId1, 1);
        String proc2Log = runPlugin.getLog(procId2, 1);
        String proc3Log = runPlugin.getLog(procId3, 1);
        String proc4Log = runPlugin.getLog(procId4, 1);
        runPlugin.stopProcess(procId4);
        assertEquals(proc1Log.substring(0, 6), "ERROR:");
        assertEquals(proc2Log.substring(0, 6), "ERROR:");
        assertEquals(proc3Log.substring(0, 6), "ERROR:");
        assertNotEquals(proc4Log.substring(0, 6), "ERROR:");
    }

    // @Test
    public void testExtractScript() throws InterruptedException, IOException {
        String exId = runPlugin.runProcess("extractTest", null);
        Thread.sleep(2000);
        String tmpDir = System.getProperty("java.io.tmpdir");
        File extract = new File(tmpDir + File.separatorChar + scriptName);
        assertTrue("File " + scriptName + " was not extract into " + tmpDir, extract.isFile());

        String result = runPlugin.getLog(exId, 1);
        assertTrue("Script was NOT run", result.startsWith("INSIDE SCRIPT"));
    }

    // @Test
    public void testRealScript() throws InterruptedException, IOException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("lastversion", "/tmp/qq");
        params.put("installdir", "/tmp");
        params.put("dbhost", "localhost");
        params.put("dbname", "simm");
        params.put("dbport", "5432");
        params.put("databasetype", "postgres");
        params.put("dbuser", "simm");
        params.put("dbpassword", "simmpass");
        params.put("importfile", "/tmp/aa.sql");
        params.put("dbserviceuser", "postgres");
        params.put("dbservicepwd", "postG");
        params.put("is64Bit", "true");

        String exId = runPlugin.runProcess("runRealImportScript", params);

        Thread.sleep(2000);
        String tmpDir = System.getProperty("java.io.tmpdir");
        File extract = new File(tmpDir + File.separatorChar + realScriptName);
        assertTrue("File " + realScriptName + " was not extract into " + tmpDir, extract.isFile());

        String result = runPlugin.getLog(exId, 1);
        assertTrue("Script was NOT run", result.contains("Check for latest version"));
    }
}
