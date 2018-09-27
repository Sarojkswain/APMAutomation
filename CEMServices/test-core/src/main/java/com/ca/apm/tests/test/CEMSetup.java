package com.ca.apm.tests.test;

import com.ca.apm.tests.cem.common.SetupMonitorHelper;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.WebDriver;

public class CEMSetup extends BaseSharedObject{
	
	SetupMonitorHelper setupMonitor=new SetupMonitorHelper(m_cemServices);
	
	public CEMSetup(){
	    //empty constructor
	}
	
	public CEMSetup(WebDriver driver){
	    this.driver = driver;
	}
	
	/**
	 * Disable all plugins.
	 * @throws Exception 
	 */
	public void disablePlugin() {
	    try{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.plugins"),getORPropValue("setup.pluginAnalyzerLink"));
		if(!isGridPresent("command")){
			return;
		}else if (!WebdriverWrapper.getElementText(driver,getORPropValue("setup.plugins.pluginsTable")).contains("Enabled")){
			return;
		}
		//disable all
		WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
		WebdriverWrapper.clickDisable(driver, getORPropValue("button.disable"));
		WebdriverWrapper.selectPopUp(driver,"accept");
	    }catch(Exception e){
	        LOGGER.error("Disable plugin Exception occour");
	    }
	}

	/**
	 * Disable all plugin.
	 * @throws Exception 
	 */
	public void deletePlugin() throws Exception{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.plugins"),getORPropValue("setup.pluginAnalyzerLink"));
		if(!isGridPresent("command")){
			return;
		}else if (WebdriverWrapper.getElementText(driver,getORPropValue("setup.plugins.pluginsTable")).contains("Enabled")){
			disablePlugin();
		}
		WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
		WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
		WebdriverWrapper.selectPopUp(driver,"accept");

	}

	public void createPlugin(String pluginName, String ip, String request, String jarFileLocation) throws Exception{
		disablePlugin();
		deletePlugin();
		//create new plugin
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.plugins")); 
		WebdriverWrapper.click(driver, getORPropValue("setup.pluginAnalyzerLink"));
		assertTrue(!isGridPresent("command"));
		WebdriverWrapper.click(driver,getORPropValue("button.new"));
		WebdriverWrapper.inputText(driver, getORPropValue("setup.plugins.name"), pluginName); 
		WebdriverWrapper.inputText(driver, getORPropValue("setup.plugins.desc"), pluginName); 		
		WebdriverWrapper.inputText(driver, getORPropValue("setup.plugins.fromIpAddr"), ip); 
		WebdriverWrapper.inputText(driver, getORPropValue("setup.plugins.toIpAddr"), ip); 
		WebdriverWrapper.selectList(driver, getORPropValue("setup.plugins.URLFilterSelect"), "Matches");   
		WebdriverWrapper.inputText(driver, getORPropValue("setup.plugins.urlPathFilter"), request); 	
		//upload jar file
		LOGGER.debug("***In case of plug-In process failue make sure jar file present in specifed location****");
		WebdriverWrapper.uploadFile(driver, getORPropValue("setup.plugins.jar"), jarFileLocation); 
		WebdriverWrapper.click(driver,getORPropValue("button.save"));	
		assertTrue(!isClassErrorLabelElementPresent());
		int colName = getColNumByColTitle("command", getORPropValue("setup.plugins.pluginNameCol"));
		assertTrue(getRowNumByContentAndColumn("command", pluginName, colName)>0);
	}
	/**
	 * Enable all plugin.
	 * @throws Exception 
	 */
	public void enablePlugin() throws Exception{		
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.plugins"),getORPropValue("setup.pluginAnalyzerLink"));
		if(!isGridPresent("command")){
			return;
		}else if (!WebdriverWrapper.getElementText(driver,getORPropValue("setup.plugins.pluginsTable")).contains("Disabled")){
			return;
		}
		WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
		WebdriverWrapper.click(driver, getORPropValue("button.enable"));
		}


