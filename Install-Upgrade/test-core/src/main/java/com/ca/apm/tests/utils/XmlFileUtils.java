package com.ca.apm.tests.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.common.XMLFileUtil;

public class XmlFileUtils{

    String EM_HOME;
    String EM_Config ;
    String toolsDir = "";
    String configUtilCommand = "";
    private static Logger LOGGER;
    XMLFileUtil xUtil;
    public static final String TEMPLATE_FILES_LOC ="C:\\Report";// "./resources/templates"; 

    
    List<String> commandList = new ArrayList<String>();
    HashMap <String,String> inputs = new HashMap<String,String>();

    public XmlFileUtils(){
        configUtilCommand = "java -jar ConfigUtility.jar -listchanges";
        commandList.add(configUtilCommand);
        LOGGER = Logger.getLogger(Util.class);
        xUtil = new XMLFileUtil();

    }       


    public boolean verifyFile(String reportFile, String testcaseID_scenario){  
        String templateFile = TEMPLATE_FILES_LOC+"/"+testcaseID_scenario+".txt";
        File baseFile = new File(templateFile);
        File resultFile = new File(reportFile);
        boolean compare = baseFile.equals(resultFile);
        return compare;
    }

    public void lineCommentAttributeInXmlFile (String file, String line) throws Exception{
        Util.replaceLine(file, line, "<!--"+line+"-->");    //make changes
    }
    public void modifyNodeValueInXmlFile (String file, String xpath, String propertyValue) throws Exception{
        Util.replaceLine(file, "Limits the # of agent connections",propertyValue);    //make changes
    }
    public void modifyAttributeValueInXmlFile(String file, String xpath , String atributeName, String newAttributeValue) throws Exception{
        try{
            XmlModifierFlowContext xmlFlow = new XmlModifierFlowContext.Builder(file).setAttribute(xpath, atributeName, newAttributeValue).build();
        }catch(Exception e){}
    }
    public void deleteNodeInXmlFile (String file, String xpath) throws Exception{
        XmlModifierFlowContext xmlFlow = new XmlModifierFlowContext.Builder(file).deleteNode(xpath).build();//make changes
    }
    public void addNodeInXmlFile (String file, String xpath, String name, String value) throws Exception{
        XmlModifierFlowContext xmlFlow = new XmlModifierFlowContext.Builder(file).createNode(xpath, name, value).build();   //make changes
    }
    
    
}
