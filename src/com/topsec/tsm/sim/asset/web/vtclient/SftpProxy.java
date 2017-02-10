package com.topsec.tsm.sim.asset.web.vtclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.catalina.connector.ClientAbortException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.topsec.tsm.comm.CommunicationExpirationException;

public class SftpProxy extends FileBrowser {
	private SSHProxy sshProxy ;
	private ChannelSftp sftp ;
	public SftpProxy(String sessionId, String ip, String name, String password,int port) {
		super(sessionId,ip,name,password,port) ;
		this.sshProxy = new SSHProxy(sessionId, ip, name, password,port);
	}
	private synchronized ChannelSftp getSftpChannel() throws JSchException, ConnectionBusyException{
		if(isBusy()){
			throw new ConnectionBusyException() ;
		}
		setLastAccessTimes(System.currentTimeMillis()) ;
		if(sftp == null){
			sftp = (ChannelSftp) sshProxy.session.openChannel("sftp") ;
			sftp.connect(10000) ;
		}
		setBusy(true) ;
		return sftp ;
	}
	/**
	 * 上传文件
	 *
	 * @param directory  上传的目录
	 * @param uploadFile 要上传的文件
	 * @param sftp
	 * @throws ProxyException 
	 * @throws FileNotFoundException 
	 * @throws ConnectionBusyException 
	 */
	public void upload(String uploadFile,String dst) throws ProxyException, FileNotFoundException, ConnectionBusyException {
		try {
			sftp = getSftpChannel() ;
			File file = new File(uploadFile);
			sftp.put(new FileInputStream(file), dst);
		} catch (FileNotFoundException e) {
			throw e ;
		} catch (JSchException e) {
			throw new ProxyException("连接失败!") ;
		} catch (SftpException e) {
			closeChannel() ;//上传失败关闭当前channel
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}else{
				throw new ProxyException("上传文件失败!") ;
			}
		}finally{
			setBusy(false) ;
		}
	}
	
	@Override
	public void upload(InputStream is, String dst)throws ProxyException, ConnectionBusyException {
		try {
			sftp = getSftpChannel() ;
			sftp.put(is, dst);
		} catch (JSchException e) {
			throw new ProxyException("连接失败!") ;
		} catch (SftpException e) {
			closeChannel() ;//上传失败关闭当前channel
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}else{
				throw new ProxyException("上传文件失败!") ;
			}
		}finally{
			setBusy(false) ;
		}
	}
	/**
	 * 下载文件
	 * @param directory    下载目录
	 * @param downloadFile 下载的文件
	 * @param saveFile     存在本地的路径
	 * @param sftp
	 * @throws ProxyException 
	 * @throws ConnectionBusyException 
	 */
	public void download(String downloadFile, OutputStream os) throws ProxyException, ConnectionBusyException {
	    try {
	    	sftp = getSftpChannel() ;
	        sftp.get(downloadFile, os);
		} catch (JSchException e) {
			throw new ProxyException("连接失败!",e) ;
		} catch (SftpException e) {
			closeChannel() ;//如果下载失败需要关闭当前channel，否则其它操作都会失败
			if(e.getCause() instanceof ClientAbortException){//客户端终止下载，不做任何处理
				
			}else if(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE){
				throw new ProxyException("文件不存在！") ;
			}else if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}else{
				throw new ProxyException("下载文件失败!",e) ;
			}
		}finally{
			setBusy(false) ;
		}
	}
	/**
	 * 删除文件
	 *
	 * @param deleteFile 要删除的文件
	 * @param sftp
	 * @throws ProxyException 
	 * @throws ConnectionBusyException 
	 */
	public void delete(String deleteFile) throws ProxyException, ConnectionBusyException {
    	try {
			sftp = getSftpChannel() ;
			sftp.rm(deleteFile);
		} catch (JSchException e) {
			throw new ProxyException("连接失败!",e) ;
		} catch (SftpException e) {
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}else if(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE){
				throw new ProxyException("文件不存在！") ;
			}
			throw new ProxyException("删除文件失败!",e) ;
		}finally{
			setBusy(false) ;
		}
	}
	/**
	 * 列出目录下的文件
	 *
	 * @param directory 要列出的目录
	 * @param sftp
	 * @return
	 * @throws SftpException
	 * @throws JSchException 
	 * @throws ProxyException 
	 * @throws ConnectionBusyException 
	 */
	private Vector<LsEntry> listFiles(String directory,ChannelSftp sftp) throws ProxyException, ConnectionBusyException {
		try{
	    	return sftp.ls(directory);
		} catch (SftpException e) {
			closeChannel() ;
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}
			throw new ProxyException("获取目录文件失败!") ;
		}finally{
			setBusy(false) ;
		}
	}
	public List<FileEntry> listFileNames(String directory) throws ProxyException, ConnectionBusyException {
		try {
			ChannelSftp sftp = getSftpChannel() ;
			Vector<LsEntry> files = listFiles(directory,sftp) ;
			List<FileEntry> fileNames = new ArrayList<FileEntry>(files.size()) ;
			for(LsEntry entry:files){
				SftpATTRS attr = entry.getAttrs() ;
				fileNames.add(new FileEntry(entry.getFilename(), attr.isDir())) ;
			}
			return fileNames ;
		} catch (JSchException e) {
			throw new CommandException("连接服务器失败！",e) ;
		}
	}
	
	@Override
	public void deleteDir(String dir) throws ProxyException, ConnectionBusyException {
		try{
			sftp = getSftpChannel() ;
			iterateDel(dir, sftp) ;
		} catch (JSchException e) {
			throw new ProxyException("连接服务器失败!") ;
		}finally{
			setBusy(false) ;
		}
	}
	private void iterateDel(String dir,ChannelSftp sftp) throws ProxyException, ConnectionBusyException{
		try{
			Vector<LsEntry> list = listFiles(dir,sftp) ;
			for (LsEntry item : list) {
				String fileName = item.getFilename() ;
		        if (!item.getAttrs().isDir()) {
		            sftp.rm(dir + "/" + fileName); 
		        } else if (!".".equals(fileName) && !"..".equals(fileName)) {
		        	iterateDel(dir + "/" + fileName,sftp);
		        }
		    }
			sftp.rmdir(dir) ;
		} catch (SftpException e) {
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}else if(e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE){
				throw new ProxyException("文件不存在！") ;
			}
			throw new ProxyException("删除目录失败!") ;
		}
	}
	@Override
	public FileEntry getFile(String file) throws ProxyException, ConnectionBusyException {
		FileEntry entry = null;
		try{
			sftp = getSftpChannel() ;
	    	SftpATTRS attr = sftp.stat(file) ;
	    	if(attr == null){
	    		return null ;
	    	}
	    	int lastSlash = file.indexOf('/') ;
	    	String fileName = file.substring(lastSlash < 0 ? 0 : lastSlash+1) ;
	    	entry = new FileEntry(fileName, attr.isDir(), attr.getSize()) ;
		} catch (JSchException e) {
			throw new ProxyException("连接服务器失败!") ;
		} catch (SftpException e) {
			if(e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE){
				throw new ProxyException("获取文件失败!",e) ;
			}
		}finally{
			setBusy(false) ;
		}
		return entry ;
	}
	
	@Override
	public void createDir(String name) throws ProxyException,ConnectionBusyException {
		try{
			FileEntry file = getFile(name) ;
			if (file != null) {
				throw new ProxyException("相同名称的目录或文件已经存在！") ;
			}
			sftp = getSftpChannel() ;
			sftp.mkdir(name) ;
		} catch (JSchException e) {
			throw new ProxyException("连接服务器失败!") ;
		} catch (SftpException e) {
			if(e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED){
				throw new ProxyException("没有权限进行此操作！") ;
			}
			throw new ProxyException("创建目录失败!",e) ;
		}finally{
			setBusy(false) ;
		}
	}
	/**
	 * 关闭当前channel
	 */
	private synchronized void closeChannel(){
		try {
			if (sftp != null && sftp.isConnected()) {
				sftp.disconnect() ;
			}
			sftp = null ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}	
	}
	@Override
	public void close() {
		closeChannel() ;
		sshProxy.close();
	}
	@Override
	public void connect(int timeout) throws ProxyException {
		sshProxy.connect(timeout) ;
	}
	@Override
	public CommandResult exec(String command, int timeout)throws ProxyException, CommunicationExpirationException {
		return sshProxy.exec(command, timeout) ;
	}
	@Override
	public void cancel() {
		sshProxy.cancel() ;
	}
	
}
