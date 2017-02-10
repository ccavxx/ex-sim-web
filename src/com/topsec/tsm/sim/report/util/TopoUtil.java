package com.topsec.tsm.sim.report.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.poifs.storage.ListManagedBlock;
import org.stringtemplate.v4.compiler.STParser.ifstat_return;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;

/**
 * @ClassName: TopoUtil
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年9月16日下午3:12:33
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class TopoUtil {
	private static TopoUtil topoUtil=null;
	private static DeviceService deviceService;
	private static DataSourceService dataSourceService;
	private static NodeMgrFacade nodeMgrFacade;
	private static Node auditor;
	private static InputStream inStream;
	private static Properties prop;
	private static Map<Object, Device> deviceMap;
	private static Map<Object,List<SimDatasource>>simDatasourceListMap;
	private static Map<Object,List<Object>>topoAssObjListMap;
	private static Map<Object,List<Device>>deviceListMap;
	private static Map<Object,List<Device>>deviceListTopoNodeIdMap;
	private static Map<Object,List<Map>>topoGroupListMap;
	private static Map<Object,List<Map>>pretreatmenttopoGroupListMap;
	private static Map<Object,List<Map>>groupTopoNodeIdGroupListMap;
	static{
		deviceService=(DeviceService) SpringContextServlet.springCtx.getBean("deviceService");
		dataSourceService=(DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService");
		nodeMgrFacade=(NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade");
		auditor = nodeMgrFacade.getKernelAuditor(false);
		inStream = TopoUtil.class.getResourceAsStream("/resource/report/evtNodeTypeReport.properties");
		prop = new Properties();
		deviceMap=new HashMap<Object, Device>();
		simDatasourceListMap=new HashMap<Object, List<SimDatasource>>();
		topoAssObjListMap=new HashMap<Object, List<Object>>();
		deviceListMap=new HashMap<Object, List<Device>>();
		deviceListTopoNodeIdMap=new HashMap<Object, List<Device>>();
		topoGroupListMap=new HashMap<Object, List<Map>>();
		pretreatmenttopoGroupListMap=new HashMap<Object, List<Map>>();
		groupTopoNodeIdGroupListMap=new HashMap<Object, List<Map>>();
		List<Device> devices= deviceService.getAll() ;
		for (Device device : devices) {
			deviceMap.put(device.getId(), device);
			List<SimDatasource> simDatasources=null;
			String deviceIp=device.getMasterIp().toString();
			if (!simDatasourceListMap.containsKey(deviceIp)) {
				simDatasources=dataSourceService.getByIp(deviceIp);
				simDatasourceListMap.put(deviceIp, simDatasources);
			}
		}
		try {
			prop.load(inStream);
			inStream.close();
		} catch (IOException e) {
		}
	}
	public static List<AssTopo> allAssTopoList(TopoService topoService){
		List<AssTopo> list=topoService.getAll();
		//暂时不加载系统拓扑，加载的话重新打开注释
		//list.add(0, topoService.getSystemTopo(nodeMgrFacade));
		return list;
	}
	public static List<Object> getTopoConfigObjects(AssTopo assTopo){
		if (GlobalUtil.isNullOrEmpty(assTopo)) {
			return null;
		}
		List<Object> resultObjList=null;
		if (topoAssObjListMap.containsKey(assTopo.getId())
				&& -1!=assTopo.getId()) {
			resultObjList=topoAssObjListMap.get(assTopo.getId());
		}else {
			resultObjList=XmlStringAnalysis.getTopoListMap(XmlStringAnalysis.stringDocument(assTopo.getConfig()));
			topoAssObjListMap.put(assTopo.getId(), resultObjList);
		}
		return resultObjList;
	}
	public static void reLoad(AssTopo assTopo){
		List<Object> resultObjList=XmlStringAnalysis.getTopoListMap(XmlStringAnalysis.stringDocument(assTopo.getConfig()));
		topoAssObjListMap.put(assTopo.getId(), resultObjList);
		if (deviceListMap.containsKey(assTopo.getId())) {
			deviceListMap.remove(assTopo.getId());
		}
		if (pretreatmenttopoGroupListMap.containsKey(assTopo.getId())) {
			pretreatmenttopoGroupListMap.remove(assTopo.getId());
		}
		if (topoGroupListMap.containsKey(assTopo.getId())) {
			topoGroupListMap.remove(assTopo.getId());
		}
		deviceListTopoNodeIdMap.clear();
		groupTopoNodeIdGroupListMap.clear();
	}
	public static List<Device> showIpsByAssTopo(AssTopo assTopo){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<Device> deviceList=null;
		if (deviceListMap.containsKey(assTopo.getId())) {
			deviceList=deviceListMap.get(assTopo.getId());
		}else {
			deviceList=new ArrayList<Device>();
			allAssIps(objList,deviceList);
			deviceListMap.put(assTopo.getId(), deviceList);
		}
		return deviceList;
	}
	/**
	 * 返回所有的ip
	 * @param objList
	 * @param ipList
	 * @return
	 */
	private static List<Device> allAssIps(List<Object>objList,List<Device> deviceList){
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<Object> idsList=lineContainsId(objList);
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				Object ipObject=map.get("ip");
				if ("asset".equals(map.get("type")) 
						&& !GlobalUtil.isNullOrEmpty(ipObject)
						&& !GlobalUtil.isNullOrEmpty(map.get("id"))) {
					Device device=null;
					if (deviceMap.containsKey(map.get("id"))) {
						device=deviceMap.get(map.get("id"));
					}else {
						device=deviceService.getDevice(map.get("id").toString());
						deviceMap.put(map.get("id"), device);
					}//此判断可以控制根topo图显示全部设备或是只显示未被分组的设备
					if (idsList.contains(map.get("id"))) {
						continue;
					}
					if (!GlobalUtil.isNullOrEmpty(device)
						&& !isContains(deviceList,ipObject)
						&& !deviceList.contains(device)) {
						List<SimDatasource> simDatasources=null;
						String deviceIp=device.getMasterIp().toString();
						if (simDatasourceListMap.containsKey(deviceIp)) {
							simDatasources=simDatasourceListMap.get(deviceIp);
						}else {
							simDatasources=dataSourceService.getByIp(deviceIp);
							simDatasourceListMap.put(deviceIp, simDatasources);
						}
						boolean notFound = true ;
						if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
							for (SimDatasource simDatasource : simDatasources) {
								device.setDeviceType(simDatasource
										.getSecurityObjectType());
								device.setScanNodeId(simDatasource
										.getAuditorNodeId());
								notFound = false;
								break;
							}
						}
						if (GlobalUtil.isNullOrEmpty(simDatasources)||notFound) {
							device.setScanNodeId(auditor.getNodeId());
						}
						deviceList.add(device);
					}
				}
				if (map.containsKey("groupMembers")) {
					List<Object>childrenList=(List<Object>)map.get("groupMembers");
					if (!GlobalUtil.isNullOrEmpty(childrenList)) {
						allAssIps(childrenList,deviceList);
					}
				}
			}
		}
		return deviceList;
	}
	private static boolean isContains(List<Device> deviceList,Object ip){
		if (GlobalUtil.isNullOrEmpty(deviceList)) {
			return false;
		}
		for (Device device : deviceList) {
			if (GlobalUtil.isNullOrEmpty(device.getMasterIp())) {
				continue;
			}
			if (ip.equals(device.getMasterIp().toString())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 返回所在分组一级的所有ip，不包含子分组
	 * @param objList
	 * @param ipList
	 * @return
	 */
	static List<String> stairAssIps(List<Object>objList,List<String> ipList){
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				Object ipObject=map.get("ip");
				if ("asset".equals(map.get("type")) 
						&& !GlobalUtil.isNullOrEmpty(ipObject)
						&& !ipList.contains(ipObject)) {
					ipList.add(ipObject.toString());
				}
				
			}
		}
		return ipList;
	}
	
	private static String assIpSelfByNodeId(List<Object>objList,String nodeId){
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				Object ipObject=map.get("ip");
				if ("asset".equals(map.get("type")) 
						&& !GlobalUtil.isNullOrEmpty(ipObject)
						&& nodeId.equals(map.get("id"))) {
					return ipObject.toString();
				}
				
			}
		}
		return null;
	}
	/**
	 * 此处的nodeId是节点的id
	 * @param assTopo
	 * @param nodeId
	 * @return
	 */
	public static List<Device> showIpsByAssGroup(AssTopo assTopo,String nodeId){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<Device> deviceList=null;
		if (deviceListTopoNodeIdMap.containsKey(nodeId+assTopo.getId())) {
			deviceList=deviceListTopoNodeIdMap.get(nodeId+assTopo.getId());
		}else {
			deviceList=new ArrayList<Device>();
			stairGroupIps(objList,deviceList,nodeId,true);
			deviceListTopoNodeIdMap.put(nodeId+assTopo.getId(), deviceList);
		}
		return deviceList;
	}
	
	public static String showIpByAssNodeId(AssTopo assTopo,String nodeId){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		String ip=assIpSelfByNodeId(objList,nodeId);
		return ip;
	}
	
	/**
	 * nodeId为节点的id
	 * @param objList
	 * @param ipList
	 * @param nodeId
	 * @return
	 */
	private static List<Device> stairGroupIps(List<Object>objList,List<Device> deviceList,String nodeId,boolean isRecursion){
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<String>nodeIdList=new ArrayList<String>();
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				Object ipObject=map.get("ip");
				if (!GlobalUtil.isNullOrEmpty(ipObject)) {
					if (nodeId.equals(map.get("id")) 
							&& "asset".equals(map.get("type")) 
							&& !GlobalUtil.isNullOrEmpty(map.get("id"))) {
						Device device=null;
						if (deviceMap.containsKey(map.get("id"))) {
							device=deviceMap.get(map.get("id"));
						}else {
							device=deviceService.getDevice(map.get("id").toString());
							deviceMap.put(map.get("id"), device);
						}
					if (!GlobalUtil.isNullOrEmpty(device)
						&& !isContains(deviceList,ipObject)
						&& !deviceList.contains(device)) {
						List<SimDatasource> simDatasources=null;
						String deviceIp=device.getMasterIp().toString();
						if (simDatasourceListMap.containsKey(deviceIp)) {
							simDatasources=simDatasourceListMap.get(deviceIp);
						}else {
							simDatasources=dataSourceService.getByIp(deviceIp);
							simDatasourceListMap.put(deviceIp, simDatasources);
						}
						for (SimDatasource simDatasource : simDatasources) {
							if (!GlobalUtil.isNullOrEmpty(simDatasource)) {
								if (simDatasource.getSecurityObjectType().startsWith(device.getDeviceType())) {
									device.setDeviceType(simDatasource.getSecurityObjectType());
									device.setScanNodeId(simDatasource.getAuditorNodeId());
									break;
								}
							}
						}//以下代码在多个 auditor的时候可能会引来bug
						if (GlobalUtil.isNullOrEmpty(simDatasources)) {
							device.setScanNodeId(auditor.getNodeId());
						}
						deviceList.add(device);
					}
					}
				}else if ("实线".equals(map.get("type")) && isRecursion) {
					Object nodeIdObject=map.get("source");
					if (nodeId.equals(map.get("destination"))
							&& !GlobalUtil.isNullOrEmpty(nodeIdObject)
							&& !nodeIdList.contains(nodeIdObject)) {
						nodeIdList.add(nodeIdObject.toString());
					}
				} else if (map.containsKey("groupMembers")) {
					List<Object>childrenList=(List<Object>)map.get("groupMembers");
					if (!GlobalUtil.isNullOrEmpty(childrenList)) {
						stairGroupIps(childrenList,deviceList,nodeId,true);
					}
				}
			}
		}
		if (!GlobalUtil.isNullOrEmpty(nodeIdList)) {
			for (String string : nodeIdList) {
				stairGroupIps(objList,deviceList,string,false);
			}
		}
		return deviceList;
	}
	/**
	 * 此方法返回 拓扑下的所有直接分组，(拓扑中非孩子节点的所有)不包含递转
	 * @param assTopo
	 * @return
	 */
	public static List<Map> pretreatmentGroupListByAssTopo(AssTopo assTopo){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<Map> mapList=null;
		if (pretreatmenttopoGroupListMap.containsKey(assTopo.getId())) {
			mapList=pretreatmenttopoGroupListMap.get(assTopo.getId());
		}else {
			List<String>nodeIdList=new ArrayList<String>();
			getGroupNodeList(objList,nodeIdList,false);
			mapList=getMapsByNodeIds(objList,nodeIdList);
			pretreatmenttopoGroupListMap.put(assTopo.getId(), mapList);
		}
		return mapList;
	}
	private static List<Map> getMapsByNodeIds(List<Object> objList,List<String>nodeIdList){
		List<Map> mapList=null;
		if (!GlobalUtil.isNullOrEmpty(nodeIdList)) {
			mapList=new ArrayList<Map>();
			for (String string : nodeIdList) {
				Map map=getGroupMap(objList,string);
				if (!GlobalUtil.isNullOrEmpty(map)
						&& !mapList.contains(map)) {
					mapList.add(map);
				}
			}
		}
		return mapList;
	}
	/**
	 * 此方法返回 拓扑下的所有直接一级分组，不包含递转
	 * @param assTopo
	 * @return
	 */
	public static List<Map> showGroupListByAssTopo(AssTopo assTopo){
		List<Map> mapList=null;
		if (topoGroupListMap.containsKey(assTopo.getId())) {
			mapList=topoGroupListMap.get(assTopo.getId());
		}else {
			mapList=pretreatmentGroupListByAssTopo(assTopo);
			mapList=filterGroupList(mapList,assTopo);
			topoGroupListMap.put(assTopo.getId(), mapList);
		}
		return mapList;
	}
	private static boolean isContainsAsset(List<Object> objList,String nodeId){
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if (nodeId.equals(map.get("id"))) {
					return true;
				}
			}
		}
		return false;
	}
	static boolean isContainsLineDe(List<Object> objList,String nodeId){
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type"))
						&& nodeId.equals(map.get("destination"))) {
					return true;
				}
			}
		}
		return false;
	}
	static boolean isContainsLineSrc(List<Object> objList,String nodeId){
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type"))
						&& nodeId.equals(map.get("source"))) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 
	 * @param objList
	 * @return 拓扑中 属于分组中的 所有设备id集合
	 */
	static List<Object> lineContainsAllId(List<Object> objList){
		List<Object>idsList=null;
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return idsList;
		}
		idsList=new ArrayList<Object>(objList.size());
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type")) ) {
					if (!idsList.contains(map.get("source"))) {
						idsList.add(map.get("source"));
					}
					if (!idsList.contains(map.get("destination"))) {
						idsList.add(map.get("destination"));
					}
				}
			}
		}
		return idsList;
	}
	/**
	 * 
	 * @param objList
	 * @return 返回拓扑中 属于分组中 非元组的设备id集合
	 */
	private static List<Object> lineContainsId(List<Object> objList){
		List<Object>idsList=null;
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return idsList;
		}
		idsList=new ArrayList<Object>(objList.size());
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type")) ) {
					if (!idsList.contains(map.get("source"))) {
						idsList.add(map.get("source"));
					}
				}
			}
		}
		return idsList;
	}
	private static List<String> getGroupNodeList(List<Object> objList,List<String>nodeIdList,boolean isfilter){
		List<String> removeList=new ArrayList<String>();
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if (!isfilter) {
					if ("实线".equals(map.get("type"))) {
						Object nodeIdObject = map.get("destination");
						if (!GlobalUtil.isNullOrEmpty(nodeIdObject)
								&& !nodeIdList.contains(nodeIdObject)) {
							nodeIdList.add((String)nodeIdObject);
						}
					}
				}else{
					Object nodeIdObject = map.get("id");
					if (!GlobalUtil.isNullOrEmpty(nodeIdObject)) {
						if (!isContainsLineDe(objList,nodeIdObject.toString())
								&& nodeIdList.contains(nodeIdObject)) {
							removeList.add(nodeIdObject.toString());
						}
					}
					
					if ("实线".equals(map.get("type"))) {
						Object nodeIdObject1=map.get("source");
						if (!GlobalUtil.isNullOrEmpty(nodeIdObject1) 
								&& !isContainsAsset(objList,nodeIdObject1.toString())
								&& nodeIdList.contains(nodeIdObject1)) {
							removeList.add((String)nodeIdObject1);
						}
					}
				}
			}
		}
		if (isfilter) {
			nodeIdList.removeAll(removeList);
		}
		return nodeIdList;
	}
	private static Map getGroupMap(List<Object> objList,String nodeId){
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if (nodeId.equals(map.get("id"))) {
					return map;
				}
			}
		}
		return null;
	}
	private static List<Map> filterGroupList(List<Map> mapList,AssTopo assTopo){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return mapList;
		}
		filterMapList(objList,mapList);
		return mapList;
	}
	private static List<Map> filterMapList(List<Object> objList,List<Map> mapList){
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return mapList;
		}
		List<Map> removeMaps=new ArrayList<Map>();
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type"))) {
					Object nodeIdObject=map.get("source");
					if (!GlobalUtil.isNullOrEmpty(objList)) {
						for (Map map2 : mapList) {
							if (nodeIdObject.equals(map2.get("id"))
									&& mapList.contains(map2)) {
								removeMaps.add(map2);
							}
						}
					}
					
				}
			}
		}
		if (!GlobalUtil.isNullOrEmpty(removeMaps)) {
			mapList.removeAll(removeMaps);
		}
		return mapList;
	}
	public static List<Map> showGroupListByAssGroup(AssTopo assTopo,String nodeId){
		List<Object> objList=getTopoConfigObjects(assTopo);
		if (GlobalUtil.isNullOrEmpty(objList)) {
			return null;
		}
		List<Map> mapList=null;
		if (groupTopoNodeIdGroupListMap.containsKey(nodeId+assTopo.getId())) {
			mapList=groupTopoNodeIdGroupListMap.get(nodeId+assTopo.getId());
		}else {
			List<String>nodeIdList=getSourceNodeIdList(objList,nodeId);
			nodeIdList=getGroupNodeList(objList,nodeIdList,true);
			mapList=getMapsByNodeIds(objList,nodeIdList);
			groupTopoNodeIdGroupListMap.put(nodeId+assTopo.getId(), mapList);
		}
		return mapList;
	}
	private static List<String> getSourceNodeIdList(List<Object> objList,String nodeId){
		if (GlobalUtil.isNullOrEmpty(objList)||GlobalUtil.isNullOrEmpty(nodeId)) {
			return null;
		}
		List<String>nodeIdList=new ArrayList<String>();
		for (Object object : objList) {
			if (!GlobalUtil.isNullOrEmpty(object) && object instanceof Map) {
				Map map=(Map)object;
				if ("实线".equals(map.get("type"))
						&& nodeId.equals(map.get("destination"))) {
					Object nodeIdObject=map.get("source");
					if (!GlobalUtil.isNullOrEmpty(nodeIdObject)
							&& !nodeIdList.contains(nodeIdObject)) {
						nodeIdList.add(nodeIdObject.toString());
					}
				}
			}
		}
		return nodeIdList;
	}
	
	public static String getIpSqlParmScope(List<String> ipList){
		StringBuffer stringBuffer=new StringBuffer("(");
		if (!GlobalUtil.isNullOrEmpty(ipList)) {
			for (String string : ipList) {
				stringBuffer.append(" '").append(string).append("' ,");
			}
		}else {
			return "";
		}
		String string =stringBuffer.substring(0, stringBuffer.length()-1);
		return string+")";
	}
	
	//hql
	public static String getExtendSqlParm(List<String> ipList){
		StringBuffer stringBuffer=new StringBuffer("and(");
		if (!GlobalUtil.isNullOrEmpty(ipList)) {
			for (String string : ipList) {
				stringBuffer.append(" assevt.dvcAddress='")
				.append(string).append("' or")
				.append(" assevt.srcAddress='")
				.append(string).append("' or")
				.append(" assevt.destAddress='")
				.append(string).append("' or");
			}
		}else {
			return "";
		}
		String string =stringBuffer.substring(0, stringBuffer.length()-2);
		return string+")";
	}
	//TABLE sql
	public static String getIpSqlParm(List<String> ipList){
		StringBuffer stringBuffer=new StringBuffer("AND(");
		if (!GlobalUtil.isNullOrEmpty(ipList)) {
			for (String string : ipList) {
				stringBuffer.append(" DVC_ADDRESS='")
				.append(string).append("' OR");
			}
		}else {
			return "";
		}
		String string =stringBuffer.substring(0, stringBuffer.length()-2);
		return string+")";
	}
	
	public static TopoUtil getInstance() {
		if (null==topoUtil) {
			topoUtil=new TopoUtil();
		}
		return topoUtil;
	}
	private TopoUtil() {
	}
	public static Map getAssetEvtMstMap(){
		return prop;
	}
}
