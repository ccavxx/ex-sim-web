package com.topsec.tsm.sim.util;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;




public class SessionListener implements HttpSessionBindingListener {
	
	private String sessionId;
	
	public SessionListener(String sessionId){
		this.sessionId = sessionId;
	}

	//session注册后执行
	public void valueBound(HttpSessionBindingEvent event) {
		
	}
	
	/*
	 * session注销时执行(non-Javadoc)
	 *释放SessionMrg中该session所占用空间
	 */
	public void valueUnbound(HttpSessionBindingEvent event) {
		// 释放SessionMrg中该session所占用空间
		HashMap sessions = SessionMrg.getInstance().getSessionMap();
		if(sessions.containsKey(this.sessionId)){
			SessionMrg.removeMsg(this.sessionId);
			
			String path = null;
			try {
				path = getDataDir("sim/flex/position/Event_"+this.sessionId+".xls",event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File f = new File(path);
			if(f.exists()){
				f.delete();
			}
//			System.out.println("==========================================");
//			System.out.println("session"+this.sessionId+"因过久不访问而释放");
//			System.out.println("===========================================");
			//通知重新登陆
		}
	}
	private String getDataDir(String temp,HttpSessionBindingEvent event) throws Exception {
		String dir = event.getSession().getServletContext().getResource("/")
				+ temp;
		URI uri = new URI(dir);
		dir = uri.getPath();
		if (dir.charAt(0) == '/') {
			dir = dir.substring(1);
		}
		return dir;
	}

}
