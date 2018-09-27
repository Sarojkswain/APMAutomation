/**
 * 
 */
package com.ca.apm.systemtest.fld.filecache;

import java.io.InputStream;

/**
 * @author KEYJA01
 *
 */
public class FileItem {
    private long lastAccess = 0L;
    private String url;
    private long length;
    private InputStream inputStream;
    
    /**
     * 
     */
    public FileItem() {
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
