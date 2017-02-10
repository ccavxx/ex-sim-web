package com.topsec.tsm.sim.report.bean.struct;

import java.util.List;

public class SqlStruct {
	// sql 主语句
		public String sql;
		// sql 参数
		public List sqlparam;
		// sql dig
		public String sqldig;
		// sql 分页
		public Integer sqlpage;
		// dvc ip
		public String dvcIp;
		// chart 下探参数2
		private String[] talCategory;
		private String sTime;
		private String eTime;
		private String[] nodeId;
		private String onlyByDvctype;
		private String transAuditNode;
		private String devTypeName;//设备类型表示名

		public String getDevTypeName() {
			return devTypeName;
		}

		public void setDevTypeName(String devTypeName) {
			this.devTypeName = devTypeName;
		}

		public String getOnlyByDvctype() {
			return onlyByDvctype;
		}

		public void setOnlyByDvctype(String onlyByDvctype) {
			this.onlyByDvctype = onlyByDvctype;
		}
		
		public String getTransAuditNode() {
			return transAuditNode;
		}

		public void setTransAuditNode(String transAuditNode) {
			this.transAuditNode = transAuditNode;
		}

		public String getSql() {
			if (sql == null)
				sql = "";
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public String getSqldig() {
			return sqldig;
		}

		public void setSqldig(String sqldig) {
			this.sqldig = sqldig;
		}

		public List getSqlparam() {
			return sqlparam;
		}

		public void setSqlparam(List sqlparam) {
			this.sqlparam = sqlparam;
		}

		public Integer getSqlpage() {
			return sqlpage;
		}

		public void setSqlpage(Integer sqlpage) {
			this.sqlpage = sqlpage;
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

		public String getsTime() {
			return sTime;
		}

		public void setsTime(String sTime) {
			this.sTime = sTime;
		}

		public String geteTime() {
			return eTime;
		}

		public void seteTime(String eTime) {
			this.eTime = eTime;
		}

		public String[] getNodeId() {
			return nodeId;
		}

		public void setNodeId(String[] nodeId) {
			this.nodeId = nodeId;
		}


}
