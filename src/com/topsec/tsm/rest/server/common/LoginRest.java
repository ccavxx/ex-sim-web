package com.topsec.tsm.rest.server.common;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;

@Path("/")
public class LoginRest {
	private static final Logger log = LoggerFactory.getLogger(LoginRest.class);
	/**
	 * 登录认证
	 * @param request
	 * @param id 服务端sessionid
	 * @return
	 */
	@Produces("application/xml;charset=UTF-8")
	@POST
	@Path("/login/auth")
	public Response login(@Context HttpServletRequest request,@CookieParam("sessionid") String id){
		ResponseBuilder build=Response.status(200);
		build.header("Content-Type","text/html;charset=UTF-8");
		String context ="";
		String usr=null;
		String pwd=null;
		try {
			String content =RestUtil.getStrFromInputStream(request.getInputStream());
			Document document = DocumentHelper.parseText(content);
			Element root = document.getRootElement();
			usr = root.element("Name").getTextTrim();
			pwd = root.element("Password").getTextTrim();
			boolean isLogin = RestSecurityAuth.getInstance().isLogin(id);
			if(!isLogin){
				id= RestSecurityAuth.getInstance().login(usr, pwd);
			}
			if(id!=null && !"".equals(id)){
				
				String accountName = SID.ADMINISTRATOR ;
				SID sid ; 
				if(!LoginUserCache.getInstance().isExist(accountName)){
					sid = new SID(accountName,request.getRemoteAddr());
					LoginUserCache.getInstance().addUser(sid);
				}else{
					sid = LoginUserCache.getInstance().getLoginUserCachByName(accountName) ;
				}
				LoginUserCache.getInstance().updateUserLoginTime(accountName, System.currentTimeMillis()) ;
				
				Element result = DocumentHelper.createElement("Result");
				result.addAttribute("value", "Success");
				Element session = result.addElement("SessionId");
				session.addText(id);
				context = result.asXML();
				build.cookie(new NewCookie("sessionid", id)) ;
			}else{
				context = RestUtil.getInstance().errorMsg("Auth","登陆任务时， 用户名或者密码有误！");
			}
		} catch (Exception e) {
			log.info("用户名：{} 密码：{} 异常： {}",new Object[]{usr,pwd,e});
			context = RestUtil.getInstance().errorMsg("Auth","登陆任务时， 发生异常！");
		}
	 return build.entity(context).build();
	}
	
}
