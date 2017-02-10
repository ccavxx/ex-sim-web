package com.topsec.tsm.sim.asset.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.AssetState;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.asset.group.GroupStrategy;


/**
 * 网络
 * @author hp
 *
 */
public class SubNet extends AssetGroup implements Serializable,Comparable<SubNet>{
	private Map<String,AssetObject> assetIpMapping = new HashMap<String, AssetObject>();
	/**
	 * 网络地址
	 */
	private String networkAddress ;
	/**
	 * 资产总数
	 */
	private int assetCount ;
	/**
	 * 已经扫描过的资产总数，包括不在线资产
	 */
	private int scannedCount ;
	/**
	 * 子网掩码
	 */
	private int netmask ;
	/**
	 * 扫描节点
	 */
	private String scanHost ;
	
	public SubNet(String groupId){
		super(groupId) ;
	}
	
	public SubNet(String scanHost,String networkAddress,int netmask,int assetCount) {
		super(generateId(scanHost,networkAddress, netmask)) ;
		this.networkAddress = networkAddress ;
		this.netmask = netmask ;
		this.assetCount = assetCount ;
		this.scanHost = scanHost ;
	}
	
	public SubNet(String scanHost,String networkAddress, int netmask) {
		this(scanHost,networkAddress,netmask,-1) ;
	}

	@Override
	public synchronized void addAsset(AssetObject obj) {
		String assetIp = obj.getMasterIp().toString() ;
		if(assetIpMapping.containsKey(assetIp)){
			return ;
		}
		scannedCount++ ;
		//只保留在线的主机
		if(obj.getState() == AssetState.ONLINE){
			assetIpMapping.put(assetIp, obj) ;
			obj.setId(assetIp) ;
			super.addAsset(obj) ;
		}
	}	
	
	@Override
	public synchronized void clear() {
		super.clear();
		assetIpMapping.clear() ;
		scannedCount = 0;
	}

	@Override
	public int compareTo(SubNet o) {
		Integer[] addr = StringUtil.toInteger(StringUtil.split(this.networkAddress,"\\.")) ;
		Integer[] addr1 = StringUtil.toInteger(StringUtil.split(o.networkAddress,"\\.")) ;
		if(addr.length<4){
			return -1 ;
		}else if(addr1.length<4){
			return 1 ;
		}else{
			for (int i = 0; i < 4; i++) {
				if(addr[i].intValue()==addr1[i].intValue()) continue ;
				return addr[i].intValue()-addr1[i].intValue() ;
			}
			return netmask - o.netmask ;
		}
	}
	
	@Override
	public AssetGroup createInstance(String groupId, String name,GroupStrategy childGroupStrategy) {
		return new SubNet(groupId) ;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj){
			return true ;
		}
		if(!(obj instanceof SubNet)){
			return false ;
		}
		SubNet net = (SubNet)obj ;
		return scanHost.equals(net.scanHost) && 
			   networkAddress.equals(net.networkAddress) &&
			   netmask == net.netmask;
	}
	
	@Override
	public int hashCode() {
		return 17+networkAddress.hashCode()*37 + netmask;
	}

	public String getNetworkAddress() {
		return networkAddress;
	}

	public void setNetworkAddress(String networkAddress) {
		this.networkAddress = networkAddress;
	}

	public int getProgress() {
		return assetCount == 0 ? 100 : scannedCount*100/assetCount ;
	}

	public int getScannedCount() {
		return scannedCount;
	}

	public int getAssetCount() {
		return assetCount;
	}

	public void setAssetCount(int assetCount) {
		this.assetCount = assetCount;
	}

	public void setScannedCount(int scannedCount) {
		this.scannedCount = scannedCount;
	}

	public int getNetmask() {
		return netmask;
	}

	public void setNetmask(int netmask) {
		this.netmask = netmask;
	}
	
	public String getScanHost() {
		return scanHost;
	}

	public void setScanHost(String scanHost) {
		this.scanHost = scanHost;
	}

	public static String generateId(String scanHost,String networkAddress,int netmask){
		return scanHost + ":" +networkAddress+"/"+netmask ;
	}
}
