/**
 * @author:WangZhiai
 */
var type="config.schedule";
var stype="config.schedule.report";
treeNode='0';
reportinfo='';
treeNodeIdVal='0';
rowedit='';
/*计划报表命名空间*/
var planReportObj = {};
var planReportFunc={};
function getReportTypeVal(){
	try{
		return $('#reportType').combobox('getValue');
	}catch(e){
		return '';
	}
}
function getPlanReportTypeVal(){
	try{
		return $('#planReportType').combobox('getValue');
	}catch(e){
		return '';
	}
}
function getDeviceTypeNameVal(){
	try{
		return $('#deviceTypeName').combobox('getValue');
	}catch(e){
		return '';
	}
}
function getSelectIPNameVal(){
	try{
		return $('#selectIPName').combobox('getValue');
	}catch(e){
		return '';
	}
}
function getPlanReportTypeNameVal(){
	try{
		return $('#planReportTypeName').combobox('getValue')+"=:"+$('#planReportTypeName').combobox('getText');
	}catch(e){
		return '';
	}
}

function simulationCheckbox(value, row,index){
	return "<input type='checkbox' id='checkbox"+index+"' value='checkbox'> ";
}

function setSubmitReportinfoVal(){
	var reportinfoval='';
	if(getPlanReportTypeVal().length>1){
		reportinfoval=getPlanReportTypeVal();
	}
	if(getDeviceTypeNameVal().length>1){
		reportinfoval+=";;"+getDeviceTypeNameVal();
	}
	if(getSelectIPNameVal().length>1){
		reportinfoval+=";;"+getSelectIPNameVal();
	}
	if(getPlanReportTypeNameVal().length>1){
		reportinfoval+=";;"+getPlanReportTypeNameVal();
	}
	if(reportinfoval.indexOf(',')>0){
		reportinfoval=reportinfoval.substring(0,reportinfoval.indexOf(','));
	}
	return reportinfoval;
}
var planReport_dialog="";
function newPlanReport() {
	var planReportPanel = $('#plan_report_panel').layout('panel','center');
	$('#editPlanReporter').css("visibility","hidden");
	
	if(!planReportPanel)return;
	var width = $(planReportPanel).width();
	var height = $(planReportPanel).height();
	var top = $(planReportPanel).position().top;
	var left = $('#scanResultTabId').panel('panel').position().left;
	$("#showPlanReport").show();
	planReport_dialog = $("#showPlanReport").dialog({
    	top:top,
		left:left,
		width:width,
		height:height,
		inline:true,
		noheader:true,
		shadow:false,
		border:false,
		style:{'padding':0,'border':0}
    });
    $('#showPlanReport').dialog('expand',true);
    reportinfo='';
    $('#planReportFrom').form('clear');
    jsRemoveAllOption(document.getElementById('report_maillist'));
    $('#report_type').combobox('setValue', 'DAY');
    $('#report_topn').combobox('setValue', '5');
    $('#report_filetype').combobox('setValue', 'pdf');
    getNode(treeNode);
}

function setReport_mail_list(){
	var mailList = getselectAlloption("report_maillist");
	if(!mailList){
		return true;
	}
	var mailListinput=$("<input name='report_maillist' type='hidden' value="+mailList+">");
	$('#planReportFrom').append(mailListinput);
	return false;
}

function getselectAlloption(objId){
	var selectObj = document.getElementById(objId);
	var mailList = '';
	if(selectObj.options.length < 1){
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "error",msg: "邮件地址为空"});
	} else if(selectObj.options.length > 30){
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "error",msg: "邮件个数不得大于30"});
	} else {
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "ok",msg: " "});
		mailList=selectObj.options[0].value;
		for(var i=1;i<selectObj.options.length;i++){
			mailList+=("EMAILadd"+selectObj.options[i].value);
		}
	}
	return mailList;
}

var isBlankEmailServerIPFlag = false;
isBlankEmailServerIP = function(){
	$.ajax({
		url: '/sim/mgrPlanReport/isBlankEmailServerIP',
		type:'post',
		dataType:"json",
		success: function(result) {
			isBlankEmailServerIPFlag = result.status;
		}
	});
}
/**
 * 提交保存
 */
function createPlanReport() {
	isBlankEmailServerIP();
	setTimeout(function(){
		planReport();
	}, 500);
}
function planReport() {
	if(setReport_mail_list()){
		return;
	}else if(isBlankEmailServerIPFlag){
		$("<div></div>").dialog({
			id:'email_config_dialog',
			href:'/page/sysconfig/sysconfig_mailserver.html',
			title:'邮件服务器配置',
			width:1000,
			height:750,
			modal:true,
			onClose:function(){
				$(this).dialog('destroy');
			}
		});
		showAlertMessage("请先配置邮件服务器！");
		return;
	}
	//提交表单事件
    $('#planReportFrom').form('submit', {
        url: '/sim/mgrPlanReport/createPlanReport?cfgType='+type+"&subType="+stype,
        onSubmit: function() {
            return planReportObj.validatPlanReport("add");
        },
        success: function(result) {
            if("success"==result) {
            	showAlertMessage("新建成功!");
            	simHandler.onClickMenuTp('planReport_menu','/page/report/planReport.html');
//            	simHandler.changeMenu(event,'/page/report/planReport.html');
            } else {
            	showErrorMessage("新建失败!");
            }
            
        }
    });
}

/**
 * 字段验证 start
 */

/**
 * 基本字段的验证
 */
planReportObj.planReportFormValidation = $("#planReportFrom").validator({
    theme: 'simple_right',
    showOk:"",
    rules: simHandler.rules,
    fields: {
    	name:"required;length[2~30]",
    	report_mail:"email;length[~30]",
    	reportUser:"length[0~30]"
    }
}).data( "validator" );
/**
 * 多个 combobox 字段
 */
planReportObj.validPlanReportComboxNotnull = function(ids, formValidation) {
	if(ids){
		var noEmptyFlag = true ;
		$.each(ids, function(index,val) {
			var $combox = $('#' + val);
			var comboxVal = $combox.combobox('getValue');
			if(!comboxVal){
				formValidation.showMsg( $combox, {type: "error",msg: "不能为空"});
				noEmptyFlag = false;
				return;
			}else{
				formValidation.showMsg( $combox, {type: "ok",msg: " "});
			}
		});
		return noEmptyFlag;
	} else {
		return false;
	}
};
/**
 * 多个执行时间字段
 */
