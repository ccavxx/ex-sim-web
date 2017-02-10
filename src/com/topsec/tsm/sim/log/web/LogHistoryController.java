package com.topsec.tsm.sim.log.web;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPFile;
import com.topsec.tal.base.archive.ArchiveFileReader;
import com.topsec.tal.base.search.HistLogInfo;
import com.topsec.tal.base.util.FTPUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.util.config.Block;
import com.topsec.tal.base.util.config.Config;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.exception.CommonUserException;
import com.topsec.tsm.sim.log.IndexingThread;
import com.topsec.tsm.sim.log.LogComparator;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.response.persistence.Response;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.tal.response.base.RespCfgHelper;
import com.topsec.tsm.tal.service.EventResponseService;
import com.topsec.tsm.util.SystemInfo.Usage;
import com.topsec.tsm.util.SystemInfoUtil;
import com.topsec.tsm.util.io.ZipUtils;
import com.topsec.tsm.util.net.FtpUploadUtil;

@Controller
@RequestMapping("logHistory")
public class LogHistoryController {
	
	private NodeMgrFacade nodeMgrFacade;
	private EventResponseService eventResponseService;
	private IndexingThread thread = null;
	private FTPClient client = null;
	private String ftpHome = "/";
	private String basePath;
	
	@Autowired
	public void setNodeMgr(NodeMgrFacade nodeMgr) {
		this.nodeMgrFacade = nodeMgr;
	}
	
	@Autowired
	public void setEventResponseService(EventResponseService eventResponseService) {
		this.eventResponseService = eventResponseService;
	}
	
