package com.topsec.tsm.sim.asset.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.topsec.tal.base.util.SpringWebUtil;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.comm.CommunicationException;
import com.topsec.tsm.comm.CommunicationExpirationException;
import com.topsec.tsm.message.MessageDefinition;
import com.topsec.tsm.resource.NodeDefinition;
import com.topsec.tsm.sim.asset.AssetFacade;
import com.topsec.tsm.sim.asset.AssetObject;
import com.topsec.tsm.sim.asset.exception.LimitedNumException;
import com.topsec.tsm.sim.asset.web.vtclient.CommandException;
import com.topsec.tsm.sim.asset.web.vtclient.CommandResult;
import com.topsec.tsm.sim.asset.web.vtclient.ConnectionBusyException;
import com.topsec.tsm.sim.asset.web.vtclient.ConnectionProxy;
import com.topsec.tsm.sim.asset.web.vtclient.ConnectionProxyFactory;
import com.topsec.tsm.sim.asset.web.vtclient.FileBrowser;
import com.topsec.tsm.sim.asset.web.vtclient.FileEntry;
import com.topsec.tsm.sim.asset.web.vtclient.ProxyException;
import com.topsec.tsm.sim.asset.web.vtclient.ProxyType;
import com.topsec.tsm.sim.common.bean.Result;
import com.topsec.tsm.sim.common.model.TreeModel;
import com.topsec.tsm.sim.node.service.NodeMgrFacade;
import com.topsec.tsm.sim.node.util.NodeStatusQueueCache;
import com.topsec.tsm.sim.resource.persistence.Node;
import com.topsec.tsm.sim.util.NodeUtil;

/**
 * 资产虚拟终端
 * @author hp
 *
 */
@Controller
@RequestMapping("assetvt")
public class AssetVTController {
	
