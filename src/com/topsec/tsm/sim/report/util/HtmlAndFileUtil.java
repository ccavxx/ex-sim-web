package com.topsec.tsm.sim.report.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import com.topsec.tsm.sim.access.util.GlobalUtil;
import com.topsec.tsm.sim.report.bean.struct.ExpDateStruct;

/**
 * @ClassName: HtmlAndFileUtil
 * @Declaration: TODo
 * 
 * @author: WangZhiai create on2014年9月4日下午6:54:21
 * @modify: </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class HtmlAndFileUtil {
	public static final String Rn = "\r\n";

	/**
	 * 删除某个文件夹下的所有文件夹和文件
	 * 
	 * @param delpath
	 *            String
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return boolean
	 */
	public static void deleteFileAndPath(String delpath) throws Exception {
		File filepath = new File(delpath);
		try {
			FileUtils.deleteDirectory(filepath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出某个文件夹下的所有文件夹和文件路径
	 * 
	 * @param delpath
	 *            String
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return boolean
	 */
	public static boolean readfile(String filepath)
			throws FileNotFoundException, IOException {
		try {

			File file = new File(filepath);
			System.out.println("遍历的路径为：" + file.getAbsolutePath());
			// 当且仅当此抽象路径名表示的文件存在且 是一个目录时（即文件夹下有子文件时），返回 true
			if (!file.isDirectory()) {
				System.out.println("该文件的绝对路径：" + file.getAbsolutePath());
				System.out.println("名称：" + file.getName());
			} else if (file.isDirectory()) {
				// 得到目录中的文件和目录
				String[] filelist = file.list();
				if (filelist.length == 0) {
					System.out.println(file.getAbsolutePath()
							+ "文件夹下，没有子文件夹或文件");
				} else {
					System.out
							.println(file.getAbsolutePath() + "文件夹下，有子文件夹或文件");
				}
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					System.out.println("遍历的路径为：" + readfile.getAbsolutePath());
					if (!readfile.isDirectory()) {
						System.out.println("该文件的路径："
								+ readfile.getAbsolutePath());
						System.out.println("名称：" + readfile.getName());
					} else if (readfile.isDirectory()) {
						System.out.println("-----------递归循环-----------");
						readfile(filepath + "\\" + filelist[i]);
					}
				}

			}

		} catch (FileNotFoundException e) {
			System.out.println("readfile() Exception:" + e.getMessage());
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			// readfile("D:/file");
			// deleteFileAndPath("D:/file/");
			// createPath("D:/file/haha/xixi/huhu.html");
			// createFile("D:/file/haha/xixi/huhu.html/hahu.html");
			clearPath("D:/file/");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("ok");
	}

	/**
	 * 此方法只会调用一次
	 * 
	 * @param html
	 */
	private static void htmlStart(StringBuffer html) {
		if (null==html) {
			html = new StringBuffer();
		}
		html.append(
				"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>")
				.append(Rn).append("<html lang='zh-CN'>").append(Rn);
	}

	/**
	 * 此方法只会调用一次
	 * 
	 * @param html
	 * @param map
	 */
	private static void initHtmlHead(StringBuffer html) {
		htmlStart(html);
		html.append("<head>")
				.append(Rn)
				.append("<title>天融信html导出</title>")
				.append(Rn)
				.append("<meta http-equiv='Content-Type' content='text/html'; charset='UTF-8'>")
				.append(Rn);
		html.append("<style type='text/css'>").append(Rn)
				.append(".rowTable td{").append(Rn)
				.append("	vertical-align:top;").append(Rn)
				.append("	margin: 5px;").append(Rn).append("	padding:5px;")
				.append(Rn).append("}").append(Rn).append(".rowTable{")
				.append(Rn).append("	margin-top: 120px;").append(Rn)
				.append("}").append(Rn).append(".report-table{").append(Rn)
				.append("	align: 'center';").append(Rn)
				.append("	border-right:1px solid #6699FF;").append(Rn)
				.append("	border-bottom:1px solid #6699FF;").append(Rn)
				.append("	width:640px;").append(Rn).append("	margin-top: 10px;")
				.append(Rn).append("	margin-left: 1px;").append(Rn).append("}")
				.append(Rn).append(".report-table td,th{")
				.append(Rn).append("	border-left:1px solid #6699FF;").append(Rn)
				.append(Rn).append("	border-top:1px solid #6699FF;").append(Rn)
				.append("	height:30px;").append(Rn).append("	cellspacing:0;")
				.append(Rn).append("	cellpadding:0;").append(Rn).append("}")
				.append(Rn).append(".rows{").append(Rn)
				.append("	margin-left: 20px;").append(Rn)
				.append("	padding-top: 5px;").append(Rn).append("}").append(Rn)
				.append(".rows td{").append(Rn)
				.append("	font-size: 20px;").append(Rn).append("	margin: 5px;")
				.append(Rn).append("	padding:5px;").append(Rn).append("}")
				.append(Rn).append(".divwel{").append(Rn)
				.append("	margin-top: 30px;").append(Rn).append("}").append(Rn)
				.append("</style>").append(Rn).append("</head>").append(Rn);
	}

	/**
	 * 此方法只会调用一次
	 * 
	 * @param html
	 * @param map
	 */
	public static void initHtmlBodyStaticContent(StringBuffer html, Map map) {
		initHtmlHead(html);
		html.append("<body style='background-color: #CCE8CF;'>")
				.append("	<div class='rows'>").append("		<p>")
				.append("			<h1 align='center'>").append(map.get("rptname"))
				.append("</h1>").append("		</p>");
		html.append("<div style='margin-top: 30px;padding-top: 50px;'>")
				.append("			<table align='center'>")
				.append("				<tr>")
				.append("					<td>")
				.append(map.get("coverType"))
				.append("</td>")
				.append("					<td><b>")
				.append(map.get("rpttype"))
				.append("</b></td>")
				.append("				</tr>")
				.append("				<tr>")
				.append("					<td>")
				.append(map.get("coverTopn"))
				.append("</td>")
				.append("					<td><b>")
				.append(map.get("dataTopn"))
				.append("</b></td>")
				.append("				</tr>")
				.append("				<tr>")
				.append("					<td>")
				.append(map.get("coverAuthor"))
				.append("</td>")
				.append("					<td><b>")
				.append(map.get("cuser"))
				.append("</b></td>")
				.append("				</tr>")
				.append("				<tr>")
				.append("					<td>")
				.append(map.get("coverCreateTime"))
				.append("</td>")
				.append("					<td><b>")
				.append(map.get("ctime"))
				.append("</b></td>")
				.append("				</tr>")
				.append("				<tr>")
				.append("					<td>")
				.append(map.get("coverLogTime"))
				.append("</td>")
				.append("					<td><b>")
				.append(map.get("clogtime"))
				.append("</b></td>")
				.append("				</tr>")
				.append("			</table>")
				.append("		</div>")
				.append("<table class='rowTable' align='center' border='0' cellspacing='0' cellpadding='0'>");
	}

	/**
	 * 此方法动态生成图和表格的内容会被调用多次
	 * 
	 * @param html
	 * @param map
	 */
	public static void initHtmlBodyDymic(StringBuffer html, String fliePath,
			ExpDateStruct exp, Map map, int st, int en) {
		if (GlobalUtil.isNullOrEmpty(map)) {
			return;
		}

		List<Map> resultTable = exp.getTable();
		String imgName = (String) map.get("chart");
		String toPath = fliePath + "image/";
		boolean isInsertPic=false;
		try {
			copyPictureToImagePath(imgName, toPath);
			isInsertPic=true;
		} catch (Exception e) {
			isInsertPic=false;
		}
		String[] tempImgNameStrings = imgName.split("/");
		imgName = tempImgNameStrings[tempImgNameStrings.length - 1];
		html.append("<tr>").append(Rn)
				.append("				<td style='padding-bottom:5px;'>").append(Rn)
				.append("					<div>").append(Rn);
		html.append("						<div>").append(Rn).append("							<h3>")
				.append(map.get("title")).append("</h3>").append(Rn)
				.append("						</div>").append(Rn).append("						<div>")
				.append(Rn);
		if (isInsertPic) {
			html.append("							<img title='").append(map.get("numT")).append("'")
				.append(" src='image/").append(imgName).append("'")
				.append(" >").append(Rn);
		}
		html.append("						</div>").append(Rn)
				.append("<div>")
				.append(Rn)
				.append("					<table class = 'report-table' cellpadding = '0' cellspacing = '0'>")
				.append(Rn).append("						<thead class = 'fixedHeader' >")
				.append(Rn)
				.append("							<tr align = 'center' class = 'tableHead'>")
				.append(Rn);
		for (int i = 0; i < st; i++) {
			for (int j = 0; j < en; j++) {
				String keyString = "tablep" + i + j;
				if (map.containsKey(keyString)) {
					html.append("<th>").append(map.get(keyString))
							.append("</th>").append(Rn);
				}
			}
		}
		html.append("					</tr >").append(Rn).append("						</thead>")
				.append(Rn).append("						<tbody>").append(Rn);
		String[] tableTdv = exp.getSubTableFile().split(",");
		if (!GlobalUtil.isNullOrEmpty(tableTdv)&&!GlobalUtil.isNullOrEmpty(resultTable)) {
			for (Map<?, ?> maptable : resultTable) {
				html.append("							<tr>").append(Rn);
				for (int i = 0; i < tableTdv.length; i++) {
					html.append("<td> ").append(maptable.get(tableTdv[i]))
							.append("</td>").append(Rn);
				}
				html.append("				</tr>").append(Rn);
			}
		}
		html.append("						</tbody>").append(Rn).append("					</table>")
				.append(Rn).append("				</div>").append(Rn).append("			</div>")
				.append(Rn).append("		</td>").append(Rn).append("	</tr>")
				.append(Rn);
	}

	/**
	 * 此方法只会调用一次
	 * 
	 * @param html
	 */
	public static void initHtmlEnd(StringBuffer html) {
		html.append("</table>").append(Rn).append("	</div>").append(Rn)
				.append("  </body>").append(Rn).append("</html>").append(Rn);
	}

	public static void createPath(String path) {
		File file = new File(path);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		file.mkdirs();
	}

	public static File createFile(String filePathAndName) {
		File file = new File(filePathAndName);
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try {
			file.createNewFile();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void copyPictureToImagePath(String fromPathAndName,
			String toPath) throws IOException{
		File srcFile = new File(fromPathAndName);
		File destDir = new File(toPath);
		FileUtils.copyFileToDirectory(srcFile, destDir);
	}

	public static void copyfromPathToPath(String fromPath, String toPath) {
		File from = new File(fromPath);
		File to = new File(toPath);
		try {
			FileUtils.copyDirectory(from, to);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearPath(String path) {
		File file = new File(path);
		try {
			FileUtils.cleanDirectory(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeContent(File file, Object content) {
		BufferedWriter writer = null;
		if (GlobalUtil.isNullOrEmpty(content)) {
			return;
		}
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file,true),"UTF-8") ;
			writer = new BufferedWriter(osw);
			writer.write((String) content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean compressFloderChangeToZip(String compressedFilePath,
			String zipFileRootPath, String zipFileName) throws IOException {
		File compressedFile = new File(compressedFilePath);
		ZipOutputStream zipOutputStream = new ZipOutputStream(
				new FileOutputStream(zipFileRootPath + zipFileName));
		String base = "";
		boolean result = HtmlAndFileUtil.compressFloderChangeToZip(
				compressedFile, zipOutputStream, base);
		zipOutputStream.close();
		return result;
	}

	private static boolean compressFloderChangeToZip(File compressedFile,
			ZipOutputStream zipOutputStream, String base) throws IOException {
		FileInputStream fileInputStream = null;
		try {
			if (compressedFile.isDirectory()) {
				File[] childrenCompressedFileList = compressedFile.listFiles();
				base = base.length() == 0 ? "" : base + File.separator;
				for (int i = 0; i < childrenCompressedFileList.length; i++) {
					HtmlAndFileUtil.compressFloderChangeToZip(
							childrenCompressedFileList[i], zipOutputStream,
							base + childrenCompressedFileList[i].getName());
				}
			} else {
				if ("".equalsIgnoreCase(base)) {
					base = compressedFile.getName();
				}
				zipOutputStream.putNextEntry(new ZipEntry(base));
				fileInputStream = new FileInputStream(compressedFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = fileInputStream.read(buf)) != -1) {
					zipOutputStream.write(buf, 0, len);
				}
				fileInputStream.close();
			}
			return true;
		} catch (Exception e) {
			e.getStackTrace();
			return false;
		}
	}

	/*
	 * @param:zipFilePath String,releasePath String
	 * 
	 * @return void
	 * 
	 * @description:Decompress A File
	 */
	@SuppressWarnings("unchecked")
	public static void decompressFile(String zipFilePath, String releasePath)
			throws IOException {
		ZipFile zipFile = new ZipFile(zipFilePath);
		Enumeration<ZipEntry> enumeration = zipFile.getEntries();
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		ZipEntry zipEntry = null;
		String zipEntryNameStr = "";
		String[] zipEntryNameArray = null;
		while (enumeration.hasMoreElements()) {
			zipEntry = enumeration.nextElement();
			zipEntryNameStr = zipEntry.getName();
			zipEntryNameArray = zipEntryNameStr.split("/");
			String path = releasePath;
			File root = new File(releasePath);
			if (!root.exists()) {
				root.mkdir();
			}
			for (int i = 0; i < zipEntryNameArray.length; i++) {
				if (i < zipEntryNameArray.length - 1) {
					path = path + File.separator + zipEntryNameArray[i];
					new File(path).mkdir();
				} else {
					if (zipEntryNameStr.endsWith(File.separator)) {
						new File(releasePath + zipEntryNameStr).mkdir();
					} else {
						inputStream = zipFile.getInputStream(zipEntry);
						fileOutputStream = new FileOutputStream(new File(
								releasePath + zipEntryNameStr));
						byte[] buf = new byte[1024];
						int len;
						while ((len = inputStream.read(buf)) > 0) {
							fileOutputStream.write(buf, 0, len);
						}
						inputStream.close();
						fileOutputStream.close();
					}
				}
			}
		}
		zipFile.close();
	}
	public static void outzipFile(String zipFilePath, OutputStream outStream)
			throws IOException {
		File file = new File(zipFilePath);
		InputStream inputStream = null;
		inputStream =  new FileInputStream(file);
		byte[] buf = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) !=-1) {
			outStream.write(buf, 0, len);
		}
		outStream.flush();
		inputStream.close();
	}
}
