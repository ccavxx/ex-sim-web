package com.topsec.tsm.sim.test.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

import com.topsec.tsm.sim.test.BaseTest;

public class ControllerTester extends BaseTest<TestController>{

	MockHttpServletRequest request ;
	MockHttpServletResponse response ;
	AnnotationMethodHandlerAdapter adapter ;
	
	public ControllerTester(){
		super() ;
		adapter = context.getBean(AnnotationMethodHandlerAdapter.class) ;
	}
	
	@Before
	public void before(){
		request = new MockHttpServletRequest() ;
		request.setCharacterEncoding("utf8") ;
		response = new MockHttpServletResponse() ;
	}
	
	@Test
	public void testHelloWorld(){
		try {
			request.setRequestURI("/test/helloWorld") ;
			ModelAndView view = adapter.handle(request, response, testInstance) ;
			System.out.println(view.getViewName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddTestBean(){
		try {
			request.setRequestURI("/test/addTestBean") ;
			request.addParameter("id", "1") ;
			request.addParameter("name", "flm") ;
			ModelAndView view = adapter.handle(request, response, testInstance) ;
			assertEquals("success", view.getViewName()) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testAddTestUseBean(){
		try {
			request.setRequestURI("/test/addTestUseBean") ;
			request.addParameter("id", "123456") ;
			request.addParameter("name", "flm213") ;
			ModelAndView view = adapter.handle(request, response, testInstance) ;
			assertEquals("ok", view.getViewName()) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddTest(){
		try {
			request.setRequestURI("/test/addTest") ;
			request.addParameter("id", "123") ;
			request.addParameter("name", "flm") ;
			adapter.handle(request, response, testInstance) ;
			assertThat(response.getContentAsString(),is("ok"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testJsonTest(){
		try {
			request.setRequestURI("/test/jsonTest") ;
			adapter.handle(request, response, testInstance) ;
			assertThat(response.getContentAsString(),containsString("flm")) ;
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
