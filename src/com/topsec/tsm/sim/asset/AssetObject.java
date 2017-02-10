package com.topsec.tsm.sim.asset;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.beanutils.BeanUtils;

import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.sim.asset.web.SubNet;
/**
 * 资产<br>
 * 此类将原来资产分类deviceType进行了拆分（以‘/’分割）<br>
 * 定义原来资产分类一级为category(分类)，二级分类为manufacturer(厂商)
 * @author hp
 *
 */
public class AssetObject extends Device{
	
	/**
	 * 所属子网
	 */
	private SubNet subnet;
	/**资产状态*/
	private AssetState state ;
	/**最后一次资产资产状态更新时间*/
	private long stateUpdateTime ;
	/**资产状态过期时间*/
	private long stateExpireTime = 10*60*1000;
	private AtomicInteger alarmCount = new AtomicInteger(0) ;
	private AtomicInteger eventCount = new AtomicInteger(0);
	private long logCount ;
	/**资产最近告警*/
	//private List<Map> alarmList = new LinkedList<Map>();
	/**资产最近日志*/
	//private List<Map> logList = new LinkedList<Map>() ;
	/**资产最近相关事件*/
	//private List<EventModel> eventList = new LinkedList<EventModel>();
	
	
	public AssetObject(){
	}
	public AssetObject(String id,IpAddress ip){
		super.setId(id) ;
		super.setMasterIp(ip) ;
	}
	public AssetObject(Device commonAttributes) {
		try {
			BeanUtils.copyProperties(this, commonAttributes) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*	public List<Map> getAlarmList() {
		return alarmList;
	}

	public void setAlarmList(List<Map> alarmList) {
		this.alarmList = alarmList;
	}*/
	
/*	public void addAlarm(Map alarm){
		alarmList.add(alarm) ;
	}
	
	public void addEvent(Map event){
		eventList.add(EventModel.createEvent(event)) ;
	}

	public void addEvent(EventModel model){
		eventList.add(model) ;
	}
	
	public List<Map> getLogList() {
		return logList;
	}

	public void setLogList(List<Map> logList) {
		this.logList = logList;
	}

	public List<EventModel> getEventList() {
		return eventList;
	}

	public void setEventList(List<EventModel> eventList) {
		this.eventList = eventList;
	}

	public int getAlarmCount(){
		return alarmList.size() ;
	}
	
	public int getEventCount(){
		return eventList.size() ;
	}*/
	
	public String getIp(){
		return getMasterIp().toString() ;
	}
	
	public AssetState getState(){
		return state ;
	}
	
	public void setState(AssetState state){
		stateUpdateTime = System.currentTimeMillis() ;
		this.state = state ;
	}
	/**
	 * 返回资产第一级分类
	 * @return
	 */
	public String getAssetCategory(){
		String deviceType = getDeviceType() ;
		int slashIndex = deviceType.indexOf('/') ;
		return deviceType.substring(0,slashIndex<0?deviceType.length():slashIndex) ; 
	}
	
	public String getAssetCategoryName(){
		String categoryName = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(getAssetCategory());
		return categoryName ;
	}
	/**
	 * 设备所属厂商编号
	 * @return
	 */
	public String getVender(){
		String deviceType = getDeviceType() ;
		return deviceType.substring(deviceType.indexOf('/')+1) ;
	}
	/**
	 * 设备厂商名称
	 * @return
	 */
	public String getVenderName(){
		return DeviceTypeShortKeyUtil.getInstance().getShortZhCN(getVender()) ;
	}

	public SubNet getSubnet() {
		return subnet;
	}

	public void setSubnet(SubNet subnet) {
		this.subnet = subnet;
	}

	public long getStateUpdateTime() {
		return stateUpdateTime;
	}

	public void setStateUpdateTime(long stateUpdateTime) {
		this.stateUpdateTime = stateUpdateTime;
	}
	
	/**
	 * 判断资产状态是否过期
	 * @return
	 */
	public boolean stateExpire(long currentTime){
		return (currentTime - stateUpdateTime) > stateExpireTime ;
	}
	/**
	 * 刷新资产状态
	 */
	public void flushState(long currentTime){
		if(stateExpire(currentTime)){
			this.state = AssetState.UNKNOW ;
		}
	}
	public long getStateExpireTime() {
		return stateExpireTime;
	}

	public void setStateExpireTime(long stateExpireTime) {
		this.stateExpireTime = stateExpireTime;
	}

	public int getAlarmCount() {
		return alarmCount.intValue();
	}

	public void setAlarmCount(int value) {
		alarmCount.set(value) ;
	}
	
	public void incrementAlarm(){
		alarmCount.incrementAndGet();
	}

	public void incrementAlarm(int count){
		alarmCount.addAndGet(count) ;
	}
	
	public int getEventCount() {
		return eventCount.intValue();
	}

	public void setEventCount(int eventCount) {
		this.eventCount.set(eventCount);
	}
	public void incrementEvent(){
		eventCount.incrementAndGet() ;
	}
	public void incrementEvent(int count){
		eventCount.addAndGet(count);
	}
	public long getLogCount() {
		return logCount;
	}

	public void setLogCount(long logCount) {
		this.logCount = logCount;
	}
}
