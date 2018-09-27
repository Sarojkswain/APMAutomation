/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ca.apm.systemtest.fld.filecache.FileCache;
import com.ca.apm.systemtest.fld.filecache.FileItem;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;

/**
 * Allows direct download of cached artifacts.
 * @author KEYJA01
 *
 */
@Controller
public class FileDownloadController {
    private Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    private FileCache fileCache;
    private HashSet<Download> currentDownloads = new HashSet<Download>();
    private HashMap<Download, Long> downloadPool = new HashMap<Download, Long>();
    
    private static int nextId = 0;
    class Download {
        final int id = nextId++;
        String url;
        DownloadStatus status = DownloadStatus.New;
        @Override
        public int hashCode() {
            return id;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Download) {
                Download dl2 = (Download) obj;
                return dl2.id == this.id;
            }
            return false;
        }
    }
    private enum DownloadStatus {
        New, Downloading, Downloaded, Failed
    }
    
    @RequestMapping("/filecache/download")
    public void handleDownload(@RequestParam(required=true) final String url, final HttpServletResponse response) throws IOException {
        boolean fileInCache = fileCache.checkFile(url, true);
        if (fileInCache) {
            FileItem item = fileCache.getFile(url);
            serveFile(item, response);
        } else {
            Download dl = getKeyForUrl(url);
            try {
                if (currentDownloads.contains(dl)) {
                    waitForDownload(dl, response);
                    FileItem item = fileCache.getFile(url);
                    serveFile(item, response);
                } else {
                    currentDownloads.add(dl);
                    // file was not in cache, so we start downloading it
                    downloadFile(dl);
                    
                    // notify other waiters (if any)
                    synchronized (dl) {
                        dl.notifyAll();
                    }
                    
                    // and finally serve the puppy if it was successful
                    if (dl.status == DownloadStatus.Downloaded) {
                        FileItem item = fileCache.getFile(url);
                        serveFile(item, response);
                    }
                }
            } finally {
                returnKeyToPool(dl);
            }
        }
    }
    
    
    
    private void downloadFile(Download dl) {
        dl.status = DownloadStatus.Downloading;
        try {
            URL netUrl = new URL(dl.url);
            File tmpFile = File.createTempFile("filedownload", "dat");
            tmpFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(tmpFile);
            FileCopyUtils.copy(netUrl.openStream(), out);
            
            // and then put it in the cache
            fileCache.putFile(dl.url, new FileInputStream(tmpFile));
            dl.status = DownloadStatus.Downloaded;
        } catch (Exception e) {
            final String msg = MessageFormat.format(
                "Failed to download {1}. Exception: {0}", e.getMessage(), dl.url);
            log.error(msg, e);
            dl.status = DownloadStatus.Failed;
        }
    }
    
    
    
    private void waitForDownload(final Download dl, HttpServletResponse response) {
        synchronized (dl) {
            try {
                dl.wait(1800000L);
            } catch (InterruptedException e) {
                final String msg = MessageFormat.format(
                    "Got interrupted. Exception: {0}", e.getMessage());
                log.error(msg, e);
            }
        }
    }


    private void serveFile(FileItem item, HttpServletResponse response) throws IOException {
        response.setContentLength((int) item.getLength());
        response.setStatus(200);
        ServletOutputStream out = response.getOutputStream();
        FileCopyUtils.copy(item.getInputStream(), out);
        out.flush();
    }
    
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(Exception ex) {
        
        ErrorMessage em = new ErrorMessage();
        
        em.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());
        
        em.setErrors(errors);
        
        ResponseEntity<ErrorMessage> retval = new ResponseEntity<ErrorMessage>(em, HttpStatus.INTERNAL_SERVER_ERROR);
        
        return retval;
    }
    
    
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleFileNotFound(FileNotFoundException ex) {
        
        ErrorMessage em = new ErrorMessage();
        
        em.setStatus(HttpStatus.NOT_FOUND);
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());
        
        em.setErrors(errors);
        
        ResponseEntity<ErrorMessage> retval = new ResponseEntity<ErrorMessage>(em, HttpStatus.NOT_FOUND);
        
        return retval;
    }
    
    
    /**
     * Returns a {@link Download} instance from the pool, incrementing its count
     * @param url
     * @return
     */
    private Download getKeyForUrl(String url) {
        if (url == null) {
            return null;
        }
        synchronized (downloadPool) {
            for (Entry<Download, Long> e: downloadPool.entrySet()) {
                Download dl = e.getKey();
                if (url.equals(dl.url)) {
                    long count = e.getValue() + 1;
                    downloadPool.put(dl, count);
                    return dl;
                }
            }
            
            Download dl = new Download();
            dl.status = DownloadStatus.New;
            dl.url = url;
            downloadPool.put(dl, 1L);
            return dl;
        }
    }
    
    
    private void returnKeyToPool(Download key) {
        synchronized (downloadPool) {
            Long val = downloadPool.get(key);
            if (val != null) {
                downloadPool.put(key,  val - 1);
            }
        }
    }
}
