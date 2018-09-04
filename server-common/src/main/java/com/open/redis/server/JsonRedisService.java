package com.open.redis.server;

public interface JsonRedisService {
	public void addJson(String key, String value);
	public String popJson(String key);
	public void del(String key);
	public void replace(String key, String value);
}
