package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.util.ArchiveUtils;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveEntry;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This flow cleans up log files from command line workstation runs.
 *
 * @author haiva01
 */
@Flow
public class ClwCleanupFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(ClwCleanupFlow.class);

    @FlowContext
    ClwCleanupFlowContext flowContext;

    private static String getArchiveFileName() {
        Date now = new Date();
        return String.format(Locale.US, "backup%tY%tm%td%d.zip", now, now, now, now.getTime());
    }

    @Override
    public void run() throws Exception {
        final Path workDir = Paths.get(flowContext.getDir());
        final long millisNow = System.currentTimeMillis();
        final long tooRecentThreshold = millisNow - TimeUnit.MINUTES.toMillis(5);
        Collection<File> filesToArchive = new ArrayList<>(10);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(workDir)) {
            for (Path fileName : directoryStream) {
                BasicFileAttributes fattrs
                    = Files.readAttributes(fileName, BasicFileAttributes.class);
                if (// We want to check only regular files.
                    !fattrs.isRegularFile()
                        // We avoid files created very recently so that we do not try to delete them
                        // while the CLW is still running and having them open.
                        || fattrs.creationTime().toMillis() > tooRecentThreshold
                        // And we are only interest in CLW's output files.
                        || !(StringUtils.startsWithIgnoreCase(fileName.getFileName().toString(),
                                "TransactionTraceData")
                            && StringUtils.endsWithIgnoreCase(fileName.getFileName().toString(),
                                ".xml"))) {
                    log.debug("Skipping file {}", fileName.toAbsolutePath());
                    continue;
                }

                // Add this file to archive.
                filesToArchive.add(fileName.toAbsolutePath().toFile());
                log.debug("File {} will be archived.", fileName.toAbsolutePath());
            }
        }

        if (filesToArchive.isEmpty()) {
            return;
        }

        Collection<ArchiveEntry> archiveEntries = new ArrayList<>(filesToArchive.size());
        for (File file : filesToArchive) {
            ArchiveEntry archiveEntry = ArchiveEntry.singleFile(file.getAbsolutePath());
            archiveEntries.add(archiveEntry);
        }

        Path archivePath = Paths.get(flowContext.getDir(), getArchiveFileName()).toAbsolutePath();
        AbstractArchiver zipArchiver = ArchiveUtils
            .prepareArchiver(archivePath.toString(), ArchiveType.ZIP, ArchiveCompression.DEFAULT,
                archiveEntries);

        try {
            zipArchiver.createArchive();
        } catch (Exception e) {
            final String msg = ErrorUtils.logExceptionFmt(log, e,
                "Failed to create archive {1}. Exception: {0}",
                archivePath);
            throw new IllegalStateException(msg, e);
        }

        for (File file : filesToArchive) {
            boolean success = FileUtils.deleteQuietly(file);
            if (!success) {
                log.warn("Failed to delete file {}", file.getAbsolutePath());
            }
        }
    }
}
