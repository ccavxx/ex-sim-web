package com.topsec.tsm.sim.util;

import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportExcelUtil{
	private static final Logger log = LoggerFactory.getLogger(ExportExcelUtil.class);
	/**
	 * 导出Excel文件
	 * @param response 请求响应
	 * @param tableHead excel中列头信息
	 * @param tableDatas excel数据
	 * @param exportExcelHandler 
	 */
	public static <T> void exportExcel(HttpServletResponse response,
			List<String> tableHead, T tableDatas, ExportExcelHandler<T> exportExcelHandler) {
		try {
			// 创建一个工作薄
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFCellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			titleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			titleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			titleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			HSSFFont font = workbook.createFont();
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			titleStyle.setFont(font);
			HSSFSheet tableSheet = workbook.createSheet("sheet");
			tableSheet.setColumnWidth(0, 35 * 256);

			HSSFRow tableRow = tableSheet.createRow((int) 0);
			for (int i = 1, len = tableHead.size(); i < len; i++) {
				HSSFCell cellHead = tableRow.createCell(i - 1);
				cellHead.setCellValue(tableHead.get(i));
				cellHead.setCellStyle(titleStyle);
			}
			exportExcelHandler.createSheetCell(tableSheet, tableDatas);
			OutputStream ouputStream = response.getOutputStream();
			workbook.write(ouputStream);
			ouputStream.flush();
			ouputStream.close();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
