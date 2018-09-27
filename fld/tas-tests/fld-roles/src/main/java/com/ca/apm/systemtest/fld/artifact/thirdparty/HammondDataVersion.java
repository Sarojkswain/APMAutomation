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
package com.ca.apm.systemtest.fld.artifact.thirdparty;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IThirdPartyArtifact;

public enum HammondDataVersion implements IThirdPartyArtifact {

    // FLD
    FLD_mainframe("fld", "mainframe", HammondDataVersion.GROUP_ID_coda_em_performance_hammond, HammondDataVersion.VERSION_coda_em_performance_hammond),
    FLD_tomcat("fld", "tomcat", HammondDataVersion.GROUP_ID_coda_em_performance_hammond, HammondDataVersion.VERSION_coda_em_performance_hammond),

    // Transaction Trace storm load
    TransactionTraceStormLoad("tt-storm-hammond-load", "", HammondDataVersion.GROUP_ID_systemtest_perf_transaction_trace_storm_load, 
        HammondDataVersion.VERSION_systemtest_perf_transaction_trace_storm_load);

    private static final String GROUP_ID_coda_em_performance_hammond = "com.ca.apm.coda.em-performance.hammond";
    private static final String VERSION_coda_em_performance_hammond = "1.0";

    private static final String GROUP_ID_systemtest_perf_transaction_trace_storm_load = "com.ca.apm.systemtest.perf.ttstorm";
    private static final String VERSION_systemtest_perf_transaction_trace_storm_load = "1.0";

    private final DefaultArtifact artifact;
    private final String artifactId;
    private final String classifier;
    private final String groupId;
    private final String version;
    private final String type = TasExtension.ZIP.getValue();

    /**
     * @param artifactId
     * @param classifier
     * @param groupId
     * @param version
     */
    private HammondDataVersion(String artifactId, String classifier, String groupId, String version) {
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.groupId = groupId;
        this.version = version;
        this.artifact = new DefaultArtifact(this.groupId, this.artifactId, this.classifier, this.type, this.version);
    }

    /** {@inheritDoc} */
    @Override
    public Artifact getArtifact() {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getExtension(), artifact.getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public String getFilename() {
        return classifier == null ? String.format("%s-%s.%s", artifactId, version, type) : String.format("%s-%s-%s.%s", artifactId, version, classifier, type);
    }

    public String getVersion() {
        return version;
    }

}
