/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public class TimestampMetricDefinition extends LongMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public TimestampMetricDefinition(String prefix, String name, MetricGroup owner, Long defaultValue) {
        super(prefix, MetricType.Timestamp, name, owner, defaultValue);
    }
    
    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kLongTimestamp;
    }
}