	/**
	 * uploadFile　上传日志备份文件
	 * @author zhou_xiaohu@topsec.com.cn
	 * @param request
	 * @param response
	 * @param uploadFfile
	 * @throws Exception
	 */
	@RequestMapping(value="uploadFile")
	public void uploadFile(SID sid,HttpServletRequest request, HttpServletResponse response, @RequestParam("uploadFfile") MultipartFile uploadFfile) throws Exception {
		Result result = new Result();
		//判断上传文件的大小
		long uploadFileLimit = StringUtil.toLongNum(System.getProperty("IMPORT.LOG.LIMIT"),1) ; 
		if (uploadFfile.getSize() >= uploadFileLimit * FileUtils.ONE_GB)  {
			result.buildError("上传文件太大，超出上限({}G)",uploadFileLimit) ;
		}else{
			String path = getDataHome() + File.separator + "tmp";
			File pathFile = new File(path);
			FileUtils.forceMkdir(pathFile);
			String tempBaseFile = path + File.separator + uploadFfile.getOriginalFilename();
			File basefile = new File(tempBaseFile);
			//将上传的文件写入指定的文件夹
			uploadFfile.transferTo(basefile);
			request.setAttribute("filePath", basefile.getAbsolutePath());
			request.setAttribute("fileLocation", "local");
			result = (Result)createIndex(sid,request);
			FileUtils.deleteQuietly(basefile) ;
			Thread.sleep(2000);//防止客户端请求太快
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html") ;//此处一定不要将text/html写成text/html;charset=utf-8
		response.getWriter().print(JSON.toJSON(result)) ;
	}
	
	/**
	 *	createIndex:创建日志索引
	 * @author zhou_xiaohu@topsec.com.cn
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("createIndex")
	@ResponseBody
	public Object createIndex(SID sid,HttpServletRequest request)throws Exception {
		SearchHistoryLogController.clearCache();
		String currentLogPath = getDataHome();
		int userId = sid.getAccountID();
		Result result = new Result();
		Usage usage = SystemInfoUtil.getInstance().getDiskUsage(new String[]{currentLogPath}) ;
		if(usage == null){
			return result.buildError("获取磁盘存储空间失败！") ;
		}
		int minAvailableSpace = 5 ;
		if (usage.availableGB() < minAvailableSpace) {
			return result.buildError("导入失败，磁盘空间不足"+minAvailableSpace+"G！");
		}
		File[] temDatFiles= null ;
		try{
			String tempLogPath = currentLogPath+"/events/temp/"+userId ;
			String tempIndexPath = currentLogPath+"/indexes/temp/"+userId ;
			File tempLogDir = new File(tempLogPath) ;
			File tempIndexDir = new File(tempIndexPath) ;
			FileUtils.deleteQuietly(tempLogDir) ;
			FileUtils.deleteQuietly(tempIndexDir) ;
			FileUtils.forceMkdir(tempLogDir);
			FileUtils.forceMkdir(tempIndexDir);
			if ("local".equals(request.getAttribute("fileLocation"))) {
				String filePath = (String) request.getAttribute("filePath");
				File zipFile = new File(filePath);
				if(!zipFile.getName().endsWith(".zip")) {
					return result.buildError("文件不是zip文件！");
				}
				ZipUtils.unzipDirectory(zipFile.getAbsolutePath(), tempLogPath, true);
			} else {// create index by a file on server
				String localBackupPath = getLocalBackupPath();
				String[] remoteFilePath =request.getParameter("filePath").split(",");
				for (int i = 0; i < remoteFilePath.length; i++) {
					if (localBackupPath == null) {
						downFTPFile(remoteFilePath[i], tempLogPath);
					} else {
						copyLocalFile(remoteFilePath[i], tempLogPath);
					}
					// 解压文件
					File[] zipFiles = tempLogDir.listFiles((FilenameFilter)new SuffixFileFilter(".zip"));
					for (File data : zipFiles) {
						try {
							ZipUtils.unzipDirectory(data.getAbsolutePath(),	tempLogPath, false);
						} catch(Exception e) {
							e.printStackTrace();
							System.out.println("文件"+data.getName()+"解压失败！");
						}
						FileUtils.deleteQuietly(data);
					}
				}
				// 检查解压后的文件格式是否正确
			}
			temDatFiles = tempLogDir.listFiles((FilenameFilter)new SuffixFileFilter(".dat"));
			// 检查解压后的文件格式是否正确
			if (temDatFiles != null && temDatFiles.length == 0) {
				return result.buildError("zip文件中不包含日志文件！") ;
			}
			HistLogInfo logInfo = getLogInfo(tempLogPath);
			if(logInfo.getType() == null || logInfo.getHost() == null){
				return result.buildError("读取日志文件失败，文件已损坏！") ;
			}
			if(!sid.accessible(logInfo.getHost(), logInfo.getType())){
				return result.buildError("当前用户无权查看主机{}的{}日志",logInfo.getHost(),DeviceTypeNameUtil.getDeviceTypeName(logInfo.getType())) ;
			}
			thread = new IndexingThread(this,tempLogPath);
			thread.setBusy(true);
			thread.start();
			result.buildSuccess(null);
		}catch(CommonUserException e){
			result.buildError(e.getMessage()) ;
		}catch(Exception e){
			e.printStackTrace();
			result.buildError("导入日志文件失败！");
		}
		return result;
	}
	
	private String getLocalBackupPath() throws Exception {
		Response resp = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
		Config config = RespCfgHelper.getConfig(resp);
		if (config == null) {
			return null;
		}
		Block backupBlock = config.getBlockbyGroup("backuppath");
		String blockKey = backupBlock.getKey();
		if (blockKey.compareToIgnoreCase("local") == 0) {
			String path = backupBlock.getItemValue("path");
			if (!path.endsWith(File.separator))
				path = path + File.separator;
			return path;
		}
		return null;
	}

	/*@author:丁广富
	 * @param logHome
	 * @return HistLogInfo
	 */
	public HistLogInfo getLogInfo(String logHome){
		HistLogInfo logInfo = new HistLogInfo();
		File file = new File(logHome);
		File[] files = file.listFiles(new com.topsec.tsm.sim.log.LogFilter());
		if(files == null){
			return logInfo;
		}
		List<File> fileList = Arrays.asList(files);
		Collections.sort(fileList, new LogComparator());
		if (fileList.size() > 0) {
			ArchiveFileReader reader = null;
			try {
				reader = new ArchiveFileReader(fileList.get(0).getAbsolutePath());
				logInfo.setType(reader.getType());
				logInfo.setHost(reader.getIp());
				logInfo.setStartTime(reader.getStartTime());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			}
			try {
				reader = new ArchiveFileReader(fileList.get(fileList.size() - 1).getAbsolutePath());
				logInfo.setEndTime(reader.getEndTime());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null)
					reader.close();
			}

		}
		return logInfo;
		
	}
	/**
	 * getDataHome 获取数据存储路径
	 * @return
	 * @throws Exception
	 */
	public String getDataHome()throws Exception {
		Node node = NodeUtil.getNodeByType(nodeMgrFacade, NodeDefinition.NODE_TYPE_INDEXSERVICE);
		String nodeId = node.getNodeId();
		List<Response> responses = eventResponseService.getResponsesByNodeId(nodeId);
		Config config = null;
		for (Response res : responses) {
			if ("sys_cfg_store".equals(res.getCfgKey())) {
				config = RespCfgHelper.getConfig(res);
				break ;
			}
		}
		String path = null;
		Block archive_path = config.getBlockbyKey("archive_path");
		if ("archive_path".equalsIgnoreCase(archive_path.getKey())) {
			path = archive_path.getItemValue("archive_path");
		}
		return path;
	}
	
