package com.ca.apm.tests.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.testbed.AgentsWindowsStandaloneTestbed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;

public class WebViewAlerts extends WebViewLoginLogout
{

    WebElement we = null;
    public void clickonInvestigator()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR);
        we.click();
    }
    public void clickonSuperDomain()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_SUPERDOMAIN);
        we.click();
    }
    public void clickonAgentNode()
    {
        initializeEMandAgents();
       WEBVIEW_INVESTIGATOR_AGENTNODE= WEBVIEW_INVESTIGATOR_AGENTNODE.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_AGENTNODE);
        we.click();
    }
    
    public void clickonAgentNameNode()
    {
        initializeEMandAgents();
       WEBVIEW_AGENTNODE_AGENTNAME =WEBVIEW_AGENTNODE_AGENTNAME.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_AGENTNODE_AGENTNAME);
        we.click();
    }
    public void clickonAgentDomainNameNode()
    {
        initializeEMandAgents();
        WEBVIEW_AGENTNAME_AGENTDOMAIN= WEBVIEW_AGENTNAME_AGENTDOMAIN.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_AGENTNAME_AGENTDOMAIN);
        we.click();
    }
    
    public void clickonAgentJSPNode()
    {
        initializeEMandAgents();
        WEBVIEW_AGENTDOMAIN_JSPNODE=WEBVIEW_AGENTDOMAIN_JSPNODE.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_AGENTDOMAIN_JSPNODE);
        we.click();
    }
    public void clickonRegisterPatient_JSPNode()
    {
        initializeEMandAgents();
        WEBVIEW_JSPNODE_REGISTERPATIENTNODE=WEBVIEW_JSPNODE_REGISTERPATIENTNODE.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_JSPNODE_REGISTERPATIENTNODE);
        we.click();
    }
    
    public void clickonAvgMetricOfRegisterPatient_JSPNode()
    {
        initializeEMandAgents();
        WEBVIEW_JSPNODE_REGISTERPATIENTNODE_AVG=WEBVIEW_JSPNODE_REGISTERPATIENTNODE_AVG.replace("kansr04-I165417", weblogicAgenthost);
        we =waitExplicitPresenceOfElement(WEBVIEW_JSPNODE_REGISTERPATIENTNODE_AVG);
        we.click();
    }
    
   
    //*****************************************************************************************//
    
    public void clickonAlert()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT);
        we.click();
    }
    public void defineAlert()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_NAME);
        we.clear();
        we.sendKeys("AlertonRegisterTT");
        we =waitExplicitPresenceOfElement(NEWTTACTION_NAME_MGMT);
        we.clear();
        we.sendKeys("Default");
        we.sendKeys(Keys.ENTER);
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_OK);
        we.click();
        try
      {
          Thread.sleep(10000);
      } catch (InterruptedException e)
      {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
            
    }
    
    public void clickonCreatedAlert()
    {
       
        //logoutandLoginFromWebView();
        //clickonMangementModule();
        we =waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_ALERTS);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_ALERTS_SELECT);
        we.click(); 
      
        
    }
    
    public void reverseClickonCreatedAlert()
    {
        we =waitExplicitPresenceOfElement(MANAGEMENT_ALERTS);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();

        
    }
    public void alertActivate()
    {
        
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_ACTIVE);
        we.click();
    }
    
    public void actionSelectandChooseAction()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ACTION_SELECTTT);
        we.click();
        we =waitExplicitPresenceOfElement(WEBVIEW_ACTION_CHOOSE);
       
        we.click();
       
    }
    public void addActionDanger()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ACTION_ADD_DANGER);
        we.click();
        actionSelectandChooseAction();
       
        
    }
    
    public void addActionCaution()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ACTION_ADD_CAUTION);
        we.click();
        actionSelectandChooseAction();
    }
    
    public void alertCombinationAll()
    {
        we=waitExplicitPresenceOfElement(WEBVIEW_ALERT_COMBINATION);
        we.clear();
        we.sendKeys("all");
        we.sendKeys(Keys.ENTER);
        
    }
    
    public void alertNotifyIndividualMetric()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_NOTIFYINDIVIDUAL);
        we.click();
    }
    
    public void alertTriggerSelection()
    {
        we=waitExplicitPresenceOfElement(WBVIEW_ALERT_TRIGGERNOTIFY);
        we.clear();
        we.sendKeys("Each Period While Problem Exists");
        we.sendKeys(Keys.ENTER);
        
  }
    
    public void alertResolutionSelection()
    {
        we=waitExplicitPresenceOfElement(WEBVIEW_ALERT_RESOLUTION);
        we.clear();
        //we.sendKeys("1 minute");
        we.sendKeys("30 seconds");
        we.sendKeys(Keys.ENTER);
        
        
    }
    
    
    public void alertApply()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_APPLY);
        we.click();
    }
    
    public void alertRevert()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_REVERT);
        we.click();
    }
    
    public void alertDelete()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_DELETE);
        we.click();
        we =waitExplicitPresenceOfElement(WEBVIEW_ALERT_DELETE_YES);
        we.click();
        try
      {
          Thread.sleep(5000);
      } catch (InterruptedException e)
      {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
        
    }
    public void createEntireAlert()
    {
        clickonInvestigator();
        clickonSuperDomain();
        clickonAgentNode();
        clickonAgentNameNode();
        clickonAgentDomainNameNode();
        clickonAgentJSPNode();
        clickonRegisterPatient_JSPNode();
        clickonAvgMetricOfRegisterPatient_JSPNode();
        rightClick();
        clickonAlert();
        defineAlert();
        reverseClickonCreatedAlert();
        clickonCreatedAlert();
        
    }
    
    
    public void clickonMangementModule()
    {
        we =waitExplicitPresenceOfElement(WEBVIEW_MANAGEMENTMODULE);
        we.click();
    }
    
    
    
    public void createTTAction()
    {
        //clickonMangementModule();
        we =waitExplicitPresenceOfElement(MANAGEMENTMODULE_ELEMENT);
        we.click();
        we =waitExplicitPresenceOfElement(ELEMENT_NEWACTION);
        mouseHover(we);
        we =waitExplicitPresenceOfElement(ELMENT_NEWTTACTION);
        we.click();
        we =waitExplicitPresenceOfElement(NEWTTACTION_NAME);
        we.clear();
        we.sendKeys("TTAction");
        we =waitExplicitPresenceOfElement(NEWTTACTION_NAME_MGMT);
        we.clear();
        we.sendKeys("Default");
        we.sendKeys(Keys.ENTER);
        we =waitExplicitPresenceOfElement(NEWTTACTION_OK);
        we.click();
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
     
    }
    
    public void movetoAction()
    {
        //logoutandLoginFromWebView();
        //clickonMangementModule();
        we =waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_ACTION);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_ACTION_SELECT);
        we.click();    
    }
    
    public void reverseMoveToAction()
    {
        we =waitExplicitPresenceOfElement(MANAGEMENT_ACTION);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
    }
    
    
    public void actionActive()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_ACTIVE);
        we.click();
        
    }
    
    public void traceAllAgents()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_TRACEALL);
        we.click();
    }
    
    public void traceOnlyAgent()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_TRACEONLY);
        we.click();
    }
    
    public void  traceBSActive()
    {
        
        we =waitExplicitPresenceOfElement(NEWTTACTION_BSCHECK);
        we.click();
    }
    
    public void traceThreshold()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_THRESHOLDINPUT);
        we.click();
    }
    
    public void traceTime()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_TTIMEINPUT);
        we.click();   
    }
    
    public void traceApply()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_APPLY);
        we.click();   
    }
    
    public void traceRevert()
    { 
        we =waitExplicitPresenceOfElement(NEWTTACTION_REVERT);
        we.click();   
        
    }
    
    public void traceDelete()
    {
        we =waitExplicitPresenceOfElement(NEWTTACTION_DELETE);
        we.click();  
        we = waitExplicitPresenceOfElement(NEWACTION_DELETE_YES);
        we.click();
        try
      {
          Thread.sleep(5000);
      } catch (InterruptedException e)
      {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
    }
    public void createEntireTTAction()
    {
        //Create TT Action...
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonMangementModule();
        createTTAction();
        traceTime();
        we.clear();
        we.sendKeys("2");;
        actionActive();
        
        
    }
    
   
    public void hitTransaction()
    {
        
        initializeEMandAgents();
        //Hit Transaction for couple of times....
        for(int i=0;i<=15;i++)
          {
          hitTransaction("http://"+weblogicAgenthost+":7001/examplesWebApp/index.jsp");
          hitTransaction("http://"+tomcatAgenthost+":9091/examples/servlets/servlet/HelloWorldExample");
          }
      
     
         
    }
    
    public void deleteAlertActionMetricGroupings()
    {
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonMangementModule();
        clickonCreatedAlert();
        alertDelete();
        reverseClickonCreatedAlert();
        //movetoAction();
        //traceDelete();
        //reverseMoveToAction();
        moveToMetricGroupExpression();
        metricGroupDelete();
    }
    
    public void moveToMetricGroupExpression()
    {
        //logoutandLoginFromWebView();
        //clickonMangementModule();
        we =waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we =waitExplicitPresenceOfElement(MANAGEMENT_METRICGROUPING);
        we.click();
        we =waitExplicitPresenceOfElement(MANGEMENT_REGISTERMETRICGROUP);
        we.click();    
    }
    
    public void changeMetricGroupExpression1()
    {
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_AGENTEXPR1);
        we.sendKeys("????");
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_METRICEXPR1);
        we.sendKeys("????");
    }
    
    public void addandChangeMetricGroupExpression2(String AgentName, String MetricExpression)
    {
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_ADD);
        we.click();
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_AGENTEXPR2);
        we.sendKeys(AgentName);
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_METRICEXPR2);
        we.sendKeys(MetricExpression);      
    }
    
    public void addandChangeMetricGroupExpression3(String AgentName, String MetricExpression)
    {
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_ADD);
        we.click();
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_AGENTEXPR3);
        we.sendKeys(AgentName);
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_METRICEXPR3);
        we.sendKeys(MetricExpression);      
    }
    
    public void addandChangeMetricGroupExpression4(String AgentName, String MetricExpression)
    {
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_ADD);
        we.click();
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_AGENTEXPR4);
        we.sendKeys(AgentName);
        we =waitExplicitPresenceOfElement(REGISTERMETRICGROUP_METRICEXPR4);
        we.sendKeys(MetricExpression);      
    }
    
    public void metricGroupApply()
    {
        we=waitExplicitPresenceOfElement(REGISTERMETRICGROUP_APPLY);
        we.click();
    }
    
    public void metricGroupDelete()
    {
        we=waitExplicitPresenceOfElement(REGISTERMETRICGROUP_DELETE);
        we.click();
        we=waitExplicitPresenceOfElement(REGISTERMETRICGROUP_DELETE_YES);
        we.click();
        
    }
    
    public int verifyTraceForRegisterPatient()
    {
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonInvestigator();
        clickonSuperDomain();
        clickonAgentNode();
        clickonAgentNameNode();
        clickonAgentDomainNameNode();
        clickonAgentJSPNode();
        initializeEMandAgents();
        REGISTERPATIENT_CLICK=REGISTERPATIENT_CLICK.replace("kansr04-I165417", weblogicAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_CLICK);
        we.click();
        we= waitExplicitPresenceOfElement(REGISTERPATIENT_TRACE);
        we.click();
        try
     {
         Thread.sleep(5000);
     } catch (InterruptedException e)
     {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
        //Get Row Count
       return  waitExplicitPreseneceOfElements(REGISTERPATIENT_TRACE_COLUMNCOUNT).size();
        
        
        
        
    }
    
    public void verifyTraceForHelloWorld()
    {
        we= waitExplicitPresenceOfElement(REGISTERPATIENT_TRACE);
        we.click();
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_TRACE);
    }
    
    public int verifyTraceForBS_Register()
    {
        logintoATC();
        loginToTeamCenter();
        moveToWebView();
        clickonInvestigator();
        clickonSuperDomain();
        clickonAgentNode();
        clickonAgentNameNode();
        clickonAgentDomainNameNode();
        initializeEMandAgents();
        REGISTERPATIENT_BS_NODE=REGISTERPATIENT_BS_NODE.replace("kansr04-I165417", weblogicAgenthost);
        we=waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE);
        we.click();
        we= waitExplicitPresenceOfElement(REGISTERPATIENT_BS_TRACE);
        we.click();
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Get Row Count
       return  waitExplicitPreseneceOfElements(REGISTERPATIENT_BS_TRACE_COLUMNCOUNT).size();
        
    }

  
    
   

    public  void startEmandWebView() {
           initializeEMandAgents();
           runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_EM);
           runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_WEBVIEW);
         
       } 
    public  void startAllAgents() {
        initializeEMandAgents();
    runSerializedCommandFlowFromRole(jbossRoleId, JbossRole.ENV_JBOSS_START);
    runSerializedCommandFlowFromRole(tomcatRoleId, TomcatRole.ENV_TOMCAT_START);
    runSerializedCommandFlowFromRole(weblogicRoleId, WebLogicRole.EP_WEBLOGIC_START);
    }
    
