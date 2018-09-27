package com.ca.apm.systemtest.fld.server.util;

import java.io.File;
import java.io.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

public class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {}

    public static boolean checkFile(File file) {
        return file.isFile() && file.canRead();
    }

    public static boolean checkDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            boolean dirCreated = dir.mkdirs();
            if (dirCreated) {
                LOGGER.info("NetworkTrafficMonitorDaoImpl.checkDir():: directory was created: {}",
                    dir);
                return true;
            } else {
                throw ErrorUtils
                    .logErrorAndThrowException(LOGGER,
                        "NetworkTrafficMonitorDaoImpl.checkDir():: directory WAS NOT created: {0}",
                        dir);
            }
        }
        return false;
    }

    public static final class DirFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && file.canExecute();
        }
    }

}
