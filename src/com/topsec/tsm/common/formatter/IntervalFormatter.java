package com.topsec.tsm.common.formatter;

import com.topsec.tal.base.log.stat.StatInterval;
import com.topsec.tal.base.log.stat.StatUtil;

public class IntervalFormatter implements PropertyFormatter{

	@Override
	public Object format(Object value) {
		StatInterval interval = StatUtil.getInterval((String)value) ;
		if(interval != null){
			return interval.getLabel() ;
		}
		return "UNKNOW" ;
	}
	
}
