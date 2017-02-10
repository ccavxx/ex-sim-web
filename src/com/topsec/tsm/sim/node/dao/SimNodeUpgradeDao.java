package com.topsec.tsm.sim.node.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.common.dao.BaseDao;
import com.topsec.tsm.sim.resource.persistence.SimNodeUpgrade;

/*
 * 功能描述：dao层升级对象接口
 */
public interface SimNodeUpgradeDao extends BaseDao<SimNodeUpgrade, Integer>{
	/**
	 * 升级对象所有列表
	 * 
	 * @return
	 */
	public List<SimNodeUpgrade> list();

	/**
	 * 升级对象带分页列表
	 * 
	 * @param pageNum
	 *           页码
	 * @param pageSize
	 *           每页显示数量
	 * @return
	 */
	public List<SimNodeUpgrade> getRecordList(int pageNum, int pageSize);

	/**
	 * 升级对象记录数
	 * 
	 * @return
	 */
	public long getRecordCount();

	/**
	 * 保存
	 * 
	 * @param simNodeUpgrade
	 */
	public Serializable save(SimNodeUpgrade simNodeUpgrade);

	/**
	 * 删除
	 * 
	 * @param simNodeUpgrade
	 */
	public void delete(SimNodeUpgrade simNodeUpgrade);

	/**
	 * 获取类型为type的所有升级对象中最高的一个
	 * 
	 * @param type
	 *           类型
	 * @param versionFrom
	 *           版本
	 * @return
	 */

	public Map<String, String> getMaxVersionStrByType(String type, String versionFrom);

	/**
	 * 通过名称获取升级对象
	 * 
	 * @param fileName
	 *           文件名称
	 * @return
	 */
	public SimNodeUpgrade getSimNodeUpgradeByName(String fileName);

}
