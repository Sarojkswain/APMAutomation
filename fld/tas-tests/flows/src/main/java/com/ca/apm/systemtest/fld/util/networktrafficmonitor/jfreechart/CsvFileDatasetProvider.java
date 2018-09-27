package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart.ChartDescriptor.ChartXYSeriesDescriptor;

public class CsvFileDatasetProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileDatasetProvider.class);

    private String separator;
    private Charset charset;

    public Collection<XYDatasetHolder> loadDatasets(File file,
        Collection<ChartDescriptor> chartDescriptors) throws IOException {
        LOGGER.info("CsvFileDatasetProvider.loadDatasets():: entry");
        try {
            Collection<XYDatasetHolder> xyDatasetHolders = new ArrayList<>(chartDescriptors.size());
            try (LineNumberReader reader =
                new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(
                    file), charset)));) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        int lineNumber = reader.getLineNumber();
                        if (lineNumber % 1000 == 0) {
                            LOGGER.debug(
                                "CsvFileDatasetProvider.getDataset():: file = {}, line number: {}",
                                file, lineNumber);
                        }

                        for (ChartDescriptor chartDescriptor : chartDescriptors) {
                            String[] values = line.split(separator);
                            for (ChartXYSeriesDescriptor chartXYSeriesDescriptor : chartDescriptor
                                .getXYSeriesDescriptors()) {
                                BaseXYDataItemRowMapper xyDataItemRowMapper =
                                    chartXYSeriesDescriptor.getXYDataItemRowMapper();
                                XYDataItem xyDataItem = xyDataItemRowMapper.mapRow(values);
                                chartXYSeriesDescriptor.getJFreeChartXYSeries().add(xyDataItem);
                            }
                        }
                    } catch (Exception e) {
                        ErrorUtils
                            .logExceptionFmt(
                                LOGGER,
                                e,
                                "CsvFileDatasetProvider.readCsvData():: cannot read CSV data from file {0}: {1}",
                                file);
                    }
                }
                for (ChartDescriptor chartDescriptor : chartDescriptors) {
                    XYSeriesCollection dataset = new XYSeriesCollection();
                    for (ChartXYSeriesDescriptor chartXYSeriesDescriptor : chartDescriptor
                        .getXYSeriesDescriptors()) {
                        dataset.addSeries(chartXYSeriesDescriptor.getJFreeChartXYSeries());
                    }
                    xyDatasetHolders.add(new XYDatasetHolder(dataset, file, chartDescriptor));
                }
            }
            return xyDatasetHolders;
        } finally {
            LOGGER.info("CsvFileDatasetProvider.loadDatasets():: exit");
        }
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

}
