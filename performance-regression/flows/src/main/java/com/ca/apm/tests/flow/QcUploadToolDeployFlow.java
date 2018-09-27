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
import com.ca.apm.automation.utils.file.TasFileNameFilter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * GacutilFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class QcUploadToolDeployFlow extends FlowBase {

    public static final String REGSRV32_PATH = "c:\\Windows\\SysWOW64\\regsvr32.exe";

    private static final Logger LOGGER = LoggerFactory.getLogger(QcUploadToolDeployFlow.class);
    @FlowContext
    private QcUploadToolDeployFlowContext context;

    public void run() throws IOException {
        this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(context.getDeploySourcesLocation()));

        // register DLLs
        File[] libraries = FileUtils.getFile(context.getDeploySourcesLocation(), "lib")
                .listFiles(new TasFileNameFilter(".dll", TasFileNameFilter.FilterMatchType.ENDS_WITH));
        try {
            for (File lib : libraries) {
                runRegsrv(lib.getAbsolutePath(), false);
            }
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runRegsrv(String dllPath, boolean unregister) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, REGSRV32_PATH)
                .args(new String[]{"/s", unregister ? "/u" : "", dllPath}).build().go();
        switch (responseCode) {
            case 0:
            case 3: // todo resolve regstration problem then remove (3)
                LOGGER.info("Regsrv Execution completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Regsrv Execution failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
