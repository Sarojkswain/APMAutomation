package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;
import com.jcraft.jsch.*;

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

	public boolean connect() {
	    boolean connectionStatus = false;
		try {
			channel = new JSch();
	        session = channel.getSession(user, host, port);
	        session.setPassword(password);
	        Properties config = new Properties();
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect(timeout);	
	        logger.info("[SSHManager] Connection established to host " + host);
	        connectionStatus = true;
	    }
	    catch(JSchException e) {
	        logger.warning("[SSHManager] failed to connect to host " + host + 
	             ". If it's Windows platform, make sure you have 'CYGWIN sshd' service running.");
	    	logger.warning(e.getMessage());
	        e.printStackTrace();
	    }	    
		return connectionStatus;
	}
	
    public boolean sendCommand(String command)  {
        StringBuffer outputBuffer = null;
		String s = "", s1="";
        try {
            if (session == null) {
//              throw new Exception ("[sendCommand] remote manager session is null. " + "Call connect() method to create a new session.");
				logger.info("[sendCommand] remote manager session is null. " + "Call connect() method to create a new session.");
				return false;
            } else {		
				logger.info("  .... sendCommand openChannel");
				Channel channel = session.openChannel("exec");
				logger.info("Running remote command: " + command);
				((ChannelExec)channel).setCommand(command); 
				logger.info("  .... sendCommand openChannel");			
				channel.connect();
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				logger.info("  .... Standard output of command ");
				int i = 1;
				while ((s = stdInput.readLine()) != null & i<10) {
					if (i == 1) { s1 = s; } 
					System.out.println(s);
					i++;
				}
				channel.disconnect();
				return true;
			}
        }
		catch(Exception e) {
			logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
			e.printStackTrace();
			return false;
        }
    }
  
	public void sendFile (String localFile, String remoteDir) {
		try {
		    if (session == null) {
                throw new Exception ("[sendFile] remote manager session is null. " +  "Call connect() method to create a new session.");
            }
			Channel channel = session.openChannel("sftp");
	        channel.connect();
	        ChannelSftp sftpChannel = (ChannelSftp) channel;
	        logger.info("Sending file " + localFile + " to " + remoteDir + " on host " + host);
	        String remotePath = remoteDir.replace('\\', '/');
			String osnameL = System.getProperty("os.name").toLowerCase();
			if (osnameL.contains("win")) {
                remotePath = "/cygdrive/" + remotePath.replace(":", "");
			} else {
				remotePath = remoteDir;
			}
	        sftpChannel.cd(remotePath);  
	        File f = new File(localFile.replace('\\', '/'));
	        sftpChannel.put(new FileInputStream(f), f.getName());	      
	        sftpChannel.exit();
	        channel.disconnect();
		}
        catch(Exception e) {
			logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void getFile (String localDir, String remoteFilePath) {
		File file = new File(remoteFilePath);
		getFile (localDir, file.getParent(), file.getName());
	}

	public void getFile (String localDir, String remoteDir, String fileName) {
		try {
		    if (session == null) {
		        throw new Exception ("[getFile] remote manager session is null. " + "Call connect() method to create a new session.");
		    }
			Channel channel = session.openChannel("sftp");
	        channel.connect();
	        ChannelSftp sftpChannel = (ChannelSftp) channel;
	        localDir = localDir.replace('\\', '/');
	        logger.info("Getting file " + remoteDir + "/" + fileName + " from " + host + " to dir " + localDir);
	        String remotePath = remoteDir.replace('\\', '/');	       
			String osnameL = System.getProperty("os.name").toLowerCase();
			if (osnameL.contains("win")) {
	        	remotePath = "/cygdrive/" + remotePath.replace(":", "");
	        } else {
	            remotePath = remoteDir;
	        }
	        sftpChannel.cd(remotePath);
	        //create output dir if it doesn't exist
	        if (!new File(localDir).exists()) {
	        	new File(localDir).mkdir();
	        }
	        File f = new File(localDir + "/" + fileName);
	        sftpChannel.get(fileName, new FileOutputStream(f));     
	        sftpChannel.exit();
	        channel.disconnect();
		}
        catch(Exception e) {
			logger.warning("Error for " + host + ":" + port + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void close() {
		if (session != null)  {
			session.disconnect(); 	
			logger.info("[SSHManager] Disconnected from host " + host);
		}
	}
	
    public void setUser(String user) {
        this.user = user;
    }
    public void setPassword(String password) {
        this.password = password;
    }
	
	public String getHost() {
        return host;
    }
	
	public int getPort() {
        return port;
    }
	
	
	
	
}
