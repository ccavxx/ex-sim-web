package com.topsec.tsm.ui.framework.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomUtil {
	private static DomUtil du;
	private static Logger log = LoggerFactory.getLogger(DomUtil.class);

	public static DomUtil getInstance() {
		if (du == null)
			du = new DomUtil();
		return du;
	}

	/**
	 * 在规定的PATH下读取 XML文档，如果PATH 不存在，会先创建这个路径和文件 默认编码是UTF-8
	 * 
	 * @param path
	 * @return Document
	 * @throws IOException,DocumentException
	 */
	public Document readXML(String path) throws IOException, DocumentException {
		if (path == null || "".equals(path))
			return null;
		//log.info("正在读取：[" + path + "]" + "\ntarget:[" + path + "]");
		File file = new File(path);
		if (!file.exists()) {
			createNewFile(file);
			createXMLStyle(file);
		}
		// 读
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		Document document = saxReader.read(new File(path));
		return document;
	}
	
	/**
	 * 在规定的PATH下读取 XML文档，如果PATH 不存在，会先创建这个路径和文件 默认编码是UTF-8
	 * 文件内容是根节点为root的XML文档
	 * @param path
	 * @return Document 根节点为root空文档。
	 * @throws Exception
	 */
	public Document readXMLFile(String path,boolean createFile) throws Exception {
		if (path == null || "".equals(path))
			return null;
		log.info("正在读取：[" + path + "]" + "\ntarget:[" + path + "]");
		File file = new File(path);
		if (!file.exists()) {
			if(createFile)createDefaultXMLFile(file);
		}
		// 读
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		Document document = saxReader.read(new File(path));
		return document;
	}
	
	private Document createDefaultXMLFile(File file)throws Exception{
		if(file==null)return null;
		createNewDir(file);
		Document doc=DocumentHelper.createDocument();
		doc.addElement("root");
		saveXML(doc,file.getAbsolutePath());
		return doc;
	}

	/**
	 * 在规定的PATH下读取 XML文档，如果PATH 不存在，会先创建这个路径和但不创建文件 默认编码是UTF-8
	 * 
	 * @param path
	 * @return Document
	 * @throws IOException,DocumentException
	 */
	public Document readXML2(String path) throws IOException, DocumentException {
		if (path == null || "".equals(path))
			return null;
		//log.info("正在读取：[" + path + "]" + "\ntarget:[" + path + "]");
		File file = new File(path);
		if (!file.exists()) {
			createNewDir(file);
		}
		// 读
		SAXReader saxReader = new SAXReader();
		saxReader.setEncoding("UTF-8");
		Document document = saxReader.read(new File(path));
		return document;
	}

	public static Document parse2Dom4jDom(org.w3c.dom.Document doc)
			throws Exception {
		if (doc == null) {
			return (null);
		}
		org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();
		return (xmlReader.read(doc));
	}

	public static org.w3c.dom.Document parse2W3cDom(Document doc) throws Exception {
		if (doc == null) {
			return (null);
		}
		java.io.StringReader reader = new java.io.StringReader(doc.asXML());
		org.xml.sax.InputSource source = new org.xml.sax.InputSource(reader);
		javax.xml.parsers.DocumentBuilderFactory documentBuilderFactory = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();
		javax.xml.parsers.DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		return (documentBuilder.parse(source));
	}

	public void outputXML(Document document, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/xml;charset=UTF-8");
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		// response.setCharacterEncoding("UTF-8");
		PrintWriter pw = response.getWriter();
		XMLWriter writer = new XMLWriter(pw, format);
		writer.write(document);
		writer.flush();
		writer.close();
	}
	
	public void outputString(String str, HttpServletResponse response)throws Exception {
//		response.setContentType("application/text;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write(str);
		writer.flush();
		writer.close();
	}

	/**
	 * 在XML文档中搜索 节点属性 id 为确定值的节点
	 * 
	 * @param doc
	 * @param id
	 * @return Element
	 */
	public static Element findElement(Document doc, String id) {
		if (doc != null && id != null) {
			Element root = doc.getRootElement();// 得到根节点
			Element eml = null;
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if (element.attribute("id") == null)
					continue;
				if (element.attribute("id").getValue().equals(id)) {
					eml = element;
					break;
				}
			}
			return eml;
		} else
			return null;
	}

	/**
	 * 保存XML文档，默认编码 UTF-8
	 * 
	 * @param document
	 * @param path
	 * @throws IOException
	 */
	public void saveXML(Document document, String path) throws IOException {
		//log.info("正在保存：[" + path + "]");
		File file = new File(path);
		OutputStream os = new FileOutputStream(file);
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(os, format);
		writer.write(document);
		writer.close();

		// format.setEncoding("UTF-8");
		// Thread.sleep(3000);
		// File file=new File(path);
		// //if(file.exists())file.delete();
		// OutputStream os=new FileOutputStream(file);
		// OutputStreamWriter fos=new OutputStreamWriter(os,"UTF-8");
		// fos.write(document.asXML());
		// fos.flush();
		// fos.close();
	}

	/**
	 * 创建结构良好的XML空文件，有header 和root根节点。
	 * 
	 * @param File
	 */
	public void createXMLStyle(File file) throws IOException {
		//log.info("正在创建：[" + file.getAbsolutePath() + "]");
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		if ("columnConfig.xml".equals(file.getName())) {
			// 创建default 节点
			Element user = root.addElement("user");
			user.addAttribute("id", "default");
			Element column = user.addElement("column");
			column.addAttribute("id", "PRIORITY");
			column.addAttribute("name", "优先级");
			Element column1 = user.addElement("column");
			column1.addAttribute("id", "NAME");
			column1.addAttribute("name", "事件名称");
			Element column2 = user.addElement("column");
			column2.addAttribute("id", "EVENT_TYPE");
			column2.addAttribute("name", "事件类型");
			Element column3 = user.addElement("column");
			column3.addAttribute("id", "SRC_ADDRESS");
			column3.addAttribute("name", "源地址");
			Element column4 = user.addElement("column");
			column4.addAttribute("id", "SRC_PORT");
			column4.addAttribute("name", "源端口");
			Element column5 = user.addElement("column");
			column5.addAttribute("id", "DVC_ADDRESS");
			column5.addAttribute("name", "设备地址");
			Element column6 = user.addElement("column");
			column6.addAttribute("id", "DEST_ADDRESS");
			column6.addAttribute("name", "目的地址");
			Element column7 = user.addElement("column");
			column7.addAttribute("id", "DEST_PORT");
			column7.addAttribute("name", "目的端口");
			Element column10 = user.addElement("column");
			column10.addAttribute("id", "START_TIME");
			column10.addAttribute("name", "开始时间");
			Element column8 = user.addElement("column");
			column8.addAttribute("id", "END_TIME");
			column8.addAttribute("name", "结束时间");
			Element column9 = user.addElement("column");
			column9.addAttribute("id", "MESSAGE");
			column9.addAttribute("name", "事件详细信息");

		}
		saveXML(doc, file.getAbsolutePath());
	}

	/**
	 * 在目标节点中插入一个新的节点对象
	 */
	public static void addNewElement(Element target, Element source) {
		if (target != null && source != null)
			target.add(source);
		else
			log.debug("addNewElement(" + target + "," + source + ")");
	}

	// 当配置文件还不存在的时候，创建它
	private static File createNewFile(File file) throws IOException {
		if (file != null) {
			log.debug("asdf=" + file.getPath());
			String path = file.getPath().substring(0,
					file.getPath().lastIndexOf("\\"));
			new File(path).mkdirs();
			file.createNewFile();
		}
		return file;
	}

	// 当配置文件还不存在的时候，创建它
	private static File createNewDir(File file) throws IOException {
		if (file != null) {
			log.debug("asdf=" + file.getPath());
			String path = file.getPath().substring(0,
					file.getPath().lastIndexOf("\\"));
			new File(path).mkdirs();

		}
		return file;
	}

}
