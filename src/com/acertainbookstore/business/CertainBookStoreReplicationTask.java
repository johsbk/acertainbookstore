package com.acertainbookstore.business;

import java.net.URL;
import java.util.concurrent.Callable;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * PCSD
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {
	private String slaveServer;
	private ReplicationRequest request;
	private HttpClient client;
	public CertainBookStoreReplicationTask(String slaveServer,HttpClient client,ReplicationRequest request) {
		this.slaveServer = slaveServer;
		this.request = request;
	}

	@Override
	public ReplicationResult call() throws Exception {
		boolean replicationSuccessful = false;
		String xml = BookStoreUtility
		.serializeObjectToXMLString(request);
		Buffer requestContent = new ByteArrayBuffer(xml);
		ContentExchange exchange = new ContentExchange();
		String urlString = slaveServer + "/replicate";
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		try {
			BookStoreUtility.SendAndRecv(client, exchange);
			replicationSuccessful = true;
		} catch(BookStoreException e) {
			replicationSuccessful = false;
		}
		return new ReplicationResult(this.slaveServer, replicationSuccessful);
	}

}
