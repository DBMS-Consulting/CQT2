package com.dbms.service;

public interface ICqtCacheManager {

	void addToCache(String cacheName, String key, Object data);

	Object getFromCache(String cacheName, String key);
	
	void removeFromCache(String cacheName, String key);
	
	void removeAllFromCache(String cacheName);

}