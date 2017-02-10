var report = {
};

var servertime = simHandler.serverTime; 	

/**
 * 单击左侧树事件(报表查询)
 * @param node 节点信息
 */
report.onClickHandler = function (node){
	$.ajax({
        url: '/sim/report/userReportRole',
        dataType:'json',
        success: function(data) {
        	var topoReport=false;
        	if(simHandler.reportParams!=null){
        		topoReport=true;
        	}
        	if(node.attributes.type=="BRANCH"){
        		var params =null;
        		var dvcAddressipnode=$("#dvcAddress");
        		var ipAndNodeId=null;
        		if(data.deviceIpList!=null&&data.deviceIpList.length>0){
        			if(undefined!=dvcAddressipnode.val() && null!=dvcAddressipnode.val()
    						&& dvcAddressipnode.val().length>2 
    						&& dvcAddressipnode.val().indexOf(node.attributes.dvcType)!=-1){
        				ipAndNodeId=dvcAddressipnode.val().split(",");
        			}else{
        				var isRun=false;
        				for(var i=0;i<data.deviceIpList.length;i++){
        					if(data.deviceIpList[i].indexOf(node.attributes.dvcType)!=-1||
        							data.deviceIpList[i].indexOf("onlyByDvctype")!=-1){
        						ipAndNodeId=data.deviceIpList[i].split("AddAuditorNodeID");
        						isRun=true;
        						break;
            				}
        				}
        				if(!isRun){
        					ipAndNodeId=data.deviceIpList[0].split("AddAuditorNodeID");
        				}
        			}
        		}
        		if("Esm/Topsec/SystemRunLog"==node.attributes.dvcType
        				||"Esm/Topsec/SystemLog"==node.attributes.dvcType
        				||"Esm/Topsec/SimEvent"==node.attributes.dvcType
        				||"Log/Global/Detail"==node.attributes.dvcType){
        			if(!data.isOperator&&data.hasOpratorRole){
        				if(ipAndNodeId!=null&&ipAndNodeId.length>0){
            				if(ipAndNodeId[1]=='全部'||ipAndNodeId[1]=='onlyByDvctype'||"Log/Global/Detail"==node.attributes.dvcType){
                				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&viewItem="+node.attributes.viewItem+"&onlyByDvctype=onlyByDvctype&screenWidth="+screen.availWidth;
                			}else{
                				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&screenWidth="+screen.availWidth;
                    			if(topoReport){
                    				params+="&dvcaddress="+simHandler.reportParams.deviceIp+"&nodeId="+simHandler.reportParams.nodeId;
                    			}else{
                    				params+="&dvcaddress="+ipAndNodeId[1]+"&nodeId="+ipAndNodeId[0];
                    			}
                			}
            			}else{
            				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&viewItem="+node.attributes.viewItem+"&onlyByDvctype=onlyByDvctype&screenWidth="+screen.availWidth;
            			}
        			}else{
        				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&viewItem="+node.attributes.viewItem+"&onlyByDvctype=onlyByDvctype&screenWidth="+screen.availWidth;
        			}
        		}else if(data.isOperator){
        			if(ipAndNodeId!=null&&ipAndNodeId.length>0){
        				if(ipAndNodeId[1]=='全部'||ipAndNodeId[1]=='onlyByDvctype'||"Log/Global/Detail"==node.attributes.dvcType){
            				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&viewItem="+node.attributes.viewItem+"&onlyByDvctype=onlyByDvctype&screenWidth="+screen.availWidth;
            			}else{
            				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&screenWidth="+screen.availWidth;
                			if(topoReport){
                				params+="&dvcaddress="+simHandler.reportParams.deviceIp+"&nodeId="+simHandler.reportParams.nodeId;
                			}else{
                				params+="&dvcaddress="+ipAndNodeId[1]+"&nodeId="+ipAndNodeId[0];
                			}
            			}
        			}else{
        				params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&viewItem="+node.attributes.viewItem+"&onlyByDvctype=onlyByDvctype&screenWidth="+screen.availWidth;
        			}
        		}else if(data.hasOpratorRole){
        			params = "dvctype="+node.attributes.dvcType+"&mstrptid="+node.id+"&screenWidth="+screen.availWidth;
        			if(topoReport){
        				params+="&dvcaddress="+simHandler.reportParams.deviceIp+"&nodeId="+simHandler.reportParams.nodeId;
        			}else{
        				params+="&dvcaddress="+ipAndNodeId[1]+"&nodeId="+ipAndNodeId[0];
        			}
        		}
        		var talStartTimeobj=$("#talStartTime");
        		var talEndTimeobj=$("#talEndTime");
        		if(undefined!=talStartTimeobj.val()
						&& null!=talStartTimeobj.val()
						&& undefined!=talEndTimeobj.val()
						&& null!=talEndTimeobj.val()){
    				params+="&talStartTime="+talStartTimeobj.val()+"&talEndTime="+talEndTimeobj.val();
    			}
    			if(undefined!=$("#talTop").val()
						&& null!=$("#talTop").val()){
    				params+="&talTop="+$("#talTop").val();
    			}
    			params+="&reportType=baseReport";
        		$('#report-right').panel('refresh',"/sim/report/reportQuery?"+params);
        		topoReport=false;
        		simHandler.reportParams=null;
        	}/*else if(node.attributes.type=="TRUNK" 
        			&& ("Firewall/TOPSEC/TOS/V005"==node.attributes.dvcType
    				||"OS/Microsoft/WindowsEventLog"==node.attributes.dvcType
    				||"Esm/Topsec/SystemRunLog"==node.attributes.dvcType
    				||"Web/Microsoft/IIS FTP"==node.attributes.dvcType
    				||"Switch"==node.attributes.dvcType.substring(0, node.attributes.dvcType.indexOf('/', 0)))){
        		var params =null;
        		
        		params = "dvctype="+node.attributes.dvcType+"&screenWidth="+screen.availWidth+"&reportType=baseReport";
        		var talStartTimeobj=$("#talStartTime");
        		var talEndTimeobj=$("#talEndTime");
        		if(undefined!=talStartTimeobj.val()
						&& null!=talStartTimeobj.val()
						&& undefined!=talEndTimeobj.val()
						&& null!=talEndTimeobj.val()){
    				params+="&talStartTime="+talStartTimeobj.val()+"&talEndTime="+talEndTimeobj.val();
    			}
    			if(undefined!=$("#talTop").val()
						&& null!=$("#talTop").val()){
    				params+="&talTop="+$("#talTop").val();
    			}
    			params+="&reportType=baseReport";
        		$('#report-right').panel('refresh',"/sim/topoReport/typeComprehensiveReport?"+params);
        		topoReport=false;
        		simHandler.reportParams=null;
        	}*/else{
        		showAlertMessage("无此类型报表!");
        	}
        }
    });
	
};

