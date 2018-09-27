/*
 * Copyright (c) 2017 CA. All rights reserved.
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
 */
package com.ca.tas.dxc.test;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ca.apm.test.atc.common.Browser;
import com.ca.tas.test.TasTestNgTest;

/**
 * Test class to click some BRTMTestApp via Chrome
 *
 * @author Zdenek Korcak (korzd01@ca.com)
 */

public class DXCTest extends TasTestNgTest {
    
    public static void main(String[] args) {
        String baseUrl = args[0];
        String localHrefs[] = {
                    "Basic Page.html", "Long content generator.html",
                    "I18N.html", "index-withouthead.html",
                    "Char_chinese-big5.html", "HTTP304.html" };

        try {
            Browser browser = new Browser();
            RemoteWebDriver driver = browser.openDefault();
            
            makeAjaxCalls(driver, baseUrl);
            driver.get(baseUrl);
            try {
                for (String href : localHrefs) {
                    try {
                        By selector = By.cssSelector("a[href='" + href + "']");
                        WebElement webElement = driver.findElement(selector);
                        webElement.click();
                        Thread.sleep(5000);
                        driver.navigate().back();
                        Thread.sleep(5000);
                    } catch (org.openqa.selenium.NoSuchElementException ex) {
                        System.out.println("Element not found: " + href);
                    }
                }
            } catch (Exception ex) {
                browser.takeScreenshot("DXCTest", "brtmtestapp", "FAILURE");
            } finally {
                Thread.sleep(5000);
                browser.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void makeAjaxCalls(RemoteWebDriver driver, String baseUrl) {
        String colorHrefs[] = { "", "red", "green", "blue", "change" };
        
        try {
            for (String href : colorHrefs) {
                driver.get(baseUrl + "/spa/#/" + href);
                
                List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
                for (WebElement webElement : buttons) {
                    webElement.click();
                    Thread.sleep(100);
                }
            }
            Thread.sleep(6000);
        } catch (Exception ex) {
            // swallow it
        }
    }

}
