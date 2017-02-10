(function(){
	EventQuery.prototype.createTabsLayout=function(){return $("<div/>").appendTo($("#event_div_id"));};//重写回溯显示布局
	EventQuery.prototype._dbClickRowHandler=function(rowIndex, rowData){
		$("#main_layout_id").layout('collapse','north');  
		this.doCorrelateOnRow(rowData);
	};
	EventQuery.prototype.doCorrelateOnRowIndex = function(rowIndex){
		if(arguments.length > 1 && arguments[1]){
			$("#main_layout_id").layout('collapse','north');
		}
		this.doCorrelateOnRow($(this.mainDataTableId).datagrid("getRows")[rowIndex]) ;
	};
})();


var eventQuery=new EventQuery(),
	eventPrioritySum=new EventPrioritySum(),
	eventCategory=new EventCategory(),
	eventTrend=new EventTrend();

(function() {
	eventQuery.initColumns({"json":"evt_query_colums"}, true);
	// doChangeCgyCart(0);
	$("#panelTools_bar").click();
	eventPrioritySum.initLabel();
	// doChangePryCart(0);
	$("#panelTools1_bar").click();
	// doChangeTrdCart(0);
	$("#panelTools2_line").click();
 	/**
 	 * 页面加载默认查询趋势数据
 	 */
 	doLoadDefaultTrendGd();
})();

function doLoadDefaultTrendGd(){
	eventTrend.loadTrend(function(id,data){
 		var starttime=eventCategory.condition.startTime,
 			endtime=eventCategory.condition.endTime,
 			title="事件信息：时间-["+starttime+"到"+endtime+"]";
		// eventQuery.initColumns({"json":"evt_query_colums"});//定制列集
		eventQuery.createDataGrid({"startTime":starttime,"endTime":endtime},"#event_table_id",title);
 	}, null);
}

function sortCountByDesc(_values){
	var l=_values.length,temp=null;
	for(var i=0;i<l&&l>1;i++){//排序 降序  
		for(var j=i+1;j<l;j++){
			if(_values[i].opCount<_values[j].opCount){
				temp=_values[i];
				_values[i]=_values[j];
				_values[j]=temp;
			}
		}
	}
}

function doChangeCgyCartByScope(unit){
	eventCategory.timeUnit = unit;
	eventCategory.setEventPanelTitle("_event_category_id", unit);
	doChangeCgyCart(eventCategory.typeCgy);
}
function onMainLayoutExpand(){
	try{
		if($.browser.msie && parseFloat($.browser.version) < 9){
			$("#_event_category_id .highcharts-series > div").css("left","0px");
			$("#_event_level_id .highcharts-series > div").css("left","0px");
		}
	}catch(e){
	}
} 
/**
 * 事件种类柱状图 表格
 * @param _ct
 */
function doChangeCgyCart(_ct){
	$("#_event_category_id").empty();
 	$("<div id='_event_category_cg_id'  style='margin: 0 auto;'></div>").appendTo("#_event_category_id");
	var showQueryfrom=function(c){
		//eventQuery.initColumns({"json":"evt_query_colums"});
		var starttime=eventCategory.condition.startTime,
	          endtime=eventCategory.condition.endTime,
	          title="事件信息：分类-["+c +"]-时间-["+starttime+"到"+endtime+"]";
	 	eventQuery.queryEvent({"startTime":starttime,"endTime":endtime,"category1":c},"#event_table_id",title);
	};
	//饼图
	if(_ct==0){
		$("#_event_category_cg_id").css({"height":"297px"});
		eventCategory.loadCategorys(function(_id,_values){
			   var c1d = {'categories':[],'data':[]};
			   $.each(_values,function(i,c){
				   c1d.categories.push(c['cat1id']);
				   c1d.data.push({name: c['cat1id'], value: c['opCount']});
			   });
			   loadPie(_id,c1d, showQueryfrom);
		}, "#_event_category_cg_id") ; 
		eventCategory.typeCgy = 0;
	}
	//柱图
	if(_ct==1){
		$("#_event_category_cg_id").css({"height":"297px"});
		eventCategory.loadCategorys(function(_id,_values){//加载柱状图
			var c2d={'categories':[],'data':[]};
			if(_values){
				sortCountByDesc(_values);
				$.each(_values,function(k,v){
					c2d.categories.push(v['cat1id']);
					c2d.data.push(v['opCount']);
				});
			}
			loadColums(_id,c2d,function(c,y){showQueryfrom(c);});
			
		}, "#_event_category_cg_id") ;
		eventCategory.typeCgy = 1;
	}
	//数据列表
	if(_ct==2){
		eventCategory.loadCategorys(function(_id,_values){//加载表格
			var $table=$("<table></table>").appendTo("#_event_category_cg_id"),fvalues=[];
			$.each(_values,function(i,_val){
				 fvalues.push({category:_val["cat1id"],value:_val["opCount"]});
			});
			sort(fvalues,"value","desc") ;
			$table.datagrid({   
				fit:true,
				fitColumns:true,
				singleSelect : true,
			   	data:fvalues,
			   	border:false,
			    columns:[[   
			        {field:'category',title:'类型',width:50},   
			        {field:'value',title:'数量',width:50} 
			    ]],
			    onClickRow:function(rowIndex, rowData){
			    	showQueryfrom(rowData['category']);
			    }
			});  
		},"#_event_category_cg_id");
		eventCategory.typeCgy = 2;
	}
}

