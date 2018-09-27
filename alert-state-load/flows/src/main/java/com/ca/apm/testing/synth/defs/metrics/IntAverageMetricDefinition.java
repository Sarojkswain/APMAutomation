/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public class IntAverageMetricDefinition extends IntMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public IntAverageMetricDefinition(String prefix, String name, MetricGroup owner, Integer defaultValue) {
        super(prefix, MetricType.IntAverage, name, owner, defaultValue);
    }

    /**
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public IntAverageMetricDefinition(String prefix, String name, MetricGroup owner,
        Integer minValue, Integer maxValue) {
        super(prefix, MetricType.IntAverage, name, owner, minValue, maxValue);
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kIntegerDuration;
    }
}
