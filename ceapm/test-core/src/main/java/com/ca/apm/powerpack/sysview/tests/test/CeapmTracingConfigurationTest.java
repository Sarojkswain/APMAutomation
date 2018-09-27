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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.ca.apm.automation.utils.LogScanner;
import com.ca.apm.automation.utils.mainframe.EncodedPropertiesConfiguration;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmConfigurationTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.install.KIntroscopeConfigConstants;
import com.wily.powerpack.sysview.config.TransformerConfig;

import org.apache.commons.io.FileUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Test TT configuration options related to AppMap support.
 */
@Test(groups = TestClassification.FULL, singleThreaded = true)
@Tas(testBeds = @TestBed(name = CeapmConfigurationTestbed.class,
    executeOn = CeapmConfigurationTestbed.MF_MACHINE_ID), size = SizeType.MEDIUM)
public class CeapmTracingConfigurationTest extends TasTestNgTest {

    private static final Logger log = LoggerFactory.getLogger(CeapmTracingConfigurationTest.class);
    // following are configured in profile file, and in effect only when 'logfile' logger is enabled
    public static final String CEAPM_LOG_ENCODING = "Cp1047";
    public static final String CEAPM_LOG_FILE = "logs/Cross-Enterprise_APM.log";

    /** Configuration properties backup file suffix. */
    private static final String BACKUP_SUFFIX = ".backup";

    /** Configuration is reloaded only every so often. */
    public static final int TRANSFORMER_RELOAD_DELAY = 60;
    /**
     * Maximum delay [seconds] between application of configuration and seeing response in logs.
     * Must accommodate {@link #TRANSFORMER_RELOAD_DELAY} and some extra.
     */
    private int maxChangeDelay = TRANSFORMER_RELOAD_DELAY + 10;

    /** Agent home directory */
    private final String ceapmHome;
    /** Properties file for the tested agent. */
    private final File ceapmProps;
    /** Handle that configures properties. */
    private final EncodedPropertiesConfiguration properties;
    /** Log file for the tested agent. */
    final File ceapmLog;
    /** Configuration changes to be performed in properties file before launching the test. */
    private final HashMap<String, String> cfgChanges = new HashMap<>();
    /** Text fragments that are expected to appear after the configuration changes are made. */
    private final LinkedList<String> expected = new LinkedList<>();
    /** Text fragments that are unexpected after the configuration changes are made. */
    private final LinkedList<String> unexpected = new LinkedList<>();

    /**
     * Constructor.
     *
     * @throws Exception Throws on unexpected error.
     */
    protected CeapmTracingConfigurationTest() throws Exception {
        ceapmHome =
            envProperties.getRolePropertyById(CeapmConfigurationTestbed.CEAPM.getRole(),
                CeapmRole.CEAPM_HOME_PROPERTY);
        Args.notBlank(ceapmHome, "home");

        ceapmLog = new File(ceapmHome, CEAPM_LOG_FILE);

        ceapmProps = new File(ceapmHome, CeapmRole.CEAPM_PROPERTIES_FILE);
        assertTrue(ceapmProps.isFile());
        assertFalse(Paths.get(ceapmProps.toPath() + BACKUP_SUFFIX).toFile().isFile());
        properties =
            new EncodedPropertiesConfiguration(ceapmProps, CeapmRole.CEAPM_PROPERTIES_ENCODING);
        properties.setThrowExceptionOnMissing(false);
    }

    @BeforeClass
    private void startup() throws IOException {
        CeapmRole.startAgent(envProperties, CeapmConfigurationTestbed.CEAPM.getRole(), null, true);
        assertTrue(ceapmLog.isFile()); // exists
        backupFile(ceapmProps, false);
    }

    @BeforeMethod
    private void init() throws IOException {
        expected.clear();
        unexpected.clear();
        cfgChanges.clear();
    }

    @AfterMethod
    private void cleanup() throws IOException {
        backupFile(ceapmProps, true);
    }

    /**
     * Backup or restore file.
     *
     * @param file Original file.
     * @param restore <code>true</code> to copy backup to original, <code>false</code> to copy
     *        original to backup.
     * @throws IOException on error
     */
    private static void backupFile(File file, boolean restore) throws IOException {
        Path original = file.toPath();
        Path backup = Paths.get(file.toPath() + BACKUP_SUFFIX);
        if (restore) {
            log.debug("restoring" + backup + " -> " + original);
            Files.copy(backup, original, REPLACE_EXISTING);
        } else {
            log.debug("backing up" + original + " -> " + backup);
            Files.copy(original, backup, REPLACE_EXISTING);
        }
    }

