package com.yabe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * 
 * 缓存结构: a.通讯录fans关系---通讯录中电话 -> 用户电话-----t_1360003 -> [135000,136000,136001]
 * b.用户fans关系-----用户ID->粉丝ID---------------f_100 -> [101]
 * c.用户follow关系---用户ID->关注人ID-------------g_101 -> [100]
 * d.电话到Uers的映射-用户电话->用户ID-------------a_1360003 -> 100
 * e.商品发布列表-----用户ID->商品ID集合-----------p_100 -> [111,222]
 * f.商品推送列表-----用户ID->推送商品ID集合------ c_100 -> [111,222]
 * 
 * @author lingkong
 *
 */
public class SNSService {
	private Jedis jedis = RedisHolder.getInstance();

	private final String KEY_TEL_PRE = "t_";
	private final String KEY_USERTEL_PRE = "a_";
	private final String KEY_FOLLOW_PRE = "g_";
	private final String KEY_FANS_PRE = "f_";
	private final String KEY_PUBLISH_PRE = "p_";
	private final String KEY_PUSH_PRE = "c_";

	/**
	 * 新增用户 - 根据通讯录建立关系
	 * 
	 * @param tel
	 *            当前手机号
	 * @param contacts
	 *            通讯录
	 * @return
	 */
	public String add(String tel, List<String> contacts) {
		// 生成新用户
		String userId = String.valueOf(new Random().nextInt(100));
		addContacts(userId, tel, contacts);
		return userId;
	}

	/**
	 * 增加联系人
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void addContacts(String userId, String tel, List<String> contacts) {
		// build <d>
		jedis.set(KEY_USERTEL_PRE + tel, userId);
		// build <a>
		for (String t : contacts) {
			jedis.sadd(KEY_TEL_PRE + t, tel);
		}

		// 维护fllow关系
		List<String> keys = new ArrayList<String>();
		for (String t : contacts) {
			keys.add(KEY_USERTEL_PRE + t);
		}
		List<String> tmp = jedis.mget(toArray(keys));
		List<String> follows = new ArrayList<String>();
		for (String id : tmp) {
			if (id != null) {
				follows.add(id);
			}
		}
		if (follows.size() > 0) {
			jedis.sadd(KEY_FOLLOW_PRE + userId, toArray(follows));
			for (String id : follows) {
				jedis.sadd(KEY_FANS_PRE + id, userId);
			}
		}

		// 维护fans关系
		Set<String> set = jedis.smembers(KEY_TEL_PRE + tel);
		if (set.size() > 0) {
			keys = new ArrayList<String>();
			for (String t : set) {
				keys.add(KEY_USERTEL_PRE + t);
			}
			tmp = jedis.mget(toArray(keys));
			List<String> fans = new ArrayList<String>();
			for (String id : tmp) {
				if (id != null) {
					fans.add(id);
					jedis.sadd(KEY_FOLLOW_PRE + id, userId);
				}
			}
			if (fans.size() > 0) {
				jedis.sadd(KEY_FANS_PRE + userId, toArray(fans));
			}
		}
	}
	
	/**
	 * 移除联系人
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void removeContacts(String userId, String tel, List<String> contacts) {
		for (String t : contacts) {
			jedis.srem(KEY_TEL_PRE + t, tel);
		}
		
		List<String> keys = new ArrayList<String>();
		for (String t : contacts) {
			keys.add(KEY_USERTEL_PRE + t);
		}
		List<String> tmp = jedis.mget(toArray(keys));
		List<String> follows = new ArrayList<String>();
		for (String id : tmp) {
			if (id != null) {
				follows.add(id);
			}
		}
		if (follows.size() > 0) {
			jedis.srem(KEY_FOLLOW_PRE + userId, toArray(follows));
			for (String id : follows) {
				jedis.srem(KEY_FANS_PRE + id, userId);
			}
		}
	}

	/**
	 * 发布商品
	 * 
	 * @param userId
	 */
	public void publishGoods(String userId, String goodsId) {
		jedis.lpush(KEY_PUBLISH_PRE + userId, goodsId);

		// 二度推送--异步
		Set<String> fans = findFans(userId);
		for (String uid : fans) {
			jedis.lpush(KEY_PUSH_PRE + uid, goodsId);
			Set<String> fans2 = findFans(uid);
			for (String uid2 : fans2) {
				jedis.lpush(KEY_PUSH_PRE + uid2, goodsId);
			}
		}
	}

	/**
	 * 查看商品列表
	 * 
	 * @param userId
	 * @return
	 */
	public List<String> findGoods(String userId) {
		List<String> list = jedis.lrange(KEY_PUSH_PRE + userId, 0, -1);
		return list;
	}
	
	/**
	 * 查二度关系
	 * 商品发布人的fans和当前用户的关注的交集，即朋友的朋友
	 * @param userId
	 * @param publishUserId
	 * @return
	 */
	public Set<String> findLinkByGoods(String userId, String publishUserId){
		return jedis.sinter(KEY_FANS_PRE+publishUserId, KEY_FOLLOW_PRE+userId);
	}

	/**
	 * 查fans
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFans(String userId) {
		return jedis.smembers(KEY_FANS_PRE + userId);
	}

	/**
	 * 查关注
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFollow(String userId) {
		return jedis.smembers(KEY_FOLLOW_PRE + userId);
	}

	private String[] toArray(List<String> list) {
		return list.toArray(new String[] {});
	}
}
