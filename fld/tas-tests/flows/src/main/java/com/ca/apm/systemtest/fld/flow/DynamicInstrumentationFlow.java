package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jErrorOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jInfoOutputStream;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.util.ZipBuilder;

/**
 * @author KEYJA01
 *
 */
@Flow
public class DynamicInstrumentationFlow extends FlowBase {

    private static final Logger log = LoggerFactory.getLogger(DynamicInstrumentationFlow.class);

    private static final String CLW_COMMAND_INSTRUMENT_SERVLETS =
        "add labeled instrumentation with resource name HWS{0} and label name HWLabel of type DynamicBlamePointTracer in group HWGroup for agents matching \"{1}.*\" "
            + "for method doGet(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V in class com.wily.test.HelloWorldServlet{0}";
    private static final String CLW_COMMAND_REMOVE_INSTRUMENTATION =
        "remove instrumentation with resource name HWS{0} of type DynamicBlamePointTracer in group HWGroup for agents matching \"{1}.*\" "
            + "for method doGet(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V in class com.wily.test.HelloWorldServlet{0}";

    private static final String BACKUP_DIR = "backup";
    private static final String BACKUP_FILE_NAME_PATTERN = "DynamicInstrumentationData_{0}.zip";
    private static final String BACKUP_FILE_NAME_DATE_PATTERN = "yyyyMMddHHmmss";
    private static final Pattern BACKUPED_FILE_NAME_PATTERN = Pattern
        .compile("dynamicinstrumentationdata-(\\d{14})\\.xml");
    private static BackupFileFilter backupFileFilter = new BackupFileFilter();

    private static int count = 1;
    private static final int NUM_THREADS = 5;

    @FlowContext
    private DynamicInstrumentationFlowContext ctx;
    
    private Queue<String> cmdQueue = new LinkedList<>();


    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        final OutputStream debugStream = new Slf4jInfoOutputStream(log);
        final OutputStream errorStream = new Slf4jErrorOutputStream(log);

        // backup DynamicInstrumentationData-yyyyMMddHHmmss.xml files
        try {
            backupObsoleteFiles();
        } catch (Exception e) {
            log.warn("unable to backup DynamicInstrumentationData xml files: ", e);
        }

        // get the list of URLs for the servlets to be instrumented
        List<String> list = createUrls(ctx.agentHost, ctx.agentPort, ctx.servlets);

        // hit each servlet once
        pageHitter(list);

        log.debug("Waiting 30 sec before instrumenting");
        synchronized (this) {
            this.wait(30000L);
        }

