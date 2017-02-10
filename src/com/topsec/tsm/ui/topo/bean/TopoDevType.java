package com.topsec.tsm.ui.topo.bean;

import java.util.HashSet;
import java.util.Set;

public class TopoDevType {

	private String key;
	private String name;
	private Set<EvtCol> evtCollectors = new HashSet<EvtCol>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void addEvtCollector(EvtCol o) {
		evtCollectors.add(o);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
  
	public Set<EvtCol> getEvtCollectors() {
		return evtCollectors;
	}

	public void setEvtCollectors(Set<EvtCol> rvtCollectors) {
		this.evtCollectors = rvtCollectors;
	}

	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (!(obj instanceof TopoDevType))
			return false;
		else {
			TopoDevType def = (TopoDevType) obj;
			if (null == this.name || null == name)
				return false;
			else
				return name.equals(def.getName());
		}
	}

	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

}
