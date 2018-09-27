package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.ApplicationDefinition;
import com.ca.wily.cem.qa.api.CEMWebServicesException;

import java.rmi.RemoteException;

public class AuthenticationHelper {
	protected CEMServices m_cemServices;
	protected ApplicationDefinition[] applicationDefinition;
	protected ApplicationDefinition businessApplication;
	protected ApplicationDefinition businessApplicationById;

	/**
	 * Class constructor.
	 * 
	 * @param a_cemServices
	 *            handle for all available webservices.
	 */
	public AuthenticationHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}
	
	/**
	 * logs in the user with specified username, password and default port 8081
	 * 
	 * @param username
	 * @param password
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void loginWithUsernamePassword(String username, String password) throws CEMWebServicesException,
			RemoteException {
		m_cemServices.getOperatorDataOutInService().loginByUsernameAndPassword(username, password);
	}
	
	/**
	 * 
	 * logs in the user with specified username, password and port
	 * 
	 * @param username
	 * @param password
	 * @param port
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void loginWithUsernamePasswordPort(String username, String password, String port) throws CEMWebServicesException,
	RemoteException {
		m_cemServices.getOperatorDataOutInService().loginByUsernameAndPassword(username, password, port);
	}
	
	
	
	
	
	
}
