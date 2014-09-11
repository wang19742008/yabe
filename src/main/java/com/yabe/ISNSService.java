package com.yabe;

import java.util.List;
import java.util.Set;

public interface ISNSService {
	/**
	 * 新增用户 - 根据通讯录建立关系
	 * 
	 * @param tel
	 *            当前手机号
	 * @param contacts
	 *            通讯录
	 * @return
	 */
	public String add(String tel, List<String> contacts);

	/**
	 * 增加联系人
	 *  联系人列表变更，调用维护link关系
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void addContacts(String userId, String tel, List<String> contacts);
	
	/**
	 * 移除联系人
	 * 联系人列表变更，调用维护link关系
	 * @param userId
	 * @param tel
	 * @param contacts
	 */
	public void removeContacts(String userId, String tel, List<String> contacts);

	/**
	 * 发布商品
	 * 
	 * @param userId
	 */
	public void publishGoods(String userId, String goodsId);

	/**
	 * 查看商品列表
	 * 
	 * @param userId
	 * @return
	 */
	public List<String> findGoods(String userId);
	
	/**
	 * 查二度关系
	 * 商品发布人的fans和当前用户的关注的交集，即朋友的朋友
	 * @param userId
	 * @param publishUserId
	 * @return
	 */
	public Set<String> findLinkByGoods(String userId, String publishUserId);

	/**
	 * 查fans
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFans(String userId);

	/**
	 * 查关注
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> findFollow(String userId);
}
