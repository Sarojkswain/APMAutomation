package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.builder.TasBuilder;
import org.apache.http.util.Args;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author rsssa02
 */
public class MQExplorerFlowContext implements IFlowContext {

    private ArrayList<String> installCommandArgs = new ArrayList<>();
    private HashMap<String, String> queueMap = new HashMap<>();
    public HashMap<String, Integer> portMap;
    private final String installerFileName;
    private URL installPackageUrl;
    private String serverStartCommand;
    private String serverStopCommand;
    private final String installPath;
    private final String installLogPath;
    private final String unPackDir;

    private boolean createQueue;

    public HashMap<String, String> getQueueMap() {
        return queueMap;
    }

    public HashMap<String, Integer> getPortMap() {
        return portMap;
    }


    public String getMqServiceName() {
        return mqServiceName;
    }

    private String mqServiceName;

    public ArrayList<String> getInstallCommandArgs() {
        return installCommandArgs;
    }

    public boolean isCreateQueue() {
        return createQueue;
    }

    public MQExplorerFlowContext(Builder builder){
        this.installCommandArgs = builder.installCommandArgs;
        this.installerFileName = builder.installerFileName;
        this.installPackageUrl = builder.installPackageUrl;
        this.serverStartCommand = builder.serverStartCommand;
        this.serverStopCommand = builder.serverStopCommand;
        this.mqServiceName = builder.mqServiceName;
        this.installLogPath = builder.installLogPath;
        this.queueMap = builder.queueMap;
        this.createQueue = builder.createQueue;
        this.installPath = builder.installPath;
        this.unPackDir = builder.unPackDir;
        this.portMap = builder.portMap;
    }

    public URL getInstallPackageUrl(){
        return this.installPackageUrl;
    }

    public String getUnPackDir(){
        return this.unPackDir;
    }

    public String getInstallerFileName(){
        return this.installerFileName;
    }

    public String getServerStartCommand() {
        return serverStartCommand;
    }

    public String getServerStopCommand() {
        return serverStopCommand;
    }

    public String getInstallPath() {
        return installPath;
    }

    public String getInstallLogPath() {
        return installLogPath;
    }

    public static class Builder extends ExtendedBuilderBase<MQExplorerFlowContext.Builder, MQExplorerFlowContext> {

        public static final Charset DEFAULT_ENCODING;
        public final ArrayList<String> installCommandArgs = new ArrayList<>();
        public HashMap<String, String> queueMap = new HashMap<>();
        public HashMap<String, Integer> portMap;

        public final String mqInstalName = "Installation2";
        public String installerFileName;
        public URL installPackageUrl;
        public String serverStartCommand;
        public String serverStopCommand;
        public String installPath;
        public String installLogPath;
        public String unPackDir;
        public boolean createQueue = true;
        public String mqServiceName;

        static {
            DEFAULT_ENCODING = StandardCharsets.UTF_8;
        }

        public MQExplorerFlowContext build() {
            initInstallCmdOptions();
            initVarForInstallation();
            MQExplorerFlowContext context = this.getInstance();
            Args.notNull(context.installerFileName, "installer file name");
            Args.notNull(context.unPackDir, "unpack dir location");
            Args.notNull(context.installLogPath, "install log path");
            Args.notNull(context.installCommandArgs, "response file map");
            Args.notNull(context.queueMap, "queue names to create");
            return context;
        }

        private void initVarForInstallation() {
            this.unPackDir = TasBuilder.WIN_SOFTWARE_LOC + "\\sourceInstall";
            this.installLogPath = this.unPackDir + "\\installogs\\logs.txt";
        }

        private void initInstallCmdOptions() {
            //service name
            this.mqServiceName = "MQ_" + this.mqInstalName;

            this.installCommandArgs.add("ADDLOCAL=Server,XR_Service,AMS,MFT_Service,MFT_Logger,Explorer,JavaMsg,Toolkit" );
            this.installCommandArgs.add("PGMFOLDER=" + this.installPath);
            this.installCommandArgs.add("DATFOLDER=" + this.installPath + "\\data");
            this.installCommandArgs.add("LOGFOLDER=" + this.installPath + "\\log");
            this.installCommandArgs.add("GSKFOLDER=" + this.installPath);
            this.installCommandArgs.add("AGREETOLICENSE=yes");
            this.installCommandArgs.add("LAUNCHWIZ=0");
            this.installCommandArgs.add("KEEPQMDATA=keep");
            this.installCommandArgs.add("KEEPWEBDATA=keep");
            this.installCommandArgs.add("INSTALLATIONNAME=" + this.mqInstalName);
        }

        protected MQExplorerFlowContext getInstance() {
            return new MQExplorerFlowContext(this);
        }

        public MQExplorerFlowContext.Builder installerFileName(String installerFileName){
            this.installerFileName = installerFileName;
            return this.builder();
        }
        public MQExplorerFlowContext.Builder installPath(String installPath){
            this.installPath = installPath;
            return this.builder();
        }
        public MQExplorerFlowContext.Builder queueMap(HashMap<String, String> queueMap){
            this.queueMap = queueMap;
            return this.builder();
        }

        public MQExplorerFlowContext.Builder portMap(HashMap<String, Integer> portMap){
            this.portMap = portMap;
            return this.builder();
        }

        public MQExplorerFlowContext.Builder createQueue(boolean createQueue){
            this.createQueue = createQueue;
            return this.builder();
        }
        public MQExplorerFlowContext.Builder installLogPath(String installLogPath){
            this.installLogPath = installLogPath;
            return this.builder();
        }
        public MQExplorerFlowContext.Builder installPackageUrl(URL installPackageUrl) {
            this.installPackageUrl = installPackageUrl;
            return this.builder();
        }
        public MQExplorerFlowContext.Builder unPackDir(String unPackDir){
            this.unPackDir = unPackDir;
            return this.builder();
        }

        protected MQExplorerFlowContext.Builder builder() {
            return this;
        }

    }
}