package com.ca.apm.nextgen.tests;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.ADashboardWidgetTester;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardWidgetTesterFactory;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * @author bocto01
 */
public class DashboardTest extends BaseWebViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardTest.class);

    private static final String[] DASHBOARD_NAMES_Test3_Guest_Admin =
        new String[] {
                "An Intro to CA Introscope (TestSample in *SuperDomain*)",
                "APM Cloud Monitor - 1 - Site Overview (APM Cloud Monitor in *SuperDomain*)",
                "APM Cloud Monitor - 2 - Site Details (APM Cloud Monitor in *SuperDomain*)",
                "APM Cloud Monitor - 3 - Checkpoint Map (APM Cloud Monitor in *SuperDomain*)",
                "APM Cloud Monitor - 4 - Checkpoint Details (APM Cloud Monitor in *SuperDomain*)",
                "Data Provider Test Alert (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Data Provider Test Calculator (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Data Provider Test Metric Grouping (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Data Provider Test Simple Metric (testdomain2-module in TestDomain2)",
                "EM Capacity (Supportability in *SuperDomain*)",
                "GC Heap XML Typeview Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Line Shape Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Network Status Information (ADA Extension for APM in *SuperDomain*)",
                "Object Link Lens Dashboard (ObjectLinkLens_FunctionalTest in *SuperDomain*)",
                "Overview (TestSample in *SuperDomain*)",
                "Problem Analysis (TestSample in *SuperDomain*)",
                "td1-testdashboard (testdomain1-module in TestDomain1)",
                "td2-testdashboard (testdomain2-module in TestDomain2)",
                "Unsupported Typeviewer Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)",
                "Welcome to APM Dashboards (Default in *SuperDomain*)",
                "XML TypeViewer Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)"};

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450932)
    // DBW - Dashboard Verify Options in Overview and Cloud Monitor
    public void verifyOptionsTest() {
        try (WebViewUi ui = getWvUi()) {
            String user;

            user = "Admin";
            doInitialNavigation(user, ui);
            for (String dashboardName : DASHBOARD_NAMES_Test3_Guest_Admin) {
                LOGGER
                    .info("DashboardTest.verifyOptionsTest():: user = {}, dashboardName = {}", user,
                        dashboardName);
                DashboardConfiguration dashboardConfig =
                    getDashboardConfiguration(ui, dashboardName);
                for (WidgetConfiguration widgetConfig : dashboardConfig.getWidgets()) {
                    ADashboardWidgetTester widget =
                        DashboardWidgetTesterFactory.createWidget(widgetConfig, ui);
                    LOGGER.info("DashboardTest.verifyOptionsTest():: widget = {}", widget);
                    assertTrue(widget.verifyOptions());
                    LOGGER.info("DashboardTest.verifyOptionsTest():: widget = {} - [ OK ]", widget);
                }
            }
            ui.logout();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450933)
    // DBW - Dashboard Verify Text in Overview and Cloud Monitor
    public void verifyTextTest() {
        try (WebViewUi ui = getWvUi()) {
            String user;

            user = "Admin";
            doInitialNavigation(user, ui);
            for (String dashboardName : DASHBOARD_NAMES_Test3_Guest_Admin) {
                LOGGER.info("DashboardTest.verifyTextTest():: user = " + user
                    + ", dashboardName = " + dashboardName);
                DashboardConfiguration dashboardConfig =
                    getDashboardConfiguration(ui, dashboardName);
                for (WidgetConfiguration widgetConfig : dashboardConfig.getWidgets()) {
                    ADashboardWidgetTester widget =
                        DashboardWidgetTesterFactory.createWidget(widgetConfig, ui);
                    LOGGER.info("DashboardTest.verifyTextTest():: widget = " + widget);
                    assertTrue(widget.verifyText());
                    LOGGER
                        .info("DashboardTest.verifyTextTest():: widget = " + widget + " - [ OK ]");
                }
            }
            ui.logout();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450934)
    // DBW - Dashboard Verify Geometry in Overview and Cloud Monitor
    public void verifyGeometryTest() {
        try (WebViewUi ui = getWvUi()) {
            String user;

            user = "Admin";
            doInitialNavigation(user, ui);
            for (String dashboardName : DASHBOARD_NAMES_Test3_Guest_Admin) {
                LOGGER.info("DashboardTest.verifyGeometryTest():: user = {}, dashboardName = {}",
                    user, dashboardName);
                DashboardConfiguration dashboardConfig =
                    getDashboardConfiguration(ui, dashboardName);
                for (WidgetConfiguration widgetConfig : dashboardConfig.getWidgets()) {
                    ADashboardWidgetTester widget =
                        DashboardWidgetTesterFactory.createWidget(widgetConfig, ui);
                    LOGGER.info("DashboardTest.verifyGeometryTest():: widget = {}", widget);
                    assertTrue(widget.verifyGeometry());
                    LOGGER
                        .info("DashboardTest.verifyGeometryTest():: widget = {} - [ OK ]", widget);
                }
            }
            ui.logout();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450935)
    // DBW - Verify Object Links in Overview and Cloud Monitor
    public void verifyObjectLinksTest() {
        try (WebViewUi ui = getWvUi()) {
            String user;

            user = "Admin";
            doInitialNavigation(user, ui);
            for (String dashboardName : DASHBOARD_NAMES_Test3_Guest_Admin) {
                LOGGER.info("DashboardTest.verifyObjectLinksTest():: dashboardName = {}",
                    dashboardName);
                DashboardConfiguration dashboardConfig =
                    getDashboardConfiguration(ui, dashboardName);
                for (WidgetConfiguration widgetConfig : dashboardConfig.getWidgets()) {
                    ADashboardWidgetTester widget =
                        DashboardWidgetTesterFactory.createWidget(widgetConfig, ui);
                    LOGGER.info("DashboardTest.verifyObjectLinksTest():: widget = {}", widget);
                    assertTrue(widget.verifyObjectLinks());
                    LOGGER.info("DashboardTest.verifyObjectLinksTest():: widget = {} - [ OK ]",
                        widget);
                }
            }
            ui.logout();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class, executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID), owner = "bocto01", size = SizeType.MEDIUM, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450936)
    // DBW - Dashboard Veify Tooltips in Overview and Cloud Monitor
    public void verifyToolTipsTest() {
        try (WebViewUi ui = getWvUi()) {
            String user;

            user = "Admin";
            doInitialNavigation(user, ui);
            for (String dashboardName : DASHBOARD_NAMES_Test3_Guest_Admin) {
                LOGGER.info("DashboardTest.verifyToolTipsTest():: user = {}, dashboardName = {}",
                    user, dashboardName);
                DashboardConfiguration dashboardConfig =
                    getDashboardConfiguration(ui, dashboardName);
                for (WidgetConfiguration widgetConfig : dashboardConfig.getWidgets()) {
                    ui.enablePolling(false);
                    ADashboardWidgetTester widget =
                        DashboardWidgetTesterFactory.createWidget(widgetConfig, ui);
                    LOGGER.info("DashboardTest.verifyToolTipsTest():: widget = {}", widget);
                    assertTrue(widget.verifyToolTips());
                    LOGGER
                        .info("DashboardTest.verifyToolTipsTest():: widget = {} - [ OK ]", widget);
                    ui.enablePolling(true);
                }
            }
            ui.logout();

        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Test failed. Exception: {0}");
        }
    }

    private void doInitialNavigation(String user, WebViewUi ui) {
        boolean login = ui.login(getWvUrl(), user, "");
        assertTrue(login);
        sleep(5000L);
        ui.clickConsoleTab();
    }

    /**
     * Select dashboard name, get and parse dashboard XML definintion saved in
     * Javascript variable
     * 
     * @param dashboardName
     *        dashboard name to select from drop down
     * @return DashboardConfiguration
     */
    private DashboardConfiguration getDashboardConfiguration(WebViewUi ui, String dashboardName) {
        boolean dashboardSelected = selectDashboard(ui, dashboardName);
        assertTrue(dashboardSelected, "Dashboard selection failed: dashboard name ("
            + dashboardName + ")");
        DashboardConfiguration dashboardConfig = parseDashboardXML(ui, dashboardName);
        assertNotNull(dashboardConfig);
        return dashboardConfig;
    }

    /**
     * Select dashboard from drop down; wait for it to render
     * 
     * @param dashboardName
     *        dashboard name to select from drop down
     * @return status (true=success)
     */
    private boolean selectDashboard(WebViewUi ui, String dashboardName) {
        ui.selectConsoleDashboard(dashboardName);
        return true;
    }

    /**
     * Get and parse dashboard XML from javascript variable
     * 
     * @param dashboardName
     * @return DashboardConfiguration
     */
    private DashboardConfiguration parseDashboardXML(WebViewUi ui, String dashboardName) {
        String xml = (String) ui.getJavaScriptExecutor().executeScript("return DashboardXML;");
        assertNotNull(xml, "failed to get dashboard XML from javascript");
        try {
            DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document originalDocument =
                documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
            DashboardConfiguration dashboardConfig = new DashboardConfiguration(originalDocument);
            writeDocumentToFile(originalDocument);
            return dashboardConfig;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to parse dashboard XML for dashboard {1}. Exception: {0}",
                dashboardName);
        }
    }

    private void writeDocumentToFile(Document document) {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String xmlBuffer = sw.toString();
            String filename = "./DashboardXML_" + System.currentTimeMillis() + ".xml";
            File file = new File(filename);
            LOGGER.info("DashboardTest.writeDocumentToFile():: saving DashboardXML xml as {}",
                file.getAbsolutePath());
            BufferedWriter bw =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(xmlBuffer);
            bw.flush();
            bw.close();
        } catch (TransformerException | IOException e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to transform or write XML. Exception: {0}");
        }
    }

}
