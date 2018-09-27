package com.ca.apm.tests.utility;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
/*import com.gargoylesoftware.htmlunit.WebClient;*/

public class GeneralUtils {
	
	
	public static String getLocalMachineIPAddress() {
		InetAddress addr;
		String ipAddr = null ;
		try {
			addr = InetAddress.getLocalHost();
			ipAddr = addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ipAddr.toString();
	}
	 
	public static String getLocalMachineHostName() throws Exception{
		InetAddress addr = InetAddress.getLocalHost();
		String hostname = addr.getHostName();
		return hostname.toString();
	}
	
	public static void sendHttpRequest(String url) {
	
		try {
			URL actualUrl = new URL(url);
			WebConversation conversation = new WebConversation();
		    WebRequest request = new GetMethodWebRequest(actualUrl,"");
		    System.out.println("request: "+request);
		    WebResponse response = conversation.getResponse(request);
		    System.out.println("response: "+response);	
		   

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public static void sendHttpRequestwithreferrer(String url,String referer) {
		
		try {
			URL actualUrl = new URL(url);
			WebConversation conversation = new WebConversation();
		    WebRequest request = new GetMethodWebRequest(actualUrl,"");
		    request.setHeaderField("Referer",referer );
		    System.out.println("request: "+request);
		    WebResponse response = conversation.getResponse(request);
		    System.out.println("response: "+response);	
		   

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * 
	 * 1, "http://130.200.163.88/MSPetShop/Default.aspx"
	 * 2, "http://130.200.163.88/MSPetShop/SignIn.aspx"
	 * 3, "http://130.200.163.88/MSPetShop/Category.aspx?categoryId=REPTILES"
	 * 4, "http://130.200.163.88/MSPetShop/Category.aspx?categoryId=DOGS"
	 * 5, "http://130.200.163.88/MSPetShop/Category.aspx?categoryId=BIRDS"
	 * 6, "http://130.200.163.88/MSPetShop/SignOut.aspx"
	 * 7, "http://130.200.163.88/MSPetShop/OrderBilling.aspx"
	 * 8, "http://130.200.163.88/MSPetShop/Search.aspx?keywords="
	 * 9, "http://130.200.163.88/MSPetShop/Help.aspx"
	 * 10, "http://130.200.163.88/MSPetShop/OrderBilling.aspx"
	 * 
	 * 
	 * @param petShopIp
	 * @return
	 */
	public static Map<Integer, String> getPetshopURLs(String petShopIp){

		Map <Integer, String> listOfPetShopURLs = new HashMap<Integer, String>();
	
		listOfPetShopURLs.put(1, "http://"+petShopIp+"/MSPetShop/Default.aspx");
		listOfPetShopURLs.put(2, "http://"+petShopIp+"/MSPetShop/SignIn.aspx"); 
		listOfPetShopURLs.put(3, "http://"+petShopIp+"/MSPetShop/Category.aspx?categoryId=REPTILES");
		listOfPetShopURLs.put(4, "http://"+petShopIp+"/MSPetShop/Category.aspx?categoryId=DOGS");
		listOfPetShopURLs.put(5, "http://"+petShopIp+"/MSPetShop/Category.aspx?categoryId=BIRDS");
		listOfPetShopURLs.put(6, "http://"+petShopIp+"/MSPetShop/SignOut.aspx");
		listOfPetShopURLs.put(7, "http://"+petShopIp+"/MSPetShop/OrderBilling.aspx");
		listOfPetShopURLs.put(8, "http://"+petShopIp+"/MSPetShop/Search.aspx?keywords=");
		listOfPetShopURLs.put(9, "http://"+petShopIp+"/MSPetShop/Help.aspx");
		listOfPetShopURLs.put(10, "http://"+petShopIp+"/MSPetShop/OrderBilling.aspx");
		
		
		return listOfPetShopURLs;
		
	}

	/*public static void sendHttpRequest(String url) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		WebClient web = new WebClient();
		web.setThrowExceptionOnScriptError(false);
		
	}*/
	    

}
