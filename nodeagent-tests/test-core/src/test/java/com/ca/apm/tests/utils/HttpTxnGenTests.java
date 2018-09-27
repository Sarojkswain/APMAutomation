package com.ca.apm.tests.utils;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.ExecutionMode;
import com.ca.apm.tests.utils.HttpTxnGen.TxnLoadReport;

public class HttpTxnGenTests {
	private final static String TEST_URL = "http://google.com";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testSequentialExecution() throws URISyntaxException, InterruptedException,
	        ExecutionException {
		long delay = 100;
		int n = 3;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(TEST_URL)
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(n)
		        .setDelayBetweenReqs(delay).build();

		FutureTask<TxnLoadReport> result = txnGen.startSync();
		Assert.assertTrue(result.isDone());
		TxnLoadReport data = result.get();
		long buffer = 10;
		Assert.assertTrue((data.getFinishTime() - data.getStartTime() + buffer) > (n * delay));
		Assert.assertEquals(n, result.get().getNumReqsCompleted());
	}

	@Test
	public void testParellelExecution() throws URISyntaxException, InterruptedException,
	        ExecutionException {
		int n = 5;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(TEST_URL)
		        .setExecutionMode(ExecutionMode.PARALLEL).setNumberReqs(n).build();
		FutureTask<TxnLoadReport> result = txnGen.startSync();
		Assert.assertTrue(result.isDone());
		Assert.assertEquals(n, result.get().getNumReqsCompleted());
	}

	@Test
	public void testBlockingExecution() throws URISyntaxException, InterruptedException,
	        ExecutionException {
		long delay = 1000;
		int n = 1;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(TEST_URL)
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(n)
		        .setDelayBetweenReqs(delay).build();

		FutureTask<TxnLoadReport> result = txnGen.startSync();
		// check startSync returned only after task completion
		Assert.assertTrue(result.isDone());
	}

	@Test
	public void testAsynchronizedExecution() throws URISyntaxException, InterruptedException,
	        ExecutionException, TimeoutException {
		long delay = 1000;
		int n = 1;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(TEST_URL)
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(n)
		        .setDelayBetweenReqs(delay).build();

		FutureTask<TxnLoadReport> result = txnGen.startAsync();
		// check startSync returned before task completion
		Assert.assertFalse(result.isDone());

		exception.expect(TimeoutException.class);
		// result should not be available in 1000 ms also
		// we expect timeout
		result.get(100, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testCompletedRequests() throws URISyntaxException, InterruptedException,
	        ExecutionException {
		long delay = 100;
		int n = 1;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder("http://ca-na-x.com/page/unknownhost")
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(n)
		        .setConnectTimeout(10, TimeUnit.SECONDS).setSocketTimeout(5, TimeUnit.SECONDS)
		        .setDelayBetweenReqs(delay).build();

		FutureTask<TxnLoadReport> result = txnGen.startSync();
		Assert.assertTrue(result.isDone());
		Assert.assertEquals(n, result.get().getNumReqsCompleted());
	}

	@Test
	public void testOKRequests() throws URISyntaxException, InterruptedException,
	        ExecutionException {
		long delay = 100;
		int n = 1;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder("http://google.com/page/notexist")
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(n)
		        .setConnectTimeout(10, TimeUnit.SECONDS).setSocketTimeout(5, TimeUnit.SECONDS)
		        .setDelayBetweenReqs(delay).build();

		FutureTask<TxnLoadReport> result = txnGen.startSync();
		Assert.assertTrue(result.isDone());
		Assert.assertEquals(0, result.get().getNumOKResponses());
	}

}
