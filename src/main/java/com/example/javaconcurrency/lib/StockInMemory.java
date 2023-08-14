package com.example.javaconcurrency.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockInMemory {

	private final Map<String, Integer> unsafe = new HashMap<>();
	private final Map<String, Integer> safe = new ConcurrentHashMap<>();


	public synchronized Integer unsafeIncrease(String key) {
		final var orDefault = unsafe.getOrDefault(key, 0);
		unsafe.put(key, orDefault + 1);
		return orDefault + 1;
	}

	public synchronized Integer safeIncrease(String key) {
		final var orDefault = safe.getOrDefault(key, 0);
		safe.put(key, orDefault + 1);
		return orDefault + 1;
	}

	public Integer unsafeGet(String key){
		return unsafe.get(key);
	}

	public Integer safeGet(String key){
		return safe.get(key);
	}
}
