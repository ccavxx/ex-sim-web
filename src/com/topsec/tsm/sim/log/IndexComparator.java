package com.topsec.tsm.sim.log;

import java.io.File;
import java.util.Comparator;

public 	class IndexComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		String p1 = ((File) o1).getName();
		String p2 = ((File) o2).getName();
		long ftime1 = Long.parseLong(p1);
		long ftime2 = Long.parseLong(p2);
		if (ftime1 < ftime2)
			return -1;
		if (ftime1 > ftime2)
			return 1;
		return 0;

		// return -(p1.compareTo(p2));
	}

}
