package com.ca.apm.systemtest.fld.plugin.em;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.LoaderConfiguration;
import org.codehaus.groovy.tools.RootLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.ProcessExecutor;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.IntroscopeDriverDBAccessHelper;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;
import com.ca.apm.systemtest.fld.plugin.run2.Run2Plugin;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemArch;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;

/**
 * Plugin for installation and uninstallation of EM Introscope. Support for installation MOM and
 * collector without database on Linux machine
 *
 * @author jirji01
 */

@PluginAnnotationComponent(pluginType = EmPlugin.PLUGIN)
public class EmPluginImpl extends AbstractPluginImpl implements EmPlugin, InitializingBean {

    private static final String OSGI_BUILD_ID = "OSGI_BUILD_ID";

    private static final Logger log = LoggerFactory.getLogger(EmPluginImpl.class);

    private static final String SAMPLE_RESPONSE_FILE = "SampleResponseFile.Introscope.txt";
    private static final String CA_EULA = "ca-eula.txt";
    private static final String OSGI_EULA = "eula.txt";
    private HashMap<String, InstallStatus> installStatusMap = new HashMap<>();
    private HashMap<String, Thread> installThreadMap = new HashMap<>();
    //clw commands
    //generate thread dump clw command
    private static final String CLW_GET_THREAD_DUMP_COMMAND = "get ThreadDump for the agent matching .*[agentName].* for functional";

    public static final String CLWORKSTATION_JAR = "CLWorkstation.jar";

    //get list of existing dumps clw command
    @SuppressWarnings("unused")
    private static final String CLW_GET_LIST_OF_THREAD_DUMPS = "get List of ThreadDump filenames for the agent matching .*[agentName].*";

    public static final String EM_INSTALLER_ARTIFACT_SPEC = String.format(
            "${%s}/${%s}/build-${%s}(${%s})/Internal/RawTools/${%s}",
            ArtifactManager.KEY_REPO_BASE,
            ArtifactManager.KEY_CODE_NAME,
            ArtifactManager.KEY_BUILD_NUMBER,
            ArtifactManager.KEY_BUILD_ID,
            ArtifactManager.KEY_PRODUCT
    );

    public static final String EM_OPENSOURCE_ARTIFACT_SPEC = String.format(
            "${%s}/${%s}/build-${%s}(${%s})/opensource/${%s}.v${%s}.${%s}.${%s}",
            ArtifactManager.KEY_REPO_BASE,
            ArtifactManager.KEY_CODE_NAME,
            ArtifactManager.KEY_BUILD_NUMBER,
            ArtifactManager.KEY_BUILD_ID,
            ArtifactManager.KEY_PRODUCT,
            OSGI_BUILD_ID,
            ArtifactManager.KEY_OS_ARCHITECTURE,
            ArtifactManager.KEY_OS_ARCHIVE_EXTENSION
    );

    public static final String EM_OPENSOURCE_EULA_ARTIFACT_SPEC = String.format(
            "${%s}/${%s}/build-${%s}(${%s})/opensource/%s",
            ArtifactManager.KEY_REPO_BASE,
            ArtifactManager.KEY_CODE_NAME,
            ArtifactManager.KEY_BUILD_NUMBER,
            ArtifactManager.KEY_BUILD_ID,
            OSGI_EULA
    );

    public static final String INTROSCOPE_JDBC_JAR = "lib/IntroscopeJDBC.jar";
    public static final String INTROSCOPE_DRIVER_URL = "jdbc:introscope:net//{0}:{1}@{2}:{3}"; // i.e. jdbc:introscope:net//Admin:@localhost:5001
    public static final long JDBC_QUERY_TIMEOUT_SECONDS = 150;

    @Autowired
    TrussDownloadMethod trussDm;

    @Autowired
    ArtifactoryLiteDownloadMethod artifactoryDm;

    @Autowired
    Run2Plugin run2Plugin;

