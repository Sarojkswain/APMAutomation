/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tools.cicstestdriver.xml;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;

/**
 * XMLParser is a helper class to use the Castor utilities for
 * marshalling and unmarshaling an XML file.
 * The XMLParser uses a mapping file to map each object defined in java
 * to the XML objects in the file.
 */

public class XMLParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLParser.class);

	private static Mapping mapping = null;
	private static Mapping defaultMapping = null;

	/**
	 * Construct and load the mapping file
	 * @param mappingFile
	 */
	public XMLParser(String mappingFile)
	{
		if (null == mapping)
		{
			mapping = new Mapping();

			try
			{
				// Load the mapping information from the file
				mapping.loadMapping(mappingFile);
			}
			catch (IOException e)
			{
				LOGGER.error("IOException   : " + e.getMessage());
				// System.out.println("IOException   : " + e.getMessage());
			}
			catch (MappingException e)
			{
				LOGGER.error("MappingException   : " + e.getMessage());
				// System.out.println("MappingException   : " + e.getMessage());
			}
		}
	}

	// Block usage
	private XMLParser()
	{
	}

	/**
	 * Creating a new XML file by marshaling the java objects
	 * @param config = IntroscopeToNsm configuration class hierarchy to save.
	 * @param propertiesFile = The path of the file to save to.
	 * @throws Exception if fails
	 */
	public synchronized void setConfig(CallJobStack config, String propertiesFile)
	throws Exception
	{
		try
		{
			FileWriter writer = new FileWriter(propertiesFile);
			Marshaller marshaller = new Marshaller(writer);
			marshaller.setMapping(mapping);
			marshaller.marshal(config);
		}
		catch (Exception ex)
		{
			LOGGER.error("Error while Saving the configuration file   : " + ex.getMessage());
			// System.out.println("Error while Saving the configuration file   : " + ex.getMessage());
			ex.printStackTrace();
			throw ex;
		}
	}

	/*
	 * Reading XML file and transform it to java objects
	 */
	public CallJobStack getConfig(String propertiesFile)
	{
		final CallJobStack config;
		try
		{
			final Reader reader = new FileReader(propertiesFile);
			Unmarshaller unmar = new Unmarshaller(mapping);
			config = (CallJobStack) unmar.unmarshal(reader);
			return config;
		}
		catch (Exception ex)
		{
			LOGGER.error("Error while getting the config data   : " + ex.getMessage());
			//System.out.println("Error while getting the config data   : " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}

	}
}
