package com.ca.apm.systemtest.fld.plugin.file.transformation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.XmlUtils;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.Configuration;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by haiva01 on 15.6.2015.
 */
public class FileTransformationPluginTest {
    Logger log = LoggerFactory.getLogger(FileTransformationPluginTest.class);

    ClassPathXmlApplicationContext applicationContext;
    FileTransformationPlugin plugin;

    @Before
    public void beforeClass() {
        applicationContext = new ClassPathXmlApplicationContext("test-context.xml");
        plugin = applicationContext.getBean(FileTransformationPluginImpl.class);
        plugin.createTempDir();
    }

    @After
    public void afterClass() {
        plugin.deleteTempDir();
        plugin = null;
        applicationContext.close();
    }

    static final String testdata = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
        + "<configuration>\n"
        + "    <transformation id=\"1\">\n"
        + "        <set-property name=\"a\" value=\"x\"/>\n"
        + "        <delete-property name=\"b\"/>\n"
        + "    </transformation>\n"
        + "\n"
        + "    <transformation id=\"2\">\n"
        + "        <set-property name=\"c\" value=\"x\"/>\n"
        + "        <delete-property name=\"d\"/>\n"
        + "    </transformation>\n"
        + "\n"
        + "    <transformation id=\"3\">\n"
        + "        <delete-property name=\"x\"/>\n"
        + "        <xslt-transform>\n"
        + "          <xslt><![CDATA[\n"
        + "             <xsl:stylesheet version=\"1.0\"\n"
        + "                 xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
        + "                 <xsl:template match=\"/\">\n"
        + "                     <html>\n"
        + "                     <body>\n"
        + "                     <h2>My CD Collection</h2>\n"
        + "                     <table border=\"1\">\n"
        + "                     <tr bgcolor=\"#9acd32\">\n"
        + "                     <th>Title</th>\n"
        + "                     <th>Artist</th>\n"
        + "                     </tr>\n"
        + "                     <tr>\n"
        + "                     <td><xsl:value-of select=\"catalog/cd/title\"/></td>\n"
        + "                     <td><xsl:value-of select=\"catalog/cd/artist\"/></td>\n"
        + "                     </tr>\n"
        + "                     </table>\n"
        + "                     </body>\n"
        + "                     </html>\n"
        + "                 </xsl:template>\n"
        + "             </xsl:stylesheet>]]>\n"
        + "          </xslt>\n"
        + "        </xslt-transform>\n"
        + "    </transformation>\n"
        + "\n"
        + "    <files id=\"group1\">\n"
        + "        <file><![CDATA[C:\\test.txt]]></file>\n"
        + "        <file>C:\\test2.txt</file>\n"
        + "    </files>\n"
        + "\n"
        + "    <files id=\"group2\">\n"
        + "        <file>C:\\test3.txt</file>\n"
        + "        <file>C:\\test4.txt</file>\n"
        + "        <file>C:\\test5.txt</file>\n"
        + "    </files>\n"
        + "\n"
        + "    <binding>\n"
        + "        <transformation-ref id=\"1\"/>\n"
        + "        <files-ref id=\"group1\"/>\n"
        + "    </binding>\n"
        + "\n"
        + "    <binding>\n"
        + "        <transformation-ref id=\"3\"/>\n"
        + "        <transformation-ref id=\"2\"/>\n"
        + "        <files-ref id=\"group2\"/>\n"
        + "    </binding>\n"
        + "</configuration>\n";

    @Test
    public void testParsing() throws JAXBException, IOException {
        log.info("test input:\n{}", testdata);

        // Parse configuration from XML string.

        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Configuration config = (Configuration) jaxbUnmarshaller
            .unmarshal(IOUtils.toInputStream(testdata));
        assertNotNull(config);
        log.info("parsed configuration:\n{}", config);

        // Serialize parsed configuration back to a string.

        Marshaller jaxbMarshaler = jaxbContext.createMarshaller();
        jaxbMarshaler.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        Writer output = new StringWriter(testdata.length());
        jaxbMarshaler.marshal(config, output);
        log.info("serialized configuration:\n{}", output.toString());

        // Parse above serialized configuration back to check round-trip equality.

        Configuration config2 = (Configuration) jaxbUnmarshaller
            .unmarshal(IOUtils.toInputStream(testdata));
        assertNotNull(config2);
        log.info("serialized configuration parsed back:\n{}", config);
        assertEquals(config.toString(), config2.toString());

        // Serialize to JSON.

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(
            mapper.getTypeFactory());
        mapper.setAnnotationIntrospectors(introspector, introspector);
        String jsonConfig = mapper.writeValueAsString(config);
        assertNotNull(jsonConfig);
        log.info("configuration serialized into JSON:\n{}", jsonConfig);

        // Parse configuration from above serialized JSON back.

        Configuration configFromJson = mapper.readValue(jsonConfig, Configuration.class);
        assertNotNull(configFromJson);
        log.info("configuration parsed back from JSON:\n{}", configFromJson.toString());
        assertEquals(config.toString(), configFromJson.toString());
    }

