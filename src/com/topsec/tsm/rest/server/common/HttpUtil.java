package com.topsec.tsm.rest.server.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;

public class HttpUtil {
	
	//返回字符串
	public static String doPostWithSSLByString(String url, String params, Map<String, String> cookies, String charset) throws Exception{
		String result = null;
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		HttpResponse response = null;
		httpClient = SSLClient.wrapClient(new DefaultHttpClient());
	    httpPost = new HttpPost(url);
		charset = StringUtils.isNotBlank(charset) ? charset : "UTF-8";
		httpPost.addHeader(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
		
		//设置参数
		if(StringUtils.isNotBlank(params)){
			HttpEntity myEntity = new StringEntity(params, charset);
			httpPost.setEntity(myEntity);
		}
		//设置cookie
		if(cookies != null && cookies.size() > 0)
			httpPost = (HttpPost) packCookies(httpPost, cookies);
		
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000); 
		response = httpClient.execute(httpPost);
		if(response != null){
			HttpEntity entity = response.getEntity();
			if(entity != null){
				result = EntityUtils.toString(entity, charset);
			}
		}
		return result;
	}
	
	/**
	 * 通过URL参数、cookie返回对应结果(post的SSL请求)
	 * @param url
	 * @param params
	 * @param cookies
	 * @param charset
	 * @return
	 */
	public static String doPostWithSSLByMap(String url, Map<String, Object> params, Map<String, String> cookies, String charset) throws Exception{
		String result = null;
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		HttpResponse response = null;
		httpClient = SSLClient.wrapClient(new DefaultHttpClient());
		httpPost = new HttpPost(url);
		charset = StringUtils.isNotBlank(charset) ? charset : "UTF-8";
		httpPost.addHeader(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
		//设置参数
		if(params != null && params.size() > 0)
			httpPost = (HttpPost) packParams(httpPost, params, charset);
		//设置cookie
		if(cookies != null && cookies.size() > 0)
			httpPost = (HttpPost) packCookies(httpPost, cookies);

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000); 
		response = httpClient.execute(httpPost);
		if(response != null && response.getStatusLine().getStatusCode() == 200){
			HttpEntity entity = response.getEntity();
			if(entity != null){
				result = EntityUtils.toString(entity, charset);
			}
		}
		return result;
	}
	/**
	 * 通过URL参数、cookie返回对应结果(post请求)
	 * @param url
	 * @param params
	 * @param cookies
	 * @param charset
	 * @return
	 */
	public static String doPostByMap(String url, Map<String, Object> params, Map<String, Object> cookies, String charset) throws Exception{
		return null;
	}

	/**
	 * 通过URL参数、cookie返回对应结果(get请求)
	 * @param url
	 * @param params
	 * @param cookies
	 * @param charset
	 * @return
	 */
	public static String doGetWithSSL(String url, Map<String, Object> params, Map<String, String> cookies, String charset) throws Exception{
		String result = null;
		HttpClient httpClient = null;
		HttpGet httpGet = null;
		HttpResponse response = null;
		httpClient = SSLClient.wrapClient(new DefaultHttpClient());
		httpGet = new HttpGet(url);
		charset = StringUtils.isNotBlank(charset) ? charset : "UTF-8";
		httpGet.addHeader(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
		StringBuffer path = null;
		//设置参数
		if(params != null && params.size() > 0){
			path = new StringBuffer();
			for (String param : params.keySet()) {
				path.append(param + "=" + StringUtil.toString(params.get(param)) + "&");
			}
			path.deleteCharAt(path.length()-1);
			url = url + "?" + URLEncoder.encode(path.toString());
		}
		//设置cookie
		httpGet = (HttpGet) packCookies(httpGet, cookies);

		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000); 
		response = httpClient.execute(httpGet);
		if(response != null){
			HttpEntity entity = response.getEntity();
			if(entity != null){
				result = EntityUtils.toString(entity, charset);
			}
		}
		return result;
	}
	
	/**
	 * 组装请求参数
	 * @param httpPost
	 * @param params
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static HttpRequestBase packParams(HttpPost httpPost, 
			Map<String, Object> params, String charset) throws UnsupportedEncodingException {
		if(ObjectUtils.isEmpty(params)){
			return httpPost;
		}
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for(Map.Entry<String, Object> entry:params.entrySet()){
			Object value = entry.getValue() ;
			if(value instanceof Object[]){
				List<String> valueList = new ArrayList<String>() ;
				for(Object val:(Object[])value){
					valueList.add(StringUtil.toString(val)) ;
				}
				list.add(new BasicNameValuePair(entry.getKey(), StringUtil.join(valueList)));
			}else{
				list.add(new BasicNameValuePair(entry.getKey(), StringUtil.toString(value)));
			}
		}
		if(list.size() > 0){
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
			httpPost.setEntity(entity);
		}
		return httpPost;
	}
	
	/**
	 * 组装请求cookie
	 * @param httpType
	 * @param params
	 * @return
	 */
	private static HttpRequestBase packCookies(HttpRequestBase httpType, Map<String, String> cookies) {
		String cookie = "";
		for (Entry<String, String> entry : cookies.entrySet()) {
			cookie += entry.getKey() + "=" + entry.getValue();
		}
		if(!"".equals(cookie))
			httpType.addHeader("Cookie", cookie);
		return httpType;
	}
	//SSLClient
	private static class SSLClient extends DefaultHttpClient{
		public static HttpClient wrapClient(HttpClient base){
			try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				X509TrustManager tm = new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
					@Override
					public X509Certificate[] getAcceptedIssuers() { return null; }
				};
				ctx.init(null, new TrustManager[]{tm}, null);
				SSLSocketFactory ssf = new SSLSocketFactory(ctx);
				ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				ClientConnectionManager ccm = base.getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				sr.register(new Scheme("https", ssf, 443));
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(base.getParams(), sr);
				return new DefaultHttpClient(mgr, base.getParams());
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static String cleanURL(String url) {
		if(url.contains(" ")){
			url = url.replace(" ", "%20");
		}
		return url;
	}
}
