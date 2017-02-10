package com.topsec.tsm.sim.test.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.topsec.tsm.sim.test.BaseTest;
import com.topsec.tsm.sim.test.web.TestBean;

public class ServiceTester extends BaseTest<TestService> {

	@Test
	public void testGet(){
		testInstance.add(1, "flm12345") ;
		String name = testInstance.get(1) ;
		assertEquals("flm12345", name) ;
	}
	
	@Test
	public void testAddBean(){
		TestBean bean = new TestBean(2,"flm") ;
		testInstance.addBean(bean) ;
	}
	
	@Test
	public void testGetEventCategoires(){
		List eventCategories = testInstance.getEventCategories() ;
		assertTrue(eventCategories!=null&&eventCategories.size()>0);
	}
}
