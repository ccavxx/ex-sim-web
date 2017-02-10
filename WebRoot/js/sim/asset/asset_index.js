var al = {
	view:"list"
}
var ONLY_LEAF_CAN_SELECTED = "OnlyLeafCanSelected" ;//只允许选中子节点\

al.onAssetLayoutWestExpand=function(){
	$("#assetLayoutId").layout("expand","west");
}
/**
 * 资产树点击事件
 */
al.assetItemClickHandler = function(node){
	if(node.attributes){
		if(node.attributes.isAsset){
			al.showAssetDetail(al.createAssetData(node.text,node.id)) ;
		}else{
			al.loadAssetList(node) ;
		}
	}
}
al.showDetail = function(name,ip){
	al.showAssetDetail(al.createAssetData(name, ip)) ;
}
al.ipFormatter = function(value,row,index){
	return "<span class='"+row.assetIconCls+"' style='width:18px;'/>"+"<span>"+value+"</span>" ;
}
al.nameFormatter = function(value,row,index){
	return "<span class='"+row.assetIconCls+"' style='width:18px;'/>"+"<span class='table_column_link' onclick=\"al.showDetail('"+ row.name + "','"+row.ip+"');\">"+value+"</span>" ;
}
al.assetTreeFormatter = function(node){
	var temp = '<span ';
	if(node.attributes&&node.attributes.isAsset){
		temp += ('title=\''+node.id+'\'');
	}
	temp += ('>'+node.text+'</span>');
	return temp;
}
al.osFormatter = function(value,row,index){
	var osFormatter = "";
	if(value){
		osFormatter = "<span class='"+row.osIconCls+"' style='width:18px;'/>"+"<span>"+value+"</span>" ;
	}
	return osFormatter;
}
al.logCountFormatter = function(value,row,index){
	return countFormatter(value,1) ;
}
/*wq 更新级别样式 */
al.safeRankFormatter = function(value,row,index){
	var val;
	if(value=="高"){
		val = 3;
	}else if(value == "中"){
		val = 2;
	}else if(value =="低"){
		val = 1;
	}
	var res = "";
	if(val){
		res = "<span class='priority"+val+"'/>";
	}
	return res;
}
al.alarmFormatter = function(value,row,index){
	var fontColor = value > 0 ? "#FF0000" : "#000000" ;
	 return "<a href='#' style='color:"+fontColor+"' onclick=\"simMainHandler.showAssetAlarm('"+row.ip+"');\">"+value+"</a>"
}
al.operationFormatter = function(value,row,index){
	var id = row.id ;
	var html = "<a href='#' class='icon-edit hand icon16' title='编辑' onclick=\"al.editAsset(event,'"+id+"')\"></a>" +"&nbsp&nbsp";
	html += "<a href='#' class='icon-remove hand icon16' title='删除' onclick=\"al.deleteOneAsset('"+id+"')\"></a>" ;
	return html ;
}
al.deleteOneAsset = function(id){
	$.messager.confirm("警告","此操作会删除相关的日志源、监视对象、日志文件，你确定要删除选中的资产吗？",function(r){
		if(!r) return ;
		var ids = id ;
		$.getJSON('/sim/assetlist/deleteAssets?ids='+ids,function(result){
			var assetTab = $("#al_asset_tabs") ;
			var deleteAssetNames = result.result ;
			if(deleteAssetNames.length > 0){
				for(var i=0;i<deleteAssetNames.length;i++){
					assetTab.tabs("close",deleteAssetNames[i]) ;
				}
				$("#al_asset_table").datagrid("reload") ;
				$("#al_asset_tree").tree("reload") ;
			}
			if(!result.status){
				showErrorMessage(result.message) ;
			}
		});
		$("#al_asset_table").datagrid("uncheckAll");
	}) ;
};
al.stateFormatter = function(value,row,index){
	var state = row.available == 1 ? "启用" : "禁用" ;
	return state ;
}
al.toolsFormatter = function(value,row,index){
	/*var tools = row.tools ;
	var html = "" ;
	var tool ;
	for(var i in tools){
		tool = tools[i] ;
		if(tool == "ping"){
			html += "<a href='#' class='icon-asset-tool icon-"+tool+" hand' title='"+tool+"' onclick=\"simHandler.ping('"+row.ip+"')\"><a>";
		}else if(tool == "mstsc"){
			html += "<a href='#' class='icon-asset-tool icon-"+tool+" hand' title='远程桌面' onclick=\"al.mstsc('"+row.ip+"')\"><a>";
		}else if("http" == tool.toLowerCase() || "https" == tool.toLowerCase()){
			var url = tool+"://"+row.ip;
			html += "<a href='"+url+"' target='_blank' class='icon-asset-tool icon-"+tool+" hand' title='"+tool+"'><a>";
		}else{
			var url = "/sim/assetvt/openClient?type="+tool+"&ip="+row.ip +"&osName="+row.osName;
			html += "<a href='"+url+"' target='_blank' class='icon-asset-tool icon-"+tool+" hand' title='"+tool+"'><a>";
		}
	} */
	var html = "<a href='#' class='icon-asset-tool icon-tool" + " hand' onclick=\"al.showToolList('"+index+"')\"></a>";
	return html ;
}

