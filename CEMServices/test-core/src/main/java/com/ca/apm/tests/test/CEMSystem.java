package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.WebDriver;

public class CEMSystem extends JBaseTest {
	
	public CEMSystem(){
        //empty constructor
    }
	
    public CEMSystem(WebDriver driver){
        this.driver = driver;
    }
	public void goToEmailSetup() throws Exception{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.system"),getORPropValue("cem.system.emailsettings"));
	}
	
	public void goToEventsTab() throws Exception{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.system"),getORPropValue("cem.system.events"));
	}
	
	public void setupEmail(String smtpServer, String port, Boolean auth, String username, String password, String subject, String message, String footer) throws Exception{
		if (!WebdriverWrapper.isObjectPresent(driver,getORPropValue("cem.system.email.smtphostedit"))){
			goToEmailSetup();
		}
		if (!smtpServer.isEmpty()){
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.smtphostedit"), smtpServer);
		}
		if (!port.isEmpty()){
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.portedit"), port);
		}
		if (auth){
		    WebdriverWrapper.selectCheckBox(driver,getORPropValue("cem.system.email.authcheck"));
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.usernameedit"), username);
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.passwordedit"), password);
		}else{
		    WebdriverWrapper.deselectCheckBox(driver, getORPropValue("cem.system.email.authcheck"));
		}
		if (!subject.isEmpty()){
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.subjectedit"), subject);
		}
		if (!message.isEmpty()){
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.messageedit"), message);
		}
		if (!footer.isEmpty()){
		    WebdriverWrapper.inputText(driver,getORPropValue("cem.system.email.footeredit"), footer);
		}
		WebdriverWrapper.click(driver,getORPropValue("button.edit.save"));
	}
}

