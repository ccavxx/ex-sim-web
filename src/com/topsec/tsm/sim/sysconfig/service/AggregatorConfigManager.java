package com.topsec.tsm.sim.sysconfig.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.node.ComponentException;
import com.topsec.tsm.node.NodeException;
import com.topsec.tsm.node.component.handler.AggregatorConfig;
import com.topsec.tsm.node.handler.aggregation.AggregateRule;
import com.topsec.tsm.node.handler.aggregation.AggregateRuleItem;
import com.topsec.tsm.node.handler.aggregation.AggregationException;
import com.topsec.tsm.node.handler.aggregation.SceneConfig;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.InvalidNodeException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;

public class AggregatorConfigManager {
	
	public static int ENABLE_AGGREGATOR = 1 ;//启用过滤器操作
	public static int DISABLE_AGGREGATOR = 0 ;//禁用过滤器操作

	private Node node ;
	private Component component ;
	private AggregatorConfig config ;
	NodeMgrFacade nodeMgrFacade ;
	/**
	 * 节点id
	 * @param nodeId
	 */
	public AggregatorConfigManager(String nodeId){
		this.node = new Node(nodeId) ;
		init() ;
	}
	
	public AggregatorConfigManager(Node node){
		this.node = node ;
		init() ;
	}
	
	protected void init(){
		nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		try {
			Node configOwner = nodeMgrFacade.getNodeByNodeId(node.getNodeId(), false, false, true, true);
			if (configOwner == null) {
				throw new InvalidNodeException(node.getNodeId()) ;
			}
			component = nodeMgrFacade.getBindableComponentByType(configOwner, NodeDefinition.HANDLER_FILTER, true);
			if(component==null){
				throw new ComponentException(MessageFormat.format("Can''t find ''{0}'' component in node {1} ",NodeDefinition.HANDLER_FILTER,node.getNodeId())) ;
			}
			config = nodeMgrFacade.getSegmentConfigByClass(component, AggregatorConfig.class);
			if(config==null){
				throw new ComponentException(MessageFormat.format("Can''t find ''{0}'' component in node {1} ",NodeDefinition.HANDLER_FILTER,node.getNodeId())) ;
			}
			if(config.getSceneConfigs()==null){
				config.setSceneConfig(new HashMap<String, SceneConfig>());
			}
		} catch (Exception e) {
			//throw new ComponentConfigException(e);
		}
	}
	private static Pattern pattern = Pattern.compile("base:([^,]*),.*?element:\\{([^\\}]*)\\}") ;
	private static Logger logger = LoggerFactory.getLogger(AggregatorConfigManager.class) ;

	private AggregatorConfigManager(){
	}
	
	/**
	 * 归并下发
	 * 
	 * @param nodeMgrFacade
	 * @param aggregator 归并场景
	 * @param operation 类型 save、modify、delete
	 */
	public boolean sendAggregatormsg(AggregatorScene aggregator,String dataSourceId, String operation) {
		Node node;
		boolean flag = true;
		try {
			node = nodeMgrFacade.getNodeByNodeId(aggregator.getNode().getNodeId(),false, false, true, true);
			if(node!=null){
				Component component = nodeMgrFacade.getBindableComponentByType(node, NodeDefinition.HANDLER_AGGREGATOR, true);
				if(component!=null){
					AggregatorConfig aggregatorConfig = nodeMgrFacade.getSegmentConfigByClass(component, AggregatorConfig.class);
					Map<String, SceneConfig> sceneConfigs = aggregatorConfig.getSceneConfigs();
					TypeDef typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef-filter.xml");
					if(sceneConfigs==null){
						sceneConfigs = new HashMap<String, SceneConfig>();
					}
					SceneConfig sceneConfig = null;
					if ("delete".equals(operation)) {
						sceneConfig = sceneConfigs.get(dataSourceId);
						if(sceneConfig!=null){
							sceneConfigs.remove(dataSourceId);
						}
					}else if ("save".equals(operation)||"modify".equals(operation)) {
						sceneConfig = sceneConfigs.get(dataSourceId);
						if(sceneConfig==null){
							sceneConfig = new SceneConfig(typedef) ;
						}
						fillConfig(sceneConfig, aggregator) ;
						sceneConfigs.put(dataSourceId, sceneConfig) ;
					}
					logger.debug("Send Aggregator Condtion:{}",aggregatorConfig.exportObjectToString()) ;
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, aggregatorConfig);
				}
			}
		} catch (Exception e) {
			flag =false;
			e.printStackTrace();
		}
		return flag;
		
	}

	private boolean modifyAggregator(int operation,AggregatorScene rule,SimDatasource... dataSources)throws AggregationException{
		if(ObjectUtils.isEmpty(dataSources)){
			return true ;
		}
		boolean success = true;
		try {
			Map<String, SceneConfig> aggregatorConfMap =config.getSceneConfigs() ;
			if (operation==DISABLE_AGGREGATOR) {//禁用归并场景
				for(SimDatasource dataSource:dataSources){
					aggregatorConfMap.remove(String.valueOf(dataSource.getResourceId()));
				}
			}else if (operation==ENABLE_AGGREGATOR) {//启用归并场景
				for(SimDatasource dataSource:dataSources){
					SceneConfig filterConfig = aggregatorConfMap.get(String.valueOf(dataSource.getResourceId()));
					if(filterConfig==null){
						TypeDef typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef-filter.xml");
						filterConfig = new SceneConfig(typedef) ;
					}
					fillConfig(filterConfig, rule) ;
					aggregatorConfMap.put(String.valueOf(dataSource.getResourceId()), filterConfig) ;
				}
			}
		} catch (AggregationException e) {
			throw e ;
		}
		return success;
	}

	public boolean sendAggregator()throws AggregationException{
		try {
			nodeMgrFacade.updateComponentSegmentAndDispatch(component, config);
		} catch (NodeException e) {
			logger.error("AggregatorConfigManager.sendAggregator exception!",e) ;
			throw new AggregationException("归并场景下发失败!",e) ;
		}
		return true ;
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
	
	private void fillConfig(SceneConfig sceneConfig,AggregatorScene aggregator)throws AggregationException{
		try {
			sceneConfig.setCondition(aggregator.getCondition());
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
	public static void main(String[] args) {
		String str = "[{base:1,element:{SRC_PORT:DEST_ADDRESS}}]" ;
		Matcher matcher = pattern.matcher(str) ;
		System.out.println(matcher.find());
		System.out.println(matcher.group(1));
		System.out.println(matcher.group(2));

	}
}
