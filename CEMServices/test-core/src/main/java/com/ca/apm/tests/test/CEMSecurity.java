package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.WebDriver;

public class CEMSecurity extends JBaseTest
{
	public CEMSecurity(){
      //empty constructor
	}
    
	public CEMSecurity(WebDriver driver){
		this.driver = driver;
	}
	public void goToFIPSSettingsSetup() throws Exception
	{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.security"),getORPropValue("cem.security.fipssettings"));
	}

	public void goToPrivateParametersTab() throws Exception
	{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.security"),getORPropValue("cem.security.privateparam"));
	}
}
