var fieldFilters={};
function EventQuery() {
	this._COLUMNS_SET_URL = "../../sim/eventMonitor/columnSet";
	this._BASE_EVENT_QUERY_URL = "../../sim/eventQuery/basicEventQuery";
	this._EXPORT_EVENT_URL="../../sim/eventQuery/exportBasicEvents";
	this.columns = [];
	this.cmenu = null;
	this.rmenu =null;
	this.logTabsDiv=null;
	this.data = [];
	this.tabs=[];
	this.queryParams = null;
	this.eventRowMenu=null;
	this.mainDataTableId=null;
};

/**
 * 格式化列
 * @param cfds
 */
EventQuery.prototype.columFormat=function(cfds){
	var argumTemp = "";
	
	if(arguments.length > 1){
		argumTemp = ("," + arguments[1]);
	}
	$.each(cfds,function(i,cff){
		var field = cff['field'];
		var headText = "";
		if(field == 'NAME'){
			headText = "事件名称";
		}
		if(field == 'SRC_ADDRESS'){
			headText = "源地址";
		}
		if(field == 'DEST_ADDRESS'){
			headText = "目的地址";
		}
		if(field == 'DVC_ADDRESS'){
			headText = "设备地址";
		}
		if(field == 'PRIORITY'){
			headText = "级别";
		}
		if(field == "CAT1_ID"){
			headText = "一级分类";
		}
		if(field == "CAT2_ID"){
			headText = "二级分类";
		}
		/*if(field == "CONFIRM_PERSON"){
			headText = "确认人";
		}*/
		if(field == 'PRIORITY'){
			cff.formatter = function(value,row,index){
				var priorityTemp = "<span class='priority"+ value +"'style=\"cursor: pointer;\" onclick=\"eventQuery.showEventData('"+field+"',"+index+",'"+headText+"',0)\"></span>";
				return priorityTemp;
			} ;
		}else if(field == 'LOG_COUNT'){
			cff.formatter = function(value,row,index){
				var spTemp = "<span style='margin-left:18px;' title='查看关联日志' class=\"badge badge-info hand\" onclick=\"eventQuery.doCorrelateOnRowIndex(";
				spTemp += (index + argumTemp + ")\">" + value + "</span>");
				return spTemp;
			} ;
		}else if(field == 'DESCR'){
			cff.formatter = function(value,row,index){
				var spTemp = "<span title ='"+value+"'>"+ value +"</span>";
				return spTemp;
			} ;
		}else{
			if(field != "END_TIME"){
				cff.formatter = function(value,row,index){
					var spanTemp = "<span  style=\"text-decoration:underline;text-align:left;cursor: pointer;\" " +
							"onclick=\"eventQuery.showEventData('"+field+"',"+index+",'"+headText+"',0)\">"+ 
					value+"</span>";
					return spanTemp;
				};
			}
			
		}
	});
};
EventQuery.prototype.showEventData=function(fieldname,conditionIndex,headText,status){
	var rows = "";
    if(status == "1"){
	   rows = $("#field_table").datagrid("getRows");
    }else{
	   rows = $("#event_query_table_id").datagrid("getRows");
    }
    var condition = rows[conditionIndex][fieldname];
	if(condition == "0"){
		condition = "非常低";
	}
	if(condition == "1"){
		condition = "低";
	}
	if(condition == "2"){
		condition = "中";
	}
	if(condition == "3"){
		condition = "高";
	}
	if(condition == "4"){
		condition = "非常高";
	}
    var text = "";
    var queryArr={};
	text = headText + " 等于 " + condition;
    queryArr = {
		datatype : headText,
		value : condition
    };

    eventQuery.createQueryElement(fieldname,text,queryArr);
    if(status == '1'){
	  $("#backup_log_fieldStatistics_dialog").dialog("close");
	}
};
EventQuery.prototype.createQueryElement=function(field, text, queryAttr){
		//每个条件的唯一UUID，用于判断重复条件
		var uuid = encodeURI(text);
		//1.判断UUID是否存在 
		var uuidEle = $("div.alert[uuid='"+uuid+"']");
		if(uuidEle.size() != 0){
			//当查询条件存在则闪烁一次
			uuidEle.fadeOut(100).fadeIn(100);
		}else{
			//当查询条件不存在则创建DOM元素
			var div = $('<div class="query-expression alert" style="float: left;padding:0px 2px;margin:3px 2px;"></div>').attr('uuid',uuid);
			var btn = $('<button style="top: auto;right: auto;" type="button" class="close" data-dismiss="alert">&times;</button>');
			div.append(btn).append('<strong>'+text+'</strong>');
			for(var key in queryAttr){
				div.attr(key, queryAttr[key]);
			}
			//2.根据ID(Field)判断查询条件组是否存在 
			var fieldQueryGroup = $("div.alert#"+field);
			if(fieldQueryGroup.size() != 0){
				//如果存在则将条件插入到此条件组中
				fieldQueryGroup.append(div);
			}else{
				//如果不存在则创建条件组，并将其插入到指定的DOM节点中
				var groupDiv = $('<div class="query-expression-group alert" style="float: left;padding: 5px 1px;margin: 2px 5px;"></div>');
				var groupBtn = $('<button style="top: 4px;right: auto;" type="button" class="close" data-dismiss="alert">&times;</button>');
				groupDiv.attr('id',field);
				var index = $("#event_query_condition").children().size() % colors.length;
				groupDiv.css('background-color',colors[index]);
				groupDiv.append(groupBtn).append(div);
				$("#event_query_condition").append(groupDiv);
			}
			//刷新日志列表
			eventQuery.setCondition('','');
		}
};
EventQuery.prototype.setCondition=function(filterField,filterUuid){
		var fieldNames = "",fieldValues = "",queryParam={};
		//标识是否第一次遍历组
		var boolgroup = true;
		//遍历所有查询条件组
		$('.query-expression-group').each(function(i){
			//组ID即是field
			var field = this.id;
			//如果当前组没有被过滤掉
			if(field != filterField){
				if(boolgroup){
					fieldNames = field;
					boolgroup = false;
				}else{
					fieldNames += ("," + field);
					fieldValues += ",";
				}
				var bool = true;
				//遍历条件组内的所有条件
				$(this).children('div.query-expression').each(function(j){
					//每个条件的唯一UUID
					var uuid = $(this).attr('uuid');
					//如果当前条件没有被过滤掉
					if(uuid != filterUuid){
						var condition = this.value.toString().replace(/\s+/g, "&nbsp;");
						if(condition == "非常低"){
							condition = "0";
						}
						if(condition == "低"){
							condition = "1";
						}
						if(condition == "中"){
							condition = "2";
						}
						if(condition == "高"){
							condition = "3";
						}
						if(condition == "非常高"){
							condition = "4";
						}
						
						
						if(bool){
							bool = false;
							fieldValues += condition;
						}
					}
				});
			}
		});
		var new_field = fieldNames.split(",");
		var new_value = fieldValues.split(",");
		$.each(new_field,function(i,value){
			
			var field_name="";
			
			if(new_field[i] == "NAME"){
				field_name = "query_event_Name";
			}else if(new_field[i] == "SRC_ADDRESS"){ 
				field_name = "srcIp";
			}else if(new_field[i] == "DVC_ADDRESS"){
				field_name = "deviceIp";
			}else if(new_field[i] == "DEST_ADDRESS"){
				field_name = "destIp";
			}else if(new_field[i] == "PRIORITY"){
				field_name = "priority";
			}else if(new_field[i] == "CAT1_ID"){
				field_name = "category1";
			}else if(new_field[i] == "CAT2_ID"){
				field_name = "category2";
			}else if(new_field[i] == "CONFIRM"){
				field_name = "confirm";
			}else if(new_field[i] == "CONFIRM_PERSON"){
				field_name = "confirm_person";
			}else if(new_field[i] == "DESCR"){
				field_name = "descr";
			}
			if(field_name){
				queryParam[field_name]=new_value[i];
			}
			
		});
		
		if(evtQueryForm.level.value){
			if(queryParam["priority"]){
				queryParam["priority"] += ","+evtQueryForm.level.value;
			}else{
				queryParam["priority"] = evtQueryForm.level.value;
			}
		}
		if(evtQueryForm.eventName.value){
				queryParam["eventName"] =evtQueryForm.eventName.value;
		}
		
		if(evtQueryForm.ruleName.value){
			if(queryParam["ruleName"]){
				queryParam["ruleName"] = +"," + evtQueryForm.ruleName.value;
			}else{
				queryParam["ruleName"] = evtQueryForm.ruleName.value;
			}
		}
		if(eventQueryForm.category1){
			if(queryParam["category1"]){
				queryParam["category1"] += ","+ eventQueryForm.category1;
			}else{
				queryParam["category1"] = eventQueryForm.category1;
			}
			
		}
		if(eventQueryForm.category2){
			if(queryParam["category2"]){
				queryParam["category2"] += ","+ eventQueryForm.category2;
			}else{
				queryParam["category2"] = eventQueryForm.category2;
			}
			
		}
		if(evtQueryForm.ip.value){
			queryParam["ip"] = evtQueryForm.ip.value;
		}
		queryParam["startTime"] = evtQueryForm.startTime.value;
		queryParam["endTime"] = evtQueryForm.endTime.value;
		
		eventQuery.createDataGrid(queryParam,"#event_query_table_id");
		eventQuery.expandTimeline(queryParam);
};

