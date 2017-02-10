package com.topsec.tsm.sim.asset.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.AssetNameExistException;
import com.topsec.tsm.ass.InvalidAssetIdException;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.persistence.AssGroup;
import com.topsec.tsm.ass.persistence.AssetType;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.persistence.OsPlatform;
import com.topsec.tsm.ass.service.AssGroupService;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.component.handler.MonitorState;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetAttribute;
import com.topsec.tsm.sim.asset.AssetCategory;
import com.topsec.tsm.sim.asset.AssetCategoryUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil;
import com.topsec.tsm.sim.asset.ExcelOperaterManager;
import com.topsec.tsm.sim.asset.ExcelOperaterPOI;
import com.topsec.tsm.sim.asset.IpComparator;
import com.topsec.tsm.sim.asset.exception.AssetException;
import com.topsec.tsm.sim.asset.exception.InvalidLicenseException;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.sim.asset.group.AllInOneGroupStrategy;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.group.GroupByAssetVender;
import com.topsec.tsm.sim.asset.group.GroupStrategy;
import com.topsec.tsm.sim.asset.group.GroupStrategyFactory;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.ErrorMark;
import com.topsec.tsm.sim.common.bean.ImportResult;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.tree.DefaultTreeIterator;
import com.topsec.tsm.sim.common.tree.FastJsonResult;
import com.topsec.tsm.sim.common.tree.FastJsonTreeVisitor;
import com.topsec.tsm.sim.common.tree.VisitResultListener;
import com.topsec.tsm.sim.common.web.IgnoreSecurityCheck;
import com.topsec.tsm.sim.common.web.NotCheck;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.kb.Leak;
import com.topsec.tsm.sim.leak.service.LeakService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.util.TopoUtil;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.LicenceServiceUtil;
import com.topsec.tsm.sim.util.NodeUtil;

@Controller
@RequestMapping("assetlist")
public class AssetListController {
	
	private static final int MAX_SCAN_HOST_COUNT = 500;
	private static final Logger logger = LoggerFactory.getLogger(AssetListController.class) ;
	private NodeMgrFacade nodeMgr ;
	private DiscoveredAssetManager dcvAssetManager = DiscoveredAssetManager.getInstance() ;
	@Autowired
	LeakService leakService;
	public AssetListController() {
	}

