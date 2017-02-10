/**
 * @author wza
 */
// roleIds : 53 operators ,52 auditors ,51 admins
var superiorreport = {};
var roles={};
var servertime = moment(simHandler.serverTime).set("minute",0).set("second",0).set("millisecond",0).toDate();
var queryConditions={};
queryConditions.pageIndex=1;
queryConditions.pageSize=30;
var isInitSub=false;
var isInitMonitorSub=false;
superiorreport.treeType=0;

superiorreport.setQueryCondition=function(node){
	if(null != $('#loadput').val() ){
		superiorreport.subQueryCondition();
		isInitSub=true;
	}
	queryConditions.securityObjectType=node.attributes.securityObjectType;
	queryConditions.parentIds=[node.id];
	return queryConditions;
};

superiorreport.setMonitorQueryCondition=function(node){
	if(null != $('#loadput').val() ){
		superiorreport.subMonitorQueryCondition();
		isInitMonitorSub=true;
	}
	var nodeParent = $('#showMonitorTree').tree("getParent",node.target) ;
	queryConditions.parentIds=[nodeParent.id];
	queryConditions.securityObjectType=node.attributes.securityObjectType;
	queryConditions.dvcAddress=node.id;
	queryConditions.resourceId=node.attributes.resourceId;
	var resourceIdParams='RESOURCE_ID='+queryConditions.resourceId;
	var dvcIpParams='DVC_ADDRESS='+node.id;
    
	queryConditions.params=dvcIpParams+';'+resourceIdParams;
	
	return queryConditions;
};
superiorreport.subMonitorQueryCondition=function(){
	queryConditions.stime=$('#stime').val();
	queryConditions.endtime=$('#etime').val();
	queryConditions.queryType='MONITOR';
	return queryConditions;
};

superiorreport.subQueryCondition=function(){
	queryConditions.queryType='LOG';
	queryConditions.dvcAddress=$('#dvcAddress').val();
	queryConditions.stime=$('#stime').val();
	queryConditions.endtime=$('#etime').val();
	queryConditions.topn=$('#topn').val();
	var dvcOptions=$('#dvcAddress option');
	var times = 1;
	if( ('ALL_ROLE_ADDRESS' == queryConditions.dvcAddress || 'ONLY_BY_DVCTYPE' == queryConditions.dvcAddress) 
			&& (undefined != dvcOptions || dvcOptions.length<2) ){
		times =dvcOptions.length-1;
	}  
	queryConditions.pageSize=queryConditions.topn *times;
	var resourceIds=[];
	var resourceIdParams='RESOURCE_ID=';
	var dvcIps = [];
	var dvcIpParams='DVC_ADDRESS=';
	dvcOptions.each(function(i) {
    	dvcIps[i] = $(this).val();
    	var resourceId=$(this)[0].resourceId;
    	resourceIds[i]=resourceId;
    	if(true == $(this)[0].selected){queryConditions.resourceId=resourceId;}
// $(this).text();
    });
    for(var i=0;i<dvcIps.length;i++){
    	if('ALL_ROLE_ADDRESS' !=dvcIps[i] && 'ONLY_BY_DVCTYPE'!= dvcIps[i]){
    		dvcIpParams+=dvcIps[i]+',';
    		resourceIdParams+=resourceIds[i]+',';
    	}
	}
    /** 此处是事件报表的类型，因为事件报表没有设备地址，这么处理可能会引进BUG */
    if(1 == dvcIps.length && 'ONLY_BY_DVCTYPE'== dvcIps[0]){
    	dvcIpParams+='127.0.0.1,';
    }
    /** 事件报表 的de设备地址 结束。 */
    if(dvcIpParams.length>1){
    	dvcIpParams=dvcIpParams.substring(0, dvcIpParams.length-1);
    	resourceIdParams=resourceIdParams.substring(0, resourceIdParams.length-1);
    }
	queryConditions.params=dvcIpParams+';'+resourceIdParams;
	return queryConditions;
};
/**
 * 适用于异步取某个正加载的值
 * 
 * @param val
 */
superiorreport.loadsuccess=function(val,func){
	if(null == val){
		setTimeout(superiorreport.loadsuccess(val,func),100);
	}else{
		func(val);
	}
};

