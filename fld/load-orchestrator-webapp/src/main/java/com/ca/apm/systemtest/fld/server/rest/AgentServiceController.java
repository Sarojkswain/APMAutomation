/**
 * 
 */
package com.ca.apm.systemtest.fld.server.rest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.activemq.util.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.agentdownload.AgentDownloadPlugin;
import com.ca.apm.systemtest.fld.plugin.util.ZipBuilder;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
import com.ca.apm.systemtest.fld.server.dao.AgentDistributionDao;
import com.ca.apm.systemtest.fld.server.dao.NodeDao;
import com.ca.apm.systemtest.fld.server.model.AgentDistribution;
import com.ca.apm.systemtest.fld.server.model.AgentDistributionData;
import com.ca.apm.systemtest.fld.server.model.Node;
import com.ca.apm.systemtest.fld.shared.vo.ErrorMessage;
import com.ca.apm.systemtest.fld.shared.vo.Response;

import freemarker.template.TemplateException;

/**
 * This REST API provides the following methods<br>
 * <br>
 * PUT /api/agent          - adds a new version of the distro<br>
 * GET /api/agent          - retrieves the .zip archive containing the latest version, or HTTP 304 not modified. Client should set If-Modified-Since header<br>
 * 
 * @author KEYJA01
 *
 */
@RestController("agentRestController")
public class AgentServiceController {
	private static final Logger log = LoggerFactory.getLogger(AgentServiceController.class);

	public static final String DEFAULT_ACTIVE_MQ_BROKER_URL = "tcp://fldcontroll01c:61616";
	public static final String DEFAULT_FILE_DOWNLOAD_CACHE_URL = "http://fldcontroll01c:8080/LoadOrchestrator/filecache/download";
	public static final String DEFAULT_AGENT_DOWNLOAD_PATH = "/LoadOrchestrator/api/agent";
	public static final String DEFAULT_AGENT_DOWNLOAD_SERVER = "fldcontroll01c:8080";
	
	@Autowired
	private AgentDistributionDao agentDistroDao;

	@Autowired
	private NodeDao nodeService;
	
	@Autowired
	private NodeManager nodeManager;

	@Autowired
	AgentProxyFactory agentProxyFactory;
	
	@Autowired
	private PlatformTransactionManager txMgr;
	
	@Value("${activemq.broker.url}")
	private String activeMqURL;
	
	private WeakHashMap<Long, byte[]> distroCache = new WeakHashMap<Long, byte[]>();

	private WeakHashMap<String, byte[]> preConfiguredAgentDistro = new WeakHashMap<>();
    
