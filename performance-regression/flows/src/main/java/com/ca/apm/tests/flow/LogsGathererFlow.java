/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class LogsGathererFlow extends CopyResultsFlowAbs {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogsGathererFlow.class);
    @FlowContext
    private LogsGathererFlowContext context;

    public LogsGathererFlow() {
    }

    public static void writeZipFile(File directoryToZip, File zipFile, boolean ignoreEmpty) throws IOException {
        Collection<File> fileList = FileUtils.listFiles(directoryToZip, null, true);
        if (fileList.size() > 0 || !ignoreEmpty) {
            LOGGER.info("Creating archive '" + zipFile.getAbsolutePath() + "'");
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos);) {
                for (File file : fileList) {
                    if (!file.isDirectory()) { // we only zip files, not directories
                        try (FileInputStream fis = new FileInputStream(file);) {
                            // we want the zipEntry's path to be a relative path that is relative
                            // to the directory being zipped, so chop off the rest of the path
                            String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                                    file.getCanonicalPath().length());
                            ZipEntry zipEntry = new ZipEntry(zipFilePath);
                            zos.putNextEntry(zipEntry);
                            IOUtils.copy(fis, zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeFormatted = sdf.format(new Date());
        if (context.getFilesMapping() != null) {
            for (Map.Entry<String, String> mapping : context.getFilesMapping().entrySet()) {
                File sourceDirectory = FileUtils.getFile(mapping.getValue());
                File targetDirectory = FileUtils.getFile(context.getTargetDir(), timeFormatted + "_" + sourceDirectory.getName());
                String zipFileName = context.getAddTimestamp() ? timeFormatted + "_" + mapping.getKey() : mapping.getKey();
                File zipFile = FileUtils.getFile(context.getTargetDir(), zipFileName);
                if (sourceDirectory.exists()) {
                    LOGGER.info("Copying files from '" + sourceDirectory.getAbsolutePath() + "' to '" + targetDirectory.getAbsolutePath() + "'");
                    FileUtils.copyDirectory(sourceDirectory, targetDirectory, true);
                    writeZipFile(sourceDirectory, zipFile, context.getIgnoreEmpty());
                    try {
                        FileUtils.deleteDirectory(targetDirectory);
                    } catch (IOException e) {
                        LOGGER.warn("Error while deleting file", e);
                    }
                    if (context.getDeleteSource()) {
                        LOGGER.info("Deleting files from '" + sourceDirectory.getAbsolutePath() + "'");
                        try {
                            FileUtils.cleanDirectory(sourceDirectory);
                        } catch (IOException e) {
                            if (context.getIgnoreDeletionErrors()) {
                                LOGGER.warn("Error while deleting file", e);
                            } else {
                                throw e;
                            }
                        }
                    }
                }
                else {
                    LOGGER.warn("Source '" + sourceDirectory.getAbsolutePath() + "' does not exist. Ignoring.");
                }
            }
        } else {
            // todo copy files
            // todo zip files
            if (context.getDeleteSource()) {
                // todo delete original files
            }
            if (this.context.getCopyResultsDestinationDir() != null) {
                Path origFilePath = Paths.get(this.context.getTargetDir(), this.context.getTargetZipFile());
                String file = origFilePath.getFileName().toString();
                if (this.context.getCopyResultsDestinationFileName() != null) {
                    file = this.context.getCopyResultsDestinationFileName();
                }
                Path destFilePath = Paths.get(this.context.getCopyResultsDestinationDir(), file);
                try {
                    LOGGER.info("Copying file " + origFilePath.toString() + " to " + destFilePath.toString());
                    if (this.context.getCopyResultsDestinationDir().startsWith("\\\\")
                            && this.context.getCopyResultsDestinationUser() != null
                            && this.context.getCopyResultsDestinationPassword() != null) {
                        // it's a network location
                        configNet(this.context.getCopyResultsDestinationDir(),
                                this.context.getCopyResultsDestinationUser(),
                                this.context.getCopyResultsDestinationPassword());
                    }
                    copyFile(origFilePath.toString(), destFilePath.toString());
                } catch (InterruptedException var3) {
                    throw new IllegalStateException(var3);
                }
            }
        }
        LOGGER.info("Flow has finished.");
    }

    @Override
    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

}
