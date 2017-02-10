/**
 * 知识库操作扩展 针对事件配置规则
 */
Knowledge.prototype.plugin = function() {
	 this._plugin_set['associateList'] = new Array();
}
Knowledge.prototype.doAssociation = function() {
	
	var checkedRows = $(this.tableId).datagrid("getChecked"),
		_this = this;
	if(checkedRows && checkedRows.length > 0) {
		$.each(checkedRows, function(i, row) {
			
			for ( var j = 0; j < _this._plugin_set['associateList'].length; j++) {
				if(_this._plugin_set['associateList'][j] == row.id) {
					showAlertMessage('解决方案[' + row.name + ']已添加!'); 
					return;
				}
			}
			_this._plugin_set['associateList'].push(row.id);
			
			var $kn_title = $('<div class="alert alert-warning fade in" style="width:80px;float: left;margin:5px 0 0 5px;"></div>'),
			$cl_btn = $('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
			$kn_title.append($cl_btn).append($("<span></span>").html(row.name).attr("id","kn_"+row.id));
			$("#selected_knowledge_id").append($kn_title);
			var knid = row.id;
			$kn_title.bind('closed.bs.alert', function () {
				for ( var k = 0; k < _this._plugin_set['associateList'].length; k++) {
					if(parseInt(_this._plugin_set['associateList'][k]+"") == parseInt(knid+"")){
						_this._plugin_set['associateList'].splice(k, 1);
						break;
					}
				}
			});
		});
		$(this.tableId).datagrid("unselectAll");
		$(this.tableId).datagrid("uncheckAll");
	} else {
		showAlarmMessage('未选择任何解决方案！');
	}
}
Knowledge.prototype.doQuery = function() {
	this.resetQueryParam();
	this.queryParams.name = event_rule_kn_form.kn_name.value;
	this.refreshKnowledgeTable();
}
