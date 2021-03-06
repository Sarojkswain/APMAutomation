/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
package com.ca.apm.tests.role;


import com.ca.tas.annotation.TasResource;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

/**
 * Java Agent Automation
 *
 * @author ahmal01@ca.com
 */
public class FetchAgentLogsRole extends AbstractRole {
    
    private final String resultDir;
    private final String pathSeparator;
    
    public FetchAgentLogsRole(String roleId, String resultDir) {
        super(roleId);
        this.resultDir = resultDir;
        this.pathSeparator = "/";
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
      //  packZip(aaClient);
    }
    
    @TasResource(value = "agentlogs", regExp = ".*log.*")
    public String getlogs() {
        return resultDir + pathSeparator +  "agentlogs";
    }

    @TasResource(value = "agentlogs", regExp = ".*profile.*")
    public String getAgentProfile() {
        return resultDir + pathSeparator +  "agentlogs";
    }
    
}
