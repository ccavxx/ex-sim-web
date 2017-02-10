var sysconfigAssGroup = {}
sysconfigAssGroup.initDropAssem = function(assemVal){
	$(assemVal).droppable({
		onDragEnter:function(e,source){
			$(source).draggable('options').cursor='auto';
			$(source).draggable('proxy').css('border','1px solid red');
			$(this).addClass('over');
		},
		onDragLeave:function(e,source){
			$(source).draggable('options').cursor='not-allowed';
			$(source).draggable('proxy').css('border','1px solid #ccc');
			$(this).removeClass('over');
		},
		onDrop:function(e,source){
			var it = $(this);
			var groupIdSrc = it.attr("id");
			var assetDeviceIdSrc = source.id;
			if(groupIdSrc){
				var groupId = groupIdSrc.substring(1);
				var assetDeviceId = assetDeviceIdSrc.substring(1);
				if(groupId && assetDeviceId){
					// 请求后台操作
					$.getJSON(
						"/sim/assGroup/changeAssetToAssGroup?_time="+new Date().getTime(),
						{groupId:groupId,assetDeviceId:assetDeviceId},
						function(result){
							if( !result ) {
								showErrorMessage("数据出错");
							} else if (result.status) {
								// 通过文档操作进行前台渲染
								it.append(source);
							} else {
								showErrorMessage(result.message);
							}
						});
				}
			}
			it.removeClass('over');
		}
	});
}
sysconfigAssGroup.initDraggableAssem = function(assemVal){
	$(assemVal).draggable({
		proxy:'clone',
		revert:true,
		cursor:'auto',
		onStartDrag:function(){
			$(this).draggable('options').cursor='not-allowed';
			$(this).draggable('proxy').addClass('dp');
		},
		onDrag:function(e){
			var targt = $("#assGroupTotalId");
			var d = e.data;
			var scrollTop = targt.scrollTop();
			var outerHeight = $(d.target).outerHeight();
			var height = $(d.target).height();
			if (d.top + outerHeight > (targt.height()+scrollTop)){
				targt.scrollTop(scrollTop + (outerHeight-height));
			}
		},
		onStopDrag:function(){
			$(this).draggable('options').cursor='auto';
		}
	});
}

/**
 * 显示业务组名称为编辑状态
 */
sysconfigAssGroup.assGroupShowInput = function(it){
	$(it).parent().css("display","none") ;
	$(it).parent().siblings().css("display","block");
	$(it).parent().siblings().find("input").val($.trim($(it).parent().text()));
	$(it).parent().siblings().find("input").focus();
}

/**
 * 更新业务组名称
 */
sysconfigAssGroup.assGroupEditOnblur = function(it,groupId){
	$(it).parent().css("display","none") ;
	$(it).parent().siblings().css("display","inline-block");
	var title = $(it).val();
	var titleSrc = $(it).parent().siblings().find("span").text();
	if (title && groupId && titleSrc!=title){
		if(title.length<=15){
			// 提交后台保存
			$.getJSON(
				"/sim/assGroup/editAssGroupName?_time="+new Date().getTime(),
				{groupName:title,groupId:groupId},
				function(result){
					if( !result ) {
						showErrorMessage("数据出错");
					} else if (result.status) {
						$(it).parent().siblings().find("span").text($.trim($(it).val()));
					} else {
						showErrorMessage(result.message);
					}
			});
		}else{
			showErrorMessage("新业务组名称长度不能超过15个字");
		}
	}
}

/**
 * 删除数据的前台渲染
 */
sysconfigAssGroup.assGroupSortableRemoveLi = function(it,groupId){
	if(groupId){
		var unEdit = $("#t" + groupId).find(".drag-unedit").length;
		if(unEdit > 0) {
			showErrorMessage("请获得该业务组下所属所有资产操作权限之后，再执行！");
			return;
		}
		$.messager.confirm('警告', '您确定要删除该业务组吗？', function(r){
			if (r){
				// 通过文档获得id，提交后台删除
				$.getJSON(
					"/sim/assGroup/delAssGroup?_time="+new Date().getTime(),
					{groupId:groupId},
					function(result){
						if( !result ) {
							showErrorMessage("数据出错");
						} else if (result.status) {
							$("#t-1").append($("#t"+groupId).html());
							sysconfigAssGroup.initDraggableAssem("#t-1 > .drag");
							// 通过文档操作进行前台渲染
							$(it).parents("li").remove();
						} else {
							showErrorMessage(result.message);
						}
					});
			}
		});
	}
}

/**
 * 前端渲染Draggable组件并绑定事件
 */
sysconfigAssGroup.romanceDraggable = function(title,id){
	if(id && title){
		var template = "<li><div id='th" + id + "'>";
		template += "<div class='assGroup_div_header'><table><tbody><tr>";
		template += "<td class='first' ><i class='icon-th-list'></i></td>";
		template += "<td class='second' >"
		template += "<div class='editable' style='display:inline-block;'><span>" + title + "</span><i class='icon-edit' onclick='sysconfigAssGroup.assGroupShowInput(this)' style='margin-left:1px;height:16px;width:21px;'/></div>";
		template += "<div class='editable_input' style='display: none;'>";
		template += "<input type='text' name='name' value='" + title + "' onblur='sysconfigAssGroup.assGroupEditOnblur(this,\""+id+"\")'/></div></td>";
		template += "<td class='third' >";
		template += "<a href='javascript:void(0);' onclick='sysconfigAssGroup.assGroupSortableRemoveLi(this,\""+id+"\")'>×</a>";
		template += "</td>";
		template += "<td class='fourth' >";
		template += "</td></tr></tbody></table></div>";
		template += ("<div class='assGroup_div_body' id='t" + id + "' >");
		template += "</div></div></li>";
		$("#sortable").append(template);
		sysconfigAssGroup.initDropAssem("#t"+id);
	}
}

/**
 * 打开新建业务组页面
 */
sysconfigAssGroup.openAddAssGroupDialog = function(){
	$('#assGroup_tools_addForm').form('clear');
	$('#assGroup_tools_adddlg').dialog('open');
}

/**
 * 关闭新建业务组页面
 */
sysconfigAssGroup.closeAddAssGroupDialog = function(){
	$('#assGroup_tools_addForm').form('clear');
	$('#assGroup_tools_adddlg').dialog('close');
}

/**
 * 初始化页面
 */
sysconfigAssGroup.initPage = function(){
	sysconfigAssGroup.initDropAssem(".assGroup_div_body");
	sysconfigAssGroup.initDraggableAssem(".drag-edit");
	$("#assGroup_tools_add").click(function(){
		sysconfigAssGroup.openAddAssGroupDialog();
	});
}

/**
 * 新建业务组
 */
sysconfigAssGroup.addAssGroup = function(){
	var title = $("#assGroup_tools_addForm").find("input[name='groupName']").val();
	if (title){
		if(title.length<=15){
			// 提交后台保存
			$.getJSON(
				"/sim/assGroup/addAssGroup?_time="+new Date().getTime(),
				{groupName:title},
				function(result){
					if( !result ) {
						showErrorMessage("数据出错");
					} else if (result.status) {
						sysconfigAssGroup.romanceDraggable(title,result.message);
					} else {
						showErrorMessage(result.message);
					}
			});
		}else{
			showErrorMessage("新业务组名称长度不能超过15个字");
		}
	}
	sysconfigAssGroup.closeAddAssGroupDialog();
}
$(function(){
	sysconfigAssGroup.initPage();
});
