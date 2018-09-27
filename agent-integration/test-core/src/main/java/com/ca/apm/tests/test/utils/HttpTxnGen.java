package com.ca.apm.tests.test.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.test.utils.HttpTxnGen.HttpTxnGenBuilder.ExecutionMode;

/**
 * @author sinka08
 *
 */
public class HttpTxnGen {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTxnGen.class);
	private static final String THREAD_GROUP_NAME = "http-txn-gen-group";
	private final int numReqs;
	private final long delayBetweenReqs;
	private final HttpUriRequest uriRequest;
	private final ExecutorService execService;
	private final ExecutionMode execMode;
	private final FutureTask<TxnLoadReport> mainTask;

	private HttpTxnGen(HttpTxnGenBuilder b) throws URISyntaxException {
		this.numReqs = b.numReqs;
		this.delayBetweenReqs = b.delayBetweenReqs;
		this.uriRequest = b.uriRequest;
		this.execMode = b.execMode;

		if (execMode == ExecutionMode.SEQUENTIAL) {
			this.execService = Executors.newSingleThreadExecutor(createThreadFactory());
		} else {
			this.execService = Executors.newCachedThreadPool(createThreadFactory());
		}

		this.mainTask = createMainExecutionTask();
	}

	private FutureTask<TxnLoadReport> createMainExecutionTask() {
		Callable<TxnLoadReport> callable = new Callable<TxnLoadReport>() {

			@Override
			public TxnLoadReport call() throws Exception {

				LOGGER.info("Starting {} txn load", execMode);

				TxnLoadReport txnExecData = new TxnLoadReport();
				txnExecData.setStartTime(System.currentTimeMillis());
				// LOGGER.debug(txnExecData.toString());
				execute(txnExecData);
				txnExecData.setFinishTime(System.currentTimeMillis());

				LOGGER.info("txn load ended");
				LOGGER.debug(txnExecData.toString());
				return txnExecData;
			}
		};

		return new FutureTask<>(callable);
	}

	private void execute(TxnLoadReport txnExecData) {
		Collection<Future<?>> futureList = new ArrayList<>();

		for (int i = 0; i < numReqs; i++) {
			futureList.add(execService.submit(createTxnCallTask()));
			futureList.add(execService.submit(createSleepTask()));
		}

		for (Future<?> future : futureList) {

			Object result;
			try {
				// waits till result is available
				result = future.get();

				if (result instanceof CloseableHttpResponse && !future.isCancelled()) {
					int statusCode = ((CloseableHttpResponse) result).getStatusLine()
					        .getStatusCode();
					txnExecData.reportStatusCode(statusCode);

					LOGGER.info("http response status code: {}", statusCode);
				}
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error(e.getMessage(), e);
				txnExecData.reportStatusCode(400);
			}

		}

		execService.shutdown();
		LOGGER.debug("executor service shutdown status: {}", execService.isShutdown());
	}

	private Callable<CloseableHttpResponse> createTxnCallTask() {
		return new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				LOGGER.info("making http request to {}", uriRequest.getURI());

				// TODO reuse same connection
				CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse response = httpclient.execute(uriRequest);
				return response;
			}
		};
	}

	private Callable<Void> createSleepTask() {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				LOGGER.debug("sleeping for {} ms", delayBetweenReqs);
				Thread.sleep(delayBetweenReqs);
				return null;
			}
		};
	}

	public FutureTask<TxnLoadReport> startSync() {
		mainTask.run();
		return mainTask;
	}

	public FutureTask<TxnLoadReport> startAsync() {
		new Thread(new ThreadGroup(THREAD_GROUP_NAME), mainTask, "http-txn-gen-main").start();
		return mainTask;
	}

	public void start() {
		startSync();
	}

	public static class HttpTxnGenBuilder {
		public enum HttpRequestMethod {
			GET, POST, PUT;
		};

		public enum ExecutionMode {
			SEQUENTIAL, PARALLEL;
		};

		private static final HttpRequestMethod DEFAULT_HTTP_METHOD = HttpRequestMethod.GET;
		private static final ExecutionMode DEFAULT_EXEC_MODE = ExecutionMode.SEQUENTIAL;
		private static final int DEFAULT_NUM_REQS = 1;
		private static final long DEFAULT_DELAY_BW_REQS = 1000;
		private static final int DEFAULT_CONNECT_TIMEOUT = 120000; // 2 minutes
		private static final int DEFAULT_SOCKET_TIMEOUT = 60000; // 1 minute
		
		private HttpRequestMethod httpMethod = DEFAULT_HTTP_METHOD;
		private ExecutionMode execMode = DEFAULT_EXEC_MODE;
		private int numReqs = DEFAULT_NUM_REQS; // number of requests to make

		// default 1 sec sleep between multiple requests for same resource
		private long delayBetweenReqs = DEFAULT_DELAY_BW_REQS;
		private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
		private URIBuilder uriBuilder;
		private HttpUriRequest uriRequest;
		private List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		private Map<String,String> customHeaders = new HashMap<String,String>();

		public HttpTxnGenBuilder() {
			uriBuilder = new URIBuilder();
		}

		public HttpTxnGenBuilder(String uriString) {
			try {
				uriBuilder = new URIBuilder(uriString);
			} catch (Exception e) {
				throw new RuntimeException("Exception while creating URIBuilder instance", e);
			}
		}

		public HttpTxnGenBuilder(URI uri) {
			uriBuilder = new URIBuilder(uri);
		}

		public HttpTxnGenBuilder(HttpUriRequest uriRequest) {
			this.uriRequest = uriRequest;
		}

		public HttpTxnGenBuilder setHost(String host) {
			checkIfSupported();
			uriBuilder.setHost(host);
			return this;
		}

		public HttpTxnGenBuilder setPort(int port) {
			checkIfSupported();
			uriBuilder.setPort(port);
			return this;
		}

		public HttpTxnGenBuilder setPath(String path) {
			checkIfSupported();
			uriBuilder.setPath(path);
			return this;
		}

		private void checkIfSupported() {
			if (uriRequest != null) {
				throw new UnsupportedOperationException("uriRequest object already set");
			}
		}

		public HttpTxnGenBuilder setHttpMethod(HttpRequestMethod method) {
			this.httpMethod = method;
			return this;
		}

		/**
		 * Applicable only to http request methods :POST and PUT
		 * 
		 * @param params
		 * @return
		 */
		public HttpTxnGenBuilder setParams(Map<String, String> params) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			return this;
		}
		
		public HttpTxnGenBuilder setCustomHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

		public HttpTxnGenBuilder setExecutionMode(ExecutionMode mode) {
			this.execMode = mode;
			return this;
		}

		/**
		 * Set delay between subsequent http requests
		 * 
		 * @param delayBetweenReqs
		 * @return
		 */
		public HttpTxnGenBuilder setDelayBetweenReqs(long durationInMillis) {
			this.delayBetweenReqs = durationInMillis;
			return this;
		}

		public HttpTxnGenBuilder setDelayBetweenReqs(long duration, TimeUnit unit) {
			this.delayBetweenReqs = TimeUnit.MILLISECONDS.convert(duration, unit);
			return this;
		}

		public HttpTxnGenBuilder setConnectTimeout(long duration, TimeUnit unit) {
			this.connectTimeout = (int) TimeUnit.MILLISECONDS.convert(duration, unit);
			return this;
		}

		public HttpTxnGenBuilder setSocketTimeout(long duration, TimeUnit unit) {
			this.socketTimeout = (int) TimeUnit.MILLISECONDS.convert(duration, unit);
			return this;
		}

		/**
		 * Set number of http requests to make
		 * 
		 * @param numReqs
		 * @return
		 */
		public HttpTxnGenBuilder setNumberReqs(int numReqs) {
			this.numReqs = numReqs;
			return this;
		}
		
		private void addCustomHeaders(HttpRequestBase request) {
	        
	        if(customHeaders.size() > 0) {
	            for (Map.Entry<String, String> header : customHeaders.entrySet()) {
	                request.addHeader(header.getKey(), header.getValue());
	            }
	        }
	    }

		public HttpTxnGen build() {
			try {
				if (uriRequest == null) {
					// create uri Request object if not already set
					URI uri = this.uriBuilder.build();
					RequestConfig config = RequestConfig.custom()
					        .setConnectTimeout(connectTimeout)
					        .setSocketTimeout(socketTimeout).build();

					switch (httpMethod) {
					case POST: {
						HttpPost httpPost = new HttpPost(uri);
						UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams,
						        Consts.UTF_8);
						addCustomHeaders(httpPost);
						httpPost.setEntity(entity);
						httpPost.setConfig(config);
						uriRequest = httpPost;
						break;
					}
					case PUT: {
						HttpPut httpPut = new HttpPut(uri);
						UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams,
						        Consts.UTF_8);
						httpPut.setEntity(entity);
						httpPut.setConfig(config);
						uriRequest = httpPut;
						break;
					}
					case GET:
						HttpGet httpGet = new HttpGet(uri);
						addCustomHeaders(httpGet);
						httpGet.setConfig(config);
						uriRequest = httpGet;
					}

				if (ExecutionMode.PARALLEL == execMode) {
					delayBetweenReqs = 0;
				}

				}

				return new HttpTxnGen(this);
			} catch (Exception e) {
				throw new RuntimeException("Exception while building HttpTxnGenInstance", e);
			}
		}
	}
	
	private static ThreadFactory createThreadFactory() {
		return new ThreadFactory() {

			@Override
			public Thread newThread(Runnable runnable) {
				Thread t = new Thread(new ThreadGroup(THREAD_GROUP_NAME), runnable,
				        "http-txn-gen-sub");
				t.setDaemon(true);
				return t;
			}
		};
	}

	public class TxnLoadReport {
		private long startTime;
		private long finishTime;
		// total number of requests made
		private AtomicInteger numReqsCompleted = new AtomicInteger(0);
		// requests with http response code = 200
		private AtomicInteger numOKResponses = new AtomicInteger(0);
		private final List<Integer> statusCodes = new ArrayList<>();

		public TxnLoadReport() {
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public long getFinishTime() {
			return finishTime;
		}

		public void setFinishTime(long finishTime) {
			this.finishTime = finishTime;
		}

		public HttpUriRequest getUriRequest() {
			return uriRequest;
		}

		/**
		 * @return returns number of request completed irrespective of response
		 *         status code
		 */
		public int getNumReqsCompleted() {
			return numReqsCompleted.intValue();
		}

		private void incrementNumReqsCompleted() {
			this.numReqsCompleted.incrementAndGet();
		}

		/**
		 * @return returns number of request with status code == 200
		 */
		public int getNumOKResponses() {
			return numOKResponses.intValue();
		}

		private void incrementNumOKReqs() {
			this.numOKResponses.incrementAndGet();
		}

		private void reportStatusCode(int code) {
			incrementNumReqsCompleted();

			if (code == 200) {
				incrementNumOKReqs();
			}
			statusCodes.add(code);
		}

		public ArrayList<Integer> getStatusCodes() {
			return new ArrayList<>(statusCodes);
		}

		public String toString() {
			StringBuilder b = new StringBuilder("- Txn Load Report - \n");
			b.append("url: " + uriRequest.getURI() + "\n");
			b.append("method: " + uriRequest.getMethod() + "\n");
			b.append("execution mode: " + execMode + "\n");
			b.append("number of requests to make: " + numReqs + "\n");
			b.append("delay between requests: " + delayBetweenReqs + " ms \n");
			b.append("start time: " + startTime + "\n");
			b.append("number of requests completed: " + numReqsCompleted + "\n");
			b.append("finish time: " + finishTime + "\n");
			return b.toString();
		}

	}

}
