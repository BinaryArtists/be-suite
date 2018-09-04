package com.open.redis.server.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.open.redis.server.JsonRedisService;

public class JsonRedisServiceImpl extends BaseRedisService implements
		JsonRedisService {
	private static final Integer BPOP_TIMEOUT = 5;

	@Override
	public void addJson(String key, String value) {
		// TODO Auto-generated method stub
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.opsForValue().set(key, value);
		redisTemplate.expire(key, BPOP_TIMEOUT, TimeUnit.HOURS);
	}

	@Override
	public String popJson(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		return redisTemplate.opsForValue().get(key);
	}
	
	public void del(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.delete(key);
	}

	@Override
	public void replace(String key, String value) {
		// TODO Auto-generated method stub
		del(key);
		addJson(key,value);
	}

}
