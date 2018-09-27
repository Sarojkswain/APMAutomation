/**
 * 
 */
package com.ca.apm.systemtest.fld.filecache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;

import com.ca.apm.systemtest.fld.server.dao.FileCacheDao;
import com.ca.apm.systemtest.fld.server.model.FileCacheItem;

/**
 * @author KEYJA01
 *
 */
@Component("fileCache")
public class DefaultFileCacheImpl implements FileCache, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(FileCache.class);
    private String cacheLocation;
    private Path cacheDir;
    @Autowired
    private FileCacheDao fileCacheDao;
    // three weeks
    private long timeoutMs = 1814400000L;
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.server.filecache.FileCache#getFile(java.lang.String)
     */
    @Override
    @Transactional
    public FileItem getFile(String url) throws FileNotFoundException, IOException {
        FileCacheItem item = fileCacheDao.findByUrl(url);
        if (item == null) {
            throw new FileNotFoundException("Entry not in cache: " + url);
        }
        
        File f = new File(item.getFilesystemPath());
        if (!f.exists()) {
            throw new FileNotFoundException("Entry not in cache: " + url);
        }
        InputStream in = new FileInputStream(f);
        
        item.setLastAccessed(new Date());
        fileCacheDao.update(item);
        
        
        
        FileItem retval = new FileItem();
        retval.setLastAccess(item.getLastAccessed().getTime());
        retval.setLength(item.getLength());
        retval.setUrl(url);
        retval.setInputStream(in);
        
        return retval;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.server.filecache.FileCache#checkFile(java.lang.String, boolean)
     */
    @Override
    @Transactional
    public boolean checkFile(String url, boolean touch) {
        if (log.isDebugEnabled()) {
            List<FileCacheItem> items = fileCacheDao.findAll();
            StringBuffer sb = new StringBuffer("Dumping file cache contents:");
            for (FileCacheItem item: items) {
                sb.append("\n  item: ").append(item);
            }
            log.debug(sb.toString());
        }
        
        FileCacheItem item = fileCacheDao.findByUrl(url);
        if (item == null) {
            log.info("URL=" + url + " not found in cache");
            return false;
        }
        
        if (touch) {
            item.setLastAccessed(new Date());
            fileCacheDao.update(item);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.server.filecache.FileCache#putFile(java.lang.String, java.io.InputStream)
     */
    @Override
    @Transactional
    public void putFile(String url, InputStream in) {
        log.info("Inserting URL " + url + " into file cache");
        String filename = DigestUtils.md5DigestAsHex(url.getBytes()) + ".dat";
        File f = new File(cacheDir.toFile(), filename);
        try {
            FileOutputStream out;
            out = new FileOutputStream(f);
            FileCopyUtils.copy(in, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new FileCacheException(e);
        }
        
        FileCacheItem item = new FileCacheItem();
        item.setUrl(url);
        item.setFilesystemPath(f.getPath());
        item.setLastAccessed(new Date());
        item.setLength(f.length());
        fileCacheDao.create(item);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.filecache.FileCache#pruneCache()
     */
    @Override
    @Transactional
    public void pruneCache() {
        log.info("Starting prune cache");
        long now = System.currentTimeMillis();
        List<FileCacheItem> items = fileCacheDao.findAll();
        
        for (FileCacheItem item: items) {
            boolean shouldDelete = false;
            
            // check if it has timed out
            long lastAccessed = item.getLastAccessed().getTime();
            long elapsed = now - lastAccessed;
            // if older than configured timeout
            if (elapsed > timeoutMs) {
                shouldDelete = true;
            }
            
            // check if the file still exists in the file system
            File f = new File(item.getFilesystemPath());
            if (!f.exists()) {
                log.info("File not found for item " + item);
                shouldDelete = true;
            }
            
            if (shouldDelete) {
                if (f.exists()) {
                    try {
                        Files.delete(Paths.get(item.getFilesystemPath()));
                        log.info("Deleted " + f.getAbsolutePath());
                    } catch (Exception e) {
                        // if there was an exception, try to delete it on exit
                        log.info("Could not delete " + f.getAbsolutePath() + ", will try to delete on exit", e);
                        f.deleteOnExit();
                    }
                }
                fileCacheDao.delete(item);
                log.info("Removed " + item + " from cache");
            }
            
        }
    }

    @Value("${file.cache.dir:filecache}")
    public void setCacheLocation(String cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cacheDir = Paths.get(cacheLocation);
    }
}
