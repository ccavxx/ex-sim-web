package com.topsec.tsm.sim.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.mapping.Array;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.ass.persistence.OsPlatform;
import com.topsec.tsm.sim.util.NodeUtil;

public class AssetUtil {

	/**
	 * 统计设备列表中指定设备状态的设备总数
	 * @param assets
	 * @param state
	 * @return
	 */
	public static int totalAssetUseState(Collection<AssetObject> assets,AssetState state){
		int assetCount = 0 ;
		for(AssetObject obj:assets){
			AssetState assetState = obj.getState() ;
			if(state != null && assetState == state){
				assetCount++;
			}
		}
		return assetCount ;
	}
	/**
	 * 返回指定设备类型图标样式
	 * @param deviceType 设备分类
	 * @return
	 */
	public static String getIconClsByDeviceType(String deviceType){
		int slashIndex = deviceType.indexOf('/') ;
		String firstDeviceType ;
		if(slashIndex > -1){
			firstDeviceType = deviceType.substring(0,slashIndex).toLowerCase() ;
		}else{
			firstDeviceType = deviceType.toLowerCase() ;
		}
		if("firewall".equals(firstDeviceType)){
			return "icon-firewall" ;
		}else if("os".equals(firstDeviceType)){
			return "icon-server";
		}else if("router".equals(firstDeviceType)){
			return "icon-router";
		}else if("switch".equals(firstDeviceType)){
			return "icon-switch";
		}else if("ids".equals(firstDeviceType)){
			return "icon-ids";
		}else if("ips".equals(firstDeviceType)){
			return "icon-ips" ;
		}else if("gate".equals(firstDeviceType)){
			return "icon-gate";
		}else if("audit".equals(firstDeviceType)){
			return "icon-audit";
		}else if("flow".equals(firstDeviceType)){
			return "icon-flow";
		}else if("antidos".equals(firstDeviceType)){
			return "icon-antidos";
		}else if("antivirus".equals(firstDeviceType)){
			return "icon-antivirus";
		}else if("flowsec".equals(firstDeviceType)){
			return "icon-flowsec";
		}else{
			return "icon-asset";
		}
	}
	/**
	 * 返回指定设备类型图标样式
	 * @param deviceType
	 * @return
	 */
	public static String getBigIconClsByDeviceType(String deviceType){
		String iconCls = getIconClsByDeviceType(deviceType) ;
		if (iconCls != null) {
			iconCls += "-24" ;
		}
		return iconCls ;
	}
	/**
	 * 根据资产类型获取大小为48*48像素的资产图片
	 * @param deviceType
	 * @return
	 */
	public static String getIcon48(String deviceType){
		int slashIndex = deviceType.indexOf('/') ;
		String firstDeviceType ;
		if(slashIndex > -1){
			firstDeviceType = deviceType.substring(0,slashIndex).toLowerCase() ;
		}else{
			firstDeviceType = deviceType.toLowerCase() ;
		}
		String iconPathPrefix = "/img/icons/asset/" ;
		if(ObjectUtils.equalsAny(firstDeviceType,"ips","ids", "firewall","router","switch","audit","antivirus","antidos","flow","os")){
			return iconPathPrefix + firstDeviceType+"_48.png" ;
		}else{
			return iconPathPrefix+"default_48.png" ;
		}
	}
	
	public static String getNodeIcon48(String nodeType){
		return "/img/icons/asset/node_"+nodeType+"_48.png" ;
	}
	
	/**
	 * 返回指定操作系统类型图标样式
	 * @param os操作系统类型
	 * @return
	 */
	public static String getIconClsByOSType(String osType){
		return null ;
	}
	/**
	 * 根据资产状态，返回对应的资产状态图标
	 * @param state
	 * @return
	 */
	public static String getIconClsByState(AssetState state){
		if (state == null) {
			return "icon-status-unknow" ;
		}
		switch (state) {
			case ONLINE:return "icon-status-online";
			case OFFLINE:return "icon-status-offline" ;
			case UNKNOW:return "icon-status-unknow" ;
			default : return "icon-status-unknow" ;
		}
	}
	
