/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.saas.flow;

import java.io.File;

import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.utils.archive.Archive;
import com.ca.apm.automation.utils.archive.TasArchiveFactory;

/**
 * @author kurma05
 */
@Flow
public class JarArchiveFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarArchiveFlow.class);

    @FlowContext
    private JarArchiveFlowContext context;

    @Override
    public void run() throws Exception {
        
        if(context.shouldUnpack()) {
            unpackJar();
        }        
        if(context.shouldPack()) {
            packJar();
        }
    }
   
    public void packJar() throws Exception {
        
        String jarFilePath = context.getArchivePath();
        LOGGER.info("Packing file : {}", jarFilePath);
   
        ZipArchiver archiver = new ZipArchiver();
        archiver.setDestFile(new File(jarFilePath));
        archiver.addDirectory(new File(context.getTempUnpackDir()));
        archiver.createArchive();
    }
    
    private void unpackJar() throws Exception {
        
        String jarFilePath = context.getArchivePath();
        LOGGER.info("Unpacking file : {}", jarFilePath);
        
        File archive = new File(jarFilePath);
        TasArchiveFactory factory = new TasArchiveFactory();
        Archive tasArchive = factory.createArchive(archive);
        tasArchive.unpack(new File(context.getTempUnpackDir()));
    }
}
