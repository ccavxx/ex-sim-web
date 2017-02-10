package com.topsec.tsm.sim.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.sysconfig.bean.Condition;
import com.topsec.tsm.sim.sysconfig.bean.Cprop;
import com.topsec.tsm.sim.sysconfig.bean.Cprops;
import com.topsec.tsm.sim.sysconfig.bean.Cselector;
import com.topsec.tsm.sim.sysconfig.bean.Cwindow;
import com.topsec.tsm.sim.sysconfig.bean.Event;
import com.topsec.tsm.sim.sysconfig.bean.Field;
import com.topsec.tsm.sim.sysconfig.bean.Op;
import com.topsec.tsm.sim.sysconfig.bean.Property;
import com.topsec.tsm.sim.sysconfig.bean.Rule;
import com.topsec.tsm.sim.sysconfig.bean.SelectObj;
import com.topsec.tsm.sim.sysconfig.bean.Val;

public class SceneTemplateUtil {
	public static void main(String[] args) {
		List<Event> eleVals = SceneTemplateUtil.getInstance().getEleVals();
		for (Event scene : eleVals) {
			System.out.println(scene.getRules().get(0).getCondition().getCprops().getCpList().get(0).isOption() + "*********************");
		}
	}

	private static Element element = null;

	private static SceneTemplateUtil instance = null;

	private static String xmlFile;
	private static final String path = SystemDefinition.DEFAULT_CONF_DIR;
	private Document doc;

