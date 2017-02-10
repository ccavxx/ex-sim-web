var simHandler = {
	token:null,	
	superioripId:null,
	sysconfigDefaultElementId:null,
	colors:['#32C8FA','#F99049','#A4E120','#FFE666','#906BC8', '#08A4F3','#FFA03F','#99CC00','#FFF558','#D040FF', '#99CCFF','#FF7F3A','#1ABEBE','#F7C43B','#FF52B0', '#6666FF','#FE8A8A','#2CB022','#EC6DFF','#95CFD1'],
    level:['非常低','低','中','高','非常高'],
    priorityColor:["#537F9F","#57B52D","#FFB300","#AF2FD1","#DB240B"],
    assetId:null,
    eventId:null,
    logQueryParam:null,
    indexEventQuery:null,
    reportParams:null,
    logQueryObject:null,
    log_stat:null,
    log_statistics_dialog:null,
    log_edit_statistics_dialog:null,
    taskReport_dialog:null,
    schedule_stat:null,
    rightData:null,
    //服务器时间
    serverTime:new Date(),
    forward:function(b64Params){//跳转到指定的url
    	var paramString = decodeURIComponent(cryptico.b64to256(b64Params)) ;
    	var paramObject = JSON.parse(paramString) ;
    	$.extend(this,paramObject.simHandler) ;
    	this.onClickMenuTp(paramObject.menu, paramObject.url) ;
    },
    //自定义验证规则
    rules : {
    	//特殊字符·只能输入汉字、字母、数字和下划线
    	specialchar:[/^([0-9a-zA-Z_\u4e00-\u9fa5-]+)$/,'允许汉字、字母、数字、下划线和中划线'],
    	//特殊字符·只能输入汉字、字母、数字、下划线和点
    	specialcharAndPoint:[/^([0-9a-zA-Z_\.\u4e00-\u9fa5-]+)$/,'允许汉字、字母、数字、点、下划线和中划线'],
		//IP地址验证
		ipv4 : [/^((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))$/, 'IP地址无效'],
		ipv4List : function(element,params){
			var spliter = params && params.length > 0 ? params[0] : "," ;
			var ipArray = $(element).val().split(spliter) ;
			for(var index in ipArray){
				var ip = ipArray[index] ;
				if(ip != "" && isInvalidIp(ip)){
					return "无效的IP地址:"+ip;
				}
			}
			return true ;
		},
		mobile:[/^\d{11}$/,'手机号码无效'],
		mac:[/^([0-9a-fA-F]{2}\-){5}[0-9a-fA-F]{2}$/,'MAC地址无效'],
		datetime:[/^\d{4}\-\d{2}\-\d{2}[ ]\d{2}\:\d{2}\:\d{2}$/,'日期时间无效'],
		seloptsgte1:function(element){
			return !!element.options.length || '至少存在一个选项';
		},
		contentRule:function(element,param,field){//如果通过返回true，否则返回错误消息
			if(element.value == ""){
				return;
			}
			 var contentValue = element.value.toUpperCase();
			 if(contentValue.indexOf("<SCRIPT") >= 0 || contentValue.indexOf("<JAVASCRIPT") >= 0 || 
				contentValue.indexOf("ALERT(") >=0 || contentValue.indexOf("<IMG") >=0 || 
				contentValue.indexOf("<HTML") >= 0 || contentValue.indexOf("<DIV") >=0 || 
				contentValue.indexOf("DOCUMENT.") >= 0 || contentValue.indexOf("<A ") >=0 || 
				contentValue.indexOf("<IFRAME'") >= 0)
			 {
				 return "输入的内容中包括非法内容!";
			 }
		}
	},
	NAV_TITLE_CONFIG:'配置',// 导航名称
	NAV_TITLE_PRODUCTSUPPORT:'产品支持设备',// 导航名称
	selectProductsupport:function() {// 设置“知识库·产品支持设备” 导航菜单为选中状态
		
		$("#topMenu").find(".icon-knowledge").each(function(){
			
			var $a = $(this).parent();
			var $ul = $a.next("ul.dropdown-menu");
			$ul.find("li a").each(function(){
				
				var textTemp = $(this).text();
				if(simHandler.NAV_TITLE_PRODUCTSUPPORT == $.trim(textTemp)){
					
					$('.active').removeClass('active');
					$(this).parent("li").addClass('active');
					$a.parent(".dropdown").addClass('active');
				}
			});
		});
	},
	selectSysconfig:function(){// 设置“配置”导航菜单为选中状态
		
		$("#topMenu").find(".icon-config").each(function(){
			
			var $a = $(this).parent();
			if(simHandler.NAV_TITLE_CONFIG == $.trim($a.text())){
				
				$('.active').removeClass('active');
				$a.parents("li.dropdown").addClass('active');
			}
		});
	},
	clearResidualDomCodes:function() {// 清除导航菜单dom操作中多余的代码
		$("script:last").nextAll("div:not(#_my97DP,.combo-p,.tooltip)").remove();
    },
	goSuperiorListBack:function(event){
		simHandler.sysconfigDefaultElementId = 'initsuperiorList';
		simHandler.changeMenu(event,'/page/sysconfig/index.html');
		simHandler.selectSysconfig();
	},
	showDaterangepickerWin:function(id){// 触发daterangepicker
		$('#'+id).click();
	},
	levelFormatter:function(value, row, index) {//格式化数字表示的等级列
		var res = "<span class='priority"+value+"'/>";
		return res;
	},
	cnLevelFormatter:function(value,row,index){//格式化中文表示的等级列
		var clazz = simHandler.getPriorityClassByCN(value) ;
		return "<span class='"+clazz+"'/>" ;
	},
	getPriorityClassByCN:function(value){//根据中文等级值返回对应的class
		switch(value){
			case "非常低" : return "priority0" ;
			case "低" : return "priority1" ;
			case "中" : return "priority2" ;
			case "高" : return "priority3" ;
			case "非常高" : return "priority4" ;
			default : return "";
		}
	},
	getPriorityLevelColorByIndex:function(value, priorityFlag){
		if( priorityFlag && value >= 0 && value <= 4 ){
			return simHandler.priorityColor[value];
		} else {
			return simHandler.colors[value];
		}
	},
	closelogStaticsDialog:function(){
		try {
			if (simHandler.log_statistics_dialog) {
				simHandler.log_statistics_dialog.dialog('collapse', true);
			}
		} catch (e) {
		}
	},
    /*显示存储数据tip*/
	showStorageTip:function(e,id){
		if($("#"+id).css("display")!="block"){
			$("#"+id).css({display:"block", position:"absolute",top:24+"px", left:-13+"px"});
		}
	},
	/*隐藏存储数据tip*/
	hideStorageTip:function(id){
		$("#"+id).css({display:"none"});
	},
	showRecentEvent:function (){
		if(system.hasOperatorRole){
			openDialog("今日事件信息",800,380,"/page/index/recentEventList.html");
		}
    },
    eventOperator:function(value,row,index){
		var id = row.EVENT_ID;
		var name = row.NAME;
		var html = "" ;
		if (row.isOperator) {
			//html += "<a href='#' class='icon-enabled' style='margin-right:10px;' title='关闭事件' onclick=\"simHandler.eventConfirm('"+id+"');\"></a>";
    		html += "<i title='屏蔽此事件(事件名称、设备地址、源地址、目的地址、描述都相同的事件将不再产生)' onclick='simHandler.addEventLimiter(this,"+index+",60,0)' class='icon-deny hand'/>";
    		html += "<span style='padding-left:5px;'></span>" + "<a href='#' class='icon-alarm icon16 hand' title='关联告警方式' onclick=\"simHandler.openAlarmDialog('"+name+"');\"><a>";
		}
		return html;
	},
	eventConfirm:function(id){
		$.messager.confirm("确认", "你确定要关闭选中事件吗？", function(r){
			if(!r) return;
			$.getJSON('/sim/eventQuery/eventConfirm?ids=' + id + '&state=1', {_time:new Date().getTime()}, function(result){
	    		$("#main_event_table_today").datagrid("reload");
	    		if(!result.status){
	    			showErrorMessage(result.message);
	    		}
	    	});
		});
	},
	dealOperationColumn:function(param){
		if(!system.hasOperatorRole){
			$(this).datagrid("hideColumn","no_field");
		}
	},
	openAlarmDialog:function(name){
		var _this;
		$('<div/>').dialog ({
			title:'告警方式列表',
			href:'/page/main/alarmCorrelationl_today.html',
			width:800,
			height:380,
			modal:true,
			onOpen:function(){
				_this = this;
 		    },
			buttons:[{
				text:'确定',
				handler:function(){
					 var checkRows = $("#alarm_datagrid_today").datagrid("getChecked");
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
					    			 $(_this).dialog('close');
					    		 }else{
					    			 showAlertMessage("关联响应方式失败！");
					    			 return false;
					    		 }
					    	 }
					     });
			    	 });
				    
				 }
			},{
				text:'取消',
				handler:function(){
					$(_this).dialog('close');
				}
			}],
			onClose : function() {
				$(this).dialog('destroy');
			}
		});
	},
	
	onLoadSuccess:function(data){
		var node = $("#main_event_table_today").datagrid("getSelected");
		if(node){
			var name = node.NAME;
			$.getJSON('/sim/eventQuery/queryEventAlarm?name=' + name, {_time:new Date().getTime()}, function(result){
				if(data.rows.length > 0){
					$.each(result.alarm, function(index, item){
						$.each(data.rows,function(index,row){
							if(row.id == item.id){
								$("#alarm_datagrid_today").datagrid("checkRow",index);
								return false;//停止循环
							}
						});
					});
				}
			});
		}
	},
	
	showMenu:function(e, rowIndex, rowData){
		//rowIndex就是当前点击时所在的行索引，rowData当前行的数据
		e.preventDefault();//阻止浏览器默认右键事件
		$("#main_event_table_today").datagrid("clearSelections");//取消所有选中项
		$("#main_event_table_today").datagrid("selectRow", rowIndex);//根据索引选中该行
		$("#menu_today").menu('show', {
			left:e.pageX,//在鼠标点击处显示菜单
			top:e.pageY
		});
		rightData = rowData;
	},
	eventConfirmMenu:function(){
		simHandler.eventConfirm(rightData.EVENT_ID);
	},
	openAlarmDialogMenu:function(){
		simHandler.openAlarmDialog(rightData.NAME);
	},
    showEventDetail:function(eventId,name){
    	simHandler.eventId = eventId ;
    	openDialog(name,800,500,'/page/index/eventLogs.html');
    },
    showEventDetailUseUUID:function(uuid,name){//使用uuid进行事件回溯时必须指定事件发生的时间，这样查询会使用索引，因为uuid没有做索引
    	simHandler.eventUUID = uuid ;
    	openDialog(name,800,500,'/page/index/eventLogs.html') ;
    },
    showEventIconAct:function(it){
		var $as = $(it).parent().children();
		$as.each(function(){
			$(this).removeClass("panelToolsAct");
		});
		$(it).addClass("panelToolsAct");
    },
    viewDataSourceLog:function(securityObjectType,id,nodeId,deviceIp){
    	var param = new Object() ;
    	param.securityObjectType = securityObjectType ;
    	param.dataSourceId = id ;
    	param.nodeId = nodeId ;
    	param.deviceIp = deviceIp ;
    	var servertime = simHandler.serverTime; 
    	param.queryStartDate = moment(servertime).startOf('day').format('YYYY-MM-DD HH:mm:ss');
    	param.queryEndDate = moment(servertime).format('YYYY-MM-DD HH:mm:ss');
    	this.logQueryParam = param ;
    	this.onClickMenuTp("menu_log_query", "/page/log/logQuery2Main.html") ;
//    	openLogQueryWindow(param);
//    	openNewWindow('menu_log_query',"/page/log/logQuery2Main.html",param);
    },
    viewReport:function(securityObjectType,id,nodeId,deviceIp){
    	var param = new Object() ;
    	param.securityObjectType = securityObjectType ;
    	param.dataSourceId = id ;
    	param.nodeId = nodeId ;
    	param.deviceIp = deviceIp ;
    	this.reportParams = param ;
    	this.onClickMenuTp("base_report", "/page/report/base_report.html") ;
    },
    ping:function(ip){
    	openDialog('ping'+ip,600,400,'/page/asset/ping.jsp?ip='+ip);
    },
    openEventDetail:function(rowIndex,rowData){
    	simHandler.showEventDetail(rowData["EVENT_ID"],rowData["NAME"]) ;
    },
    eventNameFormatter:function(value,row){
    	return "<span class='table_column_link' onclick=\"simHandler.showEventDetail('"+ row["EVENT_ID"] + "','"+row["NAME"]+"');\">"+value+"</span>" ;
    },
    openEventDetailFEM:function(rowIndex,rowData){//事件查询时返回EventModel数据结构使用此方法
    	simHandler.showEventDetail(rowData["eventId"],rowData["name"]) ;
    },
    eventDescFormatter:function(value,row,index){
      return "<span title ='"+value+"'>"+ value +"</span>";
    },
    eventNameFEM:function(value,row){//事件查询时返回EventModel数据结构使用此方法
    	return "<span class='table_column_link' onclick=\"simHandler.showEventDetail('"+ row["eventId"] + "','"+row["name"]+"');\">"+value+"</span>" ;
    },
    eventRowDBClickHandler:function(rowIndex,row){
    	simHandler.showEventDetail(row["EVENT_ID"], row["NAME"]) ;
    },
    eventName4UUID:function(value,row){//使用uuid时进行回溯
    	return "<span class='table_column_link' onclick=\"simHandler.showEventDetailUseUUID('"+ row["uuid"] + "','"+row["name"]+"');\">"+value+"</span>" ;
    },
	_horizonSetMsgSize:function(panel) {
		var mask = panel.children("div.datagrid-mask.horizon-mask");
		if (mask.length) {
			mask.css( {
				width : panel.outerWidth(),
				height : panel.outerHeight()
			});
			var msg = panel.children("div.datagrid-mask-msg.horizon-mask-msg");
			msg.css({
				left : (panel.outerWidth() - msg.outerWidth()) / 2,
				top : (panel.outerHeight() - msg.outerHeight()) / 2
			});
		}
	},
    openBodyLoadingModal:function(loadMsg) {// 开启body状态加载框
		if (!loadMsg) {
			loadMsg = "<span style='color:red;'>努力加载中，请稍等......</span>";
		}
		$(document.body).children("div.datagrid-mask-msg.horizon-mask-msg").remove();
		$(document.body).children("div.datagrid-mask.horizon-mask").remove();
		var headZindex = $("body>.sim-header").css("z-index");
		if(!headZindex) {
			headZindex = 1030;
		}
		$("<div class=\"datagrid-mask horizon-mask\" style=\"display:block;\"></div>").css("z-index",headZindex + 1).appendTo($(document.body));
		$("<div class=\"datagrid-mask-msg horizon-mask-msg\" style=\"display:block\"></div>").html(loadMsg).appendTo($(document.body));
		simHandler._horizonSetMsgSize($(document.body));
    },
    openLoadingModal:function(panel, loadMsg) {// 开启状态加载框
    	if (!loadMsg) {
    		loadMsg = "<span style='color:red;'>努力加载中，请稍等......</span>";
    	}
    	if(panel.css("position")) {
    		panel.attr("horizonPosition", panel.css("position"));
    	}
    	panel.css("position", "relative");
    	panel.children("div.datagrid-mask-msg.horizon-mask-msg").remove();
    	panel.children("div.datagrid-mask.horizon-mask").remove();
    	$("<div class=\"datagrid-mask horizon-mask\" style=\"display:block\"></div>").appendTo(panel);
    	$("<div class=\"datagrid-mask-msg horizon-mask-msg\" style=\"display:block\"></div>").html(loadMsg).appendTo(panel);
    	simHandler._horizonSetMsgSize(panel);
    },
    closeLoadingModal:function(panel) {// 关闭状态加载框
    	panel.css("position", "");
    	if(panel.attr("horizonPosition")) {
    		panel.css("position", panel.attr("horizonPosition"));
    		panel.removeAttr("horizonPosition");
    	}
    	panel.children("div.datagrid-mask-msg.horizon-mask-msg").remove();
		panel.children("div.datagrid-mask.horizon-mask").remove();
    },
    cancelDefaultLoadingModal:function(panel) {// 取消easyui中datagrid原有的状态加载框
    	panel.children("div.datagrid-mask-msg").remove();
		panel.children("div.datagrid-mask").remove();
    },
    eventTimeFormatter:function(value,row,index){
    	if (row.isOperator) {
    		return "<i title='屏蔽此事件(事件名称、设备地址、源地址、目的地址、描述都相同的事件将不再产生)' onclick='simHandler.addEventLimiter(this,"+index+",60,0)' class='icon-deny hand'/>"+"<span style='padding-left:2px;'>"+value+"</span>";
		} else {
			return value;
		}
    },
    addEventLimiter:function(el,index,window,rate){
		var datagridId = $(el).parents(".datagrid-view").children(".easyui-datagrid").attr("id") ;
		var event = $("#"+datagridId).datagrid("getRows")[index] ;
		window = nvl(window,60) ;//默认一小时
		rate = nvl(rate,0) ;//默认屏蔽所有告警
		var limiter = {
				name:event.NAME,
				deviceAddress:event.DVC_ADDRESS,
				srcAddress:event.SRC_ADDRESS,
				destAddress:event.DEST_ADDRESS,
				description:event.DESCR,
				window:window,
				rate:rate,
				available:true
		}
		$.post("/sim/EventFilterRule/save",limiter,function(result){
			if(result.success){
				showAlertMessage("事件过滤规则添加成功！") ;
			}else{
				showErrorMessage(result.message) ;
			}
		},"json") ;
	}
};
loadPage=function(){
	//日志实时流量数据集合
	var log_flow_data = [];
	var log_flow_size = 40 ;
    function drawCpuMemChart(){
    	var cpuChartContext = ['echarts', 'echarts/chart/gauge'];
    	 require.config({
             paths: {
                 echarts: '/js/echart/build/dist'
             }
         });
    	require(cpuChartContext, function(echarts) {
    		 // 基于准备好的dom，初始化echarts图表
    		var option = {
    				toolbox: {
    					show : false
    				},
    				series : []
    		};
    		var base = {
  	        	  name:'',
  	        	  type:'gauge',
  	        	  min:0,
  	        	  max:100,
  	        	  center : [],    // 默认全局居中
  	        	  radius : '180%',
  	        	  startAngle:180,
  	        	  endAngle:0,
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
  	        		  length:'95%',
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
  	        		  },
  	        		  formatter:'{value}%'
  	        	  },
  	        	  name : '',
  	        	  data:[]
      		};
    		for(var i = 0; i < 3; i++){
    			option.series[i] = new Object();
    			for(var p in base){ 
    				option.series[i][p]=base[p]; 
    			} 
    		}
      		option.series[0].name = 'CPU';
      		option.series[0].center = ['15%', '100%'];
      		option.series[0].data = [{value: 20, name: 'CPU'}];
      		option.series[1].name = '内存';
      		option.series[1].center = ['50%', '100%'];
      		option.series[1].data = [{value: 30, name: '内存'}];
      		option.series[2].name = '磁盘';
      		option.series[2].center = ['85%', '100%'];
      		option.series[2].data = [{value: 40, name: '磁盘'}];
    		myChart = echarts.init(document.getElementById('top_cpu_mem_container')); 
            myChart.setOption(option); 
            getTopRealTimeData(myChart);
    	});
    }

    /**
     * 获取顶部实时数据
     */
    function getTopRealTimeData(cpu_mem_chart) {
    	loadRealTimeData(log_flow_data,cpu_mem_chart) ;
        setInterval(function () {
        	loadRealTimeData(log_flow_data,cpu_mem_chart,null);
        }, 15000);
    }
    
    function loadRealTimeData(log_flow_data,cpu_mem_chart){
    	$.getJSON('/sim/index/serverRealTimeData',{r:(new Date()).getTime()},function(data){
    		if(data){
    			//更新CPU、内存、流量仪表盘数据
    			var option = cpu_mem_chart.getOption();
    			option.series[0].data[0].value = data.cpu_usage?data.cpu_usage:0;
    			option.series[1].data[0].value = data.mem_usage?data.mem_usage:0;
    			var mem_total, mem_avaliable;
    			if(data.mem_usage){
    				mem_total = data.mem_total?data.mem_total/1024:0;
    				var scale = mem_total > (1024*1024*1024) ? 2 : 0 ;
    				mem_avaliable = (100-data.mem_usage)*mem_total/100;
    				mem_total = kbFormatter(mem_total,scale);
    				scale = mem_avaliable > (1024*1024*1024) ? 2 : 0 ;
    				mem_avaliable = kbFormatter(mem_avaliable,scale);
    			}else {
    				mem_avaliable = mem_total = 0;
    			}
    			
    			var avaliable, total;
    			option.series[2].data[0].value = data.storage_usage == undefined ? 0 : data.storage_usage.toFixed(0) ;
    			if(data.storage_avaliable || data.storage_total){
    				scale = data.storage_avaliable > (1024*1024*1024) ? 2 : 0 ;
    				avaliable = kbFormatter(data.storage_avaliable,scale);
    				scale = data.storage_total > (1024*1024*1024) ? 2 : 0 ;
    				total = kbFormatter(data.storage_total,scale);
    			} else {
    				avaliable = total = 0;
    			}
				
    			option.tooltip = {
    				formatter:function(tip){
    					if(tip.seriesName == '磁盘'){
    						return avaliable +'可用,共'+ total;
    					}else if(tip.seriesName == '内存'){
    						return mem_avaliable +'可用,共'+ mem_total;
    					}else{
    						return tip.seriesName+':'+tip.value+'%';
    					}
    				},
   					position : function(p) {
   						return [p[0]-20, 16];
   			        },
   					textStyle:{
   						fontSize:12
   					}
   			    };
    			option.series[2].tooltip = {
    				trigger: 'item',
					formatter:function(tip){
						return avaliable +'可用,共'+ total;
    				},
   					position : function(p) {
   						return [p[0]-130, 16];
   			        },
   					textStyle:{
   						fontSize:12
   					}
    			};
    			cpu_mem_chart.setOption(option, true);
    			
    			var realTimeEvents = data.realTimeEvents;
    			if(realTimeEvents) {
    				var eventMsg = "<ul style='margin-bottom:0;'>";
    				$.each(realTimeEvents, function(ind, val) {
    					eventMsg += ("<li>" + val.eventName + "</li>");
    				});
    				eventMsg += "</ul>";
    				var msg = "<div style='overflow-y:auto;height:100px;font-size:12px;'>" + eventMsg + "</div>";
    	        	showPopMessage("提示", msg, {width:200, height:160, timeout:2000});
    			}
                if(data.log_flow != undefined){
                	fillLogFlow(data.log_flow) ;
                }
                if(data.eventCount != undefined){
                	$("#eventCount").html("事件: "+data.eventCount) ;
                	$("#eventCount").parent().attr("title","今日事件数量:"+data.eventCount+"条/秒")
                }
                simHandler.serverTime = moment(data.server_time).toDate();
    		}
    	});
    }
    //绘制头部存储柱状图
    function drawStroageChart(){
		var top_storage = $('#top_storage_container').progressbar();
    }
    //点击菜单处理函数
    function onClickMenu(e,url){
    	// 清除页面加载中dom操作中多余的代码
    	simHandler.clearResidualDomCodes();
    	clearAllTimer() ;
    	$('.active').removeClass('active');
    	var target = e.srcElement ? e.srcElement : e.target ;
    	if(target.tagName.toLowerCase()=='a'){
    		$(target).parent().addClass('active');
    	} else if(target.tagName.toLowerCase()=='i'){
    		$(target).parent().parent().addClass('active');
    	}
        if (target.parentElement.parentElement.nodeName == 'UL' && target.parentElement.parentElement.getAttribute('class') == 'dropdown-menu') {
            target.parentElement.parentElement.parentElement.setAttribute("class","dropdown active");
        };
    	//changeMainContent(url);
    	$('#main_panel').panel('refresh',url);
    	$('#main_panel').panel('resize');
    }

    /**
     * 点击拓扑菜单处理函数 by horizon
     */
    function onClickMenuTp(id,url){
    	simHandler.clearResidualDomCodes();
    	clearAllTimer();
    	if(id){
    		$('.active').removeClass('active');
    		$("#"+id).parents(".dropdown").addClass('active');
    		var target = $("#"+id);
    			target.parent().addClass("active");
    	}
    	$('#main_panel').panel('refresh',url);
    	$('#main_panel').panel('resize');
    }

    function changeMainContent(url){
        $('#main_content').empty();
        $.ajax({
            url : url,
            cache : false,
            dataType : "html",
            success : function (data) {
                $('#main_content').append(data);
            }
        });
    }
    /**
     * 获取最近日志流量列表
     */
    function getLogFlowList(){
    	$.getJSON("/sim/index/logFlowList?limit="+log_flow_size+"&_time="+new Date().getTime(),function(result){
    		if(result && result.length > 0){
    			fillLogFlow(result) ;
    		}
    	}) ;
    }
    /**
     * 为日志流量列表增加数据,data可以是单一一条数据或者数组
     * @param data 
     */
    function fillLogFlow(data){
    	if($.isArray(data)){
    		for(var i=0;i<data.length;i++){
    			log_flow_data.push(data[i]) ;
    		}
    	}else{
    		log_flow_data.push(data);
    	}
        if (log_flow_data.length > log_flow_size)
         	log_flow_data.splice(0,log_flow_data.length-log_flow_size);
        
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
					 boundaryGap:false,
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
					x2:1,
					y2:1
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
			         data:log_flow_data
				}]
			};
    		var flowLineChart = echarts.init(document.getElementById('log_flow')); 
    		flowLineChart.setOption(option); 
    	});
