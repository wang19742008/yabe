package com.yabe.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.yabe.RedisHolder;
import com.yabe.SNSService;

public class AppTest extends TestCase {
	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}
	@Override
	protected void setUp() throws Exception {
		//清数据
		Set<String> keys = RedisHolder.getInstance().keys("*");
		for(String k : keys){
			if(k.startsWith("a_") || k.startsWith("t_") || k.startsWith("f_") || k.startsWith("g_")){
				RedisHolder.getInstance().del(k);
			}
		}
	}

	/**
	 * 新增用户case
	 */
	public void testAdd() {
		SNSService service = new SNSService();
		String tel = "";
		List<String> contacts;
		/**
		 * 用户13601
		 * 通讯录：
		 * 		13602
		 * 		13603
		 * 		13604
		 */
		tel = "13601";
		contacts = new ArrayList<String>();
		contacts.add("13602");
		contacts.add("13603");
		contacts.add("13604");
		String u1 = service.add(tel, contacts);
		/**
		 * 用户13602
		 * 通讯录：
		 * 		13601
		 */
		tel = "13602";
		contacts = new ArrayList<String>();
		contacts.add("13601");
		String u2 = service.add(tel, contacts);
		
		/**
		 * 用户13603
		 * 通讯录：
		 * 		13602
		 */
		tel = "13603";
		contacts = new ArrayList<String>();
		contacts.add("13602");
		String u3 = service.add(tel, contacts);
		
		Assert.assertTrue(service.findFans(u1).contains(u2));
		Assert.assertTrue(service.findFans(u2).contains(u1));
		Assert.assertTrue(service.findFans(u3).contains(u1));
		Assert.assertTrue(service.findFans(u2).contains(u3));
		
		Assert.assertTrue(service.findFollow(u1).contains(u2));
		Assert.assertTrue(service.findFollow(u1).contains(u3));
		Assert.assertTrue(service.findFollow(u2).contains(u1));
		Assert.assertTrue(service.findFollow(u3).contains(u2));
		
	}
	
	public void testPublishGoods() {
		SNSService service = new SNSService();
		String tel = "";
		List<String> contacts;
		/**
		 * 用户13701
		 * 通讯录：
		 * 		13702
		 */
		tel = "13701";
		contacts = new ArrayList<String>();
		contacts.add("13702");
		String u1 = service.add(tel, contacts);
		/**
		 * 用户13702
		 * 通讯录：
		 * 		13701
		 *      13703
		 */
		tel = "13702";
		contacts = new ArrayList<String>();
		contacts.add("13701");
		contacts.add("13703");
		String u2 = service.add(tel, contacts);
		
		/**
		 * 用户13703
		 * 通讯录：
		 * 		13705
		 */
		tel = "13703";
		contacts = new ArrayList<String>();
		contacts.add("13705");
		String u3 = service.add(tel, contacts);
		
		//一度
		List<String> list1 = service.findGoods(u2);
		service.publishGoods(u1,"10000");
		List<String> list2 = service.findGoods(u2);
		
		Assert.assertTrue((list2.size()-list1.size()) == 1);
		
		//二度
		list1 = service.findGoods(u1);
		service.publishGoods(u3,"10000");
		list2 = service.findGoods(u1);
		
		Assert.assertTrue((list2.size()-list1.size()) == 1);
		
		//朋友的朋友
		Set<String> set = service.findLinkByGoods(u1, u3);
		Assert.assertTrue(set.contains(u2));
	}
	
	
	public void testa(){
		RedisHolder.getInstance().lpush("p_94", "10000");
	}
}
