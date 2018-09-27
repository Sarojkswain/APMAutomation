/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.test.test;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.test.testbed.AbstractLuceneWurlitzerTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App0001BackendTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App0005BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App0050BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App1000BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent001App2000BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent005Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent030Apps0100BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer001Agent500Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer005Agents010Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer005Agents010Apps0020BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer010Agents020Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer010Agents020Apps0050BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer010Agents150Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer010Agents200Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer010Agents300Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer050Agents010Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer050Agents100Apps0025BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer100Agents010Apps0002BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer100Agents010Apps0010BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer100Agents010Apps0025BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer250Agents010Apps0002BackendsTestbed;
import com.ca.apm.test.testbed.LuceneWurlitzer250Agents010Apps0025BackendsTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test class utilizing Wurlitzer stress tests while observing certain Lucene indexing related
 * metrics.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class LuceneWurlitzerTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneWurlitzerTest.class);

    private static final String AGENT_REGEXP = ".*Custom Metric Agent.*";
    private static final String METRIC_REGEXP_PREFIX =
        "Enterprise Manager|ApplicationTriageMap|Public REST API|Lucene|Vertex:";
    private static final String INDEX_UPDATING_TASK_COUNT_REGEXP = METRIC_REGEXP_PREFIX
        + "Index Updating Tasks In Queue Count";
    private static final String INDEX_WRITER_DOCS_COUNT_REGEXP = METRIC_REGEXP_PREFIX
        + "Index Writer Docs Count";
    private static final String SUBMITTED_CHANGES_PER_INTERVAL_REGEXP = METRIC_REGEXP_PREFIX
        + "Submitted Changes Per Interval";
    private static final String PROCESSED_CHANGES_PER_INTERVAL_REGEXP = METRIC_REGEXP_PREFIX
        + "Processed Changes Per Interval";

    private static final long SHORT_SAMPLING_DURATION = 5 * 2L; // 5 mins
    private static final long MEDIUM_SAMPLING_DURATION = 15 * 2L; // 15 mins
    private static final long LONG_SAMPLING_DURATION = 30 * 2L; // 30 mins

    private static final int ACCEPTABLE_SUBSEQUENT_VIOLATIONS = 2;
    private static final int VIOLATION_THRESHOLD = 1;

    protected void appMapStressTest(long samplingDurationInHalvesOfMinute) throws Exception {
        ClwUtils clwUtils = utilities.createClwUtils(AbstractLuceneWurlitzerTestbed.EM_ROLE_ID);

        int subsequentViolationsCounter = 0;
        int totalViolationsCounter = 0;
        Calendar maxIntervalStart = Calendar.getInstance();
        maxIntervalStart.add(Calendar.DAY_OF_MONTH, -1);

        for (int i = 0; i < samplingDurationInHalvesOfMinute; i++) {
            long roundStart = System.currentTimeMillis();

            Calendar maxIntervalEnd = Calendar.getInstance();

            String stringIndexUpdatingTaskCount =
                clwUtils.getMetricFromAgent(AGENT_REGEXP, INDEX_UPDATING_TASK_COUNT_REGEXP);
            String stringIndexWriterDocsCount =
                clwUtils.getMetricFromAgent(AGENT_REGEXP, INDEX_WRITER_DOCS_COUNT_REGEXP);
            String stringSubmittedChangesPerInterval =
                clwUtils.getMetricFromAgent(AGENT_REGEXP, SUBMITTED_CHANGES_PER_INTERVAL_REGEXP);
            String stringProcessedChangesPerInterval =
                clwUtils.getMetricFromAgent(AGENT_REGEXP, PROCESSED_CHANGES_PER_INTERVAL_REGEXP);
            Long maxSubmittedChangesPerInterval =
                clwUtils.getMaxMetricsValueFromAgent(AGENT_REGEXP,
                    SUBMITTED_CHANGES_PER_INTERVAL_REGEXP, maxIntervalStart, maxIntervalEnd);
            Long maxProcessedChangesPerInterval =
                clwUtils.getMaxMetricsValueFromAgent(AGENT_REGEXP,
                    PROCESSED_CHANGES_PER_INTERVAL_REGEXP, maxIntervalStart, maxIntervalEnd);

            Assert.assertFalse("IndexUpdatingTaskCount empty!",
                StringUtils.isBlank(stringIndexUpdatingTaskCount));
            Assert.assertFalse("IndexWriterDocsCount empty!",
                StringUtils.isBlank(stringIndexWriterDocsCount));
            Assert.assertFalse("SubmittedChangesPerInterval empty!",
                StringUtils.isBlank(stringSubmittedChangesPerInterval));
            Assert.assertFalse("ProcessedChangesPerInterval empty!",
                StringUtils.isBlank(stringProcessedChangesPerInterval));

            LOGGER.debug("IndexUpdatingTaskCount: " + stringIndexUpdatingTaskCount);

            int indexUpdatingTaskCount = Integer.parseInt(stringIndexUpdatingTaskCount);

            if (indexUpdatingTaskCount > VIOLATION_THRESHOLD) {
                subsequentViolationsCounter++;
                totalViolationsCounter++;
            } else {
                // reset counter
                subsequentViolationsCounter = 0;
            }

            LOGGER.info("Round#: " + i + " SubsequentViolations: " + subsequentViolationsCounter
                + " TotalViolations: " + totalViolationsCounter + " IndexUpdatingTaskCount: "
                + indexUpdatingTaskCount + " IndexWriterDocsCount: " + stringIndexWriterDocsCount
                + " SubmittedChangesPerInterval: " + stringSubmittedChangesPerInterval
                + " ProcessedChangesPerInterval: " + stringProcessedChangesPerInterval
                + " MaxSubmittedChangesPerInterval: " + maxSubmittedChangesPerInterval
                + " MaxProcessedChangesPerInterval: " + maxProcessedChangesPerInterval);

            if (subsequentViolationsCounter > ACCEPTABLE_SUBSEQUENT_VIOLATIONS) {
                Assert.fail("Acceptable number of subsequent violations exceeded!");
            }

            long roundStop = System.currentTimeMillis();
            long roundDuration = roundStop - roundStart;
            long sleepDuration = 30000 - roundDuration;

            if (sleepDuration > 0) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    // noop
                }
            }
        }
    }

    // --- 001 agent tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App0001BackendTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App0001BackendTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App0005BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App0005BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App0050BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App0050BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App1000BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App1000BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent001App2000BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent001App2000BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent005Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent005Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent030Apps0100BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent030Apps0100BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer001Agent500Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-001agent"})
    public void luceneWurlitzer001Agent500Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    // --- 005 agents tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer005Agents010Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-005agents"})
    public void luceneWurlitzer005Agents010Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer005Agents010Apps0020BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-005agents"})
    public void luceneWurlitzer005Agents010Apps0020BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    // --- 010 agents tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer010Agents020Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-010agents"})
    public void luceneWurlitzer010Agents020Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer010Agents020Apps0050BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-010agents"})
    public void luceneWurlitzer010Agents020Apps0050BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer010Agents150Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-010agents"})
    public void luceneWurlitzer010Agents150Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer010Agents200Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-010agents"})
    public void luceneWurlitzer010Agents200Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer010Agents300Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-010agents"})
    public void luceneWurlitzer010Agents300Apps0010BackendsTest() throws Exception {
        appMapStressTest(SHORT_SAMPLING_DURATION);
    }

    // --- 050 agents tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer050Agents010Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-050agents"})
    public void luceneWurlitzer050Agents010Apps0010BackendsTest() throws Exception {
        appMapStressTest(MEDIUM_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer050Agents100Apps0025BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-050agents"})
    public void luceneWurlitzer050Agents100Apps0025BackendsTest() throws Exception {
        appMapStressTest(MEDIUM_SAMPLING_DURATION);
    }

    // --- 100 agents tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer100Agents010Apps0002BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-100agents"})
    public void luceneWurlitzer100Agents010Apps0002BackendsTest() throws Exception {
        appMapStressTest(LONG_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer100Agents010Apps0010BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-100agents"})
    public void luceneWurlitzer100Agents010Apps0010BackendsTest() throws Exception {
        appMapStressTest(LONG_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer100Agents010Apps0025BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-100agents"})
    public void luceneWurlitzer100Agents010Apps0025BackendsTest() throws Exception {
        appMapStressTest(LONG_SAMPLING_DURATION);
    }

    // --- 250 agents tests

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer250Agents010Apps0002BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-250agents"})
    public void luceneWurlitzer250Agents010Apps0002BackendsTest() throws Exception {
        appMapStressTest(LONG_SAMPLING_DURATION);
    }

    @Tas(testBeds = @TestBed(name = LuceneWurlitzer250Agents010Apps0025BackendsTestbed.class, executeOn = AbstractLuceneWurlitzerTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "zakja01")
    @Test(groups = {"lucene-wurlitzer-test", "lucene-wurlitzer-test-250agents"})
    public void luceneWurlitzer250Agents010Apps0025BackendsTest() throws Exception {
        appMapStressTest(LONG_SAMPLING_DURATION);
    }

}
