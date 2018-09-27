/*
#  ~ Copyright (c) 2013. CA Technologies.  All rights reserved.
#  ~
#  ~
#  ~ Authors:  racsr02
#              Marina Kur (kurma05) - refactored original version
 */

package com.ca.apm.webui.test.framework.tools.qc;

import com.ca.apm.webui.test.framework.base.BaseTestObject;
import com.mercury.qualitycenter.otaclient.IList;


import com4j.Com4jObject;
import com4j.ComException;
import com4j.ExecutionException;

import java.net.UnknownHostException;

import com.mercury.qualitycenter.otaclient.IBaseFactory;
import com.mercury.qualitycenter.otaclient.IRun;
import com.mercury.qualitycenter.otaclient.IRunFactory;
import com.mercury.qualitycenter.otaclient.ITDConnection2;
import com.mercury.qualitycenter.otaclient.ITDFilter;
import com.mercury.qualitycenter.otaclient.ITSTest;
import com.mercury.qualitycenter.otaclient.ITestSet;
import com.mercury.qualitycenter.otaclient.ITestSetFactory;
import com.mercury.qualitycenter.otaclient.ITestSetFolder;
import com.mercury.qualitycenter.otaclient.ITestSetTreeManager;
import com.mercury.qualitycenter.otaclient.ITreeManager;

import com4j.COM4J;
import com4j.util.ComObjectCollector;


