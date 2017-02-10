<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<table class="easyui-datagrid" data-options="scrollbarSize:0,fitColumns:true,singleSelect:true,title:'进程信息',onLoadSuccess:generalLoadSuccess">
	<thead>  
		<tr>  
			<th data-options="field:'NO',width:15">序号</th>  
			<th data-options="field:'DVC_PROCESS_NAME',width:150,formatter:simHandler.eventDescFormatter">进程名称</th>  
			<th data-options="field:'OBJECT_ID',width:150">进程ID</th> 
			<th data-options="field:'SRC_USER_NAME',width:150">用户名</th> 
			<th data-options="field:'FILE_PATH',width:150,formatter:simHandler.eventDescFormatter">路径</th>  
		</tr>  
	</thead>  
	<tbody>
		<c:forEach var="process" items="${processList}" varStatus="status">
	        <tr>  
	        	<td>${status.index + 1}</td>
	            <td>${process.DVC_PROCESS_NAME }</td>  
	            <td>${process.OBJECT_ID}</td>
	            <td>${process.SRC_USER_NAME}</td>
	            <td>${process.FILE_PATH}</td>
	        </tr>  
		</c:forEach>
    </tbody>  
</table>
