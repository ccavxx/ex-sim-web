/**
 * 通知方式响应
 */
var sysInformp = {editRule:null};
function EvtCfgResponse() {
	this.respChoice = null;
}
var responId='';
var emailListValue='';
function setEmailList(){
	emailListValue='';
	var mList='';
	try{
		mList=getselectAlloption("emaillist");
	}catch(e){
		return false;
	}
	if(mList.length<2){
		showAlertMessage("邮件地址为空");
		return false;
	}
	emailListValue=mList;
	return true;
}

function getselectAlloption(objId){
	var selectObj=document.getElementById(objId);
	var mailList='';
	if(selectObj.options.length<1){
		return '';
	}else{
		
		mailList=selectObj.options[0].value;
		
		for(var i=1;i<selectObj.options.length;i++){
			mailList+=(", "+selectObj.options[i].value);
		}
	}
	
	return mailList;
}

EvtCfgResponse.prototype.initCfgTabs=function(){
	var _this=this;
	try{
		$(inform_choice_form.allResponse).empty();
		_this.load_resp();
	}catch(ere){}
};


EvtCfgResponse.prototype.select_resp = function(r) {
	if (this.respChoice) {
		for ( var k in this.respChoice) {
			var c = this.respChoice[k];
			if (c.name && c.name == r) {
				var items = c.items;
				// 添加界面
				this.handRespItem(items);
				break;
			}
		}
	}
};

EvtCfgResponse.prototype.finditem_by_name = function(n,items) {
	for ( var k in items) {
		if (items[k].name == n) {
			return items[k];
		}
	}
};

