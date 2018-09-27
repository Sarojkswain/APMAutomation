/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public class IntRateMetricDefinition extends IntMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public IntRateMetricDefinition(String prefix, String name, MetricGroup owner, Integer defaultValue) {
        super(prefix, MetricType.IntRate, name, owner, defaultValue);
    }

    /**
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public IntRateMetricDefinition(String prefix, String name, MetricGroup owner, Integer minValue, Integer maxValue) {
        super(prefix, MetricType.IntRate, name, owner, minValue, maxValue);
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kIntegerRate;
    }
}
