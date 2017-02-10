/***
 * 事件管理
 * @returns {ManageEvent}
 */

function ManageEvent(){
	this._M_EVENT_DATA_URL="/sim/database/partitionPage";
	this._M_EVENT_JSON_URL="/sim/eventMonitor/jsondata";
	this.critical_m_event_data_url=this._M_EVENT_DATA_URL;
	this.columns=null;//显示列集
}
		
		
ManageEvent.prototype.batchSelected=function(){
	var checkedRows=$("#event_mg_table_id").datagrid("getChecked");
    if(!checkedRows||(checkedRows && checkedRows.length<=0)){
    	showAlertMessage("请选择需要删除的事件!");
		return;
	}  
	return checkedRows;
};
ManageEvent.prototype.setColFormat=function(headers){
	var len=headers.length;
	if(!len){
		showErrorMessage("无显示列!");
	}else{
		$.each(headers[0],function(i,col){
			if(col['field']=='delete'){
				col.formatter=function(value,row,index){
					var date=simHandler.newServerDate();
					var month=date.getMonth()+1;
					var nowDate=date.getFullYear()+""+(month<10?("0"+month):month)+(date.getDate()<10?("0"+date.getDate()):date.getDate());
					var partitionName=row['partitionName'];
					if(partitionName<nowDate){
						return  "<a title='删除' style='width:16px;height:16px;cursor: pointer;' class='icon-remove' onclick='doDeleteMEvent("+row['partitionName']+")'></a>";
					}else if(partitionName==nowDate){
						return "";
					}
					
				    
				};
			};
			if(col["field"] == "importFlag") {
				col.formatter = function(value, row, index) {
					var resultTemp = "未知";
					
					if(value == "operate_succeed") {
						resultTemp = "已导入";
					} else if(value == "operate_processing") {
						resultTemp = "正在导入";
					} else if(value == "operate_fail") {
						resultTemp = "导入失败";
					} else if(value == "operate_new") {
						resultTemp = "未处理";
					}
					
					return resultTemp;
				};
			}
			
			if(col['field'] == 'exportFlag') {
				col.formatter = function(value, row, index) {
					var resultTemp = "未知";
					
					if(value == "operate_succeed") {
						resultTemp = "已导出";
					} else if(value == "operate_processing") {
						resultTemp = "正在导出";
					} else if(value == "operate_fail") {
						resultTemp = "导出失败";
					} else if(value == "operate_new") {
						resultTemp = "未处理";
					}
					
					return resultTemp;
				};
			
			}
			
		});
	};
	
};
ManageEvent.prototype.loadMEventTable=function(){
	var _this=this;
	$.ajax({
	    type: "post",
	    url:this._M_EVENT_JSON_URL,
	    async:false,
	    data:{"json":"evt_mg_colums"},
	    success: function(data){
	    _this.columns=null;
	    if(data){
	    	_this.columns= eval(data);
	    	//添加列格式化 
	    	_this.setColFormat(_this.columns);
	    }	
	   }
	});
	
	this.refreshEventTable();
};

