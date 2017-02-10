package com.topsec.tsm.sim.access.service;

import java.util.List;
import java.util.Set;

import com.topsec.tsm.auth.manage.AuthUserRole;
import com.topsec.tsm.sim.access.SysMenuRoleCorrelation;
import com.topsec.tsm.sim.access.SysTreeMenu;

/**
 * @ClassName: SysRoleService
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年6月27日下午4:24:20
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface SysAccessService {
	public AuthUserRole showByUniqueAccountId(Integer accountId);
	public List<AuthUserRole>showByRoleId(Integer roleId);
	public List<AuthUserRole>showByAccountId(Integer accountId);
	
	public List<SysMenuRoleCorrelation>showMenuByRoleId(Integer roleId);
	
	public List<SysTreeMenu>showTreeMenuByAccountId(Integer accountId);
	public SysTreeMenu showTreeMenuByMenuId(String menuId);
	public Set<SysTreeMenu>showTopTreeMenus(Integer accountId);
}
