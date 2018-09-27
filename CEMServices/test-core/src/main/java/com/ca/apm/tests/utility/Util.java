package com.ca.apm.tests.utility;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util {
    static QaUtils util = new QaUtils();
    public static final Logger log = Logger.getLogger(Util.class.getName());

    public static boolean invokeProcess(ProcessBuilder pb) throws Exception {
        // final Process process = null;
        try {

            log.info("Running command..." + pb.command());
            final Process process = pb.start();
            final InputStream is = process.getInputStream();
            new Thread() {
                public void run() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        log.info("Consuming process output...");
                        while (br.ready())
                            log.info(br.readLine());
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            new Thread() {
                public void run() {
                    try {
                        BufferedReader br =
                            new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        while (br.ready())
                            log.info(br.readLine());
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            log.info("Command completed.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    public static boolean invokeProcessBuilder(List<String> args, String workingDir)
        throws Exception {

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        if (workingDir != null) pb.directory(new File(workingDir));
        Map<String, String> env = pb.environment();

        env.put("BUILD_ID", "dontKillMe");
        invokeProcessBuilderNoWait(pb);
        return true;
    }



    public static Process invokeProcessBuilderNoWait(List<String> args) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Map<String, String> env = pb.environment();
        env.put("BUILD_ID", "dontKillMe");
        return invokeProcessBuilderNoWait(pb);
    }

    public static StringBuffer invokeProcessBuilder(ProcessBuilder pb) throws Exception {

        Process p = pb.start();
        return readInputStream(p.getInputStream());
    }

    public static StringBuffer readInputStream(InputStream stream) throws Exception {

        StringBuffer stdout = new StringBuffer();
        BufferedReader bufferedreader = null;
        InputStreamReader inputstreamreader = null;

        try {
            inputstreamreader = new InputStreamReader(stream);
            bufferedreader = new BufferedReader(inputstreamreader);


            String line;
            while ((line = bufferedreader.readLine()) != null) {
                // eliminates unnecessary log messages, specifically in case of tomcat server
                if (!line.contains("IntroscopeAgent")) {
                    log.info(line);
                }
                // skipping some PO lines to avoid testng OOM for system tests
                if (!line.contains("[Pipe Organ]") && !line.equals(" ") && !line.equals("")) {
                    stdout.append(line);
                    stdout.append(System.getProperty("line.separator"));
                }

                // This check is applied to take out control from WLS startup script, as it is
                // started.
                if (line.contains(AutomationConstants.WLS_PORTAL_START_MESSAGE)) break;
                if (line.contains(AutomationConstants.WLS_STARTING_MESSAGE_WINDOWS)) break;
                if (line.contains(AutomationConstants.TOMCAT_STARTUP_MESSAGE)) break;
            }

        } finally {

            if (inputstreamreader != null) inputstreamreader.close();
            if (bufferedreader != null) bufferedreader.close();
        }
        log.info("**** At end of invokeProcessBuilder **** ");
        return stdout;
    }

    public static Process invokeProcessBuilderNoWait(ProcessBuilder pb) throws Exception {
        Process process = null;
        try {
            process = pb.start();
            SimpleStreamReader reader = new SimpleStreamReader(process.getInputStream());
            new Thread(reader).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("**** At end of invokeProcessBuilderNoWait ****");
        return process;
    }


    /**
     * Starts Epagent
     * 
     * @param script
     * @throws Exception
     */
    public static Process runEpagent(String epagentHome, String epagentProfilePath)
        throws Exception {

        List<String> args = new ArrayList<String>();

        args.add("java");
        args.add("-classpath");
        args.add(epagentHome + ";" + epagentHome + "/lib/EPAgent.jar");
        args.add("-Dcom.wily.introscope.epagent.properties=" + epagentProfilePath);
        args.add("com.wily.introscope.api.IntroscopeEPAgent");

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.directory(new File(epagentHome));

        log.info("[runEpagent] epagent startup command: " + args.toString().replace(",", ""));
        return invokeProcessBuilderNoWait(pb);
    }


    public static boolean startAgent(String appserverBin, String appServerStartFile, String arg)

    {
        try {
            List<String> args = new ArrayList<String>();
            args.add(appserverBin + "/" + appServerStartFile);
            if (!arg.trim().isEmpty()) args.add(arg);

            if (invokeProcessBuilder(args, appserverBin)) {
                if (lookForPortAvailability(System.getProperty("agent.host"),
                    Integer.parseInt(System.getProperty("appserver.port")), 20 * 60, true))
                    return true;
                else
                    return false;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean startEM(String host, String port, String executable) {
        String[] processArgs = StringUtils.split(executable, ' ');
        return startEM(host, port, Arrays.asList(processArgs), null);
    }

    public static boolean startEM(String host, String port, List<String> args, String workingDir)

    {
        try {
            if (invokeProcessBuilder(args, workingDir)) {
                if (lookForPortAvailability(host, Integer.parseInt(port), 20 * 60, true))
                    return true;
                else {
                    log.info("Failed to start EM in 20 mins.");
                    return false;
                }

            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 
     * @param installDir
     * @param ipaddress
     * @return
     */


    // chusw01
    /* starts em through watchdog.jar file */
    public static String startWatchEM(String installDir, String ipaddress) {
        String outputFile = installDir + "/bin/emConsoleOutput.txt";
        String binDir = installDir + "/bin";
        String[] cmd =
            {installDir + "/jre/bin/java", "-jar", installDir + "/bin/WatchDog.jar", "start",
                    "-watch", "-startcmd", installDir + "/Introscope_Enterprise_Manager.exe",
                    "-emhost", ipaddress, "-port", "4321"};

        try {
            util.executeCommandWatch(cmd, util.convertPathSeparators(binDir), outputFile, 100);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outputFile;
    }

    /**
     * This method is to stop the EM
     * 
     * @param clw
     * @param hostName
     * @param port
     * @return
     */
    public static boolean stopEM(CLWBean clw, String hostName, int port) {
        String shutdown = "shutdown";

        try {
            log.info(port + "-----" + hostName);
            if (!isPortAvailable(port, hostName)) {
                log.info("EM is stopped already");
                return false;
            }

            clw.runCLW(shutdown).toString();

            log.info("EM Shutting down");

            while (isPortAvailable(port, hostName)) {
                Util.sleep(1000 * 10);
            }
            log.info("EM Shutdown Successful");
            Util.sleep(1000 * 30);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public static boolean startAgent()

    {
        return startAgent(System.getProperty("appserver.bin.dir"),
            System.getProperty("appserver.startup.file"),
            System.getProperty("appserver.startup.args"));

    }


    public static boolean stopAgent(String appserverBin, String appServerStopFile, String arg) {

        try {
            List<String> args = new ArrayList<String>();
            args.add(appserverBin + "/" + appServerStopFile);
            if (!arg.trim().isEmpty()) args.add(arg);

            if (invokeProcessBuilder(args, appserverBin)) {
                if (lookForPortAvailability(System.getProperty("agent.host"),
                    Integer.parseInt(System.getProperty("appserver.port")), 10 * 60, false))
                    return true;
                else
                    return false;
            } else
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean stopAgent()

    {
        return stopAgent(System.getProperty("appserver.bin.dir"),
            System.getProperty("appserver.stop.file"), System.getProperty("appserver.stop.args"));

    }

    public static boolean lookForPortAvailability(String host, int port, long sec, boolean status)

    {
        while ((sec = sec - 30) > 0) {
            if (status == isPortAvailable(port, host)) return true;
            log.info("Waiting for the port become ready...");
            sleep(30000);
        }
        return false;

    }



    public static boolean findPattern(String filePath, String pattern) throws Exception {

        boolean patternFound = false;
        LineNumberReader lineReader = null;
        File file = new File(filePath);

        try {

            Pattern regexp = Pattern.compile(pattern);
            Matcher matcher = regexp.matcher("");
            lineReader = new LineNumberReader(new FileReader(file));

            String line = null;
            while ((line = lineReader.readLine()) != null && !patternFound) {
                matcher.reset(line); // reset the input
                if (matcher.find()) {
                    patternFound = true;
                }
            }

        } finally {

            if (lineReader != null) lineReader.close();
        }

        return patternFound;
    }

    /**
     * Collects unique lines in the file matching provided pattern
     *
     * @param filePath
     * @param pattern
     * @return
     * @throws Exception
     */
    public static TreeSet<String> collectUniqueLinesPerPattern(String filePath, String pattern)
        throws Exception {

        LineNumberReader lineReader = null;
        File file = new File(filePath);
        TreeSet<String> linesCol = new TreeSet<String>();

        try {
            Pattern regexp = Pattern.compile(pattern);
            Matcher matcher = regexp.matcher("");
            lineReader = new LineNumberReader(new FileReader(file));

            String line = null;
            while ((line = lineReader.readLine()) != null) {
                matcher.reset(line); // reset the input
                if (matcher.find()) linesCol.add(line);
            }

        } finally {
            if (lineReader != null) lineReader.close();
        }

        return linesCol;
    }

    /**
     * Collects all lines in the file matching provided pattern
     *
     * @param filePath
     * @param pattern
     * @return
     * @throws Exception
     */
    public static ArrayList<String> collectLinesPerPattern(String filePath, String pattern)
        throws Exception {

        LineNumberReader lineReader = null;
        File file = new File(filePath);
        ArrayList<String> linesCol = new ArrayList<String>();

        try {
            Pattern regexp = Pattern.compile(pattern);
            Matcher matcher = regexp.matcher("");
            lineReader = new LineNumberReader(new FileReader(file));

            String line = null;
            while ((line = lineReader.readLine()) != null) {
                matcher.reset(line); // reset the input
                if (matcher.find()) linesCol.add(line);
            }

        } finally {
            if (lineReader != null) lineReader.close();
        }

        return linesCol;
    }

    public static Properties loadPropertiesFile(String fileName) {

        Properties properties = new Properties();
        InputStream propsFile = null;

        try {
            propsFile = new FileInputStream(fileName);
            properties.load(propsFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (propsFile != null) propsFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return properties;
    }

    /**
     * Using custom implementation of Properties store(..) method as original one escapes
     * some special characters (ie colon) & the agent is having troubles loading those properties
     * 
     * @param propertyFilePath
     * @param props
     * @throws FileNotFoundException
     */
    public static void writePropertiesToFile(String propertyFilePath, Properties props) {

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(propertyFilePath);
            for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                pw.println(key + "=" + props.getProperty(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pw != null) pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void sleep(long duration) {
        log.info("Sleeping for " + duration + " milliseconds.");
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log.info("***sleep failed!***");
        }
    }


    // To comment / uncomment the line in a file
    public static boolean replaceLine(String fileName, String oldLine, String newLine) {

        String line = "";
        String lines = "";
        boolean isFound = false;

        try {
            File file = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                lines += line + "\r\n";
            }
            reader.close();

            if (lines.contains(oldLine)) {

                lines = lines.replaceAll(oldLine, newLine);
                // log.info("******* newLine: **** " + lines);
                System.out.println("*********** Value of oldLine is " + oldLine);
                System.out.println("*********** Value of newLine is " + newLine);

                FileWriter writer = new FileWriter(fileName);
                writer.write(lines);
                writer.close();
                isFound = true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return isFound;

    }

    /**
     * checks for the availability of the processsID in windows task list
     *
     * @param processId
     * @return
     * @throws IOException
     */

    public static boolean isProcessAvail(String processId) throws IOException {
        if (processId == null) return false;
        Process p = null;
        BufferedReader reader = null;
        try {
            p = Runtime.getRuntime().exec(AutomationConstants.TASK_LIST);
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            boolean processIDfound = false;
            while (line != null) {

                if (line.contains(processId)) {
                    log.info("%%%%%%%%%%RUNNING WITH PROCESSID:" + processId);

                    processIDfound = true;
                    break;

                }
                line = reader.readLine();
            }
            if (processIDfound) {
                return true;
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return false;
    }

    /**
     * Use jps (Java Virtual Machine Process Status Tool) to check for the jvm availability
     *
     * @param name package name for the application's main
     *        class or the path name to the application's JAR file
     * @return process id
     * @throws IOException
     */
    public static String isProcessAvailByName(String name) throws IOException {
        log.info("[isProcessAvailByName] checking if  " + name + " is running.");
        Process p = null;
        BufferedReader reader = null;
        try {
            p = Runtime.getRuntime().exec("jps -l");
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[isProcessAvailByName] available process: " + line);
                if (line.contains(name)) {
                    log.info("[isProcessAvailByName] process " + name + " is running.");
                    String[] out = line.split(" ");
                    return out[0];
                }
                log.info("[isProcessAvailByName] process " + name + " is not running.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    /**
     * Killing process via pid
     *
     * @param pid
     */
    public static void killProcess(String pid) {

        String command = null;

        if (pid != null) {

            log.info("[killProcess] killing pid " + pid);
            if (System.getProperty("os.name").contains("Windows")) {
                command = "taskkill /F /PID " + pid;
            } else {
                command = "kill -9 " + pid;
            }
            runOSCommand(command);
        }
    }

    public static void denyWinWriteFilePermission(String user, String file) {

        setFilePermission(user, file, false, "(OI)(CI)W");
    }

    public static void grantWinWriteFilePermission(String user, String file) {

        setFilePermission(user, file, true, "(OI)(CI)W");
    }

    /**
     * Set file permissions
     *
     * @param file or directory name
     */
    public static void setFilePermission(String user, String file, boolean grant, String permission) {

        // due to the following java bug that was fixed only in version 7 such commands as
        // (new File(file).setWritable(false) don't work. So have to use system commands instead.
        // See ref http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6728842

        if (file != null && user != null) {
            String command = null;
            log.info("[setFilePermission] updating permission " + permission + " on " + file);
            if (System.getProperty("os.name").contains("Windows")) {
                if (grant)
                    command = "icacls \"" + file + "\" /grant " + user + ":" + permission;
                else
                    command = "icacls \"" + file + "\" /deny " + user + ":" + permission;
            } else {
                // TODO
            }

            runOSCommand(command);
        }
    }

    /**
     * Running OS command
     *
     * @param command string
     */
    public static boolean runOSCommand(String command) {

        boolean isDataPresent = false;
        try {
            if (command == null) {
                log.info("System command to execute wasn't provided.");
                return isDataPresent;
            }

            log.info("Running command: " + command);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
                isDataPresent = true;
            }
            reader.close();
        } catch (Exception e) {
            log.info("Error occurred while trying to execute command " + command);
            e.printStackTrace();
        }
        return isDataPresent;
    }

    /**
     * Returns the process instance for the commands passed
     *
     * @param commands
     *        - commands start the sql agent
     * @param dirLoc
     *        - commands execution directory location
     * @return - return the process
     * @throws IOException
     *         -Any IO Exception throws the error
     */

    public static Process getProcess(String[] commands, String dirLoc) throws IOException {

        if (commands == null) return null;
        String[] execCommandStrings = new String[commands.length + 2];
        execCommandStrings[0] = "cmd.exe";
        execCommandStrings[1] = "/c";
        for (int i = 0; i < commands.length; i++) {
            execCommandStrings[i + 2] = commands[i];
        }

        ProcessBuilder processBuilder = new ProcessBuilder(execCommandStrings);
        processBuilder.directory(new File(dirLoc));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return process;

    }

    public static boolean checkMessage(String compareStrings, String file)
        throws InterruptedException {

        return checkMessage(compareStrings, new File(file));
    }

    /**
     * check for the message in the file
     *
     * @param compareStrings
     * @param file
     * @return
     * @throws InterruptedException
     */

    public static boolean checkMessage(String compareStrings, File file)
        throws InterruptedException {
        boolean found = false;

        if (file == null) {
            throw new InterruptedException("File name wasn't provided.");
        }

        if (!file.exists()) {
            throw new InterruptedException("File " + file.getAbsolutePath() + " doesn't exist.");
        }

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
                    if (str.contains(compareStrings)) {
                        log.info("---- " + compareStrings);
                        found = true;
                        break;
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

        }
        return found;

    }

    /**
     * This method is used to check whether we are able to access the webpage or
     * not
     *
     * @param webPage
     * @return
     */
    public static boolean urlResponse(String webPage) {
        try {
            log.info("URL to be hit" + webPage);
            URL url = new URL(webPage);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = null;
            while ((str = in.readLine()) != null) {
                // log.info(str);
            }
            in.close();
            return true;
        } catch (Exception e) {
            log.info("PAGE NOT FOUND");
            return false;
        }
    }

    /**
     * Method to run a jar file app
     *
     * @param jarfile with absolute path
     * @return Process process instance to be able to destroy it later
     */
    public static Process runEchoServer(String vjarFile, String listPort, String shutdownPort)
        throws Exception {

        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-jar");
        args.add(vjarFile);
        args.add(listPort);
        args.add(shutdownPort);

        return invokeProcessBuilderNoWait(args);
    }

    /**
     * Method to check the port availability
     *
     * @param port and hostname
     * @return boolean
     */
    public static boolean isPortAvailable(int port, String hostName) {
        Socket soc = null;
        boolean isAvailable = false;
        try {
            soc = new Socket(hostName, port);
            isAvailable = soc.isBound();

        } catch (Exception e) {
            log.info("******* Exception isPortAvailable: " + e.getMessage());
            isAvailable = false;
        } finally {
            try {
                if (soc != null) soc.close();
            } catch (Exception e) {

            }

        }
        return isAvailable;

    } // End of isPortAvailable

    /**
     * Find specified path in the xml file
     *
     * @param xmlFile
     * @param searchExpr xpath search expression
     * @return
     * @throws Exception
     */
    public static ArrayList<String> parseXmlFile(String xmlFile, String searchExpr)
        throws Exception {

        log.info("[parseXmlFile] parsing xml file: " + xmlFile + " with xpath expression: "
            + searchExpr);

        ArrayList<String> data = new ArrayList<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(searchExpr);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            data.add(nodes.item(i).getNodeValue());
        }

        return data;
    }

    /**
     * Update xml file by providing required xpath
     *
     * @param xmlFile
     * @param searchExpr xpath search expression
     * @param newValue
     * @return
     * @throws Exception
     */
    public static boolean updateXmlFile(String xmlFile, String searchExpr, String newValue)
        throws Exception {

        log.info("[updateXmlFile] updating xml file: " + xmlFile + "; search expr: " + searchExpr
            + "; new value: " + newValue);

        // find node to update
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        Document doc = domFactory.newDocumentBuilder().parse(xmlFile);

        XPathFactory factory = XPathFactory.newInstance();
        XPathExpression expr = factory.newXPath().compile(searchExpr);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        // update document
        for (int i = 0; i < nodes.getLength(); i++) {
            nodes.item(i).setNodeValue(newValue);
        }

        // write back to a file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(new DOMSource(doc), result);

        return true;
    }


    public static String getOSType() {
        String osName = System.getProperty("os.name");
        String osType;
        if (osName.toLowerCase().contains("windows")) {
            osType = "windows";
        } else if (osName.toLowerCase().contains("unix") || osName.toLowerCase().contains("linux")) {
            osType = "unix";
        } else {
            osType = "unsupported";
        }
        return osType;
    }

    /**
     * @return full path of java command if 'appserver jvm home' property is set
     *         else it returns just string "java" which refers to java is set in System Path
     */
    private static String getJavaCommandPath() {
        String appServerJvmHome = System.getProperty("role_webapp.appserver.jvm.home");
        if (appServerJvmHome != null) {
            return appServerJvmHome + "/bin/java";
        } else {
            return "java";
        }
    }

    /**
     * Find threshold value of a clamp in the xml file
     *
     * @param xmlFile
     * @param clampid
     * @return String
     * @throws Exception
     */
    public static String findThresholdValueofClamp(String xmlFile, String clampid) throws Exception {
        String clampvalue = null;
        String searchExpr = "*/*/*[@id=\"" + clampid + "\"]/*";

        log.info("Checking xml file: " + xmlFile + " for xpath expression: " + searchExpr);

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(searchExpr);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeName() == "threshold") {
                clampvalue = nodes.item(i).getAttributes().getNamedItem("value").toString();
            }
        }

        log.info("The threshold value of " + clampid + " is " + clampvalue);

        return clampvalue;
    }

    /**
     * 
     * @param host
     * @param userName
     * @param password
     * @param sshport
     * @param workingDir
     * @param action
     */

    public static void startStopEMInLinux(String host, String userName, String password,
        int sshport, String workingDir, String action) {

        StringBuffer output = new StringBuffer();
        String scriptFileName = "cd " + workingDir + "/bin;" + "./EMCtrl.sh " + action;
        log.info(scriptFileName);
        try {

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            // TODO : Generalize this to the EM HOSTNAME, USERNAME and PASSWORD
            Session session = jsch.getSession(userName, host, sshport);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            log.info("Connected");
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            ((ChannelExec) channel).setCommand(scriptFileName);

            ((ChannelExec) channel).setErrStream(System.err);


            channel.connect();

            String msg = null;
            // String output = "";
            while ((msg = in.readLine()) != null) {
                // output = output + msg;
                output.append(msg);
            }

            log.info("Output is " + output);
            int exitStatus = channel.getExitStatus();

            if (exitStatus < 0) {
                log.info("Done, but exit status not set!");
            } else if (exitStatus > 0) {
                log.info("Done, but with error!");
            } else {
                log.info("Done!");
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            log.info(e.toString());
        }


    }

    /**
     * 
     * @param host
     * @param user
     * @param pwd
     * @param scriptFileName
     * @return
     */


    public static StringBuffer runScriptOnUnix(String host, String user, String pwd,
        String scriptFileName) {

        log.info("Host is : " + host);
        log.info("User is : " + user);
        log.info("PWD  is : " + pwd);
        log.info("Command is : " + scriptFileName);
        StringBuffer output = new StringBuffer();
        try {

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(pwd);
            session.setConfig(config);
            session.connect();
            log.info("Connected");
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            log.info(scriptFileName);
            ((ChannelExec) channel).setCommand(scriptFileName);

            ((ChannelExec) channel).setErrStream(System.err);


            channel.connect();

            String msg = null;
            // String output = "";
            while ((msg = in.readLine()) != null) {
                // output = output + msg;
                output.append(msg);
            }

            log.info("Output is " + output);
            int exitStatus = channel.getExitStatus();

            if (exitStatus < 0) {
                log.info("Done, but exit status not set!");
            } else if (exitStatus > 0) {
                log.info("Done, but with error!");
            } else {
                log.info("Done!");
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            log.info(e.toString());
        }
        return output;

    }

}
