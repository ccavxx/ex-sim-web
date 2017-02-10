package com.topsec.tsm.sim.auth.service;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.auth.manage.UserColumnConfig;


public interface UserService  {
	/**
	 * addUser 添加用户
	 * @author zhou_xiaohu
	 * @param authAccount
	 * @param sid
	 * @return
	 */
	public void addUser(AuthAccount authAccount);
	/**
	 * 删除帐号
	 * @param userID帐号id
	 * @param sid  在线用户的安全描述符
	 */
	public void delUserByID(int userID);
	/**
	 * 根据用户ID 获取用户信息
	 * @author zhou_xiaohu
	 * @param userID
	 * @param sid
	 * @return
	 * @throws RemoteException
	 */

	public AuthAccount getUserByID(int userID) ;
	/**
	 * 获取所有用户信息
	 * @author zhou_xiaohu
	 * @param sid
	 * @return
	 * @throws RemoteException
	 */
	public Collection getAllUsers() ;
	public PageBean<AuthAccount> getUsersPage(Integer pageIndex,Integer pageSize,Map<String, Object> searchCondition) ;
	/**
	 * modifyInfo : 修改用户信息
	 * @author zhou_xiaohu
	 * @param user
	 * @param sid
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public void modifyInfo(AuthAccount user);
	/**
	 * getUsersByRoleName 根据用户角色获取用户列表
	 * @param roleName
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public List getUsersByRoleName(String roleName);
	/**
	 * getUserByUserName 根据用户名获取用户
	 * @param userName
	 * @return
	 */
	public AuthAccount getUserByUserName(String userName);
	/**
	 * 更新用户连续登录失败次数
	 * @param aa
	 * @param count
	 */
	public void updateFailedCount(AuthAccount aa,Integer count) ;
	/**
	 * 更改用户登录后首页默认拓扑图
	 * @param accountId
	 * @param topoId
	 */
	public void changeDefaultTopo(Integer accountId,Integer topoId) ;
	/**
	 * 删除用户相关信息
	 * @param userName
	 */
	public void deleteUserRelateInfo(String userName);
	/**保存用户列配置信息*/
	public void saveColumnConfig(UserColumnConfig columnConfig);
	/**
	 * 获取列配置信息
	 */
	public UserColumnConfig getColumnConfig(ColumnConfigId id) ;
	/**
	 * 返回列配置信息<br>
	 * 
	 */
	public Map<String,JSONObject> getColumnConfigMap(ColumnConfigId id) ;
}
