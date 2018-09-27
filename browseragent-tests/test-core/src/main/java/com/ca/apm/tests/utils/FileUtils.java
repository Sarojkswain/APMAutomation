/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 */

/**
 * Browser Agent File manipulation utility class
 *
 * @author Legacy Browser Agent automation code
 *         Updates for TAS by gupra04
 */

package com.ca.apm.tests.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Properties;

// import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    public String destFilePath;

    public String srcFilePath;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Update the properties of a file in a windows remote machine
     * 
     * @param hostname
     * @param username
     * @param password
     * @param key
     * @param value
     * @param filepath
     * 
     *        Example Usage of this method:
     *        FileUtils.changeRemotePropertiesFile(AgentMachinehostname, AgentMachineusername,
     *        AgentMachinepassword, "introscope.agent.brt.urlgroup.keys" , "delta",
     *        "\\apache-tomcat-7.0.30\\wily\\core\\config\\IntroscopeAgent.profile");
     *        Works for Windows OS only
     *        Known Issue(to be fixed): This works only for the machines with C drive as the default
     *        HOME DRIVE.
     */

    public static void changeRemotePropertiesFile(String hostname, String username,
        String password, String key, String value, String filepath) {

        // Commenting the below as this was already called in testng @BeforeClass setUp() method
        // If this was not called during setup(), then this should be uncommented
        // invokeProcess("net use \\\\"+hostname+"\\C$ /USER:"+username+" "+password);
        // logger.info("Updating property "+key+" to "+value+" in: \\\\"+hostname+"\\C$"+filepath);
        LOGGER.info("Updating property " + key + " to " + value + " in: " + filepath);

        // updateProperties(key, value, "\\\\"+hostname+"\\C$"+filepath);
        updateProperties(key, value, filepath);

        // Commenting the below as this was used in testng @AfterClass setUp() method
        // invokeProcess("net use /delete \\\\"+hostname+"\\C$");
    }

    /**
     * Update the properties of a file in a windows remote machine - to delete a property
     * 
     * @param hostname
     * @param username
     * @param password
     * @param key
     * @param filepath
     * 
     *        Example Usage of this method:
     *        FileUtils.deletePropertyFromRemotePropertiesFile(AgentMachinehostname,
     *        AgentMachineusername, AgentMachinepassword, "introscope.agent.brt.urlgroup.keys" ,
     *        "\\apache-tomcat-7.0.30\\wily\\core\\config\\IntroscopeAgent.profile");
     *        Works for Windows OS only
     *        Known Issue(to be fixed): This works only for the machines with C drive as the default
     *        HOME DRIVE.
     */

    public static void deletePropertyFromRemotePropertiesFile(String hostname, String username,
        String password, String key, String filepath) {
        // logger.info("Removing property "+key+" in: \\\\"+hostname+"\\C$"+filepath);

        LOGGER.info("Removing property " + key + " in: " + filepath);
        // removeProperty(key, "\\\\"+hostname+"\\C$"+filepath);
        removeProperty(key, filepath);

    }


    /**
     * Get the property of a file in a windows remote machine - to retrieve a property
     * 
     * @param hostname
     * @param username
     * @param password
     * @param key
     * @param filepath
     * 
     *        Example Usage of this method:
     *        FileUtils.getPropertyFromRemotePropertiesFile(AgentMachinehostname,
     *        AgentMachineusername, AgentMachinepassword, "introscope.agent.brt.urlgroup.keys" ,
     *        "\\apache-tomcat-7.0.30\\wily\\core\\config\\IntroscopeAgent.profile");
     *        Works for Windows OS only
     *        Known Issue(to be fixed): This works only for the machines with C drive as the default
     *        HOME DRIVE.
     */

    public static String getPropertyFromRemotePropertiesFile(String hostname, String username,
        String password, String key, String filepath) {
        // logger.info("Retrieving property "+key+" in: \\\\"+hostname+"\\C$"+filepath);
        LOGGER.info("Retrieving property " + key + " in: " + filepath);

        // return (getProperty(key, "\\\\"+hostname+"\\C$"+filepath));
        return (getProperty(key, filepath));

    }

    // never used
    // TODO: CLEAN UP IF NOT NEEDED
    public static void changeRemoteBRTMPblFile(String hostname, String username, String password,
        String key, String value, String pblfilepath) throws IOException {

        // Commenting the below as this was already called in testng @BeforeClass setUp() method
        // If this was not called during setup(), then this should be uncommented
        // invokeProcess("net use \\\\"+hostname+"\\C$ /USER:"+username+" "+password);
        LOGGER.info("Replacing property " + key + " to " + value + " in: \\\\" + hostname + "\\C$"
            + pblfilepath);
        String str = "\\\\" + hostname + "\\C$" + pblfilepath;
        replaceProperty(key, value, str);

        // Commenting the below as this was used in testng @AfterClass setUp() method
        // invokeProcess("net use /delete \\\\"+hostname+"\\C$");
    }

    public static void replaceProperty(String key, String value, String pbltokenreplace)
        throws IOException {
        File file = new File(pbltokenreplace);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", oldtext = "";
        String newline = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            if (line.equals(key)) {
                line = value;
                oldtext = oldtext + line + newline;
            } else {
                oldtext = oldtext + line + newline;
            }
        }
        reader.close();
        FileWriter writer = new FileWriter(pbltokenreplace);
        BufferedWriter bbr = new BufferedWriter(writer);
        bbr.write(oldtext);
        bbr.close();

    }

    /**
     * Create a .orig copy of the specified file in the same location
     * 
     * @param filePath
     */
    public static void createBackupFile(String filePath) {
        String strNewName = filePath + ".orig";
        File f1 = new File(filePath);
        File f2 = new File(strNewName);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f1);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {
                textinLine = br.readLine();
                if (textinLine == null) break;
                sb.append(textinLine);
                sb.append(System.getProperty("line.separator"));
            }
            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fstream = new FileWriter(f2);
            BufferedWriter outobj = new BufferedWriter(fstream);
            outobj.write(sb.toString());
            outobj.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }


    /**
     * Restore the .orig copy of the specified file in the same location
     * 
     * @param filePath
     */
    public static void restoreBackupFile(String filePath) {
        String strNewName = filePath + ".orig";
        File f1 = new File(strNewName);
        File f2 = new File(filePath);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f1);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {
                textinLine = br.readLine();
                if (textinLine == null) break;
                sb.append(textinLine);
                sb.append(System.getProperty("line.separator"));
            }
            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fstream = new FileWriter(f2);
            BufferedWriter outobj = new BufferedWriter(fstream);
            outobj.write(sb.toString());
            outobj.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    /**
     * Update a line in a specified file. Saves a copy of the original file with a .orig extension
     * 
     * @param origString
     * @param newString
     * @param filePath
     * @param partialMatch - if true, it looks for a substring to match and replaces the entire line
     *        with the new string. If false, it finds the complete string and replaces with the new
     *        string.
     */
    public static void updateFile(String origString, String newString, String filePath,
        boolean partialMatch) {
        File f = new File(filePath);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {
                textinLine = br.readLine();
                if (textinLine == null) break;
                sb.append(textinLine);
                sb.append(System.getProperty("line.separator"));
            }
            int cnt1 = sb.indexOf(origString);
            int cnt2 = sb.indexOf(System.getProperty("line.separator"), cnt1);

            if (partialMatch)
                sb.replace(cnt1, cnt2 - 1, newString);
            else
                sb.replace(cnt1, cnt1 + origString.length(), newString);

            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fstream = new FileWriter(f);
            BufferedWriter outobj = new BufferedWriter(fstream);
            outobj.write(sb.toString());
            outobj.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }


    /**
     * Update a line in memory.
     * 
     * @param origString
     * @param newString
     * @param filePath
     */
    public static String updateFileContentInMemory(String origString, String newString,
        String filePath) {
        File f = new File(filePath);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {
                textinLine = br.readLine();
                if (textinLine == null) break;
                sb.append(textinLine);
                sb.append(System.getProperty("line.separator"));
            }
            int cnt1 = sb.indexOf(origString);
            sb.replace(cnt1, cnt1 + origString.length(), newString);

            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("File contents are:");
        LOGGER.info(sb.toString());
        return sb.toString();


    }

    /**
     * Return the contents of the file as a String
     */
    public static boolean verifyFileContent(String filePath, String message) {
        File f = new File(filePath);
        boolean result = false;
        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {

                textinLine = br.readLine();
                if (textinLine == null) break;
                if ((!(textinLine == null) && textinLine.toLowerCase().contains(
                    message.toLowerCase()))) {
                    LOGGER.info("Text: " + message + " exist in " + filePath + " at " + textinLine);
                    result = true;
                }

            }
            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * Return the contents of the file as a String
     */
    public static String getFileContent(String filePath) {
        File f = new File(filePath);

        FileInputStream fs = null;
        InputStreamReader in = null;
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        String textinLine;

        try {
            fs = new FileInputStream(f);
            in = new InputStreamReader(fs);
            br = new BufferedReader(in);

            while (true) {
                textinLine = br.readLine();
                if (textinLine == null) break;
                sb.append(textinLine);
                sb.append(System.getProperty("line.separator"));
            }

            fs.close();
            in.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("File contents are:");
        LOGGER.info(sb.toString());
        return sb.toString();

    }


    /**
     * Update a property in a property file
     * 
     * @param key
     * @param value
     * @param filepath
     */
    public static void updateProperties(String key, String value, String filepath) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(new File(filepath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String oldValue = properties.getProperty(key);
        properties.setProperty(key, value);
        try {
            properties.store(new FileOutputStream(filepath), null);
        } catch (FileNotFoundException e) {
            LOGGER.info("**** FILE NOT FOUND **** " + filepath);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.info("*** Unable to Save File ***** " + filepath);
            e.printStackTrace();
        }
        String newValue = properties.getProperty(key);
        if (!value.equals(null) && newValue.equals(value)) {
            LOGGER.info("Property " + key + " updated successfully from " + oldValue + " to "
                + newValue);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Remove a property from the property file
     * 
     * @param key
     * @param filepath
     */
    public static void removeProperty(String key, String filepath) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(new File(filepath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.remove(key);
        try {
            properties.store(new FileOutputStream(filepath), null);
        } catch (FileNotFoundException e) {
            LOGGER.info("**** FILE NOT FOUND **** " + filepath);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.info("*** Unable to Save File ***** " + filepath);
            e.printStackTrace();
        }
        if (properties.getProperty(key) == null) {
            LOGGER.info("Property " + key + " removed successfully");
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get a property from a property file
     * 
     * @param key
     * @param filepath
     */
    public static String getProperty(String key, String filepath) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(new File(filepath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String value;
        if (properties.getProperty(key) == null) {
            LOGGER.info("Property " + key + " not present, returning an empty string");
            value = "";
        } else {
            value = properties.getProperty(key);
            LOGGER.info("Property " + key + " retrieved successfully and is " + value);
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    /**
     * This method is used to invoke EM process.
     * This also checks whether the net use command is successfull or not.
     * 
     * @param command
     * @returns boolean
     */

    public static boolean invokeProcess(String command) {
        Process process = null;
        boolean result = false;
        try {
            LOGGER.info("Executing command: \n\t" + command);
            process = Runtime.getRuntime().exec(command);
            InputStream in = process.getInputStream();
            InputStream err = process.getErrorStream();
            int c;
            StringBuffer sb = new StringBuffer("");
            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }
            while ((c = err.read()) != -1) {
                sb.append((char) c);
            }
            in.close();
            LOGGER.info(sb.toString());
            LOGGER.info("Completed executing command: " + command);
            if (sb.toString().contains("The command completed successfully.")) {
                result = true;
            } else if (sb.toString().contains("System error")) {
                LOGGER
                    .info("\n\nNET USE COMMAND FAILED!!! Please check whether username and password are correct and host is reachable.\n Command: "
                        + command);
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (process != null) {
                try {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.getOutputStream().close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                };
            }
        }
        return result;
    }

    public static void createnewFile(String filename) {
        File file = new File(filename);
        boolean blnCreated = false;
        try {
            blnCreated = file.createNewFile();
            System.out.println("File " + filename + " Created.");

        } catch (IOException ioe) {
            System.out.println("Error while creating a new empty file :" + ioe);
        }
        // System.out.println("Was file " + file.getPath() + " created ? : " + blnCreated);
    }

    public static void deletefile(String filename) {
        File f1 = new File(filename);
        boolean success = f1.delete();
        if (!success) {
            System.out.println("Unable to delete" + filename);
        } else {
            System.out.println("File " + filename + " deleted.");
        }
    }

    public static void appendTextToaFile(String filename, String text) {

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            out.println(text);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Search the log file for the specified message
     * 
     * @param logFile log file path
     * @param message message string to search for
     * @return true if the message string is found. Otherwise, false.
     */
    public static boolean checkLogFile(String logFile, String message) {
        boolean bFound = false;
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(logFile));
            while ((line = br.readLine()) != null) {
                if (line.indexOf(message) != -1) {
                    bFound = true;
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bFound;
    }

    /**
     * Search for a string in a file and insert a string before or after that string.
     * 
     * @param fileToInsert path to the file to insert the string
     * @param stringToInsert the string to be inserted
     * @param stringToLookFor the string to be searched in the file
     * @param before true if stringToInsert should be inserted before stringToLookFor
     * @return String path to the original file named "filename.orig"
     * @throws IOException
     */
    public static String insertToFile(String fileToInsert, String stringToInsert,
        String stringToLookFor, boolean before) throws IOException {
        LOGGER.info("Start of insertToFile");
        String origFile = fileToInsert + ".orig";
        // Make a copy of original file
        Files.copy(Paths.get(fileToInsert), Paths.get(origFile),
            StandardCopyOption.REPLACE_EXISTING);

        try (BufferedReader br = new BufferedReader(new FileReader(origFile));
            FileWriter writer = new FileWriter(fileToInsert);) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(stringToLookFor)) {
                    if (before) {
                        writer.write(stringToInsert);
                        writer.write("\n");
                        writer.write(line);
                    } else {
                        writer.write(line);
                        writer.write("\n");
                        writer.write(stringToInsert);
                    }
                } else {
                    writer.write(line);
                }
                writer.write("\n");
            }
            LOGGER.info("End of insertToFile");
            return origFile;
        }
    }


    /*
     * public static void main(String... argv) {
     * FileUtils fu = new FileUtils();
     * String filePath =
     * "C:\\Oracle\\Middleware\\wlserver_10.3\\samples\\server\\medrec\\modules\\medrec\\assembly\\target\\snippet.js"
     * ;
     * FileUtils.createBackupFile(filePath);
     * FileUtils.updateFile("rum.async = true;", "rum.async = false;", filePath, false);
     * FileUtils.restoreBackupFile(filePath);
     * // su.stopServer(AppServerShutdownCmd);
     * // su.startServer(AppServerStartupCmd);
     * }
     */
}
