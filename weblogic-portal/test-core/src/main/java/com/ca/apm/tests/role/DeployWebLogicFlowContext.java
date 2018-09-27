/*
 * Copyright (c) 2014 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.annotation.TasResource;
import com.ca.tas.builder.BuilderBase;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class DeployWebLogicFlowContext implements IFlowContext {

    private static final String XML_ATTRIBUTE_DATA_VALUE = "data-value";
    private static final String SILENT_INSTALL_XML_NAME_BEAHOME = "BEAHOME";
    private static final String SILENT_INSTALL_XML_NAME_WLS_INSTALL_DIR = "WLS_INSTALL_DIR";
    private static final String SILENT_INSTALL_XML_NAME_COMPONENT_PATHS = "COMPONENT_PATHS";
    private static final String SILENT_INSTALL_XML_NAME_INSTALL_NODE_MANAGER_SERVICE = "INSTALL_NODE_MANAGER_SERVICE";
    private static final String SILENT_INSTALL_XML_NAME_NODEMGR_PORT = "NODEMGR_PORT";
    private static final String SILENT_INSTALL_XML_NAME_INSTALL_SHORTCUT_IN_ALL_USERS_FOLDER = "INSTALL_SHORTCUT_IN_ALL_USERS_FOLDER";
    private static final String SILENT_INSTALL_XML_NAME_LOCAL_JVMS = "LOCAL_JVMS";

    /**
     * From where we download the installer
     */
    private final URL webLogicInstallerUrl;
    /**
     * Where we install the app server
     */
    private final String installLocation;
    /**
     * Complete pathname to the product installation directory in which to install WebLogic Server. Response file prop
     */
    private final String wlsInstallDir;
    /**
     * Components and subcomponents to be installed. To install multiple components, separate the components with a bar (|). Response file
     * prop
     */
    private final Set<String> componentsPaths;
    /**
     * Install Node Manager as a Windows service. The default is "no". Response file prop
     */
    private final boolean installNodeManagerService;
    /**
     * Node Manager listen port number. If none specified, installer uses default port 5556. Response file prop
     */
    private final int nodeManagerPort;
    /**
     * Where the installer gets stored
     */
    private final String webLogicInstallerDir;
    /**
     * Where do we store the response files
     */
    private final String responseFileDir;
    /**
     * Where do we store the installer after download
     */
    private final String webLogicInstallerFilename;
    /**
     * Installation log
     */
    private final String installLogFile;
    /**
     * Weblogic server directory
     */
    private final String wlServerDir;


    private final String serverStartCommand;
    private final String serverStopCommand;
    private final boolean genericJavaInstaller;
    private final String javaAgentArguments;
    private final Set<Triplet> responseFileData;


    /**
     * Flow logic context construction through builder
     *
     * @param builder WebLogic flow context's builder
     */
    private DeployWebLogicFlowContext(Builder builder) {
        webLogicInstallerUrl = builder.webLogicInstallerUrl;
        installLocation = builder.installLocation;
        wlsInstallDir = builder.wlsInstallDir;
        //@todo validation on components path
        componentsPaths = builder.componentsPaths;
        installNodeManagerService = builder.installNodeManagerService;
        nodeManagerPort = builder.nodeManagerPort;
        webLogicInstallerDir = builder.webLogicInstallerDir;
        responseFileDir = builder.responseFileDir;
        webLogicInstallerFilename = builder.webLogicInstallerFilename;
        installLogFile = builder.installLogFile;
        serverStartCommand = builder.serverStartCommand;
        serverStopCommand = builder.serverStopCommand;
        javaAgentArguments = builder.javaAgentArguments;
        genericJavaInstaller = builder.genericJavaInstaller;
        responseFileData = builder.responseFileData;
        wlServerDir = builder.wlServerDir;
    }

    @NotNull
    public Set<Triplet> getInstallResponseFileData() {
        return responseFileData;
    }

    public URL getWebLogicInstallerUrl() {
        return webLogicInstallerUrl;
    }

    public String getWebLogicInstallerDir() {
        return webLogicInstallerDir;
    }

    public String getResponseFileDir() {
        return responseFileDir;
    }

    public String getWebLogicInstallerFilename() {
        return webLogicInstallerFilename;
    }

    public String getInstallLogFile() {
        return installLogFile;
    }

    public String getWlsInstallDir() {
        return wlsInstallDir;
    }

    @TasResource(value = "wlsLogs", regExp = ".*log$")
    public String getInstallLocation() {
        return installLocation;
    }

    public Set<String> getComponentsPaths() {
        return componentsPaths;
    }

    @TasEnvironmentProperty("startCommand")
    public String getServerStartCommand() {
        return serverStartCommand;
    }

    @TasEnvironmentProperty("stopCommand")
    public String getServerStopCommand() {
        return serverStopCommand;
    }

    public boolean isGenericJavaInstaller() {
        return genericJavaInstaller;
    }

    public String getJavaAgentArgument() {
        return javaAgentArguments;
    }

    public String getWlServerDir() {
        return wlServerDir;
    }

    public static class LinuxBuilder extends Builder {

        private static final String SERVER_START_COMMAND = "bin/startWebLogic.sh";
        private static final String SERVER_STOP_COMMAND = "bin/stopWebLogic.sh";
        private static final String AGENT_RELATIVE_PATH = "wily/Agent.jar";
        private static final String AGENT_PROFILE_RELATIVE_PATH = "wily/core/config/IntroscopeAgent.profile";

        public LinuxBuilder() {
            installLocation(getLinuxDeployBase() + WEBLOGIC_BASE_DIR + getOsSeparator() + DEFAULT_INSTALL_LOCATION);
        }

        @NotNull
        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase() + WEBLOGIC_BASE_DIR + getOsSeparator();
        }

        @NotNull
        @Override
        protected String getOsSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected void initAgentPath() {
            agentPath(wlsInstallDir + getOsSeparator() + AGENT_RELATIVE_PATH);
        }

        @Override
        protected void initAgentProfilePath() {
            agentProfilePath(wlsInstallDir + getOsSeparator() + AGENT_PROFILE_RELATIVE_PATH);
        }

        @Override
        protected void initStartStopCommands() {
            if (serverStartCommand != null) {
                return;
            }

            serverStartCommand(wlServerDir + SERVER_START_COMMAND);
            serverStopCommand(wlServerDir + SERVER_STOP_COMMAND);
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployWebLogicFlowContext> {

        private static final boolean DEFAULT_GENERIC_JAVA_INSTALLER = false;
        private static final int DEFAULT_NODE_MANAGER_PORT = 5595;
        private static final boolean DEFAULT_INSTALL_NODE_MANAGER_SERVICE = false;
        private static final String AGENT_RELATIVE_PATH = "wily\\Agent.jar";
        private static final String AGENT_PROFILE_RELATIVE_PATH = "wily\\core\\config\\IntroscopeAgent.profile";
        private static final String DEFAULT_INSTALLER_DIR = "sources";
        private static final String DEFAULT_RESPONSE_FILES_DIR = "responseFiles";
        private static final String DEFAULT_INSTALL_LOG = "install.log";

        private static final String SERVER_START_COMMAND = "bin\\startWebLogic.cmd";
        private static final String SERVER_STOP_COMMAND = "bin\\stopWebLogic.cmd";

        protected static final String WEBLOGIC_BASE_DIR = "Oracle";
        protected static final String DEFAULT_INSTALL_LOCATION = "install";

        private static final Set<String> DEFAULT_COMPONENT_PATHS = new HashSet<>(Arrays.asList(
            "WebLogic Server/Core Application Server",
            "WebLogic Server/Administration Console",
            "WebLogic Server/Configuration Wizard and Upgrade Framework",
            "WebLogic Server/Web 2.0 HTTP Pub-Sub Server",
            "WebLogic Server/WebLogic JDBC Drivers",
            "WebLogic Server/Third Party JDBC Drivers",
            "WebLogic Server/WebLogic Server Clients",
            "WebLogic Server/WebLogic Web Server Plugins",
            "WebLogic Server/UDDI and Xquery Support",
            "WebLogic Server/Server Examples")
        );

        private final Set<String> componentsPaths = new HashSet<>();
        private final Set<Triplet> responseFileData = new LinkedHashSet<>();

        protected String installLocation;
        protected String wlsInstallDir;
        protected String domainDirRelativePath;
        protected String wlServerDir;

        protected String webLogicInstallerDir;
        protected String responseFileDir;
        protected String installLogFile;

        protected int nodeManagerPort = DEFAULT_NODE_MANAGER_PORT;
        protected boolean installNodeManagerService = DEFAULT_INSTALL_NODE_MANAGER_SERVICE;
        protected String serverStartCommand;
        protected String serverStopCommand;
        private String agentPath;
        private String agentProfilePath;
        /**
         * If specified, denotes the path of the JRE to be used by WebLogic.   If not specified then the LOCAL_JVMS field in the WebLogic
         * silent install xml will not br generated.   WebLogic will then either use the bundled JRE (if installed) or use the JDK
         * referenced by JAVA_HOME.
         */
        private String localJvm;
        @Nullable
        private String webLogicInstallerFilename;
        @Nullable
        private URL webLogicInstallerUrl;
        private boolean genericJavaInstaller = DEFAULT_GENERIC_JAVA_INSTALLER;
        protected String javaAgentArguments;
        private boolean useDefaultComponentPaths;

        public Builder() {
            installLocation(getDeployBase() + DEFAULT_INSTALL_LOCATION);
        }

        @NotNull
        protected String getOsSeparator() {
            return WIN_SEPARATOR;
        }

        @NotNull
        @Override
        protected String getDeployBase() {
            return super.getDeployBase() + WEBLOGIC_BASE_DIR + getOsSeparator();
        }

        @NotNull
        protected String getInstallBase() {
            return getDeployBase() + DEFAULT_INSTALL_LOCATION + getOsSeparator();
        }

        public DeployWebLogicFlowContext build() {

            initWlsInstallDir();
            initWlServerPath();
            initJavaAgentArguments();
            initStartStopCommands();
            initWebLogicInstallerDir();
            initResponseFilesDir();
            initLogFile();
            initComponentPaths();
            initResponseFileData();

            DeployWebLogicFlowContext flowContext = getInstance();
            Args.notNull(flowContext.webLogicInstallerUrl, "WebLogic's installer URL");
            Args.notNull(flowContext.webLogicInstallerFilename, "WebLogic's installer file name");
            Args.notNull(flowContext.serverStartCommand, "WebLogic start command");
            Args.notNull(flowContext.javaAgentArguments, "Java Agent arguments");
            Args.notNull(flowContext.installLocation, "Install location");
            Args.notNull(flowContext.responseFileData, "Response file data");

            return flowContext;
        }

        protected void initWlsInstallDir() {
            if (wlsInstallDir != null) {
                return;
            }
            Args.notNull(webLogicInstallerFilename, "WebLogic's installer file name");
            String filename = FilenameUtils.getBaseName(webLogicInstallerFilename);
            wlsInstallDir(getInstallBase() + filename);
        }

        protected void initJavaAgentArguments() {
            assert wlsInstallDir != null;

            if (agentPath == null) {
                initAgentPath();
            }
            if (agentProfilePath == null) {
                initAgentProfilePath();
            }
            // Set Java environment options
            javaAgentArguments = String.format("-javaagent:%s -Dcom.wily.introscope.agentProfile=%s", agentPath, agentProfilePath);
        }

        protected void initAgentPath() {
            agentPath(wlsInstallDir + getOsSeparator() + AGENT_RELATIVE_PATH);
        }

        protected void initAgentProfilePath() {
            agentProfilePath(wlsInstallDir + getOsSeparator() + AGENT_PROFILE_RELATIVE_PATH);
        }

        protected void initWlServerPath() {
            assert wlsInstallDir != null : "initWlsInstallDir() must be called first.";

            wlServerDir = String.format("%1$s%3$s%2$s%3$s", wlsInstallDir, domainDirRelativePath, getOsSeparator());
        }

        protected void initStartStopCommands() {
            if (serverStartCommand != null) {
                return;
            }

            serverStartCommand(wlServerDir + SERVER_START_COMMAND);
            serverStopCommand(wlServerDir + SERVER_STOP_COMMAND);
        }

        protected void initWebLogicInstallerDir() {
            if (webLogicInstallerDir != null) {
                return;
            }

            webLogicInstallerDir(wlsInstallDir + getOsSeparator() + DEFAULT_INSTALLER_DIR);
        }

        protected void initResponseFilesDir() {
            if (responseFileDir != null) {
                return;
            }

            responseFileDir(wlsInstallDir + getOsSeparator() + DEFAULT_RESPONSE_FILES_DIR);
        }

        protected void initLogFile() {
            if (installLogFile != null) {
                return;
            }

            installLogFile(wlsInstallDir + getOsSeparator() + DEFAULT_INSTALL_LOG);
        }

        protected void initComponentPaths() {
            if (!componentsPaths.isEmpty() && !useDefaultComponentPaths) {
                return;
            }
            //only default if non set
            componentsPaths.addAll(DEFAULT_COMPONENT_PATHS);
        }

        protected void initResponseFileData() {
            responseFileData.addAll(
                Arrays.asList(
                    new Triplet(XML_ATTRIBUTE_DATA_VALUE, SILENT_INSTALL_XML_NAME_BEAHOME, installLocation),
                    new Triplet(XML_ATTRIBUTE_DATA_VALUE, SILENT_INSTALL_XML_NAME_COMPONENT_PATHS, StringUtils.join(componentsPaths, "|")),
                    new Triplet(XML_ATTRIBUTE_DATA_VALUE, SILENT_INSTALL_XML_NAME_INSTALL_NODE_MANAGER_SERVICE, installNodeManagerService))
            );

            if (localJvm != null) {
                responseFileData.add(new Triplet(XML_ATTRIBUTE_DATA_VALUE, SILENT_INSTALL_XML_NAME_LOCAL_JVMS, localJvm));
            }
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployWebLogicFlowContext getInstance() {
            return new DeployWebLogicFlowContext(this);
        }

        public Builder webLogicInstallerUrl(@NotNull URL webLogicInstallerUrl) {
            this.webLogicInstallerUrl = webLogicInstallerUrl;
            return builder();
        }

        public Builder installLocation(@NotNull String installLocation) {
            this.installLocation = installLocation;
            return builder();
        }

        public Builder wlsInstallDir(@NotNull String wlsInstallDir) {
            this.wlsInstallDir = wlsInstallDir;
            return builder();
        }

        public Builder noNodeManagerService() {
            installNodeManagerService = false;
            return builder();
        }

        public Builder nodeManagerPort(int nodeManagerPort) {
            this.nodeManagerPort = nodeManagerPort;
            return builder();
        }

        public Builder webLogicInstallerDir(@NotNull String webLogicInstallerDir) {
            this.webLogicInstallerDir = webLogicInstallerDir;
            return builder();
        }

        public Builder responseFileDir(@NotNull String responseFileDir) {
            this.responseFileDir = responseFileDir;
            return builder();
        }

        public Builder webLogicInstallerFilename(@NotNull String webLogicInstallerFilename) {
            this.webLogicInstallerFilename = webLogicInstallerFilename;
            return builder();
        }

        public Builder installLogFile(@NotNull String installLogFile) {
            this.installLogFile = installLogFile;
            return builder();
        }

        public Builder domainDirRelativePath(@NotNull String domainDirRelativePath) {
            this.domainDirRelativePath = domainDirRelativePath;
            return builder();
        }

        public Builder serverStartCommand(@NotNull String value) {
            serverStartCommand = value;
            return builder();
        }

        public Builder serverStopCommand(@NotNull String value) {
            serverStopCommand = value;
            return builder();
        }

        public Builder agentPath(@NotNull String value) {
            agentPath = value;
            return builder();
        }

        public Builder agentProfilePath(@NotNull String value) {
            agentProfilePath = value;
            return builder();
        }

        /**
         * @param value use either path to custom JVM or null to use system default JVM
         */
        public Builder localJvm(@Nullable String value) {
            localJvm = value;
            return builder();
        }

        public Builder genericJavaInstaller() {
            genericJavaInstaller = true;
            return builder();
        }

        public Builder customComponentPaths(Set<String> componentsPaths) {
            Args.notNull(componentsPaths, "Component paths");
            this.componentsPaths.addAll(componentsPaths);
            return builder();
        }

        public Builder useDefaultComponentPaths() {
            useDefaultComponentPaths = true;

            return builder();
        }
    }

    @Override
    public String toString() {
        return "DeployWebLogicFlowContext{" +
               "webLogicInstallerUrl=" + webLogicInstallerUrl +
               ", installLocation=" + installLocation +
               ", wlsInstallDir=" + wlsInstallDir +
               ", componentsPaths=" + componentsPaths +
               ", noNodeManagerService=" + installNodeManagerService +
               ", nodeManagerPort=" + nodeManagerPort +
               ", webLogicInstallerDir=" + webLogicInstallerDir +
               ", responseFileDir=" + responseFileDir +
               ", webLogicInstallerFilename='" + webLogicInstallerFilename + '\'' +
               ", installLogFolder=" + installLogFile +
               '}';
    }
}

