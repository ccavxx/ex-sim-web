package com.topsec.tsm.ui.topo.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.ltd.getahead.dwr.WebContextFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.asset.AssTopology;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupByAssetCategory;
import com.topsec.tsm.sim.asset.group.GroupByAssetVender;
import com.topsec.tsm.sim.asset.group.GroupStrategy;
import com.topsec.tsm.sim.asset.service.TopoMgrService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.tree.DefaultTreeIterator;
import com.topsec.tsm.sim.common.tree.FastJsonResult;
import com.topsec.tsm.sim.common.tree.FastJsonTreeVisitor;
import com.topsec.tsm.sim.common.tree.VisitResultListener;
import com.topsec.tsm.sim.resource.persistence.Group;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.ui.topo.svg.elements.Image;
import com.topsec.tsm.ui.topo.svg.elements.PolyLine;
import com.topsec.tsm.ui.topo.svg.elements.Rect;
import com.topsec.tsm.ui.topo.util.TopoConstant;
import com.topsec.tsm.ui.topo.util.TopoUtil;
import com.topsec.tsm.ui.topo.vo.ItemVo;
import com.topsec.tsm.ui.topo.vo.NetworkToolVo;
import com.topsec.tsm.ui.topo.vo.StatisticsVo;

public class TopoMgrBean {
	private static Logger log = LoggerFactory.getLogger(TopoMgrBean.class);
	public static final String DOMAIN = "DM";
	public static final String DEFAULT_WIDTH = "250";
	public static final String DEFAULT_HEIGHT = "170";
	public static  Map<String,String> assetInfoMap = new HashMap<String,String>();
	public static final double[][] XY = { { 20, 20 }, { 340, 20 }, { 660, 20 }, { 20, 240 }, { 340, 240 }, { 660, 240 }, { 20, 460 }, { 340, 460 }, { 660, 460 } };

	public static final String TOPO_TEMPLATE_TYPE_NODES = "nodesTemplate";
	public static final String TOPO_TEMPLATE_TYPE_DOMAINS = "domainsTemplate";
	public TopoMgrBean() {
	}
	public boolean addAssTopology(){
		boolean boo = true;
		TopoMgrService topoService;
		try {
			topoService = getTopoService();
			AssTopology ass = new AssTopology();
			ass.setViewId("domain");
			ass.setNodeId("localhost");
			ass.setViewInfo("<g></g>");
			topoService.addAssTopology(ass);
		} catch (Exception e) {

			log.error(e.getMessage());
		}
		return boo;
	}
	public int saveNowView(String viewId,String str,String nodeId){
		int boo = 1;
		try {
			TopoMgrService topoService = getTopoService();
			AssTopology assTopology = new AssTopology();
			assTopology.setViewInfo(str.replaceAll("xlink:href", "href"));
			topoService.updateAssTopology(TopoCache.assTopoId, assTopology);
		} catch (Exception e) {

			log.error(e.getMessage());
			boo = 0;
		}
		return boo;
	}

