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
public class Dashboard {
    private String name;
    private String description;
    private int height;
    private int width;
    private int gridSize = 20;
    private List<DashboardWidget> widgets;
    
    /**
     * @param name
     * @param description
     * @param height
     * @param width
     * @param gridSize
     */
    public Dashboard(String name, String description, int height, int width, int gridSize, List<DashboardWidget> widgets) {
        this.name = name;
        this.description = description;
        this.height = height;
        this.width = width;
        this.gridSize = gridSize;
        this.widgets = widgets;
    }
    
    /**
     * @param name
     * @param description
     * @param height
     * @param width
     * @param gridSize
     */
    public Dashboard(String name, String description, int height, int width, int gridSize) {
        this(name, description, height, width, gridSize, new ArrayList<>());
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public List<DashboardWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<DashboardWidget> widgets) {
        this.widgets = widgets;
    }
    
    public void addWidget(DashboardWidget widget) {
        if (widgets == null) {
            widgets = new ArrayList<>();
        }
        widgets.add(widget);
    }
    
    public void toXml(Element parent) {
        Element dashboard = parent.addElement("Dashboard").addAttribute("PixelHeight", Integer.toString(height))
            .addAttribute("PixelWidth", Integer.toString(width))
            .addAttribute("GridHeight", Integer.toString(gridSize))
            .addAttribute("GridWidth", Integer.toString(gridSize))
            .addAttribute("ClearAgentSpecifier", "false")
            .addAttribute("IsActive", "true")
            .addAttribute("DescriptionContentType", "text/plain");
        dashboard.addElement("Name").addAttribute("ns1:translate", "yes").setText(name);
        dashboard.addElement("Description").addAttribute("ns1:translate", "yes")
            .setText(description == null ? "" : description);
        
        for (DashboardWidget widget: widgets) {
            widget.toXml(dashboard);
        }
    }
}
