package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.BusinessProcessDefinition;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import java.io.File;
import java.io.IOException;

public class AdminBTImportHelper {

	protected CEMServices m_cemServices;
	AdminBusinessServiceHelper m_adminBS;
	String m_host;
	int m_port;
	String m_jsessionid;

	public AdminBTImportHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
		m_adminBS = new AdminBusinessServiceHelper(m_cemServices);
		m_jsessionid = m_cemServices.getM_jsessionID();
		String strPort = m_cemServices.getM_port();
		m_port = Integer.parseInt(strPort);
		m_host = m_cemServices.getM_host();
	}

	public void importZipFileToExistingBS(String businessServiceName,
			String sourceFile) throws IOException {

		if (m_jsessionid != null) {
			BusinessProcessDefinition businessProcess = m_adminBS
					.getBusinessProcessDefinitionsByName(businessServiceName);
			long BS_id = businessProcess.getId();
			if (BS_id != 0) {
				File importFile = new File(sourceFile);
				if (!importFile.exists()) {
					throw new RuntimeException("File path does not exist");
				}

				if (!importFile.getName().endsWith("zip")) {
					throw new RuntimeException(
							"Ensure the file is the exported zip file");
				}
				importValues(BS_id, importFile);
			} else {
				System.out.println("Business Service: " + businessServiceName
						+ " not found :: AdminBTImportHelper");
			}
		} else {
			System.out
					.println("User: CEM Session not established :: AdminBTImportHelper");

		}

	}

	public void importZipFileToNewBS(String businessAppName,
			String businessServiceName, String businessServiceDescription,
			String sourceFile) throws HttpException, IOException {

		// Check if jsessionId has value
		long BS_id = 0;
		if (m_jsessionid != null) {
			BS_id = m_adminBS.createBusinessServiceWithInheritedSLAValues(
					businessServiceName, businessServiceDescription,
					businessAppName);
			// check if create BS is successful
			if (BS_id != 0) {
				File importFile = new File(sourceFile);
				// check if the target file exists
				if (!importFile.exists()) {
					throw new RuntimeException("File path does not exist");
				}
				// check if the extension is zip
				if (!importFile.getName().endsWith("zip")) {
					throw new RuntimeException(
							"Ensure the file is the exported zip file");
				}
				importValues(BS_id, importFile);
			}

			else {
				System.out.println("Business Service: " + businessServiceName
						+ " not found :: AdminBTImportHelper");
			}
		} else {
			System.out
					.println("User: CEM Session not established :: AdminBTImportHelper");
		}
	}

	public void importXMLFiletoNewBS(String businessAppName,
			String businessServiceName, String businessServiceDescription,
			String sourceXMLFile) throws HttpException, IOException {
		File importFile = new File(sourceXMLFile);
		long BS_id = 0;
		// login(CEM_Login, Password);
		if (m_jsessionid != null) {
			BS_id = m_adminBS.createBusinessServiceWithInheritedSLAValues(
					businessServiceName, businessServiceDescription,
					businessAppName);
			if (BS_id != 0) {

				if (!importFile.exists()) {
					throw new RuntimeException("File path does not exist");
				}

				if (!importFile.getName().endsWith("xml")) {
					throw new RuntimeException(
							"Ensure the file is the exported xml file");
				}
				importValues(BS_id, importFile);
			}

			else {
				System.out.println("Business Service: " + businessServiceName
						+ " not found :: AdminBTImportHelper");
			}
		} else {
			System.out
					.println("User: CEM Session not established :: AdminBTImportHelper");
		}
	}

	public void importXMLFiletoExistingBS(String businessServiceName,
			String sourceXMLFile) throws HttpException, IOException {

		File importFile = new File(sourceXMLFile);
		long BS_id = 0;
		// login(CEM_Login, Password);
		if (m_jsessionid != null) {

			BusinessProcessDefinition businessProcess = m_adminBS
					.getBusinessProcessDefinitionsByName(businessServiceName);

			BS_id = businessProcess.getId();

			if (BS_id != 0) {

				if (!importFile.exists()) {
					throw new RuntimeException("File path does not exist");
				}

				if (!importFile.getName().endsWith("xml")) {
					throw new RuntimeException(
							"Ensure the file is the exported xml file");
				}
				importValues(BS_id, importFile);
			}

			else {
				System.out.println("Business Service: " + businessServiceName
						+ " not found :: AdminBTImportHelper");
			}
		} else {
			System.out
					.println("User: CEM Session not established :: AdminBTImportHelper");
		}
	}

	private void importValues(long BS_id, File importFile)
			throws HttpException, IOException {
		System.out
				.println("Importing the file - this can take few minutes depending on the file size...");

		String importUrl = "http://" + m_host + ":" + m_port
				+ "/wily/cem/tess/app/admin/importExportFileUpload.html?"
				+ "pId=" + String.valueOf(BS_id) + "&userAction=&importFile="
				+ importFile.getName();

		PostMethod filePost = new PostMethod(importUrl);
		filePost.setRequestHeader("Cookie", "JSESSIONID=" + m_jsessionid);
		Part[] parts = { new FilePart("importFile", importFile) };
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
				.getParams()));
		HttpClient client = new HttpClient();
		int status = client.executeMethod(filePost);
		if (status != 302) {
			System.out
					.println("File import failed - please ensure the server is running and the import file is formatted right");
		} else {
			System.out.println("Import Successful");

		}

	}

}