EventQuery.prototype.initLoadQueryCondition=function(){
	//当关闭查询条件组后，刷新日志列表
	$('.query-expression-group').die().live('closed', function (event) {
		var target = event.target;
		var field = $(target).attr('id');
		eventQuery.setCondition(field,'');
	});
	
	//当关闭一个查询条件,刷新日志列表
	$('.query-expression').die().live('closed', function (event) {
		//禁止事件冒泡
		event.stopPropagation();
		var target = event.target;
		//找到所有同辈查询条件
		var siblings = $(target).siblings('div.query-expression');
		//如果没有同辈查询条件，则关闭其所属查询条件组，否则刷新日志列表
		if(siblings.size() == 0){
			$(target).parent().alert('close');
		}else{
			var uuid = $(target).attr('uuid');
			eventQuery.setCondition('',uuid);
		}
	});		
};
EventQuery.prototype.resetQuery = function(){
	//清空所有查询条件DOM元素
	$('#event_query_condition').empty();
	
};
EventQuery.prototype.initColumns = function (_col_param) {//加载列集
	var argumTemp = null;
	if(arguments.length > 1){
		argumTemp = arguments[1];
	}
	var _this=this;this.columns = [];
	$.ajax({
		type : "post",
	 	url : "../../sim/eventMonitor/jsondata",
	 	data:_col_param,
		async : false,
		dataType : "json",
		success : function(data) {
			if(data && data.length == 1) {
				if(argumTemp != null) {
					_this.columFormat(data[0], argumTemp);
				} else {
					_this.columFormat(data[0]);
				}
				_this.columns.push(data[0]);
			}
		}
	});
};
function cnRiskToNum(condition){
	if(condition == "非常低"){
		return "0";
	}else if(condition == "低"){
		return "1";
	}else if(condition == "中"){
		return "2";
	}else if(condition == "高"){
		return "3";
	}else if(condition == "非常高"){
		return "4";
	}
}
EventQuery.prototype.createColumnMenu = function(id) {
	var _this = this;
	_this.cmenu = $('<div/>').appendTo('body');
	_this.cmenu.menu({
		onClick : function(item) {
			if (item.iconCls == 'icon-ok') {
				$(id).datagrid('hideColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-empty'
				});
			} else {
				$(id).datagrid('showColumn', item.name);
				_this.cmenu.menu('setIcon', {
					target : item.target,
					iconCls : 'icon-ok'
				});
			}
		}
	});
	var fields = $(id).datagrid('getColumnFields');
	for ( var i = 0; i < fields.length; i++) {
		var field = fields[i];
		var col = $(id).datagrid('getColumnOption', field);
		_this.cmenu.menu('appendItem', {
			text : col.title,
			name : field,
			iconCls : 'icon-ok'
		});
	}
};

