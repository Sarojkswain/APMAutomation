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


package com.ca.apm.browseragent.testsupport.collector.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

/***
 * This handler responds and processes profiles
 *
 * @author Doug Briere
 */

public class ProfileConfigContextHandler extends AbstractHttpHandler {
    public static final String CONFIGS_CONTEXT = "/configs";

    private static final String DEFAULT_CONFIG_NAME = "defaultprofile.json";
    private static final String PROFILE_PREFIX = "profile.";
    private static final String PROFILE_EXT = ".json";
    private static final String PROFILES_DIR = "profiles";

    private final String PROFILES_DIRECTORY;
    private final String DEFAULT_PROFILE_FILE_PATH;

    private final Configuration DEFAULT_PROFILE_CONFIG;

    private final Logger LOGGER = Logger.getLogger(ProfileConfigContextHandler.class);

    /**
     * Constructor
     *
     * @param workingDirectory
     * @param collectorNameInstance
     */

    public ProfileConfigContextHandler(String workingDirectory, String collectorNameInstance,
        String collectionUrl) {

        DEFAULT_PROFILE_CONFIG = BATestCollectorUtils.getDefaultConfiguration(collectionUrl);

        PROFILES_DIRECTORY =
            workingDirectory + File.separator + collectorNameInstance + File.separator
                + PROFILES_DIR;
        DEFAULT_PROFILE_FILE_PATH = workingDirectory + File.separator + DEFAULT_CONFIG_NAME;

        try {
            File profilesDir = new File(PROFILES_DIRECTORY);

            // now create profiles i.e. C:/workingdirectory/myinstance/profiles
            if (!profilesDir.exists()) {
                boolean success = profilesDir.mkdir();
                LOGGER.debug("ProfileConfigContextHandler: creating profiles directory "
                    + PROFILES_DIRECTORY + " result: " + success);
            }

            // See if the default profile exists, this will be placed at the top level working
            // directory shared
            // by all collectors if more than one.
            File defaultProfile = new File(DEFAULT_PROFILE_FILE_PATH);
            if (!defaultProfile.exists()) {
                BATestCollectorUtils.writeStringToFile(
                    BATestCollectorUtils.getPrettyJson(DEFAULT_PROFILE_CONFIG), defaultProfile);

                LOGGER.debug("ProfileConfigContextHandler: default profile being created "
                    + DEFAULT_PROFILE_FILE_PATH);
            }
        } catch (Exception e) {
            LOGGER.error("ProfileConfigContextHandler exception found ", e);
        }

    }

    /**
     * Provides an ability to get the Profile/Configuration based on the passed tenant and app
     *
     * @param tenantId
     * @param appId
     * @return Configuration
     */

    public Configuration getConfiguration(String tenantId, String appId,
        boolean allowDefaultIfNotFound) {
        return getConfigurationFromStorage(tenantId, appId, allowDefaultIfNotFound, null);
    }

    /**
     * Pushes updates of the passed configuration to storage. Preferred way to update a
     * Configuration
     *
     * @param config retrieved via getConfiguration(), mutated, then passed to this method for
     *        persistence
     * @param tenantId
     * @param appId
     * @param versionExtension
     */

    public void updateConfiguration(Configuration config, String tenantId, String appId,
        String versionExtension) {
        updateConfiguration(config, tenantId, appId, versionExtension, false);
    }

    /**
     * This update method takes a string and should be used if the user needs to write specific
     * config
     * content to disk. This is to be used for negative testing where a bad/invalid config to be
     * used.
     * 
     * @param configAsString - some bogus string
     * @param tenantId
     * @param appId
     * @param versionExtension
     */

    public void updateConfigurationString(String configAsString, String tenantId, String appId,
        String versionExtension) {
        try {
            // Now update the config to the well known name i.e. profile.tenantId.appId.json
            String fileName = generateFileName(tenantId, appId, null);
            File tenantConfigFile = new File(fileName);

            BATestCollectorUtils.writeStringToFile(configAsString, tenantConfigFile);

            if (versionExtension != null && versionExtension.length() > 0) {
                // Now write a version of the same file to
                // profile.tenantId.appId.versionExtension.json,
                // this will serve as a
                // snapshot of the config in time useful for debugging/historical purposes later
                fileName = generateFileName(tenantId, appId, versionExtension);
                File versionBackup = new File(fileName);

                if (versionBackup.exists()) {
                    LOGGER
                        .warn("updateConfigurationString call to update with file already exists by name: "
                            + fileName);
                }

                BATestCollectorUtils.writeStringToFile(configAsString, versionBackup);
            }

            performConfigValidationCheck(configAsString,
                "updateConfigurationString call with invalid config failure exception:\n");

        } catch (Exception e) {
            LOGGER.error("updateConfigurationString exception found", e);
        }
    }

