package com.topsec.tsm.common.message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.interfaces.EventListener;
import com.topsec.tsm.util.ticker.Tickerable;
/**
 * 消息记数器<br>
 * 此类会根据dateFormat指定的日期格式来格式化日期<br>
 * 然后使用lastDate与当前的日期做比较，如果当前日期大于最后的事件日期重置记数器<br>
 * 例如:<br>
 * <strong>按天</strong>统计日期格式为:yyyy-MM-dd<br>
 * <strong>按月</strong>统计日期格式为:yyyy-MM<br>
 * <strong>按年</strong>统计日期格式为:yyyy<br>
 * @author hp
 *
 */
public class EventCounter implements EventListener,Tickerable {
	/**日期格式,在记数时，系统会根据日期格式将日期转换为此格式来比较日期的大小*/
	private String dateFormat ="yyyy-MM-dd";
	private String lastDate = StringUtil.currentDateToString(dateFormat);
	private AtomicInteger count = new AtomicInteger(0) ;
	@Override
	public void onEvent(Map<String, Object> event) {
		synchronized (lastDate) {
			count.incrementAndGet() ;
		}
	}

	@Override
	public void onTicker(long mills){
		String now = StringUtil.currentDateToString(dateFormat) ;
		if(now.compareTo(lastDate) > 0){
			synchronized (lastDate) {
				lastDate = now;
				count.set(0);
			}
		}
	}

	@Override
	public void onEvent(List<Map<String, Object>> events) {
		for(Map<String,Object> event:events){
			onEvent(event) ;
		}
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public int getCount() {
		return count.intValue();
	}

	public void add(int value){
		count.addAndGet(value) ;
	}
}
