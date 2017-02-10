/**
 * 关联分析手动配置
 * @returns {EventRuleGroup}
 */
function EventRuleGroup(){
	this.currentGroupDispatch = null;// 关联事件配置当前配置数据
	this._windowArray = ["knowledge_datagrid_id"];// 添加、编辑页面中的 window id 集合，方便返回时关闭
}

/**
 * 关闭 （添加、编辑）关联分析 窗口
 */
EventRuleGroup.prototype.closeAddOrEditDialog = function() {
	
	var _this = this;
	
	$.each(_this._windowArray, function(index, id) {
		
		if($("#"+id).window) {
			
			$("#"+id).window("destroy");
		}
		if($(".menu-top.menu").length > 0){
			
			$(".menu-top.menu").remove();
		}
	});
	$('#al_add_evtrule_group').dialog("close");
}

/**
 * 打开 （添加、编辑）关联分析 窗口
 */
EventRuleGroup.prototype.openAddOrEditDialog = function(oper) {

	var _this = this;
	
	
	var w = $('#eventrule_panel').layout('panel','center').width();
	var h = $('#eventrule_panel').layout('panel','center').height();
//	var left = $('#evtrule_group_main_panel_id').panel('panel').position().left;
//	var top = $('#evtrule_group_main_panel_id').panel('panel').position().top;
	$('#al_add_evtrule_group').dialog({
	    href : '/page/sysconfig/eventRuleGroup_mainForm.html',
		style : {'padding':0, 'border':0},
		top : 0,
		left : 0,
		width : w,
		height : h + 30,
		shadow : false,
		inline : true,
		noheader : true,
	    onClose : function() {
	    	//$(this).dialog("destroy");
	    	
	    	if($('#al_add_evtrule_group').length === 0){
	    		$("#evtrule_group_main_panel_id").append('<div id="al_add_evtrule_group"></div>');
	    	}
	    	if(oper === "edit") {
	    		_this.currentGroupDispatch = null;
	    	}
		},
		onBeforeClose:function() {
			if(oper === "edit") {
				_this.currentGroupDispatch = null;
			}
		}
	});
}

/**
 * 加载一级分类
 */
EventRuleGroup.prototype.load_syscfg_evt_cat1 = function() {// 加载一级分类
	
	var _this = this;
	
	var _delete_f2 = function(){
	 	if(rule_group_prop_form._r_cat2.options) {
	 		
	 		if(rule_group_prop_form._r_cat2.options.length > 0) {
	 		
	 			rule_group_prop_form._r_cat2.options.remove(0);
	 			_delete_f2();
	 		}
	 	}
	};
	
	$.ajax({// 按id加载配置
	    type: "post",
	    url: "/sim/sysconfig/event/eventCategory",
	    dataType:"json",
	    async:false,
	    success: function(json){
	    	
	    	rule_group_prop_form._r_cat1.add(new Option("请选择", -1));
	    	rule_group_prop_form._r_cat2.add(new Option("请选择", -1));
	    	
	    	$.each(json,function(i, ctg){
	    		
	    		var option = new Option(ctg.categoryName, ctg.id);
	    		rule_group_prop_form._r_cat1.add(option);
	    	});
	    	
	    	$("#_r_cat1_id").change(function(){
	    		
				$.each($(this).children(), function(i, option) {
					
					if (option.selected) {
						
						_delete_f2();
						_this.load_syscfg_evt_cat2(rule_group_prop_form._r_cat1.value);
					}
				});
			});
	    }
	});
};

/**
 * 加载二级分类
 * @param id
 */
