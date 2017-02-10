package com.topsec.tsm.ui.topo.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.topsec.tsm.auth.manage.SID;
import com.topsec.tsm.framework.util.ResourceLoader;
import com.topsec.tsm.ui.topo.bean.DevManage;
import com.topsec.tsm.ui.topo.bean.DevStatus;
import com.topsec.tsm.ui.topo.bean.EvtCol;
import com.topsec.tsm.ui.topo.bean.ReponseMode;
import com.topsec.tsm.ui.topo.bean.TopoDevType;
import com.topsec.tsm.ui.topo.excption.TopoException;
import com.topsec.tsm.ui.topo.svg.elements.Circle;
import com.topsec.tsm.ui.topo.svg.elements.Image;
import com.topsec.tsm.ui.topo.svg.elements.Line;
import com.topsec.tsm.ui.topo.svg.elements.PolyLine;
import com.topsec.tsm.ui.topo.svg.elements.Rect;
import com.topsec.tsm.ui.topo.svg.elements.Statistic;
import com.topsec.tsm.ui.topo.svg.elements.Text;
import com.topsec.tsm.ui.topo.svg.elements.Tspan;
import com.topsec.tsm.ui.topo.vo.EventTypeVo;
import com.topsec.tsm.ui.topo.vo.ItemVo;
import com.topsec.tsm.ui.topo.vo.NetworkToolVo;
import com.topsec.tsm.ui.topo.vo.StatisticsVo;

public class TopoUtil {

	public static final String UTF8 = "UTF-8";
	private static Map<Long, NetworkToolVo> nwts = new HashMap<Long, NetworkToolVo>();
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String TOPO_CONF_FNAME = "../../../../conf/topo/topoConf.xml";
	private static final String DEV_CONF = "../../../../conf/topo/Topo-Statistics-config.xml";
	public static final String TOPO_VIEW = "../../../../conf/topo/TopoView.xml";

	public static Map<Long, NetworkToolVo> getNetworkToolConfig() {
		return nwts;
	}

	public static String getNetworkToolConfigFileName() {
		return "NetworkTool-config.xml";
	}
  
	@Deprecated
	public static void readTopoEvtListenersConf() {
		String msg = "readTopoEvtListenersConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		List devEvtEle = doc.selectNodes("//listener/evtListeners/devEvt");
	}

