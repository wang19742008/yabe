package com.yabe.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.yabe.core.common.RedisHolder;
import com.yabe.core.common.Utils;
import com.yabe.core.dto.Contacts;
import com.yabe.core.dto.Goods;

/**
 * 
 * 缓存结构: 
 * a.通讯录fans关系---通讯录中电话 -> 用户电话-----t_1360003 -> [135000,136000,136001]
 * b.用户fans关系-----用户ID->粉丝ID---------------f_100 -> [101]
 * c.用户follow关系---用户ID->关注人ID-------------g_101 -> [100]
 * d.电话到Uers的映射-用户电话->用户ID-------------a_1360003 -> 100
 * e.商品发布列表-----用户ID->商品ID集合-----------p_100 -> [111,222]
 * f.商品推送列表-----用户ID->推送商品ID集合------ c_100 -> [111,222]
 * g.联系人nick-------用户ID+联系人ID->nick ------ n_100_101 -> xx
 * 
 * @author lingkong
 *
 */

public class SNSService implements ISNSService{
	private Jedis redis = RedisHolder.getInstance();

	private final String KEY_TEL_PRE = "t_";
	private final String KEY_USERTEL_PRE = "a_";
	private final String KEY_FOLLOW_PRE = "g_";
	private final String KEY_FANS_PRE = "f_";
	private final String KEY_PUBLISH_PRE = "p_";
	private final String KEY_PUSH_PRE = "c_";
	private final String KEY_NICK = "n_";

	@Override
	public String add(String userId,String tel, List<Contacts> contacts)  throws Exception{
		//String userId = String.valueOf(new Random().nextInt(100));
		addContacts(userId, tel, contacts);
		
		//初始化推送列表
		List<Goods> list = new ArrayList<Goods>();
		Set<String> f1 = findFollow(userId);
		//暂只处理1度关系
		for(String uid : f1){
			if(list.size()>100){
				break;
			}
			list.addAll(findPubGoods(uid,10));
		}
		//按发布时间倒序
		Collections.sort(list, new Comparator<Goods>(){
			@Override
			public int compare(Goods o1, Goods o2) {
				return o2.getPubDateTime().compareTo(o1.getPubDateTime());
			}
		});
		//推送商品--只有一度关系
		for(Goods goods : list){
			goods.setLinkType("1");
		}
		pushGoods(userId,list);
		return userId;
	}

	@Override
	public void addContacts(String userId, String tel, List<Contacts> contacts) throws Exception{
		// build <d>
		redis.set(KEY_USERTEL_PRE + tel, userId);
		// build <a>
		for (Contacts t : contacts) {
			redis.sadd(KEY_TEL_PRE + t.getTel(), tel);
		}

		// 维护fllow关系
		List<String> keys = new ArrayList<String>();
		for (Contacts t : contacts) {
			keys.add(KEY_USERTEL_PRE + t.getTel());
		}
		Map<String,String> nickMap = new HashMap<String,String>();
		List<String> tmp = redis.mget(toArray(keys));
		List<String> follows = new ArrayList<String>();
		for (int i=0;i<tmp.size();i++) {
			if (tmp.get(i) != null) {
				follows.add(tmp.get(i));
				nickMap.put(tmp.get(i), contacts.get(i).getNick());
			}
		}
		if (follows.size() > 0) {
			redis.sadd(KEY_FOLLOW_PRE + userId, toArray(follows));
			for (String id : follows) {
				redis.sadd(KEY_FANS_PRE + id, userId);
				
				//记录联系人昵称
				setContactsNick(userId,id,nickMap.get(id));
			}
		}

		// 维护fans关系
		Set<String> set = redis.smembers(KEY_TEL_PRE + tel);
		if (set.size() > 0) {
			keys = new ArrayList<String>();
			for (String t : set) {
				keys.add(KEY_USERTEL_PRE + t);
			}
			tmp = redis.mget(toArray(keys));
			List<String> fans = new ArrayList<String>();
			for (String id : tmp) {
				if (id != null) {
					fans.add(id);
					redis.sadd(KEY_FOLLOW_PRE + id, userId);
				}
			}
			if (fans.size() > 0) {
				redis.sadd(KEY_FANS_PRE + userId, toArray(fans));
			}
		}
	}
	
