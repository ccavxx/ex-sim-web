package com.topsec.tsm.sim.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.topsec.tal.base.util.ObjectUtils;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tal.base.web.SpringContextServlet;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.service.DataSourceService;
import com.topsec.tsm.sim.datasource.SimDatasource;
import com.topsec.tsm.sim.report.service.ReportService;
import com.topsec.tsm.sim.util.DeviceTypeNameUtil;
import com.topsec.tsm.sim.webservice.xmltype.DataSourceElement;
import com.topsec.tsm.sim.webservice.xmltype.DataSourceTypeElement;
import com.topsec.tsm.sim.webservice.xmltype.DataSourceTypeListElement;
import com.topsec.tsm.sim.webservice.xmltype.EntryElement;
import com.topsec.tsm.sim.webservice.xmltype.ReportSubjectCategoryElement;
import com.topsec.tsm.sim.webservice.xmltype.ReportSubjectElement;



@Path("/datasource/")
public class DataSourceWebService {

	@Path("types")
	@GET
	@Produces("application/xml")
	public DataSourceTypeListElement types(){
		try {
			DataSourceService service = (DataSourceService) SpringContextServlet.springCtx.getBean("dataSourceService") ; 
			ReportService reportService = (ReportService) SpringContextServlet.springCtx.getBean("reportService") ;
			List<SimDatasource> dataSources = service.getAll(false,false,false) ;
			if(ObjectUtils.isEmpty(dataSources)){
				return new DataSourceTypeListElement(Collections.<DataSourceTypeElement>emptyList());
			}
			Map<String,DataSourceTypeElement> result = new HashMap<String,DataSourceTypeElement>() ;
			for(SimDatasource ds:dataSources){
				AssetObject ao = AssetFacade.getInstance().getAssetByIp(ds.getDeviceIp()) ;
				if(ao == null){
					continue ;
				}
				String securityObjectType = ds.getSecurityObjectType() ;
				DataSourceTypeElement dst = result.get(securityObjectType) ;
				if(dst == null){
					String name = DeviceTypeNameUtil.getDeviceTypeName(securityObjectType) ;
					result.put(securityObjectType, (dst = new DataSourceTypeElement(securityObjectType,name))) ;
					List<Map> subjects = reportService.getRptMaster(securityObjectType) ;
					if(ObjectUtils.isNotEmpty(subjects)){
						for(Map sub:subjects){
							Integer mstId = (Integer)sub.get("id");
							ReportSubjectCategoryElement cat = new ReportSubjectCategoryElement(mstId,(String)sub.get("mstName")) ;
							dst.addSubjectCategory(cat) ;
							List<Map<String,Object>> childSubject = reportService.getChildSubject(mstId) ;
							if(ObjectUtils.isNotEmpty(childSubject)){
								for(Map<String,Object> child:childSubject){
									ReportSubjectElement rs = new ReportSubjectElement((Integer)child.get("id"),(String)child.get("subName")) ;
									String[] fieldLabels = StringUtil.split((String)child.get("tableLabel"));
									String[] fieldNames = StringUtil.split((String)child.get("tableField")) ;
									if(fieldLabels.length != fieldNames.length){
										continue ;
									}
									for(int i=0;i<fieldNames.length;i++){
										rs.addFields(new EntryElement(fieldNames[i], fieldLabels[i])) ;
									}
									cat.addSubject(rs) ;
								}
							}
						}
					}
				}
				dst.addDataSource(new DataSourceElement(ds.getSecurityObjectType(),ds.getDeviceIp(),ao.getName())) ;
			}
			return new DataSourceTypeListElement(new ArrayList<DataSourceTypeElement>(result.values()));
		} catch (Exception e) {
			e.printStackTrace();
			return new DataSourceTypeListElement(Collections.<DataSourceTypeElement>emptyList()) ;
		}
	}
	
}
