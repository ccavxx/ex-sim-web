/**
* 版权声明北京天融信科技有限公司，版权所有违者必究
*
* Copyright：Copyright (c) 2011
* Company：北京天融信科技有限公司
* @author 杨轩嘉（yang_xuanjia@topsec.com.cn）
* @since  2011-06-15
* @version 1.0
* 
*/
package com.topsec.tsm.sim.asset.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.asset.web.DataSourceStatus;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.dao.SimOrder;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;

/**
* 功能描述: 日志源Service层接口
*/
public interface DataSourceService{
	public static final String CMD_DATA_SOURCE="DataSource"; 
	/**cmd="Start"为启用的日志源列表*/
	public static final String CMD_START="Start";
	/**cmd="Stop"为停用的日志源列表*/
	public static final String CMD_STOP="Stop";
	/**获取所有日志源*/
	public static final String CMD_ALL = "DataSource" ;
	
	/**type="system"为系统日志源*/
	public static final String TYPE_SYSTEM="system";
	/**type="other"为非系统日志源*/
	public static final String TYPE_OTHER="other";

	/**日志源*/
	public static final String DATASOURCE_TYPE_LOG="log";
	/**
	* @method: getDataSourceTreeWithNodeList 
	* 		       得到日志源树集合
	* @author: 杨轩嘉(yang_xuanjia@topsec.com.cn)
	* @param:   type: 日志源类型 type="system"为系统日志源;  type="other"为非系统日志源; null为所有日志源
	* @return:  List<Map<String,Object>>:日志源集合   , dataSourceIp,securityObjectType,auditorNodeId,nodeIp,nodeAlias,dataSourceName
	* @exception: Exception
	*/
	public List<Map<String,Object>> getDataSourceTreeWithNodeList(String type) throws Exception;
	/**
	 * 获取日志源黑名单
	 * @return
	 */
	public List getBlackList();
	
	/**
	 * @method: getDataSource
	 *          查询启动的所有日志源
	 * @param cmd: 分类, 如果cmd="DataSource"为数据源列表;  cmd="Start"为启用的日志源列表;cmd="Stop"为禁用的日志源列表; 
	 * @param ownGroup
	 * @return
	 * @throws Exception
	 */
	public List<SimDatasource> getDataSource(String cmd);

	/**
	 * 根据ip获取对应的第一个日志源
	 * @param dataSource
	 * @return
	 */
	public SimDatasource getFirstByIp(String ip) ;
	
	/**
	 * 根据ip和日志源状态获取第一个日志源
	 * @param dataSource
	 * @return
	 */
	public SimDatasource getFirstByIp(String ip,DataSourceStatus status) ;
	/**
	 * 根据ip获取日志源列表
	 * @param ip
	 * @return
	 */
	public List<SimDatasource> getByIp(String ip) ;	
	/**
	 * 保存日志源信息
	 * @param datasource
	 * @throws ResourceNameExistException 
	 */
	public void save(SimDatasource datasource)throws ResourceNameExistException,ComponentNotFoundException ;
	/**
	 * 更新日志源
	 * @param dataSource
	 * @throws ResourceNameExistException
	 */
	public void update(SimDatasource dataSource)throws ResourceNameExistException,ComponentNotFoundException ;
	
	/**
	 * 删除日志源
	 * @param id
	 */
	public SimDatasource delete(long id) ;
	/**
	 * 根据ip删除日志源信息
	 * @param ip
	 */
	public void deleteByIp(String ip) ;
	/**
	 * 根据设备类型获取相应的日志源
	 * @param deviceType
	 * @return List<SimDatasource>
	 */
	public List<SimDatasource> getDataSourceByDvcType(String deviceType);
	
	/**
	 * 切换日志源状态
	 * @param id
	 * @param available
	 */
	public void switchState(Long id,Integer available) ;
	/**
	 * 根据id获取日志源或者监视对象id
	 * @param id
	 * @return
	 */
	public SimDatasource getById(Long id) ;
	/**
	 * 获取与指定归并规则关联的日志源
	 * @param aggId
	 * @return
	 */
	public List<SimDatasource> getByAggregatorId(Long aggId) ;
	
	public List<String> getDistinctDvcType(String cmd);
	/**
	 * 查找指定节点的日志源信息
	 * @param nodeId
	 * @return
	 */
	public List<SimDatasource> getByNodeId(String nodeId) ;
	/**
	 * 获取日志源类型列表
	 * @return
	 */
	public List<String> getDataSourceTypeList();
	/**
	 * 根据ip数据查询对象的日志源信息，new SimDatasource(long resourceId,String deviceIp, String securityObjectType,String nodeId)
	 * @param ipArray
	 * @return
	 */
	public List<SimDatasource> getByIPs(String[] ipArray) ;
	/**
	 * 根据日志源类型与IP查找日志源信息
	 * @param securityObjectType
	 * @param ip
	 * @return
	 */
	public SimDatasource findByDeviceTypeAndIp(String securityObjectType,String ip);
	/**
	 * 获取所有的日志源信息
	 * @param includeMonitor 是否包含监视对象
	 * @param includeAuditLog 是否包含审计日志
	 * @param includeSystemLog 是否包含系统日志
	 * @return
	 */
	public List<SimDatasource> getAll(boolean includeMonitor,boolean includeAuditLog,boolean includeSystemLog) ;
	
	/**
	 * 返回用户有权限的日志源列表,如果是operator账号，会包含系统日志<br>
	 * operator返回所有的日志源<br>
	 * auditor返回审计日志<br>
	 * admin返回所有日志源<br>
	 * @param sid
	 * @return
	 */
	public List<SimDatasource> getUserDataSource(SID sid) ;
	/**
	 * 返回用户有权限的日志源列表,如果是operator账号，会包含系统日志
	 * @param sid
	 * @return
	 */
	public List<SimDatasource> getUserDataSource(String userName) ;
	/**
	 * 返回用户有权限的日志源列表<br>
	 * operator返回所有的日志源<br>
	 * auditor返回审计日志<br>
	 * admin返回所有日志源<br>
	 * @param sid 用户sid信息
	 * @param operatorExcludeSystemLog如果是operator账号是否将系统日志排除<br>
	 * @return
	 */
	public List<SimDatasource> getUserDataSource(SID sid,boolean operatorExcludeSystemLog) ;
	/**
	 * 返回用户具有权限的日志源列表
	 * @param userName
	 * @return
	 */
	public List<SimDatasource> getUserDataSource(String userName,boolean operatorExcludeSystemLog) ;
	/**
	 * 返回用户有权限的日志源列表，每条记录表示一个日志源，格式(ip和类型中间用逗号分割):deviceIp,securityObjectType
	 * @param sid
	 * @param operatorExcludeSystemLog
	 * @return
	 */
	public List<String> getUserDataSourceAsString(SID sid,boolean operatorExcludeSystemLog) ;
	/**
	 * 判断日志源名称是否存在
	 * @param name
	 * @return
	 */
	boolean isResourceNameExist(String name);
	
	/**
	 * 获取日志源列表（分页）
	 * @param sid 
	 */
	PageBean<SimDatasource> getList(SID sid, int pageIndex, int pageSize, Map<String, Object> searchCondition, SimOrder... orders);
}

