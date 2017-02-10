package com.topsec.tsm.sim.report.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.bean.ChartParam;
import com.topsec.tsm.sim.report.bean.struct.BaseStruct;
import com.topsec.tsm.sim.report.chart.highchart.HChartFactory;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartConstant;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/*
 *	 ReportTalChart	适配器杨圣风JfreeChart模块
 */
public class ReportTalChart {
	private Logger _log = Logger.getLogger(ReportTalChart.class);

	/**
	 * 构造Jreechart对象
	 * 
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param HttpServletResponse
	 *            response HttpServletResponse *
	 * @param ArrayList
	 *            <ChartData> al ChartData结构体
	 * @param Map
	 *            subMap子报表信息
	 * @return Object JfreeChart[exp]或者String[browse]
	 * @throws Exception
	 */
	public Object creChart(HttpServletRequest request,
			HttpServletResponse response, ArrayList<ChartData> al, Map subMap,List ruleResult)
			throws Exception {

		ChartRender _render = new ChartRender();
		_render.setHight(ReportUiConfig.PicHight);
		_render.setWidth(ReportUiConfig.PicWidth);
		if(request!=null){
			int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"), 0);
			if(screenWidth>0){
				_render.setWidth(ReportUiUtil.calculateClientPictureWidth(screenWidth, subMap, request));
			}
		}
		// 是不是3d柱
		_render.setC3D(true);

		// 类型 LineChart BarChart
		_render.setType(ReportUiConfig.GraphType.get(subMap.get("chartType")));

		// x轴线下显示内容
		ChartAxisLabelRender crX = new ChartAxisLabelRender();
		// crX.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crX.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crX.setFont(ChartConstant.Default_Font);
		}
		crX.setName(subMap.get("subName").toString());
		// 不显示xy轴
		crX.setShow(false);
		_render.setCategoryAxisLabel(crX);

