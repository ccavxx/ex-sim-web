package com.topsec.tsm.sim.newreport.handler;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.newreport.util.ResultOperatorUtils;

/**
 * @ClassName: QueryResultFormatImpl
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年9月15日上午10:47:41
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public final class QueryResultFormatImpl implements QueryResultFormat {

	@Override
	public <T> List<T> preprocess(List<T> data, Map<String, Object> structureMap) {
		if (null == data || data.size()==0) {
			return data;
		}
		String[]categorys=(String[])structureMap.get("categorys");
		String[]series=(String[])structureMap.get("series");
		String[]statistical=(String[])structureMap.get("statistical");
		String[]formats=(String[])structureMap.get("formats");
		
		if (null != formats) {
			String[]formatsFileName=(String[])structureMap.get("formatsFileName");
			String[]formatsType=(String[])structureMap.get("formatsType");
			for (Object object : data) {
				if (null ==object) {
					continue;
				}
				List<Map<String, Object>>maps=(List<Map<String, Object>>)object;
				for (int i = 0; i < formats.length; i++) {
					String catFiled=formats[i];
					String resourceFile=formatsFileName[i];
					String catType=formatsType[i];
					for (Map<String, Object> map : maps) {
						Object cObj=ResultOperatorUtils.mapping(resourceFile, map.get(catFiled));
						map.put(catFiled, cObj);
					}
				}
				reStatisticDat(maps,categorys,statistical);
			}
			
		}
		
		return data;
	}

	@Override
	public <T> List<T> showFlowOperater(List<T> data, String units) {
		return null;
	}

	@Override
	public <T> List<T> showNumberOperater(List<T> data, String units) {
		return null;
	}

	@Override
	public <T> List<T> trendOperater(List<T> data, Integer showNum) {
		return null;
	}

	@Override
	public <T> T mapping(String type, Object fromobj) {
		return null;
	}

	@Override
	public <T> Map<T, Long> reducing(List<Map<T, Long>> list,
			Map<T, Long> context) {
		return null;
	}

	private void reStatisticDat(List<Map<String, Object>>maps,String[]categorys,String[]statistical){
		if (null == maps || maps.size()==0) {
			return;
		}
		
		int mapsLen=maps.size();
		for (int j = 0; j < mapsLen-1; j++) {
			Map sMap=maps.get(j);
			for (int k = j+1; k < mapsLen; k++) {
				Map eMap=maps.get(k);
				boolean needReplace=true;
				for (int i = 0; i < categorys.length; i++) {
					if (sMap.get(categorys[i])!=eMap.get(categorys[i])) {
						needReplace=false;
					}
				}
				if (needReplace) {
					for (int i = 0; i < statistical.length; i++) {
						long slong=null==sMap.get(statistical[i])?0L:Long.valueOf(sMap.get(statistical[i]).toString());
						long elong=null==eMap.get(statistical[i])?0L:Long.valueOf(eMap.get(statistical[i]).toString());
						sMap.put(statistical[i], slong+elong);
					}
					maps.remove(k);
					mapsLen=maps.size();
				}
			}
		}
		
		
	}
}
