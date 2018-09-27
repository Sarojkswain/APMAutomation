package com.ca.apm.commons.coda.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;

/***
 * This class is used as util for all the DI Modules.
 */

public class DIUtil {
	
	
	    /**
	     * Common method used to Run CLW Command.
	     * 
	     * @param command
	     *            - Command that should be executed.
	     * @param isConsoleMsgReqd
	     *            - expected boolean value.
	     * @param logMessage
	     *            - message which should be verified on the console.
	     */
	  public static String runCommand(StringBuilder command) throws Exception {

	        String line = null;
	        Process process = null;
	        ProcessBuilder processBuilder = null;
	        processBuilder = new ProcessBuilder("cmd.exe", "/C", command.toString());
	       
	        processBuilder.redirectErrorStream(true);

	        try {
	            process = processBuilder.start();
	            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            line = bf.readLine();

	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (process != null) {
	                process.destroy();
	                process.getErrorStream().close();
	                process.getInputStream().close();
	                process.getOutputStream().close();
	            }
	        }
	        return line;
	    }

	  /***
	   * This method compares the 2 xml files, to check whether they are equal or not.
	   * 
	   */
	  public static boolean isXmlEqual(String orginalXMLLoc, String generatedXmlLoc, String outPutXmlFolder)
	    throws Exception {
	        boolean isNodeEqual = true;

	        /*String tempOrgXml = "c:/sw/temp_org.xml";
	             String tempGenXml = "c:/sw/temp_gen.xml";*/

	        String tempOrgXml = outPutXmlFolder+"\\temp_org.xml";
	        String tempGenXml = outPutXmlFolder+"\\temp_gen.xml";

	        boolean isOrgXmlFiltered = filterXml(orginalXMLLoc, tempOrgXml);
	        if(!isOrgXmlFiltered){
	            System.out.println("%%%%%%% ORGINAL XML NOT PARSED PROPERLY %%%%%%%%");
	            return isOrgXmlFiltered; 
	        }

	        boolean isGenXmlFiltered =filterXml(generatedXmlLoc, tempGenXml);
	        if(!isGenXmlFiltered){
	            System.out.println("%%%%%%% GENERATED XML NOT PARSED PROPERLY %%%%%%%%");
	            return isGenXmlFiltered; 
	        }  


	        ArrayList<Node> sourceEntry = new ArrayList<Node>();
	        ArrayList<Node> destinationEntry = new ArrayList<Node>();
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        dbf.setCoalescing(true);
	        dbf.setIgnoringElementContentWhitespace(true);
	        dbf.setIgnoringComments(true);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        FileInputStream fileInputStream = new FileInputStream(tempOrgXml);
	        BufferedInputStream bufferedInputStream = new BufferedInputStream(
	                fileInputStream);
	        FileInputStream fileInputStream1 = new FileInputStream(tempGenXml);
	        BufferedInputStream bufferedInputStream1 = new BufferedInputStream(
	                fileInputStream1);
	        Document sourceDocument = db
	        .parse(new InputSource(bufferedInputStream));
	        Document destinationDocument = db.parse(new InputSource(
	                bufferedInputStream1));
	        sourceDocument.normalizeDocument();
	        destinationDocument.normalizeDocument();
	        fileInputStream.close();
	        fileInputStream1.close();
	        bufferedInputStream.close();
	        bufferedInputStream1.close();
	        NodeList sourceEntryList = (NodeList) sourceDocument
	        .getElementsByTagName("entry");

	        for (int srcIndex = 0; srcIndex < sourceEntryList.getLength(); srcIndex++) {
	            sourceEntry.add(sourceEntryList.item(srcIndex));
	        }
	        NodeList destinationEntryList = (NodeList) destinationDocument
	        .getElementsByTagName("entry");
	        for (int destIndex = 0; destIndex < destinationEntryList.getLength(); destIndex++) {
	            destinationEntry.add(destinationEntryList.item(destIndex));
	        }

	        for (int sourceEntryIndex = 0; sourceEntryIndex < sourceEntry.size(); sourceEntryIndex++) {
	            for (int destinationEntryIndex = 0; destinationEntryIndex < destinationEntry
	            .size(); destinationEntryIndex++) {
	                if (!(sourceEntry.get(sourceEntryIndex)
	                        .isEqualNode(destinationEntry
	                                .get(destinationEntryIndex)))) {
	                    isNodeEqual = false;
	                    continue;
	                } else {
	                    isNodeEqual = true;
	                    break;
	                }
	            }
	            if (!isNodeEqual) {
	                break;
	            }
	        } 


	        return isNodeEqual;
	    }


