package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import java.io.File;

import org.jfree.data.xy.XYDataset;

public class XYDatasetHolder {

    private XYDataset dataset;
    private File file;
    private ChartDescriptor chartDescriptor;

    protected XYDatasetHolder() {}

    protected XYDatasetHolder(XYDataset dataset, File file, ChartDescriptor chartDescriptor) {
        this.dataset = dataset;
        this.file = file;
        this.chartDescriptor = chartDescriptor;
    }

    public XYDataset getDataset() {
        return dataset;
    }

    public void setDataset(XYDataset dataset) {
        this.dataset = dataset;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ChartDescriptor getChartDescriptor() {
        return chartDescriptor;
    }

    public void setChartDescriptor(ChartDescriptor chartDescriptor) {
        this.chartDescriptor = chartDescriptor;
    }

}
