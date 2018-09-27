package com.ca.apm.systemtest.fld.plugin.wurlitzer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;

public class WurlitzerPluginImplTest {
    private static final Logger log = LoggerFactory.getLogger(WurlitzerPluginImplTest.class);

    private WurlitzerPluginImpl wu;
    private ArtifactoryDownloadMethod dm;


    @Before
    public void before() {
        dm = Mockito.mock(ArtifactoryDownloadMethod.class);
        
        wu = new WurlitzerPluginImpl();
        wu.setDownloadMethod(dm);
        wu.createTempDir();
    }

    @SuppressWarnings("unused")
    @Test
    public void test() throws Exception {
        
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {

            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                File tmpFile = File.createTempFile("wurlitzerPluginImplTest", "zip");
                ZipBuilder zip = new ZipBuilder(tmpFile);
                zip.addFile("foo.txt", "Foo me, Amadeus!");
                //wurlitzer\scripts\xml
                zip.addFolder("/scripts");
                zip.addFolder("/scripts/xml");
                zip.close();
                tmpFile.deleteOnExit();
                
                ArtifactFetchResult value = new ArtifactFetchResult();
                value.setFile(tmpFile);
                return value;
            }
            
        };
        
        doAnswer(answer).when(dm).fetchTempArtifact(urlCaptor.capture(), anyString(), anyString(), anyString(), (String) any(), eq("zip"));
        
        File file = wu.downloadWurlitzer(null);
        assertNotNull(file);
        log.info("Wurlitzer downloaded in {} file.", file);

        File dir = wu.unzipWurlitzerZip();
        assertNotNull(dir);
        log.info("Wurlitzer extracted to {} directory.", dir);

        String xml = "scripts/xml/appmap-stress/load-test/scenarios/001-agent-001-app-0001-backend.appmap.xml"; 
        wu.setBuiltInScenario(xml);

        wu.setScenarioUrl(new URL("https://somewhere/foo.xml"));
        
        wu.setBuiltTargetScenario("BUILD_TARGET");
        String wurlitzerId = wu.execute();
        
        wu.replaceContent("agent-host-names.txt");

        if (false) { // disabled for now
           
            wurlitzerId = wu.execute();
            Thread.sleep(TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
            wu.stop(wurlitzerId);
        }
    }

    @After
    public void after() {
        wu.deleteTempDir();
        wu = null;
    }
}