	/**
	 * 根据srcId打开一个新的topo·获得topo的json数据
	 * @param viewId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List drawAreaTopo(String viewId){
		TopoMgrService topoService = getTopoService();
		try {
			String viewInfo = "";
			if(TopoUtil.isNotEmpty(viewId)){
				AssTopology ass = topoService.queryAssTopologyById(Integer.parseInt(viewId));
				TopoCache.assTopoId = ass.getResourceId();
				viewInfo = ass.getViewInfo();
			}else{
				AssTopology ass = topoService.queryUniqueAssTopolog();
				if(ass==null){
					TopoCache.assTopoId = -1;
					viewInfo = "";
				}else{
					TopoCache.assTopoId = ass.getResourceId();
					viewInfo = ass.getViewInfo();
				}
			}

			Map config = null;
			if(StringUtils.isNotEmpty("topoViewXml")){
				config = TopoUtil.readTopoViewConf1(viewInfo);
			}else{
				config = null;
			}
			if (config != null && !config.isEmpty()) {
				List result = new ArrayList();
				Map lineConfig = (Map) config.get(TopoConstant.LINE);
				Map imageConfig = (Map<String, Image>) config.get(TopoConstant.IMAGE);
				Map polyLineConfig = (Map<String, PolyLine>)config.get(TopoConstant.POLY_LINE);
				Map rectConfig =(Map)config.get(TopoConstant.RECT);
				Map textConfig =(Map)config.get(TopoConstant.TEXT);
				Map tspanConfig =(Map)config.get(TopoConstant.TSPAN);
				//horizon-start
				Map circleConfig =(Map)config.get(TopoConstant.CIRCLE);
				if(circleConfig!=null){
					for (Iterator it = circleConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				//horizon-end
				if(rectConfig!=null){
					for (Iterator it = rectConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				
				if(polyLineConfig!=null){
					for (Iterator it = polyLineConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(textConfig!=null){
					for (Iterator it = textConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(lineConfig!=null){
					for (Iterator it = lineConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(imageConfig!=null){
					for (Iterator it = imageConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(tspanConfig!=null){
					for (Iterator it = tspanConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(polyLineConfig!=null){
					for (Iterator it = polyLineConfig.entrySet().iterator(); it.hasNext();) {
						Entry entry = (Entry) it.next();
						result.add(entry.getValue());
					}
				}
				if(result.size()==0){
					return null;
				}
				return result;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	public void putDevIntoArea(String viewId,String imageId,String viewName){
		TopoUtil.putDevIntoArea(viewId, imageId, viewName);
	}
	public void saveViewSize(String WAmp,String HAmp){
		TopoUtil.saveTopoViewSize(WAmp, HAmp);
	}
	public String readViewSize(){
		return TopoUtil.readTopoViewSize();
	}
	/**
	 * 获得拓扑图背景图片
	 * @return
	 */
	public String findViewImage(){
		return TopoUtil.findBgView();
	}

