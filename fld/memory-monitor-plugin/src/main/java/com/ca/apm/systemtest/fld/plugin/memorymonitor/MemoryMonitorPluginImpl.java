package com.ca.apm.systemtest.fld.plugin.memorymonitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.ScrollPaneConstants;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPluginConfiguration;
import com.tagtraum.perf.gcviewer.GCPreferences;
import com.tagtraum.perf.gcviewer.ModelChartImpl;
import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Plugin to upload memory monitoring information.
 * 
 * @author jirji01
 */
public class MemoryMonitorPluginImpl extends AbstractPluginImpl implements MemoryMonitorPlugin {
    private static final Logger log = LoggerFactory.getLogger(MemoryMonitorPluginImpl.class);

    @Override
    @ExposeMethod(description = "Process GC log files and create chart images")
    public void createChart(String group, String roleName, String orchestratorHost) {
        String gcLogFile =
            configurationManager.loadPluginConfiguration(EmPlugin.PLUGIN,
                EmPluginConfiguration.class).getCurrentGcLogFile();

        if (gcLogFile != null) {
            File gcSummaryFile = new File(roleName + "_summary.txt");
            ByteArrayOutputStream out = readGcLog(gcLogFile, gcSummaryFile);

            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                uploadFile(new ByteArrayInputStream(out.toByteArray()), gcSummaryFile, group,
                    roleName, orchestratorHost);
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Exception during uploading GC chart. Exception: {0}");
            }
        } else {
            log.error("GClog.txt file name is not set in EM configuration.");
        }
    }

    public void createChartFile(String gcLogName) {
        if (gcLogName != null) {
            File gcSummaryFile = new File(gcLogName + "_summary.txt");
            ByteArrayOutputStream out = readGcLog(gcLogName + ".txt", gcSummaryFile);

            Path file = Paths.get(gcLogName + ".png");
            try (OutputStream fileOut = Files.newOutputStream(file)) {
                fileOut.write(out.toByteArray());
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Exception during saving GC chart. Exception: {0}");
            }
        } else {
            log.error("gcLogFile parameter cannot be null");
        }
    }

    private ByteArrayOutputStream readGcLog(String gcLogFile, File gcSummaryFile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataWriter summaryWriter =
            DataWriterFactory.getDataWriter(gcSummaryFile, DataWriterType.SUMMARY)) {

            DataReaderFacade dataReaderFacade = new DataReaderFacade();
            final GCModel model = dataReaderFacade.loadModel(gcLogFile, false, null);
            summaryWriter.write(model);
            render(model, out);
        } catch (IOException | DataReaderException e) {
            ErrorUtils.logExceptionFmt(log, e,
                "Exception during processing GC logfiles. Exception: {0}");
        }
        return out;
    }

    private void uploadFile(InputStream input, File gcSummaryFile, String group, String roleName,
        String orchestratorHost) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile =
                new HttpPost(new URI("http://" + orchestratorHost
                    + ":8080/LoadOrchestrator/api/memorymonitor/" + group + "/" + roleName));

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("imageFile", input, ContentType.APPLICATION_OCTET_STREAM,
                "chart.png");
            builder.addTextBody("description",
                new String(Files.readAllBytes(gcSummaryFile.toPath())));
            HttpEntity multipart = builder.build();

            uploadFile.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                log.info(response.getStatusLine().getReasonPhrase());
            }
        }

    }

    private void render(GCModel model, OutputStream output) throws IOException {
        GCPreferences gcPreferences = new GCPreferences();
        gcPreferences.load();

        final ModelChartImpl pane = new ModelChartImpl();
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pane.setModel(model, gcPreferences);
        pane.setFootprint(model.getFootprint());
        pane.setMaxPause(model.getPause().getMax());
        pane.setRunningTime(model.getRunningTime());

        Dimension dim = new Dimension(1622, 968);
        pane.setSize(dim);
        pane.addNotify();
        pane.validate();

        pane.autoSetScaleFactor();

        final BufferedImage image =
            new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

        pane.paint(graphics);

        ImageIO.write(image, "png", output);
    }

    /**
     * Testing main class.
     * 
     * @param args
     */
    public static void main(String[] args) {
        MemoryMonitorPluginImpl mm = new MemoryMonitorPluginImpl();
        if (args.length > 0) {
            for (String arg : args) {
                mm.createChartFile(arg);
            }
        } else {
            System.err.println("Parameters: GC log files");
        }
        
    }

}
