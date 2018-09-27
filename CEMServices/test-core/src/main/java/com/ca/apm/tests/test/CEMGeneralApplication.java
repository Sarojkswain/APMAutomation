package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 * Setup applications, business transactions and generate defects for general purpose.
 * 
 */
public class CEMGeneralApplication extends JBaseTest {
	
	private CemUtil cemutil = new CemUtil(driver);
	private CEMAdministration admin = new CEMAdministration(driver);
	private String testSuiteTestDataFullPath;
	private String PETSHOP_HOSTIP = environmentConstants.getString("petShopHostIp");

	public String MEDREC_REQ_INDEX 			= getTestDataValue("MEDREC_REQ_INDEX");
	public String MEDREC_REQ_START			= getTestDataValue("MEDREC_REQ_START");
	
	public String MEDREC_REQ_ADMIN			= getTestDataValue("MEDREC_REQ_ADMIN");
	public String MEDREC_REQ_PATIENT 		= getTestDataValue("MEDREC_REQ_PATIENT");
	public String MEDREC_REQ_PHYSICIAN 		= getTestDataValue("MEDREC_REQ_PHYSICIAN");
	public String MEDREC_REQ_ADMIN_GROUP 	= getTestDataValue("MEDREC_REQ_ADMIN_GROUP");
	public String MEDREC_REQ_PATIENT_GROUP 	= getTestDataValue("MEDREC_REQ_PATIENT_GROUP");
	public String MEDREC_REQ_PHYSICIAN_GROUP = getTestDataValue("MEDREC_REQ_PHYSICIAN_GROUP");
	public String MEDREC_REQ_ADMIN_NAME 	= getTestDataValue("MEDREC_REQ_ADMIN_NAME");
	public String MEDREC_REQ_PATIENT_NAME 	= getTestDataValue("MEDREC_REQ_PATIENT_NAME");
	public String MEDREC_REQ_PHYSICIAN_NAME  = getTestDataValue("MEDREC_REQ_PHYSICIAN_NAME");

	public String PETSHOP_REQ_HOME 			=  getTestDataValue("PETSHOP_REQ_HOME");
	public String PETSHOP_REQ_CATEGORY 		=  getTestDataValue("PETSHOP_REQ_CATEGORY");
	public String PETSHOP_REQ_ITEMS		    =  getTestDataValue("PETSHOP_REQ_ITEMS");
	public String PETSHOP_REQ_ITEMDETAILS 	=  getTestDataValue("PETSHOP_REQ_ITEMDETAILS");
	public String PETSHOP_REQ_SHOPPINGCART   =  getTestDataValue("PETSHOP_REQ_SHOPPINGCART");

	public String PETSHOP_REQ_CHECKOUT 		=  getTestDataValue("PETSHOP_REQ_CHECKOUT");
	public String PETSHOP_REQ_SIGNIN		=  getTestDataValue("PETSHOP_REQ_SIGNIN");
	public String PETSHOP_REQ_ORDERBILLING   =  getTestDataValue("PETSHOP_REQ_ORDERBILLING");
	public String PETSHOP_REQ_ORDERPROCESS   =  getTestDataValue("PETSHOP_REQ_ORDERPROCESS");
	public String PETSHOP_REQ_SIGNOUT 		=  getTestDataValue("PETSHOP_REQ_SIGNOUT");

	//private String TIM_IP = environmentConstants.getString("timIp");

	//private PostgresUtil db = new PostgresUtil(TESS_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
	
	public static String testSuiteNameDefault = "GeneralApplication";
	public String testSuiteName = testSuiteNameDefault;
	public String testSuiteApplicationName1 = testSuiteName + "1 Medrec";
	public String testSuiteApplicationName2 = testSuiteName + "2 MSpetshop";
	
	public static final String defaultApplicationName = "Default Application";
	public String testSuiteWebFilterName = testSuiteName + " Web Filter";
	
	public String testSuiteServiceName11 = testSuiteName + " Service11";
	public String testSuiteServiceName12 = testSuiteName + " Service12";
	public String testSuiteServiceName21 = testSuiteName + " Service21";
	public String testSuiteServiceName22 = testSuiteName + " Service22";
	public String testSuiteServiceName23 = testSuiteName + " Service23";
	
