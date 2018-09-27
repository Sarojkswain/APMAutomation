package com.ca.apm.tests.cem.common;

/**
 * CommonFrmaework Helper class that assists in doing all Business Service related 
 * operations 
 * 
 * @author murma13
 */

import com.ca.wily.cem.qa.api.BusinessProcessDefinition;
import com.ca.wily.cem.qa.api.BusinessProcessDefinitionFull;
import com.ca.wily.cem.qa.api.BusinessTransactionDefinition;
import com.ca.wily.cem.qa.api.CEMWebServicesException;

import java.rmi.RemoteException;

public class AdminBusinessServiceHelper {

	protected CEMServices m_cemServices;

	/**
	 * Class constructor.
	 * 
	 * @param a_cemServices
	 *            handle for all available webservices.
	 */

	public AdminBusinessServiceHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * Create a business service in a Business Application specified by name
	 * with customized SL values
	 * 
	 * @param businessServiceName
	 * @param businesssServiceDescription
	 * @param applicationName
	 * @param importanceInherited
	 * @param successRateSlaInherited
	 * @param sigmaSlaInherited
	 * @param tranTimeSlaInherited
	 * @param importanceValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @return  Returns the id of the Business Service Created
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public long createBusinessServiceWithCustomSLAValues(
			String businessServiceName, String businesssServiceDescription,
			String applicationName, boolean importanceInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, String importanceValue,
			String successRateSlaValue, String sigmaSlaValue,
			String transTimeValue) throws CEMWebServicesException, RemoteException {
	
		 	long businessServiceId; 
		 	businessServiceId = m_cemServices.getInternalService().createBusinessService(
					businessServiceName, businesssServiceDescription,
					applicationName, importanceInherited,
					successRateSlaInherited, sigmaSlaInherited,
					tranTimeSlaInherited, importanceValue, successRateSlaValue,
					sigmaSlaValue, transTimeValue);
		 	System.out.println("Business Service " + businessServiceName + " Created Successfully");
		 	return businessServiceId;
	}

	/**
	 * 
	 * Create a Business Service with inherited SLA values in a business
	 * application specified by name
	 * 
	 * @param businessServiceName
	 * @param businessServiceDescription
	 * @param businessApplicationName
	 * @return  Returns the id of the Business Service Created
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public long createBusinessServiceWithInheritedSLAValues(
			String businessServiceName, String businessServiceDescription,
			String businessApplicationName) throws CEMWebServicesException,
			RemoteException {
		long businessServiceId = 0;

		businessServiceId = m_cemServices.getConfigurationDataInService()
				.createBusinessService(businessServiceName,
						businessServiceDescription, businessApplicationName);
		System.out.println("Business Service " + businessServiceName + " Created Successfully");
		return businessServiceId;
	}

	/**
	 * 
	 * Delete Business service specified by name
	 * 
	 * @param businessServiceName
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 * 
	 */
	public void deleteBusinessService(String businessServiceName) throws CEMWebServicesException, RemoteException {
			m_cemServices.getInternalService().deleteBusinessService(
					businessServiceName);
			System.out.println("Business Service deleted successfully: "
					+ businessServiceName);
		
	}

	/**
	 * 
	 * Delete the business service by specifying business service ID
	 * @param businessServiceId
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public void deleteBusinessServiceById(long businessServiceId) throws CEMWebServicesException, RemoteException {
			m_cemServices.getInternalService().deleteBusinessServiceById(
					businessServiceId);
			System.out.println("Business Service deleted successfully: "
					+ businessServiceId);
		
	}

	/**
	 * 
	 * update business service specified by Name with custom SLA values
	 * 
	 * 
	 * 
	 * @param businessServiceName
	 * @param newBusinessServiceName
	 * @param serviceDescription
	 * @param applicationName
	 * @param transactionImpactLevelInherited
	 * @param successRateSlaInherited
	 * @param sigmaSlaInherited
	 * @param tranTimeSlaInherited
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateBusinessServiceByName(String businessServiceName,
			String newBusinessServiceName, String serviceDescription,
			String applicationName, boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, String transactionImpactLevelValue,
			String successRateSlaValue, String sigmaSlaValue,
			String transTimeValue) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().updateBusinessService(
				businessServiceName, newBusinessServiceName,
				serviceDescription, applicationName,
				transactionImpactLevelInherited, successRateSlaInherited,
				sigmaSlaInherited, tranTimeSlaInherited,
				transactionImpactLevelValue, successRateSlaValue,
				sigmaSlaValue, transTimeValue);

	}

	/**
	 * * update business service specified by Id with custom SLA values
	 * 
	 * 
	 * 
	 * 
	 * @param businessServiceId
	 * @param newBusinessServiceName
	 * @param businessServiceDescription
	 * @param businessApplicationName
	 * @param importanceInherited
	 *            - when true the value importanaceValue is not overridden
	 * @param successRateSlaInherited
	 *            - when true the value successRaleValue is not overridden
	 * @param sigmaSlaInherited
	 *            - when true the value sigmaSlaValue is not overridden
	 * @param tranTimeSlaInherited
	 *            - when true the value transTimeSLAValue is not overridden
	 * @param importanceValue
	 * @param successRateValue
	 * @param sigmaSlaValue
	 * @param tranTimeSlaValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateBusinessServiceById(long businessServiceId,
			String newBusinessServiceName, String businessServiceDescription,
			String businessApplicationName, boolean importanceInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, String importanceValue,
			String successRateValue, String sigmaSlaValue,
			String tranTimeSlaValue) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().updateBusinessServiceById(
				businessServiceId, newBusinessServiceName,
				businessServiceDescription, businessApplicationName,
				importanceInherited, successRateSlaInherited,
				sigmaSlaInherited, tranTimeSlaInherited, importanceValue,
				successRateValue, sigmaSlaValue, tranTimeSlaValue);
		
		System.out.println("Business Service id" + businessServiceId + " updated Successfully");

	}

	/**
	 * This method returns an array of business services matching the specified
	 * Application Id
	 * 
	 * @param applicationId
	 * @returns - Business Process (Business Service object) matching the
	 *          specified ID.
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessProcessDefinition[] getBusinessProcessDefinitionsByAppId(
			long applicationId) throws CEMWebServicesException, RemoteException {

		BusinessProcessDefinition[] allBusinessProcessesUsingAppId = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessProcessDefinitionsByAppId(applicationId);
		System.out
				.println(" Retrival of Business Service using application id  successful : ");

		return allBusinessProcessesUsingAppId;
	}

	/**
	 * This method Returns the array of all Business Services
	 * 
	 * @return - Array of Business Services
	 */
	public BusinessProcessDefinition[] getAllBusinessProcessDefinitions()
			throws CEMWebServicesException, RemoteException {

		BusinessProcessDefinition[] allBusinessService = m_cemServices
				.getConfigurationDataOutService()
				.getAllBusinessProcessDefinitions();
		System.out.println(" Retrival of All Business Service successful : ");

		return allBusinessService;
	}