/**
 * The <code>QCWrapper</code> is responsible for making the calls to the QC Test
 * Director api.
 * 
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class QCWrapper extends BaseTestObject {

	private String _qcHostName = null;
	private ITDConnection2 _qcConnect = null;
	private ComObjectCollector _collector = null;
	private ITestSet _testSet = null;
	private ITestSetFolder _tsFolder = null;
	private ITreeManager treeManager = null;	

	private ITestSetTreeManager _testSetTreeManager = null;
    
  
	public QCWrapper(ITDConnection2 qcConnect) {

        setObjectName("ComWrapper");
        setObjectType("qc");

        if (qcConnect == null) {
            logTestCase(ERROR, "QC connection is null.");
            return;
        }

        try {
			this._qcConnect = qcConnect;
			if (_qcConnect.connected()) {
                logTestCase(TRACE, "Connected to ALM...");
				_testSetTreeManager = _qcConnect.testSetTreeManager().queryInterface(ITestSetTreeManager.class);
				treeManager = _qcConnect.treeManager().queryInterface(ITreeManager.class);
				_qcHostName = java.net.InetAddress.getLocalHost().getHostName();
			} else {
                logTestCase(ERROR, "Not connected to ALM...");
            }
			if (_collector == null) {
				_collector = new ComObjectCollector();
				COM4J.addListener(_collector);
			}

		} catch (UnknownHostException ex) {
            logTestCase(ERROR, "Error in getting ALM host address: " + ex.getMessage());
        } catch (Exception e) {
            logTestCase(ERROR, "Exception in com4Jwrapper Constructor :" + e.getMessage());
        }
    }

    /**
     * Update test case status.
     * 
     * @param testCaseID
     * @param tcStatus
     * @param buildNumber
     * @param runName
     */
    public void updateTestCaseStatus(String testCaseID, String tcStatus,
                                     String buildNumber, String runName) {
        boolean isTestCaseFound = false;

		try {
            ITSTest tstest = findTestCaseInTestSet(testCaseID);
			if (tstest == null) {
                logTestCase(ERROR, "Test instance doesn't exist in testSet");
                return;
            } // end param check

            // For 12.x
            Object tcrId = tstest.field("TS_USER_10");
            // For 13.x
            Object testId = tstest.testId();
            // For 12.x
            if (tcrId != null && testCaseID.equalsIgnoreCase(tcrId.toString())) {
                isTestCaseFound = true;
                updateFields(testCaseID, tcStatus, tstest, false, buildNumber, runName);
            } // end 12.x if
			// For 13.x
			else {
				if (testId != null	&& testId.toString().trim().equals(testCaseID)) {
                    isTestCaseFound = true;
                    updateFields(testCaseID, tcStatus, tstest, true,
                                 buildNumber, runName);
                } // end inner 13.x if
            } // end 12/13.x if..else

			if (!isTestCaseFound) {
                logTestCase(ERROR, testCaseID + "[ " + tcStatus + " ]" + " does not exist in QC test run.");
            } // end if tc not found

        } catch (Exception ex) {
            logTestCase(ERROR, "Error while trying to update test case:" + ex.getMessage());
			ex.printStackTrace();
        } // end catch
    } // end method

    /**
     * Update execution runs' fields in QC
     * 
     * @param testCaseID
     * @param tcStatus
     * @param tstest_
     * @param updateRNHost
     * @param buildNumber
     * @param runName
     */
    private void updateFields(String testCaseID, String tcStatus,
							 ITSTest tstest_, boolean updateRNHost, 
							 String buildNumber, String runName) {

        long t5 = System.currentTimeMillis();
        IRunFactory runFactory = tstest_.runFactory().queryInterface(IRunFactory.class);
        Com4jObject comRun = runFactory.addItem(tstest_.name());
        IRun run = comRun.queryInterface(IRun.class);

        if (updateRNHost) {
			run.field("RN_HOST", _qcHostName);
        } // end host name if

        run.status(tcStatus);
        run.field("RN_USER_01", buildNumber);
        run.field("RN_RUN_NAME", runName);
        run.copyDesignSteps();
        run.post();

        tstest_.autoPost(true);
        tstest_.field("TC_USER_01", buildNumber);

        logTestCase(TRACE, "TIME TOOK to run query in ms " + (System.currentTimeMillis() - t5));
        logTestCase(TRACE, "[tcID] \"" + testCaseID + "\" [status] \"" + tcStatus + "\" ... update complete.");
    } // end method

    /**
     * Adds test case to test set
     * 
     * @param testCaseID
     *            Quality Center test-case ID. This ID must exist in QC.
     * @return
     */
	public ITSTest populateTestSetWithTestCase(String testCaseID) {

		IBaseFactory testSetFactory = _testSet.tsTestFactory().queryInterface(IBaseFactory.class);
		if (testSetFactory != null) {
			try {
                ITSTest testInstance = testSetFactory.addItem(testCaseID)
                        .queryInterface(ITSTest.class);
                return testInstance;
			} catch (Exception e) {
                logTestCase(ERROR,  "Test case \"" + testCaseID + "\""
								+ " does not exist in QC. Retrying it as this shouldn't happen... "
								+ e.getMessage());
                int retries = 3;
                boolean updated = false;

				while (!updated && retries > 0) {
					try {
						_testSet.refresh();
						ITSTest testInstance = testSetFactory.addItem(testCaseID).
								queryInterface(ITSTest.class);
                        updated = true;
                        return testInstance;
					} catch (Exception ex) {
                        logTestCase(ERROR, "Test case \"" + testCaseID + "\"" +
								" does not exist in QC. Retrying again... "	+ e.getMessage());
                        retries--;
                    } // end inner try catch
                } // end while
            } // end catch
		} else {
            logTestCase(DEBUG, "testSetFactory is null");
        } // end else
        return null;
    } // end method

    /**
     * Finds test case in the test set
     * 
     * @param testCaseID
     * @return
     */
	public ITSTest findTestCaseInTestSet(String testCaseID) {

		IBaseFactory testSetFactory = _testSet.tsTestFactory().queryInterface(IBaseFactory.class);
        ITSTest itstest = null;

		if (testSetFactory != null) {
			ITDFilter iFilter = testSetFactory.filter().queryInterface(ITDFilter.class);

			iFilter.filter("TC_TEST_ID", testCaseID);// Filter for all test instances in test set
			IList iFilterList = testSetFactory.newList(iFilter.text()); // Create list from the filter	
            if (iFilterList.count() > 0)
				itstest = ((Com4jObject) iFilterList.item(1)).queryInterface(ITSTest.class);

			if (itstest != null) {
                logTestCase(TRACE, "Found TestSet " + (itstest.testId().equals(testCaseID)));
                return itstest;
            }
			else {		
                logTestCase(DEBUG, "Test instance doesn't exist in testSet - adding new instance.");
                return populateTestSetWithTestCase(testCaseID);
            } // end else
        } // end outer if
        return null;
    } // end method

    /**
     * Returns if test set folder exists or not.
     * 
     * @param testSetFolder
     * @return
     * @throws Exception
     */
    public boolean setTestSetFolder(String testSetFolder) throws Exception
    {
    	String testSetFolderPathConverted = null;
    	final String pathDelimeter = "/";
        boolean result = false;
		try {
        	logTestCase(DEBUG, "Test set folder is <" + testSetFolder + ">");    	
        	         	
        	if(testSetFolder != null && !testSetFolder.isEmpty())
            {
        		testSetFolderPathConverted = testSetFolder.replace(pathDelimeter,"\\");
        	}
        	
            Com4jObject comTSFolder = _testSetTreeManager.nodeByPath(testSetFolderPathConverted);

			if (comTSFolder != null) {
				_tsFolder = comTSFolder.queryInterface(ITestSetFolder.class);
				result = true;
            } else {
                // logger.info("comTSFolder is null");
            }
        } catch (ComException comex) {

            if (testSetFolderPathConverted != null) {
                logTestCase(DEBUG, "Test Set Folder \"" + testSetFolder + "\"" + " does not exist - creating new folder.");
                int retries = 5;
                boolean updated = false;

                String[] path = testSetFolder.split(pathDelimeter);

                while (retries > 0 && !updated) {
					ITestSetFolder topLevelNode = _testSetTreeManager.root()
							.queryInterface(ITestSetFolder.class);
                    boolean success = true;
                    ITestSetFolder testSetNode = null;

					for (int i = 1; i < path.length; i++) {
						try {
                            topLevelNode.refresh();
                            // check if the path already exists
							if (topLevelNode.findChildNode(path[i]) != null) {
								topLevelNode = topLevelNode.findChildNode(
										path[i]).queryInterface(
										ITestSetFolder.class);
								continue;
                            }
						} catch (ComException com) {
							try {
                                testSetNode = topLevelNode.addNodeDisp(path[i])
                                        .queryInterface(ITestSetFolder.class);
                                testSetNode.post();
                                updated = true;
                                topLevelNode = testSetNode;
                            } catch (Exception e) {
                                logTestCase(ERROR, "Exception while trying to add a new TestSet Node. Retrying again ");
                                success = false;
                                retries--;
                                break;
                            }

						} catch (ExecutionException exce) {
                            logTestCase(ERROR, "TestSet Folder doesn't exist Creating New TestSetFolder ");

							try {
								testSetNode = topLevelNode.addNodeDisp(path[i]).queryInterface(ITestSetFolder.class);
                                testSetNode.post();
                                updated = true;
                                topLevelNode = testSetNode;
							} catch (Exception e) {
                                logTestCase(ERROR, "Error while trying to add a new TestSet Node. Retrying again "
                                                    + e.getMessage());
                                success = false;
                                retries--;
                                break;
                            } // end inner catch
                        } // end outer catch
                    } // end for

                    if (success) {
                        updated = true;
						_tsFolder = topLevelNode;
						result = true;
                    } // end if
                } // end while
            } // end top-level if
        } catch (Exception e) {
            logTestCase(ERROR, "Exception while adding TestSetFolder: " + e.toString());
        } // end catch

        return result;
    } // end method

    /**
     * Returns if the test set exists in the test set folder or not.
     * 
     * @param testSetName
     * @return
     */
	public boolean setTestSet(String testSetName) {

        boolean result = false;
        try {
            int retries = 5;

            while (!result && retries > 0) {

                if (testSetName != null) {
                    // findTestSets will return all test sets in the folder if
                    // testSetName is null.
                    // Hence, ensure that testSetName is not null prior to using
                    // here.
                    IList testSetList = _tsFolder.findTestSets(testSetName, true, "");
                    if (testSetList != null && testSetList.count() > 0) {
						_testSet = ((Com4jObject) testSetList.item(1))
								.queryInterface(ITestSet.class);
                        result = true;
                        logTestCase(TRACE, "Found test set: \"" + _testSet.name() + "\"");
					} else {
						try {
                            if (testSetName != null) {
                                logTestCase(DEBUG, "Test set '"
												+ testSetName
												+ "' does not exist - creating new test set.");
								ITestSetFactory qcTestSetFac = _tsFolder.testSetFactory().queryInterface(
																	ITestSetFactory.class);
								_testSet = qcTestSetFac.addItem(testSetName).queryInterface(ITestSet.class);								
								_testSet.post();
                                result = true;
                            }
						} catch (Exception ex) {
							logTestCase(ERROR, "Error while creating TestSet. Retrying again.." + ex.getMessage());
                            retries--;
                        }
                    }
                } else {
                    // logger.info("TestSetName is Empty");
                    break;
                }
            }
        } catch (ComException comex) {
            logTestCase(ERROR, "FindTestSets failed for \"" + testSetName + "\"" + " with exception :" + comex.getMessage());
        } // end catch

        return result;
    } // end method 

} // end class