package com.topsec.tsm.sim.common.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import com.topsec.tsm.ass.PageBean;

public abstract class HibernateDaoImpl<T,ID extends Serializable> implements BaseDao<T, ID> {

	private Class<T> entityClass ;
	private SessionFactory sessionFactory ;
	
	@SuppressWarnings("unchecked")
	public HibernateDaoImpl(){
		entityClass = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0] ;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession(){
		return sessionFactory.getCurrentSession() ;
	}
	
	@SuppressWarnings("unchecked")
	protected List<T> findByCriteria(Criterion... criterions){
		Criteria cri = createCriteria(criterions) ;
		return cri.list() ;
	}
	
	@SuppressWarnings("unchecked")
	protected T findFirstByCriteria(Criterion... criterions){
		Criteria cri = createCriteria(criterions) ;
		cri.setMaxResults(1) ;
		return (T) cri.uniqueResult() ;
	}
	
	@SuppressWarnings("unchecked")
	protected T findUniqueByCriteria(Criterion... criterions){
		Criteria cri = createCriteria(criterions) ;
		return (T)cri.uniqueResult() ;
	}
	protected Criteria createCriteria(Criterion... criterions){
		Criteria cri = getSession().createCriteria(entityClass) ;
		for(Criterion condition:criterions){
			cri.add(condition) ;
		}
		return cri ;
	}
	@SuppressWarnings("unchecked")
	@Override
	public T findById(ID id) {
		if (id == null) {
			return null ;
		}
		return (T) getSession().get(entityClass, id);
	}

	@Override
	public List<T> getAll() {
		return getSession().createCriteria(entityClass).list() ;
	}

	@Override
	public PageBean<T> search(int pageIndex, int pageSize,Map<String, Object> searchCondition) {
		return search(pageIndex, pageSize, searchCondition,new SimOrder[0]) ;
	}
	
	@Override
	public PageBean<T> search(int pageIndex, int pageSize,Map<String, Object> searchCondition, SimOrder... orders) {
		Criterion[] criterions = getSearchCriterions(searchCondition) ;
		Criteria cri = createCriteria(criterions) ;
		cri.setProjection(Projections.rowCount()) ;
		Number rowCount = (Number) cri.uniqueResult() ;
		cri.setProjection(null) ;
		cri.setResultTransformer(CriteriaSpecification.ROOT_ENTITY) ;
		cri.setFirstResult((pageIndex-1)*pageSize) ;
		cri.setMaxResults(pageSize) ;
		for(SimOrder od:orders){
			cri.addOrder(od.isAsc() ? Order.asc(od.getProperty()) : Order.desc(od.getProperty())) ;
		}
		PageBean<T> result = new PageBean<T>(pageIndex, pageSize, rowCount.intValue()) ;
		result.setData(cri.list()) ;
		return result ;
	}

	/**
	 * 根据用户搜索条件、Criterion查询对象<br>
	 * 此方法要求子类根据具体的业务来实现<br>
	 * 例如:<br>
	 * searchCondition = {name="flm",age=29} ;
	 * @return
	 */
	protected Criterion[] getSearchCriterions(Map<String,Object> searchCondition){
		return new Criterion[0] ;
	}

	@Override
	public List<T> findByCondition(Map<String, Object> condition) {
		throw new UnsupportedOperationException("findByCondition method in "+entityClass.getName()+" is not supported!") ;
	}
	
	/**
	 * 返回id
	 */
	@Override
	public Serializable save(T entity) {
		return getSession().save(entity) ;
	}

	@Override
	public void update(T entity) {
		getSession().update(entity) ;
	}

	@Override
	public void delete(T entity) {
		getSession().delete(entity) ;
	}

	@Override
	public void delete(ID id) {
		T t = (T) getSession().load(entityClass, id) ;
		delete(t) ;
	}

	@Override
	public void batchSave(List<T> list) {
		Session session = getSession();
		for(int i=0;i<list.size();i++){
			T e=list.get(i);
			session.save(e);
			if(i%10==0){
				session.flush();
			}
		}
	}

	@Override
	public int updateProperty(ID id,String propertyName, Object value) {
		StringBuffer updateSQL = new StringBuffer(256) ;
		String idPropertyName = getSessionFactory().getClassMetadata(entityClass).getIdentifierPropertyName() ;
		updateSQL.append("UPDATE ").append(entityClass.getName()).append(" _updateEntity ")
				 .append("SET ")
				 .append("_updateEntity.").append(propertyName).append("=:propertyValue").append(" ")
				 .append("WHERE ")
				 .append("_updateEntity.").append(idPropertyName).append("=:id");
		Query query  = getSession().createQuery(updateSQL.toString()) ;
		query.setParameter("propertyValue", value) ;
		query.setParameter("id", id) ;
		return query.executeUpdate() ;
	}
	
	@Override
	public T getTransient(ID id){
		T obj = findById(id) ;
		if (obj != null) {
			getSession().evict(obj) ;
		}
		return obj ;
	}
}
