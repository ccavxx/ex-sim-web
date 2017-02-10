package com.topsec.tsm.sim.log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.util.ticker.Tickerable;

public class LogCountStatTask implements Tickerable{
	
	private static final Logger logger = LoggerFactory.getLogger(LogCountStatTask.class) ;
	private NodeMgrFacade nodeMgr ;
	private long lastExecuteTime ;
	private List<Map<String,Object>> deivceTypeLogCount ;
	public List<Map<String, Object>> getDeivceTypeLogCount() {
		return deivceTypeLogCount;
	}
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgr = nodeMgr;
	}

	@Override
	public void onTicker(long ticker) {
		try{
			long currentTime = System.currentTimeMillis() ;
			if((currentTime - lastExecuteTime) < 3 *60 * 1000){
				return ;
			}
			Calendar cal = Calendar.getInstance() ;
			int currentDay = cal.get(Calendar.DAY_OF_YEAR) ;
			cal.setTimeInMillis(lastExecuteTime) ;
			int previousDay = cal.get(Calendar.DAY_OF_YEAR) ;
			//时区偏移
			//跨天,重置日志计数
			if(currentDay != previousDay){
				List<AssetObject> allAsset = AssetFacade.getInstance().getAll() ;
				for(AssetObject ao:allAsset){
					ao.setLogCount(0) ;
				}
			}
			Map<String,Object> params = new HashMap<String, Object>() ;
			Date now = new Date() ;
			params.put("startDate",ObjectUtils.dayBegin(now)) ;
			params.put("endDate", ObjectUtils.dayEnd(now)) ;
			List<Node> reportNodes = nodeMgr.getNodesByType(NodeDefinition.NODE_TYPE_REPORTSERVICE, false, false, false, false) ;
			if(ObjectUtils.isEmpty(reportNodes)){//节点还没注册
				return ;
			}
			Node reportNode = reportNodes.get(0) ;
			AssetLogCountCollector assetLogCountCollector = null ;
			DeviceTypeLogCountCollector deviceTypeLogCountCollector = null ;
			if (NodeStatusQueueCache.online(reportNode.getNodeId())) {
				assetLogCountCollector = new AssetLogCountCollector(NodeUtil.getRoute(reportNode), params);
				deviceTypeLogCountCollector = new DeviceTypeLogCountCollector(NodeUtil.getRoute(reportNode)) ;
			}else{
				return ;
			}
			lastExecuteTime = currentTime ;
			collectAssetLogCount(assetLogCountCollector) ;
			collectDeviceTypeLogCount(deviceTypeLogCountCollector) ;
			//ExecutorService executor = Executors.newFixedThreadPool(collectors.size()) ;
			//List<Future<List<Map>>> executorResult = executor.invokeAll(collectors) ;
			
		}catch (Exception e) {
			logger.error("日志统计任务执行出错!",e) ;
		}
	}
	
	private void collectAssetLogCount(AssetLogCountCollector collector){
		try {
			List<Map<String,Object>> result = collector.call() ;
			Map<String,Long> allIpLogCount = new HashMap<String,Long>() ;
			List<Map<String,Object>> nodeIpAndCountList = result ;//每个节点的ip和日志数量统计值
			for(Map<String,Object> ipAndCount:nodeIpAndCountList){
				String ip = (String) ipAndCount.get("ip") ;
				Number count = (Number) ipAndCount.get("counts") ;
				Long c= allIpLogCount.get(ip) ;
				if(c == null){
					allIpLogCount.put(ip, count.longValue()) ;
				}else{
					allIpLogCount.put(ip, count.longValue() + c) ;
				}
			}
			for(Map.Entry<String, Long> entry:allIpLogCount.entrySet()){
				AssetObject ao = AssetFacade.getInstance().getAssetByIp(entry.getKey()) ;
				if (ao != null) {
					ao.setLogCount(entry.getValue()) ;
				}
			}
		} catch (Exception e) {
			logger.error("", e) ;
		}
	}
	
	private void collectDeviceTypeLogCount(DeviceTypeLogCountCollector collector){
		try {
			List<Map<String,Object>> result = collector.call() ;
			if(result != null){
				deivceTypeLogCount = result ;
			}
		} catch (Exception e) {
			logger.error("",e) ;
		}
	}
	
	class AssetLogCountCollector implements Callable<List<Map<String,Object>>>{
		private String[] routes ;
		private Map<String,Object> params ;
		public AssetLogCountCollector(String[] routes,Map<String,Object> params) {
			this.routes = routes;
			this.params = params ;
		}

		@Override
		public List<Map<String,Object>> call() throws Exception {
			List<Map<String,Object>> result ;
			try {
				result = NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_STAT_LOG_COUNT_DAY, (Serializable)params, 10000L);
			}catch (Exception e) {
				logger.error("日志数量获取任务执行出错!",e) ;
				result = Collections.emptyList() ;
			}
			return result ;
		}
	}
	
	class DeviceTypeLogCountCollector implements Callable<List<Map<String,Object>>>{
		private String[] routes ;
		public DeviceTypeLogCountCollector(String[] routes) {
			this.routes = routes;
		}
		@Override
		public List<Map<String,Object>> call() throws Exception {
			List<Map<String,Object>> result = null;
			try {
				result = NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_STAT_LOG_COUNT_TYPE,null, 10000L);
			}catch (Exception e) {
				logger.error("日志数量获取任务执行出错!!",e) ;
			}
			return result ;
		}
	}
}