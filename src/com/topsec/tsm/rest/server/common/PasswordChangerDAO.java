package com.topsec.tsm.rest.server.common;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.topsec.tsm.auth.manage.SID;
import com.topsec.tsm.framework.util.TSMResourceManage;

public class PasswordChangerDAO {
	public String getUserName(SID sid){		
		return sid.getUserName();
	}
	private String UPDATE_PWD="update auth_account set passwd=? where name=?";
	private String GET_PWD="select passwd from auth_account where name=?";
	private String GET_DESCRIPTION="select description from auth_account where name=?";
	private String xxx(byte hex){
		  byte low=(byte) (hex&(0x0F));
		  byte hi=(byte)((hex>>>4)&(0x0F));
		  byte [] cv=new byte[2];
		  cv[0]=inttochar(hi);
		  cv[1]=inttochar(low);
		  return new String(cv);
	}
	private  byte inttochar(byte hex) {

		  if (hex >= 0 && hex < 10) {
		   return (byte) (hex + '0');
		  } else if (hex > 9 && hex < 16) {
		   return (byte) (hex - 10 + 'a');
		  }
		  return (byte)0xFF;
	}
	
	public String toHashString(byte[] org){
		try{
		MessageDigest md = MessageDigest.getInstance("MD5");		
		byte [] pwd=md.digest(org);
		String ret="";
		//for (int i=pwd.length-1;i>=0;i--){
		for (int i=0;i<pwd.length;i++){
			ret+=xxx(pwd[i]);
			
		}
		return ret;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public Connection getConnection(){
		try{
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, TSMResourceManage
							.getResourceForFileNameAndKey(
									TSMResourceManage.JNDI_RESOURCE_FILE,
									"java.naming.factory.initial"));
			props.put(Context.PROVIDER_URL, TSMResourceManage
							.getResourceForFileNameAndKey(
									TSMResourceManage.JNDI_RESOURCE_FILE,
									"java.naming.provider.url"));

			props.put(Context.URL_PKG_PREFIXES, TSMResourceManage
							.getResourceForFileNameAndKey(
									TSMResourceManage.JNDI_RESOURCE_FILE,
									"java.naming.factory.url.pkgs"));
			InitialContext ctx=new InitialContext(props);
			DataSource ds=(DataSource) ctx.lookup("java:SIM_DS");
			return  ds.getConnection();			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String getDescriptionByUserName(String userName){
		Connection con=null;
		PreparedStatement ps=null;
		ResultSet rs = null;
		String pwd = null;
		try{
			con=this.getConnection();
			ps=con.prepareStatement(GET_DESCRIPTION);

			ps.setString(1,userName);
			rs = ps.executeQuery();
			if(rs!=null){
				rs.next();
				pwd = rs.getString(1);
			}	
		}catch(Exception e){
			throw new RuntimeException(e);
			
		}finally{
			if (rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return pwd;
	}
	
	public String getPasswordByUserName(String userName){
		Connection con=null;
		PreparedStatement ps=null;
		ResultSet rs = null;
		String pwd = null;
		try{
			con=this.getConnection();
			ps=con.prepareStatement(GET_PWD);

			ps.setString(1,userName);
			rs = ps.executeQuery();
			if(rs!=null){
				rs.next();
				pwd = rs.getString(1);
			}	
		}catch(Exception e){
			throw new RuntimeException(e);
			
		}finally{
			if (rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return pwd;
	}
	public void updateChangePassword(String userName,String password) {
		Connection con=null;
		PreparedStatement ps=null;
		try{
			con=this.getConnection();
			ps=con.prepareStatement(UPDATE_PWD);
			
			ps.setString(1,toHashString(password.getBytes()));
			ps.setString(2,userName);
			int ret=ps.executeUpdate();
			System.out.println("update password "+(ret==1));
		}catch(Exception e){
			throw new RuntimeException(e);
			
		}finally{
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
