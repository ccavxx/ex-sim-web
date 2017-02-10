var simSysConfResource = {editResource:null};
function ResourceManager(){
	
}

ResourceManager.executeQuery = function(){
	var name = $("#sysres_iplocation_name").val() ;
	var ip = $("#sysres_iplocation_ip").val() ;
	if(ip != "" && isInvalidIp(ip)){
		showErrorMessage("无效的IP地址！") ;
		$("#sysres_iplocation_ip").focus() ;
		return ;
	}
	$("#ipLocationGrid").datagrid("reload",{name:name,ip:ip}) ;
}

/*ResourceManager.endIpLocationEdit = function(){
	var editIndex = ResourceManager.editIndex ;
	if (editIndex == undefined){return true}
	if ($('#ipLocationGrid').datagrid('validateRow', editIndex)){
		var ed = $('#ipLocationGrid').datagrid('getEditor', {index:editIndex,field:'id'});
		$('#ipLocationGrid').datagrid('endEdit', editIndex);
		ResourceManager.editIndex = undefined;
		return true;
	} else {
		return false;
	}
}*/

/*ResourceManager.ipLocationCellClick = function(index,field){
    if (ResourceManager.editIndex != index){
        if (ResourceManager.endIpLocationEdit()){
            $('#ipLocationGrid').datagrid('selectRow', index).datagrid('beginEdit', index);
            var ed = $('#ipLocationGrid').datagrid('getEditor', {index:index,field:field});
            ($(ed.target).data('textbox') ? $(ed.target).textbox('textbox') : $(ed.target)).focus();
            ResourceManager.editIndex = index;
        } else {
            $('#ipLocationGrid').datagrid('selectRow', ResourceManager.editIndex);
        }
    }
}*/

ResourceManager.nameFormater = function (index, field){
	if(field.name != undefined){
		field.nameHtmlUnescape = field.name
			.replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
	}
	return field.nameHtmlUnescape;
}

/**
 * 编辑按钮
 */
ResourceManager.formatterOperation = function(value,row,index){
	 return "<a title='编辑' style='width:16px;height:16px;cursor: pointer;' class='icon-edit' onclick='ResourceManager.editSysResource(\""+index+"\")'></a>";
}

/**
 * 添加系统资源
 */
ResourceManager.addIpLocation = function(){
    /*if (ResourceManager.endIpLocationEdit()){
        $('#ipLocationGrid').datagrid('appendRow',{});
        var editIndex = ResourceManager.editIndex = $('#ipLocationGrid').datagrid('getRows').length-1;
        $('#ipLocationGrid').datagrid('selectRow', editIndex).datagrid('beginEdit', editIndex);
    }*/
	openResMgrEditorDialog();
}

/**
 * 打开系统资源编辑窗口
 */
function openResMgrEditorDialog(){
	var w = $('#sysconfig_container').panel('panel').width();
	var h = $('#sysconfig_container').panel('panel').height();
	var top = $('#sysconf_resource_table_panel').panel('panel').position().top;
	var left = $('#sysconf_resource_table_panel').panel('panel').position().left;
	
	$("#sysconf_resource_editor_dialog").dialog({
		href:'/page/sysconfig/sysconfig_resource_edit.html',
		style:{'padding':0,'border':0},
		top:top,
		left:left,
		width:w,
		height:h,
		inline:true,
		noheader:true,
		shadow:false,
		fit:true,
		border:false,
		onClose:reloadResourceTable
	});		
}

/**
 * 删除系统资源
 */
ResourceManager.deleteIpLocation = function(){

	var selrows = $('#ipLocationGrid').datagrid('getChecked');

	if (selrows.length > 0) {
		var ids = "";
		$.each(selrows, function(index, selrow) {
			var rowid = selrow.id;
			ids += rowid;
			if (index != selrows.length-1) {
				ids += ",";
			}
		});
		$.messager.confirm('警告', '您确定要删除选中的资源吗？', function(r) {
			if (r) {
				$.getJSON('/sim/resource/delete?_time=' + new Date().getTime(), {ids:ids}, function(result) {
					if (result.status) {
						reloadResourceTable();
					} else {
		        		showErrorMessage(result.message);
		        	}
				});
			}
		});
	} else {
		showErrorMessage( '请选择一条数据' );
	}
}

/**
 * 重新加载系统资源列表
 */
function reloadResourceTable(){
	$('#ipLocationGrid').datagrid('reload');//重新加载本页
	$('#ipLocationGrid').datagrid('unselectAll');//取消所有选择
	$('#ipLocationGrid').datagrid('uncheckAll');//取消所有选择
	simSysConfResource.editResource = null;//每次刷新列表清空此值
}

/*ResourceManager.deleteIpLocation = function(){
	var editIndex = ResourceManager.editIndex ;
	if (editIndex == undefined){return}
    $('#ipLocationGrid').datagrid('cancelEdit', editIndex).datagrid('deleteRow', editIndex);
    ResourceManager.editIndex = undefined;
}*/

/*ResourceManager.saveIpLocation = function(){
	if (ResourceManager.endIpLocationEdit()){
		var insertRows = $("#ipLocationGrid").datagrid("getChanges","inserted") ;
		var updateRows = $("#ipLocationGrid").datagrid("getChanges","updated") ;
		var deleteRows = $("#ipLocationGrid").datagrid("getChanges","deleted") ;
		var data = new Object() ;
		if(insertRows.length > 0){
			data["insert"] = JSON.stringify(insertRows);
		}
		if(updateRows.length > 0){
			data["update"] = JSON.stringify(updateRows);
		}
		if(deleteRows.length > 0){
			data["delete"] = JSON.stringify(deleteRows);
		}
		$.post("/sim/resource/saveIpLocation",data,function(result){
			if(result.success){
				$("#ipLocationGrid").datagrid("reload") ;
			}else{
				showErrorMessage(result.message) ;
			}
		},"json");
	}
}*/
	
	/**
	 * 修改系统资源
	 */
	ResourceManager.editSysResource = function(selRowIndex){
		var rows = $('#ipLocationGrid').datagrid('getRows');
		var selrow = rows[selRowIndex];
		if(selrow){
			simSysConfResource.editResource = selrow;
			openResMgrEditorDialog();
		}else{
			showErrorMessage( '请选择一条数据' );
		}
	}
