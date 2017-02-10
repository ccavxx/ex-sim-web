package com.topsec.tsm.sim.asset;

import java.util.Date;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.util.CommonUtils;

public class AttributeFormatterFactory {

	public static AttributeFormatter get(String type){
		if (type == null) {
			return null ;
		}
		return new GeneralAttributeFormatter(type) ;
	}
}
/**
 * 通用属性格式化类<br>
 * 
 * @author hp
 *
 */
class GeneralAttributeFormatter implements AttributeFormatter{
	private String type ;

	public GeneralAttributeFormatter(String type) {
		super();
		this.type = type;
	}

	@Override
	public Object format(Object value) {
		if(type.equalsIgnoreCase("date") && value instanceof Date){
			return StringUtil.dateToString((Date) value,"yyyy-MM-dd HH:mm:ss");
		}else if(type.equalsIgnoreCase("bytes") && value instanceof Number){//字节格式化
			return CommonUtils.formatBytes(((Number) value).longValue(),2) ;
		}else if(type.equalsIgnoreCase("kb") && value instanceof Number){//K字节格式化
			return CommonUtils.formatBytes(((Number) value).longValue()*1024,2) ;
		}else if(type.equalsIgnoreCase("mb") && value instanceof Number){//M字节格式化
			return CommonUtils.formatBytes(((Number) value).longValue()*1024*1024,2) ;
		}
		return StringUtil.toString(value, "") ;
	}
}