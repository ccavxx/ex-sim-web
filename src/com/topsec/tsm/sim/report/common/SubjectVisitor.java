package com.topsec.tsm.sim.report.common;

import com.topsec.tsm.sim.report.component.ChartSubject;
import com.topsec.tsm.sim.report.component.ImageSubject;
import com.topsec.tsm.sim.report.component.TableSubject;
import com.topsec.tsm.sim.report.component.TextSubject;

public abstract class SubjectVisitor {

	/**
	 * 访问图片对象
	 * @param subject
	 */
	public abstract void visitImageSubject(ImageSubject subject) ;
	/**
	 * 访问文本对象
	 * @param subject
	 */
	public abstract void visitTextSubject(TextSubject subject) ;
	/**
	 * 访问图表对象
	 * @param subject
	 */
	public abstract void visitChartSubject(ChartSubject subject);
	/**
	 * 访问表格对象
	 * @param subject
	 */
	public abstract void visitTableSubject(TableSubject subject) ;
}