report.moreReport = function(url){
	$('#report-right').panel('refresh',encodeURI(url)+"&screenWidth="+screen.availWidth);
};

/**
 * 下钻报表
 * @param url url地址
 */
report.drillReport = function(url){
	if(url.indexOf('%')<1){
		url=encodeURI(url);
	}
	$('#report-right').panel('refresh',url+"&screenWidth="+screen.availWidth);
};
report.formatReportQueryObj=function(queryObj){
	if(queryObj.conditionName instanceof Array){
		var conditionName=queryObj.conditionName[0]+'';
		var queryContent=queryObj.queryContent[0]+'';
		var operator=queryObj.operator[0]+'';
		var queryType=queryObj.queryType[0]+'';
		
		while(conditionName.indexOf("***", 0)>0){
			conditionName=conditionName.replace("***", ",");
			queryContent=queryContent.replace("***", ",");
			operator=operator.replace("***", ",");
			queryType=queryType.replace("***", ",");
		}
		queryObj.conditionName=conditionName;
		queryObj.queryContent=queryContent;
		queryObj.operator=operator;
		queryObj.queryType=queryType;
	}
};
report.formatReportLogQueryObj=function(logQueryObj){
	var dvcAddressipnode=$("#dvcAddress");
	var ipAndNodeId=null;
	if(null!=dvcAddressipnode.val()
			&& dvcAddressipnode.val().length>2){
		ipAndNodeId=dvcAddressipnode.val().split(",");
	}
	if(ipAndNodeId!=null && 
			(ipAndNodeId[1]=='全部'||ipAndNodeId[1]=='onlyByDvctype')){
		logQueryObj.host="";
	}
	report.formatReportQueryObj(logQueryObj);
	var objParams='reportQueryLog=reportQueryLog';
	for(var pro in logQueryObj){
		if (typeof(logQueryObj[pro])== "function"){
			logQueryObj[pro](); 
		} else { // p 为属性名称，obj[p]为对应属性的值 
			objParams+='&'+pro+'='+encodeURI(logQueryObj[pro]);
		}
	}
	/*if(objParams.length>1){
		objParams=objParams.substring(1);
	}*/
	return objParams;
};
report.reportLogQuery=function(url,logQueryObj){
	report.formatReportLogQueryObj(logQueryObj);
	simHandler.logQueryObject=logQueryObj;
	simHandler.onClickMenuTp('menu_log_query','/page/log/logQuery2Main.html');
};