EventQuery.prototype.createTabsLayout=function(){//使用到其它地方需要复写
	return $("<div data-options='border:true' fit='true' class='easyui-panel sim'></div>").appendTo($("#event_correl_id"));
};
 
/**
 * 双击事件处理
 */
EventQuery.prototype._tab_close=function(){
	if(!this.logTabsDiv){
		return;
	}
	var tabs=this.logTabsDiv.tabs('tabs');
	if(tabs&&tabs.length>=1){
		this.logTabsDiv.tabs('close',0);
		this._tab_close(); 
	}
};
EventQuery.prototype.showCorrelatelog=function(data){
	var _this=this;
	if(!this.logTabsDiv){
		this.logTabsDiv=this.createTabsLayout();
	}
	
	this.logTabsDiv.tabs();
	this._tab_close();
	 
	
	if(data.length){
  		$.each(data,function(i,_log){
    	   var _$tab_panel=$("<div></div>"),dgid="_t_"+i;
 			_this.logTabsDiv.tabs('add',{   
 			    title:_log["dvcTypeName"],   
 			    content:_$tab_panel,   
 			    closable:true,
 			    selected:false
 			});  
  			var headcolums=_log["headcolums"],_f_log=_log["columsdata"];
  			var _field_m=[];//
  			$.each(headcolums,function(i,_field){
  				 //设置表头样式
  				_field["width"]=100;
  				if(_field['field']=='MESSAGE'){
  					_field.formatter=function(value,row,index){//设置消息样式
  						return '<a class="_m_value_class_'+index+'" style="color:black;text-decoration:none;">'+value+'</a>';
  					};
  				}
  			});
  			_field_m.push(headcolums);
  			
  			var _field_o=[],_row_log_o=null;
  			//提取原始日志
  			  for(var k in _f_log){
  				_row_log_o=_f_log[k];
  			    _field_o.push({"ORIGINAL_DATA":(_row_log_o['ORIGINAL_DATA']?_row_log_o['ORIGINAL_DATA']:"无原始日志")});
  			} 
  			var isFitColumns = false;
  			if(headcolums.length < 10){
  				isFitColumns = true;
  			}
  		  $("<table id='"+dgid+"'/>").appendTo(_$tab_panel).datagrid({
 				view:detailview,
 				fitColumns:isFitColumns,
 				scrollbarSize : 0,
 			//	nowrap:false,
 				closable:true,
 				singleSelect:true,
 				onExpandRow:function(index,row){
 							var w= _$tab_panel.width()-55;
 							$('#ddv-'+dgid+"-"+index).datagrid({//需要新创建   原始日志
 								data:[_field_o[index]],
 								nowrap:false,
 								width:w,
 								height:'auto',
 								columns:[[{field:"ORIGINAL_DATA",title:"原始日志",width:w-4}]] ,
 								onResize:function(){},
 								onLoadSuccess:function(){},
 								onLoad:function(){
 						               $('#'+dgid).datagrid('fixDetailRowHeight',index);
 						        }
 							});
 							
 							$('#'+dgid).datagrid('fixDetailRowHeight',index);
 			   },
 			   detailFormatter:function(index,row){//原始日志
 					return '<div style="padding:2px"><table id="ddv-'+dgid+"-" + index + '"></table></div>';
 			   },
 			   data:_f_log,//格式化日志数据
 			   columns: _field_m ,
 			   onClose:function(title,index){},
 			   onLoadSuccess:function(data){
 				  //格式格式化日志消息显示
 				  if(data.rows){
					   $.each(data.rows,function(i,row){
						   $('._m_value_class_'+i).tooltip({   
							   position: 'left',   
							   content: '<div style="color:#fff;width:400px;">'+row['MESSAGE']+'</div>',   
							   onShow: function(){   
								   $(this).tooltip('tip').css({   
									   backgroundColor: '#666',   
									   borderColor: '#666'  
								   });   
							   }   
						   }); 
					   });
				   }
 			   }
 			});
		});
		if(_this.logTabsDiv.tabs("tabs").length > 0) {
     		_this.logTabsDiv.tabs('select', 0);
		}
	}
	
	
	
	//$panel.panel({title: '回溯详情'});
};
EventQuery.prototype.doCorrelateOnRow=function(rowData){
	var uuid=rowData['UUID'],_this=this;
	//$.messager.progress(); 
	$.ajax({
		type : "post",
	 	url : "../../sim/eventQuery/correlatorDataByUUID",
	 	data:{uuid:uuid},
		async : false,
		dataType : "json",
		success : function(data) {
			_this.showCorrelatelog(data);
			//$.messager.progress('close');
		}
	});

};

