package com.topsec.tsm.sim.auth.form;

import java.sql.Timestamp;

import com.topsec.tsm.auth.manage.baseInfoUtil.BaseInfo;

/**
 * @author lei_peng
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class RoleForm {
	private BaseInfo baseInfo;
	private String textPermissionMap;
	private String page;

	/**
	 * @return
	 */
	public boolean isCreate() {
		return baseInfo.isCreate();
	}

	/**
	 * @return
	 */
	public boolean isDelete() {
		return baseInfo.isDelete();
	}

	/**
	 * @return
	 */
	public boolean isExecute() {
		return baseInfo.isExecute();
	}

	/**
	 * @return
	 */
	public boolean isRead() {
		return baseInfo.isRead();
	}

	/**
	 * @return
	 */
	public boolean isWrite() {
		return baseInfo.isWrite();
	}


	public RoleForm() {
		baseInfo = new BaseInfo();
		textPermissionMap = "";
		page = null;
	}



	/**
	 * @return
	 */
	public String getCreationIp() {
		return baseInfo.getCreationIp();
	}

	/**
	 * @return
	 */
	public Timestamp getCreationTime() {
		return baseInfo.getCreationTime();
	}

	/**
	 * @return
	 */
	public String getCreator() {
		return baseInfo.getCreator();
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return baseInfo.getDescription();
	}

	/**
	 * @return
	 */
	public Integer getId() {
		return baseInfo.getId();
	}

	/**
	 * @return
	 */
	public String getLastModifyIp() {
		return baseInfo.getLastModifyIp();
	}

	/**
	 * @return
	 */
	public Timestamp getLastModifyTime() {
		return baseInfo.getLastModifyTime();
	}

	/**
	 * @return
	 */
	public String getName() {
		return baseInfo.getName();
	}

	/**
	 * @param creationIp
	 */
	public void setCreationIp(String creationIp) {
		baseInfo.setCreationIp(creationIp);
	}

	/**
	 * @param creationTime
	 */
	public void setCreationTime(Timestamp creationTime) {
		baseInfo.setCreationTime(creationTime);
	}

	/**
	 * @param creator
	 */
	public void setCreator(String creator) {
		baseInfo.setCreator(creator);
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		baseInfo.setDescription(description);
	}

	/**
	 * @param id
	 */
	public void setId(Integer id) {
		baseInfo.setId(id);
	}

	/**
	 * @param lastModifyIp
	 */
	public void setLastModifyIp(String lastModifyIp) {
		baseInfo.setLastModifyIp(lastModifyIp);
	}

	/**
	 * @param lastModifyTime
	 */
	public void setLastModifyTime(Timestamp lastModifyTime) {
		baseInfo.setLastModifyTime(lastModifyTime);
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		baseInfo.setName(name);
	}

	/**
	 * @return 返回 baseInfo。
	 */
	public final BaseInfo getBaseInfo() {
		return baseInfo;
	}

	/**
	 * @param baseInfo
	 *           要设置的 baseInfo。
	 */
	public final void setBaseInfo(BaseInfo baseInfo) {
		this.baseInfo = baseInfo;
	}

	/**
	 * @return
	 */
	public String getLastModifyUser() {
		return baseInfo.getLastModifyUser();
	}

	/**
	 * @param lastModifyUser
	 */
	public void setLastModifyUser(String lastModifyUser) {
		baseInfo.setLastModifyUser(lastModifyUser);
	}


	/**
	 * @return 返回 textPermissionMap。
	 */
	public final String getTextPermissionMap() {
		return textPermissionMap;
	}

	/**
	 * @param textPermissionMap
	 *           要设置的 textPermissionMap。
	 */
	public final void setTextPermissionMap(String textPermissionMap) {
		this.textPermissionMap = textPermissionMap;
	}

	/**
	 * @return 返回 page。
	 */
	public final String getPage() {
		return page;
	}

	/**
	 * @param page
	 *           要设置的 page。
	 */
	public final void setPage(String page) {
		this.page = page;
	}
}
