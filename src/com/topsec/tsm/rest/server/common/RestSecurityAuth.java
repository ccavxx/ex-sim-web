package  com.topsec.tsm.rest.server.common;

import java.util.ArrayList;
import java.util.List;

public class RestSecurityAuth {
	
	private static RestSecurityAuth securityAuth;
	
	private static List<String> loginMap = new ArrayList<String>();
	
	private int cachSize = 100;
	private RestSecurityAuth(){
		
	}
	
	public String login(String username,String password){
		synchronized(loginMap) {
			String pwd = new PasswordChangerDAO().getPasswordByUserName(username);
			if(password.equals(pwd)){
				int size = loginMap.size();
				if (size >= cachSize) {
					loginMap.remove(0);
				}
				String id =RestUtil.randomTokens();
				loginMap.add(id);
				return id;
			}
			return null;
		}
		
	}
	
	public boolean isLogin(String sessionId){
		  if ( sessionId!=null&&!sessionId.equals("")){
			  return loginMap.contains(sessionId) ;
		  }
		   return false;
	}
	
	
	public static RestSecurityAuth getInstance(){
		if(securityAuth == null){
			securityAuth = new RestSecurityAuth();
		}
		return securityAuth;
	}

}
