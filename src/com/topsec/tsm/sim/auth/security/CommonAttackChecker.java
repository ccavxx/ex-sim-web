package com.topsec.tsm.sim.auth.security;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;


public class CommonAttackChecker implements AttackChecker{

	@Override
	public Attack findAttack(Object object,Set<String> uncheckProperties,String[] allows) {
		if(object == null){
			return null ;
		}
		if(object instanceof String){
			return SecurityUtil.findAttack(new String[]{(String)object}, allows) ;
		}else if(object instanceof Map){
			return SecurityUtil.findAttack((Map<?,?>)object, allows) ;
		}else if(object.getClass().isArray()){
			return SecurityUtil.findAttack((Object[])object,allows) ;
		}else{
			Class<?> cls = object.getClass() ;
			Field[] fields = cls.getDeclaredFields() ;
			for(Field f:fields){
				if(uncheckProperties.contains(f.getName())){
					continue ;
				}
				if(f.getType() == String.class){
					try {
						Attack attack = SecurityUtil.findAttack((String)PropertyUtils.getProperty(object, f.getName())) ;
						if(attack != null){
							return attack ;
						}
					} catch (Exception e) {
						continue ;
					}
				}
			}
		}
		return null ;
	}

}
