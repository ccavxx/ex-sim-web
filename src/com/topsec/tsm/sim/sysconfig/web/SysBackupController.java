package com.topsec.tsm.sim.sysconfig.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.util.AuditLogFacade;
import com.topsec.tsm.sim.util.DbMgrUtils;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.sim.util.FtpConfigUtil;
import com.topsec.tsm.util.io.ZipUtils;
import com.topsec.tsm.util.net.FtpUploadUtil;


/**
 *类描述：系统备份
 *@version 1.0
 */
@Controller
@RequestMapping("sysconfig/sysbackup")
public class SysBackupController {
	
	private String tempInDir = "sysBackup";
	private String tempOutDir = "_sysBackup";
	private String tempDir = System.getProperty("java.io.tmpdir");// 临时备份文件目录(C:/Users/TAL/AppData/Local/Temp/)
	private String zipFileName = "sysBackup.zip";

	/**
	 * 获取要备份的表
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String[] getTableList() {
		InputStream inStream = SysBackupController.class.getResourceAsStream("backupTbs.properties");// 读取配置文件backupTbs.properties要备份的表
		Properties prop = new Properties();
		try {
			prop.load(inStream);
			Set tables = prop.entrySet();
			Iterator it = tables.iterator();
			String[] result = new String[prop.size()];
			int i=0;
			while(it.hasNext()) {
				Entry table = (Entry) it.next();
				result[i] = table.getKey() + "-" + table.getValue();
				i++;
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 备份页面、从数据库导出表，并上传FTP
	 *  @param request
	 *  @param response
	 *  @throws Exception
	 */
	@RequestMapping("getBackupFile")
	public void getBackupFile(SID sid,HttpServletRequest request, HttpServletResponse response) throws Exception {
		DbMgrUtils dbMgrUtils = (DbMgrUtils) FacadeUtil.getFacadeBean(request, null, "dbMgrUtils");

		String[] tables = getTableList();

		String filePath = tempDir + tempInDir;// 导出数据库文件临时存放目录
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();// 没有则创建
		}

		Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("log");
		int port = Integer.parseInt((String) args.get("port"));
		String host = (String) args.get("host");
		String user = (String) args.get("user");
		String password = (String) args.get("password");
		String encoding = (String) args.get("encoding");

		boolean fileExist = FtpUploadUtil.checkFileExist(host, port, user, password,encoding, ".", zipFileName);
		if (fileExist) {
			FtpUploadUtil.deleteFile(host, port, user, password,encoding, ".", zipFileName);// 删除原来的
		}

		boolean res = dbMgrUtils.doData("OUT", tables, true, filePath);

		if (res) {// 如果导出成功
			String zipFilePath = tempDir + zipFileName;// 压缩文件目录
			File zipFile = new File(zipFilePath);
			ZipUtils.zipDirectory(filePath, false, zipFilePath);

			InputStream inputStream = new FileInputStream(zipFile);

			boolean b = FtpUploadUtil.uploadFile(host, port, user, password,encoding, ".", zipFileName, inputStream);
			if (b) {// 上传成功

				// 以下产生日志信息
				String curUser = sid.getUserName();
				if(curUser != null)
					toLog(AuditCategoryDefinition.SYS_ADD, "导出系统备份文件", "导出系统备份文件对象: " + zipFile.getAbsolutePath(),curUser, true,Severity.LOWEST);

				File[] listFiles = file.listFiles();
				for (File _file : listFiles) {
					if (_file.isFile() && _file.getName().endsWith("dmp")) {
						_file.delete();
					}
				}
				file.delete();
				zipFile.delete();
			}
		}
		
		response.setContentType("application/x-javascript;charset=utf-8");
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.print("{\"result\":\"" + "success" + "\"" +"}");
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从FTP下载备份文件
	 *  @param request
	 *  @param response
	 *  @throws Exception
	 */
	@RequestMapping("loadBackupFile")
	public void loadBackupFile(SID sid, HttpServletRequest request, HttpServletResponse response) throws Exception {
		BufferedInputStream buffin = null;
		BufferedOutputStream buffout = null;
		File backupFile = null;
		File zipFile = null;
		try {
			// 从FTP下载备份文件
			Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("log");
			int port = Integer.parseInt((String) args.get("port"));
			String host = (String) args.get("host");
			String user = (String) args.get("user");
			String password = (String) args.get("password");
			String encoding = (String) args.get("encoding");
			boolean fileExist = FtpUploadUtil.checkFileExist(host, port, user, password,encoding, ".", zipFileName);
			if (fileExist) {
				response.setContentType("application/x-msdownload");
				response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(zipFileName, "UTF-8"));
				String zipFilePath = tempDir + tempOutDir;// 从FTP下载下来备份文件存放临时目录
				zipFile = new File(zipFilePath);
				if (!zipFile.exists()) {
					zipFile.mkdirs();// 没有则创建
				}
				FtpUploadUtil.downFile(host, port, user, password, encoding,".", zipFileName, zipFilePath);

				backupFile = new File(zipFilePath + "/" + zipFileName); // 要被下载的文件
				buffin = new BufferedInputStream(new FileInputStream(backupFile));
				buffout = new BufferedOutputStream(response.getOutputStream());

				byte[] b = new byte[1024];
				int len = 0;
				while ((len = buffin.read(b)) != -1) {
					buffout.write(b, 0, len);
				}
				String userName = sid.getUserName();
				// 以下产生日志信息
				toLog(AuditCategoryDefinition.SYS_ADD, "下载系统备份文件", "下载系统备份文件对象: " + zipFile.getAbsolutePath(), userName, true,Severity.LOWEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {// 关闭流
			if (buffin != null) {
				buffin.close();
			}
			if (buffout != null) {
				buffout.close();
			}
			if (backupFile != null) {
				backupFile.delete();
			}
			if (zipFile != null) {
				zipFile.delete();
			}
		}

	}
	
	private void toLog(String cat, String name, String desc, String subject,  boolean result, Severity severity) {
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(cat);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}

}