	@SuppressWarnings("unchecked")
	public static Set<String> readTopoDevs() {
		Set<String> ids = new HashSet<String>();

		String msg = "readTopoDevs error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
			List<Element> devEles = doc.selectNodes("//image");
			for (Element dev : devEles) {
				String id = dev.attributeValue("id", "");
				if (id.startsWith("DM") && id.indexOf("-DV") != -1) {
					String[] arr = id.split("-DV");
					if (arr.length == 2 && StringUtils.isNotEmpty(arr[1])) {
						ids.add(arr[1]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		return ids;
	}

	public static void main(String[] args) throws DocumentException {
		// Document doc =
		// loadDocFile("F:/ta5/TopAnalyzer/app-server/server/bin/topoConf.xml");
		// List nodes =
		// doc.selectNodes("//templates/template/item[@type='areaType']/g");
		// System.out.println(nodes.size());

		// try {
		// boolean reachable = telnet("192.168.76.102", 2000);
		// System.out.println(reachable);
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// readTopoDevTypeConf();

		// Evaluator eval = new Evaluator();
		// System.out.println(eval.evaluate("2*3-5/(3-1)"));
		// System.out.println(eval.evaluate("7 / 2"));
		// System.out.println(eval.evaluate("7 % 2"));
		// System.out.println(eval.evaluate("((4 + 3) * -2) * 3"));
		// System.out.println(eval.evaluate("((4 + 3) * -2) * 3 + sqrt(30)"));
		// System.out.println(eval.evaluate("((4 + 3) * -2) * 3 + sin(45)"));

		//String val = eval("val*13*3", "2");

		//System.out.println(val.substring(0, val.indexOf(".")));
	}

	private static Evaluator eval = new Evaluator();
	private static String EVAL_EVT_VAR = "val";

	public static String eval(String exp, String value) {
		if (StringUtils.isEmpty(exp)) {
			return value;
		}
		if (StringUtils.isNotEmpty(exp) && exp.indexOf(EVAL_EVT_VAR) != -1) {
			exp = exp.replaceAll(EVAL_EVT_VAR, "#{" + EVAL_EVT_VAR + "}");
		}
		return eval(exp, EVAL_EVT_VAR, value);
	}

	public static final synchronized String eval(String exp, String var, String value) {
		eval.clearVariables();
		String val = "";

		if (StringUtils.isEmpty(exp) || StringUtils.isEmpty(value)) {
			return val;
		}

		if (StringUtils.isNotEmpty(var)) {
			eval.putVariable(var, value);
		}

		try {
			val = eval.evaluate(exp);
		} catch (EvaluationException e) {
			e.printStackTrace();
			log.error("evaluator error ,exp:" + exp + e.getMessage(), e);
		} 
		return val;
	}

	public static Set<EvtCol> readDevEvtCols(Map<String, String> devTypeMapping, Map<String, TopoDevType> devType, String ip) {
		Set<EvtCol> set = new HashSet<EvtCol>();
		if (devTypeMapping == null || devType == null) {
			return set;
		}
		if (StringUtils.isEmpty(ip)) {
			log.error("getDevEvtCols ip is empty");
			return set;
		}

		String typeKey = devTypeMapping.get(ip);
		if (StringUtils.isEmpty(typeKey)) {
			log.error("getDevEvtCols " + ip + "no define devType");
			return set;
		}
		TopoDevType topoDevType = devType.get(typeKey);
		if (topoDevType == null) {
			log.error("getDevEvtCols no def typeKey:" + typeKey);
			return set;
		}
		return topoDevType.getEvtCollectors();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> readDevTypeMapping() {
		String msg = "readDevTypeMapping error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element devTypeEle = (Element) doc.selectSingleNode("//conf/devType");
		if (devTypeEle == null) {
			devTypeEle = doc.getRootElement().element("conf").addElement("devType");
		}

		Element devMappingEle = devTypeEle.element("devMapping");
		if (devMappingEle == null) {
			devMappingEle = devTypeEle.addElement("devMapping");
		}
		Map<String, String> m = new HashMap<String, String>();
		List<Element> devsEle = devMappingEle.elements("dev");
		for (Element dev : devsEle) {
			String ip = dev.attributeValue("ip");
			String type = dev.attributeValue("type");
			if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(type)) {
				log.error("error ,skip ..");
			}
			m.put(ip, type);
		}

		log.error("devMappingEle define " + m);
		return m;
	}
	public static void saveTopoViewSize(String WAmp,String HAmp){
		String msg = "readTopoDevTypeConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element viewElement = (Element) doc.selectSingleNode("//viewSize");
		Element topoElement = (Element) doc.selectSingleNode("//viewSize/size");
		if(topoElement==null){
			topoElement = viewElement.addElement("size");
		}
		topoElement.setAttributeValue("WAmp", WAmp);
		topoElement.setAttributeValue("HAmp", HAmp);
		writeTopoConf(TOPO_CONF_FNAME, doc);
	}
	public static String readTopoViewSize(){
		String msg = "readTopoDevTypeConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element topoElement = (Element) doc.selectSingleNode("//viewSize/size");
		String WAmp =topoElement.attributeValue("WAmp");
		String HAmp =topoElement.attributeValue("HAmp");
		if(WAmp.isEmpty()||HAmp.isEmpty()){
			WAmp = "5";
			HAmp = "5";
		}
		return WAmp+"@"+HAmp ;
	}
	public static String getTopoEventShowCount(String type){
		String msg = "getTopoEventShowCount error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element devTypeEle = (Element) doc.selectSingleNode("//conf/params");
		if (devTypeEle == null) {
			devTypeEle = doc.getRootElement().element("conf").addElement("params");
		}
		Element element = null;
		if("evtCount".equals(type)){
			element = devTypeEle.element("evtCount");
			if(element == null){
				element = devTypeEle.addElement("evtCount");
				element.setAttributeValue("num","5");
			}
		}else if("evtTime".equals(type)){
			element = devTypeEle.element("evtTime");
			if(element == null){
				element = devTypeEle.addElement("evtTime");
				element.setAttributeValue("num","5");
			}
		}
		String num = "";
		if(StringUtils.isNotEmpty(element.attributeValue("num"))){
			num = element.attributeValue("num");
		}
		return num;
		
	}
	@SuppressWarnings("unchecked")
	public static Map<String, TopoDevType> readTopoDevTypeConf() {
		// liujl
		String msg = "readTopoDevTypeConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element devTypeEle = (Element) doc.selectSingleNode("//conf/devType");
		if (devTypeEle == null) {
			devTypeEle = doc.getRootElement().element("conf").addElement("devType");
		}

		List<Element> typesEle = devTypeEle.elements("type");

		Map<String, TopoDevType> pool = new LinkedHashMap<String, TopoDevType>();
		for (Element typeEle : typesEle) {
			String key = typeEle.attributeValue("key");
			// String value = typeEle.attributeValue("value");
			String name = typeEle.attributeValue("name");

			TopoDevType type = new TopoDevType();
			type.setKey(key);
			type.setName(name);
			// type.setValue(value);

			List<Element> collectorsEle = typeEle.elements("collector");
			Set<DevStatus> topoEvtDef = new HashSet<DevStatus>();
			Map<String, EvtCol> mapping = new HashMap<String, EvtCol>();

			for (EvtCol def : topoEvtDef) {
				mapping.put(def.getId(), def);
			}

			for (Element ele : collectorsEle) {
				EvtCol evtCollector = mapping.get(ele.getTextTrim());
				if (evtCollector != null) {
					type.addEvtCollector(evtCollector);
				}
			}
			pool.put(key, type);
		}
		log.debug("dev type define " + pool);
		return pool;
	}

	@Deprecated
	private static boolean telnet(String serverIp, int timeOut) throws IOException, UnknownHostException {
		timeOut = timeOut < 0 ? 2000 : timeOut;
		boolean reachable = java.net.InetAddress.getByName(serverIp).isReachable(timeOut);
		if (!reachable) {
			log.error("server " + serverIp + "l?????(timeOut:" + timeOut + ")");
		}
		return reachable;
	}

	public static String dictEvt(Set<DevStatus> dic, String id) {
		for (DevStatus s : dic) {
			if (s.getId().equals(id)) {
				return s.getName();
			}
		}
		return null;
	}

	private static Logger log = Logger.getLogger(TopoUtil.class);
	public static synchronized void saveOrUpdateDevConf(StatisticsVo devConf) {
		Document document = null;
		File file = new File(DEV_CONF);
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding(UTF8);
		if (!file.exists()) {
			log.error("not exists file " + DEV_CONF + " auto create file");
		}

		SAXReader reader = new SAXReader();
		try {
			document = reader.read(new InputStreamReader(new FileInputStream(file), UTF8));
		} catch (Exception e) {
			log.error(e);
		}
		Element root = document.getRootElement();

		Element userEle = (Element) root.selectSingleNode("//userConfig");

		if (userEle == null) {
			userEle = root.addElement("userConfig");
		}

		Element item = (Element) userEle.selectSingleNode("//item[@itemId='" + devConf.getItemSimpleId() + "']");
		if (item != null) {
			boolean flag = userEle.remove((Element) item);
			log.error("delete old item success" + flag);
		}

		item = userEle.addElement("item");
		item.addAttribute("itemId", devConf.getItemSimpleId());

		// if (devConf.getLockInfoInView()) {
		// Attribute lockInfoInViewAttr = item.attribute("lockInfoInView");
		// String val = String.valueOf(devConf.isLockInfoInView());
		// if (lockInfoInViewAttr == null) {
		// item.addAttribute("lockInfoInView", val);
		// } else {
		// item.setAttributeValue("lockInfoInView", val);
		// }
		// }

		String[] fireWallState = devConf.getDevStatus();
		if (fireWallState != null && fireWallState.length > 0) {
			Element fireWallStatesEle = item.addElement("fireWallStates");

			for (String state : fireWallState) {
				fireWallStatesEle.addElement("state").addText(state);
			}
		}
		
		String[] features = devConf.getPhyValues();
		if (features != null && features.length > 0) {
			Element elfeElement = item.addElement("feature");

			for (String feature : features) {
				elfeElement.addElement("feature").addText(feature);
			}
		}
		String[] types = devConf.getEventTypes();
		if (types != null && types.length > 0) {
			Element typesEle = item.addElement("eventTypes");
			for (String type : types) {
				typesEle.addElement("type").addText(type);
			}
		}

		String[] levels = devConf.getEventLevels();
		if (levels != null && levels.length > 0) {
			Element levelsEle = item.addElement("eventLevels");
			for (String level : levels) {
				levelsEle.addElement("level").addText(level);
			}
		}

		String[] filters = devConf.getFilters();
		if (filters != null && filters.length > 0) {
			Element filtersEle = item.addElement("filters");
			for (String filter : filters) {
				filtersEle.addElement("filter").addText(filter);
			} 
		}

		writeTopoConf(DEV_CONF, document);
	}

	// public static List getTopo

	public static List<EventTypeVo> getEventTypeDef() {
		String file = "../../../../conf/eventtype.xml";
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			File evtDefFile = new File(file);
			doc = reader.read(new InputStreamReader(new FileInputStream(evtDefFile)));
			Element root = doc.getRootElement();
			List<Element> mainTypesEles = root.elements("category");

			List<EventTypeVo> list = new ArrayList<EventTypeVo>();

			for (Element node : mainTypesEles) {
				EventTypeVo pType = new EventTypeVo();
				String name = node.attributeValue("name");
				String desc = node.attributeValue("description");
				pType.setName(name);
				pType.setDescription(desc);

				List<Element> subNodes = node.elements("category");
				if (subNodes != null && !subNodes.isEmpty()) {
					for (Element sub : subNodes) {
						EventTypeVo subType = new EventTypeVo();
						String subName = sub.attributeValue("name");
						subType.setName(subName);
						String subDesc = sub.attributeValue("description");
						subType.setDescription(subDesc);
						subType.setParentType(pType);
						list.add(subType);
					}
				}
			}
			return list;
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	// liujl
	@SuppressWarnings("unchecked")
	public static Set<StatisticsVo> getStatisticsConfig(String[] devIds) {
		Document doc = null;
		try {
			doc = readTopoDevConfDoc();
			Element userNode = (Element) doc.selectSingleNode("//userConfig");// 

			if (userNode != null) {

				List<Element> itemEles = (List<Element>) userNode.selectNodes("//item");

				if (itemEles != null && !itemEles.isEmpty()) {
					Set<StatisticsVo> items = new LinkedHashSet<StatisticsVo>();

					for (Element itemEle : itemEles) {

						StatisticsVo vo = getStatisticsVo(itemEle);

						String itemId = vo.getItemSimpleId();
						if (!ArrayUtils.contains(devIds, itemId.trim())) {
							log.error("skip  item:" + itemId);
							continue;
						}
						items.add(vo);

					}
					return items;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	public static Set<String> readTopoViewLockDevs() {
		Document doc = null;
		String msg = "readTopoViewLockDevs";
		try {
			doc = readTopoDevConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage());
			throw new TopoException(msg + e.getMessage());
		}
		Set<String> set = new HashSet<String>();
		List<Element> lockDevEles = doc.selectNodes("//userConfig/item");
		for (Element dev : lockDevEles) {
			String id = dev.attributeValue("lockInfoInView");
			if (StringUtils.isNotEmpty(id)) {
				set.add(id);
			}
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	public static String[] getTopoFilters() {
		Document doc = null;
		String[] ids = null;
		try {
			doc = readTopoDevConfDoc();
			List<Element> filters = (List<Element>) doc.selectNodes("//filter");
			if (filters != null && !filters.isEmpty()) {
				Set<String> filterIds = new HashSet<String>();
				for (Element ele : filters) {
					if (StringUtils.isNotEmpty(ele.getStringValue()))
						filterIds.add(ele.getStringValue());
				}
				ids = filterIds.toArray(new String[filters.size()]);
			} else {
				ids = new String[] {};
			}
		} catch (Exception e) {
			log.error(e);
			throw new TopoException("getTopoFilters error" + e.getMessage(), e);
		}
		return ids;

	}

	public static StatisticsVo getStatisticsVo(String itemId) {
		Document doc = null;
		try {
			doc = readTopoDevConfDoc();
			Element userNode = (Element) doc.selectSingleNode("//userConfig");//
			if (userNode != null) {
				Element item = (Element) userNode.selectSingleNode("//item[@itemId='" + itemId + "']");
				return getStatisticsVo(item);
			}

		} catch (Exception e) {
			log.error(e);
		}
		return null;

	}

	public static void lockDevWindow(String DevId, String windowId, boolean isLock) {
		// lockInfoInView
		String msg = "lockDevWindow error";
		Document doc = null;
		try {
			doc = readTopoDevConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		try {
			Element item = (Element) doc.selectSingleNode("//item[@itemId='" + DevId + "']");
			if (item == null) {
				Element userNode = (Element) doc.selectSingleNode("//userConfig");
				item = userNode.addElement("item");
				item.addAttribute("itemId", DevId);
				item.addAttribute("lockInfoInView", windowId);
			}

			Attribute attr = item.attribute("lockInfoInView");
			if (isLock) {
				if (attr == null) {
					item.addAttribute("lockInfoInView", windowId);
				} else {
					item.setAttributeValue("lockInfoInView", windowId);
				}
			} else {
				if (attr == null) {
					return;
				} else {
					item.remove(attr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg, e);
		}
		writeTopoConf(DEV_CONF, doc);

	}

	@SuppressWarnings("unchecked")
	private static StatisticsVo getStatisticsVo(Element itemEle) {
		StatisticsVo devConf = new StatisticsVo();
		if (itemEle == null || itemEle.attributeValue("itemId") == null) {
			return devConf;
		}

		String itemId = itemEle.attributeValue("itemId");
		if (StringUtils.isEmpty(itemId)) {
			log.error("itemId is empty ,skip. ");
			return null;
		}

		devConf.setItemId(itemId);

		Element fireWallStatesEle = itemEle.element("fireWallStates");
		if (fireWallStatesEle != null) {
			List<Element> stateEle = fireWallStatesEle.elements("state");
			if (stateEle != null && !stateEle.isEmpty()) {
				String[] states = new String[stateEle.size()];
				for (int i = 0; i < stateEle.size(); i++) {
					states[i] = stateEle.get(i).getTextTrim();
				}
				devConf.setDevStatus(states);
				log.info("fireWallState:" + Arrays.toString(states));
			}

		}

		Element typesEle = itemEle.element("eventTypes");
		if (typesEle != null) {
			List<Element> types = typesEle.elements("type");
			if (types != null && !types.isEmpty()) {
				String[] eventTypes = new String[types.size()];
				for (int i = 0; i < types.size(); i++) {
					eventTypes[i] = ((Element) types.get(i)).getText();
				}
				devConf.setEventTypes(eventTypes);
			}
		}

		Element levelsEle = itemEle.element("eventLevels");
		if (levelsEle != null) {
			List<Element> levels = levelsEle.elements("level");
			if (levels != null && !levels.isEmpty()) {

				String[] eventLevels = new String[levels.size()];
				for (int i = 0; i < levels.size(); i++) {
					eventLevels[i] = ((Element) levels.get(i)).getText();
				}
				devConf.setEventLevels(eventLevels);
			}
		}

		Element filtersEle = itemEle.element("filters");
		if (filtersEle != null) {
			List<Element> filters = filtersEle.elements("filter");
			if (filters != null && !filters.isEmpty()) {

				String[] eventFilters = new String[filters.size()];
				for (int i = 0; i < filters.size(); i++) {
					eventFilters[i] = ((Element) filters.get(i)).getText();
				}
				devConf.setFilters(eventFilters);
			}
		}
		Element featureEle = itemEle.element("feature");
		if (featureEle != null) {
			List<Element> featureEles = featureEle.elements("feature");
			if (featureEles != null && !featureEles.isEmpty()) {

				String[] strf= new String[featureEles.size()];
				for (int i = 0; i < featureEles.size(); i++) {
					strf[i] = ((Element) featureEles.get(i)).getText();
				}
				devConf.setPhyValues(strf);
			}
		}

		return devConf;
	}

	private static void saveNetworkToolConfig(NetworkToolVo vo) {
		try {
			if ((vo.getResourceId()==null)||"0".equals(vo.getResourceId())||(vo.getResourceId()).isEmpty()) {
				return;
			} else {
				NetworkToolVo vv = nwts.get(new Long(vo.getResourceId()));
				nwts.put(new Long(vo.getResourceId()), vo);
			}
			saveNetworkToolsConfig();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("saveNetworkToolConfig error", e);
		}
	}

	public static void saveOrUpdateNetToolConf(NetworkToolVo vo) {
		String msg = "saveOrUpdateNetToolConf error";
		try {
			Validate.notNull(vo, msg + " null tool");
			Validate.notNull(vo.getResourceId(), msg + " null tool id");

			Document doc = readTopoConfDoc();

			Element netToolConfEle = (Element) doc.selectSingleNode("//conf/NetworkToolConf");
			if (netToolConfEle == null) {
				Element e = (Element) doc.selectSingleNode("//conf");
				netToolConfEle = e.addElement("NetworkToolConf");
			}

			Element _root = (Element) doc.selectSingleNode("//conf/NetworkToolConf/device[@id='" + vo.getResourceId() + "']");
			if (_root != null) {
				netToolConfEle.remove(_root);
			}

			_root = netToolConfEle.addElement("device");
			_root.addAttribute("id", String.valueOf(vo.getResourceId()));

			Element el = _root.addElement("ping");
			el.addAttribute("count", String.valueOf(vo.getPing_count() == 0 ? 4 : vo.getPing_count()));
			el.addAttribute("timeout", String.valueOf(vo.getPing_timeout() == 0 ? 1000 : vo.getPing_timeout()));

			el = _root.addElement("telnet");
			el.addAttribute("port", vo.getTelnet_port() == 0 ? "23" : String.valueOf(vo.getTelnet_port()));
			el = _root.addElement("ssh");

			el.addAttribute("port", vo.getSsh_port() == 0 ? "22" : String.valueOf(vo.getSsh_port()));
			el.addAttribute("user", vo.getSsh_user());

			el = _root.addElement("web");
			el.addAttribute("url", vo.getWeb_url());

			writeTopoConf(TOPO_CONF_FNAME, doc);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg + e.getMessage());
		}

	}

	@Deprecated
	private static void saveNetworkToolsConfig() {
		String msg = "saveNetworkToolsConfig error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		try {

			Element NetworkToolConfE = (Element) doc.selectSingleNode("//conf/NetworkToolConf");
			if (NetworkToolConfE == null) {
				Element e = (Element) doc.selectSingleNode("//conf");
				NetworkToolConfE = e.addElement("NetworkToolConf");
			}
			Element root = (Element) doc.selectSingleNode("//conf/NetworkToolConf/config");
			if (root == null) {
				root = NetworkToolConfE.addElement("config");
			}

			// Element root =
			// doc.getRootElement().element("").addElement("config");

			for (NetworkToolVo vo : nwts.values()) {
				Element _root = NetworkToolConfE.addElement("device");
				_root.addAttribute("id", String.valueOf(vo.getResourceId()));
				Element el = _root.addElement("ping");
				el.addAttribute("count", String.valueOf(vo.getPing_count() == 0 ? 4 : vo.getPing_count()));
				el.addAttribute("timeout", String.valueOf(vo.getPing_timeout() == 0 ? 1000 : vo.getPing_timeout()));
				el = _root.addElement("telnet");
				el.addAttribute("port", vo.getTelnet_port() == 0 ? "23" : String.valueOf(vo.getTelnet_port()));
				el = _root.addElement("ssh");
				el.addAttribute("port", vo.getSsh_port() == 0 ? "22" : String.valueOf(vo.getSsh_port()));
				el.addAttribute("user", vo.getSsh_user());
				el = _root.addElement("web");
				el.addAttribute("url", vo.getWeb_url());
			}

			writeTopoConf(TOPO_CONF_FNAME, doc);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// OutputFormat format = OutputFormat.createPrettyPrint();
		// format.setEncoding(UTF8);
		// XMLWriter writer = null;
		// try {
		// File file = new File(getNetworkToolConfigFileName());
		// // file.deleteOnExit();
		// Writer xW = new OutputStreamWriter(new FileOutputStream(file), UTF8);
		// writer = new XMLWriter(xW, format);
		// writer.write(doc);
		// } catch (IOException e) {
		// e.printStackTrace();
		// log.error(e.getMessage(), e);
		// } finally {
		// try {
		// if (writer != null)
		// writer.close();
		// } catch (IOException e) {
		// System.out.println(e.getMessage());
		//
		// }
		// }
	}

	static Random r = new Random();

	public static String random(int a) {
		String val = String.valueOf(r.nextFloat() * a);
		return val.substring(0, val.indexOf("."));
	}

	public static Text doDefaultDomainText(Rect rect) {
		Text text = new Text();
		text.setId(rect.getId() + "-T");
		text.setFontSize("14");
		text.setX(String.valueOf(Double.parseDouble(rect.getX()) + Double.parseDouble(rect.getWidth()) / 2));
		text.setY(String.valueOf(Double.parseDouble(rect.getY()) + 20));
		text.setType(TopoConstant.TEXT);
		text.setName(rect.getName());
		return text;
	}

	public static NetworkToolVo readNetWorkConf(String devId) {
		NetworkToolVo tool = new NetworkToolVo();
		String msg = "initNetworkToolConfig error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();

			Element el = (Element) doc.selectSingleNode("//conf/NetworkToolConf/device[@id='" + devId + "']");

			if (el != null) {
				tool.setResourceId(devId);

				Element ping = el.element("ping");
				if (ping != null) {
					String count = ping.attributeValue("count");
					if (count == null) {
						tool.setPing_count(4);
					} else {
						tool.setPing_count(Integer.valueOf(count));
					}
					String timeout = ping.attributeValue("timeout");
					if (timeout == null) {
						tool.setPing_timeout(1000);
					} else {
						tool.setPing_timeout(Integer.valueOf(timeout));
					}
				}

				Element telnet = el.element("telnet");
				if (telnet != null) {
					String telnetPort = telnet.attributeValue("port");
					if (telnetPort == null) {
						tool.setTelnet_port(23);
					} else {
						tool.setTelnet_port(Integer.valueOf(telnetPort));
					}
				}

				Element ssh = el.element("ssh");
				if (ssh != null) {
					String sshPort = ssh.attributeValue("port");
					if (sshPort == null) {
						tool.setSsh_port(22);
					} else {
						tool.setSsh_port(Integer.valueOf(sshPort));
					}

					String user = ssh.attributeValue("user");
					if (user == null) {
						tool.setSsh_user("");
					} else {
						tool.setSsh_user(user);
					}

					String pwd = ssh.attributeValue("pwd");
					if (pwd == null) {
						tool.setSsh_pwd("");
					} else {
						tool.setSsh_pwd(pwd);
					}
				}

				
				Element web = el.element("web");
				if (web != null) {
					String url = web.attributeValue("url");
					if (url == null) {
						tool.setWeb_url("");
					} else {
						tool.setWeb_url(url);
					}
				}
			}
		} catch (Exception e) {
			log.error(msg + e.getMessage(), e);
			e.printStackTrace();
			throw new TopoException(msg + e.getMessage(), e);
		}

		return tool;

	}

	public static boolean hasDefaultView() {
		return loadDefaultView() != null;
	}

	private static Element loadDefaultView() {
		String msg = "loadDefaultView error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		String path = "//template/item[@type='default']/g";
		return (Element) doc.selectSingleNode(path);
	}

	private static Element loadViewConfEleById(String viewId) {
		String msg = "loadViewConfEleById error";
		Validate.notEmpty(viewId, msg + "empty viewId");
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		String path = "//template/item[@id='" + viewId + "']/g";

		return (Element) doc.selectSingleNode(path);

	}
	public static Map readTopoViewConf1(String topoViewXml){
		Map params = new HashMap();
		Document viewContent = changeStrToDom(topoViewXml);
		if(viewContent!=null){
			Element viewConf = (Element) viewContent.selectSingleNode("/g");
	
			if (viewConf == null) {
				return null;
			}else {
				// 安全域
				doRects(params, viewConf);
				// 设备
				doImages(params, viewConf);
				// 文本域
				doTexts(params, viewConf);
				doLines(params, viewConf);
				doPolyLine(params, viewConf);
				doTspan(params, viewConf);
				// horizon-start·添加circle选项-state
				doCircle(params, viewConf);
				// horizon-end
				return params;
			}
		}else{
			return null;
		}
	}
	public static Document changeStrToDom(String topoViewXml){
		Document viewContent = null;
		if(StringUtils.isNotEmpty(topoViewXml)){
			try {
				topoViewXml = URLDecoder.decode(topoViewXml, "utf-8");
				topoViewXml = topoViewXml.replaceAll("xlink:href", "href");
				viewContent = DocumentHelper.parseText(topoViewXml.trim());
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return viewContent;
	}
	@SuppressWarnings("unchecked")
	public static Map readTopoViewConf(String requestViewId) {
		String msg = "readTopoViewConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Map params = new HashMap();
		// params.put("Wdif", 0);
		// params.put("Hdif", 0);

		Element viewConf = loadViewConfEleById(requestViewId);

		if (viewConf == null) {
			return null;
		}else {
			doRects(params, viewConf);
			doImages(params, viewConf);
			doTexts(params, viewConf);
			doLines(params, viewConf);
			doPolyLine(params, viewConf);
			return params;
		}
	}
	//�����豸
	public static List selectImageList(String val,String viewId){
		String msg = "loadTopoServerInfo error";
		Document doc = null;
		List<Image> list = new ArrayList<Image>();
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element conf = (Element) doc.selectSingleNode("//templates/template/item [@id='" + viewId + "']/g");
		if(conf!=null){
			List<Element> images = conf.selectNodes("image");
			System.out.println(images.size());
			for (Iterator iterator = images.iterator(); iterator.hasNext();) {
				Element el = (Element) iterator.next();
				String name = el.attributeValue("name");
				String ip = el.attributeValue("ip");
				Image _el = new Image();
				_el.setId(el.attributeValue("id"));
				_el.setX(el.attributeValue("x"));
				_el.setY(el.attributeValue("y"));
				_el.setWidth(el.attributeValue("width"));
				_el.setHeight(el.attributeValue("height"));
				_el.setHref(el.attributeValue("href"));
				_el.setType(TopoConstant.IMAGE);
				_el.setModel(el.attributeValue("model"));
				_el.setIp(el.attributeValue("ip"));
				_el.setHostName(el.attributeValue("hostName"));
				_el.setName(el.attributeValue("name"));
				_el.setTransform(el.attributeValue("transform"));
				if(name.indexOf(val)!=-1||ip.indexOf(val)!=-1){
					list.add(_el);
				}
			}
		}
		if(list.size()==0){
			list = null;
		}
		return list;
	}

	public static Document readImportFile(InputStream in){
		SAXReader reader = new SAXReader();
		Document doc = null;
		String msg = "loadDocFile error";
		try {
			doc = reader.read(new InputStreamReader(in, UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg, e);
		} catch (DocumentException e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return doc;
	}

	private static Document loadDocFile(String fName) {
		SAXReader reader = new SAXReader();
		InputStream in = new ResourceLoader().loadFile(fName);
		Document doc = null;
		String msg = "loadDocFile error";
		try {
			doc = reader.read(new InputStreamReader(in, UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg, e);
		} catch (DocumentException e) {
			e.printStackTrace();
			log.error(msg, e);
			throw new TopoException(msg, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> readLinkConf() {
		String msg = "readTopoLinkConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		List<Element> lines = doc.selectNodes("//links/link");
		if (lines == null || lines.isEmpty()) {
		}
		Map<String, String> confs = new HashMap<String, String>();
		for (Element link : lines) {
			String linkName = link.attributeValue("linkName");
			String linkType = link.attributeValue("linkType");
			String viewLinkName = link.attributeValue("viewLinkName");
			confs.put(linkName + "/" + linkType, viewLinkName);
		}
		return confs;
	}

	public static void saveOrUpdateLinkConf(Map<String, String> line) {

		String linkName = line.get("linkName");
		String linkType = line.get("linkType");
		String viewLinkName = line.get("viewLinkName");

		if (StringUtils.isEmpty(linkName) || StringUtils.isEmpty(linkType) || StringUtils.isEmpty(viewLinkName)) {
			log.error("saveOrUpdateLinkConf error line param is empty");
			return;
		}

		String msg = "saveOrUpdateLinkConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element root = doc.getRootElement();

		Element confEle = root.element("conf");
		if (confEle == null) {
			confEle = root.addElement("conf");
		}

		Element listenerEle = confEle.element("listener");
		if (listenerEle == null) {
			listenerEle = confEle.addElement("listener");
		}

		Element linesEle = listenerEle.element("links");
		if (linesEle == null) {
			linesEle = listenerEle.addElement("links");
		}

		Node _line = linesEle.selectSingleNode("//links/link[@linkName='" + linkName + "']");
		if (_line != null) {
			linesEle.remove(_line);
		}
		Element linkEle = linesEle.addElement("link");
		linkEle.addAttribute("linkName", linkName);
		linkEle.addAttribute("linkType", linkType);
		linkEle.addAttribute("viewLinkName", viewLinkName);

		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding(UTF8);
		XMLWriter writer = null;

		try {
			File file = new File(TOPO_CONF_FNAME);
			writer = new XMLWriter(new FileWriter(file), format);
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("saveOrUpdateLinkConf error" + e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				log.error(e);
			}
		}

	}

	/**
	 * 得到topo安全域参数集合
	 * @param result
	 * @param root
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void doRects(Map result, Element root) {
		Map<String, Rect> _result = new HashMap<String, Rect>();

		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);

		Iterator itr = root.elements("rect").iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			if(el.attributeValue("id")==null||el.attributeValue("id").equals("")||el.attributeValue("id").indexOf("DM")==-1){
				continue;
			}
			Rect _el = new Rect();
			_el.setId(el.attributeValue("id"));
			_el.setX(adjustSize(el.attributeValue("x"), wdif));
			_el.setY(adjustSize(el.attributeValue("y"), hdif));
			_el.setName(el.attributeValue("name"));
			_el.setFill(el.attributeValue("fill"));
			_el.setWidth(el.attributeValue("width"));
			_el.setHeight(el.attributeValue("height"));
			_el.setStroke(el.attributeValue("stroke"));
			_el.setStrokeWidth(el.attributeValue("stroke-width"));
			_el.setStrokeDasharray(el.attributeValue("stroke-dasharray"));
			_el.setFillOpacity(el.attributeValue("fill-opacity"));
			_el.setRx(el.attributeValue("rx"));
			_el.setRy(el.attributeValue("ry"));
			_el.setType(TopoConstant.RECT);
			_el.setModel(el.attributeValue("model"));
			_el.setTransform(el.attributeValue("transform"));

			_result.put(el.attributeValue("id"), _el);
		}
		result.put(TopoConstant.RECT, _result);
	}

	private static String adjustSize(String srcPoint, Integer dif) {
		if (StringUtils.isEmpty(srcPoint))
			return "";
		if (dif == 0)
			return srcPoint;
		return String.valueOf(Integer.parseInt(srcPoint) + dif);

	}

	private static Integer getWdif(Map result) {
		return result.get("Wdif") == null ? 0 : Integer.parseInt(result.get("Wdif") + "");
	}

	private static Integer getHdif(Map result) {
		return result.get("Hdif") == null ? 0 : Integer.parseInt(result.get("Hdif") + "");
	}

	/**
	 * 获得topo设备信息
	 * @param result
	 * @param root
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	private static void doImages(Map result, Element root) {
		Map<String, Image> _result = new HashMap<String, Image>();

		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);

		List imgs = root.elements("image");
		Iterator itr = imgs.iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			if(el.attributeValue("id").endsWith("-F")||el.attributeValue("id").endsWith("-WARN")||el.attributeValue("id").endsWith("-tip")){
				continue;
			}else if(el.attributeValue("id")==null||el.attributeValue("id").equals("")||el.attributeValue("id").indexOf("DV")==-1){
				continue;
			}
			Image _el = new Image();
			_el.setId(el.attributeValue("id"));
			_el.setX(adjustSize(el.attributeValue("x"), wdif));
			_el.setY(adjustSize(el.attributeValue("y"), hdif));
			_el.setNodeId(el.attributeValue("nodeId"));
			_el.setWidth(el.attributeValue("width"));
			_el.setHeight(el.attributeValue("height"));
			_el.setHref(el.attributeValue("href"));
			_el.setType(TopoConstant.IMAGE);
			_el.setModel(el.attributeValue("model"));
			_el.setIp(el.attributeValue("ip"));
			_el.setHostName(el.attributeValue("hostName"));
			_el.setName(el.attributeValue("name"));
			_el.setGroupId(el.attributeValue("groupId"));
			_el.setTransform(el.attributeValue("transform"));

			// log.error("-->" + _el.getX() + "/" + _el.getY() );
			_result.put(el.attributeValue("id"), _el);
		}
		result.put(TopoConstant.IMAGE, _result);
	}

	private static void doLines(Map result, Element root) {
		Map<String, Line> _result = new HashMap<String, Line>();

		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);

		List line = root.elements("line");
		Iterator itr = line.iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			Line _el = new Line();
			_el.setId(el.attributeValue("id"));

			_el.setX1(adjustSize(el.attributeValue("x1"), wdif));
			_el.setY1(adjustSize(el.attributeValue("y1"), hdif));

			_el.setX2(adjustSize(el.attributeValue("x2"), wdif));
			_el.setY2(adjustSize(el.attributeValue("y2"), hdif));
			_el.setStrokeDasharray(el.attributeValue("stroke-dasharray"));
			_el.setStroke(el.attributeValue("stroke"));
			_el.setStrokeWidth(el.attributeValue("stroke-width"));
			_el.setType(TopoConstant.LINE);
			_result.put(el.attributeValue("id"), _el);
		}
		result.put(TopoConstant.LINE, _result);
	}
	private static void doPolyLine(Map result, Element root) {
		Map<String, PolyLine> _result = new HashMap<String, PolyLine>();

		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);
		String dash = "";
		List line = root.elements("polyline");
		Iterator itr = line.iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			PolyLine _el = new PolyLine();
			_el.setId(el.attributeValue("id"));
			_el.setPoints(el.attributeValue("points"));
			_el.setFill(el.attributeValue("fill"));
			_el.setStroke(el.attributeValue("stroke"));
			_el.setType(TopoConstant.POLY_LINE);
			_el.setStrokeDasharray(el.attributeValue("stroke-dasharray"));
			_el.setMarkerStart(el.attributeValue("marker-start"));
			_el.setMarkerEnd(el.attributeValue("marker-end"));
			_el.setStrokeWidth(el.attributeValue("stroke-width"));
			_result.put(el.attributeValue("id"), _el);
		}
		result.put(TopoConstant.POLY_LINE, _result);
	}

	/**
	 * horizon-start·添加circle选项-state
	 * @param result
	 * @param root
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void doCircle(Map result, Element root) {
		Map<String, Circle> _result = new HashMap<String, Circle>();
		Iterator itr = root.elements("circle").iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			if(el.attributeValue("id").indexOf("-state")!=-1){
				Circle circleBean = new Circle();
				circleBean.setId( el.attributeValue("id"));
				circleBean.setCx( el.attributeValue("cx"));
				circleBean.setCy( el.attributeValue("cy"));
				circleBean.setR( el.attributeValue("r"));
				circleBean.setStyle( el.attributeValue("style"));
				circleBean.setType("circle");
				_result.put(el.attributeValue("id"), circleBean);
			}
		}
		result.put(TopoConstant.CIRCLE, _result);
	}
	// horizon-end
	private static void doTspan(Map result, Element root) {
		Map<String, Tspan> _result = new HashMap<String, Tspan>();
		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);
		Iterator itr = root.elements("text").iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			if(el.attributeValue("id").indexOf("-ET")!=-1){
				String str = el.asXML();
				Tspan tspan = new Tspan();
				tspan.setSpan(str);
				tspan.setId(el.attributeValue("id"));
				tspan.setType(TopoConstant.TSPAN);
				_result.put(el.attributeValue("id"), tspan);
			}
		}
		result.put(TopoConstant.TSPAN, _result);
	}

	/**
	 * -T -alarmText
	 * @param result
	 * @param root
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void doTexts(Map result, Element root) {
		Map<String, Text> _result = new HashMap<String, Text>();

		Integer wdif = getWdif(result);
		Integer hdif = getHdif(result);

		Iterator itr = root.elements("text").iterator();
		for (; itr.hasNext();) {
			Element el = (Element) itr.next();
			// String str = el.asXML();
			if(StringUtils.isEmpty(el.attributeValue("id"))||el.attributeValue("id").indexOf("-ET")!=-1){
				continue;
			}
			Text _el = new Text();
			_el.setId(el.attributeValue("id"));

			_el.setX(adjustSize(el.attributeValue("x"), wdif));
			_el.setY(adjustSize(el.attributeValue("y"), hdif));
			_el.setFontSize(el.attributeValue("font-size"));
			_el.setName(el.getText());
			_el.setIsShow(el.attributeValue("visibility"));
			_el.setType(TopoConstant.TEXT);
			_el.setTransform(el.attributeValue("transform"));
			_el.setWritingMode(el.attributeValue("writing-mode"));
			_el.setStyle(el.attributeValue("style"));
			_result.put(el.attributeValue("id"), _el);
		}
		result.put(TopoConstant.TEXT, _result);
	}


	public static String getWarningCon() {
		File file = new File("../../../../conf/topoConf.properties");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			Properties propertires = new Properties();
			propertires.load(new FileInputStream(file));

			System.out.println("test=" + propertires.getProperty("condition"));

			return propertires.getProperty("condition");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static String toString(Date date) {
		Validate.notNull(date);
		return df.format(date);
	}

	@Deprecated
	public static String requestURLOld(String url, Map params) throws Exception {
		String charset = HTTP.UTF_8;
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		HttpEntity entity = null;
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			for (Iterator it = params.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = params.get(key);
				nvps.add(new BasicNameValuePair(String.valueOf(key), String.valueOf(value)));
			}
		}
		post.setEntity(new UrlEncodedFormEntity(nvps, charset));
		response = client.execute(post);
		try {
			entity = response.getEntity();
			if (response.getEntity() != null) {
				return EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (entity != null)
				entity.consumeContent();
			client.getConnectionManager().shutdown();
		}

		return null;
	}

	public static String requestURL(String url, Map params) throws Exception {
		String charset = HTTP.UTF_8;
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			for (Iterator it = params.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = params.get(key);
				nvps.add(new BasicNameValuePair(String.valueOf(key), String.valueOf(value)));
			}
		}
		post.setEntity(new UrlEncodedFormEntity(nvps, charset));
		response = client.execute(post);
		HttpEntity en = response.getEntity();

		if (en != null && en.isStreaming()) {
			// log.error("is stream");
			InputStream rpsRetrunStream = en.getContent();
			if (rpsRetrunStream != null) {

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte b[] = new byte[1024];
				while (rpsRetrunStream.read(b) != -1) {
					bos.write(b);
				}
				try {
				} finally {
					rpsRetrunStream.close();
					bos.close();
				}
				return bos.toString(HTTP.UTF_8);
			}
		}

		try {
			int statusCode = response.getStatusLine().getStatusCode();
			return "request statusCode " + statusCode;
		} catch (Exception e) {
			throw e;
		} finally {
			if (en != null)
				en.consumeContent();
			client.getConnectionManager().shutdown();
		}

	}

	public static String call(String url, String order, Map<String, String> param) throws Exception {
		log.info("calling..." + url);
		String state = TopoUtil.requestURL(url, param);
		log.info("calling finished! ");
		return state;
	}

	public static String callServer(String serverIp, String order, Map<String, String> param) throws Exception {
		String url = "http://" + serverIp + ":8080/sim-web/topo/jumpTopo?order=" + order;
		return call(url, order, param);
	}


	public static String[] getMapTmplateSize() {
		String msg = "getMapTmplateSize error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element tmpEle = (Element) doc.selectSingleNode("//template");
		if (tmpEle == null) {
			throw new TopoException(TopoConstant.TOPO_MAPNAME_SHAN_XI + " not find template element");
		}

		String winWidth = tmpEle.attributeValue("winWidth");
		String winHeigth = tmpEle.attributeValue("winHeigth");

		Validate.notEmpty(winWidth, TopoConstant.TOPO_MAPNAME_SHAN_XI + "winWidth must not null");
		Validate.notEmpty(winHeigth, TopoConstant.TOPO_MAPNAME_SHAN_XI + "winHeigth must not null");

		String[] size = new String[2];
		size[0] = winWidth;
		size[1] = winHeigth;
		return size;
	}

	public static List<ItemVo> findTopoViewList(){
		List<ItemVo> list = new ArrayList<ItemVo>();
		String msg = "getMapTmplateSize error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		List<Element> tmpEle = doc.selectNodes("//item");
		for(Element ele:tmpEle){
			ItemVo item = new ItemVo();
			String id= ele.attributeValue("id");
			String name = ele.attributeValue("name");
			String pid = ele.attributeValue("parent");
			String nodeId = ele.attributeValue("nodeId");
			item.setId(id);
			item.setText(name);
			item.setPid(pid);
			item.setNodeId(nodeId);
			list.add(item);
			
		}
		return list;
	}
	public static void addTopoToView(String viewId,String url){
		String msg = "getMapTmplateSize error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		if(url==null){
			Element itemEle = (Element) doc.selectSingleNode("//templates/template/item [@id='" + viewId+ "']");
			itemEle.setAttributeValue("isView","true");
		}else{
			Element topoElement = (Element) doc.selectSingleNode("/topo");
			Element  viewConfElement = (Element) doc.selectSingleNode("/topo/viewConf");
			if(viewConfElement==null){
				viewConfElement = topoElement.addElement("viewConf");
			}else{
				List<Element> iframes = viewConfElement.selectNodes("/iframe");
				for(Element ele:iframes){
					if(ele.attributeValue("url").equals(url)){
						ele.addAttribute("isView","true");
					}else{
						Element iframe = viewConfElement.addElement("iframe");
						iframe.addAttribute("id", UUID.randomUUID().toString());
						iframe.addAttribute("url", url);
						iframe.addAttribute("isView", "true");
						iframe.addAttribute("name", "���ղ�");
						
					}
				}
			}
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
	}
	public static List<ItemVo> findTopoViewList1(){
		List<ItemVo> list = new ArrayList<ItemVo>();
		String msg = "getMapTmplateSize error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		List<Element> tmpEle = doc.selectNodes("//item");
		List<Element> iframes = doc.selectNodes("//iframe");
		int k = tmpEle.size()+iframes.size();
		for(Element ele:tmpEle){
			ItemVo item = new ItemVo();
			String id= ele.attributeValue("id");
			String name = ele.attributeValue("name");
			String _id = ele.attributeValue("_id");
			String time = ele.attributeValue("time");
			if(ele.attributeValue("isView")!=null&&ele.attributeValue("isView").equals("false")){
				continue;
			}
			if(!StringUtils.isNotEmpty(time)){
				item.setTime(Integer.parseInt("5"));
			}else{
				item.setTime(Integer.parseInt(time));
			}
			item.setId(id);
			item.setText(name);
			if(StringUtils.isNotEmpty(_id)){
				item.set_id(Integer.parseInt(_id));
			}else{
				item.set_id(k+1);
				k = k+1;
			}
			//item.setPid(pid);
			list.add(item);
			
		}
		for(Element ele:iframes){
			ItemVo item = new ItemVo();
			String id= ele.attributeValue("id");
			String name = ele.attributeValue("name");
			String _id = ele.attributeValue("_id");
			String url = ele.attributeValue("url");
			String time = ele.attributeValue("time");
			if(ele.attributeValue("isView")!=null&&ele.attributeValue("isView").equals("false")){
				continue;
			}
			item.setId(id);
			item.setText(name);
			if(StringUtils.isNotEmpty(_id)){
				item.set_id(Integer.parseInt(_id));
			}else{
				item.set_id(k+1);
				k = k+1;
			}
			if(!StringUtils.isNotEmpty(time)){
				item.setTime(Integer.parseInt("5"));
			}else{
				item.setTime(Integer.parseInt(time));
			}
			item.setUrl(url);
			list.add(item);
			
		}
		ComparatorItem comparatorItem = new ComparatorItem();
		Collections.sort(list,comparatorItem);
		return list;
	}
	public static class ComparatorItem implements Comparator{
		 public int compare(Object arg0, Object arg1) {
		  ItemVo item0=(ItemVo)arg0;
		  ItemVo item1=(ItemVo)arg1;
		  int flag=item0.get_id().compareTo(item1.get_id());
		   return flag;
		  }  
		 
	}
	public static Map<String, Statistic> changeJsonToMap(String json){
		Gson gson = new Gson();
		Map<String,Statistic> map = new HashMap<String, Statistic>();
		if(StringUtils.isNotEmpty(json)){
			map = gson.fromJson(json, new TypeToken<Map<String,Statistic>>(){}.getType());
		}
		return map;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> changeLineId2Map(String lineId){
		Map map = new HashMap<String, String>();
		String devId2 = "-DV"+lineId.split(".DK")[1].split("-DV")[1];
		String devId1 = "-DV"+lineId.split(".DK")[0].split("-DV")[1];	
		String dk1 = lineId.split(".DK")[0].split("--DV")[0].substring(2, lineId.split(".DK")[0].split("--DV")[0].length());
		String dk2 = lineId.split(".DK")[1].split("--DV")[0];
		map.put("id1", devId1);
		map.put("id2", devId2);
		map.put("dk1", dk1);
		map.put("dk2", dk2);
		return map;
	}
	
	//��ӻ��߸ı�ĳ���豸��State
	public static String changeOrAddStateByDevId(String json,String type,String devId,String states){
		Gson gson = new Gson();
		String jsonString = null;
		if(StringUtils.isNotEmpty(json)){
			Map<String,Statistic> map = gson.fromJson(json, new TypeToken<Map<String,Statistic>>(){}.getType());
			if(map!=null&&map.size()!=0){
				if(map.containsKey(devId)){
					Statistic statistic = map.get(devId);
					statistic.setState(states);
					map.put(devId, statistic);
					jsonString = TopoUtil.toJson(map);
				}else{
					Statistic statistic = new Statistic();
					statistic.setDevId(devId);
					statistic.setType(type);
					statistic.setState(states);
					map.put(devId, statistic);
					jsonString = TopoUtil.toJson(map);
				}
				
			}
		}else{
			Map<String, Statistic> map = new HashMap<String, Statistic>();
			Statistic statistic = new Statistic();
			statistic.setDevId(devId);
			statistic.setType(type);
			statistic.setState(states);
			map.put(devId, statistic);
			jsonString = TopoUtil.toJson(map);
		}
		return jsonString;
	}
	public static String getStateByDevId(String json,String devId){
		Gson gson = new Gson();
		String args = null;
		if(StringUtils.isNotEmpty(json)){
			Map<String,Statistic> map = gson.fromJson(json, new TypeToken<Map<String,Statistic>>(){}.getType());
			if(map!=null&&map.size()!=0){
				if(map.containsKey(devId)){
					Statistic statistic =  map.get(devId);
					args = statistic.getState();
				}
			}
		}
		return args;
	}
	//��ӻ��߸ı�ĳ�������豸��State
	public static String changeOrAddStateByType(String json,String type,String states){
		Gson gson = new Gson();
		String jsonString = null;
		if(StringUtils.isNotEmpty(json)){
			Map<String,String> map = gson.fromJson(json, new TypeToken<Map<String,String>>(){}.getType());
			if(map!=null&&map.size()!=0){
				map.put(type, states);
			}else{
				map = new HashMap<String, String>();
				map.put(type, states);
			}
			jsonString = TopoUtil.toJson(map);
		}else{
			Map<String, String> map = new HashMap<String, String>();
			map.put(type, states);
			jsonString = TopoUtil.toJson(map);
		}
		return jsonString;
	}
	public static void saveTreeToXML(String json){
		Gson gson = new Gson();
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Element itemEle1 = null;
		Element itemEle = null;
		List<ItemVo> list = gson.fromJson(json, new TypeToken<List<ItemVo>>(){}.getType());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			ItemVo itemVo = (ItemVo) iterator.next();
			System.out.println(itemVo.getId());
			List<ItemVo> chiItemVos = itemVo.getChildren();
			itemEle = (Element) doc.selectSingleNode("//templates/template/item [@id='" + itemVo.getId() + "']");
			if (itemEle != null) {
				itemEle.addAttribute("parent", itemVo.getPid());
				itemEle.addAttribute("name", itemVo.getText());
			}
			if(chiItemVos!=null){
				for (Iterator iterator2 = chiItemVos.iterator(); iterator2.hasNext();) {
					ItemVo itemVo1 = (ItemVo) iterator2.next();
					System.out.println(itemVo1.getId());
					itemEle1 = (Element) doc.selectSingleNode("//templates/template/item [@id='" + itemVo1.getId() + "']");
					if (itemEle1 != null) {
						itemEle1.addAttribute("parent", itemVo1.getPid());
						itemEle1.addAttribute("name", itemVo1.getText());
					}
				}
			}
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
		
	}
	public static void saveTreeFromView(String json){
		Gson gson = new Gson();
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Element itemEle = null;
		List<ItemVo> list = gson.fromJson(json, new TypeToken<List<ItemVo>>(){}.getType());
		for (int i=0;i<list.size();i++) {
			ItemVo itemVo = (ItemVo) list.get(i);
			itemEle = (Element) doc.selectSingleNode("//templates/template/item [@id='" + itemVo.getId() + "']");
			if (itemEle != null) {
				itemEle.addAttribute("_id",i+1+"");
				itemEle.addAttribute("time", itemVo.getTime().toString());
			}else{
				itemEle = (Element) doc.selectSingleNode("//viewConf/iframe [@id='" + itemVo.getId() + "']");
				if(itemEle!=null){
					itemEle.addAttribute("_id",i+1+"");
					itemEle.addAttribute("time", itemVo.getTime().toString());
				}
			}
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
		
	}
	public static boolean deleteViewById(String id){
		Document doc = null;
		try {
			doc = readTopoConfDoc();
			Element itemEle = (Element) doc.selectSingleNode("//templates/template/item [@id='" + id + "']");
			if (itemEle != null) {
				itemEle.addAttribute("isView","false");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
		return true;
	}
	public static void putDevIntoArea(String viewId,String imageID,String viewName){
		Document doc = null;
		String msg = "��������";
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		String textId = imageID+"-T";
		Element image = (Element) doc.selectSingleNode("//templates/template/item/g/image[@id='" + imageID + "']");
		Element text = (Element) doc.selectSingleNode("//templates/template/item/g/text[@id='" + textId + "']");
		Element templatesEle = (Element) doc.selectSingleNode("//templates");
		if (templatesEle == null) {
			templatesEle = doc.getRootElement().addElement("templates");
		}
		Element templateEle = (Element) doc.selectSingleNode("//templates/template");
		if (templateEle == null) {
			templateEle = templatesEle.addElement("template");
		}
		String id = "Area-"+viewId;
		Element conf = (Element) doc.selectSingleNode("//templates/template/item [@id='" + id + "']/g");
		if(conf==null){
			Element  item= templateEle.addElement("item");
			
			item.addAttribute("id", id);
			item.addAttribute("name", viewName);
			item.addAttribute("width", "833");
			item.addAttribute("height", "528");
			item.addAttribute("type", "areaType");
			Element g = item.addElement("g");
			g.addAttribute("id", "topo");
			
		}
		Element newNodeElement = (Element)image.createCopy();
		Element newTextElement = (Element)text.createCopy();
		conf.add(newNodeElement);
		conf.add(newTextElement);
		try{
			writeTopoConf(TOPO_CONF_FNAME, doc);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public static void copyTopoToView(String toId,Map<String, String> imageMap){
		List<Element> list = new ArrayList<Element>();
		List<Element> textList = new ArrayList<Element>();
		List<Element> rectList = new ArrayList<Element>();
		List<Element> rectTList = new ArrayList<Element>();
		List<Element> lineList = new ArrayList<Element>();
		List<Element> lineList1 = new ArrayList<Element>();
		Element viewConf = loadViewConfEleById("doamin.shanXi.xiAn");
		List imgs = viewConf.elements("image");
		Iterator itr = imgs.iterator();
		for (; itr.hasNext();) {
			Element ele = (Element) itr.next();
			String id = ele.attributeValue("id");
			if(imageMap.containsKey(id)){
				list.add(ele);
			}
		}
		List imageText = viewConf.elements("text");
		Iterator itrText = imageText.iterator();
		for (; itrText.hasNext();) {
			Element ele = (Element) itrText.next();
			String id = ele.attributeValue("id");
			if(id.indexOf("DV")==-1){
				if(imageMap.containsValue(id.split("-")[0])){
					textList.add(ele);
				}
			}else if(id.indexOf("DV")!=-1){
				if(imageMap.containsKey(id.split("-T")[0])){
					rectTList.add(ele);
				}
			}
		}
		List rect = viewConf.elements("rect");
		Iterator it = rect.iterator();
		for (; it.hasNext();) {
			Element ele = (Element) it.next();
			String id = ele.attributeValue("id");
			if(id.indexOf("DM")!=-1){
				
				if(imageMap.containsValue(id)){
					rectList.add(ele);
				}
				
			}
				
		}
		List polyline = viewConf.elements("polyline");
		Iterator poly = polyline.iterator();
		for (; poly.hasNext();) {
			Element ele = (Element) poly.next();
			String id = ele.attributeValue("id");
			if(id.indexOf("DM")!=-1){
				if(imageMap.containsKey("DM"+id.split(".DK")[0].split("DM")[1])){
					if(imageMap.containsKey("DM"+id.split(".DK")[1].split("DM")[1])){
						lineList.add(ele);
					}
				}
			}
			
		}
		List line = viewConf.elements("line");
		Iterator line1 = line.iterator();
		for (; line1.hasNext();) {
			Element ele = (Element) line1.next();
			String id = ele.attributeValue("id");
			if(id.indexOf("DM")!=-1){
				if(imageMap.containsKey("DM"+id.split(".DK")[0].split("DM")[1])){
					if(imageMap.containsKey("DM"+id.split(".DK")[1].split("DM")[1])){
						lineList1.add(ele);
					}
				}
			}
			
		}
		Document doc = null;
		String viewId = toId;
		String msg = "��������";
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element conf = (Element) doc.selectSingleNode("//templates/template/item [@id='" + viewId + "']/g");
		if(conf!=null){
			for (int i = 0; i < list.size(); i++) {
				//conf.add(list.get(i));
				Element ele = list.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
			for(int i=0;i<textList.size();i++){
				Element ele = textList.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
			for(int i=0;i<rectTList.size();i++){
				Element ele = rectTList.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
			for(int i=0;i<rectList.size();i++){
				Element ele = rectList.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
			for(int i=0;i<lineList.size();i++){
				Element ele = lineList.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
			for(int i=0;i<lineList1.size();i++){
				Element ele = lineList1.get(i); 
				Element newNodeElement = (Element)ele.createCopy();
				conf.add(newNodeElement);
			}
		}
		try{
			writeTopoConf(TOPO_CONF_FNAME, doc);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static void exportXmlFile(String path){
		File assf=new File(TOPO_CONF_FNAME);
		try {
			InputStream streamIn = new FileInputStream(assf);
			OutputStream streamOut = new FileOutputStream(path+"//topoConf.xml");
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while((bytesRead = streamIn.read(buffer, 0, 8192)) != -1) {
				streamOut.write(buffer, 0, bytesRead);
			}
			streamOut.close();
			streamIn.close();
		} catch (FileNotFoundException e) {
			log.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		}
	}
	public static void importXmlFile(String path){
		File assf=new File(path);
		try {
			InputStream streamIn = new FileInputStream(assf);
			OutputStream streamOut = new FileOutputStream(TOPO_CONF_FNAME);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while((bytesRead = streamIn.read(buffer, 0, 8192)) != -1) {
				streamOut.write(buffer, 0, bytesRead);
			}
			streamOut.close();
			streamIn.close();
		} catch (FileNotFoundException e) {
			log.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e);
			e.printStackTrace();
		}
	}
	public static void createTopoView(String id,String name,String pid){
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Element viewElement = (Element) doc.selectSingleNode("//templates/template/item [@id='"+id+"']");
		if(viewElement!=null){
			return;
		}
		Element templatesEle = (Element) doc.selectSingleNode("//templates");
		if (templatesEle == null) {
			templatesEle = doc.getRootElement().addElement("templates");
		}
		Element templateEle = (Element) doc.selectSingleNode("//templates/template");
		if (templateEle == null) {
			templateEle = templatesEle.addElement("template");
		}
		Element  item= templateEle.addElement("item");
		item.addAttribute("id", id);
		item.addAttribute("name", name);
		item.addAttribute("width", "833");
		item.addAttribute("height", "528");
		item.addAttribute("parent", pid);
		item.addAttribute("type", "areaType");
		Element g = item.addElement("g");
		g.addAttribute("id", "topo");
		try{
			writeTopoConf(TOPO_CONF_FNAME, doc);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	/**
	 * 保存设置背景
	 * @param url
	 */
	public static synchronized void saveBgView(String url) {
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "加载TOPOConf错误";
			throw new TopoException(msg, e);
		}
		Element ele = (Element) doc.selectSingleNode("//bgView");
		Element eleBg = (Element) doc.selectSingleNode("//bgView/bg");
		if(eleBg!=null){
			eleBg.setAttributeValue("url", url);
		}else{
			eleBg = ele.addElement("bg");
			eleBg.addAttribute("url",url);
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
	}
	public static synchronized String findBgView(){
		String str = "";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "加载TOPOConf错误";
			throw new TopoException(msg, e);
		}
		Element eleBg = (Element) doc.selectSingleNode("//bgView/bg");
		if(eleBg==null){
			str="../images/base.jpg";
		}else{
			str = eleBg.attributeValue("url");
		}
		return str;
	}
	/**public static synchronized void getAllImages(String topoView,String viewId,TopoCache topoCache) {
		Document viewContent = null;
		try {
			viewContent = DocumentHelper.parseText(topoView.trim());
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String,String> map = new HashMap<String, String>();
		// remove warn ele
		List<Element> imgs = viewContent.selectNodes("//image");
		for (Element ele : imgs) {
			String id = ele.attributeValue("id");
			if(id.indexOf("DV")!=-1){
				String ip = ele.attributeValue("ip");
				map.put(id, ip);
				topoCache.cacheDevInfo("");
			}
		}
	}*/
	public static synchronized void saveOrUpdateTopoViewConf(String topoViewXml, String viewId) {
		Validate.notEmpty(topoViewXml, "saveOrUpdateTopoViewConf error topoViewXml is empty");
		Validate.notEmpty(viewId, "saveOrUpdateTopoViewConf error viewType is empty");
		topoViewXml = topoViewXml.replaceAll("xlink:href", "href");

		String msg = "saveOrUpdateTopoViewConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element templatesEle = (Element) doc.selectSingleNode("//templates");
		if (templatesEle == null) {
			templatesEle = doc.getRootElement().addElement("templates");
		}

		Element templateEle = (Element) doc.selectSingleNode("//templates/template");
		if (templateEle == null) {
			templateEle = templatesEle.addElement("template");
		}

		Element itemEle = (Element) doc.selectSingleNode("//templates/template/item [@id='" + viewId + "']");

		if (itemEle == null) {
			templateEle.addElement("item");

			if (TopoUtil.isAreaViewType(viewId)) {
				templateEle.addAttribute("type", TopoConstant.TOPO_VTYPE_AREA);
			} else {
				templateEle.addAttribute("type", TopoConstant.TOPO_VTYPE_DOMAIN);
			}
			templateEle.addAttribute("id", viewId);

		} else {
			Element view = itemEle.element("g");
			if (view != null) {
				itemEle.remove(view);
			}
		}
		Element viewXML = null;
		try {
			Document viewContent = DocumentHelper.parseText(topoViewXml.trim());

			// remove warn ele
			List<Element> imgs = viewContent.selectNodes("//image");
			for (Element ele : imgs) {
				String id = ele.attributeValue("id");
				if (StringUtils.isNotEmpty(id) && id.endsWith("-WARN")) {
					boolean re = viewContent.getRootElement().remove(ele);
					log.debug("rmove warn node  " + re + " " + ele.attributeValue("x"));
				}
				if (StringUtils.isNotEmpty(id) && id.endsWith("new ")) {
					boolean re = viewContent.getRootElement().remove(ele);
					log.debug("rmove warn node  " + re + " " + ele.attributeValue("x"));
				}
				if(!StringUtils.isNotEmpty(id)){
					boolean re = viewContent.getRootElement().remove(ele);
					log.debug("rmove warn node  " + re + " " + ele.attributeValue("x"));
				}
				
				if (StringUtils.isNotEmpty(id) && id.indexOf("logo")!=-1) {
					boolean re = viewContent.getRootElement().remove(ele);
					log.debug("rmove warn node  " + re + " " + ele.attributeValue("x"));
				}
				if (StringUtils.isNotEmpty(id) && id.indexOf("-F")!=-1) {
					boolean re = viewContent.getRootElement().remove(ele);
					log.debug("rmove warn node  " + re + " " + ele.attributeValue("x"));
				}
			}
			List<Element> aa = viewContent.selectNodes("//a");
			for (Element ele : aa) {
				viewContent.getRootElement().remove(ele);
			}

			// remove lock
			List<Element> texts = viewContent.selectNodes("//text");
			for (Element ele : texts) {
				String id = ele.attributeValue("id");
				if (StringUtils.isNotEmpty(id) && id.indexOf("devInfoTextLock-") != -1) {
					boolean re = viewContent.getRootElement().remove(ele);
				}else if(StringUtils.isNotEmpty(id) && id.indexOf("devInfoText-") != -1){
					boolean re = viewContent.getRootElement().remove(ele);
				}
			}
			// remove lock
			List<Element> rects = viewContent.selectNodes("//rect");
			for (Element ele : rects) {
				String id = ele.attributeValue("id");
				if (StringUtils.isNotEmpty(id) && id.indexOf("devInfoRectLock-") != -1) {
					boolean re = viewContent.getRootElement().remove(ele);
				}else if(StringUtils.isNotEmpty(id) && id.indexOf("devInfoRect-") != -1){
					boolean re = viewContent.getRootElement().remove(ele);
				}else if(StringUtils.isNotEmpty(id) && id.indexOf("rectGroup") != -1){
					boolean re = viewContent.getRootElement().remove(ele);
				}else if(StringUtils.isNotEmpty(id) && id.indexOf("rectObj") != -1){
				boolean re = viewContent.getRootElement().remove(ele);
				}
			}

			// rollback warn line color
			List<Element> lines = viewContent.selectNodes("//line");
			for (Element line : lines) {
				String lineId = line.attribute("id").getValue();
				if (lineId.indexOf("area-") != -1) {
					line.setAttributeValue("stroke", "red");
				}
			}
			viewXML = viewContent.getRootElement();
			itemEle.add(viewXML);
		} catch (DocumentException e) {
			e.printStackTrace();
			String error = "saveOrUpdateViewTopoConf error";
			log.error(error, e);
			throw new TopoException(error, e);
		}

		writeTopoConf(TOPO_CONF_FNAME, doc);
	}
	public static synchronized void createNewFile(String view){
		Document doc = changeStrToDom(view);
		writeTopoConf(TOPO_VIEW,doc);
	}
	private static synchronized void writeTopoConf(String fName, Document doc) {
		XMLWriter writer = null;
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding(UTF8);
		try {
			Writer xW = new OutputStreamWriter(new FileOutputStream(new File(fName)), UTF8);
			writer = new XMLWriter(xW, format);
			writer.write(doc);
			log.info("saveOrUpdateViewTopoConf success !");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());

			}
		}
	}

	public static List<DevManage> loadDevsManage() {
		String msg = "loadDevsManage error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		List<Element> devs = doc.selectNodes("//conf/devConf/dev");
		List<DevManage> list = new ArrayList<DevManage>();
		if (devs != null) {

			for (Element dev : devs) {
				String devId = dev.attributeValue("id");
				List<Element> ms = dev.elements("manage");
				for (Element me : ms) {
					DevManage m = new DevManage();

					Element nameEle = me.element("name");
					if (nameEle == null) {
						m.setName("");
					} else {
						m.setName(nameEle.getTextTrim());
					}

					Element urlEle = me.element("url");
					if (urlEle == null) {
						log.error("error conf dev manage, null url");
						m.setUrl("#");
					} else {
						m.setUrl(urlEle.getTextTrim());
					}

					m.setDevId(devId);
					m.setDevId(devId);

					list.add(m);
				}
			}
		}
		return list;
	}

	public static void saveDevManage(String devId, String name, String url) {
		String msg = "saveDevManage error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element confEle = (Element) doc.selectSingleNode("/topo/conf");
		if (confEle == null) {
			confEle = doc.getRootElement().addElement("conf");
		}

		Element devConfEle = (Element) doc.selectSingleNode("//conf/devConf");
		if (devConfEle == null) {
			devConfEle = confEle.addElement("devConf");
		}

		Element devEle = (Element) doc.selectSingleNode("//devConf/dev[@id='" + devId + "']");
		if (devEle == null) {
			devEle = devConfEle.addElement("dev");
			devEle.addAttribute("id", devId);
		}

		Element manage = devEle.addElement("manage");

		Element nameEle = manage.addElement("name");
		nameEle.setText(name);

		Element urlEle = manage.addElement("url");
		urlEle.setText(url);

		writeTopoConf(TOPO_CONF_FNAME, doc);

	}

	public static Document readTopoConfDoc() {
		return loadDocFile(TOPO_CONF_FNAME);
	}

	public static Document readTopoDevConfDoc() {
		return loadDocFile(DEV_CONF);
	}

	private static Element findViewEle(String viewType) {
		String msg = "findViewEle error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element viewEle = (Element) doc.selectSingleNode("//templates/template/item[@type='" + TopoConstant.TOPO_VTYPE_DOMAIN + "']/g");
		return viewEle;
	}

	@SuppressWarnings("unchecked")
	public static Set<String> readTopoDomains() {
		Set<String> set = new HashSet<String>();
		Element tagetItem = findViewEle(TopoConstant.TOPO_VTYPE_DOMAIN);

		if (tagetItem == null) {
			return set;
		}

		List<Element> domainEles = tagetItem.elements("rect");
		for (Element domainEle : domainEles) {
			String id = domainEle.attributeValue("id");
			if (StringUtils.isNotEmpty(id) && id.startsWith("DM") && id.indexOf("-") == -1) {
				set.add(id);
			}
		}
		log.error("domain:" + set);
		return set;
	}
	public static Set<String> readTopoDomains1(String viewString){
		Set<String> set = new HashSet<String>();
		Document topoConstant =  changeStrToDom(viewString);
		if(topoConstant!=null){
			List<Element> domainEles = topoConstant.selectNodes("//rect");
			for (Element domainEle : domainEles) {
				String id = domainEle.attributeValue("id");
				if (StringUtils.isNotEmpty(id) && id.startsWith("DM") && id.indexOf("-") == -1) {
					set.add(id);
				}
			}
			log.error("domain:" + set);
		}
		return set;
	}

	public static Set<DevStatus> readTopoEvtDef() {
		String file = "../../../../conf/eventtype.xml";
		SAXReader reader = new SAXReader();
		Set<DevStatus> set = new LinkedHashSet<DevStatus>();
		Document doc = null;
		try {
			File evtDefFile = new File(file);
			doc = reader.read(new InputStreamReader(new FileInputStream(evtDefFile)));
			Element root = doc.getRootElement();
			List<Element> mainTypesEles = root.elements("category");
			for (Element node : mainTypesEles) {
				List<Element> subNodes = node.elements("category");
				if (subNodes != null && !subNodes.isEmpty()) {
					for (Element sub : subNodes) {
						EventTypeVo subType = new EventTypeVo();
						String subName = sub.attributeValue("name");
						String subDesc = sub.attributeValue("description");
						DevStatus eDef = new DevStatus(subName, subName, subDesc);
						set.add(eDef);
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		return set;
	}
	public static Set<DevStatus> readTopoStateDef() {
		String msg = "readTopoStateDef error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		List<Element> evtDefs = doc.selectNodes("//conf/stateConf/evt");
		Set<DevStatus> set = new LinkedHashSet<DevStatus>();
		if (evtDefs != null && !evtDefs.isEmpty()) {
			for (Element def : evtDefs) {
				String id = def.attributeValue("id");
				String name = def.attributeValue("name");
				String col = def.attributeValue("col");
				String postfix = def.attributeValue("postfix");
				String evaluator = def.attributeValue("evaluator");

				DevStatus eDef = new DevStatus(id, name, col);

				if (StringUtils.isNotEmpty(postfix)) {
					eDef.setPostfix(postfix);
				}
				if (StringUtils.isNotEmpty(evaluator)) {
					eDef.setEvaluator(evaluator);
				}

				set.add(eDef);
			}
		}
		return set;
	}
	public static Map<String, String> readTopoUIConf(String viewId) {

		String msg = "getTopoUIConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element UIConf = (Element) doc.selectSingleNode("//conf/topoUIConf");
		Map<String, String> m = new HashMap<String, String>();

		String svgWidth = "svgWidth";
		String svgHeigth = "svgHeigth";
		Element svgEle = UIConf.element("svg");
		m.put(svgWidth, svgEle.attributeValue(svgWidth, ""));
		m.put(svgHeigth, svgEle.attributeValue(svgHeigth, ""));
		Element areaImgEle = null;
		String path = "//template/item[@id='" + viewId + "']";
		try {
			areaImgEle = (Element) doc.selectSingleNode(path);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TopoException("undefine node " + viewId + " " + path);
		}
		String areaImgName = "img";
		String width = "width";
		String heigth = "heigth";
		String areaImgX = "areaImgX";
		String areaImgY = "areaImgY";

		m.put(areaImgName, areaImgEle.attributeValue(areaImgName, "").trim());
		m.put(width, areaImgEle.attributeValue(width, ""));
		m.put(heigth, areaImgEle.attributeValue(heigth, ""));
		m.put(areaImgX, areaImgEle.attributeValue("x", ""));
		m.put(areaImgY, areaImgEle.attributeValue("y", ""));

		return m;
	}

	public static Map<String, Map<String, String>> readDeBugConf() {
		String msg = "readDeBugConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element debugConf = (Element) doc.selectSingleNode("//conf/debug");
		Map<String, Map<String, String>> conf = new HashMap<String, Map<String, String>>();

		if (debugConf != null) {
			Element bakEvtEle = debugConf.element("debugEvt");

			String enable = bakEvtEle.attributeValue("enable", "false");
			String maxLine = bakEvtEle.attributeValue("maxLine", "2000");

			Map<String, String> c = new HashMap<String, String>();
			conf.put(TopoConstant.DEBUG_BAKEVT_CONF, c);
			c.put(TopoConstant.ENABLE_EVT_BAK, enable);
			c.put(TopoConstant.MAXLINE_EVT_BAK, maxLine);
		}

		return conf;
	}
	

	public static final boolean enbleEvtDebugMode() {
		Map<String, String> conf = getDebugEvtConf(readDeBugConf());
		return "true".equalsIgnoreCase(conf.get(TopoConstant.ENABLE_EVT_BAK) == null ? "false" : String.valueOf(conf.get(TopoConstant.ENABLE_EVT_BAK)).trim());
	}

	public static final int getDebugEvtLine() {
		Map<String, String> conf = getDebugEvtConf(readDeBugConf());
		String conut = conf.get(TopoConstant.MAXLINE_EVT_BAK);
		Integer _count = 2000;
		if (StringUtils.isNotEmpty(conut)) {
			try {
				_count = Integer.parseInt(conut);
			} catch (Exception e) {
				log.error(" DebugEvtLine error, value:" + conut);
			}
		}
		return _count;
	}

	private static Map<String, String> getDebugEvtConf(Map<String, Map<String, String>> conf) {
		return conf.get(TopoConstant.DEBUG_BAKEVT_CONF);
	}

	public static void writeObjToFile(Collection evts, String fName) {
		OutputStreamWriter writer = null;
		String msg = "writeObjToFile " + fName + "  error";
		try {
			writer = new OutputStreamWriter(new FileOutputStream(new File(fName), true), "UTF-8");
			IOUtils.writeLines(evts, "", writer);
			log.error("debug evts ok");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static String toJson(Map map) {
		Gson g = new Gson();
		String json = g.toJson(map);
		// log.error("json :" + json);
		return json;
	}

	@SuppressWarnings("unchecked")
	public static String toJson(Collection coll) {
		Gson g = new Gson();
		String json = g.toJson(coll);
		return json;
	}

	public static void saveDevEvtConf(String devIp, String devType) {
		String msg = "saveDevEvtConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element mappingEle = (Element) doc.selectSingleNode("//conf/devType/devMapping");
		if (mappingEle == null) {
			Element conf = (Element) doc.selectSingleNode("/topo/conf");
			Validate.notNull(conf, "null element 'conf'");

			Element devTypeConf = (Element) doc.selectSingleNode("/topo/conf/devType");
			if (devTypeConf == null) {
				devTypeConf = conf.addElement("devType");
			}

			mappingEle = devTypeConf.addElement("devMapping");
		}

		Element devEle = (Element) doc.selectSingleNode("//conf/devType/devMapping/dev[@ip='" + devIp + "']");
		if (devEle != null) {
			mappingEle.remove(devEle);
		}

		Element NdevEle = mappingEle.addElement("dev");
		NdevEle.addAttribute("ip", devIp);
		NdevEle.addAttribute("type", devType);

		writeTopoConf(TOPO_CONF_FNAME, doc);

	}

	public static String readTopoName(String topoType) {
		String msg = "readTopoName error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		Element nameEle = (Element) doc.selectSingleNode("//templates/template/item[@id='" + topoType + "']");
		if (nameEle != null) {
			return nameEle.attributeValue("name", "");
		}

		return "";
	}

	private static Element readResponseConf() {
		String msg = "loadResponseConf error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}

		return (Element) doc.selectSingleNode("//responseConf");
	}

	@SuppressWarnings("unchecked")
	public static void loadRpsModeConf(Map<String, ReponseMode> conf) {
		Element confEle = readResponseConf();

		if (confEle != null) {
			Element modes = confEle.element("modes");
			if (modes != null) {
				List<Element> list = modes.elements("reponseMode");
				for (Element mode : list) {
					ReponseMode rpsMode = new ReponseMode();

					String id = mode.attributeValue("id");
					if (StringUtils.isBlank(id)) {
						continue;
					}

					String type = mode.attributeValue("type");
					String value = mode.attributeValue("value");
					rpsMode.setId(id);
					rpsMode.setType(type);
					rpsMode.setValue(value);

					conf.put(id, rpsMode);
				}
			}
		}

	}


	public static void saveOrUpdateEvtValve(String devId, String key, String val) {
		String msg = "saveOrUpdateEvtValve error";

		Validate.notEmpty(devId, msg + " devId is empty val");
		Validate.notEmpty(key, msg + "is empty val");
		Validate.notEmpty(val, msg + "is empty val");
		Document doc = readTopoDevConfDoc();

	}

	@SuppressWarnings("unchecked")
	public static void loadEvtValve(Map<String, Map<String, String>> mapping) {
		String msg = "loadEvtValve error";
		if (mapping == null) {
			throw new TopoException(msg + "EvtValveCache is null");
		}

		Document doc = null;
		try {
			doc = readTopoDevConfDoc();
			List<Element> items = doc.selectNodes("//userConfig/*");

			String evtTypes = "eventTypes";
			String evtType = "type";

			String evtLevels = "eventLevels";
			String evtLevel = "level";

			String evtValve = "valve";

			String split = "/";

			for (Element item : items) {
				String devId = item.attributeValue("itemId");

				if (StringUtils.isEmpty(devId)) {
					continue;
				}
				Map<String, String> devPool = mapping.get(devId);
				if (devPool == null) {
					devPool = new HashMap<String, String>();
				}

				Element typeEle = item.element(evtTypes);
				if (typeEle != null) {

					List<Element> types = typeEle.elements(evtType);

					for (Element type : types) {
						String valve = type.attributeValue(evtValve);
						String text = type.getTextTrim();
						if (StringUtils.isBlank(valve) || StringUtils.isBlank(text))
							continue;

						devPool.put(evtType + split + text, valve);
					}
				}

				Element levelEle = item.element(evtLevels);

				if (levelEle != null) {
					List<Element> levels = levelEle.elements(evtLevel);
					for (Element level : levels) {
						String valve = level.attributeValue(evtValve);
						String text = level.getTextTrim();

						if (StringUtils.isBlank(valve) || StringUtils.isBlank(text))
							continue;

						if (text.equals("0")) {
							text = "�ǳ���";
						} else if (text.equals("1")) {
							text = "��";
						} else if (text.equals("2")) {
							text = "��";
						} else if (text.equals("3")) {
							text = "��";
						} else if (text.equals("4")) {
							text = "�ǳ���";
						}

						devPool.put(evtLevel + split + text, valve);
					}
				}

				if (!devPool.isEmpty()) {
					mapping.put(devId, devPool);
				}

			}

		} catch (Exception e) {
			msg += e.getMessage();
			log.error(msg, e);
			throw new TopoException(msg);
		}

	}

	public static Map<String, String> readJumpWays() {
		String msg = "readJumpWays";

		// k:area v:area/domain
		Map<String, String> map = new LinkedHashMap<String, String>();

		Document doc = null;
		String message = "readJumpWays error";
		try {
			doc = readTopoConfDoc();

			List<Element> list = doc.selectNodes("//template/item");

			if (list.isEmpty()) {
				log.error(message + ",no jump way define");
				throw new TopoException(message + ",no jump way define");
			}

			// root -default
			for (Element ele : list) {
				String id = ele.attributeValue("id");
				if (StringUtils.isBlank(id))
					continue;

				String friend = ele.attributeValue("friend");
				if (StringUtils.isNotBlank(friend)) {
					map.put(id, id + "/" + friend);
				} else {
					map.put(id, id);
				}

			}
			Set<String> demo = map.keySet();

			log.error("readJumpWays : " + map + " and root is" + demo.iterator().next());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);

			throw new TopoException(msg, e);
		}

		return map;
	}






	private static void checkJumpMapping(Map<String, String> set) {
		if (set.isEmpty()) {
			throw new TopoException("no root jump conf");
		}
	}



	public static boolean isAreaViewType(String srcViewId) {
		Validate.notEmpty(srcViewId, "isAreaViewType error");
		Element item = getViewEle(srcViewId);
		if (item != null) {
			return TopoConstant.TOPO_VTYPE_AREA.equals(item.attributeValue("type"));
		} else {
			log.error("not find view element " + srcViewId);
			return false;
		}
	}

	public static boolean isDomainViewType(String srcViewId) {
		Validate.notEmpty(srcViewId, "isDomainViewType error");
		Element item = getViewEle(srcViewId);
		if (item != null) {
			return TopoConstant.TOPO_VTYPE_DOMAIN.equals(item.attributeValue("type"));
		} else {
			log.error("not find view element " + srcViewId);
			return false;
		}
	}


	private static Element getViewEle(String srcViewId) {
		Document doc = readTopoConfDoc();
		Element item = (Element) doc.selectSingleNode("//template/item[@id='" + srcViewId + "']");
		return item;
	}
	public static SID getUserSID(HttpServletRequest request) {
        SID sid = null;
        try {
            HttpSession session = request.getSession();
            if (session.getAttribute("sid") == null) {
                Cookie c = getLoginCookie(request);
                if (c != null) {
                    String sidV = c.getValue();
                    sid = new SID(sidV);
                    session.setAttribute("sid", sidV);
                }
            } else {
                sid = new SID(session.getAttribute("sid").toString());
            }

        } catch (Exception e) {

        }
        return sid;
    }
	protected static Cookie getLoginCookie(HttpServletRequest request) {
		return getCookie(request,"com.topsec.sid");
	}
	protected static Cookie getCookie(HttpServletRequest request,String key){
		Cookie thisCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(key)) {
					thisCookie = cookies[i];
					break;
				}
			}
		}

		return thisCookie;		
	}
	public static void saveImageUrl(String devId, String name, String url) {
		String msg = "saveImageUrl error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		Element confEle = (Element) doc.selectSingleNode("/topo/conf");
		if (confEle == null) {
			confEle = doc.getRootElement().addElement("conf");
		}

		Element devConfEle = (Element) doc.selectSingleNode("//conf/objInfo");
		if (devConfEle == null) {
			devConfEle = confEle.addElement("objInfo");
		}

		Element devEle = (Element) doc.selectSingleNode("//objInfo/obj[@id='" + devId + "']");
		if (devEle == null) {
			devEle = devConfEle.addElement("obj");
			devEle.addAttribute("id", devId);
		}
		if(StringUtils.isNotEmpty(name)){
			devEle.setAttributeValue("name", name);
		}
		if(StringUtils.isNotEmpty(url)){
			devEle.setAttributeValue("imgUrl", url);
		}
		writeTopoConf(TOPO_CONF_FNAME, doc);
	}
	public static List readImageUrl() {
		String msg = "saveImageUrl error";
		Document doc = null;
		try {
			doc = readTopoConfDoc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(msg + e.getMessage(), e);
			throw new TopoException(msg, e);
		}
		List imageList = new ArrayList<ItemVo>();
		List<Element> list = doc.selectNodes("//objInfo/obj");
		for (int i = 0; i < list.size(); i++) {
			ItemVo itemVo = new ItemVo();
			Element element = list.get(i);
			itemVo.setId(element.attributeValue("id"));
			itemVo.setText(element.attributeValue("name"));
			itemVo.setUrl(element.attributeValue("imgUrl"));
			imageList.add(itemVo);
		}
		return imageList;
	}
	public static boolean isNotEmpty(String str){
		if(str==null){
			return false;
		}else{
			if("".equals(str)){
				return false;
			}
		}
		return true;
	}


}
