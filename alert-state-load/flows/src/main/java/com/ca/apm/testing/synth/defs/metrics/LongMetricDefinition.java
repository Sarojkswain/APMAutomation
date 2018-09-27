/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import java.util.List;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.Metric;
import com.wily.introscope.stat.timeslice.LongTimeslicedValue;

/**
 * @author keyja01
 *
 */
public abstract class LongMetricDefinition extends AbstractNumericMetricDefinition<Long> {

    /**
     * @param type
     * @param name
     * @param owner
     * @param defaultValue
     */
    public LongMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, Long defaultValue) {
        super(prefix, type, name, owner, defaultValue);
    }

    /**
     * @param type
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public LongMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, Long minValue, Long maxValue) {
        super(prefix, type, name, owner, minValue, maxValue);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.synth.AbstractNumericMetricDefinition#generateValue()
     */
    @Override
    protected Long generateValue() {
        List<Long> aa = map.get(profile);
        if (aa == null) {
            aa = map.get(DEFAULT);
        }
        
        long t0 = aa.get(0);
        long t1 = aa.get(1);
        long delta = t1 - t0;
        if (delta < 0) {
            delta = 0;
        }
        
        long dx = 0;
        
        long maxInt = Integer.MAX_VALUE;
        if (delta == 0) {
            return t0;
        } else if (delta < maxInt) {
            dx = rand.nextInt((int) delta);
            return t0 + dx;
        } else {
            if (delta != 0) {
                while (true) {
                    dx = rand.nextLong();
                    if (dx >= 0 && dx < delta) {
                        return t0 + dx;
                    }
                }
            } else {
                return t0;
            }
        }
        
    }

    
    public AgentMetricData generateAgentMetricData() {
        AgentMetricData data = null;
        try {
            String path = prefix + owner.generatePath();
            String metricName = path + ":" + this.name;
            Metric m = Metric.getMetric(metricName, getMetricAttributeType());
            long value = this.generateValue();

            LongTimeslicedValue tsValue = new LongTimeslicedValue(m.getAttributeType(), 0, 0, null, 1, false, value, value, value);
            data = new AgentMetricData(m.getAgentMetric(), Frequency.kDefaultAgentFrequency, tsValue);
        } catch (Exception e) {
        }        
        
        return data;
    }
}
