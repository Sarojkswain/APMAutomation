package com.ca.apm.systemtest.fld.plugin.powerpack.reporting;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Load Orchestrator agent plugin to build report for PowerPack performance test results.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@PluginAnnotationComponent(pluginType = PowerPackReportPlugin.PLUGIN)
public class PowerPackReportPluginImpl extends AbstractPluginImpl implements PowerPackReportPlugin {

    public static final int EXPECTED_JTL_COLUMNS = 12;
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerPackReportPluginImpl.class);
    private static final SimpleDateFormat DATE_FORMAT_FOR_FILE_RENAMING = new SimpleDateFormat(
        "yyyy_MM_dd__HH_mm");

    public static final String OLD_AGENT_RESULT_FOLDER_SUFFIX = "_oldAgent";
    public static final String NEW_AGENT_RESULT_FOLDER_SUFFIX = "_newAgent";
    public static final String NO_AGENT_RESULT_FOLDER_SUFFIX = "_noAgent";
    public static final String OLD_AGENT_PLUS_POWER_PACK_RESULT_FOLDER_SUFFIX = "_oldAgentPlusPP";
    public static final String NEW_AGENT_PLUS_POWER_PACK_RESULT_FOLDER_SUFFIX = "_newAgentPlusPP";

    public static final String JTL_EXTENSION = ".jtl";
    public static final String S0U_COLUMN = "S0U";
    public static final String S1U_COLUMN = "S1U";
    public static final String EU_COLUMN = "EU";
    public static final String OU_COLUMN = "OU";

    //CPU (TypePerf)
    public static final String CPU_NO_AGENT_SHEET_NAME = "cpu_no_agent";
    public static final String CPU_OLD_AGENT_SHEET_NAME = "cpu_old_agent";
    public static final String CPU_OLD_AGENT_POWER_PACK_SHEET_NAME = "cpu_old_agent_power_pack";
    public static final String CPU_NEW_AGENT_SHEET_NAME = "cpu_new_agent";
    public static final String CPU_NEW_AGENT_POWER_PACK_SHEET_NAME = "cpu_new_agent_power_pack";

    //Jstat
    public static final String JSTAT_NO_AGENT_SHEET_NAME = "jstat_no_agent";
    public static final String JSTAT_OLD_AGENT_SHEET_NAME = "jstat_old_agent";
    public static final String JSTAT_OLD_AGENT_POWER_PACK_SHEET_NAME = "jstat_old_agent_power_pack";
    public static final String JSTAT_NEW_AGENT_SHEET_NAME = "jstat_new_agent";
    public static final String JSTAT_NEW_AGENT_POWER_PACK_SHEET_NAME = "jstat_new_agent_power_pack";
        
    //Jmeter
    public static final String JMETER_NO_AGENT_SHEET_NAME = "rt_no_agent";
    public static final String JMETER_OLD_AGENT_SHEET_NAME = "rt_old_agent";
    public static final String JMETER_OLD_AGENT_POWER_PACK_SHEET_NAME = "rt_old_agent_power_pack";
    public static final String JMETER_NEW_AGENT_SHEET_NAME = "rt_new_agent";
    public static final String JMETER_NEW_AGENT_POWER_PACK_SHEET_NAME = "rt_new_agent_power_pack";
    
    //JMX
    public static final String JMX_NO_AGENT_SHEET_NAME = "memory_no_agent";
    public static final String JMX_OLD_AGENT_SHEET_NAME = "memory_old_agent";
    public static final String JMX_OLD_AGENT_POWER_PACK_SHEET_NAME = "memory_old_agent_power_pack";
    public static final String JMX_NEW_AGENT_SHEET_NAME = "memory_new_agent";
    public static final String JMX_NEW_AGENT_POWER_PACK_SHEET_NAME = "memory_new_agent_power_pack";
    
    @Override
    @ExposeMethod(description = "Combines performance metrics into one single XSL report file")
    public void generateReport(ResultParseConfig config) throws IOException {
        String groupedResultsFolder = config.getResultsFolder();
        String noAgentResultFolder =
            config.isIncludeNoAgentResults() ? findFolderWithSuffix(groupedResultsFolder,
                NO_AGENT_RESULT_FOLDER_SUFFIX, true) : null;
        String oldAgentResultFolder =
            config.isIncludeOldAgentResults() ? findFolderWithSuffix(groupedResultsFolder,
                OLD_AGENT_RESULT_FOLDER_SUFFIX, true) : null;
        String oldAgentPlusPowerPackResultFolder =
            config.isIncludeOldAgentWithPowerPackResults() ? findFolderWithSuffix(
                groupedResultsFolder, OLD_AGENT_PLUS_POWER_PACK_RESULT_FOLDER_SUFFIX, true) : null;
        String newAgentResultFolder =
            config.isIncludeNewAgentResults() ? findFolderWithSuffix(groupedResultsFolder,
                NEW_AGENT_RESULT_FOLDER_SUFFIX, false) : null;
        String newAgentPlusPowerPackResultFolder =
            config.isIncludeNewAgentWithPowerPackResults() ? findFolderWithSuffix(
                groupedResultsFolder, NEW_AGENT_PLUS_POWER_PACK_RESULT_FOLDER_SUFFIX, false) : null;

        String reportFileName =
            config.getFinalResultReportFileName() != null
                ? config.getFinalResultReportFileName()
                : PowerPackConstants.DEFAULT_POWER_PACK_FINAL_RESULT_REPORT_FILE_NAME;

        File template =
            config.getReportTemplateFilePath() != null
                ? new File(config.getReportTemplateFilePath())
                : getTemplate(config.getReportTemplateFileUrl());
        File reportFile = createReportFile(template, reportFileName, groupedResultsFolder);

        if (config.isBuildTypePerfReport()) {
            info("Processing TypePerf results");
            String typePerfFileName = config.getTypePerfResultFileName();
            collectTypePerfResults(noAgentResultFolder, oldAgentResultFolder,
                oldAgentPlusPowerPackResultFolder, newAgentResultFolder,
                newAgentPlusPowerPackResultFolder, typePerfFileName, reportFile,
                config.getMonitoredProcessId());
        } else {
            info("Ignoring any TypePerf results");
        }

        if (config.isBuildJmxReport()) {
            info("Processing JMX results");
            String jmxFileName = config.getJmxResultFileName();
            collectJmxResults(noAgentResultFolder, oldAgentResultFolder,
                oldAgentPlusPowerPackResultFolder, newAgentResultFolder,
                newAgentPlusPowerPackResultFolder, jmxFileName, reportFile);
        } else {
            info("Ignoring any JMX results");
        }

        if (config.isBuildJmeterReport()) {
            info("Processing Jmeter results");
            collectJmeterResults(noAgentResultFolder, oldAgentResultFolder,
                oldAgentPlusPowerPackResultFolder, newAgentResultFolder,
                newAgentPlusPowerPackResultFolder, reportFile);
        } else {
            info("Ignoring any Jmeter results");
        }

        if (config.isBuildJstatReport()) {
            info("Processing Jstat results");
            collectJstatResults(noAgentResultFolder, oldAgentResultFolder,
                oldAgentPlusPowerPackResultFolder, newAgentResultFolder,
                newAgentPlusPowerPackResultFolder, config.getJstatResultFileName(), reportFile);
        } else {
            info("Ignoring any Jstat results");
        }
    }

    protected void collectJstatResults(String noAgentResultFolder, String oldAgentResultFolder,
        String oldAgentPlusPowerPackResultFolder, String newAgentResultFolder,
        String newAgentPlusPowerPackResultFolder, String jstatFileName, File reportFile)
        throws IOException {

        if (noAgentResultFolder != null) {
            File noAgentJstatFile = new File(noAgentResultFolder, jstatFileName);
            copyJstatResultsToReport(noAgentJstatFile, reportFile, reportFile, 
                JSTAT_NO_AGENT_SHEET_NAME);
        }

        if (oldAgentResultFolder != null) {
            File oldAgentJstatFile = new File(oldAgentResultFolder, jstatFileName);
            copyJstatResultsToReport(oldAgentJstatFile, reportFile, reportFile, 
                JSTAT_OLD_AGENT_SHEET_NAME);
        }

        if (oldAgentPlusPowerPackResultFolder != null) {
            File oldAgentWithPPJstatFile =
                new File(oldAgentPlusPowerPackResultFolder, jstatFileName);
            copyJstatResultsToReport(oldAgentWithPPJstatFile, reportFile, reportFile, 
                JSTAT_OLD_AGENT_POWER_PACK_SHEET_NAME);
        }

        if (newAgentResultFolder != null) {
            File newAgentJstatFile = new File(newAgentResultFolder, jstatFileName);
            copyJstatResultsToReport(newAgentJstatFile, reportFile, reportFile, 
                JSTAT_NEW_AGENT_SHEET_NAME);
        }

        if (newAgentPlusPowerPackResultFolder != null) {
            File newAgentWithPPJstatFile =
                new File(newAgentPlusPowerPackResultFolder, jstatFileName);
            copyJstatResultsToReport(newAgentWithPPJstatFile, reportFile, reportFile, 
                JSTAT_NEW_AGENT_POWER_PACK_SHEET_NAME);
        }
    }

    protected void collectJmeterResults(String noAgentResultFolder, String oldAgentResultFolder,
        String oldAgentPlusPowerPackResultFolder, String newAgentResultFolder,
        String newAgentPlusPowerPackResultFolder, File reportFile) throws IOException {

        if (noAgentResultFolder != null) {
            collectJmeterResultsForCase(noAgentResultFolder, "no-agent", reportFile, reportFile, 
                JMETER_NO_AGENT_SHEET_NAME);
        }

        if (oldAgentResultFolder != null) {
            collectJmeterResultsForCase(oldAgentResultFolder, "old-agent", reportFile, reportFile,
                JMETER_OLD_AGENT_SHEET_NAME);
        }

        if (oldAgentPlusPowerPackResultFolder != null) {
            collectJmeterResultsForCase(oldAgentPlusPowerPackResultFolder,
                "old-agent-with-power-pack", reportFile, reportFile, 
                JMETER_OLD_AGENT_POWER_PACK_SHEET_NAME);
        }

        if (newAgentResultFolder != null) {
            collectJmeterResultsForCase(newAgentResultFolder, "new-agent", reportFile, reportFile,
                JMETER_NEW_AGENT_SHEET_NAME);
        }

        if (newAgentPlusPowerPackResultFolder != null) {
            collectJmeterResultsForCase(newAgentPlusPowerPackResultFolder,
                "new-agent-with-power-pack", reportFile, reportFile, 
                JMETER_NEW_AGENT_POWER_PACK_SHEET_NAME);
        }
    }

    protected void collectJmxResults(String noAgentResultFolder, String oldAgentResultFolder,
        String oldAgentPlusPowerPackResultFolder, String newAgentResultFolder,
        String newAgentPlusPowerPackResultFolder, String jmxFileName, File reportFile)
        throws IOException {

        if (noAgentResultFolder != null) {
            File noAgentJmxResFile = new File(noAgentResultFolder, jmxFileName);
            copyResultsToReport(noAgentJmxResFile, reportFile, reportFile, true, 
                JMX_NO_AGENT_SHEET_NAME);
        }

        if (oldAgentResultFolder != null) {
            File oldAgentJmxResFile = new File(oldAgentResultFolder, jmxFileName);
            copyResultsToReport(oldAgentJmxResFile, reportFile, reportFile, true, 
                JMX_OLD_AGENT_SHEET_NAME);
        }

        if (oldAgentPlusPowerPackResultFolder != null) {
            File oldAgentWithPPJmxResFile =
                new File(oldAgentPlusPowerPackResultFolder, jmxFileName);
            copyResultsToReport(oldAgentWithPPJmxResFile, reportFile, reportFile, true, 
                JMX_OLD_AGENT_POWER_PACK_SHEET_NAME);
        }

        if (newAgentResultFolder != null) {
            File newAgentJmxResFile = new File(newAgentResultFolder, jmxFileName);
            copyResultsToReport(newAgentJmxResFile, reportFile, reportFile, true, 
                JMX_NEW_AGENT_SHEET_NAME);
        }

        if (newAgentPlusPowerPackResultFolder != null) {
            File newAgentWithPPJmxResFile =
                new File(newAgentPlusPowerPackResultFolder, jmxFileName);
            copyResultsToReport(newAgentWithPPJmxResFile, reportFile, reportFile, true, 
                JMX_NEW_AGENT_POWER_PACK_SHEET_NAME);
        }
    }

    protected void collectTypePerfResults(String noAgentResultFolder, String oldAgentResultFolder,
        String oldAgentPlusPowerPackResultFolder, String newAgentResultFolder,
        String newAgentPlusPowerPackResultFolder, String typePerfFileName, File reportFile, Long pid)
        throws IOException {

        if (noAgentResultFolder != null) {
            File noAgentTypePerfFile = new File(noAgentResultFolder, typePerfFileName);
            copyResultsToReport(noAgentTypePerfFile, reportFile, reportFile, true, CPU_NO_AGENT_SHEET_NAME);
        }

        if (oldAgentResultFolder != null) {
            File oldAgentTypePerfFile = new File(oldAgentResultFolder, typePerfFileName);
            copyResultsToReport(oldAgentTypePerfFile, reportFile, reportFile, true, CPU_OLD_AGENT_SHEET_NAME);
        }

        if (oldAgentPlusPowerPackResultFolder != null) {
            File oldAgentWithPPTypePerfFile =
                new File(oldAgentPlusPowerPackResultFolder, typePerfFileName);
            copyResultsToReport(oldAgentWithPPTypePerfFile, reportFile, reportFile, true, CPU_OLD_AGENT_POWER_PACK_SHEET_NAME);
        }

        if (newAgentResultFolder != null) {
            File newAgentTypePerfFile = new File(newAgentResultFolder, typePerfFileName);
            copyResultsToReport(newAgentTypePerfFile, reportFile, reportFile, true, CPU_NEW_AGENT_SHEET_NAME);
        }

        if (newAgentPlusPowerPackResultFolder != null) {
            File newAgentWithPPTypePerfFile =
                new File(newAgentPlusPowerPackResultFolder, typePerfFileName);
            copyResultsToReport(newAgentWithPPTypePerfFile, reportFile, reportFile, true, CPU_NEW_AGENT_POWER_PACK_SHEET_NAME);
        }
    }

    protected void copyResultsToReport(File resultsFile, File templateFile, File reportFile,
        boolean expectHeader, String sheetName) throws IOException {
        try (LineNumberReader resultReader = new LineNumberReader(new FileReader(resultsFile));
            FileInputStream templateIS = new FileInputStream(templateFile);
            HSSFWorkbook workbook = new HSSFWorkbook(templateIS);
            FileOutputStream reportOS = new FileOutputStream(reportFile);) {
            templateIS.close();
            HSSFSheet sheet = workbook.getSheet(sheetName);
            String resultMetricsRow = null;
            int rowNum = 1;
            boolean headerPassed = false;
            while ((resultMetricsRow = resultReader.readLine()) != null) {
                String[] metricsArray = StringUtils.split(resultMetricsRow, ',');
                if (metricsArray == null || metricsArray.length == 0) {
                    info("Skipping empty metric row: {0}", resultMetricsRow);
                    continue;
                }
                if (expectHeader && !headerPassed) {
                    info("Skipping header: {0}", resultMetricsRow);
                    headerPassed = true;
                    continue;
                }
                HSSFRow row = sheet.createRow(rowNum++);
                for (int i = 0; i < metricsArray.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String metricVal = StringUtils.remove(metricsArray[i], '"').trim();
                    if (!StringUtils.isBlank(metricVal)) {
                        if (NumberUtils.isDigits(metricVal)) {
                            cell.setCellValue(Long.parseLong(metricVal));
                        } else if (NumberUtils.isNumber(metricVal)) {
                            cell.setCellValue(Double.parseDouble(metricVal));
                        } else {
                            cell.setCellValue(metricVal);
                        }
                    }
                }
            }

            workbook.write(reportOS);
        } catch (IOException e) {
            error(MessageFormat.format(
                "Failed to copy result metrics from ''{0}'' to sheet named ''{1}'' "
                    + "of the report file at ''{2}'' derived from template ''{3}''", resultsFile,
                sheetName, reportFile, templateFile), e);
            throw e;
        }
    }

    protected File createReportFile(String templateFileUrl, String reportFileName, String resultDir)
        throws IOException {
        File template = getTemplate(templateFileUrl);
        return createReportFile(template, reportFileName, resultDir);
    }

    protected File createReportFile(File template, String reportFileName, String resultDir)
        throws IOException {
        File reportFile = new File(resultDir, reportFileName);

        if (reportFile.exists()) {
            /*
             * Destination file exists. Let's rename it first appending its creation time
             * to the end of the file name.
             */
            BasicFileAttributes fileAttrs =
                Files.readAttributes(reportFile.toPath(), BasicFileAttributes.class);
            String formattedTimestamp =
                DATE_FORMAT_FOR_FILE_RENAMING.format(new Date(fileAttrs.lastModifiedTime()
                    .toMillis()));
            File renameFile = new File(resultDir, formattedTimestamp + "_" + reportFileName);
            if (!renameFile.exists()) {
                info(
                    "Destination report file ''{0}'' exists. Renaming it to ''{1}'' using its last modified time",
                    reportFile, renameFile);
                org.apache.commons.io.FileUtils.moveFile(reportFile, renameFile);
            } else {
                info(
                    "Destination report file ''{0}'' exists. Can''t rename it to ''{1}'' as it also exists. Trying to use index.",
                    reportFile, renameFile);
                /*
                 * Use the closest index to rename the file.
                 */
                boolean renameNameFound = false;
                for (int i = 1; i < 1000; i++) {
                    renameFile = new File(resultDir, reportFileName + "." + String.valueOf(i));
                    if (!renameFile.exists()) {
                        renameNameFound = true;
                        break;
                    }
                }
                if (!renameNameFound) {
                    String msg = "Failed to rename existing result file!";
                    error(msg);
                    throw new ResultsParserException(msg);
                }

                info("Renaming existing destination report file ''{0}'' into ''{1}''", reportFile,
                    renameFile);

                org.apache.commons.io.FileUtils.moveFile(reportFile, renameFile);
            }
        }

        org.apache.commons.io.FileUtils.copyFile(template, reportFile);
        return reportFile;
    }

    protected File getTemplate(String templateFileUrl) throws IOException {
        File template =
            new File(PowerPackConstants.AGENT_DOWNLOAD_DIR_NAME,
                PowerPackConstants.DEFAULT_POWER_PACK_REPORT_TEMPLATE_NAME);
        if (template.exists()) {
            return template;
        }

        template =
            FileUtils.downloadResource(templateFileUrl,
                PowerPackConstants.DEFAULT_POWER_PACK_REPORT_TEMPLATE_NAME_NO_EXT,
                PowerPackConstants.DEFAULT_POWER_PACK_REPORT_TEMPLATE_EXT, new File(
                    PowerPackConstants.AGENT_DOWNLOAD_DIR_NAME));

        return template;
    }

    protected String findFolderWithSuffix(String groupedResultFolder, final String suffix,
        boolean older) throws ResultsParserException, NullPointerException {
        File file = new File(groupedResultFolder);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                File file = new File(current, name);
                return file.isDirectory() ? name.endsWith(suffix) : false;
            }
        });
        if (directories == null || directories.length == 0) {
            String message =
                MessageFormat
                    .format(
                        "Did not find any matching result folder in grouped result folder ''{0}'' for suffix ''{1}''",
                        groupedResultFolder, suffix);
            warn(message);
            return null;
        }
        if (directories.length == 1) {
            String dir = directories[0];
            info(
                "Found a single folder matching suffix ''{0}'' in grouped result folder ''{1}'': ''{2}''",
                suffix, groupedResultFolder, dir);
            return Paths.get(groupedResultFolder, dir).toString();

        }

        String version = null;
        String resultDir = null;
        for (String dir : directories) {
            String[] tokens = StringUtils.split(dir, '_');
            if (tokens == null || tokens.length < 2) {
                String message =
                    MessageFormat
                        .format(
                            "Failed to parse version in directory name ''{0}'' residing in grouped folder ''{1}''",
                            dir, groupedResultFolder);
                error(message);
                throw new ResultsParserException(message);
            }
            String curVersion = tokens[tokens.length - 2];
            if (version == null) {
                version = curVersion;
                resultDir = dir;
                continue;
            }

            int compareResult = version.compareTo(curVersion);
            if ((older && compareResult > 0) || (!older && compareResult < 0)) {
                resultDir = dir;
            }

            String resultFullDirPath = Paths.get(groupedResultFolder, resultDir).toString();
            info("Found result dir: {0}", resultFullDirPath);
            return resultFullDirPath;
        }

        String message =
            MessageFormat
                .format(
                    "Did not find matching result folder in grouped result folder ''{0}'' for suffix ''{1}'' for {2} agent version",
                    groupedResultFolder, suffix, older ? "older" : "newer");
        error(message);
        throw new ResultsParserException(message);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private void copyJstatResultsToReport(File resultsFile, File templateFile, File reportFile,
        String sheetName) throws IOException {
        try (LineNumberReader resultReader = new LineNumberReader(new FileReader(resultsFile));
            FileInputStream templateIS = new FileInputStream(templateFile);
            HSSFWorkbook workbook = new HSSFWorkbook(templateIS);
            FileOutputStream reportOS = new FileOutputStream(reportFile);) {
            templateIS.close();
            HSSFSheet sheet = workbook.getSheet(sheetName);
            String resultMetricsRow = null;
            int rowNum = 1;
            boolean headerPassed = false;
            Map<String, Integer> columnNameToIndexMappings = new HashMap<String, Integer>();
            while ((resultMetricsRow = resultReader.readLine()) != null) {
                resultMetricsRow = resultMetricsRow.trim();
                if ("".equals(resultMetricsRow)) {
                    info("Skipping empty metric row: {0}", resultMetricsRow);
                    continue;
                }
                String[] metricsArray = StringUtils.split(resultMetricsRow, " \t");
                if (metricsArray == null || metricsArray.length == 0) {
                    info("Skipping empty metric row: {0}", resultMetricsRow);
                    continue;
                }
                if (!headerPassed) {
                    info("Found Jstat header: {0}", resultMetricsRow);
                    headerPassed = true;
                    for (int i = 0; i < metricsArray.length; i++) {
                        columnNameToIndexMappings.put(metricsArray[i], i);
                    }
                    continue;
                }

                HSSFRow row = sheet.createRow(rowNum++);
                int i = 0;
                for (; i < metricsArray.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String metricVal = metricsArray[i];
                    if (!StringUtils.isBlank(metricVal)) {
                        if (NumberUtils.isDigits(metricVal)) {
                            cell.setCellValue(Long.parseLong(metricVal));
                        } else if (NumberUtils.isNumber(metricVal)) {
                            cell.setCellValue(Double.parseDouble(metricVal));
                        } else {
                            cell.setCellValue(metricVal);
                        }
                    }
                }
                double gcHeap =
                    (Double.parseDouble(metricsArray[columnNameToIndexMappings.get(S0U_COLUMN)])
                        + Double
                            .parseDouble(metricsArray[columnNameToIndexMappings.get(S1U_COLUMN)])
                        + Double
                            .parseDouble(metricsArray[columnNameToIndexMappings.get(EU_COLUMN)]) + Double
                        .parseDouble(metricsArray[columnNameToIndexMappings.get(OU_COLUMN)])) / 1024D;

                HSSFCell gcHeapCell = row.createCell(15);
                gcHeapCell.setCellValue(gcHeap);
            }

            workbook.write(reportOS);
        } catch (IOException e) {
            error(MessageFormat.format(
                "Failed to copy result metrics from ''{0}'' to sheet named ''{1}'' "
                    + "of the report file at ''{2}'' derived from template ''{3}''", resultsFile,
                sheetName, reportFile, templateFile), e);
            throw e;
        }
    }

    private void collectJmeterResultsForCase(String caseResultFolder, String caseName,
        File templateFile, File reportFile, String sheetName) throws IOException {
        File caseDir = new File(caseResultFolder);
        String[] metricsArray;
        String[] caseJtls = caseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return name.endsWith(JTL_EXTENSION);
            }
        });

        if (caseJtls == null || caseJtls.length == 0) {
            String message =
                MessageFormat.format(
                    "Did not find any JTL files for ''{0}'' result folder ''{1}''", caseName,
                    caseResultFolder);
            error(message);
            throw new ResultsParserException(message);

        }
        info("Found the following JTL files for ''{0}'' case: {1}", caseName,
            Arrays.asList(caseJtls));
        if (caseJtls.length > 1) {
            warn("''{0}'' case result directory has more than one JTL file!", caseName);
        }

        File caseJtlFile = new File(caseDir, caseJtls[0]);

        Map<String, JmeterSampleData> sampleDataMap = new TreeMap<String, JmeterSampleData>();
        String resultMetricsRow = null;
        try (LineNumberReader resultReader = new LineNumberReader(new FileReader(caseJtlFile))) {
            boolean foundHeader = false;
            while ((resultMetricsRow = resultReader.readLine()) != null) {
                metricsArray = StringUtils.splitPreserveAllTokens(resultMetricsRow, ',');
                if (metricsArray == null || metricsArray.length == 0) {
                    info("Skipping empty metric row");
                    continue;
                }
                if (!foundHeader) {
                    // skip header
                    foundHeader = true;
                    continue;
                }
                if (metricsArray.length < EXPECTED_JTL_COLUMNS) {
                    warn(
                        "Skipping suspicious jMeter log line number : {0}, expected number of "
                            + "elements delimited by comma is {2}, but contains only {1}",
                        resultReader.getLineNumber(), metricsArray.length, EXPECTED_JTL_COLUMNS);
                    continue;
                }
                String timeStampStr = metricsArray[0];
                String responseStatusStr = metricsArray[3];
                String responseTimeStr = metricsArray[11];
                Long responseTime = Long.parseLong(responseTimeStr);
                JmeterSampleData sampleData = sampleDataMap.get(timeStampStr);
                if (sampleData == null) {
                    sampleData =
                        new JmeterSampleData(timeStampStr, responseStatusStr, responseTime,
                            responseTime, responseTime, 1L);
                    sampleDataMap.put(timeStampStr, sampleData);
                } else {
                    sampleData.setResponseStatus(responseStatusStr);
                    sampleData.setMaxResponseTimeMillis(responseTime);
                    sampleData.setMinResponseTimeMillis(responseTime);
                    sampleData.addResponseTimeMillis(responseTime);
                    sampleData.incrementRequestCountBy1();
                }
            }
        } catch (IOException e) {
            error(MessageFormat.format("Failed to parse JTL result metrics from ''{0}''",
                caseJtlFile), e);
            throw e;
        }

        try (FileInputStream templateIS = new FileInputStream(templateFile);
            HSSFWorkbook workbook = new HSSFWorkbook(templateIS);
            FileOutputStream reportOS = new FileOutputStream(reportFile);) {
            templateIS.close();
            HSSFSheet sheet = workbook.getSheet(sheetName);
            int rowNum = 1;
            for (Entry<String, JmeterSampleData> sampleDataEntry : sampleDataMap.entrySet()) {
                String timestamp = sampleDataEntry.getKey();
                JmeterSampleData sampleData = sampleDataEntry.getValue();

                HSSFRow row = sheet.createRow(rowNum++);
                int i = 0;
                row.createCell(i++).setCellValue(timestamp);
                row.createCell(i++).setCellValue(sampleData.getResponseStatus());
                row.createCell(i++).setCellValue(sampleData.getMinResponseTimeMillis());
                row.createCell(i++).setCellValue(sampleData.getMaxResponseTimeMillis());
                row.createCell(i++).setCellValue(sampleData.getSumResponseTimeMillis());
                row.createCell(i++).setCellValue(sampleData.getRequestCount());
                row.createCell(i++).setCellValue(
                    sampleData.getSumResponseTimeMillis().doubleValue()
                        / sampleData.getRequestCount().doubleValue());
            }
            workbook.write(reportOS);
        } catch (IOException e) {
            error(MessageFormat.format(
                "Failed to write Jmeter result metrics from ''{0}'' to sheet named ''{1}'' "
                    + "of the report file at ''{2}'' derived from template ''{3}''", caseJtlFile,
                sheetName, reportFile, templateFile), e);
            throw e;
        }
    }

}