superiorreport.treeLoadSuccess=function(){
	if(simHandler.reportParams!=null){
		var show_tree = $("#showTree") ;
		// 获取根节点
		var selectNode = show_tree.tree("find",simHandler.reportParams.securityObjectType) ;
		var children = show_tree.tree("getChildren",selectNode.target) ;
		/* 选中第一个子节点 */  
		// 根据根节点获取子节点
		show_tree.tree("select",children[0].target);
		show_tree.tree('expand',selectNode.target);
	}
};
/**
 * 
 */
superiorreport.clickNode = function (node){
	if(null == node)return;
	superiorreport.treeType=1;
	if(node.attributes.type=="BRANCH"){
		
		if(null == $('#loadput').val() ){
			superiorreport.loadQueryPage(node,'basic');
// superiorreport.authroles(node,"#showTree");
		}
		$("#dvc_div").css('display','inline');
		$("#top_div").css('display','inline');
		if(isChangeConditions(node)){
			return;
		}
    	if(null !=$('#loadput').val()){
    		superiorreport.ajaxRequestFunc(node);
    	}
    	
	}
};

superiorreport.ajaxRequestFunc=function(node){
	var isSameType = isChangeSecurityObjectType(node);
	superiorreport.setQueryCondition(node);
	var subscontent=$('#subscontent').empty().append($('<div/>').html("正在加载..."));
	$.ajax({
        url: '/sim/reportRest/reportQuery?ip='+superiorreport.targetIp,
        type: 'POST',
        dataType:'json',
        data: JSON.stringify(queryConditions),
        contentType:"text/javascript",
        success: function(jsondata){
        	
        	superiorreport.valPageProperty(jsondata);
        	if(!isInitSub || !isSameType){
        		superiorreport.loadsuccess($('#dvcAddress').val(),superiorreport.subQueryCondition);
        	}
        	var subThemes=jsondata.result;
        	subscontent.empty();
        	
        	var len=subThemes.length;
        	var isj=1==(len%2);
        	$.each(subThemes,function(i,subjectData){
        		queryConditions.parentSubId=subjectData.parentSubId;
        		if("TREND" == subjectData.showType){
        			queryConditions.pageSize=280;
        		}
        		
        		var subjectDivId='subject'+subjectData.parentSubId;
        		var subjectDiv=null;
        		if(isj && i==len-1 || 'BIG_IMAGE' == subjectData.userShow){
        			subjectDiv=$('<div id="'+subjectDivId+'" style="height: 440px;" class="subchartdiv"/>');
        		}else{
        			subjectDiv=$('<div id="'+subjectDivId+'" style="height: 440px;" class="subchartdiv-sm"/>');
        		}
            	subscontent.append(subjectDiv);
            	
            	var ajaxfunc=function(){
            		$.ajax({
                        url: '/sim/reportRest/subThemeData?ip='+superiorreport.targetIp,
                        async: true,
                        type: 'POST',
// data: queryConditions,
                        dataType:'json',
                        data: JSON.stringify(queryConditions),
                        contentType:"text/javascript",
                        success: function(subjson){
                        	var topn=$('#topn').val();
                        	var subReportName=subjectData.subReportName;
            	        	superiorreport.createSubject(subjson,jsondata,topn,subReportName,subjectDivId) ;
            	        }
            	    });
            		
            	};
// ajaxfunc();
            	superiorreport.loadsuccess($('#dvcAddress').val(),ajaxfunc);
        	});

        }
    });
	
};
superiorreport.createSubject = function(subjson,jsondata,topn,subReportName,subjectDivId){
	if(null == subjson.data[0])return;
// if(0==subjson.data[0].length)return;
	subjson.roleDs=jsondata.roleDs;
	var needReGroup=subjson.needReGroup;
	var showType=subjson.showType;
	var colon=showType.indexOf(':');
	var recommend=null;
	if(colon>-1){
		recommend=showType.substring(showType.indexOf('(')+1, showType.indexOf(')') );
		showType=showType.substring(0,colon);
	}
	var option=null;
	if('true' ==needReGroup){
		if('TREND' == showType){
    		recommend=(recommend==null)?'line':recommend;
		}else if('NOT_TREND' == showType){
    		recommend=(recommend==null)?'bar':recommend;
    		recommend=recommend.replace('standard', '');
			recommend=recommend.replace('rainbow', '');
    	}
		option=echartshow.groupPieBarOption(subReportName,subjson,topn,recommend);
	}else{
		if('TREND' == showType){
    		recommend = recommend == null ? 'line' : recommend;
    		option=echartshow.standardOption(subReportName,subjson,subjson.data[0].length+1,'line');
    	}else if('NOT_TREND' == showType){
    		recommend=(recommend==null)?'bar':recommend;
    		switch(recommend){
    		case 'standardbar':
    			option=echartshow.standardOption(subReportName,subjson,topn,'bar');
    			break;
    		case 'standardline':
    			option=echartshow.standardOption(subReportName,subjson,topn,'line');
    			break;
    		case 'pie':
    			option=echartshow.pieOption(subReportName,subjson,topn);
    			break;
    		case 'rainbow':
    			option=echartshow.rainbowOption(subReportName,subjson,topn,'bar');
    			break;
    		case 'eventRiver':
    			option=echartshow.eventRiverOption(subReportName,subjson,topn,'eventRiver');
    			break;
    		default:
    			recommend=recommend.replace('standard', '');
    			recommend=recommend.replace('rainbow', '');
    			option=echartshow.rainbowOption(subReportName,subjson,topn,recommend);
    		}
    		
    	}else{
    		option=echartshow.rainbowOption(subReportName,subjson,topn,'bar');
    	}
	}
	
	try{
		echartshow.init(subjectDivId,option);
	}catch(e){}
};
superiorreport.treeMonitorLoadSuccess=function(){
	
};