	/**
	 * 向主机发送ping命令
	 * @param ip
	 * @return
	 */
	@RequestMapping(value="ping",produces="text/html;charset=utf-8")
	@ResponseBody
	public String ping(@RequestParam("ip")String ip,HttpServletRequest request) {
		try {
			AssetFacade assetFacade = AssetFacade.getInstance() ;
			AssetObject ao = assetFacade.getAssetByIp(ip) ;
			if(ao == null){
				return "资产已经被删除!" ; 
			}
			NodeMgrFacade nodeMgrFacade = (NodeMgrFacade) SpringWebUtil.getBean("nodeMgrFacade", request) ;
			String scanNodeId = ao.getScanNodeId() ;
			Node scanNode = nodeMgrFacade.getNodeByNodeId(scanNodeId,false,true,false,false) ;
			Node collectorNode = NodeUtil.isAgent(scanNode.getType()) ? scanNode : NodeUtil.getChildByType(scanNode, NodeDefinition.NODE_TYPE_COLLECTOR) ;
			if (NodeStatusQueueCache.offline(collectorNode.getNodeId())) {
				return "节点掉线！" ;
			}
			String pingResult = (String) NodeUtil.dispatchCommand(NodeUtil.getRoute(collectorNode), MessageDefinition.CMD_PING_ASSET, ip, 30000) ;
			if (pingResult != null) {
				pingResult = pingResult.replace("\n", "<br/>") ;
			}
			return pingResult ; 
		} catch (CommunicationException e) {
			return "ping命令下发超时！" ;
		}catch (Exception e) {
			return "ping命令下发出错！";
		}
	}	
	@RequestMapping("openClient")
	public String openClient(@RequestParam("type")String type,@RequestParam("ip")String ip,HttpServletRequest request) {
		AssetObject asset = AssetFacade.getInstance().getAssetByIp(ip) ; 
		if(asset == null){
			request.setAttribute("errorMessage","资产不存在！") ;
			return "/page/error/error_page" ;
		}
		ProxyType proxyType = ConnectionProxyFactory.getProxyType(type) ;
		if(proxyType == null){
			request.setAttribute("error_message", "不支持的代理类型"+type+"！");
			return "/page/error/error_page" ;
		}
		request.setAttribute("defaultPort", proxyType.getDefaultPort()) ;
		request.setAttribute("type",type) ;
		request.setAttribute("clientProperties", proxyType.getClientProperties()) ;
		request.setAttribute("accountName", asset.getAccountName()) ;
		request.setAttribute("accountPassword", StringUtil.decrypt(asset.getAccountPassword())) ;
		if("mstsc".equalsIgnoreCase(type)){
			return "/page/asset/mstsc" ;
		}else{
			return "/page/asset/remote_client" ;
		}
	}
	/**
	 * 远程登录
	 * @param type
	 * @param ip
	 * @param username
	 * @param password
	 * @param port
	 * @param connectionId
	 * @param session
	 * @return
	 */
	@RequestMapping("remoteLogin")
	@ResponseBody
	public Object remoteLogin(
			@RequestParam("type")String type,
			@RequestParam("ip")String ip,
			@RequestParam("username")String username,
			@RequestParam("password")String password,
			@RequestParam("charset")String charset,
			@RequestParam(value="port",defaultValue="-1")int port,
			@RequestParam(value="connectionId",required=false)String connectionId,
			@RequestParam(value="timout",defaultValue="10")int timeout,
			HttpServletRequest request,
			HttpSession session) {
		Result result = new Result() ;
		if(StringUtil.isBlank(username)||StringUtil.isBlank(password)){
			return result.buildError("用户名或密码不能为空") ;
		}
		try {
			if(StringUtil.isNotBlank(connectionId)){
				ConnectionProxyFactory.deleteProxy(connectionId) ;
			}
			port = (port <0 || port > 65535) ? ConnectionProxyFactory.getDefaultPort(type) : port ;
			ConnectionProxy proxy = ConnectionProxyFactory.createProxy(type,ip, username, password,port) ;
			if(StringUtil.isNotBlank(charset)){
				proxy.setCharset(charset) ;
			}
			timeout = timeout > 60 || timeout < 5 ? 5 : timeout ;
			Enumeration<String> allParamsName = request.getParameterNames() ;
			while(allParamsName.hasMoreElements()){
				String paramName = allParamsName.nextElement() ;
				if(paramName.startsWith("property_")){
					proxy.setProperty(paramName.substring(9),request.getParameter(paramName));
				}
			}
			proxy.connect(timeout*1000) ;
			ConnectionProxyFactory.put(proxy.getSessionId(), proxy) ; 
			result.buildSuccess(proxy.getSessionId()) ;
		} catch (ProxyException e) {
			result.buildError(StringUtil.nvl(e.getMessage(),"登录失败！")) ;
		} catch (LimitedNumException e) {
			result.buildError("登录失败，客户端数已达上限！") ;
		} catch (Exception e) {
			result.buildError("登录失败!") ;
		}
		return result;
	}
	/**
	 * 退出当前远程登录的会话
	 * @param connectionId
	 * @return
	 */
	@RequestMapping("quitLogin")
	@ResponseBody
	public Object quitLogin(@RequestParam("connectionId")String connectionId) {
		ConnectionProxyFactory.deleteProxy(connectionId) ;
		return new Result(true,null) ;
	}
	@RequestMapping("changeClientCharset")
	@ResponseBody
	public Object changeClientCharset(@RequestParam("connectionId")String connectionId,@RequestParam("charset")String charset) {
		ConnectionProxy proxy = ConnectionProxyFactory.getProxy(connectionId) ;
		if (proxy != null) {
			proxy.setCharset(StringUtil.isBlank(charset) ? null : charset) ;
		}
		return new Result(true,null) ;
	}
	@RequestMapping("exec")
	@ResponseBody
	public Object exec(@RequestParam("command")String command,@RequestParam("connectionId")String connectionId) {
		ConnectionProxy proxy = ConnectionProxyFactory.getProxy(connectionId) ;
		Result result = new Result() ;
		if (proxy == null) {
			return result.buildError("会话已经关闭，请重新登录！") ;
		}
		try {
			command = StringUtil.trim(command) ;
			if(StringUtil.isBlank(command)){
				throw new ProxyException("命令不能为空！") ;
			}
			CommandResult response = proxy.exec(command, 20000) ;
			result.buildSuccess(response) ;
		} catch (CommunicationExpirationException e) {
			result.buildError("命令执行超时！") ;
		}catch(UnsupportedOperationException e){
			result.buildError("不支持的命令："+e.getMessage()) ;
		}catch (CommandException e) {
			result.buildError(e.getMessage()) ;
		}catch (ProxyException e) {
			result.buildError(StringUtil.nvl(e.getMessage(),"命令执行出错！")) ;
		} catch (ConnectionBusyException e) {
			result.buildError("会话正忙，无法继续执行其它命令！") ;
		}
		return result ;
	}
	/**
	 * 列出目录文件
	 * @param directory
	 * @return
	 */
	@RequestMapping("listFileNames")
	@ResponseBody
	public Object listFileNames(
			@RequestParam("directory")String directory,
			@RequestParam("connectionId")String connectionId) {
		Result result = new Result() ;
		FileBrowser fb = (FileBrowser) ConnectionProxyFactory.getProxy(connectionId) ;
		if (fb == null) {
			return result.buildError("会话已经关闭，请重新登录！") ;
		}
		List<TreeModel> treeData = new ArrayList<TreeModel>() ;
		try {
			directory = StringUtil.isBlank(directory) ? "/" : directory ;
			List<FileEntry> fileNames = fb.listFileNames(directory);
			for(FileEntry fileName:fileNames){
				if(fileName.getName().equals(".") || fileName.getName().equals("..")){
					continue ;
				}
				treeData.add(new TreeModel(fileName.getName(),fileName.getName(),fileName.isDir() ? "closed" : "open")) ;
			}
			result.buildSuccess(treeData) ;
		}catch(ConnectionBusyException e){
			result.buildError("会话正忙，无法执行其它操作！");
		}catch (ProxyException e) {
			result.buildError(StringUtil.nvl(e.getMessage(),"列出文件目录失败！")) ;
		}catch (Exception e) {
			e.printStackTrace() ;
			result.buildError("列出文件目录失败！") ;
		}
		return result ;
	}
	
