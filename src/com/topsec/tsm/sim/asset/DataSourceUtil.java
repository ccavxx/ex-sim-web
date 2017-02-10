package com.topsec.tsm.sim.asset;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.EnhanceProperties;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.xml.XmlAccessException;
import com.topsec.tsm.collector.datasource.DataSource;
import com.topsec.tsm.collector.datasource.JobDataSource;
import com.topsec.tsm.collector.datasource.SourceControlBean;
import com.topsec.tsm.collector.datasource.foramter.DataSourceElementFormater;
import com.topsec.tsm.collector.datasource.loader.SourcesLoaderSupport;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.asset.DeviceTypeShortKeyUtil.ShortKey;
import com.topsec.tsm.sim.asset.web.DataSourceNode;
import com.topsec.tsm.sim.common.tree.DefaultTreeIterator;
import com.topsec.tsm.sim.common.tree.FastJsonResult;
import com.topsec.tsm.sim.common.tree.FastJsonTreeVisitor;
import com.topsec.tsm.sim.common.tree.VisitResultListener;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.util.actiontemplate.ActionTemplate;
import com.topsec.tsm.util.actiontemplate.ActionTemplateElementFormater;
import com.topsec.tsm.util.xml.DefaultDocumentFormater;

public class DataSourceUtil {
	
