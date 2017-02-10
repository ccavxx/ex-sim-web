var simEventQueryHandler = {};
//根据用户输入条件，查询事件信息
function doEventQuery() {
	var  priority = eventQueryForm.dealFormPriority(), /** 优先级* */
	fields = null,
	queryParam = {
		"startTime" :evtQueryForm.startTime.value,
		"endTime" : evtQueryForm.endTime.value,
		"priority" : evtQueryForm.level.value,
		"eventName" : evtQueryForm.eventName.value,
		"ruleName":evtQueryForm.ruleName.value,
		"ip" : evtQueryForm.ip.value,
		"category1" : eventQueryForm.category1,
		"category2" : eventQueryForm.category2,
		"requestIp":simEventQueryHandler.targetIp,
		"fields" : fields,
		"header" : null
	};
	if(!priority){
		delete queryParam['priority'];
	}
	
	eventQuery.queryEvent(queryParam,"#event_query_table_id","");
	eventQuery.expandTimeline(queryParam);
	return true;
}
//此方法用于返回上一次查询
function backSearch(){
	var str=savetime[savetime.length-1];
	savetime.pop();
	var dateArr=str.split("&");
	var startdate=dateArr[0];
	var enddate=dateArr[1];
	$("#startTimeId").val(startdate);
	$("#endTimeId").val(enddate);
	var  priority = eventQueryForm.dealFormPriority(), /** 优先级* */
	fields = null,
	queryParam = {
		"startTime" :evtQueryForm.startTime.value,
		"endTime" : evtQueryForm.endTime.value,
		"priority" : evtQueryForm.level.value,
		"eventName" : evtQueryForm.eventName.value,
		"ruleName":evtQueryForm.ruleName.value,
		"ip" : evtQueryForm.ip.value,
		"category1" : eventQueryForm.category1,
		"category2" : eventQueryForm.category2,
		"requestIp":simEventQueryHandler.targetIp,
		"fields" : fields,
		"header" : null
	};
	if(!priority){
		delete queryParam['priority'];
	}
	 eventQuery.queryEvent(queryParam,"#event_query_table_id");
	eventQuery.expandTimeline(queryParam);
	if(savetime.length==0){
		$("#event_floatDiv").hide();
	}
}
//重置form表单数据
function doReset(){
	evtQueryForm.level.value="";
	evtQueryForm.ip.value="";
	var selectNode=$('#tt').tree("getSelected");
	var selectRule=$('#tt1').tree("getSelected");
	if(!selectNode && !selectRule){
		evtQueryForm.eventName.value="";
	}else if(selectNode){
		var nodeType=selectNode.attributes['type'];
		if(nodeType!="3"){
			evtQueryForm.eventName.value="";
		}
	}
}
var eventQuery=null;
var eventQueryForm=null;

