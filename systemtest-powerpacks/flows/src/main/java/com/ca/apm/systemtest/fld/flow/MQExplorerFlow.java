package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution.Builder;

/**
 * to install MQ explorer and to create Queue Manager and Queues
 * @Author rsssa02
 */
@Flow
public class MQExplorerFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrclSrvcBusFlow.class);
    @FlowContext
    private MQExplorerFlowContext context;

    public void run() throws Exception {
        this.archiveFactory.createArchive(this.context.getInstallPackageUrl())
                .unpack(new File(this.context.getUnPackDir()));
        ArrayList<String> args = this.context.getInstallCommandArgs();
        constructCmdLineMQParams(args);
        try {
            this.runInstallationProcess(args);
            if(new File(this.context.getInstallLogPath()).exists()) {
                validateInstall();
                startMQServices();
                createQueueAndQManager();
            }else{
                throw new IllegalStateException("Was not able to validate the installation.. " +
                        "Failure during install process");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void startMQServices() throws Exception{
        String[] mqServiceEnabled = new String[]{"config", this.context.getMqServiceName(), "start=", "auto"};

        int retVal = (new Builder("sc", LOGGER).args(mqServiceEnabled).build().go());
        if(retVal != 0){
            throw new IllegalStateException("failure during enabling the service");
        }

        int retVal1 = (new Builder("strmqsvc", LOGGER).workDir(new File(this.context.getInstallPath() + "\\bin"))
                .build().go());
        if(retVal1 != 0){
            throw new IllegalStateException("Failure during starting the service");
        }
    }

    private void createQueueAndQManager() throws Exception{
        if(this.context.getQueueMap() != null && this.context.isCreateQueue()){
            Map<String, String> qmap = new HashMap<>(this.context.getQueueMap());
            Map<String, Integer> mqPortMap = new HashMap<>(this.context.getPortMap());
            int retVal = 1, retVal2 = 1, retVal3 = 1;
            //int mqPortTemp = 1414;

            if(!qmap.isEmpty()){
                for(Map.Entry<String, String> testMap : qmap.entrySet()){
                    LOGGER.info("Check Point: " + testMap.getKey() + " == " + testMap.getValue());
                    String[] createQmgr = new String[]{"-q", "-d", "MY.DEFAULT.XMIT.QUEUE", testMap.getKey()};

                    retVal = (new Builder("crtmqm", LOGGER).workDir(new File(this.context.getInstallPath() + "\\bin"))
                            .args(createQmgr).build().go());
                    retVal2 = (new Builder("strmqm", LOGGER).workDir(new File(this.context.getInstallPath() + "\\bin"))
                            .args(new String[]{testMap.getKey()}).build().go());

                    //clearing the queue names of the list
                    ArrayList<String> var4 = new ArrayList<>();
                    var4.clear();
                    for(String qNameSplit : testMap.getValue().split(":")) {
                        var4.add("DEFINE QLOCAL('" + qNameSplit + "')");
                    }
                    var4.add("define listener(TCP.LISTENER) trptype(tcp) control(qmgr) port(" + mqPortMap.get(testMap.getKey()) + ")");
                    var4.add("start listener(TCP.LISTENER)");
                    var4.add("ALTER QMGR CHLAUTH(DISABLED)");

                    if(!var4.isEmpty()) {
                        List<String> lines = Arrays.asList(var4.toArray(new String[0]));
                        Path file = Paths.get(this.context.getUnPackDir() + "\\queue_" + testMap.getKey() + ".txt");
                        if(Files.exists(file)) {
                            LOGGER.info("Deleting file if exists");
                            Files.deleteIfExists(file);
                        }
                        Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW);

                        retVal3 = (new Builder("runmqsc", LOGGER).workDir(new File(this.context.getInstallPath()
                                + "\\bin")).args(new String[]{testMap.getKey(), "<", file.toString()}).build().go());
                    } else {
                        throw new IllegalStateException("Queue names are empty and unable to create! ");
                    }
                    Thread.sleep(5000l);
                }
            }
            if(retVal != 0 && retVal2 != 0){
                throw new IllegalStateException("Creating queue Manager failed !");
            }
        }
    }

    private void constructCmdLineMQParams(ArrayList<String> args) {
        args.add("/q");
        args.add("/l*v");
        args.add(this.context.getInstallLogPath());
    }

    private void runInstallationProcess(ArrayList<String> args) throws Exception{
        String installExecutablePath = Paths.get(this.context.getUnPackDir(),
                this.context.getInstallerFileName()).toString();
        //TasFileWatchMonitor watchMonitor = this.monitorFactory.createWatchMonitor();

       // try {
            String pathToDir = new File(context.getInstallLogPath()).getAbsolutePath();
            Path logFilePath = Paths.get(context.getInstallLogPath());
            String logFileName = logFilePath.getFileName().toString();
            File logDir = new File(pathToDir.substring(0, pathToDir.lastIndexOf(File.separator)));

            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            if (args == null || args.isEmpty()) {
                throw new IllegalStateException(String.format("Launching silent installation failed (%d)",
                        new Object[]{"Failed to compile the needed arguments"}));
            }
            /*watchMonitor.watchFileChanged(logDir, ".*" + logFileName + ".*$").watchFileCreated(logDir,
                    ".*" + logFileName + ".*$").monitor();*/
            LOGGER.info("the filename is : " + installExecutablePath);
            int responseCode = (new Builder(installExecutablePath, LOGGER).args(args.toArray(new String[0]))
                    .build().go());
            Thread.sleep(120000l);
            if (responseCode != 0) {
                throw new IllegalStateException(String.format("Launching silent installation failed (%d)",
                        new Object[]{responseCode}));
            }
      /*  }
        finally {
            if (watchMonitor != null) {
                watchMonitor.close();
            }
        }*/
    }

    private void validateInstall() {
        if(!new File(context.getInstallPath() + "\\bin").exists()){
            throw new IllegalStateException("MQ Explorer installation was not successful, please re-install");
        }
    }
}
