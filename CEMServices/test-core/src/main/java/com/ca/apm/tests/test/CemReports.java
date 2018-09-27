package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.WebDriver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CemReports extends BaseSharedObject{

	public final String defectTypeSelectAllTxt	  	   = getORPropValue("cem.reports.defectTypeSelectAllTxt");
	public final String showSelectAllTxt	  	  	   = getORPropValue("cem.reports.showSelectAllTxt");
	
	//Defects - search result
	public final String defectDefectNameTH			   = getORPropValue("cem.reports.defectDefectNameTH");
	
	//incidents
	public final String inTableID       		   	    = getORPropValue("cem.reports.inTableID");
	public final String inDefectNameTH				    = getORPropValue("cem.reports.inDefectNameTH");
	public final String inLastOccurredTH			    = getORPropValue("cem.reports.inLastOccurredTH");
	public final String inDefectsTH					    = getORPropValue("cem.reports.inDefectsTH");
	public final String inIDTH			   			 	= getORPropValue("cem.reports.inIDTH");
	public final String inBusinessServiceTH	 		  	= getORPropValue("cem.reports.inBusinessServiceTH");
	public final String inBusinessTransactionTH	 	  	= getORPropValue("cem.reports.inBusinessTransactionTH");
	public final String inTransactionTraceTH	 	  	= getORPropValue("cem.reports.inTransactionTraceTH");
	
	public final String defectTableID          	  	   = getORPropValue("cem.reports.defectTableID");
	public final String defectIViewDateandTimeCol	   = getORPropValue("cem.reports.defectIViewDateandTimeCol");
	public final String allOption				  	  = getORPropValue("cem.reports.allOption");
	
	public CemReports(){
	    //empty constructor
	}
	
	public CemReports(WebDriver driver){
	    this.driver = driver;
	}
	public int getSearchResultCountDefectShowAll () throws Exception {
		WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.defects"));
		WebdriverWrapper.selectBox(driver,getORPropValue("cem.incidentManagement.defects.defectTypeSelect"), defectTypeSelectAllTxt);
		WebdriverWrapper.selectBox(driver,getORPropValue("cem.incidentManagement.defects.showSelect"),showSelectAllTxt);
		WebdriverWrapper.click(driver,getORPropValue("button.refresh"));
		WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
		
		int itemFoundCurrentInt = -1;
		itemFoundCurrentInt = getSearchResultCountDefect();
		return itemFoundCurrentInt;
	}

    public int getDefectSearchResult(String loginName, String ba, String bs) throws Exception {
        int i = 0;
        long sleepTime=30000;
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
            getORPropValue("cem.incidentmanagemnt"),
            getORPropValue("cem.incidentManagement.defects"));
        WebdriverWrapper.selectBox(driver,
            getORPropValue("cem.incidentManagement.defects.defectTypeSelect"),
            defectTypeSelectAllTxt);
        WebdriverWrapper.selectBox(driver,
            getORPropValue("cem.incidentManagement.defects.showSelect"), showSelectAllTxt);
        if (!loginName.trim().equalsIgnoreCase("")) {
            WebdriverWrapper.inputText(driver, getORPropValue("cem.defect.loginName"), loginName);
        }
        WebdriverWrapper.selectBox(driver, getORPropValue("cem.defect.businesapplication"), ba);
        WebdriverWrapper.selectBox(driver, getORPropValue("cem.defect.businessservices"), bs);
        WebdriverWrapper.click(driver, getORPropValue("button.generate"));

        while (i < 10) {
            if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.defect.pagebanner"))) {
                WebdriverWrapper.click(driver, getORPropValue("button.generate"));
                break;
            } else {
                LOGGER.debug("Process getting sleep for----"+sleepTime);
                Thread.sleep(sleepTime);
                WebdriverWrapper.click(driver, getORPropValue("button.generate"));
                LOGGER.info("Submited Search Again");
                i++;
            }

        }
        LOGGER.info("Filtered Applied : All eligiable defect display ");
        int itemFoundCurrentInt = -1;
        itemFoundCurrentInt = getSearchResultCountDefect();
        LOGGER.info("Total number of defect found::" + itemFoundCurrentInt);
        return itemFoundCurrentInt;
    }
	public int getSearchResultCountDefect () throws NumberFormatException, Exception {
		String itemFoundStr = "";
		String itemStr = "";
		int indexOfItem = -1;
		int itemFoundCurrentInt = -1;
		
		if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.defect.pagebanner"))){   //found item
            itemFoundStr = WebdriverWrapper.getElementText(driver, getORPropValue("cem.defect.pagebanner"));
            indexOfItem = itemFoundStr.indexOf("item");
            assertTrue(indexOfItem>0);
            itemStr = itemFoundStr.substring(0,indexOfItem-1);
            if (itemStr.equals("One")) {
                itemFoundCurrentInt = 1;
            }else if (itemStr.equals("1,000")) {
                itemFoundCurrentInt = 1000;
            }else {
                itemStr = itemStr.replace(",", "");
                itemFoundCurrentInt = Integer.parseInt(itemStr);
            }
            assertTrue(itemFoundCurrentInt >=1);
        } else {
            itemFoundCurrentInt = 0;      //not found
        }
		
		return itemFoundCurrentInt;
	}