EvtCfgResponse.prototype.handRespItem = function(items) {
	reInitValiSysInformp();
	var $ct = $("<table></table>"),
		_this=this;
	$("#resp_inform_show_div").empty().append($ct);
	var rows = [];

	$.each(items, function(i, item) { 
		var row = {
			"c1" : item.label,
			"c2" : item.name,
			"requried":item.requried
		};
		rows.push(row);
	});
	$ct.datagrid({
		data : rows,
		border:false,
		//singleSelect : true,
		showHeader : false,
		nowrap : false,
		onClickRow:function(rowIndex, rowData){
			$ct.datagrid("unselectRow",rowIndex);
		},
		columns : [ [ {
			field : 'c1',
			title : 'v1',
			width : 100,
			align:'right',
			formatter:function(value,row,index){
				if(row.requried){
					return "<span style='color:red;margin-right:2px;'>*</span>"+value;
				}
				else{
					return value;
				}
			}
		}, {
			field : 'c2',
			title : 'v2',
			width : 390,
			formatter : function(value, row, index) {
				var strpan = "<span id='___" + row.c2 + "'></span>";
				return strpan;
			},
			resizable : true
		} ] ]
	});

	/** 添加操作组件* */
	var h = 0;
	$.each(rows, function(j, row) {
		var c2 = row.c2, $span = $("#___" + c2), c = _this.finditem_by_name(c2,items);
		if (c.showInput && c.dataType == "String" && !c.onclick) {
			sysInformp.event_resp_add_form_validator.setField(c2,"required;length[~30];");
			$("<input type='text' id='"+c2+"' name='" + c2 + "' style='width:218px;'/>").appendTo($span);
			h = h + 45;
		}
		if (c.showInput && c.dataType == "String" && c.onclick) {
			if(c.name=="email"){
				sysInformp.event_resp_add_form_validator.setField(c2,"email;length[1~30];");
				$("<div class='input-append'><input id='email' name='"+ c2 +"' style='width:196px;' type='text'/><span class='add-on' style='cursor: pointer;' onclick='jsAddItemToSelect(document.getElementById(\"emaillist\"), $(\"#email\")[0].value,$(\"#email\")[0].value, null, this);'><i class='icon-plus'></i></span><span class='msg-box n-right' style='position:static;' for='email'></span></div>").appendTo($span);
			}else if(c.name=="phone"){
				sysInformp.event_resp_add_form_validator.setField(c2,"mobile;");
				$("<div class='input-append'><input id='phone' name='phone' style='width:196px;' type='text'/><span class='add-on' style='cursor: pointer;' onclick='jsAddItemToSelect(document.getElementById(\"phonelist\"), $(\"#phone\")[0].value,$(\"#phone\")[0].value, null, this);'><i class='icon-plus'></i></span><span class='msg-box n-right' style='position:static;' for='phone'></span></div>").appendTo($span);
			}
			h = h + 45;
		}
		if (c.showInput && c.dataType == "password") {
			sysInformp.event_resp_add_form_validator.setField(c2,"required;length[~30];");
			$("<input type='password' id='"+c2+"' name='" + c2 + "' style='width:218px;'/>").appendTo($span);
			h = h + 45;
		}
		if (c.showInput && c.dataType == "Integer") {
			sysInformp.event_resp_add_form_validator.setField(c2,"required;length[~30];integer[+]");
			$("<input type='text' id='"+c2+"' name='" + c2 + "' style='width:218px;'/>").appendTo($span);
			h = h + 45;
		}
		if (c.showInput && c.dataType == "IP") {
			sysInformp.event_resp_add_form_validator.setField(c2,"required;ipv4;");
			$("<input type='text' id='"+c2+"' name='" + c2 + "' style='width:218px;'/>").appendTo($span);
			h = h + 45;
		}
		if (c.showInput && c.dataType == "text" && !c.isSelect) {
			sysInformp.event_resp_add_form_validator.setField(c2,"length[~100];");
			$("<textarea id='"+c2+"' name='" + c2 + "' style='height:40px;width:206px;'/>").appendTo($span);
			h = h + 45 * 3 / 2;
		}
		if (c.showInput && c.dataType == "text" && c.isSelect) {
			if(c.name == "mailreceivers"){
				$("<div class='input-append'><select id='emaillist' name='" + c2 + "' multiple='multiple' style='height:60px;width:199px;' ></select><span class='add-on' style='vertical-align:middle; cursor: pointer;margin-top:15px;' onclick='jsRemoveSelectedItemFromSelect(document.getElementById(\"emaillist\"));' ><i class='icon-minus'></i></span><span class='msg-box n-right' style='position:static;' for='emaillist'></span></div>").appendTo($span);
			}else if(c.name == "phonelist"){
				$("<div class='input-append'><select id='phonelist' name='phonelist' multiple='multiple' style='height:60px;width:199px;' ></select><span class='add-on' style='vertical-align:middle; cursor: pointer;margin-top:15px;' onclick='jsRemoveSelectedItemFromSelect(document.getElementById(\"phonelist\"));' ><i class='icon-minus'></i></span><span class='msg-box n-right' style='position:static;' for='"+c2+"'></span></div>").appendTo($span);
			}
			h = h + 45 * 3 / 2;
		}
		if (c.showSelect && c.options) {
			//event_resp_add_form_validator.setField(c2,"length[~100];");
			var onchange = c2 == "smscomport" ? "evtResp.portChange('"+c2+"')" : "" ;
			var $select = $("<select id='"+c2+"' name='" + c2 + "' onchange=\""+onchange+"\" style='width:220px;' data-rule='required'></select>")
			$select.appendTo($span);
			if(c2 == "smscomport"){
				var actionNodeId = $("#actionNode").combobox("getValue") ;
				var $input = $("<input id='"+c2+"baudRate' name='smsbaudRate' type='hidden'/>");
				$.getJSON("/sim/sysconfig/event/getAvaliableComPort?nodeId="+actionNodeId+"&_time="+new Date().getTime(),
					function(data){
						if(data.success && data.result){
							var index = 0 ;
							var firstValue = null ;
							for(var port in data.result){
								if(index++ == 0){
									firstValue = port ;
								}
								$("#"+c2).append("<option value='"+port+"' baudRate='"+data.result[port]+"'>"+port+"</option>") ;
							}
							//选中编辑时的值或者第一条数据
							var selectValue = $("#"+c2).attr("selectValue") || firstValue;
							$("#"+c2).val(selectValue) ;
							$input.appendTo($span);
							_this.portChange(c2) ;
						} else {
							showErrorMessage(data.message);
						}
					}) ;
			}
			$.each(c.value, function(k, v) {
				$select.append("<option value='" + v + "'>" + c.options[k] + "</option>");
			});
			h = h + 45;
		}
		if(c.dataType=='label'){
			//$select.appendTo($span);
			h = h + 20;
		}
		
	});
	h=h+10;
	$ct.datagrid("resize", {
		width : 590,
		height : /* 45*rows.length */h
	});
};
EvtCfgResponse.prototype.portChange = function (portElementid){
	$("#"+portElementid+"baudRate").val($("#"+portElementid+" option:selected").attr("baudRate"));
};
EvtCfgResponse.prototype.load_resp_choice = function() {
	var _this = this;
	$.post("../../sim/sysconfig/event/jsondata", {
		"json" : "evt_resp_choice"
	}, function(json) {
		_this.respChoice = eval(json);
	});

};

