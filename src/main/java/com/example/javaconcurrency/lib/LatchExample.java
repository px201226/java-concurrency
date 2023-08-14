package com.example.javaconcurrency.lib;

import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LatchExample implements Runnable {

	private final CountDownLatch latch;

	public LatchExample(final CountDownLatch latch) {
		this.latch = latch;
	}

	@Override public void run() {
		log.info("Do parallel Async Processing");
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		latch.countDown();
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("Done parallel Async Processing");

	}
}
