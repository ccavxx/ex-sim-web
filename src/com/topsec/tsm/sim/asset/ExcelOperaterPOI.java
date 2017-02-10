package com.topsec.tsm.sim.asset;

/**
 * @ClassName: ExcelOperaterPOI
 * @Declaration: ToDO
 * 
 * @author: WangZhiai create on2014年6月13日上午11:12:21
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddressList;

import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.ass.persistence.AssGroup;
import com.topsec.tsm.ass.service.AssGroupService;
import com.topsec.tsm.ass.service.AssGroupServiceImpl;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.common.exception.DataAccessException;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.resource.persistence.Node;

public class ExcelOperaterPOI {
	//信息系统实体
//	private List<InformationSystem> systemList = new ArrayList<InformationSystem>();
//	private List<String> systemNames = new ArrayList<String>();
	public static final Integer IMPORT_ASSET_NO=500;
	private List<String> assetCategoryNameList = new ArrayList<String>();
	public static void main(String[] args) {
		AssGroupService groupService=new AssGroupServiceImpl();
		new ExcelOperaterPOI(groupService);
	}
	private NodeMgrFacade nodeMgrFacade;
	private HSSFWorkbook workbook = null; 
	private HSSFCellStyle titleStyle = null; 
	private HSSFCellStyle dataStyle = null;
	private HSSFCellStyle titleStyleHidden = null; 
	private HSSFCellStyle dataStyleHidden = null;
	private byte[] excelTobytes=null;
	private List<AssetCategory> categories =AssetCategoryUtil.getInstance().getCategories();
	public static ExcelOperaterPOI newInstance(AssGroupService groupService){
		return new ExcelOperaterPOI(groupService);
	}
	/**
	 * 生成导出下拉框excel
	 * @param outPathStr 输出路径
	 */
	private ExcelOperaterPOI(AssGroupService groupService) {
		try {//
			if (!(null==categories||categories.size()<1)) {
				for (AssetCategory assetCategory : categories) {
					assetCategoryNameList.add(assetCategory.getName().trim() + "_" + assetCategory.getId());
				}
			}
			
			workbook = new HSSFWorkbook();//excel文件对象  
			HSSFSheet sheet1 = workbook.createSheet("资产信息");//工作表对象
			//设置列头样式
			this.setTitleCellStyles(workbook,sheet1);
			//设置数据样式
			this.setDataCellStyles(workbook,sheet1);
			//创建一个隐藏页和隐藏数据集
			this.creatAndSetHideSheet(workbook, "hideselectinfosheet");
			//设置名称数据集
			try {
				this.creatExcelSonNameList(workbook);
			} catch (Exception e) {
				e.printStackTrace() ;
			}
			//创建一行列头数据
			this.creatAppRowHead(sheet1,1);
			//创建一行数据
			for (int i = 2; i < IMPORT_ASSET_NO+2; i++) {
				this.creatAppRow(sheet1, i,i,groupService);
			}

			//生成输入文件
//			FileOutputStream outputStream=new FileOutputStream("assetModel.xls");  
//			workbook.write(outputStream); 
//			excelTobytes=workbook.getBytes();
//			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace() ;
		} 
	}
	
	/**
	 * 列头样式
	 * @param workbook
	 * @param sheet
	 */
	public void setTitleCellStyles(HSSFWorkbook workbook,HSSFSheet sheet){
		titleStyle = workbook.createCellStyle();

		//设置边框
		titleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		//设置背景色
		titleStyle.setFillForegroundColor(HSSFColor.GREEN.index);
		titleStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		//设置居中
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//设置字体
		HSSFFont font = workbook.createFont();
		font.setFontName("微软雅黑");
		font.setFontHeightInPoints((short) 11); //设置字体大小
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
		titleStyle.setFont(font);//选择需要用到的字体格式
		//设置自动换行
		titleStyle.setWrapText(true);
		titleStyleHidden=titleStyle;
		titleStyleHidden.setHidden(true);
		//设置列宽 ,第一个参数代表列id(从0开始),第2个参数代表宽度值
		sheet.setColumnWidth(0, 2500); 
		sheet.setColumnWidth(1, 5000); 
		sheet.setColumnWidth(2, 5000); 
		sheet.setColumnWidth(3, 6200); 
		sheet.setColumnWidth(4, 6200); 
		sheet.setColumnWidth(5, 6200);
		sheet.setColumnWidth(6, 4000); 
		sheet.setColumnWidth(7, 4000); 
		sheet.setColumnWidth(8, 4000); 
		sheet.setColumnWidth(9, 0); 
		sheet.setColumnWidth(10, 0); 
		sheet.setColumnWidth(11, 0); 
		sheet.setColumnWidth(12, 0); 
		sheet.setColumnWidth(13, 4000); 
	}
	
	/**
	 * 数据样式
	 * @param workbook
	 * @param sheet
	 */
	public void setDataCellStyles(HSSFWorkbook workbook,HSSFSheet sheet){
		dataStyle = workbook.createCellStyle();

		//设置边框
		dataStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		dataStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		dataStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		dataStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		//设置背景色
		dataStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
		dataStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		//设置居中
		dataStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		//设置字体
		HSSFFont font = workbook.createFont();
		font.setFontName("微软雅黑");
		font.setFontHeightInPoints((short) 11); //设置字体大小
		dataStyle.setFont(font);//选择需要用到的字体格式
		//设置自动换行
		dataStyle.setWrapText(true);
		dataStyleHidden=dataStyle;
		dataStyleHidden.setHidden(true);
	}

	private void creatAndSetHideSheet(HSSFWorkbook workbook,String hideSheetName){
		HSSFSheet hideselectinfosheet = workbook.createSheet(hideSheetName);//隐藏一些信息
		//1.查询所有的系统名称，作为Excel的名称管理
		if(null==categories||categories.size()<1){
			categories =AssetCategoryUtil.getInstance().getCategories();
		}
		
		if(null==assetCategoryNameList || assetCategoryNameList.size() < 1){
			return;
		}
//		HSSFCellStyle style = workbook.createCellStyle(); 
//		style.setLocked(true);
		//系统，在隐藏页设置选择信息
		HSSFRow provinceRow = hideselectinfosheet.createRow(0);
		creatRow(provinceRow, assetCategoryNameList);
		//子系统，在隐藏页设置选择信息
		List<String> assetCategorySonNames;
		for (int i = 0; i < categories.size(); i++ ) {
			AssetCategory assetCategory=categories.get(i);
			List<AssetCategory>sonAssetCategories=assetCategory.getChildren();
			if (null==sonAssetCategories||sonAssetCategories.size()<1) {
				continue;
			}
			List<String> tempStrings=new ArrayList<String>();
			for (int j = 0; j< sonAssetCategories.size();j++) {
				tempStrings.add(sonAssetCategories.get(j).getName().trim() + "_" + sonAssetCategories.get(j).getId());
			}
			assetCategorySonNames=tempStrings;
			assetCategorySonNames.add(0,assetCategory.getName().trim() + "_" + assetCategory.getId());
			
			HSSFRow zjProvinceRow = hideselectinfosheet.createRow(i+1);
			this.creatRow(zjProvinceRow, assetCategorySonNames);
		}
		
		//设置隐藏页标志
		workbook.setSheetHidden(workbook.getSheetIndex(hideSheetName), true);
	}
	
	/**
	 * 创建隐藏页和数据域
	 * @param workbook
	 * @param hideSheetName
	 */
