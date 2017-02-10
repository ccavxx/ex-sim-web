package com.topsec.tsm.sim.access.dao;

import java.util.List;

import com.topsec.tsm.sim.access.NodeTypeShow;
import com.topsec.tsm.sim.common.dao.BaseDao;

/**
 * @ClassName: NodeTypeShowDao
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年12月1日下午4:24:42
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public interface NodeTypeShowDao extends BaseDao<NodeTypeShow, Integer> {
	public List<NodeTypeShow> findByUserName(String userName);
	public NodeTypeShow findByUserNameAndNodeType(String userName,String nodeType);
	public List<NodeTypeShow> findByUserNameAndIsShow(String userName,Boolean isShow);
}
