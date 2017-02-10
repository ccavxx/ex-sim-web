package com.topsec.tsm.sim.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.topsec.tal.base.search.SearchObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;

public class LogStatsUtils {
	/**
	 * 雷达图数据组装
	 * @author zhou_xiaohu
	 * @param data
	 * @param searchObject
	 * @return Map<String,Object>
	 */
	public static  List<Map<String,Object>> formatLogChart( Map<String,Object> data,Date start,Date end){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Date endTime = end;
		long span = getSpan(start,end);
		Date date =null;
		long maxShowNumber = 0;
		if(span >1){
			
			if(span ==60 || span ==30){
				date = new Date(endTime.getYear(),endTime.getMonth(),endTime.getDate(),endTime.getHours(),0);
				maxShowNumber = ((endTime.getTime()-start.getTime())/(60000*span))+1;
			}else if(span == 6*60 || span == 12*60 || span == 24*60){
				date = new Date(endTime.getYear(),endTime.getMonth(),endTime.getDate(),0,0);
				maxShowNumber = ((endTime.getTime()-start.getTime())/(60000*span))+1;
			}else{
				long minutes = end.getMinutes()%10;
				if((0<=minutes && minutes <=4) || span ==10){
					minutes = 0;
				}else if(5<=minutes && minutes <=9){
					minutes = 5;
				}
				int time =  end.getMinutes()/10;
				String tempminutes =time+""+minutes;
				date = new Date(endTime.getYear(),endTime.getMonth(),endTime.getDate(),endTime.getHours(),Integer.parseInt(tempminutes));
				maxShowNumber = ((endTime.getTime()-start.getTime())/(60000*span))+1;
			}
			
		}else{
			date = new Date(endTime.getYear(),endTime.getMonth(),endTime.getDate(),endTime.getHours(),endTime.getMinutes());
			maxShowNumber =((endTime.getTime()-start.getTime())/60000)+1;
		}
		while(maxShowNumber-->0){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("x", date.getTime()+"&"+(date.getTime() +60000*span-1000));
			if(data!=null){
				String dateString = StringUtil.longDateString(date) ;
				Long count = (Long) data.get(dateString) ;
				map.put("y", ObjectUtils.nvl(count, new Long(0))) ;
			}
			list.add(map);
			date.setTime(date.getTime()-span*60*1000);
		}
		Collections.reverse(list) ;
		
		return list;
	}
	
	/**
	 * 获取日志区间跨度
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static  long getSpan (Date startTime,Date endTime){
		//最小1分钟一个刻度
		//与前台计算方法一致，前台图形从右向左显示
		long span = ((endTime.getTime()-startTime.getTime())/60000);
		if(span<1)
			span = 1;
		if(span/(24*60)>=60)
			span = 24*60;
		else if(span/(12*60)>=60)
			span = 12*60;
		else if(span/(6*60)>=60)
			span = 6*60;
		else if(span/(60)>=60)
			span = 60;
		else if(span/30>=60)
			span = 30;
		else if(span/10>=60)
			span = 10;
		else if(span/5>=60)
			span = 5;
		else
			span = 1;
		return span;
	}
	
	
}