//	public void creatHideSheet(HSSFWorkbook workbook,String hideSheetName){
//		HSSFSheet hideselectinfosheet = workbook.createSheet(hideSheetName);//隐藏一些信息
//		//1.查询所有的系统名称，作为Excel的名称管理
//		getAllEnableInfoSystemList();
//		if(systemList != null && systemList.size() > 0){
//			for (InformationSystem sys : systemList) {
//				this.systemNames.add(sys.getName().trim() + "_id" + sys.getId());
//			}
//		}
//
//		if(this.systemNames != null && this.systemNames.size() > 0){
//			//系统，在隐藏页设置选择信息
//			HSSFRow provinceRow = hideselectinfosheet.createRow(0);
//			this.creatRow(provinceRow, this.systemNames);
//			//子系统，在隐藏页设置选择信息
//			List<String> systemSonNames;
//			for (int i = 0; i < this.systemNames.size(); i++) {
//				systemSonNames = this.findSon(this.systemNames.get(i).split("_id")[1]);
//				systemSonNames.add(0, this.systemNames.get(i));
//
//				HSSFRow zjProvinceRow = hideselectinfosheet.createRow(i+1);
//				this.creatRow(zjProvinceRow, systemSonNames);
//			}
//		}
//
//		//设置隐藏页标志
//		workbook.setSheetHidden(workbook.getSheetIndex(hideSheetName), false);
//	}

	/**获取所有信息系统数据**/
