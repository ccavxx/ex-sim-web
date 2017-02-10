package com.topsec.tsm.sim.newreport.bean;

/**
 * @ClassName: PageVo
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2015年7月14日下午3:32:51
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class PageVo {
	
	private long pageIndex=1L;
	private long pageSize;
	
	public long getPageIndex() {
		return pageIndex;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPage(long pageIndex,long pageSize) {
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	public PageVo() {
		super();
	}

	public PageVo(Long pageIndex, Long pageSize) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
}
