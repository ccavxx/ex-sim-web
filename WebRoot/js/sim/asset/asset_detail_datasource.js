/**
 * ds表示asset datasource
 */
var ds = {
};

ds.currentOpenDialog = undefined ;
ds.addDataSource = function(tabSeq,ip){
	var dialogId = "add_datasource_dialog"+tabSeq ;
	ds.currentOpenDialog = dialogId ;
	$("<div class='horizonCss-dialog-centerDiv'/>").dialog({
		id:dialogId,
		title:"新建日志源",
		width:650,
		height:600,
		modal:true,
		border:false,
		buttons:[{text:"保存",
			handler:function(){
				ds.saveDataSource();
			}
		},{
			text:"取消",
			handler:function(){
				$("#"+dialogId).dialog("close");
			}
		}],
		href:'/sim/datasource/showAdd?ip='+ip,
		onClose:function(){
			 $(this).remove() ;
		}
	});
};
ds.deviceTypeHelp = function() {
	var dataSourceType = $("#dsa_dataSourceType").combo("getValue");
	var name = "";
	dataSourceType = dataSourceType ? dataSourceType : deviceTypeGlobal;
	
	var dataTypes = dataSourceType.split("/");
	if(dataTypes && dataTypes.length >= 2) {
		name =  ("&name=" + dataTypes[0] + "/" + dataTypes[1]);
	}
	window.open(encodeURI("/sim/productsupport/productsupportHelp?asset=asset" + name), '_blank');
}
ds.saveDataSource = function(){
	var selectNodes = $("#dsa_nodes option:selected");
	if(selectNodes.length > 0){
		if(selectNodes.attr("online") == "false"){
			$.messager.confirm("确认","收集节点"+selectNodes.attr("nodeName")+"已经掉线，是否继续保存当前日志源？",function(result){
				if(result){
					$("#dsa_form").submit();
				}
			}) ;
		}else{
			$("#dsa_form").submit();
		}
	}else{
		$("#dsa_form").submit();
	}
};

ds.deleteDataSource = function(id,title){
	$.messager.confirm("确认","你确定要删除日志源“"+title+"”吗?",function(r){
		if(r){
			$.getJSON("/sim/datasource/delete?id="+id,function(result){
				if(result.status){
					if(title){				
						$('#adt_asset_datasource'+getSelectTabSeq()).accordion("remove",title);
					}
				}else{
					showErrorMessage(result.message);
				}
			});
		}
	}) ;
};
ds.editDataSource = function(resourceId){
	var dialogId = "edit_datasource_dialog" ;
	ds.currentOpenDialog = dialogId ;
	$("<div class='horizonCss-dialog-centerDiv'/>").dialog({
		id:dialogId,
		title:"编辑日志源",
		width:650,
		height:600,
		modal:true,
		border:false,
		buttons:[{text:"保存",
			handler:function(){
				ds.saveDataSource();
			}
		},{
			text:"取消",
			handler:function(){
				$("#"+dialogId).dialog("close");
			}
		}],
		href:'/sim/datasource/showEdit?id='+resourceId,
		onClose:function(){
			 $(this).remove() ;
		}
	});	
}
ds.displayPathName = function(){
	var cbt = $(this) ; 
	var selectNode = cbt.combotree("tree").tree("getSelected") ;
	cbt.combotree("setText",selectNode.pathName) ;
};