//	private void getAllEnableInfoSystemList() {
//		for (int i = 1; i < 11; i++) {
//			InformationSystem infoSys = new InformationSystem();
//			infoSys.setId(i);
//			infoSys.setName("系统");
//			systemList.add(infoSys);
//		}
//	}
	
	/**
	 * 创建一列数据
	 * @param currentRow
	 * @param textList
	 */
	public void creatRow(HSSFRow currentRow,List<String> textList){
		if(textList!=null&&textList.size()>0){
			int i = 0;
			for(String cellValue : textList){
				HSSFCell userNameLableCell = currentRow.createCell(i++);
				userNameLableCell.setCellValue(cellValue);
			}
		}
	}
	
	/**
	 * 找到系统下的子系统
	 * @param fatherId
	 * @return
	 */
//	private List<String> findSon(String fatherId){
//		List<String> sonNames = new ArrayList<String>();
//		for (int i = 1; i < 3; i++) {
//			sonNames.add("子系统_" + fatherId + i);
//		}
//		return sonNames;
//	}

	
	private void creatExcelSonNameList(HSSFWorkbook workbook){
		if(categories != null && categories.size() > 0){
			//名称管理
			Name name;
			name = workbook.createName();
			name.setNameName("sysytemSonInfo");
			name.setRefersToFormula("hideselectinfosheet!$A$1:$"+this.getcellColumnFlag(categories.size())+"$1");

			if(this.assetCategoryNameList != null && assetCategoryNameList.size() > 0){
				//子系统，在隐藏页设置选择信息
				List<String> assetCategorySonNames;
				for (int i = 0; i < categories.size(); i++) {
					AssetCategory assetCategory=categories.get(i);
					List<AssetCategory>sonAssetCategories=assetCategory.getChildren();
					if (null==sonAssetCategories||sonAssetCategories.size()<1) {
						continue;
					}
					List<String> tempStrings=new ArrayList<String>();
					for (int j = 0; j< sonAssetCategories.size();j++) {
						tempStrings.add(sonAssetCategories.get(j).getName().trim() + "_" + sonAssetCategories.get(j).getId());
					}
					assetCategorySonNames=tempStrings;
					assetCategorySonNames.add( 0,assetCategory.getName().trim() + "_" + assetCategory.getId());

					name = workbook.createName();
					//父级系统
					name.setNameName(assetCategoryNameList.get(i));
					//子系统的范围
					name.setRefersToFormula("hideselectinfosheet!$B$"+(i+2)+":$"+this.getcellColumnFlag(assetCategorySonNames.size())+"$"+(i+2));
				}
			}
		}
	}
	
	/**
	 * 名称管理
	 * @param workbook
	 * @throws FrameworkException 
	 */
