package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.ApplicationDefinition;
import com.ca.wily.cem.qa.api.ApplicationDefinitionFull;
import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.TemplateDefinition;

import java.rmi.RemoteException;

public class AdminBusinessApplicationHelper {

	protected CEMServices m_cemServices;

	/**
	 * Class constructor.
	 * 
	 * @param a_cemServices
	 *            handle for all available webservices.
	 */
	public AdminBusinessApplicationHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * 
	 * Create Business application in the system
	 * 
	 * 
	 * 
	 * @param applicationName
	 * @param applicationDescription
	 * @param appType
	 * @param appAuthType 
	 * @param caseSensitiveURL  : boolean
	 * @param caseSensitiveLogin : boolean
	 * @param appTimeOut
	 * @param appUserProcessing
	 * @param charEncoding
	 * @param timCollectorEM
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @return Returns the id of the created Business Application
	 */
	public long createBusinessApplication(String applicationName,
			String applicationDescription, String appType, String appAuthType,
			boolean caseSensitiveURL, boolean caseSensitiveLogin,
			String appTimeOut, String appUserProcessing, String charEncoding,
			String timCollectorEM) throws CEMWebServicesException,
			RemoteException {
		long businessApplicationId = 0;
		ApplicationDefinition[] businessapp= getAllBusinessApplications();
		for(ApplicationDefinition ad:businessapp)
			if(ad.getName().equals(applicationName))
					deleteBusinessApplicationByName(applicationName);
		businessApplicationId = m_cemServices.getInternalService()
				.createBusinessApplication(applicationName,
						applicationDescription, appType, appAuthType,
						caseSensitiveURL, caseSensitiveLogin,
						appUserProcessing, appTimeOut, charEncoding,
						timCollectorEM);
		System.out.println("Application " + applicationName + " Created Successfully");
		return businessApplicationId;
	}

	/**
	 * 
	 * Updated Business application specified by name
	 * 
	 * 
	 * @param appName
	 * @param newBusAppName
	 * @param newDescription
	 * @param newAppType
	 * @param newAppAuthType
	 * @param caseSensitiveURL
	 * @param caseSensitiveLogin
	 * @param appUserProcessing
	 * @param appTimeout
	 * @param charEncoding
	 * @param timCollectorEM
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public void updateBusinessApplicationByName(String appName,
			String newBusAppName, String newDescription, String newAppType,
			String newAppAuthType, boolean caseSensitiveURL,
			boolean caseSensitiveLogin, String appUserProcessing,
			String appTimeout, String charEncoding, String timCollectorEM) throws CEMWebServicesException, RemoteException {
			m_cemServices.getInternalService()
					.updateBusinessApplication(appName, newBusAppName,
							newDescription, newAppType, newAppAuthType,
							caseSensitiveURL, caseSensitiveLogin,
							appUserProcessing, appTimeout, charEncoding,
							timCollectorEM);
			System.out.println("Application " + appName + " Updated Successfully");
		
	}

	/**
	 * 
	 * Update Business Application specified by ID
	 * 
	 * 
	 * @param applicationId
	 * @param newBAName
	 * @param applicationDescription
	 * @param appType
	 * @param appAuthType
	 * @param caseSensitiveURL
	 * @param caseSensitiveLogin
	 * @param appUserProcessing
	 * @param appTimeout
	 * @param characterEncoding
	 * @param timCollectorEM
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public void updateBusinessApplicationById(long applicationId,
			String newBAName, String applicationDescription, String appType,
			String appAuthType, boolean caseSensitiveURL,
			boolean caseSensitiveLogin, String appUserProcessing,
			String appTimeout, String characterEncoding, String timCollectorEM) throws CEMWebServicesException, RemoteException {

			m_cemServices.getInternalService().updateBusinessApplicationById(
					applicationId, newBAName, applicationDescription, appType,
					appAuthType, caseSensitiveURL, caseSensitiveLogin,
					appUserProcessing, appTimeout, characterEncoding,
					timCollectorEM);
			
			System.out.println("Updated the business application: appid = "
					+ applicationId);
		
	}

	/**
	 * 
	 * Delete Business application specified by name
	 * 
	 * @param applicationName
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public void deleteBusinessApplicationByName(String applicationName) throws CEMWebServicesException, RemoteException {

	    ApplicationDefinition[] businessapp= getAllBusinessApplications();
        if(businessapp!=null)
	    for(ApplicationDefinition ad:businessapp)
            if(ad.getName().equals(applicationName)){
                TemplateDefinition[] templates = m_cemServices.getConfigurationDataOutService().getAllTransactionTemplates();
                if(templates!=null)
                for (TemplateDefinition templateDefinition : templates) {
                    if(templateDefinition.getAppName().equals(applicationName))
                        m_cemServices.getInternalService().deleteAutogenTemplate(templateDefinition.getName());            
                }
                m_cemServices.getInternalService().deleteBusinessApplication(
                            applicationName);
                    System.out
                            .println("Business application deleted successfully: name = "
                                    + applicationName);
            }
		

	}
	
	/**
     * 
     * Delete All Business applications
     * 
     * @throws RemoteException 
     * @throws CEMWebServicesException 
     */
    public void deleteAllBusinessApplications() throws CEMWebServicesException, RemoteException {

        ApplicationDefinition[] businessapp= getAllBusinessApplications();
        if(businessapp!=null)
        for(ApplicationDefinition ad:businessapp){
                TemplateDefinition[] templates = m_cemServices.getConfigurationDataOutService().getAllTransactionTemplates();
                if(templates!=null)
                for (TemplateDefinition templateDefinition : templates)
                    m_cemServices.getInternalService().deleteAutogenTemplate(templateDefinition.getName());
                if(!ad.getName().equalsIgnoreCase("Default Application")){
                    m_cemServices.getInternalService().deleteBusinessApplication(ad.getName());
                    System.out.println("Business application deleted successfully: name = "+ ad.getName());
                }                
            }
    }

