package com.ca.apm.tests.test;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;





import com.ca.apm.tests.testbed.Weblogic12TestBed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
public class WebViewOperations extends WebViewLoginLogout
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
    public void clickonAgentNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
       WEBVIEW_INVESTIGATOR_AGENTNODE= WEBVIEW_INVESTIGATOR_AGENTNODE.replace("kansr04-I165417", weblogicAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_AGENTNODE);
        we.click();
    }
    public void clickonTomcatAgentNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        WEBVIEW_TOMCATINVESTIGATOR_AGENTNODE= WEBVIEW_TOMCATINVESTIGATOR_AGENTNODE.replace("tas-itc-n33", tomcatAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_TOMCATINVESTIGATOR_AGENTNODE);
        we.click();
    }
    public void clickonAgentNameNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
       WEBVIEW_AGENTNODE_AGENTNAME =WEBVIEW_AGENTNODE_AGENTNAME.replace("kansr04-I165417", weblogicAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_AGENTNODE_AGENTNAME);
        we.click();
    }
    public void clickonAgentTomcatAgentNameNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        WEBVIEW_TOMCATAGENTNODE_AGENTNAME =WEBVIEW_TOMCATAGENTNODE_AGENTNAME.replace("tas-itc-n33", tomcatAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_TOMCATAGENTNODE_AGENTNAME);
        we.click();
    }
    
    public void clickonAgentDomainNameNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
        WEBVIEW_AGENTNAME_AGENTDOMAIN= WEBVIEW_AGENTNAME_AGENTDOMAIN.replace("kansr04-I165417", weblogicAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_AGENTNAME_AGENTDOMAIN);
        we.click();
    }
    
    public void clickonTomcatAgentDomainNameNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        WEBVIEW_TOMCATAGENTNAME_AGENTDOMAIN= WEBVIEW_TOMCATAGENTNAME_AGENTDOMAIN.replace("tas-itc-n33", tomcatAgentHostName);
        we =waitExplicitPresenceOfElement(WEBVIEW_TOMCATAGENTNAME_AGENTDOMAIN);
        we.click();
    }
    
    
    public boolean clickonBSNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_BS_NODE= REGISTERPATIENT_BS_NODE.replace("kansr04-I165417", weblogicAgentHostName);
        try
        {
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE);
        we.click();
        return true;
        }
        catch (Exception e) {
            System.out.println("Element Not Found");
            return false;
        }
        
        
    }
    
    
    public void clickonTomcatBSNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_TOMCATBS_NODE= REGISTERPATIENT_TOMCATBS_NODE.replace("tas-itc-n33", tomcatAgentHostName);
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_TOMCATBS_NODE);
        we.click();
    }
    
    
    public boolean checkBSDefaultNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_BS_NODE_DEFAULTBS= REGISTERPATIENT_BS_NODE_DEFAULTBS.replace("kansr04-I165417", weblogicAgentHostName);
        try
        {
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE_DEFAULTBS);
        return true;
        }
        catch (Exception e) {
            System.out.println("Element Not Found");
            return false;
        }
        
        
    }
    
    public boolean checkTomcatBSDefaultNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_TOMCATBS_NODE_DEFAULTBS= REGISTERPATIENT_TOMCATBS_NODE_DEFAULTBS.replace("tas-itc-n33", tomcatAgentHostName);
        
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_TOMCATBS_NODE_DEFAULTBS);
       
        return we.isDisplayed();
    }
    
    public WebElement checkBSIdentifiedNode(String weblogicAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_BS_NODE_BTNODE= REGISTERPATIENT_BS_NODE_BTNODE.replace("kansr04-I165417", weblogicAgentHostName);
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_BS_NODE_BTNODE);
        return we;
    }
    
    public WebElement checkTomcatBSIdentifiedNode(String tomcatAgentHostName)
    {
        initializeEMandAgents();
        REGISTERPATIENT_TOMCATBS_NODE_BTNODE= REGISTERPATIENT_TOMCATBS_NODE_BTNODE.replace("tas-itc-n33", tomcatAgentHostName);
        we =waitExplicitPresenceOfElement(REGISTERPATIENT_TOMCATBS_NODE_BTNODE);
        return we;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   
    //*****************************************************************************************//
    
    
   
    public void hitTransactionforWeblogic(String WeblogicAgentHostName,String Path1, String Path2)
    {
        
        initializeEMandAgents();
        //Hit Transaction for couple of times....
        for(int i=0;i<=5;i++)
          {
          //hitTransaction("http://"+WeblogicAgentHostName+":7001/examplesWebApp/index.jsp");
          hitTransaction("http://"+WeblogicAgentHostName+":7001"+Path1+""+Path2+"");
          }
      
     
         
    }
    public void hitTransactionforCorId(String WeblogicAgentHostName,String Path1, String Path2)
    {
        
        initializeEMandAgents();
        //Hit Transaction for couple of times....
        for(int i=0;i<=5;i++)
          {
          //hitTransaction("http://"+WeblogicAgentHostName+":7001/examplesWebApp/index.jsp");
          hitTransactionCorId("http://"+WeblogicAgentHostName+":7001"+Path1+""+Path2+"");
          
          }
      
     
         
    }
    
    
    
    public void hitTransactionofTomcat(String tomcatAgentHostName,String Path1, String Path2)
    {
        
        initializeEMandAgents();
        //Hit Transaction for couple of times....
        for(int i=0;i<=5;i++)
          {
          //hitTransaction("http://"+weblogicAgenthost+":7001/examplesWebApp/index.jsp");
          //hitTransaction("http://"+tomcatAgenthost+":9091/examples/servlets/servlet/HelloWorldExample");
            hitTransaction("http://"+tomcatAgentHostName+":9091"+Path1+""+Path2+"");
          }
      
     
         
    }
   
    
    
    
    
  
    
 
    

  
    
   

    public  void startEmandWebView() {
           initializeEMandAgents();
           runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_EM);
           runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_WEBVIEW);
       } 
    public  void startAllAgents() {
        initializeEMandAgents();
   
    runSerializedCommandFlowFromRole(tomcatRoleId, TomcatRole.ENV_TOMCAT_START);
    //runSerializedCommandFlowFromRole(weblogicRoleId1, WebLogicRole.EP_WEBLOGIC_START);
    //runSerializedCommandFlowFromRole(weblogicRoleId2, WebLogicRole.EP_WEBLOGIC_START);
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
  
 
  
  
  runSerializedCommandFlowFromRole(Weblogic12TestBed.TOMCAT_ROLE_ID,
      TomcatRole.ENV_TOMCAT_STOP);

  runSerializedCommandFlowFromRole(weblogicRoleId1, WebLogicRole.EP_WEBLOGIC_STOP);
  runSerializedCommandFlowFromRole(weblogicRoleId2, WebLogicRole.EP_WEBLOGIC_STOP);
}

    
 }




