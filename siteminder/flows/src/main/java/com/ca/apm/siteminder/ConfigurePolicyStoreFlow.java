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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class ConfigurePolicyStoreFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurePolicyStoreFlow.class);

    @FlowContext
    private ConfigurePolicyStoreFlowContext context;

    @Override
    public void run() throws Exception {
        String psRootDir = context.getPsRootDir();

        // set env variables that are set during cadir and ps installation
        Map<String, String> env = new HashMap<>();
        env.put("NETEGRITY_LICENSE_FILE", context.getPsRootDir() + "/license/license.dat");
        env.put("NETE_PS_ROOT", context.getPsRootDir());
        env.put("NETE_JVM_OPTION_FILE", context.getPsRootDir() + "/config/JVMOptions.txt");
        env.put("NETE_PS_OPACK", "INSTALLED");
        env.put("NETE_PS_PATH",
            context.getPsRootDir() + "/bin;" + psRootDir + "/bin/thirdparty;" + psRootDir + "/lib;"
                + psRootDir + "/bin/thirdparty/axis2c/lib");
        env.put("NETE_SHORTCUTS", "C:/ProgramData/Microsoft/Windows/Start Menu/Programs/CA/SiteMinder");
        final String jre = context.getJavaRE();
        env.put("NETE_JAVA_PATH", jre + "/bin;" + jre + "/bin/server");
        env.put("NETE_JRE_ROOT", jre);
        String path = System.getenv("Path");
        String newPath = path + ";" + psRootDir + "/bin;" + psRootDir + "/bin/thirdparty";
        env.put("PATH", newPath);

        String[] args = {"/C", "smldapsetup", "reg",
            "-h" + context.getLdapHost(),
            "-p" + context.getLdapPort(),
            "-d" + context.getLdapUser(),
            "-w" + context.getLdapPass(), "-r" + context.getLdapRoot()};

        // response code from the cmd is always 0 - need to update script and add err handling
        final int responseCode = Utils.exec(psRootDir + "/bin", "cmd", args, LOGGER, env);

        final int responseCode2 = Utils.exec(psRootDir
            + "/bin", "cmd", new String[] {"/C", "smldapsetup", "switch", "-ps", "-ks"}, LOGGER, env);

        final int responseCode3 = Utils.exec(psRootDir
            + "/bin", "cmd", new String[] {"/C", "smreg", "-su", "siteminder"}, LOGGER, env);

        final int responseCode4 = Utils.exec(psRootDir
            + "/xps/dd", "cmd", new String[] {"/C", "XPSDDInstall", "SmMaster.xdd"}, LOGGER, env);

        final int responseCode5 = Utils.exec(psRootDir
            + "/db", "cmd", new String[] {"/C", "XPSImport", "smpolicy.xml", "-npass"}, LOGGER, env);

        final int responseCode6 = Utils.exec(psRootDir
            + "/bin", "cmd", new String[] {"/C", "XPSRegClient", "siteminder:siteminder", "-adminui-setup"}, LOGGER, env);

        final int responseCode7 = Utils.exec(
            psRootDir + "/bin", "cmd", new String[] {"/C", "sc", "start", "SmPolicySrv"}, LOGGER);

        File plFile = new File(context.getPsRootDir() + "/CLI/bin/_smconfig_create.pl");
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("smconfig_create.pl"), plFile);
        int responseCode8 = Utils.exec(psRootDir
            + "/CLI/bin", "cmd", new String[] {"/C", "perl.exe", plFile.getAbsolutePath()}, LOGGER, env);

    }

}
