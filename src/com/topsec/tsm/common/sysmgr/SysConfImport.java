package com.topsec.tsm.common.sysmgr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.resource.SystemDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.util.io.ZipUtils;

/*
 * mysql导入数据
 */
public class SysConfImport {
	
	private static Logger logger = LoggerFactory.getLogger(SysConfImport.class) ;
	
	private SessionFactory sessionFactory;// spring注入对象
	private NodeMgrFacade nodeMgrFacade;

	public void runImport() {
		File zipSysconfPath = new File(SystemDefinition.DEFAULT_INSTALL_DIR, "backup");
		if (!zipSysconfPath.exists())
			return;
		else if (zipSysconfPath.isDirectory()) {
			String[] files = zipSysconfPath.list();
			if (files[0].indexOf(".zip") > 0) {
				try {
					cleanDefaultTable();
					exeImpSysConf(zipSysconfPath.getCanonicalPath() + File.separator + files[0]);
					sycNodeId();
					disableAllAssets();
					FileUtils.deleteDirectory(zipSysconfPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void sycNodeId() {
		Session session = null;
		Connection con = null;
		Statement ps = null;
		FileInputStream fis = null;
		BufferedWriter writer = null;
		session = sessionFactory.openSession();
		String localIp = IpAddress.getLocalIp().getLocalhostAddress() ;
		StringBuilder sql = new StringBuilder("select nodeid, type from sim_node where ip = '").append(localIp).append("'");
		try {
			con = session.connection();
			ps = con.createStatement();
			ResultSet re = ps.executeQuery(sql.toString());
			while (re.next()) {
				String type = re.getString(2);
				String filePath = null;
				if (NodeUtil.isAuditor(type)) {
					filePath = "../conf/node/node.properties";
				} else if (NodeUtil.isIndexService(type)) {
					filePath = "../conf/node/service.properties";
				}else if(NodeUtil.isReportService(type)){
					filePath = "../conf/node/report.properties" ;
				}else if(NodeUtil.isQueryService(type)){
					filePath = "../conf/node/search.properties" ;
				}else if(NodeUtil.isCollector(type)){
					filePath = "../conf/node/collector.properties" ;
				}else if(NodeUtil.isAction(type)){
					filePath = "../conf/node/action.properties" ;
				}else if(NodeUtil.isFlexer(type)){
					filePath = "../conf/node/flexer.properties" ;
				}
				if (filePath != null) {
					Properties properties = new Properties();
					fis = new FileInputStream(filePath);
					properties.load(fis);
					properties.setProperty("topsec.tsm.node.id", re.getString(1));
					OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath));
					writer = new BufferedWriter(out);
					properties.store(writer, null);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {
			closeResource(con, ps, session);
			ObjectUtils.close(fis) ;
			ObjectUtils.close(writer) ;
		}

	}

	public void cleanSequence() {
		Session session = null;
		Connection con = null;
		Statement ps = null;
		session = sessionFactory.openSession();
		StringBuilder s = new StringBuilder("truncate table sim_sequence");
		try {
			con = session.connection();
			ps = con.createStatement();
			ps.execute(s.toString());

		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			closeResource(con, ps, session);
		}

	}

	public void cleanDefaultTable() {
		Session session = null;
		Connection con = null;
		Statement ps = null;
		session = sessionFactory.openSession();
		try {
			con = session.connection();
			ps = con.createStatement();
			ps.execute("truncate table sim_sequence;");
			ps.execute("truncate table auth_account;");
			ps.execute("truncate table auth_user_role;");
			// ps.execute("truncate table auth_role;");
			ps.execute("truncate table auth_activeuser;");
			ps.execute("truncate table auth_permission;");
			ps.execute("truncate table auth_disable_client;");
			ps.execute("truncate table auth_failedlogin;");
			ps.execute("truncate table auth_operation;");
			ps.execute("truncate table auth_operation_role;");
			// ps.execute("truncate table sim_resource;");
			// ps.execute("truncate table sim_relationship;");
			// ps.execute("truncate table sim_node;");
			// ps.execute("truncate table sim_component;");
			// ps.execute("truncate table sim_segment;");
			// ps.execute("truncate table sim_dataflow;");
			// ps.execute("truncate table sim_dataflow_component;");
			// ps.execute("truncate table sim_datasource;");
			// ps.execute("truncate table sim_datasource_blacklist;");
			// ps.execute("truncate table sim_node_eventpolicys;");
			// ps.execute("truncate table sim_datasource_blacklist;");
			// ps.execute("truncate table tal_eventpolicy;");
			// ps.execute("truncate table tal_r_event_response;");
			// ps.execute("truncate table tal_response;");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResource(con, ps, session);
		}

	}

	public void disableAllAssets() {
		Session session = null;
		Connection con = null;
		Statement ps = null;
		session = sessionFactory.openSession();
		try {
			con = session.connection();
			ps = con.createStatement();
			ps.execute("UPDATE SIM_DATASOURCE SET AVAILABLE = 0");
			ps.execute("UPDATE ASS_DEVICE SET ENABLED = 0") ;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResource(con, ps, session);
		}
		try {
			nodeMgrFacade.disableUserDataSources() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导入
	 * 
	 * @param type
	 *            导入导出区分词
	 * @param tables
	 *            表名数组
	 * 
	 * @param filePath
	 *            路径
	 * @return
	 */
	public boolean impSysConf(String type, String[] tables, String filePath) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		Transaction transaction = null;
		Session session = null;
		String table = null;
		String sql = null;
		if (type == null || tables == null || tables.length <= 0
				|| filePath == null) {
			return false;
		}
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			transaction.begin();// 开始事物
			conn = session.connection();// 获取连接

			for (int i = 0; i < tables.length; i++) {
				table = tables[i];
				String dir = filePath + "/" + table + ".dmp";
				File file = new File(dir);
				if ("IN".equals(type)) {
					if (!file.exists()) {
						throw new RuntimeException(dir + " 文件不存在！");
					}
					if (!file.isFile()) {
						throw new RuntimeException(dir + " 不是一个文件！");
					}
					table = table.substring(table.indexOf('-')+1);
					logger.info("正在导入备份文件:{}",table) ;
					sql = "LOAD DATA LOCAL INFILE '" + dir + "' INTO TABLE " + table + " CHARACTER SET UTF8 ";// 导入sql语句
				}
				String _sql = StringUtils.replaceChars(sql, "\\", "/");
				pstmt = conn.prepareStatement(_sql);
				pstmt.execute();// 执行
			}
			transaction.commit();// 提交事物
		} catch (Exception e) {
			transaction.rollback();// 回滚事物
			e.printStackTrace();
			return false;
		} finally {
			closeResource(conn, pstmt, session);// 关闭资源
		}
		return true;
	}

	/**
	 * 关闭资源
	 */
	public void closeResource(Connection conn, Statement stmt, Session session) {
		ObjectUtils.close(stmt) ;
		ObjectUtils.close(conn) ;
		if (session != null) {
			session.close();
		}
	}

	/**
	 * 导入备份文件到数据库
	 * 
	 * @param zipFileInPath
	 *            备份文件目录
	 * @return
	 */
	public boolean exeImpSysConf(String zipFileInPath) {
		if (StringUtils.isBlank(zipFileInPath)) {
			return false;
		}
		String unzipFileDir = System.getProperty("java.io.tmpdir") + "sys_backup";// 备份文件的解压目录
		try {
			File file = new File(unzipFileDir);
			if (file.exists()) {
				FileUtils.forceDelete(file);
			}
			file.mkdirs();// 没有则创建

			ZipUtils.unzipDirectory(zipFileInPath, unzipFileDir, true);// 解压文件

			File[] files = file.listFiles();
			if (files.length > 0) {
				List<String> tablesList = new ArrayList<String>();
				for (File _file : files) {
					if (_file.isFile() && _file.getName().endsWith("dmp")) {
						String table = _file.getName().substring(0, _file.getName().length() - 4);// 获取名称，无后缀名
						tablesList.add(table);
					}
				}

				if (tablesList.size() > 0) {
					String[] tables = new String[tablesList.size()];
					for (int i = 0; i < tablesList.size(); i++) {
						tables[i] = tablesList.get(i);
					}

					boolean res = impSysConf("IN", tables, unzipFileDir);// 导入
					// if (res) {// 导入成功后删除文件
					for (File temp : files) {
						if (temp.isFile() && temp.getName().endsWith("dmp")) {
							temp.delete();
						}
					}
					FileUtils.deleteDirectory(new File(unzipFileDir));
					if (new File(zipFileInPath).exists())
						FileUtils.deleteQuietly(new File(zipFileInPath));
					// }
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public NodeMgrFacade getNodeMgrFacade() {
		return nodeMgrFacade;
	}

	public void setNodeMgrFacade(NodeMgrFacade nodeMgrFacade) {
		this.nodeMgrFacade = nodeMgrFacade;
	}

}
