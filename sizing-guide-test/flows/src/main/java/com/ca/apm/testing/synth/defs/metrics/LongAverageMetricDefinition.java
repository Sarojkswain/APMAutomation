/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public class LongAverageMetricDefinition extends LongMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public LongAverageMetricDefinition(String prefix, String name, MetricGroup owner, Long defaultValue) {
        super(prefix, MetricType.LongAverage, name, owner, defaultValue);
    }

    /**
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public LongAverageMetricDefinition(String prefix, String name, MetricGroup owner, Long minValue, Long maxValue) {
        super(prefix, MetricType.LongAverage, name, owner, minValue, maxValue);
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kLongDuration;
    }
}
