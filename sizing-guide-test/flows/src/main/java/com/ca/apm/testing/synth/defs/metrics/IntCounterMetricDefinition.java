/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public class IntCounterMetricDefinition extends IntMetricDefinition {

    /**
     * @param name
     * @param owner
     * @param defaultValue
     */
    public IntCounterMetricDefinition(String prefix, String name, MetricGroup owner, Integer defaultValue) {
        super(prefix, MetricType.IntCounter, name, owner, defaultValue);
    }

    /**
     * @param name
     * @param owner
     * @param minValue
     * @param maxValue
     */
    public IntCounterMetricDefinition(String prefix, String name, MetricGroup owner, Integer minValue, Integer maxValue) {
        super(prefix, MetricType.IntCounter, name, owner, minValue, maxValue);
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kIntegerFluctuatingCounter;
    }

}