	  /**
	    * helper method to update the agentname, agentprocess and flinenumber 
	    * @param xmlLoc
	    * @param newXmlLoc
	    * @return
	    * @throws Exception
	    */
	    private static boolean filterXml(String xmlLoc, String newXmlLoc) throws Exception{
	        boolean isXmlFiltered = true;
	        
	        Document orginalXmlDoc = RegressionBaseAgentUtil.getDocument(xmlLoc);

	        NodeList fLineNumberLst = orginalXmlDoc.getElementsByTagName("fLineNumber"); 
	        for (int i = 0; i < fLineNumberLst.getLength(); i++) {
	            Node node = fLineNumberLst.item(i);
	            String nodeCont = node.getTextContent();
	            int flineCount = Integer.parseInt(nodeCont);
	            if(flineCount < 0){
	                System.out.println("%%%%%%%%%%%%% FLINE COUNT IS LESS THAN ZERO %%%%%%%%%%%%%%%");
	                isXmlFiltered = false;
	                return isXmlFiltered;
	            }else{
	                node.setTextContent("xyz");
	            }
	        }

	        NodeList strNodeLst = orginalXmlDoc.getElementsByTagName("string");
	        for (int i = 0; i < strNodeLst.getLength(); i++) {
	            Node node = strNodeLst.item(i);
	            String nodeCont = node.getTextContent();
	            if( nodeCont!=null ){
	                String[] cntLst = nodeCont.split("\\|");
	                String newContent = "";
	                //skipping machine name 
	                for(int j=1; j<cntLst.length; j++){
	                    newContent = newContent+cntLst[j]+"|";
	                }
	                node.setTextContent(newContent);
	            }
	        }
			  NodeList detMsgLst = orginalXmlDoc.getElementsByTagName("detailMessage"); 
			  if(detMsgLst!=null && detMsgLst.getLength()>0)
		      {
			  System.out.println("detMsgLst [detailMessage] is NOT EMPTY");	   	   
			   for (int i = 0; i < detMsgLst.getLength(); i++) {
				Node node = detMsgLst.item(i);
				String nodeCont = node.getTextContent();
			   
			     if(nodeCont.contains("Database")){
				StringBuilder sb = new StringBuilder();
		String[] s = nodeCont.split(" ");
		for(int j=0; j<s.length;j++){
			if(!s[j].contains("Database")){
				sb.append(s[j]);	
			}
		}
		node.setTextContent(sb.toString());
	}		
	       }
		   }
		    else
		   {
			System.out.println("detMsgLst [detailMessage] is EMPTY");	   	   
		   }	   


	        NodeList traceLst = orginalXmlDoc.getElementsByTagName("trace");
	        for (int i = 0; i < traceLst.getLength(); i++) {
	            Node node = traceLst.item(i);
	            String nodeCont = node.getTextContent();
	            int colonIndex = nodeCont.lastIndexOf(".java:");
	            if( nodeCont!=null && colonIndex!=-1){
	                String newContent  = nodeCont.substring(0, colonIndex);
	                node.setTextContent(newContent+")");
	            }
	        }



	        RegressionBaseAgentUtil.writeToXMLFile(orginalXmlDoc, newXmlLoc);
	        return isXmlFiltered;
	    }

