package com.topsec.tsm.sim.asset.web.vtclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationExpirationException;

public class SSHProxy extends OSCommandProxy{

	public static final String CHARSET_PROPERTY = "charset" ;
	protected Session session ;
	private StreamReader commandResultReader ;
	private PrintStream printStream ;
	public SSHProxy(String sessionId,String ip,String name,String password, int port) {
		super(sessionId,ip,name,password,port) ;
	}
	
	public SSHProxy(String sessionId,String ip,String name,String password) {
		super(sessionId,ip,name,password,22) ;
	}

	@Override
	public void connect(int timeout) throws ProxyException {
		try {
			if(session != null && !session.isConnected()){
				return ;
			}
			JSch jsch = new JSch();
			session = jsch.getSession(name, ip, port);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session.setConfig(sshConfig);
			session.setPassword(password) ;
			session.connect(timeout) ;
			session.setDaemonThread(true) ;
		} catch (JSchException e) {
			String message = StringUtil.nvl(e.getMessage()) ;
			if(message.equals("Auth fail")){
				throw new ProxyException("用户名或密码错误！") ;
			}
			throw new ProxyException("无法连接到服务器！") ;
		}
	}

	@Override
	public void cancel() {
		close() ;
	}
	
	@Override
	public CommandResult innerExec(String command, int timeout)throws CommunicationExpirationException,ProxyException {
		try {
			if(commandResultReader == null){
				ChannelShell channel= (ChannelShell) session.openChannel("shell") ;
				channel.setPtyType("dumb") ;
				channel.connect(timeout) ;
				commandResultReader = new StreamReader(channel.getInputStream(), StringUtil.ifBlank(getCharset(),"ISO-8859-1"), 512 * 1024) ;
				commandResultReader.setName("ssh-"+getIp()) ;
				printStream = new PrintStream(channel.getOutputStream()) ;
				commandResultReader.start() ;
			}
			commandResultReader.clear() ;
			printStream.println(command) ;
			printStream.flush() ;
			int preBufferPos = -1 ;
			int times = 50 ;//在没有收到数据前尝试次数
			while(times-- > 0){
				Thread.sleep(100) ;
				int currentPos = commandResultReader.getBuffPos() ;
				if(currentPos != preBufferPos){
					times = 10 ;//收到数据后尝试次数
					preBufferPos = currentPos ;
				}
			}
			return new StringResult(commandResultReader.getStringResult()) ;
		} catch (JSchException e) {
			throw new CommunicationExpirationException(e) ;
		} catch (IOException e) {
			throw new ProxyException(e) ;
		} catch (Exception e){
			throw new ProxyException(e) ;
		}
	}
	

	@Override
	public void close() {
		if(session != null && session.isConnected()){
			session.disconnect() ;
		}
		if(commandResultReader != null){
			commandResultReader.close() ;
		}
	}
	public static class StreamReader extends Thread{
		
		private volatile byte[] buff ;
		private volatile int buffPos = 0 ; 
		private String charset ;
		private InputStream in ;
		public StreamReader(InputStream in,String charset,int bufferSize) {
			this.in = in ;
			this.buff = new byte[bufferSize] ;
			this.charset = charset ;
		}

		@Override
		public void run() {
			try{
			    int ret_read = 0;
			    do{
			    	while(in.available() <= 0){
			    		Thread.sleep(100) ;
			    	}
			        ret_read = in.read(buff,buffPos,buff.length-buffPos);
			        if(ret_read > 0){
			        	buffPos += ret_read ;
			        }
			    }while (ret_read >= 0);
			} catch (IOException e){
			    System.err.println("Exception when reading socket:" + e.getMessage());
			} catch (InterruptedException e){
			}
			ObjectUtils.close(in) ;
		}
		
		public byte[] getResult(){
			return Arrays.copyOf(buff,buffPos) ;
		}
	
		public void clear(){
			buffPos = 0 ;
			Arrays.fill(buff, (byte)0) ;
		}
		
		public void close(){
			ObjectUtils.close(in) ;
			this.interrupt() ;//强制线程对象停止
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

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}
	}
}
