package com.ca.apm.siteminder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.eclipse.aether.installation.InstallationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.responsefile.IResponseFile;
import com.ca.apm.automation.action.utils.Utils;
import com.ca.apm.automation.utils.archive.Archive;

@Flow
public class DeployPolicyServerFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployPolicyServerFlow.class);
    @FlowContext
    private DeployPolicyServerFlowContext context;

    private long installLogFilePointer;
    private final long pollingInterval = 2 * 1000;

    @Override
    public void run() throws Exception {
        File psInstallationDir = context.getPSUnpackedSourcesDir();
        Archive installerArchive =
            getArchiveFactory().createArchive(context.getPSPackedInstallSourcesURL());
        installerArchive.unpack(new File(psInstallationDir.getPath()));

        File responseFileDir = context.getResponseFileDir();
        File installResponseFile =
            new File(responseFileDir + "/ps_install_response_file.properties");
        IResponseFile psResponseFile = new PSResponseFile(context.getInstallResponseFileData());
        psResponseFile.create(installResponseFile);

        if (!installResponseFile.exists() || !installResponseFile.canRead()) {
            throw new IllegalStateException("Installation response file is either missing or can't be read. Bummer...");
        } else {
            LOGGER.info("Installation response file created at: {}", installResponseFile);
        }

        File psHomeDir = new File(context.getPSHomeDir());
        runAndMonitorInstallationProcess(psHomeDir, psInstallationDir, installResponseFile);

        // copy smreg.exe to ps install directory
        File smreg = new File(psInstallationDir.getPath() + "/smreg.exe");
        if (smreg.exists()) {
            File newFile = new File(psHomeDir.getPath() + "/bin/smreg.exe");
            LOGGER.info("Copy {} to : {}", smreg.getAbsolutePath(), newFile.getAbsolutePath());
            Files.copy(smreg.toPath(), newFile.toPath());
        } else {
            LOGGER.info("Cannot find source file: {}", smreg.getAbsolutePath());
        }

    }

    private void runAndMonitorInstallationProcess(File psHomeDir, File psInstallationDir, File installResponseFile) throws Exception {
        File installationLogDir = new File(psHomeDir + "/logs/install");
        File installationUserLogDir = new File("C:/Users/Administrator/pslogs");
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
                        // rewind to desired location
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
        String installExecutableDirectory = psInstallationDir + "";

        final int responseCode = Utils.exec(installExecutableDirectory, installExecutableDirectory
                + context.getPsInstallExecutable(), new String[] {"-f", "ps_install_response_file.properties", "-i", "silent"}, LOGGER);
        try {
            monitor.stop();
        } catch (Exception e) {
            LOGGER.warn("Failure to stop the installation monitor. Most likely it has already been stopped... ({})", e.getMessage());
        }
        switch (PolicyServerInstallerResponse.fromExitStatus(responseCode)) {
            case SUCCESS:
                LOGGER.info("Installation completed SUCCESSFULLY. Congrats!");
                break;
            case JAVA_MISSING:
                LOGGER.info("Installation failed with return code 1. Check the install log for details. Probably an invalid Java path provided");
                throw new InstallationException(String.format("Silent installation of Policy Store failed - invalid Java path (%d). Check DEFAULT_JRE_ROOT path from response file.", responseCode));
            case PARTIAL_SUCCESS:
                LOGGER.info("Installation completed with return code 2. That means only a PARTIAL SUCCESS. Check to log for details.");
                break;
            case ERROR_IN_RESPONSE_FILE:
                LOGGER.info("Installation failed due to error in response file.");
            default:
                throw new IllegalStateException(String.format("Launching silent installation of PS failed (%d)", responseCode));
        }
    }
}