	/**
	 * 
	 */
	public AgentServiceController() {
	}

	
	@RequestMapping(value="/agent", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public ResponseEntity<Response> uploadDistro(@RequestParam("agentZip") MultipartFile file) throws IOException {
	    log.info("Processing new agent distro upload");
		byte[] fBytes = file.getBytes();
		Long ts = getTsFromZip(fBytes);

		if (ts == null){
			SimpleDateFormat timestampVersion=new SimpleDateFormat(AgentDownloadPlugin.VERSION_DATE_FORMAT);
			ts = Long.parseLong(timestampVersion.format(new Date()));
		}

		AgentDistribution dist = new AgentDistribution();
		dist.setTimestamp(ts);
		AgentDistributionData distData = new AgentDistributionData();
		distData.setZipData(fBytes);
		dist.setData(distData);
		log.info("New Agent version registered: {}", ts);
        
        // Ensure the data is flushed to the DB before sending a response, so use a new transaction
		log.info("Starting new transaction for storing agent distro");
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus tx = txMgr.getTransaction(txDef);
		agentDistroDao.create(dist);
		txMgr.commit(tx);
        log.info("Committed");
		
		distroCache.put(dist.getTimestamp(), dist.getData().getZipData());

		// Notify all nodes
		List<Node> allNodes = nodeService.findAll();
		for (Node n : allNodes) {
		    if (!nodeManager.checkNodeAvailable(n.getName())) {
		        continue;
		    }
		    Long lastHeartbeat = n.getLastHeartbeat();
		    if (lastHeartbeat == null) {
		        lastHeartbeat = System.currentTimeMillis();
		    }
		    
		    if (System.currentTimeMillis() - lastHeartbeat < 180000L) {
	            String nodeName = n.getName();
	            AgentProxy nodeProxy = agentProxyFactory.createProxy(nodeName);
	            nodeProxy.getPlugin(AgentDownloadPlugin.class).downloadNewVersion();
	            log.info("Node {} notified about new Agent version", nodeName);
	            //set node status to updating
	            boolean isNodeUpdating = n.getIsAgentUpdating() != null ? n.getIsAgentUpdating() : false;
	            if (!isNodeUpdating) {
	                n.setIsAgentUpdating(true);
	                nodeService.update(n);
	            }
		    }
		}

		//Remove any preconfigured agent distro from cache if any
		preConfiguredAgentDistro.clear();
		
		Response resp = new Response();
		resp.setHasAgentDistribution(true);
		resp.setLatestAgentDistributionVersion(ts.toString());
		resp.setStatus(HttpStatus.OK);
		return new ResponseEntity<Response>(resp, HttpStatus.OK);
	}

	@RequestMapping(value="/preconfiguredAgent", method=RequestMethod.GET, produces="application/zip")
	@Transactional(propagation=Propagation.REQUIRED)
	public ResponseEntity<byte[]> getPreconfiguredDistribution(HttpServletRequest request) throws FileNotFoundException, 
	    ZipException, IOException, TemplateException {
	    
	    AgentDistribution dist = agentDistroDao.findNewest();
	    
	    if (dist == null) {
            throw new AgentDistroNotFoundException();
	    }
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", Long.toString(dist.getTimestamp()));
        headers.add("Content-Disposition", "attachment; filename=preconfigured-agent-dist.zip");
        
        String key = request.getLocalName() + ":" + request.getLocalPort() + ":" + dist.getTimestamp();
        byte[] buf = preConfiguredAgentDistro.get(key);
        if (buf != null) {
            ResponseEntity<byte[]> resp = new ResponseEntity<byte[]>(buf, headers, HttpStatus.OK);
            return resp;
        }
        //Get the latest agent distro data and version.
        byte[] latestAgentData = dist.getData().getZipData();
        
        String agentPropsText = generateAgentProperties(request);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipBuilder zipBuilder = new ZipBuilder(baos);
        zipBuilder.addFile("fld-agent-production.properties", agentPropsText);
        
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(latestAgentData));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            String name = entry.getName();
            if (entry.isDirectory()) {
                zipBuilder.addFolder(name);
            } else {
                zipBuilder.addFile(name, readStream(zip));
            }
        }
        
        zipBuilder.close();
        buf = baos.toByteArray();
        preConfiguredAgentDistro.put(key, buf);
        ResponseEntity<byte[]> resp = new ResponseEntity<byte[]>(buf, headers, HttpStatus.OK);
        
