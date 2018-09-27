package com.ca.apm.systemtest.fld.plugin.em;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

public interface EmPlugin extends Plugin {

    String PLUGIN = "emPlugin";

    public static final int DEFAULT_EM_PORT = 5001;

    enum Database {
        oracle, postgre, local
    }

    class InstallationParameters {
        public String installDir;
        public OperatingSystemFamily platform;
        public String noInstallerSpecification;
        public String osgiBuildId;
        public String trussServer;
        public String logs;
        public String jvmExtraArgs;
        public Long heapSize;

        public Database db;
        public String dbHost;
        public Integer dbPort;
        public String dbSid;
        public String dbUserName;
        public String dbUserPass;
        public String dbAdminName;
        public String dbAdminPass;
        public String dbConfigImportTargetRelease;

        public String wvEmHost;
        public Integer wvEmPort;

        public String[] collectors;

        public InstallerType installerType;
    }

    enum InstallStatus {
        Installing, Installed, Error, UnknownInstallationInstanceId
    }

    class CLWResult {
        public Integer exitCode;
        public String output;
    }

    /**
     * Begins installation of the component in the background
     *
     * @param config
     * @return installation instance ID - used to query installation status
     */
    String install(InstallationParameters config);

    InstallStatus checkInstallStatus(String installationInstanceId);

    void uninstall(InstallationParameters config);

    void start(InstallationParameters config);

    void stop(InstallationParameters config);

    void fetchArtifact(String repoBase, String productName, String codeName, String buildNumber,
        String buildId);

    Integer runJarArtifact(String jarFileName, String outFileName, String[] javaParameters,
        String[] jarParameters);

    void runJarClass(String jarFileName, String className, String outFileName,
        String[] javaParameters, String[] classParameters);

    String runGroovyScript(String script, String jarFile, Map<String, String> variables);
    
    void executeCLW(String agentName, String outFileName);

    Integer executeCLWCommand(String command, String outFileName, boolean waitFor);

    CLWResult executeCLWCommand(String command) throws IOException;

    void verifyTDList(String agentName, String outFileName)  throws Exception;
    
    /**
     * Fetches the specified artifact and installs the management modules onto the MOM (or EM)
     * @param artifact
     * @return
     */
    public void installManagementModules(String artifact);

    List<Integer> executeJdbc(String... queries);
    List<Integer> executeJdbc(String emHost, Integer emPort, String user, String password, String... queries);

}
