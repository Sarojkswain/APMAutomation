package com.ca.apm.systemtest.fld.server.rest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ca.apm.systemtest.fld.server.dao.NetworkTrafficMonitorDao;
import com.ca.apm.systemtest.fld.server.model.NetworkTrafficMonitorValue;
import com.ca.apm.systemtest.fld.server.rest.NetworkTrafficMonitorException.ErrorCode;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.Response;

@Controller
public class NetworkTrafficMonitorController {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(NetworkTrafficMonitorController.class);

    @Autowired
    private NetworkTrafficMonitorDao<NetworkTrafficMonitorValue> networkTrafficMonitorDao;

    /**
     * Add or update network traffic monitor image.
     * 
     * @param host
     * @param remoteHost
     * @param type
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/networktrafficmonitor/{host}/{remoteHost}/{type}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResponseEntity<Response> uploadImage(@PathVariable String host,
        @PathVariable String remoteHost, @PathVariable String type,
        @RequestParam MultipartFile imageFile, @RequestParam String description) {
        LOGGER.debug("NetworkTrafficMonitorController.uploadImage():: entry");
        LOGGER
            .debug(
                "NetworkTrafficMonitorController.uploadImage():: host = {}, remoteHost = {}, type = {}",
                host, remoteHost, type);
        try {
            NetworkTrafficMonitorValue value =
                new NetworkTrafficMonitorValue(host, remoteHost, type);
            if (StringUtils.hasText(description)) {
                value.setDescription(description);
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    value.setImage(imageFile.getBytes());
                } catch (IOException e) {
                    String message =
                        MessageFormat.format("Unable to read {0} to image: {1}", imageFile, e);
                    LOGGER.warn(message);
                    throw new NetworkTrafficMonitorException(ErrorCode.InvalidParameter, message);
                }
            }
            try {
                networkTrafficMonitorDao.update(value);
            } catch (IOException e) {
                String message = MessageFormat.format("Unable to store {0}: {1}", value, e);
                LOGGER.warn(message);
                throw new NetworkTrafficMonitorException(ErrorCode.UnknownError, message);
            }
            return getOKResponse();
        } finally {
            LOGGER.debug("NetworkTrafficMonitorController.uploadImage():: exit");
        }
    }

    /**
     * Get network traffic monitor image.
     * 
     * @param host
     * @param remoteHost
     * @param type
     * @return
     */
    @RequestMapping(value = "/networktrafficmonitor/{host}/{remoteHost}/{type}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public byte[] getImage(@PathVariable String host, @PathVariable String remoteHost,
        @PathVariable String type) {
        LOGGER.debug("NetworkTrafficMonitorController.getImage():: entry");
        LOGGER.debug(
            "NetworkTrafficMonitorController.getImage():: host = {}, remoteHost = {}, type = {}",
            host, remoteHost, type);
        try {
            NetworkTrafficMonitorValue value;
            try {
                value = networkTrafficMonitorDao.find(host, remoteHost, type);
            } catch (IOException e) {
                String message =
                    MessageFormat.format(
                        "Unable to get image for host = {0}, remoteHost = {1}, type = {2}: {3}",
                        host, remoteHost, type, e);
                LOGGER.warn(message);
                throw new NetworkTrafficMonitorException(ErrorCode.UnknownError, message);
            }
            if (value == null) {
                String message =
                    MessageFormat.format(
                        "Image not found for host = {0}, remoteHost = {1}, type = {2}", host,
                        remoteHost, type);
                LOGGER.warn(message);
                throw new NetworkTrafficMonitorException(ErrorCode.DataNotFound, message);
            }
            return value.getImage();
        } finally {
            LOGGER.debug("NetworkTrafficMonitorController.getImage():: exit");
        }
    }

