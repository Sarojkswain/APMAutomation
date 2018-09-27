package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * CSV export test.
 *
 * @author haiva01
 */
public class ChartCsvExportTest extends BaseWebViewTest {
    private static final CSVFormat WV_CSV_EXPORT_FORMAT = CSVFormat.newFormat(',')
        .withQuote('"')
        .withAllowMissingColumnNames(false)
        .withIgnoreSurroundingSpaces()
        .withHeader();
    private static final Logger log = LoggerFactory.getLogger(ChartCsvExportTest.class);
    private static final String[] KNOWN_HEADERS = {
        "Domain", "Host", "Process", "AgentName", "Resource", "MetricName", "Period",
        "Actual Start Timestamp", "Value Count", "Integer Value", "Integer Min", "Integer Max"};
    private static final String EXPORT_CSV_BUTTON_XPATH
        = "//*[@id='webview-investigator-linechart-chart-export-csv']"
        + "//*[text()[contains(.,'Export to CSV')]]";

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        // This test *must* be executed on the SELENIUM_GRID_MACHINE_ID machine so that it can
        // access CSV export file to evaluate its correctness.
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.MEDIUM,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450506)
    public void basicChartCsvExportTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        log.debug("Selenium Grid hub at {}", hubHost);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", "Admin", "");

            // get Home tab chart and display Export CSV menu item

            ui.clickHomeTab();
            final WebElement slowestFrontEndAvgRespTimeChart = ui.getHomeTabChart();

            // get menu button

            WebElement menuButton = ui.getExportMenuButton(slowestFrontEndAvgRespTimeChart);
            WebElement exportButton = ui.getExportButton(menuButton);
            ui.clickExportButton(exportButton);
            TimeUnit.SECONDS.sleep(10);

            // Check the downloaded export.csv file

            File exportCsv = getExportCsvFile();
            validateExportCsv(exportCsv);

            // get Console tab and export chart from there as well

            ui.clickConsoleTab();
            WebElement dashboardInput = ui
                .waitForWebElement(By.id("dashboard-selection-combobox-input"));
            WebElement lineChartContainer = ui.waitForWebElement(
                By.xpath("//*[@id='dashboard-scroll-container']"
                    + "//*[@id='webview-investigator-linechart-container']"));
            menuButton = ui.getExportMenuButton(lineChartContainer);
            exportButton = ui.getExportButton(menuButton);
            ui.clickExportButton(exportButton);
            TimeUnit.SECONDS.sleep(10);

            // Check the downloaded export.csv file

            exportCsv = getExportCsvFile();
            validateExportCsv(exportCsv);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    private void validateExportCsv(File exportCsv) throws IOException {
        CSVParser exportParser = new CSVParser(
            new InputStreamReader(new BOMInputStream(FileUtils.openInputStream(exportCsv)),
                StandardCharsets.UTF_8), WV_CSV_EXPORT_FORMAT);
        final Set<String> headers = new TreeSet<>(exportParser.getHeaderMap().keySet());
        log.debug("headers in CSV export: {}", headers);
        final Set<String> knownHeaders = new TreeSet<>(Arrays.asList(KNOWN_HEADERS));
        log.debug("        known headers: {}", knownHeaders);
        assertTrue(headers.containsAll(knownHeaders));

        long rowCount = 0;
        for (CSVRecord csvRecord : IteratorUtils.asIterable(exportParser.iterator())) {
            assertEquals(csvRecord.size(), headers.size());
            ++rowCount;
        }
        assertTrue(rowCount > 0, "zero row count found");
    }

    private File getExportCsvFile() {
        Collection<File> files = FileUtils.listFiles(new File("C:\\SW"),
            new WildcardFileFilter("export*.csv", IOCase.INSENSITIVE), null);
        assertTrue(!files.isEmpty());
        List<Pair<File, Long>> filesAndTimes = new ArrayList<>(files.size());
        for (File file : files) {
            filesAndTimes.add(new ImmutablePair<>(file, file.lastModified()));
        }
        Collections.sort(filesAndTimes, new Comparator<Pair<File, Long>>() {
            @Override
            public int compare(Pair<File, Long> o1, Pair<File, Long> o2) {
                return (int) (o2.getValue() - o1.getValue());
            }
        });
        log.debug("files and times: {}", filesAndTimes);
        File exportCsv = filesAndTimes.get(0).getKey();
        log.debug("Most recent export.csv file: {}", filesAndTimes.get(0));
        return exportCsv;
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.MEDIUM,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450580)
    public void exportCsvButtonBehaviourTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        log.debug("Selenium Grid hub at {}", hubHost);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", "Admin", "");

            // get Home tab chart and display Export CSV menu item

            ui.clickHomeTab();
            final WebElement slowestFrontEndAvgRespTimeChart = ui.getHomeTabChart();

            // get menu button

            final long MENU_TEST_DELAY = 1;

            WebElement menuButton = ui.getExportMenuButton(slowestFrontEndAvgRespTimeChart);
            ui.getActions().click().perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));
            ui.getActions().click().perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            try {
                ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));
                assertFalse(true, "element should not be present here");
            } catch (NoSuchElementException ex) {
                ErrorReport
                    .logExceptionFmt(log, ex, "This exception is expected and not an error: {0}");
                assertTrue(true);
            }
            ui.getActions().click().perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));
            ui.getActions().click().perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            try {
                ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));
                assertFalse(true, "element should not be present here");
            } catch (NoSuchElementException ex) {
                ErrorReport
                    .logExceptionFmt(log, ex, "This exception is expected and not an error: {0}");
                assertTrue(true);
            }
            ui.getActions().click().perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));

            // Now move outside the chart and re-check that the menu is still there.

            WebElement theLogo = ui.getWebElement(By.className("apmLogo"));
            ui.getActions().moveToElement(theLogo).perform();
            TimeUnit.SECONDS.sleep(MENU_TEST_DELAY);
            ui.getWebElement(By.xpath(EXPORT_CSV_BUTTON_XPATH));
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
