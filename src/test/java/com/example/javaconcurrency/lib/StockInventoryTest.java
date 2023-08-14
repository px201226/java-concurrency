package com.example.javaconcurrency.lib;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StockInventoryTest {

	@Test
	void readLock() throws InterruptedException {
		final StockInventory stockInventory = new StockInventory();

		new Thread(
				() -> {
					final var qty = stockInventory.getStock("aaa");
					System.out.println(qty);
				}
		).start();

		new Thread(
				() -> {
					final var qty = stockInventory.getStock("aaa");
					System.out.println(qty);
				}
		).start();

		Thread.sleep(5000L);
	}

	@Test
	void writeLock() {
		final StockInventory stockInventory = new StockInventory();

		new Thread(
				() -> {
					stockInventory.increaseStock("aaa");
				}
		).start();

		new Thread(
				() -> {
					stockInventory.increaseStock("aaa");
				}
		).start();

		try {
			Thread.sleep(4000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void writeLockVsReadLock() throws InterruptedException {
		final StockInventory stockInventory = new StockInventory();

		new Thread(
				() -> {
					stockInventory.increaseStock("aaa");
				}
		).start();

		new Thread(
				() -> {
					final var qty = stockInventory.getStock("aaa");
					System.out.println(qty);
				}
		).start();

		Thread.sleep(4000L);
	}

	@Test
	void readLockAndWriteLock() throws InterruptedException {
		final StockInventory stockInventory = new StockInventory();

		new Thread(
				() -> {
					final var qty = stockInventory.getStock("aaa");
					System.out.println(qty);
				}
		).start();

		new Thread(
				() -> {
					stockInventory.increaseStock("aaa");
				}
		).start();

		Thread.sleep(5000L);
	}
}