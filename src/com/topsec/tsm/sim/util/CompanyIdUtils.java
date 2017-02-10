package com.topsec.tsm.sim.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/*
 * 读取company.properties的工具类，方便更新系统界面与公司关联的信息
 */
public class CompanyIdUtils {
	private static CompanyIdUtils companyIdUtils = null;
	private Properties prop;

	private CompanyIdUtils() {
		
		String company=null;
		if(!TalVersionUtil.TAL_VERSION_SIM.equals(TalVersionUtil.getInstance().getVersion())){
			company="company.properties";
		}else{
			company="company_SIM.properties";
		}
		
		InputStream systemResourceAsStream = CompanyIdUtils.class.getResourceAsStream(company);
		InputStreamReader in = null;
		try {
			in = new InputStreamReader(systemResourceAsStream, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		prop = new Properties();
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public synchronized static CompanyIdUtils getInstance() {
		if (companyIdUtils == null) {
			companyIdUtils = new CompanyIdUtils();
		}
		return companyIdUtils;
	}

	public String getValue(String key) {
		return (String) prop.getProperty(key);
	}
}