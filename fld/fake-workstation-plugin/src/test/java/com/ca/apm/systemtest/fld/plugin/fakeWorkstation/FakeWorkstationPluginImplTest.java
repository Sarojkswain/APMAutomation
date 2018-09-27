package com.ca.apm.systemtest.fld.plugin.fakeWorkstation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.commons.lang3.StringUtils.isBlank;


@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/fake-workstation-plugin-test-context.xml"})
public class FakeWorkstationPluginImplTest {
    private static final int LOG_CHUNK_SIZE = 512;

    private static Logger log = LoggerFactory.getLogger(FakeWorkstationPluginImplTest.class);

    @Autowired
    private FakeWorkstationPlugin plugin;


    @Before
    public void setUp() throws Exception {
        Path p = Paths.get("target", "conf");
        FileUtils.forceMkdir(p.toFile());
        try (FileOutputStream out = new FileOutputStream("target/conf/agent.conf.json")) {
            p = Paths.get("target", "work");
            String workDir = p.toAbsolutePath().toString();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, Collections.singletonMap("defaultWorkDir", workDir));
        }

        /*
         * Create a stub fakeWorkstation.jar which takes the same args, but isn't downloaded and
         * dependent on external resources
         */
        File outputDir = new File("target/fakeWS");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File file = new File("target/fakeWS/fakeWorkstation.jar");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            ZipEntry entry = new ZipEntry("META-INF/");
            zip.putNextEntry(entry);
            zip.closeEntry();
            entry = new ZipEntry("META-INF/MANIFEST.MF");
            zip.putNextEntry(entry);
            zip.write(
                ("Manifest-Version: 1.0\nCreated-By: FakeWorkstationPluginImplTest\nMain-Class: "
                    + "com.ca.apm.systemtest.fld.plugin.fakeWorkstation.Stub\n")
                    .getBytes("US-ASCII"));
            zip.closeEntry();
            entry = new ZipEntry("com/ca/apm/systemtest/fld/plugin/fakeWorkstation/Stub.class");
            zip.putNextEntry(entry);
            InputStream in = getClass()
                .getResourceAsStream(
                    "/com/ca/apm/systemtest/fld/plugin/fakeWorkstation/Stub.class");
            byte[] buf = FileCopyUtils.copyToByteArray(in);
            zip.write(buf);
            zip.closeEntry();
        }
    }

    @After
    public void tearDown() throws Exception {
        plugin = null;
    }

    //@Test
    public void downloadFW() {
        plugin.downloadFakeWorkstation("99.99.sys-SNAPSHOT", ".", "fakeworstation", ".jar");
    }

    @Test
    public void testFakeWorkstationFunctionality() throws InterruptedException {
        boolean testme = true;
        if (testme) {
            String fakeWorkstationJarPath = "target/fakeWS/fakeWorkstation.jar";

            log.info("Launching fake workstation process...");
            String processId = plugin
                .runQueriesAgainstMOM(fakeWorkstationJarPath,
                    FakeWorkstationPlugin.DEFAULT_JVM_OPTIONS,
                    "localhost", 5001, "Admin", "Admin", 15, 15000, null,
                    "Servlets\\|Servlet_(.*):Average Response Time \\(ms\\)", "(.*)Agent");

            Assert.assertNotNull(processId);
            log.info("...OK. Process id: {}", processId);

            log.info("Waiting for fake workstation process ('{}') for 2.5 seconds...", processId);

            TimeUnit.MILLISECONDS.sleep(2500);

            String logChunk = plugin.getFakeWorkstationLogs(processId, LOG_CHUNK_SIZE);
            log.info("Verifying logs are not empty...");
            Assert.assertNotNull(logChunk);
            Assert.assertFalse(isBlank(logChunk));
            log.info("...OK.");
            log.info("Logs: {}", logChunk);

            log.info("Stopping fake workstation process ('{}')", processId);
            plugin.stopFakeWorkstationProcess(processId);

            TimeUnit.SECONDS.sleep(3);

            log.info("Deleting fake workstation jar...");
            File fakeWorkstationJarFile = new File(fakeWorkstationJarPath);
            Assert.assertTrue(fakeWorkstationJarFile.delete());
            Assert.assertFalse(fakeWorkstationJarFile.exists());
            log.info("...OK.");
        }

    }
}
