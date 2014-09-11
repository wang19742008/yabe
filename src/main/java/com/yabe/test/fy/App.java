package com.yabe.test.fy;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 */
public class App {
	public static void main(String[] args) {
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
		
		
		tel = "10000000001";
		contacts = new ArrayList<String>();
		contacts.add("10000000000");
		contacts.add("10000000002");
		contacts.add("10000000005");
		contacts.add("10000000006");
		service.add(tel, contacts);
	}
}
