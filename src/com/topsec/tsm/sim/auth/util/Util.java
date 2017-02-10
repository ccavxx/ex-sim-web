package com.topsec.tsm.sim.auth.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class Util {
    public static Long ipTohl(InetAddress ipAddr) {
		long hostIP = 0L;
    	byte[] hostip = ipAddr.getAddress();
		
		hostIP = 256L*256L*256L*shortTouLong(hostip[0])
					+256L*256L*shortTouLong(hostip[1])
					+256L*shortTouLong(hostip[2])
					+shortTouLong(hostip[3]);
		return new Long(hostIP);
    }
    
    public static long shortTouLong(short ipAddr){
		short  mask = 0x7f;
		short  rt = (0x80 & ipAddr )> 0 ? (short)128:0;
		//System.out.println((long)(rt+ mask & ipAddr));
		return (long)(rt+ (mask & ipAddr));
	} 
    
    public static BigInteger toIpNumber(String ipString) throws UnknownHostException
    {
        if (ipString == null) return null;
        return new BigInteger(InetAddress.getByName(ipString).getAddress());
    }
    public static String inet_ntoa(long ipAddr) {
        byte[] bits = Util.longToByte(ipAddr);
        StringBuilder strBuf = new StringBuilder(18);
        int offset = 0;
        for (int i = 3; i >= 0; i--) {
            strBuf.insert(offset, Math.abs((int) bits[i]));
            strBuf.append(".");
            offset = strBuf.length();
        }
        strBuf.deleteCharAt(strBuf.length() - 1);
        return strBuf.toString();
    }
    
    /**
     * @param ipAddr
     * @return
     */
    public static byte[] longToByte(long ipAddr) {
        // TODO Auto-generated method stub
        byte[] bits = new byte[4];
        long a = ipAddr;
        for (int i = 3; i >= 0; i--) {
            bits[i] = (byte) ((a) & (0x000000ff));
            a = a >> 8;
        }
        return bits;

    }
    
    public static String toIpString(BigInteger ipNumber) throws UnknownHostException
    {
        if (ipNumber == null) return null;   
        return com.topsec.tsm.framework.util.IpAddressPool.getStringIp(ipNumber.longValue());
    }
}
