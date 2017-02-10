/**
 * 知识库
 * @param id
 * @returns {Knowledge}
 */
function Knowledge(id) {
	this.tableId = id;
	this.menuId = null;
	this.colums = null;
	
	this.queryParams = {
				 "name": null,
				 "cat1id" : null,
				 "cat2id" : null,
				 "datetime" : null
		};
	this._plugin_set = {};
	this.plugin();
}

Knowledge.prototype.plugin = function(){};
/**
 * 初始化datagrid的colum
 * @param _colums_set
 */
Knowledge.prototype.initColum = function(_colums_set) {
	
	var _this = this;
	
	$.ajax({
		type : "post",
		url : "/sim/knowledge/jsondata",
		data : {"json":_colums_set},
		async : false,
		dataType : "json",
		success : function(data) {
			
			_this.colums = data;
			
			// 判断操作列按钮
			$.each(_this.colums[0], function(i, _col) {
				
				if(_col['field'] == '_option_modify') {
					
					_col['align'] = 'center';
					_col['formatter'] = function(value, row, index) {
						
						return  "<a class='icon-edit icon16 hand' title='编辑' style='cursor:pointer;'></a>";
					};
				}
			});
			
		}
	});
};

/**
 * 打开 添加知识界面
 */
Knowledge.prototype.addKnowledge = function(){
	
	$("#kn_add_id").window({
		width : 550,
		height : 350,
		inline : false,
		title : "新建",
		modal : true,
		shadow : true,
		collapsible : false,
		minimizable : false,
		maximizable : false,
		href : '/page/knowledge/knowledgeAddForm.html',
		onClose : function(){
			$("#kn_add_id").window("destroy");
			if($("#kn_add_id").length === 0){
				$("#horizon_windowTeplateId").append('<div id="kn_add_id"></div>');
			}
		}
	}).window('open');
};

/**
 * 关闭编辑窗口
 */
Knowledge.prototype.closeKnowledgeW = function(windowId) {
	$("#" + windowId).window("close");
};

/**
 * 添加知识操作·添加事件类型
 * @param _cat1
 * @param _cat2
 * @param _function
 */
Knowledge.prototype.addEventCategory = function(_cat1, _cat2, _function) {
	
	$.ajax({
		type : "post",
		url : "../../sim/event/addEventCategory",
		data : {"cat1" : _cat1, "cat2" : _cat2},
		async : false,
		dataType : "json",
		success : function(data) {
			if(_function) {
				
				_function(data);
			}
		}
	});
};

/**
 * 添加知识操作
 */
Knowledge.prototype.doAddKnowledge = function(){
	
	var kn_name = knowledge_add_Form.name.value,
		kn_cat1 = knowledge_add_Form.cat1.value,
		kn_cat2 = knowledge_add_Form.cat2.value,
		kn_desc = knowledge_add_Form.desc.value,
		kn_solution = knowledge_add_Form.solution.value,
		_this = this;
	
	this.addEventCategory(kn_cat1, kn_cat2, function(data) {
		$.ajax({
			type : "post",
			url : "../../sim/knowledge/addEventKnowlege",
			data : {
				name : kn_name,
				priority : "0",
				cat1id : kn_cat1,
				cat2id : kn_cat2,
				description : kn_desc,
				solution : kn_solution
			},
			async : false,
			dataType : "json",
			success : function(data) {
				// 关闭添加窗口
				_this.closeKnowledgeW("kn_add_id");
				_this.resetQueryParam();
				_this.refreshKnowledgeTable();
				$(_this.menuId).tree("reload");
			}
		});
	});
};

/**
 * 打开修改页面
 * @param id
 */
Knowledge.prototype.showModifyKnowledge = function(id) {
	
	$.getJSON(
		"../../sim/knowledge/getOneKnowledgebyId?id=" + id,
		{_time:new Date().getTime()},
		function(data){
			
			$("#kn_md_id").window({
				width : 550,
				height : 350,
				inline : false,
				title : "编辑",
				modal : true,
				shadow : true,
				collapsible : false,
				minimizable : false,
				maximizable : false,
				href : '/page/knowledge/knowledgeModifyForm.html',
				onClose : function(){
					$("#kn_md_id").window("destroy");
					if($("#kn_md_id").length === 0){
						$("#horizon_windowTeplateId").append('<div id="kn_md_id"></div>');
					}
				},
				onOpen : function() {
					
					setTimeout(function() {
						
						knowledge_modify_Form.id.value = data['id'];
						knowledge_modify_Form.name.value = data['name'];
						knowledge_modify_Form.creater.value = data['creater'];
						$('#modify_cat1_id').combobox('setValue', data['cat1id']);
						$('#modify_cat2_id').combobox('setValue', data['cat2id']);

						knowledge_modify_Form.desc.value = data['description'];
						knowledge_modify_Form.solution.value = data['solution'];
						
						var $$priority = $("#knowledge_modify_form_id").find("input[name='_r_priority']");
						
						$.each($$priority, function(i, _p_input) {

							if(_p_input.value == data['priority']) {
								
								_p_input.checked = true;
							}
						});
					}, 500);
				}
			}).window('open');
		}
	);
};

/**
 * 修改知识
 */
