var eventQuery = null;
var eventQueryForm = null;
function showQueryEvent() {
	$("<div id='evt_eq_div_id'></div>").dialog({   
	    title: '查询',   
	    width: 500,   
	    closed: false,   
	    cache: false,  
	    resizable:true,
	    iconCls:"icon-search",
	    href: '/page/event/eventQueryForm.html',   
	    modal: true ,
		onClose : function() {
			$(this).dialog("destroy");
		},
		onOpen:function(){
			//dataSourceDeviceTree.clean();// 清除数据源数据
		}
	});
	/**
	 * 重新调整位置
	 */
	$("#evt_eq_div_id").dialog("move",{top:$(document).scrollTop()+($(window).height()-600)*0.5});
}
//此方法用于返回上一次查询
function backSearch(){
	var str = savetime[savetime.length-1];
	savetime.pop();
	var eventName = eventQuery.queryParams.query_event_Name ? eventQuery.queryParams.query_event_Name :
						(evtQueryForm.eventName.value ? evtQueryForm.eventName.value : '');
	var dateArr = str.split("&");
	var startdate = dateArr[0];
	var enddate = dateArr[1];
	$("#startTimeId").val(startdate);
	$("#endTimeId").val(enddate);
	fields = null,
	queryParam = {
		"startTime" :evtQueryForm.startTime.value,
		"endTime" : evtQueryForm.endTime.value,
		"priority" : evtQueryForm.level.value,
		"eventName" : eventName,
		"ruleName":evtQueryForm.ruleName.value,
		"ip" : evtQueryForm.ip.value,
		"category1" : eventQueryForm.category1,
		"category2" : eventQueryForm.category2,
		"fields" : fields,
		"header" : null
	};
	eventQuery.queryEvent(queryParam,"#event_query_table_id");
	eventQuery.expandTimeline(queryParam);
	if(savetime.length == 0){
		$("#event_floatDiv").hide();
	}
}
function doEventQuery() {
	eventQuery.setCondition();
	return true;
}

function doEventExport() {
	eventQuery.exportEvents(function(data) {
	  if (data && data.filepath) {
		  var event_form = $("<form id='event_export_form_Id' style='display:none' action='/sim/eventQuery/downloadfile'><input type='hiden' name='filename' value='"
					+ data.filepath + "'/></form>");
		  $("body").append(event_form);
		  event_form.submit();
		  $("body").remove("#event_export_form_Id");
	  }
	});

}
function doEventReset(){
	doReset();
	doEventQuery();
}
function doReset(){
	eventQuery.resetQuery();
	evtQueryForm.level.value = "";
	evtQueryForm.ip.value = "";
	var selectNode = $('#tt').tree("getSelected");
	var selectRule = $('#tt1').tree("getSelected");
	if(!selectNode && !selectRule){
		evtQueryForm.eventName.value= "";
	}else if(selectNode){
		var nodeType=selectNode.attributes['type'];
		if(nodeType != "3"){
			evtQueryForm.eventName.value = "";
		}
	}
}

function doConfirm(state) {
	var selectRows = $("#event_query_table_id").datagrid("getChecked");
	if(selectRows.length == 0){
		showAlertMessage("请选择要关闭的事件！");
	} else if(state == 1){
		$.messager.confirm("确认", "你确定要关闭选中事件吗？", function(r){
			if(!r) return;
			changeConfirmState(selectRows, state);
			$("#event_query_table_id").datagrid("uncheckAll");
		});
	} else {
		$.messager.confirm("确认","你确定要关闭选中事件吗？", function(r){
			if(!r) return;
			changeConfirmState(selectRows, state);
			$("#event_query_table_id").datagrid("uncheckAll");
		});
	}
}

changeConfirmState = function(selectRows, state){
	var ids = "";
	$.each(selectRows, function(index, row){
		ids = ids + row.EVENT_ID +",";
	});
	$.getJSON('/sim/eventQuery/eventConfirm?ids=' + ids + '&state=' + state, {_time:new Date().getTime()}, function(result){
		$("#event_query_table_id").datagrid("reload");
		if(!result.status){
			showErrorMessage(result.message);
		}
	});
};

