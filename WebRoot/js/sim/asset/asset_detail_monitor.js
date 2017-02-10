/**
 * mnt为monitor的缩写
 */
var mnt = {
}

mnt.currentOpenDialog = undefined ;
mnt.addMonitor = function(tabSeq,ip){
	var dialogId = "add_monitor_dialog"+tabSeq ;
	mnt.currentOpenDialog = dialogId ;
	$("<div class='horizonCss-dialog-centerDiv'/>").dialog({
		id:dialogId,
		title:"新建监视对象",
		width:650,
		height:600,
		modal:true,
		border:false,
		buttons:[{text:"保存",
			handler:function(){
			  mnt.saveMonitor();
			}
		},{
			text:"取消",
			handler:function(){
			  $("#"+dialogId).dialog("close");
			}
		}],
		href:'/sim/monitor/showAdd?ip='+ip,
		onClose:function(){
			 $(this).remove() ;
		}
	});	
};
mnt.saveMonitor = function (){
	$("#ma_form").submit();
};
mnt.editMonitor = function(resourceId){
	var dialogId = "add_monitor_dialog"+tabSeq ;
	mnt.currentOpenDialog = dialogId ;
	$("<div class='horizonCss-dialog-centerDiv'/>").dialog({
		id:dialogId,
		title:"编辑监视对象",
		width:650,
		height:600,
		modal:true,
		border:false,
		buttons:[{text:"保存",
			handler:function(){
			  mnt.saveMonitor();
			}
		},{
			text:"取消",
			handler:function(){
			  $("#"+dialogId).dialog("close");
			}
		}],
		href:'/sim/monitor/showEdit?id='+resourceId,
		onClose:function(){
			 $(this).remove() ;
		}
	});	
};
mnt.deviceTypeHelp = function(){
	var dataSourceType = $("#ma_monitorType").combo("getValue");
	var name = "";
	dataSourceType = dataSourceType ? dataSourceType : deviceTypeGlobal;
	
	dataSourceType = dataSourceType.replace("Monitor", "");
	var dataTypes = dataSourceType.split("/");
	if(dataTypes && dataTypes.length >= 2) {
		name =  ("&name=" + dataTypes[0] + "/" + dataTypes[1]);
	}
	
	window.open(encodeURI("/sim/productsupport/productsupportHelp?asset=asset" + name), '_blank');
}
mnt.deleteMonitor = function(id,title){
	$.messager.confirm("确认","你确定要删除监视对象“"+title+"”吗?",function(r){
		if(r){
			$.getJSON("/sim/monitor/delete?id="+id,function(result){
				if(result.status){
					if(title){
						$('#adt_asset_monitor'+getSelectTabSeq()).accordion("remove",title);
					}
					var ps = $("#adt_asset_monitor"+getSelectTabSeq()).accordion("panels");
					if(ps.length==0){
						$("#adt_asset_monitor_tb"+getSelectTabSeq()).css('display','block');
					}
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}
	}) ;	
	
};
mnt.createAlarmPolicy = function(tabSeq,monitorId,securityObjectType){
	var dialogId = "add_alarm_policy_dialog"+tabSeq ;
	mnt.currentOpenDialog = dialogId ;
	$("<div/>").dialog({
		id:dialogId,
		title:"告警规则配置",
		width:600,
		height:600,
		modal:true,
		border:false,
		buttons:[{
			text:"保存",
			handler:function(){
			  mnt.saveAlarmPolicy(tabSeq);
			}
		},{
			text:"取消",
			handler:function(){
			  $("#"+dialogId).dialog("close");
			}
		}],
		href:'/sim/monitor/showAddAlarmPolicy?monitorId='+monitorId+'&securityObjectType='+securityObjectType,
		onClose:function(){
			 $(this).remove() ;
		}
	});
};
mnt.editCreateAlarmPolicy = function(event,tabSeq,monitorId,securityObjectType,id){
	if(event.preventDefault){
		event.preventDefault();
	}
	if(event.stopPropagation){
		event.stopPropagation();
	}
	var dialogId = "add_alarm_policy_dialog"+tabSeq ;
	mnt.currentOpenDialog = dialogId ;
	$("<div/>").dialog({
		id:dialogId,
		title:"编辑告警规则配置",
		width:600,
		height:600,
		modal:true,
		border:false,
		href:'/sim/monitor/showEditAlarmPolicy?monitorId='+monitorId+'&securityObjectType='+securityObjectType+'&id='+id,
		onClose:function(){
			$(this).remove() ;
		},
		buttons:[{
			text:"保存",
			handler:function(){
				mnt.saveAlarmPolicy(tabSeq);
			}
		},{
			text:"取消",
			handler:function(){
				$("#"+dialogId).dialog("close");
			}
		}]
	});

};
mnt.deleteAlarmPolicy = function(event,id,name){
	if(event.preventDefault){
		event.preventDefault();
	}
	if(event.stopPropagation){
		event.stopPropagation();
	}
	$.messager.confirm('确认', '你确定要删除“'+name+'”告警策略吗?', function(r){
		if (r){
			$.getJSON("/sim/monitor/deleteAlarmPolicy?id="+id,function(result){
				if(result.status){
					getTabElement("adt_monitor").panel("refresh") ;
				}else{
					showErrorMessage(result.message) ;
				}
			}) ;
		}
	});

}
mnt.saveAlarmPolicy = function(tabSeq){
	$("#aap_form").submit();
};