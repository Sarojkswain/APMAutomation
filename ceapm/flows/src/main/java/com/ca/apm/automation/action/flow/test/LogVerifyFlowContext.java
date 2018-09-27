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

package com.ca.apm.automation.action.flow.test;

import com.ca.apm.automation.action.flow.IFlowContext;

import java.util.Collections;
import java.util.Set;

/**
 * Context for the {@link LogVerifyFlow} flow.
 */
public class LogVerifyFlowContext implements IFlowContext {
    private final String logFile;
    private final Set<String> keywords;
    private final boolean contained;

    /**
     * Factory method for a positive check (single keyword).
     *
     * @param logFile Log file to verify.
     * @param keyword Keyword to verify.
     * @return {@link LogVerifyFlowContext} instance.
     */
    public static LogVerifyFlowContext verifyContained(String logFile, String keyword) {
        return verifyContained(logFile, Collections.singleton(keyword));
    }

    /**
     * Factory method for a positive check (multiple keywords).
     *
     * @param logFile Log file to verify.
     * @param keywords Keywords to verify.
     * @return {@link LogVerifyFlowContext} instance.
     */
    public static LogVerifyFlowContext verifyContained(String logFile,
                                                       Set<String> keywords) {
        return new LogVerifyFlowContext(logFile, keywords, true);
    }

    /**
     * Factory method for a negative check (single keyword).
     *
     * @param logFile Log file to verify.
     * @param keyword Keywords to verify.
     * @return {@link LogVerifyFlowContext} instance.
     */
    public static LogVerifyFlowContext verifyNotContained(String logFile, String keyword) {
        return verifyNotContained(logFile, Collections.singleton(keyword));
    }

    /**
     * Factory method for a negative check (multiple keywords).
     *
     * @param logFile Log file to verify.
     * @param keywords Keywords to verify.
     * @return {@link LogVerifyFlowContext} instance.
     */
    public static LogVerifyFlowContext verifyNotContained(String logFile,
                                                          Set<String> keywords) {
        return new LogVerifyFlowContext(logFile, keywords, false);
    }

    /**
     * Constructor.
     *
     * @param logFile Log file to verify.
     * @param keywords Keywords to verify.
     * @param contained Whether the keywords are expected or unexpected.
     */
    protected LogVerifyFlowContext(String logFile, Set<String> keywords, boolean contained) {
        this.logFile = logFile;
        this.keywords = keywords;
        this.contained = contained;
    }

    public String getLogFile() {
        return logFile;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public boolean isContained() {
        return contained;
    }
}
