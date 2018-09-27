package com.ca.apm.systemtest.fld.hammond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.SerializationUtils;

import com.ca.apm.systemtest.fld.hammond.data.AppmapData;
import com.ca.apm.systemtest.fld.hammond.data.SmartstorData;
import com.ca.apm.systemtest.fld.hammond.data.TransactionsData;
import com.wily.introscope.agent.connection.IsengardServerConnectionManager;
import com.wily.introscope.agent.enterprise.EnterpriseAgent;
import com.wily.introscope.agent.extension.IExtensionLocatorPolicy;
import com.wily.introscope.agent.transactiontrace.SharedCrossProcessData;
import com.wily.introscope.agent.transformer.dynamic.AInstrumentationHelper;
import com.wily.introscope.appmap.agent.connection.DependencyMapEdgeCommand;
import com.wily.introscope.spec.agent.beans.appmap.IAppMapDecisionSupportBean.ICallBackOnSent;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.server.appmap.IAppMapEdge;
import com.wily.introscope.spec.server.appmap.IAppMapProperty;
import com.wily.introscope.spec.server.appmap.IAppMapVertex;
import com.wily.introscope.spec.server.appmap.impl.AAppMapEdge;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.OutOfTheBoxAppMapVertexAbstractionLevel;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.OutOfTheBoxAppMapVertexProperty;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.VertexAppMapProperties;
import com.wily.introscope.spec.server.transactiontrace.ACrossProcessData;
import com.wily.introscope.spec.server.transactiontrace.KTransactionTraceConstants;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
import com.wily.introscope.spec.server.transactiontrace.TransactionTraceData;
import com.wily.introscope.stat.timeslice.ATimeslicedValue;
import com.wily.isengard.IsengardException;
import com.wily.util.extension.IExtensionLocator;
import com.wily.util.extension.ShallowJarExtensionLocator;
import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.Module;
import com.wily.util.io.ExtendedFile;
import com.wily.util.text.IStringLocalizer;
import com.wily.util.text.MultipleResourceBundleStringLocalizer;

public class Agent extends EnterpriseAgent {

    public static final long HARVEST_PERIOD = 7500;

    private static final long SENDING_TOPOLOGY_PAUSE = 1000 * 60 * 30;

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

    private int agentId;
    private AgentName origAgentName;
    private AgentName newAgentName;
    private String variant;

    static private HashMap<String, List<AgentName>> agentNameMapping = new HashMap<>();

    private SmartstorData smartstorData;
    private TransactionsData transactionsData;
    private AppmapData appmapData;

    private long lastHarvestCycle = -1;
    private long dataStart;
    private long dataDuration;
    private long timeOffset;
    private HashSet<String> sentMetrics = new HashSet<>();

    private ApplicationFeedback feedback;

    public static final Long agentStartedMs = System.currentTimeMillis();

    public static long txCount = 0;

    private List<String> metricSuffixes = new ArrayList<>();

    private boolean rotateMetrics;

    private int rotationIndex;

    private long rotationTime;

    private static Timer rotationTimer = new Timer();


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

    public Agent(ApplicationFeedback feedback, int agentId, AgentName origAgentName,
                 AgentName newAgentName, String variant) throws BadlyFormedNameException {
        super(feedback, newAgentName.getProcess(), new ShallowJarPolicy(),
                makeStringLocalizer(feedback), null);

        this.feedback =
                Configuration.instance().createFeedback(newAgentName.getProcessURLWithoutDomain());

        this.agentId = agentId;
        this.origAgentName = origAgentName;
        this.newAgentName = newAgentName;
        this.variant = variant;

        String key = origAgentName.getProcessURLWithoutDomain();
        List<AgentName> names = agentNameMapping.get(key);
        if (names == null) {
            names = new ArrayList<AgentName>();
            agentNameMapping.put(key, names);
        }
        names.add(newAgentName);

        Configuration cfg = Configuration.instance();
        if (cfg.getRotationScale() != 0) {
            rotateMetrics = true;
            for (int i = 0; i < cfg.getRotationScale(); i++) {
                String randomSuffix = null;
                do {
                    randomSuffix = "|num" + Integer.toString(i) + ":";
                } while (metricSuffixes.contains(randomSuffix));
                metricSuffixes.add(randomSuffix);
            }

            rotationTime = cfg.getRotationTime();


            rotationTimer.schedule(new RotationTimerTask(), rotationTime, rotationTime);
        }
    }

    private class RotationTimerTask extends TimerTask {

        @Override
        public void run() {
            rotationIndex = (rotationIndex + 1) % metricSuffixes.size();
        }

    }

    public void start(SmartstorData smartstorData, TransactionsData transactionsData,
                      AppmapData appmapData, long playbackStartMillis) throws IsengardException,
            BadlyFormedNameException, IOException {
        this.dataDuration = smartstorData.getSlicesRangeTo() - smartstorData.getSlicesRangeFrom();
        this.dataStart = smartstorData.getSlicesRangeFrom();
        this.timeOffset = playbackStartMillis - this.dataStart;
        this.smartstorData = smartstorData;
        this.transactionsData = transactionsData;
        this.appmapData = appmapData;

        AgentReporter.init();

        setAgentName(newAgentName.getAgentName());
    }

