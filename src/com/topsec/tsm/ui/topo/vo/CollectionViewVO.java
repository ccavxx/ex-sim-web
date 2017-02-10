package com.topsec.tsm.ui.topo.vo;

import com.topsec.tsm.ui.topo.bean.CollectionView;

public class CollectionViewVO {
	
	private long id;
	
	private String name;
	
	private long userId;
	
	private String url;
	
	private String type;
	
	private long time;
	
	private CollectionView collectionView;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CollectionView getCollectionView() {
		return collectionView;
	}

	public void setCollectionView(CollectionView collectionView) {
		this.collectionView = collectionView;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
   
}
