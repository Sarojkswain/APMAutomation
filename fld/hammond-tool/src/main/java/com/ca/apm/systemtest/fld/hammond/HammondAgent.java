/*
 * Copyright (c) 2017 CA. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import com.ca.apm.systemtest.fld.hammond.imp.HammondMetricFilter;
import org.rocksdb.RocksDBException;

import com.ca.apm.systemtest.fld.hammond.imp.HammondTables;
import com.ca.apm.systemtest.fld.hammond.imp.SliceDataValues;
import com.ca.apm.systemtest.fld.hammond.imp.SliceDataValues.SliceDataValue;
import com.wily.introscope.agent.enterprise.EnterpriseAgent;
import com.wily.introscope.agent.extension.IExtensionLocatorPolicy;
import com.wily.introscope.agent.transactiontrace.SharedCrossProcessData;
import com.wily.introscope.agent.transformer.dynamic.AInstrumentationHelper;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.server.transactiontrace.ACrossProcessData;
import com.wily.introscope.spec.server.transactiontrace.KTransactionTraceConstants;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
import com.wily.introscope.stat.timeslice.ATimeslicedValue;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;
import com.wily.isengard.IsengardException;
import com.wily.util.extension.IExtensionLocator;
import com.wily.util.extension.ShallowJarExtensionLocator;
import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.Module;
import com.wily.util.io.ExtendedFile;
import com.wily.util.text.IStringLocalizer;
import com.wily.util.text.MultipleResourceBundleStringLocalizer;
import sun.rmi.runtime.Log;

public class HammondAgent extends EnterpriseAgent {

    public static final long HARVEST_PERIOD = 7500;

    private static Semaphore semaphore = new Semaphore(8);

    private long playMetricsFrom;
    private long playMetricsTo;
    private long playTracesFrom;
    private long playTracesTo;

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

    private AgentName origAgentName;
    private AgentName newAgentName;
    private String variant;

    private HashSet<AgentMetric> sentMetrics = new HashSet<>();

    private ApplicationFeedback feedback;

    private HammondTables hammondData;

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

    public HammondAgent(ApplicationFeedback feedback, AgentName origAgentName,
                 AgentName newAgentName, String variant) throws BadlyFormedNameException {
        super(feedback, newAgentName.getProcess(), new ShallowJarPolicy(),
                makeStringLocalizer(feedback), null);

        this.feedback =
                Configuration.instance().createFeedback(newAgentName.getProcessURLWithoutDomain());

        this.origAgentName = origAgentName;
        this.newAgentName = newAgentName;
        this.variant = variant;
    }

    public void start(HammondTables hammondData, long playMetricsFrom, long playMetricsTo, long playTracesFrom, long playTracesTo)
            throws IsengardException, BadlyFormedNameException, IOException {

        this.hammondData = hammondData;
        this.playMetricsFrom = playMetricsFrom;
        this.playMetricsTo = playMetricsTo;
        this.playTracesFrom = playTracesFrom;
        this.playTracesTo = playTracesTo;

        setAgentName(newAgentName.getAgentName());
    }

    @Override
    public void harvest(long nowInMillis, int initial) {

        try {
            semaphore.acquire();

            try {
                long metricsRange = playMetricsTo - playMetricsFrom;
                long metricsHammondTime = ((nowInMillis - playMetricsFrom) % metricsRange) + playMetricsFrom;
                metricsHammondTime = 15000 * (metricsHammondTime / 15000);

                long tracesRange = playTracesTo - playTracesFrom;
                long tracesHammondTime = ((nowInMillis - playTracesFrom) % tracesRange) + playTracesFrom;
                tracesHammondTime = 15000 * (tracesHammondTime / 15000);

                long start = System.currentTimeMillis();
                int metricsCount = 0;
                try {
                    metricsCount = sendMetrics(metricsHammondTime, nowInMillis, initial);
                } catch (RocksDBException e) {
                    feedback.error(e);
                }
                long metricSentTime = System.currentTimeMillis();
                int transactionsCount = 0;
                try {
                    transactionsCount = sendTransactions(tracesHammondTime, (15000 * nowInMillis / 15000) - tracesHammondTime);
                } catch (RocksDBException e) {
                    feedback.error(e);
                }
                long transactionSentTime = System.currentTimeMillis();

                String message = "metrics: " + (metricSentTime - start) + "ms; "
                        + "transactions: " + (transactionSentTime - metricSentTime) + "ms";

                if (transactionSentTime - start > HARVEST_PERIOD / 2 && transactionSentTime - start < HARVEST_PERIOD) {
                    feedback.warn(message);
                } else if (transactionSentTime - start >= HARVEST_PERIOD) {
                    feedback.error(message);
                }

                AgentReporter.report(transactionsCount, metricsCount);
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) { }
    }

    public ATimeslicedValue getReportableValue(ATimeslicedValue value, long nowInMillis, AgentName agentName) {
        if (value instanceof IntegerTimeslicedValue) {
            return new IntegerTimeslicedValue(value.getType(), 
                nowInMillis - Agent.HARVEST_PERIOD,
                nowInMillis, value.getBlameStackSnapshot(), 
                value.getDataPointCount(), value.dataIsAbsent(),
                ((IntegerTimeslicedValue) value).getValue(),
                ((IntegerTimeslicedValue) value).getMinimum(),
                ((IntegerTimeslicedValue) value).getMaximum());
        }
        
        return new LongTimeslicedValue(value.getType(), 
            nowInMillis - Agent.HARVEST_PERIOD,
            nowInMillis, value.getBlameStackSnapshot(), 
            value.getDataPointCount(), value.dataIsAbsent(),
            ((LongTimeslicedValue) value).getValue(),
            ((LongTimeslicedValue) value).getMinimum(),
            ((LongTimeslicedValue) value).getMaximum());
    }

    private int sendMetrics(long hammondTime, long nowInMillis, int initial) throws RocksDBException {
        SliceDataValues dataTmp = hammondData.getMetricSlice(hammondTime, origAgentName);

        List<AgentMetricData> data = new ArrayList<>();

        for (SliceDataValue v : dataTmp.getValues()) {
            AgentMetric attribute = hammondData.getAttributeById(v.getAttribute());
            
            ATimeslicedValue value = getReportableValue((ATimeslicedValue) v.getValue(), nowInMillis, newAgentName);

            if (HammondMetricFilter.filterMetric(attribute)) {
                feedback.debug("Excluding metric: " + attribute.getAttributeURL());
                continue;
            }

            data.add(new AgentMetricData(attribute, Frequency.kDefaultAgentFrequency, value));
        }
        
        Set<AgentMetric> newMetrics = new HashSet<>();
        for (AgentMetricData d : data) {
            newMetrics.add(d.getAgentMetric());
        }
        
        Set<AgentMetric> deadMetrics = new HashSet<>(sentMetrics);
        deadMetrics.removeAll(newMetrics);
        
        newMetrics.removeAll(sentMetrics);
        
        sentMetrics.removeAll(deadMetrics);
        sentMetrics.addAll(newMetrics);

        doReportTimeslice(newMetrics.toArray(new AgentMetric[newMetrics.size()]),
            data.toArray(new AgentMetricData[data.size()]), 
            deadMetrics.toArray(new AgentMetric[deadMetrics.size()]), initial);
        return data.size();
    }

    private long lastTransactionQuery = 0;
    private int sendTransactions(long hammondTime, long timeOffset) throws RocksDBException {
        if (lastTransactionQuery == hammondTime) {
            // Do not send twice
            return 0;
        }
        
        lastTransactionQuery = hammondTime;
        
        List<TransactionComponentData> transactions = hammondData.getTraceSlice(hammondTime, origAgentName);
        
        List<TransactionComponentData> result = new ArrayList<>();
        for (TransactionComponentData transaction : transactions) {
            result.add(transactionWithOffset(transaction, timeOffset));
        }
        reportTransactions(result.toArray(new TransactionComponentData[result.size()]));
        return transactions.size();
    }


    private TransactionComponentData transactionWithOffset(TransactionComponentData orig,
                                                           long offset) {

        TransactionComponentData[] subNodes = new TransactionComponentData[orig.getSubNodeCount()];
        for (int i = 0; i < subNodes.length; i++) {
            subNodes[i] = transactionWithOffset(orig.getSubNodes()[i], offset);
        }

        String[] keys =
                {ACrossProcessData.kCrossProcessDataKey, KTransactionTraceConstants.kTxnTraceId,
                        KTransactionTraceConstants.kCallerTxnTraceId};

        @SuppressWarnings("unchecked")
        Map<String, String> parameters = new HashMap<>(orig.getParameters());

        for (String key : keys) {
            String id = parameters.get(key);
            if (id != null) {
                parameters.put(key, updateId(id, variant, offset));
            }
        }
        String callerTimestamp = parameters.get(SharedCrossProcessData.kCallerTimestampKey);
        if (callerTimestamp != null && !callerTimestamp.isEmpty()) {
            parameters.put(SharedCrossProcessData.kCallerTimestampKey,
                    Long.toString(Long.parseLong(callerTimestamp) + offset));
        }

        return new TransactionComponentData(orig.getResource(), orig.getMagnitude(),
                orig.getStartTime() + offset, orig.getTimeZone(), orig.getDuration(), parameters,
                subNodes);
    }

    private String updateId(String id, String variant, long offset) {
        if (variant != null && !variant.isEmpty()) {
            id += "_" + variant;
        }
        if (offset != 0) {
            id += "_" + Long.toHexString(offset);
        }
        return id;
    }

    private static class AgentReporter {
        private static int transactionsCount;
        private static int metricsCount;
        private static int activeAgentCount;

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

        private static synchronized void report(int transactionsCount, int metricsCount) {
            AgentReporter.transactionsCount += transactionsCount;
            AgentReporter.metricsCount += metricsCount;

            if (transactionsCount > 0 || metricsCount > 0) {
                AgentReporter.activeAgentCount++;
            }
        }

        public static synchronized void printReport(ApplicationFeedback feedback) {
            feedback.info(String.format(
                    "Sending %d transactions and %d metrics by %d active agents.",
                    transactionsCount, metricsCount, activeAgentCount));

            transactionsCount = 0;
            metricsCount = 0;
            activeAgentCount = 0;

        }
    }
}
