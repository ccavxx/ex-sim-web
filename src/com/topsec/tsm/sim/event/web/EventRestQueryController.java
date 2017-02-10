package com.topsec.tsm.sim.event.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.rest.server.common.RestUtil;



@Controller
@RequestMapping("eventRestQuery")
public class EventRestQueryController {
	
	protected static Logger log= LoggerFactory.getLogger(EventRestQueryController.class);
	/**
	 * 事件查询
	 * @param criteria
	 * @param result
	 * @return
	 */
	
	@RequestMapping(value="basicEventQuery" ,produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object getEventByBasicCondition(HttpServletRequest request){
		String ip = request.getParameter("requestIp");
		try {
			if(!StringUtils.isBlank(ip)){
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String path = "https://"+ip+"/resteasy/event/getEventByBasicCondition";
				Map params = request.getParameterMap() ;
				String result = HttpUtil.doPostWithSSLByMap(path, params, cookies, "UTF-8") ;
				if(StringUtil.isNotBlank(result)){
					JSONObject json  =JSONObject.parseObject(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}

	/**
	 * 事件时间轴
	 * @param request
	 * @return
	 */
	@RequestMapping(value="expandTimeline" ,produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object expandTimelineByBasicCondition(HttpServletRequest request){
		String ip = request.getParameter("requestIp");
		try {
			if(!StringUtils.isBlank(ip)){
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String path = "https://"+ip+"/resteasy/event/expandTimelineByBasicCondition";
				Map params = request.getParameterMap() ;
				String result = HttpUtil.doPostWithSSLByMap(path, params, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}

	/**
	 * 事件回溯
	 * @author zhaojun 2014-6-27下午3:11:19
	 * @param id
	 * @return
	 */
	@RequestMapping(value="correlatorData"/*,produces="text/javascript;charset=utf-8"*/)
	@ResponseBody
	public Object correlatorData(@RequestParam(value="evtId")Integer id,@RequestParam(value="requestIp")String requestIp){
		try {
			if(!StringUtils.isBlank(requestIp)){
				String path = "https://"+requestIp+"/resteasy/event/correlatorData?evtId="+id;
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(requestIp));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	
	/**
	 * 事件名称树
	 * 事件节点带统计数值
	 * @author zhaojun 2014-5-20下午2:25:33
	 * @return
	 */
	@RequestMapping(value="eventRule")
	@ResponseBody
	public Object getEventRule(@RequestParam(value="requestIp",defaultValue="")String requestIp){//统计每一条规则产生的事件数
		try {
			if(!StringUtils.isBlank(requestIp)){
				String path = "https://"+requestIp+"/resteasy/event/getEventRule";
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(requestIp));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	
	
	/**
	 * 异步统计分级节点事件数
	 * @param levelParam
	 * @return
	 */
	@RequestMapping(value="levelStatistic",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getLevelEventStatistic(HttpServletRequest request){
		String ip = request.getParameter("requestIp");
		try {
			if(!StringUtils.isBlank(ip)){
				String path = "https://"+ip+"/resteasy/event/getLevelEventStatistic?";
				Set<String> set = request.getParameterMap().keySet();
				for (Object key : set) {
					path +=key+"="+StringUtils.join(request.getParameterMap().get(key))+"&";
				}
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	/**
	 * 事件分类树
	 * 节点带统计
	 * 
	 * @author zhaojun 2014-3-17下午6:06:56
	 * @return
	 */
	@RequestMapping(value="eventCategory",produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public Object getEventCategory(@RequestParam(value="requestIp",required=false)String ip,@RequestParam(value="id",required=false)Integer id){
		try {
			if(!StringUtils.isBlank(ip)){
				String path = "https://"+ip+"/resteasy/event/getEventCategory?eid="+id;
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	/**
	 * 知识库关联查询
	 * @param ip
	 * @param id
	 * @return
	 */
	@RequestMapping(value="getAssociatedKnowledgebyEvtId")
	@ResponseBody
	public Object getAssociatedKnowledgeByEvtId(@RequestParam(value="requestIp",required=false)String ip,@RequestParam(value="evtid")float id){
		try {
			if(!StringUtils.isBlank(ip)){
				String path = "https://"+ip+"/resteasy/event/getAssociatedKnowledgeByEvtId?eid="+id;
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	
	/**
	 * 获取列集
	 * @param name
	 * @return
	 */
	@RequestMapping(value="jsondata" ,produces="text/javascript;charset=UTF-8")
	@ResponseBody
	public String getJsonFile(@RequestParam(value="json",required=true)String name){
		String json = null;
		try {
			
		    InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream("resource/ui/event/"+name+".json");
			json = IOUtils.toString(jsonStream,"utf-8");
			IOUtils.closeQuietly(jsonStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return JSONArray.parseArray(json).toJSONString();
	}
	/**
	 * 按照一级分类进行事件数量统计
	 * @return
	 */
	@RequestMapping(value="cat1Statistic",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object cat1Statistic(@RequestParam(value="requestIp",defaultValue="")String requestIp) {
		try {
			if(!StringUtils.isBlank(requestIp)){
				String path = "https://"+requestIp+"/resteasy/event/cat1Statistic";
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(requestIp));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
	/**
	 * 按照事件二级分类进行事件统计
	 * @param cat1 一级分类
	 * @return
	 */
	@RequestMapping(value="cat2Statistic",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object cat2Statistic(@RequestParam(value="requestIp",defaultValue="")String requestIp,@RequestParam("cat1")String cat1) {
		try {
			if(!StringUtils.isBlank(requestIp)){
				cat1=new String(cat1.getBytes("ISO-8859-1"), "UTF-8");
				String path = "https://"+requestIp+"/resteasy/event/cat2Statistic?cat1="+cat1;
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(requestIp));
				String result = HttpUtil.doPostWithSSLByString(path, null, cookies, "UTF-8") ;
				if (StringUtil.isNotBlank(result)){
					JSONArray json  =JSONObject.parseArray(result);
					return json;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}
}
