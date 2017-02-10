package com.topsec.tsm.sim.report.bean.base;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.topsec.tsm.sim.auth.util.SID;


/**
 * <dd>类名：会话变量存储实体
 * <dd>类说明：会话变量存储实体
 * <dd>备注
 * 
 * @version 1.01 2009/04/30
 * @author TopSec)Rick
 */
public class BaseSession extends Session {
	/**
	 * 会话标志字符串
	 */
	static final String SESSION = "SESSION";

	private SID user = null;

	/**
	 * <dd>方法名：构造方法
	 * <dd>功能概要：对会话变量存储实体的事例化
	 * <dd>备注
	 * 
	 * @param request
	 */
	public BaseSession(HttpServletRequest request) {
		super(request);
	}

	/**
	 * <dd>方法名：初期化会话实体Cgݒ
	 * <dd>功能概要：初期化会话实体
	 * <dd>备注：重写底层方法
	 * 
	 * @param request
	 */
	public Object newSession(HttpServletRequest request) {

		if (getSession(request) == null) {
			request.getSession().setAttribute(SESSION, this);
		}
		return this;

	}

	/**
	 * <dd>方法名：对会话变量存储实体的取得Cgݒ
	 * <dd>功能概要：对会话变量存储实体的取得
	 * <dd>备注
	 * 
	 * @param request
	 * @return BaseSession
	 */
	public static BaseSession getSession(HttpServletRequest request) {

		BaseSession se = (BaseSession) request.getSession().getAttribute(SESSION);

		if (se == null) {
			se = new BaseSession(request);
			request.getSession().setAttribute(SESSION, se);
		}

		return se;
	}

	/**
	 * <dd>方法名：消除会话实体Cgݒ
	 * <dd>功能概要：消除会话实体
	 * <dd>备注：重写底层方法
	 * 
	 * @param request
	 */
	public void delSession(HttpServletRequest request) {
		request.getSession().removeAttribute(SESSION);
	}

	/**
	 * <dd>方法名：得到用户信息Cgݒ
	 * <dd>功能概要：得到用户信息
	 * <dd>备注
	 * 
	 * @return UserInfo tHbJXڐݒl
	 */
	public SID getUser() {
		return user;
	}

	/**
	 * <dd>方法名：设定用户信息
	 * <dd>功能概要：设定用户信息
	 * <dd>备注
	 * 
	 * @param user
	 */

	public void setUser(SID user) {
		this.user = user;
	}

}

abstract class Session implements HttpSession {

	public abstract Object newSession(HttpServletRequest httpservletrequest);

	public abstract void delSession(HttpServletRequest httpservletrequest);

	public Session(HttpServletRequest request) {
		session = request.getSession();
	}

	public long getCreationTime() {
		return session.getCreationTime();
	}

	public String getId() {
		return session.getId();
	}

	public long getLastAccessedTime() {
		return session.getLastAccessedTime();
	}

	public ServletContext getServletContext() {
		return null;
	}

	public void setMaxInactiveInterval(int arg0) {
		session.setMaxInactiveInterval(arg0);
	}

	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}

	public HttpSessionContext getSessionContext() {
		return session.getSessionContext();
	}

	public Object getAttribute(String arg0) {
		return session.getAttribute(arg0);
	}

	public Object getValue(String arg0) {
		return session.getValue(arg0);
	}

	public Enumeration getAttributeNames() {
		return session.getAttributeNames();
	}

	public String[] getValueNames() {
		return session.getValueNames();
	}

	public void setAttribute(String arg0, Object arg1) {
		session.setAttribute(arg0, arg1);
	}

	public void putValue(String arg0, Object arg1) {
		session.putValue(arg0, arg1);
	}

	public void removeAttribute(String arg0) {
		session.removeAttribute(arg0);
	}

	public void removeValue(String arg0) {
		session.removeValue(arg0);
	}

	public void invalidate() {
		session.invalidate();
	}

	public boolean isNew() {
		return session.isNew();
	}

	public static final String SESSION = "SESSION";
	HttpSession session;
}