EventRuleGroup.prototype.load_syscfg_evt_cat2 = function(id) {
	
	var _this = this;
	
	$.ajax({// 按id加载配置
	    type: "post",
	    url: "/sim/sysconfig/event/eventCategory",
	    dataType:"json",
	    data:{
			"cattype" : "ec2",
			"id" : id
		},
	    async:false,
	    success: function(json){
	    	
	    	_this.category2 = [];
	    	rule_group_prop_form._r_cat2.add(new Option("请选择", -1));
	    	if(json){
	    		$.each(json,function(i, ctg) {
		    		var option = new Option(ctg.categoryName,ctg.id);
		    		rule_group_prop_form._r_cat2.add(option);
		    	});
	    		
	    	}
	    	
	    }
	});
};

/**
 * 批量修改规则启用禁用状态
 */
EventRuleGroup.prototype.batchAlterStatus = function(_status) {
	
	var selections = $("#al_evtrule_group_table").datagrid("getChecked"),
		_this = this,
		_id = [];
	
	if(selections && selections.length > 0) {
		
		$.each(selections, function(i, r) {
			
			 _id.push(r.groupId);
		});
	} else {
		showAlarmMessage( '请选择一条数据' );
		return;
	}
	if(_id.length > 0) {
		
		$.ajax({
		    type: "post",
		    url: "/sim/sysconfig/event/alterRuleGroupStatus",
		    dataType:"json",
		    data:{status:_status, id:_id},
		    success: function(data){
		    	/**
		    	 * 刷新规则列表
		    	 */
		    	_this.showEventRuleGroupListReload();
		   }
		});
	} 
};

/**
 * 批量启用
 */
EventRuleGroup.prototype.batchSetUp = function(){
	this.batchAlterStatus(1);
};

/**
 * 批量禁用
 */
EventRuleGroup.prototype.batchTurnOff = function(){
	this.batchAlterStatus(0);
};

/**
 * 点击编辑规则加载当前规则所有数据
 */
EventRuleGroup.prototype.editEventRuleGroup = function(id,cloneEventGroup) {
	
	var _this = this;
	this.currentGroupDispatch = {"group" : null, "rule" : null, "assc" : null, "resp" : null, "knowledge" : null};
	this.cloneEventGroup = cloneEventGroup;//拷贝
	$.ajax({// 规则
		type : "post",
	 	url : "/sim/sysconfig/event/getEventRulesByGroupId",
	 	data : {groupId : id},
		//async : false,
		dataType : "json",
		success : function(data) {
			_this.currentGroupDispatch.group = data.group;
			_this.currentGroupDispatch.assc = data.assc;
			_this.currentGroupDispatch.resp = data.resp;
			_this.currentGroupDispatch.rule = data.rule;
			_this.currentGroupDispatch.knowledge = data.knowledge;
			_this.openAddOrEditDialog("edit");
		}
	});
};

/**
 * 显示规则列表
 */
EventRuleGroup.prototype.showEventRuleGroupListReload = function() {
	$("#al_evtrule_group_table").datagrid("reload");
};
//关联告警
EventRuleGroup.prototype.alarmCorrelation = function(){
	 var checkRows = $("#alarm_datagrid").datagrid("getChecked");
	 var alarmIds = "";
	 $.each(checkRows,function(index,item){
		 alarmIds += item.id;
		 if(index < checkRows.length -1 ){
			 alarmIds += ";";
		 }
	 });
	 var ruleId = $("#al_evtrule_group_table").datagrid("getSelected").groupId;
    $.ajax({
    	url:'/sim/sysconfig/event/addAlarmCorrelation',
    	data:{alarmIds:alarmIds,ruleId:ruleId},
    	dataType:'json',
    	success:function(data){
    		if(data.success){
    			eventRuleGroup.closeAlarmDialog();
    			eventRuleGroup.showEventRuleGroupListReload();
    		}else{
    			showAlertMessage("关联告警失败！");
    			return false;
    		}
    	}
    });
};
EventRuleGroup.prototype.onLoadSuccess = function(data){
	var ruleId = $("#al_evtrule_group_table").datagrid("getSelected").respIds.split(",");
	if(ruleId && ruleId.length > 0){
		if(data.rows.length > 0){
			$.each(ruleId, function(index, item){
				var rowIndex = eventRuleGroup.getRowIndex(data.rows,item);
				$("#alarm_datagrid").datagrid("checkRow",rowIndex) ;
			});
		}
	}
}
EventRuleGroup.prototype.getRowIndex = function(data,id){
	var rowIndex = -1 ;
	$.each(data,function(index,field){
		if(field.id == id){
			rowIndex = index ;
			return false ;//停止循环
		}
	}) ;
	return rowIndex ;
}
//打开关联告警列表
EventRuleGroup.prototype.openAlarmDialog = function(){
	$("#al_alarm_dialog").dialog ({
		title:'告警列表',
		href:'/page/sysconfig/alarmCorrelationl.html',
		width:600,
		height:400,
		inline:true,
		modal:true,
		shadow:false,
		border:false,
		buttons:[{
			text:'确定',
			handler:function(){
				eventRuleGroup.alarmCorrelation();
			}
		},{
			text:'取消',
			handler:function(){
				eventRuleGroup.closeAlarmDialog();
			}
		}]
	});
	$("#al_alarm_dialog").dialog('open');
};
EventRuleGroup.prototype.closeAlarmDialog = function (){
	$("#al_alarm_dialog").dialog('close');
}
/**
 * 显示规则列表
 */
