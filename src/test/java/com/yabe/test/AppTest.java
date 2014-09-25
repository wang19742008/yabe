package com.yabe.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.yabe.core.common.Constants;
import com.yabe.core.dto.Contacts;
import com.yabe.core.dto.Goods;
import com.yabe.core.service.SNSService;

public class AppTest extends TestCase {
	private SNSService service;
	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		service = new SNSService();
		service.setRedis(RedisHolder.getInstance());
		// 清数据
		Set<String> keys = RedisHolder.getInstance().keys("*");
		for (String k : keys) {
			if (k.startsWith("a_") || k.startsWith("t_") || k.startsWith("f_")
					|| k.startsWith("g_") || k.startsWith("c_")
					|| k.startsWith("p_") || k.startsWith("n_")) {
				RedisHolder.getInstance().del(k);
			}
		}
	}

	/**
	 * 新增用户case
	 * @throws Exception 
	 */
	public void testAdd() throws Exception {
		String userId = "";
		String tel = "";
		List<Contacts> contacts;
		/**
		 * 用户13601 通讯录： 13602 13603 13604
		 */
		userId = "1";
		tel = "13601";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13602","B"));
		contacts.add(new Contacts("13603","C"));
		contacts.add(new Contacts("13604","D"));
		String u1 = service.add(userId,tel, contacts);
		/**
		 * 用户13602 通讯录： 13601
		 */
		userId = "2";
		tel = "13602";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13601","A"));
		String u2 = service.add(userId,tel, contacts);

		/**
		 * 用户13603 通讯录： 13602
		 */
		userId = "3";
		tel = "13603";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13602","B3"));
		String u3 = service.add(userId,tel, contacts);

		Assert.assertTrue(service.findFans(u1).contains(u2));
		Assert.assertTrue(service.findFans(u2).contains(u1));
		Assert.assertTrue(service.findFans(u3).contains(u1));
		Assert.assertTrue(service.findFans(u2).contains(u3));

		Assert.assertTrue(service.findFollow(u1).contains(u2));
		Assert.assertTrue(service.findFollow(u1).contains(u3));
		Assert.assertTrue(service.findFollow(u2).contains(u1));
		Assert.assertTrue(service.findFollow(u3).contains(u2));

	}

	public void testPublishGoods() throws Exception {
		String userId = "";
		String tel = "";
		List<Contacts> contacts;
		/**
		 * 用户13701 通讯录： 13702
		 */
		userId = "1";
		tel = "13701";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13702","x2"));
		String u1 = service.add(userId,tel, contacts);
		/**
		 * 用户13702 通讯录： 13701 13703
		 */
		userId = "2";
		tel = "13702";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13701","x1"));
		contacts.add(new Contacts("13703","x3"));
		String u2 = service.add(userId,tel, contacts);

		/**
		 * 用户13703 通讯录： 13705
		 */
		userId = "3";
		tel = "13703";
		contacts = new ArrayList<Contacts>();
		contacts.add(new Contacts("13705","x5"));
		String u3 = service.add(userId,tel, contacts);

		// 一度
		List<Goods> list1 = service.findGoods(u2,0,10);
		service.publishGoods(u1, "10000");
		List<Goods> list2 = service.findGoods(u2,0,10);

		Assert.assertTrue((list2.size() - list1.size()) == 1);
		Assert.assertTrue(list2.get(0).getId().equals("10000"));
		Assert.assertTrue(list2.get(0).getLinkType().equals(Constants.LINK_TYPE_1));
		Assert.assertTrue(list2.get(0).getNick().equals("x1"));

		// 二度
		list1 = service.findGoods(u1,0,10);
		service.publishGoods(u3, "10001");
		list2 = service.findGoods(u1,0,10);

		Assert.assertTrue((list2.size() - list1.size()) == 1);
		Assert.assertTrue(list2.get(0).getId().equals("10001"));
		Assert.assertTrue(list2.get(0).getLinkType().equals(Constants.LINK_TYPE_2));
		Assert.assertTrue(list2.get(0).getNick()==null);

		// 朋友的朋友
		Set<String> set = service.findLinkByGoods(u1, u3);
		Assert.assertTrue(set.contains(u2));
	}

	public void testa() {
		RedisHolder.getInstance().lpush("p_94", "10000");
	}
}
