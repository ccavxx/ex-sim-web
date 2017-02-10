package com.topsec.tsm.sim.report.exp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.bean.struct.ExpStruct;
import com.topsec.tsm.sim.report.common.ReportFileCreator;
import com.topsec.tsm.sim.report.jasper.JRReportFileCreator;
import com.topsec.tsm.sim.report.jasper.SchemeReport;
import com.topsec.tsm.sim.report.persistence.RptMaster;
import com.topsec.tsm.sim.report.util.ReportUiConfig;
import com.topsec.tsm.sim.resource.persistence.Node;

/**
 * 自定义报表文件导出
 * @author hp
 *
 */
public class CustomReportExport extends SchemeReport {

	private RptMaster report ;
	public CustomReportExport(RptMaster report) {
		this.report = report;
	}
	@Override
	public String saveMstReport(ExpStruct exp) throws Exception {
		Parameter parameter = new Parameter() ;
		parameter.put(ReportUiConfig.Html_Field.get(1), exp.getTop()) ;//Top N
		parameter.put(ReportUiConfig.Html_Field.get(2), exp.getRptTimeS()) ;//开始时间
		parameter.put(ReportUiConfig.Html_Field.get(3), exp.getRptTimeE()) ;//结束时间
		parameter.put("nodeId", getNodeIds()) ;
		ReportFileCreator reportFileCreator = new JRReportFileCreator(report, exp.getFileType(),parameter) ;
		//服务器临时文件路径
		StringBuffer filePath = new StringBuffer(System.getProperty("jboss.server.home.dir")).append(File.separatorChar).append("tmp").append(File.separatorChar);
		//文件名称
		filePath.append(report.getMstName()).append("_").append(StringUtil.currentDateToString("yyyy-MM-dd HH.mm.ss.SSS")) ;
		File tempFile = new File(filePath.append(exp.getFileExtension()).toString()) ;
		if(tempFile.exists()){//如果文件存在，在文件后追加一个1000以内的随机数
			filePath.append(".").append(new Random().nextInt(1000)) ;
			tempFile = new File(filePath.append(exp.getFileExtension()).toString()) ;
		}
		try {
			FileOutputStream fileOS = new FileOutputStream(tempFile) ;
			reportFileCreator.exportReportTo(fileOS) ;
			fileOS.flush() ;
			ObjectUtils.close(fileOS) ;
			String zipFile = zipFile(tempFile.getAbsolutePath().replace('\\', '/')) ;
			return zipFile ;
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		}finally{
			if(tempFile!=null&&tempFile.exists()){
				tempFile.delete() ;
			}
		}
	}
	/**
	 * 获取系统各节点id
	 * @return
	 * @throws Exception
	 */
	private String[] getNodeIds()throws Exception{
		NodeMgrFacade nodeMgrFacade=(NodeMgrFacade)SpringContextServlet.springCtx.getBean("nodeMgrFacade");
		List<Node> nodes = nodeMgrFacade.getNodesByType(NodeDefinition.NODE_TYPE_AUDIT, false, false, false, false);
		if(ObjectUtils.isNotEmpty(nodes)){
			int nodeLength = nodes.size() ;
			String[] nodeIds = new String[nodeLength] ;
			for(int i=0;i<nodeLength;i++){
				nodeIds[i] = nodes.get(i).getNodeId();
			}
			return nodeIds ;
		}
		return new String[0] ;
	}
}
