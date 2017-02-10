package com.topsec.tsm.sim.common.common;

import java.util.ResourceBundle;

public class WebProperty {
public static WebProperty defaultPro = new WebProperty();
	
	static {

		defaultPro.init("resource/application");
	}
	
	public static WebProperty getDefault(){
		return defaultPro;
	}
	
	public boolean hasInit(){
		if(bundle == null)
			return false;
		return true;
	}
	
	public void init(String appResourceBundleName){
		bundle = ResourceBundle.getBundle(appResourceBundleName);
	}
	
	
	public String getString(String name){
		if(bundle == null){
			throw new RuntimeException("[PropertyHelper] not init");
		}
		String key = name;
		try {
			key = bundle.getString(name);
		} catch (Exception e) {		
		}
		return key;
	}	
	
	private ResourceBundle bundle = null;
}
