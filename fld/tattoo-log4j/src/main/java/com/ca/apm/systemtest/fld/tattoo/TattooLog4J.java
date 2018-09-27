package com.ca.apm.systemtest.fld.tattoo;

import com.ca.apm.systemtest.fld.tattoo.core.TattooCore;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by haiva01 on 13.1.2016.
 */
public class TattooLog4J extends TattooCore {
    private static final Logger log = LoggerFactory.getLogger(TattooLog4J.class);

    public static void main(String[] args) throws IOException {
        // Configure basic logging so that the tool itself can do some output.

        BasicConfigurator.configure();

        // Run the core functionality.

        int retval = new TattooLog4J().run(args);
        System.exit(retval);
    }

    @Override
    protected void configureLoggingSystem(File configurationFile) {
        if (configurationFile != null) {
            final String configurationFileAbsPath = configurationFile.getAbsolutePath();
            log.info("Configuring log4j from {}", configurationFileAbsPath);
            PropertyConfigurator.configure(configurationFileAbsPath);
        } else {
            log.info("Configuring log4j from internal resource properties file");
            InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("tattoo.log4j.properties");
            PropertyConfigurator.configure(inputStream);
        }
    }

    @Override
    protected void enableVerboseLogging() {
        org.apache.log4j.Logger tattooLogger = org.apache.log4j.Logger.getLogger(
            "com.ca.apm.systemtest.fld.tattoo");
        tattooLogger.setLevel(Level.DEBUG);
    }
}
