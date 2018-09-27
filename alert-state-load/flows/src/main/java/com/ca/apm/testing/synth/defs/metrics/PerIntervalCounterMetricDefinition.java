/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.Metric;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;

/**
 * @author keyja01
 *
 */
public class PerIntervalCounterMetricDefinition extends IntMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public PerIntervalCounterMetricDefinition(String prefix, String name, MetricGroup owner, Integer defaultValue) {
        this(prefix, name, owner, defaultValue, defaultValue);
    }

    /**
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public PerIntervalCounterMetricDefinition(String prefix, String name, MetricGroup owner, Integer minValue, Integer maxValue) {
        super(prefix, MetricType.PerIntervalCounter, name, owner, minValue, maxValue);
    }

    
    public AgentMetricData generateAgentMetricData() {
        AgentMetricData data = null;
        try {
            String path = prefix + owner.generatePath();
            String metricName = path + ":" + this.name;
            Metric m = Metric.getMetric(metricName, MetricTypes.kIntegerFluctuatingCounter);
            int value = this.generateValue();

            IntegerTimeslicedValue tsValue = new IntegerTimeslicedValue(m.getAttributeType(), 0, 0, null, 1, false, value, value, value);
            data = new AgentMetricData(m.getAgentMetric(), Frequency.kDefaultAgentFrequency, tsValue);
        } catch (Exception e) {
        }        
        
        return data;
    }
    
    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kLongIntervalCounter;
    }
}