planReportObj.validPlanReportDateNotnull = function(formValidation) {
	var idJson = [{divElem:"planTimeTypeTd",formElem:"runTime"},
	              {divElem:"monthdiv",formElem:"month"},
	              {divElem:"daydiv",formElem:"day"},
	              {divElem:"datediv",formElem:"date"},
	              {divElem:"pollTime_hourInput",formElem:"hour"},
	              {divElem:"pollTime_minInput",formElem:"min"}];
	var noEmptyFlag = true ;
	var $timeGroup = $('#hid_timeGroup');
	$.each(idJson, function(index,val) {
		var $divElem = $('#' + val.divElem);
		if($divElem.css("display")!="none"){
			var $formElem = $('#' + val.formElem);
			var formElemVal = $formElem.val();
			if(!formElemVal){
				noEmptyFlag = false;
				return;
			}
			if(("date"==val.formElem && (formElemVal>31 || formElemVal<1)) 
					|| ("hour"==val.formElem && (formElemVal>23 || formElemVal<0))
					|| ("min"==val.formElem && (formElemVal>59 || formElemVal<0))
					){
				noEmptyFlag = false;
				return;
			}
		}
	});
	if(noEmptyFlag) {
		formValidation.showMsg( $timeGroup, {type: "ok",msg: " "});
	} else {
		formValidation.showMsg( $timeGroup, {type: "error",msg: "时间格式不对"});
	}
	return noEmptyFlag;
};
/**
 * 名称、email、个人签名
 */
planReportObj.validNameEmailNotnull = function(formValidation) {
	var $planname = $("#planname");
	var requiredFlag = null;
	var lengthFlag = null;

	requiredFlag = formValidation.test($planname[0], "required;");
	lengthFlag = formValidation.test($planname[0], "length[2~30];");
	if(!requiredFlag){
		formValidation.showMsg( $planname, {type: "error",msg: "不能为空"});
		return false;
	}else if(!lengthFlag){
		formValidation.showMsg( $planname, {type: "error",msg: "长度应该为2到30个字符"});
		return false;
	}else{
		formValidation.showMsg( $planname, {type: "ok",msg: " "});
	}

	var $report_user = $("#report_user");
	lengthFlag = formValidation.test($report_user[0], "length[~30];");
	if(!lengthFlag){
		formValidation.showMsg( $report_user, {type: "error",msg: "长度应该为小于30个字符"});
		return false;
	}else{
		formValidation.showMsg( $report_user, {type: "ok",msg: " "});
	}

	var $report_mail = $("#report_mail");
	var emailFlag = null;
	if($report_mail.val()){
		emailFlag = formValidation.test($report_mail[0], "email;");
	}else{
		emailFlag = true;
	}
	lengthFlag = formValidation.test($report_mail[0], "length[~30];");
	if(!emailFlag){
		formValidation.showMsg( $report_mail, {type: "error",msg: "邮件格式错误"});
		return false;
	}else if(!lengthFlag){
		formValidation.showMsg( $report_mail, {type: "error",msg: "长度应该为小于30个字符"});
		return false;
	}else{
		formValidation.showMsg( $report_mail, {type: "ok",msg: " "});
	}
	return true;
};
/**
 * 表单字段验证 main
 */
planReportObj.validatPlanReport = function(operatorVal){
	var emptyFlag = null ;
	// 名称、email、个人签名
	emptyFlag = planReportObj.validNameEmailNotnull(planReportObj.planReportFormValidation);
	if(!emptyFlag){
		return false;
	}
	// 执行时间
	emptyFlag = planReportObj.validPlanReportDateNotnull(planReportObj.planReportFormValidation);
	if(!emptyFlag){
		return false;
	}
	var ids = ["report_type", "report_topn", "report_filetype"];
	if(reportType && reportType=="schedule_cfg_logstatistics") {
		ids.splice(0,2);
	}
	if(operatorVal=="add"){
		// 计划报表类型、主题类型
		emptyFlag = planReportObj.validPlanReportComboxNotnull(["reportType","planReportType"], planReportObj.planReportFormValidation);
		if(!emptyFlag){
			return false;
		}
		var reportType = $("#reportType").combobox('getValue');
		var baseTypeVal = getPlanReportTypeVal();
		var baseTypeData = $("#planReportType").combobox('getData');
		var baseType = null;
		$.each(baseTypeData,function(index,val){
			if(val.value == baseTypeVal){
				baseType = val.baseType;
				return;
			}
		});
		if(reportType && reportType=="schedule_cfg_sysreport") {
			// 报表主题
			ids.push("planReportTypeName");
			if(baseType=="2") {
				// 设备类型、日志源
				ids.push("deviceTypeName", "selectIPName");
			}
		}
		
	}
	// 时间类型、数据Top(N)、导出文件格式
	emptyFlag = planReportObj.validPlanReportComboxNotnull(ids, planReportObj.planReportFormValidation);
	if(!emptyFlag){
		return false;
	}
	return true;
};

/**
 * 字段验证 end
 */
planReportFunc.cancelBtn=function(){
	simHandler.onClickMenuTp('planReport_menu','/page/report/planReport.html');
//	simHandler.changeMenu(event,'/page/report/planReport.html');
};
planReportFunc.setEmailServer=function(){
//	simHandler.changeMenu(event,'/page/sysconfig/index.html');
	$('<div/>').dialog({
		title : '配置页面',
		closable:true,
        width: 1200,
        height: 700,
		modal : true,
		cache : false,
		href : '/page/sysconfig/sysconfig_mailserver.html',
		onClose : function() {
			$(this).dialog('destroy');
		},
		onLoad: function() {
			
		}
	});
//	simSysconfig.changeMenu(this, event,'/page/sysconfig/sysconfig_mailserver.html');
};
function selectchange(value){
	if("EVERY_YEAR"==value){
		$("#monthdiv").css({"display": "inline"});
		$("#datediv").css({"display": "inline"});
	}else{
		$("#monthdiv").css("display","none");
	}
	if("EVERY_WEEK"==value){
		
		$("#daydiv").css({"display": "inline"});
		
		$("#datediv").css("display", "none");
	}else{
		$("#daydiv").css("display", "none");
	}
	if("EVERY_MONTH"==value){
		$("#datediv").css({"display": "inline"});
		$("#daydiv").css("display", "none");
	}
	if("EVERY_DAY"==value){
		$("#datediv").css("display", "none");
	}
	if(!(null==value||value.length<1||undefined==value)){
		try{
			$("#planTimeTypeTd").css("display", "none");
		}catch(er){}
	}
}

var reportHandler={};

function cusReportRoleData(result){
	
	var hasCustomReoprtRole=result['hasLogReoprtRole'];
	var hasLogStatisticsRole=result['hasLogStatisticsRole'];
	
	if(hasLogStatisticsRole){
		if(hasCustomReoprtRole){
			return [{
		        "id": "schedule_cfg_sysreport",
		        "text": "基本报表"
		    }, {
		        "id": "schedule_cfg_customreport",
		        "text": "自定义报表"
		    }, {
		        "id": "schedule_cfg_logstatistics",
		        "text": "日志统计"
		    }];
		}else{
			return [{
		        "id": "schedule_cfg_sysreport",
		        "text": "基本报表"
		    }, {
		        "id": "schedule_cfg_logstatistics",
		        "text": "日志统计"
		    }];
		}
	}else{
		if(hasCustomReoprtRole){
			return [{
		        "id": "schedule_cfg_sysreport",
		        "text": "基本报表"
		    }, {
		        "id": "schedule_cfg_customreport",
		        "text": "自定义报表"
		    }];
		}else{
			return [{
		        "id": "schedule_cfg_sysreport",
		        "text": "基本报表"
		    }];
		}
	}
}

