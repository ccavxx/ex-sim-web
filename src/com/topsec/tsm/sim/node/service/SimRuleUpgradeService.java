package com.topsec.tsm.sim.node.service;

import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.resource.persistence.SimRuleUpgrade;

/*
 * 功能描述：service层事件规则库升级对象接口
 */
public interface SimRuleUpgradeService {
	/**
	 * 事件规则库升级对象所有列表
	 * 
	 * @return
	 */
	public List<SimRuleUpgrade> list();

	/**
	 * 事件规则库升级对象带分页列表
	 * 
	 * @param pageNum
	 *           页码
	 * @param pageSize
	 *           每页显示数量
	 * @return
	 */
	public List<SimRuleUpgrade> getRecordList(int pageNum, int pageSize);

	/**
	 * 事件规则库升级对象记录数
	 * 
	 * @return
	 */
	public Long getRecordCount();

	/**
	 * 保存
	 * 
	 * @param SimRuleUpgrade
	 */
	public void save(SimRuleUpgrade SimRuleUpgrade);

	/**
	 * 删除
	 * 
	 * @param SimRuleUpgrade
	 */
	public void delete(SimRuleUpgrade SimRuleUpgrade);

	/**
	 * 获取最高版本的升级包
	 * 
	 * @return
	 */

	public Map<String, String> getMaxVersion();

	/**
	 * 通过名称获取事件规则库升级对象
	 * 
	 * @param fileName
	 *           文件名称
	 * @return
	 */
	public SimRuleUpgrade getSimRuleUpgradeByName(String fileName);
}
