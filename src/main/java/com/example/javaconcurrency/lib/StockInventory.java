package com.example.javaconcurrency.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;


@Slf4j
public class StockInventory {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock readLock = rwl.readLock();
	private final Lock writeLock = rwl.writeLock();
	private final Map<String, Integer> stockRepository = new HashMap<>();


	public Integer getStock(String item) {
		readLock.lock();
		log.info("readLock lock");
		try {
			Thread.sleep(2000);
			return stockRepository.getOrDefault(item, 0);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			readLock.unlock();
			log.info("readLock unLock");
		}
	}

	public void increaseStock(String item) {
		writeLock.lock();
		log.info("writeLock lock");
		try {
			Thread.sleep(2000);
			stockRepository.put(item, stockRepository.getOrDefault(item, 0) + 1);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			writeLock.unlock();
			log.info("writeLock unlock");
		}
	}

}
