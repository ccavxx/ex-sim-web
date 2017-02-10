package com.topsec.tsm.sim.asset;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.util.actiontemplate.AddablePropertyEntry;
import com.topsec.tsm.util.actiontemplate.CollectionPropertyEntry;
import com.topsec.tsm.util.actiontemplate.PropertyEntry;

public class PropertyEntryUtil {
	public static final String ADDABLE_CONFIG = "addable";
	public static final String COLLECTION_CONFIG = "collection";
	public static final String PROPERTY_CONFIG = "property" ;
	private static JSONArray uiDisplayControl ;
	//前台往后台传递配置参数时使用分割符，此分割符必须与前台js中使用的一致，而且不能是正则表达式中的具有特殊含义的字符如:.?*
	public static final String CONFIG_PARAM_SPLIT_CHAR = "_" ;
	/**
	 * 根据收集方式、property类型、和property名称，返回property可用的值<br>
	 * 如果property是select类型或者boolean类型会返回一个list列表，包含所有可用的值<br>
	 * 如果property是String类型或者NonNegativeInteger或者ip类型返回""字符串<br>
	 * @param collectType 日志收集方式
	 * @param bizType bizType(Select,Boolean,NonNegativeInteger,IP)
	 * @param property 改名名称
	 * @return
	 */
	public static Object getOptions(String collectType,String bizType,String property){
		List<Map<String,Object>> options = new ArrayList<Map<String,Object>>() ;
		if(collectType == null||bizType==null||property==null){
			return options ;
		}
		property = property.toLowerCase() ;
		collectType = collectType.toLowerCase() ;
		bizType = bizType.toLowerCase() ;
		if(property.equals("properties.encoding")||property.equals("charset") || property.equals("encoding")){
			putOptions(options, "默认", Charset.defaultCharset().name()) ;
			putOptions(options, "GB2312", "GB2312") ;
			putOptions(options, "UTF-8", "UTF-8") ;
			putOptions(options, "UTF-16", "UTF-16") ;
			putOptions(options, "GB18030", "GB18030") ;
			putOptions(options, "ISO-8859-1", "ISO-8859-1") ;
			return options ;
		}
		//文件名编码
		if(property.equals("fnencoding")){
			putOptions(options, "无","") ;
			putOptions(options, "GBK", "GBK") ;
			putOptions(options, "UTF-8", "UTF-8") ;
			return options ;
		}
		if(bizType.equals("boolean")){
			putOptions(options, "是", "true") ;
			putOptions(options, "否", "false") ;
			return options ;
		}
		if(collectType.equals("jdbc")&&property.equals("type")){
			putOptions(options, "ORACLE", "ORACLE") ;
			putOptions(options, "DB2", "DB2") ;
			putOptions(options, "MYSQL", "MYSQL") ;
			putOptions(options, "SQLSERVER", "SQLSERVER") ;
			putOptions(options, "GBASE", "GBASE") ;
			putOptions(options, "SQLLITE", "SQLLITE") ;
			putOptions(options, "ACCESS", "ACCESS") ;
			return options ;
		}
		if(property.equals("termtype")){
			putOptions(options, "vt52", "vt52") ;
			putOptions(options, "vt100", "vt100") ;
			putOptions(options, "vtnt", "vtnt") ;
			putOptions(options, "ansi", "ansi") ;
			return options ;
		}
		if(collectType.equals("snmpget")){
			if(property.equals("snmpversion")){
				putOptions(options, "V1", "V1") ;
				putOptions(options, "V2C", "V2C") ;
				putOptions(options, "V3", "V3") ;
				return options ;
			}
			if(property.equals("version")){
				putOptions(options, "V1", "0") ;
				putOptions(options, "V2C", "1") ;
				putOptions(options, "V3", "3") ;
				return options ;
			}
			if(property.equals("securitylevel")){
				putOptions(options, "不认证不加密", "1") ;
				putOptions(options, "认证不加密", "2") ;
				putOptions(options, "认证加密", "3") ;
				return options ;
			}
			if(property.equals("authenticationmethod")){
				putOptions(options, "MD5", "1") ;
				putOptions(options, "SHA", "2") ;
				return options ;
			}
			if(property.equals("privacymethod")){
				putOptions(options, "请选择", "") ;
				putOptions(options, "AES128", "1") ;
				putOptions(options, "AES192", "2") ;
				putOptions(options, "AES256", "3") ;
				putOptions(options, "DES", "4") ;
				putOptions(options, "3DES", "5") ;
				return options ;
			}
		}
		return null ;
	}
	
	private static void putOptions(List<Map<String,Object>> optionContainer,String name,Object value){
		Map<String,Object> option = new HashMap<String,Object>(2) ;
		option.put("name", name) ;
		option.put("value", value) ;
		optionContainer.add(option) ;
	}
	/**
	 * 将指定的entry对象克隆n次
	 * @param entry 要克隆的对象
	 * @param count 克隆次数
	 * @return
	 */
	public static List<PropertyEntry> clone(PropertyEntry entry,int count){
		List<PropertyEntry> list = new ArrayList<PropertyEntry>(count) ;
		try {
			for (int i = 0; i < count ; i++) {
				list.add((PropertyEntry)entry.clone()) ;
			}
			return list ;
		} catch (CloneNotSupportedException e) {
			return null ;
		}
	}
	/**
	 * 根据schema名称查询schema对象
	 * @param name schema
	 * @param addableEntry 被检索对象
	 * @return
	 */
	public static PropertyEntry getSchemaByName(String name,AddablePropertyEntry addableEntry){
		List<PropertyEntry> schemas = addableEntry.getSchemas() ;
		for(PropertyEntry pe:schemas){
			if(pe.getName().equals(name)){
				return pe ;
			}
		}
		return null ;
	}
	