	public void lockDevWindow(String winId, boolean isLock) {
		if (StringUtils.isEmpty(winId)) {
			return;
		}
		String devId = winId.split("-DV")[1];
		TopoUtil.lockDevWindow(devId, winId, isLock);
	}

	
	public String getAreaConf(String viewId) {
		try {
			Map<String, String> map = TopoUtil.readTopoUIConf(viewId);
			String json = TopoUtil.toJson(map);
			//log.error("getAreaConf " + json);
			return json;
		} catch (Exception e) {
			log.error("getAreaConf error" + e.getMessage(), e);
		}
		return "";
	}
	/**
	 * 保存设置背景
	 * @param url
	 */
	public void saveBgUrl(String url){
		try {
			TopoUtil.saveBgView(url);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void saveNetworkToolConfig(NetworkToolVo vo) {
		TopoUtil.saveOrUpdateNetToolConf(vo);
	}

	public NetworkToolVo useNetTool(String devId) {
		String msg = "useNetTool error";
		NetworkToolVo vo = null;

		if (StringUtils.isBlank(devId)) {
			log.error(msg + " devId is empty");
		} else {
			vo = TopoUtil.readNetWorkConf(devId);
		}
		return vo;
	}
	/**
	 * 网络工具方法
	 * @param id
	 * @return
	 */
	public NetworkToolVo getNetworkToolById(String id) {
		return useNetTool(id + "");
	}



	public String getDomainInfo(String viewId) throws Exception {
		// AssetFacadeBD delegate = getAssetFacadeBD();
		// return delegate.getAssetGroup(domainId).getResourceName();
		String readTopoName = TopoUtil.readTopoName(viewId);
		return readTopoName;
	}

	public List drawViewBox(String areaViewId) throws Exception {

		return null;
	}
	public String getAreaJson(){
		List<ItemVo> list = TopoUtil.findTopoViewList();
		Gson gson = new Gson();
		String str = gson.toJson(list);
		return str;
	}
	public boolean deleteViewById(String id){
		boolean boo = TopoUtil.deleteViewById(id);
		return boo;
	}
	public void addToView(String viewId,String url){
		TopoUtil.addTopoToView(viewId,url);
	}
	public String getViewJson() {
		List<ItemVo> list = TopoUtil.findTopoViewList1();
		Gson gson = new Gson();
		String str = gson.toJson(list);
		return str;
	}
	public boolean copyDomainView(String ids,String desc) throws Exception {
		Map<String,String> map = new HashMap<String, String>();
		String[] strings = ids.split(",");
		for (int i = 0; i <strings.length; i++) {
			map.put(strings[i],strings[i].split("-")[0]);
		}
		try {
			TopoUtil.copyTopoToView(desc, map);
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}
	@SuppressWarnings("unchecked")
	public List getTopoGroup(String parentId) throws Exception {
		try {

			Map config = TopoUtil.readTopoViewConf(parentId);

			if (config != null && !config.isEmpty()) {
				List result = new ArrayList();
				Map rectConfig = (Map) config.get(TopoConstant.TEXT);
				// ï¿½ï¿½ï¿½ßºï¿½ï¿½è±¸
				for (Iterator it = rectConfig.entrySet().iterator(); it.hasNext();) {
					Entry entry = (Entry) it.next();
					if(entry.getKey().toString().indexOf("-DV")==-1){
					result.add(entry.getValue());
					}
				}
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("drawMap error " + e);
		}
		return null;
	}
	//保存视图

	public void createAreaView(String id,String name){
		String pid = "";
		TopoUtil.createTopoView(id, name, pid);
	}
	//导出xml	
	public void exportXmlFile(String path){
		TopoUtil.exportXmlFile(path);
	}
	//导入xml	
	public void importXmlFile(String path){
		TopoUtil.importXmlFile(path);
	}
	//查找设备
	public List selectImageByAgr(String val,String viewId){
		return TopoUtil.selectImageList(val, viewId);
	}
	@SuppressWarnings("unchecked")
	public List getTopoImage(String parentId) throws Exception {
		try {

			Map config = TopoUtil.readTopoViewConf("doamin.shanXi.xiAn");

			if (config != null && !config.isEmpty()) {
				List result = new ArrayList();
				Map rectConfig = (Map) config.get(TopoConstant.IMAGE);
				for (Iterator it = rectConfig.entrySet().iterator(); it.hasNext();) {
					Entry entry = (Entry) it.next();
					if(entry.getKey().toString().indexOf(parentId+"-DV")!=-1){
						result.add(entry.getValue());
					}
				}
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("drawMap error " + e);
		}
		return null;
	}
	



	private Rect doDefaultDomain(Group domain, int j, String id) {
		Rect rect = new Rect();
		rect.setName(domain.getResourceName());
		rect.setId(id);
		rect.setWidth(DEFAULT_WIDTH);
		rect.setHeight(DEFAULT_HEIGHT);
		rect.setType(TopoConstant.RECT);
		rect.setModel(TopoConstant.MODEL_DOMAIN);
		if (j < 9) {
			rect.setX(String.valueOf(XY[j][0]));
			rect.setY(String.valueOf(XY[j][1]));
		} else {
			rect.setX(String.valueOf(XY[j % 9][0] + (10) * (j / 9)));
			rect.setY(String.valueOf(XY[j % 9][1] + (10) * (j / 9)));
		}
		return rect;
	}

	private Rect doDomain(Group domain, Map config, String id) {
		Rect rect = (Rect) config.get(id);
		if (rect == null) {
			rect = new Rect();
			rect.setId(id);
			rect.setX("100");
			rect.setY("100");
			rect.setWidth(DEFAULT_WIDTH);
			rect.setHeight(DEFAULT_HEIGHT);
			rect.setModel(TopoConstant.MODEL_DOMAIN);
			rect.setType(TopoConstant.RECT);
			rect.setName(domain.getResourceName());
		} else {
			rect.setNodeId("333");
			rect.setName(domain.getResourceName());
		}

		return rect;
	}

	


	/**
	 * added by ysf 2010-2-25
	 * 
	 * @param domainId
	 * @param ids
	 * @return
	 * @throws Exception
	 */




	private HttpServletRequest getRequest() {
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		return request;
	}

	private Object getBean(String beanName) {
		return WebApplicationContextUtils.getWebApplicationContext(getRequest().getSession().getServletContext()).getBean(beanName);
	}
	private TopoMgrService getTopoService() {
		return (TopoMgrService) getBean("topoMgrService");
	}
	private DeviceService getDeviceService() {
		return (DeviceService) getBean("deviceService");
	}
	
	/**
	 * 
	 * 
	 * @param itemId
	 * @return
	 */


	public List<String> checDevStatues(String srcJumpId){
		List<String> list = new ArrayList<String>();
		List<String> idList = new ArrayList<String>();
		Map config = TopoUtil.readTopoViewConf(srcJumpId);
		Map imageConfig = (Map<String, Image>) config.get(TopoConstant.IMAGE);
		for (Iterator it = imageConfig.entrySet().iterator();it.hasNext();) {
			Entry entry = (Entry) it.next();
			String idString = entry.getKey().toString();
			idList.add(idString);
		}
		try{
			for(int i=0;i<idList.size();i++){
				if(idList.get(i).toString().indexOf("DV")!=-1){
					StatisticsVo conf = TopoUtil.getStatisticsVo(idList.get(i).toString().split("-DV")[1]);
					StringBuffer str = new StringBuffer();
					if(conf.getDevStatus()!=null){
						for(int j=0;j<conf.getDevStatus().length;j++){
							str.append(conf.getDevStatus()[j]+",");
						}
						if(str.toString().indexOf("Z")!=-1){
							list.add(idList.get(i).toString());
						}
					}
				}
			}
		}catch (Exception e) {

			e.printStackTrace();
		}
		return list;
	}
	public boolean checkLine(String devId){
		boolean boo = false;
		try{
			StatisticsVo conf = TopoUtil.getStatisticsVo(devId.toString());
			if(conf.getPhyValues()!=null){
				String[] str = conf.getPhyValues();
				for(int i=0;i<str.length;i++){
					if(Integer.parseInt(str[i])<0){
						boo = true;
					}
				}
			}
		}catch (Exception e) {

		}
		return boo;
	}
	public static void main(String[] args) {
		 Map map = new HashMap();
		 map.put("aaaaaa", "ccccc");
		 map.put("bbbbbb", "dddddd");
		
		 String json = TopoUtil.toJson(map);
		 log.error(json);

		// MD5Util u = new MD5Util();
		// System.out.println(u.toHashString("o".getBytes()));

		// System.out.println(TopoUtil.random(100));
		// System.out.println(TopoUtil.random(10));

		//System.out.println("DM770806".split("DM")[1]);

	}

	/**
	 * 锁定信息层·保留方法
	 * @return
	 */
	public String getLockDevs() {
		Set<String> locks = TopoUtil.readTopoViewLockDevs();
		if (locks.isEmpty())
			return "";
		String json = TopoUtil.toJson(locks);
		log.info("getLockDevs json" + json);
		return json;
	}

	private SID getSid() {
		HttpSession session = getRequest().getSession();
		String sidV = String.valueOf(session.getAttribute("sid"));
		return new SID(sidV);
	}

	/**
	 * 获得图片列表信息(id、name、url)·保留方法
	 * @return
	 */
	public String getImagesUrl(){
		List list = TopoUtil.readImageUrl();
		String json = TopoUtil.toJson(list);
		return json;
	}

	public void setImageUrl(String id,String name,String url){
		TopoUtil.saveImageUrl(id, name, url);
	}
	public List getWarnImages(String ips){
		String[] strs = ips.split("&");
		String address = "";
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < strs.length; i++) {
			String ip = strs[i].split("=")[0];
			String id = strs[i].split("=")[1];
			map.put(ip, id);
			address = ip+",";
		}
		
		return null;
	}

	/**
	 * 获得拓扑树结构树
	 * @return
	 */
	public String getViewsTreeList(){
		TopoMgrService topoService = getTopoService();
		List<AssTopology> list = null;
		try {
			list = topoService.queryAssTopologyList("");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		List<Map<String, String>> arrayList = new ArrayList<Map<String,String>>();
		if(list!=null){
			for (int i = 0; i < list.size(); i++) {
				AssTopology assTopology = list.get(i);
				Integer resourceId = assTopology.getResourceId();
				String viewName = assTopology.getViewName();
				Map<String, String>  map = new HashMap<String, String>();
				map.put("id", resourceId+"");
				if(!TopoUtil.isNotEmpty(viewName))
					viewName = "未命名";
				map.put("text", viewName);
				map.put("pid", "base");
				arrayList.add(map);
			}
			
		}
		Map<String, String>  map = new HashMap<String, String>();
		map.put("id", "base");
		map.put("text", "拓扑树");
		map.put("pid", "-1");
		arrayList.add(map);
		Gson gson = new Gson();
		String str = gson.toJson(arrayList);
		return str;
	}
	/**
	 * 创建拓扑树新节点
	 * @param id
	 * @param name
	 * @param pid 备用节点
	 * @return
	 */
	public String createNewTopoView(String id,String name,String pid){
		try {
			String uuid =UUID.randomUUID().toString();

			String ids = uuid.replaceAll("-", "");

			TopoMgrService service = getTopoService();
			AssTopology ass = new AssTopology();
			ass.setCreateTime(new Date());
			ass.setViewName(name);
			ass.setViewId(ids);
			service.addAssTopology(ass);

			// 获得id
			if(ass.getResourceId()!=null){
				id=ass.getResourceId().toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return id;
	}
	public int deleteViewNode(Integer id){
		int boo = 1;
		try {
			TopoMgrService service = getTopoService();
			service.deleteAssTopology(id);
		} catch (Exception e) {

			boo = 0;
			log.error("TopoMgrBean.deleteViewNode exception!",e);
		}
		return boo;
	}
	public int changeViewName(String id,String name){
		int boo = 1;
		try {
			AssTopology ass = new AssTopology();
			ass.setViewName(name);
			TopoMgrService service = getTopoService();
			service.updateAssTopology(Integer.parseInt(id), ass);
		} catch (Exception e) {
			log.error("TopoMgrBean.changeViewName method exception",e);
			boo = 0;
		}
		return boo;
	}

	/**
	 * 获得设备关系树
	 * @return
	 */
	public String getDeviceTree(){
		GroupStrategy strategy = new GroupByAssetCategory(new GroupByAssetVender()) ;
		AssetGroup root = AssetFacade.getInstance().groupByWithRoot(strategy) ;
		DefaultTreeIterator iterator = new DefaultTreeIterator() ;

		final List<Object> jsonResult = new ArrayList<Object>() ;

		iterator.regist(new VisitResultListener<FastJsonResult,AssetGroup>() {
			@Override
			public void onResult(FastJsonResult result, AssetGroup group) {
				if(group.getId()!=null){					
					JSONObject groupJson = FastJsonUtil.toJSON(group, "pathId=id", "name=text", "parent.id=pid");
					if(groupJson.size()>0){
						if(groupJson.getString("type")==null){
							groupJson.put("type", "group");
						}
						jsonResult.add(groupJson);
					}
					System.out.println(jsonResult);
					if(group.isLeaf()){
						List<AssetObject> assets = group.getAssets() ;
						for(AssetObject ao:assets){
							JSONObject assetJson = FastJsonUtil.toJSON(ao, "id","name=text","deviceType=type");
							assetJson.put("pid", group.getPathId()) ;
							assetJson.put("topType", ao.getAssetCategory()) ;
							jsonResult.add(assetJson);
						}
					}
				}
			}
		}) ;
		iterator.iterate(root, new FastJsonTreeVisitor()) ;
		return JSONArray.toJSONString(jsonResult) ;
	}
	/**
	 * 添加设备
	 * horizon
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	public List<Image> getAssetInfosByIds(String idAndTopTypes) throws Exception {
		DeviceService delegate = getDeviceService();
		String[] _idAndTopTypes = idAndTopTypes.split(",");
		List<Image> result = new ArrayList<Image>();
		for (int i = 0; i < _idAndTopTypes.length; i++) {
			String[] _idAndTopType = _idAndTopTypes[i].split(":");
			if(_idAndTopType.length==2){				
				String id = _idAndTopType[0];
				String topType = _idAndTopType[1];

				if(StringUtils.isNotEmpty(id)){
					Device device = delegate.getDevice(id);
					Image img = new Image();
					img.setId(device.getId());
					img.setName(device.getName());
					img.setIp(device.getMasterIp().toString());
					img.setType(device.getDeviceType());
					img.setHref(topType);
					result.add(img);
				}
			}
		}
		return result;
	}

	/**
	 * 通过ID集合设定assetsInfo（当前设备的信息）
	 * @param domainId
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	public Map getAssetsInfo(long domainId, String[] ids) throws Exception {
		try {
			DeviceService delegate = getDeviceService();
			for (int i = 0; i < ids.length; i++) {
				if(StringUtils.isNotEmpty(ids[i])){
					Device device = delegate.getDevice(ids[i]);
					String deviceTypeName = device.getDeviceTypeName();
					String assetType = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(device.getDeviceType());
					 String ip = device.getMasterIp().toString();
					String r = deviceTypeName + ";" + ip + ";" + assetType;
					assetInfoMap.put(ids[i] + "", r);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("getAssetsInfo error" + e.getMessage());
		}
		return assetInfoMap;

	}

}
