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
package com.ca.apm.systemtest.fld.hammond;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.SeverityLevel;

public class Configuration {

    private static Configuration instance;

    public static Configuration instance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    private String smartstor;
    private String traces;
    private String database;
    private String data;
    private String credential;
    private double agentScale;
    private String collector;
    private long duration;
    private String prefix;
    private String included;
    private String excluded;
    private boolean tracesonly;
    private boolean skipTracesXml;
    private Long from;
    private Long to;
    private Integer rotationScale;
    private Long rotationTime;
    private int synteticAgentCount;
    private int metricCount;
    private boolean rocksdb;
    private String dcu;
    private boolean playOnce;

    public ApplicationFeedback createFeedback(String applicationName) {
        ApplicationFeedback feedback = new ApplicationFeedback(applicationName);
        feedback.setLevel(SeverityLevel.VERBOSE);
        feedback.setShouldBuffer(false);
        return feedback;
    }

    public boolean parseReaderOptions(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help message");
        options.addOption("i", "input", true,
            "input folder with data, traces and database subfolders");
        options.addOption("ss", "smartstor", true, "input smartstor folder");
        options.addOption("tr", "traces", true, "transaction traces folder");
        options.addOption("db", "database", true, "postgres database folder");
        options.addOption("o", "output", true, "output data folder");
        options.addOption("f", "from", true, "read data from timestamp (millis).");
        options.addOption("t", "to", true, "read data to timestamp (millis)");
        options.addOption("tro", "tracesonly", true, "read only traces, parameter is transaction traces folder");
        options.addOption("noxml", "notracesxmls", false, "do not dump traces as xml (for tracesonly)");
        options.addOption("rdb", "rocksdb", false, "read metadata from RocksDB, not from metrics.metadata");
        options.addOption("dcu", "dcu", true, "read data from DCU archive");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            data = line.getOptionValue("output");
            
            if (line.hasOption("from")) {
                from = new Long(line.getOptionValue("from"));
            }
            if (line.hasOption("to")) {
                to = new Long(line.getOptionValue("to"));
            }

                       
            if (line.hasOption("noxml")) {
                skipTracesXml = true;
            }

            if (line.hasOption("dcu")) {
                Path dcaArchive = Paths.get(line.getOptionValue("dcu")).toAbsolutePath();
                dcu = dcaArchive.toString();
                data = Paths.get(dcaArchive.getParent().toString(), "hammond-data").toString();
                smartstor = Paths.get(dcaArchive.getParent().toString(), "em", "data").toString();
                traces = Paths.get(dcaArchive.getParent().toString(), "em", "traces").toString();

            } else if (line.hasOption("smartstor") && line.hasOption("traces")
                && line.hasOption("database") && line.hasOption("output")) {
                data = line.getOptionValue("output");
                smartstor = line.getOptionValue("smartstor");
                traces = line.getOptionValue("traces");
                database = line.getOptionValue("database");
            } else if (line.hasOption("input") && line.hasOption("output")) {
                String input = line.getOptionValue("input");
                smartstor = Paths.get(input, "data").toString();
                traces = Paths.get(input, "traces").toString();
                database = Paths.get(input, "database").toString();
            } else if (line.hasOption("tracesonly") && line.hasOption("output")) {
                data = line.getOptionValue("output");
                traces = line.getOptionValue("tracesonly");
                tracesonly = true;
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java.exe -jar hammond.jar SmartstorReader:", options);
                return false;
            }
            if (line.hasOption("rocksdb")) {
                rocksdb = true;
            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
        return true;
    }

    public boolean parseCSVOptions(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help message");
        options.addOption("ss", "smartstor", true, "input smartstor folder");
        options.addOption("t", "to", true, "output CSV files folder");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("smartstor") && line.hasOption("to")) {
                smartstor = line.getOptionValue("smartstor");
                data = line.getOptionValue("to");
                return true;
            }
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java.exe -jar hammond.jar Smartstor2CSV:", options);
            return false;
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
    }
    
    public boolean parsePlayerOptions(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help message");
        options.addOption("i", "input", true, "input data folder");
        options.addOption("c", "collector", true, "collector host name (without domain)");
        options.addOption("cr", "credential", true, "agent credential to use");
        options.addOption("s", "scale", true, "agent scaling ratio (double)");
        options.addOption("d", "duration", true, "playback duration (s)");
        options.addOption("p", "prefix", true, "prefix added to generated agent name");
        options.addOption("inc", "included", true,
            "comma-separated list of agent names to include (supports *)");
        options.addOption("exc", "excluded", true,
            "comma-separated list of agent names to exclude (supports *)");
        options.addOption("r", "rotation", true, "rotate metrics in agents");
        options.addOption("rt", "rotation-time", true, "time for metrics rotation in minutes");
        options.addOption("f", "from", true, "read data from timestamp (millis).");
        options.addOption("t", "to", true, "read data to timestamp (millis)");
        options.addOption("re", "replay", false, "replay captured data once");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("from")) {
                from = new Long(line.getOptionValue("from"));
            }
            if (line.hasOption("to")) {
                to = new Long(line.getOptionValue("to"));
            }
            
