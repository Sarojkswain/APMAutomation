package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.QaFileUtils;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Autogen extends JBaseTest{

    String templateName;    
    String autogenUI = "Transaction Discovery";
    String ipAddress;
    String busiServices = "Business Services";
    String urlPathFilter;
    String contentTypeFilter;
    String appDefName;
    String autogenBS = "Discovered Transactions";
    int sleepTimeout = 5000;
    
    public Autogen(){
        //empty constructor
    }
    
	public Autogen(WebDriver driver){
        this.driver = driver;
    }
    public void setAutogenUI() throws Exception{
        gotoAutogenUI();
    }
    
    // go to Transaction Discovery UI
    public void gotoAutogenUI() throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.transactiondiscovery"));
    
    }
    
    // go to Business Services UI
    public void gotoBSUI() throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        
    }
    
    // Go to Discovered Transactions UI
    public void gotoDTsUI() throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"),getORPropValue("linkText")+autogenBS);
    }
    
    // Go to BTs in Discovered Transactions
    public void gotoDTBTsUI(int rowNumber) throws Exception{
        gotoDTsUI();
    }
    /** 
     * starts transaction discovery
     * @throws Exception
     */
    public void startAutogen() throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.transactiondiscovery"));
        LOGGER.info("Going into Sleep Command for " + sleepTimeout);
        Thread.sleep(sleepTimeout);
        if(WebdriverWrapper.verifyTextPresent(driver,getORPropValue("administration.autogen.stoppedStatusMsg"))
                || WebdriverWrapper.isElementEnabled(driver, getORPropValue("administration.autogen.startBtn")))
        {
            WebdriverWrapper.click(driver, getORPropValue("administration.autogen.startBtn"));
            int i=1;
            while(!WebdriverWrapper.verifyTextPresent(driver, getORPropValue("administration.autogen.runningStatusMsg")))
            {
                LOGGER.info("Going into Sleep Command for " + sleepTimeout); 
                Thread.sleep(sleepTimeout);
                WebdriverWrapper.pageRefresh(driver);
                i++;
                if (i==5) break;
            }
        }
    }
    
    /**
     * Stop transaction discovery
     * @throws Exception
     */
  public void stopAutogen() throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.transactiondiscovery"));
        LOGGER.info("Going into Sleep Command for " + sleepTimeout);
        Thread.sleep(sleepTimeout);
        if(WebdriverWrapper.verifyTextPresent(driver,getORPropValue("administration.autogen.runningStatusMsg"))
                || WebdriverWrapper.isElementEnabled(driver, getORPropValue("administration.autogen.stopBtn")))
        {
            WebdriverWrapper.click(driver, getORPropValue("administration.autogen.stopBtn"));
            int i=1;
            while(!WebdriverWrapper.verifyTextPresent(driver, getORPropValue("administration.autogen.stoppedStatusMsg")))
            {
                LOGGER.info("Going into Sleep Command for " + sleepTimeout);
                Thread.sleep(sleepTimeout);
                WebdriverWrapper.pageRefresh(driver);
                i++;
                if (i==5) break;
            }
        }
    }
    
    

    /**
     * set the three parameters (Limit the Number of Transactions, 
     *                          Stop discovering transactions after, Path Parameter Separator) in Discovered Transactions UI
     * @param checkLimit
     * @param limitNumber
     * @param checkStopAutogen
     * @param stopMinutes
     * @param pathSeparator
     * @throws Exception
     */
    public void addGeneralParams(boolean checkLimit, String limitNumber, boolean checkStopAutogen, String stopMinutes, String pathSeparator) throws Exception{
        if (checkLimit){
            if(!WebdriverWrapper.isElementSelected(driver, getORPropValue("administration.autogen.checkLimit")))
            {
                WebdriverWrapper.click(driver, getORPropValue("administration.autogen.checkLimit"));
            }
            WebdriverWrapper.click(driver,getORPropValue("administration.autogen.maxBusinessTransactions"));
            WebdriverWrapper.inputText(driver, getORPropValue("administration.autogen.maxBusinessTransactions"), limitNumber);
        }
        if(checkStopAutogen){
            if(!WebdriverWrapper.isElementSelected(driver, getORPropValue("administration.autogen.autogenDisabled")))
            {
                WebdriverWrapper.click(driver, getORPropValue("administration.autogen.autogenDisabled"));
            }
            WebdriverWrapper.inputText(driver, getORPropValue("administration.autogen.autogenDisableInterval"), stopMinutes);
        }
        WebdriverWrapper.selectList(driver, getORPropValue("administration.autogen.pathParamSeparatorSelect"), pathSeparator);
        WebdriverWrapper.click(driver, getORPropValue("button.name.save"));
        //driver.findElement(By.xpath("//input[@name='save']")).click();
        //WebdriverWrapper.submitButton(driver,getORPropValue("administration.autogen.saveBtn"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    }
    

    /**
     * add a transaction template in Discovered Transactions UI 
     * first deletes if template with the same templateName exists and then adds a new template
     * @param templateName
     * @param urlPathFilter
     * @param contentTypeFilter
     * @param appDefName
     * @throws Exception
     */
    public void addTemplate(String templateName, String urlPathFilter, String contentTypeFilter, String appDefName  ) throws Exception {
                
        if(WebdriverWrapper.verifyTextPresent(driver, templateName))
        {
            WebdriverWrapper.click(driver, getORPropValue("administration.autogen.idList"));
            WebdriverWrapper.click(driver, getORPropValue("administration.autogen.delete"));
            assertTrue(WebdriverWrapper.getPopUpText(driver).matches("^Are you sure you want to delete this transaction template[\\s\\S]$"));
        }
        WebdriverWrapper.click(driver, getORPropValue("administration.autogen.newBtn"));
        WebdriverWrapper.inputText(driver, getORPropValue("administration.autogen.templateName"), templateName);
        WebdriverWrapper.inputText(driver,getORPropValue("administration.autogen.urlPath"),urlPathFilter);
        WebdriverWrapper.inputText(driver,getORPropValue("administration.autogen.contentType"),contentTypeFilter);
        WebdriverWrapper.selectList(driver,getORPropValue("administration.autogen.appDef"),appDefName);
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
        WebdriverWrapper.click(driver, getORPropValue("administration.autogen.saveBtn"));

    }
    
    /**
     * deletes a transaction template from Discovered Transactions UI
     * @throws Exception
     */
    public void deleteAllTemplates() throws Exception {
        if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("administrator.autogen.templatesTable")))
        {
            WebdriverWrapper.click(driver, getORPropValue("grid.header.checkbox"));
            
            try {
                driver.switchTo().alert().accept();
                LOGGER.info("Pre exisitng ALert FOUNDDDDDDDDDDD");
                
            } catch(Exception e) {
                LOGGER.info("No Pre exisitng ALert FOUNDDDDDDDDDDD");
            }
            
            TakesScreenshot ts = (TakesScreenshot)driver;
            File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
            File DestFile=new File("C:\\Alert.jpg");
            screenshotFile.renameTo(DestFile);
            driver.findElement(By.xpath("//input[@name='delete']")).click();
            LOGGER.info("Going into Sleep Command for " + sleepTimeout);
            Thread.sleep(sleepTimeout);
            LOGGER.info("####    " + driver.switchTo().alert().getText());
            LOGGER.info("Going into Sleep Command for " + sleepTimeout);
            Thread.sleep(sleepTimeout);
            driver.switchTo().alert().accept();
        
        }
    }
    
    /** 
     * adds a parameter for a transaction template following 'Number of Parameters' link from an existing transaction template
     * @param templateName
     * @param URLType
     * @param URLName
     * @param actionEdit
     * @param patternEdit
     * @param isRequired
     * @throws Exception
     */
    public void addParameter(String templateName, String URLType, String URLName, String actionEdit, String patternEdit, boolean isRequired) throws Exception {
        
        WebdriverWrapper.click(driver, getORPropValue("linkText")+templateName);
        WebdriverWrapper.click(driver,getORPropValue("administration.autogen.newBtn"));
        //WebdriverWrapper.selectList(driver, getORPropValue("administrator.autogen.parameterType"), URLType);
        new Select(driver.findElement(By.name("type"))).selectByVisibleText(URLType);
        LOGGER.info("Going into Sleep Command for " + sleepTimeout);
        Thread.sleep(sleepTimeout);
        if(URLType == "URL"){
            //WebdriverWrapper.selectList(driver,getORPropValue("administrator.autogen.parameterName"), URLName);
            new Select(driver.findElement(By.name("name"))).selectByVisibleText(URLName);
        }
        else
            WebdriverWrapper.inputText(driver, getORPropValue("administrator.autogen.parameterName"), URLName);
        WebdriverWrapper.selectList(driver, getORPropValue("administrator.autogen.parameterAction"), actionEdit);
        WebdriverWrapper.inputText(driver, getORPropValue("administrator.autogen.parameterPattern"), patternEdit);
        if(isRequired)
        {
            WebdriverWrapper.click(driver,getORPropValue("administrator.autogen.parameterRequired"));
        }
        WebdriverWrapper.click(driver, getORPropValue("button.save"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    }
    
    /**
     * deletes a parameter from a transaction template
     * @param templateName
     * @throws Exception
     */
    public void deleteParameter(String templateName) throws Exception {
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.transactiondiscovery"));
        WebdriverWrapper.click(driver, getORPropValue("linkText")+templateName);
        WebdriverWrapper.selectList(driver, "name_pTemplateId1", "label="+templateName);
        WebdriverWrapper.click(driver, "name_idList");
        WebdriverWrapper.click(driver, "name_deleteBtn");
        assertTrue(WebdriverWrapper.getPopUpText(driver).matches("^Are you sure you want to delete the selected Parameter Types [\\s\\S]$"));
        WebdriverWrapper.selectPopUp(driver, "accept");
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }
    
     
    /**
     * enables all transaction templates in Discovered Transactions UI
     * @throws Exception
     */
    public void enableTemplate() throws Exception {
        gotoAutogenUI();
        WebdriverWrapper.click(driver, getORPropValue("administration.discoveredtxn.allbtcheckbox"));
        WebdriverWrapper.click(driver, getORPropValue("button.enable"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        
    }
    
     
    /**
     * disables all transaction templates in Discovered Transactions UI
     * @throws Exception
     */
    public void disableTemplate() throws Exception {
        gotoAutogenUI();
        WebdriverWrapper.click(driver, getORPropValue("administration.discoveredtxn.allbtcheckbox"));
        WebdriverWrapper.clickDisable(driver, getORPropValue("button.disable"));
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(WebdriverWrapper.getPopUpText(driver).contains("Select OK to disable the transaction template(s)."));
        WebdriverWrapper.selectPopUp(driver, "accept");
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    }
    
    /**
     * enable transaction discovery
     * @param monitorName
     * @throws Exception
     */
    public void enableAutogen(String monitorName) throws Exception{ 
        WebdriverWrapper.click(driver, getORPropValue("home.setup"));
        String monitorValue = setup.getPIdForName("monitor", monitorName);
        WebdriverWrapper.click(driver, getORPropValue("setup.services"));
        WebdriverWrapper.click(driver, getORPropValue("setup.services.TIMCollectionServiceLink"));
        WebdriverWrapper.selectList(driver, getORPropValue("setup.services.childServicesDef"), getORPropValue("setup.services.trasactiondiscoveryservicelabel"));
        assertEquals(WebdriverWrapper.getElementText(driver, getORPropValue("setup.services.enabledmonitors")),monitorName);
        if(!WebdriverWrapper.isElementSelected(driver, monitorValue))
        {
            WebdriverWrapper.click(driver, monitorValue);
            WebdriverWrapper.click(driver, getORPropValue("setup.services.saveChildServices"));
            //assertTrue(WebdriverWrapper.getPopUpText(driver).matches("^Are you sure you want to make changes to the service[\\s\\S] Proceed with the update[\\s\\S]$"));
            WebdriverWrapper.selectPopUp(driver, "accept");
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }
    }

    /**
     * disable transaction discovery
     * @param monitorName
     * @throws Exception
     */
    public void disableAutogen(String monitorName) throws Exception{
        WebdriverWrapper.click(driver, getORPropValue("home.setup"));
        WebdriverWrapper.click(driver, getORPropValue("setup.monitors"));
        String monitorValue = setup.getPIdForName("monitor", monitorName);
        WebdriverWrapper.click(driver, getORPropValue("setup.services"));
        WebdriverWrapper.click(driver,getORPropValue("setup.services.TIMCollectionServiceLink"));
        WebdriverWrapper.selectList(driver, getORPropValue("setup.services.childServicesDef"), getORPropValue("setup.services.trasactiondiscoveryservicelabel"));
        assertEquals(WebdriverWrapper.getElementText(driver, getORPropValue("setup.services.enabledmonitors")),monitorName);
        if(WebdriverWrapper.isElementSelected(driver, monitorValue))
        {
            WebdriverWrapper.click(driver, monitorValue);  
            WebdriverWrapper.click(driver, getORPropValue("setup.services.saveChildServices"));
            //assertTrue(WebdriverWrapper.getPopUpText(driver).matches("^Are you sure you want to make changes to the service[\\s\\S] Proceed with the update[\\s\\S]$"));
            WebdriverWrapper.selectPopUp(driver, "accept");
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        }
    }

    /**
     * to check if autogen is enabled or not
     * @param ipAddress
     * @return
     * @throws Exception
     */
    public boolean checkAutogenConfigUI(String ipAddress) throws Exception{
  
        WebdriverWrapper.navigateToUrl(driver, "http://"+ipAddress+getORPropValue("TIMDiagnosticsPageURL"));
        WebdriverWrapper.click(driver, getORPropValue("timautogenconfiglink"));
        if(WebdriverWrapper.verifyTextPresent(driver, "enabled=\"1\"")){
            return true;
        } else {
           return false;
        }
    }

    /**
     * parse autogenconfig.xml file.
     * @param attrName
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String parseAutogenConfigForEnabled(String attrName) throws ParserConfigurationException, SAXException, IOException{
        String autogenEnabled = null; 
        QaFileUtils config = new QaFileUtils();
        File file = new File(config.getTestDataFullPath("Autogen", "autogenconfig.xml"));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        // LOGGER.info("Root element " + doc.getDocumentElement().getNodeName());
        NodeList nodeLst = doc.getElementsByTagName("AutogenConfig");
        for (int s = 0; s < nodeLst.getLength(); s++) {
            Node fstNode = nodeLst.item(s);
            if (fstNode.hasAttributes()) {
                NamedNodeMap attributes = (NamedNodeMap)fstNode.getAttributes();
                if(attributes!=null){
                    Attr attribute1 = (Attr)attributes.getNamedItem(attrName);
                    //  Attr attribute = (Attr)attributes.item(0);
                    autogenEnabled = attribute1.getValue();
                    LOGGER.info(autogenEnabled);   
                }
            }
        }
        doc = null;
        db = null;
        file= null;
        return autogenEnabled;     
    }

    /**
     * parse autogenconfig.xml file for template
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String parseAutogenConfigForTemplate() throws ParserConfigurationException, SAXException, IOException{
        
        String template = null;
        QaFileUtils config = new QaFileUtils();
        File file = new File(config.getTestDataFullPath("Autogen", "autogenconfig.xml"));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        // LOGGER.info("Root element " + doc.getDocumentElement().getNodeName());
        NodeList nodeLst = doc.getElementsByTagName("Template");
        for (int s = 0; s < nodeLst.getLength(); s++) {
           Node fstNode = nodeLst.item(s);
                 if (fstNode.hasAttributes()) {
                    NamedNodeMap attributes = (NamedNodeMap)fstNode.getAttributes();
                     if(attributes!=null){
                    Attr attribute = (Attr)attributes.item(3);
                    template = attribute.getValue();
                    
                    }
                 }
          }
          doc = null;
          db = null;
          file= null;
          return template;                   
    }

    /**
     * parse autogenconfig.xml file for parameter
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String parseAutogenConfigForParameter() throws ParserConfigurationException, SAXException, IOException{
    
        String parameter = null;
        QaFileUtils config = new QaFileUtils();
        File file = new File(config.getTestDataFullPath("Autogen", "autogenconfig.xml"));
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbf.newDocumentBuilder();
          Document doc = db.parse(file);
          doc.getDocumentElement().normalize();
         // LOGGER.info("Root element " + doc.getDocumentElement().getNodeName());
          NodeList nodeLst = doc.getElementsByTagName("ParameterDef");
          for (int s = 0; s < nodeLst.getLength(); s++) {
                 Node fstNode = nodeLst.item(s);
                 if (fstNode.hasAttributes()) {
                 NamedNodeMap attributes = (NamedNodeMap)fstNode.getAttributes();
                 if(attributes!=null){
                 Attr attribute = (Attr)attributes.item(0);
                 parameter = attribute.getValue();      
                 }
             }
          }
          doc = null;
          db = null;
          file= null;
          return parameter;             
    }

    /**
     * parse autogenconfig.xml file for AppDefId
     * @param autogenTemplateName
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String parseAutogenConfigForAppDefId(String autogenTemplateName) throws ParserConfigurationException, SAXException, IOException{
    
        String autogenAppDefID = null;
        QaFileUtils config = new QaFileUtils();
        File file = new File(config.getTestDataFullPath("Autogen", "autogenconfig.xml"));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        // LOGGER.info("Root element " + doc.getDocumentElement().getNodeName());
        NodeList nodeLst = doc.getElementsByTagName("Template");
        LOGGER.info(""+nodeLst.getLength());
        for (int s = 0; s < nodeLst.getLength(); s++) {
            Node fstNode = nodeLst.item(s);
             if (fstNode.hasAttributes()) {
                NamedNodeMap attributes = (NamedNodeMap)fstNode.getAttributes();
                if(attributes!=null){
                    Attr attribute = (Attr)attributes.item(3);
                    String template = attribute.getValue();
                    LOGGER.info(template);
                    if (template.equals(autogenTemplateName)){
                        Attr attribute1 = (Attr)attributes.item(0);
                        autogenAppDefID = attribute1.getValue();
                        LOGGER.info(autogenAppDefID);
                        }                 
                }
             }
             
        }
        doc = null;
        db = null;
        file= null;
        return autogenAppDefID;                
    }

    /**
     * parse domainconfig.xml file for AppDefIdS
     * @param appDefName
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public String parseDomianConfigForAppDefId(String appDefName) throws ParserConfigurationException, SAXException, IOException{
    
      String domainAppDefID = null;
      QaFileUtils config = new QaFileUtils();
      File file = new File(config.getTestDataFullPath("Autogen", "domainconfig.xml"));
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(file);
      doc.getDocumentElement().normalize();
     // LOGGER.info("Root element " + doc.getDocumentElement().getNodeName());
      NodeList nodeLst = doc.getElementsByTagName("AppDef");
      LOGGER.info(""+nodeLst.getLength());
      for (int s = 0; s < nodeLst.getLength(); s++) {

          Node fstNode = nodeLst.item(s);
             if (fstNode.hasAttributes()) {
                NamedNodeMap attributes = (NamedNodeMap)fstNode.getAttributes();
                if(attributes!=null){
                    Attr attribute = (Attr)attributes.getNamedItem("name");
                    //Attr attribute = (Attr)attributes.item(11);
                    String template = attribute.getValue();
                    if (template.equals(appDefName)){
                        Attr attribute1 = (Attr)attributes.item(10);
                        domainAppDefID = attribute1.getValue();
                        LOGGER.info(domainAppDefID);
                        }
                }
             }
      }
      doc = null;
      db = null;
      file= null;
      return domainAppDefID;
                
    }

    /**
     * Delete all business transactions from specified Business service
     * @param businessService
     * @throws Exception
     */
    public void deleteAllBTsFromBS(String businessService) throws Exception{
        WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),getORPropValue("administration.businessservices"));
        WebdriverWrapper.click(driver, getORPropValue("linkText")+businessService);
        WebdriverWrapper.click(driver, getORPropValue("name")+getORPropValue("button.deleteAll"));
        //assertTrue(WebdriverWrapper.getPopUpText(driver).matches("^Are you sure you want to delete all Business Transactions from the Business Service[\\s\\S]$"));
        WebdriverWrapper.selectPopUp(driver, "accept");
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

    }


}