al.showToolList = function(rowIndex){
	var row = $("#al_asset_table").datagrid("getRows")[rowIndex];
	var tools = row.tools;
	var html = "";
	var tool;
	//$("#al_menu").html("");
	$('#al_menu').empty();
	for(var i in tools){
		tool = tools[i];
		if(tool == "ping"){
			html += "<div onmouseover=\"al.showColor(this)\" onmouseout=\"al.hideColor(this)\"><a href='#' class='icon-asset-tool icon-"+tool+" hand' onclick=\"simHandler.ping('"+row.ip+"')\"></a>" 
			+ "<a href='#' style=\"text-decoration:none;\" onclick=\"simHandler.ping('"+row.ip+"')\">" + tool  + "</a></div>";
		}else if(tool == "mstsc"){
			html += "<div onmouseover=\"al.showColor(this)\" onmouseout=\"al.hideColor(this)\"><a href='#' class='icon-asset-tool icon-"+tool+" hand' onclick=\"al.mstsc('"+row.ip+"')\"></a>"
			+ "<a href='#' style=\"text-decoration:none;\" onclick=\"al.mstsc('"+row.ip+"')\">远程桌面</a></div>";
		}else if("http" == tool.toLowerCase() || "https" == tool.toLowerCase()){
			var url = tool+"://"+row.ip;
			html += "<div onmouseover=\"al.showColor(this)\" onmouseout=\"al.hideColor(this)\"><a href='"+url+"' target='_blank' class='icon-asset-tool icon-"+tool+" hand'></a>" 
			+ "<a href='"+url+"' style=\"text-decoration:none;\" target='_blank'>" + tool + "</a></div>";
		}else{
			var url = "/sim/assetvt/openClient?type="+tool+"&ip="+row.ip +"&osName="+row.osName;
			html += "<div onmouseover=\"al.showColor(this)\" onmouseout=\"al.hideColor(this)\"><a href='"+url+"' target='_blank' class='icon-asset-tool icon-"+tool+" hand'></a>" 
			+ "<a href='"+url+"' style=\"text-decoration:none;\" target='_blank'>" + tool + "</a></div>";
		}
	}
	
	$("#al_menu").append(html);
	$("#al_asset_table").datagrid("clearSelections");//取消所有选中项
	$("#al_asset_table").datagrid("selectRow", rowIndex);//根据索引选中该行
	$("#al_menu").menu('show', {
		left:x,//在鼠标点击处显示菜单
		top:y
	});
}
al.showColor = function(div){
	$(div).css("background","#00ccff");
}
al.hideColor = function(div){
	$(div).css("background","none");
}

var x,y;
$('#al_asset_panel').mousemove(function(e) {
	x=e.pageX;
	y=e.pageY;
}); 