	private void copyLocalFile(String fromPath, String targetDir) throws Exception {
		File from = new File(fromPath);
		if (from.exists()) {
			File out = new File(targetDir);
			try {
				FileUtils.copyFileToDirectory(from, out);
				//FileCopyUtils.copy(from, out);
			} catch (IOException e) {
			    e.printStackTrace();
			}
		} 

	}

	private void downFTPFile(String ftpPath, String targetDir) throws Exception {
		Response resp = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
		Config config = RespCfgHelper.getConfig(resp);
		if (config != null) {
			Block backupBlock = config.getBlockbyGroup("backuppath");
			String blockKey = backupBlock.getKey();
			if (blockKey.equalsIgnoreCase("ftp")) {
				String serverip = backupBlock.getItemValue("serverip");
				String user = backupBlock.getItemValue("user");
				String password = backupBlock.getItemValue("password");
				String encoding = backupBlock.getItemValue("encoding");
				String remotePath = ftpPath.substring(0, ftpPath.lastIndexOf('/'));
				String fileName = ftpPath.substring(ftpPath.lastIndexOf('/') + 1);
				remotePath = StringUtils.replaceChars(remotePath, '/', ':');
				boolean result = FtpUploadUtil.downFile(serverip, 21, user, password,encoding, remotePath, fileName, targetDir);
				if (!result) {
					throw new CommonUserException("获取FTP文件失败!");
				}
			}
		}
	}
	
