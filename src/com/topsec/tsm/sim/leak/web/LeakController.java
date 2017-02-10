package com.topsec.tsm.sim.leak.web;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.ass.PageBean;
import com.topsec.tsm.sim.kb.CpeBean;
import com.topsec.tsm.sim.kb.Leak;
import com.topsec.tsm.sim.leak.service.CpeService;
import com.topsec.tsm.sim.leak.service.LeakService;
import com.topsec.tsm.sim.util.FastJsonUtil;
import com.topsec.tsm.sim.util.JSONConverterCallBack;

@Controller
@RequestMapping("leak")
public class LeakController {
	@Autowired
	private LeakService leakService;
	@Autowired
	private CpeService cpeService;
	
	public void setLeakService(LeakService leakService) {
		this.leakService = leakService;
	}
	
	public void setCpeService(CpeService cpeService) {
		this.cpeService = cpeService;
	}
	
	@RequestMapping("importXMLFile")
	@ResponseBody
	public Object importXMLFile(HttpServletRequest request, HttpServletResponse response){
		JSONObject result = new JSONObject();
		
		try {
	        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	        MultipartFile mFile = multipartRequest.getFile("leakXMLFile");
	        CommonsMultipartFile cf= (CommonsMultipartFile)mFile; 
	        DiskFileItem dfi = (DiskFileItem)cf.getFileItem(); 
	        File file = dfi.getStoreLocation();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(file);
			NodeList entryList = document.getElementsByTagName("entry");
			SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );
			for(int i = 0; i < entryList.getLength(); i++){
				Leak leak = new Leak();
				Element element = (Element)entryList.item(i);
				String name = element.getElementsByTagName("vuln:cve-id").item(0).getFirstChild().getNodeValue();
				if(leakService.getLeakByName(name) != null){
					continue;
				}
				Float score =  element.getElementsByTagName("cvss:score").getLength() != 0 ? Float.valueOf(element.getElementsByTagName("cvss:score").item(0).getFirstChild().getNodeValue()) : 0f;
				Long publishedTime = sdf.parse(element.getElementsByTagName("vuln:published-datetime").item(0).getFirstChild().getNodeValue().substring(0,26)).getTime();
				String year = name.substring(4,8);
				Long mdfTime = sdf.parse(element.getElementsByTagName("vuln:last-modified-datetime").item(0).getFirstChild().getNodeValue().substring(0,26)).getTime();
				String cpe = "";
				NodeList cpes = element.getElementsByTagName("vuln:product");
				for(int j = 0; j < cpes.getLength(); j++){
					cpe += cpes.getLength()-1 == j ? cpes.item(j).getFirstChild().getNodeValue() : cpes.item(j).getFirstChild().getNodeValue() + ",";
				}
				String summary = element.getElementsByTagName("vuln:summary").item(0).getFirstChild().getNodeValue();
				String detail = entryList.item(i).getTextContent().trim();
				leak.setName(name);
				leak.setScore(score);
				leak.setPublishedTime(publishedTime);
				leak.setMdfTime(mdfTime);
				leak.setCpe(cpe);
				leak.setSummary(summary);
				leak.setDetail(detail);
				leak.setYear(year);
				leakService.saveLeaks(leak);
				for(int j = 0; j < cpes.getLength(); j++){
					CpeBean cpeBean = new CpeBean();
					cpeBean.setName(cpes.item(j).getFirstChild().getNodeValue());
					cpeBean.setLeak(leak);
					cpeBean.setLeakId(leak.getId());
					leak.getCpes().add(cpeBean);
					cpeService.save(cpeBean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping("getAllLeaks")
	@ResponseBody
	public Object getAllLeaks(@RequestParam(value = "page", defaultValue = "1") Integer pageIndex,
			@RequestParam(value = "rows", defaultValue = "20") Integer pageSize,@RequestParam  Map<String,Object> condition){
		PageBean<Leak> pageList = leakService.getAllLeaks(pageIndex, pageSize, condition);
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		List<Leak> leakList =pageList.getData();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String publishedTime = "";
		String mdfTime = "";
		for(Leak leak : leakList){
			Map<String, Object> row = new HashMap<String, Object>();
			publishedTime = sdf.format(new Date(leak.getPublishedTime()));
			mdfTime = sdf.format(new Date(leak.getMdfTime()));
			row.put("id",leak.getId());
			row.put("name",leak.getName());
			if(leak.getScore() < 4.0){
				row.put("score","低");
			}else if(leak.getScore() < 7.0){
				row.put("score","中");
			}else{
				row.put("score","高");
			}
			row.put("publishedTime",publishedTime);
			row.put("mdfTime",mdfTime);
			row.put("cpe",leak.getCpe());
			row.put("summary",leak.getSummary());
			row.put("detail", leak.getDetail());
			dataList.add(row);
		}
		result.put("total", pageList.getTotal());
		result.put("rows", dataList);
		return result;
	}
	
	@RequestMapping("getAllYears")
	@ResponseBody
	public List<String> getAllYears(@RequestParam  Map<String,Object> condition){
		List<String> years = leakService.getAllYears();
		return years;
	}
	
	@RequestMapping("getByCpe")
	@ResponseBody
	public Object getByCpe(@RequestParam("cpe")String cpeName){
		List<Leak> cveList = leakService.getByCpe(cpeName) ;
		JSONArray result = FastJsonUtil.toJSONArray(cveList,new JSONConverterCallBack<Leak>(){
			@Override
			public void call(JSONObject result, Leak obj) {
				result.put("publishedTime", StringUtil.longDateString(new Date(obj.getPublishedTime()))) ;
				result.put("mdfTime", StringUtil.longDateString(new Date(obj.getMdfTime()))) ;
				if(obj.getScore() != null){
					if (obj.getScore() > 0.0f && obj.getScore() < 3.91f) {
						result.put("score", "低");
					} else if (obj.getScore() > 4.0f && obj.getScore() < 6.91f) {
						result.put("score", "中");
					} else {
						result.put("score", "高");
					}
				}
			}
		}, "name","summary","year");
		Collections.sort(result,new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((JSONObject)o2).getString("name").compareTo(((JSONObject)o1).getString("name")) ;
			}
		}) ;
		return result ;
	}
	
}
