/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import java.util.HashMap;
import java.util.Map;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.Metric;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.introscope.stat.timeslice.StringTimeslicedValue;

/**
 * @author keyja01
 *
 */
public class StringEventMetricDefinition extends AbstractMetricDefinition {
    private String value;
    private Map<String, String> profileMap = new HashMap<String, String>();

    /**
     * @param type
     * @param name
     * @param owner
     */
    public StringEventMetricDefinition(String prefix, String name, MetricGroup owner, String value) {
        super(MetricType.StringEvent, name, owner, prefix);
        this.value = value;
    }
    
    public void addProfileValue(String profile, String value) {
        if (profile != null && !profile.isEmpty()) {
            profileMap.put(profile, value);
        }
    }
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.synth.MetricDefinition#generateXmlMetric()
     */
    @Override
    public String generateXmlMetric() {
        String val = null;
        if (profile != null) {
            val = profileMap.get(profile);
        }
        if (val == null) {
            val = value;
        }
        StringBuffer sb = new StringBuffer("<metric type=\"").append(type.toString()).append("\" name=\"")
            .append(owner.generatePath()).append(":").append(name).append("\" value=\"")
            .append(val).append("\" />");
        
        return sb.toString();
    }

    @Override
    public AgentMetricData generateAgentMetricData() {
        AgentMetricData data = null;
        try {
            String path = prefix + owner.generatePath();
            String metricName = path + ":" + this.name;
            Metric m = Metric.getMetric(metricName, getMetricAttributeType());
            
            StringTimeslicedValue tsValue = new StringTimeslicedValue(getMetricAttributeType(), 0, 0, null, value);

            data = new AgentMetricData(m.getAgentMetric(), Frequency.kDefaultAgentFrequency, tsValue);
        } catch (Exception e) {
        }        
        
        return data;
    }

    @Override
    protected int getMetricAttributeType() {
        return MetricTypes.kStringConstant;
    }

}
