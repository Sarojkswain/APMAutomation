package DownloadMethods;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/plugin-api-test-context.xml"})

@Ignore("Ignored for now since we don't want to waste time downloading artifacts every build. Enable when you specifically want to TEMPORARILY execute tests.")
public class ArtifactoryDownloadMethodTest {
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
    ArtifactoryDownloadMethod dm;

    @Test
    public void TestCorrectClassInstantiated() {
        assertEquals("com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod", dm.getClass().getName());
        assertEquals("ArtifactoryDownloadMethod", dm.getClass().getSimpleName());
    }

    String DEFAULT_ARTIFACTORY_URL = "http://artifactory-emea-cz.ca.com:8081/artifactory/repo";

   // @Test
    public void TestFetchTempArtifact() {
        String VERSION = "10.0.0.1";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "agent-noinstaller-tomcat-windows";
        String classifier = null;
        String type = "zip";

        ArtifactFetchResult artifact = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, type);
        Assert.assertNotNull(artifact);
        Assert.assertEquals(artifact.getFile().getName(), "agent-noinstaller-tomcat-windows-10.0.0.1.zip");
        Assert.assertEquals(artifact.getBuildId(), VERSION);
    }

    @Test
    public void TestFetchTempArtifactWrongVersion() {
        String VERSION = "10.0.0.99";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "agent-noinstaller-tomcat-windows";
        String classifier = null;
        String type = "zip";

        ArtifactFetchResult artifact = dm.fetchTempArtifact(DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, type);
        Assert.assertNotNull(artifact);
        Assert.assertNull(artifact.getFile());
        Assert.assertNull(artifact.getBuildId());
    }

    @Test
    public void testFetchMavenWithAetherJcabi() throws Exception {
        String artifact = ArtifactoryDownloadMethod.buildArtifactCoordinatesStr("org.slf4j", "slf4j-api", "1.7.7", null, "jar");

        Path path = null;
        try {
            path = Files.createTempDirectory("lo-agent");
        } catch (IOException e1) {
            throw new ArtifactManagerException(e1);
        }

        ArtifactFetchResult result = dm.fetchUsingMavenCommandLine(artifact, path.toFile(), DEFAULT_ARTIFACTORY_URL);
        assertTrue(result.getFile().exists(), "Artifact result file does not exist.");
        assertTrue(path.toFile().getCanonicalPath().equals(result.getFile().getParentFile().getCanonicalPath()),
                "The artifact fetch result file is not under the requested directory.");
    }
}
