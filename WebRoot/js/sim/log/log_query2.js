//@ sourceURL=log_query2.js
/**
 * ------------------------
 * 日志查询
 * @author hou_jianyong
 * @date 2014/4/4
 * ------------------------
 */
var simLogQueryHandler = {};
//日志查询条件
var queryParams = {};
//日志列表序号
var rowNumbers = [];
//用于标识下一页按钮是否可用
var nextPre=false;
//定时器
//用于存储每个列标题对应过滤器的值
var column_tooltip = {};
//查询条件组背景色
var colors = ['#2f7ed8', '#8bbc21', '#1aadce', '#f28f43', '#77a1e5', '#c42525', '#a6c96a'];
//创建表格状态，用于判断表格和分页是否已经创建过
var createdTableStatus = false;
//日志源加载状态
var logDataSourceStatus = false;
//服务器时间
var servertime = simHandler.serverTime;
//查询分析传值
var conName,conOperator,conQueryContent,conQueryType;
var seq = 0 ;
var logQueryStatus = true;
var isCancel = false;
var cmenu ;
var isHaveSourceLog = true;
var searchStatus = true;
var columns ;
var menuContext = {row:null,rowIndex:-1,column:null} ;//右键菜单上下文
//用于查询分析显示查询条件
	/**
	 * 日志列表加载数据定时器<br>
	 * 由于后台查询需要大量计算过程，导致查询结果响应较慢，所以需要多次查询才能将结果显示完整。
	 */
	function logTableLoadDataTimer(params){
		//构造查询参数
		for(var name in params){
			queryParams[name] = params[name];
		}
		if(queryParams.deviceType){
			//即时查询
			changeTableData(queryParams);
		}
	}

	/**
	 * 根据查询参数获取日志列表数据，动态构建列集和数据集
	 * @param params 查询参数
	 */
	function changeTableData(params){
		seq++ ;
		if(simHandler.logQueryParam){
			params.host = simHandler.logQueryParam.deviceIp ;
			params.nodeId = simHandler.logQueryParam.nodeId ;
			simHandler.logQueryParam = null ;
		}
		if(simHandler.logQueryObject){
			params.host = simHandler.logQueryObject.host;
			params.queryEndDate=simHandler.logQueryObject.queryEndDate;
			params.queryStartDate=simHandler.logQueryObject.queryStartDate;
			simHandler.logQueryObject=null;
		}
		params.seq = seq;
		if(!params.pageSize)
			params.pageSize = 20;
		if(!params.pageNo)
			params.pageNo = 1;
        $.ajax({
            url: '/sim/logSearch/doLogSearch',
            type: 'POST',
            data: JSON.stringify(params),
            dataType:'text',
            contentType:"text/javascript",
            success: function(text){
            	text = text.replace(/</g,"&lt;").replace(/>/g,"&gt;");
            	var data = $.parseJSON(text) ;
				if(!!data){
					//后台是否已经查询结束，如果结束则清除定时器
					var isOk = data.finished;
					//定义列集、数据集
					var cols = [], rows = [] ; 
					
					//初始化数据集
					if(!!data.maps){
						rows = data.maps;
					}
					//初始化字段查询过滤器DOM {field:html}
					var fieldFilters = {};
					if(!!data.filters){
						$.map(data.filters,function(filter){
							var field = filter.name;
							fieldFilters[field] = createFilterElement(filter);
						});
					}
					//初始化列集
					if(!!data.columns){
						rowNumberData(params.pageNo,params.pageSize);
						cols.push({field:'rownumber',title:'序号',width:50,formatter:rowNumber});
						columns = data.columns;
						$.map(data.columns,function(col){
							if(!!fieldFilters[col.dataField]){
								var type="";
								if(!!data.filters){
									$.map(data.filters,function(filter){
										var field = filter.name;
										if(field==col.dataField){
											type=filter.type;
										}
									});
								}
								if(col.headerText=="详情" ||col.headerText=="详细"  ||col.headerText=="描述" || col.headerText=="入侵内容" ){
									cols.push({field:col.dataField,title:col.headerText,hidden:col.hidden,width:600,formatter:function(value,row,index){
										if(value != undefined && value != null){
											if(col.dataField!="MESSAGE"){
											      return "<span title='"+value+"' style=\"text-decoration:underline;text-align:left;cursor: pointer;\"  onclick=\" showLogDetail('"+col.dataField+"','"+index+"','"+type+"','"+col.headerText+"',0)\">"+value+"</span>";
											}else{
												return "<span title='"+value+"'>"+value+"</span>";
											}
										}else{
											return "";
										}
									}});
								}else if(col.headerText=="原始日志"){
									cols.push({field:col.dataField,title:col.headerText,hidden:col.hidden,width:1200,formatter:function(value,row,index){
										if(value != undefined && value != null){
											return "<span title='"+value+"'>"+value+"</span>";
										}else{
											return "";
										}
										
									}});
								}else{
								   cols.push({field:col.dataField,hidden:col.hidden,width:nvl(col.width,140),title:col.headerText,formatter:function(value,row,index){
								   var str="";
								   if(value != undefined && value != null){
									   var title = "" ;
									   var iconSpan = "" ;
									   if(col.type == "ip"){
										   title = " title='资产名称："+row[col.dataField+"$ASSET_NAME"]+"'" ;
										   var flagCss = nvl(row[col.dataField+"_COUNTRY_ID"],"UNKNOW") ;
										   iconSpan = "<span class='hand flag flag-"+flagCss+"' title='"+row[col.dataField+"_LOCATION"]+"'/>" ;
									   }
									   str="<div style='width:50px;'>"+iconSpan+"<span style=\"text-decoration:underline;text-align:left;cursor: pointer;\" " +
									   	        "onclick=\" showLogDetail('"+col.dataField+"','"+index+"','"+type+"','"+col.headerText+"',0)\"";
									   if(col.headerText=="级别"){
										   str+= " class=\""+simHandler.getPriorityClassByCN(value)+"\"></span></div>" ;
									   }else{
										   str+= title + ">"+value+"</span></div>";
									   }
									   return str;
									}else{
										return "";
									}
							       }});
								}
							}else{
								if(col.headerText=="详情" ||col.headerText=="详细" ||col.headerText=="描述" || col.headerText=="入侵内容" ){
									cols.push({field:col.dataField,title:col.headerText,hidden:col.hidden,width:600,formatter:function(value,row,index){
										if(value != undefined && value != null){
											return "<span title='"+value+"'>"+value+"</span>";
										}else{
											return "";
										}
									}});
								}else{
								  cols.push({field:col.dataField,title:col.headerText,hidden:col.hidden,width:nvl(col.width,150),formatter:function(value,row,index){
									var str="";
									if(value != undefined && value != null){
										var title = "" ;
										var iconSpan = "" ;
										if(col.type == "ip"){
											title = " title='资产名称："+row[col.dataField+"$ASSET_NAME"]+"'" ;
											var flagCss = nvl(row[col.dataField+"_COUNTRY_ID"],"UNKNOW") ;
											iconSpan = "<span class='hand flag flag-"+flagCss+"' title='"+row[col.dataField+"$LOCATION"]+"'/>" ;
										}
										str="<div style='width:50px;'>"+iconSpan+"<span ";			
										if(col.dataField == "PRIORITY"){
											str+= " class='"+simHandler.getPriorityClassByCN(value)+"'></span></div>" ;
									    }else{
									    	str+= title + ">"+value+"</span></div>";
									    }
							            return str;
									}else{
									   return "";
									}
						          }});
								}
							}
							
						});
					}
					//初始化日志源列表
					var logSourcesList = [];
					if(!!data.dataSource){
						logSourcesList = data.dataSource;
					}
					refreshLogTimelineChartData(data.timeline);
					
					
					var totalcount=data.totalCount;
					if(totalcount==null){
						totalcount=0;
					}
					//刷新分页菜单
					refreshLogTablePager(totalcount, data.totalRecords, data.displayCount, data.lapTime, params.pageSize, params.pageNo, !isOk,data.searchLimit);
					//如果已经创建过表格，只刷新表格数据
					if(createdTableStatus){
						refreshLogTableData(rows);
					}else{
						if(!logDataSourceStatus){
							createLogSourcesList(logSourcesList);
						}
						createLogTable(cols,rows,fieldFilters);
						fillLimit(data.displayLimit, data.searchLimit) ;
					}
					if(data.logType == "Esm/Topsec/SystemRunLog" || data.logType == "Esm/Topsec/SystemLog"){
						isHaveSourceLog = false;
		    		 }else{
		    			 isHaveSourceLog = true;
		    		 }
					showExportTip(isHaveSourceLog);
					createdTableStatus = true;
					if(!isOk && data.seq == seq){
						if(!isCancel){
							changeTableData(params) ;
						}else{
							 $('#log_query_table_pager').pagination('loaded');
							 $("#log_query_table").datagrid("loaded");
							 isCancel = false;
							
						}
						
					}else{
						searchStatus = true;
						 $('#log_query_table_pager').pagination('loaded');
						 $("#log_query_table").datagrid("loaded");
						 
						 setTimeout(function(){
							if(params.host){
						       	$("#log_source_sel").find("option[ip='"+params.host+"']").attr("selected",true);
							}
						},500);
					}
					logQueryStatus=isOk;
					 
				}            	
            }
        });
	}	
	
  	function rowNumber(value,row,index){
  		var str = rowNumbers[index];
        return str;
  	}
	
	/**
	 * 创建日志源列表下拉框
	 * @param logSourcesList 日志源数组
	 */
	function createLogSourcesList(logSourcesList){
		var optAll = $("<option value='' ip=''>全部</option>");
		$('#log_source_sel').empty().append(optAll);
		for(var i in logSourcesList){
			var ip = logSourcesList[i].ip;
			var name = logSourcesList[i].name;
			var nodeId = logSourcesList[i].nodeId;
			var opt = $("<option/>").val(nodeId).attr('ip', ip).attr('title',ip).text(!!name ? name : ip);
			if(logSourcesList[i].selected == true){
				opt.attr("selected","") ;
			}
			$('#log_source_sel').append(opt);
		}
	}
	
	/**
	 * 刷新日志列分页菜单
	 * @param totalLogs 日志总量
	 * @param totalRecords 命中数
	 * @param displayCount 当前记录数
	 * @param lapTime 查询耗时
	 * @param pageSize 每页显示条数
	 * @param pageNumber 当前页码
	 * @param loading 是否正在加载数据
	 */
	function refreshLogTablePager(totalLogs, totalRecords, displayCount, lapTime, pageSize, pageNumber, loading,searchLimit){
		var logcount=totalRecords<searchLimit ? "=" : ">=";
		var disMsg = "显示<font color='red'>{from}</font>到<font color='red'>{to}</font>,前<font color='red'>{total}</font>条, 命中数"+logcount+"<font color='red'>" + totalRecords + "</font>条, 日志总量<font color='red'>" + totalLogs + "</font>条, 耗时<font color='red'>" + (lapTime/1000) + "</font>秒";
		var options = {
			total:displayCount,   
		    pageSize:pageSize,
		    pageNumber:pageNumber,
		    loading:loading,
		    displayMsg:disMsg
		};
		$('#log_query_table_pager').pagination('refresh',options);
	}
	
	/**
	 * 根据参数，创建表格分页工具条<br/>
	 * @param totalLogs 日志总量
	 * @param totalRecords 命中数
	 * @param displayCount 当前记录数
	 * @param lapTime 查询耗时
	 * @param pageSize 每页显示条数
	 * @param pageNumber 当前页码
	 * @param loading 是否正在加载数据
	 */
	function createLogTablePager(totalLogs,totalRecords,displayCount,lapTime, pageSize, pageNumber, loading,searchLimit){
		var logCount = totalRecords < searchLimit ? "=" : ">=";
		var disMsg = "显示<font color='red'>{from}</font>到<font color='red'>{to}</font>,前<font color='red'>{total}</font>条, 命中数"+logCount+"<font color='red'>" + totalRecords + "</font>条, 日志总量<font color='red'>" + totalLogs + "</font>条, 耗时<font color='red'>" + (lapTime/1000) + "</font>秒";
		//更新分页信息
		$('#log_query_table_pager').pagination({   
		    total:displayCount,   
		    pageSize:pageSize,
		    pageNumber:pageNumber,
		    loading:loading,
		    displayMsg:disMsg,
		    onSelectPage:function(pageNumber, pageSize){
		    	queryParams.pageSize = pageSize;
				queryParams.pageNo = pageNumber;
				rowNumberData(pageNumber, pageSize);
				logTableLoadDataTimer(queryParams);
				$('#log_query_table').datagrid('loading');
				$('#log_query_table_pager').pagination('loading');
				
		    }
		}); 
	}
	function rowNumberData(pageNumber,pageSize){
		var start = (pageNumber-1)*parseInt(pageSize);
		var end = start + parseInt(pageSize);
		rowNumbers = [];
		for(var i= start ; i < end;i++){
			rowNumbers.push(i+1);
		}
	}
	
	function fillLimit(displayLimit,searchLimit){
		var formatDisplayLimit = countFormatter(displayLimit, 1) ;
		var formatSearchLimit = countFormatter(searchLimit, 1) ;
		
		var selectLimitElement = $("#searchLimit") ;
		if(selectLimitElement.find("option[value='"+searchLimit+"']").size() == 0){
			searchLimitElement.prepend("<option value='"+searchLimit+"'>"+formatSearchLimit+"</option>")
		}
		selectLimitElement.val(searchLimit) ;
		
		var displayLimitElement = $("#displayLimit") ;
		if(displayLimitElement.find("option[value='"+displayLimit+"']").size() == 0){
			displayLimitElement.prepend("<option value='"+displayLimit+"'>"+formatDisplayLimit+"</option>")
		}
		displayLimitElement.val(displayLimit) ;
	}
	
	function changeLimit(){
		var params ={
			searchLimit:$("#searchLimit").val(),
			displayLimit:$("#displayLimit").val()
		}
		if(parseInt(params.searchLimit) < parseInt(params.displayLimit)){
			showErrorMessage("查询上限不能小于显示上限！") ;
			return ;
		}
		$.post("/sim/logSearch/changeLimit",params,function(result){
			if(!result.success){
				showErrorMessage(result.message) ;
			}
		}) ;
	}
	
	/**
	 * 刷新日志列表数据
	 * @param rows 数据集合
	 */
	function refreshLogTableData(rows){
		$("#raw_log_content").html("");
		
		$("#log_query_table").datagrid("loadData",(function (){ 
			 $("#log_query_table").datagrid("loading"); 
			return rows;//需要加载的数据
		})());
		var conditionHeight = $('#log_query_condition_table').height();
		var height = 0;
		if(conditionHeight>0){
			height = 68+conditionHeight;
		}else{
			height = 68;
		}
		$('#log_query_table_layout').layout('panel', 'north').panel('resize',{height:height});
		$('#log_query_table_layout').layout('resize');
	}	
	function stopLogQuery(){
		//isCancel = true;
		//changeTableData(queryParams);
	}
	/**
	 * 根据列集和数据集创建日志查询表格
	 * @param cols 列集
	 * @param rows 数据集合
	 */
	function createLogTable(cols,rows,filters){
		//创建表格
		$('#log_query_table').datagrid({
			fit : true,
			nowrap : true,
			border : false,
			singleSelect : true,
		    columns : [cols],
		    data : rows,
		    striped:true,
		    loadMsg:'正在加载，请稍后。。。</a>',
		    //toolbar:'#log_query_table_toolbar',
		    onBeforeLoad:function(param){
		    	$('#log_query_table').datagrid('loading');
		    },
			onLoadSuccess:function(data){
				if(data && data.rows && data.rows.length > 0){
					$("#log_query_table").datagrid("selectRow",0) ;
				}
			},
			onSelect:function(index,indexdata){
				$("#raw_log_content").html(nvl(indexdata.ORIGINAL_DATA,""));
			},
			onHeaderContextMenu: function(e, field){
                e.preventDefault();
                showColumnConfig(e,0,0) ;
            },
            onRowContextMenu:function(e,rowIndex,rowData){
            	e.preventDefault();
            	menuContext.row = rowData ;
            	menuContext.rowIndex = rowIndex ;
            	$(this).datagrid("selectRow",rowIndex) ;
            	var target = e.target;
            	var field ;
            	if(target.tagName == "TD"){
            		field = $(target).attr("field");
            	}else{
            		var parent = $(target).parents("td") ;
                	var td= parent.get(parent.size()-1);
                	field = $(td).attr("field");
            	}
                $.each(columns,function(index,item){
                	if(field == item.dataField){
                		menuContext.column = item ;
          				return false ;
                	}
                }); 
                
                var fieldType = getFieldType(field) ;
                var logRightMenu = $('#logRightMenu'); 
                logRightMenu.children().each(function(index,domEl){
                	if($(domEl).attr("supportFieldTypes")){
                		//按钮支持的字段类型
                		var supportFieldTypes = $(domEl).attr("supportFieldTypes").split(",") ;
                		if($.inArray(fieldType,supportFieldTypes) > -1){
                			logRightMenu.menu("enableItem",domEl) ;
                		}else{
                			//如果当前列字段类型不被支持，则禁用此按钮
                			logRightMenu.menu("disableItem",domEl) ;
                		}
                	}
                });
                logRightMenu.menu('show', {
                	left: e.pageX,
                	top: e.pageY
                });
                if(menuContext.column.type == "ip"){
      				$('#src_menuTree').tree({   
      					url:'/sim/logSearch/contextMenuTree?ip=SRC_ADDRESS',
      					onSelect:function(node){
      						setQueryParams("SRC_ADDRESS","ip",rowData[menuContext.column.dataField],node.id,"源地址");
      					}
      				});
      				$('#dest_menuTree').tree({   
      					url:'/sim/logSearch/contextMenuTree?ip=DEST_ADDRESS',
      					onSelect:function(node){
      						setQueryParams("DEST_ADDRESS","ip",rowData[menuContext.column.dataField],node.id,'目的地址');
      						
      					}
      				});
                }
            }
		});
		
		
		//日志列表所有标题DOM
		var titles = $('.datagrid-view2 .datagrid-htable .datagrid-header-row:first>td');
		//遍历所有标题，如果标题字段field出现在过滤字段中则在其标题文本后方加上过滤图标
		$.each(titles, function(i, td) {
			var span = $(td).find('span:first');
			var field = $(td).attr('field');
			var title = $(span).text();
			if(field!="MESSAGE"){
				if(!!filters[field]){
					var span = $(span) ;
					span.append("<a class='log-filter hand' title='查询条件' field = '"+field+"'><i class='icon-filter'></i></a>")
					span.append("<a class='icon-pie hand' style='padding-left:10px;background-size:14px 14px' field = '"+field+"' rel='"+title+"' title='查看统计结果'></a>");
					if(getFieldType(field) == "ip"){
						span.append("<a class='icon-global-green hand' onclick=\"showIpLocationMap('"+field+"');\"></a>");
					}
				}
				
			}
			
		});
         //为日志可以下钻的字段统计添加点击事件
		$('span a.icon-pie').each(function(){
			$(this).click(function(){
				var field = $(this).attr('field');
				var text = $(this).attr('rel');
				$('#logFieldStatic_test').dialog('open').dialog('setTitle',text+"统计TOP(20)");
				simHandler.openLoadingModal($("#field_pie"), "统计中......");
				logFieldStatic(field,text);
			});
		});
		//为新增加的过滤图标增加qtip提示框，用于显示查询过滤器
		$('a.log-filter').each(function(){
			var txt = $(this).parent().text();
			var field = $(this).attr('field');
			var html = filters[field];
			
			$(this).click(function(){
				$('.query-expression-group').each(function(i){
					if(field == this.id){
					   $("select[name=" + field + "_operator]").attr('disabled','disabled');
						return false;
					}
				});
			});
			
			var tooltip = $(this).qtip({
				content : {
					text : html,
					title : {
						text : txt, // Give the tooltip a title using each elements text
						button : '关闭' // Show a close link in the title
					}
				},
				position : {
					corner : {
						target : 'bottomMiddle', // Position the tooltip above the link
						tooltip : 'topMiddle'
					},
					adjust : {
						screen : true // Keep the tooltip on-screen at all times
					},
					container: $('#log_query_table_column_tips')
				},
				show : {
					when : 'click',
					solo : true // Only show one tooltip at a time
				},
				hide : 'unfocus',
				style : {
					tip : true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
					border : {
						width : 0,
						radius : 4
					},
					name : 'light', // Use the default light style
					width : 270 // Set the tooltip width
				}
			});		
			//临时保存每个字段的qtip实例，用于隐藏操作
			column_tooltip[field] = tooltip;
		});
		
	}
	/**
	 * 根据字段名称获取字段信息
	 * @param fieldName
	 * @returns
	 */
	function getField(fieldName){
		var field = null;
		$.each(columns,function(index,item){
			if(item.dataField == fieldName){
				field = item ;
				return false ;
			}
		}) ;
		return field ;
	}
	/**
	 * 根据字段名称，获取字段类型
	 * @param fieldName
	 * @returns
	 */
	function getFieldType(fieldName){
		var field = getField(fieldName) ;
		return field ? field.type : null ;
	}
	function setQueryParams(field,type,content,deviceType,headText){
        searchStatus = false;
    	var selectNode = $('#log_query_tree').tree('find',deviceType);
    	if(selectNode != null){
    	   var childrenNode = $('#log_query_tree').tree("getChildren",selectNode.target);
    	   $('#log_query_tree').tree("select",childrenNode[0].target);
    	   $('#logRightMenu').menu('hide');
    	 }
        var operator = "等于";
        var text = "";
    	var queryArr = {};
    	text = headText + " " + operator +" "+ content;
    	queryArr = {
    			datatype : type,
    			operator : operator,
    			value : content
    	};
    	createQueryElement(field,text,queryArr);
    	searchStatus = true;
	}
	function showColumnConfig(event,leftOffset,topOffset){
		e = event || window.event ;
		if (!cmenu){
            createColumnMenu();
        }
        cmenu.menu('show', {
            left:nvl(e.pageX,e.clientX) + leftOffset,
            top:nvl(e.pageY,e.clientY) + topOffset
        });
	}
	function createColumnMenu(){
        cmenu = $('<div/>').appendTo('body');
        cmenu.menu({
            onClick: function(item){
                if (item.iconCls == 'icon-ok'){
                    $('#log_query_table').datagrid('hideColumn', item.name);
                    cmenu.menu('setIcon', {
                        target: item.target,
                        iconCls: 'icon-none'
                    });
                } else if(item.iconCls == "icon-none"){
                    $('#log_query_table').datagrid('showColumn', item.name);
                    cmenu.menu('setIcon', {
                        target: item.target,
                        iconCls: 'icon-ok'
                    });
                }else{
                	saveColumnConfig() ;
                }
            }
        });
        cmenu.menu('appendItem', {
            text: "保存配置",
            name: field,
            iconCls: "icon-save"
        });
        var fields = $('#log_query_table').datagrid('getColumnFields');
        for(var i=0; i<fields.length; i++){
            var field = fields[i];
            if(field == "rownumber"){
				continue ;
			}
            var col = $('#log_query_table').datagrid('getColumnOption', field);
            var iconCls = col.hidden ? "icon-none" : "icon-ok" ;
            cmenu.menu('appendItem', {
                text: col.title,
                name: field,
                iconCls: iconCls
            });
        }
    }
	function becomeReadOnly(id,flag){
		$("select[name='"+id + "_operator']").attr("disabled",flag);
	}
	function saveColumnConfig(){
		var columnConfig = $.map($('#log_query_table').datagrid('options').columns[0],function(column){
    		var columnTmp = {column:column.field,width:column.width,hidden:column.hidden ? true : false} ;
    		return columnTmp ;
    	}) ;
    	var selectNode = $("#log_query_tree").tree("getSelected") ;
    	var group = nvl(selectNode.attributes.groupId,"1") ;//列集
    	var deviceType = selectNode.attributes.deviceType ;//日志源类型 
    	var module = "/sim/log/logQuery/"+deviceType+"/" + group ;
    	var data = {module:module,config:JSON.stringify(columnConfig)};
    	$.post("/sim/authUser/saveColumnConfig",data,function(result){
    		if(result.success){
    			showAlertMessage("保存成功！") ;
    		}else{
    			showErrorMessage(result.message) ;
    		}
    	},"json") ;
	}
	function showIpTraceTree(traceField,value,onlyWanSrc){
		var requestParams = $.extend({},queryParams,{traceField:traceField,value:value,onlyWanSrc:onlyWanSrc}) ;
		var params = $.param(requestParams) ;
		window.open("/sim/logSearchResultStat/showLogIpStatPage?goPage=trace&"+params,"_blank") ;
	}
	function showIpRelationTree(){
		var params = $.param(queryParams) ;
		window.open("/sim/logSearchResultStat/showLogIpStatPage?goPage=relation&"+params,"_blank") ;
	}
	function showLogsTimeLine(traceField,value){
		var requestParams = $.extend({},queryParams,{traceField:traceField,value:value}) ;
		var params = $.param(requestParams) ;
		window.open("/sim/logSearchResultStat/logsTimeLine?"+params,"_blank") ;
	}
	function showIpLocationMap(fieldName){
		var params = $.extend({},queryParams,{statColumn:fieldName}) ;
		queryParams.statColumn = fieldName;
		var field = getField(fieldName) ;
		openDialog(field.headerText+"数量分布", 800, 500, "/page/log/logIpLocation.jsp",{id:"ipLocationMapDialog",params:params});
	}
	function onRowMenuClick(item){
		var field = menuContext.column.dataField ;
		var fieldValue = menuContext.row[menuContext.column.dataField];
		if(item.name == "traceTreeMenu"){
			showIpTraceTree(field,fieldValue);
		}else if(item.name == "timeLineMenu"){
			showLogsTimeLine(field, fieldValue) ;
		}
	}
	/**
	 * 创建字段查询过滤器DOM元素
	 * @param filter 字段查询过滤对象
	 */
	function createFilterElement(filter){
		//字段名(英文)
		var field = filter.name;
		//字段名(中文)
		var alias = filter.alias;
		//查询数据类型
		var datatype = filter.type;
		//查询运算符数组 用于下拉框
		var operators = filter.operators;
		//值数组 用于下拉框
		var values = filter.values && filter.values.length > 0 ? filter.values : null;
		
		
		var divEle = $("<div class='input-append'></div>");
		
		//运算符节点，下拉框
		var operEle = $("<select class='input-small' name='"+field+"_operator'></select>");
		
		//运算符节点，下拉框
		for(var i in operators){
			var option = $('<option></option>').attr('value',operators[i]).text(operators[i]);	
			operEle.append(option);
		}
		
		
		//值节点，如果是数组则创建下拉框，否则创建文本框
		var valEle = "";
		if(values){
			if(field == "FILE_NAME"){
				//文本框
				valEle = $("<input type='text' class='input-small'/>").attr('name',field);
			}else{
				//下拉框
				valEle = $("<select class='input-small'></select>").attr('name',field);
				for(var i in values){
					var option = $('<option></option>').attr('value',values[i].value).text(values[i].label);
					valEle.append(option);
				}
			}
		}else{
				//文本框
				valEle = $("<span id='" + field + "_span'><input type='text' class='input-small' name='" + field + "'/></span>");
		}
		//创建确定按钮并绑定点击事件
		var btnEle = $('<button class="btn" type="button">确定</button>').bind('click',function(){
			//运算符
			var operator = $("select[name='" + field + "_operator'] option:selected").val();
			//值-value
			var fieldValue = "";
			//值-text(显示)
			var fieldText = "";
			var text = "";
			if(values){
				if(field == "FILE_NAME"){
					fieldText = $("input[name='" + field + "']").val();
				    fieldValue = fieldText;
				}else{
				    fieldText = $("select[name='" + field + "'] option:selected").text();
				    fieldValue = $("select[name='" + field + "'] option:selected").val();
				}
			}else{
					fieldText = $("input[name='"+field+"']").val();
					fieldValue = fieldText;
					text = alias +" "+operator + " "+fieldText;
					
			}
			//如果值不含空格则继续
			 var queryAttr = {
					datatype : datatype,
					operator : operator,
					value : fieldValue
			};
			$("input[name='"+field+"']").val("");
			if(fieldValue){
					if(!/\s+/.test(fieldValue)){
						 if(field!="MSG_ID"){
							 text = alias +" "+operator + " "+fieldText;
						 }
					}
			}else{
				return;
			}
			createQueryElement(field, text, queryAttr);
		});
		divEle.append(operEle).append(valEle).append(btnEle);
		return divEle;
	}
	
	/**
	 * 创建查询条件组及组内条件的DOM元素
	 * @param field 字段
	 * @param text 显示的表达式文本
	 * @param queryAttr 查询的属性，用于创建到每个条件DOM元素中
	 */
	function createQueryElement(field, text, queryAttr,reloadData,host){
		if(reloadData == null || reloadData == undefined){
			reloadData = true ;
		}
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
				var index = $("#log_query_condition").children().size() % colors.length;
				groupDiv.css('background-color',colors[index]);
				groupDiv.append(groupBtn).append(div);
				$("#log_query_condition").append(groupDiv);
			}
			//查询条件DOM创建完成后，隐藏字段过滤器
			$(column_tooltip[field]).qtip("hide"); // Hides the tooltip
			//刷新日志列表
			if(reloadData){
				refreshLogTable('','',host);
			}
		}
	}
		
	/**
	 * 刷新日志列表
	 * @param filterField 过滤掉的字段(条件组)
	 * @param filterUuid 过滤掉的条件uuid(单个条件)
	 */
	function refreshLogTable(filterField,filterUuid,host){
		$("#raw_log_content").html("");
		var fieldNames = "",fieldOperators = "",fieldValues = "",fieldTypes = "";
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
					fieldOperators += ",";
					fieldValues += ",";
					fieldTypes += ",";
				}
				var bool = true;
				//遍历条件组内的所有条件
				$(this).children('div.query-expression').each(function(j){
					//每个条件的唯一UUID
					var uuid = $(this).attr('uuid');
					//如果当前条件没有被过滤掉
					if(uuid != filterUuid){
						var condition=this.value.toString().replace(/\s+/g, "&nbsp;");
						switch(condition){
						    case "非常低":
							    condition = 0;
							    break;
						    case "低":
						    	condition = 1;
						    	break;
						    case "中":
						    	condition = 2;
						    	break;
						    case "高":
						    	condition = 3;
						    	break;
						    case "非常高":
						    	condition = 4;
						    	break;
							
						}
						
						var oper = $("select[name='" + field + "_operator'] option:selected").val();
						if(oper == "between"){
							var value = condition.substring(1,condition.indexOf('至'))+"#"+condition.substring(condition.indexOf('至')+1,condition.length);
							condition = "";
							condition = value;
						}
						if(bool){
							bool = false;
							fieldOperators += $(this).attr('operator');
							fieldValues += condition;
							fieldTypes += $(this).attr('datatype');
						}else{
							//fieldOperators += (" " + $(this).attr('operator'));
							fieldValues += (" " + condition);
							//fieldTypes += (" " + $(this).attr('datatype'));
						}
					}
				});
			}
		});
		//创建查询条件
		var searchParams = {};
		searchParams.pageNo = 1;
		var node = $("#log_query_tree").tree("getSelected");
		searchParams.deviceType = node.attributes.deviceType;
		searchParams.group = node.attributes.groupId;
		if(node.attributes.filter){
			  var filtercondition = node.attributes.filter;
			  var filtersplit = filtercondition.split(" AND ");
			  for(var i = 0 ; i < filtersplit.length ; i++){
				  var childrenFilter = $.trim(filtersplit[i]).split("=");
				  if(childrenFilter[0].indexOf("!") >= 0){
					 fieldNames += "," + childrenFilter[0].substring(0,childrenFilter[0].length-1);
					 fieldOperators += "," + "不等于";
				  }else{
					 fieldNames += "," + childrenFilter[0];
					 fieldOperators += "," + "等于";
				  }
				 fieldValues += "," + childrenFilter[1].substring(1,childrenFilter[1].length-1);
				 fieldTypes += "," + "String";
			   }
			
		}
		searchParams.conditionName = fieldNames;
		searchParams.operator = fieldOperators;
		searchParams.queryContent = fieldValues;
		searchParams.queryType = fieldTypes;
		searchParams.queryStartDate = $('#begin_time').val();
		searchParams.queryEndDate = $('#end_time').val();
		if(host!=null){
			searchParams.host = host;
		}
		conName = fieldNames;
		conOperator = fieldOperators;
		conQueryContent = fieldValues;
		conQueryType = fieldTypes;
		//执行日志查询定时器
		logTableLoadDataTimer(searchParams);
		var conditionHeight = $('#log_query_condition_table').height();
		var height = 0;
		if(conditionHeight>0){
			height = 68+conditionHeight;
		}else{
			height = 68;
		}
		$('#log_query_table_layout').layout('panel', 'north').panel('resize',{height:height});
		$('#log_query_table_layout').layout('resize');
	}
	/**
	 * 清除查询相关条件及临时对象
	 */
	function resetQuery(){
		//清空列标题对应过滤器对象
		column_tooltip = {};
		//清空列标题过滤器DOM元素
		$('#log_query_table_column_tips').empty();
		//清空所有查询条件DOM元素
		$('#log_query_condition').empty();
		//重置创建表格状态
		createdTableStatus = false;
	}
	function onBeforeSelect(node){
		if(!logQueryStatus){
			return false;
		}
	}
	
	/**
	 * 当选择日志类型树节点后，折叠查询面板并初始化查询参数，最后调用定时器
	 */
	var oldNode="";
	function onSelectTreeNode(node){
		if(cmenu){
			cmenu.menu("destroy") ;
			cmenu = null ;
		}
		var isLeaf = $('#log_query_tree').tree('isLeaf',node.target);
		if(!isLeaf || simHandler.logQueryObject){
			return;
		}else{
			if(oldNode){
				if(node.attributes.deviceType == oldNode){//设备类型相等，日志源不需要重新加载
					logDataSourceStatus = true;
				}else{//需要重新加载日志源
					logDataSourceStatus = false;
				}
			}
			oldNode = node.attributes.deviceType;
		}
		$('#log_query_table_layout').layout('panel', 'north').panel('resize',{height:68});
		$('#log_query_table_layout').layout('resize');
		$("#raw_log_content").html("");
		
		//重置页面所DOM元素及临时变量		
		 resetQuery();
		 if(node.attributes.deviceType == "Esm/Topsec/SystemRunLog" || node.attributes.deviceType == "Esm/Topsec/SystemLog"){
			isHaveSourceLog = false;
			$("#log_searchDiv").layout('remove','south');
		 }else{
			isHaveSourceLog = true;
			$("#log_searchDiv").layout('add',{region:'south',height:100,content:'<span class="label label-info">原始日志:</span><p class="muted alert" id="raw_log_content" style="height:55px;overflow:auto;"></p>'});
		 }
		queryParams = {};
		var host = $("#log_source_sel").find(':selected').attr('ip');
		if(host){
			
			if(logDataSourceStatus){
				queryParams.host = host;
			}else{
				queryParams.host = "";
			}
			queryParams.nodeId = $("#log_source_sel").val();
		}
		
		queryParams.deviceType = node.attributes.deviceType;
		queryParams.group = node.attributes.groupId;
		queryParams.queryStartDate = $('#begin_time').val();
		queryParams.queryEndDate = $('#end_time').val();
		queryParams.pageSize = 20;
		queryParams.pageNo = 1;
		var fieldNames = "",fieldOperators = "",fieldValues = "",fieldTypes = "";
		if(node.attributes.filter){
			var filtercondition = node.attributes.filter;
			var filtersplit = filtercondition.split(" AND ");
			for(var i = 0 ; i < filtersplit.length ; i++){
				var childrenFilter = $.trim(filtersplit[i]).split("=");
				if(childrenFilter[0].indexOf("!") >= 0){
					 fieldNames += childrenFilter[0].substring(0,childrenFilter[0].length-1);
					 fieldOperators += "不等于";
				}else{
					fieldNames += childrenFilter[0];
				    fieldOperators += "等于";
				}
				fieldValues += childrenFilter[1].substring(1,childrenFilter[1].length-1);
			    fieldTypes += "String";
			    if(i<filtersplit.length-1){
				  fieldNames += ",";
				  fieldOperators += ",";
				  fieldValues += ",";
				  fieldTypes += ",";
			    }
			}
			conName = fieldNames;
			conOperator = fieldOperators;
			conQueryContent = fieldValues;
			conQueryType = fieldTypes;
		}else{
			conName = conOperator = conQueryContent = conQueryType = "";
		}
		queryParams.conditionName = fieldNames;
		queryParams.operator = fieldOperators;
		queryParams.queryContent = fieldValues;
		queryParams.queryType = fieldTypes;
		//调用日志查询定时器
		if(searchStatus){
			logTableLoadDataTimer(queryParams);
		}
	}
	
	/**
	 * 当改变日志接收时间，则重新加载日志列表
	 */
	function onChangeReceiptTime(){
		var searchParams = {};
		searchParams.pageNo = 1;
		searchParams.queryStartDate = $('#begin_time').val();
		searchParams.queryEndDate = $('#end_time').val();
		logTableLoadDataTimer(searchParams);
	}
	
	//日期范围选择器回调函数	
	var cb = function(start, end) {
		if((end-start)/(1000*60*60*24) > 365){
			showAlarmMessage("开始与结束时间范围不能超过一年！") ;
			return ;
		}
	    $('#begin_time').val(start.format('YYYY-MM-DD HH:mm:ss'));
	    $('#end_time').val(end.format('YYYY-MM-DD HH:mm:ss'));
	    onChangeReceiptTime();
	};		
    var begin_time;
	var end_time ;
	var deafultValue ;
	if(simHandler.logQueryObject != null ){
		var startDate = simHandler.logQueryObject.queryStartDate;
		var endDate = simHandler.logQueryObject.queryEndDate;
		begin_time = startDate;
		end_time = endDate;
		deafultValue = -1;
		
	}else{
		begin_time = servertime;
		end_time = servertime;
		deafultValue = 1;
	}
  	//日期范围选择器初始化参数
	var optionSet = {
	    startDate: moment(begin_time).subtract('hours', deafultValue),
	    endDate: moment(end_time),
	    maxDate: false,
	    minDate: false,
	    showDropdowns:true,
	    timePicker: true,//显示时间选择器
	    timePickerIncrement: 1,//时间间隔
	    timePicker12Hour: false,//12小时制
	    locale: locales['zh-CN'],
	    format: 'YYYY-MM-DD HH:mm:ss',
	    opens: 'right',
	    ranges: {
	       '最近1小时': [moment(begin_time).subtract('hours',1), moment(end_time)],
	       '最近6小时': [moment(begin_time).subtract('hours',6), moment(end_time)],
	       '今天': [moment(begin_time).startOf('day'), moment(end_time)],
	       '昨天': [moment(begin_time).subtract('days', 1).startOf('day'), moment(end_time).subtract('days', 1).endOf('day')]
	    },
	    onMonthYearChange:function(isLeft,month,year){
	    	if(isLeft){
	    		var oldStartDate = this.startDate.clone() ;
	    		this.setStartDate(year+"-" + (month+1) + "-01 00:00:00") ;
	    		this.oldStartDate = oldStartDate ;
	    	}else{
	    		var oldEndDate = this.endDate.clone() ;
	    		var endDate = simHandler.newServerDate() ;
	    		endDate.setUTCFullYear(year, month, this.endDate.date()); 
	    		endDate.setUTCHours(15, 59, 59, 0);//UTC时间15点相当于中国23 
	    		this.setEndDate(endDate) ;
	    		this.oldEndDate = oldEndDate ;
	    	}
	    }
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
	var savetime = [];//用于保存时间轴时间点
	function createLogTimelineChartInstance(seriesData,category){
		
		var w = $('#log_timeline_chart').parent().width()-2;
		var h = $('#log_timeline_chart').parent().height();
		var barWidth ;
		if(seriesData != null){
			if(seriesData.length<50){
				barWidth = 20;
			}
		}
		$("#log_timeline_chart").css("height",h);
		$("#log_timeline_chart").css("width",w);
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
		                   // 位置回调
		            	   var step = 320;
		            	   var x = p[0];
		            	   if(p[0] - step < 0){
		            		   x = p[0];
		            	   }
		            	   if(p[0]+step > $(window).width()){
		            		   x = p[0]-step;
		            	   }
		                   return [x, p[1]-10];

		                  // return [p[0] + 10, p[1] - 100];
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
		                return  startdate + "至" + enddate +  '<br/>匹配的日志：<span style="color:red">' + params.value + '</span>条';   
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
		                    splitLine:{
		                    	   show:false
		                       }
		                }
		            ],
		            noDataLoadingOption: {
		    			textStyle:{
		            		fontWeight:'bold'
		            	},
		                text: '暂无数据',
		                effect: 'bubble',
		                effectOption: {
		                    effect: {
		                        n: 0
		                    }
		                }
		    		},
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
						var myChart = ec.init(document.getElementById("log_timeline_chart"));
						myChart.setOption(option);
						myChart.on('click', function(param){
							 if (param.type == 'click') { 
								    var str = param.name;
			                		var dateArr = str.split("&");
			                		var date1 = parseInt(dateArr[0]);
			                		var startdate = formatterDate(date1);
			                		var date2 = parseInt(dateArr[1]);
			                		var enddate = formatterDate(date2);
			                		if(startdate == $("#begin_time").val() && enddate == $("#end_time").val()){
			                			return false;
			                		}
			                		savetime[savetime.length] = $("#begin_time").val()+"&"+$("#end_time").val();
			                		$("#begin_time").val(startdate);
			                		$("#end_time").val(enddate);
			                		$("#floatDiv").fadeIn("normal");
			                		var searchParams = {};
			                		searchParams.pageNo = 1;
			                		searchParams.queryStartDate = startdate;
			                		searchParams.queryEndDate = enddate;
			                		logTableLoadDataTimer(searchParams);
							 }
							
						});
					});
	}
	//此方法用于返回上一次查询
	function backSearch(){
		var str = savetime[savetime.length-1];
		savetime.pop();
		var dateArr = str.split("&");
		var startdate = dateArr[0];
		var enddate = dateArr[1];
		$("#begin_time").val(startdate);
		$("#end_time").val(enddate);
		var searchParams = {};
		searchParams.pageNo = 1;
		searchParams.queryStartDate = startdate;
		searchParams.queryEndDate = enddate;
		logTableLoadDataTimer(searchParams);
		if(savetime.length == 0){
			$("#floatDiv").hide();
		}
	}
	function refreshLogTimelineChartData(data){
		if(data){
			var seriesData = [];
			var category = [];
			for(var i = 0 ; i < data.length ; i ++){
				seriesData[i] = data[i].y;
				category[i] = data[i].x;
			}
			createLogTimelineChartInstance(seriesData,category);
		}
		
	}
	/**
	 * 初始化页面
	 */
	function initPage(){
		//初始化左侧日志查询规则树
		$('#log_query_tree').tree({   
		    url:'/sim/logSearch/getTreeForGroup',
		    onBeforeSelect:onBeforeSelect,
		    onSelect:onSelectTreeNode,
		    onLoadSuccess:function(node, data){
		    	var log_cat_tree = $("#log_query_tree") ;
		    	//获取根节点
		    	var children ;  
		    	var isSelect = false;
		    	if(simHandler.logQueryParam){
		    		isSelect = true ;
		    		doQuery(log_cat_tree,simHandler.logQueryParam) ;
		    	}else if (simHandler.logQueryObject){
		    		var params = simHandler.logQueryObject;
		    		var selectNode = log_cat_tree.tree("find",params.deviceType) ;
		    		children = log_cat_tree.tree("getChildren",selectNode.target) ;
		    		for(var i = 0 ; i < children.length ; i++){
		    			if(params.group == children[i].attributes.groupId){
		    				log_cat_tree.tree("select",children[i].target); 
		    				isSelect = true;
		    				break;
		    			}
		    		}
		    		$('#begin_time').val(params.queryStartDate);
		    		$('#end_time').val(params.queryEndDate);
		    		$.ajax({
		    	        url: '/sim/basicreport/logColumnTemplet?securityObjectType='+params.deviceType,
		    	        type: 'POST',
		    	        dataType:'json',
		    	        contentType:"text/javascript",
		    	        success: function(jsondata){
		    	        	simLogQueryHandler.setLogQueryObjToLogDetail(params,jsondata[0]);
		    	        }
		    		});
		    		simHandler.logQueryObject=null;
		    	}else{
		    		var rootNode = log_cat_tree.tree('getRoot');
		    		children = log_cat_tree.tree('getChildren',rootNode.target) ;
		    	}
		    	if(!isSelect){
		    		log_cat_tree.tree("select",children[0].target); 
		    	}
		    }
		});  	
		//初始化日期范围选择器
			$('#receipt_time').daterangepicker(optionSet, cb);	
		
		//当关闭查询条件组后，刷新日志列表
		$('.query-expression-group').die().live('closed', function (event) {
			var target = event.target;
			var field = $(target).attr('id');
			becomeReadOnly(field,false);
			refreshLogTable(field,"");
		});
		
		//当关闭一个查询条件,刷新日志列表
		$('.query-expression').die().live('closed', function (event) {
			//禁止事件冒泡
			event.stopPropagation();
			var target = event.target;
			//找到所有同辈查询条件
			var siblings = $(target).siblings('div.query-expression');
			//如果没有同辈查询条件，则关闭其所属查询条件组，否则刷新日志列表
			var fieldId = $(target).parent().attr("id");
			becomeReadOnly(fieldId,false);
			if(siblings.size() == 0){
				$(target).parent().alert('close');
			}else{
				var uuid = $(target).attr('uuid');
				refreshLogTable("",uuid);
			}
		});			
		
		//当改变日志源的后，刷新日志列表
		$('#log_source_sel').change(function(event){
			event.stopPropagation();
			var searchParams = {};
			searchParams=queryParams;
			searchParams.host = $(this).find(':selected').attr('ip');
			searchParams.pageNo = 1;
			searchParams.pageSize = 20;
			searchParams.nodeId = $(this).val();
			logTableLoadDataTimer(searchParams);
		});
		
		//创建日志数据列表
		//createLogTable([],[],[]);
		//创建分页菜单
		createLogTablePager(0,0,0,0,20,0,false,0);
		
		createLogTimelineChartInstance();
		
	}
	
