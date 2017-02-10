<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/page/common/taglib.jsp" %>
<style>
	.control-group > .controls > select{
		width:90px;
	}
	.control-group > .controls > input{
		width:180px;
	}
</style>
<div class="btn-toolbar">
	<div class="btn-group">
	  <button class="btn" id="filter_editor_and_btn">AND</button>
	  <button class="btn" id="filter_editor_or_btn">OR</button>
	  <button class="btn" id="filter_editor_not_btn">NOT</button>
	</div>	
	<button class="btn btn-success" id="filter_editor_insert_btn">插入</button>
	<a class="easyui-linkbutton" id="filter_editor_remove_btn" data-options="disabled:true" style="margin:0 5px;">删除</a>
	<button class="btn btn-primary" id="filter_editor_finish_btn">完成</button>
	<button class="btn" onclick="$('#sysconfig_filter_dialog').dialog('close');">取消</button>		
</div>
<div class="container-fluid">
	<div class="row-fluid">
		<div class="span5 well">
			<ul id="filter_etitor_tree"></ul>  
			<div id="filter_etitor_tree_menu" class="easyui-menu" style="width:120px;">
				<div id="filter_etitor_tree_menu_and" data-options="iconCls:'conditions-and'">AND</div>
				<div id="filter_etitor_tree_menu_or" data-options="iconCls:'conditions-or'">OR</div>
				<div id="filter_etitor_tree_menu_not" data-options="iconCls:'conditions-not'">NOT</div>
				<div id="filter_etitor_tree_menu_remove" data-options="iconCls:'icon-remove'">删除</div>
			</div>			
		</div>
		<div class="span7">
			<form class="form-horizontal" id="filter_editor_form">
				<c:forEach var="field" items="${fieldset}">
			  		<div class="control-group" style="margin-bottom: 5px;">
			    		<label class="control-label" style="width: 100px;">${field.alias}</label>
		    			<div class="controls" style="margin-left: 110px;">
							<select class="input-small" name="${field.field}_exp" >
							    <option value="=">=</option>
							    <option value="&lt;&gt;">&lt;&gt;</option>
							    <option value="&gt;">&gt;</option>
							    <option value="&lt;">&lt;</option>
							    <option value="&gt;=">&gt;=</option>
							    <option value="&lt;=">&lt;=</option>
							</select>
							<c:choose>
								<c:when test="${field.values ne null}">
									<select name="${field.field}" style="width:181px;" id="${field.field}" dataType="${field.type }">
										<option value=""></option>
										<c:forEach var="opts" items="${field.values}">
											<c:set var="op" value="${fn:split(opts,'#')}" scope="page"></c:set>
											<option value="${op[0]}">${op[1]}</option>
										</c:forEach>	
									</select>						
								</c:when>
								<c:otherwise>
									<c:choose>
										<c:when test="${fn:toLowerCase(field.type) eq 'ip'}">
											<input type="text" data-rule="ipv4" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:when>
										<c:when test="${fn:toLowerCase(field.type) eq 'long'}">
											<input type="text" data-rule="integer[+]" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:when>
										<c:when test="${fn:toLowerCase(field.type) eq 'int'}">
											<input type="text" data-rule="integer[+];range[1~65536]" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:when>
										<c:when test="${fn:toLowerCase(field.type) eq 'mac'}">
											<input type="text" data-rule="mac" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:when>
										<c:when test="${fn:toLowerCase(field.type) eq 'date'}">
											<input type="text" class="form-datetime Wdate" data-rule="datetime" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:when>
										<c:otherwise>
											<input type="text" name="${field.field}" id="${field.field}" dataType="${field.type }">
										</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
		    			</div>
			  		</div>
			  	</c:forEach>
			</form>		
		</div>
	</div>
</div>
<script src="/js/sim/sysconfig/filter_editor.js" type="text/javascript"></script>