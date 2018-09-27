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


package com.ca.apm.siteminder;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * This is a temporary addition to Utils class from automation-flows with modification to take env variables.
 * It can be safely removed once it is available there.
 */
public class Utils extends com.ca.apm.automation.action.utils.Utils {
    public static int exec(@NotNull String workingDir, @NotNull String command, String[] args, final Logger log, Map<String, String> env) throws CommandLineException {
        Commandline cmd = new Commandline();
        cmd.setExecutable(command);
        if (StringUtils.isNotBlank(workingDir)) {
            cmd.setWorkingDirectory(new File(workingDir));
        }
        if (args != null) {
            cmd.addArguments(args);
        }
        if (env != null) {
            for (Entry<String, String> entry : env.entrySet()) {
                cmd.addEnvironment(entry.getKey(), entry.getValue());
            }
        }
        log.info("Executing command");
        log.info(" Working Directory : " + workingDir);
        log.info(" Command : " + command);
        if (args != null) {
            for (String s : args) {
                log.info(" arg : " + s);
            }
        }
        if (env != null) {
            for (Entry<String, String> entry : env.entrySet()) {
                log.info(" env : " + entry.getKey() + " = " + entry.getValue());
            }
        }

        int ret = CommandLineUtils.executeCommandLine(cmd, new StreamConsumer() {
                @Override
                public void consumeLine(String line) {
                    log.info(" stdout : " + line);
                }
            }, new StreamConsumer() {
                @Override
                public void consumeLine(String line) {
                    log.info(" stderr : " + line);
                }
            });

        log.info("Exited with code " + ret);
        return ret;
    }
}
