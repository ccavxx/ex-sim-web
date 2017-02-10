package com.topsec.tsm.ui.topo.util;

import java.util.HashMap;
import java.util.Map;

public class TopoConstant {

	public static final String TEXT = "text";
	public static final String RECT = "rect";
	public static final String TSPAN = "tspan";
	public static final String IMAGE = "image";
	public static final String CIRCLE = "circle";
	public static final String LINE = "line";
	public static final String POLY_LINE ="polyline";
	public static final String MODEL_DOMAIN = "domain";
	public static final String MODEL_DEVICE = "device";
	public static final String DEFAULT_SAFE_DOMAIN_ID = "301";

	// public static final String TOPO_TEMPLATE_TYPE_NODES = "nodesTemplate";
	// public static final String TOPO_TEMPLATE_TYPE_DOMAINS =
	// "domainsTemplate";

	public static final String TOPO_VTYPE_DEFAULT = "defaultArea";
	public static final String TOPO_VTYPE_AREA = "areaType";
	public static final String TOPO_VTYPE_DOMAIN = "domainType";

	public static final String TOPO_JUMP_CMD_DEFAULT = "defaultJump";
	public static final String TOPO_JUMP_CMD_NEXT = "nextJump";
	public static final String TOPO_JUMP_CMD_PREVIOUS = "previousJump";
	public static final String TOPO_JUMP_CMD_FRIEND = "friendJump";
  
	public static final String TOPO_VALVE = "valve";

	public static final String TOPO_MAPNAME_SHAN_XI = "ShanXi";

	public static final String EVENT_TIER_ID = "AGT_TIER_ID";
	public static final String EVENT_UUID = "UUID";

	public static final String TOPSEC_FIREWALL_TOS = "Firewall/Topsec/TOS";
	public static final String TOPSEC_FIREWALL_NGF4000 = "Firewall/Topsec/NGFW4000";
	public static final String TOPSEC_FIREWALL_NGF4000_005 = "Monitor/Snmp/Topsec/NGFW4000/V3.3.005";

	public static final String TOPO_ROOT_NODE = "rootNode";
	public static final String TOPO_SUB_NODE = "subNode";

	public static final String TOPO_DEFAULT_USER = "admin";

	public static final String TOPO_CMD_EVT_RPS_REGISTER = "topoCmdEvtRegister";
	public static final String TOPO_CMD_EVT_RPS_GATHER = "topoCmdEvtGather";
	public static final String TOPO_CMD_EVT_RPS_SYNC_TOUP = "topoCmdEvtSyncToUp";
	public static final String TOPO_CMD_EVT_RPS_CLEAR = "topoCmdEvtClear";
	public static final String TOPO_PARAM_EVT_RPS = "topoParamRps";

	public static final String TOPO_EVT_RPS_SRCSERVER_IP = "srcRpsServerIp";
	public static final String TOPO_CMD_SRCSERVER_IP = "srcCmdServerIp";
	
	
	//����evt�ֶγ���
	public static final String COL_ASSET_ID = "DVC_ASSET_ID";
	public static final String COL_EVT_UUID = TopoConstant.EVENT_UUID;
	public static final String COL_NAME = "NAME";
	public static final String COL_START_TIME = "START_TIME";
	public static final String COL_END_TIME = "END_TIME";
	public static final String COL_DVC_TYPE = "DVC_TYPE";
	public static final String COL_EVENT_TYPE = "EVENT_TYPE";
	public static final String COL_MESSAGE = "MESSAGE";
	public static final String COL_DEST_ADDRESS = "DEST_ADDRESS";
	public static final String COL_DEST_PORT = "DEST_PORT";
	public static final String COL_SRC_ADDRESS = "SRC_ADDRESS";
	public static final String COL_SRC_PORT = "SRC_PORT";
	public static final String COL_DVC_ADDRESS = "DVC_ADDRESS";
	
	
	
	
	
	
	

	// ����豸������͵ı�Ƿ�ţ���ҳ���������ã�
	public static final String TOPO_PARAM_TAG = "#@";
	public static final String ENABLE_EVT_BAK = "ENABLE_EVT_BAK";
	public static final String MAXLINE_EVT_BAK = "MAXLINE_EVT_BAK";
	public static final String DEBUG_BAKEVT_CONF = "debug_bakEvt_Conf";


	public static final Map<String, String> DEV_STATUS_DEFS = new HashMap<String, String>();
	public static final Map<String, String> EVT_LEVEL_DEFS = new HashMap<String, String>();

	static {
		DEV_STATUS_DEFS.put("CPUʹ����", "CPU_USED_PERCENT");
		DEV_STATUS_DEFS.put("�ڴ�", "MEM_USED_PERCENT");
		DEV_STATUS_DEFS.put("������", "SESSION_COUNT");
		DEV_STATUS_DEFS.put("�Ựʹ����", "SESSION_PERCENT");
		DEV_STATUS_DEFS.put("Ӳ��", "DISK_USED_PERCENT");

		DEV_STATUS_DEFS.put("��ȡ����", "CUSTOM_INTEGER1");
		DEV_STATUS_DEFS.put("ת������", "CUSTOM_INTEGER2");
		DEV_STATUS_DEFS.put("��ȡ�ֽ�", "CUSTOM_INTEGER3");
		DEV_STATUS_DEFS.put("ת���ֽ�", "CUSTOM_INTEGER4");
		DEV_STATUS_DEFS.put("��ȡ����", "CUSTOM_INTEGER5");
		DEV_STATUS_DEFS.put("ת������", "CUSTOM_INTEGER6");

		EVT_LEVEL_DEFS.put("�ǳ���", "0");
		EVT_LEVEL_DEFS.put("�ϵ�", "1");
		EVT_LEVEL_DEFS.put("��", "2");
		EVT_LEVEL_DEFS.put("�ϸ�", "3");
		EVT_LEVEL_DEFS.put("��", "4");

	}

	public static Map<String, String> getDevStatusDefs() {
		return DEV_STATUS_DEFS;
	}

	public static Map<String, String> getEvtTypeDefDic() {
		return EVT_LEVEL_DEFS;
	}

}
