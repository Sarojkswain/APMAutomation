package com.ca.apm.tests.tests;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.tests.testbed.ConfigUtilityLinuxTestbed;
import com.ca.apm.tests.testbed.ConfigUtilityWindowsTestbed;
import com.ca.apm.tests.utils.XmlFileUtils;
import com.ca.apm.tests.utils.Utility;
import com.ca.apm.tests.utils.XmlModifications;
import com.ca.tas.test.TasTestNgTest;

public class ConfigUtilityXmlTests extends TasTestNgTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtilityXmlTests.class);
    private Utility  util = new Utility();
    private XmlFileUtils xmlUtils = new XmlFileUtils();

    //Install locations for Various installation instances
    public String InstLoc_EMFULL ;
    public String InstLoc_EM ;
    public String InstLoc_WV ;
    public String InstLoc_ACC ;
    public String InstLoc_WS ;
    public String OLD_EM_VERSION ;
    public String InstLoc_EMFULL_old ;

    {
        //Install locations for Various installation instances

        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            InstLoc_EMFULL = ConfigUtilityWindowsTestbed.InstLoc_EMFULL;
            InstLoc_EM = ConfigUtilityWindowsTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityWindowsTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityWindowsTestbed.InstLoc_ACC;
            InstLoc_WS = ConfigUtilityWindowsTestbed.InstLoc_WS;
            OLD_EM_VERSION = ConfigUtilityWindowsTestbed.OLD_EM_VERSION;
            InstLoc_EMFULL_old = ConfigUtilityWindowsTestbed.InstLoc_EMFULL_old;
        }
        else if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
            InstLoc_EMFULL = ConfigUtilityLinuxTestbed.InstLoc_EMFULL;
            InstLoc_EM = ConfigUtilityLinuxTestbed.InstLoc_EM;
            InstLoc_WV = ConfigUtilityLinuxTestbed.InstLoc_WV;
            InstLoc_ACC = ConfigUtilityLinuxTestbed.InstLoc_ACC;
            InstLoc_WS = ConfigUtilityLinuxTestbed.InstLoc_WS;
            InstLoc_EMFULL_old = ConfigUtilityLinuxTestbed.InstLoc_EMFULL_old;
        }
    }
    private String configLocation = InstLoc_EMFULL+"/config";
    private String sourceConfigLoc = InstLoc_EMFULL_old+"/config";
    XmlModifications xml = new XmlModifications();
    /*
     * Testcase Id #454799
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454799_AgentclustersXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454799 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "Agentclusters.xml";
        String filePath = configLocation+"/"+fileName;
        String property = "introscope.webview.startup.emcheck.interval";
        String propXpath = "";
        String value = "10";
        String modifiedValue = "15";
        String propToComment = "";
        String newProperty = "new.property";
        String newValue = "newValue";
        String parentNode = "<agent-clusters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"0.1\" xsi:noNamespaceSchemaLocation=\"agentclusters0.1.xsd\">";

        try{
            //No uncmmented properties are present in this file by default

            //add new property
            ApmbaseUtil.fileBackUp(filePath);

            String[] metrSpecifiers = { "CPU.*", "GC Heap.*" };
            String msg1 = XMLUtil.createAgentCluster(filePath,"CPUAndHeapMetricsAgAgentDomain", "SuperDomain",
                                                     ".*\\|.*\\|Web[S|L].*", metrSpecifiers);

            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            ApmbaseUtil.revertFile(filePath);

            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkXmlMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, parentNode, "<!--", "CPUAndHeapMetricsAgAgentDomain"))){
                flag = true;
            }
            ApmbaseUtil.revertFile(filePath);
            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #454798

     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454798_apmEventsThresholdsConfigXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454798 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "apm-events-thresholds-config.xml";
        String filePath = configLocation+"/"+fileName;

        String  primaryElements = "clamp";
        String key = "id";
        String key_value ="introscope.enterprisemanager.agent.connection.limit";
        String element ="description";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "value";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<threshold value=\"1024\"/>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #454797

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454797_apmPerfMonContextXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454797 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "apm-perf-mon-context.xml";
        String filePath = configLocation+"/"+fileName;
        String primaryElements = "entry";
        String key = "key";
        String key_value = "Hibernate:name=AppMapStatistics";
        String new_element = "new.ref";
        String newElementValue = "new.ref.value";
        String attribute = "local";
        String new_attribute = "new.value";
        String newAttributeValue = "new.value.new";
        String element = "ref";
        String comment = "<value>true</value>";
        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, attribute, newAttributeValue,1);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,1);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,"value",newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", "value",newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,1);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,""))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }


    }


    /*
     * Testcase Id #454796

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454796_CatalystPolicyXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454796 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "CatalystPolicy.xml";
        String filePath = configLocation+"/"+fileName;
        String primaryElements = "entity";
        String key = "name";
        String key_value = "DatabaseInstance";
        String new_element = "new.ref";
        String newElementValue = "new.ref.value";
        String element = "include_relations";
        String comment = "<value>true</value>";
        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);


            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", element,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,""))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454795

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454795_cemHibernateCfgXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454795 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "cem.hibernate.cfg.xml";
        String filePath = configLocation+"/"+fileName;
        String primaryElements = "mapping";
        String key = "resource";
        String key_value = "com/timestock/tess/data/objects/Locks.hbm.xml";
        String new_element = "mapping";
        String newElementValue = "new.mapping.value";
        String element = "<mapping resource=\"com/timestock/tess/data/objects/Locks.hbm.xml\"/>";
        String comment = "<mapping resource=\"com/timestock/tess/data/objects/Locks.hbm.xml\"/>";
        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            //  xml.deleteElement(filePath, primaryElements, key, key_value, element);
            Util.replaceLine(filePath, element, "");    //make changes
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            //   xml.updateElementValue(filePath,primaryElements, key,key_value,"value",newElementValue);
            Util.replaceLine(filePath, "<mapping resource=\"com/timestock/tess/data/objects/Locks.hbm.xml\"/>", "<mapping resource=\"new/resource/value\"/>");    //make changes
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", "value",newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,""))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454792

     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454792_domainsXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454792 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "domains.xml";
        String filePath = configLocation+"/"+fileName;
        String primaryElements = "agent";
        String key = "mapping";
        String key_value = "(.*)";
        String new_element = "grant";
        String newElementValue = "new entry";
        String element = "group";
        String comment = "<grant group=\"Admin\" permission=\"full\"/>";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value";
        String attribute = "mapping";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            // xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            XMLUtil.createUserGrantElement(filePath, "CEM Analyst", "full");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", "CEM Analyst","full"))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            //     xml.deleteElement(filePath, primaryElements, key, key_value, element);
            XMLUtil.deleteElement(filePath, primaryElements, key, key_value);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,""))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);


            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #454778

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454778_eiamConfigFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454778 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "eiam.config";
        String filePath = configLocation+"/"+fileName;

        //for attribute
        String attribute_element = "CyclicBuffer";
        String attribute_key = "file";
        String attribute_key_value = "dump.log";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String attribute = "size";
        //for element
        String  primaryElements = "SDK";
        String key = "type";
        String key_value ="Java";
        String element ="FIPSMode";
        String new_element = "new.element";
        String newElementValue ="new value";

        String comment = "<logLevel></logLevel>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, attribute_element, attribute_key, attribute_key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", attribute_key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,1);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, attribute_element, attribute_key, attribute_key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,InstLoc_EMFULL, attribute_key,attribute_key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #454777

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454777_eiamLog4jConfigFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454777 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "eiam.log4j.config";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "appender";
        String key = "name";
        String key_value ="SDK";
        String element ="layout";
        String new_element = "param";
        String newElementValue ="";
        String attribute = "class";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<param name=\"file\" value=\"eiam.javasdk.log\" />";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", comment,comment))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454791

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454791_emJettyConfigXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454791 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "em-jetty-config.xml";
        String filePath = configLocation+"/"+fileName;
        String key_value ="com.wily.webserver.TrustingSslSocketConnector";


        try{
            ApmbaseUtil.fileBackUp(filePath);
           // new XMLUtil().addHttpEntryInEMJetty(filePath);   This method is removed from xMLUtils. Need to re-write
            LOGGER.info("This method is not complete. Some of the internal methods that are imported from other project have been deleted. Please check ");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,"http"))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #454794

     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454794_loadbalancingXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454794 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "loadbalancing.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "clamp";
        String key = "id";
        String key_value ="introscope.enterprisemanager.agent.connection.limit";
        String element ="description";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "value";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<threshold value=\"1024\"/>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            //    xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            XMLUtil xml_util = new XMLUtil();
            xml_util.addEmptyCollectorEntryInLoadbalanceXML(filePath, "agent_Collector_1", ".*\\|.*\\|EPAgent.*", "type");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }


    }

    /*
     * Testcase Id #454801

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454801_loggingXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454801 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "logging.xml";
        String filePath = configLocation+"/shibboleth/conf/"+fileName;

        String  primaryElements = "appender";
        String key = "name";
        String key_value ="IDP_AUDIT";
        String element ="File";
        String new_element = "new.node";
        String newElementValue ="new ndode.value";
        String attribute = "class";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<File>logs/shibboleth/idp-audit.log</File>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,1);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454793

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454793_realmsXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454793 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "realms.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "property";
        String key = "name";
        String key_value ="usersFile";
        String element ="value";
        String new_element = "new.value";
        String newElementValue ="new node value";
        String attribute = "name";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<value>users.xml</value>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454800

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454800_relyingPartyXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454800 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "relying-party.xml";
        String filePath = configLocation+"/shibboleth/conf/"+fileName;


        String  primaryElements = "security:Credential";
        String key = "id";
        String key_value ="IdPCredential";
        String element ="security:PrivateKey";
        String new_element = "new.element";
        String newElementValue ="new element value";
        String attribute = "xsi:type";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<security:Certificate>config/internal/server/EMcert.pem</security:Certificate>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }


    /*
     * Testcase Id #454790

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454790_samlSpMetadataXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454790 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "saml-sp-metadata.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "md:EntityDescriptor";
        String key = "entityID";
        String key_value ="com.ca.apm.webview.serviceprovider";
        String element ="md:OrganizationName";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "xml:lang";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<md:OrganizationName xml:lang=\"en\">CA APM</md:OrganizationName>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454789

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454789_samlSpWebstartMetadataXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454789 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "saml-sp-webstart-metadata.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "md:EntityDescriptor";
        String key = "entityID";
        String key_value ="com.ca.apm.webstart.serviceprovider";
        String element ="md:OrganizationName";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "xml:lang";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<md:OrganizationName xml:lang=\"en\">CA APM</md:OrganizationName>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    
    
    /*
     * Testcase Id #454788

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454788_serverXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454788 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "server.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "grant";
        String key = "group";
        String key_value ="Admin";
        String element ="grant";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "permission";
        String new_attribute = "new.attribute";
        String newAttributeValue = "none";
        String comment = "<grant group=\"Admin\" permission=\"none\"/>";

        String groupName="CEM Analyst";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            //   xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            XMLUtil.createGroupGrantForElement(filePath, "server", groupName, "full");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454787

     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454787_tessDbCfgXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454787 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "tess-db-cfg.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "property";
        String key = "name";
        String key_value ="em.dbtype";
        String element ="property";
        String new_element = "new.property";
        String newElementValue ="new property value";
        String attribute = "value";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<property name=\"em.dbtype\">Postgres</property>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454784
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454784_usersXmlAccFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454784 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "users.xml";
        String filePath = InstLoc_ACC+"/config/"+fileName;

        String  primaryElements = "user";
        String key = "firstname";
        String key_value ="User";
        String element ="user";
        String newElementValue ="new description";
        String attribute = "password";
        String new_attribute = "new.attribute";
        String newAttributeValue = "none";
        String comment = "<user email=\"user@example.com\" firstname=\"User\" lastname=\"Demo\" password=\"\"/>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            XMLUtil.createUserInUsersXML(filePath, "newuser", "newUserPassword");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,"newuser"))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TEST CASE 454784 EXECUTION COMPLETED >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }


    /*
     * Testcase Id #455326
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_455326_VerifyConfigUtilityJarForNewAttributeInXml(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455326 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "apm-events-thresholds-config.xml";
        String filePath = configLocation+"/"+fileName;

        String  primaryElements = "clamp";
        String key = "id";
        String key_value ="introscope.enterprisemanager.agent.connection.limit";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;


        try{

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){
             
        }
    }

    /*
     * Testcase Id #454786

     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_454786_usersXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454786 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "users.xml";
        String filePath = configLocation+"/"+fileName;

        String  primaryElements = "user";
        String key = "firstname";
        String key_value ="User";
        String element ="user";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "password";
        String new_attribute = "new.attribute";
        String newAttributeValue = "none";
        String comment = " <user password=\"\" name=\"Admin\"/>";

        String userName="newuser";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            //   xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            XMLUtil.createUserInUsersXML(filePath, userName, "newUserPassword");
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,userName))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }

    }

    /*
     * Testcase Id #454785
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_454785_webviewJettyConfigXmlFileTest(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454785 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "webview-jetty-config.xml";
        String filePath = configLocation+"/"+fileName;
        /*  String property = "introscope.webview.startup.emcheck.interval";
        String propXpath = "";
        String value = "10";
        String modifiedValue = "15";
        String propToComment = "";
        String newProperty = "new.property";
        String newValue = "newValue";

         */
        try{
            ApmbaseUtil.fileBackUp(filePath);
          //  new XMLUtil().addHttpEntryInEMJetty(filePath);   
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", "call","/call"))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    /*
     * Testcase Id #455119
     */
    @Test(groups = { "BAT","EM"  })
    public void verify_ALM_455119_runConfigUtilityForOrderChangeInXml(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455119 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag1,flag2 = false;
        Process p ;

        String fileName = "cem.hibernate.cfg.xml";
        String filePath = InstLoc_ACC+"/config/"+fileName;
        String line1 = "<mapping resource=\"com/timestock/tess/data/objects/Locks.hbm.xml\"/>";
        String line2 = "<mapping resource=\"com/timestock/tess/data/objects/StartTimes.hbm.xml\"/>";
        String tempLine = "this is a temporary line";
        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            util.updateLine(fileName, line2, tempLine);
            util.updateLine(fileName, line1, line2);
            util.updateLine(fileName, tempLine, line1);

            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");

            flag1 = util.verifyCommandOutput(p,"Report Generated");
            flag2 = util.checkXmlMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", line1, line2);

            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag1&&!flag2);
        }catch(Exception e){
             
        }
    }


    /*
     * Testcase Id #455328
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_455328_VerifyAttributeOrderForAddAttributeInXml(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455328 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -d "+configLocation};
        Boolean flag = false;
        Process p ;

        String fileName = "apm-events-thresholds-config.xml";
        String filePath = configLocation+"/"+fileName;

        String  primaryElements = "clamp";
        String key = "id";
        String key_value ="introscope.enterprisemanager.agent.connection.limit";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;


        try{

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){
             
        }
    }


    /*
     * Testcase Id #455322
     */
    @Test(groups = { "SMOKE","EM"  })
    public void verify_ALM_455322_VerifyConfigUtilityForAttributeModifiedInXml(){
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455322 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfigLoc+" -d "+configLocation};
        Boolean flag = false;
        Process p ;


        String fileName = "users.xml";
        String filePath = sourceConfigLoc+"/"+fileName;

        String  primaryElements = "user";
        String key = "firstname";
        String key_value ="User";
        String attribute = "password";
        String newAttributeValue = "none";



        try{

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);
            Assert.assertTrue(flag);
        }catch(Exception e){
             
        }
    }


    /**
     * Test Case ID : 454850
     * Test case description
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_454850_VerifyReportForModificationInxmlFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfigLoc+" -d "+configLocation};
        try {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 454850 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            //create backup
            ApmbaseUtil.fileBackUp(InstLoc_EMFULL_old+"/config/domains.xml");
            //edit file
            ApmbaseUtil.appendProperties(Arrays.asList("newProperty=newValue"),"domains.xml", InstLoc_EMFULL_old+"/config");
            //run cmd
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            //restore file
            ApmbaseUtil.revertFile(InstLoc_EMFULL_old+"/config/domains.xml");

            Boolean flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", "domains.xml", "newProperty", "", "newValue");
            Assert.assertTrue(flag);

        } catch (Exception e) {
             
        }
    }

    /**
     * Test Case ID : 455327
     * Test case description : Run ConfigUtility jar to verify two sections in viewChanges output
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_455327_VerifyReportForAddTagInxmlFiles() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfigLoc+" -d "+configLocation};
        Boolean flag = false;
        String customChangesHeader = "Following are the configuration changes that have been identified by the installer which haven't been migrated upon an upgrade.Based on your requirements, you may want to manually carry forward these changes to the upgraded EM.";
        String defaultChangesHeader = "These are default values of configurations that were changed by CA in the upgraded release.";
        String fileName = "apm-events-thresholds-config.xml";
        String filePath = configLocation+"/"+fileName;

        String  primaryElements = "clamp";
        String key = "id";
        String key_value ="introscope.enterprisemanager.agent.connection.limit";
        String element ="description";
        String new_element = "new.description";
        String newElementValue ="new description";

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> START TEST CASE 455327 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");


        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            Process p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);


            flag =  util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", customChangesHeader,"log4j.properties", "log4j.rootCategory",defaultChangesHeader);

            Assert.assertTrue(flag);
        } catch (Exception e) {
        }
    }

    /*
     *  String fileName = "tess-db-cfg.xml";
        String filePath = configLocation+"/"+fileName;


        String  primaryElements = "property";
        String key = "name";
        String key_value ="em.dbtype";
        String element ="property";
        String new_element = "new.property";
        String newElementValue ="new property value";
        String attribute = "value";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<property name=\"em.dbtype\">Postgres</property>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
     */

    /**
     * Test Case ID : 455324
     * Test case description : Run ConfigUtility jar 
     */

    @Test(groups = {"configUtility", "SMOKE"})

    public void verify_ALM_455324_VerifyReportForxmlDifference() {
        String[] command = {"java -jar ConfigUtility.jar -viewchanges -s "+sourceConfigLoc+" -d "+configLocation};
        Boolean flag = false;
        String customChangesHeader = "Following are the configuration changes that have been identified by the installer which haven't been migrated upon an upgrade.Based on your requirements, you may want to manually carry forward these changes to the upgraded EM.";
        String defaultChangesHeader = "These are default values of configurations that were changed by CA in the upgraded release.";

        String fileName = "tess-db-cfg.xml";
        String filePath = sourceConfigLoc+"/"+fileName;

        Process p ;
        String  primaryElements = "md:EntityDescriptor";
        String key = "entityID";
        String key_value ="com.ca.apm.webstart.serviceprovider";
        String element ="md:OrganizationName";
        String new_element = "new.description";
        String newElementValue ="new description";
        String attribute = "xml:lang";
        String new_attribute = "new.attribute";
        String newAttributeValue = "new.attribute.value" ;
        String comment = "<md:OrganizationName xml:lang=\"en\">CA APM</md:OrganizationName>";

        try{
            //add element
            ApmbaseUtil.fileBackUp(filePath);
            xml.addElement(filePath, primaryElements, key, key_value, new_element, newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //add attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.addAttribute(filePath, primaryElements, key, key_value, new_attribute, newAttributeValue,0);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newAttributeValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : add new attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete element
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteElement(filePath, primaryElements, key, key_value, element,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete element for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //delete attribute
            ApmbaseUtil.fileBackUp(filePath);
            xml.deleteAttribute(filePath, primaryElements, key, key_value, attribute,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,element))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : delete attribute for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify element value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateElementValue(filePath,primaryElements, key,key_value,element,newElementValue);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify element value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //modify attribute value
            ApmbaseUtil.fileBackUp(filePath);
            xml.updateAttributeValue(filePath,primaryElements, key,key_value,attribute,newAttributeValue,3);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : modify attribute value for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            //comment 
            ApmbaseUtil.fileBackUp(filePath);
            xml.lineCommentAttributeInXmlFile(filePath, comment);
            p = ApmbaseUtil.getProcess(command, InstLoc_EMFULL+"/tools");
            if((util.verifyCommandOutput(p,"Report Generated")&&util.checkMessages(InstLoc_EMFULL+"/tools/ConfigChanges.txt", fileName, "", key_value,newElementValue))&&flag){
                flag = true;
            }
            else{
                LOGGER.info("Test failed : comment a property for file "+filePath);
            }
            ApmbaseUtil.revertFile(filePath);

            Assert.assertTrue(flag);
        }catch(Exception e){ }
    }

    
}
