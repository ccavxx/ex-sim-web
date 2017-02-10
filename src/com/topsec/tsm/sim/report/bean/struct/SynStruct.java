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
 *		1	SynStruts	PDF DOC XLS报表导出封面所用结构体
 *	历史:
 *		NO	日期		版本		修改人		内容				
 *		1	2009/04/30	V1.0.1		Rick		初版
 */
public class SynStruct extends ExpStruct{

	private String subName;// 子报表名
	private int subPage;// 子页
	private String dvcType;//设备
	private String dvcIp;//设备ip
	private String[] talCategory;
	private String[] nodeId;
	private String onlyByDvctype;
	public String getSubName() {
		return subName;
	}
	public void setSubName(String subName) {
		this.subName = subName;
	}
	public int getSubPage() {
		return subPage;
	}
	public void setSubPage(int subPage) {
		this.subPage = subPage;
	}
	public String getDvcType() {
		return dvcType;
	}
	public void setDvcType(String dvcType) {
		this.dvcType = dvcType;
	}
	public String getDvcIp() {
		return dvcIp;
	}
	public void setDvcIp(String dvcIp) {
		this.dvcIp = dvcIp;
	}
	public String[] getTalCategory() {
		return talCategory;
	}
	public void setTalCategory(String[] talCategory) {
		this.talCategory = talCategory;
	}
	public String[] getNodeId() {
		return nodeId;
	}
	public void setNodeId(String[] nodeId) {
		this.nodeId = nodeId;
	}
	public String getOnlyByDvctype() {
		return onlyByDvctype;
	}
	public void setOnlyByDvctype(String onlyByDvctype) {
		this.onlyByDvctype = onlyByDvctype;
	}

	
}