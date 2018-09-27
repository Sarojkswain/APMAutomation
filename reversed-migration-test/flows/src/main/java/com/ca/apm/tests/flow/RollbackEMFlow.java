/*
 * Copyright (c) 2017 CA.  All rights reserved.
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
import com.ca.apm.automation.action.utils.monitor.TasFileWatchMonitor;
import com.google.common.io.Files;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rollback EM Flow
 *
 * @author dugra04
 */
@Flow
public class RollbackEMFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackEMFlow.class);
    public static final String ROLLBACK_SCRIPT_LINUX = "rollback.sh";
    public static final String ROLLBACK_SCRIPT_WINDOWS = "rollback.bat";
    public static final String ROLLBACK_LOG_REGEXP = "APMRollback\\.log$";
    private static final String ROLLBACK_PROPERTIES_FILENAME = "APMMasterRollback.properties";
    private static final String ROLLBACK_OPT_USE_REVERSED_MIGRATION = "-useReversedMigration";

    @FlowContext
    private RollbackEMFlowContext context;

    private File apmRollbackScript;
    private File apmRollbackDirectory;
    private File apmRollbackPropertyFile;
    private String apmRollbackBackupId;

    @Override
    public void run() throws Exception {
        initRollbackScript();
        if (this.apmRollbackBackupId != null || !this.context.isAllowFinishedRollback()) {
            runRollbackScript();
        }

    }

    /**
     * Initiates the APMRollback script
     */
    protected void initRollbackScript() throws Exception {
        this.apmRollbackDirectory = new File(this.context.getApmRollbackDirectory());
        LOGGER.info("Looking up the APMRollback directory: {}", this.apmRollbackDirectory);
        if (!this.apmRollbackDirectory.exists()) {
            if (this.context.isAllowFinishedRollback()) {
                // rollback was already finished, so rollback directory does not exist
                this.apmRollbackBackupId = null;
                return;
            } else {
                throw new IllegalStateException("Error looking up the APMRollback directory: " + this.apmRollbackDirectory +
                        " Maybe you have not run the upgrade which should create this directory.");
            }
        }

        this.apmRollbackScript = new File(this.apmRollbackDirectory, context.isLinux()
                                                                        ? ROLLBACK_SCRIPT_LINUX
                                                                        : ROLLBACK_SCRIPT_WINDOWS
        );
        LOGGER.info("APMRollback script: {}", this.apmRollbackScript);

        this.apmRollbackBackupId = findBackupId();
        LOGGER.debug("Using backup id: {} of installer property file {}.", apmRollbackBackupId, this.apmRollbackPropertyFile);
    }

    private String findBackupId() throws IOException {
        this.apmRollbackPropertyFile = new File(this.apmRollbackDirectory, ROLLBACK_PROPERTIES_FILENAME);
        LOGGER.debug("APMRollback property file {}.", this.apmRollbackPropertyFile);

        String foundBackupIdLine = null;
        List<String> backupIdLines = Files.readLines(this.apmRollbackPropertyFile, Charset.forName("UTF-8"));

        for (String backupIdLine : backupIdLines) {
            if (backupIdLine.trim().endsWith("="+this.context.getOlderEmInstallDir())) {
                foundBackupIdLine = backupIdLine;
                break;
            }
        }

        if (foundBackupIdLine==null) {
            // fallback - if OlderEmInstallDir/backup.id file exists, then it contains probably backup id.
            //If so, read the id and validate against apm rollback property file
            File backupIdFile = new File(this.context.getOlderEmInstallDir(), "backup.id");
            LOGGER.info("Trying to load backupid from "+backupIdFile.getPath());
            if (backupIdFile.isFile()) {
                String backupId = Files.readFirstLine(backupIdFile, Charset.forName("UTF-8")).trim();
                LOGGER.info("Installation backupid {} reported by {}", backupId, this.context.getOlderEmInstallDir());
                for (String backupIdLine : backupIdLines) {
                    if (backupIdLine.trim().startsWith(backupId + "=")) {
                        String directory = backupIdLine.substring(backupIdLine.indexOf('=') + 1);
                        LOGGER.info("Backup id {} found for directory {}", backupId, directory);
                        return backupId;
                    }
                }
                // not found, so continue...
            }
        }

        assert foundBackupIdLine != null || this.context.isAllowFinishedRollback(): "APMRollback backupId corresponding to backupId path: "+ this.context.getOlderEmInstallDir()
                + " inside APMRollback property file: " + ROLLBACK_PROPERTIES_FILENAME + " not found with content: " + Arrays.toString(backupIdLines.toArray());

        if (foundBackupIdLine == null && this.context.isAllowFinishedRollback()) {
            // rollback was already finished, so rollback entry was removed from rollback property file
            return null;
        }

        int indexOfEquals = foundBackupIdLine.indexOf("=");
        assert indexOfEquals > 0 : "APMRollback property backupId line must be in format timestamp=path, but found: '" + foundBackupIdLine + "'";

        return foundBackupIdLine.substring(0, indexOfEquals).trim();
    }

    private void runRollbackScript() throws Exception {
        assert this.apmRollbackScript != null : "APMRollback path is not initialized.";
        assert this.apmRollbackBackupId != null : "APMRollback backupId is not initialized.";

        if (this.context.isDoCleanupOnly()) {
            LOGGER.info("About to run APMRollback of EM to do clean-up of backup");
        } else {
            LOGGER.info("About to run APMRollback of EM to do rollback");
        }
        try (TasFileWatchMonitor watchMonitor = this.monitorFactory.createWatchMonitor()) {
            //setup monitoring
            final File monitorParentDir = this.apmRollbackDirectory;
            watchMonitor.watchFileChanged(monitorParentDir, ROLLBACK_LOG_REGEXP).watchFileCreated(monitorParentDir, ROLLBACK_LOG_REGEXP).monitor();

            final File execFile = this.apmRollbackScript;

            // Downloaded unix binaries are not executable, let's fix it
            if (!execFile.canExecute() && SystemUtils.IS_OS_UNIX) {
                if (!execFile.setExecutable(true)) {
                    throw new IllegalStateException("Failed to make rollback script executable.");
                }
            }


            final List<String> argsList = new ArrayList<String>(Arrays.asList((this.context.isDoCleanupOnly()?"cleanup":"rollback"), apmRollbackBackupId));
            if (this.context.isUseReversedMigration()) {
                argsList.add(ROLLBACK_OPT_USE_REVERSED_MIGRATION);
            }
            final String[] args = argsList.toArray(new String[]{});

            // Run installer
            final int retVal = new Execution.Builder(execFile.getPath(), LOGGER)
                    .args(args)
                    .workDir(this.apmRollbackDirectory)
                    .build().go();

            if (retVal != 0) {
                // check if rollback directory has been removed - it this case on windows an error is returned...
                if (SystemUtils.IS_OS_WINDOWS) {
                    if (!this.apmRollbackDirectory.exists()) {
                        logger.info("Ignoring Rollback failure ("+retVal+") on Windows, because rollback directory does not exist: "+context.getApmRollbackDirectory());
                    } else {
                        throw new IllegalStateException("APMRollback of EM failed.");
                    }
                } else {
                    throw new IllegalStateException("APMRollback of EM failed.");
                }
            }
        }
    }
}