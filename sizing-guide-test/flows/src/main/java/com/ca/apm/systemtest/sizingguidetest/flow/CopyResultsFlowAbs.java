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
package com.ca.apm.systemtest.sizingguidetest.flow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public abstract class CopyResultsFlowAbs extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyResultsFlowAbs.class);

    public CopyResultsFlowAbs() {}

    /**
     * Adapted from Apache IOUtils
     * blocks until process is running
     */
    public static long copyStream(InputStream input, OutputStream output, boolean logAlive,
        @Nullable String fileName) throws IOException {
        long count = 0L;
        byte[] buffer = new byte[4096];
        int n1;
        long time = System.currentTimeMillis();
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            output.write(buffer, 0, n1);
            long currentTime = System.currentTimeMillis();
            if (logAlive && currentTime - time > 20000) {
                // log every ~20 seconds
                LOGGER.info("Stream copy is alive."
                    + (fileName != null ? "[" + fileName + "]" : ""));
                time = currentTime;
            }
        }
        return count;
    }

    protected void configNet(String remoteMachine, String user, String password)
        throws InterruptedException {
        int responseCode =
            this.getExecutionBuilder(LOGGER, "net")
                .args(new String[] {"use", remoteMachine, "/user:" + user, password}).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Net completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Net failed (%d)",
                    new Object[] {responseCode}));
        }
    }

    protected void copyFile(String origFile, String destFile) throws InterruptedException {
        int responseCode =
            this.getExecutionBuilder(LOGGER, "copy").args(new String[] {origFile, destFile})
                .build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("Copy File completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Copy File failed (%d)",
                    new Object[] {responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }

}
