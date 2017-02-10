<%@ page language="java" pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="easyui-layout" fit="true" style="font-size: 14px;">
	<div id="${classNameId}_source" data-options="region:'center',border:false,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" title="类" class="sim">
		<table>
			<c:forEach items="${lines}" varStatus="stat" var="lineContent">
				<c:if test="${fn:length(fn:trim(lineContent)) != 0 }">
					<tr id="${classNameId}_line_${stat.index+1}">
						<td width="35" style="background-color: #f5f5f5;padding-left: 3px;">${stat.index+1}</td>
						<td><pre style="margin: 0px;padding: 0px;">${lineContent}</pre></td>
					</tr>
				</c:if>
			</c:forEach>
		</table>
	</div>
	<div data-options="region:'east',split:true,width:200,collapsible:true,border:false,headerCls:'layoutWHeader',bodyCls:'layoutWBody'" title="方法" class="sim">
		<table>
			<c:forEach items="${methods}" var="method">
				<tr>
					<td width="16"><span onclick="debug.breakpoint(this,'${className}','${method.name}')" class="${method.hasBreakpoint ? 'icon-stop' : 'icon-start' } icon16 hand"/></td>
					<td width="35"><button class="btn btn-link" onclick="debug.methodClickHandler('${className}','${method.location}')">${method.name}:${method.location}</button></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>