al.mstsc = function(ip){
	if($.browser.msie && parseFloat($.browser.version) >= 8){
		try{
		   	var objShell = new ActiveXObject("wscript.shell");      
		   	objShell.Run("mstsc /v:" + ip);    
		   	return ;
		}catch(err){
			var width = $(window).width() ;
			var height = $(window).height() - 70;
			var url = "/sim/assetvt/openClient?type=mstsc&ip="+ip+"&width="+width+"&height="+height ;
			window.open(url) ;
		}
	}else{
		showAlertMessage("此功能只有在IE浏览器下可用，且浏览器版本最低为IE8！") ;
	}
}
al.changeState = function(a){
	var clickLink = $(a) ;
	var id = clickLink.attr("id") ;
	var state = clickLink.attr("currentState") == 0 ? 1 : 0;
	var startRelated  ;
	if(state == 1){//启用要询问用户是否启动相关的日志源及监视对象
		var dialog = $('<div/>').dialog({
			title:null,
	        width: 300,   
	        height: 80,   
	        closed: false, 
	        cache: false,
	        content:"<div style='margin:5px;'>是否启动关联的日志源对象和监视对象？</div>",
	        modal: true,
	        buttons:[{text:"是",
			      handler:function(){
			    	  al.doChange(id, state, true) ;
			    	  dialog.dialog("destroy") ;
			      }
			},{
				      text:"否",
				      handler:function(){
				    	  al.doChange(id, state, false) ;
				    	  dialog.dialog("destroy") ;
				      }
			}]
	    });
	}else{
		al.doChange(id, state, true) ;
	}
		
}
al.doChange = function(id,state,cascade){
	var url = "/sim/assetlist/changeState?id="+id+"&state="+state+"&cascade="+cascade+"&_time="+new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.status){
			$("#al_asset_table").datagrid("reload") ;
		}else{
			showErrorMessage(result.message) ;
		}
	}) ;
}
al.availableFormatter = function(value,row,index){
	if(value == 0){
		return "<span class='icon-disabled'></span>" ;
	}else{
		return "<span class='icon-enabled'></span>" ;
	}
}
al.switchAssetView = function(select){
	var selectValue = select.value ;
	al.view = selectValue ;
	if(selectValue=="grid"){
		al.showGridView() ;
	}else if(selectValue=="card"){
		al.showCardView() ;
	}
}
al.showGridView = function(){
	$("#al_asset_tabs").tabs("select",0) ;
	$("#al_asset_card_panel").css("display","none") ;
	$("#al_asset_list_panel").css("display","block") ;
	$("#al_asset_table").datagrid("resize");
}
al.showCardView = function(){
	$("#al_asset_tabs").tabs("select",0) ;
	$("#al_asset_list_panel").css("display","none") ;
	$("#al_asset_card_panel").css("display","block") ;
	al.rebuildCardView() ;
}
al.rebuildCardView = function(){
	var cardContainer = $("#al_asset_card_container") ;
	cardContainer.empty() ;
	/*if(cardContainer.attr("dataLoaded") == "true"){
		return ;
	}*/
	var groupAlias = "category" ;//分组方式
	$.getJSON("/sim/assetlist/assetCard?groupAlias="+groupAlias+"&_time="+new Date().getTime(),function(result){
		if(result && result.length > 0){
			for(var groupIndex in result){
				var group = result[groupIndex] ;
				var catContainer = $("<div/>") ;
				catContainer.append(al.createCatHeader(group.name)) ;
				var catList = $("<div/>") ;
				var assets = group.assets ;
				for(var assetIndex in assets){
					catList.append(al.createAssetCard(assets[assetIndex],2)) ;
				}
				catContainer.append(catList) ;
				catContainer.append("<div style='clear:left;height:10px;'/>") ;//空的div
				cardContainer.append(catContainer) ;
			}
		}
		cardContainer.attr("dataLoaded","true");
	}) ;
}
al.createCatHeader = function(name,groupIndex){
	var categoryHeader = $("<div style='padding-left:10px;'/>") ; 
	categoryHeader.append("<div class='cat_header'>"+name+"</div>") ;
	categoryHeader.append("<div class='card_line'/>") ;
	return categoryHeader ;
}
al.createRow = function(isFirstRow){
	var paddingTop = isFirstRow ? "0px" : "10px" ; 
	return $("<div style='padding-top:"+paddingTop+"'/>") ;
}
al.createAssetCard = function(asset,rowspan){
	var cardDIV = $("<div class='asset_card'/>") ;
	var cardHeader = al.createCardHeader(asset) ;//卡片头信息
	cardDIV.append(cardHeader) ;
	//cardDIV.append("<div style='clear:both;width:100%;height:1px;background-color:#98C1D0'/>") ;//分割线
	cardDIV.append(al.createCardMiddle(asset)) ;
	cardDIV.append("<div style='clear:left;width:170px;margin-left:5px;height:1px;background-color:#98C1D0'/>") ;//分割线
	cardDIV.append(al.createCardFooter(asset)) ;
	return cardDIV ; 
}
al.createCardHeader = function(asset){
	var cardHeader = $("<div class='card_header' style='width:100%;'/>").css("line-height","33px").css("float","left") ;
	var cls = asset.available == 1 ? "icon-asset-enabled" : "icon-asset-disabled" ;
	var avaiTitle = asset.available == 1  ? "启用" : "禁用" ;
	cardHeader.append("<span class='"+cls+"' title='"+avaiTitle+"' style='width:18px;line-height:18px;height:18px;margin:-2px 2px 0px;'/>") ;
	cardHeader.append("<span class='table_column_link' style='text-decoration: underline;' onclick=\"al.showDetail('"+asset.name+"','"+asset.ip+"')\">"+asset.name+"</span>") ;
	//var operationDIV = $("<div style='float:right;width:40px;height:25px;'/>")
	//cardHeader.append(operationDIV) ;
	//编辑菜单
	var modifyIcon = $("<span class='card_modify hand' style='margin:5px;float:right;' onclick=\"al.showForm('edit','"+asset.id+"')\"/>") ;
	cardHeader.append(modifyIcon) ;
	modifyIcon.mouseover(function(){
		$(this).removeClass("card_modify").addClass("card_modify_over") ;
	}) ;
	modifyIcon.mouseout(function(){
		$(this).removeClass("card_modify_over").addClass("card_modify") ;
	}) ;
	return cardHeader ;
}
al.createCardMiddle = function(asset){
	var middleDIV = $("<div style='float:left;height:80px;width:100%;'/>") ;
	var stateICON = $("<span class='"+asset.stateIconCls+"' title='"+asset.stateText+"' style='margin:30px 0px 0px 2px;'/>") ;
	var deviceTypeImage = $("<img src='"+asset.deviceTypeIcon+"' style='margin-top:15px;' title='"+asset.deviceTypeName+"'/>") ;
	middleDIV.append(stateICON) ;
	middleDIV.append(deviceTypeImage) ;
	var cpuCount = 0;
	var memoryCount = 0;
	var processCount = 0;
	var win32ServiceCount = 0;
	var portCount = 0;
	var leakCount = 0;
	var interfaces = 0;
	var existInterface = false;
	$.ajaxSettings.async = false; 
	
	$.getJSON("/sim/assetdetail/assetStatus?attributeId=cpu" + "&ip=" + asset.ip, function(data){
		if (data && data.result) {
			cpuCount = data.result;
		}
	});
	$.getJSON("/sim/assetdetail/assetStatus?attributeId=memory" + "&ip=" + asset.ip, function(data){
		if (data && data.result) {
			memoryCount = data.result;
		}
	});
	$.getJSON("/sim/assetlist/processCount?ip=" + asset.ip, function(data){
		if (data) {
			processCount = data.length;
		}
	});
	$.getJSON("/sim/assetlist/win32ServiceCount?ip=" + asset.ip, function(data){
		if (data) {
			win32ServiceCount = data.length;
		}
	});
	$.getJSON("/sim/assetlist/portCount?ip=" + asset.ip, function(data){
		if (data) {
			portCount = data.length;
		}
	});
	$.getJSON("/sim/assetlist/leakCount?ip=" + asset.ip, function(data){
		if (data) {
			leakCount = data.result;
		}
	});
	$.getJSON("/sim/assetlist/getInterface?ip=" + asset.ip, function(data){
		if (data && data.interfaces) {
			existInterface = true;
			$.getJSON("/sim/assetdetail/assetStatus?attributeId=interface" + "&ip=" + asset.ip, function(data){
				if(!data.result){
					return;
				}
				var data =  $.map(data.result,function(record){
					return record;
				});
				interfaces = data.length;
			});
		} else {
			existInterface = false;
		}
	});
	
	$.ajaxSettings.async = true;
	var assetCpuTitle = "cpu：" + cpuCount + "%";
	var assetMemoryTitle = "内存：" + memoryCount + "%";
	var assetProcessTitle = "进程数：" + processCount;
	var assetServiceTitle = "服务数：" + win32ServiceCount;
	var assetPortTitle = "端口数：" + portCount;
	var assetLeakTitle = "漏洞数：" + leakCount;
	var assetInterfacesTitle = "接口数：" + interfaces;
	var assetInfoDiv = $("<div style='float:right;height:60px;'/>");
	var assetCpuDiv = "<div class='icon-asset-cpu-info'  style='margin:0px 0px 0px -20px;' title='"+assetCpuTitle+"'></div>";
	var assetMemoryDiv = "<div class='icon-asset-memory-info'  style='margin:0px 0px 0px -20px;' title='"+assetMemoryTitle+"'></div>";
	var assetProcessDiv = "<div class='icon-asset-process-info'  style='margin:0px 0px 0px -20px;' title='"+assetProcessTitle+"'></div><br/>";
	var assetServiceDiv = "<div class='icon-asset-service-info'  style='margin:-25px 0px 0px -20px;' title='"+assetServiceTitle+"'></div>";
	var assetPortDiv = "<div class='icon-asset-port-info'  style='margin:-25px 0px 0px -20px;' title='"+assetPortTitle+"'></div>";
	var assetLeakDiv = "<div class='icon-asset-leak-info'  style='margin:-25px 0px 0px -20px;' title='"+assetLeakTitle+"'></div>";
	var assetInterfacesDiv = "<div class='icon-asset-interfaces-info'  style='margin:-25px 0px 0px -20px;' title='"+assetInterfacesTitle+"'></div>"
	assetInfoDiv.append(assetCpuDiv);
	assetInfoDiv.append(assetMemoryDiv);
	assetInfoDiv.append(assetProcessDiv);
	assetInfoDiv.append(assetServiceDiv);
	assetInfoDiv.append(assetPortDiv);
	assetInfoDiv.append(assetLeakDiv);
	if (existInterface) {
		assetInfoDiv.append(assetInterfacesDiv);
	}
	middleDIV.append(assetInfoDiv);
	return middleDIV ;
}

