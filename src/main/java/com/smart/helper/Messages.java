package com.smart.helper;

public class Messages {

	private String content;
	private String type;
	
	public Messages(String content, String type) {
		super();
		this.content = content;
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
