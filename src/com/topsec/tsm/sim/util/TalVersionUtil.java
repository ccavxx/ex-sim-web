/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-12-06
* @version 1.0
* 
*/
package com.topsec.tsm.sim.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.jmx.JMXWrapper;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.deployment.core.BasicServicesDeployerMBean;
import com.topsec.tsm.deployment.system.CheckResource;
import com.topsec.tsm.license.util.LicenceStateConstants;

/**
* 功能描述: Tal3.1 版本控制类
*/
public class TalVersionUtil {
	
	public static final String TAL_VERSION_ENTERPRISE="ENTERPRISE";  //企业版
	public static final String TAL_VERSION_STANDARD="STANDARD";  //标准版(去掉事件,审计对象)
	public static final String TAL_VERSION_SIMPLE="SIMPLE";  //简版(只支持天融信设备的日志源)
	public static final String TAL_VERSION_SIM="SIM";  //sim版, 加入漏扫,资产等模块
	
	private static String talVersion;
	
	private static TalVersionUtil talVersionUtil;
	
	private static final String path="../../../../conf/";
	
	private static final Logger log = LoggerFactory.getLogger(TalVersionUtil.class);
	
	private TalVersionUtil(){
		
	}
	public static String getVersionInfo(){
		String endpoint ;
		IpAddress addr = IpAddress.getLocalIp();
		if(addr.isIpv4Compatible())
			endpoint = new StringBuilder().append("jnp://localhost:").append(CheckResource.JNDI_PORT).toString();
		else
			endpoint = new StringBuilder().append("jnp://[").append(addr.toString()).append("]").toString();
		String haspID = "未知";
		String expireTime = null;
		String max_tal_num = "0";
		String license_valid = null;
		String license_error = null;
		try {
			JMXWrapper jmxWrapper = new JMXWrapper(endpoint, "topsec.deployment:type=ServicesDeployer");

			BasicServicesDeployerMBean mbean = jmxWrapper.getProxy(BasicServicesDeployerMBean.class);
			String ret = mbean.getInfo("Test");

			String ttt[] = ret.substring(1, ret.length() - 1).split(",");
			for (int i = 0; i < ttt.length; i++) {

				if (ttt[i].trim().indexOf("EXPIRE_TIME") != -1) {
					expireTime = ttt[i].split("=")[1];
				}
				if (ttt[i].trim().indexOf("TSM_ASSET_NUM") != -1) {
					max_tal_num = ttt[i].split("=")[1];
				}
				if (ttt[i].trim().indexOf("HASP_ID") != -1) {
					haspID = ttt[i].split("=")[1];
				}
				if (ttt[i].trim().indexOf("LICENSE_VALID") != -1) {
					license_valid = ttt[i].split("=")[1];
				}
				if (ttt[i].trim().indexOf("LICENSE_STATE") != -1) {
					license_error = ttt[i].split("=")[1];
				}
			}
		} catch (Exception e) {

			System.err.println(e.toString());

		}
		if (license_valid == null) {
			haspID = "未知";
			expireTime = "未知";
			max_tal_num = "0(系统异常)";
		} else if (license_valid.equals("0")) {
			haspID = "未知";
			expireTime = "未知";
			if (license_error != null) {
				if (license_error.equals(LicenceStateConstants.LICENCE_FILE_INVALID)) {
					max_tal_num = "0(文件错误)";
				} else {
					max_tal_num = "0(License Key 异常)";
				}
			}
		}
		if("-1".equals(haspID)&&expireTime!=null){
			return "试用版 (剩余天数:"+expireTime+")";
		}
		return "";
		
	}
	
	public synchronized static TalVersionUtil getInstance(){
		if(talVersionUtil==null){
			talVersionUtil=new TalVersionUtil();
		}
		return talVersionUtil;
	}
	 
	
	/**
	* @method: getVersion 
	* 			从License中得到系统版本信息
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:  
	* @return: String 系统版本
	*/
	public String getVersion(){
		Map licenceMap=LicenceServiceUtil.getInstance().getLicenseInfo();
		talVersion=(String)licenceMap.get("TAL_VERSION");
		if(talVersion==null){
			talVersion=TAL_VERSION_STANDARD;
		}
		
		return talVersion;
	}
	
	
	/**
	* @method: readVersionFile 
	* 			读取conf目录下buildVersion.xml文件 
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param: 
	* @return: Element 根节点
	*/
	public synchronized Element readVersionFile(){
		SAXReader sAXReader=new SAXReader();
		Document doc = null;
		try {
			doc = sAXReader.read(path+"buildVersion.xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		} 
	    Element element=doc.getRootElement();
		return element;
	}
	
	/**
	* @method: getBuildVersion 
	* 			读取conf目录下buildVersion.xml文件,得到上一次系统版本信息
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param: 
	* @return: buildVersion 上一次系统版本信息
	*/
	public synchronized String getBuildVersion(){
		Element readVersionFile = readVersionFile();
		String buildVersion = readVersionFile.elementTextTrim("buildVersion");
		if(buildVersion==null||buildVersion.equals("")){
			String version = getVersion();
			writeVersionFile(version,"buildVersion");
			buildVersion=version;
		}
		
		return buildVersion;
	}
	
	/**
	* @method: writeVersionFile 
	* 			把新的版本信息覆盖到配置文件中.
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param: newBuildVersion: 新的版本信息
	* @return:  null
	*/
	public synchronized void writeVersionFile(String newBuildVersion,String key){
		Validate.notEmpty(newBuildVersion); 
		Element readVersionFile = readVersionFile();
		Element buildVersion = readVersionFile.element(key);
		buildVersion.setText(newBuildVersion);
		
		System.out.println("");
		XMLWriter writer=null;
		try {
			writer=new XMLWriter(new FileWriter(new File(path+"buildVersion.xml")));
			writer.write(readVersionFile);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
