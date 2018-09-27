package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jfree.data.xy.XYSeries;

public class ChartDescriptor {

    private final String id;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private int chartWidth;
    private int chartHeight;
    private Collection<ChartXYSeriesDescriptor> xySeriesDescriptors;

    public ChartDescriptor(String id) {
        this.id = id;
    }

    // no XYSeries
    public ChartDescriptor(String id, String title, String xAxisLabel, String yAxisLabel,
        int chartWidth, int chartHeight) {
        this(id, title, xAxisLabel, yAxisLabel, chartWidth, chartHeight,
            new ArrayList<ChartXYSeriesDescriptor>());
    }

    // one XYSeries
    public ChartDescriptor(String id, String title, String xAxisLabel, String yAxisLabel,
        int chartWidth, int chartHeight, String xySeriesKey, int indexX, int indexY,
        BaseXYDataItemRowMapper xyDataItemRowMapper) {
        this(id, title, xAxisLabel, yAxisLabel, chartWidth, chartHeight,
            new ChartXYSeriesDescriptor(xySeriesKey, indexX, indexY, xyDataItemRowMapper));
    }

    // one XYSeries
    public ChartDescriptor(String id, String title, String xAxisLabel, String yAxisLabel,
        int chartWidth, int chartHeight, ChartXYSeriesDescriptor xySeriesDescriptor) {
        this(id, title, xAxisLabel, yAxisLabel, chartWidth, chartHeight, new ArrayList<>(
            Collections.singleton(xySeriesDescriptor)));
    }

    // more of XYSeries
    public ChartDescriptor(String id, String title, String xAxisLabel, String yAxisLabel,
        int chartWidth, int chartHeight, Collection<ChartXYSeriesDescriptor> xySeriesDescriptors) {
        this(id);
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.chartWidth = chartWidth;
        this.chartHeight = chartHeight;
        this.xySeriesDescriptors = xySeriesDescriptors;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public void setChartWidth(int chartWidth) {
        this.chartWidth = chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public void setChartHeight(int chartHeight) {
        this.chartHeight = chartHeight;
    }

    public Collection<ChartXYSeriesDescriptor> getXYSeriesDescriptors() {
        return xySeriesDescriptors;
    }

    public void setXYSeriesDescriptors(Collection<ChartXYSeriesDescriptor> xySeriesDescriptors) {
        this.xySeriesDescriptors = xySeriesDescriptors;
    }

    public boolean addXYSeriesDescriptor(ChartXYSeriesDescriptor xySeriesDescriptors) {
        if (this.xySeriesDescriptors == null) {
            this.xySeriesDescriptors = new ArrayList<>();
        }
        return this.xySeriesDescriptors.add(xySeriesDescriptors);
    }

    public boolean addXYSeriesDescriptors(Collection<ChartXYSeriesDescriptor> xySeriesDescriptors) {
        if (this.xySeriesDescriptors == null) {
            this.xySeriesDescriptors = new ArrayList<>(xySeriesDescriptors);
            return true;
        } else {
            return this.xySeriesDescriptors.addAll(xySeriesDescriptors);
        }
    }

    public static class ChartXYSeriesDescriptor {
        private String xySeriesKey;
        private int indexX; // column index within a csv data file
        private int indexY; // column index within a csv data file
        private XYSeries jFreeChartXYSeries;
        private BaseXYDataItemRowMapper xyDataItemRowMapper;

        public ChartXYSeriesDescriptor(String xySeriesKey, int indexX, int indexY,
            BaseXYDataItemRowMapper xyDataItemRowMapper) {
            this.xySeriesKey = xySeriesKey;
            this.indexX = indexX;
            this.indexY = indexY;
            this.jFreeChartXYSeries = new XYSeries(xySeriesKey);
            this.xyDataItemRowMapper = xyDataItemRowMapper;
            this.xyDataItemRowMapper.setIndexX(indexX);
            this.xyDataItemRowMapper.setIndexY(indexY);
        }

        public String getXySeriesKey() {
            return xySeriesKey;
        }

        public int getIndexX() {
            return indexX;
        }

        public int getIndexY() {
            return indexY;
        }

        public XYSeries getJFreeChartXYSeries() {
            return jFreeChartXYSeries;
        }

        public BaseXYDataItemRowMapper getXYDataItemRowMapper() {
            return xyDataItemRowMapper;
        }
    }

}
