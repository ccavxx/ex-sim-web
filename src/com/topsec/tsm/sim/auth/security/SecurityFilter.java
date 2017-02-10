package com.topsec.tsm.sim.auth.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.sim.auth.util.SafeMgrConfigUtil;

public class SecurityFilter implements Filter {
	
	protected FilterConfig filterConfig = null;
	public static String FORWARD_PATH = null;
	public static boolean ENABLED = false;
	private static final String[] DEFAULT_PAGES = new String[]{"/index.html","/index.jsp","/default.jsp","/default.html","/main.jsp","/main.html"} ;
	private List<String> localAddresss = new ArrayList<String>();
	private String[] availableReferer;
	private Set<String> ignoreURI = new HashSet<String>() ;
	private Set<String> emptyRefererURLs ;
	
	public void destroy() {
		this.filterConfig = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(!ENABLED){
			chain.doFilter(request, response) ;
			return ;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String method = req.getMethod() ;
		if(!(method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("GET"))){
			resp.setStatus(403) ;
			return ;
		}
		String requestURI = req.getRequestURI() ;
		if(requestURI.equals("/favicon.ico") || requestURI.startsWith("/resteasy")){
			chain.doFilter(req, resp) ;
			return ;
		}
		String referer = req.getHeader("referer");
		if (referer != null) {
			boolean referValid = StringUtils.startsWithAny(referer, availableReferer) ;
			if (!referValid) {
				SecurityUtil.goBackHome(req, resp, AttackType.CROSS_SCRIPT,"非法referer");
				return;
			}
		} else {
			if(!requestURI.startsWith("/sim") && !emptyRefererURLs.contains(requestURI.toLowerCase())){
				SecurityUtil.goBackHome(req, resp, AttackType.CROSS_SCRIPT,"非法referer");
				return ;
			}
		}
		try {
			if(ignoreURI.contains(requestURI)){
				chain.doFilter(req, resp) ;
				return ;
			}
			chain.doFilter(req, resp);
		}catch (Exception e) {
			if(e.getCause() instanceof AttackException){
				Attack attack = ((AttackException)e.getCause()).getAttack() ;
				SecurityUtil.goBackHome(req, resp, attack.type,attack.content) ;
			}else{
				SecurityUtil.goBackHome(req, resp, AttackType.ERROR);
			}
			return;
		}
	}
	
	public void init(FilterConfig conf) throws ServletException {
		this.filterConfig = conf;
		SecurityFilter.FORWARD_PATH = conf.getInitParameter("forwardPath");
		String enableSecurityCheck = SafeMgrConfigUtil.getInstance().getValue("securityCheck") ;
		SecurityFilter.ENABLED = enableSecurityCheck == null ? true : StringUtil.booleanVal(enableSecurityCheck) ;
		String[] refererURLs = StringUtil.trim(StringUtil.split(conf.getInitParameter("empty_referer_url"))) ;
		this.emptyRefererURLs = new HashSet<String>(refererURLs.length) ;
		for(String url:refererURLs){
			emptyRefererURLs.add(url.toLowerCase()) ;
		}
		this.localAddresss = getLocalAddress() ;
		List<String> refererList = new ArrayList<String>() ;
		for (String addr : localAddresss) {
			refererList.add("http://" + addr) ;
			refererList.add("https://" + addr) ;
		}
		refererList.add("http://127.0.0.1") ; 
		refererList.add("https://127.0.0.1") ; 
		for (String addr : localAddresss) {//ipv6地址放到最后
			refererList.add("http://[" + addr + "]") ; 
			refererList.add("https://[" + addr + "]") ; 
		}
		refererList.add("http://[0:0:0:0:0:0:0:1]") ;
		refererList.add("https://[0:0:0:0:0:0:0:1]") ;
		refererList.add("http://[::1]") ; 
		refererList.add("https://[::1]") ; 
		this.availableReferer = refererList.toArray(new String[0]) ;
	}

	// 解决多网卡、多地址情况下，只要符合其中任意一个地址就认为是对本机的访问
	private List<String> getLocalAddress() {
		List<String> addresss = new ArrayList<String>();
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				if(ni.isLoopback()){
					continue ;
				}
				while (ips.hasMoreElements()) {
					InetAddress inetAddress = ips.nextElement() ;
					addresss.add(inetAddress.getHostAddress());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return addresss;
	}
}