	private void setContactsNick(String userId, String ContactsUserId, String nick){
		String k = KEY_NICK + userId+"_"+ContactsUserId;
		redis.set(k, nick);
	}
	
	private String getContactsNick(String userId, String ContactsUserId){
		String k = KEY_NICK + userId+"_"+ContactsUserId;
		return redis.get(k);
	}
	
	
	@Override
	public void removeContacts(String userId, String tel, List<Contacts> contacts) throws Exception{
		for (Contacts t : contacts) {
			redis.srem(KEY_TEL_PRE + t.getTel(), tel);
		}
		
		List<String> keys = new ArrayList<String>();
		for (Contacts t : contacts) {
			keys.add(KEY_USERTEL_PRE + t.getTel());
		}
		List<String> tmp = redis.mget(toArray(keys));
		List<String> follows = new ArrayList<String>();
		for (String id : tmp) {
			if (id != null) {
				follows.add(id);
			}
		}
		if (follows.size() > 0) {
			redis.srem(KEY_FOLLOW_PRE + userId, toArray(follows));
			for (String id : follows) {
				redis.srem(KEY_FANS_PRE + id, userId);
			}
		}
	}

	@Override
	public void publishGoods(String userId, String goodsId) throws Exception{
		redis.lpush(KEY_PUBLISH_PRE + userId, Utils.joinV(goodsId,String.valueOf(System.currentTimeMillis()/1000)));
		// 二度推送--异步
		Set<String> fans = findFans(userId);
		for (String uid : fans) {
			pushGoods(uid,new Goods(goodsId,"1",userId));
			Set<String> fans2 = findFans(uid);
			for (String uid2 : fans2) {
				pushGoods(uid2,new Goods(goodsId,"2",userId));
			}
		}
	}
	
	private List<Goods> findPubGoods(String userId, int count){
		List<Goods> r = new ArrayList<Goods>();
		List<String> list = redis.lrange(KEY_PUBLISH_PRE + userId, 0, count-1);
		for(String s : list){
			String[] arr = Utils.splitV(s);
			Goods goods = new Goods();
			goods.setId(arr[0]);
			goods.setUserId(userId);
			goods.setPubDateTime(Long.valueOf(arr[1]));
			r.add(goods);
		}
		return r;
	}
	
	@Override
	public void pushGoods(String userId,Goods goods) throws Exception{
		redis.lpush(KEY_PUSH_PRE + userId, Utils.joinV(goods.getId(),goods.getLinkType(),goods.getUserId()));
	}
	
	@Override
	public void pushGoods(String userId,List<Goods> list) throws Exception{
		List<String> r = new ArrayList<String>();
		for(Goods goods : list){
			r.add(Utils.joinV(goods.getId(),goods.getLinkType().toString(),goods.getUserId()));
		}
		redis.lpush(KEY_PUSH_PRE + userId, r.toArray(new String[]{}));
	}

	@Override
	public List<String> findGoods(String userId) throws Exception{
		List<String> list = redis.lrange(KEY_PUSH_PRE + userId, 0, -1);
		return list;
	}

	@Override
	public Set<String> findLinkByGoods(String userId, String publishUserId) throws Exception{
		return redis.sinter(KEY_FANS_PRE+publishUserId, KEY_FOLLOW_PRE+userId);
	}

	@Override
	public Set<String> findFans(String userId) throws Exception{
		return redis.smembers(KEY_FANS_PRE + userId);
	}

	@Override
	public Set<String> findFollow(String userId) throws Exception{
		return redis.smembers(KEY_FOLLOW_PRE + userId);
	}

	private String[] toArray(List<String> list) {
		return list.toArray(new String[] {});
	}
}
