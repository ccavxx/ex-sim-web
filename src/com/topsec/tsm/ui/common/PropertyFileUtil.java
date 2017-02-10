package com.topsec.tsm.ui.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.util.TalVersionUtil;


/**
 * @author wan_ke
  */
public class PropertyFileUtil {
	
	public static String getValue(String name){
		String company=null;
		if(!TalVersionUtil.TAL_VERSION_SIM.equals(TalVersionUtil.getInstance().getVersion())){
			company="upLoad.properties";
		}else{
			company="upLoad_SIM.properties";
		}
		
		InputStream in=null;
		try {
			in = new BufferedInputStream(new FileInputStream(SystemDefinition.DEFAULT_CONF_DIR+company));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Properties p=new Properties();
		try {
			p.load(in);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String key = null;
		try {
			key = (String)p.get(name);
		} catch (Exception e) {		
		}
		return key;
	}	
	public static void setValue(String key,String value){
		String company=null;
		if(!TalVersionUtil.TAL_VERSION_SIM.equals(TalVersionUtil.getInstance().getVersion())){
			company="upLoad.properties";
		}else{
			company="upLoad_SIM.properties";
		}
		try {
			Properties p=new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(SystemDefinition.DEFAULT_CONF_DIR+company));
			p.load(in);
			FileOutputStream out   =   new   FileOutputStream(SystemDefinition.DEFAULT_CONF_DIR+company,false);   
			p.setProperty(key, value);
			p.store(out, null);
		} catch (Exception e) {		
		}
	}	
}
