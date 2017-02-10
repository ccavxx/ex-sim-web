package com.topsec.tsm.sim.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class DeviceTypeNameUtil {
	
	public static String getDeviceTypeName(String key){
		return getDeviceTypeName(key, Locale.getDefault()) ;
	}
	
	public static String getDeviceTypeName(String key,Locale locale){
		key = key.startsWith("Monitor") ? key.substring(7) : key ;
		ResourceBundle rb = ResourceBundle.getBundle("resource.application",locale);
		String resValue = null;
		if(key != null){
			resValue = TalSourceTypeFactory.getInstance().getTypeName(key);
			if(resValue == null){
				try{
//					if("Esm/Topsec/SystemLog".equals(key)){
//						resValue ="审计日志";
//					}else if("Esm/Topsec/SystemRunLog".equals(key)){
//						resValue ="系统日志";
//					}
					if(resValue ==  null){
						if(key.indexOf("/") > 0){
							key = key.substring(0,key.indexOf("/"));
						}
						resValue = rb.getString(key);
					}
				}catch(MissingResourceException me){
					try{
						resValue = key;
						//i18nStr = rb.getString(DEF_DEVICETYPE);
					}catch(MissingResourceException e){
						resValue = key;
					}
				}
			}
		}else{
			//throw new I18nTagNoKeyAttributeException();
			resValue = null;
		}
		return resValue;
	}
	
	public static void main(String[] args) {
		System.out.println(getDeviceTypeName("OS/Microsoft/WindowsEventLog",Locale.getDefault()));
	}
}
