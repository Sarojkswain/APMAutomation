package com.ca.apm.systemtest.fld.plugin.prelert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.XmlUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by haiva01 on 26.6.2015.
 */
public class PrelertPluginImplTest {
    private Logger log = LoggerFactory.getLogger(PrelertPluginImplTest.class);

    private static String engineXmlContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!-- engine configuration file -->\n"
        + "<engine xmlns:xi=\"http://www.w3.org/2003/XInclude\" xml:base=\"S:\\sw\\CA "
        + "Technologies\\APM Analysis Server\\config\\\">\n"
        + "  <!-- Specify a port to receive evidence on -->\n"
        + "  <evidence_tcp_port>49997</evidence_tcp_port>\n"
        + "  <!-- Specify a host and port to send activities to when they're ready\n"
        + "         to be written to the database -->\n"
        + "  <tcp_server>\n"
        + "    <host_name>127.0.0.1</host_name>\n"
        + "    <port>49995</port>\n"
        + "  </tcp_server>\n"
        + "  <!-- The connection to the database -->\n"
        + "  <xi:include href=\"dbconfig.xml\" />\n"
        + "  <!-- Correlator config -->\n"
        + "  <analysisconfig>\n"
        + "    <!-- Introscope data lags about 4 minutes behind real-time,\n"
        + "             hence the 5 minute buffer delay here -->\n"
        + "    <bufferdelay>300</bufferdelay>\n"
        + "    <bufferflushinterval>5</bufferflushinterval>\n"
        + "    <sakoechibabandsize>60</sakoechibabandsize>\n"
        + "    <learnonlytime>300</learnonlytime>\n"
        + "    <lowerbound>\n"
        + "      <bucket_size>300</bucket_size>\n"
        + "      <number_of_buckets>288</number_of_buckets>\n"
        + "      <threshold>0.5</threshold>\n"
        + "    </lowerbound>\n"
        + "    <seriescounter>\n"
        + "      <span>172800</span>\n"
        + "      <significance_threshold>0.51</significance_threshold>\n"
        + "      <purge_interval>5400</purge_interval>\n"
        + "      <purge_threshold>0.25</purge_threshold>\n"
        + "    </seriescounter>\n"
        + "    <attributematch>\n"
        + "      <attributes>\n"
        + "        <attribute>source</attribute>\n"
        + "        <attribute>Process</attribute>\n"
        + "        <attribute>Agent</attribute>\n"
        + "        <attribute>ResourcePath0</attribute>\n"
        + "        <attribute>ResourcePath1</attribute>\n"
        + "        <attribute>ResourcePath2</attribute>\n"
        + "        <attribute>ResourcePath3</attribute>\n"
        + "        <attribute>ResourcePath4</attribute>\n"
        + "        <attribute>ResourcePath5</attribute>\n"
        + "        <attribute>ResourcePath6</attribute>\n"
        + "        <attribute>ResourcePath7</attribute>\n"
        + "      </attributes>\n"
        + "      <max_simultaneous_attributes>2</max_simultaneous_attributes>\n"
        + "      <excluder>time_series_id</excluder>\n"
        + "    </attributematch>\n"
        + "    <!-- SQL to read in granularities - must return 3 columns, namely type,\n"
        + "             metric and granularity -->\n"
        + "    <granularityLookupSql>SELECT\n"
        + "                type,\n"
        + "                metric,\n"
        + "                usual_interval AS granularity\n"
        + "            FROM\n"
        + "                time_series_type</granularityLookupSql>\n"
        + "    <minActivityTimeSql>SELECT\n"
        + "                COALESCE(EXTRACT(EPOCH FROM MAX(min_time)), 0) AS min_time\n"
        + "            FROM\n"
        + "                evidence_link_min_time</minActivityTimeSql>\n"
        + "  </analysisconfig>\n"
        + "</engine>\n"
        + "\n";

    @Test
    public void testEngineXmlModification() throws IOException, XPathExpressionException,
        ParserConfigurationException, SAXException {
        PrelertPluginImpl plugin = new PrelertPluginImpl();
        assertNotNull(plugin);

        // Store engine.xml

        File installDir = ACFileUtils.createTemporaryDirectory("prelert");
        installDir.deleteOnExit();
        File configDir = new File(installDir, "config");
        configDir.deleteOnExit();
        File engineXmlFile = new File(configDir, "engine.xml");
        engineXmlFile.deleteOnExit();
        FileUtils.forceMkdir(configDir);
        try (OutputStream outputStream = FileUtils.openOutputStream(engineXmlFile)) {
            IOUtils.write(engineXmlContents, outputStream, StandardCharsets.UTF_8);
        }
        log.info("Test engine.xml stored to {}.", engineXmlFile.getAbsolutePath());

        // Try to use it.

        PrelertPlugin.Configuration configuration = new PrelertPlugin.Configuration();
        configuration.prelertInstallDir = installDir.getAbsolutePath();
        plugin.setConfigFile(configuration);

        // Prepare XPath expressions for test.

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression xpathExpression = xpath
            .compile("/engine/analysisconfig/learnonlytime/text()");

        // Load the results of the transformation.

        Document doc = XmlUtils.openDocument(engineXmlFile);

        // Check that the result is set to value 600.

        NodeList nl = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);
        assertNotNull(nl);
        assertTrue(nl.getLength() != 0);
        String val = nl.item(0).getNodeValue();
        assertEquals(val, "600");
    }
}