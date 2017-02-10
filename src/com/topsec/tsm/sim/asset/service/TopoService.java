package com.topsec.tsm.sim.asset.service;

import java.util.List;

import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;

/**
 * Flex版拓扑service接口
 * @author hp
 *
 */
public interface TopoService {
	/**
	 * 保存或更新逻辑拓扑
	 * @param top
	 */
	public void saveOrUpdate(AssTopo topo)throws ResourceNameExistException  ;
	/**
	 * 返回所有逻辑拓扑图
	 */
	public List<AssTopo> getAll() ;
	/**
	 * 获取用户的拓扑列表
	 * @param userName
	 * @return
	 */
	public List<AssTopo> getUserTopoList(String userName) ;
	
	/**
	 * 根据id获取逻辑拓扑图
	 * @param Id
	 * @return
	 */
	public AssTopo get(Integer id) ;
	/**
	 * 删除拓扑对象
	 * @param id
	 */
	public void delete(Integer id);
	/**
	 * 更新拓扑图名称
	 * @param id
	 * @param name
	 */
	public void updateTopoName(Integer id,String name)throws ResourceNameExistException  ;
	/**
	 * 获取当前系统节点、资产拓扑图
	 * @return
	 */
	public AssTopo getSystemTopo(NodeMgrFacade nodeMgr) ;
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public AssTopo getByName(String name);
}
