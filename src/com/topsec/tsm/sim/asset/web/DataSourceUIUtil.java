package com.topsec.tsm.sim.asset.web;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.PropertyEntryUtil;
import com.topsec.tsm.sim.common.exception.TimeExpressionException;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.DataSourceException;
import com.topsec.tsm.sim.response.persistence.TimeExpression;
import com.topsec.tsm.sim.util.CommonUtils;
import com.topsec.tsm.util.actiontemplate.ActionTemplate;
import com.topsec.tsm.util.actiontemplate.AddablePropertyEntry;
import com.topsec.tsm.util.actiontemplate.CollectionPropertyEntry;
import com.topsec.tsm.util.actiontemplate.PropertyEntry;
import com.topsec.tsm.util.actiontemplate.accessor.ActionTemplateUiAccessor;
import com.topsec.tsm.util.encrypt.DesSecurity;

public class DataSourceUIUtil {
	
	public static ActionTemplate buildActionTemplate(String collectType,String securityObjectType,String ownerGroup,Parameter parameter){
		try {
			ActionTemplate template = DataSourceUtil.getDataSourceTemplate(securityObjectType, collectType,ownerGroup) ;
			if (template != null) {
				template = template.clone() ;
				ActionTemplateUiAccessor accessor = new ActionTemplateUiAccessor() ;
				accessor.setActionTemplate(template) ;
				//encryptProperty(parameter) ;
				Map<String,String[]> allParameter = parameter.getParameterMap() ;
				for(Map.Entry<String, String[]> entry:allParameter.entrySet()){
					String paramName = entry.getKey() ;
					String[] paramArray = StringUtil.split(paramName,PropertyEntryUtil.CONFIG_PARAM_SPLIT_CHAR) ;
					if(paramArray.length<2||ObjectUtils.isEmpty(entry.getValue())){
						continue ;
					}
					String[] values = entry.getValue() ;
					String propertyType = paramArray[0] ;
					if(propertyType.equals(PropertyEntryUtil.PROPERTY_CONFIG)){//参数名格式:property_PropertyName
						setPropertyValue(parameter, paramName, paramArray[1], values[0], accessor.getProperty(paramArray[1])) ;
					}else if(propertyType.equals(PropertyEntryUtil.COLLECTION_CONFIG)){//参数名格式:collection_collectionName_PropertyName
						CollectionPropertyEntry collection = (CollectionPropertyEntry) accessor.getProperty(paramArray[1]) ;
						int paramIndex = 2 ;
						//处理参数多级嵌套格式的数据，例如:collection_collectionName_collectionName_propertyName
						for(;paramIndex<paramArray.length-1;paramIndex++){
							PropertyEntry pt = collection.getProperty(paramArray[paramIndex]) ;
							if(pt instanceof CollectionPropertyEntry){
								collection = (CollectionPropertyEntry) pt ;
							}else{//多级嵌套处理失败
								throw new DataSourceException("参数:"+pt.getAlias()+"无法识别！") ;
							}
						}
						setPropertyValue(parameter, paramName, paramArray[paramIndex], values[0], collection.getProperty(paramArray[paramIndex])) ;
					}else if(propertyType.equals(PropertyEntryUtil.ADDABLE_CONFIG)){//参数名格式:addable_addableName_collectionName_paramName
						String addableName = paramArray[1] ;
						String collectionName = paramArray[2] ;
						String propertyName = paramArray[3] ;
						AddablePropertyEntry addableProperty = (AddablePropertyEntry) accessor.getProperty(addableName) ;
						List<PropertyEntry> properties = addableProperty.getProperties(collectionName) ;
						if(ObjectUtils.isEmpty(properties)){
							PropertyEntry schema = PropertyEntryUtil.getSchemaByName(collectionName, addableProperty) ;
							properties = PropertyEntryUtil.clone(schema,values.length) ;
							addableProperty.getProperties().addAll(properties) ;
						}
						int index = 0 ;
						for(PropertyEntry pt:properties){
							if(pt instanceof CollectionPropertyEntry){
								((CollectionPropertyEntry) pt).getProperty(propertyName).setValue(values[index++]) ;
							}
						}
					}
				}
			}
			return template ;
		} catch (CloneNotSupportedException e) {
		}
		return null ;
	}
	/**
	 * 
	 * @param parameter 所有请求参数集合
	 * @param paramName 当前赋值字段参数名
	 * @param propertyName 当前赋值字段属性名
	 * @param value 值
	 * @param property 属性
	 */
	private static void setPropertyValue(Parameter parameter,String paramName,String propertyName,String value,PropertyEntry property){
		if("Password".equalsIgnoreCase(property.getBizType())){
			value = CommonUtils.decrypt(value,"") ;
		}
		//如果是password字段或者是encrypt+propertyName字段存在说明页面有选项可供选择是否加密,因此需要根据页面选择来确定是否加密
		//例如encryptABC存在表示ABC字段需要加密
		String encryptPropertyName = propertyName.equals("password") ? "encrypt" : "encrypt"+propertyName;
		String encryptParamName = paramName.replace(propertyName, encryptPropertyName);
		boolean needEncrypt = parameter.getBoolean(encryptParamName) ; 
		if(needEncrypt){
			value = StringUtil.encrypt(value) ;
		}
		property.setValue(value) ;
	}
	/**
	 * 加密property参数信息
	 * @param parameter
	 */
	private static void encryptProperty(Parameter parameter){
		for(String paramName:parameter.getNames()){
			if(!paramName.startsWith(PropertyEntryUtil.COLLECTION_CONFIG)){
				continue ;
			}
			//按.分割属性名最后一个字符串为实际的字段名
			String[] paramArr = StringUtil.split(paramName,PropertyEntryUtil.CONFIG_PARAM_SPLIT_CHAR) ;
			if(paramArr.length < 1){
				continue ;
			}
			String propertyName = paramArr[paramArr.length-1];
			//如果是选择加密
			if(propertyName.startsWith("encrypt") && parameter.getBoolean(paramName)){
				//被加密字段的名称
				String encryptPropertyName = propertyName.equals("encrypt") ? "password" : propertyName.substring(8);
				paramArr[paramArr.length-1] = encryptPropertyName ;
				String encryptParamName = StringUtil.join(paramArr,PropertyEntryUtil.CONFIG_PARAM_SPLIT_CHAR) ;
				String encryptString = StringUtil.encrypt(parameter.getValue(encryptParamName)) ;
				parameter.put(encryptParamName, encryptString) ;
			}
		}
	}	
	/**
	 * 为日志源指定定时任务
	 * @param params
	 * @param dataSource
	 * @throws TimeExpressionException 
	 */
	public static void buildJob(Parameter params,SimDatasource dataSource) throws TimeExpressionException{
		TimeExpression timeExpression = CommonUtils.createTimeExpression(params) ;
		dataSource.setTimer(timeExpression.getExpression());
		dataSource.setTimerType(timeExpression.getType());
		dataSource.setReference("isJob");
	}

}