$(function(){
	//domainmy=document.domain;
	reportHandler.planLoadSuccess=function(data){
		$(".report-mail").qtip({
			content : {
				text:false
			},
			style : 'cream',//样式cream, light, dark, green and red.
			position : {
				target : 'mouse',//跟随鼠标显示提示信息
				adjust : { screen: true },//根据屏幕调整显示位置
				container: $('#report_email_tips')//提示信息的HTML元素加入文档中的位置
			},
			hide :'click mouseleave'
		});
	};
	
	$("#runTime").change(function(){
		var value='';
		try{
			value=$("#runTime").val();
		}catch(e){return;}
		if("EVERY_YEAR"==value) {
			$("#monthdiv").css({"display": "inline"});
			$("#datediv").css({"display": "inline"});
		} else {
			$("#monthdiv").css("display","none");
		}
		if("EVERY_WEEK"==value){
			$("#daydiv").css({"display": "inline"});
			
			$("#datediv").css("display", "none");
		} else {
			$("#daydiv").css("display", "none");
		}
		if("EVERY_MONTH"==value){
			$("#datediv").css({"display": "inline"});
			$("#daydiv").css("display", "none");
		}
		if("EVERY_DAY"==value){
			$("#datediv").css("display", "none");
		}
	});
	$("#planreport_period_execute_tree_id").tree({//page/report/ss.json
		url:'/sim/mgrPlanReport/showPlanTaskTreeByPeriod',
		animate: true, 
		onClick: function(node) {
			treeNode=node.id;
			treeNodeIdVal=node.id;
			showReportResultByNode(node.id,node.text);
			
		}
	});
    
	$('#reportType').combobox({
	    url: '/sim/report/reoprtRole',
	    loadFilter:function(datas){
	     	return cusReportRoleData(datas);
	    },    
	    valueField: 'id',
	    textField: 'text',
	    onChange:function(newValue,oldValue){
	    	var reportTypeId=$('#reportType').combobox('getValue');
	    	var reportTypeCNnameVal=null;
	    	if(reportTypeId=='schedule_cfg_sysreport'){
	    		reportTypeCNnameVal="基本报表:";
	    		
	    	}else if(reportTypeId=='schedule_cfg_customreport'){
	    		reportTypeCNnameVal="自定义报表:";
	    		$("#planReportTd").css("display","none");
	    		$("#planReportTd1").css("display","none");
	    	}else if(reportTypeId=='schedule_cfg_logstatistics'){
	    		reportTypeCNnameVal="日志统计:";
	    	}
	    	
	    	var createCofigId=$("<input name='configId' type='hidden'>");//setSubmitReportinfoVal()
	    	createCofigId.val(reportTypeId) ;
	    	try{
	    		var createCofigval=$("input[name='configId']").val();
	    		
	    		if(undefined==createCofigval||null==createCofigval){
	    			var tdv=$("<td>");
	    	    	tdv.append(createCofigId);
	    			$("#configIdTr").append(tdv);
	    		}else{
	    			$("input[name='configId']").replaceWith(createCofigId);
	    		}
	    	}catch(ex){
	    		var tdv=$("<td>");
    	    	tdv.append(createCofigId);
    			$("#configIdTr").append(tdv);
	    	}
	    	
	    	$("#reportTypeCNname").text(reportTypeCNnameVal);
	    	$("#showReportdiv").css("display","block");
	    	reportinfo='';
	    	changeConfig();
	    }
	});
	
	$("#report_type").combobox({
		data: [{
            "id": "DAY",
            "text": "天报表"
        }, {
            "id": "WEEK",
            "text": "周报表"
        },{
            "id": "MONTH",
            "text": "月报表"
        }, {
            "id": "YEAR",
            "text": "年报表"
        }],
	    valueField: 'id',
	    textField: 'text'
	});
	
	$("#report_topn").combobox({
		data: [{
            "id": "5",
            "text": "Top5"
        }, {
            "id": "10",
            "text": "Top10"
        },{
            "id": "15",
            "text": "Top15"
        }, {
            "id": "20",
            "text": "Top20"
        }, {
            "id": "25",
            "text": "Top25"
        }],
	    valueField: 'id',
	    textField: 'text'
	});
	
	$("#report_filetype").combobox({
		data: [{
            "id": "docx",
            "text": "word文件"
        }, {
            "id": "pdf",
            "text": "pdf文件"
        }, {
            "id": "excel",
            "text": "excel文件"
        }],
	    valueField: 'id',
	    textField: 'text'
	});

});

var report_maillist='';

var cou=0;
var addval='';
function addToTextarea(oid, values){
	
	var tid='#'+oid;
	
	
	if(cou>0){
		var tempval=$(tid).val();
		addval=tempval+' \n'+values;
	}else{
		addval=values;
	}
	$(tid).val(addval);
	
	if(report_maillist.length<2){
		
		report_maillist+=values;
	}else{
		report_maillist+=("EMAILadd"+values);
	}
	cou++;
}
function removeToTextarea(oid){
	var tid='#'+oid;
	$(tid).val("");
	report_maillist='';
	addval='';
	cou=0;
}
function addReportTypeName(){
	var reportTId=$('#reportType').combobox('getValue');
	var typetextname='';
	var typetextvalue='';
	if(reportTId=='schedule_cfg_sysreport'){
		typetextname=$('#planReportTypeName').combobox('getText').toString();
		if(typetextname.indexOf(',')>0){
			typetextname=typetextname.substring(0,typetextname.indexOf(','));
		}
		typetextvalue=$('#planReportTypeName').combobox('getValue');
		reportinfo+=(';;'+typetextvalue+'=:'+typetextname);
	}else if(reportTId=='schedule_cfg_customreport'){
		typetextname=$('#planReportType').combobox('getText');
		typetextvalue=$('#planReportType').combobox('getValue');
		reportinfo=typetextvalue+'=:'+typetextname;
	}else if(reportTId=='schedule_cfg_logstatistics'){
		typetextname=$('#planReportType').combobox('getText');
		typetextvalue=$('#planReportType').combobox('getValue');
		reportinfo=typetextvalue+'=:'+typetextname;
	}
	
	var reinfoinput=$("<input name='reportinfo'  type='hidden'>");//setSubmitReportinfoVal()
	reinfoinput.val(reportinfo) ;
	try{
		var reinfoinhtmlval=$("input[name='reportinfo']").val();
		
		if(undefined==reinfoinhtmlval||null==reinfoinhtmlval){
			$('#planReportFrom').append(reinfoinput);
		}else{
			$("input[name='reportinfo']").replaceWith(reinfoinput);
		}
	}catch(ex){
		$('#planReportFrom').append(reinfoinput);
	}
	$("#type").val(typetextname);
	
	//reportinfo='';//修改的，
}

function changeConfig(){
	var reportTId=$('#reportType').combobox('getValue');
	if(reportTId=='schedule_cfg_sysreport'){
		basereportcombo();
		//reportTypeCNnameVal="基本报表:";
	}else if(reportTId=='schedule_cfg_customreport'){
		//reportTypeCNnameVal="自定义报表:";
		customreportcombo();
	}
	
	$("#planReportTypediv").css("display","block");
	$("#planReportTypediv2").css("display","block");
    
}

