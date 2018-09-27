package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.DefectRS;

import java.rmi.RemoteException;

public class DefectsHelper {
	protected CEMServices m_cemServices;

	public DefectsHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * 
	 * @param applicationId
	 * @param strDefectType
	 * @param startTime
	 * @param endTime
	 * @param metaKeys
	 * @param includeComponentTimingInfo
	 * @param nextStartIndex
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public DefectRS getDefectsByTypeAndByBusinessApplication(
			long applicationId, java.lang.String strDefectType,
			java.util.Calendar startTime, java.util.Calendar endTime,
			java.lang.String[] metaKeys, boolean includeComponentTimingInfo,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsByType_BA = m_cemServices.getEventsDataOutService()
				.getDefectsByTypeAndByBusinessApplication(applicationId,
						strDefectType, startTime, endTime, metaKeys,
						includeComponentTimingInfo, nextStartIndex);

		return defectsByType_BA;

	}

	/**
	 * 
	 * @param businessProcessId
	 * @param strDefectType
	 * @param startTime
	 * @param endTime
	 * @param metaKeys
	 * @param includeComponentTimingInfo
	 * @param nextStartIndex
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public DefectRS getDefectsByTypeAndByBusinessProcess(
			long businessProcessId, String strDefectType,
			java.util.Calendar startTime, java.util.Calendar endTime,
			java.lang.String[] metaKeys, boolean includeComponentTimingInfo,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsByType_BS = m_cemServices.getEventsDataOutService()
				.getDefectsByTypeAndByBusinessProcess(businessProcessId,
						strDefectType, startTime, endTime, metaKeys,
						includeComponentTimingInfo, nextStartIndex);

		return defectsByType_BS;
	}

	/**
	 * 
	 * @param businessTransactionId
	 * @param strDefectType
	 * @param startTime
	 * @param endTime
	 * @param metaKeys
	 * @param includeComponentTimingInfo
	 * @param nextStartIndex
	 * @return
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public DefectRS getDefectsByTypeAndByBusinessTransaction(
			long businessTransactionId, java.lang.String strDefectType,
			java.util.Calendar startTime, java.util.Calendar endTime,
			java.lang.String[] metaKeys, boolean includeComponentTimingInfo,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsByType_BT = m_cemServices.getEventsDataOutService()
				.getDefectsByTypeAndByBusinessTransaction(
						businessTransactionId, strDefectType, startTime,
						endTime, metaKeys, includeComponentTimingInfo,
						nextStartIndex);
		return defectsByType_BT;
		// http://sqw3vwls005:8081/wily/cem/webservices/EventsDataOutService?method=getDefectsByTypeAndByBusinessTransaction&businessTransactionId=700000000000000000&strDefectType=&startTime=2011-01-23T21:00:37-05:00&endTime=2011-03-28T12:04:03-05:00&metaKeys=&includeComponentTimingInfo=false&nextStartIndex=0
	}

	/**
	 * 
	 * @param applicationId
	 *            - - specifies the business App Id (long)
	 * @param startTime
	 *            - specifies the start time (Calendar Object)
	 * @param endTime
	 *            - specifies the start end (Calendar Object)
	 * @param nextStartIndex
	 *            - The number defects returned. For instance setting this value
	 *            to 0 will return the top 500 defects
	 * @return -DefectRS Object contains the defect type value in INTEGER
	 *         FORMAT. To get the actual Defect Name Associated with a code
	 *         String defect_slowtimeValue =
	 *         BaseProperties.DefectType.get(1).name(); TO GET THE DEFECT CODE
	 *         ASSOCIATED WITH A DEFECT NAME int defect_slowtime =
	 *         BaseProperties.DefectType.SLOW_TIME.getCode();
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public DefectRS getDefectsByApplication(long applicationId,
			java.util.Calendar startTime, java.util.Calendar endTime,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsbyApplication = m_cemServices.getEventsDataOutService()
				.getDefectsByApplication(applicationId, startTime, endTime,
						nextStartIndex);
		return defectsbyApplication;
		// http://sqw3vwls005:8081/wily/cem/webservices/EventsDataOutService?method=getDefectsByApplication&applicationId=700000000000000000&startTime=2011-01-23T21:00:37-05:00&endTime=2011-03-28T12:04:03-05:00&nextStartIndex=0
	}

	/**
	 * 
	 * @param businessProcessId
	 *            - specifies the business Process Id (long)
	 * @param startTime
	 *            - specifies the start time (Calendar Object)
	 * @param endTime
	 *            - specifies the start end (Calendar Object)
	 * @param nextStartIndex
	 *            - The number defects returned. For instance setting this value
	 *            to 0 will return the top 500 defects
	 * @return - DefectRS Object contains the defect type value in INTEGER
	 *         FORMAT. To get the actual Defect Name Associated with a code
	 *         String defect_slowtimeValue =
	 *         BaseProperties.DefectType.get(1).name(); TO GET THE DEFECT CODE
	 *         ASSOCIATED WITH A DEFECT NAME int defect_slowtime =
	 *         BaseProperties.DefectType.SLOW_TIME.getCode();
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public DefectRS getDefectsByBusinessProcess(long businessProcessId,
			java.util.Calendar startTime, java.util.Calendar endTime,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsbyBP = m_cemServices.getEventsDataOutService()
				.getDefectsByBusinessProcess(businessProcessId, startTime,
						endTime, nextStartIndex);
		return defectsbyBP;
		// http://sqw3vwls005:8081/wily/cem/webservices/EventsDataOutService?method=getDefectsByBusinessProcess&businessProcessId=700000000000000001&startTime=2011-01-23T21:00:37-05:00&endTime=2011-03-28T12:04:03-05:00&nextStartIndex=0
	}

	/**
	 * 
	 * @param businessTransactionId
	 *            -specifies the business Trnx Id (long)
	 * @param startTime
	 *            - specifies the start time (Calendar Object)
	 * @param endTime
	 *            - specifies the start end (Calendar Object)
	 * @param nextStartIndex
	 *            - - The number defects returned. For instance setting this
	 *            value to 0 will return the top 500 defects
	 * @return-DefectRS Object contains the defect type value in INTEGER FORMAT.
	 *                  To get the actual Defect Name Associated with a code
	 *                  String defect_slowtimeValue =
	 *                  BaseProperties.DefectType.get(1).name(); TO GET THE
	 *                  DEFECT CODE ASSOCIATED WITH A DEFECT NAME int
	 *                  defect_slowtime =
	 *                  BaseProperties.DefectType.SLOW_TIME.getCode();
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public DefectRS getDefectsByBusinessTransaction(long businessTransactionId,
			java.util.Calendar startTime, java.util.Calendar endTime,
			int nextStartIndex) throws CEMWebServicesException, RemoteException {

		DefectRS defectsbyTransaction = m_cemServices.getEventsDataOutService()
				.getDefectsByBusinessTransaction(businessTransactionId,
						startTime, endTime, nextStartIndex);

		return defectsbyTransaction;
		// http://sqw3vwls005:8081/wily/cem/webservices/EventsDataOutService?method=getDefectsByBusinessTransaction&businessTransactionId=700000000000000000&startTime=2011-01-23T21:00:37-05:00&endTime=2011-03-28T12:04:03-05:00&nextStartIndex=0

	}
}
