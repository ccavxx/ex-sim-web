package com.topsec.tsm.sim.common.bean;

import java.util.Vector;

public class Type {
	private String id;

	private String name;

	private String page;

	private String className;
	
	private String updatePage;

	/**
	 * @return 返回 className。
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            要设置的 className。
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return 返回 id。
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            要设置的 id。
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return 返回 name。
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            要设置的 name。
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return 返回 page。
	 */
	public String getPage() {
		return page;
	}

	/**
	 * @param page
	 *            要设置的 page。
	 */
	public void setPage(String page) {
		this.page = page;
	}

	private Vector chartTpyes = new Vector();

	public void addChartType(ChartType chartType) {
		chartTpyes.add(chartType);
	}

	public int getChartTypeSize() {
		return chartTpyes.size();
	}

	public ChartType getChartType(int i) {
		return (ChartType) chartTpyes.get(i);
	}

	public ChartType getChartType(Object key) {
		
		for(int i =0;i<chartTpyes.size();i++){
			ChartType chart = (ChartType) chartTpyes.get(i);
			if(chart.getId().equals(key)){
				return chart;
			}
		}
		return new ChartType();
	}
	
	public Vector getChartTypes() {
		return chartTpyes;
	}
	/**
	 * @return 返回 updatePage。
	 */
	public String getUpdatePage() {
		return updatePage;
	}
	/**
	 * @param updatePage 要设置的 updatePage。
	 */
	public void setUpdatePage(String updatePage) {
		this.updatePage = updatePage;
	}

}
