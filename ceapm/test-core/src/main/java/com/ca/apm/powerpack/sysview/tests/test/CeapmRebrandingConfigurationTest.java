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
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.ca.apm.automation.utils.LogScanner;
import com.ca.apm.automation.utils.mainframe.EncodedPropertiesConfiguration;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmConfigurationTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

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
import java.util.Random;


/**
 * Test of DB2 related configuration options and their rebranding from Insight to SYSVDB2.
 */
@Test(groups = TestClassification.FULL, singleThreaded = true)
@Tas(testBeds = @TestBed(name = CeapmConfigurationTestbed.class,
    executeOn = CeapmConfigurationTestbed.MF_MACHINE_ID), size = SizeType.MEDIUM)
public class CeapmRebrandingConfigurationTest extends TasTestNgTest {

    private static final Logger log = LoggerFactory
        .getLogger(CeapmRebrandingConfigurationTest.class);
    private static final Random rand = new Random(1);
    // following are configured in profile file, and in effect only when 'logfile' logger is enabled
    public static final String CEAPM_LOG_ENCODING = "Cp1047";
    public static final String CEAPM_LOG_FILE = "logs/Cross-Enterprise_APM.log";

    // properties
    private static final String INSIGHT_PFX = "Insight.";
    private static final String INSIGHT_DB2_PFX = "Insight.DB2.";
    private static final String SYSVDB2_PFX = "SYSVDB2.";
    /** Password key - its value must never appear in logs. */
    private static final String PASSWORD = "Insight.password";

    /** Configuration properties backup file suffix. */
    private static final String BACKUP_SUFFIX = ".backup";

    /** Configuration is reloaded only every so often. */
    public static final int TRANSFORMER_RELOAD_DELAY = 60;
    /**
     * Maximum delay [seconds] between application of configuration and seeing response in logs.
     * Must accommodate {@link #TRANSFORMER_RELOAD_DELAY} and Insight metric poll interval, and some
     * extra.
     */
    private int maxChangeDelay = 0;

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
    /** Initial metric update delay configured in testbed. */
    private int defaultUpdate;
    /** Which combination of old and new keys are we currently testing. */
    private KeyTestMode keyTestMode;

    /**
     * Tests can either configure old keys starting with {@value #INSIGHT_PFX}, new keys using
     * {@value #SYSVDB2_PFX}, or both.
     */
    private enum KeyTestMode {
        OLD, NEW, BOTH
    }

