/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author keyja01
 *
 */
public abstract class AbstractNumericMetricDefinition<T extends Number> extends AbstractMetricDefinition {
    protected HashMap<String, List<T>> map = new HashMap<>();
    protected SecureRandom rand = new SecureRandom();

    public AbstractNumericMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, T defaultValue) {
        this(prefix, type, name, owner, defaultValue, defaultValue);
    }

    public AbstractNumericMetricDefinition(String prefix, MetricType type, String name, MetricGroup owner, T minValue, T maxValue) {
        super(type, name, owner, prefix);
        List<T> aa = Arrays.asList(minValue, maxValue);
        map.put(DEFAULT, aa);
    }
    
    
    public void addValue(String profileName, T value) {
        addRange(profileName, value, value);
    }
    
    public void addRange(String profileName, T minValue, T maxValue) {
        List<T> aa = Arrays.asList(minValue, maxValue);
        map.put(profileName, aa);
    }
    
    
    protected abstract T generateValue();
    
    @Override
    public String generateXmlMetric() {
        T val = generateValue();
        StringBuffer sb = new StringBuffer("<metric type=\"").append(type.toString()).append("\" name=\"")
            .append(owner.generatePath()).append(":").append(name).append("\" value=\"")
            .append(val).append("\" />");
        
        return sb.toString();
    }
}