    @Override
    public void harvest(long nowInMillis, int initial) {
        if (nowInMillis > dataStart + timeOffset + dataDuration) {
            if (Configuration.instance().isPlayOnce()) {
                feedback.info("Agent " + getName() + " playback is finished.");
                return;
            } else {
                feedback.info("Current metric data is over, replaying it again...");
                timeOffset += dataDuration;
            }
        }

        long a = System.currentTimeMillis();
        int metricsCount = sendMetrics(nowInMillis, initial);
        long b = System.currentTimeMillis();
        int topologyChangesCount = sendTopologyChanges(nowInMillis);
        long c = System.currentTimeMillis();
        int transactionsCount = sendTransactions(nowInMillis);
        long d = System.currentTimeMillis();

        if (d - a > HARVEST_PERIOD / 2 && d - a < HARVEST_PERIOD) {
            feedback.warn("metrics: " + (b - a) + "ms; topology: " + (c - b) + "ms; transactions: "
                    + (d - c) + "ms");
        } else if (d - a >= HARVEST_PERIOD) {
            feedback.error("metrics: " + (b - a) + "ms; topology: " + (c - b)
                    + "ms; transactions: " + (d - c) + "ms");
        }

        AgentReporter.report(transactionsCount, metricsCount, topologyChangesCount);
        lastHarvestCycle = nowInMillis;
    }


    private AgentMetric rotateMetric(AgentMetric agentMetric) {
        try {

            StringBuilder sb = new StringBuilder();

            sb.append(agentMetric.getAgentMetricPrefix().toString());
            sb.append(metricSuffixes.get(rotationIndex));
            sb.append(agentMetric.getAttributeName());
            AgentMetric ret = new AgentMetric(sb.toString(), agentMetric.getAttributeType());
            feedback.info(ret.toString());
            return ret;
        } catch (BadlyFormedNameException e) {
            feedback.error(e);
            return agentMetric;
        }
    }


    private int sendMetrics(long nowInMillis, int initial) {
        Long timesliceKey = smartstorData.findTimesliceKey(nowInMillis, timeOffset);
        if (timesliceKey == null) {
            return 0;
        }


        List<Metric> slice = smartstorData.getTimeslice(timesliceKey);


        ArrayList<AgentMetric> newMetrics = new ArrayList<>();
        ArrayList<AgentMetricData> timeslicedBindings = new ArrayList<>();

        for (Metric metric : slice) {
            if (metric.getAgentId() != agentId) {
                continue;
            }

            AgentMetric agentMetric = Agent.this.smartstorData.getMetric(metric.metricId);

            if (rotateMetrics) {
                agentMetric = rotateMetric(agentMetric);
            }

            if (initial > 0 && sentMetrics.add(agentMetric.getAttributeURL())) {
                newMetrics.add(agentMetric);
            }

            ATimeslicedValue value = metric.getReportableValue(nowInMillis, newAgentName);

            if (agentMetric != null && value != null) {
                AgentMetricData agentMetricData =
                        new AgentMetricData(agentMetric, Frequency.kDefaultAgentFrequency, value);
                timeslicedBindings.add(agentMetricData);
            }
        }

        int metricsCount = 0;
        doReportTimeslice(newMetrics.toArray(new AgentMetric[newMetrics.size()]),
                timeslicedBindings.toArray(new AgentMetricData[timeslicedBindings.size()]),
                new AgentMetric[0], initial);
        metricsCount += timeslicedBindings.size();
        return metricsCount;
    }

    private TransactionComponentData[] lastTransactionResult;

    private int sendTransactions(long time) {
        int transactionsCount = 0;

        List<TransactionComponentData> transactions = findTransactions(time);
        if (!transactions.isEmpty()) {
            ArrayList<TransactionComponentData> result = new ArrayList<>();
            for (int i = 0; i < transactions.size(); i++) {
                result.add(transactionWithOffset(transactions.get(i), timeOffset));
            }
            lastTransactionResult = result.toArray(new TransactionComponentData[result.size()]);
            reportTransactions(lastTransactionResult);
            transactionsCount = lastTransactionResult.length;
        } else if (lastTransactionResult != null
                && getTransactionTraceController().isTransactionTracingSetInAgent()) {
            reportTransactions(lastTransactionResult);
            transactionsCount = lastTransactionResult.length;
        }

        return transactionsCount;
    }

    private Map<IAppMapProperty, Object> physicalVertexProperties(IAppMapVertex vertex) {
        Map<IAppMapProperty, Object> properties = vertex.getProperties();
        if (OutOfTheBoxAppMapVertexAbstractionLevel.Physical.equals(properties
                .get(VertexAppMapProperties.AbstractionLevel))) {
            return properties;
        } else {
            return null;
        }
    }

