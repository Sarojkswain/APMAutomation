/**
 * 
 */
package com.ca.apm.systemtest.fld.common.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.files.InsertPoint.Location;
import com.ca.apm.systemtest.fld.common.files.UpdateLinesOperation.OneLineUpdate;

/**
 * Utility class providing such utility methods for working with files as:
 *  - downloading a file by URL
 *  - searching files by names
 * 
 * @author KEYJA01
 *
 */
public class FileUtils {
    
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Downloads resource from the given URL to a local file named using the 
     * provided <code>prefix</code> and <code>suffix</code>, optionally into 
     * the provided <code>targetDir</code>.
     * 
     * <p/>
     * This method converts <code>resourceUrlStr</code> into URL and calls {@link #downloadResource(URL, String, String, File)}.
     * 
     * 
     * @param resourceUrlStr  resource URL
     * @param prefix       local resource prefix
     * @param suffix       local resource suffix
     * @param targetDir    target directory to download to; if <code>null</code> the resource is downloaded to a 
     *                     temporary folder created using the resource URL as its name
     * @return             downloaded resource file
     */
    public static File downloadResource(String resourceUrlStr, String prefix, String suffix, File targetDir) {
        URL resourceUrl;
        try {
            resourceUrl = new URL(resourceUrlStr);
        } catch (MalformedURLException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, 
                "Failed to create URL from string {1}. Exception: {0}", 
                resourceUrlStr, e);
        }
        return downloadResource(resourceUrl, prefix, suffix, targetDir);
    }
    
    /**
     * Downloads resource from the given URL to a local file named using the 
     * provided <code>prefix</code> and <code>suffix</code>, optionally into 
     * the provided <code>targetDir</code>.
     * 
     * @param resourceUrl  resource URL
     * @param prefix       local resource prefix
     * @param suffix       local resource suffix
     * @param targetDir    target directory to download to; if <code>null</code> the resource is downloaded to a 
     *                     temporary folder created using the resource URL as its name
     * @return             downloaded resource file
     */
    public static File downloadResource(URL resourceUrl, String prefix, String suffix, File targetDir) {
        log.info("Downloading resource from {}.", resourceUrl);

        ReadableByteChannel rbc = null;
        try {
            rbc = Channels.newChannel(resourceUrl.openStream());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e, 
                "Failed to open {1}. Exception: {0}", resourceUrl.toString());
        }

        try {
            if (targetDir == null) {
                targetDir = Files.createTempDirectory(resourceUrl.toString()).toFile();
            }
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create target directory for resource download. Exception: {0}");
        }
        
        File downloadedResourceFile = null;
        try {
            downloadedResourceFile = File.createTempFile(prefix, suffix, targetDir);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to create temporary file for resource download. Exception: {0}");
        }

