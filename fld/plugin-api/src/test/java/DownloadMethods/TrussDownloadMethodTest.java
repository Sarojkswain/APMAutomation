package DownloadMethods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpURLDownloader;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.JvmArch;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemArch;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemName;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;

/**
 * Created by shadm01 on 08-Jul-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/plugin-api-test-context.xml"})

public class TrussDownloadMethodTest {
    @Before
    public void setUp() throws Exception {
        SystemUtil.override(OperatingSystemFamily.Windows, JvmArch.Jvm64Bit,
            OperatingSystemArch.Arch64Bit, OperatingSystemName.WindowsServer2008);
        Mockito.reset(httpDownloader);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Autowired
    private TrussDownloadMethod dm;

    @Autowired
    private HttpURLDownloader httpDownloader;


    @Test
    public void testCorrectClassInstantiated() {
        assertEquals("com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethodImpl",
            dm.getClass().getName());
        assertEquals("TrussDownloadMethodImpl", dm.getClass().getSimpleName());
    }

    @Test
    public void testFetchResultFromDownloadSource() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                String artifactUrl = (String) invocation.getArguments()[0];
                URL url = new URL(artifactUrl);
                System.out.println(artifactUrl);
                String fileName = url.getFile();
                result.setFile(new File(fileName));

                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean()))
            .then(answer);

        File tempFolderPath = new File(getClass().getResource(".").toURI());

        ArtifactFetchResult res = dm
            .fetchResultFromDownloadSource("truss:10.0.0:990006:10.0.0.8", tempFolderPath,
                "tomcat");
        assertEquals("10.0.0.8", res.getBuildId());
        assertEquals("IntroscopeAgentFiles-NoInstaller10.0.0.8tomcat.windows.zip",
            res.getFile().getName());
    }

    @Test
    public void testDownloadAgentInstaller() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                File dest = new File((File) invocation.getArguments()[1], "test.zip");
                ZipBuilder zip = new ZipBuilder(dest);
                zip.addFile("echo.txt", "Echo");
                zip.close();

                result.setFile(dest);

                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean()))
            .then(answer);
        
        /*
         * FIXME - need some reasonable asserts here to set what is being returned from the
         * downloader
         * such as verifying that the URL generated is correct, the filename is correct (which
         * the above
         * filename is guaranteed NOT to be correct)
         */
        Path p = dm.downloadAgent("truss.ca.com", "99.99.sys-ISCP", "99.99.0.sys", "000046",
            SystemUtil.OperatingSystemFamily.Windows);
        assertNotNull(p);
        assertNotNull(p.getFileName());
    }

    @Test
    public void testFetchUsingUrl() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        Answer<ArtifactFetchResult> answer = new Answer<ArtifactFetchResult>() {
            @Override
            public ArtifactFetchResult answer(InvocationOnMock invocation) throws Throwable {
                ArtifactFetchResult result = new ArtifactFetchResult();
                // create a zip file that will be used
                File dest = new File((File) invocation.getArguments()[1], "test.zip");
                ZipBuilder zip = new ZipBuilder(dest);
                zip.addFile("echo.txt", "Echo");
                zip.close();

                result.setFile(dest);

                return result;
            }
        };
        when(httpDownloader.download(urlCaptor.capture(), (File) anyObject(), anyBoolean()))
            .then(answer);

        final File Directory = new File(System.getProperty("java.io.tmpdir"));

        String donwnloadUrl
            = "http://truss.ca.com/builds/InternalBuilds/10.0.0-ISCP/build-990006(10.0.0.8)"
            + "/introscope10.0.0.8/IntroscopeAgentFiles-NoInstaller10.0.0.8tomcat.windows.zip";

        ArtifactFetchResult p = dm.fetch(donwnloadUrl, Directory, true);
        /*
         * FIXME - we need some reasonable asserts to check that the URL is being handled properly
         */
        assertNotNull(p);
        assertNotNull(p.getFile());
    }


    @Test
    public void testGetDownloadUrl() {
        String res = "";
        try {
            res = dm.getDownloadUrl("truss:10.0.0:990007:10.0.0.9", "websphere");

            assertEquals(
                "http://truss.ca.com/builds/InternalBuilds/10.0.0-ISCP/build-990007(10.0.0.9)"
                    + "/introscope10.0.0.9/IntroscopeAgentFiles-NoInstaller10.0.0.9websphere"
                    + ".windows.zip",
                res);
        } catch (Exception ex) {
            assertTrue(false);
        }
    }


    //DotNetUrl
    //http://truss.ca.com/builds/InternalBuilds/9.7.0-NET  /build-000091(9.7.0.0)
    //        /DotNetAgentFiles-NoInstaller.x86.9.7.0.0.zip
    //Java
    //http://truss.ca.com/builds/InternalBuilds/10.0.0-ISCP/build-990006(10.0.0.8)/introscope10.0
    // .0.8    /IntroscopeAgentFiles-NoInstaller10.0.0.8tomcat.windows.zip

    //TODO - DM - DotNetClientTest


}
