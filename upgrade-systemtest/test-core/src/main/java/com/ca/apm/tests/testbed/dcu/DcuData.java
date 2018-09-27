package com.ca.apm.tests.testbed.dcu;

import com.ca.tas.agent.entities.RoleData;
import com.ca.tas.exception.UnknownEnumTypeException;
import com.google.gson.Gson;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by jirji01 on 6/23/2017.
 */
public class DcuData {

    private static final Logger LOGGER = LoggerFactory.getLogger(DcuData.class);
    public static final String ARCHIVE_EXT = ".tar.gz";

    private static class Data {
        private EmConfigProperties configProperties;
        private RoleData[] roleData;
    }

    private final static Map<String, Data> data = new HashMap<>();

    private final Iterator<Path> dcuFolderIterator;
    private Path tmpFolder;
    private Path dcuDataFile;

    public static void main(String[] args) {
        try {
            DcuData dd = new DcuData("c:\\replay\\test");
            while (dd.hasNextDcu())
                dd.processNextDcu();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DcuData(String dcuFolder) throws IOException {
        DirectoryStream<Path> dcuFolderStream = Files.newDirectoryStream(Paths.get(dcuFolder));

        HashSet<Path> set = new HashSet<>();
        for (Path item : dcuFolderStream) {

            if (Files.isDirectory(item) || !item.getFileName().toString().endsWith(ARCHIVE_EXT)) {
                continue;
            }
            set.add(item);
        }
        dcuFolderIterator = set.iterator();
    }

    public boolean hasNextDcu() {
        return dcuFolderIterator.hasNext();
    }

    public void processNextDcu() throws IOException {
        if (!this.dcuFolderIterator.hasNext()) {
            return;
        }

        this.tmpFolder = Files.createTempDirectory("dcu");
        this.dcuDataFile = this.dcuFolderIterator.next();
        final String key = this.dcuDataFile.getFileName().toString();
        LOGGER.info("Processing {} archive.", this.dcuDataFile);
        if (!data.containsKey(key)) {
            final String dataFilePath = this.dcuDataFile.toFile().getAbsolutePath();
            try (TarArchiveInputStream tarStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(dataFilePath)))) {
                Path configFile = null;
                Path listingFile = null;
                RoleData[] roleData = null;
                ArchiveEntry tarEntry;
                boolean dataReady = false;
                while ((tarEntry = tarStream.getNextEntry()) != null) {
                    if (tarEntry.isDirectory()) {
                        continue;
                    }
                    // file tar entry
                    final DcuManagedFile dcuManagedFile = DcuManagedFile.fromFilePath(tarEntry.getName());
                    switch (dcuManagedFile) {
                        case EM_CONFIG:
                            LOGGER.info("Detected {} file.", dcuManagedFile);
                            final ZipInputStream zcis = new ZipInputStream(tarStream);
                            ZipEntry zcEntry;
                            while ((zcEntry = zcis.getNextEntry()) != null) {
                                if (DcuManagedFile.fromFilePath(zcEntry.getName()) == DcuManagedFile.IEM_PROPERTIES) {
                                    configFile = DcuManagedFile.IEM_PROPERTIES.toPathAt(this.tmpFolder, zcis);
                                    break;
                                }
                            }
                            break;
                        case EM_FILE_LIST:
                            LOGGER.info("Detected {} file.", dcuManagedFile);
                            listingFile = DcuManagedFile.EM_FILE_LIST.toPathAt(this.tmpFolder, tarStream);
                            break;
                        case DISCOVERY:
                            LOGGER.info("Detected {} file.", dcuManagedFile);
                            roleData = new Gson().fromJson(new InputStreamReader(tarStream), RoleData[].class);
                            break;
                        case NOT_RELEVANT:
                            break;
                    }
                    if (configFile != null && listingFile != null && roleData != null) {
                        LOGGER.info("Collected all core files for archive {}.", this.dcuDataFile);
                        dataReady = true;
                        break;
                    }
                }
                if (!dataReady) {
                    throw new IllegalArgumentException("One of the core files is missing. Need to have " + Arrays.asList(DcuManagedFile.values()));
                }
                Data d = new Data();
                d.roleData = roleData;
                d.configProperties = new EmConfigProperties(configFile, listingFile);
                data.put(key, d);
            }
        }

        FileUtils.deleteDirectory(tmpFolder.toFile());
    }

    public Path getDcuDataFile() {
        return dcuDataFile;
    }

    public EmConfigProperties getConfigProperties() {
        return data.get(dcuDataFile.getFileName().toString()).configProperties;
    }

    public Collection<RoleData> getRoleData() {
        return Arrays.asList(data.get(dcuDataFile.getFileName().toString()).roleData);
    }