        long downloadedBytes;
        try (FileOutputStream fos = new FileOutputStream(downloadedResourceFile)) {
            try {
                downloadedBytes = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Error during resource download. Exception: {0}");
            }

            log.debug("Transferred {} bytes.", downloadedBytes);
        } catch (FileNotFoundException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to open file {1} for resource download. Exception: {0}",
                downloadedResourceFile);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error closing file {1}. Exception: {0}", downloadedResourceFile);
        }

        log.info("Downloaded {} bytes into {}.", downloadedBytes, downloadedResourceFile);
        return downloadedResourceFile;
    }
    
    /**
     * Reads the contents of the src file into memory, and then inserts the contents of toInsert at all
     * of the specified {@link InsertPoint}s. The the modified contents is written to the tgt file. 
     * @param src
     * @param tgt
     * @param toInsert
     * @param insertPoints
     * @throws IOException
     */
    public static void insertIntoFile(File src, File tgt, String[] toInsert, InsertPoint[] insertPoints) throws IOException {
        List<String> lines = Files.readAllLines(src.toPath(), Charset.defaultCharset());
        
        for (InsertPoint ip: insertPoints) {
            Pattern p = Pattern.compile(ip.getSearchText());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    int idx = i;
                    if (ip.getLocation() == Location.After) {
                        idx++;
                    }
                    for (String s: toInsert) {
                        lines.add(idx++, s);
                    }
                    break;
                }
            }
        }
        Files.write(tgt.toPath(), lines, Charset.defaultCharset());
    }

    /**
     * 
     * @param src
     * @param tgt
     * @param toInsert
     * @param insertPoints
     * @throws IOException
     */
    public static void insertIntoFileSkipIfAlreadyIncludes(File src, File tgt, 
                                                           String[] toInsert, 
                                                           InsertPoint[] insertPoints) throws IOException {
        List<String> lines = Files.readAllLines(src.toPath(), Charset.defaultCharset());
        for (InsertPoint ip: insertPoints) {
            if (ip.getLocation() == Location.EndOfFile) {
                boolean alreadyIncludes = checkInsertLinesAlreadyIncluded(toInsert, lines.size(), lines, 
                    InsertPoint.Location.Before);
                if (!alreadyIncludes) {
                    for (String s : toInsert) {
                        lines.add(s);
                    }
                }
            } else {
                Pattern p = Pattern.compile(ip.getSearchText());
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        int idx = i;
                        if (ip.getLocation() == Location.After) {
                            idx++;
                        }
                        boolean alreadyIncludes = checkInsertLinesAlreadyIncluded(toInsert, idx, 
                            lines, ip.getLocation());
                        if (alreadyIncludes) {
                            break;
                        } else {
                            for (String s : toInsert) {
                                lines.add(idx++, s);
                            }
                            break;
                        }
                    }
                }
            }
        }
        Files.write(tgt.toPath(), lines, Charset.defaultCharset());
    }

    public static boolean checkInsertLinesAlreadyIncluded(String[] insertLines, int insertInd, 
                                                          List<String> targetLines, Location location) {
        if (insertLines == null || insertLines.length == 0 || 
            targetLines == null || location == null || 
            insertLines.length > targetLines.size() ||
            insertInd < 0 || insertInd > targetLines.size()) {
            return false;
        }

        int targetSize = targetLines.size();
        int insertSize = insertLines.length;

        switch (location) {
            case Before:
                int startInd = insertInd - insertSize;
                if (startInd < 0) {
                    return false;
                }
                int k = 0;
                for (int i = startInd; i < insertInd; i++) {
                    if (!targetLines.get(i).equals(insertLines[k++])) {
                        return false;
                    }
                }
                return true;
            case After: 
                if (insertSize > targetSize - insertInd) {
                    return false;
                }
                k = insertInd;
                for (String insertLine : insertLines) {
                    if (!insertLine.equals(targetLines.get(k++))) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    protected static boolean includes(String[] toInsert, int idx, List<String> lines,
        Location location) {
        if (idx < 0 || toInsert == null || lines == null || toInsert.length == 0 || lines.isEmpty()
            || (toInsert.length > lines.size()) || location == null) {
            return false;
        }
        switch (location) {
            case Before: {
                if ((idx >= lines.size()) || ((toInsert.length - 1) > idx)) {
                    return false;
                }
                for (int i = (toInsert.length - 1); i >= 0; i--) {
                    idx--;
                    if (idx < 0) {
                        return false;
                    }
                    if (!toInsert[i].equals(lines.get(idx))) {
                        return false;
                    }
                }
                return true;
            }
            case After: {
                if ((idx >= lines.size()) || (toInsert.length > (lines.size() - idx))) {
                    return false;
                }
                for (String s : toInsert) {
                    if (!s.equals(lines.get(idx))) {
                        return false;
                    }
                    idx++;
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static void updateLinesInFile(File src, File tgt,
        UpdateLinesOperation... updateLinesOperations) throws IOException {
        List<String> lines = Files.readAllLines(src.toPath(), Charset.defaultCharset());
        if (updateLinesOperations == null || updateLinesOperations.length == 0) {
            Files.write(tgt.toPath(), lines, Charset.defaultCharset());
            return;
        }
        for (UpdateLinesOperation updateLinesOperation : updateLinesOperations) {
            Pattern searchTextPattern = updateLinesOperation.getSearchTextPattern();
            List<OneLineUpdate> oneLineUpdates = updateLinesOperation.getLineUpdates();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher m = searchTextPattern.matcher(line);
                if (m.matches()) {
                    for (UpdateLinesOperation.OneLineUpdate oneLineUpdate : oneLineUpdates) {
                        switch (oneLineUpdate.getUpdateMethod()) {
                            case ADD_TO_BEGINNING: {
                                lines.set(i++, oneLineUpdate.getUpdateText() + line);
                                break;
                            }
                            case ADD_TO_END: {
                                lines.set(i++, line + oneLineUpdate.getUpdateText());
                                break;
                            }
                            case REWRITE: {
                                lines.set(i++, oneLineUpdate.getUpdateText());
                                break;
                            }
                            case CLEAR: {
                                lines.set(i++, "");
                                break;
                            }
                            case DELETE: {
                                lines.remove(i);
                                break;
                            }
                            default: {
                                lines.set(i++, oneLineUpdate.getUpdateText());
                                break;
                            }
                        }
                        if (i == lines.size()) {
                            break;
                        }
                        line = lines.get(i);
                    }
                    break;
                }
            }
        }
        Files.write(tgt.toPath(), lines, Charset.defaultCharset());
    }

    public static Path search(Path dir, final String name) {
        final List<Path> result = new ArrayList<>();
        
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file,
                        BasicFileAttributes attrs) throws IOException {
                    String fileString = file.getFileName().toString();

                    if (fileString.equalsIgnoreCase(name)) {
                        result.add(file);
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e)
                        throws IOException {
                    log.info("Visiting failed for {}", file);

                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (IOException e) {
            log.error("Got exception during file search", e);
        }
        
        return result.isEmpty() ? null : result.get(0);
    }

    public static Path searchAll(String name) {
        Path result = null;
        for (File f : File.listRoots()) {
            result = search(Paths.get(f.getAbsolutePath()), name);
            if (result != null)
                break;
        }
        return result;
    }

    /**
     * Reads and returns contents of the file found on the resource path specified by <code>filePath</code>. 
     * 
     * @param    filePath       resource file path
     * @param    classLoader    class loader for accessing resource file
     * @return                  file contents as a string
     * @throws   IOException    if an IO error happens
     */
    public static String readFileFromResourcePath(String filePath, ClassLoader classLoader) throws IOException {
        if (classLoader == null) {
            classLoader = FileUtils.class.getClassLoader();
        }
        try (Reader read = new InputStreamReader(classLoader.getResourceAsStream(filePath))) {
            char[] readBuf = new char[1024*10];
            int readLen = 0;
            StringBuilder strBuffer = new StringBuilder();
            while ((readLen = read.read(readBuf)) != -1) {
                strBuffer.append(readBuf, 0, readLen);
            }
            return strBuffer.toString();
        } 
    }

}
