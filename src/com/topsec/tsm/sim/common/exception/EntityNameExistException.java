package com.topsec.tsm.sim.common.exception;

/**
 * 名称已经存在异常
 * @author hp
 *
 */
public class EntityNameExistException extends Exception {

	/**
	 * 实体名称
	 */
	private String name ;

	public EntityNameExistException(String name) {
		this.name = name ;
	}

	public EntityNameExistException(String name,String message) {
		super(message);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