Knowledge.prototype.doModifyKnowledge = function() {
	
	var kn_name = knowledge_modify_Form.name.value,
		kn_cat1 = knowledge_modify_Form.cat1.value,
		kn_cat2 = knowledge_modify_Form.cat2.value,
		kn_desc = knowledge_modify_Form.desc.value,
		kn_solution = knowledge_modify_Form.solution.value,
		kn_id = knowledge_modify_Form.id.value,
		kn_creater = knowledge_modify_Form.creater.value,
		_this = this;

	$.ajax({
		type : "post",
		url : "../../sim/knowledge/updateEventKnowlege",
		data : {
			id : kn_id,
			name : kn_name,
			priority : "0",
			cat1id : kn_cat1,
			cat2id : kn_cat2,
			description : kn_desc,
			solution : kn_solution,
			creater : kn_creater
		},
		async : false,
		dataType : "json",
		success : function(data) {
			// 关闭修改窗口
			_this.closeKnowledgeW("kn_md_id");
			_this.refreshKnowledgeTable();
		}
	});
};

/**
 * 刷新表格数据
 */
Knowledge.prototype.refreshKnowledgeTable = function(fit){
	
	var _this = this,
		_wrappDetailView = function(_data) {
			var tables = "",
				table = null;
				table = '<p>';
    			table += '<label>解决方案：</label>';
    			table += '<pre>' + _data.solution + '</pre>';
    			table += "</p>";
				tables += table;
			
			return tables;
		};
	
	$(this.tableId).datagrid({
		url : "/sim/knowledge/queryEventKnowlege",
		method : 'post',
		fitColumns : true,
		fit : true,
		singleSelect : false,
		checkOnSelect : false,
		selectOnCheck : false,
		nowrap:false,
		pagination : true,
		columns : _this.colums,
		queryParams : _this.queryParams,
		view : detailview,
		onLoadSuccess : function(data) {
			if(data) {
				
				var rows = data.rows;
				$.each($('.icon-edit'), function(i,$btn) {
					row = rows[i];
					
//					$($btn).linkbutton({
//						iconCls : 'icon-edit',
//						plain : true
//					});
					
					$($btn).bind("click", function() {
						_this.showModifyKnowledge(rows[i].id);
					});
				});
			}
		},
	    detailFormatter : function(index, row) {
	        return '<div class="ddv" style="padding:5px 0"></div>';
	    },
	    onExpandRow : function(index, row) {
	    	
	        var ddv = $(this).datagrid('getRowDetail', index).find('div.ddv');
			ddv.panel({
			    border : false,
			    cache : false,
			    href : '../../sim/knowledge/getOneKnowledgebyId?id=' + row.id,
			    extractor : function(data) {
			    	var _data = eval('(' + data + ')');
			       	return _wrappDetailView(_data);
			    },
			    onLoad : function() {
			        $(_this.tableId).datagrid('fixDetailRowHeight', index);
			    }
			});
	        $(_this.tableId).datagrid('fixDetailRowHeight', index);
	    },
		onHeaderContextMenu : function(e, field) {
			e.preventDefault();
		},
		onDblClickRow : function(rowIndex, rowData) {
		},
		onRowContextMenu:function(e, rowIndex, rowData){
			e.preventDefault();
		}
	});
};

/**
 * 重设查询数据
 */
Knowledge.prototype.resetQueryParam = function(){
	
	this.queryParams.name =
	this.queryParams.cat1id =
	this.queryParams.cat2id =
	this.queryParams.datetime = null;
	
};

/**
 * 查询知识
 */
Knowledge.prototype.doQueryKnowledge = function() {
	var kn_name = knowledge_query_Form.name.value;
	var kn_cat1,kn_cat2;
    var node_tree = $("#kn_menu_id").tree("getSelected"); 
    if(node_tree){
    	if(node_tree.attributes) {
    		var nodeType = node_tree.attributes['type'],text = node_tree.text;
    		switch (parseInt(nodeType)) {
    			case 1:
    				kn_cat1 = text;
    				break;
    			case 2:
    				kn_cat2 = text;
    				break;
    		}
    	}
    }
	this.queryParams.name = kn_name;
	this.queryParams.cat1id = kn_cat1;
	this.queryParams.cat2id = kn_cat2;
	this.queryParams.datetime = null;
	this.refreshKnowledgeTable();
	
};

/**
 * 删除知识
 */
Knowledge.prototype.deleteKnowledge = function() {
	
	var checkedRows = $(this.tableId).datagrid("getChecked"),
		_this = this;
	
	if(checkedRows && checkedRows.length > 0) {
		
		var idArray = new Array();
		
		$.each(checkedRows, function(i, row) {
			idArray.push(row.id);
		});
		
		$.messager.confirm('警告', '您确定要删除选定数据吗？', function(r) {
		    if (r) {
				$.ajax({
					type : "post",
					url : "../../sim/knowledge/delEventKnowlegeById",
					data : {id : idArray},
					async : false,
					dataType : "json",
					success : function(data) {
						if(data.status) {
							_this.refreshKnowledgeTable();
						}
					}
				});
		    }   
		});
	} else {
		showAlarmMessage('请选择一条数据');
	}
};

/**
 * 初始化菜单
 */
Knowledge.prototype.initKnMenu = function(_menu_id) {
	
	var _this = this;
	
	this.menuId = _menu_id;
	
	$(_menu_id).tree({// 类型树导航事件
		url : "/sim/knowledge/knowledgeCategory",
		onClick : function(node){
			if(node.attributes) {
				var nodeType = node.attributes['type'],
				 /* id = node.attributes['id'],*/
				 	text = node.text;
				_this.resetQueryParam();
				switch (parseInt(nodeType)) {
					case 1:
						_this.queryParams.cat1id = text;
						break;
					case 2:
					//	_this.resetQueryParam();
					//	var p = $(_menu_id).tree("getParent", node.target);
					//	_this.queryParams.cat1id = p.text;
					//	_this.queryParams.cat2id = text;
						_this.queryParams.cat2id = text;
						break;
					case 3:
						_this.queryParams.name = text;
						break;
					default:
						break;
				}
				_this.refreshKnowledgeTable();
			}
		}
	});
};
