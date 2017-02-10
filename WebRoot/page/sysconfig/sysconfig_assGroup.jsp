<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/page/common/taglib.jsp" %>
<style>
	.drag{
		width:200px;
		height:26px;
		padding:2px;
		margin:2px;
		border-left:1px solid #86BBD1;
		border-right:1px solid #86BBD1;
		background: url('/img/skin/datagrid/mid_bg2.png') repeat-x;
		text-align:left;
	}
	.dp{
		opacity:0.5;
		filter:alpha(opacity=50);
	}
	.over{
		background:#E6F3F7;
	}
	#sortable {
		list-style-type: none;
		margin: 0;
		padding: 0;
		width:100%;
	}
    #sortable li {
		border:1px solid #C4DBE9;
		margin: 3px 3px 0 0;
		padding:0;
		float: left;
		width: 260px;
		height: 300px;
		text-align: center;
	}
	.assGroup_sortable>li>div>.assGroup_div_body{
		overflow: auto;
		height:274px;
	}
	.assGroup_sortable>li>div>.assGroup_div_header{
		background: url('/img/skin/list/nav_list_bg.png') repeat-x;
		height:26px;
		line-height:26px;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table{
		width:260px;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.first{
		width:30px;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.second{
		width:200px;
		text-align:left;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.second input{
		height:20px;
		width:160px;
		font-size:12px;
		margin:0 0 3px 0;
		padding:0;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.third{
		width:15px;
		font-size:19px;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.third>a{
		text-decoration:none;
	}
	.assGroup_sortable>li>div>.assGroup_div_header>table>tbody>tr>td.fourth{
		width:2px;
	}
	.assGroup_sortable>li>div{
		width:260px;
		height:300px;
	}
	.drag-unedit{
		color:#CCC;
	}
	.drag-edit{
		color:#labebe;
	}
</style>
<div id="assGroup_tools">
	<a href="javascript:void(0)" title="新建" class="icon-add" id="assGroup_tools_add" ></a>
</div>
<div class="easyui-layout" data-options="fit:true">
	<div class="easyui-panel" id="assGroupTotalId" style="padding:5px;" data-options="region:'center',headerCls:'sim-panel-header',bodyCls:'sim-panel-body',
		title:'业务组管理',tools:'#assGroup_tools'">
		<ul id="sortable" class="assGroup_sortable">
			<c:forEach var="group" items="${assGroupList}">
				<li>
					<div id="th${group.groupId}">
						<div class="assGroup_div_header">
							<table>
								<tbody>
								
									<tr>
										<td class="first" ><i class="icon-th-list"></i></td>
										<td class="second" >
											<div class="editable" style="display:inline-block;"><span>${group.groupName}</span><c:if
											    test="${'默认组' ne group.groupName}"><i
											  	class='icon-edit' onclick="sysconfigAssGroup.assGroupShowInput(this)" style='margin-left:1px;height:16px;width:21px;'/>
											 </c:if>
											 </div>
												 <c:if test="${'默认组' ne group.groupName}">
													<div class="editable_input" style="display: none;">
														<input type="text" name="groupName" value="${group.groupName}" onblur="sysconfigAssGroup.assGroupEditOnblur(this,'${group.groupId}')"/>
													</div>
												 </c:if>
										</td>
										<td class="third" >
											<c:if test="${'默认组' ne group.groupName}">
												<a href="javascript:void(0);" onclick="sysconfigAssGroup.assGroupSortableRemoveLi(this,'${group.groupId}')">×</a>
											</c:if>
										</td>
										<td class="fourth" ></td>
									</tr>
								</tbody>
							</table>
						</div>
						<div class="assGroup_div_body" id="t${group.groupId}">
							<c:forEach var="asset" items="${group.assets}">
								<c:if test='${!asset.isDelete}'>
									<div id="d${asset.id}" class="drag ${asset.isEdit?'drag-edit':'drag-unedit'}">
										<table class="easyui-tooltip" data-options="position:'right'" title="${asset.ip}">
											<tbody>
												<tr>
													<td class="first" style="line-height:16px;"><i class='${asset.iconCls}'></i></td>
													<td><div style="width:160px;overflow:hidden;text-overflow:ellipsis;line-height:26px;padding-left:5px;">
															<nobr>${asset.name}</nobr>
														</div>
													</td>
												</tr>
											</tbody>
										</table>
									</div>
								</c:if>
							</c:forEach>
						</div>
					</div>
			    </li>
	   		</c:forEach>
		</ul>
	</div>
</div>
<div id="assGroup_tools_adddlg" class="easyui-dialog" style="width:450px;height:145px;padding:10px"
		data-options="
			title: '新建业务组',
			closed:true,
			modal:true,
			draggable:false,
			buttons: [{
				text:'保存',
				iconCls:'icon-save',
				handler:function(){
					sysconfigAssGroup.addAssGroup();
				}
			},{
				text:'取消',
				iconCls:'icon-cancel',
				handler:function(){
					sysconfigAssGroup.closeAddAssGroupDialog();
				}
			}]
		">
	<form id="assGroup_tools_addForm">
		<table>
			<tr>
				<td>新业务组名称：</td>
				<td><input name="groupName" /><span style="color:red;padding-left:10px;">长度不能超过15个字</span></td>
			</tr>
		</table>
	</form>
</div>
<script src="/js/sim/sysconfig/sysconfig_assGroup.js"></script>