	/**
	 * 树形菜单初始化数据
	 * @author zhou_xiaohu@topsec.com.cn
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public void initFtphome(){
		try{
			Response resp = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
			Config config = RespCfgHelper.getConfig(resp);
			if (config != null) {
				Block backupBlock = config.getBlockbyGroup("backuppath");
				String blockKey = backupBlock.getKey();
				if (blockKey.compareToIgnoreCase("local") == 0) {
					String path = backupBlock.getItemValue("path");
					if (path != null && path.length() > 0) {
						if (path.endsWith(":")) {
							path = path + File.separator;
						}
						basePath = path.replace('\\', '/');
					}
				} else if (blockKey.compareToIgnoreCase("ftp") == 0) {
					String serverip = backupBlock.getItemValue("serverip");
					String user = backupBlock.getItemValue("user");
					String password = backupBlock.getItemValue("password");
					String encoding = backupBlock.getItemValue("encoding");
					if(client != null) {
						ftpHome = "/";
					}
					client = FTPUtil.getFTPClient(serverip, user, password,encoding, true);
					if (client != null) {
						ftpHome = client.pwd();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 或许树形菜单节点信息
	 * 
	 * @param dir
	 * @param folderId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("getTreeNode")
	@ResponseBody
	public Object getTreeNode(SID sid,@RequestParam(value="id",required=false)String dir)throws Exception {
        Response resp = eventResponseService.getResponsesbyCfgKey("sys_cfg_backup").get(0);
        Config config = RespCfgHelper.getConfig(resp);
        JSONArray jsonArray = new JSONArray();
		if(config !=null){
			Block backupBlock = config.getBlockbyGroup("backuppath");
			String blockKey = backupBlock.getKey();
			if (blockKey.equalsIgnoreCase("local")) {
				return localBackupConfig(sid,backupBlock,dir) ;
			} else if (blockKey.equalsIgnoreCase("ftp")) {
				return ftpBackupConfig(sid,dir) ;
			}
		}
		return jsonArray;
	}
	
	
	private JSONArray localBackupConfig(SID sid,Block backupBlock,String dir){
		JSONArray jsonArray = new JSONArray() ;
		String path = backupBlock.getItemValue("path");
		if (path != null && path.length() > 0) {
			path = path + File.separator;
			basePath = path.replace('\\', '/');
		}
		File targetFile =null;
		if(dir == null || dir.isEmpty()){
			dir = basePath + File.separator +"logs";
		}
		
		targetFile = new File(dir);
		if(targetFile.exists() && targetFile.isDirectory()){
			String bakupPath=dir.substring(dir.indexOf("logs"),dir.length());
			String[] pathArray = StringUtil.split(bakupPath.replace("//", "/"),"/") ;
			List<File> tmpFiles = Arrays.asList(targetFile.listFiles()) ;
			Collections.sort(tmpFiles,new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int o1Number = StringUtil.toInt(FilenameUtils.getBaseName(o1.getName()));
					int o2Number = StringUtil.toInt(FilenameUtils.getBaseName(o2.getName())) ; 
					return o1Number - o2Number ;
				}
			}) ;
			File[] sons = tmpFiles.toArray(new File[tmpFiles.size()]);
			for(int i =0;i<sons.length;i++){
				String cpath = sons[i].getAbsolutePath().replace('\\', '/');
				JSONObject json = new JSONObject();
				String fileName = sons[i].getName();
				if(pathArray.length == 2){
					String type = fileName.replace('_', '/') ;
					if(!visible(sid,sons[i].list(), type))
						continue ;
				}else if(pathArray.length == 3){
					String host = fileName ;
					if(!sid.accessible(host, pathArray[2].replace('_', '/')))
						continue ;
				}
				if(sons[i].equals(".") ||  sons[i].equals(".."))
					continue;
				json.put("id",cpath);
				json.put("text", pathArray.length == 2 ? DeviceTypeNameUtil.getDeviceTypeName(fileName.replace('_', '/')) : fileName);
				json.put("state", sons[i].isFile() ? "open" : "closed") ;
				jsonArray.add(json);
			}
		}
		return jsonArray ;
	}
	
	/**
	 * getHistoryLogInfo 获取用户上次备份日志信息
	 * @author zhou_xiaohu@topsec.com.cn
	 * @return Object
	 */
	@RequestMapping("getHistoryLogInfo")
	@ResponseBody
	public Object getHistoryLogInfo(SID sid){
		JSONObject json=new JSONObject();
		try {
			String path = getDataHome();
			if (path == null) {
				json.put("info","nopath");
				throw new Exception("Get EventPath Failed.");
			}
			long user =sid.getAccountID();
			String tempLogPath = new StringBuilder(path).append("/events/temp/"+user).toString();
			HistLogInfo logInfo = getLogInfo(tempLogPath);
			if(logInfo.getType() == null){
				return json;
			}
			String rname = DeviceTypeNameUtil.getDeviceTypeName(logInfo.getType());
			if(!sid.accessible(logInfo.getHost(), logInfo.getType())){
				return null ;
			}
			json.put("deviceTypeName", logInfo.getType());
			json.put("hostIP", logInfo.getHost());
			json.put("startTime", logInfo.getStartTime());
			json.put("endTime", logInfo.getEndTime());
			json.put("rname", rname);
		}catch(Exception e){
			e.printStackTrace() ;
		}
		return json;
	}
	private JSONArray ftpBackupConfig(SID sid,String dir){
			initFtphome();//初始化ftp服务器信息
		if(client == null)
			return null;
		JSONArray jsonArray = new JSONArray() ;
		if (StringUtil.isBlank(dir)){
			dir = ftpHome + "/logs";
		}
		try {
			client.chdir(dir);
//			try {
//				client.chdir(dir);
//			} catch (FTPException e) {
//				e.printStackTrace() ;
//				if(e.getReplyCode() == 550){//文件不可用（可能已经被删除或者）
//					
//				}
//			}
			List<FTPFile> tmpFiles = Arrays.asList(client.dirDetails(dir)) ;
			Collections.sort(tmpFiles,new Comparator<FTPFile>() {
				@Override
				public int compare(FTPFile o1, FTPFile o2) {
					int o1Number = StringUtil.toInt(FilenameUtils.getBaseName(o1.getName()));
					int o2Number = StringUtil.toInt(FilenameUtils.getBaseName(o2.getName())) ; 
					return o1Number - o2Number ;
				}
			}) ;
			FTPFile[] files = tmpFiles.toArray(new FTPFile[tmpFiles.size()]) ;
			//根据用户权限信息限制用户所看的ftp目录
			String[] pathArray = StringUtil.split(dir.replace("//", "/"),"/") ;
			for (FTPFile file : files) {
				String fileName = file.getName();
				String type = fileName.replace("_", "/") ;
				if(fileName.equals(".") || fileName.equals(".."))
					continue;
				if(pathArray.length == 3 && !visible(sid,client.dir(file.getName()), type)){
					continue ;
				}
				if(pathArray.length == 4 && !sid.accessible(fileName, pathArray[3].replace('_', '/'))) {
					continue ;
				}
				JSONObject json = new JSONObject();
				json.put("id",client.pwd() + "/" + fileName);
				json.put("text", DeviceTypeNameUtil.getDeviceTypeName(type));
				json.put("state", file.isFile() ? "open" : "closed") ;
				jsonArray.add(json);
			}
		} catch (Exception e) {
			throw new RuntimeException("Ftp访问失败!",e);
		}finally {
			try {
				if(client != null){
					FTPUtil.logOut(client);
				}
			}catch(Exception e){
				e.printStackTrace() ;
			}
		} 
		return jsonArray ;
	}
	
	private boolean visible(SID sid,String[] hosts,String type){
		if(hosts == null){
			return false ;
		}
		for(String host:hosts){
			if(sid.accessible(host, type)){
				return true ;
			}
		}
		return false ;
	}
}
