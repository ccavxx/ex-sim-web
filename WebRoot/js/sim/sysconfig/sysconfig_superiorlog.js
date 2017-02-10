/**
 * ------------------------
 * 日志查询
 * @author hou_jianyong
 * @date 2014/4/4
 * ------------------------
 */
var simsuperiorLogQueryHandler = {};
//日志查询条件
var speriorqueryParams = {};
//日志列表序号
var speriorrowNumbers = [];

//用于标识下一页按钮是否可用
var nextPre=false;
//原始日志列表
var rawLogList = [];
//定时器
//用于存储每个列标题对应过滤器的值
var column_tooltip = {};
//查询条件组背景色
var colors = ['#2f7ed8', '#8bbc21', '#1aadce', '#f28f43', '#77a1e5', '#c42525', '#a6c96a'];
//创建表格状态，用于判断表格和分页是否已经创建过
var createdTableStatus = false;
//日志源加载状态
var logDataSourceStatus=false;
//服务器时间
var servertime = simHandler.serverTime;
//查询分析传值
var conName,conOperator,conQueryContent,conQueryType;
var seq = 0 ;
var logQueryStatus=true;
//用于查询分析显示查询条件
//	simsuperiorLogQueryHandler.goSuperiorListBack = function(event){
//		simHandler.sysconfigDefaultElementId = 'initsuperiorList';
//		simHandler.changeMenu(event,'/page/sysconfig/index.html');
//	}
	/**
	 * 日志列表加载数据定时器<br>
	 * 由于后台查询需要大量计算过程，导致查询结果响应较慢，所以需要多次查询才能将结果显示完整。
	 */
	function logTableLoadDataTimer(params){
		//构造查询参数
		for(var name in params){
			speriorqueryParams[name] = params[name];
		}
		if(speriorqueryParams.deviceType){
			//即时查询
			changeTableData(speriorqueryParams);
		}
	}

	/**
	 * 根据查询参数获取日志列表数据，动态构建列集和数据集
	 * @param params 查询参数
	 */
	function changeTableData(params){
		seq++ ;
		params.seq = seq;
		if(simHandler.logQueryParam){
			params.host = simHandler.logQueryParam.deviceIp ;
			params.nodeId = simHandler.logQueryParam.nodeId ;
			simHandler.logQueryParam = null ;
		}
		if(!params.pageSize)
			params.pageSize = 20;
		if(!params.pageNo)
			params.pageNo = 1;
        $.ajax({
            url: '/sim/logRestQuery/doLogSearch?ip=' + simsuperiorLogQueryHandler.targetIp,
            type: 'POST',
            data: JSON.stringify(params),
            dataType:'json',
            contentType:"text/javascript",
            success: function(data){
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
									cols.push({field:col.dataField,title:col.headerText,width:600,formatter:function(value,row,index){
										if(value != undefined && value != null){
											if(col.dataField!="MESSAGE"){
											      return "<span title='"+value+"' style=\"text-decoration:underline;text-align:left;cursor: pointer;\"  onclick=\" showLogDetail('"+col.dataField+"','"+index+"','"+type+"','"+col.headerText+"')\">"+value+"</span>";
												}else{
													return "<span title='"+value+"'>"+value+"</span>";
												}
										}else{
											return "";
										}
									}});
								}else if(col.headerText=="原始日志"){
									cols.push({field:col.dataField,title:col.headerText,width:1200,formatter:function(value,row,index){
										if(value != undefined && value != null){
											return "<span title='"+value+"'>"+value+"</span>";
											
										}else{
											return "";
										}
										
									}});
								}else{
								   cols.push({field:col.dataField,width:140,title:col.headerText,formatter:function(value,row,index){
									var str="";
									if(value != undefined && value != null){
										str="<div style='width:50px;'><span style=\"text-decoration:underline;text-align:left;cursor: pointer;\"  onclick=\" showLogDetail('"+col.dataField+"','"+index+"','"+type+"','"+col.headerText+"')\"";			
										 if(col.headerText=="级别"){
											 	str+= " class=\""+simHandler.getPriorityClassByCN(value)+"\"></span></div>" ;
									     }else{
									    	str+=">"+value+"</span></div>";
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
								  cols.push({field:col.dataField,title:col.headerText,width:150,formatter:function(value,row,index){
									var str="";
									if(value != undefined && value != null){
										 str="<div style='width:50px;'><span ";			
										 if(col.headerText=="级别"){
											 	str+= " class=\""+simHandler.getPriorityClassByCN(value)+"\"></span></div>" ;
									     }else{
									    	str+=">"+value+"</span></div>";
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
					//初始化原始日志列表
					rawLogList = [];
					if(!!data.records){
						rawLogList = $.map(data.records,function(log){
							return log.msg;
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
					refreshLogTablePager(totalcount, data.totalRecords, data.displayCount, data.lapTime, params.pageSize, params.pageNo, !isOk);
					//如果已经创建过表格，只刷新表格数据
					if(createdTableStatus){
						refreshLogTableData(rows);
					}else{
						if(!logDataSourceStatus){
							createLogSourcesList(logSourcesList);
						}
						createLogTable(cols,rows,fieldFilters);
					}
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
						 $('#log_query_table_pager').pagination('loaded');
						 $("#log_query_table").datagrid("loaded");
					}
					logQueryStatus=isOk;
				}            	
            }
        });	
	}	
	
  	function rowNumber(value,row,index){
  		var str=speriorrowNumbers[index];
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
			var opt = $("<option/>").val(nodeId).attr('ip', ip).attr('title', ip).text(!!name ? name : ip);
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
	function refreshLogTablePager(totalLogs, totalRecords, displayCount, lapTime, pageSize, pageNumber, loading){
		var logcount=totalRecords<100000 ? "=" : ">=";
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
	function createLogTablePager(totalLogs,totalRecords,displayCount,lapTime, pageSize, pageNumber, loading){
		var logcount=totalRecords<100000?"=":">=";
		var disMsg = "显示<font color='red'>{from}</font>到<font color='red'>{to}</font>,前<font color='red'>{total}</font>条, 命中数"+logcount+"<font color='red'>" + totalRecords + "</font>条, 日志总量<font color='red'>" + totalLogs + "</font>条, 耗时<font color='red'>" + (lapTime/1000) + "</font>秒";
		//更新分页信息
		$('#log_query_table_pager').pagination({   
		    total:displayCount,   
		    pageSize:pageSize,
		    pageNumber:pageNumber,
		    loading:loading,
		    displayMsg:disMsg,
		    onSelectPage:function(pageNumber, pageSize){
		    	speriorqueryParams.pageSize = pageSize;
				speriorqueryParams.pageNo = pageNumber;
				rowNumberData(pageNumber, pageSize);
				logTableLoadDataTimer(speriorqueryParams);
				$('#log_query_table').datagrid('loading');
				$('#log_query_table_pager').pagination('loading');
				
		    }
		}); 
	}
	function rowNumberData(pageNumber,pageSize){
		var start = (pageNumber-1)*parseInt(pageSize);
		var end = start + parseInt(pageSize);
		speriorrowNumbers=[];
		for(var i=start;i<end;i++){
			speriorrowNumbers.push(i+1);
		}
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

				$('#raw_log_tooltip_container').empty();
				$('.raw-log').qtip({
					content : {
						text:false
					},
					style : 'cream',//样式cream, light, dark, green and red.
					show : {
						solo : true // Only show one tooltip at a time
					},
					hide : 'unfocus',
					position : {
						target : 'mouse',//跟随鼠标显示提示信息
						adjust : { screen: true },//根据屏幕调整显示位置
						container: $('#raw_log_tooltip_container')//提示信息的HTML元素加入文档中的位置
					}
				});
				if(data){
					$("#raw_log_content").html(rawLogList[0]);
					//$('#log_query_table').datagrid('loaded');
				}
				

			},
			onSelect:function(index,indexdata){
				$("#raw_log_content").html(rawLogList[index]);
			}
		});  
		//日志列表所有标题DOM
		var titles = $('.datagrid-view2 .datagrid-htable .datagrid-header-row:first>td');
		//遍历所有标题，如果标题字段field出现在过滤字段中则在其标题文本后方加上过滤图标
		$.each(titles, function(i, td) {
			var span = $(td).find('span:first');
			var field = $(td).attr('field');
			//var title = $(span).text();
			if(!!filters[field]){
				$(span).append("<a class='log-filter' style='cursor: pointer;' field = '"+field+"'>" + "<i class='icon-filter'></i></a>");
			}
		});

		//为新增加的过滤图标增加qtip提示框，用于显示查询过滤器
		$('a.log-filter').each(function(){
			var txt = $(this).parent().text();
			var field = $(this).attr('field');
			var html = filters[field];
			
			$(this).click(function(){
				$('.query-expression-group').each(function(i){
					if(field ==this.id){
					   $("select[name="+field+"_operator]").attr('disabled','disabled');
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
	function becomeReadOnly(id,flag){
		$("select[name='"+id + "_operator']").attr("disabled",flag);
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
		var values = filter.values;
		
		
		var divEle = $("<div class='input-append'></div>");
		
		//运算符节点，下拉框
		var operEle = $("<select class='input-small' name='"+field+"_operator'></select>");
		
		//运算符节点，下拉框
		for(var i in operators){
			var option = $('<option></option>').attr('value',operators[i].value).text(operators[i].text);	
			operEle.append(option);
		}
		
		
		//值节点，如果是数组则创建下拉框，否则创建文本框
		var valEle = "";
		if(values){
			if(field=="FILE_NAME"){
				//文本框
				valEle = $("<input type='text' class='input-small'/>").attr('name',field);
			}else{
				//下拉框
				valEle = $("<select class='input-small'></select>").attr('name',field);
				for(var i in values){
					var option = $('<option></option>').attr('value',values[i].value).text(values[i].text);
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
			var operator = $("select[name='"+field+"_operator'] option:selected").val();
			//值-value
			var fieldValue = "";
			//值-text(显示)
			var fieldText = "";
			var text="";
			if(values){
				if(field=="FILE_NAME"){
					fieldText = $("input[name='"+field+"']").val();
				    fieldValue = fieldText;
				}else{
				    fieldText = $("select[name='"+field+"'] option:selected").text();
				    fieldValue = $("select[name='"+field+"'] option:selected").val();
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
	function createQueryElement(field, text, queryAttr){
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
				refreshLogTable('','');
		}
	}
		
	/**
	 * 刷新日志列表
	 * @param filterField 过滤掉的字段(条件组)
	 * @param filterUuid 过滤掉的条件uuid(单个条件)
	 */
	function refreshLogTable(filterField,filterUuid){
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
		conName = fieldNames;
		conOperator = fieldOperators;
		conQueryContent = fieldValues;
		conQueryType = fieldTypes;
		//执行日志查询定时器
		logTableLoadDataTimer(searchParams);
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
		$('#log_query_table_layout').layout('panel', 'north').panel('resize',{height:68});
		$('#log_query_table_layout').layout('resize');
		
		var isLeaf = $('#log_query_tree').tree('isLeaf',node.target);
		if(!isLeaf){
			return;
		}
		else{
			if(oldNode!=""){
				if(node.attributes.deviceType==oldNode){//设备类型相等，日志源不需要重新加载
					logDataSourceStatus=true;
				}else{//需要重新加载日志源
					logDataSourceStatus=false;
				}
			}
			 oldNode=node.attributes.deviceType;
		}
		$("#raw_log_content").html("");
		
		//重置页面所DOM元素及临时变量		
		 resetQuery();
		 if(node.attributes.deviceType == "Esm/Topsec/SystemRunLog" || node.attributes.deviceType == "Esm/Topsec/SystemLog"){
			 $("#super_log_searchDiv").layout('remove','south');
			 
		 }else{
			 $("#super_log_searchDiv").layout('add',{region:'south',height:100,content:'<span class="label label-info">原始日志:</span><p class="muted alert" id="raw_log_content" style="height:55px;overflow:auto;"></p>'});
		 }
		speriorqueryParams = {};
		var host=$("#log_source_sel").find(':selected').attr('ip');
		if(host){
			
			if(logDataSourceStatus){
				queryParams.host = host;
			}else{
				queryParams.host = "";
			}
			queryParams.nodeId = $("#log_source_sel").val();
		}
		speriorqueryParams.deviceType = node.attributes.deviceType;
		speriorqueryParams.group = node.attributes.groupId;
		speriorqueryParams.queryStartDate = $('#begin_time').val();
		speriorqueryParams.queryEndDate = $('#end_time').val();
		speriorqueryParams.pageSize = 20;
		speriorqueryParams.pageNo = 1;
	    var fieldNames="",fieldOperators="",fieldValues="",fieldTypes="";
        if(node.attributes.filter){
			var filtercondition=node.attributes.filter;
			var filtersplit=filtercondition.split("AND");
			for(var i=0;i<filtersplit.length;i++){
				var childrenFilter=$.trim(filtersplit[i]).split("=");
				 if(childrenFilter[0].indexOf("!") >= 0){
					 fieldNames+=childrenFilter[0].substring(0,childrenFilter[0].length-1);
					 fieldOperators+="不等于";
				}else{
					fieldNames+=childrenFilter[0];
				     fieldOperators+="等于";
				}
				 fieldValues+=childrenFilter[1].substring(1,childrenFilter[1].length-1);
				 fieldTypes+="String";
					 if(i<filtersplit.length-1){
						 fieldNames+=",";
						 fieldOperators+=",";
						 fieldValues+=",";
						 fieldTypes+=",";
					 }
			}
	
				conName=fieldNames;
				conOperator=fieldOperators;
				conQueryContent=fieldValues;
				conQueryType=fieldTypes;
        	}else{
			  conName = conOperator = conQueryContent = conQueryType = "";
		    }
		
        speriorqueryParams.conditionName = fieldNames;
		speriorqueryParams.operator = fieldOperators;
		speriorqueryParams.queryContent = fieldValues;
		speriorqueryParams.queryType = fieldTypes;
		//调用日志查询定时器
		logTableLoadDataTimer(speriorqueryParams);
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
  	//日期范围选择器初始化参数
	var optionSet = {
			startDate: moment(servertime).subtract('hours', 1),
		    endDate: moment(servertime),
		    maxDate: false,
		    minDate: moment(servertime).subtract('year',10),
		    showDropdowns:true,
		    timePicker: true,//显示时间选择器
		    timePickerIncrement: 10,//时间间隔5分钟
		    timePicker12Hour: false,//12小时制
		    locale: locales['zh-CN'],
		    format: 'YYYY-MM-DD HH:mm:ss',
		    opens: 'left',
		    ranges: {
		       '最近1小时': [moment(servertime).subtract('hours',1), moment(servertime)],
		       '最近6小时': [moment(servertime).subtract('hours',6), moment(servertime)],
		       '今天': [moment(servertime).startOf('day'), moment(servertime)],
		       '昨天': [moment(servertime).subtract('days', 1).startOf('day'), moment(servertime).subtract('days', 1).endOf('day')]
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
	var savetime=[];//用于保存时间轴时间点
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
	function superiorlog_backSearch(){
		var str=savetime[savetime.length-1];
		savetime.pop();
		var dateArr=str.split("&");
		var startdate=dateArr[0];
		var enddate=dateArr[1];
		$("#begin_time").val(startdate);
		$("#end_time").val(enddate);
		var searchParams = {};
		searchParams.pageNo = 1;
		searchParams.queryStartDate = startdate;
		searchParams.queryEndDate = enddate;
		logTableLoadDataTimer(searchParams);
		if(savetime.length==0){
			if($('#log_timeline_chart').highcharts().resetZoomButton){
				$('#log_timeline_chart').highcharts().resetZoomButton.hide();
			}
			$("#superiorlog_div").hide();
		}
	}
	function refreshLogTimelineChartData(data){
		//var timelineChart = $('#log_timeline_chart').highcharts();
		if(data!=null && data!=""){
			var seriesData=[];
			var category=[];
			for(var i=0;i<data.length;i++){
				seriesData[i]=data[i].y;
				category[i]=data[i].x;
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
		    url:'/sim/logRestQuery/getTreeForGroup?ip=' + simsuperiorLogQueryHandler.targetIp,
		    onSelect:onSelectTreeNode,
		    onBeforeSelect:onBeforeSelect,
		    onLoadSuccess:function(node, data){
		    	var log_cat_tree = $("#log_query_tree") ;
		    	//获取根节点
		    	var children ;  
		    	if(simHandler.logQueryParam){
		    		var selectNode = log_cat_tree.tree("find",simHandler.logQueryParam.securityObjectType) ;
		    		children = log_cat_tree.tree("getChildren",selectNode.target) ;
		    	}else{
		    		var rootNode = log_cat_tree.tree('getRoot');
		    		 children = log_cat_tree.tree('getChildren',rootNode.target) ;
		    	}
		    	/* 选中第一个子节点 */  
		    	//根据根节点获取子节点
		    	log_cat_tree.tree("select",children[0].target); 
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
			searchParams.host = $(this).find(':selected').attr('ip');
			searchParams.nodeId = $(this).val();
			logTableLoadDataTimer(searchParams);
		});
		

		//创建日志数据列表
		//createLogTable([],[],[]);
		//创建分页菜单
		createLogTablePager(0,0,0,0,20,0,false);
		
		createLogTimelineChartInstance();
		
	}
//根据点击事件查询相关日志信息
function showLogDetail(fieldname,conditionIndex,type,headText){
	var rows = $("#log_query_table").datagrid("getRows");
	var condition = rows[conditionIndex][fieldname];
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
    
    if(operator==""){
    	operator="等于";
    }
    $("select[name='"+fieldname+"_operator']").val(operator);
    if(operator=="between"){
		condition="从"+condition+"至"+condition;
		text=headText+" "+condition;
	}else{
		text=headText+" "+operator+" "+condition;
	}
		queryArr={
				datatype : type,
				operator : operator,
				value : condition
		};
	createQueryElement(fieldname,text,queryArr);
}
//初始化加载数据
(function(){
	var targetId = simHandler.superioripId;
	simsuperiorLogQueryHandler.targetIp = targetId.replace(/[_]/g, ".");
	$("#sysconfigsuperiorlogPanelId").panel({
		headerCls:'sim-panel-header',
		fit:true,
		title:"子节点日志查询（" + simsuperiorLogQueryHandler.targetIp + "）",
		tools:[{
					iconCls:'icon-cancel',
					text:'返回',
					handler:function(event){
						simHandler.goSuperiorListBack(event);
					}
				}]
	});
	//初始化日志接收开始时间和结束时间
	$('#begin_time').val(moment(servertime).subtract('hours',1).format('YYYY-MM-DD HH:mm:ss'));
	$('#end_time').val(moment(servertime).format('YYYY-MM-DD HH:mm:ss'));
	initPage();
})();


