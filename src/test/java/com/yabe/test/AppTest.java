package com.yabe.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.yabe.UserService;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}


	public void testAdd() {
		UserService service = new UserService();
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
		
		Assert.assertTrue(service.findFans(u1).contains(u2));
		Assert.assertTrue(service.findFans(u2).contains(u1));
	}
	
	public void testPublishGoods() {
		UserService service = new UserService();
		service.publishGoods("");
	}
	
	public void testFindGoods() {
		UserService service = new UserService();
		List<String> list = service.findGoods("100");
		for(String t : list){
			System.out.println(t);
		}
	}
}
