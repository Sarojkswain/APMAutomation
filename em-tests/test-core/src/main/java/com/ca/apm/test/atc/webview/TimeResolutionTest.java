package com.ca.apm.test.atc.webview;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UrlUtils;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.WebView;
import com.ca.apm.test.atc.common.element.PageElement;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test of F9270 APM - Time resolution control in WebView (Limited Version).
 * 
 * @author strma15
 */
public class TimeResolutionTest extends UITest {

    private static final Long VW_TR_LIVE = 0L;
    
    /**
     * Map of the time range label to the time range length in MINUTES.
     */
    private static final Map<String, Long> timeRanges = new HashMap<String, Long>();
    
    /**
     * Map of the time resolution label to the time range length in MILLISECONDS.
     */
    private static final Map<String, Long> timeResolutions = new HashMap<String, Long>();
    
    private UI ui;
    private WebView wv;
    private String wvLiveUrl;
    private String wvHistUrl;
       
    private void init() throws Exception {
        ui = getUI();
        
        ui.login();
    }
    
    @BeforeClass
    private void initializeMaps() {
        timeRanges.put("8 Minutes", 8L);
        timeRanges.put("20 Minutes", 20L);
        timeRanges.put("1 Hour", 60L);
        timeRanges.put("2 Hours", 120L);
        timeRanges.put("6 Hours", 360L);
        timeRanges.put("12 Hours", 720L);
        timeRanges.put("24 Hours", 1440L);
        timeRanges.put("48 Hours", 2880L);
        timeRanges.put("60 Hours", 3600L);
        timeRanges.put("7 Days", 10080L);
        timeRanges.put("30 Days", 43200L);
        
        timeResolutions.put("15 seconds", 15000L);
        timeResolutions.put("30 seconds", 30000L);
        timeResolutions.put("1 minute", 60000L);
        timeResolutions.put("2 minutes", 120000L);
        timeResolutions.put("6 minutes", 360000L);
        timeResolutions.put("12 minutes", 720000L);
        timeResolutions.put("24 minutes", 1440000L);
        timeResolutions.put("48 minutes", 2880000L);
        timeResolutions.put("1 hour", 3600000L);
        timeResolutions.put("168 minutes", 10080000L);
        timeResolutions.put("12 hours", 43200000L);
    }
    
    /**
     * Test that the live mode is kept when we jump from ATC UI to WebView
     * and the time resolution combo box is disabled.
     * @throws Exception 
     */
    @Test(groups = {"failing", "dependency"})
    public void testTimeResolutionDisabledInLiveMode() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().turnOnLiveMode();
        
        WebElement wvLink = ui.getTopNavigationPanel().getAnyWebviewLinkElement();
        
        // Remember the URL for usage in further test methods
        wvLiveUrl = wvLink.getAttribute("href"); 
        Map<String, Long> attr = UrlUtils.getMapOfParameterValuesFromWebViewUrl(wvLiveUrl, true);
        Assert.assertEquals(attr.get("tr"), VW_TR_LIVE);
        
        wvLink.click();
        ui.switchToWebView();

        wv = new WebView(ui);
        
        Assert.assertTrue(wv.getTimeController().isEnabled());
        Assert.assertEquals("Live", wv.getTimeControllerSelectedValue());
        Assert.assertFalse(wv.getTimeResolution().isEnabled());
        
