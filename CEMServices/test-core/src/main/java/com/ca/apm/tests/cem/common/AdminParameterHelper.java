package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.ParamDefinition;
import com.ca.wily.cem.qa.common.util.ErrorException;

import java.rmi.RemoteException;

public class AdminParameterHelper {

	protected CEMServices m_cemServices;

	public static final String m_cookie = "COOKIE";
	public static final String m_post = "POST";
	public static final String m_query = "QUERY";
	public static final String m_url = "URL";
	public static final String m_http = "HTTP";
	public static final String m_xml = "XML";
	public static final String m_netegrity_siteminder = "NETEGRITY_SITEMINDER";
	public static final String m_ntlm_auth = "NTLM_AUTH";
	public static final String m_basic_auth = "BASIC_AUTH";
	public static final String m_x_wtg_info = "X_WTG_INFO";
	public static final String m_plugin = "PLUGIN";
	public static final String m_http_response = "HTTP_RESPONSE";
	public static final String m_http_response_header = "HTTP_RESPONSE_HEADER";
	public static final String m_html_response_tag = "HTML_RESPONSE_TAG";
	public static final String m_plugin_http_response = "PLUGIN_HTTP_RESPONSE";
	public static final String m_flex_response="FLEX_RESPONSE_PROPERTY";
	public static final String m_flex_request="FLEX_REQUEST_PROPERTY";

	public AdminParameterHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public long createRequestParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName,
			String paramNameType, String paramAction, String paramPattern)
			throws CEMWebServicesException, RemoteException, ErrorException {
		long ParamId;
		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {

			ParamId = m_cemServices.getInternalService().createParam(
					businessServiceName, businessTransactionName,
					transactionName, componentName, paramType, paramName,
					paramNameType, paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}
		return ParamId;
	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */

	public long createRequestParameterById(long componentId, String paramType,
			String paramName, String paramNameType, String paramAction,
			String paramPattern) throws CEMWebServicesException,
			RemoteException, ErrorException {
		long ParamId = 0;
		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {

			ParamId = m_cemServices.getInternalService().createParamById(
					componentId, paramType, paramName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}
		return ParamId;
	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */

	public long createResponseParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName,
			String paramNameType, String paramAction, String paramPattern)
			throws CEMWebServicesException, RemoteException, ErrorException {
		long ParamId = 0;
		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || paramType.equals(m_flex_response))) {
			ParamId = m_cemServices.getInternalService().createParam(
					businessServiceName, businessTransactionName,
					transactionName, componentName, paramType, paramName,
					paramNameType, paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a response parameter Type : " + paramType);
			throw e;
		}
		return ParamId;
	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */

	public long createResponseParameterById(long componentId, String paramType,
			String paramName, String paramNameType, String paramAction,
			String paramPattern) throws CEMWebServicesException,
			RemoteException, ErrorException {
		long ParamId = 0;
		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || paramType.equals(m_flex_response))) {

			ParamId = m_cemServices.getInternalService().createParamById(
					componentId, paramType, paramName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a response parameter Type : " + paramType);
			throw e;
		}
		return ParamId;

	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @param newParamName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void updateRequestParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName,
			String newParamName, String paramNameType, String paramAction,
			String paramPattern) throws CEMWebServicesException,
			RemoteException, ErrorException {

		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {

			m_cemServices.getInternalService().updateParam(businessServiceName,
					businessTransactionName, transactionName, componentName,
					paramType, paramName, newParamName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @param newParamName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void updateRequestParameterById(Long componentId, String paramType,
			String paramName, String newParamName, String paramNameType,
			String paramAction, String paramPattern)
			throws CEMWebServicesException, RemoteException, ErrorException {

		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {

			m_cemServices.getInternalService().updateParamById(componentId,
					paramType, paramName, newParamName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @param newParamName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void updateResponseParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName,
			String newParamName, String paramNameType, String paramAction,
			String paramPattern) throws CEMWebServicesException,
			RemoteException, ErrorException {

		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || m_flex_response.equals(m_flex_response))) {

			m_cemServices.getInternalService().updateParam(businessServiceName,
					businessTransactionName, transactionName, componentName,
					paramType, paramName, newParamName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @param newParamName
	 * @param paramNameType
	 * @param paramAction
	 * @param paramPattern
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void updateResponseParameterById(Long componentId, String paramType,
			String paramName, String newParamName, String paramNameType,
			String paramAction, String paramPattern)
			throws CEMWebServicesException, RemoteException, ErrorException {

		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || paramType.equals(m_flex_response))) {

			m_cemServices.getInternalService().updateParamById(componentId,
					paramType, paramName, newParamName, paramNameType,
					paramAction, paramPattern);
		} else {
			ErrorException e = new ErrorException(
					"This is not a response parameter Type : " + paramType);
			throw e;
		}
	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void deleteRequestParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName)
			throws CEMWebServicesException, RemoteException, ErrorException {

		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {
			m_cemServices.getInternalService().deleteParam(businessServiceName,
					businessTransactionName, transactionName, componentName,
					paramType, paramName);

		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void deleteRequestParameterById(Long componentId, String paramType,
			String paramName) throws CEMWebServicesException, RemoteException,
			ErrorException {

		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || paramType.equals(m_url)
				|| paramType.equals(m_http) || paramType.equals(m_xml)
				|| paramType.equals(m_netegrity_siteminder)
				|| paramType.equals(m_ntlm_auth)
				|| paramType.equals(m_basic_auth)
				|| paramType.equals(m_x_wtg_info) || paramType.equals(m_plugin) || paramType.equals(m_flex_request))) {
			m_cemServices.getInternalService().deleteParamById(componentId,
					paramType, paramName);

		} else {
			ErrorException e = new ErrorException(
					"This is not a request parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param transactionName
	 * @param componentName
	 * @param paramType
	 * @param paramName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void deleteResponseParameter(String businessServiceName,
			String businessTransactionName, String transactionName,
			String componentName, String paramType, String paramName)
			throws CEMWebServicesException, RemoteException, ErrorException {

		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || paramType.equals(m_flex_response))) {

			m_cemServices.getInternalService().deleteParam(businessServiceName,
					businessTransactionName, transactionName, componentName,
					paramType, paramName);
		} else {
			ErrorException e = new ErrorException(
					"This is not a response parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param componentId
	 * @param paramType
	 * @param paramName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * @throws ErrorException
	 */
	public void deleteResponseParameterById(Long componentId, String paramType,
			String paramName) throws CEMWebServicesException, RemoteException,
			ErrorException {

		if ((paramType.equals(m_http_response)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response) || paramType.equals(m_flex_response))) {

			m_cemServices.getInternalService().deleteParamById(componentId,
					paramType, paramName);
		} else {
			ErrorException e = new ErrorException(
					"This is not a response parameter Type : " + paramType);
			throw e;
		}

	}

	/**
	 * 
	 * @param businessServiceName - business service name
	 * @param businessTransactionName - business transaction name
	 * @param transactionName - transaction name
	 * @param componentName - component name
	 * @return - returns the list of all the parameters in a component
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public ParamDefinition[] getAllParamsForComponent(
			String businessServiceName, String businessTransactionName,
			String transactionName, String componentName)
			throws CEMWebServicesException, RemoteException {

		return m_cemServices
				.getConfigurationDataOutService()
				.getAllParamsForComponent(businessServiceName,
						businessTransactionName, transactionName, componentName);
	}

}
