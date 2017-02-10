//
// COPYRIGHT (C) 2009 TOPSEC CORPORATION
//
// ALL RIGHTS RESERVED BY TOPSEC CORPORATION, THIS PROGRAM
// MUST BE USED SOLELY FOR THE PURPOSE FOR WHICH IT WAS
// FURNISHED BY TOPSEC CORPORATION, NO PART OF THIS PROGRAM
// MAY BE REPRODUCED OR DISCLOSED TO OTHERS, IN ANY FORM
// WITHOUT THE PRIOR WRITTEN PERMISSION OF TOPSEC CORPORATION.
// USE OF COPYRIGHT NOTICE DOES NOT EVIDENCE PUBLICATION
// OF THE PROGRAM
//
//            TOPSEC CONFIDENTIAL AND PROPROETARY
//
////////////////////////////////////////////////////////////////////////////
package com.topsec.tsm.sim.report.bean.struct;

/*
 *	TopSec-Ta-l 2009
 *	系统名：Ta-L Report
 *	类一览
 *		NO	类名		概要
 *		1	ExpMstRpt	Pdf、Xls、Doc文件导出类
 *	历史:
 *		NO	日期		版本		修改人		内容				
 *		1	2009/04/30	V1.0.1		Rick		初版
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import java_cup.runtime.int_token;

import javax.servlet.http.HttpServletRequest;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.PropertyManager;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.report.chart.highchart.HighChartExportTool;
import com.topsec.tsm.sim.report.chart.highchart.HighChartTask;
import com.topsec.tsm.sim.report.jasper.JRDesignHelper;
import com.topsec.tsm.sim.report.util.HtmlAndFileUtil;
import com.topsec.tsm.sim.report.util.JasperClasspath;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.report.util.ReportUiUtil;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;

public class ExpMstRpt {// 上下71*2.54 厘米 左右89*3.17 厘米
	private static Logger log = LoggerFactory.getLogger(ExpMstRpt.class);
	// 正常 三级菜单
	private final static JRDesignStyle NORMAL = new JRDesignStyle();
	// 二级菜单
	private final static JRDesignStyle NORMALTITLE = new JRDesignStyle();
	// 一级菜单
	private final static JRDesignStyle TITLE = new JRDesignStyle();
	private final static JRDesignStyle BOLBTITLE = new JRDesignStyle();
	private final static JRDesignStyle TABLETITLE = new JRDesignStyle();
	private final static JRDesignStyle BOLB = new JRDesignStyle();

	private final static String mstChartXmlPath = "rptChartTemplet.jrxml";
	private final static String mstTableXmlPath = "rptTableTemplet.jrxml";
	private final static String mstCoverXmlPath = "rptCoverTemplet.jrxml";
	private final static String mstCoverExcelXmlPath = "rptCoverExcelTemplet.jrxml";
	private final static String mstSynXmlPath = "rptSynTemplet.jrxml";

	public static final String REPORT_FOOTER_IMAGE_PATH = "report.footer.image.path";
	public static final String DEFAULT_REPORT_IMAGE = "/img/skin/top/logo.png";

	private static String FontName = "微软雅黑";
	private static final String WinFont = "/resource/report/msyh.ttf";
	private static final String PdfFontNameSong = ReportUiUtil.getSysPath()
			+ WinFont;
	private static final String PdfEncodUCS2 = "Identity-H";
	private static String fliePath = "";

	public static String getFliePath() {
		return fliePath;
	}

	public static void setFliePath(String fliePath) {
		ExpMstRpt.fliePath = fliePath;
	}

	public Map<ExpDateStruct, String> reportImages;
	static {
		try {
			String[] fontNames = GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getAvailableFontFamilyNames();
			if (!ArrayUtils.contains(fontNames, "微软雅黑")) {
				Font font = Font.createFont(Font.PLAIN, new File(
						PdfFontNameSong));
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
						font);
			}
		} catch (Exception e) {
			log.error("报表导出初始化失败!", e);
		}
		JasperClasspath.setJasperClasspath();
	}

	/**
	 * 报表用Style
	 * 
	 * @param JasperDesign
	 *            mstDesign 报表模版
	 * @return void
	 * @throws JRException
	 */
	private void setFont(JasperDesign mstDesign) throws JRException {
		NORMAL.setName("NORMAL");
		NORMAL.setFontName(FontName);
		NORMAL.setFontSize(10);
		NORMAL.setPdfFontName(FontName);
		NORMAL.setPdfEncoding(PdfEncodUCS2);
		NORMAL.setPdfEmbedded(true);

		BOLB.setName("BOLB");
		BOLB.setFontName(FontName);
		BOLB.setFontSize(10);
		BOLB.setPdfFontName(PdfFontNameSong);
		BOLB.setPdfEncoding(PdfEncodUCS2);
		BOLB.setPdfEmbedded(true);
		BOLB.setVerticalAlignment(new Byte("3"));// 垂直居中

		NORMALTITLE.setName("NORMALTITLE");
		NORMALTITLE.setFontName(FontName);
		NORMALTITLE.setFontSize(12);
		NORMALTITLE.setPdfFontName(FontName);
		NORMALTITLE.setPdfEncoding(PdfEncodUCS2);
		NORMALTITLE.setPdfEmbedded(true);

		TITLE.setName("TITLE");
		TITLE.setFontName(FontName);
		TITLE.setFontSize(14);
		TITLE.setPdfFontName(FontName);
		TITLE.setPdfEncoding(PdfEncodUCS2);
		TITLE.setPdfEmbedded(true);
		//
		BOLBTITLE.setName("BOLBTITLE");
		BOLBTITLE.setFontName(FontName);
		BOLBTITLE.setFontSize(10);
		BOLBTITLE.setPdfFontName(FontName);
		BOLBTITLE.setPdfEncoding(PdfEncodUCS2);
		BOLBTITLE.setPdfEmbedded(true);
		BOLBTITLE.setMode(JRElement.MODE_TRANSPARENT); // 透明/不透明
		BOLBTITLE.setVerticalAlignment(new Byte("2"));// 垂直居中
		BOLBTITLE.setHorizontalAlignment(new Byte("2")); // 水平居中
		BOLBTITLE.setForecolor(Color.BLACK); // 显示内容字的颜色
		BOLBTITLE.setBackcolor(Color.BLUE); // 三边
		BOLBTITLE.setFill(new Byte("3"));
		BOLBTITLE.setBorder(new Byte("1"));// 边框
		BOLBTITLE.setBorderColor(new Color(102, 153, 255));// 边框颜色// #6699FF

		TABLETITLE.setName("TABLETITLE");
		TABLETITLE.setFontName(FontName);
		TABLETITLE.setFontSize(10);
		TABLETITLE.setPdfFontName(FontName);
		TABLETITLE.setPdfEncoding(PdfEncodUCS2);
		TABLETITLE.setPdfEmbedded(true);
		TABLETITLE.setMode(JRElement.MODE_OPAQUE);
		TABLETITLE.setVerticalAlignment(new Byte("2"));
		TABLETITLE.setHorizontalAlignment(new Byte("2"));
		TABLETITLE.setForecolor(Color.BLACK);
		TABLETITLE.setBackcolor(Color.decode("#D1ECF8"));
		TABLETITLE.setFill(new Byte("3"));
		TABLETITLE.setBorder(new Byte("1"));
		TABLETITLE.setBorderColor(new Color(102, 153, 255));

		mstDesign.addStyle(NORMALTITLE);
		mstDesign.addStyle(TITLE);
		mstDesign.addStyle(BOLBTITLE);
		mstDesign.addStyle(NORMAL);
		mstDesign.addStyle(BOLB);
		mstDesign.addStyle(TABLETITLE);

	}

	/**
	 * 构建报表目录
	 * 
	 * @param LinkedHashMap
	 *            <String, List> expMap 目录内容
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param ExpStruct
	 *            exps 报表导出用结构体
	 * @return List<JasperPrint> JasperPrint list
	 * @throws Exception
	 */
	public List<JasperPrint> creMstRpt(LinkedHashMap<String, List> expMap,
			HttpServletRequest request, ExpStruct exps) {
		if (exps.getFileType().equalsIgnoreCase("html")) {
			File file=new File(ExpMstRpt.fliePath);
			if (!file.exists()) {
				HtmlAndFileUtil.createPath(ExpMstRpt.fliePath+"image/");
			}
		}
		String spac = " ";
		int V_DOWN_PAGE = 0;
		List<JasperPrint> printAl = new ArrayList();
		Iterator expIr = expMap.keySet().iterator();
		Object strKey = null;
		List<ExpDateStruct> expList = null;
		int iR = 0;
		LinkedHashMap<String, List<SynStruct>> snyMap = new LinkedHashMap<String, List<SynStruct>>();
		JasperPrint coverJasperPrint=null;
		try {
			coverJasperPrint = getCoverRpt(exps);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		while (expIr.hasNext()) {
			strKey = expIr.next();
			expList = expMap.get(strKey);
			JasperDesign mstDesign = null;
			List<SynStruct> synList = new ArrayList();
			int i = 0;
			long st = System.currentTimeMillis();
			reportImages = generateImage(expList);
			long et = System.currentTimeMillis();
			System.out.print("picture: " + (et - st));
			for (ExpDateStruct exp : expList) {
				SynStruct synS = new SynStruct();
				Boolean isSub = !exp.getSubType().endsWith("*");
				synS.setTalCategory(exp.getTalCategory());
				// 子表名称
				synS.setSubName(exp.getTitle());
				// 设备类型
				synS.setDvcType(exp.getSubType());
				String mstXmlPath = null;
				if (exp.getSubChart() != null) {
					mstXmlPath = ReportUiUtil.getSysPath(mstChartXmlPath);
				} else {
					mstXmlPath = ReportUiUtil.getSysPath(mstTableXmlPath);
				}
				// 加载主报表
				try {
					mstDesign = JRXmlLoader.load(mstXmlPath);
					setFont(mstDesign);
				} catch (Exception e) {
					log.error("getCreXml==" + e);
				}
				// $P
				Map map = new HashMap();

				// 主报表设置title 数字
				int iOSj = (iR + 2);
				// x.x 数字
				int num = 0;
				int rptWidth = mstDesign.getColumnWidth() - 59;// column 的宽

				int textH = 22;// text 高度
				int top = 71;
				int left = 53;
				int coef = 0;// 系数
				int tableImage = 225;
				int imgImage = 245;
				JRDesignBand titleBand = (JRDesignBand) mstDesign.getTitle();
				int headHigth = mstDesign.getTitle().getHeight();// column
				boolean picw = false;
				int leftTop = 0;
				// 2// Height// left,top,h,w
				if (i == 0) {
					String subType = ReportUiUtil.getProperty(exp.getSubType());
					if (subType.equals(exp.getSubType())) {
						subType=subType.replace("Comprehensive", "");
						subType=DeviceTypeNameUtil.getDeviceTypeName(subType);
					}
					String item1 = iOSj + " " + subType;
					if (isSub) {
						JRDesignHelper.setStaticTextControl(titleBand,
								new PositionStruct(left, top, textH, rptWidth),
								item1, TITLE);
					}
					leftTop = (iOSj + spac).length();
					coef++;
				} else {
					((JRDesignBand) mstDesign.getTitle()).setHeight(headHigth
							- textH);
					picw = true;
				}
				headHigth = mstDesign.getTitle().getHeight();// column

				int iTSj = (i + 1);// 主报表设置title
				// 2.1
				String itemS2 = iOSj + "." + iTSj + spac;
				String itemV2 = exp.getTitle();
				String item2 = itemS2 + itemV2;
				// left,top,h,w
				left += leftTop;
				if (isSub) {
					JRDesignHelper.setStaticTextControl(titleBand,
							new PositionStruct(left, textH * coef + top, textH,
									rptWidth), item2, NORMALTITLE);
				}
				coef++;
				String itemS3 = iOSj + "." + iTSj + "." + 1 + spac;
				String itemV3 = ReportUiConfig.ExpDvc + exp.getDvcIp();

				synS.setDvcIp(exp.getDvcIp()); // 子表页数

				// 2.2.1
				String item3 = itemS3 + itemV3;
				left += itemS3.length();
				// left,top,h,w
				if (isSub) {
					JRDesignHelper.setStaticTextControl(titleBand,
							new PositionStruct(left, (coef * textH + top),
									textH, rptWidth), item3, NORMAL);
				}
				coef++;
				// 2.2.2
				String itemS4 = iOSj + "." + iTSj + "." + 2 + spac;

				String itemV4 = "";
				if (exp.getSTime() == null || exp.getETime() == null) {
					itemV4 = ReportUiConfig.ExpSyn2.replace("$title",
							exp.getTitle());
				} else {
					itemV4 = ReportUiConfig.ExpSyn
							.replace("$title", exp.getTitle())
							.replace("$stime", exp.getSTime())
							.replace("$etime", exp.getETime());
				}

				itemV4 = ReportUiUtil.getExpItemValue(exps, itemV4);
				// 2.2.3
				String item4 = itemS4 + itemV4;
				// left,top,h,w
				String numT = "numT";

				int ilength=item4.length();
				int counti=1;
				if (ilength>60) {
					counti=(int) (ilength/60);
				}
				if (isSub) {
					JRDesignHelper.addPParam(mstDesign, numT, String.class);

					JRDesignHelper.setTextControl(titleBand,
							new PositionStruct(left, coef * textH + top, textH*(counti+1),
									rptWidth), numT, String.class,
							ReportUiConfig.param_P, NORMAL, true);
				}
				coef += 3;//2.1.2
				map.put(numT, item4);
				num = 2;
				if (exp.getSubChart() != null) {
					num++;
					// 图片title
					String itemS5 = iOSj + "." + iTSj + "." + num + spac;
					String itemV5 = exp.getTitle() + ReportUiConfig.ExpPic;
					String item5 = itemS5 + itemV5;
					if (!isSub) {
						item5 = itemV5;
						coef = 0;
					}
					// left,top,h,w
					JRDesignHelper.setStaticTextControl(titleBand,
							new PositionStruct(left, coef * textH + top, textH,
									rptWidth), item5, NORMAL);
					coef++;
					// 图片 left,top,h,w

					int picH = 0;
					if (picw) {
						// picH = headHigth - 145;
						picH = imgImage;
					} else {
						// picH = headHigth - 165;
						picH = tableImage;
					}

					JRDesignHelper.setImgControl(titleBand, new PositionStruct(
							left, coef * textH + top, picH, rptWidth), "chart",
							String.class, ReportUiConfig.param_P, null, null);
					coef++;
					top += picH;
				}
				// map.put("chart", new JCommonDrawableRenderer((JFreeChart)
				// exp.getSubChart()));
				map.put("chart", reportImages.get(exp));
				map.put("V_DOWN_PAGE", V_DOWN_PAGE);
				map.put("footlogo", getHead());
				map.put("headlogo", getHead());
				map.put("cretime",
						ReportUiUtil.getNowTime(ReportUiConfig.dFormat1));
				map.put("title", exp.getTitle());
				// table TITLE 分组
				String[] subFiledPParam = exp.getTitleLable().split(",");
				int tableWidth = rptWidth / subFiledPParam.length;
				num++;
				String itemS6 = iOSj + "." + iTSj + "." + num + spac;
				String itemV6 = exp.getTitle() + ReportUiConfig.ExpTable;
				String item6 = itemS6 + itemV6;
				if (!isSub) {
					item6 = itemV6;
				}
				// left,top,h,w table title 题目统计表 题目

				if (exps.getFileType().equals("doc")) {
					JRDesignHelper.setStaticTextControl(titleBand,
							new PositionStruct(left, coef * textH + top, textH,
									rptWidth), item6, BOLB);
				} else {
					if (exp.getTable() != null && exp.getTable().size() > 0) {// 表格标题
						JRDesignHelper.setStaticTextControl(titleBand,
								new PositionStruct(left, coef * textH + top,
										textH, rptWidth), item6, BOLB);
					}
				}

				coef++;
				((JRDesignBand) mstDesign.getTitle())
						.setHeight((coef * textH + top));
				// 数据迭带
				String[] subFiledFParam = exp.getSubTableFile().split(",");
				int urlLength=330;
				int tempY=0;
				boolean issetTableWidth=false;
				int timeLength=125;
				boolean issetTimeWidth=false;
				for (int j = 0; j < subFiledPParam.length
						&& exp.getTable() != null; j++) {// 表格
					// $p start// 创建p
					String paramPname = "tablep" + i + j;
					JRDesignHelper.addPParam(mstDesign, paramPname,
							String.class);
					if ("REQUEST_OBJECT".equalsIgnoreCase(subFiledFParam[j])
							|| "url".equalsIgnoreCase(subFiledFParam[j])
							||"REQUEST_URL".equalsIgnoreCase(subFiledPParam[j])) {
						tableWidth=urlLength;
						issetTableWidth=true;
					}
					if ("start_time".equalsIgnoreCase(subFiledFParam[j]) && subFiledFParam.length>3) {
						tableWidth=timeLength;
						issetTimeWidth=true;
					}
					// left top hight width 迭带title
					JRDesignHelper.setTextControl((JRDesignBand) mstDesign
							.getColumnHeader(), new PositionStruct(left
							+ (tempY), 0, textH, tableWidth),
							paramPname, String.class, ReportUiConfig.param_P,
							TABLETITLE, false);
					map.put(paramPname, subFiledPParam[j]);
					// $p end
					// $f start //stretch type relative to band height
					subFiledFParam[j] = subFiledFParam[j] == null
							|| subFiledFParam[j].trim().length() <= 0 ? ""
							: subFiledFParam[j];
					JRDesignHelper.addFParam(mstDesign, subFiledFParam[j],
							String.class);

					JRDesignHelper.setTextControl((JRDesignBand) mstDesign
							.getDetail(), new PositionStruct(left
							+ (tempY), 0, textH, tableWidth),
							subFiledFParam[j], String.class,
							ReportUiConfig.param_F, BOLBTITLE, false);
					// $f end
					tempY+=tableWidth;
					if (issetTableWidth) {
						tableWidth = (rptWidth-urlLength) / (subFiledPParam.length-1);
						issetTableWidth=false;
					}
					if (issetTimeWidth) {
						tableWidth = (rptWidth-timeLength) / (subFiledPParam.length-1);
						issetTimeWidth=false;
					}
				}

				// 填充f数据
				JRBeanCollectionDataSource jc = new JRBeanCollectionDataSource(
						ReportUiUtil.editMap(exp.getTable()));
				// add background start
				setBackground(mstDesign, map);
				// add background end
				// 构造主报表
				// 测试采样用 生产环境删除 start
				// getCreXml(mstDesign, "内容");// del
				// 测试采样用 生产环境删除 end
				JasperReport mstReport = null;
				JasperPrint mstJasperPrint = null;
				try {
					mstReport = JasperCompileManager.compileReport(mstDesign);

					if (exp.getTable() == null || exp.getTable().size() <= 0)
						mstJasperPrint = JasperFillManager.fillReport(
								mstReport, map, new JREmptyDataSource());
					else
						mstJasperPrint = JasperFillManager.fillReport(
								mstReport, map, jc);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("creMstRpt==" + e.getMessage());

				}
				V_DOWN_PAGE += mstJasperPrint.getPages().size();
				// 子表页数
				synS.setSubPage(V_DOWN_PAGE);
				if (mstJasperPrint.getPages().size() > 0) {
					printAl.add(mstJasperPrint);
				}
				// 为了能把下钻的内容导出而写，目录中不包括下钻报表。
				if (isSub) {
					synList.add(synS);
					i++;
				}
				if (exps.getFileType().equalsIgnoreCase("html")) {
					HtmlAndFileUtil.initHtmlBodyDymic(exps.getHtml(), fliePath, exp, map, expList.size(), subFiledPParam.length);
				}
			}
			if (!exps.getFileType().equalsIgnoreCase("html")){
				snyMap.put(strKey.toString(), synList);
			}
			iR++;
		}
		if (exps.getFileType().equalsIgnoreCase("html")) {
			HtmlAndFileUtil.initHtmlEnd(exps.getHtml());
			return null;
		}
		List reValue = new ArrayList();
		try {
			reValue.add(coverJasperPrint); // 封面
			reValue.add(getSyn(snyMap, exps));// 简介 目录
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
			log.error("creMstRpt==" + e.getMessage());
		}
		reValue.addAll(printAl);
		return reValue;
	}

	private Map<ExpDateStruct, String> generateImage(List<ExpDateStruct> expList) {
		List<Callable<Map<ExpDateStruct, String>>> tasks = new ArrayList<Callable<Map<ExpDateStruct, String>>>();
		ExecutorService executor = Executors.newFixedThreadPool(
				Math.min(10, expList.size()), new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "GenerateReportImage");
					}
				});
		List<Future<Map<ExpDateStruct, String>>> results = null;
		try {
			for (ExpDateStruct stt : expList) {
				tasks.add(new HighChartTask(stt,
						HighChartExportTool.PLANTOMJS_EXPORT));
			}
			results = executor.invokeAll(tasks);
			Map<ExpDateStruct, String> allImage = new HashMap<ExpDateStruct, String>(
					expList.size());
			if (!GlobalUtil.isNullOrEmpty(results)) {
				for (Future<Map<ExpDateStruct, String>> imgStr : results) {
					if (!GlobalUtil.isNullOrEmpty(imgStr)) {
						allImage.putAll(imgStr.get());
					}
				}
			}
			return allImage;
		} catch (Exception e) {
			log.error("Generate highchart image fail!", e);
			return Collections.emptyMap();
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * 获取页眉图片
	 * 
	 * @return String 页眉图片
	 * 
	 */
	public static String getHead() {
		String rootPath = ReportUiUtil.getSysPath().substring(0,
				ReportUiUtil.getSysPath().indexOf("WEB-INF/"));
		String headImage = null;
		headImage = PropertyManager.getResource(SIMConstant.SYS_PROPERTIE_PATH)
				.getProperty(REPORT_FOOTER_IMAGE_PATH);
		String absPath = rootPath + headImage;
		File file = new File(absPath);
		String reValue = file.exists() ? absPath : DEFAULT_REPORT_IMAGE;
		return reValue;
	}

	// 测试审计用 生产环境删除
	private void getCreXml(JasperDesign mstDesign, String type) {
		// String tmpPath = ReportUiUtil.getSysPath("jrxml");
		String serverHome = System.getProperty("jboss.server.home.dir");
		String tmpPath = serverHome + File.separatorChar + "tmp"
				+ File.separatorChar + "reportXML";
		int osValue = 0;
		if (!new File(tmpPath).isFile())
			new File(tmpPath).mkdirs();
		try {
			JRXmlWriter.writeReport(
					mstDesign,
					tmpPath.substring(osValue, tmpPath.length()) + "/"
							+ System.currentTimeMillis() + "." + type
							+ ".jrxml", "GBK");
		} catch (JRException e) {
			e.printStackTrace();
			log.error("getCreXml==" + e.getMessage());
		}
	}

	/**
	 * 构建报表目录
	 * 
	 * @param LinkedHashMap
	 *            <String, List<SynStruct>> synMap 目录内容
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @return JasperPrint JasperPrint
	 * @throws Exception
	 */

	private JasperPrint getSyn(LinkedHashMap<String, List<SynStruct>> synMap,
			ExpStruct exps) throws Exception {
		// 子报表名列表
		StringBuffer subNameList = new StringBuffer();
		StringBuffer dvcNameList = new StringBuffer();
		StringBuffer muList = new StringBuffer();
		Iterator expIr = synMap.keySet().iterator();
		Object strKey = null;
		List<SynStruct> expList = null;
		int iR = 0;
		int mstSum = 0;
		String pageView = "";
		String staticPageView = ReportUiConfig.ExpNA;
		while (expIr.hasNext()) {
			strKey = expIr.next();
			expList = synMap.get(strKey);
			int i = 0;
			int pageSum = 0;
			StringBuffer subMuList = new StringBuffer();
			for (SynStruct exp : expList) {
				mstSum += exp.getSubPage();
				pageSum += exp.getSubPage();

				if (ReportUiUtil.checkNull(exp.getTalCategory())) {
					String[] talCategoryArray = exp.getTalCategory();
					String talCategory = "";
					if (talCategoryArray != null && talCategoryArray.length > 0) {
						for (int j = 0; j < talCategoryArray.length; j++) {
							if (talCategoryArray[j] != null
									&& !talCategoryArray[j].equals("")
									&& !talCategoryArray[j].equals("null")) {
								talCategory += "->" + talCategoryArray[j];
							}
						}
						if (talCategory.length() > 2) {
							talCategory = talCategory.substring(2);
						}
					}
					if (exp.getSubName().indexOf(talCategory) == -1) {
						exp.setSubName(exp.getSubName().replace("(",
								"(" + talCategory + " "));
					}
				}
				subNameList.append(
						exp.getSubName() + "(" + exp.getDvcIp() + ")").append(
						"、");
				if (exp.getSubPage() == 0)
					pageView = staticPageView;
				else
					// pageView = (mstSum - exp.getSubPage() + 1) + "";
					pageView = exp.getSubPage() + "";
				if (i == 0){
					// 1.2
					String cnDvcType = exp.getDvcType()==null?"":exp.getDvcType();
					cnDvcType=cnDvcType.replace("Comprehensive", "");
					cnDvcType = ReportUiUtil.getProperty(cnDvcType);
					if (cnDvcType.equals(exp.getDvcType())) {
						cnDvcType=DeviceTypeNameUtil.getDeviceTypeName(cnDvcType);
					}
					dvcNameList.append(cnDvcType + "("
									+ exp.getDvcIp() + ")").append("、");
				}
				// subMuList.append("&nbsp;" + "(" + (i + 1) + ")"
				String subjectTitle = exp.getSubName() + "(" + exp.getDvcIp()
						+ ")";
				int padDotCount = 160 - (String.valueOf(pageView).length() + subjectTitle
						.length()) * 4;
				subMuList.append("&nbsp;").append(subjectTitle)
						.append(StringUtil.lpad(pageView, padDotCount, '.'))
						.append("<br>");
				i++;
			}
			if (pageSum == 0)
				pageView = staticPageView;
			else
				pageView = (mstSum - pageSum + 1) + "";

			String subType = ReportUiUtil.getProperty(strKey.toString());
			if (subType.equals(strKey.toString())) {
				subType=subType.replace("Comprehensive", "");
				subType=DeviceTypeNameUtil.getDeviceTypeName(subType);
			}
			muList.append("(" + (iR + 1) + ")"
					+ subType + "<br>");
			muList.append(subMuList.toString());
			iR++;
		}
		Map<String, String> map = new HashMap<String, String>();
		// 加载目录
		JasperDesign synDesign = JRXmlLoader.load(ReportUiUtil
				.getSysPath(mstSynXmlPath));

		// 主报表设置title
		/*
		 * String one = exps.getRptName() + "，向您综合展示自" + exps.getRptTimeS() +
		 * "至" + exps.getRptTimeE() + "历时" +
		 * ReportUiUtil.getCountTime(exps.getRptTimeE(), exps.getRptTimeS()) +
		 * dvcNameList.toString().substring(0,dvcNameList.toString().length() -
		 * 1) + "设备，这段时间内运行的情况。"; String two = exps.getRptName()+ "，包含" +
		 * subNameList.toString().substring(0,subNameList.toString().length() -
		 * 1) + "。" + exps.getRptSummarize(); // 报表类型 目录 //one =
		 * ReportUiUtil.getExpItemValue(exps, one); //two =
		 * ReportUiUtil.getExpItemValue(exps, two); //map.put("one", one);// 系统
		 * 相关信息 //map.put("two", two);
		 */
		String three = ReportUiUtil.getExpItemValue(exps, muList.toString());
		map.put("three", three);
		JRDesignTextElement text = (JRDesignTextElement) synDesign
				.getColumnHeader().getElementByKey("staticText-1");
		setTextFieldStyle(text, 15);

		// JRBand titleBand = synDesign.getColumnHeader() ;
		// setTextFieldStyle("textField-2,staticText-2", titleBand);

		// JRBand columnHeaderBand = synDesign.getColumnHeader();
		// setTextFieldStyle("textField-1,staticText-3", columnHeaderBand);

		JRBand summaryBand = synDesign.getColumnHeader();
		setTextFieldStyle("textField-3,staticText-4", summaryBand);

		// add background start
		setBackground(synDesign, map);
		// add background end
		// getCreXml(synDesign, "目录");// del

		JasperReport synReport = JasperCompileManager.compileReport(synDesign);
		JasperPrint synJasperPrint = JasperFillManager.fillReport(synReport,
				map, new JREmptyDataSource());
		return synJasperPrint;
	}

	/**
	 * 设置背景image
	 * 
	 * @param JasperDesign
	 *            design JasperDesign
	 * @param Map
	 *            map JasperDesign所需参数
	 * @return
	 * 
	 */
	private static int BackGroundW = 595;// 背景宽
	private static int BackGroundBackH = 842;// 背景高

	private void setBackground(JasperDesign design, Map<String, String> map) {
		// add background start
		JRDesignBand bgBand = (JRDesignBand) design.getBackground();
		bgBand.setHeight(BackGroundBackH);
		JRDesignHelper.setImgControl(bgBand, new PositionStruct(0, 0,
				BackGroundBackH, BackGroundW), "bgChart", String.class,
				ReportUiConfig.param_P, null, new Byte("2"));
	}

	/**
	 * 给文本框组件设置Style
	 * 
	 * @param String
	 *            JrTextString 文本框名称
	 * @param design
	 *            synDesign JasperDesign
	 * @return
	 * 
	 */
	private void setTextFieldStyle(String JrTextString, JasperDesign design) {
		for (String JrTextStrings : JrTextString.split(",")) {
			JRDesignTextElement text = (JRDesignTextElement) design.getTitle()
					.getElementByKey(JrTextStrings);
			setTextFieldStyle(text);
		}

	}

	/**
	 * 给文本框组件设置Style
	 * 
	 * @param String
	 *            JrTextString 文本框名称
	 * @param design
	 *            synDesign JasperDesign
	 * @return
	 * 
	 */
	private void setTextFieldStyle(String JrTextString, JasperDesign design,
			int size) {
		for (String JrTextStrings : JrTextString.split(",")) {
			JRDesignTextElement text = (JRDesignTextElement) design.getTitle()
					.getElementByKey(JrTextStrings);
			setTextFieldStyle(text, size);
		}

	}

	/**
	 * 给文本框组件设置Style
	 * 
	 * @param String
	 *            JrTextString 文本框名称
	 * @param JRBand
	 *            jrBand JRBand
	 * @return
	 * 
	 */
	private void setTextFieldStyle(String JrTextString, JRBand jrBand) {

		for (String JrTextStrings : JrTextString.split(",")) {
			JRDesignTextElement text = (JRDesignTextElement) jrBand
					.getElementByKey(JrTextStrings);
			setTextFieldStyle(text);
		}

	}

	/**
	 * 给文本框组件设置Style
	 * 
	 * @param JRDesignTextElement
	 *            JrText 文本框名称
	 * @return
	 * 
	 */
	private void setTextFieldStyle(JRDesignTextElement JrText) {
		JrText.setFontName(FontName);
		JrText.setFontSize(12);
		JrText.setPdfFontName(PdfFontNameSong);
		JrText.setPdfEncoding(PdfEncodUCS2);
		JrText.setPdfEmbedded(true);
	}

	/**
	 * 给文本框组件设置Style
	 * 
	 * @param JRDesignTextElement
	 *            JrText 文本框名称 *
	 * @param int size 字体大小
	 * @return
	 * 
	 */
	private void setTextFieldStyle(JRDesignTextElement jrText, int size) {
		jrText.setFontName(FontName);
		jrText.setFontSize(size);
		jrText.setPdfFontName(PdfFontNameSong);
		jrText.setPdfEncoding(PdfEncodUCS2);
		jrText.setPdfEmbedded(true);
	}

	/**
	 * 根据内容计算高度
	 * 
	 * @param String
	 *            content 内容
	 * @return int content高度
	 * 
	 */
	private int getCWidth(String content) {
		int reValue = 0;
		return reValue;
	}

	/**
	 * 构建报表封面
	 * 
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @return JasperPrint JasperPrint
	 * @throws Exception
	 */
	private JasperPrint getCoverRpt(ExpStruct exps) throws Exception {
		Map map = new HashMap();
		String rpttype = exps.getRptType();
		String cuser = exps.getRptUser();
		String ctime = ReportUiUtil.getNowTime(ReportUiConfig.dFormat1);
		String dataTopn=exps.getTop();
		String clogtime = null;

		if (exps.getFileType().equals("rtf")) {
			clogtime = "<br>" + exps.getRptTimeS() + "<br>至<br>"
					+ exps.getRptTimeE() + "<br><br>";
		} else if (exps.getFileType().equals("excel")) {
			clogtime = ""
					+ exps.getRptTimeS()
					+ "<br>&emsp;&emsp;&emsp;&emsp;&emsp;至<br>&emsp;&emsp;&emsp;&emsp;&emsp;"
					+ exps.getRptTimeE();
			;
		}
		if (exps.getFileType().equalsIgnoreCase("html")) {
			clogtime = "" + exps.getRptTimeS() + "至" + exps.getRptTimeE();
		} else {
			clogtime = exps.getRptTimeS() + "<br>至<br>" + exps.getRptTimeE();
		}

		if (exps.getFileType().equals("excel")) {
			rpttype = "报表类型：" + rpttype;
			cuser = "制 作 人：" + cuser;
			ctime = "制作时间：" + ctime;
			dataTopn="数据top：" + dataTopn;
			clogtime = "日志时间：" + clogtime;
		} else {
			map.put("coverType", "报表类型：");
			map.put("coverAuthor", "制 作 人：");
			map.put("coverCreateTime", "制作时间：");
			map.put("coverLogTime", "日志时间：");
			map.put("coverTopn", "数据top：");
		}

		// 主报表设置title
		// map.put("coverimg", ReportUiUtil.getSysPath(coverLogo));
		map.put("rptname", exps.getRptName());
		// 报表类型
		map.put("rpttype", rpttype);
		// 创建用户
		map.put("cuser", cuser);
		// 报表创建时间
		map.put("ctime", ctime);
		// 报表内容时间区间
		map.put("dataTopn", dataTopn);
		map.put("clogtime", clogtime);

		JasperDesign coverDesign = null;
		if (exps.getFileType().equals("excel")) {
			coverDesign = JRXmlLoader.load(ReportUiUtil
					.getSysPath(mstCoverExcelXmlPath));
		} else {
			coverDesign = JRXmlLoader.load(ReportUiUtil
					.getSysPath(mstCoverXmlPath));
		}

		JRDesignTextElement text = (JRDesignTextElement) coverDesign.getTitle()
				.getElementByKey("textField-10");
		setTextFieldStyle(text, 36);
		setTextFieldStyle("textField-12,textField-11,textField-8,textField-9,textField-6",
				coverDesign, 18);

		if (exps.getFileType().equals("pdf")) {
			setTextFieldStyle(
					"textFieldyxj-1,textFieldyxj-2,textFieldyxj-3,textFieldyxj-4,textFieldyxj-5",
					coverDesign, 18);
		}
		if (exps.getFileType().equalsIgnoreCase("html")) {
			HtmlAndFileUtil.initHtmlBodyStaticContent(exps.getHtml(), map);
		}
		// 构造主报表
		JasperReport coverReport = JasperCompileManager
				.compileReport(coverDesign);
		JasperPrint coverJasperPrint = JasperFillManager.fillReport(
				coverReport, map, new JREmptyDataSource());

		return coverJasperPrint;
	}

	/**
	 * 构建报表目录
	 * 
	 * @param LinkedHashMap
	 *            <String, List> expMap 目录内容
	 * @param HttpServletRequest
	 *            request HttpServletRequest
	 * @param ExpStruct
	 *            exps 报表导出用结构体
	 * @return List<JasperPrint> JasperPrint list
	 * @throws Exception
	 */
	public List<JasperPrint> creMstRptBrowse(ExpDateStruct expDateStruct,
			HttpServletRequest request, ExpStruct exps) throws Exception {

		String spac = " ";
		int V_DOWN_PAGE = 0;
		List<JasperPrint> printAl = new ArrayList();
		List<ExpDateStruct> expList = null;
		int iR = 0;
		LinkedHashMap<String, List<SynStruct>> snyMap = new LinkedHashMap<String, List<SynStruct>>();
		JasperDesign mstDesign = null;
		List<SynStruct> synList = new ArrayList();
		int i = 0;
		// for (ExpDateStruts exp : expList) {
		ExpDateStruct exp = expDateStruct;
		SynStruct synS = new SynStruct();

		Boolean isSub = !exp.getSubType().endsWith("*");
		synS.setTalCategory(exp.getTalCategory());
		// 子表名称
		synS.setSubName(exp.getTitle());
		// 设备类型
		synS.setDvcType(exp.getSubType());
		String mstXmlPath = null;
		if (exp.getSubChart() != null) {
			mstXmlPath = ReportUiUtil.getSysPath(mstChartXmlPath);
		} else {
			mstXmlPath = ReportUiUtil.getSysPath(mstTableXmlPath);
		}
		// 加载主报表
		try {
			mstDesign = JRXmlLoader.load(mstXmlPath);
			// 处理导出word时“经典宋体简”为斜体
			if (exps.getFileType().equals("rtf")
					|| exps.getFileType().equals("docx")) {
				FontName = "微软雅黑";
			} else {
				FontName = "经典宋体简";
			}
			setFont(mstDesign);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("getCreXml==" + e.getMessage());

		}
		// $P
		Map map = new HashMap();

		// 主报表设置title 数字
		int iOSj = 1;
		// x.x 数字
		int num = 0;
		int rptWidth = mstDesign.getColumnWidth() - 59;// column 的宽

		int textH = 22;// text 高度
		int top = 71;
		int left = 53;
		int coef = 0;// 系数
		int tableImage = 225;
		int imgImage = 245;
		JRDesignBand titleBand = (JRDesignBand) mstDesign.getTitle();
		int headHigth = mstDesign.getTitle().getHeight();// column
		boolean picw = false;
		int leftTop = 0;
		// 2// Height// left,top,h,w
		if (i == 0) {
			String subType = ReportUiUtil.getProperty(exp.getSubType());
			if (subType.equals(exp.getSubType())) {
				subType=subType.replace("Comprehensive", "");
				subType=DeviceTypeNameUtil.getDeviceTypeName(subType);
			}
			String item1 = iOSj + " " + subType;
			if (isSub) {
				JRDesignHelper.setStaticTextControl(titleBand,
						new PositionStruct(left, top, textH, rptWidth), item1,
						TITLE);
			}
			leftTop = (iOSj + spac).length();
			coef++;
		} else {
			((JRDesignBand) mstDesign.getTitle()).setHeight(headHigth - textH);
			picw = true;
		}
		headHigth = mstDesign.getTitle().getHeight();// column

		int iTSj = (i + 1);// 主报表设置title
		// 2.1
		String itemS2 = iOSj + "." + iTSj + spac;
		String itemV2 = exp.getTitle();
		String item2 = itemS2 + itemV2;
		// left,top,h,w
		left += leftTop;
		if (isSub) {
			JRDesignHelper.setStaticTextControl(titleBand, new PositionStruct(
					left, textH * coef + top, textH, rptWidth), item2,
					NORMALTITLE);
		}
		coef++;
		String itemS3 = iOSj + "." + iTSj + "." + 1 + spac;
		String itemV3 = ReportUiConfig.ExpDvc + exp.getDvcIp();

		synS.setDvcIp(exp.getDvcIp()); // 子表页数

		// 2.2.1
		String item3 = itemS3 + itemV3;
		left += itemS3.length();
		// left,top,h,w
		if (isSub) {
			JRDesignHelper
					.setStaticTextControl(titleBand, new PositionStruct(left,
							(coef * textH + top), textH, rptWidth), item3,
							NORMAL);
		}
		coef++;
		// 2.2.2
		String itemS4 = iOSj + "." + iTSj + "." + 2 + spac;

		String itemV4 = "";
		if (exp.getSTime() == null || exp.getETime() == null) {
			itemV4 = ReportUiConfig.ExpSyn2.replace("$title", exp.getTitle());
		} else {
			itemV4 = ReportUiConfig.ExpSyn.replace("$title", exp.getTitle())
					.replace("$stime", exp.getSTime())
					.replace("$etime", exp.getETime());
		}

		itemV4 = ReportUiUtil.getExpItemValue(exps, itemV4);
		// 2.2.3
		String item4 = itemS4 + itemV4;
		// left,top,h,w
		String numT = "numT";

		if (isSub) {
			JRDesignHelper.addPParam(mstDesign, numT, String.class);

			JRDesignHelper.setTextControl(titleBand, new PositionStruct(left,
					coef * textH + top, textH, rptWidth), numT, String.class,
					ReportUiConfig.param_P, NORMAL, true);
		}
		coef += 2;//
		map.put(numT, item4);
		num = 2;
		if (exp.getSubChart() != null) {
			num++;
			// 图片title
			String itemS5 = iOSj + "." + iTSj + "." + num + spac;
			String itemV5 = exp.getTitle() + ReportUiConfig.ExpPic;
			String item5 = itemS5 + itemV5;
			if (!isSub) {
				item5 = itemV5;
				coef = 0;
			}
			// left,top,h,w
			JRDesignHelper.setStaticTextControl(titleBand, new PositionStruct(
					left, coef * textH + top, textH, rptWidth), item5, NORMAL);
			coef++;
			// 图片 left,top,h,w

			int picH = 0;
			if (picw) {
				// picH = headHigth - 145;
				picH = imgImage;
			} else {
				// picH = headHigth - 165;
				picH = tableImage;
			}

			/*
			 * JRDesignHelper.setImgControl(titleBand, new PositionStruct(left,
			 * coef * textH + top, picH, rptWidth),
			 * "chart",net.sf.jasperreports.
			 * engine.JRRenderable.class,ReportUiConfig.param_P, BOLBTITLE,
			 * null);
			 */
			JRDesignHelper.setImgControl(titleBand, new PositionStruct(left,
					coef * textH + top, picH, rptWidth), "chart",
					java.lang.String.class, ReportUiConfig.param_P, BOLBTITLE,
					null);
			coef++;
			top += picH;
		}
		// map.put("chart", new JCommonDrawableRenderer((JFreeChart)
		// exp.getSubChart()));
		map.put("chart", exp.getSubChart());
		map.put("title", exp.getTitle());
		map.put("V_DOWN_PAGE", V_DOWN_PAGE);

		map.put("footlogo", getHead());
		map.put("headlogo", getHead());
		map.put("cretime", ReportUiUtil.getNowTime(ReportUiConfig.dFormat1));
		// table TITLE 分组
		String[] subFiledPParam = exp.getTitleLable().split(",");
		int tableWidth = rptWidth / subFiledPParam.length;
		num++;
		String itemS6 = iOSj + "." + iTSj + "." + num + spac;
		String itemV6 = exp.getTitle() + ReportUiConfig.ExpTable;
		String item6 = itemS6 + itemV6;
		if (!isSub) {
			item6 = itemV6;
		}
		// left,top,h,w table title 题目统计表 题目

		if (exps.getFileType().equals("doc")) {
			JRDesignHelper.setStaticTextControl(titleBand, new PositionStruct(
					left, coef * textH + top, textH, rptWidth), item6, BOLB);
		} else {
			if (exp.getTable() != null && exp.getTable().size() > 0) {// 表格标题
				JRDesignHelper.setStaticTextControl(titleBand,
						new PositionStruct(left, coef * textH + top, textH,
								rptWidth), item6, BOLB);
			}
		}

		coef++;
		((JRDesignBand) mstDesign.getTitle()).setHeight((coef * textH + top));
		// 数据迭带
		String[] subFiledFParam = exp.getSubTableFile().split(",");
		for (int j = 0; j < subFiledPParam.length && exp.getTable() != null; j++) {// 表格
			// $p start// 创建p
			String paramPname = "tablep" + i + j;
			JRDesignHelper.addPParam(mstDesign, paramPname, String.class);
			// left top hight width 迭带title
			JRDesignHelper.setTextControl((JRDesignBand) mstDesign
					.getColumnHeader(), new PositionStruct(left
					+ (j * tableWidth), 0, textH, tableWidth), paramPname,
					String.class, ReportUiConfig.param_P, TABLETITLE, false);
			map.put(paramPname, subFiledPParam[j]);
			// $p end
			// $f start //stretch type relative to band height
			subFiledFParam[j] = subFiledFParam[j] == null
					|| subFiledFParam[j].trim().length() <= 0 ? ""
					: subFiledFParam[j];
			JRDesignHelper
					.addFParam(mstDesign, subFiledFParam[j], String.class);

			JRDesignHelper.setTextControl((JRDesignBand) mstDesign.getDetail(),
					new PositionStruct(left + (j * tableWidth), 0, textH,
							tableWidth), subFiledFParam[j], String.class,
					ReportUiConfig.param_F, BOLBTITLE, false);
			// $f end
		}

		// 填充f数据
		JRBeanCollectionDataSource jc = new JRBeanCollectionDataSource(
				ReportUiUtil.editMap(exp.getTable()));

		// add background start
		setBackground(mstDesign, map);
		// add background end

		// 构造主报表
		// 测试采样用 生产环境删除 start
		getCreXml(mstDesign, "内容");// del
		// 测试采样用 生产环境删除 end
		JasperReport mstReport = null;
		JasperPrint mstJasperPrint = null;
		try {
			mstReport = JasperCompileManager.compileReport(mstDesign);

			if (exp.getTable() == null || exp.getTable().size() <= 0)
				mstJasperPrint = JasperFillManager.fillReport(mstReport, map,
						new JREmptyDataSource());
			else
				mstJasperPrint = JasperFillManager.fillReport(mstReport, map,
						jc);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("creMstRpt==" + e.getMessage());

		}
		V_DOWN_PAGE += mstJasperPrint.getPages().size();
		// 子表页数
		synS.setSubPage(V_DOWN_PAGE);
		if (mstJasperPrint.getPages().size() > 0) {
			printAl.add(mstJasperPrint);
		}
		// 为了能把下钻的内容导出而写，目录中不包括下钻报表。
		if (isSub) {
			synList.add(synS);
			i++;
		}
		List reValue = new ArrayList();
		try {
			reValue.add(getCoverRptBrowse(exps)); // 封面
		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
			log.error("creMstRpt==" + e.getMessage());

		}
		reValue.addAll(printAl);
		return reValue;
	}

	/**
	 * 构建报表封面
	 * 
	 * @param ExpStruct
	 *            exp 报表导出用结构体
	 * @return JasperPrint JasperPrint
	 * @throws Exception
	 */
	private JasperPrint getCoverRptBrowse(ExpStruct exps) throws Exception {
		Map map = new HashMap();
		String rpttype = "报表查询";
		String cuser = exps.getRptUser();
		String ctime = ReportUiUtil.getNowTime(ReportUiConfig.dFormat1);
		String clogtime = exps.getRptTimeS() + "<br>至<br>" + exps.getRptTimeE();

		if (exps.getFileType().equals("rtf")) {
			clogtime = "<br>" + clogtime + "<br><br>";
		} else if (exps.getFileType().equals("excel")) {
			clogtime = "<br>" + clogtime;
		}

		if (exps.getFileType().equals("excel")) {
			rpttype = "报表类型：" + rpttype;
			cuser = "制作人：" + cuser;
			ctime = "制作时间：" + ctime;
			clogtime = "日志时间：" + clogtime;
		} else if (exps.getFileType().equals("pdf")) {
			map.put("coverType", "报表类型：");
			map.put("coverAuthor", "制作人：");
			map.put("coverCreateTime", "制作时间：");
			map.put("coverLogTime", "日志时间：");
		}
		// 主报表设置title
		// map.put("coverimg", ReportUiUtil.getSysPath(coverLogo));
		map.put("rptname", exps.getRptName());
		// 报表类型
		map.put("rpttype", rpttype);
		// 创建用户
		map.put("cuser", cuser);
		// 报表创建时间
		map.put("ctime", ctime);
		// 报表内容时间区间

		map.put("clogtime", clogtime);

		JasperDesign coverDesign = null;
		if (exps.getFileType().equals("excel")) {
			coverDesign = JRXmlLoader.load(ReportUiUtil
					.getSysPath(mstCoverExcelXmlPath));
		} else {
			coverDesign = JRXmlLoader.load(ReportUiUtil
					.getSysPath(mstCoverXmlPath));
		}

		JRDesignTextElement text = (JRDesignTextElement) coverDesign.getTitle()
				.getElementByKey("textField-10");
		setTextFieldStyle(text, 48);
		setTextFieldStyle("textField-11,textField-8,textField-9,textField-6",
				coverDesign, 18);

		if (exps.getFileType().equals("pdf")) {
			setTextFieldStyle(
					"textFieldyxj-1,textFieldyxj-2,textFieldyxj-3,textFieldyxj-4",
					coverDesign, 18);
		}

		JasperReport coverReport = JasperCompileManager
				.compileReport(coverDesign);

		JasperPrint coverJasperPrint = JasperFillManager.fillReport(
				coverReport, map, new JREmptyDataSource());

		return coverJasperPrint;
	}

	/**
	 * 获得报表默认的字体,经典宋体简
	 * 
	 * @return
	 */
	public static Font getDefaultFont(int style, int size) {
		return new Font(FontName, style, size);
	}

	/**
	 * 获取默认的字体,style为Font.PLAIN,字体大小默认为12
	 * 
	 * @return
	 */
	public static Font getDefaultFont() {
		return new Font(FontName, Font.PLAIN, 12);
	}

	// public static void main(String[] args) {
	// String str =
	// "full，包含防火墙源IP流入流量排行(192.168.73.10)、防火墙源IP流入流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙源IP流入流量排行(File Sharing 协议组跟踪)(192.168.73.10)、防火墙源IP流出流量排行(192.168.73.10)、防火墙源IP流出流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙源IP流出流量排行(FTP 协议组跟踪)(192.168.73.10)、防火墙源IP总流量排行(192.168.73.10)、防火墙源IP总流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙源IP总流量排行(Network Management 协议组跟踪)(192.168.73.10)、防火墙目的IP流入流量排行(192.168.73.10)、防火墙目的IP流入流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙目的IP流入流量排行(Network Management 协议组跟踪)(192.168.73.10)、防火墙目的IP流出流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙目的IP流出流量排行(192.168.73.10)、防火墙概要统计(192.168.73.10)、防火墙危险级别排行()、防火墙危险级别小时趋势(192.168.73.10)、防火墙阻断小时趋势(192.168.73.10)、防火墙非阻断排行(192.168.73.10)、防火墙阻断排行(192.168.73.10)、防火墙协议组连接排行(192.168.73.10)、防火墙目的IP连接排行(192.168.73.10)、防火墙源IP连接排行(192.168.73.10)、防火墙流量小时趋势(192.168.73.10)、防火墙协议组总流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙协议组总流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙协议组总流量排行(192.168.73.10)、防火墙协议组流出流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙协议组流出流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙协议组流出流量排行(192.168.73.10)、防火墙协议组流入流量排行(1.1.1.1 目的IP跟踪)(192.168.73.10)、防火墙协议组流入流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙协议组流入流量排行(192.168.73.10)、防火墙目的IP总流量排行(Network Management 协议组跟踪)(192.168.73.10)、防火墙目的IP总流量排行(1.1.1.1 源IP跟踪)(192.168.73.10)、防火墙目的IP总流量排行(192.168.73.10)、防火墙目的IP流出流量排行(Network Management 协议组跟踪)(192.168.73.10)、交换机危险级别小时趋势(192.168.78.123)、交换机事件源小时趋势(192.168.78.123)、交换机事件类型小时趋势(192.168.78.123)、交换机事件源排行(192.168.78.123)、交换机事件类型排行(192.168.78.123)。";
	// ExpStruct ep = new ExpStruct();
	// ep.setFileType("aa");
	// str = ReportUiUtil.getExpItemValue(ep, str);
	// Font f = new Font("仿宋", Font.PLAIN, 12);
	// FontMetrics fm = SwingUtilities2.getFontMetrics(null, null, f);
	// int wid = SwingUtilities.computeStringWidth(fm, str);
	// int hight = fm.getHeight();
	// System.out.println("hight==" + hight);
	// System.out.println(wid);
	// System.out.println(wid / 478);
	// }
}
