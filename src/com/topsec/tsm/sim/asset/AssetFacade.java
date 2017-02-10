package com.topsec.tsm.sim.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.ass.InvalidAssetIdException;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.sim.alarm.service.AlarmService;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupStrategy;
import com.topsec.tsm.sim.asset.service.AssetService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.util.ticker.Ticker;
import com.topsec.tsm.util.ticker.Tickerable;

public class AssetFacade implements Tickerable{

	private AssetService assetService ;
	private Map<String,AssetObject> assets ;
	
	private static AssetFacade instance ;
	private AssetFacade(){
		init() ;
	}
	
	private void init(){
		while(assetService == null){
			assetService = (AssetService) SpringContextServlet.springCtx.getBean("assetService") ;
		}
		Ticker ticker = (Ticker) SpringContextServlet.springCtx.getBean("ticker") ;
		ticker.addTicker(this) ;
		assets = new HashMap<String, AssetObject>() ;
		List<Device> devices = assetService.getAll() ;
		if(devices==null){
			return ;
		}
		for(Device device:devices){
			addAssetFromDevice(device) ;
		}
		initEventCount() ;
	}
	
	private void initEventCount(){
		Date beginDate = ObjectUtils.dayBegin(new Date()) ;
		Date endDate = ObjectUtils.dayEnd(beginDate) ;
		EventService eventService = (EventService) SpringContextServlet.springCtx.getBean("eventService") ;
		List<Map<String,Object>> eventStatDatas = eventService.getEventStatistic(beginDate, endDate) ;
		if(eventStatDatas == null){
			return ;
		}
		for(Map<String,Object> event:eventStatDatas){
			String dvc = (String) event.get("dvc_address") ;
			String dest = (String) event.get("dest_address") ;
			String src = (String) event.get("src_address") ;
			int total = ((Number) event.get("total")).intValue() ;
			if(dvc != null){
				incrementIpEvent(dvc,total) ;
			}
			if(src != null && !src.equals(dvc)){
				incrementIpEvent(src,total) ;
			}
			if(dest != null && !dest.equals(dvc) && !dest.equals(src)){
				incrementIpEvent(dest,total) ;
			}
		}
	}
		
	private void incrementIpEvent(String ip,int count){
		AssetObject asset = getAssetByIp(ip) ;
		if (asset != null) {
			asset.incrementEvent(count);
		}
	}
	
	public AssetObject addAssetFromDevice(Device device){
		AssetObject asset =  new AssetObject(device) ;
		addAsset(asset) ;
		return asset ;
	}
	
