package com.topsec.tsm.ui.topo.bean;

public class EvtCol {
	private String id;
	private String name;
	private String colName;
	private String postfix;
	private String evaluator;

	public String getPostfix() {
		return postfix;
	}

	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	public String getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(String evaluator) {
		this.evaluator = evaluator;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
		}  

	public void setName(String name) {
		this.name = name;
	}

	public String getColName() {
		return colName;
	}

	public String getId() {
		return id;
	}

	public EvtCol(String id, String name, String colName) {
		super();
		this.id = id;
		this.name = name;
		this.colName = colName;
	}

	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (!(obj instanceof DevStatus))
			return false;
		else {
			DevStatus def = (DevStatus) obj;
			if (null == this.getColName() || null == def.getColName())
				return false;
			else
				return this.getColName().equals(def.getColName());
		}
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	public void setColName(String colName) {
		this.colName = colName;
	}
}
