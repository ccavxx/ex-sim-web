package com.topsec.tsm.common.message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.framework.timeoutstatisticor.TimeoutStatisticor;
import com.topsec.tsm.framework.timeoutstatisticor.metadata.TimeoutStatisticMeta;
import com.topsec.tsm.framework.timeoutstatisticor.metadata.TimeoutStatisticMetas;
/**
 * 内存统计处理接口实现
 * @author wangxinbing
 *
 */
public class MonitorStatisticServiceImpl implements MonitorStatisticService {

	private TimeoutStatisticor stat ;
	
	public MonitorStatisticServiceImpl(){
		stat = MonitorStatisticUntil.getTimeoutStatisticorInstance();
	}
	@Override
	public synchronized void statisticHandler(Map<String, Object> result) {
		
		try {
            TimeoutStatisticMetas metas = MonitorStatisticUntil.getStaitsticMetas();
            for(Iterator it = metas.getStatistics().iterator(); it.hasNext();) {
                TimeoutStatisticMeta timeoutMeta = (TimeoutStatisticMeta)it.next();
                stat.addStatistic(timeoutMeta);
            }
            stat.start(null);
            stat.work(result);
        } catch (Exception e) {
            e.printStackTrace();
            try {
				stat.stop(null);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        }
	}

	@Override
	public synchronized void statisticHandler(List<Map<String, Object>> result) {
		try {
			TimeoutStatisticMetas metas = MonitorStatisticUntil.getStaitsticMetas();
            for(Iterator it = metas.getStatistics().iterator(); it.hasNext();) {
                TimeoutStatisticMeta timeoutMeta = (TimeoutStatisticMeta)it.next();
                stat.addStatistic(timeoutMeta);
            }
            stat.start(null);
            stat.work(result);
        } catch (Exception e) {
            e.printStackTrace();
            try {
				stat.stop(null);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        }
		
	}
}