	/**
	 * 获取总数
	 * @return
	 */
	public int getTotal(){
		return assets.size() ;
	}
	/**
	 * 资产在线总数
	 * @return
	 */
	public int getOnlineCount(){
		return AssetUtil.totalAssetUseState(assets.values(), AssetState.ONLINE) ;
	}
	/**
	 * 资产不在线总数
	 * @return
	 */
	public int getOfflineCount(){
		return AssetUtil.totalAssetUseState(assets.values(), AssetState.OFFLINE) ;
	}
	/**
	 * 返回所有资产信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<AssetObject> getAll(){
		List<AssetObject> result = new ArrayList<AssetObject>() ;
		SID currentUser = SID.currentUser() ;
		if(currentUser != null && currentUser.hasOperatorRole() && !currentUser.isOperator()){
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("deviceId") ;
			Collection<String> userDeviceIds = (Collection<String>) CollectionUtils.collect(currentUser.getUserDevice(),trans) ;
			for(AssetObject ao:assets.values()){
				if(userDeviceIds.contains(ao.getId())){
					result.add(ao) ;
				}
			}
		}else{
			result = new ArrayList(assets.values()) ;
		}
		return result ;
	}
	
	/**
	 * 返回查询条件的所有资产信息
	 * @return
	 */
	public List<AssetObject> getAll(Map<String,Object> searchCondition){
		// 获得数据集合
		List<AssetObject> assetObjectList = new ArrayList<AssetObject>() ;

		String ip = (String) searchCondition.get("ip");
		String name = (String) searchCondition.get("name");
		String deviceType = (String) searchCondition.get("deviceType");
		String osName = (String) searchCondition.get("osName");
		String safeRank = (String) searchCondition.get("safeRank");
		String group = (String) searchCondition.get("group");
		String nodeId = (String) searchCondition.get("nodeId");
		Integer enabled = StringUtil.toInteger((String)searchCondition.get("enabled")) ;
		// 如果有查询条件
		// 过滤数据集合
		List<AssetObject> allAssets = getAll() ;
		for(AssetObject ass:allAssets){
			// 条件判断
			if (StringUtil.isNotBlank(ip) && !ass.getIp().contains(ip)){
				continue ;
			}
			if (StringUtil.isNotBlank(name)  && !ass.getName().contains(name)){
				continue ;
			}
			if (StringUtil.isNotBlank(deviceType) && !ass.getDeviceType().equals(deviceType)){
				continue ;
			}
			if(StringUtil.isNotBlank(osName) && !ass.getOs().getOsName().contains(osName)){
				continue ;
			}
			if(StringUtil.isNotBlank(group) && !(Integer.valueOf(group)).equals(ass.getAssGroup().getGroupId())){
				continue ;
			}
			if(StringUtil.isNotBlank(safeRank) && !ass.getSafeRank().equals(safeRank)){
				continue ;
			}
			if(StringUtil.isNotBlank(nodeId) && !ass.getScanNodeId().equals(nodeId)){
				continue ;
			}
			if(enabled != null && !ass.getEnabled().equals(enabled)){
				continue ;
			}
			assetObjectList.add(ass) ;
		}
		return assetObjectList;
	}
	
	public PageBean<AssetObject> getPage(int pageIndex,int pageSize,Map<String,Object> searchCondition){
		List<AssetObject> asset =  getAll(searchCondition);
		Collections.sort(asset, IpComparator.getInstance()) ;
		int beginIndex = (pageIndex-1)*pageSize ;
		int endIndex = beginIndex+pageSize ;
		PageBean<AssetObject> pager = new PageBean<AssetObject>(pageIndex, pageSize, asset.size()) ;
		if(asset.size()<beginIndex){
			pager.setData(Collections.<AssetObject> emptyList()) ;
			return pager ;
		}
		if(asset.size()<endIndex){
			endIndex = asset.size() ;
		}
		pager.setData(asset.subList(beginIndex, endIndex)) ; 
		return pager ;
	}
	
	/**
	 * 根据扫描节点，获取对应的资产信息
	 * @param nodeId
	 * @return
	 */
	public List<AssetObject> getByScanNode(String nodeId){
		List<AssetObject> result = new ArrayList<AssetObject>() ;
		for(AssetObject ao:getAll()){
			if(ao.getScanNodeId().equals(nodeId)){
				result.add(ao) ;
			}
		}
		return result ;
	}
	
	public AssetObject getById(String id){
		if(id == null ){
			return null ;
		}
		for(AssetObject ao:getAll()){
			if(id.equals(ao.getId())){
				return ao ;
			}
		}
		return null ;
	}
	/**
	 * 增加资产
	 * @param device
	 */
	public AssetObject addAsset(AssetObject asset){
		assets.put(asset.getIp(), asset) ;
		return asset ;
	}
	/**
	 * 将数据库资产加入到当前缓存中
	 * @param id　资产id
	 */
	public AssetObject addAssetFromDB(String id)throws InvalidAssetIdException{
		Device device =  assetService.getDevice(id) ;
		return addAssetFromDevice(device) ;
	}
	/**
	 * 删除当前缓存中对应的资产，重新加载数据为中的资产信息
	 * @param id
	 * @return
	 * @throws InvalidAssetIdException
	 */
	public AssetObject reloadAssetFromDB(String id)throws InvalidAssetIdException{
		Device device =  assetService.getDevice(id) ;
		return merge(device, id) ;
	}
	
