package com.ca.apm.systemtest.fld.hammond;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wily.introscope.spec.metric.AgentMetric;

public class SimpleFilter implements Filter {
    private Pattern pattern;
    private Integer prefixLength;
    
    
    public SimpleFilter(String regex) {
        this(regex, null);
    }
    
    public SimpleFilter(String regex, Integer prefixLength) {
        pattern = Pattern.compile(regex);
        this.prefixLength = prefixLength;
    }
    
    @Override
    public boolean metricIsExcluded(AgentMetric agentMetric) {
        Matcher m = pattern.matcher(agentMetric.toString());
        int prefixLength = agentMetric.getAgentMetricPrefix().getSegmentCount();
        
        if (this.prefixLength != null) {
            return (this.prefixLength.equals(prefixLength)) && m.matches();
        } else {
            return m.matches();
        }
    }
}