function showlogRes(){
	$("#deviceTypeTd").css("display","block");
	$("#deviceTypeTd1").css("display","block");
	$("#selectIPTd").css("display","block");
	$("#selectIPTd1").css("display","block");
}

function hidelogRes(){
	$("#deviceTypeTd").css("display","none");
	$("#deviceTypeTd1").css("display","none");
	$("#selectIPTd").css("display","none");
	$("#selectIPTd1").css("display","none");
}
//,{
//    "value": "3",
//    "text": "审计对象报表"
//}
//, {
//    "value": "5",
//    "text": "监视对象报表"
//}
function reportRoleData(haslogReportRole){
	if(null==haslogReportRole||undefined==haslogReportRole){
		return [{
		    "value": "1",
		    "text": "审计报表",
		    "baseType":"1"
		}];
	}else if(haslogReportRole){
		return [ {
            "value": "2",
            "text": "事件报表",
            "baseType":"1"
        }, {
            "value": "4",
            "text": "日志报表",
            "baseType":"2"
        }];
	}else{
		return [ {
            "value": "2",
            "text": "事件报表",
            "baseType":"1"
        }];
	}
}

function basereportcombo(){
	$("#planReportTd").css("display","block");
	$("#planReportTd1").css("display","block");
	$("#report_type_tr").css("visibility","visible");
	$("#report_topn_tr").css("visibility","visible");
	$('#planReportType').combobox({
	    url:'/sim/report/hasLogReoprtRole', 
	    valueField: 'value',
	    textField: 'text',
	    editable: false,
	    loadFilter:function(dat){
	    	return reportRoleData(dat);
	    },
        onChange: function(value) {
        	if(value==4||value==5){
        		showlogRes();
        	}else{
        		hidelogRes();
        	}
        	$.ajax({
            	url: '/sim/planReport/changeReportType?reportType='+value,
                type: "POST",
                dataType: "xml",
                success: function (dat) {
                	var json = $.xml2json(dat);
                	var mgrdata='';
                	var deviceType='';
                	var selectIP='';
                	try{
                		mgrdata=json.selectReport;
                		deviceType=json.deviceType;
                		selectIP=json.selectIP;
                		
                	}catch(e){  }
                	
                	$('#planReportTypeName').combobox({
                		valueField: 'value',
                        textField: 'text',
                        data:mgrdata,
                        loadFilter: function(datamgrs){
                        	for(var i=0;i<datamgrs.length;i++){
                    			var temptext=datamgrs[i].text.toString();
                    			if(temptext.indexOf(',')>0){
                    				temptext=temptext.substring(0,temptext.indexOf(','));
                    			}
                    			datamgrs[i].text=temptext;
                    		}
                    		return datamgrs;
                    	},
                        onHidePanel: function(valu,text){
                        	reportinfo='';
                        	if(undefined!=deviceType.value && deviceType.value.length>0){
                        		reportinfo=deviceType.value;
                        	}
                        	if(undefined!=selectIP.value &&selectIP.value.length>0){
                        		reportinfo+=';;'+selectIP.value;
                        	}
                        	if(value==1||value==2||value==3){
                        		addReportTypeName();
                        	}
                        	
                        }
                	});
                	
                	if(value==4||value==5){
                		if(undefined==deviceType.length){
                    		deviceType=[deviceType];
                    	}
                		$('#deviceTypeName').combobox({
                    		valueField: 'value',
                            textField: 'text',
                            data:deviceType,
                            loadFilter: function(devicedata){
                            	for(var i=0;i<devicedata.length;i++){
                        			var temptext=devicedata[i].text.toString();
                        			if(temptext.indexOf(',')>0){
                        				temptext=temptext.substring(0,temptext.indexOf(','));
                        			}
                        			devicedata[i].text=temptext;
                        		}
                        		return devicedata;
                        	},
                            onChange: function(valuea){
                            	$.ajax({
                                	url: '/sim/planReport/changeDeviceType?deviceType='+valuea,
                                    type: "POST",
                                    dataType: "xml",
                                    success: function (datas) {
                                    	var jsonda = $.xml2json(datas);
                                    	var mgrdatass='';
                                    	
                                    	var selectIPss='';
                                    	try{
                                    		mgrdatass=jsonda.selectReport;
                                    		
                                    		selectIPss=jsonda.selectIP;
                                    		if(undefined==selectIPss.length){
                                    			selectIPss=[selectIPss];
                                    		}
                                    		if(undefined==mgrdatass.length){
                                    			mgrdatass=[mgrdatass];
                                    		}
                                    	}catch(e){  }
                                    	                                    	
                                    	$('#selectIPName').combobox({
                                    		valueField: 'value',
                                            textField: 'text',
                                            data:selectIPss,
                                            loadFilter: function(selectIPdata){
                                            	for(var i=0;i<selectIPdata.length;i++){
                                        			var temptext=selectIPdata[i].text.toString();
                                        			if(temptext.indexOf(',')>0){
                                        				temptext=temptext.substring(0,temptext.indexOf(','));
                                        			}
                                        			selectIPdata[i].text=temptext;
                                        		}
                                        		return selectIPdata;
                                        	},
                                            onHidePanel: function(valu,text){
                                            	
                                            }
                                    	});
                                    	
                                    	$('#planReportTypeName').combobox({
                                    		//disabled:true,
                                    		valueField: 'value',
                                            textField: 'text',
                                            data:mgrdatass,
                                            loadFilter: function(datamgrss){
                                            	for(var i=0;i<datamgrss.length;i++){
                                        			var temptext=datamgrss[i].text.toString();
                                        			if(temptext.indexOf(',')>0){
                                        				temptext=temptext.substring(0,temptext.indexOf(','));
                                        			}
                                        			datamgrss[i].text=temptext;
                                        		}
                                        		return datamgrss;
                                        	},
                                            onHidePanel: function(valu,text){
                                            	reportinfo='';
                                            	reportinfo=$('#deviceTypeName').combobox('getValue');
                                            	reportinfo+=';;'+$('#selectIPName').combobox('getValue');
                                            	addReportTypeName();
                                            }
                                    	});

                                    }
                                });
                            }
                    	});
                	}
                	

                }
            });
        
        }
	});
}

function customreportcombo(){
	hidelogRes();
	$("#report_type_tr").css("visibility","visible");
	$("#report_topn_tr").css("visibility","visible");
	$("#planReportTd").css("display","none");
	$("#planReportTd1").css("display","none");
	$('#planReportType').combobox('clear');
	$('#planReportType').combobox({
		url:'/sim/planReport/openDefReportPage?value=',
		valueField: 'id',
        textField: 'mstName',
	    loadFilter:function(dat){
	    	return dat;
	    },
        onHidePanel: function(valu,text){
        	reportinfo='';
        	addReportTypeName();
        }
	});
	
}

