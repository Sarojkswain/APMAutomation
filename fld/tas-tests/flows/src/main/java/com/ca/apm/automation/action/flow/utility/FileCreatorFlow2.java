package com.ca.apm.automation.action.flow.utility;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.tas.annotation.TasDocFlow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Writes a file.
 *
 * This class provides a means to place a file on the target machine without the need to have it in Artifactory.
 *
 * @author Nick Giles (gilni04@ca.com)
 */
@Flow
@TasDocFlow(description = "Flow is designed to create a single file from either another file (copy), resource or chunk of data.")
public class FileCreatorFlow2 extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCreatorFlow2.class);

    @FlowContext
    private FileCreatorFlowContext context;

    @Override
    public void run() throws Exception {
        byte[] data = loadData();
        if (context.getDestinationPath() != null) {
            LOGGER.info("Creating filtered file {}.", context.getDestinationPath());
            Path destinationPath = new File(context.getDestinationPath()).toPath();
            if (destinationPath.getParent() != null) {
                Files.createDirectories(destinationPath.getParent());
            }
            Files.write(new File(context.getDestinationPath()).toPath(), processFilters(data));
        } else {
            Path destinationPath = getDestinationPath();
            if (destinationPath.getParent() != null) {
                Files.createDirectories(destinationPath.getParent());
            }
            Files.write(destinationPath, processFilters(data));
            LOGGER.info("Creating filtered file {}.", destinationPath);
        }
    }

    /* Non-public methods */

    protected byte[] loadData() throws IOException {
        byte[] data;
        if (context.getSourceResourcePath() != null) {
            LOGGER.info("Loading data from resource {}.", context.getSourceResourcePath());
            data = IOUtils.toByteArray(getClass().getResourceAsStream(context.getSourceResourcePath()));
        } else if (context.getSourceFilePath() != null) {
            LOGGER.info("Loading data from file {}.", context.getSourceFilePath());
            data = FileUtils.readFileToByteArray(new File(context.getSourceFilePath()));
        } else {
            LOGGER.info("Loading data from collection {}.", context.getSourceData());
            Path tmpPath = Files.createTempFile("fileCreator", StringUtils.EMPTY);
            File tmpFile = tmpPath.toFile();
            FileUtils.writeLines(tmpFile, context.getSourceData());
            data = FileUtils.readFileToByteArray(tmpFile);
        }

        return data;
    }

    protected byte[] processFilters(byte[] data) {

        Map<String, String> substitutionVariables = context.getSubstitutionVariables();
        if (!substitutionVariables.isEmpty()) {
            LOGGER.info("Substitution variables detected {}.", substitutionVariables);
            VarSubstitutionFilter filter = VarSubstitutionFilter.withCharsetAndPlaceholder(context.getCharset(), context.getPlaceholder());
            filter.add(substitutionVariables);
            data = filter.filter(data);
        }

        Map<String, String> replacePairs = context.getReplacePairs();
        if (!replacePairs.isEmpty()) {
            LOGGER.info("Replace pairs detected {}.", replacePairs);
            ReplaceFilter filter = ReplaceFilter.withCharset(context.getCharset());
            filter.add(replacePairs);
            data = filter.filter(data);
        }

        return data;
    }

    protected Path getDestinationPath() {
        assert context.getDestinationDir() != null;
        assert context.getDestinationFilename() != null;

        FileSystem fileSystem = FileSystems.getDefault();
        return fileSystem.getPath(context.getDestinationDir(), context.getDestinationFilename());
    }
}