function doChangePryCartScope(unit){
	eventPrioritySum.timeUnit = unit;
	eventCategory.setEventPanelTitle("_event_level_id", unit);
	doChangePryCart(eventCategory.typePry);
}
/**
 * 优先级 柱状图 表格
 * @param _ct
 */
function doChangePryCart(_ct){
	$("#_event_level_id").empty();
 	$("<div id='_event_level_cg_id'  style='margin: 0 auto; height: 297px;'></div>").appendTo("#_event_level_id");
 	var showQueryfrom=function(c){
 		//eventQuery.initColumns({"json":"evt_query_colums"});
		var starttime=eventPrioritySum.condition.startTime,
	          endtime=eventPrioritySum.condition.endTime, 
	          title="事件信息：级别-["+c +"]-时间-["+starttime+"到"+endtime+"]";
	    var ic=0;
	    $.each(eventPrioritySum.labelInfo,function(i,lb){
	    	if(lb['text']==c){
	    		ic=lb['id'];
	    		return;
	    	}
	    });
	 	eventQuery.queryEvent({"startTime":starttime,"endTime":endtime ,"priority":""+ic+""},"#event_table_id",title);
 	},colors=simHandler.colors;
 	
 	if(_ct==0){
 		eventPrioritySum.loadPrioritys(function(_id,_values){
 		   var piedata={'categories':[],'data':[]};
 	       $.each(_values,function(i,p){
 	       		$.each(eventPrioritySum.labelInfo,function(j,pt){
 	       			if(pt['text']==p['name']){
 	       				piedata.categories.push( p['name']);
 			       		piedata.data.push(
 				       		 {
 			                    name: p['name'],
 			                    value: p['value'],
 			                 }
 			       		);
 	       			}
 	       			
 	       		});
 	       });
 		   loadPie(_id,piedata, showQueryfrom);
 		},"#_event_level_cg_id");
 		eventCategory.typePry = 0;
 	}
 	
	if(_ct==1){
		$("#_event_level_cg_id").css({"height":"297px"});
		eventPrioritySum.loadPrioritys(function(_id,_values){
		var c2d={'categories':[],'data':[]};
		if(_values){
			 sortCountByDesc(_values);
			 $.each(_values,function(i,_val){
				c2d.data.push(_val['value']);
				c2d.categories.push(_val['name']);
			 });
		}
		loadColums(_id,c2d,function(c,y){showQueryfrom(c);});
		
		}, "#_event_level_cg_id");
		eventCategory.typePry = 1;
	}
	
	if(_ct==2){
		eventPrioritySum.loadPrioritys(function(_id,_values){//加载表格
				var $table=$("<table></table>").appendTo("#_event_level_cg_id"),fvalues=[];
				sort(_values,"value","desc") ;
				$.each(_values,function(i,_val){
					fvalues.push({priority:_val["name"],count:_val["value"]});
				});
				$table.datagrid({ 
					fit:true,
					fitColumns:true,
					singleSelect : true,
				   	data:fvalues,
				   	border:false,
				    columns:[[   
				        {field:'priority',title:'级别',width:50},   
				        {field:'count',title:'数量',width:50},   
				    ]],
				    onClickRow:function(rowIndex, rowData){
				    	showQueryfrom(rowData['priority']);
				    }
				});  
			},"#_event_level_cg_id");
		eventCategory.typePry = 2;
	}
}