report.reportEventQuery=function(url,eventQueryObject){
	if(eventQueryObject == null){
		return;
	}
	report.formatReportQueryObj(eventQueryObject);
	eventQueryObject=report.formatReportEventQueryObj(eventQueryObject);
	if(null != eventQueryObject){
		report.showReportQueryEvent(url,eventQueryObject);
	}
};

report.constructorEventQobj=function(){
	var eventQueryObject={};
	eventQueryObject.ip=null;
	eventQueryObject.deviceIp=null;
	eventQueryObject.deviceType=null;
	eventQueryObject.eventType=null;
	eventQueryObject.eventName=null;
	eventQueryObject.srcIp=null;
	eventQueryObject.destIp=null;
	eventQueryObject.srcPort=null;
	eventQueryObject.destPort=null;
	eventQueryObject.category1=null;
	eventQueryObject.category2=null;
	eventQueryObject.page=null;
	eventQueryObject.rows=null;
	eventQueryObject.fields=null;
	eventQueryObject.header=null;
	eventQueryObject.protocol=null;
	eventQueryObject.startTime=null;
	eventQueryObject.requestIp=null;
	eventQueryObject.endTime=null;
	eventQueryObject.priority=null;
	eventQueryObject.ruleName=null;
	eventQueryObject.query_event_Name=null;
	return eventQueryObject;
};

report.formatReportEventQueryObj=function(paramsObject){
	if(null == paramsObject){return paramsObject;}
	var conditionName=paramsObject.conditionName+'';
	var queryContent=paramsObject.queryContent+'';
	var paramsObjs={};
	if(conditionName.indexOf(',')>0){
		var conditionnameArr=conditionName.split(',');
		var contentArr=queryContent.split(',');
		for(var i=0;i<conditionnameArr.length;i++){
			paramsObjs[conditionnameArr[i]]=contentArr[i];
		}
	}else{
		paramsObjs[conditionName]=queryContent;
	}
	var eventQueryObject={};
	for(var pro in paramsObjs){
		if(pro === "NAME"){
			eventQueryObject.query_event_Name=paramsObjs[pro];
		}else if(pro === "SRC_ADDRESS"){ 
			eventQueryObject.srcIp=paramsObjs[pro];
		}else if(pro === "DVC_ADDRESS"){
			eventQueryObject.deviceIp=paramsObjs[pro];
		}else if(pro === "DEST_ADDRESS"){
			eventQueryObject.destIp=paramsObjs[pro];
		}else if(pro === "PRIORITY"){
			eventQueryObject.priority=paramsObjs[pro];
		}else if(pro === "CAT1_ID"){
			eventQueryObject.category1=paramsObjs[pro];
		}else if(pro === "CAT2_ID"){
			eventQueryObject.category2=paramsObjs[pro];
		}else if(pro === "endTime"){
			eventQueryObject.endTime=paramsObjs[pro];
		}
	}
	eventQueryObject.startTime=paramsObject.queryStartDate;
	eventQueryObject.endTime=paramsObject.queryEndDate;
	return eventQueryObject;
};

