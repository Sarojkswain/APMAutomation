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
package com.ca.apm.tests.artifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * StockTrader
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum CsvToXlsTemplateVersion implements IThirdPartyArtifact {

    AGENT_VER_10_2("com/ca/apm/coda/agent.overhead", "AgentPerformanceTemplate_40min_10.2", TasExtension.ZIP, "AgentPerformanceTemplate_40min_10.2.xls"),
    AGENT_VER_10_5("com/ca/apm/coda/agent.overhead", "AgentPerformanceTemplate_40min_10.5", TasExtension.ZIP, "AgentPerformanceTemplate_40min_10.5.xls"),
    EM_PERFORMANCE("com/ca/apm/coda/em-performance", "EmPerformanceTemplate", TasExtension.ZIP, "EmPerformanceTemplate.xls"),
    EM_PERFORMANCE_2("com/ca/apm/coda/em-performance", "EmPerformanceTemplate-2", TasExtension.ZIP, "EmPerformanceTemplate.xls");

    private final DefaultArtifact artifact;

    private final String xlsFileName;


    /**
     * @param archiveFileName name of the file in Artifactory
     * @param extension       file extension.
     */
    CsvToXlsTemplateVersion(String groupId, String archiveFileName, TasExtension extension, String xlsFileName) {
        artifact = new DefaultArtifact(groupId, archiveFileName, extension.getValue(), null);
        this.xlsFileName = xlsFileName;
    }

    public String getXlsFileName() {
        return xlsFileName;
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
    }

    /**
     * Non-standard artifact without POM
     *
     * @param artifactory artifactory URL
     * @return URL of the non-standard artifact
     */
    public URL getArtifactUrl(URL artifactory) {
        String artifactUrlTmp = artifactory.toExternalForm();
        artifactUrlTmp += "/" + this.getArtifact().getGroupId() + "/" + this.getFilename();
        try {
            return new URL(artifactUrlTmp);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Artifact URL resolution failed.", var6);
        }
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }
}