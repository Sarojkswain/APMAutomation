package com.ca.apm.tests.utility;

import com.jcraft.jsch.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class QaFileUtils {
	
	/**
	 * Get me the full path to the data file
	 * @param testDataFolder - the testdata folder has subfolders named by tc Id, what is yours?
	 * @param testDataFile - the file name for the assuming the file is located in you test case id subfolder
	 * @return the full string to the data file path suitable for Tess Browse dialog
	 */
	public String getTestDataFullPath(String testDataFolder,String testDataFile){
		String delim = System.getProperty("file.separator");
		StringBuffer fullPath = new StringBuffer();
		fullPath.append(System.getProperty("user.dir"));
		System.out.println(System.getProperty("user.dir"));
		fullPath.append(delim+"testdata"+delim+testDataFolder+delim);
		fullPath.append(testDataFile);
		return fullPath.toString();
	}
	
	/**
	 * Get me the full path to the test suite folder under testdata
	 * @param testSuiteName
	 * @return the full string to the data file path suitable for Tess Browse dialog
	 */
	public String getTestSuiteTestDataFullPath(String testSuiteName){
		String delim = System.getProperty("file.separator");
		StringBuffer fullPath = new StringBuffer();
		fullPath.append(System.getProperty("user.dir"));
		fullPath.append(delim+"testdata"+delim+testSuiteName+delim);
		return fullPath.toString();
	}
	
	
	/** to execute unix commands remotely(on tim/tess), output of command is not captured or processed
	 * @param user
	 * @param pwd
	 * @param host
	 * @param cmd
	 * @throws JSchException
	 */
	public void execUnixCmd(String host,String user, String pwd, String cmd) throws JSchException {
		
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(pwd);  ///u todo
			session.connect();			
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(cmd);
			channel.connect();
			session.disconnect();		
		}
	
	
	
	
	/**
	 * SCP a file from a remote host (ie. a TIM machine)
	 * Assumes it is a tim machine with root:quality
	 * @param remoteFile in the format host:/path/to/file
	 * @param localFile 
	 */
	public void getFileByScp(String remoteFile, String localFile) {
		FileOutputStream fos = null;
		try {

			String user = "root";
			remoteFile = remoteFile.substring(remoteFile.indexOf('@') + 1);
			String host = remoteFile.substring(0, remoteFile.indexOf(':'));
			String rfile = remoteFile.substring(remoteFile.indexOf(':') + 1);
			String lfile = localFile;

			String prefix = null;
			if (new File(lfile).isDirectory()) {
				prefix = lfile + File.separator;
			}

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword("quality");  ///u todo
			session.connect();
			// exec 'scp -f rfile' remotely
			String command = "scp -f " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ')
						break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0;; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				// System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? lfile : prefix
						+ file);
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					System.exit(0);
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}

			session.disconnect();

		
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fos != null)
					fos.close();
			} catch (Exception ee) {
			}
		}
	}
	
	private static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public void writeStringToFile(String fileFullPath, String aString) {
		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter(fileFullPath));
			out.write(aString + "\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	public String readStringFromFile(String fileFullPath) {
		String strLine = "";

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileFullPath));
			if ((strLine = in.readLine()) != null) {
				return strLine;
			}
			in.close();
		} catch (IOException e) {
			System.out.println("Exception ");

		}
		return strLine;
	}
	
	public int compareDefectEndTime(String testSuiteName) {
		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
		Calendar calendar = Calendar.getInstance();
		Date timeNow = calendar.getTime();
		//String timeStrNow = simpleDateFormat.format(calendar.getTime());
		//System.out.println(timeStrNow);
		String testSuiteTestDataFullPath = getTestSuiteTestDataFullPath(testSuiteName);
		String timeStrBefore = "";
		timeStrBefore = readStringFromFile(testSuiteTestDataFullPath+"DefectGenerationEndTime.txt");  ////may read from properties.
		//System.out.println(timeStrBefore);
		
		Date timeBefore;
		try {
			timeBefore = simpleDateFormat.parse(timeStrBefore.substring(0,13)+":10:00"); //timeStrBefore.substring(0,13)+
		} catch (ParseException e) {
			e.printStackTrace();
			return 9999;
		}
		
		long diff = timeNow.getTime() - timeBefore.getTime();
		int diffHour = (int) (diff /(1000 * 60 * 60));
		//System.out.println("Run defect generation "+diff /(1000 * 60)+ "m before.");
		//System.out.println("diff is " + diffHour+ "hour/s old.");
		return diffHour;
	}


public int compareDefectEndTimeMinutes(String testSuiteName) {
	String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
	Calendar calendar = Calendar.getInstance();
	Date timeNow = calendar.getTime();
	calendar.add(Calendar.HOUR, 1);
	Date timeNext = calendar.getTime();
	String timeNextStr = simpleDateFormat.format(calendar.getTime());
	//System.out.println(timeNextStr);

	try {
		timeNext = simpleDateFormat.parse(timeNextStr.substring(0,13)+":15:00"); //timeStrBefore.substring(0,13)+
	} catch (ParseException e) {
		e.printStackTrace();
		return 9999;
	}
	
	long diff = timeNext.getTime() - timeNow.getTime();
	int diffMinutes = (int) (diff /(1000 * 60 ));
	//System.out.println("Run defect generation "+diff /(1000 * 60)+ "m before.");
	System.out.println("diff is " + diffMinutes+ "minutes/s.");
	return diffMinutes;
}

public String printCurrentTimeStr() {
	String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);
	Calendar calendar = Calendar.getInstance();
	Date timeNow = calendar.getTime();
	String timeNowStr = simpleDateFormat.format(calendar.getTime());
	System.out.println(timeNowStr);
	return timeNowStr;
}

public void syncTimeWithTess(String tessHostIP){
    Runtime rt = Runtime.getRuntime();
    String commd="net time \\\\"+tessHostIP+" /set /yes";
    Process pr;
    try {
        pr = rt.exec(commd);
        int k=pr.waitFor();
        if(k!=0){
          System.out.println("Failed to sync time with the given tess host");
        }
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}

}
