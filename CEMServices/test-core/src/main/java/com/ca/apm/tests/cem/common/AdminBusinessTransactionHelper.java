package com.ca.apm.tests.cem.common;

/**
 * /**
 * CommonFrmaework Helper class that assists in doing all Business Transaction related 
 * operations 
 * 
 * @author murma13
 */

import com.ca.wily.cem.qa.api.*;
import com.ca.wily.cem.qa.common.util.ByteToZipFileConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class AdminBusinessTransactionHelper {

	protected CEMServices m_cemServices;

	/**
	 * Class constructor.
	 * 
	 * @param a_cemServices
	 *            handle for all available web-services.
	 */
	public AdminBusinessTransactionHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * Creates a business transaction in a business service specified as
	 * Business Service Name with all custom values for SLA ( not inherited from
	 * the domain )
	 * 
	 * @param businessServiceName
	 * @param BusinesstransactionName
	 * @param BusinessTransactionDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public long createBusinessTransactionByBusinesServiceName(
			String businessServiceName, String businessTransactionName,
			String businessTransactionDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException, RemoteException {
		long businessTransactionId = 0;
			businessTransactionId = m_cemServices.getInternalService()
					.createBusinessTransaction(businessServiceName,
							businessTransactionName,
							businessTransactionDescription,
							collectTransactionStats,
							transactionImpactLevelInherited,
							successRateSlaInherited, sigmaSlaInherited,
							tranTimeSlaInherited, calculateBusinessValue,
							transactionImpactLevelValue, successRateSlaValue,
							sigmaSlaValue, transTimeValue,
							calculateBusinessValueValue);
		System.out.println("Business Transaction " + businessTransactionName + " Created Successfully");
		return businessTransactionId;

	}

	/**
	 * Creates a business transaction in a business service specified by
	 * Business Service ID with all custom values for SLA ( not inherited from
	 * the domain )
	 * 
	 * @param businessServiceId
	 * @param BusinesstransactionName
	 * @param BusinessTransactionDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 */
	public long createBusinessTransactionByBusServiceId(long businessServiceId,
			String businessTransactionName,
			String businessTransactionDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException, RemoteException {
		long businessTransactionId = 0;
		businessTransactionId = m_cemServices.getInternalService()
					.createBusinessTransactionById(businessServiceId,
							businessTransactionName,
							businessTransactionDescription,
							collectTransactionStats,
							transactionImpactLevelInherited,
							successRateSlaInherited, sigmaSlaInherited,
							tranTimeSlaInherited, calculateBusinessValue,
							transactionImpactLevelValue, successRateSlaValue,
							sigmaSlaValue, transTimeValue,
							calculateBusinessValueValue);
		System.out.println("Business Transaction " + businessTransactionName + " Created Successfully");
		return businessTransactionId;

	}

	/**
	 * 
	 * Creates a response based business transaction from business transaction
	 * specified by Name in a Business Service specified by name with all custom
	 * values for SLA ( not inherited from the domain )
	 * 
	 * @param businessServiceName
	 * @param baseBusinessTransactionName
	 * @param responseBTransactionName
	 * @param responseBTransactionDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createResponseBusinessTransactionByName(
			String businessServiceName, String baseBusinessTransactionName,
			String responseBTransactionName,
			String responseBTransactionDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException,
			RemoteException {
		long businessTransactionId;
		businessTransactionId = m_cemServices.getInternalService()
				.createResponseBusinessTransaction(businessServiceName,
						baseBusinessTransactionName, responseBTransactionName,
						responseBTransactionDescription,
						collectTransactionStats,
						transactionImpactLevelInherited,
						successRateSlaInherited, sigmaSlaInherited,
						tranTimeSlaInherited, calculateBusinessValue,
						transactionImpactLevelValue, successRateSlaValue,
						sigmaSlaValue, transTimeValue,
						calculateBusinessValueValue);
		System.out.println("Response based transaction " + responseBTransactionName + " Created Successfully");
		return businessTransactionId;
	}

	/**
	 * 
	 * Creates a response based business transaction from business transaction
	 * specified by ID in a Business Service specified by Id with all custom
	 * values for SLA ( not inherited from the domain )
	 * 
	 * @param businessServiceId
	 * @param baseBusinessTransactionId
	 * @param baseBusinessTransactionName
	 * @param responseBTransactionName
	 * @param responseBTransactionDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public long createResponseBusinessTransactionByBSBTxId(
			long businessServiceId, long baseBusinessTransactionId,
			String baseBusinessTransactionName,
			String responseBTransactionName,
			String responseBTransactionDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException,
			RemoteException {
		long businessTransactionId;
		businessTransactionId = m_cemServices.getInternalService()
				.createResponseBusinessTransactionById(businessServiceId,
						baseBusinessTransactionId, responseBTransactionName,
						responseBTransactionDescription,
						collectTransactionStats,
						transactionImpactLevelInherited,
						successRateSlaInherited, sigmaSlaInherited,
						tranTimeSlaInherited, calculateBusinessValue,
						transactionImpactLevelValue, successRateSlaValue,
						sigmaSlaValue, transTimeValue,
						calculateBusinessValueValue);
		System.out.println("Response based transaction " + responseBTransactionName + " Created Successfully");
		return businessTransactionId;
	}

	/**
	 * '
	 * 
	 * Updates Business Transaction in Business Service specified by name with
	 * specified properties
	 * 
	 * @param businessServiceName
	 * @param newBTName
	 * @param BusinessTxName
	 * @param BusinessTxDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public void updateBusinessTransactionUsingBServiceName(
			String businessServiceName, String newBTName,
			String businessTxName, String businessTxDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().updateBusinessTransaction(
				businessServiceName, newBTName, businessTxName,
				businessTxDescription, collectTransactionStats,
				transactionImpactLevelInherited, successRateSlaInherited,
				sigmaSlaInherited, tranTimeSlaInherited,
				calculateBusinessValue, transactionImpactLevelValue,
				successRateSlaValue, sigmaSlaValue, transTimeValue,
				calculateBusinessValueValue);
		System.out.println("Business transaction " + businessTxName + " Updated Successfully");

	}

	/**
	 * 
	 * Updates Business Transaction in Business Service specified by ID with
	 * specified properties
	 * 
	 * 
	 * 
	 * @param newBTName
	 * @param businessTxId
	 * @param businessTxDescription
	 * @param collectTransactionStats
	 * @param transactionImpactLevelInherited
	 *            if passed true ignores the value specified
	 * @param successRateSlaInherited
	 *            if passed true ignores the value specified
	 * @param sigmaSlaInherited
	 *            if passed true ignores the value specified
	 * @param tranTimeSlaInherited
	 *            if passed true ignores the value specified
	 * @param calculateBusinessValue
	 * @param transactionImpactLevelValue
	 * @param successRateSlaValue
	 * @param sigmaSlaValue
	 * @param transTimeValue
	 * @param calculateBusinessValueValue
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateBusinessTransactionUsingBServiceId(String newBTName,
			long businessTxId, String businessTxDescription,
			boolean collectTransactionStats,
			boolean transactionImpactLevelInherited,
			boolean successRateSlaInherited, boolean sigmaSlaInherited,
			boolean tranTimeSlaInherited, boolean calculateBusinessValue,
			String transactionImpactLevelValue, String successRateSlaValue,
			String sigmaSlaValue, String transTimeValue,
			String calculateBusinessValueValue) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().updateBusinessTransactionById(
				newBTName, businessTxId, businessTxDescription,
				collectTransactionStats, transactionImpactLevelInherited,
				successRateSlaInherited, sigmaSlaInherited,
				tranTimeSlaInherited, calculateBusinessValue,
				transactionImpactLevelValue, successRateSlaValue,
				sigmaSlaValue, transTimeValue, calculateBusinessValueValue);
		
		System.out.println("Business transaction Id" + businessTxId + " Updated Successfully");

	}
	
	/**
	 * 
	 * Enables the business transaction in a business service specified by name
	 * for monitoring
	 * 
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void enableBusinessTxMonitoringUsingName(String businessServiceName,
			String businessTxName) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().enableBusinessTransaction(
				businessServiceName, businessTxName);
		
		System.out.println("Business Transaction" + businessTxName + " Enabled Successfully for Monitoring ");

	}

	/**
	 * 
	 * Enables the business transaction in a business service specified by ID
	 * for monitoring
	 * 
	 * @param businessTxId
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void enableBusinessTxMonitoringUsingId(long businessTxId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableBusinessTransactionById(
				businessTxId);
		
		System.out.println("Business Transaction id" + businessTxId + " Enabled Successfully for Monitoring ");

	}

	/**
	 * 
	 * Disables monitoring for the business transaction in a business service
	 * specified by name
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void disableBusinessTxMonitoringUsingName(
			String businessServiceName, String businessTxName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().disableBusinessTransaction(
				businessServiceName, businessTxName);
		
		System.out.println("Business Transaction " + businessTxName + " disabled Successfully for Monitoring ");

	}

	/**
	 * 
	 * Disables monitoring for the business transaction in a business service
	 * specified by name
	 * 
	 * 
	 * @param businessTxId
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void disableBusinessTxMonitoringUsingId(long businessTxId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableBusinessTransactionById(
				businessTxId);
		System.out.println("Business Transaction id" + businessTxId + " disabled Successfully for Monitoring ");
	}

	/**
	 * 
	 * Delete business transaction specified in a Business Service by Name
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void deleteBusinessTxByName(String businessServiceName,
			String businessTransactionName) throws CEMWebServicesException,
			RemoteException {

		m_cemServices.getInternalService().deleteBusinessTransaction(
				businessServiceName, businessTransactionName);
		System.out.println("Business Transaction " + businessTransactionName + "Deleted Successfully ");
	}

	/**
	 * 
	 * Delete business transaction specified in a Business Service by ID
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void deleteBusinessTxById(long businessTxId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteBusinessTransactionById(
				businessTxId);
		
		System.out.println("Business Transaction Id " + businessTxId + "Deleted Successfully ");

	}

	/**
	 * 
	 * 
	 * exports a single business transaction specified by name from a Business
	 * service specified by name into zipfile
	 * 
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @param newBusinessServiceName
	 * @param fileName
	 *            Specify zipfile name with .zip extension
	 * @return Returns a zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public File exportBusinessTxByName(String businessServiceName,
			String businessTxName, String fileName)
			throws FileNotFoundException, IOException, CEMWebServicesException,
			RemoteException {
		byte[] buffer = new byte[1024];
		File file = new File(fileName);
		buffer = m_cemServices.getInternalService().exportBusinessTransaction(
				businessServiceName, businessTxName);
		ByteToZipFileConverter btoZip = new ByteToZipFileConverter();
		return btoZip.createZipFileFromByteArray(buffer, file);

	}

	/**
	 * 
	 * exports a single business transaction specified by ID into zipfile
	 * 
	 * @param businessTxId
	 * @param fileName
	 *            Specify zipfile name with .zip extension
	 * @return Returns a zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public File exportBusinessTxById(long businessTxId, String fileName)
			throws FileNotFoundException, IOException, CEMWebServicesException,
			RemoteException {
		byte[] buffer = new byte[1024];
		File file = new File(fileName);
		buffer = m_cemServices.getInternalService()
				.exportBusinessTransactionById(businessTxId);
		ByteToZipFileConverter btoZip = new ByteToZipFileConverter();
		return btoZip.createZipFileFromByteArray(buffer, file);

	}

	/**
	 * 
	 * @param businessServiceName
	 * @param BusinessTxNameList
	 * @param fileName
	 *            Specify zipfile name with .zip extension
	 * @return Returns a zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public File exportBusinessTxByNameList(String businessServiceName,
			String[] BusinessTxNameList, String fileName)
			throws FileNotFoundException, IOException, CEMWebServicesException,
			RemoteException {
		byte[] buffer = new byte[1024];
		File file = new File(fileName);
		buffer = m_cemServices.getInternalService()
				.exportBusinessTransactionList(businessServiceName,
						BusinessTxNameList);
		ByteToZipFileConverter btoZip = new ByteToZipFileConverter();
		return btoZip.createZipFileFromByteArray(buffer, file);

	}

	/**
	 * 
	 * Exports all transactions specified in a list of transaction ids
	 * 
	 * 
	 * 
	 * 
	 * @param businessTransactionId
	 * @param fileName
	 *            Specify zipfile name with .zip extension
	 * @return Returns a zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public File exportBusinessByTxIdList(long[] businessTransactionId,
			String fileName) throws FileNotFoundException, IOException,
			CEMWebServicesException, RemoteException {
		byte[] buffer = new byte[1024];
		File file = new File(fileName);
		buffer = m_cemServices.getInternalService()
				.exportBusinessTransactionByIdList(businessTransactionId);
		ByteToZipFileConverter btoZip = new ByteToZipFileConverter();
		return btoZip.createZipFileFromByteArray(buffer, file);

	}

	/**
	 * 
	 * Exports all business transactions for a business service in zip file
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param fileName
	 *            Specify zipfile name with .zip extension
	 * @return Returns a zip file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public File exportAllBusinessTransactions(String businessServiceName,
			String businessTransactionName, String zipFileName)
			throws FileNotFoundException, IOException, CEMWebServicesException,
			RemoteException {
		byte[] buffer = new byte[1024];
		File file = new File(zipFileName);
		buffer = m_cemServices.getInternalService()
				.exportAllBusinessTransactions(businessServiceName);
		ByteToZipFileConverter btoZip = new ByteToZipFileConverter();
		return btoZip.createZipFileFromByteArray(buffer, file);
	}

	/**
	 * 
	 * Move Business transaction from one business service to another
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @param newBusinessServiceName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void moveBusinessTxToBusinessService(String businessServiceName,
			String businessTxName, String newBusinessServiceName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().moveBusinessTransaction(
				businessServiceName, businessTxName, newBusinessServiceName);
		
		System.out.println("Business Transaction " + businessTxName + "Moved Successfully ");
	}

	/**
	 * 
	 * Update defect specification for a business transaction specified by name
	 * in a business service specified by name
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * @param defectName
	 * @param newName
	 * @param bEnabled
	 * @param bLocked
	 * @param conditionValue
	 * @param importance
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateBusinessTxDefectSpecsByName(String businessServiceName,
			String businessTransactionName, String defectName, String newName,
			boolean bEnabled, boolean bLocked, String conditionValue,
			String importance) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService()
				.updateBusinessTransactionSpecification(businessServiceName,
						businessTransactionName, defectName, newName, bEnabled,
						bLocked, conditionValue, importance);
		
		System.out.println( defectName + " specification for Business Transaction " + businessTransactionName + "Updated Successfully ");

	}

	/**
	 * 
	 * Update defect specification for a business transaction specified by ID in
	 * a
	 * 
	 * 
	 * @param businessTxId
	 * @param defectName
	 * @param newName
	 * @param bEnabled
	 * @param bLocked
	 * @param conditionValue
	 * @param importance
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateBusinessTxDefectSpecsById(long businessTxId,
			String defectName, String newName, boolean bEnabled,
			boolean bLocked, String conditionValue, String importance)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService()
				.updateBusinessTransactionSpecificationById(businessTxId,
						defectName, newName, bEnabled, bLocked, conditionValue,
						importance);
		System.out.println( defectName + " specification for Business Transaction id " + businessTxId + "Updated Successfully ");
	}

	/**
	 * 
	 * Enables defect specification for Business transaction specified by name
	 * in a business service
	 * 
	 * 
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @param defectName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void enableBusinessTxDefectSpecsByName(String businessServiceName,
			String businessTxName, String defectName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService()
				.enableBusinessTransactionSpecification(businessServiceName,
						businessTxName, defectName);
		
		System.out.println( defectName + " specification for Business Transaction " + businessTxName + "Enabled Successfully ");

	}

	/**
	 * 
	 * Enables defect specification for Business transaction specified by ID in
	 * a business service
	 * 
	 * @param businessTxId
	 * @param defectName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void enableBusinessTxDefectSpecsById(long businessTxId,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableTransactionSpecificationById(
				businessTxId, defectName);
		
		System.out.println( defectName + " specification for Business Transaction id " + businessTxId + "Enabled Successfully ");

	}

	/**
	 * 
	 * disables defect specification for Business transaction specified by name
	 * in a business service
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @param defectName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void disableBusinessTxDefectSpecsByName(String businessServiceName,
			String businessTxName, String defectName)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService()
				.disableBusinessTransactionSpecification(businessServiceName,
						businessTxName, defectName);
		
		System.out.println( defectName + " specification for Business Transaction" + businessTxName + "Disabled Successfully ");

	}

	/**
	 * 
	 * Disables defect specification for Business transaction specified by ID
	 * 
	 * @param businessTxId
	 * @param defectName
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void disableBusinessTxDefectSpecsById(long businessTxId,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService()
				.disableBusinessTransactionSpecificationById(businessTxId,
						defectName);
		
		System.out.println( defectName + " specification for Business Transaction Id" + businessTxId + "Disabled Successfully ");

	}

	/**
	 * Creating zip file example picked from net
	 * 
	 */

	/*
	 * public class CreateZipFile {
	 * 
	 * private static void doCreateZipFile(String[] files) {
	 * 
	 * String zipFileName = files[0]; byte[] buf = new byte[1024];
	 * 
	 * try { ZipOutputStream zipOut = new ZipOutputStream(new
	 * FileOutputStream(new File("compressed.zip")));
	 * 
	 * ZipOutputStream out = new ZipOutputStream(new
	 * FileOutputStream(zipFileName));
	 * 
	 * System.out.println("Archive:  " + zipFileName);
	 * 
	 * // Compress the files for (int i=1; i<files.length; i++) {
	 * 
	 * FileInputStream in = new FileInputStream(files[i]);
	 * System.out.println("  adding: " + files[i]);
	 * 
	 * out.putNextEntry(new ZipEntry(files[i]));
	 * 
	 * // Transfer bytes from the file to the ZIP file int len; while((len =
	 * in.read(buf)) > 0) { out.write(buf, 0, len); }
	 * 
	 * // Complete the entry out.closeEntry(); in.close(); }
	 * 
	 * // Complete the ZIP file out.close();
	 * 
	 * 
	 * } catch (IOException e) { e.printStackTrace(); System.exit(1); }
	 * 
	 * }
	 * 
	 * try { // Open the ZIP file ZipFile zf = new ZipFile("filename.zip");
	 * 
	 * // Enumerate each entry for (Enumeration entries = zf.entries();
	 * entries.hasMoreElements();) { // Get the entry name String zipEntryName =
	 * ((ZipEntry)entries.nextElement()).getName(); } } catch (IOException e) {
	 * }
	 */

	/**
	 * 
	 * 
	 * 
	 * @param businessServiceName
	 * @param businessTransactionName
	 * 
	 *            public void deleteAllBusinessTransactionsFromBusinessService(
	 *            String businessServiceName, String businessTransactionName) {
	 *            try { m_cemServices.getInternalService()
	 *            .deleteBusinessTransaction(businessServiceName,
	 *            businessTransactionName); } catch (CEMWebServicesException e)
	 *            { // TODO Auto-generated catch block e.printStackTrace(); }
	 *            catch (RemoteException e) { // TODO Auto-generated catch block
	 *            e.printStackTrace(); }
	 * 
	 *            }
	 */

	/**
	 * This method returns an array of Business Transactions present in the
	 * Business Service specified using Business service Id.
	 * 
	 * @param businessProcessDefinitionId
	 *            - specifies the business Service Id
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @return- Array of Business Transactions
	 */
	public BusinessTransactionDefinition[] getBusinessTransactionDefinitionsByBusinessServiceId(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] businessTxnsFromBS = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessTransactionDefinitions(businessServiceId);
		System.out.println(" Retrival of Business Transactions successful : ");

		return businessTxnsFromBS;
	}

	/**
	 * This method returns the array of business transactions present in the
	 * business service specified by the Business Name
	 * 
	 * @param businessServiceName
	 *            - specifies the business service name
	 * @return - returns an array of business transactions
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition[] getBusinessTransactionDefinitionsByBusinessServiceName(
			String businessServiceName) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] businessTxnsFromBS_Name = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessTransactionDefinitions(businessServiceName);
		System.out.println(" Retrival of Business Transactions successful : ");
		return businessTxnsFromBS_Name;
	}

	/**
	 * Gets Business Transaction Definitions for a specified Business Process.
	 * It retrieves the identifying parameters for the business transactions if
	 * needed. Throws exception if specified Business Process Definition is not
	 * found
	 * 
	 * @param businessProcessDefinitionId
	 *            -- specifies the business Service Id -ts_transet_group table
	 * @param includeIdentifyingParams
	 *            - boolean TRUE or FALSE- if True - Identifying parameter
	 *            name/value pairs are included if true
	 * @return - Array of Business Transactions present in the specified
	 *         Business Service
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public BusinessTransactionDefDetail[] getBusinessTransactionDefinitionsByBusinessProcessIdIncludingIdentifyingParams(
			long businessProcessDefinitionId, boolean includeIdentifyingParams)
			throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefDetail[] businessTxnsFromBS_IdentifyingParams1 = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessTransactionDefsWithDetails(
						businessProcessDefinitionId, includeIdentifyingParams);
		System.out.println(" Retrival of Business Transactions successful : ");
		return businessTxnsFromBS_IdentifyingParams1;
	}

	//

	/**
	 * Gets all enabled Business Transaction Definitions for a specified
	 * Business Process Definition. It retrieves the identifying parameters for
	 * the business transactions if needed. Throws exception if specified
	 * Business Process Definition is not found.
	 * 
	 * @param businessProcessDefinitionId
	 *            - Specifies the id of the business service - ts_transet_group
	 * @return - array of enabled business transactions from business service.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition[] getEnabledBusinessTransactionDefinitions(
			long businessProcessDefinitionId) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] Enabled_businessTxnsFromBS_Id1 = m_cemServices
				.getConfigurationDataOutService()
				.getEnabledBusinessTransactionDefinitions(
						businessProcessDefinitionId);
		System.out.println(" Retrival of Business Service successful : ");

		return Enabled_businessTxnsFromBS_Id1;
	}

	/**
	 * 
	 * @param businessProcessDefinitionId
	 *            -- Specifies the id of the business service - ts_transet_group
	 * @param includeIdentifyingParams
	 *            - - boolean TRUE or FALSE- if True - Identifying parameter
	 *            name/value pairs are included if true
	 * @return - Array of enabled business transaction with details in the
	 *         specified business service
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition[] getEnabledBusinessTransactionDefinitionsWithDetails(
			long businessProcessDefinitionId, boolean includeIdentifyingParams)
			throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefinition[] Enabled_businessTxnsWithDetailsFromBS_Id = m_cemServices
				.getConfigurationDataOutService()
				.getEnabledBusinessTransactionDefsWithDetails(
						businessProcessDefinitionId, includeIdentifyingParams);

		System.out.println(" Retrival of Business Service successful : ");
		return Enabled_businessTxnsWithDetailsFromBS_Id;
	}

	/**
	 * Gets filtered enabled Business Transaction Definitions for a specified
	 * Business Process Definition. Throws exception if specified Business
	 * Process Definition is not found.
	 * 
	 * @param businessProcessDefinitionId
	 *            --- Specifies the id of the business service -
	 *            ts_transet_group
	 * @param regex_pattern
	 *            - Pattern for matching the Transaction name
	 * @return - Array of Business transactions matching the specified regex
	 *         pattern in the specifed Business Service
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition[] getFilteredEnabledBusinessTransactionDefinitions(
			long businessProcessDefinitionId, String regex_pattern)
			throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefinition[] filtered_Enabled_businessTxnsWithDetailsFromBS_Id = m_cemServices
				.getConfigurationDataOutService()
				.getFilteredEnabledBusinessTransactionDefinitions(
						businessProcessDefinitionId, regex_pattern);

		System.out
				.println(" Retrival of Business Service Transactions succesfull: ");

		return filtered_Enabled_businessTxnsWithDetailsFromBS_Id;
	}

	/**
	 * Gets filtered enabled Business Transaction Definitions for a specified
	 * Business Process Definition. It retrieves the identifying parameters for
	 * the business transactions if needed. Throws exception if specified
	 * Business Process Definition is not found
	 * 
	 * @param businessProcessDefinitionId
	 *            - Specifies the id of the business service - ts_transet_group
	 * @param regex_pattern
	 *            - Pattern for matching the Transaction name
	 * @param includeIdentifyingParams
	 *            - boolean TRUE or FALSE- if True - Identifying parameter
	 *            name/value pairs are included if true
	 * @return
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition[] getFilteredEnabledBusinessTransactionDefsWithDetails(
			long businessProcessDefinitionId, String regex_pattern,
			boolean includeIdentifyingParams) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] filtered_Enabled_businessTxnsWithDetailsFromBS_Id_WithDetails = m_cemServices
				.getConfigurationDataOutService()
				.getFilteredEnabledBusinessTransactionDefsWithDetails(
						businessProcessDefinitionId, regex_pattern,
						includeIdentifyingParams);

		System.out
				.println(" Retrival of Business Service Transactions successful: ");

		return filtered_Enabled_businessTxnsWithDetailsFromBS_Id_WithDetails;
	}

	/**
	 * Gets the Business Transaction Definition for a specified Business Service
	 * Definition. Throws exception if specified Business Service Definition or
	 * Business Transaction is not found.
	 * 
	 * @param businessServiceName
	 *            - specifies the name of the Business Services Name -
	 * @param transactionName
	 *            - Specifies the name of the Business Transaction
	 * @return - returns the business transaction object present matching the
	 *         specified name from the specified business services.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public BusinessTransactionDefinition getBusinessTransactionDefinitionByName(
			String businessServiceName, String businessTransactionName)
			throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefinition businessTransactionDefinitionByName = m_cemServices
				.getConfigurationDataOutService()
				.getBusinessTransactionDefinitionByName(businessServiceName,
						businessTransactionName);

		System.out
				.println(" Retrival of Business Service Transaction successful : ");

		return businessTransactionDefinitionByName;
	}

	/**
	 * Returns the Full Business Transaction Definition data object. NOTE: the
	 * full definition contains all data and my be updated release-to-release,
	 * thus requiring a new stub, if it is used.
	 * 
	 * @param id
	 *            - Specifies the Id of the Business Transaction
	 * @return - returns full business transaction definition data object
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public BusinessTransactionDefinitionFull getFullBusinessTransactionDefinitionByBTId(
			long businessTransactionId) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinitionFull fullBusinessTransactionDefinitionById = m_cemServices
				.getInternalService().getFullBusinessTransactionDefinitionById(
						businessTransactionId);

		System.out
				.println(" Retrival of full Business  Transaction successful : ");

		return fullBusinessTransactionDefinitionById;
	}

	/**
	 * Gets all Business Transaction Definitions in the system. It retrieves the
	 * identifying parameters for the business transactions if needed. Throws
	 * exception if specified Business Process Definition is not found.
	 * 
	 * @param includeIdentifyingParams
	 *            -Identifying parameter name/value pairs are included if true
	 * 
	 * @return-Array of DBusinesstransactionDefDetail
	 */
	public BusinessTransactionDefDetail[] getAllBusinessTransactionDefsWithDetails(
			boolean includeIdentifyingParams) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefDetail[] allBusinessTransactionDefsWithDetails = m_cemServices
				.getConfigurationDataOutService()
				.getAllBusinessTransactionDefsWithDetails(
						includeIdentifyingParams);

		System.out
				.println(" Retrival of full Business Transaction Transactions successful : ");

		return allBusinessTransactionDefsWithDetails;
	}

	/**
	 * Gets all Business Transaction Definitions by the Application Name. Throws
	 * exception if specified Business Process Definition is not found.
	 * 
	 * @param applicationName
	 *            - Specifies the application Name
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * @return- Array of BusinessTransactionDefinition object matching the
	 *          specified application name.
	 */
	public BusinessTransactionDefinition[] getAllBusinessTransactionDefsByApplicationName(
			String applicationName) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] allBusinessTransactionDefsByApplicationName = m_cemServices
				.getConfigurationDataOutService()
				.getAllBusinessTransactionDefsByApplicationName(applicationName);

		System.out
				.println(" Retrival of full Business Transactions successful  : ");

		return allBusinessTransactionDefsByApplicationName;
	}

	/**
	 * This method gets filtered Business Transaction Definitions for a
	 * specified Business Process Definition. Throws exception if specified
	 * Business Process Definition is not found.
	 * 
	 * @param businessProcessDefinitionId
	 *            - specified the business process id
	 * @param regex
	 *            - specifies the pattern to be matched
	 * @return - Array of BusinessTransactionDefintion object the specified
	 *         pattern in the BS
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessTransactionDefinition[] getFilteredBusinessTransactionDefinitions(
			long businessProcessDefinitionId, java.lang.String regex)
			throws CEMWebServicesException, RemoteException {

		BusinessTransactionDefinition[] filteredBusinessTransactionDefsRegex = m_cemServices.getConfigurationDataOutService()
				.getFilteredBusinessTransactionDefinitions(
						businessProcessDefinitionId, regex);

		System.out
				.println(" Retrival of filtered Business Transactions successful  : ");

		return filteredBusinessTransactionDefsRegex;
	}

	/**
	 * Gets filtered Business Transaction Definitions for a specified Business
	 * Process Definition. It retrieves the identifying parameters for the
	 * business transactions if needed. Throws exception if specified Business
	 * Process Definition is not found.
	 * 
	 * @param businessProcessDefinitionId
	 *            -specifies the BS id
	 * @param regex
	 *            - Specifies the pattern to be matched with
	 * @param includeIdentifyingParams
	 *            - Boolean True or false - Identifying parameter name/value
	 *            pairs are included if true.
	 * @return- Array of BusinessTransactionDefintion object the specified
	 *          pattern in the BS
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessTransactionDefinition[] getFilteredBusinessTransactionDefsWithDetails(
			long businessProcessDefinitionId, java.lang.String regex,
			boolean includeIdentifyingParams) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] filteredBusinessTransactionDefsRegexWithDetails = m_cemServices
				.getConfigurationDataOutService()
				.getFilteredBusinessTransactionDefsWithDetails(
						businessProcessDefinitionId, regex,
						includeIdentifyingParams);

		System.out
				.println(" Retrival of filtered Business Transactions successful  : ");

		return filteredBusinessTransactionDefsRegexWithDetails;
	}

	/**
	 * Gets all Business Transaction Definitions from a specified Business
	 * Process Definition that can be used as a replacement. That includes
	 * request business transactions only that do not have extended business
	 * transactions. Throws exception if specified Business Process Definition
	 * is not found.
	 * 
	 * @param businessProcessName
	 *            - specifies the name of the Business Process
	 * @return - Array of Business Transactions (request based) present in the
	 *         Business Process
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessTransactionDefinition[] getBTReplacementsFromBusinessProcess(
			String businessProcessName) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] transactionReplacements = m_cemServices
				.getConfigurationDataOutService()
				.getTransetReplacementsFromBusinessProcess(businessProcessName);
		{
			System.out
					.println(" Retrival of  replacement Business Transactions successful  : ");

			return transactionReplacements;
		}
	}

	/**
	 * Gets all Business Transaction Definitions from a specified Business
	 * Process Definition that can be used as a replacement. That includes
	 * request business transactions only that do not have extended business
	 * transactions. Throws exception if specified Business Process Definition
	 * is not found.
	 * 
	 * @param businessProcessId
	 *            -specifies the Business Process Definition id
	 * @return -Array of Business Transaction Definitions
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessTransactionDefinition[] getBTReplacementsFromBusinessProcessByBusinessProcessId(
			long businessProcessId) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] transactionReplacementsById = m_cemServices
				.getConfigurationDataOutService()
				.getTransetReplacementsFromBusinessProcessId(businessProcessId);
		{
			System.out
					.println(" Retrival of  replacement Business Transactions successful  : ");

			return transactionReplacementsById;
		}
	}
	
	/**
	 * 
	 * Returns all Business Transactions in a business Service
	 * 
	 * 
	 * @param businessServiceId
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public BusinessTransactionDefinition[] getAllBusinessTxDefsInBusinessService(
			long businessServiceId) throws CEMWebServicesException,
			RemoteException {

		BusinessTransactionDefinition[] transactionReplacementsById = getFilteredBusinessTransactionDefinitions(businessServiceId, ".*");
		{
			System.out
					.println(" Retrival of  replacement Business Transactions successful  : ");

			return transactionReplacementsById;
		}
	}
	
	
	/**
	 * 
	 * Returns true if specified BT in specified BS is enabled
	 * 
	 * 
	 * @param businessServiceName
	 * @param businessTxName
	 * @return
	 * @throws RemoteException
	 */
	public boolean isBTEnabled(String businessServiceName, String businessTxName)
			throws RemoteException {
		boolean a = false;
		BusinessProcessDefinition bpd = m_cemServices.m_configurationDataOutService
				.getBusinessProcessDefinitionByName(businessServiceName);
		BusinessTransactionDefinition[] btd = m_cemServices.m_configurationDataOutService
				.getEnabledBusinessTransactionDefinitions(bpd.getId());
		for (int j = 0; j < btd.length; j++)
			if (btd[j].getName().equalsIgnoreCase(businessTxName))
				a = true;

		return a;
	}

}
