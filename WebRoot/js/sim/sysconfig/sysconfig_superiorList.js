var superiorListObj = {
	INTERVAL_VAL : 60000,
	PRE : "pre",
	NEXT : "next",
	FLAG_NUM : "horizonTimerStopFlag" + (new Date().getTime()),
	logQuery : function(event, idVal){// 跳转至多级日志查询页面
		if(!idVal) {
			return;
		}
		simHandler.superioripId = idVal;
		simHandler.changeMenu(event, "/page/sysconfig/sysconfig_superiorlog.html");
		simHandler.selectSysconfig();
	},
	eventQuery:function(event, idVal){// 跳转至多级事件页面
		if(!idVal){
			return;
		}
		simHandler.superioripId = idVal;
		simHandler.changeMenu(event,"/page/sysconfig/sysconfig_superiorEvent.html");
		simHandler.selectSysconfig();
	},
	reportQuery:function(event, idVal){// 跳转至多级基本报表页面
		if(!idVal){
			return;
		}
		simHandler.superioripId = idVal;
		simHandler.changeMenu(event, "/page/sysconfig/sysconfig_superiorReport.html");
		simHandler.selectSysconfig();
	},
	deleteChild:function(event, idVal){// 删除子节点
		if(!idVal){
			return;
		}
		$.ajax({
			url: "/sim/systemConfig/superiorConfigDeleteChildNode",
			type: "POST",
			data: {r:(new Date()).getTime(), registIp:superiorListObj.idToip(idVal)},
			dataType:'json',
			success: function(data) {
				if(data.status == 'false') {
					showPopMessage("提示", data.message);
				} else {
					var pageNowNo = $("#horizonPagination").find("li.active a").text();
					superiorListObj.changePage(pageNowNo);
				}
			}
		});
	},
	changePage:function(pageNoTemp) {
		var pageNowNo = $("#horizonPagination").find("li.active a").text();
		
		var pageNo = 1;
		
		if($.isNumeric(pageNoTemp)) {
			pageNo = pageNoTemp;
		} else if(pageNoTemp == superiorListObj.PRE) {
			pageNo = parseInt(pageNowNo) - 1;
		} else if(pageNoTemp == superiorListObj.NEXT) {
			pageNo = parseInt(pageNowNo) + 1;
		}
		
		simSysconfig.withPramsChangePage("/sim/systemConfig/superiorConfigListPage?pageSize=10&pageNo=" + pageNo);
	},
	hasHorizonTimerStopFlagId:function(num) {
		return (superiorListObj.FLAG_NUM == num);
	},
	ipToid:function(ip){// 将ip中的"."替换为"_"定义为 id
		return ip.replace(/[.]/g, "_");
	},
	idToip:function(id){// 将id中的"_"替换为"."定义为 ip
		return id.replace(/[_]/g, ".");
	}
}

