package com.topsec.tsm.sim.asset.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tsm.ass.persistence.AssGroup;
import com.topsec.tsm.ass.service.AssGroupService;
import com.topsec.tsm.sim.asset.AssetCategory;
import com.topsec.tsm.sim.asset.AssetCategoryUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;

@Controller
@RequestMapping("asset/*")
public class AssetController {
	/**
	 * 返回操作系统数据源
	 * 数据源中的label与value，前台应该页面使用value属于来取值
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping("osList")
	@ResponseBody
	public Object getOSList(){
		JSONArray osArray = new JSONArray() ;
		osArray.add(createJson("value", "Windows Server 2008")) ;
		osArray.add(createJson("value", "Cisco Switch")) ;
		osArray.add(createJson("value", "Cisco Router")) ;
		osArray.add(createJson("value", "H3C Switch")) ;
		osArray.add(createJson("value", "H3C Router")) ;
		osArray.add(createJson("value", "RedHat")) ;
		osArray.add(createJson("value", "Linux")) ;
		osArray.add(createJson("value", "Unix")) ;
		osArray.add(createJson("value", "VMware EXSi Server")) ;
		osArray.add(createJson("value", "Windows Server 2012")) ;
		osArray.add(createJson("value", "Windows Server 2003")) ;
		osArray.add(createJson("value", "Windows Server 2000")) ;
		osArray.add(createJson("value", "Windows 8")) ;
		osArray.add(createJson("value", "Windows 7")) ;
		osArray.add(createJson("value", "Debian")) ;
		osArray.add(createJson("value", "Mac OS")) ;
		osArray.add(createJson("value", "Ubuntu")) ;
		osArray.add(createJson("value", "FreeBSD")) ;
		osArray.add(createJson("value", "Windows Vista")) ;
		osArray.add(createJson("value", "Windows XP")) ;
		osArray.add(createJson("value", "其它")) ;
		return osArray ;
	}
	
	@RequestMapping("assetCategories")
	@ResponseBody
	public Object getAssetCategories(HttpServletRequest request){
		List<AssetCategory> categories = AssetCategoryUtil.getInstance().getCategories() ;
		JSONArray result = new JSONArray(categories.size()) ;
		for (AssetCategory assetCategory : categories) {
			JSONObject catJson = createJson("id",assetCategory.getPathId());
			catJson.put("text", assetCategory.getName()) ;
			
			// 如果levelFlag不为null，则只获得设备类型第一级数据
			String levelFlag = request.getParameter("levelFlag");
			
			if(levelFlag == null){
				
				catJson.put("state", "closed") ;
				JSONArray children = new JSONArray() ;
				catJson.put("children", children) ;
				for (AssetCategory child : assetCategory.getChildren()) {
					JSONObject childJson = createJson("id", child.getPathId()) ;
					childJson.put("text", child.getName()) ;
					childJson.put("attributes",FastJsonUtil.toJSON(child, "pathName=deviceTypeName")) ;
					children.add(childJson) ;
				}
				
			}
			result.add(catJson) ;
		}
		return result ;
	}

	@RequestMapping("groups")
	@ResponseBody
	public Object businessGroups(HttpServletRequest request) {
		 AssGroupService groupService = (AssGroupService) SpringWebUtil.getBean("assetGroupService", request);
		 List<AssGroup> groups = groupService.getAll() ;
		 JSONArray result = (JSONArray) FastJsonUtil.toJSONArray(groups,"groupId","groupName") ;
		return result ;
	}
	
	@RequestMapping("safeRankList")
	@ResponseBody
	public Object safeRank() {
		JSONArray result = new JSONArray(3) ;
		result.add(createJson("value", "高")) ;
		result.add(createJson("value", "中")) ;
		result.add(createJson("value", "低")) ;
		return result ;
	}

	@RequestMapping("test")
	public String test(RedirectAttributes attr) {
		attr.addFlashAttribute("msg", "123") ;
		return "forward:/asset/safeRankList" ;
	}
	
	public ModelAndView handleRequest(HttpServletRequest request,HttpServletResponse response) throws Exception {
		System.out.println("handleRequest");
		return null;
	}

	private JSONObject createJson(String key,Object value){
		JSONObject obj = new JSONObject() ;
		obj.put(key, value) ;
		return obj ;
	}
}
