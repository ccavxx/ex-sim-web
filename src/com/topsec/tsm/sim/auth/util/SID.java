package com.topsec.tsm.sim.auth.util;

import java.security.PublicKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthRole;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.util.encrypt.RSAUtil;

public class SID {
	
	public static final String ADMINISTRATOR = "administrator" ;
	
	private String loginIP="";
	private String userName = "";
	private String userType = "";
	private int accountID = 0;
	private long lastLoginTime =-1;
	private Set<AuthUserDevice> userDevice;
	private Set<AuthRole> userRoles ;
	private Integer defaultTopoId ;
	private boolean visible = true ;
	
	private static final ThreadLocal<SID> threadUsers = new ThreadLocal<SID>() ;
	private String groupId;//日志源列集ID
	private PublicKey clientPublicKey ;
	private Authority authority ;
	
	public SID(){}
	public SID(String ip,AuthAccount account){
		loginIP = ip;
		userName =account.getName();
		userType =account.getUsertype();
		accountID =account.getID();
		lastLoginTime = Calendar.getInstance().getTimeInMillis();
		userDevice = account.getUserDevice();
		userRoles = account.getRoles() ;
		defaultTopoId = account.getDefaultTopoId() ;
		groupId = account.getGroupId();
	}
	public SID(String sid) {
		String[] info = sid.split("/");
		if (info.length != 4) {
			return;
		}
		loginIP = info[0];
		userName = info[1];
		userType =info[2];
		accountID = Integer.parseInt(info[3]);
	}
    public SID(String accountName, String loginIP) {
    	this.userName = accountName ;
    	this.loginIP = loginIP ;
	}
	public String toString(){
    	StringBuilder output = new StringBuilder(128);
        output.append(loginIP+"/"+userName+"/"+userType+"/"+accountID);
        return output.toString();
    }
	public Set<AuthUserDevice> getUserDevice() {
		return userDevice;
	}
	public void setUserDevice(Set<AuthUserDevice> userDevice) {
		this.userDevice = userDevice;
	}
    public long getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public String getLoginIP() {
		return loginIP;
	}

	public void setLoginIP(String loginIP) {
		this.loginIP = loginIP;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public int getAccountID() {
		return accountID;
	}

	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}
	public Set<AuthRole> getRoles() {
		return userRoles;
	}
	public void setRoles(Set<AuthRole> userRoles) {
		this.userRoles = userRoles;
	}

	/**
	 * 是否是admin账号
	 * @return
	 */
	public boolean isAdmin(){
		return "admin".equalsIgnoreCase(userName) ;
	}
	/**
	 * 是否是operator账号
	 * @return
	 */
	public boolean isOperator(){
		return "operator".equalsIgnoreCase(userName) ;
	}
	/**
	 * 是否是auditor账号
	 * @return
	 */
	public boolean isAuditor(){
		return "auditor".equalsIgnoreCase(userName) ;
	}
	public Integer getDefaultTopoId() {
		return defaultTopoId;
	}
	public void setDefaultTopoId(Integer defaultTopoId) {
		this.defaultTopoId = defaultTopoId;
	}
	
	public boolean isLogReportRole(){
		boolean role=isOperator();
		if (!isOperator() && hasOperatorRole()) {
			if (!GlobalUtil.isNullOrEmpty(getUserDevice())) {
				role=true;
			}
		}
		return role;
	}
	/**
	 * 是否是系统内置的账号auditor或operator或admin
	 * @return
	 */
	public boolean isDefaultUser(){
		return isAuditor() || isOperator() || isAdmin()  ;
	}
	/**
	 * 是否是超级用户
	 * @return
	 */
	public boolean isSuperMan(){
		return ADMINISTRATOR.equalsIgnoreCase(userName) ;
	}
	
	public boolean hasOperatorRole(){
		return hasRole(53) ;
	}
	
	public boolean hasAuditorRole(){
		return hasRole(52) ;
	}
	
	public boolean hasAdminRole(){
		return hasRole(51) ;
	}
	
	public void removeUserDeivce(AuthUserDevice authUserDeivce){
		if (userDevice != null) {
			userDevice.remove(authUserDeivce) ;
		}
	}
	
	private boolean hasRole(Integer roleId){
		if(ObjectUtils.isEmpty(userRoles)){
			return false ;
		}
		for(AuthRole aur:userRoles){
			if(aur.getId().equals(roleId)) return true ;
		}
		return false ;
	}
	
	/**
	 * 将用户与当前线程绑定
	 * @param sid
	 */
	public static void setCurrentUser(SID sid){
		threadUsers.set(sid) ;
	}
	/**
	 * 获取当前线程绑定的用户信息
	 * @return
	 */
	public static SID currentUser(){
		return threadUsers.get() ;
	}
	/**
	 * 解除线程与用户的绑定
	 */
	public static void removeCurrentUser(){
		threadUsers.remove() ;
	}
	
	public Map<String,String[]> getGroupId() {
		 if(groupId != null && !"".equals(groupId)){
			 Map<String,String[]> group = new HashMap<String,String[]>();
			 String[] deviceType = groupId.split(";");
			 for(String type :deviceType){
				 String key = type.substring(0,type.indexOf(":"));
				 String[] value = type.substring(type.indexOf(":")+1, type.length()).split("#");
				 group.put(key,value);
			 }
			 return group;
		 }
		return null;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public String getRole(){
		if(hasAuditorRole()){
			return "auditor";
		}else if(hasOperatorRole()){
			return "operator";
		}else if(hasAdminRole()){
			return "admin";
		}
		return "unknow" ;
	}
	
	public Authority getAuthority() {
		return authority;
	}
	public void setAuthority(Authority authority) {
		this.authority = authority;
		authority.setSid(this) ;
	}
	
	public Set<String> getUserIp(){
		Set<String> ips = new HashSet<String>(userDevice != null ? userDevice.size() : 0) ;
		if(userDevice != null){
			for(AuthUserDevice aud:userDevice){
				ips.add(aud.getIp()) ;
			}
		}
		return ips ;
	}
	
	/**
	 * 判断对某一日志源类型是否具体访问权限
	 * @param type 日志源类型
	 * @return
	 */
	public boolean accessible(String host,String type){
		return authority.hasAuthority(host, type) ;
	}
	
	public PublicKey getClientPublicKey() {
		return clientPublicKey;
	}
	
	public void setClientPublicKey(PublicKey clientPublicKey) {
		this.clientPublicKey = clientPublicKey;
	}

	public void setClientPublicKey(String key){
		String[] keyArray = StringUtil.split(key,"\\|") ;
		if(keyArray.length < 2){
			return ;
		}
		this.clientPublicKey = RSAUtil.getRSAPublidKey(keyArray[0], keyArray[1]) ;
	}
	
}