function logstatisticscombo(){
	hidelogRes();
	$("#planReportTd").css("display","none");
	$("#planReportTd1").css("display","none");
	$("#report_type_tr").css("visibility","hidden");
	$("#report_topn_tr").css("visibility","hidden");
	$('#planReportType').combobox('clear');
	$('#planReportType').combobox({
		url:'/sim/planReport/reportLogStatisticsTask',
		valueField: 'id',
        textField: 'taskName',
	    loadFilter:function(dat){
	    	return dat;
	    },
        onHidePanel: function(valu,text){
        	reportinfo='';
        	addReportTypeName();
        }
	});
	
}

function editPlanReport(selRowIndex){
	
	try{
		var planReportPanel = $('#plan_report_panel').layout('panel','center');
		
		var w = $(planReportPanel).width();
		var h = $(planReportPanel).height();
		var top = $(planReportPanel).position().top;
		var left = $('#scanResultTabId').panel('panel').position().left;
		
		
		if(!planReportPanel)return;
		$(planReportPanel).parent().css('position','relative');
		$("#showPlanReport").show();
	    $("#showPlanReport").dialog({
	    	top:top,
			left:left,
			width:w,
			height:h,
			inline:true,
			noheader:true,
			modal:true,
			shadow:false,
			border:false,
			style:{'padding':0,'border':0}
	    });
	}catch(es){}
    
	try{
		
		var rows = $('#asset_table').datagrid('getRows');
		var row = rows[selRowIndex];

		if (row) {
	    	rowedit=row;
	    	$('#createPlanReporter').css("display","none");
	    	$('#editPlanReporter').css("visibility","visible");
	    	$('#showPlanReport').dialog('expand',true).dialog('setTitle', '编辑信息');

	    	$('#planReportFrom').form('load', row);
	    	$('#showReportdiv').css("display","block");
	        $.ajax({
	            url: '/sim/mgrPlanReport/showPlanReport',
	            type: "POST",
	            dataType: "json",
	            data: {'respId': row.id,
	            	'cfgType':type,
	            	'subType':stype
	            	},
	            success: function(dats) {
	            	
	            	//var configString=dats.config;
	            	var report_type= dats.reportType;
	            	//getValueByStringXml(configString,'report_type');
	            	var report_topn= dats.reportTopn;
	            	//getValueByStringXml(configString,'report_topn');
	            	var report_filetype= dats.reportFileType;
	            	//getValueByStringXml(configString,'report_filetype');
	            	var report_user= dats.reportUser;
	            	var reportSys=dats.reportSys;
	            	if($("#reportType").combobox('getValue')){
	            		$("#report_type_tr").css("display","none");
	            		$("#report_topn_tr").css("display","none");
	            	}else{
	            		$("#report_type_tr").css("display","inline");
	            		$("#report_topn_tr").css("display","inline");
	            	}
	            	var r_expression=dats.scheduleExpression;
	            	//getValueByStringXml(configString,'report_user');
//	            	var report_sys= getValueByStringXml(configString,'report_sys');
	            	var report_maillist= emailStringToArr(dats.reportMailList);
	            	//getValueBySvx(configString,'report_maillist');
	            	if(report_maillist.length>0){
	            		try{
	            			jsRemoveAllOption(document.getElementById('report_maillist'));
	            			for(var i=0;i<report_maillist.length;i++){
	            				jsAddItemToSelect(document.getElementById('report_maillist'), report_maillist[i],report_maillist[i]);
	                		}
	            		}catch(e){}
	            	}
	            	selectchange(dats.scheduleType);
//	            	var reportsystemp=report_sys.split('=:');
	            	var expres=r_expression.split(" ");
//	            	if(reportsystemp.length==2){
//	            		var reportsysValueArr=reportsystemp[0].split(';;');
//	            		var reportsysText=reportsystemp[1];
//	            		if(reportsysValueArr.length==1){
//	            			$("#planReportType").combobox('setValue',reportsysValueArr[0]);
//	            		}else if(reportsysValueArr.length>1){
//	            			$("#planReportType").combobox('setValue',reportsysValueArr[0]);
//	            			$("#planReportTypeName").combobox('setValue',reportsysValueArr[reportsysValueArr.length-1]);
//	            			$('#planReportTd').css("display","block");
//	                    	$('#planReportTd1').css("display","block");
//	            		}
//	            		
//	            	}
	            	try{
	            		$('#planReportTypediv').css("display","block");
		            	$('#planReportTypediv2').css("display","block");
		            	$('#planReportType').combobox('setValue',reportSys.split("=:")[1]);
		            	$("#planReportType").combobox('disable');
	            	}catch(er){
	            		$('#planReportTypediv').css("display","none");
		            	$('#planReportTypediv2').css("display","none");
		            	$("#planReportType").combobox('disable');
	            	}
//	                $("#plandesc").text(dats.desc);
	            	$('#planname').val(dats.name);
	                $("#report_user").val(report_user);
	                $("#report_mail").val(report_maillist[0]);
	                $("#report_type").combobox('setValue',report_type);
	                $("#report_topn").combobox('setValue',report_topn);
	                $("#reportType").combobox('disable');
	                $("input[disabled]").css("background-color","#eeeeee");
	                $("#hour").numberbox("setValue",expres[2]);
	                $("#min").numberbox("setValue",expres[1]);
	                $("#date").numberbox("setValue","?"==expres[3]?"1":"*"==expres[3]?"1":expres[3]);
	                $("#day").val("?"==expres[5]?"1":"*"==expres[5]?"1":expres[5]);
	                $("#month").val("?"==expres[4]?"1":"*"==expres[4]?"1":expres[4]);
	                
	                $("#report_filetype").combobox('setValue',report_filetype);
	            },
	            error: function() {
	            	showErrorMessage('无法加载');
	            }
	        });
	    } else {
	    	showAlarmMessage( '请选择要编辑的行后再进行编辑操作');
	        $('#showPlanReport').dialog('close');
	        return;
	    }
	}catch(err){}
	
}

function emailStringToArr(emailString){
	var resultArr=[];
	try{
		if(emailString.length<1){return resultArr;}
		else{
			if (emailString.indexOf('MAILadd')>0) {
				resultArr=emailString.split('MAILadd');
			} else{resultArr[0]=emailString;}
			return resultArr;
		}
	}catch(e){
		
	}
}

function getValueByStringXml(source,va){
	var reporttypet=source.split('"'+va+'"');
	var rtt=reporttypet[1].trim().split('value="');
	var reporttypevalue=rtt[1].trim().split('"\/>');
	return reporttypevalue[0];
}

