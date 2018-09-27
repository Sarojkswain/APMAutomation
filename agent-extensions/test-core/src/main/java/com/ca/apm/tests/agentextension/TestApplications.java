/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * 
 * DATE: 07/18/2017
 * AUTHOR: MARSA22/SAI KUMAR MAROJU
 */

package com.ca.apm.tests.agentextension;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestApplications
{
    public FirefoxDriver        fd;
    
    private static final int PAGE_LOAD_TIMEOUT = 5;

    private static final Logger LOGGER = LoggerFactory
                                               .getLogger(TestApplications.class);

    public void SpringComponentApp(String url)
    {

        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);
        LOGGER.info("url successfully opened");

        waitExplicitPresenceOfElementByXPath("//button[@type='submit']");
        LOGGER.info("waitstatement");

        fd.findElementByXPath("//input[@id='firstName']").sendKeys("test");

        fd.findElementByXPath("//input[@id='lastName']").sendKeys("test");

        fd.findElementByXPath("//input[@id='email']")
                .sendKeys("test@gmail.com");

        fd.findElementByXPath("//input[@id='confirmEmail']")
                .sendKeys("test@gmail.com");

        fd.findElementByXPath("//input[@id='password']").sendKeys("testtest");

        Select se = new Select(fd.findElementByXPath("//select[@id='month']"));
        se.selectByVisibleText("May");

        new Select(fd.findElementByXPath("//select[@id='day']"))
                .selectByVisibleText("2");
        new Select(fd.findElementByXPath("//select[@id='year']"))
                .selectByVisibleText("2010");

        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Sign Out')]");

        fd.close();

    }

    public void SpringControllerApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//button[@type='submit']");

        fd.close();
    }

    public void SpringServiceApp(String url)
    {
        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstname");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastname");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hyd");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hyd");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345678");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");

        fd.close();
    }

    public void SpringRepositoryApp(String url)
    {
        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstnamere");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastnamere");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345");
        fd.findElementByXPath("//button[@type='submit']").click();
        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");

        fd.close();
    }

    public void SpringValidatorApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstnamere");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastnamere");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");
        fd.findElementByXPath("//a[contains(text(),'Add New Pet')]").click();

        waitExplicitPresenceOfElementByXPath("//input[@id='name']");
        fd.findElementByXPath("//input[@id='name']").sendKeys("ownerfirstdog");
        fd.findElementByXPath("//input[@id='birthDate']")
                .sendKeys("2017/01/02");
        fd.findElementByXPath("//select[@id='type']");

        Select se1 = new Select(fd.findElementByXPath("//select[@id='type']"));
        se1.selectByVisibleText("dog");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Add New Pet')]");

        fd.close();
    }

    public void SpringAspectApp(String url)
    {
        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstname");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastname");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hyd");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hyd");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345678");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");

        fd.close();
    }

    public void SpringRequestMappingApp(String url)
    {
        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstnamere");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastnamere");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");
        fd.findElementByXPath("//a[contains(text(),'Add New Pet')]").click();

        waitExplicitPresenceOfElementByXPath("//input[@id='name']");

        fd.close();
    }

    public void SpringRestWebservicesApp(String url)
    {
        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        LOGGER.info("url successfully opened");
        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        LOGGER.info("waitstatement");

        fd.findElementByXPath("//input[@id='firstName']").sendKeys("test1");

        fd.findElementByXPath("//input[@id='lastName']").sendKeys("test1");

        fd.findElementByXPath("//input[@id='email']")
                .sendKeys("test1@gmail.com");

        fd.findElementByXPath("//input[@id='confirmEmail']")
                .sendKeys("test1@gmail.com");

        fd.findElementByXPath("//input[@id='password']").sendKeys("testtest");

        Select se = new Select(fd.findElementByXPath("//select[@id='month']"));
        se.selectByVisibleText("May");

        new Select(fd.findElementByXPath("//select[@id='day']"))
                .selectByVisibleText("2");
        new Select(fd.findElementByXPath("//select[@id='year']"))
                .selectByVisibleText("2010");

        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Sign Out')]");
        fd.findElementByXPath("//a[contains(text(),'Settings')]").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Connect to Twitter')]");
        fd.findElementByXPath("//a[contains(text(),'Connect to Twitter')]")
                .click();
        
        waitExplicitPresenceOfElementByXPath("//button[@type='submit']");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//input[@id='cancel']");

        fd.close();
    }

    public void SpringTransactionManagementApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@id='firstName']");
        fd.findElementByXPath("//input[@id='firstName']")
                .sendKeys("ownerfirstnamere");
        fd.findElementByXPath("//input[@id='lastName']")
                .sendKeys("ownerlastnamere");
        fd.findElementByXPath("//input[@id='address']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='city']").sendKeys("hydre");
        fd.findElementByXPath("//input[@id='telephone']").sendKeys("12345");
        fd.findElementByXPath("//button[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//a[contains(text(),'Edit Owner')]");

        fd.close();
    }

    public void SpringRemotingRMIApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@name='j_username']");
        fd.findElementByXPath("//input[@name='j_username']").sendKeys("admin");
        fd.findElementByXPath("//input[@name='j_password']").sendKeys("admin");
        fd.findElementByXPath("//input[@type='submit']").click();

        waitExplicitPresenceOfElementByXPath("//h2[contains(text(),'Hello World')]");

        fd.close();
    }

    public void SpringRemotingHessainApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@name='number']");
        fd.findElementByXPath("//input[@type='text']").sendKeys("4");
        fd.findElementByXPath("//input[@type='submit']").click();

        fd.close();
    }

    public void SpringRemotinghttpinvokerApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);
        waitExplicitPresenceOfElementByXPath("//input[@name='number']");

        fd.close();
    }

    public void SpringJasperReportsApp(String url)
    {
        fd = new FirefoxDriver();

        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//div/div[2]/div[4]/div/div/div/div[6]");

        fd.close();
    }

    public void accessWebservicetestapp(String url)
    {

        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@value='Save']");
        fd.findElementById("id").sendKeys("12345678");
        fd.findElementById("name").sendKeys("zb bz");
        fd.findElementById("email").sendKeys("zb@dummy.com");
        fd.findElementByXPath("//input[@value='Save']").click();

        fd.close();

    }

    public void JspCrudApp(String url)
    {

        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@value='Submit']");

        fd.findElementByXPath("//input[@name='userid']").sendKeys("1234");
        fd.findElementByXPath("//input[@name='firstName']").sendKeys("John");
        fd.findElementByXPath("//input[@name='lastName']").sendKeys("Cena");

        fd.findElementByXPath("//input[@value='Submit']").click();
        waitExplicitPresenceOfElementByLinkText("Add User");
        fd.findElementByLinkText("Add User").click();

        waitExplicitPresenceOfElementByLinkText("View-All-Records");
        fd.findElementByLinkText("View-All-Records").click();

        waitExplicitPresenceOfElementByLinkText("Add User");
        fd.findElementByLinkText("Add User").click();

        waitExplicitPresenceOfElementByLinkText("View-All-Records");
        fd.findElementByLinkText("View-All-Records").click();

        fd.close();

    }

    public void JspCrudAppParamLength(String url)
    {

        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);

        waitExplicitPresenceOfElementByXPath("//input[@value='Submit']");

        fd.findElementByXPath("//input[@name='userid']").sendKeys("12345");
        fd.findElementByXPath("//input[@name='firstName']")
                .sendKeys("JohnMikeGeorge");
        fd.findElementByXPath("//input[@name='lastName']")
                .sendKeys("CenaAlexander");

        fd.findElementByXPath("//input[@value='Submit']").click();

        waitExplicitPresenceOfElementByLinkText("Add User");
        fd.findElementByLinkText("Add User").click();

        waitExplicitPresenceOfElementByLinkText("View-All-Records");
        fd.findElementByLinkText("View-All-Records").click();

        waitExplicitPresenceOfElementByLinkText("Add User");
        fd.findElementByLinkText("Add User").click();

        waitExplicitPresenceOfElementByLinkText("View-All-Records");
        fd.findElementByLinkText("View-All-Records").click();

        fd.close();

    }

    public void KonakartApp(String url)
    {

        fd = new FirefoxDriver();
        fd.get(url);
        pageLoadingWait(PAGE_LOAD_TIMEOUT);
        
        fd.findElementByLinkText("Games").click();
        fd.findElementByLinkText("DVD Movies").click();
        fd.findElementByLinkText("Computer Peripherals").click();
        fd.findElementByLinkText("Electronics").click();
        fd.findElementByLinkText("Software").click();
        fd.findElementByLinkText("Games").click();

        fd.close();

    }

    public void waitExplicitPresenceOfElementByXPath(String element)
    {
        int count = 1;

        while (count <= 20)
        {
            try
            {
                fd.findElementByXPath(element);
                LOGGER.info("Element found inside try ");
                break;
                
            } catch (Exception e)
            {
            	LOGGER.info("Element not found try again ");
            }
            count++;

        }

    }

    public void waitExplicitPresenceOfElementByLinkText(String element)
    {
        int count = 1;

        while (count <= 20)
        {
            try
            {
                fd.findElementByLinkText(element);
                LOGGER.info("Element found inside try ");
                break;
                
            } catch (Exception e)
            {
            	LOGGER.info("Element not found try again ");
            }
            count++;

        }

    }
    
    public void pageLoadingWait(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
            LOGGER.info("Page load wait completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
