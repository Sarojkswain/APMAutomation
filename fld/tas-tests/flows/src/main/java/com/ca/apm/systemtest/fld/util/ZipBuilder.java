package com.ca.apm.systemtest.fld.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.util.FileCopyUtils;

/*
 * code based on
 * plugin-api/src/main/java/com/ca/apm/systemtest/fld/plugin/util/ZipBuilder.java
 * 
 * TODO - consider moving this class to some 'common' module
 */
public class ZipBuilder implements Closeable {

    private ZipOutputStream zip;

    /**
     * Construct a ZipBuilder which will create its output using the destination file
     * 
     * @param destination
     */
    public ZipBuilder(File destination) throws IOException {
        this(new FileOutputStream(destination));
    }

    public ZipBuilder(OutputStream destination) throws IOException {
        zip = new ZipOutputStream(destination);
    }

    public ZipBuilder flush() throws IOException {
        zip.flush();
        return this;
    }

    @Override
    public void close() throws IOException {
        zip.flush();
        zip.close();
    }

    public ZipBuilder addFolder(String name) throws IOException {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.closeEntry();
        return this;
    }

    public ZipBuilder addFile(String name, String txt) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(txt.getBytes("US-ASCII"));
        zip.flush();
        zip.closeEntry();
        return this;
    }

    public ZipBuilder addFile(String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(data);
        zip.flush();
        zip.closeEntry();
        return this;
    }

    public ZipBuilder addFile(String name, InputStream in) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        FileCopyUtils.copy(in, zip);
        zip.flush();
        zip.closeEntry();
        return this;
    }

}
