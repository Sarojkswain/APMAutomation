package com.ca.apm.systemtest.fld.plugin.dotnet;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.IisUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.ArtifactoryDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.W32Service;
import com.sun.jna.platform.win32.W32ServiceManager;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Winsvc;

/**
 * Plugin to install and uninstall APM's .NET agent.
 *
 * @author haiva01
 */
public class DotNetPluginImpl extends AbstractPluginImpl implements DotNetPlugin {
    private static final Logger log = LoggerFactory.getLogger(DotNetPluginImpl.class);

    private static final String KEY_BITNESS = "bitness";
    private static final String TRUSS_URL =
        "${" + ArtifactManager.KEY_REPO_BASE + "}/${" + ArtifactManager.KEY_CODE_NAME + "}/build-${"
            + ArtifactManager.KEY_BUILD_NUMBER + "}(${" + ArtifactManager.KEY_BUILD_ID
            + "})/IntroscopeDotNetAgentInstall${" + KEY_BITNESS + "}_${"
            + ArtifactManager.KEY_BUILD_ID
            + "}.exe";
    private static final String DEFAULT_ARTIFACTORY_URL = "http://oerth-scx.ca"
        + ".com:8081/artifactory/repo";
    private static final String DOTNET_INSTALLER_ARTIFACT_GLOB = "dotnet-agent-installer*.*";
    private static final String DOTNET_INSTALLER_EXE_GLOB = "IntroscopeDotNetAgentInstall*.exe";
    private static final String DOTNET_INSTALLER_GROUP_ID = "com.ca.apm.delivery";
    private static final String DOTNET_INSTALLER_ARTIFACT_ID = "dotnet-agent-installer";
    private static final String DOTNET_INSTALLER_TYPE = "zip";
    private static final String DOTNET_AGENT_SERVICE_NAME = "PerfMonCollectorAgent";
    private static final Pattern IIS_WP_LIST_PATTERN
        = Pattern.compile("\\S+\\s+\"(\\d+)\"\\s+(.*)");
    @Autowired
    TrussDownloadMethod trussDm;
    @Autowired
    ArtifactoryDownloadMethod artifactoryDm;
    @ExposeAttribute(description = "Agent installation prefix.")
    File installPrefix = new File("C:\\apm-dotnet-agent");
    @ExposeAttribute(description = "Agent installation executable.")
    File installerExe;
    @ExposeAttribute(description = "Agent installer .zip artifact.")
    File installerZipArtifact;

    public DotNetPluginImpl() {
    }

