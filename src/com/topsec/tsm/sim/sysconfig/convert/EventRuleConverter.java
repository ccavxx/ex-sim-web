package com.topsec.tsm.sim.sysconfig.convert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.node.detect.Group;
import com.topsec.tsm.sim.sceneUser.persistence.EventCategory;
import com.topsec.tsm.ui.util.StringUtils;


/**
 * 转换规则配置数据 前端使用
 * @author zhaojun 2014-3-28下午6:53:59
 */
public class EventRuleConverter {
	
	private static final Logger log = LoggerFactory.getLogger(EventRuleConverter.class) ;
	
	
	public static final String  RULE_ATTR_ID="id";
	public static final String  RULE_ATTR_NAME="name";
	public static final String  RULE_ATTR_PRIORITY="priority";
//	public static final String  RULE_ATTR_ALARMSTATE="alarmState";
	
	public static final String  RULE_ATTR_CATEGORY="category";
	public static final String  RULE_ATTR_VERSION="version";
	public static final String  RULE_ATTR_RESPIDS="responseIds";
	public static final String  RULE_ATTR_RESPCFGKEYS="responsecfgkeys";
	
	public static final String  RULE_CONDITION="condition";
	public static final String  RULE_CONDITION_ATTR_SELECTOR="selector";
	
	
	public static final String  RULE_WINDOW="window";
	public static final String  RULE_WINDOW_ATTR_TIME="time";
	public static final String  RULE_WINDOW_ATTR_COUNT="count";
	public static final String  RULE_WINDOW_ATTR_TRIGGER="trigger";
	
	public static final String  RULE_COLUMNS="columns";
	
	public static final String  RULE_COLUMN="column";
	public static final String  RULE_COLUMN_ATTR_FUNC="func";
	public static final String  RULE_COLUMN_ATTR_FIELD="field";
	public static final String  RULE_COLUMN_PARAM="param";
	
	public static final String  RULE_RESULT="result";
	public static final String  RULE_RESULT_ATTR_RETYPE="re_type";
	public static final String  RULE_RESULT_ATTR_CUSTOM8="CUSTOM8";

	
	public static final String  RULE_CATEGORIES="categories";
	public static final String  RULE_CATEGORY="category";
	public static final String  RULE_CATEGORY_ID="id";
	public static final String  RULE_CATEGORY_NAME="name";
	
	public static final String  RULE_GROUPS="groups";
	public static final String  RULE_GROUP="group";
	public static final String  RULE_GROUP_ID="id";
	public static final String  RULE_GROUP_NAME="name";
	public static final String  RULE_ORDER_IN_GROUP="order";
	
	private Document  xmlRuleDoc=null;
	
	private Integer id;
	private String name;
	private String version;
	
	private Integer count;
	private Integer time;
	private String trigger;
	
	
	private List<Map<String,Object>> columns=new ArrayList<Map<String,Object>>();
	
	
	private String resultReType;
	
	
	private RuleColumnHandler columnHandler;


	private String custom8;

	private List<Group> dispatchGroups=new ArrayList<Group>();
 
	public void setColumnHandler(RuleColumnHandler columnHandler) {
		this.columnHandler = columnHandler;
	}
	public void build(String xmlRule){
		parseText2Xml(xmlRule);
		startParsing(); 
		return  ;
	}
	private void startParsing() {
		Element root = xmlRuleDoc.getRootElement();
		parseRoot(root);
		parseCondition(root);
		parseResult(root);
		parseGroups(root);
	}
 
	private void parseGroups(Element root) {
		Element groups = root.element(RULE_GROUPS);
		if (groups!=null) {
			List<Element> grouplist = groups.elements(RULE_GROUP);
			if(grouplist!=null){
				for (Element element : grouplist) {
					Group g=new Group();
					g.setGroupId(getAttribute(element, RULE_GROUP_ID));
					g.setName(getAttribute(element, RULE_GROUP_NAME));
					g.setOrder(Integer.parseInt(getAttribute(element, RULE_GROUP_ID)));
					dispatchGroups.add(g);
				}
			}
		}
	}
	private void parseResult(Element root) {
		Element result = root.element(RULE_RESULT);
		if(result!=null){
			resultReType=getAttribute(result, RULE_RESULT_ATTR_RETYPE);
			custom8=getAttribute(result, RULE_RESULT_ATTR_CUSTOM8);
		}
	}
	private void parseCondition(Element root) {
		Element element = root.element(RULE_CONDITION);
		if(element!=null){
			parseWindow(element);
			parseColumns(element);
		}
	}
	private void parseColumns(Element element) {
		Element columns=element.element(RULE_COLUMNS);
		if(columns!=null){
			Iterator<Element> iter = columns.elementIterator(RULE_COLUMN);
			while(iter!=null&&iter.hasNext()){
				Element col = iter.next();
				parseColumn(element, col);
			}
		}
	}
	private void parseColumn(Element element, Element col) {
		String func =getAttribute(col, RULE_COLUMN_ATTR_FUNC);
		String field=getAttribute(col, RULE_COLUMN_ATTR_FIELD);
		Element param = col.element(RULE_COLUMN_PARAM);
		String  paramText=param!=null?param.getStringValue().trim():null;
		this.columns.add(this.columnHandler.separate(func,field,paramText));
	}
 
	private void parseWindow(Element element) {
			Element window = element.element(RULE_WINDOW);
			if(window!=null){
				time=Integer.parseInt(getAttribute(window, RULE_WINDOW_ATTR_TIME));
				count=Integer.parseInt(getAttribute(window, RULE_WINDOW_ATTR_COUNT));
				trigger=getAttribute(window, RULE_WINDOW_ATTR_TRIGGER);
			}
	}
	
	private void parseRoot(Element root) {
		id=Integer.parseInt(getAttribute(root,RULE_ATTR_ID)) ;
		name=getAttribute(root,RULE_ATTR_NAME);
		version=getAttribute(root, RULE_ATTR_VERSION);
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	private String getAttribute(Element root, String attrName) {
		Attribute attr = root.attribute(attrName);
		if(attr==null){
			log.debug("there is no attr ["+attrName+"]");
			return null;
		}
		
		return attr.getValue().trim();
	}
	private void parseText2Xml(String xmlRule) {
		try {
			xmlRuleDoc=DocumentHelper.parseText(xmlRule);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public String getTrigger() {
		return trigger;
	}
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	public List<Map<String, Object>> getColumns() {
		return columns;
	}
	public void setColumns(List<Map<String, Object>> columns) {
		this.columns = columns;
	}
	public String getResultReType() {
		return resultReType;
	}
	public void setResultReType(String resultReType) {
		this.resultReType = resultReType;
	}
	public String getCustom8() {
		return custom8;
	}
	public void setCustom8(String custom8) {
		this.custom8 = custom8;
	}
	 
	public static void main(String[] args) {
		String str="a,b";
	}
	public List<Group> getDispatchGroups() {
		return dispatchGroups;
	}
	public void setDispatchGroups(List<Group> dispatchGroups) {
		this.dispatchGroups = dispatchGroups;
	}
	
	
}