public void goToIncidentsLink(){
		
		try {
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"));
			if(!WebdriverWrapper.isTextInSource(driver, "Open  Incidents for All Business Application"))
			{
				WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"));
				WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
			}
			WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.incidents"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
public void closeAllOpenIncidents() {
	//go to incidents page, close all existed incidents.
	try {
		goToIncidentsLink();
		WebdriverWrapper.click(driver, getORPropValue("cem.incidentmanagment.incident.refresh"));
		WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+getORPropValue("grid.base.locator")+inTableID+"']")) 
		{
			this.checkAllGridRows(inTableID);
			WebdriverWrapper.click(driver, getORPropValue("cem.reports.closebtn"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}
/**
* Will check to make sure a defect fitting these params is present in the defects table
* Checks by Service name, defect type and expected time interval for defects in defects page
* Assumes defects will be on the first page of the paginated list
* @param serviceName
* @param clientIp
* @param defectName
* @return true/false
* @throws Exception 
 */
public boolean isDefectPresentTime(String serviceName, String defectType, Date startTime, Date endTime) throws Exception{ //deprecated

	String DATE_AND_TIME_FORMAT = "d-MMM-yyyy HH:mm:ss";
	//add some wiggle to the start and end times for clock differences
	SimpleDateFormat sdf = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
	Calendar cal = Calendar.getInstance();
	cal.setTime(startTime);
	cal.add(Calendar.SECOND, -30);
	Date startDate = cal.getTime();
	cal.setTime(endTime);
	cal.add(Calendar.SECOND, 30);
	Date endDate = cal.getTime();
	WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
	WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects"));
	//sel.select(businessServiceSelect, serviceName);
	WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.defectTypeSelect"), defectType);
	WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.showselect"),"All Defects" );
	WebdriverWrapper.click(driver,getORPropValue("button.search"));
	//start walking table rows, check service, then check type, then ip
	boolean found = false;
	int row = 1;
	while (WebdriverWrapper.isObjectPresent(driver,getORPropValue("grid.reportbaselocator")+"defect"+getORPropValue("grid.bodylocator")+row+"]")){
		if (getReportCellValue("defect", row, "Business Service").equals(serviceName)){
			if (getReportCellValue("defect", row, "Defect Name").equals(defectType)){
				//now check that defect is in right time range
				String defectTime = getReportCellValue("defect", row, "Date and Time");
				Date defectDate = new Date();

				try {
					defectDate = sdf.parse(defectTime);
				} catch (Exception e) {

					e.printStackTrace();
				}
				if (defectDate.after(startDate)&&defectDate.before(endDate)){
					found = true;
					break;
				}
			}

		}
		row++;
	}
	return found;

}
/**
 * Special version for reports div
 * @param grid
 * @param row
 * @param colTitle
 * @return
 * @throws Exception 
  */
 @Deprecated
 public String getReportCellValue(String grid, int row, String colTitle ) throws Exception{
                 String gridLocator = getORPropValue("grid.reportbaselocator")+grid+getORPropValue("grid.bodylocator");

 return WebdriverWrapper.getElementText(driver,gridLocator+row+"]/td["+getColumnNumberForTitle("defect", colTitle)+"]");
 }
 /**
 * Special version of grid info for reports div
 * @param grid
 * @param title
 * @return
 * @throws Exception 
  */
 @Deprecated
 public String getColumnNumberForTitle(String grid, String title) throws Exception{
                 Integer col = 1;
                 boolean found = false;
                 
                 while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("grid.locator")+col+getORPropValue("grid.header.cell.locator"))){
                                 if (WebdriverWrapper.verifyTextPresent(driver,getORPropValue("grid.locator")+col+getORPropValue("grid.header.cell.locator").equals(title))){
                                                 found=true;
                                                 break;
                                 }
                                 col++;
                 }
                 if (found){           
                                 return col.toString();
                 }else{
                                 return "0";
                 }
 }
 public boolean isDefectPresent(String applicationName, String defectType) throws Exception{
                 WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
                 WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects"));
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.businessAppSelect"), defectType);
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.showselect"),"All Defects" );
                 WebdriverWrapper.click(driver,getORPropValue("button.search"));
                 //start walking table rows, check service, then check type, then ip
                 boolean found = false;
                 int rows;
                 rows = getRowCountFromTable("defect");
                 if (rows > 0){found = true;}
                 return found;
 }
 public int getSearchResultCountHRHP () throws Exception {
                 WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
                 WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects"));
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.defectTypeSelect"),getORPropValue("cem.im.defects.defectTypeSelectHRHPTxt"));
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.showselect"),getORPropValue("cem.im.defects.showSelectAllTxt"));
                 WebdriverWrapper.click(driver,getORPropValue("button.search"));

                 int itemFoundCurrentInt = -1;
                 itemFoundCurrentInt = getSearchResultCountDefect();
                 return itemFoundCurrentInt;
 }

 public void goToSearchResultCountDefectShowAll () throws Exception {
                 WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
                 WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects"));
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.defectTypeSelect"),"All");
                 WebdriverWrapper.selectList(driver,getORPropValue("cem.im.defects.showselect"), "All Defects");
                 WebdriverWrapper.click(driver,getORPropValue("button.search"));
 }
 public void deleteAllMyReports () throws Exception {
                 WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"),getORPropValue("cem.myreports"));

                 checkAllGridRows("reportDef");
                 WebdriverWrapper.click(driver,getORPropValue("button.delete"));

                 if (WebdriverWrapper.isAlertPresent(driver)) {
                                 WebdriverWrapper.selectPopUp(driver,"accept");;                                        
                 }
                 assertTrue(!WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+getORPropValue("grid.base.locator")+"reportDef"+"']"));
 }
 public String getColTitleForColNum(String grid, int colNum) throws Exception{

                 String gridLocator = getORPropValue("grid.base.locator")+grid+getORPropValue("grid.header.locator");
                 if (WebdriverWrapper.isObjectPresent(driver,gridLocator+colNum+getORPropValue("grid.header.cellLocator"))){
                                 return WebdriverWrapper.getElementText(driver,gridLocator+colNum+getORPropValue("grid.header.cellLocator"));
                 }
                 else {
                                 return "";
                 }
 }
 // email PDF method to send email PDFs.
 public void emailPDF(String reportName) throws Exception{

     WebdriverWrapper.click(driver,getORPropValue("linkText")+"Email PDF");

     WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.emailpdf.to"), getEnvConstValue("emailId"));
     WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.emailpdf.subject"), "Wily CEM Report:"+reportName);
     WebdriverWrapper.click(driver,getORPropValue("cem.reports.emailpdf.send"));
     assertTrue(WebdriverWrapper.isTextInSource(driver, "Email sent successfully"));
     WebdriverWrapper.click(driver,getORPropValue("cem.reports.emailpdf.close"));

}    

 public boolean verifySaveorScheduleReport (String reportFormat, String applicationName, String serviceName, String timeFrameOption ) throws Exception {

                 boolean result = true;
                 //sel.click(slmSaveorScheduleReportLink);
                 //sel.waitForPageToLoad(timeout);
                 

                 if (!WebdriverWrapper.getSelectedValue(driver,getORPropValue("cem.report.reportFormatSelect")).equals(reportFormat)) {
                                 result = false;
                 }
                 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.reportAppSelect")).equals(applicationName)) {
                                 result = false;
                 }
                 if (!WebdriverWrapper.getSelectedValue(driver,getORPropValue("cem.report.reportBSReportNewSelect")).equals(serviceName))  {
                                 result = false;
                 }
                 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.timeFrameReportNewSelect")).equals(timeFrameOption)) {
                                 result = false;
                 }
                 return result;
 }
 public boolean verifyTrendSaveorScheduleReport (String reportFormat, String applicationName, String serviceName, String currenttimeFrameOption, String previoustimeFrameOption ) throws Exception {
	 
	 
                
					boolean result = true;
					 //sel.click(slmSaveorScheduleReportLink);
					 //sel.waitForPageToLoad(timeout);

					 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.reportFormatSelect")).equals(reportFormat)) {
					                 result = false;
					 }
					 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.reportAppSelect")).equals(applicationName)) {
					                 result = false;
					 }
					 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.reportBSReportNewSelect")).equals(serviceName))  {
					                 result = false;
					 }
					 if (!WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.report.timeFrameReportCurrentSelect")).equals(serviceName))  {

					                 result = false;
					 }
					 if (!WebdriverWrapper.getSelectedValue(driver,getORPropValue("cem.report.timeFrameReportPreviousSelect")).equals(serviceName))  {

					                 result = false;
					 }
					 return result;
				
                 
 }

 /**
  * This method is used to open the defects page in incidents TAB.
  * @param defectType
  */
 public void openDefectsPage(String defectType)
 {
     try{
         WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"), getORPropValue("cem.incidentManagement.defects"));
         WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
         if(WebdriverWrapper.selectList(driver, getORPropValue("cem.incidentManagement.defects.defectTypeSelect"), defectType))
             WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.defectTypeSelect"), defectType);
         else
         {
             WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.defectTypeSelect"), "All");
             WebdriverWrapper.inputText(driver, getORPropValue("cem.incidentManagement.defects.defectNameText"), defectType);
         }

             
         assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.incidentManagement.defects.searchBtn")));
         WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.showSelect"), "All Defects");
         WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
         WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects.searchBtn"));
         WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
     }catch(Exception e){
         e.printStackTrace();
     }
 }
 /**
  * This method is used to open the defectDetails Page
  * @param defectType
  */
 
 public void openDefectDetailsPage(String defectType)
 {
     try{

         openDefectsPage(defectType);
         WebdriverWrapper.click(driver, getORPropValue("cem.incidentManagement.defects.defectList.firstLink"));
         WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
     }catch(Exception e){
             e.printStackTrace();
     }
 }


}
