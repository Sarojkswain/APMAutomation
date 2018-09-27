package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.CEMChromeTestbed;
import com.ca.apm.tests.utility.OracleUtil;
import com.ca.apm.tests.utility.PostgresUtil;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_ID;
public class DefectProcessing extends JBaseTest {

	public String timeLineHeader = "";
	final long AggrtimeOut = 4000000;
	final long timeOut = 10000;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefectProcessing.class);
	public WebDriver driver;
	public String Verify_ALM_351159_x46_btImportFile = getTestDataValue("Verify_ALM_351159_BKPR46_btImportFile");
	protected BufferedWriter writer = null;
    String scriptName = "sh "+getEnvConstValue("ssh_scriptName") + " ";

	@BeforeTest(alwaysRun = true)
	public void initialize() {
		System.out
				.println("**********Initializing in Defect Processing*******");
		try {
			super.initialize();
			String testCaseName = "verify_ALM_351147_INCM21";
			LOGGER.info("Setup Monitor : " + setupMonitor);

			setupMonitor.createMonitor(TIM_HOST_NAME, TIM_IP, TESS_HOST);
			setupMonitor.enableMonitor(TIM_HOST_NAME);
			logIn();

			System.out.println("Current URL@@@@@@"
					+ super.driver.getCurrentUrl());

			setupAppData("medrec_All_BA", "medrec_All_BS",
					getTestDataValue("defectProcessorImportFile"));
			setupAppData(
					testCaseName + "_MissingResponse_BA",
					testCaseName + "_MissingResponse_BS",
					getTestDataValue("verify_ALM_351147_INCM21_MissingResponse_btImportFile"));
			setupAppData(
					testCaseName + "_ClientRequestError_BA",
					testCaseName + "_ClientRequestError_BS",
					getTestDataValue("verify_ALM_351147_INCM21_ClientRequestError_btImportFile"));
			setupMonitor.syncMonitors();
			this.generateDefectsForDefectProcessor();

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		System.out
				.println("**********Successful Initializing in Defect Processing Completed*******");
	}
 
	@Tas(testBeds = @TestBed(name = CEMChromeTestbed.class, executeOn = EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_257487_txBD01() {
		String testCaseName = "verify_ALM_257487_txBD01";
		setupTest(testCaseName);
		startTestCaseName(testCaseName);

		try {
			reports.openDefectDetailsPage("Slow Time");
			driver.navigate().refresh();
			Thread.sleep(30000);
			driver.navigate().refresh();
			assertTrue(WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.timeLineImg")));
			LOGGER.info("The value is "
					+ WebdriverWrapper
							.getElementText(
									driver,
									getORPropValue("cem.incidentManagement.defects.defectList.defectValueInTbl")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_342131_CPLT91A() {
		setupTest("verify_ALM_342131_CPLT91A");
		String testCaseName = "verify_ALM_342131_CPLT91A";
		startTestCaseName(testCaseName);

		try {
			reports.openDefectsPage("Slow Time");
			Thread.sleep(30000);
			if (WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.firstLink"))) {
				LOGGER.info("Hurray ... There are defects present");
				assertTrue(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_257489_txBD03() {
		setupTest("verify_ALM_257489_txBD03");
		String testCaseName = "verify_ALM_257489_txBD03";
		startTestCaseName(testCaseName);

		try {
			reports.openDefectDetailsPage("Slow Time");

			timeLineHeader = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.timeLineHeader"));
			LOGGER.info("The time line header is " + timeLineHeader);
			assertTrue(timeLineHeader.contains("Time Line"));
			assertTrue(timeLineHeader.contains("ms"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_257490_257497_txBD04() {
		setupTest("verify_ALM_257490_257497_txBD04");
		String testCaseName = "verify_ALM_257490_257497_txBD04";
		startTestCaseName(testCaseName);
		String defectValue = "";

		try {
			reports.openDefectDetailsPage("Slow Time");
			defectValue = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.defectValueInTbl"));
			timeLineHeader = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.timeLineHeader"));

			LOGGER.info("The value is " + defectValue);
			LOGGER.info("Actual defect value is "
					+ defectValue.charAt(defectValue.length() - 2));
			LOGGER.info(timeLineHeader);
			LOGGER.info("Time Line header "
					+ timeLineHeader.charAt(timeLineHeader.length() - 5));
			assertEquals(defectValue.charAt(defectValue.length() - 2),
					timeLineHeader.charAt(timeLineHeader.length() - 5));

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_257491_txBD05() {
		setupTest("verify_ALM_257491_txBD05");
		String testCaseName = "verify_ALM_257491_txBD05";
		startTestCaseName(testCaseName);

		try {
			reports.openDefectDetailsPage("Slow Time");
			assertTrue(WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.timeLineImg")));
			LOGGER.info("The value is "
					+ WebdriverWrapper
							.getElementText(
									driver,
									getORPropValue("cem.incidentManagement.defects.defectList.defectValueInTbl")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_257495_TxBD09() {
		setupTest("verify_ALM_257495_TxBD09");
		String testCaseName = "verify_ALM_257495_TxBD09";
		startTestCaseName(testCaseName);

		try {
			reports.openDefectDetailsPage("Slow Time");
			assertTrue(WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.legend.timeToFirstResponse")));
			assertTrue(WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.legend.timeToResponseTransfer")));
			WebdriverWrapper
					.verifyTextPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.legend.timeToFirstResponseText"),
							"Time to First Response");
			WebdriverWrapper
					.verifyTextPresent(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.legend.timeToResponseTransferText"),
							"Response Transfer Time");
		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_351144_INCM16() {

		setupTest("verify_ALM_351144_INCM16");
		String testCaseName = "verify_ALM_351144_INCM16";
		/**
		 * Defect Type is a column header which should not be present.
		 * defectTypePresent is the boolean value to verify that.
		 * 
		 */
		boolean defectTypePresent = true;
		try {
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"));
			LOGGER.info(WebdriverWrapper.getElementText(driver,
					getORPropValue("cem.incidentManagement.bizEvent")
							+ "/thead/tr/th[2]"));

			for (int i = 2; i < 16; i++) {
				if (WebdriverWrapper.getElementText(
						driver,
						getORPropValue("cem.incidentManagement.bizEvent")
								+ "/thead/tr/th[" + i + "]").equalsIgnoreCase(
						"Defect type")) {
					defectTypePresent = false;
					break;
				}
			}
			assertTrue(defectTypePresent);

			/**
			 * Goto the Incidents page and click on Incident ID
			 * 
			 */
			WebdriverWrapper
					.navigateToPage(
							driver,
							getORPropValue("home.cem"),
							getORPropValue("cem.incidentmanagemnt"),
							getORPropValue("cem.incidentManagement.incidents"),
							getORPropValue("cem.incidentManagement.incidents.firstLink"));
			/**
			 * Defect Type is a column header which should not be present.
			 * defectTypePresent is the boolean value to verify that.
			 */
			for (int i = 2; i < 14; i++) {
				if (WebdriverWrapper.getElementText(
						driver,
						"xpath_//*[@id='formDiv']/table/tbody/tr[1]/td[1]/table/tbody/tr["
								+ i + "]/td[1]")
						.equalsIgnoreCase("Defect type")) {
					defectTypePresent = false;
					break;
				}
			}
			assertTrue(defectTypePresent);

			for (int i = 15; i < 19; i++) {
				if (WebdriverWrapper.getElementText(
						driver,
						"xpath_//*[@id='formDiv']/table/tbody/tr[1]/td[1]/table/tbody/tr["
								+ i + "]/td[1]")
						.equalsIgnoreCase("Defect type")) {
					defectTypePresent = false;
					break;
				}
			}
			assertTrue(defectTypePresent);

			for (int i = 21; i < 25; i++) {
				if (WebdriverWrapper.getElementText(
						driver,
						"xpath_//*[@id='formDiv']/table/tbody/tr[1]/td[1]/table/tbody/tr["
								+ i + "]/td[1]")
						.equalsIgnoreCase("Defect type")) {
					defectTypePresent = false;
					break;
				}
			}
			assertTrue(defectTypePresent);

			/**
			 * 
			 * Goto defect details page Defect Type is a column header which
			 * should not be present. defectTypePresent is the boolean value to
			 * verify that.
			 */

			reports.openDefectDetailsPage("Slow Time");

			for (int i = 2; i < 7; i++) {
				if (WebdriverWrapper.getElementText(
						driver,
						"xpath_//*[@id='formDiv']/table[1]/tbody/tr[1]/td[1]/table/tbody/tr["
								+ i + "]/td[1]")
						.equalsIgnoreCase("Defect type")) {
					defectTypePresent = false;
					break;
				}
			}
			assertTrue(defectTypePresent);

		} catch (Exception e) {
			e.printStackTrace();
		}

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351146_INCM20() {
		setupTest("verify_ALM_351146_INCM20");
		String testCaseName = "verify_ALM_351146_INCM20";
		String dataInTable = "";

		/**
		 * Defect Type is a column header which should not be present.
		 * defectTypePresent is the boolean value to verify that.
		 */
		boolean dataPresent = true;
		try {
			WebdriverWrapper
					.navigateToPage(
							driver,
							getORPropValue("home.cem"),
							getORPropValue("cem.incidentmanagemnt"),
							getORPropValue("cem.incidentManagement.incidents"),
							getORPropValue("cem.incidentManagement.incidents.firstLink"),
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink"));
			WebdriverWrapper
					.click(driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink.clientTierTableDataLink"));
			WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink.clientTierTableDataLink.data"));
			dataInTable = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink.clientTierTableDataLink.data"));
			LOGGER.info(dataInTable);

			if (dataInTable.equalsIgnoreCase(""))
				dataPresent = false;

			assertTrue(dataPresent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_351151_INCM47() {
		setupTest("verify_ALM_351151_INCM47");
		String testCaseName = "verify_ALM_351151_INCM47";
		String dataInTable = "";

		/**
		 * Defect Type is a column header which should not be present.
		 * defectTypePresent is the boolean value to verify that.
		 */
		boolean dataPresent = false;
		try {
			WebdriverWrapper
					.navigateToPage(
							driver,
							getORPropValue("home.cem"),
							getORPropValue("cem.incidentmanagemnt"),
							getORPropValue("cem.incidentManagement.incidents"),
							getORPropValue("cem.incidentManagement.incidents.firstLink"),
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink"));
			WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink.DefectCountByWebServerIPAddress"));
			dataInTable = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink.DefectCountByWebServerIPAddress"));
			LOGGER.info(dataInTable);

			if (dataInTable.contains("IP"))
				dataPresent = true;

			assertTrue(dataPresent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_351159_BKPR46() {
		setupTest("verify_ALM_351159_BKPR46");
		String testCaseName = "verify_ALM_351159_BKPR46";
		String adminLoginurlwithQuery = MED_REC_BASE_URL
				+ getTestDataValue("medRec.admin.loginPage")
				+ "\\?userName=\"User1\"\\&groupId=\"Group1\"";
		String count = " 10";
		generateData(scriptName + adminLoginurlwithQuery + count);

		this.setupAppData("verify_ALM_351159_BKPR46_BA",
				"verify_ALM_351159_BKPR46_BS",
				getTestDataValue("Verify_ALM_351159_BKPR46_btImportFile"));

		try {
			/**
			 * Select all defects for the above BA, BS
			 */
			Thread.sleep(timeOut);
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));
			admin.selectTransactionInformation("medrec_All_BA",
					"medrec_All_BS", "All");
			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");
			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));

			if (WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.secondColumn"))
					.equalsIgnoreCase("medrec_All_BS")) {
				System.out.println("This is Successful");
				assertTrue(true);
			} else
				assertTrue(false);

		} catch (Exception e) {
			e.printStackTrace();
		}

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_351161_BKPRS() {
		setupTest("verify_ALM_351161_BKPRS");
		String testCaseName = "verify_ALM_351161_BKPRS";
		String URL = getTestDataValue("verify_ALM_351161_BKPRS_URL");
		String count = " 50";

		this.setupAppData(testCaseName + "_BA", testCaseName + "_BS",
				getTestDataValue(testCaseName + "_btImportFile"));

		try {
			WebdriverWrapper.navigateToPage(driver,
					getORPropValue("home.administration"),
					getORPropValue("administration.businessapplication"));
			/**
			 * Add post user parameter to the Application created. Take the
			 * table ID, no of rows, run through the loop until the app is
			 * found.
			 */
			LOGGER.info("Begin Application creation");
			String table = "//*[@id='appdef']/tbody/tr";
			// int size = driver.findElements(By.xpath(table)).size();
			int size = WebdriverWrapper.getXpathCount(driver, "xpath_" + table);
			for (int i = 1; i <= size; i++) {
				if ((WebdriverWrapper.getElementText(driver, "xpath_" + table
						+ "[" + i + "]/td[2]/a")).trim().equalsIgnoreCase(
						(testCaseName + "_BA").trim())) {
					WebdriverWrapper.click(driver, "xpath_" + table + "[" + i
							+ "]/td[2]/a");
					break;
				} else
					LOGGER.info("Business Application not found");
			}

			/**
			 * //*[@name='userIdentification']. Create user identification for
			 * application.
			 * 
			 */

			WebdriverWrapper
					.click(driver,
							getORPropValue("administration.businessapplication.app.useridentification"));// "xpath_//*[@name='userIdentification']");
			if (!WebdriverWrapper
					.isObjectPresent(
							driver,
							getORPropValue("administration.businessapplication.app.loginNameParam"))) {// "xpath_//*[@id='loginNameParam']"
				WebdriverWrapper
						.click(driver,
								getORPropValue("administration.businessapplication.app.newParameterGroup"));// xpath_//*[@value='New
				// Parameter
				// Group']
				WebdriverWrapper
						.selectBox(
								driver,
								getORPropValue("administration.businessapplication.app.loginNameType"),
								"Post");// id_key.type1
				WebdriverWrapper.inputText(driver, "id_key.name1", "user");
				WebdriverWrapper
						.click(driver,
								getORPropValue("administration.businessapplication.app.useridentification.save"));
			}

			else if (WebdriverWrapper
					.getElementText(driver,
							"xpath_//*[@id='loginNameParam']/tbody/tr/td[2]/a")
					.trim().equalsIgnoreCase("user"))
				LOGGER.info("The identifier already exists");

			LOGGER.info("End Application creation");

			WebdriverWrapper.navigateToPage(driver,
					getORPropValue("home.setup"),
					getORPropValue("setup.monitors"));
			WebdriverWrapper.click(driver,
					getORPropValue("setup.monitors.syncMonitorsButton"));

			setupMonitor.syncMonitors();

			generateData(scriptName + URL + count);

			Thread.sleep(timeOut * 2);

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));
			admin.selectTransactionInformation(testCaseName + "_BA",
					testCaseName + "_BS", "All");

			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");
			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));

			if (WebdriverWrapper.isObjectPresent(driver,
					getORPropValue("cem.incidentManagement.defectTbl")))// xpath_//*[@id='defect']
				assertTrue(WebdriverWrapper
						.getElementText(
								driver,
								getORPropValue("cem.incidentManagement.defectTbl.userNameColumn"))
						.equalsIgnoreCase("jammi"));// here, the username is to
													// be verified and the
													// value passed is jammi
			else
				assertTrue(false);

		} catch (Exception e) {
			e.printStackTrace();
		}

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void Verify_ALM_314512_WrongDefectSearch() {
		setupTest("Verify_ALM_314512_WrongDefectSearch");
		String testCaseName = "Verify_ALM_314512_WrongDefectSearch";
		String itemsFound = "";
		boolean truthValue = false;
		try {
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));

			admin.selectTransactionInformation("medrec_All_BA",
					"medrec_All_BS", "Index");
			// All Defects
			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

			itemsFound = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.itemsFoundSpan"));
			LOGGER.info(itemsFound);
			String[] str = itemsFound.split(" ");
			LOGGER.info(str[0]);
			LOGGER.info(str[1]);

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));

			admin.selectTransactionInformation("medrec_All_BA",
					"medrec_All_BS", "Index");
			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectTypeSelect"),
							"Slow Time");
			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

			itemsFound = WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.itemsFoundSpan"));

			String[] str1 = itemsFound.split(" ");
			LOGGER.info(str1[0]);
			LOGGER.info(str1[1]);
			if (Integer.parseInt(str1[0]) < Integer.parseInt(str[0])) {
				truthValue = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(truthValue);

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void Verify_ALM_324033_DefectSetMonId() {
		setupTest("Verify_ALM_324033_DefectSetMonId");
		String testCaseName = "Verify_ALM_324033_DefectSetMonId";
		PostgresUtil connPostgresDB = new PostgresUtil(DB_HOST, DB_PORT,
				DB_NAME, DB_OWNER, DB_PASSWORD);
		Vector<String> ts_monitor_id = connPostgresDB.getMonitorIdForDefects();
		boolean isEmpty = true;

		if (ts_monitor_id.size() > 0) {
			isEmpty = false;
		}

		assertFalse(isEmpty);

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_350039_76424() {
		setupTest("verify_ALM_350039_76424");
		String testCaseName = "verify_ALM_350039_76424";
		OracleUtil connOraDB = new OracleUtil(getEnvConstValue("OracleDBHost"),
				getEnvConstValue("OracleDBPort"),
				getEnvConstValue("OracleDBName"),
				getEnvConstValue("OracleUser"),
				getEnvConstValue("OraclePassword"));

		String TIMESTAMP = getDateTime();
		assertTrue(connOraDB.areDefectsPresent(TIMESTAMP));

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_351150_INCM33() {
		setupTest("verify_ALM_351150_INCM33");
		String testCaseName = "verify_ALM_351150_INCM33";
		List<WebElement> SelectBoxValues;

		try {
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));

			admin.selectTransactionInformation("medrec_All_BA",
					"medrec_All_BS", "All");

			Select sel = new Select(
					WebdriverWrapper
							.findElementByType(
									driver,
									getORPropValue("cem.incidentManagement.defects.SearchByTransactionInformation.BusinessTransactionDropDown")));
			SelectBoxValues = sel.getOptions();
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
			System.out.println(SelectBoxValues.size());
			for (WebElement e : SelectBoxValues) {
				System.out.println(e.getText());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_257124_APID234() {
		setupTest("verify_ALM_257124_APID234");
		String testCaseName = "verify_ALM_257124_APID234";

		try {
			this.setupAppData("verify_ALM_351159_BKPR46_BA",
					"verify_ALM_351159_BKPR46_BS",
					getTestDataValue("Verify_ALM_351159_BKPR46_btImportFile"));

			String adminLoginurlwithQuery = MED_REC_BASE_URL
					+ getTestDataValue("medRec.admin.loginPage")
					+ "\\?userName=\"User1\"\\&groupId=\"Group1\"";
			String BSName = "verify_ALM_351159_BKPR46_BS";
			String BAName = "verify_ALM_351159_BKPR46_BA";
			String count = " 10";

			// admin.disableBTsforAllBS();
			// admin.disableEnableBTsforAllBS("disable");
			// admin.enableDisableAllBusinessTransactions(BSName, true);

			setupMonitor.syncMonitors();

			generateData(scriptName + adminLoginurlwithQuery + count);

			// Defect Details page
			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));
			admin.selectTransactionInformation(BAName, BSName, "All");

			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");
			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));

			if (WebdriverWrapper.isObjectPresent(driver,
					getORPropValue("cem.incidentManagement.defectTbl")))
				if (WebdriverWrapper
						.getElementText(
								driver,
								getORPropValue("cem.incidentManagement.defects.defectList.secondColumn"))
						.equalsIgnoreCase(BSName)) {
					LOGGER.info("Successful");
					assertTrue(true);
				}

			admin.createBusinessService(testCaseName + "_BS", testCaseName
					+ "_BS", BAName, true, "Medium (Default)");

			/**
			 * Move transaction to new BS, Sync Monitors and run script to
			 * generate defects
			 */
			admin.moveBusinessTransactions(BSName, testCaseName + "_BS");

			setupMonitor.syncMonitors();

			generateData(scriptName + adminLoginurlwithQuery + count);

			/**
			 * Then search for the generated defects with the new BS
			 */

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.defects"));
			admin.selectTransactionInformation(BAName, testCaseName + "_BS",
					"All");
			WebdriverWrapper
					.selectBox(
							driver,
							getORPropValue("cem.incidentManagement.defects.showSelect"),
							"All Defects");

			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentManagement.defects.searchBtn"));
			WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

			if (WebdriverWrapper
					.getElementText(
							driver,
							getORPropValue("cem.incidentManagement.defects.defectList.secondColumn"))
					.equalsIgnoreCase(testCaseName + "_BS")) {
				assertTrue(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_304044_69204() {
		setupTest("verify_ALM_304044_69204");
		String testCaseName = "verify_ALM_304044_69204";
		String count = " 100";
		String URL = getTestDataValue("verify_ALM_304044_69204_URL");
		List<String> t1, t2;
		t1 = new ArrayList<String>();
		t2 = new ArrayList<String>();
		try {
			WebdriverWrapper.navigateToPage(driver,
					getORPropValue("home.setup"),
					getORPropValue("setup.introscopesettings"));
			WebdriverWrapper.inputText(driver,
					getORPropValue("cem.security.istransactionthresholdedit"),
					"5");
			WebdriverWrapper
					.selectCheckBox(
							driver,
							getORPropValue("cem.security.isenableworkstationwebstartcheck"));
			WebdriverWrapper.click(driver,
					getORPropValue("cem.introscope.SavaSettingsButton"));

			setupAppData(testCaseName + "_BA", testCaseName + "_BS",
					getTestDataValue("verify_ALM_304044_69204_btImportFile"));

			/**
			 * Close all the incidents before generating defects.
			 */

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"));

			if (WebdriverWrapper.isObjectPresent(driver,
					"xpath_//*[@id='bizEvent']")) {
				WebdriverWrapper.selectCheckBox(driver,
						"xpath_//*[@id='bizEvent']/thead/tr/th[1]/input");
				WebdriverWrapper.click(driver, getORPropValue("button.close"));
			} else
				LOGGER.info("No incidents to close");

			/**
			 * Generating defects
			 */

			generateData(scriptName + URL + count);

			Thread.sleep(timeOut);

			WebdriverWrapper.click(driver,
					getORPropValue("cem.incidentmanagemnt"));

			if (WebdriverWrapper.isObjectPresent(driver,
					"xpath_//*[@id='bizEvent']")) {
				if (WebdriverWrapper
						.getElementText(driver,
								"xpath_//*[@id='bizEvent']/tbody/tr/td[7]")
						.trim().equalsIgnoreCase("Unnamed")) {
					WebdriverWrapper.click(driver,
							"xpath_//*[@id='bizEvent']/tbody/tr/td[2]/a");
					if (WebdriverWrapper
							.isObjectPresent(
									driver,
									getORPropValue("cem.incidentManagement.incidents.inStartTransactionTraceButton")))
						WebdriverWrapper
								.click(driver,
										getORPropValue("cem.incidentManagement.incidents.inStartTransactionTraceButton"));
					else if (WebdriverWrapper
							.isObjectPresent(
									driver,
									getORPropValue("cem.incidentManagement.incidents.inStopTransactionTraceButton")))
						LOGGER.info("Already Started");

					generateData(scriptName + URL + count);

					Thread.sleep(timeOut);

					WebdriverWrapper
							.click(driver,
									getORPropValue("cem.incidentManagement.bizEventOverviewTab"));

					String transTableAfterClick = getORPropValue("cem.incidentManagement.incidents.incidentDetails.overviewLink.DefectsWithTransTraceValueLink.tranTbl");

					if (WebdriverWrapper
							.isObjectPresent(
									driver,
									getORPropValue("cem.incidentManagement.incidents.incidentDetails.overviewLink.DefectsWithTransTraceValueLink"))) {
						WebdriverWrapper
								.click(driver,
										getORPropValue("cem.incidentManagement.incidents.incidentDetails.overviewLink.DefectsWithTransTraceValueLink"));
						WebdriverWrapper.click(driver,
								"xpath_//*[@id='defect']/tbody/tr[1]/td[1]/a");
						if (WebdriverWrapper
								.isObjectPresent(
										driver,
										getORPropValue("cem.incidentManagement.incidents.incidentDetails.overviewLink.DefectsWithTransTraceValueLink.tranTable"))) {
							// int size =
							// driver.findElements(By.xpath(transTableAfterClick)).size();
							int size = WebdriverWrapper.getXpathCount(driver,
									transTableAfterClick);
							for (int i = 1; i <= size; i++) {
								t1.add(WebdriverWrapper.getElementText(driver,
										transTableAfterClick + "[" + i
												+ "]/td[2]"));
							}

							LOGGER.info("" + t1);
							t2 = t1;
							Collections.reverse(t1);
							assertTrue(t1.equals(t2));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_351147_INCM21() {
		setupTest("verify_ALM_351147_INCM21");
		String testCaseName = "verify_ALM_351147_INCM21";
		String count = " 10";
		String MissingResponseUrl = getTestDataValue("missingResponseURL");
		String ClientRequestErrorUrl = getTestDataValue("ClientRequestErrorUrl");
		String ClientReqErrorXpath = "xpath_//td[text()='" + testCaseName
				+ "_ClientRequestError_BS']//../td[2]/a";
		String MissingResponseXpath = "xpath_//td[text()='" + testCaseName
				+ "_MissingResponse_BS']//../td[2]/a";

		/**
		 * Generate defects for the above cases. Wait for defects to generate
		 */

		try {
			Thread.sleep(timeOut * 2);
			generateData(scriptName + ClientRequestErrorUrl + count);
			generateData(scriptName + MissingResponseUrl + count);

			/**
			 * Goto incidents and verify for
			 */

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.incidents"));
			if (WebdriverWrapper.isObjectPresent(driver, ClientReqErrorXpath))
				WebdriverWrapper.click(driver, ClientReqErrorXpath);
			WebdriverWrapper
					.click(driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink"));
			assertFalse(WebdriverWrapper.isObjectPresent(driver,
					getORPropValue("cem.incidentManagement.defectTimeSeries")));

			WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
					getORPropValue("cem.incidentmanagemnt"),
					getORPropValue("cem.incidentManagement.incidents"));
			if (WebdriverWrapper.isObjectPresent(driver, MissingResponseXpath))
				WebdriverWrapper.click(driver, MissingResponseXpath);
			WebdriverWrapper
					.click(driver,
							getORPropValue("cem.incidentManagement.incidents.incidentDetails.troubleshootLink"));
			assertFalse(WebdriverWrapper.isObjectPresent(driver,
					getORPropValue("cem.incidentManagement.defectTimeSeries")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_351154_BKPR39() {
		setupTest("verify_ALM_351154_BKPR39");
		String testCaseName = "verify_ALM_351154_BKPR39";
		startTestCaseName(testCaseName);
		int retries = 0;

		try {

			boolean areReportsPresent = false;
			admin.disableEnableBTsforAllBS("enable");

			while (areReportsPresent == false) {
				WebdriverWrapper
						.navigateToPage(
								driver,
								getORPropValue("home.cem"),
								getORPropValue("cem.servicelevelManagement"),
								getORPropValue("cem.servicelevelManagement.transactionsla"));

				areReportsPresent = WebdriverWrapper.isObjectPresent(driver,
						"xpath_//*[@id='reportDiv']/table/thead/tr/th[1]");
				if (areReportsPresent == true || retries >= 36)
					break;
				if (areReportsPresent == false || retries < 36) {
					Thread.sleep((AggrtimeOut / 100000) * 3);
					retries++;
					LOGGER.info("Retrying every 2mins" + retries);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		endTestCaseName(testCaseName);
		tearDownTest(testCaseName);
	}

	/**
	 * Generic Method. Generate the defects for the initial phase.
	 */

	private void generateDefectsForDefectProcessor() {
		String indexUrl = MED_REC_BASE_URL + MEDREC_INDEX_PAGE;
		String patientRegisterURL = MED_REC_BASE_URL + "/"
				+ getTestDataValue("medRec.patient.registerPage");
		String groupIdUrl = MED_REC_BASE_URL
				+ getTestDataValue("medRec.groupId_url");
		String physicianLoginUrl = MED_REC_BASE_URL
				+ getTestDataValue("medRec.physician.loginPage");
		String adminLoginurl = MED_REC_BASE_URL
				+ getTestDataValue("medRec.admin.loginPage");
		String MissingResponseUrl = getTestDataValue("missingResponseURL");
		String ClientRequestErrorUrl = getTestDataValue("ClientRequestErrorUrl");
		LOGGER.info("These are the TIM details");
		LOGGER.info(TIM_HOST_NAME + "  " + TIM_REMOTELOGIN + "  "
				+ TIM_REMOTEPWD);
		String count = " 100";
		//String[] commands = {scriptName + indexUrl , count};
//		try{
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,
//		 TIM_REMOTELOGIN, TIM_REMOTEPWD, commands);
//		 String[] commands1 = {scriptName + patientRegisterURL + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands1);
//         String[] commands2 = {scriptName  + groupIdUrl  + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands2);
//         String[] commands3 = {scriptName  + physicianLoginUrl  + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands3);
//         String[] commands4 = {scriptName  + adminLoginurl  + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands4);
//         String[] commands5 = {scriptName  + ClientRequestErrorUrl  + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands5);
//         String[] commands6 = {scriptName  + MissingResponseUrl  + count};
//		 utility.execUnixCmd(TIM_HOST_NAME, 22,TIM_REMOTELOGIN,
//		 TIM_REMOTEPWD, commands6);
//		}
//		catch(Exception e)
//		{
//		    e.printStackTrace();
//		}
		generateData(scriptName + indexUrl + count);
		generateData(scriptName + patientRegisterURL + count);
		generateData(scriptName + groupIdUrl + count);
		generateData(scriptName + physicianLoginUrl + count);
		generateData(scriptName + adminLoginurl + count);
		generateData(scriptName + ClientRequestErrorUrl + count);
		generateData(scriptName + MissingResponseUrl + count);

		try {
			Thread.sleep(timeOut);
		} catch (Exception e) {
			LOGGER.info(e.toString());
		}
	}

	public static String getDateTime() {
		Calendar cal = Calendar.getInstance();

		DateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yyyy hh:mm:ss.FFFFFFFFF aaa");
		cal.set(Calendar.HOUR, 0);
		return dateFormat.format(cal.getTime());
	}

//	@AfterClass()
	public void close() {
		driver.quit();
		Util.runOSCommand("cmd.exe /c taskkill /im "
				+ getEnvConstValue("browser") + "* -F");
	}

	@AfterSuite
	public void generateResultsFile(ITestContext context) {

		if (!isGenerateResults()) {
			LOGGER.info("Skipping 'generating result file for QC upload' task ...");
			return;
		}

		Collection<ITestResult> passed = context.getPassedTests()
				.getAllResults();
		Collection<ITestResult> failed = context.getFailedTests()
				.getAllResults();
		Collection<ITestResult> skipped = context.getSkippedTests()
				.getAllResults();
		String filename = getEnvConstValue("getQcuploadtoolResultFile");

		if (filename == null) {
			LOGGER.warn("Skipping QC results generation as file name wasn't provided.");
			return;
		}
		File resultsFile = new File(filename);
		try {
			if (!resultsFile.exists()) {
				resultsFile.createNewFile();
			}
		} catch (IOException ioe) {
			LOGGER.error("Could not create results file.");
			ioe.printStackTrace();
		}

		try {
			writer = new BufferedWriter(new FileWriter(resultsFile, true));
			LOGGER.info("Writing results to file to " + resultsFile);

			for (ITestResult result : passed) {
				LOGGER.info("Appending passed results to file. "
						+ result.getName());
				appendResultFile(result, "Passed", writer);
			}

			for (ITestResult result : failed) {
				LOGGER.info("Appending failed results to file."
						+ result.getName());
				appendResultFile(result, "Failed", writer);
			}

			for (ITestResult result : skipped) {
				LOGGER.info("Appending skipped results to file."
						+ result.getName());
				appendResultFile(result, "Skipped", writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	protected void appendResultFile(ITestResult result, String status,
			BufferedWriter writer) {
		try {
			ITestNGMethod method = result.getMethod();

			String str[] = method.toString().split("_");
			writer.write(str[2] + "," + status + "," + method.toString());
			writer.newLine();
			LOGGER.info("Updating results for QCID: " + str[2] + "," + status
					+ "," + method.toString());

		} catch (Exception ex) {
			LOGGER.error("Exception while appending result file");
			ex.printStackTrace();
		}

	}

	public boolean isGenerateResults() {
		boolean truthValue = false;
		if (getEnvConstValue("generate.results").trim()
				.equalsIgnoreCase("true"))
			truthValue = true;
		else
			truthValue = false;
		return truthValue;
	}
}
