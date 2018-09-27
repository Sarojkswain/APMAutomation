package com.ca.apm.tests.flow;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.utils.archive.Archive;
import com.ca.apm.automation.utils.archive.TasZipArchive;

/**
 * Downloading file without validating file extension like TAS core does
 * @author kurma05
 */
@Flow
public class FileDownloadFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadFlow.class);

    @FlowContext
    private FileDownloadFlowContext context;

    @Override
    public void run() throws Exception {
        
        downloadArchive();
    }
   
    public void downloadArchive() throws Exception {
        
        URL url = new URL(context.getUrl());
        
        LOGGER.info("Downloading file : {}", url);   
        String file = context.getInstallDir() + "/" + context.getDestFileName();
        FileUtils.copyURLToFile(url, new File(file));
        
        if(context.shouldUnpack()) {
            LOGGER.info("Unpacking file : {}", file);        
            File archive = new File(file);
            Archive tasArchive = new TasZipArchive(archive);
            tasArchive.unpack(new File(context.getInstallDir()));   
        }
    }   
}
