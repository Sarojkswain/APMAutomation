/**
 * 
 */
package com.ca.apm.systemtest.fld.hammond;

import com.wily.introscope.spec.metric.AgentMetric;

/**
 * @author keyja01
 *
 */
public interface Filter {
    boolean metricIsExcluded(AgentMetric agentMetric);
}
