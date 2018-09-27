package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ErrorUtils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by nick on 8.10.14.
 */
@Flow
public class FileBackupFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBackupFlow.class);

    @FlowContext
    private FileBackupFlowContext flowContext;

    @Override
    public void run() throws Exception {

        String operation = flowContext.getOperation().trim();
        List<String> arguments = flowContext.getArguments();
        LOGGER.debug(operation);

        switch (operation) {
            case "copyDir": {
                if (arguments.size() == 2) {
                    String srcStr = arguments.get(0);
                    String destStr = arguments.get(1);
                    File src = new File(srcStr);
                    File dest = new File(destStr);
                    try {
                        FileUtils.copyDirectory(src, dest);
                    } catch (Exception e) {
                        ErrorUtils.logExceptionFmt(LOGGER, e,
                            "Failed to copy directory {1} to {2}. Exception: {0}",
                            src.getAbsolutePath(), dest.getAbsolutePath());
                    }
                }
                break;
            }

            case "renameDir": {
                if (arguments.size() == 2) {
                    File src = new File(arguments.get(0));
                    File dest = new File(arguments.get(1));
                    boolean isMoved = src.renameTo(dest);
                    if (!isMoved) {
                        throw ErrorUtils.logErrorAndReturnException(LOGGER,
                            "Failed to rename {0} to {1}", src.getAbsolutePath(),
                            dest.getAbsolutePath());
                    }
                }
                break;
            }
        }
    }
}
