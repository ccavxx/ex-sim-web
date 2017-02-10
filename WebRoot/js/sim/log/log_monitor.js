var simLogMonitorHandler = {
	detailFormatter : function(index, row) {
		var content = "<pre style='padding:2px 5px;margin:1px;'>" + row.ORIGINAL_DATA + "</pre>";
		return content;
    },
    onExpandRow : function(index, row) {
        $("#log_table").datagrid('fixDetailRowHeight', index);
    }
};
var fretime =10*1000;
var logCache = new Array();//日志缓存对列
var logCacheLimit = 100 ;//日志对列数量上限
var heartTimer;
var getDataTimer;//获取后台日志的定时器
var insertDataTimer ;//从缓存中插入数据到datagrid中的定时器
var pageRowSize = 50 ;
$(function(){
	simLogMonitorHandler.onLoadSuccess = function(data){
	}
	
	simLogMonitorHandler.formatterOriginalDataField = function(value,row,index){
		if(!!value && value.length > 140){
			return  "<abbr class='original-data' title='"+value+"'>"+value.substring(0,140)+"...</abbr>";
		}
		return value;
	}
	
	simLogMonitorHandler.formatterShowOriginalData = function(value,row,index){
		return "<a class='icon-start icon16' href='#' onclick=\"showOriginalLogDetails('"+row["UUID"]+"',this)\"></a>" ;
	}
});

function showEventGrid(){
	$("#eventGrid").datagrid("getPanel").panel("open") ;
}
function showEventList(){
	$("#eventList").datagrid("load");
	$("#eventListPanel").panel("open");
}

function loadRawLog(index,row){
	$('#ddv-'+index).css('display','block');
	$('#logList').datagrid('fixDetailRowHeight',index);
}
function detailFormatter(index,row){
	return '<div style="padding:2px;display:none;" id="ddv-' + index + '">'+row.rawLog+'</div>';
}
function searchView(){
	var asset_table_panel = $('#result_panle').panel('panel');
	if(!asset_table_panel)return;
	$(asset_table_panel).parent().css('position','relative');
	var width = $(asset_table_panel).width();
	var height = $(asset_table_panel).height();
	$("#search_log").dialog({
		top:0,
		left:0,
		width:width,
		height:400,
		inline:true,
		noheader:true,
		collapsed:true,
		modal:true,
		shadow:false,
		border:false,
		href:'logSearchView.html',
		style:{'padding':0,'border':0},
		onCollapse:coloseSearchPanel
	}).dialog('expand',true);
}

var tmp_close_count = 0;
function coloseSearchPanel(){
	tmp_close_count++;
	if(tmp_close_count%2 == 0){
		$('#search_log').dialog('close',false);
	}
		
}
function onCancelSearchView(){
	$('#search_log').dialog('collapse',true);
	
}

/************清空数据**********/
function onLogMonitorClean(){
	logCache.splice(0, logCache.length) ;
	$.ajax({
		type : "post",
		url : "../../sim/logMonitor/clearData",
		async : false,
		dataType : "json",
		success : function(data) {
			if (data.success=='true') {
				 $("#log_table").datagrid('loadData',{total:0,rows:[]});
			}else{
				showErrorMessage("清除日志失败！");
			}
		}
	});
}
/*********加载日志列表*********/
function getLogMonitorData(){
	$.ajax({
		url:'/sim/logMonitor/changIsSend',
		type:'post',
		async: false,
		success : function(data) {
		}
	});
}

/******初始化启动刷新*********/
function startHeartTimer(fretime) {
	heartTimer=createTimer(getLogMonitorData,fretime);
}
var stopState = false;
/**********停止逐步插入数据***********/
function onLogDataStop(){
	clearTimer(getDataTimer);
	//clearTimer(heartTimer);
	clearTimer(insertDataTimer) ;
	stopState = true;
	$("#logStop>.l-btn-left>.l-btn-icon-left").html("启动刷新");
	$("#logStop>.l-btn-left>.l-btn-icon-left").removeClass("icon-stop");
	$("#logStop>.l-btn-left>.l-btn-icon-left").addClass("icon-start");
	$("#logStop").attr("onclick","onLogDataStart()");
}
function onLogDataStart() {
	$("#logStop>.l-btn-left>.l-btn-icon-left").html("停止刷新");
	$("#logStop>.l-btn-left>.l-btn-icon-left").removeClass("icon-start");
	$("#logStop>.l-btn-left>.l-btn-icon-left").addClass("icon-stop");
	$("#logStop").attr("onclick", "onLogDataStop()");
	if(stopState) {
		onLoadLogMonitorData();
		stopState = false;
	}
}
var log_url = "";
function loadLogData() {
    log_url = "/sim/logMonitor/getLogData?_time=" + new Date().getTime();
	$.getJSON(log_url, function(logData) {
		if(logData != null &&logData.length > 0) {
			insertRowLog(logData);
		}
	});
}
/********初始化加载数据*********/
function onLoadLogMonitorData(){
	setTimeout(loadLogData, 3000) ;
	getDataTimer = createTimer(loadLogData,fretime);
	insertDataTimer = createTimer(loadRowDataFromCache,500) ;
}
/********向datagrid逐步插入数据**********/
var index=0;
function insertRowLog(logDatas){
	if(logDatas!=null){
		for(var i=0;i<logDatas.length;i++){
			logCache.push(logDatas[i]) ;
		}
		if(logCache.length > logCacheLimit){
			logCache = logCache.slice(0, logCacheLimit) ;
		}
   }
}

