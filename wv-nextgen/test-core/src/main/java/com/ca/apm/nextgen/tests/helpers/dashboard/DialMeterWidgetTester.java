package com.ca.apm.nextgen.tests.helpers.dashboard;

import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kInvestigatorLink;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kToolTipLink;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

/**
 * <code> DialMeterWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class DialMeterWidgetTester extends ADashboardWidgetTester {

    public DialMeterWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
    }

    @Override
    public String getIdentifier() {
        return "dashboard-dialmeter";
    }

    @Override
    public boolean verifyToolTips() {
        boolean status = false;
        String toolTipOwnerId = getId() + DB_DIALMETER_CHART_ID;
        WebElement toolTipOwner = ui.getWebElement(By.id(toolTipOwnerId));
        // tooltip element get stale sometimes
        int count = 0;
        while (count++ < 4 && !status) {
            // verify hyperlink tooltip in DialMeter Chart
            WebElement toolTip = getMetricToolTip(toolTipOwner, METRIC_TOOLTIP_HYPERLINK_ID);
            String linkToken = getOnClickAttribute(toolTip);
            if (linkToken != null) {
                try {
                    status = jumpToAndReturn(toolTip, linkToken, kInvestigatorLink, kToolTipLink);
                } catch (StaleElementReferenceException t) {
                    status = false;
                }
            }
        }
        // verify label tooltip (no hyperlink) in DialMeter Label if visible
        if (fWidgetConfig.isLabelsVisible()) {
            toolTipOwnerId = getId() + DB_DIALMETER_LABEL_ID;
            toolTipOwner = ui.getWebElement(By.id(toolTipOwnerId));
            status &= (getMetricToolTip(toolTipOwner, METRIC_TOOLTIP_ID) != null);
        }
        return status;
    }

}
