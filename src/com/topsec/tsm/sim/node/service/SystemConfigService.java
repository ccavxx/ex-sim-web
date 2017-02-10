/*
 * 版权声明:北京天融信科技有限公司，版权所有违者必究
 * Copyright：Copyright (c) 2011
 * Company：北京天融信科技有限公司
 * @author zhouxiaohu 
 * 2011-08-03
 * @version 1.0
 */
package com.topsec.tsm.sim.node.service;

import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.sim.common.exception.DataAccessException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;

/**
 * 用于添加SMP、Auditor时的系统配置的接口类
 * @author zhouxiaohu
 * @version 1.0
 */
public interface SystemConfigService {
	/**
	 * 第一次启动.SOC节点首先注册,注册后,会调用改接口
	 * 在数据库表里会插入服务器配置的相关记录
	 * @param nodeId
	 */
	public void configSMP(String nodeId)throws Exception;
	
	/**
	 * 当Auditor注册时, 会调用该接口,参数(String  nodeId),这时在数据库表里会插入这个Auditor配置的相关记录
	 * @param nodeId
	 * @param logFilePath
	 */
	public void configAuditor(Node node,NodeMgrFacade nodeMgrFacade)throws Exception;
	/**
	 * 当Service注册时, 这时在数据库表里会插入这个Service配置的相关记录
	 * @param nodeId
	 * @param logFilePath
	 */
	public void configService(Node node,NodeMgrFacade nodeMgrFacade)throws Exception;
	/**
	 * 当ReportService注册时,这时在数据库表里会插入这个ReportService配置的相关记录
	 * @param nodeId
	 * @param logFilePath
	 */
	public void configReportService(Node node,NodeMgrFacade nodeMgrFacade) throws Exception;
	/**
	 * 当Collector注册时, 会调用该接口,这时在数据库表里会插入这个Collector配置的相关记录
	 * @param node
	 * @param nodeMgrFacade
	 * @throws Exception
	 */
	public void configCollector(Node node,NodeMgrFacade nodeMgrFacade) throws Exception ;
	
}