        return resp;
	}

    @RequestMapping(value="/agent", method=RequestMethod.GET, produces="application/zip")
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public ResponseEntity<byte[]> getLatestDistribution(@RequestHeader(value="Last-Modified", required=false) Long lastModified) {

		log.info("Checking for agent distribution newer than {}", lastModified);
		AgentDistribution dist = agentDistroDao.findNewest();
		if (dist != null) {
			if (lastModified == null || dist.getTimestamp() > lastModified) {
				log.info("Serving new version {}", dist.getTimestamp());
				HttpHeaders headers = new HttpHeaders();
				headers.add("ETag", Long.toString(dist.getTimestamp()));
				byte[] data = distroCache.get(dist.getTimestamp());
				if (data == null) {
				    data = dist.getData().getZipData();
				    distroCache.put(dist.getTimestamp(), data);
				}
				
				ResponseEntity<byte[]> resp = new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
				return resp;
			} else {
			    log.info("No newer distro found");
				throw new AgentDistroNotModifiedException();
			}
		} else {
			throw new AgentDistroNotFoundException();
		}
	}

	@RequestMapping(value="/hasAgentDistro", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public ResponseEntity<Response> hasAgentDistribution() {
	    AgentDistribution dist = agentDistroDao.findNewest();
	    
	    Response response = new Response();
        response.setStatus(HttpStatus.OK);
        if (dist != null) {
            response.setHasAgentDistribution(true);    
            response.setLatestAgentDistributionVersion(dist.getTimestamp() != null ? dist.getTimestamp().toString() : null);
        }
        
	    ResponseEntity<Response> resp = new ResponseEntity<Response>(response, HttpStatus.OK);
	    return resp;

	}

	@ExceptionHandler(AgentDistroNotFoundException.class)
	public ResponseEntity<ErrorMessage> handleNotFoundException() {
		ErrorMessage em = new ErrorMessage();
		List<String> errors = new ArrayList<>();
		errors.add("Not found");
		em.setErrors(errors);
		em.setStatus(HttpStatus.NOT_FOUND);
		ResponseEntity<ErrorMessage> retval = new ResponseEntity<ErrorMessage>(em, HttpStatus.NOT_FOUND);
		return retval;
	}


	@ExceptionHandler(AgentDistroIsAlreadyPreconfigured.class)
	public ResponseEntity<ErrorMessage> handleAgentDistroIsAlreadyPreconfiguredException(AgentDistroIsAlreadyPreconfigured exception) {
	    ErrorMessage em = new ErrorMessage();
	    List<String> errors = new ArrayList<>();
	    errors.add(exception.getMessage());
	    em.setErrors(errors);
	    em.setStatus(HttpStatus.EXPECTATION_FAILED);
	    ResponseEntity<ErrorMessage> retval = new ResponseEntity<ErrorMessage>(em, HttpStatus.EXPECTATION_FAILED);
	    return retval;
	}

    private byte[] readStream(ZipInputStream zip) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int num = 0;
        byte[] buf = new byte[4096];
        while ((num = zip.read(buf)) >= 0) {
            if (num > 0) {
                out.write(buf, 0, num);
            }
        }
        
        return out.toByteArray();
    }
	
	private String generateAgentProperties(HttpServletRequest request) throws IOException, TemplateException {
	    Properties props = new Properties();
	    
	    String host = request.getLocalName();
	    int port = request.getLocalPort();
	    String contextPath = request.getServletContext().getContextPath();
	    if (!contextPath.startsWith("/")) {
	        contextPath = "/" + contextPath;
	    }
	    
	    String fileCacheUrl = "http://" + host + ":" + port + contextPath + "/filecache/download";
	    String agentDownloadServer = host + ":" + port;
	    
	    props.put("activemq.broker.url", activeMqURL);
        props.put("fld.file.download.cache.url", fileCacheUrl);
        props.put("agent.download.server", agentDownloadServer);
        props.put("agent.download.path", contextPath + "/api/agent");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer out = new PrintWriter(baos);
        props.store(out, "No comment");
        out.flush();
        
        String text = baos.toString("US-ASCII");
        return text;
	}
	
	private Long getTsFromZip(byte[] fileBytes) throws IOException, FileNotFoundException, ZipException {
	        // Resolve timestamp from agent.jar/build.properties
	        File zFile = File.createTempFile("agent-", ".zip");
	        try (FileOutputStream fos = new FileOutputStream(zFile)) {
	            fos.write(fileBytes);
	        }
	        ZipFile zipFile = new ZipFile(zFile);
	        Enumeration<? extends ZipEntry> entries = zipFile.entries();
	        InputStream agentJar = null;
	        while (entries.hasMoreElements()) {
	            ZipEntry entry = entries.nextElement();
	            String nm = entry.getName();
	            if (nm.matches("lib.*agent-.*.jar") && nm.indexOf("plugin") == -1) {
	                agentJar = zipFile.getInputStream(entry);
	                break;
	            }
	        }
	        Long ts = null;
	        if (agentJar != null) {
	            File jFile = File.createTempFile("agent-", ".jar");
	            try (BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(jFile))) {
	                int len;
	                byte[] lBuf = new byte[1024 * 1024]; // 1MB buffer
	                while( (len=agentJar.read(lBuf)) != -1) {
	                    bOut.write(lBuf, 0, len);
	                }
	                bOut.close();
	                JarFile jarFile = new JarFile(jFile);
	                Enumeration<JarEntry> jEntries = jarFile.entries();
	                while (jEntries.hasMoreElements()) {
	                    JarEntry jEntry = jEntries.nextElement();
	                    if (jEntry.getName().endsWith("build-agent.properties")) {
	                        Properties prop = new Properties();
	                        InputStream jFileInput = jarFile.getInputStream(jEntry);
	                        prop.load(jFileInput);
	                        String agentVersion = prop.getProperty("agent.version");
	                        jFileInput.close();
	                        ts = Long.parseLong(agentVersion);
	                        break;
	                    }
	                }
	                jarFile.close();
	            } catch (IOException ioe) {
	                log.error("Error reading build information from jar file", ioe);
	            }
	            agentJar.close();
	        }
	
	        zipFile.close();
	        return ts;
	}

}
