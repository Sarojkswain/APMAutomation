package com.ca.apm.tests.utils;

import static org.testng.Assert.assertTrue;
import com.ca.tas.annotation.TasResource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.TasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.common.PropertiesUtility;
import com.ca.apm.commons.coda.common.Util; 

public class FileUtils extends TasTestNgTest{
    String EM_HOME;
    String EM_Config ;
    String toolsDir = "";
    String configUtilCommand = "";
    private static Logger LOGGER;
    public static final String TEMPLATE_FILES_LOC = "./resources/Templates"; 


    List<String> commandList = new ArrayList<String>();
    HashMap <String,String> inputs = new HashMap<String,String>();
    
    public FileUtils(){

        configUtilCommand = "java -jar ConfigUtility.jar -listchanges";
        commandList.add("configUtilCommand");
        LOGGER = Logger.getLogger(Util.class);

    }
    
    public void appendProp(List<String> newProp, String machineID, String filePath) {
        FileModifierFlowContext propertyAppend =
            new FileModifierFlowContext.Builder().append(filePath, newProp).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyAppend);
    }
    
    public void replaceProp(String oldProp, String newProp, String machineID, String filePath) {

        FileModifierFlowContext propertyReplace =
            new FileModifierFlowContext.Builder().replace(filePath,
                Collections.singletonMap(oldProp, newProp)).build();

        runFlowByMachineId(machineID, FileModifierFlow.class, propertyReplace);

    }

    public boolean verifyFile(String reportFile, String testcaseID_scenario){  
        String templateFile = TEMPLATE_FILES_LOC+"/"+testcaseID_scenario+".txt";
        File baseFile = new File(templateFile);
        File resultFile = new File(reportFile);
        boolean compare = baseFile.equals(resultFile);
        return compare;
    }
    
    public void DefaultValuesinPropertiesFileTest(String file, String testcaseID_scenario) throws IOException{
        //make no changes to file
        ApmbaseUtil.runCommand(commandList, toolsDir);  //run jar
        
    //    return verify(testcaseID_scenario);
    }
    
    public void commentPropertyInPropertiesFile (String file, String propertyName) throws Exception{
        List<String> propertyList = new ArrayList<String>();
        propertyList.add(propertyName);
        PropertiesUtility.commentProperties(file,propertyList); //make changes
    }
    
    public void modifyPropertyInPropertiesFile (String file, String propertyName, String propertyValue) throws Exception{
        PropertiesUtility.updateProperty(file, propertyName, propertyValue);    //make changes
    }
    
    public void deletePropertyInPropertiesFile (String file, String propertyName, String propertyValue) throws Exception{
        Util.replaceLine(file, propertyName+"="+propertyValue, ""); //make changes
    }
    
    public void uncommentPropertyinPropertiesFile(String file, String propertyName) throws Exception{
        List<String> propertyList = new ArrayList<String>();
        propertyList.add(propertyName);
        PropertiesUtility.uncommentProperties(file,propertyList);   //make changes
    }
    
    public void modifyCommentedPropertyinPropertiesFile(String file, String propertyName, String propertyValue, String testcaseID_scenario) throws Exception{
        PropertiesUtility.updateProperty(file, "#"+propertyName, propertyValue);    //make changes
    }
    
    public void deleteCommentedPropertyInPropertiesFile (String file, String propertyName, String propertyValue, String testcaseID_scenario) throws Exception{
        Util.replaceLine(file, "#"+propertyName+"="+propertyValue, ""); //make changes
    }
    
    public void uncommentAndModifyPropertyinPropertiesFile(String file, String propertyName, String propertyValue) throws Exception{
        List<String> propertyList = new ArrayList<String>();
        propertyList.add(propertyName);
        PropertiesUtility.uncommentProperties(file,propertyList);   //make changes 1
        PropertiesUtility.updateProperty(file, propertyName, propertyValue);    //make changes 2
    }
    
    public void addPropertyInPropertiesFile (String file, String propertyName, String propertyValue) throws Exception{
        List<String> propertyList = new ArrayList<String>();
        propertyList.add(propertyName);
        PropertiesUtility.insertProperty(file, propertyName, propertyValue);    //make changes
    }
    
    public void addCommentedPropertyInPropertiesFile (String file, String propertyName, String propertyValue) throws Exception{
        List<String> propertyList = new ArrayList<String>();
        propertyList.add(propertyName);
        PropertiesUtility.insertProperty(file, propertyName, propertyValue);    //make changes 1
        PropertiesUtility.commentProperties(file, propertyList);    //make changes 2
    }
    
    public void runClw(String command, String WorkingDirectory){
        
    }

    public String getPropertyValuePropertiesFile (String file, String propertyName) throws Exception{
        String value = "";
        value = PropertiesUtility.getPropertiesAsMap(file).get(propertyName);
        return value;
    }



}
