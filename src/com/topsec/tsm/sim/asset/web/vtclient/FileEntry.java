package com.topsec.tsm.sim.asset.web.vtclient;

public class FileEntry {

	private String name ;
	private long size ;
	private boolean isDir ;
	
	public FileEntry(String name, boolean isDir) {
		this(name,isDir,0) ;
	}
	public FileEntry(String name, boolean isDir, long size) {
		super();
		this.name = name;
		this.size = size;
		this.isDir = isDir;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public boolean isDir() {
		return isDir;
	}
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}
	
}