superiorreport.clickMonitorNode=function(node){
	superiorreport.treeType=2;
	if(node.attributes.type=="BRANCH"){
		
		if(null == $('#loadput').val() ){
			superiorreport.loadQueryPage(node,'monitor');
// superiorreport.authroles(node,"#showMonitorTree");
		}
		$("#dvc_div").css('display','none');
		$("#top_div").css('display','none');
		if(isChangeMonitorConditions(node)){
			return;
		}
    	if(null !=$('#loadput').val()){
    		superiorreport.ajaxMonitorFunc(node);
    	}
    	
	}
};

superiorreport.ajaxMonitorFunc=function(node){
	
	var isSameType=true;
	if(!isChangeSecurityObjectType(node)){
		isSameType=false;
	}
	superiorreport.setMonitorQueryCondition(node);
	var subscontent=$('#subscontent');
	
	subscontent.empty();
	var divload=$('<div/>');
	divload.html("正在加载...");
	subscontent.append(divload);
	
	$.ajax({
        url: '/sim/reportRest/reportQuery',
        type: 'POST',
        data: queryConditions,
        dataType:'json',
// data: JSON.stringify(queryConditions),
// contentType:"text/javascript",
        success: function(jsondata){
        	
        	superiorreport.valMonitorPageProperty(jsondata);
        	
        	if(! isInitSub || !isSameType){
        		superiorreport.loadsuccess($('#stime').val(),superiorreport.subMonitorQueryCondition);
        	}
        	
        	var subThemes=jsondata.result;
        	subscontent.empty();
        	
        	var len=subThemes.length;
        	var isj=1==(len%2);
        	$.each(subThemes,function(i,subjectData){
        		queryConditions.parentSubId=subjectData.parentSubId;
        		if("TREND" ==subThemes[i].showType){
        			queryConditions.pageSize=280;
        		}
        		
        		var subjectDivId='subject'+subjectData.parentSubId;
        		var subjectDiv=null;
        		if(isj && i==len-1){
        			subjectDiv=$('<div id="'+subjectDivId+'" class="subchartdiv"/>');
        		}else{
        			subjectDiv=$('<div id="'+subjectDivId+'" class="subchartdiv-sm"/>');
        		}
            	subscontent.append(subjectDiv);
            	
            	var ajaxMonitfunc=function(){
            		$.ajax({
                        url: '/sim/reportRest/subThemeData?ip='+superiorreport.targetIp,
                        async: true,
                        type: 'POST',
// data: queryConditions,
                        data: JSON.stringify(queryConditions),
                        dataType:'json',
                        contentType:"text/javascript",
                        success: function(subjson){
                        	var topn=999;
                        	var subReportName=subjectData.subReportName;
            	        	superiorreport.createSubject(subjson,jsondata,topn,subReportName,subjectDivId) ;
            	        }
            	    });
            		
            	};
            	superiorreport.loadsuccess($('#stime').val(),ajaxMonitfunc);
        	}
        ); 
        }
	});
};

