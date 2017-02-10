package com.topsec.tsm.sim.auth.security;

public class AttackException extends RuntimeException{
	
	private static final long serialVersionUID = 705818367163485480L;
	
	private Attack attack ;

	public AttackException(Attack attack) {
		super();
		this.attack = attack;
	}
	
	public Attack getAttack() {
		return attack;
	}

	public void setAttack(Attack attack) {
		this.attack = attack;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	
}