	public void reloadAllFromDB(){
		List<Device> allDevice = assetService.getAll() ;
		for(Device device:allDevice){
			merge(device, device.getId()) ;
		}
	}
	private AssetObject merge(Device device,String id){
		if(device == null){
			throw new InvalidAssetIdException(id) ;
		}
		AssetObject ao = getById(id) ;
		if (ao != null) {
			try {
				BeanUtils.copyProperties(ao, device) ;
			}catch(Exception e){}
			return ao ;
		}
		return null ;
	}
	public AssetObject updateAsset(String id,String oldIp){
		Device device = assetService.getDevice(id) ;
		if(oldIp.equals(device.getMasterIp().toString())){//新旧ip相同,只更新资产基本信息
			AssetObject ao = getAssetByIp(oldIp) ;
			try {
				BeanUtils.copyProperties(ao, device) ;
			}catch (Exception e) {}
			return ao ;
		}else{
			deleteAsset(oldIp) ;
			return addAssetFromDevice(device) ;
		}
	}
	/**
	 * 根据ip删除资产
	 * @param ip
	 */
	public AssetObject deleteAsset(String id){
		Device device = assetService.deleteDevice(id) ;
		if (device != null) {
			return assets.remove(device.getMasterIp().toString()) ;
		}
		return null ;
	}
	
	public AssetObject getAssetByIp(String ip){
		return assets.get(ip) ;
	}
	
	public AssetObject changeState(String id, Integer state,boolean cascade)throws InvalidAssetIdException {
		assetService.changeState(id, state,cascade) ;
		AssetObject device = getById(id) ;
		device.setEnabled(state) ;
		return device ;
	}
	/**
	 * 按照分组策略，将所有资产分别分配到不同的组中
	 * @param strategy 分组方式
	 * @return
	 */
	public List<AssetGroup> groupBy(GroupStrategy strategy){
		for(AssetObject asset:AssetFacade.getInstance().getAll()){
			asset.setEventCount(0) ;
		}
		initEventCount();
		return groupBy(strategy,getAll()) ;
	}
	/**
	 * 根据分组策略,对资产列表中资产进行分组
	 * @param strategy 分组方式
	 * @param assetList 资产
	 * @return
	 */
	public List<AssetGroup> groupBy(GroupStrategy strategy,List<AssetObject> assetList){
		AssetGroup root = groupByWithRoot(strategy, assetList) ;
		List<AssetGroup> result = root.getChildren() ;
		return result ;
	}
	/**
	 * 根据分组资产将资产进行分组，并返回分组后的根节点
	 * @see #groupByWithRoot(GroupStrategy, List)
	 * @param strategy
	 * @return
	 */
	public AssetGroup groupByWithRoot(GroupStrategy strategy){
		return groupByWithRoot(strategy, getAll()) ;
	}
	
	/**
	 * 根据分组资产将资产分组，并返回根节点<br>
	 * 返回的根节点是一个虚拟的根节点(没有名称，没有id的根节点)<br>
	 * 所有的资产分析都包含在root节点的children中
	 * @param strategy
	 * @return
	 */
	public AssetGroup groupByWithRoot(GroupStrategy strategy,List<AssetObject> assetList){
		AssetGroup rootGroup = new AssetGroup(null,null,strategy) ;
		if (ObjectUtils.isEmpty(assetList)) {
			return rootGroup;
		}
		for(AssetObject asset:assetList){
			rootGroup.addAsset(asset) ;
		}
		return rootGroup ;
	}
	
	@Override
	public void onTicker(long ticker) {
		long currentTime = System.currentTimeMillis() ;
		for(AssetObject ao:assets.values()){
			ao.flushState(currentTime) ;
		}
	}
	
	public static AssetFacade getInstance(){
		if (instance == null) {
			synchronized (AssetFacade.class) {
				if (instance == null) {
					instance = new AssetFacade() ;
				}
			}
		}
		return instance ;
	}

}
