package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.TransactionDefinition;

import java.rmi.RemoteException;

public class AdminTransactionHelper {

	protected CEMServices m_cemServices;

	public AdminTransactionHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * 1
	 * 
	 * @param businessServiceName
	 *            - specifies the name of the existing BusinessService
	 *            Associated with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created. Please Note - If a Transaction is created as an
	 *            identifying Transaction, its cacheable flag cannot be set to
	 *            true and also isIncluded flag cannot be set to false. The
	 *            identifying transaction created using this method inherits SLA
	 *            values associated with its BusinssTransaction.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createIdentifyingTransactionWithInheritedSLAValues(
			String businessServiceName, String businessTransactionName,
			String transactionName, String transactionDescription)
			throws CEMWebServicesException, RemoteException {
		long transactionId = 0;
		transactionId = m_cemServices.getInternalService().createTransaction(
				businessServiceName, businessTransactionName, transactionName,
				transactionDescription, true, true, false, true, true, true,
				"", "", "");
		System.out
				.println("Creation of Identifying Transaction with Inherited SLA Values successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 2
	 * 
	 * @param businessServiceName
	 *            - specifies the name of the existing BusinessService
	 *            Associated with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isSigmaSlaInherited
	 *            - boolean true or false - if true ignores the value specified
	 * @param isSuccessRateSlaInherited
	 *            - boolean true or false -if true ignores the value specified
	 * @param isTransTimeInherited
	 *            - boolean true or false -if true ignores the value specified
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Value (in seconds - it is
	 *            converted to milliseconds by TESS) Please Note - If a
	 *            Transaction is created as an identifying Transaction, its
	 *            cacheable flag cannot be set to true and also isIncluded flag
	 *            cannot be set to false. The identifying transaction created
	 *            using this method inherits SLA values associated with its
	 *            BusinssTransaction.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createIdentifyingTransactionWithCustomSLAValues(
			String businessServiceName, String businessTransactionName,
			String transactionName, String transactionDescription,
			boolean isSuccessRateSlaInherited, boolean isSigmaSlaInherited,
			boolean isTransTimeInherited, String successRateSlaValue,
			String sigmaSlaValue, String tranTimeSlaValue)
			throws CEMWebServicesException, RemoteException {
		long transactionId = 0;
		transactionId = m_cemServices.getInternalService().createTransaction(
				businessServiceName, businessTransactionName, transactionName,
				transactionDescription, true, true, false,
				isSuccessRateSlaInherited, isSigmaSlaInherited,
				isTransTimeInherited, successRateSlaValue, sigmaSlaValue,
				tranTimeSlaValue);
		System.out
				.println("Creation of Identifying Transaction with Inherited SLA Values successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 3
	 * 
	 * @param businessServiceName
	 *            - specifies the name of the existing BusinessService
	 *            Associated with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the non-identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isIncluded
	 *            - Specifies whether the transaction should be marked as
	 *            Included. It should be either true or false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked as
	 *            Cacheable. It should be either true or false. Please Note -
	 *            There should be one identifying transaction in the BT before
	 *            creating any non -identifying transactions
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createNonIdentifyingTransactionWithInheritedSLAValues(
			String businessServiceName, String businessTransactionName,
			String transactionName, String transactionDescription,
			boolean isIncluded, boolean isCacheable)
			throws CEMWebServicesException, RemoteException {
		long transactionId;
		transactionId = m_cemServices.getInternalService().createTransaction(
				businessServiceName, businessTransactionName, transactionName,
				transactionDescription, false, isIncluded, isCacheable, true,
				true, true, "", "", "");
		System.out
				.println("Non Identifying Transaction with Inherited SLA Values creation successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 4
	 * 
	 * @param businessServiceName
	 *            - specifies the name of the existing BusinessService
	 *            Associated with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isIncluded
	 *            - Specifies whether the transaction should be marked as
	 *            Included. It should be either true or false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked as
	 *            Cacheable. It should be either true or false.
	 * @param isSigmaSlaInherited
	 *            - boolean true or false - if true ignores the value specified
	 * @param isSuccessRateSlaInherited
	 *            - boolean true or false -if true ignores the value specified
	 * @param isTransTimeInherited
	 *            - boolean true or false -if true ignores the value specified
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Value (in seconds - it is
	 *            converted to milliseconds by TESS) The non identifying
	 *            transaction created using this method inherits SLA values
	 *            associated with its BusinssTransaction. Please Note - There
	 *            should be one identifying transaction in the BT before
	 *            creating any non -identifying transactions
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createNonIdentifyingTransactionWithCustomSLAValues(
			String businessServiceName, String businessTransactionName,
			String transactionName, String transactionDescription,
			boolean isIncluded, boolean isCacheable,
			boolean isSuccessRateSlaInherited, boolean isSigmaSlaInherited,
			boolean isTransTimeInherited, String successRateSlaValue,
			String sigmaSlaValue, String tranTimeSlaValue)
			throws CEMWebServicesException, RemoteException {
		long transactionId;
		transactionId = m_cemServices.getInternalService().createTransaction(
				businessServiceName, businessTransactionName, transactionName,
				transactionDescription, false, isIncluded, isCacheable,
				isSuccessRateSlaInherited, isSigmaSlaInherited,
				isTransTimeInherited, successRateSlaValue, sigmaSlaValue,
				tranTimeSlaValue);
		System.out
				.println("Non Identifying Transaction with Custom SLA Values creation successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 5
	 * 
	 * @param businessTransactionId
	 *            - specifies the Id of the existing BusinessTransaction
	 *            Associated with the Transaction Business Transaction Database
	 *            Information - Business Transaction ID can be obtained from
	 *            ts_transets.ts_id field and Business Transaction Name:
	 *            ts_transets.ts_name
	 * @param transactionName
	 *            - specifies the name of the non identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created. Please Note - If a Transaction is created as an
	 *            identifying Transaction, its cacheable cannot be set to true
	 *            and also isIncluded flag cannot be set to false.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createIdentifyingTxnWithInheritedSLAValuesUsingBTxnId(
			long businessTransactionId, String transactionName,
			String transactionDescription) throws CEMWebServicesException,
			RemoteException {
		long transactionId = 0;
		transactionId = m_cemServices.getInternalService()
				.createTransactionById(businessTransactionId, transactionName,
						transactionDescription, true, true, false, true, true,
						true, "", "", "");
		System.out
				.println("Creating Identifying Transaction with inherited SLA Values  using Business Transaction Id successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 6
	 * 
	 * @param businessTransactionId
	 *            -specifies the Id of the existing BusinessTransaction
	 *            Associated with the Transaction Business Transaction Database
	 *            Information - Business Transaction ID can be obtained from
	 *            ts_transets.ts_id field and Business Transaction Name:
	 *            ts_transets.ts_name
	 * @param transactionName
	 *            - specifies the name of the non identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isSigmaSlaInherited
	 *            - boolean true or false - if true ignores the value specified
	 * @param isSuccessRateSlaInherited
	 *            boolean true or false -if true ignores the value specified
	 * @param isTransTimeInherited
	 *            boolean true or false -if true ignores the value specified
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Values (in seconds - it is
	 *            converted to milliseconds by TESS) Please Note - If a
	 *            Transaction is created as an identifying Transaction, its
	 *            cacheable cannot be set to true and also isIncluded flag
	 *            cannot be set to false.
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createIdentifyingTxnWithCustomSLAValuesUsingBTxnId(
			long businessTransactionId, String transactionName,
			String transactionDescription, boolean isSuccessRateSlaInherited,
			boolean isSigmaSlaInherited, boolean isTransTimeInherited,
			String successRateSlaValue, String sigmaSlaValue,
			String tranTimeSlaValue) throws CEMWebServicesException,
			RemoteException {
		long transactionId;
		transactionId = m_cemServices.getInternalService()
				.createTransactionById(businessTransactionId, transactionName,
						transactionDescription, true, true, false,
						isSuccessRateSlaInherited, isSigmaSlaInherited,
						isTransTimeInherited, successRateSlaValue,
						sigmaSlaValue, tranTimeSlaValue);
		System.out
				.println("Creating Identifying Transaction with Custom SLA Values using Business Transaction Id successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 7
	 * 
	 * @param businessTransactionId
	 *            -specifies the Id of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the non identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isIncluded
	 *            - Specifies whether the transaction should be marked as
	 *            Included. It should be either true or false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked as
	 *            Cacheable. It should be either true or false.
	 * @param isSigmaSlaInherited
	 *            - boolean true or false - if true ignores the value specified
	 * @param isSuccessRateSlaInherited
	 *            boolean true or false -if true ignores the value specified
	 * @param isTransTimeInherited
	 *            boolean true or false -if true ignores the value specified
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Value (in seconds - it is
	 *            converted to milliseconds by TESS) Please Note - There should
	 *            be one identifying transaction in the BT before creating any
	 *            non -identifying transactions
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createNonIdentifyingTxnWithCustomSLAValuesUsingBTxnId(
			long businessTransactionId, String transactionName,
			String transactionDescription, boolean isIncluded,
			boolean isCacheable, boolean isSuccessRateSlaInherited,
			boolean isSigmaSlaInherited, boolean isTransTimeInherited,
			String successRateSlaValue, String sigmaSlaValue,
			String tranTimeSlaValue) throws CEMWebServicesException,
			RemoteException {
		long transactionId = 0;
		transactionId = m_cemServices.getInternalService()
				.createTransactionById(businessTransactionId, transactionName,
						transactionDescription, false, isIncluded, isCacheable,
						isSuccessRateSlaInherited, isSigmaSlaInherited,
						isTransTimeInherited, successRateSlaValue,
						sigmaSlaValue, tranTimeSlaValue);
		System.out
				.println("Creating Non identifying Transaction with Custom SLA Values using Business Transaction Id successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * 8
	 * 
	 * @param businessTransactionId
	 *            - specifies the Id of the existing BusinessTransaction
	 *            Associated with the Transaction
	 * @param transactionName
	 *            - specifies the name of the non identifying transaction to be
	 *            created.
	 * @param transactionDescription
	 *            - specifies the description of the transaction that is
	 *            created.
	 * @param isIncluded
	 *            - Specifies whether the transaction should be marked as
	 *            Included. It should be either true or false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked as
	 *            Cacheable. It should be either true or false. Please Note -
	 *            There should be one identifying transaction in the BT before
	 *            creating any non -identifying transactions
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public long createNonIdentifyingTxnWithInheritedSLAValuesUsingBTxnId(
			long businessTransactionId, String transactionName,
			String transactionDescription, boolean isIncluded,
			boolean isCacheable) throws CEMWebServicesException,
			RemoteException {
		long transactionId;
		transactionId = m_cemServices.getInternalService()
				.createTransactionById(businessTransactionId, transactionName,
						transactionDescription, false, isIncluded, isCacheable,
						true, true, true, "", "", "");
		System.out
				.println("Creating Non Identifying Transaction with Inherited SLA Values  using Business Transaction Id successful : "
						+ transactionName);
		return transactionId;
	}

	/**
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing name of the transaction to be updated
	 * @param newTransactionName
	 *            - specifes the new (updated) name of the transaction
	 * @param transactionDescription
	 *            - specifies the new (updated) description of the transaction
	 * @param isIdentifying
	 *            - specifies the updated status of the transaction marking it
	 *            as Identifying (true) or Non-Identifying (false) transaction
	 *            param isIncluded- Specifies whether the transaction should be
	 *            marked (updated) as Included. It should be either true or
	 *            false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked (updated)
	 *            as Cacheable. It should be either true or false.
	 * @param isSuccessRateSlaInherited
	 *            - specifies if Success Rate SLA Value is inherited (true) or
	 *            custome values (false)
	 * @param isSigmaSlaInherited
	 *            -specifies if the Sigma SLA Value is inherited (true) or
	 *            custome values (false)
	 * @param isTransTimeInherited
	 *            - specifies if the custom TranTime SLA Value is inherited
	 *            (true) or custome values (false)
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value)
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Value (in seconds - it is
	 *            converted to milliseconds by TESS)
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateTransaction(String businessServiceName,
			String businessTransactionName, String transactionName,
			String newTransactionName, String transactionDescription,
			boolean isIdentifying, boolean isIncluded, boolean isCacheable,
			boolean isSuccessRateSlaInherited, boolean isSigmaSlaInherited,
			boolean isTransTimeInherited, String successRateSlaValue,
			String sigmaSlaValue, String tranTimeSlaValue)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateTransaction(
				businessServiceName, businessTransactionName, transactionName,
				newTransactionName, transactionDescription, isIdentifying,
				isIncluded, isCacheable, isSuccessRateSlaInherited,
				isSigmaSlaInherited, isTransTimeInherited, successRateSlaValue,
				sigmaSlaValue, tranTimeSlaValue);
		System.out
				.println("Update Transaction successful : " + transactionName);

	}

	/**
	 * @param transactionId
	 *            - specifies the transaction Id of the Transaction that has to
	 *            be updated. The transaction ID in Database can be obtained
	 *            from ts_tranunits.ts_id and transaction name can be obtained
	 *            from ts_transets.ts_name
	 * @param newTransactionName
	 *            - specifies the new (updated) name of the transaction
	 * @param transactionDescription
	 *            - specifies the new (updated) description of the transaction
	 * @param isIdentifying
	 *            - specifies the updated status of the transaction marking it
	 *            as Identifying (true) or Non-Identifying (false) transaction
	 *            param isIncluded- Specifies whether the transaction should be
	 *            marked (updated) as Included. It should be either true or
	 *            false.
	 * @param isCacheable
	 *            - Specifies whether the transaction should be marked (updated)
	 *            as Cacheable. It should be either true or false.
	 * @param isSuccessRateSlaInherited
	 *            - specifies if Success Rate SLA Value is inherited (true) or
	 *            custome values (false)
	 * @param isSigmaSlaInherited
	 *            -specifies if the Sigma SLA Value is inherited (true) or
	 *            custome values (false)
	 * @param isTransTimeInherited
	 *            - specifies if the custom TranTime SLA Value is inherited
	 *            (true) or custome values (false)
	 * @param successRateSlaValue
	 *            - specifies the custom Success Rate SLA Value)
	 * @param sigmaSlaValue
	 *            -specifies the custom Sigma SLA Value
	 * @param tranTimeSlaValue
	 *            -specifies the custom TranTime SLA Value (in seconds - it is
	 *            converted to milliseconds by TESS)
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateTransactionUsingTransactionId(long transactionId,
			String newTransactionName, String transactionDescription,
			boolean isIdentifying, boolean isIncluded, boolean isCacheable,
			boolean isSuccessRateSlaInherited, boolean isSigmaSlaInherited,
			boolean isTransTimeInherited, String successRateSlaValue,
			String sigmaSlaValue, String tranTimeSlaValue)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateTransactionById(transactionId,
				newTransactionName, transactionDescription, isIdentifying,
				isIncluded, isCacheable, isSuccessRateSlaInherited,
				isSigmaSlaInherited, isTransTimeInherited, successRateSlaValue,
				sigmaSlaValue, tranTimeSlaValue);
		System.out
				.println("Update Transaction using Transaction Id successful : "
						+ newTransactionName);

	}

	/**
	 * 
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing name of the transaction to be updated
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public void deleteTransaction(String businessServiceName,
			String businessTransactionName, String transactionName)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteTransaction(
				businessServiceName, businessTransactionName, transactionName);
		System.out.println("Transaction deletion successful : "
				+ transactionName);
	}

	/**
	 * @param transactionId
	 *            - specifies the transaction Id of the Transaction that has to
	 *            be updated. The transaction ID in Database can be obtained
	 *            from ts_tranunits.ts_id and transaction name can be obtained
	 *            from ts_transets.ts_name
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void deleteTransactionById(long transactionId)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().deleteTransactionById(transactionId);
		System.out
				.println("Delete Transaction using Transaction Id successful : "
						+ transactionId);

	}

	/**
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing name of the transaction
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @param setNewName
	 *            - specifies the new defect name to be updated
	 * @param setEnabled
	 *            - specifies if the defect specification has to be enabled
	 *            (true) or disabled (false)
	 * @param setLocked
	 *            - specifies if the defect specification has to be locked
	 *            (true) or not locked (false)
	 * @param conditionValue
	 *            - specifies the threshold value associated with the defect
	 *            specification
	 * @param importance
	 *            - specifies the importance and should be one of the following
	 *            - Ignore, Minimum, Very Low, Low, Trigger Immediately, High,
	 *            Medium (Default), Critical
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateTransactionSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName, String setNewName, boolean setEnabled,
			boolean setLocked, String conditionValue, String importance)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateTransactionSpecification(
				businessServiceName, businessTransactionName, transactionName,
				defectName, setNewName, setEnabled, setLocked, conditionValue,
				importance);
		System.out.println("Update Transaction  Specification successful : "
				+ transactionName);

	}

	/**
	 * @param transactionId
	 *            - specifies the transaction Id of the Transaction that has to
	 *            be updated. The transaction ID in Database can be obtained
	 *            from ts_tranunits.ts_id and transaction name can be obtained
	 *            from ts_transets.ts_name
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @param setNewName
	 *            - specifies the new defect name to be updated
	 * @param setEnabled
	 *            - specifies if the defect specification has to be enabled
	 *            (true) or disabled (false)
	 * @param setLocked
	 *            - specifies if the defect specification has to be locked
	 *            (true) or not locked (false)
	 * @param conditionValue
	 *            - specifies the threshold value associated with the defect
	 *            specification
	 * @param importance
	 *            - specifies the importance and should be one of the following
	 *            - Ignore, Minimum, Very Low, Low, Trigger Immediately, High,
	 *            Medium (Default), Critical
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void updateTransactionSpecificationById(long transactionId,
			String defectName, String newName, boolean setEnabled,
			boolean setLocked, String conditionValue, String importance)
			throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().updateTransactionSpecificationById(
				transactionId, defectName, newName, setEnabled, setLocked,
				conditionValue, importance);
		System.out
				.println("Update Transaction Specification using Transaction Id successful : "
						+ transactionId);
	}

	/**
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing of the transaction
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void enableTransactionSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableTransactionSpecification(
				businessServiceName, businessTransactionName, transactionName,
				defectName);
		System.out.println("Enable Transaction  Specification successful : "
				+ transactionName);

	}

	/**
	 * @param transactionId
	 *            - specifies the transaction Id of the Transaction that has to
	 *            be updated. The transaction ID in Database can be obtained
	 *            from ts_tranunits.ts_id and transaction name can be obtained
	 *            from ts_transets.ts_name
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 * 
	 */
	public void enableTransactionSpecificationById(long transactionId,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().enableTransactionSpecificationById(
				transactionId, defectName);
		System.out
				.println("Enable Transaction Specification using Transaction Id successful : "
						+ transactionId);
	}

	/**
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing of the transaction
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */

	public void disableTransactionSpecification(String businessServiceName,
			String businessTransactionName, String transactionName,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().disableTransactionSpecification(
				businessServiceName, businessTransactionName, transactionName,
				defectName);
		System.out
				.println("Transaction specifications disabled successfully : "
						+ transactionName);
	}

	/**
	 * @param transactionId
	 *            - specifies the transaction Id of the Transaction that has to
	 *            be updated. The transaction ID in Database can be obtained
	 *            from ts_tranunits.ts_id and transaction name can be obtained
	 *            from ts_transets.ts_name
	 * @param defectName
	 *            - specifies the defect name to enable and should be one of the
	 *            following - Slow Time, Fast Time, High Throughput, Low
	 *            Throughput, Large Size, Small Size
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public void disableTransactionSpecificationById(long transactionId,
			String defectName) throws CEMWebServicesException, RemoteException {

		m_cemServices.getInternalService().disableTransactionSpecificationById(
				transactionId, defectName);
		System.out
				.println("Disabling Transaction Specification using Transaction Id successful : "
						+ transactionId);

	}

	/**
	 * 
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService Associated
	 *            with the Transaction
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction associated with
	 *            the Transaction
	 * @param transactionName
	 *            - specifies the existing name of the transaction that needs to
	 *            be returned
	 * @return -TransactionDefinition object
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public TransactionDefinition getTransaction(String businessServiceName,
			String businessTransactionName, String transactionName)
			throws CEMWebServicesException, RemoteException {
		TransactionDefinition transaction = m_cemServices
				.getConfigurationDataOutService().getTransactionByName(
						businessServiceName, businessTransactionName,
						transactionName);
		System.out
				.println(" Get Transaction Information using Transaction name is successful : "
						+ transaction.getName());

		return transaction;

	}

	/**
	 * 
	 * @param businessServiceName
	 *            -specifies the name of the existing BusinessService
	 * @param businessTransactionName
	 *            -specifies the name of the BusinessTransaction
	 * @return - All the Transactions associated with the specifed Business
	 *         Transaction in the specified Business Service
	 * @throws RemoteException
	 * @throws CEMWebServicesException
	 */
	public TransactionDefinition[] getAllTransactionsInBusinessTransaction(
			String businessServiceName, String businessTransactionName)
			throws CEMWebServicesException, RemoteException {

		TransactionDefinition[] setOfTransactions = m_cemServices
				.getConfigurationDataOutService()
				.getAllTransactionsByBusinessTransaction(businessServiceName,
						businessTransactionName);

		System.out
				.println(" Get All Transaction Information using Business Transaction name is successful : "
						+ businessTransactionName);

		return setOfTransactions;
	}
	

	/**
	 * 
	 * Returns an identifying transaction in specified business service and in specified business transaction
	 * 
	 *            - Name of the business Service
	 * @param businessTransactionName
	 *            - Name of the business Transaction
	 * @return - Returns Transaction definition object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public TransactionDefinition getIdentifyingTxDefInBusinessService(String businessServiceName, String businessTxName) throws CEMWebServicesException, RemoteException {
		TransactionDefinition[] transactions = getAllTransactionsInBusinessTransaction(businessServiceName, businessTxName);
		for (TransactionDefinition transactionDefinition : transactions) {
			if( transactionDefinition.getIdentifying() ){
				return transactionDefinition;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param businessServiceName
	 *            - Name of the business Service
	 * @param businessTransactionName
	 *            - Name of the business Transaction
	 * @return - Returns Transaction definition object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public TransactionDefinition getIdentifyingTransactionInBT(
			String businessServiceName, String businessTransactionName)
			throws CEMWebServicesException, RemoteException {
		TransactionDefinition transaction = null;
		TransactionDefinition[] setOfTransactions = m_cemServices
				.getConfigurationDataOutService()
				.getAllTransactionsByBusinessTransaction(businessServiceName,
						businessTransactionName);
		System.out.println("Fetching Identifying Transctions in BT: "
				+ businessTransactionName + " from BS: " + businessServiceName);

		for (TransactionDefinition ss : setOfTransactions) {

			if (ss.getIdentifying() == true) {
				transaction = ss;
				System.out.println("Identifying Tx is : "
						+ transaction.getName());
				break;
			}

		}

		return transaction;

	}

}