    public static class EmConfigProperties {
        private static final String PROPERTY_CLUSTER_MODE = "introscope.enterprisemanager.clustering.mode";
        private static final String PROPERTY_CLUSTER_HOST = "introscope.enterprisemanager.clustering.login.em%d.host";
        private static final String PROPERTY_SMARTSTOR_DIR = "introscope.enterprisemanager.smartstor.directory";
        private static final String PROPERTY_SMARTSTOR_METADATA_DIR = "introscope.enterprisemanager.smartstor.directory.metadata";
        private static final String PROPERTY_SMARTSTOR_ARCHIVE_DIR = "introscope.enterprisemanager.smartstor.directory.archive";
        private static final String PROPERTY_BASELINE_FILE = "introscope.enterprisemanager.baseline.database";
        private static final String PROPERTY_TRACES_DIR = "introscope.enterprisemanager.transactionevents.storage.dir";
        private static final String PROPERTY_THREADDUMP_DIR = "introscope.enterprisemanager.threaddump.storage.dir";
        private final ClusterMode clusterMode;
        private final Collection<String> collectorHosts = new HashSet<>();
        private final String emDir;
        private final String smartStorDir;
        private final String tracesDir;
        private final String smartStorMetadataDir;
        private final String smartStorArchiveDir;
        private final String baseLineFile;
        private final String threadDumpDir;
        private final String version;

        public EmConfigProperties(final Path configFile, final Path fileListingFile) {
            final Properties properties = new Properties();
            try (InputStream is = Files.newInputStream(configFile)) {
                properties.load(is);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            String version = null;
            try (BufferedReader br = Files.newBufferedReader(configFile)) {
                String line;
                while ((line = br.readLine()) != null) {
                    int index = line.indexOf("Release");
                    if (index > -1) {
                        version = line.substring(index + 8);
                        break;
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            String emDir = null;
            try (BufferedReader br = Files.newBufferedReader(fileListingFile)) {
                String line = br.readLine();
                if (line.endsWith(":")) {
                    emDir = line.substring(0, line.length() - 1);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            this.clusterMode = ClusterMode.from(properties.getProperty(PROPERTY_CLUSTER_MODE));
            for (int i = 0; i < 10; i++) {
                final String property = properties.getProperty(String.format(PROPERTY_CLUSTER_HOST, i));
                if (StringUtils.isNotBlank(property)) {
                    this.collectorHosts.add(property);
                }
            }
            this.emDir = StringUtils.defaultIfEmpty(emDir, "/opt/automation/em");
            this.tracesDir = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_TRACES_DIR), "traces");
            this.smartStorDir = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_SMARTSTOR_DIR), "data");
            this.smartStorMetadataDir = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_SMARTSTOR_METADATA_DIR), "data/metadata");
            this.smartStorArchiveDir = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_SMARTSTOR_ARCHIVE_DIR), "data/archive");
            this.baseLineFile = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_BASELINE_FILE), "data/variance.db");
            this.threadDumpDir = StringUtils.defaultIfEmpty(properties.getProperty(PROPERTY_THREADDUMP_DIR), "threaddumps");
            this.version = StringUtils.defaultIfEmpty(version, "10.3.0.16");
        }

        public ClusterMode getClusterMode() {
            return this.clusterMode;
        }

        public Collection<String> getCollectorHosts() {
            return this.collectorHosts;
        }

        public String getSmartStorDir() {
            return this.smartStorDir;
        }

        public boolean isExternalSmartStor() {
            return isExternal(this.smartStorDir);
        }

        private boolean isExternal(final String path) {
            return Paths.get(path).isAbsolute() && !Paths.get(path).startsWith(getEmDir());
        }

        public String getTracesDir() {
            return this.tracesDir;
        }

        public boolean isExternalTraces() {
            return isExternal(this.tracesDir);
        }

        public String getEmDir() {
            return this.emDir;
        }

        public String getVersion() {
            return this.version;
        }

        public String getSmartStorMetadataDir() {
            return this.smartStorMetadataDir;
        }

        public String getSmartStorArchiveDir() {
            return this.smartStorArchiveDir;
        }

        public String getBaseLineFile() {
            return this.baseLineFile;
        }

        public String getThreadDumpDir() {
            return this.threadDumpDir;
        }

        public enum ClusterMode {
            StandAlone, CDV, Collector, MOM;

            private static ClusterMode from(final String value) {
                for (final ClusterMode clusterMode : values()) {
                    if (clusterMode.name().toLowerCase().equals(value.toLowerCase())) {
                        return clusterMode;
                    }
                }
                throw new UnknownEnumTypeException(value, values());
            }
        }
    }

    private enum DcuManagedFile {
        EM_CONFIG("em-config.zip"),
        EM_FILE_LIST("em-file-listing.txt"),
        DISCOVERY("apm-discovery.json"),
        IEM_PROPERTIES("IntroscopeEnterpriseManager.properties"),
        NOT_RELEVANT("");

        private final String fileName;

        DcuManagedFile(final String fileName) {
            this.fileName = fileName;
        }

        static DcuManagedFile fromFilePath(final String filePath) {
            Args.notBlank(filePath, "File path cannot be blank");
            for (final DcuManagedFile managedFile : values()) {
                if (managedFile != NOT_RELEVANT && filePath.endsWith(managedFile.fileName)) {
                    return managedFile;
                }
            }
            return NOT_RELEVANT;
        }

        private String getFileName() {
            return this.fileName;
        }

        private Path toPathAt(final Path folder, final InputStream is) throws IOException {
            final Path path = Paths.get(folder.toString(), getFileName());
            try (OutputStream fos = Files.newOutputStream(path, CREATE)) {
                IOUtils.copy(is, fos);
            }
            return path;
        }
    }
}
