package com.open.redis;

import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisMananger {
	private HashMap<Integer, StringRedisTemplate> runingMap = new HashMap<Integer, StringRedisTemplate>();
	private HashMap<StringRedisTemplate, List<Integer>> roadMap = new HashMap<StringRedisTemplate, List<Integer>>();
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();

	private ArrayList<StringRedisTemplate> redisTemplateList;

	private ArrayList<StringRedisTemplate> redisBadTemplateList = new ArrayList<StringRedisTemplate>();

	public void setBadTemplate(StringRedisTemplate badTemplate) {
		redisTemplateList.remove(badTemplate);
		redisBadTemplateList.add(badTemplate);
		List<Integer> list = roadMap.get(badTemplate);
		roadMap.remove(badTemplate);
		for (Integer place : list) {

			StringRedisTemplate value = redisTemplateList.get((int) (System
					.currentTimeMillis() % redisTemplateList.size()));
			runingMap.put(place, value);
			roadMap.get(value).add(place);

		}

	}

	public void init() {

		singleThreadExecutor.execute(new Runnable() {
			public void run() {
				try {
					for (StringRedisTemplate template : redisBadTemplateList) {
						try {
							if (template.opsForValue().get("check") == null) {

								template.opsForValue().set("check", "1");

							}

							redisBadTemplateList.remove(template);
							redisTemplateList.add(template);
							for (List<Integer> items : roadMap.values()) {
								if (items.size() > 1) {
									Integer integer = items.remove(0);
									runingMap.put(integer, template);
									ArrayList<Integer> list = new ArrayList<Integer>();
									list.add(integer);
									roadMap.put(template, list);

								}

							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public List<StringRedisTemplate> getRedisTemplateList() {
		return redisTemplateList;
	}

	@Autowired
	public void setRedisTemplateList(
			ArrayList<StringRedisTemplate> redisTemplateList) {
		this.redisTemplateList = redisTemplateList;
		initHashMap();
	}

	private void initHashMap() {
		// TODO Auto-generated method stub

		for (int i = 0; i < redisTemplateList.size(); i++) {

			runingMap.put(i, redisTemplateList.get(i));
			List<Integer> placeList = new ArrayList<Integer>();
			placeList.add(i);
			roadMap.put(redisTemplateList.get(i), placeList);
		}
	}

	public StringRedisTemplate getRedisTemplate(String key) {
		System.out.println("key:" + key);
		int value = key.hashCode() % getRedisTemplateList().size();
		System.out.println("value:" + value);

		return runingMap.get(value);

	}
}
