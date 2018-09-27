package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class CEMAdministration extends BaseSharedObject {

    public final String userIDTypeURLOption = "URL";

    public CEMAdministration() {
        // empty constructor
    }

    public CEMAdministration(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Creates a business application
     * 
     * @param applicationName
     * @param applicationDescription
     * @param appType
     * @param appAuthType
     * @param caseSensitiveURL
     * @param caseSensitiveLogin
     * @param appUserProcessing
     * @param appTimeout
     * @param characterEncoding
     * @throws Exception
     */
    public String createBusinessApplication(String applicationName, String applicationDescription,
        String appType, String appAuthType, Boolean caseSensitiveURL, Boolean caseSensitiveLogin,
        String appUserProcessing, String appTimeout, String characterEncoding) throws Exception {
        System.out.println("Create a new BusinessApplication");
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        System.out.println("Navigated to Business Application Page");
        WebdriverWrapper.click(driver, getORPropValue("button.new"));
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.appname"), applicationName);
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.description"),
            applicationDescription);
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.sessiontimeout"), appTimeout);
        WebdriverWrapper.selectBox(driver,
            getORPropValue("administration.businessapplication.authenticationtype"), appAuthType);
        if (caseSensitiveURL) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver,
                getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        }
        if (caseSensitiveLogin) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.businessapplication.cesesensitiveloginnames"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver,
                getORPropValue("administration.businessapplication.cesesensitiveloginnames"));
        }
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.userprocessingtype"),
            appUserProcessing);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.characterencoding"),
            characterEncoding);
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
        if (WebdriverWrapper.isObjectPresent(driver,
            getORPropValue("cem.system.email.msgerrorlable"))) {
            return WebdriverWrapper.getElementText(driver,
                getORPropValue("cem.system.email.msgerrorvalue"));
        }
        assertTrue(WebdriverWrapper.isObjectPresent(driver,
            getORPropValue("administration.businessapplication.app.general")));
        System.out.println("Business Application created : " + applicationName);
        return null;
    }

    /**
     * Creates a new Business Process - lots of params
     * @param name
     * @param descr
     * @param application
     * @param inheritImpact
     * @param impactLevel
     * @param inheritSuccessRate
     * @param successRate
     * @param inheritSigma
     * @param sigmaLevel
     * @param inheritTransTime
     * @param transactionTimeSLA
     * @throws Exception 
     */
    public void setNewBusinessProcess(String name, String descr, String application, Boolean inheritImpact, 
            String impactLevel, Boolean inheritSuccessRate, String successRate, Boolean inheritSigma, 
            String sigmaLevel, Boolean inheritTransTime, String transactionTimeSLA) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver,getORPropValue("button.new"));
        WebdriverWrapper.inputText(driver, getORPropValue("administration.businessservice.bsNameEdit"), name);
        WebdriverWrapper.inputText(driver,getORPropValue("administration.businessservice.bsDescriptionEdit"), descr);
        WebdriverWrapper.selectList(driver,getORPropValue("administration.businessservice.bsApplicationSelect"), application);
        WebdriverWrapper.click(driver,getORPropValue("button.save"));
        WebdriverWrapper.click(driver,getORPropValue("linkText")+name);
        WebdriverWrapper.click(driver,getORPropValue("linkText")+"General");
        if (!inheritImpact){
            WebdriverWrapper.deselectCheckBox(driver,getORPropValue("administration.businessservice.bsInheritImpactLevelCheck"));
            WebdriverWrapper.selectList(driver,getORPropValue("administration.businessservice.bsTransactionImpactSelect"), impactLevel);
        }
        if (!inheritSuccessRate){
            WebdriverWrapper.deselectCheckBox(driver,getORPropValue("administration.businessservice.bsInheritSucessRateSLACheck"));
            WebdriverWrapper.inputText(driver, getORPropValue("administration.businessservice.bsSuccessRateSLAEdit"), successRate);
        }
        if (!inheritSigma){
            WebdriverWrapper.deselectCheckBox(driver,getORPropValue("administration.businessservice.bsInheritSigmaCheck"));
            WebdriverWrapper.inputText(driver,getORPropValue("administration.businessservice.bsSigmaSLAEdit"), sigmaLevel);
        }
        if (!inheritTransTime){
            WebdriverWrapper.deselectCheckBox(driver,getORPropValue("administration.businessservice.bsInheritTransactionTime"));
            WebdriverWrapper.inputText(driver,getORPropValue("administration.businessservice.bsTransactionTimeEdit"), transactionTimeSLA);
        }
        WebdriverWrapper.click(driver,getORPropValue("button.edit.save"));
    }

    /**
     * Create or recreate a Business application when no TIM - will delete existing if found
     * @param applicationName
     * @param applicationDescription
     * @param caseSensitiveURL
     * @param characterEncoding
     * @throws Exception 
     */
    public void createBusinessApplicationNoTIM(String applicationName, String applicationDescription, Boolean caseSensitiveURL,
            String characterEncoding) throws Exception{
        System.out.println("Create a new BusinessApplication");
        logIn();
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessapplication"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")+applicationName)){
            deleteApplication(applicationName);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.new")); 
        WebdriverWrapper.inputText(driver, getORPropValue("administration.businessapplication.appname"),applicationName);
        WebdriverWrapper.inputText(driver, getORPropValue("administration.businessapplication.description"), applicationDescription);
        if (caseSensitiveURL){
            WebdriverWrapper.selectCheckBox(driver,getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        }else {
            WebdriverWrapper.deselectCheckBox(driver,getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        }
        WebdriverWrapper.selectList(driver,getORPropValue("administration.businessapplication.characterencoding"), characterEncoding);
        WebdriverWrapper.click(driver,getORPropValue("button.save"));
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("administration.businessapplication.app.general") ));
        System.out.println("Business Application created : "+applicationName);
    }

    /**
     * Edit an exiting application General values
     * 
     * @param applicationName
     * @param applicationDescription
     * @param appType
     * @param appAuthType
     * @param caseSensitiveURL
     * @param caseSensitiveLogin
     * @param appUserProcessing
     * @param appTimeout
     * @param characterEncoding
     * @return
     * @throws Exception
     */
    public void editBusinessApplicationGeneralProperties(String applicationName,
        String applicationDescription, String appType, String appAuthType,
        Boolean caseSensitiveURL, Boolean caseSensitiveLogin, String appUserProcessing,
        String appTimeout, String characterEncoding) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + "General");
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.appname"), applicationName);
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.description"),
            applicationDescription);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.type"), appType);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.authenticationtype"), appAuthType);
        if (caseSensitiveURL) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver,
                getORPropValue("administration.businessapplication.casesensitiveurlpath"));
        }
        if (caseSensitiveLogin) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.businessapplication.cesesensitiveloginnames"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver,
                getORPropValue("administration.businessapplication.cesesensitiveloginnames"));
        }
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.userprocessingtype"),
            appUserProcessing);
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.sessiontimeout"), appTimeout);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.characterencoding"),
            characterEncoding);
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);



        System.out.println("Business Application edited : " + applicationName);
    }

    /**
     * Add advanced User Id Param to application
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     * @param offset
     * @param length
     * @throws Exception
     */
    public String addUserIdAdvancedParamToApplication(String applicationName, String parameterType,
        String parameterName, String offset, String length) throws Exception {
        String errMsg = "";

        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.navigateToPage(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        WebdriverWrapper.click(driver, getORPropValue("button.new"));
        WebdriverWrapper.selectBox(driver,
            getORPropValue("administration.businessapplication.app.useridentification.type"),
            parameterType);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification.advanced"));
        if (!parameterType.equals("Post")) {
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            // WebdriverWrapper.inputText(driver,
            // getORPropValue("administration.businessapplication.app.useridentification.type"),
            // parameterName);
        }
        if (WebdriverWrapper.isObjectPresent(driver,
            getORPropValue("administration.businessapplication.app.useridentification.nameselect"))) {
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.nameselect"),
                    parameterName);
        } else {
            WebdriverWrapper
                .inputText(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.nameedit"),
                    parameterName);
        }
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.app.useridentification.offset"),
            offset);
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessapplication.app.useridentification.length"),
            length);
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
        if (offset.trim().equals("") || length.trim().equals("")) {
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.message"))) {
                if (WebdriverWrapper.isTextInSource(driver, "Offset is required."))
                    errMsg = "Offset is required.";
                if (WebdriverWrapper.isTextInSource(driver, "Length is required."))
                    errMsg = errMsg + "Length is required.";
                LOGGER.info("Error message in UserId Advance Parm ::" + errMsg);
            }

        }

        return errMsg;
    }

    /**
     * Edit existing advanced User Id param
     * 
     * @param applicationName
     * @param parameterName
     * @param offset
     * @param length
     * @throws Exception
     */
    public void editUserIdAdvancedParamToApplcation(String applicationName, String parameterName,
        String offset, String length) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + parameterName)) {
            WebdriverWrapper.click(driver, getORPropValue("linkText") + parameterName);
            WebdriverWrapper
                .click(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.advanced"));
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.useridentification.offset.edit"),
                offset);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.useridentification.length.edit"),
                length);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
        } else
            System.out.println("parameter not present");

    }

    /**
     * Edit existing User Id param
     * 
     * @param applicationName
     * @param parameterName
     * @param newParameterName
     * @throws Exception
     */
    public void editUserIdParamToApplcation(String applicationName, String parameterName,
        String newParameterName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + parameterName)) {
            WebdriverWrapper.click(driver, getORPropValue("linkText") + parameterName);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.useridentification.name.edit"),
                newParameterName);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
        } else
            System.out.println("parameter not present");
    }


    /**
     * Delete a session id param, returns confirmation text
     * 
     * @param applicationName
     * @param paramName
     * @return
     * @throws Exception
     */
    public void deleteSessionIdParamFromApplication(String applicationName, String paramName)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.sessionidentification"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + paramName)) {
            WebdriverWrapper.click(driver, getORPropValue("linkText") + paramName);
            WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
            /*if(WebdriverWrapper.isAlertPresent(driver))
            WebdriverWrapper.selectPopUp(driver,"accept");*/
            WebdriverWrapper.selectPopUp(driver, "accept");
        }
    }

    /**
     * Delete an user id param, returns confirmation text
     * 
     * @param applicationName
     * @param paramName
     * @return
     * @throws Exception
     */
    public void deleteUserIdParameterFromApplication(String applicationName, String paramName)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + paramName)) {
            WebdriverWrapper.click(driver, getORPropValue("linkText") + paramName);
            WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
            Boolean confirm = WebdriverWrapper.selectPopUp(driver, "accept");
        }
    }

    /**
     * Edit user identification parameters for application - will throw exception if parameter does
     * not exist
     * 
     * @param applicationName
     * @param originalParamName
     * @param newParamName
     * @throws Exception
     */
    public void editApplicationUserIdentification(String applicationName, String originalParamName,
        String newParamName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + originalParamName);
        if (WebdriverWrapper
            .isObjectPresent(
                driver,
                getORPropValue("administration.businessapplication.app.useridentification.name.select"))) {
            WebdriverWrapper
                .selectList(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.name.select"),
                    newParamName);
        } else {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.useridentification.name.edit"),
                newParamName);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
    }

    /**
     * Deletes a business service and returns the confirm string
     * 
     * @param businessProcess
     * @return
     * @throws Exception
     */
    public void deleteBusinessService(String businessProcess) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        /*
         * if (!WebdriverWrapper.isObjectPresent(driver,
         * getORPropValue("linkText")+businessProcess)) {
         * }
         */

        checkGridRow("tranDefGroup", businessProcess);
        driver.findElement(By.name("delete")).click();
        // WebdriverWrapper.click(driver,getORPropValue("button.delete"));
        WebdriverWrapper.selectPopUp(driver, "accept");
    }

    /**
     * Pass in the Service name and path to file to import
     * @param businessService
     * @param filePathToImport
     */
    public void importBusinessTranXML(String businessService, String fileToImportFullPath){
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver,getORPropValue("linkText")+businessService);
            WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
            WebdriverWrapper.click(driver,getORPropValue("button.import"));
            WebdriverWrapper.waitForPageToLoad(driver,iGlobalTimeout);
			System.out.println("Importing file: "+fileToImportFullPath);
            
            WebdriverWrapper.inputTextToBrowseButtonByCss(driver, getORPropValue("administration.businessservice.name.importedit"), fileToImportFullPath);
            Thread.sleep(10000);
            WebdriverWrapper.click(driver,getORPropValue("button.import"));
            WebdriverWrapper.waitForPageToLoad(driver,10000000);
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

    /**
     * Enables monitoring for all Business Transaction defects (Slow Time, Fast Time, etc)
     * @param serviceName
     * @param businessTransactionName
     * @throws Exception 
     */
    public void enableBusinessTransactionDefects(String serviceName, String businessTransactionName) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver,getORPropValue("linkText")+serviceName);
        WebdriverWrapper.click(driver, getORPropValue("linkText")+businessTransactionName);
        WebdriverWrapper.click(driver,getORPropValue("administration.businessservice.BusinessTransSpec"));
        WebdriverWrapper.click(driver,getORPropValue("grid.header.checkbox"));
        WebdriverWrapper.click(driver,getORPropValue("button.enable"));
    }

    /**
     * Enables monitoring for all Business Transaction defects (Slow Time)
     * 
     * @param serviceName
     * @param businessTransactionName
     * @throws Exception
     */
    public void enableSlowTimeBusinessTransactionDefects(String serviceName,
        String businessTransactionName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + businessTransactionName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec"));
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec.slowTime"));
        WebdriverWrapper.selectCheckBox(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec.slowTime.enable"));
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec.slowTime.time"), "0");
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
    }

    /**
     * Enables or disables all transactions in a Business service
     * 
     * @param serviceName
     * @param toEnable
     * @throws Exception
     */
    public void enableDisableAllBusinessTransactions(String serviceName, boolean toEnable)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        if (!WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + serviceName)) {
            System.out.println(serviceName + " not existed.");
            return;
        }
        WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
        if (!WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")
            + "//table[@id='tranSetDef']")) { // no bServices existed.
            return;
        }
        checkAllGridRows("tranSetDef");
        if (toEnable) {
            WebdriverWrapper.click(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.clickDisable(driver, getORPropValue("button.disable"));
            WebdriverWrapper.selectPopUp(driver, "accept");
            //driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        } 
    }

    /**
     * Enables or disables all Business transactions
     * 
     * @param toEnable
     * @throws Exception
     */
    public void enableDisableAllBusinessTransactions(boolean toEnable) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        int i = 1;
        String link = "xpath_//table[@id='tranDefGroup']/tbody/tr[" + i + "]/td[2]/a";
        String bs = "";
        while (WebdriverWrapper.isObjectPresent(driver, link)) {
            bs = WebdriverWrapper.getElementText(driver, link);
            enableDisableAllBusinessTransactions(bs, toEnable);
            i++;
            link = "xpath_//table[@id='tranDefGroup']/tbody/tr[" + i + "]/td[2]/a";
        }
        return;
    }

    /**
     * Set/Update BT Spec
     * 
     * @param serviceName
     * @param transactionName
     * @param specName
     * @param enabled
     * @param condition
     * @throws Exception
     */
    public void setBusinessTransactionSpecs(String serviceName, String businessTransactionName,
        String specName, Boolean enabled, String condition) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + businessTransactionName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec"));
        WebdriverWrapper.click(driver, getORPropValue("linkText" + specName));
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.businessTransSpec.edit"), condition);
        if (enabled) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver, getORPropValue("button.enable"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Set/Update transaction Spec
     * 
     * @param serviceName
     * @param businessTransactionName
     * @param transactionName
     * @param specName
     * @param enabled
     * @param condition
     * @throws Exception
     */
    public void setTransactionSpecs(String serviceName, String businessTransactionName,
        String transactionName, String specName, Boolean enabled, String condition)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + businessTransactionName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + transactionName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec.TransSpec"));
        WebdriverWrapper.click(driver, getORPropValue("linkText" + specName));

        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.businessTransSpec.edit"), condition);
        if (enabled) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver, getORPropValue("button.enable"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Set/Update component Spec
     * 
     * @param serviceName
     * @param businessTransactionName
     * @param transactionName
     * @param specName
     * @param enabled
     * @param condition
     * @throws Exception
     */
    public void setComponentSpecs(String serviceName, String businessTransactionName,
        String transactionName, String specName, Boolean enabled, String condition)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + businessTransactionName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + transactionName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessservice.BusinessTransSpec.CompSpec"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + specName);

        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.businessTransSpec.edit"), condition);
        if (enabled) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.deselectCheckBox(driver, getORPropValue("button.enable"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Will enable or disable all specifications returned in the search parameters in the
     * Specifications page
     * 
     * @param businessService
     * @param businessTransaction
     * @param defectType
     * @param defectName
     * @param enabled
     * @throws Exception
     */
    public void enableDisableTransactionsViaSpecificationsPage(String businessService,
        String businessTransaction, String defectType, String defectName, boolean enabled)
        throws Exception {
        searchSpecificationsPage(businessService, businessTransaction, defectType, defectName);

        WebdriverWrapper.click(driver, getORPropValue("grid.header.checkbox"));
        if (enabled) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.disable"));
            WebdriverWrapper.selectPopUp(driver, "accept");
        }
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * Go to specifications page, enter search criteria and click search button
     * 
     * @param businessService
     * @param businessTransaction
     * @param defectType
     * @param defectName
     * @throws Exception
     */
    public void searchSpecificationsPage(String businessService, String businessTransaction,
        String defectType, String defectName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.specifications"));
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.specification.BusinessServiceSelect"), businessService);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.specification.BusinessTransactionSelect"),
            businessTransaction);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.specification.DefectTypeSelect"), defectType);
        if (!defectName.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.specification.DefectNameEdit"), defectName);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.search"));
    }

    /**
     * Will enable or disable all specifications returned in the search parameters in the
     * Specifications page
     * for all Business Service and for all BTs and defet types.
     * 
     * @param enabled
     * @throws Exception
     */
    public void enableDisableAllTransactionsViaTxnSearchPage(boolean enabled) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("administration.businessService.btSerach"));
        WebdriverWrapper.click(driver, getORPropValue("button.search"));
        WebdriverWrapper.click(driver, getORPropValue("grid.header.checkbox"));
        if (enabled) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.enable"));
        } else {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("button.disable"));
            WebdriverWrapper.selectPopUp(driver, "accept");
        }
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * Start new TIM recording session with all defaults
     * 
     * @return
     */
    public String newRecordingSession() throws Exception {
        return newRecordingSession(true, "", "", "", "", "");
    }

    /**
     * Check all rows in the grid
     * 
     * @param enabled
     */
    public void checkAllGridRows(String grid) throws Exception {
        WebdriverWrapper.selectCheckBox(driver,
            getORPropValue("administration.discoveredtxn.allbtcheckbox"));
    }

    /**
     * Deletes the requested recording session if it exists, returns true if there is one
     * 
     * @param sessionName
     * @return
     */
    public boolean deleteRecordingSession(String sessionName)  {
        boolean confirm = false;
        try{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));

        
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + sessionName)) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver, getORPropValue("button.stop"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
            confirm = WebdriverWrapper.selectPopUp(driver, "accept");
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            confirm = true;
        }
        }catch(Exception e)
        {
            LOGGER.error("Delete Recoding Session Exception occour");
        }
        return confirm;
    }


    /**
     * Create a Business Service (formerly Business Process)
     * 
     * @param serviceName
     * @param serviceDesc
     * @param application
     * @param inheritImpact
     * @param impactLevel
     */
    public void createBusinessService(String serviceName, String serviceDesc, String application,
        Boolean inheritImpact, String impactLevel) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            Thread.sleep(10000);
            // deleteBusinessService(serviceName);
            if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + serviceName)) {
                deleteBusinessService(serviceName);
            }

            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessservice.bsNameEdit"), serviceName);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessservice.bsDescriptionEdit"), serviceDesc);
            if (!application.isEmpty()) {
                WebdriverWrapper.selectList(driver,
                    getORPropValue("administration.businessservice.bsApplicationSelect"),
                    application);
            }
            if (inheritImpact) {
                WebdriverWrapper.selectCheckBox(driver,
                    getORPropValue("administration.businessservice.bsInheritImpactLevelCheck"));
            } else {
                WebdriverWrapper.deselectCheckBox(driver,
                    getORPropValue("administration.businessservice.bsInheritImpactLevelCheck"));
                WebdriverWrapper.selectList(driver,
                    getORPropValue("administration.businessservice.bsTransactionImpactSelect"),
                    impactLevel);
            }
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new session id parameter to an application (nameType should be empty string if it does
     * not apply)
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     * @param nameType
     */
    public void addSessionIdParamToApplication(String applicationName, String parameterType,
        String parameterName, String nameType) {
        try {
            // goToSessionIdParamsForApplication applicationName
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"), getORPropValue("linkText")
                    + applicationName,
                getORPropValue("administration.businessapplication.app.sessionidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.type"),
                    parameterType);
            if (!parameterType.equals("Cookie")) {
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            }
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nameselect"))) {
                WebdriverWrapper
                    .selectBox(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nameselect"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nameedit"),
                        parameterName);
            }

            if (!nameType.isEmpty()) {
                WebdriverWrapper
                    .selectBox(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                        nameType);
            }
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Add new session id parameter to an application (nameType should be empty string if it does
     * not apply)
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     * @param nameType
     * @param offset
     * @param length
     */
    public void addSessionIdAdvancedParamToApplication(String applicationName,
        String parameterType, String parameterName, String nameType, String offset, String length) {
        try {
            // goToSessionIdParamsForApplication applicationName
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"), getORPropValue("linkText")
                    + applicationName,
                getORPropValue("administration.businessapplication.app.sessionidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.type"),
                    parameterType);
            if (!parameterType.equals("Cookie")) {
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            }
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nameselect"))) {
                WebdriverWrapper
                    .selectBox(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nameselect"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nameedit"),
                        parameterName);
            }

            if (!nameType.isEmpty()) {
                WebdriverWrapper
                    .selectBox(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                        nameType);
            }
            WebdriverWrapper.click(driver, getORPropValue("button.advanced"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper
                .inputText(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.offset"),
                    offset);
            WebdriverWrapper
                .inputText(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.length"),
                    length);
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Add parameters for User Identification to an application
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     */
    public void addUserIdParamToApplication(String applicationName, String parameterType,
        String parameterName) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.navigateToPage(driver,
                getORPropValue("administration.businessapplication.app.useridentification"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.businessapplication.app.useridentification.type"),
                parameterType);
            if (!parameterType.equals("Post")) {
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            }
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.nameselect"))) {
                WebdriverWrapper
                    .selectBox(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.nameselect"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.nameedit"),
                        parameterName);
            }
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            LOGGER.info("User Added or edited with parameterType::" + parameterType
                + "  parameterName::" + parameterName + "  applicationName::" + applicationName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes an application and returns boolean value
     * 
     * @param applicationName
     * @return
     */
    public boolean deleteApplication(String applicationName) throws Exception {

        boolean confirm = false;
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")
            + applicationName)) {
            checkGridRow(
                getORPropValue("administration.businessapplication.applicationstablename"),
                applicationName);
            WebdriverWrapper.selectPopUp(driver, "accept");;
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            confirm = WebdriverWrapper.selectPopUp(driver, "accept");
            if (this
                .getMessageFromErrorDiv()
                .equals(
                    "Business Application(s) Avitek Application have been associated with transaction templates , cannot be deleted.")) {
                WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                    getORPropValue("administration.transactiondiscovery"));
                Autogen autogen = new Autogen();
                autogen.deleteAllTemplates();
                WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                    getORPropValue("administration.businessapplication"));
            }
        }
        return confirm;
    }

    /**
     * Deletes all Business transactions from the specified Business Service
     * 
     * @param serviceName
     */
    public void deleteAllBTsFromBS(String serviceName) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"), getORPropValue("linkText")
                    + serviceName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            // WebdriverWrapper.click(driver,getORPropValue("button.deleteAll"));

            try {
                driver.switchTo().alert().accept();
                System.out.println("Pre exisitng ALert FOUNDDDDDDDDDDD");

            } catch (Exception e) {
                System.out.println("No Pre exisitng ALert FOUNDDDDDDDDDDD");
            }

            /*
             * TakesScreenshot ts = (TakesScreenshot)driver;
             * File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
             * File DestFile=new File("C:\\results\\Alert2.jpg");
             * screenshotFile.renameTo(DestFile);
             */
            driver.findElement(By.xpath("//*[@name='deleteAll']")).click();
            Thread.sleep(4000);
            System.out.println("####    " + driver.switchTo().alert().getText());
            Thread.sleep(10000);
            driver.switchTo().alert().accept();

            /*
             * WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
             * //assertTrue(WebdriverWrapper.selectPopUp(driver, "accept").matches(
             * "^Are you sure you want to delete all Business Transactions from the Business Service[\\s\\S]$"
             * ));
             * WebdriverWrapper.selectPopUp(driver, "accept");
             * WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience wrapper to start TIM recording session passing only name and accepting defaults
     * for others
     * 
     * @param sessionName
     * @return
     */
    public String newRecordingSession(String sessionName) throws Exception {
        return newRecordingSession(true, sessionName, "", "", "", "");
    }

    /**
     * Attempt to initiate recording session, returns error message if any
     * Be sure you have a TIM enabled if you plan on TIM recording
     * 
     * @param sessionName
     * @param ipAddress
     * @param agentModifier
     * @param browserLangPattern
     * @return
     */
    public String newRecordingSession(Boolean recordingFromTim, String sessionName,
        String ipAddress, String agentModifier, String browserLangPattern, String encoding)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + sessionName)) {
            deleteRecordingSession(sessionName);
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.new"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        if (recordingFromTim) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.recording.new.recordingTimsRadio"));
        }
        if (!sessionName.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingSessionNameEdit"),
                sessionName);
        }
        if (!ipAddress.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingClientIpEdit"), ipAddress);
        }
        if (!agentModifier.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingAgentIdEdit"), agentModifier);
        }
        if (!browserLangPattern.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingBrowserLangEdit"),
                browserLangPattern);
        }
        if (!encoding.isEmpty()) {
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.recording.new.recordingDefaultEncodingSelect"),
                encoding);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.tran.record"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

        /*WebdriverWrapper.waitForObject(driver,
            getORPropValue("administration.recording.new.recordingTable"));
        WebdriverWrapper.click(driver, getORPropValue("button.stop"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, getORPropValue("button.finish"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);*/
        return getMessageFromErrorDiv();
    }

    /**
     * Stops the recording session
     */
    public void stopRecordingSessions() {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
            WebdriverWrapper.selectCheckBox(driver,
                "administration.recording.gridHeaderCheckAllCheckbox");
            WebdriverWrapper.click(driver, "administration.recording.stop");
            if (WebdriverWrapper.isAlertPresent(driver)) {
                WebdriverWrapper.selectPopUp(driver, "accept");
            }
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Returns the absolute path of Test data directory file
     * 
     * @param testDataFolder
     * @param testDataFile
     * @return
     */
    public String getTestDataFullPath(String testDataFolder, String testDataFile) {
        String delim = System.getProperty("file.separator");
        StringBuffer fullPath = new StringBuffer();
        fullPath.append(workingDir + delim + "testdata");
        fullPath.append(delim + testDataFolder + delim);
        fullPath.append(testDataFile);
        System.out.println("### Importing business transactions from: " + fullPath.toString());
        return fullPath.toString();
    }

    /**
     * Will enable monitoring for <b>all</b> transactions in a Business Service
     * 
     * @param serviceName
     */
    public void enableBusinessServiceMonitoring(String serviceName) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + serviceName);
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver, getORPropValue("button.enable"));
            WebdriverWrapper.waitForPageToLoad(driver, 30);
        } catch (Exception e) {
            System.out.println("Error while enabling BusinessService Monitoring");
            e.printStackTrace();

        }
    }

    /**
     * Get to a specific recorded transaction
     * 
     * @param sessionName
     * @param transactionName
     * @throws Exception
     */
    public void addCorrelationSLA(String UserGroupName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.correlationslas"));
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.correlationslas.usergroups"), "Unspecified Users");
        WebdriverWrapper.click(driver, getORPropValue("button.refresh"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("grid.header.checkbox"))) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
           
        }
        WebdriverWrapper.click(driver, getORPropValue("button.new"));
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.correlationslas.usergroups"), "Unspecified Users");
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.bsSuccessRateSLAEdit"), "90");
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.bsSigmaSLAEdit"), "4.5");
        WebdriverWrapper.inputText(driver,
            getORPropValue("administration.businessservice.bsTransactionTimeSla"), "5");
        WebdriverWrapper.click(driver, getORPropValue("button.name.save"));
    }

    /**
     * Adding a session ID parameter to a new parameter group
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     * @param nameType
     * @throws Exception
     */
    public void addNewSessionIdParamToNewParameterGroup(String applicationName,
        String parameterType, String parameterName, String nameType) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.sessionidentification"));
        WebdriverWrapper
            .click(
                driver,
                getORPropValue("administration.businessapplication.app.sessionidentification.newparametergrp"));
        WebdriverWrapper
            .selectList(
                driver,
                getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                parameterType);
        if (!parameterType.equals("Cookie"))
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nametype.nameselect"))) {
                WebdriverWrapper
                    .selectList(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nametype.nameselect"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.name"),
                        parameterName);
            }
        if (!nameType.isEmpty()) {
            WebdriverWrapper
                .selectList(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                    nameType);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Add first session ID parameter to a new parameter group
     * 
     * @param parameterType
     * @param parameterName
     * @param nameType
     * @throws Exception
     */
    public void addFirstSessionIdParamToNewParameterGroup(String parameterType,
        String parameterName, String nameType) throws Exception {
        WebdriverWrapper
            .click(
                driver,
                getORPropValue("administration.businessapplication.app.sessionidentification.newparametergrp"));
        WebdriverWrapper
            .selectList(
                driver,
                getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                parameterType);
        if (!parameterType.equals("Cookie"))
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nametype.nameselect"))) {
                WebdriverWrapper
                    .selectList(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.nametype.nameselect"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.sessionidentification.name"),
                        parameterName);
            }
        if (!nameType.isEmpty()) {
            WebdriverWrapper
                .selectList(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.nametype"),
                    nameType);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Go to new parameter group
     * 
     * @param sessionName
     * @param transactionName
     * @throws Exception
     */
    public void addFirstParameterToNewParameterGroup(String parameterType, String parameterName)
        throws Exception {
        WebdriverWrapper.click(driver,
            getORPropValue("button.app.useridentification.newparametergroup"));
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.app.useridentification.type"),
            parameterType);
        if (!parameterType.equals("Post"))
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.name.select"))) {
                WebdriverWrapper
                    .selectList(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.name.select"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.name"),
                        parameterName);
            }
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
    }

    /**
     * Add new parameter to new parameter group
     * 
     * @param sessionName
     * @param transactionName
     * @throws Exception
     */
    public void addNewParameterToNewParameterGroup(String applicationName, String parameterType,
        String parameterName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + applicationName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.businessapplication.app.useridentification"));
        WebdriverWrapper.click(driver,
            getORPropValue("button.app.useridentification.newparametergroup"));
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.businessapplication.app.useridentification.type"),
            parameterType);
        if (!parameterType.equals("Post"))
            if (WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("administration.businessapplication.app.useridentification.name.select"))) {
                WebdriverWrapper
                    .selectList(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.name.select"),
                        parameterName);
            } else {
                WebdriverWrapper
                    .inputText(
                        driver,
                        getORPropValue("administration.businessapplication.app.useridentification.name"),
                        parameterName);
            }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    /**
     * Add an Interim session id parameter to an application
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     */
    public void addInterimSessionIdParamToApplication(String applicationName, String parameterType,
        String parameterName) {
        try {
            WebdriverWrapper
                .navigateToPage(
                    driver,
                    getORPropValue("home.administration"),
                    getORPropValue("administration.businessapplication"),
                    getORPropValue("linkText") + applicationName,
                    getORPropValue("administration.businessapplication.app.interimsessionidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));;
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            // interimSessionIdTypeSelect interimSessionIdNameEdit
            WebdriverWrapper.selectList(driver,
                getORPropValue("administration.businessapplication.app.interimsession.type"),
                parameterType);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.interimsession.name"),
                parameterName);
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add parameters for User Group Identification to an application
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     * @param offset
     * @param length
     */
    public String addAdvancedUserGroupIdParamToApplication(String applicationName,
        String parameterType, String parameterName, String offset, String length) {
        String errMsg = "";
        try {

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"), getORPropValue("linkText")
                    + applicationName,
                getORPropValue("administration.businessapplication.app.usergroupidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessapplication.app.UG.advanced"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.type"),
                    parameterType);
            if (!parameterType.equals("Post")) {
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);;
            }
            if (WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.businessapplication.app.UG.nameselect"))) {
                WebdriverWrapper.selectList(driver,
                    getORPropValue("administration.businessapplication.app.UG.nameselect"),
                    parameterName);
            } else {
                WebdriverWrapper.inputText(driver,
                    getORPropValue("administration.businessapplication.app.UG.nameedit"),
                    parameterName);
            }
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.UG.offset"), offset);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessapplication.app.UG.length"), length);
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            if (offset.trim().equals("") || length.trim().equals("")) {
                if (WebdriverWrapper.isObjectPresent(driver,
                    getORPropValue("administration.businessapplication.app.UG.message"))) {
                    if (WebdriverWrapper.isTextInSource(driver, "Offset is required."))
                        errMsg = "Offset is required.";
                    if (WebdriverWrapper.isTextInSource(driver, "Length is required."))
                        errMsg = errMsg + "Length is required.";
                    LOGGER.info("Error message in UserId Advance Parm ::" + errMsg);
                }

            }

            LOGGER.info("UserGroup Added or edited with parameterType::" + parameterType
                + "  parameterName::" + parameterName + "  applicationName::" + applicationName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errMsg;
    }

    /**
     * Add parameters for User Group Identification to an application
     * 
     * @param applicationName
     * @param parameterType
     * @param parameterName
     */
    public void addUserGroupIdParamToApplication(String applicationName, String parameterType,
        String parameterName) {
        try {

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"), getORPropValue("linkText")
                    + applicationName,
                getORPropValue("administration.businessapplication.app.usergroupidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.type"),
                    parameterType);
            if (!parameterType.equals("Post")) {
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);;
            }
            if (WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.businessapplication.app.UG.nameselect"))) {
                WebdriverWrapper.selectList(driver,
                    getORPropValue("administration.businessapplication.app.UG.nameselect"),
                    parameterName);
            } else {
                WebdriverWrapper.inputText(driver,
                    getORPropValue("administration.businessapplication.app.UG.nameedit"),
                    parameterName);
            }
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            LOGGER.info("UserGroup Added or edited with parameterType::" + parameterType
                + "  parameterName::" + parameterName + "  applicationName::" + applicationName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get to a specific recorded transaction
     * 
     * @param sessionName
     * @param transactionName
     * @throws Exception
     */
    public void goToExistingRecordedTransaction(String sessionName, String transactionName)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver, getORPropValue("linkTEXT") + sessionName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + transactionName);
    }

    /**
     * Go to an existing recording session
     * 
     * @param sessionName
     * @throws Exception
     */
    public void openExistingRecordingSession(String sessionName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver,getORPropValue("linkText")+sessionName);
    }

    /**
     * While in the record/stop/record next process, after stopping call this method to rename a txn
     * based on the url parameter
     * 
     * @param transactionUrl
     * @param newName
     * @throws Exception
     */
    public void updateRecordedTransactionNameBeforeFinishedRecording(String transactionUrl,
        String newName) throws Exception {
        Integer colNum = getColNumByColTitle("comp", "URL");
        Integer rowNum = getRowNumByContentAndColumn("comp", transactionUrl, colNum);
        String id = getAttributeFromCell("comp", rowNum, 1, "//input/@value");
        WebdriverWrapper.inputText(driver, "xpath_//input[@name='" + id + "']", newName);
        WebdriverWrapper.selectCheckBox(driver, "xpath_//input[@value='" + id + "']");
        WebdriverWrapper.click(driver, getORPropValue("administration.recording.updaterecBtn"));
    }

    /**
     * Go to an existing recording session and click record new transaction button
     * 
     * @param sessionName
     * @throws Exception
     */
    public void startExistingRecordingSession(String sessionName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver, getORPropValue("linkTEXT") + sessionName);
        WebdriverWrapper.click(driver, getORPropValue("administration.recording.recBtn"));
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * Promote a transaction to overwrite existing transaction from a recording session to a
     * Business Service
     * 
     * @param sessionName
     * @param serviceName
     * @param transactionName
     * @throws Exception
     */
    public void promoteRecordingSessionOverwrite(String sessionName, String serviceName,
        String transactionToBePromoted, String transactionToOverwrite) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver, getORPropValue("linkTEXT") + sessionName);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.recording.promoteServiceSelect"), serviceName);
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        // sel.waitForPageToLoad(timeout);
        String transactionPid = getPIdForName("tranList", transactionToBePromoted);
        WebdriverWrapper.selectList(driver, transactionPid, transactionToOverwrite);
        checkGridRow("tranList", transactionToBePromoted);
        WebdriverWrapper.click(driver, getORPropValue("button.promote"));
    }

    /**
     * Promote a single or All transactions from a recording session to a Business Service
     * Pass 'All' for transaction name to get all
     * 
     * @param sessionName
     * @param serviceName
     * @param transactionName
     * @throws Exception
     */
    public void promoteRecordingSession(String sessionName, String serviceName,
        String transactionName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
        WebdriverWrapper.click(driver, getORPropValue("button.stop"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        LOGGER.info("Recoding Session object present::"+WebdriverWrapper.isObjectPresent(driver,getORPropValue("linkText") + sessionName)+":::"+sessionName);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + sessionName);
        WebdriverWrapper.selectList(driver,
            getORPropValue("administration.recording.promoteServiceSelect"), serviceName);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        // sel.waitForPageToLoad(timeout);
        // driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        if (transactionName.equals("All")) {
            WebdriverWrapper.click(driver, getORPropValue("grid.header.checkbox"));
        } else {
            // checkGridRow("tranList", transactionName);
            checkBTRow(transactionName);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.promote"));
    }

    /**
     * Edit an existing session. initialSessionName is required. If empty strings are passed the
     * field is not edited.
     * 
     * @param initialSessionName
     * @param newSessionName
     * @param clientIp
     * @param browserLangPattern
     * @param defaultEncoding
     * @throws Exception
     */
    public void editRecordingSession(String initialSessionName, String newSessionName,
        String clientIp, String browserLangPattern, String defaultEncoding) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + initialSessionName);
        WebdriverWrapper.click(driver,
            getORPropValue("administration.recording.recordingGeneralLink"));
        if (!newSessionName.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingSessionNameEdit"),
                newSessionName);
        }
        if (!clientIp.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingClientIpEdit"), clientIp);
        }
        if (!browserLangPattern.isEmpty()) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.recording.new.recordingBrowserLangselect"));
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingBrowserLangEdit"),
                browserLangPattern);
        }
        if (!defaultEncoding.isEmpty()) {
            WebdriverWrapper.selectList(driver,
                getORPropValue("administration.recording.new.recordingDefaultEncodingSelect"),
                defaultEncoding);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
    }

    /**
     * Create a User Group with all parameters
     * 
     * @param userGroupName
     * @param userGroupDescription
     * @param inheritImpactFromDomain
     * @param impactLevel
     * @param groupNewUsersBySubnet
     * @param subnetAddressEdit
     * @param subnetMask
     */
    public String addUserGroup(String userGroupName, String userGroupDescription,
        Boolean inheritImpactFromDomain, String impactLevel, Boolean groupNewUsersBySubnet,
        String subnetAddress, String subnetMask) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.inputText(driver, getORPropValue("admin.ug.userGroupNameEdit"),
                userGroupName);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.inputText(driver, getORPropValue("admin.ug.userGroupDescriptionEdit"),
                userGroupDescription);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            if (!inheritImpactFromDomain) {
                WebdriverWrapper.deselectCheckBox(driver,
                    getORPropValue("admin.ug.ImpactLevelCheckbox"));
                WebdriverWrapper.selectList(driver,
                    getORPropValue("admin.ug.userGroupImpactLevelSelect"), impactLevel);
            } else {
                WebdriverWrapper.selectCheckBox(driver,
                    getORPropValue("admin.ug.ImpactLevelCheckbox"));
            }
            if (groupNewUsersBySubnet) {
                WebdriverWrapper.selectCheckBox(driver,
                    getORPropValue("admin.ug.groupNewUsersBySubnetCheck"));
                WebdriverWrapper.inputText(driver, getORPropValue("admin.ug.subnetAddressEdit"),
                    subnetAddress);
                WebdriverWrapper.inputText(driver, getORPropValue("admin.ug.subnetMaskEdit"),
                    subnetMask);
            } else {
                WebdriverWrapper.deselectCheckBox(driver,
                    getORPropValue("admin.ug.groupNewUsersBySubnetCheck"));
            }
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.click(driver, getORPropValue("button.save"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            return getMessageFromErrorDiv();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a user group by name
     * 
     * @param userGroupName
     * @return
     */
    public void deleteUserGroup(String userGroupName) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            if (WebdriverWrapper
                .isObjectPresent(driver, getORPropValue("linkText") + userGroupName)) {
                // this.checkGridRow("userdef", userGroupName);
                WebdriverWrapper.click(driver, getORPropValue("linkText") + userGroupName);
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.navigateToPage(driver, getORPropValue("admin.ug.generaltab"));
                WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
                //WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.selectPopUp(driver, "accept");
            } else
                LOGGER.info("userGroup not exist::" + userGroupName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a user group by name
     * 
     * @param userGroupName
     * @return
     */
    public void deleteAllUserGroup() {
        try {
            int rowCount = 0;
            int i = 1;
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));

            rowCount =
                WebdriverWrapper.findElementsByType(driver,
                    getORPropValue("administration.usergroups.table")).size();
            for (; i < rowCount; i++) {
                String tmp =
                    WebdriverWrapper.getElementText(driver,
                        getORPropValue("administration.usergroups.table") + "[" + i + "]/td[2]");
                if ("Unspecified Users".equalsIgnoreCase(tmp.trim())
                    || "New Users".equalsIgnoreCase(tmp.trim())) {

                    WebdriverWrapper.click(driver,
                        getORPropValue("administration.usergroups.table") + "[" + i + "]/td[1]");
                }

            }
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectPopUp(driver, "accept");

            LOGGER.info("All userGroup deletedt::");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a user group by name
     * 
     * @param parameterNameUID
     * 
     */
    public void deleteUserID(String parameterNameUID) {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")
                + parameterNameUID)) {
                // this.checkGridRow("userdef", userGroupName);
                WebdriverWrapper.click(driver, getORPropValue("linkText") + parameterNameUID);
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.click(driver, getORPropValue("button.delete"));
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.selectPopUp(driver, "accept");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inactive a user All
     * 
     */
    public void deActiveUserID() {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.deactive"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectPopUp(driver, "accept");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllUserID() {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectPopUp(driver, "accept");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * user Search by name
     * 
     */
    public boolean userIDSearch(String userName) {
        int i = 0;
        boolean result = false;
        long sleepTime = 30000;
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            while (i < 5) {
                if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + userName)) {
                    result = true;
                    break;
                } else {
                    LOGGER.info("Process sleep for:::" + sleepTime);
                    Thread.sleep(sleepTime);
                    WebdriverWrapper.click(driver,
                        getORPropValue("administration.usergroups.userSearch.btnSearch"));
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.deactive"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectPopUp(driver, "accept");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllUserID() {
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            WebdriverWrapper.selectPopUp(driver, "accept");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all applications except the Default application and returns a confirmation message
     * 
     * @return
     */
    public String deleteAllAppExceptDefaultApp() {
        String confirm = "";
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"));
            if (getRowCountFromTable("appdef") != 1) {
                checkAllGridRows("appdef");
                uncheckGridRow("appdef", "Default Application");

                WebdriverWrapper.clickDisable(driver, getORPropValue("button.delete"));
                //WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.selectPopUp(driver, "accept");
                if (this
                    .getMessageFromErrorDiv()
                    .equals(
                        "Business Application(s) Avitek Application have been associated with transaction templates , cannot be deleted.")) {
                    WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                        getORPropValue("administration.transactiondiscovery"));
                    autogen.deleteAllTemplates();
                    deleteAllAppExceptDefaultApp();
                }
                confirm = "deleted";
            }

            return confirm;
        } catch (Exception e) {
            e.printStackTrace();
            return confirm;
        }
    }

    /**
     * Deletes a business service and returns the confirm string
     * 
     * @param businessProcess
     * @return
     */
    public String deleteAllBusinessService() {
        String confirm = "";
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            if (getRowCountFromTable("tranDefGroup") != 1) {
                checkAllGridRows("tranDefGroup");
                uncheckGridRow("tranDefGroup", "Discovered Transactions");
                WebdriverWrapper.click(driver, getORPropValue("button.delete"));
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                WebdriverWrapper.selectPopUp(driver, "accept");
                confirm = "deleted";
            }
            return confirm;
        } catch (Exception e) {
            e.printStackTrace();
            return confirm;
        }
    }

    public String getRecordPlugInParam(String sessionName) throws Exception {
        String tblPath = "xpath_//*[@id='paramDef']/tbody/tr";
        String tblPath2 = "]/td[";
        String tblPath3 = "]";
        String tblPath4 = "[";
        String component_id_value = "";
        int rowCount = 0;
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + sessionName);
        WebdriverWrapper
            .click(driver, getORPropValue("administration.recordingsessions.trns.name"));
        WebdriverWrapper.click(driver,
            getORPropValue("administration.recordingsessions.trns.parameters"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        rowCount = WebdriverWrapper.findElementsByType(driver, tblPath).size();
        for (int i = 1; i <= rowCount; i++) {
            if ("component-id".equalsIgnoreCase(WebdriverWrapper.getElementText(driver,
                tblPath + tblPath4 + i + tblPath2 + 2 + tblPath3).trim())) {
                component_id_value =
                    WebdriverWrapper.getElementText(driver, tblPath + tblPath4 + i + tblPath2 + 3
                        + tblPath3);
                break;
            }
        }
        return component_id_value;
    }

    public String newRecordingSession(Boolean recordingFromTim, String sessionName,
        String ipAddress, String agentModifier, String browserLangPattern, String encoding,
        String scriptName, String url, String count) throws Exception {
        int delay = 0;
        int i = 0;
        boolean result = false;
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.recordingsessions"));

        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + sessionName)) {
            deleteRecordingSession(sessionName);
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.new"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        if (recordingFromTim) {
            WebdriverWrapper.selectCheckBox(driver,
                getORPropValue("administration.recording.new.recordingTimsRadio"));
        }
        if (!sessionName.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingSessionNameEdit"),
                sessionName);
        }
        if (!ipAddress.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingClientIpEdit"), ipAddress);
        }
        if (!agentModifier.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingAgentIdEdit"), agentModifier);
        }
        if (!browserLangPattern.isEmpty()) {
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.recording.new.recordingBrowserLangEdit"),
                browserLangPattern);
        }
        if (!encoding.isEmpty()) {
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.recording.new.recordingDefaultEncodingSelect"),
                encoding);
        }
        WebdriverWrapper.click(driver, getORPropValue("button.record"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        delay = iGlobalTimeout / 4;
        runScriptOnUnix(scriptName.trim() + " " + url + count);
        Thread.sleep(delay);
        result =
            WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.recording.new.recordingTable"));
        i++;
        while (!result) {
            runScriptOnUnix(scriptName.trim() + " " + url + count);
            result =
                WebdriverWrapper.isObjectPresent(driver,
                    getORPropValue("administration.recording.new.recordingTable"));
            Thread.sleep(delay);
            i++;
            if (i == 4) result = true;
        }
       WebdriverWrapper.click(driver, getORPropValue("button.stop"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, getORPropValue("button.finish"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        return getMessageFromErrorDiv();

    }
/**
     * Edit existing User Group Id param
     * 
     * @param parameterName
     * @param newParameterName
     * @throws Exception
     */
 public void editUserGroupIdParam(String parameterName, String newParameterName)
        throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.usergroups"));
        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + parameterName)) {
            WebdriverWrapper.click(driver, getORPropValue("linkText") + parameterName);
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.General"));
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.usergroups.General.name"), newParameterName);
            WebdriverWrapper
                .click(driver, getORPropValue("administration.usergroups.General.save"));
        } else
            LOGGER.info("parameter not present::" + parameterName);
 }

   public boolean moveUserIDToOtherGroup(String userName, String ug2) {
        int rowCount = 0;
        int i = 1;

        boolean result = false;
        try {
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.usergroups"));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups.userSearch"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.usergroups.userSearch.btnSearch"));

            if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText") + userName)) {

                rowCount =
                    WebdriverWrapper.findElementsByType(driver,
                        getORPropValue("administration.usergroups.General.usertable")).size();
                for (; i <= rowCount; i++) {
                    String tmp =
                        WebdriverWrapper.getElementText(driver,
                            getORPropValue("administration.usergroups.General.usertable") + "[" + i
                                + "]/td[2]");
                    String status =
                        WebdriverWrapper.getElementText(driver,
                            getORPropValue("administration.usergroups.General.usertable") + "[" + i
                                + "]/td[7]");

                    if (userName.equalsIgnoreCase(tmp.trim())
                        && "Active".equalsIgnoreCase(status.trim())) {
                        LOGGER.info("found User Name in Table:::" + tmp);
                        WebdriverWrapper.click(driver,
                            getORPropValue("administration.usergroups.General.usertable") + "[" + i
                                + "]/td[1]/*[@id='idList']");
                        WebdriverWrapper.selectBox(driver,
                            getORPropValue("administration.usergroups.General.moveToList"), ug2);
                        WebdriverWrapper.click(driver, getORPropValue("button.move"));
                        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                        WebdriverWrapper.selectPopUp(driver, "accept");
                        result = true;
                        break;
                    }

                }
                if (i == rowCount)
                    LOGGER.info("No matching user found::moveUserIDToOtherGroup did not hapen");

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addXMLTypeToAppplicationSessionOrUserOrUserGroupIdentification(String type,
        String appName, String parameterType, String sessionName, String businessTransaction,

        String xmlAttribute) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
            getORPropValue("administration.businessapplication"));
        WebdriverWrapper.click(driver, getORPropValue("linkText") + appName);
        if ("user".equalsIgnoreCase(type)) {
            WebdriverWrapper.navigateToPage(driver,
                getORPropValue("administration.businessapplication.app.useridentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.businessapplication.app.useridentification.type"),
                parameterType);
        } else if ("userGroup".equalsIgnoreCase(type)) {
            WebdriverWrapper.navigateToPage(driver,
                getORPropValue("administration.businessapplication.app.usergroupidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.businessapplication.app.usergroup.type"),
                parameterType);
        } else if ("session".equalsIgnoreCase(type)) {
            WebdriverWrapper.navigateToPage(driver,
                getORPropValue("administration.businessapplication.app.sessionidentification"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            WebdriverWrapper
                .selectBox(
                    driver,
                    getORPropValue("administration.businessapplication.app.sessionidentification.type"),
                    parameterType);
        }
        WebdriverWrapper.selectBox(driver,
            getORPropValue("administration.businessapplication.app.xml.recordingSessionId"),
            sessionName);
        WebdriverWrapper.selectBox(driver,
            getORPropValue("administration.businessapplication.app.xml.recordingComponentId"),
            businessTransaction);
        WebdriverWrapper.click(driver, getORPropValue("linkText") + xmlAttribute);
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
    }

    public void checkBTRow(String name) throws Exception {
        int rowCount = 0;
        int i = 0;

        rowCount =
            WebdriverWrapper.findElementsByType(driver,
                getORPropValue("administration.recordingsessions.trns.table")).size();
        for (; i < rowCount; i++) {
            String tmp =
                WebdriverWrapper.getElementText(driver,
                    getORPropValue("administration.recordingsessions.trns.table") + "[" + (i + 1)
                        + "]/td[2]");
            if (name.equalsIgnoreCase(tmp.trim())) {

                WebdriverWrapper.click(driver,
                    getORPropValue("administration.recordingsessions.trns.table") + "[" + (i + 1)
                        + "]/td[1]");
            }

        }


    }

    public StringBuffer runScriptOnUnix(String scriptFileName) {
        return Util.runScriptOnUnix(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, scriptFileName);

    }
    
    public void deleteAllRecordingSession()throws Exception{
        
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.recordingsessions"));

        if (WebdriverWrapper.isObjectPresent(driver, getORPropValue("grid.header.checkbox"))) {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("grid.header.checkbox"));        
            WebdriverWrapper.clickDisable(driver,getORPropValue("button.delete"));
            if(WebdriverWrapper.isAlertPresent(driver)){
                WebdriverWrapper.selectPopUp(driver,"accept");
            }
        }
    }

        /**
         * This is used to select the BA, BS and BT.
         */
            public void selectTransactionInformation(String applicationName, String ServiceName, String transactionName)
            {
                try{
                WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.SearchByTransactionInformation.BusinessApplicationDropDown"), applicationName);
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                //cem.incidentManagement.defects.SearchByTransactionInformation.BusinessServiceDropDown
                WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.SearchByTransactionInformation.BusinessServiceDropDown"), ServiceName);
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                //cem.incidentManagement.defects.SearchByTransactionInformation.BusinessTransactionDropDown
                WebdriverWrapper.selectBox(driver, getORPropValue("cem.incidentManagement.defects.SearchByTransactionInformation.BusinessTransactionDropDown"), transactionName);
                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                }catch(Exception e)
                {
                    LOGGER.info(e.toString());
                }
            }
            
            /**
             * To open a businessService using its name
             * 
             */
            public void openBusinessService(String BSName)
            {
                try{
                    WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
                    String table = "//*[@id='tranDefGroup']/tbody/tr";
                    int size = driver.findElements(By.xpath(table)).size();
                    for(int i=1;i<=size;i++)
                    {
                        if((WebdriverWrapper.getElementText(driver, "xpath_" + table + "["+i+"]/td[2]/a")).trim().equalsIgnoreCase(BSName.trim()))
                            WebdriverWrapper.click(driver, "xpath_" + table + "["+i+"]/td[2]/a");
                    }
                }catch(Exception e)
                {
                    LOGGER.info(e.toString());
                }
            }
            
            
            /**
             * To move a business Transaction  under BusinessService 1 to Business Service 2
             * @param Business Service Names From and To 
             */
                public void moveBusinessTransactions(String BSName1, String BSNname2)
                {
                    try{
                        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
                        String table = "//*[@id='tranDefGroup']/tbody/tr";
                        String tableTransetGroupHeader = "//*[@id='tranSetDef']/thead/tr/th[1]/input";
                        int size = driver.findElements(By.xpath(table)).size();
                        for(int i=1;i<=size;i++)
                        {
                            if((WebdriverWrapper.getElementText(driver, "xpath_" + table + "["+i+"]/td[2]/a")).trim().equalsIgnoreCase(BSName1.trim()))
                            {
                                WebdriverWrapper.click(driver, "xpath_" + table + "["+i+"]/td[2]/a");
                                WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

                                if(WebdriverWrapper.isObjectPresent(driver, "xpath_"+tableTransetGroupHeader))
                                {
                                    WebdriverWrapper.click(driver, "xpath_" + tableTransetGroupHeader);
                                    WebdriverWrapper.selectBox(driver, "xpath_//*[@id='tranDefGroupList']", BSNname2);
                                    driver.findElement(By.xpath("//*[@name='move']")).click();
                                    WebdriverWrapper.selectPopUp(driver, "accept");

                                }
                            }
                        }
                    }catch(Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
            }
                /**
                 * disable the BTs under different BSs.
                 */
                
                public void disableBTsforAllBS()
                {
                    try{
                        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
                        String table = "//*[@id='tranDefGroup']/tbody/tr";
                        String tableTransetGroupHeader = "//*[@id='tranSetDef']/thead/tr/th[1]/input";
                        //String disableButton = "//*[@name='disable'and@value='Disable']";
                        int size = driver.findElements(By.xpath(table)).size();
                        LOGGER.info("The size of the table is"+size);
                        for(int i=1;i<=size;i++)
                        {
                            if(!WebdriverWrapper.getElementText(driver, "xpath_" + table + "["+i+"]/td[2]/a").trim().equalsIgnoreCase("Discovered Transactions"))
                            {    
                                WebdriverWrapper.click(driver, "xpath_" + table + "["+i+"]/td[2]/a");
                                if(WebdriverWrapper.isObjectPresent(driver, "xpath_"+tableTransetGroupHeader))
                                {
                                    WebdriverWrapper.click(driver, "xpath_" + tableTransetGroupHeader);
                                    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                                    //driver.findElement(By.xpath("//*[@name='disable']")).click();
                                    WebdriverWrapper.click(driver, getORPropValue("administration.businessservice.trasactionsDisableButton"));// + disableButton);
                                    WebdriverWrapper.selectPopUp(driver, "accept");
                                    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                                }
                            }
                        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));

                        }
                    }catch(Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
            }
                /**
                 * 
                 * @param toDo {Values are enable and disable}
                 */
                
                public void disableEnableBTsforAllBS(String toDo)
                {
                    try{
                        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
                        String table = "//*[@id='tranDefGroup']/tbody/tr";
                        String tableTransetGroupHeader = "//*[@id='tranSetDef']/thead/tr/th[1]/input";
                        //String disableButton = "//*[@name='disable'and@value='Disable']";
                        int size = driver.findElements(By.xpath(table)).size();
                        LOGGER.info("The size of the table is"+size);
                        for(int i=1;i<=size;i++)
                        {
                            if(!WebdriverWrapper.getElementText(driver, "xpath_" + table + "["+i+"]/td[2]/a").trim().equalsIgnoreCase("Discovered Transactions"))
                            {    
                                WebdriverWrapper.click(driver, "xpath_" + table + "["+i+"]/td[2]/a");
                                if(WebdriverWrapper.isObjectPresent(driver, "xpath_"+tableTransetGroupHeader))
                                {
                                    if(toDo.trim().equalsIgnoreCase("disable")){
                                    WebdriverWrapper.click(driver, "xpath_" + tableTransetGroupHeader);
                                    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                                    //driver.findElement(By.xpath("//*[@name='disable']")).click();
                                    WebdriverWrapper.click(driver, getORPropValue("administration.businessservice.trasactionsDisableButton"));// + disableButton);
                                    WebdriverWrapper.selectPopUp(driver, "accept");
                                    WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                                    }
                                    else if(toDo.trim().equalsIgnoreCase("enable"))
                                    {
                                        WebdriverWrapper.click(driver, "xpath_" + tableTransetGroupHeader);
                                        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
                                        WebdriverWrapper.click(driver, getORPropValue("administration.businessservice.trasactionsEnableButton"));// + disableButton);
                                    }   
                                }       
                                WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
                            }

                        }
                    }catch(Exception e)
                    {
                        LOGGER.info(e.toString());
                    }
            }  
            

}
