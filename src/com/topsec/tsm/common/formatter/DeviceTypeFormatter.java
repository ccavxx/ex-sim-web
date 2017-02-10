package com.topsec.tsm.common.formatter;

import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

public class DeviceTypeFormatter implements PropertyFormatter{

	@Override
	public Object format(Object value) {
		return DeviceTypeNameUtil.getDeviceTypeName((String)value);
	}
}
