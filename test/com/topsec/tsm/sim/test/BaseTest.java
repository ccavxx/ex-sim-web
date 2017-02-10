package com.topsec.tsm.sim.test;

import java.lang.reflect.ParameterizedType;

import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BaseTest<T> {
	
	protected static ApplicationContext context ;
	protected T testInstance ;
	protected Class<T> testClass ;
	
	@BeforeClass
	public static void initEnv(){
		context = new ClassPathXmlApplicationContext("classpath:applicationContextTest.xml") ;		
	}
	
	@SuppressWarnings("unchecked")
	public BaseTest() {
		testClass = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0] ;
		testInstance = context.getBean(testClass) ;
	}

}
