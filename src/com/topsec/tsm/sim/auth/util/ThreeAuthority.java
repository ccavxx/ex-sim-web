package com.topsec.tsm.sim.auth.util;

import java.util.List;
import java.util.Set;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.datasource.SimDatasource;

public class ThreeAuthority implements Authority {

	private SID sid ;
	
	@Override
	public void setSid(SID sid) {
		this.sid = sid ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasAuthority(String host, String type) {
		//auditor只具有审计日志权限
		if(sid.hasAuditorRole()){
			//本地localhost地址(127.0.0.1或者::1)
			String localHostAddress = IpAddress.getLocalIp().getLocalhostAddress() ;
			if(StringUtil.isNotBlank(host) && !host.equals(localHostAddress)){
				return false ;
			}
			return DataSourceUtil.SYSTEM_LOG.equals(type) ;
		}
		if(sid.hasOperatorRole()){
			//operator不具有审计日志权限
			if(DataSourceUtil.SYSTEM_LOG.equals(type)){
				return false ;
			}
			//operator包含除去审计日志权限的所有日志源权限
			if(sid.isOperator()){
				return true ;
			}else if(DataSourceUtil.SYSTEM_RUN_LOG.equals(type)){//非auditor不具有系统日志的权限
				return false ;
			}else{//普通的非operator账号根据权限表进行权限判断
				Set<AuthUserDevice> userDevices = sid.getUserDevice() ;
				if(StringUtil.isNotBlank(host)){
					AssetObject ao = AssetFacade.getInstance().getAssetByIp(host) ;
					return ao != null && userDevices.contains(new AuthUserDevice(ao.getId())) ;
				}else{
					DataSourceService dataSourceService = (DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService") ;
					for(AuthUserDevice ud:userDevices){
						List<SimDatasource> dataSources = dataSourceService.getByIp(ud.getIp()) ;  
						for(SimDatasource ds:dataSources){
							if(ds.getSecurityObjectType().equals(type)){
								return true ;
							}
						}
					}
				}
			}
		}
		return false ;
	}
}
