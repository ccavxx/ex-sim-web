package com.topsec.tsm.sim.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.topsec.tsm.sim.node.service.NodeMgrFacade;

public class FacadeUtil {

	public static WebApplicationContext getWebApplicationContext(
			HttpServletRequest request) {
		return WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
	}

	// public static ManagedGroupFacade getManagedGroupFacade(
	// HttpServletRequest request, String sid) {
	// return (ManagedGroupFacade) getWebApplicationContext(request).getBean(
	// ManagedGroupFacade.class.getName());
	// }

	// public static StandardMgrFacade getStandardFacade(
	// HttpServletRequest request, String sid) {
	// return (StandardMgrFacade) getWebApplicationContext(request).getBean(
	// StandardMgrFacade.class.getName());
	// }

	/**
	 * 获取Spring容器中的bean。
	 * 
	 * @param request
	 * @param sid
	 * @param beanName
	 *            xml中配置的bean的名称
	 * @return Object
	 */
	public static Object getFacadeBean(HttpServletRequest request, String sid,
			String beanName) {
		return getWebApplicationContext(request).getBean(beanName);
	}

	// public static DashBoardFacade getDashBoardFacade(
	// HttpServletRequest request, String sid) {
	// return (DashBoardFacade) getWebApplicationContext(request).getBean(
	// DashBoardFacade.class.getName());
	// }

//	public static DashboardDataManageable getDashboardDataManageable(HttpServletRequest request, String sid) {
//		return (DashboardDataManageable) getWebApplicationContext(request).getBean(DashboardDataManageable.class.getName());
//	}

	// public static StandardMgrFacade getStandardFacade(
	// HttpServletRequest request, String sid) {
	// return (StandardMgrFacade) getWebApplicationContext(request).getBean(
	// StandardMgrFacade.class.getName());
	// }

	// public static PolicyMgrFacade getPolicyFacade(
	// HttpServletRequest request, String sid) {
	// return (PolicyMgrFacade) getWebApplicationContext(request).getBean(
	// PolicyMgrFacade.class.getName());
	// }

	// public static DocumentManageFacade getDocumentManageFacade(
	// HttpServletRequest request, String sid) {
	// return (DocumentManageFacade) getWebApplicationContext(request).getBean(
	// com.topsec.tsm.sim.document.DocumentManageFacade.class.getName());
	// }

	// public static AssetManager getAssetManager(HttpServletRequest request,
	// String sid) {
	// return (AssetManager)
	// getWebApplicationContext(request).getBean(AssetManager.class.getName());
	// }
	//
	// public static VulnerabilityFacade getVulnerabilityFacade(
	// HttpServletRequest request, String sid) {
	// return (VulnerabilityFacade) getWebApplicationContext(request).getBean(
	// VulnerabilityFacade.class.getName());
	// }
	//
	public static NodeMgrFacade getNodeMgrFacade(HttpServletRequest request,String sid) {
		return (NodeMgrFacade) getWebApplicationContext(request).getBean("nodeMgrFacade");
	}

	// public static ConfigPolicyMgrFacade getConfigPolicyMgrFacade(
	// HttpServletRequest request, String sid) {
	// return (ConfigPolicyMgrFacade)
	// getWebApplicationContext(request).getBean("configPolicyMgrFacade");
	// }

	// public static UpgradePlanFacade getUpgradePlanFacade(
	// HttpServletRequest request, String sid) {
	// return (UpgradePlanFacade)
	// getWebApplicationContext(request).getBean("upgradePlanFacade");
	// }
	// //excutor
	// public static UpgradeCommandExecutor getCommandeExcutor(
	// HttpServletRequest request, String sid) {
	// return (UpgradeCommandExecutor)
	// getWebApplicationContext(request).getBean("upgradeCommandExcuter");
	// }
	//
	// // public static ActiveListService getActiveList(HttpServletRequest
	// request, String sid) {
	// // return (ActiveListService)
	// getWebApplicationContext(request).getBean(ActiveListService.class.getName());
	// // }
	//
	// public static UpgradeRecordFacade getUpgradeRecordFacade(
	// HttpServletRequest request, String sid) {
	// return (UpgradeRecordFacade)
	// getWebApplicationContext(request).getBean("upgradeRecordFacade");
	// }

}
