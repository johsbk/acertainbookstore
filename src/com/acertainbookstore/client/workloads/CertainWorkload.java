/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {
	private static final int START_BOOKS = 100;
	private static int numRun = 5;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		run(numRun, false);
	}
	
	public static void run(int _num, boolean _local) throws Exception {
		int numConcurrentWorkloadThreads = _num;
		String serverAddress = "http://localhost:8081";
		boolean localTest = _local;
		List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
		List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();

		initializeBookStoreData(serverAddress, localTest);

		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);
		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
			// The server address is ignored if localTest is true
			WorkloadConfiguration config = new WorkloadConfiguration(
					serverAddress, localTest);
			Worker workerTask = new Worker(config);
			// Keep the futures to wait for the result from the thread
			runResults.add(exec.submit(workerTask));
		}
		// Get the results from the threads using the futures returned
		for (Future<WorkerRunResult> futureRunResult : runResults) {
			WorkerRunResult runResult = futureRunResult.get(); // blocking call
			workerRunResults.add(runResult);
		}

		exec.shutdownNow(); // shutdown the executor
		reportMetric(workerRunResults);
		System.out.println("1");
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults) {
		// TODO: You should aggregate metrics and output them for plotting here
		//latency 
		float latency = 0;
		float throughput = 0;
		for(WorkerRunResult r:workerRunResults)
		{
			// test for successful interactions
			//latency
			latency += r.getElapsedTimeInNanoSecs()/r.getSuccessfulInteractions();
			System.out.println(r.getSuccessfulFrequentBookStoreInteractionRuns());
			//throughput for local test
			//throughput += (r.getSuccessfulFrequentBookStoreInteractionRuns()*100)/TimeUnit.NANOSECONDS.toMillis(r.getElapsedTimeInNanoSecs());
			// for RPC (throughput * 1000)
			throughput += (r.getSuccessfulFrequentBookStoreInteractionRuns()*1000)/TimeUnit.NANOSECONDS.toSeconds(r.getElapsedTimeInNanoSecs());
		}
		latency = latency/workerRunResults.size();
		try {
			// for local test
			//ReadWriteFile.Write(latency/1000 + "-" + throughput/100);
			// for RPC
			ReadWriteFile.Write(latency/1000 + "-" + throughput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 * @param serverAddress
	 * @param localTest
	 * @throws Exception
	 */
	public static void initializeBookStoreData(String serverAddress,
			boolean localTest) throws Exception {
		BookStore bookStore = null;
		StockManager stockManager = null;
		// Initialize the RPC interfaces if its not a localTest
		if (localTest) {
			stockManager = CertainBookStore.getInstance();
			bookStore = CertainBookStore.getInstance();
		} else {
			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);
		}

		// PCSD: We initialize data for our bookstore here
		BookSetGenerator bsg = new BookSetGenerator();
		
		Set<StockBook> books = bsg.nextSetOfStockBooks(START_BOOKS);
		stockManager.addBooks(books);
		
		
		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore).stop();
			((StockManagerHTTPProxy) stockManager).stop();
		}

	}
}