report.showReportQueryEvent=function(queryUrl,params){
	if(queryUrl == null || params == null){
		return;
	}
	$('<div/>').dialog({
		title : '报表事件详情',
		closable:true,
        width: 800,
        height: 500,
		modal : true,
		cache : false,
		href : '/page/report/reportQueryEvent.html',
		onClose : function() {
			$(this).dialog('destroy');
		},
		onLoad: function() {
			$("#report2_event_table").datagrid({
				url:queryUrl,
				queryParams: params
			});
		}
	});
};

report.exportReport = function (url,type,mstId,sTime,eTime,top){
	var _sTime = $("#talStartTime").val();
	var _eTime = $("#talEndTime").val();
	var _top = $("#talTop").val();
	if(_sTime==null||_sTime==""){
		_sTime = sTime;
	}
	if(_eTime==null||_eTime==""){
		_eTime = eTime;
	}
	if(_top==null||_top==""){
		_top = top;
	}
	var node = $("#rptTree").tree("getSelected");
	$.messager.defaults = { ok: "更多数据", cancel: "当前数据" };
	$.messager.confirm('导出提示', '需要 导出的 数据提示', function(r) {
		if(r){
			_top=50;
		}
		url+="&exprpt="+type+"&mstrptid="+mstId+"&talTop="+_top+"&talStartTime="+_sTime+"&talEndTime="+_eTime+"&viewItem="+node.attributes.viewItem;
		window.open(encodeURI(url)) ;
	});
	$.messager.defaults = { ok: "确定", cancel: "取消" };
};
 
/**
 * 返回
 * @param url url地址
 */
report.goBack = function(url){
	url=report.formatAssReportUrl(url);
	$('#report-right').panel('refresh',encodeURI(url)+"&screenWidth="+screen.availWidth);
};
report.formatAssReportUrl = function(url){
	var newurl=url;
	var ass_rpt_tree=$("#assetevt_tree_id") ;
	var node = ass_rpt_tree.tree("getSelected");
	if(undefined ==node || node ==null){
		return newurl;
	}
	var parent = ass_rpt_tree.tree("getParent",node.target) ;
	var nodeLevel="";
	try{
		topoId=node.attributes.topoId;
		nodeLevel=node.attributes.nodeLevel;
	}catch(e){topoId="";nodeLevel="";}
	if(nodeLevel=="0"){
		return;
	}
	while(null!=parent){
		var parentpar = ass_rpt_tree.tree("getParent",parent.target) ;
		if(null==parentpar){
			break;
		}
		parent=parentpar;
	}
	if(null==parent){
		parent=node;
	}
	if(url.indexOf("/sim/topoReport/reportQuery?")>-1){
		var dvcpo=url.indexOf('dvctype=',0);
		var andpo=-1;
		if(dvcpo>-1){
			andpo=url.indexOf('&',dvcpo);
			if(andpo==-1){andpo=url.length;}
		}
		
		if(node.type=="TRUNK"){
			if(andpo != -1){
				newurl=url.substring(0,url.indexOf('?')+1)+'dvctype=Profession/Group'+url.substring(andpo,url.length);
			}
			var mstidpo=newurl.indexOf('mstrptid=',0);
			var andmstpo=-1;
			if(mstidpo>-1){
				andmstpo=newurl.indexOf('&',mstidpo);
				if(andmstpo==-1){andmstpo=newurl.length;}
			}
			if(andmstpo != -1){
				newurl=newurl.substring(0,mstidpo+1)+'mstrptid=10000'+newurl.substring(andmstpo,newurl.length);
			}
		}else if(node.type=="LEAF"){
			if(andpo != -1){
				newurl=url.substring(0,url.indexOf('?')+1)+'dvctype=Profession/Group/Asset'+url.substring(andpo,url.length);
			}
			
			var mstidpo=newurl.indexOf('mstrptid=',0);
			var andmstpo=-1;
			if(mstidpo>-1){
				andmstpo=newurl.indexOf('&',mstidpo);
				if(andmstpo==-1){andmstpo=newurl.length;}
			}
			if(andmstpo != -1){
				newurl=newurl.substring(0,mstidpo)+'mstrptid=10001'+newurl.substring(andmstpo,newurl.length);
			}
		}
	}
	return newurl;
};
/**
 * 查询
 * @param isDrill 是否下钻主题
 * @param talCategory 下钻字段连接串
 * @param mstId 大主题Id
 */
