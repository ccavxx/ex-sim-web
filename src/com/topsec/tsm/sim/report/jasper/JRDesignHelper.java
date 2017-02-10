//
// COPYRIGHT (C) 2009 TOPSEC CORPORATION
//
// ALL RIGHTS RESERVED BY TOPSEC CORPORATION, THIS PROGRAM
// MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
// FURNISHED BY TOPSEC CORPORATION, NO PART OF THIS PROGRAM
// MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
// WITHOUT THE PRIOR WRITTEN PERMISSION OF TOPSEC CORPORATION.
// USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
// OF THE PROGRAM
//
//            TOPSEC CONFIDENTIAL AND PROPROETARY
//
////////////////////////////////////////////////////////////////////////////
package com.topsec.tsm.sim.report.jasper;

/*
 *	TopSec-Ta-l 2009
 *	系统名：Ta-L Report
 *	类一览
 *		NO	类名		概要
 *		1	JRDesignHelper		Pdf、Xls、Doc文件导出Helper类
 *	历史:
 *		NO	日期		版本		修改人		内容				
 *		1	2009/04/30	V1.0.1		Rick		初版
 */
import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.apache.log4j.Logger;

import com.topsec.tsm.sim.report.bean.struct.PositionStruct;

public class JRDesignHelper {
	private static Logger log = Logger.getLogger(JRDesignHelper.class);

	/**
	 * 设置expression
	 * 
	 * @param String
	 *            expName expression名称 *
	 * @param Class
	 *            valueClass Class
	 * @param String
	 *            paramType 值
	 * @return JRDesignExpression JRDesignExpression
	 */

	public static JRDesignExpression setExp(String expName, Class valueClass,
			String paramType) {
		JRDesignExpression expression = new JRDesignExpression();
		expression.setValueClass(valueClass);
		expression.setText(paramType + "{" + expName + "}");
		return expression;
	}

	/**
	 * 设置F
	 * 
	 * @param String
	 *            fieldName F名称 *
	 * @param Class
	 *            valueClass Class
	 * @return JRDesignField JRDesignField
	 * 
	 */
	public static JRDesignField setField(String fieldName, Class valueClass) {
		JRDesignField field = new JRDesignField();
		field.setName(fieldName);
		field.setValueClass(valueClass);
		return field;
	}

	/**
	 * 设置p
	 * 
	 * @param String
	 *            fieldName p名称 *
	 * @param Class
	 *            valueClass Class
	 * @return JRDesignParameter JRDesignParameter
	 * 
	 */
	public static JRDesignParameter setParam(String fieldName, Class valueClass) {
		JRDesignParameter parameter = new JRDesignParameter();
		parameter.setName(fieldName);
		parameter.setValueClass(valueClass);
		return parameter;
	}

	/**
	 * 设置静态文本框
	 * 
	 * @param JRDesignBand
	 *            band 文本框所在的band
	 * @param PositionStruts
	 *            positionStruts 静态文本l t w h内容
	 * @param String
	 *            strText 静态文本框内容
	 * @param JRDesignStyle
	 *            jStyle 静态文本框Style
	 * @return void
	 * 
	 */
	public static void setStaticTextControl(JRDesignBand band,
			PositionStruct positionStruct, String strText, JRDesignStyle jStyle) {
		JRDesignStaticText staticText = new JRDesignStaticText();
		staticText.setX(positionStruct.getX());
		staticText.setY(positionStruct.getY());
		staticText.setWidth(positionStruct.getWidth());
		staticText.setHeight(positionStruct.getHeight());
		staticText.setText(strText);
		staticText.setMarkup(JRCommonText.MARKUP_HTML);
		if (jStyle != null){
			staticText.setStyle(jStyle);
		}
		band.addElement(staticText);
	}

	/**
	 * 设置动态文本框
	 * 
	 * @param JRDesignBand
	 *            band 文本框所在的band
	 * @param PositionStruts
	 *            positionStruts 动态文本框l t w h内容 *
	 * @param String
	 *            fieldName 文本变量名称 *
	 * @param Class
	 *            valueClass Class
	 * @param String
	 *            paramType 文本变量内容
	 * @param JRDesignStyle
	 *            jStyle 文本框Style
	 * @param boolean
	 *            overflow文本框内容是否为overflow
	 * @return void
	 * 
	 */
	public static void setTextControl(JRDesignBand band,
			PositionStruct positionStruct, String fieldName, Class valueClass,
			String paramType, JRDesignStyle jStyle, boolean overflow) {
		JRDesignTextField titleField = new JRDesignTextField();
		titleField.setX(positionStruct.getX());
		titleField.setY(positionStruct.getY());
		titleField.setWidth(positionStruct.getWidth());
		titleField.setHeight(positionStruct.getHeight());
		titleField.setMarkup(JRCommonText.MARKUP_HTML);
		titleField.setBlankWhenNull(true);
		titleField.setExpression(JRDesignHelper.setExp(fieldName, valueClass,paramType));
		if (jStyle != null)
			titleField.setStyle(jStyle);
		titleField.setStretchWithOverflow(overflow);
		band.addElement(titleField);
	}

	/**
	 * 设置Image对象
	 * 
	 * @param JRDesignBand
	 *            band 文本框所在的band
	 * @param PositionStruts
	 *            positionStruts 动态文本框l t w h内容 *
	 * @param String
	 *            fieldName 文本变量名称 *
	 * @param Class
	 *            valueClass Class
	 * @param String
	 *            paramType 文本变量内容
	 * @param JRDesignStyle
	 *            jStyle 文本框Style scaleImage="FillFrame"
	 * @return void
	 * 
	 */
	public static void setImgControl(JRDesignBand band,
			PositionStruct positionStruct, String fieldName, Class valueClass,
			String paramType, JRDesignStyle jStyle, Byte scaleImage) {
		JRDesignImage image = new JRDesignImage(new JRDesignStyle()
				.getDefaultStyleProvider());
		image.setX(positionStruct.getX());
		image.setY(positionStruct.getY());
		image.setWidth(positionStruct.getWidth());
		image.setHeight(positionStruct.getHeight());
		image.setExpression(JRDesignHelper.setExp(fieldName, valueClass,paramType));
		if (scaleImage != null)
			image.setScaleImage(scaleImage);
		if (jStyle != null)
			image.setStyle(jStyle);
		band.addElement(image);
	}

	/**
	 * 设置$P
	 * 
	 * @param JasperDesign
	 *            mstDesign $P所在的design *
	 * @param String
	 *            fieldName $P名称
	 * @param Class
	 *            valueClass Class
	 * 
	 * @return JRDesignExpression JRDesignExpression
	 */
	public static void addPParam(JasperDesign mstDesign, String fieldName,
			Class valueClass) {
		try {
			mstDesign.addParameter(JRDesignHelper.setParam(fieldName,
					valueClass));
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
			log.error("addPParam==" + e.getMessage());
		}
	}

	/**
	 * 设置$F
	 * 
	 * @param JasperDesign
	 *            mstDesign $F所在的design *
	 * @param String
	 *            fieldName $F名称
	 * @param Class
	 *            valueClass Class
	 * 
	 * @return JRDesignExpression JRDesignExpression
	 */
	public static void addFParam(JasperDesign subDesign, String fieldName,
			Class valueClass) {
		try {
			subDesign.addField(JRDesignHelper.setField(fieldName, valueClass));
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
			log.error("addPParam==" + e.getMessage());
		}
	}

}