/**
 * 事件趋势
 */

function doChangeTrdCartScope(unit){
	eventTrend.condition.scope = unit;
	eventCategory.setEventPanelTitle("_event_trend_id", unit);
	doChangeTrdCart(eventCategory.typeTrd);
}
function doChangeTrdCart(_ct){
	$("#_event_trend_id").empty();
	$("<div id='_event_trend_cg_id'></div>").appendTo("#_event_trend_id");
//	$("<div id='_event_trend_cg_id'  style='margin: 15px auto;'></div>").appendTo("#_event_trend_id");
 	var showQueryfrom=function(c){
		var starttime=eventTrend.condition.startTime,
	          endtime=eventTrend.condition.endTime,
	          title="事件信息：分类-["+c +"]-时间-["+starttime+"到"+endtime+"]";
	 	eventQuery.queryEvent({"startTime":starttime,"endTime":endtime,"eventName":c},"#event_table_id",title);
	};
 	
 	if(_ct == 0) {
 		eventTrend.loadTrend(function(_id, values) {
 		 	var dc0 = $("<div></div>")/*用于调整趋势图位置*/,
 		 		dc1 = $('<div id="detail-trend-m-container">');
 		 	
 		 	dc0.appendTo($(_id));
 		 	dc1.css({"height" : "290px"})/*css({"width":680,"height":240}).*/.appendTo(dc0);
 		 	loadMTrand('#'+dc1.attr('id'), values, showQueryfrom);
 		}, "#_event_trend_cg_id");
		eventCategory.typeTrd = 0;
 	}
 	
	if(_ct==1){
		eventTrend.loadTrend(function(_id,values){
			var c2d={'categories':[],'data':[]};
			$.each(eval(values), function(i, node) {
				var temp = {
						name : i,
						type: 'line',
						smooth:true,
						showAllSymbol: true,
//						symbolSize:2,
						data : []
				};
				$.each(node, function(i, n) {
					temp.data.push([new Date(n.time), n.value]);
				});
				c2d.categories.push(i);
				c2d.data.push(temp);
			});
			var dc0=$("<div></div>")/*用于调整趋势图位置*/;
		 	dc0.css({"padding-left":"-30px","height":"290px"}).appendTo($(_id));
//		 	dc0.css({"padding-top":"35px","padding-left":"-30px","height":"200px"}).appendTo($(_id));
	
		  	loadTrendLine(dc0,c2d,function(c,y){
		 			showQueryfrom(c);
		 		}
		 	); 
			
			
		}, "#_event_trend_cg_id");
		eventCategory.typeTrd = 1;
	}
	
	if(_ct==2){
		eventTrend.loadTrend(function(_id,values){
			$(_id).css({"margin-top":"0px"});
			var $table=$("<table></table>").appendTo(_id);
			var _values = [];
			var sum = 0;
			console.log(values);
//			sort(values,"value","desc") ;
			$.each(eval(values), function(i, n) {
				$.each(n, function(index, node) {
					sum += node.value;
				});
				_values.push({'name':i,'value':sum});
				sum = 0;
			});
			sort(_values,"value","desc") ;
			$table.datagrid({   
				fit:true,
				fitColumns:true,
				singleSelect : true,
			   	data:_values,
			   	border:false,
			    columns:[[   
			        {field:'name',title:'事件名',width:50 },   
			        {field:'value',title:'数量',width:50}
			    ]],
			    onClickRow:function(rowIndex, rowData){
			    	showQueryfrom(rowData['name']);
			    }
			});  
			
		}, "#_event_trend_cg_id");
		eventCategory.typeTrd = 2;
	}
	
}