    private static String stripDoubleQuotes(String str) {
        if (str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }

    static Pair<String, String> parseIisWorkersListLine(CharSequence line) {
        Matcher matcher = IIS_WP_LIST_PATTERN.matcher(line);
        if (matcher.matches()) {
            String pid = matcher.group(1);
            String label = matcher.group(2);
            return new ImmutablePair<>(pid, label);
        } else {
            return null;
        }
    }

    @ExposeMethod(description = "Stop IIS.")
    @Override
    public void stopIis() {
        int exitCode = IisUtils.stop();
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "IIS failed to stop. Command has returned with exit code {0}.", exitCode);
        }
    }

    @ExposeMethod(description = "Start IIS.")
    @Override
    public void startIis() {
        int exitCode = IisUtils.start();
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "IIS failed to start. Command has returned with exit code {0}.", exitCode);
        }
    }

    /**
     * Query Windows registry for APM .NET agent installation directory.
     *
     * @return installed agent directory
     */
    public String getInstalledAgentDirectory() {
        try {
            String dir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Wow6432Node\\ComputerAssociates\\Introscope\\NETAgent",
                "USER_INSTALL_DIR");
            log.info("Detected APM .NET agent installed to {}", dir);
            return dir;
        } catch (Win32Exception e) {
            log.info("Failed to get installed agent directory. Agent is possibly not installed.");
            log.debug("Exception.", e);
            return null;
        }
    }

    /**
     * Query Windows registry for APM .NET agent installation directory.
     *
     * @return installed agent application name
     */
    public String getInstalledAgentAppName() {
        try {
            // HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\ComputerAssociates\Introscope\NETAgent
            String productName = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Wow6432Node\\ComputerAssociates\\Introscope\\NETAgent", "PRODUCT_NAME");
            log.info("Detected APM .NET agent installed as {}", productName);
            return productName;
        } catch (Win32Exception e) {
            log.info(
                "Failed to get installed agent application name. Agent is possibly not installed.");
            log.debug("Exception.", e);
            return null;
        }
    }

    @ExposeMethod(description = "Delete .NET agent directory.")
    @Override
    public boolean uninstallAgent() {
        // XXX: HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\ComputerAssociates\Introscope\NETAgent
        // XXX: Delete this registry key and already installed files if the installer
        // XXX: refuses to uninstall.

        // Query registry to see if the agent is installed at all.
        String agentDir = getInstalledAgentDirectory();
        String agentAppName = getInstalledAgentAppName();
        if (agentDir == null || agentAppName == null) {
            log.info("APM .NET agent not installed.");
            return false;
        }

        Path wmicPath = Paths.get(System.getenv("windir"), "system32", "wbem", "wmic.exe");

        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command(wmicPath.toAbsolutePath().toString(), "product", "where",
                String.format("name='%s'", agentAppName), "call", "uninstall", "/nointeractive");
        StartedProcess process;
        try {
            process = pe.start();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to start APM .NET agent uninstaller. Exception: {0}");
        }
        int exitCode = ProcessUtils2.waitForProcess(process, 15, TimeUnit.MINUTES, false);
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                ".NET agent uninstaller command has returned with exit code {0}.", exitCode);
        }

        return true;
    }

    @ExposeMethod(description = "Create install prefix directory.")
    @Override
    public String makeInstallPrefix() {
        try {
            FileUtils.forceMkdir(installPrefix);
            return installPrefix.getAbsolutePath();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create install prefix {1}. Exception: {0}", installPrefix
                    .getAbsoluteFile());
        }
    }

    @ExposeMethod(description = "Get install prefix directory.")
    @Override
    public String getInstallPrefix() {
        try {
            return installPrefix.getAbsolutePath();
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to get install prefix. Exception: {0}");
        }
    }


    @ExposeMethod(description = "Delete .NET agent directory.")
    @Override
    public void deleteAgentDirectory() {
        File dir = new File(installPrefix, "dotnet-agent");
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to delete directory {1}. Exception: {0}", dir);
        }
    }

    @ExposeMethod(description = "Fetch installer artifact from TRUSS.")
    @Override
    public String fetchInstallerArtifactFromTruss(String repoBase, String codeName,
        String buildNumber, String buildId,
        String bitness) {
        ACFileUtils.forceDeleteFilesByGlob(installPrefix, DOTNET_INSTALLER_EXE_GLOB);


        // http://truss.ca.com/builds/InternalBuilds/9.7.0-NET/build-000067(9.7.0.0)/
        // IntroscopeDotNetAgentInstall64_9.7.0.0.exe
        Map<String, Object> props = new TreeMap<String, Object>();
        props.put(ArtifactManager.KEY_REPO_BASE, repoBase);
        props.put(ArtifactManager.KEY_CODE_NAME, codeName);
        props.put(ArtifactManager.KEY_BUILD_NUMBER, buildNumber);
        props.put(ArtifactManager.KEY_BUILD_ID, buildId);
        props.put(KEY_BITNESS, bitness);
        try {
            ArtifactFetchResult fetchRes = trussDm
                .fetch(TRUSS_URL, installPrefix, props, true); //TODO - DM - crappy x64 Bits!!!
            installerExe = fetchRes.getFile();
            log.info("Successfully fetched from TRUSS into {}.", installerExe);
            return installerExe.getAbsolutePath();
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to fetch artifact from artifactory. Exception {0}");
        }
    }

    /**
     * Fetches .NET agent installer from artifactory.
     * <p>
     * <p>
     * Typical Maven coordinates will be these:
     * <pre>
     *     <dependency>
     *         <groupId>com.ca.apm.delivery</groupId>
     *         <artifactId>dotnet-agent-installer</artifactId>
     *         <version>99.99.sys</version>
     *         <classifier>64</classifier>
     *         <type>zip</type>
     *      </dependency>
     * </pre>
     * <p>
     * <p>
     * Group ID, artifact ID and type are implicitly set.
     *
     * @param version
     * @param classifier
     * @param artifactoryBaseUrl
     */
    @ExposeMethod(description = "Fetch .NET installer artifact from artifactory.")
    @Override
    public String fetchInstallerArtifactFromArtifactory(String version, String classifier,
        String artifactoryBaseUrl) {

        if (log.isDebugEnabled()) {
            log.debug("Artifactory URL: {}", artifactoryBaseUrl);
        }

        ACFileUtils.forceDeleteFilesByGlob(installPrefix, DOTNET_INSTALLER_ARTIFACT_GLOB);

        String url = artifactoryBaseUrl != null ? artifactoryBaseUrl : DEFAULT_ARTIFACTORY_URL;

        ArtifactFetchResult fetchRes = artifactoryDm.fetchTempArtifact(url,
            DOTNET_INSTALLER_GROUP_ID, DOTNET_INSTALLER_ARTIFACT_ID,
            version, classifier, DOTNET_INSTALLER_TYPE);

        installerZipArtifact = fetchRes.getFile().getAbsoluteFile();

        return installerZipArtifact.getAbsolutePath();
    }

    /**
     * Searches given directory for .NET agent installer executable.
     *
     * @param dir directory to search for installer executable
     */
    private void setInstallerPath(File dir) {
        Collection<File> files = ACFileUtils.globFiles(dir, DOTNET_INSTALLER_EXE_GLOB);
        if (files.size() != 1) {
            if (log.isDebugEnabled()) {
                log.debug("All found files for pattern {}: {}",
                    DOTNET_INSTALLER_EXE_GLOB, files);
            }
            throw ErrorUtils.logErrorAndReturnException(log,
                "Expected exactly one file matching glob pattern {0} but got {1} files.",
                DOTNET_INSTALLER_EXE_GLOB, files.size());
        }

        installerExe = files.iterator().next();
    }

    @ExposeMethod(description = "Extracts installer artifact from given zip file.")
    @Override
    public String unzipInstallerArtifact() {
        ACFileUtils.forceDeleteFilesByGlob(installPrefix, DOTNET_INSTALLER_EXE_GLOB);

        try {
            ZipFile artifactZip = new ZipFile(installerZipArtifact);
            artifactZip.extractAll(installPrefix.getAbsolutePath());
            log.info("Extracted ZIP file {}.", installerZipArtifact.getAbsolutePath());
        } catch (ZipException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create extract artifact {1}. Exception: {0}",
                installerZipArtifact);
        }

        setInstallerPath(installPrefix);
        return installerExe.getAbsolutePath();
    }

    @ExposeMethod(description = "Run .NET agent installation")
    @Override
    public void installAgent(String emHost, int emPort, Configuration config) {
        // %~3 /s /v"/qn INSTALLDIR=\"%~4\" EMHOST=%~5 EMPORT=%~6 ENABLESOA=1"
        StringBuffer sb = new StringBuffer(1024);
        sb.append("\"/v/qn INSTALLDIR=\\\"" + installPrefix + "\\agent\\\""
            //+ " ISDEBUGLOG=\\\"" + installerExe.getAbsolutePath() + ".log\\\""
            + " EMHOST=" + emHost + " EMPORT=" + emPort);
        if (config != null) {
            // PROFILERTYPE=%PROFILERTYPEx% ENABLESOA=%ENABLESOAx% ENABLECD=%ENABLECDx%
            // CDAGENTID=%CDAGENTIDx% ENABLESPP=%ENABLESPPx% INSTALLSPMONITOR=%INSTALLSPMONITORx%"
            if (config.profilerType != null) {
                sb.append(" PROFILERTYPE=");
                sb.append(config.profilerType);
            }
            if (config.enableSoa) {
                sb.append(" ENABLESOA=1");
            }
            if (config.enableCd) {
                sb.append(" ENABLECD=1");
            }
            if (config.cdAgentId != null) {
                sb.append(" CDAGENTID=");
                sb.append(config.cdAgentId);
            }
            if (config.enableSpp) {
                sb.append(" ENABLESPP=1");
            }
            if (config.installSpMonitor) {
                sb.append(" INSTALLSPMONITOR=1");
            }
        }
        sb.append("\"");

        ProcessExecutor pb = ProcessUtils2.newProcessExecutor()
            .command(installerExe.getAbsolutePath(), "/s", sb.toString());
        StartedProcess process = ProcessUtils2.startProcess(pb);
        int exitCode = ProcessUtils2.waitForProcess(process, 15, TimeUnit.MINUTES, false);
        switch (exitCode) {
            case 1618:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "Installer exit code {0,number,#} says that another installation is already "
                        + "running.",
                    exitCode);

            case 1603:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "Installer exit code {0,number,#} says that the product is already installed.",
                    exitCode);

            case 0:
                log.info("Installer succeeded.");
                return;

            default:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "Installer returned with non-zero exit code: {0,number,#}", exitCode);
        }
    }

    @ExposeMethod(description = "Check .NET agent service state")
    @Override
    public AgentCheckResult checkAgent() {
        W32ServiceManager sm = new W32ServiceManager();
        sm.open(Winsvc.SC_MANAGER_CONNECT);
        try {
            W32Service service = sm.openService(DOTNET_AGENT_SERVICE_NAME,
                Winsvc.SERVICE_QUERY_STATUS);
            try {
                Winsvc.SERVICE_STATUS_PROCESS status = service.queryStatus();
                log.info(".NET agent service status is {}.", status.dwCurrentState);
                if (status.dwCurrentState == Winsvc.SERVICE_RUNNING) {
                    return AgentCheckResult.RUNNING;
                } else {
                    return AgentCheckResult.NOT_RUNNING;
                }
            } catch (Win32Exception e) {
                log.warn("Got exception querying service {}. Exception: {}",
                    DOTNET_AGENT_SERVICE_NAME, e.getMessage());
                return AgentCheckResult.NOT_RUNNING;
            } finally {
                service.close();
            }
        } catch (Win32Exception e) {
            log.warn("Got exception opening service {}. Exception: {}",
                DOTNET_AGENT_SERVICE_NAME, e.getMessage());
            return AgentCheckResult.NOT_INSTALLED;
        } finally {
            sm.close();
        }
    }

    @ExposeMethod(description = "Find IIS worker processes and their PIDs.")
    @Override
    public Map<String, String> getIisWorkers() throws ExecutionException, InterruptedException,
        IOException {
        Path workDir = Paths.get(System.getenv("windir"), "System32", "inetsrv");
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .directory(workDir.toFile())
            .readOutput(true)
            .command(workDir.resolve("appcmd.exe").toAbsolutePath().toString(), "list", "wp");

        StartedProcess process = ProcessUtils2.startProcess(pe);
        Future<ProcessResult> future = process.getFuture();
        int retval = ProcessUtils2.waitForProcess(process, 1, TimeUnit.MINUTES, true);
        if (retval != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "Failed to retrieve IIS worker processes with 'appcmd list wp'. Appcmd returned "
                    + "with exit code {0,number,#}",
                retval);
        }

        ProcessResult pr = future.get();
        String output = pr.outputString();
        Map<String, String> workers = new HashMap<>(10);
        for (String line : IOUtils.readLines(new StringReader(output))) {
            Pair<String, String> pair = parseIisWorkersListLine(line);
            if (pair != null) {
                workers.put(pair.getKey(), pair.getValue());
            }
        }

        return workers;
    }
}
