package com.ca.apm.siteminder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.responsefile.IResponseFile;
import com.ca.apm.automation.action.utils.Utils;
import com.ca.apm.automation.utils.archive.Archive;

@Flow
public class DeployAdminUIFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployAdminUIFlow.class);
    private long installLogFilePointer;
    private final long pollingInterval = 2 * 1000;

    @FlowContext
    private DeployAdminUIFlowContext context;

    @Override
    public void run() throws Exception {
        File auiInstallationDir = context.getAUIUnpackedSourcesDir();

        Archive auiprInstallerArchive =
            getArchiveFactory().createArchive(context.getAUIPRPackedInstallSourcesURL());
        Archive auiInstallerArchive =
            getArchiveFactory().createArchive(context.getAUIPackedInstallSourcesURL());

        auiprInstallerArchive.unpack(new File(auiInstallationDir.getPath()));
        auiInstallerArchive.unpack(new File(auiInstallationDir.getPath()));

        File responseFileDir = context.getResponseFileDir();
        File installResponseFile =
            new File(responseFileDir + "/aui_install_response_file.properties");
        IResponseFile auiResponseFile = new AUIResponseFile(context.getInstallResponseFileData());
        auiResponseFile.create(installResponseFile);

        if (!installResponseFile.exists() || !installResponseFile.canRead()) {
            throw new IllegalStateException("Installation response file is either missing or can't be read. Bummer...");
        } else {
            LOGGER.info("Installation response file created at: {}", installResponseFile);
        }

        File auiHomeDir = context.getAUIHomeDir();
        String auiprInstallerExecutable = context.getAUIPrereqExecutable();
        String auiInstallerExecutable = context.getAUIExecutable();

        File layoutProperties = new File(auiInstallationDir + "/layout.properties");
        File layoutPropertiesCopy = new File(auiInstallationDir + "/layout_copy.properties");

        Files.copy(layoutProperties.toPath(), layoutPropertiesCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

        List<String> lines =
            Files.readAllLines(layoutProperties.toPath(), Charset.defaultCharset());
        lines.remove(0);
        Files.write(layoutProperties.toPath(), lines, Charset.defaultCharset());

        runAndMonitorInstallationProcess(auiHomeDir, auiInstallationDir, installResponseFile, auiprInstallerExecutable);
        Files.copy(layoutPropertiesCopy.toPath(), layoutProperties.toPath(), StandardCopyOption.REPLACE_EXISTING);
        runAndMonitorInstallationProcess(auiHomeDir, auiInstallationDir, installResponseFile, auiInstallerExecutable);

    }

    private void runAndMonitorInstallationProcess(File auiHomeDir, File auiInstallationDir, File installResponseFile, String installerExecutable) throws Exception {
        File installationLogDir = new File(auiHomeDir + "/logs/install");
        File installationUserLogDir = new File("C:/Users/Administrator/auilogs");
        final File installationUserLogFile = new File(installationUserLogDir + "/log.txt");

        FileAlterationObserver observerLog = new FileAlterationObserver(installationLogDir);
        FileAlterationObserver observerUserLog = new FileAlterationObserver(installationUserLogDir);
        final FileAlterationMonitor monitor = new FileAlterationMonitor(pollingInterval);

        FileAlterationListener listenerUserLog = new FileAlterationListenerAdaptor() {

            @Override
            public void onFileCreate(File file) {
                if (file.getName().contains("log.txt")) {
                    LOGGER.info("Installation log file detected: {}", file);
                }
            }

            @Override
            public void onFileChange(File file) {
                if (file.getName().contains("log.txt")) {
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        int size = fileInputStream.available();
                        //rewind to desired location
                        fileInputStream.skip(installLogFilePointer);
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, Charsets.UTF_8))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                LOGGER.info(line);
                            }
                            installLogFilePointer = size;
                        }
                    } catch (FileNotFoundException e) {
                        LOGGER.error("{} log file was not found", installationUserLogFile);
                    } catch (IOException e) {
                        LOGGER.error("Log file seek error", e);
                    } catch (Exception e) {
                        LOGGER.error("Something went awry. Perhaps something with monitor?", e);
                    }
                }
            }
        };

        FileAlterationListener listenerLog = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                if (file.getName().contains("log.txt")) {
                    LOGGER.info("Installation log file created, looks like the installation completed: {}", file);
                    try {
                        monitor.stop();
                    } catch (Exception e) {
                        LOGGER.error("Error occurred while stopping the monitor.", e);
                    }
                }
            }
        };

        observerUserLog.addListener(listenerUserLog);
        observerLog.addListener(listenerLog);
        monitor.addObserver(observerUserLog);
        monitor.addObserver(observerLog);
        try {
            monitor.start();
        } catch (Exception e) {
            LOGGER.error("Failure to start the installation monitor. Looks like its going to be pretty silent...", e);
        }

        LOGGER.info("Initializing silent installation...");
        String installExecutableDirectory = auiInstallationDir + "";

        final int responseCode = Utils.exec(installExecutableDirectory, installExecutableDirectory
            + installerExecutable, new String[] {"-f", "aui_install_response_file.properties", "-i", "silent"}, LOGGER);
        try {
            monitor.stop();
        } catch (Exception e) {
            LOGGER.warn("Failure to stop the installation monitor. Most likely it has already been stopped... ({})", e.getMessage());
        }

        switch (AdminUIInstallerResponse.fromExitStatus(responseCode)) {
            case SUCCESS:
                LOGGER.info("Installation completed SUCCESSFULLY. Congrats!");
                break;
            case SUCCESS_WITH_RESULT_1:
                LOGGER.info("Installation probably completed SUCCESSFULLY with result 1.");
                break;
            case PARTIAL_SUCCESS:
                LOGGER.info("Installation completed with return code 2. That means only a PARTIAL SUCCESS. Check to log for details.");
                break;
            case ERROR_IN_RESPONSE_FILE:
                LOGGER.info("Installation failed due to error in response file.");
            default:
                throw new IllegalStateException(String.format("Launching silent installation of AUI failed (%d)", responseCode));
        }

    }

}
