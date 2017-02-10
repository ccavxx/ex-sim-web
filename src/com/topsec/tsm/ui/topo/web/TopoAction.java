package com.topsec.tsm.ui.topo.web;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping("/topo/*")
public class TopoAction{
	private static Logger log = Logger.getLogger(TopoAction.class);
	@RequestMapping("go2Topo")
	@ResponseBody
	public List go2Topo(@RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit) throws Exception {
		return null;
	}
	

}
