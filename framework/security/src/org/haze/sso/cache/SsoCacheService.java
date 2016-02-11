package org.haze.sso.cache;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

public class SsoCacheService {
	public final static String CACHE_SSO_ONLINE_USER_TOKEN = "cache_sso_online_user_token";
	public final static String CACHE_SSO_ONLINE_USER_NAME = "cache_sso_online_user_name";
	
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * 添加缓存项
	 * 
	 * @param key
	 * @param item
	 * @return
	 */
	
	@Caching(put={
			@CachePut(value = CACHE_SSO_ONLINE_USER_TOKEN, key = "#item.key"),
			@CachePut(value = CACHE_SSO_ONLINE_USER_NAME, key = "#item.value")
	})
	public SsoCacheItem put(SsoCacheItem item) {
		if(item != null){
		log.debug("item[\"" + item.getKey() + " value : " + item.getValue()
				+ "\"] has put to  Cache...");
		}
		return item;
	}
	
	/**
	 * 获取缓存项
	 * 
	 * @param key
	 * @return
	 */
	@Caching(cacheable={
			@Cacheable(value = CACHE_SSO_ONLINE_USER_TOKEN, key = "#token")
	})
	public SsoCacheItem getByToken(String token) {
		log.debug("item[\"" + token + "\"] get from cache");
		return null;
	}

	/**
	 * 通过用户名，找到缓存项
	 * 
	 * @param userName
	 * @return
	 */
	@Cacheable(value = CACHE_SSO_ONLINE_USER_NAME, key = "#userName")
	public SsoCacheItem getByUserName(String userName) {
		log.debug("get item by user name [\"" + userName + " \"] ");
		return null;
	}

	/**
	 * 删除缓存项
	 * 
	 * @param key
	 * @return
	 */
	@Caching(evict={
			@CacheEvict(value = CACHE_SSO_ONLINE_USER_TOKEN, key = "#item.key"),
			@CacheEvict(value = CACHE_SSO_ONLINE_USER_NAME, key = "#item.value")
	})
	public void delete(SsoCacheItem item) {
		if(item != null){
		log.debug("item[\"" + item.getKey()
				+ "\"] is deleted from Cache...");
		}
	}


	/**
	 * 清空所有缓存项
	 */
	@Caching(evict={
			@CacheEvict(value = CACHE_SSO_ONLINE_USER_TOKEN,allEntries = true, beforeInvocation = true),
			@CacheEvict(value = CACHE_SSO_ONLINE_USER_NAME, allEntries = true, beforeInvocation = true)
	})
	public void clear() {
		log.debug("all items are deleted from  Cache...");
		//dataMap.clear();
	}



}
