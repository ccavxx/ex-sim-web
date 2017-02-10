package com.topsec.tsm.sim.report.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.topsec.license.util.ChangePageEncode;
import com.topsec.tal.base.report.service.RptMasterTbService;
import com.topsec.tal.base.util.LogKeyInfo;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.sim.access.IsEditNodeType;
import com.topsec.tsm.sim.access.NodeTypeShow;
import com.topsec.tsm.sim.access.service.NodeTypeService;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.asset.service.TopoService;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.report.bean.ReportBean;
import com.topsec.tsm.sim.report.bean.StandardTree;
import com.topsec.tsm.sim.report.chart.highchart.CreateChartFactory;
import com.topsec.tsm.sim.report.chart.highchart.model.ChartTable;
import com.topsec.tsm.sim.report.chart.highchart.model.ColumnData;
//import com.topsec.tsm.sim.report.model.ReportConfStruct;
import com.topsec.tsm.sim.report.model.ReportModel;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.report.util.TopoUtil;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FacadeUtil;

/**
 * @ClassName: TopoReportController
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年9月16日下午2:59:43
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */

public class TopoReportController {
	private Logger logger = LoggerFactory.getLogger(TopoReportController.class) ;
	
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private ReportService reportService;
	@Autowired
	private TopoService topoService ; 
	@Autowired
	private NodeTypeService nodeTypeService;
	/**
	 * 构建基本报表左侧树
	 */
	public Object topoTree(SID sid,HttpServletRequest request){
		List<AssTopo>assTopos=TopoUtil.allAssTopoList(topoService);
		StandardTree[] treeResultArr=new StandardTree[2];
		StandardTree treeResult=new StandardTree();
		
		treeResult.setId("0");
		treeResult.setText("资产事件报表");
		treeResult.setState("open");
		treeResult.setType("TRUNK");
		treeResult.putAttribute("topoId", "allAssTopos");
		treeResult.putAttribute("nodeLevel", 0);
		if (!GlobalUtil.isNullOrEmpty(assTopos)) {
			for (AssTopo assTopo : assTopos) {
				List<Map> maps=TopoUtil.showGroupListByAssTopo(assTopo);
				StandardTree standardTree=new StandardTree();
				standardTree.setParentId(treeResult.getId());
				standardTree.setId(assTopo.getId()+"");
				standardTree.setText(assTopo.getName());
				standardTree.putAttribute("topoId", assTopo.getId());
				int level=0;
				if (!GlobalUtil.isNullOrEmpty(maps)){
					getAssTopoTree(assTopo,standardTree,maps,level);
					getTopLevelAssTopoTree(assTopo,standardTree,level);
				}else {
					getTopLevelAssTopoTree(assTopo,standardTree,level);
				}
				treeResult.addChild(standardTree);
			}
		}
		
		StandardTree statusTree=new StandardTree();
		statusTree.setId("1");
		statusTree.setText("资产状态报表");
		statusTree.setState("closed");
		statusTree.setChildren(treeResult.getChildren());
		statusTree.setType("TRUNK");
		statusTree.putAttribute("topoId", "allAssTopos");
		statusTree.putAttribute("nodeLevel", 0);
		treeResultArr[0]=treeResult;
		treeResultArr[1]=statusTree;
		//先只显示 资产事件报表 下面的子报表
		Object jsonObject=JSON.toJSON(treeResultArr[0].getChildren());
		return jsonObject;
	}
	
	private void getAssTopoTree(AssTopo assTopo,StandardTree standardTree,List<Map> maps,int level){
		level++;
		if (GlobalUtil.isNullOrEmpty(maps)) {
			standardTree.setState("open");
			standardTree.setType("LEAF");
		}else{
			String scanNodeId=null;
			for (Map map : maps) {
				StandardTree treeChild=new StandardTree();
				treeChild.setParentId(standardTree.getId());
				treeChild.setId(map.get("id").toString());
				treeChild.setText(map.get("text").toString());
				List<Device>deviceList=TopoUtil.showIpsByAssGroup(assTopo, map.get("id").toString());
				if (!GlobalUtil.isNullOrEmpty(deviceList)) {
					for (Device device : deviceList) {
						StandardTree treeChildChild=new StandardTree();
						treeChildChild.setParentId(standardTree.getId());
						treeChildChild.setId(device.getMasterIp().toString());
						treeChildChild.setText(device.getMasterIp().toString()+"("+DeviceTypeNameUtil.getDeviceTypeName(device.getDeviceType(), Locale.getDefault())+")");
						treeChildChild.setState("open");
						treeChildChild.setType("LEAF");
						scanNodeId=device.getScanNodeId();
						treeChildChild.putAttribute("ip", device.getMasterIp().toString());
						treeChildChild.putAttribute("deviceType", device.getDeviceType());
						treeChildChild.putAttribute("scanNodeId", scanNodeId);
						treeChild.addChild(treeChildChild);
					}
					treeChild.setType("TRUNK");
					treeChild.setState("closed");
					treeChild.putAttribute("topoId", assTopo.getId());
					treeChild.putAttribute("scanNodeId", scanNodeId);
					treeChild.putAttribute("nodeLevel", level+1);
				}else{
					treeChild.setType("LEAF");
					treeChild.setState("open");
					treeChild.putAttribute("ip", map.get("ip"));//需要测试
				}
				List<Map> childMaps=TopoUtil.showGroupListByAssGroup(assTopo, map.get("id").toString());
				if (!GlobalUtil.isNullOrEmpty(childMaps)) {
					getAssTopoTree(assTopo,treeChild,childMaps,level);
				}
				standardTree.addChild(treeChild);
			}
			standardTree.setType("TRUNK");
			standardTree.putAttribute("topoId", assTopo.getId());
			standardTree.putAttribute("nodeLevel", level);
			standardTree.putAttribute("scanNodeId", scanNodeId);
		}
	}
	private void getTopLevelAssTopoTree(AssTopo assTopo,StandardTree treeChild,int level){
		List<Device>deviceList=TopoUtil.showIpsByAssTopo(assTopo);
		if (GlobalUtil.isNullOrEmpty(deviceList)) {
			if (level==0) {
				treeChild.setType("TRUNK");
				treeChild.setState("open");
				treeChild.putAttribute("topoId", assTopo.getId());
				treeChild.putAttribute("nodeLevel", level+1);
				treeChild.putAttribute("scanNodeId", "");
			}
			return;
		}
		String scanNodeId=null;
		for (Device device : deviceList) {
			StandardTree treeChildChild=new StandardTree();
			treeChildChild.setParentId(assTopo.getId()+"");
			treeChildChild.setId(device.getMasterIp().toString());
			treeChildChild.setText(device.getMasterIp().toString()+"("+DeviceTypeNameUtil.getDeviceTypeName(device.getDeviceType(), Locale.getDefault())+")");
			treeChildChild.setState("open");
			treeChildChild.setType("LEAF");
			scanNodeId=device.getScanNodeId();
			treeChildChild.putAttribute("ip", device.getMasterIp().toString());
			treeChildChild.putAttribute("deviceType", device.getDeviceType());
			treeChildChild.putAttribute("scanNodeId", scanNodeId);
			treeChild.addChild(treeChildChild);
		}
		treeChild.setType("TRUNK");
		treeChild.setState("closed");
		treeChild.putAttribute("topoId", assTopo.getId());
		treeChild.putAttribute("nodeLevel", level+1);
		treeChild.putAttribute("scanNodeId", scanNodeId);
	}
	public String reportQuery(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONObject json = new JSONObject();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		ReportModel.setBeanPropery(bean);
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map> subResult = new ArrayList<Map>();
		Map<Integer,Integer> rowColumns = new HashMap<Integer, Integer>();
		
		List<Map<String,Object>> subResultTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(bean.getMstrptid(), StringUtil.toInt(bean.getTalTop(),5))});
		Map<Integer,Integer> rowColumnsTeMap = ReportModel.getRowColumns(subResultTemp) ;
		int evtRptsize=subResultTemp.size();
		if (!GlobalUtil.isNullOrEmpty(subResultTemp)) {
			subResult.addAll(subResultTemp);
			rowColumns.putAll(rowColumnsTeMap);
		}
		String nodeType=bean.getNodeType();
		String dvcaddress=bean.getDvcaddress();
		if (!GlobalUtil.isNullOrEmpty(bean.getDvctype())
				&& bean.getDvctype().startsWith("Profession/Group")
				&& !GlobalUtil.isNullOrEmpty(nodeType) 
				&& !GlobalUtil.isNullOrEmpty(dvcaddress)) {
			Map map=TopoUtil.getAssetEvtMstMap();
			String mstIds=null;
			List<SimDatasource> simDatasources=dataSourceService.getByIp(dvcaddress);
			if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
				mstIds="";
				for (SimDatasource simDatasource : simDatasources) {
					if (map.containsKey(simDatasource.getSecurityObjectType())) {
						mstIds+=map.get(simDatasource.getSecurityObjectType()).toString()+":::";
					}else {
						String keyString=getStartStringKey(map, simDatasource.getSecurityObjectType());
						if (!GlobalUtil.isNullOrEmpty(keyString)) {
							mstIds+=map.get(keyString).toString()+":::";
						}
					}
				}
				if (mstIds.length()>3) {
					mstIds=mstIds.substring(0,mstIds.length()-3);
				}
			}else {
				if (map.containsKey(nodeType)) {
					mstIds=map.get(nodeType).toString();
				}else {
					String keyString=getStartStringKey(map, nodeType);
					if (!GlobalUtil.isNullOrEmpty(keyString)) {
						mstIds=map.get(keyString).toString();
					}
				}
			}
			/**/
			if (!GlobalUtil.isNullOrEmpty(mstIds)) {
				String[]mstIdArr=mstIds.split(":::");
				for (String string : mstIdArr) {
					List<Map<String,Object>> subTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(string, StringUtil.toInt(bean.getTalTop(),5))});
					if (!GlobalUtil.isNullOrEmpty(subTemp)) {
						int maxCol=0;
						if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
							maxCol=getMaxOrMinKey(rowColumns, 1);
						}
						for (Map map2 : subTemp) {
							Integer row = (Integer) map2.get("subRow")+maxCol;
							map2.put("subRow", row);
						}
						subResult.addAll(subTemp);
						Map<Integer,Integer> rowColTemp = ReportModel.getRowColumns(subTemp) ;
						rowColumns.putAll(rowColTemp);
