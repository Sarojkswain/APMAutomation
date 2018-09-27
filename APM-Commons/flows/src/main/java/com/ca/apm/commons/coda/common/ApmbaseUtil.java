package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.apache.log4j.Logger;
import org.testng.Assert;

import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.common.Context;
import com.ca.apm.tests.common.IResponse;
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.common.introscope.util.AbstractMetricsUtil;
import com.ca.apm.tests.common.introscope.util.AbstractMetricsUtilFactory;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.tasks.Task;
import com.ca.apm.tests.common.tasks.file.CopyTask;
import com.ca.apm.tests.common.tasks.file.DeleteTask;
import com.ca.apm.tests.common.tasks.properties.PropertiesFileTask;

public class ApmbaseUtil {
    private static Logger LOGGER = Logger.getLogger(ApmbaseUtil.class);

    private static final int SUCCESS = 1;

    private static final int FAILURE = 0;

    private static String value = "";

    private static int log_found = 0;

    public static final String SUCCESS_MESSAGE = "Successful";
    public static final String FAILURE_MESSAGE = "Failed";

    public static String platform = System.getProperty("os.name");
    static TestUtils utility = new TestUtils();
    static CLWCommons clwCommon = new CLWCommons();

    /**
     * This method is to check the file for specific message with time period
     * 
     * @param fileName
     * @param updatedTime
     * @param errorMsg
     * @return
     * @throws Exception
     */
    public static boolean checkValidLastUpdate(String fileName, long updatedTime, String errorMsg)
        throws Exception {
        File file = new File(fileName);
        System.out.println("in fileName " + fileName + " to CHECK " + errorMsg);
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();
            Calendar currentCal = Calendar.getInstance();
            currentCal.add(Calendar.DATE, -1);
            Date previousDay = currentCal.getTime();
            LOGGER.info("Searching from Date" + previousDay.getTime());

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);

