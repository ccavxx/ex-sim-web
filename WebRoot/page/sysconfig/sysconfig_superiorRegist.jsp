<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/page/common/taglib.jsp" %>
<div class="easyui-panel" style="padding: 5px;" data-options="fit:true,title:'上级管理',headerCls:'sim-panel-header',bodyCls:'sim-panel-body'">
	<div class="alert alert-info">
		<strong>描述 </strong>
		注册节点配置。
	</div>
	<div class="row-fluid">
		<div class="span7">
			<form class="bs-docs-example form-horizontal" id="sysconfig_superior_regist_form">
				<input type="hidden" name="resourceId" id="superior_regist_resourceId" value="${resourceId}"/>
				<input type="hidden" name="operator" id="superior_regist_operator"/>
				<div class="control-group">
					<label class="control-label"><span style="color:red;">*</span>上级IP地址</label>
					<div class="controls">
						<input type="text" name="registIp" id="updateRegistIp" value="${registIp}"/>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><span style="color:red;">*</span>本机名称</label>
					<div class="controls">
						<input type="text" name="registName" id="updateRegistName" value="${registName}"/>
					</div>
				</div>
				<div class="control-group">
					<div class="controls">
						<div id="sysconfig_superior_regist_form_regist_btn_div">
							<a class="easyui-linkbutton" id="sysconfig_superior_regist_form_regist_button" data-options="iconCls:'icon-apply'">注册</a>
						</div>
						<div id="sysconfig_superior_regist_form_update_btn_div">
							<a class="easyui-linkbutton" id="sysconfig_superior_regist_form_update_button" data-options="iconCls:'icon-edit'">变更</a>
						</div>
					</div>
				</div>
			</form>
		</div>
		<div class="span5 well">
			<p class="text-info"><strong>上级IP地址：</strong><span id="deleteRegistIp">${registIp}</span></p>
			<p class="text-info"><strong>本机名称：</strong><span id="deleteRegistName">${registName}</span></p>
			<p class="text-info"><a class="easyui-linkbutton" id="sysconfig_superior_regist_form_delete_button" data-options="iconCls:'icon-remove'">删除</a></p>
		</div>
	</div>
</div>
<script src="/js/sim/sysconfig/sysconfig_superiorregist.js" type="text/javascript"></script>