    // Default values.
    @Test(priority = 0)
    public void defaultValues() throws Exception {
        String propData =
            FileUtils.readFileToString(ceapmProps, CeapmRole.CEAPM_PROPERTIES_ENCODING);

        assertTrue(propData.contains(TransformerConfig.kTransactionTraceUniqueSelectionRatio));
        expected.add("The Unique Selection Ratio value is now " + TransformerConfig.transactionTraceUniqueSelectionRatioDefaultValue);

        assertFalse(propData.contains(TransformerConfig.kTransactionTraceSelectionPeriod));
        expected.add("The Selection Period value is now " + TransformerConfig.transactionTraceSelectionPeriodDefaultValue);

        assertFalse(propData.contains(TransformerConfig.kTransactionTraceSelectionBufferSize));
        expected.add("The Selection Buffer Max Size value is now " + TransformerConfig.transactionTraceSelectionBufferSizeDefaultValue);

        assertFalse(propData.contains(TransformerConfig.kTransactionTraceSignatureBufferMaxAge));
        expected.add("The Signature Buffer Max Age value is now " + TransformerConfig.transactionTraceSignatureBufferMaxAgeDefaultValue);

        assertFalse(propData.contains(TransformerConfig.kTransactionTraceInputBufferSize));
        expected.add("The Input Queue Max Size value is now " + TransformerConfig.transactionTraceInputBufferSizeDefaultValue);

        assertTrue(propData.contains(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE));
        expected.add(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE + " is set to " + TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE_DEFAULT);

        assertTrue(propData.contains(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED));
        expected.add(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED + " is set to " + TransformerConfig.CICS_PROGRAMS_MAX_REPORTED_DEFAULT);

        assertFalse(propData.contains(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM));
        expected.add(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM + " is set to " + TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM_DEFAULT);

        /**
         * DE166525 antiflood threshold is replaced by TT clamp, see
         * {@link SyntheticSmfTest#regressionDe166525()}
         */
        final File ceapmProfile = new File(ceapmHome, CeapmRole.CEAPM_PROFILE_FILE);
        assertTrue(ceapmProfile.isFile());
        final EncodedPropertiesConfiguration profile =
            new EncodedPropertiesConfiguration(ceapmProfile, CeapmRole.CEAPM_PROPERTIES_ENCODING);
        profile.setThrowExceptionOnMissing(false);

        String profileData =
            FileUtils.readFileToString(ceapmProfile, CeapmRole.CEAPM_PROPERTIES_ENCODING);
        assertFalse(profileData
            .contains(KIntroscopeConfigConstants.kAgentTransactionTracerReportingLimitConfigKey));
        assertTrue(profileData.contains(KIntroscopeConfigConstants.kAgentTransactionTracerClamp));
        assertEquals(
            profile.getInteger(KIntroscopeConfigConstants.kAgentTransactionTracerClamp, null),
            new Integer(150));

        configureAndCheckMessages(false);
    }

