package com.yabe.core.dto;

public class Goods {
	
	private String id;
	private String linkType;
	private String userId;
	private String nick;
	private Long pubDateTime;
	
	public Goods(){
		
	}
	public Goods(String id, String linkType, String userId){
		this.id = id;
		this.linkType = linkType;
		this.userId = userId;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getPubDateTime() {
		return pubDateTime;
	}
	public void setPubDateTime(Long pubDateTime) {
		this.pubDateTime = pubDateTime;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	
}
