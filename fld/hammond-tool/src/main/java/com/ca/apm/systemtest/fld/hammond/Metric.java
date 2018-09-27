package com.ca.apm.systemtest.fld.hammond;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.TypeInquisitor;
import com.wily.introscope.stat.timeslice.ATimeslicedValue;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;
import com.wily.introscope.stat.timeslice.StringTimeslicedValue;

public class Metric implements Serializable {
    private static final long serialVersionUID = -848067294428693933L;

    protected int agentId;
    protected int metricId;
    protected ATimeslicedValue value;

    transient protected int harvestableCycles;
    transient protected ConcurrentMap<AgentName, AtomicInteger> reportedCounts;

    public Metric(int agentId, int metricId, ATimeslicedValue value) {
        super();
        this.agentId = agentId;
        this.metricId = metricId;
        this.value = value;
        
        this.harvestableCycles =
            (int) ((value.getStopTimestampInMillis() - value.getStartTimestampInMillis()) / Agent.HARVEST_PERIOD);
        this.reportedCounts = new ConcurrentHashMap<AgentName, AtomicInteger>();
    }

    public int getAgentId() {
        return agentId;
    }

    public int getMetricId() {
        return metricId;
    }

    public ATimeslicedValue getValue() {
        return value;
    }

    public int getHarvestableCycles() {
        return harvestableCycles;
    }

    public ATimeslicedValue getReportableValue(long nowInMillis, AgentName agentName) {

        ATimeslicedValue reportableValue = null;
        reportedCounts.putIfAbsent(agentName, new AtomicInteger(0));
        AtomicInteger reportedCount = reportedCounts.get(agentName);

        if (reportedCount.getAndIncrement() < harvestableCycles) {
            if (value instanceof IntegerTimeslicedValue) {
                reportableValue =
                    new IntegerTimeslicedValue(value.getType(), nowInMillis - Agent.HARVEST_PERIOD,
                        nowInMillis, value.getBlameStackSnapshot(), (int) normalizeCount(
                            value.getDataPointCount(), value.getType()), value.dataIsAbsent(),
                        (int) normalizeValue(((IntegerTimeslicedValue) value).getValue(),
                            value.getType()), ((IntegerTimeslicedValue) value).getMinimum(),
                        ((IntegerTimeslicedValue) value).getMaximum());
            } else if (value instanceof LongTimeslicedValue) {
                reportableValue =
                    new LongTimeslicedValue(value.getType(), nowInMillis - Agent.HARVEST_PERIOD,
                        nowInMillis, value.getBlameStackSnapshot(), normalizeCount(
                            value.getDataPointCount(), value.getType()), value.dataIsAbsent(),
                        normalizeValue(((LongTimeslicedValue) value).getValue(), value.getType()),
                        ((LongTimeslicedValue) value).getMinimum(),
                        ((LongTimeslicedValue) value).getMaximum());
            } else if (value instanceof StringTimeslicedValue) {
                reportableValue =
                    new StringTimeslicedValue(value.getType(), nowInMillis - Agent.HARVEST_PERIOD,
                        nowInMillis, value.getBlameStackSnapshot(), normalizeCount(
                            value.getDataPointCount(), value.getType()), value.dataIsAbsent(),
                        ((StringTimeslicedValue) value).getString());
            }

            if (reportedCount.get() == harvestableCycles) {
                // this agent metric already reported enough cycles,
                // reset the counter to 0 for next run
                reportedCount.set(0);
            }
        }

        return reportableValue;
    }

    private long normalizeCount(long dataValue, int metricType) {
        // TypeInquisitor typeInfo = new TypeInquisitor(metricType);
        // From the analysis of metric Gatherers,
        // most of the types of metrics needs to follow the split in count,
        // so no type check is added as of now.
        return dataValue / harvestableCycles;
    }

    private long normalizeValue(long dataValue, int metricType) {
        TypeInquisitor typeInfo = new TypeInquisitor(metricType);
        if (typeInfo.isIntervalCounter()) { // interval counter
            return dataValue / harvestableCycles;
        } else { // saturation, fluctuating counter, duration, monolithic counter
            return dataValue;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        this.harvestableCycles =
            (int) ((value.getStopTimestampInMillis() - value.getStartTimestampInMillis()) / Agent.HARVEST_PERIOD);
        this.reportedCounts = new ConcurrentHashMap<AgentName, AtomicInteger>();
    }
}
