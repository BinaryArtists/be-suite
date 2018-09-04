package com.open.redis.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.open.redis.RedisMananger;

public abstract class BaseRedisService {

	// private ArrayList<StringRedisTemplate> redisTemplateList;
	private RedisMananger redisMananger;

	public StringRedisTemplate getRedisTemplate(String key) {
		return redisMananger.getRedisTemplate(key);
	}

	public List<Long> stringSetToIdList(Set<String> stringSet) {

		if (null == stringSet) {
			return Collections.emptyList();
		}

		List<Long> rtValue = new ArrayList<Long>(stringSet.size());
		for (String tmpId : stringSet) {
			rtValue.add(Long.valueOf(tmpId));
		}

		return rtValue;
	}

	public RedisMananger getRedisMananger() {
		return redisMananger;
	}

	@Autowired
	public void setRedisMananger(RedisMananger redisMananger) {
		this.redisMananger = redisMananger;
	}

}
