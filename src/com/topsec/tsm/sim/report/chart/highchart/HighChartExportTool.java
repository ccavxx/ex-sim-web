package com.topsec.tsm.sim.report.chart.highchart;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

public class HighChartExportTool {
	
	private static final AtomicLong fileCounter = new AtomicLong(0);
	private static final String PLANTOMJS_PATH = new File("phantomjs").getAbsolutePath() ;
	
	public static final String RHINO_EXPORT = "rhino" ;
	public static final String PLANTOMJS_EXPORT = "plantomjs" ;
	
	public static String exportUseRhino(String jsonData){
		String imagePath = CreateChartFactory.getInstance().createChartPicuture(jsonData) ;
		return imagePath ;
	}
	
	public static String exportUsePhantomjs(String jsonData)throws Exception{
		String jsFileName = "/tmp/"+fileCounter.addAndGet(1)+".js" ;
		File jsFile = new File(PLANTOMJS_PATH+jsFileName) ;
		FileUtils.writeStringToFile(jsFile, jsonData,"UTF-8") ;
		//InputStream is = null ;
		//BufferedReader br = null ;
		try {
			String outputFileName = PLANTOMJS_PATH+"/tmp/"+fileCounter.addAndGet(1) +".png" ;
			String fileName = SystemUtils.IS_OS_WINDOWS ? "phantomjs.exe" : "phantomjs" ; 
			String command = PLANTOMJS_PATH+"/" + fileName +
							 " phantomjs/highcharts-convert.js" +
					         " -infile " + jsFile.getAbsolutePath() + 
					         " -outfile " + outputFileName;
			Process pro = Runtime.getRuntime().exec(command) ;
			pro.waitFor() ;
			//is = pro.getInputStream() ;
			//br = new BufferedReader(new InputStreamReader(is)) ;
			//String line ;
			//while((line = br.readLine()) != null){
			//	System.out.println(line);
			//}
			return outputFileName ;
		}finally{
			//ObjectUtils.close(is) ;
			//ObjectUtils.close(br) ;
			if (jsFile != null && jsFile.exists()) {
				FileUtils.forceDelete(jsFile) ;
			}
		}
	}
}