EventQuery.prototype._correlateReq=function(evtId){

	$.ajax({
		type : "post",
	 	url : "../../sim/eventMonitor/jsondata",
	 	data:_col_param,
		async : false,
		dataType : "json",
		success : function(data) {
			if(data&&data.length==1){
				_this.columFormat(data[0]);
				_this.columns.push(data[0]);
			}
		}
	});
	
};


EventQuery.prototype.doCorrelateOnRowIndex = function(rowIndex){
	 this.doCorrelateOnRow($(this.mainDataTableId).datagrid("getRows")[rowIndex]) ;
};

EventQuery.prototype._dbClickRowHandler=function(rowIndex, rowData){
	//this.showEventDetail(rowData);
	this.doCorrelateOnRow(rowData);
};

EventQuery.prototype._loadSuccessHandler=function(){//其他地方使用可复写
	
	$('.event_corr_linkbtn').linkbutton({   
	    iconCls: 'icon-search',
	    plain:true
	});  
};
EventQuery.prototype.expandTimeline=function(_param){
	$.ajax({
		type : "post",
	 	url : "/sim/eventQuery/expandTimeline",
	 	data:_param,
		dataType : "json",
		success : function(data) {
			if(data!=null && data!=""){
				var seriesData=[],category=[];
				$.each(data,function(i,_data){
					seriesData.push(_data.y);
					category.push(_data.x);
				});
				createLogTimelineChartInstance(seriesData,category);
			}
		}
	});
};
EventQuery.prototype.createDataGrid = function(param,id,_title,filters){
	if(!$.isEmptyObject(simHandler.indexEventQuery)){
		$('#startTimeId').val(simHandler.indexEventQuery.startTime);
		$('#endTimeId').val(simHandler.indexEventQuery.endTime);
		for(var pro in simHandler.indexEventQuery){
			if("name" ==pro){
				param["query_event_Name"]=simHandler.indexEventQuery[pro];
			}else if("priority"==pro){
				param[pro]=cnRiskToNum(simHandler.indexEventQuery[pro]);
			}else{
				param[pro]=simHandler.indexEventQuery[pro];
			}
		}
//		simHandler.indexEventQuery=null;
	}
	var _this = this,
	_wrappDetailView = function(_data) {
		var contents = "",
			content = null;
		$.each(_data, function(i, c) {
			content = '<div class="horizon_wrappDetailView">';
			content += '<p class="horizon_wrappDetailView_pHead">';
			content += '<span class="horizon_wrappDetailView_p_spHead">名称：</span>';
			content += '<span>' + c.name + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">等级：</span>';
			content += '<span>' + c.priority + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">描述：</span>';
			content += '<span>' + c.description + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">解决方案：</span>';
			content += '<span>' + c.solution + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">创建人：</span>';
			content += '<span>' + c.creater + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">时间：</span>';
			content += '<span>' + c.createTime + '</span>';
			content += '</p>';
			content += "</div>";
			contents += content;
		});
		return contents;
	};
	this.queryParams = param;
	this.mainDataTableId=id;
	
	$(_this.mainDataTableId).datagrid({
		url : _this._BASE_EVENT_QUERY_URL,
		method : 'post',
	//	title : ((!_title)?'事件':_title),
		width : 'auto',
		height : 'auto',
		iconCls:'icon-grid',
		fitColumns : true,
		
		singleSelect : true,
		checkOnSelect:false,
		selectOnCheck:false,
		pageSize:10,
		pageNumber:1,
		/*pageList:[10],*/
		pagination : true,
	//	rownumbers : true,
	//	striped : true,
		nowrap:true,
		view: detailview,
		detailFormatter:function(index,row){
		        return '<div class="ddv" style="padding:5px 0"></div>';
		},
	    onExpandRow: function(index,row){
	         var ddv= $(this).datagrid('getRowDetail',index).find('div.ddv');
	         ddv.panel({
	            border:false,
	            cache:false,
	            href:'../../sim/knowledge/getAssociatedKnowledgebyEvtId?evtid='+row['EVENT_ID'],
	            extractor: function(data){
	            	var _data=eval(data);
	            	if(_data&&_data.length>0){
	            		return _wrappDetailView(_data);
	            	}
	            	return "暂无解决方案";
	            },
	            onLoad:function(){
	                $(_this.mainDataTableId).datagrid('fixDetailRowHeight',index);
	            }
	        });  
	        $(_this.mainDataTableId).datagrid('fixDetailRowHeight',index);
	    },
		    
		columns : _this.columns,
		queryParams : _this.queryParams,
//		onHeaderContextMenu : function(e, field) {
//			e.preventDefault();
//			if(!_this.cmenu){
//				_this.createColumnMenu(_this.mainDataTableId);
//			}
//			
//			_this.cmenu.menu('show', {
//				left : e.pageX,
//				top : e.pageY
//			});
//			
//		},
		onClickRow : function(rowIndex, rowData) {
			_this._dbClickRowHandler(rowIndex, rowData);
		},
		//鼠标右键事件
		onRowContextMenu:function(e, rowIndex, rowData){
			//rowIndex就是当前点击时所在的行索引，rowData当前行的数据
			e.preventDefault();//阻止浏览器默认右键事件
			$(this).datagrid("clearSelections");//取消所有选中项
			$(this).datagrid("selectRow", rowIndex);//根据索引选中该行
			$("#menu").menu('show', {
				left:e.pageX,//在鼠标点击处显示菜单
				top:e.pageY
			});
			rightData = rowData;
		},
		onSelect:function(rowIndex, rowData){
		},
		onLoadSuccess:function(data){
			_this._loadSuccessHandler(data);
			var pager = $(_this.mainDataTableId).datagrid('getPager');
			if(pager.pagination('options').showPageList){
				pager.pagination({showPageList:false});
			} 
			if(data.rows.length>0){
				_this.doCorrelateOnRowIndex(0);
			}else{
				_this._tab_close();
			}
		}
	});	
	eventQuery.initLoadQueryCondition();
};

