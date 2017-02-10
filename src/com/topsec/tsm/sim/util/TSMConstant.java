/**
 * Copyright ? 2005 Bejing Topsec Co. Ltd.
 * All right reserved.
 */
package com.topsec.tsm.sim.util;


/**
 * 常量类
 * 
 * @author: 杨圣峰
 * @version $Revision: 1.3 $
 */
public class TSMConstant {

    public final static String RESOURCE_FILE = "ApplicationResource";

    // 动作
    public final static String ACTION_NAME = "action";

    public final static String ADD = "add";

    public final static String UPDATE = "update";

    public final static String DELETE = "delete";
    
    public final static String DELETEALL = "deleteAll";

    public final static String SELECT = "select";
    
    public final static String SAVEAS = "saveas";

    public final static String GET_ROOT = "getRoot";

    public final static String GET_NODE = "getNode";

    public final static String GET_CHILDREN = "getChildren";

    public final static String TREE_ERROR = "tree_error";

    //
    public final static String FAILURE = "failure";

    public final static String SUCCESS = "success";

    public final static String SELF_ID = "selfId";

    public final static String PARENT_ID = "parentId";

    // public final static String GROUP_ID ="groupId";

    public final static String RESOURCE_NAME = "resourceName";

    public final static String RESOURCE_ID = "resourceId";

    public final static String RESOURCE_TYPE = "resourceType";

    public final static String RESOURCE_PAGE = "resourcePage";

    public final static String RESOURCE_FORWARD = "resourceForward";

    public final static String FRESH_TREE = "fleshTree";
    
    public final static String GROUP_OP_RES = "groupOpRes";


    public final static String RESOURCE_ISGROUP = "isGroup";

    public final static int PAGE_SIZE = 10;

    // 集合名
    public final static String ROOTS = "roots";

    public final static String GROUPS = "groups";

    public final static String MEMBERS = "members";

    public final static String ASSETS = "assets";

    public final static String GROUP = "group";

    public final static String MEMBER = "member";

    public final static String ASSET = "asset";

    // 排序
    public final static String DESC = "desc";

    public final static String ASC = "asc";

    // jndi
    // public final static String JNDI_RESOURCE_FILE
    // ="com.topsec.tsm.ui.framework.service.jndi";

    public final static String JNDI_RESOURCE_FILE = "../../../../conf/sim_jndi.properties";
    public final static String AUTH_JNDI_RESOURCE_FILE ="../../../../conf/auth_jndi.properties";


    
    public final static String GET_PAGE_INFO = "getPageInfo";   // 获取树节点对应页面信息
    //add by wangxinbing
    public static final String UPLOAD_LOG = "uploadlog.dat";

	public static final String PATH_KEY = "sys_cfg";

}