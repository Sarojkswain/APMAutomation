package com.ca.apm.systemtest.fld.flow.mm;

import org.dom4j.Element;

public class FullMetricSpecifier {
    private String processSpecifier;
    
    private String metricSpecifier;
    
    public FullMetricSpecifier() {
    }

    /**
     * @param processSpecifier
     * @param metricSpecifier
     */
    public FullMetricSpecifier(String processSpecifier, String metricSpecifier) {
        this.processSpecifier = processSpecifier;
        this.metricSpecifier = metricSpecifier;
    }

    public String getProcessSpecifier() {
        return processSpecifier;
    }

    public void setProcessSpecifier(String processSpecifier) {
        this.processSpecifier = processSpecifier;
    }

    public String getMetricSpecifier() {
        return metricSpecifier;
    }

    public void setMetricSpecifier(String metricSpecifier) {
        this.metricSpecifier = metricSpecifier;
    }
    
    public void toXml(Element parent) {
        Element entry = parent.addElement("FullMetricSpecifierEntry");
        entry.addElement("DomainSpecifier").addElement("DomainSpecifierRegExp").setText("(.*)");
        entry.addElement("ProcessSpecifier").addElement("ProcessSpecifierRegExp").setText(processSpecifier);
        entry.addElement("MetricSpecifier").addElement("MetricSpecifierRegExp").setText(metricSpecifier);
    }
}
