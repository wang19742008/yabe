package com.yabe.test;

import java.util.ArrayList;
import java.util.List;

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
		
		String tel = "10000000000";
		List<String> contacts = new ArrayList<String>();
		contacts.add("10000000001");
		contacts.add("10000000002");
		contacts.add("10000000003");
		contacts.add("10000000004");
		
		long st = System.currentTimeMillis();
		service.add(tel, contacts);
		System.out.println(System.currentTimeMillis()-st);
		
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