    /**
     * Pushes updates of the passed configuration to storage.
     * 
     * @param config
     * @param tenantId
     * @param appId
     * @param versionExtension
     * @param isCreate
     */

    public void updateConfiguration(Configuration config, String tenantId, String appId,
        String versionExtension, boolean isCreate) {
        try {
            // if this is creation only set created and reset updated
            long currentTime = System.currentTimeMillis();
            if (isCreate) {
                config.setCreated(currentTime);
                config.setLastUpdated(currentTime);
            }
            // we are updating, only update the updated time.
            else {
                config.setLastUpdated(currentTime);
            }

            String asString = BATestCollectorUtils.getPrettyJson(config);
            updateConfigurationString(asString, tenantId, appId, versionExtension);

        } catch (Exception e) {
            LOGGER.error("updateConfiguration exception found", e);
        }
    }

    /**
     * This is a helper for callers to updateConfiguration, but only to this class
     * 
     * @return
     */

    private String generateVersionExtension() {
        return "webput-" + System.currentTimeMillis();
    }


    // implements HttpHandler

    public void handle(HttpExchange httpExchange) throws IOException {

        // From HttpExchange JavaDocs:
        // The typical life-cycle of a HttpExchange is shown in the sequence below.

        // 1. getRequestMethod() to determine the command
        // 2. getRequestHeaders() to examine the request headers (if needed)
        // 3. getRequestBody() returns a InputStream for reading the request body. After reading the
        // request body, the stream is close.
        // 4. getResponseHeaders() to set any response headers, except content-length
        // 5. sendResponseHeaders(int,long) to send the response headers. Must be called before next
        // step.
        // 6. getResponseBody() to get a OutputStream to send the response body. When the response
        // body has been written, the stream must be closed to terminate the exchange.

        try {
            String requestMethod = httpExchange.getRequestMethod();

            switch (requestMethod) {
                case BATestCollectorUtils.METHOD_OPTIONS:
                    handleOPTIONS(httpExchange);
                    break;
                case BATestCollectorUtils.METHOD_GET:
                    handleGET(httpExchange);
                    break;
                case BATestCollectorUtils.METHOD_POST:
                    handlePOST(httpExchange);
                    break;
                case BATestCollectorUtils.METHOD_PUT:
                    handlePUT(httpExchange);
                    break;
                case BATestCollectorUtils.METHOD_DELETE:
                    handleDELETE(httpExchange);
                    break;
                default:
                    LOGGER.warn("Unknown request: " + requestMethod);
                    BATestCollectorUtils.sendResponse(httpExchange, requestMethod
                        + " method not supported for this end point",
                        BATestCollectorUtils.HTTP_SERVER_ERROR);
            }
        }
        // DO NOT REMOVE !!! The server that calls HttpHandler tries to log excetpion to a
        // configured logger
        // but when the logger isnt configured the exception doesnt go to standard output and not
        // seen. horrible!
        catch (Exception e) {
            LOGGER.error("Server error: ", e);
            BATestCollectorUtils.sendResponse(httpExchange, "Server error: " + e.getMessage(),
                BATestCollectorUtils.HTTP_SERVER_ERROR);
            throw e;
        }
    }


    /**
     *
     * @param httpExchange
     * @throws IOException
     */

