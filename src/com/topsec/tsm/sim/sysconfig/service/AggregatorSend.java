package com.topsec.tsm.sim.sysconfig.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.framework.exceptions.XMLFormatableException;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.node.component.handler.AggregatorConfig;
import com.topsec.tsm.node.handler.aggregation.AggregateRule;
import com.topsec.tsm.node.handler.aggregation.AggregateRuleItem;
import com.topsec.tsm.node.handler.aggregation.AggregationException;
import com.topsec.tsm.node.handler.aggregation.SceneConfig;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentConfigException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;

public class AggregatorSend {
	public static final String DELETE = "delete";// 删除操作
	public static final String MODIFY = "modify";// 编辑操作
	public static final String SAVE = "save";// 保存操作
	private static Pattern pattern = Pattern.compile("base:([^,]*),.*?element:\\{([^\\}]*)\\}") ;
	private static Logger logger = LoggerFactory.getLogger(AggregatorSend.class) ;
	private NodeMgrFacade nodeMgrFacade ;
	private Component component ;
	private AggregatorConfig aggregatorConfig ;
	
	public AggregatorSend(String nodeId){
		nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		Node node = nodeMgrFacade.getNodeByNodeId(nodeId,false, false, true, true);
		if (node != null) {
			component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_AGGREGATOR, true);
			if (component != null) {
				aggregatorConfig = nodeMgrFacade.getSegmentConfigByClass(component, AggregatorConfig.class);
			}
		}
	}
	/**
	 * 启用指定日志源上的归并规则
	 * @param scene
	 * @param dataSourceId
	 */
	public void enableAggregator(AggregatorScene scene,SimDatasource dataSource){
		aggregatorOP(SAVE, scene, dataSource) ;
	}
	/**
	 * 禁用指定日志源上的归并规则
	 * @param scene
	 * @param dataSourceId
	 */
	public void disableAggregator(SimDatasource dataSource){
		aggregatorOP(DELETE, null, dataSource) ;
	}
	private void aggregatorOP(String operation,AggregatorScene aggregator,SimDatasource dataSource){
		try {
			String dataSourceId = String.valueOf(dataSource.getResourceId()) ;
			Map<String, SceneConfig> sceneConfigs = aggregatorConfig.getSceneConfigs();
			TypeDef typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef-filter.xml");
			SceneConfig sceneConfig = null;
			if (DELETE.equals(operation)) {
				sceneConfigs.remove(dataSourceId);
			}else if (SAVE.equals(operation) || MODIFY.equals(operation)) {
				if((sceneConfig = sceneConfigs.get(dataSourceId)) == null){
					sceneConfig = new SceneConfig(typedef) ;
				}
				fillConfig(sceneConfig, aggregator,dataSource) ;
				sceneConfigs.put(dataSourceId, sceneConfig) ;
			}
			if(logger.isDebugEnabled()){
				try {
					logger.debug("Send Aggregator Condtion:{}",aggregatorConfig.exportObjectToString()) ;
				} catch (XMLFormatableException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			throw new ComponentConfigException("归并策略下发出错",e) ;
		}
	}
	
	/**
	 * 归并策略下发
	 * 
	 * @param nodeMgrFacade
	 * @param aggregator 归并场景
	 * @param operation 类型 save、modify、delete
	 */
	public boolean sendAggregator() {
		try {
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, aggregatorConfig);
		}catch (Exception e) {
			throw new ComponentConfigException("归并策略下发出错",e) ;
		}
		return true;
	}

	private AggregateRule getAggregateRule(String ruleCondition)throws AggregationException{
		//[{base:frist,element:{ID:OBJECT_PARAM,DEST_ADDRESS:OBJECT_PARAM,ID:REQUEST_STATUS,DEST_ADDRESS:REQUEST_STATUS}}]
		AggregateRule rule = new AggregateRule();
		if(StringUtil.isNotBlank(ruleCondition)){
	    	try {
	    		ruleCondition = ruleCondition.replace("\"", "");
	    		Matcher matcher = pattern.matcher(ruleCondition) ;
	    		if(matcher.find()){
	    			String eventNum = matcher.group(1) ;//first
	    			rule.setBaseEventNum(eventNum);
	    			String elementString = matcher.group(2) ;//ID:OBJECT_PARAM,DEST_ADDRESS:OBJECT_PARAM,ID:REQUEST_STATUS,DEST_ADDRESS:REQUEST_STATUS
	    			String[] elements = StringUtil.split(elementString) ;
	    			for(String element:elements){
	    				String[] attributes = StringUtil.split(element,":") ;
	    				if(attributes.length==2){
	    					AggregateRuleItem item = AggregateRuleItem.buildAggregateRuleItem(eventNum, attributes[0],attributes[1]) ;
	    					rule.addAggregateRuleItem(item) ;
	    				}else{
	    					throw new AggregationException("Invalid element "+element) ;
	    				}
	    			}
	    		}else{
	    			throw new AggregationException("Invalid rule condition:"+ruleCondition) ;
	    		}
			} catch (AggregationException e) {
				throw e ;
			}
	    }
		return rule;
	}
	
	private void fillConfig(SceneConfig sceneConfig,AggregatorScene aggregator,SimDatasource dataSource)throws AggregationException{
		try {
			sceneConfig.setCondition(insertDataSourceCondition(aggregator.getCondition(),dataSource));
			sceneConfig.setMaxCount(aggregator.getMaxCount());
			sceneConfig.setName(aggregator.getName());
			sceneConfig.setTimeOut(aggregator.getTimeOut());
			sceneConfig.setTimes(aggregator.getTimes());
			sceneConfig.setRule(getAggregateRule(aggregator.getRule()));
			sceneConfig.setIdentities(StringUtil.split(aggregator.getColumnSet()));
		} catch (AggregationException e) {
			throw e ;
		}
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
		}else if(!condition.equalsIgnoreCase("(TRUE)")){
			result.append(" AND ").append(condition.substring(1,condition.length()-1)) ;
		}
		result.append(")") ;
		if(logger.isDebugEnabled()){
			logger.debug(result.toString()) ;
		}
		return result.toString() ;
	}
	public static void main(String[] args) {
		String str = "[{base:1,element:{SRC_PORT:DEST_ADDRESS}}]" ;
		Matcher matcher = pattern.matcher(str) ;
		System.out.println(matcher.find());
		System.out.println(matcher.group(1));
		System.out.println(matcher.group(2));

	}
}
