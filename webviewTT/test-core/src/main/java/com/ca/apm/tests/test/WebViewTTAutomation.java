package com.ca.apm.tests.test;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class WebViewTTAutomation extends WebViewAlerts
{

    @BeforeTest
    public void runAll()
    {
        
       startEmandWebView();
        startAllAgents();
    }
    
   @Test(priority=0)
    public void checkAlltheUIoptionsofcreatedTransactionTraceAction_TestCaseID_454144()
   
    {
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonMangementModule();
        createTTAction();
        reverseMoveToAction();
        movetoAction();
        WebElement we1 =waitExplicitPresenceOfElement(NEWTTACTION_TRACEALL);
        WebElement we2 =waitExplicitPresenceOfElement(NEWTTACTION_THRESHOLDINPUT);
        int theresholdValue = Integer.parseInt(we2.getAttribute("value"));
        WebElement  we3 = waitExplicitPresenceOfElement(NEWTTACTION_TTIMEINPUT);
        we3.click();
        int timeValue =  Integer.parseInt(we3.getAttribute("value"));
        WebElement  we4 =waitExplicitPresenceOfElement(NEWTTACTION_BSCHECK);
       Boolean  BsValue = we4.isSelected();
        if(we1.isSelected() && theresholdValue==100 &&timeValue==1 && BsValue==false )
        {
            System.out.println("checkAlltheUIoptionsofcreatedTransactionTraceAction_TestCaseID_454144 is PASS");
        }
        else
        {
            System.out.println("checkAlltheUIoptionsofcreatedTransactionTraceAction_TestCaseID_454144 is failed");
        }
        traceDelete();
        closeBrowser();
       
    }
    
   @Test(priority=1)
    public void createNewTransactionTraceActionfromWebView_TestCaseID_454143()
    {
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonMangementModule();
        createTTAction();
        reverseMoveToAction();
        movetoAction();
        //Just verify the name of the Action if it matches with created one test case is pass else it is failed
      we=waitExplicitPresenceOfElement(NEWTTACTION_POSTCREATION_NAME);
    
        if( we.getAttribute("value").equals("TTAction"))
        {
            System.out.println("createNewTransactionTraceActionfromWebView_TestCaseID_454143 is PASS");
            
        }
        else
        { 
            System.out.println("createNewTransactionTraceActionfromWebView_TestCaseID_454143 is Failed");
            
        }
        traceDelete();
        closeBrowser();
    }
   @Test(priority=2)
   public void enableTransactionTraceActionandcheckUIvalidations_TestCaseID_454145()
   {
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       createTTAction();
       reverseMoveToAction();
       movetoAction();
       we =waitExplicitPresenceOfElement(NEWTTACTION_THRESHOLDINPUT);
       we.clear();
       we.sendKeys("10000");
       traceApply();
       we= waitExplicitPresenceOfElement(NEWACTION_THRESHOLD_ALERT);
       String thresholdText= we.getText();
       
       we =waitExplicitPresenceOfElement(NEWACTION_THRESHOLD_OK);
       we.click();
       we = waitExplicitPresenceOfElement(NEWTTACTION_TTIMEINPUT);
       we.clear();
       we.sendKeys("10000");
       traceApply();
       we= waitExplicitPresenceOfElement(NEWACTION_THRESHOLD_ALERT);
       String timeText= we.getText();
       we =waitExplicitPresenceOfElement(NEWACTION_THRESHOLD_OK);
       we.click();
       traceRevert();
       we =waitExplicitPresenceOfElement(NEWTTACTION_THRESHOLDINPUT);
       int theresholdValue = Integer.parseInt(we.getAttribute("value"));
       WebElement  we = waitExplicitPresenceOfElement(NEWTTACTION_TTIMEINPUT);
       int timeValue =  Integer.parseInt(we.getAttribute("value"));
       System.out.println(thresholdText+" "+timeText+" "+theresholdValue+" "+timeValue);
       if(thresholdText.equals("Please enter an Percentage value between 1 and 999.") && timeText.equals("Please enter an integer session duration between 1 and 720 minutes.")  && theresholdValue==100 && timeValue==1 )
       {
           System.out.println("enableTransactionTraceActionandcheckUIvalidations_TestCaseID_454145 is PASS");
       }
       else
       {
           System.out.println("enableTransactionTraceActionandcheckUIvalidations_TestCaseID_454145 is Failed");
       }
       traceDelete();
       closeBrowser();
       
   }
   @Test(priority=3)
   public void webViewTTAlertCombinationAllTraceAllAgentsFalse_TestCaseID_454151()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Hit Transactions to get the required nodes....
       hitTransaction();
       //Create TT Action...
       createEntireTTAction(); 
       traceOnlyAgent();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
        createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       //Now hit the transaction for couple of times
       hitTransaction();
       //If sessions are there then mark test case as pass
       closeBrowser();
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalse_TestCaseID_454151 is PASS");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalse_TestCaseID_454151 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   }
   @Test(priority=4)
   public void webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454153()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceApply();
       reverseMoveToAction();
        //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertTriggerSelection();
        alertResolutionSelection();
        alertNotifyIndividualMetric();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       //Now hit the transaction for couple of times
       hitTransaction();
       //If sessions are there then mark test case as pass
       closeBrowser();
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454153 is PASS");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454153 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   } 
   @Test(priority=5)
   public void webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454159()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView(); 
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       
       //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertTriggerSelection();
        alertResolutionSelection();
        alertNotifyIndividualMetric();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression3("(.*)\\|Tomcat\\|(.*)","Business Segment\\|BS2\\|BT2\\|T2:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression4("(.*)\\|WebLogic\\|(.*)","Business Segment\\|BS1\\|BT1\\|T1:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Add logic for CEM Businees Transaction Defintions....
       addalldataonCemUi();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
      //If sessions are there then mark test case as pass
       int numberOfBsTraces=verifyTraceForBS_Register();
       System.out.println(numberOfBsTraces);
       if(numberOfBsTraces>1)
       {
           
       System.out.println("BS Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454159 is Pass");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454159 is Failed");
       }
    //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       disableAllBS();
       syncronizeMonitors();
       //deleteAllCemDatafromCemUi();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();        
   }
  @Test(priority=6)
   public void webViewTTAlertCombinationAllTraceAllAgentsTrue_TestCaseID_454150()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
     //Start EM and WebView...
      // startEmandWebView();
        //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
       //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrue_TestCaseID_454150 is PASS");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrue_TestCaseID_454150 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   }
   @Test(priority=7)
   public void webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrue_TestCaseID_454152()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
      //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
        //Create Alert....
       createEntireAlert();
      //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertNotifyIndividualMetric();
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
      //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrue_TestCaseID_454152 is PASS");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
      //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   }
   @Test(priority=8)
   public void webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454158()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertCombinationAll();
        alertTriggerSelection();
        alertResolutionSelection();
        alertNotifyIndividualMetric();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression3("(.*)\\|Tomcat\\|(.*)","Business Segment\\|BS2\\|BT2\\|T2:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression4("(.*)\\|WebLogic\\|(.*)","Business Segment\\|BS1\\|BT1\\|T1:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Add logic for CEM Businees Transaction Defintions....
       //addalldataonCemUi();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       enableAllBS();
       syncronizeMonitors();
       closeBrowser();
      //Now hit the transaction for couple of times
       hitTransaction();
      //If sessions are there then mark test case as pass
       int numberOfBsTraces=verifyTraceForBS_Register();
       System.out.println(numberOfBsTraces);
       if(numberOfBsTraces>1)
       {
           
       System.out.println("BS Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454158 is Pass");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454158 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       disableAllBS();
       syncronizeMonitors();
       //deleteAllCemDatafromCemUi();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   }
   @Test(priority=9)
   public void webViewTTAlertCombinationAnyTraceAllAgentsFalse_TestCaseID_454147()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceApply();
       reverseMoveToAction();
      //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert...
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsFalse_TestCaseID_454147 is PASS");
       }
      else
          {
              System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsFalse_TestCaseID_454147 is Failed");
          }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();    
   }
   @Test(priority=10)
   public void webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454149()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertNotifyIndividualMetric();
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
        //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454149 is PASS");
       }
      else
      {
          System.out.println("webViewTTAlertCombinationAllTraceAllAgentsFalseNotifyIndividualMetricsTrue_TestCaseID_454149 is Failed");
      }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();     
   }
   @Test(priority=11)
   public void webViewTTAlertCombinationAnyTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454157()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
        //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
      //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertNotifyIndividualMetric();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression3("(.*)\\|Tomcat\\|(.*)","Business Segment\\|BS2\\|BT2\\|T2:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression4("(.*)\\|WebLogic\\|(.*)","Business Segment\\|BS1\\|BT1\\|T1:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Add logic for CEM Businees Transaction Defintions....
       //addalldataonCemUi();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       enableAllBS();
       syncronizeMonitors();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
      //If sessions are there then mark test case as pass
       int numberOfBsTraces=verifyTraceForBS_Register();
       System.out.println(numberOfBsTraces);
       if(numberOfBsTraces>1)
       {
           
       System.out.println("BS Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454157 is Pass");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsFalseNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454157 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       disableAllBS();
       syncronizeMonitors();
      // deleteAllCemDatafromCemUi();
       //Stop EM and clear logs..
       // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();
  }
   @Test(priority=12)
   public void webviewTTAlertCombinationAnyTraceAllAgentsFalseTracebusinessTransactionTrue_TestCaseID_454155()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceOnlyAgent();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression3("(.*)\\|Tomcat\\|(.*)","Business Segment\\|BS2\\|BT2\\|T2:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression4("(.*)\\|WebLogic\\|(.*)","Business Segment\\|BS1\\|BT1\\|T1:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Add logic for CEM Businees Transaction Defintions....
       //addalldataonCemUi();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       enableAllBS();
       syncronizeMonitors();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfBsTraces=verifyTraceForBS_Register();
       System.out.println(numberOfBsTraces);
       if(numberOfBsTraces>1)
       {
           
       System.out.println("BS Traces are generated succesfully");
       System.out.println("webviewTTAlertCombinationAnyTraceAllAgentsFalseTracebusinessTransactionTrue_TestCaseID_454155 is Pass");
       }
       else
       {
           System.out.println("webviewTTAlertCombinationAnyTraceAllAgentsFalseTracebusinessTransactionTrue_TestCaseID_454155 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       disableAllBS();
       syncronizeMonitors();
       //deleteAllCemDatafromCemUi();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();   
   }
   @Test(priority=13)
   public void webViewTTAlertCombinationAnyTraceAllAgentsTrue_TestCaseID_454148()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
      //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert...
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
       //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
      //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsTrue_TestCaseID_454148 is PASS");
       }
      else
     {
       System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsTrue_TestCaseID_454148 is Failed");
      }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser();    
   }
   @Test(priority=14)
   public void webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrue_TestCaseID_454156()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
       //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
       //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertNotifyIndividualMetric();
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
    //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrue_TestCaseID_454156 is PASS");
       }
      else
      {
          System.out.println("webViewTTAlertCombinationAllTraceAllAgentsTrueNotifyIndividualMetricsTrue_TestCaseID_454156 is Failed");
      }
   
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       //Stop EM and clear logs..
       //Close the browser instance...
       closeBrowser();  
   }
   @Test(priority=15)
   public void webViewTTAlertCombinationAnyTraceAllAgentsTrueeNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454154()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //Start EM and WebView...
      // startEmandWebView();
       //Hit Transaction for couple of times....
       hitTransaction();
      //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
      //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertNotifyIndividualMetric();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression3("(.*)\\|Tomcat\\|(.*)","Business Segment\\|BS2\\|BT2\\|T2:Average Response Time \\(ms\\)");
       addandChangeMetricGroupExpression4("(.*)\\|WebLogic\\|(.*)","Business Segment\\|BS1\\|BT1\\|T1:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Add logic for CEM Businees Transaction Defintions....
       //addalldataonCemUi();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       enableAllBS();
       syncronizeMonitors();
       closeBrowser();
      //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfBsTraces=verifyTraceForBS_Register();
       System.out.println(numberOfBsTraces);
       if(numberOfBsTraces>1)
       {
           
       System.out.println("BS Traces are generated succesfully");
       System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsTrueeNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454154 is Pass");
       }
       else
       {
           System.out.println("webViewTTAlertCombinationAnyTraceAllAgentsTrueeNotifyIndividualMetricsTrueTraceBusinessTransactionsTrue_TestCaseID_454154 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
       logintoCemAdminConsole("http://"+emHost+":8081", "cemadmin", "quality");
       deleteAllCemDatafromCemUi();
        //Stop EM and clear logs..
      // stopEmandWebView();
       //Close the browser instance...
       closeBrowser(); 
       //fd.quit();
   }   
   @Test(priority=16)
   public void enableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454713()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //AddPropertis to the EM config file...need to implement
       List<String> addProperty = new ArrayList<String>();
       addProperty.add("skipTraceActionMetric.1=(.*)\\|__index:(.*)");
       addProperty.add("skipTraceActionMetric.2=(.*)\\|HelloWorldExample:(.*)");
       appendProp(addProperty, emMachineId, emConfigFile);
       //Stop EM and Webview
       stopEmandWebView();
       //Start EM and WebView...
       startEmandWebView();
       //startonlyEm();
       //Hit Transaction for couple of times....
       hitTransaction();
      //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
      //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       
       if(numberOfTraces==1)
       {
           
       System.out.println("BS Traces are not generated");
       System.out.println("enableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454713 is Pass");
       }
       else
       {
           System.out.println("enableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454713 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
   }   
   @Test(priority=17)
   public void disableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454714()
   { //GetEM and Agents hostnames
       initializeEMandAgents();
       //AddPropertis to the EM config file...need to implement..comment out the properties added
       replaceProp("skipTraceActionMetric.1=(.*)\\|__index:(.*)",
                   "#skipTraceActionMetric.1=(.*)\\|__index:(.*)",
                   emMachineId, emConfigFile);
       replaceProp("skipTraceActionMetric.2=(.*)\\|HelloWorldExample:(.*)",
                   "#skipTraceActionMetric.2=(.*)\\|HelloWorldExample:(.*)",
                   emMachineId, emConfigFile);
       //Stop EM and Webview
       stopEmandWebView();
       //Start EM and WebView...
       startEmandWebView();
       //startonlyEm();
       //Hit Transaction for couple of times....
       hitTransaction();
      //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceApply();
       reverseMoveToAction();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces>1)
       {
           
       System.out.println("BS Traces are generated");
       System.out.println("disableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454714 is Pass");
       }
       else
       {
           System.out.println("disableAgentRegexandcheckWebviewTTfunctionality_TestCaseID_454714 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
   }  
   
   @Test(priority=18)
   public void enableoneofthemetricgroupregexandcheckWebviewTTfunctionality_TestCaseID_454715()
   {
       //GetEM and Agents hostnames
       initializeEMandAgents();
       //AddPropertis to the EM config file...need to implement
       replaceProp("#skipTraceActionMetric.1=(.*)\\|__index:(.*)",
                   "skipTraceActionMetric.1=(.*)\\|__index:(.*)",
                   emMachineId, emConfigFile);
       //Stop EM and Webview
       stopEmandWebView();
       //Start EM and WebView...
       startEmandWebView();
       //startonlyEm();
       //Hit Transaction for couple of times....
       hitTransaction();
      //Create TT Action...
       //createEntireTTAction();
       logintoATC();
       loginToTeamCenter();
       moveToWebView();
       clickonMangementModule();
       movetoAction();
       traceAllAgents();
       traceBSActive();
       traceApply();
       reverseMoveToAction();
      //Log in and Logout from Webview
       //logoutandLoginFromWebView();
       //Create Alert....
       createEntireAlert();
       //Now Add Respective test case conditions in the alert....
        alertTriggerSelection();
        alertResolutionSelection();
        alertActivate();
        addActionDanger();
        addActionCaution();
        alertApply();
        reverseClickonCreatedAlert();
        //Change Metric group expressions...
       moveToMetricGroupExpression();
       addandChangeMetricGroupExpression2("(.*)\\|Tomcat\\|(.*)","Servlets\\|HelloWorldExample:Average Response Time \\(ms\\)");
       metricGroupApply();
       closeBrowser();
       //Now hit the transaction for couple of times
       hitTransaction();
     //If sessions are there then mark test case as pass
       int numberOfTraces= verifyTraceForRegisterPatient();
       System.out.println(numberOfTraces);
       if(numberOfTraces==1)
       {
           
       System.out.println("BS Traces are not generated");
       System.out.println("enableoneofthemetricgroupregexandcheckWebviewTTfunctionality_TestCaseID_454715 is Pass");
       }
       else
       {
           System.out.println("enableoneofthemetricgroupregexandcheckWebviewTTfunctionality_TestCaseID_454715 is Failed");
       }
       //Delete Created alert, mertric grouping , Action....
       closeBrowser();
       deleteAlertActionMetricGroupings();
       closeBrowser();
       fd.quit();
   }
}