EvtCfgResponse.prototype.load_resp=function(){
	$.getJSON("../../sim/sysconfig/event/allResponse?_time="+new Date().getTime(), function(json){
		  $.each(json,function(i,resp){
			  var option=new Option(resp.name,resp.id);
			  $(option).dblclick( function () {
				  var b=false;
				  $.each( inform_choice_form.responseIds.options,function(j,opt){
					  if(opt==option){
						 b=true;
						 return;
					  }
				  });
				  if(b){
					  /***
					   * 新整个规则时  如果当前通知已经切换 需要对应处理
					   */
					  if(evtRule.formData){
						  var responseIds=evtRule.formData.choice_form.responseIds;
						  for(var n=0;n<responseIds.length;n++){
							  if(responseIds[n]==resp.id){
								  evtRule.formData.choice_form.responseIds=responseIds.splice(n,0);
								  break;
							  }
							  
						  }
					  }
					  //
					  inform_choice_form.allResponse.options.add(this); 
				  }else{
					  inform_choice_form.responseIds.options.add(this);
				  }
			  }); 
			  
			  /**
			   * 判断是否已经在被选择列中
			   */
			  var selected=false;
			  $.each(inform_choice_form.responseIds.options,function(k,option1){
				  if(option1.value==resp.id){
					  selected=true;
					  return;
				  }
			  }); 
			  
			  if(!selected){//不在其中
				  inform_choice_form.allResponse.options.add(option);
			  }
			  
			  /****
			   * 更新整个规则时  如果当前已有选择的响应方式·······
			   */
			  if(evtRule.formData){
				var responseIds=evtRule.formData.choice_form.responseIds;
				$.each(responseIds,function(l,respId){
					if(respId==resp.id){
						var b=false;
						$.each(inform_choice_form.responseIds.options,function(m,option2){
							if(option2.value==respId){
								b=true;
								return;
							}
						});
						if(!b){
							inform_choice_form.responseIds.options.add(option);
						}
					}
				});
			  }
			  
		  });
	}); 
	
};

var isBlankEmailServerIPFlag = false;
EvtCfgResponse.prototype.isBlankEmailServerIP = function(){
	$.ajax({
		url: '/sim/mgrPlanReport/isBlankEmailServerIP',
		type:'post',
		dataType:"json",
		success: function(result) {
			isBlankEmailServerIPFlag = result.status;
		}
	});
}
EvtCfgResponse.prototype.add_resp=function(){
	EvtCfgResponse.prototype.isBlankEmailServerIP();
	setTimeout(function(){
		EvtCfgResponse.prototype.add();
	}, 500);
}
EvtCfgResponse.prototype.add=function(){
	var resp_type=event_resp_add_form.cfg.value;
	if('resp_cfg_mail'==resp_type && isBlankEmailServerIPFlag){
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
	}else{
		$('#event_resp_add_form_id').submit();
	}
};

EvtCfgResponse.prototype.test_resp=function(){
	EvtCfgResponse.prototype.isBlankEmailServerIP();
	setTimeout(function(){
		EvtCfgResponse.prototype.test();
	}, 500);
}

