package com.ca.apm.systemtest.fld.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadWebViewUtil {
	private static final Logger log = LoggerFactory.getLogger(LoadWebViewUtil.class);

	public static void logDebug(String format, Object... args) throws MalformedURLException, IOException {
		log.debug(format, args);
	}

	public static void logInfo(String format, Object... args) throws MalformedURLException, IOException {
		log.info(format, args);
	}

	public static List<String[]> downloadAndParseCsv(String url) throws MalformedURLException, IOException {
		log.info("Parsing CSV file from {}", url);
		int i=0;
		URL realUrl = new URL(url);
		List<String[]> retVal = new ArrayList<>();
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(realUrl.openStream()))) {
			String line;
			while ( (line = bReader.readLine()) != null) {
				i++;
				String[] lineSplit = line.split(",", 3);
				if (lineSplit.length != 3) {
					log.error("Line {} in CSV file has no 3 elements - skipping", i);
				} else {
					retVal.add(lineSplit);
				}
			}
		}
		log.debug("Parsing CSV file finished, {} lines imported", url, i);
		return retVal;
	}
}
