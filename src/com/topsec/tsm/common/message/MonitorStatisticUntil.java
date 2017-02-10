package com.topsec.tsm.common.message;

import com.topsec.tsm.framework.exceptions.XMLFormatableException;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.statisticor.exception.StatisticException;
import com.topsec.tsm.framework.timeoutstatisticor.TimeoutStatisticor;
import com.topsec.tsm.framework.timeoutstatisticor.metadata.TimeoutStatisticMetas;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.resource.SystemDefinition;
/**
 * 内存统计工具类
 * @author wangxinbing
 *
 */
public class MonitorStatisticUntil {
	private static String datafuctionsFile = SystemDefinition.DEFAULT_CONF_DIR + "datafunctions.xml";
	private static String typedefFile = SystemDefinition.DEFAULT_CONF_DIR + "typedef.xml";
	private static TypeDef typedef =TypeDefFactory.createInstance(typedefFile);
	
	private static TimeoutStatisticor stat;
	
	public static TimeoutStatisticor getTimeoutStatisticorInstance(){
		if(stat == null) {   
		     synchronized(TimeoutStatisticor.class) {   
		       if(stat == null) {   
		    	   try {
					stat = new TimeoutStatisticor(datafuctionsFile,typedef);
				} catch (StatisticException e) {
					e.printStackTrace();
				}   
		       }   
		    }   
		  } 
		return stat;
	}
	
	public static  TimeoutStatisticMetas getStaitsticMetas() {
	    String configFile = SystemDefinition.DEFAULT_CONF_DIR+"monitorstatisticor.xml";
        TimeoutStatisticMetas metas = new TimeoutStatisticMetas();
        try {
            metas.importObjectFromFile(configFile);
        } catch (XMLFormatableException e) {
            e.printStackTrace();
        }
        return metas;
	}

}