//						rowColumns=newLocationMap(rowColumns, rowColTemp);
					}
				}
			}
		}
		StringBuffer layout = new StringBuffer();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("dvcType", bean.getDvctype());
		params.put("talTop", bean.getTalTop());
		params.put("mstId", bean.getMstrptid());
		params.put("eTime", bean.getTalEndTime());
		params.put("rootId", bean.getRootId());
		params.put("assGroupNodeId", bean.getAssGroupNodeId());
		params.put("topoId", bean.getTopoId());
		params.put("nodeLevel", bean.getNodeLevel());
		params.put("nodeType", bean.getNodeType());
		String sUrl = null;
		int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"),1280) - 25- 200 ;
		
		StringBuffer subUrl = new StringBuffer();
		Map layoutValue = new HashMap() ;
		for(int i=0,len =subResult.size();i<len;i++ ){
			params.remove("sTime");
			Map subMap = subResult.get(i);
			if(i==0){
				bean.setViewItem(StringUtil.toString(subMap.get("viewItem"), ""));
			}
			Integer row = (Integer) subMap.get("subRow");
			layout.append(row + ":" + subMap.get("subColumn")+ ",");
			if (GlobalUtil.isNullOrEmpty(subMap)) {
				continue;
			}
			params.put("sTime", bean.getTalStartTime());
			
			if (i<evtRptsize) {
				sUrl=getUrl(ReportUiConfig.subEvtUrl,request, params,bean.getTalCategory(),true).toString();
			}else{
				sUrl=getUrl(ReportUiConfig.subEvtUrl,request, params,bean.getTalCategory(),false).toString();
			}
			subUrl.replace(0, subUrl.length(),sUrl);
			subUrl.append("&").append(ReportUiConfig.subrptid).append("=").append(subMap.get("subId"));
			subUrl.substring(0, subUrl.length());
			int column = rowColumns.get(row);
			String width = String.valueOf((screenWidth-10*column)/column) ;
			String _column = subMap.get("subColumn").toString();
			layoutValue.put(row+_column, ReportUiUtil.createSubTitle(subMap, width,subUrl.toString(),bean.getTalCategory(),StringUtil.toInt(bean.getTalTop(), 5)));
		}

		if(!GlobalUtil.isNullOrEmpty(subResult)&&subResult.size()>0){
			if (!GlobalUtil.isNullOrEmpty(subResult.get(0).get("mstName"))) {
				request.setAttribute("title", subResult.get(0).get("mstName"));
			}
	    }
		String htmlLayout = ReportModel.createMstTable(layout.toString(),layoutValue);
		StringBuffer sb = getExportUrl(request, params,talCategory,true);
		request.setAttribute("expUrl", sb.toString());
		request.setAttribute("layout", htmlLayout);
		request.setAttribute("bean", bean);
				
		return "/page/report/assetStatusEvtReport";
	}
	/**
	 * 暂时的是所有的综合信息报表，带有图表的
	 * @param sid
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String dynamicComprehensiveReport(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONObject json = new JSONObject();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		ReportModel.setBeanPropery(bean);
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map<String,Object>> subResult = new ArrayList<Map<String,Object>>();
		Map<Integer,Integer> rowColumns = new HashMap<Integer, Integer>();
		List<String> dvcTypes = null;
		List<SimDatasource> simDatasources=dataSourceService.getDataSource(DataSourceService.CMD_ALL);
		if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
			dvcTypes=setDvcTypes(sid,simDatasources,bean,dvcTypes);
		}
		String scanNodeId=null;
		if (! GlobalUtil.isNullOrEmpty(simDatasources)) {
			scanNodeId=simDatasources.get(0).getAuditorNodeId();
		}else {
			scanNodeId="";
		}
		List<String>mstrptidAndNodeTypeList=new ArrayList<String>();
		setMstIdAndScanNodeType(dvcTypes,mstrptidAndNodeTypeList);
		int evtRptsize=0;
		if (!GlobalUtil.isNullOrEmpty(mstrptidAndNodeTypeList)) {
			List<Map<String,Object>> subResultTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt((mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[0], StringUtil.toInt(bean.getTalTop(),5))});
			Map<Integer,Integer> rowColumnsTeMap = ReportModel.getRowColumns(subResultTemp) ;
			evtRptsize=subResultTemp.size();
			if (!GlobalUtil.isNullOrEmpty(subResultTemp)) {
				for (Map map2 : subResultTemp) {
					map2.put("subject", (mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[1]);
				}
				subResult.addAll(subResultTemp);
				rowColumns.putAll(rowColumnsTeMap);
			}
			
			int len=mstrptidAndNodeTypeList.size();
			for (int i=1;i<len;i++) {
				String mstrptidAndNodeType=mstrptidAndNodeTypeList.get(i);
				String string=mstrptidAndNodeType.split("IDandNODEtype")[0];
				List<Map<String,Object>> subTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(string, StringUtil.toInt(bean.getTalTop(),5))});
				if (!GlobalUtil.isNullOrEmpty(subTemp)) {
					int maxCol=0;
					if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
						maxCol=getMaxOrMinKey(rowColumns, 1);
					}
					for (Map map2 : subTemp) {
						Integer row = (Integer) map2.get("subRow")+maxCol;
						map2.put("subRow", row);
						map2.put("subject", mstrptidAndNodeType.split("IDandNODEtype")[1]);
					}
					subResult.addAll(subTemp);
					Map<Integer,Integer> rowColTemp = ReportModel.getRowColumns(subTemp) ;
					rowColumns.putAll(rowColTemp);
//					rowColumns=newLocationMap(rowColumns, rowColTemp);
				}
			}
		}
		
		StringBuffer layout = new StringBuffer();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("eTime", bean.getTalEndTime());
		String sUrl = null;
		int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"),1280) - 25- 200 ;
		
		StringBuffer subUrl = new StringBuffer();
		Map layoutValue = new HashMap() ;
		
		setImfo(subResult, params, bean, layout, subUrl, layoutValue, scanNodeId, rowColumns, sUrl, screenWidth);

		if(!GlobalUtil.isNullOrEmpty(subResult)&&subResult.size()>0){
			if (!GlobalUtil.isNullOrEmpty(subResult.get(0).get("mstName"))) {
				request.setAttribute("title", subResult.get(0).get("mstName"));
			}
	    }
		String htmlLayout = ReportModel.createMstTable(layout.toString(),layoutValue);
		StringBuffer sb = getExportUrl(request, params,talCategory,true);
		request.setAttribute("expUrl", sb.toString());
		request.setAttribute("layout", htmlLayout);
		request.setAttribute("bean", bean);
				
		return "/page/report/assetStatusEvtReport";
	}
	/**
	 * 展示所有的 仅仅文字表格数据的 按照设备类型分组块显示
	 * @param sid
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String comprehensiveInformReport(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONObject json = new JSONObject();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		ReportModel.setBeanPropery(bean);
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map<String,Object>> comprehensiveSubAndColList = new ArrayList<Map<String,Object>>();
		List<String> hasComprehensiveReport=reportService.findAllTypeList();
		IsEditNodeType isEditNodeType=nodeTypeService.findIsEditByUserName(sid.getUserName());
		if (GlobalUtil.isNullOrEmpty(isEditNodeType)) {
			isEditNodeType=new IsEditNodeType();
			initEditNodeType(isEditNodeType,sid);
			if (!GlobalUtil.isNullOrEmpty(isEditNodeType.getUserName())) {
				nodeTypeService.saveIsEditNodeType(isEditNodeType);
			}
		}
		List<String> dvcTypes = null;
		String scanNodeId= null;
		List<SimDatasource> simDatasources = dataSourceService
				.getDataSource(DataSourceService.CMD_ALL);
		if (!isEditNodeType.getIsEdit()) {
			if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
				dvcTypes = setDvcTypes(sid, simDatasources, bean, dvcTypes);
				hasComprehensive(dvcTypes,hasComprehensiveReport);
			}
			saveAndUpDateStatus(dvcTypes,sid,isEditNodeType);
		}else {
			List<String>deviceTypeList= ReportModel.getDeviceTypeList(dataSourceService,sid);
			hasComprehensive(deviceTypeList,hasComprehensiveReport);
			if (!GlobalUtil.isNullOrEmpty(deviceTypeList)) {
				List<NodeTypeShow>nodeTypeShowList=nodeTypeService.findTypeShowsByUserName(sid.getUserName());
				removeShowsType(deviceTypeList,nodeTypeShowList);
				saveAndUpDateStatus(deviceTypeList,sid,isEditNodeType);
				if (!GlobalUtil.isNullOrEmpty(nodeTypeShowList)) {
					for (NodeTypeShow nodeTypeShow : nodeTypeShowList) {
						nodeTypeService.deleteNodeTypeShow(nodeTypeShow);
					}
				}
			}
			
			List<NodeTypeShow>nodeTypeShows=nodeTypeService.findByUserNameAndIsShow(sid.getUserName(),true);
			dvcTypes=new ArrayList<String>(nodeTypeShows.size());
			if (!GlobalUtil.isNullOrEmpty(nodeTypeShows)) {
				for (NodeTypeShow nodeTypeShow : nodeTypeShows) {
					if (!dvcTypes.contains(nodeTypeShow.getNodeType())) {
						dvcTypes.add(nodeTypeShow.getNodeType());
					}
				}
			}
		}
		if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
			scanNodeId = simDatasources.get(0).getAuditorNodeId();
		} else {
			scanNodeId = "";
		}
		List<String>mstrptidAndNodeTypeList=new ArrayList<String>();
		setMstIdAndScanNodeType(dvcTypes,mstrptidAndNodeTypeList);
		int evtRptsize=0;
		if (!GlobalUtil.isNullOrEmpty(mstrptidAndNodeTypeList)) {
			for (String string : mstrptidAndNodeTypeList) {
				List<Map<String,Object>> subResult = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt((string.split("IDandNODEtype"))[0], StringUtil.toInt(bean.getTalTop(),5))});
				Map<Integer,Integer> rowColumns = ReportModel.getRowColumns(subResult) ;
				if (!GlobalUtil.isNullOrEmpty(subResult)) {
					for (Map map2 : subResult) {
						map2.put("subject", (string.split("IDandNODEtype"))[1]);
					}
					Map subResultAndRowColsMap=new HashMap<String, Object>();
					subResultAndRowColsMap.put("subResult", subResult);
					subResultAndRowColsMap.put("rowColumns", rowColumns);
					subResultAndRowColsMap.put("scanNodeType", string.split("IDandNODEtype")[1]);
					comprehensiveSubAndColList.add(subResultAndRowColsMap);
				}
				
			}
		}
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("eTime", bean.getTalEndTime());
		String sUrl = null;
		int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"),1280) - 25- 200 ;
		
		StringBuffer subUrl = new StringBuffer();
		StringBuffer htmlOut = new StringBuffer();
		Map layoutValue = new HashMap() ;
		htmlOut.append("<table><tbody>");
		int tdnum=0;
		boolean repeatRep=false;
		List<Object>subIdList=new ArrayList<Object>();
		int totalSub=0;
		for (Map<String, Object> map:comprehensiveSubAndColList) {
			List<Map> subResult=(List<Map>)map.get("subResult");
			int len = subResult.size();
			totalSub+=len;
			for (int i = 0; i < len; i++) {
				Map subMap = subResult.get(i);
				Object subId=subMap.get("subId");
				if (!subIdList.contains(subId)) {
					subIdList.add(subId);
				}
			}
		}
		if (subIdList.size()<totalSub) {
			repeatRep=true;
		}
		int repeatj=0;
		for (Map<String, Object> map:comprehensiveSubAndColList) {
			List<Map> subResult=(List<Map>)map.get("subResult");
			Map<Integer,Integer> rowColumns=(Map<Integer,Integer>)map.get("rowColumns");
			String scanNodeType=(String)map.get("scanNodeType");
			StringBuffer layout = new StringBuffer();
			int divPaneltotalwidth=0;
			divPaneltotalwidth=(int) ((screenWidth-10)/2.3);
			for (int i = 0, len = subResult.size(); i < len; i++) {
				params.remove("sTime");
				Map subMap = subResult.get(i);
				if (i == 0) {
					bean.setViewItem(StringUtil.toString(
							subMap.get("viewItem"), ""));
				}
				Integer row = (Integer) subMap.get("subRow");
				layout.append(row + ":" + subMap.get("subColumn") + ",");
				if (GlobalUtil.isNullOrEmpty(subMap)) {
					continue;
				}
				params.put("sTime", bean.getTalStartTime());
				params.put("dvcType", subMap.get("subject"));
				params.put("onlyTable", "onlyTable");
				String reportType=request.getParameter("reportType");
				if (!GlobalUtil.isNullOrEmpty(reportType)) {
					params.put("reportType", reportType);
				}
				sUrl = getComprehensiveUrl(ReportUiConfig.subEvtUrl,
						scanNodeId, params, bean.getTalCategory()).toString();
				params.remove("dvcType");
				subUrl.replace(0, subUrl.length(), sUrl);
				subUrl.append("&").append(ReportUiConfig.subrptid).append("=")
						.append(subMap.get("subId"));
				subUrl.substring(0, subUrl.length());
				if (repeatRep) {
					subUrl.append("subREPEAT_").append(repeatj++);
				}
				int column = rowColumns.get(row);
				String width = String.valueOf(((screenWidth - 10 * column)
						/ column)/2.4);
				String _column = subMap.get("subColumn").toString();
				subMap.put("InformReportOnlyTable", true);
				layoutValue.put(row + _column, ReportUiUtil.createSubTitle(
						subMap, width, subUrl.toString(),
						bean.getTalCategory(),
						StringUtil.toInt(bean.getTalTop(), 5)));
			}
			String htmlLayoutmp = ReportModel.createMstTable(layout.toString(),layoutValue);
			String cnDvcName=ReportUiUtil.getDeviceTypeName(scanNodeType.replace("Comprehensive", ""), Locale.getDefault());
			if (tdnum%2==0) {
				htmlOut.append("<tr>");
			}
			tdnum++;
			htmlOut.append("<td style='padding: 14px'><div id='div_tt' title='"+cnDvcName)
			.append("' class='easyui-panel' data-options='headerCls:\"sim-panel-header\",width:"+divPaneltotalwidth+",closable:true'")
			.append(htmlLayoutmp).append("</div></td>");
			if (tdnum%2==0) {
				htmlOut.append("</tr>");
			}
		}
		if(comprehensiveSubAndColList.size()>0){
			request.setAttribute("title", "综合信息报表");
	    }
		String htmlLayout=htmlOut.toString();
		StringBuffer sb = getExportUrl(request, params,talCategory,true);
		request.setAttribute("expUrl", sb.toString());
		request.setAttribute("layout", htmlLayout);
		request.setAttribute("bean", bean);
				
		return "/page/report/assetStatusEvtReport";
	}
	private void hasComprehensive(List<String>dvcList,List<String>mstList){
		if (GlobalUtil.isNullOrEmpty(dvcList)) {
			return;
		}
		if (GlobalUtil.isNullOrEmpty(mstList)) {
			dvcList.clear();
			return;
		}
		List<String> removeList=new ArrayList<String>(dvcList.size());
		for (String string : dvcList) {
			String tString=string;
			if (string.indexOf("/")>-1) {
				int xl=string.indexOf("/");
				tString=string.substring(0, xl);
			}
			if (!mstList.contains("Comprehensive"+tString)
					&& !mstList.contains("Comprehensive"+string)) {
				removeList.add(string);
			}
		}
		if (removeList.size()>0) {
			dvcList.removeAll(removeList);
		}
	}
	public String typeComprehensiveReport(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONObject json = new JSONObject();
		ReportBean bean = new ReportBean();
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		ReportModel.setBeanPropery(bean);
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map<String,Object>> subResult = new ArrayList<Map<String,Object>>();
		Map<Integer,Integer> rowColumns = new HashMap<Integer, Integer>();
		List<String> dvcTypes = null;
		List<SimDatasource> simDatasources=null;
		if (!GlobalUtil.isNullOrEmpty(bean.getDvctype())) {
			simDatasources=dataSourceService.getDataSourceByDvcType(bean.getDvctype());
		}
		if (!GlobalUtil.isNullOrEmpty(simDatasources)) {
			dvcTypes=new ArrayList<String>();
			dvcTypes.add(bean.getDvctype());
		}
		String scanNodeId=null;
		if (! GlobalUtil.isNullOrEmpty(simDatasources)) {
			scanNodeId=simDatasources.get(0).getAuditorNodeId();
		}else {
			scanNodeId="";
		}
		List<String>mstrptidAndNodeTypeList=new ArrayList<String>();
		setMstIdAndScanNodeType(dvcTypes,mstrptidAndNodeTypeList);
		int evtRptsize=0;
		if (!GlobalUtil.isNullOrEmpty(mstrptidAndNodeTypeList)) {
			List<Map<String,Object>> subResultTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt((mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[0], StringUtil.toInt(bean.getTalTop(),5))});
			Map<Integer,Integer> rowColumnsTeMap = ReportModel.getRowColumns(subResultTemp) ;
			evtRptsize=subResultTemp.size();
			if (!GlobalUtil.isNullOrEmpty(subResultTemp)) {
				for (Map map2 : subResultTemp) {
					map2.put("subject", (mstrptidAndNodeTypeList.get(0).split("IDandNODEtype"))[1]);
				}
				subResult.addAll(subResultTemp);
				rowColumns.putAll(rowColumnsTeMap);
			}
			
			int len=mstrptidAndNodeTypeList.size();
			for (int i=1;i<len;i++) {
				String mstrptidAndNodeType=mstrptidAndNodeTypeList.get(i);
				String string=mstrptidAndNodeType.split("IDandNODEtype")[0];
				List<Map<String,Object>> subTemp = rptMasterTbImp.queryTmpList(ReportUiConfig.MstSubSql, new Object[]{StringUtil.toInt(string, StringUtil.toInt(bean.getTalTop(),5))});
				if (!GlobalUtil.isNullOrEmpty(subTemp)) {
					int maxCol=0;
					if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
						maxCol=getMaxOrMinKey(rowColumns, 1);
					}
					for (Map map2 : subTemp) {
						Integer row = (Integer) map2.get("subRow")+maxCol;
						map2.put("subRow", row);
						map2.put("subject", mstrptidAndNodeType.split("IDandNODEtype")[1]);
					}
					subResult.addAll(subTemp);
					Map<Integer,Integer> rowColTemp = ReportModel.getRowColumns(subTemp) ;
					rowColumns.putAll(rowColTemp);
				}
			}
		}
		String reportType=request.getParameter("reportType");
		StringBuffer layout = new StringBuffer();
		Map<String,Object> params = new HashMap<String,Object>();
		if (!GlobalUtil.isNullOrEmpty(reportType)) {
			params.put("reportType", reportType);
		}
		params.put("talTop", bean.getTalTop());
		params.put("eTime", bean.getTalEndTime());
		String sUrl = null;
		int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"),1280) - 25- 200 ;
		
		StringBuffer subUrl = new StringBuffer();
		Map layoutValue = new HashMap() ;
		
		setImfo(subResult, params, bean, layout, subUrl, layoutValue, scanNodeId, rowColumns, sUrl, screenWidth);
		
		if(!GlobalUtil.isNullOrEmpty(subResult)&&subResult.size()>0){
			if (!GlobalUtil.isNullOrEmpty(subResult.get(0).get("mstName"))) {
				request.setAttribute("title", subResult.get(0).get("mstName"));
				params.put("dvcType", subResult.get(0).get("subject"));
			}
	    }
		String htmlLayout = ReportModel.createMstTable(layout.toString(),layoutValue);
		String[] nodeIds={scanNodeId};
		params.put("nodeIds", nodeIds);
		StringBuffer sb = getExportUrl(request, params,talCategory,true);
		request.setAttribute("expUrl", sb.toString());
		request.setAttribute("layout", htmlLayout);
		request.setAttribute("bean", bean);
		
		if ("baseReport".equals(reportType)) {
			return "/page/report/base_report_detail";
		}else {
			
		}
		return "/page/report/base_report_detail";
	}
	
	/**
	 * 小主题查询
	 */
	public Object getSubTitle(SID sid,HttpServletRequest request,HttpServletResponse response){
		try {SID.setCurrentUser(sid) ;
			String subId = request.getParameter(ReportUiConfig.subrptid);
			String sTime = request.getParameter(ReportUiConfig.sTime);
			String eTime = request.getParameter(ReportUiConfig.eTime);
			String nodeType=request.getParameter("nodeType");
			String chartTableId=request.getParameter("chartTableId");
			RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
			List<Map> subList = rptMasterTbImp.queryTmpList(ReportUiConfig.SubTitleSql, new Object[]{StringUtil.toInt(subId, 0)});
			JSONObject json = null;
			if(subList.size()>0){
				Map subMap = (Map) subList.get(0);
				Map<String,Object> rsMap =null;
				Map params = new HashMap();
				if(dataSourceService == null){
					dataSourceService = (DataSourceService)FacadeUtil.getFacadeBean(request, null, "dataSourceService");
				}
				if (!GlobalUtil.isNullOrEmpty(nodeType)) {
					SID.setCurrentUser(sid) ;
					params.put("nodeType", nodeType);
				}
				String dvctype =request.getParameter("dvctype");
				String realdvctype=dvctype.replace("Comprehensive", "");
				
				List<String>deviceTypes = ReportModel.getDeviceTypeList(dataSourceService,SID.currentUser());
				List<String>deviceIps = ReportModel.getDeviceIpList(dataSourceService,SID.currentUser());
				if (!GlobalUtil.isNullOrEmpty(deviceTypes) && deviceTypes.contains(realdvctype)) {
					deviceTypes.clear();
					deviceTypes.add(realdvctype);
					List<SimDatasource>realSimDatasources=dataSourceService.getDataSourceByDvcType(realdvctype);
					List<String> nowIPList=new ArrayList<String>();
					for (SimDatasource simDatasource : realSimDatasources) {
						if (deviceIps.contains(simDatasource.getDeviceIp())) {
							nowIPList.add(simDatasource.getDeviceIp());
						}
					}
					deviceIps=null;
					deviceIps=nowIPList;
				}
				rsMap = ReportModel.getSubTitleData(rptMasterTbImp,deviceTypes,deviceIps,subMap, sTime, eTime, subId, false,request);
				 
				Map<Object,Object> data = ReportModel.reformingResult(subMap, rsMap);
				
				params.put("dvcType", dvctype);
				params.put("talTop", request.getParameter("talTop"));
				params.put("sTime", sTime);
				params.put("eTime", eTime);
				
				String chartLink = StringUtil.toString(subMap.get("chartLink"),"0");
				int _chartLink = Integer.valueOf(chartLink);
				
				/************************************************/
				String logQueryUrl=null;
				if (!GlobalUtil.isNullOrEmpty(subMap.get("logQueryCondition"))) {
					logQueryUrl=getLogQueryUrl();
				}
				/************************************************/
				if (("Esm/Topsec/SystemRunLog".equals(dvctype)
						||"ComprehensiveEsm/Topsec/SystemRunLog".equals(dvctype)) && ! sid.isOperator()) {
					logQueryUrl=null;
				}
				String[] talCategory = null;
				String [] categoryValues = request.getParameterValues(ReportUiConfig.talCategory);
				if(categoryValues!=null){
					talCategory = new String [categoryValues.length];
					for(int i=0,len= categoryValues.length;i<len;i++){
						talCategory[i] =ChangePageEncode.IsoToUtf8(categoryValues[i]);
					}
				}
				String reportType=request.getParameter("reportType");
				if (!GlobalUtil.isNullOrEmpty(reportType)) {
					params.put("reportType", reportType);
				}
				StringBuffer url =getUrl(ReportUiConfig.reportEvtUrl,request, params,talCategory,true);;
				String surl = url.toString();
				if(_chartLink>0){
					url.append("&superiorId=").append(subList.get(0).get("mstId")).append("&").append(ReportUiConfig.mstrptid).append("=").append(chartLink).append("&drill=true");
				}
				data.put("url", _chartLink>0? url.toString():"");
				url.replace(0, url.length(), surl).replace(0, ReportUiConfig.reportEvtUrl.length(), ReportUiConfig.moreEvtUrl);
				url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"))
				.append("&").append(ReportUiConfig.subrptid).append("=").append(subId);
				data.put("moreUrl", url.toString());
				
				/**********************************************************************/
				if (!GlobalUtil.isNullOrEmpty(logQueryUrl)) {
					data.put("logQueryUrl", logQueryUrl);
					String nodeId=request.getParameter("nodeId");
					String onlyTable=request.getParameter("onlyTable");
					if (!GlobalUtil.isNullOrEmpty(nodeId)) {
						params.put("nodeId", nodeId);
					}
					if (!GlobalUtil.isNullOrEmpty(onlyTable)) {
						params.put("onlyTable", onlyTable);
					}
					data.put("frontEndParams", params);
				}
				/**********************************************************************/
				
				int type = StringUtil.toInt(StringUtil.toString(subMap.get("chartType"),"1"));
				Map<String,Object> rstMap = CreateChartFactory.getInstance().createChart(type, data);
				
				if(rstMap!=null){
					json = new JSONObject();
					if (null!=chartTableId) {
						json.put("chartTableId", chartTableId);
					}
					json.put("subID", subId);
					json.put("type",StringUtil.toString(rstMap.get("type"),""));
					json.put("chart", rstMap.get("chart"));
					json.put("table",rstMap.get("table"));
				}
			}
			return json;
		}finally{
			if (!GlobalUtil.isNullOrEmpty(SID.currentUser())) {
				SID.removeCurrentUser() ;
			}
		}
	}
	
	public String moreReport(SID sid,HttpServletRequest request,HttpServletResponse response){
		boolean fromRest  = false;
		if( request.getParameter("fromRest") != null){
			 fromRest =Boolean.parseBoolean( request.getParameter("fromRest"));
		}
		JSONObject jsonObj = new JSONObject();
		ReportBean bean = new ReportBean();
		String onlyByDvctype = request.getParameter("onlyByDvctype");
		bean = ReportUiUtil.tidyFormBean(bean, request);
		String [] talCategory = bean.getTalCategory();
		RptMasterTbService rptMasterTbImp = (RptMasterTbService) SpringContextServlet.springCtx.getBean(ReportUiConfig.MstBean);
		List<Map> subList = rptMasterTbImp.queryTmpList(ReportUiConfig.PaginationSql, new Object[]{StringUtil.toInt(bean.getSubrptid(), 0)});
		JSONObject obj= null;
		StringBuffer url = null;
		String surl ="";
		boolean flag = false;
		String nodeType=request.getParameter("nodeType");
		try {
			Map params = new HashMap();
			if (!GlobalUtil.isNullOrEmpty(nodeType) && !"null".equalsIgnoreCase(nodeType)) {
				SID.setCurrentUser(sid) ;
				params.put("nodeType", nodeType);
			}
			if(subList.size()>0){
				Map subMap = (Map) subList.get(0);
				String subTitle = subMap.get("subName").toString();
				String mstType = request.getParameter("msttype");
				subTitle = ReportUiUtil.viewRptName(subTitle, mstType);
				request.setAttribute("subName", subTitle);
				if(fromRest){
					jsonObj.put("subName", subTitle);
				}
				String paginationSql = null;
				if (!GlobalUtil.isNullOrEmpty(subMap)
						&& !GlobalUtil.isNullOrEmpty(subMap.get("paginationSql"))) {
					paginationSql = subMap.get("paginationSql").toString();
				}
				String deviceType = bean.getDvctype();

				if (!GlobalUtil.isNullOrEmpty(paginationSql)) {
					if (deviceType.equals(LogKeyInfo.LOG_SYSTEM_TYPE)
							|| deviceType.equals("Log/Global/Detail")
							|| deviceType.startsWith("Comprehensive")) {
						paginationSql = paginationSql.replace("**",
								ReportUiConfig.PageSqlRule2);
						paginationSql = paginationSql.replace("*alias*",
								ReportUiConfig.PageSqlRuleAlias2);
					} else {
						if (onlyByDvctype != null
								&& onlyByDvctype.equals("onlyByDvctype")) {
							paginationSql = paginationSql.replace("**",
									ReportUiConfig.PageSqlRule2);
							paginationSql = paginationSql.replace("*alias*",
									ReportUiConfig.PageSqlRuleAlias2);
						} else {
							paginationSql = paginationSql.replace("**",
									ReportUiConfig.PageSqlRule);
							paginationSql = paginationSql.replace("*alias*",
									ReportUiConfig.PageSqlRuleAlias);
						}
					}
					paginationSql = ReportModel.getTimeSql(paginationSql,
							bean.getTalStartTime(), bean.getTalEndTime());
				}
				// 显示出来的lable
				String paginationViewFiled = null;
				String paginationHtmFiled = null;
				// sql中map的字段
				String paginationSqlFiled = null;
				String tableSql = null;
				
				if (!GlobalUtil.isNullOrEmpty(subMap)) {
					if (!GlobalUtil.isNullOrEmpty(subMap.get("paginationViewFiled"))) {
						paginationViewFiled = subMap.get("paginationViewFiled").toString();
					}
					if (!GlobalUtil.isNullOrEmpty(subMap.get("paginationHtmFiled"))) {
						paginationHtmFiled = subMap.get("paginationHtmFiled").toString();
					}
					if (!GlobalUtil.isNullOrEmpty(subMap.get("paginationSqlFiled"))) {
						paginationSqlFiled = subMap.get("paginationSqlFiled").toString();
					}
				}
				List sqlParam = ReportUiUtil.getPaginationItem(request, paginationHtmFiled);// 前台数值
				
				sqlParam.add(bean.getTalStartTime());// 开始时间 倒数第3个参数
				sqlParam.add(bean.getTalEndTime());// 结束时间倒数第4个参数
				if (!bean.getDvcaddress().equals("")) {
					if (deviceType.equals(LogKeyInfo.LOG_SYSTEM_TYPE) || deviceType.equals("Log/Global/Detail")) {
					} else {
						if (onlyByDvctype != null && onlyByDvctype.equals("onlyByDvctype")) {
						} else {
							sqlParam.add(bean.getDvcaddress()); // dvcaddress 倒数第5个参数
						}
					}
				}
			
				if(!GlobalUtil.isNullOrEmpty(talCategory)){
					for(String str:talCategory){
						if (!GlobalUtil.isNullOrEmpty(str)) {
							sqlParam.add(str.substring(str.indexOf("***")+3));
						}
					}
				}
				/*String viewParam = ReportUiUtil.getViewPaginationItem(request, paginationHtmFiled);
				viewParam = viewParam.replace("'''", "\"'\"");
				request.setAttribute("viewParam", viewParam);*/

				String pageNum = "1";// 当前页数
				int pageSize = bean.getPagesize() == null ? 10 : Integer.parseInt(bean.getPagesize());
				String pagein = request.getParameter("pagein");
				String pageingo = request.getParameter("pageingo");
				String pageIndex = request.getParameter("pageIndex");

				if (ReportUiUtil.checkNull(pageIndex)) {
					pageNum = pageIndex;
				} else if (ReportUiUtil.checkNull(pagein) && ReportUiUtil.checkNull(pageingo)) {
					pageNum = pagein;
				}
			    String [] nodeId = request.getParameterValues("nodeId");
			    Map<String,Object> rsMap = null;
			    if (!GlobalUtil.isNullOrEmpty(paginationSql)) {
			    	List<String>deviceTypes=ReportModel.getDeviceTypeList(dataSourceService,SID.currentUser());
					List<String>deviceIps=ReportModel.getDeviceIpList(dataSourceService,SID.currentUser());
					rsMap = ReportModel.reformingSubTitleData(deviceTypes,deviceIps,paginationSql,sqlParam, subMap,  
		            		pageSize, nodeId,bean.getTalStartTime(),bean.getTalEndTime(),true, request);
			    	
				}else {
					String subId = request.getParameter(ReportUiConfig.subrptid);
					String sTime = bean.getTalStartTime();
					String eTime = bean.getTalEndTime();
					List<Map> subList1 = rptMasterTbImp.queryTmpList(ReportUiConfig.SubTitleSql, new Object[]{StringUtil.toInt(subId, 0)});
					JSONObject json = null;
					Map<String,Object> rsMap1=null;
					if(subList1.size()>0){
						Map subMap1 = (Map) subList1.get(0);
						List<String>deviceTypes=ReportModel.getDeviceTypeList(dataSourceService,SID.currentUser());
						List<String>deviceIps=ReportModel.getDeviceIpList(dataSourceService,SID.currentUser());
						rsMap1 = ReportModel.getSubTitleData(rptMasterTbImp,deviceTypes,deviceIps,subMap1, sTime, eTime, subId, false,request);
					}
					rsMap = rsMap1;
				}
				String viewParamItem = ReportModel.createSearchItem(paginationViewFiled, paginationHtmFiled);
				request.setAttribute("viewParamItem", viewParamItem);
				if(fromRest){
					jsonObj.put("viewParamItem", viewParamItem);
				}
				int sumPage= 0;
				if(rsMap!=null){
					sumPage = StringUtil.toInt(StringUtil.toString(rsMap.get("sumPage"), "0"));
				}
				// 总页数
				request.setAttribute("pages", ReportModel.getPages(sumPage, pageSize));
				request.setAttribute("sumdata", sumPage);
				// 当前页
				request.setAttribute("pageIndex", pageNum);
				if(fromRest){
					jsonObj.put("pages", ReportModel.getPages(sumPage, pageSize));
					jsonObj.put("sumdata", sumPage);
					// 当前页
					jsonObj.put("pageIndex", pageNum);
				}
				Map<Object,Object> data = ReportModel.reformingResult(subMap, rsMap);
			    int chartType  = StringUtil.toInt(subMap.get("chartType").toString(),0);       
			   if(chartType>0&&chartType<4){
				   flag = true;
			   }
				params.put("dvcType", request.getParameter("dvctype"));
				params.put("talTop", request.getParameter("talTop"));
				params.put("sTime", bean.getTalStartTime());
				params.put("eTime", bean.getTalEndTime());
				String chartLink = StringUtil.toString(subMap.get("chartLink"),"0");
				int _chartLink = Integer.valueOf(chartLink);
				String reportType = request.getParameter("reportType");
				if ("comprehensiveReportIndex".equals(reportType)) {
					params.put("reportType", reportType);
					url = getUrl("/sim/topoReport/comprehensiveInformReport?",request, params,talCategory,true);
				}else {
					url = getUrl(ReportUiConfig.reportEvtUrl,request, params,bean.getTalCategory(),true);
				}
				surl = url.toString();
				
				if(_chartLink>0){
					url.append("&superiorId=").append(subList.get(0).get("mstId")).append("&").append(ReportUiConfig.mstrptid).append("=").append(chartLink);
				}
				data.put("url", _chartLink>0? url.toString():"");
				url.replace(0, url.length(), surl).replace(0, ReportUiConfig.reportEvtUrl.length(), ReportUiConfig.moreEvtUrl);
				url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"))
				.append("&").append(ReportUiConfig.subrptid).append("=").append(bean.getSubrptid());
				data.put("moreUrl", url.toString());
				request.setAttribute("moreUrl", url.toString());
				if(fromRest){
					jsonObj.put("moreUrl", url.toString());
				}
				ChartTable table = CreateChartFactory.getInstance ().reformingChartTable(data);
				obj = moreRptAssembleData(table,StringUtil.toInt(pageNum),pageSize);
				url.replace(0, url.length(), surl);
				if (! "comprehensiveReportIndex".equals(reportType)) {
					url.append("&").append(ReportUiConfig.mstrptid).append("=").append(subList.get(0).get("mstId"));
				}
			}
			if(subList!=null){
				request.setAttribute("title",subList.get(0).get("subName"));
				if(fromRest){
					jsonObj.put("title", subList.get(0).get("subName"));
				}
			}
			List<SimDatasource> dslist = new ArrayList<SimDatasource>();
			
			SimDatasource dsource = new SimDatasource();
			if("onlyByDvctype".equals(onlyByDvctype)){
				dsource.setDeviceIp("全部");
				dsource.setNodeId("");
			}else{
				dsource.setDeviceIp(request.getParameter("dvcaddress"));
				dsource.setNodeId(request.getParameter("nodeId"));
			}
			dsource.setSecurityObjectType(bean.getDvctype());
			dslist.add(0, dsource);
			request.setAttribute("dslist", dslist);
			request.setAttribute("flag", flag);
			request.setAttribute("goUrl", url!=null?url.toString():"");
			request.setAttribute("tableOptions", obj);
			request.setAttribute("bean", bean);
			if(fromRest){
				jsonObj.put("dslist", dslist);
				jsonObj.put("flag", flag);
				jsonObj.put("goUrl", url!=null?url.toString():"");
				jsonObj.put("tableOptions", obj);
				jsonObj.put("bean", bean);
				return JSON.toJSONString(jsonObj);
			}
		} finally {
			if (!GlobalUtil.isNullOrEmpty(SID.currentUser())) {
				SID.removeCurrentUser() ;
			}
		}
	
		return "/page/report/more_report";
	}
	public Object showHasRoleAllowScanNodeType(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		List<String>deviceTypeList= ReportModel.getDeviceTypeList(dataSourceService,sid);
		if (GlobalUtil.isNullOrEmpty(deviceTypeList)) {
			return null;
		}
		List<NodeTypeShow>nodeTypeShows=nodeTypeService.findTypeShowsByUserName(sid.getUserName());
		removeShowsType(deviceTypeList,nodeTypeShows);
		List<StandardTree> treeList=new ArrayList<StandardTree>(deviceTypeList.size());
		for (String string : deviceTypeList) {
			String cnName=DeviceTypeNameUtil.getDeviceTypeName(string, Locale.getDefault());
			StandardTree treeResult=new StandardTree();
			treeResult.setId(string);
			treeResult.setText(cnName);
			if (!treeList.contains(treeResult)) {
				treeList.add(treeResult);
			}
		}
		
		//先只显示 资产事件报表 下面的子报表
		Object jsonObject=JSON.toJSON(treeList);
		return jsonObject;
	}
	
	public Object showAllHasRoleScanNodeType(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		List<String>deviceTypeList= ReportModel.getDeviceTypeList(dataSourceService,sid);
		if (GlobalUtil.isNullOrEmpty(deviceTypeList)) {
			return null;
		}
		List<StandardTree> treeList=new ArrayList<StandardTree>(deviceTypeList.size());
		for (String string : deviceTypeList) {
			String cnName=DeviceTypeNameUtil.getDeviceTypeName(string, Locale.getDefault());
			StandardTree treeResult=new StandardTree();
			treeResult.setId(string);
			treeResult.setText(cnName);
			if (!treeList.contains(treeResult)) {
				treeList.add(treeResult);
			}
		}
		
		//先只显示 资产事件报表 下面的子报表
		Object jsonObject=JSON.toJSON(treeList);
		return jsonObject;
	}
	
	public Object showAllRoleNodeTypeShow(SID sid,HttpServletRequest request,HttpServletResponse response) throws Exception{
		if (sid==null || GlobalUtil.isNullOrEmpty(sid.getUserName())) {
			return null;
		}
		List<NodeTypeShow>nodeTypeShows=nodeTypeService.findTypeShowsByUserName(sid.getUserName());
		Object jsonObject=JSON.toJSON(nodeTypeShows);
		return jsonObject;
	}
	
	public Object saveOrUpdateNodeTypeShow(SID sid,@ModelAttribute("nodeTypeShow")NodeTypeShow nodeTypeShow,HttpServletRequest request,HttpServletResponse response) throws Exception{
		if (sid==null || GlobalUtil.isNullOrEmpty(sid.getUserName())) {
			return null;
		}
		NodeTypeShow nodeTypeShow2=nodeTypeService.findByUserNameAndNodeType(sid.getUserName(), nodeTypeShow.getNodeType());
		if (GlobalUtil.isNullOrEmpty(nodeTypeShow.getId())
				&& GlobalUtil.isNullOrEmpty(nodeTypeShow2)) {
			initNodeTypeShow(nodeTypeShow, nodeTypeShow.getNodeType(), sid);
			nodeTypeService.saveNodeTypeShow(nodeTypeShow);
		}
		return true;
	}
	
	public Object updateNodeTypeShows(SID sid,@RequestParam("idS") String idS,@RequestParam("isShow") String isShow,HttpServletRequest request,HttpServletResponse response) throws Exception{
		if (sid==null || GlobalUtil.isNullOrEmpty(sid.getUserName())) {
			return null;
		}
		String[]ids=idS.split(",");
		if (GlobalUtil.isNullOrEmpty(ids)) {
			return null;
		}
		for (String string : ids) {
			if (GlobalUtil.isNullOrEmpty(string)) {
				continue;
			}
			Integer id=Integer.valueOf(string);
			NodeTypeShow nodeTypeShow=nodeTypeService.findNodeTypeShowById(id);
			nodeTypeShow.setIsShow(Boolean.valueOf(isShow));
			nodeTypeService.updateNodeTypeShow(nodeTypeShow);
//			nodeTypeService.updateNodeTypeShowProperty(nodeTypeShow, "isShow", isShow);
		}
		
		return true;
	}
	
	public Object showLogTypeFieldsObject(SID sid){
//		List<String> list=ReportConfStruct.getFiledNameList(ReportConfStruct.WHOLE_PATH+"IpsUrlHour");
		return null;
	}
	
	private void setImfo(List<Map<String,Object>> subResult,Map<String,Object> params,ReportBean bean,StringBuffer layout,
			StringBuffer subUrl,Map layoutValue,String scanNodeId,Map<Integer,Integer> rowColumns,String sUrl,int screenWidth){
		
		for(int i=0,len =subResult.size();i<len;i++ ){
			params.remove("sTime");
			Map subMap = subResult.get(i);
			if(i==0){
				bean.setViewItem(StringUtil.toString(subMap.get("viewItem"), ""));
			}
			Integer row = (Integer) subMap.get("subRow");
			layout.append(row + ":" + subMap.get("subColumn")+ ",");
			if (GlobalUtil.isNullOrEmpty(subMap)) {
				continue;
			}
			params.put("sTime", bean.getTalStartTime());
			params.put("dvcType", subMap.get("subject"));
			sUrl=getComprehensiveUrl(ReportUiConfig.subEvtUrl,scanNodeId,params,bean.getTalCategory()).toString();
			params.remove("dvcType");
			subUrl.replace(0, subUrl.length(),sUrl);
			subUrl.append("&").append(ReportUiConfig.subrptid).append("=").append(subMap.get("subId"));
			subUrl.substring(0, subUrl.length());
			int column = rowColumns.get(row);
			String width = String.valueOf((screenWidth-10*column)/column) ;
			String _column = subMap.get("subColumn").toString();
			layoutValue.put(row+_column, ReportUiUtil.createSubTitle(subMap, width,subUrl.toString(),bean.getTalCategory(),StringUtil.toInt(bean.getTalTop(), 5)));
		}
	}
	
	private List<String> setDvcTypes(SID sid,List<SimDatasource>simDatasources,ReportBean bean,List<String>dvcTypes){
		if (!GlobalUtil.isNullOrEmpty(bean.getDvctype())
				&& "DynamicComprehensiveReport".equals(bean.getDvctype())) {
			Set<AuthUserDevice> devices = sid.getUserDevice() == null ? Collections.<AuthUserDevice>emptySet() : sid.getUserDevice();
			if (sid.isOperator()) {
				dvcTypes = dataSourceService.getDistinctDvcType(DataSourceService.CMD_ALL);
			}else{
				dvcTypes=new ArrayList<String>();
				BeanToPropertyValueTransformer trans = new BeanToPropertyValueTransformer("ip") ;
				Collection<String> userDeviceIPs = (Collection<String>) CollectionUtils.collect(devices,trans) ;
				for (SimDatasource simDatasource : simDatasources) {
					Device device = AssetFacade.getInstance().getAssetByIp(simDatasource.getDeviceIp()) ;
					if (device != null && userDeviceIPs.contains(simDatasource.getDeviceIp())) {
						if(!dvcTypes.contains(simDatasource.getSecurityObjectType())){
							dvcTypes.add(simDatasource.getSecurityObjectType());
						}
					}
				}
			}
		}else {
			dvcTypes=new ArrayList<String>();
		}
		if (!sid.hasAuditorRole() && dvcTypes.contains(LogKeyInfo.LOG_SYSTEM_TYPE)) {
			dvcTypes.remove(LogKeyInfo.LOG_SYSTEM_TYPE);
		}
		return dvcTypes;
	}
	private void setMstIdAndScanNodeType(List<String>dvcTypes,List<String>mstrptidAndNodeTypeList){
		if (!GlobalUtil.isNullOrEmpty(dvcTypes)) {
			
			for (String dvcType : dvcTypes) {
				List<Map> rptList = reportService.getRptMaster("Comprehensive"+dvcType);
				if (!GlobalUtil.isNullOrEmpty(rptList)) {
					for (Map map : rptList) {
						if (map.get("id")!=null) {
							String string=StringUtil.toString(map.get("id"));
							if (! mstrptidAndNodeTypeList.contains(string)) {
								mstrptidAndNodeTypeList.add(string+"IDandNODEtypeComprehensive"+dvcType);
							}
						}
					}
				}
			}
			
		}
	}
	private StringBuffer getUrl(String prefix,HttpServletRequest request,Map params,String[] talCategory,boolean isevt){
		StringBuffer sb = getConditonUrl(prefix, request, params,isevt);
		if(talCategory!=null&&talCategory.length>0){
			for(String str:talCategory){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(str);
			}
		}
		return sb;
	}
	private String getLogQueryUrl(){
		return ReportUiConfig.reportQueryLogUrl;
	}
	private StringBuffer getComprehensiveUrl(String prefix,String nodeId,Map params,String[] talCategory){
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append(ReportUiConfig.dvctype).append("=").append(params.get("dvcType"));
		if(!GlobalUtil.isNullOrEmpty(nodeId)){
			sb.append("&nodeId=").append(nodeId);
		}
		
		sb.append("&").append(ReportUiConfig.TalTop).append("=").append(params.get("talTop"))
		.append("&").append(ReportUiConfig.sTime).append("=").append(params.get("sTime"))
		.append("&").append(ReportUiConfig.eTime).append("=").append(params.get("eTime"));
		if (params.containsKey("onlyTable")) {
			sb.append("&onlyTable=").append(params.get("onlyTable"));
		}
		if(params.containsKey("reportType") && !GlobalUtil.isNullOrEmpty(params.get("reportType"))){
			sb.append("&reportType").append("=").append(params.get("reportType"));
		}
		if(talCategory!=null&&talCategory.length>0){
			for(String str:talCategory){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(str);
			}
		}
		return sb;
	}
	private StringBuffer getNoConditionUrl(String prefix,HttpServletRequest request,Map params,boolean isevt){
    	String type = request.getParameter("type");
		String[] nodeIds = request.getParameterValues("nodeId");
		if (GlobalUtil.isNullOrEmpty(nodeIds) && !GlobalUtil.isNullOrEmpty(params.get("nodeIds"))) {
			nodeIds=(String[])params.get("nodeIds");
		}
		String dvcaddress = request.getParameter(ReportUiConfig.dvcaddress);
		String reportType = request.getParameter("reportType");
		StringBuffer sb = new StringBuffer();
		if (isevt) {
			sb.append(prefix).append(ReportUiConfig.dvctype).append("=").append(params.get("dvcType"));
		}else if ("comprehensiveReportIndex".equals(reportType)) {
			sb.append(prefix).append(ReportUiConfig.dvctype).append("=").append("DynamicComprehensiveReport");
		}else {
			sb.append(prefix).append(ReportUiConfig.dvctype).append("=").append(params.get("nodeType"));
		}
		
		if(StringUtil.isNotBlank(type)){
			sb.append("&type").append("=").append(type);
		}
		if(!GlobalUtil.isNullOrEmpty(nodeIds)){
			for(String str:nodeIds){
				sb.append("&nodeId=").append(str);
			}
		}
		if(StringUtil.isNotBlank(dvcaddress)){
			sb.append("&").append(ReportUiConfig.dvcaddress).append("=").append(dvcaddress);
		}
		if(StringUtil.isNotBlank(reportType)){
			sb.append("&reportType").append("=").append(reportType);
		}
		return sb;
    }
	
    private StringBuffer getExportUrl(HttpServletRequest request,Map params,String[] talCategory,boolean isevt){
    	StringBuffer sb = getNoConditionUrl(ReportUiConfig.expUrl, request, params,isevt);
    	if(talCategory!=null&&talCategory.length>0){
			for(String str:talCategory){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(str);
			}
		}
		return sb;
    }
    
	private StringBuffer getConditonUrl(String prefix,HttpServletRequest request,Map params,boolean isevt){
		StringBuffer sb = getNoConditionUrl(prefix, request, params,isevt);
		sb.append("&").append(ReportUiConfig.TalTop).append("=").append(params.get("talTop"))
		.append("&").append(ReportUiConfig.sTime).append("=").append(params.get("sTime"))
		.append("&").append(ReportUiConfig.eTime).append("=").append(params.get("eTime"))
		.append("&").append(ReportUiConfig.rootId).append("=").append(params.get("rootId"))
		.append("&").append(ReportUiConfig.assGroupNodeId).append("=").append(params.get("assGroupNodeId"))
		.append("&").append(ReportUiConfig.topoId).append("=").append(params.get("topoId"))
		.append("&").append(ReportUiConfig.nodeLevel).append("=").append(params.get("nodeLevel"))
		.append("&").append(ReportUiConfig.nodeType).append("=").append(params.get("nodeType"));
		return sb;
	}
	private StringBuffer getSuperiorUrl(HttpServletRequest request,Map params,String[] talCategory,boolean isevt){
		String sid = request.getParameter("superiorId");
		if(sid==null){
			List<Map> list =reportService.getSuperiorId(params.get("mstId").toString());
			if(list!=null&&list.size()>0){
				Map map = list.get(0);
				sid = StringUtil.toString(map.get("mstId"), "0");
			}
		}
		int len = talCategory.length;
		String prefix =ReportUiConfig.reportUrl;
		StringBuffer sb = getConditonUrl(prefix, request, params,isevt);
		sb.append("&").append(ReportUiConfig.mstrptid).append("=").append(sid);
		if(len>1){
			for(int i=0;i<len-1;i++){
				sb.append("&").append(ReportUiConfig.talCategory).append("=").append(talCategory[i]);
			}
		}
		return sb;
	}
	private Integer getMaxOrMinKey(Map<Integer,Integer> rowColumns,int status){
		if (GlobalUtil.isNullOrEmpty(rowColumns)) {
			if (status>=0)return Integer.MIN_VALUE;
			return Integer.MAX_VALUE;
		}
		Integer maxKey=Integer.MIN_VALUE;
		Integer minKey=Integer.MAX_VALUE;
		for (Map.Entry<Integer, Integer> entry : rowColumns.entrySet()) {
			Integer temp=entry.getKey();
			if (maxKey<temp) {
				maxKey=temp;
			}
			if (minKey>temp) {
				minKey =temp;
			}
		}
		Integer resultInteger=null;
		if (status>=0) {
			resultInteger=maxKey;
		}else {
			resultInteger=minKey;
		}
		return resultInteger;
	}
	
	private Map<Integer,Integer> newLocationMap(Map<Integer,Integer> rowColumns,int maxKey){
		Map<Integer,Integer> resultMap=null;
		if (!GlobalUtil.isNullOrEmpty(rowColumns)) {
			resultMap=new HashMap<Integer, Integer>();
			for (Map.Entry<Integer, Integer> entry : rowColumns.entrySet()) {
				Integer temp=entry.getKey()+maxKey;
				resultMap.put(temp, entry.getValue());
			}
		}
		return resultMap;
	}
	private Map<Integer,Integer> newLocationMap(Map<Integer,Integer> rowColumnNeed,Map<Integer,Integer> rowColumnTemp){
		Integer maxKey=null;
		if (GlobalUtil.isNullOrEmpty(rowColumnNeed)) {
			rowColumnNeed=new HashMap<Integer, Integer>();
			maxKey=0;
		}else {
			maxKey=getMaxOrMinKey(rowColumnNeed, 1);
		}
		if (!GlobalUtil.isNullOrEmpty(rowColumnTemp)) {
			for (Map.Entry<Integer, Integer> entry : rowColumnTemp.entrySet()) {
				Integer temp=entry.getKey()+maxKey;
				rowColumnNeed.put(temp, entry.getValue());
			}
		}
		return rowColumnNeed;
	}
	
	private boolean keyIsStartString(Map<Object,Object> map,String string){
		if (GlobalUtil.isNullOrEmpty(map) || GlobalUtil.isNullOrEmpty(string)) {
			return false;
		}
		for (Map.Entry<Object,Object> entry : map.entrySet()) {
			Object keyObject=entry.getKey();
			if (!GlobalUtil.isNullOrEmpty(keyObject) && string.startsWith(keyObject.toString())) {
				return true;
			}
		}
		return false;
	}
	private String getStartStringKey(Map<Object,Object> map,String string){
		if (GlobalUtil.isNullOrEmpty(map) || GlobalUtil.isNullOrEmpty(string)) {
			return "";
		}
		for (Map.Entry<Object,Object> entry : map.entrySet()) {
			Object keyObject=entry.getKey();
			if (!GlobalUtil.isNullOrEmpty(keyObject) && string.startsWith(keyObject.toString())) {
				return keyObject.toString();
			}
		}
		return "";
	}
	
	private JSONObject moreRptAssembleData(ChartTable table,int pageIndex,int pageSize){
		JSONObject jsonTable = new JSONObject() ;
		String[] fields = table.getFields();
		String[] header = table.getHeader();
		List<JSONObject> columns = new ArrayList<JSONObject>();
		for(int i=0,len=fields.length;i<len;i++){
			JSONObject columnJSON = new JSONObject() ;
			columnJSON.put("field", fields[i]) ;
			columnJSON.put("title", header[i]) ;
			columnJSON.put("width", 200) ;
			columns.add(columnJSON) ;
		}
		jsonTable.put("columns", columns);
		List<JSONObject> data = new ArrayList<JSONObject>();
		List<ColumnData> list = table.getBodyList(); 
		for(ColumnData cdata:list){
			JSONObject dataJSON = (JSONObject) JSON.toJSON(cdata.getData()) ;
			data.add(dataJSON);
		}
		jsonTable.put("data", data);
		return jsonTable;
	}
	
	private void initEditNodeType(IsEditNodeType isEditNodeType,SID sid){
		if (sid!=null) {
			isEditNodeType.setUserId(sid.getAccountID()+"");
			isEditNodeType.setUserName(sid.getUserName());
			isEditNodeType.setIsEdit(false);
			isEditNodeType.setCreateTime(new Date());
			isEditNodeType.setAttr("null");
		}
	}
	
	private void initNodeTypeShow(NodeTypeShow nodeTypeShow,String deviceType,SID sid){
		if (sid!=null) {
			nodeTypeShow.setUserId(sid.getAccountID()+"");
			nodeTypeShow.setUserName(sid.getUserName());
			nodeTypeShow.setIsShow(true);
			nodeTypeShow.setCreateTime(new Date());
			nodeTypeShow.setNodeType(deviceType);
			nodeTypeShow.setShowName(ReportUiUtil.getDeviceTypeName(deviceType, Locale.getDefault()));
			nodeTypeShow.setIp("null");
			nodeTypeShow.setAttr("null");
		}
	}
	private void removeShowsType(List<String>deviceTypeList,List<NodeTypeShow>nodeTypeShows){
		if (GlobalUtil.isNullOrEmpty(deviceTypeList)) {
			return;
		}
		List<String>removeList=null;
		List<NodeTypeShow>removeTypeShowList=null;
		if (!GlobalUtil.isNullOrEmpty(nodeTypeShows)) {
			removeList=new ArrayList<String>(nodeTypeShows.size());
			removeTypeShowList=new ArrayList<NodeTypeShow>(deviceTypeList.size());
			for (NodeTypeShow nodeTypeShow : nodeTypeShows) {
				if (deviceTypeList.contains(nodeTypeShow.getNodeType())){
					if(!removeList.contains(nodeTypeShow.getNodeType())){
						removeList.add(nodeTypeShow.getNodeType());
					}
					if (!removeTypeShowList.contains(nodeTypeShow.getNodeType())) {
						removeTypeShowList.add(nodeTypeShow);
					}
				}
			}
		}
		if (!GlobalUtil.isNullOrEmpty(removeList)) {
			deviceTypeList.removeAll(removeList);
		}
		if (!GlobalUtil.isNullOrEmpty(removeTypeShowList)) {
			nodeTypeShows.removeAll(removeTypeShowList);
		}
	}
	
	private void saveAndUpDateStatus(List<String>dvcTypes,SID sid,IsEditNodeType isEditNodeType){
		if (!GlobalUtil.isNullOrEmpty(dvcTypes)) {
			boolean isedit=false;
			for (String string : dvcTypes) {
				NodeTypeShow nodeTypeShow=new NodeTypeShow();
				initNodeTypeShow(nodeTypeShow,string,sid);
				nodeTypeService.saveNodeTypeShow(nodeTypeShow);
				if(!isedit)isedit=true;
			}
			if (isedit && ! isEditNodeType.getIsEdit()) {
				isEditNodeType.setIsEdit(true);
				nodeTypeService.updateEditNodeType(isEditNodeType);
			}
		}
	}
}
