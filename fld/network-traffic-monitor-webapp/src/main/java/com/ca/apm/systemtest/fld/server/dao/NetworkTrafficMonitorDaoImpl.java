package com.ca.apm.systemtest.fld.server.dao;

import static com.ca.apm.systemtest.fld.server.util.Util.checkDir;
import static com.ca.apm.systemtest.fld.server.util.Util.checkFile;

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

import com.ca.apm.systemtest.fld.server.model.NetworkTrafficMonitorValue;
import com.ca.apm.systemtest.fld.server.util.Util.DirFileFilter;

@Component
public class NetworkTrafficMonitorDaoImpl
    implements
        NetworkTrafficMonitorDao<NetworkTrafficMonitorValue> {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(NetworkTrafficMonitorDaoImpl.class);

    private static final String IMAGE_FILE_SUFFIX = ".png";
    private static final String DESCRIPTION_FILE_SUFFIX = ".txt";

    private DirFileFilter dirFileFilter = new DirFileFilter();

    private NetworkTrafficMonitorDaoFileFilter networkTrafficMonitorDaoFileFilter =
        new NetworkTrafficMonitorDaoFileFilter();

    @Autowired
    private String networkTrafficMonitorDataBaseDirName;
    private File networkTrafficMonitorDataBaseDir;

    @PostConstruct
    public void init() {
        this.networkTrafficMonitorDataBaseDirName =
            Paths.get(networkTrafficMonitorDataBaseDirName).toAbsolutePath().toString();
        LOGGER.info(
            "NetworkTrafficMonitorDaoImpl.init():: networkTrafficMonitorDataBaseDirName = {}",
            networkTrafficMonitorDataBaseDirName);
        this.networkTrafficMonitorDataBaseDir = new File(networkTrafficMonitorDataBaseDirName);
        checkDir(networkTrafficMonitorDataBaseDir);
    }

    @Override
    public void update(NetworkTrafficMonitorValue value) throws IOException {
        LOGGER.debug("NetworkTrafficMonitorDaoImpl.update():: entry");
        LOGGER.debug("NetworkTrafficMonitorDaoImpl.update():: value = {}", value);
        try {
            // image
            String imageFilename = getImageFilename(value);
            File imageFile = getImageFile(value, imageFilename);
            FileUtils.writeByteArrayToFile(imageFile, value.getImage());
            LOGGER
                .info(
                    "NetworkTrafficMonitorDaoImpl.update()::       image data successfully wrote to {}",
                    imageFile);

            // description
            String descriptionFilename = getDescriptionFilename(value);
            File descriptionFile = getDescriptionFile(value, descriptionFilename);
            FileUtils.writeStringToFile(descriptionFile, value.getDescription());
            LOGGER
                .info(
                    "NetworkTrafficMonitorDaoImpl.update():: description data successfully wrote to {}",
                    descriptionFile);
        } finally {
            LOGGER.debug("NetworkTrafficMonitorDaoImpl.update():: exit");
        }
    }

    @Override
    public NetworkTrafficMonitorValue find(String host, String remoteHost, String type)
        throws IOException {
        LOGGER.debug("NetworkTrafficMonitorDaoImpl.find():: entry");
        LOGGER.debug("NetworkTrafficMonitorDaoImpl.find():: host = {}, remoteHost = {}, type = {}",
            host, remoteHost, type);
        try {
            NetworkTrafficMonitorValue value =
                new NetworkTrafficMonitorValue(host, remoteHost, type);

            // image
            String imageFilename = getImageFilename(value);
            File imageFile = getImageFile(value, imageFilename);
            boolean hasImageFile = checkFile(imageFile);
            if (hasImageFile) {
                fillImage(imageFile, value);
            }

            // description
            String descriptionFilename = getDescriptionFilename(value);
            File descriptionFile = getDescriptionFile(value, descriptionFilename);
            boolean hasDescriptionFile = checkFile(descriptionFile);
            if (hasDescriptionFile) {
                fillDescription(descriptionFile, value);
            }

            if (hasImageFile && hasDescriptionFile) {
                LOGGER.debug("NetworkTrafficMonitorDaoImpl.find():: found value = {}", value);
                return value;
            } else {
                if (!hasImageFile && !hasDescriptionFile) {
                    LOGGER
                        .info(
                            "NetworkTrafficMonitorDaoImpl.find():: value not found for host = {}, remoteHost = {}, type = {}",
                            host, remoteHost, type);
                } else if (!hasImageFile) {
                    LOGGER
                        .warn(
                            "NetworkTrafficMonitorDaoImpl.find():: image file {} not found for host = {}, remoteHost = {}, type = {}",
                            imageFile, host, remoteHost, type);
                } else if (!hasDescriptionFile) {
                    LOGGER
                        .warn(
                            "NetworkTrafficMonitorDaoImpl.find():: description file {} not found for host = {}, remoteHost = {}, type = {}",
                            descriptionFile, host, remoteHost, type);
                }
                return null;
            }
        } finally {
            LOGGER.debug("NetworkTrafficMonitorDaoImpl.find():: exit");
        }
    }

    @Override
    public List<NetworkTrafficMonitorValue> findAll() {
        LOGGER.debug("NetworkTrafficMonitorDaoImpl.findAll():: entry");
        try {
            List<NetworkTrafficMonitorValue> result = new ArrayList<>();
            checkDir(networkTrafficMonitorDataBaseDir);
            File[] hostsDirs = networkTrafficMonitorDataBaseDir.listFiles(dirFileFilter);
            for (File hostDir : hostsDirs) {
                File[] remoteHostsDirs = hostDir.listFiles(dirFileFilter);
                for (File remoteHostDir : remoteHostsDirs) {
                    File[] files = remoteHostDir.listFiles(networkTrafficMonitorDaoFileFilter);
                    Map<String, NetworkTrafficMonitorValue> values = new HashMap<>(files.length);
                    for (File file : files) {
                        String type = getType(file);
                        NetworkTrafficMonitorValue value = values.get(type);
                        if (value == null) {
                            value =
                                new NetworkTrafficMonitorValue(hostDir.getName(),
                                    remoteHostDir.getName(), type);
                            values.put(type, value);
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

                    for (NetworkTrafficMonitorValue value : values.values()) {
                        if (value.getImage() == null) {
                            LOGGER
                                .warn(
                                    "NetworkTrafficMonitorDaoImpl.findAll():: no image file found for {}",
                                    value);
                            continue;
                        }
                        if (value.getDescription() == null) {
                            LOGGER
                                .warn(
                                    "NetworkTrafficMonitorDaoImpl.findAll():: no description file found for {}",
                                    value);
                            continue;
                        }
                        result.add(value);
                    }
                }
            }
            return result;
        } finally {
            LOGGER.debug("NetworkTrafficMonitorDaoImpl.findAll():: exit");
        }
    }

    private File getImageFile(NetworkTrafficMonitorValue value, String imageFilename) {
        return Paths.get(networkTrafficMonitorDataBaseDirName, value.getHost(),
            value.getRemoteHost(), imageFilename).toFile();
    }

    private File getDescriptionFile(NetworkTrafficMonitorValue value, String descriptionFilename) {
        return Paths.get(networkTrafficMonitorDataBaseDirName, value.getHost(),
            value.getRemoteHost(), descriptionFilename).toFile();
    }

    private static void fillImage(File file, NetworkTrafficMonitorValue value) throws IOException {
        byte[] image = FileUtils.readFileToByteArray(file);
        value.setImage(image);
    }

    private static void fillDescription(File file, NetworkTrafficMonitorValue value)
        throws IOException {
        String description = FileUtils.readFileToString(file);
        value.setDescription(description);
    }

    private static String getImageFilename(NetworkTrafficMonitorValue value) {
        String id = value.getType();
        return (new StringBuilder(id)).append(IMAGE_FILE_SUFFIX).toString();
    }

    private static String getDescriptionFilename(NetworkTrafficMonitorValue value) {
        String id = value.getType();
        return (new StringBuilder(id)).append(DESCRIPTION_FILE_SUFFIX).toString();
    }

    private static boolean isImageFile(File file) {
        return file.getName().toLowerCase().endsWith(IMAGE_FILE_SUFFIX);
    }

    private static boolean isDescriptionFile(File file) {
        return file.getName().toLowerCase().endsWith(DESCRIPTION_FILE_SUFFIX);
    }

    private static String getType(File file) {
        String fileName = file.getName();
        int i = fileName.lastIndexOf(IMAGE_FILE_SUFFIX);
        if (i < 0) {
            i = fileName.lastIndexOf(DESCRIPTION_FILE_SUFFIX);
        }
        return fileName.substring(0, i);
    }

    private static final class NetworkTrafficMonitorDaoFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return checkFile(file) && (isImageFile(file) || isDescriptionFile(file));
        }
    }

}
