package com.ca.apm.systemtest.fld.plugin.tim;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.LinuxUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.cm.ConfigurationManager;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryLiteDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.google.common.collect.Lists;


/**
 * Plugin to install and uninstall APM TIMs.
 *
 * @author haiva01
 */
public class TimPluginImpl extends AbstractPluginImpl implements TimPlugin, InitializingBean {
    public static final String TIM_PLUGIN = "timPlugin";

    private static final Logger log = LoggerFactory.getLogger(TimPluginImpl.class);

    private static final String TRUSS_TIM_URL =
        "${" + ArtifactManager.KEY_REPO_BASE + "}/${" + ArtifactManager.KEY_CODE_NAME + "}/build-${"
        + ArtifactManager.KEY_BUILD_NUMBER + "}(${" + ArtifactManager.KEY_BUILD_ID + "})/${"
        + ArtifactManager.KEY_FILE_NAME + "}";
    private static final String TRUSS_PUBKEY_URL =
        "${" + ArtifactManager.KEY_REPO_BASE + "}/${" + ArtifactManager.KEY_CODE_NAME + "}/build-${"
        + ArtifactManager.KEY_BUILD_NUMBER + "}(${" + ArtifactManager.KEY_BUILD_ID + "})/${"
        + ArtifactManager.KEY_FILE_NAME + "}";
    private static final String DEFAULT_ARTIFACTORY_URL
        = "http://oerth-scx.ca.com:8081/artifactory/repo";
    private static final String EULA_FILE = "ca-eula.en.txt";
    private static final String TIM_TAR_INSTALLER_GLOB = "tim-*-install.tar.gz";
    private static final String TIM_UNINSTALL_SCRIPT = "timUninstall.sh";
    private static final String TIM_UNINSTALL_SCRIPT_DIR = "/CA/APM/tim/uninstall/";
    private static final String TIM_QA_AUTOMATION_PATH =
        "cgi-bin/ca/apm/tim-qa-automation/TimQaWebServices";
    private static final int DEFAULT_HTTPS_PORT = 8443;
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_TIM_PORT = 81;
    private static final int DEFAULT_PRIVATE_HTTPD_PORT = 80;
    private static final String CA_AUTOMATION_FILE = "/etc/CA_AUTOMATION";

    //    private String monitoredNetworkInterface = "eth0";
//    private String controllNetworkInterface = "eth1";
//    private File installerArtefactPath
//        = new File ("/root/tim-rhel6-dist-99.99.sys-20141002.140629-22-Linux-el6-x64.zip");
    private String extractorName = "./timInstall.bin";
    private int timPort = DEFAULT_TIM_PORT;
    private int privateHttpd = DEFAULT_PRIVATE_HTTPD_PORT;
    private int httpPort = DEFAULT_HTTP_PORT;
    private int httpsPort = DEFAULT_HTTPS_PORT;
    private File tempDir;
    private File installerTarArtifact;
    private File installerZipArtifact;

    @ExposeAttribute(description = "fld-scripts working directory")
    private String fldScriptsWorkDir;

    @Autowired
    ArtifactoryDownloadMethod artifactoryDm;

    @Autowired
    TrussDownloadMethod trussDm;

    @Autowired
    ArtifactoryLiteDownloadMethod artifactoryLiteDm;

    @Autowired
    private ConfigurationManager configurationManager;
    
    private InstallStatus installStatus = InstallStatus.None;


    public TimPluginImpl() {
        setFldScriptsWorkDir("./fld-scripts");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        readConfiguration();
    }
    
