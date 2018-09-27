package com.ca.apm.systemtest.fld.plugin.tomcattest;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.FileCopyUtils;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AgentInstallException;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpURLDownloader;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPlugin;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPluginImpl;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.JvmArch;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemArch;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemName;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;

/**
 * Unit test for {@link TomcatPluginImpl} - uses mocks for the actual downloads to speed up the test execution
 * @author shadm01 keyja01
 *
 */
public class TomcatPluginImplTest {
    private static final Logger log = LoggerFactory.getLogger(TomcatPluginImplTest.class);
    private TomcatPlugin plugin;
    private ClassPathXmlApplicationContext ctx;
    private HttpURLDownloader httpDownloader;
    static final String tomcatPluginConfigFilePath = "./target/conf/tomcatPlugin.conf.json";
    static Map<String, String> extraProperties = new HashMap<>();
    static ArrayList<String> extraModules = new ArrayList<>();

    private String getBaseDir() {
        final String baseDir = System.getProperty("java.io.tmpdir");
        String res = baseDir.replace("\\", "/") + "tomcat" ;

        return res;
    }

    @Before
    public void setUp() throws Exception {
        String[] targetDirs = {"target/tomcat1", "target/tomcat2", "target/tomcat3", "target/tomcat4"};
        for (String tgt: targetDirs) {
            File tomcat = new File(tgt);
            if (tomcat.exists()) {
                FileUtils.forceDelete(tomcat);
            }
            File bin = new File(tomcat, "bin");
            FileUtils.forceMkdir(bin);
            File catalinaBat = new File(bin, "catalina.bat");
            FileCopyUtils.copy(getClass().getResourceAsStream("/tomcat6/bin/catalina.bat"), new FileOutputStream(catalinaBat));
            catalinaBat = new File(bin, "catalina.sh");
            FileCopyUtils.copy(getClass().getResourceAsStream("/tomcat6/bin/catalina.sh"), new FileOutputStream(catalinaBat));
        }
        
        
        InputStream in = getClass().getResourceAsStream("/com/ca/apm/systemtest/fld/plugin/tomcat/tomcatPlugin.conf.json");
        String conf = FileCopyUtils.copyToString(new InputStreamReader(in, "US-ASCII"));
        conf = conf.replace("${base.dir}", "target");
        FileUtils.writeStringToFile(new File(tomcatPluginConfigFilePath), conf);

        extraProperties = new HashMap<>();
        extraProperties
            .put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", "192.168.57.101");
        extraProperties.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        extraProperties.put("introscope.agent.agentAutoNamingEnabled", "false");
        extraProperties.put("introscope.agent.customProcessName", "null");
        extraProperties
            .put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", "5001");
        extraProperties.put("introscope.agent.decorator.enabled", "true");
        extraProperties.put("introscope.autoprobe.directivesFile",
            "tomcat-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd");
        extraProperties.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");

        extraModules = new ArrayList<>(1);

        ctx = new ClassPathXmlApplicationContext("tomcat-plugin-test-context.xml");
        plugin = ctx.getBean(TomcatPluginImpl.class);
        httpDownloader = ctx.getBean(HttpURLDownloader.class);
    }

    @After
    public void tearDown() {
        File dir = new File(getBaseDir());
        try {
            deleteDirectory(dir);
        } catch (IOException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Error deleting {1}. Exception: {0}", dir.getAbsolutePath());
        }
        ctx.close();
    }

    @Test
    public void dummyTest() {
        assertNotNull(plugin);
    }

    @Test
    public void testIsPluginIsInstalled() {
        boolean installed = plugin.isAgentInstalled("server1");
        assertFalse("'Test plugin not installed' fails", installed);
    }

