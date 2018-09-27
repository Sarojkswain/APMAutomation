/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class QcUploadToolSimpleUploadFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(QcUploadToolSimpleUploadFlow.class);
    @FlowContext
    private QcUploadToolSimpleUploadFlowContext context;

    public void run() throws IOException {
        List<String> args = new ArrayList<>();
        args.add("-classpath");
        args.add("QCUploader.jar;./lib/com.mercury.qualitycenter.otaclient-9.2.jar;./lib/CLWorkstation.jar;./lib/com4j-2.1.jar;./lib/dom4j-1.6.1.jar");
        args.add("-DpropertFile=./resources/qc/qcUploader.properties");
        args.add("-Dlog4j.configuration=file:lib/log4j-qc.properties");
        args.add("-Dconfig.location=file");
        args.add("com.ca.tools.qc.SimpleResultUploader");
        args.add(this.context.getTestId());
        args.add((this.context.getPassed() != null && this.context.getPassed()) ? "Passed" : "Failed");
        String[] argsArr = new String[args.size()];
        argsArr = args.toArray(argsArr);
        // modify config file
        File configFile = FileUtils.getFile(context.getInstallPath(), "resources/qc/qcUploader.properties");
        setProperty(configFile, "testLab.testSetFolder", context.getTestSetFolder());
        setProperty(configFile, "testLab.testSetName", context.getTestSetName());
        // run upload
        try {
            runQcUpload(argsArr);
//        } catch (InterruptedException var3) {
//            throw new IllegalStateException(var3);
        } catch (Exception e) {
            LOGGER.warn("Exception in QcUpload: " + e, e);
        }
        LOGGER.info("QcUploadToolSimpleUploadFlow has finished.");
    }

    protected void runQcUpload(String[] argsArr) throws InterruptedException {
        String command = "java";
        if (context.getJavaHome() != null) {
            command = FileUtils.getFile(context.getJavaHome(), "bin/java.exe").getAbsolutePath();
        }
        int responseCode = this.getExecutionBuilder(LOGGER, command).workDir(FileUtils.getFile(context.getInstallPath()))
                .args(argsArr).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("QcUpload completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("QcUpload failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void setProperty(File configFile, String property, String value) throws IOException {
        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll(property + "(\\s*)=.*", property + "=" + value);
        Files.write(path, content.getBytes(charset));
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
