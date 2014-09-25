package com.yabe.core.common;

import org.apache.commons.lang.StringUtils;


public class Utils {
	public static String joinV(String... str){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<str.length;i++){
			if(i>0){
				sb.append((char)1);
			}
			sb.append(str[i]);
		}
		return sb.toString();
	}
	
	public static String[] splitV(String s){
		return StringUtils.splitPreserveAllTokens(s, (char)1);
	}
}