var rightData;

//右键查看事件详细信息
function view(){
	var node = $("#event_query_table_id").datagrid('getSelected');
    if(node){
    	$("#event_detail_dialog").show().dialog({
    		   title: '事件详细信息',    
    		   width: 800,
    		   height: 542,
    		   closed: false,
    		   cache: false,
    		   modal: true,
    		   collapsible: true,
    		   onOpen:function(){
    		   }
		});
    	
    	var evtId = rightData['EVENT_ID'];
    	var evtName = rightData['NAME'];
    	var end_time = rightData['END_TIME'];
    	var uuid = rightData['UUID'];
    	//选择的页签名称
    	$("#event_detail_tab").tabs({
            onSelect: function (title) {
            }
        });
    	//设置每次打开都显示首页
    	$('#event_detail_tab').tabs('select', '关联资产');
    	
    	//查询数据，加载表格
    	$.getJSON('/sim/eventQuery/queryEventDetail?uuid=' + uuid + "&evtId=" + evtId + "&end_time=" + end_time + "&evtName=" + encodeURI(evtName), function(result){
    		if (result) {
    			//加载资产
    	    	$("#event_asset_table").datagrid({
    	    		width : 'auto',
    	    		height : 'auto',
    	    		iconCls:'icon-grid',
    	    		fitColumns : true,
    	    		singleSelect : true,
    	    		fit: true,
    	    		border: true,
    	    		striped : true,
    	    		nowrap:false,
    	    		loadMsg:'正在加载，请稍后。。。</a>',
    	    		columns : [[
    	    		            {field:'name',title:'资产名称',width:10,formatter:nameFormatter},
    	    		            {field:'ip',title:'IP地址',width:10},
    	    		            {field:'deviceTypeName',title:'资产类型',width:10},
    	    		            {field:'nodeName',title:'管理节点',width:10},
    	    		            {field:'linkman',title:'联系人',width:10}
    	    		        ]]
    	    		
    	        }).datagrid('loadData', result.assets);
    			
    	    	//加载日志源
    	    	$("#event_logSrc_table").datagrid({
    	    		width : 'auto',
    	    		height : 'auto',
    	    		iconCls:'icon-grid',
    	    		fitColumns : true,
    	    		singleSelect : true,
    	    		fit: true,
    	    		striped : true,
    	    		nowrap:false,
    	    		loadMsg:'正在加载，请稍后。。。</a>',
    	    		columns : [[
    	    		            {field:'deviceIp',title:'日志源IP',width:10},
    	    		            {field:'resourceName',title:'日志源名称',width:10},
    	    		            {field:'securityObjectTypeName',title:'日志源类型',width:10}
    	    		        ]]
    	    		        
    	        }).datagrid('loadData', result.dataSources);
    	    	
    	    	//加载告警方式
    	    	$("#event_alarm_table").datagrid({
    	    		width : 'auto',
    	    		height : 'auto',
    	    		iconCls:'icon-grid',
    	    		fitColumns : true,
    	    		singleSelect : true,
    	    		fit: true,
    	    		rownumbers : false,
    	    		striped : true,
    	    		nowrap:false,
    	    		loadMsg:'正在加载，请稍后。。。</a>',
    	    		columns : [[
    			            	{field:'name', title:"名称", width:10},
    		            		{field:'cfgKey', title:"响应方式", width:10, formatter:formattercfgKey},
    		            		{field:'creater',title:'创建者',width:10},
    		            		{field:'desc',title:'描述',width:10},
    		            		{field:'start',title:'状态',width:10, formatter:formatterStatus}
    	    		        ]]
    	    		        
    	        }).datagrid('loadData', result.alarm);;
    	    	
    	    	//加载解决方案
    	    	$("#event_solution_table").datagrid({
    	    		width : 'auto',
    	    		height : 'auto',
    	    		iconCls:'icon-grid',
    	    		fitColumns : true,
    	    		singleSelect : true,
    	    		fit: true,
    	    		rownumbers : false,
    	    		striped : true,
    	    		nowrap:false,
    	    		loadMsg:'正在加载，请稍后。。。</a>',
    	    		columns : [[
    	    		            {field:'name',title:'名称',width:10},
    	    		            //{field:'priority',title:'等级',width:10},
    	    		            {field:'description',title:'描述',width:10},
    	    		            {field:'solution',title:'解决方案',width:10},
    	    		            {field:'creater',title:'创建人',width:10},
    	    		            {field:'createTime',title:'时间',width:10}
    	    		        ]]
    	    		        
    	        }).datagrid('loadData', result.knowledge);
    	    	
    	    	//加载业务组
    	    	/*$("#event_topo_table").datagrid({
    	    		width : 'auto',
    	    		height : 'auto',
    	    		iconCls:'icon-grid',
    	    		fitColumns : true,
    	    		singleSelect : true,
    	    		fit: true,
    	    		rownumbers : false,
    	    		striped : true,
    	    		nowrap:false,
    	    		loadMsg:'正在加载，请稍后。。。</a>',
    	    		columns : [[
	    		            	//{field:'name',title:'资产名称',width:10},
    	    		            //{field:'ip',title:'资产IP',width:10},
    	    		            {field:'topoName',title:'业务组',width:10}
    	    		        ]]
    	    		        
    	        }).datagrid('loadData', result.topo);*/
    	    	
			}
    	});
    	
    	/**********************************加载日志时间轴BEGIN****************************************/
    	$.post("/sim/eventQuery/correlatorDataByUUID", {uuid:uuid}, function(data) {
    		if(!data.length){
    			return;
    		}
    		$("#GametimelineHandler").html("");
    		$("#GameModuleTimelineContainer").html("");
    		$.each(data,function(i,_log){
    			var _f_log=_log["columsdata"];
    			var _row_log_o = null;
    			for(var k in _f_log){
    				_row_log_o = _f_log[k]['ORIGINAL_DATA'];
    				$("<li><span>" + _f_log[k]['START_TIME'] + "</span><b></b></li>").appendTo($("#GametimelineHandler"));
					$("<div><p style='color:blue'>" + "源IP: " + nvl(_f_log[k]['SRC_ADDRESS'],"无") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "源端口: " + nvl(_f_log[k]['SRC_PORT'],"无") + "<br/>"
							+ "目的IP: " + nvl(_f_log[k]['DEST_ADDRESS'],"无") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "目的端口: " + nvl(_f_log[k]['DEST_PORT'],"无") + "<br/>"
							+ "设备IP: " + nvl(_f_log[k]['DVC_ADDRESS'],"无") + "</p></div>").appendTo($("#GameModuleTimelineContainer"));
    			}
    		});
    		new Gametimeline().init(0);
    	}, "json");
    	/**********************************加载时间轴END****************************************/
    }
}

