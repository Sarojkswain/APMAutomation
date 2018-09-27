package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.utils.file.FileModifier;

/**
 * Oracle DB installation flow.
 * 
 * @author sinal04
 */
@Flow
public class DeployOracleDBFlow extends FileModifierFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployOracleDBFlow.class);

    public static final String DEFAULT_PARENT_DIR_NAME    = "database";
    public static final String DEFAULT_RESPONSE_DIR_NAME  = "response" ;
    
    @FlowContext
    private DeployOracleDBFlowContext context;
    
    @Override
    public void run() throws IOException {
        File unpackedSourcesFile = new File(context.getInstallUnpackedSourcesLocation());
        archiveFactory.createArchive(context.getInstallPackageUrl()).unpack(unpackedSourcesFile);

        File installResponseFile = createResponseFile();
        String installResponseFilePath = installResponseFile.getAbsolutePath();
        context.getReplaceMap().put(installResponseFilePath, context.getResponseFileOptions());
        
        prepareResponseFile();
        
        try {
            runInstallationProcess(installResponseFile);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        
    }

    protected void prepareResponseFile() throws IOException {
        LOGGER.info("Preparing response file");
        for (Map.Entry<String, Map<String, String>> replaceFileEntry : context.getReplaceMap().entrySet()) {
            File file = new File(replaceFileEntry.getKey());
            LOGGER.info("Preparing file '{}' with replacing map: {}", file, replaceFileEntry.getValue());
            String content = FileUtils.readFileToString(file);

            FileModifier fileModifier = fileOperationFactory.create(file, context.getEncoding());
            fileModifier.replace(replaceFileEntry.getValue());

            for (Map.Entry<String, String> singleReplace : replaceFileEntry.getValue().entrySet()) {
                String oldString = singleReplace.getKey();
                String newString = singleReplace.getValue();
                LOGGER.info("Replacing {} with {} in {}", oldString, newString, file);
                content = content.replace(oldString, newString);
            }
            FileUtils.write(file, content, context.getEncoding()); 
        }
    }

    @NotNull
    protected File createResponseFile() throws IOException {
        File responseFileDir = null;
        if (context.getResponseFileDir() == null) {
            File unpackedSourcesLocation = new File(context.getInstallUnpackedSourcesLocation());
            File responseDirParent = new File(unpackedSourcesLocation, DEFAULT_PARENT_DIR_NAME); 
            responseFileDir = new File(responseDirParent, DEFAULT_RESPONSE_DIR_NAME);
        } else {
            responseFileDir = new File(context.getResponseFileDir());
        }
        
        File installResponseFile = new File(responseFileDir, context.getResponseFileName());

        if (!installResponseFile.exists() || !installResponseFile.canRead()) {
            throw new IllegalStateException(
                "Installation response file('" + installResponseFile.getAbsolutePath() + "') is either missing or can't be read.");
        }
        LOGGER.info("Installation response file located at: {}", installResponseFile);

        return installResponseFile;
    }

    protected void runInstallationProcess(File installResponseFile) throws InterruptedException {
        File installExecutableDirectory = FileUtils.getFile(context.getInstallUnpackedSourcesLocation(), DEFAULT_PARENT_DIR_NAME, "setup.exe");

        int responseCode = getExecutionBuilder(LOGGER, installExecutableDirectory.toString())
            .args(new String[] {
                                "-silent", 
                                "-force", 
                                "-waitforcompletion", 
                                "-nowait", 
                                "-ignorePrereq", 
                                "-ignoreSysPrereqs", 
                                "-responseFile", 
                                installResponseFile.toString()
                                })
            .build()
            .go();

        
        switch (responseCode) {
            case 0:
                LOGGER.info("Oracle DB installation completed SUCCESSFULLY! Congratulations!");
                break;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of Oracle DB failed (%d)", responseCode));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

}