function doQuery(log_cat_tree,logQueryParam){
	var deviceType = nvl(logQueryParam.securityObjectType,"");
	var group = nvl(logQueryParam.group,"1") ;
	var selectNode = log_cat_tree.tree("find",deviceType + "/" + group) ;
	if(!selectNode){
		if(isBlank(deviceType)){
			selectNode = log_cat_tree.tree("getChildren",log_cat_tree.tree("find",null).target)[0] ;
		}else{
			selectNode = log_cat_tree.tree("getChildren",log_cat_tree.tree("find",deviceType).target)[0] ;
		}
	}
	var condition = logQueryParam.condition ;
	$('#begin_time').val(logQueryParam.queryStartDate);
	$('#end_time').val(logQueryParam.queryEndDate);
	if(condition && condition.length > 0){
		searchStatus = false ;
		log_cat_tree.tree("select",selectNode.target); 
		for(var i in condition){
			var fieldMeta = condition[i] ;
			var text = fieldMeta.alias + " "+ fieldMeta.operator + " "+fieldMeta.value ;
			var queryAttr = {datatype:fieldMeta.type,value:fieldMeta.value,operator:fieldMeta.operator} ;
			createQueryElement(fieldMeta.name, text, queryAttr, i == (condition.length - 1) , logQueryParam.host) ;
		}
	}else{
		log_cat_tree.tree("select",selectNode.target);
	}
}	
//根据点击事件查询相关日志信息
function showLogDetail(fieldname,conditionIndex,type,headText,status){
	var condition = "";
	if(status == "1"){
		//rows = $("#field_table").datagrid("getRows");
		condition = conditionIndex;
	}else{
		var rows = $("#log_query_table").datagrid("getRows");
		condition = rows[conditionIndex][fieldname];
	}
    var operator = "";
    var text = "";
	var queryArr = {};
    $('.query-expression-group').each(function(i){
		if(fieldname == this.id){
			$(this).children('div.query-expression').each(function(j){
				operator = $(this).attr('operator');
			    return false;
			});
		}
	});
    
    if(operator == ""){
    	operator = "等于";
    }
    $("select[name='" + fieldname + "_operator']").val(operator);
    if(operator == "between"){
		condition = "从"+condition + " 至 " + condition;
		text = headText + " "+ condition;
	}else{
		text = headText + " " + operator + " " + condition;
	}
	queryArr = {
			datatype : type,
			operator : operator,
			value : condition
	};
	
	createQueryElement(fieldname,text,queryArr);
	
	if(status == '1'){
		$('#logFieldStatic_test').dialog("close");
	}
}