//时间轴时间格式化
function createDate(year,month,date,hour,min,seconds){
	var d = new Date();
	d.setUTCFullYear(year, month, date); 
	d.setUTCHours(hour-8,min,seconds, 0);//UTC时间15点相当于中国23
	return d;
}

//时间轴处理
function Gametimeline(){
	var doc=document,indicator=arguments.callee,that=this;
	that.parent=doc.getElementById('GameModuleTimeline'),
	that.hand=doc.getElementById('GametimelineHandler'),
	that.handler=that.hand.getElementsByTagName('b'),
	that.date=that.hand.getElementsByTagName('span'),
	that.container=doc.getElementById('GameModuleTimelineContainer'),
	that.pointer=doc.getElementById('GametimelinePointer');
	!indicator.cache&&(indicator.cache=[[],[],[]]);
	if(that.container.children.length!==that.handler.length)
		return false;
	!this.parseDate
		&&(indicator.prototype.parseDate=function(){
			var i=0,
			len=that.handler.length,
			temp=[];
			for(;i<len;){
				var elem=that.handler[i],                
				date=new Function('return'+('['+that.date[i].innerHTML.replace(/\.|:|-|\s/gi,',')+']'))();
				that[i]=+ createDate(date[0],date[1]-1,date[2],date[3],date[4],date[5]);
				that.length=i;elem.style.left='';
				void function(j,o){
					that.addEvent(o,'mouseover',
						function(){
							that.activity.call(o,j);
						},false);
				}(i++,elem);
			};
			return that;
	});
	indicator.prototype.locateHandler=function(){
		i=0,
		len=that.handler.length,
		temp=0;
		for(;i<len;){
			temp = i*(that.parent.offsetWidth-115)/((len===1)?1:len-1);
			that.fx(that.handler[i],'left',((i===0)?temp-15:temp),0);
			i++;
		}
	};
	indicator.prototype.Linear=function(t,b,c,d){
		if((t/=d)<(1/2.75)){
			return c*(7.5625*t*t)+b;
		}else if(t<(2/2.75)){
			return c*(7.5625*(t-=(1.5/2.75))*t+.75)+b;
		}else if(t<(2.5/2.75)){
			return c*(7.5625*(t-=(2.25/2.75))*t+.9375)+b;
		}else{
			return c*(7.5625*(t-=(2.625/2.75))*t+.984375)+b;
		}
	};
	indicator.prototype.fx=function(o,property,c,d){
		var b=0,c=c||50,d=d||100,t=0,k=0,j=10,i=0;
		void function(){
			o.style[property]=Math.ceil(that.Linear(t,b,c,d))+'px';
			if(parseInt(o.style[property])<c){
				t++;
				setTimeout(arguments.callee,10);
			};
		}();
	};
	indicator.prototype.activity=function(index){
		var slice=Array.prototype.slice,
		date=that.date,span=date[index],
		container=that.container,
		div=container.children[index],
		rect=that.getClinetRect(this),
		limit=that.getClinetRect(that.container);
		!indicator.cache[0][index]&&(indicator.cache[0][index]
			='visibility:visible;left:'+(parseInt(this.style.left)-date[0].offsetWidth/2+this.offsetWidth/2+'px'));
		if(!indicator.cache[1][index]){
			if((rect.left-div.offsetWidth/2)<limit.left){
				indicator.cache[1][index]='visibility:visible;left:0px;';
			}else if((rect.left+div.offsetWidth/2)>limit.right){
				indicator.cache[1][index]='visibility:visible;left:'+(limit.right-div.offsetWidth-limit.left)+'px;';}
			else{
				indicator.cache[1][index]='visibility:visible;left:'+(rect.left-div.offsetWidth/2-limit.left)+'px;';
			};
		};
		!indicator.cache[2][index]&&(indicator.cache[2][index]
			=('visibility:visible;left:'+(parseInt(this.style.left)-that.pointer.offsetWidth/2+this.offsetWidth/2)+'px; z-index:60;'));
		that.off.call(this,index);
		this.className='GametimelineOn';
		span.style.cssText=indicator.cache[0][index];
		that.fx(span,'top',15,80);
		!window.ActiveXObject&&(that.fadeIn.call(span,30),that.fadeIn.call(div,30));
		div.style.cssText=indicator.cache[1][index];that.pointer.style.cssText=indicator.cache[2][index];
	};
	indicator.prototype.off=function(index){
		var i=0,len=that.handler.length;
		for(;i<len;){
			if(i!==index){
				that.date[i].style.visibility='hidden',that.container.children[i].style.visibility='hidden';
				that.handler[i].className='GametimelineOff';
			}
			i++;
		};
	};
	indicator.prototype.getClinetRect=function(elem){
		var result=elem.getBoundingClientRect(),
		temp=(temp={left:result.left,right:result.right,top:result.top,bottom:result.bottom,
			height:(result.height?result.height:(result.bottom-result.top)),
			width:(result.width?result.width:(result.right-result.left))}
		);
		return temp;
	};
	indicator.prototype.fadeIn=function(steps,fn){
		that.doFade.call(this,steps/10,0,true,fn);
	};
	indicator.prototype.doFade=function(steps,value,action,fn){
		var ie=undefined!==window.ActiveXObject,
		calls=arguments.callee,
		t=this,step;
		value+=(action?1:-1)/steps,
		(action?value>1:value<0)&&(value=action?1:0),
		ie===true?t.style.filter='alpha(opacity='+value*100+')':t.style.opacity=value;
		(action?value<1:value>0)&&setTimeout(
			function(){
				calls.call(t,steps,value,action,fn);
			},
			1000/steps);
			(action?value===1:value===0&&'undefined'!==typeof fn)&&('function'===typeof fn&&fn.call(t)
		);
	};
	indicator.prototype.addEvent=function(elem,evType,fn,capture){
		var indicator=arguments.callee;
		elem.attachEvent&&(indicator=function(elem,evType,fn){elem.attachEvent('on'+evType,fn)}).apply(this,arguments);
		elem.addEventListener&&(indicator=function(elem,evType,fn){
			elem.addEventListener(evType,fn,capture||false);
			}).apply(this,arguments);
		elem['on'+evType]&&(indicator=function(elem,evType,fn){
			elem['on'+evType]=function(){fn();};}).apply(this,arguments);
	};
	indicator.prototype.trigger=function(elem,evType){
		var event,doc=document;
		undefined!==doc.createEvent?(event=doc.createEvent('MouseEvents'),
			event.initMouseEvent(evType,true,true,document.defaultView,0,0,0,0,0,false,false,false,false,0,null),
			elem.dispatchEvent(event)):
				(event=doc.createEventObject(),
					event.screenX=100,
					event.screenY=0,
					event.clientX=0,
					event.clientY=0,
					event.ctrlKey=false,
					event.altKey=false,
					event.shiftKey=false,
					event.button=false,
					elem.fireEvent('on'+evType,event)
				);
	};
	return{init:
		function(index){
			that.parseDate();
			that.locateHandler();
			that.trigger(that.handler[index],'mouseover');
		}
	};
};