    /**
     * Get network traffic monitor image description.
     * 
     * @param host
     * @param remoteHost
     * @param type
     * @return
     */
    @RequestMapping(value = "/networktrafficmonitor/{host}/{remoteHost}/{type}/description", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getDescription(@PathVariable String host, @PathVariable String remoteHost,
        @PathVariable String type) {
        LOGGER.debug("NetworkTrafficMonitorController.getDescription():: entry");
        LOGGER
            .debug(
                "NetworkTrafficMonitorController.getDescription():: host = {}, remoteHost = {}, type = {}",
                host, remoteHost, type);
        try {
            NetworkTrafficMonitorValue value;
            try {
                value = networkTrafficMonitorDao.find(host, remoteHost, type);
            } catch (IOException e) {
                String message =
                    MessageFormat
                        .format(
                            "Unable to get description for host = {0}, remoteHost = {1}, type = {2}: {3}",
                            host, remoteHost, type, e);
                LOGGER.warn(message);
                throw new NetworkTrafficMonitorException(ErrorCode.UnknownError, message);
            }
            if (value == null) {
                String message =
                    MessageFormat.format(
                        "Description not found for host = {0}, remoteHost = {1}, type = {2}", host,
                        remoteHost, type);
                LOGGER.warn(message);
                throw new NetworkTrafficMonitorException(ErrorCode.DataNotFound, message);
            }
            return value.getDescription();
        } finally {
            LOGGER.debug("NetworkTrafficMonitorController.getDescription():: exit");
        }
    }

    /**
     * Get simple HTML page with current results.
     * 
     * @return
     */
    @RequestMapping(value = "/networktrafficmonitor/summary", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getSummary(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<title>FLD network traffic monitoring</title>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("<p/>\n");
        sb.append("<h2>FLD network traffic monitoring</h2>\n");

        // sb.append("<h4>bla bla</h4>\n");
        // sb.append("<h5>bla bla bla</h5>\n");

        List<NetworkTrafficMonitorValue> values = networkTrafficMonitorDao.findAll();
        if (values == null || values.isEmpty()) {
            sb.append("<br><hr><br><h3><font color=\"red\">No data found..</font></h3><br><hr>\n");
        } else {
            Collections.sort(values);
            for (NetworkTrafficMonitorValue value : values) {
                sb.append("<br><hr>\n");
                String url = getParticularBaseUrl(contextPath, value);
                String title =
                    value.getType() + ": " + value.getHost() + "  &lt==&gt  "
                        + value.getRemoteHost();
                LOGGER.debug("NetworkTrafficMonitorController.getSummary():: url = {}", url);
                LOGGER.debug("NetworkTrafficMonitorController.getSummary():: title = {}", title);

                sb.append("<p/><br/>\n");
                sb.append("<h4>" + title + " - <a href =\"" + url
                    + "/description\">Description</a></h4>\n");
                sb.append("<img alt=\"" + url + "\" src=\"" + url + "/image\" /><br><br>\n");
            }
            sb.append("<br><hr>\n");
            sb.append("<p/><br/>");
        }
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    /**
     * Process exception.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(NetworkTrafficMonitorException.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(
        NetworkTrafficMonitorException exception) {
        LOGGER.warn("An application exception has occured", exception);
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setStatus(HttpStatus.BAD_REQUEST);
        List<String> errors = new ArrayList<>();
        errors.add(exception.getMessage());
        errorMessage.setErrors(errors);
        return getBadRequestResponse(errorMessage);
    }

    private static ResponseEntity<Response> getOKResponse() {
        return new ResponseEntity<Response>(new Response(HttpStatus.OK.toString()), HttpStatus.OK);
    }

    private static ResponseEntity<ErrorMessage> getBadRequestResponse(ErrorMessage errorMessage) {
        return new ResponseEntity<ErrorMessage>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    private static String getParticularBaseUrl(String contextPath, NetworkTrafficMonitorValue value) {
        return contextPath + "/api/networktrafficmonitor/" + value.getHost() + "/"
            + value.getRemoteHost() + "/" + value.getType();
    }

}
