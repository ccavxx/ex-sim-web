package com.topsec.tsm.sim.log.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.component.handler.LogMonitorFormater;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.log.util.LogCache;
import com.topsec.tsm.sim.log.util.LogTimerTask;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.util.UUIDUtils;
@Controller
@RequestMapping("logMonitor")
public class LogMonitorController {
	private static NodeMgrFacade nodeMgrFacade;

	private static final Logger logger = LoggerFactory
			.getLogger(LogMonitorController.class);

	@Autowired
	public void setNodeMgrFacade(NodeMgrFacade nodeMgr) {
		this.nodeMgrFacade = nodeMgr;
	}
	private static boolean unUsable =false;
	public volatile static boolean isSend =true;//是否发送数据
	private static LogTimerTask myTimeTask = null; 
	private static String coustomIp = null;
	private static String username= null;
	private static Thread thred = null;
	private DataSourceService dataSourceService;
	
	@Autowired
	@Qualifier("dataSourceService")
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}
	/**
	 * login 用户登录实时日志时验证是否已登录
	 * @author zhou_xiaohu@topsec.com.cn
	 * @param request
	 * @return Object
	 */
	@RequestMapping("login")
	@ResponseBody
	public Object login(SID sid,HttpServletRequest request){
		JSONObject json=new JSONObject();
		try {
			if(coustomIp != null && username != null && unUsable && sid != null){
				if(username.equals(sid.getUserName()) && coustomIp.equals(request.getRemoteAddr())){
					json.put("usable", "true");
					unUsable =false;
				}
			}
			if(!unUsable){
				if(myTimeTask != null){
					myTimeTask.setRunning(false) ;
				}
				isSend = true;
				try {
					List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, true, true, true, true);
					List<AuthUserDevice> devices = this.getUserDevices(sid);
					for(Node node : nodes){
						Component component = nodeMgrFacade.getBindableComponentByType(node,NodeDefinition.HANDLER_LOG_MONITOR,true);
						
						if(component != null) {
							LogMonitorFormater config = nodeMgrFacade.getSegmentConfigByClass(component, LogMonitorFormater.class);
							config.setEnable(true);
							String filter="";
							if(devices.size()>0){
								Map<String,List<String>> userDeviceTypeAndIps = getUserDeviceAndIP(devices) ;
								StringBuffer sb = new StringBuffer("SELECTOR(") ;
								int index = 0 ;
								for(Map.Entry<String, List<String>> entry:userDeviceTypeAndIps.entrySet()){
									if(index++ != 0){
										sb.append(" OR ") ;
									}
									sb.append("((DVC_TYPE = '").append(entry.getKey()).append("') AND (") ;
									List<String> ipList = entry.getValue() ;
									for(int i=0;i<ipList.size();i++){
										if(i != 0){
											sb.append(" OR ") ;
										}
										sb.append("DVC_ADDRESS = '").append(ipList.get(i)).append("'") ;
									}
									sb.append("))") ;
								}
								sb.append(")") ;
								filter = sb.toString() ;
						     } else if(sid.isOperator()){
						    	 filter= "SELECTOR(TRUE)";
						     }else{
						    	 json.put("usable", "");
						    	 return json;
						     }
							config.setFilter(filter);
							nodeMgrFacade.updateComponentSegmentAndDispatch(component, config);
						}
						String[] route = null;
						route=NodeUtil.getRoute(node);
					    NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_START_LOGMONITOR,  null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				json.put("usable", "true");
				unUsable = true;
				myTimeTask = new LogTimerTask();
				thred = new Thread(myTimeTask,"LogMonitorChecker");
				thred.start();
				username = sid.getUserName();
				coustomIp = request.getRemoteAddr();
			}else{
				unUsable = true;
				json.put("ip",coustomIp);
				json.put("usable", "false");
			}
		} catch (Exception e) {
			unUsable = true;
			json.put("usable", "false");
			e.printStackTrace();
		} 
		return json;
	}
	private static Map<String,List<String>> getUserDeviceAndIP(List<AuthUserDevice> userDevices){
		Map<String,List<String>> result = new HashMap<String,List<String>>() ;
		for(AuthUserDevice device:userDevices){
			List<String> ipAddress = result.get(device.getDeviceType()) ;
			if(ipAddress == null){
				result.put(device.getDeviceType(), (ipAddress = new ArrayList<String>())) ;
			}
			ipAddress.add(device.getIp()) ;
		}
		return result ;
	}
	/**
	 * 退出实时监视模块
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return void
	 */
	@RequestMapping("logOut")
	public static void  logOut(){
		unUsable = false;
		isSend = true;
		try {
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, true, true, true, true);
			for(Node node : nodes){
				Component component = nodeMgrFacade.getBindableComponentByType(node,NodeDefinition.HANDLER_LOG_MONITOR,true);
				if(component != null) {
					LogMonitorFormater config = nodeMgrFacade.getSegmentConfigByClass(component, LogMonitorFormater.class);
					config.setEnable(false);
					config.setFilter("SELECTOR(FALSE)");
					nodeMgrFacade.updateComponentSegmentAndDispatch(component, config);
					String[] route = null;
					route=NodeUtil.getRoute(node);
				    NodeUtil.getCommandDispatcher().sendCommand(route, MessageDefinition.CMD_STOP_LOGMONITOR,  null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			myTimeTask.setRunning(false);
		}
	}
	/**
	 * setFilter 用于设置实时日志过滤器
	 * 
	 * @param host
	 * @param deviceType
	 * @param nodeId
	 */
	@RequestMapping("setFilter")
	@ResponseBody
	public Object setFilter(SID sid,
			              @RequestParam(value = "host") String host,
						  @RequestParam(value = "deviceType") String deviceType,
						  @RequestParam(value = "nodeId") String nodeId,
						  @RequestParam(value = "filter") String filterValue) {
		try {
			if (host == null || deviceType == null || nodeId == null) {
				logger.error("host == null || deviceType == null || nodeId == null");
				throw new Exception("host == null || deviceType == null || nodeId == null");
			}
			List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, true, true, true, true);
			List<AuthUserDevice> devices = this.getUserDevices(sid);
			for (Node node : nodes) {
				Component component = nodeMgrFacade.getBindableComponentByType(node,NodeDefinition.HANDLER_LOG_MONITOR, true);
				if (component != null) {
					LogMonitorFormater config = new LogMonitorFormater();
					config = nodeMgrFacade.getSegConfigByComAndT(component,config);
					config.setEnable(true);
					String filter = null;
					if (host == null || host.equals("undefined")) {
						if("ALL/ALL/Default".equals(deviceType)) {
							if(devices.size()>0){
								filter= "SELECTOR(";
								for( AuthUserDevice userDeviceParent :  devices)
						        {
									if(filter.indexOf(userDeviceParent.getDeviceType()) >=0)
										continue;
									String filterParent = "((DVC_TYPE ='"+userDeviceParent.getDeviceType()+"') AND (";
									String addressTemp="";
									for( AuthUserDevice userDeviceChild :  devices)
							        {        
							            if(userDeviceChild.getDeviceType().equals(userDeviceParent.getDeviceType())){
							            	if("".equals(addressTemp))
							            		addressTemp = "DVC_ADDRESS = '" + userDeviceChild.getIp() +"'";
							            	else
							            		addressTemp += " OR DVC_ADDRESS = '" + userDeviceChild.getIp() +"'";
							            }
							         }
									 filterParent +=addressTemp+ "))";
									 if("SELECTOR(".equals(filter)){
										 filter += filterParent;
									 }else{
										 filter += " OR " +filterParent;
									 }
						        }
								 if(!"".equals(filterValue)){
										filter += " AND "+filterValue;
									}
								filter +=	")";
							}else{
								filter = "SELECTOR(TRUE)";
							}
							
						}else{
							if(devices.size()>0){
								filter= "SELECTOR((DVC_TYPE = '" + deviceType + "') AND (";
								String addressTemp="";
								for( AuthUserDevice userDevice :  devices)
						        {            
						            if(userDevice.getDeviceType().equals(deviceType)){
						            	if("".equals(addressTemp))
						            		addressTemp = "DVC_ADDRESS = '" + userDevice.getIp() +"'";
						            	else
						            		addressTemp += " OR DVC_ADDRESS = '" + userDevice.getIp() +"'";
						            }
						         }
								filter +=addressTemp+ ")";
								 if(!"".equals(filterValue)){
										filter += " AND "+filterValue;
									}
								filter +=	")";
						     } else{
						    	 filter= "SELECTOR((DVC_TYPE = '" + deviceType + "')";
						    	 if(!"".equals(filterValue)){
										filter += " AND "+filterValue;
									}
								filter +=	")";
						     }
						}
					}else{
						filter = "SELECTOR((DVC_TYPE = '" + deviceType + "') AND (DVC_ADDRESS = '" + host + "')";
						 if(!"".equals(filterValue)){
								filter += " AND "+filterValue;
							}
						filter +=	")";
					}
					config.setFilter(filter);
					nodeMgrFacade.updateComponentSegmentAndDispatch(
							component, config);
					LogCache.getInstance().clearCache();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null ;
	}
	/**
	 * changIsSend: 改变当前isSend值
	 * @author zhou_xiaohu@topsec.com.cn
	 * @throws Exception
	 */
	@RequestMapping("changIsSend")
	@ResponseBody
	public Object changIsSend(){
		isSend = true;
		return "" ;
	}
	
	/**
	 * getLogData 获得实时日志数据
	 * @author zhou_xiaohu@topsec.com.cn
	 * @throws Exception
	 */
	@RequestMapping("getLogData")
	@ResponseBody
	public Object getLogData(@RequestParam(value = "host",defaultValue="")String host,
							 @RequestParam(value = "deviceType",defaultValue=SimDatasource.DATASOURCE_ALL)String deviceType,
							 HttpServletRequest request) throws Exception {
		JSONArray logJson = new JSONArray();
		List<Map<String, Object>> logList = LogCache.getInstance().getCache();
		Locale locale=request.getLocale();
		for(Map<String,Object> event:logList){
			JSONObject  json = new JSONObject();
			String address = StringUtil.toString(event.get("DVC_ADDRESS")) ;
			String eventDeviceType = (String) event.get("DVC_TYPE") ;
			if(host.equals(address) || deviceType.equals(eventDeviceType) || deviceType.equals(SimDatasource.DATASOURCE_ALL)){
				Iterator eventKey =event.keySet().iterator();
				while(eventKey.hasNext()){
					Object key = eventKey.next();
					String keyName = String.valueOf(key);
					String fieldValue = "";
					if("DVC_TYPE".equals(keyName)){
						 fieldValue =DeviceTypeNameUtil.getDeviceTypeName(String.valueOf(event.get(key)),locale);
					}if("ORIGINAL_DATA".equals(keyName)){
						fieldValue = event.get(key).toString().replace("{", "").replace("}", "");
					}else if("START_TIME".equals(keyName) || "DVC_ADDRESS".equals(keyName)){
						 fieldValue = String.valueOf(event.get(key));
					}
					if (StringUtil.isNotBlank(fieldValue) && !fieldValue.trim().equals("null")) {
						if(event.get(key) instanceof Date){
							json.put(keyName, StringUtil.dateToString((Date)event.get(key),"yyyy-MM-dd HH:mm:ss"));
						}else{
							json.put(keyName, fieldValue);
						}
					}
				}
				json.put("UUID", UUIDUtils.lightCompressedUUID()) ;
				logJson.add(json);
			}
		}
		LogCache.getInstance().clearCache();
		isSend = true;
		return logJson;
	}
	/**
	 * clearData 清空缓存数据
	 * @author zhou_xiaohu@topsec.com.cn
	 * @throws Exception
	 */
	@RequestMapping("clearData")
	@ResponseBody
	public Object clearData()throws Exception {
		JSONObject json = new JSONObject();
		LogCache.getInstance().clearCache();
		json.put("success","true");
		return json;
	}
	/**
	 * 获取用户权限信息
	 * @param sid
	 * @return
	 */
	private List<AuthUserDevice> getUserDevices(SID sid){
		Set devices = sid.getUserDevice();
		List<AuthUserDevice> device =new ArrayList<AuthUserDevice>();
		for( Iterator   it = devices.iterator(); it.hasNext(); )
        {            
            AuthUserDevice userDevice = (AuthUserDevice)it.next();  
            List<SimDatasource> datasource = dataSourceService.getByIp(userDevice.getIp());
            for(SimDatasource data : datasource){
            	  AuthUserDevice user = new AuthUserDevice();
            	  user.setDeviceType(data.getSecurityObjectType());
            	  user.setIp(data.getDeviceIp());
            	  user.setNodeId(data.getNodeId());
            	  device.add(user);
            }
        } 
		return device;
	}
	/**
	 * 
	 * 根据设备类型获取列集
	 * @param deviceType
	 * @return
	 */
	@RequestMapping("getGroupByDeviceType")
	@ResponseBody
	public Object getGroupByDeviceType(SID sid,@RequestParam(value="deviceType",defaultValue="")String deviceType){
		Map<String,String[]> group= sid.getGroupId();
		List<Map<String,Object>>  listGroup = IndexTemplateUtil.getInstance().getGroupByDeviceType(deviceType);
		List<Map<String,Object>>  groups = new ArrayList<Map<String,Object>>();
		if((group != null && group.containsKey(deviceType)) || group == null){
			for(Map<String,Object> map : listGroup){
				if(group != null && group.containsKey(deviceType)){
					for(String groupId : group.get(deviceType)){
						if(groupId.equals(map.get("groupId").toString())){
							TreeModel model = new TreeModel();
							model.setText(map.get("name").toString());
							model.setAttributes(map);
							groups.add(map);
						}
					}
				}else if(group == null){
					TreeModel model = new TreeModel();
					model.setText(map.get("name").toString());
					model.setAttributes(map);
					groups.add(map);
				}
			}
		}else{
			return listGroup;
		}
		return groups;
	}
}
