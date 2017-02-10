package com.topsec.tsm.sim.report.jasper;

import java.util.HashMap;
import java.util.Map;

import com.topsec.tsm.sim.report.common.SubjectVisitor;
import com.topsec.tsm.sim.report.component.ChartSubject;
import com.topsec.tsm.sim.report.component.ImageFillMode;
import com.topsec.tsm.sim.report.component.ImageSubject;
import com.topsec.tsm.sim.report.component.Subject;
import com.topsec.tsm.sim.report.component.TableSubject;
import com.topsec.tsm.sim.report.component.TextSubject;

import net.sf.jasperreports.components.table.DesignCell;
import net.sf.jasperreports.components.table.StandardColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDatasetRun;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.component.ComponentKey;
import net.sf.jasperreports.engine.design.JRDesignBreak;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignDatasetRun;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignRectangle;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
/** 
 * @author hp
 *
 */
public class JasperSubjectVisitor extends SubjectVisitor {

	private JRBand band ;
	private Map<String,JRStyle> reportStyles ;//报表样式
	private boolean enablePagination ;//表示是否启用分页
	private int pageHeight ;//分页高度
	
	public JasperSubjectVisitor(JRBand band) {
		this(band,new HashMap<String,JRStyle>()) ;
	}
	public JasperSubjectVisitor(JRBand band,Map<String,JRStyle> reportStyles) {
		this(band,reportStyles,false,-1) ;
	}
	public JasperSubjectVisitor(JRBand band,Map<String,JRStyle> reportStyles,boolean pageEnable,int pageHeight) {
		this.band = band;
		this.reportStyles = reportStyles ;
		this.enablePagination = pageEnable ;
		this.pageHeight = pageHeight ;
	}

	/**
	 * 访问chart类型主题
	 */
	@Override
	public void visitChartSubject(ChartSubject subject) {
		if(enablePagination&&subject.getGlobalY()+subject.getHeight()>pageHeight){//如果启用分页
			String pageBreakKey = "break"+(subject.getGlobalY() / pageHeight) ;
			if(band.getElementByKey(pageBreakKey)==null){
				JRDesignBreak pageBreak = new JRDesignBreak() ;
				pageBreak.setKey(pageBreakKey) ;
				pageBreak.setY(subject.getGlobalY()-1) ;
				addChild(pageBreak) ;
			}
		}
		JRDesignRectangle border = new JRDesignRectangle() ;
		border.setStyle(reportStyles.get("subjectBorder")) ;
		fillElement(border, subject) ;
		addChild(border) ;
		if(subject.getBackgroundSubject()!=null){
			 JRDesignImage bgImage = createImageElement(subject.getBackgroundSubject()) ;
			addChild(bgImage) ;
		}
		JRDesignStaticText titleText = createTextElement(subject.getTitleSubject()) ;
		titleText.setStyle(reportStyles.get("subjectTitleStyle")) ;
		addChild(titleText);
		JRDesignImage chartImage = createImageElement(subject.getChartImage()) ;
		addChild(chartImage) ; 
	}
	/**
	 * 访问表格类型主题
	 */
	public void visitTableSubject(TableSubject subject){
		JRDesignExpression expression = new JRDesignExpression() ;
		expression.setText("$P{dataSetParam}") ;
		expression.setValueClass(JRDataSource.class) ;
		JRDesignDatasetRun dataSet = new JRDesignDatasetRun() ;
		dataSet.setDatasetName("dt") ;
		dataSet.setDataSourceExpression(expression) ;
		
		StandardTable table = new StandardTable() ;
		table.setDatasetRun(dataSet) ;
		StandardColumn column = new StandardColumn() ;
		column.setWidth(50) ;
		DesignCell cell = new DesignCell() ;
		JRDesignTextField text = new JRDesignTextField() ;
		JRDesignExpression textExpression = new JRDesignExpression() ;
		textExpression.setText("$F{n}") ;
		textExpression.setValueClass(String.class) ;
		text.setExpression(textExpression) ;
		cell.addElement(text) ;
		cell.setHeight(20) ;
		column.setDetailCell(cell) ;
		table.addColumn(column) ;
		JRDesignComponentElement component = new JRDesignComponentElement() ;
		component.setComponentKey(new ComponentKey("http://jasperreports.sourceforge.net/jasperreports/components", "jr", "table")) ;
		component.setWidth(100) ;
		component.setHeight(50) ;
		component.setComponent(table) ;
		addChild(component) ;
	}
	
	/**
	 * 访问Image类型主题
	 */
	@Override
	public void visitImageSubject(ImageSubject subject) {
		JRDesignImage image = createImageElement(subject) ;
		addChild(image) ;
	}
	/**
	 * 访问text类型主题
	 */
	@Override
	public void visitTextSubject(TextSubject subject) {
		JRDesignStaticText text = createTextElement(subject) ;
		addChild(text) ;
	}
	/**
	 * 添加子band子项
	 * @param child
	 */
	private void addChild(JRChild child){
		band.getChildren().add(child) ;
	}
	/**
	 * 根据主题的globalX,globalY,width,height赋值给JasperDesignElement对象
	 * @param el
	 * @param subject
	 */
	private static void fillElement(JRDesignElement el,Subject subject){
		el.setWidth(subject.getWidth()) ;
		el.setHeight(subject.getHeight()) ;
		el.setX(subject.getGlobalX()) ;
		el.setY(subject.getGlobalY()) ;
	}
	/**
	 * 根据文本类型主题属性创建JRDesignStaticText对象
	 * @param el
	 * @param subject
	 */
	private static JRDesignStaticText createTextElement(TextSubject subject){
		JRDesignStaticText textElement = new JRDesignStaticText() ;
		fillElement(textElement, subject) ;
		textElement.setText(subject.getText()) ;
		return textElement ;
	}
	/**
	 * 根据图片类型主题创建JasperDesignImage对象
	 * @param el
	 * @param subject
	 */
	private static JRDesignImage createImageElement(ImageSubject subject){
		JRDesignImage imageElement = new JRDesignImage(null) ;
		fillElement(imageElement, subject) ;
		JRDesignExpression exp = new JRDesignExpression() ;
		exp.setValueClass(String.class) ;
		exp.setText("\""+subject.getImageFile()+"\"") ;
		imageElement.setExpression(exp) ;
		ScaleImageEnum fillMode ;
		if(subject.getFillMode()==ImageFillMode.CLIP){
			fillMode = ScaleImageEnum.CLIP ;
		}else if(subject.getFillMode()==ImageFillMode.REPEAT){
			fillMode = ScaleImageEnum.FILL_FRAME ;
		}else{
			fillMode = ScaleImageEnum.RETAIN_SHAPE ;
		}
		imageElement.setScaleImage(fillMode) ;
		return imageElement ;
	}

}
