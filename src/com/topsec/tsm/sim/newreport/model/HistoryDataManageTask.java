package com.topsec.tsm.sim.newreport.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.util.ticker.Tickerable;

/**
 * @ClassName: HistoryDataManageTask
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2016年10月14日上午10:35:31
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class HistoryDataManageTask implements Tickerable {

	private static ExecutorService threadPool ;
	private static boolean isexe=false;
	private NodeMgrFacade nodeMgrFacade;
	private DataSourceService dataSourceService;
	private DataSourceService monitorService;
	public void setDataSourceService(DataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

	public DataSourceService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(DataSourceService monitorService) {
		this.monitorService = monitorService;
	}

	@Override
	public void onTicker(long tickerInterval) {
		
		Date date=new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		if (hour == 1 && !isexe) {
			try {
				Node node = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_REPORTSERVICE, false, false, false, false).get(0) ;
				if (null ==node || NodeStatusQueueCache.offline(node.getNodeId())) {
					return;
				}
			} catch (Exception e) {
				return;
			}
			List<SimDatasource> datasources = dataSourceService.getDataSource(DataSourceService.CMD_ALL);
			List<SimDatasource> monitorsources = monitorService.getDataSource(DataSourceService.CMD_ALL);

			if (null == datasources || datasources.size() == 0) 
				return;

			if(threadPool == null){
				synchronized (HistoryDataManageTask.class) {
					if(threadPool == null){
						threadPool = Executors.newFixedThreadPool(3, new ThreadFactory() {
							
							@Override
							public Thread newThread(Runnable runnable) {
								
								return new Thread(runnable, "DELETE-HISTORY-DATA");
							}
						});
					}
				}
			}
			
			for (SimDatasource simDatasource : datasources) {
				threadPool.execute(new ExecuteRunning(simDatasource));
			}
			for (SimDatasource simDatasource : monitorsources) {
				threadPool.execute(new ExecuteRunning(simDatasource));
			}
			isexe=true;
		}else if(hour < 1){
			isexe=false;
		}
	}
	
	class ExecuteRunning implements Runnable{

		private SimDatasource datasource;
		
		public ExecuteRunning(SimDatasource datasource) {
			super();
			this.datasource = datasource;
		}

		@Override
		public void run() {
			new ReportDataManagerHandler().removeHistoryByDatasource(datasource);
		}
		
	}

}
