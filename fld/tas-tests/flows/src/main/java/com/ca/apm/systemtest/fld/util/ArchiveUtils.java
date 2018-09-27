package com.ca.apm.systemtest.fld.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnixStat;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarLongFileMode;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.components.io.resources.AbstractPlexusIoResource;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import javax.annotation.Nonnull;

import static com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression.GZIP;

/**
 * @author haiva01
 */
public final class ArchiveUtils {
    public static final String DOT_DUMMY = ".dummy";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveUtils.class);
    private static final String DUMMY_CONTENTS
        = "This file is automatically created to ensure that the archive creation succeeds.\n"
        + "It may be deleted if you so desire.\n";

    public enum ArchiveType {
        TAR, ZIP, JAR
    }

    public enum ArchiveCompression {
        NONE, DEFAULT, GZIP, BZIP2
    }

    public static class ArchiveEntry {
        /**
         * Prefix (directory) in archive where given files will be archived.
         */
        private String prefix;
        /**
         * Working directory against which paths/masks will be resolved.
         */
        private String directory;
        /**
         * Files matching these masks will be included in created archive.
         */
        private String[] includes;
        /**
         * Files matching these masks will be excluded from created archive.
         */
        private String[] excludes;
        /**
         * Case sensitivity.
         */
        private boolean caseSensitive = false;
        /**
         * Archive empty directories.
         */
        private boolean includeEmptyDirs = true;

        /**
         * Create archive entry to archive all of directory's contents directly into archive.
         *
         * @param directory directory to archive
         */
        public ArchiveEntry(String directory) {
            this.directory = directory;
        }

        /**
         * Create archive entry to archive contents of directory and put its files into given
         * prefix in the archive.
         *
         * @param directory directory to archive
         * @param prefix prefix of directory's contents in created archive
         */
        public ArchiveEntry(String directory, String prefix) {
            this(directory);
            this.prefix = prefix;
        }

        public ArchiveEntry(String directory, String prefix, String[] includes, String[] excludes) {
            this(directory, prefix);
            this.includes = includes;
            this.excludes = excludes;
        }

        public static ArchiveEntry directory(String directory) {
            return new ArchiveEntry(directory);
        }

        public static ArchiveEntry directoryIntoPrefix(String directory, String prefix) {
            return new ArchiveEntry(directory, prefix);
        }

        public static ArchiveEntry singleFile(String fileStr) {
            return singleFileIntoPrefix(fileStr, null);
        }

        public static ArchiveEntry singleFileIntoPrefix(String fileStr, String prefix) {
            File file = new File(fileStr);
            return new ArchiveEntry(file.getParentFile().getAbsolutePath(), prefix,
                new String[]{file.getName()}, null);
        }

        public String getPrefix() {
            return prefix;
        }

        public ArchiveEntry setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public String getDirectory() {
            return directory;
        }

        public ArchiveEntry setDirectory(String directory) {
            this.directory = directory;
            return this;
        }

        public String[] getIncludes() {
            return includes;
        }

        public ArchiveEntry setIncludes(String[] includes) {
            this.includes = includes;
            return this;
        }

        public String[] getExcludes() {
            return excludes;
        }

        public ArchiveEntry setExcludes(String[] excludes) {
            this.excludes = excludes;
            return this;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        public ArchiveEntry setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public boolean isIncludeEmptyDirs() {
            return includeEmptyDirs;
        }

        public ArchiveEntry setIncludeEmptyDirs(boolean includeEmptyDirs) {
            this.includeEmptyDirs = includeEmptyDirs;
            return this;
        }
    }

    public static TarArchiver getTarArchiver(ArchiveCompression compression) {
        TarArchiver tarArchiver = new TarArchiver();
        switch (compression) {
            default:
                LOGGER.warn("Unknown Tar compression method {}. Using {}.", compression, GZIP);
            case DEFAULT:
            case GZIP:
                tarArchiver.setCompression(TarArchiver.TarCompressionMethod.gzip);
                break;

            case BZIP2:
                tarArchiver.setCompression(TarArchiver.TarCompressionMethod.bzip2);
                break;

            case NONE:
                tarArchiver.setCompression(TarArchiver.TarCompressionMethod.none);
                break;
        }
        tarArchiver.setLongfile(TarLongFileMode.posix);
        return tarArchiver;
    }

    public static void zipSetCompression(AbstractZipArchiver zipArchiver,
        ArchiveCompression compression) {
        switch (compression) {
            case NONE:
                zipArchiver.setCompress(false);
                break;

            default:
                zipArchiver.setCompress(true);
                break;
        }
    }

    public static ZipArchiver getZipArchiver(ArchiveCompression compression) {
        ZipArchiver zipArchiver = new ZipArchiver();
        zipSetCompression(zipArchiver, compression);
        return zipArchiver;
    }

    public static JarArchiver getJarArchiver(ArchiveCompression compression) {
        JarArchiver zipArchiver = new JarArchiver();
        zipSetCompression(zipArchiver, compression);
        return zipArchiver;
    }

    public static AbstractArchiver getArchiver(ArchiveType type, ArchiveCompression compression) {
        switch (type) {
            case TAR:
                return getTarArchiver(compression);

            case ZIP:
                return getZipArchiver(compression);

            case JAR:
                return getJarArchiver(compression);

            default: {
                final String msg = MessageFormat.format("Unhandled archive type {0}.", type);
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    public static void processArchiveEntry(Archiver archiver, ArchiveEntry entry) {
        DefaultFileSet fileSet = DefaultFileSet.fileSet(new File(entry.getDirectory()));
        if (StringUtils.isNotBlank(entry.getPrefix())) {
            fileSet.setPrefix(entry.getPrefix());
        }
        fileSet.includeExclude(entry.getIncludes(), entry.getExcludes());
        fileSet.setCaseSensitive(entry.isCaseSensitive());
        fileSet.includeEmptyDirs(entry.isIncludeEmptyDirs());
        fileSet.setUsingDefaultExcludes(false);
        archiver.addFileSet(fileSet);
    }

    public static AbstractArchiver prepareArchiver(String archivePath, ArchiveType type,
        ArchiveCompression compression, Iterable<ArchiveEntry> archiveEntries) {
        AbstractArchiver archiver = getArchiver(type, compression);
        archiver.setDestFile(new File(archivePath).getAbsoluteFile());

        for (ArchiveEntry archiveEntry : archiveEntries) {
            try {
                processArchiveEntry(archiver, archiveEntry);
            } catch (Exception e) {
                final String msg = MessageFormat.format("Missing archive entry {0}.",
                    archiveEntry.getDirectory());
                LOGGER.error(msg);
            }
        }

        PlexusIoResource dummy = new AbstractPlexusIoResource(DOT_DUMMY, System.currentTimeMillis(),
            DUMMY_CONTENTS.length(), true, false, true) {

            /**
             * Creates an {@link InputStream}, which may be used to read
             * the files contents. This is useful, if the file selector
             * comes to a decision based on the files contents.
             *
             * <p> Please note that this InputStream is unbuffered. Clients should wrap this in a
             * BufferedInputStream or attempt reading reasonably large chunks (8K+).</p>
             */
            @Nonnull
            @Override
            public InputStream getContents() throws IOException {
                return IOUtils.toInputStream(DUMMY_CONTENTS);
            }

            /**
             * Returns an {@link URL}, which may be used to reference the
             * resource, if possible.
             *
             * @return An URL referencing the resource, if possible, or null.
             * In the latter case, you are forced to use {@link #getContents()}.
             */
            @Override
            public URL getURL() throws IOException {
                return null;
            }
        };
        archiver.addResource(dummy, DOT_DUMMY, UnixStat.DEFAULT_FILE_PERM);

        return archiver;
    }
}
