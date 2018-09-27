package com.ca.apm.flow;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DownloadInstallerFlow extends FlowBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadInstallerFlow.class);
    private File targetDir;

    @FlowContext
    DownloadInstallerFlowContext context;
    
    @Override
    public void run() throws Exception
    {
        //targetDir=new File("C:\\automation\\deployed\\installers\\em");
        targetDir=new File(context.getinstallerTargetDir());
        createInstallerTargetDirectory();
        downloadEm();
        downloadEula();
    }
    private void createInstallerTargetDirectory()
    {
        LOGGER.info("creating target directory");
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IllegalStateException("Error creating installer target directory: " + targetDir);
        }
    }
    private void downloadEm() throws Exception
    {
        LOGGER.info("Downloading em installer");
        URL installerUrl=context.getInstroscopeArtifactURL();
        File installerPath = new File(targetDir, new File(installerUrl.getFile()).getName());
        archiveFactory.createArtifact(installerUrl).download(installerPath);
    }
    private void downloadEula() throws Exception
    {
        LOGGER.info("downloading eula file");
        URL eulaUrl=context.getEulaArtifactURL();
        archiveFactory.createArchive(eulaUrl).unpack(targetDir);
    }
}