            // validate that block-size has been set
            if (line.hasOption("help") || !line.hasOption("input") && !line.hasOption("collector")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java.exe -jar hammond.jar SmartstorPlayer:", options);
                return false;
            } else {
                data = line.getOptionValue("input");
                collector = line.getOptionValue("collector");

                if (line.hasOption("cr")) {
                    credential = line.getOptionValue("credential");
                }

                if (line.hasOption("inc")) {
                    included = line.getOptionValue("included");
                }

                if (line.hasOption("exc")) {
                    excluded = line.getOptionValue("excluded");
                }

                if (line.hasOption("prefix")) {
                    prefix = line.getOptionValue("prefix");
                } else {
                    prefix = "";
                }

                if (line.hasOption("scale")) {
                    agentScale = Double.parseDouble(line.getOptionValue("scale"));
                } else {
                    agentScale = 1;
                }

                if (line.hasOption("duration")) {
                    duration = Long.parseLong(line.getOptionValue("duration")) * 1000;
                } else {
                    duration = Long.MAX_VALUE;
                }

                if (line.hasOption("rotation")) {
                    rotationScale = Integer.parseInt(line.getOptionValue("rotation"));
                } else {
                    rotationScale = 0;
                }
                
                if (line.hasOption( "rotation-time")) {
                    rotationTime =
                        TimeUnit.MINUTES.toMillis(Long.parseLong(line
                            .getOptionValue("rotation-time")));
                } else {
                    rotationTime = TimeUnit.MINUTES.toMillis(60L);
                }

                if (line.hasOption("replay")) {
                    playOnce = true;
                    prefix = "";
                    agentScale = 1;
                }
            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
        return true;
    }

    public boolean parseSynteticPlayerOptions(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help message");

        options.addOption("c", "collector", true, "collector host name (without domain)");
        options.addOption("cr", "credential", true, "agent credentials to use");
        options.addOption("a", "agents", true, "number of agents (default is 10)");
        options
            .addOption("m", "metric-count", true, "number of metrics per agent (default is 100)");
        options.addOption("d", "duration", true, "playback duration (s)");
        options.addOption("p", "prefix", true, "prefix added to generated agent name");
        options.addOption("r", "rotation", true, "rotate metrics in agents");
        options.addOption("rt", "rotation-time", true, "time for metrics rotation in minutes (default is 60 minutes)");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            // validate that block-size has been set
            if (line.hasOption("help") || !line.hasOption("input") && !line.hasOption("collector")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java.exe -jar hammond.jar SmartstorPlayer:", options);
                return false;
            } else {
                data = line.getOptionValue("input");
                collector = line.getOptionValue("collector");

                if (line.hasOption("a")) {
                    synteticAgentCount = Integer.parseInt(line.getOptionValue("a"));
                } else {
                    synteticAgentCount = 10;
                }

                if (line.hasOption("m")) {
                    metricCount = Integer.parseInt(line.getOptionValue("m"));
                } else {
                    metricCount = 100;
                }


                if (line.hasOption("prefix")) {
                    prefix = line.getOptionValue("prefix");
                } else {
                    prefix = "";
                }

                if (line.hasOption("prefix")) {
                    prefix = line.getOptionValue("prefix");
                } else {
                    prefix = "";
                }

                if (line.hasOption("duration")) {
                    duration = Long.parseLong(line.getOptionValue("duration")) * 1000;
                } else {
                    duration = Long.MAX_VALUE;
                }

                if (line.hasOption("rotation")) {
                    rotationScale = Integer.parseInt(line.getOptionValue("rotation"));
                } else {
                    rotationScale = 0;
                }

                if (line.hasOption("rotation-time")) {
                    rotationTime =
                        TimeUnit.MINUTES.toMillis(Long.parseLong(line
                            .getOptionValue("rotation-time")));
                } else {
                    rotationTime = TimeUnit.MINUTES.toMillis(60L);
                }


            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
        return true;
    }

    public String getSmartstorFolder() {
        return smartstor;
    }

    public String getTracesFolder() {
        return traces;
    }

    public String getDatabaseFolder() {
        return database;
    }

    public String getDataFolder() {
        return data;
    }

    public String getAgentCredential() {
        return credential;
    }

    public double getScaleRatio() {
        return agentScale;
    }

    public String getCollectorHost() {
        return collector;
    }

    public long getDuration() {
        return duration;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getExcluded() {
        return excluded;
    }

    public String getIncluded() {
        return included;
    }
    
    public boolean isTracesOnly() {
        return tracesonly;
    }

    public boolean isSkipTracesXml() {
        return skipTracesXml;
    }
 
    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public Integer getRotationScale() {
        return rotationScale;
    }

    public void setRotationScale(Integer rotationScale) {
        this.rotationScale = rotationScale;
    }

    public Long getRotationTime() {
        return rotationTime;
    }

    public void setRotationTime(Long rotationTime) {
        this.rotationTime = rotationTime;
    }

    public int getSynteticAgentCount() {
        return synteticAgentCount;
    }

    public int getMetricCount() {
        return metricCount;
    }

    public boolean isRocksdb() {
        return rocksdb;
    }

    public String getDcu() {
        return dcu;
    }

    public boolean isPlayOnce() {
        return playOnce;
    }
}
