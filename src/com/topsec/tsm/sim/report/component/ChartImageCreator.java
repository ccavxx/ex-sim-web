package com.topsec.tsm.sim.report.component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.topsec.tsm.common.CallableCallback;

/**
 * 抽象的图表图片生成类
 * 同时此类实现了Callable接口，此类的实例可以使用ExecutorService.invokeAll或者ExecutorService.invokeAny
 * 同时客户端还可以指定一个CallableCallback对象，在call方法完成返回以前对结果进行处理
 * @see ExecutorService#invokeAll(java.util.Collection)
 * @author hp
 *
 */
public abstract class ChartImageCreator implements Callable<String>{
	/**
	 * call方法回调接口，在使用多线程生成图片时，图片生成完成以后回调此接口
	 */
	protected CallableCallback<String> callback ;
	/**
	 * 生成图片的类型
	 */
	protected String imageType ;
	/**
	 * 图表的类型
	 */
	protected Integer chartType ;
	/**
	 * 生成图片的宽度
	 */
	protected int chartWidth ;
	/**
	 * 生成图片的高度
	 */
	protected int chartHeight ;
	
	public ChartImageCreator() {
		super();
	}
	public ChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight){
		this(chartType,imageType,chartWidth,chartHeight,null) ;
	}
	public ChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,CallableCallback<String> callback) {
		this.callback = callback ;
		this.imageType = imageType ;
		this.chartType = chartType;
		this.chartWidth = chartWidth;
		this.chartHeight = chartHeight;
	}

	/**
	 * 生成图片,createTempImageFile可以生成一个临时文件，可以将生成的图片信息存储到临时文件中去
	 * @return 图片的绝对路径
	 */
	public abstract String generateChartImage() ;
	
	/**
	 * 创建一个图片临时文件,前辍为chart,后辍为为指定的imageType
	 * @param imageType 图片后辍
	 * @return 创建的临时对象
	 * @throws IOException
	 */
	protected File createTempImageFile(String imageType) throws IOException{
		File f = File.createTempFile("chart", "."+imageType) ;
		return f ;
	}

	@Override
	public String call() throws Exception {
		String filePath = null;
		try {
			filePath = generateChartImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(callback!=null){
			callback.callback(filePath) ;
		}
		return filePath;
	}
	public CallableCallback<String> getCallback() {
		return callback;
	}
	public void setCallback(CallableCallback<String> callback) {
		this.callback = callback;
	}
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	public Integer getChartType() {
		return chartType;
	}
	public void setChartType(Integer chartType) {
		this.chartType = chartType;
	}
	public int getChartWidth() {
		return chartWidth;
	}
	public void setChartWidth(int chartWidth) {
		this.chartWidth = chartWidth;
	}
	public int getChartHeight() {
		return chartHeight;
	}
	public void setChartHeight(int chartHeight) {
		this.chartHeight = chartHeight;
	}
	
}
