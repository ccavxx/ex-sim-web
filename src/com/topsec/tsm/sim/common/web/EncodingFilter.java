/*
 * Created on 2005-7-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.topsec.tsm.sim.common.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author kinfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EncodingFilter implements Filter
{

	private String encoding;

	public void destroy() {
	   this.encoding = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	   request.setCharacterEncoding(this.encoding);
	   chain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	   this.encoding = filterConfig.getInitParameter("encoding");
	}


}
