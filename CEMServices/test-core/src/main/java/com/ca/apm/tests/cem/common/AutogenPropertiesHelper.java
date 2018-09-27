package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.BusinessTransactionDefinition;
import com.ca.wily.cem.qa.api.CEMWebServicesException;

import java.rmi.RemoteException;

public class AutogenPropertiesHelper {

	protected CEMServices m_cemServices;
	

	/**
	 * 
	 * @param a_cemServices handle for all available webservices.
	 */
	public AutogenPropertiesHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * Sets the transaction discovery configuration properties and starts the autodiscovery
	 * 
	 * @param limitByNumber
	 *            - boolean value, to enable the number of transactions in the
	 *            discovered transaction business service.
	 * @param limitToNumber
	 *            - limit the number of transactions in discovered transaction
	 *            business service.
	 * @param limitByTime
	 *            - boolean value, to enable stop discovering transactions after
	 *            X minutes
	 * @param limitToTime
	 *            - number of minutes, transaction discovery to run before
	 *            stopped
	 * @param pathSeperator
	 *            - pathSeperator, valid values are
	 *            [" ","!","$",",","-",".",":",";","@","^","|","~","_"]
	 * @param discoverNew
	 *            - boolean value, to enable discover new non-identifying
	 *            components
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void setAndStartTransactionDiscovery(boolean limitByNumber,
			int limitToNumber, boolean limitByTime, int limitToTime,
			String pathSeperator, boolean discoverNew)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().setAndStartTransactionDiscovery(
				limitByNumber, limitToNumber, limitByTime, limitToTime,
				pathSeperator, discoverNew);
		System.out
				.println("Transaction Discovery properties are set and started successfully");
	}

	/**
	 * Sets the transaction discovery configuration properties
	 * 
	 * @param limitByNumber
	 *            - boolean value, to enable the number of transactions in the
	 *            discovered transaction business service.
	 * @param limitToNumber
	 *            - limit the number of transactions in discovered transaction
	 *            business service.
	 * @param limitByTime
	 *            - boolean value, to enable stop discovering transactions after
	 *            X minutes
	 * @param limitToTime
	 *            - number of minutes, transaction discovery to run before
	 *            stopped
	 * @param pathSeperator
	 *            - pathSeperator, valid values are
	 *            [" ","!","$",",","-",".",":",";","@","^","|","~","_"]
	 * @param discoverNew
	 *            - boolean value, to enable discover new non-identifying
	 *            components
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void setTransactionDiscoveryConfiguration(boolean limitByNumber,
			int limitToNumber, boolean limitByTime, int limitToTime,
			String pathSeperator, boolean discoverNew)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService()
				.setTransactionDiscoveryConfiguration(limitByNumber,
						limitToNumber, limitByTime, limitToTime, pathSeperator,
						discoverNew);
		System.out
				.println("Transaction Discovery properties are set successfully");

	}

	
	/**
	 * Starts the Transaction Discovery process
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void startTransactionDiscovery()
			throws CEMWebServicesException, RemoteException {
		
		m_cemServices.getInternalService()
		.stopTransactionDiscovery();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_cemServices.getInternalService()
				.startTransactionDiscovery();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out
				.println("Transaction Discovery started successfully");

	}

	/**
	 * Stops the Transaction Discovery Service
	 * Returns the number of transactions discovered
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public int stopTransactionDiscovery() throws CEMWebServicesException,
			RemoteException {
		
		int totalBusinessTxnsDiscovered = m_cemServices.getInternalService()
		.stopTransactionDiscovery();
		System.out.
		println("Transaction Discovery stopped successfully and No. of Txns discovered =" +totalBusinessTxnsDiscovered);
		return totalBusinessTxnsDiscovered;
		
	}
		
	/**
	 * Moves the Business Transactions from "Discovered Transactions" Business Service
	 * to the one specified in "newBusinessServiceName" Business Service
	 * 
	 * @param businessServiceName
	 * @param transactionName
	 * @param newBusinessServiceName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void moveDiscoveredTransactions(String businessServiceName,
			String transactionName, 
			String newBusinessServiceName) 
	throws CEMWebServicesException,RemoteException {
	
	m_cemServices.getInternalService()
			.moveBusinessTransaction("Discovered Transactions", transactionName, newBusinessServiceName);
	System.out.
			println("Discovered Business transactions moved successfully to Business Service :"+newBusinessServiceName);
		
}
	
	/** 
     * Gets all the Business Transactions discovered using Auto Discovery Service
     * by Business Service Name.The Business Service name is always "Discovered Transactions"
     * for Auto discovered transactions
     * 
     * @param businessServiceName
     * @param businessTransactionName
     * @return
     * @throws CEMWebServicesException
     * @throws RemoteException
     */
	public BusinessTransactionDefinition[] getAllDiscoveredTransactionsByName(
			)
	throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefinition[] allDiscoveredBusinessTransactions = m_cemServices.getConfigurationDataOutService()
																			.getBusinessTransactionDefinitions("Discovered Transactions");

		System.out
			.println("Retrieved all business transactions within Discovered Transactions Business Service");
		return allDiscoveredBusinessTransactions;

	}
	
	/** 
     * Gets all the Business Transactions discovered using Auto Discovery Service
     * by Business Service ID. The Business Service ID is always "700000000000000000"
     * for Auto discovered transactions
     * 
     * @param businessProcessDefinitionId
     * @return BusinessTransactionDefinition[]
     * @throws CEMWebServicesException
     * @throws RemoteException
     */
	 public BusinessTransactionDefinition[] getAllDiscoveredTransactionsById(
             )
             throws CEMWebServicesException, RemoteException {

		 BusinessTransactionDefinition[] allDiscoveredBusinessTransactions = m_cemServices
                             .getConfigurationDataOutService()
                             .getBusinessTransactionDefinitions(700000000000000000L);
		 System.out
		 		.println("Retrieved all business transactions within Discovered Transactions Business Service");
		 return allDiscoveredBusinessTransactions;
	 }
	 
	 /**
	  * 
	  * Gets enabled matching Business Transaction Definition for discovered and specified business transaction.  
	  * 
	  * 
	  * @param businessTxName
	  * @throws CEMWebServicesException
	  * @throws RemoteException
	  */
	 public BusinessTransactionDefinition getMatchingEnabledBTUsingBTName(String businessTxName) throws CEMWebServicesException, RemoteException{
		 
		 BusinessTransactionDefinition matchinEnabledBT = m_cemServices
         .getConfigurationDataOutService().getEnabledMatchingTransetByName("Discovered Transactions", businessTxName);
		 
		 return matchinEnabledBT;
	 }


	 /**
	  * 
	  * Gets enabled matching Business Transaction Definition for discovered and specified business transaction.
	  * 
	  * @param businessTxId
	  * @return
	  * @throws CEMWebServicesException
	  * @throws RemoteException
	  */
	 public BusinessTransactionDefinition getMatchingEnabledBTUsingBTId(long businessTxId) throws CEMWebServicesException, RemoteException{
		 
		 BusinessTransactionDefinition matchinEnabledBT = m_cemServices
         .getConfigurationDataOutService().getEnabledMatchingTransetByTransetId(businessTxId);
		 
		 return matchinEnabledBT;
	 }
	 
	/**
	 * Returns the number of transactions in discovered transactions
	 * 
	 * @return - number of transactions in discovered transactions business
	 *         service
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public int getTransactionDiscoveryNumber() throws CEMWebServicesException,
			RemoteException {
		System.out.println("No of trx's  " + m_cemServices.getConfigurationDataOutService().getTransactionDiscoveryNumber());
		return m_cemServices.getConfigurationDataOutService()
				.getTransactionDiscoveryNumber();
	}
	
	
}
