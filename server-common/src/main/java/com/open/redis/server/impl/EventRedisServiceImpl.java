package com.open.redis.server.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import com.open.redis.server.EventRedisService;

 


public class EventRedisServiceImpl extends BaseRedisService implements EventRedisService {
	
	private static final Integer BPOP_TIMEOUT = 5 * 60;

	 
	public void addEvent(String eventKey, String event) {
		String eventSetKey = getEventSetKey(eventKey);
		String eventListKey = getEventListKey(eventKey);
		//先sadd,在rpush
		StringRedisTemplate redisTemplate = getRedisTemplate(eventKey);
		redisTemplate.opsForSet().add(eventSetKey, event);
		redisTemplate.opsForList().rightPush(eventListKey, event);
	}

	 
	public String popEvent(String eventKey) {
		
		String eventSetKey = getEventSetKey(eventKey);
		String eventListKey = getEventListKey(eventKey);
		StringRedisTemplate redisTemplate = getRedisTemplate(eventKey);
		String event = redisTemplate.opsForList().leftPop(eventListKey, BPOP_TIMEOUT, TimeUnit.SECONDS);
		if( StringUtils.isEmpty(event) ) {
			return null;
		} else if ( redisTemplate.opsForSet().remove(eventSetKey, event) > 0 ) {
			return event;
		} else {
			return null;
		}
	}

	private String getEventSetKey(String eventKey){
		return "event:set:" + eventKey;
	}
	
	private String getEventListKey(String eventKey){
		return "event:list:" + eventKey;
	}
}
