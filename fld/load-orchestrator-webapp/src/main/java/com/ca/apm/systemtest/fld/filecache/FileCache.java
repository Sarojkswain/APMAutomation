package com.ca.apm.systemtest.fld.filecache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author KEYJA01
 *
 */
public interface FileCache {
    /**
     * Retrieves the {@link FileItem} for the specified file.  Callers should close the {@link InputStream} in the file item.
     * Calling this method automatically touches the last access date.
     * @param url
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public FileItem getFile(String url) throws FileNotFoundException, IOException;
    
    /**
     * Checks if a file is already in the cache.
     * @param touch If true, set the file access date to now, which will extend the time the file will be kept in the cache.
     * @return
     */
    public boolean checkFile(String url, boolean touch);
    
    /**
     * Inserts the file content
     * @param url
     * @param in
     */
    public void putFile(String url, InputStream in);
    
    /**
     * Removes stale entries from the cache. Also removes entries where the file has been manually deleted
     * from the file system.
     */
    public void pruneCache();
}
