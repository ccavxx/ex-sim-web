<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/page/common/taglib.jsp"%>
<style>
<!--
.form-horizontal .control-group {
	margin-bottom: 10px;
}
-->
</style>
<form id="scheduleForm" class="form-horizontal" onclick="endRowEdit(lastEditRow)">
	<input type="hidden" name="operator" value="${empty statTask ? 'add' : 'update'}" id="schedule_operator"> 
	<input type="hidden" name="id" value="${statTask.id}" id="schedule_Id">
	<fieldset>
		<legend></legend>
		<div style="padding-left:100px;">
			<div class="control-group">
				<label class="control-label">名称</label>
				<div class="controls"> <input type="text" id="taskName" name="name" value="${statTask.name}"> </div>
			</div>
			<div class="control-group">
				<label class="control-label">邮箱地址</label>
				<div class="controls">
					<input type="text" id="email" name="email" value="${statTask.email }"> 
					<font color="red">提示：多个邮箱使用分号隔开</font>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">执行周期</label>
				<div class="controls"> <%@include file="/page/asset/poll_time.jsp"%> </div>
			</div>
			<div class="control-group">
				<label class="control-label">选择主题</label>
				<div class="controls">
					<a class="easyui-linkbutton" onclick="getTaskList()">选择主题</a>
					<div style="margin-top: 10px;color: red;">
						<div style="float: left;"><b>提示：</b></div>
						<div style="float: left;"><b>1、</b>单击日志源或时间间隔表格可以修改日志源和时间间隔</div>
						<div style="float: left;"><b>2、</b>日志源可以选择某一种类型日志(第三级)或者某一具体的日志源(第四级)</div>
						<div style="float: left;"><b>3、</b>如果时间间隔选择自定义，需要再点击<i class='icon-calendar'></i>选择时间范围</div>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label"></label>
				<div class="controls">
					<table id="subject_table" class="easyui-datagrid" style="width:900px;height:350px" 
						   data-options="singleSelect:true,onClickCell:onClickCell">
						<thead>
							<tr>
								<th data-options="field:'id',hidden:true">主题Id</th>
								<th data-options="field:'taskName',width:300">主题名称</th>
								<th data-options="field:'dataSource',formatter:deviceTypeFmt,width:200,editor:deviceTypeEditor">资产类型</th>
								<th data-options="field:'interval',formatter:intervalFmt,editor:intervalEditor,width:120">时间间隔</th>
								<th data-options="field:'diagram',width:100,formatter:formatterDiagram">结果类型</th>
								<th data-options="field:'creater',width:100">创建者</th>
								<!-- <th data-options="field:'sort',width:80,formatter:formatterSort">排序</th> -->
							</tr>
						</thead>
					</table>
				</div>
			</div>
			<div class="control-group">
				<div class="controls">
					<a class="easyui-linkbutton" data-options="iconCls:'icon-save'"
						onclick="validateName()">保存</a> <a class="easyui-linkbutton"
						data-options="iconCls:'icon-cancel'"
						onclick="closeScheduleDialog()">取消</a>
				</div>
			</div>
		</div>
		<div id="taskList_Dialog" class="easyui-dialog" title="选择主题" closed="true" style="width:750px;height:450px;"
			 data-options="iconCls:'icon-save',resizable:true,modal:true,buttons:'#dlg-butons'">
			<table id="taskList_table"></table>
		</div>
		<div id="dlg-butons">
			<a href="javascript:void(0);" class="easyui-linkbutton" onclick="getCheckTheme();">确定</a> <a href="javascript:void(0);"
				class="easyui-linkbutton" onclick="closeTaskListDialog();">关闭</a>
		</div>
	</fieldset>
</form>
<script  src="/js/sim/asset/poll_time.js"></script>
<script src="/js/sim/log/schedule_taskcreate.js"></script>
