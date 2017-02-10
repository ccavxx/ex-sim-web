package com.topsec.tsm.sim.asset.web.vtclient;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.enterprisedt.net.ftp.ControlChannelIOException;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationExpirationException;

public class FtpProxy extends FileBrowser {

	private FTPClient client ;
	
	public FtpProxy(String sessionId, String ip, String name, String password,int port) {
		super(sessionId, ip, name, password, port);
	}

	@Override
	public void connect(int timeout) throws ProxyException {
		try {
			client = new FTPClient() ;
			client.setRemoteHost(ip);
			String charset = getCharset() ;
			client.setControlEncoding(StringUtil.isBlank(charset) ? "UTF-8" : charset);
			client.setMessageListener(new FTPMessageCollector()) ;
			client.setTimeout(timeout) ;
			client.connect() ;
			client.login(name,password) ;
		} catch (Exception e) {
			close() ;
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}
	@Override
	public void upload(String uploadFile, String dst)throws ProxyException, ConnectionBusyException,FileNotFoundException {
		try {
			client.put(uploadFile, dst) ;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}

	@Override
	public void upload(InputStream is, String dst)throws ProxyException, ConnectionBusyException {
		try {
			client.put(is, dst) ;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}

	@Override
	public void download(String downloadFile, OutputStream os)throws ProxyException, ConnectionBusyException {
		try {
			client.get(os, downloadFile) ;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		} 
	}

	@Override
	public void delete(String deleteFile) throws ProxyException,ConnectionBusyException {
		try {
			client.delete(deleteFile) ;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}

	@Override
	public void deleteDir(String dir) throws ProxyException,ConnectionBusyException {
		try{
			FTPFile[] list = client.dirDetails(dir) ;
			for (FTPFile item : list) {
				String fileName = item.getName() ;
		        if (!item.isDir()) {
		            client.delete(dir + "/" + fileName); 
		        } else if (!".".equals(fileName) && !"..".equals(fileName)) {
		        	deleteDir(dir + "/" + fileName);
		        }
		    }
			client.rmdir(dir) ;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}

	@Override
	public List<FileEntry> listFileNames(String directory)throws ProxyException, ConnectionBusyException {
		try {
			FTPFile[] files = client.dirDetails(directory) ;
			List<FileEntry> result = new ArrayList<FileEntry>() ;
			for(FTPFile file:files){
				result.add(new FileEntry(file.getName(), file.isDir(), file.size())) ;
			}
			return result ;
		}catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
			return Collections.emptyList() ;
		}
	}

	@Override
	public FileEntry getFile(String file) throws ProxyException {
		try {
			FTPFile ftpFile = client.fileDetails(file) ;
			FileEntry entry = new FileEntry(ftpFile.getName(), ftpFile.isDir(), ftpFile.size()) ;
			return entry;
		} catch (Exception e) {
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
			return null ;
		}
	}

	@Override
	public void createDir(String name)throws ProxyException,ConnectionBusyException {
		try{
			client.mkdir(name) ;
		}catch(Exception e){
			ProxyException ex = exceptionHandler(e) ;
			if (ex != null) {
				throw ex ;
			}
		}
	}

	private ProxyException exceptionHandler(Exception e){
		Exception cause = e ;
		if(cause instanceof ConnectException){
			return new ProxyException("连接服务器失败！") ;
		}else if(cause instanceof ControlChannelIOException){
			return new ProxyException("连接中断，请重新登录！") ;
		}else if(cause instanceof FTPException){
			FTPException ftpEx = (FTPException)cause ;
			int code = ftpEx.getReplyCode() ; 
			switch(code){
				case 530:return new ProxyException("用户名或密码错误！") ;
				case 550:return new ProxyException("没有权限进行此操作！");
			}
		}
		return new ProxyException("未知错误！") ;
	}
	
	@Override
	public CommandResult exec(String command, int timeout)throws ProxyException, CommunicationExpirationException,ConnectionBusyException {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void close() {
		try {
			if (client != null) {
				client.quit() ;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancel() {
		close() ;
	}

}
