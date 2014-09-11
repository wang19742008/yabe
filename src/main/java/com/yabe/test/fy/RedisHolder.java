package com.yabe.test.fy;

import redis.clients.jedis.Jedis;

public class RedisHolder {
	private static Jedis jedis = null;
	public static Jedis getInstance(){
		if(jedis == null){
			jedis = new Jedis("182.92.75.43");
		}
		return jedis;
	}
}