simLogQueryHandler.setLogQueryObjToLogDetail = function(paramsObject,colsInfo){
	if(null == paramsObject){return paramsObject;}
	var conditionName = paramsObject.conditionName + '';
	var queryContent = paramsObject.queryContent + '';
	var operatorCon = paramsObject.operator + '';
	var queryTypeCon = paramsObject.queryType + '';
	
	if(conditionName.indexOf(',')>0){
		var conditionnameArr = conditionName.split(',');
		var operatorArr = operatorCon.split(',');
		var contentArr = queryContent.split(',');
		var queryTypeArr = queryTypeCon.split(',');
		
		for(var i = 0 ; i < conditionnameArr.length ; i++){
			var condtionname = conditionnameArr[i];
			var operator = operatorArr[i];
			var condition = contentArr[i];
			var querytype = queryTypeArr[i];
			var text = null;
			
			if(operator == "between"){
				var conditiontemp = condition.split('#', 2);
				condition="从" + simLogQueryHandler.valueToCN(condtionname,conditiontemp[0]) + "至" + simLogQueryHandler.valueToCN(condtionname,conditiontemp[1]);
				text = simLogQueryHandler.conditionCNname(conditionnameArr[i],colsInfo) + " " + condition;
			}else{
				text = simLogQueryHandler.conditionCNname(conditionnameArr[i],colsInfo) + " " + operator + " " + simLogQueryHandler.valueToCN(condtionname,condition);
			}
			var queryArr = simLogQueryHandler.setQueryArray(querytype,operator,condition);
			createQueryElement(conditionnameArr[i],text,queryArr,i == (conditionnameArr.length -1),paramsObject.host);
		}
	}else{
		var text = null;
		if(operatorCon == "between"){
			var conditiontemp = queryContent.split('#', 2);
			queryContent= "从" + simLogQueryHandler.valueToCN(conditionName,conditiontemp[0])+"至"+simLogQueryHandler.valueToCN(conditionName,conditiontemp[1]);
			text = simLogQueryHandler.conditionCNname(conditionName,colsInfo)+" "+queryContent;
		}else{
			text = simLogQueryHandler.conditionCNname(conditionName,colsInfo)+" "+operatorCon+" "+simLogQueryHandler.valueToCN(conditionName,queryContent);
		}
		var queryArr = simLogQueryHandler.setQueryArray(queryTypeCon,operatorCon,queryContent);
		createQueryElement(conditionName,text,queryArr,true,paramsObject.host);
	}
	
};

