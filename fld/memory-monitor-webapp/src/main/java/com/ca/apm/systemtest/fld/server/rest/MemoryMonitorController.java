package com.ca.apm.systemtest.fld.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ca.apm.systemtest.fld.server.dao.MemoryMonitorDao;
import com.ca.apm.systemtest.fld.server.model.MemoryMonitorValue;
import com.ca.apm.systemtest.fld.server.rest.MemoryMonitorException.ErrorCode;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.Response;

@Controller
public class MemoryMonitorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitorController.class);

    private static final int MAX_LENGTH_GROUP = 20;
    private static final int MAX_LENGTH_ID = 30;

    @Autowired
    private MemoryMonitorDao<MemoryMonitorValue> memoryMonitorDao;

    private String getMemoryMonitorId(String group, String id) throws MemoryMonitorException {
        StringBuilder mmId = new StringBuilder();
        if (group.length() < MAX_LENGTH_GROUP && !group.contains("_")) {
            mmId.append(group);
        } else {
            String msg = "Invalid group '" + group + "' is too long";
            LOGGER.warn(msg);
            throw new MemoryMonitorException(ErrorCode.InvalidParameter, msg);
        }
        mmId.append('_');
        if (!id.isEmpty() && id.length() < MAX_LENGTH_ID) {
            mmId.append(id);
        } else {
            String msg = "Invalid ID '" + id + "'";
            LOGGER.warn(msg);
            throw new MemoryMonitorException(ErrorCode.InvalidParameter, msg);
        }
        return mmId.toString();
    }

    /**
     * Add or update memory monitor image.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResponseEntity<Response> uploadImage(@PathVariable String id,
        @RequestParam MultipartFile imageFile, @RequestParam String description) {
        return uploadImage("", id, imageFile, description);
    }

    /**
     * Add or update memory monitor image.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{group}/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResponseEntity<Response> uploadImage(@PathVariable String group,
        @PathVariable String id, @RequestParam MultipartFile imageFile,
        @RequestParam String description) {
        LOGGER.info("MemoryMonitorController.uploadImage():: entry");
        LOGGER.debug("MemoryMonitorController.uploadImage():: group = {}, id = {}", group, id);
        try {
            MemoryMonitorValue value = new MemoryMonitorValue();
            value.setId(getMemoryMonitorId(group, id));
            if (description != null && !description.isEmpty()) {
                value.setDescription(description);
            }
            try {
                if (imageFile != null && !imageFile.isEmpty()) {
                    value.setImage(imageFile.getBytes());
                }
            } catch (IOException e) {
                String msg = "Unable to read " + imageFile + " to image: " + e;
                LOGGER.warn(msg);
                throw new MemoryMonitorException(ErrorCode.InvalidParameter, msg);
            }
            try {
                memoryMonitorDao.update(value);
            } catch (IOException e) {
                String msg = "Unable to store " + value + ": " + e;
                LOGGER.warn(msg);
                throw new MemoryMonitorException(ErrorCode.InvalidParameter, msg);
            }
            Response response = new Response();
            response.setStatus(HttpStatus.OK);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        } finally {
            LOGGER.info("MemoryMonitorController.uploadImage():: exit");
        }
    }

    /**
     * Get memory monitor image.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{id}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public byte[] getImage(@PathVariable String id) {
        return getImage("", id);
    }

    /**
     * Get memory monitor image.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{group}/{id}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public byte[] getImage(@PathVariable String group, @PathVariable String id) {
        MemoryMonitorValue value;
        try {
            value = memoryMonitorDao.find(getMemoryMonitorId(group, id));
        } catch (IOException e) {
            LOGGER.warn("Unable to get image for group = {}, id = {}: {}", group, id, e);
            value = null;
        }
        if (value == null) {
            String msg = "Unable to find image with id '" + getMemoryMonitorId(group, id) + "'";
            LOGGER.warn(msg);
            throw new MemoryMonitorException(ErrorCode.ImageNotFound, msg);
        }
        return value.getImage();
    }

    /**
     * Get memory monitor image description.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{id}/description", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getDescription(@PathVariable String id) {
        return getDescription("", id);
    }

    /**
     * Get memory monitor image description.
     * 
     * @param role
     * @param imageFile
     * @param description
     * @return
     */
    @RequestMapping(value = "/memorymonitor/{group}/{id}/description", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getDescription(@PathVariable String group, @PathVariable String id) {
        MemoryMonitorValue value;
        try {
            value = memoryMonitorDao.find(getMemoryMonitorId(group, id));
        } catch (IOException e) {
            LOGGER.warn("Unable to get description for group = {}, id = {}: {}", group, id, e);
            value = null;
        }
        if (value == null) {
            String msg = "Unable to find image with id '" + id + "'";
            LOGGER.warn(msg);
            throw new MemoryMonitorException(ErrorCode.ImageNotFound, msg);
        }
        return value.getDescription();
    }

    /**
     * Get simple HTML page with current results.
     * 
     * @return
     */
    @RequestMapping(value = "/memorymonitor/summary", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getSummary(HttpServletRequest request) {
        return getSummary("", request);
    }

    /**
     * Get simple HTML page with current results.
     * 
     * @return
     */
    @RequestMapping(value = "/memorymonitor/summary/{group}", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.SUPPORTS)
    @ResponseBody
    public String getSummary(@PathVariable String group, HttpServletRequest request) {
        StringBuilder result = new StringBuilder();
        result.append("<html><head><title>FLD Cluster Heap Utilization</title></head>\n");
        result.append("<body><p/><h2>FLD Cluster Heap Utilization</h2>\n");
        result.append("<h4>Time line for each graph is at the top of its image. For many its "
            + "greyed out (program bug?). It exists for atleast one graph. "
            + "Refer that for others too. </h4>\n");
        result.append("<h5>Red - Total Heap </h5>\n");
        result.append("<h5>Blue - Used Heap </h5>\n");
        result.append("<h5>Green - GC Times Line </h5>\n");
        result.append("<h5>Black - Full GC Lines </h5>\n");
        result.append("<h5>Grey - Used Young Heap</h5>\n");
        String groupFilter = group + "_";
        String contextPath = request.getContextPath();
        List<MemoryMonitorValue> values = memoryMonitorDao.findAll();
        Collections.sort(values, new Comparator<MemoryMonitorValue>() {
            @Override
            public int compare(MemoryMonitorValue o1, MemoryMonitorValue o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        for (MemoryMonitorValue value : values) {
            String id = value.getId();
            if (!id.startsWith(groupFilter)) {
                continue;
            }
            String idUrl = id.replace('_', '/');
            result.append("<p/><br/>\n");
            result.append("<h3> " + id.replace("_", id.startsWith("_") ? "" : ", ").toUpperCase()
                + " Heap - <a href =\"" + contextPath + "/api/memorymonitor/" + idUrl
                + "/description\">" + idUrl + "_gc_summary</a></h3>\n");
            result.append("<img alt=\"" + idUrl + "_gc\" src=\"" + contextPath
                + "/api/memorymonitor/" + idUrl + "/image\" />\n");
        }
        result.append("<p/><br/></body></html>");
        return result.toString();
    }

    /**
     * Process exception.
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(MemoryMonitorException.class)
    public ResponseEntity<ErrorMessage> handleApplicationException(MemoryMonitorException ex) {
        LOGGER.warn("An application exception has occured", ex);
        ErrorMessage em = new ErrorMessage();
        em.setStatus(HttpStatus.BAD_REQUEST);
        List<String> errors = new ArrayList<>();
        errors.add(ex.getMessage());
        em.setErrors(errors);
        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.BAD_REQUEST);
        return retval;
    }

}
