package com.topsec.tsm.sim.sysconfig.web;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.topsec.tsm.node.detect.Comparator;
import com.topsec.tsm.node.detect.Group;
import com.topsec.tsm.sim.event.EventRuleDispatch;

public class CorrRuleBuilder {
	
	private EventRuleDispatch dispatch;
	
	public CorrRuleBuilder(EventRuleDispatch dispatch) {
		super();
		this.dispatch = dispatch;
	}

	public CorrRuleBuilder() {
	}

	public EventRuleDispatch getDispatch() {
		return dispatch;
	}


	public void setDispatch(EventRuleDispatch dispatch) {
		this.dispatch = dispatch;
	}

	public void rebuild(Group group){
		
		String comparatorTemplate = dispatch.getComparatorTemplate();
		try {
			if(comparatorTemplate!=null&&comparatorTemplate.length()!=0){
				Document document = DocumentHelper.parseText(comparatorTemplate);
				Element root = document.getRootElement();
				if(root!=null){
					buildComparator(root,group);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
	}


	private void buildComparator(Element root,Group group) {
		List<Element> comparators = root.elements("comparator");
		if(comparators!=null){
			List<Comparator> comparatorsList=new ArrayList<Comparator>();
			for (Element element : comparators) {
				String func = element.attributeValue("func");
				String field=element.attributeValue("field");
				comparatorsList.add(new Comparator(func,field)); 
			}
			group.setComparators(comparatorsList);
		}
	} 
}
