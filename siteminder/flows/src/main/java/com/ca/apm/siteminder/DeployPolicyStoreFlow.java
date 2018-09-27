/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.apm.siteminder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class DeployPolicyStoreFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployPolicyStoreFlow.class);

    @FlowContext
    private DeployPolicyStoreFlowContext context;

    @Override
    public void run() throws Exception {
        // download & unpack installer to desired location
        File installDir = context.getInstallDir();
        getArchiveFactory().createArchive(context.getArtifactUrl()).unpack(installDir);

        // set env variables that are set during cadir and ps installation
        Map<String, String> env = new HashMap<>();
        env.put("DXHOME", context.getCadirectoryDir());
        env.put("NETE_PS_ROOT", context.getPsDir());

        // install
        // response code from the cmd is always 0 - need to update script and add err handling
        final int responseCode = Utils.exec(installDir.getPath(),
            installDir.getPath() + "/smps_auto/CreatePstore.cmd", new String[] {}, LOGGER, env);
    }

}
