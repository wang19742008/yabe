package com.yabe.core.service;

import java.util.List;
import java.util.Set;

import com.yabe.core.dto.Contacts;
import com.yabe.core.dto.Goods;

/**
 * SNS服务
 * @author lingkong 
 *
 */
public interface ISNSService {
	/**
	 * 新增用户 - 根据通讯录建立关系/新建推送列表
	 * @param userId
	 * @param tel 当前手机号
	 * @param contacts 通讯录
	 * @return
	 * @throws Exception
	 */
	public String add(String userId, String tel, List<Contacts> contacts) throws Exception;

	/**
	 * 增加联系人
	 *  联系人列表变更，调用维护link关系
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void addContacts(String userId, String tel, List<Contacts> contacts) throws Exception;
	
	/**
	 * 移除联系人
	 * 联系人列表变更，调用维护link关系
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void removeContacts(String userId, String tel, List<Contacts> contacts) throws Exception;

	/**
	 * 发布商品
	 * 
	 * @param userId
	 */
	public void publishGoods(String userId, String goodsId) throws Exception;

	/**
	 * 查看商品列表
	 * 
	 * @param userId
	 * @return
	 */
	public List<Goods> findGoods(String userId, int offset, int count) throws Exception;
	
	/**
	 * 查二度关系
	 * 商品发布人的fans和当前用户的关注的交集，即朋友的朋友
	 * @param userId
	 * @param publishUserId
	 * @return
	 */
	public Set<String> findLinkByGoods(String userId, String publishUserId) throws Exception;

	/**
	 * 查fans
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFans(String userId) throws Exception;

	/**
	 * 查关注
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFollow(String userId) throws Exception;
	
	/**
	 * 推送商品
	 * @param userId
	 * @param goodsId
	 * @param linkType
	 * @param sellerId
	 * @throws Exception
	 */
	public void pushGoods(String userId,Goods goods) throws Exception;
	
	/**
	 * 
	 * @param userId
	 * @param list
	 * @throws Exception
	 */
	public void pushGoods(String userId,List<Goods> list) throws Exception;
}
