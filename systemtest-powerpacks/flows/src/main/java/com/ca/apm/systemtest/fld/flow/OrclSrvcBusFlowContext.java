package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for OSB installation, silent installers may need the following parameters to work properly
 * -slient -response .rsp -jreLoc jdkhome -waitforcompletion
 *
 * @Author rsssa02.
 */
public class OrclSrvcBusFlowContext implements IFlowContext{

    private final Map<String, String> responseFileOptions;
    private final String installLocation;
    private final String responseFileName = "custom_installtype.rsp";
    private final String agentJarPath;
    private final String profileFilePath;
    private String unpackDirName ;
    private final String installerFileName;
    private final URL installPackageUrl;
    private final String serverStartCommand;
    private final String serverStopCommand;
    private final String domainDirRelativePath;
    private boolean osbSunJvmVendor = true;

    private final String setupInstallerName;
    private final String encoding;
    private final String wlsServerHome;

    public String getJreHomeLocation() {
        return this.jreHomeLocation;
    }

    private final String jreHomeLocation;

    protected OrclSrvcBusFlowContext(Builder builder, String wlsServerHome) {
        this.wlsServerHome = wlsServerHome;
        this.responseFileOptions = builder.responseFileOptions;
        this.serverStartCommand = builder.serverStartCommand;
        this.serverStopCommand = builder.serverStopCommand;
        this.installLocation = builder.installPath;
        this.unpackDirName = builder.unpackDirName;
        this.setupInstallerName = builder.setupInstallerName;
        this.agentJarPath = builder.agentPath;
        this.profileFilePath = builder.agentProfilePath;
        this.installerFileName = builder.installerFileName;
        this.installPackageUrl = builder.installPackageUrl;
        this.jreHomeLocation = builder.jreHomeLocation;
        this.domainDirRelativePath = builder.domainDirRelativePath;
        this.osbSunJvmVendor = builder.osbSunJvmVendor;
        this.encoding = builder.getEncoding();

    }

    public String getDomainDirRelativePath() {
        return domainDirRelativePath;
    }
    public String getAgentJarPath() {
        return agentJarPath;
    }

    public String getProfileFilePath() {
        return profileFilePath;
    }
    public URL getInstallPackageUrl() {
        return this.installPackageUrl;
    }

    public Map<String, String> getResponseFileOptions() {
        return this.responseFileOptions;
    }

    public String getResponseFileName() {
        return this.responseFileName;
    }

    public String getSetupInstallerName() {
        return setupInstallerName;
    }
    @TasEnvironmentProperty("startCommand")
    public String getServerStartCommand() {
        return this.serverStartCommand;
    }

    @TasEnvironmentProperty("stopCommand")
    public String getServerStopCommand() {
        return this.serverStopCommand;
    }

    public String getUnpackDirName() {
        return unpackDirName;
    }

    public String getInstallLocation(){
        return this.installLocation;
    }

    public boolean getOsbSunJvmVendor(){
        return this.osbSunJvmVendor;
    }


    public static class Builder extends ExtendedBuilderBase<OrclSrvcBusFlowContext.Builder, OrclSrvcBusFlowContext> {
        private static final Charset DEFAULT_ENCODING;
        private final Map<String, String> responseFileOptions = new HashMap<>();
        protected String installPath;
        protected String wlsServerHome;
        private static final String SERVER_START_COMMAND = "bin\\startWebLogic.cmd";
        private static final String SERVER_STOP_COMMAND = "bin\\stopWebLogic.cmd";

        private String serverStartCommand;
        private String serverStopCommand;
        protected String unpackDirName = "C:\\CA\\sourcesUnpacked\\install";
        protected String installerFileName;
        protected String setupInstallerName;
        private String agentPath;
        private String agentProfilePath;
        private boolean osbSunJvmVendor;
        protected String javaAgentArguments;
        protected URL installPackageUrl;

        static {
            DEFAULT_ENCODING = StandardCharsets.UTF_8;
        }
        @NotNull
        protected String getOsSeparator() {
            return "\\";
        }

        protected String jreHomeLocation;
        protected String domainDirRelativePath;

        public Builder() {
            this.encoding(DEFAULT_ENCODING);
            //this.installPath(this.concatPaths(this.getDeployBase(), "osb"));
        }

