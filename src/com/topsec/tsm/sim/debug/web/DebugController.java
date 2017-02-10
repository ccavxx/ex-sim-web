package com.topsec.tsm.sim.debug.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.topsec.tal.base.util.ChainMap;
import com.topsec.tal.base.util.StringUtil;
import com.topsec.tsm.common.SIMConstant;
import com.topsec.tsm.sim.common.bean.Result;

import flm.jd.Decompiler;
import flm.jd.gui.DirSourceDecompiler;
import flm.jdi.debug.DebugException;
import flm.jdi.debug.ThreadDebugger;
import flm.jdi.debug.VMDebugger;

@Controller
@RequestMapping("debug")
public class DebugController {

	private static final String LIB_PATH = "../server/default/lib;"+
										   "../applications/tsm-4sim.ear/sim-web.war/WEB-INF/classes" ;
	private static final String SOURCE_PATH = "./sources/" ;

	private static String VMDEBUGGER_SESSION_KEY = "VMDEBUGGER_SESSION_KEY" ;
	private static String RECENT_FILES_SESSION_KEY = "OPEN_SESSION_KEY" ;
	@RequestMapping("ui")
	public String debug(HttpServletRequest request){
		boolean enableDebug = StringUtil.booleanVal(System.getProperty(SIMConstant.DEBUG_PROPERTY_KEY)) ;
		if(enableDebug){
			return "/page/debug/debug" ;
		}
		request.setAttribute("message", "Debug参数未打开！") ;
		return "page/error/error" ;
	}

	private JSONArray listFiles(String dir,String rootPath){
		JSONArray data = new JSONArray() ;
		File currentDir = new File(dir) ;
		if(!currentDir.exists() || !currentDir.isDirectory()){
			return data ;
		}
		File[] files = currentDir.listFiles() ;
		for(File file:files){
			if(!file.exists()){
				continue ;
			}
			data.add(createFileJSON(file, rootPath)) ;
		}
		return data ;
	}
	