		public void updatePluginName(String pluginName) throws Exception{
			disablePlugin();
			//update plugin
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.plugins"),getORPropValue("setup.pluginAnalyzerLink"));
			if (!isGridPresent("command")) {
				return;
			} else if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+pluginName)) {
				return;
			}
			WebdriverWrapper.click(driver,getORPropValue("//table[@class='gridTable']/tbody/tr[1]/td[2]/a"));
			WebdriverWrapper.inputText(driver, getORPropValue("setup.plugin.name.edit"), pluginName);
			WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));		
			assertTrue(!isClassErrorLabelElementPresent());
			int colName = getColNumByColTitle("command", getORPropValue("setup.plugins.pluginNameCol"));
			assertTrue(getRowNumByContentAndColumn("command", pluginName, colName)>0);

	}

	/**
	 * Simply navigate and click Synchronize all 
	 * @throws Exception 
	 */
	public void synchronizeAllMonitors() throws Exception{
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.monitors")); //	goToMonitorsSetup();
		WebdriverWrapper.click(driver,getORPropValue("cem.security.syncallmonitorsbtn"));	//driver.findElement(By.xpath(syncAllMonitorsBtn)).click();
		LOGGER.info("TIM Monitors are synchronized Now");
		
		try{
			Thread.sleep(5000);
		}catch(InterruptedException e){	
			e.printStackTrace();
		}
	}


    public void setPathParameterDelimiters(String delimiters) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
            getORPropValue("setup.domain"));
        WebdriverWrapper.inputText(driver, getORPropValue("setup.domain.pathParamDelimiters"),
            delimiters);
        WebdriverWrapper.click(driver, getORPropValue("setup.domain.save"));
        WebdriverWrapper.waitForObject(driver, getORPropValue("messagesDiv"));
        assertTrue(WebdriverWrapper.isTextInSource(driver,
            "The domain information was saved successfully"));
        LOGGER.info("Found Text : The domain information was saved successfully");
    }
	/**
	 * Create, Enable a monitor and Synchronize it
	 * Will call individual methods inside to create, enable and Synchronize
	 * @param monitorName
	 * @param ipAddress
	 * @throws Exception 
	 */
	public void createEnableSynchronizeMonitor(String monitorName, String ipAddress) throws Exception{
		createMonitor(monitorName, ipAddress);
		enableMonitor(monitorName);
		synchronizeAllMonitors();
	}	
	
	/**
	 * Create a web filter with all parameters
	 * <b> you must use the radio select as defined in the Setup class addressTypeIpAddressRadio or addtressTypeMacAddressRadio</b>
	 * Will return the message div string if it is found
	 * @param webFilterName
	 * @param monitorName
	 * @param addressType
	 * @param fromIp
	 * @param toIp
	 * @param port
	 * @param skip
	 * @return
	 */
	public String createWebFilter(String webFilterName, String monitorName, String addressType,String fromIp, String toIp, String port, Boolean skip){
		String confirm = "";
		try{
		    //LogIn();
		    System.out.println("Creating WebServer Filter for monitor: "+monitorName);
		    WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.webserverfilters"));
		    if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("grid.header.checkbox"))) {
		        WebdriverWrapper.click(driver, getORPropValue("grid.header.checkbox"));
		        WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
		        WebdriverWrapper.selectPopUp(driver, "accept");
		    }
		    
    		WebdriverWrapper.click(driver, getORPropValue("button.new"));
    		WebdriverWrapper.inputText(driver, getORPropValue("setup.wsf.webFilterNameEdit"), webFilterName);
    		WebdriverWrapper.selectBox(driver, getORPropValue("setup.wsf.monitorNameSelect"), monitorName);
    		if(!addressType.toLowerCase().contains("mac"))
    		    WebdriverWrapper.selectRadioButton(driver, getORPropValue("setup.wsf.addressTypeIpAddressRadio"));
    		else
    		    WebdriverWrapper.selectRadioButton(driver, getORPropValue("setup.wsf.addtressTypeMacAddressRadio"));
    		WebdriverWrapper.inputText(driver, getORPropValue("setup.wsf.fromIpAddressEdit"), fromIp);	
    		WebdriverWrapper.inputText(driver, getORPropValue("setup.wsf.toIpAddressEdit"), toIp);		
    		WebdriverWrapper.inputText(driver, getORPropValue("setup.wsf.portEdit"), port);	
    		if(skip)
    		    WebdriverWrapper.selectCheckBox(driver, getORPropValue("setup.wsf.saveWithoutCheckingForOverlapCheck"));
    		else	
    		    WebdriverWrapper.deselectCheckBox(driver, getORPropValue("setup.wsf.saveWithoutCheckingForOverlapCheck"));	
    		
    		WebdriverWrapper.click(driver, getORPropValue("button.save"));
    		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    			
		}catch(Exception e){
            e.printStackTrace();
		}
		return confirm;
	}
	
	public String createWebFilter(String webFilterName, String monitorName, String ipAddress){
		return createWebFilter(webFilterName, monitorName, getORPropValue("setup.wsf.addressTypeIpAddressRadio"), ipAddress, ipAddress, "0", false);
	}
	
	public void deleteWebFilter(String webFilterName){
	   try{
	    if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+webFilterName)){
            WebdriverWrapper.click(driver, getORPropValue("linkText")+webFilterName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
            //if(WebdriverWrapper.isAlertPresent(driver)))
            WebdriverWrapper.selectPopUp(driver,"accept");
            //WebdriverWrapper.selectPopUp(driver, "accept");
            assertFalse(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+webFilterName));
        }    
	    }catch(Exception e){
	        e.printStackTrace();
	    }
	}
	
	
	/*
	 * Creates a new monitor.
	 * 
	 * @param monitorname
	 * 
	 * @param ipaddress
	 * 
	 * @param
	 */
	
	public void createMonitor(String monitorName, String ipAddress) {
		try {
			//LogIn();
			WebdriverWrapper.navigateToPage(driver,getORPropValue("home.system"),getORPropValue("home.setup"),getORPropValue("setup.monitors"));


			//WebdriverWrapper.navigateToUrl(driver, "http://"+TESS_HOST+":"+TESS_PORT+"/"+getORPropValue("monitors.link.url"));
		    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            System.out.println("Inside createMonitor() method");
            System.out.println("monitorName: "+monitorName);
            System.out.println("ipAddress: "+ipAddress);
			
            if(WebdriverWrapper.isObjectPresent(driver,getORPropValue("linkText")+monitorName))
            {	
            	disableMonitor(monitorName);
                deleteTim(monitorName);
            }
            if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+ipAddress))
            {
            	Integer col = getColNumByColTitle("monitor", "IP Address");
    			Integer row = getRowNumByContentAndColumn("monitor",ipAddress, col);
    			deleteTim(getCellContentsByRowAndCol("monitor", row, 2));
            	
            }
			WebdriverWrapper.click(driver, getORPropValue("button.new"));
			System.out.println("clicked on new button");
			WebdriverWrapper.inputText(driver,
					getORPropValue("setup.tim.name"), monitorName);
			WebdriverWrapper.inputText(driver,
					getORPropValue("setup.tim.ipaddress"), ipAddress);
			WebdriverWrapper.click(driver, getORPropValue("button.save"));
			System.out.println("monitor creation is successfull");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	/**
	 * Create a new monitor - reuse or create monitor using tim ip.
	 * @param ipAddress is same as monitorName
	 */
	public void createMonitor(String ipAddress){
		try{
		WebdriverWrapper.navigateToPage(driver,
				getORPropValue("home.setup"),
				getORPropValue("setup.monitors"));
		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		for (int i=1; WebdriverWrapper.isObjectPresent(driver, getORPropValue("setup.monitorTable")+"/tbody/tr["+i+"]");i++) {
			if (getCellContentsByRowAndCol(getORPropValue("setup.monitors.monitorTableID"),i,getORPropValue("grid.nameTH")).equals(ipAddress)) {
				System.out.println("Monitor existed. "+ipAddress);
				return;
			}
		}
		if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+ipAddress)){
			Integer col = getColNumByColTitle("monitor", "IP Address");
			Integer row = getRowNumByContentAndColumn("monitor",ipAddress, col);
			deleteTim(getCellContentsByRowAndCol("monitor", row, 2));
		}
		WebdriverWrapper.click(driver, getORPropValue("button.new"));
		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		System.out.println("clicked on new button");
		WebdriverWrapper.inputText(driver,
				getORPropValue("setup.tim.name"), ipAddress);
		WebdriverWrapper.inputText(driver,
				getORPropValue("setup.tim.ipaddress"), ipAddress);
		WebdriverWrapper.click(driver, getORPropValue("button.save"));
		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		System.out.println("Monitor created. "+ipAddress);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Default enable monitor
	 * @param monitorName
	 */
	public void enableMonitor(String monitorName)throws Exception{
		 enableMonitor(monitorName, false);
	}
	
	
	public void enableMonitor(String monitorName, boolean upgrade)throws Exception{
WebdriverWrapper.navigateToPage(driver,getORPropValue("home.system"),
				getORPropValue("home.setup"),
				getORPropValue("setup.monitors"));
		String nameVal = "";
		for (int i=1;WebdriverWrapper.isObjectPresent(driver,getORPropValue("setup.monitorTable")+"/tbody/tr["+i+"]");i++) {
			nameVal = getCellContentsByRowAndCol(getORPropValue("setup.monitors.monitorTableID"),i,getORPropValue("grid.nameTH"));
			if (!nameVal.equals(monitorName)) {
				checkGridRow(getORPropValue("setup.monitors.monitorTableID"), nameVal);
				WebdriverWrapper.clickDisable(driver,getORPropValue("setup.monitors.disable"));
				
				if(WebdriverWrapper.isAlertPresent(driver))
				{
					WebdriverWrapper.selectPopUp(driver,"accept");
				}
							
			}
		}
		checkGridRow(getORPropValue("setup.monitors.monitorTableID"), monitorName);
		if (upgrade){
			WebdriverWrapper.click(driver, getORPropValue("setup.monitors.enable"));
			WebdriverWrapper.selectPopUp(driver, "accept");
		}else{
			WebdriverWrapper.click(driver, getORPropValue("setup.monitors.enable"));
			
			if(WebdriverWrapper.isAlertPresent(driver))
			{
				WebdriverWrapper.selectPopUp(driver, "accept");
			}
		}
		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		}
	public void deleteTim(String monitorName)
	{
		try {
			WebdriverWrapper.navigateToPage(driver,
					getORPropValue("home.setup"),
					getORPropValue("setup.monitors"));
			if (getCellContentsByRowAndCol("monitor", getRowNumByContentAndColumn("monitor", monitorName, getColNumByColTitle("monitor","Name")),getColNumByColTitle("monitor","Enabled")).equals("Enabled")){
				disableMonitor(monitorName);
			}
			if (!WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+monitorName)){
				return;
			}
			WebdriverWrapper.click(driver, getORPropValue("linkText")+monitorName);
			WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
			WebdriverWrapper.selectPopUp(driver, "accept");
			if(this.getMessageFromErrorDiv().equals("This monitor is currently being used to monitor web servers. Please assign a different monitor to these web servers before deleting this monitor.")){
				WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.webserverfilters"));
				Integer rowNum = this.getRowNumByContentAndColumn("webserverfilter", monitorName, this.getColNumByColTitle("webserverfilter", "TIM"));
				String webFilterName = this.getCellContentsByRowAndCol("webserverfilter", rowNum, "Name");
				deleteWebFilter(webFilterName);
				WebdriverWrapper.navigateToPage(driver,
						getORPropValue("home.setup"),
						getORPropValue("setup.monitors"));
				
				if (getCellContentsByRowAndCol("monitor", getRowNumByContentAndColumn("monitor", monitorName, getColNumByColTitle("monitor","Name")),getColNumByColTitle("monitor","Enabled")).equals("Enabled")){
					disableMonitor(monitorName);
				}
				WebdriverWrapper.click(driver, getORPropValue("linkText")+monitorName);
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
				WebdriverWrapper.selectPopUp(driver, "accept");
				
			}

			System.out.println("tim deleted successfully");
			
		} catch (Exception e) {
			
			System.out.println(e.getMessage());
		}
		
	}
