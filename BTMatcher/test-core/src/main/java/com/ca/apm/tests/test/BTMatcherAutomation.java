package com.ca.apm.tests.test;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
public class BTMatcherAutomation extends WebViewOperations
{
    @BeforeTest
    public void runAll()
    {
        
        startEmandWebView(); 
        startAllAgents();
        //stopAllAgents();
        //stopNodeApp();
        //startNodeApp();
        //startEmandWebView();
        //startAllAgents();
        
    }
    @Test(priority=0)
   public void testBTmatcherproperty_TestCaseID_454966()
  
   {
       initializeEMandAgents();
       File file = new File(emConfigFile);
       String string=null;
    try
    {
        string = FileUtils.readFileToString(file);
    } catch (IOException e)
    {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
       if(string.contains("enable.default.BusinessTransaction=false"))
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454966 is Pass");
       }
       else
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454966 is Fail");
       }
   }
   
    
     @Test(priority=1)
    public void  testvalidiationforNONBTdefintionsonCEMUI_TestCaseID_454967()
   
    {
         
        initializeEMandAgents();
        stopEmandWebView();
        replaceProp("enable.default.BusinessTransaction=false", "enable.default.BusinessTransaction=true",emMachineId, emConfigFile);
        startEmandWebView();
        logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
        addBS("Default BS", "BSName", 1);
        WebElement we = waitExplicitPresenceOfElement(CEM_ADMINISTRATION_BSERVICE_ERROR);
        if(we.getText().equals("Please enter a different name value. You have entered a default definition name."))
        {
            System.out.println("Business Service Validation is working fine...User Can't create a BS with Default BT name");
            System.out.println("testvalidiationforNONBTdefintionsonCEMUI_TestCaseID_454967 is Pass");
        }
        else
        {
            System.out.println("testvalidiationforNONBTdefintionsonCEMUI_TestCaseID_454967 is Fail");
        }
        closeBrowser();
    }
    
    @Test(priority=2)
    public void testwithnonBTmatcherdefinition_TestCaseID_454963()
   
    {
        initializeEMandAgents();
        //In this case we are adding one transaction and hitting other transaction, then everyhting should go under Default node
        //we are running this on Tomcat
        addalldataonCemUi("TomcatBS","/examples/jsp/jsp2/","simpletag/hello.jsp");
        
        //Now hit other than added transaction for couple of times
        hitTransactionofTomcat(tomcatAgenthost,"/examples/servlets/","servlet/RequestInfoExample");
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonInvestigator();
        clickonSuperDomain();
        clickonTomcatAgentNode(tomcatAgenthost);
        clickonAgentTomcatAgentNameNode(tomcatAgenthost);
        clickonTomcatAgentDomainNameNode(tomcatAgenthost);
        clickonTomcatBSNode(tomcatAgenthost);
        boolean condition= checkTomcatBSDefaultNode(tomcatAgenthost);
        /*initializeEMandAgents();
        REGISTERPATIENT_BS_NODE=REGISTERPATIENT_BS_NODE.replace("kansr04-I165417", tomcatAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE);
        we.click();
        REGISTERPATIENT_BS_NODE_BTNODE=REGISTERPATIENT_BS_NODE_BTNODE.replace("kansr04-I165417", tomcatAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE_BTNODE);*/
        if(condition==true)
        {
            System.out.println("Default BT matching node is generated successfully");
            System.out.println("testwithnonBTmatcherdefinition_TestCaseID_454963 is Pass");
        }
        else
        {
            System.out.println("testwithnonBTmatcherdefinition_TestCaseID_454963 is Fail");
        }
        closeBrowser();
    }
    
    
    @Test(priority=3)
    public void testwithBTmatcherdefinition_TestCaseID_454962()
   
    {
        initializeEMandAgents();
        //Now hit the transaction for couple of times
        hitTransactionofTomcat(tomcatAgenthost,"/examples/jsp/jsp2","/simpletag/hello.jsp");
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonInvestigator();
        clickonSuperDomain();
        clickonTomcatAgentNode(tomcatAgenthost);
        clickonAgentTomcatAgentNameNode(tomcatAgenthost);
        clickonTomcatAgentDomainNameNode(tomcatAgenthost);
        clickonTomcatBSNode(tomcatAgenthost);
       WebElement we=  checkTomcatBSIdentifiedNode(tomcatAgenthost);
        /*initializeEMandAgents();
        REGISTERPATIENT_BS_NODE=REGISTERPATIENT_BS_NODE.replace("kansr04-I165417", tomcatAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE);
        we.click();
        REGISTERPATIENT_BS_NODE_BTNODE=REGISTERPATIENT_BS_NODE_BTNODE.replace("kansr04-I165417", tomcatAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE_BTNODE);*/
        if(we.isDisplayed()&&we.getText().equals("TomcatBS"))
        {
            System.out.println("BT matching node is generated successfully");
            System.out.println("testwithBTmatcherdefinition_TestCaseID_454962 is Pass");
        }
        else
        {
            System.out.println("testwithBTmatcherdefinition_TestCaseID_454962 is Fail");
        }
       
        
        closeBrowser();
    }
 
   @Test(priority=4)
   public void testwithBTmatcherwithcoriddefinition_TestCaseID_454964()
  
   {
      
       initializeEMandAgents();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       deleteAllCemDatafromCemUi();
       syncronizeMonitors();
       addalldataonCemUi("MedrecBS","/QATestApp/urlhitter","/ServletCallingJSP?duration=1000");
       hitTransactionforCorId(weblogicAgenthost2,"/QATestApp/urlhitter","/hitter.jsp");
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonInvestigator();
       clickonSuperDomain();
       clickonAgentNode(weblogicAgenthost1);
       clickonAgentNameNode(weblogicAgenthost1);
       clickonAgentDomainNameNode(weblogicAgenthost1);
       boolean condition=clickonBSNode(weblogicAgenthost1);
      
       if(condition==false)
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454964 is Pass");
       }
       else
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454964 is Fail");
       }
       closeBrowser();
   }
   
   @Test(priority=5)
   public void testwithnonBTmatcherwithcoriddefinition_TestCaseID_454965()
  
   {
       initializeEMandAgents();
       
       //addalldataonCemUi("MedrecBS","/QATestApp/urlhitter","/ServletCallingJSP?duration=1000");
     
       hitTransactionforWeblogic(weblogicAgenthost1,"/QATestApp/urlhitter","/ServletCallingJSP?duration=1000");
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonInvestigator();
       clickonSuperDomain();
       clickonAgentNode(weblogicAgenthost1);
       clickonAgentNameNode(weblogicAgenthost1);
       clickonAgentDomainNameNode(weblogicAgenthost1);
       clickonBSNode(weblogicAgenthost1);
       boolean condition= checkBSDefaultNode(weblogicAgenthost1);
       if(condition==true)
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454965 is Pass");
       }
       else
       {
           System.out.println("testwithBTmatcherdefinition_TestCaseID_454965 is Fail");
       }
       closeBrowser();
   }
   
   
 
   
}