        for (int i = 1; i <= ctx.servlets; i++) {
            String instrumentCmd =
                MessageFormat.format(CLW_COMMAND_INSTRUMENT_SERVLETS, i, ctx.agentName);
            cmdQueue.add(instrumentCmd);
        }
        
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread th = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    boolean done = false;
                    while (!done) {
                        String cmd = null;
                        synchronized (cmdQueue) {
                            if (cmdQueue.size() == 0) {
                                return;
                            }
                            cmd = cmdQueue.poll();
                        }
                        try {
                            executeCommand(cmd, debugStream, errorStream);
                            Thread.sleep(5000L);
                        } catch (Exception e) {
                            log.warn("Exception while starting Dynamic Instrumentation", e.getMessage());
                        }
                    }
                }
            });
            th.setDaemon(true);
            th.start();
        }
        
        //TODO wait for cmdQueue to empty
        boolean done = false;
        while (!done) {
            synchronized (cmdQueue) {
                done = cmdQueue.isEmpty();
                if (!done) {
                    try {
                        cmdQueue.wait(5000L);
                    } catch (Exception e) {
                        // don't care
                    }
                }
            }
        }

        // hit the servlets again after instrumenting
        pageHitter(list);
        log.debug("Waiting 30 sec before uninstrumenting");
        synchronized (this) {
            this.wait(30000L);
        }

        for (int i = 1; i <= ctx.servlets; i++) {
            String instrumentCmd = MessageFormat.format(CLW_COMMAND_REMOVE_INSTRUMENTATION, i, ctx.agentName);
            cmdQueue.add(instrumentCmd);
        }
        
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread th = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    boolean done = false;
                    while (!done) {
                        String cmd = null;
                        synchronized (cmdQueue) {
                            if (cmdQueue.size() == 0) {
                                return;
                            }
                            cmd = cmdQueue.poll();
                        }
                        try {
                            executeCommand(cmd, debugStream, errorStream);
                            Thread.sleep(5000L);
                        } catch (Exception e) {
                            log.warn("Exception while starting Dynamic Instrumentation", e.getMessage());
                        }
                    }
                }
            });
            th.setDaemon(true);
            th.start();
        }

        // backup DynamicInstrumentationData-yyyyMMddHHmmss.xml files
        try {
            backupObsoleteFiles();
        } catch (Exception e) {
            log.warn("unable to backup DynamicInstrumentationData xml files: ", e);
        }
    }

    private void pageHitter(List<String> urls) {
        for (String url : urls) {
            try {
                log.debug("About to ping URL " + url);
                (new URL(url)).openStream().close();
            } catch (Exception e) {
                log.warn("Unable to ping URL: " + url, e);
            }
        }
    }

    private List<String> createUrls(String host, int port, int count) {
        List<String> urls = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            String url = MessageFormat.format(ctx.urlFormat, host, port, i);
            urls.add(url);
        }
        return urls;
    }

    private void executeCommand(String instrumentCmd, OutputStream debugStream,
        OutputStream errorStream) throws Exception {
        File batchFile = generateCommandLine(instrumentCmd);
        ProcessExecutor executor = new ProcessExecutor(batchFile.getCanonicalPath());
        if (ctx.diHome != null) {
            executor.directory(new File(ctx.diHome));
        }
        executor.redirectOutput(debugStream).redirectError(errorStream);
        executor.redirectOutputAlsoTo(new FileOutputStream("c:\\windows\\temp\\clw-" + count++
            + ".log"));
        log.debug("About to execute the batch file {}", batchFile.getCanonicalPath());
        ProcessResult result = executor.execute();
        log.debug("Executed");
        if (result.getExitValue() != 0) {
            log.warn("Dynamic instrumentation process exited with code " + result.getExitValue()
                + " for command \"" + instrumentCmd + "\"");
        }
    }

    private File generateCommandLine(String instrumentCmd) throws Exception {
        File batchFile = File.createTempFile("runclw-", ".bat");
        log.debug("Writing batch file " + batchFile.getCanonicalPath());
        batchFile.deleteOnExit();
        PrintWriter out = new PrintWriter(batchFile);

        out.printf("%s -Dhost=%s -Dintroscope.showAllDynamicInstrumentationAPI=true -jar %s %s\n",
            ctx.javaHome, ctx.emHost, ctx.clwJar, instrumentCmd);

        out.flush();
        out.close();

        log.debug("Batch file written");
        return batchFile;

    }

    // backup DynamicInstrumentationData-yyyyMMddHHmmss.xml files
    private void backupObsoleteFiles() throws IOException {
        File[] files = listFiles(ctx.diHome, backupFileFilter);
        if (files != null && files.length > 0) {
            // check backup dir
            File backupDir = Paths.get(ctx.diHome, BACKUP_DIR).toFile();
            checkDir(backupDir);

            // backup file name
            String date = (new SimpleDateFormat(BACKUP_FILE_NAME_DATE_PATTERN)).format(new Date());
            String backupFileName = MessageFormat.format(BACKUP_FILE_NAME_PATTERN, date);
            File backupFile = new File(backupDir, backupFileName);
            log.info("backup file: {}", backupFile);

            // create zip
            try (ZipBuilder zipBuilder = new ZipBuilder(backupFile);) {
                for (File file : files) {
                    zipBuilder.addFile(file.getName(), Files.readAllBytes(file.toPath()));
                    log.info("  -> adding file to zip: {}", file.getName());
                }
                zipBuilder.flush();
                zipBuilder.close();
            }

            // delete old files
            for (File file : files) {
                try {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info("file deleted: {}", file);
                    } else {
                        log.warn("unable to delete file {}", file);
                    }
                } catch (Exception e) {
                    log.warn("unable to delete file " + file + ": " + e, e);
                }
            }
            log.info("backup file finished: {}", backupFile);
        }
    }

    private static void checkDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            log.info("creating directory: {}", dir);
            dir.mkdirs();
        }
    }

    private static File[] listFiles(String dirName, FileFilter fileFilter) {
        File directory = new File(dirName);
        File[] files =
            directory.listFiles(fileFilter == null
                ? ((FileFilter) FileFileFilter.FILE)
                : fileFilter);
        log.debug("files found: {}", files == null ? 0 : files.length);
        return files;
    }

    private static final class BackupFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isFile()
                && BACKUPED_FILE_NAME_PATTERN.matcher(file.getName().toLowerCase()).matches();
        }
    }

}
