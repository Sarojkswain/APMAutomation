import com.ca.wurlitzer.api.util.APMConnectionInfo;

import util.*


/**
  Extend the SystemConfiguration class to create a custom configuration.
*/
public class johann_config extends SystemConfiguration {
	
	// Build the custom configuration
	public configure() {
		def wurlitzerApp = new APMConnectionInfo(new URL("http://127.0.0.1:8080/wurlitzer-noejb"))
		
		addWurlitzerWebApplication("default.wurlitzer.webapp",  wurlitzerApp)
		addWurlitzerWebApplication("wurlitzer.webapp1", wurlitzerApp)  // alias
		
		setTestCaseProperty("webservice.host1.service1.url", new URL("http://%WURLITZER1%/wurlitzer-noejb/JaxWSServiceImpl1Service?WSDL"))  
		setTestCaseProperty("webservice.host1.service2.url", new URL("http://%WURLITZER1%/wurlitzer-noejb/JaxWSServiceImpl2Service?WSDL"))
		setTestCaseProperty("webservice.host2.service1.url", new URL("http://%WURLITZER2%/wurlitzer-noejb/JaxWSServiceImpl1Service?WSDL"))
		setTestCaseProperty("webservice.host2.service2.url", new URL("http://%WURLITZER2%/wurlitzer-noejb/JaxWSServiceImpl2Service?WSDL"))
		
	}
	
	
}

