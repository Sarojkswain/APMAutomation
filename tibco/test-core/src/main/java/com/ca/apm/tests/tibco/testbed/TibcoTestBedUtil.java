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

package com.ca.apm.tests.tibco.testbed;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.apm.tests.tibco.artifact.TibcoSoftwareComponentVersions;
import com.ca.apm.tests.tibco.flow.CreateDomainFlow;
import com.ca.apm.tests.tibco.flow.DeployTibcoFlowContext;
import com.ca.apm.tests.tibco.flow.StartBWServiceFlow;
import com.ca.apm.tests.tibco.flow.TibcoConstants;
import com.ca.apm.tests.tibco.role.TibcoRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;

/**
 * Default TestBedUtil. If customization is needed roles should be created in the TestBed itself.
 * 
 * @author Vashistha Singh (sinva01@ca.com)
 *
 */
public class TibcoTestBedUtil implements TibcoConstants {


    public static Set<Triplet> getTibcoRVInstallerResponsefileData(String installdir) {
        Set<Triplet> installResponseFileData = new LinkedHashSet<>();
        installResponseFileData.add(new Triplet("", "acceptLicense", true));
        installResponseFileData.add(new Triplet("", "installationRoot", installdir));
        installResponseFileData.add(new Triplet("", "createNewEnvironment", true));
        installResponseFileData.add(new Triplet("", "environmentName", "TIBCO-HOME"));
        installResponseFileData.add(new Triplet("", "feature_Runtime_rv", true));
        installResponseFileData.add(new Triplet("", "feature_Development_rv", true));
        installResponseFileData.add(new Triplet("", "feature_Source_rv", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_rv", true));
        installResponseFileData.add(new Triplet("", "feature_RVDM_rv", true));
        installResponseFileData.add(new Triplet("", "protocolDaemon", "trdp"));
        return installResponseFileData;
    }

    public static Set<Triplet> getTibcoEMSInstallerResponsefileData(String installdir) {
        Set<Triplet> installResponseFileData = new LinkedHashSet<>();
        installResponseFileData.add(new Triplet("", "acceptLicense", true));
        installResponseFileData.add(new Triplet("", "installationRoot", installdir));
        installResponseFileData.add(new Triplet("", "createNewEnvironment", false));
        installResponseFileData.add(new Triplet("", "environmentName", "TIBCO-HOME"));
        installResponseFileData.add(new Triplet("", "feature_Runtime_ems", true));
        installResponseFileData.add(new Triplet("", "feature_Development_ems", true));
        installResponseFileData.add(new Triplet("", "feature_Samples_ems", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_ems", true));
        installResponseFileData.add(new Triplet("", "manualRB", false));
        installResponseFileData.add(new Triplet("", "autoRB", true));
        installResponseFileData.add(new Triplet("", "configDirectoryRoot", TIBCO_EMS_CONFIG_DIR));
        installResponseFileData.add(new Triplet("", "configFile",
                TIBCO_EMS_CONFIG_DIR + "\\tibco\\cfgmgmt\\ems\\data\\tibemsd.conf"));
        return installResponseFileData;
    }

    public static IRole getTibcoRVRole(TibcoSoftwareComponentVersions version,
        ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir) {
        DeployTibcoFlowContext rvContext =
            new DeployTibcoFlowContext.Builder().roleId(TIBCO_RV_ROLE_ID)
                .installerUnpackDir(installUnpackDir)
                .artifactURL(tasResolver.getArtifactUrl(version.getArtifact()))
                .installDir(installdir)
                .responsefileData(getTibcoRVInstallerResponsefileData(installdir))
                .responseFileName("tibcorv.slient").installerLogFile(logFile).version(version)
                .build();
        String ver = version.getArtifact().getVersion();
        ver = ver.substring(0, ver.lastIndexOf('.'));
        String rvBin = installdir + "\\tibrv\\" + ver + "\\bin";

        // Command to remove the RV service
        RunCommandFlowContext command1 =
            new RunCommandFlowContext.Builder(RV_SERVICE_REG_WIN_EXECUTABLE).workDir(rvBin)
                .args(Arrays.asList("/r")).build();
        // Command to register the RV service        
        RunCommandFlowContext command2 =
            new RunCommandFlowContext.Builder(RV_SERVICE_REG_WIN_EXECUTABLE).workDir(rvBin)
                .args(Arrays.asList("/i", "/a", "rvd", rvBin, "\"" + rvBin + "\"")).build();

        TibcoRole tibcoRVRole =
            new TibcoRole.Builder(TIBCO_RV_ROLE_ID, tasResolver).flowContext(rvContext)
                .addPostInstallationFlow(RunCommandFlow.class, command1)
                .addPostInstallationFlow(RunCommandFlow.class, command2).build();

        return tibcoRVRole;
    }

    public static IRole getTibcoEMSRole(TibcoSoftwareComponentVersions version,
        ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir) {
        DeployTibcoFlowContext emsContext =
            new DeployTibcoFlowContext.Builder().roleId(TIBCO_EMS_ROLE_ID)
                .installerUnpackDir(installUnpackDir)
                .artifactURL(tasResolver.getArtifactUrl(version.getArtifact()))
                .installDir(installdir)
                .responsefileData(getTibcoEMSInstallerResponsefileData(installdir))
                .responseFileName("tibcoems.slient").installerLogFile(logFile).version(version)
                .build();
        TibcoRole tibcoRMSRole =
            new TibcoRole.Builder(TIBCO_EMS_ROLE_ID, tasResolver).flowContext(emsContext).build();

        return tibcoRMSRole;
    }


    public static IRole getTibcoTRARole(TibcoSoftwareComponentVersions version,
        ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir) {
        DeployTibcoFlowContext.Builder builder = new DeployTibcoFlowContext.Builder();
        DeployTibcoFlowContext traContext =
            builder.roleId(TIBCO_TRA_ROLE_ID).installerUnpackDir(installUnpackDir)
                .artifactURL(tasResolver.getArtifactUrl(version.getArtifact()))
                .installDir(installdir)
                .responsefileData(getTibcoTRAInstallerResponsefileData(installdir))
                .responseFileName("tibcotra.slient").installerLogFile(logFile).version(version)
                .build();
        TibcoRole tibcoTRARole =
            new TibcoRole.Builder(TIBCO_TRA_ROLE_ID, tasResolver).flowContext(traContext).build();

        return tibcoTRARole;
    }

    public static Set<Triplet> getTibcoTRAInstallerResponsefileData(String installDir) {
        Set<Triplet> installResponseFileData = new LinkedHashSet<>();
        installResponseFileData.add(new Triplet("", "acceptLicense", true));
        installResponseFileData.add(new Triplet("", "installationRoot", installDir));
        installResponseFileData.add(new Triplet("", "createNewEnvironment", false));
        installResponseFileData.add(new Triplet("", "environmentName", "TIBCO-HOME"));
        installResponseFileData.add(new Triplet("", "protocolDaemon", "trdp"));
        installResponseFileData.add(new Triplet("", "feature_Runtime_tpcl", true));
        installResponseFileData.add(new Triplet("", "feature_Agent_hawk", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_Designer", true));
        installResponseFileData.add(new Triplet("", "feature_Runtime_Designer", true));
        installResponseFileData.add(new Triplet("", "feature_Runtime_TRA", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_TRA", true));
        installResponseFileData.add(new Triplet("", "useVendorDriver", false));
        installResponseFileData.add(new Triplet("", "databaseType", ""));
        installResponseFileData.add(new Triplet("", "databaseDriver", ""));
        installResponseFileData.add(new Triplet("", "ociDirectory", ""));
        installResponseFileData.add(new Triplet("", "configDirectoryRoot", "C:\\tibco_cfg"));
        return installResponseFileData;
    }


    public static Set<Triplet> getTibcoBWInstallerResponsefileData(String installDir) {
        Set<Triplet> installResponseFileData = new LinkedHashSet<>();
        installResponseFileData.add(new Triplet("", "acceptLicense", true));
        installResponseFileData.add(new Triplet("", "installationRoot", installDir));
        installResponseFileData.add(new Triplet("", "createNewEnvironment", false));
        installResponseFileData.add(new Triplet("", "environmentName", "TIBCO-HOME"));
        installResponseFileData.add(new Triplet("", "feature_AMBW Runtime_BW", true));
        installResponseFileData.add(new Triplet("", "feature_AMBW Designtime_BW", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_BW", true));
        return installResponseFileData;
    }

    public static IRole getTibcoBWRole(TibcoSoftwareComponentVersions version,
        ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir) {
        DeployTibcoFlowContext traContext =
            new DeployTibcoFlowContext.Builder().roleId(TIBCO_BW_ROLE_ID)
                .installerUnpackDir(installUnpackDir)
                .artifactURL(tasResolver.getArtifactUrl(version.getArtifact()))
                .installDir(installdir)
                .responsefileData(getTibcoBWInstallerResponsefileData(installdir))
                .responseFileName("tibcobw.slient").installerLogFile(logFile).version(version)
                .build();

        TibcoRole tibcoRMSRole =
            new TibcoRole.Builder(TIBCO_BW_ROLE_ID, tasResolver).flowContext(traContext).build();

        return tibcoRMSRole;
    }

    public static Set<Triplet> getTibcoBWAdminInstallerResponsefileData(String installDir) {
        Set<Triplet> installResponseFileData = new LinkedHashSet<>();
        installResponseFileData.add(new Triplet("", "acceptLicense", true));
        installResponseFileData.add(new Triplet("", "installationRoot", installDir));
        installResponseFileData.add(new Triplet("", "createNewEnvironment", false));
        installResponseFileData.add(new Triplet("", "environmentName", "TIBCO-HOME"));
        installResponseFileData.add(new Triplet("", "feature_Repository_TIBCOAdmin", true));
        installResponseFileData.add(new Triplet("", "feature_Documentation_TIBCOAdmin", true));
        return installResponseFileData;
    }

    // Overload, so as to support user defined domain name function
    public static IRole getTibcoBWAdminRole(TibcoSoftwareComponentVersions version,
                                            ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir){
        IRole bwAdminRole = getTibcoBWAdminRole(version,
                tasResolver, logFile, installdir, installUnpackDir, "APM");
        return bwAdminRole;
    }

    public static IRole getTibcoBWAdminRole(TibcoSoftwareComponentVersions version,
                                            ITasResolver tasResolver, String logFile, String installdir, String installUnpackDir, String domainName) {
        DeployTibcoFlowContext bwAdminContext =
                new DeployTibcoFlowContext.Builder().roleId(TIBCO_ADMIN_ROLE_ID)
                        .installerUnpackDir(installUnpackDir)
                        .artifactURL(tasResolver.getArtifactUrl(version.getArtifact()))
                        .installDir(installdir)
                        .responsefileData(getTibcoBWAdminInstallerResponsefileData(installdir))
                        .responseFileName("tibcobwadmin.slient").installerLogFile(logFile)
                        .domainName(domainName).version(version).build();
        String ver = version.getArtifact().getVersion();
        ver = ver.substring(0, ver.lastIndexOf('.'));

        String traHome = installdir + "\\tra";
        String traversionDir = traHome + "\\" + ver;
        String traBinDir = traversionDir + "\\bin";

        String adminHome = bwAdminContext.getInstallDir() + "\\administrator";
        String adminVersionDir = adminHome + "\\" + ver;
        String adminBinDir = adminVersionDir + "\\bin";

        String domainFileName = adminBinDir + "\\createDomain.xml";

        String domainCreationLogFileName =
                installUnpackDir + "\\" + TIBCO_ADMIN_ROLE_ID + "\\logs\\" + "createDomain.log";

        RunCommandFlowContext commandcontext =
                new RunCommandFlowContext.Builder(DOMAIN_UTILITY_WIN_EXECUTABLE)
                        .workDir(traBinDir)
                        .args(
                                Arrays
                                        .asList("-cmdFile", domainFileName, "-logFile", domainCreationLogFileName))
                        .build();


        TibcoRole tibcoBWAdminRole =
                new TibcoRole.Builder(TIBCO_ADMIN_ROLE_ID, tasResolver).flowContext(bwAdminContext)
                        .addPostInstallationFlow(CreateDomainFlow.class, bwAdminContext)
                        .addPostInstallationFlow(RunCommandFlow.class, commandcontext)
                        .addPostInstallationFlow(StartBWServiceFlow.class, bwAdminContext).build();

        return tibcoBWAdminRole;
    }

}
