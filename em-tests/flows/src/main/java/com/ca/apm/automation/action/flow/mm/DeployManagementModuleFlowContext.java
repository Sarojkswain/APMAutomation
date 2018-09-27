/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.automation.action.flow.mm;

import com.ca.apm.automation.action.flow.IFlowContext;


/**
 * Immutable context file for Management Module flow.
 *
 * @author korzd01
 */
public class DeployManagementModuleFlowContext implements IFlowContext {

    private final String mmPathName;
    private final String emInstallDir;

    public DeployManagementModuleFlowContext(String mmPathName, String emInstallDir) {
        this.mmPathName = mmPathName;
        this.emInstallDir = emInstallDir;
    }

    public String getManagementModulePathName() {
        return mmPathName;
    }

    public String getEmInstallDir() {
        return emInstallDir;
    }

    @Override
    public String toString() {
        return "DeployManagementModuleFlowContext{" +
               "managementModulePathName=" + mmPathName +
               ", emInstallDir=" + emInstallDir +
               '}';
    }
}
