package com.topsec.tsm.sim.common.bean;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @ClassName: ImportResult
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月12日下午5:59:27
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ImportResult implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int successCount;
	private int failedCount;
	private int totalCount;
	private String message;
	private boolean status;
	private String summaryErrorContent;
	private String particularErrorContent;
	private Map<String, String>formatErrorMap;
	private Map<String, String>dataConflictErrorMap;
	private List<ErrorMark> marks;
	public ImportResult() {
		super();
	}
	@Override
	public String toString() {
		return "ImportResult [successCount=" + successCount + ", failedCount="
				+ failedCount + ", totalCount=" + totalCount + ", message="
				+ message + ", status=" + status + "]";
	}
	public int getSuccessCount() {
		return successCount;
	}
	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}
	public int getFailedCount() {
		return failedCount;
	}
	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getSummaryErrorContent() {
		return summaryErrorContent;
	}
	public String getParticularErrorContent() {
		return particularErrorContent;
	}
	public Map<String, String> getFormatErrorMap() {
		return formatErrorMap;
	}
	public Map<String, String> getDataConflictErrorMap() {
		return dataConflictErrorMap;
	}
	private void setErrorMap(Map<String, String> formatErrorMap,Map<String, String> dataConflictErrorMap) {
		this.formatErrorMap = formatErrorMap;
		this.dataConflictErrorMap = dataConflictErrorMap;
	}
	public void initErrorContent(Map<String, String> formatErrorMap,Map<String, String> dataConflictErrorMap){
		setErrorMap(formatErrorMap,dataConflictErrorMap);
		StringBuffer tempStringBuffer=new StringBuffer("序号: ");
		StringBuffer tempStringBuffer1=new StringBuffer();
		if (!(null==formatErrorMap||formatErrorMap.size()<1)) {
			for(Map.Entry<String, String> entry : formatErrorMap.entrySet()){
				String string = entry.getKey();
				String string1 = entry.getValue();
				tempStringBuffer.append(string).append(", ");
				tempStringBuffer1.append("序号 ").append(string).append(": ").append(string1).append("  ");
			}
		}
		
		if (!(null==dataConflictErrorMap||dataConflictErrorMap.size()<1)) {
			for(Map.Entry<String, String> entry : dataConflictErrorMap.entrySet()){
				String string = entry.getKey();
				String string1 = entry.getValue();
				tempStringBuffer.append(string).append(", ");
	            tempStringBuffer1.append("序号 ").append(string).append(": ").append(string1).append("  ");
			}
		}
		tempStringBuffer.append("内容有误, 请检查后再导入");
		summaryErrorContent=tempStringBuffer.toString();
		particularErrorContent=tempStringBuffer1.toString();
	}
	public List<ErrorMark> getMarks() {
		return marks;
	}
	public void setMarks(List<ErrorMark> marks) {
		this.marks = marks;
	}
	
}
