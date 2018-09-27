package com.ca.apm.systemtest.sizingguidetest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author rsssa02
 */
public class GenerateExcelReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateExcelReport.class);

    // public static final String S0U_COLUMN = "S0U";
    // public static final String S1U_COLUMN = "S1U";
    // public static final String EU_COLUMN = "EU";
    // public static final String OU_COLUMN = "OU";


    public void copyResults(File resultsFile, File templateFile, File reportFile, String sheetName,
        boolean expectHeader) throws Exception {
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
                    LOGGER.info("Skipping empty metric row: " + resultMetricsRow);
                    continue;
                }
                if (expectHeader && !headerPassed) {
                    LOGGER.info("Skipping header: " + resultMetricsRow);
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
                        } else if (NumberUtils.isCreatable/* isNumber */(metricVal)) {
                            cell.setCellValue(Double.parseDouble(metricVal));
                        } else {
                            cell.setCellValue(metricVal);
                        }
                    }
                }
            }

            workbook.write(reportOS);
            workbook.close();
        } catch (IOException e) {
            LOGGER.error(MessageFormat.format(
                "Failed to copy result metrics from ''{0}'' to sheet named ''{1}'' "
                    + "of the report file at ''{2}'' derived from template ''{3}''", resultsFile,
                sheetName, reportFile, templateFile), e);
            throw e;
        }
    }

    // public void copyJstatResults(File resultsFile, File templateFile, File reportFile,
    // String sheetName) throws IOException {
    // try (LineNumberReader resultReader = new LineNumberReader(new FileReader(resultsFile));
    // FileInputStream templateIS = new FileInputStream(templateFile);
    // HSSFWorkbook workbook = new HSSFWorkbook(templateIS);
    // FileOutputStream reportOS = new FileOutputStream(reportFile);) {
    // templateIS.close();
    // HSSFSheet sheet = workbook.getSheet(sheetName);
    // String resultMetricsRow = null;
    // int rowNum = 1;
    // boolean headerPassed = false;
    // Map<String, Integer> columnNameToIndexMappings = new HashMap<String, Integer>();
    // while ((resultMetricsRow = resultReader.readLine()) != null) {
    // resultMetricsRow = resultMetricsRow.trim();
    // if ("".equals(resultMetricsRow)) {
    // LOGGER.info("Skipping empty metric row: " + resultMetricsRow);
    // continue;
    // }
    // String[] metricsArray = StringUtils.split(resultMetricsRow, " \t");
    // if (metricsArray == null || metricsArray.length == 0) {
    // LOGGER.info("Skipping empty metric row: " + resultMetricsRow);
    // continue;
    // }
    // if (!headerPassed) {
    // LOGGER.info("Found Jstat header: " + resultMetricsRow);
    // headerPassed = true;
    // for (int i = 0; i < metricsArray.length; i++) {
    // columnNameToIndexMappings.put(metricsArray[i], i);
    // // LOGGER.info("checkpoint: " + metricsArray[i]);
    // }
    // continue;
    // }
    //
    // HSSFRow row = sheet.createRow(rowNum++);
    // int i = 0;
    // for (; i < metricsArray.length; i++) {
    // HSSFCell cell = row.createCell(i);
    // String metricVal = metricsArray[i];
    // if (!StringUtils.isBlank(metricVal)) {
    // if (NumberUtils.isDigits(metricVal)) {
    // cell.setCellValue(Long.parseLong(metricVal));
    // } else if (NumberUtils.isNumber(metricVal)) {
    // cell.setCellValue(Double.parseDouble(metricVal));
    // } else {
    // cell.setCellValue(metricVal);
    // }
    // }
    // }
    // double gcHeap =
    // (Double.parseDouble(metricsArray[columnNameToIndexMappings.get(S0U_COLUMN)])
    // + Double
    // .parseDouble(metricsArray[columnNameToIndexMappings.get(S1U_COLUMN)])
    // + Double
    // .parseDouble(metricsArray[columnNameToIndexMappings.get(EU_COLUMN)]) + Double
    // .parseDouble(metricsArray[columnNameToIndexMappings.get(OU_COLUMN)])) / 1024D;
    //
    // HSSFCell gcHeapCell = row.createCell(17);
    // gcHeapCell.setCellValue(gcHeap);
    // }
    //
    // workbook.write(reportOS);
    // workbook.close();
    // } catch (IOException e) {
    // LOGGER.error(MessageFormat.format(
    // "Failed to copy result metrics from ''{0}'' to sheet named ''{1}'' "
    // + "of the report file at ''{2}'' derived from template ''{3}''", resultsFile,
    // sheetName, reportFile, templateFile), e);
    // throw e;
    // }
    // }

}
