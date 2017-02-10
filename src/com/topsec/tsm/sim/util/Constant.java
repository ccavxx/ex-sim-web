package com.topsec.tsm.sim.util;

public class Constant {
	public static final int NODE_STATE_WRITE=0;//白名单
	public static final int NODE_STATE_GRAY=1;//灰名单
	public static final int NODE_STATE_BLACK=2;//黑名单
	public static final String QUERY_TOTAL="total";
	public static final String QUERY_RESULT="result";
	
	public static final int NODE_RESOURCE_TYPE="SOC".hashCode();
	public static final int FLOW_RESOURCE_TYPE="FLW".hashCode();
	public static final int COMPONENT_RESOURCE_TYPE="COM".hashCode();
	public static final int SEGMENT_RESOURCE_TYPE="SEG".hashCode();
	
	public static final String LIVECONTROLSERVICE="LiveControlService";
}
