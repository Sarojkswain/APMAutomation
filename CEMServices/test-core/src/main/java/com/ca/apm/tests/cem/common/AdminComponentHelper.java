package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.ComponentDefinition;

import java.rmi.RemoteException;

/**
 * @author jotpr01
 * 
 */
public class AdminComponentHelper {

	protected CEMServices m_cemServices;
	protected ComponentDefinition component;

	public AdminComponentHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * This method creates an Identifying Component for a given Business
	 * Service, Business Transaction and Transaction
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public long createIdentifyingComponent(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {
		long componentId = 0;

		try {
			System.out.println("Calling createIdentifyingComponent");
			componentId = m_cemServices.getInternalService().createComponent(
					businessServiceName, businessTransactionName,
					transactionName, componentName, componentDescription, true,
					included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out.println("Could not create the Identifying Component: "
					+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out.println("Could not create the Identifying Component: "
					+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out.println("Could not create the Identifying Component: "
					+ componentName);
			e.printStackTrace();
		}
		return componentId;

	}

	/**
	 * This method creates an Identifying Component for a given Transaction Id
	 * 
	 * @param transactionId
	 * @param componentName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public long createIdentifyingComponentById(long transactionId,
			String componentName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {
		long componentId = 0;
		try {
			componentId = m_cemServices.getInternalService()
					.createComponentById(transactionId, componentName,
							componentDescription, true, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the Identifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the Identifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not create the Identifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();
		}
		return componentId;
	}

	/**
	 * This method creates a Non Identifying Component for a given Business
	 * Service, Business Transaction and Transaction
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public long createNonIdentifyingComponent(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {
		long componentId = 0;
		try {
			componentId = m_cemServices.getInternalService().createComponent(
					businessServiceName, businessTransactionName,
					transactionName, componentName, componentDescription,
					false, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the NonIdentifying Component: "
							+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the NonIdentifying Component: "
							+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not create the NonIdentifying Component: "
							+ componentName);
			e.printStackTrace();
		}
		return componentId;

	}

	/**
	 * This method creates a Non Identifying Component for a transaction Id
	 * 
	 * @param transactionId
	 * @param componentName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public long createNonIdentifyingComponentById(long transactionId,
			String componentName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {
		long componentId = 0;
		try {

			componentId = m_cemServices.getInternalService()
					.createComponentById(transactionId, componentName,
							componentDescription, false, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the NonIdentifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not create the NonIdentifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not create the NonIdentifying Component by transactionId: "
							+ componentName);
			e.printStackTrace();
		}
		return componentId;
	}

	/**
	 * This method updates an Identifying Component for a given Business
	 * Service, Business Transaction and Transaction. Provide NULL for the
	 * params that you don't want to change.
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param newName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public void updateIdentifyingComponent(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String newName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {

		try {

			m_cemServices.getInternalService().updateComponent(
					businessServiceName, businessTransactionName,
					transactionName, componentName, newName,
					componentDescription, true, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out.println("Could not update the Identifying Component: "
					+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out.println("Could not update the Identifying Component: "
					+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out.println("Could not update the Identifying Component: "
					+ componentName);
			e.printStackTrace();
		}

	}

	/**
	 * @param componentId
	 * @param newName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public void updateIdentifyingComponentById(long componentId,
			String newName, String componentDescription, Boolean identifying,
			Boolean included, Boolean cacheable) {

		try {
			m_cemServices.getInternalService().updateComponentById(componentId,
					newName, componentDescription, true, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the Identifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the Identifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not update the Identifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param newName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public void updateNonIdentifyingComponent(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String newName, String componentDescription,
			Boolean identifying, Boolean included, Boolean cacheable) {

		try {
			m_cemServices.getInternalService().updateComponent(
					businessServiceName, businessTransactionName,
					transactionName, componentName, newName,
					componentDescription, false, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the Non Identifying Component: "
							+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the Non Identifying Component: "
							+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not update the Non Identifying Component: "
							+ componentName);
			e.printStackTrace();
		}

	}

	/**
	 * @param componentId
	 * @param newName
	 * @param componentDescription
	 * @param identifying
	 * @param included
	 * @param cacheable
	 */
	public void updateNonIdentifyingComponentById(long componentId,
			String newName, String componentDescription, Boolean identifying,
			Boolean included, Boolean cacheable) {

		try {
			m_cemServices.getInternalService().updateComponentById(componentId,
					newName, componentDescription, false, included, cacheable);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the NonIdentifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:createComponent ");
			System.out
					.println("Could not update the NonIdentifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not update the NonIdentifying Component by Component Id: "
							+ componentId);
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 */
	public void deleteComponent(String businessServiceName,
			String businessTransactionName, String transactionName,
			java.lang.String componentName) {

		try {
			m_cemServices.getInternalService().deleteComponent(
					businessServiceName, businessTransactionName,
					transactionName, componentName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out.println("Could not delete the Component: "
					+ componentName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not delete the NonIdentifying Component: "
							+ componentName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not delete the NonIdentifying Component : "
							+ componentName);
			e.printStackTrace();
		}

	}

	/**
	 * @param componentId
	 */
	public void deleteComponentById(long componentId) {

		try {
			m_cemServices.getInternalService().deleteComponentById(componentId);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not delete the Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not delete the Component by Component Id: "
							+ componentId);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not delete the Component by Component Id: "
							+ componentId);
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param defectName
	 * @param bEnabled
	 * @param bLocked
	 * @param condition
	 * @param headerValue
	 * @param conditionValue
	 * @param importance
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public long createComponentSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName, Boolean bEnabled, Boolean bLocked,
			String condition, String headerValue, String conditionValue,
			String importance) throws CEMWebServicesException, RemoteException {
		long CompSpecificationId = 0;
		try {
			CompSpecificationId = m_cemServices.getInternalService()
					.createComponentSpecification(businessServiceName,
							businessTransactionName, transactionName,
							defectName, bEnabled, bLocked, condition,
							headerValue, conditionValue, importance);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not create the Component Specification of type : "
							+ condition
							+ "and Name"
							+ defectName
							+ "for"
							+ businessServiceName
							+ "->"
							+ businessTransactionName + "->" + transactionName);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not create the Component Specification of type : "
							+ condition
							+ "and Name"
							+ defectName
							+ "for"
							+ businessServiceName
							+ "->"
							+ businessTransactionName + "->" + transactionName);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not create the Component Specification of type : "
							+ condition
							+ "and Name"
							+ defectName
							+ "for"
							+ businessServiceName
							+ "->"
							+ businessTransactionName + "->" + transactionName);
			e.printStackTrace();
		}
		return CompSpecificationId;

	}

	/**
	 * @param transactionId
	 * @param defectName
	 * @param bEnabled
	 * @param bLocked
	 * @param condition
	 * @param headerValue
	 * @param conditionValue
	 * @param importance
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public long createComponentSpecificationById(long transactionId,
			String defectName, Boolean bEnabled, Boolean bLocked,
			String condition, String headerValue, String conditionValue,
			String importance) throws CEMWebServicesException, RemoteException {
		long CompSpecificationId = 0;
		try {
			CompSpecificationId = m_cemServices.getInternalService()
					.createComponentSpecificationById(transactionId,
							defectName, bEnabled, bLocked, condition,
							headerValue, conditionValue, importance);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not create the Component Specification by transactionId : "
							+ transactionId
							+ ":Condition is"
							+ condition
							+ ":Condition Value is" + conditionValue);
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not create the Component Specification by transactionId : "
							+ transactionId
							+ ":Condition is"
							+ condition
							+ ":Condition Value is" + conditionValue);
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not create the Component Specification by transactionId : "
							+ transactionId
							+ ":Condition is"
							+ condition
							+ ":Condition Value is" + conditionValue);
			e.printStackTrace();
		}
		return CompSpecificationId;
	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param defectName
	 * @param bEnabled
	 * @param bLocked
	 * @param headerValue
	 * @param conditionValue
	 * @param importance
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void updateComponentSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName, Boolean bEnabled, Boolean bLocked,
			String headerValue, String conditionValue, String importance)
			throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService().updateComponentSpecification(
					businessServiceName, businessTransactionName,
					transactionName, defectName, bEnabled, bLocked,
					headerValue, conditionValue, importance);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();
		}

	}

	/**
	 * @param transactionId
	 * @param defectName
	 * @param bEnabled
	 * @param bLocked
	 * @param headerValue
	 * @param conditionValue
	 * @param importance
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void updateComponentSpecificationById(long transactionId,
			String defectName, Boolean bEnabled, Boolean bLocked,
			String headerValue, String conditionValue, String importance)
			throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService()
					.updateComponentSpecificationById(transactionId,
							defectName, bEnabled, bLocked, headerValue,
							conditionValue, importance);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not update the Component Specification of type : ");
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void enableComponentSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService().enableComponentSpecification(
					businessServiceName, businessTransactionName,
					transactionName, defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not enable the Component Specification  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out.println("Could not enable the Component Specification ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not enable the Component Specification  ");
			e.printStackTrace();
		}

	}

	/**
	 * @param transactionId
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void enableComponentSpecificationById(long transactionId,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices
					.getInternalService()
					.enableComponentSpecificationById(transactionId, defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not enable the Component Specification  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out.println("Could not enable the Component Specification ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not enable the Component Specification  ");
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void disableComponentSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService().disableComponentSpecification(
					businessServiceName, businessTransactionName,
					transactionName, defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:disableComponentSpecification ");
			System.out
					.println("Could not disable the Component Specification  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:disableComponentSpecification ");
			System.out
					.println("Could not disable the Component Specification ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not disable the Component Specification  ");
			e.printStackTrace();
		}

	}

	/**
	 * @param transactionId
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void disableComponentSpecificationById(long transactionId,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService()
					.disableComponentSpecificationById(transactionId,
							defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not disable the Component Specification by transaction Id  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not enable the Component Specification by transaction Id");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not enable the Component Specification by transaction Id");
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void deleteComponentSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices.getInternalService().deleteComponentSpecification(
					businessServiceName, businessTransactionName,
					transactionName, defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:disableComponentSpecification ");
			System.out
					.println("Could not delete the Component Specification  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:disableComponentSpecification ");
			System.out.println("Could not delete the Component Specification ");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not delete the Component Specification  ");
			e.printStackTrace();
		}

	}

	/**
	 * @param transactionId
	 * @param defectName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public void deleteComponentSpecificationById(long transactionId,
			String defectName) throws CEMWebServicesException, RemoteException {

		try {
			m_cemServices
					.getInternalService()
					.deleteComponentSpecificationById(transactionId, defectName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not delete the Component Specification by transaction Id  ");
			e.printStackTrace();

		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataInServiceSoapBindingStub:deleteComponent ");
			System.out
					.println("Could not delete the Component Specification by transaction Id");
			e.printStackTrace();

		} catch (Exception e) {
			System.out
					.println("Could not delete the Component Specification by transaction Id");
			e.printStackTrace();
		}

	}

	/**
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @return
	 */
	public ComponentDefinition getComponentDefinitionByName(
			String businessServiceName, String businessTransactionName,
			String transactionName, String componentName) {
		try {
			System.out.println("Inside getComponentDefinitionByName ....");
			component = m_cemServices.getConfigurationDataOutService()
					.getComponent(businessServiceName, businessTransactionName,
							transactionName, componentName);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataOutServiceSoapBindingStub:getComponent ");
			System.out.println("Could not retrieve the Component: ");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataOutServiceSoapBindingStub:getComponent ");
			System.out.println("Could not retrieve the Component: ");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Could not retrieve the Component: ");
			e.printStackTrace();
		}
		System.out.println("Component retrieved successfully:"
				+ component.getNumberOfParamDefinitions());

		return component;
	}

	/**
	 * @param componentId
	 * @return
	 */
	public ComponentDefinition getComponentDefinitionById(long componentId) {

		try {

			component = m_cemServices.getConfigurationDataOutService()
					.getComponentById(componentId);

		} catch (CEMWebServicesException e) {
			System.out
					.println("CEM Web Services exception : ConfigurationDataOutServiceSoapBindingStub:getComponentById ");
			System.out.println("Could not retrieve the Component by Id: ");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out
					.println("Remote Exception : ConfigurationDataOutServiceSoapBindingStub:getComponentById ");
			System.out.println("Could not retrieve the Component by Id: ");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Could not retrieve the getComponent By Id: ");
			e.printStackTrace();
		}
		System.out.println("Component retrieved successfully by Id:"
				+ component.getName().toString());
		System.out.println("Component retrieved successfully by Id:"
				+ component.getNumberOfParamDefinitions());

		return component;
	}

}
