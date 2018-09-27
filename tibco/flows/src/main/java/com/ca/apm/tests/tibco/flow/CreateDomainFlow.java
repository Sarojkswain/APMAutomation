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

package com.ca.apm.tests.tibco.flow;


import java.io.File;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * CreateDomainFlow
 * To create the Tibco Domain.
 * Vashistha Singh (sinva01.ca.com)
 *
 */
@Flow
public class CreateDomainFlow extends FlowBase {

    @FlowContext
    private DeployTibcoFlowContext context;

    @Override
    public void run() throws Exception {

        String ver = context.getVersion().getArtifact().getVersion();
        ver = ver.substring(0, ver.lastIndexOf('.'));

        String adminHome = context.getInstallDir() + "\\administrator";
        String adminVersionDir = adminHome + "\\" + ver;
        String adminBinDir = adminVersionDir + "\\bin";

        String domainFileName = adminBinDir + "\\createDomain.xml";
        File createDomainFile = new File(domainFileName);

        // For now create a simple domain creation xml file.....

        Collection<String> lines = new ArrayList<>();
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        lines
            .add("<Task xmlns=\"http://www.tibco.com/domainutility/commandline\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        lines.add("<CreateDomain>");
        lines.add("<DomainName>" + context.getDomainName() + "</DomainName>");
        lines.add("<AdministratorUsername>" + context.getDomainUser() + "</AdministratorUsername>");
        lines.add("<AdministratorPassword>" + context.getDomainPassword()
            + "</AdministratorPassword>");
        lines.add("<AdminHome>" + adminVersionDir + "</AdminHome>");
        lines.add("<LocalApplicationData>true</LocalApplicationData>");
        lines.add("</CreateDomain>");
        lines.add("</Task>");
        // Write file
        FileUtils.writeLines(createDomainFile, lines);

    }

}
