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
	public void webService1_Cross_JVM_Test() throws Exception {
		TransactionCommandQueue transaction = new TransactionCommandQueue()

		JaxWebService1 cmd = new JaxWebService1()
		cmd.setExecutionLocation(webServiceHost1Service1URL)
		cmd.setOperationStart(70)
		cmd.setOperationEnd(75)
		//cmd.setThrowException(true);
		transaction.addCommand(cmd)


		// WebService2 on Host2
		JaxWebService2 cmd2 = new JaxWebService2();
		cmd2.setExecutionLocation(webServiceHost2Service2URL)
		cmd2.setOperationStart(90)
		cmd2.setOperationEnd(95)
		transaction.addCommand(cmd2)

		System.out.println("\n*** Contacting  " +  webServiceHost2Service2URL.toString() + "\nJSON = " + new Gson().toJson(transaction))

		// Start the set of web service calls
		TransactionResult result = TransactionExecutor.execute(transaction)

		System.out.println("\n*** WS TRANSACTION RESULT: json = " + new Gson().toJson(result) )
	}
}
