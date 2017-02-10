package com.topsec.tsm.sim.common.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.topsec.tsm.util.StringFormater;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Result implements Serializable{
	private static final long serialVersionUID = -6264725810388868492L;
	@XmlAttribute
	private boolean status = true;
	@XmlAttribute
	private String message;
	private Object result;
	public Result() {
		super();
	}

	public Result(boolean status, String message) {
		this.status = status;
		this.message = message;
	}

	public boolean isStatus() {
		return status;
	}

	public boolean isSuccess(){
		return status ;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Result build(boolean status,String message,Object result){
		this.status = status ;
		this.message = message ;
		this.result = result ;
		return this ;
	}
	public Result build(boolean status,String message){
		return build(status, message,null) ;
	}
	
	public Result build(boolean status){
		return build(status,null,null) ;
	}

	public Result buildSuccess(Object result) {
		return build(true,null,result) ;
	}
	
	public Result buildSuccess(){
		return build(true, null,null) ;
	}
	
	public Result buildError(String message){
		return build(false,message) ;
	}
	
	public Result buildSystemError(){
		return build(false, "系统出错") ;
	}
	
	public Result buildError(String message,Object... arguments){
		return build(false, StringFormater.format(message, arguments)) ;
	}
}
