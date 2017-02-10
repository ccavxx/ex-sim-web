var ad = {
	resetDeviceTypeText : function(){
		/*var cbt = $(this) ;
		var selectItem = cbt.combotree("tree").tree("getSelected") ;
		var text = cbt.combotree("getText") ;
		if(selectItem != null && text != selectItem.attributes.deviceTypeName){
			cbt.combotree("setText",selectItem.attributes.deviceTypeName) ;
		}*/
	},
	beforeAssetCategoreSelect:function(node){
		var tree = $(this) ;
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
}
ad.lastEditRow = -1 ;
/**
 * 安全等级编辑器
 */
safeRankEditor = {type:'combobox',options:{editable:false,url:'/sim/asset/safeRankList',valueField:'value',textField:'value',panelHeight:80}} ;
//businessGroupEditor = {type:'combobox',options:{editable:false,url:'/sim/asset/groups',valueField:'groupId',textField:'groupName',textToField:"groupName"}} ;
osEditor = {type:'combobox',options:{url:'/sim/asset/osList',valueField:'value',textField:'value',editable:false}} ;
assetCategoryEditor = {type:'combotree',options:{editable:false,url:'/sim/asset/assetCategories',panelHeight:300,textToField:'deviceTypeName',onHidePanel:ad.resetDeviceTypeText,onBeforeSelect:ad.beforeAssetCategoreSelect}} ;

var scanTimer = undefined ;
/**
 * 开始资产扫描
 * @param rescan
 */
ad.scanAsset = function(rescan){
	var networkAddress = $.trim($("#ad_networkAddress").val()) ;
	var netmask = $.trim($("#ad_netmask").val()) ;
	var nodeId = $("#ad_scanNodes").val() ;
	if(ad.valid(networkAddress,netmask,nodeId)==false){
		return ;
	}
	ad.removeScanTimer() ;
	$("#ad_discoveredAssets").datagrid("loadData",{total:0,rows:[]}) ;
	$("#ad_networkAddress,#ad_netmask,#ad_scanNodes").attr("disabled","disabled") ;
	$("#ad_startScan,#ad_rescan").each(function(){
		$(this).linkbutton("disable") ;
	});
	$("#ad_progress").css("display","block") ;
	ad.getDiscoverAssets(networkAddress, netmask,nodeId, rescan) ;
}
ad.valid = function(networkAddress,netmask,nodeId){
	var ad_networkAddress=$("#ad_networkAddress").val();
	var ad_netmask = $("#ad_netmask").val();
	if(ad_networkAddress == ""){
		showErrorMessage("网络地址不能为空，请输入！") ;
		return false ;
	}
	
	if(/^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/.test(networkAddress)==false){
		showErrorMessage("无效的网络地址！") ;
		return false ;
	}
	
	if(ad_netmask == ""){
		showErrorMessage("掩码不能为空，请输入！") ;
		return false ;
	}
	
	if(/^\d{1,2}$/.test(netmask)==false){
		showErrorMessage("无效的掩码地址，掩码地址只能包含一到两位整数！") ;
		return false ;
	}
	if(!nodeId){
		showErrorMessage("请选择扫描结点！") ;
		return false ;
	}
	return true ;
}
/**
 * 删除扫描定时器
 */
ad.removeScanTimer = function(){
	if(scanTimer){
		clearTimer(scanTimer) ;
		scanTimer = undefined ;
	}
}
ad.stopScan = function(){
	var networkAddress = $.trim($("#ad_networkAddress").val()) ;
	var netmask = $.trim($("#ad_netmask").val()) ;
	var nodeId = $("#ad_scanNodes").val() ;
	var param = {"networkAddress":networkAddress,"netmask":netmask,"scanNodeId":nodeId,"date":new Date().getTime()} ;
	$.getJSON("/sim/assetlist/stopDiscover",param,function(){}) ;
	ad.reset() ;
}
/**
 * 获取发现的资产列表
 * @param rescan 重新扫描
 */
ad.getDiscoverAssets = function(networkAddress,netmask,nodeId,rescan){
	var param = {"networkAddress":networkAddress,"netmask":netmask,"scanNodeId":nodeId,"rescan":rescan,"date":new Date().getTime()} ;
	$.getJSON('/sim/assetlist/assetDiscover',param,function(result){
		if(result.status){
			var currentRowCount = $("#ad_discoveredAssets").datagrid("getRows").length ; 
			$.each(result.result.rows,function(index,data){
				if(index >= currentRowCount){
					$("#ad_discoveredAssets").datagrid("appendRow",data) ;
				}
			}) ;
			$("#ad_progress").children(":first").css("width",result.result.progress+"%").html(result.result.progress+"%") ;
			if(result.result.progress >= 100){
				ad.reset() ;
			}else if(scanTimer == undefined){
				scanTimer = createTimer(function(){ad.getDiscoverAssets(networkAddress, netmask,nodeId, false)}, 1000,getSelectTabSeq()) ;
			}
		}else{
			ad.reset() ;
			showErrorMessage(result.message) ;
		}
	}) ;
}
ad.reset = function(){
	$("#ad_progress").css("display","none") ;
	$("#ad_progress").children(":first").css("width","0%") ;
	$("#ad_networkAddress,#ad_netmask,#ad_scanNodes").removeAttr("disabled","disabled") ;
	$("#ad_startScan,#ad_rescan").each(function(){
		$(this).linkbutton("enable") ;
	});
	ad.removeScanTimer() ;
}
ad.rowClickHandler = function(rowIndex,rowData){
	if(rowIndex != ad.lastEditRow){
		if(ad.lastEditRow != -1){
			ad.endRowEdit(ad.lastEditRow) ;
		}
		var table = $("#ad_discoveredAssets") ;
		table.datagrid("endEdit",ad.lastEditRow) ;
		table.datagrid("beginEdit",rowIndex) ;
		var editors = table.datagrid("getEditors",rowIndex) ;
		if(editors && editors.length > 0){
			$(editors[0].target).focus();
		}
	}
	ad.lastEditRow = rowIndex ;
}
ad.endRowEdit = function(editRowIndex){
	 var editors = $('#ad_discoveredAssets').datagrid('getEditors',editRowIndex);
	 var currentRow = $("#ad_discoveredAssets").datagrid("getRows")[editRowIndex] ;
	 for ( var i = 0; i < editors.length; i++) {
		var ed = editors[i] ;
		if(ed.type == "combobox"||ed.type=="combotree"){
			var options = $(ed.target).combo('options');
			var toField = options.textToField ; 
			if(toField){
				currentRow[toField] = $(ed.target).combo('getText') ;
			}
		}
	}
}
ad.operationFmt = function(value,row,rowIndex){
	return '<button class="btn btn-small" onclick="ad.addAsset('+rowIndex+')" style="margin:2px;"><i class="icon-add" style="width:16px;height:16px;margin-right:8px;"></i>添加</button>';
}
ad.addAsset = function(rowIndex){
	var table = $("#ad_discoveredAssets") ;
	var data = table.datagrid("getRows")[rowIndex] ;
	var url ="/sim/assetlist/saveAsset?operation=add" ;
	if(rowIndex == ad.lastEditRow){//如果要保存的行与当前编辑的是同一行，要结果当前行的编辑，将当前行编辑后的文本存储到行对象中
		ad.endRowEdit(ad.lastEditRow) ;
		table.datagrid("endEdit",rowIndex) ;
	}
	if(!ad.validData(data)){
		table.datagrid("beginEdit",rowIndex) ;
		return ;
	}
	data.state = 1 ;
	//data.group = data.groupId ;
	$.post(url,data,
		function(result){
			if(result.status){
				showAlertMessage('保存成功！') ;
			}else{
				showErrorMessage(result.message) ;
			}
		}
	,"json") ;
}
ad.validData = function(data){
	if(data.name == null || data.name == ""){
		showErrorMessage("资产名称不能为空!") ;
		return false ;
	}
	if(data.name.length > 30){
		showErrorMessage("资产名称不能超过30个字符!") ;
		return false ;
	}
	if(data.deviceType == null || data.deviceType == ""){
		showErrorMessage("资产类型不能为空!") ;
		return false ;
	}
	if(data.osName != null && data.osName.length > 30){
		showErrorMessage("操作系统不能超过30个字符!") ;
		return false ;
	}
	if(data.hostName != null && data.hostName.length > 30){
		showErrorMessage("主机名不能超过30个字符!") ;
		return false ;
	}
	if(data.linkman != null && data.linkman.length > 10){
		showErrorMessage("联系人不能超过10个字符!") ;
		return false ;
	}
	return true ;
}
ad.showScanHistory = function(node){
	var networkAddress = node.attributes.networkAddress ;
	var netmask = node.attributes.netmask ;
	$("#ad_networkAddress").val(networkAddress) ;
	$("#ad_netmask").val(netmask) ;
	$("#ad_scanNodes").val(node.attributes.nodeId) ;
	ad.scanAsset(false) ;
}
ad.scanHistoryFormatter = function(node){
	var attr = node.attributes ;
	var deleteFunction = "ad.deleteScanHistory('{0}','{1}',{2})".format(attr.scanHost,attr.networkAddress,attr.netmask) ;
	return "<span style='display:inline-block;width:120px;' title='" + attr.scanHost + "'>" + node.text + "</span>" + 
	       "<span class='icon-remove icon16' onclick=\""+deleteFunction+"\"></span>" ;
}
ad.deleteScanHistory = function(scanHost,networkAddress,netmask){
	var data = {scanHost:scanHost,networkAddress:networkAddress,netmask:netmask} ;
	$.post("/sim/assetlist/deleteScanHistory",data,function(result){
		$("#ad_scanHistory").tree("reload") ;
	}) ;
}
ad.fillSelect = function(selector,datas,deleteOldData){
	var select ;
	if($.type(selector)=="string"){
		select = $(selector) ;
	}else{
		select = selector ;
	}
	if(deleteOldData){
		select.children().remove() ;
	}
	for(var i=0;i<datas.length;i++){
		var optionData = datas[i] ;
		if(optionData.name){
			var option = $("<option/>").attr("value",optionData.value).html(optionData.name) ;
			$.each(optionData,function(fieldName,fieldValue){
				if(fieldName == "name"){
					option.html(fieldValue) ;
				}else{
					option.attr(fieldName,fieldValue) ;
				}
			}) ;
			select.append(option) ;
		}
	}
	return select ;
}
$(function(){
	$.getJSON("/sim/node/allNode?"+"_time=" + new Date().getTime(),function(result){
		if(result){
			var datas = $.map(result,function(item){
				var data = {name:item.ip,value:item.nodeId} ;
				return data ;
			}) ;
			ad.fillSelect("#ad_scanNodes", datas, true) ;
		}
	}) ;
}) ;
