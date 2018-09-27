package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.*;
import org.apache.axis.AxisFault;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CEMServices {

	private static final String ACEGI_SECURITY_CHECK_ENDPOINT_PATH = "wily/cem/tess/app/j_acegi_security_check";
	private static final String LOGIN_ERROR = "login_error";

	private static final String CONFIGURATION_DATA_IN_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/ConfigurationDataInService";
	// private static final String EVENTS_DATA_IN_SERVICE_ENDPOINT_PATH =
	// "wily/cem/webservices/EventsDataInService";
	private static final String CONFIGURATION_DATA_OUT_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/ConfigurationDataOutService";
	private static final String ADMIN_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/AdminService";
	private static final String OPERATOR_DATA_OUT_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/OperatorDataOutService";
	private static final String EVENTS_DATA_OUT_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/EventsDataOutService";
	private static final String BIZ_IMPACT_DATA_OUT_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/BizImpactDataOutService";
	private static final String STATISTICS_DATA_OUT_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/StatisticsDataOutService";
	private static final String VERSION_SERVICE_ENDPOINT_PATH = "wily/cem/webservices/Version";
	private static final String INTERNAL_SERVICES_ENDPOINT_PATH = "wily/cem/webservices/InternalServices";

	protected String m_host;
	protected String m_port;

	protected String m_user;
	protected String m_password;

	public String m_jsessionID;

	

	protected IConfigurationDataInService m_configurationDataInService;
	// protected IEventsDataInService m_eventsDataInService;
	protected IConfigurationDataOutService m_configurationDataOutService;
	protected Admin_PortType m_adminService;
	protected IOperatorDataOutService m_operatorDataOutService;
	protected IEventsDataOutService m_eventsDataOutService;
	protected IBizImpactDataOutService m_bizImpactDataOutService;
	protected IStatisticsDataOutService m_statisticsDataOutService;
	protected Version_PortType m_versionService;
	protected IInternalServices m_internalServices;

	public CEMServices(String a_host, String a_port, String a_user,
			String a_password) {
		System.out.println("Start of CEM Services");
		m_host = a_host;
		m_port = a_port;
		m_user = a_user;
		m_password = a_password;
		m_jsessionID = null;
		System.out.println("End of CEM Services");
	}

	public String getM_host() {
		return m_host;
	}

	public void setM_host(String m_host) {
		this.m_host = m_host;
	}

	public String getM_port() {
		return m_port;
	}

	public void setM_port(String m_port) {
		this.m_port = m_port;
	}

	public String getM_jsessionID() {
		return m_jsessionID;
	}

	public void setM_jsessionID(String m_jsessionID) {
		this.m_jsessionID = m_jsessionID;
	}

	protected String generateEndPointURL(String a_endPointPath) {
		return "http://" + m_host + ":" + m_port + "/" + a_endPointPath;
	}

	protected boolean initializeAllServices() {
		boolean allServicesInitializedSuccessfully = false;
		if (/*
			 * initializeEventsDataInService() &&
			 */initializeConfigurationDataInService()
				&& initializeConfigurationDataOutService()
				&& initializeOperatorDataOutService()
				&& initializeEventsDataOutService()
				&& initializeBizImpactDataOutService()
				&& initializeStatisticsDataOutService()
				&& initializeAdminService() && initializeVersionService()
				&& initializeInternalServices())
			allServicesInitializedSuccessfully = true;

		return allServicesInitializedSuccessfully;

	}

	protected boolean initializeConfigurationDataInService() {
		ConfigurationDataIntServiceLocator coWS = new ConfigurationDataIntServiceLocator();
		coWS.setMaintainSession(true);
		String ciEndpointURL = generateEndPointURL(CONFIGURATION_DATA_IN_SERVICE_ENDPOINT_PATH);
		URL ciURL;
		try {
			ciURL = new URL(ciEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		ConfigurationDataInServiceSoapBindingStub cdiService;
		try {
			cdiService = new ConfigurationDataInServiceSoapBindingStub(ciURL,
					coWS);
		} catch (AxisFault e) {
			return false;
		}
		cdiService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		cdiService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_configurationDataInService = cdiService;
		return true;
	}

	/*
	 * protected boolean initializeEventsDataInService() {
	 * EventsDataInService_ServiceLocator ediWS = new
	 * EventsDataInService_ServiceLocator(); ediWS.setMaintainSession(true);
	 * String ediEndpointURL =
	 * generateEndPointURL(EVENTS_DATA_IN_SERVICE_ENDPOINT_PATH); URL ediURL;
	 * try { ediURL = new URL(ediEndpointURL); } catch (MalformedURLException e)
	 * { return false; } EventsDataInServiceSoapBindingStub ediService; try {
	 * ediService = new EventsDataInServiceSoapBindingStub(ediURL, ediWS); }
	 * catch (AxisFault e) { return false; }
	 * ediService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
	 * Boolean.TRUE);
	 * ediService._setProperty(HTTPConstants.HEADER_COOKIE,"JSESSIONID=" +
	 * m_jsessionID); m_eventsDataInService = ediService; return true; }
	 */

	protected boolean initializeConfigurationDataOutService() {
		ConfigurationDataOutService_ServiceLocator cdoWS = new ConfigurationDataOutService_ServiceLocator();
		cdoWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(CONFIGURATION_DATA_OUT_SERVICE_ENDPOINT_PATH);
		URL cdoURL;
		try {
			cdoURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		ConfigurationDataOutServiceSoapBindingStub cdoService;
		try {
			cdoService = new ConfigurationDataOutServiceSoapBindingStub(cdoURL,
					cdoWS);
		} catch (AxisFault e) {
			return false;
		}
		cdoService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		cdoService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_configurationDataOutService = cdoService;
		return true;
	}

	protected boolean initializeAdminService() {
		AdminServiceLocator adminWS = new AdminServiceLocator();
		adminWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(ADMIN_SERVICE_ENDPOINT_PATH);
		URL adminURL;
		try {
			adminURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		AdminSoapBindingStub adminService;
		try {
			adminService = new AdminSoapBindingStub(adminURL, adminWS);
		} catch (AxisFault e) {
			return false;
		}
		adminService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		adminService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_adminService = adminService;
		return true;
	}

	protected boolean initializeOperatorDataOutService() {
		OperatorDataOutService_ServiceLocator odoWS = new OperatorDataOutService_ServiceLocator();
		odoWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(OPERATOR_DATA_OUT_SERVICE_ENDPOINT_PATH);
		URL odoURL;
		try {
			odoURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		OperatorDataOutServiceSoapBindingStub odoService;
		try {
			odoService = new OperatorDataOutServiceSoapBindingStub(odoURL,
					odoWS);
		} catch (AxisFault e) {
			return false;
		}
		odoService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		odoService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_operatorDataOutService = odoService;
		return true;
	}

	protected boolean initializeEventsDataOutService() {
		EventsDataOutService_ServiceLocator edoWS = new EventsDataOutService_ServiceLocator();
		edoWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(EVENTS_DATA_OUT_SERVICE_ENDPOINT_PATH);
		URL edoURL;
		try {
			edoURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		EventsDataOutServiceSoapBindingStub edoService;
		try {
			edoService = new EventsDataOutServiceSoapBindingStub(edoURL, edoWS);
		} catch (AxisFault e) {
			return false;
		}
		edoService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		edoService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_eventsDataOutService = edoService;
		return true;
	}

	protected boolean initializeBizImpactDataOutService() {
		BizImpactDataOutService_ServiceLocator bidoWS = new BizImpactDataOutService_ServiceLocator();
		bidoWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(BIZ_IMPACT_DATA_OUT_SERVICE_ENDPOINT_PATH);
		URL bidoURL;
		try {
			bidoURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		BizImpactDataOutServiceSoapBindingStub bidoService;
		try {
			bidoService = new BizImpactDataOutServiceSoapBindingStub(bidoURL,
					bidoWS);
		} catch (AxisFault e) {
			return false;
		}
		bidoService._setProperty(javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY,
				Boolean.TRUE);
		bidoService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_bizImpactDataOutService = bidoService;
		return true;
	}

	protected boolean initializeStatisticsDataOutService() {
		StatisticsDataOutService_ServiceLocator statsdoWS = new StatisticsDataOutService_ServiceLocator();
		statsdoWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(STATISTICS_DATA_OUT_SERVICE_ENDPOINT_PATH);
		URL statsdoURL;
		try {
			statsdoURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		StatisticsDataOutServiceSoapBindingStub statsdoService;
		try {
			statsdoService = new StatisticsDataOutServiceSoapBindingStub(
					statsdoURL, statsdoWS);
		} catch (AxisFault e) {
			return false;
		}
		statsdoService._setProperty(
				javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
		statsdoService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_statisticsDataOutService = statsdoService;
		return true;
	}

	protected boolean initializeVersionService() {
		VersionServiceLocator versionWS = new VersionServiceLocator();
		versionWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(VERSION_SERVICE_ENDPOINT_PATH);
		URL versionURL;
		try {
			versionURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		VersionSoapBindingStub versionService;
		try {
			versionService = new VersionSoapBindingStub(versionURL, versionWS);
		} catch (AxisFault e) {
			return false;
		}
		versionService._setProperty(
				javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
		versionService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_versionService = versionService;
		return true;
	}

	protected boolean initializeInternalServices() {
		InternalServices_ServiceLocator internalServicesWS = new InternalServices_ServiceLocator();
		internalServicesWS.setMaintainSession(true);
		String ediEndpointURL = generateEndPointURL(INTERNAL_SERVICES_ENDPOINT_PATH);
		URL internalServicesURL;
		try {
			internalServicesURL = new URL(ediEndpointURL);
		} catch (MalformedURLException e) {
			return false;
		}
		InternalServicesSoapBindingStub internalService;
		try {
			internalService = new InternalServicesSoapBindingStub(
					internalServicesURL, internalServicesWS);
		} catch (AxisFault e) {
			return false;
		}
		internalService._setProperty(
				javax.xml.rpc.Stub.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
		internalService._setProperty(HTTPConstants.HEADER_COOKIE, "JSESSIONID="
				+ m_jsessionID);
		m_internalServices = internalService;
		return true;
	}

	public boolean login() {
		HttpClient client = new HttpClient();

		PostMethod post = new PostMethod(
				generateEndPointURL(ACEGI_SECURITY_CHECK_ENDPOINT_PATH));
		post.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

		NameValuePair userdata = new NameValuePair("j_username", m_user);
		NameValuePair pwddata = new NameValuePair("j_password", m_password);
		post.addParameter(userdata);
		post.addParameter(pwddata);

		// execute method and handle any erro responses.
		int status;
		try {
			status = client.executeMethod(post);
		} catch (HttpException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		if (status == 302 && post.getResponseHeader("location") != null) {
			String redirectLocation = post.getResponseHeader("location")
					.getValue();
			if (redirectLocation.indexOf(LOGIN_ERROR) > -1) {
				return false;
				// throw new
				// RuntimeException("Invalid credentials. Please check the username and password.");
			}
			m_jsessionID = redirectLocation.substring(
					redirectLocation.indexOf('=') + 1,
					redirectLocation.length());

		} else {
			return false;
			// throw new
			// RuntimeException("Could not login into the system - unexpected response from server - "
			// +
			// "http response status: " + status);
		}
		System.out
				.println("Successfully logged into the EM server with jessionID = "
						+ m_jsessionID);
		return initializeAllServices();
	}

	public IConfigurationDataInService getConfigurationDataInService() {
		return m_configurationDataInService;
	}

	/*
	 * public IEventsDataInService getEventDataInService() { return
	 * m_eventsDataInService; }
	 */

	public IConfigurationDataOutService getConfigurationDataOutService() {
		return m_configurationDataOutService;
	}

	public Admin_PortType getAdminService() {
		return m_adminService;
	}

	public IOperatorDataOutService getOperatorDataOutInService() {
		return m_operatorDataOutService;
	}

	public IEventsDataOutService getEventsDataOutService() {
		return m_eventsDataOutService;
	}

	public IBizImpactDataOutService getBizImpactDataOutService() {
		return m_bizImpactDataOutService;
	}

	public IStatisticsDataOutService getStatisticsDataOutService() {
		return m_statisticsDataOutService;
	}

	public Version_PortType getVersionService() {
		return m_versionService;
	}

	public IInternalServices getInternalService() {
		return m_internalServices;
	}

}
