package com.topsec.tsm.sim.auth.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.audit.AuditRecord;
import com.topsec.tsm.base.type.IpAddress;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.auth.form.SafeMgrForm;
import com.topsec.tsm.sim.auth.security.SecurityFilter;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.web.SecurityRequestBody;
import com.topsec.tsm.sim.util.AuditLogFacade;

@Controller
@RequestMapping("safeMgr")
public class SafeMgrController {
	private static final Logger log = LoggerFactory.getLogger(SafeMgrController.class);

	/**
	 * editUI：安全管理页面 
	 * @author zhou_xiaohu
	 * @time 2014-03-25
	 * @return Result
	 */
	@RequestMapping("editUI")
	@ResponseBody
	public Object editUI(){
		Result result = new Result(true,"");
		try {
			Map<String, Object> configMap = SafeMgrConfigUtil.getInstance().getSafeMgrConfigList();
			if (configMap.size() > 0) {
				SafeMgrForm safeMgrForm = new SafeMgrForm();
				safeMgrForm.setLostTime(configMap.get("lostTime").toString());
				safeMgrForm.setFailCount(configMap.get("failCount").toString());
				safeMgrForm.setMinLowerCount(configMap.get("minLowerCount").toString());
				safeMgrForm.setMinUpperCount(configMap.get("minUpperCount").toString());
				safeMgrForm.setMinNumCount(configMap.get("minNumCount").toString());
				safeMgrForm.setPwdModifyCycle(configMap.get("pwdModifyCycle").toString());
				safeMgrForm.setMinCount(configMap.get("minCount").toString());
				String enableSecurityCheck = (String) configMap.get("securityCheck") ;
				safeMgrForm.setSecurityCheck(enableSecurityCheck == null ? true : StringUtil.booleanVal(enableSecurityCheck));
				result.setResult(safeMgrForm);
			}
		} catch (Exception e) {
			result.buildError("加载配置信息失败!");
			log.error(result.getMessage(),e);
		}
		return result;
		
	}

	/* 修改安全管理配置项 */
	@RequestMapping("edit")
	@ResponseBody
	public Object edit(@SecurityRequestBody @RequestBody SafeMgrForm safeMgrForm, SID sid, HttpServletRequest request) throws Exception {
		JSONObject json = new JSONObject();
		try {
			SafeMgrConfigUtil safeMgrConfigUtil = SafeMgrConfigUtil.getInstance();
//			request.getSession().setMaxInactiveInterval(Integer.parseInt(safeMgrForm.getLostTime()) * 60);// session过期时间
			int lastTime = Math.max(StringUtil.toInt(safeMgrForm.getLostTime()), 1) ;//至少一分钟
			int failCount = Math.max(StringUtil.toInt(safeMgrForm.getFailCount()),1) ;//至少一次
			safeMgrConfigUtil.updateSafeConfig("lostTime", StringUtil.toString(lastTime));
			safeMgrConfigUtil.updateSafeConfig("failCount", StringUtil.toString(failCount));
			safeMgrConfigUtil.updateSafeConfig("minUpperCount", safeMgrForm.getMinUpperCount());
			safeMgrConfigUtil.updateSafeConfig("minLowerCount", safeMgrForm.getMinLowerCount());
			safeMgrConfigUtil.updateSafeConfig("minNumCount", safeMgrForm.getMinNumCount());
			safeMgrConfigUtil.updateSafeConfig("pwdModifyCycle", safeMgrForm.getPwdModifyCycle());
			safeMgrConfigUtil.updateSafeConfig("minCount", safeMgrForm.getMinCount());
			safeMgrConfigUtil.updateSafeConfig("securityCheck",StringUtil.toString(safeMgrForm.isSecurityCheck()));
			safeMgrConfigUtil.store() ;
			SecurityFilter.ENABLED =safeMgrForm.isSecurityCheck();
			toLog(AuditCategoryDefinition.SYS_SAFEMGRCONFIG, "安全策略修改", "安全策略修改", sid.getUserName(), false, Severity.LOW, request.getRemoteHost());
			json.put("result","success");// 修改成功
		} catch (Exception e) {
			json.put("result", "failure");// 修改失败
			e.printStackTrace();
			return json;
		}
		return json;
	}
	private void toLog(String behavior, String name, String desc, String subject, boolean result, Severity severity,String remoteAddress) {
		AuditRecord _log = AuditLogFacade.createConfigAuditLog();
		_log.setBehavior(behavior);
		_log.setSecurityObjectName(name);
		_log.setDescription(desc);
		_log.setSubject(subject);
		_log.setSubjectAddress(new IpAddress(remoteAddress));
		_log.setObjectAddress(IpAddress.IPV4_LOCALHOST);
		_log.setSuccess(result);
		_log.setSeverity(severity);
		AuditLogFacade.send(_log);// 发送系统自审计日志
	}
}
