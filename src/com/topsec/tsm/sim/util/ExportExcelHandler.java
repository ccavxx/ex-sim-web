package com.topsec.tsm.sim.util;

import org.apache.poi.hssf.usermodel.HSSFSheet;

public interface ExportExcelHandler <T> {

	void createSheetCell(HSSFSheet tableSheet, T tableDatas);
	
}
