package com.topsec.tsm.rest.server.upgrade;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.topsec.tsm.node.metadata.NodeConfigurationFormater;
import com.topsec.tsm.node.metadata.NodeConfigurationMetadata;
import com.topsec.tsm.rest.server.common.RestSecurityAuth;
import com.topsec.tsm.rest.server.common.RestUtil;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.service.SimNodeUpgradeService;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.FacadeUtil;
import com.topsec.tsm.sim.util.NodeUtil;

@Path("/")
public class UpgradeRest {
	
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/upgrade/updateNodeSegment")
	public Response updateNodeSegment(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
		if(!isLogin){
			String msg = RestUtil.getInstance().errorMsg("Auth","访问 任务启动  接口前未登陆！");
           return build.entity(msg).build();
		}
		// 处理下发
		NodeMgrFacade nodeMgrFacade = FacadeUtil.getNodeMgrFacade(request, null);
		Node node = nodeMgrFacade.getKernelAuditor(true, true, true, true);
		NodeConfigurationFormater formater = (NodeConfigurationFormater) NodeUtil.findNodeSegmentConfig(node, NodeConfigurationFormater.class) ;
		if (formater != null) {
				NodeConfigurationMetadata metaData = formater.getConfiguration();
				metaData.setUpgradeCycle(request.getParameter("timeExpression"));
				formater.setConfiguration(metaData);
				nodeMgrFacade.updateNodeSegmentAndDispatch(node, formater);
		}
		return null;
		
	}
	
	
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/upgrade/queryPatch")
	public Response queryPatch(@Context HttpServletRequest request){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/xml;charset=UTF-8");
		SimNodeUpgradeService simNodeUpgradeService = (SimNodeUpgradeService)FacadeUtil.getFacadeBean(request, null,"simNodeUpgradeService");
		Map<String, String>  map = simNodeUpgradeService.getMaxVersionStrByType("WEB", request.getParameter("version"));
		if(map == null){
			 build.status(404);
			 return build.entity("error").build();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<patch port=\""+map.get("port")
					   +"\" host=\""+map.get("host")
					   +"\" user=\""+map.get("user")
					   +"\" password=\""+map.get("password")
					   +"\" home=\""+map.get("home")
					   +"\" patch=\""+map.get("patch")
					   +"\"/>");
		return build.entity(sb.toString()).build();
	}
}