	public String testSuiteServiceName15 = testSuiteName + " Service15";
	public String testSuiteServiceName16 = testSuiteName + " Service16";
	public String testSuiteServiceName25 = testSuiteName + " Service25";
	public String testSuiteServiceName26 = testSuiteName + " Service26";
	
	public String testSuiteSessionName = testSuiteName + " Recording Session";
	public String testSuiteTransactionName = testSuiteName + " Transaction";	
	
	public String businessTransactionNameMEDPhysician = "MED Physician Login page";	
	public String businessTransactionNameMEDPatient = "MED Patient Login page";
	public String transactionNamePhysicianLogin = "physician/login.do";
	public String transactionNameStartJSP = "start.jsp";
	
	public String testSuitePlugin = testSuiteName + " Plugins - Patient";
	public String businessTransactionNameMEDPatietPlugin = businessTransactionNameMEDPatient + " Plugin";

	    public CEMGeneralApplication (){
			//empty constructor
	    }
		
		public CEMGeneralApplication(WebDriver driver){
			this.driver = driver;
	    }
		
	    public CEMGeneralApplication (WebDriver driver, String testSuiteName){
	       this.driver = driver;
	       try
           {
	           super.logIn();
           }
	       catch (Exception e)
           {
	           e.printStackTrace();
           }
	       updateTestSuiteName(testSuiteName);
	    }
	
	    /**
	     * 
	     * @param aTestSuiteName
	     */
    	private void updateTestSuiteName (String aTestSuiteName) {
    		testSuiteName = aTestSuiteName;
    
    		testSuiteWebFilterName = testSuiteName + " Web Filter";
    
    		testSuiteApplicationName1 = testSuiteName + "1 Medrec";
    		testSuiteApplicationName2 = testSuiteName + "2 MSpetshop";
    		testSuiteServiceName11 = testSuiteName + " Service11";
    		testSuiteServiceName12 = testSuiteName + " Service12";
    		testSuiteServiceName21 = testSuiteName + " Service21";
    		testSuiteServiceName22 = testSuiteName + " Service22";
    		testSuiteServiceName23 = testSuiteName + " Service23";
    
    		testSuiteSessionName = testSuiteName + " Recording Session";
    		
    		testSuiteServiceName15 = testSuiteName + " Service15";
    		testSuiteServiceName16 = testSuiteName + " Service16";
    		testSuiteServiceName25 = testSuiteName + " Service25";
    		testSuiteServiceName26 = testSuiteName + " Service26";
    	}

    	/**
    	 * Creates applications, business transactions, and generates defects
    	 * @throws Exception
    	 */
	   public void setupAppBSTSDefects() throws Exception{
	       
	        db.setUpgradeToTimMonitoring(true); 
	        logIn();
	        WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
	        
	        createAppBSTS();  
	        setup.setNumDefects(1);
	        
	        generateDefects();
	        recordCurrentTime(testSuiteName);
	        
	        setup.synchronizeAllMonitors();
	        logOut();       

	        System.out.println("Created applications, business transactions, and generated defects. ");
	    }
	   
	   /**
	    * Deletes Business applications, Tim and all the test files
	    * @throws Exception
	    */
	   public void cleanup() throws Exception{

	        admin.deleteApplication(testSuiteApplicationName1);
	        admin.deleteApplication(testSuiteApplicationName2);
	        setup.deleteTim(testSuiteName);
	        setup.synchronizeAllMonitors();
	        
	        try {
	            deleteTestFiles();
	        } catch (InterruptedException e) {
	            System.out.println("Got exception while run deleteTestFiles");
	            e.printStackTrace();
	        } catch (IOException e) {
	            System.out.println("Got exception while run deleteTestFiles");
	            e.printStackTrace();
	        }
	        System.out.println("cleanup - Done.");
	    }
	
	   /**
	    * Deletes test files
	    * @throws InterruptedException
	    * @throws IOException
	    */
    private void deleteTestFiles() throws InterruptedException, IOException {

	        String cmdDelete = "cmd /c del "+testSuiteTestDataFullPath+"cem_autotest_log_grep*.txt";
	        driver.wait(iGlobalTimeout);
	        Runtime.getRuntime().exec(cmdDelete);
	        driver.wait(iGlobalTimeout);
	    }
    