    public EmPluginImpl() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // ensure the default configuration is set,and that our current configuration is readable
        EmPluginConfiguration cfg = configurationManager
            .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        log.info("Read configuration {}", cfg);
    }

    @Override
    public PluginConfiguration getPluginConfiguration() {
        EmPluginConfiguration cfg = configurationManager
            .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        return cfg;
    }

    @Override
    @ExposeMethod(description = "Checks the status of a previously started installation.")
    public InstallStatus checkInstallStatus(String installationInstanceId) {
        info("Checking install status of " + installationInstanceId);
        InstallStatus status = null;
        synchronized (installStatusMap) {
            status = installStatusMap.get(installationInstanceId);
            if (status == null) {
                info("Status was null, returning " + InstallStatus.UnknownInstallationInstanceId);
                return InstallStatus.UnknownInstallationInstanceId;
            }
            if (status != InstallStatus.Installed) {
                Thread th = installThreadMap.get(installationInstanceId);
                if (th == null) {
                    info("Status was " + status + " and thread was was null, returning "
                        + InstallStatus.UnknownInstallationInstanceId);
                    return InstallStatus.UnknownInstallationInstanceId;
                }
                if (!th.isAlive()) {
                    status = InstallStatus.Error;
                    info("Thread was not alive, returning " + status);
                    installStatusMap.put(installationInstanceId, status);
                }
            }
        }

        info("Install status: " + status);
        return status;
    }

    @ExposeMethod(description = "Install EM into given prefix.")
    @Override
    public String install(final InstallationParameters params) {
        final String installId = "em-install-" + System.currentTimeMillis();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                proceedWithInstallation(params);

                synchronized (installStatusMap) {
                    installStatusMap.put(installId, InstallStatus.Installed);
                    installThreadMap.remove(installId);
                    info(this.getClass().getSimpleName() + ": installation completed for id \""
                            + installId + "\"");
                }
            }
        };

        synchronized (installStatusMap) {
            Thread th = new Thread(r);
            info(this.getClass().getSimpleName() + ": starting installation with id \"" + installId
                + "\"");
            installStatusMap.put(installId, InstallStatus.Installing);
            installThreadMap.put(installId, th);
            th.start();
        }

        return installId;
    }

    protected void proceedWithInstallation(InstallationParameters params)
    {
        final Long dashboardId = DashboardIdStore.getDashboardId();
        final String currentOp = currentOperation.get();

        DashboardIdStore.setDashboardId(dashboardId);
        currentOperation.set(currentOp);

        EmPluginConfiguration cfg = configurationManager
                .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);

        if (cfg.isInstalled()) {
            uninstall(params);
        }

        params.platform = SystemUtil.getOsFamily();
        params.installDir = defaultIfBlank(params.installDir, cfg.getDefaultInstallDir());
        params.logs = defaultIfBlank(params.logs, cfg.getDefaultLogDir());

        Path instDir = Paths.get(params.installDir);
        Path logDir = Paths.get(params.logs);
        File tmpDir = createTempDirectory("em-staging");
        Path installerDir = tmpDir.toPath();

        info("Fetch and unzip installation files into " + installerDir.toString());
        Path osgiPackage = null;
        Path osgiEula = null;
        ArtifactFetchResult installerPackage;

        try {
            final String osFamilyTruss;
            final String osFamilyArtifactory;
            final String suffix;
            final String fileExtension;
            final String fileExtensionArchive;
            final String architecture;

            switch (params.platform)
            {
                case Windows:
                {
                    osFamilyTruss = "windows";
                    osFamilyArtifactory = "windows";
                    suffix = "windowsAMD64";
                    fileExtension = "exe";
                    fileExtensionArchive = "zip";
                    if (SystemUtil.getOsArch().equals(OperatingSystemArch.Arch64Bit)) {
                        architecture = "windowsAMD64";
                    } else {
                        architecture = "windowsinstall";
                    }
                    break;
                }
                case Linux:
                {
                    osFamilyTruss = "unix";
                    osFamilyArtifactory = "unix";
                    suffix = "linuxAMD64";
                    fileExtension = "bin";
                    fileExtensionArchive = "tar";
                    if (SystemUtil.getOsArch().equals(OperatingSystemArch.Arch64Bit)) {
                        architecture = "linuxAMD64";
                    } else {
                        architecture = "linuxinstall";
                    }
                    break;
                }
                default:
                {
                    throw new ArtifactManagerException("Os, that is used in configuration is unknown");
                }
            }

            if (params.noInstallerSpecification.startsWith("maven"))
            {
                String groupId = "com.ca.apm.delivery";
                String version = params.noInstallerSpecification.split(":")[1];
                String artifactId="introscope-installer-"+osFamilyArtifactory;

                //.exe file here, so no need to unzip
                downloadByArtifactory(groupId, artifactId, version, suffix, fileExtension);

                if (params.installerType != InstallerType.DATABASE) {

                    artifactId = "opensource";
                    warn("Truss method is used for download OSGI and eula, need to update this");

                    osgiPackage = downloadByArtifactory(groupId, artifactId, version,
                            osFamilyArtifactory + "-dist", fileExtensionArchive).getFile().toPath();


//                            osgiEula = downloadByArtifactory(groupId, artifactId, version, osFamily + "-dist", fileExtensionArchive).getFile().toPath();
                    //TODO - DM -  NEED ARTIFACTORY URL!!!
                }

                throw new ArtifactManagerException("Maven method is not implemented yet - osgi and eula are not downloaded");
            }

            if (params.noInstallerSpecification.startsWith("truss"))
            {
                String[] splits = params.noInstallerSpecification.split(":");

                if (splits.length!=4)
                {
                    throw new ArtifactManagerException("Number of truss parameters is incorrect");
                }

                final String codeName = splits[1];    //=BRANCH="10.0.0-ISCP"
                final String buildNumber = splits[2]; //"990006"
                final String version = splits[3];     //=BUILD_ID="10.0.0.8"

                final String server = params.trussServer != null ? params.trussServer : "truss.ca.com";

                @SuppressWarnings("serial")
                Map<String, Object> parameters = new HashMap<String, Object>() {
                    {
                        put(ArtifactManager.KEY_REPO_BASE, "http://" + server + "/builds/InternalBuilds");
                        put(ArtifactManager.KEY_INSTALLER_PREFIX, "introscope");

                        put(ArtifactManager.KEY_CODE_NAME, codeName + "-ISCP");
                        put(ArtifactManager.KEY_BUILD_NUMBER, buildNumber);
                        put(ArtifactManager.KEY_BUILD_ID, version);

                        put(ArtifactManager.KEY_PRODUCT, "introscope");
                        put(ArtifactManager.KEY_OS_ARCHITECTURE, architecture);
                        put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION, fileExtensionArchive);
                    }
                };

                installerPackage = downloadByTruss(null, installerDir, parameters);

                try {
                    unpackFile(installerPackage.getFile(), params);
                    Files.delete(installerPackage.getFile().toPath());
                } catch(IOException ex)
                {
                    throw ErrorUtils.logExceptionAndWrap(log, ex, "Cannot delete unpacked installer artifact");
                }

                //Download OSGI and EULA
                if (params.installerType != InstallerType.DATABASE) {
                    parameters.put(ArtifactManager.KEY_OS_ARCHITECTURE, osFamilyTruss);
                    parameters.put(ArtifactManager.KEY_BUILD_ID, version);
                    parameters.put(OSGI_BUILD_ID, params.osgiBuildId);
                    parameters.put(ArtifactManager.KEY_PRODUCT, "osgiPackages");

                    osgiPackage = downloadByTruss(EM_OPENSOURCE_ARTIFACT_SPEC, installerDir, parameters).getFile().toPath();
                    osgiEula = downloadByTruss(EM_OPENSOURCE_EULA_ARTIFACT_SPEC, installerDir, parameters).getFile().toPath();
                }
            }

        } catch (ArtifactManagerException e) {
            throw ErrorUtils
                    .logExceptionAndWrap(log, e, "Cannot download installer artifact");
        }

        //default installer is EM with DB
        if (params.installerType == null) {
            params.installerType = InstallerType.EM;
        }

        info("Updating response file");
        InstallerProperties response =
                new InstallerProperties(
                        Paths.get(installerDir.toString(), SAMPLE_RESPONSE_FILE), //TODO - DM - sample response file not present for Artifactory
                        params.installerType)
                        .setInstallationDir(instDir);

        if (SystemUtil.getOsFamily() == OperatingSystemFamily.Linux) {
            response.setHeadless(true);
        }

        if (params.installerType != InstallerType.DATABASE) {
            response.setExternalComponentPackage(osgiPackage);
        }

        if (params.logs != null) {
            response.setLogFolder(logDir);
        }

        if (!StringUtils.isEmpty(params.jvmExtraArgs)) {
            response.setJvmExtraArgs(params.jvmExtraArgs);
        }

        if (StringUtils.isEmpty(params.dbUserName)) {
            params.dbUserName = cfg.getDefaultDbUserName();
        }

        if (StringUtils.isEmpty(params.dbUserPass)) {
            params.dbUserPass = cfg.getDefaultDbUserPass();
        }

        if (StringUtils.isEmpty(params.dbAdminName)) {
            params.dbAdminName = cfg.getDefaultDbAdminName();
        }

        if (StringUtils.isEmpty(params.dbAdminPass)) {
            params.dbAdminPass = cfg.getDefaultDbAdminPass();
        }

        switch (params.installerType) {
            case AGC:
                params.dbHost="127.0.0.1";
            case EM:
                // install EM as MOM
                if (params.collectors != null && params.collectors.length > 0) {
                    response.setCollectors(Arrays.asList(params.collectors));
                }

                switch (params.db) {
                    case oracle:
                        response
                                .setOracleDatabase(params.dbHost, params.dbPort, params.dbSid,
                                        params.dbUserName, params.dbUserPass);
                        break;
                    case postgre:
                        response.setPostgreDatabase(
                                params.dbHost,
                                params.dbUserName,
                                params.dbUserPass,
                                params.dbAdminName,
                                params.dbAdminPass);
                        break;
                    default:
                        // do not change default DB setting
                }
                break;

            case WEBVIEW:
                if (params.wvEmHost != null && params.wvEmPort != null) {
                    response.setEmHostPort(params.wvEmHost, params.wvEmPort);
                }
                break;

            case DATABASE:
                switch (params.db) {
                    case oracle:
                        response
                                .setOracleDatabase(params.dbHost, params.dbPort, params.dbSid,
                                        params.dbUserName, params.dbUserPass);
                        break;
                    case postgre:
                        response.setPostgreDatabase(params.dbHost, params.dbUserName, params.dbUserPass,
                                params.dbAdminName, params.dbAdminPass);
                        break;
                    default:
                        // do not change default DB setting
                }
                break;

            default:
                break;

        }

        response.writeResponseFile();
        log.debug("Response file {}:\n{}", response.getResponseFile().toString(),
                response.toString());

        info("Accept eula");
        new Eula(Paths.get(installerDir.toString(), CA_EULA)).acceptEula();
        if (params.installerType != InstallerType.DATABASE) {
            new Eula(osgiEula).acceptEula();
        }
        info("Start silent installer");
        startInstaller(installerDir, instDir.toString(), response);
        info("Installation finished successfully");

        cfg.setCurrentGcLogFile(response.getGcLogFile().toString());
        cfg.setCurrentInstallDir(instDir.toString());
        cfg.setCurrentLogDir(logDir.toString());
        cfg.setInstalled(true);
        configurationManager.savePluginConfiguration(PLUGIN, cfg);

        deleteFolder(installerDir);

    }



    protected ArtifactFetchResult downloadByArtifactory(String groupId, String artifactId, String version, String suffix, String fileExtension) {
        return artifactoryDm.fetchTempArtifact(
                ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL,
                groupId, artifactId, version, suffix, fileExtension
        );
    }

    protected ArtifactFetchResult downloadByTruss(String artifactorySpecification, Path destinationDirecotry, Map<String, Object> parameters) throws ArtifactManagerException {
        return trussDm.fetch(artifactorySpecification, destinationDirecotry.toFile(), parameters, true);
    }

    private void deleteFolder(Path directory) {
        if (Files.exists(directory)) {
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                });
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Cannot delete folder {1}. Exception {0}", directory);
            }
        }
    }

    protected File findFile(Path directory, String pattern) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
            directory)) {
            for (Path file : directoryStream) {
                if (matcher.matches(file.getFileName())) {
                    return file.toFile();
                }
            }
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Cannot find the installer file {1} in directory {2}. Exception {0}",
                pattern, directory);
        }
        throw ErrorUtils.logErrorAndReturnException(log,
            "Cannot find the installer file {1} in directory {2}. Exception {0}",
            pattern, directory);
    }

    protected void startInstaller(Path installerDir, String installDir,
        InstallerProperties installerProperties) {
        File installerFile = findFile(installerDir, "introscope*.{exe,bin}");
        installerFile.setExecutable(true, false);
        Map<String, String> env = new TreeMap<>(System.getenv());
        env.put("CLASSPATH", null);
        ProcessExecutor install =
            ProcessUtils2
                .newProcessExecutor()
                .command(installerFile.toString(), "-f",
                    installerProperties.getResponseFile().toString())
                .directory(installerDir.toFile())
                .environment(env);
        int result =
            ProcessUtils2.waitForProcess(ProcessUtils2.startProcess(install), 10,
                TimeUnit.MINUTES, true);

        if (result != 0) {
            ErrorUtils.throwRuntimeException("Installation failed with error code {0}", result);
        }

        // Save the installation success status and uninstaller info to the configuration
        EmPluginConfiguration cfg = configurationManager
            .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        String extension = osFamily == OperatingSystemFamily.Windows ? ".exe" : "";
        Path uninstaller = Paths
            .get(installDir, "UninstallerData", "base", "Uninstall_Introscope" + extension);
        cfg.setUninstallerFile(uninstaller.toString());
        cfg.setInstalled(true);
        configurationManager.savePluginConfiguration(PLUGIN, cfg);
    }

    private void unpackFile(File file, InstallationParameters config) {
        String unpacker = config.platform == OperatingSystemFamily.Windows ? "jar" : "tar";
        ProcessExecutor unpack =
            ProcessUtils2.newProcessExecutor().command(unpacker, "xvf",
                file.getAbsoluteFile().toString());
        unpack.directory(file.getParentFile());
        ProcessUtils2.waitForProcess(ProcessUtils2.startProcess(unpack), 5, TimeUnit.MINUTES, true);
    }

    @ExposeMethod(description = "Uninstall EM installed in given prefix.")
    @Override
    public void uninstall(InstallationParameters config) {
        EmPluginConfiguration cfg = getConfiguration();

        String uninstallerFile = cfg.getUninstallerFile();
        if (!isBlank(uninstallerFile)
            && Files.exists(Paths.get(cfg.getUninstallerFile()))) {
            log.info("Uninstaller exists... going to remove");
            Map<String, String> env = new TreeMap<>(System.getenv());
            env.put("CLASSPATH", null);
            ProcessExecutor uninstall =
                ProcessUtils2.newProcessExecutor().command(cfg.getUninstallerFile())
                    .directory(new File(cfg.getCurrentInstallDir()))
                    .environment(env);
            int result =
                ProcessUtils2.waitForProcess(ProcessUtils2.startProcess(uninstall), 5,
                    TimeUnit.MINUTES, true);
            if (result != 0) {
                ErrorUtils.throwRuntimeException("Uninstallation failed with error code {0}",
                    result);
            }
        } else {
            log.info("Uninstaller doesn't exist... skipping uninstall");
        }

        if (!isBlank(cfg.getCurrentInstallDir())) {
            deleteFolder(Paths.get(cfg.getCurrentInstallDir()));
            log.info("Installation folder {} deleted", cfg.getCurrentInstallDir());
        }

        cfg.setInstalled(false);
        saveConfiguration(cfg);
    }

    @Override
    public void start(InstallationParameters config) {
        EmPluginConfiguration cfg = getConfiguration();

        log.info("Starting EM server");
        Path instDir = Paths.get(cfg.getCurrentInstallDir());
        Path logDir = Paths.get(cfg.getCurrentLogDir());
        if (!NetworkUtils.isServerListening("localhost", 5001)) {
            File exeFile = findFile(instDir, "Introscope_Enterprise_Manager");

            ArrayList<String> commands = new ArrayList<>();

            boolean redirect = false;
            if (exeFile.exists()) {
                String unixCommandLine = "nohup ./Introscope_Enterprise_Manager >> " + logDir.toString() + "/em.txt &\n";
                File loStartScript = new File(instDir.toFile(), "loStartEM.sh");
                try {
                    FileOutputStream fos = new FileOutputStream(loStartScript);
                    fos.write(unixCommandLine.getBytes());
                    fos.flush();
                    fos.close();
                } catch (IOException ioe) {
                    ErrorUtils.logExceptionAndWrap(log, ioe, "Unable to write start script to disk");
                }
                
                
                commands.add("/bin/bash");
                commands.add("./loStartEM.sh");
            } else {
                exeFile = findFile(instDir, "Introscope_Enterprise_Manager.exe");
                commands.add(exeFile.getAbsolutePath());
                redirect = true;
            }

            ProcessBuilder em =
                ProcessUtils
                    .newProcessBuilder(true)
                    .command(commands)
                    .directory(instDir.toFile())
                    .redirectErrorStream(true);
            
            if (redirect) {
                em.redirectOutput(Redirect.appendTo(new File(logDir.toFile(), "em.txt")));
            }
            ProcessUtils.startProcess(em);

            log.debug("Waiting for EM");

            boolean isListening = false;
            for (int i = 0; i < 60; i++) {
                if (isListening = NetworkUtils.isServerListening("localhost", 5001)) {
                    break;
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // Panic!
                }
            }

            if (!isListening) {
                ErrorUtils.logErrorAndThrowException(log, "EM server is not running");
            }
        } else {
            log.info("Em was already running.");
        }
    }

    @Override
    public void stop(InstallationParameters config) {
        EmPluginConfiguration cfg = getConfiguration();

        log.info("Stopping EM server");
        Path instDir = Paths.get(cfg.getCurrentInstallDir());

        if (NetworkUtils.isServerListening("localhost", 5001)) {
            ProcessBuilder clw =
                ProcessUtils
                    .newProcessBuilder()
                    .command("java", "-jar",
                        Paths.get("lib", CLWORKSTATION_JAR).toString(), "shutdown")
                    .directory(instDir.toFile());
            ProcessUtils.waitForProcess(ProcessUtils.startProcess(clw), 1, TimeUnit.MINUTES, true);

            boolean isListening = true;
            for (int i = 0; i < 60; i++) {
                if (!(isListening = NetworkUtils.isServerListening("localhost", 5001))) {
                    break;
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // Panic!
                }
            }

            if (isListening) {
                ErrorUtils.logErrorAndThrowException(log, "EM server is running after shutdown.");
            }
        } else {
            log.info("EM was not running.");
        }
    }

    @Override
    public void fetchArtifact(String repoBase, String productName, String codeName,
        String buildNumber, String buildId) {
        Path downloadDir = Paths.get(System.getProperty("user.dir"));

        Map<String, Object> parameters = new HashMap<String, Object>(7);
        parameters.put(ArtifactManager.KEY_REPO_BASE, repoBase);
        parameters.put(ArtifactManager.KEY_CODE_NAME, codeName);
        parameters.put(ArtifactManager.KEY_BUILD_NUMBER, buildNumber);
        parameters.put(ArtifactManager.KEY_BUILD_ID, buildId);
        parameters.put(ArtifactManager.KEY_PRODUCT, productName);
        // parameters.put(ArtifactManager.KEY_OS_ARCHITECTURE, os);
        // parameters.put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION, os.startsWith("windows") ?
        // "zip" : "tar");

        // if (repoType.equals("truss")) type = RepositoryType.TRUSS;
        // else if (repoType.equals("artifactory")) type = RepositoryType.ARTIFACTORY;
        // else if (repoType.equals("artifactoryLite")) type = RepositoryType.ARTIFACTORYLITE;
        // else type = RepositoryType.HTTP;

        log.info("Fetch {} artifact into {}", productName, downloadDir.toString());
        try {
            trussDm.fetch(EM_INSTALLER_ARTIFACT_SPEC, downloadDir.toFile(), parameters, true);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot download artifact");
        }
    }

    @Override
    public Integer runJarArtifact(String jarFileName, String outFileName, String[] javaParameters,
        String[] jarParameters) {
        String[] params = new String[3 + javaParameters.length + jarParameters.length];
        int index = 0;
        params[index++] = "java";
        for (String javaParameter : javaParameters) {
            params[index++] = javaParameter;
        }
        params[index++] = "-jar";
        params[index++] = Paths.get(System.getProperty("user.dir"), jarFileName).toString();
        for (String jarParameter : jarParameters) {
            params[index++] = jarParameter;
        }
        log.info("Running {}", jarFileName);
        String pathOut = Paths.get(System.getProperty("user.dir"), outFileName).toString();
        log.info("Redirecting output to: {}", pathOut);

        Map<String, String> env = new HashMap<>(1);
        env.put("CLASSPATH", null);
        try {
            return run2Plugin.runProcess2(Arrays.asList(params), pathOut, null, env);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error executing process JAR artifact using command {1}. Exception: {0}", params);
        }
    }

    @Override
    public void runJarClass(String jarFileName, String className, String outFileName,
        String[] javaParameters, String[] classParameters) {
        String pathOut = Paths.get(System.getProperty("user.dir"), outFileName).toString();
        String[] params = new String[4 + javaParameters.length + classParameters.length];
        int index = 0;
        params[index++] = "java";
        for (int i = 0; i < javaParameters.length; ++i) {
            params[index++] = javaParameters[i];
        }
        params[index++] = "-cp";
        params[index++] = Paths.get(System.getProperty("user.dir"), jarFileName).toString();
        params[index++] = className;
        for (int i = 0; i < classParameters.length; ++i) {
            params[index++] = classParameters[i];
        }
        log.info("Running {}", className);
        log.info("Redirecting output to: {}", pathOut);
        ProcessBuilder clw = ProcessUtils.newProcessBuilder().command(params);
        clw.redirectOutput(new File(pathOut));
        ProcessUtils.startProcess(clw);
    }

    @Override
    public String runGroovyScript(String script, String jarFile, Map<String, String> variables) {
        Binding binding = new Binding();
        if (variables != null) {
            for (String k : variables.keySet()) {
                binding.setVariable(k, variables.get(k));
            }
        }
        GroovyShell shell = null;
        if (jarFile != null) {
            LoaderConfiguration loaderConfig = new LoaderConfiguration();
            loaderConfig
                .addClassPath(Paths.get(System.getProperty("user.dir"), jarFile).toString());
            CompilerConfiguration config = new CompilerConfiguration();
            config.setClasspath(Paths.get(System.getProperty("user.dir"), jarFile).toString());
            shell = new GroovyShell(new RootLoader(loaderConfig), binding, config);
        } else {
            shell = new GroovyShell(binding);
        }
        log.info("Running remote groovy script:\n{}", script);
        Object result = shell.evaluate(script);

        log.info("Result: '{}'", result.toString());
        return result.toString();
    }


    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.em.EmPlugin#installManagementModules(java.lang.String)
     */
    @Override
    public void installManagementModules(String artifactUrl) {
        // temporary override until downloader is fixed
        File zipFile = null;
        File tempDir = null;
        try {
            // fetch the management modules from artifactory
            URL url = new URL(artifactUrl);
            tempDir = createTempDirectory("mm");
            zipFile = new File(tempDir, "mm.zip");
            FileOutputStream out = new FileOutputStream(zipFile);
            IOUtils.copy(url.openStream(), out);
            out.flush();
            out.close();

            // unzip
            List<File> list = unzipFile(zipFile, tempDir);

            // and copy to the MOM's install directory $installDir/config/modules
            EmPluginConfiguration cfg = getConfiguration();
            File installDir = new File(cfg.getCurrentInstallDir());
            File mmDir = Paths.get(installDir.getAbsolutePath(), "config", "modules").toFile();
            for (File f: list) {
                org.apache.commons.io.FileUtils.copyFileToDirectory(f, mmDir);
            }

        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Unable to install memory modules");
        } finally {
            deleteFile(zipFile);
            deleteDirectory(tempDir);
        }
    }


    private List<File> unzipFile(File zipFile, File dest) throws IOException {
        List<File> list = new ArrayList<>();

        ZipFile zip = null;

        try {
            zip = new ZipFile(zipFile);
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                InputStream in = zip.getInputStream(entry);
                File destFile = new File(dest, entry.getName());
                FileOutputStream out = new FileOutputStream(destFile);
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                list.add(destFile);
            }
        } finally {
            IOUtils.closeQuietly(zip);
        }

        return list;
    }

    /**
     * Executes CLW to get thread dumps and write it to outFileName
     * @param agentName
     * @param outFileName
     */
    @Override
    public void executeCLW(String agentName, String outFileName){
        String command = CLW_GET_THREAD_DUMP_COMMAND.replace("[agentName]", agentName);
        executeCLWCommand(command, outFileName, false);
    }

    /**
     * Executes CLW to execute a common command and write the output to outFileName
     * @param command
     * @param outFileName
     */
    @Override
    public Integer executeCLWCommand(String command, String outFileName, boolean waitFor){
        String pathOut = Paths.get(System.getProperty("user.dir"), outFileName).toString();
        log.info("Executing CLW command {}", command);

        EmPluginConfiguration cfg = configurationManager.loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        Path instDir = Paths.get(cfg.getCurrentInstallDir());
        ProcessBuilder clw =
            ProcessUtils
                .newProcessBuilder()
                .command("java", "-jar",
                    Paths.get("lib", CLWORKSTATION_JAR).toString(), command)
                .directory(instDir.toFile());
        clw.redirectOutput(new File(pathOut));
        log.debug("CLW Output file path is {}", pathOut);
        if (waitFor) {
            log.info("CLW Starting process");
            int retVal =  ProcessUtils.waitForProcess(ProcessUtils.startProcess(clw), 15, TimeUnit.SECONDS,
                true);
            log.debug("CLW Process returned {}", retVal);
            return retVal;
        }
        else {
            ProcessUtils.startProcess(clw);
        }
        return null;
    }

    @Override
    @ExposeMethod(description = "Executes CLW command on EM")
    public CLWResult executeCLWCommand(String command) throws IOException {
        File outFile = createTempFile();
        Integer exitCode = executeCLWCommand(command, outFile.getName(), true);
        String output = FileUtils.readFileToString(outFile);

        CLWResult clwResult = new CLWResult();
        clwResult.exitCode = exitCode;
        clwResult.output = output;
        return clwResult;
    }

    /**
     * Verifies the collected thread dump (one sample) everytime to ensure that proper thread dumps are being collected
     * @param outFileName
     * @param agentName
     * @throws Exception
     */
    public void verifyTDList(String agentName, String outFileName) throws Exception{
        log.info("Verifying threaddumps collection periodically");
        int count = 0;
        String pathOut = Paths.get(System.getProperty("user.dir"), outFileName).toString();
        File file = new File(pathOut);
        log.info("Handle will now count the number of thread collected on agent :{} in file - {}",
            agentName, pathOut);

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
        String strLine;
            while ((strLine = br.readLine()) != null){
                    if(strLine.contains("-thread-120")) {
                        count++;
                    }
            }
        }
        catch(IOException e){
            ErrorUtils.logExceptionAndWrap(log, e, "Cannot read processUtils output");
        }
        log.info("Actual count from output stream file :{}", count);

        /**
         * comment by RSSSA02 (Ragu)
         * changing the verification method to be flexible
         * if there is multiple agent the count will be more than 1 (1*(no.of agent))
         * based on the count printed, one can identify the number of agent reporting
         */
        if(count >= 1){
            log.info(
                "Expected number of Thread match, dumps are being collected as expected, total "
                    + "collection from agents are: {}",
                count);
        }else{
           log.error("Please check if the agent is running and thread dump app has been accessed properly, error could be that the expected TD was not collected or Agent/EM got disconnected");
           throw new Exception("PLEASE CHECK IF THE APPLICATION IS RUNNING AND THE THREADS ARE ALIVE, THERE IS SOME DISCREPENCY IN TD COLLECTION");
        }
    }

    @Override
    @ExposeMethod(description = "Executes JDBC queries (count) on EM via IntroscopeDriver")
    public List<Integer> executeJdbc(String... queries) {
        return executeJdbc("localhost", DEFAULT_EM_PORT, "Admin", "", queries);
    }

    @Override
    @ExposeMethod(description = "Executes JDBC queries (count) on EM via IntroscopeDriver")
    public List<Integer> executeJdbc(final String emHost, final Integer emPort, final String user, final String password, final String... queries) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Integer>> future;
        try {
            future = executor.submit(new Callable<List<Integer>>() {
                @Override
                public List<Integer> call() throws Exception {
                    List<Integer> list = executeJdbcInternal(emHost, emPort, user, password, queries);
                    log.info("Operation executeJdbcInternal finished");
                    return list;
                }});
        } catch (RejectedExecutionException e) {
            log.warn("Unable to execute operation executeJdbcInternal in another thread");
            return executeJdbcInternal(emHost, emPort, user, password, queries);
        }

        try {
            return future.get(JDBC_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.info("Operation executeJdbcInternal has timeouted - returning null");
            future.cancel(true);
            return null;
        } catch (InterruptedException e) {
            log.info("Operation executeJdbcInternal - thread interrupted");
        } catch (ExecutionException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Cannot execute JDBC query: {0}");
        } finally {
            executor.shutdownNow();
        }
        return null;
    }

    private List<Integer> executeJdbcInternal(String emHost, Integer emPort, String user, String password, String... queries) {
        log.debug("emHost   = {}", emHost);
        log.debug("emPort   = {}", emPort);
        log.debug("user     = {}", user);
        log.debug("password = {}", password);
        log.debug("queries  = {}", Arrays.asList(queries));

        List<Integer> counts = new ArrayList<>(queries.length);
        IntroscopeDriverDBAccessHelper dbAccessHelper = null;
        try {
            EmPluginConfiguration cfg = configurationManager.loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
            URL jarUrl = Paths.get(cfg.getCurrentInstallDir(), INTROSCOPE_JDBC_JAR).toAbsolutePath().toUri().toURL();
            log.debug("jarUrl = {}", jarUrl);

            if (emPort == null) {
                emPort = cfg.getCurrentPort() == null ? cfg.getDefaultEmPort() : cfg.getCurrentPort();
                log.debug("using EM port {}", emPort);
            }

            dbAccessHelper = new IntroscopeDriverDBAccessHelper(jarUrl);

            String url = MessageFormat.format(INTROSCOPE_DRIVER_URL, user, (password == null ? "" : password), emHost, String.valueOf(emPort));
            log.debug("url = {}", url);
            dbAccessHelper.setUpConnection(url);

            for (String query : queries) {
                int count = dbAccessHelper.getCount(query);
                counts.add(count);
            }
            return counts;
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Cannot execute JDBC query: {0}");
        } finally {
            if (dbAccessHelper != null) {
                try {
                    dbAccessHelper.close();
                } catch (Exception e) {
                }
            }
            log.debug("executeJdbcInternal():: exit");
        }
    }

    private void deleteFile(File f) {
        if (f == null) {
            return;
        }
        try {
            f.delete();
        } catch (Exception e) {
            // ignore
        }
    }

    private void deleteDirectory(File dir) {
        if (dir == null) {
            return;
        }
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(dir);
        } catch (Exception e) {
            // ignore
        }
    }

    protected EmPluginConfiguration getConfiguration() {
        EmPluginConfiguration cfg = configurationManager
            .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        return cfg;
    }

    private void saveConfiguration(EmPluginConfiguration cfg) {
        configurationManager.savePluginConfiguration(PLUGIN, cfg);
    }

    private static File createTempFile() throws IOException {
        File tempFile = File.createTempFile("clw_", ".txt", new File(System.getProperty("user.dir")));
        tempFile.deleteOnExit();
        return tempFile;
    }

}
