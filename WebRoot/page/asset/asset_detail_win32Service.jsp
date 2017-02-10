<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<table class="easyui-datagrid" data-options="scrollbarSize:0,fitColumns:true,singleSelect:true,title:'设备服务信息',onLoadSuccess:generalLoadSuccess">
	<thead>  
		<tr>  
			<th data-options="field:'NO',width:15">序号</th>
			<th data-options="field:'DEST_SERVICE_NAME',width:100,formatter:simHandler.eventDescFormatter">服务名称</th>  
			<th data-options="field:'OBJECT_ID',width:30">进程ID</th>  
			<th data-options="field:'FILE_PATH',width:150,formatter:simHandler.eventDescFormatter">路径</th>
			<th data-options="field:'STATUS',width:50">状态</th> 
			<th data-options="field:'MESSAGE',width:200,formatter:simHandler.eventDescFormatter">描述</th> 
		</tr>  
	</thead>  
	<tbody>
		<c:forEach var="win32Service" items="${win32ServiceList}" varStatus="status">
	        <tr>  
	        	<td>${status.index + 1}</td> 
	            <td>${win32Service.DEST_SERVICE_NAME }</td>  
	            <td>${win32Service.OBJECT_ID}</td>
	            <td>${win32Service.FILE_PATH}</td>
	            <td>${win32Service.STATUS}</td>
	            <td>${win32Service.MESSAGE}</td>
	        </tr>  
		</c:forEach>
    </tbody>  
</table>
