
package com.ca.apm.systemtest.fld.plugin.em;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;

/**
 * Set silent installer properties.
 * 
 * @author jirji01
 *
 */
public class SilentInstallerProperties {

    private static final String TEST_RESPONSE_FILE = "TestResponseFile.txt";
    private Path responseFile;

    @BeforeTest
    public void prepareTempFolder() throws Exception {
        responseFile = Paths.get(TEST_RESPONSE_FILE);
    }

    /**
     * Prepare testing setup file.
     * 
     * @throws Exception
     */
    @BeforeMethod
    public void prepareResponseFile() throws Exception {
        Enumeration<URL> urls =
            SilentInstallerProperties.class.getClassLoader().getResources(
                "SampleResponseFile.Introscope.txt");
        try (InputStream input = Files.newInputStream(Paths.get(urls.nextElement().toURI()));
        OutputStream output = Files.newOutputStream(responseFile)) {
            IOUtils.copyLarge(input, output);
        }
    }

    @AfterMethod
    public void deleteResponseFile() throws Exception {
        Files.deleteIfExists(responseFile);
    }

    @Test
    public void loadSampleResponseFile() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM);
    }

    @Test
    public void setHeapSize() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setHeapSize(1234).writeResponseFile();

        Properties pr = getProperties();
        String st = pr.getProperty("emLaxNlJavaOptionAdditional");

        assertTrue(st.contains("-Xms1234m"), "Expected memory size is 1234");
        assertTrue(st.contains("-Xmx1234m"), "Expected memory size is 1234");
    }

    @Test
    public void setLogFile() throws Exception {
        if ("/".equals(File.separator)) {
            new InstallerProperties(responseFile, InstallerType.EM).setLogFolder(Paths.get("/test"))
                .writeResponseFile();
            String st = getProperties().getProperty("emLaxNlJavaOptionAdditional");
            assertTrue(st.contains("-Xloggc:/test"), "Expected log folder /test");
        } else {
            new InstallerProperties(responseFile, InstallerType.EM).setLogFolder(Paths.get("X:\\test"))
                .writeResponseFile();
            String st = getProperties().getProperty("emLaxNlJavaOptionAdditional");
            assertTrue(st.contains("-Xloggc:X:\\test"), "Expected log folder X:\\test");
        }
    }

    @Test
    public void setEmptyCollectors() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setCollectors(Collections.<String>emptySet())
            .writeResponseFile();

        Properties pr = getProperties();

        assertEquals(pr.getProperty("emClusterRole"), "Collector",
            "emClusterRole should be set to Collector");
        for (int i = 1; i <= 10; i++) {
            assertTrue(pr.getProperty("emCollectorHost." + i).isEmpty(),
                "emCollectorHost property must be empty");
            assertTrue(pr.getProperty("emCollectorPort." + i).isEmpty(),
                "emCollectorPort property must be empty");
        }
    }

    @Test
    public void setValidCollectors() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setCollectors(Arrays.asList("A", "B", "C"))
            .writeResponseFile();

        Properties pr = getProperties();

        assertEquals(pr.getProperty("emClusterRole"), "Manager",
            "emClusterRole should be set to Manager");

        assertEquals(pr.getProperty("emCollectorHost.1"), "A",
            "emCollectorHost property must be A");
        assertEquals(pr.getProperty("emCollectorPort.1"), "5001",
            "emCollectorPort property must be 5001");
        assertEquals(pr.getProperty("emCollectorHost.2"), "B",
            "emCollectorHost property must be B");
        assertEquals(pr.getProperty("emCollectorPort.2"), "5001",
            "emCollectorPort property must be 5001");
        assertEquals(pr.getProperty("emCollectorHost.3"), "C",
            "emCollectorHost property must be C");
        assertEquals(pr.getProperty("emCollectorPort.3"), "5001",
            "emCollectorPort property must be 5001");
        assertTrue(pr.getProperty("emCollectorHost.4").isEmpty(),
            "emCollectorHost property must be empty");
        assertTrue(pr.getProperty("emCollectorPort.4").isEmpty(),
            "emCollectorPort property must be empty");
    }

    @Test
    public void setInstallationDir() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setInstallationDir(Paths.get("test"))
            .writeResponseFile();

        String st = getProperties().getProperty("USER_INSTALL_DIR");

        assertTrue(!st.startsWith("test"), "Expected installation absolute folder path");
    }

    @Test
    public void setExternalComponentPackage() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setExternalComponentPackage(Paths.get("test"))
            .writeResponseFile();

        String st = getProperties().getProperty("externalComponentPackage");

        assertTrue(st.endsWith("test"), "Expected osgi file");
        assertTrue(!st.startsWith("test"), "Expected osgi absolute file path");
    }

    @Test
    public void setOracleDb() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setOracleDatabase("host", 12345, "sid", "usr", "pwd")
            .writeResponseFile();

        Properties pr = getProperties();

        assertEquals(pr.get("chosenDatabaseIsPostgres"), "false");
        assertEquals(pr.get("chosenDatabaseIsOracle"), "true");
        assertEquals(pr.get("useExistingSchemaForOracle"), "false");
        assertEquals(pr.get("oracleDbHost"), "host");
        assertEquals(pr.get("oracleDbPort"), "12345");
        assertEquals(pr.get("oracleDbSidName"), "sid");
        assertEquals(pr.get("oracleDbUsername"), "usr");
        assertEquals(pr.get("oracleDbPassword"), "pwd");
    }

    @Test
    public void setPostgreDb() throws Exception {
        new InstallerProperties(responseFile, InstallerType.EM).setPostgreDatabase("host", "usr", "pwd", "susr", "spwd")
            .writeResponseFile();

        Properties pr = getProperties();

        assertEquals(pr.get("chosenDatabaseIsPostgres"), "true");
        assertEquals(pr.get("chosenDatabaseIsOracle"), "false");
        assertEquals(pr.get("dbHost"), "host");
        assertEquals(pr.get("dbPassword"), "pwd");
        assertEquals(pr.get("dbAdminUser"), "susr");
        assertEquals(pr.get("dbAdminPassword"), "spwd");
    }
    
    @Test
    public void setEmHostPort() throws Exception {
        new InstallerProperties(responseFile, InstallerType.WEBVIEW).setEmHostPort("sqw64xeoserv", 5001)
            .writeResponseFile();

        String st = getProperties().getProperty("wvEmHost");
        
        String st2 = getProperties().getProperty("wvEmPort");

        assertTrue(st.startsWith("sqw64xeoserv"), "Expected hostname to EM");
        assertTrue(st2.startsWith(Integer.toString(5001)), "Expected port to EM");
    }

    private Properties getProperties() throws IOException {
        Properties pr = new Properties();
        Reader re = Files.newBufferedReader(responseFile, StandardCharsets.UTF_8);
        pr.load(re);
        re.close();
        return pr;
    }
}
