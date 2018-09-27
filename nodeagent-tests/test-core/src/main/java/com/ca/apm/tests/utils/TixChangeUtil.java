package com.ca.apm.tests.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;

public class TixChangeUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(TixChangeUtil.class);

	public static void createCustomSession(String urlPrefix, String emailId, String session) {
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("email", emailId);
		postParams.put("id", session);
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(urlPrefix
		        + "/httpService/account/session").setHttpMethod(HttpRequestMethod.POST)
		        .setParams(postParams).setNumberReqs(1).build();
		txnGen.start();
	}

}
