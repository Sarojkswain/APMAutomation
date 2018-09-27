package com.ca.apm.tests.test;
import java.util.Collections;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;

import com.ca.apm.tests.testbed.Weblogic12TestBed;
import com.ca.tas.role.TixChangeRole;
import com.ca.tas.test.TasTestNgTest;

public class ElementsIdentification extends TasTestNgTest
{

    

    protected String emMachineId;
    private String agentMachineId;

    protected String emRoleId;
    protected String tomcatRoleId;
    protected String tomcatRoleId1;
    protected String weblogicRoleId1,weblogicRoleId2;
    protected String jbossRoleId;
    public String emHost;
    protected String tomcatAgenthost;
   
    protected String weblogicAgenthost1,weblogicAgenthost2;
    private int port;
    protected String emConfigdir,emConfigFile;
   
    public void initializeEMandAgents()
    {

        
        emMachineId = Weblogic12TestBed.EM_MACHINE_ID;
        agentMachineId = Weblogic12TestBed.AGENT_MACHINE_ID;

        emRoleId = Weblogic12TestBed.EM_ROLE_ID;
        tomcatRoleId = Weblogic12TestBed.TOMCAT_ROLE_ID;
      
      
        
        weblogicRoleId1=Weblogic12TestBed.WLS_ROLE_ID;
        weblogicRoleId2=Weblogic12TestBed.WLS_ROLE2_ID;

        emHost =
            envProperties
                .getMachineHostnameByRoleId(Weblogic12TestBed.EM_ROLE_ID);
        tomcatAgenthost = envProperties.getMachineHostnameByRoleId(Weblogic12TestBed.TOMCAT_ROLE_ID);
        String[] a= tomcatAgenthost.split("\\.");
        tomcatAgenthost=a[0];
        weblogicAgenthost1= envProperties.getMachineHostnameByRoleId(Weblogic12TestBed.WLS_ROLE_ID);
        weblogicAgenthost2= envProperties.getMachineHostnameByRoleId(Weblogic12TestBed.WLS_ROLE2_ID);
        a=weblogicAgenthost1.split("\\.");
        weblogicAgenthost1=a[0];
        a=weblogicAgenthost2.split("\\.");
        weblogicAgenthost2=a[0];
        port =
            Integer.parseInt(envProperties.getRolePropertyById(emRoleId,
                DeployEMFlowContext.ENV_EM_PORT)); 
        emConfigdir = envProperties.getRolePropertyById(emRoleId,
                                                        DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        emConfigFile = envProperties.getRolePropertyById(emRoleId,
                                                          DeployEMFlowContext.ENV_EM_CONFIG_FILE);
       
        System.out.println("Following"+emHost+" "+tomcatAgenthost+" "+weblogicAgenthost1+" "+weblogicAgenthost2);
    }
    
    
    public void appendProp(List<String> newProp, String machineID, String filePath) {

        FileModifierFlowContext propertyChange =
            new FileModifierFlowContext.Builder().append(filePath, newProp).build();
        runFlowByMachineId(machineID, FileModifierFlow.class, propertyChange);
    }
    
    public void replaceProp(String oldProp, String newProp, String machineID, String filePath) {


        FileModifierFlowContext propertyChange =  
            new FileModifierFlowContext.Builder().replace(filePath,
                Collections.singletonMap(oldProp, newProp)).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyChange);

    }
    public void stopEM(String emRoleId1) {
        EmUtils emUtils = utilities.createEmUtils();
        
     
        ClwUtils clwUtilsEM = utilities.createClwUtils(emRoleId1);
        try {
            emUtils.stopLocalEmWithTimeoutSec(clwUtilsEM.getClwRunner(), emRoleId1, 240);
           
            //emUtils.stopRemoteEmWithTimeoutSec(clwRunnerLocalEm, clwRunnerRemoteEm, timeout);
            
            
         
        } catch (Exception e) {
            e.printStackTrace();
           System.out.println("Improper Shutdown EM");
        }
    }
   
