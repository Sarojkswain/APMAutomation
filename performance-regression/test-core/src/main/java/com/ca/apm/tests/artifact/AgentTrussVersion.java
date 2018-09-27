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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
 * Java APM Agent Artifact in TRUSS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public enum AgentTrussVersion implements TrussArtifact {


    ISCP_10_0_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990010", "10.0.0.12", "10.0.0", TasExtension.ZIP),
    ISCP_10_0_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990010", "10.0.0.12", "10.0.0", TasExtension.ZIP),
    ISCP_10_0_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "990010", "10.0.0.12", "10.0.0", TasExtension.ZIP),

    ISCP_10_1_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990014", "10.1.0.15", "10.1.0", TasExtension.ZIP),
    ISCP_10_1_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990014", "10.1.0.15", "10.1.0", TasExtension.ZIP),
    ISCP_10_1_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "990014", "10.1.0.15", "10.1.0", TasExtension.ZIP),

    ISCP_10_2_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990022", "10.2.0.22", "10.2.0", TasExtension.ZIP),
    ISCP_10_2_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990022", "10.2.0.22", "10.2.0", TasExtension.ZIP),
    ISCP_10_2_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "990022", "10.2.0.22", "10.2.0", TasExtension.ZIP),

//  ISCP_10_3_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990015", "10.3.0.15", "10.3.0", TasExtension.ZIP),
//  ISCP_10_3_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990015", "10.3.0.15", "10.3.0", TasExtension.ZIP),
//  ISCP_10_3_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "990015", "10.3.0.15", "10.3.0", TasExtension.ZIP),
    ISCP_10_3_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990101", "10.5.2.16", "10.5.2", TasExtension.ZIP),
    ISCP_10_3_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990101", "10.5.2.16", "10.5.2", TasExtension.ZIP),
    ISCP_10_3_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows",  "990101", "10.5.2.16", "10.5.2", TasExtension.ZIP),


    ISCP_10_5_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "990304", "10.5.0.28", "10.5.0", TasExtension.ZIP),
    ISCP_10_5_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "990304", "10.5.0.28", "10.5.0", TasExtension.ZIP),
    ISCP_10_5_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "990304", "10.5.0.28", "10.5.0", TasExtension.ZIP),

    ISCP_10_5_1_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "000005", "10.5.0.2SP1", "10.5.2SP1", TasExtension.ZIP),
    ISCP_10_5_1_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "000005", "10.5.0.2SP1", "10.5.2SP1", TasExtension.ZIP),
    ISCP_10_5_1_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "000005", "10.5.0.2SP1", "10.5.2SP1", TasExtension.ZIP),

    ISCP_10_5_2_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "991601", "10.5.2.46", "10.5.2", TasExtension.ZIP),
    ISCP_10_5_2_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "991601", "10.5.2.46", "10.5.2", TasExtension.ZIP),
    ISCP_10_5_2_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "991601", "10.5.2.46", "10.5.2", TasExtension.ZIP),

    ISCP_10_7_0_GA_TOMCAT_WIN("IntroscopeAgentFiles-NoInstaller", "tomcat", "windows", "000086", "10.7.0.0_dev", "10.7.0_dev", TasExtension.ZIP, TRUSS_SC_URL),
    ISCP_10_7_0_GA_WAS_WIN("IntroscopeAgentFiles-NoInstaller", "websphere", "windows", "000086", "10.7.0.0_dev", "10.7.0_dev", TasExtension.ZIP, TRUSS_SC_URL),
    ISCP_10_7_0_GA_WLS_WIN("IntroscopeAgentFiles-NoInstaller", "weblogic", "windows", "000086", "10.7.0.0_dev", "10.7.0_dev", TasExtension.ZIP, TRUSS_SC_URL);

    public static final String NOINSTALLER_FILE_NAME = " IntroscopeAgentFiles-NoInstaller";

    private static final String DEFAULT_TRUSS_URL = TRUSS_URL;

    private final DefaultArtifact artifact;

    private final String buildNumber;
    private final String shortVersion;
    private final String version;
    private final String os;
    @SuppressWarnings("unused")
    private final String appServer;
    private final String baseTrussUrl;




    /**
     * @param archiveFileName name of the file in Artifactory
     * @param appServer       application server - tomcat/weblogic/websphere/jboss/default/interstage/allappserver
     * @param os              operating system - windows/unix/os400
     * @param buildNumber     build number (e.g. 990014)
     * @param version         full version number (e.g. 10.1.0.15)
     * @param shortVersion    shortened version number (e.g. 10.1.0)
     * @param extension       file extension
     */
    private AgentTrussVersion(String archiveFileName, String appServer, String os, String buildNumber, String version, String shortVersion, TasExtension extension) {
        this(archiveFileName, appServer, os, buildNumber, version, shortVersion, extension, DEFAULT_TRUSS_URL);
    }



    /**
     * @param archiveFileName name of the file in Artifactory
     * @param appServer       application server - tomcat/weblogic/websphere/jboss/default/interstage/allappserver
     * @param os              operating system - windows/unix/os400
     * @param buildNumber     build number (e.g. 990014)
     * @param version         full version number (e.g. 10.1.0.15)
     * @param shortVersion    shortened version number (e.g. 10.1.0)
     * @param extension       file extension
     * @param baseTrussUrl    Truss URL
     */
    private AgentTrussVersion(String archiveFileName, String appServer, String os, String buildNumber, String version, String shortVersion, TasExtension extension, String baseTrussUrl) {
        this.buildNumber = buildNumber;
        this.shortVersion = shortVersion;
        this.version = version;
        this.os = os;
        this.appServer = appServer;
        this.baseTrussUrl = baseTrussUrl;

        String artifactId = getArtifactId(archiveFileName, appServer, os, version);
        String groupId = getGroupId(buildNumber, version, shortVersion);
        artifact = new DefaultArtifact(groupId, artifactId, extension.getValue(), version);
    }

    /**
     * Artifact from Artifactory
     * <p/>
     * URL in format http://artifactory-emea-cz.ca.com:8081/artifactory/repo/com/ca/apm/delivery/agent-noinstaller-tomcat-windows/10.3.0.3/agent-noinstaller-tomcat-windows-10.3.0.3.zip
     *
     * @param artifactory artifactory URL
     * @return URL of the artifact
     */
    public static URL getArtifactUrl(URL artifactory, String version, String appServer, String os) {
        String artifactUrlTmp = artifactory.toExternalForm();
        artifactUrlTmp += "/com/ca/apm/delivery/agent-noinstaller-" + appServer + "-" + os + "/" + version + "/agent-noinstaller-" + appServer + "-" + os + "-" + version + ".zip";
        try {
            return new URL(artifactUrlTmp);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Artifact URL resolution failed.", var6);
        }
    }

    public static URL getArtifactUrl(ITasResolver resolver, String version, String appServer, String os) {
        ITasArtifact artifact = (new TasArtifact.Builder("agent-noinstaller-" + appServer + "-" + os)).groupId("com.ca.apm.delivery")
                .version(version == null ? resolver.getDefaultVersion() : version).extension(TasExtension.ZIP).build();
        return resolver.getArtifactUrl(artifact);
    }

    public static String getGroupId(String buildNumber, String version, String shortVersion) {
        return shortVersion + "-ISCP/build-" + buildNumber + "(" + version + ")/introscope" + version;
    }

    public static String getArtifactId(String archiveFileName, String appServer, String os, String version) {
        return archiveFileName + version + appServer + "." + os;
    }

    /**
     * Artifact from Truss
     * <p/>
     * URL in format http://truss.ca.com/builds/InternalBuilds/10.1.0-NET/build-990014(10.1.0.15)/...
     *
     * @param appServer    application server - tomcat/weblogic/websphere/jboss/default/interstage/allappserver
     * @param os           operating system - windows/unix/os400
     * @param buildNumber  build number (e.g. 990014)
     * @param version      full version number (e.g. 10.1.0.15)
     * @param shortVersion shortened version number (e.g. 10.1.0)
     * @param extension    file extension
     * @return URL of the artifact
     */
    public static URL getArtifactUrl(String appServer, String os, String buildNumber, String version, String shortVersion, TasExtension extension) {
        return getArtifactUrl(appServer, os, buildNumber, version, shortVersion, extension, DEFAULT_TRUSS_URL);
    }

    /**
     * Artifact from Truss
     * <p/>
     * URL in format http://truss.ca.com/builds/InternalBuilds/10.1.0-NET/build-990014(10.1.0.15)/...
     *
     * @param appServer    application server - tomcat/weblogic/websphere/jboss/default/interstage/allappserver
     * @param os           operating system - windows/unix/os400
     * @param buildNumber  build number (e.g. 990014)
     * @param version      full version number (e.g. 10.1.0.15)
     * @param shortVersion shortened version number (e.g. 10.1.0)
     * @param extension    file extension
     * @param baseTrussUrl Truss URL
     * @return URL of the artifact
     */
    public static URL getArtifactUrl(String appServer, String os, String buildNumber, String version, String shortVersion, TasExtension extension, String baseTrussUrl) {
        String artifactUrlTmp = baseTrussUrl;

        String artifactId = getArtifactId(NOINSTALLER_FILE_NAME, appServer, os, version);
        String groupId = getGroupId(buildNumber, version, shortVersion);

        artifactUrlTmp += "/" + groupId + "/" + String.format("%s.%s", artifactId, extension);

        try {
            return new URL(artifactUrlTmp);
        } catch (MalformedURLException var6) {
            throw new IllegalArgumentException("Artifact URL resolution failed.", var6);
        }
    }

    public String getOs() {
        return os;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String getFilename() {
        Artifact artifact = getArtifact();
        return String.format("%s.%s", artifact.getArtifactId(), artifact.getExtension());
    }

    /**
     * Artifact from Truss
     * <p/>
     * URL in format http://truss.ca.com/builds/InternalBuilds/10.1.0-NET/build-990014(10.1.0.15)
     *
     * @return URL of the artifact
     */
    public URL getArtifactUrl() {
        String artifactUrlTmp = this.baseTrussUrl;
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
