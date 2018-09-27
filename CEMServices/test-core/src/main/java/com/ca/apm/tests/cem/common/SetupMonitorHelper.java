package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.Monitor;
import com.ca.wily.cem.qa.api.WebServerDefinition;

import java.rmi.RemoteException;

public class SetupMonitorHelper {
	
	SetupWebFilterHelper setupWebFilter;

	protected CEMServices m_cemServices;

	/**
	 * @param a_cemServices
	 *            handle for all available webservices.
	 */
	public SetupMonitorHelper(CEMServices a_cemServices) {
		m_cemServices = a_cemServices;
	}

	/**
	 * Creates Monitor using the name and ipAddress of the TIM
	 * 
	 * @param monitorName
	 *            - name for the new monitor
	 * @param ipAddress
	 *            - ipAddress of the TIM machine
	 * @param timCollectorEM
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: Passing the name or ipAddress which already exists
	 *             throws an error
	 */
	public long createMonitor(String monitorName, String ipAddress,
			String timCollectorEM) throws CEMWebServicesException,
			RemoteException {
		long monitorId = 0;
		Monitor[] monitor=getMonitors();
		for(Monitor m:monitor)
		if((m.getName().equals(monitorName)) || (m.getIpAddressAsString().equals(ipAddress)))
		{
		    disableMonitor(m.getName());
			deleteMonitor(m.getName());
		}
		monitorId = m_cemServices.getInternalService().createMonitor(
				monitorName, ipAddress, timCollectorEM);
		System.out.println("Monitor " + monitorName + " Created Successfully");
		return monitorId;
	}
	/**
	 * Enables monitor using the monitor name
	 * 
	 * @param monitorName
	 *            - monitor name to enable
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: Enabling the enabled monitor does not throw any error
	 */
	public void enableMonitor(String monitorName)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().enableMonitor(monitorName);
		System.out.println("Monitor " + monitorName + " Enabled Successfully");

	}

	/**
	 * Disables monitor using the monitor name
	 * 
	 * @param monitorName
	 *            - monitor name to disable
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: Disabling the disabled monitor does not throw any error
	 */

	public void disableMonitor(String monitorName)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().disableMonitor(monitorName);
		System.out.println("Monitor " + monitorName + " Disabled Successfully");
	}

	/**
	 * Updates the existing monitor name with new one
	 * 
	 * @param monitorName
	 *            - existing monitor name
	 * @param newName
	 *            - new name for the monitor
	 * @param ipAddress
	 *            - ipAddress of the existing monitor
	 * @param timCollectorEM
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: TIM ipAddress cannot be changed when the monitors are
	 *             enabled, if you try to do so it will throw an error
	 * 
	 */

	public void updateMonitorwhenenabled(String monitorName, String newName,
			String ipAddress, String timCollectorEM)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().updateMonitor(monitorName, newName,
				ipAddress, timCollectorEM);
		System.out.println("Monitor " + monitorName + " Updated Successfully");

	}

	/**
	 * Update the existing monitor name and ipAddress with new name and TIM
	 * ipAddress
	 * 
	 * @param monitorName
	 *            - existing monitor name
	 * @param newName
	 *            - new name for the monitor
	 * @param ipAddress
	 *            - ipAddress of the new TIM machine
	 * @param timCollectorEM
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: TIM ipAddress can be changed only when the monitors are
	 *             disabled, if you try to do for enabled monitor it will throw
	 *             an error
	 */

	public void updateMonitorwhendisabled(String monitorName, String newName,
			String ipAddress, String timCollectorEM)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().updateMonitor(monitorName, newName,
				ipAddress, timCollectorEM);
		System.out.println("Monitor " + monitorName + " Updated Successfully");

	}

	/**
	 * Deletes the monitor using monitor name
	 * 
	 * @param monitorName
	 *            - monitor name to delete
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 * 
	 *             NOTE: If you try to delete an enable monitor it will throw an
	 *             error
	 * 
	 */

	public void deleteMonitor(String monitorName)
			throws CEMWebServicesException, RemoteException {
		
		Monitor[] monitor=getMonitors();
        if(monitor!=null)
		for(Monitor m:monitor)
        if(m.getName().equals(monitorName)){
            WebServerDefinition[] wsd=m_cemServices.getConfigurationDataOutService().getWebserverFilters();
            if(!(wsd==null))
            for(WebServerDefinition w:wsd)
                if(w.getMonitor().equals(monitorName))
                    m_cemServices.getInternalService().deleteWebServerFilter(w.getName());              
            disableMonitor(monitorName);
            m_cemServices.getInternalService().deleteMonitor(monitorName);
            System.out.println("Monitor Deleted Successfully");
        }		

	}

	/**
	 * Returns the array of all Monitors
	 * 
	 * @return - list of monitors
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public Monitor[] getMonitors() throws CEMWebServicesException,
			RemoteException {

		return m_cemServices.getConfigurationDataOutService().getMonitors();
	    
	    

	}

	/**
	 * Returns the Monitor object using Monitor name
	 * 
	 * @param name
	 *            - monitor name
	 * @return - monitor object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public Monitor getMonitorByName(String name)
			throws CEMWebServicesException, RemoteException {
		return m_cemServices.getConfigurationDataOutService().getMonitorByName(
				name);

	}

	/**
	 * Returns the Monitor object using Monitor id
	 * 
	 * @param id
	 *            - monitor id
	 * @return - monitor object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public Monitor getMonitorById(long id) throws CEMWebServicesException,
			RemoteException {
		return m_cemServices.getConfigurationDataOutService()
				.getMonitorById(id);

	}

	/**
	 * 
	 * @return - Returns int. 0--> Synchronization of atleast one enabled
	 *         monitor failed 1--> Synchronization Successful 2-->There are no
	 *         enabled monitors
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */

	public int syncMonitors() throws CEMWebServicesException, RemoteException {
		int status = m_cemServices.getInternalService().syncMonitors();
		if (status == 0) {
			System.out
					.println("Synchronization of atleast one enabled monitor failed ");
		}
		if (status == 1) {
			System.out.println("Synchronization Successful");
		}
		if (status == -1) {
			System.out.println("There are no enabled monitors ");
		}
		return status;
	}
	
	public boolean isMonitorAvailable(String monitorName) throws CEMWebServicesException, RemoteException {
		Monitor monitor=getMonitorByName(monitorName);
		if(monitor==null)
			return false;
		if(monitor.getName().equalsIgnoreCase(monitorName))
			return true;
		else
			return false;
		
	}
}
