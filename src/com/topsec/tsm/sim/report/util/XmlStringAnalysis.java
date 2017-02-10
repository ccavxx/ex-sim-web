package com.topsec.tsm.sim.report.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

import com.topsec.tsm.sim.access.util.GlobalUtil;


//import com.common.object.SmsSendResponseObject;

/**
 * @ClassName: XmlStringAnalysis
 * @Declaration: ToDo
 * 
 * @author: WangZhiai create on2014年5月14日上午11:07:
 * @modify: </p>
 * @version:3.1 Copyright © TopSec
 */
public class XmlStringAnalysis {
	/**
	 * stringDocument 将字符串转为Document
	 * 
	 * @return
	 * @param s
	 *            xml格式的字符串
	 */
	public static Document stringDocument(String string) {
		Document doc = null;
		try {
			if (!GlobalUtil.isNullOrEmpty(string)) {
				doc = DocumentHelper.parseText(string);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return doc;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getMap(Document document) {
		Map<String, String>map=new HashMap<String, String>();
		Element root = document.getRootElement();
//		String rootnode=root.getName(); 
		List<Element> sElements=root.elements();
		
		if (sElements != null && sElements.size() > 0) {
			for (Element element : sElements) {
//				String firstnode = element.getName();
				List<Element> blElements=element.elements();
				if (blElements != null && blElements.size() > 0){
					for (Element elementbl : blElements) {
//						String secondnode = elementbl.getName();
						
						List<Element> itElements=elementbl.elements();
						
						if (itElements != null && itElements.size() > 0) {
							String keymail=elementbl.attributeValue("key");
							String valueMail=null;
							for (Element elementit : itElements){
//								String thirdnode = elementit.getName();
								String valueString=elementit.attributeValue("info");
								if (null==valueMail) {
									valueMail=valueString;
								}else{
									valueMail+="MAILadd"+valueString;
								}
							}
							map.put(keymail, valueMail);
						}else{
							String keyString=elementbl.attributeValue("key");
							String valueString=elementbl.attributeValue("value");
							map.put(keyString, valueString);
						}
					}
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> getTopoListMap(Document document) {
		List<Object>listObj=null;
		if (GlobalUtil.isNullOrEmpty(document)) {
			return listObj;
		}
		Element root = document.getRootElement();
		List<Element> sElements=root.elements();
		if (sElements != null && sElements.size() > 0) {
			listObj=new ArrayList<Object>();
			entryLevelListMap(sElements,listObj);
		}
		return listObj;
	}
	@SuppressWarnings("unchecked")
	private static List<Object> entryLevelListMap(List<Element> sElements,List<Object>listObj){
		if (sElements != null && sElements.size() > 0){
			Map<String, Object>map=null;
			for (Element element : sElements) {
				if (!GlobalUtil.isNullOrEmpty(element)) {
					map=new HashMap<String, Object>();
					List<DefaultAttribute> list=element.attributes();
					for (DefaultAttribute obj:list) {
						String keyString=obj.getName();
						String valueString=obj.getValue();
						map.put(keyString, valueString);
					}
					List<Element> itElements=element.elements();
					if (null!=itElements && itElements.size()>0) {
						List<Object> listChild=new ArrayList<Object>();
						entryLevelListMap(itElements,listChild);
						map.put("groupMembers",listChild);
					}
					if (!listObj.contains(map)) {
						listObj.add(map);
					}
				}
			}
		}
		return listObj;
	}

	public static void main(String[] args) {
		StringBuilder stringBuilder = new StringBuilder();//"<?xml version='1.0' encoding='UTF-8'?>"
//		stringBuilder.append("<config key='schedule_cfg_sysreport'><block key='reportinfo'><item key='report_sys' value='Esm/Topsec/SystemLog;;127.0.0.1;;60=:审计系统登录排行报表'/><item key='report_type' value='DAY'/><item key='report_topn' value='5'/><item key='report_filetype' value='pdf'/><item key='report_maillist' value=''><value info='wang_zhiai@topsec.com.cn'/><value info='liu_gengyang@topsec.com.cn'/></item><item key='report_user' value='王支爱'/></block></config>");
		stringBuilder.append("<graph>")
  .append("<node id='ZLxRu09zQv6OpoURFH5pxg' text='TAL test2' tooltip='TAL test2' type='asset' bounds='264.0,84.0,60.0,68.0' dropShadow='false' gid='ZLxRu09zQv6OpoURFH5pxg' gtext='TAL test2' icon='/img/icons/asset/os_48.png' ip='192.168.73.242' labelposition='bottom' mx_internal_uid='2CF99568-40CA-28AD-C3C7-6AC8481DD92F' showLabel='true'/>")
  .append("<node id='YiTCDIKET0aMgaOio53HnA' text='TAL test1' tooltip='TAL test1' type='asset' bounds='418.0,99.0,60.0,68.0' dropShadow='false' gid='YiTCDIKET0aMgaOio53HnA' gtext='TAL test1' icon='/img/icons/asset/os_48.png' ip='192.168.73.241' labelposition='bottom' mx_internal_uid='DD138508-9AF8-30A5-4A04-6AC8481DBEB7' showLabel='true'/>")
  .append("<node id='LX5A1f9OQ0iIzgF6hhafHA' text='宋卫利' tooltip='宋卫利' type='asset' bounds='518.0,100.0,40.0,68.0' dropShadow='false' gid='LX5A1f9OQ0iIzgF6hhafHA' gtext='宋卫利' icon='/img/icons/asset/os_48.png' ip='192.168.75.15' labelposition='bottom' mx_internal_uid='93CCF173-6C46-BCCE-B397-6AC8481D518F' showLabel='true'/>")
  .append("<node id='_M9rVmgJRQyx7olGQk1HIA' text='demo manager' tooltip='demo manager' type='asset' bounds='644.0,194.0,103.0,68.0' dropShadow='false' gid='_M9rVmgJRQyx7olGQk1HIA' gtext='demo manager' icon='/img/icons/asset/os_48.png' ip='192.168.79.203' labelposition='bottom' mx_internal_uid='C19DCC0C-C841-7196-8D46-6AC8481DF44A' showLabel='true'/>")
  .append("<node id='F6-_8ILyRW6yPeG_YDU8ig' text='TAL test3' tooltip='TAL test3' type='asset' bounds='744.0,194.0,60.0,68.0' dropShadow='false' gid='F6-_8ILyRW6yPeG_YDU8ig' gtext='TAL test3' icon='/img/icons/asset/os_48.png' ip='192.168.73.243' labelposition='bottom' mx_internal_uid='79277EAB-2404-9F2E-2F00-6AC8481D539B' showLabel='true'/>")
  .append("<node id='3gR0LzGXQYeBMp4-XQdevw' text='丁广富' tooltip='丁广富' type='asset' bounds='844.0,194.0,40.0,68.0' dropShadow='false' gid='3gR0LzGXQYeBMp4-XQdevw' gtext='丁广富' icon='/img/icons/asset/os_48.png' ip='192.168.75.5' labelposition='bottom' mx_internal_uid='499D8BE3-4B9F-E9A8-01BB-6AC8481D9CE9' showLabel='true'/>")
  .append("<node id='2vJJxGomSgeiRFHHtU8GvA' text='TEst Server' tooltip='TEst Server' type='asset' bounds='944.0,194.0,75.0,68.0' dropShadow='false' gid='2vJJxGomSgeiRFHHtU8GvA' gtext='TEst Server' icon='/img/icons/asset/os_48.png' ip='192.168.79.207' labelposition='bottom' mx_internal_uid='7F167419-F815-7875-7018-6AC8481DBB6D' showLabel='true'/>")
  .append("<node id='ZFX4yKpKQx-RkTz7ZVU3yQ' text='周小虎' tooltip='周小虎' type='asset' bounds='1044.0,194.0,40.0,68.0' dropShadow='false' gid='ZFX4yKpKQx-RkTz7ZVU3yQ' gtext='周小虎' icon='/img/icons/asset/os_48.png' ip='192.168.75.20' labelposition='bottom' mx_internal_uid='DCF0E7A6-8347-3130-A4EC-6AC8481DE593' showLabel='true'/>")
  .append("<node id='KqZ56lJmSBSPsCiwzEcINw' text='TA-EX服务器' tooltip='TA-EX服务器' type='asset' bounds='1144.0,194.0,79.0,68.0' dropShadow='false' gid='KqZ56lJmSBSPsCiwzEcINw' gtext='TA-EX服务器' icon='/img/icons/asset/os_48.png' ip='192.168.66.135' labelposition='bottom' mx_internal_uid='F573CA25-0BB4-C47A-436E-6AC8481D3A03' showLabel='true'/>")
  .append("<node id='gshmOjH4SNyaIajBY0sR-Q' text='TA Server' tooltip='TA Server' type='asset' bounds='1244.0,194.0,66.0,68.0' dropShadow='false' gid='gshmOjH4SNyaIajBY0sR-Q' gtext='TA Server' icon='/img/icons/asset/os_48.png' ip='192.168.66.134' labelposition='bottom' mx_internal_uid='B6377EC0-9807-B563-F24A-6AC8481D8740' showLabel='true'/>")
  .append("<node id='分组节点1' text='分组名称' tooltip='分组名称' type='分组' bounds='264.0,4.0,294.0,164.0' gid='分组节点1' groupedNode='true' gtext='分组名称'>")
  .append("  <property name='Grouped nodes count'>3</property>")
  .append("  <property name='Grouped nodes'>TAL test2, TAL test1, 宋卫利</property>")
  .append("  <node id='ZLxRu09zQv6OpoURFH5pxg' text='TAL test2' tooltip='TAL test2' type='asset' bounds='264.0,84.0,60.0,68.0' dropShadow='false' gid='ZLxRu09zQv6OpoURFH5pxg' gtext='TAL test2' icon='/img/icons/asset/os_48.png' ip='192.168.73.242' labelposition='bottom' mx_internal_uid='2CF99568-40CA-28AD-C3C7-6AC8481DD92F' showLabel='true' originalPosition='264,84'/>")
  .append("  <node id='YiTCDIKET0aMgaOio53HnA' text='TAL test1' tooltip='TAL test1' type='asset' bounds='418.0,99.0,60.0,68.0' dropShadow='false' gid='YiTCDIKET0aMgaOio53HnA' gtext='TAL test1' icon='/img/icons/asset/os_48.png' ip='192.168.73.241' labelposition='bottom' mx_internal_uid='DD138508-9AF8-30A5-4A04-6AC8481DBEB7' showLabel='true' originalPosition='418,99'/>")
  .append("  <node id='LX5A1f9OQ0iIzgF6hhafHA' text='宋卫利' tooltip='宋卫利' type='asset' bounds='518.0,100.0,40.0,68.0' dropShadow='false' gid='LX5A1f9OQ0iIzgF6hhafHA' gtext='宋卫利' icon='/img/icons/asset/os_48.png' ip='192.168.75.15' labelposition='bottom' mx_internal_uid='93CCF173-6C46-BCCE-B397-6AC8481D518F' showLabel='true' originalPosition='518,100'/>")
  .append("  <arc id='689554B8-6AC8-07AD-4197-BE486950C7D3' type='实线' tooltip='TAL test2 - 实线 - TAL test1' source='ZLxRu09zQv6OpoURFH5pxg' destination='YiTCDIKET0aMgaOio53HnA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("  <arc id='68AB4031-6AC8-FEBF-41C3-B866841D4A76' type='实线' tooltip='TAL test1 - 实线 - 宋卫利' source='YiTCDIKET0aMgaOio53HnA' destination='LX5A1f9OQ0iIzgF6hhafHA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("  <arc id='68AFFE6A-6AC8-02AD-41D0-29BAC12A2EAD' type='实线' tooltip='宋卫利 - 实线 - demo manager' source='LX5A1f9OQ0iIzgF6hhafHA' destination='_M9rVmgJRQyx7olGQk1HIA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("</node>")
  .append("<node id='68AE0604-6AC8-0265-41CF-D88F909B55BF' text='cloud' tooltip='cloud' type='image' bounds='269.0,277.0,179.0,101.0' dropShadow='false' gid='68AE0604-6AC8-0265-41CF-D88F909B55BF' gtext='cloud' labelposition='bottom' showLabel='false' source='img/topo/cloud.gif'/>")
  .append("<node id='68A3E17E-6AC8-7EA5-4190-D3B0A62E0125' text='IBM-Server' tooltip='IBM-Server' type='image' bounds='462.0,370.0,64.0,64.0' dropShadow='false' gid='68A3E17E-6AC8-7EA5-4190-D3B0A62E0125' gtext='IBM-Server' labelposition='bottom' showLabel='false' source='img/topo/IBM-Server.png'/>")
  .append("<arc id='68AB4031-6AC8-FEBF-41C3-B866841D4A76' type='实线' tooltip='TAL test1 - 实线 - 宋卫利' source='YiTCDIKET0aMgaOio53HnA' destination='LX5A1f9OQ0iIzgF6hhafHA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("<arc id='68A2CBD6-6AC8-01E5-41C2-82B255B9F6C8' type='实线' tooltip='cloud - 实线 - demo manager' source='68AE0604-6AC8-0265-41CF-D88F909B55BF' destination='_M9rVmgJRQyx7olGQk1HIA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("<arc id='68AFFE6A-6AC8-02AD-41D0-29BAC12A2EAD' type='实线' tooltip='宋卫利 - 实线 - demo manager' source='LX5A1f9OQ0iIzgF6hhafHA' destination='_M9rVmgJRQyx7olGQk1HIA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("<arc id='689554B8-6AC8-07AD-4197-BE486950C7D3' type='实线' tooltip='TAL test2 - 实线 - TAL test1' source='ZLxRu09zQv6OpoURFH5pxg' destination='YiTCDIKET0aMgaOio53HnA' directed='true' weight='2' lineType='straightline' layout='default'/>")
  .append("</graph>");
		String string = stringBuilder.toString();
		Document document = stringDocument(string);
		getTopoListMap(document);
	}

}
