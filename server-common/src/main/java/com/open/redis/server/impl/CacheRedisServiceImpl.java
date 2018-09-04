package com.open.redis.server.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.open.redis.server.CacheRedisService;
 

public class CacheRedisServiceImpl extends BaseRedisService implements CacheRedisService {

	 
	public void setCache(String key, String value, Long expire) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.opsForValue().set(key, value);
		redisTemplate.expire(key, expire, TimeUnit.SECONDS);
	}

	 
	public String getCache(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		return redisTemplate.opsForValue().get(key);
	}

	 
	public void clearCache(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.delete(key);
	}

	 
	public void setCache(String key, String field, String value, Long expire) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.opsForHash().put(key, field, value);
		redisTemplate.expire(key, expire, TimeUnit.SECONDS);
	}

	 
	public String getCache(String key, String field) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		return (String) redisTemplate.opsForHash().get(key, field);
	}

	 
	public void clearCache(String key, String field) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.opsForHash().delete(key, field);
	}
}