    public final String  LOGIN_USERNAME="input[id='username']";
    public final String  LOGIN_LOGINBUTTON="#webview-loginPage-login-button";
    public final String  POPUP_CLOSE="button[id='btn-close']";
    public final String  LINK_WEBVIEW="#ca-navbar-top div>span>a";
    public final String  WEBVIEW_LOGOUT="//*[@id='webview-logout-link']";
    public final String  WEBVIEW_INVESTIGATOR="#webview-TabPanel>div>div>ul li:nth-child(3)";
    //public final String  WEBVIEW_INVESTIGATOR_SUPERDOMAIN="div[ftid='investigator-tree-2_Domain:']>div>img[class='GMDBJMCDKLB']";
    public final String  WEBVIEW_INVESTIGATOR_SUPERDOMAIN="div[ftid='investigator-tree-2_Domain:']>div>img:nth-child(2)";
    protected String  WEBVIEW_INVESTIGATOR_AGENTNODE="div[ftid='investigator-tree-2_Host:kansr04-I165417']>div>img:nth-child(2)";
    protected String  WEBVIEW_TOMCATINVESTIGATOR_AGENTNODE="div[ftid='investigator-tree-2_Host:tas-itc-n33']>div>img:nth-child(2)";
    protected  String  WEBVIEW_AGENTNODE_AGENTNAME="div[ftid='investigator-tree-2_Process:kansr04-I165417|WebLogic']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_TOMCATAGENTNODE_AGENTNAME="div[ftid='investigator-tree-2_Process:tas-itc-n33|Tomcat']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_AGENTNAME_AGENTDOMAIN="div[ftid='investigator-tree-2_Agent:SuperDomain|kansr04-I165417|WebLogic|pipeorgandomain//server']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_TOMCATAGENTNAME_AGENTDOMAIN="div[ftid='investigator-tree-2_Agent:SuperDomain|tas-itc-n33|Tomcat|Tomcat Agent']>div>img[class='GMDBJMCDKLB']";
    
    protected  String  WEBVIEW_AGENTDOMAIN_JSPNODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_JSPNODE_REGISTERPATIENTNODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP|__index']>div>img[class='GMDBJMCDKLB']";
    
    
    
   
    
    public  String REGISTERPATIENT_BS_NODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|pipeorgandomain//server|Business Segment']>div>img[class='GMDBJMCDKLB']";
    public  String REGISTERPATIENT_TOMCATBS_NODE="div[ftid='investigator-tree-2_Path:SuperDomain|tas-itc-n33|Tomcat|Tomcat Agent|Business Segment']>div>img[class='GMDBJMCDKLB']";
    
    public  String REGISTERPATIENT_BS_NODE_DEFAULTBS="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|pipeorgandomain//server|Business Segment|Default BS']>div:nth-child(1)>span:last-child";
    public  String REGISTERPATIENT_TOMCATBS_NODE_DEFAULTBS="div[ftid='investigator-tree-2_Path:SuperDomain|tas-itc-n33|Tomcat|Tomcat Agent|Business Segment|Default BS']>div:nth-child(1)>span:last-child";
    
    public  String REGISTERPATIENT_BS_NODE_BTNODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|pipeorgandomain//server|Business Segment|MedrecBS']>div:nth-child(1)>span:last-child";
    public  String REGISTERPATIENT_TOMCATBS_NODE_BTNODE="div[ftid='investigator-tree-2_Path:SuperDomain|tas-itc-n33|Tomcat|Tomcat Agent|Business Segment|TomcatBS']>div:nth-child(1)>span:last-child";
    
    
    
    public  String REGISTERPATIENT_BS_NODE_DEFAULTBSNODE="div[ftid='investigator-tree-2_Path:SuperDomain|cemload21|WebLogic|WebLogic Agent|Business Segment|Default BS']>div:nth-child(1)>span:last-child";
    public final String REGISTERPATIENT_BS_TRACE="#webViewTypeview_2-TabPanel>div>div>ul>li:nth-child(4)";
    public final String REGISTERPATIENT_BS_TRACE_ROWCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody>tr";
    public final String REGISTERPATIENT_BS_TRACE_COLUMNCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody:nth-child(2)>tr>td";

    public final String  CEM_LOGIN_LOGINHOMELINK="//*[@id='cemLogin']";
    public final String  CEM_LOGIN_USERNAME="//*[@id='loginForm:loginId_userName']";
    public final String  CEM_LOGIN_PASSWORD="//*[@id='loginForm:loginId_passWord']";
    public final String  CEM_LOGIN_LOGINBUTTON="//*[@id='loginForm:loginId_loginButton']";
    
    public final String  CEM_ADMINISTRATION4="a[name='administrationMenu']";
    public final String  CEM_ADMINISTRATION_BUSINESSSERVICE="a[title='Business Services']";
    public final String   CEM_ADMINISTRATION_BSERVICE_NEW="input[name='new']";
    public final String  CEM_ADMINISTRATION_BSERVICE_BSNAME1="#name";
    public final String  CEM_ADMINISTRATION_BSERVICE_BSDESCRIPTION1="#description";
    public final String   CEM_ADMINISTRATION_BSERVICE_BSSAVE="input[value='Save']";
    public final String   CEM_ADMINISTRATION_BSERVICE_ERROR= ".error";
    