	/**
	 * 
	 * Delete Business Application specified by Id
	 * 
	 * @param applicationId
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public void deleteBusinessApplicationById(long applicationId) throws CEMWebServicesException, RemoteException {

			m_cemServices.getInternalService().deleteBusinessApplicationById(
					applicationId);
			System.out
					.println("Business application deleted successfully: name = "
							+ applicationId);
		
	}

	/**
	 * This method returns an array of the Business Applications
	 * 
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public ApplicationDefinition[] getAllBusinessApplications()
			throws CEMWebServicesException, RemoteException {

		ApplicationDefinition[] applicationDefinition = m_cemServices
				.getConfigurationDataOutService()
				.getAllApplicationDefinitions();

		System.out.println("All Business Applications retrieved successfully: ");
		return applicationDefinition;
	}

	/**
	 * This method returns the Business Application specified by the business
	 * application Name.
	 * 
	 * @param businessApplicationName
	 *            - specifies the name of the business application to be
	 *            retrieved
	 * @return - Application definition object. * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public ApplicationDefinition getApplicationDefinitionByName(
			String businessApplicationName) throws CEMWebServicesException,
			RemoteException {

		ApplicationDefinition businessApplication = m_cemServices
				.getConfigurationDataOutService()
				.getApplicationDefinitionByName(businessApplicationName);

		System.out.println("Business Application retrieved successfully: " + businessApplicationName);

		return businessApplication;
	}

	/**
	 * This method returns the Business Application specified by the business
	 * application id.
	 * 
	 * @param businessApplicationId
	 *            - specifies the BusinessApplication Id. Please refer
	 *            ts_apps.ts_id() for the values
	 * 
	 * @return - Application definition object * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public ApplicationDefinition getApplicationDefinitionById(
			long businessApplicationId) throws CEMWebServicesException,
			RemoteException {

		ApplicationDefinition businessApplication = m_cemServices
				.getConfigurationDataOutService().getApplicationDefinitionById(
						businessApplicationId);
		System.out.println("Business Application retrieved successfully business id: " + businessApplicationId);
		return businessApplication;
	}

	/**
	 * Returns the complete Application Definition object with all the
	 * information
	 * 
	 * @param businessApplicationId
	 * @returns- full ApplicationDefinitionFull object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public ApplicationDefinitionFull getFullApplicationDefinitionById(
			long businessApplicationId) throws CEMWebServicesException,
			RemoteException {

		ApplicationDefinitionFull fullBusinessApplication = m_cemServices
				.getInternalService().getFullApplicationDefinitionById(
						businessApplicationId);
		System.out.println("Full Business Application retrieved successfully business id: " + businessApplicationId);
		return fullBusinessApplication;
	}

}
