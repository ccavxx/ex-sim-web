package com.topsec.tsm.sim.kb.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.sim.event.EventAssocKb;
import com.topsec.tsm.sim.kb.KBEvent;
import com.topsec.tsm.sim.kb.bean.KnowledgeQueryBean;
import com.topsec.tsm.sim.kb.dao.KnowledgeAsscoDao;
import com.topsec.tsm.sim.kb.dao.KnowledgeDao;

public class KnowledgeServiceImpl implements KnowledgeService {
	
	
	protected static Logger log= LoggerFactory.getLogger(KnowledgeServiceImpl.class);
	
	private KnowledgeDao knowledgeDao; 
	private KnowledgeAsscoDao knowledgeAsscoDao;
	
	public void setKnowledgeDao(KnowledgeDao knowledgeDao) {
		this.knowledgeDao = knowledgeDao;
	}
	
	public void setKnowledgeAsscoDao(KnowledgeAsscoDao knowledgeAsscoDao) {
		this.knowledgeAsscoDao = knowledgeAsscoDao;
	}


	/**
	 * @method 获取所有事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return List
	 */
	@Override
	public List<KBEvent> getEvents(){
		return knowledgeDao.getAll();
	}
	/**
	 * @method 添加事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	@Override
	public void addKBEvent(KBEvent event){
		knowledgeDao.save(event);
	}
	/**
	 * @method 删除事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	@Override
	public void deleteKBEvent(KBEvent event){
		knowledgeDao.delete(event);
	}
	/**
	 * @method 修改事件规则知识库信息
	 * @author zhou_xiaohu
	 * @return NULL
	 */
	@Override
	public void updateKBEvent(KBEvent event){
		knowledgeDao.update(event);
	}
	@Override
	public Map<String, Object> getKBEventsByPage(KnowledgeQueryBean knowledgeQueryBean) {
		Map<String,Object> cmap=new HashMap<String, Object>();
		Class<? extends KnowledgeQueryBean> clazz = knowledgeQueryBean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Method[] method = clazz.getMethods();
		try {
			for (Field field : fields) {
				String name=field.getName();
				for (int i = 0; i < method.length; i++) {
				 Class<?>[] pts = method[i].getParameterTypes();
					 if(pts==null||pts.length==0){
						 String methodName = method[i].getName();
						 if(methodName.toLowerCase().indexOf(name.toLowerCase())!=-1&&methodName.startsWith("get")){
							Object val= method[i].invoke(knowledgeQueryBean,null);
							if(val!=null&&!val.equals("")){
								cmap.put(name,val);
							}
						 }
					 }
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		cmap.remove("rows");
		cmap.remove("page");
		
		Integer total = knowledgeDao.getCountByCondition(cmap);
		List<KBEvent> list=knowledgeDao.findByPages(cmap,knowledgeQueryBean.getPage(),knowledgeQueryBean.getRows());
		Map<String,Object> rMap=new HashMap<String, Object>();
		rMap.put("total", total);
		rMap.put("rows", list);
		return rMap;
	}
	@Override
	public boolean deleteKBEventById(Integer... id) {
		try {
			if(id != null && id.length > 0) {
				for (int i = 0; i < id.length; i++) {
					KBEvent kBEvent = knowledgeDao.findById(id[i]);
					EventAssocKb kb2 = knowledgeAsscoDao.findById(id[i]);

					if(kBEvent != null) {
						knowledgeDao.delete(id[i]);
					}
					if(kb2 != null) {
						// 删除关联
						knowledgeAsscoDao.delete(id[i]);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}
	@Override
	public boolean associate2Knowledge(EventAssocKb... assocKbs) {
		try {
			knowledgeAsscoDao.batchSave(Arrays.asList(assocKbs));
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public List<KBEvent> getAssociatedKnowledgeByEvtName(String name) {
		return knowledgeDao.findByEvtName(name);
	}

	@Override
	public void deleteKnAssoc(String name) {
		knowledgeAsscoDao.deleteByEventName(name);
	}

	@Override
	public void updateKnAssocByEvtName(String eventName, Integer... knid) {
		
		if(knid==null){
			return;
		}
		 
		List<Integer> knidList=new ArrayList<Integer>()/*Arrays.asList(knid)*/;
		Collections.addAll(knidList, knid);
		Map<String, Object> condition=new HashMap<String, Object>();
		condition.put("event", eventName);
		List<EventAssocKb> eventAssocKbs=knowledgeAsscoDao.findByCondition(condition); 
		if(eventAssocKbs!=null&&eventAssocKbs.size()>0){
			List<EventAssocKb> eList=new ArrayList<EventAssocKb>();
			for (EventAssocKb eventAssocKb : eventAssocKbs) {
				boolean isexit=false;
				for (int i = 0; i < knid.length; i++) {
					if(eventAssocKb.getKnId()==knid[i]){
						isexit=true;
						eList.add(eventAssocKb);//有相同的关联
					}
				}
				if(!isexit){//删除不需要的关联
					knowledgeAsscoDao.delete(eventAssocKb);
				}
			}
			
			for (EventAssocKb eventAssocKb : eList) {//剔除相同关联
				int ix=-1;
				if((ix=knidList.indexOf(eventAssocKb.getKnId()))!=-1){
					knidList.remove(ix);
				}
			}
			
		}
		
		
		//保存不存在的关联
		if(knidList.size()!=0){
			EventAssocKb[] kbs=new EventAssocKb[knidList.size()];
			for (int i = 0; i < kbs.length; i++) {
				kbs[i]=new EventAssocKb(eventName,knidList.get(i));
			}
			this.associate2Knowledge(kbs);
		}
		
	}

	@Override
	public List<KBEvent> getKnowledgeByCategory(Map<String, Object> categoryMap) {
		if(categoryMap==null){
			return knowledgeDao.getKnowledgesNoCategory();
		}
		return knowledgeDao.findByCondition(categoryMap);
	}

	
	@Override
	public List<KBEvent> getAssociatedKnowledgeByEvtRuleId(Integer id) {
		return knowledgeDao.findByEvtRuleId(id);
	}

	@Override
	public KBEvent getKnowledgeById(Integer id) {
		return knowledgeDao.findById(id);
	}

	/**
	 * 按照事件ID查询关联解决方案
	 */
	@Override
	public List<KBEvent> getAssociatedKnowledgeByEvtId(float id) {
		return knowledgeDao.findByEvtId(id);
	}

	@Override
	public List<KBEvent> getAssociatedKnowledgeByGid(Integer id) {
		return knowledgeDao.findByGid(id);
	}
	
	@Override
	public List<KBEvent> getAssociatedKnowledgeByEvtIdAndEndTime(float id, String end_time) {
		return knowledgeDao.findByEvtIdAndEndTime(id, end_time);
	}
}
