package com.topsec.tsm.sim.sysconfig.bean;

import java.util.List;

public class Event {
	private List<Rule> rules;
	private Integer maxRuleSize;
	
	public Integer getMaxRuleSize() {
		return maxRuleSize;
	}

	public void setMaxRuleSize(Integer maxRuleSize) {
		this.maxRuleSize = maxRuleSize;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

}
