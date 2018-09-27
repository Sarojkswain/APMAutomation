package com.ca.apm.tests.cem.common;

import com.ca.apm.tests.cem.common.BaseProperties.AutogenParameterType;
import com.ca.apm.tests.utility.ErrorException;
import com.ca.wily.cem.qa.api.AutogenParamDefinition;
import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.TemplateDefinition;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AdminAutogenHelper {
	protected CEMServices m_cemServices;
	public static final String m_cookie = AutogenParameterType.COOKIE.getAutogenParamType();
	public static final String m_post = AutogenParameterType.POST.getAutogenParamType();
	public static final String m_query = AutogenParameterType.QUERY.getAutogenParamType();
	public static final String m_url = AutogenParameterType.URL.getAutogenParamType();
	public static final String m_http = AutogenParameterType.HTTP_REQUEST_HEADER.getAutogenParamType();
	public static final String m_path = AutogenParameterType.PATH.getAutogenParamType();
	public static final String m_any = AutogenParameterType.ANY.getAutogenParamType();
	public static final String m_plugin = AutogenParameterType.PLUG_IN.getAutogenParamType();
	public static final String m_flex_req_property = AutogenParameterType.FLEX_REQ_PROPERTY.getAutogenParamType();
	public static final String m_flex_http_req_header = AutogenParameterType.FLEX_HTTP_REQ_HEADER.getAutogenParamType();
	public static final String m_http_response =AutogenParameterType.HTTP_RESPONSE.getAutogenParamType();
	public static final String m_http_response_header =AutogenParameterType.HTTP_RESPONSE_HEADER.getAutogenParamType();
	public static final String m_html_response_tag =AutogenParameterType.HTML_RESPONSE_TAG.getAutogenParamType();
	public static final String m_plugin_http_response =AutogenParameterType.PLUG_IN_HTTP_RESP.getAutogenParamType();
	public static final String m_flex_resp_property = AutogenParameterType.FLEX_RESP_PROPERTY.getAutogenParamType();
	
	public static final String m_http_delete = AutogenParameterType.HTTP_DELETE.getAutogenParamType();

	public static final String m_flex_request_property = "FLEX_REQUEST_PROPERTY";
	public static final String m_flex_request_header = "FLEX_HTTP_REQUEST_HEADER";
	public static final String m_flex_response_property = "FLEX_RESPONSE_PROPERTY";

	public static final String m_flex_request_property_messagetype = "messagetype";
	public static final String m_flex_request_property_destination = "destination";
	public static final String m_flex_request_property_source = "source";
	public static final String m_flex_request_property_operation = "operation";
	public static final String m_flex_request_property_contenttype = "contenttype";
	public static final String m_flex_request_property_method = "method";
	public static final String m_flex_request_property_url = "url";
	public static final String m_flex_request_property_remoteusername = "remoteusername";

	public static final String m_flex_response_property_messagetype = "messagetype";
	public static final String m_flex_response_property_destination = "destination";

	public static final String m_url_param_host = "Host";
	public static final String m_url_param_path = "Path";
	public static final String m_url_param_port = "Port";
	public static final String m_http_response_param_status = "Status";

	public static final String m_action_matches = "matches";
	public static final String m_action_not_exist = "not_exist";
	public static final String m_action_expression = "expression";


	/** @param a_cemServices */
	public AdminAutogenHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/** @param templateName
	 *            - creates template of the specified name. Cannot create a
	 *            template
	 * @param description
	 *            - Adds template description specified in this parameter
	 * @param urlPathFilter
	 *            - As per TESS UI, the default value is default "/*"
	 * @param contentTypeFilter
	 *            - As per TESS UI, the default value is "text/html"
	 * @param applicationName
	 *            - This parameter specifies the application the template is
	 *            associated with.
	 * @return - Id of the template created if not returns 0
	 * @throws RemoteException
	 * @throws CEMWebServicesException */
	public long createAutogenTemplate(String templateName, String description,
			String urlPathFilter, String contentTypeFilter,
			String applicationName) throws CEMWebServicesException,
			RemoteException {
		long autogenTemplateId = 0;
		autogenTemplateId = m_cemServices.getInternalService()
				.createAutogenTemplate(templateName, description,
						urlPathFilter, contentTypeFilter, applicationName);
		System.out.println("Autogen Template creation successful : "
				+ templateName);
		return autogenTemplateId;

	}

	/** @param existingTemplateName
	 *            - Existing Name using which the Template is identified. Please
	 *            Note - the Name of the template CANNOT be updated.
	 * @param description
	 *            - The new description
	 * @param urlPathFilter
	 *            - The new urlPathFilter
	 * @param contentTypeFilter
	 *            - The new contentTypeFilter
	 * @param applicationName
	 *            - The new applicationName
	 * @throws RemoteException
	 * @throws CEMWebServicesException */
	public void updateAutogenTemplate(String existingTemplateName,
			String description, String urlPathFilter, String contentTypeFilter,
			String applicationName) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().updateAutogenTemplate(
				existingTemplateName, description, urlPathFilter,
				contentTypeFilter, applicationName);
		System.out.println("Autogen Template updated successfully : "
				+ existingTemplateName);

	}

	/** @param templateName
	 *            - Specifies the name of the template to be deleted.
	 * @throws RemoteException
	 * @throws CEMWebServicesException */
	public void deleteAutogenTemplate(String templateName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteAutogenTemplate(templateName);
		System.out.println("Autogen Template deleted successfully : "
				+ templateName);

	}

	/** @param templateName
	 *            - Specifies the name of the template to be enabled
	 * @throws RemoteException
	 * @throws CEMWebServicesException */
	public void enableAutogenTemplate(String templateName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableAutogenTemplate(templateName);
		System.out.println("Autogen Template enabled successfully : "
				+ templateName);

	}

	/** @param templateName
	 *            - specifies the name of the template to be disabled
	 * @throws RemoteException
	 * @throws CEMWebServicesException */
	public void disableAutogenTemplate(String templateName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().disableAutogenTemplate(templateName);
		System.out.println("Autogen Template disabled successfully : "
				+ templateName);

	}

	/** @param templateName
	 *            -specifies the name of the template in which the parameter
	 *            will be created.
	 * @param paramType
	 *            - specifies the type of parameter and should be one of the
	 *            following- COOKIE, POST, QUERY, URL, HTTPHEADER, PATH, ANY,
	 *            PLUGIN,FLEX_REQUEST_PROPERTY, FLEX_HTTP_REQUEST_HEADER
	 * @param paramName
	 *            - specifies the name of the parameter. For Parameters of
	 *            ParamType:- URL, the paramName should be one of the following
	 *            Host, Path, Port. FLEX_REQUEST_PROPERTY - [messagetype,
	 *            destination, source, operation", contenttype, method, url,
	 *            remoteusername]
	 * @param paramAction
	 *            - specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist, expression
	 * @param paramPattern
	 *            - specifies the matching pattern for the action
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public long createAutogenRequestParam(String templateName,
			String paramType, String paramName, String paramAction,
			String paramPattern, boolean required)
			throws CEMWebServicesException, RemoteException, ErrorException {
		boolean action = false;
		boolean paramURL = false;
		boolean paramFlexRequestProperty = false;
		long param_Id = 0;

		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist) || paramAction
				.equals(m_action_expression))) {
			action = true;
		}

		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path)))

		{
			paramURL = true;

		}
		if ((paramType.equals(m_flex_request_property))
				&& (paramName.equals(m_flex_request_property_messagetype)
						|| paramName
								.equals(m_flex_request_property_destination)
						|| paramName.equals(m_flex_request_property_source)
						|| paramName.equals(m_flex_request_property_operation)
						|| paramName
								.equals(m_flex_request_property_contenttype)
						|| paramName.equals(m_flex_request_property_method)
						|| paramName.equals(m_flex_request_property_url) || paramName
						.equals(m_flex_request_property_remoteusername)))

		{
			paramFlexRequestProperty = true;
		}

		boolean checkParameters = (paramType.equals(m_cookie)
				|| paramType.equals(m_post) || paramType.equals(m_query)
				|| (paramURL) || paramType.equals(m_http)
				|| paramType.equals(m_path) || paramType.equals(m_any)
				|| paramType.equals(m_plugin) || (paramFlexRequestProperty) || paramType
				.equals(m_flex_request_header));

		if ((action && checkParameters) == true) {

			param_Id = m_cemServices.getInternalService().createAutogenParam(
					templateName, paramType, paramName, paramAction,
					paramPattern, required);
			System.out
					.println("Autogen Template Request Parameter created successfully in Template : "
							+ templateName);
		} else {
			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type : " + paramType
							+ " or Param Name: " + paramName + " or Action: "
							+ paramAction);
			throw e;
		}
		return param_Id;
	}

	/** @param templateName
	 *            -specifies the name of the template in which the parameter
	 *            will be created.
	 * @param paramType
	 *            - specifies the type of parameter and should be one of the
	 *            following- HTTP_RESPONSE, HTTP_RESPONSE_HEADER,
	 *            HTML_RESPONSE_TAG, PLUGIN_HTTP_RESPONSE,FLEX_RESPONSE_PROPERTY
	 * @param paramName
	 *            - specifies the name of the parameter. For Parameters of
	 *            ParamType:- HTTP_RESPONSE, the paramName should be Status. For
	 *            FLEX_RESPONSE_PROPERTY - [messagetype, destination]
	 * @param paramAction
	 *            - specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist, expression
	 * @param paramPattern
	 *            - specifies the matching pattern for the action
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public long createAutogenResponseParam(String templateName,
			String paramType, String paramName, String paramAction,
			String paramPattern, boolean required)
			throws CEMWebServicesException, RemoteException, ErrorException {
		boolean action = false;
		boolean paramHTTPResponse = false;
		boolean paramFlexResponse = false;
		long param_Id = 0;
		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist) || paramAction
				.equals(m_action_expression))) {
			action = true;

		}

		if (paramType.equals(m_http_response)
				&& paramName.equals(m_http_response_param_status)) {
			paramHTTPResponse = true;

		}

		if (paramType.equals(m_flex_response_property)
				&& (paramName.equals(m_flex_response_property_destination) || paramName
						.equals(m_flex_response_property_messagetype))) {
			paramFlexResponse = true;

		}

		boolean checkParameters = ((paramHTTPResponse)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag)
				|| paramType.equals(m_plugin_http_response)
				|| (paramHTTPResponse) || (paramFlexResponse));

		if ((action && checkParameters) == true) {

			param_Id = m_cemServices.getInternalService().createAutogenParam(
					templateName, paramType, paramName, paramAction,
					paramPattern, required);

			System.out
					.println("Autogen Template Response Parameter created successfully : "
							+ templateName);
		} else {
			ErrorException e = new ErrorException(
					"This is not a valid response parameter Type : "
							+ paramType + " or Param Name: " + paramName
							+ " or Action: " + paramAction);
			throw e;
		}
		return param_Id;
	}

	/** @param templateId
	 *            -specifies the id of the template in which the parameter will
	 *            be created. Please refer to ts_autogen_template.ts_id field to
	 *            obtain the value from database
	 * @param paramType
	 *            - specifies the type of the parameter. It should be of the
	 *            following format HTTP_RESPONSE, HTTP_RESPONSE_HEADER,
	 *            HTML_RESPONSE_TAG, PLUGIN_HTTP_RESPONSE,FLEX_REQUEST_PROPERTY,
	 *            FLEX_HTTP_REQUEST_HEADER.
	 * @param paramName
	 *            - specifies the name of the parameter. For Parameters of
	 *            ParamType:- URL, the paramName should be one of the following
	 *            Host, Path, Port. FLEX_REQUEST_PROPERTY - [messagetype,
	 *            destination, source, operation", contenttype, method, url,
	 *            remoteusername]
	 * @param paramAction
	 *            :- specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist, expression
	 * @param paramPattern
	 *            - specifies the matching pattern for the action
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public long createAutogenRequestParamById(long templateId,
			String paramType, String paramName, String paramAction,
			String paramPattern, boolean required)
			throws CEMWebServicesException, RemoteException, ErrorException {
		boolean action = false;
		boolean paramURL = false;
		boolean paramFlexRequestProperty = false;
		long param_Id = 0;

		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist) || paramAction
				.equals(m_action_expression))) {
			action = true;
			System.out.print(action);
		}

		if ((paramType.equals(m_flex_request_property))
				&& (paramName.equals(m_flex_request_property_messagetype)
						|| paramName
								.equals(m_flex_request_property_destination)
						|| paramName.equals(m_flex_request_property_source)
						|| paramName.equals(m_flex_request_property_operation)
						|| paramName
								.equals(m_flex_request_property_contenttype)
						|| paramName.equals(m_flex_request_property_method)
						|| paramName.equals(m_flex_request_property_url) || paramName
						.equals(m_flex_request_property_remoteusername)))

		{
			paramFlexRequestProperty = true;
		}

		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path)))

		{
			paramURL = true;
		}

		boolean checkParameters = (paramType.equals(m_cookie)
				|| paramType.equals(m_post) || paramType.equals(m_query)
				|| (paramURL) || paramType.equals(m_http)
				|| paramType.equals(m_path) || paramType.equals(m_any)
				|| paramType.equals(m_plugin) || (paramFlexRequestProperty) || paramType
				.equals(m_flex_request_header));

		if ((action && checkParameters) == true) {
			param_Id = m_cemServices.getInternalService()
					.createAutogenParamById(templateId, paramType, paramName,
							paramAction, paramPattern, required);
			System.out
					.println("Autogen Template Request Parameter created successfully using Template ID : "
							+ templateId);

		} else {

			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type : " + paramType
							+ " or Param Name: " + paramName + " or Action: "
							+ paramAction);
			throw e;
		}
		return param_Id;
	}

	/** @param templateId
	 *            - Id of the template
	 * @param paramType
	 *            - specifies the type of parameter and should be one of the
	 *            following- HTTP_RESPONSE, HTTP_RESPONSE_HEADER,
	 *            HTML_RESPONSE_TAG, PLUGIN_HTTP_RESPONSE,FLEX_RESPONSE_PROPERTY
	 * @param paramName
	 *            - specifies the name of the parameter. For Parameters of
	 *            ParamType:- HTTP_RESPONSE, the paramName should be Status. For
	 *            FLEX_RESPONSE_PROPERTY - [messagetype, destination]
	 * @param paramAction
	 *            - specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist, expression
	 * @param paramPattern
	 *            - specifies the matching pattern for the action
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public long createAutogenResponseParamById(long templateId,
			String paramType, String paramName, String paramAction,
			String paramPattern, boolean required)
			throws CEMWebServicesException, RemoteException, ErrorException {
		boolean action = false;
		boolean paramHTTPResponse = false;
		boolean paramFlexResponse = false;
		long param_Id = 0;
		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist) || paramAction
				.equals(m_action_expression))) {
			action = true;
			System.out.print(action);
		}

		if (paramType.equals(m_http_response)
				&& paramName.equals(m_http_response_param_status)) {
			paramHTTPResponse = true;

		}
		if (paramType.equals(m_flex_response_property)
				&& (paramName.equals(m_flex_response_property_destination) || paramName
						.equals(m_flex_response_property_messagetype))) {
			paramFlexResponse = true;

		}
		boolean checkParameters = ((paramHTTPResponse)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag)
				|| paramType.equals(m_plugin_http_response) || (paramFlexResponse));

		if ((action && checkParameters) == true) {

			param_Id = m_cemServices.getInternalService()
					.createAutogenParamById(templateId, paramType, paramName,
							paramAction, paramPattern, required);
			System.out
					.println("Autogen Template Response Parameter created successfully : "
							+ templateId);
		} else {
			ErrorException e = new ErrorException(
					"This is not a valid response parameter Type : "
							+ paramType + " or Param Name: " + paramName
							+ " or Action: " + paramAction);
			throw e;
		}
		return param_Id;
	}

	/** @param templateName
	 *            - specifies the name of the template associated with the
	 *            parameter. This cannot be updated.
	 * @param paramType
	 *            -specifies the type of parameter and should be one of the
	 *            following- COOKIE, POST, QUERY, URL, HTTP, PATH, ANY, PLUGIN,
	 *            HTTP_RESPONSE, HTTP_RESPONSE_HEADER, HTML_RESPONSE_TAG,
	 *            PLUGIN_HTTP_RESPONSE ,FLEX_REQUEST_PROPERTY,
	 *            FLEX_HTTP_REQUEST_HEADER,FLEX_RESPONSE_PROPERTY. This cannot
	 *            be updated. Please note HTTPHEADER is saved as HTTP
	 * 
	 * @param paramName
	 *            - specifies the name of the parameter. This cannot be updated.
	 * @param paramAction
	 *            - specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist,
	 *            expression. This can be updated.
	 * @param paramPattern
	 *            - specifies the matching pattern for the action. This can be
	 *            updated.
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false. This can be
	 *            updated.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public void updateAutogenParam(String templateName, String paramType,
			String paramName, String paramAction, String paramPattern,
			boolean required) throws CEMWebServicesException, RemoteException,
			ErrorException {
		boolean action = false;
		boolean paramURL = false, paramHTTPResponse = false;
		boolean paramFlexResponse = false;
		boolean paramFlexRequestProperty = false;

		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist)
				|| paramAction.equals(m_action_expression) || paramAction
				.equals(null)) == true) {
			action = true;

		}

		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path)))

		{
			paramURL = true;

		}

		if (paramType.equals(m_http_response)
				&& (paramName.equals(m_http_response_param_status))) {
			paramHTTPResponse = true;
		}

		if (paramType.equals(m_flex_response_property)
				&& (paramName.equals(m_flex_response_property_destination) || paramName
						.equals(m_flex_response_property_messagetype))) {
			paramFlexResponse = true;

		}

		if ((paramType.equals(m_flex_request_property))
				&& (paramName.equals(m_flex_request_property_messagetype)
						|| paramName
								.equals(m_flex_request_property_destination)
						|| paramName.equals(m_flex_request_property_source)
						|| paramName.equals(m_flex_request_property_operation)
						|| paramName
								.equals(m_flex_request_property_contenttype)
						|| paramName.equals(m_flex_request_property_method)
						|| paramName.equals(m_flex_request_property_url) || paramName
						.equals(m_flex_request_property_remoteusername)))

		{
			paramFlexRequestProperty = true;
		}

		boolean checkParameters = (paramType.equals(m_cookie)
				|| paramType.equals(m_post) || paramType.equals(m_query)
				|| (paramURL) || paramType.equals(m_http)
				|| paramType.equals(m_path) || paramType.equals(m_any)
				|| paramType.equals(m_plugin) || (paramHTTPResponse)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag)
				|| paramType.equals(m_plugin_http_response)
				|| paramType.equals(m_flex_request_header)
				|| (paramFlexRequestProperty) || (paramFlexResponse));

		if ((action && checkParameters) == true) {
			m_cemServices.getInternalService().updateAutogenParam(templateName,
					paramType, paramName, paramAction, paramPattern, required);
			System.out
					.println("Autogen Template Parameter updated successfully : "
							+ templateName);
		} else {
			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type : " + paramType
							+ " or Param Name: " + paramName + " or Action: "
							+ paramAction);
			throw e;
		}

	}

	/** @param templateId
	 *            -specifies the id of the template associated with the
	 *            parameter. Please refer to ts_autogen_template.ts_id field to
	 *            obtain the value from database. This cannot be updated.
	 * @param paramType
	 *            -specifies the type of parameter and should be one of the
	 *            following- COOKIE, POST, QUERY, URL, HTTP, PATH, ANY, PLUGIN,
	 *            HTTP_RESPONSE, HTTP_RESPONSE_HEADER, HTML_RESPONSE_TAG,
	 *            PLUGIN_HTTP_RESPONSE,FLEX_REQUEST_PROPERTY,
	 *            FLEX_HTTP_REQUEST_HEADER,FLEX_RESPONSE_PROPERTY. This cannot
	 *            be updated. Please note HTTPHEADER is saved as HTTP
	 * @param paramName
	 *            - specifies the name of the parameter. This cannot be updated.
	 * @param paramAction
	 *            - specifies the action associated with the parameter and
	 *            should be one of the following- matches, not_exist,
	 *            expression. This can be updated.
	 * @param paramPattern
	 *            - specifies the matching pattern for the action. This can be
	 *            updated.
	 * @param required
	 *            - specifies whether it is a mandatory parameter or not and
	 *            should be one of the following - true, false. This can be
	 *            updated.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public void updateAutogenParamById(long templateId, String paramType,
			String paramName, String paramAction, String paramPattern,
			boolean required) throws CEMWebServicesException, RemoteException,
			ErrorException {
		boolean action = false;
		boolean paramURL = false, paramHTTPResponse = false;
		boolean paramFlexResponse = false;
		boolean paramFlexRequestProperty = false;
		if ((paramAction.equals(m_action_matches)
				|| paramAction.equals(m_action_not_exist)
				|| paramAction.equals(m_action_expression) || paramAction
				.equals(null))) {
			action = true;
			System.out.print(action);
		}

		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path)))

		{
			paramURL = true;
			System.out.print(paramURL);
		}

		if (paramType.equals(m_http_response)
				&& paramName.equals(m_http_response_param_status)) {
			paramHTTPResponse = true;

		}

		if (paramType.equals(m_flex_response_property)
				&& (paramName.equals(m_flex_response_property_destination) || paramName
						.equals(m_flex_response_property_messagetype))) {
			paramFlexResponse = true;

		}

		if ((paramType.equals(m_flex_request_property))
				&& (paramName.equals(m_flex_request_property_messagetype)
						|| paramName
								.equals(m_flex_request_property_destination)
						|| paramName.equals(m_flex_request_property_source)
						|| paramName.equals(m_flex_request_property_operation)
						|| paramName
								.equals(m_flex_request_property_contenttype)
						|| paramName.equals(m_flex_request_property_method)
						|| paramName.equals(m_flex_request_property_url) || paramName
						.equals(m_flex_request_property_remoteusername)))

		{
			paramFlexRequestProperty = true;
		}

		boolean checkParameters = (paramType.equals(m_cookie)
				|| paramType.equals(m_post) || paramType.equals(m_query)
				|| paramURL || paramType.equals(m_http)
				|| paramType.equals(m_path) || paramType.equals(m_any)
				|| paramType.equals(m_plugin) || paramHTTPResponse
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag)
				|| paramType.equals(m_plugin_http_response)
				|| paramType.equals(m_flex_request_header)
				|| (paramFlexResponse) || (paramFlexRequestProperty));

		if ((action && checkParameters) == true)

		{
			m_cemServices.getInternalService().updateAutogenParamById(
					templateId, paramType, paramName, paramAction,
					paramPattern, required);
			System.out
					.println("Autogen Template Parameter updated successfully using Template ID: "
							+ templateId);

		}

		else {
			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type : " + paramType
							+ " or Param Name: " + paramName + " or Action: "
							+ paramAction);
			throw e;
		}
	}

	/** @param templateName
	 *            -specifies the name of the template associated with the
	 *            parameter.
	 * @param paramType
	 *            - specifies the type of parameter to be deleted and should be
	 *            one of the following- COOKIE, POST, QUERY, URL, HTTP, PATH,
	 *            ANY, PLUGIN, HTTP_RESPONSE, HTTP_RESPONSE_HEADER,
	 *            HTML_RESPONSE_TAG, PLUGIN_HTTP_RESPONSE. Please note that
	 *            HTTPHEADER is saved as HTTP
	 * @param paramName
	 *            - Specifies the name of the parameter to be deleted.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public void deleteAutogenParam(String templateName, String paramType,
			String paramName) throws CEMWebServicesException, RemoteException,
			ErrorException {
		boolean action = false;
		boolean paramURL = false, paramHTTPResponse = false;
		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path))) {
			paramURL = true;
			System.out.print(paramURL);
		}

		if (paramType.equals(m_http_response)
				&& paramName.equals(m_http_response_param_status)) {
			paramHTTPResponse = true;

		}
		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || (paramURL)
				|| paramType.equals(m_http_delete) || paramType.equals(m_path)
				|| paramType.equals(m_any) || paramType.equals(m_plugin)
				|| (paramHTTPResponse)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response)|| paramType.equals(m_flex_request_property)) == true)

		{
			m_cemServices.getInternalService().deleteAutogenParam(templateName,
					paramType, paramName);
			System.out
					.println("Autogen Template Parameter deleted successfully : "
							+ templateName);

		}

		else {
			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type: " + paramType
							+ " or paramName: " + paramName);
			throw e;
		}

	}

	/** @param templateId
	 *            -specifies the id of the template associated with the
	 *            parameter. Please refer to ts_autogen_template.ts_id field to
	 *            obtain the value from database.
	 * @param paramType
	 *            -specifies the type of parameter to be deleted and should be
	 *            one of the following- COOKIE, POST, QUERY, URL, HTTP, PATH,
	 *            ANY, PLUGIN, HTTP_RESPONSE, HTTP_RESPONSE_HEADER,
	 *            HTML_RESPONSE_TAG, PLUGIN_HTTP_RESPONSE. Please note that
	 *            HTTPHEADER is saved as HTTP
	 * @param paramName
	 *            - Specifies the name of the parameter to be deleted.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @throws ErrorException */
	public void deleteAutogenParamById(long templateId, String paramType,
			String paramName) throws CEMWebServicesException, RemoteException,
			ErrorException {
		boolean action = false;
		boolean paramURL = false, paramHTTPResponse = false;
		if ((paramType.equals(m_url))
				&& (paramName.equals(m_url_param_host)
						|| paramName.equals(m_url_param_port) || paramName
						.equals(m_url_param_path)))

		{
			paramURL = true;
			System.out.print(paramURL);
		}

		if (paramType.equals(m_http_response)
				&& paramName.equals(m_http_response_param_status)) {
			paramHTTPResponse = true;

		}
		if ((paramType.equals(m_cookie) || paramType.equals(m_post)
				|| paramType.equals(m_query) || (paramURL)
				|| paramType.equals(m_http_delete) || paramType.equals(m_path)
				|| paramType.equals(m_any) || paramType.equals(m_plugin)
				|| (paramHTTPResponse)
				|| paramType.equals(m_http_response_header)
				|| paramType.equals(m_html_response_tag) || paramType
				.equals(m_plugin_http_response)) == true) {
			m_cemServices.getInternalService().deleteAutogenParamById(
					templateId, paramType, paramName);
			System.out
					.println("Autogen Template Parameter deleted successfully using Template Id : "
							+ templateId);
		} else {
			ErrorException e = new ErrorException(
					"This is not a valid request parameter Type: " + paramType
							+ " or parameter Name " + paramName);
			throw e;
		}
	}
	
	public AutogenParamDefinition[] getAutogenParams(String templateName)throws CEMWebServicesException, RemoteException,
	ErrorException{
		return m_cemServices.getConfigurationDataOutService().getAutogenParams(templateName);
	}
	
	public TemplateDefinition[] getAllTransactionTemplates() throws CEMWebServicesException, RemoteException{
		return m_cemServices.getConfigurationDataOutService().getAllTransactionTemplates();
	}
	
	public List<TemplateDefinition> getAllEnabledAutogenTemplates() throws RemoteException,IOException{
		TemplateDefinition[] templates = m_cemServices.getConfigurationDataOutService().getAllTransactionTemplates();
		List<TemplateDefinition> enabledTemplates = new ArrayList<TemplateDefinition>() ;
		for (TemplateDefinition templateDefinition : templates) {
			if(templateDefinition.isEnabled())
				enabledTemplates.add(templateDefinition);
			
		}
		return enabledTemplates;
	}

}
