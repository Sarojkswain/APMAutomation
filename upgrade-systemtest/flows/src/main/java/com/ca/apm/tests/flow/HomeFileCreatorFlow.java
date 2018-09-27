package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.tas.annotation.TasDocFlow;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;


/**
 * Created by jirji01 on 7/18/2017.
 */
@Flow
@TasDocFlow(
        description = "Flow is designed to create a single file from either another file (copy), resource or chunk of data, can set permissions and resolve home directory."
)
public class HomeFileCreatorFlow extends FileCreatorFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeFileCreatorFlow.class);
    @FlowContext
    private HomeFileCreatorFlowContext context;

    @Override
    public void run() throws Exception {
        super.run();

        try {
            if (StringUtils.isNotBlank(context.getPermissions())) {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString(context.getPermissions());
                Files.setPosixFilePermissions(getDestinationPath(), perms);
            }
        } catch (IOException|SecurityException exception) {
            LOGGER.error("Cannot change permissions", exception);
        }
    }

    @Override
    protected Path getDestinationPath() {
        String path = super.getDestinationPath().toString();
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }

        FileSystem fileSystem = FileSystems.getDefault();
        return fileSystem.getPath(path);
    }
}
