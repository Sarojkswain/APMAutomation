package com.ca.apm.tests.cem.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BaseProperties {

	// Section 1: These are the supported properties
	public static final String HOST = "emHostIp";
	public static final String PORT = "emHostPort";
	public static final String USER = "tessUser";
	public static final String PASSWORD = "tessPassword";
	public static final String TIMIP = "timIp";
	public static final String PETSHOPIP = "petshopServerIp";
	public static final String APACHEIP = "apacheServerIp";
	public static final String FILEDIR_EXE = "testDataDirUtility";
	public static final String TESS_REMOTELOGIN = "emMachineUser";
	public static final String TESS_REMOTEPWD = "emMachinePwd";
	public static final String DB_NAME = "databaseName";
	public static final String POSTGRES_ADMIN = "databaseUser";
	public static final String POSTGRES_PASSWORD = "databasePwd";
	public static final String POSTGRES_PORT = "databasePort";
	public static final String DB_TYPE = "databaseType";
	public static final String EM_INSTALL_DIR = "emInstallDir";
	public static final String MEDRECIP = "medrecServerIp";
	public static final String DEFAULT_CONTENT_TYPE = "text/plain,text/html,text/xml,application/x-java-serialized-object";
	public static final String MEDRECURL = "medrecURL";
	public static final String SRCDIRECTORY = "srcDirectory";
	public static final String DESTDIRECTORY = "destDirectory";
	public static final String APACHE_PORT ="apacheServerPort";
	public static final String FLEX_APP_SERVER_IP ="flexAppServerIp";
	public static final String FLEX_APP_SERVER_PORT ="flexAppServerPort";
	public static final String FLEXURL="flexURL";
	public static final String BROWSERPATH="browserPath";
	public static final String MEDREC_PORT="mederServerPort";
	public static final String MEDRECURL1 = "medrecURL1";
	public static final String FLEXAPPSERVERPORT = "flexAppServerPort";

	// Section 2: Initialized data
	protected HashMap<String, String> m_properties = new HashMap<String, String>();

	public String getProperty(String a_propName) {
		return m_properties.get(a_propName);
	}

	protected BaseProperties() {
		ResourceBundle environmentConstants = ResourceBundle
				.getBundle("environmentConstants");
		try {
			m_properties.put(HOST, environmentConstants.getString(HOST));
			m_properties.put(PORT, environmentConstants.getString(PORT));
			m_properties.put(USER, environmentConstants.getString(USER));
			m_properties.put(TESS_REMOTELOGIN,
					environmentConstants.getString(TESS_REMOTELOGIN));
			m_properties.put(TESS_REMOTEPWD,
					environmentConstants.getString(TESS_REMOTEPWD));
			m_properties.put(DB_NAME, environmentConstants.getString(DB_NAME));
			m_properties.put(POSTGRES_ADMIN,
					environmentConstants.getString(POSTGRES_ADMIN));
			m_properties.put(POSTGRES_PASSWORD,
					environmentConstants.getString(POSTGRES_PASSWORD));
			m_properties.put(POSTGRES_PORT,
					environmentConstants.getString(POSTGRES_PORT));
			m_properties.put(DB_TYPE, environmentConstants.getString(DB_TYPE));

			m_properties
					.put(PASSWORD, environmentConstants.getString(PASSWORD));
			m_properties.put(TIMIP, environmentConstants.getString(TIMIP));
			m_properties.put(FILEDIR_EXE,
					environmentConstants.getString(FILEDIR_EXE));
			m_properties.put(APACHEIP, environmentConstants.getString(APACHEIP));
			m_properties.put(APACHE_PORT, environmentConstants.getString(APACHE_PORT));
			
			m_properties.put(PETSHOPIP,environmentConstants.getString(PETSHOPIP));
	m_properties
			.put(MEDRECIP, environmentConstants.getString(MEDRECIP));
	m_properties
	.put(MEDREC_PORT, environmentConstants.getString(MEDREC_PORT));
	m_properties
	.put(MEDRECURL, environmentConstants.getString(MEDRECURL));	
	m_properties
	.put(SRCDIRECTORY, environmentConstants.getString(SRCDIRECTORY));
	m_properties
	.put(DESTDIRECTORY, environmentConstants.getString(DESTDIRECTORY));
	m_properties.put(FLEX_APP_SERVER_IP, environmentConstants.getString(FLEX_APP_SERVER_IP));
	m_properties.put(FLEX_APP_SERVER_PORT, environmentConstants.getString(FLEX_APP_SERVER_PORT));
	m_properties.put(FLEXURL, environmentConstants.getString(FLEXURL));
	m_properties.put(BROWSERPATH, environmentConstants.getString(BROWSERPATH));
	m_properties.put(MEDRECURL1, environmentConstants
			.getString(MEDRECURL1));
	m_properties.put(FLEXAPPSERVERPORT, environmentConstants
			.getString(FLEXAPPSERVERPORT));

		} catch (Exception e) {
			// TODO: Some logging, which may not work, since this is static
		}
	}

	private static BaseProperties s_singleton;

	public static BaseProperties getBaseProperties() {
		if (s_singleton == null)
			s_singleton = new BaseProperties();
		return s_singleton;
	}

	public static enum AppType {
		GENERIC("Generic"), APPSPECIFIC("Application Specific");

		private final String appType;

		AppType(String appType) {
			this.appType = appType;
		}

		public String getAppType() {
			return appType;
		}
	}

	public static enum AppAuthType {
		APPSPECIFIC("Application Specific"), BASICAUTH("Basic Authentication"), CASITEMIND(
				"CA SiteMinder"), NTLMAUTH("NTLM Authentication");

		private final String appAuthType;

		AppAuthType(String appAuthType) {
			this.appAuthType = appAuthType;

		}

		public String getAppAuthType() {
			return appAuthType;
		}
	}

	public static enum UserProcessingType {
		ECOMMERCE("E-Commerce"), ENTERPRISE("Enterprise");
		private final String userProcessingType;

		UserProcessingType(String userProcessingType) {
			this.userProcessingType = userProcessingType;

		}

		public String getUserProcessingType() {
			return userProcessingType;
		}
	}

	public static enum CharEncoding {
		ISO88591("ISO-8859-1"), UTF8("UTF-8"), EUCJP("EUC-JP"), SHIFTJIS(
				"Shift-JIS"), ISO2022JP("ISO-2022-JP"), WIN31J("Windows-31J"), GB2312(
				"GB2312"), BIG5("Big5"), EUCKR("EUC-KR");

		private final String charEncoding;

		CharEncoding(String charEncoding) {
			this.charEncoding = charEncoding;

		}

		public String getCharEncoding() {
			return charEncoding;
		}
	}

	/**
	 * The value stored in the ts_type column of the ts_defect_defs table when
	 * it specifies the Defect Def Type.
	 * 
	 * public static final short DEFECT_TYPE_SLOW_TRAN_TIME = 1; public static
	 * final short DEFECT_TYPE_FAST_TRAN_TIME = 2; public static final short
	 * DEFECT_TYPE_HIGH_THROUGHPUT = 3; public static final short
	 * DEFECT_TYPE_LOW_THROUGHPUT = 4; public static final short
	 * DEFECT_TYPE_LARGE_TRAN_SIZE = 5; public static final short
	 * DEFECT_TYPE_SMALL_TRAN_SIZE = 6; public static final short
	 * DEFECT_TYPE_HTTP_STATUS_ERROR = 8; public static final short
	 * DEFECT_TYPE_MISSING_SUB_PART = 9; public static final short
	 * DEFECT_TYPE_CONTENT_ERROR = 10; public static final short
	 * DEFECT_TYPE_MISSING_RESPONSE = 11; public static final short
	 * DEFECT_TYPE_PARTIAL_RESPONSE = 16; public static final short
	 * DEFECT_TYPE_HTTP_HEADER_PARAM = 17; public static final short
	 * DEFECT_TYPE_CUSTOM = 18; HOW to use in the TEST CASES TO GET THE DEFECT
	 * NAME ASSOCIATED WITH A CODE String defect_slowtimeValue =
	 * BaseProperties.DefectType.get(1).name(); TO GET THE DEFECT CODE
	 * ASSOCIATED WITH A DEFECT NAME int defect_slowtime =
	 * BaseProperties.DefectType.SLOW_TIME.getCode();
	 */

	public enum DefectType {
		SLOW_TIME(1, "Slow Time"), FAST_TIME(2, "Fast Time"), HIGH_THROUGHPUT(
				3, "High Throughput"), LOW_THROUGHPUT(4, "Low Throughput"), LARGE_TRANSACTION_SIZE(
				5, "Large Size"), SMALL_TRANSACTION_SIZE(6, "Small Size"), HTTP_STATUS_ERROR(
				8, "HTTP Status Code"), MISSING_SUB_PART(9,
				"Missing Transaction/Component"), CONTENT_ERROR(10,
				"Content Error"), MISSING_RESPONSE(11, "Missing Response"), PARTIAL_RESPONSE(
				16, "Partial Response"), HTTP_HEADER_PARAM(17,
				"HTTP Response Header Parameter"), CUSTOM(18, "Custom");

		private static final Map<String, Integer> lookup = new HashMap<String, Integer>();

		static {
			for (DefectType s : EnumSet.allOf(DefectType.class))
				lookup.put(s.getDefectName(), s.getDefectCode());
		}

		private static final Map<Integer, String> lookup2 = new HashMap<Integer, String>();

		static {
			for (DefectType s : EnumSet.allOf(DefectType.class))
				lookup2.put(s.getDefectCode(), s.getDefectName());
		}

		public static Integer get(String defectName) {
			return lookup.get(defectName);
		}

		public String getDefectName(int defectCode) {
			return lookup2.get(defectCode);
		}

		private final int defectCode; // in kilograms
		private final String defectName; // in meters

		DefectType(int defectCode, String defectName) {
			this.defectCode = defectCode;
			this.defectName = defectName;
		}

		public int getDefectCode() {
			return defectCode;
		}

		public String getDefectName() {
			return defectName;
		}

	}

	public static enum AutogenParameterType {
		COOKIE("COOKIE"), POST("POST"), QUERY("QUERY"), URL("URL"), HTTP_REQUEST_HEADER(
				"HTTPHEADER"), PATH("PATH"), ANY("ANY"), PLUG_IN("PLUGIN"), FLEX_REQ_PROPERTY(
				"FLEX_REQUEST_PROPERTY"), FLEX_HTTP_REQ_HEADER(
				"FLEX_HTTP_REQUEST_HEADER"), HTTP_RESPONSE("HTTP_RESPONSE"), HTTP_RESPONSE_HEADER(
				"HTTP_RESPONSE_HEADER"), HTML_RESPONSE_TAG("HTML_RESPONSE_TAG"), PLUG_IN_HTTP_RESP(
				"PLUGIN_HTTP_RESPONSE"), FLEX_RESP_PROPERTY(
				"FLEX_RESPONSE_PROPERTY"), HTTP_DELETE("HTTP");

		private final String autogenParamType;

		AutogenParameterType(String autogenParamType) {
			this.autogenParamType = autogenParamType;
		}

		public String getAutogenParamType() {
			return autogenParamType;
		}
	}

	public static enum ParameterType {
		COOKIE("COOKIE"), POST("POST"), QUERY("QUERY"), URL("URL"), HTTP_REQUEST_HEADER(
				"HTTP"), XML("XML"), CA_SITEMINDER("NETEGRITY_SITEMINDER"), NTML_AUTH(
				"NTLM_AUTH"), BASIC_AUTH("BASIC_AUTH"), X_WTG_INFO("X_WTG_INFO"), PLUG_IN(
				"PLUGIN"), PATH("PATH"), FLEX_REQ_PROPERTY(
				"FLEX_REQUEST_PROPERTY"), FLEX_HTTP_REQ_HEADER(
				"FLEX_HTTP_REQUEST_HEADER"), HTTP_RESPONSE("HTTP_RESPONSE"), HTTP_RESPONSE_HEADER(
				"HTTP_RESPONSE_HEADER"), HTML_RESPONSE_TAG("HTML_RESPONSE_TAG"), PLUG_IN_HTTP_RESP(
				"PLUGIN_HTTP_RESPONSE"), FLEX_RESP_PROPERTY(
				"FLEX_RESPONSE_PROPERTY");

		private final String paramType;

		ParameterType(String paramType) {
			this.paramType = paramType;
		}

		public String getParamType() {
			return paramType;
		}
	}

}
