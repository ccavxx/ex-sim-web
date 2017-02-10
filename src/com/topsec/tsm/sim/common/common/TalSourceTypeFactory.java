package com.topsec.tsm.sim.common.common;

import java.util.HashMap;
import java.util.Map;

import com.topsec.tsm.sim.common.bean.ChartType;
import com.topsec.tsm.sim.common.bean.Type;

public class TalSourceTypeFactory {
	public static TalSourceTypeFactory getInstance(){
		return factory;
	}
	
	private TalSourceTypeFactory(){}
	
	public static TalSourceTypeFactory factory = new TalSourceTypeFactory();
	
	public String getSubMoudleName(String sourceType){
		if(!bTag)
			init();
		
		return typeMap.get(sourceType);
	}
	
	public String getTypeName(String sourceType){
		if(!bTag)
			init();
		String name = nameMap.get(sourceType);
			return name;
	}
	
	public synchronized void init(){		
		typeMap = new HashMap<String, String>();
		nameMap = new HashMap<String, String>();
		
		DeviceType dt = DeviceType.getInstance();
		Object keys[] = dt.getTypeKeys();
		
		for(int i=0;i<keys.length;i++){
			Type cat = dt.getType(keys[i]);
			String subModuleName = cat.getId();

			for(int j=0;j<cat.getChartTypeSize();j++){
				ChartType f = cat.getChartType(j);
				typeMap.put(f.getId(), subModuleName);
				nameMap.put(f.getId(), f.getName());
			}
		}
		bTag = true;
	}
	Map<String, String> typeMap = null;
	Map<String, String> nameMap = null;
	boolean bTag = false;
}
