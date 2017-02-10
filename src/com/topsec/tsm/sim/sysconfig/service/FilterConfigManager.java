package com.topsec.tsm.sim.sysconfig.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.node.ComponentException;
import com.topsec.tsm.node.component.handler.FilterConf;
import com.topsec.tsm.node.component.handler.FilterHandlerConfig;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentConfigException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.rule.SimRule;

/**
 * 节点过滤器管理
 * @author hp
 *
 */
public class FilterConfigManager {
	
	public static int ENABLE_FILTER = 1 ;//启用过滤器操作
	public static int DISABLE_FILTER = 0 ;//禁用过滤器操作
	
	private static Logger logger = LoggerFactory.getLogger(FilterConfigManager.class) ;
	private Node node ;
	private Component component ;
	private FilterHandlerConfig config ;
	NodeMgrFacade nodeMgrFacade ;
	/**
	 * 节点id
	 * @param nodeId
	 * @throws ComponentConfigException 
	 */
	public FilterConfigManager(String nodeId) throws ComponentConfigException{
		Node n = new Node() ;
		n.setNodeId(nodeId) ;
		this.node = n ;
		init() ;
	}
	
	public FilterConfigManager(Node node) throws ComponentConfigException{
		this.node = node ;
		init() ;
	}
	
	protected void init()throws ComponentConfigException{
		nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		try {
			Node configOwner = nodeMgrFacade.getNodeByNodeId(node.getNodeId(), false, false, true, true);
			if (configOwner == null) {
				//throw new InvalidNodeException(node.getNodeId()) ;
			}
			component = nodeMgrFacade.getBindableComponentByType(configOwner, NodeDefinition.HANDLER_FILTER, true);
			if(component==null){
				throw new ComponentException(MessageFormat.format("Can''t find ''{0}'' component in node {1} ",NodeDefinition.HANDLER_FILTER,node.getNodeId())) ;
			}
			config = nodeMgrFacade.getSegmentConfigByClass(component, FilterHandlerConfig.class);
			if(config==null){
				throw new ComponentException(MessageFormat.format("Can''t find ''{0}'' component in node {1} ",NodeDefinition.HANDLER_FILTER,node.getNodeId())) ;
			}
			if(config.getFilterConfMap()==null){
				config.setFilterConfMap(new HashMap<String, FilterConf>());
			}
		} catch (Exception e) {
			throw new ComponentConfigException(e);
		}
	}
	/**
	 * 将过滤器应用到所有指定的数据源
	 * @param rule 过滤器
	 * @param dataSources 要过滤的数据源
	 */
	public void enableFilter(SimRule rule,SimDatasource... dataSources){
		modifyFilter(ENABLE_FILTER, rule, dataSources) ;
	}
	/**
	 * 禁用数据源的所有过滤器
	 */
	public void disableFilter(SimDatasource... dataSources){
		modifyFilter(DISABLE_FILTER, null, dataSources) ;
	}
	/**
	 * 禁用数据源的指定过滤器(现在数据源与过滤器是一对一关联，所以此调用相当于禁用数据源的所有过滤器)
	 * @see #disableFilter(SimDatasource... ) 
	 * @param rule 过滤器
	 * @param dataSources 数据源
	 */
	public void disableFilter(SimRule rule,SimDatasource... dataSources){
		modifyFilter(DISABLE_FILTER, rule, dataSources) ;
	}
	/**
	 * 修改并重新下发过滤器信息
	 * @param operation 修改操作
	 * @param rule 过滤器
	 * @param dataSources 数据源
	 */
	private boolean modifyFilter(int operation,SimRule rule,SimDatasource... dataSources){
		if(ObjectUtils.isEmpty(dataSources)){
			return true ;
		}
		boolean success = true;
		Map<String, FilterConf> filterConfMap =config.getFilterConfMap();
		if (operation==DISABLE_FILTER) {//禁用过滤器
			for(SimDatasource dataSource:dataSources){
				filterConfMap.remove(String.valueOf(dataSource.getResourceId()));
			}
		}else if (operation==ENABLE_FILTER) {//启用过滤器
			for(SimDatasource dataSource:dataSources){
				FilterConf filterConfig = filterConfMap.get(String.valueOf(dataSource.getResourceId()));
				if(filterConfig==null){
					filterConfig = new FilterConf() ;
				}
				setConfig(filterConfig, rule,dataSource) ;
				filterConfMap.put(String.valueOf(dataSource.getResourceId()), filterConfig) ;
			}
		}
		return success;
	}
	
	/**
	 * 设置过滤器条件
	 * @param config
	 * @param rule
	 */
	private void setConfig(FilterConf config,SimRule rule,SimDatasource dataSource){
		config.setId(rule.getId().toString());
		config.setState(rule.getAvailable().toString());
		config.setCondition(insertDataSourceCondition(rule.getCondition(),dataSource));
		config.setDiscard(rule.getDiscard());
	}
	/**
	 * 下发过滤器配置
	 * @return
	 */
	public boolean sendFilter()throws ComponentConfigException{
		logger.debug("Send FilterHandler Condition:{}",config.getFilterConfMap()) ;
		try {
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, config);
		} catch (Exception e) {
			throw new ComponentConfigException(e) ;
		}
		return true ;
	}

	/**
	 * 加入数据源限制条件
	 * @param rule
	 * @param dataSource
	 * @return
	 */
	private static String insertDataSourceCondition(String condtion,SimDatasource dataSource){
		String selectorWords = condtion.substring(0,8) ;//SELECTOR
		int leftBracketIndex = condtion.indexOf('(') ;//'('位置
		String condition = condtion.substring(leftBracketIndex) ; //(SRC_ADDRESS = '10.0.0.2')
		StringBuffer dataSourceCondtion = new StringBuffer() ;//DVC_TYPE=Flow/TOPSEC/TA-W NetflowV5 AND DVC_ADDRESS=192.168.75.40
		dataSourceCondtion.append(" DVC_TYPE = '").append(dataSource.getSecurityObjectType()).append("' AND ")
						  .append("DVC_ADDRESS = '").append(dataSource.getDeviceIp()).append("'") ;
		StringBuffer result = new StringBuffer() ;//SELECTOR(DVC_TYPE=Flow/TOPSEC/TA-W NetflowV5 AND DVC_ADDRESS=192.168.75.40 AND (SRC_ADDRESS = '10.0.0.2'))
		result.append(selectorWords).append("(").append(dataSourceCondtion) ;
		if(condition.contains("=")){
			result.append(" AND ").append(condition) ;
		}else{
			result.append(" AND ").append(condition.substring(1,condition.length()-1)) ;
		}
		result.append(")") ;
		return result.toString() ;
	}
	@Override
	public String toString() {
		return this.node.getNodeId();
	}
}
