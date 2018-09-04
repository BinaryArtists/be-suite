package com.open.redis.server;

import java.util.Map;

public interface CountRedisService {
	
	
	public int inc(String key);
	public int add(String key, long count);
	public int incWithExpire(String key, Integer expire);
	public int addWithExpire(String key, long count, Integer expire);
	
	public int getCount(String key);
	public void setCountWithExpire(String key, long count, Integer expire);
	public void deleteCount(String key);
	
	public Map<String, Integer> getCount(Map<String, String> keyMap);
}
