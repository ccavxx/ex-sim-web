package com.topsec.tsm.sim.log;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.index.HistIndexer;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.node.support.AuditRecordHelper;
import com.topsec.tsm.sim.log.web.LogHistoryController;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.util.SystemInfoUtil;



public class IndexingThread extends Thread {
//	private HistoryLogAction action;
	private String _sourcePath;
	private ServletContext _sc;
	private static final Logger logger = LoggerFactory
			.getLogger(	IndexingThread.class);
//	private String _role;
	private static boolean busy = false;
	
	public static List<File> indexdFileList = new CopyOnWriteArrayList<File>();
	public IndexingThread(LogHistoryController action,String sourcePath) {
		super();
		// TODO Auto-generated constructor stub
//		this.action=action;
		this._sourcePath=sourcePath;
//		this._sc=sc;
//		this._role = role;
	}

	@Override
	public void run() {

		busy = true;
		try {
		File file = new File(this._sourcePath);

		File[] files = file.listFiles(new LogFilter());
		List<File> fileList = Arrays.asList(files);
		Collections.sort(fileList, new LogComparator());

		for (File f : fileList) {
			if (SystemInfoUtil.getInstance().isDataHomeFull()) {
				AuditRecord log = AuditLogFacade.createSystemAuditLog().sysNotify("历史日志查询", "警告：磁盘空间不足，停止历史日志索引！", true) ;
				AuditLogFacade.send(log) ;
				logger.warn("磁盘空间不足，停止历史日志索引。");
				break;
			}
			try {
				File indexFile = indexing(f.getAbsolutePath());
			}catch(Exception e) {
				break;
			}

		}
		}finally{
			busy = false;
		}
	}
	
	
	//create index.
	private File indexing(String sourcePath) throws Exception{
		HistIndexer indexer = null;
		try {
			indexer = new HistIndexer(sourcePath);
	    	indexer.indexLog();
	    	indexer.closeIndex();
				

	    	return indexer.getIndexFile();
	    	
		
		}finally {
			if(indexer != null){
				indexer.closeIndex();
				
			}
		}
	}
	
	public static boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy){
		this.busy = busy;
	}
	
}
