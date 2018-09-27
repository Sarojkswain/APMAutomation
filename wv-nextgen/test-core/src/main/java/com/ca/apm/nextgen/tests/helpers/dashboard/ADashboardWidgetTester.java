package com.ca.apm.nextgen.tests.helpers.dashboard;

import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kBorderWidthStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kCursorStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontFamilyStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontSizeStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontStyleStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kFontWeightStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kHeightStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kLeftStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kOnClickAttribute;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kPointerCursorStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kStyleAttribute;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kTextAlignStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kTextColorStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kTopStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kWidthStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.Constants.kZindexStyle;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kContextMenuLink;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kCtLinkToken;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kDefaultLink;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kManagementModuleLink;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kWebLink;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kXCoordinate;
import static com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConstants.kYCoordinate;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.dashboard.DashboardConfiguration.WidgetConfiguration;

public abstract class ADashboardWidgetTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADashboardWidgetTester.class);

    public static final String METRIC_TOOLTIP_ID = "metric-tooltip"; // metric.tooltip.id
    public static final String DB_HYPERLINK_WIDGET_ID = "dashboard-hyperlink;"; // db.hyperlink.widget.id
    public static final String DB_CONTEXT_MENU_ID = "context_menu-"; // db.context.menu.id
    public static final String DB_CONTEXT_SUBMENU_ID = "context_submenu-"; // db.context.submenu.id
    public static final String METRIC_TOOLTIP_HYPERLINK_ID = "metric-tooltip-hyperlink"; // metric.tooltip.hyperlink.id

    public static final String DB_DIALMETER_CHART_ID = "-chart"; // db.dialmeter.chart.id
    public static final String DB_DIALMETER_LABEL_ID = "-label"; // db.dialmeter.label.id

    public static final String DB_STRING_WIDGET_LABEL_XPATH = "div/div"; // db.string.widget.label.xpath

    protected WebViewUi ui;
    protected WidgetConfiguration fWidgetConfig;
    private List<DashboardLink> fDashboardLinks;

    public ADashboardWidgetTester(WidgetConfiguration widgetConfig, WebViewUi ui) {
        this.ui = ui;
        this.fWidgetConfig = widgetConfig;
        this.fDashboardLinks = getJavaScriptObjectLinks();
    }

    public abstract String getIdentifier();

    protected String getId() {
        return getIdentifier() + "-" + fWidgetConfig.getId();
    }

    @Override
    public String toString() {
        return getId();
    }

    /*
     * Verifies all options defined
     */
    public boolean verifyOptions() {
        return true;
    }

    /*
     * Verifies all text options defined
     */
    public boolean verifyText() {
        return true;
    }

    /*
     * Verifies location and size of Widget
     * Compares location and size info in fWidgetConfig
     * with actual rendered Widget's size and location
     */
    public boolean verifyGeometry() {
        boolean status = false;
        WebElement element;
        element = ui.getWebElement(By.id(getId()));
        if (element != null) {
            status = true;
            Integer borderWidth = (Integer) getStyleAttribute(element, kBorderWidthStyle);
            if (borderWidth == null) {
                borderWidth = 1;
            }
            Integer browserX = (Integer) getStyleAttribute(element, kLeftStyle);
            Integer browserY = (Integer) getStyleAttribute(element, kTopStyle);

            Integer browserWidth = (Integer) getStyleAttribute(element, kWidthStyle);
            Integer browserHeight = (Integer) getStyleAttribute(element, kHeightStyle);

            Integer configX = fWidgetConfig.getXCoordinate();
            Integer configY = fWidgetConfig.getYCoordinate();

            Integer configWidth = getWidth();
            Integer configHeight = getHeight();

            status &= testIntValues(browserX, configX, borderWidth, kXCoordinate);
            status &= testIntValues(browserY, configY, borderWidth, kYCoordinate);
            status &= testIntValues(browserWidth, configWidth, 6 * borderWidth, kWidthStyle);
            status &= testIntValues(browserHeight, configHeight, 6 * borderWidth, kHeightStyle);
        }
        return status;
    }

    /*
     * Verifies all object links defined
     */
    public boolean verifyObjectLinks() {
        boolean status = true;
        String[] configuredLinkNames = fWidgetConfig.getLinkNames();
        if (fDashboardLinks.size() > 0) {
            // verify default link if it exists
            DashboardLink defaultLink = getDefaultLink();
            if (defaultLink != null) {
                // verify cursor type is 'pointer'
                if (!getIdentifier().contains(DB_HYPERLINK_WIDGET_ID)) {
                    status &= hasPointerCursor(By.id(getId()));
                }

                // verify default links works
                WebElement widget = null;
                if (this.getIdentifier().equals("dashboard-alert")) {
                    widget =
                        ui.waitForWebElement(By.cssSelector("div[id=" + getId()
                            + "] div[id='webview-trafficlight']"));
                } else {
                    widget = ui.waitForWebElement(By.id(getId()));
                }
                status &= widget != null;
                status &=
                    jumpToAndReturn(widget, defaultLink.getLinkToken(), defaultLink.getLinkType(),
                        kDefaultLink);
            }
            // jump to and return for each context menu item link
            for (int i = 0; i < fDashboardLinks.size() && status; i++) {
                By menuParentSelector = By.id(getId());
                By menuSelector = By.id(DB_CONTEXT_MENU_ID + fWidgetConfig.getId());
                By subMenuSelector = By.id(DB_CONTEXT_SUBMENU_ID + fWidgetConfig.getId() + "." + i);
                int count = 0;
                boolean verified = false;
                while (count++ < 4 && !verified) {
                    try {
                        verified = true;
                        WebElement menuItem =
                            getContextMenuItem(menuParentSelector, menuSelector, subMenuSelector);
                        verified &= menuItem != null;
                        if (menuItem != null) {
                            verified &= verifyLinkName(menuItem.getText(), configuredLinkNames, i);
                            DashboardLink dbLink = fDashboardLinks.get(i);
                            verified &=
                                jumpToAndReturn(menuItem, dbLink.getLinkToken(),
                                    dbLink.getLinkType(), kContextMenuLink);
                        }
                    } catch (StaleElementReferenceException e) {
                        ErrorReport.logExceptionFmt(LOGGER, e,
                            "Trying to recover from a stale element: {0}");
                        verified = false;
                    }
                }
                status &= verified;
            }
        }
        return status;
    }

    /*
     * Verifies all text options defined
     */
    public boolean verifyToolTips() {
        return true;
    }

    protected Integer getWidth() {
        return fWidgetConfig.getWidth();
    }

    protected Integer getHeight() {
        return fWidgetConfig.getHeight();
    }

    protected boolean testIntValues(Integer browserValue, Integer configValue, int errorVal,
        String errMsg) {
        boolean status = true;
        if (browserValue == null) {
            LOGGER.error("Browser {} is null", errMsg);
            status = false;
        } else if (configValue == null) {
            LOGGER.error("Configured {} is null", errMsg);
            status = false;
        } else if (Math.abs(browserValue - configValue) > errorVal) {
            LOGGER.error("Browser {} ({}) does not equal Configured {} ({}) +-{}", errMsg,
                browserValue, errMsg, configValue, errorVal);
            status = false;
        }
        if (status) {
            LOGGER.info("Browser {} ({}) equals Configured {} ({}) +-{}", errMsg,
                browserValue, errMsg, configValue, errorVal);
        }
        return status;
    }

    protected boolean testStringValues(String browserValue, String configValue, String errMsg) {
        boolean status = true;
        if (browserValue == null) {
            LOGGER.error("Browser {} is null", errMsg);
            status = false;
        } else if (configValue == null) {
            LOGGER.error("Configured {} is null", errMsg);
            status = false;
        } else if (!browserValue.equals(configValue)) {
            LOGGER.error("Browser {} ({}) does not equal Configured {} ({})", errMsg, browserValue,
                errMsg, configValue);
            status = false;
        }
        if (status) {
            LOGGER.info("Browser {} ({}) equals Configured {} ({})", errMsg, browserValue, errMsg,
                configValue);
        }
        return status;
    }

    protected boolean verifyTextOptions() {
        boolean status = false;
        WebElement element = ui.getWebElement(By.id(getId()));
        if (element != null) {
            status = true;
            Integer browserFontSize = (Integer) getStyleAttribute(element, kFontSizeStyle);
            String browserFontWeight = (String) getStyleAttribute(element, kFontWeightStyle);
            String browserFontStyle = (String) getStyleAttribute(element, kFontStyleStyle);
            String browserFontFamily = (String) getStyleAttribute(element, kFontFamilyStyle);
            String browserTextColor = (String) getStyleAttribute(element, kTextColorStyle);
            String browserTextAlign = (String) getStyleAttribute(element, kTextAlignStyle);

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

    protected boolean jumpToAndReturn(WebElement linkElement, String linkToken, String linkType,
        String uiSource) {
        boolean status = true;
        String originalUrl = getCurrentUrl();
        String errMsg = uiSource + "(" + linkToken + ")";
        LOGGER.info("jumpToAndReturn(): Attempting jump to {}", errMsg);
        // quit if linkElement is null
        if (linkElement == null) {
            LOGGER.error("jumpToAndReturn(): failed;  Jump to {} failed; element not found",
                errMsg);
            status = false;
        } else if (currentURLContainsToken(linkToken, false)) {
            // attempt jump only if destination is not current page
            LOGGER.info("jumpToAndReturn():  Jump not required:  already at target ({})",
                originalUrl);
            linkElement.click();
        } else if (status) {
            // click on link element
            linkElement.click();
            sleep(5000);
            // compare current URL with linkToken; did we jump OK?
            // perform test and jump back only when not a web URL
            if (!linkType.equalsIgnoreCase(kWebLink)) {
                status = currentURLContainsToken(linkToken);
                if (!status) {
                    LOGGER
                        .error("jumpToAndReturn(): failed;  Jump to {} failed; tokens do not match",
                            errMsg);
                } else {
                    LOGGER.info("jumpToAndReturn():  success;  Jump to {} successful", errMsg);
                    // return back to original page if link type is not "web"
                    // return back to original page
                    ui.back();
                    LOGGER.info("jumpToAndReturn(): Attempt 1 to navigate back to ({})",
                        originalUrl);
                    sleep(5000); // wait for page to render

                    // compare current URL with original URl; did we return OK?
                    status =
                        currentURLContainsToken(originalUrl,
                            !linkType.equalsIgnoreCase(kManagementModuleLink));

                    // back again if MM page and our first attempt failed; token "ct" is potentially
                    // wrong
                    if (!status && linkType.equalsIgnoreCase(kManagementModuleLink)) {
                        ui.back();
                        LOGGER.info("jumpToAndReturn():  Attempt 2 to navigate back to ({})",
                            originalUrl);
                        sleep(5000); // wait for page to render

                        // compare current URL with original URl; did we return OK?
                        status = currentURLContainsToken(originalUrl);
                    }
                    if (!status) {
                        LOGGER.error(
                            "jumpToAndReturn(): failed;  Return to orignal URL ({}) failed",
                            originalUrl);
                    }
                }
            }
        }
        if (status) {
            LOGGER.info("jumpToAndReturn(): success;  Jump to and return from {} successful",
                errMsg);
        }
        return status;
    }

    protected String getOnClickAttribute(WebElement toolTip) {
        String linkToken = null;
        if (toolTip != null) {
            String onClick = ui.getWebElementAttribute(toolTip, kOnClickAttribute);
            if (onClick != null) {
                String[] tokens = onClick.split("\"");
                linkToken = tokens[1];
            }
        }
        return linkToken;
    }

    public boolean hasPointerCursor(By selector) {
        boolean status = false;
        String cursorType = null;
        WebElement element = ui.getWebElement(selector);
        if (element != null) {
            cursorType = (String) getStyleAttribute(element, kCursorStyle);
            if (cursorType != null) {
                status = cursorType.equalsIgnoreCase(kPointerCursorStyle);
            }
        }
        if (!status) {
            LOGGER.error("{} cursor type ({}) is not equal to (" + kPointerCursorStyle + ")",
                selector, cursorType);
        }
        return status;
    }

    /*
     * moves to element that has a MetricToolTip
     * causing tooltip to be displayed
     * 
     * @param toolTipId Id for tooltip
     * 
     * @return WebElement tooltip
     */
    protected WebElement getMetricToolTip(WebElement toolTipOwner, String toolTipId) {
        WebElement toolTip = null;
        if (toolTipOwner != null) {
            // try twice to get tooltip to appear
            ui.getActions().moveToElement(toolTipOwner).build().perform();

//            toolTip = ui.getWebElement(By.id(METRIC_TOOLTIP_ID));
//            toolTip = ui.waitForWebElement(By.id(METRIC_TOOLTIP_ID));
            toolTip = ui.waitForWebElementOrNull(By.id(METRIC_TOOLTIP_ID));

            if (toolTip == null) {
                Point point = toolTipOwner.getLocation();
                Dimension size = toolTipOwner.getSize();
                int x = point.getX() + size.getWidth() / 2;
                int y = point.getY() + size.getHeight() / 2;
                LOGGER.error("getMetricToolTip():: Failed: to show tooltip at location({},{})", x, y);
            } else {
                String text = toolTip.getText();
                if (text != null) {
                    String uid = text.replace("\n", "");
                    // uid = uid.replace("     ", "");
                    // uid = uid.replace("    ", "");
                    // uid = uid.replace("   ", "");
                    // uid = uid.replace("  ", "");
                    // uid = uid.replace("| ", "|");
                    LOGGER.info("getMetricToolTip():: Tooltip for metric ({}) located successfully", uid);
                }
                // return hyperlink as tooltip if requested
                if (METRIC_TOOLTIP_HYPERLINK_ID.equals(toolTipId)) {
                    toolTip = ui.getWebElement(By.id(toolTipId));
                }
            }
        }
        return toolTip;
    }

    protected Object getStyleAttribute(WebElement element, String attribute) {
        Object value = null;
        String style = element.getAttribute(kStyleAttribute);
        if (style != null && !style.isEmpty()) {
            String[] styleElements = style.split(";");
            for (String styleElement : styleElements) {
                String[] styleTokens = styleElement.split(":");
                String styleAttribute = styleTokens[0].trim();
                String styleValue = styleTokens[1].trim();
                if (styleAttribute.equals(attribute)) {
                    switch (attribute) {
                        case kFontSizeStyle:
                        case kBorderWidthStyle:
                        case kLeftStyle:
                        case kTopStyle:
                        case kWidthStyle:
                        case kHeightStyle:
                            value = Integer
                                .parseInt(styleValue.substring(0, styleValue.length() - 2));
                            break;
                        case kZindexStyle:
                            value = Integer.parseInt(styleValue);
                            break;
                        case kTextColorStyle:
                            value = rgbToHex(styleValue);
                            break;
                        default:
                            value = styleValue.replaceAll("'", "");
                            break;
                    }
                    break;
                }
            }
        }
        return value;
    }

    protected String rgbToHex(String rgbString) {
        String hexValue = "";
        int beginIndex = rgbString.indexOf('(') + 1;
        int endIndex = rgbString.indexOf(')');
        String rgbValues = rgbString.substring(beginIndex, endIndex);
        String[] tokens = rgbValues.split(",");
        if (tokens.length > 2) {
            int r = Integer.parseInt(tokens[0].trim());
            int g = Integer.parseInt(tokens[1].trim());
            int b = Integer.parseInt(tokens[2].trim());
            hexValue = String.format("#%02x%02x%02x", r, g, b).toUpperCase();
        }
        return hexValue;
    }

    protected String getInnerHTML(String id) {
        String innerHTML = null;
        String script = "return document.getElementById(\"" + id + "\").innerHTML;";
        try {
            innerHTML = (String) ui.getJavaScriptExecutor().executeScript(script);
        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to execute javaScript ({1}). Exception: {0}", script);
        }
        return innerHTML;
    }

    protected void moveFromWebElement(WebElement element, String elementId) {
        if (element != null) {
            ui.getActions().moveToElement(element, -element.getSize().width + 2, 0).perform();
            waitForWebElementInvisible(By.id(elementId));
        }
    }

    private void waitForWebElementInvisible(By selector) {
        ui.waitFor(ExpectedConditions.invisibilityOfElementLocated(selector));
    }

    private boolean verifyLinkName(String browserLinkNameText, String[] configuredLinkNames,
        int index) {
        boolean status = true;
        int jsDBLinksSize = this.fDashboardLinks.size();
        int configuredLinkNamesSize = configuredLinkNames == null ? 0 : configuredLinkNames.length;
        String browserLinkName = browserLinkNameText == null ? "" : browserLinkNameText;
        if (index < 0 || index >= configuredLinkNamesSize) {
            LOGGER.error(
                "verifyLinkNames(): failed; Browser link name ({}) not found in configured data",
                browserLinkName);
            status = false;
        } else if (configuredLinkNamesSize != jsDBLinksSize) {
            LOGGER.error(
                "verifyLinkNames(): failed; javascript link name size({}) not equal configured "
                    + "size{})",
                jsDBLinksSize, configuredLinkNamesSize);
            status = false;
        } else if (!configuredLinkNames[index].equals(browserLinkName)) {
            LOGGER.error(
                "verifyLinkNames(): failed; Browser link name ({}) does not equal Configured link"
                    + " name ({})",
                browserLinkName, configuredLinkNames[index]);
            status = false;
        } else {
            LOGGER.info(
                "verifyLinkName(): success; Browser link name ({}) equals Configured link name "
                    + "({})",
                browserLinkName, configuredLinkNames[index]);
            LOGGER.info(
                "verifyLinkName(): success; Javascript link name ({}) equals Configured link name"
                    + " ({})",
                this.fDashboardLinks.get(index).getLinkDisplayName(), configuredLinkNames[index]);
        }
        return status;
    }

    private WebElement getContextMenuItem(By parentSelector, By contextMenuSelector,
        By contextSubMenuSelector) {
        WebElement element = null;
        boolean notFound = true;
        int counter = 3;
        WebElement parentElement = ui.waitForWebElement(parentSelector);
        while (notFound && counter-- > 0 && parentElement != null) {
            // click to show context menu
            ui.getActions().moveToElement(parentElement, 10, 10).contextClick().perform();
            // locate and move to context menu to show sub-menu
            element = ui.waitForWebElement(contextMenuSelector);
            if (element != null) {
                ui.getActions().moveToElement(element).perform();
                notFound = false;
            } else {
                LOGGER.info("getContextMenuItemText(): retry {}", counter);
                ui.getActions().moveToElement(parentElement, -10, -10).perform();
                sleep(100);
            }
        }
        if (element != null) {
            // get Sub menu text
            element = ui.waitForWebElement(contextSubMenuSelector);
        }
        // report success
        if (element != null) {
            LOGGER.info("getContextMenuItemText(): success; found menu element({})",
                contextSubMenuSelector);
        }
        sleep(1000);
        return element;
    }

    private boolean currentURLContainsToken(String linkToken) {
        return currentURLContainsToken(linkToken, true);
    }

    private boolean currentURLContainsToken(String linkToken, boolean reportFailures) {
        boolean status = true;
        String[] linkTokenTokens = linkToken.split(";");
        String currentPageUrl = getCurrentUrl();
        for (String token : linkTokenTokens) {
            // ignore "ct" token
            if (!token.startsWith(kCtLinkToken)) {
                if (!currentPageUrl.contains(token)) {
                    status = false;
                    if (reportFailures) {
                        LOGGER.error(
                            "currentURLContainsToken():  failed;  Token ({}) not found in {}",
                            token, currentPageUrl);
                    }
                    break;
                }
            }
        }
        if (status) {
            LOGGER.info("currentURLContainsToken():  success;  Tokens ({}) found in {}", linkToken,
                currentPageUrl);
        }
        return status;
    }

    @SuppressWarnings("unchecked")
    private List<DashboardLink> getJavaScriptObjectLinks() {
        int widgetId = fWidgetConfig.getId();
        List<DashboardLink> dashboardLinks = new ArrayList<>();
        List<String> dbLinkStrings = null;
        String script =
            "if (typeof DashboardLinks != \"undefined\" && typeof DashboardLinks[" + widgetId
                + "] != \"undefined\" )" + "return DashboardLinks[" + widgetId
                + "]; else return null;";
        try {
            dbLinkStrings = (List<String>) ui.getJavaScriptExecutor().executeScript(script);
            if (dbLinkStrings != null) {
                for (String dbLinkString : dbLinkStrings) {
                    String[] tokens = dbLinkString.split("~");
                    dashboardLinks.add(new DashboardLink(tokens[0], tokens[1], tokens[2]
                        .equalsIgnoreCase("yes"), tokens[3], tokens[4]));
                }
            }
        } catch (Exception e) {
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e,
                "Failed to execute javaScript ({1}). Exception: {0}", script);
        }
        return dashboardLinks;
    }

    private DashboardLink getDefaultLink() {
        DashboardLink defaultLink = null;
        for (DashboardLink link : fDashboardLinks) {
            if (link.isDefault()) {
                defaultLink = link;
                break;
            }
        }
        return defaultLink;
    }

    private String getCurrentUrl() {
        String url = ui.getCurrentUrl();
        String currentUrl = url.replace("%25", "%");
        return currentUrl;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Cannot sleep for {} millis. Interrupted.", millis);
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Exception: {0}");
        }
    }

    private static class DashboardLink {
        private String linkDisplayName;
        private String linkToken; // internal link to a dashboard, or other target.
        private String linkType;
        private boolean isDefault;
        private String linkId;

        private DashboardLink(String linkDisplayName, String linkToken, boolean isDefault,
            String linkType, String itemId) {
            this.linkDisplayName = linkDisplayName;
            this.linkToken = linkToken;
            this.isDefault = isDefault;
            this.linkType = linkType;
            this.linkId = itemId;
        }

        private String getLinkDisplayName() {
            return linkDisplayName;
        }

        private String getLinkToken() {
            return linkToken;
        }

        private boolean isDefault() {
            return isDefault;
        }

        private String getLinkType() {
            return linkType;
        }

        @SuppressWarnings("unused")
        public String getLinkId() {
            return linkId;
        }
    }

}