report.query = function(talCategory,mstId){
	var sTime = $('#talStartTime').val();
	var eTime = $('#talEndTime').val();
	var top = $('#talTop').val();
	var dvcAddress=null;
	if(!(undefined==$("#dvcAddress").val()||null==$("#dvcAddress").val())){
		dvcAddress = $("#dvcAddress").val().split(",");
	}
	
	var node = $("#rptTree").tree("getSelected");
	if(node.attributes.type=="BRANCH"){
		var params = "/sim/report/reportQuery?dvctype="+node.attributes.dvcType+"&mstrptid="+mstId+"&talStartTime="+sTime+"&talEndTime="+eTime+"&talTop="+top+"&screenWidth="+screen.availWidth;
		if(!(undefined==dvcAddress||null==dvcAddress||dvcAddress.length<1)){
			if(dvcAddress[1]=="全部"||dvcAddress[1]=='onlyByDvctype'){
				params+="&onlyByDvctype=onlyByDvctype";
			}else{
				params+="&dvcaddress="+dvcAddress[1]+"&nodeId="+dvcAddress[0];
			}
		}
		if(undefined!=talCategory && talCategory!=null){
			var talCategorys = talCategory.split(",");
			for(var i=0,len=talCategorys.length;i<len;i++){
				params+="&talCategory="+talCategorys[i];
			}
		}
		params+="&reportType=baseReport";
		$('#report-right').panel('refresh',encodeURI(params));
	}/*else if(node.attributes.type=="TRUNK" 
		&& ("Firewall/TOPSEC/TOS/V005"==node.attributes.dvcType
				||"OS/Microsoft/WindowsEventLog"==node.attributes.dvcType
				||"Esm/Topsec/SystemRunLog"==node.attributes.dvcType
				||"Web/Microsoft/IIS FTP"==node.attributes.dvcType
				||"Switch"==node.attributes.dvcType.substring(0, node.attributes.dvcType.indexOf('/', 0)))){
    		var params =null;
    		
    		params = "dvctype="+node.attributes.dvcType+"&screenWidth="+screen.availWidth+"&reportType=baseReport";
    		var talStartTimeobj=$("#talStartTime");
    		var talEndTimeobj=$("#talEndTime");
    		if(undefined!=talStartTimeobj.val()
					&& null!=talStartTimeobj.val()
					&& undefined!=talEndTimeobj.val()
					&& null!=talEndTimeobj.val()){
				params+="&talStartTime="+talStartTimeobj.val()+"&talEndTime="+talEndTimeobj.val();
			}
			if(undefined!=$("#talTop").val()
					&& null!=$("#talTop").val()){
				params+="&talTop="+$("#talTop").val();
			}
			params+="&reportType=baseReport";
    		$('#report-right').panel('refresh',"/sim/topoReport/typeComprehensiveReport?"+params);
    		topoReport=false;
    		simHandler.reportParams=null;
    	}*/
	
	dvcAddress=null;
} ;

/**
 * 查询
 * @param isDrill 是否下钻主题
 * @param talCategory 下钻字段连接串
 * @param mstId 大主题Id
 */
report.moreRptQuery= function(url){
	var data = $("#form_more_report").serialize() ;
	$.post(encodeURI(url),null,function(data){
		
	},"json") ;
} ;