	    /**
	     * Helper method to stop the agent
	     * @param agntName
	     * @return
	     * @throws Exception
	     */
	    public static boolean stopDIAgentHelper(String agntName,String agentHost,CLWBean clw,String agentProcess) throws Exception{
	        boolean isAgentStopped = true;
	       
	        String metricExpr = "*SuperDomain*|" + agentHost
	        + "|"+agentProcess+"|"+agntName+"|ProcessID";

	        System.out.println("metricExpr : " + metricExpr);

	        MetricUtil mu = new MetricUtil(metricExpr, clw);

	        String[] metricValueBefore = mu.getLastNMinutesMetricValues(8);
	        String processId = null;
	        for (String mvb : metricValueBefore) {
	            if (mvb != null) {
	                processId = mvb;
	                break;
	            }
	        }

	        System.out.println("%%%processId%%%% " + processId);
	        Runtime.getRuntime().exec(AutomationConstants.TASK_KILL + processId);

	        System.out.println("Stop DIAgent command executed");

	        Util.sleep(60 * 1000);

	        int i = 0;
	        while (Util.isProcessAvail(processId) && i < 4) {
	            Runtime.getRuntime().exec(AutomationConstants.TASK_KILL + processId);
	            Util.sleep(1 * 60 * 1000);
	            i++;
	        }
	        System.out.println("DIAgent stopped successfully");
	        return isAgentStopped;
	    }   

	    /**
	     * Helper method will check the agent has started completely
	     * @param agntName
	     * @return
	     * @throws InterruptedException
	     */
	    public static boolean checkAgentHelper(String agntName,String agentHost,CLWBean clw,String agentProcess) throws InterruptedException{
	        boolean agentStarted = false;
	        String ipMetric = "*SuperDomain*|" + agentHost + "|" + agentProcess
	        + "|" + agntName + "|Host:IP Address";
	        String vmMetric = "*SuperDomain*|" + agentHost + "|" + agentProcess
	        + "|" + agntName + "|Heuristics:VM";

	        MetricUtil ipMetricUtil = new MetricUtil(ipMetric, clw);
	        MetricUtil vmMetricUtil = new MetricUtil(vmMetric, clw);

	        boolean isIpMetricExists = false;
	        boolean isVmMeticExists = false;
	        int elapsedInterval = 0;
	        int chkInterval = 4 * 60 * 1000;
	        while (!isIpMetricExists || !isVmMeticExists) {
	            isIpMetricExists = ipMetricUtil.metricExists();
	            isVmMeticExists = vmMetricUtil.metricExists();

	            if (chkInterval == elapsedInterval)
	            {
	                break;
	            }
	            Thread.sleep(1*60*1000);
	            elapsedInterval = elapsedInterval + (1*60*1000);

	        }

	        isIpMetricExists = ipMetricUtil.metricExists();
	        isVmMeticExists = vmMetricUtil.metricExists();
	        if(isIpMetricExists && isVmMeticExists){
	            System.out.println("%% TestAgent Started   %%");
	            agentStarted = true;
	        }
	        return agentStarted;
	    }

	    
	    /**
	     * Helper method which delete complete folder
	     */
	    public static boolean deleteFolder(String location) throws Exception{
	        boolean success = false;
	            File file = new File(location);
	            
	            if(!file.exists()){
	                success = true;
	                return success;
	            }

	            File[] files = file.listFiles(); 
	            if( files == null || files.length==0){
	                success = true;
	                return success;
	            }
	            
	            for (File file1 : files) 
	            { 
	                System.out.println("--file---"+file1);
	                success = file1.delete();   
	            }

	        return success;
	    }
	    
	    /**
	 * This method is to delete the Directory
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static boolean deleteDir(File dir) throws IOException {
		boolean deleted = false;
		if (!dir.isDirectory()) {
			return deleted;
		}

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (file.isDirectory()) {
				//System.out.println("Deleting --" + file.getName());
				deleteDir(file);
			} else {
				deleted = file.delete();
				//System.out.println("Deleting --" + file.getName());
				if (!deleted) {
					// throw new IOException("Unable to delete file" + file);
					return deleted;
				}
			}
		}

		return dir.delete();
	}

}
