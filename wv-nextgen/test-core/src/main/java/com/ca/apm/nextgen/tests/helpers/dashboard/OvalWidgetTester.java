package com.ca.apm.nextgen.tests.helpers.dashboard;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

/**
 * <code> OvalWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class OvalWidgetTester extends ADashboardWidgetTester {

    public OvalWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
    }

    @Override
    public String getIdentifier() {
        return "dashboard-sprite";
    }

}
