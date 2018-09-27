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
package com.ca.apm.systemtest.fld.hammond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.wily.introscope.agent.enterprise.EnterpriseAgent;
import com.wily.introscope.agent.extension.IExtensionLocatorPolicy;
import com.wily.introscope.agent.transformer.dynamic.AInstrumentationHelper;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.introscope.stat.timeslice.ATimeslicedValue;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.isengard.IsengardException;
import com.wily.util.extension.IExtensionLocator;
import com.wily.util.extension.ShallowJarExtensionLocator;
import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.Module;
import com.wily.util.io.ExtendedFile;
import com.wily.util.text.IStringLocalizer;
import com.wily.util.text.MultipleResourceBundleStringLocalizer;

public class InsaneSyntheticAgent extends EnterpriseAgent {

    public static final long HARVEST_PERIOD = 7500;

    private static class ShallowJarPolicy implements IExtensionLocatorPolicy {
        public IExtensionLocator createExtensionLocator(IModuleFeedbackChannel feedback,
            IStringLocalizer localizer, ExtendedFile extensionDirectory) {
            ShallowJarExtensionLocator locator = null;

            if (AInstrumentationHelper.isValidate()) {
                locator =
                    new ShallowJarExtensionLocator(feedback, localizer, extensionDirectory, true,
                        AInstrumentationHelper.getValidationFeedbackChannel());
            } else {
                locator =
                    new ShallowJarExtensionLocator(feedback, localizer, extensionDirectory, true);
            }

            return locator;
        }
    }

    protected static final Module kModule = new Module("HammondAgent");

    protected static final Random rand = new Random(1449224256760L);

    private AgentName newAgentName;

    private HashSet<AgentMetric> sentMetrics = new HashSet<AgentMetric>();

    private ApplicationFeedback feedback;

    public static final Long agentStartedMs = System.currentTimeMillis();

    private static final int CYCLE_SIZE = 30;

    public static long txCount = 0;

    private List<AgentMetric> metrics;

    private int cycle;

    private static IStringLocalizer makeStringLocalizer(IModuleFeedbackChannel feedback) {
        String[] coreList = getCommonAgentResourceBundleList();
        String[] entireList = new String[coreList.length + 1];

        System.arraycopy(coreList, 0, entireList, 0, coreList.length);
        entireList[coreList.length] =
            "com.wily.introscope.agent.properties.EnterpriseAgentReleaseStrings";

        IStringLocalizer localizer =
            new MultipleResourceBundleStringLocalizer(feedback,
                EnterpriseAgent.class.getClassLoader(), entireList, false);

        return localizer;
    }

    public InsaneSyntheticAgent(ApplicationFeedback feedback, AgentName newAgentName) throws BadlyFormedNameException {
        super(feedback, newAgentName.getProcess(), new ShallowJarPolicy(),
            makeStringLocalizer(feedback), null);

        this.feedback =
            Configuration.instance().createFeedback(newAgentName.getProcessURLWithoutDomain());

        this.newAgentName = newAgentName;

        this.metrics = new ArrayList<AgentMetric>();
        for (int i1 = 0; i1 < 10; i1++) {
            for (int i2 = 0; i2 < 10; i2++) {
                for (int i3 = 0; i3 < 10; i3++) {
                    for (int i4 = 0; i4 < 10; i4++) {
                        this.metrics.add(AgentMetric.getAgentMetric("folder1_" + i1 + "|folder2_" + i2 + "|folder3_" + i3 + "|folder4_" + i4 + ":ART", MetricTypes.kIntegerPercentage));

                        // Ugly folder
                        String folder = newAgentName.toString().replace("|", "_");
                        this.metrics.add(AgentMetric.getAgentMetric("folder1_" + i1 + "|folder2_" + i2 + "|folder3_" + i3 + "|folder4_" + i4 + "_" + folder + ":ART", MetricTypes.kIntegerPercentage));
                    }
                }
            }
        }
        
        this.cycle = 0;
    }

    public void start() throws IsengardException,
        BadlyFormedNameException, IOException {

        AgentReporter.init();

        setAgentName(newAgentName.getAgentName());
    }

    @Override
    public void harvest(long nowInMillis, int initial) {

        long a = System.currentTimeMillis();
        int metricsCount = sendMetrics(nowInMillis, initial);
        long b = System.currentTimeMillis();

        if (b - a >= HARVEST_PERIOD) {
            feedback.error("metrics: " + (b - a) + "ms");
        }

        AgentReporter.report(0, metricsCount, 0);
    }



    private int sendMetrics(long nowInMillis, int initial) {
        ArrayList<AgentMetric> newMetrics = new ArrayList<>();
        ArrayList<AgentMetricData> timeslicedBindings = new ArrayList<>();

        boolean even = (cycle / CYCLE_SIZE) % 2 == 0;
        for (int i = even ? 1 : 0; i < metrics.size(); i += 2) {
            AgentMetric metric = metrics.get(i);
            
            if (initial > 0 && sentMetrics.add(metric)) {
                newMetrics.add(metric);
            }

            ATimeslicedValue value = new IntegerTimeslicedValue(metric.getAttributeType(),
                nowInMillis - 15000, nowInMillis, null, 1, false, 2, 1, 3);

            AgentMetricData agentMetricData =
                new AgentMetricData(metric, Frequency.kDefaultAgentFrequency, value);
            timeslicedBindings.add(agentMetricData);
        }

        cycle ++;
        
        int metricsCount = 0;
        doReportTimeslice(newMetrics.toArray(new AgentMetric[newMetrics.size()]),
            timeslicedBindings.toArray(new AgentMetricData[timeslicedBindings.size()]),
            new AgentMetric[0], initial);
        metricsCount += timeslicedBindings.size();
        return metricsCount;
    }

    private static class AgentReporter {
        private static int transactionsCount;
        private static int metricsCount;
        private static int topologyChangesCount;
        private static int activeAgentCount;

        public static void init() {};

        static {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                ApplicationFeedback feedback = Configuration.instance().createFeedback(
                    "AgentReporter");

                @Override
                public void run() {
                    printReport(feedback);
                }
            }, 0, HARVEST_PERIOD);
        }

        private static synchronized void report(int transactionsCount, int metricsCount,
            int topologyChangesCount) {
            AgentReporter.transactionsCount += transactionsCount;
            AgentReporter.metricsCount += metricsCount;
            AgentReporter.topologyChangesCount += topologyChangesCount;

            if (transactionsCount > 0 || metricsCount > 0 || topologyChangesCount > 0) {
                AgentReporter.activeAgentCount++;
            }
        }

        public static synchronized void printReport(ApplicationFeedback feedback) {
            feedback.info(String.format(
                "Sending %d transactions, %d metrics and %d topology changes by %d active agents.",
                transactionsCount, metricsCount, topologyChangesCount, activeAgentCount));

            transactionsCount = 0;
            metricsCount = 0;
            topologyChangesCount = 0;
            activeAgentCount = 0;

        }
    }
}