function getValueBySvx(source,va){
	var reporttypet=source.split('"'+va+'"');
	var rtt=reporttypet[1].trim().split('<value info="');
	var reporttypevalue=rtt[1].trim().split('"\/>');
	var resultLi=[];
	resultLi[0]=reporttypevalue[0];
	if(rtt.length>=3){
		var reporttypevalue1=rtt[2].trim().split('"\/>');
		resultLi[1]=reporttypevalue1[0];
	}
	return resultLi;
}
function submitUpdatePlanReport() {
	isBlankEmailServerIP();
	setTimeout(function(){
		updatePlanReport();
	}, 500);
}
function updatePlanReport(){
	if(isBlankEmailServerIPFlag){
		$("<div></div>").dialog({
			id:'email_config_dialog',
			href:'/page/sysconfig/sysconfig_mailserver.html',
			title:'邮件服务器配置',
			width:1000,
			height:750,
			modal:true,
			onClose:function(){
				$(this).dialog('destroy');
			}
		});
		showAlertMessage("请先配置邮件服务器！");
		return;
	}
	var rowt = $("#asset_table").datagrid("getSelected");
	if(null==rowt||undefined==rowt){
		try{rowt=rowedit;}catch(erro){}
	}
	var updatePlanId=rowt.id;
	var updateNodeId=rowt.outNodeIdJsonString;

	if(setReport_mail_list()){
		return;
	}
	if(reportinfo.length<2){
		var reinfoinput=$("<input name='reportinfo'  type='hidden' value="+' '+">");//setSubmitReportinfoVal()
		try{
			var reinfoinhtmlval=$("input[name='reportinfo']").val();
			
			if(undefined==reinfoinhtmlval||null==reinfoinhtmlval){
				$('#planReportFrom').append(reinfoinput);
			}else{
				$("input[name='reportinfo']").replaceWith(reinfoinput);
			}
		}catch(ex){
			$('#planReportFrom').append(reinfoinput);
		}
	}
	//提交表单事件
	$('#planReportFrom').form('submit', {
        url: '/sim/mgrPlanReport/editPlanReport?cfgType='+type+"&subType="+stype+'&respId='+updatePlanId+'&nodeId='+updateNodeId,
        onSubmit: function() {
            return planReportObj.validatPlanReport("update");
        },
        success: function(result) {
            if("success"==result){
            	simHandler.onClickMenuTp('planReport_menu','/page/report/planReport.html');
//            	simHandler.changeMenu(event,'/page/report/planReport.html');
            }else{
            	showErrorMessage( "更新失败!");
            }
            
        }
    });
}
function removeBatchPlanReport(){
	var rows=$("#asset_table").datagrid("getSelections");
	var idLength=rows.length;
	if (!rows || idLength<1) {
		showAlarmMessage( '请选择要删除的行后再进行删除操作');
        $('#showPlanReport').dialog('close');
    } else{
    	var deleteIds='';
    	for(var index=0;index<idLength-1;index++){
    		deleteIds+=rows[index].id+',';
    	}
    	deleteIds+=rows[idLength-1].id;
    	$.messager.confirm('提示', '确定要删除选中行吗?', function(r) {
            if (r) {
                $.ajax({
                    url: '/sim/mgrPlanReport/removeBatchPlanReport',
                    data: {'respIds': deleteIds,
                    	'cfgType':type,
                    	'subType':stype
                    },
                    dataType:'json',
                    contentType:"text/javascript",
                    success: function(result) {
                    	if(result.status){
                    		showAlertMessage( "删除成功!");
                        	$('#asset_table').datagrid('reload');
                        	$("#planreport_period_execute_tree_id").tree('reload');
                        }else{
                        	showErrorMessage("删除数据失败!");
                        }
                    	$('#asset_table').datagrid('reload');
                    },
                    error: function() {
                    	showErrorMessage( '删除数据失败');
                    }
                });
            }
        });
    }
	
}
function deletePlanReport(){
	var row = $("#asset_table").datagrid("getSelected");
		
	if (!row) {
		showAlarmMessage( '请选择要删除的行后再进行删除操作');
        $('#showPlanReport').dialog('close');
    } else {
    	var deleteId=row.id;
        $.messager.confirm('提示', '确定要删除选中行吗?', function(r) {
            if (r) {
                $.ajax({
                    url: '/sim/mgrPlanReport/removePlanReport',
                    data: {'respId': deleteId,
                    	'cfgType':type,
                    	'subType':stype
                    },
                    success: function(result) {
                    	if("success"==result){
                    		showAlertMessage( "删除成功!");
                        	$('#asset_table').datagrid('reload');
                        	$("#planreport_period_execute_tree_id").tree('reload');
                        }else{
                        	showErrorMessage("删除数据失敗!");
                        }
                    	$('#asset_table').datagrid('reload');
                    	
                        
                    },
                    error: function() {
                    	showErrorMessage( '删除数据失败');
                    }
                });
            }
        });
    }
}

function showReportResultdiv(rowIndex, rowData){
	
    var row = $("#asset_table").datagrid("getSelected");
    if (!row && undefined==rowData) {
    	showAlarmMessage( '请选择需查看的行再操作');
    } else {
    	var id=null;
    	try{
    		if(row)id=row.id;
    		if(undefined!=rowData&&null!=rowData){
    			id=rowData.id;
    			var plan_rpt_tree = $("#planreport_period_execute_tree_id") ;
    			var selectNode = plan_rpt_tree.tree("find",id) ;
    			plan_rpt_tree.tree("select",selectNode.target);
    			plan_rpt_tree.tree('expand',selectNode.parent.target);
    			treeNodeIdVal=id;
    		}
    	}catch(er){}
    	var planReportPanel = $('#showPlanReportResult').panel('panel');
    	if(!planReportPanel)return;
    	$(planReportPanel).parent().css('position','relative');
    	var width = $(planReportPanel).width();
    	var height = $(planReportPanel).height();
    	
        $("#showPlanReportResult").dialog({
        	top:0,
    		left:0,
    		width:width,
    		inline:true,
    		noheader:true,
    		//collapsed:true,
    		modal:true,
    		shadow:false,
    		border:false,
    		style:{'padding':0,'border':0}
        });
        $('#showPlanReportResult').dialog('expand',true);
    	$("#planresulttable").datagrid({
        	url:'/sim/mgrPlanReport/showPlanTaskResult?subType=config.schedule.report&cfgType=config.schedule&respId='+id,
        	selectOnCheck: true,
        	checkOnSelect: true

        });
    	
    }
    
}

function getTreeNodeId(){
	var plan_rpt_tree = $("#planreport_period_execute_tree_id") ;
	var paln_node = plan_rpt_tree.tree("getSelected");
	try{
		if(undefined!=paln_node && null!=paln_node)
			treeNodeIdVal=paln_node.id;
		return treeNodeIdVal;
	}catch(er){return '';}
	
}

function getNode(treeNodeId){
	try{
		if(treeNodeId.length<2){
			$('#runTime').val('EVERY_DAY');
			$('#planTimeTypeTd').css("display","inline");
		}else if(treeNodeId.length>2&&treeNodeId.length<12){
				$('#runTime').val(treeNodeId);
				$('#planTimeTypeTd').css("display","none");
		}
	}catch(err){}
}

