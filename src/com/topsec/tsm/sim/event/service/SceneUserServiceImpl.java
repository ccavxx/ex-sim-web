package com.topsec.tsm.sim.event.service;

import java.util.List;

import com.topsec.tsm.sim.auditnode.AuditNode;
import com.topsec.tsm.sim.event.dao.SceneUserDao;
import com.topsec.tsm.sim.sceneUser.persistence.SceneUser;

public class SceneUserServiceImpl implements SceneUserService {

	private SceneUserDao sceneUserDao;

	/**
	 * 保存
	 * 
	 * @param entity
	 *           要保存的实体
	 */
	@Override
	public void save(Object entity) {
		sceneUserDao.save(entity);
	}

	/**
	 * 删除
	 * 
	 * @param id
	 *           要删除实体的id
	 */
	@Override
	public void delete(Integer id) {
		sceneUserDao.delete(id);
	}

	/**
	 * 修改
	 * 
	 * @param entity
	 *           要修改的实体
	 */
	@Override
	public void update(Object entity) {
		SceneUser sceneUser = (SceneUser)entity ;
		sceneUserDao.deleteColumns(sceneUser.getId()) ;
		sceneUserDao.update(entity);
	}

	/**
	 * 全部记录
	 * 
	 * @return 全部记录
	 */
	@Override
	public List list() {
		return sceneUserDao.list();
	}

	@Override
	public boolean modifyStatus(int sceneId,int status) {
		SceneUser su = (SceneUser) getById(sceneId) ;
		su.setStatus(status) ;
		return true;
	}

	/**
	 * 通过id获取实体
	 * 
	 * @param id
	 *           要查询实体的id
	 * @return 实体
	 */
	@Override
	public Object getById(Integer id) {
		Object obj = sceneUserDao.getById(id);

		return obj;
	}

	public SceneUserDao getSceneUserDao() {
		return sceneUserDao;
	}

	public void setSceneUserDao(SceneUserDao sceneUserDao) {
		this.sceneUserDao = sceneUserDao;
	}

	/**
	 * 全部记录的数量
	 * 
	 * @return 全部实体记录数量
	 */
	@Override
	public Long getRecordCount() {
		return sceneUserDao.getRecordCount();
	}

	/**
	 * 分页查询列表
	 * 
	 * @param pageNum
	 *           当前页
	 * @param pageSize
	 *           每页显示数量
	 * @return 分页列表实体对象
	 */
	@Override
	public List getRecordList(int pageNum, int pageSize) {
		List<AuditNode> list = sceneUserDao.getRecordList(pageNum, pageSize);
		return list;
	}

	@Override
	public Object getById(Class clazz, Integer id) {
		return sceneUserDao.getById(clazz, id);
	}

	@Override
	public Long getRecordCount(Class clazz) {
		return sceneUserDao.getRecordCount(clazz);
	}

	@Override
	public List getRecordList(int pageNum, int pageSize, Class clazz) {
		return sceneUserDao.getRecordList(pageNum, pageSize, clazz);
	}

	@Override
	public List list(Class clazz) {
		return sceneUserDao.list(clazz);
	}

	@Override
	public void delete(Integer id, Class clazz) {
		sceneUserDao.delete(id, clazz);
	}

	@Override
	public Object getByIp(String ip, Class clazz) {
		return sceneUserDao.getByIp(ip, clazz);
	}

	@Override
	public Object getByName(String name, Class clazz) {
		return sceneUserDao.getByName(name, clazz);
	}
}
