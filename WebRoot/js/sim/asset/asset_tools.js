var att = {
		currentReq:null,
	    closeLoading:function(panel) {// 关闭状态加载框
	    	panel.css("position", "");
	    	if(panel.attr("horizonPosition")) {
	    		panel.css("position", panel.attr("horizonPosition"));
	    		panel.removeAttr("horizonPosition");
	    	}
	    	panel.children("div.datagrid-mask-msg.horizon-mask-msg").remove();
			panel.children("div.datagrid-mask.horizon-mask").remove();
	    },
	    openBodyLoadingModal:function(loadMsg) {// 开启body状态加载框
			if (!loadMsg) {
				loadMsg = "<span style='color:red;'>努力加载中，请稍等......</span>";
			}
			$(document.body).children("div.datagrid-mask-msg.horizon-mask-msg").remove();
			$(document.body).children("div.datagrid-mask.horizon-mask").remove();
			var headZindex = $("body>.sim-header").css("z-index");
			if(!headZindex) {
				headZindex = 1030;
			}
			$("<div class=\"datagrid-mask horizon-mask\" style=\"display:block;\"></div>").css("z-index",headZindex + 1).appendTo($(document.body));
			$("<div class=\"datagrid-mask-msg horizon-mask-msg\" style=\"display:block\"></div>").html(loadMsg).appendTo($(document.body));
			att.centerLoading() ;
	    },
		centerLoading:function() {
			var panel = $(document.body) ;
			var mask = panel.children("div.datagrid-mask.horizon-mask");
			if (mask.length) {
				mask.css( {
					width : panel.outerWidth(),
					height : panel.outerHeight()
				});
				var msg = panel.children("div.datagrid-mask-msg.horizon-mask-msg");
				msg.css({
					left : (panel.outerWidth() - msg.outerWidth()) / 2,
					top : (panel.outerHeight() - msg.outerHeight()) / 2
				});
			}
		}
}
att.remoteLogin = function(autoLogin){
	var type = $("#type").val() ;
	var ip = $("#ip").val() ;
	var username = $("#username").val() ;
	var password = $("#password").val() ;
	var port = $("#port").val() ;
	var charset = $("#charset").val() ;
	var connectionId = $("#connectionId").val() ;
	if(username == "" || password == ""){
		showErrorMessage("用户名或密码不能为空！") ;
		return ;
	}
	if(/^\d{1,5}$/g.test(port)){
		var portNum = parseInt(port) ;
		if(portNum < 1 || portNum > 65535){
			showAlertMessage("无效的端口号！") ;
			return ;
		}
	}else{
		showAlertMessage("无效的端口号！") ;
		return ;
	}
	var url = "/sim/assetvt/remoteLogin";
	var param = {
			type:type,
			ip:ip,
			username:username,
			password:password,
			port:port,
			charset:charset,
			connectionId:connectionId,
			_time:new Date().getTime()
		} ;
	$("#loginForm input[name^='property_']").each(function(index,domEle){
		var domNode = $(domEle) ;
		param[domNode.attr("name")] = domNode.val() ;
	}) ;
	
	var callback ;
	if(type == "ssh" || type == "telnet" || type == "mysql" || type == "oralce" || type == "sqlserver" || type == "db2"){
		callback = att.commandCallback ;
	}else if(type == "sftp" || type == "ftp"){
		callback = att.fileCallback ;
	}
	att.openBodyLoadingModal("<span style='margin-right:10px;'>正在登录，请等待......</span><button style='margin-top:-5px;' onclick='att.cancelLogin()' type='button' class='btn hand'>取消登录</button>") ;
	att.currentReq = $.getJSON(url,param,function(result){
		att.closeLoading($(document.body)) ;
		if(result.success){
			$("#connectionId").val(result.result) ;
			$("#loginInfo").panel("setTitle",type+"到"+ip+"[已登录]") ;
			$("#resultPanel").panel("open") ;
			if(callback){
				callback.call() ;
			}
		}else{
			if(!autoLogin){
				showErrorMessage(result.message) ;
			}
		}
	}) ;
}
att.cancelLogin = function(){
	att.closeLoading($(document.body)) ;
	att.currentReq.abort() ;
}
att.quitLogin = function(){
	var connectionId = $("#connectionId").val() ;
	if(connectionId == ""){
		return ;
	}
	var url = "/sim/assetvt/quitLogin?connectionId="+connectionId+"&_time"+new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.status){
			var type = $("#type").val() ;
			var ip = $("#ip").val() ;
			var connectionId = $("#connectionId").val() ;
			$("#loginInfo").panel("setTitle",type+"到"+ip+"[未登录]") ;
			$("#resultPanel").panel("close") ;
			if(type == "ftp" || type == "sftp"){
				$("#dir_tree").tree("loadData",[]) ;
			}
		}
	}) ;
}
att.changeCharset = function(){
	var connectionId = $("#connectionId").val() ;
	if(connectionId == ""){
		return ;
	}
	var charset = $("#charset").val() ;
	var url = "/sim/assetvt/changeClientCharset?connectionId="+connectionId+"&charset="+charset+"&_time"+new Date().getTime() ;
	$.getJSON(url,function(result){}) ;
}
att.enterHandler = function(event,callback){
	var event = event || window.event || arguments.callee.caller.arguments[0] ;
	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;  
    if (keyCode == 13 && callback) {  
        callback.call() ;
    }   
}
att.execCommand = function(){
	var command = $("#command").val() ;
	if(command == ""){
		showErrorMessage("命令不能为空！") ;
		return ;
	}
	if(command.length > 50){
		showErrorMessage("命令不能大于50个字符！") ;
		return ;
	}
	var connectionId = $("#connectionId").val() ;
	if(connectionId == ""){
		showErrorMessage("未登录！") ;
		$("#username").focus() ;
		return ;
	}
	var url = "/sim/assetvt/exec" ;
	var param = { connectionId : connectionId,command : command,_time:new Date().getTime() } ;
	att.openBodyLoadingModal("正在执行命令，请等待！") ;
	$.getJSON(url,param,function(result){
		att.closeLoading($(document.body)) ;
		if(result.success){
			var commandResult = result.result ;
			if(commandResult.displayType == "string"){
				$("#resultContainer").val(commandResult.result) ;
			}else if(commandResult.displayType == "table"){
				att.buildTable(commandResult) ;
			}
			$("#command").val("") ;
		}else{
			showErrorMessage(result.message) ;
		}
	}) ;
}
att.buildTable = function(result){
	var header = result.header ;
	var data = result.result ;
	var container =  $("#resultContainer").empty() ;
	var currentResultDiv = $("<div/>").width(container.width()).css("padding","5px") ;
	container.append(currentResultDiv) ;
	var datagrid = $("<table border='1'/>").width(container.width()-10) ;
	currentResultDiv.append(datagrid) ;
	var th = $("<tr/>") ;
	datagrid.append(th) ;
	for(var index in header){
		th.append("<td>"+header[index]+"</td>") ;
	}
	if(data && data.length > 0){
		for(var rowIndex in data){
			var rowData = data[rowIndex] ;
			var row = $("<tr/>") ;
			datagrid.append(row) ;
			for(var i=0;i<header.length;i++){
				row.append("<td>"+rowData[i]+"</td>") ;
			}
		}
	}
}
att.commandCallback = function(){
	$("#command").width($("#resultContainer").width()-70) ;
	$("#command").focus() ;
}

