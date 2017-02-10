package com.topsec.tsm.sim.log.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.rest.server.common.HttpUtil;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.log.bean.LogSearchObject;

@Controller
@RequestMapping("logRestQuery")
public class LogRestQueryController {

	private static final Logger log = LoggerFactory.getLogger(LogRestQueryController.class);

	/**
	 * @method 获取日志列集树
	 * @author zhou_xiaohu
	 * @param HttpServletRequest
	 * @return Object
	 */
	@RequestMapping(value="getTreeForGroup", produces="text/html;charset=utf-8")
	@ResponseBody
	public String getTreeForGroup(HttpServletRequest request) {
		String treeJson = null;
		
		String ip = request.getParameter("ip");
		try{
			if(!StringUtils.isBlank(ip)){
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				treeJson = HttpUtil.doPostWithSSLByString("https://"+ip+"/resteasy/log/getGourpTree", null, cookies, "UTF-8") ;
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}
		return treeJson;
	}

	/**
	 * doLogSearch 获取日志查询结果
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return
	 */
	@RequestMapping(value="doLogSearch", produces="text/html;charset=utf-8")
	@ResponseBody
	public String doLogSearch(@RequestBody LogSearchObject logSearchObject,HttpServletRequest request) {
		String ip = request.getParameter("ip");
		String recordList = null;
		try{
			if(!StringUtils.isBlank(ip)){
				String param ="<Log>" +
				"<Host>"+logSearchObject.getHost()+"</Host>" +
				"<DeviceType>"+logSearchObject.getDeviceType()+"</DeviceType>" +
				"<NodeId>"+logSearchObject.getNodeId()+"</NodeId>" +
				"<QueryStartDate>"+logSearchObject.getQueryStartDate()+"</QueryStartDate>" +
				"<QueryEndDate>"+logSearchObject.getQueryEndDate()+"</QueryEndDate>" +
				"<PageNo>"+logSearchObject.getPageNo()+"</PageNo>" +
				"<PageSize>"+logSearchObject.getPageSize()+"</PageSize>" +
				"<ConditionName>"+StringUtils.join(logSearchObject.getConditionName(), ",")+"</ConditionName>" +
				"<Operator>"+StringUtils.join(logSearchObject.getOperator(), ",")+"</Operator>" +
				"<QueryContent>"+StringUtils.join(logSearchObject.getQueryContent(), ",")+"</QueryContent>" +
				"<Group>"+logSearchObject.getGroup()+"</Group>" +
				"<QueryType>"+StringUtils.join(logSearchObject.getQueryType(), ",")+"</QueryType>" +
				"<Cancel>"+logSearchObject.isCancel()+"</Cancel>" +
			  "</Log>";
				Map<String,String> cookies = new HashMap<String,String>();
				cookies.put("sessionid",RestUtil.getSessionId(ip));
				String result = HttpUtil.doPostWithSSLByString("https://"+ip+"/resteasy/log/search", param, cookies, "UTF-8") ;
				return result ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return recordList;
	}

}
