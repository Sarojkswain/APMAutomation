/*
 * Author: Abhishek Sinha(sinab10@ca.com)
 * 
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
package com.ca.apm.saas.pagefactory;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

public class RegistrationPage {
	
	WebDriver driver;	
	public static String baseURL = "http://nansw02-u187776:8081/signup-dev/#/register";
	public String newInstanceURL = null;
	
	@FindBy(name="firstName")
    WebElement tenantFirstName;
	
	@FindBy(name="lastName")
    WebElement tenantLastName;
	
	@FindBy(name="userName")
    WebElement tenantEmailAddress;
	
	@FindBy(id="password")
    WebElement tenantPassword;
	
	@FindBy(id="signUpSubmitBtn")
    WebElement registrationFormSubmission;
	
	
	
	
	/*@FindBy(id="company")
    WebElement tenantCompany;
	
	@FindBy(id="jobgroup")
    WebElement tenantJobType;
	
	@FindBy(id="phone")
    WebElement tenantPhone;
	
	@FindBy(id="country")
    WebElement tenantCountry;
	
	
	@FindBy(name="confirm")
    WebElement tenantPasswordConfirmation;
	
	@FindBy(id="subscribe")
    WebElement tenantCompanySubscription;*/
	

	
	public RegistrationPage(WebDriver driver){
        this.driver = driver;
        
        //This initElements method will create all WebElements
        PageFactory.initElements(driver, this);
    }
	
	private RegistrationPage setFirstName(String firstNameInput){
		tenantFirstName.sendKeys(firstNameInput);
		return this;
	}
	
	private RegistrationPage setLastName(String lastNameInput){
		tenantLastName.sendKeys(lastNameInput);
		return this;
	}
	
	private RegistrationPage setEmailAddress(String emailAddressInput){
		tenantEmailAddress.sendKeys(emailAddressInput);
		return this;
	}
	
	private RegistrationPage setPassword(String passwordInput){
		tenantPassword.sendKeys(passwordInput);
		return this;
	}
	
	/*
	private RegistrationPage setCompany(String companyInput){
		tenantCompany.sendKeys(companyInput);
		return this;
	}
	
	private RegistrationPage selectJobGroup(String jobGroupInput){
		new Select(tenantJobType).selectByValue(jobGroupInput);
		return this;
	}
	
	private RegistrationPage setPhone(String phoneInput){
		tenantPhone.sendKeys(phoneInput);
		return this;
	}
	
	private RegistrationPage selectCountry(String countryInput){
		new Select(tenantCountry).selectByVisibleText(countryInput);
		return this;
	}	

	
	private RegistrationPage setPasswordConfirmation(String passwordConfirmationInput){
		tenantPasswordConfirmation.sendKeys(passwordConfirmationInput);
		return this;
	}
	
	private RegistrationPage setSubscription(boolean subscriptionInput){
		if(subscriptionInput == true)tenantCompanySubscription.click();
		return this;
	}*/
	
	private boolean isElementPresent(By by) {
	    try {
	      driver.findElement(by);
	      return true;
	    } catch (NoSuchElementException e) {
	      return false;
	    }
	  }
	
	public void closePopUp() throws InterruptedException{
    	if(isElementPresent(By.className("walkme-custom-balloon-button-text"))){
			driver.findElement(By.className("walkme-custom-balloon-button-text")).click();
			Thread.sleep(5000);
		}
    }
	
	// Resets the URL for registration page
	public void resetPageURL(String url){
		this.baseURL = url;
	}
	
	// Fetches the registration page in browser
	public void fetchPage() throws InterruptedException{
		driver.get(baseURL);
		Thread.sleep(10000);
		// Occasionally, a survey pop-up appears that need to be closed.
		if(isElementPresent(By.className("smcx-modal-close"))){
			driver.findElement(By.className("smcx-modal-close")).click();
			Thread.sleep(10000);
		}
		/*WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.elementToBeClickable(By.name("Submit")));*/
		Assert.assertTrue(isElementPresent(By.id("signUpSubmitBtn")),"Registration page didn't load");
	}
	
	// Fills the registration form with all values
	public void fillFormValues(String firstName, String lastName, String emailAddress, String password){
		this.setFirstName(firstName)
			.setLastName(lastName)
			.setEmailAddress(emailAddress)
			//.setCompany(company)
			//.selectJobGroup(jobType)
			//.setPhone(phone)
			//.selectCountry(country)
			.setPassword(password);
			//.setPasswordConfirmation(passwordConfirmation)
			//.setSubscription(subscription);
	}
	
	// Submits the form
	public void submitForm() throws InterruptedException{
		//registrationFormSubmission.submit();
		registrationFormSubmission.click();
		Thread.sleep(10000);
		//Assert.assertTrue(isElementPresent(By.id("settings-view-link")),"ATC homepage didn't load");
	}
}
