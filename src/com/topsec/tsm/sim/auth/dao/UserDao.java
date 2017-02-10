package com.topsec.tsm.sim.auth.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.ColumnConfigId;
import com.topsec.tsm.auth.manage.UserColumnConfig;
import com.topsec.tsm.sim.common.dao.BaseDao;


public interface UserDao extends BaseDao<AuthAccount,Integer>{
	/**
	 * 获取所有用户信息
	 * @author zhou_xiaohu
	 * @param sid
	 * @return
	 * @throws RemoteException
	 */
	public Collection getAllUsers() ;
	public PageBean<AuthAccount> getUsersPage(Integer pageIndex,Integer pageSize,Map<String, Object> searchCondition);
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
	public AuthAccount getByName(String userName);
	/**更新失败次数*/
	public void updateFailedCount(AuthAccount aa, Integer count);
	/**删除资产权限*/
	public void deleteAuthDeivce(String deviceId) ;
	/**更改用户默认拓扑*/
	public void changeDefaultTopo(Integer accountId, Integer topoId);
	/**删除与用户关联的信息*/
	public void deleteUserRelateInfo(String userName);
	/**保存用户自定义列信息*/
	public void saveColumnConfig(UserColumnConfig config) ;
	/***/
	public UserColumnConfig getColumnConfig(ColumnConfigId id) ;
}