	public static JSONArray toJSON(String collectType,List<PropertyEntry> properties,AssetObject asset){
		JSONArray result = new JSONArray() ;
		Set<String> encryptedProperties = new HashSet<String>() ;//被加密的字段列表
		for(PropertyEntry entry:properties){
			if(entry.isUI()){
				JSONObject entryJson = new JSONObject();
				result.add(entryJson) ;
				String type = entry instanceof AddablePropertyEntry ? ADDABLE_CONFIG : 
					          entry instanceof CollectionPropertyEntry ? COLLECTION_CONFIG : PROPERTY_CONFIG ;
				appendToJSON(collectType,type,entry, entryJson,asset,encryptedProperties) ;
			}
		}
		return result ;
	}
	private static void appendToJSON(String collectType,String type,PropertyEntry pe,JSONObject json,AssetObject asset,Set<String> encryptedProperties){
		String name = pe.getName() ;
		json.put("name", name) ;
		json.put("realName", pe.getName()) ;
		json.put("alias", pe.getAlias()) ;
		json.put("type", type) ;
		if(pe instanceof AddablePropertyEntry){
			JSONArray schemasJson = new JSONArray();
			json.put("bizType", "addable") ;
			json.put("schemas", schemasJson) ;
			for(PropertyEntry cpe:((AddablePropertyEntry)pe).getSchemas()){
				if(cpe.isUI()){
					JSONObject scmJson = new JSONObject();
					schemasJson.add(scmJson) ;
					appendToJSON(collectType,type,cpe,scmJson,asset,encryptedProperties) ;
				}
			}
		}
		if(pe instanceof CollectionPropertyEntry){
			JSONArray propertiesJson = new JSONArray();
			json.put("properties", propertiesJson) ;
			json.put("bizType", "collection") ;
			encryptedProperties.clear() ;
			for(PropertyEntry cpe:((CollectionPropertyEntry)pe).getProperties()){
				if(cpe.isUI()){
					JSONObject ptJson = new JSONObject();
					propertiesJson.add(ptJson) ;
					appendToJSON(collectType,type,cpe,ptJson,asset,encryptedProperties) ;
				}
			}
			//解密加密的密码，同时加密Password类型的数据（传输使用）
			for(Object obj:propertiesJson){
				JSONObject pt = (JSONObject) obj ;
				if(encryptedProperties.size() > 0){
					for(String encryptProperty:encryptedProperties){
						if(pt.getString("name").equals(encryptProperty)){
							pt.put("value", StringUtil.decrypt(pt.getString("value"))) ;
							break ;
						}
					}
				}
				if("Password".equals(pt.getString("bizType"))){
					pt.put("value", CommonUtils.encrypt(SID.currentUser(), pt.getString("value"))) ;
				}
			}
		}else{
			String value = getPropertyValue(pe, asset,collectType) ;
			//收集被加密的字段
			if(name.startsWith("encrypt") && StringUtil.booleanVal(value)){
				encryptedProperties.add(name.equals("encrypt") ? "password" : name.substring(8)) ;
			}
			json.put("bizType", pe.getBizType()) ;
			json.put("value", value) ;
			json.put("description", pe.getDescription()) ;
			json.put("required", !pe.isOption()) ;
			json.put("options", PropertyEntryUtil.getOptions(collectType, pe.getBizType(), pe.getName())) ;
		}
	}
	private static String getPropertyValue(PropertyEntry pe,AssetObject asset,String collectorType){
		if(asset == null || StringUtil.isNotBlank(pe.getValue()) || 
		   collectorType.equalsIgnoreCase("FTP") || collectorType.equalsIgnoreCase("SNMPGet") || 
		   collectorType.equalsIgnoreCase("JDBC") || collectorType.equalsIgnoreCase("RMI") || 
		   collectorType.equalsIgnoreCase("JMX")){
			return pe.getValue() ;
		}
		String name = pe.getName() ;
		if("username".equalsIgnoreCase(name)){
			return asset.getAccountName();
		}else if("password".equalsIgnoreCase(name) && StringUtil.isBlank(pe.getValue())){
			return StringUtil.decrypt(asset.getAccountPassword()) ;
		}else{
			return pe.getValue() ;
		}
	}
	public static JSONArray getDisplayControl(String collectMethod){
		if(uiDisplayControl == null){
			synchronized (PropertyEntryUtil.class) {
				if(uiDisplayControl == null){
					try {
						String str = IOUtils.toString(PropertyEntryUtil.class.getClassLoader().getResourceAsStream("resource/asset/display_control.json")) ;
						uiDisplayControl = JSON.parseArray(str) ;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		for(int i=0;i<uiDisplayControl.size();i++){
			JSONObject jo = (JSONObject) uiDisplayControl.get(i) ;
			if(jo.get("collectMethod").equals(collectMethod)){
				return (JSONArray) jo.get("displayControl") ;
			}
		}
		return new JSONArray(0) ;
	}
}
