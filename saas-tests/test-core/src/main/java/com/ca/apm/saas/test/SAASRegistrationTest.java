/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.saas.test;

import static org.testng.Assert.assertTrue;

import org.apache.commons.lang.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Sample test class to demonstrate use of test-bed using core roles
 *
 * @author TAS (tas@ca.com)
 * @since 1.0
 */
public class SAASRegistrationTest extends SaaSBaseTest {

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "pmfkey")
    @Test
    public void dummyTest() throws Exception {
    	RemoteWebDriver driver;
    	System.out.println("get current url -------------------" +ui.getCurrentUrl());
    	driver=ui.getDriver();
    	String baseUrl = "https://www.ca.com/us/trials/ca-application-performance-management.register.html";
    	driver.get(baseUrl);
    	String emailid;		
		emailid = RandomStringUtils.randomAlphabetic(6);
		Thread.sleep(20000);
		driver.findElement(By.id("firstname")).sendKeys("Ravi Kanth");
		driver.findElement(By.id("lastname")).sendKeys("Bandari");
		driver.findElement(By.id("email")).sendKeys(emailid + "@ca.com");
		driver.findElement(By.id("company")).sendKeys("ca technologies");
		new Select(driver.findElement(By.id("jobgroup")))
				.selectByValue("System Programmer");
		driver.findElement(By.id("phone")).sendKeys("4084081234");
		new Select(driver.findElement(By.id("country")))
				.selectByVisibleText("United States");
		// country.selectByVisibleText("United States");
		driver.findElement(By.name("password1")).sendKeys("interOP@123");
		driver.findElement(By.name("confirm")).sendKeys("interOP@123");
		WebElement checkbox = driver.findElement(By.id("subscribe"));
		checkbox.click();
		Thread.sleep(5000);
		assertTrue(true);
    }
}