        public OrclSrvcBusFlowContext build() {
            this.initResponseFileData();
            this.initOsbInstallDir();
            this.initStartStopCommand();
            this.initJavaAgentArguments();
            OrclSrvcBusFlowContext context = this.getInstance();
            Args.notNull(context.installLocation, "OSB install path");
            Args.notNull(context.installPackageUrl, "installPackageUrl");
            Args.notNull(context.responseFileName, "responseFileName");
            Args.notNull(context.unpackDirName, "unpackDirName");
            Args.notNull(context.installerFileName, "installerFileName");
            Args.notNull(context.setupInstallerName, "setupInstallerName");
            Args.notNull(context.encoding, "Response file encoding");
            Args.notNull(context.wlsServerHome, "WLS HOME");
            Args.notNull(context.jreHomeLocation, "jre HOME for installer");
            return context;
        }

        protected void initStartStopCommand() {
            if(this.domainDirRelativePath == null){
                this.defaultDomainHome(this.installPath + this.getOsSeparator() + "user_projects\\samples\\domains\\servicebus");
            }
            if(this.serverStartCommand == null) {
                this.serverStartCommand(this.domainDirRelativePath + this.getOsSeparator() + SERVER_START_COMMAND);
                this.serverStopCommand(this.domainDirRelativePath + this.getOsSeparator() + SERVER_STOP_COMMAND);
            }
        }

        protected void initResponseFileData() {
            this.responseFileOptions.put("ORACLE_HOME", this.installPath + "\\Oracle_Home" );
            this.responseFileOptions.put("MIDDLEWARE_HOME", this.installPath);
            this.responseFileOptions.put("Oracle Service Bus IDE", "false");
            this.responseFileOptions.put("WL_HOME", this.wlsServerHome);
            this.responseFileOptions.put("SKIP_SOFTWARE_UPDATES", "true");
        }
        protected void initOsbInstallDir() {
            if(this.installPath == null) {
                Args.notNull(this.installerFileName, "OSB\'s installer file name");
                String filename = FilenameUtils.getBaseName(this.installerFileName);
                this.installPath(this.getInstallBase() + filename);
            }
        }

        protected void initJavaAgentArguments() {
            assert this.installPath != null;

            if(this.agentPath == null) {
                this.initAgentPath();
            }

            if(this.agentProfilePath == null) {
                this.initAgentProfilePath();
            }

            this.javaAgentArguments = String.format("-javaagent:%s -Dcom.wily.introscope.agentProfile=%s", new Object[]{this.agentPath, this.agentProfilePath});
        }

        protected void initAgentPath() {
            this.agentPath(this.installPath + this.getOsSeparator() + "wily\\Agent.jar");
        }

        protected void initAgentProfilePath() {
            this.agentProfilePath(this.installPath + this.getOsSeparator() + "wily\\core\\config\\IntroscopeAgent.profile");
        }

        protected OrclSrvcBusFlowContext getInstance() {
            return new OrclSrvcBusFlowContext(this, wlsServerHome);
        }

        public OrclSrvcBusFlowContext.Builder installPath(String installLocation) {
            this.installPath = installLocation;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder installPackageUrl(URL installPackageUrl) {
            this.installPackageUrl = installPackageUrl;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder wlsServerHome(String wlsServerHome) {
            this.wlsServerHome = wlsServerHome;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder agentPath(@NotNull String value) {
            this.agentPath = value;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder agentProfilePath(@NotNull String value) {
            this.agentProfilePath = value;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder installerFileName(String installerFileName) {
            this.installerFileName = installerFileName;
            return this.builder();
        }
        public OrclSrvcBusFlowContext.Builder serverStartCommand(@NotNull String value) {
            this.serverStartCommand = value;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder osbSunJvmVendor(@NotNull boolean setVendor) {
            this.osbSunJvmVendor = setVendor;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder serverStopCommand(@NotNull String value) {
            this.serverStopCommand = value;
            return this.builder();
        }


        public OrclSrvcBusFlowContext.Builder setupInstallerName(String setupInstallerName) {
            this.setupInstallerName = setupInstallerName;
            return this.builder();
        }

        public OrclSrvcBusFlowContext.Builder jreHomeLocation(String jreHomeLocation) {
            this.jreHomeLocation = jreHomeLocation;
            return this.builder();
        }

        protected OrclSrvcBusFlowContext.Builder builder() {
            return this;
        }

        public OrclSrvcBusFlowContext.Builder defaultDomainHome(String defaultDomainHome) {
            this.domainDirRelativePath =  defaultDomainHome;
            return this.builder();
        }

        public String getInstallBase() {
            return this.getWinDeployBase();
        }
    }
}