    static final String testPropertyFileTransformationConfig =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<configuration>\n"
            + "    <transformation id=\"1\">\n"
            + "        <set-property name=\"a\" value=\"${#_.replace('A','X')}\"/>\n"
            + "        <delete-property name=\"b\"/>\n"
            + "        <append-property name=\"c\" value=\"Y\"/>\n"
            + "        <set-property name=\"introscope.autoprobe.directivesFile\" value=\"${#_.replace('default-full.pbl','default-typical.pbl')}\"/>\n"
            + "    </transformation>\n"
            + "\n"
            + "    <files id=\"group1\">\n"
            + "        <file>${#dir + '/' + #fileName}</file>\n"
            + "    </files>\n"
            + "\n"
            + "    <binding>\n"
            + "        <transformation-ref id=\"1\"/>\n"
            + "        <files-ref id=\"group1\"/>\n"
            + "    </binding>\n"
            + "</configuration>\n";

    static final String testPropertyFileTransformationPropFileContents = "# header comment\n"
        + "a=AAAA\n"
        + "# Comment for b.\n"
        + "b=this will be removed\n"
        + "# Will append Y to the c property.\n"
        + "c=X\n"
        + "introscope.autoprobe.directivesFile=default-full.pbl,hotdeploy,spm.pbl,sharepoint.pbl\n"
        + "# footer comment\n";

    @Test
    public void testPropertyFileTransformation() throws Exception {
        // Save the configuration XML into a file.

        final File tempXmlConfigurationFile = ACFileUtils.generateTemporaryFile("configuration", 
            ".xml", FileUtils.getTempDirectory());
        tempXmlConfigurationFile.deleteOnExit();

        try (OutputStream os = FileUtils.openOutputStream(tempXmlConfigurationFile)) {
            IOUtils.write(testPropertyFileTransformationConfig, os,
                StandardCharsets.UTF_8);
        }

        // Serve the transformation configuration using Jetty.

        Server server = new Server();
        try {
            ServerConnector connector = new ServerConnector(server);
            // We do not set the port here. It will be randomly assigned and retrieved.
            //connector.setPort(8080);
            server.addConnector(connector);

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setResourceBase(tempXmlConfigurationFile.getAbsoluteFile().getParent());
            resourceHandler.setWelcomeFiles(new String[]{tempXmlConfigurationFile.getName()});

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
            server.setHandler(handlers);

            server.start();

            final URL configurationUrl = new URL("http", "localhost", connector.getLocalPort(),
                "/" + tempXmlConfigurationFile.getName());

            // Prepare properties file for the test.

            final File tempPropertyFile = ACFileUtils.generateTemporaryFile("test", 
                ".properties", FileUtils.getTempDirectory());
            assertNotNull(tempPropertyFile);
            tempPropertyFile.deleteOnExit();
            log.info("Storing test properties to {}", tempPropertyFile.getAbsolutePath());

            try (OutputStream os = FileUtils.openOutputStream(tempPropertyFile)) {
                IOUtils.write(testPropertyFileTransformationPropFileContents, os,
                    StandardCharsets.ISO_8859_1);
            }

            // Set up transformation configuration.

            final Map<String, Object> vars = new TreeMap<>();
            vars.put("dir", tempPropertyFile.getParent());
            vars.put("fileName", tempPropertyFile.getName());

            // Run the transformation.

            plugin.transformUrl(configurationUrl.toString(),
                FileTransformationPlugin.ConfigurationFormat.XML, vars);

            // Load the results of the transformation.

            final Properties testFileProps = new Properties();
            try (InputStream inputStream = FileUtils.openInputStream(tempPropertyFile)) {
                testFileProps.load(inputStream);
            }

            // Check properties.

            assertFalse(testFileProps.containsKey("b"));
            assertTrue(testFileProps.containsKey("a"));
            assertEquals(testFileProps.get("a"), "XXXX");
            assertTrue(testFileProps.containsKey("c"));
            assertEquals(testFileProps.get("c"), "XY");
            assertTrue(testFileProps.containsKey("introscope.autoprobe.directivesFile"));
            assertEquals("default-typical.pbl,hotdeploy,spm.pbl,sharepoint.pbl",
                testFileProps.get("introscope.autoprobe.directivesFile"));
        } catch (Exception e) {
            log.error("Exception.", e);
        } finally {
            //server.join();
            server.stop();
        }
    }


