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

package com.ca.apm.powerpack.sysview.tools.sysvutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.TabularData;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;

public class Cli {
    private static final Logger log = LoggerFactory.getLogger(Cli.class);

    /**
     * Entry point.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) throws IOException {
        Parms parms = new Parms();
        final JCommander jc = new JCommander(parms, args); // populates the parms variable
        
        if (parms.help) {
            jc.usage();
            System.exit(0);
        }
        
        // Prepare row filters
        final Map<String, Pattern> includeRows = new HashMap<String, Pattern>();
        for (String rawFilter : parms.includeRows) {
            final int split = rawFilter.indexOf('=');
            if (split == -1) {
                log.warn("Ignoring malformed filter: " + rawFilter);
                continue;
            }
            
            final String field = rawFilter.substring(0, split);
            final String pattern = rawFilter.substring(split + 1);
            
            includeRows.put(field, Pattern.compile(pattern));
        }

        final Map<String, Pattern> excludeRows = new HashMap<String, Pattern>();
        for (String rawFilter : parms.excludeRows) {
            final int split = rawFilter.indexOf('=');
            if (split == -1) {
                log.warn("Ignoring malformed filter: " + rawFilter);
                continue;
            }
            
            final String field = rawFilter.substring(0, split);
            final String pattern = rawFilter.substring(split + 1);
            
            excludeRows.put(field, Pattern.compile(pattern));
        }
        
        // Prepare column filters
        final List<Pattern> includeColumns = new ArrayList<Pattern>();
        for (String rawFilter : parms.includeColumns) {
            includeColumns.add(Pattern.compile(rawFilter));
        }
        final List<Pattern> excludeColumns = new ArrayList<Pattern>();
        for (String rawFilter : parms.excludeColumns) {
            excludeColumns.add(Pattern.compile(rawFilter));
        }
        Predicate<String> columnFilter = new Predicate<String>(){
            @Override
            public boolean test(String tested) {
                boolean match = includeColumns.isEmpty();

                if (includeColumns != null && !includeColumns.isEmpty()) {
                    for (Pattern filter : includeColumns) {
                        if (filter.matcher(tested).matches()) {
                            match = true;
                            break;
                        }
                    }
                }

                if (match && !excludeColumns.isEmpty()) {
                    for (Pattern filter : excludeColumns) {
                        if (filter.matcher(tested).matches()) {
                            match = false;
                            break;
                        }
                    }
                }
                
                return match;
            }
        };

        // Sysview Command
        if (parms.command.isEmpty()) {
            log.error("No sysview command specified");
            System.exit(1);
        }
        final String sysviewCommand = parms.command.stream().collect(Collectors.joining(" "));

        try (Sysview sysv = new Sysview(parms.loadlib)) {
            // Execution
            log.debug("Running Sysview command: {}", sysviewCommand);
            final ExecResult result = sysv.execute(sysviewCommand);
            if (!result.getRc().isOk()) {
                log.error("Command failed with RC={} - unmodified output follows", result.getRc());

                for (String line : result.getOutput()) {
                    log.error("{}", line);
                }
                System.exit(2);
            } else {
                log.debug("Command succeeded with RC={}", result.getRc());
            }
            
            if (parms.raw) {
                for (String line : result.getOutput()) {
                    System.out.println(line);
                }
                System.exit(0);
            }

            final TabularData tb = result.getTabularData();

            // Row filtering 
            List<Map<String, String>> rows = tb.getAllRowsMatching(includeRows, excludeRows);
            
            // Figure out column widths
            final Collection<String> columns = tb.getColumns().stream()
                .filter(columnFilter).collect(Collectors.toList());
            final Map<String, Integer> widths = new HashMap<String, Integer>();
            for (String column : columns) {
                int width = column.length();
                for (Map<String, String> row : rows) {
                    width = Math.max(width, row.get(column).length());
                }
                widths.put(column, width + 2);
            }
            
            // Column filtering and output
            if (!parms.skipHeader) {
                System.out.print(columns.stream()
                    .map(s -> parms.align ? StringUtils.center(s, widths.get(s)) : s)
                    .collect(Collectors.joining(parms.separator)));
                System.out.println();
            }

            for (Map<String, String> row : rows) {
                boolean first = true;
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    final String column = entry.getKey();
                    final String value = entry.getValue();
                    
                    if (columnFilter.test(column)) {
                        if (!first) {
                            System.out.print(parms.separator);
                        } else {
                            first = false;
                        }
                        System.out.print(parms.align
                            ? StringUtils.center(value, widths.get(column))
                            : value);
                    }
                }
                System.out.println();
            }
        }
    }
}

class Parms {
    @Parameter(names = { "--help" }, help = true)
    public boolean help = false;
    
    @Parameter(names = { "-r", "--raw" }, help = true, description = "Raw output (no filtering)")
    public boolean raw = false;

    @Parameter(names = { "-nh", "--no-header" }, help = true, description = "Header in output")
    public boolean skipHeader = false;

    @Parameter(names = { "-a", "--align" }, description = "Align output")
    public boolean align = false;
    
    @Parameter(names = { "-s", "--separator" }, description = "Separator")
    public String separator = ",";

    @Parameter(names = { "-l", "--loadlib" }, description = "Sysview loadlib")
    public String loadlib = "";

    @Parameter(names = { "-ci", "--column-include" }, description = "Inclusion column filters")
    public List<String> includeColumns = new ArrayList<String>();
    
    @Parameter(names = { "-ce", "--column-exclude" }, description = "Exclusion column filters")
    public List<String> excludeColumns = new ArrayList<String>();

    // TODO: support other types of filtering (e.g. numeric comparisons)
    @Parameter(names = { "-fi", "--filter-include" }, description = "Inclusion row filters")
    public List<String> includeRows = new ArrayList<String>();

    @Parameter(names = { "-fe", "--filter-excludes" }, description = "Exclusion row filters")
    public List<String> excludeRows = new ArrayList<String>();

    @Parameter(description = "Sysview command")
    public List<String> command = new ArrayList<String>();
}