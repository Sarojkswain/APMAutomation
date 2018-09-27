package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import static com.ca.apm.systemtest.fld.util.networktrafficmonitor.Util.checkDir;
import static com.ca.apm.systemtest.fld.util.networktrafficmonitor.Util.checkFile;
import static com.ca.apm.systemtest.fld.util.networktrafficmonitor.Util.listFiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.util.networktrafficmonitor.ChartGenerationService;
import com.ca.apm.systemtest.fld.util.networktrafficmonitor.Configuration;

public class ChartGenerationServiceJFreeChartImpl implements ChartGenerationService {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ChartGenerationServiceJFreeChartImpl.class);

    private Pattern inputDataFilePattern;
    private String chartImageFileNamePrefixPattern;
    private String chartImageFileNameSuffix;
    private String chartTitlePattern;

    private Collection<ChartDescriptor> chartDescriptors;

    private Configuration configuration;

    private CsvFileDatasetProvider datasetProvider;

    private RegexFileFilter fileFilter = new RegexFileFilter();

    @Override
    public void generateCharts() {
        LOGGER.info("ChartGenerationServiceJFreeChartImpl.generateCharts():: entry");
        int inputDataFilePrefixLength = configuration.getInputDataFilePrefix().length();
        File chartsDir = configuration.getChartsDir();
        String networkTrafficMonitorWebappHost = configuration.getNetworkTrafficMonitorWebappHost();
        try {
            File[] files = getSourceFiles();
            if (files == null || files.length == 0) {
                LOGGER
                    .info("ChartGenerationServiceJFreeChartImpl.createCharts():: found no files to process");
                return;
            }

            for (File file : files) {
                LOGGER.info(
                    "ChartGenerationServiceJFreeChartImpl.createCharts():: processing file {}",
                    file);
                String remoteHost = getRemoteHost(file, inputDataFilePrefixLength);
                try {
                    Collection<XYDatasetHolder> xyDatasetHolders =
                        datasetProvider.loadDatasets(file, chartDescriptors);
                    for (XYDatasetHolder xyDatasetHolder : xyDatasetHolders) {
                        try {
                            ChartDescriptor chartDescriptor = xyDatasetHolder.getChartDescriptor();

                            JFreeChart chart =
                                ChartFactory.createXYLineChart(
                                    getChartTitle(chartDescriptor.getTitle(), remoteHost),
                                    chartDescriptor.getXAxisLabel(),
                                    chartDescriptor.getYAxisLabel(), xyDatasetHolder.getDataset());
                            chart.setBorderVisible(true);

                            String chartId = chartDescriptor.getId();

                            checkDir(chartsDir);
                            File chartImageFile = getChartFile(chartsDir, remoteHost, chartId);

                            ChartUtilities.saveChartAsPNG(chartImageFile, chart,
                                chartDescriptor.getChartWidth(), chartDescriptor.getChartHeight());

                            LOGGER
                                .info(
                                    "ChartGenerationServiceJFreeChartImpl.createCharts():: chart saved as PNG image {}",
                                    chartImageFile);

                            BufferedImage image = ImageIO.read(chartImageFile);
                            File summaryFile = createSummaryFile(remoteHost, chartDescriptor);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", out);
                            uploadFile(new ByteArrayInputStream(out.toByteArray()), summaryFile,
                                remoteHost, chartId);
                            LOGGER
                                .info(
                                    "ChartGenerationServiceJFreeChartImpl.createCharts():: chart sent to {}: {}",
                                    networkTrafficMonitorWebappHost, chartImageFile);
                        } catch (Exception e) {
                            ErrorUtils
                                .logExceptionFmt(
                                    LOGGER,
                                    e,
                                    "ChartGenerationServiceJFreeChartImpl.createCharts():: cannot create chart from data file {0}: {1}",
                                    file);
                        }
                    }
                } catch (Exception e) {
                    ErrorUtils
                        .logExceptionFmt(
                            LOGGER,
                            e,
                            "ChartGenerationServiceJFreeChartImpl.createCharts():: cannot create chart from data file {0}: {1}",
                            file);
                }

            }
        } finally {
            LOGGER.info("ChartGenerationServiceJFreeChartImpl.generateCharts():: exit");
        }
    }

    private File[] getSourceFiles() {
        File dataDir = configuration.getDataDir();
        File[] files = null;
        try {
            files = listFiles(dataDir, fileFilter);
            LOGGER.info(
                "ChartGenerationServiceJFreeChartImpl.getSourceFiles():: number of files = {}",
                files.length);
            LOGGER.debug("ChartGenerationServiceJFreeChartImpl.getSourceFiles():: files = {}",
                Arrays.toString(files));
            return files;
        } catch (Exception e) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(
                    LOGGER,
                    e,
                    "ChartGenerationServiceJFreeChartImpl.getSourceFiles():: cannot list files in {0}: {1}",
                    dataDir);
        }
    }

    // TODO - params vs. configuration
    private File getChartFile(File chartsDir, String remoteHost, String chartId) {
        File chartImageFile =
            Paths.get(
                chartsDir.getAbsolutePath(),
                getChartImageFilePrefix(remoteHost) + "_" + chartId + "_chart"
                    + chartImageFileNameSuffix).toFile();
        LOGGER.info("ChartGenerationServiceJFreeChartImpl.getChartFile():: chartImageFile = {}",
            chartImageFile);
        return chartImageFile;
    }

    // TODO - params vs. configuration
    private File getSummaryFile(File chartsDir, String remoteHost, String chartId) {
        File summaryFile =
            new File(configuration.getSummaryDir(), (new StringBuilder(
                getChartImageFilePrefix(remoteHost)).append("_").append(chartId).append("_summary")
                .append(".txt").toString()));
        LOGGER.info("ChartGenerationServiceJFreeChartImpl.getSummaryFile():: summaryFile = {}",
            summaryFile);
        return summaryFile;
    }

    private void uploadFile(InputStream input, File summaryFile, String remoteHost, String chartId)
        throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = getUploadImageRequest(remoteHost, chartId);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("imageFile", input, ContentType.APPLICATION_OCTET_STREAM,
                summaryFile.getName());
            builder.addTextBody("description",
                summaryFile == null ? "" : new String(Files.readAllBytes(summaryFile.toPath())));
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                LOGGER.info(
                    "ChartGenerationServiceJFreeChartImpl.uploadFile():: response status: {}",
                    response.getStatusLine().getReasonPhrase());
            }
        }
    }

    // TODO - params vs. configuration
    private HttpPost getUploadImageRequest(String remoteHost, String chartId)
        throws URISyntaxException {
        String url =
            (new StringBuilder("http://"))
                .append(configuration.getNetworkTrafficMonitorWebappHost()).append(':')
                .append(configuration.getNetworkTrafficMonitorWebappPort()).append('/')
                .append(configuration.getNetworkTrafficMonitorWebappContextRoot())
                .append(configuration.getNetworkTrafficMonitorWebappUploadImageUrl())
                .append(configuration.getThisHostName()).append('/').append(remoteHost).append('/')
                .append(chartId).toString();
        LOGGER.info("ChartGenerationServiceJFreeChartImpl.getUploadImageRequest():: url = {}", url);
        return new HttpPost(new URI(url));
    }

    private File createSummaryFile(String remoteHost, ChartDescriptor chartDescriptor)
        throws IOException {
        String chartId = chartDescriptor.getId();
        File summaryFile = getSummaryFile(configuration.getSummaryDir(), remoteHost, chartId);

        StringBuilder summary = new StringBuilder();
        summary.append("host=");
        // TODO
        summary.append(configuration.getThisHost());
        summary.append('\n');
        summary.append("remoteHost=");
        summary.append(remoteHost);
        summary.append('\n');
        summary.append("chartId=");
        summary.append(chartId);
        summary.append('\n');
        summary.append("updateTime=");
        summary.append(new Date());
        summary.append('\n');

        checkDir(configuration.getSummaryDir());
        Files.write(summaryFile.toPath(), summary.toString().getBytes());
        LOGGER.info("ChartGenerationServiceJFreeChartImpl.getSummaryFile():: summaryFile = {}",
            summaryFile);
        return summaryFile;
    }

    private String getRemoteHost(File file, int inputDataFilePrefixLength) {
        String fileName = file.getName();
        return fileName.substring(inputDataFilePrefixLength,
            fileName.lastIndexOf(configuration.getInputDataFileSufix()));
    }

    private String getChartImageFilePrefix(String remoteHost) {
        return MessageFormat.format(chartImageFileNamePrefixPattern, remoteHost);
    }

    private String getChartTitle(String chartTitle, String remoteHost) {
        return MessageFormat.format(chartTitlePattern, chartTitle, configuration.getThisHost(),
            remoteHost);
    }

    public void setInputDataFilePattern(Pattern inputDataFilePattern) {
        this.inputDataFilePattern = inputDataFilePattern;
    }

    public void setChartImageFileNamePrefixPattern(String chartImageFileNamePrefixPattern) {
        this.chartImageFileNamePrefixPattern = chartImageFileNamePrefixPattern;
    }

    public void setChartImageFileNameSuffix(String chartImageFileNameSuffix) {
        this.chartImageFileNameSuffix = chartImageFileNameSuffix;
    }

    public void setChartTitlePattern(String chartTitlePattern) {
        this.chartTitlePattern = chartTitlePattern;
    }

    public void setChartDescriptors(Collection<ChartDescriptor> chartDescriptors) {
        this.chartDescriptors = chartDescriptors;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setDatasetProvider(CsvFileDatasetProvider datasetProvider) {
        this.datasetProvider = datasetProvider;
    }

    private final class RegexFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return checkFile(pathname)
                && (inputDataFilePattern.matcher(pathname.getName()).matches());
        }
    }

}