EvtCfgResponse.prototype.test=function(){
	var resp_name=event_resp_add_form.resp_name.value;//名称
	var resp_desc=event_resp_add_form.resp_desc.value;//描述
	var resp_type=event_resp_add_form.cfg.value;
	var resp_node=event_resp_add_form.resp_nodeId.value;
	var responseCfg={
					 nodeId:resp_node,
					 respDesc:resp_desc,
					 respCfgType:resp_type,
				 	 subType:"config.response.exec",
					 cfgType:"config.response",
					 cfgItems:[]
					};
	$("#event_resp_add_form_id").trigger("validate"); 
	var validateFlag = sysInformp.event_resp_add_form_validator.isFormValid();
	if(!validateFlag){
		return;
	}
	var elecfg=evtResp.finditem_by_name(resp_type,evtResp.respChoice);
	var selectValidFlag = false;
	if(elecfg){
		var items=elecfg.items;
		$.each(items,function(i,item){
			if(item.dataType!='label'){
				if(item.isSelect && item.submitAllValue){
					var allOption = new Array() ;
					var elementSelect = $("#event_resp_add_form_id [name='"+item.name+"']");
					$("#event_resp_add_form_id [name='"+item.name+"'] > option").each(function(){
							allOption.push($(this).val()) ;
						}
					) ;
					if(allOption.length>29){
						sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "error",msg: "至多存在30个选项"});
						selectValidFlag = true;
						return;
				 	}if(allOption.length<1){
				 		sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "error",msg: "至少存在一个选项"});
				 		selectValidFlag = true;
				 		return;
				 	}else{
				 		sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "ok",msg: " "});
				 	}
					responseCfg.cfgItems.push({"name":item.name,"splitToArray":true,"dataType":item.dataType,"value":allOption.join(";")}) ;
				}else{
					responseCfg.cfgItems.push({"name":item.name,"dataType":item.dataType,"value":event_resp_add_form[item.name].value});
				}
			}
		});
	}
	if(selectValidFlag){
		return;
	}
	$.post('/sim/mgrPlanReport/testResp?respId='+responId, responseCfg, function() {
		if('resp_cfg_mail'==resp_type && isBlankEmailServerIPFlag){
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
		}else{
			showAlertMessage("相应测试已下发");
		}
	},"json");
};


EvtCfgResponse.prototype.cancelInformBtn=function(){
	try{
		$("#sysconf_inform_dialog").dialog('close');
		$('#sysconf_inform_table').datagrid('reload');
	}catch(e){}
};

sysInformp.changeState = function(status){

	var selrows = $("#sysconf_inform_table").datagrid('getChecked');

	if(selrows.length > 0) {
		var ids = "";
		$.each(selrows, function(index, selrow){
			if(selrow.start !== status){
				var id = selrow.id;
				ids += (id + ",");
			}
		});
		if(ids.length > 1){
			ids = ids.substring(0, ids.length-1);
			$.ajax({
				url: '/sim/mgrPlanReport/changeBatchStates',
				type:'post',
				data: {respIds: ids, subType:"config.response.exec", cfgType:"config.response", status:status},
				dataType:"json",
				success: function(result) {
					if(result.status) {
						reloadSysInformTable();
					} else {
						showErrorMessage(result.message);
					}
				},
				error: function(e) {
					showErrorMessage('状态更改失败');
				}
			});
    	}
	} else {
		showErrorMessage( '请选择一条数据' );
	}
};

var jsontypedata=null;
function settypename(){
    try{
    	$.ajax({
            url: '/sim/sysconfig/event/jsondata?json=evt_resp_options',
            dataType:'json',
            success: function(data) {
            	jsontypedata= data;
            }
        });
    }catch(e){}
    return jsontypedata;
};

