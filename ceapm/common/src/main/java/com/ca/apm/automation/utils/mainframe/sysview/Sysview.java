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

package com.ca.apm.automation.utils.mainframe.sysview;

import com.ibm.jzos.Exec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Utility class that provides ways of interacting with SYSVIEW.
 * Allows to execute SYSVIEW commands through its REXX interface and work with the returned data.
 *
 * <p>If the {@link #execute(String, Object...)} method is called at least once the {@link #close()}
 * method has to be called once the instance is no longer used to cleanup deployed resources. The
 * class also supports the {@link AutoCloseable} interface to simplify this process.
 */
public class Sysview implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Sysview.class);
    private static final String COMMAND_SCRIPT = "sysviewCommand.rex";
    private static final int VERBATIM_LIMIT = 100;

    private String loadlib = null;
    private File script;
    private boolean verbatim = false;

    /**
     * Possible return code values from SYSVIEW command execution.
     *
     * @see <a href="https://docops.ca.com/display/CSPM10/Data+Returned+from+the+API">doc</a>
     */
    public enum Rc {
        /** Command executed, no messages are returned. */
        OK_NOMSG(0),
        /** Informational message returned. */
        OK_INFO_MSG(4),
        /** Action messages are returned. */
        OK_ACTION_MSG(8),
        /** Warning messages are returned. */
        WARN(12),
        /** Error messages are returned. */
        ERROR(16),
        /** Termination return code. The API has terminated. */
        TERM(20);

        private int value;

        private static final Collection<Rc> OK_VALUES = Collections.unmodifiableCollection(Arrays
            .asList(OK_NOMSG, OK_INFO_MSG, OK_ACTION_MSG));

        Rc(int value) {
            this.value = value;
        }

        /**
         * Returns an {@link Rc} instance that corresponds to the specified raw return value.
         *
         * @param value Raw return code value.
         * @return Rc instance.
         * @throws IllegalArgumentException if the raw value is not recognized.
         */
        public static Rc fromValue(int value) {
            for (Rc rc : values()) {
                if (rc.getValue() == value) {
                    return rc;
                }
            }

            throw new IllegalArgumentException("The value " + value
                + " does not correspond to a recognized SYSVIEW return code.");
        }

        /**
         * Returns the raw return code value.
         *
         * @return Raw return code.
         */
        public int getValue() {
            return value;
        }

        public static Collection<Rc> getOkValues() {
            return new ArrayList<>(OK_VALUES);
        }

        /**
         * Indicates whether the return value corresponds to a successful command execution
         * regardless of what messages, if any, were returned.
         *
         * @return {@link true} if the value indicates successful command execution, {@link false}
         *         otherwise.
         */
        public boolean isOk() {
            return OK_VALUES.contains(this);
        }
    }

    /**
     * Constructor that uses an explicit SYSVIEW instance.
     *
     * @param loadlib Load library of the SYSVIEW instance to use. Can be null or an empty string in
     *        which case the default instance is used.
     * @throws IOException When deployment of the prerequisite {@link #COMMAND_SCRIPT} script fails.
     */
    public Sysview(@Nullable String loadlib) throws IOException {
        this();

        this.loadlib = loadlib;
    }

    /**
     * Constructor that uses the default SYSVIEW instance.
     *
     * @throws IOException When deployment of the prerequisite {@link #COMMAND_SCRIPT} script fails.
     */
    public Sysview() throws IOException {
        deployScript();
    }

    /**
     * Executes a SYSVIEW command. Blocks until the command finishes or times out.
     *
     * <p>This overload provides the ability to pass the command in a {@link MessageFormat}
     * compatible manner.
     *
     * @param pattern Pattern of command to execute.
     * @param arguments Arguments to be substituted in the command pattern.
     * @return An {@link ExecResult} instance with information about the execution.
     * @throws IOException When execution of the command fails.
     */
    public synchronized ExecResult execute(String pattern, Object... arguments) throws IOException {
        Args.notBlank(pattern, "pattern");

        return execute(MessageFormat.format(pattern, arguments));
    }

    /**
     * Executes a SYSVIEW command. Blocks until the command finishes or times out.
     *
     * @param command Command to execute.
     * @return An {@link ExecResult} instance with information about the execution.
     * @throws IOException When execution of the command fails.
     */
    public synchronized ExecResult execute(String command) throws IOException {
        Args.notBlank(command, "command");

        try {
            if (script == null) {
                deployScript();
            }
            assert script != null;

            String steplibCommand;
            if (loadlib != null && !loadlib.isEmpty()) {
                steplibCommand = "export STEPLIB=" + loadlib + "; ";
            } else {
                steplibCommand = "";
            }
            // escape special shell characters (using single quotes, escaping single quotes first)
            command = command.replace("'", "'\\''");
            command = "'" + command + "'";
            String[] args =
                {"sh", "-c", steplibCommand + script.getAbsolutePath() + " " + command};

            final List<String> output = new ArrayList<>();

            logger.debug("Executing: " + StringUtils.join(args, ' '));
            final Exec exec = new Exec(args);
            exec.setTimeout(120_000);
            Thread reader = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (BufferedReader o =
                        new BufferedReader(new InputStreamReader(exec.getStdoutStream()))) {
                        String line;
                        while ((line = o.readLine()) != null) {
                            output.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            exec.run();
            reader.start();
            int rc = exec.getReturnCode();
            while (reader.isAlive()) {
                Thread.yield();
            }

            logger.debug("Command finished with rc={}", rc);
            if (verbatim) {
                if (output.size() <= VERBATIM_LIMIT) {
                    logger.debug(StringUtils.join(output, System.lineSeparator()));
                } else {
                    List<String> head = output.subList(0, VERBATIM_LIMIT / 2);
                    List<String> tail =
                        output.subList(output.size() - VERBATIM_LIMIT / 2, output.size());
                    logger.debug("{}\n(omitted {} lines)\n{}", StringUtils.join(head, "\n"),
                        output.size() - head.size() - tail.size(), StringUtils.join(tail, "\n"));
                }
            }
            return new ExecResult(Rc.fromValue(rc), output);
        } catch (IOException e) {
            throw new IOException("Failed to execute SYSVIEW command", e);
        }
    }

    /**
     * Deploys the REXX script required to execute commands.
     *
     * @throws IOException When deployment of the script fails.
     */
    private void deployScript() throws IOException {
        // Save the SYSVIEW command script from resources to a temporary file.
        URL url = Sysview.class.getResource(COMMAND_SCRIPT);
        script = File.createTempFile(COMMAND_SCRIPT, null);
        logger.debug("Deploying support REXX script to: " + script.getAbsolutePath());
        FileUtils.copyURLToFile(url, script);
        if (!script.setExecutable(true, false)) {
            logger.warn("Failed to set support REXX script as executable");
        }
    }

    /**
     * Removes any temporary files created by the instance.
     */
    public void cleanup() {
        if (!script.delete()) {
            logger.warn("Failed to cleanup support REXX script from {}", script.getAbsolutePath());
        }
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Turn verbatim output from executed commands on or off.
     *
     * @param verbatim {@code true} to turn extra output on, {@code false} otherwise.
     */
    public void verbatim(boolean verbatim) {
        this.verbatim = verbatim;
    }

    /**
     * Holds all the output information from an execution of a SYSVIEW command.
     */
    public class ExecResult {
        private final Rc rc;
        private final List<String> output;
        private TabularData tabularData = null;

        private ExecResult(Rc rc, List<String> output) {
            this.rc = rc;
            this.output = output;
        }

        /**
         * Returns the return code of the command execution.
         *
         * @return Return code.
         */
        public Rc getRc() {
            return rc;
        }

        /**
         * Returns the output of the command execution.
         *
         * @return List of lines captured during the command execution.
         */
        public List<String> getOutput() {
            return output;
        }

        /**
         * Returns a {@link TabularData} instance based on the output of the command execution.
         *
         * @return Tabular data.
         * @throws IOException When parsing of the command output fails.
         */
        public TabularData getTabularData() throws IOException {
            if (tabularData == null) {
                tabularData = TabularData.fromRexxCommandOutput(output);
            }

            return tabularData;
        }

        /**
         * Identifies whether a text fragment is part of the execution output.
         * The output is checked line by line.
         *
         * @param needle Text to look for.
         * @return true if the text is found, false otherwise.
         */
        public boolean outputContains(String needle) {
            Args.notNull(needle, "needle");

            return outputContains(Pattern.compile(Pattern.quote(needle)));
        }

        /**
         * Identifies whether a pattern is part of the execution output.
         * The output is checked line by line.
         *
         * @param pattern Pattern to look for.
         * @return true if the pattern is found, false otherwise.
         */
        public boolean outputContains(Pattern pattern) {
            Args.notNull(pattern, "pattern");

            for (String line : output) {
                if (pattern.matcher(line).find()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return StringUtils.join(getOutput(), "\n");
        }
    }
}
