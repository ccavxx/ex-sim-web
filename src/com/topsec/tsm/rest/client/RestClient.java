package com.topsec.tsm.rest.client;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RestClient {
	private static final Logger log = LoggerFactory.getLogger(RestClient.class);
	public static void main(String[] args) {
//		String param ="<Register>" +
//						"<Ip>192.168.75.10</Ip>" +
//						"<NodeId>e095f50b-8cf5-4b4e-9e0c-fafbcb6bf922</NodeId>" +
//						"<Alias>王树青</Alias>" +
//						"<Type>register</Type>" +
//					  "</Register>";
		ClientRequest request=new ClientRequest("https://192.168.75.10/resteasy/node/systemInfo");
		request.setHttpMethod("POST");
		String sessionId = getSessionId() ;
		request.cookie("sessionid",sessionId);
//	 	request.body("application/xml;charset=UTF-8", param);
	 	ClientResponse<String> response;
		try {
			response = request.post(String.class);
			if (response.getStatus()==200){
				 String result = response.getEntity();
				 System.out.println(result);
				 Document document = DocumentHelper.parseText(result);
				Element root = document.getRootElement();
				System.out.println(root.attributeValue("storage_avaliable"));
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	public static String getSessionId(){
		String loginResult = login() ;
		String sessionIdIndex = "<SessionId>" ;
		String sessionIdEndIndex = "</SessionId>" ;
		String sessionId = loginResult.substring(loginResult.indexOf(sessionIdIndex)+11,loginResult.indexOf(sessionIdEndIndex)) ;
		System.out.println(sessionId);
		return sessionId ;
	}
	
	public static String login(){
		String s ="<Login><Name>operator</Name><Password>3935e49ed41a6c9b13fdd7056128fbd5</Password></Login>";
		try{
		String reqUrl=getServiceURL();
		ClientRequest request=new ClientRequest(reqUrl);
		request.setHttpMethod("POST");
	 	request.body("application/xml;charset=UTF-8", s);
	 	ClientResponse<String> response=request.post(String.class);
		 if (response.getStatus()==200){
			 String result = response.getEntity();
			 System.out.println(result);
			 return result ;
		 }
		}catch(Exception e){
		}
		return s;
		
	}
	
	public static String getServiceURL(){
		return "https://192.168.75.10/resteasy/log/auth" ;//http://192.168.75.30/resteasy/report/condition
	}
	
	@Test
	public void connectByHttps() throws Exception{
		String url = "https://192.168.75.188/resteasy/safety/receiveSafetyInfo";
        // 创建SSLContext对象，并使用我们指定的信任管理器初始化
        TrustManager[] tm = { new MyX509TrustManager() };
        SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
        sslContext.init(null, tm, new java.security.SecureRandom());
        // 从上述SSLContext对象中得到SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
//        trustAllHttpsCertificates();
//        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        // 创建URL对象
        URL myURL = new URL(url);
        // 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
        HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
        httpsConn.setSSLSocketFactory(ssf);
        // 取得该连接的输入流，以读取响应内容
        InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream());
        // 读取服务器的响应内容并显示
        int respInt = insr.read();
        while (respInt != -1) {
            System.out.print((char) respInt);
            respInt = insr.read();
        }
	}
	
	HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
            System.out.println("Warning: URL Host: " + urlHostName + " vs. "
                               + session.getPeerHost());
            return true;
        }
    };
	
	private static void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new MiTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}

	static class MiTM implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

	
	public class MyX509TrustManager implements X509TrustManager {
	    /*
	     * The default X509TrustManager returned by SunX509.  We'll delegate
	     * decisions to it, and fall back to the logic in this class if the
	     * default X509TrustManager doesn't trust it.
	     */
	    X509TrustManager sunJSSEX509TrustManager;
	    MyX509TrustManager() throws Exception {
	        // create a "default" JSSE X509TrustManager.
	        KeyStore ks = KeyStore.getInstance("JKS");
	        ks.load(new FileInputStream("D:/home/tomcat.keystore"), "talent123".toCharArray());
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
	        tmf.init(ks);
	        TrustManager tms [] = tmf.getTrustManagers();
	        /*
	         * Iterate over the returned trustmanagers, look
	         * for an instance of X509TrustManager.  If found,
	         * use that as our "default" trust manager.
	         */
	        for (int i = 0; i < tms.length; i++) {
	            if (tms[i] instanceof X509TrustManager) {
	                sunJSSEX509TrustManager = (X509TrustManager) tms[i];
	                return;
	            }
	        }
	        /*
	         * Find some other way to initialize, or else we have to fail the
	         * constructor.
	         */
	        throw new Exception("Couldn't initialize");
	    }
	    /*
	     * Delegate to the default trust manager.
	     */
	    public void checkClientTrusted(X509Certificate[] chain, String authType)
	                throws CertificateException {
	        try {
	            sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
	        } catch (CertificateException excep) {
	            // do any special handling here, or rethrow exception.
	        }
	    }
	    /*
	     * Delegate to the default trust manager.
	     */
	    public void checkServerTrusted(X509Certificate[] chain, String authType)
	                throws CertificateException {
	        try {
	            sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
	        } catch (CertificateException excep) {
	            /*
	             * Possibly pop up a dialog box asking whether to trust the
	             * cert chain.
	             */
	        }
	    }
	    /*
	     * Merely pass this through.
	     */
	    public X509Certificate[] getAcceptedIssuers() {
	        return sunJSSEX509TrustManager.getAcceptedIssuers();
	    }
	}

}
