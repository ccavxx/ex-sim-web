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
package com.topsec.tsm.sim.asset.dao;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.asset.web.DataSourceStatus;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.util.CollectorType;

/**
* 功能描述: 日志源Dao层接口
*/
public interface DataSourceDao extends BaseDao<SimDatasource, Long>{
	
	/**
	 * 根据ip获取日志源列表
	 * @param ip
	 * @return
	 */
	public List<SimDatasource> getByIp(String ip) ;
	
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
	 * @method: getDataSource
	 *          查询启动的所有日志源
	 * @param cmd: 分类, 如果cmd="DataSource"为数据源列表;  cmd="Start"为启用的日志源列表;cmd="Stop"为禁用的日志源列表; 
	 * @param ownGroup
	 * @return
	 * @throws Exception
	 */
	public List<SimDatasource> getDataSource(String cmd);
	/**
	 * 判断日志源名称是否已经存在
	 * @param name
	 * @return
	 */
	public boolean isNameExist(String name) ;
	
	public boolean isNameExist(SimDatasource dataSource) ;
	
	public List<SimDatasource> getDatasourcesByRuleId(Long id);
	/**
	 * 根据日志源状态获取第一个日志源
	 * @param ip
	 * @param status
	 * @return
	 */
	public SimDatasource getFirstByIp(String ip, DataSourceStatus status);
	/**
	 * 根据设备类型获取相应的日志源
	 * @param deviceType
	 * @return List<SimDatasource>
	 */
	public List<SimDatasource> getByDvcType(String deviceType);

	public SimDatasource updateState(Long id, Integer available);
	/**
	 * 根据ip、日志源类型，所属结点，所属组，判断一个日志是否已经存在
	 * @param ip
	 * @param securityObjectType
	 * @param nodeId
	 * @param group
	 * @return
	 */
	public boolean exist(String ip,String securityObjectType,String nodeId,String group) ;

	public List<SimDatasource> getByAggregatorId(Long aggId);
	/**
	 * 获取一个游离(hibernate中的状态)状态的对象
	 * @param id
	 * @return
	 */
	public SimDatasource getTransient(Long id) ;
	
	public List<String> getDataSourceTypeList();
	
	public List<SimDatasource> getByIPs(String[] ipArray);
	
	public SimDatasource findByDeviceTypeAndIp(String securityObjectType,String ip);
	
	public List<SimDatasource> getDataSourceListByDeviceType(String type);
	/**
	 * 获取所有的日志源和监视对象
	 * @return
	 */
	public List<SimDatasource> getAll(boolean includeMointor,boolean includeAuditLog,boolean includeSystemLog);
	
	public List<SimDatasource> getAllDataSource(boolean includeAuditLog,boolean includeSystemLog);
	
	public List<SimDatasource> getUserDataSource(SID sid) ;
	
	public boolean exist(SimDatasource datasource, CollectorType collectType);

	public List<SimDatasource> getByNodeId(String nodeId);
}
