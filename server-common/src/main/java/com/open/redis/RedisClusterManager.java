package com.open.redis;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

public class RedisClusterManager {

	private JedisPoolConfig poolConfig;

	private String jedisClusterNodes;

	private int maxRedirections;

	public String getJedisClusterNodes() {
		return jedisClusterNodes;
	}

	public void setJedisClusterNodes(String jedisClusterNodes) {
		this.jedisClusterNodes = jedisClusterNodes;
	}

	public int getMaxRedirections() {
		return maxRedirections;
	}

	public void setMaxRedirections(int maxRedirections) {
		this.maxRedirections = maxRedirections;
	}

	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	@Autowired
	public void setPoolConfig(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public StringRedisTemplate jedisConnectionFactory() {
		String[] jedisClusterNodes = this.jedisClusterNodes.split(",");
		RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
				Arrays.asList(jedisClusterNodes));
		clusterConfig.setMaxRedirects(maxRedirections);
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(new JedisConnectionFactory(
				clusterConfig, poolConfig));
		stringRedisTemplate.afterPropertiesSet();

		return stringRedisTemplate;
	}

	public JedisConnectionFactory getJedisConnectionFactory() {
		String[] jedisClusterNodes = this.jedisClusterNodes.split(",");
		RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
				Arrays.asList(jedisClusterNodes));
//		clusterConfig.setMaxRedirects(maxRedirections);
		

		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(clusterConfig);
		jedisConnectionFactory.afterPropertiesSet();
		return jedisConnectionFactory;
	}
}
