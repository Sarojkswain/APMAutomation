package pl.mf.amf.tester;

import java.io.File;

import org.apache.jmeter.protocol.amf.util.AmfXmlConverter;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			byte[] data = org.apache.commons.io.FileUtils
					.readFileToByteArray(new File(
							"/mnt/mfranek/tmp/dev-tools/apache-jmeter-2.9-realtime/bin/POST6164123184367298893.binary"));

			String xml = AmfXmlConverter.convertAmfMessageToXml(data);
			System.out.println(xml);

			org.apache.commons.io.FileUtils
					.writeStringToFile(
							new File(
									"/mnt/mfranek/tmp/dev-tools/apache-jmeter-2.9-realtime/bin/out4.xml"),
							xml);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}