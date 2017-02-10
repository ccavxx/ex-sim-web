package com.topsec.tsm.sim.sysconfig.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.index.IndexTemplateUtil;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.node.handler.aggregation.AggregateCheck;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.aggregator.AggregatorScene;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.sysconfig.service.AggregatorRuleService;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;

@Controller
@RequestMapping("AggregatorRule")
public class AggregatorRuleController {

	@Autowired
	private AggregatorRuleService aggregatorRuleService;

	@RequestMapping("getAggregatorRuleList")
	@ResponseBody
	public Object getLogFilterRuleList(SID sid, @RequestParam(value = "page", defaultValue = "1") Integer pageNumber, @RequestParam(value = "rows", defaultValue = "10") Integer pageSize,
			String order, String sort) {
		List<SimOrder> orders = new ArrayList<SimOrder>();
		if (order != null && sort != null) {
			if ("deviceTypeName".equals(sort)) {
				sort = "deviceType";
			}
			if ("columnSetName".equals(sort)) {
				sort = "columnSet";
			}
			orders.add(order.equals("asc") ? SimOrder.asc(sort) : SimOrder.desc(sort));
		}
		Map<String, Object> searchCondition = new HashMap<String, Object>();
		if (!"operator".equals(sid.getUserName())) {
			searchCondition.put("creater", sid.getUserName());
		}
		PageBean<AggregatorScene> page = aggregatorRuleService.getList(pageNumber, pageSize, searchCondition, orders.toArray(new SimOrder[0]));
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", page.getTotal());
		result.put("rows", FastJsonUtil.toJSONArray(page.getData(),new JSONConverterCallBack<AggregatorScene>(){
			@Override
			public void call(JSONObject result, AggregatorScene obj) {
				result.put("deviceTypeName", DeviceTypeNameUtil.getDeviceTypeName(obj.getDeviceType(),Locale.getDefault())) ;
				String columnSet = obj.getColumnSet();
				if(columnSet!=null){
					String[] columnSetArray = StringUtil.split(columnSet);
					for(int i=0 ; i<columnSetArray.length ; i++){
						String column = columnSetArray[i];
						String columnName = IndexTemplateUtil.getInstance().getFieldAlias(obj.getDeviceType(), column);
						columnSetArray[i] = columnName;
					}
					result.put("columnSetName", columnSetArray) ;
				}
			}
		}, "id","module","rule","name","available","timeOut","times","condition","maxCount","columnSet","deviceType","creater"));
		return result;
	}
	
