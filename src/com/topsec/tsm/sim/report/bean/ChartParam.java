package com.topsec.tsm.sim.report.bean;

public class ChartParam {
	 private Integer charttype;
	   private String subname;
	   private String unit;
	   private String msttype;
	   private String chartlink;
	   
	   public Integer getCharttype() {
			return charttype;
		}
		public void setCharttype(Integer charttype) {
			this.charttype = charttype;
		}
		public String getSubname() {
			return subname;
		}
		public void setSubname(String subname) {
			this.subname = subname;
		}
		public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}
		public String getMsttype() {
			return msttype;
		}
		public void setMsttype(String msttype) {
			this.msttype = msttype;
		}
		public String getChartlink() {
			return chartlink;
		}
		public void setChartlink(String chartlink) {
			this.chartlink = chartlink;
		}
}
