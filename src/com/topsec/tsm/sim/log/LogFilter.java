package com.topsec.tsm.sim.log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

public 	class LogFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		// TODO Auto-generated method stub
		if (name.toLowerCase().endsWith(".dat"))
			return true;
		else
			return false;
	}

}
