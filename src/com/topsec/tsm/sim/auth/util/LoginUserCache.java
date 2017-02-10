package com.topsec.tsm.sim.auth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.common.message.AuditorCommandDispatcher;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.util.ticker.Tickerable;



public class LoginUserCache implements Tickerable {
	private Map<String,SID> cache = new ConcurrentHashMap<String, SID>();
	private static LoginUserCache instance = new LoginUserCache();
	private static final int MAX_ONLINE_USER_COUNT = 20;//最大在线用户数
	
	private LoginUserCache() {
		
	}
	
	//单例模式
	public static LoginUserCache getInstance() {
		return instance;
	}
	/**
	 * 根据用户名删除缓存中的用户信息
	 * @author zhou_xiaohu
	 * @param userName
	 */
	public synchronized void removeUser(String userName){
		if(cache.containsKey(userName))
			cache.remove(userName);
		if(cache.size() == 0){//没有用户在线时，通知auditor所有用户已经退出
			AuditorCommandDispatcher unt = new AuditorCommandDispatcher(MessageDefinition.CMD_NODE_ALL_LOGOUT,null) ;
			unt.start() ;
		}
	}
	
	public boolean isExist(String userName){
		return cache.containsKey(userName);
	}
	/**
	 * 添加用户信息到缓存中
	 * @author zhou_xiaohu
	 * @param account
	 */
	public synchronized boolean addUser(SID sid){
		if(cache.size() < MAX_ONLINE_USER_COUNT){
			if(cache.size() == 0){//第一次有用户登录时，通知auditor当前有用户登录
				AuditorCommandDispatcher unt = new AuditorCommandDispatcher(MessageDefinition.CMD_NODE_FIRST_LOGIN,null) ;
				unt.start() ;
			}
			cache.put(sid.getUserName(),sid);
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 根据用户名获取用户信息
	 * @author zhou_xiaohu
	 * @param userName
	 * @return
	 */
	public SID getLoginUserCachByName(String userName){
		return cache.get(userName);
	}
	/**
	 * 更新用户登陆时间
	 * @param userName
	 * @param now
	 */
	public void updateUserLoginTime(String userName,long now){
		SID sid =  cache.get(userName);
		sid.setLastLoginTime(now);
	}
	
	public synchronized List<SID> getOnlineUsers(){
		List<SID> result = new ArrayList<SID>(cache.size()) ;
		for(Map.Entry<String, SID> entry:cache.entrySet()){
			if(entry.getKey().equals(SID.ADMINISTRATOR)){
				continue ;
			}
			result.add(entry.getValue()) ;
		}
		return result ;
	}
	public int getOnlineUserCount(){
		return cache.size() ;
	}
	public void deleteAuthDevice(String deviceId){
		AuthUserDevice aud = new AuthUserDevice(deviceId) ;
		for(Map.Entry<String, SID> entry:cache.entrySet()){
			SID sid = entry.getValue() ;
			sid.removeUserDeivce(aud) ;
		}
	}

	@Override
	public void onTicker(long ticker) {
		SID administrator = getLoginUserCachByName(SID.ADMINISTRATOR) ;
		if (administrator != null) {
			boolean timeout = System.currentTimeMillis() - administrator.getLastLoginTime() > 5*60*1000 ;
			if(timeout){
				removeUser(SID.ADMINISTRATOR) ;
			}
		}
	}
	
}

