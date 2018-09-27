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

package com.ca.apm.automation.action.test;

import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Clw {
    private static final Logger logger = LoggerFactory.getLogger(Clw.class);
    private static final String[] ESCAPE_SYMBOLS = {"(", ")", ":", "?", "|"};

    private final String emHost;
    private final String user;
    private final String password;
    private final int port;
    private final String clWorkstationJarFileLocation;
    private final String javaPath;

    public interface Consumer<T> {
        public void consume(T param);
    }

    protected Clw(Builder builder) {
        emHost = builder.host;
        user = builder.user;
        password = builder.password;
        port = builder.port;
        clWorkstationJarFileLocation = builder.clWorkstationJarFileLocation;
        javaPath = builder.javaPath;
    }

    /**
     * Queries metrics from EM parses the results into a Map.
     *
     * @param agentRegularExpression Regular expression identifying the agent(s) to query.
     * @param start Start of the queried time frame.
     * @param end End of the queried time frame.
     * @return Metrics data.
     */
    public List<Map<String, String>> getUniqueMetricsFromAgents(String agentRegularExpression,
        Calendar start,
        Calendar end) {
        agentRegularExpression = escapeSymbols(agentRegularExpression);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        final String command =
            "get historical data from agents matching \"(" + agentRegularExpression
                + ")\" and metrics matching \"(.*)\" "
                + "between " + startFormated + " and " + endFormated
                + " with frequency of 15 s";

        return runParseOutput(command);
    }

    public Document getTransactions(String agentRegularExpression, int duration) {
        try {
            final String command = "trace transactions exceeding 1 millisecond in agents matching \"("
                + escapeSymbols(agentRegularExpression) + ")\" for " + duration + " second";

            File outFile = File.createTempFile("tracedata", ".xml");

            logger.info("Running CLW command '{}' on {}:{}", command, emHost, port);
            final List<String> args = new ArrayList<>();
            args.add(javaPath);
            args.add("-Xmx256M");
            args.add("-Duser=" + escape(user));
            args.add("-Dpassword=" + escape(password));
            args.add("-Dhost=" + emHost);
            args.add("-Dport=" + port);
            args.add("-Dintroscope.clw.tt.dirname=" + outFile.getParent());
            args.add("-Dintroscope.clw.tt.filename=" + outFile.getName());
            args.add("-jar");
            args.add(clWorkstationJarFileLocation);
            args.add(command);

            logger.info("Running the CLW command : {}", args.toString().replace(",", ""));

            final ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            while (isAlive(process)) {
                Thread.sleep(100);
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(outFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    protected List<Map<String, String>> runParseOutput(String command) {
        logger.info("Running CLW command '{}' on {}:{}", command, emHost, port);
        final List<String> args = new ArrayList<>();
        args.add(javaPath);
        args.add("-Xmx256M");
        args.add("-Duser=" + escape(user));
        args.add("-Dpassword=" + escape(password));
        args.add("-Dhost=" + emHost);
        args.add("-Dport=" + port);
        args.add("-jar");
        args.add(clWorkstationJarFileLocation);
        args.add(command);
        final ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);

        logger.info("Running the CLW command : {}", args.toString().replace(",", ""));

        Process process;

        try {
            List<Map<String, String>> metrics = new ArrayList<Map<String, String>>();
            process = pb.start();
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream())) {
                try (CSVParser csv = new CSVParser(isr, CSVFormat.DEFAULT.withCommentMarker('?')
                    .withIgnoreSurroundingSpaces().withHeader())) {
                    while (isAlive(process)) {
                        // Read and parse the returned data throughout the execution.
                        for (CSVRecord record : csv.getRecords()) {
                            metrics.add(record.toMap());
                        }
                        Thread.yield();
                    }
                }
            }

            return metrics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String escapeSymbols(String source) {
        if ((source == null) || source.isEmpty()) {
            return source;
        }
        for (String str : ESCAPE_SYMBOLS) {
            source = source.replace(str, "\\" + str);
        }

        return source;
    }

    protected static boolean isAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    protected String escape(String str) {
        if (str == null) {
            return null;
        }

        if (str.trim().contains(" ")) {
            return "\"" + str + "\"";
        }

        return str;
    }

    public static class Builder {
        private static final String DEFAULT_HOST = "localhost";
        private static final String DEFAULT_USER = "Admin";
        private static final String DEFAULT_PASSWORD = "";
        private static final int DEFAULT_PORT = 5001;
        private static final String DEFAULT_CLW_FILENAME = "CLWorkstation.jar";
        private static final String DEFAULT_JAVA = "java";

        protected String host = DEFAULT_HOST;
        protected String user = DEFAULT_USER;
        protected String password = DEFAULT_PASSWORD;
        protected int port = DEFAULT_PORT;
        protected String clWorkstationJarFileLocation;
        protected String clwWorkStationDir;
        protected String javaPath = DEFAULT_JAVA;

        /**
         * Builds a {@link Clw} instance.
         *
         * @return Clw instance.
         */
        public Clw build() {
            clWorkstationJarFileLocation = clwWorkStationDir + DEFAULT_CLW_FILENAME;

            return getInstance();
        }

        /**
         * Sets a path to the directory containing CLWorkstation.jar.
         *
         * @param location Directory path.
         * @return Builder instance the method was called on.
         */
        public Builder clwWorkStationDir(String location) {
            clwWorkStationDir = location;

            return builder();
        }

        public Builder host(String host) {
            this.host = host;
            return builder();
        }

        public Builder javaPath(String javaPath) {
            this.javaPath = javaPath;
            return builder();
        }

        public Builder password(String password) {
            this.password = password;
            return builder();
        }

        public Builder port(int port) {
            this.port = port;
            return builder();
        }

        public Builder user(String user) {
            this.user = user;
            return builder();
        }

        protected Builder builder() {
            return this;
        }

        protected Clw getInstance() {
            return new Clw(this);
        }
    }
}