EventRuleGroup.prototype.showEventRuleGroupList = function() {
	
	var _optformatter = function(value, row, index) {
		var str = "";
		str += "<a class='icon-clone icon16 hand' style='margin-right:10px;' title='复制' href='javascript:void(0)' onclick='eventRuleGroup.editEventRuleGroup("+row.groupId+",true)'></a>" ;
//		if(!row.isSystem){
//			str += "<a class='icon-edit icon16 hand'style='margin-right:10px;' title='编辑' href='javascript:void(0)' onclick='eventRuleGroup.editEventRuleGroup("+row.groupId+",false)'></a>";
//		}
		str += "<a class='icon-alarm icon16 hand' title='关联告警方式' style='margin-right:10px;' href='javascript:void(0);' onclick='eventRuleGroup.openAlarmDialog()'></a>";
		if(row.isSystem == 0){
			str += "<a class='icon-edit icon16 hand' title='编辑' href='javascript:void(0)' onclick='eventRuleGroup.editEventRuleGroup("+row.groupId+",false)'></a>";
		}
		return str ;
	},
	_statusformatter = function(value,row,index) {
		
		if(row.status) {
			
			return '<span title="启用">启用</span>';
		}
		
		return '<span title="禁用">禁用</span>';
	},
	_wrappDetailView = function(_data) {
		var contents = "",
			content = null;
		$.each(_data, function(i, c) {
			content = '<div class="horizon_wrappDetailView">';
			content += '<p class="horizon_wrappDetailView_pHead">';
			content += '<span class="horizon_wrappDetailView_p_spHead">名称：</span>';
			content += '<span>' + c.name + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">等级：</span>';
			content += '<span>' + c.priority + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">描述：</span>';
			content += '<span>' + c.description + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">解决方案：</span>';
			content += '<span>' + c.solution + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">创建人：</span>';
			content += '<span>' + c.creater + '</span>';
			content += '</p>';
			content += '<p>';
			content += '<span class="horizon_wrappDetailView_p_spHead">时间：</span>';
			content += '<span>' + c.createTime + '</span>';
			content += '</p>';
			content += "</div>";
			contents += content;
		}); 
		return contents;
	},
	_typeformatter = function(value, row, index) {
		return (row['isSystem'] ? "<span>系统内置</span>" : "<span>自定义</span>");
	};
	_alertformatter = function(value, row, index) {
		if(value){
			return "<span title='"+value+"'>"+value+"</span>";
		}
		else{
			return "";
		}
	};
	
	$("#al_evtrule_group_table").datagrid({
		url : "/sim/sysconfig/event/evtRuleGroupList",
	    view : detailview,
	    detailFormatter : function(index, row) {
	    	
	        return '<div class="ddv" style="padding:5px 0"></div>';
	    },
	    onExpandRow : function(index, row) {
	    	
	    	var ddv = $(this).datagrid('getRowDetail', index).find('div.ddv');
	        ddv.panel({
	            border : false,
	            cache : false,
	            href : '../../sim/knowledge/getAssociatedKnowledgebyEvtRuleId?evtid=' + row.groupId,
	            extractor : function(data){
	            	var _data = eval(data);
	            	if(_data && _data.length > 0){
	            		return _wrappDetailView(_data);
	            	}
	            	return "暂无解决方案";
	            },
	            onLoad:function(){
	                $('#al_evtrule_group_table').datagrid('fixDetailRowHeight', index);
	            }
	        });
	        
	        $('#al_evtrule_group_table').datagrid('fixDetailRowHeight', index);
	    },
		method : 'post',
		width : 'auto',
		height : 'auto',
		fit : true,
		border : false,
		fitColumns : true,
		checkOnSelect : false,
		selectOnCheck : false,
		pagination : true,
		singleSelect : true,
		pageSize : 20,
		pageList : [20, 40],
		columns : [[
		           {field:"ck", checkbox:true},
		           {field:'groupName', width:15, title:"规则名称"},
		           {field:'priority', width:5,title:"级别",formatter:simHandler.levelFormatter},
		           {field:'cat1id', width:10, title:"一级分类"},
		           {field:'cat2id', width:10, title:"二级分类"},
		           {field:'resp', width:15, title:"告警方式",formatter:_alertformatter},
		           {field:'status', width:5,align:"center", title:"状态", formatter:_statusformatter},
		           {field:'createTime', width:12, title:"创建时间"},
		           {field:'isSystem', width:8, title:"类型",align:"center", formatter:_typeformatter},
		           {field:'creater', width:5, title:"创建人",align:"center"},
		           {field:'groupId', width:7, title:"操作", align:'left', formatter:_optformatter}
		          ]],
		onHeaderContextMenu : function(e, field) {},
		onDblClickRow : function(rowIndex, rowData) {},
		onRowContextMenu:function(e, rowIndex, rowData) {},
		onBeforeLoad:function(param){
			$(this).datagrid('uncheckAll');
		}
	});
};