    /**
     * Records the current time
     * @param testSuiteName
     */
	public void recordCurrentTime(String testSuiteName) {
		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
		Calendar calendar = Calendar.getInstance();
		String timeStr = simpleDateFormat.format(calendar.getTime());
		
		testSuiteTestDataFullPath = file.getTestSuiteTestDataFullPath(testSuiteName);
		file.writeStringToFile(testSuiteTestDataFullPath+"DefectGenerationEndTime.txt", timeStr);  ////may read from properties.
		System.out.println("recordCurrentTime - Done.");
	}
	
	/**
	 * Generates new defect	
	 * @param bTransactionName
	 * @throws Exception
	 */
	public void generateNewDefect(String bTransactionName) throws Exception{
        //go to Defects page, count defects existed.
        int itemFound = -1;
        itemFound = reports.getSearchResultCountDefectShowAll();

        //send request
        String MED_REC_PAGE = "http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+bTransactionName;
        int repeat = 5;
        for (int i=1; i<=repeat; i++) {
            BaseSharedObject.sendHttpRequest(MED_REC_PAGE);
            driver.wait(iGlobalTimeout);
        }
        driver.wait(iGlobalTimeout);
         
        
        //go to Defects page, count defects current.
        int itemFoundCurrent = -1;
        itemFoundCurrent = reports.getSearchResultCountDefectShowAll();
        System.out.println(itemFoundCurrent+">"+itemFound);     
        if (itemFoundCurrent==itemFound) {
            System.out.println("**Did not find latest generated defects, may check from UI to make sure system works fine on defect generation.**");
            System.out.println("**Automation tests continue...**");
        }
    }
	
	/**
	 * Generates new defects for a particular Business service and Business transaction
	 * @param bServiceName
	 * @param bTransactionName
	 * @throws Exception
	 */
	public void generateNewDefectsForBS(String bServiceName, String bTransactionName) throws Exception{
		setup.synchronizeAllMonitors();
		admin.enableDisableAllBusinessTransactions(bServiceName, true);
		setup.synchronizeAllMonitors();
		generateNewDefect(bTransactionName);
		admin.enableDisableAllBusinessTransactions(bServiceName, false);
		setup.synchronizeAllMonitors();
	}
		
	/**
	 * Generates defects
	 * @throws Exception
	 */
	public void generateDefects () throws Exception {
		//setup defects data
		admin.enableDisableAllBusinessTransactions(testSuiteServiceName11, false);
		admin.enableDisableAllBusinessTransactions(testSuiteServiceName12, false);
		admin.enableDisableAllBusinessTransactions(testSuiteServiceName21, false);
		admin.enableDisableAllBusinessTransactions(testSuiteServiceName22, false);
		admin.enableDisableAllBusinessTransactions(testSuiteServiceName23, false);
		
		generateNewDefectsForBS(testSuiteServiceName11, transactionNamePhysicianLogin);
		generateNewDefectsForBS(testSuiteServiceName21, transactionNamePhysicianLogin);
		generateNewDefectsForBS(testSuiteServiceName23, transactionNamePhysicianLogin);
		System.out.println("setupBSDefectsData - Done.");
	}
	
