package com.topsec.tsm.sim.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.common.bean.FilterField;
import com.topsec.tsm.ui.util.StringUtils;
/**
 * 监视指标工具
 * @author hp
 *
 */
public class MonitorParamsUtil {
	private static MonitorParamsUtil instance = null;
	private Map<String, List<FilterField>> FieldCache = new ConcurrentHashMap<String, List<FilterField>>();

	private MonitorParamsUtil() {
	}

	public synchronized static MonitorParamsUtil getInstance() {
		if (instance == null) {
			instance = new MonitorParamsUtil();
		}
		return instance;
	}
	/**
	 * 根据监视对象类型，获取所有此对象可监视的指标参数
	 * @param monitorType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<FilterField> getParamsByType(String monitorType) {
    	List<FilterField> fields =  FieldCache.get(monitorType);
    	if(fields == null){
    		SAXReader sAXReader=new SAXReader();
    		try {
    			Document doc = sAXReader.read(SystemDefinition.DEFAULT_CONF_DIR+"MonitorAlarmConfig.xml");
	    		Element element=doc.getRootElement();
	    		fields = new ArrayList<FilterField>();
	    		List<Element> nodes=element.elements("DeviceMonitorType");
	    		boolean found = false ;
	    		for(Element node :nodes){
	    			String type = node.attributeValue("id");
	    			if(type != null && type.equals(monitorType)){
	    				parseFieldsTo(node.elements("Field"),fields) ;
	    				FieldCache.put(monitorType, fields);
	    				found = true ;
	    			}
	    			for(Element subNode:(List<Element>)node.elements("SubDeviceMonitorType")){
	    				String id = subNode.attributeValue("id") ;
	    				if(id != null && id.equals(monitorType)){
	    					parseFieldsTo(node.elements("Field"), fields) ;
	    					FieldCache.put(monitorType, fields);
	    					found = true ;
	    					break ;
	    				}
	    			}
	    			if(found){
	    				break ;
	    			}
	    		}
    		} catch (DocumentException e) {
    			e.printStackTrace();
    		}
    		
    	}
    	return fields;
    }
	private void parseFieldsTo(List<Element> fiellist,List<FilterField> fields){
		for(Element field:fiellist){
			FilterField filterField = new FilterField(field.attributeValue("name"),field.attributeValue("alias"),field.attributeValue("type"));
			String tmpvalue = field.attributeValue("value");
			if(tmpvalue != null &&  tmpvalue.trim().length() != 0){
				String[] values = StringUtils.split(tmpvalue, ",");
				filterField.setValues(values);
			}
			fields.add(filterField);
		}
	}
	public static void main(String[] args) {
		MonitorParamsUtil.getInstance().getParamsByType("MonitorOS/Microsoft/WindowsStatus");
	}
}
