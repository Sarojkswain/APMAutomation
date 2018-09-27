package com.ca.apm.webui.test.framework.tools.qc;

import java.util.Properties;

import com.ca.apm.webui.test.framework.base.BaseTestObject;
import com.ca.apm.webui.test.framework.base.PropertyLoader;

/**
 * 
 * The <code>QCUpdater</code> is responsible for updating Quality Center with
 * the results of the test run, at run time following the completion of the test
 * case. The test-lab folder, test-set name, and test-case ID must be known.
 * Additionally, QC Login user/password must be specified.
 * <p>
 * 
 * See the qualitycenter.properties file in /resources/qc/...
 * <p>
 * The QC-Update functionality is based on the pre-existing libraries found with the QC-Update Tool
 * created by other APM teams (notably the agent team and Marina Kur). Those java source files have
 * been slightly modified to use BaseTestObject logging functions instead of logger.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class QCUpdater extends BaseTestObject {

    private QCWrapper fQcWrap;

    private final Properties fProp = new Properties();

    public final String fTestSetFolder;

    public final String fTestSetName;

    public final String fBuildNumber;

    public final String fBrowserName;

    public final Boolean fQcUpdate;

    public final String fUpdateFlag;

    private QCConnector fConnector;

    private final static String kDefaultConfig = "qualitycenter.properties";

    private final static String kLaunchProps = "launch.properties";

    private final static String kBrowserText = "Browser=";


    /**
     * get singleton instance
     */
    public static QCUpdater getInstance() {
        if (fInstance == null) {
            fInstance = new QCUpdater();
        }

        return fInstance;
    }

    public QCUpdater() {
        setObjectName("Updater");
        setObjectType("qc");
        loadProperties(kDefaultConfig);
        loadProperties(kLaunchProps);
        fBrowserName = kBrowserText + fProp.getProperty("browser.type").trim();
        fUpdateFlag = fProp.getProperty("qc.update.testset");
        fQcUpdate = Boolean.valueOf(fUpdateFlag);
        fTestSetFolder = fProp.getProperty("testLab.testSetFolder").trim();
        fTestSetName = fProp.getProperty("testLab.testSetName").trim();
        fBuildNumber = fProp.getProperty("buildNumber").trim();
    }

    /**
     * Constructor to set user-specified QC values.
     * 
     * @param testSetFolder
     *        QC Test-set folder name. Use double backslashes.
     * @param testSetName
     *        Name of test-set to use. Will be created if testset is not
     *        present in QC.
     * @param buildNumber
     *        APM Build number.
     * @param runName
     *        Name of test run to use when updating test run instance.
     */
    public QCUpdater(String testSetFolder, String testSetName, String buildNumber, String runName) {
        setObjectName("updater");
        setObjectType("qc");
        this.fTestSetFolder = testSetFolder;
        this.fTestSetName = testSetName;
        this.fBuildNumber = buildNumber;
        this.fBrowserName = runName;
        fUpdateFlag = fProp.getProperty("qc.update.testset");
        fQcUpdate = Boolean.valueOf(fUpdateFlag);
    }

    /**
     * Connect to Quality Center.
     */
    public void connect() {
        if (fQcUpdate) {
            fConnector = QCConnector.getInstance();
            if (!fConnector.isConnected()) {
                fConnector.qcConnectProject();
            }
            fQcWrap = new QCWrapper(fConnector.getQCConnection());
        }
    }

    /**
     * Disconnect from Quality Center.
     */
    public void disconnect() {
        if (fConnector != null) {
            fConnector.logout();
        }
    }

    /**
     * Update Quality Center: testset, testcase, status only if the
     * qc.update.testset flag is set to true.
     * 
     * @param tcData
     */
    public void uploadResult(TestCaseData tcData) {

        if (fQcUpdate) {
            logTestCase(DEBUG, "Updating Quality Center: [testset] \"" + fTestSetName
                + "\" [folder]" + " \"" + fTestSetFolder + "\"");

            // prevent automatic disconnect on timeout from server
            QCConnector.getInstance().getQCConnection().keepConnection();

            try {
                boolean folderFound = fQcWrap.setTestSetFolder(fTestSetFolder);
                if (folderFound) {
                    fQcWrap.setTestSet(fTestSetName);
                    fQcWrap.updateTestCaseStatus(tcData.getTestCaseId(), tcData.getStatus(),
                        fBuildNumber, fBrowserName);
                } else {
                    logTestCase(ERROR, "QC Update Error: Folder \"" + fTestSetFolder
                        + "\" wasn't found.");
                }
            } catch (Exception e) {
                logTestCase(ERROR, "Error during QC Updating. Check QC params "
                    + " in the qualitycenter.properites file.");
            }
        }
    }

    /**
     * Load properties from fileName and add to the properties object associated
     * to this class. <br>
     * Note that the fileName must be specified as a resource and exist in the
     * classpath.
     * <p>
     * Example:
     * <p>
     * <i>loadPropertiesFromFile("/com/ca/apm/qatf/contributed/mymodule/ myprops.properties");</i>
     * 
     * @param fileName
     *        Properties file in classpath.
     * @since QATF2.0
     */
    public final void loadProperties(final String fileName) {
        Properties tempProp = PropertyLoader.loadProperties(fileName);
        fProp.putAll(tempProp);
    }


}
