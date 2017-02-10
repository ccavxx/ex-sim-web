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
package com.topsec.tsm.sim.report.bean.struct;

/*
 *	TopSec-Ta-l 2009
 *	系统名：Ta-L Report
 *	类一览
 *		NO	类名		概要
 *		1	PositionStruts	iReport控件坐标所用结构体
 *	历史:
 *		NO	日期		版本		修改人		内容				
 *		1	2009/04/30	V1.0.1		Rick		初版
 */
public class PositionStruct {
	private int X;
	private int Y;
	private int Height;
	private int Width;
	public PositionStruct(){}
	public PositionStruct(double x, double y, double height, double width) {
		X =(int) x;
		Y = (int) y;
		Height = (int) height;
		Width = (int) width;
	}

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

	public int getHeight() {
		return Height;
	}

	public void setHeight(int height) {
		Height = height;
	}

	public int getWidth() {
		return Width;
	}

	public void setWidth(int width) {
		Width = width;
	}
}