al.createCardFooter = function(asset){
	var footerDIV = $("<div class='card_footer'/>") ;
	footerDIV.append("<span title='今日事件:"+asset.eventCount+"'>事件: "+countFormatter(asset.eventCount)+"</span>") ;
	footerDIV.append("<span style='width:1px;background-color:#98C1D0;height:42px;margin:0px;'/>") ;
	footerDIV.append("<span title='今日日志:"+asset.logCount+"'>日志: "+countFormatter(asset.logCount)+"</span>") ;
	return footerDIV ;
}
/**
 * wq
 */
al.more = function(it){
	var disp = $("#assetMoreSearchInfoId").css("display");
	if(disp=='none'){
		$("#assetMoreSearchInfoId").css("display","block")
		$(it).attr("title","隐藏");
		$(it).find(".l-btn-left>.l-btn-text>.l-btn-empty").removeClass("easyui-accordion-expand");
		$(it).find(".l-btn-left>.l-btn-text>.l-btn-empty").addClass("easyui-accordion-collapse");
		$("#assetMoreSearchInfoId select").each(function(){
			$(this).combobox("setValue","");
		});
	}else{
		$("#assetMoreSearchInfoId").css("display","none");
		$(it).attr("title","更多");
		$(it).find(".l-btn-left>.l-btn-text>.l-btn-empty").removeClass("easyui-accordion-collapse");
		$(it).find(".l-btn-left>.l-btn-text>.l-btn-empty").addClass("easyui-accordion-expand");
	}
	// var height = $("#al_asset_panel").panel('options').height;
     $("#al_asset_table").datagrid('resize');
}
/**
 * 创建资产数据结构
 */
