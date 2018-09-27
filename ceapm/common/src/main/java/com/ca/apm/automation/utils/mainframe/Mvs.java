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

package com.ca.apm.automation.utils.mainframe;

import com.ibm.jzos.Exec;
import com.ibm.jzos.RcException;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileException;
import com.ibm.jzos.ZUtil;

import org.apache.commons.io.FileUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utility class for interacting with MVS.
 * Allows execution of MVS commands.
 */
public class Mvs implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Mvs.class);
    private static final String COMMAND_SCRIPT = "mvsCommand.rex";

    /** Data set path qualifier pattern */
    private static final String MVS_PATH_QUALIFIER = "[A-Z$@#][A-Z$@#-}]*";
    /** Data set and member pattern */
    private static final Pattern MVS_MEMBER_PATH_PATTERN = Pattern.compile(MVS_PATH_QUALIFIER
        + "(\\." + MVS_PATH_QUALIFIER + ")*" + "\\(" + MVS_PATH_QUALIFIER + "\\)");

    private File script;
    private InputStream stdout = null;

    /**
     * Constructor.
     *
     * @throws IOException When deployment of the prerequisite {@link #COMMAND_SCRIPT} script fails.
     */
    public Mvs() throws IOException {
        deployScript();
    }

    /**
     * Provides access to the output stream of the most recent command execution.
     * Guaranteed to be null before {@link Mvs#execute(String)} is run for the
     * first time.
     *
     * @return Stream with the output of the command.
     */
    public synchronized InputStream getStdoutStream() {
        return stdout;
    }

    /**
     * Executes the MVS command.
     *
     * @param command Command to execute.
     * @return Command return code.
     * @throws IOException When execution of the command fails unexpectedly.
     */
    public synchronized int execute(String command) throws IOException {
        Args.notBlank(command, "command");

        try {
            if (script == null) {
                deployScript();
            }
            assert script != null;

            logger.debug("Executing: " + script.getAbsolutePath() + " " + command);
            String[] args = {script.getAbsolutePath(), command};
            Exec exec = new Exec(args);
            exec.run();
            stdout = exec.getStdoutStream();

            return exec.getReturnCode();
        } catch (IOException e) {
            throw new IOException("Failed to execute mvs command", e);
        }
    }

    /**
     * Deploys the REXX script required to execute commands.
     *
     * @throws IOException When deployment of the script fails.
     */
    private void deployScript() throws IOException {
        // Save the MVS command script from resources to a temporary file.
        URL url = Mvs.class.getResource(COMMAND_SCRIPT);
        script = File.createTempFile(COMMAND_SCRIPT, null);
        logger.debug("Deploying support REXX script to: " + script.getAbsolutePath());
        FileUtils.copyURLToFile(url, script);
        script.setExecutable(true, false);
    }

    /**
     * Removes any temporary files created by the instance.
     */
    public void cleanup() {
        script.delete();
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Write text member into existing partitioned data set. Existing contents are overwritten.
     *
     * @param path Absolute member path.
     * @param contents Text contents.
     * @throws IOException When operation fails.
     */
    public static void writeDatasetMember(String path, String contents) throws IOException {
        Args.check(MVS_MEMBER_PATH_PATTERN.matcher(path).matches(), "invalid member path " + path);

        ZFile member = null;
        IOException primaryEx = null;
        try {
            member = new ZFile("//'" + path + "'", "w", ZFile.FLAG_DISP_SHR + ZFile.FLAG_PDS_ENQ);
            member.write(contents.getBytes(ZUtil.getDefaultPlatformEncoding()));
        } catch (RcException | IOException e) {
            primaryEx = new IOException("Failed to write member " + path, e);
            throw primaryEx;
        } finally {
            if (member != null) {
                try {
                    member.close();
                } catch (ZFileException | RcException e) {
                    IOException closeEx = new IOException("Failed to close file", e);
                    if (primaryEx == null) {
                        throw closeEx;
                    } else {
                        primaryEx.addSuppressed(closeEx);
                        throw primaryEx;
                    }
                }
            }
        }
    }
}
