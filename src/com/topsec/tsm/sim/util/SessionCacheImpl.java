package com.topsec.tsm.sim.util;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.framework.interfaces.TypeDef;
import com.topsec.tsm.framework.selector.ParseException;
import com.topsec.tsm.framework.selector.Selector;
import com.topsec.tsm.framework.selector.Selectorable;
import com.topsec.tsm.framework.util.TypeDefFactory;
import com.topsec.tsm.resource.SystemDefinition;


public class SessionCacheImpl{
	private String filterStr = "";
	private TypeDef typedef = null;
	private Selectorable s = null;
	private	SessionCache cache = new SessionCache();

	public SessionCacheImpl(String filter){
		typedef = TypeDefFactory.createInstance(SystemDefinition.DEFAULT_CONF_DIR + "typedef.xml");
		filterStr = filter==null ?  "" : filter;
	}

	public String getFilterStr() {
		return this.filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
		if(!filterStr.equals(filterStr)){
			cache.removeCach() ;
		}
	}
	
	public SessionCache getCach() {
		return this.cache;
	}

	public void setCach(SessionCache cach) {
		this.cache = cach;
	}

	public void removeCach() {
		cache.removeCach();
	}
	
	public List<Map> filterMessage(List messages){
		List msg = messages;
		if(filterStr!=null&&!"".equals(filterStr)){
			try {
				s = new Selector(typedef, null).createSelector(this.filterStr);
				msg = s.select(messages);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
}