simLogQueryHandler.setQueryArray=function(type,operator,condition){
	var queryArr={};
	queryArr={
			datatype : type,
			operator : operator,
			value : condition
	};
	return queryArr;
};
/**
 * report 中映射的中文名
 */
simLogQueryHandler.conditionCNname=function(conditionname,colsInfo){
	if(undefined == colsInfo || colsInfo.length <1)
		return conditionname;
	for(var i=0;i<colsInfo.length;i++)
		if(conditionname === colsInfo[i].name)
			return colsInfo[i].alias;
	
	return '条件';
};

simLogQueryHandler.valueToCN=function(conditionname,value){
	if(conditionname === 'PRIORITY'){
		if(value==0){
			return '非常低';
		}else if(value==1){
			return '低';
		}else if(value==2){
			return '中';
		}else if(value==3){
			return '高';
		}else if(value==4){//priority
			return '非常高';
		}
		return value;
	}else if(conditionname === 'START_TIME'){
		var temp='';
		temp=value.substring(0, 4)+'-'+value.substring(4, 6)+'-'+value.substring(6, 8)+' '+value.substring(8, 10)+':'+value.substring(10, 12)+':'+value.substring(12, value.length);
		return temp;
	}
	return value;
};
//初始化加载数据
(function(){
	if(simHandler.log_statistics_dialog){
		simHandler.log_statistics_dialog = null;
	}
	$('#log_tabs').tabs({   
	    border:false,   
	    onSelect:function(title){   
	    	var  tab = $('#log_tabs').tabs('getSelected');
	    	var url = "";
	    	if(title == "统计主题"){
	    	   url = "/page/log/logtasklist.html";
	    	}else if(title == "统计任务"){
	    		url = "/page/log/scheduleTaskList.html";
	    	}
	        $('#log_tabs').tabs('update', {
	          tab: tab,
	          options: {
		        href: url  
	          }
	       });   
	      simHandler.closelogStaticsDialog();
	    }   
	});
	//初始化日志接收开始时间和结束时间
	$('#begin_time').val(moment(servertime).subtract('hours',1).format('YYYY-MM-DD HH:mm:ss'));
	$('#end_time').val(moment(servertime).format('YYYY-MM-DD HH:mm:ss'));
    $("#floatDiv").hide();	
    initPage();
    showExportTip(isHaveSourceLog);
    queryConditionList();
	
})();

