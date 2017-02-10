/**
 * 日志查询
 */
var simLogSearchHandler = {};
$(function(){
	//日志查询条件
	var queryParams = {};
	//原始日志列表
	var rawLogList = [];
	//定时器
	var timer = null;
	//定时器间隔时间 5秒
	var interval = 5000;
	//日志查询窗口
	var log_query_dialog = null;
	
	/**
	 * 初始化日志源树
	 */
	$('#log_query_tree').tree({   
	    url:'/sim/logSearch/getTree',
	    onSelect:onSelectTreeNode,
	    onLoadSuccess:function(node, data){
	    	//加载成功后，选中根节点
	    	var root = $(this).tree('getRoot');
			$(this).tree('select',root.target);
	    }
	});  	
	
	/**
	 * 选择日志类型树节点后，折叠查询面板并初始化查询参数，最后调用定时器
	 */
	function onSelectTreeNode(node){
		collapseSearchPanel();
		queryParams = {};
		queryParams.deviceType = node.attributes.deviceType;
		queryParams.nodeId = node.attributes.nodeId;
		queryParams.host = node.attributes.host;
		queryParams.queryStartDate = moment(simHandler.serverTime).subtract('hours', 1).format('YYYY-MM-DD HH:mm:ss');
		queryParams.queryEndDate = moment(simHandler.serverTime).format('YYYY-MM-DD HH:mm:ss');
		queryParams.pageSize = 20;
		queryParams.pageNo = 1;
		logTableLoadDataTimer({});
	}
	
	/**
	 * 根据查询参数获取日志列表数据，动态构建列集和数据集
	 */
	function changeTableData(params){
		if(!params.pageSize)
			params.pageSize = 20;
		if(!params.pageNo)
			params.pageNo = 1;
        $.ajax({
            url: '/sim/logSearch/doLogSearch',
            type: 'POST',
            data: JSON.stringify(params),
            dataType:'json',
            contentType:"text/javascript",
            success: function(data){
				if(!!data){
					//后台是否已经查询结束，如果结束则清除定时器
					var isOk = data.finished;
					if(isOk){
						clearTimer(timer) ;
					}
					//定义列集、数据集，并初始化
					var cols = [], rows = [], colslength = {}; 
					if(!!data.maps){
						rows = $.map(data.maps,function(row){
							var _row = {};
							for(var key in row){
								_row[key] = row[key];
								if(!!colslength[key]){
									if(row[key].length > colslength[key])
										colslength[key] = row[key].length;
								}else{
									colslength[key] = row[key].length;
								}
							}
							return _row;
						});
					}
					
					if(!!data.logUtil){
						cols = $.map(data.logUtil,function(col){
							//计算字段宽度，最小宽度为5
							var w = (!!colslength[col.dataField]) ? colslength[col.dataField] > 5 ? colslength[col.dataField] : 5 : 5;
							return {field:col.dataField,title:col.headerText,width:w};
						});
					}					
					//初始化原始日志列表
					rawLogList = [];
					if(!!data.records){
						rawLogList = $.map(data.records,function(log){
							return log.msg;
						});
					}
					
					createLogTable(cols,rows);
					createLogTablePager(data.totalLogs, data.totalRecords, data.displayCount, data.lapTime, params.pageSize, params.pageNo, !isOk);
				}            	
            }
        });	
	}
	
	/**
	 * 日志列表加载数据定时器<br>
	 * 由于后台查询需要大量计算过程，导致查询结果响应较慢，所以需要多次查询才能将结果显示完整。
	 */
	function logTableLoadDataTimer(params){
		//如果定时器已存在则清楚它
		if(!!timer)
			clearTimer(timer) ;
		//构造查询参数
		for(var name in params){
			queryParams[name] = params[name];
		}
		//即时查询
		changeTableData(queryParams);
		//调用定时器查询
		timer = createTimer(function(){changeTableData(queryParams);},interval);
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
		var disMsg = "显示{from}到{to}, " + (displayCount >= 100000 ? "前" : "共") + "{total}条, 命中数>=" + totalRecords + "条, 日志总量" + totalLogs + "条, 耗时" + (lapTime/1000) + "秒";
		//更新分页信息
		$('#log_query_table_pager').pagination({   
		    total:displayCount,   
		    pageSize:pageSize,
		    pageNumber:pageNumber,
		    //loading:loading,
		    displayMsg:disMsg,
		    buttons:[{
				iconCls:'icon-search',
				plain:true,
				handler:onOpenSearchDialog
			}],
		    onSelectPage:function(pageNumber, pageSize){
		    	queryParams.pageSize = pageSize;
				queryParams.pageNo = pageNumber;
				logTableLoadDataTimer(queryParams);
		    },
		    onBeforeRefresh:function(pageNumber, pageSize){
		    	alert(1);
//				queryParams.pageSize = pageSize;
//				queryParams.pageNo = pageNumber;
//				logTableLoadDataTimer(queryParams);
			}
		}); 
		$('#log_query_table_pager').css('margin','0px');
	}
	
	/**
	 * 根据列集和数据集创建日志查询表格
	 * @param cols 列集
	 * @param rows 数据集合
	 */
	function createLogTable(cols,rows){
		$('#log_query_table').datagrid({   
			border : false,
			fit : true,
			nowrap : true,
			fitColumns : true,
			striped : true,
			//rownumbers : true,
			singleSelect : true,
		    columns : [cols],
		    data : rows,
		    toolbar:'#log_query_table_pager',
			onSelect : function(rowIndex, rowData){
 				if(!!rawLogList[rowIndex]){
 					$('#raw_log_content').empty().text(rawLogList[rowIndex]);
 				}
			},
			onBeforeLoad:function(param){
				$('#raw_log_content').empty();
				return true;
			}
		});  		
	}

	/**
	 * 打开查询面板
	 */
	function onOpenSearchDialog(){
		//判断是否选中了设备类型，如果没有选中则不继续执行
		var seltreenode = $('#log_query_tree').tree('getSelected');
		if(!seltreenode)return;
		//获取当前选中的节点设备类型,主机地址和节点ID
		var param = $.param({
							deviceType : seltreenode.attributes.deviceType,
							host : seltreenode.attributes.host,
							nodeId : seltreenode.attributes.nodeId
						});
		//计算当前面板宽度和高度
		var w = $('#log_query_layout').layout('panel','center').width();
		var h = $('#log_query_layout').layout('panel','center').height();
		//初始化日志查询弹出窗口并自上向下展开
		log_query_dialog = $("#log_query_dialog").dialog({
			href:'/sim/logSearch/getLogSearchFieldset?'+param,
			style:{'padding':0,'border':0},
			top:0,
			left:0,
			width:w,
			height:h,
			inline:true,
			noheader:true,
			modal:true,
			border:false,
			collapsed:true,
			onCollapse:onCollapseSearchDialog
		}).dialog('expand',true);	
	}
	
	//由于日志查询弹出窗口每次创建时是先折叠再展开，所以第一次折叠不能删除这个窗口。因此利用此变量加以限制。
	var tmp_close_count=0;
	/**
	 * 当折叠日志查询弹出窗口后，删除此窗口的HTML元素。
	 * 并删除日期范围选择框HTML元素。
	 * 如果不删除每次展开窗口都将创建新的元素。
	 */
	function onCollapseSearchDialog(){
		tmp_close_count++;
		if(tmp_close_count%2 == 0){
			$('#receipt_time').data('daterangepicker').remove();//删除日历选择插件
			log_query_dialog.dialog('close',false);
		}
			
	}
	/**
	 * 如果查询面板存在则折叠
	 */
	function collapseSearchPanel(){
		if(!!log_query_dialog)
			log_query_dialog.dialog('collapse',true);
	}
	

	//为弹出窗口绑定一个取消查询事件，当点击查询页面的取消按钮后执行此方法
	$("#log_query_dialog").bind('onCancelSearch',function(){
		collapseSearchPanel();
	});
	//为弹出窗口绑定一个提交查询事件，当点击查询页面的查询按钮后执行此方法
	$("#log_query_dialog").bind('onSubmitSearch', function(event,data) {
		logTableLoadDataTimer(data);
	});	
	
	/**
	 * 初始化页面
	 */
	function initPage(){
		createLogTable([],[]);
		createLogTablePager(0,0,0,0,0,0,false);
	}
	
	return initPage();
});