    // Explicit values.
    @Test(priority = 1)
    public void explicitValues() throws Exception {
        cfgChanges.put(TransformerConfig.kTransactionTraceUniqueSelectionRatio, "10");
        expected.add("The Unique Selection Ratio value is now 10");

        cfgChanges.put(TransformerConfig.kTransactionTraceSelectionPeriod, "20");
        expected.add("The Selection Period value is now 20");

        cfgChanges.put(TransformerConfig.kTransactionTraceSelectionBufferSize, "30");
        expected.add("The Selection Buffer Max Size value is now 30");

        cfgChanges.put(TransformerConfig.kTransactionTraceSignatureBufferMaxAge, "40");
        expected.add("The Signature Buffer Max Age value is now 40");

        cfgChanges.put(TransformerConfig.kTransactionTraceInputBufferSize, "50");
        expected.add("The Input Queue Max Size value is now 50");

        cfgChanges.put(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE, "no");
        expected.add(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE + " is set to no");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED, "75");
        expected.add(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED + " is set to 75");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM, "yes");
        expected.add(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM + " is set to yes");

        configureAndCheckMessages(true);

        init();

        cfgChanges.put(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE, "yes");
        expected.add(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE + " is set to yes");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM, "no");
        expected.add(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM + " is set to no");

        configureAndCheckMessages(true);
    }

    // Invalid values with fall-back to default values.
    @Test(priority = 2)
    public void invalidValues() throws Exception {
        cfgChanges.put(TransformerConfig.kTransactionTraceUniqueSelectionRatio, "200");
        expected.add("The Unique Selection Ratio value is now " + TransformerConfig.transactionTraceUniqueSelectionRatioDefaultValue);

        cfgChanges.put(TransformerConfig.kTransactionTraceSelectionPeriod, "0");
        expected.add("The Selection Period value is now " + TransformerConfig.transactionTraceSelectionPeriodDefaultValue);

        cfgChanges.put(TransformerConfig.kTransactionTraceSelectionBufferSize, "0");
        expected.add("The Selection Buffer Max Size value is now " + TransformerConfig.transactionTraceSelectionBufferSizeDefaultValue);

        cfgChanges.put(TransformerConfig.kTransactionTraceSignatureBufferMaxAge, "0");
        expected.add("The Signature Buffer Max Age value is now " + TransformerConfig.transactionTraceSignatureBufferMaxAgeDefaultValue);

        cfgChanges.put(TransformerConfig.kTransactionTraceInputBufferSize, "0");
        expected.add("The Input Queue Max Size value is now " + TransformerConfig.transactionTraceInputBufferSizeDefaultValue);

        cfgChanges.put(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE, "999");
        expected.add(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE + " value 999 is not yes/no. Setting the default value of \""  + TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE_DEFAULT + "\".");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED, "maybe");
        expected.add(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED + " value maybe is not an integer. Setting the default value of " + TransformerConfig.CICS_PROGRAMS_MAX_REPORTED_DEFAULT);

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM, "999");
        expected.add(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM + " value 999 is not yes/no. Setting the default value of \"" + TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM_DEFAULT + "\".");

        configureAndCheckMessages(true);
    }

    // Missing values with fall-back to default values.
    @Test(priority = 3)
    public void missingValues() throws Exception {
        cfgChanges.put(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE, null);
        expected.add(TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE + " Failed to get the value from the property file. Setting the default value of \"" + TransformerConfig.GENERATE_CICS_PROGRAMS_TRACE_DEFAULT + "\".");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED, null);
        expected.add(TransformerConfig.CICS_PROGRAMS_MAX_REPORTED + " Failed to get the value from the property file. Setting the default value of \"" + TransformerConfig.CICS_PROGRAMS_MAX_REPORTED_DEFAULT + "\".");

        cfgChanges.put(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM, null);
        unexpected.add(TransformerConfig.CICS_PROGRAMS_INCLUDE_SYSTEM + " Failed to get the value from the property file.");

        configureAndCheckMessages(true);
    }

    /**
     * Register lines that are expected after configuration change.
     * Open log, perform the configuration changes and start watching for the expected log
     * lines until timeout.
     *
     * @param onlyNew <code>true</code> to look only for messages after change, <code>false</code>
     *        to expect fresh agent start
     * @throws Exception on any error
     */
    private void configureAndCheckMessages(boolean onlyNew) throws Exception {
        try {
            log.debug("=== start {}", new Date());
            final LogScanner listener =
                new LogScanner(ceapmLog, Charset.forName(CEAPM_LOG_ENCODING), expected,
                    unexpected, onlyNew);
            properties.configure(cfgChanges);
            log.debug("configured:" + cfgChanges);
            log.debug("expected:" + expected);
            log.debug("unexpected:" + unexpected);
            listener.awaitLines(maxChangeDelay);
            assertEquals(expected.size(), 0, "Log messages not found:" + expected);
            assertEquals(unexpected.size(), 0, "Unexpected messages found:" + unexpected);
        } finally {
            log.debug("=== end {}", new Date());
            log.debug(FileUtils.readFileToString(ceapmProps, CeapmRole.CEAPM_PROPERTIES_ENCODING)
                .replaceAll("(?m)^#.*?\n", "").replaceAll("(?m)^[ \t]*\r?\n", ""));
        }
    }
}