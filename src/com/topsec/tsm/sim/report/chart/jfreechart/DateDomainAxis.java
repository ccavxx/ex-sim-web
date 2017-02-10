package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.Graphics2D;
import java.util.Date;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

import com.topsec.tal.base.util.StringUtil;

/**
 * 日期类型坐标轴
 * 此类指定的日期匹配格式去显示坐标轴数据，只有与匹配格式想匹配的坐标轴信息才会显示
 * 显示格式使用pattern指定
 * 
 * @author hp
 *
 */
public class DateDomainAxis extends CategoryAxis{
	
	
	/**
	 * 要显示日期格式
	 */
	private String pattern ;
	/**
	 * 要匹配的日期格式，只有与此格式匹配的坐标信息才会显示
	 */
	private String matchPattern ;

	/**
	 * 该类使用matchPattern与category进行匹配，只有匹配成功的category才会显示，显示的日期格式为pattern指定的日期格式
	 * @param matchPattern 要匹配的格式
	 * @param pattern　显示的日期格式
	 */
	public DateDomainAxis(String matchPattern, String pattern) {
		this.pattern = pattern;
		this.matchPattern = matchPattern;
	}

	@Override
	protected TextBlock createLabel(Comparable category, float width,RectangleEdge edge, Graphics2D g2) {
		if(category instanceof String){
			String dateString = (String)category ;
			if(StringUtil.matchPattern(dateString, matchPattern)){
				Date date = StringUtil.toDate(dateString, "yyyy-MM-dd HH:mm:ss") ;
				return super.createLabel(StringUtil.dateToString(date, pattern), pattern.length()*10, edge, g2) ;//宽度是根据要显示的字符个数来计算，每个字符按10像素定义
			}else{
				return super.createLabel("", 0, edge, g2) ;
			}
		}else{
			return super.createLabel(category, width, edge, g2);
		}
	}
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getMatchPattern() {
		return matchPattern;
	}
	public void setMatchPattern(String matchPattern) {
		this.matchPattern = matchPattern;
	}
}