nameFormatter = function(value,row,index){
	return "<span class='"+row.assetIconCls+"' style='width:18px;'/>"+"<span class='table_column_link' onclick=\"al.showDetail('"+ row.name + "','"+row.ip+"');\">"+value+"</span>" ;
};

var jsontypedata=null;
function settypename(){
    try{
    	$.ajax({
            url: '/sim/sysconfig/event/jsondata?json=evt_resp_options',
            dataType:'json',
            success: function(data) {
            	jsontypedata= data;
            }
        });
    }catch(e){}
    return jsontypedata;
};
settypename();
function formatterStatus(value,row,index){
	return value ? "启用" : "禁用";
}
function formattercfgKey(value,row,index){
	if(undefined==jsontypedata||null==jsontypedata){
		return value;
	}
	for(var i=0;i<jsontypedata.length;i++){
		if(value==jsontypedata[i].value){
			return jsontypedata[i].name;
		}
	}
	
}

//鼠标右键刷新
function reload(){
	var node = $("#event_query_table_id").datagrid('getSelected');
	  if(node){
		  $("#event_query_table_id").datagrid('reload');
	  }
}

EventQuery.prototype.queryEvent = function(param,id,_title) {
	this.queryParams = param;
	$(this.mainDataTableId).datagrid("load",param) ;
};
//格式化日期
function formatterDate(str){
	var date1=parseInt(str);
	var startdate=new Date(date1);
	startdate=startdate.getFullYear()
	          +"-"+((startdate.getMonth()+1)<10 ? ("0"+(startdate.getMonth()+1)):(startdate.getMonth()+1))
	          +"-"+(startdate.getDate()<10 ? ("0"+startdate.getDate()):startdate.getDate())
	          +" "+(startdate.getHours()<10 ? ("0"+startdate.getHours()):startdate.getHours())
	          +":"+(startdate.getMinutes()<10 ? ("0"+startdate.getMinutes()):startdate.getMinutes())
	          +":"+(startdate.getSeconds()<10 ? ("0"+startdate.getSeconds()):startdate.getSeconds()) ;
	return startdate;
}
/**
 * 创建日志时间线对象
 */