//	public void creatExcelNameList(HSSFWorkbook workbook){
//		if(systemList != null && systemList.size() > 0){
//			//名称管理
//			Name name;
//			name = workbook.createName();
//			name.setNameName("sysytemInfo");
//			name.setRefersToFormula("hideselectinfosheet!$A$1:$"+this.getcellColumnFlag(systemList.size())+"$1");
//
//			if(this.systemNames != null && this.systemNames.size() > 0){
//				//子系统，在隐藏页设置选择信息
//				List<String> systemSonNames;
//				for (int i = 0; i < this.systemNames.size(); i++) {
//					systemSonNames = this.findSon(this.systemNames.get(i).split("id")[1]);
//					systemSonNames.add(0, this.systemNames.get(i));
//
//					name = workbook.createName();
//					//父级系统
//					name.setNameName(this.systemNames.get(i));
//					//子系统的范围
//					name.setRefersToFormula("hideselectinfosheet!$B$"+(i+2)+":$"+this.getcellColumnFlag(systemSonNames.size())+"$"+(i+2));
//				}
//			}
//		}
//	}

	//根据数据值确定单元格位置（比如：28-AB）
	private String getcellColumnFlag(int num) {
		String columFiled = "";
		int chuNum = 0;
		int yuNum = 0;
		if(num >= 1 && num <= 26){
			columFiled = this.doHandle(num);
		}else{
			chuNum = num / 26;
			yuNum = num % 26;

			columFiled +=  this.doHandle(chuNum);
			columFiled +=  this.doHandle(yuNum);
		}
		return columFiled;
	}

	private String doHandle(final int num) {
		String[] charArr = {"A","B","C","D","E","F","G","H","I","J","K","L","M"
				           ,"N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		return charArr[num-1].toString();
	}
	
	/**
	 * 创建一列应用列头
	 * @param userinfosheet1
	 * @param userName
	 */
	public void creatAppRowHead(HSSFSheet userinfosheet1,int naturalRowIndex){
		HSSFRow row = userinfosheet1.createRow(naturalRowIndex-1);

		//0.序号
		HSSFCell serialNumberCell = row.createCell(0);
		serialNumberCell.setCellValue("序号(*必填)");
		serialNumberCell.setCellStyle(titleStyle);

		//1.ip
		HSSFCell assetIpCell = row.createCell(1);
		assetIpCell.setCellValue("IP(*必填)");
		assetIpCell.setCellStyle(titleStyle);

		//2.资产名称
		HSSFCell assetNameCell = row.createCell(2);
		assetNameCell.setCellValue("资产名称(*必填)");
		assetNameCell.setCellStyle(titleStyle);

		//3.资产类型一级
		HSSFCell assetTypeStairCell = row.createCell(3);
		assetTypeStairCell.setCellValue("资产类型父级(*必填)");
		assetTypeStairCell.setCellStyle(titleStyle);
		
		//4.资产类型二级
		HSSFCell assetTypeSecondCell = row.createCell(4);
		assetTypeSecondCell.setCellValue("资产类型子级(*必填)");
		assetTypeSecondCell.setCellStyle(titleStyle);
		
		//5.主机名称
		HSSFCell scanIpCell = row.createCell(5);
		scanIpCell.setCellValue("管理节点(*必填)");
		scanIpCell.setCellStyle(titleStyle);
		
		//6.主机名称
		HSSFCell assetHostCell = row.createCell(6);
		assetHostCell.setCellValue("主机名称");
		assetHostCell.setCellStyle(titleStyle);
				
		//7.操作系统
		HSSFCell assetOSCell = row.createCell(7);
		assetOSCell.setCellValue("操作系统");
		assetOSCell.setCellStyle(titleStyle);
				
		//8.安全等级
		HSSFCell assetSafeRankCell = row.createCell(8);
		assetSafeRankCell.setCellValue("安全等级");
		assetSafeRankCell.setCellStyle(titleStyle);
		
		//9.业务组
		HSSFCell assetBusinessGroupCell = row.createCell(9);
		assetBusinessGroupCell.setCellValue("业务组");
		assetBusinessGroupCell.setCellStyle(titleStyleHidden);
		
		//10.物理地址
		HSSFCell assetMACCell = row.createCell(10);
		assetMACCell.setCellValue("MAC物理地址");
		assetMACCell.setCellStyle(titleStyleHidden);
		
		//11.厂商
		HSSFCell assetManufacturerCell = row.createCell(11);
		assetManufacturerCell.setCellValue("厂商");
		assetManufacturerCell.setCellStyle(titleStyleHidden);
		
		//12.地理位置
		HSSFCell assetLocationCell = row.createCell(12);
		assetLocationCell.setCellValue("地理位置");
		assetLocationCell.setCellStyle(titleStyleHidden);
		
		//13.联系人
		HSSFCell assetLinkmanCell = row.createCell(13);
		assetLinkmanCell.setCellValue("联系人");
		assetLinkmanCell.setCellStyle(titleStyle);
		
		//14.描述
		HSSFCell assetDescribeCell = row.createCell(14);
		assetDescribeCell.setCellValue("描述");
		assetDescribeCell.setCellStyle(titleStyle);
		
		//15.类型名称
//		HSSFCell assetTypeNameCell = row.createCell(15);
//		assetTypeNameCell.setCellValue("类型名称");
//		assetTypeNameCell.setCellStyle(titleStyle);
		
	}
	
	/**
	 * 创建一列应用数据
	 * @param userinfosheet1
	 * @param userName
	 */
	public void creatAppRow(HSSFSheet userinfosheet1,int num,int naturalRowIndex,AssGroupService groupService){
		
		HSSFRow row = userinfosheet1.createRow(naturalRowIndex-1);

		//0.序号
		HSSFCell serialNumberCell = row.createCell(0);
		serialNumberCell.setCellValue(num-1+"");
		serialNumberCell.setCellStyle(dataStyle);
		
		//1.ip
		HSSFCell assetIpCell = row.createCell(1);
		//assetIpCell.setCellValue(titels);
		assetIpCell.setCellStyle(dataStyle);

		//2.资产名称
		HSSFCell assetNameCell = row.createCell(2);
		//dataStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));
		//assetNameCell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString());
		assetNameCell.setCellStyle(dataStyle);

		//3.资产类型一级
		HSSFCell assetTypeStairCell = row.createCell(3);
		assetTypeStairCell.setCellValue("请选择");
		assetTypeStairCell.setCellStyle(dataStyle);

		// 4.资产类型二级
		HSSFCell assetTypeSecondCell = row.createCell(4);
		assetTypeSecondCell.setCellValue("请选择");
		assetTypeSecondCell.setCellStyle(dataStyle);
		
		// 5.资产类型二级
		HSSFCell scanIpCell = row.createCell(5);
		scanIpCell.setCellValue("请选择");
		scanIpCell.setCellStyle(dataStyle);
		
		// 6.主机名称
		HSSFCell assetHostCell = row.createCell(6);
		assetHostCell.setCellStyle(dataStyle);

		// 7.操作系统
		HSSFCell assetOSCell = row.createCell(7);
		assetOSCell.setCellValue("请选择");
		assetOSCell.setCellStyle(dataStyle);

		// 8.安全等级
		HSSFCell assetSafeRankCell = row.createCell(8);
		assetSafeRankCell.setCellValue("请选择");
		assetSafeRankCell.setCellStyle(dataStyle);

		//9.业务组
		HSSFCell assetBusinessGroupCell = row.createCell(9);
		assetBusinessGroupCell.setCellValue("请选择");
		assetBusinessGroupCell.setCellStyle(dataStyle);

		//10.物理地址
		HSSFCell assetMACCell = row.createCell(10);
		assetMACCell.setCellStyle(dataStyleHidden);
		
		// 11.厂商
		HSSFCell assetManufacturerCell = row.createCell(11);
		assetManufacturerCell.setCellStyle(dataStyleHidden);

		// 12.地理位置
		HSSFCell assetLocationCell = row.createCell(12);
		assetLocationCell.setCellStyle(dataStyleHidden);

		// 13.联系人
		HSSFCell assetLinkmanCell = row.createCell(13);
		assetLinkmanCell.setCellStyle(dataStyle);

		// 14.描述
		HSSFCell assetDescribeCell = row.createCell(14);
		assetDescribeCell.setCellStyle(dataStyle);
		
		// 15.类型名称
//		HSSFCell assetTypeNameCell = row.createCell(15);
//		assetTypeNameCell.setCellStyle(dataStyle);

		String[]osNameArr=listToArr(AssetUtil.getOsList());
		String[]safeRankArr=listToArr(AssetUtil.getSafeRank());
		try {
			if(null!=groupService){
				List<AssGroup> groups = groupService.getAll() ;
				List<String> groupIdList=new ArrayList<String>();
				if (null!=groups&&groups.size()>0) {
					for (AssGroup assGroup : groups) {
						groupIdList.add(assGroup.getGroupName());
					}
					String[]businessGroupArr=listToArr(groupIdList);
					DataValidation businessDataValidation=setDataValidationByFormula( businessGroupArr, naturalRowIndex, 10);
					userinfosheet1.addValidationData(businessDataValidation);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		try {
			nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
			if (null!=nodeMgrFacade) {
				List<String> types = new ArrayList<String>(2);
				types.add(NodeDefinition.NODE_TYPE_AUDIT);
				types.add(NodeDefinition.NODE_TYPE_AGENT);
				List<Node> nodes = nodeMgrFacade.getNodesByTypes(types, false,
						false, false, false);
				List<String>scanIpList=new ArrayList<String>();
				if (null!=nodes&&nodes.size()>0) {
					for (Node node : nodes) {
						scanIpList.add(node.getIp());
					}
				}
				String[]scanIpArr=listToArr(scanIpList);
				DataValidation businessDataValidation=setDataValidationByFormula( scanIpArr, naturalRowIndex, 6);
				userinfosheet1.addValidationData(businessDataValidation);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		// 得到验证对象
		DataValidation data_validation_list = this.getDataValidationByFormula("sysytemSonInfo",naturalRowIndex,4); //从1开始下拉框处于第几列
		//工作表添加验证数据  
		String tempString="INDIRECT($D"+1+")";
		userinfosheet1.addValidationData(data_validation_list);
		DataValidation data_validation_list2 = this.getDataValidationByFormula(tempString,naturalRowIndex,5);
		//工作表添加验证数据  
		userinfosheet1.addValidationData(data_validation_list2);
		
		DataValidation osNameDataValidation=setDataValidationByFormula( osNameArr, naturalRowIndex, 8);
		userinfosheet1.addValidationData(osNameDataValidation);
		
		DataValidation safeRankDataValidation=setDataValidationByFormula( safeRankArr, naturalRowIndex, 9);
		userinfosheet1.addValidationData(safeRankDataValidation);
	}
	
	/**
	 * 使用已定义的数据源方式设置一个数据验证
	 * @param formulaString
	 * @param naturalRowIndex
	 * @param naturalColumnIndex
	 * @return
	 */
	public DataValidation getDataValidationByFormula(String formulaString,int naturalRowIndex,int naturalColumnIndex){
		//加载下拉列表内容  
		DVConstraint constraint = DVConstraint.createFormulaListConstraint(formulaString); 
		//设置数据有效性加载在哪个单元格上。  
		//四个参数分别是：起始行、终止行、起始列、终止列  
		int firstRow = naturalRowIndex-1;
		int lastRow = naturalRowIndex-1;
		int firstCol = naturalColumnIndex-1;
		int lastCol = naturalColumnIndex-1;
		CellRangeAddressList regions=new CellRangeAddressList(firstRow,lastRow,firstCol,lastCol);  
		//数据有效性对象 
		DataValidation data_validation_list = new HSSFDataValidation(regions,constraint);
		return data_validation_list;  
	}
	
	/**
	 * 
	 * @param formulaArr
	 * @param naturalRowIndex
	 * @param naturalColumnIndex
	 * @return
	 */
	public DataValidation setDataValidationByFormula(String[] formulaArr,int naturalRowIndex,int naturalColumnIndex){
		//加载下拉列表内容  
		DVConstraint constraint = DVConstraint.createExplicitListConstraint(formulaArr); 
		//设置数据有效性加载在哪个单元格上。  
		//四个参数分别是：起始行、终止行、起始列、终止列  
		int firstRow = naturalRowIndex-1;
		int lastRow = naturalRowIndex-1;
		int firstCol = naturalColumnIndex-1;
		int lastCol = naturalColumnIndex-1;
		CellRangeAddressList regions=new CellRangeAddressList(firstRow,lastRow,firstCol,lastCol);  
		//数据有效性对象 
		DataValidation data_validation_list = new HSSFDataValidation(regions,constraint);
		return data_validation_list;  
	}

	private String[] listToArr(List<String>arrList){
		if (null==arrList||arrList.size()<1) {
			return null;
		}
		int len=arrList.size();
		String[] resultArr=new String[len];
		for (int i = 0; i < len; i++) {
			resultArr[i]=arrList.get(i);
		}
		return resultArr;
	}
	

	public HSSFWorkbook getWorkbook() {
		return workbook;
	}
	public byte[] getExcelTobytes() {
		return excelTobytes;
	}

}

