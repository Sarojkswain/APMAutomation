/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import java.util.List;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.Metric;
import com.wily.introscope.stat.timeslice.IntegerTimeslicedValue;

/**
 * @author keyja01
 *
 */
public abstract class IntMetricDefinition extends AbstractNumericMetricDefinition<Integer> {

    public IntMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, Integer defaultValue) {
        super(prefix, type, name, owner, defaultValue);
    }
    
    public IntMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, Integer minValue, Integer maxValue) {
        super(prefix, type, name, owner, minValue, maxValue);
    }


    @Override
    protected Integer generateValue() {
        List<Integer> aa = map.get(profile);
        if (aa == null) {
            aa = map.get(DEFAULT);
        }
        
        int t0 = aa.get(0);
        int t1 = aa.get(1);
        int delta = t1 - t0;
        
        if (delta != 0) {
            return t0 + rand.nextInt(delta);
        } else {
            return t0;
        }
    }

    
    public AgentMetricData generateAgentMetricData() {
        AgentMetricData data = null;
        try {
            String path = prefix + owner.generatePath();
            String metricName = path + ":" + this.name;
            Metric m = Metric.getMetric(metricName, getMetricAttributeType());
            int value = this.generateValue();

            IntegerTimeslicedValue tsValue = new IntegerTimeslicedValue(m.getAttributeType(), 0, 0, null, 1, false, value, value, value);
            data = new AgentMetricData(m.getAgentMetric(), Frequency.kDefaultAgentFrequency, tsValue);
        } catch (Exception e) {
        }        
        
        return data;
    }
}
