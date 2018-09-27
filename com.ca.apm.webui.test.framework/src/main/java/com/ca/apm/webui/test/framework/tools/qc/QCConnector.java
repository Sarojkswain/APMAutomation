package com.ca.apm.webui.test.framework.tools.qc;

import java.util.Properties;

import com.ca.apm.webui.test.framework.base.BaseTestObject;
import com.mercury.qualitycenter.otaclient.ClassFactory;
import com.mercury.qualitycenter.otaclient.ITDConnection2;
import com4j.ComException;

/**
 * 
 * The <code>QCConnector</code> represents ...
 * 
 * @author whogu01
 * @since
 * @copyright 2014 CA Technology, All rights reserved.
 */
public class QCConnector extends BaseTestObject {

	private ITDConnection2 qcConnect;

	private Properties prop;
	public static QCConnector instance;

	public ITDConnection2 getQCConnection() {

		return qcConnect;
	}

	public static QCConnector getInstance() {
        if (instance == null) {
			instance = new QCConnector();
        }

		return instance;
	}

    private QCConnector()
    {
		if (qcConnect == null) {
            setObjectName("Connector");
            setObjectType("qc");

			try {
				qcConnect = ClassFactory.createTDConnection();
				qcConnectProject();

			} catch (Exception e) {
				e.printStackTrace();
            }
        }
    } // end connector

    public boolean isConnected() {
        return qcConnect != null && qcConnect.connected();
    }

	public void logout() {
		try {
			if (qcConnect.connected()) {
                // commented this out as this is deprecated from version 9.0
                // Enabling it in version >9.0 will cause indefinite wait
                // sometimes
                // _qcConnect.disconnectProject();

				qcConnect.disconnect();
                logTestCase(TRACE, "Disconnected the user from the currently connected project");
            } else {
                logTestCase(TRACE, "User already disconnected from the currently connected project");
            }

			if (qcConnect.loggedIn()) {
				qcConnect.logout();
                logTestCase(TRACE, "Terminated the user's session");
            } else {
                logTestCase(TRACE, "User session already terminated");
			}
			qcConnect.releaseConnection();
            logTestCase(TRACE, "Connection object released");
            logTestCase(TRACE, "user successfully Logged out");
        }

        catch (Exception ex) {
            logTestCase(ERROR, "Exception while logging out QC user");
        }
    } // end logout

    public boolean qcConnectProject() {

		prop = QCConfig.getProperties();
		String qcURL = prop.getProperty("qc.server.URL").trim();
		String qcUser = prop.getProperty("qc.login.username").trim();
		String qcPassword = prop.getProperty("qc.login.password").trim();
		String qcDomain = prop.getProperty("qc.name.DOMAIN").trim();
		String qcProject = prop.getProperty("qc.name.PROJECT").trim();
		
        // should the password be decrypted?
		if (Boolean.parseBoolean(prop.getProperty("qc.login.password.encrypted"))) {
			qcPassword = decrypt(qcPassword);
        }

        boolean result = false;
		try {
			qcConnect.initConnectionEx(qcURL);
			if (!qcConnect.loggedIn()) {
				qcConnect.login(qcUser, qcPassword);
				qcConnect.connect(qcDomain, qcProject);
               
                logTestCase(TRACE, "Connection established to QC with status " + qcConnect.connected());
                result = true;
            }

        } catch (ComException comex) {
            logTestCase(ERROR, "\"" + qcUser + "\" failed to connect QC project "
					+ qcProject + ": " + comex);
			comex.printStackTrace();
			System.exit(1);
		}
        return result;
    } // end connect project

    public static String encrypt(String origString) {
        char[] chars = origString.toCharArray();
        int len = chars.length;
        chars[0] = (char) (chars[0] + 5);
        chars[len - 1] = (char) (chars[len - 1] - 3);

        for (int i = 1; i <= len - 2; i++) {
            chars[i] = (char) (chars[i] + 5);
        }
        return new String(chars);
    } // end encrypt method

    public static String decrypt(String origString) {
        char[] chars = origString.toCharArray();
        int len = chars.length;
        chars[0] = (char) (chars[0] - 5);
        chars[len - 1] = (char) (chars[len - 1] + 3);

        for (int i = 1; i <= len - 2; i++) {
            chars[i] = (char) (chars[i] - 5);
        }
        return new String(chars);
    } // end decrypt method

} // end class