//        $('#log_flow').sparkline(log_flow_data, { width: 130,height:38, tooltipPrefix:'日志:',chartRangeMin:0});
    }
    /**
     * 获取日志数
     */
    function getLogCount(){
    	queryLogCount() ;
    	setInterval(queryLogCount, 30000) ;
    }
    function queryLogCount(){
    	$.getJSON("/sim/logSearch/queryTodayLogCount",{_time:new Date().getTime()},function(result){
			if(result.status){
				$("#logCount").html("日志: "+countFormatter(result.result,1)) ;
				$("#logCount").parent().attr("title","今日日志数:"+result.result+"条/秒")
			}else{
				$("#logCount").html("日志:未知") ;
			}
		}) ;
    }


    //绘制头部存储柱状图
    function drawStroageChart(){
		var top_storage = $('#top_storage_container').progressbar();
    }
    function getSystemInfo(){
    	var clientPublicKey = cryptico.publicKeyString(system.rsaClientKey) ;
    	var data = {
    		clientPublicKey:clientPublicKey,
    		_date:new Date().getTime()
    	};
    	$.post('/sim/index/getSystemInfo',data,function(result){
    		simHandler.systemInfo = result ;
    	},"json") ;
    }
    function bindMenuEvent(){
    	// 对导航进行鼠标滑动监听
    	$("#header-nav>.nav>li").mouseenter(function(){
    		$("#header-nav.sim-menu.navbar-inner>.nav>li.dropdown").each(function(index,element){
    			$(this).removeClass("open");
    		});
    		if($(this).find(".dropdown-toggle").length>0){
    			$(this).addClass("open");
    		}
    	});
    	$("#header-nav>.nav>li").mouseleave(function(){
    		if($(this).find(".dropdown-toggle").length>0){
    			$(this).removeClass("open");
    		}
    	});   
    	$(".dropdown-menu > li").mouseenter(function(){
    		$(this).addClass("menu-hover") ;
    	}) ;
    	$(".dropdown-menu > li").mouseleave(function(){
			$(this).removeClass("menu-hover") ;
    	}) ;
    }
    
    //初始化头部视图
    function initTopView(){
    	//获取系统信息
    	getSystemInfo() ;
    	
    	bindMenuEvent() ;
    	//绘制CPU和内存图
        drawCpuMemChart();
        //绘制存储柱图
        drawStroageChart();
        //实时更新顶部数据
//        getTopRealTimeData();
        //获取日志流量信息
        getLogFlowList() ;
        //获取当天日志数量
        getLogCount() ;
        //服务器时间定时器
        var counter = 0 ;
        setInterval(function(){
        	if(simHandler.serverTime){
        		simHandler.serverTime = moment(simHandler.serverTime).add('seconds', 1).toDate() ;
        		$("#server_time").html(moment(simHandler.serverTime).format("YYYY-MM-DD HH:mm:ss")) ;
        	}
        	if((counter++)%5 == 0){//每5秒发送一次保活信号
        		$.get("/sim/keepAlive?token="+system.token+"&_time="+new Date().getTime(),function(result){
        			var result = parseInt(result) ; 
        			if(result == -1){
        				window.location.href = "/sim/userLogin/logout" ;
        			}else if(result == -2){
        				window.location.href = "/page/login.jsp" ;
        			}
        		},"text") ;
        	}
        },1000);
        //初始化主页面
        var width = $(window).width();
	    var height = $(window).height();
	    if(isBlank(system.forwardParams)){//如果页面指定了跳转参数，则不需要加载main页面
	    	$('#main_panel').panel({ width:width, height:height-88-32, href:'/page/main/main.html' });
	    	setTimeout(function(){ simHandler.initReportQueryLog(); },300);
	    }else{
	    	$('#main_panel').panel({ width:width, height:height-88-32, content:"" });
	    }
    }

    /**
     * 导航拓扑菜单触发方法 by horizon
     */    
    simHandler.onClickMenuTp = onClickMenuTp;
    simHandler.changeMenu = onClickMenu;
    simHandler.getTopRealTimeData = getTopRealTimeData;
    return initTopView();
};
simHandler.initReportQueryLog=function(){
	var url=window.location.href;
	var paramstring = null;
	if(url.indexOf("?")>0){
		var reportQueryUrl=url.substring(url.indexOf("?")+1, url.length);
		if(reportQueryUrl.indexOf('reportQueryLog')>-1){
			reportQueryUrl=reportQueryUrl.substring(reportQueryUrl.indexOf('&', 0)+1);
			paramstring=reportQueryUrl.split("&" );
		}
	}
	if(paramstring != null){
		//参数组
	    var paraObj = {};
	    for (var i = 0; j = paramstring[i]; i++) {
	        paraObj[j.substring(0, j.indexOf("="))] = ''+decodeURI(j.substring(j.indexOf("=" )+ 1, j.length));
	    }
	    simHandler.logQueryObject=paraObj;
		simHandler.onClickMenuTp('menu_log_query','/page/log/logQuery2Main.html');
		var isHtml5=false;
		try{
			if(undefined != window.history.pushState){
				if(typeof(window.history.pushState) == "function"){
					isHtml5=true;
				}
			}
		}catch(e){
			isHtml5=false;
		}
		if(isHtml5){
			window.history.pushState( null, null, '/sim/index/');
		}
	}
};
/**
 * 点击tooltip当前在线用户列表
 */
