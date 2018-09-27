package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class DirectoryCollectionItem extends CollectionItem {
    private String srcPath;
    private String dstPath;
    private Collection<String> includeFileSuffixes;
    private boolean copyContents;
    
    public DirectoryCollectionItem() {
        super();
    }
    
    public DirectoryCollectionItem(String srcPath, String dstPath) {
        this(srcPath, dstPath, null);
    }

    public DirectoryCollectionItem(String srcPath, String dstPath, boolean isMove) {
        this(srcPath, dstPath, null, null, isMove);
    }

    public DirectoryCollectionItem(String srcPath, String dstPath, 
                                   Collection<String> includeFileSuffixes) {
        this(srcPath, dstPath, includeFileSuffixes, null, true);
    }
    
    public DirectoryCollectionItem(String srcPath, String dstPath, 
                                   Collection<String> includeFileSuffixes, 
                                   AbstractPluginImpl plugin, 
                                   boolean isMove) {
        this(srcPath, dstPath, includeFileSuffixes, plugin, isMove, false, true);
    }
    
    public DirectoryCollectionItem(String srcPath, String dstPath, 
                                   Collection<String> includeFileSuffixes, 
                                   AbstractPluginImpl plugin, 
                                   boolean isMove, 
                                   boolean ignoreCleanupErrors,
                                   boolean copyContents) {
        super(plugin, isMove, ignoreCleanupErrors);
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.includeFileSuffixes = includeFileSuffixes;
        this.copyContents = copyContents;
    }

    /**
     * @return the copyContents
     */
    public boolean isCopyContents() {
        return copyContents;
    }

    /**
     * @param copyContents the copyContents to set
     */
    public void setCopyContents(boolean copyContents) {
        this.copyContents = copyContents;
    }

    /**
     * @return the includeFileSuffixes
     */
    public Collection<String> getIncludeFileSuffixes() {
        return includeFileSuffixes;
    }

    /**
     * @param includeFileSuffixes the includeFileSuffixes to set
     */
    public void setIncludeFileSuffixes(Collection<String> includeFileSuffixes) {
        this.includeFileSuffixes = includeFileSuffixes;
    }

    /**
     * @return the srcPath
     */
    public String getSrcPath() {
        return srcPath;
    }

    /**
     * @param srcPath the srcPath to set
     */
    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    /**
     * @return the dstPath
     */
    public String getDstPath() {
        return dstPath;
    }

    /**
     * @param dstPath the dstPath to set
     */
    public void setDstPath(String dstPath) {
        this.dstPath = dstPath;
    }

    @Override
    public void runCollection() {
        StringBuffer buf = new StringBuffer("Directory Collection Item -> Input parameters: ").
            append('\n').
            append("Source directory path: {0}").
            append('\n').
            append("Destination directory: {1}").
            append('\n').
            append("Copy files with suffixes (null or empty means all): {2}");

        logInfo(buf.toString(), 
            srcPath, dstPath, includeFileSuffixes);

        if (isBlank(srcPath)) {
            String msg = "Source directory path is blank!";
            logError(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }
        
        if (isBlank(dstPath)) {
            String msg = "Destination directory path is blank!";
            logError(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        final File src = new File(srcPath);
        final File dest = new File(dstPath);

        logInfo((isMove ? "Moving" : "Copying") + " ''{0}'' into ''{1}''", src, dest);

        try {
            if (includeFileSuffixes != null && !includeFileSuffixes.isEmpty()) {
                for (String includeFileSuffix : includeFileSuffixes) {
                    FileUtils.copyDirectory(src, dest, new SuffixFileFilter(includeFileSuffix));
                }
            } else {
                if (copyContents) {
                    FileUtils.copyDirectory(src, dest);
                } else {
                    FileUtils.copyDirectoryToDirectory(src, dest);
                }
            }
            
            if (isMove) {
                try {
                    FileUtils.cleanDirectory(src);
                } catch (IOException e) {
                    if (isIgnoreCleanupErrors()) {
                        logWarn(e, "Ignoring IO error which occurred while cleaning up directory ''{0}''", src);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            String msg = "Source not found!";
            logError(msg, ex);
            throw new PerfResultCollectorPluginException(msg, ex,  
                PerfResultCollectorPluginException.ERR_RESULT_LOG_COLLECTION_FAILED);
        } catch (IOException ex) {
            String msg = "IO error occurred!";
            logError(msg, ex);
            throw new PerfResultCollectorPluginException(msg, ex,  
                PerfResultCollectorPluginException.ERR_RESULT_LOG_COLLECTION_FAILED);
        }
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DirectoryCollectionItem [srcPath=" + srcPath + ", dstPath=" + dstPath
            + ", includeFileSuffixes=" + includeFileSuffixes + ", copyContents=" + copyContents
            + ", isIgnoreCleanupErrors()=" + isIgnoreCleanupErrors() + ", isMove()=" + isMove()
            + "]";
    }

    
    
}