/**
 * 
 */
package com.ca.apm.test.atc;

import static com.ca.apm.test.atc.HelpLinksConstants.HELP_LINK_PATTERN;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

/**
 * Base class for help links tests on langind and app map pages.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class HelpLinksTest extends UITest {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelpLinksTest.class);
	
	@Override
    protected UI createLocalUI(DesiredCapabilities dc, String localDriverPath) {
    	dc.setJavascriptEnabled(true);
		return UI.getLocal(dc, localDriverPath);
    }

	@Override
    protected UI createRemoteUI(DesiredCapabilities dc, String remoteDriverUrl) throws Exception {
		dc.setJavascriptEnabled(true);
    	return UI.getRemote(dc, remoteDriverUrl);
    }

	public static void checkHelpLinkElementBy(WebDriver driver, By by, String pattern, String hid, String space, String lang) {
		WebElement notebookHelpLink = driver.findElement(by);
        String href = notebookHelpLink.getAttribute("href");
        checkHelpDocLink(pattern, href, hid, space, lang);
	}

	public static void checkHelpLinkElement(WebDriver driver, String xpath, String hid, String space, String lang) {
		WebElement notebookHelpLink = driver.findElement(By.xpath(xpath));
        String href = notebookHelpLink.getAttribute("href");
        checkHelpDocLink(href, hid, space, lang);
	}

	public static void checkHelpDocLink(String href, String expectedHID, String expectedSpace, String expectedLanguage) {
		checkHelpDocLink(HELP_LINK_PATTERN, href, expectedHID, expectedSpace, expectedLanguage);
	}
	
	public static void checkHelpDocLink(String regexPattern, String href, String expectedHID, String expectedSpace, String expectedLanguage) {
        Assert.assertNotNull(href);
        
        //https://docops.ca.com/rest/ca/product/latest/topic?hid=HID_?&space=APMDEVOPS???&format=rendered&language=??
        LOGGER.info("Pattern: {}", regexPattern);
        LOGGER.info("Retrieved href: {}", href);
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(href);
        
        Assert.assertTrue(matcher.matches());
        
        String hid = matcher.group(1);
        
        LOGGER.info("Comparing retrieved hid='{}' with expected hid='{}'", hid, expectedHID);
        
        Assert.assertNotNull(hid);
        Assert.assertEquals(hid, expectedHID);
        
        String space = matcher.group(2);
        
        LOGGER.info("Comparing retrieved space='{}' with expected space='{}'", space, expectedSpace);
        
        Assert.assertNotNull(space);
        Assert.assertEquals(space, expectedSpace);
        
        
        String langCode = matcher.group(3);
        
        LOGGER.info("Comparing retrieved language='{}' with expected language='{}'", langCode, expectedLanguage);
        Assert.assertNotNull(langCode);
        Assert.assertEquals(langCode, expectedLanguage);
	}
	
    protected static void setUpDocOpsReleaseOverride(RemoteWebDriver wd, String releaseVersion) {
        LOGGER.info("Setting DOCOPS_RELEASE_STRING to {}", releaseVersion);
        Utils.waitForAngularConfiguration(wd, HelpLinksConstants.DOCOPS_RELEASE_STRING, 30);
        wd.executeScript(String.format("angular.element(document.body).injector().get('ConfigurationService').%s='%s'", HelpLinksConstants.DOCOPS_RELEASE_STRING, releaseVersion));
    }

	protected static void setUpAppRelease(RemoteWebDriver wd, String releaseVersion) {
		LOGGER.info("Setting RELEASE_STRING to {}", releaseVersion);
		Utils.waitForAngularConfiguration(wd, HelpLinksConstants.RELEASE_STRING, 30);
		wd.executeScript(String.format("angular.element(document.body).injector().get('ConfigurationService').%s='%s'", HelpLinksConstants.RELEASE_STRING, releaseVersion));
	}
	
	protected static void setUpDevSpaceSuffix(RemoteWebDriver wd, String space) {
		LOGGER.info("Setting DEFAULT_DEVELOPMENT_RELEASE_STRING to {}", space);
		wd.executeScript(String.format("angular.element(document.body).injector().get('ConfigurationService').DEFAULT_DEVELOPMENT_RELEASE_STRING='%s'", 
				space));
	}

	protected static void setUpLanguage(RemoteWebDriver wd, String langCode) {
		LOGGER.info("Setting APM_USER_LANGUAGE to '{}'", langCode);
		wd.executeScript(String.format("window.APM_USER_LANGUAGE='%s'", langCode));
	}

	protected static RemoteWebDriver createWebDriver() throws MalformedURLException {
        DesiredCapabilities dc = DesiredCapabilities.chrome();
        dc.setJavascriptEnabled(true);
        
        RemoteWebDriver wd = new RemoteWebDriver(new URL("http://localhost:9515"), dc);
        wd.manage().window().maximize();
        wd.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        return wd;
	}

}
