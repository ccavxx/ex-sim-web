package com.topsec.tsm.sim.access.service;

import java.util.List;

import com.topsec.tsm.sim.access.IsEditNodeType;
import com.topsec.tsm.sim.access.NodeTypeShow;
import com.topsec.tsm.sim.access.dao.IsEditNodeTypeDao;
import com.topsec.tsm.sim.access.dao.NodeTypeShowDao;

/**
 * @ClassName: NodeTypeServiceImpl
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:34:13
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class NodeTypeServiceImpl implements NodeTypeService{
	private IsEditNodeTypeDao isEditNodeTypeDao;
	private NodeTypeShowDao nodeTypeShowDao;
	
	public void setIsEditNodeTypeDao(IsEditNodeTypeDao isEditNodeTypeDao) {
		this.isEditNodeTypeDao = isEditNodeTypeDao;
	}

	public void setNodeTypeShowDao(NodeTypeShowDao nodeTypeShowDao) {
		this.nodeTypeShowDao = nodeTypeShowDao;
	}

	public NodeTypeServiceImpl() {
	}

	@Override
	public List<NodeTypeShow> findTypeShowsByUserName(String userName) {
		return nodeTypeShowDao.findByUserName(userName);
	}

	@Override
	public IsEditNodeType findIsEditByUserName(String userName) {
		return isEditNodeTypeDao.findByUserName(userName);
	}

	@Override
	public void saveNodeTypeShow(NodeTypeShow nodeTypeShow) {
		nodeTypeShowDao.save(nodeTypeShow);
	}

	@Override
	public void saveIsEditNodeType(IsEditNodeType isEditNodeType) {
		isEditNodeTypeDao.save(isEditNodeType);
	}

	@Override
	public void updateNodeTypeShow(NodeTypeShow nodeTypeShow) {
		nodeTypeShowDao.update(nodeTypeShow);
	}

	@Override
	public void updateEditNodeType(IsEditNodeType isEditNodeType) {
		isEditNodeTypeDao.update(isEditNodeType);
	}

	@Override
	public void deleteNodeTypeShow(NodeTypeShow nodeTypeShow) {
		nodeTypeShowDao.delete(nodeTypeShow.getId());
	}

	@Override
	public void deleteIsEditNodeType(IsEditNodeType isEditNodeType) {
		isEditNodeTypeDao.delete(isEditNodeType.getId());
	}

	@Override
	public void updateNodeTypeShowProperty(NodeTypeShow nodeTypeShow,String propertyName,Object value) {
		nodeTypeShowDao.updateProperty(nodeTypeShow.getId(), propertyName, value);
	}

	@Override
	public NodeTypeShow findByUserNameAndNodeType(String userName,
			String nodeType) {
		return nodeTypeShowDao.findByUserNameAndNodeType(userName, nodeType);
	}

	@Override
	public NodeTypeShow findNodeTypeShowById(Integer id) {
		return nodeTypeShowDao.findById(id);
	}

	@Override
	public IsEditNodeType findIsEditNodeTypeById(Integer id) {
		return isEditNodeTypeDao.findById(id);
	}

	@Override
	public List<NodeTypeShow> findByUserNameAndIsShow(String userName,
			Boolean isShow) {
		return nodeTypeShowDao.findByUserNameAndIsShow(userName, isShow);
	}

}
