package com.topsec.tsm.sim.asset;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.collector.datasource.DataSource;

public class AssetCategoryUtil {
	private static Logger logger = LoggerFactory.getLogger(AssetCategoryUtil.class) ;
	private static AssetCategoryUtil instance ;
	private static String assetCategoryFile = "../../../../conf/AssetCategory.xml" ;
	private List<AssetCategory> categories = new ArrayList<AssetCategory>();
	private AssetCategoryUtil(){
		loadAssetCategory(assetCategoryFile) ;
	}
	
	private void generateAssetTypeFile(String dataSourceDirectory,OutputStream os){
		File file = new File(dataSourceDirectory) ;
		if(file.exists()){
			Document doc = DocumentHelper.createDocument() ;
			Element root = doc.addElement("AssetCategories") ;
			File[] assetTypes = file.listFiles() ;
			for(File assetType:assetTypes){
				Element assetTypeElement = root.addElement("AssetCategory") ;
				assetTypeElement.addAttribute("id", assetType.getName()) ;
				File[] venders = assetType.listFiles() ;
				for(File vender:venders){
					Element venderElement = assetTypeElement.addElement("Vender") ;
					venderElement.addAttribute("id", vender.getName()) ;
					Element dataSourceElement = venderElement.addElement("DataSource") ;
					dataSourceElement.addAttribute("securityObjectType", assetType.getName()+"/"+vender.getName()) ;
				}
			}
			try {
				XMLWriter writer = new XMLWriter(os,OutputFormat.createPrettyPrint()) ;
				writer.write(doc) ;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 创建
	 */
	@Test
	public void createAssetTypeFileTest(){
		generateAssetTypeFile("E:\\TopsecServer\\conf\\agent\\datasourcetemplate", System.out) ;
	}
	/**
	 * 从文件中加载资产分类信息
	 * @param filePath
	 */
	public void loadAssetCategory(String filePath){
		try {
			logger.debug("Load AssetType File From {}",filePath) ;
			Document doc = new SAXReader().read(new File(filePath));
			AssetCategory root = new AssetCategory();
			root.setChildren(parseAssetCategory(doc.getRootElement())) ;
			root.sortChild() ;
		} catch (DocumentException e) {
			logger.error("Read AssetType file error!",e) ;
		}
	}
	/**
	 * 解析资产大类
	 * @param rootElement
	 */
	@SuppressWarnings("unchecked")
	private List<AssetCategory> parseAssetCategory(Element rootElement){
		List<AssetAttribute> baseAttributes = parseAttribute(rootElement) ;//资产公共属性
		List<String> allTools = parseTools(rootElement,Collections.EMPTY_LIST);
		List<Element> catElements = rootElement.elements("AssetCategory") ;
		categories.clear() ;
		for(Element categoryElement:catElements){
			String id = categoryElement.attributeValue("id") ;
			String name = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(id) ;
			int order = StringUtil.toInt(categoryElement.attributeValue("order"),Integer.MAX_VALUE) ;
			AssetCategory category = new AssetCategory(id, name,order) ;
			category.addAll(baseAttributes) ;
			List<AssetAttribute> categoryAttributes = parseAttribute(categoryElement) ;//某种资产类型特有的属性
			List<String> categoryTools = parseTools(categoryElement, allTools) ;
			categories.add(category) ;
			List<Element> categoryChild = categoryElement.elements() ;
			for(Element child:categoryChild){
				String childElementName = child.getName() ;
				if(childElementName.equals("Vender")){
					AssetCategory vender = parseVender(child,category) ;
					List<AssetAttribute> venderAttributes = parseAttribute(child) ;//资产厂商特有的属性
					vender.addAll(baseAttributes) ;
					vender.addAll(categoryAttributes) ;
					vender.addAll(venderAttributes) ;
					List<String> venderTools = parseTools(child, categoryTools) ;
					vender.setTools(venderTools) ;
					category.addChild(vender) ;
				}else if(childElementName.equals("DataSource")){
					category.addDataSource(parseDataSource(child)) ;
				}
			}
		}
		return categories ;
	}
	
	@SuppressWarnings("unchecked")
	private List<AssetAttribute> parseAttribute(Element rootElement){
		List<Element> elements = rootElement.elements("Attribute") ;
		List<AssetAttribute> properties = new ArrayList<AssetAttribute>() ;
		for(Element attributeElement:elements){
			String id = attributeElement.attributeValue("id") ;
			String type = attributeElement.attributeValue("type") ;
			String name = attributeElement.attributeValue("name") ;
			String label = attributeElement.attributeValue("label") ;
			String group = attributeElement.attributeValue("group") ;
			String visibleValue = attributeElement.attributeValue("visible") ;
			String formatter = attributeElement.attributeValue("formatter") ;
			boolean visible =  visibleValue== null ? true : StringUtil.booleanVal(visibleValue) ;
			AssetAttributeType attrType = AssetAttributeType.parse(type) ;
			AssetAttribute attribute ; 
			if(attrType == AssetAttributeType.STATIC){
				attribute = new AssetAttribute(id,attrType, name,label,visible,group,formatter) ;
			}else{
				attribute = new AssetStatus(id, attrType, name,attributeElement.attributeValue("stateKey"), label, visible,group,formatter) ;
			}
			properties.add(attribute) ;
		}
		return properties ;
	}
	/**
	 * 解析资产所支持的工具，此会将来自tools中的工具继承过来，同时删除此分类不支持的工具
	 * @param element
	 * @param tools
	 * @return
	 */
	private List<String> parseTools(Element element,List<String> tools){
		List<String> elementSupportTools = tools;
		String[] support = StringUtil.split(element.attributeValue("support")) ;
		String[] unsupport = StringUtil.split(element.attributeValue("unsupport")) ;
		if(support.length > 0 || unsupport.length > 0){
			elementSupportTools = new ArrayList<String>(tools) ;
			elementSupportTools.addAll(Arrays.asList(support)) ;
			elementSupportTools.removeAll(Arrays.asList(unsupport)) ;
		}
		return elementSupportTools ;
	}
	
	/**
	 * 解析日志源
	 * @param child
	 * @return
	 */
	private AssetDataSource parseDataSource(Element child) {
		String securityObjectType = child.attributeValue("securityObjectType") ;
		String ownerGroup = child.attributeValue("group") ;
		int order = StringUtil.toInt(child.attributeValue("order"),Integer.MAX_VALUE) ;
		if(securityObjectType != null && ownerGroup != null){
			AssetDataSource ds = new AssetDataSource(securityObjectType,ownerGroup,order) ;
			return ds ;
		}
		return null;
	}
	/**
	 * 解析厂商分类
	 * @param child
	 * @param parent
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private AssetCategory parseVender(Element child,AssetCategory parent) {
		String id = child.attributeValue("id") ;
		String name = DeviceTypeShortKeyUtil.getInstance().getShortZhCN(id);
		int order = StringUtil.toInt(child.attributeValue("order"),Integer.MAX_VALUE) ;
		AssetCategory vender = new AssetCategory(id,name,order,parent) ;
		List<Element> dataSourceElements = child.elements("DataSource") ;
		if (dataSourceElements != null) {
			for(Element dataSourceElement:dataSourceElements){
				vender.addDataSource(parseDataSource(dataSourceElement)) ;
			}
		}
		return vender;
	}
	
	public void printAll(){
		for(AssetCategory category:categories){
			System.out.println(category);
			for(AssetCategory child:category.getChildren()){
				System.out.println(child);
			}
		}
	}

	/**
	 * 根据资产分类，获取对应的日志源信息
	 * @param category
	 * @return
	 */
	public List<AssetDataSource> getDataSources(AssetCategory category,String ownerGroup){
		return getDataSources(category.getPathId(),ownerGroup) ;
	}
	/**
	 * 根据资产分类路径id，获取对应的日志源信息
	 * @param pathId
	 * @return 如果没有对应的日志源信息，返回0长度的List结果集
	 */
	public List<AssetDataSource> getDataSources(String pathId,String ownerGroup){
		if(pathId==null){
			return Collections.emptyList() ;
		}
		AssetCategory findResult = getCategoryByPathId(pathId) ;

		if(findResult != null){
			return findResult.getDataSources(ownerGroup) ;
		}
		return Collections.emptyList();
	}
	
	public List<AssetCategory> getCategories(){
		return categories ;
	}
	
	public AssetCategory getCategoryByPathId(String pathId){
		AssetCategory findResult = null ;
		for(AssetCategory category:categories){
			if((findResult=category.findByPathId(pathId))!=null){
				break ;
			}
		}
		return findResult ;
	}
	
	public static AssetCategoryUtil getInstance(){
		if(instance==null){
			synchronized (AssetCategoryUtil.class) {
				instance = new AssetCategoryUtil() ;
			}
		}
		return instance ;
	}
	
}
