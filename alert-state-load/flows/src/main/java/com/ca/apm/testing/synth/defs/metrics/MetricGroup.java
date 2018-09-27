/**
 * 
 */
package com.ca.apm.testing.synth.defs.metrics;

import java.util.ArrayList;
import java.util.List;

import com.wily.introscope.spec.metric.AgentMetricData;

/**
 * @author keyja01
 *
 */
public class MetricGroup {
    private String name;
    private MetricGroup parent;
    private List<MetricGroup> children;
    private List<MetricDefinition> metrics = new ArrayList<>();
    
    public MetricGroup(String name) {
        this(name, null);
    }
    
    public MetricGroup(String name, MetricGroup parent) {
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
        if (parent != null) {
            parent.addGroup(this);
        }
    }
    
    
    public void generateMetrics(List<String> list) {
        for (MetricDefinition md: metrics) {
            String metric = md.generateXmlMetric();
            list.add(metric);
        }
        for (MetricGroup mg: children) {
            mg.generateMetrics(list);
        }
    }
    
    
    public void generateAgentMetrics(List<AgentMetricData> list) {
        for (MetricDefinition md: metrics) {
            AgentMetricData data = md.generateAgentMetricData();
            list.add(data);
        }
        for (MetricGroup mg: children) {
            mg.generateAgentMetrics(list);
        }
    }
    
    
    public void setActiveProfile(String activeProfile) {
        for (MetricDefinition md: metrics) {
            md.setProfile(activeProfile);
        }
        for (MetricGroup mg: children) {
            mg.setActiveProfile(activeProfile);
        }
    }
    
    
    public String generatePath() {
        if (parent == null) {
            return name;
        } else {
            return parent.generatePath() + "|" + name;
        }
    }

    public void addMetricDefinition(MetricDefinition md) {
        md.setOwner(this);
        metrics.add(md);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricGroup getParent() {
        return parent;
    }

    public void setParent(MetricGroup parent) {
        this.parent = parent;
    }

    public List<MetricGroup> getChildren() {
        return children;
    }

    public void setChildren(List<MetricGroup> children) {
        this.children = children;
    }

    public List<MetricDefinition> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricDefinition> metrics) {
        this.metrics = metrics;
    }

    public void addGroup(MetricGroup childMG) {
        childMG.setParent(this);
        children.add(childMG);
    }
}
