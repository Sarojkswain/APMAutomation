package com.ca.apm.nextgen.tests.helpers.dashboard;

import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontFamilyStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontSizeStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontStyleStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontWeightStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kTextAlignStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kTextColorStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kText;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

/**
 * <code> LabelWidgetTester</code> extends ADashboardWidgetTester
 * It implements all abstract methods declared in the base class
 * and overrides any test method in the base class as needed.
 * 
 */
public class LabelWidgetTester extends ADashboardWidgetTester {

    public LabelWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        super(widgetConfig, ui);
    }

    @Override
    public String getIdentifier() {
        if (fWidgetConfig.hasDefaultLink()) {
            return "dashboard-hyperlink";
        }
        return "dashboard-label";
    }

    @Override
    public boolean verifyText() {
        String browserText = null;
        String configText = null;
        // use Selenium to get text when embedded in hyperlink <a> tag
        if (fWidgetConfig.hasDefaultLink()) {
            WebElement element = ui.getWebElement(By.id(getId()));
            if (element == null) {
                return false;
            }
            browserText = element.getText();
        } else {
            // otherwise use Javascript to get innerHTML
            browserText = getInnerHTML(getId());
            if (browserText == null) {
                return false;
            }
        }
        configText = fWidgetConfig.getText();
        return testStringValues(browserText, configText, kText);
    }

    @Override
    public boolean verifyOptions() {
        if (fWidgetConfig.hasDefaultLink()) {
            return verifyHyperLinkOptions();
        } else {
            return verifyTextOptions();
        }
    }

    private boolean verifyHyperLinkOptions() {
        boolean status = false;
        WebElement divEl = ui.getWebElement(By.id(getId()));
        WebElement anchorEl = ui.getWebElement(By.cssSelector("div[id='" + getId() + "'] a"));
        if (divEl != null && anchorEl != null) {
            status = true;
            Integer browserFontSize = (Integer) getStyleAttribute(anchorEl, kFontSizeStyle);
            String browserFontWeight = (String) getStyleAttribute(anchorEl, kFontWeightStyle);
            String browserFontStyle = (String) getStyleAttribute(anchorEl, kFontStyleStyle);
            String browserFontFamily = (String) getStyleAttribute(anchorEl, kFontFamilyStyle);
            String browserTextColor = (String) getStyleAttribute(anchorEl, kTextColorStyle);
            String browserTextAlign = (String) getStyleAttribute(divEl, kTextAlignStyle);

            Integer configFontSize = fWidgetConfig.getFontSize();
            String configFontWeight = fWidgetConfig.getFontWeight();
            String configFontStyle = fWidgetConfig.getFontStyle();
            String configFontFamily = fWidgetConfig.getFontName();
            String configTextColor = fWidgetConfig.getTextColor();
            String configTextAlign = fWidgetConfig.getTextAlign();

            status &= testIntValues(browserFontSize, configFontSize, 0, kFontSizeStyle);
            status &= testStringValues(browserFontWeight, configFontWeight, kFontWeightStyle);
            status &= testStringValues(browserFontStyle, configFontStyle, kFontStyleStyle);
            status &= testStringValues(browserFontFamily, configFontFamily, kFontFamilyStyle);
            status &= testStringValues(browserTextColor, configTextColor, kTextColorStyle);
            status &= testStringValues(browserTextAlign, configTextAlign, kTextAlignStyle);
        }
        return status;
    }

}
