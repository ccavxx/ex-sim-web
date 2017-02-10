package com.topsec.tsm.sim.auth.security;

public class Attack {

	public AttackType type ;
	public String content ;
	
	public Attack(AttackType type, String content) {
		super();
		this.type = type;
		this.content = content;
	}
	public AttackType getType() {
		return type;
	}
	public void setType(AttackType type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
