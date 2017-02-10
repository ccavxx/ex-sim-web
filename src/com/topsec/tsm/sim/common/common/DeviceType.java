package com.topsec.tsm.sim.common.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.common.bean.ChartType;
import com.topsec.tsm.sim.common.bean.Type;

public class DeviceType extends Thread {

	private static Logger logger = LoggerFactory.getLogger(DeviceType.class) ;
	private static DeviceType instance = null;
	private static long checksum = 0;
	
	private Map<String,Type> deviceTypes = new HashMap<String,Type>();
	
	public synchronized void addType(Type type){
		deviceTypes.put(type.getId(),type);
	}
	public synchronized Object[] getTypeKeys(){
		return deviceTypes.keySet().toArray();
	}
	public synchronized Type getType(Object key){
		return deviceTypes.get(key);
	}
	
	@Override
	public void run() {
		while(true){
			try{
				File f = new File(SystemDefinition.DEFAULT_CONF_DIR+ "deviceType.xml");
				long currentCRC = FileUtils.checksumCRC32(f);
				if(currentCRC != checksum){
					synchronized(deviceTypes){
						deviceTypes.clear();
						init();
					}
					checksum = currentCRC;
				}
				sleep(5000);
			}catch(Exception e){
				logger.error("Load 'deviceType.xml' exception!", e) ;
			}
		}
	}
	private DeviceType()
	{
		init();
		try {
			checksum = FileUtils.checksumCRC32(new File(SystemDefinition.DEFAULT_CONF_DIR +"deviceType.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
	}
	public synchronized static DeviceType getInstance()
	{
		if(instance==null){
			instance = new DeviceType();			
		}
		return instance;
	}
	public void init()
	{
		String fw=System.getProperty("FW");
		if(fw==null||!fw.equals("true")){
			Digester digester = new Digester();
			digester.setValidating(false);
			digester.setClassLoader(this.getClass().getClassLoader());
			digester.push(this);
			
			digester.addObjectCreate("*/type",Type.class);
			digester.addSetProperties("*/type");
			digester.addSetNext("*/type", "addType");
			
			digester.addObjectCreate("*/type/chartType",ChartType.class);
			digester.addSetProperties("*/type/chartType");
			digester.addSetNext("*/type/chartType", "addChartType");
			
			InputStream in;
			try {
				in = new FileInputStream(SystemDefinition.DEFAULT_CONF_DIR +"deviceType.xml");
				digester.parse(in);
			}catch (Exception e){
				logger.error("Digester parse deviceType.xml error!",e);
			}
		}else{
			Type syslog=new Type();
			syslog.setId("Syslog.Collector");
			syslog.setName("Syslog设备支持表");   
			String idp=System.getProperty("IDP","false");
			if(idp.equalsIgnoreCase("true")){
				ChartType firewallTopsecTOS = new ChartType();
				firewallTopsecTOS.setId("Ips/Topsec/TopsecIDP-V1");
				firewallTopsecTOS.setName("入侵防御");
				syslog.addChartType(firewallTopsecTOS);
			}else{
				ChartType firewallTopsecTOS=new ChartType();
				firewallTopsecTOS.setId("Firewall/Topsec/TOS");
				firewallTopsecTOS.setName("天融信防火墙");
				syslog.addChartType(firewallTopsecTOS);
			}			
			String bmj=System.getProperty("BMJ");
			if(bmj == null) {
				ChartType ipsTopsecTopsecIDP=new ChartType();
				ipsTopsecTopsecIDP.setId("Ips/Topsec/TopsecIDP");
				ipsTopsecTopsecIDP.setName("天融信入侵防御系统");
				
				ChartType idsTopsecNGIDS=new ChartType();
				idsTopsecNGIDS.setId("Ids/Topsec/NGIDS");
				idsTopsecNGIDS.setName("天融信入侵检测");
				
				ChartType idsTopsecNGIDSV1=new ChartType();
				idsTopsecNGIDSV1.setId("Ids/Topsec/NGIDS V1.0");
				idsTopsecNGIDSV1.setName("天融信入侵检测V1.0");
				
				ChartType vpnTopsecsslvpn=new ChartType();
				vpnTopsecsslvpn.setId("Vpn/Topsec/sslvpn");
				vpnTopsecsslvpn.setName("天融信VPN");
				
				ChartType antivirusTopsecTopsecavse=new ChartType();
				antivirusTopsecTopsecavse.setId("Antivirus/Topsec/Topsec avse");
				antivirusTopsecTopsecavse.setName("天融信病毒网关");
				
				ChartType uTMTopsecTOS=new ChartType();
				uTMTopsecTOS.setId("UTM/Topsec/TOS");
				uTMTopsecTOS.setName("天融信安全网关");
				
				syslog.addChartType(ipsTopsecTopsecIDP);
				syslog.addChartType(idsTopsecNGIDS);
				syslog.addChartType(idsTopsecNGIDSV1);
				syslog.addChartType(vpnTopsecsslvpn);
				syslog.addChartType(antivirusTopsecTopsecavse);
				syslog.addChartType(uTMTopsecTOS);
			}
			
			Type system=new Type();
			system.setId("System");
			system.setName("系统日志");
			
			ChartType esmTopsecSystemLog=new ChartType();
			esmTopsecSystemLog.setId("Esm/Topsec/SystemLog");
			esmTopsecSystemLog.setName("审计系统");
			
			ChartType esmTopsecSystemRunLog=new ChartType();
			esmTopsecSystemRunLog.setId("Esm/Topsec/SystemRunLog");
			esmTopsecSystemRunLog.setName("系统日志");
			 			
			system.addChartType(esmTopsecSystemLog);
			system.addChartType(esmTopsecSystemRunLog);
			
			if (bmj != null) {
				if(idp.equalsIgnoreCase("true"))
				{
					ChartType firewallMgmtTopsecTOS = new ChartType();
					firewallMgmtTopsecTOS.setId("Firewall/Topsec/TOSMGMT");
					firewallMgmtTopsecTOS.setName("入侵防御");
					system.addChartType(firewallMgmtTopsecTOS);
				}
				else
				{
					ChartType firewallMgmtTopsecTOS = new ChartType();
					firewallMgmtTopsecTOS.setId("Firewall/Topsec/TOSMGMT");
					firewallMgmtTopsecTOS.setName("天融信防火墙");
					system.addChartType(firewallMgmtTopsecTOS);
				}
			}
			this.addType(syslog);
			this.addType(system);
		}
	}
	
	public static void main(String args[])
	{
		DeviceType ccc = DeviceType.getInstance();
		Object keys[] = ccc.getTypeKeys();
		for(int i=0;i<keys.length;i++){
			Type cat = ccc.getType(keys[i]);
			System.out.println("--type---"+ cat.getId()+ "----------------"+ cat.getName());
			for(int j=0;j<cat.getChartTypeSize();j++){
				ChartType f = cat.getChartType(j);
				System.out.println("----------------"+f.getId()+"----"+f.getName());
			}
		}
	}
}