function logToolbar(item){
	if(item.name == "traceTree"){
		showIpTraceTree();
	}else if(item.name == "relationTree"){
		showIpRelationTree();
	}else if(item.name == "timeLine"){
		showLogsTimeLine(null,null);
	}else if(item.name == "wanTraceTree"){
		showIpTraceTree(null,null,true);
	}
}
//判断选择导出的日志类型
function expLog(item){
	if(item.name =='0' || item.name=="2"){//当前
		exportCurLog(item.name);
	}
	if(item.name=='1' || item.name=="3"){//所有的日志
		exportAllLog(item.name);
	}
}
//导出所有日志
function exportAllLog(nameArr){
	 var queryParams={};
	 queryParams =setLogCondition('','');
	 if(nameArr=="1"){//等于1 指的是所有格式化日志
		 queryParams.isFormate=1;
	 }
	 if(nameArr=="3"){//等于3指的是所有原始日志
		 queryParams.isFormate=3;
	 }
	 simHandler.openBodyLoadingModal("正在导出，请稍后。。。");
	 $.ajax({
         url: '/sim/logSearch/exportLogs',
         type: 'POST',
         data: JSON.stringify(queryParams),
         dataType:'json',
         contentType:"text/javascript",
         success: function(data){
            if(data.ftpfilepath!="" && data.ftpfilepath!=undefined){
        		 var time = (new Date()).valueOf();
    	         window.location="/sim/logSearch/downloadFile?filename="+data.ftpfilepath+"&time="+time;
    	            	 
            }else{
            	showErrorMessage("导出失败");
            }
            simHandler.closeLoadingModal($("body"));
        }
   });
}
//导出当前日志
function exportCurLog(nameArr){
	   var page = $('#log_query_table_pager').pagination("options");
	   var queryParams={};
		queryParams = setLogCondition('','');
		queryParams.pageSize = page.pageSize;
		queryParams.pageNo = page.pageNumber;
		
		if(nameArr=="0"){//等于0 指的是当前格式日志
			 queryParams.isFormate=0;
		 }
		 if(nameArr=="2"){//等于2 指的是当前原始日志
			 queryParams.isFormate=2;
		 }
		 simHandler.openBodyLoadingModal("正在导出，请稍后。。。");
		 $.ajax({
	         url: '/sim/logSearch/exportCurPageLogs',
	         type: 'POST',
	         data: JSON.stringify(queryParams),
	         dataType:'json',
	         contentType:"text/javascript",
	         success: function(data){
	            if(data.ftpfilepath){
	        		var time = (new Date()).valueOf();
	    	        window.location="/sim/logSearch/downloadFile?filename="+data.ftpfilepath+"&time="+time;
	            }else{
	            	showErrorMessage("导出失败");
	            }
	            simHandler.closeLoadingModal($("body"));
            }
	   });
 }