simHandler.onlineUsers = function() {
	$("#showOnlineUsersId").click(function(){
		$("#showOnlineUsersId").tooltip('update');
	});
	$("#showOnlineUsersId").tooltip({
		position: "bottom",
		content: $('<div></div>'),
		showEvent: 'click',
		onUpdate: function(content){
			content.panel({
				width: 280,
				height:200,
				border: false,
				href: '/page/onlineUserList.html'
			});
		},
		onShow: function() {
			var t = $(this);
			t.tooltip('tip').unbind().bind('mouseenter', function() {
				t.tooltip('show');
			}).bind('mouseleave', function() {
				t.tooltip('hide');
			});
		},
		onHide:function(){
			try{
            	if($.browser.msie && parseFloat($.browser.version) < 9){
            		$("#main_event_name_pie_chart .highcharts-series > div").css("left","0px");
            		$("#main_event_level_pie_chart .highcharts-series > div").css("left","0px");
            	}
            }catch(e){
            }
		}
	});
	$("#showOnlineUsersId").attr("title","当前用户") ;
}
simHandler.newServerDate = function(){
	return new Date(simHandler.serverTime.getTime()) ;
}
$(function(){
	loadPage();
	simHandler.onlineUsers();
	$(document).ajaxError(function(event,request,options,errorMsg){
		if(request.status == 555){//客户端提交了非法的内容
			alert(request.responseText) ;
		}
	}) ;
	if(isNotBlank(system.forwardParams)){
		simHandler.forward(system.forwardParams) ;
	}
});
var lockDiv ;
function lockUser(){
	$.getJSON("/sim/userLogin/lock?_time="+new Date().getTime(),function(result){
		if(result){
			lockPage() ;
		}
	}) ;  		
}

function lockPage(){
	lockDiv = $("<div/>").dialog({
		height:$(window).height(),
		width:$(window).width(),
		href:"/page/lock_page.jsp",
		noheader:true
	}) ;
}
function unlock(){
	var password = $("#password").val() ;
	if(password.length == 0){
		$("#error").html("密码不能为空！") ;
		return;
	}else{
		password = rsaEncrypt(password) ;
	}
	$.getJSON("/sim/userLogin/unlock?password=" + password,function(result){
		if(result.success){
			lockDiv.dialog("destroy") ;
		}else{
			$("#error").html(result.message) ;
			$("#password").select().focus();
		}
	})
}
if(system.locked){
	lockPage() ;
}