	/**
	 * 返回指定操作系统的图标样式
	 * @param os 操作系统
	 * @return
	 */
	public static String getIconClsByOS(String os){
		if(os==null){
			return "icon-os-default" ;
		}
		os = os.toLowerCase() ;
		if(os.contains("win")||os.contains("window")){
			return "icon-os-win" ;
		}else if(os.contains("redhat")||os.contains("red hat")){
			return "icon-os-redhat" ;
		}else if(os.contains("freebsd")){
			return "icon-os-linux" ;
		}else if(os.contains("linux")){
			return "icon-os-linux" ;
		}else if(os.contains("ubuntu")){
			return "icon-os-linux" ;
		}else if(os.contains("debian")){
			return "icon-os-linux" ;
		}else if(os.contains("switch")){
			return "icon-os-switch" ;
		}else if(os.contains("router")){
			return "icon-os-router" ;
		}else if(os.contains("unix")){
			return "icon-os-unix" ;
		}else if(os.contains("vmware")){
			return "icon-os-vmware" ;
		}else if(os.contains("mac")){
			return "icon-os-mac" ;
		}
		return "icon-os-default" ;
	}
	/**
	 * 返回指定操作系统的图标样式
	 * @param os 操作系统
	 * @return
	 */
	public static String getIconClsByOS(OsPlatform os){
		if(os==null){
			return getIconClsByOS((String)null) ;
		}
		return getIconClsByOS(os.getOsName()) ;
	}
	/**
	 * 根据资产状态、获取相应的状态图标
	 * @param state
	 * @return
	 */
	public static Object getStateIcon(AssetState state) {
		switch (state) {
			case ONLINE:
				return "/img/icons/status-1.png" ;
			case OFFLINE:
				return "/img/icons/status-4.png" ;
			case UNKNOW:
				return "/img/icons/status-3.png" ;
			default:
				return "/img/icons/status-3.png" ;
		}
	}
	/**
	 * 根据资产状态、获取相应的状态图标
	 * @param state
	 * @return
	 */
	public static Object getStateIconCls(AssetState state) {
		switch (state) {
			case ONLINE:
				return "icon-status-online" ;
			case OFFLINE:
				return "icon-status-offline" ;
			case UNKNOW:
				return "icon-status-unknow" ;
			default:
				return "icon-status-unknow" ;
		}
	}	
	/**
	 * 方法返回操作系统名称
	 * @return 操作系统名称
	 */
	public static List<String> getOsList(){
		List<String> resultList=new ArrayList<String>();
		resultList.add( "Debian") ;
		resultList.add( "Mac OS") ;
		resultList.add( "Unix") ;
		resultList.add( "RedHat") ;
		resultList.add( "Ubuntu") ;
		resultList.add( "VMware EXSi Server") ;
		resultList.add( "Cisco Switch") ;
		resultList.add( "Cisco Router") ;
		resultList.add( "H3C Switch") ;
		resultList.add( "H3C Router") ;
		resultList.add( "FreeBSD") ;
		resultList.add( "Linux") ;
		resultList.add( "Windows Server 2012") ;
		resultList.add( "Windows Server 2008") ;
		resultList.add( "Windows Server 2003") ;
		resultList.add( "Windows Server 2000") ;
		resultList.add( "Windows 8") ;
		resultList.add( "Windows 7") ;
		resultList.add( "Windows Vista") ;
		resultList.add( "Windows XP") ;
		resultList.add( "其它") ;
		return resultList;
	}
	
	public static List<String> getSafeRank(){
		List<String> resultList=new ArrayList<String>(3);
		resultList.add("高") ;
		resultList.add("中") ;
		resultList.add("低") ;
		return resultList;
	}
}
