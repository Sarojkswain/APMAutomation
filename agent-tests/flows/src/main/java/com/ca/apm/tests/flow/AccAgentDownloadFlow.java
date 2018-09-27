package com.ca.apm.tests.flow;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.utils.archive.Archive;
import com.ca.apm.automation.utils.archive.TasZipArchive;

/**
 * @author kurma05
 */
@Flow
public class AccAgentDownloadFlow extends FlowBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccAgentDownloadFlow.class);
	private static final String DEFAULT_ACC_HOST = "https://acc-route-edge-8088-apm.app.unvdev1.cs.saas.ca.com";
	private static final String ACC_LOGIN_PATH = "/login";
	private static final String ACC_PACKAGE_URL_PATH = "/apm/acc/package?size=40";
	private static final String ACC_LOGIN_USER = "user@example.com";
	private static final String ACC_LOGIN_PASSWORD = "acc";

	@FlowContext
	private AccAgentDownloadFlowContext context;

	@Override
	public void run() throws Exception {

		// get url
		String url = context.getAgentPackageUrl();
		if (url == null) {
			logger.info("Url wasn't provided; trying to retrieve it for package: "
			        + context.getPackageName());
			url = getUrl();
		}

		// download agent
		downloadPackage(url);
	}

	public String getUrl() throws Exception {
		String accHostBaseUri = (context.getAccServerUrl() == null) ? DEFAULT_ACC_HOST : context
		        .getAccServerUrl();

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost mainPage = new HttpPost(accHostBaseUri + ACC_LOGIN_PATH);
		HttpGet jsonRequest = new HttpGet(accHostBaseUri + ACC_PACKAGE_URL_PATH);

		List<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair("username", ACC_LOGIN_USER));
		arguments.add(new BasicNameValuePair("password", ACC_LOGIN_PASSWORD));
		mainPage.setEntity(new UrlEncodedFormEntity(arguments));

		logger.info("Logging into : {}", mainPage);
		HttpResponse mainPageResponse = client.execute(mainPage);

		logger.info("Requesting list of packages : {}", jsonRequest);
		HttpResponse jsonResponse = client.execute(jsonRequest);

		String jsonData = EntityUtils.toString(jsonResponse.getEntity());
		logger.info("Json output: \n" + jsonData);

		JSONObject obj = new JSONObject(jsonData);
		JSONArray jsonArray = obj.getJSONObject("_embedded").getJSONArray("package");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject item = jsonArray.getJSONObject(i);
			String name = item.getString("packageName");
			String os = item.getJSONObject("environment").getString("osName");

			if (name.equalsIgnoreCase(context.getPackageName()) && os.contains(context.getOsName())) {
				logger.info("Found package: " + name + " => " + os);

				String modified = item.getString("modified");
				File file = new File(context.getInstallDir() + "/accAgent.properties");
				FileUtils.writeStringToFile(file, "modified=" + modified);
				logger.info("Package properties written to " + file.getCanonicalPath());
				logger.info("Package Timestamp: " + modified);

				JSONObject download = item.getJSONObject("_links").getJSONObject("download");
				String href = download.getString("href");
				logger.info("href: " + href);
				return href;
			}
		}

		return null;
	}

	public void downloadPackage(String href) throws Exception {

		URL url = new URL(href);
		LOGGER.info("Downloading file : {}", url);
		String file = context.getInstallDir() + "/accagent.zip";
		FileUtils.copyURLToFile(url, new File(file));

		LOGGER.info("Unpacking file : {}", file);
		File archive = new File(file);
		Archive tasArchive = new TasZipArchive(archive);
		tasArchive.unpack(new File(context.getInstallDir()));
	}
}
