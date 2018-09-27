/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.flow.action.responsefile;

import com.ca.apm.automation.action.responsefile.ResponseFile;
import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.apm.automation.action.responsefile.writer.IResponseFileWriter;
import com.ca.apm.automation.action.responsefile.writer.XmlFileWriter;

import java.util.Set;

/**
 * TibcoResponseFile class
 *
 * Creates a response file in Tibco's format
 *
 * @author Vashistha Singh (sinva01.ca.com)
 * @since 1.0
 */
public class TibcoResponseFile extends ResponseFile {
    private final String initalLine =
        "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";
    private final IResponseFileWriter writer = new XmlFileWriter(initalLine
        + "\n<properties>\n%s\n</properties>", "%s<entry key=\"%s\">%s</entry>");
    private final Set<Triplet> installResponseFileData;

    /**
     * @param installResponseFileData Data used in response file
     */
    public TibcoResponseFile(Set<Triplet> installResponseFileData) {
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
