package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static java.lang.String.format;

public class CemUtil extends JBaseTest
{

    
    
        public CemUtil(WebDriver driver){
        this.driver=driver;
}

    public String getTimProcessID(){
        return util.execUnixCmd(TIM_IP, TIM_REMOTELOGIN, TIM_REMOTEPWD, "pgrep -x tim").trim();
    }
    
    public void setupAppData(String appName, String businessService, String btImportFile){       
        
        try{
            adminBA.createBusinessApplication(appName, appName, "Generic", "Application Specific", true, true, "5", "E-Commerce", "UTF-8", TESS_HOST);
            //createBusinessApplication(appName, appName, "Generic", "Application Specific", true, true, "E-Commerce", "5", "UTF-8");
            String bsImportFile = admin.getTestDataFullPath("GeneralApplication", btImportFile);
            adminBTImport.importZipFileToNewBS(appName, businessService, businessService, bsImportFile);
            util.sleep(10000);
            setupMonitor.syncMonitors();
            admin.enableBusinessServiceMonitoring(businessService);
            setupMonitor.syncMonitors();
        }
        
        catch(Exception e){
            System.out.println("Failed to create Application: "+appName);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public boolean verifyMetricValues(String metric){
        String temp[] = metric.split(",");
        int count = Integer.parseInt(temp[10]);
        int value = Integer.parseInt(temp[12]);
        int min = Integer.parseInt(temp[13]);
        int max = Integer.parseInt(temp[14]);
        System.out.println("Count: "+count);
        System.out.println("Value: "+value);
        System.out.println("Min: "+min);
        System.out.println("Max: "+max);
        
        boolean valid = false;
        if(count == 0){
            if(value == 0 && min == 0 && max == 0)
                valid = true;
        }
        else if(count ==1){
            if( min!=0 && max!=0 && value ==min && min== max)
                valid = true;
        }
        else 
            if( min!=0 && max!=0 && value >=min && value <= max && value >= (min+max)/count)
                valid = true;
        
        return valid;
    }

    public void generateNewDefects(URIBuilder appPageURL) throws URISyntaxException,Exception{
        System.out.println("Generate defects > hit url 10 times");
        for (int i = 0; i <= 10; i++) {
            String appPageURLString = appPageURL.build().toString();
            System.out.println(appPageURLString);
            WebdriverWrapper.navigateToUrl(driver, appPageURLString);
        }
        //the driver object is with appPage. Navigate back to CEM page
        WebdriverWrapper.navigateToUrl(driver, tessUrl);
        logIn();
    }
    
    public void moveBusinessTransactionToBusinessService(String oldBS,String BT,String newBS) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver,getORPropValue("linkText")+oldBS);
        WebdriverWrapper.selectCheckBox(driver,getORPropValue("grid.header.checkbox"));
        WebdriverWrapper.selectBox(driver,getORPropValue("administration.businessservice.tranDefGroupList"),newBS);
        WebdriverWrapper.click(driver, getORPropValue("button.move"));
        if(WebdriverWrapper.isAlertPresent(driver)){
            WebdriverWrapper.selectPopUp(driver, "accept");
        }
    }

    public void setupTim(String timName, String timIP){
        try{
            System.out.println(setupMonitor);
            setupMonitor.createMonitor(timName, timIP, TESS_HOST);
            setupMonitor.enableMonitor(timName);
            //setup.createMonitor(timName, timIP);
            //System.out.println("enabling monitor"+TIM_HOST_NAME);
            //setup.enableMonitor(timName);
        }
        catch(Exception e){
            try{
                setupMonitor.enableMonitor(timName);
            }
            catch(Exception e1){
                e.printStackTrace();
                assertTrue(false); 
            }
           
        }
    }

