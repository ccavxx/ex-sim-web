/**
 * @author:WangZhiai
 */
var type="config.schedule";
var stype="config.schedule.report";
treeNode='0';
treeNodeIdVal='0';
/*计划报表命名空间*/
var planReportObj = {};
var planReportFunction={};
var planConfig={};

function simulationCheckbox(value, row,index){
	return "<input type='checkbox' id='checkbox"+index+"' value='checkbox'> ";
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

function emailArray(objId){
	var selectObj = document.getElementById(objId);
	var emailArr=[];
	if(selectObj.options.length < 1){
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "error",msg: "邮件地址为空"});
	} else if(selectObj.options.length > 30){
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "error",msg: "邮件个数不得大于30"});
	} else {
		planReportObj.planReportFormValidation.showMsg( $(selectObj), {type: "ok",msg: " "});
		for(var i=0;i<selectObj.options.length;i++){
			emailArr[i]=selectObj.options[i].value;
		}
	}
	return emailArr;
}

function addPlanConfig(savetype){
	if(! planReportObj.validatPlanReport(savetype)){
		return;
	}
	
	$.extend($.fn.tree.methods, {
		getLevel:function(jq,target){
			var l = $(target).parentsUntil("ul.tree","ul");
			return l.length+1;
		}
	});
	
	planConfig.configType='config.schedule';
	planConfig.subConfigType='report.schedule.report';
	planConfig.taskName=$('#planname').val();
	planConfig.scheduleType=$('#runTime').val();
	planConfig.month=$('#month').val();
	planConfig.day=$('#day').val();
	planConfig.date=$('#date').val();
	planConfig.hour=$('#hour').val();
	planConfig.min=$('#min').val();
	planConfig.reportConfigType=$('#reportConfigType').combobox('getValue');
	
	var planReportType=$('#planReportType');
	var planTree = planReportType.combotree('tree');
	var planNode = planTree.tree('getSelected');
	if(null == planNode)return;
	var level =  planTree.tree("getLevel",planNode.target);
	var parent = planTree.tree("getParent",planNode.target);
	
	if(3 == level){
		planConfig.parentReportId=planNode.id;
		planConfig.reportName=planNode.text;
		planConfig.deviceIp='127.0.0.1';
		planConfig.securityObjectType=parent.id;
	}else if(5 == level){
		planConfig.deviceIp=planNode.id;
		planConfig.parentReportId=parent.id;
		planConfig.reportName=parent.text;
		planConfig.resourceId=planNode.attributes.resourceId;
		var parentParent = planTree.tree("getParent",parent.target) ;
		planConfig.securityObjectType=parentParent.id;
		
	}
	var root =planTree.tree("getRoot",planNode.target) ;
	planConfig.nodeId=root.attributes.nodeId;
	planConfig.roleAccount=root.attributes.username;
	planConfig.reportType=$("#report_type").combobox('getValue');
	planConfig.reportTopn=$("#report_topn").combobox('getValue');
	planConfig.reportFiletype=$("#report_filetype").combobox('getValue');
	planConfig.reportMaillist=emailArray('report_maillist');
	planConfig.reportUser=$("#report_user").val();
	
	$.ajax({
        url: '/sim/planExecute/savePlanConfig',
        type: 'POST',
        data: JSON.stringify(planConfig),
        dataType:'json',
        contentType:"text/javascript",
        success: function(jsondata){
        	if(jsondata.saveResult) {
            	showAlertMessage("保存成功!");
            	simHandler.onClickMenuTp('new_plan_menu','/page/newreport/plan_report.html');
            } else {
            	showErrorMessage("保存失败!");
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
		formValidation.showMsg( $timeGroup, {type: "error",msg: "不能为空"});
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
	
	if(operatorVal=="add"){
		// 计划报表类型、主题类型
		emptyFlag = planReportObj.validPlanReportComboxNotnull(["reportConfigType","planReportType"], planReportObj.planReportFormValidation);
		if(!emptyFlag){
			return false;
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
planReportFunction.cancelBtn=function(){
	simHandler.onClickMenuTp('new_plan_menu','/page/newreport/plan_report.html');
//	simHandler.changeMenu(event,'/page/report/planReport.html');
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
	
	var hasCustomReportRole=result['hasCusReoprtRole'];
	
	if(hasCustomReportRole){
		return [{
	        "id": "schedule_cfg_newreport",
	        "text": "基本报表"
	    }, {
	        "id": "schedule_cfg_customreport",
	        "text": "自定义报表"
	    }];
	}else{
		return [{
	        "id": "schedule_cfg_newreport",
	        "text": "基本报表"
	    }];
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
    
	$('#reportConfigType').combobox({
	    url: '/sim/basicreport/reportRole',
	    loadFilter:function(datas){
	     	return cusReportRoleData(datas);
	    },    
	    valueField: 'id',
	    textField: 'text',
	    onChange:function(newValue,oldValue){
	    	$("#showReportdiv").css("display","block");
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
        }, {
            "id": "html",
            "text": "html文件"
        }],
	    valueField: 'id',
	    textField: 'text'
	});

});

function changeConfig(){
	var reportTId=$('#reportConfigType').combobox('getValue');
	if(reportTId=='schedule_cfg_newreport'){
		basereportcombo();
	}else if(reportTId=='schedule_cfg_customreport'){
		customreportcombo();
	}
	
	$("#planReportTypediv").css("display","block");
	$("#planReportTypediv2").css("display","block");
}

function basereportcombo(){
	$("#report_type_tr").css("visibility","visible");
	$("#report_topn_tr").css("visibility","visible");
	var planReportType=$('#planReportType');
	planReportType.combotree( {  
	    //获取数据URL  
	    url : '/sim/basicreport/reportComboTree',
//	    disabled:true,
//	    selectOnNavigation:true,
//	    checkbox:true,
//	    onlyLeafCheck:true,
//	    cascadeCheck: false,
//	    multiple: true,
	    onSelect : function(node) {  
	        var tree = $(this).tree;  
	        var isLeaf = tree('isLeaf', node.target);
	        if (!isLeaf) {  
	            //清除选中  
	        	planReportType.combotree('clear');
	        }
	    }
	});
	
}

function customreportcombo(){
	$("#report_type_tr").css("visibility","visible");
	$("#report_topn_tr").css("visibility","visible");
	$('#planReportType').combotree('loadData', []);

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
			planConfig.responseId=row.id;
	    	
	    	$('#createPlanReporter').css("display","none");
	    	$('#editPlanReporter').css("visibility","visible");
	    	$('#showPlanReport').dialog('expand',true).dialog('setTitle', '编辑信息');

	    	$('#planReportFrom').form('load', row);
	    	$('#showReportdiv').css("display","block");
	        $.ajax({
	            url: '/sim/planExecute/showPlanReport',
	            type: "POST",
	            dataType: "json",
	            data: {'respId': row.id
	            	},
	            success: function(dats) {
	            	
	            	if($("#reportConfigType").combobox('getValue')){
	            		$("#report_type_tr").css("display","none");
	            		$("#report_topn_tr").css("display","none");
	            	}else{
	            		$("#report_type_tr").css("display","inline");
	            		$("#report_topn_tr").css("display","inline");
	            	}
	            	
	            	var report_maillist= dats.reportMaillist;
	            	
	            	if(report_maillist.length>0){
	            		try{
	            			jsRemoveAllOption(document.getElementById('report_maillist'));
	            			for(var i=0;i<report_maillist.length;i++){
	            				jsAddItemToSelect(document.getElementById('report_maillist'), report_maillist[i],report_maillist[i]);
	                		}
	            		}catch(e){}
	            	}
	            	selectchange(dats.scheduleType);
	            	
	            	try{
	            		$('#planReportTypediv').css("display","block");
		            	$('#planReportTypediv2').css("display","block");
		            	var planReportType=$('#planReportType');
		            	planReportType.combotree( {  
		            	    //获取数据URL  
		            	    url : '/sim/basicreport/reportComboTree',
		            	    onSelect : function(node) {  
		            	        var tree = $(this).tree;  
		            	        var isLeaf = tree('isLeaf', node.target);
		            	        if (!isLeaf) {  
		            	        	planReportType.combotree('clear');
		            	        }
		            	    },
		            	    onLoadSuccess:function(node, data){
		            	    	var planTree = planReportType.combotree('tree');
		            	    	
				            	var selectNode = planTree.tree("find",dats.securityObjectType) ;
				        		var children = planTree.tree("getChildren",selectNode.target) ;
				        		var treeroot=planTree.tree("getRoot",selectNode.target) ;
		            	    	var roleIds=treeroot.attributes.roleIds;
				        		var username=treeroot.attributes.username;
				        		/**start
				        		 * 此处的作用是编辑的时候普通管理员设备 的全部 变为 operator 的全部
				        		 */
				        		if(53 ==roleIds && 'operator'== username && 'ALL_ROLE_ADDRESS' == dats.deviceIp){
				        			dats.deviceIp='ONLY_BY_DVCTYPE';
				        		}
				        		var i=0;
				        		while(i<children.length && null !=children){
				        			
				        			var subnode=children[i];
				        			i++;
				        			if(planTree.tree('isLeaf',subnode.target) ){
				        				if(dats.parentReportId==subnode.id){
				        					planReportType.combotree('setValue',subnode.id);
					        				planTree.tree("select",subnode.target);
							        		planTree.tree('expand',selectNode.target);
							        		
							        		break;
				        				}else if(dats.deviceIp==subnode.id){
				        					var selectNodeParent = planTree.tree("getParent",selectNode.target) ;
				        					var subnodeParent = planTree.tree("getParent",subnode.target) ;
				        					if(dats.parentReportId==subnodeParent.id){
				        						planReportType.combotree('setValue',subnode.id);
				        						planTree.tree("select",subnode.target);
				        						
						        				planTree.tree('expand',selectNodeParent.target);
								        		planTree.tree('expand',selectNode.target);
								        		planTree.tree('expand',subnodeParent.target);
								        		break;
				        					}
				        				}
				        				
				        			}else if(dats.parentReportId==subnode.id){
				        				children = planTree.tree("getChildren",subnode.target) ;
				        				i=0;
				        			}
				        		}
//				        		planReportType.combotree('disable');
		            	    }
		            	});
		            	
	            	}catch(er){
	            		$('#planReportTypediv').css("display","none");
		            	$('#planReportTypediv2').css("display","none");
		            	$("#planReportType").combotree('disable');
	            	}
	                $("#report_user").val(dats.reportUser);
	                $("#report_mail").val(report_maillist[0]);
	                $("#report_type").combobox('setValue',dats.reportType);
	                $("#report_topn").combobox('setValue',dats.reportTopn);
	                $("#reportConfigType").combobox('disable');
	                $("input[disabled]").css("background-color","#eeeeee");
	                $("#hour").numberbox("setValue",dats.hour);
	                $("#min").numberbox("setValue",dats.min);
	                $("#date").numberbox("setValue",dats.date);
	                $("#day").val(dats.day);
	                $("#month").val(dats.month);
	                
	                $("#report_filetype").combobox('setValue',dats.reportFiletype);
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
function downloadReport(selRowIndex){
	
	try{
		
		var rows = $('#asset_table').datagrid('getRows');
		var row = rows[selRowIndex];

		if (row) {
			planConfig.responseId=row.id;
			var strUrl='/sim/planExecute/downloadPlanResult?respId='+row.id;
			$('#scanResultTabId iframe').remove();

			var iframe = $("<iframe>");
			iframe.attr('src', strUrl);
			iframe.attr('style', 'display:none');
			$('#scanResultTabId').append(iframe);
	        
	    } else {
	        return;
	    }
	}catch(err){}
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
    }else{
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

var status=0;
function showEmailList(rowIndex, field, value){
	//$('abbr').hide();
	if('reportMailList'==field){
		var valshow='';

		if (value.indexOf('MAILadd')>0) {

        	valshow=value.replace(/MAILadd/g,'\n');
			
        } else {
        	valshow=value;
        }
		
        if(status++==1&&valshow.length>25){
        	var valshowtext=valshow.substring(0,25);
            var abbrtext=$("<abbr class='report-mail' title='"+valshow+"'>"+valshowtext+"...</abbr>");
            status=0;
            $("#showEmailList"+rowIndex).parent().css({"white-space":"nowrap"});
            $("#showEmailList"+rowIndex).css({"width":"150px"},{"white-space":"normal"}).empty().append(abbrtext);//text(valshow)
            return;
        }

        $("#showEmailList"+rowIndex).parent().css({"white-space":"normal"});
		$("#showEmailList"+rowIndex).css({"width":"150px"}).text(valshow);
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