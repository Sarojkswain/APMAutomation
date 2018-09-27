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

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Flow that allows log verification. The assertion can be either positive (something is in the
 * log) or negative (something is not in the log).
 */
@Flow
public class LogVerifyFlow extends FlowBase {
    private static final Logger log = LoggerFactory.getLogger(LogVerifyFlow.class);

    @FlowContext
    private LogVerifyFlowContext context;

    @Override
    public void run() throws Exception {
        Set<String> found = new HashSet<>();
        try (FileReader fr = new FileReader(context.getLogFile());
            BufferedReader br = new BufferedReader(fr)) {

            String line = br.readLine();
            while (line != null) {
                for (String keyword : context.getKeywords()) {
                    if (!found.contains(keyword) && line.contains(keyword)) {
                        found.add(keyword);
                    }
                }
                line = br.readLine();
            }

            if (context.isContained()) {
                if (found.size() != context.getKeywords().size()) {
                    Set<String> notFound = new HashSet<>(context.getKeywords());
                    notFound.removeAll(found);

                    log.debug("Did not find these keywords: {}", StringUtils.join(notFound, ","));
                    throw new AssertionError("Some of the keywords were not found in the log file");
                }
            } else {
                if (!found.isEmpty()) {
                    log.debug("Found these keywords: {}", StringUtils.join(found, ","));
                    throw new AssertionError("Some of the keywords were found in the log file");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