    private TimPluginConfiguration readConfiguration() {
        TimPluginConfiguration config = configurationManager.loadPluginConfiguration(TIM_PLUGIN, TimPluginConfiguration.class);
        return config;
    }
    
    
    @ExposeMethod(description = "Starts the TIM installation.")
    @Override
    public void startInstall(final String trussBaseUrl, final String timCodeName, final String timBuildNumber, 
                             final String timBuildId, final String timFilename) {
        Runnable r = new Runnable() {
            
            @Override
            public void run() {
                try {
                    runInstall(trussBaseUrl, timCodeName, timBuildNumber, timBuildId, timFilename);
                } catch (Exception e) {
                    installStatus = InstallStatus.Error;
                    TimPluginConfiguration config = configurationManager.loadPluginConfiguration(TIM_PLUGIN, TimPluginConfiguration.class);
                    config.setInstalled(false);
                    configurationManager.savePluginConfiguration(TIM_PLUGIN, config);
                }
            }
        };
        Thread th = new Thread(r);
        th.start();
    }
    
    
    private void runInstall(String trussBaseUrl, String timCodeName, String timBuildNumber, String timBuildId, String timFilename) {
        installStatus = InstallStatus.Installing;
        try {
            TimPluginConfiguration config = readConfiguration();
            
            if (config.isInstalled()) {
                timUninstall(config.getCurrentInstallDirectory());
            }
            
            createTempDir();
            
            turnOffFirewall();
            
            stopHttpd();
            
            fetchInstallerArtifactFromTruss(trussBaseUrl, timCodeName, timBuildNumber, timBuildId, timFilename);
            
            extractInstallerTarGz();
            
            acceptEula();
            
            extractRpmsFromInstaller();
            
            createPrefixDir(config.getDefaultInstallDirectory());
            
            installExtractedRpms(config.getDefaultInstallDirectory());
            
            restartHttpd();
            
            configureInterface("eth2");
            
            setTimSetting("MaxFlexRequestBodySize", "100000");
            setTimSetting("MaxFlexResponseBodySize", "100000");
            
            installStatus = InstallStatus.Installed;
            config.setInstalled(true);
            config.setCurrentInstallDirectory(config.getDefaultInstallDirectory());
            configurationManager.savePluginConfiguration(TIM_PLUGIN, config);
        } finally {
            deleteTempDir();
        }
    }
    
    
    @ExposeMethod(description = "Checks the TIM installation status.")
    @Override
    public InstallStatus checkInstallStatus() {
        return installStatus;
    }
    

