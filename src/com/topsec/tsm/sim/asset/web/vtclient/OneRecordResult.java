package com.topsec.tsm.sim.asset.web.vtclient;

import java.util.ArrayList;
import java.util.List;

/**
 * 只包含一条记录的输出结果
 * @author hp
 *
 */
public class OneRecordResult extends TableResult {

	public OneRecordResult(String header, Object result) {
		super(new ArrayList<String>(1),new ArrayList<Object>(1));
		getHeader().add(header) ;
		((List)getResult()).add(new Object[]{result}) ;
	}
	
}
