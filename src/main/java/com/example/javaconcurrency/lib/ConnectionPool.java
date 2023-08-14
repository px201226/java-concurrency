package com.example.javaconcurrency.lib;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionPool {

	private final Semaphore semaphore;
	private final Queue<String> connectionPool;

	public ConnectionPool(int poolSize) {
		this.semaphore = new Semaphore(poolSize, true);
		this.connectionPool = new LinkedList<>();
		for (int i = 0; i < poolSize; i++) {
			connectionPool.add("pool" + i);
		}
	}

	public String acquireConnection() throws InterruptedException {
		semaphore.acquire();
		log.info("acquire connection");
		return connectionPool.poll();
	}

	public void releaseConnection(String connection) {
		log.info("release connection");
		connectionPool.offer(connection);
		semaphore.release();
	}

}
