package testcase.sample

import groovy.util.logging.Log;

import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull;

import com.ca.wurlitzer.api.*
import com.ca.wurlitzer.api.command.HttpRequestor
import com.ca.wurlitzer.api.command.webservice.JaxWebService1;
import com.ca.wurlitzer.api.command.webservice.JaxWebService2;
import com.google.gson.Gson;

import testcase.BaseSystemTest

// These are JUnit test cases implemented in Groovy
// All methods marked with the @Test annotation will be executed

@Log
public class JaxWSClientRequests extends BaseSystemTest {

	static def remoteHost = null

	def URL  webServiceHost1Service1URL
	def URL  webServiceHost1Service2URL
	def URL  webServiceHost2Service1URL
	def URL  webServiceHost2Service2URL

	@Before public void initialize() {
		// globalConfiguration
	    webServiceHost1Service1URL= globalConfiguration.getTestCaseProperty("webservice.host1.service1.url")
		webServiceHost1Service2URL= globalConfiguration.getTestCaseProperty("webservice.host1.service2.url")
		webServiceHost2Service1URL= globalConfiguration.getTestCaseProperty("webservice.host2.service1.url")
		webServiceHost2Service2URL= globalConfiguration.getTestCaseProperty("webservice.host2.service2.url")
	}


	@Test
	public void service1_operations_1_to_5() throws Exception {

		log.info("WURLITZER Service1 WSDL =  " + webServiceHost1Service1URL.toString())

		TransactionCommandQueue transaction = new TransactionCommandQueue()

		// Create and configure the Wurlitzer JAX-WS client (JaxWebService1)
		JaxWebService1 ws1 = new JaxWebService1()
		ws1.setExecutionLocation(webServiceHost1Service1URL)
		ws1.setOperationStart(1)
		ws1.setOperationEnd(5)

		// Add the client to a Wurlitzer command set
		transaction.addCommand(ws1)

		// Invoke the remote web service
		TransactionResult result = TransactionExecutor.execute(transaction)

		assertNotNull("The result should not be null", result)
	}



	@Test
	public void service1_and_service2_operations() throws Exception  {

		TransactionCommandQueue transaction = new TransactionCommandQueue()

		// Initial web service client calls (operation1 to operation4)
		JaxWebService1 ws1 = new JaxWebService1()
		ws1.setExecutionLocation(webServiceHost1Service1URL)
		ws1.setOperationStart(1)
		ws1.setOperationEnd(2)
		transaction.addCommand(ws1)

		// Each operation should then call operation10 ... operation12)
		JaxWebService1 ws2 = new JaxWebService1()
		ws2.setExecutionLocation(webServiceHost1Service1URL)
		ws2.setOperationStart(10)
		ws2.setOperationEnd(12)
		ws2.setDelayMillis(5000) // invoke a sleep() in the web service
		transaction.addCommand(ws2)

		//URL  service2WSDL = new URL(connInfo.getURL().toString() + "/JaxWSServiceImpl2Service?WSDL")

		// Each operation should then call operation50 ... operation55 on
		// WebService2
		JaxWebService2 ws3 = new JaxWebService2()
		ws3.setExecutionLocation(webServiceHost1Service2URL)
		ws3.setOperationStart(50)
		ws3.setOperationEnd(55)
		transaction.addCommand(ws3)

		// Start the set of web service calls
		TransactionResult result = TransactionExecutor.execute(transaction)
	}
}

