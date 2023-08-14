package com.example.javaconcurrency.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;import java.util.concurrent.ScheduledExecutorService;import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class LatchExampleTest {

	@Test
	void latch_test() throws InterruptedException {
		final var countDownLatch = new CountDownLatch(5);

		final var pool = Executors.newFixedThreadPool(5);
		for (int i=0; i<5; i++){
			pool.execute(new LatchExample(countDownLatch));
		}

		log.info("await on main");
		countDownLatch.await();
		log.info("done on main");
		Thread.sleep(5000L);

		final var forkJoinPool = ForkJoinPool.commonPool();
	}
}