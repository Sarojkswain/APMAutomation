package com.ca.apm.systemtest.fld.common;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.LoggerMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public final class LoggerMonitorUtils {

    public static final int MAX_MESSAGE_LENGTH = 63535;
    public static final int MAX_CATEGORY_LENGTH = 256;
    public static final int MAX_TAG_LENGTH = 256;
    public static final int MAX_NODE_NAME_LENGTH = 256;
    public static final int MAX_PROCESS_ID_LENGTH = 256;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerMonitorUtils.class);

    public static String convertLogtoJSON(LoggerMessage message) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(message);
    }


    public static String convertLogtoJSON(LoggerMessage message, boolean pretty)
        throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonLog = null;

        if (pretty) {
            jsonLog = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } else {
            jsonLog = mapper.writeValueAsString(message);
        }
        return jsonLog;
    }


    public static LoggerMessage convertJSONtoLog(String json) throws JsonParseException,
        JsonMappingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, LoggerMessage.class);

    }


    public static String getLevel(String reqLevel) {
        String filterLevel = null;
        for (FldLevel level : FldLevel.values()) {
            if (level.name().equals(reqLevel)) {
                filterLevel = level.toString();
                break;
            }
        }
        return filterLevel;
    }

    public static String preparePersistentLogMessage(String message, int maxLength) {
        if (message != null && message.length() > maxLength) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.debug("Truncating string which exceeds MAX length of {}, original: {}",
                    maxLength, message);
            }
            return message.substring(0, maxLength);
        }
        return message;
    }
    
}
