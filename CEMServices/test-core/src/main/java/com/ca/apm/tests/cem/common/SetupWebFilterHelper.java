package com.ca.apm.tests.cem.common;

import com.ca.wily.cem.qa.api.CEMWebServicesException;
import com.ca.wily.cem.qa.api.WebServerDefinition;

import java.rmi.RemoteException;

public class SetupWebFilterHelper {
	protected CEMServices m_cemServices;

	public SetupWebFilterHelper(CEMServices cem) {
		m_cemServices = cem;
	}

	/**
	 * Creates webserver filter with Address type IP Address
	 * 
	 * @param webFilterName
	 *            - name of the Webserver filter
	 * @param monitorName
	 *            - name of the monitor
	 * @param fromIPAddress
	 *            - from IP Address
	 * @param toIPAddress
	 *            - to IP Address
	 * @param port
	 *            - port number
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void createIPWebServerFilter(String webFilterName,
			String monitorName, String fromIPAddress, String toIPAddress,
			int port,boolean allowOverlap) throws CEMWebServicesException, RemoteException {
//TODO: Clarify with Ravi about the booolean flag
			//boolean allowOverlap=true;
			WebServerDefinition[] wsd=m_cemServices.getConfigurationDataOutService().getWebserverFilters();
	        if(!(wsd==null))
	        for(WebServerDefinition w:wsd){
	            System.out.println("Webserver Filter name  "+w.getName());
	            if(w.getName().equals(webFilterName)){
	                m_cemServices.getInternalService().deleteWebServerFilter(w.getName());
	                break;
	            }	                
	        }
			m_cemServices.getInternalService()
					.createIPWebServerFilter(webFilterName, monitorName,
							fromIPAddress, toIPAddress, port, allowOverlap);
			System.out.println("WebServer Filter  " + webFilterName
					+ "  created successfully");
		}
	

	/**
	 * Creates webserver filter with Address type MAC Address
	 * 
	 * @param webFilterName
	 *            - name of the Webserver filter
	 * @param monitorName
	 *            - name of the monitor
	 * @param macAddress
	 *            - mac Address
	 * @throws RemoteException 
	 * @throws CEMWebServicesException 
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void createMACWebServerFilter(String webFilterName,
			String monitorName, String macAddress) throws CEMWebServicesException, RemoteException {

			m_cemServices.getInternalService()
					.createMACWebServerFilter(webFilterName, monitorName,
							macAddress);
			System.out.println("MACWebServer Filter  " + webFilterName
					+ "  created successfully");
		
	}

	/**
	 * Deletes webserver filter using webserver filter name
	 * 
	 * @param webFilterName
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void deleteWebServerFilter(String webFilterName)
			throws CEMWebServicesException, RemoteException {
		m_cemServices.getInternalService().deleteWebServerFilter(webFilterName);
	}
	
	/**
	 * 
	 * Return the array of all WebServer Filters in the system
	 * 
	 * @return Returns an array of WebServerDefinition objects
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public WebServerDefinition[] getAllWebServerFilters() throws CEMWebServicesException, RemoteException {
	
		return m_cemServices.getConfigurationDataOutService().getWebserverFilters();
	}
	
	/**
	 * 
	 * Deletes the webserver filter using webserver filter Id
	 * 
	 * @param webServerFilterId
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public void deleteWebServerFilterById(long webServerFilterId) throws CEMWebServicesException, RemoteException {
		
		m_cemServices.getInternalService().deleteWebServerFilterById(webServerFilterId);
	}
	
	/**
	 * 
	 * Returns the IP Web server filter matching the name and fromIP address, to IP address and port
	 * 
	 * @param filterName
	 * @param fromIpAddress
	 * @param toIPaddress
	 * @param port
	 * @return Returns the WebServerFilterDefinition object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public WebServerDefinition getIPWebServerFilter(String filterName, String fromIpAddress, String toIPaddress, String port) throws CEMWebServicesException, RemoteException {
	
		//WebServerDefinition macWebFilter = new WebServerDefinition() ;
		WebServerDefinition[] allWebServerFilters=  m_cemServices.getConfigurationDataOutService().getWebserverFilters();
		for (WebServerDefinition webServerDefinition : allWebServerFilters) {
			if (filterName.equals(webServerDefinition.getName())){
				if(fromIpAddress.equals(webServerDefinition.getFromIPAddress())){
					if(toIPaddress.equals(webServerDefinition.getToIpAddress())){
						if(port.equals(webServerDefinition.getPort())){
							return webServerDefinition;
						}
					}
				
				}
					
			}
		}
		return null;
	}
	
	/**
	 * 
	 * Returns the MAC Web server filter matching the name and mac address
	 * 
	 * @param filterName
	 * @param macAddress
	 * @return return the WebServerfilterDefinition object
	 * @throws CEMWebServicesException
	 * @throws RemoteException
	 */
	public WebServerDefinition getMacWebServerFilter(String filterName, String macAddress) throws CEMWebServicesException, RemoteException {
	
		//WebServerDefinition macWebFilter = new WebServerDefinition() ;
		WebServerDefinition[] allWebServerFilters=  m_cemServices.getConfigurationDataOutService().getWebserverFilters();
		for (WebServerDefinition webServerDefinition : allWebServerFilters) {
			if (filterName.equals(webServerDefinition.getName())){
				if(macAddress.equalsIgnoreCase(webServerDefinition.getMacAddress())){
					return webServerDefinition;
				}
					
			}
		}
		return null;
	}
}

