package com.ca.apm.systemtest.fld.hammond;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Created by jirji01 on 6/22/2017.
 */
public class DcuFileHandler {
    private final Configuration cfg;

    private String dcuZipPath;

    private Path tmpFolder;
    private Path dataFolder;

    private long endTimestamp;

    public DcuFileHandler(Configuration cfg) {
        this.cfg = cfg;

        tmpFolder = Paths.get(cfg.getSmartstorFolder()).toAbsolutePath().getParent();
        dataFolder = Paths.get(cfg.getDataFolder());
    }

    public void extractData() throws IOException {
        Path dcaArchive = Paths.get(cfg.getDcu());

        if (Files.isDirectory(tmpFolder)) {
            FileUtils.deleteDirectory(tmpFolder.toFile());
        }

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(dcaArchive))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {

                if (zipEntry.isDirectory()) {
                    continue;
                }

                String fileName = zipEntry.getName();
                Path newFile;

                if (fileName.contains("apm-data/em/traces/")) {
                    String[] segments = fileName.split("apm-data/em/traces/");
                    newFile = Paths.get(cfg.getTracesFolder(), segments[1]);
                    dcuZipPath = segments[0];
                } else if (fileName.contains("apm-data/traces/")) {
                    String[] segments = fileName.split("apm-data/traces/");
                    newFile = Paths.get(cfg.getTracesFolder(), segments[1]);
                    dcuZipPath = segments[0];
                } else if (fileName.contains("apm-data/em/data/")) {
                    String[] segments = fileName.split("apm-data/em/data/");
                    newFile = Paths.get(cfg.getSmartstorFolder(), segments[1]);
                    if (fileName.endsWith("000.spool")) {
                        endTimestamp = Long.parseLong(newFile.getFileName().toString().replaceFirst(".spool", ""));
                    }
                    dcuZipPath = segments[0];
                } else if (fileName.contains("apm-data/smartstor/")) {
                    String[] segments = fileName.split("apm-data/smartstor/");
                    newFile = Paths.get(cfg.getSmartstorFolder(), segments[1]);
                    if (fileName.endsWith("000.spool")) {
                        endTimestamp = Long.parseLong(newFile.getFileName().toString().replaceFirst(".spool", ""));
                    }
                    dcuZipPath = segments[0];
                } else if (fileName.contains("apm-data/smartstor_archive/")) {
                    String[] segments = fileName.split("apm-data/smartstor_archive/");
                    newFile = Paths.get(cfg.getSmartstorFolder(), "archive", segments[1]);
                    dcuZipPath = segments[0];
                } else {
                    continue;
                }

                if (Files.notExists(newFile.getParent())) {
                    Files.createDirectories(newFile.getParent());
                }
                try (OutputStream fos = Files.newOutputStream(newFile, CREATE )) {
                    IOUtils.copy(zis, fos);
                }
            }
        }
    }

    private void updateDcuZipPath(String path) {

    }

    public void packData() throws IOException {

        String rootDir = dataFolder.toString() + tmpFolder.getFileSystem().getSeparator();
        Path hammondZipPath = Paths.get(dataFolder.getParent().toString(), "hammond-data.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(hammondZipPath, CREATE, TRUNCATE_EXISTING))) {

            Files.walkFileTree(dataFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {

                        String fileString = file.toAbsolutePath().toString();

                        if (fileString.contains("hammond-data" + file.getFileSystem().getSeparator() + "edges")) {
                            return FileVisitResult.CONTINUE;
                        }

                        ZipEntry entry = new ZipEntry(fileString
                                .replace(rootDir, "")
                                .replaceAll("\\\\", "/"));
                        zip.putNextEntry(entry);

                        try (InputStream in = Files.newInputStream(file, READ)) {
                            IOUtils.copy(in, zip);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        Map<String, String> zip_properties = new HashMap<>();
        zip_properties.put("create", "false");
        zip_properties.put("encoding", "UTF-8");
        // Create ZIP file System
        URI uri = URI.create("jar:" + Paths.get(cfg.getDcu()).toUri());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, zip_properties)) {
            Path zipHammondPath = zipfs.getPath(dcuZipPath, hammondZipPath.getFileName().toString());
            Files.deleteIfExists(zipHammondPath);
            Files.copy(hammondZipPath, zipHammondPath);
            Files.deleteIfExists(hammondZipPath);
        }
    }

    public void cleanTmpFiles() throws IOException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Files.isDirectory(tmpFolder)) {
                    FileUtils.deleteDirectory(tmpFolder.toFile());
                }
            } catch (IOException e) {}
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Files.isDirectory(dataFolder)) {
                    FileUtils.deleteDirectory(dataFolder.toFile());
                }
            } catch (IOException e) {}
        }));
    }

    public boolean isRocksdb() {
        Path metadataDir = Paths.get(cfg.getSmartstorFolder(), "metadata");
        return Files.isDirectory(metadataDir);
    }

    public long getEndTime() {
        return endTimestamp;
    }
}