(function(){
    eventQuery = new EventQuery();
    eventQueryForm = new EventQueryForm();
	eventQuery.initColumns({"json":"evt_query_colums"});
	/*初始化加载最近一个小时的数据*/
    function initEventData(){
    	var _param = simHandler.indexEventQuery;
    	if(_param.hasOwnProperty('startTime') && _param.hasOwnProperty('endTime')){
    		$('#startTimeId').val(_param.startTime);
			$('#endTimeId').val(_param.endTime);
    		eventQuery.createDataGrid(_param,"#event_query_table_id") ;
    		eventQuery.expandTimeline({'name':_param.name,'startTime':_param.startTime,'endTime':_param.endTime});
//    		simHandler.indexEventQuery = null;
    	}else{
			$('#startTimeId').val(moment(simHandler.serverTime).subtract('days',1).format('YYYY-MM-DD HH:mm:ss'));
			$('#endTimeId').val(moment(simHandler.serverTime).format('YYYY-MM-DD HH:mm:ss'));
			var startTime = $('#startTimeId').val();
			var endTime = $('#endTimeId').val();
			var params = {"startTime":startTime,"endTime":endTime};
			eventQuery.createDataGrid(params,"#event_query_table_id") ;
			eventQuery.expandTimeline(params);
    	}
	}

	initEventData();   
	
	$('#tt').tree({//类型树导航事件
		url:'/sim/eventQuery/eventCategory',
		onClick: function(node){
			if(node.attributes){
			   var nodeType = node.attributes['type'],text = node.attributes['realName'],queryParam = {};
			   evtQueryForm.eventName.value = "";
			   evtQueryForm.ruleName.value = "";
			   evtQueryForm.level.value = "";
			   evtQueryForm.ip.value = "";
			   queryParam['startTime'] = $('#startTimeId').val();
			   queryParam['endTime'] = $('#endTimeId').val();
			   switch (parseInt(nodeType)) {
					case 1:
						eventQueryForm.category1 = queryParam['category1'] = text;
						eventQueryForm.category2 = queryParam['category2'] = "";
						$("#event_name").show();
						$("#eventname_input").show();
						break;
					case 2:
						var p = $('#tt').tree("getParent",node.target);
						eventQueryForm.category1 = queryParam['category1'] = p.attributes['realName'];
						eventQueryForm.category2 = queryParam['category2'] = text;
						$("#event_name").show();
						$("#eventname_input").show();
						break;
					case 3:
						var p = $('#tt').tree("getParent",node.target), pp = $('#tt').tree("getParent",p.target);;
						eventQueryForm.category1 = queryParam['category1'] = pp.attributes['realName'];
						eventQueryForm.category2 = queryParam['category2'] = p.attributes['realName'];
						$("#event_name").hide();
						$("#eventname_input").hide();
 						queryParam['eventName'] = eventQueryForm.eventName = evtQueryForm.eventName.value = text;
						break;
					default:
						break;
				}
				 eventQuery.queryEvent(queryParam,"#event_query_table_id");
				 eventQuery.expandTimeline(queryParam);
				 eventQuery._tab_close();
				 doReset();
			}
		},
		loadFilter:function(data,parent){
			var url ;
			var parentNode = parent ? $("#tt").tree("getNode",parent) : null ;
			if(!parentNode){//统计一级分类
				url = "/sim/eventQuery/cat1Statistic?_time"+new Date().getTime() ;
			}else if(parentNode.attributes['type'] == "1"){//统计二级分类
				url = "/sim/eventQuery/cat2Statistic?cat1="+encodeURI(parentNode.attributes["realName"])+"&_time"+new Date().getTime() ;
			}else{//
				return data ;
			}
			setTimeout(function(){
				$.getJSON(url,function(result){
					if(!result || result.length == 0){
						return ;
					}
					var catTree = $("#tt") ;
					var childNode ;
					if(parent != undefined && parent != null){//不能替换为!parent
						childNode = catTree.tree("getChildren",parent) ;
					}else{
						childNode = catTree.tree("getRoots") ;
					}
					if(childNode.length > 0){
						for(var i=0;i<childNode.length;i++){
							var node = childNode[i] ;
							var count = 0 ;
							for(var j=0;j<result.length;j++){
								if(result[j].cat == node.attributes["realName"]){
									count = result[j].counts ; 
									break ;
								}
							}
							catTree.tree("update",{target:node.target,text:node.attributes["realName"]+"("+count+")"});
						}
					}
				}) ;
			}, 500);
			return data;
		} 
	});
	
	$('#tt1').tree({
		url:'/sim/eventQuery/eventRule',
		onClick: function(node){
			 var queryParam = {};
			 queryParam['ruleName'] = node.attributes['realName'];
			 queryParam['startTime'] = $('#startTimeId').val();
			 queryParam['endTime'] = $('#endTimeId').val();
			 evtQueryForm.level.value = "";
			 evtQueryForm.ip.value = "";
			 $("#event_name").hide();
			 $("#eventname_input").hide();
			 evtQueryForm.ruleName.value = queryParam['ruleName'];
			 evtQueryForm.eventName.value = "";
			 eventQueryForm.category1 = "";
			 eventQueryForm.category2 = "";
			 eventQuery.queryEvent(queryParam,"#event_query_table_id");
			 eventQuery.expandTimeline(queryParam);
			 doReset();
		}
	});
	
	/**
	 * 重新修正回溯结果显示DIV的高度
	 */
	 var m=function(){
			var h=$("#event_query_main_div_id").height();
			if((h-360)>50){
				$("#event_correl_id").css("height",""+(h-350)+"px");
				return;				
			}
			setTimeout(m,500);
		};
	 setTimeout(m,0); 
})();