ManageEvent.prototype.refreshEventTable=function(){
	var _this = this;
	if(this.columns){
		$('#event_mg_table_id').datagrid({
			url:this.critical_m_event_data_url,
			method: 'post',
			title: '事件管理',
			iconCls: 'icon-grid',
			width: 'auto',
		 	fit : true,
		 	striped : true,
		//	height: 'auto', 
			fitColumns: true,
			striped:true,
			singleSelect: true,
			selectOnCheck:false,
			checkOnSelect:false,
			pagination:true,
			rownumbers:true,
			columns:this.columns,
			onHeaderContextMenu: function(e, field){
				e.preventDefault();
			},
			onDblClickRow:function(rowIndex,rowData){
			},
			onLoadSuccess:function(data){
				var pageNumber=$('#event_mg_table_id').datagrid("getPager").pagination('options').pageNumber;
				if(pageNumber!=null&&pageNumber!=undefined&&parseInt(pageNumber)==1){
					var inputs=$("input[name='ck']");
					
					var partitionName=data.rows[0]['partitionName'];
					if(partitionName == _this.todayDate()){
					  $(inputs[0]).remove();
					}
					
				}
			},
			toolbar:$("#tools")
		});
		$('#event_mg_table_id').datagrid({checkOnSelect:$("#event_mg_table_id").is(':checked')});
	}
	
};
ManageEvent.prototype.todayDate = function(){
	var date=simHandler.newServerDate();
	var month=date.getMonth()+1;
	var nowDate=date.getFullYear()+""+(month<10?("0"+month):month)+(date.getDate()<10?("0"+date.getDate()):date.getDate());
	return nowDate;
}
ManageEvent.prototype.exportPEvents=function(){
	var flag = false;
	var _this=this;
	var doExportFunc=function(){
	    var pnames=[];
	    var checkedRows=$("#event_mg_table_id").datagrid("getChecked");
	    $.each(checkedRows,function(i,row){
			if(row['partitionName'] != _this.todayDate()){
				pnames.push(row['partitionName']);
			}
		});
	    if(pnames.length == 0){
	    	showErrorMessage("请选择需要备份的事件！");
	    	return;
	    } else if(pnames.length>0){
	    	$.each(checkedRows,function(i,row){
	    			if(parseInt(checkedRows[i].eventCount)>0){
		    			flag = true;
		    		}
			});
	    }
	    if(!flag){
	    	showErrorMessage("没有可备份的事件！");
	    	return;
	    }
		 $.messager.progress(); 
		$.ajax({
			    type: "post",
			    url: "/sim/database/exportPartitions",
			    async:false,
			    dataType:"json",
			    data:{"pnames":pnames},
			    success: function(result){
			    	$.messager.progress('close'); 
			    	 if(result.result=='error'){
			    		 showErrorMessage("事件导出发生错误！");
			    	 }
			    	 //$('#event_mg_table_id').datagrid('uncheckAll');
			    	 _this.refreshEventTable();
			   }
		});
	};
	/**
		检测服务器配置
	*/
	var prompt=function(json){
		if(json){
			var checkRt=json.station;
			if(checkRt=="ftpivalid"){
				showErrorMessage("FTP服务器验证失败，无法进行事件导出！");
			}
			
			if(checkRt=="nopath"){
				$("<div></div>").dialog({
					id:'backup_path_config_dialog',
					href:'/page/sysconfig/sysconfig_logbackup.html',
					title:'日志备份策略配置',
					width:800,
					height:435,
					modal:true,
					onClose:function(){
						$(this).dialog('destroy');
					}
				});	
				showAlertMessage("请先配置备份路径！");
				return;
			}
			
			if(checkRt=="ok"){
				return 1;
			}
			
		}
		
		return -1;
	};
	$.ajax({
			 url:"/sim/logBackUp/checkBkServer", 
			 dataType:"json",
			 success:function(data){
				if(prompt(data)){
					doExportFunc();
				}
		 	 }
	}); 

};

ManageEvent.prototype.deletePEvents=function (param,_before,_after){
	$.messager.confirm('提示','确定删除该事件吗？',function(r){
		if (r){   
			$.ajax({
			    type: "post",
			    url: "/sim/database/deletePartitions",
			    dataType:"json",
			    data:param,
			    success: function(data){
			    	if(data.result&&data.result==1){
			    		//_this.loadMEventTable();
			    		$('#event_mg_table_id').datagrid('uncheckAll');
			    		$("#event_mg_table_id").datagrid("reload");
			    	}
			   }
		    });
		}else{
			return;
		}
	});
	
};
var manageEvent=new ManageEvent();

