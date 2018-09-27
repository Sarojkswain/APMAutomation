package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * This flow extracts data from DCU into installed EM and Hammond
 *
 * @author jirji01
 */
@Flow
public class RestoreDataFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreDataFlow.class);

    @FlowContext
    private RestoreDataFlowContext flowContext;

    private String dbData;

    @Override
    public void run() throws Exception {

//        backupConfiguration();

        unpackDcuData();

        restoreDatabase();

//        restoreConfiguration();

    }

    private void restoreDatabase() throws InterruptedException {

        if (new File(dbData).isDirectory()) {
            new Execution.Builder("java", LOGGER)
                    .args(Arrays.asList(
                            "-jar",
                            "data-pump.jar",
                            "--folder.data=" + dbData,
                            "--folder.scripts=" + flowContext.data.em + "/install/database-scripts",
                            "--db.host=" + flowContext.data.dbHost,
                            "--db.port=5432",
                            "--db.name=cemdb",
                            "--db.user=admin",
                            "--db.password=Lister@123",
                            "--db.release=" + flowContext.data.dbVersion
                    ))
                    .workDir(Paths.get(flowContext.data.sourceData).getParent().toFile())
                    .build().go();
        }
    }

    private void backupConfiguration() throws IOException {

        Path emConfigDir = Paths.get(flowContext.data.em, "config");
        Path emConfigBackupDir = Paths.get(flowContext.data.em, "config_backup");

        FileUtils.moveDirectory(emConfigDir.toFile(), emConfigBackupDir.toFile());
    }

    private void restoreConfiguration() throws IOException {
        Path emConfigDir = Paths.get(flowContext.data.em, "config");
        Path emConfigBackupDir = Paths.get(flowContext.data.em, "config_backup");

        Files.copy(Paths.get(emConfigBackupDir.toString(), "users.xml"), Paths.get(emConfigDir.toString(), "users.xml"), REPLACE_EXISTING);
        Files.copy(Paths.get(emConfigBackupDir.toString(), "tess-db-cfg.xml"), Paths.get(emConfigDir.toString(), "tess-db-cfg.xml"), REPLACE_EXISTING);

        // update IntroscopeEnterpriseManager.properties
        Path destFile = Paths.get(emConfigDir.toString(), "IntroscopeEnterpriseManager.properties");
        Properties dest = new Properties();
        BufferedReader reader = Files.newBufferedReader(destFile);
        dest.load(reader);
        IOUtils.closeQuietly(reader);
        Properties src = new Properties();
        reader = Files.newBufferedReader(Paths.get(emConfigBackupDir.toString(), "IntroscopeEnterpriseManager.properties"));
        src.load(reader);
        IOUtils.closeQuietly(reader);


        // collectors
        for (int i=1; i <= 10; i++) {
            setProperty(dest, src, "introscope.enterprisemanager.clustering.login.em" + i + ".host");
            setProperty(dest, src, "introscope.enterprisemanager.clustering.login.em" + i + ".port");
            setProperty(dest, src, "introscope.enterprisemanager.clustering.login.em" + i + ".publickey");
        }

        setProperty(dest, src, "introscope.saml.internalIdpUrl");
        setProperty(dest, src, "introscope.saml.idpUrl");
        setProperty(dest, src, "introscope.webview.default.url");

        BufferedWriter writer = Files.newBufferedWriter(destFile, TRUNCATE_EXISTING);
        dest.store(writer, "restored version");
        IOUtils.closeQuietly(writer);
    }

    private void setProperty(Properties dest, Properties src, String key) {
        String value = src.getProperty(key);
        if (value != null) {
            dest.setProperty(key, src.getProperty(key));
        }
    }

    private void unpackDcuData() throws IOException {

        dbData = Paths.get(flowContext.data.sourceData).getParent().toString() + "/db-data";

        try (TarArchiveInputStream tarStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(this.flowContext.data.sourceData)))) {
            ArchiveEntry tarEntry;

            while ((tarEntry = tarStream.getNextEntry()) != null) {
                if (tarEntry.isDirectory()) {
                    continue;
                }

                String fileName = tarEntry.getName();
                Path unzipPath = null;
                boolean unzip = false;
                if (fileName.contains("/apm-data/em/data/metadata/")) {
                    String[] segments = fileName.split("/apm-data/em/data/metadata/");
                    unzipPath = Paths.get(flowContext.data.smartstorMeta, segments[1]);
                } else if (fileName.contains("/apm-data/em/smartstor_metadata/")) {
                    String[] segments = fileName.split("/apm-data/em/smartstor_metadata/");
                    unzipPath = Paths.get(flowContext.data.smartstorMeta, segments[1]);
                } else if (fileName.contains("/apm-data/smartstor_metadata/")) {
                    String[] segments = fileName.split("/apm-data/smartstor_metadata/");
                    unzipPath = Paths.get(flowContext.data.smartstorMeta, segments[1]);
                } else if (fileName.contains("/apm-data/em/traces/")) {
                    String[] segments = fileName.split("/apm-data/em/traces/");
                    unzipPath = Paths.get(flowContext.data.traces, segments[1]);
                } else if (fileName.contains("variance.db")) {
                    unzipPath = Paths.get(flowContext.data.baseLine);
                } else if (fileName.contains("/apm-data/em/data/archive/")) {
                    String[] segments = fileName.split("/apm-data/em/data/archive/");
                    unzipPath = Paths.get(flowContext.data.smartstorArchive, segments[1]);
                } else if (fileName.contains("/apm-data/em/smartstor_archive/")) {
                    String[] segments = fileName.split("/apm-data/em/smartstor_archive/");
                    unzipPath = Paths.get(flowContext.data.smartstorArchive, segments[1]);
                } else if (fileName.contains("/apm-data/smartstor_archive/")) {
                    String[] segments = fileName.split("/apm-data/smartstor_archive/");
                    unzipPath = Paths.get(flowContext.data.smartstorArchive, segments[1]);
                } else if (fileName.contains("/apm-data/em/data/")) {
                    String[] segments = fileName.split("/apm-data/em/data/");
                    unzipPath = Paths.get(flowContext.data.smartstor, segments[1]);
                } else if (fileName.contains("/apm-data/em/smartstor/")) {
                    String[] segments = fileName.split("/apm-data/em/smartstor/");
                    unzipPath = Paths.get(flowContext.data.smartstor, segments[1]);
                } else if (fileName.contains("/apm-data/smartstor/")) {
                    String[] segments = fileName.split("/apm-data/smartstor/");
                    unzipPath = Paths.get(flowContext.data.smartstor, segments[1]);
                } else if (fileName.contains("/db-data/")) {
                    String[] segments = fileName.split("/db-data/");
                    unzipPath = Paths.get(dbData, segments[1]);
                } else if (fileName.endsWith("em-file-listing.txt")) {
                    unzipPath = Paths.get(Paths.get(flowContext.data.sourceData).getParent().toString(), "em-file-listing.txt");
                } else if (fileName.endsWith("hammond-data.zip")) {
                    unzipPath = Paths.get(flowContext.data.hammondData);
                    unzip = true;
                } else {
                    continue;
                }

                if (unzip) {
                    LOGGER.info("Unzipping " + fileName + " into " + unzipPath);
                    unzip(tarStream, unzipPath);
                } else {
                    LOGGER.info("Extracting " + fileName + " into " + unzipPath);
                    if (Files.notExists(unzipPath.getParent())) {
                        Files.createDirectories(unzipPath.getParent());
                    }
                    try (OutputStream fos = Files.newOutputStream(unzipPath, CREATE)) {
                        IOUtils.copy(tarStream, fos);
                    }
                }
            }
        }
    }

    private void unzip(InputStream zis, Path unzipPath) throws IOException {

        ZipInputStream ziis = new ZipInputStream(zis);
        ZipEntry ziEntry;
        while ((ziEntry = ziis.getNextEntry()) != null) {
            if (ziEntry.isDirectory()) {
                continue;
            }

            Path newFile = Paths.get(unzipPath.toAbsolutePath().toString(), ziEntry.getName().replace('\\', '/'));

            if (Files.notExists(newFile.getParent())) {
                Files.createDirectories(newFile.getParent());
            }

            try (OutputStream fos = Files.newOutputStream(newFile, CREATE, TRUNCATE_EXISTING)) {
                IOUtils.copy(ziis, fos);
            }
        }
    }
}
