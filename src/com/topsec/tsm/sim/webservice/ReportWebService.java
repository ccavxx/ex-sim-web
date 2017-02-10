package com.topsec.tsm.sim.webservice;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.Parameter;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.common.SubjectModel;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.webservice.xmltype.ReportSubjectElement;

@Path("/report")
public class ReportWebService {

	@Path("data")
	@GET
	@Produces("application/xml")
	public ReportSubjectElement getSubjectData(
				@QueryParam("subjectId")Integer subjectId,
				@QueryParam("startTime")String startTime,
				@QueryParam("endTime")String endTime,
				@QueryParam("securityObjectType")String securityObjectType,
				@QueryParam("dvcAddress")String dvcAddress,
				@QueryParam("top")Integer top){
		NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringContextServlet.springCtx.getBean("nodeMgrFacade") ;
		Node auditorNode = nodeMgrFacade.getKernelAuditor(false, false, false, false);
		ReportSubjectElement subject = new ReportSubjectElement(subjectId,null) ;
		if(auditorNode == null){
			return subject ;
		}
		Parameter param = new Parameter() ;
		param.put("id", StringUtil.toString(subjectId)) ;
		param.put("talTop", StringUtil.toString(top)) ;
		param.put("talStartTime", startTime) ;
		param.put("talEndTime", endTime) ;
		param.put("dvcAddress", dvcAddress) ;
		param.put("deviceType", securityObjectType) ;
		param.put("onlyByDvctype", StringUtil.isBlank(dvcAddress) ? "onlyByDvctype" : "") ;
		param.put("nodeId", new String[]{auditorNode.getNodeId()}) ;
		SubjectModel subjectModel = new SubjectModel(subjectId, param) ;
		List<Map<String,Object>> data = subjectModel.getData() ;
		if(ObjectUtils.isNotEmpty(data)){
			System.out.println(data);
			subject.setData(data) ;
		}
		return subject;
	}
	
}
