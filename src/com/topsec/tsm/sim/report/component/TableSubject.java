package com.topsec.tsm.sim.report.component;

import java.util.List;
import java.util.Map;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.report.common.SubjectVisitor;

/**
 * 表格类型主题
 * @author hp
 *
 */
public class TableSubject extends AbstractSubject {


	/**
	 * 行高
	 */
	private int rowHeight ;
	/**
	 * 列标题<br/>
	 * 每列都是一个Map对象
	 * 列定义<br/>
	 * headerText　列标题
	 * width　列宽
	 */
	private List<Map<String,Object>> headers ;
	/**
	 * 列数据
	 */
	private List<List> datas ;

	/**
	 * 创建表格主题，根据表格数据动态的计算各列的宽度,默认行高为20
	 * @param headers　列定义
	 * @param datas　数据
	 */
	public TableSubject(List<Map<String,Object>> headers, List<List> datas) {
		this(headers,datas,20) ;
	}
	/**
	 * 创建表格主题，根据表格数据动态的计算各列的宽度
	 * @param headers　列定义
	 * @param datas　数据定义
	 * @param rowHeight 行高
	 */
	public TableSubject(List<Map<String,Object>> headers, List<List> datas,int rowHeight) {
		this.rowHeight = rowHeight;
		this.datas = datas;
		this.headers = headers ;
		computeColumnWidth() ;
	}
	
	/**
	 * 根据每列中的最大字符数，动态的计算每列的宽度
	 */
	private void computeColumnWidth(){
		int[] columnMaxWords = new int[headers.size()] ;
		for(int i=0;i<datas.size();i++){
			List row = datas.get(i) ;
			for(int j=0;j<row.size();j++){
				String content = StringUtil.toString(row.get(j)) ;
				if(columnMaxWords[j]<content.length()){
					columnMaxWords[j] = content.length() ;
				}
			}
		}
		int totalWords = 0 ;//总共包含多少字符
		for(int len:columnMaxWords){
			totalWords+= len ;
		}
		if(totalWords==0){//空表格直接返回
			return ;
		}
		for(int i=0;i<headers.size();i++){
			headers.get(i).put("width", this.getWidth()*(columnMaxWords[i]/totalWords)) ;
		}
	}
	/**
	 * 获取指定列的宽度，如果没有指定，或者没有进行动态计算返回平均宽度
	 * @param columnIndex
	 * @return
	 */
	public int getColumnWidth(int columnIndex){
		Map<String,Object> columnDefine = headers.get(columnIndex) ;
		Integer columnWidth = (Integer)columnDefine.get("width") ;
		if(columnWidth==null){
			return this.getWidth()/headers.size() ;
		}
		return columnWidth ;
	}
	
	@Override
	public void accept(SubjectVisitor visitor) {
		visitor.visitTableSubject(this) ;
	}
	public int getRowHeight() {
		return rowHeight;
	}
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}
	public List<Map<String, Object>> getHeaders() {
		return headers;
	}
	public void setHeaders(List<Map<String, Object>> headers) {
		this.headers = headers;
	}
	public List<List> getDatas() {
		return datas;
	}
	public void setDatas(List<List> datas) {
		this.datas = datas;
	}
}