att.fileCallback = function(){
	$("#dir_tree").empty() ;
	$("#dir_tree").tree({
		url:"/sim/assetvt/listFileNames",
		loadFilter:function(data,parent){
			if($.isArray(data)){
				return data ;
			}else if(data.status){
				return data.result ; 
			}else{
				showErrorMessage(data.message) ;
			}
		},
		onContextMenu:function(e,node){
			e.preventDefault();
			$(this).tree("select", node.target);
			$("#tree_menu").menu("show", {
				left: e.pageX,
				top: e.pageY
			});
		},
		onBeforeLoad:function(node,param){
			if(node == null){
				var dir_tree = $(this) ;
				dir_tree.tree("append",{data : [{id : "/",text : "根目录",state:"closed"}]}) ;
				return false ;
			}
			param.connectionId = $("#connectionId").val() ;
			param.directory = att.getFullPath($(this),node) ;
		}
	}) ;
}

att.getFullPath = function(tree,node){
	var path = "" ;
	var curNode = node ;
	while(curNode != null){
		path = "/"+curNode.id +  path;
		curNode = tree.tree("getParent",curNode.target) ;
	}
	return path == null ? "/" : path ;
}

att.downloadFile = function(){
	var dir_tree = $("#dir_tree") ;
	var node = dir_tree.tree("getSelected") ;
	if(node == null){
		showAlarmMessage("请选择要下载的文件") ;
		return ;
	}
	if(!dir_tree.tree("isLeaf",node.target)){
		showAlarmMessage("不支持目录下载！") ;
		return ;
	}
	var filePath = att.getFullPath(dir_tree,node) ;
	var connectionId = $("#connectionId").val() ;
	var url = "/sim/assetvt/downloadFile?file="+encodeURI(filePath)+"&connectionId="+connectionId ;
	window.open(url) ;
}