al.createAssetData = function(name,ip){
	var assetData = {name:name,ip:ip};
	return assetData ;
}
/**
 * 资产列表双击事件
 */
al.assetRowDbClickHandler = function(index,rowData){
	al.showAssetDetail(al.createAssetData(rowData.name,rowData.ip));
}
al.assetCellDbClickHandler = function(rowIndex,field,value){
	if(field == "_expander" || field == "id" || field == "no_field2" || field == "no_field3"){
		return ;
	}
	var rowData = $(this).datagrid("getRows")[rowIndex] ;
	al.showAssetDetail(al.createAssetData(rowData.name,rowData.ip));
}
al.loadAssetList = function(node){
	
}
al.detailFormatter = function(index,row){
    return '<div style="padding:2px"><table class="ddv"></table></div>';
}
al.onExpandRow = function(index,row){
	var ddv = $(this).datagrid('getRowDetail',index).find('table.ddv');
    ddv.datagrid({
        url:'/sim/assetlist/more?ip='+row.ip,
        fitColumns:true,
        singleSelect:true,
        loadMsg:'',
        height:'auto',
        columns:[[
            {field:'group',title:'所属组',width:100},
            {field:'name',title:'名称',width:100},
            {field:'deviceTypeName',title:'类型',width:100},
            {field:'collectMethod',title:'收集方式',width:100},
            {field:'available',title:'状态',width:100,formatter:al.stateFormatter}
        ]],
        onResize:function(){
            $(this).datagrid('fixDetailRowHeight',index);
        },
        onLoadSuccess:function(data){
        	if(data.total == 0){
        		$(this).datagrid('appendRow', {group:'<div style="text-align:center;color:red">没有相关记录！</div>' })
        		       .datagrid('mergeCells', {index: 0, field: 'group', colspan: 5 })
        	}
            setTimeout(function(){
                $('#al_asset_table').datagrid('fixDetailRowHeight',index);
            },0);
        }
    }) ;
}
/**
 * 跳转到资产详细页面
 */
