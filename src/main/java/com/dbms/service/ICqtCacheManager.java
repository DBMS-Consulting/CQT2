package com.dbms.service;

public interface ICqtCacheManager {

	void addToCache(String cacheName, String key, Object data);

	Object getFromCache(String cacheName, String key);

}