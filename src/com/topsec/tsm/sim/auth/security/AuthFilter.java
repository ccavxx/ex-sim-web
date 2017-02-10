package com.topsec.tsm.sim.auth.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.auth.util.LoginUserCache;
import com.topsec.tsm.sim.auth.util.SID;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;

public class AuthFilter implements Filter {
	protected static Logger log = LoggerFactory.getLogger(AuthFilter.class);

	protected FilterConfig filterConfig = null;

	protected String forwardPath = null;
	private Set<String> unnecessaryUpdate = new HashSet<String>();
	private Set<String> passedURLs = new HashSet<String>();
	public void destroy() {
		this.filterConfig = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String requestURI = req.getRequestURI();
		if(requestURI.startsWith("/sim") && !passedURLs.contains(requestURI)){
			String accept = StringUtil.nvl(req.getHeader("Accept")) ;
			boolean isKeepAlive = requestURI.startsWith("/sim/keepAlive") ;
			boolean acceptJSON = accept.contains("application/json");
			SID sid = getUserSID(req) ;
			if(sid == null){
				if(isKeepAlive){
					resp.getWriter().print("-1") ;
				}else if(!acceptJSON){
					resp.sendRedirect(this.forwardPath) ;
				}
				return ;
			}else if(isKeepAlive){
				String token = req.getParameter("token") ;
				HttpSession session = req.getSession() ;
				if(session.getAttribute(SIMConstant.SESSION_TOKEN).equals(token)){
					resp.getWriter().print("1") ;
				}else{//当前请求的token与session中的token不一致，说明session已经被覆盖
					resp.getWriter().print("-2") ;
				}
				return  ;
			}
			if (!unnecessaryUpdate.contains(requestURI)){
				LoginUserCache.getInstance().updateUserLoginTime(sid.getUserName(), System.currentTimeMillis());
			}
		}
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			if(e.getCause() instanceof AttackException){
				Attack attack = ((AttackException)e.getCause()).getAttack() ;
				SecurityUtil.goBackHome(req, resp, attack.type,attack.content) ;
			}else{
				e.printStackTrace();
			}
		}
		
	}
	
	public SID getUserSID(HttpServletRequest req){
		// 登录页面
		SID sid = (SID) req.getSession().getAttribute(SIMConstant.SESSION_SID);
		if (sid == null || !sid.getLoginIP().equals(req.getRemoteAddr())) {
			return null;
		}
		//获取当前登陆名
		String userName = sid.getUserName();
		//获取当前用户登陆的系统时间
		//从缓存中获取用户登陆信息
		SID infor = LoginUserCache.getInstance().getLoginUserCachByName(userName);
		if (infor == null || sid != infor) {// 登录信息为空或者与当前缓存中的sid不同(同一电脑多浏览器登录)
			return null;
		}
		if (infor.getLastLoginTime() == -1 || !infor.getLoginIP().equals(req.getRemoteAddr())) {
			return null;
		}
		long now = System.currentTimeMillis();
		// 获取safeMgrConfig.xml配置信息
		long lostTime = Math.max(StringUtil.toLongNum(SafeMgrConfigUtil.getInstance().getValue("lostTime"),30),1);
		//判断用户最近登陆时间与当前系统时间比较 如果时间查过失效时间则强制退出
		boolean locked = StringUtil.booleanVal(req.getSession().getAttribute("locked")) ;
		if (!locked && (now - infor.getLastLoginTime()) > lostTime * 60 * 1000){
			log.warn(infor.getUserName() + "长时间不操作，强制退出。");
			return null;
		}
		return infor ;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		this.forwardPath = filterConfig.getInitParameter("forwardPath");
		String[] unUpdateURLs = StringUtil.split(filterConfig.getInitParameter("unnecessaryUpdate")) ;
		StringUtil.trim(unUpdateURLs) ;
		for(String url:unUpdateURLs){
			unnecessaryUpdate.add(url) ;
		}
		String[] urls = StringUtil.split(filterConfig.getInitParameter("passedURLs")) ;
		StringUtil.trim(urls) ;
		for(String url:urls){
			passedURLs.add(url);
		}
	}

}