$(function(){
	// 收集已经加载的ip节点
	var registDataIpList = [];
	// 收集highcharts组件初始化的数据
	var registHighChartList = [];
	// ip请求收集器
	var idTimerMap = {};
	// 每隔 60s 取一次数据
	function initJsonData() {
		initSuperiorListContainerHW("superiorListContainerId");
		getJsonDataFirst();
		// 获得本地数据加载·插入和删除table元素
		createTimer(getJsonDataFirst, superiorListObj.INTERVAL_VAL);
	}
	// 防止数据过大，添加滚动条
	function initSuperiorListContainerHW(superiorListContainerId){
		var $superiorListContainer = $("#" + superiorListContainerId)
		$superiorListContainer.width($superiorListContainer.parent().width() - 2);
		$superiorListContainer.height($superiorListContainer.parent().height() - 2);
	}
	// 初次异步获取数据fn
	function getJsonDataFirst() {
		$.ajax({
			url: "/sim/systemConfig/superiorConfigListFirst",
			type: "POST",
			data: {r:(new Date()).getTime(), pageSize:horizonPaginationPageSize, pageNo:horizonPaginationPageNo, numFlag:superiorListObj.FLAG_NUM},
			dataType:'json',
			success: function(data) {
				if(superiorListObj.hasHorizonTimerStopFlagId(data.numFlag)){
					initDataFirst(data.dataList);
					initPage(data.page);
				}
			}
		});
	}
	function initPage(page) {
		
		var totalPage = page.totalPage;
		var pageNo = page.pageNo;
		var pageSize = page.pageSize;
		
		var pageDom = "";
		if(!page.havePrevious) {
			pageDom += "<li class='disabled'><a id='horizonPaginationPre' href='javascript:void(0)'>前一页</a></li>";
		} else {
			pageDom += "<li><a id='horizonPaginationPre' href='javascript:superiorListObj.changePage(\"" + superiorListObj.PRE + "\")'>前一页</a></li>";
		}
		
		for(var i = 1; i <= totalPage; i++) {
			if(i != pageNo) {
				pageDom += "<li><a href='javascript:superiorListObj.changePage(" + i + ")'>" + i + "</a></li>";
			} else {
				pageDom += "<li class='active'><a href='javascript:superiorListObj.changePage(" + i + ")'>" + i + "</a></li>";
			}
		}
		
		if(!page.haveNext) {
			pageDom += "<li class='disabled'><a id='horizonPaginationNext' href='javascript:void(0)'>后一页</a></li>";
		} else {
			pageDom += "<li><a id='horizonPaginationNext' href='javascript:superiorListObj.changePage(\"" + superiorListObj.NEXT + "\")'>后一页</a></li>";
		}
		$("#horizonPagination").html(pageDom);
		$("#horizonPaginationText").html("显示" + page.firstNum + "到" + page.lastNum + "，共" + page.totalPage + "页，" + page.total + "条信息");
	}
	// 异步获取数据fn
	function getJsonData(ip) {
		$.ajax({
			url: "/sim/systemConfig/superiorConfigListSingle",
			type: "POST",
			data: {r:(new Date()).getTime(), ip:ip, numFlag:superiorListObj.FLAG_NUM},
			dataType:'json',
			success: function(data) {
				if(data && superiorListObj.hasHorizonTimerStopFlagId(data.numFlag)){
					// 渲染自己的数据
					initData(data);
				}
			}
		});
	}
	// 渲染fn
	function initDataFirst(registDataList) {
		if(registDataList) {
			// 将iplist注册数组赋值给临时变量iplistTemp
			var registDataIpListTemp = registDataIpList.slice(0);
			if(registDataIpList.length > 0) {
				registDataIpList = [];
			}
			$.each(registDataList, function(index, val) {
				var registIp = val.registIp;
				if(registIp) {
					
					var registId = superiorListObj.ipToid(registIp);
					var registIdIndex = $.inArray(registId, registDataIpListTemp);
					// 如果原注册数组中有该ip则删除临时数组中的该数据
					if(registIdIndex != -1) {
						registDataIpListTemp.splice(registIdIndex, 1);
						return true;
					}
					// 添加不存在的tr·如果原注册数组中没有该ip则添加tr
					if($("#tr_" + registId).length === 0) {
						addTrIp(registId, val);
					}
					var valTemp = $.extend({}, val);
					changeTableData(valTemp, registId);
					// 注册ip集合
					registDataIpList.push(registId);
					// 如果id:time集合中没有注册该id，注册并启动该id的timer
					if(!idTimerMap[registId]) {
						getJsonData(registIp);
						idTimerMap[registId] = createTimer(function() {
							getJsonData(registIp);
						}, superiorListObj.INTERVAL_VAL);
					}
				}
			});
			// 移除无效的tr
			removeTrIp(registDataIpListTemp);
		}
	}
	// 异步数据加载使用渲染fn
	function initData(registData) {
		if(registData) {

			var registIp = registData.registIp;
			
			if(registIp) {
				var registId = superiorListObj.ipToid(registIp);
				if($("#tr_" + registId).length === 0) {
					// there is no dom
					return;
				}
				changeTableData(registData, registId);
				if(!idTimerMap[registId]) {
					idTimerMap[registId] = createTimer(getJsonData, superiorListObj.INTERVAL_VAL);
				}
			}
		}
	}
	/**
	 * 页面table渲染
	 */
	function changeTableData(val, registId){
		var sessionid = val.sessionid;
		if(sessionid === "") {
			$("#registState_" + registId)
				.removeClass("icon-status-1")
				.addClass("icon-status-2")
				.tooltip({content:"暂时掉线"});
		} else {
			$("#registState_" + registId)
				.removeClass("icon-status-2")
				.addClass("icon-status-1")
				.tooltip({content:"在线"});
		}
		var cpu = val.cpu_usage;
		var mem = val.mem_usage;
		var logFlow = val.log_flow;
		var storage_usage = val.storage_usage;
		var storage_avaliable = val.storage_avaliable;
		var storage_total = val.storage_total;
		// 渲染图表数据
		changeCharts(cpu, mem, logFlow, storage_usage, storage_avaliable,storage_total, registId);
		
		var registName = val.registName;
		var assetCount = val.assetCount;
		var offlineAssetCount = val.offlineAssetCount;
		var onlineAssetCount = val.onlineAssetCount;
		var alarmCount = val.alarmCount;
		var eventCount = val.eventCount;
		// 渲染非图表数据
		changeOtherDatas(registName, assetCount, offlineAssetCount, alarmCount, eventCount, registId, onlineAssetCount);
	}
	
	// 渲染多个图形组件及点击按钮
	function changeCharts(cpu, mem, logFlow, storage_usage, storage_avaliable, storage_total,registId){

		cpuChart(parseInt(cpu), registId);
		
		memChart(parseInt(mem), registId);
		
		logFlowChart(parseInt(logFlow), registId);

		storageChart(parseFloat(storage_usage), parseFloat(storage_avaliable),parseFloat(storage_total), registId);
		
		initButton(registId);
	}
	/**
	 * 渲染图形组件以外的数据
	 */
	function changeOtherDatas(registName, assetCount, offlineAssetCount, alarmCount, eventCount, registId, onlineAssetCount) {
		$("#registName_" + registId).text(registName);
		$("#registIp_" + registId).text(superiorListObj.idToip(registId));
		$("#assetCount_" + registId).text(assetCount);
		$("#onlineAssetCount_" + registId).text(onlineAssetCount);
		$("#offlineAssetCount_" + registId).text(offlineAssetCount);
/*		$("#alarmCount_" + registId).text(alarmCount);*/
		$("#eventCount_" + registId).text(eventCount);
	}
	function cpuChart(cpu, registId){
		fillDialChart(cpu, registId, "#cpu_usage_", "CPU");
		$("#cpu_usage_"+registId).attr('title', 'CPU:'+cpu+'%');
	}
	function memChart(mem, registId){
		fillDialChart(mem, registId, "#mem_usage_", "内存");
		$("#mem_usage_"+registId).attr('title', '内存:'+mem+'%');
	}
	function logFlowChart(logFlow, registId){
		fillSuperiorLogFlow(logFlow, registId);
	}
	function storageChart(storage_usage, storage_avaliable,storage_total, registId){
		fillStorChart(storage_usage, storage_avaliable,storage_total, registId, "#storage_usage_");
	}
	function initButton(registId){
		$("#easyuiLogbtn_" + registId).linkbutton();
		$("#easyuiEventbtn_" + registId).linkbutton();
		$("#easyuiReportbtn_" + registId).linkbutton();
		$("#easyuiDeletebtn_" + registId).linkbutton();
	}
	/**
	 * 规划表盘图
	 */
	function fillDialChart(data, registId, dialId, name){
		drawCMChart(dialId, name, registId, data);
	}
	/**
	 * 规划磁盘图
	 */
	function fillStorChart(storage_usage, storage_avaliable,storage_total,registId, storId){
    	var storage_chart_percent = storage_usage == undefined ? 0 : storage_usage.toFixed(0) ;
    	var avaliable = (!storage_avaliable || storage_avaliable == 'NaN') ? 0 : kbFormatter(storage_avaliable, 0);
		var total = (!storage_total || storage_total == 'NaN') ? 0 : kbFormatter(storage_total, 0);
		fillDialChart(storage_chart_percent, registId, storId, '磁盘');
		$(storId+registId).attr('title', avaliable+'可用,共'+total);
	}
	 /**
     * 为日志流量列表增加数据,data可以是单一一条数据或者数组
     * @param data
     */
 	var superior_log_flow_data = {};
 	var superior_log_flow_size = 40 ;
    function fillSuperiorLogFlow(data, registId){
		var flowKey = registId + "_data";
		if(!superior_log_flow_data[flowKey]) {
			superior_log_flow_data[flowKey] = [];
    		for(var index=0, leg=superior_log_flow_size; index < leg; index++){
	    		superior_log_flow_data[flowKey].push(0) ;
    		}
		}
    	if($.isArray(data)) {
    		var i,
    			leg;
    		for(i=0, leg=data.length; i < leg; i++){
	    		superior_log_flow_data[flowKey].push(data[i]) ;
    		}
    	} else {
    		superior_log_flow_data[flowKey].push(data) ;
    	}
        if (superior_log_flow_data[flowKey].length > superior_log_flow_size) {
        	superior_log_flow_data[flowKey].splice(0, superior_log_flow_data[flowKey].length - superior_log_flow_size);
        }
        
      //实时流量图
   	 	require.config({
            paths: {
                echarts: '/js/echart/build/dist'
            }
        });
    	require(['echarts', 'echarts/chart/line'], function(echarts) {
    		var option = {
				tooltip : {
					trigger: 'axis',
					formatter:'{a}:{c}条/秒',
					 position : function(p) {
			            return [p[0] + 10, 10];
			        },
					textStyle:{
						fontSize:12
					}
				},
				color:['#00cc66'],		//#6666ff	#228b22	33cccc
				animation:false,
				 xAxis: [{
					 type : 'category',
					 axisLabel:{show:false},
					 splitLine:{show:false},
					 axisLine:{show:false},
					 axisTick:{show:false},
		             data : [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40]
			     }],
			     yAxis: [{
		             type: 'value',
		             axisLabel:{show:false},
		             axisLine:{show:false},
					 splitLine:{show:false}
			     }],
			     grid:{
			    	x:0,
			    	y:0,
			    	x2:0,
			    	y2:2
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
				series : [{
					 name:'日志',
			         type:'line',
			         symbolSize:0,
			         itemStyle: {normal: { lineStyle:{width:1},areaStyle: {type: 'default'}}},
			         data:superior_log_flow_data[flowKey]
				}]
			};
    		var flowLineChart = echarts.init(document.getElementById("log_flow_" + registId)); 
    		flowLineChart.setOption(option); 
    	});
    }
    /**
     * 移除无效的tr
     */
    function removeTrIp(registDataIpListTemp) {
    	var memChartId = "#mem_usage_";
    	var cpuChartId = "#cpu_usage_";
		$.each(registDataIpListTemp, function(index, id) {
			// 停止开启的时间函数、并删除相应的id记录
			clearTimer(idTimerMap[id]);
			delete idTimerMap[id];

			// 销毁对应的tr组件·销毁对应的tooltip提示
			$("#registState_" + id).tooltip("destroy");
			// 销毁对应的tr组件
			$("#tr_" + id).remove();
			var memChartIdIndex = $.inArray(memChartId + id, registHighChartList);
			if(memChartIdIndex !== -1) {
				registHighChartList.splice(memChartIdIndex, 1);
			}
			// 销毁对应的tr组件·消除CPU视图记录
			var cpuChartIdIndex = $.inArray(cpuChartId + id, registHighChartList);
			if(cpuChartIdIndex !== -1) {
				registHighChartList.splice(cpuChartIdIndex, 1);
			}
			// 销毁对应的tr组件·消除流量视图数据记录
			var flowKey = id + "_data";
			if(superior_log_flow_data[flowKey]) {
				delete superior_log_flow_data[flowKey];
			}
		});
    }
    function addTrIp(registId, val){
    	var trExample = "<tr class='tr_ip' id='tr_" + registId + "'>";
    	trExample += "<td id='registName_" + registId + "'></td>";
    	trExample += "<td><span id='registState_" + registId + "' >&nbsp;</span></td>";
    	trExample += "<td id='registIp_" + registId + "'></td>";
		trExample += "<td><div id='cpu_usage_" + registId + "' style='width:100px;height:41px;'></div></td>";
		trExample += "<td><div id='mem_usage_" + registId + "' style='width:100px;height:41px;'></div></td>";
		trExample += "<td><div style='border:solid #99CCFF 1px;width:100px;height:41px;'><div id='log_flow_" + registId + "' style='height:100%;'></div></div></td>";
		trExample += "<td><table class='borderNoTable' style='width:120px;font-size:12px;'>";
		trExample += "<tbody>";
		trExample += "<tr>";
		//trExample += "<td style='padding:0px;'><img src='/img/storage2.png' style='height:30px;width:51px;padding:0;margin:0;' /></td>";
		trExample += "<td><div id='storage_usage_" + registId + "' title='0.0 GB 可用，共0.0 GB' style='width:100px;height:41px;'></div></td>";
		trExample += "</tr>";
		trExample += "</tbody>";
		trExample += "</table></td>";
		trExample += "<td><span class='label label-normal' title='总数' id='assetCount_" + registId + "' ></span>　";
		trExample += "<span class='label label-success' title='在线资产数' id='onlineAssetCount_" + registId + "' ></span>　";
		trExample += "<span class='label label-important' title='不在线资产数' id='offlineAssetCount_" + registId + "' ></span></td>";
/*		trExample += "<td id='alarmCount_" + registId + "'></td>";*/
		trExample += "<td id='eventCount_" + registId + "'></td>";
		trExample += "<td><a id='easyuiLogbtn_" + registId + "' class='easyui-linkbutton' href='javascript:void(0)' onclick='superiorListObj.logQuery(event, \"" + registId + "\")' >日志</a>";
		trExample += "<a id='easyuiEventbtn_" + registId + "'  class='easyui-linkbutton' href='javascript:void(0)' onclick='superiorListObj.eventQuery(event, \"" + registId + "\")' >事件</a><br/>";
		trExample += "<a id='easyuiReportbtn_" + registId + "' class='easyui-linkbutton' href='javascript:void(0)' onclick='superiorListObj.reportQuery(event, \"" + registId + "\")' >报表</a>";
		trExample += "<a id='easyuiDeletebtn_" + registId + "' class='easyui-linkbutton' href='javascript:void(0)' onclick='superiorListObj.deleteChild(event, \"" + registId + "\")' >删除</a></td>";
		trExample += "</tr>";
		$("#tbody_for_add").append(trExample);
    }

    function drawCMChart(containerId, _name, registId, _data) {
    	registHighChartList.push(containerId + registId);
    	var cpuChartContext = ['echarts', 'echarts/chart/gauge'];
   	 	require.config({
            paths: {
                echarts: '/js/echart/build/dist'
            }
        });
	    require(cpuChartContext, function(echarts) {
	   		 // 基于准备好的dom，初始化echarts图表
	   		var option = {
	   				series : [{
	   		    	  name: _name,
	   		    	  startAngle:180,
	   		    	  endAngle:0,
	   		    	  type: 'gauge',
	   		    	  min:0,
	   		    	  max:100,
	   		    	  center : ['50%', '90%'],    // 默认全局居中
	   		    	  radius : '180%',
	   		    	  splitNumber:1,
	   		    	  axisLine: {            // 坐标轴线
	   		    		  lineStyle: {       // 属性lineStyle控制线条样式
	   		    			  width: 2
	   		    		  }
	   		    	  },
	   		    	  axisTick: {            // 坐标轴小标记
	   		    		  length :5,        // 属性length控制线长
	   		    		  lineStyle: {       // 属性lineStyle控制线条样式
	   		    			  color: 'auto'
	   		    		  }
	   		    	  },
	   		    	  axisLabel: {
	   		    		  show : false
	   		    	  },
	   		    	  splitLine: {           // 分隔线
	   		    		  length :2,         // 属性length控制线长
	   		    		  lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
	   		    			  color: 'auto'
	   		    		  }
	   		    	  },
	   		    	  pointer: {
	   		    		  width:2
	   		    	  },
	   		    	  title :{
	   		    		  show : true,
	   		    		  offsetCenter: [0, '-60%'],
	   		    		  textStyle: {
	   		    			  color: '#333',
	   						  fontSize : 12
	   					  }
	   		 		  },
	   		    	  detail : {
	   		    		  offsetCenter: [0, '-80%'],
	   		    		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
	   		    			  fontWeight: 'bolder',
	   		    			  fontSize : 12
	   		    		  }
	   		    	  },
	   		    	  data:[{name: _name, value: _data+'%'}]
	   				}]
	   		};
	 		myChart = echarts.init(document.getElementById((containerId + registId).substring(1))); 
	 		myChart.setOption(option, true); 
	   	});
    }

    // 数据渲染
    initJsonData();
});