//导出日志时，设置查询条件
function setLogCondition(filterField,filterUuid){
	var fieldNames = "",fieldOperators = "",fieldValues = "",fieldTypes = "";
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
					fieldOperators += ",";
					fieldValues += ",";
					fieldTypes += ",";
				}
				var bool = true;
				//遍历条件组内的所有条件
				$(this).children('div.query-expression').each(function(j){
					//每个条件的唯一UUID
					var uuid = $(this).attr('uuid');
					//如果当前条件没有被过滤掉
					if(uuid != filterUuid){
						var condition = this.value.toString().replace(/\s+/g, "&nbsp;");
						switch (condition){
						    case "非常低":
							    condition = 0;
							    break;
						    case "低":
								condition = 1;
								break;
						    case "中":
								condition = 2;
								break;
						    case "高":
								condition = 3;
								break;
						    case "非常高":
								condition = 4;
								break;
						}
						var oper = $("select[name='"+field+"_operator'] option:selected").val();
						if(oper == "between"){
							var value = condition.substring(1,condition.indexOf('至'))+"#"+condition.substring(condition.indexOf('至')+1,condition.length);
							condition = "";
							condition = value;
						}
						if(bool){
							bool = false;
							fieldOperators += $(this).attr('operator');
							fieldValues += condition;
							fieldTypes += $(this).attr('datatype');
						}else{
							//fieldOperators += (" " + $(this).attr('operator'));
							fieldValues += (" " + condition);
							//fieldTypes += (" " + $(this).attr('datatype'));
						}
					}
				});
			}
		});
		//创建查询条件
		var queryParams = {};
		var log_source=$("#log_source_sel").children("option:selected").text();
		var node=$("#log_query_tree").tree("getSelected");
		queryParams.deviceType = node.attributes.deviceType;
		if(log_source!="全部"){
			queryParams.host =$("#log_source_sel").find(':selected').attr('ip');
			queryParams.nodeId = $("#log_source_sel").val();
		}else{
			queryParams.host ="";
			queryParams.nodeId ="";
		}
		
		queryParams.group = node.attributes.groupId;
		queryParams.pageSize = 20;
		queryParams.pageNo = 1;
		if(node.attributes.filter){
			  var filtercondition=node.attributes.filter;
			  var filtersplit=filtercondition.split(" AND ");
			  for(var i=0;i<filtersplit.length;i++){
				  var childrenFilter=$.trim(filtersplit[i]).split("=");
					 if(childrenFilter[0].indexOf("!") >= 0){
						 fieldNames+=","+childrenFilter[0].substring(0,childrenFilter[0].length-1);
						 fieldOperators+=","+"不等于";
					}else{
						fieldNames+=","+childrenFilter[0];
					     fieldOperators+=","+"等于";
					}
					 fieldValues+=","+childrenFilter[1].substring(1,childrenFilter[1].length-1);
					 fieldTypes+=","+"String";
			   }
			
		  }
		queryParams.conditionName = fieldNames;
		queryParams.operator = fieldOperators;
		queryParams.queryContent = fieldValues;
		
		queryParams.queryType = fieldTypes;
		queryParams.queryStartDate = $('#begin_time').val();
		queryParams.queryEndDate = $('#end_time').val();
		return  queryParams;
	}
