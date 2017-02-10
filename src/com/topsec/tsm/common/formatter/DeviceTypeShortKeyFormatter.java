package com.topsec.tsm.common.formatter;

import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil;

public class DeviceTypeShortKeyFormatter implements PropertyFormatter{

	private DeviceTypeShortKeyUtil util = DeviceTypeShortKeyUtil.getInstance();
	
	@Override
	public Object format(Object value) {
		return util.getShortZhCN((String)value);
	}
}
