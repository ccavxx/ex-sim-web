package com.topsec.tsm.sim.sysconfig.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.AssetNameExistException;
import com.topsec.tsm.ass.persistence.AssGroup;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.AssGroupService;
import com.topsec.tsm.ass.service.DeviceService;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.util.AuditLogFacade;

@Controller
@RequestMapping("assGroup")
public class AssGroupController {

	protected static Logger log= LoggerFactory.getLogger(AssGroupController.class);

	/**
	 *  业务组列表查询
	 *  @param sid 登录用户信息
	 *  @param request 请求对象
	 *  @return 跳转业务组列表页面
	 */
	@RequestMapping("assGroupList")
	public String assGroupList(SID sid,HttpServletRequest request) {
		
		AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		List<AssGroup> groups = groupService.getAllWithAssets();
		
		SID currentUser = sid;
		
		JSONArray groupArray = new JSONArray();
		for(AssGroup assGroup : groups){
			
			// 收集group信息包括 groupId、groupName、assets
			JSONObject assGroupObj = new JSONObject();
			assGroupObj.put("groupId", assGroup.getGroupId());
			assGroupObj.put("groupName", assGroup.getGroupName());
			
			// assets 收集 deviceObj
			JSONArray assets = new JSONArray();
			Set<Device> devices = assGroup.getAssets();
			BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("deviceId");
			
			@SuppressWarnings("unchecked")
			Collection<String> userDeviceIds = CollectionUtils.collect(currentUser.getUserDevice(), trans);

			for(Device device : devices){
				
				JSONObject deviceObj = new JSONObject();
				
				deviceObj.put("id", device.getId());
				deviceObj.put("ip", device.getMasterIp());
				deviceObj.put("name", device.getName());
				deviceObj.put("isDelete", device.getIsDelete());
				deviceObj.put("iconCls", AssetUtil.getBigIconClsByDeviceType(device.getDeviceType()));
				deviceObj.put("isEdit", false);
				
				if(currentUser.isOperator() || userDeviceIds.contains(device.getId())){
					deviceObj.put("isEdit", true);
				}
				assets.add(deviceObj);
			}
			
			assGroupObj.put("assets", assets);
			
			groupArray.add(assGroupObj);
		}
		request.setAttribute("assGroupList", groupArray) ;
		return "/page/sysconfig/sysconfig_assGroup";
	}

	/**
	 *  添加业务组
	 *  @param sid 登录用户信息
	 *  @param groupName 业务组名称
	 *  @param request 请求对象
	 *  @return 操作反馈信息
	 */
	@RequestMapping("addAssGroup")
	@ResponseBody
	public Object addAssGroup(SID sid,
			@RequestParam("groupName") String groupName,
			HttpServletRequest request) {
		Result result = new Result();
		if(StringUtil.isBlank(groupName)){
			result.buildError("业务组名称为空");
			return result;
		}
		groupName = StringUtil.recode(groupName);
		AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		try {
			AssGroup group = groupService.getByName(groupName);
			if(group != null){
				result.buildError("该业务组名称已存在");
				return result;
			}

			AssGroup assGroup = new AssGroup();
			assGroup.setGroupName(groupName);
			groupService.add(assGroup);
			result.setStatus(true);
			result.setMessage(assGroup.getGroupId().toString());
			AuditLogFacade.addSuccess("添加业务组", sid.getUserName(), "添加业务组:" + groupName, new IpAddress(sid.getLoginIP()));
		} catch (AssetNameExistException e) {
			result.buildError("业务组新建失败！") ;
		} catch (Exception e) {
			result.buildError("业务组新建失败！") ;
			log.error(e.getMessage());
		}
		return result;
	}
	