att.deleteFile = function(){
	var dir_tree = $("#dir_tree") ;
	var node = dir_tree.tree("getSelected") ;
	if(node == null){
		showAlarmMessage("请选择要删除的文件或目录！") ;
		return ;
	}
	$.messager.confirm("确认","你确定要删除："+node.text+"?",function(r){
		if(r){
			var filePath = att.getFullPath(dir_tree,node) ;
			var connectionId = $("#connectionId").val() ;
			var isLeaf = dir_tree.tree("isLeaf",node.target) ;
			att.openBodyLoadingModal("正在删除......") ;
			var param = {
				file : Trim(filePath),
				connectionId : connectionId,
				isDir : !isLeaf,
				_time : new Date().getTime()
			}
			$.getJSON("/sim/assetvt/deleteFile",param,function(result){
				att.closeLoading($(document.body)) ;
				if(result.success){
					var parentNode = dir_tree.tree("getParent",node.target) ;
					dir_tree.tree("reload",parentNode.target) ;
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}
		
	}) ;
}

att.uploadFile = function(){
	var uploadFile = $("#uploadFile").val() ;
	if(uploadFile == null || uploadFile == ""){
		showAlertMessage("请选择本地文件！") ;
		return ;
	}
	var dir_tree = $("#dir_tree") ;
	var dirNode = dir_tree.tree("getSelected") ;
	if(dirNode == null){
		showAlertMessage("请选择上传文件路径！") ;
		return ;
	}
	if(dir_tree.tree("isLeaf",dirNode.target)){
		dirNode = dir_tree.tree("getParent",dirNode.target) ;
	}
	var connectionId = $("#connectionId").val() ;
	var dest = att.getFullPath(dir_tree, dirNode) ;
	$.ajaxFileUpload({
		url : "/sim/assetvt/uploadFile?connectionId="+connectionId+"&dest="+encodeURI(dest),
		secureuri : false,
		fileElementId : "uploadFile",
		dataType : "json",
		success : function(data, status){
			if(data.status){
				showAlertMessage("文件上传成功！");
				dir_tree.tree("reload",dirNode.target) ;
			}else{
				showErrorMessage(data.message) ;
			}
		},
		error : function(data, status, e) {
			showErrorMessage("文件上传失败！");
		}
	});
}
att.refreshDir = function(){
	var dir_tree = $("#dir_tree") ;
	var node = dir_tree.tree("getSelected") ;
	if(dir_tree.tree("isLeaf",node.target)){
		return ;
	}
	dir_tree.tree("reload",node.target) ;
}
att.createDir = function(){
	var dir_tree = $("#dir_tree") ;
	var node = dir_tree.tree("getSelected") ;
	if(node == null){
		showAlertMessage("请选择目录所在路径！") ;
		return ;
	}
	if(dir_tree.tree("isLeaf",node.target)){
		node = dir_tree.tree("getParent",node.target) ;
	}
	$.messager.prompt("创建目录","请输入目录名称(最多20个字符)：",function(name){
		if(name){
			if(name.length > 20 ){
				showAlertMessage("目录名称太长！") ;
				return ;
			}
			var path = att.getFullPath(dir_tree, node) ;
			var connectionId = $("#connectionId").val() ;
			var param = {
				dir : path + "/" + Trim(name),
				connectionId : connectionId,
				_time:new Date().getTime()
			}
			$.getJSON("/sim/assetvt/createDir",param,function(result){
				if(result.status){
					dir_tree.tree("reload",node.target) ;
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}
	}) ;
}
$(function(){
	$(window).bind('beforeunload', function(){
		att.quitLogin() ;
	});
	var type = $("#type").val() ;
	var username = $("#username").val() ;
	var password = $("#password").val() ;
	if(type == ""){
		return ;
	}
	if(username != "" && password != ""){
		att.remoteLogin(true) ;
	}else{
		$("#username").focus() ;
	}
}) ;