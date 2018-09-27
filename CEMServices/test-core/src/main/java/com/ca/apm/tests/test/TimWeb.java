package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TimWeb extends BaseSharedObject{
   
   private  final long SLEEP_TIME =iGlobalTimeout;
   public final String timStartOrStopLink         = getORPropValue("timStartOrStopLink");
   public final String systemMachineLinkURL      = "http://"+TIM_ADMIN+":"+TIM_ADMIN_PWD+"@"+TIM_HOST_NAME+getORPropValue("systemMachineLinkURL");
   public final String systemSystemLinkURL       = "http://"+TIM_ADMIN+":"+TIM_ADMIN_PWD+"@"+TIM_IP+getORPropValue("systemSystemLinkURL");
   public final String returnToMainSetupLinkURL  = "http://"+TIM_ADMIN+":"+TIM_ADMIN_PWD+"@"+TIM_IP+getORPropValue("returnToMainSetupLinkURL");
   public final String timDiagnosticsPageURL     = "http://"+TIM_ADMIN+":"+TIM_ADMIN_PWD+"@"+TIM_IP+getORPropValue("timDiagnosticsPageURL");
   public final String systemTimLinkURL          = "http://"+getEnvConstValue("timAdmin")+":"+getEnvConstValue("timAdminPwd")+"@"+getEnvConstValue("timIp")+getORPropValue("TIMBaseUrl");


   public final String timLogErrorMessageIdentifier            ="ERROR: ";
   public final String timLogTraceMessageIdentifier            = " Trace: ";
   public final String timLogWarningMessageIdentifier          = "! Warning:";
   
   public final String timConfigDisableDHBasedCipherProcessing = "DisableDHBasedCipherSuiteProcessing";
   public final String disableDHCipherProcessing               = "0";
   public final String enableDHCipherProcessing                = "1";
   public final String timConfigMaxLogFileSize                 = "MaxLogSizeInMB";
  
   public final String sslServersStatusAddressTH               = "Address";
   public final String sslServersStatusTotalConnectionsTH      = "Total connections";
   public final String sslServersStatusFailuresTH              = "Connections with decode failures";
   public final int sslServersStatusTotalConnectionsCol        = 2;
   public final int sslServersStatusTotalFailuresCol           = 3;
   public final int sslServersStatusUnSupportedFailuresCol     = 4;
   public final String sslErrorMsg                             = "Warning: sslprint: Unsupported CipherSuite";
   public final String timLogInfoMessageIdentifiers[]          ={"WebServer:", "ConnManager:", "RecorderPost:", "AdapterManager:", "EventManager:",
           "Service:", "StatsMgr:", "ThreadMgr:", "TranDefManager:", "LoginManager:", "ConfigFile:", "Analysis:"};
   public QaUtils qautil = new QaUtils();
    
   public TimWeb(){
     //empty constructor
   }
   
   public TimWeb(WebDriver driver){
       this.driver = driver;
   }
   public final String pluginTableItem=getORPropValue("pluginTableItem");
   
    public boolean isTimRunning() throws Exception{
	     WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
	     WebdriverWrapper.click(driver, timStartOrStopLink);
	     return WebdriverWrapper.isTextInSource(driver, "Tim is running.");
    }
    
    /**
     * Pass column title and row number 
     * @param tableXpath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param row
     * @param colTitle
     * @return String that is in that table cell, 'not found' if cell is not present
     */
    public String getCellContentsByRowAndCol(String tableXpath, Integer row, String colTitle) throws Exception{
        Integer colNum = getColNumByColTitle(tableXpath, colTitle);
        return getCellContentsByRowAndCol(tableXpath, row, colNum);
    }
    
	public String getCellContentsByRowAndCol1(String tableXpath, Integer row, String colTitle) throws Exception{
        Integer colNum = getColNumByColTitle1(tableXpath, colTitle);
        System.out.println("Coulumn Number " + colNum + " Row = " + row);
        return getCellContentsByRowAndCol(getORPropValue("xpath")+tableXpath, row, colNum);
    }
   
    public void goToViewSSLServerStatusPage(){
    	try {
			WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
			WebdriverWrapper.navigateToPage(driver, getORPropValue("timSslServerStatusLink"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
    
    public void goToTransactionInspectionPage(){
       try {
			WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
			WebdriverWrapper.navigateToPage(driver, getORPropValue("timTransactionInspectionLink"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    public void goToTimLogPage(){
    	try {
			WebdriverWrapper.navigateToUrl(driver,systemTimLinkURL);
			WebdriverWrapper.navigateToPage(driver, getORPropValue("timViewLogLink"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public String readTimLog(String regmatch) throws Exception {
        String URL = "grep -i \"" + regmatch + "\" " + TIM_INSTALLDIR + "/CA/APM/tim/logs/timlog* ";// if timLog file change default path this line need to change 

        String timLogFile = "";
        int dealy = 0;
        dealy = (int) SLEEP_TIME ;
        Thread.sleep(dealy);
        timLogFile = qautil.execUnixCmd(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, URL);
        LOGGER.info("=====tim Log file========After grep value"+regmatch+":::" + timLogFile);
        return timLogFile;
    }

    public void setConfigSettingsValue(String property, String value){
    	try {
			System.out.println("Navigating to: "+systemTimLinkURL);
			WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
			WebdriverWrapper.navigateToPage(driver, getORPropValue("timConfigureSettingsLink"));
			if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+property))
			{
				WebdriverWrapper.click(driver, getORPropValue("linkText")+property);
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
				WebdriverWrapper.inputText(driver,getORPropValue("timConfigNewValueTxtBox"), value);
				WebdriverWrapper.click(driver,getORPropValue("timConfigChangeValueBtn"));
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
				
			}
			else
			{
				WebdriverWrapper.click(driver,getORPropValue("timConfigDefineNewValueLink"));
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
				WebdriverWrapper.inputText(driver,getORPropValue("timConfigNewNameTxtBox"), property);
				WebdriverWrapper.inputText(driver, getORPropValue("timConfigNewValueTxtBox"), value);
				WebdriverWrapper.click(driver,getORPropValue("timConfigAddValueBtn"));
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }
    
    public void goToConfigTimSettingsPage(){
        try{
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timConfigureSettingsLink"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void disableAllTimTraces(){
        try{
        goToConfigTimTraceOptionsPage();        
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceStatisticsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceDefectsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceSessionsAndLoginsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceBusinessTransactionsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceTransactionsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceTransactionComponentsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceHTTPcomponentsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceHTTPparametersChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceConnectionsChkBox"));
        WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timTraceSSLerrorsChkBox"));        
        WebdriverWrapper.click(driver, getORPropValue("timSetOptionsAndFiltersBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getTimPluginStatus(String pluginName) {
        String status = "";
        try{
        for (int i=2; ;i++) {
            if(!WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+pluginTableItem+"["+i+"]")){
                break;
            } 
            if(WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+pluginTableItem+"["+i+"]/td[2]").equals(pluginName)){
                status = WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+pluginTableItem+"["+i+"]/td[3]");
                break;
            } 
        }
        return status;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param tableXPath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param content
     * @param col
     * @return row number starting from 2, 0 if not found
     */
    public Integer getRowNumByContentAndColumn(String tableXPath, String content, Integer col)throws Exception{
        
        Integer row=2;
        boolean found=false;
        String colLocator = "]/td["+col.toString()+"]";
        while(WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+tableXPath+gridBodyLocator+row+colLocator))
        {
        	if(WebdriverWrapper.getElementText(driver, getORPropValue("xpath_")+tableXPath+gridBodyLocator+row+colLocator).equals(content))
        	{
        		found=true;
        		break;
        	}
        	row++;
        }
        if (found){ 
            return row;
        }else{
            return 0;
        }        
    
    }
	public Integer getRowNumByContentAndColumn1(String tableXPath, String content, Integer col)throws Exception{
        
        Integer row=1;
        boolean found=false;
        String colLocator = "]/td["+col.toString()+"]";
        while(WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+tableXPath+gridBodyLocator+row+colLocator))
        {
        	if(WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+tableXPath+gridBodyLocator+row+colLocator).equals(content))
        	{
        		found=true;
        		break;
        	}
        	row++;
        }
        if (found){ 
            return row;
        }else{
            return 0;
        }        
    
    }
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param tableXPath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param content
     * @param colTitle
     * @return row number starting from 2, 0 if not found
     */
    public Integer getRowNumByContentAndColTitleInTableWith2HeaderRows(String tableXPath, String content, String colTitle)throws Exception{
        Integer col = getColNumByColTitle(tableXPath, colTitle);
        Integer row = getRowNumByContentAndColumnInTableWith2HeaderRows(tableXPath, content, col);
        return row;
    }
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text.
     * Same as the getRowNumByContentAndColumn method, except this will handle if table has two header rows
     * @param tableXPath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param content
     * @param col
     * @return row number starting from 2, 0 if not found
     */
    public Integer getRowNumByContentAndColumnInTableWith2HeaderRows(String tableXPath, String content, Integer col)throws Exception{
        
        	Integer row=3;
			boolean found=false;
			String colLocator = "]/td["+col.toString()+"]";
			while(WebdriverWrapper.isObjectPresent(driver, tableXPath+gridBodyLocator+row+colLocator))
			{
				if(WebdriverWrapper.getElementText(driver,tableXPath+gridBodyLocator+row+colLocator).equals(content))
				{
					found=true;
					break;
				}
				row++;
			}
			if (found){ 
			    return row;
			}else{
			    return 0;
			}
		
    }
    /**
     * Main table getter, most others should ultimately call this one, uses Integers for row and column
     * @param tableXpath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param row
     * @param col
     * @return String that is in that table cell, 'not found' if cell is not present
     */
    public String getCellContentsByRowAndCol(String tableXpath, Integer row, Integer col) throws Exception{
        if (row.equals(0) || col.equals(0)){
            return "not found";
        }
        String gridLocator = tableXpath+gridBodyLocator;
        if(!WebdriverWrapper.isObjectPresent(driver, gridLocator+row+"]/td["+col+"]"))
        {
        	return "not found";
        }
        return WebdriverWrapper.getElementText(driver, gridLocator+row+"]/td["+col+"]");
    }
    
    public boolean eraseTimLog() { 
		try{
		    WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
		    WebdriverWrapper.click(driver, getORPropValue("timConfigureTraceLink"));
	        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	        //WebdriverWrapper.click(driver,getORPropValue("timEraseLogBtn"));
	        driver.findElement(By.xpath("//form/input[@value='Erase the Tim log']")).click();
	        System.out.println("Erasing tim log");
	        //WebdriverWrapper.selectPopUp(driver,"accept");
            Thread.sleep(10000);
            /*scr = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            DestFile=new File("C:\\Alert1.jpg");
            scr.renameTo(DestFile);*/
            System.out.println("Alert Present " + WebdriverWrapper.isAlertPresent(driver));
            driver.switchTo().alert().accept();
            
           /* if(WebdriverWrapper.isAlertPresent(driver))
          {
              WebdriverWrapper.selectPopUp(driver,"accept");
          }*/

	        Thread.sleep(10000);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
	        return WebdriverWrapper.isTextInSource(driver, getORPropValue("timLogErasedText"));
	        }catch(Exception e){
	            e.printStackTrace();
	            return false;
	        }
		
	}
    public void goToConfigTimTraceOptionsPage(){
        try{
            LOGGER.info("TIM URL:::"+systemTimLinkURL);
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timConfigureTraceLink"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void enableAllTimTraces(){
    	try{
        goToConfigTimTraceOptionsPage();
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceStatisticsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceDefectsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceSessionsAndLoginsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceBusinessTransactionsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceTransactionsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceTransactionComponentsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceHTTPcomponentsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceHTTPparametersChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceConnectionsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceSSLerrorsChkBox"));        
        WebdriverWrapper.click(driver, getORPropValue("timSetOptionsAndFiltersBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	}catch(Exception e){
            e.printStackTrace();
        }
    	
    }
    public void enableRequiredTimTraces(){
        try{
        goToConfigTimTraceOptionsPage();
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceDefectsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceSessionsAndLoginsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceBusinessTransactionsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceTransactionsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceTransactionComponentsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceHTTPcomponentsChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceHTTPparametersChkBox"));
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("timTraceConnectionsChkBox"));
        WebdriverWrapper.click(driver, getORPropValue("timSetOptionsAndFiltersBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public void enableTimTraceSessionsAndLogins() {
        try {
            goToConfigTimTraceOptionsPage();

            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("timTraceSessionsAndLoginsChkBox"));
            WebdriverWrapper.click(driver, getORPropValue("timSetOptionsAndFiltersBtn"));
            LOGGER.info("TimTraceSessionsAndLogins enabale");
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public void startTim(){
    	try{
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timStartOrStopLink"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            if(!WebdriverWrapper.isTextInSource(driver, "Tim is running.")){        
    	        WebdriverWrapper.click(driver, getORPropValue("timStartBtn"));
    	        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	        System.out.println("Starting Tim...");
                Thread.sleep(3000);       
    	    }     
    	}
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void stopTim(){
		try{
    	System.out.println("Navigating to url: "+systemTimLinkURL);
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);        
        WebdriverWrapper.click(driver, getORPropValue("timStartOrStopLink"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, getORPropValue("timStopBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        System.out.println("Stopping tim...");
        Thread.sleep(3000);
        }
        catch(Exception e){            
            e.printStackTrace();
        }

    }
    
  /**
   * To get tim config Settings properties
   * */
    public String getConfigSettingsValue(String property){
        //goToConfigTimSettingsPage();

        try{
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.navigateToPage(driver, getORPropValue("timConfigureSettingsLink"));
        WebdriverWrapper.click(driver, getORPropValue("linkText")+property);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        return WebdriverWrapper.getAttribute(driver, getORPropValue("timConfigNewValueTxtBox"), "value").trim();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public void setWebProtectOptions(String key, boolean state){
    	try{
        goToTimWebProtectOptionsPage();
        if(state)
            WebdriverWrapper.selectCheckBox(driver, key);
        else
        	WebdriverWrapper.deselectCheckBox(driver, key);
        WebdriverWrapper.click(driver, getORPropValue("timWebProtectSubmitBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
   
    public void goToTimWatchdogSettingsPage(){
    	try{
    	WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timConfigureWatchdogSettingsLink"));
    	WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void deleteConfigSettingsValue(String property){
        try{
        goToConfigTimSettingsPage();
        WebdriverWrapper.click(driver, getORPropValue("linkText")+property);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, getORPropValue("timConfigDeleteValueBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public boolean isConfigSettingExists(String property){
        try{
        goToConfigTimSettingsPage();
        return WebdriverWrapper.isObjectPresent(driver, property);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    public void goToSiteminderLogPage(){
    	try{
        WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timViewSiteMinderLogLink"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void goToSiteminderHostConfigPage(){
    	
    	try{
            WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
            WebdriverWrapper.click(driver, getORPropValue("timSiteminderHostConfigLink"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        	}catch(Exception e){
        		e.printStackTrace();
        }
    }
    
    public void goToTimWebProtectOptionsPage(){
        try{
            WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
            WebdriverWrapper.click(driver, getORPropValue("timConfigureWebProtectOptionsLink"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        	}catch(Exception e){
        		e.printStackTrace();
        }
    }
    
    public void goToConfigTimNetworkInterfacesPage(){
    	try{
    	WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timConfigureNetworkInterfacesLink"));
    	WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param tableXPath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param content
     * @param colTitle
     * @return row number starting from 2, 0 if not found
     */
    public Integer getRowNumByContentAndColTitle(String tableXPath, String content, String colTitle){
        try{
        Integer col = getColNumByColTitle(tableXPath, colTitle);
        Integer row = getRowNumByContentAndColumn(tableXPath, content, col);
        return row;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
	public Integer getRowNumByContentAndColTitle1(String tableXPath, String content, String colTitle){
        try{
        Integer col = getColNumByColTitle1(tableXPath, colTitle);
        System.out.println("Column Number " + col);
        Integer row = getRowNumByContentAndColumn1(tableXPath, content, col);
        System.out.println("Ron Number " + row);
        return row;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
	public void setDefaultTimWatchdogSettings(){
        try{
        WebdriverWrapper.navigateToUrl(driver, getORPropValue("timConfigureWatchdogSettingsLink"));;
        WebdriverWrapper.inputText(driver, getORPropValue("timWatchdogMaxMemoryTxtBox"), getORPropValue("timWatchdogMaxMemoryDefaultValue"));
        WebdriverWrapper.inputText(driver, getORPropValue("timWatchdogMaxResponseTimeTxtBox"), getORPropValue("timWatchdogMaxResponseTimeDefaultValue"));
        WebdriverWrapper.inputText(driver, getORPropValue("timWatchdogRestartHourTxtBox"), "");
        WebdriverWrapper.inputText(driver, getORPropValue("timWatchdogRestartMinTxtBox"), "");
        WebdriverWrapper.click(driver, getORPropValue("timWatchdogSettingsSetBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void goToTimStatusPage(){
        try{
        WebdriverWrapper.navigateToPage(driver, systemTimLinkURL);
        WebdriverWrapper.click(driver, getORPropValue("timViewStatusLink"));
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Scans table header for desired column and returns column number
     * @param tableXpath : XPath of the table to be searched. Ex: "/html/body/table[2]"
     * @param title
     * @return column number starting from 1, 0 if not found
     */
    public Integer getColNumByColTitle(String tableXpath, String title){
    	try{
        Integer col = 1;
        boolean found = false;        
        String gridLocator = tableXpath+gridBodyLocator;
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+gridLocator+"1]/th["+col.toString()+"]")){
            if(WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+gridLocator+"1]/th["+col.toString()+"]").equals(title)){
                found=true;
                break;
            }
            col++;
        }
        if(found){
            return col;
        }else{
            return 0;
        }
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
	public Integer getColNumByColTitle1(String tableXpath, String title){
    	try{
        Integer col = 1;
        boolean found = false;        
        String gridLocator = tableXpath+"']/thead/tr[";
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+gridLocator+"1]/th["+col.toString()+"]")){
        	String str = WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+gridLocator+"1]/th["+col.toString()+"]").replaceAll("[\\t\\n\\r]"," ");
        	//System.out.println(str);
            if(str.equals(title)){
                found=true;
                break;
            }
            col++;
        }
        if(found){
            return col;
        }else{
            return 0;
        }
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }

    public void goToTimNetWorkInterface() {
        try {
            WebdriverWrapper.navigateToUrl(driver, systemTimLinkURL);
            WebdriverWrapper.navigateToPage(driver, getORPropValue("timViewNetworkInterface"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void setLoopback() {
        try {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("timloopback"));
            WebdriverWrapper.click(driver, getORPropValue("timloSetBtn"));
            LOGGER.info("Loop back enable ::Tim network interfaces");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void disableLoopback() {
        try {
            WebdriverWrapper.deselectCheckBox(driver, getORPropValue("timloopback"));
            WebdriverWrapper.click(driver, getORPropValue("timloSetBtn"));
            LOGGER.info("Loop back enable ::Tim network interfaces");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
