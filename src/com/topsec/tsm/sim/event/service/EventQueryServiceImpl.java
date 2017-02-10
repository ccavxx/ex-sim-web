package com.topsec.tsm.sim.event.service;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.node.collector.DataConstants;
import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.sim.event.dao.EventQueryDao;
import com.topsec.tsm.ui.util.tree.TreeNode;


public class EventQueryServiceImpl implements EventQueryService {

    private Logger log = LoggerFactory.getLogger(EventQueryServiceImpl.class); 
    private EventQueryDao dao;
    private final Map priority=new HashMap(){{put("0","非常低");
                                        put("1","低");
                                        put("2","中");
                                        put("3","高");
                                        put("4","非常高");}} ;
//  private int iteratorNum=0;
    public TreeNode getDeviceTypeTree(String filePath) {
        //reading XML
        log.debug("正在读取：["+filePath+"]");
        // 读
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding("GB2312");
        Document document=null;
        try{
            document=saxReader.read(new File(filePath));
        }catch(Exception e)
        {
//            e.printStackTrace();
            log.error("reading deviceConfig.xml ERROR!"+e.getMessage());
        }
        TreeNode tn=new TreeNode();
        tn.setNodeId("402");
        //解析XML
        if(document!=null)
        {
//          Node node=document.selectSingleNode("MType/type[@id='10']");
//          document.getRootElement();
//          document.remove(document.selectSingleNode("MType/type[@id='10']"));
//          document.remove(document.selectSingleNode("MType/type[@id='11']"));
//          document.remove(document.selectSingleNode("MType/type[@id='12']"));
            log.debug("xml="+document.asXML());
            parseXMLForDeviceType(tn,document.getRootElement());
        }
        return tn;
    }
    
    public TreeNode getEventTypeTree(String filePath) {
        log.debug("正在读取：["+filePath+"]");
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding("GB2312");
        Document document=null;
        try{
            document=saxReader.read(new File(filePath));
        }catch(Exception e)
        {
            e.printStackTrace();
            log.error("reading eventtype.xml ERROR!"); 
        }
        TreeNode tn=new TreeNode();
        tn.setNodeId("402");
        //解析XML
        if(document!=null)
        {
            parseXMLForEventType(tn,document.getRootElement(),1);
        }
        return tn;
    }
    
    //根据 输入条件查询事件信息
    @SuppressWarnings("unchecked")
    public List getEvents(Condition searchParams) throws SQLException{
        List list=dao.getEvents(searchParams);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormate(list,simpleDateFormat);
        return list;
    }

    @SuppressWarnings("rawtypes")
	public List getEventsForFlex(Condition searchParams) throws SQLException{
        List list=dao.getEventsForFlex(searchParams);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormate(list,simpleDateFormat);
        return list;
    }

    
    public EventQueryDao getDao() {
        return dao;
    }

    public void setDao(EventQueryDao dao) {
        this.dao = dao;
    }

    private void parseXMLForDeviceType(TreeNode tn,Element element)
    {
        if(element!=null){
            TreeNode subNode=new TreeNode();
            if(element.attribute("id")!=null)
            {
                if(element.getParent()!=null&&(element==element.getParent().selectSingleNode("type[@id='10']")||
                        element==element.getParent().selectSingleNode("type[@id='11']")||
                        element==element.getParent().selectSingleNode("type[@id='12']")||
                        element==element.getParent().selectSingleNode("type[@id='9']")
                                ))return;
                subNode.setNodeId(element.attributeValue("id"));
                subNode.setNodeText(element.attributeValue("name"));
                subNode.setUrl("");
                if(element.elements().size()>0)
                {
                    for(int i=0;i<element.elements().size();i++)
                    {
                        parseXMLForDeviceType(subNode,(Element)element.elements().get(i));
                    }
                }
                tn.add(subNode);
            }else
            {
                if(element.elements().size()>0)
                {
                    if(element.getParent()!=null&&(element==element.getParent().selectSingleNode("type[@id='10']")||
                            element==element.getParent().selectSingleNode("type[@id='11']")||
                            element==element.getParent().selectSingleNode("type[@id='12']")||
                            element==element.getParent().selectSingleNode("type[@id='9']")
                            ))return;
                    for(int i=0;i<element.elements().size();i++)
                    {
                        parseXMLForDeviceType(tn,(Element)element.elements().get(i));
                    }
                }
//              tn.add(subNode);
            }
        }
    }
    
    private void parseXMLForEventType(TreeNode tn,Element element,int iteratorNum)
    {
        if(element!=null){
            TreeNode subNode=new TreeNode();
            if(element.attribute("id")!=null)
            {
                subNode.setNodeId(iteratorNum+element.attributeValue("id"));
                subNode.setNodeText(element.attributeValue("name"));
                subNode.setUrl("");
                if(element.elements().size()>0)
                {
                    for(int i=0;i<element.elements().size();i++)
                    {
                        parseXMLForEventType(subNode,(Element)element.elements().get(i),iteratorNum++);
                    }
                }
                tn.add(subNode);
            }else
            {
                if(element.elements().size()>0)
                {
                    for(int i=0;i<element.elements().size();i++)
                    {
                        parseXMLForEventType(tn,(Element)element.elements().get(i),iteratorNum++);
                    }
                }
//              tn.add(subNode);
            }
        }
    }