function loadRowDataFromCache(){
	var log = logCache.shift() ;
	if(!log) return ;
	var logTable = $("#log_table") ; 
	logTable.datagrid('insertRow',{
		index:0,
		row:{
			UUID:log.UUID,
			START_TIME:log.START_TIME,
			DVC_ADDRESS:log.DVC_ADDRESS,
			DVC_TYPE:log.DVC_TYPE,
			ORIGINAL_DATA:log.ORIGINAL_DATA
		}
	});
	if(logTable.datagrid("getRows").length > pageRowSize ){
		logTable.datagrid("deleteRow",pageRowSize) ;
	}
}
var oldNode="";
var filter="";
/***点击日志树触发事件*******/
$('#logTree').tree({
	url:'/sim/logSearch/getTree?flagMonitor=true',
	lines:true,
    formatter:function(node){
		var temp = '<span ';
		if(node.attributes&&node.attributes.host){
			temp += ('title=\''+node.attributes.host+'\'');
		}
		temp += ('>'+node.text+'</span>');
		return temp;
    },
	onLoadSuccess:function(node,data){
		var log_cat_tree = $("#logTree") ;
		var rootNode = log_cat_tree.tree('getRoot');
		if(rootNode){
			log_cat_tree.tree("select", rootNode.target); 
		}
	},
	onSelect: function(node) {
		  // 清空数据
		  onLogMonitorClean();
		  // 过滤条件
		  if(node.attributes) {
			  
			  var nodeId = node.attributes.nodeId;
			  var deviceType = node.attributes.deviceType;
			  var host = node.attributes.host;
			  if(deviceType == "ALL/ALL/Default") {
				  $("#log_group_form").hide();
				  filter = "";
			  } else {
				  $("#log_group_form").show();
				  if(oldNode) {
					  if(oldNode != deviceType) {
						  filter = "";
					  }
				  }
				  filter = loadGroup(deviceType);
				  oldNode = deviceType;
			  }
		      setLogFilter(nodeId,deviceType,host,filter);
		   
		  } else {
			  setLogFilter(null, null, null, null);
		  }
		  setTimeout(getLogMonitorData, 3000) ;
		  if($("#logStop>.l-btn-left>.l-btn-icon-left").html()=="启动刷新"){
			  onLogDataStart();
		  }
	}
});
//根据设备类型加载列集
function loadGroup(deviceType){
	var firstValue = "";
	$.ajax({
		 url:'/sim/logMonitor/getGroupByDeviceType?deviceType=' + deviceType,
		 type:'post',
		 dataType:'json',
		 async:false,
		 success : function(data) {
			 if(data.length>0){
				 firstValue = data[0]["filter"];
				 loadSelectOption(data);
			 }
		 }
	});
	return firstValue;
}
function loadSelectOption(data) {
	data[0]["selected"] = true;
	for(var i = 0; i<data.length; i++) {
		var filter = data[i].filter;
		if(filter == "") {
			data[i].filter = "null_" + i;
		}
	}
	$('#log_group').combobox({
		height:24,
		width:120,
		valueField:'filter',
		textField:'name',
		onChange:function(newValue, oldValue) {
			onChangeSelect(newValue);
			
		}
	}).combobox("loadData", data);
}
function onChangeSelect(obj) {
	// 清空数据
	onLogMonitorClean();
	var node = $("#logTree").tree("getSelected");
	if(node.attributes) {
		var nodeId = node.attributes.nodeId;
	  	var deviceType = node.attributes.deviceType;
	  	var host = node.attributes.host;
	  	
	  	if(deviceType == "ALL/ALL/Default"){
	  		$("#log_group_form").hide();
	  		obj = "";
	  	}
      	setLogFilter(nodeId,deviceType,host,obj);
	} else {
		setLogFilter(null,null,null,null);
	}
	
}
/*******过滤条件*******/
function setLogFilter(nodeId, deviceType, host,filter){
	if(filter.indexOf("null_") != -1) {
		filter = "";
	}
	log_url =  "/sim/logMonitor/getLogData?nodeId="+nodeId+"&deviceType="+deviceType+"&host="+host;
	$.ajax({
		url:'/sim/logMonitor/setFilter?nodeId='+nodeId+'&deviceType='+deviceType+"&host="+host+"&filter="+filter,
		type:'post',
		dataType:'json',
		async : false,
		success : function() {
		}
	});
}
//当鼠标移上单元格时，显示详细内容
function showOriginalLogDetails(uuid, el) {}