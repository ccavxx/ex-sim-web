package com.topsec.tsm.sim.asset.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.topsec.tsm.asset.AssTopo;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.AssetUtil;
import com.topsec.tsm.sim.asset.dao.TopoDao;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.report.util.TopoUtil;
import com.topsec.tsm.sim.resource.exception.ResourceNameExistException;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;
import com.topsec.tsm.util.UUIDUtils;

public class TopoServiceImpl implements TopoService {
	private TopoDao topoDao ;
	public TopoDao getTopoDao() {
		return topoDao;
	}

	public void setTopoDao(TopoDao topoDao) {
		this.topoDao = topoDao;
	}

	@Override
	public void saveOrUpdate(AssTopo topo)throws ResourceNameExistException {
		if(isNameExist(topo.getId(), topo.getName())){
			throw new ResourceNameExistException(topo.getName()) ;
		}
		topoDao.saveOrUpdate(topo) ;
	}

	@Override
	public List<AssTopo> getAll() {
		return topoDao.getAll();
	}

	@Override
	public List<AssTopo> getUserTopoList(String userName) {
		return topoDao.getUserTopoList(userName);
	}

	@Override
	public AssTopo get(Integer id) {
		return topoDao.findById(id);
	}

	@Override
	public void delete(Integer id) {
		topoDao.delete(id) ;
	}

	@Override
	public void updateTopoName(Integer id, String name)throws ResourceNameExistException {
		if(isNameExist(id, name)){
			throw new ResourceNameExistException(name) ;
		}
		topoDao.updateProperty(id,"name",name) ;
	}
	
	private boolean isNameExist(Integer id,String name){
		AssTopo topo = topoDao.getByName(name) ;
		if(topo == null || topo.getId().equals(id)){//此名称的拓扑图还不存在或者是与当前的拓扑图是同一对象
			return false ;
		}
		return true ;
	}

	@Override
	public AssTopo getSystemTopo(NodeMgrFacade nodeMgr) {
		String config = getSystemTopoConfig(nodeMgr) ;
		AssTopo at = new AssTopo(-1,"系统拓扑",config) ;
		return at;
	}
	private String getSystemTopoConfig(NodeMgrFacade nodeMgr){
		Node auditor = nodeMgr.getKernelAuditor(false, true, false, false) ;
		try {
			Document doc = DocumentFactory.getInstance().createDocument() ;
			Element graph = doc.addElement("graph") ;
			appendNodeElement(graph, auditor.getNodeId(), auditor.getResourceName(), AssetUtil.getNodeIcon48("auditor"),auditor.getIp()) ;
			appendAssetElementsByNode(graph, auditor.getNodeId()) ;
			for(Node child:auditor.getChildren()){
				if(NodeUtil.isAgent(child.getType())){
					appendNodeElement(graph, child.getNodeId(), child.getResourceName(), AssetUtil.getNodeIcon48("agent"),child.getIp()) ;
					appendLineElement(graph, child.getNodeId(), auditor.getNodeId()) ;
					appendAssetElementsByNode(graph, child.getNodeId()) ;
				}
			}
			StringWriter sw = new StringWriter() ;
			doc.write(sw) ;
			return sw.getBuffer().toString();
		} catch (IOException e) {
			e.printStackTrace() ;
			return null;
		}
	}
	private void appendAssetElementsByNode(Element graph,String nodeId){
		List<AssetObject> assets = AssetFacade.getInstance().getByScanNode(nodeId) ;
		for(AssetObject ao:assets){
			appendAssetElement(graph, ao.getId(), ao.getName(), AssetUtil.getIcon48(ao.getDeviceType()), ao.getIp()) ;
			appendLineElement(graph, ao.getId(), nodeId) ;
		}
	}
	private Element appendNodeElement(Element parent,String id,String text,String icon,String ip){
		Element el = parent.addElement("node") ;
		el.addAttribute("type", "node").addAttribute("id", id).addAttribute("text", text).addAttribute("ip", ip)
		  .addAttribute("icon", icon).addAttribute("showLabel", "true").addAttribute("labelposition", "bottom") ;
		return el ;
	}
	private Element appendAssetElement(Element parent,String id,String text,String icon,String ip){
		Element el = parent.addElement("node") ;
		el.addAttribute("type", "asset").addAttribute("id", id).addAttribute("text", text).addAttribute("ip", ip)
		  .addAttribute("icon", icon).addAttribute("showLabel", "true").addAttribute("labelposition", "bottom") ;
		return el ;
	}
	private Element appendLineElement(Element parent,String from,String to){
		Element el = parent.addElement("arc").addAttribute("id", UUIDUtils.compactUUID())
				           .addAttribute("source", from).addAttribute("destination", to).addAttribute("type", "实线")
				           .addAttribute("directed", "true").addAttribute("lineType", "straightline").addAttribute("weight", "2")
				           .addAttribute("layout", "default").addAttribute("color", "0");
		return el ;
	}

	@Override
	public AssTopo getByName(String name) {
		return topoDao.getByName(name);
	}
}
