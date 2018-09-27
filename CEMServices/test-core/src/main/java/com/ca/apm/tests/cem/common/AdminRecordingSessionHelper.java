package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class AdminRecordingSessionHelper {

	protected CEMServices m_cemServices;

	/** Class constructor.
	 * 
	 * @param a_cemServices
	 *            handle for all available webservices. */

	public AdminRecordingSessionHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/** Create Recording session with TIMs as Monitor Type
	 * 
	 * @param recordingSessionName
	 * @param clientIp
	 * @param browserLangPattern
	 * @param contentType
	 * @param charEncoding
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public long createRecordingSessionWithTIM(String recordingSessionName,
			String clientIp, boolean useBrowserLanguagePattern,
			String browserLangPattern, String contentType, String charEncoding)
			throws CEMWebServicesException, RemoteException {
		long recordingSession = 0;
		RecordingSessionDefinition[] rsd=getAllRecordingSessionDefinitions();
		for(int i=0;i<rsd.length;i++){
			if(rsd[i].getSessionName().equalsIgnoreCase(recordingSessionName))
			m_cemServices.getInternalService().deleteRecordingSession(recordingSessionName);
		}
		recordingSession = m_cemServices.getInternalService()
				.createRecordingSession("1", recordingSessionName, clientIp,
						browserLangPattern, useBrowserLanguagePattern, null,
						contentType, charEncoding);
		System.out.println("Recording Session " + recordingSession +" created successfully");
		return recordingSession;

	}

	/** Create Recording session with Agents as Monitor Type
	 * 
	 * @param recordingSessionName
	 * @param clientIp
	 * @param browserLangPattern
	 * @param agentRegEx
	 * @param contentType
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public long createRecordingSessionWithAgent(String recordingSessionName,
			String clientIp, String browserLangPattern,
			boolean useBrowserLanguagePattern, String agentIdentifierRegEx,
			String contentType) throws CEMWebServicesException, RemoteException {
		long recordingSession = 0;
		recordingSession = m_cemServices.getInternalService()
				.createRecordingSession("2", recordingSessionName, clientIp,
						browserLangPattern, useBrowserLanguagePattern,
						agentIdentifierRegEx, contentType, null);
		return recordingSession;
	}

	/** Start a recording session using recording session name
	 * 
	 * @param recordingSessionName
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void startRecordingSessionUsingName(String recordingSessionName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().startRecordingSession(
				recordingSessionName);
	}

	/** Start a recording session using recording session id
	 * 
	 * @param recordingSessionId
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void startRecordingSessionUsingId(long recordingSessionId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().startRecordingSessionById(
				recordingSessionId);
	}

	/** Stop a recording session using recording session name
	 * 
	 * @param recordingSessionName
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void stopRecordingSessionUsingName(String recordingSessionName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().stopRecordingSession(
				recordingSessionName);
	}

	/** Stop a recording session using recording session Id
	 * 
	 * @param recordingSessionId
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void stopRecordingSessionUsingId(long recordingSessionId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().stopRecordingSessionById(
				recordingSessionId);
	}

	/** Get array of details for all recording session in the system
	 * 
	 * @return array of RecordingSessionDefinition objects
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingSessionDefinition[] getAllRecordingSessionDefinitions()
			throws CEMWebServicesException, RemoteException {

		RecordingSessionDefinition[] allRecordingSessionDefns;
		allRecordingSessionDefns = m_cemServices
				.getConfigurationDataOutService()
				.getAllRecordingSessionDefinitions();
		return allRecordingSessionDefns;

	}

	/** Get recording session details using recording session Name
	 * 
	 * @param recordingSessionName
	 * @return Return a RecordingSessionDefinition object for recording session
	 *         specified by name
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingSessionDefinition getRecordingSessionDetailsUsingRecordingSessionName(
			String recordingSessionName) throws CEMWebServicesException,
			RemoteException {

		RecordingSessionDefinition recordingSessionDefn;
		recordingSessionDefn = m_cemServices.getConfigurationDataOutService()
				.getRecordingSessionDefinition(recordingSessionName);
		return recordingSessionDefn;

	}

	/** Get recording session details using recording session Id
	 * 
	 * @param recordingSessionId
	 * @return Return a RecordingSessionDefinition object for recording session
	 *         specified by name
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingSessionDefinition getRecordingSessionDetailsUsingRecordingSessionId(
			long recordingSessionId) throws CEMWebServicesException,
			RemoteException {

		RecordingSessionDefinition recordingSessionDefn;
		recordingSessionDefn = m_cemServices.getConfigurationDataOutService()
				.getRecordingSessionDefinitionById(recordingSessionId);
		return recordingSessionDefn;

	}

	/** Get all recorded tx for a recording session specified by recording
	 * session name
	 * 
	 * @param recordingSessionName
	 * @return Returns an array of all recorded Txs for a Recording Session
	 *         specified by Name
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingComponentDefinition[] getAllRecordedTxsUsingRecordingSessionName(
			String recordingSessionName) throws CEMWebServicesException,
			RemoteException {

		RecordingComponentDefinition[] recordingComponentsDefns;
		if (m_cemServices.getConfigurationDataOutService() == null) {
			System.out.println("YEs configuration data out is null!!!!! ");
		}
		recordingComponentsDefns = m_cemServices
				.getConfigurationDataOutService().getAllRecordingComponents(
						recordingSessionName);
		return recordingComponentsDefns;

	}

	/** Get all recorded tx for a recording session specified by recording
	 * session id
	 * 
	 * @param recordingSessionId
	 * @return Returns an array of all recording components for a Recording
	 *         Session specified by Id
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingComponentDefinition[] getAllRecordedTxsUsingRecordingSessionId(
			long recordingSessionId) throws CEMWebServicesException,
			RemoteException {

		RecordingComponentDefinition[] recordingComponentsDefns;
		recordingComponentsDefns = m_cemServices
				.getConfigurationDataOutService()
				.getAllRecordingtComponentsById(recordingSessionId);
		return recordingComponentsDefns;

	}

	public RecordingComponentDefinition getIdentifyingTransactionInRecordingSession(
			String recordingSessionName) throws CEMWebServicesException,
			RemoteException {
		RecordingComponentDefinition identifyingRecordingComponentDefinition = null;
		RecordingComponentDefinition[] ss = m_cemServices
				.getConfigurationDataOutService().getAllRecordingComponents(
						recordingSessionName);
		for (RecordingComponentDefinition s : ss) {
			if (s.getIdentifying() == true) {
				System.out.println("Original Recorded Component Name is: "
						+ s.getComponentName());
				identifyingRecordingComponentDefinition = s;
				break;
			}

		}
		return identifyingRecordingComponentDefinition;
	}

	/** Get recorded components for a recorded Tx using recorded Tx ID
	 * 
	 * @param recordingSessionId
	 * @param recordingComponentId
	 * @return Returns an array of recorded components for Recorded Tc specified
	 *         by ID
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingComponentDefinition[] getRecordedComponentsForRecordedTxUsingRecordedTxId(
			long recordingSessionId, long recordedTxId)
			throws CEMWebServicesException, RemoteException {

		RecordingComponentDefinition[] recordingComponentsDefns;
		recordingComponentsDefns = m_cemServices
				.getConfigurationDataOutService().getRecordingComponentsById(
						recordingSessionId, recordedTxId);
		return recordingComponentsDefns;

	}

	/** Get recorded parameter for a recorded Tx using recorded Tx ID
	 * 
	 * @param recordingSessionId
	 * @param recordedTxId
	 * @return Returns the array of Recorded Parameters for Recorded Tx
	 *         specified by id
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public RecordingParamsDefinition[] getRecordedParamsForRecordedTxUsingRecordedTxId(
			long recordingSessionId, long recordedTxId)
			throws CEMWebServicesException, RemoteException {

		RecordingParamsDefinition[] recordingParamDefns;
		recordingParamDefns = m_cemServices.getConfigurationDataOutService()
				.getRecordingParamsById(recordingSessionId, recordedTxId);
		return recordingParamDefns;

	}

	/** Update a recording session using recording session name
	 * 
	 * @param recordingSessionName
	 * @param newRecordingSessionName
	 * @param clientIp
	 * @param browserLangPattern
	 * @param useBrowserLanguagePattern
	 * @param agentIdentifierRegEx
	 * @param contentType
	 * @param charEncoding
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void updateRecordingSession(String recordingSessionName,
			String newRecordingSessionName, String clientIp,
			String browserLangPattern, boolean useBrowserLanguagePattern,
			String agentIdentifierRegEx, String contentType, String charEncoding)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateRecordingSession(
				recordingSessionName, newRecordingSessionName, clientIp,
				browserLangPattern, useBrowserLanguagePattern,
				agentIdentifierRegEx, contentType, charEncoding);
	}

	/** Delete a recording session using recording session name
	 * 
	 * @param recordingSessionName
	 * @param newRecordingSessionName
	 * @param clientIp
	 * @param browserLangPattern
	 * @param useBrowserLanguagePattern
	 * @param agentIdentifierRegEx
	 * @param contentType
	 * @param charEncoding
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void updateRecordingSession(String recordingSessionName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteRecordingSession(
				recordingSessionName);
	}

	/** Promote recorded tx to from specified recording session to the specified
	 * business service
	 * 
	 * @param recordingSessionName
	 * @param recordedTxId
	 * @param businessServiceName
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void promoteRecordedTxUsingRecordingSessionName(
			String recordingSessionName, long recordedTxId,
			String businessServiceName) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().promoteRecordingComponent(
				recordingSessionName, recordedTxId, businessServiceName);
	}

	/** Promote recorded tx to from specified recording session to the specified
	 * business service and overwrite the specified Business Transaction
	 * 
	 * @param recordingSessionName
	 * @param recordedTxId
	 * @param businessServiceName
	 * @param businessTxNameToOverwrite
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void promoteRecordedTxAndOverwriteBTUsingRecordingSessionName(
			String recordingSessionName, long recordedTxId,
			String businessServiceName, String businessTxNameToOverwrite)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().promoteRecordingComponent(
				recordingSessionName, recordedTxId, businessServiceName,
				businessTxNameToOverwrite);
	}

	/** Promote recorded tx to from specified recording session to the specified
	 * business service
	 * 
	 * @param recordingSessionId
	 * @param recordedTxId
	 * @param businessServiceId
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void promoteRecordedTxUsingRecordingSessionId(
			long recordingSessionId, long recordedTxId, long businessServiceId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().promoteRecordingComponentById(
				recordingSessionId, recordedTxId, businessServiceId);
	}

	/** Promote recorded tx to from specified recording session to the specified
	 * business service and overwrite the specified Business Transaction
	 * 
	 * @param recordingSessionId
	 * @param recordedTxId
	 * @param businessServiceId
	 * @param businessTxNameToOverwriteId
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void promoteRecordedTxAndOverwriteBTUsingRecordingSessionId(
			long recordingSessionId, long recordedTxId, long businessServiceId,
			long businessTxNameToOverwriteId) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().promoteRecordingComponentById(
				recordingSessionId, recordedTxId, businessServiceId,
				businessTxNameToOverwriteId);
	}

	/** Returns the matching enabled Business Transaction for the specified
	 * recorded transaction id
	 * 
	 * @param recordedTxId
	 * @return Returns the BusinessTransactionDefinition object of matching
	 *         enabled BT
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public BusinessTransactionDefinition getMatchingEnabledBusinessTx(
			long recordedTxId) throws CEMWebServicesException, RemoteException {

		return m_cemServices.getConfigurationDataOutService()
				.getEnabledMatchingTransetByRecordingCompId(recordedTxId);

	}

	/** Returns an array of all Business Transaction Definitions for a specified
	 * Business Process Definition that can be used as a replacement. That
	 * includes request business transactions only that do not have extended
	 * business transactions.
	 * 
	 * @param businessServiceName
	 * @return Returns array of BusinessTransactionDefinition objects
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public BusinessTransactionDefinition[] getBTxListToOverwriteInBusinessServiceUsingBSName(
			String businessServiceName) throws CEMWebServicesException,
			RemoteException {

		return m_cemServices.getConfigurationDataOutService()
				.getTransetReplacementsFromBusinessProcess(businessServiceName);

	}

	/** Returns an array of all Business Transaction Definitions for a specified
	 * Business Process Definition that can be used as a replacement. That
	 * includes request business transactions only that do not have extended
	 * business transactions.
	 * 
	 * @param businessServiceId
	 * @return Returns array of BusinessTransactionDefinition objects
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public BusinessTransactionDefinition[] getBTxListToOverwriteInBusinessServiceUsingBSId(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {

		return m_cemServices.getConfigurationDataOutService()
				.getTransetReplacementsFromBusinessProcessId(businessServiceId);

	}

	/** Deletes the recording session specified by name.
	 * 
	 * @param recordingSessionName
	 * @throws CEMWebServicesException
	 * @throws RemoteException */
	public void deleteRecordingSessionUsingName(String recordingSessionName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteRecordingSession(
				recordingSessionName);
		System.out.println("Recording session " + recordingSessionName +  "  deleted");

	}
	
	/**
	 * 
	 * This method returns the array of  all identifying recorded transactions in a recording session
	 * 
	 * @param recordingSessionName
	 * @return Array of RecordingComponentDefinition 
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public RecordingComponentDefinition[] getAllIdentifyingRecordedTxsUsingRecordingSessionName(
			String recordingSessionName) throws CEMWebServicesException,
			RemoteException {

		 RecordingComponentDefinition[] recordingComponentsDefns = m_cemServices
				.getConfigurationDataOutService().getAllRecordingComponents(
						recordingSessionName);
		
		ArrayList<RecordingComponentDefinition> identifyingRecordingComponentsDefns
		= new ArrayList<RecordingComponentDefinition>();
		
		
		if(recordingComponentsDefns.length >0){
			for (RecordingComponentDefinition recordingComponentDefinition : recordingComponentsDefns) {
				if(recordingComponentDefinition.getIdentifying()){
					identifyingRecordingComponentsDefns.add(recordingComponentDefinition);
				}
			}
		}else{
			System.out.println("There are no recorded transactions!");
		}
		return identifyingRecordingComponentsDefns.toArray(new RecordingComponentDefinition[identifyingRecordingComponentsDefns.size()]);

	}
	
	public void updateRecordingComponent(String recordingSessionName,
			long recordingComponentId, String recordingComponentName,
			String comments, boolean included, boolean identifying,
			boolean cacheable) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateRecordingComponent(
				recordingSessionName, recordingComponentId,
				recordingComponentName, comments, identifying, included,
				cacheable);

	}

}