    public final String CEM_ADMINISTRATION_BSERVICE_BTNEW="//*[@id='controlDiv']/table/tbody/tr/td[1]/input";
    public final String CEM_ADMINISTRATION_BSERVICE_BTNAME="//*[@id='name']";
    public final String CEM_ADMINISTRATION_BSERVICE_BTDESCR="//*[@id='description']";
    public final String CEM_ADMINISTRATION_BSERVICE_BTSAVE="input[value='Save']";
    
    
    
    public final String CEM_ADMINISTRATION_ADDALLCOMP_LOGOUT="//*[@id='bannerDiv']/table[1]/tbody/tr/td[3]/div/span[2]/a";
    public final String   CEM_ADMINISTRATION_ADDALLCOMP_USERNAME="//*[@id='loginForm:loginId_userName']";
    public final String   CEM_ADMINISTRATION_ADDALLCOMP_PASSWORD="//*[@id='loginForm:loginId_passWord']";
    public final String CEM_ADMINISTRATION_ADDALLCOMP_LOGIN="//*[@id='loginForm:loginId_loginButton']";
    
    public final String CEM_ADMINISTRATION_ADDALLBT_ADMINISTRATION4="a[name='administrationMenu']";
    public final String CEM_ADMINISTRATION_ADDALLBT_BUSINESSSERVICE="a[title='Business Services']";
    public final String CEM_ADMINISTRATION_ADDALLBT_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
    public final String CEM_ADMINISTRATION_ADDALLBT_COLUMNCOUNT="//*[@id='tranDefGroup']/tbody/tr[1]/td";
    public final String CEM_ADMINISTRATION_ADDALLBT_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
    public final String CEM_ADMINISTRATION_ADDALLBT_ADMINISTRATION5="a[name='administrationMenu']";
    public final String CEM_ADMINISTRATION_ADDALLBT_BUSINESSSERVICE5="a[title='Business Services']";
    
    public final String   CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION4="a[name='administrationMenu']";
    public final String   CEM_ADMINISTRATION_ADDALLT_BUSINESSSERVICE="a[title='Business Services']";
    public final String   CEM_ADMINISTRATION_ADDALLT_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
    public final String   CEM_ADMINISTRATION_ADDALLT_COLUMNCOUNT="//*[@id='tranDefGroup']/tbody/tr[1]/td";
    public final String   CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION_AddALLT_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
    public final String   CEM_ADMINISTRATION_ADDALLT_ADMINISTRATION5="a[name='administrationMenu']";
    public final String   CEM_ADMINISTRATION_ADDALLT_BUSINESSSERVICE5="a[title='Business Services']";


   public final String    CEM_ADMINISTRATION_ADDALLCHILDT_BT="//*[@id='tranSetDef']/tbody/tr/td[2]/a";
   public final String    CEM_ADMINISTRATION_ADDALLCHILDT_NEWT="//*[@id='controlDiv']/input[1]";
   public final String    CEM_ADMINISTRATION_ADDALLCHILDT_TNAME="//*[@id='name']";
   public final String    CEM_ADMINISTRATION_ADDALLCHILDT_TDESCRIPTION="//*[@id='description']";
   public final String    CEM_ADMINISTRATION_ADDALLCHILDT_TSAVE="//*[@id='controlDiv']/input";
   
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_ADMINISTRATION4="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_BUSINESSSERVICE="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_COLUMNCOUNT="//*[@id='tranDefGroup']/tbody/tr[1]/td";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_ADMINISTRATION5="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_BUSINESSERVICE5="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_BT="//*[@id='tranSetDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_ADDALLCOMP_NEWT="//*[@id='tranUnitDef']/tbody/tr/td[2]/a";




   public final String  CEM_ADMINISTRATION_ADDALLCHILDCOMP_CNEW="//*[@id='controlDiv']/input[1]";
   public final String  CEM_ADMINISTRATION_ADDALLCHILDCOMP_CNAME="//*[@id='name']";
   public final String  CEM_ADMINISTRATION_ADDALLCHILDCOMP_CDESCR="//*[@id='description']";
   public final String  CEM_ADMINISTRATION_ADDALLCHILDCOMP_CSAVE="//*[@id='controlDiv']/input";
   
   
   
   
   public final String  CEM_ADMINISTRATION_AddALLURL_ADMINISTRATION4="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_AddALLURL_BUSINESSSERVICE="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_AddALLURL_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
   public final String  CEM_ADMINISTRATION_AddALLURL_columncount="//*[@id='tranDefGroup']/tbody/tr[1]/td";
   public final String  CEM_ADMINISTRATION_AddALLURL_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
   public final String  CEM_ADMINISTRATION_AddALLURL_ADMINISTRATION5="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_AddALLURL_BUSINESSSERVICE5="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_AddALLURL_BT="//*[@id='tranSetDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_AddALLURL_NEWT="//*[@id='tranUnitDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_AddALLURL_NEWC="//*[@id='tranCompDef']/tbody/tr/td[2]/a";



