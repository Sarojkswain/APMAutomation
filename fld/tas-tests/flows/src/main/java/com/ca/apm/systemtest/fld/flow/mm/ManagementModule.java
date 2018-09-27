/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author keyja01
 *
 */
public class ManagementModule {
    private String name;
    
    private List<MetricGroup> metricGroups = new ArrayList<>();
    private List<Dashboard> dashboards = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetricGroup> getMetricGroups() {
        return metricGroups;
    }

    public void setMetricGroups(List<MetricGroup> metricGroups) {
        this.metricGroups = metricGroups;
    }

    public void addMetricGroup(MetricGroup mg) {
        if (metricGroups == null) {
            metricGroups = new ArrayList<>();
        }
        metricGroups.add(mg);
    }
    
    public List<Dashboard> getDashboards() {
        return dashboards;
    }

    public void setDashboards(List<Dashboard> dashboards) {
        this.dashboards = dashboards;
    }
    
    public void addDashboard(Dashboard d) {
        if (dashboards == null) {
            dashboards = new ArrayList<>();
        }
        dashboards.add(d);
    }

    public String toXml() {
        Document doc = DocumentHelper.createDocument();
        
        Element root = doc.addElement("ManagementModule");
        root.addNamespace("ns1", "http://www.w3.org/2005/11/its")
            .addNamespace("introscope", "generated://introscope.xsd")
            .addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .addAttribute("xsi:noNamespaceSchemaLocation", "bundle://com.wily.introscope.em/com/wily/introscope/server/enterprise/entity/bundle/IntroscopeManagementModules4.0.xsd")
            .addAttribute("Editable", "true")
            .addAttribute("Version", "4.0")
            .addAttribute("IsActive", "true")
            .addAttribute("DescriptionContentType", "text/plain");
        
        root.addElement("Name").setText(name);
        Element dgs = root.addElement("DataGroups");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "TriageMapAlertDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "MapEntityMetricGroupingDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "EntityDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "CalculatorDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "AlertDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "UnmonitoredComponentGroupDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "introscope:AlertDowntimeScheduleDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "SnmpCollectionDataGroup");
        Element mgs = dgs.addElement("DataGroup").addAttribute("xsi:type", "MetricGroupingDataGroup");
        for (MetricGroup mg: metricGroups) {
            mg.toXml(mgs);
        }
        Element dsg = dgs.addElement("DataGroup").addAttribute("xsi:type", "DashboardDataGroup");
        for (Dashboard d: dashboards) {
            d.toXml(dsg);
        }
        dgs.addElement("DataGroup").addAttribute("xsi:type", "ImageDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "DifferentialControlDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "ReportDataGroup");
        dgs.addElement("DataGroup").addAttribute("xsi:type", "ActionDataGroup");
        
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter(out, format);
        try {
            writer.write(doc);
            
            return out.toString();
        } catch (IOException e) {
            return null;
        }
    }
    
    public static void main(String[] args) {
        ManagementModule mm = new ManagementModule();
        mm.setName("MyModule");
        MetricGroup mg = new MetricGroup("TomcatMG", "This is the description of the metric group");
        mg.addFullMetricSpecifier(new FullMetricSpecifier("(.*)\\|tomcat2Agent\\|(.*)", "GC Heap:Bytes In Use"));
        mg.addFullMetricSpecifier(new FullMetricSpecifier("(.*)\\|tomcat2Agent\\|(.*)", "GC Heap:Bytes Total"));
        
        mm.addMetricGroup(mg);
        System.out.println(mm.toXml());
    }
}
