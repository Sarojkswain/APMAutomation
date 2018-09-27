package DownloadMethods;

import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@Configurable
@ContextConfiguration(locations = {"/plugin-api-test-context.xml"})

public class ArtifactoryLiteDownloadMethodTest {
    @Autowired
    ArtifactoryLiteDownloadMethod dm;

    private void UrlStructureCheck(String repo_url, String groupID, String artifactId, String VERSION, String classifier, String os, String type)
    {
        classifier = classifier == null ? "" : "-" + classifier;


        String url = String.format(
                "%s/%s/%s/%s/%s-%s%s.%s",
                repo_url,
                groupID.replace(".", "/"),
                artifactId,
                VERSION,
                artifactId,
                VERSION,
                classifier,
                type
        );

        try {
            URL artifactUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) artifactUrl.openConnection();

            if (conn.getContentLength() == -1) {
                fail("File not found from URL");
            }
            System.out.println("URL OK: " + url);
        } catch (MalformedURLException e) {
            fail("Url not composed correctly");
        } catch (IOException e) {
            fail("File not found from URL");
        }
    }


   // @Test
    public void testCorrectClassInstantiated() {
        assertEquals("com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethodImpl", dm.getClass().getName());
        assertEquals("ArtifactoryLiteDownloadMethodImpl", dm.getClass().getSimpleName());
    }

    //@Test
    public void TestFetchArtifactFromArtifactoryLite() {
        String VERSION = "10.0.0.1";
        String os = "windows";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "agent-noinstaller-tomcat-" + os;
        String classifier = null;
        String type = "zip";

        UrlStructureCheck(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, os, type);
    }

    //@Test
    public void TestDownloadEMWindows() {
        String VERSION = "10.0.0.8";
        String os = "windows";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "introscope-installer-" + os;
        String classifier = os + "AMD64";
        String type = "exe";

        UrlStructureCheck(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, os, type);
    }

    //@Test
    public void TestDownloadEMUnix() {
        String VERSION = "10.0.0.8";
        String os = "unix";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "introscope-installer-" + os;
        String classifier = "linuxAMD64";
        String type = "bin";

        UrlStructureCheck(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, os, type);
    }

    //@Test
    public void TestDownloadOsgiPackage() {
        String VERSION = "10.0.0.11";
        String os = "windows";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "opensource";
        String classifier = os + "-dist";
        String type = "zip";

        UrlStructureCheck(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, os, type);
    }

   // @Test
    public void TestDownloadOsgiEula() {
        String VERSION = "10.0.0.11";
        String os = "windows";

        String groupID = "com.ca.apm.delivery";
        String artifactId = "opensource";
        String classifier = os + "-dist";
        String type = "zip";

        UrlStructureCheck(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupID, artifactId, VERSION, classifier, os, type);
    }

    private void checkArtifact(String repo_url, String groupID, String artifactId, String VERSION, String classifier, String type)
    {
        ArtifactFetchResult artifact = dm.fetchTempArtifact(repo_url, groupID, artifactId, VERSION, classifier, type);
        Assert.assertNotNull(artifact);
        Assert.assertNotNull(artifact.getBuildId());
        Assert.assertNotNull(artifact.getFile());
    }

//    @Test
//    public void TestBuildCoordinatesString()
//    {
//        String VERSION = "10.0.0.1";
//        String os = "windows";
//
//        String groupID = "com.ca.apm.delivery";
//        String artifactId = "agent-noinstaller-tomcat-" + os;
//        String classifier = null;
//        String type = "zip";
//
//        String s = dm.buildArtifactCoordinatesStr(groupID, artifactId, VERSION, classifier, type);
//        System.out.println(s);
//    }

}
