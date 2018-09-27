package DownloadMethods;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shadm01 on 08-Jul-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/plugin-api-test-context.xml"})

public class HttpDownloadMethodTest {
    @BeforeClass
    public static void setUpClass() {

    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Autowired
    HttpDownloadMethod dm;

    @Test
    public void TestCorrectClassInstantiated() {
        assertEquals("com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod", dm.getClass().getName());
        assertEquals("HttpDownloadMethod", dm.getClass().getSimpleName());
    }

//    @Test
    public void testFetchResultFromDownloadSource() throws URISyntaxException {
        File path = new File(getClass().getResource(".").toURI());

        ArtifactFetchResult res = dm.fetchResultFromDownloadSource("truss:10.0.0:990006:10.0.0.8", path, "tomcat");
        assertEquals("10.0.0.8", res.getBuildId());
        assertEquals("IntroscopeAgentFiles-NoInstaller10.0.0.8tomcat.windows.zip", res.getFile().getName());
    }


//    @Test
    public void testDownloadAgentInstaller() throws Exception {
        Path p = dm.downloadAgent("truss.ca.com", "99.99.sys-ISCP", "99.99.0.sys", "000046", SystemUtil.OperatingSystemFamily.Windows);
        assertNotNull(p);
        assertNotNull(p.getFileName());
    }

//    @Test
    public void testFetch() throws Exception {
        final String SPECIFICATION_LINE = "http://truss.ca.com/builds/InternalBuilds/10.0.0-ISCP/build-990006(10.0.0.8)/introscope10.0.0.8/IntroscopeAgentFiles-NoInstaller10.0.0.8tomcat.windows.zip";
        final File Directory = new File(System.getProperty("java.io.tmpdir"));

        ArtifactFetchResult p = dm.fetch(SPECIFICATION_LINE, Directory, true);
        assertNotNull(p);
        assertNotNull(p.getFile());
        assertNotNull(p.getBuildId());
    }

}