public void stopEmandWebView() {
  initializeEMandAgents();
  stopEM(emRoleId);
  File source = new File("C:\\windows\\System32\\taskkill.exe");
  File destination = new File("C:\\automation\\deployed\\em");
  try {
      FileUtils.copyFileToDirectory(source, destination);
   
      
     
  } catch (IOException e) {
      e.printStackTrace();
  }
  runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_STOP_WEBVIEW);
     
  File traces = new File("C:\\automation\\deployed\\em\\traces");
  File data   = new File("C:\\automation\\deployed\\em\\data");
  try
  {
  FileUtils.deleteDirectory(traces);
  FileUtils.deleteDirectory(data);
  }catch (IOException e) {
      e.printStackTrace();
  }
    

  }
public void stopAllAgents()
{
  initializeEMandAgents();
// Stop Tomcat
  runSerializedCommandFlowFromRole(AgentsWindowsStandaloneTestbed.TOMCAT_ROLE_ID,
      TomcatRole.ENV_TOMCAT_STOP);
  // Stop JBoss
  runSerializedCommandFlowFromRole(AgentsWindowsStandaloneTestbed.JBOSS_ROLE_ID,
      JbossRole.ENV_JBOSS_STOP);
  runSerializedCommandFlowFromRole(weblogicRoleId, WebLogicRole.EP_WEBLOGIC_STOP);
}

    
 }


