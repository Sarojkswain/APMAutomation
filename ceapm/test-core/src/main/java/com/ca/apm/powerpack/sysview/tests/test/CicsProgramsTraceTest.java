/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.powerpack.sysview.tests.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlow;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext;
import com.ca.apm.automation.action.flow.mainframe.ConfigureEncodedFlowContext.Builder;
import com.ca.apm.automation.action.test.Clw;
import com.ca.apm.automation.utils.CommonUtils;
import com.ca.apm.automation.utils.mainframe.Transactions;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CicsTestDriverRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmCtgTransactionTestbed;
import com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml.ConfigGenerator;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.powerpack.sysview.config.TransformerConfig;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.equinox.weaving.internal.caching.Log;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Test CICS Transaction Programs trace */
@Test(groups = TestClassification.SMOKE)
@Tas(testBeds = @TestBed(name = CeapmCtgTransactionTestbed.class,
    executeOn = CeapmCtgTransactionTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM)
public class CicsProgramsTraceTest extends TasTestNgTest {
    private static final Logger logger = LoggerFactory.getLogger(CicsProgramsTraceTest.class);
    private final String ceapmHome = envProperties.getRolePropertyById(
        CeapmCtgTransactionTestbed.CEAPM.getRole(), CeapmRole.CEAPM_HOME_PROPERTY);
    
    private static final int GENERATION_DURATION = 1; // [minutes]
    private static final int GENERATION_DELAY = 5_000; // [ms]
    private static final String CICS_PROGRAM_NAME = "CALLPROG";
    private static final String CICS_NONEXISTENT_PROGRAM = "CALL_ERR";
    private static final int CALLPRO1_DELAY = 300_000; // [micro-s]
    private static final String CICS_DEFINITION_1 =
        "[" + "{count:3,program:CALLPRO1,subActions:[{delay:" + CALLPRO1_DELAY + "}]},"
            + "{count:3,program:" + CICS_NONEXISTENT_PROGRAM + "}," 
            + "{program:CALLNONE,sysId:NONE},"
            + "{abend:AEY1}"
            + "]";
    private static final String CICS_DEFINITION_2 =
        "[" + "{abend:AEY2}" + "]";
    private static final String CICS_DEFINITION_3 =
        "[" + "{program:" + CICS_NONEXISTENT_PROGRAM + "}," 
            + "{program:CALLPRO1},"
            + "{program:CALLPRO2},"
            + "{program:CALLPRO3}"
            + "]";
    private static final Pattern PROGRAM_TRACE_PATH =
        Pattern.compile("CICS Regions\\|[^|]*\\|[^|]*\\|[^|]*\\|Programs");

    @BeforeClass
    public void startup() throws IOException {
        CeapmRole.startAgent(aaClient, envProperties, CeapmCtgTransactionTestbed.CEAPM.getRole(),
            null, true);
    }

    /**
     * Verifies Transaction Programs trace content, using default configuration.
     */
    @Test(priority = 10)
    public void defaultConfigTest() throws Exception {

        // Generates transactions and collects traces to test
        Document doc = generateAndCaptureTraces(
            Arrays.asList(CICS_DEFINITION_1, CICS_DEFINITION_2), "DefaultConfig");

        // Extract programs component section
        NodeList programs = findProgramsComponent(doc);

        assertNotNull(programs, "Programs TT Component");
        assertEquals(programs.getLength(), 4, "Programs component number");

        // Loop through Program components
        for (int i = 0; i < programs.getLength(); ++i) {
            assertEquals(programs.item(i).getNodeType(), Node.ELEMENT_NODE);
            final Element program = (Element) programs.item(i);
            final String componentType = assertAndGetAttribute(program, "ComponentType");
            final String componentName = assertAndGetAttribute(program, "ComponentName");
            final String metricPath = assertAndGetAttribute(program, "MetricPath");
            final String duration = assertAndGetAttribute(program, "Duration");
            final String relativeTimestamp = assertAndGetAttribute(program, "RelativeTimestamp");
            
            assertEquals(componentType, "Program", "Component Type");
            assertTrue(NumberUtils.isNumber(duration) && Integer.parseInt(duration) > 0,
                "Duration is positive number");
            assertTrue(
                NumberUtils.isNumber(relativeTimestamp) && Integer.parseInt(relativeTimestamp) >= 0,
                "RelativeTimestamp is not negative number");
            
            // Access Parameters
            final NodeList parameters = program.getElementsByTagName("Parameter");
            final Map<String, String> parametersMap = new HashMap<>();
            for (int j = 0; j < parameters.getLength(); ++j) {
                assertEquals(parameters.item(j).getNodeType(), Node.ELEMENT_NODE);
                final Element parameter = (Element) parameters.item(j);
                final String name = assertAndGetAttribute(parameter, "Name");
                final String value = assertAndGetAttribute(parameter, "Value");
                parametersMap.put(name, value);
            }
            // Verify program attributes
            assertTrue(parametersMap.containsKey("Average Time"), "Average Time attribute exists");
            assertTrue(parametersMap.containsKey("DataCreationType"), "DataCreationType attribute exists");
            assertTrue(parametersMap.containsKey("Program Name"), "Program Name attribute exists");
            assertTrue(parametersMap.containsKey("Total Time"), "Total Time attribute exists");
            assertTrue(parametersMap.containsKey("Errors"), "Errors attribute exists");
            assertTrue(parametersMap.containsKey("Request Count"), "Request Count attribute exists");
            
            assertTrue(NumberUtils.isNumber(parametersMap.get("Errors")), "Errors is number");
            assertTrue(NumberUtils.isNumber(parametersMap.get("Request Count")), "Request Count is number");

            String ats = parametersMap.get("Average Time"); 
            String atn = ats.substring(0, ats.length() - 3); // 3 means time unit suffix
            assertTrue(NumberUtils.isNumber(atn), "Average Time is number based");
            final int averageTime = Integer.parseInt(atn);
            
            String tts = parametersMap.get("Total Time"); 
            String ttn = tts.substring(0, tts.length() - 3);  // 3 means time unit suffix
            assertTrue(NumberUtils.isNumber(ttn), "Total Time is number based");
            final int totalTime = Integer.parseInt(ttn);

            // Verify specific program values
            assertEquals(parametersMap.get("Program Name"), componentName, "Program Name");
            switch (i) {
                case 0:
                    assertEquals(parametersMap.get("Program Name"), "CALLPROG", "Program Name");
                    assertTrue(parametersMap.containsKey("Exception"));
                    assertEquals(parametersMap.get("Exception"), "Abends: AEY1, AEY2", "Exception");
                    break;
                case 1:
                    assertEquals(parametersMap.get("Program Name"), "CALLPRO1", "Program Name");
                    assertEquals(metricPath, "Program|CALLPRO1","Metric Path");
                    assertEquals(parametersMap.get("Request Count"), "3", "Request Count");

                    assertTrue(CALLPRO1_DELAY <= averageTime
                            && averageTime <= CALLPRO1_DELAY + CALLPRO1_DELAY / 10,
                        "Average Time (" + averageTime + ") fits program defined delay (" 
                            + CALLPRO1_DELAY + ")");

                    int totalTimeExpected =
                        Integer.parseInt(parametersMap.get("Request Count")) * averageTime;
                    assertTrue(totalTimeExpected - 1 <= totalTime && totalTime <= totalTimeExpected + 1,
                        "Total Time (" + totalTime
                            + ") doesn't match expected multiple of Average Time (" + averageTime + ")");
                    break;
                case 2:
                    assertEquals(parametersMap.get("Program Name"), "CALL_ERR", "Program Name");
                    assertEquals(parametersMap.get("Errors"), "3", "Errors");
                    break;
                case 3:
                    assertEquals(parametersMap.get("Program Name"), "CALLNONE", "Program Name");
                    assertEquals(metricPath, "Program|NONE|CALLNONE", "Metric Path");
                    assertTrue(parametersMap.containsKey("Remote SysId"));
                    assertEquals(parametersMap.get("Remote SysId"), "NONE", "Remote SysId");
                    break;
            }
        }
    }
    
     /**
     * Verifies programs limit setting. Usecases: 1. Exact limit 2. Abend 3. No limit   
     */
    @Test(priority = 20)
    public void programsLimitTest() throws Exception {
        HashMap<String, String> cfgChanges = new HashMap<>();

        // Set exact programs number limit   
        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED, "3");
        configureChanges(cfgChanges);
        
        // Generate transactions and collect traces to test
        Document doc = generateAndCaptureTraces(
            Collections.singletonList(CICS_DEFINITION_3), "ProgramsLimitExact");
        NodeList programs = findProgramsComponent(doc);
        
        // Test if TTs exist and number of programs is limited 
        assertNotNull(programs, "Programs TT Component");
        assertEquals(programs.getLength(), 3, "Programs component number");
    
        // Generate transactions with abend
        doc = generateAndCaptureTraces(
            Collections.singletonList(CICS_DEFINITION_1), "ProgramsLimitAbend");
        programs = findProgramsComponent(doc);
        
        // Test if TTs exist and contains generated number of programs (not limited) 
        assertNotNull(programs, "Programs TT Component");
        assertEquals(programs.getLength(), 4, "Programs component number");
    
        // Set programs unlimited 
        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED, "0");
        configureChanges(cfgChanges);
        
        // Generate transactions and collect traces to test
        doc = generateAndCaptureTraces(
            Collections.singletonList(CICS_DEFINITION_3), "ProgramsLimitNone");
        programs = findProgramsComponent(doc);
    
        // Test if TT exists and contains generated number of programs  
        assertNotNull(programs, "Programs TT Component");
        assertEquals(programs.getLength(), 5, "Programs component number");
    }

    /**
     * Verifies system programs setting  
     */
    @Test(priority = 30)
    public void systemProgramsTest() throws Exception {
        HashMap<String, String> cfgChanges = new HashMap<>();
    
        // Configure to include system programs
        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM, "yes");
        configureChanges(cfgChanges);
        
        // Generate transactions and collect traces to test
        Document doc = generateAndCaptureTraces(
            Collections.singletonList(CICS_DEFINITION_3), "SystemPrograms");
        NodeList programs = findProgramsComponent(doc);
    
        assertNotNull(programs, "Programs TT Component");
    
        boolean systemProgramFound = false;
        for (int k = 0; k < programs.getLength(); ++k) {
            assertEquals(programs.item(k).getNodeType(), Node.ELEMENT_NODE);
            final Element program = (Element) programs.item(k);
            final String programName = assertAndGetAttribute(program, "ComponentName");
            if (programName.startsWith("DFH")) {
                systemProgramFound = true;
                break;
            }
        }
        // Test if system programs included in TT 
        assertTrue(systemProgramFound, "System program DFH* found in TT");
    }

    /**
     * Verifies program traces on/off configuration  
     */
    @Test(priority = 90)
    public void tracesOffTest() throws Exception {
        final HashMap<String, String> cfgChanges = new HashMap<>();

        // Configure program traces off
        cfgChanges.put(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE, "no");
        configureChanges(cfgChanges);
        
        // Generates transactions and collects traces to test
        Document doc = generateAndCaptureTraces(
            Collections.singletonList(CICS_DEFINITION_3), "TracesOff");
        
        // Extract programs component section if exists
        NodeList programs = findProgramsComponent(doc);
        assertTrue(programs == null, "No Programs TT generated");
    }

    /**
     * Changes configuration of agent
     * 
     * @param cfgChanges name/value list of config properties   
     */
    private void configureChanges(Map<String, String> cfgChanges) {
        logger.debug("Profile config changes: " + cfgChanges);
        String filename = ceapmHome + CeapmRole.CEAPM_PROPERTIES_FILE;
        
        Builder builder =
            new ConfigureEncodedFlowContext.Builder().encoding(CeapmRole.CEAPM_PROPERTIES_ENCODING);
        builder.configurationMap(filename, cfgChanges);
        ConfigureEncodedFlowContext context = builder.build();
        runFlowByMachineId(CeapmCtgTransactionTestbed.MF_MACHINE_ID, ConfigureEncodedFlow.class, context);
    }
    
    /**
     * Generates transactions with CICS programs defined here and captures EM traces
     * 
     * @param cicsProgramDefinitions CICS program sequence to be invoked (CALLPROG commarea parameter)
     * @param tempFileSuffix suffix to distinguish temp files from differrent tests (ctd xml and trace capture)  
     * @return EM captured TTs XML   
     */
    private Document generateAndCaptureTraces(List<String> cicsProgramDefinitions, String tempFileSuffix) throws Exception {
        assert(cicsProgramDefinitions != null && !cicsProgramDefinitions.isEmpty());
        final int captureDuration = GENERATION_DURATION * 60 + 180; // [s] 
        final String ctdDir = envProperties.getRolePropertyById(CeapmCtgTransactionTestbed.CTD_ROLE_ID,
            CicsTestDriverRole.INSTALL_DIR_PROPERTY);
        final String emLibDir = envProperties.getRolePropertyById(
            CeapmCtgTransactionTestbed.EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        final String ctdDefinitionFile =  "generated" + tempFileSuffix + ".xml";

        // Generate CTD config files
        ConfigGenerator cfgGenerator = new ConfigGenerator(
            "tcp://" + envProperties.getMachineHostnameByRoleId(CeapmCtgTransactionTestbed.CTG_ROLE_ID),
            2006, CeapmCtgTransactionTestbed.CICS.getIpicId(), GENERATION_DURATION,
            GENERATION_DELAY * 1_000, 1, cicsProgramDefinitions.get(0));
        for (String definition : cicsProgramDefinitions) {
            logger.debug("CICS program definition used: " + definition);
            cfgGenerator.addProgramCall(CICS_PROGRAM_NAME, definition);
        }
        cfgGenerator.generate(ctdDir + "\\xml\\mapping.xml", ctdDir + "\\xml\\generated" + tempFileSuffix + ".xml");
        
        // TT capture thread
        final List<Document> transactionData = new ArrayList<>(1);
        final Thread capture = new Thread(new Runnable() {
            @Override
            public void run() {
                Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();

                logger.info("Started TT capture");
                Document doc = clw.getTransactions(".*", captureDuration);
                logger.info("Finished TT capture");

                transactionData.add(doc);
            }
        });

        // Start capture
        capture.start();

        // Generate transactions
        Transactions.generateCtgCics(ctdDir, ctdDefinitionFile, false);

        // Wait for capture to finish
        logger.info("Waiting for TT capture to finish");
        capture.join();

        assertEquals(transactionData.size(), 1);
        Document doc = transactionData.get(0);

        // Write the whole unmodified XML to a file for potential analysis.
        CommonUtils.saveDocumentToFile(doc, new File("test-output/capturedTraces" + tempFileSuffix + ".xml"));

        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Looks for first Programs TT containing "marking" nonexistent program 
     * 
     * @param doc captured traces
     * @return TT Programs component section to test
     */
    private NodeList findProgramsComponent(Document doc) {
        NodeList ttNodes = doc.getElementsByTagName("TransactionTrace");
        NodeList foundPrograms = null;
    
        // loop through TTs
        for (int ttIdx = 0; ttIdx < ttNodes.getLength(); ++ttIdx) {
            assertEquals(ttNodes.item(ttIdx).getNodeType(), Node.ELEMENT_NODE);
            final Element tt = (Element) ttNodes.item(ttIdx);
    
            // get top component(s)
            final NodeList ccNodes = tt.getElementsByTagName("CalledComponent");
            for (int j = 0; j < ccNodes.getLength(); ++j) {
                assertEquals(ccNodes.item(j).getNodeType(), Node.ELEMENT_NODE);
                final Element cc = (Element) ccNodes.item(j);
                final String componentType = assertAndGetAttribute(cc, "ComponentType");
                final String componentName = assertAndGetAttribute(cc, "ComponentName");
                final String metricPath = assertAndGetAttribute(cc, "MetricPath");
    
                // filter to Programs only
                if (PROGRAM_TRACE_PATH.matcher(metricPath).matches()) {
                    Log.debug("Found TT component CICS Regions|.*|Programs");
                    assertEquals(componentType, "CICS Regions", "Component Type");
                    assertEquals(componentName, "Programs", "Component Name");
    
                    // get program subcomponents
                    final NodeList programs = cc.getElementsByTagName("CalledComponent");
                    for (int k = 0; k < programs.getLength(); ++k) {
                        assertEquals(programs.item(k).getNodeType(), Node.ELEMENT_NODE);
                        final Element program = (Element) programs.item(k);
                        final String programName = assertAndGetAttribute(program, "ComponentName");
    
                        // check for marker program name (to avoid test false TTs)
                        // the last matching TT will be returned (to avoid overlap with previous series) 
                        if (programName.equals(CICS_NONEXISTENT_PROGRAM)) {
                            foundPrograms =  programs;
                        }
                    }
                }
            }
        }
        return foundPrograms;
    }

    /**
     * Checks that an XML Element contains an attribute and returns its value.
     *
     * @param element XML Element.
     * @param attribute Attribute name.
     * @return Attribute value.
     */
    private String assertAndGetAttribute(Element element, String attribute) {
        assertTrue(element.hasAttribute(attribute));
        return element.getAttribute(attribute);
    }
}