    /**
     * Constructor.
     *
     * @throws Exception Throws on unexpected error.
     */
    protected CeapmRebrandingConfigurationTest() throws Exception {
        String ceapmHome =
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
        defaultUpdate = properties.getInt(CeapmRole.UPDATE_INTERVAL_PROPERTY);
        updateMaxChangeDelay(defaultUpdate);
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

    // check default behavior, execute before other tests
    @Test(priority = -2, groups = TestClassification.FULL)
    public void defaultConfig() throws Exception {
        // sanity check that configuration does not contain any old prefixes
        String propData =
            FileUtils.readFileToString(ceapmProps, CeapmRole.CEAPM_PROPERTIES_ENCODING);
        assertFalse(propData.contains(INSIGHT_PFX));
        assertFalse(propData.contains(INSIGHT_DB2_PFX));
        assertTrue(propData.contains(SYSVDB2_PFX));
        // defaults are applied
        expected.add("Collection of DB2 metrics is now turned off");
        expected.add("The metric update interval in seconds is: " + defaultUpdate);
        configureAndCheckMessages(false);
    }

    // execute second
    @Test(priority = -1, groups = TestClassification.FULL)
    public void enableDb2Metrics() throws Exception {
        keyTestMode = KeyTestMode.NEW;
        cfgCollectMetrics();
        expected.add("Collection of DB2 metrics is now turned on");
        expected.add("Connection properties are not configured properly: "
            + "Username is not specified. "
            + "Passticket security is enabled but passticket application name is not defined. "
            + "Port number 0 is invalid.  No connection shall be attempted.");
        configureAndCheckMessages(true);
    }

    // execute last
    @Test(priority = 99, groups = TestClassification.FULL)
    public void backToDefaults() throws Exception {
        keyTestMode = KeyTestMode.NEW;
        cfgCollectMetrics();
        // undocumented properties only use new key
        String enc = randomLetters(5);
        cfgChanges.put("SYSVDB2.codepage", enc);
        expected.add("The SYSVIEW for DB2 XNET will use custom string encoding SYSVDB2.codepage = "
            + enc);
        cfgChanges.put("SYSVDB2.error.reconnect.delay", "66");
        expected.add("Custom SYSVIEW for DB2 XNET error reconnect delay "
            + "(SYSVDB2.error.reconnect.delay) in seconds is: 66");
        configureAndCheckMessages(true);
        cfgChanges.put("SYSVDB2.error.reconnect.delay", null);
        expected.add("The SYSVIEW for DB2 XNET will use default error reconnect delay.");
        configureAndCheckMessages(true);
        cfgChanges.put("SYSVDB2.error.reconnect.delay", null);
        cfgChange("Insight.metrics.collect", "no", "Collection of DB2 metrics is now turned off");
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void oldConfiguration1() throws Exception {
        keyTestMode = KeyTestMode.OLD;
        cfg1();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void newConfiguration1() throws Exception {
        keyTestMode = KeyTestMode.NEW;
        cfg1();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void bothConfigurations1() throws Exception {
        keyTestMode = KeyTestMode.BOTH;
        cfg1();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void oldConfiguration2() throws Exception {
        keyTestMode = KeyTestMode.OLD;
        cfg2();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void newConfiguration2() throws Exception {
        keyTestMode = KeyTestMode.NEW;
        cfg2();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void bothConfigurations2() throws Exception {
        keyTestMode = KeyTestMode.BOTH;
        cfg2();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void oldConfiguration3() throws Exception {
        keyTestMode = KeyTestMode.OLD;
        cfg3();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void newConfiguration3() throws Exception {
        keyTestMode = KeyTestMode.NEW;
        cfg3();
        configureAndCheckMessages(true);
    }

    @Test(groups = TestClassification.FULL)
    public void bothConfigurations3() throws Exception {
        keyTestMode = KeyTestMode.BOTH;
        cfg3();
        configureAndCheckMessages(true);
    }

    private String randomLetters(int len) {
        return random(len, 0, 0, true, false, null, rand);
    }

    private HashMap<Integer, Integer> nextNumberMap = new HashMap<>();

    private String nextNumber(int base) {
        if (!nextNumberMap.containsKey(base)) {
            nextNumberMap.put(base, base);
        }
        return Integer.toString(nextNumberMap.put(base, nextNumberMap.get(base) + 1));
    }

    private void updateMaxChangeDelay(int updateInterval) {
        maxChangeDelay = TRANSFORMER_RELOAD_DELAY + updateInterval + 5;
    }

    // cfg1-cfg3 strives to cover all configuration options, separating conflicting options
    // DB2 metric collection must be turned on in order for other options to be considered

    private void cfg1() throws Exception {
        cfgUpdateInterval();
        cfgCollectMetrics();
        cfgConnectionPassticket();
        cfgSubsystems();
        cfgGroups();
        cfgRefreshInterval();
        cfgRefreshThreads();
        cfgUpdateThreads();
    }

    private void cfg2() throws Exception {
        cfgCollectMetrics();
        cfgConnectionPassword();
    }

    private void cfg3() throws Exception {
        cfgCollectMetrics();
        cfgConnectionBlankPass();
    }

    private void cfgCollectMetrics() throws Exception {
        cfgChange("Insight.metrics.collect", "yes");
    }

    private void cfgUpdateInterval() throws Exception {
        String newUpdateInterval = nextNumber(17);
        assertNotEquals(newUpdateInterval, defaultUpdate);
        cfgChange("Insight.update.interval", newUpdateInterval,
            "The metric update interval in seconds is: " + newUpdateInterval);
        updateMaxChangeDelay(Integer.parseInt(newUpdateInterval));
    }

    private void cfgConnectionPassticket() throws Exception {
        String host = randomLetters(10);
        cfgChange("Insight.connection.hostname", host);
        String port = nextNumber(1111);
        cfgChange("Insight.connection.port", port);
        String user = randomLetters(8);
        cfgChange("Insight.username", user);
        String pass = randomLetters(8);
        cfgChange("Insight.passticket.appl", pass);
        cfgChange("Insight.passticket.support", "yes");
        String dir = randomLetters(16);
        cfgChange("Insight.director", dir, String.format("The connection properties are: "
            + "Host=%s Port=%s User=%s PassticketApp=%s DirectorID=%s", host, port, user, pass,
            dir.toUpperCase()));
    }

    private void cfgConnectionPassword() throws Exception {
        String host = randomLetters(10);
        cfgChange("Insight.connection.hostname", host);
        String port = nextNumber(1111);
        cfgChange("Insight.connection.port", port);
        String user = randomLetters(8);
        cfgChange("Insight.username", user);
        String pass = randomLetters(8);
        cfgChange("Insight.password", pass);
        cfgChange("Insight.passticket.support", "no");
        String dir = randomLetters(16);
        cfgChange("Insight.director", dir, String.format(
            "The connection properties are: Host=%s Port=%s User=%s Password=**** DirectorID=%s",
            host, port, user, dir.toUpperCase()));
    }

    private void cfgConnectionBlankPass() throws Exception {
        String host = randomLetters(10);
        cfgChange("Insight.connection.hostname", host);
        String port = nextNumber(1111);
        cfgChange("Insight.connection.port", port);
        String user = randomLetters(8);
        cfgChange("Insight.username", user);
        cfgChange("Insight.password", "");
        cfgChange("Insight.passticket.support", "no");
        String dir = randomLetters(16);
        cfgChange(
            "Insight.director",
            dir,
            String.format("The connection properties are: "
                + "Host=%s Port=%s User=%s BlankPassword DirectorID=%s", host, port, user,
                dir.toUpperCase()));
    }

    private void cfgSubsystems() throws Exception {
        String list = randomLetters(4) + "," + randomLetters(4);
        cfgChange("Insight.DB2.subsystem.name.list", list, "The DB2 subsystem name filter is: "
            + list);
    }

    private void cfgGroups() throws Exception {
        String list = randomLetters(4) + "," + randomLetters(4);
        cfgChange("Insight.DB2.group.name.list", list,
            "The DB2 data sharing group name filter is: " + list);
    }

    private void cfgRefreshInterval() throws Exception {
        String interval = nextNumber(21);
        cfgChange("Insight.DB2.subsystem.refresh.interval", interval,
            "The DB2 refresh interval in seconds is: " + interval);
    }

    private void cfgRefreshThreads() throws Exception {
        String threads = nextNumber(3);
        cfgChange("Insight.DB2.subsystem.refresh.threads", threads,
            "The number of DB2 connection refresh threads is: " + threads);
    }

    private void cfgUpdateThreads() throws Exception {
        String threads = nextNumber(5);
        cfgChange("Insight.update.threads", threads,
            "The number of DB2 metric polling threads is: " + threads);
    }

    /**
     * Add configuration change and debug message expectations for old/new key(s) according to
     * {@link #keyTestMode}.
     * Set additional optional expectations.
     *
     * @param key Configuration key (Must use the old Insight. prefix).
     * @param value Configuration value.
     * @param expectedMessages Optional expected messages triggered by the configuration change.
     */
    private void cfgChange(String key, String value, String... expectedMessages) {
        final String newKey;
        if (key.startsWith(INSIGHT_DB2_PFX)) {
            newKey = key.replace(INSIGHT_DB2_PFX, SYSVDB2_PFX);
        } else if (key.startsWith(INSIGHT_PFX)) {
            newKey = key.replace(INSIGHT_PFX, SYSVDB2_PFX);
        } else {
            throw new IllegalArgumentException("altered key " + key + " must start with "
                + INSIGHT_PFX + "or" + INSIGHT_DB2_PFX);
        }
        if (value == null) {
            throw new IllegalArgumentException("value not specified");
        }
        if (keyTestMode == null) {
            throw new IllegalStateException("key test mode not set");
        }
        // clear text password must not be displayed
        if (key.equals(PASSWORD) && !value.isEmpty()) {
            unexpected.add(value);
        }
        switch (keyTestMode) {
            case NEW:
                // set value with new key, should be listed
                cfgChanges.put(newKey, value);
                if (!PASSWORD.equals(key)) {
                    expected.add(newKey + " is set to " + value);
                }
                // nothing about the old key should be listed
                unexpected.add(key);
                break;
            case OLD:
                // remove new key, it should NOT be listed
                cfgChanges.put(newKey, null);
                unexpected.add(newKey + " is set to " + value);
                // set old key, value should be listed, including information which key it is
                // fallback for
                cfgChanges.put(key, value);
                if (!PASSWORD.equals(key)) {
                    expected.add(newKey + " fallback to " + key);
                    expected.add(key + " is set to " + value);
                }
                break;
            case BOTH:
                // set value with new key, should be listed
                cfgChanges.put(newKey, value);
                if (!PASSWORD.equals(key)) {
                    expected.add(newKey + " is set to " + value);
                }
                // set old key, with modified value, it should NOT be used
                StringBuilder modValue = new StringBuilder(value);
                if (value.length() > 1) {
                    modValue.setCharAt(1, (char) (value.charAt(1) + 1));
                } else {
                    modValue.append('0');
                }
                cfgChanges.put(key, modValue.toString());
                unexpected.add(newKey + " fallback to ");
                unexpected.add(key + " is set to ");
                break;
            default:
                throw new IllegalStateException("invalid key test mode " + keyTestMode);
        }
        // add expectations set from caller
        for (String line : expectedMessages) {
            expected.add(line);
        }
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
                new LogScanner(ceapmLog, Charset.forName(CEAPM_LOG_ENCODING), expected, unexpected,
                    onlyNew);
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
