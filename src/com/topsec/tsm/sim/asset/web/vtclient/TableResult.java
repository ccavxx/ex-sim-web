package com.topsec.tsm.sim.asset.web.vtclient;

import java.util.List;

public class TableResult implements CommandResult {

	private List<String> header ;
	private List<?> rows ;
	
	public TableResult(List<String> header, List<?> rows) {
		super();
		this.header = header;
		this.rows = rows;
	}

	@Override
	public String getDisplayType() {
		return "table";
	}

	@Override
	public Object getResult() {
		return rows;
	}

	public List<String> getHeader() {
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = header;
	}

}
