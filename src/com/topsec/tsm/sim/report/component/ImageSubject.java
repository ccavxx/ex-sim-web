package com.topsec.tsm.sim.report.component;

import com.topsec.tsm.sim.report.common.SubjectVisitor;


/**
 * 图像主题
 * @author hp
 *
 */
public class ImageSubject extends AbstractSubject{
	/**
	 * 图像路径
	 */
	private String imageFile ;
	/**
	 * 如果宽度大于图片宽度,使用的填充方式
	 */
	private ImageFillMode fillMode = ImageFillMode.CLIP ;

	/**
	 * 
	 * @param imageFile 图片路径
	 * @param width 主题宽度
	 * @param height 主题高度
	 * @param fillMode 图片填充模式
	 */
	public ImageSubject(String imageFile,int width,int height,ImageFillMode fillMode) {
		this(imageFile,0,0,width,height,fillMode) ;
	}
	/**
	 * 
	 * @param imageFile 图片路径
	 * @param x 主题x坐标
	 * @param y 主题y坐标
	 * @param width 主题宽度
	 * @param height 主题高度
	 * @param fillMode 图片填充模式
	 */
	public ImageSubject(String imageFile,int x,int y,int width,int height,ImageFillMode fillMode) {
		super(x,y,width,height) ;
		this.imageFile = imageFile ;
		this.fillMode = fillMode ;
	}
	/**
	 * 默认创建主题x,y,width,height都为0,需要创建完成后指定x,y,width,height的值
	 * @param imageFile 图片路径
	 * @param fillMode 图片填充模式
	 */
	public ImageSubject(String filePath,ImageFillMode fillMode){
		this(filePath,0,0,0,0,fillMode) ;
	}
	/**
	 * 默认创建主题x,y,width,height都为0,需要创建完成后指定x,y,width,height的值
	 * 默认的图片填充方式为ImageFillMode.CLIP
	 * @param imageFile 图片路径
	 */
	public ImageSubject(String filePath){
		this(filePath,0,0,0,0,ImageFillMode.CLIP) ;
	}

	@Override
	public void accept(SubjectVisitor visitor) {
		visitor.visitImageSubject(this) ;
	}
	public void addChild(Subject child) {
		throw new UnsupportedOperationException() ;
	}
	public Subject getChildAt(int index) {
		throw new UnsupportedOperationException() ;
	}
	public int getChildCount() {
		throw new UnsupportedOperationException() ;
	}
	public ImageFillMode getFillMode() {
		return fillMode;
	}
	public void setFillMode(ImageFillMode fillMode) {
		this.fillMode = fillMode;
	}
	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}
}
