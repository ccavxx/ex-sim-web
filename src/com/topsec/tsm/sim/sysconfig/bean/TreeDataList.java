package com.topsec.tsm.sim.sysconfig.bean;

import java.util.ArrayList;
import java.util.Iterator;

public class TreeDataList extends ArrayList<TreeData> {
	

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public int indexOf(Object o) {
		if (o != null) {
			TreeData treedata = (TreeData) o;
			Object[] arrs = toArray();
			for (int i = 0; i < arrs.length; i++) {
				TreeData data = (TreeData) arrs[i];
				if (data.getId().equals(treedata.getId()))
					return i;
			}
		}
		return -1;
	}

	public TreeData get(TreeData o) {
		if (o != null) {
			Iterator<TreeData> it = iterator();
			while (it.hasNext()) {
				TreeData data = it.next();
				if (data.getId().equals(o.getId()))
					return data;
			}
		}
		return null;
	}

}