        String preselectedResolution = wv.getTimeResolutionSelectedValue();
        Assert.assertEquals(preselectedResolution, "15 seconds");
    }
    
    /**
     * Test that the historic mode is kept when we jump from ATC UI to WebView
     * and the time resolution combo box is enabled.
     * @throws Exception 
     */
    @Test
    public void testTimeResolutionEnabledInHistoricMode() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().turnOffLiveMode();
        
        WebElement wvLink = ui.getTopNavigationPanel().getAnyWebviewLinkElement();
        
        // Remember the URL for usage in further test methods
        wvHistUrl = wvLink.getAttribute("href"); 
        Map<String, Long> attr = UrlUtils.getMapOfParameterValuesFromWebViewUrl(wvHistUrl, true);
        Assert.assertNotEquals(attr.get("tr"), VW_TR_LIVE);
        
        wvLink.click();
        ui.switchToWebView();

        wv = new WebView(ui);
            
        Assert.assertTrue(wv.getTimeController().isEnabled());
        Assert.assertNotEquals("Live", wv.getTimeControllerSelectedValue());
        Assert.assertTrue(wv.getTimeResolution().isEnabled());
    }
    
    /**
     * Test all available predefined test ranges and test resolutions that are offered by the UI.
     * Click through both time controls in WebView. 
     * Verify that time resolutions are reasonable with respect to the size of the time range.
     * Also verify that the selected time resolution is properly stored in URL.  
     * @throws Exception 
     */
    @Test(dependsOnMethods="testTimeResolutionDisabledInLiveMode", groups = "failing")
    public void testTimeResolutionValuesOnTimeRangeChange() throws Exception {
        init();
                
        ui.getDriver().navigate().to(wvLiveUrl);
        
        wv = new WebView(ui);
        
        List<String> timeRangeOptions = wv.getTimeControllerValues();
        int timeRangeCount = timeRangeOptions.size();
        
        for (int i = 0; i < timeRangeCount; i++) {
            String val = timeRangeOptions.get(i);
            if (!WebView.TimeRange.LIVE.getLabel().equals(val) && !WebView.TimeRange.CUSTOM_RANGE.getLabel().equals(val)) {
                doTestOneTimeRangeOptionOnTimeRangeChange(i);
            }
        }
    }

    /**
     * Verify that the time resolution selected by default for a certain time range is reasonable (30-100 data points).
     * Further test all time resolutions available for the given time range index.
     * There should be at least two and not more than a thousand data points in any time range. 
     */
    private void doTestOneTimeRangeOptionOnTimeRangeChange(int index) {
        WebElement timeRangeElement = wv.getTimeControllerValueElements().get(index);
        String timeRangeValue = timeRangeElement.getText();
        Long timeRangeInMinutes = timeRanges.get(timeRangeValue);
        
        timeRangeElement.click();

        logger.info("Testing time range '{}'", timeRangeValue);
        
        String preselectedResolution = wv.getTimeResolutionSelectedValue();
        Long resolutionValueInMillis = timeResolutions.get(preselectedResolution);
        
        // Conversion: there are 60000 milliseconds in a minute
        Long ratio = timeRangeInMinutes * 60000 / resolutionValueInMillis;

        Assert.assertTrue(ratio >= 30, "The default time resolution for the time range '" + timeRangeValue + "' should have at least 30 data points and not " + ratio);
        Assert.assertTrue(ratio <= 100, "The default time resolution for the time range '" + timeRangeValue + "' should have at maximum 100 data points and not " + ratio);
                
        List<PageElement> resolutionOptions = wv.getTimeResolutionValueElements();
        int timeResolutionCount = resolutionOptions.size();
        
        for (int j = 0; j < timeResolutionCount; j++) {
            doTestOneTimeResolutionOptionOnTimeRangeChange(timeRangeValue, j);
        }
    }

    /**
     * Test that the time resolution is reasonable with respect to the time range and that the time resolution,
     * when selected, is reflected in the URL.
     * 
     * There should be at least 2 and not more than 1500 data points in any time range. 
     */
    private void doTestOneTimeResolutionOptionOnTimeRangeChange(String timeRangeValue, int index) {
        WebElement timeResolutionElement = wv.getTimeResolutionValueElements().get(index);
        String timeResolutionValue = timeResolutionElement.getText();
        
        Long timeRangeInMinutes = timeRanges.get(timeRangeValue);
        Long timeResolutionInMillis = timeResolutions.get(timeResolutionValue);

        logger.info("Testing time range '{}' and resolution '{}'", timeRangeValue,
            timeResolutionValue);
        
        // Conversion: there are 60000 milliseconds in a minute
        Long ratio = timeRangeInMinutes * 60000 / timeResolutionInMillis;
        
        Assert.assertTrue(ratio >= 2, "There are less than 2 data points for the time range '" + timeRangeValue + "' and the resolution '" + timeResolutionValue + "'");
        Assert.assertTrue(ratio < 1500, "There are more than 1500 data points for the time range '" + timeRangeValue + "' and the resolution '" + timeResolutionValue + "'");
        
        timeResolutionElement.click();
        
        Utils.sleep(100);
        
        String currUrl = ui.getDriver().getCurrentUrl();
        logger.info("URL: {}", currUrl);
        
        Map<String, Long> map = UrlUtils.getMapOfParameterValuesFromWebViewUrl(currUrl, true);
        Long timeResolutionValueFromUrl = map.get("re");
        
        Assert.assertEquals(timeResolutionValueFromUrl, timeResolutionInMillis, "For the time range '" + timeRangeValue 
            + "' the expected resolution is " + timeResolutionInMillis + " msec but the actual value is " + timeResolutionValueFromUrl + " msec");
    }
    
    /**
     * Test that a resolution value that is (manually) modified in the URL and that would lead to requesting too few or 
     * too many data points is capped to a reasonable value, i.e. not less than 2 and not more than 1500 data points. 
     * @throws Exception 
     */
    @Test(dependsOnMethods="testTimeResolutionEnabledInHistoricMode")
    public void testCapTheExcessiveResolutionValueInUrl() throws Exception {
        init();
        
        ui.getDriver().navigate().to(wvHistUrl);
        
        wv = new WebView(ui);
        
        List<String> timeRangeOptions = wv.getTimeControllerValues();
        int timeRangeCount = timeRangeOptions.size();
        
        for (int i = 0; i < timeRangeCount; i++) {
            String val = timeRangeOptions.get(i);
            if (!WebView.TimeRange.LIVE.getLabel().equals(val) && !WebView.TimeRange.CUSTOM_RANGE.getLabel().equals(val)) {
                doTestOneTimeRangeOptionCapResolutionValue(i);
            }
        }
    }

    /**
     * Replace the "re" URL parameter, which represents time resolution, with a too small and 
     * too big values and verify that they are capped by WebView to a reasonable value.
     */
    private void doTestOneTimeRangeOptionCapResolutionValue(int index) throws Exception {
        String currUrl = ui.getDriver().getCurrentUrl();

        String modUrl1 = UrlUtils.getWebViewUrlWithParamValue(currUrl, "re", 1L);
        doTestOneUrlCapResolutionValue(modUrl1);
        
        String modUrl2 = UrlUtils.getWebViewUrlWithParamValue(currUrl, "re", 100000000L);
        doTestOneUrlCapResolutionValue(modUrl2);
    }
    
    private void doTestOneUrlCapResolutionValue(String url) throws Exception {
        ui.getDriver().navigate().to(url);
        logger.info("URL: {}", url);
        
        Map<String, Long> params = UrlUtils.getMapOfParameterValuesFromWebViewUrl(url, true);
        Long startTime = params.get("st");
        Long endTime = params.get("et");
        long timeRangeInMillis = endTime - startTime;
        
        String timeResolutionValue = wv.getTimeResolutionSelectedValue();
        Long resolutionValueInMillis = timeResolutions.get(timeResolutionValue);

        logger.info("Time range value is {} and the capped time resolution is '{}'",
            timeRangeInMillis, timeResolutionValue);
        
        Long ratio = timeRangeInMillis / resolutionValueInMillis;
        
        Assert.assertTrue(ratio >= 2, "There would be less than 2 data points requested for the time range " + timeRangeInMillis + " and the resolution '" + timeResolutionValue + "'");
        Assert.assertTrue(ratio < 1500, "There would be more than 1500 data points requested for the time range " + timeRangeInMillis + " and the resolution '" + timeResolutionValue + "'");
    }
}