function showReportResultByNode(treeNodeId,text){
	
	if(treeNodeId.length<2){
		
		try{
			$('#asset_table').datagrid({
				url:'/sim/mgrPlanReport/getPlanBySubType?subType=config.schedule.report&cfgType=config.schedule&scheduleType='
			});
			$('#runTime').val('EVERY_DAY');
			selectchange($('#runTime').val());
			$('#planTimeTypeTd').css("display","inline");
			
			$('#asset_table').datagrid("reload");
			$('#showPlanReportResult').dialog('close');
		}catch(e){}
		
		return;
	}else if(treeNodeId.length>2&&treeNodeId.length<12){
		
		try{
			$("#scanResultTabId").attr({'title':text+'计划报表'}); 
			$('#runTime').val(treeNodeId);
			selectchange($('#runTime').val());
			$('#planTimeTypeTd').css("display","none");
			
			$('#asset_table').datagrid({
				url:'/sim/mgrPlanReport/getPlanBySubType?subType=config.schedule.report&cfgType=config.schedule&scheduleType='+treeNodeId
			});
			
			$('#asset_table').datagrid("reload");
			$('#showPlanReportResult').dialog('close');
		}catch(ei){}
		
		return;
	}
	
	try{
		var planReportPanel = $('#plan_report_panel').layout('panel','center');
		if(!planReportPanel)return;
	
		$(planReportPanel).parent().css('position','relative');
		var width = $(planReportPanel).width();
		var height = $(planReportPanel).height();
		var top = $(planReportPanel).position().top;
		
	    $("#showPlanReportResult").dialog({
	    	top:top,
			left:0,
			width:width,
			inline:true,
			noheader:true,
			//collapsed:true,
			modal:false,
			shadow:false,
			border:false,
			style:{'padding':0,'border':0}
	    });
	    $('#showPlanReportResult').dialog('expand',true);
		$("#planresulttable").datagrid({
	    	url:'/sim/mgrPlanReport/showPlanTaskResult?subType=config.schedule.report&cfgType=config.schedule&respId='+treeNodeId,
	    	selectOnCheck: true,
	    	checkOnSelect: true

	    });
	}catch(ee){}
}

function changeIntervalType() {
	for ( var i = 0; i < document.all["intervalType"].length; i++) {
		if (document.all["intervalType"][0].checked) {

			document.all["eventFrequencyType"].disabled = true;
			$j("[name=eventFrequencyType]").attr("style", "display: none");
		}
		if (document.all["intervalType"][1].checked) {
			document.all["eventFrequencyType"].disabled = false;
			$j("[name=eventFrequencyType]").attr("style", "width: 150px");

		}

	}
}
function checkExpression(obj,expressionRadioName,cfgType){
	if(document.getElementsByName("intervalType")[1].value==expressionRadioName)
	{
	  
	  if(checkEmptyAndSpace(document.getElementById("expression").value))
	  {
	  var root = $("expression").parentNode;
	  text="自定义内容不能为空";
	  root.childNodes[1].innerHTML = text;
	  lockSubmitButton();
	  return false;
	  }

	}
	   var value = obj.value;
		Request.sendGET(objectPath + "/sim/mgrPlanReport/checkExpression?expression=" + value+"&cfgType="+type,checkExpressionCallBack);
}

function checkExpressionCallBack(response) {
	var value = response.responseText;
	if (value == "success") {
		showWarMessage(true);
		unLockSubmitButton();
	} else {
		if (value == "failed") {
			showWarMessage(false);
			lockSubmitButton(false);
		}
	}
}
function changeBatchStates(statusubmit){
	var rows=$("#asset_table").datagrid("getSelections");
	var idLength=rows.length;
	if (!rows || idLength<1) {
		showAlarmMessage( '请选择需要操作的行操作');
        
    } else{
    	var modifyIds='';
    	for(var index=0;index<idLength-1;index++){
    		if(rows[index].status !== statusubmit){
    			modifyIds+=rows[index].id+',';
    		}
    	}
    	if(rows[idLength-1].status !== statusubmit){
    		modifyIds+=rows[idLength-1].id;
    	}
    	if(modifyIds != ''){
    		$.ajax({
				url: '/sim/mgrPlanReport/changeBatchStates',
				data: {respIds: modifyIds,
					cfgType:type,
					subType:stype,
					status:statusubmit
				},
                dataType:'json',
                contentType:"text/javascript",
				success: function() {
					$('#asset_table').datagrid('reload'); // reload the user data
				},
				error: function() {
					showErrorMessage('状态更改失败');
				}
			});
    	}else{
    		showErrorMessage('状态无需更改');
    	}
    }
}
function changeState(statusubmit){
	var row = $("#asset_table").datagrid("getSelected");
	if (row) {
		if(row.status !== statusubmit){
			$.ajax({
				url: '/sim/mgrPlanReport/changeStates',
				data: {respId: row.id,
					cfgType:type,
					subType:stype,
					status:statusubmit
				},
				success: function() {
					$('#asset_table').datagrid('reload'); // reload the user data
				},
				error: function() {
					showErrorMessage('状态更改失败');
				}
			});
		}
    } else {
    	showAlarmMessage('请选择需要操作的行操作');
    }
}

function deleteAllResult(){
	var rows = null;
	try{
		rows=$("#asset_table").datagrid("getSelected");
		if(null==rows){
			rows='';
		}
    }catch(e){
    	rows='';
    }
	var deleteId='';
	if (rows.length<1) {
        try{
        	deleteId=getTreeNodeId();
        }catch(e){ }
    } else {
    	try{
        	deleteId=rows.id;
        }catch(e){
        	try{
            	deleteId=getTreeNodeId();
            }catch(e1){  }
        }
    }
    if(deleteId.length<30){
    	return;
    }
    $.messager.confirm('提示', '确定要清空吗?', function(r) {
        if (r) {
            $.ajax({
                url: '/sim/mgrPlanReport/delPlanResultsByRespId',
                data: {
                	respId:deleteId
                	},
                success: function(result) {
                	if("success"==result){
                		showAlertMessage( "清空成功!" );
                    	$('#planresulttable').datagrid('reload');
                    }else{
                    	showErrorMessage("清空数据失敗!");
                    }
                    $('#planresulttable').datagrid('reload'); // reload the user data
                    
                },
                error: function() {
                	showErrorMessage('清空数据失败');
                }
            });
        }
    });
}

function deleteResultCont(){
	
	var rows = $("#planresulttable").datagrid("getSelections");
	
	if (rows.length<1) {
		showAlarmMessage( '请选择要删除的行后再进行删除操作');
    } else {
    	var respResultIdvalues='';
    	for(var i=0;i<rows.length;i++){
    		respResultIdvalues+=(rows[i].id+"ADDdelID");
    	}
    	
        $.messager.confirm('警告', '确定要删除选中行吗?', function(r) {
            if (r) {
                $.ajax({
                    url: '/sim/mgrPlanReport/removePlanTaskResult',
                    data: {//respId: deleteId,
                    	cfgType:type,
                    	subType:stype,
                    	respResultIds:respResultIdvalues
                    	},
                    success: function(result) {
                    	if("success"==result){
                    		showAlertMessage("删除成功!");
                        	$('#planresulttable').datagrid('reload'); 
                        }else{
                        	showErrorMessage("删除数据失敗!");
                        }
                        $('#planresulttable').datagrid('reload'); // reload the user data
                        
                    },
                    error: function() {
                    	showErrorMessage('删除数据失败');
                    }
                });
            }
        });
    }
}

