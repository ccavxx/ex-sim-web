package com.topsec.tsm.sim.sysconfig.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.node.detect.Condition;
import com.topsec.tsm.node.detect.Result;
import com.topsec.tsm.node.detect.Rule;
import com.topsec.tsm.node.detect.Window;
import com.topsec.tsm.node.detect.function.Column;
import com.topsec.tsm.node.detect.function.Columns;
import com.topsec.tsm.node.detect.function.Parameter;
import com.topsec.tsm.sim.event.EventRule;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.sim.sysconfig.convert.EventRuleConverter;
import com.topsec.tsm.sim.sysconfig.convert.XMLRuleColumnHandler;
import com.topsec.tsm.sim.sysman.bean.EventRuleConfig;

public   class RuleBuilder {
	
	public void rebuildEventRule(EventRule ...ruleEventRules){
		for (int i = 0; i < ruleEventRules.length; i++) {
			Rule rule = convertXMLRule(ruleEventRules[i]);
			Document document = DocumentFactory.getInstance().createDocument();
			Element ele = document.addElement("rule");
			rule.setElementFromObj(ele);
			String xml = ele.asXML();// 生成xml
			ruleEventRules[i].setRuleTemplate(xml);
		}
	}
	
	
	
	public Rule convertXMLRule(EventRule eventRule) {
		EventRuleConverter ruleConverter = createConverter();
		String ruleXML =eventRule.getRuleTemplate();
		ruleConverter.build(ruleXML);
		Rule rule = convertRuleAttrs(ruleConverter);
		return rule;
	}



	public Rule convertRuleAttrs(EventRuleConverter ruleConverter) {
		
		Rule rule = new Rule();
		rule.setId(ruleConverter.getId().toString());
		rule.setVersion(ruleConverter.getVersion());
		rule.setName(ruleConverter.getName());
	 
		//condition
		Condition condition=new Condition();
		List<Map<String, Object>> columnMaps = ruleConverter.getColumns();
		List<Column> funcList = new ArrayList<Column>();
		//columns
		Columns columns = new Columns();
		for (Map<String, Object> columnMap : columnMaps) {
			
			Column column = new Column();
			
			String field=(String) columnMap.get("field");
			String function=(String) columnMap.get("function");
			List<String> parameterValues= (List<String>) columnMap.get("params");
			
			column.setField(field);
			column.setFunc(function);
			
			if (StringUtil.isNotBlank(field) && StringUtil.isNotBlank(function)) {
				funcList.add(column);
				if(parameterValues!=null){
					Parameter param = new Parameter();
					column.setParam(param);
					if(parameterValues.size()==0){
						param.setValue("");
					}
					if(parameterValues.size()==1){
						param.setValue(parameterValues.get(0));
					}
					if (parameterValues.size()==2) {
						String val0=parameterValues.get(0);
						String val1=parameterValues.get(1);
						param.setValue(val0+"θ"+val1);//值分割
					}
					/**
					 * 黑白名单  以后添加
					 */
					
					if(function.toUpperCase().equals("BLACKSOP")){
						param.setValue(getBlacksOp(parameterValues));
					}
					if(function.toUpperCase().equals("WHITESOP")){
						param.setValue(getWhitesOp(parameterValues));
					}
					
				}
			}
		}
		
		columns.setFuncList(funcList);
		condition.setFunctions(columns);
		
		//window
		condition.setWindow(new Window(ruleConverter.getTime()*1000, ruleConverter.getCount(), ruleConverter.getTrigger()));
		
		rule.setCondition(condition);
		
		//result
		Result result = new Result();
		result.setType(ruleConverter.getResultReType());
		result.addResultColumn(EventRuleConverter.RULE_RESULT_ATTR_CUSTOM8, ruleConverter.getCustom8());
		
		rule.setResult(result);
		
		//category
		rule.setBelongGroups(ruleConverter.getDispatchGroups());
		return rule;
	}



	public EventRuleConverter createConverter() {
		EventRuleConverter ruleConverter=new EventRuleConverter();
		ruleConverter.setColumnHandler(new XMLRuleColumnHandler());
		return ruleConverter;
	}
	
	public String getWhitesOp(List<String> parameterValues) {return null;}

	public String getBlacksOp(List<String> parameterValues) {return null;}

	
	
	//---------------------------------------------------------------------------------------------------
	public EventRule  buildEventRuleFromConfig(EventRuleConfig erc) {
		Rule rule=buildRulefromConfig(erc);
		String xml = wrappRuleToXmlTemplate(rule);
		EventRule eventRule=new EventRule();
		eventRule.setName(erc.getEventName());
		eventRule.setCreateTime(new Date());
		eventRule.setRuleTemplate(xml);
		eventRule.setRuleNum("1");
		eventRule.setAlarmState(erc.getIsAlarm());
		return eventRule;
	}
	private String wrappRuleToXmlTemplate(Rule rule) {
		Document document = DocumentFactory.getInstance().createDocument();
		Element ele = document.addElement("rule");
		rule.setElementFromObj(ele);
		String xml = ele.asXML();// 生成xml
		return xml;
	}
	public Rule buildRulefromConfig(EventRuleConfig  erc){
		
		Rule rule = new Rule();
		 
		Condition condition = new Condition();
		List<Column> funcList = genFunctionCfgs(erc);
		Columns columns = new Columns();
		columns.setFuncList(funcList);
		condition.setFunctions(columns);

		//执行频率配置
		Window window=new Window(erc.getTime()*1000, erc.getCount(), "count");//按次数触发
		condition.setWindow(window);
		rule.setId(Integer.toString(1));
		rule.setName(erc.getEventName().replace("\"", "&quot;"));
		rule.setVersion(erc.getVersion());
		rule.setCondition(condition);
		Result result = new Result();
		result.setType(Result.Type_Coverl);
		result.addResultColumn("CUSTOM8", "a:UUID") ;
		rule.setResult(result);

		return rule;
	}
	
	
	private List<Column> genFunctionCfgs(EventRuleConfig erc) {
		List<Column> funcList = new ArrayList<Column>();
		String[] fields = erc.getFields();
		String[] ops = erc.getPropOps();
		List<Map<String, Object>> fVals = erc.getfVals();
		for (int i = 0; i < fields.length; i++) {
			String field=fields[i];
			String op=ops[i];
			Map<String, Object> fValMap = fVals.get(i);
			Parameter param = new Parameter();
			int size = fValMap.size();/*参数处理*/
			if(size==1||size==0){//无参数值
				//param.setValue("α"+i);
				param.setValue("");
			}
			if(size==2){//只有一个参数值
				String val1=(String) fValMap.get("val1");
				//param.setValue(val1+"μ"+0+"α"+i);
				param.setValue(val1==null?"":val1);
			}
			if(size==3){//两个参数值
				String val1=(String) fValMap.get("val1");
				String val2=(String) fValMap.get("val2");
				//param.setValue(val1+"μ"+0+"α"+i+"θ"+val2+"μ"+1+"α"+i);//次数"μ"参数位置"α"第几条规则配置"θ"分割参数值位置
				param.setValue(val1+"θ"+val2);
			}
			
			/**
			 * 黑名单 暂未处理
			 */
			funcList.add(new Column(field,op,param));
		}
		return funcList;
	}
	
	
	public   Response getReponse(String responseId){return null;};
	
	public   EventCategory getEventCategory(Integer catid){	return null;}; 
	
	 
}
