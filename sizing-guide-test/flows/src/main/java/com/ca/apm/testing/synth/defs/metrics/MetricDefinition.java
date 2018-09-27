/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import com.wily.introscope.spec.metric.AgentMetricData;


/**
 * @author keyja01
 *
 */
public interface MetricDefinition {
    public static final String DEFAULT = "default";
    
    /**
     * Generates XML Metric data suitable for sending to EPAgent
     * @return
     */
    public String generateXmlMetric();
    
    /**
     * Generates {@link AgentMetricData} suitable for sending over Isengard directly
     * @return
     */
    public AgentMetricData generateAgentMetricData();
    
    public void setProfile(String profileName);
    
    public void setOwner(MetricGroup owner);
}
