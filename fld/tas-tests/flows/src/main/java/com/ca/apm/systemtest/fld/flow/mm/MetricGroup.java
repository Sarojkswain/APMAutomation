/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

/**
 * @author keyja01
 *
 */
public class MetricGroup {
    private String name = "MetricGroup";
    
    private String description = "";
    
    private List<FullMetricSpecifier> fullMetricSpecs = new ArrayList<>();
    
    public MetricGroup() {
    }

    /**
     * @param name
     * @param description
     */
    public MetricGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FullMetricSpecifier> getFullMetricSpecs() {
        return fullMetricSpecs;
    }

    public void setFullMetricSpecs(List<FullMetricSpecifier> fullMetricSpecs) {
        this.fullMetricSpecs = fullMetricSpecs;
    }
    
    public void addFullMetricSpecifier(FullMetricSpecifier fms) {
        if (fullMetricSpecs == null) {
            fullMetricSpecs = new ArrayList<>();
        }
        
        fullMetricSpecs.add(fms);
    }
    
    public void toXml(Element parent) {
        Element mg = parent.addElement("MetricGrouping")
            .addAttribute("useAgentExpressionFromManagementModule", "false")
            .addAttribute("IsActive", "true")
            .addAttribute("DescriptionContentType", "text/plain");
        mg.addElement("Name").setText(name);
        mg.addElement("Description").setText(description != null ? description : "");
        Element fullMetricSpecifier = mg.addElement("FullMetricSpecifier");
        for (FullMetricSpecifier fms: fullMetricSpecs) {
            fms.toXml(fullMetricSpecifier);
        }
    }
}
