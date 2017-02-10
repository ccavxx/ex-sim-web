package com.topsec.tsm.sim.test.util;

import static org.junit.Assert.*;
import org.junit.Test;

import com.topsec.tal.base.util.EnhanceProperties;
import com.topsec.tal.base.util.PropertyManager;

public class PropertManagerTest{

	@Test
	public void testGetFile() {
		EnhanceProperties ep = PropertyManager.getFile("a.properties") ;
		assertNotNull(ep) ;
	}
	
	@Test
	public void testGetResource() {
		EnhanceProperties ep = PropertyManager.getResource("resource/a.properties") ;
		assertNotNull(ep) ;
	}
	@Test
	public void testGetProperties(){
		EnhanceProperties ep = PropertyManager.getResource("resource/a.properties") ;
		int a = ep.getInt("a") ;
		long b = ep.getLong("b") ; 
		float c = ep.getFloat("c") ;
		double d = ep.getDouble("d") ;
		String[] arr = ep.getArray("e") ;
		
		int x = ep.getInt("x") ;
		long y = ep.getLong("y") ;
		float z = ep.getFloat("z") ;
		double u = ep.getDouble("u") ;
		String[] arr1 = ep.getArray("arr") ;
		assertEquals(1, a) ;
		assertEquals(2, b) ;
		assertTrue(c==3.0);
		assertTrue(d==4.0) ;
		assertTrue(arr.length==2) ;
		assertTrue(x==-1) ;
		assertTrue(y==-1) ;
		assertTrue(z==-1) ;
		assertTrue(u==-1) ;
		assertTrue(arr1.length==0) ;
	}
	
	@Test
	public void setProperties(){
		EnhanceProperties ep = PropertyManager.getResource("com/topsec/tsm/sim/test/web/a.properties") ;
		ep.setProperty("a", 11) ;
		ep.setProperty("b", 22L) ;
		ep.setProperty("c", 33F) ;
		ep.setProperty("d", 44D) ;
		ep.setProperty("x", "中文") ;
		ep.store() ;
	}
}
