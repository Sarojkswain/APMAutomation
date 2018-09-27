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

import java.util.Set;

import com.ca.apm.automation.action.responsefile.ResponseFile;
import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.apm.automation.action.responsefile.writer.IResponseFileWriter;
import com.ca.apm.automation.action.responsefile.writer.TextFileWriter;

/**
 * @author surma04
 */
public class CADirectoryResponseFile extends ResponseFile {

    private Set<Triplet> responseData;
    private final IResponseFileWriter writer = new TextFileWriter("%s%s%s");

    public CADirectoryResponseFile(Set<Triplet> installResponseFileData) {
        this.responseData = installResponseFileData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.responsefile.ResponseFile#getWriter()
     */
    @Override
    protected IResponseFileWriter getWriter() {
        return this.writer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.automation.action.responsefile.ResponseFile#getInstallResponseFileData()
     */
    @Override
    protected Set<Triplet> getInstallResponseFileData() {
        return this.responseData;
    }

}
