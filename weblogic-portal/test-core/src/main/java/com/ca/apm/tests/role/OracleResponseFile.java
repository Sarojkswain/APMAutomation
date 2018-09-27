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
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.responsefile.writer.IResponseFileWriter;
import com.ca.apm.automation.action.responsefile.writer.XmlFileWriter;
import  com.ca.apm.automation.action.responsefile.*;

import java.util.Set;

/**
 * OracleResponseFile class
 *
 * Creates a response file in Oracle's format
 *
 * @author Jan Pojer (pojja01.ca.com)
 * @since 1.0
 */
public class OracleResponseFile extends ResponseFile {
	

    private final IResponseFileWriter writer = new XmlFileWriter("<domain-template-descriptor><input-fields>%s</input-fields></domain-template-descriptor>",
                                                                 "<%s name=\"%s\" value=\"%s\"/>");
    private final Set<Triplet> installResponseFileData;

    /**
     * @param installResponseFileData Data used in response file
     */
    public OracleResponseFile(Set<Triplet> installResponseFileData) {
        this.installResponseFileData = installResponseFileData;
    }

    @Override
    protected IResponseFileWriter getWriter() {
        return writer;
    }

    @Override
    protected Set<Triplet> getInstallResponseFileData() {
        return installResponseFileData;
    }
}
