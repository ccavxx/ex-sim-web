package com.topsec.tsm.sim.kb.dao;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.event.EventAssocKb;
import com.topsec.tsm.sim.kb.KBEvent;

public class KnowledgeDaoImpl  extends HibernateDaoImpl<KBEvent, Integer> implements KnowledgeDao{

	private SqlMapClient sqlMapClient;
	
	@Override
	public List<KBEvent> findByPages(Map<String, Object> cmap, int page, int rows) {
		Criteria criteria = this.getSession().createCriteria(KBEvent.class);
		criteria = getKBEventCriteria(cmap, criteria);
		return criteria.setFirstResult((page-1)*rows).setMaxResults(rows).list();
	}

	private Criteria getKBEventCriteria(Map<String, Object> cmap, Criteria criteria) {
		if(cmap!=null&&cmap.size()>0){
			Set<Entry<String, Object>> cset = cmap.entrySet();
			for (Entry<String, Object> entry : cset) {//description solution
				String colname= entry.getKey();
				
				if("description".equals(colname)||"solution".equals(colname)||"name".equals(colname)){
						criteria.add(Restrictions.like(colname, "%"+entry.getValue()+"%"));
				}else{
					criteria=criteria.add(Restrictions.eq(colname, entry.getValue()));
				}
			}
		}
		return criteria;
	}

	@Override
	public Integer getCountByCondition(Map<String, Object> cmap) {
		Criteria criteria = this.getSession().createCriteria(KBEvent.class);
		criteria = getKBEventCriteria(cmap, criteria);
		Integer uniqueResult=(Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
		return uniqueResult;
	}

	@Override
	public List<KBEvent> findByEvtName(String name) {
		return   this.getSession().createQuery("select kb from KBEvent kb, EventAssocKb ea  where kb.id=ea.knId and ea.event = :name")
				  .setParameter("name", name).list();
	}

	@Override
	public List<KBEvent> getKnowledgesNoCategory() {
		Criteria criteria = this.getSession().createCriteria(KBEvent.class);
		return criteria.add(Restrictions.isNull("cat1id")).add(Restrictions.isNull("cat2id")).list();
	}

	@Override
	public List<KBEvent> findByCondition(Map<String, Object> condition) {
		Criteria criteria = this.getSession().createCriteria(KBEvent.class);
		Set<Entry<String, Object>> entrySet = condition.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			String key=entry.getKey();
			Object val=entry.getValue();
			if(val==null){
				criteria=criteria.add(Restrictions.isNull(key));
			}else{
				criteria=criteria.add(Restrictions.eq(key, val));
			}
		}
		return criteria.list();
	}

	@Override
	public List<KBEvent> findByEvtRuleId(Integer id) {
		return this.getSession().createQuery("select kb from KBEvent kb,EventAssocKb ea,EventRule er where kb.id=ea.knId and er.name=ea.event and er.id = :evtid")
								.setParameter("evtid", id).list();
	}

	@Override
	public List<KBEvent> findByGid(Integer id) {
		return  this.getSession().createQuery("select kb from KBEvent kb,EventAssocKb ea,EventRuleGroup eg where kb.id=ea.knId and eg.groupName=ea.event and eg.groupId = :gid")
				.setParameter("gid", id).list();
	}

	@Override
	public List<KBEvent> findByEvtId(float id) {
		String[] props={"id","name","cat1id","cat2id","priority","description","solution","createTime","creater"};
		List<KBEvent> list=new ArrayList<KBEvent>();
		try {
			Map<String,Float> paramMap=new HashMap<String,Float>();
			paramMap.put("event_id", id);//hibernate decimal类型不对付
			List<Map<String,Object>> rowList = this.sqlMapClient.queryForList("getEventKnowledgeByEvtId", paramMap);
			if(rowList!=null){
				for (Map<String, Object> map : rowList) {
					KBEvent kbEvent=new KBEvent();
					for (int i = 0; i < props.length; i++) {
						BeanUtils.setProperty(kbEvent, props[i], map.get(props[i]));
					}
					list.add(kbEvent);
				}
			}
	 
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return list;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	@Override
	public List<KBEvent> findByEvtIdAndEndTime(float id, String end_time) {
		String[] props={"id","name","cat1id","cat2id","priority","description","solution","createTime","creater"};
		List<KBEvent> list=new ArrayList<KBEvent>();
		try {
			Map<String,Object> paramMap=new HashMap<String,Object>();
			paramMap.put("event_id", id);
			paramMap.put("end_time", end_time);
			List<Map<String,Object>> rowList = this.sqlMapClient.queryForList("getEventKnowledgeByEvtId", paramMap);
			if(rowList!=null){
				for (Map<String, Object> map : rowList) {
					KBEvent kbEvent=new KBEvent();
					for (int i = 0; i < props.length; i++) {
						BeanUtils.setProperty(kbEvent, props[i], map.get(props[i]));
					}
					list.add(kbEvent);
				}
			}
	 
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return list;
	}
}
