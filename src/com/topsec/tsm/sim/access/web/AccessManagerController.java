package com.topsec.tsm.sim.access.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.access.SysTreeMenu;
import com.topsec.tsm.sim.access.service.SysAccessService;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.util.UUIDUtils;

/**
 * @ClassName: AccessManagerController
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月27日下午4:45:20
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */

@Controller("access")
@RequestMapping("access")
public class AccessManagerController {
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private SysAccessService sysAccessService;
	
	@RequestMapping(value="showIndexMenu",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object showIndexMenu(SID sid,HttpSession session) {
		List<SysTreeMenu> sysTreeMenus=sysAccessService.showTreeMenuByAccountId(sid.getAccountID());
		JSONObject result = new JSONObject();
		if (GlobalUtil.isNullOrEmpty(sysTreeMenus)) {
			if (GlobalUtil.isNullOrEmpty(session.getAttribute("setSysTreeMenus"))) {
				session.removeAttribute("setSysTreeMenus");
			}
			return result;
		}
		Set<SysTreeMenu>setSysTreeMenus=new TreeSet<SysTreeMenu>();
		for (SysTreeMenu sysTreeMenu : sysTreeMenus) {
			if (sysTreeMenu.getMenuLevel()==0) {
				
				for (SysTreeMenu sysTreeMenu1 : sysTreeMenus) {
					if (!sysTreeMenu.equals(sysTreeMenu1)) {
						if (sysTreeMenu1.getMenuParentId().equals(sysTreeMenu.getMenuId())) {
							JSONObject jsonStyle =(JSONObject) JSON.parse(sysTreeMenu1.getJsonStyle());
							sysTreeMenu1.setMenuAttributes(jsonStyle);
							sysTreeMenu.addChild(sysTreeMenu1);
						}
					}
				}
				Map<String, Object> map=jsonStringToMap(sysTreeMenu.getJsonStyle());
				sysTreeMenu.setMenuAttributes(map);
				setSysTreeMenus.add(sysTreeMenu);
			}
		}
		if (GlobalUtil.isNullOrEmpty(session.getAttribute("setSysTreeMenus"))) {
			session.removeAttribute("setSysTreeMenus");
		}
		
		session.setAttribute("setSysTreeMenus", setSysTreeMenus);
		result.put("indexMenu", setSysTreeMenus);
		return result;
	}
	
	@RequestMapping(value="showTestMenu",produces="text/javascript;charset=utf-8")
	@ResponseBody
	public Object showTestMenu(SID sid) {
		JSONObject result = new JSONObject();
		Set<SysTreeMenu> sysTreeMenus=new TreeSet<SysTreeMenu>();
		SysTreeMenu sysTreeMenu=new SysTreeMenu();
		sysTreeMenu.setMenuId("00ddffgghhjj");
		sysTreeMenu.setMenuName("主页");
		sysTreeMenu.setMenuParentId("0");
		sysTreeMenu.setMenuUrl("/page/main/main.html");
		Map<String, Object>mapAttr=new HashMap<String, Object>();
		mapAttr.put("liClass", "active first");
		mapAttr.put("iClass", "icon-home");
		sysTreeMenu.setMenuAttributes(mapAttr);
		try {
			sysTreeMenus.add(sysTreeMenu);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SysTreeMenu sysTreeMenuasset=new SysTreeMenu();
		sysTreeMenuasset.setMenuId("00ddffgghhjj1");
		sysTreeMenuasset.setMenuName("资产");
		sysTreeMenuasset.setMenuParentId("0");
		Map<String, Object>mapAttrasset=new HashMap<String, Object>();
		mapAttrasset.put("liClass", "dropdown");
		mapAttrasset.put("iClass", "icon-asset");
		mapAttrasset.put("ulClass", "dropdown-menu");
		mapAttrasset.put("bClass", "caret");
		mapAttrasset.put("aClass", "dropdown-toggle");
		sysTreeMenuasset.setMenuAttributes(mapAttrasset);
		
		SysTreeMenu sysTreeMenu2=new SysTreeMenu();
		sysTreeMenu2.setMenuName("资产管理");
		sysTreeMenu2.setMenuUrl("/page/asset/asset.html");
		sysTreeMenu2.setMenuLiId("menu_asset");
		sysTreeMenuasset.addChild(sysTreeMenu2);
		
		SysTreeMenu sysTreeMenu21=new SysTreeMenu();
		sysTreeMenu21.setMenuName("拓扑管理");
		sysTreeMenu21.setMenuUrl("/page/asset/topo/topoDesign.html");
		sysTreeMenu21.setMenuLiId("menu_topo");
		sysTreeMenuasset.addChild(sysTreeMenu21);
		
		sysTreeMenus.add(sysTreeMenuasset);
		
		result.put("indexMenu", sysTreeMenus);
		return result;
	}
	private static Map<String, Object> jsonStringToMap(String jsonstring){
		if (GlobalUtil.isNullOrEmpty(jsonstring)) {
			return null;
		}
		
		String string=jsonstring.trim();
		String jsonS=string.substring(1, string.length()-1);
		String[]keyValues=jsonS.split(",");
		if (GlobalUtil.isNullOrEmpty(keyValues)) {
			return null;
		}
		Map<String, Object> resultMap=new HashMap<String, Object>();
		for (String string2 : keyValues) {
			String[]putStrings=string2.split(":");
			if (GlobalUtil.isNullOrEmpty(putStrings)) {
				continue;
			}
			if (putStrings.length==2) {
				resultMap.put(putStrings[0], putStrings[1]);
			}
		}
		return resultMap;
	}
	public static void main(String[]args){
		for (int i = 0; i < 20; i++) {
			System.out.println(UUIDUtils.compactUUID());
		}
		
	}
}
