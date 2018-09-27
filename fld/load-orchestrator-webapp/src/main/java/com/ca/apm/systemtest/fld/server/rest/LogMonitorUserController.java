/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ca.apm.systemtest.fld.server.dao.LogMonitorUserDao;
import com.ca.apm.systemtest.fld.server.model.LogMonitorUser;
import com.ca.apm.systemtest.fld.server.model.LogMonitorUserEmail;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.LogMonitorRecipientVO;
import com.ca.apm.systemtest.fld.shared.vo.LogMonitorRecipientResponse;

/**
 * REST API for fetching information about log monitor recipients and operating on them.
 * 
 * @author TAVPA01
 * @author Alexander Sinyushkin (sinal04@ca.com) 
 *
 */
@RestController("logMonitorRestController")
public class LogMonitorUserController {

    private Logger LOGGER = LoggerFactory.getLogger(LogMonitorUserController.class);
    
    @Autowired
	private LogMonitorUserDao logMonitorDao;
	
	@Autowired
	private SimpleMailMessage messageTemplate;

    @Autowired
    private Mapper mapper;

	public LogMonitorUserController() {
	}
	
	/**
	 * Returns a log monitor recipient by its persistence id. 
	 * 
	 * @param recipientId   recipient's id
	 * @return
	 */
	@RequestMapping(value="/logmonitor/recipients/{recipientId}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@Transactional(propagation=Propagation.REQUIRED)
	public ResponseEntity<LogMonitorRecipientVO> getLogMonitorRecipientById(@PathVariable("recipientId") Long recipientId) {
		LogMonitorUser user = logMonitorDao.find(recipientId);
		if (user == null) {
			throw new LogMonitorUserException("No log monitor recipient with id=" + recipientId + " found!");
		}
		
		LogMonitorRecipientVO recipientVO = convertLogMonitorUserToRecipientVO(user);
		return new ResponseEntity<LogMonitorRecipientVO>(recipientVO, HttpStatus.OK);
	}

	/**
	 * Returns whether all log monitor recipients persisted in the database (if any) or all recipients hardcoded 
	 * in the application context file if no persisted recipients are found. Either one or another set of recipients 
	 * is used by the application. When sending a notification email, the app first tries to get the list of all 
	 * persisted ones. If there are no such, it uses the hardcoded recipients (if any).  
	 * 
	 * @return log monitor recipients
	 */
	@RequestMapping(value = "/logmonitor/recipients", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<LogMonitorRecipientResponse> getLogMonitorRecipients() {
		List<LogMonitorUser> persistedRecipients = logMonitorDao.findAll();
		String[] templateToEmailAddresses = messageTemplate.getTo();
		int size = 0;
		if (persistedRecipients != null) {
		    size += persistedRecipients.size();
		}
		if (templateToEmailAddresses != null) {
		    size += templateToEmailAddresses.length;
		}
		
		Collection<LogMonitorRecipientVO> recipientVOs = new ArrayList<LogMonitorRecipientVO>(size);
		if (persistedRecipients != null && !persistedRecipients.isEmpty()) {
		    /*
		     * Ok, we have some persisted log monitor recipients. They are used by the app, so send them.
		     */
		    for (LogMonitorUser u : persistedRecipients) {
	            LogMonitorRecipientVO recipientVO = convertLogMonitorUserToRecipientVO(u);
		        recipientVOs.add(recipientVO);
	        }
		} else if (templateToEmailAddresses != null) {
		    /*
		     * There are no recipients persisted in the DB, but there might be some hardcoded values
		     * which we might like to see as well.
		     */
		    for (String toEmailAddress : templateToEmailAddresses) {
		        recipientVOs.add(new LogMonitorRecipientVO(null, null, null, null, null, "Application context defined", 
		            Collections.singletonList(toEmailAddress)));
		    }
		}

		LogMonitorRecipientResponse recp = new LogMonitorRecipientResponse();
		recp.setStatus(HttpStatus.OK);
		recp.setRecipients(recipientVOs);
		return new ResponseEntity<LogMonitorRecipientResponse>(recp, HttpStatus.OK);
	}

	/**
	 * Adds a new log monitor recipient.
	 * 
	 * @param recipientVO recipient to persist
	 * @return
	 */
    @RequestMapping(value = "/logmonitor/addRecipient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<LogMonitorRecipientResponse> addLogMonitorRecipient(@RequestBody(required = true) LogMonitorRecipientVO recipientVO) {
        LogMonitorUser user = addRecipient(recipientVO);
        LogMonitorRecipientResponse response = new LogMonitorRecipientResponse();
        response.setRecipient(convertLogMonitorUserToRecipientVO(user));
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<LogMonitorRecipientResponse>(response, HttpStatus.OK); 
    }
	
    /**
     * Updates existing log monitor recipient.
     * 
     * @param   recipientVO  log monitor recipient's data
     * @return
     */
    @RequestMapping(value = "/logmonitor/updateRecipient", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<LogMonitorRecipientResponse> updateLogMonitorRecipient(@RequestBody(required = true) LogMonitorRecipientVO recipientVO) {
        boolean updated = updateRecipient(recipientVO);
        
        LogMonitorRecipientResponse response = new LogMonitorRecipientResponse();
        response.setCount(updated ? 1 : 0);
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<LogMonitorRecipientResponse>(response, HttpStatus.OK); 
    }
    
    /**
     * Deletes all log monitor recipients.
     * 
     * @return
     */
    @RequestMapping(value = "/logmonitor/deleteAllRecipients", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<LogMonitorRecipientResponse> deleteAllRecipients() {
        int count = logMonitorDao.deleteAll();
        
        LogMonitorRecipientResponse response = new LogMonitorRecipientResponse();
        response.setStatus(HttpStatus.OK);
        response.setCount(count);
        return new ResponseEntity<LogMonitorRecipientResponse>(response, HttpStatus.OK); 
    
    }    
    
    /**
     * Deletes log monitor recipient by its persistence id.
     * 
     * @param recipientId  log monitor recipient id
     * @return
     */
    @RequestMapping(value = "/logmonitor/deleteRecipientById/{recipientId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<LogMonitorRecipientResponse> deleteRecipientById(@PathVariable("recipientId") Long recipientId) {
        LogMonitorUser logMonitorUser = logMonitorDao.find(recipientId);
        if (logMonitorUser == null) {
            throw new IllegalStateException("Recipient with id=" + recipientId + " does not exist!");
        }

        logMonitorDao.delete(logMonitorUser);
        
        LogMonitorRecipientResponse response = new LogMonitorRecipientResponse();
        response.setCount(1);
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<LogMonitorRecipientResponse>(response, HttpStatus.OK); 

    }    
    
    /**
     * Deletes a log monitor recipient(s) matched by the provided email address.
     * If more recipients found, all of them are removed and the number of the removed recipients 
     * will be set in the response.
     *  
     * @return
     */
    @RequestMapping(value = "/logmonitor/deleteRecipientByEmail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<LogMonitorRecipientResponse> deleteRecipientByEmail(@RequestParam(required = true, value = "email") String email) {
        LogMonitorUser user = logMonitorDao.findByEmail(email);
        if (user == null) {
            String message = "No such log monitor recipient with email='" + email + "' found!";
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        
        logMonitorDao.delete(user);
        
        LogMonitorRecipientResponse response = new LogMonitorRecipientResponse();
        response.setCount(1);
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<LogMonitorRecipientResponse>(response, HttpStatus.OK); 
    }    
    
    /**
     * Handles any other generic exception.
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorMessage> handleAnyOtherException(Throwable ex) {
        LOGGER.error("LogMonitorUserController: unexpected exception", ex);

        ErrorMessage em = new ErrorMessage();

        em.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        List<String> errors = new ArrayList<>(1);
        errors.add(ex.getMessage());
        em.setErrors(errors);

        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.INTERNAL_SERVER_ERROR);

        return retval;
    }

    /**
     * Handles this REST controller's runtime exceptions.
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler(LogMonitorUserException.class)
    public ResponseEntity<ErrorMessage> handleLogMonitorUserException(LogMonitorUserException ex) {
        LOGGER.error("LogMonitorUserController: unexpected exception", ex);

        ErrorMessage em = new ErrorMessage();

        em.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        List<String> errors = new ArrayList<>(1);
        errors.add(ex.getMessage());
        em.setErrors(errors);

        ResponseEntity<ErrorMessage> retval =
            new ResponseEntity<ErrorMessage>(em, HttpStatus.INTERNAL_SERVER_ERROR);

        return retval;
    }

    private boolean updateRecipient(LogMonitorRecipientVO recipientVO) {
        if (recipientVO == null) {
            String message = "Recipient is null!";
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getId() == null) {
            String message = "Recipient's id is null! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getId() < 0) {
            String message = "Recipient's id is invalid! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getEmailAddresses() == null) {
            String message = "Recipient can not have null email addresses! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getEmailAddresses().isEmpty()) {
            String message = "Recipient can not have empty email addresses! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        
        LogMonitorUser user = logMonitorDao.find(recipientVO.getId());
        if (user == null) {
            String message = "No persisted log monitor recipient found for id=" + recipientVO.getId() + "! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        
        LogMonitorRecipientVO persistedRecipientVO = convertLogMonitorUserToRecipientVO(user);
        
        List<String> emails = new ArrayList<String>(recipientVO.getEmailAddresses());
        List<String> persistedEmails = new ArrayList<String>(persistedRecipientVO.getEmailAddresses());
        
        Collections.sort(emails);
        Collections.sort(persistedEmails);
        
        recipientVO.setEmailAddresses(emails);
        persistedRecipientVO.setEmailAddresses(persistedEmails);
            
        if (!persistedRecipientVO.equals(recipientVO)) {
            /*
             * Ok, there are some changes, go save them.
             */
            user.setName(recipientVO.getName());
            user.setSurname(recipientVO.getSurname());
            user.setReason(recipientVO.getReason());
            user.setTelephone(recipientVO.getTelephone());

            List<LogMonitorUserEmail> recreateUserEmails = new ArrayList<LogMonitorUserEmail>(emails.size());
            
            for (String email : emails) {
                recreateUserEmails.add(new LogMonitorUserEmail(email));
            }
            user.setEmailEntries(recreateUserEmails);
            
            logMonitorDao.update(user);
            return true;
        }
        return false;
    }
    
    private LogMonitorUser addRecipient(LogMonitorRecipientVO recipientVO) {
        if (recipientVO == null) {
            String message = "Recipient can not be null!";
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getEmailAddresses() == null) {
            String message = "Recipient can not have null email addresses! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        if (recipientVO.getEmailAddresses().isEmpty()) {
            String message = "Recipient can not have empty email addresses! Recipient: " + recipientVO;
            LOGGER.error(message);
            throw new LogMonitorUserException(message);
        }
        
        Collection<LogMonitorUserEmail> emails = new ArrayList<LogMonitorUserEmail>(recipientVO.getEmailAddresses().size());
        
        for (String email : recipientVO.getEmailAddresses()) {
            LogMonitorUser logMonitorUser = logMonitorDao.findByEmail(email);
            if (logMonitorUser != null) {
                String message = "Log monitor recipient with the email='" + email + "' already exists: " + logMonitorUser;
                LOGGER.error(message);
                throw new LogMonitorUserException(message);
            }
            
            emails.add(new LogMonitorUserEmail(email));
        }

        LogMonitorUser recipientEntity = null;
        synchronized (mapper) {
            recipientEntity = mapper.map(recipientVO, LogMonitorUser.class);
        }
        
        recipientEntity.setId(null);
        recipientEntity.setEmailEntries(emails);
        
        if (recipientEntity.getReason() == null) {
            recipientEntity.setReason("Automatic Reason: this recipient is added through a REST call");
        }
        recipientEntity.setTimestamp(new Date().getTime());
        
        LOGGER.info("About to persist a new log monitor recipient: {}", recipientEntity);
        
        logMonitorDao.create(recipientEntity);
        return recipientEntity;
    }
    
    private Collection<String> getEmailsFromLogMonitorUser(LogMonitorUser user) {
        List<String> emails = new ArrayList<String>(user.getEmailEntries() != null ? user.getEmailEntries().size() : 0);
        if (user.getEmailEntries() != null) {
            for (LogMonitorUserEmail userEmail : user.getEmailEntries()) {
                emails.add(userEmail.getEmailAddress());
            }
        }
        return emails;
    }

    private LogMonitorRecipientVO convertLogMonitorUserToRecipientVO(LogMonitorUser user) {
        LogMonitorRecipientVO recipientVO = null;
        synchronized (mapper) {
            recipientVO = mapper.map(user, LogMonitorRecipientVO.class);
        }
        
        recipientVO.setEmailAddresses(getEmailsFromLogMonitorUser(user));
        return recipientVO;
    }
}
