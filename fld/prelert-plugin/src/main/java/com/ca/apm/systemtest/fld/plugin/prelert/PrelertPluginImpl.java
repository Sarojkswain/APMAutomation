package com.ca.apm.systemtest.fld.plugin.prelert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.TrussDownloadMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManagerException;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * Plugin for Prelert.
 *
 * @author filja01
 */
public class PrelertPluginImpl extends AbstractPluginImpl implements PrelertPlugin {

    private static final Logger log = LoggerFactory.getLogger(PrelertPluginImpl.class);

    private static final String CONFIG_FILE_ENGINE = "/config/engine.xml";

    @Autowired
    TrussDownloadMethod dm;

    @Override
    @ExposeMethod(description = "Test if running and accesible")
    public boolean isServerRunning(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();

            return HttpURLConnection.HTTP_OK == urlConn.getResponseCode();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    @ExposeMethod(description = "Install prelert")
    public void install(Configuration config) {
        log.info("installing prelert");

        Path installerDir = getMSIInstallerDir(config);

        uninstall(config, installerDir);

        File installerFile = findFile(installerDir, "CAAnalysisServer*.msi");
        if (installerFile != null) {
            if (OperatingSystemFamily.Windows == config.platform) {
                String command = "msiexec /i " + installerFile.toString() + " /qn INSTALLDIR=\""
                    + config.prelertInstallDir + "\"";

                //ProcessBuilder clw =
                //    ProcessUtils
                //        .newProcessBuilder()
                //        .command(new String [] {"msiexec", "/i", installerFile.toString(),
                // "/qn", "\"INSTALLDIR="+config.prelertInstallDir+"\""})
                //        .directory(installerDir.toFile());

                try {
                    log.info("Run command: {}", command);
                    ProcessUtils
                        .waitForProcess(Runtime.getRuntime().exec(command), 5, TimeUnit.MINUTES,
                            true);
                } catch (IOException e) {
                    throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Cannot start installation");
                }
            } else {
                throw ErrorUtils.logErrorAndReturnException(log, "Linux system is not supported");
            }
        } else { // installer/unistaller msi file is expected in temp directory
            throw ErrorUtils
                .logExceptionAndWrap(log, null, "Cannot install prelert, installer is missing");
        }
        deleteFolder(installerDir);
        // set engine.xml and stop start to take the changes
        setConfigFile(config);
        stop(config);
        start(config);
    }

    private File findFile(Path directory, String pattern) {
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
                "Cannot find the installer file {0} in directory {1}.",
                pattern, directory);
        }
        return null;
    }

    private void deleteFolder(Path directory) {
        try {
            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Cannot delete folder {1}. Exception {0}", directory);
        }
    }

    void setConfigFile(Configuration config) {
        try {
            File engineXml = new File(new File(config.prelertInstallDir), CONFIG_FILE_ENGINE);

            SAXEngine builder = new SAXBuilder();
            Document doc = builder.build(engineXml);
            Element rootNode = doc.getRootElement();
            rootNode.getChild("analysisconfig").getChild("learnonlytime").setText("600");

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            try (Writer fwr = new FileWriter(engineXml)) {
                xmlOutput.output(doc, fwr);
            }
        } catch (IOException | JDOMException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot edit engine.xml file.");
        }

    }

    private File download(final Configuration config, Path installerDir) {
        // Download prelert installer
        ArtifactFetchResult installerPackage = null;

        try {
            @SuppressWarnings("serial")
            Map<String, Object> parameters = new HashMap<String, Object>() {
                {
                    put(ArtifactManager.KEY_REPO_BASE, "http://"
                        + (config.trussServer != null ? config.trussServer : "truss.ca.com")
                        + "/builds/InternalBuilds");
                    put(ArtifactManager.KEY_CODE_NAME, config.codeName);
                    put(ArtifactManager.KEY_BUILD_NUMBER, config.buildNumber);
                    put(ArtifactManager.KEY_BUILD_ID, config.buildId);
                    put(ArtifactManager.KEY_PRODUCT, "introscope");
                    put(ArtifactManager.KEY_FILE_NAME, "CAAnalysisServer");
                    put(ArtifactManager.KEY_OS_ARCHITECTURE,
                        config.platform == OperatingSystemFamily.Windows
                            ? "windowsAMD64"
                            : "linuxAMD64");
                    put(ArtifactManager.KEY_OS_ARCHIVE_EXTENSION,
                        config.platform == OperatingSystemFamily.Windows ? "zip" : "tar");
                }
            };
            String trussSpec = "http://"
                + (config.trussServer != null ? config.trussServer : "truss.ca.com")
                + "/builds/InternalBuilds/"
                + config.codeName
                + "/build-"
                + config.buildNumber
                + "("
                + config.buildId
                + ")/introscope"
                + config.buildId
                + "/CAAnalysisServer"
                + config.buildId
                + (config.platform == OperatingSystemFamily.Windows ? "windowsAMD64" : "linuxAMD64")
                + "."
                + (config.platform == OperatingSystemFamily.Windows ? "zip" : "tar");
            installerPackage = dm.fetch(trussSpec, installerDir.toFile(), parameters, true);
        } catch (ArtifactManagerException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot download installer artifact");
        }

        return installerPackage.getFile();
    }

    private void unpackFile(File file, Configuration config) {
        String unpacker = config.platform == OperatingSystemFamily.Windows ? "jar" : "tar";
        ProcessBuilder unpack =
            ProcessUtils.newProcessBuilder().command(unpacker, "xf",
                file.toString());
        unpack.directory(file.getParentFile());
        ProcessUtils.waitForProcess(ProcessUtils.startProcess(unpack), 5, TimeUnit.MINUTES, true);
    }

    @Override
    @ExposeMethod(description = "Start prelert")
    public boolean start(Configuration config) {
        Path instDir = Paths.get(config.prelertInstallDir);
        if (Files.exists(instDir, LinkOption.NOFOLLOW_LINKS)) {
            if (OperatingSystemFamily.Windows == config.platform) {
                ProcessBuilder pb = ProcessUtils.newProcessBuilder()
                    .command("cmd", "/c",
                        "\"" + config.prelertInstallDir + "/ctl/admin/prelert_startup.bat\"", "<",
                        "NUL");
                try {
                    ProcessUtils
                        .waitForProcess(ProcessUtils.startProcess(pb), 1, TimeUnit.MINUTES, true);
                } catch (RuntimeException ex) {
                    log.warn("Process killed during Starting prelert.", ex);
                }
            } else {
                throw ErrorUtils.logErrorAndReturnException(log, "Linux system is not supported");
            }
        } else {
            throw ErrorUtils.logErrorAndReturnException(log,
                "Missing Installation directory, can't start the Prelert!");
        }
        return true;
    }

    @Override
    @ExposeMethod(description = "Stop prelert")
    public boolean stop(Configuration config) {
        Path instDir = Paths.get(config.prelertInstallDir);
        if (Files.exists(instDir, LinkOption.NOFOLLOW_LINKS)) {
            if (OperatingSystemFamily.Windows == config.platform) {
                ProcessBuilder pb = ProcessUtils.newProcessBuilder()
                    .command("cmd", "/c",
                        "\"" + config.prelertInstallDir + "/ctl/admin/prelert_shutdown.bat\"", "<",
                        "NUL");
                try {
                    ProcessUtils
                        .waitForProcess(ProcessUtils.startProcess(pb), 1, TimeUnit.MINUTES, true);
                } catch (RuntimeException e) {
                    log.warn("Process killed during Stopping prelert.", e);
                }
            } else {
                throw ErrorUtils.logErrorAndReturnException(log, "Linux system is not supported");
            }
        } else {
            throw ErrorUtils.logErrorAndReturnException(log,
                "Missing Installation directory, can't stop the Prelert!");
        }
        return true;
    }

    @Override
    @ExposeMethod(description = "Uninstall prelert")
    public void uninstall(Configuration config, Path installerDir) {
        log.info("try to uninstall prelert");

        Path installerDir2 = installerDir == null ? getMSIInstallerDir(config) : installerDir;

        File installerFile = findFile(installerDir2, "CAAnalysisServer*.msi");
        if (installerFile != null) {
            if (OperatingSystemFamily.Windows == config.platform) {
                ProcessBuilder clw =
                    ProcessUtils
                        .newProcessBuilder()
                        .command("msiexec", "/x", installerFile.toString(), "/qn")
                        .directory(installerDir2.toFile());
                ProcessUtils
                    .waitForProcess(ProcessUtils.startProcess(clw), 5, TimeUnit.MINUTES, true);
            } else {
                throw ErrorUtils.logErrorAndReturnException(log, "Linux system is not supported");
            }
        } else { // installer/unistaller msi file is expected in install directory
            throw ErrorUtils.logErrorAndReturnException(log,
                "Cannot uninstall prelert, uninstaller is missing");
        }

        if (installerDir == null) {
            deleteFolder(installerDir2);
        }
    }

    private Path getMSIInstallerDir(Configuration config) {
        Path installerDir;
        try {
            installerDir = Files.createTempDirectory("staging");
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrap(log, e,
                "Cannot create temporary installer directory. Exception: {0}");
        }

        File instFileZip = download(config, installerDir);

        unpackFile(instFileZip, config);

        return installerDir;
    }
}