// superiorreport.exportReport=function(type){
// if(undefined == type)return;
// queryConditions.exportFormat=type;
// queryConditions.pageSize=queryConditions.topn ;
// var pars='';
// for(var pro in queryConditions){
// pars+=pro+'='+queryConditions[pro]+'&';
// }
// var strUrl='/sim/basicreport/exportReport?'+pars;
// $('#page-body iframe').remove();
//
// var iframe = $("<iframe>");
// iframe.attr('src', strUrl);
// iframe.attr('style', 'display:none');
// $('#page-body').append(iframe);
// };

superiorreport.drillReport=function(url,conditionObject){
	$('<div/>').dialog({
		title : '下钻主题内容',
		closable:true,
        width: 800,
        height: 500,
		modal : true,
		cache : false,
		href : '/page/newreport/chooseStatistic.html',
		onClose : function() {
			$(this).dialog('destroy');
		},
		onLoad: function() {
			var bizField=$('#bizField');
			bizField.combotree( {  
			    // 获取数据URL
			    url : '/sim/basicreport/logQueryColumnLevel?securityObjectType='+queryConditions.securityObjectType,
			    onSelect : function(node) {  
			        var tree = $(this).tree;  
			        var isLeaf = tree('isLeaf', node.target);
			        if (!isLeaf) {  
			            // 清除选中
			        	bizField.combotree('clear');
			        }
			    }
			});
		}
	});
};

superiorreport.formatReportLogQueryObj=function(logQueryObj){
	var objParams='reportQueryLog=reportQueryLog';
	for(var pro in logQueryObj){
		if (typeof(logQueryObj[pro])== "function"){
			logQueryObj[pro](); 
		} else { // p 为属性名称，obj[p]为对应属性的值
			objParams+='&'+pro+'='+encodeURI(logQueryObj[pro]);
		}
	}
	return objParams;
};

superiorreport.reportStringQueryEvent=function(url,eventQueryString){
	if(eventQueryString == null){
		return;
	}
	var eventQueryObject=superiorreport.stringToEvtQueObj(eventQueryString);
	superiorreport.reportEventQuery(url,eventQueryObject);
};
superiorreport.stringToEvtQueObj=function(eventQueryString){
	var eventQueryObj={};
	var prarms=eventQueryString.split(',');
	for(var i=0;i<prarms.length;i++){
		var propArr=prarms[i].split('=');
		eventQueryObj[propArr[0]]=propArr[1];
	}
	return eventQueryObj;
};
superiorreport.reportEventQuery=function(url,eventQueryObject){
	if(eventQueryObject == null){
		return;
	}
	
	if(null != eventQueryObject){
		superiorreport.showReportQueryEvent(url,eventQueryObject);
	}
};