function  dialogPanel(){
	var log_statistics_panel = $('#log_query_layout').layout('panel','center');
	var w = $(log_statistics_panel).width();
	var h = $(log_statistics_panel).height();
	var url="/page/log/logtaskcreate2.html";
	var node = $("#log_query_tree").tree("getSelected");
	simHandler.log_stat = {
						   operation : "add",conditionNames : conName,operators : conOperator,
						   queryContent : conQueryContent,queryType : conQueryType,
						   deviceType : node.attributes.deviceType,groupId : node.attributes.groupId,
						   queryCondition : $("#log_query_condition").text(),
						   startTime : $('#begin_time').val(),endTime: $('#end_time').val(),
						   dataSourceIp : $("#log_source_sel").children("option:selected").attr("ip"),
						   dataSourceName : $("#log_source_sel").children("option:selected").text()
						   } ;
	var log_staticstics_div= $("<div id='log_statistics_dialog'></div>");
	log_staticstics_div.appendTo($("#log_searchDiv"));
	simHandler.log_statistics_dialog = $('#log_statistics_dialog').dialog({
		href:url,
		style:{'padding':0,'border':0},
		top:0,
		left:0,
		width:w,
		height:h,
		inline:true,
		noheader:true,
		modal:true,
		border:false,
		fitColumns:true,
		collapsed:true,
		onCollapse:onCollapseDialog
	}).dialog('expand',true);	
	closeQueryConditionList();
}
var tempcount = 0;
//当折叠日志查询弹出窗口后
function onCollapseDialog(){
	tempcount++;
	if(tempcount%2 == 0 && simHandler.log_statistics_dialog){
		try{
			simHandler.log_statistics_dialog.dialog('destroy');
		}catch(e){
		}
		//$('#log_statistics_dialog').remove() ;
		simHandler.log_statistics_dialog = null;
	}
}

