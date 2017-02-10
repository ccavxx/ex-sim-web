package com.topsec.tsm.sim.auth.service;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.auth.manage.UserColumnConfig;
import com.topsec.tsm.sim.auth.dao.UserDao;

public class UserServiceImpl implements UserService {
	private UserDao userDao;
	public void setUserDao(UserDao userDao){
		this.userDao = userDao;
	}
	/**
	 * addUser 添加用户
	 * @author zhou_xiaohu
	 * @param authAccount
	 * @param 
	 * @return
	 */
	public void addUser(AuthAccount authAccount){
		 userDao.save(authAccount);
	}
	/**
	 * 删除帐号
	 * @param userID帐号id
	 * @param   在线用户的安全描述符
	 */
	public void delUserByID(int userID ){
		userDao.delete(userID);
	}
	/**
	 * 根据用户ID 获取用户信息
	 * @author zhou_xiaohu
	 * @param userID
	 * @param 
	 * @return
	 * @throws RemoteException
	 */

	public AuthAccount getUserByID(int userID ) {
		return userDao.findById(userID);
	}

	/**
	 * modifyInfo : 修改用户信息
	 * @author zhou_xiaohu
	 * @param user
	 * @param 
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public void modifyInfo(AuthAccount user ) {
		   userDao.update(user);
	}

	/**
	 * getUsersByRoleName 根据用户角色获取用户列表
	 * @param roleName
	 * @return
	 * @throws java.rmi.RemoteException
	 */

	public List getUsersByRoleName(String roleName) {
		return userDao.getUsersByRoleName(roleName);
	}
	/**
	 * 
	 * 获取所有用户
	 */
	public Collection getAllUsers() {
		return userDao.getAllUsers();
	};
	public PageBean<AuthAccount> getUsersPage(Integer pageIndex,Integer pageSize,Map<String, Object> searchCondition) {
		return userDao.getUsersPage(pageIndex, pageSize, searchCondition);
	}
	/**
	 * getUserByUserName 根据用户名获取用户
	 * @param userName
	 * @return
	 */
	public AuthAccount getUserByUserName(String userName){
		return userDao.getByName(userName);
	}
	@Override
	public void updateFailedCount(AuthAccount aa, Integer count) {
		userDao.updateFailedCount(aa,count) ;
	}
	@Override
	public void changeDefaultTopo(Integer accountId, Integer topoId) {
		userDao.changeDefaultTopo(accountId,topoId) ;
	}
      /**
       * 删除用户相关信息
       * @param userName
       */
	public void deleteUserRelateInfo(String userName){
		userDao.deleteUserRelateInfo(userName);
	}
	@Override
	public void saveColumnConfig(UserColumnConfig columnConfig) {
		userDao.saveColumnConfig(columnConfig) ;
	}
	@Override
	public UserColumnConfig getColumnConfig(ColumnConfigId id) {
		return userDao.getColumnConfig(id);
	}
	@Override
	public Map<String, JSONObject> getColumnConfigMap(ColumnConfigId id) {
		UserColumnConfig columnConfig = userDao.getColumnConfig(id) ;
		Map<String,JSONObject> columnConfigMap = new HashMap<String,JSONObject>() ;
		if(columnConfig != null && StringUtil.isNotBlank(columnConfig.getConfig())){
			JSONArray columnJSONArray = JSON.parseArray(columnConfig.getConfig()) ;
			for(Object obj:columnJSONArray){
				columnConfigMap.put(((JSONObject)obj).getString("column"),(JSONObject)obj) ;
			}
		}
		return columnConfigMap ;
	}
}