	/**
	 *  编辑业务组名称
	 *  @param sid 登录用户信息
	 *  @param groupId 业务组ID
	 *  @param groupName 业务组名称
	 *  @param request 请求对象
	 *  @return 操作反馈信息
	 */
	@RequestMapping("editAssGroupName")
	@ResponseBody
	public Object editAssGroupName(SID sid,
			@RequestParam("groupId") Integer groupId,
			@RequestParam("groupName") String groupName,
			HttpServletRequest request) {
		Result result = new Result();
		if(StringUtil.isBlank(groupName)||groupId==null){
			result.buildError("业务组不存在！");
			return result;
		}
		groupName = StringUtil.recode(groupName);
		AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		try {
			AssGroup assGroupTemp = groupService.get(groupId);
			String assGroupTempName = assGroupTemp.getGroupName();
			if(groupName.equals(assGroupTempName)){
				result.setStatus(true);
				return result;
			}
			AssGroup assGroup = new AssGroup(groupId,groupName);
			groupService.updateGroup(assGroup);
			AssetFacade.getInstance().reloadAllFromDB();
			result.buildSuccess("业务组修改成功");
			AuditLogFacade.updateSuccess("修改业务组", sid.getUserName(), "修改业务组:" + assGroupTempName + " 更新为 " + groupName, new IpAddress(sid.getLoginIP()));
		} catch (AssetNameExistException e) {
			result.buildError("业务组名称已存在") ;
		} catch (Exception e) {
			result.buildError("业务组名称更新失败！") ;
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return result;
	}
	
	/**
	 *  删除业务组
	 *  @param sid 登录用户信息
	 *  @param groupId 业务组ID
	 *  @param request 请求对象
	 *  @return 操作反馈信息
	 */
	@RequestMapping("delAssGroup")
	@ResponseBody
	public Object delAssGroup(SID sid,
			@RequestParam("groupId") Integer groupId,
			HttpServletRequest request) {
		Result result = new Result();
		if(groupId==null){
			result.buildError("数据丢失，请重新操作");
			return result;
		}
		AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		try {
			AssGroup assGroupTemp = groupService.get(groupId);
			groupService.delete(groupId);
			AssetFacade.getInstance().reloadAllFromDB();
			result.buildSuccess("业务组删除成功");
			AuditLogFacade.deleteSuccess("删除业务组", sid.getUserName(), "删除业务组:" + assGroupTemp.getGroupName(), new IpAddress(sid.getLoginIP()));
		} catch (Exception e) {
			result.buildError("业务组删除失败！") ;
			log.error(e.getMessage());
		}
		return result;
	}
	
	/**
	 *  业务组分配资产
	 *  @param sid 登录用户信息
	 *  @param groupId 业务组ID
	 *  @param assetDeviceId 业务组资产ID
	 *  @param request 请求对象
	 *  @return 操作反馈信息
	 */
	@RequestMapping("changeAssetToAssGroup")
	@ResponseBody
	public Object changeAssetToAssGroup(SID sid,
			@RequestParam("groupId") Integer groupId,
			@RequestParam("assetDeviceId") String assetDeviceId,
			HttpServletRequest request) {
		Result result = new Result();
		if(groupId==null || assetDeviceId==null){
			result.buildError("业务组不存在！");
			return result;
		}
		AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		DeviceService deviceService = (DeviceService) SpringWebUtil.getBean("deviceService", request);
		try {

			Device device = deviceService.getDevice(assetDeviceId);
			String groupNameFrom = device.getAssGroup().getGroupName();
			AssGroup groupTo = groupService.get(groupId);
			String groupNameTo = groupTo.getGroupName();

			device.setAssGroup(groupTo);
			if(groupNameTo.equals(groupNameFrom)){
				result.setStatus(true);
				return result;
			}
			deviceService.update(device);
			AssetFacade.getInstance().reloadAssetFromDB(assetDeviceId);
			result.buildSuccess("修改资产业务组成功");
			AuditLogFacade.updateSuccess("修改资产业务组", sid.getUserName(), "修改资产业务组:" + device.getName() + "从业务组" + groupNameFrom + " 转到业务组" + groupNameTo, new IpAddress(sid.getLoginIP()));
		} catch (Exception e) {
			e.printStackTrace();
			result.buildError("修改资产业务组失败！") ;
			log.error(e.getMessage());
		}
		return result;
	}

}
