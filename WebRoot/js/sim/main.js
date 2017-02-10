//@ sourceURL=main.js
var simMainHandler = {};

(function() {
	simHandler.indexEventQuery={};
	var realTimeAlarmTimerId, realTimeEventTimerId;
	var startTime = null;
	var endTime = null;
	// 加载告警或事件级别统计图表数据
	var loadAlarmOrEventLevelChartData = function(chart, url, unit, filter) {
		var options = chart.getOption();
		var date = new Date(simHandler.serverTime.getTime());
		endTime = date.Format("yyyy-MM-dd HH:mm:ss");
		if (unit == 'hour') {
			date.setHours(date.getHours() - 1);
		}

		if (unit == 'day') {
			date.setHours(date.getHours() - 24);
		}

		if (unit == 'week') {
			date.setHours(date.getHours() - 24 * 7);
		}

		if (unit == 'month') {
			date.setMonth(date.getMonth() - 1);
		}

		startTime = date.Format("yyyy-MM-dd HH:mm:ss");
		
		$.getJSON(url, {
			"startTime" : startTime,
			"endTime" : endTime,
			_time : new Date().getTime()
		}, function(data) {
			if (data) {
				for ( var node in data) {
					data[node].startTime = startTime;
					data[node].endTime = endTime;
				}
				var serie= {
					type:'pie', 
					itemStyle : {
		                normal : {
		                    label : {
		                        show : false
		                    },
		                    labelLine : {
		                        show : false
		                    }
		                }
		            },
		            data:data
			    };
				options.series.push(serie);
				chart.setOption(options, true);
			}
		});
	};

	var _filter = function() {// 数据过滤
		var _sdata = [];
		if (arguments.length == 3) {
			var _data = arguments[0], stime = arguments[1], etime = arguments[2];
			for ( var i in _data) {
				_sdata.push({
					"name" : _data[i]['p_label'],
					"y" : _data[i]['opCount'],
					"level" : _data[i]['priority'],
					"color" : simHandler.getPriorityLevelColorByIndex(
							_data[i]['priority'], true),
					"stime" : stime,
					"etime" : etime
				});
			}
		}
		return _sdata;
	};

	// 绘制主视图右侧事件饼图
	function drawEventLevelPieChart(step) {
		var w = $('#main_event_level_pie_chart').parent().width();
		var h = $('#main_event_level_pie_chart').parent().height();
		require(['echarts', 'echarts/chart/pie'], function(echarts) {
			var eventLevelChart = new EchartsModel().buildChart(echarts, '#main_event_level_pie_chart', function(){
				return option = {
						tooltip : {
							trigger: 'item'
						},
						series : []
				};
			});
			loadAlarmOrEventLevelChartData(eventLevelChart,
					'/sim/event/levelStatisticByTime', step?step:'day', _filter); 
			eventLevelChart.on('click', function(param) {
				if(system.hasOperatorRole){
					simHandler.indexEventQuery.priority=param.name;
					simHandler.indexEventQuery.startTime=param.data.startTime;
					simHandler.indexEventQuery.endTime=param.data.endTime;
					simHandler.onClickMenuTp('menu_evtquery','/page/event/eventQuery.html');
				}
			});
		});
	}
	// 绘制主视图左侧事件饼图
	function drawEventNamePieChart(unit) {
		require(['echarts', 'echarts/chart/pie'], function(echarts) {
			var eventNameChart = new EchartsModel().buildChart(echarts, '#main_event_name_pie_chart', function(){
				return option = {
						tooltip : {
							trigger: 'item'
						},
						noDataLoadingOption: {
				            text: '暂无数据',
				            effect: 'bubble',
				            effectOption: {
				                effect: {
				                    n: 0
				                }
				            }
						},
						series : []
				};
			});
			loadAlarmOrEventLevelChartData(eventNameChart,
					'/sim/event/nameStatistic', unit?unit:'day', formatEventNameStatisticData);
			eventNameChart.on('click', function(param) {
				if(system.hasOperatorRole){
					var date=new Date(simHandler.serverTime.getTime());
			    	var starttime,endtime=date.Format("yyyy-MM-dd HH:mm:ss");
			    	var scope = unit? unit: 'day';
			    	if(scope=='day'){
			    		date.setTime(date.getTime()-24*60*60*1000);
			    	}
			    	if(scope=='week'){
			    		date.setTime(date.getTime()-7*24*60*60*1000);
			    	}
			    	if(scope=='month'){
			    		date.setTime(date.getTime()-30*24*60*60*1000);
			    	}
			    	starttime=date.Format("yyyy-MM-dd HH:mm:ss");
					simHandler.indexEventQuery.name=param.name;
					simHandler.indexEventQuery.startTime=starttime;
					simHandler.indexEventQuery.endTime=endtime;
					simHandler.onClickMenuTp('menu_evtquery','/page/event/eventQuery.html');
				}
			});
		});
	}
	function formatEventNameStatisticData() {
		var _sdata = [];
		if (arguments.length == 3) {
			var _data = arguments[0], stime = arguments[1], etime = arguments[2];
			for ( var i in _data) {
				_sdata.push({
					"name" : _data[i].name,
					"y" : _data[i].opCount,
					"color" : simHandler.colors[i % simHandler.colors.length]
				});
			}
		}
		return _sdata;
	}
	function formatterEventAndAlarmDes(value, row, index) {
		var res = "";
		if (value != null) {
			var length = 100;
			var extendStr = "...";
			if (value.length <= 100) {
				length = value.length;
				extendStr = "";
			}
			res = "<span title='" + value.substring(0, length) + extendStr
					+ "'>" + value + "</span>";
		}
		return res;
	}
	function formatDroggleNode(value, row, index) {
		var res;
		if (row.isdevice && row.hasOperatorRole) {
			res = "<span mainDeviceTableid='"
					+ row.id
					+ "' class='table_column_link' onclick='simMainHandler.showAssetDetail(\""
					+ row.id + "\"," + row.enabled + ")'>" + value + "</span>";
		} else {
			res = "<span mainDeviceTableid='" + row.id + "'>" + value
					+ "</span>";
		}
		return res;
	}

	function mainDeviceTableLoadSuccess() {
		$("span[mainDeviceTableid]").parent().parent().mouseenter(
				function() {
					var id = $(this).find("span[mainDeviceTableid]").attr(
							"mainDeviceTableid");
					treegridDroggleNode(id);
				});
		var tree_data = $("#main_device_table").treegrid("getData");
		if(tree_data){
			var parentCount = tree_data.length;
			var childCount = 0;
			for(var i =0;i<tree_data.length;i++){
				childCount += $("#main_device_table").treegrid("getChildren", tree_data[i].id).length;
			}
			if(parentCount <5 && childCount<10){
				$("#main_device_table").treegrid("expandAll");
			}
		}
	}

	function treegridDroggleNode(id) {
		var node = $("#main_device_table").treegrid("find", id);
		if (!node)
			return;
		var state = node.state;
		var childCount = $("#main_device_table").treegrid("getChildren", id).length;
		if (state == "closed" && childCount > 0) {
			$("#main_device_table").treegrid("collapseAll");
			$("#main_device_table").treegrid("expand", id);
		}
	}
	function deviceEventFormatter(value, row, index) {
		if (row.ip) {
			return "<span class='label label-normal hand' title='今日事件数量 " + value
					+ "' " + "onclick=\"simMainHandler.showAssetEvent('"
					+ row.id + "')\">" + value + "</span>";
		} else {
			return "<span class='label label-normal' title='今日事件数量 " + value
					+ "'>" + value + "</span>";
		}
	}
	function formatterMainDeviceTableIp(value, row, index) {
		if (row.isdevice) {
			return '<span>' + value + '</span>';
		} else {
			return "<span class='label label-success' title='在线资产数'>"
					+ row.onlineCount
					+ "</span><span style='display:inline-block;width:5px;'></span><span class='label label-important' title='离线资产数'>"
					+ row.offlineCount + "</span>";
		}
	}

	function showAssetDetail(id, enabled) {
		if (enabled == 0) {
			showAlertMessage("资产已被禁用，无法查看详细信息！");
			return;
		}
		simHandler.assetId = id;
		simHandler.onClickMenuTp("menu_asset", "/page/asset/asset.html");
	}

	function showAssetEvent(ip) {
		openDialog('资产事件',800,366,'/page/main/asset_event.jsp?ip=' + ip);
	}
	
	function showAssetStatus(ip) {
		
	}
	/**
	 * 拓扑导航菜单触发方法 by horizon
	 */
	function showAssetDetailTp(id) {
		simHandler.assetId = id;
		simHandler.onClickMenuTp("menu_asset", "/page/asset/asset.html");
	}

	/**
	 * 切换最新事件TopN数据
	 */
	function changeEventTableTopN(n) {
		$('#main_event_panel').panel('setTitle', '今日最新事件Top' + n);
		$('#main_event_table').datagrid('reload', {
			limit : n
		});
	}

	/**
	 * 切换最新告警TopN数据
	 */
	function changeAlarmTableTopN(n) {
		$('#main_alarm_panel').panel('setTitle', '最新告警Top' + n);
		$('#main_alarm_table').datagrid('reload', {
			limit : n
		});
	}

	/**
	 * 刷新告警实时列表
	 */
	function refreshAlarmRealTimeTable() {
		$('#main_real_time_alarm_table').datagrid('reload');
	}

	/**
	 * 刷新事件实时列表
	 */
	function refreshEventRealTimeTable() {
		$('#main_real_time_event_table').datagrid('reload');
	}

	function changeEventLevelBarChart(unit) {
		var title = '事件级别统计-';
		switch (unit) {
		case 'hour':
			title += '最近1小时';
			break;
		case 'day':
			title += '最近1天';
			break;
		case 'week':
			title += '最近1周';
			break;
		case 'month':
			title += '最近1月';
			break;
		default:
			break;
		}
		$('#main_event_level_panel').panel('setTitle', title);
		drawEventLevelPieChart(unit);
	}
	function changeEventNameBarChart(unit) {
		var title = '事件名称统计-';
		switch (unit) {
		case 'hour':
			title += '最近1小时';
			break;
		case 'day':
			title += '最近1天';
			break;
		case 'week':
			title += '最近1周';
			break;
		case 'month':
			title += '最近1月';
			break;
		default:
			break;
		}
		$('#main_event_name_panel').panel('setTitle', title);
		drawEventNamePieChart(unit);
	}
	function changeEventRiverChart(unit) {
		var title = '事件名称统计-';
		switch (unit) {
		case 'day':
			title += '最近1天';
			break;
		case 'week':
			title += '最近1周';
			break;
		case 'month':
			title += '最近1月';
			break;
		default:
			break;
		}
		$('#main_event_river_panel').panel('setTitle', title);
		drawEventRiverChart(unit);
	}
	function changeEventChordChart(unit) {
		/*var title = '主机访问关系图-';
		switch (unit) {
		case '100':
			title += '最近100';
			break;
		case '500':
			title += '最近500';
			break;
		case '1000':
			title += '最近1000';
			break;
		default:
			break;
		}
		$('#main_event_chord_panel').panel('setTitle', title);
		*/
		drawEventChordChart(unit);
	}
	
	function expandAssetRow(row) {
		$("#main_device_table").treegrid("expand", row.id);
	}
	function showEventDetail(rowIndex, rowData) {
		simHandler.showEventDetail(rowData["EVENT_ID"], rowData["NAME"]);
	}
	function eventOperator(value,row,index){
		var id = row.EVENT_ID;
		var name = row.NAME;
		var html ="";
		if(system.hasOperatorRole){
			//html += "<a href='#' class='icon-enabled' style='margin-right:10px;' title='关闭事件' onclick=\"simMainHandler.eventConfirm('"+id+"');\"></a>";
			//html += "<a href='#' class='icon-alarm icon16 hand' title='关联告警' onclick=\"simMainHandler.openAlarmDialog('"+name+"');\"><a>";
			html += "<i title='屏蔽此事件(事件名称、设备地址、源地址、目的地址、描述都相同的事件将不再产生)' onclick='simHandler.addEventLimiter(this,"+index+",60,0)' class='icon-deny hand'/>";
			html += "<span style='padding-left:5px;'></span>" + "<a href='#' class='icon-alarm icon16 hand' title='关联告警方式' onclick=\"simMainHandler.openAlarmDialog('"+name+"','main_event_table');\"><a>"
		}
		return html;
	}
	function eventOperatorByAsset(value,row,index){
		var id = row.EVENT_ID;
		var name = row.NAME;
		var html ="";
		if(system.hasOperatorRole){
			html += "<i title='屏蔽此事件(事件名称、设备地址、源地址、目的地址、描述都相同的事件将不再产生)' onclick='simHandler.addEventLimiter(this,"+index+",60,0)' class='icon-deny hand'/>";
			//html += "<span style='padding-left:5px;'></span>" + "<a href='#' class='icon-alarm icon16 hand' title='关联告警方式' onclick=\"simMainHandler.openAlarmDialog('"+name+"','top_event_table');\"><a>"
		}
		return html;
	}
	function eventConfirm(id){
		$.messager.confirm("确认", "你确定要关闭选中事件吗？", function(r){
			if(!r) return;
			$.getJSON('/sim/eventQuery/eventConfirm?ids=' + id + '&state=1', {_time:new Date().getTime()}, function(result){
	    		$("#main_event_table").datagrid("reload");
	    		if(!result.status){
	    			showErrorMessage(result.message);
	    		}
	    	});
		});
	}
	function openAlarmDialog(name, fromTableId){
		simMainHandler.fromTableId= fromTableId;
		$("#alarm_dialog").dialog ({
			title:'告警方式列表',
			href:'/page/main/alarmCorrelationl.html',
			top : 150,
			width:600,
			height:400,
			inline:true,
			modal:true,
			shadow:false,
			border:false,
			buttons:[{
				text:'确定',
				handler:function(){
					 var checkRows = $("#alarm_datagrid").datagrid("getChecked");
					 var alarmIds = "";
					 $.each(checkRows,function(index,item){
						 alarmIds += item.id;
						 if(index < checkRows.length -1 ){
							 alarmIds += ";";
						 }
					 });
					 //根据rule名称查询ruleId
					 $.getJSON('/sim/eventQuery/queryEventAlarm?name=' + name, {_time:new Date().getTime()}, function(result){
						 if(!result.ruleId){
							 showAlertMessage("关联告警失败！");
						 }
						 var ruleId = result.ruleId;
						 $.ajax({
					    	 url:'/sim/sysconfig/event/addAlarmCorrelation',
					    	 data:{alarmIds:alarmIds,ruleId:ruleId},
					    	 dataType:'json',
					    	 success:function(data){
					    		 if(data.success){
					    			 $("#alarm_dialog").dialog('close');
					    		 }else{
					    			 showAlertMessage("关联告警失败！");
					    			 return false;
					    		 }
					    	 }
					     });
			    	 });
				    
				 }
			},{
				text:'取消',
				handler:function(){
					$("#alarm_dialog").dialog('close');
				}
			}]
		});
	}
	function onLoadSuccess (data){
		var node = $("#"+simMainHandler.fromTableId).datagrid("getSelected");
		if (node){
			var name = node.NAME;
			$.getJSON('/sim/eventQuery/queryEventAlarm?name=' + name, {_time:new Date().getTime()}, function(result){
				if(data.rows.length > 0){
					$.each(result.alarm, function(index, item){
						$.each(data.rows,function(index,row){
							if(row.id == item.id){
								$("#alarm_datagrid").datagrid("checkRow",index);
								return false;//停止循环
							}
						});
					});
				}
			});
		}
	}
	var riverModel = new EchartsModel();
    function drawEventRiverChart(unit){
		require.config({
			paths: {
				echarts: '/js/echart/build/dist'
			}
		});    	
    	require(['echarts', 'echarts/chart/eventRiver'], function(echarts) {
    		var eventRiverChart = echarts.init(document.getElementById('main_event_river')); 
			var option = {
				tooltip : {
					trigger: 'item',
					formatter:function(params){
						var temp = params.series.data[0].evolution;
						var title = temp[0].time+'至'+ temp[temp.length -1].time + '<br/>';
						title += params.series.name+'事件共';
						title +=params.series.data[0].count + '条';
						return title;
					},
					position : function(params) {
						return riverModel.setTooltipPosition('#main_event_river', params, 280, 0);
			        }
				},
				 xAxis: [{
		             type: 'time',
		             boundaryGap: [0.05,0.1]
			     }],
			     grid:{
			    	x:30,
			    	y:30,
			    	x2:30,
			    	y2:80
			    },
			    legend: {
			    	x : 'center',
			    	y : 'bottom',
				    data:[]
			    },
				noDataLoadingOption: {
		            text: '暂无数据',
		            effect: 'bubble',
		            effectOption: {
		                effect: {
		                    n: 0
		                }
		            }
				},
				series : []
			};
			
			eventRiverChart.setOption(option);
			fillEventRiver(eventRiverChart, unit);
    		eventRiverChart.on('click', function(param) {
				if(system.hasOperatorRole){
					var date=new Date(simHandler.serverTime.getTime());
			    	var starttime,endtime=date.Format("yyyy-MM-dd HH:mm:ss");
			    	var scope = unit? unit: 'day';
			    	if(scope=='day'){
			    		date.setTime(date.getTime()-24*60*60*1000);
			    	}
			    	if(scope=='week'){
			    		date.setTime(date.getTime()-7*24*60*60*1000);
			    	}
			    	if(scope=='month'){
			    		date.setTime(date.getTime()-30*24*60*60*1000);
			    	}
			    	starttime=date.Format("yyyy-MM-dd HH:mm:ss");
					simHandler.indexEventQuery.name=param.name;
					simHandler.indexEventQuery.startTime=starttime;
					simHandler.indexEventQuery.endTime=endtime;
					simHandler.onClickMenuTp('menu_evtquery','/page/event/eventQuery.html');
				}
    		});
    		eventRiverChart.on('hover', function(param) {
    			if (param.type == 'hover' && param.event.target) {
    				param.event.target.style.cursor='pointer'; 
    			}
    		});
    		eventRiverChart.on('mouseout', function(param) {
    			if (param.type == 'mouseout' && param.event.target) {
    				param.event.target.style.cursor='default'; 
    			}
    		});
    	});
    }
    
    function fillEventRiver(chart, scope) {
    	var option = chart.getOption();
    	var date=new Date(simHandler.serverTime.getTime());
    	var starttime,endtime=date.Format("yyyy-MM-dd HH:mm:ss");
    	var scope = scope? scope: 'day';
    	if(scope=='day'){
    		date.setTime(date.getTime()-24*60*60*1000);
    	}
    	if(scope=='week'){
    		date.setTime(date.getTime()-7*24*60*60*1000);
    	}
    	if(scope=='month'){
    		date.setTime(date.getTime()-30*24*60*60*1000);
    	}
    	starttime=date.Format("yyyy-MM-dd HH:mm:ss");
//    	var date = new Date(simHandler.serverTime.getTime()),timestamp = date.Format("yyyyMMdd");
//    	this.condition.time=timestamp;
    	$.ajax({
    		type : "post",
    		url : "/sim/event/eventRiverDataByTime",
    		data: {"startTime":starttime,"endTime":endtime, "scope":scope},
    		dataType:"json",
    	    async : true,
    		success : function(data) {
    			$.each(data, function(m, node) {
					if(node.length > 1) {
						option.legend.data.push(m);
						var sum = 0;
						var temp = {
								name : m,
								type : 'eventRiver',
								weight : node[0].weight,
								priority : node[0].priority,
								itemStyle : {
									normal: {
										label: {
											textStyle : {
												color: '#000'
											}
										}
									}
								},
								data : [{
									name: m, 
									count :0,
									weight: node[0].weight, 
									evolution: []
								}]
						};
						$.each(node, function(m, element) {
							temp.data[0].evolution.push({
								time: element.time,
								value:element.value, 
								detail: {
									text: element.detail, 
								}
							});
							sum += element.value;
						});
						temp.data[0].count = sum;
						option.series.push(temp);
					}
				});
    			chart.setOption(option, true);
    		}
    	});
    };
    function drawEventChordChart(unit){
		require.config({
			paths: {
				echarts: '/js/echart/build/dist'
			}
		});    	
    	require(['echarts', 'echarts/chart/force'], function(echarts) {
			var chordOption = {
					 tooltip : {
				        trigger: 'item',
				        formatter : function(params,ticket,callback){
				        	var itemData = params.data ;
				        	if(params.indicator){
				        		var sourceNode = params.series.nodes[itemData.source] ;
				        		var targetNode = params.series.nodes[itemData.target] ;
				        		return sourceNode.text + "->" + targetNode.text + "(" + itemData.count + ")" ;  
				        	}else{
				        		var text = itemData.text ;
				        		if(itemData.value){
				        			text += "("+itemData.value+")";
				        		}
				        		return text ;
				        	}
				        }
				    },
				    toolbox: {
				        show : false
				    },
				    legend : {
				        data : [],
				        orient : 'vertical',
				        x : 'left'
				    },
					toolbox: {
				        show : true,
				        orient: 'vertical',
				        x: 'right',
				        y: 'bottom',
					    feature : {
						    dataView : {
			        	        show: true, 
			        	        readOnly: true,
			        	        lang : ["<label style='text-align:center;' id='force_title'><b>主机访问关系列表</b></label>","返回"],
			 				    optionToContent: function(opt) {
			 				    	$("#force_title").html("<b>主机访问关系列表</b>");
			 				        var table = '<table style="text-align:center;width:80%;border-bottom:1px dotted #ccc;margin:15px 10px 15px 60px;">';
			 				    	   table += '<tr style="border-bottom:1px dotted #ccc;">';
			 				    	   table += "<td align='left'><b>源地址</b></td>";
			 				    	   table += "<td align='left'><b>目的地址</b></td>";
			 				    	   table += "<td align='left'><b>次数</b></td></th></tr>";
			 				    	   var nodes = opt.series[0].nodes;
			 				    	   var links = opt.series[0].links;
				    	  				var count = 0;
				    	  				var target = "";
				    	  				var source = "";
				    	  				$.each(links,function(index,linkItem){
				    	  						source = nodes[linkItem.source].text;
				    	  						target = nodes[linkItem.target].text;
				    	  						count = linkItem.count;
				    	  						if(count >0){
						    	  					table += "<tr style='border-bottom:1px dotted  #ccc;line-height:30px;'>";
						    	  					table += "<td align='left'>"+ source + "</td>";
						    	  					table += "<td align='left'>"+ target + "</td>";
						    	  					table += "<td align='left'>"+ count + "</td></tr>";
						    	  				}
				    	  				});
					    	  				
			 				    	table += '</table>';
			 				    	return  table;
			 					 }
			        	        }
					    	,
				            fullscreen : {
				                show : true,
				                title : '全屏显示',
				                icon : 'image://../../img/full_screen.png',
				                onclick : function (params){
				                	var _width = $(window).width();
				                	var _height = document.body.offsetHeight; 
				                	var node = $('<div id="dialog_main_event_chord_panel" class="easyui-panel" style="overflow: auto;"></div>');
				                	node.append($('<div id="dialog_main_event_chord" style="height:100%;"></div>'));
				                	$(node).dialog({
				                		title : '主机访问关系图',
				                		width : _width,
				                		height : _height,
				                		top:0,
				                		modal : true,
				                		cache : false,
				                		onClose : function() {
				                			$(this).dialog('destroy');
				                		}
				                	});
			            			var chordOption = {
		            					 tooltip : {
		            				        trigger: 'item',
		            				        formatter : function(params,ticket,callback){
		            				        	var itemData = params.data ;
		            				        	if(params.indicator){
		            				        		var sourceNode = params.series.nodes[itemData.source] ;
		            				        		var targetNode = params.series.nodes[itemData.target] ;
		            				        		return sourceNode.text + "->" + targetNode.text + "(" + itemData.count + ")" ;  
		            				        	}else{
		            				        		var text = itemData.text ;
		            				        		if(itemData.value){
		            				        			text += "("+itemData.value+")";
		            				        		}
		            				        		return text ;
		            				        	}
		            				        }
		            				    },
		            				    toolbox: {
		            				        show : false
		            				    },
		            				    legend : {
		            				        data : [],
		            				        orient : 'vertical',
		            				        x : 'left'
		            				    },
		            				    series :[]
				        			};
				        			var eventChordChart = echarts.init(document.getElementById('dialog_main_event_chord'));
				        			eventChordChart.setOption(chordOption);
				        			fillEventChord(eventChordChart, unit, true);
					            }
				            }
					    }
				    },
					noDataLoadingOption: {
			            text: '暂无数据',
			            effect: 'bubble',
			            effectOption: {
			                effect: {
			                    n: 0
			                }
			            }
					},
				    series :[]
				};
			
		    var eventChordChart = echarts.init(document.getElementById('main_event_chord'));
			eventChordChart.setOption(chordOption);
			if(system.userName == "operator"){
				eventChordChart.on("click",onEventchordDataClick) ;
				eventChordChart.on("hover",function(params){
					if(params.data.category != undefined || params.data.source != undefined){
						params.event.target.style.cursor='pointer';
					}
				}) ;
				eventChordChart.on('mouseout', function(param) { param.event.target.style.cursor='default'; });
			}
			fillEventChord(eventChordChart, unit);
    	});
    }
    function onEventchordDataClick(params){
    	var handlerParam = {
    			logQueryParam:{
    				queryStartDate:moment(simHandler.serverTime).format("YYYY-MM-DD 00:00:00"),
    				queryEndDate:moment(simHandler.serverTime).format("YYYY-MM-DD 23:59:59"),
    				securityObjectType:null,
    				condition:[]
    			}
    	};
    	if(params.data.text != null){
    		handlerParam.logQueryParam.condition.push({name:"DEST_ADDRESS",alias:"目的地址",type:"String",value:params.data.text,operator:"等于"});
    	}else{
    		handlerParam.logQueryParam.condition.push({name:"SRC_ADDRESS",alias:"源地址",type:"String",value:this._option.series[0].nodes[params.data.source].text,operator:"等于"});
    		handlerParam.logQueryParam.condition.push({name:"DEST_ADDRESS",alias:"目的地址",type:"String",value:this._option.series[0].nodes[params.data.target].text,operator:"等于"});
    	}
    			
    	openLogQueryWindow(handlerParam) ;
    }
    function fillEventChord(myChart, scope, isFullScreen){
    	var chordOption = myChart.getOption();
    	var scope = scope? scope: '100';
    	var queryConditions={
    		'scope': scope
		};
		$.ajax({
			url : '/sim/index/getForceData',
			async : true,
			type : 'POST',
			data : queryConditions,
			dataType : 'json',
			success : function (result) {
				if(result){
					var list = [];
					if(result.categories){
						 list = [{
							name:result.categories[0],
							symbol : 'circle'
						}];
					}
					var node = {
						type: 'force',
						name: '力导向',
						itemStyle: {
							normal : {
								linkStyle : {
									opacity : 0.5
								}
							}
						},
						categories: list,
						nodes: result.nodes,
						links: result.links,
						minRadius: 5,
						maxRadius: 8,
						gravity: 1.1,
						scaling: 1.1,
						steps: 20,
						large: true,
						useWorker: true,
						linkSymbol:'arrow',
						coolDown: 0.995,
						ribbonType: false
					};
					if(isFullScreen){
						node.size = computeSize(result.count) ;
	                	var parentHeight = $("#dialog_main_event_chord").parent().height() ; 
	                	if(node.size >= parentHeight * 1.6){
	                		$("#dialog_main_event_chord").height(node.size/1.6) ;
	                	}
					}
					chordOption.series.push(node);
					myChart.setOption(chordOption, true);
					myChart.resize();
				}
			}
		});
    	
    }
    /**
     * 计算图的直径大小
     * @param count
     * @returns
     */
    function computeSize(count){
    	var unitNodes = 200 ;//一个单位半径可以显示的节点数
    	var unitSize = 500 ;//一个单位半径的大小
    	var size = count / unitNodes * unitSize ;
    	return Math.max(Math.min(size,2000),1000) ;
    }
	var rightData;
	function showMenu (e, rowIndex, rowData){
		//rowIndex就是当前点击时所在的行索引，rowData当前行的数据
		e.preventDefault();//阻止浏览器默认右键事件
		$("#main_event_table").datagrid("clearSelections");//取消所有选中项
		$("#main_event_table").datagrid("selectRow", rowIndex);//根据索引选中该行
		$("#menu").menu('show', {
			left:e.pageX,//在鼠标点击处显示菜单
			top:e.pageY
		});
		rightData = rowData;
	}
	function eventConfirmMenu(){
		simMainHandler.eventConfirm(rightData.EVENT_ID);
	}
	function openAlarmDialogMenu(){
		simMainHandler.openAlarmDialog(rightData.NAME);
	}
	
	new EchartsModel().initChart();
	//首页地图初始化
	$('#mapTab').tabs({
		border:false,
		onSelect:function(title, index){
			var url = '/sim/index/getMapData';
			switch (index) {
				case 0: initChina(); break;
				case 1: initWorld(); break;
				case 2: initLan(); break;
			}
		}
	});
	
	initCharData();
	function initCharData() {
		$.getJSON('/sim/logSearch/getLogsAmount?_time='+new Date().getTime(),function(resultData) {
				var seriesData = [] ;
				var categories = [] ;
				if(resultData) {
					seriesData.push($.extend(resultData.countData,{type:'bar',barMaxWidth:30,xAxisIndex: 0}));
					seriesData.push($.extend(resultData.sizeData,{type:'bar',barMaxWidth:30,xAxisIndex: 1}));
					categories = resultData.categories;
				}
				showLogBar(resultData,categories,seriesData);
			});
	}
	
	function showLogBar(resultData,categories,seriesdata){
		option = {
			    tooltip : {
			        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
			            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
			        },
			        formatter:function(params,ticker,callback){
			        	var seriesName = params.seriesName ;
			        	var tip = "";
		        		var value = seriesName == "数量" ? 
			        			countFormatter(params.value,0) : 
			        			bytesFormatter(params.value,0);
			        	tip += params.name + "【" + value + "】<br/>" ;
			        	var dataSources = params.series.dataSources[params.name] ;
			        	for(var i in dataSources){
			        		var dataSourceData = dataSources[i];
			        		value = seriesName == "数量" ? 
					        			countFormatter(dataSourceData.counts,0) : 
					        			bytesFormatter(dataSourceData.size,0);
			        		tip += dataSources[i].ip + "【" + value + "】<br/>";
			        	}
			        	return tip ;
			        }
			    },
			    toolbox: {
			       show : true,
			       y:'bottom',
			       feature : {
			    	   exp : {},
			    	   dataView : {
			    		   show: true,
			    		   readOnly: true,
			    		   lang : ["<label style='text-align:center'>日志数量大小统计</label>","返回"],
			    		   optionToContent: function(opt) {
			    			   return createTreeGrid(resultData);
						   }
			    	   }
			       }
			    },
			    grid:{
			    	x:30,y:70,x1:30,y1:70
			    },
			    calculable : true,
			    legend: {
			        data:['大小','数量']
			    },
			    yAxis : [
			        {	
			            axisTick:{
			            	length:0,
			            	show:false
			            },
			        	axisLabel: {
			        			show:false,
			        			rotate: 60
			        	},
			            type : 'category',
			            data : categories
			        }
			    ],
			    xAxis : [
			        {
			        	name : '(数量)',
			            type : 'value',
			            splitNumber:3,
		            	axisLabel: {
			                formatter: function(value){
			                	return countFormatter(value, 0) ;
			                }
			            }
			        },{
			        	name : '(大小)',
			            type : 'value',
			            splitNumber:3,
			            axisLabel: {
			                formatter: function(value){
			                	return bytesFormatter(value, 0) ;
			                }
			            }
			        }
			    ],
			    series : seriesdata
			};
		if(system.isOperator){
			option.toolbox.feature.exp = {
		                show : true,
		                title : '导出数据',
		                icon : 'image://../../img/icons/ecxel.png',
		                onclick : function (params){
		                	window.location = "/sim/logSearch/exportExcelLogData";
		                }
		    	   };
			log_echarts.setOption = option;
		}
		log_echarts.init("main_log_panel", option);
	}
	
	function createTreeGrid(resultData){
		var treeGriddata = [];
		var i = 0;
		for(;i < resultData.categories.length;i++){
			  var name = resultData.categories[i];
			  var value = resultData.countData.data[i];
			  var size = resultData.sizeData.data[i];
			  var childrenData = [];
			  $.each(resultData.logCountJson,function(index,item){
				  if(item.name == resultData.categories[i]){
					  childrenData.push({id:10*i+index,name:item.ip,count:countFormatter(item.value,1),size: bytesFormatter(item.logSize,1)});
				  }
			  })
			  treeGriddata.push({id:i,name:name,count:countFormatter(value,1),size: bytesFormatter(size,1),children:childrenData});
		}
		treeGriddata.push({id:i,name:"总计",count:countFormatter(resultData.countTotal,1),size: bytesFormatter(resultData.sizeTotal,1)});
		setTimeout(function(){
			$("#logInfoTable").treegrid({
				idField:'id',
				treeField:'name',
				border:false,
				columns:[[
				          {title:'名称',field:'name',width:180},
				          {field:'size',title:'大小',width:80},
				          {field:'count',title:'数量(条)',width:80}
				          ]],
				data:treeGriddata
			});
		}, 500) ;
		return "<table id='logInfoTable' style='height:360px;'></table>" ;
	}
	
//	function createTable(resultData){
//		  var table = '<table style="width:100%;border:1px dotted #ccc;">';
//		  table += '<thead style="border-bottom:1px dotted #ccc;"><tr>';
//		  table += '<th style="border:1px dotted #ccc;">名称</th>';
//		  table += '<th style="border:1px dotted #ccc;">数量</th>';
//		  table += '<th style="border:1px dotted #ccc;">大小</th>';
//		  table += '</tr></thead>';
//		  for(var i = 0;i < resultData.categories.length;i++){
//			  var name = resultData.categories[i];
//			  var value = resultData.countData.data[i];
//			  var size = resultData.sizeData.data[i];
//			  table += '<tr style="border:1px dotted #ccc;">';
//			  table += '<td style="border:1px dotted #ccc;text-align:left;">'+ resultData.categories[i] + '</td>';
//			  table += '<td style="border:1px dotted #ccc;">'+ countFormatter(value,1) + '</td>';
//			  table += '<td style="border:1px dotted #ccc;">'+ bytesFormatter(size,1) + '</td>';
//			  $.each(resultData.logCountJson,function(index,item){
//				  if(item.name == resultData.categories[i]){
//					  table += '<tr style="border:1px dotted #ccc;">';
//					  table += '<td style="border:1px dotted #ccc;text-align:right;">'+ item.ip + '</td>';
//					  table += '<td style="border:1px dotted #ccc;" >'+ countFormatter(item.value,1) + '</td>';
//					  table += '<td style="border:1px dotted #ccc;">'+ bytesFormatter(item.logSize,1) + '</td>';  
//				  }
//			  })
//		  }
//		  table += '</tr>';
//		   table += '</tbody></table>';
//		   return table;
//	}
	
	simMainHandler.eventConfirmMenu = eventConfirmMenu;
	simMainHandler.openAlarmDialogMenu = openAlarmDialogMenu;
	simMainHandler.showMenu = showMenu;
	simMainHandler.onLoadSuccess = onLoadSuccess;
	simMainHandler.openAlarmDialog = openAlarmDialog;
	simMainHandler.eventConfirm = eventConfirm;
	simMainHandler.eventOperator = eventOperator;
	simMainHandler.eventOperatorByAsset = eventOperatorByAsset;
	simMainHandler.drawEventNamePieChart = drawEventNamePieChart;
	simMainHandler.drawEventLevelPieChart = drawEventLevelPieChart;
	simMainHandler.deviceEventFormatter = deviceEventFormatter;
	simMainHandler.formatDeviceTableIp = formatterMainDeviceTableIp;
	simMainHandler.loadAlarmOrEventLevelChartData = loadAlarmOrEventLevelChartData;
	simMainHandler.showAssetDetail = showAssetDetail;
	simMainHandler.showAssetEvent = showAssetEvent;
	simMainHandler.showAssetStatus = showAssetStatus;
	simMainHandler.formatDroggleNode = formatDroggleNode;
	simMainHandler.mainDeviceTableLoadSuccess = mainDeviceTableLoadSuccess;
	simMainHandler.formatterEventAndAlarmDes = formatterEventAndAlarmDes;
	simMainHandler.showEventDetail = showEventDetail;
	/**
	 * 拓扑导航菜单触发方法 by horizon
	 */
	simMainHandler.showAssetDetailTp = showAssetDetailTp;
	simMainHandler.changeEventTableTopN = changeEventTableTopN;
	simMainHandler.changeAlarmTableTopN = changeAlarmTableTopN;
	simMainHandler.changeEventLevelBarChart = changeEventLevelBarChart;
	simMainHandler.changeEventNameBarChart = changeEventNameBarChart;
	
	simMainHandler.drawEventRiverChart = drawEventRiverChart;
	simMainHandler.drawEventChordChart = drawEventChordChart;
	simMainHandler.changeEventRiverChart = changeEventRiverChart;
	simMainHandler.changeEventChordChart = changeEventChordChart;
	simMainHandler.expandAssetRow = expandAssetRow;
})();