    public void createPrefixDir(final String prefix) {
        File prefixPath = new File(prefix);
        try {
            FileUtils.forceMkdir(prefixPath);
            prefixPath.setWritable(true, true);
            log.info("Created working directory {}.", prefixPath.getAbsolutePath());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create directory {1}. Exception: {0}",
                prefixPath);
        }
    }


    public void createTempDir() {
        try {
            setTempDir(Files.createTempDirectory("tim-plugin").toFile());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Failed to create temporary directory. Exception: {0}");
        }

    }


    public void unzipInstallerArtifact() {
        try {
            ZipFile artifactZip = new ZipFile(installerZipArtifact);
            artifactZip.extractAll(getTempDir().getAbsolutePath());
            log.info("Extracted ZIP file {}.", installerZipArtifact.getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                installerZipArtifact);
        }
    }


    public void extractRpmsFromInstaller() {
        // Start RPM extractor script.

        final ProcessBuilder pb = ProcessUtils.newProcessBuilder()
            .command(extractorName)
            .directory(getTempDir());

        int rpmExtractionExitCode;
        final File caAutomationFile = new File(CA_AUTOMATION_FILE);
        try {
            // This file is needed for unattended installation to work.
            log.info("Creating {} to enable unattended installation.",
                caAutomationFile.getAbsolutePath());
            FileUtils.touch(caAutomationFile);

            Process process = ProcessUtils.startProcess(pb);
            rpmExtractionExitCode = ProcessUtils
                .waitForProcess(process, 2, TimeUnit.MINUTES, true);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Failure during RPM installation.");
        } finally {
            log.info("Deleting {}.", caAutomationFile.getAbsolutePath());
            boolean success = FileUtils.deleteQuietly(caAutomationFile);
            if (! success) {
                log.warn("Failed to delete {}", caAutomationFile.getAbsolutePath());
            }
        }

        log.info("RPM extraction process ended with exit code {}.",
            rpmExtractionExitCode);
        if (rpmExtractionExitCode != 0) {
            ErrorUtils.throwRuntimeException("Failed to extract RPMs.");
        }
    }


    public void installExtractedRpms(String prefix) {
        final ProcessBuilder pb = ProcessUtils.newProcessBuilder()
            .directory(getTempDir());

        Map<String, String> env = pb.environment();
        env.put("TIM_PORT", Integer.toString(timPort));
        env.put("PRIVATE_HTTPD", Integer.toString(privateHttpd));
        env.put("HTTP_PORT", Integer.toString(httpPort));
        env.put("HTTPS_PORT", Integer.toString(httpsPort));

        File prefixPath = new File(prefix);
        List<String> command = Lists.newArrayList(
            "rpm", "-i", "--prefix=" + prefixPath.getAbsolutePath());
        for (Iterator<File> it
                 = FileUtils.iterateFiles(getTempDir(), new String[]{"rpm"}, false);
             it.hasNext(); ) {
            command.add(it.next().getAbsolutePath());
        }
        pb.command(command);

        final File caAutomationFile = new File(CA_AUTOMATION_FILE);
        Process rpmProcess;
        int rpmExitCode;
        try {
            // This file is needed for unattended installation to work.
            log.info("Creating {} to enable unattended installation.",
                caAutomationFile.getAbsolutePath());
            FileUtils.touch(caAutomationFile);

            rpmProcess = ProcessUtils.startProcess(pb);
            rpmExitCode = ProcessUtils.waitForProcess(rpmProcess, 5, TimeUnit.MINUTES, true);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Failure during RPM installation.");
        } finally {
            log.info("Deleting {}.", caAutomationFile.getAbsolutePath());
            boolean success = FileUtils.deleteQuietly(caAutomationFile);
            if (! success) {
                log.warn("Failed to delete {}", caAutomationFile.getAbsolutePath());
            }
        }

        log.info("RPM installation process ended with exit code {}.",
            rpmExitCode);
        if (rpmExitCode != 0) {
            ErrorUtils.throwRuntimeException("Failed to install TIM. RPM exit code {0}.",
                rpmExitCode);
        }
    }

    public void restartHttpd() {
        int exitCode = LinuxUtils.serviceOperation("httpd", LinuxUtils.ServiceOp.RESTART);
        if (exitCode != 0) {
            ErrorUtils.throwRuntimeException("Failed to restart httpd. Exit code {0}.", exitCode);
        }
    }


    public void stopHttpd() {
        int exitCode = LinuxUtils.serviceOperation("httpd", LinuxUtils.ServiceOp.STOP);
        if (exitCode != 0) {
            log.warn("Possible problems stopping httpd. Exit code {}.", exitCode);
        }
    }


    public void turnOffFirewall() {
        int exitCode = LinuxUtils.turnOffFirewall();
        if (exitCode != 0) {
            log.warn("Possible problems turning off firewall. Exit code {}.", exitCode);
        }
    }


    public void timStop() {
        int exitCode = LinuxUtils.serviceOperation("tim", LinuxUtils.ServiceOp.STOP);
        if (exitCode != 0) {
            log.warn("Possible problems stopping TIM. Exit code {}.", exitCode);
        }
    }


    public void timStart() {
        int exitCode = LinuxUtils.serviceOperation("tim", LinuxUtils.ServiceOp.START);
        if (exitCode != 0) {
            log.warn("Possible problems starting TIM. Exit code {}.", exitCode);
        }
    }


    public void acceptEula() {
        Path eulaPath = Paths.get(getTempDir().getAbsolutePath(), EULA_FILE);
        String contents;
        try {
            contents = new String(Files.readAllBytes(eulaPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to read file license file {1}. Exception: {0}.",
                eulaPath.toFile().getAbsoluteFile());
        }

        contents = contents.replace("CA-EULA=reject", "CA-EULA=accept");

        try {
            Files.write(eulaPath, contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to write modified license file {1}. Exception: {0}",
                eulaPath.toFile().getAbsoluteFile());
        }
    }


    public void extractInstallerTarGz() {
        File dir = getTempDir();
        File[] files = dir.listFiles(
            (FilenameFilter) new WildcardFileFilter(TIM_TAR_INSTALLER_GLOB));
        if (files.length != 1) {
            ErrorUtils.throwRuntimeException("Expected 1 TAR file, found {0}", files.length);
        }

        File tarFile = files[0];
        ProcessBuilder pb = ProcessUtils.newProcessBuilder()
            .command("tar", "zxvf", tarFile.getAbsolutePath())
            .directory(dir);

        Process process = ProcessUtils.startProcess(pb);
        int exitCode = ProcessUtils.waitForProcess(process, 2, TimeUnit.MINUTES, true);
        if (exitCode != 0) {
            ErrorUtils.throwRuntimeException("TAR returned exit code {0}", exitCode);
        }
    }


    public void fetchInstallerArtifactFromTruss(String repoBase, String codeName,
                                                String buildNumber, String buildId, String
        fileName) {
        final File dir = getTempDir();

        // http://truss/builds/InternalBuilds/10.0.0_APM_Release.TIM_RedHat_6.0_x64/build-990010(10.0.0.12)/tim-10.0.0.12.990010-d94e67a6353f4f79348c91e695431d5e6f9484d2.Linux.el6.x86_64-install.tar.gz
        
        // http://truss.ca.com/builds/InternalBuilds/9.7.0_APM_Release.TIM_RedHat_6.0_x64/
        //   build-000023(9.7.0.23)/tim-9.7.0.23.23-15a55a207a24783729f6a335f8492376d67d1240
        // .Linux.el6.x86_64-install.tar.gz
        Map<String, Object> props = new TreeMap<>();
        props.put(ArtifactManager.KEY_REPO_BASE, repoBase);
        props.put(ArtifactManager.KEY_CODE_NAME, codeName);
        props.put(ArtifactManager.KEY_BUILD_NUMBER, buildNumber);
        props.put(ArtifactManager.KEY_BUILD_ID, buildId);
        props.put(ArtifactManager.KEY_FILE_NAME, fileName);
        try {
            ArtifactFetchResult fetchRes = trussDm.fetch(TRUSS_TIM_URL, dir, props, true);
            installerTarArtifact = fetchRes.getFile();
            log.info("Successfully fetched from TRUSS into {}.", installerTarArtifact);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Failed to fetch TIM installer "
                                                            + "artifact. Exception: {0}");
        }
        
        props.put(ArtifactManager.KEY_FILE_NAME, "CA-APM-TIM-public_key.txt");
        try {
            ArtifactFetchResult fetchRes = trussDm.fetch(TRUSS_PUBKEY_URL, dir, props, true);
            File pubKeyFile = fetchRes.getFile();
            log.info("Successfully fetched from TRUSS into {}.", pubKeyFile);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Failed to fetch TIM installer "
                                                            + "artifact. Exception: {0}");
        }
    }

    public void fetchInstallerArtifactFromArtifactory(String groupId, String artifactId,
                                                      String version, String classifier, String
        type,
                                                      String artifactoryBaseUrl) {
        if (log.isDebugEnabled()) {
            log.debug("Artifactory URL: {}", artifactoryBaseUrl);
        }

        ArtifactFetchResult fetchRes =
                artifactoryDm.fetchTempArtifact(
                        DEFAULT_ARTIFACTORY_URL,
                        groupId,
                        artifactId,
                        version,
                        classifier, type);

        installerZipArtifact = fetchRes.getFile();
    }


    @ExposeMethod(description = "Uninstall TIM installed in given prefix.")
    @Override
    public void timUninstall(String prefix) {
        //final File prefixPath = new File(prefix);

        Path file = com.ca.apm.systemtest.fld.common.files.FileUtils.search(Paths.get(prefix+TIM_UNINSTALL_SCRIPT_DIR), TIM_UNINSTALL_SCRIPT);
        if (file != null) {
            ProcessBuilder pb = ProcessUtils.newProcessBuilder()
                .command(file.toFile().getAbsolutePath())
                ;
    
            final Process process = ProcessUtils.startProcess(pb);
//        final InputStream in = process.getInputStream();
//        final OutputStream out = process.getOutputStream();
        
        /*

CA EULA File Path and Filename (DEFAULT: /root/stage/ca-eula.en.txt):
ENTER AN ABSOLUTE PATH, OR PRESS <ENTER> TO ACCEPT THE DEFAULT
      :
The installation path is:  /opt
    Is this correct? (Y/N, default: Y):
ENTER THE NUMBER OF THE DESIRED CHOICE, OR PRESS <ENTER> TO ACCEPT THE
   DEFAULT:
ENTER A VALID PORT NUMBER, OR PRESS <ENTER> TO ACCEPT THE DEFAULT
      :
ENTER THE NUMBER OF THE DESIRED CHOICE, OR PRESS <ENTER> TO ACCEPT THE
   DEFAULT:
         */
        // FIXME - switch to a different library like expectj to run the shell script
//        ProcessScriptRunner scriptRunner = new ProcessScriptRunner(in, null, out);
//        scriptRunner.waitFor("PRESS <ENTER> TO CONTINUE:", 120000L)
//            .send("")
//            .waitFor("ENTER AN ABSOLUTE PATH, OR PRESS <ENTER> TO ACCEPT THE DEFAULT", 60000L)
//            .waitFor("", 10000L);
//        ;
        
            int exitCode = ProcessUtils.waitForProcess(process, 5, TimeUnit.MINUTES, true);
            if (exitCode != 0) {
                ErrorUtils.throwRuntimeException("TIM uninstaller returned with exit code {0}.",
                    exitCode);
            }
            TimPluginConfiguration config = configurationManager.loadPluginConfiguration(TIM_PLUGIN, TimPluginConfiguration.class);
            config.setInstalled(false);
            configurationManager.savePluginConfiguration(TIM_PLUGIN, config);
        }
    }

    /**
     * Get temporary directory File.
     * @return temporary directory as File
     */
    public File getTempDir() {
        if (tempDir == null) {
            createTempDir();
        }

        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }


    public void deleteTempDir() {
        File tempDir = null;
        try {
            tempDir = getTempDir();
            FileUtils.deleteDirectory(getTempDir());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to delete directory {1}. Exception: {0}", tempDir);
        }
    }


    private String getOwnHostname() {
        String ownHostname;
        try {
            ownHostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Failed to get own host name. "
                                                            + "Exception: {0}");
        }
        return ownHostname;
    }


    public void setTimSetting(String setting, String value) {
        String ownHostname = getOwnHostname();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String qaUrl = "http://" + ownHostname + "/" + TIM_QA_AUTOMATION_PATH;
            HttpGet req = new HttpGet(new URI(qaUrl));
            req.setHeader("X-TIM-QA-Protocol-Version", "1");
            req.setHeader("X-TIM-QA-Request-Type", "setDatabaseSetting");
            req.setHeader("X-Setting-Name", setting);
            req.setHeader("X-Setting-Value", value);
            log.info("Setting TIM setting {} to value {}.", setting, value);
            try (CloseableHttpResponse resp = httpclient.execute(req)) {
                log.info("HTTP response status line: {}", resp.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception during interface configuration. Exception: {0}");
        } catch (URISyntaxException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception when building QA API URL. Exception: {0}");
        }
    }


    public void configureInterface(String networkInterface) {
        String ownHostname = getOwnHostname();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String qaUrl = "http://" + ownHostname + "/" + TIM_QA_AUTOMATION_PATH;
            HttpGetWithEntity req = new HttpGetWithEntity(new URI(qaUrl));
            req.setHeader("X-TIM-QA-Protocol-Version", "1");
            req.setHeader("X-TIM-QA-Request-Type", "setNetworkInterfaces");
            req.setEntity(new StringEntity("if=" + networkInterface));
            log.info("Setting network interface to {}.", networkInterface);
            try (CloseableHttpResponse resp = httpclient.execute(req)) {
                log.info("HTTP response status line: {}", resp.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception during interface configuration. Exception: {0}");
        } catch (URISyntaxException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Exception when building QA API URL. Exception: {0}");
        }
    }

    @Override
    public Integer executeScript(boolean waitFor, String... command) {
        String script = command[0];
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command(command).directory(Paths.get(script).getParent().toFile());
        if (waitFor) {
            int exitCode =  ProcessUtils.waitForProcess(ProcessUtils.startProcess(pb), 15, TimeUnit.SECONDS, true);
            log.info("Executed script: {}, exit code {}", script, exitCode);
            return exitCode;
        } else {
            ProcessUtils.startProcess(pb);
            log.info("Executed script: {}", script);
        }
        return null;
    }

    @Override
    public void prepareFldScripts(String groupId, String artifactId, String version, String classifier, String type) {
        ArtifactFetchResult result = artifactoryLiteDm.fetchTempArtifact(ArtifactoryLiteDownloadMethod.DEFAULT_ARTIFACTORY_URL, groupId, artifactId, version, classifier, type);
        File outputDir = new File(getFldScriptsWorkDir());
        try {
            ACFileUtils.unpackFile(result.getFile(), outputDir);
        } catch (ArchiveException | IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to unpack fetched zip file. Exception: {0}");
        }

        // TODO [TB] - this does not work (and returns exit code 1) although on command line it's ok
        // -> using java.nio.file.Files.walkFileTree() + java.io.File.setExecutable() for now
        /*
        ProcessBuilder pb = ProcessUtils.newProcessBuilder().command("find", getFldScriptsWorkDir(), "-name", "\"*.sh\"", "-type", "f", "-exec", "chmod", "u+x", "{}", " \\;");
        ProcessUtils.waitForProcess(ProcessUtils.startProcess(pb), 15, TimeUnit.SECONDS, true);
        */

        try {
            setScriptsExecutable(getFldScriptsWorkDir());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to set executable permission for fld scripts. Exception: {0}");
        }
    }

    @Override
    public void setFldScriptsWorkDir(String fldScriptsWorkDir) {
        this.fldScriptsWorkDir = (new File(fldScriptsWorkDir)).getAbsolutePath();
    }

    @Override
    public String getFldScriptsWorkDir() {
        return this.fldScriptsWorkDir;
    }

    @Override
    public boolean isScriptAvailable(String script) {
        return (new File(script)).canExecute();
    }

    protected void setScriptsExecutable(String dir) throws IOException {
        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                if (f.getName().endsWith(".sh")) {
                    f.setExecutable(true, true);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
