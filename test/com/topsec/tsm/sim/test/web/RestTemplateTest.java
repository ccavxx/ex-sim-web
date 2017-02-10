package com.topsec.tsm.sim.test.web;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class RestTemplateTest {

	RestTemplate restTemplate ;  
	
	@BeforeClass
	public void start(){
		restTemplate = new RestTemplate() ;
	}

	@Test
	public void testJson(){
		String str = restTemplate.getForObject("http://localhost/sim/test/jsonTest",String.class) ;
		System.out.println(str);
	}
	@Test
	public void testString(){
		String str = restTemplate.getForObject("http://localhost/sim/test/helloWorld",String.class) ;
		System.out.println(str);
	}

}
