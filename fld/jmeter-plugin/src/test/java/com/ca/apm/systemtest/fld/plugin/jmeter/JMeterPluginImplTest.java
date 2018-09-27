package com.ca.apm.systemtest.fld.plugin.jmeter;

import static com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin.JMETER_ARTIFACT_DEFAULT_VERSION;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JMeterPluginImplTest {
    private static final String BUILT_IN_SCENARIO = "/diagrams/alm432913/HelloWorldServlet-test.jmx";
    private JMeterPluginImpl plugin;
    private ArtifactoryDownloadMethod dm;
    private static final Logger log = LoggerFactory.getLogger(JMeterPluginImplTest.class);

    private boolean enable() {
        return false;
    }

    @Before
    public void setUp() throws Exception {
        dm = Mockito.mock(ArtifactoryDownloadMethod.class);
        plugin = new JMeterPluginImpl();
        plugin.dm = dm;
    }

    @After
    public void tearDown() throws Exception {
        plugin.deleteTempDir();
        plugin = null;
    }

    @Test
    public void testPlugin() throws Exception {
        final ArgumentCaptor<String> groupCapture = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> downloadJMeterZipAnswer = new Answer<ArtifactFetchResult>() {

            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                String group = groupCapture.getValue();
                ArtifactFetchResult result = new ArtifactFetchResult();
                
                if ("com.ca.apm.binaries".equals(group)) {
                    File tmpFile = File.createTempFile("jmeterDistro", ".zip");
                    tmpFile.deleteOnExit();
                    ZipBuilder zip = new ZipBuilder(tmpFile);
                    zip.addFile("foo.txt", "Foo me, Amadeus");
                    zip.addFolder("apache-jmeter-" + JMETER_ARTIFACT_DEFAULT_VERSION);
                    zip.addFolder("apache-jmeter-" + JMETER_ARTIFACT_DEFAULT_VERSION + "/lib");
                    zip.addFolder("apache-jmeter-" + JMETER_ARTIFACT_DEFAULT_VERSION + "/lib/ext");
                    zip.close();
                    result.setFile(tmpFile);
                } else if ("com.ca.apm.systemtest.fld.flex".equals(group)) {
                    File tmpFile = File.createTempFile("jmeterExt", ".jar");
                    ZipBuilder zip = new ZipBuilder(tmpFile);
                    zip.addFile("foo.txt", "Foo me, Amadeus");
                    zip.close();
                    result.setFile(tmpFile);
                }
                
                
                return result;
            }
            
        };
        doAnswer(downloadJMeterZipAnswer).when(dm).fetchTempArtifact(anyString(), groupCapture.capture(), anyString(), anyString(), (String) any(), anyString());
        
        assertNotNull(plugin);

        log.info("about do download jMeter distribution ZIP file");
        String jMeterZip = plugin.downloadJMeter(null);
        assertNotNull(jMeterZip);
        log.info("about to unzip jMeter distribution ZIP file");
        String jMeterDir = plugin.unzipJMeterZip();
        assertNotNull(jMeterDir);
        log.info("setting built in scenario {}", BUILT_IN_SCENARIO);
        plugin.setBuiltinScenario(BUILT_IN_SCENARIO);
        assertEquals(plugin.getScenario(), BUILT_IN_SCENARIO);
        assertEquals(plugin.getScenarioType(), JMeterPlugin.ScenarioType.BUILT_IN);
        plugin.deployExtension(jMeterDir, JMeterPluginImpl.JMETER_ARTIFACTORY_URL,
            "com.ca.apm.systemtest.fld.flex", "jmeter-amf", "99.99.aquarius-SNAPSHOT",
            "jar-with-dependencies", "jar");

        if (enable()) {
            Map<String, String> props = new TreeMap<>();
            props.put("numberOfServletsToAccess", "1000");
            props.put("helloWorld10kHost", "localhost");
            props.put("helloWorld10kHostPort", "8080");
            props.put("helloWorld10kPath", "/HelloWorld10k");
            props.put("concurrency", "20");
            // props.put("logDir",".");
            log.info("executing jMeter with these parameters: {}", props);
            String task = plugin.execute(props);
            assertNotNull(task);
            assertTrue(!task.isEmpty());
            boolean state = plugin.isRunning(task);
            log.info("task state: {}", state);
            String lastResult = plugin.getLastResult(task);
            log.info("last result: {}", lastResult);
        }
    }

    @Test
    public void testGetLastResult() throws Exception {
        assertNotNull(plugin);

        String log =
            "2015/07/02 09:03:00 INFO  - jmeter.util.JMeterUtils: Setting Locale to en_US \r\n"
                + "2015/07/02 09:03:00 INFO  - jmeter.JMeter: Loading user properties from: C:\\Users\\jirji01\\AppData\\Local\\Temp\\jmeter-plugin548322737123100808\\apache-jmeter-2.12\\bin\\user.properties \r\n"
                + "2015/07/02 09:03:00 INFO  - jmeter.JMeter: Loading system properties from: C:\\Users\\jirji01\\AppData\\Local\\Temp\\jmeter-plugin548322737123100808\\apache-jmeter-2.12\\bin\\system.properties \r\n"
                + "2015/07/02 09:03:00 FATAL - jmeter.JMeter: An error occurred:  java.lang.IllegalArgumentException: Unknown arg:  -JhelloWorld10kHost\r\n"
                + "    at org.apache.jmeter.JMeter.initializeProperties(JMeter.java:602)\r\n"
                + "    at org.apache.jmeter.JMeter.start(JMeter.java:294)\r\n"
                + "    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n"
                + "    at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)\r\n"
                + "    at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)\r\n"
                + "    at java.lang.reflect.Method.invoke(Unknown Source)\r\n"
                + "    at org.apache.jmeter.NewDriver.main(NewDriver.java:259)\r\n";
        File temp = Files.createTempDirectory("jmeter-plugin-test").toFile();
        File file = File.createTempFile("scenario", ".log", temp);
        Files.write(file.toPath(), log.getBytes());
        String tempDir = file.getParentFile().getName();
        String logName = file.getName();
        String jmeterHandle = tempDir + "/" + logName.substring(0, logName.lastIndexOf('.'));
        String result = plugin.getLastResult(jmeterHandle);

        assertTrue(!result.contains(" INFO "));

        temp.deleteOnExit();
        file.deleteOnExit();
    }
}
