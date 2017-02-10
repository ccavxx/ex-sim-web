package com.topsec.tsm.sim.sysconfig.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.IpLocation;
import com.topsec.tsm.base.type.IpLocationUtil;
import com.topsec.tsm.base.type.Ipv4Address;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.sysconfig.service.ResourceService;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;
import com.topsec.tsm.sim.util.NodeUtil;

@Controller
@RequestMapping("resource")
public class ResourceController {
	
	@Autowired
	public ResourceService resourceService ;
	
	/**
	 * 用户自定义ip地址库
	 * @return
	 */
	@RequestMapping("/ipLocationList")
	@ResponseBody
	public Object ipLocationList(@RequestParam(value="page",defaultValue="1")Integer pageIndex,
								 @RequestParam(value="rows",defaultValue="20")Integer pageSize,
								 HttpServletRequest request){
		Map<String,Object> searchConditions = new HashMap<String,Object>() ;
		searchConditions.put("name", request.getParameter("name")) ;
		String ip = request.getParameter("ip") ;
		if(Ipv4Address.validIPv4(ip)){
			searchConditions.put("ip", ip) ;
		}
		PageBean<IpLocation> ipLocations = resourceService.search(pageIndex, pageSize, searchConditions) ;
		JSONArray rows = FastJsonUtil.toJSONArray(ipLocations.getData(),new JSONConverterCallBack<IpLocation>() {
			@Override
			public void call(JSONObject result, IpLocation obj) {
				result.put("startIp", new IpAddress(obj.small).toString()) ;
				result.put("endIp", new IpAddress(obj.big).toString()) ;
				result.put("name", obj.netSegment) ;
				result.put("nameHtmlUnescape", HtmlUtils.htmlEscape(obj.netSegment)) ;
			}
		},"locationId=id") ;
		JSONObject result = new JSONObject() ;
		result.put("total", ipLocations.getTotal()) ;
		result.put("rows", rows) ;
		return result ;
	}
	
	/**
	 * 保存ip地址信息
	 * @return
	 */
	@RequestMapping("/saveIpLocation")
	@ResponseBody
	public Object saveIpLocation(HttpServletRequest request){
		Result result = new Result(true, "保存成功！");
		Integer id = null;
		if (!"".equals(request.getParameter("id"))) {
			id = Integer.parseInt(request.getParameter("id"));
		}
		String name = request.getParameter("name") ;
		String startIP = request.getParameter("startIP") ;
		String endIP = request.getParameter("endIP") ;
		try {
			if(!Ipv4Address.validIPv4(startIP) || !Ipv4Address.validIPv4(endIP)){
				result.buildError("IP地址无效！");
				return result;
			}
			IpLocation location = new IpLocation();
			location.locationId = id;
			location.netSegment = name;
			location.small = Ipv4Address.parseLong(startIP);
			location.big = Ipv4Address.parseLong(endIP);
			location.countryId = "LOCAL" ;
			if(location.big < location.small){//数据不合法
				result.buildError("起始IP地址不能大于结束IP地址！");
				return result;
			}
			resourceService.saveOrUpdateIpLocation(location) ; 
			NodeUtil.sendCommand(new String[]{"**"}, MessageDefinition.CMD_NODE_CLEAR_LOCATION_CACHE,"",10000) ;
		} catch (Exception e) {
			result.buildSystemError();
			e.printStackTrace();
		}finally{
			IpLocationUtil.clearUserIpLocationCache() ;
		}
		return result ;
	}
	
	/**
	 * 删除系统资源
	 */
	@RequestMapping("delete")
	@ResponseBody
	public Object delete(@RequestParam("ids") String ids) {
		Result result = new Result(true, "操作成功！");
		try {
			String[] idArray = StringUtil.split(ids);
			for(String id:idArray){
				resourceService.deleteIpLocations(StringUtil.toInt(id));
				IpLocationUtil.clearUserIpLocationCache() ;
			}
		} catch (Exception e) {
			result.buildSystemError() ;
			e.printStackTrace();
		}
		return result;
	}
}