report.rowDataFormatter = function(value,row,index){
	return "<a href='www.baidu.com'>"+value+"</a>" ;
};

/**
 * 隐藏或显示表格信息
 * @param obj 组件ID
 * @td tb 表格ID
 */
report.viewCmd = function(obj,tb){
	if($('#'+tb).css('display')!='none'){
		$('#'+tb).css('display', 'none');
		$(obj).html('显示');
	}else{
		$('#'+tb).css('display', '');
		$(obj).html('隐藏');
	}
};

report.isShowParamItems = function(viewItem){
	if(viewItem=="1,2,3"){
		$("#paramItems").css("display", "");
	}else{
		$("#paramItems").css("display", "none");
	}
};
report.treeOnSelectNode=function(node){
	var isLeaf = $('#rptTree').tree('isLeaf',node.target);
	if(!isLeaf){
		return;
	}else{
		
	}
};
report.treeOnLoadSuccess=function(node, data){
	if(simHandler.reportParams!=null){
		var rpt_tree = $("#rptTree") ;
		//获取根节点
		var selectNode = rpt_tree.tree("find",simHandler.reportParams.securityObjectType) ;
		var children = rpt_tree.tree("getChildren",selectNode.target) ;
		/* 选中第一个子节点 */  
		//根据根节点获取子节点
		rpt_tree.tree("select",children[0].target);
		rpt_tree.tree('expand',selectNode.target);
	}
};


report.init = function(){
	//初始化日期范围选择器
	var datepicker = $('#receipt_time') ;
	if(datepicker.length > 0){
		datepicker.daterangepicker(optionSet, cb);	
	}
	report.initmodule();
};
function stopBubble(e){
	// 如果传入了事件对象，那么就是非ie浏览器
	if(e&&e.stopPropagation){
		//因此它支持W3C的stopPropagation()方法
		e.stopPropagation();
	}else{
		//否则我们使用ie的方法来取消事件冒泡
		window.event.cancelBubble = true;
	}
}

report.deletePro=function(value){
	for(var pro in value){
		delete value[pro];
		if(typeof(value[pro])=="function"){
			value[pro]=function(){
				return false;
			};
		}
	}
};
report.initmodule=function(){
	/*try{
		$("#talTop").combobox({
			height:24,
			editable:false,
			panelHeight:120
		});
	}catch(e){}*/
};
/**
 * 当改变日志接收时间，则重新加载日志列表
 */
function onChangeReceiptTime(){
	var searchParams = {};
	searchParams.pageNo = 1;
	searchParams.queryStartDate = $('#talStartTime').val();
	searchParams.queryEndDate = $('#talEndTime').val();
}

//日期范围选择器回调函数	
var cb = function(start, end) {
    $('#talStartTime').val(start.format('YYYY-MM-DD HH:mm:ss'));
    $('#talEndTime').val(end.format('YYYY-MM-DD HH:mm:ss'));
    onChangeReceiptTime();
};	

//日期范围选择器初始化参数
var optionSet = {
    startDate: moment(servertime).subtract('day', 1),
    endDate: moment(servertime),
    maxDate: false,
    minDate: false,
    showDropdowns:true,
    timePicker: true,//显示时间选择器
    timePickerIncrement: 1,//时间间隔5分钟
    timePicker12Hour: false,//12小时制
    locale: locales['zh-CN'],
    format: 'YYYY-MM-DD HH:mm:ss',
    opens: 'left',
    ranges: {
       '今天': [moment(servertime).startOf('day'), moment(servertime)],
       '昨天': [moment(servertime).subtract('days', 1).startOf('day'), moment(servertime).subtract('days', 1).endOf('day')],
       '最近一小时': [moment(servertime).subtract('hours',1), moment(servertime)],
       '最近一周': [moment(servertime).subtract('days',7), moment(servertime)],
       '最近一月': [moment(servertime).subtract('month',1), moment(servertime)]
    }
};

$(function(){
	report.init() ;
}) ;