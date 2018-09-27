/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */

package com.ca.apm.commons.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.io.LineNumberReader;

import com.jcraft.jsch.*;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.Util;

/**
 * @author jamsa07
 *
 */
public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);
    public static String platform = System.getProperty("os.name");
    private final String line_sep = "@@";

    public void connectToURL(String urlToHit, int timesToHit) {

        try {
            URL url = new URL(urlToHit);
            URLConnection myURLConnection = url.openConnection();
            myURLConnection.connect();

            for (int i = 0; i < timesToHit; i++) {

                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    LOGGER.info(inputLine);
            }
        } catch (MalformedURLException e) {
            LOGGER.info("MalformedURLException");
        } catch (IOException e) {
            LOGGER.info("IOException");
        }
    }

    /**
     * 
     * @param host
     * @return
     */
    public String getRemoteTime(String host) {
        String cmd = "cmd.exe /c net time \\\\" + host;
        String str = execWindowsCmd(cmd, null).split("is")[1];
        str = str.split("The")[0];

        return str;
    }

    /**
     * 
     * @param cmd
     * @param workingDir
     * @return
     */
    public String execWindowsCmd(String cmd, String workingDir) {
        System.out.println("Running command: " + cmd);
        String execOutput = null;

        /*CommandLine cmdLine = CommandLine.parse(cmd);
        Executor executor = new DefaultExecutor();
        if (StringUtils.isNotEmpty(workingDir)) {
            executor.setWorkingDirectory(new File(workingDir));
        }
        ByteArrayOutputStream execOutput = new ByteArrayOutputStream(512);
        PumpStreamHandler streamHandler =
            new PumpStreamHandler(new TeeOutputStream(System.out, execOutput), System.err);
        executor.setStreamHandler(streamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        try {
            executor.setExitValue(0);
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                "Unable to execute command \"%s\" from %s!", cmd, workingDir), e);
        }

        if (resultHandler.getException() != null) {
            throw new IllegalStateException(String.format(
                "Execution of command \"%s\" finished with error.", cmd),
                resultHandler.getException());
        }

        return execOutput.toString();*/
        try{
        Process subprocess = ApmbaseUtil.runCommand(Arrays.asList(cmd), workingDir);
        long processStartTime = System.currentTimeMillis();
        LOGGER.info("Process started at " + Long.toString(processStartTime));
        try (InputStream inputStream = subprocess.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {

          while ((isAlive(subprocess) && isBeddingIn(processStartTime)) || br.ready()) {
            while (br.ready()) {
              execOutput = execOutput + br.readLine();
            }
            Thread.sleep(500);
          }

          if (isAlive(subprocess)) {
            LOGGER.debug("Subprocess bedded in, returning success.");
            
          }
          else {
            LOGGER.debug("Subprocess no-longer running after %d seconds, returning failure.", (System.currentTimeMillis() - processStartTime) /1000);
          }

      } catch (IOException e) {
        LOGGER.error("IOException", e);
      }}
        catch(Exception e)
        {
            LOGGER.error("IOException", e);
        }
        
        return execOutput;
    }
    
    private boolean isAlive(Process p) {
        try {
          p.exitValue();
          return false;
        } catch (IllegalThreadStateException e) {
          return true;
        }
      }

      private boolean isBeddingIn(long processStartTime) {
        return System.currentTimeMillis() < (processStartTime + (10 * 1000));
      }

    
    
    public String execUnixCmd(String hostname, int hostPort, String username, String password, String[] commands) throws Exception
    {
        StringBuilder output= new StringBuilder();
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, hostPort);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        LOGGER.info("Connected");

        ChannelExec channel = (ChannelExec) session.openChannel("exec");

        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        StringBuilder sb= new StringBuilder();
        for (String s : commands)
        {
            if (commands.length>1)
            sb.append(s + ';');
            else
                sb.append(s);
        }
        String command = sb.toString();
        System.out.println("Executing Commands "+command);
        channel.setCommand(command);
        channel.connect();

        String msg = null;
        while ((msg = in.readLine()) != null)
        {
            LOGGER.info(msg);
            output.append(msg+":::");
        }
        System.out.println(output);
        channel.disconnect();
        session.disconnect();
        LOGGER.info("DONE");
        return output.toString();
    }    
    
 /**
     * Checks if the port is busy by creating socket and checking for success.
     * 
     * @throws InterruptedException
     * 
     */
    public static void waitTillPortIsBusy(int port, Long timeout) throws InterruptedException {
        Long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            try (Socket socket = new Socket("localhost", port);) {
                System.out.println("Port " + port + " took " + (System.currentTimeMillis() - start)
                    + " msecs to become busy");
                return;
            } catch (IOException e) {
                long elapsed = (System.currentTimeMillis() - start) / 1000;
                System.out.println("Port " + port + " is not busy yet. Time elapsed[s] : " + elapsed);
                Thread.sleep(5 * 1000);
            }
        }
        throw new RuntimeException("Port " + port + " is not busy after " + timeout + " msecs");
    }

    /**
	* replaces content in file
	*/
	public boolean replaceContentInFile(String file, String oldText, String newText) {

        File f = new File(file);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;
        FileWriter fstream = null;
        BufferedWriter outobj = null;
        StringBuffer sb = new StringBuffer();
        String line = null;
        boolean success = false;
        try {
            fs = new FileInputStream(f);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while ((line = br.readLine()) != null) {

                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            int index = sb.indexOf(oldText);
            line = sb.toString();
            if (index > 0) {
                line = line.replace(oldText, newText);
                System.out.println("All occurrences of String " + oldText + " replaced with "
                    + newText + " successfully in file " + file);
            } else
                System.out.println("Nothing changed in file " + file);
            fs.close();
            in.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            fstream = new FileWriter(f);
            outobj = new BufferedWriter(fstream);
            outobj.write(line);
            success = true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                outobj.close();
                fstream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

	
	/**
     * 
     * @param cmd
     * @param workingDir
     * @return
     */
    public String execPumpStreamWindowsCmd(String cmd, String workingDir) {
        System.out.println("Running command: " + cmd);

        CommandLine cmdLine = CommandLine.parse(cmd);
        Executor executor = new DefaultExecutor();
        if (StringUtils.isNotEmpty(workingDir)) {
            executor.setWorkingDirectory(new File(workingDir));
        }
        ByteArrayOutputStream execOutput = new ByteArrayOutputStream(512);
        PumpStreamHandler streamHandler =
            new PumpStreamHandler(new TeeOutputStream(System.out, execOutput), System.err);
        executor.setStreamHandler(streamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        try {
            executor.setExitValue(0);
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                "Unable to execute command \"%s\" from %s!", cmd, workingDir), e);
        }

        if (resultHandler.getException() != null) {
            throw new IllegalStateException(String.format(
                "Execution of command \"%s\" finished with error.", cmd),
                resultHandler.getException());
        }

        return execOutput.toString();
    }

    public void copyToRemoteMachine(String hostname, String username, String password, String src, String dest)
    {
        LOGGER.info("SRC is "+src);
        LOGGER.info("DEST is "+dest);
        try{
            JSch jsch = new JSch();
            Session session = null;
            session = jsch.getSession(username,hostname,22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            ChannelSftp channel = null;
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            File localFile = new File(src);
            //If you want you can change the directory using the following line.
            channel.cd(dest);
            channel.put(new FileInputStream(localFile),localFile.getName());
            channel.disconnect();
            session.disconnect();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String returnIPforGivenHost(String hostName) throws IOException {
        Util.sleep(6000);
        List<String> returnValues = new ArrayList<String>();
        List<String> commandFormation = new ArrayList<String>();
        returnValues.clear();
        commandFormation.clear();

        LOGGER.info("The given Command to get IP/HostName is " + commandFormation);

        if (platform.toUpperCase().contains("WINDOWS")) {
            commandFormation.add(0, "cmd");
            commandFormation.add(1, "/c");
            commandFormation.add("nslookup " + hostName + "|findstr \"Address:\"");
        } else if (platform.toUpperCase().contains("LINUX")) {
            commandFormation.add(0, "sh");
            commandFormation.add(1, "-c");
            commandFormation.add("nslookup " + hostName + "|grep \"Address:\"");
        }
        LOGGER.info("Running these list" + commandFormation);
        ProcessBuilder processBuilder = new ProcessBuilder(commandFormation);
        processBuilder.directory(new File("/"));
        Process process;
        try {
            process = processBuilder.start();
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
                returnValues.add(line);
            }

        } catch (IOException e) {
            LOGGER.info("Unable to Execute the given command" + commandFormation);
            e.printStackTrace();
        }

        LOGGER.info("The output of the Command is" + returnValues);
        String[] str = returnValues.get(1).split(":");

        return str[1].trim();
    }
    
    /**
     * This method is used to check if a list(mainlist) contains all the elements 
     * of another list (subsetlist)
     * 
     * @param subsetlist
     * @param mainlist
     * @return
     */
    public boolean containsList(List<String> subsetlist, List<String> mainlist) {
    	LOGGER.info("Inside containsLists");
    	boolean foundVal = true;
		for (String subsetlistelement : subsetlist) {
    	if (!(mainlist.contains(subsetlistelement))) {
    	foundVal = false;
        break;
    	}    	
		}
		return foundVal;
    }
    
    /**
	 * This method will read the contents of a file from the bottom  and
	 * stores it in an ArrayList
	 * 
	 * @param fileName
	 * @param errorMsg
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> readFilefromLast(String fileName) throws Exception {
		File file = new File(fileName);
		String completeString = null;
		ArrayList<String> completeStringLst = new ArrayList<String>();
		System.out.println("in fileName " + fileName + " to CHECK " + "");
		RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
		long fileLength = file.length() - 1;
		StringBuilder sb = new StringBuilder();		

		for (long filePointer = fileLength; filePointer != -1; filePointer--) {
			fileHandler.seek(filePointer);

			int readByte = fileHandler.readByte();
			if (readByte == 0xA) {				
				if (filePointer == fileLength) {
					continue;
				} else {					
					sb.append((char) readByte);
					completeString = sb.reverse().toString();
					System.out.println("LINES " + completeString);
					completeStringLst.add(completeString);
					sb = new StringBuilder();
				}
			}
			sb.append((char) readByte);
		}

		return completeStringLst;
	}
	
	
	/**
	 * Returns the last line in the file specified containing the given string.
	 * @param filePath
	 * @param substr
	 * @return
	 */
	public String lineWithSubString(String filePath, String substr){

		String linewithGivenString = null;
				
		try {
			//Get the latest EM list entry in the file
			ArrayList<String> tempFileContents = readFilefromLast(filePath);
//			System.out.println(tempFileContents);
			for (String temp : tempFileContents){
				 if (temp.contains(substr)){
					 LOGGER.info("Line from log is ##" + temp);
					 linewithGivenString = temp;
					 break;
				 }
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			linewithGivenString = null;
			LOGGER.info("Exception while reading the file, returning NULL string");
			e.printStackTrace();
		}
		
		return linewithGivenString;
	}
	/**
	 * This method will load the file , fetches the property value and adds additional arguments to that value
	 * 
	 * @param filename - filename whose property needs to be changed
	 * @param fileDir - directory path where the file is present
	 * @param property - property present in the file
	 * @param argument - argument value which should be appended to the property
	 * @return
	 * @throws Exception
	 */
	public void setadditionalargtoproperty(String filename, String fileDir, String property,String argument) {
    	try
        {
            LOGGER.info("Invoking setadditionalargtoproperty");            
            Properties properties = new Properties();
            properties = loadPropertiesFile(fileDir + "/" + filename);
            String actualpropertyVal = properties.getProperty(property);
            String newpropertyvalue = actualpropertyVal + " " + argument;
            LOGGER.info("Value to update in File:" + newpropertyvalue);
            ApmbaseUtil.setproperties(filename,fileDir+ "/",property,newpropertyvalue);            
       
        } catch (Exception e)
        {
            LOGGER.error("setadditionalargtoproperty failed with Exception : " + e.getMessage());            
        }
    }
    
    public Properties loadPropertiesFile(String fileName) throws Exception {

		Properties properties = new Properties();
		InputStream propsFile = new FileInputStream(fileName);

		try {
			properties.load(propsFile);
		} finally {
			propsFile.close();
		}

		return properties;
	}    
    /**
     * This method will search for list of files in a given directory and returns true if all found 
     * 
     * @param fileNames
     * @param directoryPath
     * @return boolean
     */ 
    public boolean isGivenListOfFilesFound(List<String> fileNames, String directoryPath) {

        int count = 0;
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                for (String fName : fileNames) {
                    if (file.getName().equalsIgnoreCase(fName)) count++;
                }
                System.out.println(file.getName());
            }
        }
        LOGGER.info("Found the Number of Files...." + count + " The size of the List is "
            + fileNames.size());
        if (count == fileNames.size())
            return true;
        else
            return false;
    }

    /**
     * This method will read the contents of a file  and returns the size of the file in KB
     * 
     * @param fileName
     * @param diretoryPath
     * @return double
     */
    public double getFileSizeinKB(String fileName, String directoryPath) {
        File file = new File(directoryPath + fileName);

        if (file.exists()) {

            double bytes = file.length();
            double kilobytes = (bytes / 1024);
            LOGGER.info("The size of the given file is...." + kilobytes);
            return Math.round(kilobytes);
        } else {
            LOGGER.info("File not found...." + fileName);
            Assert.assertTrue(false);
        }
        return 0;

    }

    /**
     * This method will read the contents of a file and and returns the numberof Lines in it
     * 
     * @param fileName
     * @return
     */
    public int returnLineCountOfGivenFile(String filename) throws IOException {
        LOGGER.info("The filename to get the count of lines is "+filename);
        
        LineNumberReader reader = new LineNumberReader(new FileReader(filename));
        int cnt = 0;
        String lineRead = "";
        while ((lineRead = reader.readLine()) != null) {}

        cnt = reader.getLineNumber();
        reader.close();
        return cnt;
    }

    /**
     * This common method is used to convert given string to a file
     * 
     * @param fileContent
     *        -holds the content which needs to be converted to file
     * @param fileName
     *        -holds the file name in which given string needs to be written
     * @param append
     *        -holds true/false, if true is passed content will be appended
     *        to a existing file else content will be written in a new file
     * @return boolean value - returns true if string is converted to file
     *         successfully else returns false
     */
    public boolean convertStringToFile(String fileContent, String fileName, String append) {
        LOGGER.info("inside convertStringToFile [begin]");
        LOGGER.info("fileContent to " + fileContent);
        LOGGER.info("fileContent to " + fileName);
        LOGGER.info("append to " + append);
        boolean isConverted = false;
        boolean appendRequired = Boolean.valueOf(append);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(fileName, appendRequired));
            String messages[] = fileContent.split(line_sep);
            for (int j = 0; j < messages.length; j++) {
                out.write(messages[j]);
                out.newLine();
            }
            out.flush();
            LOGGER.info("convert String To File COMPLETED ");
            isConverted = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            isConverted = false;
            LOGGER.error("Exception in  method convert String To File 1 : " + ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("Exception in  method convert String To File 2 : " + e);
                }
            }
        }
        LOGGER.info("inside convertStringToFile [end]");
        return isConverted;

    }

    /**
     * This common method is used to convert given file to a string
     * 
     * @param filename
     *        -holds the name of the file which needs to be converted to a
     *        string
     * @return String value returns the content as a string
     * 
     */
    public String convertFileToString(String filename) {
        LOGGER.info("inside convertFileToString [begin]");
        LOGGER.info("===========filename========" + filename);
        BufferedReader br = null;

        String result = null;
        try {
            File f = new File(filename);
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                sb.append(line);
                sb.append(line_sep);
            }
            LOGGER.info("total is  : " + sb);
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception in  method convertFileToString 1: " + e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("Exception in  method convertFileToString 2: " + e);
                }
            }
        }
        LOGGER.info("inside convertFileToString [end]  result =" + result);
        return result;
    }   
    
    public List<String> runCmd(String cmd, String dirLoc) throws Exception {
        List<String> results = new ArrayList<String>();
        LOGGER.info("About to run command " + cmd);
        ProcessBuilder pb;

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            pb = new ProcessBuilder("cmd.exe", "/C", cmd);
            pb.directory(new File(dirLoc));
        } else {
            pb = new ProcessBuilder("bash", "-c", cmd);
            pb.directory(new File(dirLoc));
        }
        pb.redirectErrorStream(true);
        Process subprocess = pb.start();
        InputStream inputStream = subprocess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line;
        while ((line = br.readLine()) != null) {
            LOGGER.info("" + line);
            results.add(line);
        }

        return results;
    }
    
    /*
     * Changes a property value with an absolute path in a file 
     */
    
    public void changePropValwithAbsolutePath(String propertyname,String filepath,String fileloc,String filename) {
    	try
    	{    		
    		File path = new File(filepath);
    		path.getParentFile().mkdirs();
    		int result = ApmbaseUtil
    		.setproperties(filename,fileloc,
    				propertyname, path.getAbsolutePath().replace('\\', '/'));    		
    		Assert.assertTrue(result==1, "Failed setting a property value");
    	} catch (Exception e)
    	{
    		//e.printStackTrace();
    		Assert.fail("changePropValwithAbsolutePath failed because of the Exception "+e);
    	}
    }   
	
}
