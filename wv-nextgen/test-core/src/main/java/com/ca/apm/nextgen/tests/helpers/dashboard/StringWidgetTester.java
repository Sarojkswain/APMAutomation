package com.ca.apm.nextgen.tests.helpers.dashboard;

import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * <code> StringWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class StringWidgetTester extends ADashboardWidgetTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringWidgetTester.class);

    public StringWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
    }

    @Override
    public String getIdentifier() {
        return "dashboard-string";
    }

    @Override
    public boolean verifyOptions() {
        return verifyTextOptions();
    }

    @Override
    public boolean verifyToolTips() {
        By selector = By.id(getId());
        try {
            WebElement stringWidget = ui.getWebElement(selector);
            LOGGER.debug("StringWidgetTester.verifyToolTips():: stringWidget = {}", stringWidget);
            List<WebElement> labels = ui.getWebElements(stringWidget,
                By.xpath(DB_STRING_WIDGET_LABEL_XPATH));
            LOGGER.debug("StringWidgetTester.verifyToolTips():: labels = {}", labels);
            final WebElement apmLogo = ui.getWebElement(By.className("apmLogo"));
            for (WebElement toolTipOwner : labels) {
                LOGGER
                    .debug("StringWidgetTester.verifyToolTips():: toolTipOwner = {}", toolTipOwner);
                WebElement toolTip = getMetricToolTip(toolTipOwner, METRIC_TOOLTIP_ID);
                assertNotNull(toolTip, "Tooltip must not be null.");
                // Move away to logo to let the tooltip disappear.
                ui.getActions()
                    .moveToElement(apmLogo)
                    .perform();
            }
        } catch (Throwable e) {
            ui.takeScreenShot("verifyToolTips-");
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to verify tooltips in string widget specified by {1}. Exception: {0}",
                selector.toString());
        }

        return true;
    }

}
