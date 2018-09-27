/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import org.dom4j.Element;

/**
 * @author keyja01
 *
 */
public class DashboardWidget {
    public static enum DashboardWidgetType {
        LiveGraph, LiveBarChart
    }

    private DashboardWidgetType type;
    private String dataProviderManagementModule;
    private String dataProviderMetricGroup;
    private Integer topN;
    private int id;
    private int originX;
    private int originY;
    private int cornerX;
    private int cornerY;
    private boolean showLegends = true;
    
    
    /**
     * @param dataProviderManagementModule
     * @param dataProviderMetricGroup
     * @param id
     * @param originX
     * @param originY
     * @param cornerX
     * @param cornerY
     */
    public DashboardWidget(String dataProviderManagementModule, String dataProviderMetricGroup,
        int id, int originX, int originY, int cornerX, int cornerY) {
        this.dataProviderManagementModule = dataProviderManagementModule;
        this.dataProviderMetricGroup = dataProviderMetricGroup;
        this.id = id;
        this.originX = originX;
        this.originY = originY;
        this.cornerX = cornerX;
        this.cornerY = cornerY;
    }

    /**
     * @param type
     * @param dataProviderManagementModule
     * @param dataProviderMetricGroup
     * @param topN
     * @param id
     * @param originX
     * @param originY
     * @param cornerX
     * @param cornerY
     * @param showLegends
     */
    public DashboardWidget(DashboardWidgetType type, String dataProviderManagementModule,
        String dataProviderMetricGroup, Integer topN, int id, int originX, int originY, int cornerX,
        int cornerY, boolean showLegends) {
        this.type = type;
        this.dataProviderManagementModule = dataProviderManagementModule;
        this.dataProviderMetricGroup = dataProviderMetricGroup;
        this.topN = topN;
        this.id = id;
        this.originX = originX;
        this.originY = originY;
        this.cornerX = cornerX;
        this.cornerY = cornerY;
        this.showLegends = showLegends;
    }

    public DashboardWidgetType getType() {
        return type;
    }

    public void setType(DashboardWidgetType type) {
        this.type = type;
    }

    public String getDataProviderManagementModule() {
        return dataProviderManagementModule;
    }

    public void setDataProviderManagementModule(String dataProviderManagementModule) {
        this.dataProviderManagementModule = dataProviderManagementModule;
    }

    public String getDataProviderMetricGroup() {
        return dataProviderMetricGroup;
    }

