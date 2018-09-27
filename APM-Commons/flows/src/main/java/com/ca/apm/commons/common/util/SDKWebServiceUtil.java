package com.ca.apm.commons.common.util;

import java.io.ByteArrayOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDKWebServiceUtil {

    static MessageFactory messageFactory = null;
    static SOAPMessage soapMessage = null;
    static SOAPPart soapPart = null;
    static String url = "";
    static SOAPMessage soapResponse = null;
    protected static String response = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SDKWebServiceUtil.class);


    /**
     * To enable web service Log in EM
     * 
     * @return List<String>
     */
    public static List<String> listAddPropsToEnableWebserviceLog() {
        List<String> addEMProperties = new ArrayList<String>();
        addEMProperties.add("log4j.logger.Manager.IscopeAlertsExtension=DEBUG, alertslogfile");
        addEMProperties.add("log4j.logger.Manager.IntroscopeWebServices=DEBUG, webserviceslogfile");
        addEMProperties.add("log4j.appender.webserviceslogfile.File=logs/IntroscopeWebServices.log");
        addEMProperties.add("log4j.appender.webserviceslogfile=com.wily.org.apache.log4j.RollingFileAppender");
        addEMProperties.add("log4j.appender.webserviceslogfile.layout=com.wily.org.apache.log4j.PatternLayout ");
        addEMProperties
            .add("log4j.appender.webserviceslogfile.layout.ConversionPattern=%d{M/dd/yy hh:mm:ss a z} [%-3p] [%c] %m%n");
        addEMProperties.add("log4j.appender.webserviceslogfile.MaxBackupIndex=4");
        addEMProperties.add("log4j.appender.webserviceslogfile.MaxFileSize=200MB");

        return addEMProperties;
    }


    /**
     * To get SOAP connection from SOAPConnectionFactory
     * 
     * @return SOAPConnection
     * @throws SOAPException
     */

    public static SOAPConnection getSOAPConnection() throws SOAPException {
        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();

        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        return soapConnection;
    }



    /**
     * *
     * To Create SOAP Message Factory & SOAP Message Object reference
     * 
     * @return SOAPMessage
     * @throws SOAPException
     */

    private static SOAPMessage createSOAPMessage() throws SOAPException {
        messageFactory = MessageFactory.newInstance();
        soapMessage = messageFactory.createMessage();

        return soapMessage;
    }

    /**
     * To create SOAP Part Object reference and provided Auth for web service
     * 
     * @param userName
     * @param password
     * @return SOAPPart
     * @throws SOAPException
     */

    private static SOAPPart createSOAPPart(final String userName, final char[] password) throws SOAPException {
        soapPart = soapMessage.getSOAPPart();

        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });

        return soapPart;
    }

    /**
     * To send actual SOAP request to EM
     * 
     * @param soapMessage
     * @return SOAPMessage
     * @throws Exception
     */
    public static SOAPMessage createSOAPRequest(SOAPMessage soapMessage) throws Exception {

        soapMessage.saveChanges(); /* Print the request message */
        soapMessage.writeTo(System.out);
        System.out.println();
        LOGGER.info("Request SOAP Message = " + soapMessage);
        return soapMessage;
    }

    /**
     * generate URL or WSDL for API
     * 
     * @param serviceName
     * @param EMHost
     * @param emWebPort
     * @return String
     */
    public static String getUrl(String serviceName, String EMHost, String emWebPort) {
        StringBuffer url = new StringBuffer();

        url.append("http://");
        url.append(EMHost);
        url.append(":");
        url.append(emWebPort);

        if (serviceName.trim().equalsIgnoreCase("AlertPollingService")) {
            url.append("/introscope-web-services/services/AlertPollingService?wsdl");
            LOGGER.info("AlertPollingService URI::" + url.toString());
        } else if (serviceName.trim().equalsIgnoreCase("serverURI")) {
            LOGGER.info("Server URI::" + url.toString());
        } else if (serviceName.trim().equalsIgnoreCase("MetricsDataService")) {
            url.append("/introscope-web-services/services/MetricsDataService?wsdl");
            LOGGER.info("MetricsDataService URI::" + url.toString());
        } else
            url.delete(0, url.length());
        return url.toString();
    }

    /**
     * Get the current time in XML specified format
     * 
     * @return String
     */
    public static String getCurrentTime() {
        StringBuffer sb = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String tempTime = sdf.format(cal.getTime());
        sb.append(tempTime.substring(0, tempTime.length() - 2));
        sb.append(":");
        sb.append(tempTime.substring(tempTime.length() - 2, tempTime.length()));

        return sb.toString();
    }

    /**
     * Method used to read the SOAP Response
     * 
     * @param soapResponse
     * @return String
     * @throws Exception
     */

    public static String readSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapResponse.getSOAPPart().getContent();

        ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(streamOut);
        transformer.transform(sourceContent, result);

        String strMessage = streamOut.toString();
        LOGGER.info("\nResponse SOAP Message = " + strMessage);
        return strMessage;
    }


    /**
     * To validate response with Target String
     * 
     * @param readSOAPResponse
     * @param targetString
     * @return boolean
     */

    public static boolean validateResult(String readSOAPResponse, String targetString) {
        boolean flag = false;

        flag = readSOAPResponse.contains(targetString.trim());

        return flag;
    }

    /**
     * External method for creating, execute SOAP request & return response in String
     * 
     * @param Map<String,String>
     * @return String
     * @throws Exception
     */
    public static String createSOAPRequest(Map<String, String> webServiceParamIn) throws Exception {

        createSOAPMessage();
        createSOAPPart(webServiceParamIn.get("userName"), webServiceParamIn.get("password").toCharArray());
        url =
            getUrl(webServiceParamIn.get("serviceName"), webServiceParamIn.get("EMHost"),
                webServiceParamIn.get("emWebPort"));
        if (webServiceParamIn.get("serviceName").equalsIgnoreCase("AlertPollingService")) {
            generateSOAPRequestForAlertPollingService(webServiceParamIn, soapPart);
        } else if (webServiceParamIn.get("serviceName").equalsIgnoreCase("MetricsDataService")) {
            generateSOAPRequestForMetricsDataService(webServiceParamIn, soapPart);
        }
        // TODO : future WSDL serviceName will added with else if condition to create SOAP request
        else {
            LOGGER.error("Provided service Name Invalid or Empty Please Check the Input Again");
        }

        createSOAPRequest(soapMessage);

        SOAPConnection soapConnection = getSOAPConnection();
        soapResponse = soapConnection.call(soapMessage, url);
        LOGGER.info("SOAP request created sucessfully ....");
        response = SDKWebServiceUtil.readSOAPResponse(soapResponse);
        LOGGER.info("Recived SOAP response ...." + response);
        return response;
    }

    /**
     * reference method to create SOAP request for AlertPollingService
     * 
     * @param manModuleName
     * @param soapPart
     * @throws Exception
     */

    private static void generateSOAPRequestForAlertPollingService(Map<String, String> webServiceParamIn,
        SOAPPart soapPart) throws Exception {
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("aler", "http://alerts.webservicesimpl.server.introscope.wily.com");

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        if (webServiceParamIn.get("operationName").equalsIgnoreCase("getAgentSnapshots")) {
            SOAPElement soapBodyElem =
                soapBody.addChildElement(webServiceParamIn.get("operationName"), "aler",
                    "http://schemas.xmlsoap.org/soap/encoding/");

            SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("manModuleName");
            soapBodyElem1.addNamespaceDeclaration("type", "xsd:string");
            soapBodyElem1.addTextNode(webServiceParamIn.get("manModuleName"));
        }
        // TODO : future AlertPollingService all operation will extended with else if condition to
        // create SOAP request
        else {
            LOGGER.error("Provided operationName Invalid or Empty Please Check the Input Again");
        }
    }

    /**
     * reference method to create SOAP request for MetricsDataService
     * 
     * @param Map<String,String>
     * @param soapPart
     * @throws Exception
     */

    private static void generateSOAPRequestForMetricsDataService(Map<String, String> webServiceParamIn,
        SOAPPart soapPart) throws Exception {
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("met", "http://metricsdata.webservicesimpl.server.introscope.wily.com");


        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        if (webServiceParamIn.get("operationName").equalsIgnoreCase("getMetricData")) {
            SOAPElement soapBodyElem = soapBody.addChildElement(webServiceParamIn.get("operationName"), "met");
            soapBodyElem.addNamespaceDeclaration("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");


            SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("agentRegex");
            soapBodyElem1.addNamespaceDeclaration("type", "xsd:string");
            soapBodyElem1.addTextNode(webServiceParamIn.get("agentRegex"));

            SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("metricRegex");
            soapBodyElem2.addNamespaceDeclaration("type", "xsd:string");
            soapBodyElem2.addTextNode(webServiceParamIn.get("metricRegex"));

            SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("startTime");
            soapBodyElem3.addNamespaceDeclaration("type", "xsd:dateTime");
            soapBodyElem3.addTextNode(webServiceParamIn.get("startTime"));

            SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("endTime");
            soapBodyElem4.addNamespaceDeclaration("type", "xsd:dateTime");
            soapBodyElem4.addTextNode(webServiceParamIn.get("endTime"));

            SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("dataFrequency");
            soapBodyElem5.addNamespaceDeclaration("type", "xsd:int");
            soapBodyElem5.addTextNode(webServiceParamIn.get("dataFrequency"));
        }
        // TODO : future in MetricsDataService all operation will extended with else if condition to
        // create SOAP request

        else {
            LOGGER.error("Provided operationName Invalid or Empty Please Check the Input Again");
        }
    }
}