	@RequestMapping("downloadFile")
	public void downloadFile(
			@RequestParam("file")String file,
			@RequestParam("connectionId")String connectionId,HttpServletResponse resp,HttpServletRequest request) throws IOException {
		try{
			file = StringUtil.recode(file) ; 
			FileBrowser fb = (FileBrowser) ConnectionProxyFactory.getProxy(connectionId) ;
			resp.setContentType("application/octet-stream") ;
			String fileName = file.substring(file.lastIndexOf('/')+1) ;
			String userAgent = request.getHeader("User-Agent") ;
			if(userAgent.indexOf("Firefox")>0){
				resp.setHeader("Content-Disposition", "attachment; filename*=\"UTF-8' '" + StringUtil.encode(fileName, "UTF-8")  + "\"");
			}else{
				resp.setHeader("Content-Disposition", "attachment; filename=\"" + StringUtil.encode(fileName, "UTF-8") + "\"");
			}
			fb.download(file, resp.getOutputStream()) ;
		}catch(ProxyException e){
			resp.sendRedirect("/page/error/error_page?errorMessage="+e.getMessage()) ;
		}catch(ConnectionBusyException e){
			resp.sendRedirect("/page/error/error_page?errorMessage=会话正忙，无法执行其它操作！") ;
		}catch(Exception e){
			e.printStackTrace() ;
		}
	}
	@RequestMapping("uploadFile")
	@ResponseBody
	public Object uploadFile(
			@RequestParam("dest")String dest,
			@RequestParam("connectionId")String connectionId,
			MultipartHttpServletRequest request) {
		Result result = new Result();
		MultipartFile file=request.getFile("uploadFile");
		FileBrowser fb = (FileBrowser) ConnectionProxyFactory.getProxy(connectionId) ;
		if (fb == null) {
			return result.buildError("会话已经关闭，请重新登录！") ;
		}
		try {
			String path = StringUtil.recode(dest) + "/" + file.getOriginalFilename() ;
			fb.upload(file.getInputStream(), path) ;
			result.buildSuccess("文件上传成功！") ;
		} catch (ProxyException e) {
			result.buildError(StringUtil.nvl(e.getMessage(),"文件上传失败！")) ;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConnectionBusyException e) {
			result.buildError("会话正忙，无法执行其它操作！") ;
		}
		return result ;
	}
	@RequestMapping("deleteFile")
	@ResponseBody
	public Object deleteFile(
			@RequestParam("file")String file,
			@RequestParam("connectionId")String connectionId,
			@RequestParam(value="isDir",defaultValue="false")boolean isDir) {
		Result result = new Result() ;
		try{
			file = StringUtil.recode(file) ;
			FileBrowser fb = (FileBrowser) ConnectionProxyFactory.getProxy(connectionId) ;
			if(isDir){
				fb.deleteDir(file) ;
			}else{
				fb.delete(file) ;
			}
			result.buildSuccess(null) ;
		}catch(ConnectionBusyException e){
			result.buildError("会话正忙，无法执行其它操作！") ;
		}catch(ProxyException e){
			result.buildError(StringUtil.nvl(e.getMessage(),"文件删除失败！")) ;
		}catch(Exception e){
			e.printStackTrace() ;
			result.buildError("删除失败！") ;
		}
		return result ;
	}
	
	@RequestMapping("createDir")
	@ResponseBody
	public Object createDir(
			@RequestParam("dir")String dir,
			@RequestParam("connectionId")String connectionId) {
		Result result = new Result() ;
		FileBrowser fb = (FileBrowser) ConnectionProxyFactory.getProxy(connectionId) ;
		if (fb == null) {
			return result.buildError("会话已经关闭，请重新登录！") ;
		}
		try {
			fb.createDir(StringUtil.recode(dir)) ;
		} catch (ProxyException e) {
			result.buildError(StringUtil.nvl(e.getMessage(),"创建目录失败！"));
		} catch (ConnectionBusyException e) {
			result.buildError("会话正忙，无法执行其它操作！") ;
		} catch(Exception e){
			result.buildError("创建目录失败！") ;
		}
		return result ;
	}
}