    // GET
    // ALL: http://brido02-win7:5000/configs/ (best viewed in browser)
    // SINGLE: http://brido02-win7:5000/configs/<tenantId>/<appId>
    private void handleGET(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handleGET start");

        setResponseHeaders(httpExchange);

        // Parse out the request to determine what is being asked
        StringBuilder tenantSB = new StringBuilder();
        StringBuilder appSB = new StringBuilder();
        getRequestTenantAndApp(httpExchange, tenantSB, appSB);

        String responseString = "";
        int statusCode = BATestCollectorUtils.HTTP_OK;

        String appId = appSB.length() > 0 ? appSB.toString() : null;
        String tenantId = tenantSB.length() > 0 ? tenantSB.toString() : null;

        // If both app and tenant are set then easy get of the config in the map
        if (appId != null && tenantId != null) {
            String profileFileName = generateFileName(tenantId, appId, null);

            File profileFile = new File(profileFileName);
            if (profileFile.exists()) {
                responseString = BATestCollectorUtils.readStringFromFile(profileFile);

                performConfigValidationCheck(responseString,
                    "handleGET found config with invalid content failure exception:\n");
            } else {
                statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
                responseString = "Cant locate profile: " + profileFileName;
            }
        } // just the tenant id is set
        else if (tenantId != null) {
            // BUG: fix me
            statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
            responseString = "Dump of just teantId not implemented (yet)... ";
        } else // nothing is set, just the configs was requested, return everything
        {
            File profilesDir = new File(PROFILES_DIRECTORY);

            String[] profiles = profilesDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean returnValue = false;

                    if (name.startsWith(PROFILE_PREFIX) && name.endsWith(PROFILE_EXT)) {
                        LOGGER.debug("handleGET passes " + name);
                        returnValue = true;
                    }
                    return returnValue;
                }
            });

            responseString += "<html>";
            for (String profile : profiles) {
                File file = new File(PROFILES_DIRECTORY + File.separator + profile);

                if (file.exists()) {
                    String prettyJson = BATestCollectorUtils.readStringFromFile(file);

                    performConfigValidationCheck(prettyJson,
                        "handleGET getting all configs found config with invalid content failure exception:\n");

                    responseString +=
                        "<b>Profile Name: " + profile + "</b><pre>" + prettyJson + "</pre>";
                    responseString += "<hr/>";
                }
            }
            if (profiles == null || profiles.length == 0)
                responseString += "<pre>No profiles found</pre>";
            responseString += "</html>";
        }

        BATestCollectorUtils.sendResponse(httpExchange, responseString, statusCode);

        LOGGER.debug("handleGET end");
    }

    /**
     *
     * @param httpExchange
     * @throws IOException
     */

    // Create new profile, POST http://brido02-win7:5000/configs/<tenantId>/<appId>
    private void handlePOST(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handlePOST start");

        StringBuilder tenantSB = new StringBuilder();
        StringBuilder appSB = new StringBuilder();
        getRequestTenantAndApp(httpExchange, tenantSB, appSB);

        String appId = appSB.length() > 0 ? appSB.toString() : null;
        String tenantId = tenantSB.length() > 0 ? tenantSB.toString() : null;

        String responseString = "";
        int statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;

        if (appId != null && tenantId != null) {
            InputStreamReader inputStream =
                new InputStreamReader(httpExchange.getRequestBody(), "utf-8");

            String fileName = generateFileName(tenantId, appId, null);
            File file = new File(fileName);

            // If the file doesnt exist create the config...
            if (!file.exists()) {

                BufferedReader bufferedReader = new BufferedReader(inputStream);
                String entirePost = BATestCollectorUtils.readString(bufferedReader);

                LOGGER.debug("handlePOST with request body:\n " + entirePost);

                // Passing in true which will create default if not found
                Configuration defaultOrStoredConfig =
                    getConfigurationFromStorage(tenantId, appId, true, null);

                // User POSTED optional content, use this content into the config
                if (entirePost != null && entirePost.length() > 0) {

                    StringBuilder builder = new StringBuilder();
                    Configuration postedConfig =
                        performConfigValidationCheck(entirePost,
                            "POST input isnt valid, JSON parse with exception: ", builder);

                    // performConfigValidationCheck returns non-null config on no error
                    // if the config is valid might as well write as pretty json.
                    // Also check defaultOrStoredConfig non-null, could be null if the stored config
                    // is bogus
                    if (postedConfig != null && defaultOrStoredConfig != null) {
                        mergeConfigs(postedConfig, defaultOrStoredConfig);
                        updateConfiguration(defaultOrStoredConfig, tenantId, appId, null, true);
                        statusCode = BATestCollectorUtils.HTTP_OK;
                        responseString = BATestCollectorUtils.getPrettyJson(defaultOrStoredConfig);
                    } else // this is likely a bad config, but write it anyway (used for negative
                           // testing)
                    {
                        updateConfigurationString(entirePost, tenantId, appId, null);
                        statusCode = BATestCollectorUtils.HTTP_OK;
                        responseString = builder.toString();
                    }
                } else { // user choose not to post any optional post data..

                    if (defaultOrStoredConfig != null) {
                        updateConfiguration(defaultOrStoredConfig, tenantId, appId, null, true);

                        statusCode = BATestCollectorUtils.HTTP_OK;
                        responseString = BATestCollectorUtils.getPrettyJson(defaultOrStoredConfig);
                    } else { // this means default is corrupted
                        statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
                        responseString = "handlePOST Unexpected case default is invalid";
                        LOGGER.error("handlePOST ERROR unexpected case, default config invalid?");
                    }
                }


            } else {
                statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
                responseString =
                    "Config for tenant: " + tenantId + " and app: " + appId
                        + " exists, must use PUT to udpate";
            }
        }

        BATestCollectorUtils.sendResponse(httpExchange, responseString, statusCode);

        LOGGER.debug("handlePOST end");

    }

    /**
     *
     * @param httpExchange
     * @throws IOException
     */

    // PUT http://brido02-win7:5000/configs/<tenantId>/<appId> with updated json
    private void handlePUT(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handlePUT start");

        StringBuilder tenantSB = new StringBuilder();

        StringBuilder appSB = new StringBuilder();
        getRequestTenantAndApp(httpExchange, tenantSB, appSB);

        String appId = appSB.length() > 0 ? appSB.toString() : null;
        String tenantId = tenantSB.length() > 0 ? tenantSB.toString() : null;

        String responseString = "";
        // Configuration config = null;
        int statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
        if (appId != null && tenantId != null) {
            InputStreamReader inputStream =
                new InputStreamReader(httpExchange.getRequestBody(), "utf-8");

            BufferedReader bufferedReader = new BufferedReader(inputStream);
            String entirePut = BATestCollectorUtils.readString(bufferedReader);


            LOGGER.debug("handlePUT request put body:\n" + entirePut);

            StringBuilder errorBuilder = new StringBuilder();
            Configuration receivedConfig =
                performConfigValidationCheck(entirePut,
                    "PUT input isnt valid, JSON parse with exception: ", errorBuilder);


            // Get the stored config, false wont create a default.. must exist!
            AtomicBoolean configExists = new AtomicBoolean(false);
            Configuration defaultOrStoredConfig =
                getConfigurationFromStorage(tenantId, appId, false, configExists);

            // Found existing config
            if (defaultOrStoredConfig != null) {

                // valid PUT config use this to perform merge
                if (receivedConfig != null) {
                    mergeConfigs(receivedConfig, defaultOrStoredConfig);

                    // Save back to storage
                    updateConfiguration(defaultOrStoredConfig, tenantId, appId,
                        generateVersionExtension());

                    // Send back 200 along with the created object as json
                    statusCode = BATestCollectorUtils.HTTP_OK;
                    responseString = BATestCollectorUtils.getPrettyJson(defaultOrStoredConfig);
                } else { // write the text likely bogus to support negative testing
                    updateConfigurationString(entirePut, tenantId, appId,
                        generateVersionExtension());
                    statusCode = BATestCollectorUtils.HTTP_OK;
                    responseString = errorBuilder.toString();
                }
            } else {

                // The config exists, but Configuration couldnt be created (hence bad config)
                if (configExists.get()) {

                    // The receivedConfig could still be valid
                    if (receivedConfig != null) {
                        updateConfiguration(receivedConfig, tenantId, appId,
                            generateVersionExtension());
                        responseString = BATestCollectorUtils.getPrettyJson(receivedConfig);
                    } else {
                        updateConfigurationString(entirePut, tenantId, appId,
                            generateVersionExtension());
                        responseString = errorBuilder.toString();
                    }

                    statusCode = BATestCollectorUtils.HTTP_OK;
                } else {
                    statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
                    responseString =
                        "Config for tenant: " + tenantId + " and app: " + appId
                            + " not found, must call POST first";
                }
            }
        }

        BATestCollectorUtils.sendResponse(httpExchange, responseString, statusCode);

        LOGGER.debug("handlePUT end");
    }

    /**
     *
     * @param httpExchange
     * @throws IOException
     */

    // Delete a profile DELETE http://brido02-win7:5000/configs/<tenantId>/<appId>
    private void handleDELETE(HttpExchange httpExchange) throws IOException {
        LOGGER.debug("handleDELETE start");

        StringBuilder tenantSB = new StringBuilder();
        StringBuilder appSB = new StringBuilder();
        getRequestTenantAndApp(httpExchange, tenantSB, appSB);

        String appId = appSB.length() > 0 ? appSB.toString() : null;
        String tenantId = tenantSB.length() > 0 ? tenantSB.toString() : null;

        String responseString = "";
        int statusCode = BATestCollectorUtils.HTTP_OK;
        if (appId != null && tenantId != null) {
            String profileFileName = generateFileName(tenantId, appId, null);

            File file = new File(profileFileName);

            if (file.exists()) {
                file.delete();
                responseString += file + "\n";
                LOGGER.debug("handleDELETE deleted file:" + profileFileName);
            } else {
                statusCode = BATestCollectorUtils.HTTP_NOT_FOUND;
            }

            // Get just the front portion profile.ten.app.
            final String PREFIX = getFileNamePrefix(tenantId, appId) + ".";


            File profilesDir = new File(PROFILES_DIRECTORY);

            // List all the profiles that match the prefix getting the "snapshot" version
            String[] profiles = profilesDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean returnValue = false;
                    if (name.startsWith(PREFIX) && name.endsWith(PROFILE_EXT)) {
                        returnValue = true;
                    }
                    return returnValue;
                }
            });

            for (String snapShotProfile : profiles) {
                File snapShotFile = new File(PROFILES_DIRECTORY + File.separator + snapShotProfile);
                snapShotFile.delete();
                responseString += snapShotFile + "\n";
                LOGGER.debug("handleDELETE deleted file:" + snapShotFile);
            }
        }

        if (responseString.length() == 0) {
            responseString = "Zero files deleted";
        } else
            responseString = "Files deleted are:\n" + responseString;

        BATestCollectorUtils.sendResponse(httpExchange, responseString, statusCode);

        LOGGER.debug("handleDELETE end");
    }


    /**
     * Helper to parse out the request: /configs/tenantId/appId
     *
     * @param httpExchange
     * @param tenantSB - the string builder which the value is returned in
     * @param appSB - the string builder which the value is returned in
     */

    private void getRequestTenantAndApp(HttpExchange httpExchange, StringBuilder tenantSB,
        StringBuilder appSB) {
        LOGGER.debug("getRequestTenantAndApp start");
        String path = httpExchange.getRequestURI().getPath();

        // We know the request will at least contain "/configs" or "/configs/"
        String requestAfterConfigs = path.substring(CONFIGS_CONTEXT.length());
        LOGGER.debug("getRequestTenantAndApp requestAfterConfigs " + requestAfterConfigs);

        // if the request is /configs/tenId/appId, then we will get /tenId/appId
        // this if statement will take off the first slash, helps for processing the split under it
        if (requestAfterConfigs.startsWith("/")) {
            requestAfterConfigs = requestAfterConfigs.substring(1, requestAfterConfigs.length());
            LOGGER.debug("getRequestTenantAndApp requestAfterConfigs (updated) "
                + requestAfterConfigs);
        }

        String[] parts = requestAfterConfigs.split("/");

        String tenantId = parts.length > 0 && parts[0].length() > 0 ? parts[0] : "";
        String appId = parts.length > 1 && parts[1].length() > 0 ? parts[1] : "";

        // Mutable the builder for return
        tenantSB.append(tenantId);
        appSB.append(appId);

        LOGGER.debug("getRequestTenantAndApp end, tenantId: " + tenantId + " appId: " + appId);
    }

    /**
     * Helper to get the configuration from storage. If not exists, will return the default if the
     * flag is set.
     *
     * @param tenantId
     * @param appId
     * @param allowDefaultIfNotFound
     * @param foundFile optional param if the config file was found, since Configuration may still
     *        return null if the file fails to parse
     * @return
     */

    private Configuration getConfigurationFromStorage(String tenantId, String appId,
        boolean allowDefaultIfNotFound, AtomicBoolean foundFile) {
        Configuration config = null;
        String configContents = null;
        try {
            String fileName = generateFileName(tenantId, appId, null);

            File file = new File(fileName);

            // First see if the configuration already exists
            if (file.exists()) {

                // The Configuration file was found, but not yet sure if the file will prase.
                if (foundFile != null) {
                    foundFile.set(true);
                }

                LOGGER.debug("getConfigurationFromStorage found: " + fileName);

                configContents = BATestCollectorUtils.readStringFromFile(file);
                config =
                    performConfigValidationCheck(configContents,
                        "getConfigurationFromStorage parse exception on read");
            } else if (allowDefaultIfNotFound) {
                fileName = DEFAULT_PROFILE_FILE_PATH;
                file = new File(fileName);

                if (file.exists()) {

                    configContents = BATestCollectorUtils.readStringFromFile(file);
                    config =
                        performConfigValidationCheck(configContents,
                            "getConfigurationFromStorage default parse exception on read");

                    LOGGER.debug("getConfigurationFromStorage found file: " + file
                        + " Configuration is: \n" + config);
                } else {
                    LOGGER.debug("getConfigurationFromStorage Unable to find " + fileName);
                }
            } else {
                LOGGER.error("file : " + file + " not found ");
            }

        } catch (Exception e) {
            LOGGER.error("getConfigurationFromStorage exception", e);
        }

        return config;
    }


    private String generateFileName(String tenantId, String appId, String extraExtension) {
        String prefix = getFileNamePrefix(tenantId, appId);
        String extra =
            extraExtension != null && extraExtension.length() > 0 ? "." + extraExtension : "";

        return PROFILES_DIRECTORY + File.separator + prefix + extra + PROFILE_EXT;
    }

    private String getFileNamePrefix(String tenantId, String appId) {
        return PROFILE_PREFIX + tenantId + "." + appId;
    }

    private void mergeConfigs(Configuration source, Configuration destination) {
        // Merge the receieved with the stored .. By doing this we prevent users from changing
        // system set values for:
        // profileId, created, lastUpdated
        destination.setProfileName(source.getProfileName());
        destination.setBaAttributes(source.getBaAttributes());
    }

    /**
     * Helper method to log a warning message if a config doesnt parse.
     *
     * @param config json as string
     * @param messageOnException what messsage to print in the warning
     * @return Configuration non-null on success
     */

    private Configuration performConfigValidationCheck(String configAsString,
        String messageOnException) {
        return performConfigValidationCheck(configAsString, messageOnException, null);
    }

    /**
     * Helper method to log a warning message if a config doesnt parse.
     *
     * @param configAsString
     * @param messageOnException
     * @param errorMessageBuilder for callers that want the warning on error
     * @return Configuration non-null on success
     */

    private Configuration performConfigValidationCheck(String configAsString,
        String messageOnException, StringBuilder errorMessageBuilder) {
        // This will perform some validation on the passed input string. If exceptions are found,
        // just log a warning message. The reason for just the warning is because we need invalid
        // configs for
        // negative testing.

        Configuration config = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readValue(configAsString, Configuration.class);
            // if we got this far assume parsing was success
        } catch (Exception e) {
            String errorMessage = messageOnException + " : " + e.getMessage();

            if (errorMessageBuilder != null) {
                errorMessageBuilder.append(errorMessage);
            }

            LOGGER.warn(errorMessage);
        }

        return config;
    }



    /**
     * Called by the constructor of this abstract class, implementers should make calls to
     * addEndPointRecord
     * registering their end points
     */

    protected void registerAllEndPoints() {
        // GET ALL: http://brido02-win7:5000/configs/ (best viewed in browser)
        addEndPointRecord(getClass().getName(), new EndPointRecord("GET", CONFIGS_CONTEXT + "",
            "returns all configs"));

        // GET (SINGLE): http://brido02-win7:5000/configs/<tenantId>/<appId>
        addEndPointRecord(getClass().getName(), new EndPointRecord("GET", CONFIGS_CONTEXT
            + "/yourTenantId/yourAppId", "return just that profile/config"));

        // POST Create new profile, POST http://brido02-win7:5000/configs/<tenantId>/<appId>
        addEndPointRecord(
            getClass().getName(),
            new EndPointRecord("POST", CONFIGS_CONTEXT + "/yourTenantId/yourAppId",
                "creates default config by that tenant/app. Optional, can include config json in POST payload"));

        // PUT http://brido02-win7:5000/configs/<tenantId>/<appId> with updated json
        addEndPointRecord(getClass().getName(), new EndPointRecord("PUT", CONFIGS_CONTEXT
            + "/yourTenantId/yourAppId", "updates the config with the required full json payload"));

        // Delete a profile DELETE http://brido02-win7:5000/configs/<tenantId>/<appId>
        addEndPointRecord(getClass().getName(), new EndPointRecord("DELETE", CONFIGS_CONTEXT
            + "/yourTenantId/yourAppId", "deletes the config by that tenant/app"));
    }


}
