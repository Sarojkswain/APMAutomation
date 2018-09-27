package com.ca.apm.systemtest.fld.plugin.agentdownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

/**
 * Plugin to update agent.
 * 
 * @author tavpa01
 */
public class AgentDownloadPluginImpl extends AbstractPluginImpl implements AgentDownloadPlugin {
	private static final Logger log = LoggerFactory.getLogger(AgentDownloadPluginImpl.class);
	private static final String ERROR_LOAD = "ERROR: Can NOT load build time version from build-agent.properties";

	// Used for copying content and unziping files
	static final int BUFFER_SIZE = 1024 * 256; // 256 kB
	static final String AGENT_PREFIX = "agent-";

	@Value( "${agent.download.server:localhost}" )
	String centralServer;

	@Value( "${agent.download.path:/LoadOrchestrator/api/agent}" )
	String downloadPath;

	@Value( "${agent.root.dir:.}" )
	String rootDir;

	@Override
	@ExposeMethod(description = "Download new version of agent")
	public void downloadNewVersion() {
		// Schedule download to new thread and send response to server
		Thread doLater = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(15000);
					AgentDownloadPluginImpl.this.realDownload();
				} catch (InterruptedException e) {
					log.warn("Got interrupted.", e);
				}
			}
		};
		doLater.setDaemon(true);
		doLater.start();
		log.info("Download of new Agent version scheduled in 15 sec");
	}

	public void realDownload() {
		long lastModified = getCurrentVersion();
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			// Make request
			HttpGet req = new HttpGet(getDownloadUri());
			req.setHeader(HttpHeaders.LAST_MODIFIED, Long.toString(lastModified));
			log.info("Download new version of agent greater than {} from {}", lastModified, getDownloadUri());

			// Get result
			try (
					CloseableHttpResponse resp = httpclient.execute(req);
			) {
				log.info("HTTP response status line: {}", resp.getStatusLine().toString());
				int statCode = resp.getStatusLine().getStatusCode();
				if (statCode == HttpStatus.SC_OK) {
					if (resp.containsHeader(HttpHeaders.ETAG)) {
						String etag = resp.getFirstHeader(HttpHeaders.ETAG).getValue();
						String cLength = resp.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue();
						long lengthMB;
						try {
							lengthMB = Long.parseLong(cLength) / (1024*1024);
						} catch (NumberFormatException nfe) {
							lengthMB=-1;
						}
						log.info("Starting download version {} (length: {} MB)", etag, lengthMB);
						File downlTo = new File(rootDir + File.separator + AGENT_PREFIX + etag + ".zip");
						// File downlTo = File.createTempFile(AGENT_PREFIX, ".zip", new File(System.getProperty("java.io.tmpdir")));
						downloadFile(resp, downlTo);

						// Extract downloaded zip and align link
						String extractTo = rootDir + File.separator + AGENT_PREFIX + etag;
						extractZipAndLink(downlTo, extractTo);
						// downlTo.delete();

						// Everything went well, restart
						System.exit(100);
					} else {
						log.error("No {} in response", HttpHeaders.ETAG);
					}
				} else if (statCode != HttpStatus.SC_NOT_MODIFIED) {
					log.error("Wrong response, expect only 200 - OK or 304 - NOT MODIFIED");
				}
			}
		} catch (IOException e) {
			throw ErrorUtils.logExceptionAndWrapFmt(log, e, "Exception during agent zip extraction. Exception: {0}");
		}
	}

	private void extractZipAndLink(File fromFile, String extractTo) throws IOException, ZipException {
		log.info("Extracting zip {} into {}", fromFile.getCanonicalPath(), extractTo);
		unzipFile(fromFile, extractTo);
		File link = new File(rootDir + File.separator + AGENT_PREFIX + "current");
		link.delete();
		Files.createSymbolicLink(link.toPath(), Paths.get(extractTo));
		log.info("Link to new version created");
	}

	private File downloadFile(CloseableHttpResponse resp, File tempFile) throws IOException {
		try (OutputStream fileOutput = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[BUFFER_SIZE];
			InputStream content = resp.getEntity().getContent();
			int length;
			while ((length = content.read(buffer)) != -1) {
				fileOutput.write(buffer, 0, length);
			}
		}
		return tempFile;
	}

	private long curBuildTime = -1;

	@Override
	public long getCurrentVersion() {
		if (curBuildTime == -1) {
			// Read from directory
			File root = new File(rootDir);
			AgentFilter aFilter = new AgentFilter();
			root.listFiles(aFilter);
			if (aFilter.lastStamp != -1) {
				curBuildTime = aFilter.lastStamp;
			} else {
				// Fallback to build-agent.properties
				try {
					InputStream buildProps = this.getClass().getResourceAsStream("/build-agent.properties");
					if (buildProps != null) {
						Properties prop = new Properties();
						prop.load(buildProps);
						String agentTimestamp = prop.getProperty("agent.version");
						if ("${maven.build.timestamp}".equals(agentTimestamp)) {
							SimpleDateFormat dFormat = new SimpleDateFormat(AgentDownloadPlugin.VERSION_DATE_FORMAT);
							curBuildTime = Long.parseLong(dFormat.format(new Date()));
						} else {
							curBuildTime = Long.parseLong(agentTimestamp);
						}
					} else {
						log.error(ERROR_LOAD);
					}
				} catch (IOException | NumberFormatException e) {
					log.error(ERROR_LOAD, e);
				}
			}
		}
		return curBuildTime;
	}

	private String getDownloadUri() {
		return "http://" + centralServer + "/" + downloadPath;
	}

	private void unzipFile(File zipFileName, String outputFolder) throws ZipException, IOException {
		try (ZipFile zipFile = new ZipFile(zipFileName)) {
			byte[] bytes = new byte[BUFFER_SIZE];
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = zipEntry.getName();
				long size = zipEntry.getSize();
				long compressedSize = zipEntry.getCompressedSize();
				if (log.isDebugEnabled()) {
					log.debug(String.format("name: %-50s| size:%8d", name, size, compressedSize));
				}

				File file = new File(outputFolder + File.separator + name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if ( !parent.exists() ) {
					parent.mkdirs();
				}

				try (
					InputStream is = zipFile.getInputStream(zipEntry);
					FileOutputStream fos = new FileOutputStream(file);
				) {
					int length;
					while ((length = is.read(bytes)) != -1) {
						fos.write(bytes, 0, length);
					}
				}
			}
		}
	}


	static class AgentFilter implements FilenameFilter {
		long lastStamp = -1;

		public boolean accept(File dir, String name) {
			if (name.startsWith(AgentDownloadPluginImpl.AGENT_PREFIX)) {
				String numStr = name.substring(AgentDownloadPluginImpl.AGENT_PREFIX.length());
				try {
					long dirStamp = Long.parseLong(numStr);
					if (lastStamp < dirStamp) {
						lastStamp = dirStamp;
						return true;
					}
				} catch (NumberFormatException nf) {
					// Not number, ignore
				}
			}
			return false;
		}
	}
}