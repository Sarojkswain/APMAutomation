package com.ca.apm.saas.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.AxaPage;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/*
 * @author Liddy Hsieh
 */
public class SaasAxaSanityTest extends SaaSBaseTest {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());    
    private AxaPage objAxa;   

    @BeforeMethod(alwaysRun = true)
    public void setup () {
        product = "AXA";
        objAxa = new AxaPage(getDriver());
    }
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")
    @Test(priority = 0)
    public void testAxaLoginSuccessful() throws InterruptedException, IOException {
        attemptLogin(getDriver());
        Assert.assertEquals(objAxa.getLogoutText().getText(), "Logout");
        objAxa.logout();
    }

    @Test(priority = 1)
    public void testOverviewSuccessful() throws InterruptedException, IOException {
        // Overview page
        // AxaPage objAxa = new AxaPage(getDriver());
        attemptLogin(getDriver());
        objAxa.clickOverviewLink();
        logger.info("********** after click Overview Link **********");
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getAppRankingTxt()),
            "Overview page doesn't shows 'App Ranking Text");
        objAxa.logout();
    }

    @Test(priority = 2)
    public void testPerformanceSuccessful() throws InterruptedException, IOException {
        // Performance page
        // AxaPage objAxa = new AxaPage(getDriver());
        attemptLogin(getDriver());
        objAxa.clickPerformanceLink();
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getAppPerformanceLink()),
            "App Performance link doesn't show up in Performance page");
        objAxa.logout();
    }

    @Test(priority = 3)
    public void testCrachesSuccessful() throws InterruptedException, IOException {
        // Crashes & Errors page
        // AxaPage objAxa = new AxaPage(getDriver());
        attemptLogin(getDriver());
        objAxa.clickCrachesLink();
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getAppCrashesLink()),
            "App Crashes link doesn't show up in Crashes & Errors page");
        objAxa.logout();
    }

    @Test(priority = 4)
    public void testUsageSuccessful() throws InterruptedException, IOException {
        // Usage page
        attemptLogin(getDriver());
        objAxa.clickUsageLink();
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getUsersTxt()),
            "Users text doesn't show up in Usage page");
        objAxa.logout();
    }

    @Test(priority = 5)
    public void testSessionsSuccessful() throws InterruptedException, IOException {
        // Sessions page
        attemptLogin(getDriver());
        objAxa.clickSessionsLink();
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getSessionsTxt()),
            "Sessions text doesn't show up in Sessions page");
        objAxa.logout();
    }
    
    @Test(priority = 6)
    public void testAxaDataStudioSuccessful() throws InterruptedException, IOException {
        // Data Studio
        attemptLogin(getDriver());
        objAxa.clickDataStudioLink();
        objAxa.checkDefaultDashboardInDataStudio();
        Assert.assertTrue(objAxa.isElementPresent(getDriver(), objAxa.getSessionOverviewPanelHeader()),
                "There is no Session Overview panel in the default Dashboard in AXA");
        objAxa.logout();
    }
   
    private RemoteWebDriver getDriver() {
        Assert.assertNotNull(ui, "Unable to get selenium driver as 'ui' object wasn't instantiated.");
        RemoteWebDriver driver = ui.getDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }
}
