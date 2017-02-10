package com.topsec.tsm.sim.asset.web.vtclient;

import java.util.HashMap;
import java.util.Map;

import com.topsec.tsm.comm.CommunicationExpirationException;


/**
 * 网络通信代理类
 * @author hp
 *
 */
public abstract class ConnectionProxy{

	private Map<String,Object> context ;
	protected String ip;
	protected String name ;
	protected String password ;
	protected int port ;
	private long lastAccessTimes  ;//最后一次访问时间
	private String charset ;
	private String sessionId ;
	private boolean busy ;
	public ConnectionProxy(String sessionId,String ip,String name,String password, int port) {
		super();
		this.ip = ip;
		this.port = port;
		this.name = name ;
		this.password = password ;
		this.sessionId = sessionId ;
		context = new HashMap<String, Object>() ;
		lastAccessTimes = System.currentTimeMillis() ;
	}

	public abstract void connect(int timeout)throws ProxyException ;
	
	public abstract CommandResult exec(String command,int timeout)throws ProxyException,CommunicationExpirationException,ConnectionBusyException ;
	
	public abstract void close() ;
	
	public abstract void cancel() ;
	
	public void setProperty(String key,Object value){
		context.put(key, value) ;
	}
	
	public Object getProperty(String key){
		return context.get(key) ;
	}
	
	@Override
	public int hashCode() {
		return sessionId.hashCode()+17;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ConnectionProxy)){
			return false ;
		}
		ConnectionProxy acp = (ConnectionProxy)obj ;
		return sessionId.equals(acp.sessionId);
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getLastAccessTimes() {
		return lastAccessTimes;
	}

	public void setLastAccessTimes(long lastAccessTimes) {
		this.lastAccessTimes = lastAccessTimes;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
}
