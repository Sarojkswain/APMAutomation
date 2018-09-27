package com.ca.apm.cem.logiProcessor;

import org.openqa.selenium.WebDriver;

import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.test.CEMAdministration;
import com.ca.apm.tests.test.CEMSetup;
import com.ca.apm.tests.test.CemReports;
import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.apm.tests.cem.common.AdminBTImportHelper;
import com.ca.apm.tests.cem.common.AdminBusinessApplicationHelper;
import com.ca.apm.tests.cem.common.SetupMonitorHelper;

public class LoginProcessorUtils extends JBaseTest {

    private long SLEEP_TIME = 30000;

    public boolean regexPattern(String timLog, String match1, String match2, String match3) {
        boolean result = false;
        String tmpstr = "";
        String[] inputSplitNewLine = timLog.split("\\n");
        for (int i = 0; i < inputSplitNewLine.length; i++) {
            tmpstr = inputSplitNewLine[i];
            if (tmpstr.contains(match1)) {
                if (tmpstr.contains(match2)) {
                    if (match3.trim().equals("")) {
                        result = true;
                        break;
                    }
                    if (tmpstr.contains(match3)) {
                        result = true;
                        break;
                    }
                    LOGGER.info("Not found Match3 regexPattern linenumber :::" + (i + 1) + "---"
                        + tmpstr);
                }
                LOGGER.info("Not found Match2 regexPattern linenumber :::" + (i + 1) + "---" + tmpstr);
            }
        }
        LOGGER.info("Result Match::"+result+"::"+match1+"::"+match2+"::"+match3);
        return result;
    }


    public String subStringJsessionID(StringBuffer str)

    {

        String tempstr = "";
        String jesssionId = "";

        tempstr = str.substring(str.indexOf("jsessionid="));
        String[] stArr = tempstr.split(" ");
        String[] splArr = stArr[0].split("=");

        jesssionId = splArr[1].substring(0, (splArr[1].length() - 1));
        LOGGER.info("jesssionId::" + jesssionId);

        return jesssionId;
    }

