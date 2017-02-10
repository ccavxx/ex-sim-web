package com.topsec.tsm.sim.test.dao;

import java.lang.reflect.ParameterizedType;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.topsec.tsm.sim.asset.dao.DataSourceDao;

public class DataSourceDaoTest {
	protected static ApplicationContext context ;	
	@BeforeClass
	public static void initEnv(){
		context = new ClassPathXmlApplicationContext("classpath:applicationContextTest.xml") ;		
	}
	
	@Test
	public void testDataSourceDao() {
		DataSourceDao dataSourceDao = (DataSourceDao) context.getBean("dataSourceDao") ;
		System.out.println(dataSourceDao != null);
	}

}
