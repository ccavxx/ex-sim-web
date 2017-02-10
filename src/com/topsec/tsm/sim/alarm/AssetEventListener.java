package com.topsec.tsm.sim.alarm;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.event.service.EventService;
import com.topsec.tsm.util.ticker.Tickerable;
/**
 * 资产告警listener
 * 将对应的告警信息加入到相关的资产中
 * @author hp
 *
 */
public class AssetEventListener implements EventListener,Tickerable{

	private String lastDate = StringUtil.currentDateToString("yyyyMMdd");
	
	private void incrementIpEvent(String ip,int count){
		AssetObject asset = AssetFacade.getInstance().getAssetByIp(ip) ;
		if (asset != null) {
			asset.incrementEvent(count);
		}
	}
	
	@Override
	public void onEvent(Map<String, Object> event) {
		IpAddress assetAddress = (IpAddress) event.get(DataConstants.DVC_ADDRESS) ;
		if(assetAddress != null){
			String ip = assetAddress.toString() ;
			incrementIpEvent(ip,1) ;
		}
		IpAddress srcAddress = (IpAddress) event.get(DataConstants.SRC_ADDRESS) ;
		if(srcAddress != null && !srcAddress.equals(assetAddress)){
			String ip = srcAddress.toString() ;
			incrementIpEvent(ip,1) ;
		}
		IpAddress dstAddress = (IpAddress) event.get("DEST_ADDRESS") ;
		if(dstAddress != null && !dstAddress.equals(srcAddress) && !dstAddress.equals(assetAddress)){
			String ip = dstAddress.toString() ;
			incrementIpEvent(ip,1) ;
		}
	}

	@Override
	public void onEvent(List<Map<String, Object>> events) {
		for(Map event:events){
			onEvent(event) ;
		}
	}

	@Override
	public void onTicker(long ticker) {
		String now = StringUtil.currentDateToString("yyyyMMdd") ;
		if(now.compareTo(lastDate)>0){
			lastDate = now ;
			for(AssetObject asset:AssetFacade.getInstance().getAll()){
				asset.setEventCount(0) ;
			}
		}
		
	}

}