(function(){
 	$('#cc').combobox({   
	    url:'../../sim/eventMonitor/jsondata?json=evt_mg_selections',   
	    valueField:'ms',   
	    textField:'text',
	    onSelect: function(rec){   
           manageEvent.critical_m_event_data_url=manageEvent._M_EVENT_DATA_URL+"?manageScope="+rec['ms'];
		   manageEvent.loadMEventTable();
        }
	});  
	manageEvent.loadMEventTable();//加载表格
})();


/**
 * 页面操作事件响应方法
 * @param arg
 */
function doDeleteMEvent(arg){//删除单条
	var pnames=[];
	pnames.push(arg);
	manageEvent.deletePEvents({"pnames":pnames},function(){$.messager.progress();},function(){$.messager.progress('close');});
}

function doBatchDeletePEvent(){//批量删除多条
	var checkedRows = manageEvent.batchSelected(), pnames=[];
	$.each(checkedRows,function(i,row){
		if(row['partitionName'] != manageEvent.todayDate()){
			pnames.push(row['partitionName']);
		}
	});
	if(pnames.length <1){
		showErrorMessage('请选择要删除的事件!');
		return;
	}
	manageEvent.deletePEvents({"pnames":pnames},function(){$.messager.progress();},function(){$.messager.progress('close');});
}

function doExportPEvent(){//导出
	manageEvent.exportPEvents();
}

function doQueryPEvent(){//查询
	$("#event_mg_qDlgId").dialog("open");
}
function importPartions(files){
	 $.ajax({
		    type: "post",
		    url: "/sim/database/importPartions",
		    dataType:"json",
		    data:{'partitions':files},
		    success: function(data){
	  			if(data.result=='success'){
	  				showAlertMessage('正在执行操作，请稍后查看结果!');
	  			}else{
	  				showErrorMessage('导入发生错误!');
	  			}
		   }
	});
}
function doImportPEvent(){
	var _$div=$('<div></div>');
	_$div.dialog({   
	    title: '请选择',   
	    width: 400,   
	    height: 280,   
	    closed: false,   
	    cache: false,   
	    href: '/page/event/eventManageImport.html',
	    onClose:function(){_$div.dialog("destroy");},
	    buttons:[
		             {
						text:'确定',
						handler:function(){
							/**
							 * 处理表单数据
							 */
							var options=importForm.importArea.options;
							if(options){
								var files=[],notExistfiles=[],b=false;
								$.each(options,function(i,option){
									files.push('P'+option.value);
								});
								
								$.getJSON("/sim/database/getLocalFiles?_time=" + new Date().getTime(), function(json){
								  
								  	$.each(files,function(k,file1){
								  		b=false;
								  		$.each(json,function(i,file){
								  			if(file1==file){
								  				b=true;
								  			}
								  		});
								  		if(!b){
								  			notExistfiles.push(file1);
								  		}
								  	});
								  	
								  	if(notExistfiles.length>0){
								  		$.each(notExistfiles,function(j,file3){
								  			var indexTemp = $.inArray(file3, files);
								  			files.splice(indexTemp, 1);
								  		});
								  	}
								  	
								   if(files.length>0){
									   if(notExistfiles.length > 0){
										   $.messager.confirm('提示','备份'+notExistfiles+'不存在,是否继续？',function(record){   
											    if (record){  
											    	$(_$div).dialog("destroy");
											    	importPartions(files);
											    }else{
											    	importForm.importArea.options.length = 0;
											    }   
											}); 
									   }else{
										   $(_$div).dialog("destroy");
										   importPartions(files);
									   }
									  
								  	}else{
								  		showErrorMessage('没有可导入的备份!');
								  	}
								});
								
								
							}else{
								showErrorMessage('未选择导入!');
							}
						}
					},
					{
						text:'取消',
						handler:function(){
							_$div.dialog("destroy");
						}
					}
				],
	    modal: true  
	});  
	
	
	/*	$.getJSON("../../sim/database/getLocalFiles", function(json){
	});*/
 	 
}

