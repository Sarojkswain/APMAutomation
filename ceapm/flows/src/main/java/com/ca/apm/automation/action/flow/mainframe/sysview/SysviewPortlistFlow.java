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

package com.ca.apm.automation.action.flow.mainframe.sysview;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;

/**
 * Flow for executing various operations on the PORTLIST type entries in SYSVIEW GROUPS.
 */
@Flow
public class SysviewPortlistFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(SysviewPortlistFlow.class);
    static final Collection<String> DEFAULT_GROUPS = Arrays.asList("CICSWILY", "IMSWILY");

    public enum Operation {
        ADD, DELETE, TEST;

        String getMemberCommand() {
            return name() + "Member";
        }
    }

    @FlowContext
    private SysviewPortlistFlowContext context;

    @Override
    public void run() throws Exception {
        try (Sysview sysv = new Sysview(context.getLoadlib())) {
            logger.debug("Will {} PORTLIST members '{}' using groups '{}'",
                context.getOperation().name().toLowerCase(),
                StringUtils.join(context.getPorts(), ","),
                StringUtils.join(context.getGroups(), ","));

            Collection<Sysview.Rc> acceptableRcs = Sysview.Rc.getOkValues();
            if (context.getOperation() != Operation.TEST) {
                // Adding existing ports, or deleting missing ones is not considered an error.
                acceptableRcs.add(Sysview.Rc.ERROR);
            }

            for (int port : context.getPorts()) {
                for (String group : context.getGroups()) {

                    ExecResult res = sysv.execute("GROUPs {0} Type PORTLIST, Group {1}, Member {2}",
                        context.getOperation().getMemberCommand(), group, String.valueOf(port));

                    if (!acceptableRcs.contains(res.getRc())) {
                        throw new IllegalStateException(context.getOperation().name() + " "
                            + " operation failed for port " + port + " in group " + group);
                    }

                    String check = null;
                    Collection<Sysview.Rc> okValues = Sysview.Rc.getOkValues();
                    switch (context.getOperation()) {
                        case ADD:
                            if (!okValues.contains(res.getRc())) {
                                check = "Member " + port + " is already in group " + group;
                            }
                            break;

                        case DELETE:
                            if (!okValues.contains(res.getRc())) {
                                check = "Member " + port + " was not found in group " + group;
                            }
                            break;

                        case TEST:
                            if (!okValues.contains(res.getRc())) {
                                throw new IllegalStateException(context.getOperation().name()
                                    + " operation failed for port " + port + " in group " + group);
                            }
                            check = "Member " + port + " is a member of group " + group;
                            break;
                    }

                    if (check != null) {
                        boolean found = false;
                        for (String message : res.getTabularData().getMessages()) {
                            if (message.contains(check)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            throw new IllegalStateException(context.getOperation().name()
                                + " operation failed for port " + port + " in group " + group);
                        }
                    }

                    logger.info("{} operation for PORTLIST member {} using group {} succeeded",
                        context.getOperation().name(), port, group);
                }
            }
        }
    }
}