	public SceneTemplateUtil() {

		SAXReader sAXReader = new SAXReader();
		try {
			doc = null;
			// doc = sAXReader.read("G:\\wks\\tal-web\\src\\com\\topsec\\tsm\\tal\\ui\\report\\action\\sceneTemplate.xml");
			doc = sAXReader.read(path + "/sceneTemplate.xml");
			// xmlFile = path + "/sceneTemplate.xml";
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		element = doc.getRootElement();

	}

	/* 获取单例对象 */
	public synchronized static SceneTemplateUtil getInstance() {
		// instance = null;// 暂开，调试用
		if (instance == null) {
			instance = new SceneTemplateUtil();
		}
		return instance;
	}

	public List<Field> getFields(Element ele, List<Field> _fields) {
		List<Field> fields = new ArrayList<Field>();
		List<Element> elements = ele.elements();
		for (Element element : elements) {
			Field field = new Field();
			field.setAlias(element.attributeValue("alias"));
			field.setName(element.attributeValue("name"));
			field.setType(element.attributeValue("type"));
			field.setValidate(element.attributeValue("validate"));
			field.setValidateSize(element.attributeValue("validateSize"));
			field.setShowItem(element.attributeValue("showItem"));
			fields.add(field);
			_fields.add(field);
		}
		return fields;
	}

	public List<Val> getVals(Element ele, List<Val> _vals) {
		List<Val> vals = new ArrayList<Val>();
		List<Element> elements = ele.elements();
		for (Element element : elements) {
			Val val = new Val();
			val.setName(element.attributeValue("name"));
			vals.add(val);
			_vals.add(val);
		}
		return vals;
	}

	public void getProperty(Element ele, List<Property> props) {
		List<Element> elements = ele.elements();
		for (Element element : elements) {
			Property property = new Property();
			property.setAlias(element.attributeValue("alias"));
			property.setDesc(element.attributeValue("desc"));
			property.setName(element.attributeValue("name"));
			property.setOption(Boolean.valueOf(element.attributeValue("isOption")));
			property.setType(element.attributeValue("type"));
			property.setValidateSize(element.attributeValue("validateSize"));
			property.setValue(element.attributeValue("value"));
			property.setVisiable(Boolean.valueOf(element.attributeValue("visiable")));

			String vals = element.attributeValue("selectValues");
			if (StringUtils.isNotBlank(vals)) {
				String[] items = vals.split(";");
				List<SelectObj> list = new ArrayList<SelectObj>();
				for (String item : items) {
					SelectObj selectObj = new SelectObj();
					String[] val = item.split(",");
					selectObj.setName(val[1]);
					selectObj.setValue(val[0]);
					list.add(selectObj);
				}
				property.setSelectValues(list);
			}
			props.add(property);
		}
	}

	public void getCfov(Element ele, List<Field> fields, List<Op> ops, List<Val> vals) {
		List<Element> elements = ele.elements();
		for (Element element : elements) {
			if ("fields".equals(element.getName())) {
				getFields(element, fields);
			}
			if ("ops".equals(element.getName())) {
				getOps(element, ops);
			}
			if ("values".equals(element.getName())) {
				getVals(element, vals);
			}
		}
	}

	public void toWinProp(Cwindow cwindow, Element ele) {
		cwindow.setAlias(ele.attributeValue("alias"));
		cwindow.setDesc(ele.attributeValue("desc"));
		cwindow.setName(ele.attributeValue("name"));
		cwindow.setOption(Boolean.valueOf(ele.attributeValue("isOption")));
		cwindow.setType(ele.attributeValue("type"));
		cwindow.setValue(ele.attributeValue("value"));
		cwindow.setVisiable(Boolean.valueOf(ele.attributeValue("visiable")));

	}

	public void toSeleProp(Cselector cselector, Element ele) {
		cselector.setAlias(ele.attributeValue("alias"));
		cselector.setDesc(ele.attributeValue("desc"));
		cselector.setName(ele.attributeValue("name"));
		cselector.setOption(Boolean.valueOf(ele.attributeValue("isOption")));
		cselector.setType(ele.attributeValue("type"));
		cselector.setValue(ele.attributeValue("value"));
		cselector.setVisiable(Boolean.valueOf(ele.attributeValue("visiable")));
	}

	public List<Op> getOps(Element ele, List<Op> _ops) {
		List<Op> ops = new ArrayList<Op>();
		List<Element> elements = ele.elements();
		for (Element element : elements) {
			Op op = new Op();
			op.setAlias(element.attributeValue("alias"));
			op.setName(element.attributeValue("name"));
			op.setShowInput(Boolean.valueOf(element.attributeValue("showInput")));
			if (StringUtils.isNotBlank(element.attributeValue("showSelect"))) {
				op.setShowSelect(Boolean.valueOf(element.attributeValue("showSelect")));
			}
			op.setShowItem(element.attributeValue("showItem"));
			op.setShowLabel(element.attributeValue("showlabel"));
			ops.add(op);
			_ops.add(op);
		}
		return ops;
	}

	public void toRuleProp(Rule rule, Element element) {
		rule.setCategory(element.attributeValue("category"));
		rule.setId(Integer.valueOf(element.attributeValue("id")));
		rule.setShowName(element.attributeValue("showName"));
		rule.setVersion(element.attributeValue("version"));
	}

	public List<Event> getEleVals() {

		List<Event> scenes = new ArrayList<Event>();
		List<Element> elements = element.elements();
		for (Element ele1 : elements) {
			Event scene = new Event();
			scene.setMaxRuleSize(Integer.valueOf(ele1.attributeValue("maxRuleSize")));
			List<Rule> rules = new ArrayList<Rule>();
			List<Element> elements2 = ele1.elements();

			for (Element ele2 : elements2) {// rule
				Rule rule = new Rule();
				toRuleProp(rule, ele2);
				List<Property> props = new ArrayList<Property>();
				Condition condition = new Condition();

				List<Element> elements3 = ele2.elements();
				for (Element ele3 : elements3) {
					if ("properties".equals(ele3.getName())) {// properties
						getProperty(ele3, props);
					}
					if ("condition".equals(ele3.getName())) {// condition
						Cprops cprops = new Cprops();
						Cselector cselector = new Cselector();
						Cwindow cwindow = new Cwindow();

						List<Cprop> cpList1 = new ArrayList<Cprop>();
						List<Cprop> cpList2 = new ArrayList<Cprop>();
						List<Property> propList = new ArrayList<Property>();

						List<Element> ele4 = ele3.elements();
						for (Element ele5 : ele4) {
							if ("properties".equals(ele5.getName())) {// properties
								List<Element> ele6 = ele5.elements();
								for (Element ele7 : ele6) {
									Cprop cprop = new Cprop();
									cprop.setOption(Boolean.valueOf(ele7.attributeValue("isOption")));
									List<Field> fields = new ArrayList<Field>();
									List<Op> ops = new ArrayList<Op>();
									List<Val> values = new ArrayList<Val>();
									getCfov(ele7, fields, ops, values);
									cprop.setFields(fields);
									cprop.setOps(ops);
									cprop.setValues(values);
									cpList1.add(cprop);
								}

							}
							if ("selector".equals(ele5.getName())) {// selector
								toSeleProp(cselector, ele5);
								List<Element> ele6 = ele5.elements();
								for (Element ele7 : ele6) {

									List<Element> ele8 = ele7.elements();
									for (Element ele9 : ele8) {

										Cprop cprop = new Cprop();
										List<Field> fields = new ArrayList<Field>();
										List<Op> ops = new ArrayList<Op>();
										List<Val> values = new ArrayList<Val>();

										getCfov(ele9, fields, ops, values);
										cprop.setFields(fields);
										cprop.setOps(ops);
										cprop.setValues(values);
										cpList2.add(cprop);
									}

								}

							}
							if ("window".equals(ele5.getName())) {// window
								toWinProp(cwindow, ele5);
								List<Element> ele6 = ele5.elements();
								for (Element ele7 : ele6) {
									getProperty(ele7, propList);
								}
							}
						}

						cprops.setCpList(cpList1);
						cselector.setCpList(cpList2);
						cwindow.setPropList(propList);

						condition.setCprops(cprops);
						condition.setCselector(cselector);
						condition.setCwindow(cwindow);
					}

				}
				rule.setCondition(condition);
				rule.setProps(props);
				rules.add(rule);
			}
			scene.setRules(rules);
			scenes.add(scene);
		}
		return scenes;
	}

}
