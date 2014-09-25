package com.yabe.core.dto;

/**
 * 联系人
 * @author lingkong
 *
 */
public class Contacts {
	private String tel;
	private String nick;
	
	public Contacts(String tel, String nick){
		this.tel = tel;
		this.nick = nick;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
}
