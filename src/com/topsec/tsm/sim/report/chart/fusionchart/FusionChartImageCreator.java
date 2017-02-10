package com.topsec.tsm.sim.report.chart.fusionchart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tsm.common.CallableCallback;
import com.topsec.tsm.sim.report.component.ChartImageCreator;
import com.topsec.tsm.sim.report.util.ChartTypeUtil;

/**
 * FunsionChart图片生成类
 * @author hp
 *
 */
public class FusionChartImageCreator extends ChartImageCreator{
	
	private String host = "localhost" ;
	private int port = 30763 ;
	private String chartData ;
	/**
	 * 使用默认的主机localhost,与默认的端口号30763连接服务器
	 */
	public FusionChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,String chartData,CallableCallback<String> callback) {
		super(chartType,imageType,chartWidth,chartHeight,callback) ;
		this.chartData = chartData ;
	}
	/**
	 * 使用socket与生成图片的服务器建立连接
	 * @param host　服务器地址
	 * @param port　服务器监听端口
	 */
	public FusionChartImageCreator(Integer chartType,String imageType,int chartWidth, int chartHeight,String chartData,CallableCallback<String> callback,String host, int port) {
		super(chartType,imageType,chartWidth,chartHeight,callback) ;
		this.chartData = chartData ;
		this.host = host;
		this.port = port;
	}

	public String generateChartImage(){
		OutputStream os = null;//服务器端输出流
		InputStream is = null ;//服务器输入流
		FileOutputStream imageFileOutput = null;//生成的图片输出流
		try {
			Socket client = new Socket() ;
			client.connect(new InetSocketAddress(host, port),10000) ;
			os = client.getOutputStream() ;
			is = client.getInputStream() ;
			//如果服务器已经准备好接受数据
			int count = 30 ;//循环的次数
			//不断的循环判断服务器是否返回数据,如果服务器没有返回数据,睡眠100ms再次进行判断,直至循环完毕或者服务器返回数据
			while(is.available()<1&&(count--)>0){
				try {
					Thread.sleep(100) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//如果服务器已经就绪
			if(is.available()>0&&is.read()==1){
				sendChartData(os, configChartData(chartData,ChartTypeUtil.getFCChartType(chartType), imageType, chartWidth, chartHeight).toString()) ;
				File f = createTempImageFile(imageType) ;
				imageFileOutput = new FileOutputStream(f) ;
				receiveImageData(is, imageFileOutput) ;
				return f.getAbsolutePath() ;
			}else{
				throw new IOException("服务器器没有响应!") ;
			}
		}catch (SocketTimeoutException e) {
			System.out.println("连接服务器超时!");
			return null;
		}catch (IOException e) {
			System.out.println("服务器访问失败:"+e.getMessage());
			return null ;
		}finally{
			ObjectUtils.close(imageFileOutput) ;
			ObjectUtils.close(os) ;
			ObjectUtils.close(is) ;
		}
	}
	private JSONObject configChartData(String chartData,String chartType,String imageType,int chartWidth,int chartHeight){
		JSONObject result = new JSONObject() ;
		result.put("chartType", chartType) ;
		result.put("imageType", imageType) ;
		result.put("chartData", chartData) ;
		result.put("chartWidth", chartWidth) ;
		result.put("chartHeight", chartHeight) ;
		result.put("chartDataType", "xml") ;
		return result ;
	}
	/**
	 * 发送生成图片所需要的信息
	 * @param os
	 * @param chartData
	 * @throws IOException
	 */
	private void sendChartData(OutputStream os,String chartData) throws IOException{
		DataOutputStream dos = new DataOutputStream(os) ;
		BufferedOutputStream bos = new BufferedOutputStream(os) ;
		byte[] utf8Data = chartData.getBytes("utf8") ;
		dos.writeInt(utf8Data.length) ;//先输出4个字节的数据长度，然后输出数据,服务器端也必须先读取数据长度，然后根据数据长度读取数据
		bos.write(utf8Data) ;
		bos.flush() ;
	}
	/**
	 * 接收生成的图片数据
	 * @param is
	 * @param fileOutputStream
	 * @throws IOException
	 */
	private void receiveImageData(InputStream is,FileOutputStream fileOutputStream) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream) ;
		DataInputStream dis = new DataInputStream(is) ;
		int dataLength = dis.readInt() ;//先读取数据长度
		byte[] buffer = new byte[8192] ;
		BufferedInputStream bis = new BufferedInputStream(is) ;
		int readBytes ;
		while(dataLength>0){
			readBytes = bis.read(buffer) ;
			bos.write(buffer, 0, readBytes) ;
			dataLength -= readBytes ;
		}
		bos.flush() ;
	}
	
}
