<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<table class="easyui-datagrid" data-options="scrollbarSize:0,nowrap:false,fitColumns:true,singleSelect:true,title:'网卡信息',onLoadSuccess:generalLoadSuccess">
	<thead>  
		<tr>  
			<c:choose>
				<c:when test="${isWindows}">
					<th data-options="field:'DVC_CUSTOM_STRING1',width:150">IP地址</th>  
					<th data-options="field:'MESSAGE',width:150">描述</th> 
					<th data-options="field:'DVC_CUSTOM_STRING2',width:150">子网掩码</th> 
					<th data-options="field:'DVC_MAC_ADDRESS',width:150">mac地址</th>
					<th data-options="field:'DVC_CUSTOM_STRING3',width:150">网关</th> 
					<th data-options="field:'STATUS',width:150">状态</th> 
				</c:when>
				<c:otherwise>
					<th data-options="field:'MESSAGE',width:150">描述</th> 
				</c:otherwise>
			</c:choose>
		</tr>  
	</thead>  
	<tbody>
		<c:choose>
			<c:when test="${isWindows}">
				<c:forEach var="networkCard" items="${networkCardList}">
		        	<tr>  
			            <td>${networkCard.DVC_CUSTOM_STRING1 }</td>  
			            <td>${networkCard.MESSAGE}</td>
			            <td>${networkCard.DVC_CUSTOM_STRING2}</td>
			            <td>${networkCard.DVC_MAC_ADDRESS}</td>
			            <td>${networkCard.DVC_CUSTOM_STRING3}</td>
			            <td>${networkCard.STATUS}</td>
		        	</tr>  
				</c:forEach>
			</c:when>
			<c:otherwise>
				<c:forEach var="networkCard" items="${networkCardList}">
			        <tr>
			        	<% request.setAttribute("CRLF", "\n") ; request.setAttribute("SPACE", " ") ; %>
			        	<td>${fn:replace(fn:replace(networkCard.MESSAGE,CRLF,'</br>'),SPACE,'&nbsp;')}</td>
			        </tr>  
				</c:forEach>
			</c:otherwise>
		</c:choose>
    </tbody>  
</table>