	/**
	 * Creates Applications, Business Services and imports service txns
	 */
	private void createAppBSTS(){
		try{
		//Create Applications
		admin.createBusinessApplication(testSuiteApplicationName1, testSuiteApplicationName1, "Generic", "Application Specific", true, true, "Enterprise", "20", "UTF-8");
		admin.addUserIdParamToApplication(testSuiteApplicationName1, "Query", "Name");
		admin.addUserGroupIdParamToApplication(testSuiteApplicationName1, "Query", "Group");
		admin.createBusinessApplication(testSuiteApplicationName2, testSuiteApplicationName2, "Generic", "Application Specific", true, true, "Enterprise", "20", "UTF-8");
		admin.addUserIdParamToApplication(testSuiteApplicationName2, "Query", "categoryId");
		
		//Create BusinessServices
		admin.createBusinessService(testSuiteServiceName12, testSuiteServiceName12, testSuiteApplicationName1, true, "");
		admin.createBusinessService(testSuiteServiceName22, testSuiteServiceName22, testSuiteApplicationName2, true, "");
		
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName11, testSuiteServiceName11, admin.getTestDataFullPath("GeneralApplication", "BTExport_CEM_AutoTest_INTG_BS_Import.zip"));
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName21, testSuiteServiceName21, admin.getTestDataFullPath("GeneralApplication", "BTExport_CEM_AutoTest_INTG_BS_Import.zip"));
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName23, testSuiteServiceName23, admin.getTestDataFullPath("GeneralApplication", "BTExport_CEM_AutoTest_INTG_BS_Import.zip"));
		setupMonitor.syncMonitors();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Creates Applications, Business Services and imports service txns
	 * @throws Exception
	 */
	private void createAppBSTS2() throws Exception{
		//Create Applications
		admin.createBusinessApplication(testSuiteApplicationName1, testSuiteApplicationName1, "Generic", "Application Specific", true, true, "Enterprise", "20", "UTF-8");
		admin.addUserIdParamToApplication(testSuiteApplicationName1, "Query", "Name");
		admin.addUserGroupIdParamToApplication(testSuiteApplicationName1, "Query", "Group");
		admin.createBusinessApplication(testSuiteApplicationName2, testSuiteApplicationName2, "Generic", "Application Specific", true, true, "Enterprise", "20", "UTF-8");
		admin.addUserIdParamToApplication(testSuiteApplicationName2, "Query", "categoryId");
		
		adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName15, testSuiteServiceName15, admin.getTestDataFullPath("GeneralApplication", "BTExport_GeneralApplication_Service15_BS_Import.zip"));
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName16, testSuiteServiceName16, admin.getTestDataFullPath("GeneralApplication", "BTExport_GeneralApplication_Service16_BS_Import.zip"));
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName25, testSuiteServiceName25, admin.getTestDataFullPath("GeneralApplication", "BTExport_GeneralApplication_Service25_BS_Import.zip"));
        adminBTImport.importZipFileToNewBS(appName, testSuiteServiceName26, testSuiteServiceName26, admin.getTestDataFullPath("GeneralApplication", "BTExport_GeneralApplication_Service26_BS_Import.zip"));
		setupMonitor.syncMonitors();
		System.out.println("createAppBSTS2 - Done.");
	}

	/**
	 * Generates defects
	 * @throws Exception
	 */
	   public void generateDefects2 () throws Exception {

	        int cycle = 5;
	        int repeat = 7;
	        //long wait = 500L;
	        for (int i=1; i<=cycle; i++) {
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+MEDREC_REQ_INDEX);
	            BaseSharedObject.repeatSendHttpRequest(repeat,"http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+MEDREC_REQ_START);
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+MEDREC_REQ_ADMIN+"?Name="+MEDREC_REQ_ADMIN_NAME+"&Group="+MEDREC_REQ_ADMIN_GROUP);
	            BaseSharedObject.repeatSendHttpRequest(repeat,"http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+MEDREC_REQ_PATIENT+"?Name="+MEDREC_REQ_PATIENT_NAME+"&Group="+MEDREC_REQ_PATIENT_GROUP);
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+MED_REC_HOST_IP+":"+MED_REC_HOST_PORT+"/"+MEDREC_REQ_PHYSICIAN+"?Name="+MEDREC_REQ_PHYSICIAN_NAME+"&Group="+MEDREC_REQ_PHYSICIAN_GROUP);

	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_HOME);
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_CATEGORY+"?categoryId=FISH$i");
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_ITEMS+"?productId=FI-FW-01");
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_ITEMDETAILS);
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_SHOPPINGCART);

	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_CHECKOUT);
	            BaseSharedObject.repeatSendHttpRequest(repeat,"http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_SIGNIN );
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_ORDERBILLING);
	            BaseSharedObject.repeatSendHttpRequest(repeat,"http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_ORDERPROCESS );
	            BaseSharedObject.repeatSendHttpRequest(repeat, "http://"+PETSHOP_HOSTIP+"/"+PETSHOP_REQ_SIGNOUT);
	            
	            System.out.println("Send requests - cycle["+(i)+"].");
	        }
	        admin.enableDisableAllBusinessTransactions(false);
	        System.out.println("generateDefects2 - Done.");
	    }
	
	   /**
	    * Creates applications, business services, imports Txns and generates defects
	    * @throws Exception
	    */
	   public void setupAppBSTSDefects2() throws Exception{
	        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);    
	        createAppBSTS2();  
	        try {
	            generateDefects2();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        recordCurrentTime(testSuiteName);
	        setup.synchronizeAllMonitors();
	        logOut();       

	        System.out.println("setupAppBSTSDefects2 - Done.");
	    }

	}