                int readByte = fileHandler.readByte();
                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    } else {
                        sb.append((char) readByte);
                        String completeString = sb.reverse().toString();
                        String[] log = completeString.split(" ");
                        if (log[0].contains("/")) {
                            String dateStr = log[0]; // log[1] contains date
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                            Date logDate = sdf.parse(dateStr.trim());

                            // splitting the string to get hours, minutes
                            // separately
                            String[] logTime = log[1].split(":"); // log[1]
                                                                  // contains
                            // time
                            String day = log[2]; // log[2] contains day
                            int dayTime = 0;
                            if (day.equals("AM"))
                                dayTime = Calendar.AM;
                            else
                                dayTime = Calendar.PM;

                            // preparing log Calendar object
                            Calendar logCal = Calendar.getInstance();
                            logCal.setTime(logDate);
                            logCal.set(Calendar.AM_PM, dayTime);
                            logCal.set(Calendar.HOUR, Integer.parseInt(logTime[0])); // setting
                            // hours
                            logCal.set(Calendar.MINUTE, Integer.parseInt(logTime[1])); // setting
                            // minutes

                            Date updateDate = new Date(updatedTime);

                            // preparing calendar object 6 minutes before on
                            // passed
                            // calendar object(updCalendar)
                            Calendar updatCalBefore = Calendar.getInstance();
                            updatCalBefore.setTime(updateDate);
                            updatCalBefore.add(Calendar.MINUTE, -6);

                            // preparing calendar object 6 minutes after on
                            // passed
                            // calendar object(updCalendar)
                            Calendar updateCalAfter = Calendar.getInstance();
                            updateCalAfter.setTime(updateDate);
                            updateCalAfter.add(Calendar.MINUTE, 6);

                            if ((logCal.after(updatCalBefore) && logCal.before(updateCalAfter))) {
                                if (completeString.contains(errorMsg)) {
                                    System.out.println("%%%%%%%exists%%%%%% " + completeString);
                                    return true;
                                }
                            } else if ((previousDay.equals(logCal.getTime()))) {

                                return false;
                            }
                        }
                        sb = new StringBuilder();

                    }// end else
                }// end else 0xD
                sb.append((char) readByte);

            }// end for
        } catch (Exception e) {
            LOGGER.error("IO Exception occurred: ", e);
            throw e;
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (Exception e) {
                    LOGGER.error("IO Exception occurred: ", e);
                }
            }
        }
        return false;
    }

    /**
     * This method is to rename the file
     * Please provide file with extension
     * 
     * @param path
     * @param fileName
     * @param testCaseName
     * @throws Exception
     */
    public static void fileRename(String path, String fileName, String testCaseName)
        throws Exception {
        LOGGER.info("");
        File file = new File(path + "/" + fileName);
        file.renameTo(new File(path + "/" + fileName + "_" + testCaseName));
        LOGGER.info("File is renamed to " + path + "/" + fileName + "_" + testCaseName);
    }


    /**
     * Method to check console output provide the string to compare.
     * compareStrings is the parameter required.
     * 
     * @param process
     * @param compareStrings
     * @return
     * @throws IOException
     */
    public static int checkConsoleOutput(Process process, String compareStrings) throws IOException {
        Util.sleep(6000);
        int found = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            LOGGER.info(line);
            if (line.toLowerCase().contains(compareStrings.toLowerCase())) {
                found = 1;
                LOGGER.info("*** " + line);
                break;
            }
        }
        return found;
    }

    /**
     * This method is to return the process after executing the command, this
     * would be used to check the generated messages on console
     * 
     * @param commands
     * @param dirLoc
     * @return
     * @throws IOException
     */
    public static Process runCommand(List<String> commands, String dirLoc) throws IOException {

        if (platform.toUpperCase().contains("WINDOWS")) {
            commands.add(0, "cmd");
            commands.add(1, "/c");
        } else if (platform.toUpperCase().contains("LINUX")) {
            commands.add(0, "sh");
            commands.add(1, "-c");
        }
        LOGGER.info("Running these list" + commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(new File(dirLoc));
        Process process = processBuilder.start();
        return process;
    }

    /**
     * This method is used to kill process
     * 
     * @param process
     */
    public static void killProcess(Process process) {
        LOGGER.info("Killing" + process);
        if (process != null) process.destroy();
    }

    /**
     * This method is used to delete the file. File is needed with test cases
     * name.
     * 
     * @param path
     * @param fileName
     * @param testCaseName
     */
    public static void deleteLogFile(String path, String fileName, String testCaseName) {
        try {

            if (platform.contains("Windows")) {
                File winFile = new File(path + "/" + fileName + "_" + testCaseName + ".log");
                if (winFile.exists()) winFile.delete();
                LOGGER.info("File " + path + "/" + fileName + "_" + testCaseName + ".log"
                    + " delted");
            }
            if (platform.contains("Linux")) {
                File linuxFile =
                    new File(path.replace('\\', '/') + "/" + fileName + "_" + testCaseName + ".log");
                if (linuxFile.exists()) linuxFile.delete();
                LOGGER.info("File " + path.replace('\\', '/') + "/" + fileName + "_" + testCaseName
                    + ".log" + " delteted ");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns process Object with the given input commands
     * 
     * @param commands
     * @param dirLoc
     * @return
     * @throws IOException
     */
    public static Process getProcess(String[] command, String dirLoc) throws IOException {

        List<String> commands = buildCommand(command);
        
        if (commands.isEmpty()) return null;
        if (platform.toUpperCase().contains("WINDOWS")) {
            commands.add(0, "cmd");
            commands.add(1, "/c");
        } else if (platform.toUpperCase().contains("LINUX")){
            commands.add(0, "sh");
            commands.add(1, "-c");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(new File(dirLoc));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return process;
    }

    public static List<String> buildCommand(String[] commands) {
        
        StringBuilder command =new StringBuilder();
        for (int i=0;i<commands.length;i++)
        {
            command.append(commands[i]);
            command.append(" ");
        }
        
        LOGGER.info("Built command is : "+command);
        
        List<String> list = new ArrayList<String>();
        list.add(command.toString());
        
        return list;
    }

    /**
     * This method is to execute the Commands, and result would be captured in
     * txt file, that would be checked against List of compareStrings This is
     * used for watchdog and it has issue with control giving back to console
     * which uses Thread.
     * 
     * @param commands
     * @param dirLoc
     * @param outFile
     * @param compareStrings
     * @param timeWait
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int executeCommandAndCheckOutput(String[] commands, String dirLoc,
        String outFile, List<String> compareStrings, int timeWait) throws IOException,
        InterruptedException {
        int found = 0;
        int mWait = 18 * 10000;
        int tWait = 0;
        if (timeWait > mWait) mWait = timeWait;
        Process process = getProcess(commands, dirLoc);
        if (process == null) return found;
        try {
            File file = new File(outFile);
            if (!file.exists()) file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            
            System.out.println("Filename is " + file.getAbsolutePath());
            
            OutputConsoleThread outPush = new OutputConsoleThread(process.getInputStream(), fos);
            Thread th = new Thread(outPush);
            th.start();
            while (found == 0) {
                if (mWait >= tWait) {
                    break;
                }
                found = checkMessages(compareStrings, file);
                Thread.sleep(tWait + 3000);
                tWait = tWait + 3000;
                LOGGER.info("" + found);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            killProcess(process);
        }
        return found;
    }

    /**
     * This method is to check the List of messages in file
     * 
     * @param compareStrings
     * @param file
     * @return
     * @throws InterruptedException
     */
    public static int checkMessages(List<String> compareStrings, File file)
        throws InterruptedException {

        LOGGER.info("Checking out messages--------------");
        int found = 0;
        DataInputStream in = null;
        FileInputStream fstream = null;
        BufferedReader br = null;
        if (compareStrings != null) {
            try {
                fstream = new FileInputStream(file);
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
                String str;
                while ((str = br.readLine()) != null) {
                    for (String errorMsg : compareStrings) {
                        if (str.contains(errorMsg)) {
                            LOGGER.info("---- " + errorMsg);
                            compareStrings.remove(errorMsg);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println(e);
            } finally {
                try {
                    fstream.close();
                    in.close();
                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (compareStrings.size() == 0) found = 1;
        }

        return found;

    }

    /**
     * This method is to check the list of messages against the messages
     * generated on Console. This method is generalised for all other test
     * cases.
     * 
     * @param command
     * @param compareStrings
     * @return
     * @throws Exception
     */
    public static int executeCommand(String[] command, List<String> compareStrings)
        throws Exception {
        int found = 0;

        Process process = null;
        try {

            process = Runtime.getRuntime().exec(command);

            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                for (String errorMsg : compareStrings) {
                    if (line.contains(errorMsg)) {
                        System.out.println("---- " + errorMsg);
                        compareStrings.remove(errorMsg);
                        break;
                    }
                }
            }
            if (compareStrings.size() == 0) found = 1;

            return found;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;

        } finally {
            if (process != null) {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }
        }
    }

    /**
     * This method is to start the WatchDog
     * 
     * Independent of OS
     * 
     * @param emBinDir
     */

    public static void startWatchDogEM(String emBinDir) {
        try {
            String startWatchDogWithPort = "java -jar WatchDog.jar start -watch";
            List<String> list = new ArrayList<String>();
            list.add(startWatchDogWithPort);
            list.add("\\r");

            LOGGER.info("START" + list);
            invokeProcessBuilder(list, emBinDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the watchdog process
     * 
     * Not dependent on OS
     * 
     * @param emBinDir
     */

    public static void stopWatchDogEM(String emBinDir) {
        try {
            String startWatchDogWithPort = "java -jar WatchDog.jar stop";
            List<String> list = new ArrayList<String>();
            list.add(startWatchDogWithPort);
            list.add("\\r");

            LOGGER.info("STOP" + list);

            invokeProcessBuilder(list, emBinDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param path
     * @param port
     * @param host
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean stopWatchDogEMWithPort(String emBinDir, String port, String host)
        throws IOException, InterruptedException {

        boolean flag = false;
        String[] stopCmnd = ApmbaseConstants.STOP_EM_COMMAND;
        String startWatchDogWithPort = "java -jar WatchDog.jar stop";
        List<String> list = new ArrayList<String>();
        list.add(startWatchDogWithPort);
        list.add("-port " + port);
        list.add("\\r");

        LOGGER.info("STOP" + list);

        try {
            flag = invokeProcessBuilder(list, emBinDir);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return flag;
    }


    /**
     * This method checks if the specified <code>port</code> at <code>hostName</code> is being
     * listened (occupied).
     * 
     * @param port
     * @param hostName
     * @return <code>true</code> if already occupied, otherwise <code>false</code>
     */
    public static boolean isPortAvailable(int port, String hostName) {
        Socket soc = null;
        try {
            soc = new Socket(hostName, port);
            return soc.isBound();
        } catch (IOException e) {
            LOGGER.error("Failed to connect to '" + hostName + ":" + port + "': " + e.getMessage());
            return false;
        } finally {
            try {
                if (soc != null) {
                    soc.close();
                }
            } catch (IOException e) {
                LOGGER.error("Exception caught while releasing the socket at '" + hostName + ":"
                    + port + "': ", e);
                e.printStackTrace();
            }
        }

    }

    /**
     * This method is to Rename and clear the content of the file. Used for
     * WatchDog module
     * 
     * @param fileLoc
     * @param fileName
     * @param append
     */
    public static void renameAndClearFileContent(String fileLoc, String fileName, String append) {
        PrintWriter pw = null;
        BufferedReader br = null;
        try {

            File inputFile = new File(fileLoc + "/" + fileName + ".log");
            File outputFile = new File(fileLoc + "/" + fileName + "_" + append + ".log");
            FileReader in = new FileReader(inputFile);
            FileWriter out = new FileWriter(outputFile);
            int c;
            while ((c = in.read()) != -1)
                out.write(c);
            in.close();
            out.close();
            br = new BufferedReader(new FileReader(inputFile));
            pw = new PrintWriter(new FileWriter(inputFile));
            String line = null;
            // Read from the original file and write to the new, unless content
            // matches data to be removed.
            while ((line = br.readLine()) != null) {
                pw.println(line);
                pw.flush();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {

            try {
                if (pw != null) pw.close();
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is to check the server is stopped as windows service or not
     * 
     * @param service_name
     * @return
     * @throws Exception
     */
    public static int StopService(String service_name) throws Exception {
        return doServiceServer("NET STOP \"" + service_name + "\"");
    }

    /**
     * This method is to check the server start as windows service or not
     * 
     * @param service_name
     * @return
     * @throws Exception
     */
    public static int StartService(String service_name) throws Exception {
        return doServiceServer("NET START \"" + service_name + "\"");
    }

    /**
     * This common method for start and stop services.
     * 
     * @param command
     * @return
     * @throws Exception
     */
    public static int doServiceServer(String command) throws Exception {
        int extValue = 0;
        try {
            System.out.println(command);
            Process p4 = Runtime.getRuntime().exec(command);
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(p4.getInputStream()));
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            extValue = 0;
            p4.waitFor();

        } catch (Exception e) {
            extValue = 1;
            e.printStackTrace();
        }
        return extValue;
    }

    /**
     * This method is to check the server running as windows service or not
     * 
     * @param service_name
     * @return
     * @throws Exception
     */
    public static String verifyService(String service_name) throws Exception {
        Runtime rt = Runtime.getRuntime();
        String command = "sc query \"" + service_name + "\"";
        System.out.println(command);
        Process p = rt.exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String c;
        while ((c = br.readLine()) != null) {
            if (c.contains("STATE")) {
                String line[] = c.split(":");
                String key = line[0].trim();
                key = key + "";
                String value = line[1].trim();
                System.out.println("The service " + service_name + " is in " + value + " state");
                if (value.contains("RUNNING")) return value;
            }
        }
        return value;
    }

    /**
     * This method is to check the Two Error Entries in the same line of file
     * 
     * @param file_name
     * @param file_path
     * @param error_entry1
     * @param error_entry2
     * @return
     * @throws Exception
     */
    public static int findTwoStringsInSingleLine(String file_name, String file_path,
        String error_entry1, String error_entry2) throws Exception {

        String file = file_path + "/" + file_name;
        File f = new File(file);
        BufferedReader br1 = null;
        try {
            br1 = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br1.readLine()) != null && log_found == 0) {
                if ((line.trim().contains(error_entry1.trim()))) {
                    if ((line.trim().contains(error_entry2.trim()))) {
                        log_found = 1;
                        LOGGER.info(line);

                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("IO Exception occurred: ", e);
            throw e;
        } finally {
            if (br1 != null) {
                try {
                    br1.close();
                } catch (Exception e) {
                    LOGGER.error("IO Exception occurred: ", e);
                    throw e;
                }
            }
        }
        return log_found;
    }

    /**
     * This method is to check the ERROR/WARN message in a log file
     * 
     * @param file_name
     * @param file_path
     * @param error_entry
     * @return
     */

    public static int checklog(String file_name, String file_path, String error_entry) {
        error_entry = error_entry.trim();
        int log_found = 0;
        BufferedReader br1 = null;
        String file = null;
        try {

            if (platform.contains("Windows")) if (file_name.contains(".log"))
                file = file_path + "\\" + file_name;
            else
                file = file_path + "\\" + file_name + ".log";

            if (platform.contains("Linux")) if (file_name.contains(".log"))
                file = file_path.replace('\\', '/') + file_name;
            else
                file = file_path.replace('\\', '/') + file_name + ".log";

            File f = new File(file);
            LOGGER.info("looking for \"" + error_entry + "\" in " + f);
            br1 = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br1.readLine()) != null && log_found == 0) {
                if (line.contains(error_entry)) {
                    log_found = 1;
                    LOGGER.info("found line: " + line);
                    break;
                }
            }

        } catch (IOException e) {
            LOGGER.error("Exception: ", e);

        } finally {
            try {
                br1.close();
            } catch (IOException e) {
                LOGGER.error("Exception: ", e);
            }
        }
        return log_found;
    }

    /**
     * This method is to check the ERROR/WARN message in a log file and if found return true else
     * false
     * 
     * @param file_path
     * @param error_entry
     * @return
     */

    public static boolean checklogMsg(String file_path, String error_entry) {
        boolean log_found = false;
        BufferedReader br1 = null;
        // String file = null;
        try {

            File f = new File(file_path);
            br1 = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br1.readLine()) != null && log_found == false) {
                if (line.trim().contains(error_entry.trim())) {
                    log_found = true;
                    LOGGER.info(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                br1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return log_found;
    }
    
    /**
     * This method is to check number of Occurrence in a log file and  return number 
     * 
     * 
     * @param file_path
     * @param error_entry
     * @return
     */

    public static int checklogMsgOccurrence(String file_path, String msg) {
       // boolean log_found = false;
        BufferedReader br1 = null;
        int counter=0;
        // String file = null;
        try {

            File f = new File(file_path);
            br1 = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br1.readLine()) != null ) {
                if (line.trim().contains(msg.trim())) {
                  //  log_found = true;
                    counter++;
                    LOGGER.info("Found Counter::"+counter+">>>>>>>"+line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                br1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return counter;
    }

    /**
     * This method is to copy the directory from src location to dest location
     * 
     * @param sourceLocation
     * @param targetLocation
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                    children[i]));
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
    }


    /**
     * Specify just the commands as part of args and the right commands for the OS.
     * Based on what OS the tests run, this will invoke the appropriate route to
     * the corresponding OPERATING SYSTEM.
     * 
     * @param args
     * @return
     * @throws Exception
     */
    public static boolean invokeProcessBuilder(List<String> args) throws Exception {

        String workingDir = "";

        if (platform.toUpperCase().contains("WINDOWS"))
            workingDir = "C:\\";
        else if (platform.toUpperCase().contains("LINUX")) workingDir = "/";

        LOGGER.info(args);
        return invokeProcessBuilder(args, workingDir);
    }

    /**
     * Specify just the commands as part of args and the right commands for the OS.
     * Based on what OS the tests run, this will invoke the appropriate route to
     * the corresponding OPERATING SYSTEM.
     * 
     * @param args
     * @param workingDir
     * @return
     * @throws Exception
     */
    public static boolean invokeProcessBuilder(List<String> args, String workingDir)
        throws Exception {

        if (platform.toUpperCase().contains("WINDOWS")) {
            args.add(0, "cmd");
            args.add(1, "/c");
        } else if (platform.toUpperCase().contains("LINUX")) {
            args.add(0, "sh");
            args.add(1, "-c");
        }
        LOGGER.info(workingDir);
        LOGGER.info("The Args ARE....."+args);

        return invokeProcessBuilder(args, workingDir, true);
    }

    public static boolean invokeProcessBuilder(List<String> args, String workingDir,
        boolean showOutput) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        if (workingDir != null) pb.directory(new File(workingDir));
        Map<String, String> env = pb.environment();

        env.put("BUILD_ID", "dontKillMe");
        invokeProcessBuilderNoWait(pb, showOutput);
        return true;
    }

    public static Process invokeProcessBuilderNoWait(ProcessBuilder pb) throws Exception {
        return invokeProcessBuilderNoWait(pb, true);
    }

    public static Process invokeProcessBuilderNoWait(ProcessBuilder pb, boolean showOutput)
        throws Exception {
        Process process = null;
        try {
            process = pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("**** At end of invokeProcessBuilderNoWait ****");
        return process;
    }

    public static boolean lookForPortAvailability(String host, int port, long sec, boolean status)

    {
        while ((sec = sec - 30) > 0) {
            if (status == isPortAvailable(port, host)) return true;
            System.out.println("Waiting for the port become ready...");
            sleep(30);
        }
        return false;

    }

    public static void sleep(long duration) {
        System.out.println("Sleeping for " + duration + " seconds.");
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            System.out.println("***sleep failed!***");
        }
    }


    /**
     * This method rolls/backs up given file.
     * It does so by renaming it to a file with appended ".N" suffix,
     * where the N is in interval [0,Integer.MAX_VALUE] and a file with the same
     * name does not exist yet.
     * 
     * @param f file to roll/backup
     */
    public static void rollFile(File f) {
        if (!f.exists()) return;

        for (int i = 0; i != Integer.MAX_VALUE; ++i) {
            File destFile = new File(f.getPath() + "." + Integer.toString(i));
            if (!destFile.exists()) {
                try {
                    f.renameTo(destFile);
                    LOGGER.info("rolled file \"" + f.getCanonicalPath() + "\" to \""
                        + destFile.getName() + "\"");
                    return;
                } catch (IOException e) {
                    LOGGER.error("failed to roll file \"" + f.getAbsolutePath() + "\" to \""
                        + destFile.getName() + "\", exception:", e);
                    return;
                }
            }
        }
    }


    /**
     * THis method is for checking the port availability for 20 min, if not
     * found it returns Failure on start of EM
     * 
     * @param hostName
     * @param port
     * @return
     * @throws Exception
     */
    public static int lookForPortReady(String hostName, int port) throws Exception {

        LOGGER.info("Entering ApmbaseUtil.lookForPortReady(String hostName, int port)");
        LOGGER.info("hostName: " + hostName);
        LOGGER.info("port: " + port);

        int timeOut = 5 * 60 * 1000; // 5 min
        long startTime = System.currentTimeMillis();
        while (!isPortAvailable(port, hostName)) {
            Util.sleep(5000);// 5 sec

            long timeDiffMillis = System.currentTimeMillis() - startTime;
            if (timeDiffMillis > timeOut) {
                LOGGER.info("TIME OUT: EM not started!");
                return FAILURE;
            }
            LOGGER.info("Waiting for " + timeDiffMillis / 1000 + " sec for EM to start ... ");
            continue;
        }
        LOGGER.info("Successfully found EM running at port '" + port + "' at host '" + hostName
            + "'!");
        LOGGER.info("Leaving ApmbaseUtil.lookForPortReady(String hostName, int port)");
        return SUCCESS;
    }

    public static SkipLineResult skipToLine(int linecount, String fileName, String errorMsg)
        throws Exception {
        boolean found = false;
        int i = 0;
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = "";
        try {
            while (i != linecount - 1) {
                br.readLine();
                i++;
            }

            while ((line = br.readLine()) != null) {
                linecount++;
                if (line.contains(errorMsg)) {
                    found = true;
                    break;
                } else {
                    found = false;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return new SkipLineResult(found, linecount);
    }


    /**
     * Method to Start HVR Agent
     * 
     * @param agentPath
     */
    public static void startHVRAgent(String agentPath) {
        System.out.println("Inside startHVRAgent- Agent Path: " + agentPath);
        Process process = null;
        try {
            String[] commands = {"cmd.exe", "/c", ApmbaseConstants.HVR_AGENT_BAT};
            process = runCommand(Arrays.asList(commands), agentPath);
            Util.sleep(30000);
            checkConsoleOutput(process, "FakeAgent");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            killProcess(process);
        }
    }

    /**
     * Method to Stop HVR Agent
     * 
     */
    public static void stopHVRAgent() {
        Process process = null;
        try {
            String[] commands =
                {"cmd.exe", "/c",
                        "wmic Path win32_process Where \"CommandLine Like \'%cloneagents%\'\" Call Terminate"};
            process = runCommand(Arrays.asList(commands), "C:/");
            Util.sleep(30000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            killProcess(process);
        }
    }

    /**
     * This method is to check the file
     * 
     * @param file_name
     * @param file_path
     * @param prop_name
     * @param prop_value
     * @throws Exception
     */
    // check for properties set in properties file
    public static int checkproperties(String file_name, String file_path, String prop_name,
        String prop_value) throws Exception {
        int property_set = 0;
        String file = null;
        if (platform.toUpperCase().contains("WINDOWS")) {
            file = file_path + "\\" + file_name;
        } else if (platform.toUpperCase().contains("LINUX")) {
            file = file_path + "/" + file_name;
        }

        BufferedReader br1 = null;
        try {
            br1 = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br1.readLine()) != null) {
                String prop_line = prop_name.trim() + "=" + prop_value.trim();
                // System.out.println(prop_line);
                if ((line.trim().equals(prop_line))) {
                    property_set = 1;
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            LOGGER.error("IO Exception occurred: " + e);
            throw e;
        } finally {
            if (br1 != null) {
                try {
                    br1.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close file: ", e);
                    throw e;
                }
            }
        }
        return property_set;
    }

    /**
     * This method is to set the properties
     * 
     * @param file_name
     * @param file_path
     * @param prop_name
     * @param prop_value
     * @return
     * @throws Exception
     */
    public static int setproperties(String file_name, String file_path, String prop_name,
        String prop_value) throws Exception {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {

            int property_set = 0;
            System.out.println(file_name + file_path + prop_name + prop_value);
            String NL = System.getProperty("line.separator");
            String file = null;

            if (platform.toUpperCase().contains("WINDOWS")) {
                file = file_path + "\\" + file_name;
            } else if (platform.toUpperCase().contains("LINUX")) {
                file = file_path + "/" + file_name;
            }

            System.out.println(file);
            String name[] = file_name.split("\\.");
            String temp_file = file_path + "\\" + name[0] + "temp." + name[1];
            File source = new File(file);
            File target = new File(temp_file);
            System.out.println(temp_file);
            br = new BufferedReader(new FileReader(source));
            bw = new BufferedWriter(new FileWriter(target));
            String line = "";
            String prop_line = prop_name.trim();
            System.out.println(prop_line);
            while ((line = br.readLine()) != null) {
                if ((line.trim().startsWith(prop_line))) {
                    System.out.println("--changed property --" + line);
                    String prop_set[] = line.split("\\=");
                    if (prop_set[0].equalsIgnoreCase(prop_line)) {
                        bw.write(prop_set[0] + "=" + prop_value + NL);
                        System.out.println(prop_set[0] + "=" + prop_value);
                    } else {
                        bw.write(line + NL);
                        System.out.println(line);
                    }
                    property_set = 1;
                } else {
                    bw.write(line + NL);
                }
            }
            bw.flush();
            Thread.sleep(3000);
            // int val = file_replace(temp_file,file);
            copy(temp_file, file);
            int val = 1;
            if (val == 1) property_set = 1;
            target.deleteOnExit();
            return property_set;
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }

    }

    /**
     * This method is to replace the file
     * 
     * @param source_file
     * @param target_file
     * @return
     */
    public static int file_replace(String source_file, String target_file) {
        File source = new File(source_file);
        File target = new File(target_file);
        int success = 0;
        String copy = "";
        try {
            if (platform.toUpperCase().contains("WINDOWS")) {
                copy = "copy";

            } else if (platform.toUpperCase().contains("LINUX")) {
                copy = "cp";

            }
            String command2 = copy + " " + "\"" + source + "\"" + " " + "\"" + target + "\"";
            System.out.println("Copying " + "\"" + source + "\"" + " file to " + "\"" + target
                + "\"");
            // @SuppressWarnings("unused")
            // Process p4 = Runtime.getRuntime().exec(command2);
            List<String> list = new ArrayList<String>();
            list.add(command2);
            invokeProcessBuilder(list);


            success = 1;
        } catch (Exception e) {
            e.printStackTrace();
            success = 0;
        }
        return success;

    }

    /**
     * This method is to copy the file from src location to dest locaion
     * 
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copy(String src, String dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            File source = new File(src);
            File target = new File(dst);
            in = new FileInputStream(source);
            out = new FileOutputStream(target);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }

        }
    }

    /**
     * This method is to append the properties
     * 
     * @param propLst
     * @param fileName
     * @param filePath
     * @throws IOException
     */
    public static void appendProperties(List<String> propLst, String fileName, String filePath)
        throws IOException {
        if (propLst == null || propLst.isEmpty()) return;
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter(filePath + "/" + fileName, true);
            out = new BufferedWriter(fstream);
            for (String eachLine : propLst) {

                out.newLine();
                out.write(eachLine);
            }
            out.flush();
        } finally {
            if (out != null) out.close();
        }
    }

    /**
     * This method is to remove the properties
     * 
     * @param file_name
     * @param file_path
     * @param propLst
     * @return
     * @throws Exception
     */
    public static int removeProperties(String file_name, String file_path, List<String> propLst)
        throws Exception {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            System.out.println("%%%%%%%%%%%%%%%%%%%");
            int property_set = 0;

            String NL = System.getProperty("line.separator");
            String file = null;
            if (platform.toUpperCase().contains("WINDOWS")) {
                file = file_path + "\\" + file_name;
            } else if (platform.toUpperCase().contains("LINUX")) {
                file = file_path + "/" + file_name;
            }
            System.out.println(file);
            String name[] = file_name.split("\\.");
            String temp_file = file_path + "\\" + name[0] + "temp." + name[1];
            File source = new File(file);
            File target = new File(temp_file);
            System.out.println(temp_file);
            br = new BufferedReader(new FileReader(source));
            bw = new BufferedWriter(new FileWriter(target));
            String line = "";

            while ((line = br.readLine()) != null) {
                if (propLst.contains(line.trim())) {
                    System.out.println("--changed property --" + line);
                    property_set = 1;
                } else {
                    bw.write(line + NL);
                }
            }
            bw.flush();
            copy(temp_file, file);
            int val = 1;
            if (val == 1) property_set = 1;
            target.deleteOnExit();
            return property_set;
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }

    }

    /**
     * This method is to get the propertiey value on its key
     * 
     * @param paramName
     * @param fileName
     * @param filePath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String getPropertyValue(String paramName, String fileName, String filePath)
        throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(filePath + "/" + fileName));
        String value = properties.getProperty(paramName);
        return value;

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
        System.out.println("% Deleting directory " + dir.getAbsolutePath() + " %");
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                deleted = file.delete();
                if (!deleted) {
                    // throw new IOException("Unable to delete file" + file);
                    return deleted;
                }
            }
        }

        return dir.delete();
    }

    /**
     * This method will check the given messages Last occurance.
     * 
     * @param fileName
     * @param errorMsg
     * @return
     * @throws Exception
     */
    public static boolean checkValidLastUpdate(String fileName, String errorMsg) throws IOException {
        String str = null, logFound = null;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int count = 0;


        try {
            while ((str = reader.readLine()) != null) {
                if (str.contains(errorMsg)) {
                    logFound = str;
                    count++;
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            reader.close();
        }

        LOGGER.info("The log message" + errorMsg + "Found " + count + " times");
        LOGGER.info("The Last occurance is at " + logFound);
        if (logFound == null)
            return false;
        else
            return true;
    }

    /**
     * This method will check the given messages Last occurance with case insesitive.
     * 
     * @param fileName
     * @param errorMsg
     * @return
     * @throws Exception
     */
    public static boolean checkValidLastUpdateIgnoreCase(String fileName, String errorMsg)
        throws IOException {
        String str = null, logFound = null;
        int count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try {
            while ((str = reader.readLine()) != null) {
                if (str.toLowerCase().contains(errorMsg.toLowerCase())) {
                    logFound = str;
                    count++;
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            reader.close();
        }
        LOGGER.info("The log message" + errorMsg + "Found " + count + " times");
        LOGGER.info("The Last occurance is at " + logFound);
        if (logFound == null)
            return false;
        else
            return true;
    }


    public static void setSystemTime(String seconds) throws IOException {
        Calendar now = Calendar.getInstance();
        int sec = Integer.parseInt(seconds);

        System.out.println("Current time : " + now.get(Calendar.HOUR_OF_DAY) + ":"
            + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND));

        now.add(Calendar.SECOND, -sec);
        String changetime =
            now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":"
                + now.get(Calendar.SECOND);


        System.out.println("The Platform is " + platform);
        List<String> cmdList = new ArrayList<String>();

        if (platform.contains("Windows")) {

            cmdList.add(changetime);
            try {
                invokeProcessBuilder(cmdList, "/");
            } catch (Exception e) {
                LOGGER.info("Unable to change the System Time for Windows OS");
                e.printStackTrace();
            }

        }
        if (platform.contains("Linux")) {
            cmdList.add(changetime);
            try {
                invokeProcessBuilder(cmdList, "/");
            } catch (Exception e) {
                LOGGER.info("Unable to change the System Time for Linux OS");
                e.printStackTrace();
            }
        }

        Calendar now1 = Calendar.getInstance();
        LOGGER.info("Time" + now1.get(Calendar.HOUR_OF_DAY) + ":" + now1.get(Calendar.MINUTE) + ":"
            + now1.get(Calendar.SECOND));
    }

    public static boolean deleteFile(String fileName) {
        boolean isFileExists = false;
        boolean isFileDeleted = false;
        try {
            isFileExists = fileExists(fileName);
            if (isFileExists) {
                Task task = new DeleteTask(fileName);
                Context context = new Context();
                IResponse response = task.execute(context);
                isFileDeleted = response.isSuccess();
            } else {
                System.out.println(fileName + "********FILE NOT FOUND********");
                System.out
                    .println("********isFileDeleted flag is made to true as file is not present********");
                isFileDeleted = true;
            }
        } catch (Exception e) {
            System.out.println("Unable to delete the file due to:" + e.getMessage());
            e.printStackTrace();
        }
        return isFileDeleted;
    }

    public static boolean fileExists(String fileDir) {
        boolean isFileExists = false;
        try {
            File file = new File(fileDir);
            System.out.println("File ::" + file.getAbsolutePath() + "is File Exist:"
                + file.exists());
            isFileExists = file.exists();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isFileExists;
    }

    public static boolean copyFile(String srcFileName, String destFileName) {
        boolean isFileExists = false;
        boolean isFileCopied = false;
        try {
            isFileExists = fileExists(srcFileName);
            if (isFileExists) {
                Task task = new CopyTask(srcFileName, destFileName);
                Context context = new Context();
                IResponse response = task.execute(context);
                isFileCopied = response.isSuccess();
            } else {
                System.out.println(srcFileName + "********FILE NOT FOUND********");
            }
        } catch (Exception e) {
            System.out.println("Unable to copy the file due to:" + e.getMessage());
            e.printStackTrace();
        }
        return isFileCopied;
    }

    /**
     * Currently not usable
     * 
     * 
     * @param propertyKeys
     * @param propertyValues
     * @param filePath
     * @return
     */


    public static boolean updateProperties(String propertyKeys, String propertyValues,
        String filePath) {
        boolean isPropertyUpdated = false;
        boolean isFileExists = false;
        String[] propKeys = propertyKeys.split("~");
        String[] propValues = propertyValues.split("~");
        try {
            isFileExists = fileExists(filePath);
            if (isFileExists) {
                PropertiesFileTask task = new PropertiesFileTask(filePath);
                Context context = new Context();
                for (int i = 0; i < propKeys.length; i++)
                    task.setProperty(propKeys[i], propValues[i]);
                task.execute(context);
                // delay is to save the changes in the file
                Util.sleep(6000);
                for (int i = 0; i < propKeys.length; i++) {
                    String propertyValue = task.getProperties().getProperty(propKeys[i]);
                    System.out.println(propertyValue);
                    isPropertyUpdated = propertyValue.equalsIgnoreCase(propValues[i]);
                    if (!isPropertyUpdated) {
                        isPropertyUpdated = false;
                        System.out.println("********Property not updated properly********");
                        break;
                    }
                }
            } else {
                System.out.println(filePath + "********FILE NOT FOUND********");
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        return isPropertyUpdated;
    }

    /**
     * This method takes timestamp in below format and addes 15 seconds to it
     * Input "0 Feb 19 2:56:00 0 2016"
     * Output "2016/2/19 2:56:15"
     * 
     * @param timeStamp
     *        - time stamp
     * @return
     */
    public static String convertTimeStamp(String timeStamp) {

        String time = "";

        String mon = "";

        String year = "";

        String day = "";

        int i = 0;

        try {

            StringTokenizer st = new StringTokenizer(timeStamp, " ");

            while (st.hasMoreElements()) {

                String token = (String) st.nextElement();

                if (null != token) {

                    if (i == 1) {

                        if ("Jan".equalsIgnoreCase(token)) {

                            mon = "01";

                        } else if ("Feb".equalsIgnoreCase(token)) {

                            mon = "02";

                        } else if ("Mar".equalsIgnoreCase(token)) {

                            mon = "03";

                        } else if ("Apr".equalsIgnoreCase(token)) {

                            mon = "04";

                        } else if ("May".equalsIgnoreCase(token)) {

                            mon = "05";

                        } else if ("Jun".equalsIgnoreCase(token)) {

                            mon = "06";

                        } else if ("Jul".equalsIgnoreCase(token)) {

                            mon = "07";

                        } else if ("Aug".equalsIgnoreCase(token)) {

                            mon = "08";

                        } else if ("Sep".equalsIgnoreCase(token)) {

                            mon = "09";

                        } else if ("Oct".equalsIgnoreCase(token)) {

                            mon = "10";

                        } else if ("Nov".equalsIgnoreCase(token)) {

                            mon = "11";

                        } else if ("Dec".equalsIgnoreCase(token)) {

                            mon = "12";

                        }

                    }

                    if (i == 2) {

                        day = token;

                    }

                    if (i == 3) {

                        time = token;

                    }

                    if (i == 5) {

                        year = token;

                    }

                }

                i++;

            }

            System.out.println("-------" + year + "/" + mon + "/" + day + " " + time);

        } catch (Exception e) {

            e.printStackTrace();

        }
        String initialTime = year + "/" + mon + "/" + day + " " + time;
        Calendar now1 = Calendar.getInstance();
        String[] initTime = time.split(":");

        now1.set(Integer.parseInt(year), Integer.parseInt(mon), Integer.parseInt(day),
            Integer.parseInt(initTime[0]), Integer.parseInt(initTime[1]),
            Integer.parseInt(initTime[2]));
        now1.add(Calendar.SECOND, 15);
        String changedSec =
            now1.get(Calendar.YEAR)

            + "/" + now1.get(Calendar.MONTH) + "/"

            + now1.get(Calendar.DAY_OF_MONTH) + " " + now1.get(Calendar.HOUR) + ":"
                + now1.get(Calendar.MINUTE) + ":" + now1.get(Calendar.SECOND);

        System.out.println("changed time:" + now1.get(Calendar.SECOND));
        System.out.println(initialTime + "~" + changedSec);
        return (initialTime + "~" + changedSec);

    }


    public static boolean checkBasicMetricsExists(CLWBean clw, String basicMetric) {
        boolean metricExist = false;
        try {
            metricExist = checkMetricExists(basicMetric, clw);
            LOGGER.info("ApmbaseUtil.checkBasicMetricsExists(CLWBean clw, String basicMetric): "
                + metricExist);
        } catch (Exception e) {
            LOGGER.error("Failed to check metric: ", e);
        }
        return metricExist;
    }


    /**
     * Common method to check if metrics exists
     * 
     * @param clw
     *        - CLWBean created when user logs into WS
     */
    public static boolean checkBasicMetricsExists(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir) {
        boolean metricExist = false;
        try {
            Set<String> outputSet =
                clwCommon.getuniqueMetricPaths(user, password, agentExpression, metricExpression,
                    host, port, emLibDir);

            metricExist = !(outputSet.isEmpty());

            LOGGER.info("ApmbaseUtil.checkBasicMetricsExists(CLWBean clw, String basicMetric): "
                + metricExist);
        } catch (Exception e) {
            LOGGER.error("Failed to check metric: ", e);
        }
        return metricExist;
    }


    /**
     * This method is used to check the metric exists or not in the
     * Investigator.
     * 
     * @param metric
     *        //To pass a metric.
     * @param clw
     *        //To pass a clw bean
     */
    public static boolean checkMetricExists(String metric, CLWBean clw) {
        LOGGER.info("Entering ApmbaseUtil.checkMetricExistsNew(String metric, CLWBean clw)");
        LOGGER.info("metric: " + metric);
        LOGGER.info("clw: " + clw);

        try {
            AbstractMetricsUtil metricUtil = AbstractMetricsUtilFactory.create(clw);
            long start = System.currentTimeMillis();
            while (true) {
                if (metricUtil.metricExists(metric)) {
                    return true;
                }
                Util.sleep(5000);// sleep 5 seconds
                if (System.currentTimeMillis() - start > 60000) {
                    return false;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to check the metric: ", ex);
        } finally {
            LOGGER.info("Leaving ApmbaseUtil.checkMetricExistsNew(String metric, CLWBean clw)");
        }
        return false;
    }

    public static boolean checkMetricExists(String domain, String agentHost, String process,
        String agent, String metric, CLWBean clw) {
        LOGGER
            .info("Entering ApmbaseUtil.checkMetricExists(String domain, String agentHost, String process, String agent, String metric, CLWBean clw)");
        LOGGER.info("domain: " + domain);
        LOGGER.info("agentHost: " + agentHost);
        LOGGER.info("process: " + process);
        LOGGER.info("agent: " + agent);
        LOGGER.info("metric: " + metric);
        LOGGER.info("clw: " + clw);

        try {
            AbstractMetricsUtil metricUtil = AbstractMetricsUtilFactory.create(clw);

            long start = System.currentTimeMillis();
            while (true) {
                if (metricUtil.metricExists(domain, agentHost, process, agent, metric)) {
                    return true;
                }
                Util.sleep(5000);// sleep 5 seconds
                if (System.currentTimeMillis() - start > 60000) {
                    return false;
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Unable to check the metric: ", ex);
        } finally {
            LOGGER
                .info("Leaving ApmbaseUtil.checkMetricExists(String domain, String agentHost, String process, String agent, String metric, CLWBean clw)");
        }
        return false;
    }



    /**
     * Method to check Investigator tree transaction trace
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @param logMessage
     * @return
     */

    public static boolean checkInvestigatorTree(String user, String password, String expression,
        String host, int port, String emLibDir, String logMessage) {

        List<String> transactions =
            new CLWCommons().getTranscationTraces(user, password, expression, host, port, emLibDir);
        String str;
        boolean flag = false;

        Iterator<String> it = transactions.iterator();

        while (it.hasNext()) {
            str = it.next();
            if (str.contains(logMessage)) {
                flag = true;
                break;
            }
        }


        return flag;
    }

    /**
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @param logMessage
     * @return
     */

    public static boolean checkListAgentsQuery(String user, String password, String expression,
        String host, int port, String emLibDir, String logMessage) {
        System.out.println("Inside    checkListAgentsQuery() ");
        List<String> transactions =
            new CLWCommons().getNodeList(user, password, expression, host, port, emLibDir);

        String str;
        boolean flag = false;

        Iterator<String> it = transactions.iterator();

        while (it.hasNext()) {
            str = it.next();
            if (str.contains(logMessage)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * Private method to create backup. The method will add "bak" to the
     * filename
     * 
     * @param filePath
     */
    public static String fileBackUp(String filePath) {
        LOGGER.info("Entering ApmbaseUtil.fileBackUp(String)");
        LOGGER.info("FileName: " + filePath);

        try {
            String bakFileName =
                filePath.substring(0, filePath.lastIndexOf(".")) + "bak"
                    + filePath.substring(filePath.lastIndexOf("."));
            LOGGER.info("Backup file name: " + bakFileName);

            File file = new File(filePath);
            File bakFile = new File(bakFileName);
            FileUtils.copy(file, bakFile);
            if (bakFile.exists()) {
                LOGGER.info("Backup file created!");
                return SUCCESS_MESSAGE;
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while creating back up: ", e);
        } finally {
            LOGGER.info("Leaving ApmbaseUtil.fileBackUp(String)");
        }
        return FAILURE_MESSAGE;
    }

    /**
     * Private Method to delete the given file and rename the "bak" file back to
     * original
     * 
     * @param filePath
     */
    public static String revertFile(String filePath) {
        try {
            File file = new File(filePath);
            file.delete();
            String bakFileName =
                filePath.substring(0, filePath.lastIndexOf(".")) + "bak"
                    + filePath.substring(filePath.lastIndexOf("."));
            File bakFile = new File(bakFileName);
            file = new File(filePath);
            bakFile.renameTo(file);
            if (file.exists()) {

                System.out.println("Backup Renamed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SUCCESS_MESSAGE;
    }


    /**
     * This is to verify the output message of the command
     * 
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public static boolean checkTranscationTraces(String user, String password, String expression,
        String host, int port, String emLibDir, String logMessage) {

        List<String> transactions =
            clwCommon.getTranscationTraces(user, password, expression, host, port, emLibDir);
        String str;
        boolean flag = false;

        Iterator<String> it = transactions.iterator();

        while (it.hasNext()) {
            str = it.next();
            if (str.contains(logMessage)) {
                flag = true;
                LOGGER.info("Match Found - In transaction traces");
                break;
            }
        }


        return flag;
    }


    /**
     * Method to check TranscationTraces and verify for the message in
     * IntroscopeEnterpriseManager.log
     * 
     * @param clwBean
     * @param logMessage
     */
    public static boolean checkTranscationTraces(CLWBean clwBean, String logMessage) {
        System.out.println("Inside checkTranscationTraces() ");
        System.out.println("checkTranscationTraces :CLW" + clwBean);
        boolean result = false;
        String clwOutputStrings[] = (String[]) null;

        String command =
            (new StringBuilder(
                "trace transactions exceeding 1 ms in agents matching (.*) for 60 seconds"))
                .toString();
        try {
            clwOutputStrings = clwBean.runCLW(command);
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (clwOutputStrings != null) {
            System.out.println("clwOutputStrings length:" + clwOutputStrings.length);

            for (int i = 0; i < clwOutputStrings.length; i++) {
                System.out.println("clwOutputStrings[]" + i + " : " + clwOutputStrings[i]);
                if (clwOutputStrings[i].contains(logMessage)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * This method is to forward the EM machine clock to given number of days
     *
     * @params daysToAdd
     */
    public static boolean setEmClock(String daysToAdd) throws Exception {

        boolean match = true;

        Calendar now = Calendar.getInstance();

        int days = Integer.parseInt(daysToAdd);

        LOGGER.info("Current day : " + now.get(Calendar.DAY_OF_MONTH) + "/"
            + now.get(Calendar.MONTH) + "/" + now.get(Calendar.YEAR));

        now.add(Calendar.DAY_OF_MONTH, +days);


        List<String> dateArgs = new ArrayList<>();


        LOGGER.info("The platfrom Type is " + platform);

        if (platform.contains("Windows")) {
            String changeDate =
                now.get(Calendar.DAY_OF_MONTH) + "/" + now.get(Calendar.MONTH) + "/"
                    + now.get(Calendar.YEAR);
            dateArgs.add("date " + changeDate);

        }
        if (platform.contains("Linux")) {
            String changeDate =
                (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.DAY_OF_MONTH) + "/"
                    + now.get(Calendar.YEAR);
            dateArgs.add("date -s \"" + changeDate + "\"");
        }

        LOGGER.info("The Date to be changed is " + dateArgs.get(0));
        invokeProcessBuilder(dateArgs, "/");

        // it takes around 10 sec for the log message to come so a wait
        Util.sleep(30000);

        Calendar now1 = Calendar.getInstance();

        LOGGER.info("Changed date DD/MM/YYYY:" + now1.get(Calendar.DAY_OF_MONTH) + "/"
            + (now1.get(Calendar.MONTH) + 1) + "/" + now1.get(Calendar.YEAR));
        int milliseconds1 = (int) now.getTimeInMillis();
        int milliseconds2 = (int) now1.getTimeInMillis();
        int diff = milliseconds1 - milliseconds2;
        int diffDays = diff / (24 * 60 * 60 * 1000);
        LOGGER.info("difference" + diffDays);
        if ((days - 1) == diffDays) {
            match = true;
        } else {
            match = false;
        }
        return match;
    }

    /**
     * This method is to check log for multiple log messages in a single line
     * 
     * @param emLogpath
     *        -to pass the path of the log file
     * @param logMessage
     *        -log message to be checked in log
     */

    public static boolean checkMultipleMsgsInSingleLine(String emLogpath, String logMessage) {
        /*
         * logger.info("##########checkMultipleMsgsInSingleLine Start##########")
         * ; logger.info("emLogpath:" + "" + emLogpath);
         * logger.info("logMessage:" + "" + logMessage);
         */
        System.out.println("emLogpath:" + "" + emLogpath);
        System.out.println("logMessage:" + "" + logMessage);
        boolean logfound = false;
        File f = new File(emLogpath);
        try {
            String[] messages = logMessage.split("~");
            int i = 0;
            int numberOfTimes = 0;
            while (true) {
                String line = "";
                BufferedReader br1 = null;
                try {
                    br1 = new BufferedReader(new FileReader(f));
                    // logger.info("Sleeping for a minute");
                    System.out.println("Sleeping for 30 secs");
                    Util.sleep(30000);
                    while ((line = br1.readLine()) != null) {
                        int j = 0;
                        while ((j < messages.length) && line.trim().contains(messages[j].trim())) {
                            numberOfTimes++;
                            j++;
                        }
                        if (numberOfTimes == messages.length) {
                            logfound = true;
                            break;
                        } else {
                            numberOfTimes = 0;
                            j = 0;
                            logfound = false;
                        }
                    }

                } catch (Exception e) {
                    LOGGER.error("IO Exception occurred: ", e);
                    throw e;
                } finally {
                    if (br1 != null) {
                        try {
                            br1.close();
                        } catch (Exception e) {
                            LOGGER.error("Failed to close stream: ", e);
                            throw e;
                        }
                    }
                }

                if ((i > 5) || logfound == true) {
                    if (i == 5) {
                        // logger.info("log not found");
                        System.out.println("log not found");
                        break;
                    } else {
                        // logger.info("logfound:" + line);
                        System.out.println("logfound:" + line);
                        break;
                    }
                }
                i++;
            }
        } catch (Exception e) {
            // logger.error("Unable to check the metric" + e.getMessage());
            Assert.fail("Unable to check the metric" + e.getMessage());
        }
        // logger.info("##########checkMultipleMsgsInSingleLine End##########");
        return logfound;
    }

    /**
     * This method is used to check the metric exists or not in the
     * Investigator.
     * 
     * @param metric
     *        //To pass a metric.
     * @param clw
     *        //To pass a clw bean
     */
    public static boolean checkMetricExists(String user, String password, String agentExpression,
        String metricExpression, String host, int port, String emLibDir) {
        LOGGER.info("Entering ApmbaseUtil.checkMetricExistsNew(String metric, CLWBean clw)");

        try {
            long start = System.currentTimeMillis();
            while (true) {
                if (checkBasicMetricsExists(user, password, agentExpression, metricExpression,
                    host, port, emLibDir)) return true;
                sleep(5000);
                if (System.currentTimeMillis() - start > 60000) {
                    return false;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to check the metric: ", ex);
        } finally {
            LOGGER.info("Leaving ApmbaseUtil.checkMetricExistsNew(String metric, CLWBean clw)");
        }
        return false;
    }

    public static boolean checkMetricExists(String domain, String agentHost, String process,
        String user, String password, String agentExpression, String metricExpression, String host,
        int port, String emLibDir) {
        LOGGER
            .info("Entering ApmbaseUtil.checkMetricExists(String domain, String agentHost, String process, String agent, String metric, CLWBean clw)");
        LOGGER.info("domain: " + domain);
        LOGGER.info("agentHost: " + agentHost);
        LOGGER.info("process: " + process);
        LOGGER.info("agent: " + agentExpression);
        LOGGER.info("metric: " + metricExpression);

        try {
            long start = System.currentTimeMillis();
            while (true) {
                if (checkBasicMetricsExists(user, password, agentExpression, metricExpression,
                    host, port, emLibDir)) return true;
                sleep(5000);
                if (System.currentTimeMillis() - start > 60000) {
                    return false;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to check the metric: ", ex);
        } finally {
            LOGGER.info("Leaving ApmbaseUtil.checkMetricExistsNew(String metric, CLWBean clw)");
        }
        return false;
    }

    /**
     * Method to Start HVR Agent and return the integer value 0 or 1
     * 
     * @param agentPath
     */
    public static Process startHVRAgentWithReturn(String agentPath) {
        // logger.info("##########startHVRAgentWithReturn Start##########");
        // logger.info("Inside startHVRAgent- Agent Path: " + agentPath);
        System.out.println("Inside startHVRAgent- Agent Path: " + agentPath);
        Process process = null;
        try {
            String[] commands = {"cmd.exe", "/c", ApmbaseConstants.HVR_AGENT_BAT};
            process = runCommand(Arrays.asList(commands), agentPath);
            // logger.info("sleeping for a minute");
            System.out.println("sleeping for half a minute");
            Util.sleep(30000);
            checkConsoleOutput(process, "FakeAgent");
        } catch (Exception e) {
            // logger.error("Unable to start the HVR Agent" + e.getMessage());
            System.out.println("Unable to start the HVR Agent" + e.getMessage());
        }
        // logger.info("##########startHVRAgentWithReturn End##########");
        return process;
    }

    /**
     * Method to Stop HVR Agent and return the process
     * 
     */
    public static Process stopHVRAgentWithReturn() {
        // logger.info("##########stopHVRAgentWithReturn Start##########");
        Process process = null;
        try {
            String[] commands =
                {"cmd.exe", "/c",
                        "wmic Path win32_process Where \"CommandLine Like \'%cloneagents%\'\" Call Terminate"};
            process = runCommand(Arrays.asList(commands), "C:/");
            // logger.info("sleeping for a minute");
            System.out.println("sleeping for half a minute");
            Util.sleep(30000);
        } catch (Exception e) {
            // logger.error("Unable to stop the HVR Agent" + e.getMessage());
            System.out.println("Unable to stop the HVR Agent" + e.getMessage());
        } finally {
            killProcess(process);
        }
        // logger.info("##########stopHVRAgentWithReturn End##########");
        return process;
    }


    /**
     * This method is to rename the file
     * 
     * @param path
     * @param fileName
     * @throws Exception
     */
    public static void renameRealmsFile(String path, String fileName) throws Exception {
        File file = new File(path + "/" + fileName);
        file.renameTo(new File(path + "/" + "realms.xml"));
    }

    public static class SkipLineResult {
        private boolean foundLog;
        private int newLineCount;

        public SkipLineResult(boolean foundLog, int newLineCount) {
            this.foundLog = foundLog;
            this.newLineCount = newLineCount;

        }

        public boolean foundCheck(boolean foundLog) {
            return this.foundLog;

        }

        public int lineCount(int newLineCount) {
            return this.newLineCount;
        }

    }

    /**
     * Deleting folders /logs and /data/archive
     */
    public static void doCleanUp() {
        LOGGER.info("Doing a clean up ...");
        try {
            ApmbaseUtil.deleteDir(new File(ApmbaseConstants.EM_LOC + File.separator
                + ApmbaseConstants.LOG_PATH_LOC));
            ApmbaseUtil.deleteDir(new File(ApmbaseConstants.EM_LOC + ApmbaseConstants.EM_DATA_LOC
                + File.separator + "archive"));
        } catch (IOException e) {
            LOGGER.error("Error while cleaning up: " + e.getLocalizedMessage());
        }

    }

    /**
     * Method to check Investigator tree transaction trace
     * 
     * @param clwBean
     * @param logMessage
     */
    public static boolean checkInvestigatorTree(CLWBean clwBean, String logMessage, String emLogFile) {
        System.out.println("Inside    checkInvestigatorTree() ");
        System.out.println("checkInvestigatorTree :CLW" + clwBean);
        boolean result = false;
        String clwOutputStrings[] = (String[]) null;

        String command =
            (new StringBuilder(
                "trace transactions exceeding 1 ms in agents matching (.*) for 60 seconds"))
                .toString();
        try {
            clwOutputStrings = clwBean.runCLW(command);
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (clwOutputStrings != null) {
            System.out.println("clwOutputStrings length:" + clwOutputStrings.length);

            for (int i = 0; i < clwOutputStrings.length; i++) {
                System.out.println("clwOutputStrings[]" + i + " : " + clwOutputStrings[i]);
            }
        }
        try {
            result = ApmbaseUtil.checkValidLastUpdate(emLogFile, logMessage);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    /**
     * This method is used to check agents query
     * 
     * @param clwBean
     * @param logMessage
     * @return
     */
    public static boolean checkListAgentsQuery(CLWBean clwBean, String logMessage, String emLogFile) {
        System.out.println("Inside    checkListAgentsQuery() ");
        System.out.println("checkListAgentsQuery :CLW" + clwBean);
        boolean result = false;
        String clwOutputStrings[] = (String[]) null;

        String command = (new StringBuilder("list agents matching (.*)")).toString();
        try {
            clwOutputStrings = clwBean.runCLW(command);
        } catch (Exception e) {

            e.printStackTrace();
        }
        if (clwOutputStrings != null) {
            System.out.println("clwOutputStrings length:" + clwOutputStrings.length);

            for (int i = 0; i < clwOutputStrings.length; i++) {
                System.out.println("clwOutputStrings[]" + i + " : " + clwOutputStrings[i]);
            }
            
            if(clwOutputStrings.length >0)
            {
            try {
                result =
                    ApmbaseUtil.checkValidLastUpdate(emLogFile + "/"
                        + ApmbaseConstants.INT_EM_MAN_FILE_NAME, logMessage);
            } catch (Exception e) {
                e.printStackTrace();

            }
            }
        }
        
        return result;
    }
    
    /**
     *  This method returns the ResponseTime of the CLWQuery
     *   by getting the latest data from the queryLog 
     *  
     */	
    
 	public int queryrunTime(String queryLogPath) throws Exception {
 		ArrayList<String> text = utility.readFilefromLast(queryLogPath); 		
 		String Val =text.get(2);
 		System.out.println("Value:"+Val);
 		
 		int startIndex = Val.indexOf("<ResponseTimeInMs>");
 		System.out.println("StartIndex:"+startIndex);
 		
 		int lastIndex = Val.lastIndexOf("</ResponseTimeInMs>");
 		System.out.println("Last Index:"+lastIndex);
 		
 		String subString1 = Val.substring(startIndex+18, lastIndex);
 		System.out.println("Required Val: "+ subString1);
 		int timeVal = Integer.parseInt(subString1);
 		LOGGER.info("Time value is "+timeVal);
 		return timeVal;
 	}
 	
 	/**
 	 * Check if the provided EM list is part of the latest EM list of the file specified
 	 * @param filePath
 	 * @param emList
 	 * @return boolean
 	 */
 	public boolean checkEMListContents(String filePath, List<String> emList){
		
		String substr = "New list {";
		String line = null;
		List<String> emListFromFile = new ArrayList<String>();
		
		
		try {
			//Get the latest EM list entry in the file
			line = utility.lineWithSubString(filePath, substr);
			LOGGER.info("The line containing the latest EM list is ##  " + line);
			//Get the String that contains the EM list and split for EMs list
			String temp = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
			LOGGER.info("The extracted List String is ###" + temp);
			emListFromFile = Arrays.asList(temp.split("\\s*,\\s*"));
			LOGGER.info("The expected array is " + emList);
			LOGGER.info("The actual array is " + emListFromFile);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return(emListFromFile.containsAll(emList));
    }
 	
 	
 	/**
 	 * Check if the latest EM list of the file specified matches the size of list 
 	 * @param filePath
 	 * @param emList
 	 * @return
 	 */
 	public boolean checkEMListSize(String filePath, Integer emListSize){
		
		String substr = "New list {";
		String line = null;
		List<String> emListFromFile = new ArrayList<String>();
		
		
		try {
			//Get the latest EM list entry in the file
			line = utility.lineWithSubString(filePath, substr);
			LOGGER.info("The line containing the latest EM list is ##  " + line);
			//Get the String that contains the EM list and split for EMs list
			String temp = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
			LOGGER.info("The extracted List String is ###" + temp);
			emListFromFile = Arrays.asList(temp.split("\\s*,\\s*"));
//			System.out.println("The expected array is " + emListFromFile);
			LOGGER.info("The latest EM list of the file has" + emListFromFile.size() + " elements");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return(emListSize.equals(emListFromFile.size()));
    }
 	
 	 /**
	     * Test method to check user is able to login by 
		 * creating a CLWBean and check if Agent Exists and also check for permission
		 *  
		 * @param emhost
		 *            - Hostname on which EM is setup
		 * @param emport
		 *            - PortNumber on which EM is setup
		 * @param userName
		 *            - WS Username
		 * @param password
		 *            - WS password
		 * @param clwJarFileLoc
		 * 			  - em clw jar file location
		 * @param emLogDir
		 *            - path to the em logs dir
		 * @param logMessage
		 *            - message to check in em log
		 */
		
		public boolean validateUserLoginWithCheckingAgents(String emhost, String emport,
				String userName, String password,String clwJarFileLoc,
				String emlogDir,String logMessage ) {
			System.out.println(" ***** CLW object parameters: *** emhost: "
					+ emhost + " userName: " + userName + " empassw: "
					+ password + "emport:" + emport
					+ " Location CLW Jar file: " + clwJarFileLoc);

			CLWBean clw = new CLWBean(emhost, userName, password,
					Integer.parseInt(emport), clwJarFileLoc);
			System.out.println("CLW Bean created:" + clw);
			
			boolean metricsExists = ApmbaseUtil.checkListAgentsQuery(clw,logMessage, emlogDir);
			return metricsExists;
			
		}
		
		/**
	     * This method is to check for SuperDomain node in domains.xml
	     * and also check its default agentmapping value
	     * @param domainsXMLFilePath
	     *            - Domains.xml file path     */
	    
	    public void chkforSuperDomainNode(String domainsXMLFilePath)
	    {
	        try {
	            Document document = XMLUtil.getDocument(domainsXMLFilePath);
	            /** getting users element/node */
	            Node agentNode = document.getElementsByTagName("agent").item(0);
	            NamedNodeMap nodesMap = agentNode.getAttributes();
	            Node mappingNode = nodesMap.getNamedItem("mapping");
	            Assert.assertEquals(mappingNode.getTextContent().trim(), "(.*)");

	        } catch (Exception e) {
	            Assert.fail("Failed while inserting new user element.");
	        }
	    } 	    
		
	    /**
         * Method to encrypt password
         * 
         * @param emToolsDirLocation
         *        - location of the EM directory
         * 
         * @param plainPassword
         *        - command to be executed
         * 
         * 
         */
        public String encryptPassword(String emToolsDirLocation, String plainPassword) {
            String encryptedPassword = "";
            String enCoderWithPasswd="";
            try {
                if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                    enCoderWithPasswd=ApmbaseConstants.SHA2ENCODER+".bat " +plainPassword;
                else
                    enCoderWithPasswd="./"+ApmbaseConstants.SHA2ENCODER+".sh "+plainPassword;
                
                encryptedPassword = runCommand(emToolsDirLocation, enCoderWithPasswd);
            } catch (IOException e) {
                LOGGER.info("Unable to execute sha command......");
                Assert.assertTrue(false);
            }
            return encryptedPassword;

        }

        /**
         * Test method to run bat file
         * 
         * @param directoryLocation
         *        - location
         * @param Password to encrypt
         *        - command to be executed

         */
        private String runCommand(String directoryLocation, String enCoderWithPasswd)
            throws IOException {
            BufferedReader reader = null;
            Process process = null;
            String messageFound = "";
            try {
                LOGGER.info("Command being executed is " + enCoderWithPasswd);
                String[] startCmnd = {enCoderWithPasswd};
                process = ApmbaseUtil.getProcess(startCmnd, directoryLocation);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(enCoderWithPasswd.split(" ")[1])) {
                        String[] encryptedPass = line.split(":");
                        if (encryptedPass.length >= 1) {
                            messageFound = encryptedPass[1];
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("unable to run the command......");
                Assert.assertTrue(false);
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (process != null) {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    process.destroy();
                }
            }
            LOGGER.info("Value of messageFound is:" + messageFound);
            return messageFound;
        }   

        
    public boolean enableAllMMonEM(String emHome){
    		boolean copied = false;
    		int copyFailedFiles = 0;
    		
    		File examplesFolder = new File(emHome + "/examples");
    		if(!examplesFolder.isDirectory()){
    			LOGGER.info("No Examples directory in the given EM Home");
    			return copied;
    		}
    		
    		LOGGER.info("Copying the Management Module jar files to " + emHome + " config directory from examples");
    		
    		File[] exampleFolderContents = examplesFolder.listFiles();
    		for (File file : exampleFolderContents) {
    			
    			if (file.isDirectory()) {
    				
    				File tempModuleDir = new File(file.getAbsolutePath() + "/config/modules/");
    				
    				if (tempModuleDir.isDirectory()) {
    					
    					File[] mmJars = tempModuleDir.listFiles();
    					for (File mmFile : mmJars) {
    						
    						if(! copyFile(mmFile.toString(),emHome + "/config/modules/" + mmFile.getName())){
    							copyFailedFiles ++;
    						}
    						
    					}
    				}
    				
    			}
    			
    		}
    		
    		copied = copyFailedFiles > 0 ? false : true;
    		return copied;
    	}
    
    /**
	 * This method returns the count of open ports
	 * 
	 * @param command
	 * 
	 * @return openPortCount
	 */
	public int OpenPortsCount() {
		String listOpenPortsCmd = platform.toUpperCase().contains("WINDOWS") ? "netstat -aon | find /C /i \"listening\""
				: "netstat -aon | grep -i \"listening\" |wc -l ";
		Process process = null;
		String[] commands = { listOpenPortsCmd };
		BufferedReader reader = null;
		try {
			process = ApmbaseUtil.getProcess(commands, "/");
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

		if (process == null)
			return 0;
		try {
			reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			LOGGER.info("The Count of Ports Opened are " + line);
			return Integer.parseInt(line.trim());
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			process.destroy();
		}

	}
}