	private JSONObject createFileJSON(File file,String rootPath){
		JSONObject fileJSON = new JSONObject() ; 
		try {
			String path = file.getAbsolutePath() ;
			fileJSON.put("id", path) ;
			fileJSON.put("text", file.getName()) ;
			Map<String,String> attributes = new HashMap<String,String>(2) ;
			fileJSON.put("attributes", attributes);
			if(file.isDirectory()){
				attributes.put("rootPath", rootPath) ;
				fileJSON.put("state", "closed") ;
			}else{
				String className = path.substring(rootPath.length()+1).replace(File.separatorChar, '.').replace(".java", "") ;
				attributes.put("className", className) ;
				fileJSON.put("state", "open") ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileJSON ;
	}
	
	@RequestMapping("listClass")
	@ResponseBody
	public Object listClass(@RequestParam(value="dir",defaultValue="")String dir,@RequestParam(value="rootPath",defaultValue="")String rootPath){
		JSONArray result = new JSONArray() ;
		if(StringUtil.isBlank(dir)){
			String[] filePaths = StringUtil.split(System.getProperty("TSM.LIB.PATH",LIB_PATH),";") ;
			for(String path:filePaths){
				File file = new File(path) ;
				if(file.exists()){
					result.add(createFileJSON(file, file.getAbsolutePath())) ;
				}
			}
		}else{
			result.addAll(listFiles(dir, rootPath)) ;
		}
		return result ;
	}
	
	@RequestMapping("listSource")
	@ResponseBody
	public Object listSource(@RequestParam(value="dir",defaultValue="")String dir,@RequestParam(value="rootPath",defaultValue="")String rootPath){
		JSONArray result = new JSONArray() ;
		if(StringUtil.isBlank(dir)){
			String[] filePaths = StringUtil.split(System.getProperty("TSM.SOURCE.PATH",SOURCE_PATH),";") ;
			for(String path:filePaths){
				File file = new File(path) ;
				if(file.exists()){
					result.add(createFileJSON(file, file.getAbsolutePath())) ;
				}
			}
		}else{
			result.addAll(listFiles(dir, rootPath)) ;
		}
		return result ;
	}
	
	@RequestMapping("decompile")
	@ResponseBody
	public Object decompile(@RequestParam("file")String file){
		Decompiler decompile = new DirSourceDecompiler() ;
		String sourceSavePath = System.getProperty("TSM.DECOMPILER.PATH","./sources/") ;
		decompile.decompile(sourceSavePath, false, file) ;
		Result result = new Result();
		return result.build(true) ;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("recentOpenFiles")
	@ResponseBody
	public Object recentOpenFiles(HttpSession session){
		Map<String,String> files = (Map<String,String>) session.getAttribute(RECENT_FILES_SESSION_KEY) ;
		if(files == null){
			return Collections.emptyList() ;
		}
		JSONArray result = new JSONArray(files.size()) ;
		for(Map.Entry<String, String> entry:files.entrySet()){
			String fileName = entry.getKey() ;
			JSONObject entryJSON = new JSONObject() ;
			entryJSON.put("id", fileName);
			entryJSON.put("text", fileName.substring(fileName.lastIndexOf('\\')+1));
			entryJSON.put("attributes", ChainMap.newMap("className", entry.getValue()));
			result.add(entryJSON) ;
		}
		return result ;
	}
	
	@SuppressWarnings("unchecked")
	private void pushRecentFiles(HttpSession session,String file,String className){
		Map<String,String> files = (Map<String,String>) session.getAttribute(RECENT_FILES_SESSION_KEY) ;
		if(files == null){
			session.setAttribute(RECENT_FILES_SESSION_KEY, (files = new LinkedHashMap<String,String>())) ;
			return ;
		}
		files.remove(files) ;
		files.put(file, className) ;
	}
	
	@RequestMapping("openFile")
	public String openFile(@RequestParam("file")String file,
						   @RequestParam("className")String className,
						   @RequestParam(value="encoding",defaultValue="UTF-8")String encoding,
						   HttpServletRequest request,
						   HttpSession session){
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		try {
			if(debugger != null){
				List<String> lines = FileUtils.readLines(new File(file), encoding) ;
				pushRecentFiles(session, file,className) ;
				request.setAttribute("lines", lines) ;
				List<Method> methods = debugger.getMethods(className) ;
				List<JSONObject> methodJSON = new ArrayList<JSONObject>(methods.size()) ;
				for(Method mt:methods){
					JSONObject mj = new JSONObject() ;
					Location loc = mt.location() ;
					if(loc != null){
						mj.put("name", mt.name()) ;
						mj.put("location", loc.lineNumber()) ;
						mj.put("hasBreakpoint", debugger.hasBreakPoint(loc)) ;
						methodJSON.add(mj) ;
					}
				}
				request.setAttribute("classNameId", className.replace('.', '_').replace('$', '_')) ;
				request.setAttribute("className", className) ;
				request.setAttribute("methods", methodJSON) ;
				request.setAttribute("tabId", request.getParameter("tabId")) ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "/page/debug/source_file" ;
	}
	
	@RequestMapping("findSource")
	@ResponseBody
	public Object findSource(@RequestParam("className")String className){
		Result result = new Result() ;
		String[] filePaths = StringUtil.split(System.getProperty("TSM.SOURCE.PATH",SOURCE_PATH),";") ;
		String[] paths = StringUtil.split(className,"\\.") ;
		for(String path:filePaths){
			File currentFile = new File(path) ;
			if(!currentFile.exists()){
				continue ;
			}
			String[] pathWithCurrentFile = new String[paths.length+1] ;
			pathWithCurrentFile[0] = currentFile.getName() ;
			System.arraycopy(paths, 0, pathWithCurrentFile, 1, paths.length) ;
			File file = findFiles(pathWithCurrentFile, currentFile, 0) ;
			if (file != null) {
				JSONObject fileJSON = createFileJSON(file, currentFile.getAbsolutePath()) ;
				return result.buildSuccess(fileJSON) ;
			}
		}
		return result.buildError("没有找到源文件！") ;
	}
	
	private File findFiles(String[] paths,File currentFile,int level){
		if(currentFile.isDirectory() && currentFile.getName().equalsIgnoreCase(paths[level])){
			level++ ;
			for(File child:currentFile.listFiles()){
				File findFile = findFiles(paths, child, level) ;
				if(findFile != null){
					return findFile ;
				}
			}
		}else if(level == paths.length-1 && currentFile.getName().equalsIgnoreCase(paths[level]+".java")){
			return currentFile ;
		}
		return null ;
	}
	@RequestMapping("connect")
	@ResponseBody
	public Object connect(@RequestParam("host")String host,@RequestParam("port")Integer port,HttpSession session){
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		Result result = new Result() ;
		if(debugger != null){
			return result.buildError("已连接！") ;
		}
		VMDebugger dbg = new VMDebugger(host,port, 10000) ;
		try {
			dbg.connect() ;
			session.setAttribute(VMDEBUGGER_SESSION_KEY, dbg) ;
			result.build(true) ;
		} catch (Exception e) {
			e.printStackTrace();
			result.buildError("连接失败："+e.getMessage()) ;
		}
		return result ;
	}
	
	@RequestMapping("breakpoint")
	@ResponseBody
	public Object breakpoint(@RequestParam("operation")String operation,
							 @RequestParam("className")String className,
							 @RequestParam("methodName")String methodName,
							 @RequestParam(value="index",defaultValue="0")Integer index,
							 HttpSession session){
		Result result = new Result();
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		if(debugger == null){
			return result.buildError("未连接！") ;
		}
		try {
			if("add".equals(operation)){
				debugger.addBreakpoint(className, methodName, index) ;
			}else{
				debugger.removeBreakpoint(className, methodName, index) ;
			}
			result.buildSuccess(null) ;
		} catch (DebugException e) {
			result.buildError(e.getMessage()) ;
		}
		return result ;
	}
	
	@RequestMapping("disconnect")
	@ResponseBody
	public Object disconnect(@RequestParam("host")String host,@RequestParam("port")Integer port,HttpSession session){
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		Result result = new Result() ;
		if(debugger != null){
			debugger.disconnect();
			session.removeAttribute(VMDEBUGGER_SESSION_KEY) ;
		}
		return result.build(true) ;
	}
	
	@RequestMapping("getSuspendThread")
	@ResponseBody
	public Object getSuspendThread(HttpSession session){
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		JSONArray result = new JSONArray() ;
		if(debugger == null){
			return result ;
		}
		Map<ThreadReference,ThreadDebugger> threads = debugger.getAllControllers() ;
		for(Map.Entry<ThreadReference, ThreadDebugger> entry:threads.entrySet()){
			ThreadReference thread = entry.getKey() ;
			JSONObject threadJSON = new JSONObject() ;
			Location loc = entry.getValue().location() ;
			threadJSON.put("threadId", thread.uniqueID()) ;
			threadJSON.put("location", loc != null ? loc.toString() : null) ;
			threadJSON.put("name", thread.name()) ;
			threadJSON.put("threadCounter", entry.getValue().getCounter()) ;
			result.add(threadJSON) ;
		}
		return result ;
	}
	
	@RequestMapping("getThreadInfo")
	@ResponseBody
	public Object getThreadInfo(@RequestParam("threadId")Long threadId,HttpSession session){
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		Result result = new Result() ;
		if(debugger == null){
			return result.buildError("未连接！") ;
		}
		JSONObject threadJSON = new JSONObject() ;
		ThreadDebugger threadDebugger = debugger.getController(threadId) ;
		if(threadDebugger != null){
			threadJSON.put("isSuspend", threadDebugger.isSuspend()) ;
			if(threadDebugger.isSuspend()){
				Location loc = threadDebugger.location() ;
				threadJSON.put("className", loc.declaringType().name()) ;
				threadJSON.put("lineNum", loc.lineNumber()) ;
				threadJSON.put("location", loc.toString()) ;
				Map<String,Object> vars = threadDebugger.getVariables();
				threadJSON.put("variables", vars) ;
			}
		}
		return result.buildSuccess(threadJSON) ;
	}
	
	@RequestMapping("step")
	@ResponseBody
	public Object step(@RequestParam("operation")String operation,
					   @RequestParam("threadId")Long threadId,
					   HttpSession session){
		Result result = new Result(true, null) ;
		VMDebugger debugger = (VMDebugger) session.getAttribute(VMDEBUGGER_SESSION_KEY) ;
		if(debugger == null){
			return result.buildError("未连接！") ;
		}
		ThreadDebugger threadDebugger = debugger.getController(threadId) ;
		if(threadDebugger == null){
			return result.buildError("线程调试对象不存在！") ;
		}
		Location currentLoc = threadDebugger.location();
		if("stepInto".equals(operation)){
			threadDebugger.stepInto() ;
		}else if("stepOver".equals(operation)){
			threadDebugger.stepOver() ;
		}else if("stepOut".equals(operation)){
			threadDebugger.stepOut() ;
		}else if("stepContinue".equals(operation)){
			threadDebugger.resume() ;
		}else{
			return result.buildError("无效的请求参数！") ;
		}
		try {
			threadDebugger.waitComplete(10000) ;
		} catch (TimeoutException e1) {
			return result.buildError("等待执行超时！");
		}
		//new StepThread(operation,threadDebugger).start() ;
		JSONObject threadInfo = new JSONObject() ;
		threadInfo.put("threadId", threadId) ;
		threadInfo.put("isSuspend", threadDebugger.isSuspend()) ;
		currentLoc = threadDebugger.location() ;
		if(currentLoc != null){//为null说明当前线程没有停留在suspend状态，可能处于wait或者sleep状态
			threadInfo.put("locationMethodName", currentLoc.method().name()) ;
			threadInfo.put("className", currentLoc.declaringType().name()) ;
			threadInfo.put("lineNum", currentLoc.lineNumber()) ;
			threadInfo.put("location", currentLoc.toString()) ;
			threadInfo.put("variables", threadDebugger.getVariables()) ;
		}
		result.buildSuccess(threadInfo) ;
		return result ;
	}
	
	/*static class StepThread extends Thread{
		private String operation ;
		private ThreadDebugger threadDebugger ;
		
		public StepThread(String operation, ThreadDebugger threadDebugger) {
			super();
			this.operation = operation;
			this.threadDebugger = threadDebugger;
		}

		@Override
		public void run() {
			if("stepInto".equals(operation)){
				threadDebugger.stepInto() ;
			}else if("stepOver".equals(operation)){
				threadDebugger.stepOver() ;
			}else if("stepOut".equals(operation)){
				threadDebugger.stepOut() ;
			}else if("stepContinue".equals(operation)){
				threadDebugger.resume() ;
			}
		}
		
	}*/
	
}
