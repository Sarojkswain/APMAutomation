package com.ca.apm.systemtest.fld.plugin.em;

import com.ca.apm.systemtest.fld.plugin.AgentConfiguration;
import com.ca.apm.systemtest.fld.plugin.cm.ConfigurationManager;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.*;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MockitoJUnitRunner.class)
@Configurable
@ContextConfiguration(locations = {"/em-plugin-test-context.xml"})
public class emPluginTest {
    //TODO - check if test dependecies needed in POM.xml
    static EmPlugin.InstallationParameters config;

//    @Autowired
    @InjectMocks @Spy
    EmPluginImpl emPlugin;

    @Mock
    ConfigurationManager cm;

    @Mock
    TrussDownloadMethodImpl trdm;

    @Mock
    Eula eula;

    @Mock
    HttpURLDownloader httpU;

    @Mock
    ArtifactoryLiteDownloadMethod aldm;


    @Before
    public void setUp() throws Exception
    {
        String tmpDirectory = System.getProperty("java.io.tmpdir");

        config = new EmPlugin.InstallationParameters();
        config.osgiBuildId = "10.0.0.8";

        config.wvEmHost = "sqw64xeoserv30";
        config.wvEmPort = 5001;

        config.platform = SystemUtil.OperatingSystemFamily.Windows;

        config.logs = tmpDirectory + "/logs";
        config.installDir = tmpDirectory + "/Introscope";

        config.db = EmPlugin.Database.local;



        initMockito();
    }

    public ArtifactFetchResult getFakeIntroscopeFile() throws IOException {
        ArtifactFetchResult result = new ArtifactFetchResult();
        File resFile = new File(config.installDir + "Introscope.zip");

        ZipBuilder zip = new ZipBuilder(resFile);
        zip.addFile("Introscope.exe", "EXIT /B 1");
        zip.addFile("SampleResponseFile.Introscope.txt", "EXIT /B 1");
        zip.close();

        result.setBuildId("99.99");
        result.setFile(resFile);

        return result;
    }

    public ArtifactFetchResult getFakeEula() throws IOException {
        ArtifactFetchResult result = new ArtifactFetchResult();
        File resFile = new File(config.installDir + "Eula.txt");

        FileUtils.writeStringToFile(resFile, "Hello Eula");
        result.setBuildId("99.99");
        result.setFile(resFile);

        return result;
    }

    public ArtifactFetchResult getFakeOsgiPackages() throws IOException {
        ArtifactFetchResult result = new ArtifactFetchResult();
        File resFile = new File(config.installDir + "osgiPackages.zip");

        ZipBuilder zip = new ZipBuilder(resFile);
        zip.addFile("HelloOSGI.exe", "TEST");
        zip.close();

        result.setBuildId("99.99");
        result.setFile(resFile);


        return result;
    }

    private void initMockito() throws IOException, ArtifactManagerException {
        MockitoAnnotations.initMocks(this);

        //Common
        when(cm.loadPluginConfiguration(EmPlugin.PLUGIN, EmPluginConfiguration.class)).thenReturn(new EmPluginConfiguration());
        when(cm.loadPluginConfiguration("agent", AgentConfiguration.class)).thenReturn(new AgentConfiguration());

        Path tempPath = Paths.get(System.getProperty("user.dir"), "temp");
        new File(tempPath + "/em-staging").mkdir();
        new File(tempPath + "/em-staging/SampleResponseFile.Introscope.txt").createNewFile();
        new File(tempPath + "/em-staging/ca-eula.txt").createNewFile();

        doNothing().when(emPlugin).startInstaller((Path) anyObject(), anyString(), (InstallerProperties) anyObject());

        //Maven
        doReturn(getFakeIntroscopeFile()).when(emPlugin).downloadByArtifactory(
                anyString(), anyString(), anyString(), anyString(), anyString());


        //Truss
        doReturn(getFakeIntroscopeFile()).when(trdm).fetch(isNull(String.class), (File) anyObject(), (Map) anyObject(), anyBoolean());
        doReturn(getFakeEula()).when(trdm).fetch(eq(EmPluginImpl.EM_OPENSOURCE_EULA_ARTIFACT_SPEC), (File) anyObject(), (Map) anyObject(), anyBoolean());
        doReturn(getFakeOsgiPackages()).when(trdm).fetch(eq(EmPluginImpl.EM_OPENSOURCE_ARTIFACT_SPEC), (File) anyObject(), (Map) anyObject(), anyBoolean());

        doReturn(eula).when(eula).acceptEula();
    }

    @Test
    public void TestUrlIsComposedCorrectly() throws IOException, ArtifactManagerException {
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        Answer<ArtifactFetchResult> res = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                URL url = new URL(urlCaptor.getValue());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                System.out.println(conn.getContentLength());

                if (conn.getContentLength()==-1)
                {
                    fail("File not found from URL");
                }
                return null;
            }
        };

        doAnswer(res).when(httpU).download(urlCaptor.capture(), (File) anyObject(), anyBoolean());

        config.noInstallerSpecification = "truss:10.0.0-ISCP:990006:10.0.0.8";
        emPlugin.proceedWithInstallation(config);
    }

    @Test
    public void TestInstallEmMaven()
    {
        try {
            config.noInstallerSpecification = "maven:10.0.0.1";
            emPlugin.proceedWithInstallation(config);
            //check that parameters are correct there
            //check that URL is correct there
            fail();
        }
        catch(Exception ex)
        {
            assertEquals(ex.getCause().getMessage(), "Maven method is not implemented yet - osgi and eula are not downloaded");
        }
    }

    @Test
    public void TestInstallEmTruss1() throws ArtifactManagerException {
        config.noInstallerSpecification = "truss:10.0.0-ISCP:990006:10.0.0.8";
        emPlugin.proceedWithInstallation(config);
    }

    @Test
    public void TestInstallEmTrussDatabase()
    {
        config.noInstallerSpecification = "truss:10.0.0-ISCP:990006:10.0.0.8";
        config.installerType = InstallerProperties.InstallerType.DATABASE;
        config.db = EmPlugin.Database.oracle;
        config.dbHost = "localhost";
        config.dbPort = 1984;
        config.dbSid = "Sid meier's gettysburg";
        config.dbUserName = "User name";
        config.dbUserPass = "123456";

        emPlugin.proceedWithInstallation(config);
    }

    @Test
    public void TestInstallTrussWebView()
    {
        config.noInstallerSpecification = "truss:10.0.0-ISCP:990006:10.0.0.8";
        config.installerType = InstallerProperties.InstallerType.WEBVIEW;
        emPlugin.proceedWithInstallation(config);
    }

    @Test @Ignore
    public void TestInstallTrussEmLinuxPlatform()
    {
        config.noInstallerSpecification = "truss:10.0.0-ISCP:990006:10.0.0.8";
        config.platform = SystemUtil.OperatingSystemFamily.Linux;
        emPlugin.proceedWithInstallation(config);
    }

    @Test
    public void TestInstallEmTrussWrongNumberOfparameters()
    {
        try {
            config.noInstallerSpecification = "truss:10.0.0-ISCP:990006";
            emPlugin.proceedWithInstallation(config);
            fail();
        }
        catch(Exception ex)
        {
            assertEquals(ex.getCause().getMessage(), "Number of truss parameters is incorrect");
        }
    }

}
