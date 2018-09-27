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

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.Clw;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmSanityTestbed;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmTransactionTestbed;
import com.ca.apm.powerpack.sysview.tools.smfgenerator.Signature;
import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfRecordGenerator;
import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfSnippet.Type;
import com.ca.apm.powerpack.sysview.tools.smfgenerator.SmfSnippetGenerator;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Tests behavior of the SMF-to-trace selection algorithm in the CEAPM Agent.
 */
public class CeapmTransactionSelectionTest extends TasTestNgTest {
    private static final Logger log = LoggerFactory.getLogger(CeapmTransactionSelectionTest.class);

    private static final Pattern IMS_PATH = Pattern
        .compile("IMS Subsystems\\|[^|]*\\|Transaction\\|[^|]*\\|Transaction Lifetime");

    protected static final int GENERATOR_DURATION = 300;
    protected static final int CAPTURE_DURATION = GENERATOR_DURATION + 10;

    @Tas(testBeds = @TestBed(name = CeapmTransactionTestbed.class,
        executeOn = CeapmTransactionTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.SMOKE)
    public void uniqueSelection() throws Exception {
        CeapmRole.startAgent(aaClient, envProperties, CeapmTransactionTestbed.CEAPM.getRole(), null, true);

        final String emLibDir = envProperties.getRolePropertyById(CeapmSanityTestbed.EM_ROLE_ID,
            DeployEMFlowContext.ENV_EM_LIB_DIR);

        final Collection<Signature> captured = new HashSet<Signature>();
        Thread capture = new Thread(new Runnable() {
            @Override
            public void run() {
                Clw clw = new Clw.Builder().clwWorkStationDir(emLibDir).build();
                Document doc = clw.getTransactions(".*", CAPTURE_DURATION);

                doc.getDocumentElement().normalize();
                NodeList nodes = doc.getElementsByTagName("TransactionTrace");

                try {
                    for (int i = 0; i < nodes.getLength(); ++i) {
                        final Element tt = (Element) nodes.item(i);
                        final NodeList ccs = tt.getElementsByTagName("CalledComponent");
                        if (ccs.getLength() <= 0) {
                            continue;
                        }
                        final Element cc = (Element) ccs.item(0);
                        final NodeList parameters = cc.getElementsByTagName("Parameter");
                        final String metricPath = cc.getAttribute("MetricPath");

                        String jobname = "";
                        String db2Ssid = null;
                        Type type = IMS_PATH.matcher(metricPath).matches() ? Type.IMS : Type.CICS;

                        for (int j = 0; j < parameters.getLength() && (jobname.isEmpty() || db2Ssid == null); ++j) {
                            final Element parameter = (Element) parameters.item(j);
                            final String name = parameter.getAttribute("Name");
                            final String value = parameter.getAttribute("Value");
                            switch (type) {
                                case CICS:
                                    if (name.compareToIgnoreCase("SSID") == 0) {
                                        // TODO: This is a workaround for the current inability of the
                                        // generator to send SMF records without a DB2 call
                                        if (value.compareToIgnoreCase("XXXX") != 0) {
                                            db2Ssid = value;
                                        }
                                    } else if (name.compareToIgnoreCase("Job Name (Server Name)") == 0) {
                                        jobname = value;
                                    }
                                    break;

                                case IMS:
                                    if (name.compareToIgnoreCase("IMS Subsystem") == 0) {
                                        jobname = value;
                                    }
                                    break;
                            }
                        }
                        captured.add(new Signature(type, jobname, db2Ssid, null));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final SmfRecordGenerator srg = new SmfRecordGenerator(
            envProperties.getMachineHostnameByRoleId(CeapmTransactionTestbed.CEAPM.getRole()),
            Arrays.asList(CeapmTransactionTestbed.CEAPM.getSmfPort()));
        final SmfSnippetGenerator ssg = new SmfSnippetGenerator(srg, GENERATOR_DURATION * 1_000);

        TreeMap<Integer, List<Signature>> dist = new TreeMap<>();
        capture.start();
        Thread.sleep(500);

        dist.putAll(ssg._executeConst(1000, 100));

        capture.join();
        srg.disconnect();

        int matched = 0;
        Set<Signature> expected = new HashSet<Signature>();
        for (Map.Entry<Integer, List<Signature>> entry : dist.entrySet()) {
            if (entry.getKey() <= 0) {
                continue;
            }
            for (Signature s : entry.getValue()) {
                expected.add(s);
                // If this is a CICS with DB2 also add the CICS without DB2 since that's the TTs it
                // will generate.
                if (s.getDb2Ssid() != null && !s.getDb2Ssid().isEmpty()) {
                    expected.add(new Signature(Type.CICS, s.getJobName(), null, null));
                }
            }
        }

        for (Signature e : expected) {
            if (captured.contains(e)) {
                ++matched;
            } else {
                log.debug("Not captured: " + e);
            }
        }

        for (Signature c : captured) {
            if (!expected.contains(c)) {
                log.debug("Not generated: " + c);
            }
        }

        final int percentage = (matched * 100) / expected.size();
        log.info(matched + "/" + expected.size() + " (" + percentage + "%) captured");
        assertTrue(percentage > 90);
    }
}
