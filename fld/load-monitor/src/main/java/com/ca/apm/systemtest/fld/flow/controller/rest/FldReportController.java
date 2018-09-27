package com.ca.apm.systemtest.fld.flow.controller.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ca.apm.systemtest.fld.flow.controller.dao.ReportEmailRecipientsDao;
import com.ca.apm.systemtest.fld.flow.controller.model.ReportEmailRecipient;
import com.ca.apm.systemtest.fld.flow.controller.service.FldReportService;
import com.ca.apm.systemtest.fld.flow.controller.vo.BaseVO;
import com.ca.apm.systemtest.fld.flow.controller.vo.ErrorInfo;
import com.ca.apm.systemtest.fld.flow.controller.vo.ReportEmailRecipientVO;
import com.ca.apm.systemtest.fld.flow.controller.vo.ReportEmailRecipientsVO;

/**
 * REST controller for sending report emails through POST requests.
 * 
 * 1. http://localhost:8080/loadmon/api/reports/report-daily-test?almId=012345&desc=Executing%20a%20set%20of%20UI%20tests%20over%20WebView&passed=true
 * 2. http://localhost:8080/loadmon/api/reports/report-load-launch?loadName=Transaction%20Trace%20Load&desc=Generating%20transaction%20storm%20load&startedAt=2017-02-20&finishesAt=2017-02-20
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@RestController
@RequestMapping("/reports")
public class FldReportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FldReportController.class);

    @Autowired
    private ReportEmailRecipientsDao reportEmailRecipientsDao;

    @Autowired
    private FldReportService fldReportService;

    @Autowired 
    private ServletContext servletContext;

    @RequestMapping(value = "/report-daily-test", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseVO reportDailyTest(
        @RequestParam(value = "almId", required = true) String almId,
        @RequestParam(value = "desc", required = true) String desc,
        @RequestParam(value = "passed", required = true, defaultValue = "false") Boolean passed) throws MessagingException {

        LOGGER.info("FLD Report controller | reportDailyTest: almId = {}, desc = '{}', passed = {}", 
            almId, desc, passed);
        
        Map<String, String> contextData = new HashMap<>();
        contextData.put("almId", almId);
        contextData.put("description", desc);
        contextData.put("passed", String.valueOf(passed));
        contextData.put("subject", "Test FLD report");
        
        fldReportService.sendDailyTestReportEmail(contextData);
        
        return new BaseVO("OK");
    }

    @RequestMapping(value = "/report-load-launch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseVO reportLoadLaunch(
        @RequestParam(value = "loadName", required = true) String loadName,
        @RequestParam(value = "desc", required = true) String desc,
        @RequestParam(value = "startedAt", required = true) String startedAt,
        @RequestParam(value  = "finishesAt", required = true) String finishesAt) throws MessagingException {

        LOGGER.info("FLD Report controller | reportLoadLaunch: loadName = {}, desc = '{}', startedAt = {}, finishesAt = {}", 
            loadName, desc, startedAt, finishesAt);
        
        Map<String, String> contextData = new HashMap<>();
        contextData.put("loadName", loadName);
        contextData.put("description", desc);
        contextData.put("startedAt", startedAt);
        contextData.put("finishesAt", finishesAt);
        contextData.put("subject", "Test FLD report");
        
        fldReportService.sendLoadLaunchReportEmail(contextData, servletContext);

        return new BaseVO("OK");
    }

    @RequestMapping(value = "/notify-load-info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseVO notifyLoadInfo(
        @RequestParam(value = "loadName", required = true) String loadName,
        @RequestParam(value = "status", required = true) String status,
        @RequestParam(value = "timestamp", required = true) 
        @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") Date timestamp) throws MessagingException {

        LOGGER.info("FLD Report controller | notifyLoadInfo: loadName = {}, status = '{}', timestamp = {}", 
            loadName, status, timestamp);
        
        fldReportService.notifyLoadInfo(loadName, status, timestamp);
        return new BaseVO("OK");
    }

    @RequestMapping(value = "/email-recipients", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    ReportEmailRecipientsVO getReportEmailRecipients() {
        List<ReportEmailRecipient> recipients = reportEmailRecipientsDao.findAll();
        List<ReportEmailRecipientVO> recipientVOs = new ArrayList<>(recipients != null ? recipients.size() : 0);
        if (recipients != null) {
            for (ReportEmailRecipient recipient : recipients) {
                recipientVOs.add(new ReportEmailRecipientVO(recipient.getId(), recipient.getName(), recipient.getSurname(), recipient.getEmail()));
            }
        }
        return new ReportEmailRecipientsVO(recipientVOs);
    }

    @RequestMapping(value = "/add-email-recipient", method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    BaseVO addReportEmailRecipient(@RequestParam(value = "name", required = false) String name, 
                                   @RequestParam(value = "surname", required = false) String surname, 
                                   @RequestParam(value = "email", required = true) String email) {
        LOGGER.info("FLD Report controller | persisting a new report recipient: name='{}', surname='{}', email='{}'", name, surname, email);
        reportEmailRecipientsDao.create(new ReportEmailRecipient(name, surname, email));
        return new BaseVO("OK");
    }

    @RequestMapping(value = "/remove-email-recipient", method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
    BaseVO removeReportEmailRecipientById(@RequestParam(value = "id", required = true) Long id) {
        LOGGER.info("FLD Report controller | removing report recipient by: id='{}'", id);
        reportEmailRecipientsDao.deleteById(id);
        return new BaseVO("OK");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorInfo> handleException(Throwable ex) {
        LOGGER.error("FLD Report Controller | unexpected exception: ", ex);

        ErrorInfo message = new ErrorInfo(null, ex.getMessage());

        ResponseEntity<ErrorInfo> retval =
            new ResponseEntity<ErrorInfo>(message, HttpStatus.INTERNAL_SERVER_ERROR);

        return retval;
    }

    
}
