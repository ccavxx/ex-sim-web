var debug = {}
debug.shutdown = false ;
debug.requestComplete = true ;
debug.timerId = null ;
debug.currentThread = null ;
debug.fileCounter = 0 ;
debug.fileLastSelectLine = {
} ;
debug.className2Id = function(className){
	return className.replace(/\.|\$/g,'_'); 
}
debug.getSourceContainerId = function(className){
	return debug.className2Id(className) + "_source" ;
}
debug.getLineId = function(className,lineNum){
	return debug.className2Id(className) + "_line_" + lineNum ;
}
debug.rebuildRequestParam = function(node,param){
	param.dir = node ? node.id : "" ;
	param.rootPath = node ? node.attributes.rootPath : "";
}

debug.createClassMenu = function(e,node){
	e.preventDefault();
	$(this).tree('select',node.target);
	$('#debug_class_menu').menu('show',{
		left: e.pageX,
		top: e.pageY
	});
} 
debug.decompile = function(){
	var selectNode = $("#debug_class_tree").tree("getSelected") ;
	$.post("/sim/debug/decompile",{file:selectNode.id},function(result){
		if(result.success){
			$("#debug_source_tree").tree("reload") ;
		}
	},"json") ;
}
debug.createSourceMenu = function(e,node){
	e.preventDefault();
	$(this).tree('select',node.target);
	$('#debug_source_menu').menu('show',{
		left: e.pageX,
		top: e.pageY
	});
} 
debug.refreshSource = function(){
	var selectNode = $("#debug_source_tree").tree("getSelected") ;
	$("#debug_source_tree").tree("reload",selectNode.id) ;
}
debug.connect = function(){
	var host = $("#debug_host").val() ;
	var port = $("#debug_port").val() ;
	if(host == "" || port == ""){
		showErrorMessage("主机名和端口不能为空！") ;
		return ;
	}
	var url = "/sim/debug/connect?host="+host+"&port="+port+"&_time="+new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.success){
			showAlertMessage("连接成功！") ;
		}else{
			showErrorMessage(result.message) ;
		}
	});
	window.setInterval(debug.getSuspendThread, 3000) ;
}
debug.getSuspendThread = function(){
	if(debug.shutdown){
		window.clearInterval(debug.timerId) ;
	}
	if(!debug.requestComplete){
		return ;
	}
	debug.requestComplete = false ;
	$.getJSON("/sim/debug/getSuspendThread?_time"+new Date().getTime(),function(result){
		debug.requestComplete = true ;
		if(result&&result.length > 0){
			var threadTree = $("#debug_thread_tree") ;
			for(var i=0;i<result.length;i++){
				var thread = result[i] ;
				var node = threadTree.tree("find",thread.threadId) ;
				if(thread.threadId == debug.currentThread && thread.threadCounter != node.attributes.threadCounter){
					debug.loadThreadInfo(node) ;
				}
				if(node && node.attributes.threadCounter == thread.threadCounter){
					continue ;
				}
				var location = thread.location ? "["+thread.location+"]" : "" ;
				if(node){
					node.attributes.threadCounter = thread.threadCounter ;
					threadTree.tree("update",{target:node.target,text:thread.name + location}) ;
				}else{
					var nodeConfig = {
							id:thread.threadId,text:thread.name + location,
							attributes:{name:thread.name,location:thread.location},
							threadCounter:thread.threadCounter
						} ;
					threadTree.tree("append",{data:[nodeConfig]}) ;
				}
			}
		}
	}) ;
}
debug.disconnect = function(){
	$.getJSON("/sim/debug/disconnect?host=&port=&_time"+new Date().getTime(),function(result){
		if(result.success){
			window.clearInterval(debug.timerId) ;
			showAlertMessage("已断开！") ;
		}
	});
}

debug.openFileTab = function(fileName,filePath,className,lineNum){
	var tab = $("#debug_source_file_tab").tabs("getTab",fileName) ;
	if(tab){
		$("#debug_source_file_tab").tabs("select",fileName) ;
	}else{
		$("#debug_source_file_tab").tabs("add",{
			id:debug.className2Id(className),
			title:fileName,
			selected:true,
			closable:true,
			className:className,
			lineNum:lineNum,
			href:'/sim/debug/openFile?file='+filePath+"&className="+className
		}) ;
	}
}

debug.openFile = function(node){
	var isLeaf = $("#debug_source_tree").tree("isLeaf",node.target) ;
	if(!isLeaf){
		$("#debug_source_tree").tree("expand",node.target) ;
		return ;
	}
	var fileName = node.text ;
	debug.openFileTab(fileName,node.id,node.attributes.className) ;
}
debug.findAndOpenSource = function(className,lineNum){
	var url = newURL("/sim/debug/findSource?",{className:className});
	$.getJSON(url,function(result){
		if(result.success){
			var sourceFile = result.result ;
			debug.openFileTab(sourceFile.text, sourceFile.id, sourceFile.attributes.className,lineNum) ;
		}else{
			showErrorMessage(result.message) ;
		}
	}) ;
}
debug.openRecentFile = function(node){
	debug.openFileTab(node.text,node.id,node.attributes.className) ;
}

