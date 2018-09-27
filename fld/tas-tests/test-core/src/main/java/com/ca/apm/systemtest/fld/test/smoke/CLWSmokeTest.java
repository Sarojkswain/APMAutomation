package com.ca.apm.systemtest.fld.test.smoke;

import static com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole.CLEANUP_CONTEXT_KEY;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.flow.ClwCleanupFlow;
import com.ca.apm.systemtest.fld.flow.ClwCleanupFlowContext;
import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.CLWSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Smoke test for the CLW transaction trace and historical queries load in FLD.
 * @author keyja01
 *
 */
@Tas(testBeds = {
    @TestBed(name = CLWSmokeTestbed.class, executeOn = FLDLoadConstants.CLW_MACHINE_ID)},
    size = SizeType.MEDIUM)
@Test
public class CLWSmokeTest extends TasTestNgTest implements FLDLoadConstants {
    private static final Logger log = LoggerFactory.getLogger(CLWSmokeTest.class);

    private Timer cleanupTimer;
    
    @BeforeClass
    public void setup() throws Exception {
        // start up wurlitzer
        runSerializedCommandFlowFromRoleAsync(WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, WurlitzerLoadRole.START_WURLITZER_FLOW_KEY, DAYS, 28);
        
        // start the JMeter instances
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_TOMCAT9080_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_TOMCAT9081_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_6TOMCAT9091_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_LOAD_ROLE_7TOMCAT9090_01_ID, JMeterLoadRole.START_LOAD_FLOW_KEY, DAYS, 28);
        
        // start the CLW load
        log.info("Starting the CLW transaction trace load");
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.CLW_START_LOAD, DAYS, 28);
        
        // start the historical CLW query load
        log.info("Starting the CLW historical query load");
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.HCLW_START_LOAD, DAYS, 28);

        // start cleanup timer
        final ClwCleanupFlowContext clwCleanupFlowContext
            = deserializeFlowContextFromRole(CLW_ROLE_ID, CLEANUP_CONTEXT_KEY,
                ClwCleanupFlowContext.class);
        if (clwCleanupFlowContext.getCleanupPeriod() > 0) {
            cleanupTimer = new Timer("clw-cleanup", true);
            cleanupTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        log.info("Running periodic CLW cleanup now...");
                        runFlowByMachineIdAsync(CLWSmokeTestbed.CLW_MACHINE_ID,
                            ClwCleanupFlow.class,
                            clwCleanupFlowContext,
                            HOURS, 1);
                        log.info("Periodic CLW cleanup done.");
                    } catch (Exception ex) {
                        ErrorUtils.logExceptionFmt(log, ex,
                            "Periodic CLW cleanup died with exception: {0}");
                    }
                }
            }, clwCleanupFlowContext.getCleanupPeriod(), clwCleanupFlowContext.getCleanupPeriod());
        }

        SECONDS.sleep(210);
    }
    
    
    public void testTransactionTraces() throws Exception {
        // verify that the transaction trace files are being created
        File installDir = new File(envProperties.getRolePropertyById(CLW_ROLE_ID, CLWWorkStationLoadRole.TRACES_DIR_KEY));
        File[] files = installDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().startsWith("TransactionTraceData")) {
                    return true;
                }
                return false;
            }
        });

        assertNotNull(files);
        assertTrue(files.length > 0, "There must be at least one transaction trace file");
    }
    
    
    @Test
    public void testHistoricalQueries() throws Exception {
        // wait 30 seconds for query to have run and produced data 
        SECONDS.sleep(30);
        
        // verify that the historical query is running and returning data
        String filename = envProperties.getRolePropertyById(CLW_ROLE_ID, CLWWorkStationLoadRole.HCLW_FILENAME_KEY);
        String installDir = envProperties.getRolePropertyById(CLW_ROLE_ID, CLWWorkStationLoadRole.INSTALL_DIR_KEY);
        File file = Paths.get(installDir, filename).toFile().getAbsoluteFile();
        assertTrue(file.exists(), "The historical query file " + file.getAbsolutePath() + " must exist");
        assertTrue(file.length() > 0, "The historical query file " + file.getAbsolutePath() + " must not be empty");
    }
}