	/**
	 * This method returns an array business services matching the given REGEX
	 * pattern.
	 * 
	 * @param pattern
	 *            - Regex patterns 1. \Quote the next metacharacter 2. ^ Match
	 *            the beginning of the line 3. . Match any character (except
	 *            newline) 4. $ Match the end of the line (or before newline at
	 *            the end) 5. | Alternation 6. () Grouping 7. [] Character class
	 *            Sample Pattern - 1. To get all - ^.* 2. To get BS starting
	 *            with "Test" - ^Test.*
	 * @return Array of the business process definition.
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessProcessDefinition[] getFilteredBusinessProcessDefinitions(
			String pattern) throws CEMWebServicesException, RemoteException {

		BusinessProcessDefinition[] filteredBusinessService = m_cemServices
				.getConfigurationDataOutService()
				.getFilteredBusinessProcessDefinitions(pattern);
		System.out
				.println(" Retrival of All Business Service matching the pattern is successful.");

		return filteredBusinessService;
	}

	/**
	 * This method returns the business service object matching the specified
	 * business service id.
	 * 
	 * @param businessServiceId
	 *            - business service Id - this can be obtained from Db -
	 *            ts_transet_groups.ts_id field
	 * @return - the business service object matching the specified business
	 *         service id
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessProcessDefinition getBusinessProcessDefinitionsById(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {

		BusinessProcessDefinition bService = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessProcessDefinitionById(businessServiceId);
		System.out.println(" Retrival of Business Service successful : "
				+ bService.getName());
		return bService;
	}

	/**
	 * This method returns the business service object matching the specified
	 * business service name
	 * 
	 * @param businessServiceName
	 *            - specifies the business service name
	 * @return - business service object matching the specified name
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessProcessDefinition getBusinessProcessDefinitionsByName(
			String businessServiceName) throws CEMWebServicesException,
			RemoteException {

		BusinessProcessDefinition bServiceByName = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessProcessDefinitionByName(businessServiceName);

		System.out.println(" Retrival of Business Service successful");

		return bServiceByName;
	}

	/**
	 * Return the full business Service object matching the specified by the Id
	 * 
	 * @param businessServiceId
	 *            - Specifies the business service Id
	 * @return - Business service full details object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessProcessDefinitionFull getFullBusinessProcessDefinitionsByName(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {

		BusinessProcessDefinitionFull bServiceFull = m_cemServices
				.getInternalService().getFullBusinessProcessDefinitionById(
						businessServiceId);

		System.out.println(" Retrival of Business Service successfull");

		return bServiceFull;
	}
	
	/**
	 * 
	 * Returns the CSV data for all the business services specified in an array
	 * 
	 * @param businessServiceNames
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public byte[] getCSVDataForBusinessService(
			String[] businessServiceNames) throws CEMWebServicesException,
			RemoteException {

		byte[] csvData = m_cemServices.getInternalService().getCSVDataForBusinessServices(businessServiceNames);

		System.out.println(" Retrival of CSV data is successfull");

		return csvData;
	}
	
	/**
	 * 
	 * Returns the CSV data for all the business services specified in an array
	 * 
	 * @param businessServiceNames
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void deleteAllBusinessTransactionFromBSById(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {
		
		BusinessTransactionDefinition[] BTransactionsById =m_cemServices.getConfigurationDataOutService()
		.getFilteredBusinessTransactionDefinitions(
				businessServiceId, ".*");
		for (BusinessTransactionDefinition businessTransactionDefinition : BTransactionsById) {
			m_cemServices.getInternalService().deleteBusinessTransactionById(businessTransactionDefinition.getId());
		}
		
		System.out.println(" All Business Transactions are deleted successfully from Business Service Id= " + businessServiceId);

	}

}
