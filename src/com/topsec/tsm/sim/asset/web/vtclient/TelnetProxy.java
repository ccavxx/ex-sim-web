package com.topsec.tsm.sim.asset.web.vtclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.net.telnet.TelnetClient;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationExpirationException;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;

public class TelnetProxy extends OSCommandProxy {

	private TelnetClient telnetClient ;
	private StreamReader reader ;
	private OutputStream out ;
	public TelnetProxy(String sessionId, String ip, String name,String password, int port) {
		super(sessionId, ip, name, password, port);
	}

	@Override
	public void connect(int timeout) throws ProxyException {
		if (telnetClient != null && telnetClient.isConnected() && telnetClient.isAvailable()) {
			return ;
		}
		AssetObject ao = AssetFacade.getInstance().getAssetByIp(ip) ;
		if(ao == null){
			throw new ProxyException("资产不存在！") ;
		}
		boolean isWindows = ao.getDeviceType().equalsIgnoreCase("OS/Microsoft");
		if(isWindows){
			telnetClient = new TelnetClient("vt200");
		}else{
			telnetClient = new TelnetClient("dumb") ;
		}
		try {
			//telnetClient.addOptionHandler(new EchoOptionHandler(false, false, false, false)) ;
			reader = new StreamReader(telnetClient, StringUtil.ifBlank(getCharset(),"ISO-8859-1"), 1024 * 512) ;
			telnetClient.setConnectTimeout(timeout) ;
			//telnetClient.addOptionHandler(new EchoOptionHandler(true, true, true, true)) ;
			telnetClient.connect(ip, port);
			reader.start() ;
			out = telnetClient.getOutputStream() ;
			reader.waitUntilReciveData(15000,true,"login:","user:",":") ;
			reader.clear() ;
			writeLine(name) ;
			reader.waitUntilReciveData(15000,true,"password","password:",":") ;
			writeLine(password) ;
		} catch (Exception e) {
			reader.close() ;
			throw new ProxyException(e) ;
		}
		
	}
	private void writeLine(String command) throws IOException{
		out.write(command.getBytes()) ;
		out.write('\r') ;
		out.write('\n') ;
		out.flush() ;
	}
	@Override
	public CommandResult innerExec(String command, int timeout)throws ProxyException, CommunicationExpirationException {
		try {
			reader.clear() ;//每次发送命令以前需要清空以前的缓冲区
			writeLine(command) ;
			int preBufferPos = -1 ;
			int times =50 ;
			while(times-- > 0){
				Thread.sleep(100) ;
				int currentPos = reader.getBuffPos() ;
				if(currentPos != preBufferPos){
					times = 10 ;
					preBufferPos = currentPos ;
				}
			}
			return new StringResult(reader.getStringResult()) ;
		} catch (Exception e) {
			throw new ProxyException(e) ;
		}
	}

	@Override
	public void close() {
		try {
			if(telnetClient.isConnected() && telnetClient.isAvailable()){
				telnetClient.disconnect() ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			reader.close() ;
		}
	}

	@Override
	public void cancel() {
	}

	@Override
	public void setCharset(String charset) {
		super.setCharset(charset) ;
		if(reader != null){
			reader.setCharset(charset) ;
		}
	}
	
	public static class StreamReader extends Thread{
		
		private byte[] buff ;
		private TelnetClient tc ;
		private volatile int buffPos = 0 ; 
		private String charset ;
		
		public StreamReader(TelnetClient tc,String charset,int bufferSize) {
			this.tc = tc;
			this.buff = new byte[bufferSize] ;
			this.charset = charset ;
		}

		@Override
		public void run() {
			InputStream instr = tc.getInputStream();
			try{
			    int ret_read = 0;
			    do{
			    	while(instr.available() <= 0){
			    		Thread.sleep(100) ;
			    	}
			        ret_read = instr.read(buff,buffPos,buff.length-buffPos);
			        if(ret_read > 0){
			        	buffPos += ret_read ;
			        }
			    }while (ret_read >= 0);
			} catch (IOException e){
			    System.err.println("Exception when reading socket:" + e.getMessage());
			} catch(InterruptedException e){
				
			}
			try{
				if(tc.isConnected() && tc.isAvailable()){
					tc.disconnect();
				}
			} catch (IOException e){
			    System.err.println("Exception while closing telnet:" + e.getMessage());
			}
		}
		
		public byte[] getResult(){
			return Arrays.copyOf(buff,buffPos) ;
		}
	
		public void clear(){
			buffPos = 0 ;
			Arrays.fill(buff, (byte)0) ;
		}
		
		public void close(){
			try {
				if(tc.isConnected() && tc.isAvailable()){
					tc.disconnect() ;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				this.interrupt() ;//强制线程对象停止
			}
		}
		
		public int getBuffPos(){
			return buffPos ;
		}
		
		public String getStringResult(){
			try {
				return new String(getResult(),charset) ;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null ;
			}
		}

		public void waitUntilReciveData(int timeout,boolean ignoreCase,String... patterns) throws ProxyException{
			while((timeout-= 100) >= 0){
				String str = getStringResult() ;
				if(str != null){
					String lowerStr = str.toLowerCase();
					for(String pattern:patterns){
						if(lowerStr.contains(pattern)){
							return ;
						}
					}
				}
				try {
					Thread.sleep(100) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(timeout < 0){
				throw new ProxyException("等待数据超时！") ;
			}
		}
		
		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}
	}
}
