package com.ca.apm.systemtest.fld.util.networktrafficmonitor;

import java.io.File;
import java.io.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

public class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {}

    public static File[] listFiles(File dir, FileFilter fileFilter) {
        checkDir(dir);
        File[] files = dir.listFiles(fileFilter);
        if (files == null) {
            throw ErrorUtils
                .logErrorAndThrowException(
                    LOGGER,
                    "Util.listFiles():: directory does not exist or IO exception occured while reading dir: {0}",
                    dir);
        }
        return files;
    }

    public static boolean checkDir(String dirName) {
        return checkDir(new File(dirName));
    }

    public static boolean checkDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            boolean dirCreated = dir.mkdirs();
            if (dirCreated) {
                LOGGER.info("Util.checkDir():: directory was created: {}", dir);
                return true;
            } else {
                throw ErrorUtils.logErrorAndThrowException(LOGGER,
                    "Util.checkDir():: directory WAS NOT created: {0}", dir);
            }
        }
        return false;
    }

    public static boolean checkFile(File file) {
        return file.isFile() && file.canRead();
    }

    public static void sleep(long sleepTime) {
        try {
            LOGGER.info("Util.sleep():: sleeping for {} [s]", (sleepTime / 1000));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("Util.sleep():: InterruptedException");
        }
    }

}
