package com.dbms.service;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.management.MBeanServer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;

@ManagedBean(name = "CqtCacheManager")
@ApplicationScoped
public class CqtCacheManager implements ICqtCacheManager {
	private CacheManager cacheManager;
	
	@PostConstruct
	public void init() {
		this.cacheManager = CacheManager.create(this.getClass().getClassLoader().getResource("cqt-ehcache.xml"));
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ManagementService.registerMBeans(cacheManager, mbs, true, true, true, true);
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICqtCacheManager#addToCache(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void addToCache(String cacheName, String key, Object data) {
		Cache cache = this.cacheManager.getCache(cacheName);
		cache.put(new Element(key, data));
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICqtCacheManager#getFromCache(java.lang.String, java.lang.String)
	 */
	@Override
	public Object getFromCache(String cacheName, String key) {
		Cache cache = this.cacheManager.getCache(cacheName);
		Element element = cache.get(key);
		if(null != element) {
			return element.getObjectValue();
		} else {
			return null;
		}
	}
	
	@Override
	public void removeFromCache(String cacheName, String key) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if(cache != null)
			cache.remove(key);
	}
	
	@Override
	public void removeAllFromCache(String cacheName) {
		Cache cache = this.cacheManager.getCache(cacheName);
		if(cache != null)
			cache.removeAll();
	}
	
}
