package com.ca.apm.systemtest.fld.common;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

/**
 * Windows scheduled tasks utilities for creating, running, deleting, etc.
 */
public class WindowsScheduledTaskUtils {
    private static Logger log = LoggerFactory.getLogger(WindowsScheduledTaskUtils.class);

    /**
     * CSV format output by SCHTASKS.
     * It intentionally does not contain withQuote('"'). See also comment in {@link
     * WindowsScheduledTaskUtils.query(String)}.
     */
    private static CSVFormat schTasksCSVFormat = CSVFormat.newFormat(',')
        .withEscape('\\').withHeader();

    /**
     * Start scheduled task given by name.
     *
     * @param schTaskName scheduled task name to start
     */
    public static void start(String schTaskName) {
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command("schtasks", "/Run", "/TN", schTaskName);
        StartedProcess sp = ProcessUtils2.startProcess(pe);
        int exitCode = ProcessUtils.waitForProcess(sp.getProcess(), 2, TimeUnit.MINUTES, true);
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "SCHTASKS ended with exit code {0}", exitCode);
        }
    }


    /**
     * Stop scheduled task given by name.
     *
     * @param schTaskName scheduled task name to stop
     */
    public static void stop(String schTaskName) {
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command("schtasks", "/End", "/TN", schTaskName);
        StartedProcess sp = ProcessUtils2.startProcess(pe);
        int exitCode = ProcessUtils.waitForProcess(sp.getProcess(), 2, TimeUnit.MINUTES, true);
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "SCHTASKS ended with exit code {0}", exitCode);
        }
    }


    /**
     * Run SCHTASKS /Query /V /FO CSV and capter its output.
     * @param schTaskName scheduled task to query information about
     * @return captured output of SCHTASKS as InputStream
     */
    public static InputStream runSchtasksQuery(String schTaskName) {
        InputStream csvStream;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(1024)) {
            ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
                .command("schtasks", "/Query", "/V", "/FO", "CSV", "/TN", schTaskName)
                .redirectOutputAlsoTo(os);
            StartedProcess sp = ProcessUtils2.startProcess(pe);
            int exitCode = ProcessUtils.waitForProcess(sp.getProcess(), 2, TimeUnit.MINUTES, true);
            if (exitCode != 0) {
                throw ErrorUtils.logErrorAndReturnException(log,
                    "SCHTASKS ended with exit code {0}", exitCode);
            }
            csvStream = new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error during SCHTASKS /Query execution. Exception: {0}", e);
        }
        return csvStream;
    }


    /**
     * Remove given quote character wrapping given string.
     * @param str string
     * @param quote quote to be removed
     * @return unquoted string
     */
    private static String unquoteString(String str, char quote) {
        if (str == null
            || str.isEmpty()) {
            return str;
        }

        String result = str;
        if (str.length() >= 2
            && str.charAt(0) == quote
            && str.charAt(str.length() - 1) == quote) {
            result = str.substring(1, str.length() - 1);
        }

        return result;
    }


    /**
     * Remove outer quotes from all of map's keys and values.
     * @param map map to transform
     * @param quote quote character
     * @return unquoted map
     */
    private static Map<String, String> unquoteMap(Map<String, String> map, char quote) {
        SortedMap<String, String> unqotedMap = new TreeMap<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            final String origKey = e.getKey();
            final String origValue = e.getValue();
            String newKey = unquoteString(origKey, quote);
            String newValue = unquoteString(origValue, quote);
            unqotedMap.put(newKey, newValue);
        }
        return unqotedMap;
    }


    /**
     * Query scheduled task states and attributes.
     * @param schTaskName scheduled task name
     * @return map of all fields and values as returned by SCHTASKS for /Query /V /FO CSV
     */
    public static Map<String, String> query(String schTaskName) {
        InputStream csvStream = runSchtasksQuery(schTaskName);
        String fileEncoding = System.getProperty("file.encoding");
        Charset sysCharset = Charset.forName(fileEncoding);
        try (Reader csvReader = new InputStreamReader(csvStream, sysCharset);
             CSVParser csvParser = new CSVParser(csvReader, schTasksCSVFormat)) {
            CSVRecord record = csvParser.iterator().next();
            Map<String, String> map = record.toMap();
            // We receive all fields quoted with double quotes (") from SCHTASKS.
            // Previously, we specified withQuote('"') in the schTasksCSVFormat.
            // Unfortunately, that does not work if the command that is being executed
            // does contain double quotes as well. SCHTASKS is being stupid and does not
            // escape the double quotes inside the command. To work around this we
            // parse the output as if it was not quoted at all and we remove
            // the outer quotes from all keys and values here instead.
            map = unquoteMap(map, '"');
            return map;
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error parsing SCHTASKS /Query output. Exception: {0}", e);
        }
    }


    public static String queryStatus(String schTaskName) {
        Map<String, String> schTaskAttributes = query(schTaskName);
        assert schTaskAttributes.get("TaskName") != null
            && schTaskAttributes.get("TaskName").equals(schTaskName);
        String status = schTaskAttributes.get("Status");
        return status;
    }


    /**
     * This function creates Windows scheduled task from XML template
     * using SCHTASKS tool.
     *
     * @param scheduledTaskXmlTemplate Freemarker template for scheduled task XML specification
     * @param command                  executable file
     * @param arguments                process command line arguments
     * @param dir                      working directory
     * @param taskUser                 user account to run the scheduled task
     * @param taskUserPassword         user account password
     * @param schTaskNameBase          scheduled task folder and name to which GUID will be
     *                                 attached for uniqueness
     * @return scheduled task name
     */
    public static String createScheduledTask(String scheduledTaskXmlTemplate,
        String command, String arguments, File dir, String taskUser, String taskUserPassword,
        String schTaskNameBase) {
        return createScheduledTask(scheduledTaskXmlTemplate, command, arguments, dir, taskUser,
            taskUserPassword, schTaskNameBase, null);
    }


    /**
     * This function creates Windows scheduled task from XML template
     * using SCHTASKS tool.
     *
     * @param scheduledTaskXmlTemplate Freemarker template for scheduled task XML specification
     * @param command                  executable file
     * @param arguments                process command line arguments
     * @param dir                      working directory
     * @param taskUser                 user account to run the scheduled task
     * @param taskUserPassword         user account password
     * @param schTaskNameBase          scheduled task folder and name to which GUID will be
     *                                 attached for uniqueness
     * @param additionalProperties     additional properties
     * @return scheduled task name
     */
    public static String createScheduledTask(String scheduledTaskXmlTemplate,
        String command, String arguments, File dir, String taskUser, String taskUserPassword,
        String schTaskNameBase, Map<String, String> additionalProperties) {
        // Prepare scheduled task XML specification.

        Map<String, Object> props = new HashMap<>(20);
        if (additionalProperties != null) {
            props.putAll(additionalProperties);
        }
        props.put("command", command);
        props.put("arguments", arguments);
        props.put("taskUser", taskUser);
        props.put("dir", dir.getAbsolutePath());

        Configuration freemarkerConfig = FreemarkerUtils.getConfig();
        Template template = FreemarkerUtils.getTemplate(freemarkerConfig, scheduledTaskXmlTemplate,
            "UTF-16LE");
        File schTaskXmlFile = ACFileUtils.generateTemporaryFile("schtask", ".xml", dir);
        // SCHTASKS is very particular about the XML file encoding.
        // It needs to be x-UTF-16LE-BOM and nothing else.
        FreemarkerUtils.processTemplate(schTaskXmlFile, template, props, "x-UTF-16LE-BOM");

        // Create new scheduled task using the prepared XML file.

        String schTaskName = schTaskNameBase + "-" + UUID.randomUUID().toString();
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command("SCHTASKS", "/Create", "/TN", schTaskName, "/XML",
                schTaskXmlFile.getAbsolutePath(), "/RU", taskUser, "/RP", taskUserPassword);
        StartedProcess sp = ProcessUtils2.startProcess(pe);
        int exitCode = ProcessUtils.waitForProcess(sp.getProcess(), 1, TimeUnit.MINUTES, true);
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "SCHTASKS ended with exit code {0}", exitCode);
        }

        return schTaskName;
    }
}
