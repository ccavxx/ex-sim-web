package com.topsec.tsm.ui.topo.bean;

import org.apache.commons.lang.StringUtils;

public class Topo {
	private String topoId;
	private String topoType;

	private String subTopo;
	private String parentTopo;

	public boolean hasSub() {
		return StringUtils.isNotEmpty(subTopo);
	}

	public boolean hasParent() {
		return StringUtils.isNotEmpty(parentTopo);
	}
   
	public String getTopoId() {
		return topoId;
	}

	@Override
	public String toString() {
		return topoId + "/" + topoType + "/parent:" + parentTopo + "/sub:" + subTopo;
	}

	public void setTopoId(String topoId) {
		this.topoId = topoId;
	}

	public String getTopoType() {
		return topoType;
	}

	public void setTopoType(String topoType) {
		this.topoType = topoType;
	}

	public String getSubTopo() {
		return subTopo;
	}

	public void setSubTopo(String subTopo) {
		this.subTopo = subTopo;
	}

	public String getParentTopo() {
		return parentTopo;
	}

	public void setParentTopo(String parentTopo) {
		this.parentTopo = parentTopo;
	}

	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (!(obj instanceof Topo))
			return false;
		else {
			Topo topo = (Topo) obj;
			if (null == this.getTopoId() || null == topo.getTopoId())
				return false;
			else
				return this.getTopoId().equals(topo.getTopoId());
		}
	}

	@Override
	public int hashCode() {
		return this.topoId.hashCode();
	}

}