    @Test
    public void testInstallAgentUsingTruss() throws Exception {
        // set up the mock
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                
                result.setFile(createAgentInstallZip((File) invocation.getArguments()[1]));
                
                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean())).then(answer);
        
        String branch = "10.0.0";
        String buildNumber = "990006";
        String buildId = "10.0.0.8";
        
        SystemUtil.override(OperatingSystemFamily.Windows, JvmArch.Jvm64Bit, OperatingSystemArch.Arch64Bit, OperatingSystemName.WindowsServer2008);
        
        plugin.installAgentNoInstaller("server2", "truss:10.0.0:990006:10.0.0.8", extraProperties,
            extraModules, false);
        String url = urlCaptor.getValue();
        String predictedUrl = 
            "http://truss.ca.com/builds/InternalBuilds/"
            + branch
            + "-ISCP/build-"
            + buildNumber
            + "(" + buildId + ")/introscope"
            + buildId
            + "/IntroscopeAgentFiles-NoInstaller"
            + buildId
            + "tomcat.windows.zip";
        assertEquals("The generated TRUSS artifact URL was incorrect", predictedUrl, url);
    }
    
    @Test
    public void testInstallAgentUsingArtifactoryLite10dot() throws Exception {
//        setServerConfig(config_ok);
//        restoreDirectoryStructure();
//        plugin.installAgentNoInstaller("tomcat6", "maven:10.0.0.1", extraProperties, extraModules);
        // set up the mock
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> groupIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> artifactIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> versionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> classifierCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                
                File dest = new File("target/tomcat3");
                result.setFile(createAgentInstallZip(dest));
                
                return result;
            }
        };
        
        ArtifactoryLiteDownloadMethod downloader = Mockito.mock(ArtifactoryLiteDownloadMethod.class);
        ((TomcatPluginImpl) plugin).setArtifactoryLiteDm(downloader);
        
        when(downloader.fetchTempArtifact(urlCaptor.capture(), groupIdCaptor.capture(), artifactIdCaptor.capture(), 
            versionCaptor.capture(), classifierCaptor.capture(), typeCaptor.capture())).then(answer);
        
        SystemUtil.override(OperatingSystemFamily.Windows, JvmArch.Jvm64Bit, OperatingSystemArch.Arch64Bit, OperatingSystemName.WindowsServer2003);

        String expectedVersion = "10.0.0.1";
        plugin.installAgentNoInstaller("server3", "maven:" + expectedVersion, extraProperties,
            extraModules, false);
        
        assertEquals(artifactIdCaptor.getValue(), "agent-noinstaller-tomcat-windows");
        assertEquals(groupIdCaptor.getValue(), "com.ca.apm.delivery");
        assertEquals(versionCaptor.getValue(), "10.0.0.1");
        assertEquals(typeCaptor.getValue(), "zip");
    }


    @Test
    public void installWithWrongServerProfile() {
        try {
            plugin.installAgentNoInstaller("tomcat12346", "truss:10.0.0:990006:10.0.0.8", 
                extraProperties, extraModules, true);
        fail("Exception should be thrown");
        } catch (AgentInstallException ex) {
            assertEquals(ex.getErrorCode(), AgentInstallException.ERR_SERVER_INSTANCE_MISSING);
        }
    }

    @Test
    public void testInstallAgent_NonEmptyAgentDirectory() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                
                result.setFile(createAgentInstallZip((File) invocation.getArguments()[1]));
                
                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean())).then(answer);
        
        plugin.installAgentNoInstaller("server4", "truss:10.0.0:990006:10.0.0.8", extraProperties,
            extraModules, false);

        try {
            plugin.installAgentNoInstaller("server4", "truss:10.0.0:990006:10.0.0.8",
                extraProperties, extraModules, true);
            fail("Exception should be thrown");
        } catch (AgentInstallException ex) {
            assertEquals(ex.getErrorCode(), AgentInstallException.ERR_AGENT_ALREADY_INSTALLED);
        }
    }

    @Test
    public void testInstallAgent_UninstallAgent() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                
                result.setFile(createAgentInstallZip((File) invocation.getArguments()[1]));
                
                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean())).then(answer);
        
        plugin.installAgentNoInstaller("server3", "truss:10.0.0:990006:10.0.0.8", extraProperties,
                extraModules, false);
        plugin.uninstallAgentNoInstaller("server3", true);
    }

    
    private File createAgentInstallZip(File destDir) throws Exception {
        File f = new File(destDir, "agent.zip");
        ZipBuilder zip = new ZipBuilder(f);
        
        zip.addFolder("/wily/");
        zip.addFolder("/wily/core/");
        zip.addFolder("/wily/core/config/");
        zip.addFile("/wily/core/config/IntroscopeAgent.profile", "some.property=true\n");
        
        zip.close();
        return f;
    }
}
