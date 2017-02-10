/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-08-14
* @version 1.0
* 
*/
package com.topsec.tsm.sim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.license.LicenseCheckUtil;
import com.topsec.license.util.LicenceFileAndMapConvertUtil;
import com.topsec.tal.base.jmx.JMXWrapper;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.deployment.core.BasicServicesDeployerMBean;
import com.topsec.tsm.deployment.system.CheckResource;
import com.topsec.tsm.sim.common.bean.Result;

public class LicenceServiceUtil{
	public final static String LICENSE_PATH = "../../../../TopAnalyzer/app-server/server/bin/";
	public final static String LICENSE_PATH_FROM = "../../../../TopAnalyzer/app-server/server/server/default/tmp/";
	private static final Logger log = LoggerFactory.getLogger(LicenceServiceUtil.class);
	
	private static LicenceServiceUtil licenceServiceUtil;
	
	public static String endpoint;
	{
		IpAddress addr = IpAddress.getLocalIp();
		if(addr.isIpv4Compatible())
			endpoint = new StringBuilder().append("jnp://localhost:").append(CheckResource.JNDI_PORT).toString();
		else
			endpoint = new StringBuilder().append("jnp://[").append(addr.toString()).append("]:").append(CheckResource.JNDI_PORT).toString();
	}
	
	private LicenceServiceUtil(){
		
	}
	
	public synchronized static LicenceServiceUtil getInstance(){
		if(licenceServiceUtil==null){
			licenceServiceUtil=new LicenceServiceUtil();
		}
		return licenceServiceUtil;
	}
	
	/**
	* @method: getLicenseInfo 
	* 			得到licence信息
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @return: Map
	* @exception: Exception
	*/
	@SuppressWarnings("unchecked")
	public Map getLicenseInfo(){
		
		Map map=new HashMap();
		String maxDataSourceLicenseNum=null;
		String expireTime=null;
		String haspID=null;
		String license_valid=null;
		String license_error=null;
		String productType = null;
		
		try {
	  		JMXWrapper jmxWrapper = new JMXWrapper(endpoint, "topsec.deployment:type=ServicesDeployer");
            
	  		BasicServicesDeployerMBean mbean = jmxWrapper.getProxy(BasicServicesDeployerMBean.class);
	        
	        String ret = mbean.getInfo("Test");
	    
	        String ttt[]=ret.substring(1, ret.length()-1).split(",");
	        int len=ttt.length;
	        for(int i=0;i<len;i++){
	        	
	        	if(ttt[i].trim().indexOf("TAL_VERSION")!=-1){
	        		productType=ttt[i].split("=")[1];
	        	}
	        	if(ttt[i].trim().indexOf("EXPIRE_TIME")!=-1){
	        		expireTime=ttt[i].split("=")[1];
	        	}
	        	if(ttt[i].trim().indexOf("TSM_ASSET_NUM")!=-1){
	        		maxDataSourceLicenseNum=ttt[i].split("=")[1];
	        	}
	        	if(ttt[i].trim().indexOf("HASP_ID")!=-1){
	        		haspID=ttt[i].split("=")[1];
	        	}
	        	if(ttt[i].trim().indexOf("LICENSE_VALID")!=-1){
	        		license_valid=ttt[i].split("=")[1];
	        	}
	        	if(ttt[i].trim().indexOf("LICENSE_STATE")!=-1){
	        		license_error=ttt[i].split("=")[1];
	        	}
        	}
	        map.put("TSM_ASSET_NUM", maxDataSourceLicenseNum);
	        map.put("EXPIRE_TIME", expireTime);
	        map.put("HASP_ID", haspID);
	        map.put("LICENSE_VALID", license_valid);
	        map.put("LICENSE_STATE", license_error);
	        map.put("TAL_VERSION", productType);
	    } catch (Exception e) {
	    	log.error(e.getMessage());
//	    	e.printStackTrace();
//	        System.err.println(e.toString());
	    }
		return map;
	}
	
	/**
	 * 刷新licence信息
	 */
	public static void refresh(){
		JMXWrapper jmxWrapper = new JMXWrapper(endpoint, "topsec.deployment:type=ServicesDeployer");
		BasicServicesDeployerMBean mbean = jmxWrapper.getProxy(BasicServicesDeployerMBean.class);
		mbean.refresh();
	}
	
	@SuppressWarnings("unchecked")
	public static Result checkLicenseFile(File license,String... productSupportVersions){
		Result result = new Result() ;
		Map<Object, Object> importLicenseMap;
		try {
			importLicenseMap = LicenceFileAndMapConvertUtil.licenceFileToMap(license);
		} catch (Exception e) {
			return result.buildError("无效的许可文件") ;
		}
		boolean importSuccess = false;
		String message = "" ;
		if (importLicenseMap.get("HASPID") != null && !importLicenseMap.get("HASPID").equals("-1")) {
			if (!importLicenseMap.get("HASPID").equals("0")) {
				if (LicenceFileAndMapConvertUtil.mapToHardKey(importLicenseMap) == 0) {
					message = "该许可文件与USB Key不匹配";
				} else {
					LicenceServiceUtil.refresh() ;
					message = "USB Key升级成功，Licence将在3分钟内生效";
				}
			} else {
				try {
					Map<String,Object> currentLicenceInfo = LicenceFileAndMapConvertUtil.licenceFileToMap(new File("license.xml")) ;
					if(currentLicenceInfo == null){
						message = "当前使用的许可文件已损坏" ;
					}else{
						Map<String,Object> importLicenceProductInfo = (Map<String,Object>)importLicenseMap.get("TEXT") ;
						Object[] validVersions = Arrays.copyOf(productSupportVersions, productSupportVersions.length) ;
						if(!ObjectUtils.equalsAny(importLicenceProductInfo.get("TAL_VERSION"), validVersions)){
							message = "该许可文件与系统不匹配" ;
						}else{
							int currentLicenseId = StringUtil.toInt((String)currentLicenceInfo.get("LICENSE_ID"),-1) ;
							int importLicenseId = StringUtil.toInt((String)importLicenseMap.get("LICENSE_ID"),-1) ;
							if(currentLicenseId == importLicenseId){
								message = "该许可文件已经导入 ，不能重复导入" ;
							}else if(currentLicenseId > importLicenseId){
								message = "该许可文件过旧，无法导入系统" ;
							}else{
								if(!LicenseCheckUtil.isValid(importLicenseMap)){
									message = "该许可文件与服务器不匹配";
								}else{
									int ret = LicenceFileAndMapConvertUtil.importSoftLicense(importLicenseMap);
									switch (ret) {
										case  0 : message = "该软许可文件已经损坏";break;
										case -1 : message = "该许可文件与服务器不匹配";break;
										case -2 : message = "该许可文件已经导入过 ，不能重复导入";break;
										default : {
											message = "Licence升级文件将在3分钟内生效" ;
											importSuccess = true;
											LicenceServiceUtil.refresh() ;
											break;
										}
									}
								}
							}
						}
					}
				} catch (FileNotFoundException e) {
					message = "当前使用的许可文件已损坏" ;
				} catch (IOException e) {
					message = "当前使用的许可文件已损坏" ;
				}
			}
		} else {
			message = "无效的许可文件" ;
		}
		return result.build(importSuccess, message) ;
	}
}
