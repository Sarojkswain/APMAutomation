package com.ca.apm.saas.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ca.apm.saas.pagefactory.DownloadAgentPage;
import com.ca.apm.saas.pagefactory.HomePage;
import com.ca.apm.saas.pagefactory.LoginPage;
import com.ca.apm.saas.test.utils.TestDataProviders;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/*
 * @author Liddy Hsieh
 */
public class PageLinksTest extends SaaSBaseTest
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    LoginPage objLogin;
    HomePage objHome;
    DownloadAgentPage objAgent;
    
    @BeforeMethod(alwaysRun = true)
    public void initPages() throws InterruptedException, IOException {
        objLogin = new LoginPage(getDriver());
        objHome = new HomePage(getDriver());
        objAgent = new DownloadAgentPage(getDriver());  
    }
    
    public RemoteWebDriver getDriver() {
        RemoteWebDriver driver = ui.getDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(dataProvider = "helpLinkTestParams", dataProviderClass = TestDataProviders.class)
    public void testHelpLink_APM(String linkName, String content) throws Exception{
        attemptLogin(getDriver());
        objHome.clickLink(linkName);
        disableDocsAuth();
        objHome.clickHelp_Link();
        objHome.goToNewTab();
        Assert.assertTrue(objHome.isHelpDocPresent(content), "APM Help: Help doc doesn't show the required content - " + content);
        objHome.closeTabAndBackToMain();
    }
    
     // Help tests all passed except 3 defects: 
     // DE304693: Agent View Help link doesn't point to View Agent Status and Manage Agent Cards
     // DE304694 : Notifications Help link has No topics found  
     // DE304955: Attributes View Help link doesn't point to Attributes setting content  
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(dataProvider = "helpIconTestParams", dataProviderClass = TestDataProviders.class)
    public void testHelpIcon_APM(String linkName, String content) throws Exception{
        attemptLogin(getDriver());
        objHome.clickLink(linkName);
        disableDocsAuth();
        objHome.clickHelp_Icon();
        objHome.goToNewTab();
        Assert.assertTrue(objHome.isHelpDocPresent(content), "APM Help: Help doc doesn't show the required content - " + content);
        objHome.closeTabAndBackToMain();
    }
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(groups = {"smoke"})
    public void testMarketplaceLink() throws InterruptedException,IOException {
        attemptLogin(getDriver());
        // verify Market Place link will open a new tab & page title = CA Marketplace, two ways and both work!
        
        objAgent.clickDownloadAgent();
        //objAgent.verifyOpenedTabTitle(objAgent.getWebElementVisitCAMarketplaceLink(), "CA Marketplace");
        
        objAgent.getWebElementVisitCAMarketplaceLink().click();
        objHome.goToNewTab();
        logger.info("Expecting NewTab page contains 'Marketplace' in thier drop-title and actual valuse is - {} - ", objAgent.getWebElementVisitCAMarketplacePage().getText());
        Assert.assertTrue(objAgent.isElementPresent(getDriver(), objAgent.getWebElementVisitCAMarketplacePage()), "New page doesn't contains Marketplace in thier drop-title");
        objHome.closeTabAndBackToMain();
        
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(dataProvider = "atcLinkTestParams", dataProviderClass = TestDataProviders.class)
    public void testATCLinks(String linkName, String expectedView) throws InterruptedException,IOException {
        attemptLogin(getDriver());
        objHome.clickLink(linkName);
        logger.info("Clicked {} ");
        Assert.assertTrue(objHome.isViewPresent(expectedView), "'" + expectedView + "' button does NOT show ... ");
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(dataProvider = "atcSettingTestParams", dataProviderClass = TestDataProviders.class)
    public void testATCSettings(String linkName, String expectedSetting) throws InterruptedException,IOException {
        attemptLogin(getDriver());
        objHome.clickLink(linkName);
        logger.info("Clicked {} ");
        Assert.assertTrue(objHome.isSettingPresent(expectedSetting), "'" + expectedSetting + "' button does NOT show ... ");
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test(dataProvider = "agentInstructionParams", dataProviderClass = TestDataProviders.class)
    public void testDownloadAgentInstruction(String agentType, String agentSubType, String expectedInstruction) throws InterruptedException,IOException {
        attemptLogin(getDriver());
        objAgent.gotoAgentDownloadPage();
        if (objAgent.downloadAgentInstruction(agentType, agentSubType)) {
            Assert.assertTrue(objHome.isInstructionPresent(expectedInstruction), "Expected Instruction - " + expectedInstruction + 
                              " Not found for Agent type: " + agentType + " sub type: " + agentSubType );
        }
    }    

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")  
    @Test
    public void testCardList() throws InterruptedException, IOException {
        attemptLogin(getDriver());
        
        // click top right Experience Drop-down img
        objHome.clickExperienceDropDown();        
        // click Demo Applications link and check it's cards
        objHome.clickDemoApplications();
        List<String> demoCards = objHome.getExperienceCards();
        SoftAssert saDemo = new SoftAssert();
            saDemo.assertTrue(demoCards.contains("Demo Applications"), "'Demo Applications' card NOT showing");
            saDemo.assertAll();
        // check sub-cards of Demo Applications card
        objHome.clickCard("Demo Applications");
        List<String> demoSubCards = objHome.getExperienceCards();
        Assert.assertTrue(demoSubCards.contains("TradingService"), "'TradingService' card NOT showing");
        Assert.assertTrue(demoSubCards.contains("ReportingService"), "'ReportingService' card NOT showing");
           
        // click top right Experience Drop-down img
        objHome.clickExperienceDropDown();
        // click Your Applications link and check it's cards
        objHome.clickYourApplications();
        List<String> yourCards = objHome.getExperienceCards();
        SoftAssert saYou = new SoftAssert();
            saYou.assertTrue(yourCards.contains("Your Applications"), "'Your Applications' card NOT showing");
            saYou.assertAll();
        // check sub-cards of Your Applications card
        objHome.clickCard("Your Applications");
        List<String> yourSubCards = objHome.getExperienceCards();
        Assert.assertTrue(yourSubCards.contains("thieves"), "'thieve' card NOT showing");

        // click top right Experience Drop-down img            
        objHome.clickExperienceDropDown();
        // click All My Universes link and check it's cards
        objHome.clickAllMyUniverses();
        List<String> allCards2 = objHome.getExperienceCards();
        SoftAssert saAll2 = new SoftAssert();
            saAll2.assertTrue(allCards2.contains("Demo Applications"), "'Demo Applications' card NOT showing");
            saAll2.assertTrue(allCards2.contains("Your Applications"), "'Your Applications' card NOT showing");
            saAll2.assertAll();
        // check sub-cards of Demo Applications card
        objHome.clickCard("Demo Applications");
        List<String> allDemoSubCards = objHome.getExperienceCards();
        Assert.assertTrue(allDemoSubCards.contains("TradingService"), "'TradingService' card NOT showing");
        Assert.assertTrue(allDemoSubCards.contains("ReportingService"), "'ReportingService' card NOT showing");

        // click top right Experience Drop-down img            
        objHome.clickExperienceDropDown();
        // click All My Universes link and check it's cards
        objHome.clickAllMyUniverses();
        List<String> allCards = objHome.getExperienceCards();
        SoftAssert saAll = new SoftAssert();
            saAll.assertTrue(allCards.contains("Demo Applications"), "'Demo Applications' card NOT showing");
            saAll.assertTrue(allCards.contains("Your Applications"), "'Your Applications' card NOT showing");
            saAll.assertAll();
        // check sub-cards of Your Applications card
        objHome.clickCard("Your Applications");
        List<String> allYourSubCards = objHome.getExperienceCards();
        Assert.assertTrue(allYourSubCards.contains("thieves"), "'thieve' card NOT showing");

    }
}
