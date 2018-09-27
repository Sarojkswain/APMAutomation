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

package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.util.ArchiveUtils;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archives (TAR, ZIP, JAR) creating flow.
 *
 * @author haiva01
 */
@Flow
public class ArchiveCreationFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveCreationFlow.class);

    @FlowContext
    private ArchiveCreationFlowContext flowContext;

    @Override
    public void run() throws Exception {
        try {
            AbstractArchiver archiver = ArchiveUtils.prepareArchiver(flowContext.getArchivePath(),
                flowContext.getArchiveType(), flowContext.getArchiveCompression(),
                flowContext.getArchiveEntries());
            FileUtils.forceMkdir(archiver.getDestFile().getParentFile());
            archiver.createArchive();
        } catch (Exception e) {
            ErrorUtils.logExceptionFmt(LOGGER, e, "Failed to create archive {1}. Exception: {0}",
                flowContext.getArchivePath());
        }
    }

}
