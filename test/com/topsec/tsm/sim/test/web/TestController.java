package com.topsec.tsm.sim.test.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.test.service.TestService;
import com.topsec.tsm.sim.test.service.TestServiceImpl;

@Controller
@RequestMapping
public class TestController {

	private TestService service;
	@RequestMapping("helloWorld")
	public String helloWorld() throws Exception {
		System.out.println("helloWorld");
		return "index" ;
	}
	@RequestMapping("hello")
	public String hello(HttpServletRequest request) throws Exception {
		System.out.println("hello");
		System.out.println(request.getParameter("id"));
		return null ;
	}
	
	@RequestMapping("addTestBean")
	public String addTestBean(@RequestParam(value="id",required=false)Integer id,@RequestParam(value="name",required=false)String name){
		service.addBean(new TestBean(id, name)) ;
		return "success" ;
	}
	
	@RequestMapping("addTestUseBean")
	public String addTestUseBean(TestBean bean){
		service.addBean(bean) ;
		return "ok" ;
	}
	
	@RequestMapping("addTest")
	public void addTest(HttpServletResponse response,@RequestParam(value="id")Integer id,@RequestParam(value="name")String name) throws IOException{
		service.add(id,name) ;
		response.getWriter().write("ok") ;
	}
	
	@RequestMapping("/jsonTest")
	@ResponseBody
	public String jsonTest(){
		return "{id:1,name:\"flm\"}" ;
	}
	@Autowired
	public void setService(TestService service) {
		this.service = service;
	}
}
