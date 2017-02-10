package com.topsec.tsm.sim.util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import com.topsec.tsm.util.io.ZipUtils;

/*
 * mysql导入导出数据
 */
public class DbMgrUtils {
	private SessionFactory sessionFactory;// spring注入对象

	/**
	 * 导入导出
	 * 
	 * @param type
	 *           导入导出区分词
	 * @param tables
	 *           表名数组
	 * @param overRide
	 *           导出时，如果该文件存在，是否覆盖
	 * @param filePath
	 *           路径
	 * @return
	 */
	public boolean doData(String type, String[] tables, boolean overRide, String filePath) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		Transaction transaction = null;
		Session session = null;
		String table = null;
		String sql = null;
		if (type == null || tables == null || tables.length <= 0 || filePath == null) {
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
				if (file.isFile() && file.exists() && "OUT".equals(type)) {// 导出
					if (overRide) {
						file.delete();
					} else {
						throw new RuntimeException("相同名称的备份" + "文件已经存在！");
					}
				} else if ("IN".equals(type)) {
					if (!file.exists()) {
						throw new RuntimeException(dir + " 文件不存在！");
					}
					if (!file.isFile()) {
						throw new RuntimeException(dir + " 不是一个文件！");
					}
				}
				 table = table.substring(9);
				if ("OUT".equals(type)) {
					sql = "SELECT * FROM " + table + " INTO OUTFILE '" + dir + "' ";// 导出sql语句

				} else if ("IN".equals(type)) {
					sql = "LOAD DATA LOCAL INFILE '" + dir + "' INTO TABLE " + table;// 导入sql语句
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
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (session != null) {
					session.close();
				}
			}
		}
	}

	/**
	 * 导入备份文件到数据库
	 * 
	 * @param zipFileInPath
	 *           备份文件目录
	 * @return
	 */
	public boolean impData(String zipFileInPath) {
		if (StringUtils.isBlank(zipFileInPath)) {
			return false;
		}
		String zipFileDir = System.getProperty("java.io.tmpdir") + "backup";// 备份文件的解压目录
		try {
			File file = new File(zipFileDir);
			if (!file.exists()) {
				file.mkdirs();// 没有则创建
			}
			ZipUtils.unzipDirectory(zipFileInPath, zipFileDir, true);// 解压文件

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

					boolean res = doData("IN", tables, false, zipFileDir);// 导入
					if (res) {// 导入成功后删除文件
						for (File temp : files) {
							if (temp.isFile() && temp.getName().endsWith("dmp")) {
								temp.delete();
							}
						}
					}
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

}
