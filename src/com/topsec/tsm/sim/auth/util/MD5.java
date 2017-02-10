package com.topsec.tsm.sim.auth.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class MD5 {
	public static String MD5(String inStr) {      
		MessageDigest md5 = null;      
		 try {      
		   md5 = MessageDigest.getInstance("MD5");      
		  } catch (Exception e) {      
		   System.out.println(e.toString());      
		   e.printStackTrace();      
		   return "";      
		  }      
		  char[] charArray = inStr.toCharArray();      
		  byte[] byteArray = new byte[charArray.length];      
		     
		  for (int i = 0; i < charArray.length; i++)      
		   byteArray[i] = (byte) charArray[i];      
		     
		  byte[] md5Bytes = md5.digest(byteArray);      
		     
		  StringBuffer hexValue = new StringBuffer();      
		     
		  for (int i = 0; i < md5Bytes.length; i++) {      
		   int val = ((int) md5Bytes[i]) & 0xff;      
		   if (val < 16)      
		    hexValue.append("0");      
		   hexValue.append(Integer.toHexString(val));      
		  }      
		     
		  return hexValue.toString();      
		 }  
	/**  

     * 对文件全文生成MD5摘要  

     *   

     * @param file  

     *            要加密的文件  

     * @return MD5摘要码  

     */ 

    public static String getMD5File(File file) {  

        FileInputStream fis = null;  

        try {  

            MessageDigest md = MessageDigest.getInstance("MD5");  

            fis = new FileInputStream(file);  

           byte[] buffer = new byte[2048];  

            int length = -1;  

            long s = System.currentTimeMillis();  

            while ((length = fis.read(buffer)) != -1) {  

               md.update(buffer, 0, length);  
           }  

            byte[] md5Bytes = md.digest();  
            StringBuffer hexValue = new StringBuffer();      
		     
  		  for (int i = 0; i < md5Bytes.length; i++) {      
  		   int val = ((int) md5Bytes[i]) & 0xff;      
  		   if (val < 16)      
  		    hexValue.append("0");      
  		   hexValue.append(Integer.toHexString(val));      
  		  }      

           return hexValue.toString(); 

        } catch (Exception ex) {  

            ex.printStackTrace();  

           return null;  

        } finally {  

            try {  

                fis.close();  

            } catch (IOException ex) {  

                ex.printStackTrace();  

            }  

       }  
    }  

}
