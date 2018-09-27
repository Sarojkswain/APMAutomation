package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class FileCollectionItem extends CollectionItem {
    private String srcFilePath;
    private String dstDirPath;
    private String targetFileName;
    
    public FileCollectionItem() {
        super();
    }
    
    public FileCollectionItem(String srcFilePath, String dstDirPath, String targetFileName) {
        this(srcFilePath, dstDirPath, targetFileName, true);
    }

    public FileCollectionItem(String srcFilePath, String dstDirPath, String targetFileName, 
                              boolean isMove) {
        this(srcFilePath, dstDirPath, targetFileName, null, isMove);
    }

    public FileCollectionItem(String srcFilePath, String dstDirPath, String targetFileName, 
                              AbstractPluginImpl plugin, boolean isMove) {
        super(plugin, isMove);
        this.srcFilePath = srcFilePath;
        this.dstDirPath = dstDirPath;
        this.targetFileName = targetFileName;
    }


    /**
     * @return the srcFilePath
     */
    public String getSrcFilePath() {
        return srcFilePath;
    }

    /**
     * @param srcFilePath the srcFilePath to set
     */
    public void setSrcFilePath(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }

    /**
     * @return the dstDirPath
     */
    public String getDstDirPath() {
        return dstDirPath;
    }

    /**
     * @param dstDirPath the dstDirPath to set
     */
    public void setDstDirPath(String dstDirPath) {
        this.dstDirPath = dstDirPath;
    }

    /**
     * @return the targetFileName
     */
    public String getTargetFileName() {
        return targetFileName;
    }

    /**
     * @param targetFileName the targetFileName to set
     */
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    @Override
    public void runCollection() {
        StringBuffer buf = new StringBuffer("File Collection Item -> Input parameters: ").
            append('\n').
            append("Source file path: {0}").
            append('\n').
            append("Destination directory: {1}").
            append('\n').
            append("Destination file name: {2}");
        
        logInfo(buf.toString(), srcFilePath, dstDirPath, targetFileName);

        if (isBlank(srcFilePath)) {
            String msg = "Source file path is blank!";
            logError(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }
        
        if (isBlank(dstDirPath)) {
            String msg = "Destination folder is blank!";
            logError(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        if (isBlank(targetFileName)) {
            String msg = "Destination file name is blank!";
            logError(msg);
            throw new PerfResultCollectorPluginException(msg, 
                PerfResultCollectorPluginException.ERR_RESULT_COLLECTION_CONFIG_IS_INVALID);
        }

        final File src = new File(srcFilePath);
        final File destFolder = new File(dstDirPath);
        final File destFile = new File(destFolder, targetFileName);

        try {
            if (destFile.exists()) {
                File renameFile = destFile;
                while (renameFile.exists()) {
                    Thread.sleep(500);
                    String renameExistingToFileName = targetFileName + "_" + PerfTestResultCollectionConfig.dateFormat.format(new Date());
                    logInfo("File ''{0}'' exists, trying to rename it to ''{1}''", 
                        renameFile, renameExistingToFileName);
                    renameFile = new File(destFolder, renameExistingToFileName);
                }
                
                FileUtils.moveFile(destFile, renameFile);
                logInfo("Renamed ''{0}'' into ''{1}''", destFile, renameFile);
            }
        } catch (IOException ex) {
            String msg = "Error renaming existing file";
            logError(msg, ex);
            throw new PerfResultCollectorPluginException(msg, ex,
                PerfResultCollectorPluginException.ERR_RESULT_LOG_COLLECTION_FAILED);
        } catch (InterruptedException e) {
            String msg = "Result collector thread is interrupted!";
            logError(msg, e);
            throw new PerfResultCollectorPluginException(msg, e, 
                PerfResultCollectorPluginException.ERR_RESULT_LOG_COLLECTION_FAILED);
        }

        logInfo((isMove ? "Moving" : "Copying") + " ''{0}'' to ''{1}''", 
            src, destFile);

        try {
            if (isMove) {
                FileUtils.moveFile(src, destFile);    
            } else {
                FileUtils.copyFile(src, destFile);
            }
        } catch (IOException ex) {
            String msg = "IO error occurred";
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
        return "FileCollectionItem [srcFilePath=" + srcFilePath + ", dstDirPath=" + dstDirPath
            + ", targetFileName=" + targetFileName + ", isMove()=" + isMove() + ", getPlugin()="
            + getPlugin() + ", getClass()=" + getClass() + "]";
    }
    
}