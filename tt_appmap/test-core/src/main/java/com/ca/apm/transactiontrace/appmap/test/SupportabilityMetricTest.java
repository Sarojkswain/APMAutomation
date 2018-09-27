/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.test;

import static org.testng.AssertJUnit.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.transactiontrace.appmap.testbed.StandAloneTestbed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test that checks supportability metrics and validates its value
 *
 * Metrics are checked using CLW
 *
 * @author TAS (tas@ca.com)
 * @since 1.0
 */
@Test(groups = {"appmap", "supportability"})
@Tas(testBeds = @TestBed(name = StandAloneTestbed.class, executeOn = StandAloneTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "bhusu01")
public class SupportabilityMetricTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportabilityMetricTest.class);

    private EnvironmentPropertyContext envProp;

    /**
     * Constructor
     */
    public SupportabilityMetricTest() {

    }

    @BeforeTest
    public void loadEnvProperties() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Test
    public void populatedMapTest() throws IOException {
            String installDir =
                envProp.getRolePropertiesById(StandAloneTestbed.EM_ROLE_ID).getProperty(EmRole.ENV_PROPERTY_INSTALL_DIR);

            String[] metricPathsToTest = new String[]{"Correlated Traces Count", "Collection Time \\(ms\\)"};

            String clwExec = installDir + "\\lib\\CLWorkstation.jar";

            String fetchMetricCommandFormatter= "get historical data from agents matching \".*|.*|.*\" and metrics matching \"%s\" for past 1 minute with frequency of 15 s";

            for(final String metricPath : metricPathsToTest) {
                Runtime rt = Runtime.getRuntime();
                String fetchMetricCommand = String.format(fetchMetricCommandFormatter,
                    ".*" + metricPath);
                String fullCommand = "cmd /c java -jar \"" + clwExec + "\" " + fetchMetricCommand;
                LOGGER.info(fullCommand);
                final MutableBoolean finished = new MutableBoolean(false);
                final MutableBoolean failed = new MutableBoolean(true); // init test as failed
                final Process process = rt.exec(fullCommand);
                new Thread(new Runnable() {
                    public void run() {
                        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;

                        try {
                            int lineCount = 0;
                            int valueCountIndex = -1;
                            int valueCount = -1;
                            while ((line = input.readLine()) != null) {
                                lineCount++;
                                if(lineCount==2) {
                                    String[] columnNames = line.split(",");
                                    for(int i=0;i<columnNames.length;i++) {
                                        if(" Value Count".equals(columnNames[i])) {
                                            valueCountIndex = i;
                                        }
                                    }
                                }
                                if(lineCount==3 && valueCountIndex>=0) {
                                    String[] values = line.split(",");
                                    valueCount = Integer.parseInt(values[valueCountIndex]);
                                }
                            }
                            assertTrue("line count for "+ metricPath + " was " + lineCount,lineCount>3);
                            assertTrue(valueCount>0);
                            failed.setValue(false);  // succeeded
                        } catch (IOException e) {
                            LOGGER.error("Error reading CLW output ", e);
                        } finally {
                            finished.setValue(true);
                        }
                    }
                }).start();

                try {
                    process.waitFor();
                    while (finished.booleanValue() == false) {
                        Thread.sleep(1000);
                    }
                    assertTrue("Failed getting metric data, check previous output", !failed.booleanValue());
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted waiting for process to end ", e);
                }
            }
    }
}
