package com.topsec.tsm.sim.report.util;

import java.util.Date;
import java.util.List;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;

public class ChartCategoryFormatter {

	public final static long ONE_DAY_MILLISECONDS = 86400000 ;//一天的毫秒数
	/**
	 * ScollArea2D中category显示格式化
	 * 根据category中的数据,重新计算要显示的category标签
	 * 并根据category数据返回ScrollArea2D可视化区域可见的数据点数量
	 * @param categoryList
	 */
	public static int formatCategoryList(List<String> categoryList){
		int numVisiblePlot = 0 ;
		if(categoryList.size()>0){
			Date beginDate = StringUtil.toDate(categoryList.get(0),"yyyy-MM-dd HH:mm:ss") ;
			Date endDate = StringUtil.toDate(categoryList.get(categoryList.size()-1),"yyyy-MM-dd HH:mm:ss") ;
			long timeMinus = endDate.getTime() - beginDate.getTime() ;//起止日期时间差
			if(greaterThanOneSeason(timeMinus)){//大于一季按照一年处理,只显示每个月1号的标签
				numVisiblePlot = 100 ;
				for (int i=0;i<categoryList.size();i++) {
					String dayString = categoryList.get(i).substring(8,10) ; //截取日信息
					if(dayString.equals("01")){
						categoryList.set(i, categoryList.get(i).substring(0,10)) ;
					}else{
						categoryList.set(i, "") ;//如果不是每个月的1号,则不显示该标签
					}
				}
			}else if(greaterThanOneMonth(timeMinus)){//大于31天按季处理,只显示1号和15号的标签
				numVisiblePlot = 75 ;//显示75个数据点
				for (int i=0;i<categoryList.size();i++) {
					String dayString = categoryList.get(i).substring(8,10) ; //截取日信息
					if(ObjectUtils.equalsAny(dayString, "01","15")){
						categoryList.set(i, categoryList.get(i).substring(0,10)) ;//截取到日
					}else{
						categoryList.set(i, "") ;//如果不是每个月的1或者15号,则不显示该标签
					}
				}
			}else if(greaterThanOneWeek(timeMinus)){//大于7天按照一个月处理,显示每天0点的标签
				numVisiblePlot = 15 ;
				for (int i=0;i<categoryList.size();i++) {
					String timeString = categoryList.get(i).substring(11) ; //截取时分秒信息
					if(timeString.equals("00:00:00")){
						categoryList.set(i, categoryList.get(i).substring(0,10)) ;//截取到日
					}else{
						categoryList.set(i, "") ;//如果不是每个月的1或者15号,则不显示该标签
					}
				}
			}else if(greaterThanOneDay(timeMinus)){//大于1天,只显示每天00点数据
				numVisiblePlot = 28 ;
				for (int i=0;i<categoryList.size();i++) {
					String timeString = categoryList.get(i).substring(11) ; 
					if(timeString.equals("00:00:00")){
						categoryList.set(i, categoryList.get(i).substring(0,10)) ;
					}else{
						categoryList.set(i, "") ;//如果不是每个月的1或者15号,则不显示该标签
					}
				}
			}else{//否则按照一天数据处理,不显示日信息,只显示整点
				numVisiblePlot = 75 ;
				for (int i=0;i<categoryList.size();i++) {
					String minString = categoryList.get(i).substring(14) ; 
					if(ObjectUtils.equalsAny(minString, "00:00")){
						categoryList.set(i, categoryList.get(i).substring(11,13)) ;//截取整点信息
					}else{
						categoryList.set(i, "") ;//如果不是每个月的1或者15号,则不显示该标签
					}
				}
			}
		}
		return numVisiblePlot ;
	}
	/**
	 * 大于一季度(按92天算)
	 * @param timeMinus 时间差
	 * @return
	 */
	public static boolean greaterThanOneSeason(long timeMinus){
		return timeMinus>92*ONE_DAY_MILLISECONDS ; 
	}
	/**
	 * 大于一个月(31天计算)
	 * @param timeMinus
	 * @return
	 */
	public static boolean greaterThanOneMonth(long timeMinus){
		return timeMinus>31*ONE_DAY_MILLISECONDS ;
	}
	/**
	 * 大于1周
	 * @param timeMinus
	 * @return
	 */
	public static boolean greaterThanOneWeek(long timeMinus){
		return timeMinus>7*ONE_DAY_MILLISECONDS ;
	}
	/**
	 * 大于一天
	 * @param timeMinus
	 * @return
	 */
	public static boolean greaterThanOneDay(long timeMinus){
		return timeMinus>1*ONE_DAY_MILLISECONDS ;
	}
	/**
	 * 大于一小时
	 * @param timeMinus
	 * @return
	 */
	public static boolean greaterThanOneHour(long timeMinus){
		return timeMinus > 60*60*1000 ;
	}
}
