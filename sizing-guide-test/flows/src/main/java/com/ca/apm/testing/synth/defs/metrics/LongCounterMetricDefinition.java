/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.MetricTypes;

/**
 * @author keyja01
 *
 */
public abstract class LongCounterMetricDefinition extends LongMetricDefinition {

    public LongCounterMetricDefinition(String prefix, String name, MetricGroup owner, Long defaultValue) {
        super(prefix, MetricType.LongCounter, name, owner, defaultValue);
    }

    public LongCounterMetricDefinition(String prefix, String name, MetricGroup owner, Long minValue, Long maxValue) {
        super(prefix, MetricType.LongCounter, name, owner, minValue, maxValue);
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kLongFluctuatingCounter;
    }
}
