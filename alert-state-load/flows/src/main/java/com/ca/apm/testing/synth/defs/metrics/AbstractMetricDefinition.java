/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

/**
 * @author keyja01
 *
 */
public abstract class AbstractMetricDefinition implements MetricDefinition {
    protected MetricType type;
    protected String name;
    protected MetricGroup owner;
    protected String profile = DEFAULT;
    protected String prefix;
    

    public AbstractMetricDefinition(MetricType type, String name, MetricGroup owner, String prefix) {
        this.type = type;
        this.name = name;
        this.owner = owner;
        this.prefix = prefix;
        owner.addMetricDefinition(this);
    }
    
    protected abstract int getMetricAttributeType();

    public MetricType getType() {
        return type;
    }

    public void setType(MetricType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricGroup getOwner() {
        return owner;
    }

    public void setOwner(MetricGroup owner) {
        this.owner = owner;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