superiorreport.showReportQueryEvent=function(queryUrl,params){
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
		href : '/page/newreport/reportQueryEvent.html',
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

superiorreport.authroles=function (node,thistree){
	var show_tree = $(thistree) ;
	var root =show_tree.tree("getRoot",node.target) ;
	roles.roleIds=root.attributes.roleIds;
	roles.username=root.attributes.username;
	return roles;
};

superiorreport.valMonitorPageProperty=function (jsondata){
	var startQueryConditions=jsondata.queryConditions;
	$('#stime').val(startQueryConditions.stime);
	$('#etime').val(startQueryConditions.endtime);
	queryConditions.nodeIds=[jsondata.selectds.auditorNodeId];
	return true;
};

/**
 * 给页面的控件赋予初始值 时间 top 选择等
 */
superiorreport.valPageProperty=function (jsondata){
	var startQueryConditions=jsondata.queryConditions;
	var roleDs=jsondata.roleDs;
	$('#stime').val(startQueryConditions.stime);
	$('#etime').val(startQueryConditions.endtime);
	var dvcAddr=$('#dvcAddress');
	dvcAddr.empty(); 
// dvcAddr.length=0;
	for(var i=0;i<roleDs.length;i++){
// var option=$("<option
// value='"+roleDs[i].deviceIp+"'>"+roleDs[i].resourceName+"</option>");
		var option=new Option(roleDs[i].resourceName,roleDs[i].deviceIp);
		option.resourceId=roleDs[i].resourceId;
		if(null == queryConditions.nodeIds){
			queryConditions.nodeIds=[roleDs[i].auditorNodeId];
		}
		dvcAddr.append(option);
	}
	var selectds=jsondata.selectds;
	if (null != selectds){
		dvcAddr.val(selectds.deviceIp);
	}
	return true;
};

superiorreport.loadQueryPage = function(node,type){
	$('#page-right').panel({   
	    href:'/page/sysconfig/sysconfig_superiorReport_showData.html',   
	    onLoad:function(){
	    	superiorreport.initTime();
	    	if('basic' == type){
	    		superiorreport.ajaxRequestFunc(node);
	    	}else if('monitor' == type){
	    		$("#dvc_div").css('display','none');
	    		$("#top_div").css('display','none');
	    		superiorreport.ajaxMonitorFunc(node);
	    	}
	    }
	});
};

superiorreport.conditionCompareTo=function(obj1,obj2){
	if(null == obj1 || null == obj2){
		return false;
	}
	try{
		for(var pro in obj1){
			if(typeof obj1[pro] =='object'){
				for(var pro1 in obj1[pro]){
					if(obj1[pro][pro1] != obj2[pro][pro1]){
						return false;
					}
				}
			}
		}
	}catch(e){
		return false;
	}
	return true;
};

superiorreport.initTime = function(){
	// 初始化日期范围选择器
	var datepicker = $('#receipt_time') ;
	if(datepicker.length > 0){
		datepicker.daterangepicker(optionSet, cb);	
	}
};

function isChangeConditions(node){
	if(null == $('#etime').val()){
		return false;
	}
	return queryConditions.securityObjectType==node.attributes.securityObjectType &&
	queryConditions.dvcAddress==$('#dvcAddress').val() &&
	queryConditions.stime==$('#stime').val() &&
	queryConditions.endtime==$('#etime').val() &&
	queryConditions.topn==$('#topn').val() &&
	queryConditions.parentIds[0]==node.id;
}

function isChangeMonitorConditions(node){
	if(null == $('#etime').val()){
		return false;
	}
	var nodeParent = $('#showMonitorTree').tree("getParent",node.target) ;
	return queryConditions.securityObjectType==node.attributes.securityObjectType &&
	queryConditions.stime==$('#stime').val() &&
	queryConditions.endtime==$('#etime').val() &&
	queryConditions.parentIds[0]==nodeParent.id&&
	queryConditions.dvcAddress==node.id;
}

function isChangeSecurityObjectType(node){
	return queryConditions.securityObjectType==node.attributes.securityObjectType;
}

function onChangeReceiptTime(thistree){
	if(superiorreport.treeType == 2)superiorreport.clickMonitorNode($(thistree).tree('getSelected'));
	else superiorreport.clickNode($(thistree).tree('getSelected'));
}

// 日期范围选择器回调函数
var cb = function(start, end) {
    $('#stime').val(start.format('YYYY-MM-DD HH:mm:ss'));
    $('#etime').val(end.format('YYYY-MM-DD HH:mm:ss'));
    
    if(superiorreport.treeType == 2)onChangeReceiptTime('#showMonitorTree');
    else onChangeReceiptTime('#showTree');
    
};	

// 日期范围选择器初始化参数
var optionSet = {
    startDate: moment(servertime).subtract('day', 1),
    endDate: moment(servertime),
    maxDate: true,
    minDate: false,
    showDropdowns:true,
    timePicker: true,// 显示时间选择器
    timePickerIncrement: 60,// 时间间隔5分钟
    timePicker12Hour: false,// 12小时制
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
function makeSimulationRejson(){
	var rejson={};
	rejson.dataStructureDesc = '{categorys:{INIT~初始~TEXT},series:{INIT~初始~TEXT},statistical:{INITNO~初始数~COUNT_NO}}';
	rejson.queryCondition = 'INITNO$INIT=?~String#SHARE@GROUP=?';
	rejson.data = [[]];
	rejson.showType = 'NOT_TREND';
	rejson.describe = '初始';
	rejson.queryConditionsObj = null;
	rejson.queryType = 'NO_QUERY';
	return rejson;
}