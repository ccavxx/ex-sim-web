package com.topsec.tsm.sim.node.service;

import java.sql.Blob;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.collector.datasource.DataSourceManage;
import com.topsec.tsm.collector.datasource.foramter.DataSourcesElementFormater;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.common.DoCommand;
import com.topsec.tsm.license.GetServerLicenseCodeUtil;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.node.ComponentConfigurationImpl;
import com.topsec.tsm.node.SegmentConfiguration;
import com.topsec.tsm.node.SegmentConfigurationImpl;
import com.topsec.tsm.sim.asset.DataSourceUtil;
import com.topsec.tsm.sim.asset.dao.DataSourceDao;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.dao.NodeMgrDao;
import com.topsec.tsm.sim.node.exception.ComponentNotFoundException;
import com.topsec.tsm.sim.node.exception.DataSourceException;
import com.topsec.tsm.sim.resource.persistence.Component;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.resource.persistence.Segment;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.sim.util.RouteUtils;
import com.topsec.tsm.util.actiontemplate.ActionTemplateException;
import com.topsec.tsm.util.xml.DefaultDocumentFormater;

public class NodeDeployServiceImpl implements NodeDeployService {
	private NodeMgrDao nodeMgrDao;
	private DataSourceDao dataSourceDao ;
	private static final Logger logger = LoggerFactory.getLogger(NodeDeployServiceImpl.class);
	
