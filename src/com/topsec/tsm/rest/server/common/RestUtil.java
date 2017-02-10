package com.topsec.tsm.rest.server.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

public class RestUtil {
	
	private static  RestUtil instance;
	public static final int PAGE_SIZE = 10 ;
	private static Logger log = Logger.getLogger( RestUtil.class);
	

	public static String getStrFromInputStream(InputStream inputStream) throws IOException{
		
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
		    if (inputStream != null) {
		        bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		        char[] charBuffer = new char[128];
		        int bytesRead = -1;
		        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
		            stringBuilder.append(charBuffer, 0, bytesRead);
		        }
		    } else {
		        stringBuilder.append("");
		    }
		
		} finally {
		    if (bufferedReader != null) {
		        try {
		            bufferedReader.close();
		        } catch (IOException ex) {
		            
		        }
		    }
		}
		return stringBuilder.toString();
		
	}
	public String errorMsg(String type,String msg){
    	StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	sb.append("<Result value=\"Fail\">")
    	.append("<ErrorMsg type=\"")
    	.append(type)
    	.append("\">")
    	.append(msg)
    	.append("</ErrorMsg>")
    	.append("</Result>");
    	return sb.toString();
	}
	public static String success(String msg){
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	sb.append("<Result success=\"true\">")
    	  .append("<Message>").append(msg).append("</Message>")
    	  .append("</Result>");
    	return sb.toString();
	}
	public static String fail(String msg,String otherXML){
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    	sb.append("<Result success=\"false\">")
    	  .append("<Message>").append(msg).append("</Message>")
    	  .append(otherXML)
    	  .append("</Result>");
    	return sb.toString();
	}

	/**
	 * 生成sessionid
	 * @return
	 */
	public synchronized static String randomTokens(){
	    Random random=new Random();
	    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm"); 
	    String dataStr=(Math.abs(random.nextLong()))+""+df.format(new Date());
	    return dataStr.substring(0,16);
	}
	public static  RestUtil getInstance(){
		if(instance==null){
			synchronized (RestUtil.class) {
				if(instance==null){
					instance = new RestUtil() ;
				}
			}
		}
		return instance ;
	}
	
	
	/**
	 * 或许sessionId
	 * @param ip
	 * @return
	 */
	public static String getSessionId(String ip){
		String loginResult = login(ip) ;
		String sessionIdIndex = "<SessionId>" ;
		String sessionIdEndIndex = "</SessionId>" ;
		String sessionId="";
		try {
			sessionId = loginResult.substring(loginResult.indexOf(sessionIdIndex)+11,loginResult.indexOf(sessionIdEndIndex)) ;
		} catch (Exception e) {
			sessionId ="";
		}
		return sessionId ;
	}
	/**
	 * 验证用户是否登陆
	 * @param ip
	 * @return
	 */
	public static String login(String ip){
		String s ="<Login><Name>administrator</Name><Password>3935e49ed41a6c9b13fdd7056128fbd5</Password></Login>";
		try{
		String reqUrl=getServiceURL(ip);
		return HttpUtil.doPostWithSSLByString(reqUrl, s, null, "UTF-8");
//		ClientRequest request=new ClientRequest(reqUrl);
//		request.setHttpMethod("POST");
//	 	request.body("application/xml;charset=UTF-8", s);
//	 	ClientResponse<String> response=request.post(String.class);
//		 if (response.getStatus()==200){
//			 String result = response.getEntity();
//			 return result ;
//		 }
		}catch(Exception e){
		}
		return "";
		
	}
	
	public static String getServiceURL(String ip){
		return "https://"+ip+"/resteasy/login/auth" ;
	}
}