function closeFieldDialog(){
	//$("#field_pie").html("");
}
function closeFieldModalDialog(){
	$('#logFieldStatic_test').dialog("close");
}
var logFieldQuery = {exportLogFieldStatic:null};
//统计日志可以下钻的字段
function logFieldStatic(params, headText) {
	 queryParams.statColumn = params;
	 var host = $("#log_source_sel").find(':selected').attr('ip');
	 queryParams.host = host;
	 logFieldQuery.exportLogFieldStatic = function(){
		window.location = "/sim/logSearchResultStat/exportLogField?exportType=xls";
	 };
		$.ajax({
            url: '/sim/logSearchResultStat/doLogFieldStatic',
            type: 'POST',
            data: JSON.stringify(queryParams),
            dataType:'json',
            contentType:"text/javascript",
            success: function(data){
            	if(data.flag == "failure"){
            		showAlertMessage(data.error);
            	}else if(data.flag == "success"){
            		var col = [];
                	col.push({field:params,title:headText},
                	{field:'result',title:'统计结果',width:15},
                	{field:'percent',title:'百分比'});
                	logFieldTable(col,data.tableData,params,headText,data.columnType);
                	simHandler.closeLoadingModal($("#field_pie"));
            	}
            	
            }
	});
}
function logFieldTable(cols,data,params,headText,columnType){
	//设置图例数量
	var lengend_data = [];
	var series_data = [];
	var i =0;
	$.each(data,function(index,item){
			series_data.push({name:item[params],value:item.result});
			lengend_data[i] = item[params];
		i++;
	});
    var option = {
    		color: simHandler.colors,
//    		legend:{
//    			orient: 'vertical',
//    			 x: 'left', // 'center' | 'left' | {number},
//    		     y: 'bottom', 
//    			data:lengend_data
//    		},
    		 tooltip : {
    		        trigger: 'item',
    		        formatter: "{a} <br/>{b} : {c} ({d}%)"
    		    },
    		    toolbox: {
    		        show : true,
    		        feature : {
    		            dataView : {
    		            	 readOnly: true,
    		            	 show:true,
    						 lang : ["<label style='text-align:center;'><b>"+headText+"统计列表</b></label>","返回"],
    						 optionToContent: function(opt) {
    							  return  createTable(cols,data,params,headText,columnType);
    						   }
    		            },
    		            exp : {
    		                show : true,
    		                title : '导出',
    		                icon : 'image://../../img/icons/ecxel.png',
    		                onclick : function (params){
    		                	logFieldQuery.exportLogFieldStatic();
    		                }
    		            }
    		        }
    		    },
            series :[{
            	     type:'pie',
                     radius : '50%',
                     itemStyle : {
 		                normal : {
 		                    label : {
 		                        show : true
 		                    },
 		                    labelLine : {
 		                        show : true
 		                    }
 		                }
// 		                ,emphasis: {
// 		                   label: {
// 		                      show: true,
// 		                      position: 'outer'
// 		                  },
// 		                  labelLine: {
// 		                      show: true,
// 		                      lineStyle: {
// 		                          color: 'red'
// 		                      }
// 		                  }
 	//	              }
                      },
                     data:series_data
                     }]
   };
    
    require.config({
		paths : {
			echarts : '/js/echart/build/dist'
		}
	});
	
	require(
			[
			    'echarts',
				'echarts/chart/pie',
				'echarts/config'
				], 
			function(ec) {
				if($("field_pie").html()!=null){
					$("#field_pie").html("");
				}
				var myChart = ec.init(document.getElementById("field_pie"));
				myChart.setOption(option);
				myChart.on('click', function(param){
					 if (param.type == 'click') { 
						 showLogDetail(params, param.name, columnType, headText, 1);
					 }
					
				});
			});
   
	
//	$("#field_table").datagrid({
//		fit:true,
//		fitColumns:true,
//		nowrap:true,
//		border:1,
//		singleSelect:true,
//		striped:true,
//	    columns : [cols],
//	    data : data
//	}).datagrid("resize");
}
function createTable(cols,tableData,params,headText,columnType){
	  var table = '<table style="text-align:center;width:90%;border-bottom:1px dotted #ccc;margin:15px 0px 0px 20px;">';
		table += '<tr style="border-bottom:1px dotted #ccc;">';
	  $.each(cols,function(index,item){
		 table += "<td align='left'><b>" + item.title + "</b></td>";
	 });
	 
	 table += "</th></tr><tbody>";
	 $.each(tableData,function(index,item){
		 table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
		 $.each(cols,function(groupIndex,groupItem){
			 var percent = item[groupItem.field];
			 if(groupItem.field == 'percent'){
				 percent = '<div  style="margin-left:2px;margin-top:5px;float:left;width:100px;height:20px;background-color:#E9E2E2">' +
		          '<div style="background-color:rgb(229, 157, 109);width:'+ percent +'px;height:20px;"></div>' +
		          '</div>' + percent + '%';
			 }
			 if(groupItem.field != 'percent' && groupItem.field != 'result'){
				 percent = "<span title=\""+percent+"\" style=\"text-decoration:underline;text-align:left;cursor: pointer;\"  onclick=\" showLogDetail('"+params+"','"+ percent +"','"+columnType+"','"+headText+"',1)\">"+percent.substring(0,20)+"</span>";
					 
					 
			 }
			 table += "<td align='left'>"+ percent + "</td>";
		 });
		table += "</tr>";
		 
	 });
	   table += '</tbody></table>';
	   return table;
}
function CurentTime() {
    var now = simHandler.serverTime;
    var year = now.getFullYear();       //年
    var month = now.getMonth() + 1;     //月
    var day = now.getDate()-1;            //日
    var hh = now.getHours();            //时
    var mm = now.getMinutes();          //分
    var clock = year + "-";
    if (month < 10)
        clock += "0";
    clock += month + "-";
    if (day < 10)
        clock += "0";
    clock += day + " ";
    if (hh < 10)
        clock += "0";
    clock += hh + ":";
    if (mm < 10) clock += '0';
    clock += mm;
    return (clock);
}
function showExportTip(isHaveSourceLog){
	if(isHaveSourceLog){
		$("#curFormatLog").show();
		$("#allFormatLog").show();
		$("#curLog").show();
		$("#allLog").show();
	} else {
		$("#curFormatLog").show();
		$("#allFormatLog").show();
		$("#curLog").hide();
		$("#allLog").hide();
	}
}
function openQueryCondition(){
	$.ajax({
		 url: '/sim/logReport/getLogInterval',
         type: 'POST',
         dataType:'json',
         contentType:"text/javascript",
         success: function(intervalList){
        	 var option = "";
        	 $.each(intervalList,function(index,item){
        		 option += "<option value='"+item.value+"'>"+item.label+"</option>";
        	 });
        	 $("#interval").empty();
        	 $("#interval").append(option);
         }
	});
	clearCondition();
	$("#queryCondition").dialog('open').dialog('setTitle',"保存条件");
}
function hideUserDefindedTime(value){
	if(value == "user_define"){
		$("#starttime").show();
		$("#endtime").show();
	}else{
		$("#starttime").hide();
		$("#endtime").hide();
	}
}
function closeQueryCondition(){
	delete queryParams.interval;
	$("#queryCondition").dialog('close');
}
function saveQueryCondition(){
	var condition = {};
	
	var title = $("#title").val();
	if(title == ""){
		$("#titleText").html(" 名称不能为空");
		return false;
	}else{
		$("#titleText").html("");
		condition.title = title;
	}
	var interval = $("#interval  option:selected").val();
	condition.interval=interval;

	if(interval == "user_define"){
		var startDate = $("#startdate").val();
		var endDate = $("#enddate").val();
		if(startDate == ""){
			$("#startText").html("起始时间不能为空");
			return false;
		}else{
			$("#startText").html("");
			condition.startTime = $("#startdate").val();
		}
		if(endDate == "" ){
			$("#endText").html("结束时间不能为空");		
			return false;
		}else{
			$("#endText").html("");
			condition.endTime = $("#enddate").val();
		}
	}
	condition.conditionName = queryParams.conditionName;
	condition.deviceType = queryParams.deviceType;
	condition.group = queryParams.group;
	condition.operator = queryParams.operator;
	condition.pageNo = queryParams.pageNo;
	condition.pageSize = queryParams.pageSize;
	condition.queryContent = queryParams.queryContent;
	condition.queryEndDate = queryParams.queryEndDate;
	condition.queryStartDate = queryParams.queryStartDate;
	condition.queryType = queryParams.queryType;
	condition.host = queryParams.host;
	 $.ajax({
         url: '/sim/logReport/queryConditionList',
         type: 'POST',
         dataType:'json',
         contentType:"text/javascript",
         success: function(data){
        	 if(data != null && data.length >=10){
        		showErrorMessage("最多可以创建10条记录!");
 				return;
        	 }else{
        			 $.ajax({
        		         url: '/sim/logReport/saveQueryCondition',
        		         type: 'POST',
        		         async:false,
        		         data: JSON.stringify(condition),
        		         dataType:'json',
        		         contentType:"text/javascript",
        		         success: function(result){
        		        	 clearCondition();
        		           if(result!=null && result.status == true){
        		        	   closeQueryCondition();
        		        	   queryConditionList();
        		           }else{
        		        	   showErrorMessage("创建失败!");
        						return false;
        		           }
        		         }
        			 });
        	 }
         }
	 });
}
//获取查询条件列表
function queryConditionList(){
	 $.ajax({
         url: '/sim/logReport/queryConditionList',
         type: 'POST',
         dataType:'json',
         contentType:"text/javascript",
         success: function(result){
        	 var trHtml="";
        	 if(result != null){
	        	 $.each(result, function(index,item) {
	        		 var name = item.name.substring(0,10);
	       		     trHtml += "<tr><td><a title='"+item.name+"' style ='text-decoration:underline;padding-left:10px;cursor:pointer;color:#333;font-size:12px;'onclick=\"findQueryConditionData("+item.id+");\">"+name+"</a></td><td style='padding-left:5px;'><a class='icon-del' style='cursor:pointer;width:16px;height:16px;' title='删除' onclick='deleteQueryCondition("+item.id+")'></a><td></tr>";
	
	        	}); 
        	 }
        	 $("#t_querycondition_body").html("");
        	 $("#t_querycondition_body").append(trHtml);
         }
	 });
	
}
function findQueryConditionData(id){
	$.ajax({
		url:'/sim/logReport/findQueryCondition?id='+id,
		type:'post',
		dataType:'json',
		contentType:"text/javascript",
		success:function(result){
			//后台返回数据
	 	     var conditionNameArr= result.searchObject.conditionNames;
	 	     var fieldNames = result.searchObject.groupColumns;
	   		 var conditionOperatorsArr = result.searchObject.operators;
	   		 var conditionQueryContentsArr = result.searchObject.queryContents;
	   		 var conditionQueryTypsArr = result.searchObject.queryTypes;
	  		 var condition_deviceType = result.searchObject.type;
	   		 var condition_host = result.searchObject.host;
     		 var condition_group = result.searchObject.group;
     		 var startDate = result.searchObject.start;
     		 var endDate = result.searchObject.end;
     		 //searchStatus这个状态用来区分，点击树时，是否下发请求查询，等于true查询，等于false不用查询
     		 searchStatus = false;
     		 resetQuery();//重置查询条件
     		 //清空设备下拉框列表
     		 $("#log_source_sel").val("");
     		 var queryCondition = {};
     		 //下拉列表框赋值
             if(condition_host !=null){
            	 queryCondition.host = condition_host;
     		 }
     		 //左边，树节点选中,当资产删除，不存在时，选中默认(所有设备)，存在时，选中各自的设备树节点
             var selectNode = $('#log_query_tree').tree('find',condition_deviceType);
	    	 if(selectNode != null){
	    		var childrenNode = $('#log_query_tree').tree("getChildren",selectNode.target);
	    		$.each(childrenNode,function(index,item){
	    			if(condition_group == item.attributes.groupId){
	    				$('#log_query_tree').tree("select",item.target);
	    				return false;
	    			}
	    	
	    		});
	    	}else{
     			var rootNode = $('#log_query_tree').tree('getRoot');
	    		var children = $('#log_query_tree').tree('getChildren',rootNode.target) ;
	    		$('#log_query_tree').tree("select",children[0].target); 
	    	}
	    	
	    	//选中日志时间
	        $('#begin_time').val(formateDate(startDate));
	        $('#end_time').val(formateDate(endDate));
	       
	        if(fieldNames.length >0){
	        	 var reload = false;
	        	$.each(fieldNames, function(index,item) {
     				var conditionQueryContent = conditionQueryContentsArr[index].split(" ");
         			$.each(conditionQueryContent, function(contentIndex,contentItem){
         				if(conditionNameArr[index] == "PRIORITY"){
         					switch(contentItem){
    					    case "0":
    					    	contentItem = "非常低";
    						    break;
    					    case "1":
    					    	contentItem = "低";
    					    	break;
    					    case "2":
    					    	contentItem = "中";
    					    	break;
    					    case "3":
    					    	contentItem = "高";
    					    	break;
    					    case "4":
    					    	contentItem = "非常高";
    					    	break;
    						
    					    }
         				}
         				
             			var text =  item + " " + conditionOperatorsArr[index]  + " " + contentItem;
             			
             			var queryAttr = {
             					datatype : conditionQueryTypsArr[index],
             					operator : conditionOperatorsArr[contentIndex],
             					value : contentItem
             			};
             			if(index == fieldNames.length-1){
             				reload = true;
             				
             			}
             			createQueryElement(conditionNameArr[index], text, queryAttr,reload,condition_host);
         			});
     			});
	        }
	        else{
	        	logTableLoadDataTimer(queryCondition);
	        }
	        $(".box_main").hide("slow");
			$("#box_img").css("background", " url('/img/layout_button_left.png') no-repeat");
		}
		
	});
}
//删除查询条件数据
function deleteQueryCondition(id){
	$.ajax({
		url:'/sim/logReport/deleteQueryCondition?id='+id,
		type:'post',
		dataType:'json',
        contentType:"text/javascript",
		success:function(result){
			if(result.status){
				queryConditionList();
			}else{
				showErrorMessage("提示","删除失败!");
			}
		}
	});
}

function clearCondition(){
	  $("#title").val("");
	  $('#starttime').hide();
      $('#endtime').hide();
      $("#startdate").val("");
      $("#enddate").val("");
      $("#interval").val("");
}
function openQueryConditionList(){
	$(".box_main").show("slow");
	$("#box_img").css("background", " url('/img/skin/layout/layout_button_right.png') no-repeat");
}
function closeQueryConditionList(){
	$(".box_main").hide("slow");
	$("#box_img").css("background", " url('/img/layout_button_left.png') no-repeat");
}
function formateDate(datetime) {
	var date = new Date(datetime);
    var year = date.getFullYear();       // 年
    var month = date.getMonth() + 1;     // 月
    var day = date.getDate();            // 日
    var hh = date.getHours();            // 时
    var mm = date.getMinutes();          // 分
    var ss = date.getSeconds();
    var clock = year + "-";
    if (month < 10)
        clock += "0";
    clock += month + "-";
    if (day < 10)
        clock += "0";
    clock += day + " ";
    if (hh < 10)
        clock += "0";
    clock += hh + ":";
    if (mm < 10) clock += '0';
    clock += mm +":";
    if(ss<10)
    	 clock +="0";
    clock += ss;
    return (clock);
}

