package com.topsec.tsm.sim.asset.web.vtclient;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
/**
 * 远程文件浏览
 * @author hp
 *
 */
public abstract class FileBrowser extends ConnectionProxy{
	
	public FileBrowser(String sessionId, String ip, String name,String password, int port) {
		super(sessionId, ip, name, password, port);
	}
	
	/**
	 * 上传文件
	 * @param uploadFile
	 * @param dst
	 * @throws ProxyException
	 * @throws FileNotFoundException
	 */
	public abstract void upload(String uploadFile,String dst) throws ProxyException,ConnectionBusyException, FileNotFoundException ;
	/**
	 * 上传文件 
	 * @param uploadFile
	 * @param dst
	 * @throws ProxyException
	 * @throws FileNotFoundException
	 */
	public abstract void upload(InputStream is,String dst) throws ProxyException,ConnectionBusyException ;
	
	/**
	 * 下载文件
	 * @param downloadFile
	 * @param os
	 * @throws ProxyException
	 */
	public abstract void download(String downloadFile, OutputStream os) throws ProxyException,ConnectionBusyException ; 
	/**
	 * 删除文件或目录
	 * @param deleteFile
	 * @throws ProxyException
	 */
	public abstract void delete(String deleteFile) throws ProxyException,ConnectionBusyException  ;
	/**
	 * 删除目录
	 * @param dir
	 * @throws ProxyException
	 */
	public abstract void deleteDir(String dir) throws ProxyException,ConnectionBusyException  ;
	/**
	 * 列出目录文件
	 * @param directory
	 * @return
	 */
	public abstract List<FileEntry> listFileNames(String directory) throws ProxyException,ConnectionBusyException;
	/**
	 * 获取文件的信息
	 * @param file
	 * @return
	 */
	public abstract FileEntry getFile(String file) throws ProxyException,ConnectionBusyException ;
	/**
	 * 创建目录
	 * @param name
	 */
	public abstract void createDir(String name)throws ProxyException,ConnectionBusyException ;
}
