package com.open.redis.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import com.open.redis.server.CountRedisService;
 

public class CountRedisServiceImpl extends BaseRedisService implements
		CountRedisService {
	
	private static final Logger logger = LoggerFactory.getLogger(CountRedisServiceImpl.class);
	private static final Integer emptyCount = -1;
	
	 
	public int inc(String key) {
		return add(key, 1);		
	}

	 
	public int add(String key, long count) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		Long value = redisTemplate.opsForValue().increment(key, count);
		
		System.out.println("redisTemplate"+redisTemplate);
		return value.intValue(); 
	}

	 
	public int incWithExpire(String key, Integer expire) {
		return addWithExpire(key, 1, expire);
	}

	 
	public int addWithExpire(String key, long count, Integer expire) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		Long value = redisTemplate.opsForValue().increment(key, count);
		redisTemplate.expire(key, expire, TimeUnit.SECONDS);
		
		return value.intValue();
	}

	 
	public int getCount(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		String value = redisTemplate.opsForValue().get(key);
		if( StringUtils.isEmpty(value) ) {
			return -1;
		} else {
			try {
				return Long.valueOf(value).intValue();
			} catch ( RuntimeException ex ) {
				logger.warn("count cache exist unknown value. delete it. key:{}, value:{}.", key, value, ex);
				deleteCount(key);
				return -1;
			}
		}
	}
	
	 
	public Map<String, Integer> getCount(Map<String, String> keyMap) {
		StringRedisTemplate redisTemplate = getRedisTemplate("");
		List<String> keys = new ArrayList<String>(keyMap.keySet());
		List<String> valueStrs = redisTemplate.opsForValue().multiGet(keys);
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = valueStrs.get(i);
			String refId = keyMap.get(key);
			if( StringUtils.isEmpty(value) ) {
				result.put(refId, emptyCount);
			} else {
				try {
					Long valueLong = Long.valueOf(value);
					result.put(refId, valueLong.intValue());
				} catch ( RuntimeException ex ) {
					logger.warn("count cache exist unknown value. delete it. key:{}, value:{}.", key, value, ex);
					deleteCount(key);
					result.put(refId, emptyCount);
				}
			}
		}		
		return result;		
	}

	 
	public void setCountWithExpire(String key, long count, Integer expire) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.opsForValue().set(key, String.valueOf(count));
		redisTemplate.expire(key, expire, TimeUnit.SECONDS);		
	}

	 
	public void deleteCount(String key) {
		StringRedisTemplate redisTemplate = getRedisTemplate(key);
		redisTemplate.delete(key);
	}

}