    private IAppMapEdge updateEdgeNames(IAppMapEdge src, boolean thisIsHead,
                                        AgentName foreignAgentName) {
        AAppMapEdge newEdge = (AAppMapEdge) SerializationUtils.clone(src);

        IAppMapVertex local;
        IAppMapVertex foreign;
        if (thisIsHead) {
            local = newEdge.getHeadNode();
            foreign = newEdge.getTailNode();
        } else {
            foreign = newEdge.getHeadNode();
            local = newEdge.getTailNode();
        }

        local.getProperties().put(OutOfTheBoxAppMapVertexProperty.AgentName,
                newAgentName.getAgentName());
        local.getProperties().put(OutOfTheBoxAppMapVertexProperty.ProcessName,
                newAgentName.getProcess());
        local.getProperties().put(OutOfTheBoxAppMapVertexProperty.HostName, newAgentName.getHost());

        if (foreignAgentName != null) {
            foreign.getProperties().put(OutOfTheBoxAppMapVertexProperty.AgentName,
                    foreignAgentName.getAgentName());
            local.getProperties().put(OutOfTheBoxAppMapVertexProperty.ProcessName,
                    foreignAgentName.getProcess());
            local.getProperties().put(OutOfTheBoxAppMapVertexProperty.HostName,
                    foreignAgentName.getHost());
        }

        return newEdge;
    }

    private boolean reportEdge(ICallBackOnSent callback, List<IAppMapEdge> data) {

        IsengardServerConnectionManager connection = IAgent_getIsengardServerConnection();
        if (connection != null && connection.shouldSendData() && data != null) {

            DependencyMapEdgeCommand edgeCommand =
                    new DependencyMapEdgeCommand(callback, IAgent_getModuleFeedback(), data);
            if (!connection.addToCommandQueue(edgeCommand)) {
                callback.onSent(false);
            } else {
                return true;
            }
        } else {
            callback.onSent(false);
        }
        return false;
    }

    private long stopSendingTopology = 0;

    private int sendTopologyChanges(final long nowInMillis) {

        if (stopSendingTopology != 0) {
            if (stopSendingTopology + SENDING_TOPOLOGY_PAUSE > nowInMillis) {
                return 0;
            } else {
                stopSendingTopology = 0;
                getModuleFeedback().info(kModule, "Sending edges enabled");
            }
        }

        int topologyChangesCount = 0;

        int period = (int) (lastHarvestCycle > -1 ? nowInMillis - lastHarvestCycle : HARVEST_PERIOD);
        List<IAppMapEdge> edges =
                appmapData.getEdgesInInterval(nowInMillis, period, timeOffset, origAgentName);

        ArrayList<IAppMapEdge> data = new ArrayList<>();
        for (IAppMapEdge edge : edges) {

            boolean thisIsHead = physicalVertexProperties(edge.getHeadNode()) == null;
            String key;

            try {
                Map<IAppMapProperty, Object> prop;
                if (thisIsHead) {
                    prop = edge.getTailNode().getProperties();
                } else {
                    prop = edge.getHeadNode().getProperties();
                }
                key =
                        AgentName.getAgentName(
                                com.wily.introscope.domain.KDomainConstants.kSuperDomainName,
                                (String) prop.get(OutOfTheBoxAppMapVertexProperty.HostName),
                                (String) prop.get(OutOfTheBoxAppMapVertexProperty.ProcessName),
                                (String) prop.get(OutOfTheBoxAppMapVertexProperty.AgentName))
                                .getProcessURLWithoutDomain();
            } catch (BadlyFormedNameException e) {
                key = null;
            }
            List<AgentName> foreignMapping = key == null ? null : agentNameMapping.get(key);

            if (foreignMapping == null || foreignMapping.isEmpty()) {
                data.add(updateEdgeNames(edge, thisIsHead, null));
            } else {
                for (AgentName foreignAgentName : foreignMapping) {
                    data.add(updateEdgeNames(edge, thisIsHead, foreignAgentName));
                }
            }
        }

        ICallBackOnSent callback = new ICallBackOnSent() {
            @Override
            public void onSent(boolean sucess) {
                if (!sucess) {
                    getModuleFeedback()
                            .error(kModule,
                                    "Unable to send environment variables to app map. Sending edges temporary disabled.");
                    stopSendingTopology = nowInMillis;
                }
            }
        };
        if (!data.isEmpty()) {
            reportEdge(callback, data);
            topologyChangesCount += data.size();
        }

        return topologyChangesCount;
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
                    Long.toString(Long.parseLong(callerTimestamp) + timeOffset));
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

    private List<TransactionComponentData> findTransactions(long nowInMillis) {
        ArrayList<TransactionComponentData> result = new ArrayList<>();

        int period = (int) (lastHarvestCycle > -1 ? nowInMillis - lastHarvestCycle : HARVEST_PERIOD);
        List<TransactionTraceData> slice =
                transactionsData.findTimeslice(nowInMillis, timeOffset, period);
        for (TransactionTraceData data : slice) {
            if (origAgentName.getProcessURLWithoutDomain().equals(
                    data.getAgent().getProcessURLWithoutDomain())) {
                result.add(data.getRootComponent());
            }
        }

        return result;
    }

    private static class AgentReporter {
        private static int transactionsCount;
        private static int metricsCount;
        private static int topologyChangesCount;
        private static int activeAgentCount;

        public static void init() {
        }

        ;

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
