package com.open.redis.server;


public interface EventRedisService {
	
	public void addEvent(String eventKey, String event);
	public String popEvent(String eventKey);
}
