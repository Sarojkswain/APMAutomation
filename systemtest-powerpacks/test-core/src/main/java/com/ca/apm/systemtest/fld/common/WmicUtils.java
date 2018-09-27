package com.ca.apm.systemtest.fld.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author rsssa02
 */
public class WmicUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(WmicUtils.class);

    public static final String GET_PROC_ID_BY_CMD_LINE_AND_PROC_NAME_PATTERN_IN_CSV_FORMAT_TEMPLATE = "wmic process where "
            + "\"CommandLine like ''%{0}%'' and name like ''%{1}%''\" get ProcessId /FORMAT:\"%WINDIR%/System32/wbem/en-us/csv\"";

    private static final String PROCESS_ID_COLUMN = "ProcessId";


    public static Long getPid(String procName, String cmdLine) throws IOException {
        String cmd = MessageFormat.format(GET_PROC_ID_BY_CMD_LINE_AND_PROC_NAME_PATTERN_IN_CSV_FORMAT_TEMPLATE,
                cmdLine, procName);

        LOGGER.info("Getting process id for process with name matching pattern {} and command line arguments {}. Command: {}",
                procName, cmdLine, cmd);

        List<String> args = new ArrayList<>(10);
        args.add("wmic.exe");
        args.add("process");
        args.add("where");
        args.add(MessageFormat.format("\"CommandLine like ''%{0}%'' and name like ''%{1}%''\"", cmdLine, procName));
        args.add("get");
        args.add("ProcessId");
        args.add("/FORMAT:");
        args.add("\"" + System.getenv("WINDIR") + "/System32/wbem/en-us/csv\"");

        ProcessBuilder procBuilder = new ProcessBuilder();
        Process p = procBuilder.command(args).start();
        Long pid = null;
        BufferedReader reader = null;
        String line = null;
        boolean checkCaption = true;
        int searchAtInd = -1;

        try {
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                LOGGER.info("wmic's row: {}", line);

                if (line.isEmpty()) {
                    continue;
                }
                if (checkCaption && line.contains(PROCESS_ID_COLUMN)) {
                    checkCaption = false;
                    String[] columns = StringUtils.split(line, ',');
                    if (columns != null) {
                        for (int i = 0; i < columns.length; i++) {
                            String column = columns[i].trim();
                            if (PROCESS_ID_COLUMN.equals(column)) {
                                searchAtInd = i;
                                break;
                            }
                        }
                    }
                    continue;
                }

                String[] columns = StringUtils.split(line, ',');
                LOGGER.info("Next wmic's row columns split by comma: {}", Arrays.toString(columns));
                if (columns == null) {
                    String msg = "wmic returned unexpected result: no columns found!";
                    LOGGER.error(msg);
                    throw new UnknownError(msg);
                }
                if (searchAtInd > columns.length - 1) {
                    String msg = "wmic's result row has less columns than expected: found " +
                            columns.length + ", expected at least " + (searchAtInd + 1);
                    LOGGER.error(msg);
                    throw new UnknownError(msg);
                }

                String pidColumn = columns[searchAtInd];
                if (pid == null) {
                    pid = Long.parseLong(pidColumn);
                    LOGGER.info("wmic found pid: {}", pid);
                } else {
                    LOGGER.warn("wmic found more than one process matching input criteria! Skipping pid {}",
                            pidColumn);
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return pid;
    }
}
