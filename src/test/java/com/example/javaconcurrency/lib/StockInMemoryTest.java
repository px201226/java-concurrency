package com.example.javaconcurrency.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class StockInMemoryTest {

	@Test
	void as() throws InterruptedException {

		final var stockInMemory = new StockInMemory();

		final var pool = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 100; i++) {
			pool.execute(() -> stockInMemory.unsafeIncrease("aa"));
		}


		Thread.sleep(5000L);

		System.out.println(stockInMemory.unsafeGet("aa"));
	}

	@Test
	void ass() throws InterruptedException {

		final var stockInMemory = new StockInMemory();

		final var pool = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 100; i++) {
			pool.execute(() -> stockInMemory.safeIncrease("aa"));
		}


		Thread.sleep(5000L);

		System.out.println(stockInMemory.safeGet("aa"));
	}
}