al.showAssetDetail = function(assetData){
	var url = "/sim/assetlist/assetEnabled?ip="+assetData.ip+"&_time="+new Date().getTime() ;
	$.getJSON(url,function(result){
		if(result.deleted){
			showAlertMessage("资产已经被删除！") ;
		}else if(result.enabled == 0){
			showAlertMessage("资产已被禁用，无法查看详细信息！") ;
		}else{
			addOrShowTab(assetData.name, '/sim/assetdetail/showDetail?ip='+assetData.ip) ;
		}
	}) ;
}
al.beforeDeviceTypeSelect = function(node){
	var tree = $(this);
	if(tree.tree("isLeaf",node.target)){
		return true ;
	}else{
		if(node.state == "closed"){
			tree.tree("expand",node.target);
		}else{
			tree.tree("collapse",node.target);
		}
		throw new Error(ONLY_LEAF_CAN_SELECTED) ;//抛出错误，阻止下拉tree被关闭
	}
}
/**
 * 函数错误处理
 * @return true不在浏览器中显示错误,false在浏览器状中显示此错误
 */
window.onerror = function(msg){
	//由于chrome中返回的msg不只自定义的msg还包含系统附加的前辍，所以采用indexOf来判断
	if(msg.indexOf(ONLY_LEAF_CAN_SELECTED) > -1){
		return true ;
	}
	return false ;
}

/**
 * 资产新建
 */
al.newAsset = function(){
	al.showForm("add") ;
}
/**
 * 资产编辑
 */
al.editAsset = function(event,id){
	var evt = event ? event : window.event ;
	 if (evt.stopPropagation) {
    	evt.stopPropagation();
    }else {
        evt.cancelBubble = true;
    }
	al.showForm("edit",id) ;
}
al.showForm = function(operation,assetId){
	var asset_table_panel = $('#al_asset_panel').panel('panel');
	if(!asset_table_panel)return;
//	$(asset_table_panel).parent().css('position','relative');
	var width = $(asset_table_panel).width();
	var height = $(asset_table_panel).height();
	var url = '/sim/assetlist/assetForm?operation='+operation+"&id="+assetId ;
	al.al_asset_form = $("#al_asset_form").dialog({
		top:0,
		left:0,
		inline:true,
		noheader:true,
		collapsed:true,
		modal:true,
		shadow:false,
		border:false,
		buttons:[{text:"保存",
			handler:al.saveAsset
		},{
			text:"取消",
			handler:al.closeAssetForm
		}],
		href:url,
		style:{'padding':0,'border':0},
		onCollapse:al.coloseAddAssetPanel
	});
	al.al_asset_form.panel('resize',{
		width: width,
		height: height
	});
	al.al_asset_form.dialog('expand',true);
}
al.setSelect = function(){
	var selectValue = $(this).attr("selectValue") ;
	var data = $(this).combobox("getData") ;
	var valueField = $(this).combobox("options").valueField ;
	var selectRowExist = false ;  
	$.each(data,function(index,row){
		if(selectRowExist = (row[valueField] == selectValue)){//选中的记录
			return false ;
		}
	}) ;
	if(selectRowExist){
		$(this).combobox("select",selectValue) ;
	}else{
		$("#hid_"+$(this).attr("id")).val("") ;
	}
}
al.setChangeValue = function(newValue, oldValue){
	var oldId = $(this).attr("id");
	if( oldId && newValue ){
		$(this).combobox("setValue",newValue);
		$("#hid_"+oldId).val(newValue);
		// $("#hid_"+oldId).blur();
	}
}
al.resetDeviceTypeText = function(){
	/*var cbt = $(this) ;
	var selectItem = cbt.combotree("tree").tree("getSelected") ;
	var text = cbt.combotree("getText") ;
	if(selectItem != null && text != selectItem.attributes.deviceTypeName){
		cbt.combotree("setText",selectItem.attributes.deviceTypeName) ;
	}*/
}
/**
 * 资产类型选择
 */
al.onDeviceTypeSelect = function(node){
	$("#af_deviceTypeName").val(node.text);
	// $("#af_deviceTypeName").blur();
}
al.setDeviceTypeSelect = function(){
	var select = $("#af_deviceType") ;
	select.combotree("setValue",select.attr("selectValue")) ;
	select.combotree("setText",select.attr("selectText")) ;
	/*var tree = select.combotree("tree") ;
	var node = tree.tree("find",select.attr("selectValue")) ;
	tree.tree("select",node.target) ;*/
}
/**
 * 关闭新建资产页面
 */
al.closeAssetForm = function(){
	//$('#add_asset').dialog('restore');
	$('#al_asset_form').dialog('collapse',true);
}
/**
 * 资产保存
 */
al.saveAsset = function(){
	$("#af_assetForm").submit();
}
/**
 * 资产保存返回处理
 */
