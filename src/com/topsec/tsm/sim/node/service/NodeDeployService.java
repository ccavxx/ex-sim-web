package com.topsec.tsm.sim.node.service;

import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.node.exception.DataSourceException;

public interface NodeDeployService {

	/**
	 * 删除日志源
	 * @param dataSource
	 */
	public void deleteDataSource(SimDatasource dataSource) ;
	
	/**
	 * 删除日志源
	 * @param dataSourceId
	 */
	public void deleteDataSource(long dataSourceId)throws DataSourceException ;
	/**
	 * 下发日志源对象
	 */
	public void sendDataSource(long dataSourceId)throws DataSourceException ;
	/**
	 * 下发日志源对象
	 * @param datasource
	 */
	public void sendDataSource(SimDatasource datasource)throws DataSourceException ;
	/**
	 * 更新日志源对象
	 * @param dataSourceId
	 */
	public void updateDataSource(long dataSourceId)throws DataSourceException,ComponentNotFoundException ;
	/**
	 * 更新日志源对象
	 * @param datasource
	 */
	public void updateDataSource(SimDatasource datasource)throws DataSourceException,ComponentNotFoundException ;
	/**
	 * 更新日志源对象
	 * @param dataSource
	 * @param oldDataSource
	 * @throws DataSourceException
	 */
	public void updateDataSource(SimDatasource dataSource,SimDatasource oldDataSource)throws DataSourceException,ComponentNotFoundException;
}
