package com.topsec.tsm.sim.report.component;

import com.topsec.tsm.sim.report.common.SubjectVisitor;


/**
 * 图表主题
 * 每一个图表主题都包含一个标题,标题还拥用一个背景图片
 * 默认标题位于图像上方
 * @author hp
 *
 */
public class ChartSubject extends AbstractSubject{
	/**
	 * 标题文字
	 */
	private String title ;

	/**
	 * 标题背景
	 */
	private String backgroundImage ;
	/**
	 * 标题高度，如果没有指定将使用DEFAULT_TITLE_HEIGHT
	 */
	private int titleHeight ;
	/**
	 * 标题背景
	 */
	private ImageSubject backgroundSubject ;
	/**
	 * 标题
	 */
	private TextSubject titleSubject ;
	/**
	 * 图像主题
	 */
	private ImageSubject chartImage ;
	
	/**
	 * 默认标题的高度
	 */
	public static final int DEFAULT_TITLE_HEIGHT = 20 ;
	/**
	 * 标题，背景，图像与外边框之间的距离
	 */
	private static final int INNER_PADDING = 1 ;
	
	public ChartSubject(String chartImagePath,String title){
		this(new ImageSubject(chartImagePath),title,0,0) ;
	}
	public ChartSubject(String chartImagePath,String title,int width,int height){
		this(new ImageSubject(chartImagePath),title,width,height) ;
	}
	
	public ChartSubject(ImageSubject chartImage,String title,int width,int height) {
		this(chartImage,title,width,height,null,DEFAULT_TITLE_HEIGHT) ;
	}
	public ChartSubject(ImageSubject chartImage,String title,int width,int height,String backgroundImage,int titleHeight) {
		this.chartImage = chartImage ;
		this.title = title ;
		this.backgroundImage = backgroundImage ;
		this.titleHeight = titleHeight ;
		if(backgroundImage!=null){
			backgroundSubject = new ImageSubject(backgroundImage,INNER_PADDING,INNER_PADDING,0,titleHeight,ImageFillMode.REPEAT) ;
		}
		titleSubject = new TextSubject(title,INNER_PADDING,INNER_PADDING,0,titleHeight) ;
		setWidth(width) ;
		setHeight(height) ;
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height) ;
		chartImage.setHeight(height-titleHeight-2*INNER_PADDING) ;
	}
	
	@Override
	public void setWidth(int width) {
		super.setWidth(width) ;
		if(titleSubject!=null){
			titleSubject.setWidth(width-2*INNER_PADDING) ;
		}
		if(backgroundSubject!=null){
			backgroundSubject.setWidth(width-2*INNER_PADDING) ;
		}
		chartImage.setWidth(width-2*INNER_PADDING) ;
	}
	
	@Override
	public void setGlobalX(int globalX) {
		super.setGlobalX(globalX) ;
		titleSubject.setGlobalX(globalX+INNER_PADDING) ;
		chartImage.setGlobalX(globalX+INNER_PADDING) ;
		if(backgroundSubject!=null){
			backgroundSubject.setGlobalX(globalX+INNER_PADDING) ;
		}
	}
	@Override
	public void setGlobalY(int globalY) {
		super.setGlobalY(globalY) ;
		titleSubject.setGlobalY(globalY+1) ;
		chartImage.setGlobalY(globalY+titleHeight+INNER_PADDING) ;
		if(backgroundSubject!=null){
			backgroundSubject.setGlobalY(globalY+INNER_PADDING) ;
		}
	}
	@Override
	public void accept(SubjectVisitor visitor) {
		visitor.visitChartSubject(this) ;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBackgroundImage() {
		return backgroundImage;
	}
	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}
	public ImageSubject getBackgroundSubject() {
		return backgroundSubject;
	}
	public void setBackgroundSubject(ImageSubject backgroundSubject) {
		this.backgroundSubject = backgroundSubject;
	}
	public TextSubject getTitleSubject() {
		return titleSubject;
	}
	public void setTitleSubject(TextSubject titleSubject) {
		this.titleSubject = titleSubject;
	}
	public ImageSubject getChartImage() {
		return chartImage;
	}
	public void setChartImage(ImageSubject chartImage) {
		this.chartImage = chartImage;
	}
}