var savetime=[];
var eventModel = new EchartsModel();
function createLogTimelineChartInstance(seriesData,category){
	var w = $('#event_timeline_chart').parent().width()-80;
	var h = $('#event_timeline_chart').parent().height();
	
	var barWidth ;
	if(seriesData != null){
		if(seriesData.length<50){
			barWidth = 20;
		}
	}
	
	$("#event_timeline_chart").css("height",h);
	$("#event_timeline_chart").css("width",w);
	 var option = {
			  color:['#6CD7D9'],
			   tooltip: {
				   trigger: 'axis'
	           },
	           grid:{
	        	 x:0,
	        	 y:0,
	        	 x2:0,
	        	 y2:0
	           },
	           tooltip: {
	        	   backgroundColor:'#FAFAFA',
	               borderColor : '#6CD7D9',
	               borderRadius : 8,
	               borderWidth: 1,
	               position : function(p) {
	            	   return eventModel.setTooltipPosition('#event_timeline_chart', p, 320, -10);
	               },
	               textStyle : {
	                   color: '#333333',
	                   decoration: 'none',
	                   fontFamily: 'Verdana, sans-serif',
	                   fontSize: 12
	                  // fontStyle: 'italic'
	                  // fontWeight: 'bold'
	               },
	              formatter: function (params) {
	        		var str = params.name;
	        		var dateArr = str.split("&");
	        		var date1 = parseInt(dateArr[0]);
	        		var startdate = formatterDate(date1);
	        		var date2 = parseInt(dateArr[1]);
	        		var enddate = formatterDate(date2);
	                return  startdate + "至" + enddate +  '<br/>匹配的事件：<span style="color:red">' + params.value + '</span>条';   
	              }  
	            },
	           xAxis:[
	                    {
	                       type: 'category', 
	                       data:category,
	                       splitLine:{
	                    	   show:false
	                       }
	                    }
	                 ],
	           yAxis:[
	                {
	                    type : 'value',
	                    axisLine:{show:false},
	                    splitLine:{
	                    	   show:false
	                       }
	                }
	            ],
	            series: [{
	                name: 'logCount',
	                type:'bar',
	                barMaxWidth:barWidth,
	                data:seriesData
	            }]
	   };

	    require.config({
			paths : {
				echarts : '/js/echart/build/dist',
			}
		});
		
		require(
				[
				    'echarts',
					'echarts/chart/bar'
					], 
				function(ec) {
					var myChart = ec.init(document.getElementById("event_timeline_chart"));
					myChart.setOption(option);
					myChart.on('click', function(param){
						 if (param.type == 'click') { 
							 isTimelineClick = true;
							 var str=param.name;
	                    		var dateArr=str.split("&");
	                    		var date1=parseInt(dateArr[0]);
	                    		var startdate=formatterDate(date1);
	                    		var date2=parseInt(dateArr[1]);
	                    		var enddate=formatterDate(date2);
	                    		if(startdate == $("#startTimeId").val() && enddate == $("#endTimeId").val()){
	                    			return false;
	                    		}
	                    		savetime[savetime.length]=$("#startTimeId").val()+"&"+$("#endTimeId").val();
	                    		$("#startTimeId").val(startdate);
	                    		$("#endTimeId").val(enddate);
	                    		$("#event_floatDiv").fadeIn("normal");
	                    		doEventQuery();
						 }
						
					});
				});
	
	
	
}
EventQuery.prototype.exportEvents = function(_function) {
	$.messager.progress();
	var ex = {};
	for ( var key in this.queryParams) {// 复制请求参数
		ex[key] = this.queryParams[key];
	}
	var fields = "", header = "";
	$.each(this.columns, function(i, ns) {// 复制域
		$.each(ns, function(j, n) {
			if (n['title']&&n['field']!='LOG_COUNT') {
			
				fields += (n['field'] + ",");
				header += (n['title'] + ",");
			}
		});
	});
	if (fields.length > 1) {
		ex['fields'] = fields.substring(0, fields.length - 1);
		ex['header'] = header.substring(0, header.length - 1);
	}
	$.ajax({
		type : "post",
		url : this._EXPORT_EVENT_URL,
		dataType : "json",
		data : ex,
		success : function(data) {
			if (_function) {
				_function(data);
			}
			$.messager.progress("close");
		}
	});
};