    public void setupTestData(){
        try{
        setupTim(TIM_HOST_NAME, TIM_IP);
        setupWebFilter.createIPWebServerFilter(MED_REC_HOST_IP, TIM_HOST_NAME, MED_REC_HOST_IP, MED_REC_HOST_IP, Integer.parseInt(MED_REC_HOST_PORT), false);
        setupAppData(appName, businessService, btImportFile);
        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
    public void goToAnalysisGraphForUserGroupandTimeSeries(String newUserGroup, String reportType){
        try{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.analysisgraphs"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.selectList(driver, getORPropValue("cem.analysisgraph.defects.usergroup"), newUserGroup);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.selectList(driver, getORPropValue("cem.analysisgraph.defects.reporttype"), reportType);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
        
    }
    public void goToAnalysisGraphCountForDefect(String appName, String businessService, String bTransactionName, String defectType, String timeFrame){
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.analysisgraphs"),getORPropValue("cem.analysisgraphs.count"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.analysisgraph.count.application"), appName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.analysisgraph.count.service"), businessService);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.analysisgraph.count.transaction"), bTransactionName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver, getORPropValue("cem.analysisgraph.refresh"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.analysisgraph.count.timeframe"), timeFrame);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver, getORPropValue("cem.analysisgraph.refresh"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }  
    }

    public void scheduleNewReport(String reportName, String reportType, boolean schedule, String scheduleHour, String scheduleMinute, String... range ){
        
        try {
            
            WebdriverWrapper.navigateToPage(driver,getORPropValue("home.cem"), getORPropValue("cem.myreports"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.nameEdit"), reportName);
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.reports.reportFormatSelect"),reportType);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            
            WebdriverWrapper.selectBox(driver, getORPropValue("cem.reports.reportTimeRangeSelect"), range[0]);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            
            if(range[0].equals(getORPropValue("cem.reports.reportTimeRangeLastXMinutesOption")))
            {
                WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.reportLastXMinutesEdit"), range[1]);
            }
            else if(range[0].equals(getORPropValue("cem.reports.reportTimeRangeLastNDefectsOption")))
            {
                WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.reportLastNDefectsEdit"), range[1]);
            }
            else {
                //TODO: Implement to handle TIME range of type From and To date 
            }
            if(schedule)
            {
                WebdriverWrapper.click(driver,getORPropValue("cem.reports.schedule"));
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                
                WebdriverWrapper.selectBox(driver,getORPropValue("cem.reports.srHourSelect"), scheduleHour);
                WebdriverWrapper.selectBox(driver,getORPropValue("cem.reports.srMinuteSelect"), ":"+scheduleMinute);
                WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.srFromAddressTxt"), "TechnicalSupport@ca.com");
                WebdriverWrapper.inputText(driver, getORPropValue("cem.reports.srToAddressTxt"), "TechnicalSupport2@ca.com");
                
            }
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }  
    }



    public void setCreateUserGroupsByIPSubnet(boolean enable, String subnetmask){
        int i = enable?1:0;
        if (DB_TYPE.contains("Oracle")){

            dbo.executeUpdateQuery("update ts_domains set ts_new_ip_us_grps_enabled="+i+", ts_subnet_mask='"+subnetmask+"'");
        } else{

            dbp.executeUpdateQuery("update ts_domains set ts_new_ip_us_grps_enabled="+enable+", ts_subnet_mask='"+subnetmask+"'");
        }
    }

    public void syncMonitors(){
        try{
            setupMonitor.syncMonitors();
        }
        catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void startEM(){
        startEM(EM_PORT, true, null);
    }

    /**
     * Starts EM as external process.
     * 
     * @param failWhenRunning Fail the execution in case EM is already running.
     * @param redirectOutputTo The file the output of external process will be redirected to.
     */
    public void startEM(String EM_PORT, boolean failWhenRunning, File redirectOutputTo) {

        if (Util.isPortAvailable(Integer.parseInt(EM_PORT), TESS_HOST)) {
            if (failWhenRunning) {
                throw new IllegalStateException(format(
                        "EM is already running on port %s of host %s", EM_PORT, TESS_HOST));
            }
            System.out.println(format("EM is already running on port %s of host %s", EM_PORT,
                    TESS_HOST));
            return;
        }

        File emExecutableFile = new File(TESS_INSTALLDIR, "Introscope_Enterprise_Manager.exe");
        if (!SystemUtils.IS_OS_WINDOWS) {
            emExecutableFile = new File(TESS_INSTALLDIR, "Introscope_Enterprise_Manager");
        }

        StringBuilder executable = new StringBuilder(64);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable.append("cmd /c ");
        }

        executable.append(emExecutableFile.getAbsolutePath());
        if (redirectOutputTo != null) {
            executable.append(" > ").append(redirectOutputTo.getAbsolutePath());
        }
        
        System.out.println(executable.toString());

        Util.startEM(TESS_HOST, EM_PORT, executable.toString());
    }


    public void waitFor(String sleep)
    {
       util.sleep(Long.parseLong(sleep));
    }
    
    public void goToBTComponentIdentificationParams(String businessService, String bTransactionName){
        try{
        String transactionsTableID     = "tranUnitDef";
        String componentsTableID       = "tranCompDef";        
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        WebdriverWrapper.navigateToPage(driver, getORPropValue("linkText")+businessService,getORPropValue("linkText")+bTransactionName);
        //String link = admin.getAttributeFromCell(transactionsTableID, 1, 2, "/a/@href");
        String link = admin.getAttributeFromCell(transactionsTableID, 1, 2, "/a/@href");
        
        String baseLink=WebdriverWrapper.getURL(driver);
        baseLink = baseLink.substring(0, baseLink.lastIndexOf("/")+1);
        WebdriverWrapper.navigateToUrl(driver, baseLink+link);
        //link = admin.getAttributeFromCell(componentsTableID, 1, 2, "/a/@href");
        link = admin.getAttributeFromCell(componentsTableID, 1, 2, "/a/@href");
        
        baseLink=WebdriverWrapper.getURL(driver);
        baseLink = baseLink.substring(0, baseLink.lastIndexOf("/")+1);
        WebdriverWrapper.navigateToUrl(driver, baseLink+link);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            assertTrue(false);
        }  
    }

    public void goToAnalysisGraphCountForDefect(String appName, String businessService, String bTransactionName, String timeFrame){
        try{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.analysisgraphs"),getORPropValue("cem.analysisgraphs.count"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        
        WebdriverWrapper.selectBox(driver, "cem.analysisgraph.count.application", appName);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.selectBox(driver, "cem.analysisgraph.count.service", businessService);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.selectBox(driver, "cem.analysisgraph.count.transaction", bTransactionName);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, "cem.analysisgraph.refresh");
        WebdriverWrapper.selectBox(driver,"cem.analysisgraph.count.timeframe", timeFrame);
        WebdriverWrapper.click(driver, "cem.analysisgraph.refresh");
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(false);
        }       
    }

    public long getDefectCountForIncident(String appName, String defectType) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.incidents.ba"), appName);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        WebdriverWrapper.click(driver,getORPropValue("button.refresh"));
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        int row = admin.getRowNumByContentAndColTitle(reports.inTableID, defectType, reports.defectDefectNameTH);
        return Long.parseLong(admin.getCellContentsByRowAndCol(reports.inTableID, row, reports.inDefectsTH));
    }
    
    public String getIncidentImpactLevel(String appName, String defectType) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.selectList(driver, getORPropValue("cem.incidentManagement.incidents.ba"), appName);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        WebdriverWrapper.click(driver,getORPropValue("button.refresh"));
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        int row = admin.getRowNumByContentAndColTitle(reports.inTableID, defectType, reports.defectDefectNameTH);
        return admin.getAttributeFromCell(reports.inTableID, row, impactLevelTHCol, "/img/@alt");
    }
    
    public long getIncidentBusinessImpact(String appName, String defectType) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.incidents.ba"), appName);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        WebdriverWrapper.click(driver,getORPropValue("button.refresh"));
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        int row = admin.getRowNumByContentAndColTitle(reports.inTableID, defectType, reports.defectDefectNameTH);
        return Long.parseLong(admin.getCellContentsByRowAndCol(reports.inTableID, row, businessImpactTHCol).replace(",", ""));
    }
    public int getTTCountForIncidentId(String incidentIdLink) throws Exception{
        //int ttCount=0;
        logIn();
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.click(driver,incidentIdLink);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        
        return Integer.parseInt(WebdriverWrapper.getElementText(driver,getORPropValue("cem.incidentManagement.ttCountXpath")));
    }
    
