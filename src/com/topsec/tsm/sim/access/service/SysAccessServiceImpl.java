package com.topsec.tsm.sim.access.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.topsec.tsm.auth.manage.AuthUserRole;
import com.topsec.tsm.sim.access.SysMenuRoleCorrelation;
import com.topsec.tsm.sim.access.SysTreeMenu;
import com.topsec.tsm.sim.access.dao.SysMenuRoleCorrelationDao;
import com.topsec.tsm.sim.access.dao.SysRoleDao;
import com.topsec.tsm.sim.access.dao.SysTreeMenuDao;
import com.topsec.tsm.sim.access.util.GlobalUtil;

/**
 * @ClassName: SysRoleServiceImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年6月27日下午4:24:49
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class SysAccessServiceImpl implements SysAccessService{

	private SysRoleDao sysRoleDao;
	private SysMenuRoleCorrelationDao sysMenuRoleCorrelationDao;
	private SysTreeMenuDao sysTreeMenuDao;

	public void setSysMenuRoleCorrelationDao(
			SysMenuRoleCorrelationDao sysMenuRoleCorrelationDao) {
		this.sysMenuRoleCorrelationDao = sysMenuRoleCorrelationDao;
	}
	public void setSysTreeMenuDao(SysTreeMenuDao sysTreeMenuDao) {
		this.sysTreeMenuDao = sysTreeMenuDao;
	}
	public void setSysRoleDao(SysRoleDao sysRoleDao) {
		this.sysRoleDao = sysRoleDao;
	}
	@Override
	public AuthUserRole showByUniqueAccountId(Integer accountId) {
		return sysRoleDao.findByUniqueAccountId(accountId);
	}

	@Override
	public List<AuthUserRole> showByRoleId(Integer roleId) {
		return sysRoleDao.findByRoleId(roleId);
	}

	@Override
	public List<AuthUserRole> showByAccountId(Integer accountId) {
		return sysRoleDao.findByAccountId(accountId);
	}
	@Override
	public List<SysMenuRoleCorrelation> showMenuByRoleId(Integer roleId) {
		return sysMenuRoleCorrelationDao.findByRoleId(roleId);
	}
	
	@Override
	public SysTreeMenu showTreeMenuByMenuId(String menuId) {
		//需要更加复杂的实现，要把子节点也查出来
		return sysTreeMenuDao.findById(menuId);
	}
	
	@Override
	public List<SysTreeMenu> showTreeMenuByAccountId(
			Integer accountId) {//此方法还需要具体分类，比较笼统，均为一级的时候使用
		List<SysTreeMenu> sysTreeMenus=new ArrayList<SysTreeMenu>();
		List<AuthUserRole> authUserRoles=sysRoleDao.findByAccountId(accountId);
		if (GlobalUtil.isNullOrEmpty(authUserRoles)) {
			return sysTreeMenus;
		}
		Set<SysMenuRoleCorrelation> sysMenuRoleCorrelations=new HashSet<SysMenuRoleCorrelation>();
		for (AuthUserRole authUserRole : authUserRoles) {
			List<SysMenuRoleCorrelation> sysMenuRoleCorrelation=sysMenuRoleCorrelationDao.findByRoleId(authUserRole.getRoleId());
			if (GlobalUtil.isNullOrEmpty(sysMenuRoleCorrelation)) {
				continue;
			}
			sysMenuRoleCorrelations.addAll(sysMenuRoleCorrelation);
		}
		if (GlobalUtil.isNullOrEmpty(sysMenuRoleCorrelations)) {
			return sysTreeMenus;
		}
		Set<SysTreeMenu> sysTreeMenuSet=new HashSet<SysTreeMenu>();
		for (SysMenuRoleCorrelation sCorrelation : sysMenuRoleCorrelations) {
			SysTreeMenu sysTreeMenu=sysTreeMenuDao.findById(sCorrelation.getMenuId());
			if (GlobalUtil.isNullOrEmpty(sysTreeMenu)) {
				continue;
			}
			sysTreeMenuSet.add(sysTreeMenu);
		}
		if (GlobalUtil.isNullOrEmpty(sysTreeMenuSet)) {
			return sysTreeMenus;
		}
		sysTreeMenus.addAll(sysTreeMenuSet);
		return sysTreeMenus;
	}
	/**
	 * 此方法返回处理过的根据用户id获取菜单，并包含子菜单
	 * @param accountId
	 * @return 处理过的用户所有有权限的菜单
	 */
	@Override
	public Set<SysTreeMenu>showTopTreeMenus(Integer accountId) {
		List<SysTreeMenu> sysTreeMenus=showTreeMenuByAccountId(accountId);
		if (GlobalUtil.isNullOrEmpty(sysTreeMenus)) {
			return null;
		}
		Set<SysTreeMenu>setSysTreeMenus=new TreeSet<SysTreeMenu>();
		for (SysTreeMenu sysTreeMenu : sysTreeMenus) {
			if (sysTreeMenu.getMenuLevel()==0) {
				
				for (SysTreeMenu sysTreeMenu1 : sysTreeMenus) {
					if (!sysTreeMenu.equals(sysTreeMenu1)) {
						if (sysTreeMenu1.getMenuParentId().equals(sysTreeMenu.getMenuId())) {
							Map<String, Object>mapAttrMap=jsonStringToMap(sysTreeMenu1.getJsonStyle());
							sysTreeMenu1.setMenuAttributes(mapAttrMap);
							sysTreeMenu.addChild(sysTreeMenu1);
						}
					}
				}
				Map<String, Object> map=jsonStringToMap(sysTreeMenu.getJsonStyle());
				sysTreeMenu.setMenuAttributes(map);
				setSysTreeMenus.add(sysTreeMenu);
			}
		}
		return setSysTreeMenus;
	}
	
	private static Map<String, Object> jsonStringToMap(String jsonstring){
		if (GlobalUtil.isNullOrEmpty(jsonstring)) {
			return null;
		}
		
		String string=jsonstring.trim();
		String jsonS=string.substring(1, string.length()-1);
		String[]keyValues=jsonS.split(",");
		if (GlobalUtil.isNullOrEmpty(keyValues)) {
			return null;
		}
		Map<String, Object> resultMap=new HashMap<String, Object>();
		for (String string2 : keyValues) {
			String[]putStrings=string2.split(":");
			if (GlobalUtil.isNullOrEmpty(putStrings)) {
				continue;
			}
			if (putStrings.length==2) {
				resultMap.put(putStrings[0], putStrings[1]);
			}
		}
		return resultMap;
	}
}
