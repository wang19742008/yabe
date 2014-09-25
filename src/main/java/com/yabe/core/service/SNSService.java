package com.yabe.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;

import com.yabe.core.common.Contents;
import com.yabe.core.common.Utils;
import com.yabe.core.dto.Contacts;
import com.yabe.core.dto.Goods;

/**
 * 
 * 缓存结构: 
 * 通讯录fans关系---通讯录中电话 -> 用户电话-----t_1360003 -> {135000,136000,136001}
 * 用户fans关系-----用户ID->粉丝ID---------------f_100 -> {101}
 * 用户follow关系---用户ID->关注人ID-------------g_101 -> {100}
 * 电话到Uers的映射-用户电话->用户ID-------------a_1360003 -> 100
 * 商品发布列表-----用户ID->商品ID集合(goodsid,pubtime)-----------p_100 -> [1/13623232,]
 * 商品推送列表-----用户ID->推送商品ID集合(goodsid,linktype,sellerid)------ c_100 -> [1/1/2,]
 * 联系人nick-------用户ID+联系人ID->nick ------ n_100_101 -> xx
 * 
 * @author lingkong
 *
 */

public class SNSService implements ISNSService{
	private Jedis redis;

	private final String KEY_TEL_PRE = "t_";
	private final String KEY_USERTEL_PRE = "a_";
	private final String KEY_FOLLOW_PRE = "g_";
	private final String KEY_FANS_PRE = "f_";
	private final String KEY_PUBLISH_PRE = "p_";
	private final String KEY_PUSH_PRE = "c_";
	private final String KEY_NICK = "n_";

	@Override
	public String add(String userId,String tel, List<Contacts> contacts)  throws Exception{
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
			goods.setLinkType(Contents.LINK_TYPE_1);
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
			pushGoods(uid,new Goods(goodsId,Contents.LINK_TYPE_1,userId));
			Set<String> fans2 = findFans(uid);
			for (String uid2 : fans2) {
				pushGoods(uid2,new Goods(goodsId,Contents.LINK_TYPE_2,userId));
			}
		}
	}
	
	@Override
	public void pushGoods(String userId,Goods goods) throws Exception{
		if(StringUtils.isBlank(userId) || goods == null  || StringUtils.isBlank(goods.getId()) || StringUtils.isBlank(goods.getLinkType()) || StringUtils.isBlank(goods.getUserId())){
			throw new Exception("param is null");
		}
		redis.lpush(KEY_PUSH_PRE + userId, Utils.joinV(goods.getId(),goods.getLinkType(),goods.getUserId()));
	}
	
	@Override
	public void pushGoods(String userId,List<Goods> list) throws Exception{
		if(list == null || list.size()==0){
			return;
		}
		List<String> r = new ArrayList<String>();
		for(Goods goods : list){
			r.add(Utils.joinV(goods.getId(),goods.getLinkType().toString(),goods.getUserId()));
		}
		redis.lpush(KEY_PUSH_PRE + userId, r.toArray(new String[]{}));
	}

	@Override
	public List<Goods> findGoods(String userId, int offset, int count) throws Exception{
		String k = KEY_PUSH_PRE + userId;
		List<Goods> rt = new ArrayList<Goods>();
		
		List<String> list = redis.lrange(k, offset, offset+count);
		for(String s : list){
			String[] arr = Utils.splitV(s);
			Goods g = new Goods();
			g.setId(arr[0]);
			g.setLinkType(arr[1]);
			g.setUserId(arr[2]);
			
			if(Contents.LINK_TYPE_1.equals(g.getLinkType())){
				g.setNick(getContactsNick(userId,g.getUserId()));
			}
			rt.add(g);
		}
		
		return rt;
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
	private void setContactsNick(String userId, String ContactsUserId, String nick){
		String k = getContactsNickKey(userId,ContactsUserId);
		redis.set(k, nick);
	}
	private String getContactsNick(String userId, String ContactsUserId){
		String k = getContactsNickKey(userId,ContactsUserId);
		return redis.get(k);
	}
	private String getContactsNickKey(String userId, String ContactsUserId){
		return KEY_NICK + userId+"_"+ContactsUserId;
	}
	private List<String> getContactsNicks(List<String> keys){
		return redis.mget(keys.toArray(new String[]{}));
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

	public void setRedis(Jedis redis) {
		this.redis = redis;
	}
}
