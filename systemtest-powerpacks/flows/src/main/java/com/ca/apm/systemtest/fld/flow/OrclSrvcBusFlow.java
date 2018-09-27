package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * Flow class, which edits the needed response file data and runs the installer.
 * @Author rsssa02
 */
@Flow
public class OrclSrvcBusFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrclSrvcBusFlow.class);
    @FlowContext
    private OrclSrvcBusFlowContext context;

    public OrclSrvcBusFlow() {
    }

    public void run() throws Exception {
        this.archiveFactory.createArchive(this.context.getInstallPackageUrl()).unpack(new File(this.context.getUnpackDirName()));
        File installResponseFile = this.createResponseFilePath();
        this.prepareResponseFile(installResponseFile);

        try {
            this.runInstallationProcess(installResponseFile, this.context.getJreHomeLocation());
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
    }

    @NotNull
    protected File createResponseFilePath() {
       File responseFileDir = FileUtils.getFile(this.context.getUnpackDirName() + "\\Disk1\\stage\\Response");
        File installResponseFile = new File(responseFileDir, this.context.getResponseFileName());
        if (installResponseFile.exists() && installResponseFile.canRead()) {
            LOGGER.info("Installation response file located at: {}", installResponseFile);
            return installResponseFile;
        } else {
            throw new IllegalStateException("Installation response file(\'" + installResponseFile.getAbsolutePath() + "\') is either missing or can\'t be read.");
        }
    }

    protected void prepareResponseFile(File installResponseFile) throws Exception {
        LOGGER.info("Preparing response file");
        File propertiesFilePath = installResponseFile.getAbsoluteFile();
        PropertiesConfiguration prop = new PropertiesConfiguration();
        Map<String, String> propertiesMap = context.getResponseFileOptions();
        try {
            prop.load(propertiesFilePath);
        } catch (ConfigurationException e) {
            throw  new IllegalStateException("Installation rsp properties files cannot be loaded");
        }
        if (propertiesMap != null) {
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Config file property: {} = {}", entry.getKey(), entry.getValue());
                prop.setProperty(entry.getKey(), entry.getValue());
            }
        }
        prop.save(propertiesFilePath);
    }

    protected void runInstallationProcess(File installResponseFile, String jreHomePath) throws Exception {
        File installExecutableDirectory = FileUtils.getFile(this.context.getUnpackDirName() + "\\Disk1",
                this.context.getSetupInstallerName());
        //waitforcompletion is needed so that the silent cmd handle runs without any interruption
        LOGGER.info("the filename is"+installExecutableDirectory.toString());
        int responseCode = this.getExecutionBuilder(LOGGER, installExecutableDirectory.toString())
                .args(new String[]{"-silent", "-response", installResponseFile.toString(), "-jreLoc", jreHomePath, "-waitforcompletion"})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Oracle Service Bus Installation was successful...!");
                validateInstall();
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation failed (%d)", new Object[]{responseCode}));
        }
    }

    private void setJVMVendorAfterInstallation(boolean osbSunJvmVendor) {
        if(osbSunJvmVendor){
            LOGGER.info("Setting JVM vendor to Sun in domain dir!");

        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

    protected void validateInstall() {
        if (!new File(context.getDomainDirRelativePath(), "bin").exists()) {
            throw new IllegalStateException("Missing domain dir bin path.. rerun installation.");
        }
    }

}
