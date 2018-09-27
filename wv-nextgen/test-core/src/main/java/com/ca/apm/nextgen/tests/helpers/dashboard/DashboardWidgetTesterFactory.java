package com.ca.apm.nextgen.tests.helpers.dashboard;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kAlertViewerWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kConnectionWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kElbowConnectionWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kImageWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLabelWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLineWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLiveBarChartWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLiveDialMeterWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLiveGraphWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLiveStringWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kLiveTextWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kOvalWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kPolygonWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kRectangleWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kRoundRectangleWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kScribbleWidgetType;
import static com.ca.apm.nextgen.tests.helpers.dashboard.WidgetTypes.kTypeViewerWidgetType;

public class DashboardWidgetTesterFactory {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(DashboardWidgetTesterFactory.class);

    private DashboardWidgetTesterFactory() {
    }

    /**
     * Returns a Dashboard widget class that extends ADashboardWidgetTester
     *
     * @param widgetConfig
     * @param ui
     * @return
     */
    public static ADashboardWidgetTester createWidget(WidgetConfiguration widgetConfig,
        WebViewUi ui) {
        String widgetType = widgetConfig.getType();
        LOGGER.debug("DashboardWidgetTesterFactory.createWidget():: widgetType = {}", widgetType);
        ADashboardWidgetTester widget = null;

        if (kRectangleWidgetType.equalsIgnoreCase(widgetType)
            || kRoundRectangleWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new RectangleWidgetTester(widgetConfig, ui);
        } else if (kOvalWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new OvalWidgetTester(widgetConfig, ui);
        } else if (kLabelWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new LabelWidgetTester(widgetConfig, ui);
        } else if (kLiveStringWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new StringWidgetTester(widgetConfig, ui);
        } else if (kLiveTextWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new TextWidgetTester(widgetConfig, ui);
        } else if (kLiveGraphWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new GraphWidgetTester(widgetConfig, ui);
        } else if (kLiveBarChartWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new BarChartWidgetTester(widgetConfig, ui);
        } else if (kAlertViewerWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new AlertWidgetTester(widgetConfig, ui);
        } else if (kImageWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new ImageWidgetTester(widgetConfig, ui);
        } else if (kLiveDialMeterWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new DialMeterWidgetTester(widgetConfig, ui);
        } else if (kScribbleWidgetType.equalsIgnoreCase(widgetType)
            || kPolygonWidgetType.equalsIgnoreCase(widgetType)
            || kLineWidgetType.equalsIgnoreCase(widgetType)
            || kConnectionWidgetType.equalsIgnoreCase(widgetType)
            || kElbowConnectionWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new DirectionalLineWidgetTester(widgetConfig, ui);
        } else if (kTypeViewerWidgetType.equalsIgnoreCase(widgetType)) {
            widget = new XMLTypeViewerWidgetTester(widgetConfig, ui);
        }

        return widget;
    }

}