    public void setDataProviderMetricGroup(String dataProviderMetricGroup) {
        this.dataProviderMetricGroup = dataProviderMetricGroup;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        this.originX = originX;
    }

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        this.originY = originY;
    }

    public int getCornerX() {
        return cornerX;
    }

    public void setCornerX(int cornerX) {
        this.cornerX = cornerX;
    }

    public int getCornerY() {
        return cornerY;
    }

    public void setCornerY(int cornerY) {
        this.cornerY = cornerY;
    }

    public boolean isShowLegends() {
        return showLegends;
    }

    public void setShowLegends(boolean showLegends) {
        this.showLegends = showLegends;
    }

    public void toXml(Element parent) {
        Element widget = parent.addElement("Widget").addAttribute("Type", type.toString());
        // set up metric group for display
        Element provider = widget.addElement("WidgetProperty").addAttribute("Name", "DataProvider");
        Element providerVal =
            provider.addElement("Value").addAttribute("xsi:type", "DataProviderType");
        Element metGroupId = providerVal.addElement("MetricGroupingID");
        metGroupId.addElement("ManagementModuleName").setText(dataProviderManagementModule);
        metGroupId.addElement("ConstructName").setText(dataProviderMetricGroup);
        Element topNFilter = providerVal.addElement("TopNFilter")
            .addAttribute("IsActive", topN == null ? "false" : "true").addAttribute("IsTop", "true")
            .addAttribute("FilterSize", topN == null ? "10" : topN.toString());
        topNFilter.addElement("IncludeList");
        topNFilter.addElement("ExcludeList");

        widget.addElement("WidgetProperty").addAttribute("Name", "Frequency").addElement("Value")
            .addAttribute("xsi:type", "IntegerType").addAttribute("Value", "15");

        Element scaleValue = widget.addElement("WidgetProperty").addAttribute("Name", "Scale")
            .addElement("Value").addAttribute("xsi:type", "ScaleType");
        scaleValue.addElement("ScaleMin").setText("0.0");
        scaleValue.addElement("ScaleMax").setText("5.0");
        scaleValue.addElement("ScaleDefaultMin").setText("0.0");
        scaleValue.addElement("ScaleDefaultMax").setText("5.0");
        scaleValue.addElement("ScaleAutoExpandMin").setText("true");
        scaleValue.addElement("ScaleAutoExpandMax").setText("true");
        scaleValue.addElement("ScaleAutoCollapseMin").setText("false");
        scaleValue.addElement("ScaleAutoCollapseMax").setText("false");

        widget.addElement("WidgetProperty").addAttribute("Name", "ShowMinMax").addElement("Value")
            .addAttribute("xsi:type", "TextType").addAttribute("Value", "false");
        widget.addElement("WidgetProperty").addAttribute("Name", "ID").addElement("Value")
            .addAttribute("xsi:type", "IntegerType").addAttribute("Value", Integer.toString(id));
        widget.addElement("WidgetProperty").addAttribute("Name", "LabelsVisible")
            .addElement("Value").addAttribute("xsi:type", "TextType")
            .addAttribute("Value", Boolean.toString(showLegends));
        if (showLegends) {
            widget.addElement("WidgetProperty").addAttribute("Name", "GraphPercentageSize")
                .addElement("Value").addAttribute("xsi:type", "IntegerType")
                .addAttribute("Value", "67");
        }
        widget.addElement("WidgetProperty").addAttribute("Name", "CanHaveLinks").addElement("Value")
            .addAttribute("xsi:type", "TextType").addAttribute("Value", "CanHaveLinks");

        Element dispBox = widget.addElement("DisplayBox");
        dispBox.addElement("OriginPoint").addAttribute("XCoordinate", Integer.toString(originX))
            .addAttribute("YCoordinate", Integer.toString(originY));

        dispBox.addElement("CornerPoint").addAttribute("XCoordinate", Integer.toString(cornerX))
            .addAttribute("YCoordinate", Integer.toString(cornerY));
    }
}
/*
 * 
 * <DisplayBox>
 * <OriginPoint XCoordinate="24" YCoordinate="20" />
 * <CornerPoint XCoordinate="324" YCoordinate="268" />
 * </DisplayBox>
 * 
 * 
 * 
 * <Widget Type="LiveGraph">
 * <WidgetProperty Name="DataProvider">
 * <Value xsi:type="DataProviderType">
 * <MetricGroupingID>
 * <ManagementModuleName>MetricGroup001</ManagementModuleName>
 * <ConstructName>TomcatMG</ConstructName>
 * </MetricGroupingID>
 * <TopNFilter IsActive="true" IsTop="true" FilterSize="10">
 * <IncludeList />
 * <ExcludeList />
 * </TopNFilter>
 * </Value>
 * </WidgetProperty>
 * <WidgetProperty Name="Frequency">
 * <Value xsi:type="IntegerType" Value="15" />
 * </WidgetProperty>
 * <WidgetProperty Name="Scale">
 * <Value xsi:type="ScaleType">
 * <ScaleMin>0.0</ScaleMin>
 * <ScaleMax>5.0</ScaleMax>
 * <ScaleDefaultMin>0.0</ScaleDefaultMin>
 * <ScaleDefaultMax>5.0</ScaleDefaultMax>
 * <ScaleAutoExpandMin>true</ScaleAutoExpandMin>
 * <ScaleAutoExpandMax>true</ScaleAutoExpandMax>
 * <ScaleAutoCollapseMin>false</ScaleAutoCollapseMin>
 * <ScaleAutoCollapseMax>false</ScaleAutoCollapseMax>
 * </Value>
 * </WidgetProperty>
 * <WidgetProperty Name="ShowMinMax">
 * <Value xsi:type="TextType" Value="false" />
 * </WidgetProperty>
 * <WidgetProperty Name="ID">
 * <Value xsi:type="IntegerType" Value="1" />
 * </WidgetProperty>
 * <WidgetProperty Name="LabelsVisible">
 * <Value xsi:type="TextType" Value="true" />
 * </WidgetProperty>
 * <WidgetProperty Name="GraphPercentageSize">
 * <Value xsi:type="IntegerType" Value="67" />
 * </WidgetProperty>
 * <WidgetProperty Name="CanHaveLinks">
 * <Value xsi:type="TextType" Value="CanHaveLinks" />
 * </WidgetProperty>
 * <DisplayBox>
 * <OriginPoint XCoordinate="24" YCoordinate="20" />
 * <CornerPoint XCoordinate="324" YCoordinate="268" />
 * </DisplayBox>
 * </Widget>
 * 
 * 
 * 
 * 
 */
