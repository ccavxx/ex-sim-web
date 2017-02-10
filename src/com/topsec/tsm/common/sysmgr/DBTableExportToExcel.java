package com.topsec.tsm.common.sysmgr;

import java.sql.Connection;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class DBTableExportToExcel implements DBTableExport{

	private Connection conn ;
	/**是否分页导出*/
	private boolean pagination ;
	private int pageSize = 10000 ;
	private HSSFWorkbook workbook ;
	public DBTableExportToExcel(HSSFWorkbook workbook,Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public void export(String table) {
		StringBuffer sql = new StringBuffer("select * from ").append(table).append(" ") ;
		if(pagination){
			
		}
	}
	
}
