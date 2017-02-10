package com.topsec.tsm.sim.event.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.topsec.tsm.sim.event.bean.Condition;
import com.topsec.tsm.ui.util.tree.TreeNode;

public interface EventQueryService {
    /**
     * 閻犲洩顕цぐ鍥╂媼閹屾▋缂侇偉顕ч悗锟絰ml
     * @param filePath
     */
    public TreeNode getDeviceTypeTree(String filePath);
    /**
     * 闁哄秷顬冨畵渚�级閳ュ弶顐介柡灞诲劥椤曟绂嶇�鈺傤偨
     * @param searchParameters
     * @return
     */
    public List getEvents(Condition searchParameters) throws SQLException;
    /**
     * 濡ゅ倹顭囨鍥蓟閵夘煈鍤勫ù婊冾儎濞嗭拷
     * @param searchParameters
     * @return
     */
    public List getAdvancedEvents(Condition searchParameters) throws SQLException;
    /**
     * 濡ゅ倹顭囨鍥蓟閵夘煈鍤勫ù婊冾儎濞嗭拷
     * @param searchParameters
     * @return
     */
    public List selectEventForPie(Condition searchParameters) throws SQLException;
    /**
     * 闁告瑦鐗曠欢杈ㄧ鐎ｂ晜顐界紒顐ヮ嚙閻庯拷xml
     * @param filePath
     * @return
     */
    public TreeNode getEventTypeTree(String filePath);
    /* modify by yangxuanjia at 2011-01-27 start */
    /**
     *  鐎电増顨滈崺宀勫礂鐎圭姳绮撻柛鎺戞閻庤姤绂嶇�鈺傤偨閻犲浄濡囩划蹇旂┍閳╁啩绱�
     */
    public List<Map> getCorrelatorData(Condition searchParameters) throws SQLException;
    /* modify by yangxuanjia at 2011-01-27 end */
    
    /**
     * 闁哄秷顬冨畵渚�级閳ュ弶顐介柡灞诲劥椤曟绂嶇�鈺傤偨闂傚棗妫楅幃锟�     * @param searchParameters
     * @return
     */
    public List getEventsForFlex(Condition searchParameters) throws SQLException;
   /**
    * 闁哄秷顬冨畵渚�级閳ュ弶顐介柡灞诲劥椤曟绂嶇�鈺傤偨闂傚棗妫楅幃搴ㄥ箑缂佹ɑ娈�
    * @param searchParameters
    * @return
    */
    public  List<Map<String, Object>> getEventsTotalForFlex(Condition params,boolean includeTime)throws SQLException;
    /**
     * 闁圭顦介崣搴㈢鐎ｂ晜顐介柛姘Ф琚ㄧ紓浣哄枙椤撳憡绂嶇�鈺傤偨闁告瑦鍨归弫鎾斥枎閳╁啯娈�
     * @return
     * @throws SQLException 
     */
    public List getEventStatistics() throws SQLException;
	
	public List<Map<String,Object>>  getEventStatisticByCatergory(Map<String, String> categoryMap,boolean includeTime) throws SQLException;
	
	public List<Map<String, Object>> getAllEventStatistics()throws SQLException;
	/**
	 * 鑾峰彇浜嬩欢鏃堕棿杞�
	 * @param searchParameters
	 * @return
	 * @throws SQLException
	 */
	public List getEventsTimeChart(Condition searchParameters) throws SQLException;
	
	
	/**
	 * 鏌ヨ宸茬粡鐢熸垚浜嬩欢鐨勮鍒欏悕绉帮紙涔熷氨鏄簨浠跺悕绉帮紝鍙兘瑙勫垯宸茬粡鍒犻櫎锛�
	 * @author zhaojun 2014-6-6涓嬪崍3:17:08
	 * @param categoryMap
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> getExistedEventNames(Map<String, Object> categoryMap) ;
	
	public List<Map<String, Object>> getEventLogsById(Integer id);
	
	public List<Map<String, Object>> getLogsByUUID(String uuid);
	
	public List<Map<String,Object>> cat1Statistic() ;
	
	public List<Map<String,Object>> cat2Statistic(String cat1) ;
	
	public List<Map<String, Object>> nameStatBaseOnCat(String recode, String recode2);
	
	public void updateEvent(String event_id, Integer confirm, String confirm_person);
}
