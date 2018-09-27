/*
 * # ~ Copyright (c) 2013. CA Technologies. All rights reserved.
 * # ~
 * # ~
 * # ~ Author: Marina Kur (kurma05)
 * #
 * # Use this class to execute remote commands.
 */
package com.ca.apm.tests.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHManager {

    private static Logger logger = Logger.getLogger("testng-agent");
    private JSch channel;
    private String user;
    private String host;
    private int port;
    private String password;
    private Session session;
    private int timeout;
    private final int DEFAULT_SSH_PORT = 22;
    private final int DEFAULT_SSH_TIMEOUT = 60000;

    public SSHManager(String user, String password, String host) {

        this.user = user;
        this.password = password;
        this.host = host;
        this.port = DEFAULT_SSH_PORT;
        this.timeout = DEFAULT_SSH_TIMEOUT;
    }

    public SSHManager(String user, String password, String host, int port) {

        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.timeout = DEFAULT_SSH_TIMEOUT;
    }

    public SSHManager(String user, String password, String host, int port, int timeout) {

        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Connect to remote host
     */
    public void connect() {

        try {
            channel = new JSch();
            session = channel.getSession(user, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(timeout);
        } catch (JSchException e) {
            logger.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send command to remote host
     * 
     * @param command Command to be sent
     * @return output of the executed command
     */
    public StringBuffer sendCommand(String command) {

        StringBuffer outputBuffer = null;

        try {
            Channel channel = session.openChannel("exec");
            logger.info("Running remote command: " + command);
            ((ChannelExec) channel).setCommand(command);
            channel.connect();
            outputBuffer = readInputStream(channel.getInputStream());
            channel.disconnect();
        } catch (Exception e) {
            logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return outputBuffer;
    }

    public StringBuffer readInputStream(InputStream stream) throws Exception {

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
                    logger.info(line);
                }
                // skipping some PO lines to avoid testng OOM for system tests
                if (!line.contains("[Pipe Organ]") && !line.equals(" ") && !line.equals("")) {
                    stdout.append(line);
                    stdout.append(System.getProperty("line.separator"));
                }

            }

        } finally {

            if (inputstreamreader != null) inputstreamreader.close();
            if (bufferedreader != null) bufferedreader.close();
        }
        logger.info("**** At end of invokeProcessBuilder **** ");
        return stdout;
    }


    /**
     * Send local file to remote host
     * 
     * @param localFile local file name
     * @param remoteDir remote directory name
     */
    public void sendFile(String localFile, String remoteDir) {

        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            logger.info("Sending file " + localFile + " to " + remoteDir + " on host " + host);

            String remotePath = remoteDir.replace('\\', '/');
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                remotePath = "/cygdrive/" + remotePath.replace(":", "");
            }

            sftpChannel.cd(remotePath);
            File f = new File(localFile.replace('\\', '/'));
            sftpChannel.put(new FileInputStream(f), f.getName());
            sftpChannel.exit();
            channel.disconnect();
        } catch (Exception e) {
            logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get file from remote host
     * 
     * @param localDir local directory name
     * @param remoteFilePath remote full file path
     */
    public void getFile(String localDir, String remoteFilePath) {

        File file = new File(remoteFilePath);
        getFile(localDir, file.getParent(), file.getName());
    }

    /**
     * Get file from remote host
     * 
     * @param localDir local directory name
     * @param remoteDir remote directory name
     * @param fileName file name
     */
    public void getFile(String localDir, String remoteDir, String fileName) {

        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            localDir = localDir.replace('\\', '/');

            logger.info("Getting file " + remoteDir + "/" + fileName + " from " + host + " to dir "
                + localDir);

            String remotePath = remoteDir.replace('\\', '/');
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                remotePath = "/cygdrive/" + remotePath.replace(":", "");
            }

            sftpChannel.cd(remotePath);
            // create output dir if it doesn't exist
            if (!new File(localDir).exists()) {
                new File(localDir).mkdir();
            }

            // get remote file
            File f = new File(localDir + "/" + fileName);
            sftpChannel.get(fileName, new FileOutputStream(f));

            sftpChannel.exit();
            channel.disconnect();
        } catch (Exception e) {
            logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close connection
     */
    public void close() {

        if (session != null) {
            session.disconnect();
        }
    }
}
