package com.topsec.tsm.sim.report.chart.jfreechart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.Align;
import org.jfree.ui.TextAnchor;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.CallableCallback;
import com.topsec.tsm.sim.report.bean.struct.ExpMstRpt;
import com.topsec.tsm.sim.report.component.ChartImageCreator;
import com.topsec.tsm.sim.report.model.ChartData;
import com.topsec.tsm.sim.report.util.ChartCategoryFormatter;
import com.topsec.tsm.sim.report.util.ReportUiUtil;

/**
 * JFreeChart图片生成
 * @author hp
 *
 */
public class JFreeChartImageCreator extends ChartImageCreator {

	/**
	 * 默认的图表颜色,此项中的颜色与前台页面中FusionChart默认的颜色对应
	 */
	
	private static final String[] defaultColor = new String[] {"#AFD8F8","#F6BD0F","#8BBA00","#FF8E46","#008E8E","#D64646","#8E468E","#588526","#B3AA00","#4169E1"} ;
	private List<ChartData> chartData ;
	/**
	 * 分类坐标轴标题(一般指X轴)
	 */
	private String categoryAxisLabel ;
	/**
	 * 数值坐标轴标题(一般指Y轴)
	 */
	private String valueAxisLabel ;
	public JFreeChartImageCreator() {
		super();
	}
	public JFreeChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,List<ChartData> chartData) {
		this(chartType,imageType,chartWidth,chartHeight,chartData,null);
	}
	public JFreeChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,List<ChartData> chartData,CallableCallback<String> callback) {
		super(chartType,imageType,chartWidth,chartHeight,callback);
		this.chartData = chartData;
	}
	@Override
	public String generateChartImage() {
		StandardChartTheme standardChartTheme=new StandardChartTheme("CN");     
		standardChartTheme.setExtraLargeFont(ExpMstRpt.getDefaultFont(Font.BOLD, 20));   
		standardChartTheme.setRegularFont(new Font("微软雅黑", Font.PLAIN, 12));     
		standardChartTheme.setLargeFont(new Font("微软雅黑", Font.PLAIN, 15));     
		ChartFactory.setChartTheme(standardChartTheme); 
		JFreeChart chart = createChart() ;
		chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);//中文显示效果好一点
		String filePath = null ;
		try {
			if(ObjectUtils.equalsAny(imageType, "jpg","jpeg","gif","png")){//无论客户端传递何种图片格式都生成png格式图片
				File file = createTempImageFile("png") ;
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth+(int)(chartWidth*0.2), (int)(chartHeight+chartHeight*0.2)) ;
				filePath = file.getAbsolutePath() ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filePath ;
	}
	/**
	 * 创建折线图
	 * @return
	 */
	private JFreeChart createLineChart3D(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset() ;
		for(ChartData data:chartData){
			dataset.addValue(data.getValue(),"sameSeries",data.getSerise()) ;
		}
		JFreeChart chart =  ChartFactory.createLineChart3D(null,getCategoryAxisLabel(), getValueAxisLabel(), dataset, PlotOrientation.VERTICAL, false, false, false)  ;
		CategoryPlot plot = chart.getCategoryPlot() ;
		chart.setBackgroundPaint(new Color(255, 255, 255)) ;
		plot.getRangeAxis().setUpperMargin(0.08) ;//设置上边距
		LineRenderer3D renderer=new LineRenderer3D();
		plot.setRenderer(renderer) ;
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());// 显示每个柱的数值
		renderer.setBaseItemLabelsVisible(true);
		configPlot(plot, ReportUiUtil.getSysPath("JF_LineChartBG.png")) ;
		renderer.setItemMargin(0.1) ;
		plot.setRangeGridlinesVisible(true);
		//plot.setRangeGridlinePaint(Color.cyan);
		return chart ;
	}
	/**
	 * 创建3D饼图
	 * @return
	 */
	private JFreeChart createPieChart3D(){
		DefaultPieDataset dataset = new DefaultPieDataset() ;
		Map<String,Paint> sectionPaint = new HashMap<String,Paint>() ;
		int index = 0 ;
		for(ChartData data:chartData){
			dataset.setValue(data.getSerise(), data.getValue()) ;
			sectionPaint.put(data.getSerise(), Color.decode(defaultColor[index%defaultColor.length])) ;
			index ++ ;
		}
		JFreeChart chart =ChartFactory.createPieChart3D(null, dataset, false, false, false);
		PiePlot3D plot = (PiePlot3D) chart.getPlot() ;
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}, {1}({2})")) ;//饼图显示信息：名称,数量(百分比)
		plot.setBackgroundPaint(new Color(0xFF,0xFF,0xFF));//白色背景
		plot.setOutlineVisible(false) ;
		for(Map.Entry<String, Paint> entry:sectionPaint.entrySet()){
			plot.setSectionPaint(entry.getKey(), entry.getValue()) ;
		}
		return chart ;
	}
	/**
	 * 创建3D柱状图
	 * @return
	 */
	private JFreeChart createBarChart3D(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset() ;
		for(ChartData data:chartData){
			dataset.addValue(data.getValue(),"sameSeries",data.getSerise()) ;
		}
		JFreeChart chart =  ChartFactory.createBarChart3D(null,getCategoryAxisLabel(), getValueAxisLabel(), dataset, PlotOrientation.VERTICAL, false, false, false)  ;
		CategoryPlot plot = chart.getCategoryPlot() ;
		chart.setBackgroundPaint(new Color(255, 255, 255)) ;
		plot.getRangeAxis().setUpperMargin(0.08) ;//设置上边距
		CustomBarRenderer3D renderer = new CustomBarRenderer3D() ;
		plot.setRenderer(renderer) ;
		configPlot(plot, ReportUiUtil.getSysPath("JF_BarChartBG.png")) ;
		renderer.setMaximumBarWidth(0.1) ;
		renderer.setItemMargin(0.1) ;
		return chart ;
	}
	/**
	 * 创建区域图
	 * @return
	 */
	private JFreeChart createAreaChart(){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset() ;
		for(ChartData data:chartData){
			if(data.getCategory().length()>19){
				dataset.addValue(data.getValue(),data.getSerise(),data.getCategory().substring(0, 19)) ;
			}else{
				dataset.addValue(data.getValue(),data.getSerise(),data.getCategory()) ;
			}
		}
		JFreeChart chart = ChartFactory.createAreaChart(null,null , null, dataset, PlotOrientation.VERTICAL, true, false, false) ;
		CategoryPlot plot = (CategoryPlot) chart.getPlot() ;
		if(dataset.getColumnCount()>2){
			Date beginDate = StringUtil.toDate((String)dataset.getColumnKey(0),"yyyy-MM-dd HH:mm:ss") ;
			Date endDate = StringUtil.toDate((String)dataset.getColumnKey(dataset.getColumnCount()-1),"yyyy-MM-dd HH:mm:ss") ;
			long timeMinus = endDate.getTime() - beginDate.getTime() ;//时间差
			String matchPattern = "00:00:00$" ;//默认情况下只匹配整点数据
			String pattern = "MM-dd" ;//默认情况下只显示月和日信息
			if(ChartCategoryFormatter.greaterThanOneSeason(timeMinus)){//时间差大于一季度,只显示每月1号坐标
				matchPattern = "^.{8}01" ;//只匹配每月1号数据(yyyy-MM-01)
			}else if(ChartCategoryFormatter.greaterThanOneMonth(timeMinus)){//时间差大于一个月，只显示每个月1号和15号坐标
				matchPattern = "^.{8}(01|15)" ;//只匹配每月1号和15号
			}else if(ChartCategoryFormatter.greaterThanOneWeek(timeMinus)){//时间差大于一周，只显示奇数天坐标
				matchPattern = "^.{9}(1|3|5|7|9) 00:00:00$" ;//只匹配奇数天数据
			}else if(ChartCategoryFormatter.greaterThanOneDay(timeMinus)){//时间差大于一天，显示00点的坐标
				matchPattern= "00:00:00$" ;//匹配任意日期
			}else{//时间差小于1天显示整点坐标
				matchPattern = "00:00$" ;//只匹配整点数据
				pattern = "HH" ;
			}
//			DateDomainAxis axis = new DateDomainAxis(matchPattern,pattern) ;
//			plot.setDomainAxis(axis) ;
		}
		configPlot(plot, ReportUiUtil.getSysPath("JF_AreaChartBG.png")) ;
		return chart ;
	}
	/**
	 * 设置CategoryPlot区域的背景图片
	 * @param plot
	 * @param backgroundImage
	 */
	private void configPlot(CategoryPlot plot,String backgroundImage){
		try {
			plot.getRangeAxis().setUpperMargin(0.1) ;//区域图上边距(百分比)
			plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits()) ;//只显示整数坐标
			plot.setRangeGridlinesVisible(false) ;
			plot.setBackgroundImage(ImageIO.read(new File(backgroundImage))) ;
			plot.setBackgroundImageAlignment(Align.FIT_HORIZONTAL) ;
			plot.setForegroundAlpha(1.0F) ;
			CategoryItemRenderer renderer = plot.getRenderer() ;
			renderer.setBaseItemLabelsVisible(true) ;//显示坐标值信息
			//使用自定义的ItemLabelGenerator屏蔽0数据
			renderer.setBaseItemLabelGenerator(new MyCategoryItemLabelGenerator()) ;//显示信息模式
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));//显示位置
			//前十项使用默认自定义的颜色
			for(int i=0;i<defaultColor.length;i++){
				renderer.setSeriesPaint(i,Color.decode(defaultColor[i])) ;
			}
		} catch (IOException e) {}
	}
	/**
	 * 根据chartType创建对应的JFreeChart
	 * @return
	 */
	private JFreeChart createChart(){
		JFreeChart chart = null ;
		switch(chartType){
			case 1: 
				chart = createBarChart3D(); break;
			case 2: break ;
			case 3: break ;
			case 4: 
				chart = createAreaChart();break ;
			case 5: 
				chart = createPieChart3D(); break;
			case 6:
				chart=createLineChart3D();break;
			default: ;
		}
		if (chart != null) {
			chart.getPlot().setNoDataMessage("没有数据") ;
			chart.setAntiAlias(true) ;
		}
		return chart ;
	}
	public List<ChartData> getChartData() {
		return chartData;
	}
	public void setChartData(List<ChartData> chartData) {
		this.chartData = chartData;
	}
	Paint colors[] = new Paint[10];

	public Paint getItemPaint(int row, int column) {
		return colors[column % colors.length];
	}
	public String getCategoryAxisLabel() {
		return categoryAxisLabel;
	}
	public void setCategoryAxisLabel(String categoryAxisLabel) {
		this.categoryAxisLabel = categoryAxisLabel;
	}
	public String getValueAxisLabel() {
		return valueAxisLabel;
	}
	public void setValueAxisLabel(String valueAxisLabel) {
		this.valueAxisLabel = valueAxisLabel;
	}

	static class CustomBarRenderer3D extends BarRenderer3D {
		public Paint getItemPaint(int row, int column) {
			return Color.decode(defaultColor[column % defaultColor.length]);
		}
	}
	
	public static void main(String[] args) {
		List<ChartData> chartData = getChartDatas();
		JFreeChartImageCreator creator = new JFreeChartImageCreator(1, "jpg", 475, 278,chartData) ;
		System.out.println(creator.generateChartImage());
	}
	
	public static ArrayList<ChartData> getChartDatas(){
		ArrayList<ChartData> datas = new ArrayList<ChartData>() ;
		for (int i = 0; i < 5; i++) {
			ChartData data = new ChartData() ;
			data.setCategory("category"+i) ;
			data.setSerise("S"+i) ;
			data.setValue(Math.random()*100) ;
			datas.add(data) ;
		}
		return datas ;
	}
}