EvtCfgResponse.prototype.setResp=function(){
	_this=this;
	$("#eventRespType").combobox({
		editable:false,
	    url:'/sim/sysconfig/event/jsondata?json=evt_resp_options',
	    valueField: 'value',
	    textField: 'name',
	    onChange:function(newValue,oldValue){
	    	$("#resp_inform_show_div :input").each(function(index,el){
	    		sysInformp.event_resp_add_form_validator.setField(el.name,null) ;
			}) ;
	    	$("#hid_eventRespType").val(newValue).blur();
	    	_this.select_resp(newValue);
	    }
	});
};

var evtResp=new EvtCfgResponse();
(function(){
	evtResp.setResp();
	evtResp.load_resp_choice();
	evtResp.initCfgTabs();
})();

/**
 * 重新加载日志过滤规则列表
 */
function reloadSysInformTable(){
	$('#sysconf_inform_table').datagrid('reload');//重新加载本页
	$('#sysconf_inform_table').datagrid('unselectAll');//取消所有选择
	$('#sysconf_inform_table').datagrid('uncheckAll');//取消所有选择
	sysInformp.editRule = null;//每次刷新列表清空此值
}
/**
 * 打开日志过滤规则编辑窗口
 */
function openSysInformDialog(selectNode){
	
	var w = $('#sysconf_inform_panel').layout('panel','center').width();
	var h = $('#sysconf_inform_panel').layout('panel','center').height();
	
	var top = $('#sysconf_inform_table_panel').panel('panel').position().top;
	var left = $('#sysconf_inform_table_panel').panel('panel').position().left;
	if(sysInformp.event_resp_add_form_validator) {
		sysInformp.event_resp_add_form_validator.cleanUp();
	}
	var data = $('#actionNode').combobox('getData');
	$("#actionNode").combobox('select',selectNode ? selectNode : data[0].nodeId);
	$("#sysconf_inform_dialog").show();
	$("#sysconf_inform_dialog").dialog({
//		href:'/page/sysconfig/sysconfig_logfilter.html',
		style:{'padding':0,'border':0},
		top:top,
		left:left,
		width:w,
		height:h,
		inline:true,
		noheader:true,
		shadow:false,
		border:false,
		onClose:reloadSysInformTable
	});
	$("#sysconf_inform_dialog").dialog('expand',true);
}
function changevalue(valsta){
	if(undefined==valsta){
		return false;
	}else if('true'==valsta){
		return false;
	}else if('false'==valsta){
		return true;
	}
	return false;
}

