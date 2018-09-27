package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ca.apm.tests.common.Context;
import com.ca.apm.tests.common.IResponse;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.common.tasks.Task;
import com.ca.apm.tests.common.tasks.file.CopyTask;
import com.ca.apm.tests.common.tasks.file.DeleteTask;
import com.ca.apm.tests.common.tasks.file.RenameTask;
import com.ca.apm.tests.common.tasks.properties.PropertiesFileTask;

public class RegressionBaseAgentUtil

{
    private static MetricUtil  metricUtil      = null;

    public static final String SUCCESS_MESSAGE = AutomationConstants.SUCCESS_MESSAGE;

    /**
	 * Method to check the property and value from the given file
	 * 
	 * @param file_name
	 *            name of the File
	 * @param file_path
	 *            path to parent directory of the File
	 * @param prop_name
	 *            Name of property which needs to be checked
	 * @param prop_value
	 *            Value of the Property
	 * @throws Exception
	 * @return int
	 */
    // check for properties set in properties file
    public int checkproperties(String file_name,
                               String file_path,
                               String prop_name,
                               String prop_value) throws Exception
    {
        int property_set = 0;

        String file = file_path + "\\" + file_name;
        BufferedReader br1 = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br1.readLine()) != null)
        {
            String prop_line = prop_name.trim() + "=" + prop_value.trim();

            if ((line.trim().equals(prop_line)))
            {
                property_set = 1;
                System.out.println(line);

            }
        }
        return property_set;
    }

	/**
	 * Method to set values for the given properties
	 * 
	 * @param file_name
	 *            name of the File
	 * @param file_path
	 *            path to parent directory of the File
	 * @param prop_name
	 *            Name of property which needs to be updated
	 * @param prop_value
	 *            Value of the Property
	 * @return int
	 * @throws Exception
	 */
    public static int setproperties(String file_name,
                                    String file_path,
                                    String prop_name,
                                    String prop_value) throws Exception
    {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try
        {

            int property_set = 0;
            System.out.println(file_name + file_path + prop_name + prop_value);
            String NL = System.getProperty("line.separator");
            String file = file_path + "\\" + file_name;
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
            while ((line = br.readLine()) != null)
            {
                if ((line.trim().startsWith(prop_line)))
                {
                    System.out.println("--changed property --" + line);
                    String prop_set[] = line.split("\\=");
                    bw.write(prop_set[0] + "=" + prop_value + NL);
                    System.out.println(prop_set[0] + "=" + prop_value);
                    property_set = 1;
                } else
                {
                    bw.write(line + NL);
                }
            }
            bw.flush();
            Thread.sleep(3000);

            copy(temp_file, file);
            int val = 1;
            if (val == 1) property_set = 1;
            target.deleteOnExit();
            return property_set;
        } finally
        {
            if (br != null)
            {
                br.close();
            }
            if (bw != null)
            {
                bw.flush();
                bw.close();
            }
        }

    }

	/**
	 * Method to copy the file from one location to other location
	 * 
	 * @param source_file
	 *            name of the source file
	 * @param target_file
	 *            target file path
	 * @return int
	 */
    public static int file_replace(String source_file, String target_file)
    {
        File source = new File(source_file);
        File target = new File(target_file);
        int success = 0;
        try
        {
            String command2 = "cmd /c copy " + "\"" + source + "\"" + " "
                              + "\"" + target + "\"";
            System.out.println("Copying " + "\"" + source + "\"" + " file to "
                               + "\"" + target + "\"");
            @SuppressWarnings("unused")
            Process p4 = Runtime.getRuntime().exec(command2);

            success = 1;
        } catch (Exception e)
        {
            e.printStackTrace();
            success = 0;
        }
        return success;

    }

	/**
	 * Method to copy the file from one location to other location
	 * 
	 * @param src
	 *            source file location
	 * @param dst
	 *            destination file location
	 * @throws IOException
	 */
    public static void copy(String src, String dst) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            File source = new File(src);
            File target = new File(dst);
            in = new FileInputStream(source);
            out = new FileOutputStream(target);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } finally
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }

        }
    }

	/**
	 * Method to append the new properties
	 * 
	 * @param propLst
	 *            List of properties
	 * @param fileName
	 *            Name of the File
	 * @param filePath
	 *            path of the file
	 * @throws IOException
	 */
    public static void appendProperties(List<String> propLst,
                                        String fileName,
                                        String filePath) throws IOException
    {
        if (propLst == null || propLst.isEmpty()) return;
        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter(filePath + "/" + fileName, true);
            out = new BufferedWriter(fstream);
            for (String eachLine : propLst)
            {

                out.write(eachLine + "\n");
            }
            out.flush();
        } finally
        {
            if (out != null) out.close();
        }
    }

	
	/**
	 * Method to remove the properties
	 * 
	 * @param fileName
	 *            Name of the File
	 * @param filePath
	 *            path of the file
	 * @param propLst
	 *            list of properties
	 * @return int
	 * @throws Exception
	 */
    public static int removeProperties(String file_name,
                                       String file_path,
                                       List<String> propLst) throws Exception
    {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try
        {
            System.out.println("%%%%%%%%%%%%%%%%%%%");
            int property_set = 0;

            String NL = System.getProperty("line.separator");
            String file = file_path + "\\" + file_name;
            System.out.println(file);
            String name[] = file_name.split("\\.");
            String temp_file = file_path + "\\" + name[0] + "temp." + name[1];
            File source = new File(file);
            File target = new File(temp_file);
            System.out.println(temp_file);
            br = new BufferedReader(new FileReader(source));
            bw = new BufferedWriter(new FileWriter(target));
            String line = "";

            while ((line = br.readLine()) != null)
            {
                if (propLst.contains(line.trim()))
                {
                    System.out.println("--changed property --" + line);

                    property_set = 1;
                } else
                {
                    bw.write(line + NL);
                }
            }
            bw.flush();

            copy(temp_file, file);
            int val = 1;
            if (val == 1) property_set = 1;
            target.deleteOnExit();
            return property_set;
        } finally
        {
            if (br != null)
            {
                br.close();
            }
            if (bw != null)
            {
                bw.flush();
                bw.close();
            }
        }

    }

	/**
	 * Method to check when the file is last updated and for the errormessage
	 * 
	 * @param fileName
	 *            name of the file
	 * @param updatedTime
	 *            last updated time
	 * @param errorMsg
	 *            error message to check
	 * @return true
	 * @throws Exception
	 */
    public static boolean checkValidLastUpdate(String fileName,
                                               long updatedTime,
                                               String errorMsg)
        throws Exception
    {
        System.out
                .println("*******************IN Log Check********************");
        File file = new File(fileName);
        System.out.println("in fileName " + fileName + " to CHECK " + errorMsg);
        RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
        long fileLength = file.length() - 1;
        StringBuilder sb = new StringBuilder();
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.DATE, -1);
        Date previousDay = (Date) currentCal.getTime();

        for (long filePointer = fileLength; filePointer != -1; filePointer--)
        {
            fileHandler.seek(filePointer);

            int readByte = fileHandler.readByte();
            if (readByte == 0xA)
            {
                if (filePointer == fileLength)
                {
                    continue;
                }
            } else if (readByte == 0xD)
            {
                if (filePointer == fileLength - 1)
                {
                    continue;
                } else
                {
                    sb.append((char) readByte);
                    String completeString = sb.reverse().toString();

                    String[] log = completeString.split(" ");
                    if (log[0].contains("/"))
                    {
                        String dateStr = log[0]; // log[1] contains date

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                        Date logDate = (Date) sdf.parse(dateStr.trim());

                        // splitting the string to get hours, minutes separately
                        String[] logTime = log[1].split(":"); // log[1] contains
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
                        logCal.set(Calendar.MINUTE,
                                   Integer.parseInt(logTime[1])); // setting
// minutes

                        Date updateDate = new Date(updatedTime);
                        // System.out.println(updateDate);

                        // preparing calendar object 5 minutes before on passed
// calendar object(updCalendar)
                        Calendar updatCalBefore = Calendar.getInstance();
                        updatCalBefore.setTime(updateDate);
                        updatCalBefore.add(Calendar.MINUTE, -5);

                        // preparing calendar object 5 minutes after on passed
// calendar object(updCalendar)
                        Calendar updateCalAfter = Calendar.getInstance();
                        updateCalAfter.setTime(updateDate);
                        updateCalAfter.add(Calendar.MINUTE, 5);

                        if ((logCal.after(updatCalBefore) && logCal
                                .before(updateCalAfter)))
                        {
                            if (completeString.contains(errorMsg))
                            {
                                System.out.println("%%%%%%%exists%%%%%% "
                                                   + completeString);
                                return true;
                            }
                        } else if ((previousDay.equals(logCal.getTime())))
                        {

                            return false;
                        }

                    }
                    sb = new StringBuilder();

                }// end else
            }// end else 0xD

            sb.append((char) readByte);

        }// end for

        return false;
    }

	/**
	 * Method to copy the file from source to destination
	 * 
	 * @param srcFile
	 *            source file location
	 * @param destFile
	 *            destination file location
	 * @throws IOException
	 */
    public static void copyFile(File srcFile, File destFile) throws IOException
    {
        if (!srcFile.exists()) return;
        if (!destFile.exists())
        {
            String filePath = destFile.getAbsolutePath();
            int index = filePath.lastIndexOf("\\");
            String path = filePath.substring(0, index);

            File f = new File(path);
            if (!f.exists())
            {
                f.mkdirs();
            }
            destFile.createNewFile();
        }
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        } finally
        {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

	/**
	 * Method getting the property value from the properties file
	 * 
	 * @param paramName
	 *            name of the property
	 * @param fileName
	 *            name of the file
	 * @param filePath
	 *            name of the file path
	 * @return String value of the property
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public static String getPropertyValue(String paramName,
                                          String fileName,
                                          String filePath)
        throws FileNotFoundException, IOException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(filePath + "/" + fileName));
        String value = properties.getProperty(paramName);
        return value;

    }
/**
	 * Method to get the document object from the xml file
	 * 
	 * @param xmlFilePath
	 *            file path of the xml
	 * @return Document XML document object
	 * @throws Exception
	 */
    public static Document getDocument(String xmlFilePath) throws Exception
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        Document document = documentBuilder.parse(xmlFilePath);
        return document;
    }
	/**
	 * Method to update the xml file
	 * 
	 * @param document
	 *            xml document object
	 * @param xmlFilePath
	 *            xml file path
	 * @throws Exception
	 */
    public static void writeToXMLFile(Document document, String xmlFilePath)
        throws Exception
    {
        DOMSource source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(xmlFilePath);
        transformer.transform(source, result);
        Thread.sleep(3000);
    }

	
	/**
	 * Method to run the PO script
	 * 
	 * @param commands
	 *            array of commands to run
	 * @param dirLoc
	 *            location from where the command should run
	 * @throws Exception
	 */
    public static void runPOScript(String commands[], String dirLoc)
        throws Exception
    {
        Process process = null;
        try
        {
            String[] execCommandStrings = new String[commands.length + 2];
            execCommandStrings[0] = "cmd.exe";
            execCommandStrings[1] = "/c";
            for (int i = 0; i < commands.length; i++)
            {
                execCommandStrings[i + 2] = commands[i];
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                                                               execCommandStrings);
            processBuilder.directory(new File(dirLoc));
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            InputStreamReader inputstreamreader = new InputStreamReader(
                                                                        process.getInputStream());
            BufferedReader bufferedreader = new BufferedReader(
                                                               inputstreamreader);
            String line = bufferedreader.readLine();
            int i = 1;
            while ((line) != null)
            {
                System.out.println(line);
                line = bufferedreader.readLine();
                if (i == 30) break;
                i++;
            }
        } finally
        {
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
            process.destroy();
        }
    }

	/**
	 * Method to update the URIGrouping attribute values in the xml file
	 * 
	 * @param xmlFilePath
	 *            Name of the xml file
	 * @param elementName
	 *            Name of the element
	 * @param attrName
	 *            Name of the attribute
	 * @param attrValue
	 *            value of the attribute
	 * @param propMap
	 *            Properties Map
	 * @return String sucess message
	 */
    public static String updateURlGroupingTestXML(String xmlFilePath,
                                                  String elementName,
                                                  String attrName,
                                                  String attrValue,
                                                  HashMap<String, String> propMap)
    {
        String message = null;
        try
        {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(elementName);

            Set<String> s = propMap.keySet();

            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                Node node = nodeLst.item(i);
                System.out.println(node.getNodeName());
                NamedNodeMap attrMap = node.getAttributes();
                Node attrNode = attrMap.getNamedItem(attrName);

                if (attrNode != null)
                {
                    if (attrNode.getNodeValue().equals(attrValue))
                    {
                        NodeList childNodeLst = node.getChildNodes();
                        for (int c = 0; c < childNodeLst.getLength(); c++)
                        {
                            Node childNode = childNodeLst.item(c);
                            NamedNodeMap childMap = childNode.getAttributes();

                            if (childMap != null)
                            {

                                for (int cc = 0; cc < childMap.getLength(); cc++)
                                {
                                    Node ad = childMap.item(cc);

                                    Iterator<String> itr = s.iterator();
                                    while (itr.hasNext())
                                    {

                                        String abc = itr.next();
                                        if (abc.equals(ad.getNodeValue()))
                                        {
                                            // System.out.println(ad.getNodeValue()+"--ueval---"+propMap.get(abc));
                                            childNode.setTextContent(propMap
                                                    .get(abc));
                                            break;
                                        }
                                    }
                                }

                                // childMap.getNamedItem(name);

                            }
                        }
                        document.normalize();
                        break;
                    }
                }
            }

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e)
        {
            e.printStackTrace();
            message = e.getMessage();
        }
        return message;
    }

	
	/**
	 * Method to get the properties from the properties file
	 * 
	 * @param fileName
	 *            name of the properties file
	 * @return Properties
	 * @throws Exception
	 */
    public static Properties loadPropertiesFile(String fileName)
        throws Exception
    {

        Properties properties = new Properties();
        InputStream propsFile = new FileInputStream(fileName);

        try
        {
            properties.load(propsFile);
        } finally
        {
            propsFile.close();
        }

        return properties;
    }

	/**
	 * Method to write the properties to the file
	 * 
	 * @param fileName
	 *            Name of the File
	 * @param properties
	 *            Properties which needs to be written
	 * @throws Exception
	 */

    public static void writePropertiesToFile(String fileName,
                                             Properties properties)
        throws Exception
    {

        OutputStream output = new FileOutputStream(fileName);
        try
        {
            properties.store(output, "");
        } finally
        {
            output.close();
        }
    }

	
	/**
	 * Method to check when the file is last updated and for the errormessage
	 * 
	 * @param fileName
	 *            name of the file
	 * @param errorMsg
	 *            error message to check
	 * @return true
	 * @throws Exception
	 */
    public static boolean checkValidLastUpdate(String fileName, String errorMsg)
        throws Exception
    {
        File file = new File(fileName);
        System.out.println("in fileName " + fileName + " to CHECK " + errorMsg);
        RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
        long fileLength = file.length() - 1;
        StringBuilder sb = new StringBuilder();
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.DATE, -1);

        for (long filePointer = fileLength; filePointer != -1; filePointer--)
        {
            fileHandler.seek(filePointer);

            int readByte = fileHandler.readByte();
            if (readByte == 0xA)
            {
                if (filePointer == fileLength)
                {
                    continue;
                }
            } else if (readByte == 0xD)
            {
                if (filePointer == fileLength - 1)
                {
                    continue;
                } else
                {
                    sb.append((char) readByte);
                    String completeString = sb.reverse().toString();
                    // System.out.println("LINES " + completeString);

                    if (completeString.toUpperCase()
                            .contains(errorMsg.toUpperCase()))
                    {
                        System.out.println("%%%%%%%exists%%%%%% "
                                           + completeString);
                        fileHandler.close();
                        return true;
                    }

                    sb = new StringBuilder();

                }// end else
            }// end else 0xD
            sb.append((char) readByte);

        }// end for
        fileHandler.close();
        return false;
    }

	/**
	 * Method to delete the jar file
	 * 
	 * @param fileToBeDeleted
	 *            jar file to be deleted
	 * @throws IOException
	 */
    public static void deleteJar(String fileToBeDeleted) throws IOException
    {

        System.out.println(fileToBeDeleted);
        System.out.println("%%;%%%%%%%%%% DELETEING JAR %%%%%%%%%%%%%%");
        File file = new File(fileToBeDeleted);
        boolean a = file.delete();
        System.out.println(a);
        System.out.println("%%;%%%%%%%%%% DELETED JAR %%%%%%%%%%%%%%");

    }

    /**
     * Updates the Properties in Introscope Agent Profile
     * 
     * @param property
     *            -Property Keys
     * @param value
     *            -Property Values
     * @return -Returns true after updating the properties else returns false
     */
    public static boolean updateAgentProfileProperties(String property,
                                                       String value,
                                                       String agentPath,
                                                       String agentFileName)
    {
        boolean updated = false;
        try
        {
            String propertys[] = property.split("~");
            String values[] = value.split("~");
            System.out.println("*******Agent Path is :" + agentPath
                               + agentFileName);
            Properties properties = Util.loadPropertiesFile(agentPath
                                                            + agentFileName);
            for (int i = 0; i <= propertys.length - 1; i++)
            {
                properties.setProperty(propertys[i], values[i]);
            }
            Util.writePropertiesToFile(agentPath + agentFileName, properties);
            // delay is to save the changes in the file
            Util.sleep(20000);
            for (int i = 0; i <= propertys.length - 1; i++)
            {
                String propertyvalue = properties.getProperty(propertys[i]);
                if (propertyvalue.equalsIgnoreCase(values[i]))
                {
                    updated = true;
                } else
                {
                    updated = false;
                    break;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            updated = false;
            System.out.println("Properties updation failed due to:"
                               + e.getMessage());
        }
        System.out
                .println("**********  in updateAgentProfileProperties ****: ");
        return updated;
    }

	/**
	 * Method to change the value of the attribute
	 * 
	 * @param xmlFilePath
	 *            xml file path
	 * @param element
	 *            Name of the element
	 * @param attrName
	 *            Name of the attribute
	 * @param attrOldValue
	 *            attribute old value
	 * @param attrNewValue
	 *            attribute new value
	 * @return String Sucess Message
	 */

    public static String changeAttributeValue(String xmlFilePath,
                                              String element,
                                              String attrName,
                                              String attrOldValue,
                                              String attrNewValue)
    {
        String message = null;
        try
        {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(attrName);
                if (abc != null && abc.getNodeValue().equals(attrOldValue))
                {

                    abc.setNodeValue(attrNewValue);

                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();
                    break;
                }
            }
            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e)
        {
            message = e.getMessage();
        }
        return message;
    }
	/**
	 * Method to copy the file from source to destination
	 * 
	 * @param srcFileName
	 *            name of the source file
	 * @param destFileName
	 *            name of the destination to copy
	 * @return boolean return true or false
	 */
    public static boolean copyFile(String srcFileName, String destFileName)
    {
        boolean isFileExists = false;
        boolean isFileCopied = false;
        try
        {
            isFileExists = fileExists(srcFileName);
            if (isFileExists)
            {
                Task task = new CopyTask(srcFileName, destFileName);
                Context context = new Context();
                IResponse response = task.execute(context);
                isFileCopied = response.isSuccess();
            } else
            {
                System.out.println(srcFileName
                                   + "********FILE NOT FOUND********");
            }
        } catch (Exception e)
        {
            System.out.println("Unable to copy the file due to:"
                               + e.getMessage());
            e.printStackTrace();
        }
        return isFileCopied;
    }
	
	/**
	 * Method to check for file exist
	 * 
	 * @param fileDir
	 *            Name of the file
	 * @return boolean
	 */
    public static boolean fileExists(String fileDir)
    {
	boolean isFileExists = false;
		try {
			File file=new File(fileDir);
			System.out.println("File ::"+file.getAbsolutePath()+"is File Exist:"+file.exists());
			isFileExists = file.exists();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return isFileExists;
    }
	
	
	/**
	 * Method to delete the file
	 * 
	 * @param fileName
	 *            Name of the file to delete
	 * @return boolean
	 */
    public static boolean deleteFile(String fileName)
    {
        boolean isFileExists = false;
        boolean isFileDeleted = false;
        try
        {
            isFileExists = fileExists(fileName);
            if (isFileExists)
            {
                Task task = new DeleteTask(fileName);
                Context context = new Context();
                IResponse response = task.execute(context);
                isFileDeleted = response.isSuccess();
            } else
            {
                System.out.println(fileName + "********FILE NOT FOUND********");
            }
        } catch (Exception e)
        {
            System.out.println("Unable to delete the file due to:"
                               + e.getMessage());
        }
        return isFileDeleted;
    }

	
	/**
	 * Method to rename the file
	 * 
	 * @param srcFileName
	 *            Name of the source file
	 * @param destFileName
	 *            Name of the destination file
	 * @return boolean return true or false
	 */
    public static boolean renameFile(String srcFileName, String destFileName)
    {
        boolean isFileExists = false;
        boolean isFileRenamed = false;
        try
        {
            isFileExists = fileExists(srcFileName);
            if (isFileExists)
            {
                Task task = new RenameTask(srcFileName, destFileName);
                Context context = new Context();
                IResponse response = task.execute(context);
                isFileRenamed = response.isSuccess();
            } else
            {
                System.out.println(srcFileName
                                   + "********FILE NOT FOUND********");
            }
        } catch (Exception e)
        {
            System.out.println("Unable to rename the file due to:"
                               + e.getMessage());
            e.printStackTrace();
        }
        return isFileRenamed;
    }
	/**
	 * Method to update the properties in the properties file
	 * 
	 * @param propertyKeys
	 *            Name of the Properties
	 * @param propertyValues
	 *            Values of the properties
	 * @param filePath
	 *            Name of the file path
	 * @return boolean
	 */
    public static boolean updateProperties(String propertyKeys,
                                           String propertyValues,
                                           String filePath)
    {
        boolean isPropertyUpdated = false;
        boolean isFileExists = false;
        String[] propKeys = propertyKeys.split("~");
        String[] propValues = propertyValues.split("~");
        try
        {
            isFileExists = fileExists(filePath);
            if (isFileExists)
            {
                PropertiesFileTask task = new PropertiesFileTask(filePath);
                Context context = new Context();
                for (int i = 0; i < propKeys.length; i++)
                    task.setProperty(propKeys[i], propValues[i]);
                IResponse response = task.execute(context);
                // delay is to save the changes in the file
                Util.sleep(20000);
                for (int i = 0; i < propKeys.length; i++)
                {
                    String propertyValue = task.getProperties()
                            .getProperty(propKeys[i]);
                    System.out.println(propertyValue);
                    isPropertyUpdated = propertyValue
                            .equalsIgnoreCase(propValues[i]);
                    if (!isPropertyUpdated)
                    {
                        isPropertyUpdated = false;
                        System.out
                                .println("********Property not updated properly********");
                        break;
                    }
                }
            } else
            {
                System.out.println(filePath + "********FILE NOT FOUND********");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            e.getMessage();
        }
        return isPropertyUpdated;
    }

    /**
	 * Changes the attribute value for the given element and attribute name
	 * 
	 * @param xmlFilePath
	 *            Name of the XML File Path
	 * @param element
	 *            Name of the element
	 * @param attrName
	 *            Name of the attrbute
	 * @param attrNewValue
	 *            New value of the attribute
	 * @return String Sucess Message
	 */
    public static String changeAttributeValue(String xmlFilePath,
                                              String element,
                                              String attrName,
                                              String attrValue)
    {
        String message = null;
        Node abc = null;
        try
        {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                if (attrName != null && attrName != "")
                {
                    abc = attrMap.getNamedItem(attrName);
                } else
                {
                    abc = attrMap.item(i);
                }
                if (abc != null)
                {
                    abc.setNodeValue(attrValue);
                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();
                    break;
                }
            }
            writeToXMLFile(document, xmlFilePath);
            message = AutomationConstants.SUCCESS_MESSAGE;
        } catch (Exception e)
        {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Gets the attribute value for the given element and attribute name
     * 
     * @param xmlFilePath
     * @param element
     * @param attrName
     * @return  value of the attribute
     */
    public static String getAttributeValue(String xmlFilePath,
                                           String element,
                                           String attrName)
    {
        String message = null;
        Node abc = null;
        try
        {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                if (attrName != null && attrName != "")
                {
                    abc = attrMap.getNamedItem(attrName);
                } else
                {
                    abc = attrMap.item(i);
                }
                message = abc.getNodeValue();
                System.out.println("*****" + message);
            }
        } catch (Exception e)
        {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * 
     * @param agentMetric
     *            -MetricPath which needs to check
     * @param clw
     *            -clwbean Object
     * @return -Returns True if metric exists else returns False
     */
    public static boolean checkMetricExists(String agentMetric, CLWBean clw)
    {
        boolean metricCheck = false;
        try
        {
            System.out
                    .println("******** Agentmetric In checkMetric - metricExists *** "
                             + agentMetric);
            metricUtil = new MetricUtil(agentMetric, clw);
            int elapsedInterval = 0;
            int chkInterval = 5 * 60 * 1000;
            while (true)
            {
                Thread.sleep(30000);
                elapsedInterval = elapsedInterval + 30000;
                if (metricUtil.metricExists())
                {
                    metricCheck = true;
                    break;
                }
                if (elapsedInterval == chkInterval)
                {
                    break;
                }
            }
            metricUtil = null;
        } catch (Exception e)
        {}
        return metricCheck;
    }
	
	/**
	 * this method checks for the presence of a message in the console after a
	 * command has been executed.
	 * 
	 * @param directoryLocation
	 *            -the directory from which the command is executed.
	 * @param command
	 *            -command to be executed
	 * @param message
	 *            - the message to be checked for after the execution
	 * @return boolean- true or false based on the presence or absence of the
	 *         message in the console
	 * @throws IOException
	 */
    public static boolean runCommand(String directoryLocation,
                                     String command,
                                     String message) throws IOException
    {
        BufferedReader reader = null;
        Process process = null;
        boolean messageFound = false;
        try
        {
            System.out.println(command);
            String[] startCmnd = { command };
            process = Util.getProcess(startCmnd, directoryLocation);
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                if (line.contains(message))
                {
                    System.out.println("COMMAND EXECUTED SUCESSFULLY");
                    messageFound = true;
                    break;
                }
            }
        } catch (Exception e)
        {
            System.out.println("Unable to execute the command:"
                               + e.getMessage());
        } finally
        {

            if (reader != null)
            {
                reader.close();
            }
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
        }
        return messageFound;
    }
	
	/**
	 * Method to convert the time stamp from string to date format
	 * 
	 * @param timeStamp
	 *            timestamp string format
	 * @return  String 
	 * 			  Converted Time Stamp			
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

							mon = "0";

						} else if ("Feb".equalsIgnoreCase(token)) {

							mon = "1";

						} else if ("Mar".equalsIgnoreCase(token)) {

							mon = "2";

						} else if ("Apr".equalsIgnoreCase(token)) {

							mon = "3";

						} else if ("May".equalsIgnoreCase(token)) {

							mon = "4";

						} else if ("Jun".equalsIgnoreCase(token)) {

							mon = "5";

						} else if ("Jul".equalsIgnoreCase(token)) {

							mon = "6";

						} else if ("Aug".equalsIgnoreCase(token)) {

							mon = "7";

						} else if ("Sep".equalsIgnoreCase(token)) {

							mon = "8";

						} else if ("Oct".equalsIgnoreCase(token)) {

							mon = "9";

						} else if ("Nov".equalsIgnoreCase(token)) {

							mon = "10";

						} else if ("Dec".equalsIgnoreCase(token)) {

							mon = "11";

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

			System.out.println("-------" + year + "/" + mon + "/" + day + " "
					+ time);

		} catch (Exception e) {

			e.printStackTrace();

		}
		
		Calendar now1 = Calendar.getInstance();
		String[] initTime = time.split(":");

		now1.set(Integer.parseInt(year), Integer.parseInt(mon),
				Integer.parseInt(day), Integer.parseInt(initTime[0]),
				Integer.parseInt(initTime[1]), Integer.parseInt(initTime[2]));
		now1.add(Calendar.SECOND, 15);
		String changedSec = now1.get(Calendar.YEAR)

		+ "/" + (now1.get(Calendar.MONTH) +1)+ "/"

		+ now1.get(Calendar.DAY_OF_MONTH) + " "
				+ now1.get(Calendar.HOUR_OF_DAY) + ":"
				+ now1.get(Calendar.MINUTE) + ":" + now1.get(Calendar.SECOND);

		System.out.println("changed time:" + now1.get(Calendar.SECOND));
		String initialTime = year + "/" + (Integer.parseInt(mon)+1) + "/" + day + " " + time;
		System.out.println(initialTime + "~" + changedSec);
		return (initialTime + "~" + changedSec);

	}

	}
