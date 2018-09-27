package com.ca.apm.systemtest.fld.util;

import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveEntry;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveType;

import org.codehaus.plexus.archiver.AbstractArchiver;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author haiva01
 */
public class ArchiveUtilsTest {
    private static final String TEST_ZIP = "test.zip";
    private static final String TEST_TAR = "test.tar.gz";

    @Test
    public void testEmptyZip() throws Exception {
        // Test that empty archive creation works.

        AbstractArchiver archiver = ArchiveUtils
            .prepareArchiver(TEST_ZIP, ArchiveType.ZIP, ArchiveCompression.DEFAULT,
                new ArrayList<ArchiveEntry>(0));
        assertNotNull(archiver);

        archiver.createArchive();

        File testArchiveFile = new File(TEST_ZIP);
        assertTrue(testArchiveFile.exists());
    }

    @Test
    public void testEmptyTar() throws Exception {
        // Test that empty archive creation works.

        AbstractArchiver archiver = ArchiveUtils
            .prepareArchiver(TEST_TAR, ArchiveType.TAR, ArchiveCompression.GZIP,
                new ArrayList<ArchiveEntry>(0));
        assertNotNull(archiver);

        archiver.createArchive();

        File testArchiveFile = new File(TEST_TAR);
        assertTrue(testArchiveFile.exists());
    }
}