function emailStringToArr(emailString){
	var resultArr = [];
	if(emailString.length < 1) {
		return resultArr;
	} else {
		if(emailString.indexOf('MAILadd') > 0) {
			resultArr = emailString.split('MAILadd');
		} else {
			resultArr[0] = emailString;
		}
		return resultArr;
	}
}
function initValiSysInformp() {
	sysInformp.event_resp_add_form_validator = $('#event_resp_add_form_id').validator({
		theme: 'simple_right',
		showOk: "",
		rules: simHandler.rules
	}).data( "validator" );
}
function reInitValiSysInformp() {
	var fields = {}
	$.each(sysInformp.event_resp_add_form_validator.fields, function(key, val){
		if(key != "cfg0" && key != "resp_desc" && key != "resp_name") {
			fields[key] = null;
		} else {
			fields[key] = val;
		}
	});
	$('#event_resp_add_form_id').validator('setField', fields);
}
$(function(){
	initValiSysInformp();
	$('#event_resp_add_form_id').submit(function(){
		var resp_name=event_resp_add_form.resp_name.value;//名称
		var resp_desc=event_resp_add_form.resp_desc.value;//描述
		var resp_type=event_resp_add_form.cfg.value;
		var resp_node=event_resp_add_form.resp_nodeId.value;
		var responseCfg={
						 respName:resp_name,
						 respDesc:resp_desc,
						 respCfgType:resp_type,
					 	 subType:"config.response.exec",
						 cfgType:"config.response",
						 cfgItems:[],
						 nodeId:resp_node
						};
		var validateFlag = sysInformp.event_resp_add_form_validator.isFormValid();
		if(!validateFlag){
			return;
		}
		var elecfg=evtResp.finditem_by_name(resp_type,evtResp.respChoice);
		var selectValidFlag = false;
		if(elecfg){
			var items=elecfg.items;
			$.each(items,function(i,item){
				if(item.dataType!='label'){
					if(item.isSelect && item.submitAllValue){
						var allOption = new Array() ;
						var elementSelect = $("#event_resp_add_form_id [name='"+item.name+"']");
						$("#event_resp_add_form_id [name='"+item.name+"'] > option").each(function(){
								allOption.push($(this).val()) ;
							}
						) ;
						if(allOption.length>29){
							sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "error",msg: "至多存在30个选项"});
							selectValidFlag = true;
							return;
					 	}if(allOption.length<1){
					 		sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "error",msg: "至少存在一个选项"});
					 		selectValidFlag = true;
					 		return;
					 	}else{
					 		sysInformp.event_resp_add_form_validator.showMsg(elementSelect,{type: "ok",msg: " "});
					 	}
						responseCfg.cfgItems.push({"name":item.name,"splitToArray":true,"dataType":item.dataType,"value":allOption.join(";")}) ;
					}else{
						responseCfg.cfgItems.push({"name":item.name,"dataType":item.dataType,"value":event_resp_add_form[item.name].value});
					}
				}
			});
		}
		if(selectValidFlag){
			return;
		}
		$.ajax({
		    type: "post",
		    url: "/sim/sysconfig/event/addResponse?respId="+responId,
		    dataType:"json",
		    data:responseCfg,
		    success: function(result) {
	        	if("success"==result.result) {
            		$("#sysconf_inform_dialog").dialog('close');
            		$('#sysconf_inform_table').datagrid('reload');
	            } else if(result.message) {
	            	showErrorMessage(result.message);
	            } else {
	            	showErrorMessage("添加失败!");
	            }
		    }
		});
	});

	settypename();
	/**
	 * 编辑按钮
	 */
	sysInformp.formatterOperation = function(value,row,index){
		 return "<a title='编辑' style='width:16px;height:16px;cursor: pointer;' class='icon-edit' onclick='sysInformp.editSysInform(\""+index+"\")'></a>";
	}
	/**
	 * 初始化过滤规则列表
	 */
	$('#sysconf_inform_table').datagrid({   
	    url : '/sim/sysconfig/event/showAllInform',
		idField : 'id',
		fit : true,
		nowrap : false,
		striped : true,
		fitColumns : true,
		pagination : true,
		rownumbers : true,
		singleSelect : true,
		checkOnSelect : false,
		selectOnCheck : false,
	    columns:[[
	        {field:'ck',checkbox:true},
	        {field:'name',title:'名称',width:60,sortable:true},
	        {field:'cfgKey',title:'响应方式',width:50,sortable:true,formatter:formattercfgKey},
	        {field:'node',title:'节点',width:50},
	        {field:'desc',title:'描述',width:100},
	        {field:'start',title:'状态',width:40,sortable:true,formatter:formatterStatus},
	        {field:'creater',title:'创建人',width:20},
	        {field:'opera',title:'操作',width:20,align:'center',formatter:sysInformp.formatterOperation}
	    ]],
	    toolbar:'#sysconf_inform_table_toolbar'
	});  	
	
	function formattercfgKey(value,row,index){
		if(undefined==jsontypedata||null==jsontypedata){
			return value;
		}
		for(var i=0;i<jsontypedata.length;i++){
			if(value==jsontypedata[i].value){
				return jsontypedata[i].name;
			}
		}
		
	}
	
	/**
	 * 格式化状态显示
	 */
	function formatterStatus(value,row,index){
		return value ? "启用" : "禁用";
	}

	/**
	 * 删除告警方式
	 */
	sysInformp.removeSysInform = function(){
		
		var selrows = $("#sysconf_inform_table").datagrid('getChecked');

		if(selrows.length > 0) {
			var ids = "";
			$.each(selrows, function(index, row){
				var deleteId = row.id;
				ids += deleteId;
				if(index != selrows.length-1){
					ids += ",";
				}
			});
	    	
	        $.messager.confirm('警告', '确定要删除选中行吗?', function(r){
	            if(r) {
	                $.ajax({
	                    url: '/sim/mgrPlanReport/removeBatchPlanReport',
	                    data: {'respIds': ids, subType:"config.response.exec", cfgType:"config.response"},
	                    dataType : "json",
	                    success: function(result) {
	                    	if(result.status) {
	                    		showAlertMessage("删除成功!");
	                        } else {
	                        	showErrorMessage(result.message);
	                        }
	                    	reloadSysInformTable();
	                    },
	                    error: function(e) {
	                    	showErrorMessage('删除数据失败');
	                    	reloadSysInformTable();
	                    }
	                });
	            }
	        });
		} else {
			showAlarmMessage( '请选择要删除的行后再进行删除操作' );
		}
	}
	
	/**
	 * 增加过滤规则
	 */
	sysInformp.addSysInform = function(){
		$('#event_resp_add_form').form('clear');
		$("#resp_name").val("");
		$("#resp_desc").val("");
		$("#eventRespType").combobox('enable');
		$("#actionNode").combobox('enable');
		$("#eventRespType").combobox('setValue','');
		$("#hid_eventRespType").val('');
		$("#resp_inform_show_div").empty();
		responId='';
		openSysInformDialog();
	}

	/**
	 * 修改通知方式
	 */
	sysInformp.editSysInform = function(selRowIndex){
		var rows = $('#sysconf_inform_table').datagrid('getRows');
		var selrow = rows[selRowIndex];
		if(selrow){
			$("#actionNode").combobox('setValue',selrow.nodeId);
			evtResp.setResp();
			sysInformp.editRule = selrow;
			$("#event_resp_add_form").form('load', selrow);
//			$("#resp_name").val(selrow.name);
//			$("#resp_desc").val(selrow.desc);
			$("#eventRespType").combobox('setValue',selrow.cfgKey);
			
			$("#hid_eventRespType").val(selrow.cfgKey);
			$("#eventRespType").combobox('disable');
			$("#actionNode").combobox('disable');
			openSysInformDialog(selrow.nodeId);
			//$("#actionNode").combobox('setValue',);
			responId=selrow.id;
			
			$.ajax({
	            url: '/sim/sysconfig/event/showOneInform',
	            type: "POST",
	            dataType: "json",
	            data: {'respId': responId},
	            success: function(dats) {
	            	var sonTask= dats.plantaskAttr;
	            	$("#resp_name").val(dats.plantask.name);
	    		$("#resp_desc").val(dats.plantask.desc);
	            	for (var key in sonTask){
	            		if("mailreceivers"==key){
	            			var taskEmaillist= emailStringToArr(sonTask["mailreceivers"]);
	    	            	if(taskEmaillist.length>0){
	    	            		try{
	    	            			jsRemoveAllOption(document.getElementById('emaillist'));
	    	            			for(var i=0;i<taskEmaillist.length;i++){
	    	            				jsAddItemToSelect(document.getElementById('emaillist'), taskEmaillist[i],taskEmaillist[i]);
	    	                		}
	    	            		}catch(e){}
	    	            		$("#email").val(taskEmaillist[0]);
	    	            	}
	    	            	
	            		}else if("phonelist"==key){
	            			var taskEmaillist= emailStringToArr(sonTask["phonelist"]);
	    	            	if(taskEmaillist.length>0){
	    	            		try{
	    	            			jsRemoveAllOption(document.getElementById('phonelist'));
	    	            			for(var i=0;i<taskEmaillist.length;i++){
	    	            				jsAddItemToSelect(document.getElementById('phonelist'), taskEmaillist[i],taskEmaillist[i]);
	    	                		}
	    	            		}catch(e){}
	    	            	}
	    	            	
	            		}else if("smscomport"==key){
	            			$("#smscomport").attr("selectValue",sonTask[key]) ;
	            		}else{
	            			$("#"+key).val(sonTask[key]);
	            		}
	            	}
	            	
	            },
	            error: function() {
	            	showErrorMessage('无法加载');
	            }
	        });
		}else{
			showErrorMessage( '请选择一条数据' );
		}
	}
	
});