debug.sourceFileOpenHandler = function(panel){
	var options = panel.panel("options");
	if(options.className && options.lineNum){
		debug.goLine(options.className, options.lineNum, true) ;
	}
	$("#debug_recent_files_tree").tree("reload") ;
}

debug.breakpoint = function(element,className,methodName,index){
	var methodIndex = index ? index : 0 ;
	var jelement = $(element) ; 
	var operation = jelement.hasClass("icon-start") ? "add" : "remove" ;
	var param = {operation:operation,className:className,methodName:methodName,index:methodIndex,_time:new Date().getTime()};
	$.getJSON("/sim/debug/breakpoint",param,function(result){
		if(result.success){
			var cssClass = jelement.hasClass("icon-start") ? "icon-stop" : "icon-start" ;
			jelement.removeClass("icon-start");
			jelement.removeClass("icon-stop") ;
			jelement.addClass(cssClass) ;
		}else{
			showErrorMessage(result.message) ;
		}
	})
}

debug.methodClickHandler = function(className,lineNum){
	debug.goLine(className, lineNum,true) ;
}
/**
 * mustScrollToTop表示是否强制跳转到指定的行，当mustScrollToTop为false时如果要跳转的行已经在可视范围内，可能不会跳转
 */
debug.goLine = function(className,lineNum,mustScrollToTop){
	var sourceFileTab = $("#debug_source_file_tab") ;
	var selectTab = sourceFileTab.tabs("getSelected")[0] ;
	var tabId = debug.className2Id(className) ;
	if(selectTab.id != tabId){//当前选中的文件不是要跳转到的文件
		var allTabs = sourceFileTab.tabs("tabs") ;
		var find = false ;
		$.each(allTabs,function(index,tab){
			if(tab.id == tabId){
				sourceFileTab.tabs("select",index) ;
				find = true ;
				return false ;
			}
		}) ;
		if(!find){
			debug.findAndOpenSource(className,lineNum) ;
			return ;
		}
	}
	var lineId = debug.getLineId(className, lineNum) ;
	var line = $("#"+lineId) ;
	if(line.length > 0){
		if(debug.fileLastSelectLine[tabId]){
			$("#"+debug.fileLastSelectLine[tabId]).removeClass("lineSelect") ;
		}
		line.addClass("lineSelect") ;
		debug.fileLastSelectLine[tabId] = lineId ;
		var sourceContainerId = debug.getSourceContainerId(className) ;
		var currentScrollTop = $("#"+sourceContainerId).scrollTop();
		var nextScrollTop = line[0].offsetTop ;
		if(mustScrollToTop || nextScrollTop < currentScrollTop ||(nextScrollTop-currentScrollTop) > 300){
			var scrollDiv = $("#"+sourceContainerId).scrollTop(nextScrollTop) ;
		}
	}
}
debug.threadSelectHandler = function(node){
	debug.loadThreadInfo(node) ;
}
debug.loadThreadInfo = function(node){
	var url = "/sim/debug/getThreadInfo?threadId=" + node.id + "&_time=" + new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.success){
			debug.currentThread = node.id ;
			var thread = result.result ;
			if(thread.isSuspend){
				debug.updateVariables(thread.variables) ;
				debug.goLine(thread.className, thread.lineNum) ;
			}
			
		}
	}) ;
}
debug.updateVariables = function(variables){
	var varData = new Array() ;
	if(variables){
		$.each(variables,function(name,value){
			varData.push({name:name,value:value,iconCls:"icon-none"}) ;
		}) ;
	}
	var variableTree = $("#debug_variable_tree") ;
	variableTree.treegrid("loadData",varData) ;
}
debug.step = function(operation){
	$.post("/sim/debug/step",{operation:operation,threadId:debug.currentThread},function(result){
		if(result.success){
			var threadInfo = result.result ;
			var threadId = threadInfo.threadId ;
			var threadTree = $("#debug_thread_tree");
			var threadNode = threadTree.tree("find",threadId) ;
			var selectNode = threadTree.tree("getSelected") ;
			/*if(threadNode.id != selectNode.id){//返回的数据不是当前选中的线程
				
			}else{
				
			}*/
			var location = threadInfo.location ? "["+threadInfo.location+"]" : "";
			threadTree.tree("update",{target:threadNode.target,text:threadNode.attributes.name + location}) ;
			threadTree.tree("getData",threadNode.target).attributes.location = threadInfo.location ;
			debug.updateVariables(threadInfo.variables) ;
			if(threadInfo.isSuspend && threadInfo.className){
				debug.goLine(threadInfo.className, threadInfo.lineNum) ;
			}
		}else{
			showErrorMessage(result.message) ;
		}
	},"json") ;
}
