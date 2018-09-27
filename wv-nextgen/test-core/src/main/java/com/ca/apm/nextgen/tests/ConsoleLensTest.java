package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.TableUi;
import com.ca.apm.nextgen.tests.helpers.WaitForLensButtonTooltip;
import com.ca.apm.nextgen.tests.helpers.WaitForStringTableLineTooltip;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author haiva01
 */
public class ConsoleLensTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(ConsoleLensTest.class);
    private static final String SELECTION_ROW_AGENT_NAME = "TD2";
    private static final List<String> SELECTION_ROW = Arrays
        .asList("", "TestDomain2", "Custom Metric Host (Virtual)",
            "Custom Metric Process (Virtual)", SELECTION_ROW_AGENT_NAME, "", "");
    private static final String OBJECT_LINK_LENS_DASHBOARD
        = "Object Link Lens Dashboard (ObjectLinkLens_FunctionalTest in *SuperDomain*)";
    private static final LinkedHashSet<List<String>> SELECTED_AGENTS_SET = new LinkedHashSet<>(
        Arrays.asList(
            Arrays.asList("", "*SuperDomain*", "laivi02-745", "WebLogic", "WebLogic Agent",
                "", ""),
            Arrays.asList("", "*SuperDomain*", "localhost", "WurlitzerDomain",
                "WurlitzerAgent_1", "", "")));
    private static final String REMOVE_LENS_BUTTON_ID = "webview-consolelens-dialog-clear-button";
    private static final String APPLY_LENS_BUTTON_ID = "webview-consolelens-dialog-apply-button";
    private static final String CANCEL_LENS_DIALOG_BUTTON_ID
        = "webview-consolelens-dialog-cancel-button";
    private static final String WEBVIEW_USER = "Admin";
    private static final String CONSOLE_LENS_TABLE_GRID_ID = "webview-consolelens-table-grid";

    private static void clickFilterDialogButton(WebViewUi ui, SearchContext searchContext,
        By buttonSelector) {
        WebElement button = ui.getWebElement(searchContext, buttonSelector);
        ui.clickButton(button, By.xpath(".//div[text()]"));
    }

    private static void verifyFilterDialogButtonState(WebViewUi ui, SearchContext searchContext,
        By buttonSelector, boolean expectedState) {
        try {
            WebElement button = ui.getWebElement(searchContext, buttonSelector);
            WebElement buttonText = ui.getWebElement(button, By.xpath(".//div[text()]"));
            // Verify button is enabled/disabled. Enabled buttons have "pointer" cursor. Disabled
            // buttons have "default" cursor.
            assertEquals(buttonText.getCssValue("cursor").equals("pointer"), expectedState);
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    private static void clickFilterDialogCloseButton(WebViewUi ui, SearchContext searchContext,
        By buttonSelector) {
        ui.clickButton(searchContext, buttonSelector);
    }

    /**
     * Find the row that we have previously selected and check it is still selected. Also check
     * that no other row is selected.
     *
     * @param tableUi
     * @param selectionRow
     */
    private static void verifyFilterSelection(TableUi tableUi, final List<String> selectionRow) {
        boolean foundSelected = false;
        final WebViewUi ui = tableUi.getUi();
        try {
            for (int row = 0, rowCount = tableUi.rowCount(); row != rowCount; ++row) {
                List<String> rowValues = tableUi.getRowValues(row);
                WebElement selectionCellElement = tableUi.getCellElement(row, 0);
                WebElement selectionBoxElement = ui.getWebElement(selectionCellElement,
                    By.xpath(".//input[@type='checkbox']"));
                if (rowValues.equals(selectionRow)) {
                    ui.scrollIntoView(selectionBoxElement);
                    assertTrue(selectionBoxElement.isSelected(), "This agent should be selected.");
                    foundSelected = true;
                } else {
                    assertFalse(selectionBoxElement.isSelected(),
                        "This agent should not be selected.");
                }
            }
            assertTrue(foundSelected, "Previously selected row was not found in the table at all");
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    private static void verifyFilterSelection(TableUi tableUi,
        final Set<List<String>> selectedRows) {
        try {
            log.debug("Agents expected to be selected: {}", selectedRows);

            final WebViewUi ui = tableUi.getUi();
            for (int row = 0, rowCount = tableUi.rowCount(); row != rowCount; ++row) {
                List<String> rowValues = tableUi.getRowValues(row);
                WebElement selectionCellElement = tableUi.getCellElement(row, 0);
                WebElement selectionBoxElement = ui.getWebElement(selectionCellElement,
                    By.xpath(".//input[@type='checkbox']"));
                ui.scrollIntoView(selectionBoxElement);
                //log.debug("row: {}", rowValues);
                if (selectionBoxElement.isSelected()) {
                    assertTrue(selectedRows.contains(rowValues),
                        "This agent is selected in filter but it is not expected to be selected.");
                } else {
                    assertFalse(selectedRows.contains(rowValues),
                        "This agent is not selected int filter but it is expected to be selected.");
                }
            }
        } catch (Throwable e) {
            tableUi.getUi().takeScreenShot();
            throw e;
        }
    }

    /**
     * Verify that none of the row is selected.
     *
     * @param tableUi
     */
    private static void verifyFilterSelectionIsEmpty(TableUi tableUi) {
        final WebViewUi ui = tableUi.getUi();
        try {
            for (int row = 0, rowCount = tableUi.rowCount(); row != rowCount; ++row) {
                WebElement selectionCellElement = tableUi.getCellElement(row, 0);
                WebElement selectionBoxElement = ui.getWebElement(selectionCellElement,
                    By.xpath(".//input[@type='checkbox']"));
                assertFalse(selectionBoxElement.isSelected(), "Filter row should not be selected.");
            }
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    private static void clickSelectionBoxOfTableRow(TableUi tableUi, List<String> selectionRow) {
        final WebViewUi ui = tableUi.getUi();
        ui.takeScreenShot();

        try {
            // Find specific row.

            WebElement selectionBoxElement = findTableRowCheckbox(tableUi, selectionRow);
            assertNotNull(selectionBoxElement);

            // Check selection box of the row.

            ui.scrollIntoView(selectionBoxElement);
            ui.moveAndClick(selectionBoxElement);
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }

        ui.takeScreenShot();
    }

    private static WebElement findTableRowCheckbox(TableUi tableUi, List<String> selectionRow) {
        final WebViewUi ui = tableUi.getUi();
        WebElement selectionBoxElement = null;

        for (int row = 0, rowCount = tableUi.rowCount(); row != rowCount; ++row) {
            List<String> rowValues = tableUi.getRowValues(row);
            if (rowValues.equals(selectionRow)) {
                WebElement selectionCellElement = tableUi.getCellElement(row, 0);
                selectionBoxElement = ui.getWebElement(selectionCellElement,
                    By.xpath(".//input[@type='checkbox']"));
                break;
            }
        }

        return selectionBoxElement;
    }

    private static WebElement produceFilterTooltip(WebViewUi ui) {
        WebElement lensButton = ui.getWebElement(
            By.id("webview-consolelens-dialog-launch-button"));
        ui.getActions()
            .moveToElement(lensButton)
            .perform();
        return ui.waitFor(new WaitForLensButtonTooltip(ui, lensButton), 10, TimeUnit.SECONDS);
    }

    private static WebElement produceStringTableTooltip(WebViewUi ui, SearchContext searchContext,
        By lineSelector) {
        WebElement cell = ui.getWebElement(searchContext, lineSelector);
        ui.scrollIntoView(cell);
        ui.getActions()
            .moveToElement(cell)
            .perform();
        return ui.waitFor(new WaitForStringTableLineTooltip(ui, cell), 10, TimeUnit.SECONDS);
    }

    private static String getAgentsFromtTooltipText(String toolTipText) {
        log.debug("Tooltip test: >{}<", toolTipText);
        toolTipText = StringUtils.removeStartIgnoreCase(toolTipText,
            "console agent filter lens applied:");
        toolTipText = StringUtils.trim(toolTipText);
        return toolTipText;
    }

    private static List<String> getStringsTableLines(WebViewUi ui, By tableSelector) {
        WebElement stringsTable = ui.getWebElement(tableSelector);
        List<WebElement> tableElements = ui.getWebElements(stringsTable, By.xpath("./div"));
        List<String> lines = new ArrayList<>(tableElements.size());
        for (WebElement tableElement : tableElements) {
            lines.add(tableElement.getText());
        }
        return lines;
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450675)
    public void consoleLensTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard("Overview (TestSample in *SuperDomain*)");

            // Wait for the dashboard to appear.

            WebElement anchor = ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

            // Open Console agent filter lens dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            TableUi tableUi
                = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find specific row and click its selection box.

            clickSelectionBoxOfTableRow(tableUi, SELECTION_ROW);

            // Click OK button.

            clickFilterDialogButton(ui, filterDialog,
                By.cssSelector("div[id=webview-consolelens-dialog-apply-button] div"));

            // Wait for selection dialog to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(filterDialog));

            // Re-open Console lens filter dialog.

            filterDialog = ui.openConsoleLensFilterDialog();

            // Re-parse table.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find the row that we have previously selected and check it is still selected. Also
            // check that no other row is selected.

            verifyFilterSelection(tableUi, SELECTION_ROW);

            // Click upper right corner close button.

            clickFilterDialogCloseButton(ui, filterDialog,
                By.id("webview-consolelens-dialog-close-button"));

            // Select different dashboard.

            ui.selectConsoleDashboard(
                "XML TypeViewer Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)");

            // Wait for the dashboard to appear.

            ui.waitForWebElement(By.xpath("//div[@title='Harvest, Smartstor, and GC Durations']"));

            // Re-open Console lens filter dialog.

            filterDialog = ui.openConsoleLensFilterDialog();

            // Re-parse table.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find the row that we have previously selected and check it is still selected. Also
            // check that no other row is selected.

            verifyFilterSelection(tableUi, SELECTION_ROW);

            // Click upper right corner close button.

            clickFilterDialogCloseButton(ui, filterDialog,
                By.id("webview-consolelens-dialog-close-button"));

            // Click Home tab.

            ui.clickHomeTab();

            // Click back to Console tab.

            ui.clickConsoleTab();

            // Re-open Console lens filter dialog.

            filterDialog = ui.openConsoleLensFilterDialog();

            // Re-parse table.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find the row that we have previously selected and check it is still selected. Also
            // check that no other row is selected.

            verifyFilterSelection(tableUi, SELECTION_ROW);

            // Click upper right corner close button.

            clickFilterDialogCloseButton(ui, filterDialog,
                By.id("webview-consolelens-dialog-close-button"));
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450666)
    public void consoleLensFilterButtonsDefaultsTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard("Overview (TestSample in *SuperDomain*)");

            // Wait for the dashboard to appear.

            ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

            // Open Console agent filter lens dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Verify buttons state.

            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(CANCEL_LENS_DIALOG_BUTTON_ID), true);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(APPLY_LENS_BUTTON_ID), false);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(REMOVE_LENS_BUTTON_ID), false);

        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450667)
    public void consoleLensFilterButtonsAfterSelectionTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard("Overview (TestSample in *SuperDomain*)");

            // Wait for the dashboard to appear.

            ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

            // Open Console agent filter lens dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Verify initial buttons state.

            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(CANCEL_LENS_DIALOG_BUTTON_ID), true);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(APPLY_LENS_BUTTON_ID), false);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(REMOVE_LENS_BUTTON_ID), false);

            // Parse table in the dialog.

            TableUi tableUi
                = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find specific row and click its selection box.

            clickSelectionBoxOfTableRow(tableUi, SELECTION_ROW);

            // Verify buttons state after selection.

            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(CANCEL_LENS_DIALOG_BUTTON_ID), true);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(APPLY_LENS_BUTTON_ID), true);
            verifyFilterDialogButtonState(ui, filterDialog,
                By.id(REMOVE_LENS_BUTTON_ID), false);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(451411)
    public void consoleLensVerifyClearLensTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard("Overview (TestSample in *SuperDomain*)");

            // Wait for the dashboard to appear.

            ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

            // Open Console agent filter lens dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            TableUi tableUi
                = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find specific row and click its selection box.

            clickSelectionBoxOfTableRow(tableUi, SELECTION_ROW);

            // Click OK button to apply the lens.

            clickFilterDialogButton(ui, filterDialog,
                By.id(APPLY_LENS_BUTTON_ID));

            // Move over to lens button and hover there to get tooltip.

            WebElement toolTip = produceFilterTooltip(ui);

            // Check tooltip for selected agent name.

            String toolTipText = getAgentsFromtTooltipText(toolTip.getText());
            assertEquals(toolTipText, SELECTION_ROW_AGENT_NAME,
                "Expected agent name not found in tooltip.");

            // Re-open filter dialog.

            filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find the row that we have previously selected and check it is still selected. Also
            // check that no other row is selected.

            verifyFilterSelection(tableUi, SELECTION_ROW);

            // Click upper right corner close button.

            clickFilterDialogCloseButton(ui, filterDialog,
                By.id("webview-consolelens-dialog-close-button"));

            // Select dasboard with "clear lens" configured.

            ui.selectConsoleDashboard(
                "Object Link Lens Dashboard (ObjectLinkLens_FunctionalTest in *SuperDomain*)");
            ui.waitFor(ExpectedConditions.presenceOfElementLocated(By.id("dashboard-image-1")));

            // Verify tooltip does not contain any agents.

            toolTip = produceFilterTooltip(ui);
            toolTipText = StringUtils.trim(toolTip.getText());
            assertTrue(toolTipText.equalsIgnoreCase("console agent filter lens"));

            // Show filter dialog

            filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Verify agent selection is empty.

            verifyFilterSelectionIsEmpty(tableUi);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(451410)
    public void consoleLensVerifyObjectLinkTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard(OBJECT_LINK_LENS_DASHBOARD);

            // Wait for it to render.

            ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div[id=webview-trafficlight]")));

            // Move over to lens button and hover there to get tooltip.

            WebElement toolTip = produceFilterTooltip(ui);

            // Verify that the tooltip does not contain any agents.

            String toolTipText = getAgentsFromtTooltipText(toolTip.getText());
            assertEquals(toolTipText, "Console Agent Filter Lens",
                "Expected default tooltip text.");

            // Open filter dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            TableUi tableUi = new TableUi(ui, filterDialog,
                By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Verify that filter selection is empty.

            verifyFilterSelectionIsEmpty(tableUi);

            // Click upper right corner close button.

            clickFilterDialogCloseButton(ui, filterDialog,
                By.id("webview-consolelens-dialog-close-button"));

            // Click traffic light link on the the dashboard.

            WebElement trafficLight = ui.getWebElement(
                By.cssSelector("div[id=webview-trafficlight]"));
            ui.getActions()
                .moveToElement(trafficLight)
                .click()
                .perform();

            // Wait for new dashboard to render.

            ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("dashboard-chart-7")));

            // Move over to lens button and hover there to get tooltip.

            toolTip = produceFilterTooltip(ui);

            // Verify that the tooltip does not contain any agents.

            toolTipText = getAgentsFromtTooltipText(toolTip.getText());
            assertEquals(toolTipText, "WebLogic Agent\nWurlitzerAgent_1",
                "Expected default tooltip text.");

            // Open filter dialog.

            filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Verify that requested agents are selected.

            verifyFilterSelection(tableUi, SELECTED_AGENTS_SET);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }


    private void consoleLensVerifyDashboardFilter(String dashboard) {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

            // Go to Console tab.

            ui.clickConsoleTab();

            // Wait for the example dashboard to render to avoid some issues with rendering when
            // we switch to another dashboard too fast.

            ui.waitFor(ExpectedConditions.textToBePresentInElementLocated(
                By.xpath("//div[@id='dashboard-scroll-container']//div[@id='dashboard-label-11']"),
                "Example Application Dashboard"));

            // Select desired dashboard to show.

            ui.selectConsoleDashboard(dashboard);

            // Wait for it to render.

            ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-investigator-livestringfigure-label-container")));

            // Open Console agent filter lens dialog.

            WebElement filterDialog = ui.openConsoleLensFilterDialog();

            // Parse table in the dialog.

            TableUi tableUi
                = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

            // Find specific row and click its selection box.

            clickSelectionBoxOfTableRow(tableUi, Arrays
                .asList("", "*SuperDomain*", "100-TSD-MORE", "WebLogic", "WebLogic Agent", "", ""));

            // Click OK button.

            clickFilterDialogButton(ui, filterDialog,
                By.cssSelector("div[id=webview-consolelens-dialog-apply-button] div"));

            // Wait for selection dialog to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(filterDialog));

            // Disable polling so that the contents of strings table is stable.

            ui.enablePolling(false);

            // Iterate over all lines in the table and verify that it contains only selected
            // agent metrics.

            WebElement container = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-investigator-livestringfigure-label-container")));
            ui.waitFor(
                ExpectedConditions.visibilityOfNestedElementsLocatedBy(container,
                    By.xpath(".//div[@id='webview-investigator-livestringfigure-label-1']")));
            WebElement stringsTable = ui.getWebElement(
                By.id("webview-investigator-livestringfigure-label-container"));
            List<WebElement> linesElements = ui.getWebElements(stringsTable, By.xpath("./div"));
            final WebElement apmLogo = ui.getWebElement(By.className("apmLogo"));
            final List<String> expectedAgent
                = Arrays.asList("*SuperDomain*", "100-TSD-MORE", "WebLogic", "WebLogic Agent");
            for (WebElement lineElement : linesElements) {
                WebElement toolTip = produceStringTableTooltip(ui, lineElement, By.xpath("./div"));
                String toolTipText = toolTip.getText();
                //log.debug("Tooltip text: >{}<", toolTipText);
                List<String> metric = Arrays.asList(toolTipText.split("[|:]\\s+"));
                log.debug("metric: {}", metric);
                assertTrue(metric.size() > 4);
                List<String> metricAgent = metric.subList(0, 4);
                assertEquals(metricAgent, expectedAgent);

                // Move to neutral element to make the tooltip disappear and wait for it to happen.

                ui.getActions()
                    .moveToElement(apmLogo)
                    .perform();
                ui.waitFor(ExpectedConditions.stalenessOf(toolTip));
            }

            // Re-enable polling.

            ui.enablePolling(true);

        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450715)
    public void consoleLensVerifyAlertProviderFilterTest() {
        consoleLensVerifyDashboardFilter(
            "Data Provider Test Alert (A-Webview-FunctionalTest-Module in *SuperDomain*)");
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450683)
    public void consoleLensVerifyMetricGroupingProviderFilterTest() {
        consoleLensVerifyDashboardFilter(
            "Data Provider Test Metric Grouping (A-Webview-FunctionalTest-Module in "
                + "*SuperDomain*)");
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(451412)
    public void consoleLensVerifyHistoryTest() {
        try {
            final String hubHost = envProperties
                .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);

            // First login and go to dashboard with lens settings to get URL with lens information.

            String urlWithLens;
            try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
                String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
                ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER, "");

                // Go to Console tab.

                ui.clickConsoleTab();

                // Select desired dashboard to show.

                ui.selectConsoleDashboard(OBJECT_LINK_LENS_DASHBOARD);

                // Wait for it to render.

                ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[id=webview-trafficlight]")));

                // Click the traffic light.

                ui.enablePolling(false);

                WebElement trafficLight = ui.getWebElement(
                    By.cssSelector("div[id=webview-trafficlight]"));

                ui.getActions()
                    .moveToElement(trafficLight)
                    .click()
                    .perform();

                // Wait for the dashboard to appear.

                ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

                // Get current URL which contains lens info.

                urlWithLens = ui.getCurrentUrl();

                ui.enablePolling(true);
            }

            // Now log in back but through previously obtained URL.

            try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
                ui.login(urlWithLens, WEBVIEW_USER, "");

                // Wait for the dashboard to appear.

                ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

                // Move over to lens button and hover there to get tooltip.

                WebElement toolTip = produceFilterTooltip(ui);

                // Verify that the tooltip does contain expected agents.

                String toolTipText = getAgentsFromtTooltipText(toolTip.getText());
                assertEquals(toolTipText, "WebLogic Agent\nWurlitzerAgent_1",
                    "Expected tooltip text with agents.");

                // Open Console agent filter lens dialog.

                WebElement filterDialog = ui.openConsoleLensFilterDialog();

                // Parse table in the dialog.

                TableUi tableUi
                    = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

                // Verify that the lens has been applied.

                verifyFilterSelection(tableUi, SELECTED_AGENTS_SET);

                // Verify remove lens button is enabled.

                verifyFilterDialogButtonState(ui, filterDialog, By.id(REMOVE_LENS_BUTTON_ID), true);

                // Click the remove lens button.

                clickFilterDialogButton(ui, filterDialog, By.id(REMOVE_LENS_BUTTON_ID));

                // Wait for the filter dialog to disappear.

                ui.waitFor(ExpectedConditions.stalenessOf(filterDialog));

                // Go to Home tab.

                ui.clickHomeTab();

                // Navigate back.

                ui.getNavigation().back();

                // Wait for the dashboard to appear.

                ui.waitForWebElement(By.xpath("//div[text()='User Experience']"));

                // Move over to lens button and hover there to get tooltip.

                toolTip = produceFilterTooltip(ui);

                // Verify that the tooltip does contain selected agents.

                toolTipText = getAgentsFromtTooltipText(toolTip.getText());
                assertEquals(toolTipText, "WebLogic Agent\nWurlitzerAgent_1",
                    "Expected default tooltip text.");

                // Open filter dialog.

                filterDialog = ui.openConsoleLensFilterDialog();

                // Parse table in the dialog.

                tableUi = new TableUi(ui, filterDialog, By.id(CONSOLE_LENS_TABLE_GRID_ID));

                // Verify that requested agents are selected.

                verifyFilterSelection(tableUi, SELECTED_AGENTS_SET);
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