public void disableMonitor(String monitorName) throws Exception{
		
		WebdriverWrapper.navigateToPage(driver,
				getORPropValue("home.setup"),
				getORPropValue("setup.monitors"));
		
		checkGridRow("monitor", monitorName);
		WebdriverWrapper.clickDisable(driver, getORPropValue("setup.monitors.disable"));
		WebdriverWrapper.selectPopUp(driver, "accept");
		
		
	}
	
	public void setMediumImpact(String impactSetting){
	    try{
	    WebdriverWrapper.navigateToPage(driver,getORPropValue("home.setup"),getORPropValue("setup.incidentsettings"));
	    WebdriverWrapper.inputText(driver, getORPropValue("setup.incidentsettings.moderateSevertityEdit"), impactSetting);
	    WebdriverWrapper.click(driver, getORPropValue("button.save"));
	    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	    }catch(Exception e){
	        e.printStackTrace();
	    }
	}
	
	public void setNumDefects(int numDefects){
	       try{
	       WebdriverWrapper.navigateToPage(driver,getORPropValue("home.setup"),getORPropValue("setup.incidentsettings"));
	       if(WebdriverWrapper.getAttribute(driver, getORPropValue("setup.incidentsettings.numDefectsPerIntervalEdit"), "value").equals("")||
	               Integer.parseInt(WebdriverWrapper.getAttribute(driver, getORPropValue("setup.incidentsettings.numDefectsPerIntervalEdit"), "value"))!=numDefects){
	           WebdriverWrapper.inputText(driver, getORPropValue("setup.incidentsettings.numDefectsPerIntervalEdit"), Integer.toString(numDefects));
	           if(!WebdriverWrapper.isElementSelected(driver, getORPropValue("setup.incidentsettings.simpleDefectRateEnableCheck"))){
	               WebdriverWrapper.selectCheckBox(driver, getORPropValue("setup.incidentsettings.simpleDefectRateEnableCheck"));
	           }
	  
	           WebdriverWrapper.click(driver, getORPropValue("button.save"));
	           WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	       }
	       WebdriverWrapper.click(driver, getORPropValue("button.save"));
	       WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	       }catch(Exception e){
	           e.printStackTrace();
	       }
	}
	   
	public void setIgnoreApplicationInUserRecognitionOnDomain(Boolean check){
	       try{
	       WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),getORPropValue("setup.domain"));
	       if (check){
	           WebdriverWrapper.selectCheckBox(driver, getORPropValue("cem.system.domain.ignoreAppsCheckBox"));
	       }else{
	           WebdriverWrapper.deselectCheckBox(driver, getORPropValue("cem.system.domain.ignoreAppsCheckBox"));
	       }
	       WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
	       WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	       }catch(Exception e){
	           e.printStackTrace();
	       }
	}
	
	
	public void setupTim(String timName, String timIP){
        try{
            setupMonitor.createMonitor(timName, timIP, TESS_HOST);
            setupMonitor.enableMonitor(timName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
	public void syncMonitors(){
        try{
            setupMonitor.syncMonitors();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
	public void setUserGroupSettings(String subnetMask) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
            getORPropValue("setup.domain"));
        WebdriverWrapper.selectCheckBox(driver, getORPropValue("setup.domain.UserGroupSettings.checkBox"));
        WebdriverWrapper.inputText(driver, getORPropValue("setup.domain.UserGroupSettings.SubnetMask.input"),
            subnetMask);
        WebdriverWrapper.click(driver, getORPropValue("setup.domain.save"));
        WebdriverWrapper.waitForObject(driver, getORPropValue("messagesDiv"));
        assertTrue(WebdriverWrapper.isTextInSource(driver,
            "The domain information was saved successfully"));
        LOGGER.info("User Group Setting added was saved successfully");
    }
	
	public void disableUserGroupSettings() throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
            getORPropValue("setup.domain"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("setup.domain.UserGroupSettings.checkBox"));
        WebdriverWrapper.click(driver, getORPropValue("setup.domain.save"));
        WebdriverWrapper.waitForObject(driver, getORPropValue("messagesDiv"));
        assertTrue(WebdriverWrapper.isTextInSource(driver,
            "The domain information was saved successfully"));
        LOGGER.info("Disable User Group Setting was saved successfully");
    }
}
