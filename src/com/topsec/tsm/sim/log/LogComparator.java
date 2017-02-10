package com.topsec.tsm.sim.log;

import java.io.File;
import java.util.Comparator;

public 	class LogComparator implements Comparator<File> {

	public int compare(File o1, File o2) {
		String p1 = ((File) o1).getName();
		String p2 = ((File) o2).getName();
		long ftime1 = Long.parseLong(p1.substring(0,p1.length()-4));
		long ftime2 = Long.parseLong(p2.substring(0,p2.length()-4));
		if (ftime1 < ftime2)
			return -1;
		if (ftime1 > ftime2)
			return 1;
		return 0;

		// return -(p1.compareTo(p2));
	}

}
