package com.ca.apm.systemtest.fld.server.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.activemq.util.ByteArrayInputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author sinal04
 *
 */
public class ZipFileEditTest {
    private static final String APPEND_FILE_CONTENT = "Sound check in Bishkek";

    @Test
    public void testZipAddFile() throws IOException {
        File zipArchive = null;
        try {
            //Create a test zip archive containing one single text file 1.txt
            zipArchive = createTestZipArchive();
            System.out
                .println("Printing out zip archive contents before adding a new entry to it.");

            //Check we have only one item inside the archive
            int archiveEntriesNumberBefore = printOutZipContent(zipArchive);
            Assert.assertEquals(1, archiveEntriesNumberBefore);

            //Add another text file called 2.txt into existing zip archive
            try (FileSystem fs = FileSystems.newFileSystem(Paths.get(zipArchive.toURI()), this.getClass().getClassLoader())) {
                Path newFilePath = fs.getPath("2.txt");
                Files.copy(new ByteArrayInputStream(APPEND_FILE_CONTENT.getBytes("UTF-8")),
                    newFilePath);
            }

            //Check we have now two files inside the archive.
            int archiveEntriesNumberAfter = printOutZipContent(zipArchive);
            Assert.assertEquals(2, archiveEntriesNumberAfter);
        } finally {
            if (zipArchive != null && zipArchive.exists()) {
                if (!zipArchive.delete()) {
                    System.err.println("Failed to delete file '" + zipArchive.getAbsolutePath()
                        + "'");
                }
            }
        }

    }

    private File createTestZipArchive() throws IOException {
        File zipFile = File.createTempFile("test-preconf-agent-", ".zip");

        try (ZipOutputStream testZipOS = new ZipOutputStream(new FileOutputStream(zipFile));) {
            ZipEntry fileEntry = new ZipEntry("1.txt");
            testZipOS.putNextEntry(fileEntry);
            testZipOS.write("FooBar in Kandahar".getBytes());
            testZipOS.closeEntry();
        }

        return zipFile;
    }

    private int printOutZipContent(File zipArchive) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipArchive)) {
            System.out.println("*****************************************");
            System.out.println("Zip archive '" + zipFile.getName() + "':");

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println(" * " + entry.getName());
            }
            return zipFile.size();
        }

    }

}
