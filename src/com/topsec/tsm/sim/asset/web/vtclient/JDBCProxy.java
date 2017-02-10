package com.topsec.tsm.sim.asset.web.vtclient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationExpirationException;

public class JDBCProxy extends ConnectionProxy {

	private String url ;
	private String driverClass ;
	/**命令计数器*/
	private AtomicInteger commandCounter = new AtomicInteger(1);
	/**命令历史*/
	private TreeMap<Integer,String> historyCommand = new TreeMap<Integer,String>() ;
	public JDBCProxy(String sessionId, String ip, String name, String password,int port) {
		super(sessionId, ip, name, password, port);
	}

	private Connection connection ;
	private Statement stat ;
	
	@Override
	public void connect(int timeout) throws ProxyException {
		String db = (String) getProperty("db") ;
		url = url.replace("{ip}", ip).replace("{port}", String.valueOf(port)).replace("{db}",db) ;
		try {
			Class.forName(driverClass) ;
			connection = DriverManager.getConnection(url, name, password) ;
			stat = connection.createStatement() ;
		} catch (ClassNotFoundException e) {
			throw new ProxyException("不支持的数据库类型！") ;
		} catch (SQLException e) {
			throw new ProxyException("连接数据库失败！",e) ;
		}
	}

	@Override
	public CommandResult exec(String command, int timeout)throws ProxyException, CommunicationExpirationException,ConnectionBusyException {
		try{
			if(command.equals("his")||command.equals("history")){
				List<String> header = new ArrayList<String>(2) ;
				Collections.addAll(header, "序号","SQL") ;
				List<Object[]> result = new ArrayList<Object[]>(historyCommand.size()) ;
				for(Map.Entry<Integer, String> hisCmd:historyCommand.entrySet()){
					result.add(new Object[]{hisCmd.getKey(),hisCmd.getValue()}) ;
				}
				return new TableResult(header, result) ;
			}
			if(historyCommand.size() > 30){
				historyCommand.remove(historyCommand.firstKey()) ;
			}
			historyCommand.put(commandCounter.getAndAdd(1), command) ;
			command = rebuildCommand(command) ;
			int firstSpaceIndex = command.indexOf(' ') ;
			String operation = command.substring(0,firstSpaceIndex > 0 ? firstSpaceIndex : command.length()) ;
			if(operation.equalsIgnoreCase("update") || operation.equalsIgnoreCase("delete") || operation.equals("insert") ||
			   operation.equalsIgnoreCase("alter") || operation.equalsIgnoreCase("create") || operation.equalsIgnoreCase("drop")){
				return handleIUD(command) ;
			}else{
				return handleSelect(command) ;
			}
		}catch(SQLException e){
			return new OneRecordResult("执行出错", e.getMessage()) ;
		}catch(Exception e){
			throw new ProxyException("SQL执行出错！") ;
		}
	}
	/**
	 * 重组命令
	 * @param command
	 * @return
	 * @throws CommandException 
	 */
	protected String rebuildCommand(String command){
		if(command.startsWith("!")){
			Integer commandSeq = StringUtil.toInteger(command.substring(1),-1) ;
			if(commandSeq < 1){
				throw new UnsupportedOperationException(command) ;
			}
			command = historyCommand.get(commandSeq) ;
			if(command == null){
				throw new CommandException("无效的命令序号："+commandSeq) ;
			}
		}
		if(command.endsWith(";")){
			command = command.substring(0,command.length()-1) ;
		}
		return command ;
	}
	protected CommandResult handleSelect(String command)throws SQLException{
		ResultSet rs = stat.executeQuery(command) ;
		ResultSetMetaData meta = rs.getMetaData() ;
		int columnCount = meta.getColumnCount() ;
		List<String> header = new ArrayList<String>(columnCount) ;
		for(int i=1;i<=columnCount;i++){
			header.add(meta.getColumnLabel(i)) ;
		}
		List<String[]> queryResult = new ArrayList<String[]>() ;
		while(rs.next()){
			String[] row = new String[columnCount] ;
			for(int i=1;i<=columnCount;i++){
				row[i-1] = StringUtil.toString(rs.getObject(i)) ;
			}
			queryResult.add(row) ;
		}
		return new TableResult(header, queryResult) ;
	}
	protected String getPageSQL(String sql,int pageIndex,int pageSize){
		return sql ;
	}
	/**
	 * 处理插入、删除、更新
	 * @param command
	 * @return
	 * @throws SQLException 
	 */
	protected CommandResult handleIUD(String command) throws SQLException{
		int rowCount = stat.executeUpdate(command) ;
		return new OneRecordResult("执行结果",rowCount+"行受影响") ;
	}
	@Override
	public void close() {
		ObjectUtils.close(stat) ;
		ObjectUtils.close(connection) ;
	}

	@Override
	public void cancel() {
		close() ;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

}
