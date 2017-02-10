package com.topsec.tsm.sim.report.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

public class ChartConstant {
	static Font newfont; 
	static{		
		String fileName=ChartConstant.class.getClassLoader().getResource("/resource/report/jingdiansong.ttf").toString();
		int pos=fileName.indexOf("file:");    
	    if(pos>-1) 
	    	fileName=fileName.substring(pos+5);  
	    
		File fontfile = new File(fileName);
		try {
			newfont = Font.createFont(Font.TRUETYPE_FONT, fontfile);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final String Type_BarChart = "BarChart";
	public static final String Type_CyliderChart = "CyliderChart";
	public static final String Type_StackedChart = "StackedChart";
	public static final String Type_LineChart = "LineChart";
	public static final String Type_PieChart = "PieChart";	 	
		
	public static final Font TITLE_Font = newfont.deriveFont(10, 15);
	public static final Font Default_Font = newfont.deriveFont(10, 12);
	public static final Font TITLE_UNIT_Font = newfont.deriveFont(10, 14);
	public static final Font FONT = new Font("黑体", Font.PLAIN, 13);	
	
	public static final Font TITLE_Font_Ex = newfont.deriveFont(10, 11);
	public static final Font Default_Font_Ex = newfont.deriveFont(10, 10);
	public static final Font TITLE_UNIT_Font_Ex = newfont.deriveFont(10, 11);	
	public static final String PlotOrientation_VERTICAL = "VERTICAL";
	public static final String PlotOrientation_HORIZONTAL = "HORIZONTAL";
}