/**
 * 显示规则配置弹出配置框
 */
EventRuleGroup.prototype.showAddEventRuleGroup = function(){
	this.openAddOrEditDialog("add");
};

/**
 * 规则选择批量删除
 */
EventRuleGroup.prototype.deleteMRuleGroup = function(){
	var rows=$("#al_evtrule_group_table").datagrid("getChecked");
	    
	if(rows&&rows.length==0){
		showAlarmMessage( '请选择一条数据' );
		return;
	}
	var _groupids="",_issystem=false;
	$.each(rows,function(i,row){
		if(!row['isSystem']){
			if(rows.length-2>=i){
				_groupids+=(row.groupId+",");
			}else{
				_groupids+=row.groupId;
			}
		}else{
			_issystem=true;
		}
	});
	if(!_issystem){
		var $it = this;
		$.messager.confirm('警告','您确定要删除选中的规则吗？',function(r){   
		    if (r){
		    	$it.deleteRuleGroup(_groupids);
		    }   
		});
	}else{
		showAlarmMessage( '删除规则中包含有系统内置规则！' );
	}
	
};

/**
 * 删除规则过程
 * @param _id
 */
EventRuleGroup.prototype.deleteRuleGroup=function(_id){//可删单个或者多个
	var _this=this;
	if(typeof(_id)=='number'||_id.length>0){
		 $.ajax({
				type : "post",
			 	url : "/sim/sysconfig/event/delEvtRuleGroup",
			 	data:{groupIds:_id},
				async : false,
				dataType : "json",
				success : function(data) {
					_this.showEventRuleGroupListReload();
				}
		 });
	}
}
