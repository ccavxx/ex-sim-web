package com.topsec.tsm.sim.log.web;

import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.charts.AxisCrossBetween;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.topsec.tal.base.report.poi.BarChartData;
import com.topsec.tal.base.report.poi.ChartData;
import com.topsec.tal.base.report.poi.LineChartData;
import com.topsec.tal.base.report.poi.PieChartData;
import com.topsec.tal.base.report.poi.SimChartDataFactory;
import com.topsec.tal.base.report.poi.SimChartDataFactoryImpl;
import com.topsec.tal.base.report.poi.SimXWPFDocument;
import com.topsec.tal.base.util.StringUtil;

public class WordChartCreator{

	private SimXWPFDocument doc ;
	private int chartCount ;
	public WordChartCreator(SimXWPFDocument doc) {
		this.doc = doc ;
	}

	public void createChart(int chartType, String title,List<List<Object>> countChartData, String unit) {
		if(countChartData.size() == 0){
			return ;
		}
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("chartData"+(chartCount++));
        final int NUM_OF_ROWS = countChartData.size();
        final int NUM_OF_COLUMNS = countChartData.get(0).size();
        if(NUM_OF_ROWS < 2){
			return ;
		}
        for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
        	XSSFRow sheetRow = sheet.createRow(rowIndex) ;
        	for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
        		XSSFCell cell = sheetRow.createCell(colIndex) ;
        		Object cellValue = countChartData.get(rowIndex).get(colIndex) ;
        		if(cellValue instanceof String){
        			cell.setCellType(Cell.CELL_TYPE_STRING) ;
        			cell.setCellValue(String.valueOf(cellValue)) ;
        		}else if(cellValue instanceof Number){
        			cell.setCellType(Cell.CELL_TYPE_NUMERIC) ;
        			cell.setCellValue(((Number)cellValue).doubleValue()) ;
        		}else if(cellValue instanceof Date){
        			cell.setCellType(Cell.CELL_TYPE_STRING) ;
        			cell.setCellValue(StringUtil.longDateString((Date)cellValue)) ;
        		}
        	}
        }
		XSSFChart chart;
		try {
			chart = doc.createChart(wb);
			ChartLegend legend  = chart.getOrCreateLegend();
	        legend.setPosition(LegendPosition.TOP_RIGHT);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
		switch(chartType){
	    	case 1:{createBarChartData(title,sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);break; }
	    	case 5:{createPieChartData(title,sheet, chart, NUM_OF_ROWS,NUM_OF_COLUMNS,unit);break;}
	    	case 6:{createLineChartData(title,sheet, chart,NUM_OF_ROWS, NUM_OF_COLUMNS,unit);break;}
	    	default:{throw new RuntimeException("无效的图表类型"+chartType);}
		}
	}
	/**
	 * 创建柱状图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createBarChartData(String title,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        BarChartData data = dataFactory.createBarChartLapData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        fillChartData(title,sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	/**
	 * 创建拆线图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createLineChartData(String title,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
        LineChartData data = dataFactory.createLineChartData();
        data.setUnit(unit) ;
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN) ;
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        fillChartData(title,sheet, chart, data, rows, columns) ;
        data.fillChart(chart, bottomAxis,leftAxis) ;
        return data ;
	}
	/**
	 * 创建饼图数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param rows
	 * @param columns
	 * @return
	 */
	private static ChartData createPieChartData(String title,XSSFSheet sheet,XSSFChart chart,int rows,int columns,String unit){
		SimChartDataFactory dataFactory = SimChartDataFactoryImpl.getInstance() ;
		PieChartData data = dataFactory.createPieChartData();
        //data.setUnit(unit) ;
		fillChartData(title,sheet, chart, data, rows, columns) ;
		data.fillChart(chart) ;
		return data ;
	}
	/**
	 * 填充数据
	 * @param reportSubject
	 * @param sheet
	 * @param chart
	 * @param data
	 * @param rows
	 * @param columns
	 */
	private static void fillChartData(String title,XSSFSheet sheet,XSSFChart chart,ChartData data,int rows,int columns){
		ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, new CellRangeAddress(1, rows-1 , 0, 0));
        data.setCategories(xs) ;
        data.setTitle(title) ;
        for(int colIndex=1;colIndex < columns;colIndex++){
        	ChartDataSource<String> seriesName = DataSources.fromStringCellRange(sheet, new CellRangeAddress(0, 0, colIndex, colIndex)) ;
        	ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, rows-1, colIndex, colIndex));
        	data.addSeries(ys,seriesName);
        }
	}
}
