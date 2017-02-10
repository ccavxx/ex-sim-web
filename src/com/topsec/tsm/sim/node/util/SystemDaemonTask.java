package com.topsec.tsm.sim.node.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.EnhanceProperties;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.SystemConfigService;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.LicenceServiceUtil;

/**
* 功能描述: 系统守护进程起始程序
*/
public final class SystemDaemonTask {
	
	private static final Logger log = LoggerFactory.getLogger(SystemDaemonTask.class);
	
	private static int assetCount=-1;
	
	private AssetService assetService;
	
	private SystemConfigService systemConfigService;
	
	private NodeMgrFacade nodeMgrFacade;
	
	private final int sleepTime=3*60*1000;  //3分钟
	
	private volatile boolean shutdown = false;
	
	private List<Node> nodeList;
	
	
	
	public SystemDaemonTask() {
		Thread hook = new Thread(){
			public void run() {
				shutdown = true;
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);
	}

	public void init(){
		Thread thread = new Thread(new InnerThread(),"LicenseCheckThread");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	* @method: checkAssetNumAndLinceseNum 
	* 			检查资产个数和lincese里的资产个数.
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: void
	*/
	private void checkAssetNumAndLinceseNum(){
		assetCount = assetService.getEnabledTotal() ;
		int licenceNum = 0;
		Map licenceMap=LicenceServiceUtil.getInstance().getLicenseInfo();
		String license_valid=(String)licenceMap.get("LICENSE_VALID");
		if(license_valid==null||license_valid.equals("0")){
			licenceNum = 0;
		} else {
			licenceNum = Integer.valueOf((String)licenceMap.get("TSM_ASSET_NUM"));
			if(licenceNum >= 0){
				if(assetCount-licenceNum>0){
					//启用的日志源个数比licence规定的要多,则禁用掉所有日志源
					try {
						assetService.disableAll() ;
						AssetFacade.getInstance().reloadAllFromDB() ;
						AuditRecord record = AuditLogFacade.createSystemAuditLog()
														   .stop().highest()
														   .userOperation("禁用所有资产","", "启用资产数"+assetCount+"超出License上限"+licenceNum+"，禁用所有系统资产。") ;
						AuditLogFacade.send(record) ;
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			} 
		}
	}
	
	/**
	* @method: checkNodeIsAlive
	* 			检查节点是否长期掉线
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: void
	 * @throws Exception 
	*/
	private void checkNodeIsAlive() throws Exception{
		nodeList=nodeMgrFacade.getAll();
		if(nodeList != null){
			for (Node node : nodeList) {
				String nodeType = node.getType() ; 
				if(ObjectUtils.equalsAny(nodeType,
						NodeDefinition.NODE_TYPE_CHILD,
						NodeDefinition.NODE_TYPE_PARENT,
						NodeDefinition.NODE_TYPE_SMP)){
					continue ;
				}
				if(NodeStatusQueueCache.offline(node.getNodeId())){
					Map<String,String> logMap=new HashMap<String, String>();
					logMap.put("nodeIp", node.getIp());
					if(NodeDefinition.NODE_TYPE_AGENT.equals(node.getType())){
						logMap.put("typeZH", "代理");
					}else if (NodeDefinition.NODE_TYPE_AUDIT.equals(node.getType())) {
						logMap.put("typeZH", "服务器");
					}else{
						logMap.put("typeZH", node.getType());
					}
					NodeStatusQueueCache.getInstance().sendAgentOfflineLog(logMap);
				}
			}
		}
	}
	
	class InnerThread implements Runnable{

		@Override
		public void run() {
			try {
				EnhanceProperties pt = new EnhanceProperties("../../../../conf/build.properties") ;
				System.setProperty("tal.version", pt.getProperty("version"));
			
				while(!shutdown){
					try {
						if(!shutdown){//检查日志源个数和lincese里的日志源个数.
							checkAssetNumAndLinceseNum();
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(),e);
					}
					
					try {
						//检查节点是否长期掉线
						if(!shutdown){
							checkNodeIsAlive();
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			}catch(Exception e){
				e.printStackTrace() ;
			}
		}
	}
	
	public void resetAssetCount() {
		SystemDaemonTask.assetCount = -1;
	}
	
	public void setAssetService(AssetService assetService) {
		this.assetService = assetService;
	}

	public SystemConfigService getSystemConfigService() {
		return systemConfigService;
	}

	public void setSystemConfigService(SystemConfigService systemConfigService) {
		this.systemConfigService = systemConfigService;
	}
	
	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}
}
