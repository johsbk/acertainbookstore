package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.client.BookStoreClientConstants;
import com.acertainbookstore.interfaces.Replicator;

/**
 * PCSD
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {
	ExecutorService executor;
	HttpClient client;
	public CertainBookStoreReplicator(int maxReplicatorThreads) throws Exception {
		executor = Executors.newFixedThreadPool(maxReplicatorThreads);
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS); // max
																									// concurrent
																									// connections
																									// to
																									// every
																									// address
		client.setThreadPool(new QueuedThreadPool(
				BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS)); // max
																			// threads
		client.setTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS); // seconds
																					// timeout;
																					// if
																					// no
																					// server
																					// reply,
																					// the
																					// request
																					// expires
		client.start();
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request) {
		List<Future<ReplicationResult>> results = new ArrayList<Future<ReplicationResult>>();
		
		for(String slave : slaveServers) {
			Future<ReplicationResult> result = executor.submit(new CertainBookStoreReplicationTask(slave,client,request));
			results.add(result);
		}
		return results;
	}

}