	/**
	 * 每次删除选中的归并规则
	 * @param id
	 * @return
	 */
	@RequestMapping("deleteAggregatorRule")
	@ResponseBody
	public Result deleteAggregatorRule(SID sid, @RequestParam("id") Long id,
			@RequestParam("name") String name, HttpServletRequest request){
		name = StringUtil.recode(name);
		Result result = new Result(false, "操作失败！");
		if(id != null){
			DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
			AggregatorScene aggregatorScene = aggregatorRuleService.deleteAggregatorRule( dataSourceService, id);
			AuditLogFacade.deleteSuccess("删除归并规则",sid.getUserName(), "删除归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
			result = new Result(true, "操作成功！");
		}
		return result;
	}
	
	/**
	 * 批量删除选中的归并规则
	 * @param id
	 * @return
	 */
	@RequestMapping("deleteBatchAggregatorRule")
	@ResponseBody
	public Result deleteBatchAggregatorRule(SID sid, @RequestParam("ids") String ids, HttpServletRequest request){
		
		Result result = new Result(false, "操作失败！");
		String[] idArray = ids.split(",");
		for(int i=0; i<idArray.length; i++){
			String id = idArray[i];
			if(id != null){
				DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
				AggregatorScene aggregatorScene = aggregatorRuleService.deleteAggregatorRule( dataSourceService, Long.parseLong(id));
				AuditLogFacade.deleteSuccess("删除归并规则",sid.getUserName(), "删除归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
			}
			if(i == (idArray.length - 1)){
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}
	
	/**
	 * 根据日志源类型deviceType 获取对应的归并数据
	 * @param deviceType
	 * @param request
	 * @return
	 */
	@RequestMapping("getByDeviceObjectType")
	@ResponseBody
	public Object getByDeviceObjectType(@RequestParam("deviceType")String deviceType,HttpServletRequest request){
		List<AggregatorScene> aggregatorScenes = aggregatorRuleService.getByDeviceObjectType(deviceType);
		return FastJsonUtil.toJSONArray(aggregatorScenes, "id=value","name");
	}
	
	/**
	 * 根据deviceType 获取对应的归并数据
	 * @param deviceType
	 * @param request
	 * @return
	 */
	@RequestMapping("getFieldAliasSelect")
	@ResponseBody
	public Object getFieldAliasSelect(@RequestParam("deviceType") String deviceType,HttpServletRequest request){
		List<String> list=IndexTemplateUtil.getInstance().getFieldNames(deviceType);//列集
		List<String> listc=IndexTemplateUtil.getInstance().getFieldAlias(deviceType);
		JSONObject fieldAlias = new JSONObject();
		int i=0;
		if(!list.isEmpty()&&!listc.isEmpty()){
			for (String enkey : list) {
				fieldAlias.put(enkey, listc.get(i++));
			}
		}
		return fieldAlias;
	}
	
	/**
	 * 保存或编辑对应的归并数据
	 * @param rule
	 * @param request
	 * @return
	 */
	@RequestMapping("saveOrUpdateAggregatorScene")
	@ResponseBody
	public Result saveOrUpdateAggregatorScene( SID sid, @ModelAttribute AggregatorScene aggregatorScene,
				HttpServletRequest request ) {
		Result result = new Result(false, "保存失败！");
		String name = aggregatorScene.getName();//场景名称
		if (name!=null) {
			boolean isUpdate = false;
			Long id = aggregatorScene.getId();//ID
			String base = request.getParameter("base");
			String filterSql = request.getParameter("filterSql");
			String securityObjectType = request.getParameter("securityObjectType");
			aggregatorScene.setDeviceType(securityObjectType);
			Integer availableTemp = aggregatorScene.getAvailable();
			if(availableTemp==null){
				aggregatorScene.setAvailable(0);
			}
			String[] columnSetArray = request.getParameterValues("columnSet");
			if(columnSetArray!=null){
				StringBuffer columnSet =new StringBuffer();
				for(int i=0;i<columnSetArray.length;i++){
					if(i==columnSetArray.length-1){
						columnSet.append(columnSetArray[i]);
					}else{
						columnSet.append(columnSetArray[i]);
						columnSet.append(",");
					}
				}
				aggregatorScene.setColumnSet(columnSet.toString());
			}
			// 数据存入保存对象开始
			AggregateCheck aggregatecheck = new AggregateCheck();
			if(aggregatecheck.checkNum(base)==false){
				base="0";
			}
			//归并基础条件保存格式组装[{"base":"end","elsment":{"name":"key"}}]
			StringBuffer condition =new StringBuffer();
		    condition.append("[{\"base\":\"");
		    condition.append(base);
		    condition.append("\",\"element\":{");
	        condition.append("}}]");
	        aggregatorScene.setRule(condition.toString());
	        aggregatorScene.setCondition(filterSql);
	        aggregatorScene.setAggregatorName(name);
	        List<AggregatorScene> aggregatorSceneList = aggregatorRuleService.getAggregatorSceneByName(name);
			//数据存入保存对象接受	
			Serializable aggreid = null;
			if(id==null){
				aggregatorScene.setCreater(sid.getUserName());
				if( aggregatorSceneList != null && aggregatorSceneList.size() > 0 ) {
					result.setMessage("该名称已经存在");
					return result;
				}
				aggreid = aggregatorRuleService.saveAggregatorScene(aggregatorScene);
				if(aggreid!=null){
					isUpdate = true;
					AuditLogFacade.userOperation("添加归并规则",sid.getUserName(), "添加归并规则名称："+aggregatorScene.getName(),new IpAddress(sid.getLoginIP()),Severity.LOW,AuditCategoryDefinition.SYS_ADD,true);
				}
			}else{
				if(aggregatorSceneList !=null && aggregatorSceneList.size()>0 && (!id.equals(aggregatorSceneList.get(0).getId())) ){
					result.setMessage("该名称已经存在");
					return result;
				}
				DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
				//id不等于空时候进行归并规则修改
				aggregatorScene.setId(Long.parseLong(id.toString()));
				AggregatorScene tempScene = aggregatorRuleService.getAggregatorScene(id);
				String aggregatorSceneName = tempScene.getName();
				aggregatorScene.setCreater(tempScene.getCreater());
				aggregatorRuleService.updateAggregatorScene( dataSourceService, aggregatorScene);
				isUpdate = true;
				if(!aggregatorSceneName.equals(aggregatorScene.getName())){
					aggregatorSceneName = aggregatorSceneName + " 更新为 " + aggregatorScene.getName();
				}
				AuditLogFacade.userOperation("更新归并规则",sid.getUserName(), "更新归并规则："+aggregatorSceneName,new IpAddress(sid.getLoginIP()),Severity.LOW,AuditCategoryDefinition.SYS_UPGRADE,true);
			}
			if(isUpdate){
				result.setStatus(true);
				result.setMessage("保存成功！");
			}
		}
		return result;
	}
	
	/**
	 * ajax验证归并规则名称是否存在
	 * @param name
	 * @param id
	 * @return
	 */
	@RequestMapping(value="validateAggregatorRule", produces="text/javascript; charset=utf-8")
	@ResponseBody
	public Object validateAggregatorRule(@RequestParam("name") String name, @RequestParam("id") String id) {
		List<AggregatorScene> aggregatorSceneList = aggregatorRuleService.getAggregatorSceneByName(name);
		boolean isUpdate = false;
		if (null != id && !"".equals(id)) {
			isUpdate = true;
		}
		JSONObject result = new JSONObject();
		if(isUpdate) {
			if (aggregatorSceneList != null && aggregatorSceneList.size()>0 && !id.equals(String.valueOf(aggregatorSceneList.get(0).getId()))) {
				result.put("error", "该名称已存在");
			} 
		} else {
			if (aggregatorSceneList != null && aggregatorSceneList.size()>0) {
				result.put("error", "该名称已存在");
			}
		}
		
		return result;
	}

	/**
	 *  每次修改一条选中数据状态
	 *  @param id
	 *  @param available
	 *  @param request
	 *  @return
	 */
	@RequestMapping("modifyAggregatorRuleAvailable")
	@ResponseBody
	public Result modifyAggregatorRuleAvailable(SID sid, @RequestParam("id") Long id,
			@RequestParam("available") Integer available, HttpServletRequest request){
		Result result = new Result(false, "操作失败！");
		if( id!=null && id>0 && available!=null ){
			AggregatorScene aggregatorScene = aggregatorRuleService.getAggregatorScene(id);
			if(aggregatorScene != null){
				aggregatorScene.setAvailable(available);
				DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
				aggregatorRuleService.updateAggregatorSceneAvailable( dataSourceService, aggregatorScene, available);
				if(available==1){
					AuditLogFacade.start("启用归并规则",sid.getUserName(), "启用归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
				}else if(available==0){
					AuditLogFacade.stop("禁用归并规则",sid.getUserName(), "禁用归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
				}
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}
	
	/**
	 *  修改状态
	 *  @param id
	 *  @param available
	 *  @param request
	 *  @return
	 */
	@RequestMapping("modifyBatchAggregatorRuleAvailable")
	@ResponseBody
	public Result modifyBatchAggregatorRuleAvailable(SID sid, @RequestParam("ids") String ids,
			@RequestParam("available") Integer available, HttpServletRequest request){

		Result result = new Result(false, "操作失败！");
		String[] idArray = ids.split(",");

		for(int i=0; i<idArray.length; i++){
			String id = idArray[i];
			AggregatorScene aggregatorScene = aggregatorRuleService.getAggregatorScene(Long.parseLong(id));
			
			if(aggregatorScene != null && available != null){

				aggregatorScene.setAvailable(available);
				DataSourceService dataSourceService = (DataSourceService) SpringWebUtil.getBean("dataSourceService", request);
				aggregatorRuleService.updateAggregatorSceneAvailable( dataSourceService, aggregatorScene, available);

				if(available == 1) {
					AuditLogFacade.start("启用归并规则",sid.getUserName(), "启用归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
				} else if(available == 0) {
					AuditLogFacade.stop("禁用归并规则",sid.getUserName(), "禁用归并规则:" + aggregatorScene.getName(), new IpAddress(sid.getLoginIP())) ;
				}
			}
			if(i == (idArray.length - 1)) {
				result = new Result(true, "操作成功！");
			}
		}
		return result;
	}
}