    static final String testXsltTransformationTestConfig =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<configuration>\n"
            + "    <transformation id=\"1\">\n"
            + "        <xslt-transform>\n"
            + "            <xslt><![CDATA["
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3"
            + ".org/1999/XSL/Transform\" xmlns:xalan=\"http://xml.apache.org/xslt\">\n"
            + "<xsl:strip-space elements=\"*\"/>\n"
            + "<xsl:output method=\"xml\" indent=\"yes\" xalan:indent-amount=\"4\"/>\n"
            + "<xsl:template match=\"@*|node()\">\n"
            + "  <xsl:copy>\n"
            + "    <xsl:apply-templates select=\"@*|node()\"/>\n"
            + "  </xsl:copy>\n"
            + "</xsl:template>\n"
            + "<xsl:template match=\"domains\">\n"
            + "  <domains>\n"
            + "     <xsl:apply-templates select=\"@* | *\"/>\n"
            + "    <domain name=\"${#domainName}\">\n"
            + "        <agent mapping=\"(.*)\\|(.*)\\|kokoagent(.*)\"/>\n"
            + "        <grant group=\"Admin\" permission=\"full\"/>\n"
            + "        <grant user=\"Guest\" permission=\"read\"/>\n"
            + "    </domain>\n"
            + "  </domains>\n"
            + "</xsl:template>\n"
            + "</xsl:stylesheet>]]></xslt>\n"
            + "        </xslt-transform>\n"
            + "    </transformation>\n"
            + "\n"
            + "    <files id=\"group1\">\n"
            + "        <file>${#fileName}</file>\n"
            + "    </files>\n"
            + "\n"
            + "    <binding>\n"
            + "        <transformation-ref id=\"1\"/>\n"
            + "        <files-ref id=\"group1\"/>\n"
            + "    </binding>\n"
            + "</configuration>\n";


    static final String testXsltTransformationTestXml = "<?xml version=\"1.0\" "
        + "encoding=\"UTF-8\"?>\n"
        + "<domains xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:noNamespaceSchemaLocation=\"domains0.3.xsd\" version=\"0.3\">\n"
        + "    <SuperDomain>\n"
        + "        <agent mapping=\"(.*)\"/>\n"
        + "        <grant group=\"Admin\" permission=\"full\"/>\n"
        + "        <grant user=\"Guest\" permission=\"read\"/>\n"
        + "    </SuperDomain>\n"
        + "</domains>\n";

    @Test
    public void testXsltTransformation() throws IOException, ParserConfigurationException,
        SAXException, XPathExpressionException {
        // Prepare properties file for the test.

        final File tempDomainsFile = ACFileUtils.generateTemporaryFile("domains", 
            ".xml", FileUtils.getTempDirectory());
        assertNotNull(tempDomainsFile);
        tempDomainsFile.deleteOnExit();
        log.info("Storing test properties to {}", tempDomainsFile.getAbsolutePath());

        try (OutputStream os = FileUtils.openOutputStream(tempDomainsFile)) {
            IOUtils.write(testXsltTransformationTestXml, os, StandardCharsets.UTF_8);
        }

        // Set up transformation configuration.

        final Map<String, Object> vars = new TreeMap<>();
        final String domainName = "TestDomain";
        vars.put("domainName", domainName);
        vars.put("fileName", tempDomainsFile);

        // Run the transformation.

        plugin.transform(testXsltTransformationTestConfig,
            FileTransformationPlugin.ConfigurationFormat.XML, vars);

        // Prepare XPath expressions for test.

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression xpathExpression
            = xpath.compile(String.format("/domains/domain[@name='%s']", domainName));

        // Load the results of the transformation.

        Document doc = XmlUtils.openDocument(tempDomainsFile);

        // Check that the result does contain <domain name="TestDomain">.

        NodeList nl = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);
        assertNotNull(nl);
        assertTrue(nl.getLength() != 0);

    }
}
