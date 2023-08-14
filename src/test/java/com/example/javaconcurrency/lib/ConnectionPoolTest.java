package com.example.javaconcurrency.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ConnectionPoolTest {


	@Test
	void semaphoreTest() throws InterruptedException {
		final var connectionPool = new ConnectionPool(2);

		final var executorService = Executors.newFixedThreadPool(4);

		for (int i=0; i<4; i++){
			executorService.execute(() -> {
				try {
					final var connection = connectionPool.acquireConnection();
					log.info("connection acquired = {}", connection);
					Thread.sleep(2000L);
					connectionPool.releaseConnection(connection);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});

		}


		Thread.sleep(15000L);

	}

	@Test
	void semaphore_wait_Test() throws InterruptedException {
		final var connectionPool = new ConnectionPool(1);

		final var executorService = Executors.newFixedThreadPool(5);

		for (int i=0; i<10; i++){
			executorService.execute(() -> {
				try {
					final var connection = connectionPool.acquireConnection();
					log.info("connection acquired = {}", connection);
					Thread.sleep(2000L);
					connectionPool.releaseConnection(connection);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});

		}


		Thread.sleep(15000L);

	}
}