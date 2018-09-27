package com.ca.apm.tests.flow;

import com.tagtraum.perf.gcviewer.GCPreferences;
import com.tagtraum.perf.gcviewer.ModelChartImpl;
import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoryMonitorRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitorRunner.class);

    private static final String MEMORY_MONITOR_RUNNER = "MemoryMonitorRunner";

    private static final String DEFAULT_MEMORY_MONITOR_WEBAPP_HOST;
    private static final int DEFAULT_MEMORY_MONITOR_WEBAPP_PORT = 8080;
    private static final String DEFAULT_MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT =
        "memory-monitor-webapp-99.99.aquarius-SNAPSHOT";

    private static final int DEFAULT_CHART_WIDTH = 1622;
    private static final int DEFAULT_CHART_HEIGHT = 968;

    private static final long DEFAULT_WAIT_INTERVAL = 900000L; // 15 min.

    private static final String DEFAULT_WORK_DIR = ".";
    private static final int DEFAULT_ITERATION_COUNT = -1; // => infinite loop mode

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS-Z";

    private static final String MEMORY_MONITOR_WEBAPP_UPLOAD_IMAGE_URL = "/api/memorymonitor/";

    private static ArgumentParser parser;

    private String gcLogFile;
    private String group;
    private String roleName;

    private String memoryMonitorWebappHost;
    private int memoryMonitorWebappPort = DEFAULT_MEMORY_MONITOR_WEBAPP_PORT;
    private String memoryMonitorWebappContextRoot = DEFAULT_MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT;

    private int chartWidth = DEFAULT_CHART_WIDTH;
    private int chartHeight = DEFAULT_CHART_HEIGHT;

    private static long waitInterval = DEFAULT_WAIT_INTERVAL;
    private static int iterationCount = DEFAULT_ITERATION_COUNT;

    private static String workDir = DEFAULT_WORK_DIR;

    static {
        String defaultMemoryMonitorWebappHost;
        try {
            defaultMemoryMonitorWebappHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            defaultMemoryMonitorWebappHost = "localhost";
        }
        DEFAULT_MEMORY_MONITOR_WEBAPP_HOST = defaultMemoryMonitorWebappHost;
    }

    public static void main(String[] args) {
        LOGGER.info("MemoryMonitorRunner.main():: entry");
        try {
            // parse arguments
            LOGGER.info("MemoryMonitorRunner.main():: parsing arguments");
            Namespace namespace = parseArgs(args);
            MemoryMonitorRunner mmRunner = init(namespace);
            int iteration = 1;
            System.out.println(MEMORY_MONITOR_RUNNER);

            // limited count of iterations mode
            if (iterationCount > 0) {
                LOGGER.info("MemoryMonitorRunner.main():: limited count of iterations mode: {}",
                    iterationCount);
                for (int i = 0; i < iterationCount; i++) {
                    LOGGER.info(
                        "MemoryMonitorRunner.main():: iteration {}: start GC chart harvesting",
                        iteration);
                    // generate heap graph
                    try {
                        mmRunner.createChart();
                    } catch (Exception e) {
                        LOGGER.error("MemoryMonitorRunner.createChart():: cannot create GC chart.", e);
                        throw e;
                    }

                    if ((iterationCount > 1) && (i < iterationCount - 1)) {
                        LOGGER
                            .info("MemoryMonitorRunner.main():: iteration {}: waiting", iteration);
                        sleep(waitInterval);
                    }
                    iteration++;
                }
            }

            // infinite loop mode
            else {
                LOGGER.info("MemoryMonitorRunner.main():: infinite loop mode");
                while (true) {
                    LOGGER.info(
                        "MemoryMonitorRunner.main():: iteration {}: start GC chart harvesting",
                        iteration);
                    // generate heap graph
                    try {
                        mmRunner.createChart();
                    } catch (Exception e) {
                        LOGGER.error("MemoryMonitorRunner.main():: cannot create GC chart.", e);
                        throw e;
                    }

                    LOGGER.info("MemoryMonitorRunner.main():: iteration {}: waiting", iteration);
                    sleep(waitInterval);
                    iteration++;
                }
            }
        } catch (Exception e) {
            LOGGER.error("MemoryMonitorRunner.main():: exception occurred", e);
            throw e;
        } finally {
            LOGGER.info("MemoryMonitorRunner.main():: exit");
        }
    }

    public void createChart() {
        try {
            LOGGER.info("in createChart");
            File gcSummaryFile = getGcSummaryFile();
            LOGGER.info("using file " + gcSummaryFile);
            ByteArrayOutputStream out = readGcLog(gcLogFile, gcSummaryFile);
            LOGGER.info("readGcLog OK");
            byte[] chart = out.toByteArray();
            uploadFile(new ByteArrayInputStream(chart), gcSummaryFile);
            saveFile(chart);
            LOGGER.info("Uploaded OK");
        } catch (Exception e) {
            LOGGER.error("MemoryMonitorRunner.createChart():: exception during creating GC chart,", e);
        }
        LOGGER.info("Exiting createChart");
    }

    private void saveFile(byte[] image) throws IOException {
        Path file = Paths.get(workDir, "gc.png");
        Files.write(file, image);
    }

    private ByteArrayOutputStream readGcLog(String gcLogFile, File gcSummaryFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataWriter summaryWriter =
            DataWriterFactory.getDataWriter(gcSummaryFile, DataWriterType.SUMMARY)) {
            DataReaderFacade dataReaderFacade = new DataReaderFacade();
            GCModel model = dataReaderFacade.loadModel(gcLogFile, false, null);
            summaryWriter.write(model);
            render(model, out);
        } catch (IOException | DataReaderException e) {
            LOGGER.error("MemoryMonitorRunner.readGcLog():: exception during processing GC logfiles.", e);
        }
        return out;
    }

    private void uploadFile(InputStream input, File gcSummaryFile) throws IOException,
        URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = getUploadImageRequest();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("imageFile", input, ContentType.APPLICATION_OCTET_STREAM, "chart.png");
            builder.addTextBody("description", new String(Files.readAllBytes(gcSummaryFile.toPath())));
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                LOGGER.info("MemoryMonitorRunner.uploadFile():: response status: {}", response
                    .getStatusLine().getReasonPhrase());
            }
        }
    }

    private void render(GCModel model, OutputStream output) throws IOException {
        GCPreferences gcPreferences = new GCPreferences();
        gcPreferences.load();
        Dimension dim = new Dimension(chartWidth, chartHeight);
        ModelChartImpl pane = new ModelChartImpl();
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setModel(model, gcPreferences);
        pane.setFootprint(model.getFootprint());
        pane.setMaxPause(model.getPause().getMax());
        pane.setRunningTime(model.getRunningTime());
        pane.setSize(dim);
        pane.addNotify();
        pane.validate();
        pane.autoSetScaleFactor();
        BufferedImage image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
        pane.paint(graphics);
        ImageIO.write(image, "png", output);
    }

    private File getGcSummaryFile() {
        File gcSummaryFile =
            new File(workDir, (new StringBuilder(roleName)).append("_summary_")
                .append((new SimpleDateFormat(DATE_FORMAT)).format(new Date())).append(".txt")
                .toString());
        LOGGER.info("MemoryMonitorRunner.getGcSummaryFile():: gcSummaryFile = {}", gcSummaryFile);
        return gcSummaryFile;
    }

    private HttpPost getUploadImageRequest() throws URISyntaxException {
        String url =
            (new StringBuilder("http://")).append(memoryMonitorWebappHost).append(':')
                .append(memoryMonitorWebappPort).append('/').append(memoryMonitorWebappContextRoot)
                .append(MEMORY_MONITOR_WEBAPP_UPLOAD_IMAGE_URL).append(group).append('/')
                .append(roleName).toString();
        LOGGER.info("MemoryMonitorRunner.getUploadImageRequest():: url = {}", url);
        return new HttpPost(new URI(url));
    }

    private static Namespace parseArgs(String[] args) {
        parser =
            ArgumentParsers
                .newArgumentParser(MemoryMonitorRunner.class.getName())
                .description(
                    "MemoryMonitorRunner generates a graph as an image from GC log and then sends it to the MemoryMonitor webapp");

        parser.addArgument("-f", "-gc", "-gcLogFile").dest("gcLogFile").type(String.class)
            .action(Arguments.store()).help("GC log file").required(true);

        parser.addArgument("-g", "-group").dest("group").type(String.class)
            .action(Arguments.store()).help("Group").required(true);

        parser.addArgument("-r", "-role", "-roleName").dest("roleName").type(String.class)
            .action(Arguments.store()).help("Role name").required(true);

        parser.addArgument("-m", "-host", "-memoryMonitorWebappHost")
            .dest("memoryMonitorWebappHost").type(String.class).action(Arguments.store())
            .help("MemoryMonitor webapp server host")
            .setDefault(DEFAULT_MEMORY_MONITOR_WEBAPP_HOST);

        parser.addArgument("-p", "-port", "-memoryMonitorWebappPort")
            .dest("memoryMonitorWebappPort").type(Integer.class).action(Arguments.store())
            .help("MemoryMonitor webapp server port")
            .setDefault(DEFAULT_MEMORY_MONITOR_WEBAPP_PORT);

        parser.addArgument("-c", "-contextRoot", "-memoryMonitorWebappContextRoot")
            .dest("memoryMonitorWebappContextRoot").type(String.class).action(Arguments.store())
            .help("MemoryMonitor webapp context root")
            .setDefault(DEFAULT_MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT);

        parser.addArgument("-x", "-chartWidth").dest("chartWidth").type(Integer.class)
            .action(Arguments.store()).help("Chart width").setDefault(DEFAULT_CHART_WIDTH);

        parser.addArgument("-y", "-chartHeight").dest("chartHeight").type(Integer.class)
            .action(Arguments.store()).help("Chart height").setDefault(DEFAULT_CHART_HEIGHT);

        parser.addArgument("-w", "-waitInterval").dest("waitInterval").type(Long.class)
            .action(Arguments.store()).help("Wait interval").setDefault(DEFAULT_WAIT_INTERVAL);

        parser.addArgument("-d", "-workDir").dest("workDir").type(String.class)
            .action(Arguments.store()).help("Working dir").setDefault(DEFAULT_WORK_DIR);

        parser.addArgument("-i", "-iterationCount").dest("iterationCount").type(Integer.class)
            .action(Arguments.store())
            .help("Count of iterations, non-positive value means infinite loop mode")
            .setDefault(DEFAULT_ITERATION_COUNT);

        Namespace namespace = parser.parseArgsOrFail(args);
        LOGGER.debug("MemoryMonitorRunner.parseArgs():: namespace = {}", namespace);
        return namespace;
    }

    private static MemoryMonitorRunner init(Namespace namespace) {
        String gcLogFile = namespace.getString("gcLogFile");
        String group = namespace.getString("group");
        String roleName = namespace.getString("roleName");
        String memoryMonitorWebappHost = namespace.getString("memoryMonitorWebappHost");
        int memoryMonitorWebappPort = namespace.getInt("memoryMonitorWebappPort");
        String memoryMonitorWebappContextRoot = namespace.getString("memoryMonitorWebappContextRoot");
        int chartWidth = namespace.getInt("chartWidth");
        int chartHeight = namespace.getInt("chartHeight");
        waitInterval = namespace.getLong("waitInterval");
        workDir = namespace.getString("workDir");
        iterationCount = namespace.getInt("iterationCount");

        Args.notBlank(gcLogFile, "gcLogFile");
        Args.notBlank(group, "group");
        Args.notBlank(roleName, "roleName");
        Args.notBlank(memoryMonitorWebappHost, "memoryMonitorWebappHost");
        Args.positive(memoryMonitorWebappPort, "memoryMonitorWebappPort");
        Args.notBlank(memoryMonitorWebappContextRoot, "memoryMonitorWebappContextRoot");
        Args.positive(chartWidth, "chartWidth");
        Args.positive(chartHeight, "chartHeight");
        Args.notNegative(waitInterval, "waitInterval");
        Args.notBlank(workDir, "workDir");
        
        return new MemoryMonitorRunner(gcLogFile, workDir, group, roleName, memoryMonitorWebappHost, memoryMonitorWebappPort,
            memoryMonitorWebappContextRoot, chartWidth, chartHeight);
    }

    public MemoryMonitorRunner(String gcLogFile, String workDir, String group, String roleName, String memoryMonitorWebappHost,
                               Integer memoryMonitorWebappPort, String memoryMonitorWebappContextRoot, Integer chartWidth, Integer chartHeight) {
        this.gcLogFile = gcLogFile;
        try {
            Files.createDirectories(Paths.get(workDir));
            this.workDir = workDir;
        } catch (IOException e) {
            LOGGER.error("Cannot crate workdir. Using current folder: '" + DEFAULT_WORK_DIR + "'", e);
            this.workDir = DEFAULT_WORK_DIR;
        }
        this.group = group;
        this.roleName = roleName;
        this.memoryMonitorWebappHost = memoryMonitorWebappHost;
        this.memoryMonitorWebappPort = 
            memoryMonitorWebappPort == null ? DEFAULT_MEMORY_MONITOR_WEBAPP_PORT : memoryMonitorWebappPort;
        this.memoryMonitorWebappContextRoot = memoryMonitorWebappContextRoot;
        this.chartHeight = chartHeight == null ? DEFAULT_CHART_HEIGHT : chartHeight;
        this.chartWidth = chartWidth == null ? DEFAULT_CHART_WIDTH : chartWidth;
    }

    private static void sleep(long sleepTime) {
        try {
            LOGGER.debug("MemoryMonitorRunner.sleep():: sleeping for {} [s]", (sleepTime / 1000));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("MemoryMonitorRunner.sleep():: InterruptedException");
        }
    }

}
