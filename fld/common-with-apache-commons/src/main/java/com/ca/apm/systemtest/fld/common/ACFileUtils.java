package com.ca.apm.systemtest.fld.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class groups methods dealing with files.
 * This class is intentionally not named FileUtils
 * because it uses Apache Commons' FileUtils class.
 * <p>
 * <p>
 * Created by haiva01 on 11.12.2014.
 */
public class ACFileUtils {
    private static final Logger log = LoggerFactory.getLogger(ACFileUtils.class);

    /**
     * Says if the program runs on Windows operating system.
     * 
     * @return
     */
    public static boolean isWindowsOS() {
        String osNameProp = System.getProperty("os.name").toLowerCase();
        if (log.isDebugEnabled()) {
            log.debug("Operating System: ", osNameProp);
        }
        return osNameProp.indexOf("win") != -1;
    }
    
    /**
     * Unpacks a ZIP | JAR | CPIO | TAR | AR | ARJ | 7z file to a desired folder.
     *
     * @param packedFile input zip/tar file
     * @param outputDir  output folder
     * @throws IOException
     * @throws ArchiveException
     * @throws FileNotFoundException
     */
    public static void unpackFile(File packedFile, File outputDir) throws ArchiveException,
        FileNotFoundException, IOException {
        byte[] buffer = new byte[1024];
        try (ArchiveInputStream ais =
                 new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(
                     new FileInputStream(packedFile)))) {
            // create output directory is not exists
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            // get the zipped file list entry
            ArchiveEntry ze = ais.getNextEntry();
            while (ze != null) {
                if (ze.isDirectory()) {
                    File dest = new File(outputDir, ze.getName());
                    if (!dest.exists()) {
                        dest.mkdirs();
                    }
                } else {
                    String fileName = ze.getName();
                    File newFile = new File(
                        outputDir.getAbsolutePath() + File.separator + fileName);
                    // create all non exists folders
                    // else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = ais.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                ze = ais.getNextEntry();
            }
        }
    }

    /**
     * This function deletes files specified by glob pattern.
     * The function does not descend into directories.
     *
     * @param dir  directory to search
     * @param glob file glob pattern to use
     */
    public static void forceDeleteFilesByGlob(File dir, String glob) {
        Collection<File> files = globFiles(dir, glob);
        for (File file : files) {
            try {
                FileUtils.forceDelete(file);
                log.debug("Deleted file {}.", file);
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Failed to delete file {1}. Exception: {0}", file);
            }
        }
    }


    /**
     * This function find files specified by glob pattern.
     * The function does not descend into directories.
     *
     * @param dir  directory to search
     * @param glob file glob pattern to use
     */
    public static Collection<File> globFiles(File dir, String glob) {
        IOFileFilter filter = new WildcardFileFilter(glob, IOCase.SYSTEM);
        return FileUtils.listFiles(dir, filter, null);
    }


    /**
     * This function is a wrapper around File.createTempFile().
     *
     * @param prefix temporary file prefix
     * @param suffix temporary file suffix
     * @param dir    directory for the temporary file
     * @return temporary file
     */
    public static File generateTemporaryFile(String prefix, String suffix, File dir) {
        try {
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create temporary file {1}XXXXXXXX{2} in {3}. Exception: {0}",
                prefix, suffix, dir);
        }
    }


    /**
     * Create temporary directory.
     *
     * @param prefix directory name prefix
     * @return temporary directory
     */
    public static File createTemporaryDirectory(String prefix) {
        try {
            return java.nio.file.Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create temporary directory. Exception: {0}");
        }
    }


    /**
     * Recursively search for file in directories and subdirectories in path
     *
     * @param directory
     * @param searchFile
     */
    public static Path searchForFileTree(File directory, String searchFile) {

        Path path = null;
        try {
            Path startSearchDir = FileSystems.getDefault().getPath(directory.toString());
            path = java.nio.file.Files
                .walkFileTree(startSearchDir, new FldSimpleFileVisitor(searchFile));
        } catch (IOException e) {
            log.error("Error during searching: {} file in tree", searchFile);
        }

        return path;

    }


    /**
     * @param file file name to search for in PATH
     * @return Path to found file or null
     */
    public static Path findFileInPathEnv(String file) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }

        final FileSystem fs = FileSystems.getDefault();
        final List<String> dirs = Arrays.asList(pathEnv.split(";"));
        for (String dirStr : dirs) {
            Path filePath = fs.getPath(dirStr, file);
            if (java.nio.file.Files.exists(filePath)) {
                filePath = filePath.normalize();
                log.debug("Found {} as {}.", file, filePath);
                return filePath;
            }
        }

        log.debug("{} not found in PATH.", file);
        return null;
    }

    public static void copy(String inputPath, String outputPath)
        throws FileNotFoundException,
        IOException {
        FileUtils.copyFile(new File(inputPath), new File(outputPath));
    }

    public static void replace(String filePath, String regexPattern,
        String replacement) throws FileNotFoundException, IOException {

        // Since we are reading the whole file into memory,
        // this will run out of memory if the file specified is too big.
        File contentFile = new File(filePath);
        String content = FileUtils.readFileToString(contentFile);

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(content);

        content = matcher.replaceAll(replacement);

        FileUtils.writeStringToFile(contentFile, content);

    }


    /**
     * Copy files from a given directory into another directory whose name will
     * be suffixed by index. Index will be set in incremental order. for e.g.:
     * files in dir named c:/agentlogs will be copied to c:/agentlogs.1
     *
     * @throws IOException
     */
    public static void copyIntoIndexedDir(File dir) throws IOException {

        // File dir = new File(dirPath);

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException(dir + "is not a directory");
        }

        // pattern to match for dir name having index suffixed
        final String indexFilePattern = "(\\Q" + dir.getName() + "\\E)\\.([0-9]*)";
        int index = 0;
        String[] filesWithIndexInName = dir.getParentFile().list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                Matcher m = Pattern.compile(indexFilePattern).matcher(name);
                return m.matches();
            }
        });

        if (filesWithIndexInName.length > 0) {
            // get highest index among available files
            for (String fileName : filesWithIndexInName) {
                Matcher m = Pattern.compile(indexFilePattern).matcher(fileName);
                // TODO: Check result?
                m.matches();

                try {
                    if (Integer.valueOf(m.group(2)) > index) {
                        index = Integer.valueOf(m.group(2));
                    }
                } catch (NumberFormatException e) {
                    // // log it, and move on to next
                    log.error(e.getMessage(), e);
                }
            }

            ++index;
        } else {
            // index for very first copy
            index = 1;
        }

        if (dir.exists()) {
            // move files
            deepCopy(dir, new File(dir.getParent(), dir.getName() + "." + index));
            // delete logs dir after moving
            FileUtils.forceDelete(dir);
        }
    }

    public static void deepCopy(File source, File destination) throws IOException {

        if (source.isDirectory()) {

            // make sure that the destination dir is not an existing file
            if (destination.exists() && !destination.isDirectory()) {
                throw new IOException(
                    "Could not copy directory [" + source.getPath() + "]. The target file ["
                        + destination.getPath() + "] already exists.");
            }

            // make sure the destination dir exists
            destination.mkdirs();

            File[] children = source.listFiles();
            if (children == null) {
                throw ErrorUtils.logErrorAndThrowException(log, "Source {0} is empty.",
                    source.getAbsolutePath());
            }

            for (File child : children) {
                // create a destination file with the same name
                // as the source child file
                File childDestination = new File(destination.getPath(),
                    child.getName());
                deepCopy(child, childDestination);
            }

        } else {
            FileUtils.copyFile(source, destination);
        }

    }

}
