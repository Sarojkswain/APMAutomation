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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.wily.util.feedback.ApplicationFeedback;
import com.wily.util.feedback.SeverityLevel;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HammondPlayerConfiguration {

    private static HammondPlayerConfiguration instance;

    public static HammondPlayerConfiguration instance() {
        if (instance == null) {
            instance = new HammondPlayerConfiguration();
        }
        return instance;
    }

    private String data;
    private String collector;
    private String prefix;
    private String group;
    private Long from;
    private Long to;
    private Double agentScale;
    private String included;
    private String excluded;
    private Path filtersPath;
    private String credential;


    public ApplicationFeedback createFeedback(String applicationName) {
        ApplicationFeedback feedback = new ApplicationFeedback(applicationName);
        feedback.setLevel(SeverityLevel.VERBOSE);
        feedback.setShouldBuffer(false);
        return feedback;
    }

    public boolean parseOptions(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "help message");
        options.addOption("d", "data", true, "hammond data folder");
        options.addOption("g", "group", true, "specify what agent subset should be played (3rd player of 5 workers - 3/5");
        options.addOption("f", "from", true, "read data from timestamp (millis).");
        options.addOption("t", "to", true, "read data to timestamp (millis)");
        options.addOption("c", "collector", true, "collector host name (without domain)");
        options.addOption("p", "prefix", true, "prefix agent name when you wish to replay data more than once");
        options.addOption("s", "scale", true, "agent scaling ratio (double)");
        options.addOption("inc", "included", true,
                "comma-separated list of agent names to include (supports *)");
        options.addOption("exc", "excluded", true,
                "comma-separated list of agent names to exclude (supports *)");
        options.addOption("cr", "credential", true, "agent credential to use");
        options.addOption("mf", "filters", true, "file name with list of metrics filter regex on each line");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("data") && line.hasOption("collector")) {
                data = line.getOptionValue("data");
                collector = line.getOptionValue("collector");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java.exe -cp hammond.jar com.ca.apm.systemtest.fld.hammond.HammondPlayer ", options);
                return false;
            }

            if (line.hasOption("prefix")) {
                prefix = line.getOptionValue("prefix");
            }

            if (line.hasOption("group")) {
                group = line.getOptionValue("group");
            }

            if (line.hasOption("from")) {
                from = new Long(line.getOptionValue("from"));
            }
            if (line.hasOption("to")) {
                to = new Long(line.getOptionValue("to"));
            }

            if (line.hasOption("scale")) {
                agentScale = Double.parseDouble(line.getOptionValue("scale"));
            }

            if (line.hasOption("included")) {
                included = line.getOptionValue("included");
            }

            if (line.hasOption("excluded")) {
                excluded = line.getOptionValue("excluded");
            }

            if (line.hasOption("filters")) {
                filtersPath = Paths.get(line.getOptionValue("filters"));
            }

            if (line.hasOption("credential")) {
                credential = line.getOptionValue("credential");
            }

        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
        return true;
    }

    public String getData() {
        return data;
    }

    public String getCollectorHost() {
        return collector;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getGroup() {
        return group;
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public Double getAgentScale() {
        return agentScale;
    }

    public String getExcluded() {
        return excluded;
    }

    public String getIncluded() {
        return included;
    }

    public Path getFiltersPath() {
        return filtersPath;
    }

    public String getAgentCredential() {
        return credential;
    }
}
