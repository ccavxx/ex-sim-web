package com.topsec.tsm.sim.asset.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.AssetNameExistException;
import com.topsec.tsm.ass.InvalidAssetIdException;
import com.topsec.tsm.ass.persistence.Device;
import com.topsec.tsm.ass.service.DeviceServiceImpl;
import com.topsec.tsm.auth.manage.AuthAccount;
import com.topsec.tsm.auth.manage.AuthUserDevice;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.auth.dao.UserDao;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeDeployService;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;

public class AssetServiceImpl extends DeviceServiceImpl implements AssetService{
	
	private DataSourceService dataSourceService ;
	private DataSourceService monitorService ;
	private UserDao userDao ;
	
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	public void setMonitorService(DataSourceService monitorService) {
		this.monitorService = monitorService;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public void update(Device device) throws AssetNameExistException {
		Device oldDevice = getDeviceDao().getTransient(device.getId()) ;
		if(device.getEnabled()==0){//如果禁用资产，要同时禁用相应的日志源和监视对象
			changeState(device, device.getEnabled(), true) ;
		}
		//新旧管理节点不同需要更改日志源节点信息，然后重新下发日志源对象给新的节点
		if(oldDevice != null  && !oldDevice.getScanNodeId().equals(device.getScanNodeId())){
			NodeMgrFacade nodeMgr = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			Node newNode = NodeUtil.getCollectNode(device.getScanNodeId(), nodeMgr) ;
			NodeDeployService nodeDeployService = (NodeDeployService) SpringContextServlet.springCtx.getBean("nodeDeployService") ;
			List<SimDatasource> allDataSource = dataSourceService.getByIp(device.getMasterIp().toString()) ;
			if(allDataSource != null){
				for(SimDatasource ds:allDataSource){
					try {
						SimDatasource oldDataSource = (SimDatasource) BeanUtils.cloneBean(ds) ;
						Map<String,Object> newCollectComponent = NodeUtil.getComponentByCollectMethod(newNode,ds.getCollectMethod()) ;
						ds.setAuditorNodeId((String)newCollectComponent.get("auditorNodeId")) ;
						ds.setNodeId((String)newCollectComponent.get("nodeId")) ;
						ds.setComponentId((Long)newCollectComponent.get("componentId")) ;
						nodeDeployService.updateDataSource(ds,oldDataSource) ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}
			List<SimDatasource> allMonitor = monitorService.getByIp(device.getMasterIp().toString()) ;
			if (allMonitor != null) {
				for(SimDatasource ds:allMonitor){
					try {
						SimDatasource monitor = (SimDatasource) BeanUtils.cloneBean(ds) ;
						Map<String,Object> newCollectComponent = NodeUtil.getComponentByCollectMethod(newNode,ds.getCollectMethod()) ;
						ds.setAuditorNodeId((String)newCollectComponent.get("auditorNodeId")) ;
						ds.setNodeId((String)newCollectComponent.get("nodeId")) ;
						ds.setComponentId((Long)newCollectComponent.get("componentId")) ;
						nodeDeployService.updateDataSource(ds,monitor) ;
					}catch(Exception e){
						throw new RuntimeException(e) ;
					}
				}
			}
		}
		super.update(device);
	}

	@Override
	public Device deleteDevice(String id) throws InvalidAssetIdException {
		Device device = super.deleteDevice(id);
		dataSourceService.deleteByIp(device.getMasterIp().toString()) ;
		monitorService.deleteByIp(device.getMasterIp().toString()) ;
		userDao.deleteAuthDeivce(id) ;
		return device ;
	}

	@Override
	public void changeState(Integer state) {
		List<Device> allDevice = getAll() ;
		if(allDevice == null) return ;
		for(Device device:allDevice){
			try {
				changeState(device, state,true) ;
			} catch (InvalidAssetIdException e) {
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void save(Device device) throws AssetNameExistException {
		super.save(device);
		SID sid = SID.currentUser() ;
		if(!sid.isDefaultUser() && sid.hasOperatorRole()){
			AuthAccount account = userDao.findById(sid.getAccountID()) ;
			Set<AuthUserDevice> userDevices = account.getUserDevice() ;
			if (userDevices == null) {
				account.setUserDevice(userDevices = new HashSet<AuthUserDevice>()) ;
	 		}
			AuthUserDevice userDevice = new AuthUserDevice(device.getName(),device.getScanNodeId(),device.getDeviceType(), device.getMasterIp().toString(), device.getId()) ;
			userDevices.add(userDevice) ;
			userDao.update(account) ;
			sid.setUserDevice(userDevices) ;
		}
		AssetFacade.getInstance().addAssetFromDevice(device);
	}

	@Override
	public void changeState(String id, Integer state,boolean cascade)throws InvalidAssetIdException {
		Device device = getDevice(id) ;
		if (device == null) {
			throw new InvalidAssetIdException(id) ;
		}
		changeState(device, state,cascade) ;
	}
	
	private void changeState(Device device,Integer state,boolean cascade){
		if(!state.equals(device.getEnabled())){
			super.changeState(device.getId(), state,cascade);
		}
		if(cascade){
			List<SimDatasource> deviceDataSource = dataSourceService.getByIp(device.getMasterIp().toString()) ;
			for(SimDatasource dataSource:deviceDataSource){
				if(state.equals(dataSource.getAvailable())){
					continue ;
				}
				dataSourceService.switchState(dataSource.getResourceId(), state) ;
			}
			List<SimDatasource> deviceMonitor = monitorService.getByIp(device.getMasterIp().toString()) ;
			for(SimDatasource monitor:deviceMonitor){
				if(state.equals(monitor.getAvailable())){
					continue ;
				}
				monitorService.switchState(monitor.getResourceId(), state) ;
			}
		}
	}
}