	public void setNodeMgrDao(NodeMgrDao nodeMgrDao) {
		this.nodeMgrDao = nodeMgrDao;
	}

	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		this.dataSourceDao = dataSourceDao;
	}

	@Override
	public void deleteDataSource(long dataSourceId) throws DataSourceException{
		SimDatasource dataSource = dataSourceDao.findById(dataSourceId) ;
		if (dataSource == null) {
			return ;
		}
		deleteDataSource(dataSource) ;
	}
	/**
	 * 卸载日志源
	 * @param dataSource
	 */
	public void unloadDataSource(SimDatasource dataSource){
		Component com = nodeMgrDao.getComponentWithSegments(dataSource.getComponentId());
		try {
			if (com == null) {
				return ;
			}
			Segment segment = getDataSourceSegment(com) ;
			DataSourcesElementFormater formatter = getFormatter(segment) ;
			if (formatter == null) {
				return ;
			}
			String id = String.valueOf(dataSource.getResourceId()) ;
			formatter.removeDataSource(id) ;
			formatter.putChange(id, DataSourceManage.DELETE) ;
			this.updateSegment(segment, formatter, dataSource.getComponentId()) ;
			Node oldDataSourceNode = nodeMgrDao.getNodeByNodeId(dataSource.getNodeId()) ;
			if(NodeUtil.isAgent(oldDataSourceNode.getType())){//收集节点是agent节点
				NodeUtil.sendCommand(NodeUtil.getRoute(oldDataSourceNode), MessageDefinition.CMD_DATASOURCE_UNLOAD, DataSourceUtil.toDataSource(dataSource)) ;
			}else if(NodeUtil.isCollector(oldDataSourceNode.getType())||NodeUtil.isAuditor(oldDataSourceNode.getType())){
				//收集节点是collector节点或者是auditor节点，如果是auditor节点可能是原来的旧数据有错，新版本的不应该存在auditor的可能
				Node auditorNode = nodeMgrDao.getNodeByNodeId(dataSource.getAuditorNodeId(), false, true, false, false) ;
				for(Node child:auditorNode.getChildren()){
					if(!NodeUtil.isAgent(child.getType())){
						NodeUtil.sendCommand(NodeUtil.getRoute(oldDataSourceNode), MessageDefinition.CMD_DATASOURCE_UNLOAD, DataSourceUtil.toDataSource(dataSource)) ;
					}
				}
			}
		} catch(ComponentNotFoundException e){
			logger.warn("卸载日志源失败，没有找到收集组件!") ;
		} catch (Exception e) {
			throw new DataSourceException("日志源更新失败,系统内部错误!",e) ;
		}
	}
	
	@Override
	public void deleteDataSource(SimDatasource dataSource) {
		Component com = nodeMgrDao.getComponentWithSegments(dataSource.getComponentId());
		try {
			if (com == null) {
				return ;
			}
			Segment segment = getDataSourceSegment(com) ;
			DataSourcesElementFormater formatter = getFormatter(segment) ;
			if (formatter == null) {
				return ;
			}
			String id = String.valueOf(dataSource.getResourceId()) ;
			formatter.removeDataSource(id) ;
			formatter.putChange(id, DataSourceManage.DELETE) ;
			this.updateSegment(segment, formatter, dataSource.getComponentId()) ;
			Node auditor = nodeMgrDao.getNodeByNodeId(dataSource.getAuditorNodeId(), false, true, false, false) ;
			DataSource deleteDataSource = DataSourceUtil.toDataSource(dataSource) ;
			//删除日志源
			if(SimDatasource.DATASOURCE_TYPE_LOG.equals(dataSource.getOwnGroup())){
				NodeUtil.sendCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_DATASOURCE_DELETE, deleteDataSource) ;
				NodeUtil.sendCommand(RouteUtils.getCollectorRoutes(auditor), MessageDefinition.CMD_DATASOURCE_DELETE, deleteDataSource) ;
				NodeUtil.sendCommand(RouteUtils.getIndexServiceRoutes(auditor), MessageDefinition.CMD_DATASOURCE_DELETE, deleteDataSource) ;
				NodeUtil.sendCommand(RouteUtils.getQueryServiceRoutes(auditor), MessageDefinition.CMD_DATASOURCE_DELETE, deleteDataSource) ;
			}else{
				NodeUtil.sendCommand(RouteUtils.getCollectorRoutes(auditor), MessageDefinition.CMD_MONITOR_DELETE, deleteDataSource) ;
				NodeUtil.sendCommand(NodeUtil.getRoute(auditor), MessageDefinition.CMD_MONITOR_DELETE, deleteDataSource) ;
			}
		}catch (ComponentNotFoundException e) {
			logger.warn("没有找到合适的收集组件") ;
		} catch (Exception e) {
			throw new DataSourceException("日志源删除失败,系统内部错误!",e) ;
		}
	}

	@Override
	public void sendDataSource(long dataSourceId)throws DataSourceException {
		sendDataSource(dataSourceDao.findById(dataSourceId)) ;
	}

	@Override
	public void sendDataSource(SimDatasource dataSource)throws DataSourceException {
		try {
			Component com = nodeMgrDao.getComponentWithSegments(dataSource.getComponentId());
			Segment segment = getDataSourceSegment(com) ;
			DataSourcesElementFormater formatter = getFormatter(segment) ;
			if (formatter == null) {
				formatter = new DataSourcesElementFormater() ;
			}
			formatter.addDataSource(DataSourceUtil.toDataSource(dataSource)) ;
			formatter.putChange(String.valueOf(dataSource.getResourceId()), DataSourceManage.ADD) ;
			this.updateSegment(segment, formatter, dataSource.getComponentId());
		}catch(XmlAccessException e){
			if(e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof ActionTemplateException){
				ActionTemplateException ate = (ActionTemplateException)e.getCause().getCause() ;
				if(ate.getErrorCode() == ActionTemplateException.DUPLICATE_ENTRY && 
				   ("file".equals(ate.getErrorObject()) || "directory".equals(ate.getErrorObject()))){
					throw new DataSourceException("文件路径重复！") ;
				}
			}
			throw new DataSourceException("日志源下发失败!",e) ;
		} catch (Exception e) {
			throw new DataSourceException("日志源下发失败!", e) ;
		}
	}

	@Override
	public void updateDataSource(long dataSourceId)throws DataSourceException, ComponentNotFoundException {
		updateDataSource(dataSourceDao.findById(dataSourceId)) ;
	}
	@Override
	public void updateDataSource(SimDatasource dataSource)throws DataSourceException, ComponentNotFoundException {
		updateDataSource(dataSource,null) ;
	}
	@Override
	public void updateDataSource(SimDatasource dataSource,SimDatasource oldDataSource)throws DataSourceException, ComponentNotFoundException {
		try {
			Component com = nodeMgrDao.getComponentWithSegments(dataSource.getComponentId());
			Segment segment = getDataSourceSegment(com) ;
			DataSourcesElementFormater formatter = getFormatter(segment) ;
			if (formatter == null) {
				return ;
			}
			String id = String.valueOf(dataSource.getResourceId());
			formatter.removeDataSource(id) ;
			formatter.addDataSource(DataSourceUtil.toDataSource(dataSource).changed()) ;
			formatter.putChange(id, DataSourceManage.UPDATE) ;
			this.updateSegment(segment, formatter, dataSource.getComponentId());
			if(oldDataSource != null && !oldDataSource.getNodeId().equals(dataSource.getNodeId())){//日志源新旧节点不一样需要重新卸载旧的节点上的日志源
				unloadDataSource(oldDataSource) ;
			}
		}catch(XmlAccessException e){
			if(e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof ActionTemplateException){
				ActionTemplateException ate = (ActionTemplateException)e.getCause().getCause() ;
				if(ate.getErrorCode() == ActionTemplateException.DUPLICATE_ENTRY && 
					("file".equals(ate.getErrorObject()) || "directory".equals(ate.getErrorObject()))){
					throw new DataSourceException("文件路径重复！") ;
				}
			}
			throw new DataSourceException("日志源下发失败!",e) ;
		}catch(ComponentNotFoundException e){
			throw e ;
		} catch (Exception e) {
			throw new DataSourceException("日志源下发失败!", e) ;
		}
	}
	/**
	 * 从Component中获取日志源Segment对象
	 * @param com
	 * @return
	 * @throws ComponentNotFoundException 
	 */
	private Segment getDataSourceSegment(Component com) throws ComponentNotFoundException{
		if(com==null){
			throw new ComponentNotFoundException() ;
		}
		Set<Segment> segments = com.getSegments();
		for (Segment segment : segments) {
			if (DataSourcesElementFormater.class.getName().equals(segment.getType())) {
				return segment ;
			}
		}
		throw new ComponentNotFoundException() ;
	}
	
	private DataSourcesElementFormater getFormatter(Segment segment)throws Exception{
		try {
			SegmentConfigurationImpl configuration = (SegmentConfigurationImpl) NodeUtil.toSegmentConfiguration(segment);
			DataSourcesElementFormater formater = (DataSourcesElementFormater) configuration.getConfiguration();
			return formater ;
		} catch (Exception e) {
			throw e ;
		}
	}
	
	private void updateSegment(Segment segment, DataSourcesElementFormater formater, long componentId) throws Exception {
		DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
		documentFormater.setFormater(formater);

		Blob blob = Hibernate.createBlob(documentFormater.exportObjectToString().getBytes());
		segment.setLastConfig(segment.getCurrentConfig());
		segment.setCurrentConfig(blob);
		nodeMgrDao.updateComponentSegment(segment);

		try{
			Node node = nodeMgrDao.getNodeByComponentId(componentId) ;
			Component component = nodeMgrDao.getComponentWithSegments(componentId) ;
			ComponentConfigurationImpl componentConfig = NodeUtil.toComponentConfiguration(component) ;
			DataSourcesElementFormater dipatchFormatter = null ;
			for(SegmentConfiguration segmentConfig:componentConfig.getSegments()){
				if(segmentConfig.getConfiguration() instanceof DataSourcesElementFormater){
					dipatchFormatter = (DataSourcesElementFormater)segmentConfig.getConfiguration() ;
					break ;
				}
			}
			if(dipatchFormatter != null){
				dipatchFormatter.setChanged(formater.getChanged()) ;
			}
			NodeUtil.getCommandDispatcher().sendCommand(
							NodeUtil.getRoute(node),
							MessageDefinition.CMD_NODE_SET_COMPONENT_CONFIGURATION,
							componentConfig);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
	}

}
