package com.open.redis.server;

public interface CacheRedisService {
	
	public void setCache(String key, String value, Long expire);
	public String getCache(String key);
	public void clearCache(String key);
	
	public void setCache(String key, String field, String value, Long expire);
	public String getCache(String key, String field);
	public void clearCache(String key, String field);
}