(function(){
	 //初始化加载数据和返回按钮跳转
 	var targetId = simHandler.superioripId;
 	simEventQueryHandler.targetIp = targetId.replace(/[_]/g, ".");
 	$("#sysconfigsuperiorEventPanelId").panel({
 		headerCls:'sim-panel-header',
 		fit:true,
 		title:"子节点事件查询（" + simEventQueryHandler.targetIp + "）",
 		tools:[{
 					iconCls:'icon-cancel',
 					text:'返回',
 					handler:function(event){
 						simHandler.goSuperiorListBack(event);
 					}
 				}]
 	});
    eventQuery=new EventQuery();
    eventQueryForm=new EventQueryForm();
    //初始化加载列集 
	eventQuery.initColumns({"json":"evt_query_colums"});
	
    function initEventData(){
    	 /*初始化加载最近一个小时的数据*/
    		$('#startTimeId').val(moment(simHandler.serverTime).subtract('days',1).format('YYYY-MM-DD HH:mm:ss'));
			$('#endTimeId').val(moment(simHandler.serverTime).format('YYYY-MM-DD HH:mm:ss'));
			 eventQuery.queryEvent({"startTime":$('#startTimeId').val(),"endTime":$('#endTimeId').val(),"requestIp":simEventQueryHandler.targetIp},"#event_query_table_id",'') ;
			eventQuery.expandTimeline({"startTime":$('#startTimeId').val(),"endTime":$('#endTimeId').val(),"requestIp":simEventQueryHandler.targetIp});
	}
	initEventData();   
	$('#tt').tree({//类型树导航事件
		url:'/sim/eventRestQuery/eventCategory?requestIp='+simEventQueryHandler.targetIp,
		onClick: function(node){
			if(node.attributes){
				 var nodeType=node.attributes['type'],/*id=node.attributes['id'],*/text=node.attributes['realName'],queryParam={};
				 evtQueryForm.eventName.value="";
				 evtQueryForm.ruleName.value="";
				 evtQueryForm.level.value="";
				 evtQueryForm.ip.value="";
				 queryParam['startTime']= $('#startTimeId').val();
				 queryParam['endTime']=$('#endTimeId').val();
				 queryParam['requestIp']=simEventQueryHandler.targetIp;
				 switch (parseInt(nodeType)) {
					case 1:
						eventQueryForm.category1=queryParam['category1']=text;
						eventQueryForm.category2=queryParam['category2']="";
						$("#event_name").show();
						$("#eventname_input").show();
						break;
					case 2:
						var p=$('#tt').tree("getParent",node.target);
						eventQueryForm.category1=queryParam['category1']=p.attributes['realName'];
						eventQueryForm.category2=queryParam['category2']=text;
						$("#event_name").show();
						$("#eventname_input").show();
						break;
					case 3:
						var p=$('#tt').tree("getParent",node.target),  pp=$('#tt').tree("getParent",p.target);;
						eventQueryForm.category1=queryParam['category1']=pp.attributes['realName'];
						eventQueryForm.category2=queryParam['category2']=p.attributes['realName'];
 						queryParam['eventName']=eventQueryForm.eventName=evtQueryForm.eventName.value=text;
 						$("#event_name").hide();
						$("#eventname_input").hide();
						break;
					default:
						break;
				}
				 
				 eventQuery.queryEvent(queryParam,"#event_query_table_id","");
				 eventQuery.expandTimeline(queryParam);
				 eventQuery._tab_close();
				 if(!queryParam['startTime']){
				 		doReset();
				 }
			}
		},
		loadFilter:function(data,parent){
			var url ;
			var parentNode = parent ? $("#tt").tree("getNode",parent) : null ;
			if(!parentNode){//统计一级分类
				url = "/sim/eventRestQuery/cat1Statistic?requestIp="+simEventQueryHandler.targetIp+"&_time"+new Date().getTime() ;
			}else if(parentNode.attributes['type'] == "1"){//统计二级分类
				url = "/sim/eventRestQuery/cat2Statistic?requestIp="+simEventQueryHandler.targetIp+"&cat1="+encodeURI(parentNode.attributes["realName"])+"&_time"+new Date().getTime() ;
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
	//初始化加载规则名称
	$('#tt1').tree({
		url:'/sim/eventRestQuery/eventRule?requestIp='+simEventQueryHandler.targetIp,
		onClick: function(node){
				 var /*id=node.attributes['id'],*/queryParam={};
				 queryParam['ruleName']= node.attributes['realName'];
				 queryParam['startTime']= $('#startTimeId').val();
				 queryParam['endTime']=$('#endTimeId').val();
				 evtQueryForm.level.value="";
				 evtQueryForm.ip.value="";
				 $("#event_name").hide();
				 $("#eventname_input").hide();
				 queryParam.requestIp=simEventQueryHandler.targetIp;
				 evtQueryForm.ruleName.value=queryParam['ruleName'];
				 evtQueryForm.eventName.value="";
				 eventQueryForm.category1="";
				 eventQueryForm.category2="";
				 eventQuery.queryEvent(queryParam,"#event_query_table_id");
				 eventQuery.expandTimeline(queryParam);
				 if(!queryParam['startTime']){
				 		doReset();
				 }
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