loadXML = function(xmlString){
    var xmlDoc=null;
    //判断浏览器的类型
    //支持IE浏览器 
    if(!window.DOMParser && window.ActiveXObject){   //window.DOMParser 判断是否是非ie浏览器
        var xmlDomVersions = ['MSXML.2.DOMDocument.6.0','MSXML.2.DOMDocument.3.0','Microsoft.XMLDOM'];
        for(var i=0;i<xmlDomVersions.length;i++){
            try{
                xmlDoc = new ActiveXObject(xmlDomVersions[i]);
                xmlDoc.async = false;
                xmlDoc.loadXML(xmlString); //loadXML方法载入xml字符串
                break;
            }catch(e){
            }
        }
    }
    //支持Mozilla浏览器
    else if(window.DOMParser && document.implementation && document.implementation.createDocument){
        try{
            /* DOMParser 对象解析 XML 文本并返回一个 XML Document 对象。
             * 要使用 DOMParser，使用不带参数的构造函数来实例化它，然后调用其 parseFromString() 方法
             * parseFromString(text, contentType) 参数text:要解析的 XML 标记 参数contentType文本的内容类型
             * 可能是 "text/xml" 、"application/xml" 或 "application/xhtml+xml" 中的一个。注意，不支持 "text/html"。
             */
            domParser = new  DOMParser();
            xmlDoc = domParser.parseFromString(xmlString, 'text/xml');
        }catch(e){
        }
    }
    else{
        return null;
    }

    return xmlDoc;
};
loadXML2 = function(xmlFile){
    var xmlDoc=null;
    //判断浏览器的类型
    //支持IE浏览器
    if(!window.DOMParser && window.ActiveXObject){
        var xmlDomVersions = ['MSXML.2.DOMDocument.6.0','MSXML.2.DOMDocument.3.0','Microsoft.XMLDOM'];
        for(var i=0;i<xmlDomVersions.length;i++){
            try{
                xmlDoc = new ActiveXObject(xmlDomVersions[i]);
                break;
            }catch(e){
            }
        }
    }
    //支持Mozilla浏览器
    else if(document.implementation && document.implementation.createDocument){
        try{
            /* document.implementation.createDocument('','',null); 方法的三个参数说明
             * 第一个参数是包含文档所使用的命名空间URI的字符串； 
             * 第二个参数是包含文档根元素名称的字符串； 
             * 第三个参数是要创建的文档类型（也称为doctype）
             */
            xmlDoc = document.implementation.createDocument('','',null);
        }catch(e){
        }
    }
    else{
        return null;
    }

    if(xmlDoc!=null){
        xmlDoc.async = false;
        xmlDoc.load(xmlFile);
    }
    return xmlDoc;
};
function formatterOperation(value,row,index){
	var btn = $('<a></a>').addClass('log_rule_table_row_btn').attr('plain','true');
	if(row.status === 'true')
		btn.attr('iconCls','icon-no').attr('title','禁用').attr("onclick","changeState()");
	else
		btn.attr('iconCls','icon-ok').attr('title','启用').attr("onclick","changeState()");
	return btn[0].outerHTML;
}

function emailFarmat(value,row,index) {
    if (value.indexOf('MAILadd')>0) {
        var divshow ='';  
        	var valshow=value.replace(/MAILadd/g,'\n');
        	if(valshow.length<=25){
        		divshow="<div id='showEmailList"+index+"'>"+valshow+"</div>";
        		return divshow;
        	}
        	var abbrtext="<abbr class='report-mail' title='"+valshow+"'>"+valshow.substring(0,25)+"...</abbr>";
        	divshow="<div id='showEmailList"+index+"' >"+abbrtext+"</div>";
            return divshow;
    } else {
        	if(value.length<=25){
        		divshow="<div id='showEmailList"+index+"' >"+value+"</div>";
        		return divshow;
        	}
        	var abbrtext="<abbr class='report-mail' title='"+value+"'>"+value.substring(0,25)+"...</abbr>";
        	divshow="<div id='showEmailList"+index+"' >"+abbrtext+"</div>";
        	return divshow;
    }
}
var checkboxId='';
function uncheckRowEvent(rowIndex,rowData){
	$("#checkbox"+rowIndex).attr("checked",false);
}
function checkRowEvent(rowIndex,rowData){
	try{$("#"+checkboxId).attr("checked",false);}catch(e){}
	$("#checkbox"+rowIndex).attr("checked",true);
	checkboxId="checkbox"+rowIndex;
	
}
//function unselectRowEvent(rowIndex,rowData){
//	$("#checkbox"+rowIndex).attr("checked",false);
//}
//function selectRowEvent(rowIndex,rowData){
//	$("#checkbox"+rowIndex).attr("checked",true);
//}
var status=0;
function showEmailList(rowIndex, field, value){
	//$('abbr').hide();
	if('reportMailList'==field){
		var valshow='';
//		var valArr=[''];
//		var pdivs=null;
		if (value.indexOf('MAILadd')>0) {
//			valArr=value.split('MAILadd');
//			pdivs='';
//			var brdiv='<br>';
//			for(var i=0;i<valArr.length;i++){
//				pdivs+='<font>'+valArr[i]+'</font>';
//				pdivs+=brdiv;
//			}
//			valshow=valArr[0];
//			pdivs=pdivs.substring(0, pdivs.length-4);
        	valshow=value.replace(/MAILadd/g,'\n');
			
        } else {
        	valshow=value;
        }
		
        if(status++==1&&valshow.length>25){
        	var valshowtext=valshow.substring(0,25);
            var abbrtext=$("<abbr class='report-mail' title='"+valshow+"'>"+valshowtext+"...</abbr>");
//            valshow=valshow.substring(0,25);
            status=0;
            $("#showEmailList"+rowIndex).parent().css({"white-space":"nowrap"});
            $("#showEmailList"+rowIndex).css({"width":"150px"},{"white-space":"normal"}).empty().append(abbrtext);//text(valshow)
            return;
        }
//        if(status1++==1&&valArr.length>1){
//        	$("#showEmailList"+rowIndex).parent().empty().css({"height":"50px"},{"white-space":"normal"}).text(pdivs);
//        	status1=0;
//        	$("#showEmailList"+rowIndex).empty().css({"width":"150px"}).text(pdivs);
//        	return;
//        }//,{"overflow":":auto"}
//		var y = $(this).offset().left;
//		var spand=$('<span>'+valshow+'</span>');
//        $("#showEmailList"+rowIndex).parent().css({"height":"24px"});
        $("#showEmailList"+rowIndex).parent().css({"white-space":"normal"});
		$("#showEmailList"+rowIndex).css({"width":"150px"}).text(valshow);
		//$("#report_email_tips").hide();
	}
	if(status>2){status=0;}//$("#report_email_tips").show();
}
function resizeDialog(width, height){
	var left = $('#scanResultTabId').panel('panel').position().left;
	if(planReport_dialog){
	planReport_dialog.dialog('resize',{
		width: width,
		left: left
	});
	}
}