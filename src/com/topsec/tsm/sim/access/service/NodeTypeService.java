package com.topsec.tsm.sim.access.service;

import java.util.List;

import com.topsec.tsm.sim.access.IsEditNodeType;
import com.topsec.tsm.sim.access.NodeTypeShow;

/**
 * @ClassName: NodeTypeService
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:29:45
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface NodeTypeService {
	public List<NodeTypeShow> findTypeShowsByUserName(String userName);
	public IsEditNodeType findIsEditByUserName(String userName);
	public void saveNodeTypeShow(NodeTypeShow nodeTypeShow);
	public void saveIsEditNodeType(IsEditNodeType isEditNodeType);
	public void updateNodeTypeShow(NodeTypeShow nodeTypeShow);
	public void updateEditNodeType(IsEditNodeType isEditNodeType);
	public void deleteNodeTypeShow(NodeTypeShow nodeTypeShow);
	public void deleteIsEditNodeType(IsEditNodeType isEditNodeType);
	public void updateNodeTypeShowProperty(NodeTypeShow nodeTypeShow,String propertyName,Object value);
	public NodeTypeShow findByUserNameAndNodeType(String userName,String nodeType);
	public NodeTypeShow findNodeTypeShowById(Integer id);
	public IsEditNodeType findIsEditNodeTypeById(Integer id);
	public List<NodeTypeShow> findByUserNameAndIsShow(String userName,Boolean isShow);
}
