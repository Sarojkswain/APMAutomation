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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.em.EmFeature;
import com.ca.apm.automation.action.flow.em.FileNameExtractor;
import com.ca.apm.automation.action.utils.monitor.TasFileWatchMonitor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

/**
 * DeployEMFlow class
 * <p>
 * Upgrades Enterprise Manager.
 *
 * @author Jan Pojer (pojja01@ca.com)
 */
@Flow
public class UpgradeEMFlow extends FlowBase {

    private static final String UPGRADE_INSTALLER_PROPERTIES_FILENAME = "upgrade.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(com.ca.apm.automation.action.flow.em.UpgradeEMFlow.class);
    private static final String INSTALLER_KEY_CA_EULA = "ca-eulaFile";
    @FlowContext
    private UpgradeEMFlowContext context;

    private File installerFile;
    private File installerDirectory;
    private File silentResponseFile;


    @Override
    public void run() throws Exception {

        initInstallerFile();
        downloadEm();
        createInstallerResponseFile();
        runSilentInstaller();
        validateInstall();
    }

    /**
     * Initiates the installer file from installer URL
     */
    protected void initInstallerFile() {
        this.installerDirectory = new File(this.context.getInstallerDir());
        LOGGER.info("Creating installer directory: {}", this.installerDirectory);
        if (!this.installerDirectory.exists() && !this.installerDirectory.mkdirs()) {
            throw new IllegalStateException("Error creating installer directory: " + this.installerDirectory);
        }

        final String filename = new FileNameExtractor(this.context.getInstallerUrl()).extract();
        this.installerFile = new File(this.installerDirectory, filename);
        LOGGER.info("Installer file: {}", this.installerFile);
    }

    protected void downloadEm() throws IOException {
        assert this.installerFile != null : "Installer URL cannot be null.";
        if (this.installerFile.exists()) {
            LOGGER.info("EM installer already exists: {}", this.installerFile);
            return;
        }

        this.archiveFactory.createArtifact(this.context.getInstallerUrl()).download(this.installerFile);
    }

    /**
     * Creates a response file for EM installation.
     */
    protected void createInstallerResponseFile() throws IOException {
        assert this.installerDirectory != null : "Target directory must be set at this point";

        final Map<String, String> installerProperties = this.context.getInstallerProperties();
        final String eulaPath = getEulaPath(new File(this.context.getOlderEmInstallDir())).getAbsolutePath();
        LOGGER.info("Setting ca-eula path: {}", eulaPath);
        installerProperties.put(INSTALLER_KEY_CA_EULA, eulaPath);

        this.silentResponseFile = new File(this.installerDirectory, UPGRADE_INSTALLER_PROPERTIES_FILENAME);
        LOGGER.debug("Creating response file {} from resource {}.", this.silentResponseFile, this.context.getSampleResponseFile());

        InputStream responseFileStream = getClass().getResourceAsStream(this.context.getSampleResponseFile());
        if (responseFileStream == null) {
            responseFileStream = Files.newInputStream(Paths.get(this.context.getSampleResponseFile()));
        }
        FileUtils.copyInputStreamToFile(responseFileStream, this.silentResponseFile);

        LOGGER.info("Response file created ({}).", this.silentResponseFile);
        this.configFileFactory.create(this.silentResponseFile).properties(installerProperties);
        LOGGER.info("Response file configured with installer properties: {}.", installerProperties);
    }

    protected void runSilentInstaller() throws Exception {
        assert this.installerFile != null : "Installer path is not initialized.";
        assert this.silentResponseFile != null : "Silent response file is not initialized.";

        LOGGER.info("About to run silent upgrade of EM");

        try (TasFileWatchMonitor watchMonitor = this.monitorFactory.createWatchMonitor()) {
            //setup monitoring
            final File monitorParentDir = new File(System.getProperty("java.io.tmpdir"));
            watchMonitor.watchFileChanged(monitorParentDir, ".*details$").watchFileCreated(monitorParentDir, ".*details$").monitor();

            final File execFile = this.installerFile;

            // Downloaded unix binaries are not executable, let's fix it
            if (!execFile.canExecute() && SystemUtils.IS_OS_UNIX) {
                if (!execFile.setExecutable(true)) {
                    throw new IllegalStateException("Failed to make installer executable.");
                }
            }

            final String[] args = {"-f", this.silentResponseFile.getPath()};
            // Run installer
            final int retVal = new Execution.Builder(execFile.getPath(), LOGGER)
                    .args(args)
                    .workDir(this.installerDirectory)
                    .build().go();

            if (retVal != 0) {
                throw new IllegalStateException("Upgrading of EM failed.");
            }
        }
    }

    protected void validateInstall() {
        if (!EmFeature.isDbOnly(this.context.getEmFeatures()) && !new File(this.context.getOlderEmInstallDir(), "bin").exists()) {
            throw new IllegalStateException("EM installation was not completed");
        }
    }

    protected File getEulaPath(final File oldEmDir) throws IOException {
        final String caEulaPath = this.context.getCaEulaPath();
        if (StringUtils.isNotBlank(caEulaPath)) {
            LOGGER.info("CA EULA is located @ {}", caEulaPath);
            File eula =  new File(caEulaPath);
            if (!eula.exists()) {
                eula = new File(this.installerDirectory, "eula.txt");
                InputStream responseFileStream = getClass().getResourceAsStream(caEulaPath);
                FileUtils.copyInputStreamToFile(responseFileStream, eula);
            }
            return eula;
        }
        final EulaFinder eulaFinder = new EulaFinder();
        final File parentFile = oldEmDir.getParentFile();
        LOGGER.info("Looking for CA EULA in {}", parentFile);
        Files.walkFileTree(parentFile.toPath(), eulaFinder);
        return eulaFinder.retrieveEula();
    }

    /**
     * Iterates through directory and attempts to recursively find EULA file
     * <p>
     * <pre>
     *  final EulaFinder eulaFinder = new EulaFinder();
     *  Files.walkFileTree(parentFile.toPath(), eulaFinder);
     *  eulaFinder.retrieveEula();
     * </pre>
     */
    private static class EulaFinder extends SimpleFileVisitor<Path> {
        private Path matchedFile;

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (!isMatch(file)) {
                return FileVisitResult.CONTINUE;
            }
            this.matchedFile = file;
            return FileVisitResult.TERMINATE;
        }

        private boolean isMatch(final Path file) {
            return file.endsWith(DeployEMFlowContext.CAEULA_FILENAME);
        }

        /**
         * @return Detected EULA {@code File}
         * @throws IllegalStateException if the eula has not been found
         */
        public File retrieveEula() {
            if (this.matchedFile == null) {
                throw new IllegalStateException("EULA has not been found");
            }
            return this.matchedFile.toFile();
        }
    }
}
