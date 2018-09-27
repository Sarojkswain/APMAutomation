package com.ca.tas.flow.tess;

import java.io.IOException;
import java.util.List;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.tas.flow.tess.TessUI.TessServiceConfig;
import com.ca.tas.flow.tess.TessUI.TimConfig;
import com.ca.tas.flow.tess.TessUI.WebServerFilterConfig;
import com.ca.tas.flow.tess.reports.TessReportConfiguration;



/**
 * 
 * @author keyja01
 *
 */
public interface TessConfigurer {

	/**
	 * Agent, TIM record types enum.
	 * 
	 * @author Alexander Sinyushkin (sinal04@ca.com)
	 *
	 */
	public enum RecordType {
    	Agent("selectedMonitorTypes1"), TIM("selectedMonitorTypes2");
    	
    	private String htmlRecordTypeRadioButtonId;
    	
    	private RecordType(String id) {
    		htmlRecordTypeRadioButtonId = id;
    	}
    	
    	public String getHtmlRecordTypeRadioButtonId() {
    		return htmlRecordTypeRadioButtonId;
    	}
    	
    	public static RecordType fromString(String type) {
    		if (type == null) {
    			throw new NullPointerException("Record type string must not be null!");
    		}
    		
    		type = type.toLowerCase();
    		if (type.equals(Agent.name().toLowerCase())) {
    			return Agent;
    		} else if (type.equals(TIM.name().toLowerCase())) {
    			return TIM;
    		}
    		throw new IllegalArgumentException("Unknown record type string provided!");
    	}
    }

	
    /**
     * Closes any open resources (selenium driver, etc) associated with this {@link TessConfigurer}
     */
    public void close();

    /**
     * Log in to the TESS UI
     */
    public void login();

    /**
     * Attempts to enable the specified TIM
     * @param tim
     * @throws IOException 
     */
    public void enableTim(String tim) throws IOException;

    /**
     * Adds a new tim to the list of configured monitors
     * @param name
     * @param ipAddress
     * @param enableMTP
     * @throws IOException 
     */
    public void configureNewTim(String name, boolean enableMTP) throws IOException;

    /**
     * Read the Tess Service configuration from the UI
     * @return
     */
    public List<TessServiceConfig> readTessServiceConfigs();

    /**
     * Configures a particular TESS service to run on the specified EM.
     * @param svc
     * @param emHost
     */
    public void configureTessService(TessService svc, String emHost) throws IOException;

    /**
     * Configures a new Web Server Filter on the TESS
     * @param cfg
     */
    public void configureWebServerFilter(WebServerFilterConfig cfg);

    /**
     * Configures RTTM configuration on the TESS
     */
    public void configureRttm();

    /**
     * Reads the list of Web Server Filter configured on the TESS
     * @return
     */
    public List<WebServerFilterConfig> readWebServerFilterConfigs();

    /**
     * Deletes all of the currently configured TIMs.  If the TIMs still have any assigned Web Server Filters, 
     * it will not be possible to delete those. [Hint: call deleteAllWebServerFilters() first]
     */
    public void deleteAllTims();

    /**
     * Returns a list of the configured TIMs
     * @return
     */
    public List<TimConfig> readTimConfigs();

    /**
     * Deletes all of the currently configured Web Server Filters.  If none are configured,
     * implementations should just return immediately.
     */
    public void deleteAllWebServerFilters();

    /**
     * Starts a new recording session of the specified <code>type</code> and using the provided
     * <code>clientIP</code>.  
     * 
     * @param clientIP      load client IP address
     * @param type          session type
     * @return              window id
     */
    public String startRecordingSession(String clientIP, RecordType type);
    
    /**
     * Stops the currently running recording session.
     * 
     * @param windowId     window id for Selenium
     */
    public void stopRecordingSession(String windowId);
    
    /**
     * Returns a list of the configured user reports
     * @return
     */
    public List<TessReportConfiguration> listReports();
    
    /**
     * Deletes all of the scheduled reports
     */
    public void deleteAllReports();
    
    /**
     * Configures a new report on the CEM UI
     * @param report
     */
    public void configureReport(TessReportConfiguration report);
    
    /**
     * Configures SMTP host.
     * 
     * @param hostname host name
     */
    public void configureSMTP(String hostname);
    
    /**
     * Reads and returns existing SMTP host setting.
     * @return
     */
    public String getSMTPHost();
    
}
