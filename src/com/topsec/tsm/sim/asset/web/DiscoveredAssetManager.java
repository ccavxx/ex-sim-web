package com.topsec.tsm.sim.asset.web;

import java.util.ArrayList;
import java.util.List;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.node.component.collector.NmapResultObj;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.group.AssetGroup;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;

/**
 * 此类主要是管理扫描到的资产信息<br>
 * 扫描的资产按子网会被分配到不同的子网中<br>
 * 注意：与真实资产不同，同一资产(相同的ip地址)可能会属于不同的子网（由于客户端扫描时使用了不同的掩码会导致同一资产属于不同的子网）
 * @author hp
 *
 */
public class DiscoveredAssetManager{

	private static DiscoveredAssetManager instance ;
	private SubNet root = new SubNet(null,null,-1,-1) ;
	
	private DiscoveredAssetManager(){
	}
	
	public void addAsset(NmapResultObj scanResult){
		String scanHost = scanResult.getScanHost(); 
		if(scanHost == null){//如果使用的是旧的代理，此属性会为null
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			Node node = nodeMgrFacade.getNodeByNodeId(scanResult.getNodeId()) ;
			if(node == null){
				return ;
			}
			scanHost = node.getIp();
		}
		if(scanHost.equals(IpAddress.getLocalIp().toString())){
			scanHost = IpAddress.getLocalIp().getLocalhostAddress() ;
		}
		String ownerId = SubNet.generateId(scanHost,scanResult.getNetworkAddress(), scanResult.getNetmask()) ;
		AssetObject asset = new AssetObject() ;
		asset.setMasterIp(new IpAddress(scanResult.getIp())) ;
		asset.setMac(scanResult.getMac()) ;
		asset.setState(scanResult.getState()) ;
		asset.setScanNodeId(scanResult.getNodeId()) ;
		SubNet subnet = (SubNet) root.getChildById(ownerId) ;
		if(subnet !=null ){
			subnet.addAsset(asset) ;
		}
	}
	
	/**
	 * 添加子网
	 * @param networkAddress 网址地址
	 * @param netmask 子网掩码
	 */
	public void addSubNet(SubNet subnet){
		if(root.getChildById(subnet.getId()) != null){
			return ;
		}
		root.addChild(subnet) ;
	}
	
	public void removeSubnet(SubNet subnet){
		root.removeChild(subnet) ;
	}
	
	/**
	 * 获取子网信息
	 * @param networkAddress
	 * @param netmask
	 * @return
	 */
	public SubNet getSubNet(String scanHost,String networkAddress,int netmask){
		return (SubNet) root.getChildById(SubNet.generateId(scanHost,networkAddress, netmask)) ;
	}
	
	public List<SubNet> getAll(){
		int length = root.getChildren() != null ? root.getChildren().size() : 0 ;
		List<SubNet> subnets = new ArrayList<SubNet>(length) ;
		for(AssetGroup group:root.getChildren()){
			subnets.add((SubNet) group) ;
		}
		return subnets ;
	}
	
	public static DiscoveredAssetManager getInstance(){
		if(instance==null){
			synchronized (DiscoveredAssetManager.class) {
				if (instance == null) {
					instance = new DiscoveredAssetManager() ;
				}
			}
		}
		return instance ;
	}
}
