package com.ca.apm.systemtest.fld.plugin.tim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TimPluginImplTest {
    static final Logger log = LoggerFactory.getLogger(TimPluginImplTest.class);

    static final String PREFIX = "/opt";

    TimPlugin timPlugin;

    @BeforeMethod
    public void setUp() throws Exception {
        timPlugin = new TimPluginImpl();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        timPlugin = null;
    }

//    private void setTimSettings() {
//        // These two are copied from coda-logic/testlogic/em/fld/tim/flex.properties
//        timPlugin.setTimSetting("MaxFlexRequestBodySize", "100000");
//        timPlugin.setTimSetting("MaxFlexResponseBodySize", "100000");
//    }

    /**
     * Try to install TIM from artifactory.
     *
     * @param prefix
     */
    public void timInstallFromArtifactory(String prefix) {
//        final File prefixPath = new File(prefix);
//
//        timPlugin.createTempDir();
//        timPlugin.turnOffFirewall();
//        timPlugin.stopHttpd();
//
//        timPlugin.fetchInstallerArtifactFromArtifactory(
//            "com.ca.apm.cem", "tim-rhel6-dist", "99.99.sys-SNAPSHOT",
//            "Linux-el6-x64", "zip", null);
//
//        timPlugin.unzipInstallerArtifact();
//        timPlugin.extractInstallerTarGz();
//        timPlugin.acceptEula();
//        timPlugin.extractRpmsFromInstaller();
//        timPlugin.createPrefixDir(prefixPath.getAbsolutePath());
//        timPlugin.installExtractedRpms(prefixPath.getAbsolutePath());
//        timPlugin.restartHttpd();
//        timPlugin.configureInterface("eth2");
//        setTimSettings();
//        timPlugin.timStop();
//        timPlugin.timStart();
//        timPlugin.deleteTempDir();
    }

    /**
     * Try to install TIM from truss.
     *
     * @param prefix
     */
    public void timInstallFromTruss(String prefix) {
//        final File prefixPath = new File(prefix);
//
//        timPlugin.createTempDir();
//        timPlugin.turnOffFirewall();
//        timPlugin.stopHttpd();
//
//        timPlugin.fetchInstallerArtifactFromTruss("http://truss.ca.com/builds/InternalBuilds",
//            "9.7.0_APM_Release.TIM_RedHat_6.0_x64", "000024", "9.7.0.24",
//            "tim-9.7.0.24.24-4a3bace1b0bacc35d89acb81441422f1d650ef80.Linux.el6.x86_64-install"
//            + ".tar.gz");
//
//        timPlugin.extractInstallerTarGz();
//        timPlugin.acceptEula();
//        timPlugin.extractRpmsFromInstaller();
//        timPlugin.createPrefixDir(prefixPath.getAbsolutePath());
//        timPlugin.installExtractedRpms(prefixPath.getAbsolutePath());
//        timPlugin.restartHttpd();
//        timPlugin.configureInterface("eth2");
//        setTimSettings();
//        timPlugin.timStop();
//        timPlugin.timStart();
//        timPlugin.deleteTempDir();
    }

    @Test
    public void testTimInstall() throws Exception {
//        try {
//            //timInstallFromArtifactory(PREFIX);
//            //timPlugin.timUninstall(PREFIX);
//
//            timInstallFromTruss(PREFIX);
//            timPlugin.timUninstall(PREFIX);
//        } catch (Exception e) {
//            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
//                "Exception thrown by timInstall({1}). Exception {0}", PREFIX);
//        }
    }
}