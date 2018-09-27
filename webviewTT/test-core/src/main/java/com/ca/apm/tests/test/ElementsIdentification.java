package com.ca.apm.tests.test;

import java.util.Collections;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.testbed.AgentsWindowsStandaloneTestbed;
import com.ca.tas.test.TasTestNgTest;

public class ElementsIdentification extends TasTestNgTest
{

    protected String emMachineId;
    private String agentMachineId;

    protected String emRoleId;
    protected String tomcatRoleId;
    protected String weblogicRoleId;
    protected String jbossRoleId;
    public String emHost;
    protected String tomcatAgenthost;
    private String jbossAgenthost;
    protected String weblogicAgenthost;
    private int port;
    protected String emConfigdir,emConfigFile;
   
    public void initializeEMandAgents()
    {

        
        emMachineId = AgentsWindowsStandaloneTestbed.EM_MACHINE_ID;
        agentMachineId = AgentsWindowsStandaloneTestbed.AGENT_MACHINE_ID;

        emRoleId = AgentsWindowsStandaloneTestbed.EM_ROLE_ID;
        tomcatRoleId = AgentsWindowsStandaloneTestbed.TOMCAT_ROLE_ID;
        jbossRoleId = AgentsWindowsStandaloneTestbed.JBOSS_ROLE_ID;
        weblogicRoleId=AgentsWindowsStandaloneTestbed.WEBLOGIC_ROLE_ID;

        emHost =
            envProperties
                .getMachineHostnameByRoleId(AgentsWindowsStandaloneTestbed.EM_ROLE_ID);
        tomcatAgenthost = envProperties.getMachineHostnameByRoleId(AgentsWindowsStandaloneTestbed.TOMCAT_ROLE_ID);
        String[] a= tomcatAgenthost.split("\\.");
        tomcatAgenthost=a[0];
        jbossAgenthost = envProperties.getMachineHostnameByRoleId(AgentsWindowsStandaloneTestbed.JBOSS_ROLE_ID);
        a=jbossAgenthost.split("\\.");
        jbossAgenthost=a[0];
        weblogicAgenthost= envProperties.getMachineHostnameByRoleId(AgentsWindowsStandaloneTestbed.WEBLOGIC_ROLE_ID);
        a=weblogicAgenthost.split("\\.");
        weblogicAgenthost=a[0];
        port =
            Integer.parseInt(envProperties.getRolePropertyById(emRoleId,
                DeployEMFlowContext.ENV_EM_PORT)); 
        emConfigdir = envProperties.getRolePropertyById(emRoleId,
                                                        DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        emConfigFile = envProperties.getRolePropertyById(emRoleId,
                                                          DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        System.out.println("Following"+emHost+" "+tomcatAgenthost+" "+jbossAgenthost+" "+weblogicAgenthost);
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
    protected  String  WEBVIEW_AGENTNODE_AGENTNAME="div[ftid='investigator-tree-2_Process:kansr04-I165417|WebLogic']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_AGENTNAME_AGENTDOMAIN="div[ftid='investigator-tree-2_Agent:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_AGENTDOMAIN_JSPNODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP']>div>img[class='GMDBJMCDKLB']";
    protected  String  WEBVIEW_JSPNODE_REGISTERPATIENTNODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP|__index']>div>img[class='GMDBJMCDKLB']";
    //public String  WEBVIEW_AGENTNODE_AGENTNAME="div[ftid='investigator-tree-2_Process:"+weblogicAgenthost+"|WebLogic']>div>img:nth-child(2)";
    //public String  WEBVIEW_AGENTNAME_AGENTDOMAIN="div[ftid='investigator-tree-2_Agent:SuperDomain|"+weblogicAgenthost+"|WebLogic|WebLogicAgent']>div>img:nth-child(2)";
   // public String  WEBVIEW_AGENTDOMAIN_JSPNODE="div[ftid='investigator-tree-2_Path:SuperDomain|"+weblogicAgenthost+"|WebLogic|WebLogicAgent|JSP']>div>img:nth-child(2)";
    //public String  WEBVIEW_JSPNODE_REGISTERPATIENTNODE="div[ftid='investigator-tree-2_Path:SuperDomain|"+weblogicAgenthost+"|WebLogic|WebLogicAgent|JSP|__index']>div>img:nth-child(2)";
    //public final String  WEBVIEW_INVESTIGATOR_AGENTNODE="div[ftid='investigator-tree-2_Host:kansr04-I165417']>div>img[class='GMDBJMCDKLB']";
    
    //public  String  WEBVIEW_JSPNODE_REGISTERPATIENTNODE_AVG="div[ftid='investigator-tree-2_Metric:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP|__index:Average Response Time (ms):1025']";
    public  String  WEBVIEW_JSPNODE_REGISTERPATIENTNODE_AVG="div[ftid='investigator-tree-2_Metric:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP|__index:Responses Per Interval:8194']";
    public final String  WEBVIEW_ALERT="#webview-treenode-contextmenu>div>div>div>span";
    public final String  WEBVIEW_ALERT_NAME="#webview-mmEditor-Element-Name-Field-input";
    public final String  WEBVIEW_ALERT_OK="#webview-mmEditor-Element-OK-Button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ALERT_ACTIVE="input[name='webview-mmEditor-mmeditorContent-active-check']";
    public final String  WEBVIEW_ALERT_COMBINATION="#webview-MMEditor-aEditor-combination-combo-input";
    public final String  WEBVIEW_ALERT_RESOLUTION="#webview-MMEditor-aEditor-resolution-combo-input";
    public final String  WEBVIEW_ALERT_NOTIFYINDIVIDUAL="input[name='webview-MMEditor-aEditor-webview-MMEditor-alertEditor-notification-checkbox']";
    public final String  WBVIEW_ALERT_TRIGGERNOTIFY="#webview-MMEditor-aEditor-triggermode-combo-input";
    public final String  WEBVIEW_ACTION_ADD_DANGER="#webview-MMEditor-aEditor-danger-add-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ACTION_ADD_CAUTION="#webview-MMEditor-aEditor-caution-add-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ACTION_SELECTTT=".apmDataAlignedDialog[title='TTAction']";
    public final String  WEBVIEW_ACTION_CHOOSE="div[id='webview-MMEditor-aEditor-Choose-Button']>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ALERT_APPLY="#webview-MMEditor-buttonBar-apply-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ALERT_REVERT="#webview-MMEditor-buttonBar-revert-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ALERT_DELETE="#webview-MMEditor-buttonBar-delete-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  WEBVIEW_ALERT_DELETE_YES="#webview-Common-ConfirmMessageBox-button-yes>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    
   
    
    
    public final String MANAGEMENT_SUPERDOMAIN="div[ftid='management-tree_*SuperDomain*']>div>img:nth-child(2)";
    public final String MANAGEMENT_MODULES="div[ftid='management-tree_*SuperDomain*|Management Modules']>div>img:nth-child(2)";
    public final String MANAGEMENT_DEFAULT="div[ftid='management-tree_*SuperDomain*|Management Modules|Default']>div>img:nth-child(2)";
    public final String MANAGEMENT_ACTION="div[ftid='management-tree_*SuperDomain*|Management Modules|Default|Actions']>div>img:nth-child(2)";
    public final String MANAGEMENT_ACTION_SELECT="div[ftid='management-tree_*SuperDomain*|Management Modules|Default|Actions|TTAction']";
    public final String MANAGEMENT_ALERTS="div[ftid='management-tree_*SuperDomain*|Management Modules|Default|Alerts']>div>img:nth-child(2)";
    public final String MANAGEMENT_ALERTS_SELECT="div[ftid='management-tree_*SuperDomain*|Management Modules|Default|Alerts|AlertonRegisterTT']";
    
    
    
    public final String  WEBVIEW_MANAGEMENTMODULE="#webview-TabPanel>div>div>ul li:nth-child(5)";
    public final String  MANAGEMENTMODULE_ELEMENT="#webViewManagementView>div>div>div>#webview-MMEditor-elements-splitButton>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td:nth-child(1)>div";
    public final String  ELEMENT_NEWACTION="#x-menu-el-webview-MMEditor-newAction-menuItem>#webview-MMEditor-newAction-menuItem>span";
    public final String  ELMENT_NEWTTACTION="#x-menu-el-webview-MMEditor-newTrxTraceAction-menuItem>#webview-MMEditor-newTrxTraceAction-menuItem>span";
    public final String  NEWTTACTION_NAME="#webview-mmEditor-Element-Name-Field-input";
    public final String  NEWTTACTION_NAME_MGMT="#webview-elementCreationPanel-Element-Construct-Combo-input";
    public final String  NEWTTACTION_OK="#webview-mmEditor-Element-OK-Button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  NEWTTACTION_POSTCREATION_NAME="#webview-mmEditor-mmeditorContent-name-field-input";
    public final String  NEWTTACTION_ACTIVE="input[name='webview-mmEditor-mmeditorContent-active-check']";
    public final String  NEWTTACTION_TRACEALL="input[name='webview-TTEditor-mmeditorContent-TraceAll-Radio']";
    public final String  NEWTTACTION_TRACEONLY="input[name='webview-TTEditor-mmeditorContent-fTraceOnly-Radio']";
    public final String  NEWTTACTION_BSCHECK="input[name='webview-TTEditor-mmeditorContent-BusinessSegment-check']";
    public final String  NEWTTACTION_THRESHOLDINPUT="input[id='webview-TTEditor-mmeditorContent--Threshold-input']";
    public final String  NEWTTACTION_TTIMEINPUT="input[id='webview-TTEditor-mmeditorContent--Duration-input']";
    public final String  NEWTTACTION_APPLY="#webview-MMEditor-buttonBar-apply-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  NEWTTACTION_REVERT="#webview-MMEditor-buttonBar-revert-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  NEWTTACTION_DELETE="#webview-MMEditor-buttonBar-delete-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  NEWACTION_DELETE_YES="#webview-Common-ConfirmMessageBox-button-yes>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String  NEWACTION_THRESHOLD_ALERT="#webview-Common-AlertMessageBox-message";
    public final String  NEWACTION_THRESHOLD_OK="#webview-Common-AlertMessageBox-button-ok>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    
    
    public final String MANAGEMENT_METRICGROUPING="div[ ftid='management-tree_*SuperDomain*|Management Modules|Default|Metric Groupings']>div>img:nth-child(2)";
    public final String MANGEMENT_REGISTERMETRICGROUP="div[ftid='management-tree_*SuperDomain*|Management Modules|Default|Metric Groupings|AlertonRegisterTT']";
    public final String REGISTERMETRICGROUP_ADD="#webview-mgEditor-add-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String REGISTERMETRICGROUP_AGENTEXPR1="#webview-mgEditor-exp-vLayout2>div:nth-child(1)>div>div>div:nth-child(1)>div>input";
    public final String REGISTERMETRICGROUP_METRICEXPR1="#webview-mgEditor-exp-vLayout2>div:nth-child(1)>div>div>div:nth-child(2)>div>input";
    public final String REGISTERMETRICGROUP_AGENTEXPR2="#webview-mgEditor-exp-vLayout2>div:nth-child(2)>div>div>div:nth-child(1)>div>input";
    public final String REGISTERMETRICGROUP_METRICEXPR2="#webview-mgEditor-exp-vLayout2>div:nth-child(2)>div>div>div:nth-child(2)>div>input";
    public final String REGISTERMETRICGROUP_AGENTEXPR3="#webview-mgEditor-exp-vLayout2>div:nth-child(3)>div>div>div:nth-child(1)>div>input";
    public final String REGISTERMETRICGROUP_METRICEXPR3="#webview-mgEditor-exp-vLayout2>div:nth-child(3)>div>div>div:nth-child(2)>div>input";
    public final String REGISTERMETRICGROUP_AGENTEXPR4="#webview-mgEditor-exp-vLayout2>div:nth-child(4)>div>div>div:nth-child(1)>div>input";
    public final String REGISTERMETRICGROUP_METRICEXPR4="#webview-mgEditor-exp-vLayout2>div:nth-child(4)>div>div>div:nth-child(2)>div>input";
    public final String REGISTERMETRICGROUP_APPLY="#webview-MMEditor-buttonBar-apply-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String REGISTERMETRICGROUP_DELETE="#webview-MMEditor-buttonBar-delete-button>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    public final String REGISTERMETRICGROUP_DELETE_YES="#webview-Common-ConfirmMessageBox-button-yes>div>table>tbody>tr:nth-child(2)>td:nth-child(2)>div>div>table>tbody>tr>td>div";
    
    public       String REGISTERPATIENT_CLICK="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|JSP|__index']>div>span:last-child";
    public final String REGISTERPATIENT_TRACE="#webViewTypeview_2-TabPanel>div>div>ul>li:nth-child(3)>a:nth-child(2)>em>span>span";
    public final String REGISTERPATIENT_TRACE_ROWCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody>tr";
    public final String REGISTERPATIENT_TRACE_COLUMNCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody:nth-child(2)>tr>td";

    //public final String REGISTERPATIENT_TRACE_COLUMNCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody>tr:nth-child(1)>td";
    public final String REGISTERPATIENT_TRACE_CELLCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody>tr>td";
    
    
    public final String HELLOWORLD_TRACE="";
    public final String HELLOWORLD_TRACE_ROWCOUNT="";
   
    
    public  String REGISTERPATIENT_BS_NODE="div[ftid='investigator-tree-2_Path:SuperDomain|kansr04-I165417|WebLogic|WebLogic Agent|Business Segment']>div:nth-child(1)>span:last-child";
    public final String REGISTERPATIENT_BS_TRACE="#webViewTypeview_2-TabPanel>div>div>ul>li:nth-child(4)";
    public final String REGISTERPATIENT_BS_TRACE_ROWCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody>tr";
    public final String REGISTERPATIENT_BS_TRACE_COLUMNCOUNT="#webview-investigator-tracetypeviewer-grid>div>div>div:nth-child(3)>div>table>tbody:nth-child(2)>tr>td";

    
    
    public final String HELLOWORLD_BS1_TRACE="";
    public final String HELLOWORLD_TRACE_BS1_ROWCOUNT="";

    
    
    
    
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