	@Autowired
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgr = nodeMgr;
	}

	@RequestMapping("getAssetTree")
	@ResponseBody
	public Object getAssetTree(SID sid){
		try {
			SID.setCurrentUser(sid) ;
			GroupStrategy strategy = new GroupByAssetCategory(new GroupByAssetVender()) ;
			AssetGroup root = AssetFacade.getInstance().groupByWithRoot(strategy) ;
			DefaultTreeIterator iterator = new DefaultTreeIterator() ;
			iterator.regist(new VisitResultListener<FastJsonResult,AssetGroup>() {
				@Override
				public void onResult(FastJsonResult result, AssetGroup group) {
					if(group.getLevel() == 1){
						return ;
					}
					if(group.getLevel() == 2){//第一级为虚拟root
						result.put("iconCls", AssetUtil.getBigIconClsByDeviceType(group.getId())) ;
					}else if(group.getLevel() == 3){
						result.put("iconCls", "icon-none") ;
					}
					if(group.isLeaf()){
						JSONArray child = FastJsonUtil.toJSONArray(group.getAllAssets(), "ip=id","name=text","deviceType=type","id=resid","scanNodeId=nodeid") ;
						JSONObject attr = new JSONObject();
						attr.put("isAsset", true) ;
						FastJsonUtil.put(child, "attributes", attr) ;
						FastJsonUtil.put(child, "iconCls",  AssetUtil.getBigIconClsByDeviceType(group.getParent().getId())) ;
						result.put("children", child) ;
					}else{
						result.put("state", "closed") ;
					}
				}
			}) ;
			FastJsonResult result = (FastJsonResult) iterator.iterate(root, new FastJsonTreeVisitor("id","name=text")) ;
			Object childrenObj = result.get("children") ;
			return childrenObj==null ? new JSONArray() : childrenObj ;
		}finally{
			SID.removeCurrentUser() ;
		}
	}
	/**
	 * 拓扑图树形菜单
	 * @return
	 */
	@RequestMapping("getTopoAssetTree")
	@ResponseBody
	public Object getTopoAssetTree(SID sid){
		try {
			SID.setCurrentUser(sid) ;
			GroupStrategy strategy = new GroupByAssetCategory(new GroupByAssetVender());
			AssetGroup root = AssetFacade.getInstance().groupByWithRoot(strategy);
			DefaultTreeIterator iterator = new DefaultTreeIterator();
			iterator.regist(new VisitResultListener<FastJsonResult, AssetGroup>() {
				@Override
				public void onResult(FastJsonResult result, AssetGroup group) {
					if (group.getLevel() == 2) {//第一级为虚拟root
						result.put("iconCls", AssetUtil.getBigIconClsByDeviceType(group.getId()));
					}
					if (group.isLeaf()) {
						result.put("children", toJSON(group.getAllAssets()));
					}
				}
			});
			FastJsonResult result = (FastJsonResult) iterator.iterate(root, new FastJsonTreeVisitor("id", "name=text"));
			return result.get("children");
		} finally{
			SID.removeCurrentUser() ;
		}
	}
	@RequestMapping("topoList")
	@ResponseBody
	public Object topoList(@RequestParam("userId")String userId,@RequestParam(value="includeSystemTopo",defaultValue="false")boolean includeSystemTopo,
			               HttpServletRequest request,final SID sid) {
		try{
			SID.setCurrentUser(sid) ;
			TopoService service = (TopoService) SpringWebUtil.getBean("topoService", request) ;
			List<AssTopo> all = new ArrayList<AssTopo>() ;
			if(includeSystemTopo){
				all.add(service.getSystemTopo(nodeMgr)) ;
			}
			List<AssTopo> userTopo = service.getUserTopoList(sid.isOperator() ? null : sid.getUserName()) ;
			if (userTopo != null) {
				all.addAll(userTopo) ;
			}
			JSONArray datas = FastJsonUtil.toJSONArray(all,new JSONConverterCallBack<AssTopo>(){
				@Override
				public void call(JSONObject result, AssTopo topo) {
					result.put("selected", topo.getId().equals(sid.getDefaultTopoId())) ;
				}
			}, "id","name","config","styleProperty","type") ;
			return datas ;
		}finally{
			SID.removeCurrentUser() ;
		}
	}

	@RequestMapping("deleteTopo")
	@ResponseBody
	public Object deleteTopo(@RequestParam("id")Integer id,HttpServletRequest request) {
		Result result = new Result() ;
		TopoService topoService = (TopoService)SpringWebUtil.getBean("topoService", request) ;
		try {
			topoService.delete(id) ;
			result.buildSuccess(id) ;
		} catch (Exception e) {
			logger.error("删除拓扑图失败!",e) ;
			result.buildError("删除拓扑图失败,系统内部错误！") ;
		}
		return result ;
	}
	
	@RequestMapping("changeState")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object changeState(@RequestParam("id")String id,@RequestParam("state")Integer state,HttpServletRequest request,SID sid) {
		try {
			return changeAssetsStateById(id, state, request, sid) ;
		} catch(InvalidAssetIdException e){
			return new Result().buildError("资产已经被删除") ;
		} catch (InvalidLicenseException e) {
			return new Result().buildError("无效的License文件！") ;
		} catch (LimitedNumException e) {
			return new Result(false,"启用的资产已达License上限！");
		}catch(AssetException e){
			return new Result(false,e.getMessage()) ;
		} catch (Exception e) {
			logger.error("资产状态修改失败!",e) ;
			return new Result().buildError("系统内部错误!") ;
		}
	}
	@RequestMapping("changeAssetsState")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object changeAssetsState(@RequestParam("ids")String[] ids,@RequestParam("state")Integer state,HttpServletRequest request,SID sid) {
		Result result = new Result(true,null) ;
		if(ObjectUtils.isEmpty(ids)){
			return result ;
		}
		for(String id:ids){
			if(StringUtil.isBlank(id))continue ;
			Result changeResult;
			try {
				changeResult = changeAssetsStateById(id, state, request, sid);
				if(!changeResult.isSuccess()){
					result.buildError(changeResult.getMessage()) ;
					break ;
				}
			} catch(InvalidAssetIdException e){
				continue ;
			} catch (InvalidLicenseException e) {
				result.buildError("无效的License文件！") ;
			} catch (LimitedNumException e) {
				result.buildError("启用的资产已达License上限！") ;
			} catch (AssetException e) {
				result.buildError(e.getMessage()) ;
			}
		}
		return result ;
	}
	
	private Result changeAssetsStateById(String id,Integer state,HttpServletRequest request,SID sid) throws InvalidLicenseException, LimitedNumException, AssetException{
		AssetObject ao = AssetFacade.getInstance().getById(id) ;
		if(ao == null){
			return new Result(false, "无效的资产信息！") ;
		}
		if(state == 1){//如果是启用资产，需要检测license数量
			AssetService assetService = (AssetService) SpringWebUtil.getBean("assetService", request) ;
			if(ao.getEnabled() != 1){//当前不是启用状态
				checkLicenseLimit(assetService.getEnabledTotal()) ;
			}
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			if(nodeMgrFacade.getNodeByNodeId(ao.getScanNodeId()) == null){
				throw new AssetException(ao.getName()+"的管理节点已删除，请指定其它管理节点，再启用！") ;
			}
		}
		boolean cascade = StringUtil.booleanVal(request.getParameter("cascade")) || state == 0 ;
		try {
			ao = AssetFacade.getInstance().changeState(id,state,cascade);
			if(state == 0){
				AuditLogFacade.stop("禁用资产", sid.getUserName(), "禁用资产"+ao.getName(), new IpAddress(sid.getLoginIP())) ;
			}else{
				AuditLogFacade.start("启用资产", sid.getUserName(), "启用资产"+ao.getName(), new IpAddress(sid.getLoginIP())) ;
			}
		}catch (InvalidAssetIdException e) {
			logger.warn("无效的资产id:"+e.getId()) ;
		}
		JSONObject result = new JSONObject() ;
		result.put("id", id) ;
		result.put("state", state) ;
		return new Result().buildSuccess(result) ;
		
	}
	
	/**
	 * 根据id获得逻辑拓扑图对象
	 * @param resourceId
	 * @param request
	 * @return
	 */
	@RequestMapping("getTopo")
	@ResponseBody
	public Object getTopo(@RequestParam("id")Integer resourceId,HttpServletRequest request) {
		TopoService service = (TopoService) SpringWebUtil.getBean("topoService", request) ;
		AssTopo topo = service.get(resourceId) ;
		if (topo != null) {
			return FastJsonUtil.toJSON(topo, "id","name","config=topoData") ;
		}else{
			return new JSONObject(0);
		}
	}
	
	/**
	 * 保存或更新逻辑拓扑
	 * @param id拓扑id
	 * @param topoData拓扑数据
	 * @return
	 */
	@RequestMapping("saveTopo")
	@ResponseBody
	@NotCheck(properties = { "topoData" })
	public Object saveTopo(@RequestParam(value="id",required=false)Integer id,
			               @RequestParam("topoData")String topoData,
			               HttpServletRequest request,
			               SID sid) {
		Result result = new Result() ;
		try {
			AssTopo at = new AssTopo(id,request.getParameter("name"),topoData) ;
			at.setStyleProperty(request.getParameter("styleProperty")) ;
			at.setOwner(sid.getUserName()) ;
			TopoService topoService = (TopoService) SpringWebUtil.getBean("topoService", request) ;
			topoService.saveOrUpdate(at) ;
			if(id != null){
				TopoUtil.reLoad(at) ;
			}
			result.buildSuccess(at.getId()) ;
		}catch (ResourceNameExistException e) {
			result.buildError("名称已经存在！") ;
		} catch (Exception e) {
			logger.error("AssetListController.saveTopo exception",e) ;
			result.buildError("保存失败，系统内部错误！") ;
		}
		return result ;
	}

	private JSONArray toJSON(List<AssetObject> assets){
		return FastJsonUtil.toJSONArray(assets,new JSONConverterCallBack<AssetObject>() {
			@Override
			public void call(JSONObject result, AssetObject asset) {
				result.put("type", "asset") ;
				result.put("icon", AssetUtil.getIcon48(asset.getDeviceType())) ;
			}
		},"id","name=text","ip") ;
	}
	/**
	 * 
	 *  @param pageIndex
	 *  @param pageSize
	 *  @param ip
	 *  @param name
	 *  @param deviceType
	 *  @param osName
	 *  @return
	 */
	@RequestMapping(value="assetGrid",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public String assetGrid(@RequestParam Map<String,Object> searchCondition,SID sid){
		int pageIndex = StringUtil.toInt((String)searchCondition.get("page"),1);
		int pageSize = StringUtil.toInt((String)searchCondition.get("rows"), 20) ;
		JSONObject result = new JSONObject();
		try {
			SID.setCurrentUser(sid) ;
			PageBean<AssetObject> pager = AssetFacade.getInstance().getPage(pageIndex, pageSize, searchCondition) ;
			result.put("total", pager.getTotal()) ;
			List<AssetObject> assets = pager.getData() ;
			JSONArray assetsJson = new JSONArray();
			result.put("rows", assetsJson) ;
			for (AssetObject assetObject : assets) {
				JSONObject assetJson = FastJsonUtil.toJSON(assetObject, "id","ip","name","hostName","os.osName=osName","deviceType","safeRank","linkman","creator");
				FastJsonUtil.mergeToJSON(assetJson, assetObject,"enabled=available","logCount") ;
				assetJson.put("osIconCls", AssetUtil.getIconClsByOS(assetObject.getOs())) ;
				assetJson.put("assetIconCls", AssetUtil.getIconClsByDeviceType(assetObject.getDeviceType())) ;
				Node node = nodeMgr.getNodeByNodeId(assetObject.getScanNodeId()) ;
				assetJson.put("nodeName", node == null ? "节点已被删除" : node.getIp()) ;
				assetJson.put("deviceTypeName", DeviceTypeShortKeyUtil.getInstance().deviceTypeToCN(assetObject.getDeviceType(),"/")) ;
				AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(assetObject.getDeviceType()) ; 
				assetJson.put("tools", category != null ? category.getTools() : Collections.emptyList()) ;
				assetsJson.add(assetJson) ;
			}
		} catch (Exception e) {
			logger.error("查询资产信息出错",e) ;
		}finally{
			SID.removeCurrentUser() ;
		}
		return result.toString() ;
	}
	/**
	 * 资产卡片视图
	 * @param sid
	 * @return
	 */
	@RequestMapping("assetCard")
	@ResponseBody
	public Object assetCard(SID sid,@RequestParam("groupAlias")String groupAlias) {
		JSONArray result = new JSONArray() ;
		try{
			SID.setCurrentUser(sid) ;
			GroupStrategy groupStrategy = GroupStrategyFactory.getGroupStrategy(groupAlias) ;
			if(groupStrategy == null){
				groupStrategy = new AllInOneGroupStrategy() ;
			}
			List<AssetGroup> assetGroups = AssetFacade.getInstance().groupBy(groupStrategy) ;
			for(AssetGroup group:assetGroups){
				JSONArray assetsJSON = new JSONArray(group.getAssets().size()) ;
				List<AssetObject> assets = group.getAssets() ;
				for (AssetObject assetObject : assets) {
					JSONObject assetJson = FastJsonUtil.toJSON(assetObject, "id","ip","name","os.osName=osName","deviceType","enabled=available");
					assetJson.put("osIconCls", AssetUtil.getIconClsByOS(assetObject.getOs())) ;
					assetJson.put("deviceTypeIcon", AssetUtil.getIcon48(assetObject.getDeviceType())) ;
					assetJson.put("deviceTypeName", assetObject.getDeviceTypeName()) ;
					assetJson.put("stateIconCls", AssetUtil.getStateIconCls(assetObject.getState())) ;
					assetJson.put("stateText", assetObject.getState().toString()) ;
					assetJson.put("eventCount", assetObject.getEventCount()) ;
					assetJson.put("logCount", assetObject.getLogCount()) ;
					assetsJSON.add(assetJson) ;
				}
				JSONObject groupJSON = FastJsonUtil.toJSON(group, "id","name") ; 
				groupJSON.put("assets", assetsJSON) ;
				result.add(groupJSON) ;
			}
		}catch(Exception e){
			logger.error("查询资产信息出错！",e) ;
		}finally{
			SID.removeCurrentUser() ;
		}
		return result ;
	}
	
	/**
	 * 进程查询
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping("processCount")
	@ResponseBody
	public Object processCount(@RequestParam("ip")String ip, HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		Object process = null;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "process") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					process = data.get("DVC_COMMONINF") ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!");
		}
		return process;
	}
	
	/**
	 * 服务查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("win32ServiceCount")
	@ResponseBody
	public Object win32ServiceCount(@RequestParam("ip")String ip, HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		Object win32Service = null;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "Win32Service") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					win32Service = data.get("DVC_COMMONINF") ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!");
		}
		return win32Service;
	}
	
	/**
	 * 端口、服务、版本查询
	 * @param ip
	 * @param tabSeq
	 * @param request
	 * @return
	 */
	@RequestMapping("portCount")
	@ResponseBody
	public Object portCount(@RequestParam("ip")String ip, HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request) ;
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false) ;
		Object services = null;
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
			SimDatasource monitor = monitorService.getFirstByIp(ip) ; 
			if (monitor != null) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "service") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 15*60*1000) ;
				Map<String,Object> data = (Map<String, Object>) ste.getData() ;
				if(data != null){
					services = data.get("DEVICE_SERVICES") ;
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!");
		}
		return services;
	}
	
	@RequestMapping("leakCount")
	@ResponseBody
	public Object leakCount(@RequestParam("ip")String ip, HttpServletRequest request){
		NodeMgrFacade nodeFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade",request);
		Result result = new Result() ;
		Node auditor = nodeFacade.getKernelAuditor(false);
		try {
			DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request);
			SimDatasource monitor = monitorService.getFirstByIp(ip); 
			if (monitor != null && monitor.getAvailable() != 0) {
				MonitorState state = new MonitorState(ip, monitor.getSecurityObjectType(), "service") ;
				MonitorState ste = (MonitorState) NodeUtil.dispatchCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_SEARCH_STATESTATISTIC, state, 60*1000);
				Map<String,Object> data = (Map<String, Object>) ste.getData();
				result.setResult(0) ;
				if(data != null){
					List<Map<String,Object>> services = (List<Map<String, Object>>) data.get("DEVICE_SERVICES");
					if (services != null) {
						Set<String> cpes = new HashSet<String>() ;
						Set<Leak> leaks = new HashSet<Leak>() ;
						for(Map<String,Object> entry:services){
							cpes.addAll(StringUtil.splitAsList((String)entry.get("CPE"))) ;
						}
						for(String cpe:cpes){
							leaks.addAll(leakService.getByCpe(cpe));
						}
						result.setResult(leaks.size()) ;
					}
				}
			}
		} catch (CommunicationException e) {
			result.buildError("请求数据超时!");
		}
		return result;
	}
	
	/**
	 * 获取资产接口
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping("getInterface")
	@ResponseBody
	public Object getInterface(@RequestParam("ip")String ip, HttpServletRequest request){
		JSONObject jo = new JSONObject();
		AssetObject asset =  AssetFacade.getInstance().getAssetByIp(ip);
		if (asset != null) {
			AssetCategory category = AssetCategoryUtil.getInstance().getCategoryByPathId(asset.getDeviceType());
			if(category != null ){
				AssetAttribute attr = category.getAttribute("interface");//接口属性
				jo.put("interfaces", attr);
			}
		}
		return jo;
	}
	
	/**
	 * 展示资产更多信息
	 * @param ip
	 * @param request
	 * @return
	 */
	@RequestMapping("more")
	@ResponseBody
	public Object more(@RequestParam("ip")String ip,HttpServletRequest request) {
		DataSourceService dss = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request) ;
		List<SimDatasource> dataSources = dss.getByIp(ip) ;
		DataSourceService ms = (DataSourceService) SpringWebUtil.getBean("monitorService", request) ;
		List<SimDatasource> monitors = ms.getByIp(ip) ;
		List<SimDatasource> all = new ArrayList<SimDatasource>();
		if (dataSources != null) {
			all.addAll(dataSources) ;
		}
		if (monitors != null) {
			all.addAll(monitors) ;
		}
		JSONArray result = FastJsonUtil.toJSONArray(all, new JSONConverterCallBack<SimDatasource>() {
			public void call(JSONObject result, SimDatasource obj) {
				result.put("group", SimDatasource.DATASOURCE_TYPE_LOG.equals(obj.getOwnGroup()) ? "日志源" : "监视对象") ;
				result.put("deviceTypeName",DeviceTypeNameUtil.getDeviceTypeName(obj.getSecurityObjectType(),Locale.getDefault()));
			}
		}, "resourceName=name","collectMethod","available") ;
		return result ;
	}
	/**
	 * 资产状态信息(CPU,内存,磁盘,链接数) for topo
	 * 用于拓扑图
	 * @param status
	 * @return
	 */
	@RequestMapping("topoAssetStatus")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object topoAssetStatus(@RequestParam("ids") String ids,HttpServletRequest request,SID sid) {
		if (sid == null) {
			return new JSONArray() ;
		}
		BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("deviceId") ;
		Collection<String> userDeviceIds = (Collection<String>) CollectionUtils.collect(sid.getUserDevice(),trans) ;
		String[] idArray = StringUtil.split(ids);
		JSONArray assetsJSON;
		assetsJSON = new JSONArray(idArray.length);
		for(String id:idArray){
			JSONObject assetJSON = getStatusJSON(id) ;
			assetJSON.put("accessed",sid.getUserName().equals(assetJSON.get("creator")) || 
									 sid.isOperator()||(sid.hasOperatorRole()&&userDeviceIds.contains(id))) ;
			assetsJSON.add(assetJSON) ;
		}
		return assetsJSON ;
	}	
	private JSONObject getStatusJSON(String assetId){
		AssetObject ao = AssetFacade.getInstance().getById(assetId) ;
		if(ao == null){
			JSONObject notFoundAsset = new JSONObject() ;
			notFoundAsset.put("id", assetId) ;
			notFoundAsset.put("isDelete", true) ;
			return notFoundAsset ;
		}else{
			JSONObject statusJSON = getStatusJSON(ao) ;
			statusJSON.put("isDelete", false) ;
			return statusJSON ;
		}
	}
	private JSONObject getStatusJSON(AssetObject asset){
		if(asset.getEnabled() == 0){
			JSONObject disabledAsset = new JSONObject() ;
			disabledAsset.put("id", asset.getId()) ;
			disabledAsset.put("isEnabled", false) ;
			return disabledAsset ;
		}else{
			JSONObject assetJSONData = FastJsonUtil.toJSON(asset, "id","name","alarmCount","eventCount","logCount","ip","creator") ;
			assetJSONData.put("stateIcon", AssetUtil.getStateIcon(asset.getState())) ;
			assetJSONData.put("stateText", asset.getState().toString()) ;
			assetJSONData.put("state", asset.getState().name()) ;
			assetJSONData.put("isEnabled", true) ;
			assetJSONData.put("deviceTypeName",asset.getDeviceTypeName()) ;
			DataSourceService dataSourceService = (DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService") ;
			List<SimDatasource> dataSources = dataSourceService.getByIp(asset.getMasterIp().toString()) ;
			assetJSONData.put("dataSources", FastJsonUtil.toJSONArray(dataSources, "securityObjectType","resourceId=id","resourceName=name","auditorNodeId=nodeId","deviceIp")) ;
			return assetJSONData ;
		}
	}
	
	/**
	 * 资产导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("exportAssetExcel")
	public void exportAssetExcel(SID sid,HttpServletRequest request, HttpServletResponse response) {
		//查询资产集合
		List<AssetObject> assetList ;
		SID.setCurrentUser(sid) ;
		try{
			assetList = AssetFacade.getInstance().getAll();
			Collections.sort(assetList, IpComparator.getInstance()) ;
		}finally{
			SID.removeCurrentUser() ;
		}
		String []column = {"序号(*必填)", "IP(*必填)", "资产名称(*必填)", "资产类型父级(*必填)", "资产类型子级(*必填)", "管理节点(*必填)", "主机名称", "操作系统", "安全等级", null,null,null,null, "联系人", "描述"};
		
		// 创建新的Excel工作簿
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		// 在Excel工作簿中建一工作表，其名为“Asset设备导出信息”
		HSSFSheet sheet = workbook.createSheet("资产信息");
		//样式
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		
		//产生表格标题行
		HSSFRow row = sheet.createRow(0);
		for (int j = 0; j < column.length; j++) {
			HSSFCell cell = row.createCell(j);
			if(column[j] == null){
				sheet.setColumnHidden(j, true) ;
				continue ;
			}
			sheet.setColumnWidth(j, 22*256) ;
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(column[j]);
		}
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		//循环资产集合，开始输入数据
		for (int i = 0; i < assetList.size(); i++) {
			AssetObject ao = assetList.get(i);
			row = sheet.createRow(i + 1);
			for (int j = 0; j < column.length; j++) {
				HSSFCell cell = row.createCell(j);
				switch (j) {
					case 0:
						cell.setCellValue(i + 1);
						cell.setCellStyle(cellStyle);
						continue ;
					case 1:
						cell.setCellValue(ao.getIp());
						continue ;
					case 2:
						cell.setCellValue(ao.getName());
						continue ;
					case 3:
						String deviceType = ao.getDeviceType().split("/")[0] ;
						cell.setCellValue(DeviceTypeShortKeyUtil.getInstance().getShortZhCN(deviceType)+"_"+deviceType);
						continue ;
					case 4:
						String vendor = ao.getDeviceType().split("/")[1];
						cell.setCellValue(DeviceTypeShortKeyUtil.getInstance().getShortZhCN(vendor)+"_"+vendor);
						continue ;
					case 5:
						Node node = nodeMgrFacade.getNodeByNodeId(ao.getScanNodeId()) ;
						cell.setCellValue(node != null ? node.getIp() : "");
						continue ;
					case 6:
						cell.setCellValue(ao.getHostName());
						continue ;
					case 7:
						cell.setCellValue(ao.getOs() != null ? ao.getOs().getOsName() : "");
						continue ;
					case 8:
						cell.setCellValue(ao.getSafeRank());
						continue ;
					case 9:
						cell.setCellValue(ao.getAssGroup().getGroupName());
						continue ;
					case 10:
					case 11:
					case 12:continue ;	
					case 13:
						cell.setCellValue(StringUtil.nvl(ao.getLinkman()));
						continue ;
					case 14:
						cell.setCellValue("");
						continue ;
					default:
						cell.setCellValue("");
						continue ;
				}
			}
		}
	    
		//响应excel数据
		//response.setContentType("application/vnd.ms-excel");
		String userAgent = request.getHeader("User-Agent") ;
		String fileName = "资产信息.xls";
		if(userAgent.indexOf("Firefox") > 0){
			response.setHeader("Content-Disposition", "attachment; filename*=\"utf8' '" + StringUtil.encode(fileName,"UTF-8") + "\"");
		}else{
			response.addHeader("Content-Disposition", "attachment; filename=\"" + StringUtil.encode(fileName,"UTF-8")+"\"");
		}
		try {
			workbook.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("assetForm")
	@IgnoreSecurityCheck
	public String assetForm(SID sid,@RequestParam("operation")String operation,HttpServletRequest request) {
		if("add".equals(operation)){
		}else if("edit".equals(operation)){
			AssetObject ao = AssetFacade.getInstance().getById(request.getParameter("id")) ;
			request.setAttribute("id", request.getParameter("id")) ;
			request.setAttribute("asset", ao) ;
			request.setAttribute("accountPassword", CommonUtils.encrypt(sid,StringUtil.decrypt(ao.getAccountPassword()))) ;
		}
		return "page/asset/asset_form" ;
	}
	@RequestMapping(value="checkUniqueIp", produces="text/javascript; charset=utf-8")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object checkUniqueIp(@RequestParam("ip") String ip, @RequestParam("id") String id, HttpServletRequest request) {

		JSONObject result = new JSONObject();
		
		if("127.0.0.1".equals(ip)){
			result.put("error", "资产IP不能为127.0.0.1！") ;
			return result ;
		}

		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
		Device ipDevice = deviceService.getDeviceByIp(ip) ;

		if(ipDevice == null || ipDevice.getId().equals(id)){
			result.put("ok","");
		}else {
			result.put("error","IP地址" + ip + "资产已经存在");
		}
		return result;
	}
	
	/**
	 * ajax验证资产名称是否存在
	 * @param name
	 * @param id
	 * @return
	 */
	@RequestMapping(value="checkUniqueName", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object checkUniqueName(@RequestParam("name") String name, @RequestParam("id") String id, HttpServletRequest request) {
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
		Device device = deviceService.getDeviceByName(name);
		boolean isUpdate = false;
		if (null != id && !"".equals(id)) {
			isUpdate = true;
		}
		JSONObject result = new JSONObject();
		if(isUpdate) {
			if (device != null && !id.equals(String.valueOf(device.getId()))) {
				result.put("error", "该名称已存在");
			} 
		} else {
			if (device != null) {
				result.put("error", "该名称已存在");
			}
		}
		
		return result;
	}
	
	@RequestMapping("saveAsset")
	@ResponseBody
	@NotCheck(properties={"id"})
	public Object saveAsset(@RequestParam("operation")String operation,HttpServletRequest request,SID sid) {
		if("add".equals(operation)){
			return addAsset(request,sid) ;
		}else if("edit".equals(operation)){
			return updateAsset(request,sid) ;  
		}
		return new Result().buildError("无效的参数！");
	} 
	/**
	 * 获取资产状态
	 * @param ip
	 * @return
	 */
	@RequestMapping("assetEnabled")
	@ResponseBody
	public Object assetEnabled(@RequestParam("ip")String ip) {
		AssetObject device = AssetFacade.getInstance().getAssetByIp(ip) ;
		JSONObject enabled = new JSONObject() ;
		if(device == null){
			enabled.put("deleted", true) ;
		}else{
			enabled.put("enabled", device.getEnabled()) ;
			enabled.put("deleted", false) ;
		}
		return enabled ;
	}
	
	/**
	 * 增加资产
	 * @param request
	 * @return
	 */
	private Object addAsset(HttpServletRequest request,SID sid) {
		String ip = request.getParameter("ip") ;
		Result result = new Result() ;
		try{
			SID.setCurrentUser(sid) ;
			if("127.0.0.1".equals(ip)){
				throw new AssetException("资产IP不能为127.0.0.1！") ;
			}
			DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
			if(deviceService.getDeviceByIp(ip)==null){
				Device device = new Device() ;
				parseDeviceInfo(request,device) ;
				if(device.getEnabled() == 1){//如果是启用状态的资产，需要检测license限制
					int total=deviceService.getEnabledTotal();
					checkLicenseLimit(total);
				}
				device.setCreator(sid.getUserName()) ;
				deviceService.save(device);
				AuditLogFacade.addSuccess("添加资产",sid.getUserName(), "添加资产:"+device.getName(), new IpAddress(sid.getLoginIP())) ;
				result.build(true) ;
			}else{
				result.buildError("IP地址"+ip+"资产已经存在！") ;
			}
		}catch(AssetException e){
			result.buildError(e.getMessage()) ;
		}catch(AssetNameExistException e){
			result.build(false,"资产名称已经存在！") ;
		}catch (InvalidLicenseException e) {
			result.build(false,"当前License无效！") ;
		}catch (LimitedNumException e) {
			result.build(false, "启用的资产已达License上限！") ;
		}catch (Exception e){
			logger.error("添加资产出错！",e) ;
			result.build(false, "系统内部错误，请与管理员联系！") ;
		}finally{
			SID.removeCurrentUser() ;
		}
		return result ;
	}
	/**
	 * 解析request中的资产信息
	 * @param request
	 * @return
	 */
	private void parseDeviceInfo(HttpServletRequest request,Device device){
		//device.setHost(device);
		OsPlatform os = new OsPlatform();
		os.setOsName(request.getParameter("osName"));
		device.setOs(os);
		String nodeId = request.getParameter("nodeId") ;
		Node node = nodeMgr.getNodeByNodeId(nodeId) ;
		if(node == null){
			nodeId = nodeMgr.getKernelAuditor(false).getNodeId() ;
		}
		device.setScanNodeId(nodeId) ;
		device.setEnabled(StringUtil.toInt(request.getParameter("state"), 0)) ;
		device.setName(StringUtil.trim(request.getParameter("name")));
		device.setHostName(request.getParameter("hostName")) ;
		device.setAlias(request.getParameter("alias"));
		device.setAssetType(AssetType.DEVICE);
		device.setMasterIp(new IpAddress(request.getParameter("ip")));
		device.setMac(request.getParameter("mac")) ;
		device.setDeviceType(request.getParameter("deviceType"));
		device.setDeviceTypeName(DeviceTypeShortKeyUtil.getInstance().deviceTypeToCN(request.getParameter("deviceType"),"/"));
		device.setManufacturer(request.getParameter("manufacturer"));
		device.setLocation(request.getParameter("location"));
		device.setLinkman(request.getParameter("linkman")) ;
		device.setServices(request.getParameter("services")) ;
		device.setNote(request.getParameter("note")) ;
		device.setAccountName(request.getParameter("accountName")) ;
		device.setAccountPassword(StringUtil.encrypt(CommonUtils.decrypt(request.getParameter("accountPassword")))) ;
		device.setIsDelete("false");
		device.setSafeRank(request.getParameter("safeRank")) ;
		int groupId = StringUtil.toInt(request.getParameter("group"),-1) ;
		device.setAssGroup(new AssGroup(groupId));
	}
	private Object updateAsset(HttpServletRequest request,SID sid){
		String id = request.getParameter("id") ;
		String ip = request.getParameter("ip") ;
		Result result = new Result() ;
		try{
			AssetService assetService = (AssetService) SpringWebUtil.getBean("assetService", request);
			Device ipDevice = assetService.getDeviceByIp(ip) ;
			Device dbDevice = assetService.getDevice(id) ;
			if(ipDevice != null && dbDevice != null && ipDevice.equals(dbDevice)){//同一ip地址的资产已经存在
				String deviceNamePre = dbDevice.getName();
				Integer oldState = dbDevice.getEnabled() ;
				parseDeviceInfo(request,dbDevice) ;
				if(oldState != 1 && dbDevice.getEnabled() == 1){//如果旧的状态为禁用，新状态为启用，需要检测license数
					int total=assetService.getEnabledTotal() ;
					checkLicenseLimit(total);
				}
				dbDevice.setLastModifiedTime(new Date()) ;
				dbDevice.setLastModifier(sid.getUserName()) ;
				assetService.update(dbDevice);
				AssetFacade.getInstance().updateAsset(id,dbDevice.getMasterIp().toString()) ;//更新内存中的资产信息
				if(!deviceNamePre.equals(dbDevice.getName())){
					deviceNamePre += " 更新为 " + dbDevice.getName();
				}
				AuditLogFacade.updateSuccess("更新资产",sid.getUserName(), "更新资产:" + deviceNamePre, new IpAddress(sid.getLoginIP())) ;
				result.build(true) ;
			}else{
				result.buildError("IP地址"+ip+"资产已经存在") ;
			}
		}catch(AssetNameExistException e){
			result.buildError("资产名称已经存在！") ;
		}catch(InvalidLicenseException e){
			result.buildError("无效的license文件！");
		}catch(LimitedNumException e){
			result.buildError("启用的资产已达License上限！") ;
		}catch (Exception e){
			logger.error("更新资产出错！",e) ;
			result.buildError("系统内部错误！") ;
		}
		return result ;
	}
	/**
	 * 资产发现
	 * @param networkAddressParam 网络地址
	 * @param nodeId 扫描结点
	 * @param netmask 网络掩码
	 * @return
	 */
	@RequestMapping("assetDiscover")
	@ResponseBody
	public Object assetDiscover(@RequestParam(value="networkAddress",required=false)String networkAddressParam,
							    @RequestParam(value="netmask",defaultValue="24")int netmask,
							    @RequestParam(value="scanNodeId",required=false)String nodeId,
							    @RequestParam(value="rescan",defaultValue="false")boolean rescan){
		Result result = new Result();
		try {
			//没有输入参数、或者参数是127.0.0.1或::1使用本机ip扫描
			if(StringUtil.isBlank(networkAddressParam)||networkAddressParam.equals(IpAddress.getLocalIp().getLocalhostAddress())){
				networkAddressParam = IpAddress.getLocalIpv4Address().toString();
			}
			Node scanNode = nodeMgr.getChildOrSelf(nodeId, NodeDefinition.NODE_TYPE_COLLECTOR) ;
			SubnetUtils subnetUtils = new SubnetUtils(networkAddressParam+"/"+netmask) ;
			String networkAddress = subnetUtils.getInfo().getNetworkAddress() ;
			int assetCount = subnetUtils.getInfo().getAddressCount() ;
			if(assetCount > MAX_SCAN_HOST_COUNT){//限制一次扫描主机的数量
				result.buildError("扫描资产数量超出上限("+MAX_SCAN_HOST_COUNT+")，请修改掩码以减少扫描资产数量");
				return result ;
			}
			if(rescan){//重新扫描
				dcvAssetManager.removeSubnet(new SubNet(scanNode.getIp(),networkAddress, netmask)) ;
			}
			JSONObject data = new JSONObject() ;
			JSONArray assetsJson = new JSONArray();
			SubNet subnet = dcvAssetManager.getSubNet(scanNode.getIp(),networkAddress, netmask) ;
			if (subnet == null) {
				result = sendDiscoverCommand(scanNode, networkAddress, netmask, assetCount) ;
			}else{
				if (ObjectUtils.isNotEmpty(subnet.getAssets())) {
					Node manageNode = nodeMgr.getAuditorOrAgentByIp(subnet.getScanHost()) ;
					for(AssetObject asset:subnet.getAssets()){
						JSONObject assetJson = FastJsonUtil.toJSON(asset,"scanNodeId=nodeId", "ip","mac","assGroup.groupId=groupId","assGroup.groupName=groupName");
						assetJson.put("nodeId", manageNode == null ? nodeMgr.getKernelAuditor(false).getNodeId() : manageNode.getNodeId()) ;
						assetsJson.add(assetJson) ;
					}
				}
				result.build(true) ;
			}
			data.put("rows", assetsJson) ;
			data.put("total", assetsJson.size()) ;
			data.put("progress", subnet == null ? 0 : subnet.getProgress()) ;
			result.setResult(data) ;
		} catch (Exception e) {
			logger.error("assetlist/assetDiscover",e) ;
			result.buildError("系统内部错误！");
		}
		return result ;
	}
	
	@RequestMapping("stopDiscover")
	@ResponseBody
	public Object stopDiscover(@RequestParam(value="networkAddress",required=false)String networkAddressParam,
							   @RequestParam(value="netmask",defaultValue="24")int netmask,
							   @RequestParam(value="scanNodeId")String scanNodeId){
		if(StringUtil.isBlank(networkAddressParam)||networkAddressParam.equals(IpAddress.getLocalIp().getLocalhostAddress())){
			networkAddressParam = IpAddress.getLocalIpv4Address().toString();
		}
		Node collectorNode = nodeMgr.getChildOrSelf(scanNodeId, NodeDefinition.NODE_TYPE_COLLECTOR) ;
		SubnetUtils subnetUtils = new SubnetUtils(networkAddressParam+"/"+netmask) ;
		String networkAddress = subnetUtils.getInfo().getNetworkAddress() ;
		SubNet sbt = dcvAssetManager.getSubNet(collectorNode.getIp(),networkAddress, netmask) ;
		if (sbt != null) {
			sbt.setScannedCount(sbt.getAssetCount()) ;
		}
		HashMap<String,Object> params = new HashMap<String,Object>(3) ;
		params.put("networkAddress", networkAddress) ;
		params.put("netmask", netmask) ;
		try {
			NodeUtil.dispatchCommand(NodeUtil.getRoute(collectorNode), MessageDefinition.CMD_STOP_DISCOVER,params, 5000) ;
		} catch (CommunicationException e) {
			logger.warn("停止资产扫描任务超时!") ;
			return false ;
		}
		return true ;
	}
	
	/**
	 * 下发扫描命令给指定的节点
	 * @param nodeId
	 * @param networkAddress
	 * @param netmask
	 * @param addressCount
	 * @return
	 * @throws IOException
	 */
	private Result sendDiscoverCommand(Node scanNode,String networkAddress,int netmask,int addressCount){
		Result result = new Result() ;
		SubNet subNet = new SubNet(scanNode.getIp(),networkAddress,netmask,addressCount) ;
		try {
			String[] routes = NodeUtil.getRoute(scanNode) ;
			HashMap<String,Object> params = new HashMap<String,Object>(3) ;
			params.put("networkAddress", networkAddress) ;
			params.put("netmask", netmask) ;
			dcvAssetManager.addSubNet(subNet) ;
			NodeUtil.dispatchCommand(routes, MessageDefinition.CMD_DISCORVER_DVC,params, 5000) ;
			result.build(true) ;
		}catch (CommunicationException e) {
			result.build(false,"扫描失败，指定节点可能掉线！") ;
			dcvAssetManager.removeSubnet(subNet) ;
		}catch (Exception e) {
			dcvAssetManager.removeSubnet(subNet) ;
			logger.error("资产发现错误", e) ;
			result.build(false, "系统异常，请与管理员联系！") ;
		}
		return result ;
	}	
	

	/**
	 * license检测
	 * @author zhaojun 2013-12-30下午2:05:26
	 * @param total
	 * @throws InvalidLicenseException
	 * @throws LimitedNumException
	 */
	private void checkLicenseLimit(int total) throws InvalidLicenseException, LimitedNumException {
		Map licenceMap=LicenceServiceUtil.getInstance().getLicenseInfo();
		String licenseValid=(String)licenceMap.get("LICENSE_VALID");
		if(licenseValid==null||licenseValid.equals("0")){
			throw new InvalidLicenseException("Licence invalid!!!");
		} 
		int licenceNum = Integer.valueOf((String)licenceMap.get("TSM_ASSET_NUM"));
		if(total >= licenceNum){
			throw new LimitedNumException("dataCount.compareTo(licenceNum)>=0!!!");
		}
	}	

	@RequestMapping("deleteAsset")
	@ResponseBody
	public Object deleteAsset(@RequestParam("id")String id,HttpServletRequest request,SID sid) {
		try {
			return deleteById(sid,id, request) ;
		} catch (InvalidAssetIdException e) {
			logger.warn("Invalid asset id:"+id);
			return new Result(false, "资产已经被删除");
		} catch (Exception e) {
			logger.error("删除资产出错", e) ;
			return new Result(false,"删除资产失败！") ;
		}
	}
	
	@RequestMapping("deleteAssets")
	@ResponseBody
	@IgnoreSecurityCheck
	public Object deleteAssets(@RequestParam("ids")String[] ids,SID sid,HttpServletRequest request) {
		Result result = new Result() ;
		if(ObjectUtils.isEmpty(ids)){
			return result.buildSuccess(Collections.EMPTY_LIST);
		}
		List<Object> deleteAssetNames = new ArrayList<Object>(ids.length) ;
		result.setResult(deleteAssetNames) ;
		for(int i=0;i<ids.length;i++){
			String id = ids[i] ;
			if(StringUtil.isBlank(id)) continue ;
			try {
				Result deleteResult = deleteById(sid, id, request);
				deleteAssetNames.add(deleteResult.getResult()) ;
			} catch (InvalidAssetIdException e) {
				continue ;
			} catch (Exception e) {
				logger.error("批量删除资产失败!",e) ;
				result.buildError("资产"+(i+1)+"删除失败！") ;
			}
		}
		return result;
	}
	
	private Result deleteById(SID sid,String id,HttpServletRequest request){
		AssetObject ao = AssetFacade.getInstance().deleteAsset(id) ;
		LoginUserCache.getInstance().deleteAuthDevice(id) ;
		AuditLogFacade.deleteSuccessHighest("删除资产",sid.getUserName(), "删除资产:"+ao.getName(), new IpAddress(sid.getLoginIP())) ;
		return new Result().buildSuccess(ao.getName()) ;
	}
	@RequestMapping("scanHistory")
	@ResponseBody
	public Object scanHistory() {
		JSONArray scanHistory = new JSONArray();
		for(SubNet subnet:dcvAssetManager.getAll()){
			JSONObject subnetJson = new JSONObject();
			subnetJson.put("id", subnet.getId()) ;
			subnetJson.put("text", subnet.getNetworkAddress()+"  ["+subnet.getNetmask()+"]") ;
			JSONObject attributes = new JSONObject();
			attributes.put("networkAddress",subnet.getNetworkAddress()) ;
			attributes.put("netmask", subnet.getNetmask()) ;
			Node node = nodeMgr.getAuditorOrAgentByIp(subnet.getScanHost()) ;
			attributes.put("nodeId", node == null ? nodeMgr.getKernelAuditor(false).getNodeId() : node.getNodeId()) ;
			attributes.put("scanHost", subnet.getScanHost()) ;
			subnetJson.put("attributes", attributes) ;
			scanHistory.add(subnetJson) ;
		}
		return scanHistory ;
	}
	@RequestMapping("deleteScanHistory")
	@ResponseBody
	public Object deleteScanHistory(
			@RequestParam("scanHost")String scanHost,
			@RequestParam("networkAddress")String networkAddress,
			@RequestParam("netmask")int netmask) {
		if(scanHost == null || networkAddress == null || netmask < 0 || netmask > 32){
			return false ;
		}
		SubNet subnet = dcvAssetManager.getSubNet(scanHost, networkAddress, netmask) ;
		if(subnet == null){
			return false ;
		}
		if(subnet.getProgress() != 100){
			Node node = nodeMgr.getAuditorOrAgentByIp(scanHost) ;
			Node collectorNode = nodeMgr.getChildOrSelf(node.getNodeId(), NodeDefinition.NODE_TYPE_COLLECTOR) ;
			HashMap<String,Object> params = new HashMap<String,Object>(3) ;
			params.put("networkAddress", networkAddress) ;
			params.put("netmask", netmask) ;
			try {
				NodeUtil.dispatchCommand(NodeUtil.getRoute(collectorNode), MessageDefinition.CMD_STOP_DISCOVER,params, 5000) ;
			} catch (CommunicationException e) {
				logger.warn("停止资产扫描任务超时!") ;
				return false ;
			}
		}
		dcvAssetManager.removeSubnet(subnet) ;
		return true ;
	}
	
	@RequestMapping("downloadAssetTemplet")
	public void downloadAssetTemplet(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
			ExcelOperaterPOI excelOperaterPOI=ExcelOperaterPOI.newInstance(groupService);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"AssetModel.xls\"");
			ServletOutputStream out = response.getOutputStream();
			HSSFWorkbook workbook=excelOperaterPOI.getWorkbook();
			workbook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 资产导入模板下载
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("downloadAssetModel")
	public void downloadAssetModel(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("resource/asset/assetModel.xls");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"assetModel.xls\"");
			ServletOutputStream out = response.getOutputStream();
			byte []b=new byte[1024];
			int len=-1;
			while((len=inputStream.read(b))!=-1){
				out.write(b, 0, len);
			}
			out.flush();
			inputStream.close();
		} catch (Exception e) {
		}
	}

	private Map<String, String>formatErrorMap=null;
	private Map<String, String>dataConflictErrorMap=null;
	private List<ErrorMark> marks=null;
	@RequestMapping("uploadExcelFile")
	public void uploadExcelFile(HttpServletRequest request,HttpServletResponse response,SID sid) throws Exception {     
        // 转型为MultipartHttpRequest：    (multipartResolver)
		setEncode(request, response);
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;     
        // 获得文件： 
        MultipartFile file=multipartRequest.getFile("fileToUpload");
        PrintWriter printWriter;
        if (file.getSize()>10L*1024*1024||!(file.getOriginalFilename().endsWith(".xls"))) {
        	JSONObject result = new JSONObject();
        	result.put("uploadInfo", "failed") ;
			result.put("errorInfo", "文件类型错误或者文件大于10mb! ") ;
        	printWriter = response.getWriter();
			printWriter.print(result);
			printWriter.flush();
			return;
		}
        int total=0;
        int successno=0;
        try {
        	SID.setCurrentUser(sid) ;
        	DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("assetService", request);
        	List<Device> deviceList=xlsToMap(file,sid);
        	total=deviceList.size();
        	List<Result> resultList=batchSaveDevice(deviceList,deviceService);
        	successno=getSuccess(resultList);
		} catch (Exception e1) {
			e1.printStackTrace() ;
		}finally{
			SID.removeCurrentUser();
		}
		try {
			ImportResult importResult=new ImportResult();
			importResult.setSuccessCount(successno);
			importResult.setTotalCount(total);
			importResult.setMessage("success");
			importResult.initErrorContent(formatErrorMap, dataConflictErrorMap);
			importResult.setStatus(true);
			importResult.setMarks(marks);
			Object json=JSON.toJSON(importResult);
			printWriter = response.getWriter();
			printWriter.print(json);
			printWriter.flush();
		} catch (IOException e) {		}
    }
	
	@RequestMapping("uploadFileErrorResult")
	public void uploadFileErrorResult(HttpServletRequest request,HttpServletResponse response) throws Exception {     
		PrintWriter printWriter;
		if (null==marks||marks.size()<1) {
			try {
				JSONObject result = new JSONObject();
				result.put("total", 0) ;
				Object json=JSON.toJSON(new ArrayList<ErrorMark>(0));
				result.put("rows", json) ;
				printWriter = response.getWriter();
				printWriter.print(result);
				printWriter.flush();
			} catch (IOException e) {		}
			return;
		}
		setEncode(request, response);
		int pageNum = 1;// 页码
		int pageSize = 10;
		String _pageNum = request.getParameter("page");
		String _pageSize = request.getParameter("rows");

		if (StringUtils.isNotBlank(_pageNum) && StringUtils.isNotBlank(_pageSize)) {
			try {
				pageNum = Integer.parseInt(_pageNum);
				pageSize = Integer.parseInt(_pageSize);
			} catch (Exception e) {
				pageNum = 1;
				e.printStackTrace();
			}
		}
		int fromIndex=(pageNum-1)*pageSize; 
		int toIndex=pageNum*pageSize;
		if (fromIndex>=marks.size()) {
			return;
		}
		toIndex=toIndex>=marks.size()?marks.size():toIndex;
		if (fromIndex>toIndex) {
			return;
		}
		
		try {
			JSONObject result = new JSONObject();
			result.put("total", marks.size()) ;
			Object json=JSON.toJSON(marks.subList(fromIndex, toIndex));
			result.put("rows", json) ;
			printWriter = response.getWriter();
			printWriter.print(result);
			printWriter.flush();
		} catch (IOException e) {		}
	}
	/**
	 * 解析excel文件
	 * @param file
	 */
	private List<Device> xlsToMap(MultipartFile file,SID sid){
		List<Device> deviceList=new ArrayList<Device>();
		formatErrorMap=new HashMap<String, String>();
		dataConflictErrorMap=new HashMap<String, String>();
		marks=new ArrayList<ErrorMark>();
		AssGroupService assGroupService = (AssGroupService) SpringContextServlet.springCtx.getBean("assetGroupService") ;
		try {
			InputStream instream=file.getInputStream();
			ExcelOperaterManager excelOperaterManager=null;
			String[][] defaultCellArr=null;
			try {
				excelOperaterManager=new ExcelOperaterManager(instream,0,ExcelOperaterPOI.IMPORT_ASSET_NO+1,0);
				defaultCellArr=excelOperaterManager.getDefaultCellArr();
			} catch (Exception e) {
				formatErrorMap.put("fileError", "非正确Excel文件或文件已经损坏！ ");
				return deviceList;
			}
			if(null==defaultCellArr||defaultCellArr.length<1){
				return deviceList;
			}
			boolean status=false;
			String defaultNodeId = nodeMgr.getKernelAuditor(false).getNodeId() ;
			for (int i = 0; i < defaultCellArr.length; i++) {
				String[] strings = defaultCellArr[i];
				if ("IP(*必填)".equals(strings[1])) {
					continue;
				}
				for (int j = 0; j < 6; j++) {
					if (StringUtil.isBlank(strings[j])) {
						status=true;
						if (StringUtil.isNotBlank(strings[0])&&StringUtil.isNotBlank(strings[1])) {
							formatErrorMap.put(strings[0], "必填项有空值！");
							ErrorMark errorMark=new ErrorMark();
							errorMark.setSerialId(blankSetString(strings[0]));
							errorMark.setDeviceIp(blankSetString(strings[1]));
							errorMark.setDeviceName(blankSetString(strings[2]));
							errorMark.setErrorContent("必填项有空值！");
							marks.add(errorMark);
						}
						break;
					}
				}
				if (status) {
					status=false;
					continue;
				}
				if (!isIp(strings[1])) {
					formatErrorMap.put(strings[0], "IP地址无效！");
					ErrorMark errorMark=new ErrorMark();
					errorMark.setSerialId(blankSetString(strings[0]));
					errorMark.setDeviceIp(blankSetString(strings[1]));
					errorMark.setDeviceName(blankSetString(strings[2]));
					errorMark.setErrorContent("IP地址无效！");
					marks.add(errorMark);
					continue;
				}
				if ("请选择".equals(strings[3]) ||"请选择".equals(strings[4])||"请选择".equals(strings[5])
						||strings[3].indexOf("_")<0
						||strings[4].indexOf("_")<0
						||strings[3].indexOf("_")!=strings[3].lastIndexOf("_")
						||strings[4].indexOf("_")!=strings[4].lastIndexOf("_")
						||strings[3].lastIndexOf("_")>=(strings[3].length()-1)
						||strings[4].lastIndexOf("_")>=(strings[4].length()-1)){
					formatErrorMap.put(strings[0], "必选项未选择！");
					ErrorMark errorMark=new ErrorMark();
					errorMark.setSerialId(blankSetString(strings[0]));
					errorMark.setDeviceIp(blankSetString(strings[1]));
					errorMark.setDeviceName(blankSetString(strings[2]));
					errorMark.setErrorContent("必选项未选择！ ");
					marks.add(errorMark);
					continue;
				}
				Device device=new Device();
				OsPlatform os = new OsPlatform();
				if ("请选择".equals(strings[7])||StringUtil.isBlank(strings[7])) {
					os.setOsName("其它");
				}else{
					os.setOsName(strings[7]);
				}
				device.setOs(os);
				if ("请选择".equals(strings[8])||StringUtil.isBlank(strings[8])) {
					device.setSafeRank("中") ;
				}else{
					device.setSafeRank(blankSetString(strings[8])) ;
				}
				device.setAssGroup(new AssGroup(-1));
				//此字段在此处用于判断错误是某行记录，相当于自然键
				device.setExternalId(strings[0]);
				device.setName(strings[2]);
				device.setHostName(blankSetString(strings[6])) ;
				device.setAlias(strings[2]);
				device.setAssetType(AssetType.DEVICE);
				device.setEnabled(0) ;//导入资产默认状态为禁用
				device.setMasterIp(new IpAddress(strings[1]));
				device.setMac(blankSetString(strings[10])) ;
				
				String firstString=strings[3].split("_")[1];
				String secordString=strings[4].split("_")[1];
				device.setDeviceType(firstString+"/"+secordString);//and strings[4]
				device.setDeviceTypeName(DeviceTypeShortKeyUtil.getInstance().deviceTypeToCN(device.getDeviceType(),"/")) ;
				Node node=nodeMgr.getAuditorOrAgentByIp(strings[5].trim());
				device.setScanNodeId(node != null ? node.getNodeId() : defaultNodeId);
				
//				device.setDeviceTypeName(blankSetString(strings[15]));
				device.setManufacturer(blankSetString(strings[11]));
				device.setLocation(blankSetString(strings[12]));
				device.setLinkman(blankSetString(strings[13])) ;
				device.setServices(blankSetString(strings[14]));
//				device.setServices("services") ;
//				device.setNote("note") ;//这2个属性可以不用填写
				device.setIsDelete("false");
				device.setCreator(sid.getUserName()) ;
				deviceList.add(device);
				device=null;
			}
		} catch (IOException e) {
		}
		return deviceList;
	}
	private String blankSetString(String string){
		if (StringUtil.isBlank(string)) {
			return " ";
		}
		return string;
	}
	
	private List<Result> batchSaveDevice(List<Device> deviceList,DeviceService deviceService){
		if (null==deviceList||deviceList.size()<1) {
			return null;
		}
		List<Result> resultList=new ArrayList<Result>();
		for (Device device : deviceList) {
			try {
				Result result=addAssetDevice(device,deviceService);
				resultList.add(result);
			} catch (Exception e) {
				continue;
			}
		}
		return resultList;
	}
	private Result addAssetDevice(Device device,DeviceService deviceService) {
		String ip = device.getMasterIp().toString() ;
		Result result = new Result() ;
		try{
			
			if(deviceService.getDeviceByIp(ip)==null){
				if(device.getEnabled() == 1){
					int total=deviceService.getEnabledTotal();
					checkLicenseLimit(total);
				}
				deviceService.save(device);
				result.build(true) ;
			}else{
				dataConflictErrorMap.put(device.getExternalId(), ip+"资产已经存在! ");
				ErrorMark errorMark=new ErrorMark();
				errorMark.setSerialId(device.getExternalId());
				errorMark.setDeviceIp(ip);
				errorMark.setDeviceName(device.getName());
				errorMark.setErrorContent(ip+"资产已经存在! ");
				marks.add(errorMark);
				result.build(false, "IP地址"+ip+"资产已经存在") ;
			}
		}catch(AssetNameExistException e){
			dataConflictErrorMap.put(device.getExternalId(), "资产名称已经存在！");
			ErrorMark errorMark=new ErrorMark();
			errorMark.setSerialId(device.getExternalId());
			errorMark.setDeviceIp(ip);
			errorMark.setDeviceName(device.getName());
			errorMark.setErrorContent("资产名称已经存在！");
			marks.add(errorMark);
			result.build(false,"资产名称已经存在！") ;
		}catch (InvalidLicenseException e) {
			dataConflictErrorMap.put(device.getExternalId(), "当前License无效！");
			ErrorMark errorMark=new ErrorMark();
			errorMark.setSerialId(device.getExternalId());
			errorMark.setDeviceIp(ip);
			errorMark.setDeviceName(device.getName());
			errorMark.setErrorContent("当前License无效！");
			marks.add(errorMark);
			result.build(false,"当前License无效！") ;
		}catch (LimitedNumException e) {
			dataConflictErrorMap.put(device.getExternalId(), "启用的资产数已达License上限！");
			ErrorMark errorMark=new ErrorMark();
			errorMark.setSerialId(device.getExternalId());
			errorMark.setDeviceIp(ip);
			errorMark.setDeviceName(device.getName());
			errorMark.setErrorContent("资产数已达License上限！");
			marks.add(errorMark);
			result.build(false, "资产数已达License上限！") ;
		}catch (Exception e){
			logger.error("添加资产出错！",e) ;
			result.build(false, "系统异常，请与管理员联系！") ;
		} 
		return result ;
	}
	
	private int getSuccess(List<Result> resultList){
		if (null==resultList||resultList.size()<1) {
			return 0;
		}
		int success=0;
		for (Result result : resultList) {
			if (result.isStatus()) {
				success++;
			}
		}
		return success;
	}
	private boolean isIp(String string){
		if (StringUtil.isBlank(string)) {
			return false;
		}
		if (string.contains(".")) {
			String[]tempsStrings=string.split("\\.");
			if (!(tempsStrings.length==4||tempsStrings.length==6)) {
				return false;
			}
			try {
				int tempno=-1;
				for (String string2 : tempsStrings) {
					tempno=Integer.parseInt(string2.trim());
					if(tempno<0||tempno>255){
						return false;
					}
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	private void setEncode(HttpServletRequest request, HttpServletResponse response){
		try {
			if(null!=request){
				request.setCharacterEncoding("UTF-8");
			}
			if(null!=request){
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html;charset=utf-8");
			}
			
		} catch (UnsupportedEncodingException e) {
		}
		
	}
	
	/**
	 * 获取所有资产IP地址
	 * @return
	 */
	@RequestMapping("getAllAsset")
	@ResponseBody
	public Object getAllAsset(SID sid, HttpServletRequest request){
		DataSourceService monitorService = (DataSourceService) SpringWebUtil.getBean("monitorService", request);
		String type = request.getParameter("type");
		SimDatasource monitor = null; 
		SID.setCurrentUser(sid);
		JSONArray ipArray = new JSONArray();
		Map<String,Object> searchCondition = new HashMap<String, Object>();
		searchCondition.put("enabled", "1");
		List<AssetObject> aoList = AssetFacade.getInstance().getAll(searchCondition);
		for (AssetObject ao : aoList) {
			if (StringUtil.isBlank(type)) {//日志源无个数限制
				ipArray.add(createJson("value", ao.getIp()));
			} else {//过滤已有监视对象的资产
				monitor = monitorService.getFirstByIp(ao.getIp());
				if (monitor == null) {
					ipArray.add(createJson("value", ao.getIp()));
				}
			}
		}
		SID.removeCurrentUser();
		return ipArray;
	}
	
	private JSONObject createJson(String key,Object value){
		JSONObject obj = new JSONObject();
		obj.put(key, value);
		return obj;
	}
}