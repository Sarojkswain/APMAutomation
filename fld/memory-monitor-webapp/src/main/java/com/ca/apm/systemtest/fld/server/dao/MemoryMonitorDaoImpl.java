package com.ca.apm.systemtest.fld.server.dao;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.server.model.MemoryMonitorValue;

@Component
public class MemoryMonitorDaoImpl implements MemoryMonitorDao<MemoryMonitorValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitorDaoImpl.class);

    private static final String IMAGE_FILE_SUFFIX = ".png";
    private static final String DESCRIPTION_FILE_SUFFIX = ".txt";

    private MemoryMonitorDaoFileFilter memoryMonitorDaoFileFilter =
        new MemoryMonitorDaoFileFilter();

    @Autowired
    private String memoryMonitorDataBaseDirName;
    private File memoryMonitorDataBaseDir;

    @PostConstruct
    public void init() {
        this.memoryMonitorDataBaseDirName =
            Paths.get(memoryMonitorDataBaseDirName).toAbsolutePath().toString();
        LOGGER.info("MemoryMonitorDaoImpl.init():: memoryMonitorDataBaseDirName = {}",
            memoryMonitorDataBaseDirName);
        this.memoryMonitorDataBaseDir = new File(memoryMonitorDataBaseDirName);
        checkDir(memoryMonitorDataBaseDir);
    }

    @Override
    public void update(MemoryMonitorValue value) throws IOException {
        LOGGER.debug("MemoryMonitorDaoImpl.update():: entry");
        LOGGER.debug("MemoryMonitorDaoImpl.update():: value = {}", value);
        try {
            // image
            String imageFilename = getImageFilename(value);
            File imageFile = getImageFile(imageFilename);
            FileUtils.writeByteArrayToFile(imageFile, value.getImage());
            LOGGER.info(
                "MemoryMonitorDaoImpl.update()::       image data successfully wrote to {}",
                imageFilename);

            // description
            String descriptionFilename = getDescriptionFilename(value);
            File descriptionFile = getDescriptionFile(descriptionFilename);
            FileUtils.writeStringToFile(descriptionFile, value.getDescription());
            LOGGER.info(
                "MemoryMonitorDaoImpl.update():: description data successfully wrote to {}",
                descriptionFilename);
        } finally {
            LOGGER.debug("MemoryMonitorDaoImpl.update():: exit");
        }
    }

    @Override
    public MemoryMonitorValue find(String id) throws IOException {
        LOGGER.debug("MemoryMonitorDaoImpl.find():: entry");
        LOGGER.debug("MemoryMonitorDaoImpl.find():: id = {}", id);
        try {
            MemoryMonitorValue value = new MemoryMonitorValue(id);

            // image
            String imageFilename = getImageFilename(id);
            File imageFile = getImageFile(imageFilename);
            if (checkFile(imageFile)) {
                fillImage(imageFile, value);
            } else {
                LOGGER.warn("MemoryMonitorDaoImpl.find():: cannot find or read file {}",
                    imageFilename);
                return null;
            }

            // description
            String descriptionFilename = getDescriptionFilename(id);
            File descriptionFile = getDescriptionFile(descriptionFilename);
            if (checkFile(descriptionFile)) {
                fillDescription(descriptionFile, value);
            } else {
                LOGGER.warn("MemoryMonitorDaoImpl.find():: cannot find or read file {}",
                    descriptionFilename);
                return null;
            }

            return value;
        } finally {
            LOGGER.debug("MemoryMonitorDaoImpl.find():: exit");
        }
    }

    @Override
    public List<MemoryMonitorValue> findAll() {
        LOGGER.debug("MemoryMonitorDaoImpl.findAll():: entry");
        try {
            File[] files = listFiles();
            Map<String, MemoryMonitorValue> values = new HashMap<>(files.length);
            for (File file : files) {
                String id = getId(file);
                MemoryMonitorValue value = values.get(id);
                if (value == null) {
                    value = new MemoryMonitorValue(id);
                    values.put(id, value);
                }

                // image
                if (isImageFile(file)) {
                    try {
                        fillImage(file, value);
                    } catch (IOException e) {
                        LOGGER.error("Unable to get image from file {}: {}", file, e);
                        continue;
                    }
                }

                // description
                else if (isDescriptionFile(file)) {
                    try {
                        fillDescription(file, value);
                    } catch (IOException e) {
                        LOGGER.error("Unable to get description from file {}: {}", file, e);
                        continue;
                    }
                }
            }

            List<MemoryMonitorValue> result = new ArrayList<>();
            for (MemoryMonitorValue value : values.values()) {
                if (value.getImage() == null) {
                    LOGGER.warn("MemoryMonitorDaoImpl.findAll():: no image file found for {}",
                        value);
                    continue;
                }
                if (value.getDescription() == null) {
                    LOGGER.warn(
                        "MemoryMonitorDaoImpl.findAll():: no description file found for {}", value);
                    continue;
                }
                result.add(value);
            }
            return result;
        } finally {
            LOGGER.debug("MemoryMonitorDaoImpl.findAll():: exit");
        }
    }

    private File getImageFile(String imageFilename) {
        return new File(memoryMonitorDataBaseDir, imageFilename);
    }

    private File getDescriptionFile(String descriptionFilename) {
        return new File(memoryMonitorDataBaseDir, descriptionFilename);
    }

    private File[] listFiles() {
        checkDir(memoryMonitorDataBaseDir);
        File[] files = memoryMonitorDataBaseDir.listFiles(memoryMonitorDaoFileFilter);
        if (files == null) {
            throw ErrorUtils
                .logErrorAndThrowException(
                    LOGGER,
                    "MemoryMonitorDaoImpl.listFiles():: directory does not exist or IO exception occured while reading dir: {0}",
                    memoryMonitorDataBaseDir);
        }
        return files;
    }

    private static void fillImage(File file, MemoryMonitorValue value) throws IOException {
        byte[] image = FileUtils.readFileToByteArray(file);
        value.setImage(image);
    }

    private static void fillDescription(File file, MemoryMonitorValue value) throws IOException {
        String description = FileUtils.readFileToString(file);
        value.setDescription(description);
    }

    private static String getImageFilename(MemoryMonitorValue value) {
        return getImageFilename(value.getId());
    }

    private static String getImageFilename(String id) {
        return (new StringBuilder(id)).append(IMAGE_FILE_SUFFIX).toString();
    }

    private static String getDescriptionFilename(MemoryMonitorValue value) {
        return getDescriptionFilename(value.getId());
    }

    private static String getDescriptionFilename(String id) {
        return (new StringBuilder(id)).append(DESCRIPTION_FILE_SUFFIX).toString();
    }

    private static boolean isImageFile(File file) {
        return file.getName().toLowerCase().endsWith(IMAGE_FILE_SUFFIX);
    }

    private static boolean isDescriptionFile(File file) {
        return file.getName().toLowerCase().endsWith(DESCRIPTION_FILE_SUFFIX);
    }

    private static String getId(File file) {
        String fileName = file.getName();
        int i = fileName.lastIndexOf(IMAGE_FILE_SUFFIX);
        if (i < 0) {
            i = fileName.lastIndexOf(DESCRIPTION_FILE_SUFFIX);
        }
        return fileName.substring(0, i);
    }

    private static boolean checkFile(File file) {
        return file.isFile() && file.canRead();
    }

    private static boolean checkDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            boolean dirCreated = dir.mkdirs();
            if (dirCreated) {
                LOGGER.info("MemoryMonitorDaoImpl.checkDir():: directory was created: {}", dir);
                return true;
            } else {
                throw ErrorUtils.logErrorAndThrowException(LOGGER,
                    "MemoryMonitorDaoImpl.checkDir():: directory WAS NOT created: {0}", dir);
            }
        }
        return false;
    }

    private static final class MemoryMonitorDaoFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return checkFile(file) && (isImageFile(file) || isDescriptionFile(file));
        }
    }

}
