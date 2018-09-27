package com.ca.apm.tests.utility;

/**
 * Common utility methods to use in Automation of APM tests
 * @author: Suresh Votla (votsu01)
 */
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.test.JBaseTest;
import com.gargoylesoftware.htmlunit.WebClient;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.exec.*;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class QaUtils extends JBaseTest{
    

    /**
     * @return Local machine IP address as a string
     */
    public String getLocalMachineIPAddress() {
        InetAddress addr;
        String ipAddr = null ;
        try {
            addr = InetAddress.getLocalHost();
            ipAddr = addr.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return ipAddr.toString();
    }
    
    /**
     * @return Local machine hostname as a string
     */
    public String getLocalMachineHostName(){
        try{
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            return hostname.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }        
    }
    
    /**
     * Checks whether the given port is open and listening for incoming connections
     * @param host
     * @param port
     * @return  true if port is opened
     */
    public boolean isListeningOnPort(String host, int port)
    {
        Socket s = null;
        boolean isListening = false;
        try
        {
            s = new Socket(host, port);
            isListening = s.isBound();
            if (s != null) s.close();

        } catch (Exception e)
        {
            //e.printStackTrace();
            System.out.println("Not listening on Port: "+port);
            isListening = false;
            
        }
        return isListening;

}
    
	/**
	 * Converts given ipv4 String from Decimal format to dotted decimal format
	 * @param ipDecimal - IP address in decimal format. ex: 176557087
	 * @return the equivalent String of Dotted decimal format. ex: 10.134.12.33
	 */
	public String decToIPv4String(String ipDecimal){
		String ipBinary = Long.toBinaryString(Long.parseLong(ipDecimal));
		int lengthOfBinaryIP = ipBinary.length();
		String ipv4String=Integer.valueOf(ipBinary.substring(0, lengthOfBinaryIP-24),2).toString()+
                    "."+Integer.valueOf(ipBinary.substring(lengthOfBinaryIP-24,lengthOfBinaryIP-16),2).toString()+
                    "."+Integer.valueOf(ipBinary.substring(lengthOfBinaryIP-16,lengthOfBinaryIP-8),2).toString()+
                    "."+Integer.valueOf(ipBinary.substring(lengthOfBinaryIP-8,lengthOfBinaryIP),2).toString();
		
		return ipv4String;
	}
	/**
	 * To execute Unix commands remotely(on tim/tess), output of command is also captured returned as a String
	 * @param host
	 * @param user
	 * @param pwd
	 * @param cmd
	 * @return the output of the command 
	 */
	public String execUnixCmd(String host,String user, String pwd, String cmd){
	    
	    String result="";
	    System.out.println("Executing remote command on "+host+": "+cmd);
	    try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pwd);  
            session.connect();
            Channel channel = session.openChannel("exec");
			
			if(cmd.contains("&&")){
            	String commands[] = cmd.split("&&");
            	for(String c:commands){
            		((ChannelExec) channel).setCommand(c);
            	}
            		
            }
            else	
            	((ChannelExec) channel).setCommand(cmd);
            
            channel.setInputStream(System.in);
            //channel.setInputStream(null);
            channel.setOutputStream(System.out);
            InputStream in=channel.getInputStream();
            channel.connect();            
            while(true){
                int l=in.available();
              if(l>0){
                  //System.out.println(l);
                  byte[] tmp=new byte[l];
                  int i=in.read(tmp);
                  if(i<0)
                      break;
                    result =new String(tmp, 0, i);                               
                  }
                if(channel.isClosed()){
                    //System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                  }
                Thread.sleep(1000);                           
            }
            channel.disconnect();
            session.disconnect();
            }
        catch(Exception e){
            System.out.println(e);
        }          
        //System.out.print(result);   
	    return result.trim();
	}
	
	/**
	 * Executes the CLWorkstation.jar on the given host, port and retrieve the metric data
	 * @param host : CLWorkstation will be connected to this host
	 * @param port : CLWorkstation will be connected to this port on the given host
	 * @param metricPath : path of the metric that need to be retrieved
	 * @return Output of CLWorkstation after executing the given command as a string
	 */
	public String execCmdCLWorkstation(String host, String port, String cmd){

        return execCmdCLWorkstation(CLW_JAR, host, port, cmd);       
    }
	
	/**
     * Executes the CLWorkstation.jar on the given host, port and retrieve the metric data
     * @param host : CLWorkstation will be connected to this host
     * @param port : CLWorkstation will be connected to this port on the given host
     * @param metricPath : path of the metric that need to be retrieved
     * @return Output of CLWorkstation after executing the given command as a string
     */
    public String execCmdCLWorkstation(String clwJar, String host, String port, String cmd){
        return execCmd("java -Dhost="+host+" -Dport="+port+" -jar "+clwJar+" "+cmd);
    }
    
    /**
     * Executes the CLWorkstation.jar on the given host, port , timeout and retrieve the metric data
     * @param host : CLWorkstation will be connected to this host
     * @param port : CLWorkstation will be connected to this port on the given host
     * @param timeout : CLWorkstation will wait till the specified timeout
     * @param metricPath : path of the metric that need to be retrieved
     * @return Output of CLWorkstation after executing the given command as a string
     */
	public String execCmdCLWorkstation(String clwJar, String host, String port, String timeout,String cmd){
        return execCmd("java -Dhost="+host+" -Dport="+port+" -Dwily.MessageReplyTimeOut="+timeout+" -jar "+clwJar+" "+cmd);
    }
	
    /**
     * Executes given command 
     * @param cmd   command to execute
     * @return  returns output of the command
     */
    public String execCmd(String cmd){        
        return execCmd(cmd, null);
    }
    
    /**
     * Executes given command 
     * @param cmd   command to execute
     * @param workingDir   Provide working directory to execute command. or Null to use parent
     * @return  returns output of the command
     */
    public String execCmd(String cmd, String workingDir) {
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
            throw new IllegalStateException(format("Unable to execute command \"%s\" from %s!",
                    cmd, workingDir), e);
        }

        if (resultHandler.getException() != null) {
            throw new IllegalStateException(format(
                    "Execution of command \"%s\" finished with error.", cmd),
                    resultHandler.getException());
        }

        return execOutput.toString();
    }
	//chusw01
	/* This method returns processs Object on which the commands can be executed*/
	public static Process getProcess(String[] commands, String dirLoc)
    throws IOException {
    	if (commands == null)
    		return null;
    	String[] execCommandStrings = new String[commands.length + 2];
    	execCommandStrings[0] = "cmd.exe";
    	execCommandStrings[1] = "/c";
    	for (int i = 0; i < commands.length; i++) {
    		execCommandStrings[i + 2] = commands[i];
    	}
    	System.out.println("Starting Command: " + Arrays.toString(execCommandStrings));
    	System.out.println("Working dir: " + dirLoc);
    	ProcessBuilder processBuilder = new ProcessBuilder(execCommandStrings);
    	processBuilder.directory(new File(dirLoc));
    	processBuilder.redirectErrorStream(true);
    	Process process = processBuilder.start();
    	return process;
    }
	//chusw01
	/* This method is to execute the Commands, and result would be captured in txt file,This is used for watchdog which require control giving back to console which uses Thread*/
	public void executeCommandWatch(String[] commands, String dirLoc,String outFile, int timeWait)throws IOException, InterruptedException {
		int found = 0;
		int mWait = 18 * 10000;
		int tWait = 0;
		Process process = getProcess(commands, dirLoc);
		if (process == null)
			return;
		try {
			File file = new File(outFile);
			if (!file.exists())
				file.createNewFile();

			FileOutputStream fos = new FileOutputStream(file);
			OutputConsoleThread outPust = new OutputConsoleThread(process.getInputStream(), fos);
			Thread th = new Thread(outPust);
			th.start();
			while (found == 0) {
				if (mWait == tWait) {
					break;
				}
				Thread.sleep(tWait + 30000);
				tWait = tWait + 30000;

			}

		} finally {
			process.destroy();
		}
		
	}

    
    /**
     * Executes given command 
     * @param cmd   command to execute
     * @param workingDir   Provide working directory to execute command. or Null to use parent
     * @return  returns error trace of the command
     */
    public String execCmdGetError(String cmd, String workingDir){
        System.out.println("Running command: " + cmd);

        CommandLine cmdLine = CommandLine.parse(cmd);
        Executor executor = new DefaultExecutor();
        if (StringUtils.isNotEmpty(workingDir)) {
            executor.setWorkingDirectory(new File(workingDir));
        }
        ByteArrayOutputStream execErrorOutput = new ByteArrayOutputStream(512);
        PumpStreamHandler streamHandler =
                new PumpStreamHandler(System.out, new TeeOutputStream(System.err, execErrorOutput));
        executor.setStreamHandler(streamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        try {
            executor.setExitValue(0);
            executor.execute(cmdLine, resultHandler);
            resultHandler.waitFor();
        } catch (Exception e) {
            throw new IllegalStateException(format("Unable to execute command \"%s\" from %s!",
                    cmd, workingDir), e);
        }

        if (resultHandler.getException() == null) {
            throw new IllegalStateException(format(
                    "Execution of command \"%s\" finished with success.", cmd));
        }

        return execErrorOutput.toString();
    }
    
    public boolean execCmdNoWait(String cmd, String workingDir){
        try{
            System.out.println("Running command: "+cmd);
            Runtime.getRuntime().exec(cmd, null, workingDir==null?null:new File(workingDir));
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets the value of given metric. If there are any special characters in the path, should use escape characters. Ex: Custom Metric Agent \(Virtual\)
     * @param EM_HOST
     * @param EM_PORT
     * @param EM_USER
     * @param EM_PASSWORD
     * @param CLW_JAR_LOC
     * @param metricPath
     * @return Value of the metric
     */
    public String getMetric(String EM_HOST, String EM_PORT, String EM_USER, String EM_PASSWORD, String CLW_JAR_LOC, String metricPath){
        
        CLWBean clw = new CLWBean(EM_HOST, EM_USER, EM_PASSWORD, Integer.parseInt(EM_PORT), CLW_JAR_LOC);
        MetricUtil metricutil = new MetricUtil(metricPath, clw);
        
        return metricutil.getMetricValue();
    }
    
    /**
     * Gets the value of given metric. 
     * @param EM_HOST
     * @param CLW_JAR_LOC
     * @param metricPath
     * @return Value of the metric
     */
    public String getMetric(String EM_HOST, String CLW_JAR_LOC, String metricPath){
        return getMetric(EM_HOST, EM_PORT, "admin", "", CLW_JAR_LOC, metricPath);
    }
    
    /**
     * To validate a metric exists or not. It checks for metric value >=0.
     * the metric path should be complete. Ex: *SuperDomain*|gamsa03-I52119|WebSphere|WebSphere Agent|EJB|Session:Average Method Invocation Time (ms)
     * this method will take care of special characters in the matric path like '(', ')' etc.
     * @param emHost
     * @param metric    complete metric path
     */
    public void validateMetricExists(String emHost, String metric){
        System.out.println("Verifiying metric "+metric);
        metric = getMetric(emHost, CLW_JAR, formatMetricPath(metric));
        System.out.println("Metric value found: "+metric);
        assertTrue(Long.parseLong(metric) >= 0, "Expected metric value >=0, found: "+metric);
    }
    
    public String formatMetricPath(String metric){
        String prefix = metric.substring(0, metric.indexOf(":"));
        String suffix = metric.substring(metric.indexOf(":"));
        prefix = prefix.replace("(", "\\(");
        prefix = prefix.replace(")", "\\)");
        return prefix+suffix;
    }
    
    /**
     * Convert the path separators to Native platform
     * @return the converted path
     */
    public String convertPathSeparators(String path){
        String delim = System.getProperty("file.separator");
        if(delim.equalsIgnoreCase("\\"))
            return path.replace("/", "\\");
        else
            return path;
        
    }
    
  /* public String convertPathSeparatorsbyCrux(String path){
        String delim = System.getProperty("file.separator");
        if(delim.equalsIgnoreCase("\\"))
            return path.replace("/", "/");
        else
            return path;
        
    }*/
    
    /**
     * Get me the full path to the testdata folder
     * @return the full path to the testdata folder
     */
    public String getTestDataFolderFullPath(){
        return convertPathSeparators(WORKING_DIR+"/testdata/");
    }
    
    /**
     * Get me the full path to the config folder
     * @return the full path to the config folder
     */
    public String getConfigFolderFullPath(){
        return convertPathSeparators(WORKING_DIR+"/config/");
    }
    
    /**
     * Get me the full path to the config folder
     * @param testDataFolder: folder name inside the testdata folder
     * @param testDataFile: name of the file you are looking for
     * @return the full path to the config folder
     */
    public String getTestDataFileFullPath(String testDataFolder, String testDataFile){
        return convertPathSeparators(WORKING_DIR+"/testdata/"+testDataFolder+"/"+testDataFile);
    }
    
    /**
     * Copies a file from remote Windows host to localhost and returns the path to the copied file on localhost
     * @param host Remote Windows hostname/IP
     * @param username Remote windows login username
     * @param pwd Remote windows login password
     * @param remoteFileDir Directory path on the remote Windows host
     * @param fileName Filename to be copied on the above directory
     * @return path to the copied file on localhost
     */
    public String copyFileFromRemoteWindowsHost(String host, String username, String pwd, String remoteFileDir, String fileName){

        try{
            String localFile=getTestDataFolderFullPath()+System.currentTimeMillis()+".txt";
            String cdmPsexec =  getTestDataFileFullPath("Utility","psexec.exe")+" \\\\"+host+" -u "+username+" -p "+pwd+" -e cmd /c ";
            String remoteShareName = "share_automation_data";
            String rTmpDir = "C:\\";
            String rLocalDrive = "M:";
            String rcmdCopy = cdmPsexec + "copy /Y \""+remoteFileDir+"\\"+fileName+"\" \""+rTmpDir+"\"";            
            String rcmdNetShare = cdmPsexec + "\"net share "+remoteShareName+"="+rTmpDir+"\"";
            String cmdNetUse = "net use "+rLocalDrive+" \\\\"+host+"\\"+remoteShareName+" /USER:"+username+" "+pwd;
            String localCmdCopy = "cmd /c copy /Y \""+rLocalDrive+"\\"+fileName+"\" \""+localFile+"\"";
            String deleteMappedDrive = "net use "+rLocalDrive+" /delete";
            System.out.println("remote copy: "+rcmdCopy);
            Process pCmdCopy = Runtime.getRuntime().exec(rcmdCopy);
            Thread.sleep(500L*2*5);
            System.out.println("remote share: "+rcmdNetShare);
            Process pCmdNetShare = Runtime.getRuntime().exec(rcmdNetShare);
            Thread.sleep(500L*2*5);
            System.out.println("local share: "+cmdNetUse);
            Runtime.getRuntime().exec(cmdNetUse);
            Thread.sleep(500L*2*10);
            System.out.println("local copy: "+localCmdCopy);
            Runtime.getRuntime().exec(localCmdCopy);
            Thread.sleep(500L*2*5);
            System.out.println("delete share: "+deleteMappedDrive);
            Runtime.getRuntime().exec(deleteMappedDrive);
            Thread.sleep(500L*2*5);
            pCmdCopy.destroy();
            pCmdNetShare.destroy();
            return localFile;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
        
    }
	
	/**
	 * Search the given array of String elements for the given element
	 * @param searchArray : String array that need to be searched
	 * @param searchElement : Element to be searched
	 * @return : return the index of the element if found, -1 if not found
	 */
	public int getIndexOfElement(String searchArray[], String searchElement){

	    Arrays.sort(searchArray);
        return Arrays.binarySearch(searchArray, searchElement);
    }
	
	/**
	 * To check for a string in the given file
	 * @param pathTofile : Complete path of the file to be searched
	 * @param pattern    : String to be searched
	 * @return           : true if matched string found
	 */
	public boolean checkFileForPattern(String pathTofile, String pattern){      
	    
	    return searchForText(pathTofile, pattern)>0 ? true:false;
    }
	
	public boolean checkLog(String pathTofile, String pattern){
	    return checkFileForPattern(pathTofile, pattern);
	}
	
	   /**
     * To find for a string in the given file
     * @param pathTofile : Complete path of the file to be searched
     * @param pattern    : String to be searched
     * @return           : line number if matched string found, else 0
     */
    public long searchForText(String pathTofile, String pattern){      
        
        return searchForText(pathTofile, pattern, 0);
    }
    
    /**
     * To find for a string in the given file
     * @param pathTofile : Complete path of the file to be searched
     * @param pattern    : String to be searched
     * @param fromLine   : The line number to start searching from
     * @return           : line number if matched string found, else 0
     */
    public long searchForText(String pathTofile, String pattern, long fromLine){      
        
        boolean patternFoundLog = false;
        int line=0;
        //read file
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathTofile))));
            String strLine;
            
            while ((strLine = br.readLine()) != null){
              //check error message
                line++;
                if(line < fromLine)
                    continue;
                if(strLine.contains(pattern)) {
                  patternFoundLog=true;
                  break;
                }
            }
              
            br.close();
            if(patternFoundLog)
            {
                LOGGER.info("The Line number is "+line);
                return line;

            }
            else
                return 0;
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
    * To find for a string in the given file
    * @param pathTofile : Complete path of the file to be searched
    * @param pattern    : String to be searched
    * @return           : the complete line as a string if matched string found, else null
    */
    public String searchForTextReturnsString(String pathTofile, String pattern){      
	       
	       String patternFoundInLine=null;
	       try{
	           BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathTofile))));
	           String strLine;
	           while ((strLine = br.readLine()) != null){
	               if(strLine.contains(pattern)) {
	            	   patternFoundInLine=strLine;
	                 break;
	               }
	           } 
	           br.close();
	           return patternFoundInLine;
	       }
	       catch(Exception e){
	           e.printStackTrace();
	           return null;
	       }
	   }
    
    /**
     * To find for a string in the given file
     * @param pathTofile : Complete path of the file to be searched
     * @param pattern    : Regex pattern to be searched
     * @return           : the complete line as a string if matched string found, else null
     */
    public String searchForRegexReturnsString(String pathTofile, String pattern){      
		Pattern patt = Pattern.compile(pattern);
       String patternFoundInLine=null;
       try{
           BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathTofile))));
           String strLine;
           while ((strLine = br.readLine()) != null){
               if(patt.matcher(strLine).matches()) {
            	   patternFoundInLine=strLine;
                 break;
               }
           } 
           br.close();
           return patternFoundInLine;
       }
       catch(Exception e){
           e.printStackTrace();
           return null;
       }
   }
    
    /**
     * To find for all lines matching particular stringpattern in the given file
     * @param pathTofile : Complete path of the file to be searched
     * @param pattern    : Regex pattern to be searched
     * @return           : the string array containing all the complete lines if matched string found, else null
     */
    public String[] searchForRegexReturnsAllInStringArray(String pathTofile, String pattern){      
		Pattern patt = Pattern.compile(pattern);
		ArrayList<String> list = new ArrayList<String>();
		try{
           BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathTofile))));
           String strLine;
           while ((strLine = br.readLine()) != null){
               if(patt.matcher(strLine).matches()) {
            	   list.add(strLine);
               }
           } 
           br.close();
           if(!list.isEmpty())
           {
        	   String[] patternFoundInLines = list.toArray(new String[list.size()]);
        	   return patternFoundInLines;
           }
       }
       catch(Exception e){
           e.printStackTrace();
           return null;
       }
	   System.out.println("No lines found in file \""+pathTofile+"\" that matched "+pattern);
 	   return null;
   }
    
    /**
     * To delete a file on local filesystem
     * @param completeFilePath
     * @return  true if file deleted successfully
     */
    public boolean deleteFile(String completeFilePath){      
        
        boolean isDeleted = false;
        try{
            File file = new File(completeFilePath);
            if(!file.exists())
                return true;
            if(file.delete())
                System.out.println(completeFilePath + " is deleted successfully.");
            else
                System.out.println("Error in deleting file "+completeFilePath);
            
            isDeleted = true;
        }
        catch(Exception e){
            e.printStackTrace();
            isDeleted = false;
        }
        return isDeleted;
    }
    
    public boolean moveFileToDir(String file, String targetDirectory){
        
        try{
            return FileUtils.move(file, targetDirectory);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
    }
	public boolean copyFileToDir(String file, String targetDirectory){
        
        try{
            FileUtils.copy(new File(file), new File(targetDirectory));
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
    }
    
    public boolean searchFileInDir(String dir, String filename){
        
        File file = new File(dir);
        if(file.isDirectory())
            return Arrays.asList(file.list()).contains(filename);
        else
            return false;
    }
	
	/**
	 * To access the HTTPS page by disabling certificate check
	 * @param url : HTTPS page to be accessed
	 */
    public void hitHttpsPage(String url){
        WebClient web;        
        try {
            web = new WebClient();
            web.setUseInsecureSSL(true);
            web.getPage(url);
            web.closeAllWindows();
            System.out.println("sent to: "+url);
            Thread.sleep(1000);
        } catch (Exception e) {            
            e.printStackTrace();
        }        
    }
    
    /**
     * To access the HTTPS page multiple times by disabling certificate check
     * @param url   : HTTPS page to be accessed
     * @param count : number of times the given page will be accessed
     */
    public void hitHttpsPage(String url, int count){
        
        for(int i=0;i<count;i++)
            hitHttpsPage(url);
    }
    
    /**
     * To access the HTTP page by disabling certificate check
     * @param url : HTTP page to be accessed
     */
    public void hitHttpPage(String url){
        WebClient web;        
        try {
            web = new WebClient();            
            web.getPage(url);
            web.closeAllWindows();
            System.out.println("sent to: "+url);
            Thread.sleep(1000);
        } catch (Exception e) {            
            e.printStackTrace();
        }        
    }
    
    /**
     * To access the HTTP page multiple times by disabling certificate check
     * @param url   : HTTP page to be accessed
     * @param count : number of times the given page will be accessed
     */
    public void hitHttpPage(String url, int count){
        
        for(int i=0;i<count;i++)
            hitHttpPage(url);
    }
    
    public void sleep(long milliseconds){
        try{
            System.out.println("Sleeping for "+milliseconds+" milliseconds...");
            Thread.sleep(milliseconds);            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * @return  Returns the current System Time in Text format. Ex: Thu April 25 15:43:11 IST 2013
     */
    public String getSystemTime(){
        return Calendar.getInstance().getTime().toString();
    }
    
    /**
     * @return  Returns the Hour value from current System Time in 24hrs format
     */
    public int getSystemTimeHour(){
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
    
    /**
     * @return  Returns the Hour value from current System Time in 12hrs format
     */
    public int getSystemTimeHour12(){
        return Calendar.getInstance().get(Calendar.HOUR);
    }
    
    /**
     * @return  Returns the Minute value from current System Time
     */
    public int getSystemTimeMinute(){
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
    
    /**
     * @return  Returns the Minute value from current System Time
     */
    public int getSystemTimeSecond(){
        return Calendar.getInstance().get(Calendar.SECOND);
    }
    
    /**
     * Returns the Date and Time represented by given Calendar Object in the specified format as a String.
     * @param formatString
     * @return
     */
    public String dateTimeAsString(Calendar calendarInstance, String formatString){
        
        SimpleDateFormat sdf = new SimpleDateFormat(formatString);
        sdf.setTimeZone(calendarInstance.getTimeZone());
        return sdf.format(calendarInstance.getTime());
    }
    
    /**
     * Returns the current Date and Time of system in the specified format as a String.
     * @param formatString
     * @return
     */
    public String dateTimeAsString(String formatString){
        
        return dateTimeAsString(Calendar.getInstance(), formatString);
    }
    
    /**
     * Return Calendar instance in new timezone specified with the +/-offset applied. the offset is in minutes. Ex: if offset=67,
     * the Calendar instance returned will be ahead of 67 minutes.
     * If timezone is null, System Timezone will be used. if offset is 0, Only timezone change will have effect.
     * @param timezone
     * @param changeInMinutes
     * @return
     */
    public Calendar getDateAndTime(String timezone, int changeInMinutes){
        
        Calendar cal = (timezone==null)?Calendar.getInstance():Calendar.getInstance(TimeZone.getTimeZone(timezone));
        
        if(changeInMinutes!=0)
            cal.add(Calendar.MINUTE, changeInMinutes);

        return cal;
    }
    
    /**
     * Return Calendar instance in the new TimeZone specified. Ex: "UTC", "Asia/Calcutta"
     * @param timezone
     * @return  CalendarInstance
     */
    public Calendar getDateAndTimeInTimeZone(String timezone){
        return getDateAndTime(timezone, 0);
    }
    
    /**
     * Return Calendar instance with the +/-offset applied. the offset is in minutes. Ex: if offset=67, 
     * the Calendar instance returned will be ahead of 67 minutes.
     * @param changeInMinutes
     * @return  CalendarInstance
     */
    public Calendar getDateAndTimeWithOffset(int changeInMinutes){
        return getDateAndTime(null, changeInMinutes);
    }
    
    /** Deletes entire folder and subcontents*/
    public void delete(File file)
        	throws IOException{
     
        	if(file.isDirectory()){
     
        		//directory is empty, then delete it
        		if(file.list().length==0){
        		   file.delete();
        		   System.out.println("Directory is deleted : " + file.getAbsolutePath());
        		}
        		else{
             		   //list all the directory contents
            	   String files[] = file.list();
     
            	   for (String temp : files) {
            	      //construct the file structure
            	      File fileDelete = new File(file, temp);
     
            	      //recursive delete
            	     delete(fileDelete);
            	   }
            	   //check the directory again, if empty then delete it
            	   if(file.list().length==0){
               	     file.delete();
            	     System.out.println("Directory is deleted : " + file.getAbsolutePath());
            	   }
        		}
     
        	}
        	else{
        		//if file, then delete it
        		file.delete();
        		System.out.println("File is deleted : " + file.getAbsolutePath());
        	}
        }   
    
    public boolean replaceContentInFile(String file, String oldText, String newText){
        
        File f=new File(file);

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

            while((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
              int index = sb.indexOf(oldText);
              line = sb.toString();
              if(index > 0){
                  line = line.replace(oldText, newText);
                  System.out.println("All occurrences of String "+oldText+" replaced with "+newText+" successfully in file "+file);                  
              }
              else
                  System.out.println("Nothing changed in file "+file);
              

              fs.close();
              in.close();
              br.close();

            } catch (Exception e) {
              e.printStackTrace();
            }

            try{
                fstream = new FileWriter(f);
                outobj = new BufferedWriter(fstream);
                outobj.write(line);
                
                success = true;

            }catch (Exception e){
              System.err.println("Error: " + e.getMessage());
            }
            finally{
                try{
                    outobj.close();
                    fstream.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        
        return success;
    }
    

	public boolean handlePopUp(Process pr)
	{
		ResourceBundle environmentConstants = Utf8ResourceBundle.getBundle("environmentConstants");
		
		String SP_DIALOG_LOGIC_EXE = environmentConstants.getString("SPDialogLogicExe");
		String SP_REGISTER_USERNAME = environmentConstants.getString("SPRegisterUserName");
		String SP_REGISTER_PASSWORD = environmentConstants.getString("SPRegisterPassword");
		String SP_REGISTER_CONFIRM_PASSWORD = environmentConstants.getString("SPRegisterConfirmPassword");
		
		//printing the command output
		System.out.println("Started Printing the Exec command output...");
		
		BufferedReader in = null, in1=null;										
		Process pr1=null;	
			
			try
			{
				in = new BufferedReader(  
	                new InputStreamReader(pr.getInputStream()));  
			 
				String line, line3 = null;				
				boolean executed=false;
				
				while ((line = in.readLine()) != null )					
				{ 					
					System.out.println(line);
				    if (line.contains("Affected parameters are") && (executed==false))
				    {
				    	System.out.println("Started executing providing the input values....");	
				    	Thread.sleep(10000);
				    	pr1=Runtime.getRuntime().exec("\""+SP_DIALOG_LOGIC_EXE+"\" "+SP_REGISTER_USERNAME+" "+SP_REGISTER_PASSWORD+" "+SP_REGISTER_CONFIRM_PASSWORD);				    	

					
						in1= new BufferedReader(new InputStreamReader(pr1.getInputStream()));
        				    String l="";
						int cnt1234=0;
				            while((l=in1.readLine())!=null)
           				    {
               					 System.out.println(l);
									cnt1234++;
								if (cnt1234 == 2)
									break;
            				}



				    	executed=true;
				    }
				    
				    
				    if (line.contains("The Commit phase completed successfully"))
				    {
				    	System.out.println("Successfully registered the Sharepoint windows service...");
				    	return true;
				    }
				    
				    if (line.contains("Rollback phase completed successfully"))
				    {	
				    	
				    	line3 = in.readLine()+in.readLine()+in.readLine();	
				    	System.out.println("line 3 = "+line3);
				    	if (line3.contains("failed"))
				    	{
				    		System.out.println("Problem registring the Sharepoint as windows service...");
				    		return false;
				    	}				    	
				    }
				    
				}
				in.close();

				
			}catch(Exception e)
			{
				e.printStackTrace();
				
			}


		System.out.println("Finished Printing the Exec command output...");

		return false;

	}//end of handlePopUp
    
	
	public boolean printDeregisterOutput(Process pr)
	{
			
		BufferedReader in = null;				
			
			try
			{
				in = new BufferedReader(  
	                new InputStreamReader(pr.getInputStream()));  
			 
				String line,line3=null;				
				while ((line = in.readLine()) != null )					
				{ 					
					System.out.println(line);				    
				    
				    if (line.contains("CA Introscope SPMonitor was successfully removed"))
				    {
				    	System.out.println("Successfully De-registered the Sharepoint windows service...");				    	
				    	return true;
				    }
				    
				    if (line.contains("exception occurred during the uninstallation"))
				    {
						System.out.println("Exception occured during the uninstallation of Sharpoint service...");
				    	line3 = in.readLine()+"\n"+in.readLine()+"\n"+in.readLine();	
				    	System.out.println("line 3 = "+line3);
				    	return false;				    	
				    }
				    
				}
				in.close();

				
			}catch(Exception e)
			{
				e.printStackTrace();
				
			}			

		return false;

	}//end of printDeregisterOutput

	public void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        if (!targetLocation.exists()) {
	            targetLocation.mkdir();
	        }

	        String[] children = sourceLocation.list();
	        for (int i=0; i<children.length; i++) {
	            copyDirectory(new File(sourceLocation, children[i]),
	                    new File(targetLocation, children[i]));
	        }
	    } else {

	        InputStream in = new FileInputStream(sourceLocation);
	        OutputStream out = new FileOutputStream(targetLocation);

	        // Copy the bits from instream to outstream
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    }
	}//end of copyDirectory
	
	/**
     * To find for a string in the given file
     * @param key : key to be searched in file
     * @param filePath :File location
     * @param value    : String to be searched
     * @return           : boolean true if matched found, else false
	 * @throws IOException 
     */
    public String searchForkeyValue(String filePath,String key, String value) throws IOException{
    	//Charset ENCODING = StandardCharsets.UTF_8;
		String perflogLine = "";
		String flag="";
		//Path path = Paths.get(filePath);
		//List<String> configFileList = Files.readAllLines(path, ENCODING);
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(new FileInputStream(filePath));
		//Iterator<String> scanner = configFileList.iterator();
		while (scanner.hasNextLine()) {
			// process each line in some way
			String tmp = scanner.nextLine();
			if (tmp.contains(key)) {
				perflogLine = tmp;
				break;
			}

		}
		if (!perflogLine.isEmpty()) {
			
			if (perflogLine.indexOf('#')>=0 && perflogLine.trim().charAt(0)=='#') flag=key+"  key commented in properties file";
			
			else{
			String perflogAry[] = perflogLine.split("[=, ]");
			for(String s : perflogAry){  
	            if(s.trim().toUpperCase().equalsIgnoreCase(value)) 
	            	{
	            	flag=value.trim().toUpperCase();
	            	break;
	            	}
	        }
			if(!flag.equalsIgnoreCase(value.trim().toUpperCase())) flag="value="+value+" Not assign to appropiate key";
			}
			
		} else
			flag="key="+key+"  In properties file not found";;
		return flag;
    }
	
	public void setEMConfigProperties(String nameValuePair){
        
        System.out.println("***** Setting properties in IntroscopeEnterpriseManager.properties file ******");
        
        String profileFile = convertPathSeparators(TESS_INSTALLDIR+"config/IntroscopeEnterpriseManager.properties");
        setProperties(profileFile, parseProperties(nameValuePair));
        System.out.println("****** Successfully set properties in IntroscopeEnterpriseManager.properties file *******");
    }
	
	public void setProperties(String file, Properties newProps){
	        
	        Properties prop = Util.loadPropertiesFile(file);
	        System.out.println("**** Updating the properties in "+file+" ******");
	        for(String property: newProps.stringPropertyNames()){
	            System.out.println("Setting property "+property+"="+newProps.getProperty(property));
	            prop.setProperty(property, newProps.getProperty(property));
	        }
	        
	        Util.writePropertiesToFile(file, prop);
	}
	
	public Properties parseProperties(String nameValuePair){
	        
	        Properties properties = new Properties();
	        
	        String props[] = nameValuePair.split("#~#");
	        String name ="", value = "";
	        for(String property: props){
	            name = property.split("=")[0];
	            value = property.split("=")[1];
	            properties.setProperty(name, value);
	        }
	        return properties;
	}

	/**
     * To find nth token in the given line
     * @param line : the line which needs to be searched for the token
     * @param t : token number (starts from 1)
     * @return  : String - token in the t'th position in the line  (space is also considered as a token)
     */
	public String parseString(String line,int t){
		String delim = " ";
		StringTokenizer tok = new StringTokenizer(line, delim, true);
		int i=1;
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if(i==t)
				return token;
			i++;
		}
		return null;
	}
	
	/**
	 * 
	 * @param pathTofile
	 * @param pattern
	 * @param fromLine
	 * @return
	 */
	public void searchForTextAndReplace(String pathTofile, String pattern, String replacePattern, long fromLine){      
        
	    String line;
	    ArrayList<String> lines = new ArrayList<String>();     

        try {
            FileReader fr = new FileReader(pathTofile);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter(pathTofile+"1");
            BufferedWriter out = new BufferedWriter(fw);
            while ((line = br.readLine()) != null) {
                if (line.contains(pattern)){ 
                    line=line.replace(pattern, replacePattern);
                    LOGGER.info(line);
                }
                lines.add(line);
                out.write(line+"\n");
            }
            br.close();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }   
    
    }
	
}