		// y轴线后显示内容
		ChartAxisLabelRender crY = new ChartAxisLabelRender();
		// crY.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crY.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crY.setFont(ChartConstant.Default_Font);
		}
		crY.setName(subMap.get("subName").toString());

		// 不显示xy轴

		crY.setShow(false);

		_render.setValueAxisLabel(crY);

		// 是否显示图例
		ChartLegendRender clr = new ChartLegendRender();
		if (response != null){
			clr.setShow(false);
		}
		else{
			clr.setShow(true);
		}
		// clr.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			clr.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			clr.setFont(ChartConstant.Default_Font);
		}
		_render.setLenged(clr);

		// 设置详细图表的显示细节部分的背景颜色
		// 设置是否显示垂直网格线
		// 设置是否显示水平网格线
		// 设置垂直网格线颜色
		// 设置水平网格线颜色
		ChartPlotRender cplot = new ChartPlotRender();
		cplot.setDomainGridlinesVisible(false);
		cplot.setRangeGridlinesVisible(true);
		_render.setPlot(cplot);

		// title
		ChartTitleRender chartTitle = new ChartTitleRender();

		// title必须有颜色
		chartTitle.setBgColor(_render.getBackgroundColor1());

		// title必须有字体
		if (response == null){
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font_Ex);
		}
		else{
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font);
		}

		String unit = subMap.get("unit") == null ? "" : subMap.get("unit").toString();
		if (response != null){
			chartTitle.setTitle(unit);
		}
		else {
			String subTitle = subMap.get("subName").toString() + unit;
			String subrptType = subMap.get("mstType") + "";
			subTitle = ReportUiUtil.viewRptName(subTitle, subrptType);
			chartTitle.setTitle(subTitle);
		}
		// 不显示title
		chartTitle.setShow(true);
		_render.setTitle(chartTitle);
		_render.setTooltips(true);
		_render.setPlot(new ChartPlotRender());

		String exporthtml = null;	
		if (request != null){
			exporthtml = request.getParameter("exporthtml");	
		}
		if ((exporthtml==null) && subMap.get("chartLink") != null && request != null) {
			BaseStruct bs = ReportUiUtil.getBaseS(request);
			bs.setMstrptid(subMap.get("chartLink").toString());
			bs.setActionname(ReportUiConfig.createReport);
			String tableUrl = ReportUiUtil.getUrl(bs);
			
			tableUrl=ReportUiUtil.convertMstType2URL(tableUrl, subMap, ruleResult);
			
			_render.setChartUrl(tableUrl);
		}

		Object str = MChartFactory.getInstance().createChart(al, _render,request, response);
		return str;
	}
   
	public Object creChart(List<Map> data, Map subMap,HttpServletRequest request,HttpServletResponse response)throws Exception {
		ChartRender _render = new ChartRender();
		_render.setHight(ReportUiConfig.PicHight);
		_render.setWidth(ReportUiConfig.PicWidth);
		if(request!=null){
			int screenWidth = StringUtil.toInt(request.getParameter("screenWidth"), 0);
			if(screenWidth>0){
				_render.setWidth(ReportUiUtil.calculateClientPictureWidth(screenWidth, subMap, request));
			}
		}
		// 是不是3d柱
		_render.setC3D(true);

		// 类型 LineChart BarChart
		_render.setType(ReportUiConfig.GraphType.get(subMap.get("chartType")));

		// x轴线下显示内容
		ChartAxisLabelRender crX = new ChartAxisLabelRender();
		// crX.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crX.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crX.setFont(ChartConstant.Default_Font);
		}
		crX.setName(subMap.get("subName").toString());
		// 不显示xy轴
		crX.setShow(false);
		_render.setCategoryAxisLabel(crX);

		// y轴线后显示内容
		ChartAxisLabelRender crY = new ChartAxisLabelRender();
		// crY.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crY.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crY.setFont(ChartConstant.Default_Font);
		}
		crY.setName(subMap.get("subName").toString());

		// 不显示xy轴

		crY.setShow(false);

		_render.setValueAxisLabel(crY);

		// 是否显示图例
		ChartLegendRender clr = new ChartLegendRender();
		if (response != null){
			clr.setShow(false);
		}
		else{
			clr.setShow(true);
		}
		// clr.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			clr.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			clr.setFont(ChartConstant.Default_Font);
		}
		_render.setLenged(clr);

		// 设置详细图表的显示细节部分的背景颜色
		// 设置是否显示垂直网格线
		// 设置是否显示水平网格线
		// 设置垂直网格线颜色
		// 设置水平网格线颜色
		ChartPlotRender cplot = new ChartPlotRender();
		cplot.setDomainGridlinesVisible(false);
		cplot.setRangeGridlinesVisible(true);
		_render.setPlot(cplot);

		// title
		ChartTitleRender chartTitle = new ChartTitleRender();

		// title必须有颜色
		chartTitle.setBgColor(_render.getBackgroundColor1());

		// title必须有字体
		if (response == null){
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font_Ex);
		}
		else{
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font);
		}

		String unit = subMap.get("unit") == null ? "" : subMap.get("unit").toString();
		if (response != null){
			chartTitle.setTitle(unit);
		}
		else {
			String subTitle = subMap.get("subName").toString() + unit;
			String subrptType = subMap.get("mstType") + "";
			subTitle = ReportUiUtil.viewRptName(subTitle, subrptType);
			chartTitle.setTitle(subTitle);
		}
		// 不显示title
		chartTitle.setShow(true);
		_render.setTitle(chartTitle);
		_render.setTooltips(true);
		_render.setPlot(new ChartPlotRender());


		Object chart = HChartFactory.getInstance().createChart(data, subMap, request, response);
		return chart;
	}
	
	
	public Object creChartBrowse(HttpServletRequest request,
			HttpServletResponse response, ArrayList<ChartData> al,ChartParam chartparam,List ruleResult)
			throws Exception {

		ChartRender _render = new ChartRender();
		_render.setHight(ReportUiConfig.PicHight);
		_render.setWidth(ReportUiConfig.PicWidth);
		// 是不是3d柱
		_render.setC3D(true);

		// 类型 LineChart BarChart
		_render.setType(ReportUiConfig.GraphType.get(chartparam.getCharttype()));

		// x轴线下显示内容

		ChartAxisLabelRender crX = new ChartAxisLabelRender();
		// crX.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crX.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crX.setFont(ChartConstant.Default_Font);
		}
		crX.setName(chartparam.getSubname().toString());
		// 不显示xy轴
		crX.setShow(false);
		_render.setCategoryAxisLabel(crX);

		// y轴线后显示内容
		ChartAxisLabelRender crY = new ChartAxisLabelRender();
		// crY.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			crY.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			crY.setFont(ChartConstant.Default_Font);
		}
		crY.setName(chartparam.getSubname().toString());

		// 不显示xy轴

		crY.setShow(false);

		_render.setValueAxisLabel(crY);

		// 是否显示图例
		ChartLegendRender clr = new ChartLegendRender();
		if (response != null){
			clr.setShow(false);
		}
		else{
			clr.setShow(true);
		}
		// clr.setFont(new Font("宋体", Font.PLAIN, 10));
		if (response == null){
			clr.setFont(ChartConstant.Default_Font_Ex);
		}
		else{
			clr.setFont(ChartConstant.Default_Font);
		}
		_render.setLenged(clr);

		// 设置详细图表的显示细节部分的背景颜色
		// 设置是否显示垂直网格线
		// 设置是否显示水平网格线
		// 设置垂直网格线颜色
		// 设置水平网格线颜色
		ChartPlotRender cplot = new ChartPlotRender();
		cplot.setDomainGridlinesVisible(false);
		cplot.setRangeGridlinesVisible(true);
		_render.setPlot(cplot);

		// title
		ChartTitleRender chartTitle = new ChartTitleRender();

		// title必须有颜色
		chartTitle.setBgColor(_render.getBackgroundColor1());

		// title必须有字体
		if (response == null){
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font_Ex);
		}
		else{
			chartTitle.setFont(ChartConstant.TITLE_UNIT_Font);
		}

		String unit = chartparam.getUnit() == null ? "单位(次)" : chartparam.getUnit()
				.toString();

		if (response != null){
			chartTitle.setTitle(unit);
		}
		else {
			String subTitle = chartparam.getSubname().toString() + unit;
			String subrptType = chartparam.getMsttype() + "";
			subTitle = ReportUiUtil.viewRptName(subTitle, subrptType);
			chartTitle.setTitle(subTitle);
		}
		// 不显示title
		chartTitle.setShow(true);
		_render.setTitle(chartTitle);
		_render.setTooltips(true);
		_render.setPlot(new ChartPlotRender());

		Object str = MChartFactory.getInstance().createChart(al, _render,
				request, response);
		return str;
	}

}