    public List getAdvancedEvents(Condition searchParams) throws SQLException {
        List list=dao.getAdvancedEvents(searchParams);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormate(list,simpleDateFormat);
        return list;
    }
    
    public List selectEventForPie(Condition searchParams) throws SQLException {
        List list=dao.selectEventForPie(searchParams);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormate(list,simpleDateFormat);
        return list;
    }
    
    /**
     * 2011年4月18日
     * 任占帅修改
     * 解决时间转换问题
     * 
     */
    private void dateFormate(List list,SimpleDateFormat simpleDateFormat){
        for (int i=0;i<list.size();i++){
            Map<String,Object> alarm=(Map<String,Object>)list.get(i);
            if(alarm.containsKey(DataConstants.START_TIME)){
                Object startTime=alarm.get(DataConstants.START_TIME);
                if(startTime instanceof Date){
                    alarm.put(DataConstants.START_TIME, StringUtil.longDateString((Date)startTime));
                }
            }
            if(alarm.containsKey(DataConstants.END_TIME)){
            	Object endTime=alarm.get(DataConstants.END_TIME);
                if(endTime instanceof Date){
                    alarm.put(DataConstants.END_TIME, StringUtil.longDateString((Date)endTime));
                }  
            }
            if(alarm.containsKey(DataConstants.AGT_RECEIPT_TIME)){
            	Object receiptTime=alarm.get(DataConstants.AGT_RECEIPT_TIME);
                if(receiptTime instanceof Date){
                    alarm.put(DataConstants.AGT_RECEIPT_TIME, StringUtil.longDateString((Date)receiptTime));
                } 
            }
        }
    }
    
    /* modify by yangxuanjia at 2011-01-27 start */
    @Override
    public List<Map> getCorrelatorData(Condition searchParameters)
            throws SQLException {
        List<Map> list=dao.getCorrelatorData(searchParameters);
        if(list!=null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormate(list,simpleDateFormat);
        }
        return list;
    }
    /* modify by yangxuanjia at 2011-01-27 end */
    /**
     * 按照事件名称统计事件发生次数
     * @return
     * @throws SQLException 
     */
    public List getEventStatistics() throws SQLException{
		return dao.getEventStatistics();
    }
    
    
    
	@Override
	public List<Map<String,Object>> getEventStatisticByCatergory(Map<String, String> categoryMap,boolean includeTime) throws SQLException {
		List<Map<String, Object>> statisticsMaps0 =new ArrayList<Map<String, Object>>();
		if(!includeTime){
			Map<String,Object> map=new HashMap<String, Object>();
			Integer count=	dao.getEventStatisticsByCategory(categoryMap);
			map.put("value", count);
			statisticsMaps0.add(map);
		}else{
			List<Map<String, Object>> statisticsMaps1 = dao.getEventMoreStatisticsByCategory(categoryMap);
			if(statisticsMaps1!=null){
				statisticsMaps0.addAll(statisticsMaps1);
			}
		}
		return  statisticsMaps0;
	}

	@Override
	public List<Map<String, Object>> getEventsTotalForFlex(Condition params,boolean includeTime) throws SQLException {
		List<Map<String, Object>> statisticsMaps0 =new ArrayList<Map<String, Object>>();
		if(!includeTime){
			Map<String,Object> map=new HashMap<String, Object>();
			Integer count = dao.getEventsTotalForFlex(params);
			map.put("value", count);
			statisticsMaps0.add(map);
		}else{
			List<Map<String, Object>> statisticsMaps1 =dao.getEventsTotalForMoreResult(params);
			if (statisticsMaps1!=null) {
				statisticsMaps0.addAll(statisticsMaps1);
				
			}
		}
		return statisticsMaps0;
	}

	@Override
	public List<Map<String, Object>> getAllEventStatistics() throws SQLException {
		return dao.getAllEventStatistics();
	}

	@Override
	public List getEventsTimeChart(Condition searchParameters)
			throws SQLException {
			List list=dao.getEventsTimeChart(searchParameters);
	        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        dateFormate(list,simpleDateFormat);
	        return list;
	}

	@Override
	public List<Map<String, Object>> getExistedEventNames(Map<String, Object> categoryMap)   {
		return dao.getExistedEventNames(categoryMap);
	}

	@Override
	public List<Map<String, Object>> getEventLogsById(Integer id) {
		return dao.getEventLogsByEvtId(id);
	}
	
	@Override
	public List<Map<String, Object>> getLogsByUUID(String uuid) {
		return dao.getLogsByUUID(uuid);
	}

	@Override
	public List<Map<String, Object>> cat1Statistic() {
		return dao.cat1Statistic();
	}

	@Override
	public List<Map<String, Object>> cat2Statistic(String cat1) {
		return dao.cat2Statistic(cat1) ;
	}

	@Override
	public List<Map<String, Object>> nameStatBaseOnCat(String cat1, String cat2) {
		return dao.nameStatBaseOnCat(cat1,cat2);
	}

	@Override
	public void updateEvent(String event_id, Integer confirm, String confirm_person) {
		dao.update( event_id, confirm, confirm_person);
	}
	
}