    public void startTTforIncidentWithID(String incidentIdLink) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.click(driver,incidentIdLink);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        
        if(WebdriverWrapper.isObjectPresent(driver,getORPropValue("cem.incidentManagement.incidents.inStartTransactionTraceButton"))){
            WebdriverWrapper.click(driver,getORPropValue("cem.incidentManagement.incidents.inStartTransactionTraceButton"));
            WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        }       
    }
    
    public void stopTTforIncidentWithID(String incidentIdLink) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.click(driver,incidentIdLink);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        if(WebdriverWrapper.isObjectPresent(driver,getORPropValue("cem.incidentManagement.incidents.inStopTransactionTraceButton"))){
            WebdriverWrapper.click(driver,getORPropValue("cem.incidentManagement.incidents.inStopTransactionTraceButton"));
            WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        }       
    }
    
    
    public boolean isIncidentOpen(String bApplicationName, String defectType) throws Exception{
        boolean isIncidentOpen = false;
        logIn();
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),getORPropValue("cem.incidentmanagemnt"),getORPropValue("cem.incidentManagement.incidents"));
        WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.incidents.ba"), bApplicationName);
        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
        System.out.println("Selected business application: "+WebdriverWrapper.getSelectedValue(driver, getORPropValue("cem.incidentManagement.incidents.ba")));
        //TODO change timeout variable's datatype to int
        

        WebdriverWrapper.click(driver, getORPropValue("button.refresh"));
        Thread.sleep(30000);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        if(admin.getRowNumByContentAndColTitle(reports.inTableID, defectType, reports.defectDefectNameTH)>0)    
        {
            isIncidentOpen=true;
            System.out.println("Incident created for defect "+defectType);
        }
        
        return isIncidentOpen;
    }
    
    public void generateNewDefectsTT(String sessionIdParamName, URIBuilder... appPageURLs) throws Exception {
        
        for (int i = 0; i <= 10; i++) {
        //TessLogin.sendHttpRequest(MED_REC_PAGE);
            for (URIBuilder appPageURL : appPageURLs) {
                appPageURL.setParameter(sessionIdParamName, Integer.toString(i));
                String appPageURLString = appPageURL.build().toString();
                System.out.println(appPageURLString);
                WebdriverWrapper.navigateToUrl(driver, appPageURLString);
            }
            util.sleep(1000);
        }
        logIn();
    }
    
    public void hitHttpPageForQuery(String parameter,String user){ //parameter is the user identification parameter name
        util.sleep(2000);
        util.hitHttpPage(MED_REC_BASE_URL+appURL+"?"+parameter+"="+user);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    }
    
        
    public String getValueFromTable(String tableXPath, int targetCol, int searchCol, String searchString)throws Exception{
        int row = 0;
        //div id="reportDiv"/table/
        //table[contains(@class, 'gridTable2 gridTable')]
        row = tim.getRowNumByContentAndColumn(tableXPath, searchString, searchCol);        
        return tim.getCellContentsByRowAndCol(tableXPath, row, targetCol);
    }
    
}
