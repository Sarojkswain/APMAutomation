/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.server.beans.agent.ICompressedTimesliceData;

/**
 * @author keyja01
 *
 */
public interface MetricFactory {
    public static final String HIGH_PROFILE = "high";
    
    public AgentMetricData[] generateMetricData() throws BadlyFormedNameException;
    
    public ICompressedTimesliceData generateCompressedMetricData() throws BadlyFormedNameException;
    
    public void setActiveProfile(String profileName);
}
