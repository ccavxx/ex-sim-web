package com.topsec.tsm.sim.report.util;

/*
 *	JasperClasspath	加载Jasper所需包
 */
import net.sf.jasperreports.engine.util.JRProperties;

public class JasperClasspath {
	/**
	 * 编译报表模板时所需要的jar
	 */
	// public static final String JASPER_CLASSPATH =
	// "../../../../common_lib/jasperreports-1.1.0.jar;../../../../common_lib/commons-logging-api-1.0.2.jar;../../../../common_lib/itext-1.3.1.jar;../../../../common_lib/iTextAsian.jar;../../../../common_lib/jfreechart-1.0.0-rc1.jar;../../../../common_lib/jcommon-1.0.0-rc1.jar;../../../../common_lib/topsec-util.jar;../../../../common_lib/topsec-dbaccess.jar";
	public static String getAllJar() {
		StringBuffer jasperSb = new StringBuffer();
		// jar   //jasperreports-3.0.0-javaflow.jar,commons-logging-api-1.0.2.jar
		String jasperJars = "iTextAsian.jar,iText-2.1.7.jar,jasperreports-4.0.2.jar,jasperreports-applet-4.0.2.jar,commons-beanutils-1.8.4.jar,commons-collections-3.2.1.jar,commons-digester-1.7.jar,commons-javaflow-20060411.jar,commons-logging.jar,jasperreports-fonts-4.0.2.jar";
		String rootJar = ReportUiUtil.getSystemLibPath();
		for (String jasperJar : jasperJars.split(","))
			jasperSb.append(rootJar + jasperJar + ";");

		// font
		// String fonts =
		// "stfangso.ttf,stzhongs.ttf,stsong.ttf,simhei.ttf,simsun.ttc";
		String rootFonts = ReportUiUtil.getSysPath();
		rootFonts += "ireport/fonts";
		jasperSb.append(";" + rootFonts + ";.;,;");

		// for (String font : fonts.split(","))
		// jasperSb.append(rootFonts + font + ";");

		return jasperSb.toString();
	}

	public static void setJasperClasspath() {
		String classpath = System.getProperty("java.class.path");
		classpath = (classpath == null || classpath.equals("")) ? "."
				: classpath;
		if (classpath.indexOf("jasperreports") == -1
				|| classpath.indexOf("topsec-dbaccess") == -1) {

			System.setProperty("java.class.path", getAllJar() + ";"
							+ classpath);
			JRProperties.setProperty(JRProperties.COMPILER_CLASSPATH,
					getAllJar() + ";" + classpath);
		}
		String jrClassPath = JRProperties
				.getProperty(JRProperties.COMPILER_CLASSPATH);
		if (jrClassPath == null || jrClassPath.equals("")
				|| jrClassPath.indexOf("jasperreports") == -1) {

			JRProperties.setProperty(JRProperties.COMPILER_CLASSPATH,
					getAllJar() + ";" + classpath);
		}
	}

	public static String getJasperClasspath() {
		String info = "";
		info += "\r\n[System-Classpath]:"
				+ System.getProperty("java.class.path") + "\r\n";
		info += "[JRProperties-Classpath]:"
				+ JRProperties.getProperty(JRProperties.COMPILER_CLASSPATH)
				+ "\r\n";
		return info;
	}
}