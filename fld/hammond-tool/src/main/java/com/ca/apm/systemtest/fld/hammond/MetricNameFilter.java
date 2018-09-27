/**
 * 
 */
package com.ca.apm.systemtest.fld.hammond;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wily.introscope.spec.metric.AgentMetric;

/**
 * @author keyja01
 *
 */
public class MetricNameFilter implements Filter {
    private Pattern pattern;
    private Integer prefixLength;
    private Set<String> allowed;
    private boolean excludeOnMatch;
    
    /**
     * 
     * @param regex
     * @param prefixLength
     * @param allowedNames list of names to check against
     * @param excludeOnMatch if true, the metric will
     */
    public MetricNameFilter(String regex, Integer prefixLength, String[] allowedNames, boolean excludeOnMatch) {
        pattern = Pattern.compile(regex);
        this.prefixLength = prefixLength;
        allowed = new HashSet<>();
        this.excludeOnMatch = excludeOnMatch;
        if (allowedNames != null) {
            for (String name: allowedNames) {
                allowed.add(name);
            }
        }
    }
    
    
    @Override
    public boolean metricIsExcluded(AgentMetric agentMetric) {
        Matcher m = pattern.matcher(agentMetric.toString());
        int prefixLength = agentMetric.getAgentMetricPrefix().getSegmentCount();
        String name = agentMetric.getAttributeName();
        boolean nameMatches = allowed.contains(name);
        
        if (this.prefixLength != null && !this.prefixLength.equals(prefixLength)) {
            return false;
        }
        
        if (!m.matches()) {
            return false;
        }
        
        if (excludeOnMatch) {
            return nameMatches;
        } else {
            return !nameMatches;
        }
    }
}
