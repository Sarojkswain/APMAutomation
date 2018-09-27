/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class for managing properties files where properties are stored in
 * <propertyName>=<propertyValue>,
 * with comments starting with #
 * 
 * @author sobar03
 *
 */
public class PropertiesUtility {

    private static final Logger log = LoggerFactory.getLogger(PropertiesUtility.class);

    /**
     * Method for changing properties based on path, if add not found is set to true, properties not
     * found in file will be added at the end. <br>
     * If it is false then if not all properties are found, then it will throw an exception.
     */
    public static void saveProperties(String path, Map<String, String> newProperties,
        boolean addNotFoundProperties) throws Exception {
        ArrayList<String> changedProperties = new ArrayList<String>();

        try (FileReader fr = new FileReader(path); BufferedReader br = new BufferedReader(fr);)

        {
            String currentLine = br.readLine();
            while (currentLine != null) {

                if (!currentLine.startsWith("#") && currentLine.contains("=")) {
                    String key = currentLine.substring(0, currentLine.indexOf("="));
                    if (newProperties.containsKey(key)) {
                        changedProperties.add(key + "=" + newProperties.get(key));
                        newProperties.keySet().remove(key);
                    } else {
                        changedProperties.add(currentLine);
                    }

                } else {
                    changedProperties.add(currentLine);
                }
                currentLine = br.readLine();
            }
            if (!newProperties.keySet().isEmpty() && !addNotFoundProperties) {
                throw new Exception("Properties " + newProperties
                    + " were not found, so they couldn't be changed");
            }
            if (addNotFoundProperties && !newProperties.keySet().isEmpty()) {
                for (String key : newProperties.keySet()) {
                    changedProperties.add(key + "=" + newProperties.get(key));
                }
            }
        }
        try (FileWriter fw = new FileWriter(path); BufferedWriter bw = new BufferedWriter(fw);) {
            for (String line : changedProperties) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }

    }

    public static void updateProperty(String filePath, String propertyKey, String propertyValue)
        throws Exception {
        log.info("Updating " + propertyKey + " to '" + propertyValue + "'");
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put(propertyKey, propertyValue);
        PropertiesUtility.saveProperties(filePath, props, false);
    }

    public static void insertProperty(String filePath, String propertyKey, String propertyValue)
        throws Exception {
        log.info("Updating " + propertyKey + " to '" + propertyValue + "'");
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put(propertyKey, propertyValue);
        PropertiesUtility.saveProperties(filePath, props, true);
    }

    /**
     * Comments out existing property in the properties file, making it inactive
     */
    public static void commentProperties(String path, List<String> propertyNames) throws Exception {
        final ArrayList<String> outputLines = new ArrayList<String>();

        try (FileReader fr = new FileReader(path); BufferedReader br = new BufferedReader(fr);) {
            String currentLine = br.readLine();
            while (currentLine != null) {

                if (!currentLine.startsWith("#") && currentLine.contains("=")) {
                    String key = currentLine.substring(0, currentLine.indexOf("=")).trim();
                    if (propertyNames.contains(key)) {
                        outputLines.add("#" + currentLine);
                        propertyNames.remove(key);
                    } else {
                        outputLines.add(currentLine);
                    }

                } else {
                    outputLines.add(currentLine);
                }
                currentLine = br.readLine();
            }
            if (!propertyNames.isEmpty()) {
                throw new Exception("Properties " + propertyNames
                    + " were not found, so they couldn't be commented out");
            }
        }
        try (FileWriter fw = new FileWriter(path); BufferedWriter bw = new BufferedWriter(fw);) {
            for (String line : outputLines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
    }

    /**
     * Uncomments existing property in the properties file, making it active
     */
    public static void uncommentProperties(String path, List<String> propertyNames)
        throws Exception {
        final ArrayList<String> outputLines = new ArrayList<String>();

        try (FileReader fr = new FileReader(path); BufferedReader br = new BufferedReader(fr);) {
            String currentLine = br.readLine();
            while (currentLine != null) {

                if (currentLine.startsWith("#") && currentLine.contains("=")) {
                    final String key = currentLine.substring(1, currentLine.indexOf("=")).trim();
                    if (propertyNames.contains(key)) {
                        outputLines.add(currentLine.substring(1));
                        propertyNames.remove(key);
                    } else {
                        outputLines.add(currentLine);
                    }

                } else {
                    outputLines.add(currentLine);
                }
                currentLine = br.readLine();
            }
            if (!propertyNames.isEmpty()) {
                throw new Exception("Properties " + propertyNames
                    + " were not found, so they couldn't be uncommented");
            }
        }
        try (FileWriter fw = new FileWriter(path); BufferedWriter bw = new BufferedWriter(fw);) {
            for (String line : outputLines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        }
    }

    /**
     * Reads properties from a file to hash map
     * 
     * 
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, String> getPropertiesAsMap(String path)
        throws FileNotFoundException, IOException {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        try (FileReader fr = new FileReader(path); BufferedReader br = new BufferedReader(fr);)

        {
            String currentLine = br.readLine();
            while (currentLine != null) {
                if (!currentLine.startsWith("#") && currentLine.contains("=")) {
                    String key = currentLine.substring(0, currentLine.indexOf("="));
                    String value = currentLine.substring(currentLine.indexOf("=") + 1);

                    propertiesMap.put(key, value);
                }
                currentLine = br.readLine();
            }
        }
        return propertiesMap;
    }



}
