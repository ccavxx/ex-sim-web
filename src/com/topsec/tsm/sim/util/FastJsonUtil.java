package com.topsec.tsm.sim.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.formatter.FormatterFactory;
import com.topsec.tsm.common.formatter.PropertyFormatter;

public class FastJsonUtil {
	
	/**空json对象*/
	public static final JSONObject EMPTY_JSON = new JSONObject(0) ;
	/**只包含一条空记录的json数组*/
	public static final JSONArray ONE_EMPTY_RECORD = new JSONArray() ; 
	static{
		ONE_EMPTY_RECORD.add(EMPTY_JSON) ;
	}
	/**
	 * 将bean对象中的属性转换为json类型的对象<br>
	 * fieldExpressions数组中每一个表示一个属性表达式<br>
	 * <strong>每一项表达中间使用"="分割</strong><br>
	 * <strong>左边表示要取值的属性</strong><br>
	 * <strong>右边表示转换成Json后的key值</strong><br>
	 * <strong>如果两值相等可以省略"="</strong><br>
	 * <strong>如果属性以$开始，表示此属性的值需要格式化</strong>，格式化接口{@link PropertyFormatter}<br>
	 * 示例:将user对象中的name属性转换为json中的text键<br>
	 * User user = new User();<br>
	 * toJSON(user,"name=text")
	 * @param bean 
	 * @param callback 在转换成JSON对象后回调接口，使用此接口可以对转换结果进行进一步的处理 
	 * @param fieldExpressions 表达式数组
	 * @return
	 */
	public static JSONObject toJSON(Object bean,JSONConverterCallBack callback,String... fieldExpressions){
		return mergeToJSON(new JSONObject(),bean,callback, fieldExpressions) ;
	}
	public static JSONObject toJSON(Object bean,String... fieldExpressions){
		return mergeToJSON(new JSONObject(),bean,null,fieldExpressions) ;
	}
	/**
	 * 将集合对象转换为json array
	 * @param collection
	 * @param fieldExpressions
	 * @return
	 */
	public static JSONArray toJSONArray(Collection collection,String... fieldExpressions){
		return toJSONArray(collection, null, fieldExpressions) ;
	}
	/**
	 * 将集合对象转换为json array
	 * @param collection
	 * @param callback 在转换成JSON对象后回调接口 
	 * @param fieldExpressions
	 * @return
	 */
	public static JSONArray toJSONArray(Collection collection,JSONConverterCallBack callback,String... fieldExpressions){
		JSONArray array = new JSONArray();
		if(ObjectUtils.isEmpty(collection)){
			return array ;
		}
		for(Object obj:collection){
			array.add(toJSON(obj,callback,fieldExpressions)) ;
		}
		return array ;
	}
	/**
	 * 为json数组中的每个对象添加一个键和值
	 * @param arr json数组
	 * @param key 键
	 * @param value 值
	 * @return
	 */
	public static JSONArray put(JSONArray arr,String key,Object value){
		for(int i=0;i<arr.size();i++){
			Object o = arr.get(i) ;
			if(o instanceof JSONObject){
				((JSONObject) o).put(key, value) ;
			}
		}
		return arr ;
	}
	public static JSONObject mergeToJSON(JSONObject json,Object bean,String... fieldExpressions){
		return mergeToJSON(json, bean, null, fieldExpressions) ;
	}
	/**
	 * 将bean中的属性合并到现有的json对象中去，并返回合并后的Json对象
	 * @param json json对象
	 * @param bean bean对象
	 * @param callback 在转换成JSON对象后回调接口
	 * @param fieldExpressions 属性表达式数组
	 * @see #toJSON(Object, String...)
	 * @return
	 */
	public static JSONObject mergeToJSON(JSONObject json,Object bean,JSONConverterCallBack callback,String... fieldExpressions){
		Map<String,String> fields = new HashMap<String,String>() ;
		for(String fieldExp:fieldExpressions){
			String[] fieldArr = StringUtil.split(fieldExp, "=") ;
			int length = fieldArr.length ;
			switch(length){
				case 0 : break ;
				case 1 : fields.put(fieldArr[0], fieldArr[0].startsWith("$") ? 
												 fieldArr[0].substring(fieldArr[0].indexOf(':')+1) :
												 fieldArr[0]) ;break ;
				case 2 : fields.put(fieldArr[0], fieldArr[1]) ;
				default : break ;
			}
		}
		for(Map.Entry<String, String> entry:fields.entrySet()){
			try {
				String key = entry.getKey() ;
				if(key.startsWith("$")){//以$开头的表示使用格式化函数
					int splitIndex = key.indexOf(':') ;
					String formatterName = key.substring(1,splitIndex) ;
					String propertyName = key.substring(splitIndex+1) ;
					PropertyFormatter formatter = FormatterFactory.getInstance().getFormatter(formatterName) ;
					Object value = PropertyUtils.getNestedProperty(bean, propertyName) ;
					json.put(entry.getValue(),value == null ? null : formatter.format(value)) ;
				}else{
					json.put(entry.getValue(),PropertyUtils.getNestedProperty(bean, key) ) ;
				}
			}catch (NestedNullException e) {
				json.put(entry.getValue(), null) ;//抛出此异常说明嵌套的属性值为null
			} catch (Exception e) {
				throw new RuntimeException("Get bean field value fail!!!", e) ;
			}
		}
		if (callback != null) {
			callback.call(json, bean) ;
		}
		return json ;
	}
	/**
	 * 将一组对象转换为一个Json数组
	 * @param obj
	 * @return
	 */
	public static JSONArray wrapper(Object... objs){
		JSONArray ja = new JSONArray(objs.length) ;
		for(Object obj:objs){
			ja.add(obj) ;
		}
		return ja ;
	}
	
	/**
	 * 一个使用","分割的属性列表
	 * @param fieldExpression
	 * @param bean
	 * @return
	 */
	public static JSONObject fieldToJSON(String fieldExpression,Object bean){
		return null ;
	}
	

}
