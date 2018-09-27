package com.ca.apm.tests.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.ca.apm.commons.coda.common.ApmbaseUtil;

public class Utility
{

    public static String platform = System.getProperty("os.name");
    private static Logger LOGGER = Logger.getLogger(Utility.class);

    public void execCommand(String command, String WorkingDir){
        try
        {
            ApmbaseUtil.runCommand(Arrays.asList(command), WorkingDir);             //this method is from APM-COMMONS project
        } catch (IOException e)
        {        }
    }

    public boolean verifyCommandOutput(Process p, String message){
        String line;
        Boolean flag = false;
        try
        {
        LOGGER.info("Looking for message : "+message);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = br.readLine()) != null) {
            LOGGER.info(line);
            if(line.contains(message)){
                flag=true;
            }

        }

        } catch (IOException e)
        {        } 
        return flag;
    }
    
    public boolean verifyCommandOutput(Process p, List<String> compareStrings){
        String line;
        Boolean flag = false;
        try
        {
            LOGGER.info("Checking out messages--------------");
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = br.readLine()) != null) {
            LOGGER.info(line);
            for (String Msg : compareStrings) {
                if (line.contains(Msg)) {
                    LOGGER.info("Found : " + Msg);
                    compareStrings.remove(Msg);
                    break;
                }
            }

        }
        if (compareStrings.size() == 0) flag = true;

        } catch (IOException e)
        {        } 
        return flag;
    }

    /**
     * This method is to check the List of messages in file
     * 
     * @param compareStrings: filename,propertyName,propertyOldValue, propertyNewValue
     * @param file
     * @return
     * @throws InterruptedException
     */
    public boolean checkMessages(String reportFileName, String modifiedFile, String propertyName, String oldValue, String newValue) throws InterruptedException {


        LOGGER.info("Checking out messages--------------");
        int found = 0;
        DataInputStream in = null;
        FileInputStream fstream = null;
        BufferedReader br = null;
        DataInputStream in1 = null;
        FileInputStream fstream1 = null;
        BufferedReader br1 = null;
        int lineNumber=0;
        int FileNameLine = 0;
        int propertyLineNumber = 0;
        int defaultValueLineNumber=0;
        int customValueLineNumber=0;
        int vFileNameLine = 0;
        int vlineNumber = 0;
        boolean flag =false;

        try {
            fstream = new FileInputStream(reportFileName);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
            String str;
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if (str.contains(modifiedFile)) {
                    FileNameLine=lineNumber;
                    LOGGER.info("Identified that "+modifiedFile+" file is modified. Line entry : "+FileNameLine);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                System.out.println(lineNumber+" : "+str);

                if (str.contains(propertyName)) {
                    propertyLineNumber=lineNumber;
                    LOGGER.info("Identified that "+propertyName+" property is modified. Line entry : "+propertyLineNumber);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if(oldValue.equals(null)){
                    break;
                }
                if (str.contains(oldValue)) {
                    defaultValueLineNumber=lineNumber;
                    LOGGER.info("Identified base value: "+oldValue+". Line entry : "+defaultValueLineNumber);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if(newValue.equals(null)){
                    break;
                }
                if (str.contains(newValue)) {
                    customValueLineNumber=lineNumber;
                    LOGGER.info("Identified modified value: "+newValue+". Line entry : "+customValueLineNumber);
                    break;
                }
            }

            fstream1 = new FileInputStream(reportFileName);
            in1 = new DataInputStream(fstream1);
            br1 = new BufferedReader(new InputStreamReader(in1));
            String str1;
            while ((str1 = br1.readLine()) != null) {
                vlineNumber++;
                if(vlineNumber<FileNameLine){
                    continue;
                }
                if(vlineNumber==FileNameLine){
                    vFileNameLine=vlineNumber;
                }

                else if (str1.contains("--- ")) {  // get line number for next line having file name
                    vFileNameLine=vlineNumber;
                    System.out.println(vFileNameLine);
                    LOGGER.info("Identified next file name line: "+vFileNameLine+" . Line entry : "+vFileNameLine);
                    break;
                }
            }

            //final  verification
            if(vFileNameLine!=FileNameLine&&(vFileNameLine==0||vFileNameLine<customValueLineNumber)){
                flag = false;
                return flag;
            }
            else {
                flag=true;
                return flag;
            }



        } catch (Exception e) {        } 
        finally {
            try {
                fstream.close();
                in.close();
                br.close();
                fstream1.close();
                in1.close();
                br1.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return flag;

    }
    
    /**
     * This method is to check the List of messages in file
     * 
     * @param checkXmlMessages: filename,parentNode,actual value,modified value
     * @return boolean : true if match found
     * @throws InterruptedException
     */
    public static boolean checkXmlMessages(String reportFileName, String modifiedFile, String parentNode, String oldValue, String newValue) throws InterruptedException {


        LOGGER.info("Checking out xml messages--------------");
        DataInputStream in = null;
        FileInputStream fstream = null;
        BufferedReader br = null;
        DataInputStream in1 = null;
        FileInputStream fstream1 = null;
        BufferedReader br1 = null;
        int lineNumber=0;
        int FileNameLine = 0;
        int parentNodeLineNumber = 0;
        int defaultValueLineNumber=0;
        int customValueLineNumber=0;
        int vFileNameLine = 0;
        int vlineNumber = 0;
        boolean flag =false;

        try {
            fstream = new FileInputStream(reportFileName);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
            String str;
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if (str.contains(modifiedFile)) {
                    FileNameLine=lineNumber;
                    LOGGER.info("Identified that "+modifiedFile+" file is modified. Line entry : "+FileNameLine);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if(parentNode.equals(null)){
                    break;
                }
                System.out.println("++++");
                System.out.println(lineNumber+" : "+str);
                if (str.contains(parentNode)) {
                    parentNodeLineNumber=lineNumber;
                    LOGGER.info("Identified that "+parentNode+" property is modified. Line entry : "+parentNodeLineNumber);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if(oldValue.equals(null)){
                    break;
                }
                if (str.contains(oldValue)) {
                    defaultValueLineNumber=lineNumber;
                    LOGGER.info("Identified base value: "+oldValue+". Line entry : "+defaultValueLineNumber);
                    break;
                }
            }
            while ((str = br.readLine()) != null) {
                lineNumber++;
                if(newValue.equals(null)){
                    break;
                }
                if (str.contains(newValue)) {
                    customValueLineNumber=lineNumber;
                    LOGGER.info("Identified modified value: "+newValue+". Line entry : "+customValueLineNumber);
                    break;
                }
            }

            fstream1 = new FileInputStream(reportFileName);
            in1 = new DataInputStream(fstream1);
            br1 = new BufferedReader(new InputStreamReader(in1));
            String str1;
            while ((str1 = br1.readLine()) != null) {
                vlineNumber++;
                if(vlineNumber<FileNameLine){
                    continue;
                }
                if(vlineNumber==FileNameLine){
                    vFileNameLine=vlineNumber;
                }
                else if (str1.contains("--- ")) {  // get line number for next line having file name
                    vFileNameLine=vlineNumber;
                }
            }
            LOGGER.info("Identified last file name entry : "+vFileNameLine+" . Line entry : "+vFileNameLine);

            if(vFileNameLine!=FileNameLine&&(vFileNameLine==0||vFileNameLine<customValueLineNumber)){
                flag = false;
                return flag;
            }
            else {
                flag=true;
                return flag;
            }
        } catch (Exception e) {        } 
        finally {
            try {
                fstream.close();
                in.close();
                br.close();
                fstream1.close();
                in1.close();
                br1.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void updateLine(String fileName, String toUpdate, String updated) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        String line;
        String input = "";

        while ((line = file.readLine()) != null)
            input += line + "\n";
        input = input.replace(toUpdate, updated);

        FileOutputStream os = new FileOutputStream(fileName);
        os.write(input.getBytes());

        file.close();
        os.close();
    }
   

}
