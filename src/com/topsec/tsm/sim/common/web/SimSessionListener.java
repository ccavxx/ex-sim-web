package com.topsec.tsm.sim.common.web;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.base.type.Severity;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.resource.AuditCategoryDefinition;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;

public class SimSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession() ;
		SID sid = (SID)session.getAttribute(SIMConstant.SESSION_SID);
		if(sid != null){
			String userName =sid.getUserName();
			SID loginSID = LoginUserCache.getInstance().getLoginUserCachByName(userName) ;
			if(loginSID != null && sid == loginSID){
				LoginUserCache.getInstance().removeUser(userName);
			}
		}
	}

}