	public static final String DEFAULT_DATASOURCE_LOCATIOIN = "../../../../conf/agent/datasourcetemplate" ;
	public static final String DEFAULT_MONITOR_LOCATION = "../../../../conf/agent/monitordatasourcetemplate" ;
	/**自审计日志*/
	public static final String SYSTEM_LOG = "Esm/Topsec/SystemLog" ;
	/**系统日志*/
	public static final String SYSTEM_RUN_LOG = "Esm/Topsec/SystemRunLog" ;
	private static final Map<String,ActionTemplate> DATASOURCE_TEMPLATE_CACHE = new HashMap<String, ActionTemplate>(8,1.0F) ;
	/**
	 * 获取日志源列表
	 * @param ownGroup　日志源类型 
	 * @return
	 */
	public static Collection<DataSource> getDataSourceList(String ownGroup) {
		DataSourceElementFormater formater = new DataSourceElementFormater();
		if (ownGroup == null) {
			throw new NullPointerException("ownGroup") ;
		}
		EnhanceProperties configFile = PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH) ; 
		String dataSourceLocation = "";
		if (SimDatasource.DATASOURCE_TYPE_LOG.equals(ownGroup)) {
			dataSourceLocation = configFile.getProperty("datasourcetemplate.path", DEFAULT_DATASOURCE_LOCATIOIN) ;
		} else if (SimDatasource.DATASOURCE_TYPE_MONITOR.equals(ownGroup)) {
			dataSourceLocation = configFile.getProperty("monitordatasourcetemplate.path",DEFAULT_MONITOR_LOCATION) ;
		}
		File dataSourceDir = new File(dataSourceLocation);
		File[] fileArray = null;
		Map<String, DataSource> datas = new HashMap<String,DataSource>();
		if (dataSourceDir.exists() && dataSourceDir.isDirectory()) {
			fileArray = dataSourceDir.listFiles();
		}
		for (File file:fileArray) {
			datas.putAll(SourcesLoaderSupport.loaderSources(file.getAbsolutePath(), formater));
		}
		return datas.values();
	}
	/**
	 * 根据日志源securityObjectType才"/"分割，将日志源转换为一个树形结构<br>
	 * securityObjectType被分为三级、第一级为日志源分类、第二级为厂商、第三级为产品<br>
	 * 如果securityObjectType为四级，第三级与第四级合并为第三级
	 * @param ownGroup
	 * @return
	 */
	public List<DataSourceNode> getTree(String ownGroup,String deviceType){
		DataSourceNode root = getTreeWithRoot(ownGroup,deviceType) ;
		return root.getChildren() ;
	}
	/**
	 * 构建日志源树
	 * @param dataSources
	 * @return
	 */
	public static DataSourceNode buildTree(Collection<DataSource> dataSources){
		String rootId = "" ;
		DataSourceNode root = new DataSourceNode(rootId,"",null,-1) ;
		DeviceTypeShortKeyUtil nameUtil = DeviceTypeShortKeyUtil.getInstance() ;
		for(DataSource dataSource:dataSources){
			String securityObjectType = dataSource.getSecurityObjectType() ;
			int level1Index = securityObjectType.indexOf('/') ;//第一级分类
			String assetCategory = securityObjectType.substring(0,level1Index) ;//分类
			int level2Index = securityObjectType.indexOf('/', level1Index+1) ;//第二级分类
			String vendor = securityObjectType.substring(level1Index+1, level2Index);//厂商
			String product = securityObjectType.substring(level2Index+1) ;//产品
			DataSourceNode assetCategoryNode = root.getChildById(assetCategory) ;
			if(assetCategoryNode == null){
				ShortKey cnKey = nameUtil.getShortKey(assetCategory) ;
				assetCategoryNode = new DataSourceNode(assetCategory,cnKey.cn, root,cnKey.order) ; 
				root.addChild(assetCategoryNode) ;
			}
			DataSourceNode vendorNode = assetCategoryNode.getChildById(vendor) ;
			if (vendorNode == null) {
				ShortKey cnKey = nameUtil.getShortKey(vendor) ;
				vendorNode = new DataSourceNode(vendor,cnKey.cn, assetCategoryNode,cnKey.order) ;
				assetCategoryNode.addChild(vendorNode) ;
			}
			DataSourceNode productNode = vendorNode.getChildById(product) ;
			if (productNode == null) {
				ShortKey cnKey = nameUtil.getShortKey(product.replace('/', ' ')) ;
				productNode = new DataSourceNode(product,cnKey.cn, vendorNode,cnKey.order) ;
				productNode.addDataSource(dataSource) ;
				vendorNode.addChild(productNode) ;
			}else{
				productNode.addDataSource(dataSource) ;
			}
		}
		return root ;
	}
	/**
	 * 日志源被分类后的树结构的根节点
	 * @param ownGroup
	 * @param deviceType 
	 * @return
	 */
	public static DataSourceNode getTreeWithRoot(String ownGroup, String deviceType){
		Collection<DataSource> dataSources = getSupportDataSources(ownGroup,deviceType) ;
		return buildTree(dataSources) ;
	}
	
	/**
	 * 根据日志源类型和收集方式，返回对应的日志源的actionTemplate
	 * @param securityObjectType　日志源类型
	 * @param collectType 收集方式
	 * @return
	 */
	public static ActionTemplate getDataSourceTemplate(String securityObjectType,String collectType,String ownGroup){
		String templateKey = securityObjectType + ":" + collectType + ":" + ownGroup ;
		ActionTemplate template = DATASOURCE_TEMPLATE_CACHE.get(templateKey) ;
		if(template != null){
			return template ;
		}
		synchronized (DATASOURCE_TEMPLATE_CACHE) {
			template = DATASOURCE_TEMPLATE_CACHE.get(templateKey) ;
			if(template == null){//双重检查
				for(DataSource ds:getDataSourceList(ownGroup)){
					if(securityObjectType.equals(ds.getSecurityObjectType()) && collectType.equals(ds.getCollectType())){
						DATASOURCE_TEMPLATE_CACHE.put(templateKey, (template = ds.getActionTemplate())) ;
						break ;
					}
				}
			}
		}
		return template ;
	}
	
	/**
	 * 返回json类型的日志源树
	 * @return
	 */
	public static JSONArray getJSONTree(String ownGroup,String deviceType,final boolean includeDataSource){
		DataSourceNode root = getTreeWithRoot(ownGroup,deviceType) ;
		DefaultTreeIterator iterator = new DefaultTreeIterator() ;
		iterator.regist(new VisitResultListener<FastJsonResult,DataSourceNode>() {
			@Override
			public void onResult(FastJsonResult result, DataSourceNode tree) {
				if(tree.getLevel()==2){//第一级为虚拟root节点
					result.put("iconCls", AssetUtil.getIconClsByDeviceType(tree.getId())) ;
				}
				if(tree.isLeaf()){
					if(includeDataSource&&ObjectUtils.isNotEmpty(tree.getDataSources())){
						JSONObject attributes = new JSONObject();
						JSONArray dataSources = new JSONArray(tree.getDataSources().size()) ;
						for(DataSource ds:tree.getDataSources()){
							JSONObject dsJson = FastJsonUtil.toJSON(ds, "securityObjectType","collectType","dataObjectType") ;
							dsJson.put("isJob", ds instanceof JobDataSource) ;
							dataSources.add(dsJson) ;
						}
						attributes.put("dataSources", dataSources) ;
						result.put("attributes", attributes) ;
					}
				}else{
					result.put("state", "closed") ;
				}
			}
		}) ;
		FastJsonResult result = (FastJsonResult) iterator.iterate(root, new FastJsonTreeVisitor("pathId=id","name=text","pathName")) ;
		return (JSONArray) result.get("children") ;
	}
	/**
	 * 返回指定资产分类支持的日志源类型,如果deviceType为null返回所有日志源
	 * @param ownerGroup
	 * @param deviceType
	 * @return
	 */
	public static Collection<DataSource> getSupportDataSources(String ownerGroup,String deviceType){
		//获取所有的日志源
		Collection<DataSource> allDataSources = getDataSourceList(ownerGroup) ;
		if(deviceType == null){
			return allDataSources ;
		}
		//根据资产类型查找资产支持的日志源
		List<AssetDataSource> supportDataSources = AssetCategoryUtil.getInstance().getDataSources(deviceType,ownerGroup) ;
		//根据支持的日志源securityObjectType查找对应的日志源
		List<DataSource> dataSources = new ArrayList<DataSource>() ;
		for(AssetDataSource supportDS:supportDataSources){
			for(DataSource ds:allDataSources){
				if(ds.getSecurityObjectType().equals(supportDS.getSecurityObjectType())){
					dataSources.add(ds) ;
				}
			}
		}
		return dataSources ;
	}
	
	public static JSONArray getJSONTree(String ownGroup){
		return getJSONTree(ownGroup,null,false) ;
	}

	private static DataSource createDataSource(SimDatasource simDataSource){
		DataSource source =null;
		if(simDataSource.getTimer() == null || "undefined".equals(simDataSource.getTimer())){
			source = new DataSource();
		}else{
			source = new JobDataSource();
			((JobDataSource)source).setTimer(simDataSource.getTimer());
		}
		source.setId(String.valueOf(simDataSource.getResourceId()));
		source.setName(simDataSource.getResourceName());
		source.setIp(new IpAddress(simDataSource.getDeviceIp()));
		source.setCollectType(simDataSource.getCollectMethod());
		source.setSecurityObjectType(simDataSource.getSecurityObjectType());
		source.setSecurityObjectId(String.valueOf(simDataSource.getSecurityObjectId()));
		source.setCustomerId(String.valueOf(simDataSource.getCustomerId()));
		source.setDataObjectType(simDataSource.getDataObjectType());
		source.setArchiveTime(simDataSource.getArchiveTime());
		SourceControlBean bean = new SourceControlBean();
		bean.setRate(simDataSource.getRate());
		bean.setDuration(simDataSource.getDuration());
		bean.setPeak(simDataSource.getPeak());
		bean.setOverWriteEventTime(StringUtil.booleanVal(simDataSource.getOverwriteEventTime())) ;
		bean.setSaveRawLog(StringUtil.booleanVal(simDataSource.getSaveRawLog())) ;
		source.setSourceControl(bean);
		source.setAvailable(StringUtil.booleanVal(simDataSource.getAvailable())) ;
		source.setReadonly(StringUtil.booleanVal(simDataSource.getReadonly())) ;
		return source ;
	}
	
	public static DataSource toDataSource(SimDatasource simDataSource) {
		return toDataSource(simDataSource, true) ;
	}
	
	/**
	 * 
	 *@标题:将SimDatasource转化为DataSource
	 *@作者:ysf 
	 *@创建时间:Sep 25, 2010 7:36:09 PM
	 *@参数:
	 *@返回值:DataSource
	 */
	public static DataSource toDataSource(SimDatasource simDataSource,boolean convertActionTemplate) {
		DataSource source = createDataSource(simDataSource);
		String actionTemplate = simDataSource.getActionTemplate();
		try {
			DefaultDocumentFormater documentFormater = new DefaultDocumentFormater();
			ActionTemplateElementFormater aActionTemplateElementFormater = new ActionTemplateElementFormater();
			documentFormater.setFormater(aActionTemplateElementFormater);			
			documentFormater.importObjectFromString(actionTemplate);
			ActionTemplate aActionTemplate = ((ActionTemplateElementFormater) documentFormater.getFormater()).getActionTemplate();
			source.setActionTemplate(aActionTemplate);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return source;
	}
	/**
	 * 判断一个日志源是否是系统自审计日志源或者系统日志源
	 * @param dataSourceType
	 * @return
	 */
	public static boolean isSysDataSource(String securityObjectType){
		return SYSTEM_LOG.equals(securityObjectType) || SYSTEM_RUN_LOG.equals(securityObjectType) ;
	}
	
	public static boolean isSysDataSource(DataSource dataSource){
		return isSysDataSource(dataSource.getSecurityObjectType()) ;
	}
	public static JSONArray getJSONTree(List<SimDatasource> userDataSources,final boolean includeDataSource) {
		if(ObjectUtils.isEmpty(userDataSources)){
			return new JSONArray(0) ;
		}
		List<DataSource> dataSources = new ArrayList<DataSource>(userDataSources.size()) ;
		for(SimDatasource sds:userDataSources){
			if(!includeDataSource && isSysDataSource(sds.getSecurityObjectType())){
				continue ;
			}
			dataSources.add(createDataSource(sds)) ;
		}
		DataSourceNode node = buildTree(dataSources) ;
		DefaultTreeIterator iterator = new DefaultTreeIterator() ;
		iterator.regist(new VisitResultListener<FastJsonResult,DataSourceNode>() {
			@Override
			public void onResult(FastJsonResult result, DataSourceNode tree) {
				if(tree.getLevel()==2){//第一级为虚拟root节点,因此从第二级开始
					result.put("iconCls", AssetUtil.getIconClsByDeviceType(tree.getId())) ;
				}
				if(tree.isLeaf() && ObjectUtils.isNotEmpty(tree.getDataSources()) && includeDataSource){
					JSONArray dataSources = new JSONArray(tree.getDataSources().size()) ;
					for(DataSource ds:tree.getDataSources()){
						JSONObject dsJson = FastJsonUtil.toJSON(ds, "id","name=text") ;
						JSONObject attr = new JSONObject() ;
						attr.put("ip",ds.getIp().toString()) ;
						attr.put("securityObjectType", ds.getSecurityObjectType()) ;
						attr.put("isHost", true) ;
						attr.put("level", tree.getLevel()) ;
						dsJson.put("attributes", attr) ;
						dsJson.put("pathName", ds.getName()) ;
						dataSources.add(dsJson) ;
					}
					result.put("children", dataSources) ;
				}else{
					result.put("state", "closed") ;
				}
				JSONObject attr = new JSONObject() ;
				attr.put("level", tree.getLevel() - 1) ;
				result.put("attributes", attr) ;
			}
		}) ;
		FastJsonResult result = (FastJsonResult) iterator.iterate(node, new FastJsonTreeVisitor("pathId=id","name=text","pathName")) ;
		return (JSONArray) result.get("children") ;
	}
}
