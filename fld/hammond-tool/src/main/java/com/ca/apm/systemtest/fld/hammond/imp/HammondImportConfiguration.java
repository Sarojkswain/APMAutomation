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
package com.ca.apm.systemtest.fld.hammond.imp;

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

public class HammondImportConfiguration {

    private static HammondImportConfiguration instance;

    public static HammondImportConfiguration instance() {
        if (instance == null) {
            instance = new HammondImportConfiguration();
        }
        return instance;
    }

    private String output;
    private String smartstor;
    private String traces;
    private boolean compact;

    private Path filtersPath;

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
        options.addOption("c", "compact", false, "compact output");
        options.addOption("ss", "smartstor", true, "input smartstor folder");
        options.addOption("tr", "traces", true, "transaction traces folder");
        options.addOption("o", "output", true, "output data folder");
        options.addOption("mf", "filters", true, "file name with list of metrics filter regex on each line");

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("output") && line.hasOption("compact")) {
                output = line.getOptionValue("output");
                compact = true;
            } else if (line.hasOption("output") && line.hasOption("smartstor") && line.hasOption("traces")) {
                output = line.getOptionValue("output");
                smartstor = line.getOptionValue("smartstor");
                traces = line.getOptionValue("traces");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java.exe -cp hammond.jar com.ca.apm.systemtest.fld.hammond.HammondImport ", options);
                return false;
            }

            if (line.hasOption("filters")) {
                filtersPath = Paths.get(line.getOptionValue("filters"));
            }

        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }
        return true;
    }

    public String getOutput() {
        return output;
    }

    public String getSmartstor() {
        return smartstor;
    }

    public String getTraces() {
        return traces;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public Path getFiltersPath() {
        return filtersPath;
    }
}