    public String getUserGroupvalue(WebDriver driver, CemReports reports,
        String businessApplication, String businessService) throws Exception {
        int defectCount = 0;
        String tmpUserGroupvalue = "";
        LOGGER.debug("Driver object value::" + driver);
        LOGGER.debug("reports object value::" + reports);
        LOGGER.debug("businessApplication object value::" + businessApplication);
        LOGGER.debug("businessService object value::" + businessService);
        LOGGER.info("getUserGroupvalue Method process halting for ::" + SLEEP_TIME + " Milisec");
        Thread.sleep(SLEEP_TIME);
        defectCount = reports.getDefectSearchResult("", businessApplication, businessService);
        if (defectCount > 0) {
            WebdriverWrapper.click(driver, getORPropValue("cem.defect.firstDefect"));
            LOGGER.debug("UserGroupvalue>>>>>>"
                + getORPropValue("cem.incidentManagement.defects.UserInformation") + "/tr[3]/td[1]");
            String tmpUserGroup =
                WebdriverWrapper.getElementText(driver,
                    getORPropValue("cem.incidentManagement.defects.UserInformation")
                        + "/tr[3]/td[1]");
            LOGGER.info("Table first column::" + tmpUserGroup);
            if (tmpUserGroup.trim().equalsIgnoreCase("User Group:")) {
                tmpUserGroupvalue =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("cem.incidentManagement.defects.UserInformation")
                            + "/tr[3]/td[2]");
                LOGGER.info("Table second column::" + tmpUserGroupvalue);
            }

        }
        LOGGER.info("return getUserGroupvalue::"+tmpUserGroupvalue+"::");
        return tmpUserGroupvalue;
    }

    public String getUserGroupvalue(WebDriver driver, CemReports reports,
        String businessApplication, String businessService, String LoginName) throws Exception {
        int defectCount = 0;
        String tmpUserGroupvalue = "";
        LOGGER.info("getUserGroupvalue Method process halting for ::" + SLEEP_TIME + " Milisec");
        Thread.sleep(SLEEP_TIME);
        defectCount =
            reports.getDefectSearchResult(LoginName, businessApplication, businessService);
        if (defectCount > 0) {
            WebdriverWrapper.click(driver, getORPropValue("cem.defect.firstDefect"));

            String tmpUserGroup =
                WebdriverWrapper.getElementText(driver,
                    getORPropValue("cem.incidentManagement.defects.UserInformation")
                        + "/tr[3]/td[1]");
            LOGGER.info("Table first column::" + tmpUserGroup);
            if (tmpUserGroup.trim().equalsIgnoreCase("User Group:")) {
                tmpUserGroupvalue =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("cem.incidentManagement.defects.UserInformation")
                            + "/tr[3]/td[2]");
                LOGGER.info("Table second column::" + tmpUserGroupvalue);
            }

        }
        return tmpUserGroupvalue;
    }

    public void setupTim(CEMSetup setup, String timName, String timIP) {
        try {
            setup.createMonitor(timName, timIP);
            setup.enableMonitor(timName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void creatBABS(AdminBusinessApplicationHelper adminBA, SetupMonitorHelper setupMonitor,
        CEMAdministration admin, String appName, String businessService) {

        try {
            adminBA.createBusinessApplication(appName, appName, "Generic", "Application Specific",
                true, true, "5", "E-Commerce", "UTF-8", TESS_HOST);
            admin.createBusinessService(businessService, businessService, appName, true, "");
            setupMonitor.syncMonitors();
        } catch (Exception e) {
            LOGGER.info("Failed to create Application: " + appName);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void enableBusinessServiceMonitoring(SetupMonitorHelper setupMonitor,
        CEMAdministration admin, String businessService) {

        try {

            admin.enableBusinessServiceMonitoring(businessService);
            setupMonitor.syncMonitors();
        }

        catch (Exception e) {
            LOGGER.info("Failed to enable businessService: " + businessService);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void setupAppData(AdminBusinessApplicationHelper adminBA,
        AdminBTImportHelper adminBTImport, QaUtils util, SetupMonitorHelper setupMonitor,
        CEMAdministration admin, String appName, String businessService, String btImportFile) {

        try {
            adminBA.createBusinessApplication(appName, appName, "Generic", "Application Specific",
                true, true, "5", "E-Commerce", "UTF-8", TESS_HOST);
            String bsImportFile = admin.getTestDataFullPath("GeneralApplication", btImportFile);
            adminBTImport.importZipFileToNewBS(appName, businessService, businessService,
                bsImportFile);
            util.sleep(10000);
            setupMonitor.syncMonitors();
            admin.enableBusinessServiceMonitoring(businessService);
            setupMonitor.syncMonitors();
        }

        catch (Exception e) {
            LOGGER.info("Failed to create Application: " + appName);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public boolean validateplugIn(WebDriver driver, CemReports reports, String componentidValue,
        String appName, String businessService) throws Exception {
        int rowCount = 0;
        int i = 2;
        int defectCount = 0;
        boolean result = false;
        String actualvalue = "";
        LOGGER.info("validateplugIn Method process halting for ::" + SLEEP_TIME + " Milisec");
        Thread.sleep(SLEEP_TIME);
        defectCount = reports.getDefectSearchResult("", appName, businessService);
        // WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        if (defectCount > 0) {
            WebdriverWrapper.click(driver, getORPropValue("cem.defect.firstDefect"));
            rowCount =
                WebdriverWrapper.findElementsByType(driver,
                    getORPropValue("cem.incidentManagement.defects.httpInformation")).size();
            for (; i < rowCount; i++) {
                String tmp =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("cem.incidentManagement.defects.httpInformation") + "[" + i
                            + "]/td[2]");
                LOGGER.info("Table second column::" + tmp);
                if ("component-id:".equalsIgnoreCase(tmp.trim())) {
                    actualvalue =
                        WebdriverWrapper.getElementText(driver,
                            getORPropValue("cem.incidentManagement.defects.httpInformation") + "["
                                + i + "]/td[3]");
                    if (!componentidValue.equals("")) {
                        try {
                            Integer.parseInt(actualvalue.trim());
                            result = true;
                        } catch (NumberFormatException e) {
                            result = false;
                        }

                        break;
                    }

                }

            }
            if (i == rowCount) {

                LOGGER.info("component-id parameter not found");
            } else {
                LOGGER.info("componentidValue is empty or different value Expected ::"
                    + componentidValue + "Actual ::" + actualvalue);
            }
        }
        return result;
    }

    public String paramValue(String parameterTypeUGId, String parameterName) {
        String paramValue = "";
        if (parameterTypeUGId.equalsIgnoreCase("URL") && parameterName.equalsIgnoreCase("Port")) {
            paramValue = MED_REC_HOST_PORT;
        } else if (parameterTypeUGId.equalsIgnoreCase("URL")
            && parameterName.equalsIgnoreCase("HOST")) {
            paramValue = MED_REC_HOSTNAME;
        } else if (parameterTypeUGId.equalsIgnoreCase("URL")
            && parameterName.equalsIgnoreCase("Path")) {
            paramValue = MED_REC_PHYSICIAN_LOGIN_PAGE;
        }
        return paramValue;
    }

    public String getUserIDValue(WebDriver driver, CemReports reports, String businessApplication,
        String businessService) throws Exception {
        int defectCount = 0;
        String tmpUserIdValue = "";
        LOGGER.debug("Driver object ::" + driver);
        LOGGER.debug("reports object ::" + reports);
        LOGGER.debug("businessApplication object ::" + businessApplication);
        LOGGER.debug("businessService object ::" + businessService);
        LOGGER.info("getUserIDValue Method process halting for ::" + SLEEP_TIME + " Milisec");
        Thread.sleep(SLEEP_TIME);
        defectCount = reports.getDefectSearchResult("", businessApplication, businessService);
        if (defectCount > 0) {
            WebdriverWrapper.click(driver, getORPropValue("cem.defect.firstDefect"));

            String tmpUserGroup =
                WebdriverWrapper.getElementText(driver,
                    getORPropValue("cem.incidentManagement.defects.UserInformation")
                        + "/tr[2]/td[1]");
            LOGGER.info("Table first column::" + tmpUserGroup);
            if (tmpUserGroup.trim().equalsIgnoreCase("User:")) {
                tmpUserIdValue =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("cem.incidentManagement.defects.UserInformation")
                            + "/tr[2]/td[2]");
                LOGGER.info("Table second column::" + tmpUserIdValue);
            }

        }
        LOGGER.info("return getUserIDValue::"+tmpUserIdValue+"::");
        return tmpUserIdValue;
    }

    public String getUserIDValue(WebDriver driver, CemReports reports, String businessApplication,
        String businessService, String LoginName) throws Exception {
        int defectCount = 0;
        String tmpUserIdValue = "";
        LOGGER.info("getUserIDValue Method process halting for ::" + SLEEP_TIME + " Milisec");
        Thread.sleep(SLEEP_TIME);
        defectCount =
            reports.getDefectSearchResult(LoginName, businessApplication, businessService);
        if (defectCount > 0) {
            WebdriverWrapper.click(driver, getORPropValue("cem.defect.firstDefect"));

            String tmpUserGroup =
                WebdriverWrapper.getElementText(driver,
                    getORPropValue("cem.incidentManagement.defects.UserInformation")
                        + "/tr[2]/td[1]");
            LOGGER.info("Table first column::" + tmpUserGroup);
            if (tmpUserGroup.trim().equalsIgnoreCase("User:")) {
                tmpUserIdValue =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("cem.incidentManagement.defects.UserInformation")
                            + "/tr[2]/td[2]");
                LOGGER.info("Table second column::" + tmpUserIdValue);
            }

        }
        return tmpUserIdValue;
    }

}
