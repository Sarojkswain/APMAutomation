package com.ca.apm.nextgen.tests.helpers.dashboard;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

/**
 * <code> TextWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class TextWidgetTester extends ADashboardWidgetTester {

    public TextWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
    }

    @Override
    public String getIdentifier() {
        return "dashboard-text";
    }

    @Override
    public boolean verifyOptions() {
        return verifyTextOptions();
    }

    @Override
    public boolean verifyToolTips() {
        boolean status = false;
        WebElement textWidget = ui.getWebElement(By.id(getId()));
        if (textWidget != null) {
            List<WebElement> textWidgetLabels = ui.getWebElements(textWidget, By.xpath("div[2]"));
            if (textWidgetLabels != null) {
                WebElement toolTip =
                    getMetricToolTip((WebElement) textWidgetLabels.toArray()[0], METRIC_TOOLTIP_ID);
                status = toolTip != null;
            }
        }
        return status;
    }

}