   public final String  CEM_ADMINISTRATION_ADDALLCHILDURL_NEWURL="//*[@id='controlDiv']/table/tbody/tr/td/input[1]";
   public final String  CEM_ADMINISTRATION_ADDALLCHILDURL_DRP="//*[@id='key_type']";
  public final String CEM_ADMINISTRATION_ADDALLCHILDURL_PATTERN="//*[@id='pattern']";
   public final String  CEM_ADMINISTRATION_ADDALLCHILDURL_URLSAVE="//*[@id='controlDiv']/table/tbody/tr/td/input";
   
   
   
   public final String  CEM_ADMINISTRATION_ADDALLPATH_ADMINISTRATION4="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_BUSINESSSERVICE="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_COLUMNCOUNT="//*[@id='tranDefGroup']/tbody/tr[1]/td";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_ADMINISTRATION5="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_BUSINESSSERVICE5="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_BT="//*[@id='tranSetDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_NewT="//*[@id='tranUnitDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_ADDALLPATH_NewC="//*[@id='tranCompDef']/tbody/tr/td[2]/a";


   public final String  CEM_ADMINISTRATION_AddALLCHILDPATH_NEWURL="//*[@id='controlDiv']/table/tbody/tr/td/input[1]";
   public final String  CEM_ADMINISTRATION_AddALLCHILDPATH_DRP="//*[@id='key.type']";
   public final String  CEM_ADMINISTRATION_AddALLCHILDPATH_DRP1="//*[@id='key.name']";
   public final String  CEM_ADMINISTRATION_AddALLCHILDPATH_PATTERN="//*[@id='pattern']";
   public final String  CEM_ADMINISTRATION_AddALLCHILDPATH_URLSAVE="//*[@id='controlDiv']/table/tbody/tr/td/input";

   public final String  CEM_ADMINISTRATION_ENAALLBS_LOGOUT="//*[@id='bannerDiv']/table[1]/tbody/tr/td[3]/div/span[2]/a";
   public final String  CEM_ADMINISTRATION_ENAALLBS_USERNAME="//*[@id='loginForm:loginId_userName']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_PaSSWORD="//*[@id='loginForm:loginId_passWord']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_LOGIN="//*[@id='loginForm:loginId_loginButton']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION4="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_ROWCOUNT="//*[@id='tranDefGroup']/tbody/tr";
   public final String  CEM_ADMINISTRATION_ENAALLBS_COLUMNCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
   public final String  CEM_ADMINISTRATION_ENAALLBS_CELLCOUNT="//*[@id='tranDefGroup']/tbody/tr/td";
   public final String  CEM_ADMINISTRATION_ENAALLBS_ADMINISTRATION5="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_BUSINESSSERVICE5="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_CHECKBT="//*[@id='tranSetDef']/thead/tr/th[1]/input";
   public final String  CEM_ADMINISTRATION_ENAALLBS_ENABLEBT="//*[@id='controlDiv']/table/tbody/tr/td[4]/input";
   public final String  CEM_ADMINISTRATION_ENAALLBS_DISABLEBT="input[value='Disable']";
   public final String  CEM_ADMINISTRATION_ENAALLBS_IDENTIFYBT="//*[@id='tranSetDef']/tbody/tr/td[2]/a";
   public final String  CEM_ADMINISTRATION_ENAALLBS_BTSPEC="//*[@id='tabList']/li[3]/a";

   public final String  CEM_ADMINISTRATION_DELETEBS_ADMINISTRATION5="a[name='administrationMenu']";
   public final String  CEM_ADMINISTRATION_DELETE_BUSINESSSERVICE5="a[title='Business Services']";
   public final String  CEM_ADMINISTRATION_DELETE_SELECTALLBS="input[title='selectAll']";
   public final String  CEM_ADMINISTRATION_DELETE_DELETE="input[name='delete']";
    
   
   public final String CEM_SYNCMON_SYNCMONITORSICON="//*[@id='navTrailDiv']/table/tbody/tr/td[2]/table/tbody/tr/td[2]/img";
   public final String CEM_SYNCMON_SYNCMONITORS="input[value='Synchronize All Monitors']";
   public final String CEM_SETUP="a[name='setupMenu']";
    
    
    
    
    


            

            



           


            
    
    
    
    
    

    
    
      
      
  

}


