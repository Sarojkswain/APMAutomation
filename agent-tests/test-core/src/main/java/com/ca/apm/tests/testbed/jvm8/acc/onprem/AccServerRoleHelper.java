package com.ca.apm.tests.testbed.jvm8.acc.onprem;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.acc.ACCConfigurationServerRole;
import com.ca.tas.type.Platform;

public class AccServerRoleHelper {
	private static final String ACC_SERVER_HTTPS_ENABLE_PROPERTY = "webserver.https.enable";
	private static final int ACC_SERVER_HTTP_PORT = 8088;
	private static final int ACC_SERVER_HTTPS_PORT = 8443;

	public static ACCConfigurationServerRole createAccServerRole(ITasResolver tasResolver,
	        String roleId) {
		return new ACCConfigurationServerRole.Builder(roleId, Platform.LINUX, tasResolver)
		        .property(ACC_SERVER_HTTPS_ENABLE_PROPERTY, String.valueOf(false))
		        .startAfterDeployment(false).build();
	}

	public static String getServerUrl(String accHost) {
		return String.format("http://%s:%d", accHost, ACC_SERVER_HTTP_PORT);
	}

	public static String getServerSecureUrl(String accHost) {
		return String.format("https://%s:%d", accHost, ACC_SERVER_HTTPS_PORT);
	}
}