al.addResultHandler = function(result){
	$("#al_asset_form > .dialog-button > .l-btn").first().linkbutton("enable");
	if(result.status){
		al.closeAssetForm() ;
		if(al.view == "card"){
			al.rebuildCardView() ;
		}
		$("#al_asset_table").datagrid("reload") ;
		$("#al_asset_tree").tree("reload") ;
	}else{
		showErrorMessage(result.message) ;
	}
}
var tmp_close_count=0;
al.coloseAddAssetPanel = function(){
	tmp_close_count++;
	if(tmp_close_count%2 == 0){
		$('#al_asset_form').dialog('close',false);
	}
		
}
al.loadSuccessHandler = function(){
	var nodeId = simHandler.assetId;
	if(nodeId){
		var node = $('#al_asset_tree').tree('find',nodeId);
		if(node){
			var treeElement = $('#al_asset_tree');
			//var expandTarget = treeElement.tree('getParent',node.target).target ;
			treeElement.tree('expandTo',node.target) ;
			treeElement.tree('select', node.target);
		}
		simHandler.assetId = null;
	}
}
/**
 * 删除选中的第一个资产
 */
al.deleteAsset = function(){
	var selectRow = $("#al_asset_table").datagrid("getSelected") ;
	if(selectRow){
		$.messager.confirm("警告","此操作会删除相关的日志源、监视对象、日志文件，你确定要删除选中的资产吗？",function(r){
			if(!r) return ;
			$.getJSON('/sim/assetlist/deleteAsset?id='+selectRow.id,function(result){
				if(result.status){
					$("#al_asset_tabs").tabs("close",result.result) ;
					$("#al_asset_table").datagrid("reload") ;
					$("#al_asset_tree").tree("reload") ;
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}) ;
	}else{
		showAlarmMessage('请选择一条数据');
	}
};
al.deleteSelectAssets = function(){
	var selectRows = $("#al_asset_table").datagrid("getChecked") ;
	if(selectRows.length > 0){
		$.messager.confirm("警告","此操作会删除相关的日志源、监视对象、日志文件，你确定要删除选中的资产吗？",function(r){
			if(!r) return ;
			var ids = "" ;
			$.each(selectRows,function(index,row){
				ids = ids+row.id+"," ;
			}) ;
			$.getJSON('/sim/assetlist/deleteAssets?ids='+ids,function(result){
				var assetTab = $("#al_asset_tabs") ;
				var deleteAssetNames = result.result ;
				if(deleteAssetNames.length > 0){
					for(var i=0;i<deleteAssetNames.length;i++){
						assetTab.tabs("close",deleteAssetNames[i]) ;
					}
					$("#al_asset_table").datagrid("reload") ;
					$("#al_asset_tree").tree("reload") ;
				}
				if(!result.status){
					showErrorMessage(result.message) ;
				}
			});
			$("#al_asset_table").datagrid("uncheckAll");
		}) ;
	}else{
		showAlertMessage("请选择要删除的资产！") ;
	}
};
al.changeAssetsState = function(state){
	var selectRows = $("#al_asset_table").datagrid("getChecked") ;
	if(selectRows.length == 0){
		var operation = state == 1 ? "启用" : "禁用" ;
		showAlertMessage("请选择要"+operation+"的资产！") ;
	}else if(state == 1){
		var isHaveForbidden;
		//遍历所选资产是否含有禁用资产
		for ( var i = 0; i < selectRows.length; i++) {
			if (selectRows[i].available == 0) {
				isHaveForbidden = true;
				break;
			}
		}
		if (isHaveForbidden) {//点击启用按钮时，勾选的资产中有禁用资产才会弹提示框
			askYesOrNo("是否启动关联的日志源对象和监视对象？",function(yesOrNo){
				al.sendChangeStateRequest(selectRows, state,yesOrNo) ;
				$("#al_asset_table").datagrid("uncheckAll");
			}) ;
		} else {
			showAlertMessage("资产已启用");
		}
	}else{
		var isHaveEnabled;
		//遍历所选资产是否含有启用资产
		for ( var i = 0; i < selectRows.length; i++) {
			if (selectRows[i].available == 1) {
				isHaveEnabled = true;
				break;
			}
		}
		if (isHaveEnabled) {//点击禁用按钮时，勾选的资产中有启用资产才会弹提示框
			$.messager.confirm("确认","你确定要禁用选中的资产吗？",function(r){
				if(!r) return ;
				al.sendChangeStateRequest(selectRows, state,true) ;
				$("#al_asset_table").datagrid("uncheckAll");
			}) ;
		} else {
			showAlertMessage("资产已禁用");
		}
	}
}
al.sendChangeStateRequest = function(selectRows,state,cascade){
	var ids = "" ;
	if (state == 1) {//启用
		$.each(selectRows,function(index,row){
			if (row.available == 0) {//此时只有被禁用的资产id才加入到数组中
				ids = ids + row.id +"," ;
			}
		});
	} else {//禁用
		$.each(selectRows,function(index,row){
			if (row.available == 1) {//此时只有启用的资产id才加入到数组中
				ids = ids + row.id +"," ;
			}
		});
	}
	$.getJSON('/sim/assetlist/changeAssetsState?cascade='+cascade+'&state='+state+'&ids='+ids,{_time:new Date().getTime()},function(result){
		$("#al_asset_table").datagrid("reload") ;
		if(!result.status){
			showErrorMessage(result.message) ;
		}
	}) ;
}
/**
 * 导出设备excel模板
 * */
al.exportAssetTemplate=function(){
	$.getJSON('/sim/assetlist/downloadAssetModel',{_time:new Date().getTime()},function(result){
		if(result){
			showAlertMessage("模板下载成功!");
		}else{
			showErrorMessage("模板下载失败！") ;
		}
	}) ;
};

/**
 * 导出资产
 */
al.exportAssetExcel = function() {
	window.open(encodeURI("/sim/assetlist/exportAssetExcel")) ;
};

/**
 * 打开导入浏览界面
 * */
al.openAssetTemplate=function(){
	addOrShowTab('导入设备', '/page/asset/import_asset.html') ;
};
al.searchQuery = function(){
	var data = {
			  ip:$("#asset_listform_Id input[name=ip]").val(),
			  name:$("#asset_listform_Id input[name=name]").val(),
			  deviceType:$("#asset_listform_Id_deviceType").combotree("getValue"),
			  osName:$("#asset_listform_Id_osName").combobox("getValue"),
			  safeRank:$("#asset_listform_Id input[name=safeRank]").val(),
			  nodeId:$("#asset_listform_Id input[name=nodeId]").val(),
			  enabled:$("#asset_listform_Id_Enabled").val()
	}

	$("#al_asset_table").datagrid({
		queryParams:data
	});
}
function importAssetTemplate(){
	
}

$(function(){
	$("#importAsset_tabs").tabs({
		onBeforeClose:function(){
		}
	});
	
});

function refreshAsset(title,index){
//	var tab = $('#al_asset_tabs').tabs("getTab",index) ;
//	clearGroupTimer(tab.panel("options").tabSeq) ;
	simHandler.changeMenu(event,'/page/asset/asset.html');
}

/**
 * 打开设备发现页面
 */
al.openAssetDiscover = function(){
	addOrShowTab('资产发现', '/page/asset/asset_discover.html') ;
}
/**
 * 新建或显示已经创建的tab页
 * @param text　tab名称
 * @param href tab地址
 */
var tabSeq = 0 ;
function addOrShowTab(text,href){
	var tabs = $('#al_asset_tabs') ; 
	var tab = tabs.tabs('getTab',text);
	if(tab){
		tabs.tabs('select',text);
	}else{
		var tabMaxCount = 10 ;
		var tabCount = tabs.tabs("tabs").length ;
		if((tabCount -1) == tabMaxCount){//减1是因为资产列表占用了一个标签位置
			showAlertMessage("已经达到标签页数量上限("+tabMaxCount+")，请先关闭其它标签页，再重新打开") ;
			return ;
		}
		tabSeq++ ;
		if(href.indexOf('?')>0){
			href += '&tabSeq='+tabSeq ;
		}else{
			href += '?tabSeq='+tabSeq ;
		}
		tabs.tabs('add',{id:"assetTab"+tabSeq,tabSeq:tabSeq,title:text,href:href,closable:true});  	
	}
}
/**
 * 返回选中的tab
 * @returns
 */
function getSelectedTab(){
	var tab = $('#al_asset_tabs').tabs('getSelected');
	return tab ;
}
/**
 * 返回选中的tab的tabSeq
 */
function getSelectTabSeq(){
	return getSelectedTab().panel("options").tabSeq ;
}

/**
 * 根据组件的id前辍(不带${tabId}部分)获取组件
 * 注意：在资产列表页面打开资产详细时，由于可以同时打开多个资产的详细页面
 * 为了避免各个资产之间组件id重复，为每个打开的tab中的组件都定义了一个后辍(即当前tab options中的tabSeq)
 * 所以页面中的组件需要加上当前tab页的tabSeq作为后辍才能获取到组件
 * @param idPart
 */
function getTabElement(idPart){
	var tabSeq = getSelectTabSeq() ; 
	return $("#"+idPart+tabSeq) ;
}
/**
 * 关闭标签事件处理
 * @param title
 * @param index
 */
function onCloseAssetTab(title,index){
	var tab = $('#al_asset_tabs').tabs("getTab",index) ;
	clearGroupTimer(tab.panel("options").tabSeq) ;
}